package servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Map;
import java.util.HashMap;
import java.util.Calendar;
import java.util.ArrayList;
import java.util.Date;
import java.util.Collection;
import javax.ejb.EJB;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import stateless.SoilWaterBalanceServiceBean;
import stateless.ClimateRecordServiceBean;
import stateless.CropServiceBean;
import stateless.ParcelServiceBean;
import stateless.IrrigationRecordServiceBean;
import stateless.MaximumInsolationServiceBean;
import stateless.PlantingRecordServiceBean;
import stateless.PlantingRecordStatusServiceBean;
import stateless.SecretKeyServiceBean;
import stateless.SolarRadiationServiceBean;
import stateless.TypePrecipitationServiceBean;
import stateless.MonthServiceBean;
import stateless.UserServiceBean;
import stateless.OptionServiceBean;
import stateless.LatitudeServiceBean;
import stateless.SessionServiceBean;
import stateless.Page;
import climate.ClimateClient;
import et.HargreavesEto;
import et.Etc;
import irrigation.WaterMath;
import model.ClimateRecord;
import model.Crop;
import model.IrrigationRecord;
import model.Parcel;
import model.PlantingRecord;
import model.PlantingRecordData;
import model.PlantingRecordStatus;
import model.SoilMoistureLevelGraph;
import model.SoilWaterBalance;
import model.Soil;
import model.IrrigationWaterNeedFormData;
import model.User;
import model.Option;
import util.ErrorResponse;
import util.PersonalizedResponse;
import util.ReasonError;
import util.RequestManager;
import util.SourceUnsatisfiedResponse;
import util.UtilConnection;
import util.UtilDate;
import utilJwt.AuthHeaderManager;
import utilJwt.JwtManager;

@Path("/plantingRecords")
public class PlantingRecordRestServlet {

  // inject a reference to the PlantingRecordServiceBean
  @EJB PlantingRecordServiceBean plantingRecordService;
  @EJB ParcelServiceBean parcelService;
  @EJB ClimateRecordServiceBean climateRecordService;
  @EJB CropServiceBean cropService;
  @EJB IrrigationRecordServiceBean irrigationRecordService;
  @EJB PlantingRecordStatusServiceBean statusService;
  @EJB SolarRadiationServiceBean solarService;
  @EJB MaximumInsolationServiceBean insolationService;
  @EJB SecretKeyServiceBean secretKeyService;
  @EJB MonthServiceBean monthService;
  @EJB LatitudeServiceBean latitudeService;
  @EJB UserServiceBean userService;
  @EJB OptionServiceBean optionService;
  @EJB SoilWaterBalanceServiceBean soilWaterBalanceService;
  @EJB SessionServiceBean sessionService;
  @EJB TypePrecipitationServiceBean typePrecipService;

  // Mapea lista de pojo a JSON
  ObjectMapper mapper = new ObjectMapper();

  private final int UNAUTHORIZED = 401;
  private final int TOO_MANY_REQUESTS = 429;
  private final int SERVICE_UNAVAILABLE = 503;
  private final int MINIMUM_DAYS_BETWEEN_SEED_DATE_AND_CURRENT_DATE = 2;
  private final String UNDEFINED_VALUE = "undefined";
  private final String NULL_VALUE = "null";

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response findAll(@Context HttpHeaders request) throws IOException {
    Response givenResponse = RequestManager.validateAuthHeader(request, secretKeyService.find());

    /*
     * Si el estado de la respuesta obtenida de validar el
     * encabezado de autorizacion de una peticion HTTP NO
     * es ACCEPTED, se devuelve el estado de error de la misma.
     * 
     * Que el estado de la respuesta obtenida de validar el
     * encabezado de autorizacion de una peticion HTTP sea
     * ACCEPTED, significa que la peticion es valida,
     * debido a que el encabezado de autorizacion de la misma
     * cumple las siguientes condiciones:
     * - Esta presente.
     * - No esta vacio.
     * - Cumple con la convencion de JWT.
     * - Contiene un JWT valido.
     */
    if (!RequestManager.isAccepted(givenResponse)) {
      return givenResponse;
    }

    /*
     * Obtiene el JWT del valor del encabezado de autorizacion
     * de una peticion HTTP
     */
    String jwt = AuthHeaderManager.getJwt(AuthHeaderManager.getAuthHeaderValue(request));

    /*
     * Valor de la clave secreta con la que la aplicacion firma
     * un JWT
     */
    String secretKeyValue = secretKeyService.find().getValue();

    /*
     * Obtiene el ID de usuario contenido en la carga util del
     * JWT del encabezado de autorizacion de una peticion HTTP
     */
    int userId = JwtManager.getUserId(jwt, secretKeyValue);

    /*
     * Si el usuario que solicita esta operacion NO tiene una
     * sesion activa, la aplicacion del lador servidor devuelve
     * el mensaje 401 (Unauthorized) junto con el mensaje "No
     * tiene una sesion activa" y no se realiza la operacion
     * solicitada
     */
    if (!sessionService.checkActiveSession(userId)) {
      return Response.status(Response.Status.UNAUTHORIZED).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.NO_ACTIVE_SESSION))).build();
    }

    /*
     * Si la fecha de emision del JWT de un usuario NO es igual
     * a la fecha de emision de la sesion activa del usuario,
     * la aplicacion del lado servidor retorna el mensaje HTTP
     * 400 (Bad request) junto con el mensaje "El JWT no
     * corresponde a una sesion abierta" y no se realiza la
     * operacion solicitada.
     * 
     * Se debe tener en cuenta que el metodo checkDateIssueLastSession
     * de la clase SessionServiceBean debe ser invocado luego
     * de invocar el metodo checkActiveSession de la misma
     * clase, ya que de lo contrario se puede comparar la
     * fecha de emision de un JWT con una sesion inactiva,
     * lo cual es incorrecto porque la fecha de emision de un
     * JWT se debe comparar con la fecha de emision de una
     * sesion activa. El motivo por el cual puede ocurrir esta
     * comparacion con una sesion inactiva es que el metodo
     * findLastSession recupera la ultima sesion del usuario,
     * independientemente de si esta activa o inactiva.
     * 
     * Este control se implementa para evitar que se puedan
     * recuperar datos mediante peticiones HTTP haciendo uso
     * de un JWT valido, pero que es de una sesion que fue
     * cerrada por el usuario.
     */
    if (!sessionService.checkDateIssueLastSession(userId, JwtManager.getDateIssue(jwt, secretKeyValue))) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.JWT_NOT_ASSOCIATED_WITH_ACTIVE_SESSION)))
          .build();
    }

    /*
     * Si el valor del encabezado de autorizacion de la peticion HTTP
     * dada, tiene un JWT valido, la aplicacion del lado servidor
     * devuelve el mensaje HTTP 200 (Ok) junto con los datos solicitados
     * por el cliente
     */
    return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(plantingRecordService.findAll(userId))).build();
  }

  @GET
  @Path("/findAllPagination")
  @Produces(MediaType.APPLICATION_JSON)
  public Response findAllPagination(@Context HttpHeaders request, @QueryParam("page") Integer page,
      @QueryParam("cant") Integer cant, @QueryParam("search") String search) throws IOException, ParseException {
    Response givenResponse = RequestManager.validateAuthHeader(request, secretKeyService.find());

    /*
     * Si el estado de la respuesta obtenida de validar el
     * encabezado de autorizacion de una peticion HTTP NO
     * es ACCEPTED, se devuelve el estado de error de la misma.
     * 
     * Que el estado de la respuesta obtenida de validar el
     * encabezado de autorizacion de una peticion HTTP sea
     * ACCEPTED, significa que la peticion es valida,
     * debido a que el encabezado de autorizacion de la misma
     * cumple las siguientes condiciones:
     * - Esta presente.
     * - No esta vacio.
     * - Cumple con la convencion de JWT.
     * - Contiene un JWT valido.
     */
    if (!RequestManager.isAccepted(givenResponse)) {
      return givenResponse;
    }

    /*
     * Obtiene el JWT del valor del encabezado de autorizacion
     * de una peticion HTTP
     */
    String jwt = AuthHeaderManager.getJwt(AuthHeaderManager.getAuthHeaderValue(request));

    /*
     * Valor de la clave secreta con la que la aplicacion firma
     * un JWT
     */
    String secretKeyValue = secretKeyService.find().getValue();

    /*
     * Obtiene el ID de usuario contenido en la carga util del
     * JWT del encabezado de autorizacion de una peticion HTTP
     */
    int userId = JwtManager.getUserId(jwt, secretKeyValue);

    /*
     * Si el usuario que solicita esta operacion NO tiene una
     * sesion activa, la aplicacion del lador servidor devuelve
     * el mensaje 401 (Unauthorized) junto con el mensaje "No
     * tiene una sesion activa" y no se realiza la operacion
     * solicitada
     */
    if (!sessionService.checkActiveSession(userId)) {
      return Response.status(Response.Status.UNAUTHORIZED).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.NO_ACTIVE_SESSION))).build();
    }

    /*
     * Si la fecha de emision del JWT de un usuario NO es igual
     * a la fecha de emision de la sesion activa del usuario,
     * la aplicacion del lado servidor retorna el mensaje HTTP
     * 400 (Bad request) junto con el mensaje "El JWT no
     * corresponde a una sesión abierta" y no se realiza la
     * operacion solicitada.
     * 
     * Se debe tener en cuenta que el metodo checkDateIssueLastSession
     * de la clase SessionServiceBean debe ser invocado luego
     * de invocar el metodo checkActiveSession de la misma
     * clase, ya que de lo contrario se puede comparar la
     * fecha de emision de un JWT con una sesion inactiva,
     * lo cual es incorrecto porque la fecha de emision de un
     * JWT se debe comparar con la fecha de emision de una
     * sesion activa. El motivo por el cual puede ocurrir esta
     * comparacion con una sesion inactiva es que el metodo
     * findLastSession recupera la ultima sesion del usuario,
     * independientemente de si esta activa o inactiva.
     * 
     * Este control se implementa para evitar que se puedan
     * recuperar datos mediante peticiones HTTP haciendo uso
     * de un JWT valido, pero que es de una sesion que fue
     * cerrada por el usuario.
     */
    if (!sessionService.checkDateIssueLastSession(userId, JwtManager.getDateIssue(jwt, secretKeyValue))) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.JWT_NOT_ASSOCIATED_WITH_ACTIVE_SESSION)))
          .build();
    }

    Map<String, String> map = new HashMap<String, String>();

    // Convert JSON string to Map
    map = mapper.readValue(search, new TypeReference<Map<String, String>>() {});

    /*
     * Si el valor del encabezado de autorizacion de la peticion HTTP
     * dada, tiene un JWT valido, la aplicacion del lado servidor
     * devuelve el mensaje HTTP 200 (Ok) junto con los datos solicitados
     * por el cliente
     */
    Page<PlantingRecord> plantingRecords = plantingRecordService.findAllPagination(userId, page, cant, map);
    return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(plantingRecords)).build();
  }

  @GET
  @Path("/filter")
  @Produces(MediaType.APPLICATION_JSON)
  public Response filter(@Context HttpHeaders request, @QueryParam("parcelName") String parcelName,
      @QueryParam("dateFrom") String stringDateFrom, @QueryParam("dateUntil") String stringDateUntil) throws IOException, ParseException {
    Response givenResponse = RequestManager.validateAuthHeader(request, secretKeyService.find());

    /*
     * Si el estado de la respuesta obtenida de validar el
     * encabezado de autorizacion de una peticion HTTP NO
     * es ACCEPTED, se devuelve el estado de error de la misma.
     * 
     * Que el estado de la respuesta obtenida de validar el
     * encabezado de autorizacion de una peticion HTTP sea
     * ACCEPTED, significa que la peticion es valida,
     * debido a que el encabezado de autorizacion de la misma
     * cumple las siguientes condiciones:
     * - Esta presente.
     * - No esta vacio.
     * - Cumple con la convencion de JWT.
     * - Contiene un JWT valido.
     */
    if (!RequestManager.isAccepted(givenResponse)) {
      return givenResponse;
    }

    /*
     * Obtiene el JWT del valor del encabezado de autorizacion
     * de una peticion HTTP
     */
    String jwt = AuthHeaderManager.getJwt(AuthHeaderManager.getAuthHeaderValue(request));

    /*
     * Valor de la clave secreta con la que la aplicacion firma
     * un JWT
     */
    String secretKeyValue = secretKeyService.find().getValue();

    /*
     * Obtiene el ID de usuario contenido en la carga util del
     * JWT del encabezado de autorizacion de una peticion HTTP
     */
    int userId = JwtManager.getUserId(jwt, secretKeyValue);

    /*
     * Si el usuario que solicita esta operacion NO tiene una
     * sesion activa, la aplicacion del lador servidor devuelve
     * el mensaje 401 (Unauthorized) junto con el mensaje "No
     * tiene una sesion activa" y no se realiza la operacion
     * solicitada
     */
    if (!sessionService.checkActiveSession(userId)) {
      return Response.status(Response.Status.UNAUTHORIZED).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.NO_ACTIVE_SESSION))).build();
    }

    /*
     * Si la fecha de emision del JWT de un usuario NO es igual
     * a la fecha de emision de la sesion activa del usuario,
     * la aplicacion del lado servidor retorna el mensaje HTTP
     * 400 (Bad request) junto con el mensaje "El JWT no
     * corresponde a una sesion abierta" y no se realiza la
     * operacion solicitada.
     * 
     * Se debe tener en cuenta que el metodo checkDateIssueLastSession
     * de la clase SessionServiceBean debe ser invocado luego
     * de invocar el metodo checkActiveSession de la misma
     * clase, ya que de lo contrario se puede comparar la
     * fecha de emision de un JWT con una sesion inactiva,
     * lo cual es incorrecto porque la fecha de emision de un
     * JWT se debe comparar con la fecha de emision de una
     * sesion activa. El motivo por el cual puede ocurrir esta
     * comparacion con una sesion inactiva es que el metodo
     * findLastSession recupera la ultima sesion del usuario,
     * independientemente de si esta activa o inactiva.
     * 
     * Este control se implementa para evitar que se puedan
     * recuperar datos mediante peticiones HTTP haciendo uso
     * de un JWT valido, pero que es de una sesion que fue
     * cerrada por el usuario.
     */
    if (!sessionService.checkDateIssueLastSession(userId, JwtManager.getDateIssue(jwt, secretKeyValue))) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.JWT_NOT_ASSOCIATED_WITH_ACTIVE_SESSION)))
          .build();
    }

    /*
     * Si el nombre de la parcela NO esta definido, la aplicacion
     * del lado servidor retorna el mensaje HTTP 400 (Bad request)
     * junto con el mensaje "La parcela debe estar definida" y no
     * se realiza la operacion solicitada
     */
    if (parcelName == null) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.INDEFINITE_PARCEL))).build();
    }

    /*
     * Si el usuario que realiza esta peticion NO tiene una
     * parcela con el nombre elegido, la aplicacion del lado
     * servidor retorna el mensaje HTTP 404 (Resource not found)
     * junto con el mensaje "La parcela seleccionada no existe"
     * y no se realiza la peticion solicitada
     */
    if (!parcelService.checkExistence(userId, parcelName)) {
      return Response.status(Response.Status.NOT_FOUND).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.NON_EXISTENT_PARCEL))).build();
    }

    /*
     * En la aplicacion del lado del navegador Web las variables
     * correspondientes a la fecha desde y a la fecha hasta pueden
     * tener el valor undefined o el valor null. Estos valores
     * representan que una variable no tiene un valor asignado.
     * En este caso indican que una variable no tiene asignada
     * una fecha. Cuando esto ocurre y se realiza una peticion
     * HTTP a este metodo con una de estas variables con uno de
     * dichos dos valores, las variables de tipo String stringDateFrom
     * y stringDateUntil tienen como contenido la cadena "undefined"
     * o la cadena "null". En Java para representar adecuadamente
     * que la fecha desde y/o la fecha hasta NO tienen una fecha
     * asignada, en caso de que provengan de la aplicacion del
     * lado del navegador web con el valor undefined o el valor
     * null, se debe asignar el valor null a las variables
     * stringDateFrom y stringDateUntil.
     */
    if (stringDateFrom != null && (stringDateFrom.equals(UNDEFINED_VALUE) || stringDateFrom.equals(NULL_VALUE))) {
      stringDateFrom = null;
    }

    if (stringDateUntil != null && (stringDateUntil.equals(UNDEFINED_VALUE) || stringDateUntil.equals(NULL_VALUE))) {
      stringDateUntil = null;
    }

    /*
     * Siempre y cuando no se elimine el control sobre el nombre
     * de parcela, si la fecha desde y la fecha hasta NO estan
     * definidas, la aplicacion del lado servidor retorna una
     * coleccion de informes estadisticos pertenecientes a
     * una parcela que tiene un nombre
     */
    if (stringDateFrom == null && stringDateUntil == null) {
      return Response.status(Response.Status.OK)
          .entity(mapper.writeValueAsString(plantingRecordService.findAllByParcelName(userId, parcelName)))
          .build();
    }

    Parcel parcel = parcelService.find(userId, parcelName);
    SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
    Date dateFrom;
    Date dateUntil;

    /*
     * Si la fecha desde elegida es estrictamente mayor al año
     * maximo (9999), la aplicacion del lado servidor retorna
     * el mensaje HTTP 400 (Bad request) junto con el mensaje
     * "La fecha desde no debe ser estrictamente mayor a 9999"
     * y no se realiza la operacion solicitada
     */
    if (stringDateFrom != null) {
      dateFrom = new Date(dateFormatter.parse(stringDateFrom).getTime());

      if (UtilDate.yearIsGreaterThanMaximum(UtilDate.toCalendar(dateFrom))) {
        return Response.status(Response.Status.BAD_REQUEST)
            .entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.DATE_FROM_GREATEST_TO_MAXIMUM)))
            .build();
      }

    }

    /*
     * Si la fecha desde elegida es estrictamente mayor al año
     * maximo (9999), la aplicacion del lado servidor retorna
     * el mensaje HTTP 400 (Bad request) junto con el mensaje
     * "La fecha hasta no debe ser estrictamente mayor a 9999"
     * y no se realiza la operacion solicitada
     */
    if (stringDateUntil != null) {
      dateUntil = new Date(dateFormatter.parse(stringDateUntil).getTime());

      if (UtilDate.yearIsGreaterThanMaximum(UtilDate.toCalendar(dateUntil))) {
        return Response.status(Response.Status.BAD_REQUEST)
            .entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.DATE_UNTIL_GREATEST_TO_MAXIMUM)))
            .build();
      }

    }

    /*
     * Siempre y cuando no se elimine el control sobre el nombre
     * de parcela, si la fecha desde esta definida y la fecha
     * hasta NO esta definida, la aplicacion del lado servidor
     * retorna una coleccion de informes estadisticos pertenecientes
     * a una parcela que tiene un nombre y una fecha desde mayor
     * o igual a la fecha desde elegida
     */
    if (stringDateFrom != null && stringDateUntil == null) {
      dateFrom = new Date(dateFormatter.parse(stringDateFrom).getTime());
      return Response.status(Response.Status.OK)
          .entity(mapper.writeValueAsString(plantingRecordService.findAllByDateGreaterThanOrEqual(userId, parcel.getId(), UtilDate.toCalendar(dateFrom))))
          .build();
    }

    /*
     * Siempre y cuando no se elimine el control sobre el nombre
     * de parcela, si la fecha desde NO esta definida y la fecha
     * hasta esta definida, la aplicacion del lado servidor retorna
     * una coleccion de informes estadisticos pertenecientes a
     * una parcela que tiene un nombre y una fecha hasta menor
     * o igual a la fecha hasta elegida
     */
    if (stringDateFrom == null && stringDateUntil != null) {
      dateUntil = new Date(dateFormatter.parse(stringDateUntil).getTime());
      return Response.status(Response.Status.OK)
          .entity(mapper.writeValueAsString(plantingRecordService.findAllByDateLessThanOrEqual(userId, parcel.getId(), UtilDate.toCalendar(dateUntil))))
          .build();
    }

    /*
     * Si la fecha desde y la fecha hasta estan definidas, y la
     * fecha desde NO es estrictamente mayor a la fecha hasta, y
     * el nombre de la parcela esta definido, la aplicacion del
     * lado del servidor retorna una coleccion de informes
     * estadistico pertenecientes a una parcela de un usuario
     * que tienen una fecha desde mayor o igual a la fecha desde
     * elegida y una fecha hasta menor o igual a la fecha hasta
     * elegida
     */
    dateFrom = new Date(dateFormatter.parse(stringDateFrom).getTime());
    dateUntil = new Date(dateFormatter.parse(stringDateUntil).getTime());

    Calendar dateFromCalendar = UtilDate.toCalendar(dateFrom);
    Calendar dateUntilCalendar = UtilDate.toCalendar(dateUntil);

    /*
     * Si la fecha desde es mayor o igual a la fecha hasta, la
     * aplicacion del lado servidor retorna el mensaje HTTP 400
     * (Bad request) junto con el mensaje "La fecha desde no debe
     * ser mayor o igual a la fecha hasta" y no se realiza la
     * operacion solicitada
     */
    if (UtilDate.compareTo(dateFromCalendar, dateUntilCalendar) >= 0) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.DATE_FROM_AND_DATE_UNTIL_OVERLAPPING)))
          .build();
    }

    /*
     * Si el valor del encabezado de autorizacion de la peticion
     * HTTP dada, tiene un JWT valido, la aplicacion del lado
     * servidor devuelve el mensaje HTTP 200 (Ok) junto con los
     * datos solicitados por el cliente
     */
    return Response.status(Response.Status.OK)
        .entity(mapper.writeValueAsString(plantingRecordService.findByAllFilterParameters(userId, parcel.getId(), dateFromCalendar, dateUntilCalendar)))
        .build();
  }

  @GET
  @Path("/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response find(@Context HttpHeaders request, @PathParam("id") int plantingRecordId) throws IOException {
    Response givenResponse = RequestManager.validateAuthHeader(request, secretKeyService.find());

    /*
     * Si el estado de la respuesta obtenida de validar el
     * encabezado de autorizacion de una peticion HTTP NO
     * es ACCEPTED, se devuelve el estado de error de la misma.
     * 
     * Que el estado de la respuesta obtenida de validar el
     * encabezado de autorizacion de una peticion HTTP sea
     * ACCEPTED, significa que la peticion es valida,
     * debido a que el encabezado de autorizacion de la misma
     * cumple las siguientes condiciones:
     * - Esta presente.
     * - No esta vacio.
     * - Cumple con la convencion de JWT.
     * - Contiene un JWT valido.
     */
    if (!RequestManager.isAccepted(givenResponse)) {
      return givenResponse;
    }

    /*
     * Obtiene el JWT del valor del encabezado de autorizacion
     * de una peticion HTTP
     */
    String jwt = AuthHeaderManager.getJwt(AuthHeaderManager.getAuthHeaderValue(request));

    /*
     * Valor de la clave secreta con la que la aplicacion firma
     * un JWT
     */
    String secretKeyValue = secretKeyService.find().getValue();

    /*
     * Obtiene el ID de usuario contenido en la carga util del
     * JWT del encabezado de autorizacion de una peticion HTTP
     */
    int userId = JwtManager.getUserId(jwt, secretKeyValue);

    /*
     * Si el usuario que solicita esta operacion NO tiene una
     * sesion activa, la aplicacion del lador servidor devuelve
     * el mensaje 401 (Unauthorized) junto con el mensaje "No
     * tiene una sesion activa" y no se realiza la operacion
     * solicitada
     */
    if (!sessionService.checkActiveSession(userId)) {
      return Response.status(Response.Status.UNAUTHORIZED).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.NO_ACTIVE_SESSION))).build();
    }

    /*
     * Si la fecha de emision del JWT de un usuario NO es igual
     * a la fecha de emision de la sesion activa del usuario,
     * la aplicacion del lado servidor retorna el mensaje HTTP
     * 400 (Bad request) junto con el mensaje "El JWT no
     * corresponde a una sesion abierta" y no se realiza la
     * operacion solicitada.
     * 
     * Se debe tener en cuenta que el metodo checkDateIssueLastSession
     * de la clase SessionServiceBean debe ser invocado luego
     * de invocar el metodo checkActiveSession de la misma
     * clase, ya que de lo contrario se puede comparar la
     * fecha de emision de un JWT con una sesion inactiva,
     * lo cual es incorrecto porque la fecha de emision de un
     * JWT se debe comparar con la fecha de emision de una
     * sesion activa. El motivo por el cual puede ocurrir esta
     * comparacion con una sesion inactiva es que el metodo
     * findLastSession recupera la ultima sesion del usuario,
     * independientemente de si esta activa o inactiva.
     * 
     * Este control se implementa para evitar que se puedan
     * recuperar datos mediante peticiones HTTP haciendo uso
     * de un JWT valido, pero que es de una sesion que fue
     * cerrada por el usuario.
     */
    if (!sessionService.checkDateIssueLastSession(userId, JwtManager.getDateIssue(jwt, secretKeyValue))) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.JWT_NOT_ASSOCIATED_WITH_ACTIVE_SESSION)))
          .build();
    }

    /*
     * Si el dato solicitado no existe en la base de datos
     * subyacente, la aplicacion del lado servidor devuelve
     * el mensaje HTTP 404 (Not found) junto con el mensaje
     * "Recurso no encontrado" y no se realiza la operacion
     * solicitada
     */
    if (!plantingRecordService.checkExistence(plantingRecordId)) {
      return Response.status(Response.Status.NOT_FOUND).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.RESOURCE_NOT_FOUND))).build();
    }

    /*
     * Si al usuario que hizo esta peticion HTTP, no le pertenece
     * el dato solicitado, la aplicacion del lado servidor
     * devuelve el mensaje HTTP 403 (Forbidden) junto con el
     * mensaje "Acceso no autorizado" (contenido en el enum
     * ReasonError) y no se realiza la operacion solicitada
     */
    if (!plantingRecordService.checkUserOwnership(userId, plantingRecordId)) {
      return Response.status(Response.Status.FORBIDDEN).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.UNAUTHORIZED_ACCESS))).build();
    }

    PlantingRecord plantingRecord = plantingRecordService.findByUserId(userId, plantingRecordId);
    Calendar seedDate = plantingRecord.getSeedDate();
    Calendar harvestDate = plantingRecord.getHarvestDate();
    Calendar deathDate = plantingRecord.getDeathDate();
    PlantingRecordStatus status = plantingRecord.getStatus();
    boolean flagNotGenerateSoilMoistureGraph = plantingRecord.getFlagNotGenerateSoilMoistureGraph();

    PlantingRecordData plantingRecordData = new PlantingRecordData();
    plantingRecordData.setPlantingRecord(plantingRecord);

    int parcelId = plantingRecord.getParcel().getId();

    /*
     * En Java las variables de instancia de tipo primitivo
     * de tipo por referencia se inicializan de forma automatica
     * con un valor por defecto. En el caso de las variables
     * de instancia de tipo boolean (tipo primitivo), estas
     * se inicializan de manera automatica con el valor false.
     * 
     * La clase SoilMoistureLevelGraph tiene una variable de
     * de tipo boolean llamada showGraph. Al instanciar la
     * clase SoilMoistureLevelGraph, la variable showGraph
     * se inicializa de manera automatica en false. Esta
     * instruccion es para, que mediante la variable showGraph
     * en false, no mostrar el grafico de la evolucion diaria
     * del nivel de humedad del suelo para un registro de
     * plantacion perteneciente a una parcela que NO tiene la
     * bandera suelo activa en sus opciones.
     */
    plantingRecordData.setSoilMoistureLevelGraph(new SoilMoistureLevelGraph());

    /*
     * - Si la bandera flagNotGenerateSoilMoistureGraph de un registro de
     * plantacion tiene el valor false,
     * - si la bandera suelo de las opciones de la parcela perteneciente
     * a un registro de plantacion, esta activa,
     * - si el estado de un registro de plantacion es "Muerto" y
     * - si en el periodo correspondientes al estado "Muerto" la parcela
     * tiene balances hidricos de suelo,
     * 
     * se genera el grafico que representa la evolucion diaria del nivel de
     * humedad del suelo para que pueda ser visualizado cuando se presiona
     * el boton de visualizacion sobre un registro de plantacion que tiene
     * el estado "Muerto" y que pertenece a una parcela que tiene la bandera
     * suelo activa en sus opciones.
     */
    if (!flagNotGenerateSoilMoistureGraph && plantingRecord.getParcel().getOption().getSoilFlag()) {

      if (statusService.equals(status, statusService.findDeadStatus())
          && !soilWaterBalanceService.findAllFromDateFromToDateUntil(parcelId, seedDate, deathDate).isEmpty()) {
        plantingRecordData.setSoilMoistureLevelGraph(generateSoilMoistureLevelGraph(plantingRecord));
      }

    }

    /*
     * Si el valor del encabezado de autorizacion de la peticion HTTP
     * dada, tiene un JWT valido, la aplicacion del lado servidor
     * devuelve el mensaje HTTP 200 (Ok) junto con los datos solicitados
     * por el cliente
     */
    // return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(plantingRecordService.findByUserId(userId, plantingRecordId))).build();
    return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(plantingRecordData)).build();
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  public Response create(@Context HttpHeaders request, String json) throws IOException {
    Response givenResponse = RequestManager.validateAuthHeader(request, secretKeyService.find());

    /*
     * Si el estado de la respuesta obtenida de validar el
     * encabezado de autorizacion de una peticion HTTP NO
     * es ACCEPTED, se devuelve el estado de error de la misma.
     * 
     * Que el estado de la respuesta obtenida de validar el
     * encabezado de autorizacion de una peticion HTTP sea
     * ACCEPTED, significa que la peticion es valida,
     * debido a que el encabezado de autorizacion de la misma
     * cumple las siguientes condiciones:
     * - Esta presente.
     * - No esta vacio.
     * - Cumple con la convencion de JWT.
     * - Contiene un JWT valido.
     */
    if (!RequestManager.isAccepted(givenResponse)) {
      return givenResponse;
    }

    /*
     * Obtiene el JWT del valor del encabezado de autorizacion
     * de una peticion HTTP
     */
    String jwt = AuthHeaderManager.getJwt(AuthHeaderManager.getAuthHeaderValue(request));

    /*
     * Valor de la clave secreta con la que la aplicacion firma
     * un JWT
     */
    String secretKeyValue = secretKeyService.find().getValue();

    /*
     * Obtiene el ID de usuario contenido en la carga util del
     * JWT del encabezado de autorizacion de una peticion HTTP
     */
    int userId = JwtManager.getUserId(jwt, secretKeyValue);

    /*
     * Si el usuario que solicita esta operacion NO tiene una
     * sesion activa, la aplicacion del lador servidor devuelve
     * el mensaje 401 (Unauthorized) junto con el mensaje "No
     * tiene una sesion activa" y no se realiza la operacion
     * solicitada
     */
    if (!sessionService.checkActiveSession(userId)) {
      return Response.status(Response.Status.UNAUTHORIZED).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.NO_ACTIVE_SESSION))).build();
    }

    /*
     * Si la fecha de emision del JWT de un usuario NO es igual
     * a la fecha de emision de la sesion activa del usuario,
     * la aplicacion del lado servidor retorna el mensaje HTTP
     * 400 (Bad request) junto con el mensaje "El JWT no
     * corresponde a una sesion abierta" y no se realiza la
     * operacion solicitada.
     * 
     * Se debe tener en cuenta que el metodo checkDateIssueLastSession
     * de la clase SessionServiceBean debe ser invocado luego
     * de invocar el metodo checkActiveSession de la misma
     * clase, ya que de lo contrario se puede comparar la
     * fecha de emision de un JWT con una sesion inactiva,
     * lo cual es incorrecto porque la fecha de emision de un
     * JWT se debe comparar con la fecha de emision de una
     * sesion activa. El motivo por el cual puede ocurrir esta
     * comparacion con una sesion inactiva es que el metodo
     * findLastSession recupera la ultima sesion del usuario,
     * independientemente de si esta activa o inactiva.
     * 
     * Este control se implementa para evitar que se puedan
     * recuperar datos mediante peticiones HTTP haciendo uso
     * de un JWT valido, pero que es de una sesion que fue
     * cerrada por el usuario.
     */
    if (!sessionService.checkDateIssueLastSession(userId, JwtManager.getDateIssue(jwt, secretKeyValue))) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.JWT_NOT_ASSOCIATED_WITH_ACTIVE_SESSION)))
          .build();
    }

    /* 
     * Si el objeto correspondiente a la referencia contenida
     * en la variable de tipo por referencia de tipo String json,
     * esta vacio, significa que el formulario del dato correspondiente
     * a esta clase, esta vacio. Por lo tanto, la aplicacion del
     * lado servidor retorna el mensaje HTTP 400 (Bad request)
     * junto con el mensaje "Debe proporcionar todos los datos
     * requeridos" y no se realiza la operacion solicitada
     */
    if (json.isEmpty()) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.EMPTY_DATA))).build();
    }

    PlantingRecord newPlantingRecord = mapper.readValue(json, PlantingRecord.class);
    Calendar seedDate = newPlantingRecord.getSeedDate();
    Calendar harvestDate = newPlantingRecord.getHarvestDate();

    /*
     * Si la fecha de siembra de un nuevo registro de plantacion
     * NO esta definida, la aplicacion del lado servidor retorna
     * el mensaje HTTP 400 (Bad request) junto con el mensaje
     * "La fecha de siembra debe estar definida" y no se realiza
     * la operacion solicitada
     */
    if (seedDate == null) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.UNDEFINED_SEED_DATE))).build();
    }

    /*
     * Si la fecha de cosecha de un nuevo registro de plantacion
     * NO esta definida, la aplicacion del lado servidor retorna
     * el mensaje HTTP 400 (Bad request) junto con el mensaje
     * "La fecha de cosecha debe estar definida" y no se realiza
     * la operacion solicitada
     */
    if (harvestDate == null) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.UNDEFINED_HARVEST_DATE))).build();
    }

    /*
     * Si la fecha de siembra es mayor o igual a la fecha de
     * cosecha, la aplicacion del lado servidor retorna el
     * mensaje HTTP 400 (Bad request) junto con el mensaje
     * "La fecha de siembra no debe ser mayor ni igual a la
     * fecha de cosecha" y no se realiza la operacion solicitada
     */
    if (UtilDate.compareTo(seedDate, harvestDate) >= 0) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.OVERLAPPING_SEED_DATE_AND_HARVEST_DATE))).build();
    }

    /*
     * Si las fechas de un nuevo registro de plantacion de una
     * parcela estan superpuestas con las fechas de los demas
     * registros de plantacion de la misma parcela, la
     * aplicacion retorna el mensaje HTTP 400 (Bad request)
     * junto con el mensaje "Hay superposicion de fechas
     * entre este registro de plantacion y los demas registros
     * de plantacion de la misma parcela" y no se realiza la
     * operacion solicitada
     */
    if (plantingRecordService.checkDateOverlapOnCreation(newPlantingRecord)) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.OVERLAPPING_DATES))).build();
    }

    /*
     * Si la parcela del registro de plantacion a crear NO
     * esta definida, la aplicacion del lado servidor retorna
     * el mensaje HTTP 400 (Bad request) junto con el mensaje
     * "La parcela debe estar definida" y no se realiza la
     * operacion solicitada
     */
    if (newPlantingRecord.getParcel() == null) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.INDEFINITE_PARCEL))).build();
    }

    /*
     * Si el cultivo del registro de plantacion a crear NO
     * esta definido, la aplicacion del lado servidor retorna
     * el mensaje HTTP 400 (Bad request) junto con el mensaje
     * "El cultivo debe estar definido" y no se realiza la
     * operacion solicitada
     */
    if (newPlantingRecord.getCrop() == null) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.INDEFINITE_CROP))).build();
    }

    /*
     * El simbolo de esta variable se utiliza para representar que la
     * necesidad de agua de riego de un cultivo en la fecha actual [mm/dia]
     * no esta disponible, pero se puede calcular. Esta situacion
     * ocurre unicamente para un registro de plantacion en desarrollo.
     */
    String cropIrrigationWaterNeedNotAvailableButCalculable = plantingRecordService.getCropIrrigationWaterNotAvailableButCalculable();

    /*
     * El valor de esta constante se asigna a la necesidad de
     * agua de riego [mm/dia] de un registro de plantacion
     * para el que no se puede calcular dicha necesidad, lo
     * cual, ocurre cuando no se tiene la evapotranspiracion
     * del cultivo bajo condiciones estandar (ETc) [mm/dia]
     * ni la precipitacion [mm/dia], siendo ambos valores de
     * la fecha actual.
     * 
     * El valor de esta constante tambien se asigna a la
     * necesidad de agua de riego de un registro de plantacion
     * finalizado o en espera, ya que NO tiene ninguna utilidad
     * que un registro de plantacion en uno de estos estados
     * tenga un valor numerico mayor o igual a cero en la
     * necesidad de agua de riego.
     * 
     * La abreviatura "n/a" significa "no disponible".
     */
    String notAvailable = plantingRecordService.getNotAvailable();

    PlantingRecordStatus finishedStatus = statusService.findFinishedStatus();
    PlantingRecordStatus waitingStatus = statusService.findWaitingStatus();
    PlantingRecordStatus inDevelopmentStatus = statusService.findInDevelopmentStatus();
    PlantingRecordStatus optimalDevelopmentStatus = statusService.findOptimalDevelopmentStatus();

    /*
     * El estado de un registro de plantacion se calcula
     * en base a su fecha de siembra, su fecha de cosecha
     * y a la bandera suelo de las opciones de la parcela
     * a la que pertenece
     */
    PlantingRecordStatus statusNewPlantingRecord = statusService.calculateStatus(newPlantingRecord);
    newPlantingRecord.setStatus(statusNewPlantingRecord);

    /*
     * Si la parcela del registro de plantacion a crear tiene
     * un registro de plantacion que tiene un estado de desarrollo
     * (en desarrollo, desarrollo optimo, desarrollo en riesgo
     * de marchitez, desarrollo en marchitez), se comprueba si
     * el estado efectivo de dicho registro es "Finalizado". Si
     * lo es, se le asigna dicho estado y se le asignan los valores
     * "n/a" (no disponible), 0 y 0 a sus atributos "necesidad
     * de agua de riego de un cultivo", "lamina total de agua
     * disponible" (capacidad de almacenamiento de agua del
     * suelo) y "lamina de riego optima" (umbral de riego),
     * respectivamente.
     * 
     * No es posible calcular la necesidad de agua de riego
     * de un cultivo de un registro de plantacion finalizado,
     * ya que este representa la cosecha de un cultivo. Por
     * este motivo se asigna el valor "n/a" al atributo "necesidad
     * de agua de riego de un cultivo" de un registro de
     * plantacion finalizado.
     * 
     * Por otro lado, los valores de la capacidad de almacenamiento
     * de agua del suelo y del umbral de riego no son necesarios
     * para un registro de plantacion finalizado. Por este
     * motivo se les asigna el valor 0 en un registro de
     * plantacion finalizado.
     */
    if (plantingRecordService.checkOneInDevelopment(newPlantingRecord.getParcel().getId())) {
      PlantingRecord developingPlantingRecord = plantingRecordService.findInDevelopment(newPlantingRecord.getParcel().getId());
      PlantingRecordStatus status = statusService.calculateStatus(developingPlantingRecord);

      if (statusService.equals(status, finishedStatus)) {
        plantingRecordService.updateCropIrrigationWaterNeed(developingPlantingRecord.getId(), notAvailable);
        plantingRecordService.updateTotalAmountWaterAvailable(developingPlantingRecord.getId(), 0);
        plantingRecordService.updateOptimalIrrigationLayer(developingPlantingRecord.getId(), 0);
        plantingRecordService.setStatus(developingPlantingRecord.getId(), finishedStatus);
      }

    }

    /*
     * Un registro de plantacion nuevo tiene el estado "Finalizado"
     * cuando es del pasado (es decir, tanto su fecha de siembra
     * como su fecha de cosecha son estrictamente menores a la
     * fecha actual).
     * 
     * Un registro de plantacion nuevo tiene el estado "En espera"
     * cuando es del futuro (es decir, tanto su fecha de isembra
     * como su fecha de cosecha son estrictamente mayor a la fecha
     * actual).
     * 
     * Un registro de plantacion del pasado tiene el valor "n/a" (no
     * disponible) en su atributo de la necesidad de agua de riego
     * porque no se tienen los registros climaticos del pasado, con
     * los cuales se calcula la ETc (evapotranspiracion del cultivo
     * bajo condiciones estandar) de un cultivo y al no tener la ETc
     * no se puede calcular la necesidad de agua de riego de un
     * cultivo.
     * 
     * Un registro de plantacion del futuro tiene el valor "n/a" (no
     * disponible) en su atributo de la necesidad de agua de riego
     * porque no se tienen los registros climaticos del futuro, con
     * los cuales se calcula la ETc (evapotranspiracion del cultivo
     * bajo condiciones estandar) de un cultivo y al no tener la ETc
     * no se puede calcular la necesidad de agua de riego de un
     * cultivo.
     */
    if (statusService.equals(statusNewPlantingRecord, finishedStatus) || (statusService.equals(statusNewPlantingRecord, waitingStatus))) {
      newPlantingRecord.setCropIrrigationWaterNeed(notAvailable);
    }

    /*
     * El caracter "-" (guion) se utiliza para representar que la
     * necesidad de agua de riego de un cultivo en la fecha actual
     * (es decir, hoy) [mm/dia] NO esta disponible, pero se puede
     * calcular. Esta situacion ocurre unicamente para un registro
     * de plantacion que tiene el estado "En desarrollo" o el estado
     * "Desarrollo optimo". El que un registro de plantacion tenga
     * el estado "En desarrollo" o el estado "Desarrollo optimo"
     * depende de la fecha de siembra, la fecha de cosecha y la
     * bandera suelo de las opciones de la parcela a la que
     * pertenece. Si la fecha de siembra y la fecha de cosecha se
     * eligen de tal manera que la fecha actual (es decir, hoy)
     * esta dentro del periodo definido por ambas y la bandera
     * suelo esta activa, el registro adquiere el estado "En
     * desarrollo". En caso contrario, adquiere el estado "Desarrollo
     * optimo".
     */
    if (statusService.equals(statusNewPlantingRecord, inDevelopmentStatus)
        || statusService.equals(statusNewPlantingRecord, optimalDevelopmentStatus)) {
      newPlantingRecord.setCropIrrigationWaterNeed(cropIrrigationWaterNeedNotAvailableButCalculable);
    }

    Parcel parcel = newPlantingRecord.getParcel();
    Crop crop = newPlantingRecord.getCrop();

    /*
     * Si el estado del nuevo registro de plantacion es "Desarrollo
     * optimo" se calculan y asignan la lamina total de agua disponible
     * (dt) [mm] (capacidad de almacenamiento de agua de un suelo [mm])
     * y la lamina de riego optima (drop) [mm] (umbral de riego [mm])
     * al mismo.
     * 
     * El metodo calculateStatus() de la clase PlantingRecordStatusServiceBean
     * calcula el estado de un registro de plantacion con base en la
     * fecha de siembra, la fecha de cosecha y la bandera suelo de las
     * opciones de la parcela a la que pertenece un registro de plantacion.
     * Si la fecha de siembra y la fecha de cosecha se eligen de tal
     * manera que la fecha actual (es decir, hoy) esta dentro del periodo
     * definido por ambas y la bandera suelo esta activa, un registro
     * adquiere el estado "En desarrollo". En caso contrario, adquiere
     * el estado "Desarrollo optimo".
     * 
     * Por lo tanto, si un registro de plantacion tiene el estado
     * "Desarrollo optimo" significa que la bandera suelo de la parcela
     * a la que pertenece, esta activa. Por este motivo no es necesario
     * utilizar una condicion para verificar el valor de dicha bandera
     * a la hora de calcular la lamina total de agua disponible y la
     * lamina de riego optima.
     */
    if (statusService.equals(statusNewPlantingRecord, optimalDevelopmentStatus)) {
      newPlantingRecord.setTotalAmountWaterAvailable(WaterMath.calculateTotalAmountWaterAvailable(crop, parcel.getSoil()));
      newPlantingRecord.setOptimalIrrigationLayer(WaterMath.calculateOptimalIrrigationLayer(crop, parcel.getSoil()));
    }

    /*
     * Un registro de plantacion nuevo debe poder ser modificado
     */
    newPlantingRecord.setModifiable(true);

    /*
     * Se persiste el nuevo registro de plantacion
     */
    newPlantingRecord = plantingRecordService.create(newPlantingRecord);

    /*
     * Si el valor del encabezado de autorizacion de la peticion HTTP
     * dada, tiene un JWT valido y no se cumplen las condiciones de
     * los controles para la creacion de un registro de plantacion, la
     * aplicacion del lado servidor devuelve el mensaje HTTP 200 (Ok)
     * junto con los datos que el cliente solicito persistir
     */
    return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(newPlantingRecord)).build();
  }

  @PUT
  @Path("/{id}/{maintainDeadStatus}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response modify(@Context HttpHeaders request, @PathParam("id") int plantingRecordId,
      @PathParam("maintainDeadStatus") boolean maintainDeadStatus, String json) throws IOException {
    Response givenResponse = RequestManager.validateAuthHeader(request, secretKeyService.find());

    /*
     * Si el estado de la respuesta obtenida de validar el
     * encabezado de autorizacion de una peticion HTTP NO
     * es ACCEPTED, se devuelve el estado de error de la misma.
     * 
     * Que el estado de la respuesta obtenida de validar el
     * encabezado de autorizacion de una peticion HTTP sea
     * ACCEPTED, significa que la peticion es valida,
     * debido a que el encabezado de autorizacion de la misma
     * cumple las siguientes condiciones:
     * - Esta presente.
     * - No esta vacio.
     * - Cumple con la convencion de JWT.
     * - Contiene un JWT valido.
     */
    if (!RequestManager.isAccepted(givenResponse)) {
      return givenResponse;
    }

    /*
     * Obtiene el JWT del valor del encabezado de autorizacion
     * de una peticion HTTP
     */
    String jwt = AuthHeaderManager.getJwt(AuthHeaderManager.getAuthHeaderValue(request));

    /*
     * Valor de la clave secreta con la que la aplicacion firma
     * un JWT
     */
    String secretKeyValue = secretKeyService.find().getValue();

    /*
     * Obtiene el ID de usuario contenido en la carga util del
     * JWT del encabezado de autorizacion de una peticion HTTP
     */
    int userId = JwtManager.getUserId(jwt, secretKeyValue);

    /*
     * Si el usuario que solicita esta operacion NO tiene una
     * sesion activa, la aplicacion del lador servidor devuelve
     * el mensaje 401 (Unauthorized) junto con el mensaje "No
     * tiene una sesion activa" y no se realiza la operacion
     * solicitada
     */
    if (!sessionService.checkActiveSession(userId)) {
      return Response.status(Response.Status.UNAUTHORIZED).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.NO_ACTIVE_SESSION))).build();
    }

    /*
     * Si la fecha de emision del JWT de un usuario NO es igual
     * a la fecha de emision de la sesion activa del usuario,
     * la aplicacion del lado servidor retorna el mensaje HTTP
     * 400 (Bad request) junto con el mensaje "El JWT no
     * corresponde a una sesion abierta" y no se realiza la
     * operacion solicitada.
     * 
     * Se debe tener en cuenta que el metodo checkDateIssueLastSession
     * de la clase SessionServiceBean debe ser invocado luego
     * de invocar el metodo checkActiveSession de la misma
     * clase, ya que de lo contrario se puede comparar la
     * fecha de emision de un JWT con una sesion inactiva,
     * lo cual es incorrecto porque la fecha de emision de un
     * JWT se debe comparar con la fecha de emision de una
     * sesion activa. El motivo por el cual puede ocurrir esta
     * comparacion con una sesion inactiva es que el metodo
     * findLastSession recupera la ultima sesion del usuario,
     * independientemente de si esta activa o inactiva.
     * 
     * Este control se implementa para evitar que se puedan
     * recuperar datos mediante peticiones HTTP haciendo uso
     * de un JWT valido, pero que es de una sesion que fue
     * cerrada por el usuario.
     */
    if (!sessionService.checkDateIssueLastSession(userId, JwtManager.getDateIssue(jwt, secretKeyValue))) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.JWT_NOT_ASSOCIATED_WITH_ACTIVE_SESSION)))
          .build();
    }

    /*
     * Si el dato solicitado no existe en la base de datos
     * subyacente, la aplicacion del lado servidor devuelve
     * el mensaje HTTP 404 (Not found) junto con el mensaje
     * "Recurso no encontrado" y no se realiza la operacion
     * solicitada
     */
    if (!plantingRecordService.checkExistence(plantingRecordId)) {
      return Response.status(Response.Status.NOT_FOUND).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.RESOURCE_NOT_FOUND))).build();
    }

    /*
     * Si al usuario que hizo esta peticion HTTP, no le pertenece
     * el dato solicitado, la aplicacion del lado servidor
     * devuelve el mensaje HTTP 403 (Forbidden) junto con el
     * mensaje "Acceso no autorizado" (contenido en el enum
     * ReasonError) y no se realiza la operacion solicitada
     */
    if (!plantingRecordService.checkUserOwnership(userId, plantingRecordId)) {
      return Response.status(Response.Status.FORBIDDEN).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.UNAUTHORIZED_ACCESS))).build();
    }

    /*
     * Si el registro de plantacion a modificar NO es modificable,
     * la aplicacion del lado servidor retorna el mensaje
     * HTTP 400 (Bad request) junto con el mensaje "No esta
     * permitida la modificacion de un registro de plantacion no
     * modificable" y no se realiza la operacion solicitada
     */
    if (!plantingRecordService.isModifiable(plantingRecordId)) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.MODIFICATION_NON_MODIFIABLE_PLANTING_RECORD_NOT_ALLOWED)))
          .build();
    }

    /* 
     * Si el objeto correspondiente a la referencia contenida
     * en la variable de tipo por referencia de tipo String json,
     * esta vacio, significa que el formulario del dato correspondiente
     * a esta clase, esta vacio. Por lo tanto, la aplicacion del
     * lado servidor retorna el mensaje HTTP 400 (Bad request)
     * junto con el mensaje "Debe proporcionar todos los datos
     * requeridos" y no se realiza la operacion solicitada
     */
    if (json.isEmpty()) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.EMPTY_DATA))).build();
    }

    PlantingRecord modifiedPlantingRecord = mapper.readValue(json, PlantingRecord.class);
    PlantingRecord currentPlantingRecord = plantingRecordService.find(plantingRecordId);

    Parcel modifiedParcel = modifiedPlantingRecord.getParcel();
    Parcel currentParcel = currentPlantingRecord.getParcel();

    Crop modifiedCrop = modifiedPlantingRecord.getCrop();
    Crop currentCrop = currentPlantingRecord.getCrop();

    Calendar currentSeedDate = currentPlantingRecord.getSeedDate();
    Calendar currentHarvestDate = currentPlantingRecord.getHarvestDate();
    Calendar modifiedSeedDate = modifiedPlantingRecord.getSeedDate();
    Calendar modifiedHarvestDate = modifiedPlantingRecord.getHarvestDate();

    PlantingRecordStatus currentStatus = currentPlantingRecord.getStatus();
    PlantingRecordStatus finishedStatus = statusService.findFinishedStatus();
    PlantingRecordStatus waitingStatus = statusService.findWaitingStatus();
    PlantingRecordStatus deadStatus = statusService.findDeadStatus();
    PlantingRecordStatus inDevelopmentStatus = statusService.findInDevelopmentStatus();
    PlantingRecordStatus optimalDevelopmentStatus = statusService.findOptimalDevelopmentStatus();
    PlantingRecordStatus developmentAtRiskWiltingStatus = statusService.findDevelopmentAtRiskWiltingStatus();
    PlantingRecordStatus developmentInWiltingStatus = statusService.findDevelopmentInWiltingStatus();

    /*
     * Si el registro de plantacion modificado tiene el atributo
     * modifiable en false y si el registro de plantacion correspondiente
     * al registro de plantacion modificado tiene un estado
     * distinto al estado "Finalizado", la aplicacion del lado
     * servidor retorna el el mensaje HTTP 400 (Bad request) junto
     * con el mensaje "No esta permitido hacer que un registro
     * de plantacion que no tiene el estado finalizado sea no
     * modificable" y no se realiza la operacion solicitada
     */
    if (!modifiedPlantingRecord.getModifiable() && !(statusService.equals(currentStatus, finishedStatus))) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.MODIFIABILITY_PLANTING_RECORD_NOT_ALLOWED)))
          .build();
    }

    /*
     * Si la fecha de siembra de un registro de plantacion modificado
     * NO esta definida, la aplicacion del lado servidor retorna
     * el mensaje HTTP 400 (Bad request) junto con el mensaje
     * "La fecha de siembra debe estar definida" y no se realiza
     * la operacion solicitada
     */
    if (modifiedSeedDate == null) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.UNDEFINED_SEED_DATE))).build();
    }

    /*
     * Si la fecha de cosecha de un registro de plantacion modificado
     * NO esta definida, la aplicacion del lado servidor retorna
     * el mensaje HTTP 400 (Bad request) junto con el mensaje
     * "La fecha de cosecha debe estar definida" y no se realiza
     * la operacion solicitada
     */
    if (modifiedHarvestDate == null) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.UNDEFINED_HARVEST_DATE))).build();
    }

    /*
     * Si la fecha de siembra es mayor o igual a la fecha de
     * cosecha, la aplicacion del lado servidor retorna el
     * mensaje HTTP 400 (Bad request) junto con el mensaje
     * "La fecha de siembra no debe ser mayor ni igual a la
     * fecha de cosecha" y no se realiza la operacion solicitada
     */
    if (UtilDate.compareTo(modifiedSeedDate, modifiedHarvestDate) >= 0) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.OVERLAPPING_SEED_DATE_AND_HARVEST_DATE))).build();
    }

    /*
     * Si las fechas de un registro de plantacion modificado de
     * una parcela estan superpuestas con las fechas de los demas
     * registros de plantacion de la misma parcela, la
     * aplicacion retorna el mensaje HTTP 400 (Bad request)
     * junto con el mensaje "Hay superposicion de fechas
     * entre este registro de plantacion y los demas registros
     * de plantacion de la misma parcela" y no se realiza la
     * operacion solicitada
     */
    if (plantingRecordService.checkDateOverlapOnModification(modifiedPlantingRecord)) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.OVERLAPPING_DATES))).build();
    }

    /*
     * Si la parcela del registro de plantacion a modificar NO
     * esta definida, la aplicacion del lado servidor retorna
     * el mensaje HTTP 400 (Bad request) junto con el mensaje
     * "La parcela debe estar definida" y no se realiza la
     * operacion solicitada
     */
    if (modifiedPlantingRecord.getParcel() == null) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.INDEFINITE_PARCEL))).build();
    }

    /*
     * Si la parcela del registro de plantacion a modificar
     * es diferente a la parcela actual de dicho registro y
     * ya tiene un registro de plantacion en desarrollo, la
     * aplicacion retorna el mensaje HTTP 400 (Bad request)
     * junto con el mensaje "La parcela seleccionada ya tiene
     * un registro de plantacion en desarrollo" y no se realiza
     * la operacion solicitada
     */
    if (!(parcelService.equals(modifiedPlantingRecord.getParcel(), currentPlantingRecord.getParcel()))
        && (plantingRecordService.checkOneInDevelopment(modifiedPlantingRecord.getParcel().getId()))) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.MODIFICATION_WITH_PARCEL_HAS_PLANTING_RECORD_IN_DEVELOPMENT_NOT_ALLOWED)))
          .build();
    }

    /*
     * Si el cultivo del registro de plantacion a modificar NO
     * esta definido, la aplicacion del lado servidor retorna
     * el mensaje HTTP 400 (Bad request) junto con el mensaje
     * "El cultivo debe estar definido" y no se realiza la
     * operacion solicitada
     */
    if (modifiedPlantingRecord.getCrop() == null) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.INDEFINITE_CROP))).build();
    }

    /*
     * Si la necesidad de agua de riego del registro de plantacion
     * modificado (proveniente del cliente) es distinta a la
     * necesidad de agua de riego actual de dicho registro, la
     * aplicacion del lado servidor retorna el mensaje HTTP 400
     * (Bad request) junto con el mensaje "No esta permitida la
     * modificacion de la necesidad de agua de riego" y no se
     * realiza la operacion solicitada
     */
    if (!modifiedPlantingRecord.getCropIrrigationWaterNeed().equals(currentPlantingRecord.getCropIrrigationWaterNeed())) {
      return Response.status(Response.Status.BAD_REQUEST)
        .entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.MODIFICATION_IRRIGATION_WATER_NEED_NOT_ALLOWED))).build();
    }

    PlantingRecordStatus currentStatusModifiedPlantingRecord = modifiedPlantingRecord.getStatus();

    /*
     * El estado actual (*) del registro de plantacion a modificar
     * puede cambiar dependiendo de:
     * - si es "Muerto" y el usuario NO se desea que el registro
     * mantenga este estado.
     * - si NO es "Muerto" y si la fecha de siembra o la fecha
     * de cosecha del registro es modificada o si ambas son
     * modificadas o si su parcela es modificada o si su
     * cultivo es modificado.
     * 
     * (*) Por "actual" se hace referencia al dato existente
     * antes de la modificacion de un registro de plantacion.
     */
    PlantingRecordStatus newStatusPlantingRecord = currentStatusModifiedPlantingRecord;

    /*
     * Si un registro de plantacion a modificar tiene el estado
     * muerto y NO se desea que mantenga ese estado luego de su
     * modificacion, se establece su proximo estado. El estado
     * de un registro de plantacion se calcula con base en la
     * fecha de siembra, la fecha de cosecha y la bandera suelo
     * de las opciones de la parcela a la que pertenece.
     */
    if (statusService.equals(currentStatusModifiedPlantingRecord, deadStatus) && !maintainDeadStatus) {
      newStatusPlantingRecord = statusService.calculateStatus(modifiedPlantingRecord);
      plantingRecordService.unsetDeathDate(plantingRecordId);
    }

    /*
     * Si el registro de plantacion a modificar NO tiene el
     * estado "Muerto" y:
     * - su fecha de siembra o su fecha de cosecha es modificada
     * o si ambas son modificadas o
     * - su parcela es modificada o
     * - su cultivo es modificado,
     * 
     * se calcula y asigna su proximo estado. El estado de un
     * registro de plantacion se calcula con base en la fecha
     * de siembra, la fecha de cosecha y la bandera suelo de
     * las opciones de la parcela a la que pertenece.
     */
    if (!statusService.equals(currentStatusModifiedPlantingRecord, deadStatus)
        && (UtilDate.compareTo(modifiedSeedDate, currentSeedDate) != 0
            || UtilDate.compareTo(modifiedHarvestDate, currentHarvestDate) != 0
            || !parcelService.equals(modifiedParcel, currentParcel)
            || !cropService.equals(modifiedCrop, currentCrop))) {
      newStatusPlantingRecord = statusService.calculateStatus(modifiedPlantingRecord);
    }

    /*
     * Asignacion del posible nuevo estado del registro de
     * plantacion a modificar. En caso de que el estado no
     * sea nuevo, al registro de plantacion a modificar
     * se le asigna el estado que tenia antes de la
     * peticion de modificacion.
     */
    modifiedPlantingRecord.setStatus(newStatusPlantingRecord);

    /*
     * El simbolo de esta variable se utiliza para representar que la
     * necesidad de agua de riego de un cultivo en la fecha actual [mm/dia]
     * no esta disponible, pero se puede calcular. Esta situacion
     * ocurre unicamente para un registro de plantacion en desarrollo.
     */
    String cropIrrigationWaterNeedNotAvailableButCalculable = plantingRecordService.getCropIrrigationWaterNotAvailableButCalculable();

    /*
     * El valor de esta constante se asigna a la necesidad de
     * agua de riego [mm/dia] de un registro de plantacion
     * para el que no se puede calcular dicha necesidad, lo
     * cual, ocurre cuando no se tiene la evapotranspiracion
     * del cultivo bajo condiciones estandar (ETc) [mm/dia]
     * ni la precipitacion [mm/dia], siendo ambos valores de
     * la fecha actual.
     * 
     * El valor de esta constante tambien se asigna a la
     * necesidad de agua de riego de un registro de plantacion
     * finalizado o en espera, ya que NO tiene ninguna utilidad
     * que un registro de plantacion en uno de estos estados
     * tenga un valor numerico mayor o igual a cero en la
     * necesidad de agua de riego.
     * 
     * La abreviatura "n/a" significa "no disponible".
     */
    String notAvailable = plantingRecordService.getNotAvailable();

    /*
     * Si el registro de plantacion a modificar tiene como nuevo
     * estado al estado "Finalizado", se actualizan sus atributos
     * "necesidad de agua de riego de un cultivo", "lamina total
     * de agua disponible" y "lamina de riego optima". Si la
     * parcela modificada de dicho registro tiene registros de
     * plantacion que tienen el estado "En espera", se selecciona
     * el registro de plantacion en espera de dicha parcela mas
     * cercano a la fecha actual y se actualiza su estado, su
     * necesidad de agua de riego, su lamina total de agua
     * disponible y su lamina de riego optima. Esto tres ultimos
     * atributos son actualizados si el nuevo estado del registro
     * de plantacion en espera es "En desarrollo" o "En desarrollo
     * optimo".
     */
    if (statusService.equals(statusService.calculateStatus(modifiedPlantingRecord), finishedStatus)) {
      modifiedPlantingRecord.setStatus(finishedStatus);

      /*
       * La necesidad de agua de riego, la capacidad de almacenamiento
       * de agua del suelo y el umbral de riego de un registro de
       * plantacion que tiene el estado finalizado tienen los valores
       * "n/a", 0 y 0, respectivamente
       */
      modifiedPlantingRecord.setCropIrrigationWaterNeed(notAvailable);
      plantingRecordService.updateTotalAmountWaterAvailable(plantingRecordId, 0);
      plantingRecordService.updateOptimalIrrigationLayer(plantingRecordId, 0);

      /*
       * Si la parcela correspondiente al registro de plantacion
       * que paso de un estado de desarrollo al estado "Finalizado",
       * tiene registros de plantacion en el estado "En espera", se
       * obtiene de ellos el que tiene la fecha de siembra mas cercana
       * a la fecha actual
       */
      if (plantingRecordService.hasInWaitingPlantingRecords(userId, modifiedParcel.getId())) {
        int idFirstPlantingRecordInWaiting = plantingRecordService.findIdFirstOneOnWaiting(modifiedParcel.getId());
        PlantingRecord firstPlantingRecordInWaiting = plantingRecordService.find(idFirstPlantingRecordInWaiting);
        PlantingRecordStatus status = statusService.calculateStatus(firstPlantingRecordInWaiting);
        plantingRecordService.setStatus(idFirstPlantingRecordInWaiting, status);

        /*
         * El caracter "-" (guion) se utiliza para representar que la
         * necesidad de agua de riego de un cultivo en la fecha actual
         * (es decir, hoy) [mm/dia] NO esta disponible, pero se puede
         * calcular. Esta situacion ocurre unicamente para un registro
         * de plantacion que tiene el estado "En desarrollo" o el estado
         * "Desarrollo optimo". El que un registro de plantacion tenga
         * el estado "En desarrollo" o el estado "Desarrollo optimo"
         * depende de la fecha de siembra, la fecha de cosecha y la
         * bandera suelo de las opciones de la parcela a la que
         * pertenece. Si la fecha de siembra y la fecha de cosecha se
         * eligen de tal manera que la fecha actual (es decir, hoy)
         * esta dentro del periodo definido por ambas y la bandera
         * suelo esta activa, el registro adquiere el estado "En
         * desarrollo". En caso contrario, adquiere el estado
         * "Desarrollo optimo".
         */
        if (statusService.equals(status, inDevelopmentStatus) || statusService.equals(status, optimalDevelopmentStatus)) {
          plantingRecordService.updateCropIrrigationWaterNeed(idFirstPlantingRecordInWaiting, cropIrrigationWaterNeedNotAvailableButCalculable);
        }

        /*
         * Si la parcela del registro de plantacion obtenido tiene
         * la opcion suelo activa en sus opciones y si el registro
         * de plantacion tiene el estado "Desarrollo optimo" se
         * actualizan sus atributos "lamina total de agua disponible"
         * (capacidad de almacenamiento de agua del suelo) [mm] y
         * "lamina de riego optima" (umbral de riego) [mm]
         */
        if (statusService.equals(status, optimalDevelopmentStatus) && firstPlantingRecordInWaiting.getParcel().getOption().getSoilFlag()) {
          plantingRecordService.updateTotalAmountWaterAvailable(idFirstPlantingRecordInWaiting, WaterMath
              .calculateTotalAmountWaterAvailable(firstPlantingRecordInWaiting.getCrop(), firstPlantingRecordInWaiting.getParcel().getSoil()));
          plantingRecordService.updateOptimalIrrigationLayer(idFirstPlantingRecordInWaiting, WaterMath
              .calculateOptimalIrrigationLayer(firstPlantingRecordInWaiting.getCrop(), firstPlantingRecordInWaiting.getParcel().getSoil()));
        }

      } // End if

      return Response.status(Response.Status.OK)
          .entity(mapper.writeValueAsString(plantingRecordService.modify(userId, plantingRecordId, modifiedPlantingRecord)))
          .build();
    } // End if

    /*
     * Un registro de plantacion tiene el estado "Finalizado" cuando
     * es del pasado, esto es que tanto su fecha de siembra como su
     * fecha de cosecha son estrictamente menores a la fecha actual
     * (hoy).
     * 
     * Un registro de plantacion tiene el estado "En espera" cuando
     * es del futuro, esto es que tanto su fecha de siembra como su
     * fecha de cosecha son estrictamente mayor a la fecha actual
     * (hoy).
     * 
     * Un registro de plantacion del pasado tiene el valor "n/a" (no
     * disponible) en su atributo de la necesidad de agua de riego
     * porque no se tienen los registros climaticos del pasado, con
     * los cuales se calcula la ETc (evapotranspiracion del cultivo
     * bajo condiciones estandar) de un cultivo y al no tener la ETc
     * no se puede calcular la necesidad de agua de riego de un cultivo.
     * 
     * Un registro de plantacion del futuro tiene el valor "n/a" (no
     * disponible) en su atributo de la necesidad de agua de riego
     * porque no se tienen los registros climaticos del futuro, con
     * los cuales se calcula la ETc (evapotranspiracion del cultivo
     * bajo condiciones estandar) de un cultivo y al no tener la ETc
     * no se puede calcular la necesidad de agua de riego de un cultivo.
     * 
     * Se asigna el valor 0 a la lamina total de agua disponible (dt)
     * [mm] y a la lamina de riego optima (drop) [mm] de un registro
     * de plantacion modificado que tiene el estado finalizado o el
     * estado en espera, ya que en uno de estos estados NO tiene
     * ninguna utilidad tener tales datos.
     */
    if (statusService.equals(newStatusPlantingRecord, finishedStatus) || statusService.equals(newStatusPlantingRecord, waitingStatus)) {
      modifiedPlantingRecord.setCropIrrigationWaterNeed(notAvailable);
      plantingRecordService.updateTotalAmountWaterAvailable(plantingRecordId, 0);
      plantingRecordService.updateOptimalIrrigationLayer(plantingRecordId, 0);

      return Response.status(Response.Status.OK)
          .entity(mapper.writeValueAsString(plantingRecordService.modify(userId, plantingRecordId, modifiedPlantingRecord)))
          .build();
    }

    /*
     * Si el nuevo estado del registro de plantacion a modificar es
     * "En desarrollo" o "Desarrollo optimo" se realizan las siguientes
     * evaluaciones:
     * 1. si el unico dato modificado del registro es su fecha de siembra.
     * 2. si la parcela o el cultivo del registro es modificado. Si el
     * estado actual (*) del registro es "Finalizado", "En espera" o
     * "Muerto".
     * 
     * 1. Si el unico dato modificado del registro de plantacion a
     * modificar es su fecha de siembra, se asigna el caracter "-" (guion)
     * al atributo "necesidad de agua de riego de un cultivo" (**) del
     * mismo.
     * 
     * 2. Si se modifica la parcela y/o el cultivo del registro de
     * plantacion a modificar, se asigna el caracter "-" (guion) al
     * atributo "necesidad de agua de riego de un cultivo" (**) del
     * mismo. Si el estado actual (*) del registro de plantacion es
     * "Finalizado", "En espera" o "Muerto" se realiza la misma
     * asignacion.
     * 
     * El caracter "-" (guion) se utiliza para representar que la
     * necesidad de agua de riego de un cultivo en la fecha actual
     * (es decir, hoy) [mm/dia] NO esta disponible, pero se puede
     * calcular. Esta situacion ocurre unicamente para un registro
     * de plantacion que tiene el estado "En desarrollo" o el estado
     * "Desarrollo optimo". El que un registro de plantacion tenga
     * el estado "En desarrollo" o el estado "Desarrollo optimo"
     * depende de la fecha de siembra, la fecha de cosecha y la
     * bandera suelo de las opciones de la parcela a la que
     * pertenece. Si la fecha de siembra y la fecha de cosecha se
     * eligen de tal manera que la fecha actual (es decir, hoy)
     * esta dentro del periodo definido por ambas y la bandera
     * suelo esta activa, el registro adquiere el estado "En
     * desarrollo". En caso contrario, adquiere el estado "Desarrollo
     * optimo".
     * 
     * (*) Por "actual" se hace referencia al estado existente antes de
     * la modificacion de un registro de plantacion.
     * 
     * (**) El atributo "necesidad de agua de riego de un cultivo" es
     * la necesidad de agua de riego de un cultivo en la fecha actual
     * (es decir, hoy) [mm/dia].
     */
    if (statusService.equals(newStatusPlantingRecord, inDevelopmentStatus) || statusService.equals(newStatusPlantingRecord, optimalDevelopmentStatus)) {

      /*
       * La necesidad de agua de riego de un cultivo en la fecha
       * actual (es decir, hoy) [mm/dia] se calcula con base en el
       * acumulado del deficit de humedad por dia [mm/dia] del dia
       * inmediatamente anterior a la fecha actual. Este valor
       * acumulado es el resultado de acumular el deficit de humedad
       * por dia [mm/dia] de cada uno de los balances hidricos de
       * suelo calculados desde la fecha de siembra de un cultivo
       * hasta la fecha inmediatamente anterior a la fecha actual.
       * El deficit de humedad por dia [mm/dia] es calculado como
       * la diferencia entre la precipitacion [mm/dia] y la ETc
       * (evapotranspiracion del cultivo bajo condiciones estandar)
       * [mm/dia] o la ETo (evapotranspiracion del cultivo de
       * referencia) [mm/dia] si la ETc = 0. La ETc se calcula
       * con base en la ETo y un Kc (coeficiente de cultivo) de
       * un cultivo. La ETo se calcula con base en los datos
       * meteorologicos de una fecha y una ubicacion geografica.
       * Por lo tanto, la ETc es calculada con base en datos
       * meteorologicos de una ubicacion geografica y una fecha.
       * La precipitacion se obtiene de los datos meteorologicos
       * de una ubicacion geografica y una fecha.
       * 
       * Con lo anterior en mente se realizan las siguientes
       * justificaciones.
       * 
       * Cuando se modifica la fecha de siembra de un registro de
       * plantacion, se asigna el carater "-" (guion) al atributo
       * "necesidad de agua de riego de un cultivo" (**) del registro
       * porque al cambiar la fecha de siembra cambia la fecha a partir
       * de la cual se calculan los balances hidricos de suelo. Por
       * lo tanto, cambia el acumulado del deficit de humedad por dia
       * [mm/dia] del dia inmediatamente anterior a la fecha actual.
       * Por ende, cambia la necesidad de agua de riego de un cultivo
       * en la fecha actual (es decir, hoy) [mm/dia].
       * 
       * Cuando se modifica la parcela de un registro de plantacion,
       * se asigna el caracter "-" (guion) al atributo "necesidad de
       * agua de riego de un cultivo" (**) del registro porque al
       * cambiar la parcela cambia la cambia la ubicacion geografica,
       * y al cambiar la ubicacion geografica cambian los datos meteorologicos
       * a partir de los cuales se obtiene la precipitacion [mm/dia]
       * y se calcula la ETo [mm/dia], con lo cual cambian la prepcipitacion
       * y la ETo, y al cambiar la ETo cambia la ETc [mm/dia]. Por
       * lo tanto, cambia el deficit de humedad por dia [mm/dia] de cada
       * uno de los balances hidricos de suelo calculados desde la
       * fecha de siembra de un cultivo hasta la fecha inmediatamente
       * anterior a la fecha actual. En consecuencia, cambia el
       * acumulado del deficit de humedad por dia [mm/dia] de la fecha
       * inmediatamente anterior a la fecha actual. Por ende, cambia
       * la necesidad de agua de riego de un cultivo en la fecha actual
       * (es decir, hoy) [mm/dia].
       * 
       * Cuando se modifica el cultivo de un registro de plantacion,
       * se asigna el caracter "-" (guion) al atributo "necesidad de
       * agua de riego de un cultivo" (**) del registro porque al cambiar
       * el cultivo cambia el Kc (coeficiente de cultivo) y al cambiar
       * el Kc cambia la ETc (evapotranspiracion del cultivo bajo condiciones
       * estandar) [mm/dia]. Por lo tanto, cambia el deficit de humedad
       * por dia [mm/dia] de cada uno de los balances hidricos de suelo
       * calculados desde la fecha de siembra de un cultivo hasta la
       * fecha inmediatamente anterior a la fecha actual. En consecuencia,
       * cambia el acumulado del deficit de humedad por dia [mm/dia] de
       * la fecha inmediatamente anterior a la fecha actual. Por ende,
       * cambia la necesidad de agua de riego de un cultivo en la fecha
       * actual (es decir, hoy) [mm/dia].
       * 
       * Si el estado actual (*) del registro de plantacion a modificar
       * es "Finalizado", "En espera" o "Muerto" y su nuevo estado es
       * "Desarrollo optimo", se asigna el caracter "-" (guion) al
       * atributo "necesidad de agua de riego de un cultivo" (**) del
       * registro para representar que la necesidad de agua de riego
       * de un cultivo en la fecha actual (es decir, hoy) [mm/dia] NO
       * esta disponible, pero se puede calcular. Esto se realiza porque
       * en los estados "Finalizado", "En espera" y "Muerto" la necesidad
       * de agua de riego de un cultivo en la fecha actual (es decir,
       * hoy) no esta disponible ni se puede calcular, lo cual se
       * representa mediante la asignacion de la abreviatura "n/a"
       * (no disponible) al atributo "necesidad de agua de riego de
       * un cultivo" de un registro de plantacion.
       * 
       * El caracter "-" (guion) se utiliza para representar que la
       * necesidad de agua de riego de un cultivo en la fecha actual
       * (es decir, hoy) [mm/dia] NO esta disponible, pero se puede
       * calcular. Esta situacion ocurre unicamente para un registro
       * de plantacion que tiene el estado "En desarrollo" o el estado
       * "Desarrollo optimo". El que un registro de plantacion tenga
       * el estado "En desarrollo" o el estado "Desarrollo optimo"
       * depende de la fecha de siembra, la fecha de cosecha y la
       * bandera suelo de las opciones de la parcela a la que
       * pertenece. Si la fecha de siembra y la fecha de cosecha se
       * eligen de tal manera que la fecha actual (es decir, hoy)
       * esta dentro del periodo definido por ambas y la bandera
       * suelo esta activa, el registro adquiere el estado "En
       * desarrollo". En caso contrario, adquiere el estado "Desarrollo
       * optimo".
       * 
       * (*) Por "actual" se hace referencia al dato existente antes
       * de la modificacion de un registro de plantacion.
       * 
       * (**) El atributo "necesidad de agua de riego de un cultivo"
       * de un registro de plantacion es la necesidad de agua de riego
       * de un cultivo en la fecha actual (es decir, hoy) [mm/dia].
       */
      if (UtilDate.compareTo(modifiedSeedDate, currentSeedDate) != 0
          && UtilDate.compareTo(modifiedHarvestDate, currentHarvestDate) == 0
          && parcelService.equals(modifiedParcel, currentParcel)
          && cropService.equals(modifiedCrop, currentCrop)) {
        modifiedPlantingRecord.setCropIrrigationWaterNeed(cropIrrigationWaterNeedNotAvailableButCalculable);
      }

      if (!parcelService.equals(modifiedParcel, currentParcel)
          || !cropService.equals(modifiedCrop, currentCrop)
          || statusService.equals(currentStatusModifiedPlantingRecord, finishedStatus)
          || statusService.equals(currentStatusModifiedPlantingRecord, waitingStatus)
          || statusService.equals(currentStatusModifiedPlantingRecord, deadStatus)) {
        modifiedPlantingRecord.setCropIrrigationWaterNeed(cropIrrigationWaterNeedNotAvailableButCalculable);
      }

    } // End if

    /*
     * Lo que influye a la hora de calcular la necesidad de agua de
     * riego de un cultivo en la fecha actual (es decir, hoy) [mm/dia],
     * y, por ende, a la hora de asignar como valor inicial el caracter
     * "-" (guion) al atributo "necesidad de agua de riego" (**) de un
     * registro de plantacion a modificar que tiene como nuevo (proximo)
     * estado el estado "En desarrollo" o el estado "Desarrollo optimo",
     * es:
     * - si su estado actual (*) es "Finalizado", "En espera" o "Muerto",
     * - o la modificacion de la parcela
     * - o la modifcacion del cultivo
     * - o la modificacion unicamente de la fecha de siembra.
     * 
     * Por este motivo, cuando se modifica unicamente la fecha de
     * cosecha de un registro de plantacion a modificar que como
     * resultado de calcular su nuevo estado tiene el estado "Desarrollo
     * optimo" y como estado actual (*) tiene el estado "Desarrollo
     * en riesgo de marchitez" o el estado "Desarrollo en marchitez",
     * no se asigna el caracter "-" (guion) al atributo "necesidad
     * de agua de riego" (**) del registro, sino que se mantiene
     * su valor actual. Ademas de esto, se mantiene el estado actual
     * del registro de plantacion a modificar para lograr la
     * consistencia del mismo, ya que no seria correcto asignarle
     * el estado "Desarrollo optimo" manteniendo el valor actual
     * de su atributo "necesidad de agua de riego de un cultivo".
     * 
     * El caracter "-" (guion) se utiliza para representar que la
     * necesidad de agua de riego de un cultivo en la fecha actual
     * (es decir, hoy) [mm/dia] NO esta disponible, pero se puede
     * calcular. Esta situacion ocurre unicamente para un registro
     * de plantacion que tiene el estado "En desarrollo" o el estado
     * "Desarrollo optimo". El que un registro de plantacion tenga
     * el estado "En desarrollo" o el estado "Desarrollo optimo"
     * depende de la fecha de siembra, la fecha de cosecha y la
     * bandera suelo de las opciones de la parcela a la que
     * pertenece. Si la fecha de siembra y la fecha de cosecha se
     * eligen de tal manera que la fecha actual (es decir, hoy)
     * esta dentro del periodo definido por ambas y la bandera
     * suelo esta activa, el registro adquiere el estado "En
     * desarrollo". En caso contrario, adquiere el estado "Desarrollo
     * optimo".
     * 
     * (*) Por "actual" se hace referencia al dato existente antes
     * de la modificacion de un registro de plantacion.
     * 
     * (**) El atributo "necesidad de agua de riego de un cultivo"
     * de un registro de plantacion es la necesidad de agua de riego
     * de un cultivo en la fecha actual (es decir, hoy) [mm/dia].
     */
    if (statusService.equals(newStatusPlantingRecord, optimalDevelopmentStatus)) {

      if (UtilDate.compareTo(modifiedSeedDate, currentSeedDate) == 0
          && UtilDate.compareTo(modifiedHarvestDate, currentHarvestDate) != 0
          && parcelService.equals(modifiedParcel, currentParcel)
          && cropService.equals(modifiedCrop, currentCrop)
          && (statusService.equals(currentStatusModifiedPlantingRecord, developmentAtRiskWiltingStatus)
              || statusService.equals(currentStatusModifiedPlantingRecord, developmentInWiltingStatus))) {
        newStatusPlantingRecord = currentStatusModifiedPlantingRecord;
        modifiedPlantingRecord.setStatus(newStatusPlantingRecord);
      }

    }

    /*
     * Si el nuevo estado del registro de plantacion a modificar es
     * "En desarrollo", se asigna el valor 0 a la lamina total de agua
     * disponible (dt) [mm] (capacidad de almacenamiento de agua de un
     * suelo [mm]) y a la lamina de riego optima (drop) [mm] (umbral
     * de riego [mm]) del mismo.
     * 
     * La lamina total de agua disponible y la lamina de riego optima
     * estan en funcion del suelo y del cultivo sembrado. El estado
     * "En desarrollo" se utiliza cuando se calcula la necesidad de
     * agua de riego de un cultivo en la fecha actual (es decir, hoy)
     * [mm/dia] sin hacer uso de datos de suelo. Por lo tanto, al no
     * utilizarse datos de suelo NO es posible calcular ambas laminas.
     * Por este motivo se les asigna el valor 0 en el registro de
     * plantacion a modificar.
     */
    if (statusService.equals(newStatusPlantingRecord, inDevelopmentStatus)) {
      plantingRecordService.updateTotalAmountWaterAvailable(plantingRecordId, 0);
      plantingRecordService.updateOptimalIrrigationLayer(plantingRecordId, 0);
    }

    /*
     * Si el nuevo estado del registro de plantacion a modificar es
     * "Desarrollo optimo", se calculan la lamina total de agua
     * disponible (dt) [mm] (capacidad de almacenamiento de agua de
     * un suelo [mm]) y la lamina de riego optima (drop) [mm] (umbral
     * de riego [mm]) del mismo.
     * 
     * La lamina total de agua disponible y la lamina de riego optima
     * estan en funcion del suelo y del cultivo sembrado. El estado
     * "Desarrollo optimo", junto con los estados "Desarrollo en riesgo
     * de marchitez" y "Desarrollo en marchitez", se utiliza cuando se
     * calcula la necesidad de agua de riego de un cultivo en la fecha
     * actual (es decir, hoy) [mm] con datos de suelo. Por lo tanto,
     * al utilizar datos de suelo se deben calcular ambas laminas.
     * Por este motivo se calculan y asignan la lamina total de agua
     * disponible y la lamina de riego optima en el registro de
     * plantacion a modificar.
     * 
     * El metodo calculateStatus() de la clase PlantingRecordStatusServiceBean
     * calcula el estado de un registro de plantacion con base en la
     * fecha de siembra, la fecha de cosecha y la bandera suelo de las
     * opciones de la parcela a la que pertenece un registro de plantacion.
     * Si la fecha de siembra y la fecha de cosecha se eligen de tal
     * manera que la fecha actual (es decir, hoy) esta dentro del periodo
     * definido por ambas y la bandera suelo esta activa, un registro
     * adquiere el estado "En desarrollo". En caso contrario, adquiere
     * el estado "Desarrollo optimo".
     * 
     * Por lo tanto, si un registro de plantacion tiene el estado
     * "Desarrollo optimo" significa que la bandera suelo de la parcela
     * a la que pertenece, esta activa. Por este motivo no es necesario
     * utilizar una condicion para verificar el valor de dicha bandera
     * a la hora de calcular la lamina total de agua disponible y
     * la lamina de riego optima.
     */
    if (statusService.equals(newStatusPlantingRecord, optimalDevelopmentStatus)) {
      plantingRecordService.updateTotalAmountWaterAvailable(plantingRecordId, WaterMath.calculateTotalAmountWaterAvailable(modifiedCrop, modifiedParcel.getSoil()));
      plantingRecordService.updateOptimalIrrigationLayer(plantingRecordId, WaterMath.calculateOptimalIrrigationLayer(modifiedCrop, modifiedParcel.getSoil()));
    }

    /*
     * Si se modifica el cultivo del registro de plantacion y hay registros
     * de riego con un cultivo asignado en el periodo definido por la fecha
     * de siembra y la fecha de cosecha del registro de plantacion, se
     * modifica el cultivo de todos los registros de riego pertenecientes
     * a dicho periodo
     */
    if (!cropService.equals(modifiedCrop, currentCrop) && !irrigationRecordService
        .findAllWithCropBetweenDates(userId, modifiedParcel.getId(), modifiedSeedDate, modifiedHarvestDate).isEmpty()) {
      irrigationRecordService.modifyCropInPeriod(modifiedParcel.getId(), modifiedCrop, modifiedSeedDate, modifiedHarvestDate);
    }

    /*
     * Si el registro de plantacion modificado tiene el estado "Desarrollo
     * optimo", "Desarrollo en riesgo de marchitez", "Desarrollo en marchitez"
     * o "Muerto" y si se le modifica la fecha de siembra y/o el cultivo,
     * NO se debe generar el grafico de la evolucion diaria del nivel de
     * humedad del suelo para este registro cuando se presione el boton
     * de visualizacion sobre el. La manera en la que se logra esto es
     * asignando el valor false a la variable bandera
     * flagNotGenerateSoilMoistureGraph de un registro de plantacion.
     * 
     * Un registro de plantacion utiliza uno de los estados mencionados
     * cuando la parcela a la que pertenece tiene la bandera suelo
     * activa en sus opciones.
     */
    if ((UtilDate.compareTo(modifiedSeedDate, currentSeedDate) != 0 || !cropService.equals(modifiedCrop, currentCrop))
        && (statusService.equals(newStatusPlantingRecord, optimalDevelopmentStatus)
            || statusService.equals(newStatusPlantingRecord, developmentAtRiskWiltingStatus)
            || statusService.equals(newStatusPlantingRecord, developmentInWiltingStatus)
            || statusService.equals(newStatusPlantingRecord, deadStatus))) {
      plantingRecordService.setFlagNotGenerateSoilMoistureGraph(plantingRecordId);
    }

    /*
     * Si la fecha de siembra y el cultivo del registro de plantacion
     * a modificar no se modifican, y si el estado actual de dicho
     * registro es el estado "Muerto" y si su nuevo estado es "Desarrollo
     * optimo", "Desarrollo en riesgo de marchitez" o "Desarrollo en
     * marchitez", NO se debe generar el grafico de la evolucion diaria del
     * nivel de humedad del suelo para este registro cuando se presione
     * el boton de visualizacion sobre el. La manera en la que se logra
     * esto es asignando el valor false a la variable bandera
     * flagNotGenerateSoilMoistureGraph de un registro de plantacion.
     * 
     * Un registro de plantacion utiliza uno de los estados mencionados
     * cuando la parcela a la que pertenece tiene la bandera suelo
     * activa en sus opciones.
     * 
     * Este control es para evitar que el grafico de la evolucion diaria
     * del nivel de humedad del suelo sea visualizado cuando el usuario
     * decide NO mantener el estado "Muerto" de un registro de plantacion
     * sin modificar ni su fecha de siembra ni su cultivo.
     */
    if (UtilDate.compareTo(modifiedSeedDate, currentSeedDate) == 0 && cropService.equals(modifiedCrop, currentCrop)
        && statusService.equals(currentStatusModifiedPlantingRecord, deadStatus)
        && (statusService.equals(newStatusPlantingRecord, optimalDevelopmentStatus)
            || statusService.equals(newStatusPlantingRecord, developmentAtRiskWiltingStatus)
            || statusService.equals(newStatusPlantingRecord, developmentInWiltingStatus))) {
      plantingRecordService.setFlagNotGenerateSoilMoistureGraph(plantingRecordId);
    }

    /*
     * Se persisten los cambios realizados en el registro
     * de plantacion
     */
    modifiedPlantingRecord = plantingRecordService.modify(userId, plantingRecordId, modifiedPlantingRecord);

    /*
     * Si el valor del encabezado de autorizacion de la peticion HTTP
     * dada, tiene un JWT valido, la aplicacion del lado servidor
     * devuelve el mensaje HTTP 200 (Ok) junto con los datos que el
     * cliente solicito actualizar
     */
    return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(modifiedPlantingRecord)).build();
  }

  @DELETE
  @Path("/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response remove(@Context HttpHeaders request, @PathParam("id") int plantingRecordId) throws IOException {
    Response givenResponse = RequestManager.validateAuthHeader(request, secretKeyService.find());

    /*
     * Si el estado de la respuesta obtenida de validar el
     * encabezado de autorizacion de una peticion HTTP NO
     * es ACCEPTED, se devuelve el estado de error de la misma.
     * 
     * Que el estado de la respuesta obtenida de validar el
     * encabezado de autorizacion de una peticion HTTP sea
     * ACCEPTED, significa que la peticion es valida,
     * debido a que el encabezado de autorizacion de la misma
     * cumple las siguientes condiciones:
     * - Esta presente.
     * - No esta vacio.
     * - Cumple con la convencion de JWT.
     * - Contiene un JWT valido.
     */
    if (!RequestManager.isAccepted(givenResponse)) {
      return givenResponse;
    }

    /*
     * Obtiene el JWT del valor del encabezado de autorizacion
     * de una peticion HTTP
     */
    String jwt = AuthHeaderManager.getJwt(AuthHeaderManager.getAuthHeaderValue(request));

    /*
     * Valor de la clave secreta con la que la aplicacion firma
     * un JWT
     */
    String secretKeyValue = secretKeyService.find().getValue();

    /*
     * Obtiene el ID de usuario contenido en la carga util del
     * JWT del encabezado de autorizacion de una peticion HTTP
     */
    int userId = JwtManager.getUserId(jwt, secretKeyValue);

    /*
     * Si el usuario que solicita esta operacion NO tiene una
     * sesion activa, la aplicacion del lador servidor devuelve
     * el mensaje 401 (Unauthorized) junto con el mensaje "No
     * tiene una sesion activa" y no se realiza la operacion
     * solicitada
     */
    if (!sessionService.checkActiveSession(userId)) {
      return Response.status(Response.Status.UNAUTHORIZED).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.NO_ACTIVE_SESSION))).build();
    }

    /*
     * Si la fecha de emision del JWT de un usuario NO es igual
     * a la fecha de emision de la sesion activa del usuario,
     * la aplicacion del lado servidor retorna el mensaje HTTP
     * 400 (Bad request) junto con el mensaje "El JWT no
     * corresponde a una sesion abierta" y no se realiza la
     * operacion solicitada.
     * 
     * Se debe tener en cuenta que el metodo checkDateIssueLastSession
     * de la clase SessionServiceBean debe ser invocado luego
     * de invocar el metodo checkActiveSession de la misma
     * clase, ya que de lo contrario se puede comparar la
     * fecha de emision de un JWT con una sesion inactiva,
     * lo cual es incorrecto porque la fecha de emision de un
     * JWT se debe comparar con la fecha de emision de una
     * sesion activa. El motivo por el cual puede ocurrir esta
     * comparacion con una sesion inactiva es que el metodo
     * findLastSession recupera la ultima sesion del usuario,
     * independientemente de si esta activa o inactiva.
     * 
     * Este control se implementa para evitar que se puedan
     * recuperar datos mediante peticiones HTTP haciendo uso
     * de un JWT valido, pero que es de una sesion que fue
     * cerrada por el usuario.
     */
    if (!sessionService.checkDateIssueLastSession(userId, JwtManager.getDateIssue(jwt, secretKeyValue))) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.JWT_NOT_ASSOCIATED_WITH_ACTIVE_SESSION)))
          .build();
    }

    /*
     * Si el dato solicitado no existe en la base de datos
     * subyacente, la aplicacion del lado servidor devuelve
     * el mensaje HTTP 404 (Not found) junto con el mensaje
     * "Recurso no encontrado" y no se realiza la operacion
     * solicitada
     */
    if (!plantingRecordService.checkExistence(plantingRecordId)) {
      return Response.status(Response.Status.NOT_FOUND).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.RESOURCE_NOT_FOUND))).build();
    }

    /*
     * Si al usuario que hizo esta peticion HTTP, no le pertenece
     * el dato solicitado, la aplicacion del lado servidor
     * devuelve el mensaje HTTP 403 (Forbidden) junto con el
     * mensaje "Acceso no autorizado" (contenido en el enum
     * ReasonError) y no se realiza la operacion solicitada
     */
    if (!plantingRecordService.checkUserOwnership(userId, plantingRecordId)) {
      return Response.status(Response.Status.FORBIDDEN).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.UNAUTHORIZED_ACCESS))).build();
    }

    PlantingRecord plantingRecord = plantingRecordService.find(plantingRecordId);
    Calendar seedDate = plantingRecord.getSeedDate();
    Calendar harvestDate = plantingRecord.getHarvestDate();

    int parcelId = plantingRecord.getParcel().getId();

    /*
     * Si la parcela del registro de plantacion a eliminar tiene
     * registros de riego que tienen una fecha que esta dentro
     * del periodo definido por la fecha de siembra y la fecha
     * de cosecha del registro de plantacion, se los elimina
     * antes de eliminar el registro de plantacion
     */
    if (!irrigationRecordService.findAllWithCropBetweenDates(userId, parcelId, seedDate, harvestDate).isEmpty()) {
      irrigationRecordService.deleteBetweenDates(userId, parcelId, seedDate, harvestDate);
    }

    /*
     * Si el valor del encabezado de autorizacion de la peticion HTTP
     * dada, tiene un JWT valido, la aplicacion del lado servidor
     * devuelve el mensaje HTTP 200 (Ok) junto con los datos que el
     * cliente solicito eliminar
     */
    return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(plantingRecordService.remove(userId, plantingRecordId))).build();
  }

  /**
   * Calcula la necesidad de agua de riego de un cultivo plantado
   * en una parcela y en desarrollo en la fecha actual. Un registro
   * de plantacion en desarrollo representa la existencia de un
   * cultivo plantado en una parcela, el cual esta en desarrollo en
   * la fecha actual.
   * 
   * @param request
   * @param plantingRecordId
   * @return referencia un objeto de tipo Response que contiene la
   * fecha actual, la necesidad de agua de riego calculada para un
   * cultivo en desarrollo en la fecha actual, la parcela en la que
   * esta plantado el cultivo para el que se calcula la necesidad de
   * agua de riego y el cultivo que esta en desarrollo en la fecha
   * actual. En caso contrario, referencia a un objeto de tipo Response
   * que contiene un mensaje de error si hay un error.
   * @throws IOException
   */
  @GET
  @Path("/calculateCropIrrigationWaterNeed/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response calculateCropIrrigationWaterNeed(@Context HttpHeaders request, @PathParam("id") int plantingRecordId) throws IOException {
    Response givenResponse = RequestManager.validateAuthHeader(request, secretKeyService.find());

    /*
     * Si el estado de la respuesta obtenida de validar el
     * encabezado de autorizacion de una peticion HTTP NO
     * es ACCEPTED, se devuelve el estado de error de la misma.
     * 
     * Que el estado de la respuesta obtenida de validar el
     * encabezado de autorizacion de una peticion HTTP sea
     * ACCEPTED, significa que la peticion es valida,
     * debido a que el encabezado de autorizacion de la misma
     * cumple las siguientes condiciones:
     * - Esta presente.
     * - No esta vacio.
     * - Cumple con la convencion de JWT.
     * - Contiene un JWT valido.
     */
    if (!RequestManager.isAccepted(givenResponse)) {
      return givenResponse;
    }

    /*
     * Obtiene el JWT del valor del encabezado de autorizacion
     * de una peticion HTTP
     */
    String jwt = AuthHeaderManager.getJwt(AuthHeaderManager.getAuthHeaderValue(request));

    /*
     * Valor de la clave secreta con la que la aplicacion firma
     * un JWT
     */
    String secretKeyValue = secretKeyService.find().getValue();

    /*
     * Obtiene el ID de usuario contenido en la carga util del
     * JWT del encabezado de autorizacion de una peticion HTTP
     */
    int userId = JwtManager.getUserId(jwt, secretKeyValue);

    /*
     * Si el usuario que solicita esta operacion NO tiene una
     * sesion activa, la aplicacion del lador servidor devuelve
     * el mensaje 401 (Unauthorized) junto con el mensaje "No
     * tiene una sesion activa" y no se realiza la operacion
     * solicitada
     */
    if (!sessionService.checkActiveSession(userId)) {
      return Response.status(Response.Status.UNAUTHORIZED).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.NO_ACTIVE_SESSION))).build();
    }

    /*
     * Si la fecha de emision del JWT de un usuario NO es igual
     * a la fecha de emision de la sesion activa del usuario,
     * la aplicacion del lado servidor retorna el mensaje HTTP
     * 400 (Bad request) junto con el mensaje "El JWT no
     * corresponde a una sesion abierta" y no se realiza la
     * operacion solicitada.
     * 
     * Se debe tener en cuenta que el metodo checkDateIssueLastSession
     * de la clase SessionServiceBean debe ser invocado luego
     * de invocar el metodo checkActiveSession de la misma
     * clase, ya que de lo contrario se puede comparar la
     * fecha de emision de un JWT con una sesion inactiva,
     * lo cual es incorrecto porque la fecha de emision de un
     * JWT se debe comparar con la fecha de emision de una
     * sesion activa. El motivo por el cual puede ocurrir esta
     * comparacion con una sesion inactiva es que el metodo
     * findLastSession recupera la ultima sesion del usuario,
     * independientemente de si esta activa o inactiva.
     * 
     * Este control se implementa para evitar que se puedan
     * recuperar datos mediante peticiones HTTP haciendo uso
     * de un JWT valido, pero que es de una sesion que fue
     * cerrada por el usuario.
     */
    if (!sessionService.checkDateIssueLastSession(userId, JwtManager.getDateIssue(jwt, secretKeyValue))) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.JWT_NOT_ASSOCIATED_WITH_ACTIVE_SESSION)))
          .build();
    }

    /*
     * Si el dato solicitado no existe en la base de datos
     * subyacente, la aplicacion del lado servidor devuelve
     * el mensaje HTTP 404 (Not found) junto con el mensaje
     * "Recurso no encontrado" y no se realiza la operacion
     * solicitada
     */
    if (!plantingRecordService.checkExistence(plantingRecordId)) {
      return Response.status(Response.Status.NOT_FOUND).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.RESOURCE_NOT_FOUND))).build();
    }

    /*
     * Si al usuario que hizo esta peticion HTTP, no le pertenece
     * el dato solicitado, la aplicacion del lado servidor
     * devuelve el mensaje HTTP 403 (Forbidden) junto con el
     * mensaje "Acceso no autorizado" (contenido en el enum
     * ReasonError) y no se realiza la operacion solicitada
     */
    if (!plantingRecordService.checkUserOwnership(userId, plantingRecordId)) {
      return Response.status(Response.Status.FORBIDDEN).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.UNAUTHORIZED_ACCESS))).build();
    }

    /*
     * Si se intenta calcular la necesidad de agua de riego de
     * un cultivo perteneciente a un registro de plantacion
     * finalizado, en espera o muerto, la aplicacion del lado
     * servidor retorna el mensaje HTTP 400 (Bad request) junto
     * con el mensaje "No esta permitido calcular la necesidad
     * de agua de riego de un cultivo finalizado, en espera o
     * muerto" y no se realiza la operacion solicitada
     */
    if (plantingRecordService.checkFinishedStatus(plantingRecordId)
        || plantingRecordService.checkWaitingStatus(plantingRecordId)
        || plantingRecordService.checkDeadStatus(plantingRecordId)) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.INVALID_REQUEST_CALCULATION_IRRIGATION_WATER_NEED))).build();
    }

    /*
     * Si el flujo de ejecucion de este metodo llega a estas lineas
     * de codigo es debido a que el registro de plantacion sobre el
     * que se quiere calcular la necesidad de agua de riego es un
     * registro de plantacion que tiene el estado "En desarrollo".
     * Un registro de plantacion en desarrollo representa la existencia
     * de un cultivo sembrado en una parcela y en desarrollo en la
     * fecha actual. Por lo tanto, este metodo calcula la necesidad
     * de agua de riego de un cultivo en desarrollo en la fecha actual.
     */
    PlantingRecord developingPlantingRecord = plantingRecordService.find(plantingRecordId);

    /*
     * El valor de esta constante se asigna a la necesidad de
     * agua de riego [mm/dia] de un registro de plantacion
     * para el que no se puede calcular dicha necesidad, lo
     * cual, ocurre cuando no se tiene la evapotranspiracion
     * del cultivo bajo condiciones estandar (ETc) [mm/dia]
     * ni la precipitacion [mm/dia], siendo ambos valores de
     * la fecha actual.
     * 
     * El valor de esta constante tambien se asigna a la
     * necesidad de agua de riego de un registro de plantacion
     * finalizado o en espera, ya que NO tiene ninguna utilidad
     * que un registro de plantacion en uno de estos estados
     * tenga un valor numerico mayor o igual a cero en la
     * necesidad de agua de riego.
     * 
     * La abreviatura "n/a" significa "no disponible".
     */
    String notAvailable = plantingRecordService.getNotAvailable();

    /*
     * El simbolo de esta variable se utiliza para representar que la
     * necesidad de agua de riego de un cultivo en la fecha actual [mm/dia]
     * no esta disponible, pero se puede calcular. Esta situacion
     * ocurre unicamente para un registro de plantacion en desarrollo.
     */
    String cropIrrigationWaterNeedNotAvailableButCalculable = plantingRecordService.getCropIrrigationWaterNotAvailableButCalculable();
    PlantingRecordStatus status = statusService.calculateStatus(developingPlantingRecord);
    int parcelId = developingPlantingRecord.getParcel().getId();

    /*
     * Si el presunto estado del registro de plantacion
     * presuntamente en desarrollo, sobre el que se calcula
     * la necesidad de agua de riego de su cultivo, es
     * efectivamente el estado "Finalizado", la aplicacion
     * del lado servidor retorna el mensaje HTTP 400 (Bad
     * request) junto con un mensaje que indica lo sucedido,
     * no se realiza la operacion solicitada y se establece
     * el estado "Finalizado" en dicho registro
     */
    if (statusService.equals(status, statusService.findFinishedStatus())) {
      plantingRecordService.setStatus(plantingRecordId, status);

      /*
       * La necesidad de agua de riego, la capacidad de almacenamiento
       * de agua del suelo y el umbral de riego de un registro de
       * plantacion que tiene el estado finalizado tiene los valores
       * "n/a", 0 y 0, respectivamente
       */
      plantingRecordService.updateCropIrrigationWaterNeed(plantingRecordId, notAvailable);
      plantingRecordService.updateTotalAmountWaterAvailable(plantingRecordId, 0);
      plantingRecordService.updateOptimalIrrigationLayer(plantingRecordId, 0);

      /*
       * Si la parcela correspondiente al registro de plantacion
       * que paso de un estado de desarrollo al estado "Finalizado",
       * tiene registros de plantacion en el estado "En espera", se
       * obtiene de ellos el que tiene la fecha de siembra mas cercana
       * a la fecha actual
       */
      if (plantingRecordService.hasInWaitingPlantingRecords(userId, parcelId)) {
        int idFirstPlantingRecordInWaiting = plantingRecordService.findIdFirstOneOnWaiting(parcelId);
        PlantingRecord firstPlantingRecordInWaiting = plantingRecordService.find(idFirstPlantingRecordInWaiting);
        status = statusService.calculateStatus(firstPlantingRecordInWaiting);
        plantingRecordService.setStatus(idFirstPlantingRecordInWaiting, status);

        /*
         * El caracter "-" (guion) se utiliza para representar que la
         * necesidad de agua de riego de un cultivo en la fecha actual
         * (es decir, hoy) [mm/dia] NO esta disponible, pero se puede
         * calcular. Esta situacion ocurre unicamente para un registro
         * de plantacion que tiene el estado "En desarrollo" o el estado
         * "Desarrollo optimo". El que un registro de plantacion tenga
         * el estado "En desarrollo" o el estado "Desarrollo optimo"
         * depende de la fecha de siembra, la fecha de cosecha y la
         * bandera suelo de las opciones de la parcela a la que
         * pertenece. Si la fecha de siembra y la fecha de cosecha se
         * eligen de tal manera que la fecha actual (es decir, hoy)
         * esta dentro del periodo definido por ambas y la bandera
         * suelo esta activa, el registro adquiere el estado "En
         * desarrollo". En caso contrario, adquiere el estado
         * "Desarrollo optimo".
         */
        if (statusService.equals(status, statusService.findInDevelopmentStatus()) || statusService.equals(status, statusService.findOptimalDevelopmentStatus())) {
          plantingRecordService.updateCropIrrigationWaterNeed(idFirstPlantingRecordInWaiting, cropIrrigationWaterNeedNotAvailableButCalculable);
        }

        /*
         * Si la parcela del registro de plantacion obtenido tiene
         * la opcion suelo activa en sus opciones y si el registro
         * de plantacion tiene el estado "Desarrollo optimo" se
         * actualizan sus atributos "lamina total de agua disponible"
         * (capacidad de almacenamiento de agua del suelo) [mm] y
         * "lamina de riego optima" (umbral de riego) [mm]
         */
        if (statusService.equals(status, statusService.findOptimalDevelopmentStatus()) && firstPlantingRecordInWaiting.getParcel().getOption().getSoilFlag()) {
          plantingRecordService.updateTotalAmountWaterAvailable(idFirstPlantingRecordInWaiting, WaterMath
              .calculateTotalAmountWaterAvailable(firstPlantingRecordInWaiting.getCrop(), firstPlantingRecordInWaiting.getParcel().getSoil()));
          plantingRecordService.updateOptimalIrrigationLayer(idFirstPlantingRecordInWaiting, WaterMath
              .calculateOptimalIrrigationLayer(firstPlantingRecordInWaiting.getCrop(), firstPlantingRecordInWaiting.getParcel().getSoil()));
        }

      } // End if

      return Response.status(Response.Status.BAD_REQUEST)
          .entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.FINISHED_CROP_IN_CALCULATION_NEED_IRRIGATION_WATER, SourceUnsatisfiedResponse.WATER_NEED_CROP)))
          .build();
    } // End if

    /*
     * ***********************************************************
     * A partir de aqui comienza el codigo necesario para calcular
     * la necesidad de agua de riego de un cultivo en la fecha
     * actual [mm/dia]
     * ***********************************************************
     */

    String stringIrrigationWaterNeedCurrentDate = null;

    /*
     * Si la ubicacion geografica de la parcela correspondiente al registro
     * de plantacion que tiene el cultivo para el que se calcula la necesidad
     * de agua de riego en la fecha actual (es decir, hoy), fue modificada y
     * no hay conexion a Internet o el servicio meteorologico Visual Crossing
     * Weather no esta en funcionamiento, la aplicacion del lado servidor asigna
     * el valor "n/a" (no disponible) al atributo "necesidad de agua de riego
     * de un cultivo" del registro de plantacion que tiene el cultivo para
     * el que se realiza dicho calculo y devuelve el mensaje HTTP 400 (Bad
     * request) junto con un mensaje que indica lo sucedido y no se realiza
     * la operacion solicitada.
     * 
     * Este control se a debe a que cuando se modifica la ubicacion geografica
     * de una parcela, la aplicacion del lado servidor solicita los datos
     * climaticos de la nueva ubicacion geografica para actualizar los registros
     * climaticos de una parcela existentes en la base de datos subyacente.
     * Por lo tanto, si no hay conexion a Internet o el servicio meteorologico
     * utilizado no esta en funcionamiento, no es posible realizar dicha
     * solicitud. En consecuencia, en esta situacion no es posible calcular
     * la necesidad de agua de riego de un cultivo en la fecha actual.
     */
    if (developingPlantingRecord.getParcel().getModifiedGeographicLocationFlag() && !UtilConnection.checkInternetConnection()) {
      plantingRecordService.updateCropIrrigationWaterNeed(plantingRecordId, notAvailable);
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.POSSIBLE_INTERNET_CONNECTION_PROBLEM_IN_MODIFIED_GEOGRAPHIC_LOCATION_OF_PARCEL, SourceUnsatisfiedResponse.WATER_NEED_CROP)))
          .build();
    }

    /*
     * Si en el calculo de la necesidad de agua de riego de un cultivo en
     * la fecha actual (es decir, hoy) falta el registro climatico de un
     * dia o de mas de un dia del periodo de dias definido por la fecha
     * inmediatamente siguiente a la fecha de siembra de un cultivo y la
     * fecha inmediatametne anterior a la fecha actual, y no hay conexion
     * a Internet o el servicio meteorologico Visual Crossing Weather no
     * esta en funcionamiento, la aplicacion del lado servidor asigna el
     * valor "n/a" (no disponible) al atributo "necesidad de agua de riego
     * de un cultivo" del registro de plantacion que tiene el cultivo para
     * el que se realiza dicho calculo y devuelve el mensaje HTTP 400 (Bad
     * request) junto con un mensaje que indica lo sucedido y no se realiza
     * la operacion solicitada.
     * 
     * Este control se debe a que para calcular la necesidad de agua de
     * riego de un cultivo en la fecha actual se requiere el registro
     * climatico de cada uno de los dias del periodo de dias definido por
     * la fecha inmediatamente siguiente a la fecha de siembra de un cultivo
     * y la fecha inmediatamente anterior a la fecha actual. Por lo tanto,
     * si falta un registro climatico de un dia o de mas de un dia de dicho
     * periodo, la aplicacion del lado servidor los solicita al servicio
     * meteorologico utilizado. Pero si no hay conexion a Internet o el
     * servicio meteorologico utilizado no esta en funcionamiento, no es
     * posible realizar dicha solicitud. En consecuencia, en esta situacion
     * no es posible calcular la necesidad de agua de riego de un cultivo
     * en la fecha actual.
     */
    if (!climateRecordService.checkClimateRecordsToCalculateIrrigationWaterNeed(userId, parcelId,
        developingPlantingRecord.getSeedDate()) && !UtilConnection.checkInternetConnection()) {
      plantingRecordService.updateCropIrrigationWaterNeed(plantingRecordId, notAvailable);
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.POSSIBLE_INTERNET_CONNECTION_PROBLEM_IN_LACK_OF_CLIMATE_RECORDS, SourceUnsatisfiedResponse.WATER_NEED_CROP)))
          .build();
    }

    try {
      /*
       * Ejecuta el proceso del calculo de la necesidad de agua
       * de riego de un cultivo en la fecha actual [mm/dia]. Esto
       * es que ejecuta los metodos necesarios para calcular y
       * actualizar la necesidad de agua de riego de un cultivo
       * (en desarrollo) en la fecha actual.
       */
      stringIrrigationWaterNeedCurrentDate = runCalculationIrrigationWaterNeedCurrentDate(developingPlantingRecord);
    } catch (Exception e) {
      e.printStackTrace();

      /*
       * La aplicacion del lado servidor requiere de un servicio
       * meteorologico para obtener los datos climaticos de una
       * ubicacion geografica (*) que se necesitan para calcular
       * la necesidad de agua de riego de un cultivo en la fecha
       * actual (es decir, hoy). Si el servicio meteorologico
       * utilizado le devuelve a la aplicacion del lado servidor
       * un mensaje HTTP de error, NO es posible calcular la
       * necesidad de agua de riego de un cultivo en la fecha
       * actual. Por lo tanto, se establece el valor "n/a" (no
       * disponible) en el atributo de la necesidad de agua de
       * riego de un cultivo del registro de plantacion en
       * desarrollo para el que se solicito calcular la necesidad
       * de agua de riego de un cultivo en la fecha actual.
       * 
       * (*) Un cultivo se siembra en una parcela y una parcela
       * tiene una ubicacion geografica. Para calcular la
       * necesidad de agua de riego de un cultivo en una fecha
       * cualquiera se requieren los datos climaticos de la
       * fecha y de la ubicacion geografica de la parcela en
       * la que esta sembrado un cultivo.
       */
      plantingRecordService.updateCropIrrigationWaterNeed(plantingRecordId, notAvailable);

      /*
       * El mensaje de la excepcion contiene el codigo de respuesta
       * HTTP devuelto por el servicio meteorologico Visual Crossing
       * Weather porque en la clase ClimateClient se asigna dicho
       * codigo al mensaje de la excepcion producida
       */
      int responseCode = Integer.parseInt(e.getMessage());

      /*
       * La aplicacion del lado servidor utiliza el servicio
       * meteorologico Visual Crossing Weather para obtener los
       * datos meteorologicos necesarios para calcular la necesidad
       * de agua de riego de un cultivo en la fecha actual (es
       * decir, hoy) [mm/dia]. Para obtener datos de este servicio
       * se requiere una clave API. Si la clave NO es la correcta,
       * dicho servicio devuelve el mensaje HTTP 401 (Unauthorized).
       * Si la aplicacion del lado servidor recibe este mensaje
       * HTTP de parte de dicho servicio, devuelve el mensaje HTTP
       * 403 a la aplicacion del lado del navegador web junto con
       * el mensaje "La clave para solicitar datos meteorologicos
       * al servicio meteorológico Visual Crossing Weather, los
       * cuales son necesarios para calcular la necesidad de agua
       * de riego de un cultivo en la fecha actual (es decir, hoy),
       * no es la correcta" y no se realiza la operacion solicitada.
       * 
       * La clave API es provista por Visual Crossing Weather y
       * se encuentra en los detalles de la cuenta que uno debe
       * crear para usar dicho servicio. Dicha clave es el valor
       * de la constante API_KEY de la clase ClimateClient de la
       * ruta app/src/climate. Por lo tanto, una vez obtenida
       * la clave API, la misma debe ser asignada a la constante
       * API_KEY.
       */
      if (responseCode == UNAUTHORIZED) {
        return Response.status(Response.Status.BAD_REQUEST)
            .entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.INVALID_API_KEY, SourceUnsatisfiedResponse.WATER_NEED_CROP)))
            .build();
      }

      /*
       * El servicio meteorologico Visual Crossing Wather brinda
       * 1000 peticiones gratuitas por dia. Si al intentar calcular
       * la necesidad de agua de riego de un cultivo en la fecha
       * actual [mm/dia] se supera esta cantidad, dicho servicio
       * devuelve el mensaje HTTP 429 (Too many requests). Si la
       * aplicacion del lado servidor recibe este mensaje HTTP de
       * parte de dicho servicio, devuelve el mensaje HTTP 429 a
       * la aplicacion del lado del navegador web junto con el
       * mensaje "La aplicacion no puede calcular la necesidad de
       * agua de riego de un cultivo porque se supero la cantidad
       * de 1000 peticiones gratuitas por dia del servicio
       * meteorologico Visual Crossing Weather" y no se realiza la
       * operacion solicitada
       */
      if (responseCode == TOO_MANY_REQUESTS) {
        return Response.status(Response.Status.TOO_MANY_REQUESTS)
            .entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.REQUEST_LIMIT_EXCEEDED, SourceUnsatisfiedResponse.WATER_NEED_CROP)))
            .build();
      }

      /*
       * Si el servicio meteorologico Visual Crossing Weather NO
       * esta disponible al intentar calcular la necesidad de agua
       * de riego de un cultivo en la fecha actual [mm/dia],
       * devuelve el mensaje HTTP 503 (Service unavailable). Si la
       * aplicacion del lado servidor recibe este mensaje HTTP de
       * parte de dicho servicio, devuelve el mensaje HTTP 503 a
       * la aplicacion del lado del navegador web junot con el
       * mensaje "La aplicacion no puede calcular la necesidad de
       * agua de riego de un cultivo porque el servicio meteorologico
       * Visual Crossing Weather no se encuentra en funcionamiento"
       * y no se realiza la operacion solicitada
       */
      if (responseCode == SERVICE_UNAVAILABLE) {
        return Response.status(Response.Status.SERVICE_UNAVAILABLE)
            .entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.WEATHER_SERVICE_UNAVAILABLE, SourceUnsatisfiedResponse.WATER_NEED_CROP)))
            .build();
      }

      /*
       * Si al intentar calcular la necesidad de agua de riego
       * de un cultivo en la fecha actual [mm/dia], el servicio
       * meteorologico Visual Crossing Weather devuelve un
       * mensaje HTTP distinto a 401, 429 y 503, la aplicacion
       * del lado servidor devuelve el mensaje HTTP 500 (Internal
       * server error) a la aplicacion del lado del navegador web
       * junto con el mensaje "Se produjo un error al calcular
       * la necesidad de agua de riego de un cultivo debido a
       * una falla desconocida del servicio meteorologico Visual
       * Crossing Weather" y no se realiza la operacion solicitada
       */
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.UNKNOW_ERROR_IN_IRRIGATION_WATER_NEED_CALCULATION, SourceUnsatisfiedResponse.WATER_NEED_CROP)))
          .build();
    }

    /*
     * El valor de esta constante se utiliza para representar
     * la situacion en la que NO se calcula el acumulado del
     * deficit de humedad por dia de un balance hidrico de suelo
     * de una parcela que tiene un cultivo sembrado y en
     * desarrollo. Esta situacion ocurre cuando la perdida de
     * humedad del suelo de un conjunto de dias es estrictamente
     * mayor al doble de la capacidad de almacenamiento de agua
     * del suelo. Esto se representa mediante la condicion de
     * que el acumulado del deficit de humedad por dia sea estrictamente
     * menor al negativo del doble de la capacidad de almacenamiento
     * de agua del suelo, ya que el acumulado del deficit de
     * agua por dia puede ser negativo o cero. Cuando es negativo
     * representa que en un periodo de dias hubo perdida de humedad
     * en el suelo. En cambio, cuando es igual a cero representa
     * que la perdida de humedad que hubo en el suelo en un periodo
     * de dias esta totalmente cubierta. Esto es que el suelo
     * esta en capacidad de campo, lo significa que el suelo
     * esta lleno de agua o en su maxima capacidad de almacenamiento
     * de agua, pero no anegado.
     * 
     * Cuando la perdida de humedad del suelo, que tiene un
     * cultivo sembrado, de un conjunto de dias es estrictamente
     * mayor al doble de la capacidad de almacenamiento de agua
     * del suelo (representado mediante la conidicion de que el
     * acumulado del deficit de humedad por dia sea estrictamente
     * menor al negativo del doble de la capacidad de almacenamiento
     * de agua del suelo), el cultivo esta muerto, ya que ningun
     * cultivo puede sobrevivir con dicha perdida de humedad.
     * Por lo tanto, la presencia del valor "NC" (no calculado)
     * tambien representa la muerte de un cultivo.
     */
    String notCalculated = soilWaterBalanceService.getNotCalculated();

    /*
     * Si el flujo de ejecucion llego a esta linea de codigo fuente
     * es porque efectivamente se calculo la necesidad de agua de
     * riego de un cultivo en la fecha actual (es decir, hoy), por
     * lo tanto, se asigna el valor false a la variable bandera
     * flagNotGenerateSoilMoistureGraph del registro de plantacion
     * para que se pueda visualizar su grafico de evolucion diaria
     * del nivel de humedad del suelo en caso de que dicho registro
     * pertenezca a una parcela que tiene la bandera suelo activa
     * en sus opciones
     */
    plantingRecordService.unsetFlagNotGenerateSoilMoistureGraph(developingPlantingRecord.getId());

    /*
     * Si la necesidad de agua de riego de un cultivo (en desarrollo)
     * en la fecha actual (es decir, hoy) [mm/dia] es "NC" (No Calculado,
     * valor de la variable notCalculated) significa que el algoritmo
     * utilizado para calcular dicha necesidad es el que utiliza datos
     * de suelo, ya que al utilizar este algoritmo se retorna el valor
     * "NC" para representar la situacion en la que NO se calcula el
     * acumulado del deficit de humedad por dia del balance hidrico de
     * una parcela que tiene un cultivo sembrado y en desarrollo. Esta
     * situacion ocurre cuando la perdida de humedad del suelo, que
     * tiene un cultivo sembrado, es estrictamente mayor al doble de
     * la capacidad de almacenamiento de agua del mismo.
     * 
     * Cuando la perdida de humedad del suelo, que tiene un cultivo,
     * sembrado es estrictamente mayor al doble de la capacidad de
     * almacenamiento de agua del mismo, el cultivo muere. Por lo
     * tanto, la aplicacion del lado servidor asigna la abreviatura
     * "n/a" (no disponible) a la necesidad de agua de riego de un
     * cultivo en la fecha actual (es decir, hoy) [mm/dia] de un
     * registro de plantacion. En esta situacion, la aplicacion
     * retorna el mensaje HTTP 400 (Bad request) informando el
     * motivo por el cual el cultivo, para que el se intento
     * calcular su necesidad de agua de riego en la fecha actual
     * (es decir, hoy) [mm/dia], ha muerto.
     */
    if (stringIrrigationWaterNeedCurrentDate != null && stringIrrigationWaterNeedCurrentDate.equals(notCalculated)) {
      plantingRecordService.updateCropIrrigationWaterNeed(developingPlantingRecord.getId(), notAvailable);

      Calendar cropDeathDate = plantingRecordService.find(developingPlantingRecord.getId()).getDeathDate();
      double totalAmountWaterAvailable = WaterMath.calculateTotalAmountWaterAvailable(
          developingPlantingRecord.getCrop(), developingPlantingRecord.getParcel().getSoil());

      String message = "El cultivo murió porque la pérdida de humedad del suelo fue estrictamente mayor al doble"
          + " de la capacidad de almacenamiento de agua del suelo (2 * " + totalAmountWaterAvailable + " mm"
          + " = " + (2 * totalAmountWaterAvailable) + " mm). A partir de la fecha " + UtilDate.formatDate(cropDeathDate)
          + " la pérdida de humedad del suelo comenzó a ser de "
          + soilWaterBalanceService.calculateCropDeathSoilMoistureLoss(developingPlantingRecord.getParcel().getId(), cropDeathDate) 
          + " mm. El gráfico de la evolución diaria del nivel de humedad del suelo puede ser visualizado para un"
          + " cultivo muerto visualizando el respectivo registro de plantación.";

      return Response.status(Response.Status.BAD_REQUEST)
          .entity(mapper.writeValueAsString(new ErrorResponse(message, SourceUnsatisfiedResponse.DEAD_CROP_WATER_NEED)))
          .build();
    }

    /*
     * Si el valor de la necesidad de agua de riego de un
     * cultivo en la fecha actual [mm/dia] NO es igual al
     * valor "NC", significa que es un valor numerico. Por
     * lo tanto, se lo convierte a double, ya que dicha
     * necesidad esta expresada como double.
     */
    double cropIrrigationWaterNeedCurrentDate = Math.abs(Double.parseDouble(stringIrrigationWaterNeedCurrentDate));

    /*
     * *****************************************************
     * Actualizacion del atributo "necesidad agua riego de
     * cultivo" del registro de plantacion en desarrollo, que
     * tiene el cultivo para el que se solicita calcular su
     * necesidad de agua de riego en la fecha actual [mm/dia],
     * con el valor de de dicha necesidad de agua de riego
     * *****************************************************
     */
    plantingRecordService.updateCropIrrigationWaterNeed(developingPlantingRecord.getId(), String.valueOf(cropIrrigationWaterNeedCurrentDate));

    /*
     * Datos del formulario del calculo de la necesidad de
     * agua de riego de un cultivo en desarrollo en la fecha
     * actual. Este formulario se despliega en pantalla cuando
     * el usuario presiona el boton que tiene la etiqueta
     * Calcular sobre un registro de plantacion en desarrollo.
     */
    IrrigationWaterNeedFormData irrigationWaterNeedFormData = new IrrigationWaterNeedFormData();
    irrigationWaterNeedFormData.setParcel(developingPlantingRecord.getParcel());
    irrigationWaterNeedFormData.setCrop(developingPlantingRecord.getCrop());
    irrigationWaterNeedFormData.setCropIrrigationWaterNeed(cropIrrigationWaterNeedCurrentDate);
    irrigationWaterNeedFormData.setIrrigationDone(cropIrrigationWaterNeedCurrentDate);

    /*
     * El motivo por el cual se recupera el estado del registro
     * de plantacion desde la base de datos subyacente y no desde
     * la instancia de dicho registro es para recuperar el estado
     * actualizado y no un estado desactualizado
     */
    irrigationWaterNeedFormData.setStatus(plantingRecordService.find(developingPlantingRecord.getId()).getStatus());

    /*
     * Si la bandera suelo de las opciones de la parcela perteneciente
     * a un registro de plantacion en desarrollo, esta activa, se genera
     * el grafico de la evolucion diaria del nivel de humdad del suelo
     */
    if (developingPlantingRecord.getParcel().getOption().getSoilFlag()) {
      irrigationWaterNeedFormData.setSoilMoistureLevelGraph(generateSoilMoistureLevelGraph(developingPlantingRecord));
    } else {
      /*
       * En Java las variables de instancia de tipo primitivo
       * de tipo por referencia se inicializan de forma automatica
       * con un valor por defecto. En el caso de las variables
       * de instancia de tipo boolean (tipo primitivo), estas
       * se inicializan de manera automatica con el valor false.
       * 
       * La clase SoilMoistureLevelGraph tiene una variable de
       * de tipo boolean llamada showGraph. Al instanciar la
       * clase SoilMoistureLevelGraph, la variable showGraph
       * se inicializa de manera automatica en false. Esta
       * instruccion es para, que mediante la variable showGraph
       * en false, no mostrar el grafico de la evolucion diaria
       * del nivel de humedad del suelo para un registro de
       * plantacion perteneciente a una parcela que NO tiene la
       * bandera suelo activa en sus opciones.
       */
      irrigationWaterNeedFormData.setSoilMoistureLevelGraph(new SoilMoistureLevelGraph());
    }

    /*
     * Si el valor del encabezado de autorizacion de la peticion HTTP
     * dada, tiene un JWT valido, la aplicacion del lado servidor
     * devuelve el mensaje HTTP 200 (Ok) junto con los datos
     * pertinentes
     */
    return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(irrigationWaterNeedFormData)).build();
  }

  /**
   * @param developingPlantingRecord
   * @return referencia a un objeto de tipo String que contiene el
   * valor utilizado para determinar la necesidad de agua de riego
   * de un cultivo en la fecha actual [mm/dia]
   * @throws IOException
   */
  private String runCalculationIrrigationWaterNeedCurrentDate(PlantingRecord developingPlantingRecord) throws IOException {
    /*
     * Persiste los registros climaticos de la parcela de un registro
     * de plantacion en desarrollo desde la fecha de siembra hasta la
     * fecha inmediatamente anterior a la fecha actual, si NO existen
     * en la base de datos subyacente. Estos registros climaticos son
     * obtenidos del servicio meteorologico utilizado por la aplicacion.
     */
    requestPastClimateRecords(developingPlantingRecord);

    /*
     * Calcula la ETo y la ETc de los registros climaticos de la parcela
     * de un registro de plantacion en desarrollo previamente obtenidos.
     * La ETc es necesaria para calcular los balances hidricos de suelo
     * de una parcela que tiene un cultivo en desarrollo. 
     */
    calculateEtsPastClimateRecords(developingPlantingRecord);

    /*
     * Persiste el balance hidrico de la fecha de siembra de un cultivo,
     * si no existe en la base de datos subyacente. En caso contrario,
     * lo modifica. Este paso es el primer paso necesario para el
     * calculo de los balances hidricos de suelo de una parcela que
     * tiene un cultivo sembrado. Este calculo se realiza para
     * calcular la necesidad de agua de riego de un cultivo en la
     * fecha actual (es decir, hoy) [mm/dia].
     * 
     * El balance hidrico de la fecha de siembra de un cultivo tiene
     * el valor 0 en todos sus atributos porque en la fecha de siembra
     * de un cultivo se parte del suelo a capacidad de campo, esto es
     * que el suelo esta lleno de agua o en su maxima capacidad de
     * almacenamiento de agua, pero no anegado.
     */
    persistSoilWaterBalanceSeedDate(developingPlantingRecord);

    /*
     * El valor de esta constante se utiliza para representar
     * la situacion en la que NO se calcula el acumulado del
     * deficit de humedad por dia de un balance hidrico de suelo
     * de una parcela que tiene un cultivo sembrado y en
     * desarrollo. Esta situacion ocurre cuando la perdida de
     * humedad del suelo de un conjunto de dias es estrictamente
     * mayor al doble de la capacidad de almacenamiento de agua
     * del suelo. Esto se representa mediante la condicion de
     * que el acumulado del deficit de humedad por dia sea estrictamente
     * menor al negativo del doble de la capacidad de almacenamiento
     * de agua del suelo, ya que el acumulado del deficit de
     * agua por dia puede ser negativo o cero. Cuando es negativo
     * representa que en un periodo de dias hubo perdida de humedad
     * en el suelo. En cambio, cuando es igual a cero representa
     * que la perdida de humedad que hubo en el suelo en un periodo
     * de dias esta totalmente cubierta. Esto es que el suelo
     * esta en capacidad de campo, lo significa que el suelo
     * esta lleno de agua o en su maxima capacidad de almacenamiento
     * de agua, pero no anegado.
     * 
     * Cuando la perdida de humedad del suelo, que tiene un
     * cultivo sembrado, de un conjunto de dias es estrictamente
     * mayor al doble de la capacidad de almacenamiento de agua
     * del suelo (representado mediante la conidicion de que el
     * acumulado del deficit de humedad por dia sea estrictamente
     * menor al negativo del doble de la capacidad de almacenamiento
     * de agua del suelo), el cultivo esta muerto, ya que ningun
     * cultivo puede sobrevivir con dicha perdida de humedad.
     * Por lo tanto, la presencia del valor "NC" (no calculado)
     * tambien representa la muerte de un cultivo.
     */
    String notCalculated = soilWaterBalanceService.getNotCalculated();
    Parcel parcel = developingPlantingRecord.getParcel();
    Calendar seedDate = developingPlantingRecord.getSeedDate();

    /*
     * La necesidad de agua de riego de un cultivo en la fecha actual
     * se determina con el acumulado del deficit de humedad por dia
     * [mm/dia] de la fecha inmediatamente anterior a la fecha actual.
     * Por este motivo se recupera de la base de datos subyacente el
     * balance hidrico de suelo de la parcela, que tiene tiene un cultivo
     * sembrado y en desarrollo, de la fecha inmediatamente anterior
     * a la fecha actual.
     */
    Calendar yesterday = UtilDate.getYesterdayDate();
    Calendar currentDate = UtilDate.getCurrentDate();

    /*
     * Si la fecha de siembra de un cultivo es igual a la fecha actual
     * (es decir, hoy), la necesidad de agua de riego de un cultivo
     * en la fecha actual [mm/dia] es el acumulado del deficit de
     * humedad de suelo por dia [mm/dia] de la fecha actual
     */
    if (UtilDate.compareTo(seedDate, currentDate) == 0) {
      return soilWaterBalanceService.find(parcel.getId(), currentDate).getAccumulatedSoilMoistureDeficitPerDay();
    }

    /*
     * Si la fecha de siembra de un cultivo es igual a la fecha
     * inmediatamente anterior a la fecha actual (es decir, hoy),
     * la necesidad de agua de riego de un cultivo en la fecha
     * actual [mm/dia] es el acumulado del deficit de humedad
     * de suelo por dia [mm/dia] de la fecha inmediatamente
     * anterior a la fecha actual
     */
    if (UtilDate.compareTo(seedDate, yesterday) == 0) {
      return soilWaterBalanceService.find(parcel.getId(), yesterday).getAccumulatedSoilMoistureDeficitPerDay();
    }

    /*
     * Calcula y persiste los balances hidricos de suelo de una parcela,
     * que tiene un cultivo en desarrollo, para calcular la necesidad
     * de agua de riego del mismo en la fecha actual [mm/dia]. Esto se
     * realiza si y solo si la cantidad de dias entre la fecha de siembra
     * de un cultivo y la fecha actual (es decir, hoy) es mayor o igual
     * a dos.
     */
    calculateSoilWaterBalances(developingPlantingRecord);

    /*
     * La necesidad de agua de riego de un cultivo en la fecha actual
     * (es decir, hoy) [mm/dia] se determina en funcion del acumulado
     * del deficit de humedad por dia [mm/dia] del dia inmediatamente
     * anterior a la fecha actual
     */
    String stringAccumulatedWaterDeficitPerDayFromYesterday = soilWaterBalanceService.find(parcel.getId(), yesterday).getAccumulatedSoilMoistureDeficitPerDay();

    /*
     * Si el valor del acumulado del deficit de humedad por dia [mm/dia]
     * de ayer es "NC" (no calculado), significa dos cosas:
     * - que el algoritmo utilizado para calcular la necesidad de
     * agua de riego de un cultivo en la fecha actual [mm/dia] es
     * el que utiliza el suelo para ello,
     * - y que la perdida de humeda del suelo, que tiene un cultivo
     * sembrado, fue estrictamente mayor al doble de la capacidad de
     * almacenamiento de agua del suelo.
     * 
     * Por lo tanto, se retorna "NC" para indicar que la necesidad de
     * agua de riego de un cultivo en la fecha actual [mm/dia] no se
     * calculo, ya que el cultivo esta muerto debido a que ningun
     * cultivo puede sobrevivir con una perdida de humedad del suelo
     * estrictamente mayor al doble de la capacidad de almacenamiento
     * de agua del suelo.
     */
    if (stringAccumulatedWaterDeficitPerDayFromYesterday.equals(notCalculated)) {
      return notCalculated;
    }

    double totalIrrigationWaterCurrentDate = irrigationRecordService.calculateTotalIrrigationWaterCurrentDate(parcel.getId());
    double accumulatedSoilMoistureDeficitPerDayFromYesterday = Double.parseDouble(stringAccumulatedWaterDeficitPerDayFromYesterday);

    /*
     * Si la bandera suelo de una parcela esta activa, se
     * comprueba el nivel de humedad del suelo para establecer
     * el estado del registro de plantacion en desarrollo para
     * el que se calcula la necesidad de agua de riego de su
     * cultivo en la fecha actual (es decir, hoy) [mm/dia].
     * 
     * El metodo calculateStatusRelatedToSoilMoistureLevel()
     * de la clase PlantingRecordStatusService se ocupa de
     * comprobar el nivel de humedad del suelo y retorna el
     * estado correspondiente.
     */
    if (parcel.getOption().getSoilFlag()) {
      plantingRecordService.setStatus(developingPlantingRecord.getId(), statusService.calculateStatusRelatedToSoilMoistureLevel(totalIrrigationWaterCurrentDate,
              accumulatedSoilMoistureDeficitPerDayFromYesterday, developingPlantingRecord));
    }

    /*
     * Calculo de la necesidad de agua de riego de un cultivo
     * en la fecha actual [mm/dia]. El motivo por el cual este
     * calculo corresponde a la fecha actual es que la cantidad
     * total de agua de riego de un cultivo [mm/dia] es de la
     * fecha actual y el acumulado del deficit de humedad por dia
     * [mm] es del dia inmediatamente anterior a la fecha actual
     * (es decir, hoy). En cambio, si en este metodo se utiliza
     * la cantidad total de agua de riego de ayer y el acumulado
     * del deficit de humedad por dia de antes de ayer, la necesidad
     * de agua de riego de un cultivo calculada es de ayer. Por
     * lo tanto, lo que determina la fecha de la necesidad de agua
     * de riego de un cultivo es la fecha de la cantidad total
     * de agua de riego de un cultivo y la fecha del acumulado
     * del deficit de humedad por dia.
     */
    return String.valueOf(WaterMath.calculateCropIrrigationWaterNeed(totalIrrigationWaterCurrentDate, accumulatedSoilMoistureDeficitPerDayFromYesterday));
  }

  /**
   * Persiste los registros climaticos de una parcela, que
   * tiene un cultivo en desarrollo, desde la fecha
   * inmediatamente siguiente a la fecha de siembra hasta
   * la fecha inmediatamente anterior a la fecha actual
   * (es decir, hoy)
   * 
   * @param developingPlantingRecord
   */
  private void requestPastClimateRecords(PlantingRecord developingPlantingRecord) throws IOException {
    Calendar seedDate = developingPlantingRecord.getSeedDate();
    Calendar yesterday = UtilDate.getYesterdayDate();

    /*
     * Si la fecha de siembra de un cultivo es igual a la fecha
     * actual (es decir, hoy), NO se solicita ni se persiste el
     * registro climatico de la fecha actual, ya que en la fecha
     * de siembra se parte del suelo en capacidad de campo, esto
     * es que el suelo esta lleno de agua, pero no anegado. En
     * esta situacion, el acumulado del deficit de humedad por dia
     * [mm/dia] de la fecha actual es 0. Por lo tanto, la necesidad
     * de agua de riego de un cultivo en la fecha actual [mm/dia]
     * es 0.
     */
    if (UtilDate.compareTo(seedDate, UtilDate.getCurrentDate()) == 0) {
      return;
    }

    /*
     * Si la fecha de siembra de un cultivo es igual a la fecha
     * inmediatamente anterior a la fecha actual (es decir, hoy),
     * NO se solicita ni persiste el registro climatico de la
     * fecha inmediatamente anterior a la fecha actual, ya que
     * en la fecha de siembra se parte del suelo en capacidad
     * de campo, esto es que el suelo esta lleno de agua, pero
     * no anegado. En esta situacion, el acumulado del deficit
     * de agua por dia [mm/dia] del dia inmediatamente anterior
     * es 0. Por lo tanto, la necesidad de agua de riego de
     * un cultivo en la fecha actual [mm/dia] es 0.
     */
    if (UtilDate.compareTo(seedDate, yesterday) == 0) {
      return;
    }

    Parcel parcel = developingPlantingRecord.getParcel();
    ClimateRecord newClimateRecord = null;
    ClimateRecord climateRecord = null;

    /*
     * Los balances hidricos de suelo se calculan a partir de la
     * fecha inmediatamente siguiente a la fecha de siembra de un
     * cultivo hasta la fecha inmediatamente anterior a la fecha
     * actual (es decir, hoy). Por este motivo se solicitan (al
     * servicio meteorologico utilizado por la aplicacion) y persisten
     * los registros climaticos (contienen datos meteorologicos)
     * de una parcela, que tiene un cultivo sembrado, desde la
     * fecha inmediatamente siguiente a la fecha de siembra de
     * un cultivo hasta la fecha inmediatamente anterior a la
     * fecha actual (es decir, hoy). Los datos meteorologicos de
     * cada uno de los dias perteneciente al periodo definido por
     * ambas fechas, son necesarios para calcular la ETo (evapotranspiracion
     * del cultivo de referencia) [mm/dia] y la ETc (evapotranspiracion
     * del cultivo bajo condiciones estandar) [mm/dia] de cada
     * uno de los dias de dicho periodo. La ETo y la ETc de cada
     * uno de los dias de dicho periodo son necesarias para calcular
     * el balance hidrico de suelo de cada uno de los dias pertencientes
     * al periodo, lo cual da como resultado un conjunto de balances
     * hidricos de suelo calculados desde la fecha inmediatamente
     * siguiente a la fecha de siembra de un cultivo hasta la
     * fecha inmediatamente anterior a la fecha actual (es decir,
     * hoy). Estos son necesarios para determinar el acumulado
     * del deficit agua por dia [mm/dia] del dia inmediatamente
     * anterior a la fecha actual en la que esta sembrado un
     * cultivo. Este valor acumulado es necesario porque en base
     * a el se determina la necesidad de agua de riego de un
     * cultivo en la fecha actual (es decir, hoy) [mm/dia].
     */
    Calendar dateFollowingSeedDate = UtilDate.getNextDateFromDate(seedDate);
    Calendar pastDate = Calendar.getInstance();
    pastDate.set(Calendar.YEAR, dateFollowingSeedDate.get(Calendar.YEAR));
    pastDate.set(Calendar.MONTH, dateFollowingSeedDate.get(Calendar.MONTH));
    pastDate.set(Calendar.DAY_OF_YEAR, dateFollowingSeedDate.get(Calendar.DAY_OF_YEAR));

    /*
     * Los registros climaticos a obtener pertenecen al periodo
     * definido por la fecha inmediatamente siguiente a la fecha
     * de siembra y la fecha inmediatamente anterior a la fecha
     * actual (es decir, hoy).
     * 
     * Se debe sumar un uno al resultado de esta diferencia para
     * que este metodo persista el registro climatico de la fecha
     * inmediatamente anterior a la fecha actual.
     */
    int days = UtilDate.calculateDifferenceBetweenDates(dateFollowingSeedDate, yesterday) + 1;

    /*
     * Crea y persiste los registros climaticos desde la fecha
     * inmediatamente siguiente a la fecha de siembra hasta
     * la fecha inmediatamente anterior a la fecha actual (es
     * decir, hoy), pertenecientes a una parcela que tiene un
     * cultivo en desarrollo en la fecha actual
     */
    for (int i = 0; i < days; i++) {

      /*
       * Si una parcela NO tiene un registro climatico perteneciente
       * a una fecha, se lo solicita al servicio meteorologico
       * utilizado y se lo persiste
       */
      if (!climateRecordService.checkExistence(pastDate, parcel)) {
        newClimateRecord = ClimateClient.getForecast(parcel, pastDate, typePrecipService.findAll());
        climateRecordService.create(newClimateRecord);
      }

      /*
       * Si existe el registro climatico perteneciente a una parcela
       * y una fecha y si la ubicacion geografica de una parcela fue
       * modificada, se solicitan los datos meteorologicos correspondientes
       * a una fecha y una nueva ubicacion geografica. Esto es necesario
       * para actualizar los datos meteorologicos de los registros
       * climaticos comprendidos en un periodo definido por dos fechas
       * pertenecientes a una parcela que fueron obtenidos antes de la
       * modificacion de la ubicacion geografica de la misma.
       */
      if (climateRecordService.checkExistence(pastDate, parcel) && parcel.getModifiedGeographicLocationFlag()) {
        climateRecord = climateRecordService.find(pastDate, parcel);
        climateRecordService.modify(climateRecord.getId(), ClimateClient.getForecast(parcel, pastDate, typePrecipService.findAll()));
      }

      /*
       * Suma un uno al numero de dia en el año de una fecha
       * pasada dada para obtener el siguiente registro climatico
       * correspondiente a una fecha pasada
       */
      pastDate.set(Calendar.DAY_OF_YEAR, pastDate.get(Calendar.DAY_OF_YEAR) + 1);
    } // End for

    /*
     * Luego de actualizar los registros climaticos comprendidos en
     * un periodo definido por dos fechas pertenecientes a una parcela,
     * con los datos meteorologicos de las fechas de dicho periodo y de
     * la nueva ubicacion geografica de una parcela, se establece la
     * bandera modifiedGeographicLocationFlag de una parcela en false
     * para evitar que la aplicacion solicite nuevamente los datos
     * meteorologicos de la nueva ubicacion geografica de una parcela
     * al calcular la necesidad de agua de riego de un cultivo (en
     * desarrollo) en la fecha actual
     */
    if (parcel.getModifiedGeographicLocationFlag()) {
      parcelService.unsetModifiedGeographicLocationFlag(parcel.getId());
    }

  }

  /**
   * Calcula y actualiza la ETo y la ETc de los registros
   * climaticos, pertenecientes a una parcela que tiene un
   * cultivo en desarrollo en la fecha actual, comprendidos
   * en el periodo definido por la fecha inmediatamente
   * siguiente a la fecha de siembra de un cultivo y la
   * fecha inmediatamente anterior a la fecha actual (es
   * decir, hoy)
   * 
   * @param developingPlantingRecord
   */
  private void calculateEtsPastClimateRecords(PlantingRecord developingPlantingRecord) {
    Calendar seedDate = developingPlantingRecord.getSeedDate();
    Calendar yesterday = UtilDate.getYesterdayDate();

    /*
     * Si la fecha de siembra de un cultivo es igual a la fecha
     * actual (es decir, hoy), NO se calculan la ETo y la ETc
     * del registro climatico de la fecha actual, ya que NO se
     * lo persiste debido a que en la fecha de siembra se parte
     * del suelo en capacidad de campo, esto es que el suelo
     * esta lleno de agua, pero no anegado. En esta situacion,
     * el acumulado del deficit de humedad por dia [mm/dia] de la
     * fecha actual es 0. Por lo tanto, la necesidad de agua de
     * riego de un cultivo en la fecha actual [mm/dia] es 0.
     */
    if (UtilDate.compareTo(seedDate, UtilDate.getCurrentDate()) == 0) {
      return;
    }

    /*
     * Si la fecha de siembra de un cultivo es igual a la fecha
     * inmediatamente anterior a la fecha actual (es decir, hoy),
     * NO se calculan la ETo y la ETc del registro climatico de
     * la fecha inmediatamente anterior a la fecha actual, ya que
     * NO se lo persiste debido a que en la fecha de siembra se
     * parte del suelo en capacidad de campo, esto es que el suelo
     * esta lleno de agua, pero no anegado. En esta situacion,
     * el acumulado del deficit de humedad por dia [mm/dia] del dia
     * inmediatamente anterior a la fecha actual es 0. Por lo tanto,
     * la necesidad de agua de riego de un cultivo en la fecha
     * actual [mm/dia] es 0.
     */
    if (UtilDate.compareTo(seedDate, yesterday) == 0) {
      return;
    }

    Parcel parcel = developingPlantingRecord.getParcel();
    ClimateRecord climateRecord = null;

    /*
     * Los balances hidricos de suelo se calculan a partir de la
     * fecha inmediatamente siguiente a la fecha de siembra de un
     * cultivo hasta la fecha inmediatamente anterior a la fecha
     * actual (es decir, hoy). Por este motivo se calculan la ETo
     * (evapotranspiracion del cultivo de referencia) [mm/dia] y
     * la ETc (evapotranspiracion del cultivo bajo condiciones
     * estandar) [mm/dia] de los registros climaticos (contienen
     * datos meteorologicos), pertenecientes a una parcela que
     * tiene un cultivo sembrado, a partir de la fecha inmediatamente
     * siguiente a la fecha de siembra de un cultivo hasta la fecha
     * inmediatamente anterior a la fecha actual. La ETo y la ETc
     * de cada uno de los dias pertenecientes al periodo definido
     * por la fecha inemdiatamente siguiente a la fecha de siembra
     * de un cultivo y la fecha inmediatamente anterior a la
     * fecha actual, son necesarias para el calculo del balance
     * hidrico de suelo de cada uno de esos dias. Esto da como
     * resultado un conjunto de balances hidricos de suelo
     * calculados desde la fecha inmediatamente siguiente a la
     * fecha de siembra de un cultivo hasta la fecha inmediatamente
     * anterior a la fecha actual, los cuales son necesarios para
     * determinar el acumulado del deficit agua por dia [mm/dia]
     * del dia inmediatamente anterior a la fecha actual en la
     * que esta sembrado un cultivo. Este valor acumulado es
     * necesario porque en base a el se determina la necesidad de
     * agua de riego de un cultivo en la fecha actual (es decir,
     * hoy) [mm/dia].
     */
    Calendar dateFollowingSeedDate = UtilDate.getNextDateFromDate(seedDate);
    Calendar pastDate = Calendar.getInstance();
    pastDate.set(Calendar.YEAR, dateFollowingSeedDate.get(Calendar.YEAR));
    pastDate.set(Calendar.MONTH, dateFollowingSeedDate.get(Calendar.MONTH));
    pastDate.set(Calendar.DAY_OF_YEAR, dateFollowingSeedDate.get(Calendar.DAY_OF_YEAR));

    /*
     * Los registros climaticos para los que se calcula
     * su ETo y su ETc pertenecen al periodo definido por
     * la fecha inmediatamente siguiente a la fecha de
     * siembra de un cultivo y la fecha inmediatamente
     * anterior a la fecha actual (es decir, hoy).
     * 
     * Se debe sumar un uno al resultado de esta diferencia
     * para que este metodo calcule la ETo y la ETc del
     * registro climatico de la fecha inmediatamente
     * anterior a la fecha actual.
     */
    int days = UtilDate.calculateDifferenceBetweenDates(dateFollowingSeedDate, yesterday) + 1;

    double eto = 0.0;
    double etc = 0.0;

    /*
     * Calcula la ETo y la ETc de los registros climaticos,
     * pertenecientes a una parcela que tiene un cultivo
     * en desarrollo en la fecha actual, comprendidos en
     * el periodo definido por la fecha inmediatamente
     * siguiente a la fecha de siembra de un cultivo y la
     * fecha inmediatamente anterior a la fecha actual (es
     * decir, hoy)
     */
    for (int i = 0; i < days; i++) {

      if (climateRecordService.checkExistence(pastDate, parcel)) {
        climateRecord = climateRecordService.find(pastDate, parcel);

        eto = calculateEtoForClimateRecord(climateRecord);
        etc = calculateEtcForClimateRecord(eto, developingPlantingRecord, pastDate);

        climateRecordService.updateEtoAndEtc(pastDate, parcel, eto, etc);

        /*
         * Luego de calcular la ETc de un registro climatico, se debe
         * restablecer el valor por defecto de esta variable para evitar
         * el error logico de asignar la ETc de un registro climatico a
         * otro registro climatico
         */
        etc = 0.0;
      }

      /*
       * Suma un uno al numero de dia en el año de una fecha
       * pasada dada para obtener el siguiente registro climatico
       * correspondiente a una fecha pasada
       */
      pastDate.set(Calendar.DAY_OF_YEAR, pastDate.get(Calendar.DAY_OF_YEAR) + 1);
    } // End for

  }

  /**
   * Calcula y persiste los balances hidricos de suelo de
   * una parcela, que tiene un cultivo sembrado, desde la
   * fecha inmediatamente siguiente a la fecha de siembra
   * hasta la fecha inmediatamente anterior a la fecha
   * actual (es decir, hoy)
   * 
   * @param developingPlantingRecord
   */
  private void calculateSoilWaterBalances(PlantingRecord developingPlantingRecord) {
    Calendar seedDate = developingPlantingRecord.getSeedDate();
    Calendar yesterday = UtilDate.getYesterdayDate();

    /*
     * Si la fecha de siembra de un cultivo es igual a la fecha
     * actual (es decir, hoy), NO se calcula el balance hidrico
     * de la fecha actual, ya que en la fecha de siembra se parte
     * del suelo en capacidad de campo, esto es que el suelo esta
     * lleno de agua o en maxima capacidad de almacenamiento de
     * agua, pero no anegado. En esta situacion, el acumulado del
     * deficit de humedad por dia [mm/dia] de la fecha actual es 0.
     * Por lo tanto, la necesidad de agua de riego de un cultivo
     * en la fecha actual [mm/dia] es 0.
     */
    if (UtilDate.compareTo(seedDate, UtilDate.getCurrentDate()) == 0) {
      return;
    }

    /*
     * Si la fecha de siembra de un cultivo es igual a la fecha
     * inmediatamente anterior a la fecha actual (es decir, hoy),
     * NO se calcula el balance hidrico de la fecha inmediatamente
     * anterior a la fecha actual, ya que en la fecha de siembra
     * se parte del suelo en capacidad de campo, esto es que el
     * suelo esta lleno de agua o en su maxima capacidad de
     * almacenamiento de agua, pero no anegado. En esta situacion,
     * el acumulado del deficit de humedad por dia [mm/dia] del dia
     * inmediatamente anterior es 0. Por lo tanto, la necesidad
     * de agua de riego de un cultivo en la fecha actual [mm/dia]
     * es 0.
     */
    if (UtilDate.compareTo(seedDate, yesterday) == 0) {
      return;
    }

    Parcel parcel = developingPlantingRecord.getParcel();
    Crop crop = developingPlantingRecord.getCrop();
    SoilWaterBalance soilWaterBalance = null;
    ClimateRecord climateRecord = null;

    /*
     * Los balances hidricos de suelo de una parcela, que
     * tiene un cultivo sembrado, se calculan desde la
     * fecha inmediatamente siguiente a la fecha de
     * siembra de un cultivo hasta la fecha inmediatamente
     * anterior a la fecha actual (es decir, hoy)
     */
    Calendar pastDate = UtilDate.getNextDateFromDate(seedDate);
    Calendar yesterdayDateFromDate = null;
    Calendar soilWaterBalanceDate = null;

    /*
     * Los balances hidricos de suelo de una parcela, que
     * tiene un cultivo sembrado, se calculan desde la
     * fecha inmediatamente siguiente a la fecha de siembra
     * hasta la fecha inmediatamente anterior a la fecha
     * actual (es decir, hoy).
     * 
     * Se debe sumar un uno al resultado de esta diferencia
     * para que este metodo calcule el balance hidrico de
     * suelo de la fecha inmediatamente anterior a la
     * fecha actual.
     */
    int days = UtilDate.calculateDifferenceBetweenDates(pastDate, yesterday) + 1;
    int parcelId = parcel.getId();

    /*
     * El valor de esta variable es la precipitacion
     * natural por dia [mm/dia] o la precipitacion
     * artificial (agua de riego) por dia [mm/dia] o
     * la suma de ambas [mm/dia]
     */
    double waterProvidedPerDay = 0.0;
    double soilMoistureDeficitPerDay = 0.0;
    double soilMoistureLossPerDay = 0.0;
    double accumulatedSoilMoistureDeficitPerDay = 0.0;
    double accumulatedWaterDeficitPerPreviousDay = 0.0;
    double totalAmountCropIrrigationWaterPerDay = 0.0;

    /*
     * Esta variable representa la capacidad de almacenamiento
     * de agua del suelo [mm], la cual esta determinada por
     * la lamina total de agua disponible (dt) [mm]
     */
    double totalAmountWaterAvailable = 0.0;

    String stringAccumulatedWaterDeficitPerPreviousDay = null;
    String stringAccumulatedSoilMoistureDeficitPerDay = null;

    /*
     * El valor de esta constante se utiliza para representar
     * la situacion en la que NO se calcula el acumulado del
     * deficit de humedad por dia de un balance hidrico de suelo
     * de una parcela que tiene un cultivo sembrado y en
     * desarrollo. Esta situacion ocurre cuando la perdida de
     * humedad del suelo de un conjunto de dias es estrictamente
     * mayor al doble de la capacidad de almacenamiento de agua
     * del suelo. Esto se representa mediante la condicion de
     * que el acumulado del deficit de humedad por dia sea estrictamente
     * menor al negativo del doble de la capacidad de almacenamiento
     * de agua del suelo, ya que el acumulado del deficit de
     * agua por dia puede ser negativo o cero. Cuando es negativo
     * representa que en un periodo de dias hubo perdida de humedad
     * en el suelo. En cambio, cuando es igual a cero representa
     * que la perdida de humedad que hubo en el suelo en un periodo
     * de dias esta totalmente cubierta. Esto es que el suelo
     * esta en capacidad de campo, lo significa que el suelo
     * esta lleno de agua o en su maxima capacidad de almacenamiento
     * de agua, pero no anegado.
     * 
     * Cuando la perdida de humedad del suelo, que tiene un
     * cultivo sembrado, de un conjunto de dias es estrictamente
     * mayor al doble de la capacidad de almacenamiento de agua
     * del suelo (representado mediante la conidicion de que el
     * acumulado del deficit de humedad por dia sea estrictamente
     * menor al negativo del doble de la capacidad de almacenamiento
     * de agua del suelo), el cultivo esta muerto, ya que ningun
     * cultivo puede sobrevivir con dicha perdida de humedad.
     * Por lo tanto, la presencia del valor "NC" (no calculado)
     * tambien representa la muerte de un cultivo.
     */
    String notCalculated = soilWaterBalanceService.getNotCalculated();
    PlantingRecordStatus deadStatus = statusService.findDeadStatus();

    /*
     * Calcula los balances hidricos de suelo de una parcela,
     * que tiene un cultivo en desarrollo en la fecha actual
     * (es decir, hoy), desde la fecha inmediatamente siguiente
     * a la fecha de siembra hasta la fecha inmediatamente
     * anterior a la fecha actual
     */
    for (int i = 0; i < days; i++) {
      /*
       * Obtencion del registro climatico y de los registros de
       * riego de una fecha para el calculo del agua provista
       * en un dia (fecha) [mm/dia] y del deficit de humedad en un
       * dia (fecha) [mm/dia]
       */
      climateRecord = climateRecordService.find(pastDate, parcel);

      totalAmountCropIrrigationWaterPerDay = irrigationRecordService.calculateTotalAmountCropIrrigationWaterForDate(parcelId, pastDate);
      waterProvidedPerDay = WaterMath.calculateWaterProvidedPerDay(climateRecord.getPrecip(), totalAmountCropIrrigationWaterPerDay);
      soilMoistureDeficitPerDay = WaterMath.calculateSoilMoistureDeficitPerDay(climateRecord.getEtc(), climateRecord.getPrecip(), totalAmountCropIrrigationWaterPerDay);
      soilMoistureLossPerDay = climateRecord.getEtc();

      /*
       * Obtiene el acumulado del deficit de humedad por dia del
       * balance hidrico de suelo de la fecha inmediatamente
       * a una fecha pasada
       */
      yesterdayDateFromDate = UtilDate.getYesterdayDateFromDate(pastDate);
      stringAccumulatedWaterDeficitPerPreviousDay = soilWaterBalanceService.find(parcelId, yesterdayDateFromDate).getAccumulatedSoilMoistureDeficitPerDay();

      /*
       * Si el acumulado del deficit de humedad por dia de la fecha
       * inmediatamente anterior a una fecha pasada NO es NC (no
       * calculado), significa que el cultivo correspondiente a
       * este calculo de balances hidricos de suelo NO murio en
       * la fecha inmediatamente anterior a la fecha pasada, por
       * lo tanto, se calcula el acumulado del deficit de humedad
       * por dia [mm/dia] de la fecha pasada, lo cual se realiza
       * para calcular el balance hidrico de suelo, en el que esta
       * sembrado un cultivo, de la fecha pasada. En caso contrario,
       * significa que el cultivo murio en la fecha inmediatamente
       * anterior a la fecha pasada, por lo tanto, NO se calcula el
       * acumulado del deficit de humedad por dia [mm/dia] de la
       * fecha pasada. Cuando un cultivo esta muerto se asigna la
       * sigla "NC" (No Calculado) a la variable de tipo String
       * accumulatedSoilMoistureDeficitPerDay [mm/dia] de un
       * balance hidrico de suelo.
       */
      if (!stringAccumulatedWaterDeficitPerPreviousDay.equals(notCalculated)) {
        accumulatedWaterDeficitPerPreviousDay = Double.parseDouble(stringAccumulatedWaterDeficitPerPreviousDay);

        /*
         * El acumulado del deficit de humedad por dia [mm/dia] de
         * una fecha depende del acumulado del deficit de humedad
         * por dia de la fecha inmdiatamente anterior
         */
        accumulatedSoilMoistureDeficitPerDay = WaterMath.accumulateSoilMoistureDeficitPerDay(soilMoistureDeficitPerDay, accumulatedWaterDeficitPerPreviousDay);
        stringAccumulatedSoilMoistureDeficitPerDay = String.valueOf(accumulatedSoilMoistureDeficitPerDay);

        /*
         * Si el estado del registro de plantacion que contiene el
         * cultivo para el que se calcula la necesidad de agua de
         * riego en la fecha actual (es decir, hoy) [mm/dia], NO
         * tiene el estado "Muerto" y si la parcela a la que
         * pertenece tiene la bandera suelo activa en sus opciones,
         * se comprueba si el acumulado del deficit de humedad por
         * dia de una fecha pasada es estrictamente menor al doble
         * de la capacidad de almacenamiento de agua del suelo que
         * contiene la parcela. Si lo es, el cultivo que contiene
         * el registro de plantacion ha muerto en una fecha
         * pasada. En caso contrario, no ha muerto en una fecha
         * pasada.
         * 
         * Lo que se busca determinar con esta comprobacion es
         * determinar si la perdida de humedad del suelo, que
         * tiene un cultivo sembrado, es estrictamente mayor al
         * doble de la capacidad de almacenamiento de agua del
         * suelo en una fecha pasada. Si lo es, el cultivo murio
         * en una fecha pasada, ya que ningún cultivo puede
         * sobrevivir con dicha perdida. El motivo de esto es
         * que cuando el acumulado del deficit de humedad por dia
         * es negativo representa que en un periodo de dias hubo
         * perdida de humedad en el suelo. El acumulado del deficit
         * de agua por dia tambien puede ser cero, ademas de negativo.
         * Cuando es igual a cero representa que la perdida de
         * humedad que hubo en el suelo en un periodo de dias esta
         * totalmente cubierta. Esto es que el suelo esta en capacidad
         * de campo, lo que significa que el suelo esta lleno de
         * agua o en su maxima capacidad de almacenamiento de agua,
         * pero no anegado.
         */
        if (!statusService.equals(developingPlantingRecord.getStatus(), deadStatus) && parcel.getOption().getSoilFlag()) {

          /*
           * El suelo de una parcela debe ser obtenido unicamente
           * si la bandera suelo de las opciones de una parcela
           * esta activa. Esto se debe a que la aplicacion
           * permite que la bandera suelo de las opciones de una
           * parcela sea activada si y solo si una parcela tiene
           * un suelo asignado. Esto esta implementado como un
           * control en el metodo modify() de la clase OptionRestServlet.
           */
          totalAmountWaterAvailable = WaterMath.calculateTotalAmountWaterAvailable(crop, parcel.getSoil());

          /*
           * Si el acumulado del deficit de humedad por dia [mm/dia] de
           * dias previos a una fecha es estrictamente menor al negativo
           * del doble de la capacidad de almacenamiento de agua del
           * suelo, significa que la perdida de humedad del suelo, que
           * tiene un cultivo sembrado, de los dias previos a una fecha
           * es estrictamente mayor al doble de la capacidad de almacenamiento
           * de agua del suelo. En esta situacion el cultivo esta muerto
           * y el registro de plantacion en desarrollo adquiere el estado
           * "Muerto". Todo esto se debe a que un acumulado del deficit
           * de agua por dia [mm/dia] negativo representa que en un
           * conjunto de dias hubo perdida de humedad en el suelo.
           * 
           * Las raices de un cultivo pueden crecer mas alla de la
           * capacidad de almacenamiento de agua del suelo [mm], con
           * lo cual un cultivo puede absorber el agua que hay en el
           * punto de marchitez permanente del suelo (*) y la que hay
           * debajo de este punto. Las raices de un cultivo no crecen
           * mas alla del doble de la capacidad de almacenamiento de
           * agua del suelo [mm], con lo cual un cultivo no puede
           * absorber el agua que hay debajo de este punto. Por lo tanto,
           * si el nivel de humedad del suelo, en el que esta sembrado
           * un cultivo, es estrictamente menor al doble de la capacidad
           * de almacenamiento de agua del mismo, el cultivo no puede
           * absorber el agua que hay debajo de este punto, lo cual
           * produce la muerte del cultivo, ya que ningun cultivo puede
           * sobrevivir con una perdida de humedad del suelo estrictamente
           * mayor al doble de la capacidad de almacenamiento de agua
           * del suelo.
           * 
           * (*) La capacidad de almacenamiento de agua del suelo:
           * - es en funcion de la profundidad radicular de un cultivo y
           * de otros datos, ya que esta dada por la lamina total de agua
           * disponible (dt) [mm], y
           * - tiene dos extremos: capacidad de campo (extremo superior)
           * y punto de marchitez permanente (extremo inferior).
           * 
           * El motivo por el cual se coloca el signo negativo al doble
           * de la capacidad de almacenamiento de agua del suelo es que
           * el acumulado del deficit de humedad por dia [mm/dia] es menor
           * o igual a cero.
           */
          if (accumulatedSoilMoistureDeficitPerDay < -(2 * totalAmountWaterAvailable)) {
            stringAccumulatedSoilMoistureDeficitPerDay = notCalculated;
            plantingRecordService.setDeathDate(developingPlantingRecord.getId(), pastDate);
            plantingRecordService.setStatus(developingPlantingRecord.getId(), deadStatus);
          }

        } // End if

      } else {
        stringAccumulatedSoilMoistureDeficitPerDay = notCalculated;
      }

      /*
       * Si el balance hidrico de suelo de una parcela y una
       * fecha NO existe en la base de datos subyacente, se lo
       * crea y persiste. En caso contrario, se lo actualiza.
       */
      if (!soilWaterBalanceService.checkExistence(parcelId, pastDate)) {
        soilWaterBalanceDate = Calendar.getInstance();
        soilWaterBalanceDate.set(Calendar.YEAR, pastDate.get(Calendar.YEAR));
        soilWaterBalanceDate.set(Calendar.MONTH, pastDate.get(Calendar.MONTH));
        soilWaterBalanceDate.set(Calendar.DAY_OF_MONTH, pastDate.get(Calendar.DAY_OF_MONTH));

        soilWaterBalance = new SoilWaterBalance();
        soilWaterBalance.setDate(soilWaterBalanceDate);
        soilWaterBalance.setParcelName(parcel.getName());
        soilWaterBalance.setCropName(crop.getName());
        soilWaterBalance.setWaterProvidedPerDay(waterProvidedPerDay);
        soilWaterBalance.setSoilMoistureLossPerDay(soilMoistureLossPerDay);
        soilWaterBalance.setSoilMoistureDeficitPerDay(soilMoistureDeficitPerDay);
        soilWaterBalance.setAccumulatedSoilMoistureDeficitPerDay(stringAccumulatedSoilMoistureDeficitPerDay);

        /*
         * Persistencia del balance hidrico
         */
        soilWaterBalance = soilWaterBalanceService.create(soilWaterBalance);

        /*
         * Se debe invocar el metodo merge() de la clase ParcelServiceBean
         * para persistir los elementos que se hayan agregado a
         * la coleccion soilWaterBalances de una parcela. De lo
         * contrario, la base de datos subyacente quedara en un
         * estado inconsistente.
         */
        parcel.getSoilWaterBalances().add(soilWaterBalance);
        parcelService.merge(parcel);
      } else {
        soilWaterBalance = soilWaterBalanceService.find(parcelId, pastDate);
        soilWaterBalanceService.update(soilWaterBalance.getId(), crop.getName(), soilMoistureLossPerDay,
            waterProvidedPerDay, soilMoistureDeficitPerDay, stringAccumulatedSoilMoistureDeficitPerDay);
      }

      /*
       * Suma un uno al numero de dia en el año de una fecha
       * pasada dada para obtener el siguiente registro climatico
       * correspondiente a una fecha pasada
       */
      pastDate.set(Calendar.DAY_OF_YEAR, pastDate.get(Calendar.DAY_OF_YEAR) + 1);
    } // End for

  }

  /**
   * Persiste el balance hidrico de suelo de una parcela,
   * que tiene un cultivo sembrado, correspondiente a la
   * fecha de siembra de un cultivo, si NO existe en la
   * base de datos subyacente. En caso contrario, lo
   * modifica.
   * 
   * El balance hidrico de suelo de la fecha de siembra
   * tiene todos sus valores numericos en 0, ya que en
   * el dia de la fecha de siembra de un cultivo, un
   * suelo deberia estar en capacidad de campo, esto
   * es que esta lleno de agua, pero no anegado.
   * 
   * @param developingPlantingRecord
   */
  private void persistSoilWaterBalanceSeedDate(PlantingRecord developingPlantingRecord) {
    Parcel parcel = developingPlantingRecord.getParcel();
    Calendar seedDate = developingPlantingRecord.getSeedDate();
    SoilWaterBalance soilWaterBalance = null;

    if (!soilWaterBalanceService.checkExistence(parcel.getId(), seedDate)) {
      soilWaterBalance = new SoilWaterBalance();
      soilWaterBalance.setDate(seedDate);
      soilWaterBalance.setParcelName(parcel.getName());
      soilWaterBalance.setCropName(developingPlantingRecord.getCrop().getName());
      soilWaterBalance.setWaterProvidedPerDay(0);
      soilWaterBalance.setSoilMoistureLossPerDay(0);
      soilWaterBalance.setSoilMoistureDeficitPerDay(0);
      soilWaterBalance.setAccumulatedSoilMoistureDeficitPerDay(String.valueOf(0));

      /*
       * Persistencia del balance hidrico
       */
      soilWaterBalance = soilWaterBalanceService.create(soilWaterBalance);

      /*
       * Se debe invocar el metodo merge() de la clase ParcelServiceBean
       * para persistir los elementos que se hayan agregado a
       * la coleccion soilWaterBalances de una parcela. De lo
       * contrario, la base de datos subyacente quedara en un
       * estado inconsistente.
       */
      parcel.getSoilWaterBalances().add(soilWaterBalance);
      parcelService.merge(parcel);
    } else {
      soilWaterBalance = soilWaterBalanceService.find(parcel.getId(), seedDate);
      soilWaterBalance.setParcelName(parcel.getName());
      soilWaterBalance.setCropName(developingPlantingRecord.getCrop().getName());
      soilWaterBalance.setWaterProvidedPerDay(0);
      soilWaterBalance.setSoilMoistureLossPerDay(0);
      soilWaterBalance.setSoilMoistureDeficitPerDay(0);
      soilWaterBalance.setAccumulatedSoilMoistureDeficitPerDay(String.valueOf(0));

      /*
       * Realiza las modificaciones del balance hidrico
       * de suelo de la fecha de siembra de un cultivo
       */
      soilWaterBalanceService.modify(parcel.getId(), seedDate, soilWaterBalance);
    }

  }

  /**
   * Calcula la ETo (evapotranspiracion del cultivo de referencia)
   * con los datos meteorologicos de una fecha dada, la cual esta
   * determinada por un registro climatico, ya que un registro
   * climatico tiene fecha
   * 
   * @param givenClimateRecord
   * @return double que representa la ETo (evapotranspiracion del
   * cultivo de referencia) calculada en una fecha con los datos
   * meteorologicos de un registro climatico que tiene una fecha
   * y pertenece a una parcela
   */
  private double calculateEtoForClimateRecord(ClimateRecord givenClimateRecord) {
    Parcel parcel = givenClimateRecord.getParcel();
    double extraterrestrialSolarRadiation = solarService.getRadiation(parcel.getGeographicLocation().getLatitude(),
        monthService.getMonth(givenClimateRecord.getDate().get(Calendar.MONTH)), latitudeService.find(parcel.getGeographicLocation().getLatitude()),
        latitudeService.findPreviousLatitude(parcel.getGeographicLocation().getLatitude()),
        latitudeService.findNextLatitude(parcel.getGeographicLocation().getLatitude()));

    /*
     * Calculo de la evapotranspiracion del cultivo de
     * referencia (ETo) de una fecha, la cual esta
     * determinada por un registro climatico, ya que
     * un registro climatico tiene fecha
     */
    return HargreavesEto.calculateEto(givenClimateRecord.getMaximumTemperature(), givenClimateRecord.getMinimumTemperature(), extraterrestrialSolarRadiation);
  }

  /**
   * Hay que tener en cuenta que este metodo calcula la ETc
   * de un cultivo para una fecha dada, ya que la ETo es de
   * una fecha dada. Si la ETo es de la fecha X, la ETc
   * calculada sera de la fecha X.
   * 
   * @param givenEto
   * @param givenPlantingRecord
   * @param dateUntil
   * @return double que representa la ETc (evapotranspiracion
   * del cultivo bajo condiciones estandar) de un cultivo
   * calculada con la ETo de una fecha dada, por lo tanto,
   * calcula la ETc de un cultivo que estuvo en desarollo
   * en una fecha dada
   */
  private double calculateEtcForClimateRecord(double givenEto, PlantingRecord givenPlantingRecord, Calendar dateUntil) {
    return Etc.calculateEtc(givenEto, cropService.getKc(givenPlantingRecord.getCrop(), givenPlantingRecord.getSeedDate(), dateUntil));
  }

  /**
   * @param plantingRecord
   * @return refencia a un objeto de tipo SoilMoistureLevelGraph
   * que representa el grafico de la evolucion diaria del nivel
   * de humedad del suelo de una parcela que tiene la bandera
   * suelo activa en sus opciones, la cual tiene un registro de
   * plantacion en desarrollo optimo, en desarrollo en riesgo de
   * marchitez, en desarrollo en marchitez o muerto
   */
  private SoilMoistureLevelGraph generateSoilMoistureLevelGraph(PlantingRecord plantingRecord) {
    PlantingRecordStatus status = plantingRecord.getStatus();
    Calendar seedDate = plantingRecord.getSeedDate();
    SoilMoistureLevelGraph soilMoistureLevelGraph = new SoilMoistureLevelGraph();

    /*
     * Para generar el grafico de la evolucion diaria del nivel
     * de humedad del suelo se requiere que haya como minimo una
     * diferencia de dos dias entre la fecha de siembra de un
     * registro de plantacion que tiene el estado "Finalizado",
     * el estado "Desarrollo optimo", el estado "Desarrollo en
     * riesgo de marchitez" o el estado "Desarrollo en marchitez",
     * y la fecha actual (es decir, hoy).
     * 
     * Si la fecha de siembra de un registro de plantacion que
     * tiene uno de los estados mencionados es:
     * - igual a la fecha actual, la diferencia de dias que hay
     * entre ambas es 0.
     * - es inmediatamente anterior a la fecha actual, la diferencia
     * de dias entre ambas es 1.
     * 
     * En estos dos casos no hay grafico de la evolucion diaria
     * del nivel de humedad del suelo que generar, ya que al
     * partirse desde la condicion de suelo a capacidad de campo
     * (suelo lleno de agua o en su maxima capacidad de almacenamiento
     * de agua, pero no anegado) en la fecha de siembra de un
     * cultivo y al ser la fecha de siembra igual o inmediatamente
     * anterior a la fecha actual, no hay una perdida de humedad
     * del suelo que mostrar. Esto se debe a que para que haya
     * perdida de humedad en el suelo partiendo desde la condicion
     * de suelo a capacidad de campo en la fecha de siembra de
     * un cultivo, debe haber como minimo dos de diferencia
     * entre dicha fecha y la fecha actual.
     * 
     * En estos dos casos, ademas de no generarse el grafico
     * de la evolucion diaria del nivel de humedad del suelo,
     * tampoco se lo de debe mostrar en la interfaz grafica
     * de usuario. La forma en la que se logra esto es
     * retornando una referencia a un objeto de tipo
     * SoilMoistureLevelGraph con su variable de instancia
     * showGraph en false. En Java las variables de instancia
     * de tipo primitivo de tipo por referencia se inicializan
     * de forma automatica con un valor por defecto. En el
     * caso de las variables de instancia de tipo boolean
     * (tipo primitivo), estas se inicializan de manera
     * automatica con el valor false.
     * 
     * En el controller PlantingRecordCtrl.js de la ruta
     * app/public/controllers se utiliza la variable showGraph
     * para mostrar u ocultar el grafico de la evolucion
     * diaria del nivel de humedad del suelo.
     */
    if (UtilDate.calculateDifferenceBetweenDates(seedDate, UtilDate.getCurrentDate()) < MINIMUM_DAYS_BETWEEN_SEED_DATE_AND_CURRENT_DATE
        && (statusService.equals(status, statusService.findFinishedStatus())
            || statusService.equals(status, statusService.findOptimalDevelopmentStatus())
            || statusService.equals(status, statusService.findDevelopmentAtRiskWiltingStatus())
            || statusService.equals(status, statusService.findDevelopmentInWiltingStatus()))) {
      return soilMoistureLevelGraph;
    }

    Crop crop = plantingRecord.getCrop();
    Soil soil = plantingRecord.getParcel().getSoil();
    Parcel parcel = plantingRecord.getParcel();
    String meaningXySoilMoistureLevelGraphTitle = ", Y: Nivel de humedad del suelo [mm], X: Día";

    int parcelId = parcel.getId();

    soilMoistureLevelGraph.setTotalAmountWaterAvailable(WaterMath.calculateTotalAmountWaterAvailable(crop, soil));
    soilMoistureLevelGraph.setOptimalIrrigationLayer(WaterMath.calculateOptimalIrrigationLayer(crop, soil));
    soilMoistureLevelGraph.setNegativeTotalAmountWaterAvailable(-1 * WaterMath.calculateTotalAmountWaterAvailable(crop, soil));
    soilMoistureLevelGraph.setData(calculateDataMoistureLevelGraph(plantingRecord));
    soilMoistureLevelGraph.setLabels(getStringDatesSoilMoistureLevelGraph(plantingRecord));
    soilMoistureLevelGraph.setShowGraph(true);

    /*
     * Si la cantidad total de agua de riego de un cultivo en desarrollo
     * en la fecha actual (es decir, hoy) es igual a cero y si el registro
     * de plantacion correspondiente al grafico de la evolucion diaria del
     * nivel de humedad del suelo, tiene el estado "Desarrollo optimo",
     * "Desarrollo en riesgo de marchitez" o "Desarrollo en marchitez",
     * el titulo del grafico tiene el periodo [<fecha de siembra> -
     * <fecha inmediatamente anterior a la fecha actual (es decir, hoy)>]
     */
    if (irrigationRecordService.calculateTotalAmountCropIrrigationWaterForCurrentDate(parcelId) == 0
        && (statusService.equals(status, statusService.findOptimalDevelopmentStatus())
            || statusService.equals(status, statusService.findDevelopmentAtRiskWiltingStatus())
            || statusService.equals(status, statusService.findDevelopmentInWiltingStatus()))) {
      soilMoistureLevelGraph.setText("Evolución diaria del nivel de humedad del suelo en el período "
          + UtilDate.formatDate(seedDate) + " - "
          + UtilDate.formatDate(UtilDate.getYesterdayDate())
          + meaningXySoilMoistureLevelGraphTitle);
    }

    /*
     * Si la cantidad total de agua de riego de cultivo en desarrollo
     * en la fecha actual (es decir, hoy) es estrictamente mayor a cero
     * y si el registro de plantacion correspondiente al grafico de la
     * evolucion diaria del nivel de humedad del suelo, tiene el estado
     * "Desarrollo optimo", "Desarrollo en riesgo de marchitez" o
     * "Desarrollo en marchitez", el titulo del grafico tiene el periodo
     * [<fecha de siembra> - <fecha actual (es decir, hoy)>].
     * 
     * La fecha actual no forma parte del periodo [fecha de siembra
     * de un cultivo - fecha inmediatamente anterior a la fecha
     * actual] que la aplicacion utiliza para calcular la necesidad
     * de agua de riego de un cultivo en la fecha actual y los motivos
     * por los cuales se la agrega, junto al nivel de humedad del
     * suelo en la fecha actual si la cantidad total de agua de riego
     * de un cultivo en desarrollo en dicha fecha es estrictamente
     * mayor a cero, al grafico de la evolucion diaria del nivel de
     * humedad del suelo si se cumple la condicion mencionada, son:
     * - mostrar en dicho grafico la forma en la que aumenta el
     * nivel de humedad del suelo en la fecha actual en funcion
     * del acumulado del deficit de humedad por dia (*) y la
     * cantidad total de agua utilizada para el riego de un cultivo
     * en desarrollo dicha fecha, y
     * - mostrar mediante dicho grafico la forma en la que un
     * registro de plantacion, que tiene un estado de desarrollo
     * relacionado al uso de datos de suelo (desarrollo optimo,
     * desarrollo en riesgo de marchitez, desarrollo en marchitez),
     * cambia de estado en funcion del acumulado del deficit de
     * humedad por dia (*) y la cantidad total de agua utilizada
     * para el riego de un cultivo en desarrollo en la fecha actual.
     * 
     * (*) El acumulado del deficit de humedad por dia que se calcula
     * para determinar la necesidad de agua de riego de un cultivo
     * en la fecha actual (es decir, hoy) [mm/dia], pertenece al
     * periodo definido por la fecha de siembra de un cultivo y
     * la fecha inmediatamente anterior a la fecha actual, y puede
     * ser negativo o igual a cero. Si es negativo representa la
     * cantidad de humedad perdida que hay en dicho periodo. Si es
     * igual a cero representa que la cantidad de humedad perdida
     * en dicho periodo fue totalmente cubierta (satisfecha) mediante
     * precipitacion natural y/o artificial. Por lo tanto, si se
     * cumple este caso, en la fecha actual el nivel de humedad del
     * suelo esta en capacidad de campo, lo cual significa que el
     * suelo esta en capacidad de campo. Esto es que el suelo esta
     * lleno de agua o en su maxima capacidad de almacenamiento de
     * agua, pero no anegado.
     */
    if (irrigationRecordService.calculateTotalAmountCropIrrigationWaterForCurrentDate(parcelId) > 0 &&
        (statusService.equals(status, statusService.findOptimalDevelopmentStatus())
            || statusService.equals(status, statusService.findDevelopmentAtRiskWiltingStatus())
            || statusService.equals(status, statusService.findDevelopmentInWiltingStatus()))) {

      /*
       * La capacidad de campo [mm] se la iguala a la capacidad de
       * almacenamiento de agua del suelo (*) para determinar el
       * nivel de humedad del suelo, que tiene un cultivo sembrado,
       * con respecto a su capacidad de almacenamiento de agua del
       * suelo.
       * 
       * La capacidad de almacenamiento de agua del suelo esta
       * determinada por la lamina total de agua disponible [mm]
       * (dt).
       */
      double fieldCapacity = WaterMath.calculateTotalAmountWaterAvailable(crop, soil);
      double accumulatedSoilMoistureDeficitPerDayFromYesterday = Double.parseDouble(soilWaterBalanceService.find(parcelId, UtilDate.getYesterdayDate()).getAccumulatedSoilMoistureDeficitPerDay());
      double totalAmountCropIrrigationWaterCurrentDate = irrigationRecordService.calculateTotalAmountCropIrrigationWaterForCurrentDate(parcelId);
      double soilMoistureLevelCurrentDate = fieldCapacity + (accumulatedSoilMoistureDeficitPerDayFromYesterday + totalAmountCropIrrigationWaterCurrentDate);

      /*
       * Si el nivel de humedad del suelo en la fecha actual (es
       * decir, hoy), calculado en funcion del acumulado del deficit
       * de humedad del periodo definido por la fecha de siembra
       * de un cultivo y la fecha inmediatamente anterior a la
       * fecha actual, y la cantidad total de agua de riego de
       * un cultivo en la fecha actual, es mayor o igual a la
       * capacidad de almacenamiento de agua del suelo en el que
       * esta sembrado un cultivo, el nivel de humedad del suelo
       * esta en capacidad de campo, lo cual significa que el
       * suelo esta en capacidad de campo. Esto es que el suelo
       * esta lleno de agua o en su maxima capacidad de almacenamiento
       * de agua, pero no anegado. El agua sobrante se escurre,
       * ya que el suelo esta en capacidad de campo.
       */
      if (soilMoistureLevelCurrentDate >= fieldCapacity) {
        soilMoistureLevelCurrentDate = fieldCapacity;
      }

      soilMoistureLevelGraph.getData().add(soilMoistureLevelCurrentDate);
      soilMoistureLevelGraph.getLabels().add(UtilDate.formatDate(UtilDate.getCurrentDate()));
      soilMoistureLevelGraph.setText("Evolución diaria del nivel de humedad del suelo en el período "
          + UtilDate.formatDate(seedDate) + " - "
          + UtilDate.formatDate(UtilDate.getCurrentDate())
          + " (hoy)"
          + meaningXySoilMoistureLevelGraphTitle);
    }

    /*
     * Si el registro de plantacion correspondiente al grafico de
     * la evolucion diaria del nivel de humedad del suelo, tiene el
     * estado "Muerto", el titulo del grafico tiene el periodo
     * [<fecha de siembra> - <fecha de muerte>]
     */
    if (statusService.equals(status, statusService.findDeadStatus())) {
      soilMoistureLevelGraph.setText("Evolución diaria del nivel de humedad del suelo en el período "
          + UtilDate.formatDate(seedDate) + " - "
          + UtilDate.formatDate(plantingRecord.getDeathDate())
          + meaningXySoilMoistureLevelGraphTitle);
    }

    return soilMoistureLevelGraph;
  }

  /**
   * @param plantingRecord
   * @return referencia a un objeto de tipo Collection que contiene
   * las fechas en formato de cadena de caracteres desde una fecha
   * de siembra hasta una fecha hasta, la cual puede ser la fecha
   * inmediatamente anterior a la fecha actual (es decir, hoy) o la
   * fecha de muerte de un cultivo
   */
  private Collection<String> getStringDatesSoilMoistureLevelGraph(PlantingRecord plantingRecord) {
    Collection<SoilWaterBalance> soilWaterBalances = null;
    Collection<String> stringDates = new ArrayList<>();

    PlantingRecordStatus status = plantingRecord.getStatus();
    Calendar seedDate = plantingRecord.getSeedDate();

    int parcelId = plantingRecord.getParcel().getId();

    /*
     * Si el estado del registro de plantacion correspondiente al
     * grafico de la evolucion diaria del nivel de humedad del
     * suelo, tiene el estado "Desarrollo optimo", "Desarrollo en
     * riesgo de marchitez" o "Desarrollo en marchitez", las fechas
     * a generar para dicho grafico son las que estan comprendidas
     * en el periodo definido por una fecha fecha de siembra y la
     * fecha inmediatamente anterior a la fecha actual (es decir,
     * hoy)
     */
    if (statusService.equals(status, statusService.findOptimalDevelopmentStatus())
        || statusService.equals(status, statusService.findDevelopmentAtRiskWiltingStatus())
        || statusService.equals(status, statusService.findDevelopmentInWiltingStatus())) {
      soilWaterBalances = soilWaterBalanceService.findAllFromSeedDateUntilYesterday(parcelId, seedDate);
    }

    /*
     * Si el estado del registro de plantacion correspondiente al
     * grafico de la evolucion diaria del nivel de humedad del
     * suelo, tiene el estado "Muerto", las fechas a generar
     * para dicho grafico son las que estan comprendidas en el
     * periodo definido por una fecha de siembra y una fecha de
     * muerte
     */
    if (statusService.equals(status, statusService.findDeadStatus())) {
      soilWaterBalances = soilWaterBalanceService.findAllFromDateFromToDateUntil(parcelId, seedDate, plantingRecord.getDeathDate());
    }

    for (SoilWaterBalance currentSoilWaterBalance : soilWaterBalances) {
      stringDates.add(UtilDate.formatDate(currentSoilWaterBalance.getDate()));
    }

    return stringDates;
  }

  /**
   * @param plantingRecord
   * @return referencia a un objeto de tipo Collection que
   * contiene los valores que representan el nivel de humedad
   * diario del suelo de una parcela que tiene un cultivo
   * en proceso de desarrollo o muerto
   */
  private Collection<Double> calculateDataMoistureLevelGraph(PlantingRecord plantingRecord) {
    /*
     * La capacidad de campo se iguala a la capacidad de
     * almacenamiento de agua del suelo [mm] para realizar
     * las comparaciones del nivel de humedad del suelo con
     * con respecto a la capacidad de almacenamiento de agua
     * del suelo [mm] (determinado por la lamina total de agua
     * disponible, dt), el umbral de riego (determinado por la
     * lamina de riego optima, drop), el punto de marchitez
     * permanente (0 [mm]) y el doble de la capacidad de
     * almacenamiento de agua del suelo [mm]
     */
    double fieldCapacity = WaterMath.calculateTotalAmountWaterAvailable(plantingRecord.getCrop(), plantingRecord.getParcel().getSoil());
    double permanentWiltingPoint = 0.0;
    double soilMoistureLevel = 0.0;
    int parcelId = plantingRecord.getParcel().getId();

    /*
     * El valor de esta constante se utiliza para representar
     * la situacion en la que NO se calcula el acumulado del
     * deficit de humedad por dia de un balance hidrico de suelo
     * de una parcela que tiene un cultivo sembrado y en
     * desarrollo. Esta situacion ocurre cuando la perdida de
     * humedad del suelo de un conjunto de dias es estrictamente
     * mayor al doble de la capacidad de almacenamiento de agua
     * del suelo. Esto se representa mediante la condicion de
     * que el acumulado del deficit de humedad por dia sea estrictamente
     * menor al negativo del doble de la capacidad de almacenamiento
     * de agua del suelo, ya que el acumulado del deficit de
     * agua por dia puede ser negativo o cero. Cuando es negativo
     * representa que en un periodo de dias hubo perdida de humedad
     * en el suelo. En cambio, cuando es igual a cero representa
     * que la perdida de humedad que hubo en el suelo en un periodo
     * de dias esta totalmente cubierta. Esto es que el suelo
     * esta en capacidad de campo, lo significa que el suelo
     * esta lleno de agua o en su maxima capacidad de almacenamiento
     * de agua, pero no anegado.
     * 
     * Cuando la perdida de humedad del suelo, que tiene un
     * cultivo sembrado, de un conjunto de dias es estrictamente
     * mayor al doble de la capacidad de almacenamiento de agua
     * del suelo (representado mediante la conidicion de que el
     * acumulado del deficit de humedad por dia sea estrictamente
     * menor al negativo del doble de la capacidad de almacenamiento
     * de agua del suelo), el cultivo esta muerto, ya que ningun
     * cultivo puede sobrevivir con dicha perdida de humedad.
     * Por lo tanto, la presencia del valor "NC" (no calculado)
     * tambien representa la muerte de un cultivo.
     */
    String notCalculated = soilWaterBalanceService.getNotCalculated();
    String accumulatedSoilMoistureDeficitPerDay = null;
    Calendar yesterdayDateFromDate = null;
    SoilWaterBalance previousSoilWaterBalance = null;
    PlantingRecordStatus status = plantingRecord.getStatus();
    Calendar seedDate = plantingRecord.getSeedDate();

    Collection<SoilWaterBalance> soilWaterBalances = null;
    Collection<Double> data = new ArrayList<>();

    /*
     * Si el estado del registro de plantacion correspondiente al
     * grafico de la evolucion diaria del nivel de humedad del
     * suelo, tiene el estado "Desarrollo optimo", "Desarrollo en
     * riesgo de marchitez" o "Desarrollo en marchitez", las fechas
     * a generar para dicho grafico son las que estan comprendidas
     * en el periodo definido por una fecha fecha de siembra y la
     * fecha inmediatamente anterior a la fecha actual (es decir,
     * hoy)
     */
    if (statusService.equals(status, statusService.findOptimalDevelopmentStatus())
        || statusService.equals(status, statusService.findDevelopmentAtRiskWiltingStatus())
        || statusService.equals(status, statusService.findDevelopmentInWiltingStatus())) {
      soilWaterBalances = soilWaterBalanceService.findAllFromSeedDateUntilYesterday(parcelId, seedDate);
    }

    /*
     * Si el estado del registro de plantacion correspondiente al
     * grafico de la evolucion diaria del nivel de humedad del
     * suelo, tiene el estado "Muerto", las fechas a generar
     * para dicho grafico son las que estan comprendidas en el
     * periodo definido por una fecha de siembra y una fecha de
     * muerte
     */
    if (statusService.equals(status, statusService.findDeadStatus())) {
      soilWaterBalances = soilWaterBalanceService.findAllFromDateFromToDateUntil(parcelId, seedDate, plantingRecord.getDeathDate());
    }

    for (SoilWaterBalance currentSoilWaterBalance : soilWaterBalances) {
      accumulatedSoilMoistureDeficitPerDay = currentSoilWaterBalance.getAccumulatedSoilMoistureDeficitPerDay();

      /*
       * Si el acumulado del deficit de humedad por dia de una fecha
       * no es "NC" (No Calculado), significa que el cultivo para
       * el que se calcula la necesidad de agua de riego en la fecha
       * actual no murio en la fecha dada. Por lo tanto, se calcula
       * el nivel de humedad que tuvo el suelo en dicha fecha.
       */
      if (!accumulatedSoilMoistureDeficitPerDay.equals(notCalculated)) {
        soilMoistureLevel = fieldCapacity + Double.parseDouble(currentSoilWaterBalance.getAccumulatedSoilMoistureDeficitPerDay());
        data.add(soilMoistureLevel);
      }

      /*
       * Si el acumulado del deficit de humedad por dia de una fecha
       * es "NC" (No Calculado), significa que el cultivo para el
       * que se calcula la necesidad de agua de riego en la fecha
       * actual murio en la fecha dada. Lo que se hace es calcular
       * el nivel de humedad que tuvo el suelo en dicha fecha para
       * representar en el grafico que el nivel de humedad del suelo
       * esta por debajo del doble de la capacidad de almacenamiento
       * de agua del suelo. Ningun cultivo puede sobrevivir con una
       * perdida de humedad del suelo estrictamente mayor al doble
       * de la capacidad de almacenamiento de agua del suelo. En esta
       * situacion un cultivo muere. Esto se representa mediante el
       * valor "NC".
       */
      if (accumulatedSoilMoistureDeficitPerDay.equals(notCalculated)) {
        yesterdayDateFromDate = UtilDate.getYesterdayDateFromDate(currentSoilWaterBalance.getDate());
        previousSoilWaterBalance = soilWaterBalanceService.find(plantingRecord.getParcel().getId(), yesterdayDateFromDate);
        soilMoistureLevel = fieldCapacity + (Double.parseDouble(previousSoilWaterBalance.getAccumulatedSoilMoistureDeficitPerDay())
                + currentSoilWaterBalance.getSoilMoistureDeficitPerDay());
        data.add(soilMoistureLevel);
        break;
      }

    }

    return data;
  }

}
