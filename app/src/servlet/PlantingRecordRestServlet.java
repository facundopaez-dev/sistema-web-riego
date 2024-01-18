package servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Calendar;
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
import stateless.MonthServiceBean;
import stateless.UserServiceBean;
import stateless.OptionServiceBean;
import stateless.LatitudeServiceBean;
import stateless.SessionServiceBean;
import climate.ClimateClient;
import et.HargreavesEto;
import et.Etc;
import irrigation.WaterNeedWos;
import irrigation.WaterNeedWs;
import irrigation.WaterMath;
import model.ClimateRecord;
import model.Crop;
import model.IrrigationRecord;
import model.Parcel;
import model.PasswordResetFormData;
import model.PlantingRecord;
import model.PlantingRecordStatus;
import model.SoilWaterBalance;
import model.IrrigationWaterNeedFormData;
import model.User;
import model.Option;
import util.ErrorResponse;
import util.PersonalizedResponse;
import util.ReasonError;
import util.RequestManager;
import util.SourceUnsatisfiedResponse;
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

  // Mapea lista de pojo a JSON
  ObjectMapper mapper = new ObjectMapper();

  private final int UNAUTHORIZED = 401;
  private final int TOO_MANY_REQUESTS = 429;
  private final int SERVICE_UNAVAILABLE = 503;
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

    /*
     * Si el valor del encabezado de autorizacion de la peticion HTTP
     * dada, tiene un JWT valido, la aplicacion del lado servidor
     * devuelve el mensaje HTTP 200 (Ok) junto con los datos solicitados
     * por el cliente
     */
    return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(plantingRecordService.findByUserId(userId, plantingRecordId))).build();
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
     * Se establece el estado del nuevo registro de plantacion
     * en base a la fecha de siembra y la fecha de cosecha de
     * su cultivo
     */
    newPlantingRecord.setStatus(statusService.calculateStatus(newPlantingRecord));

    PlantingRecordStatus statusNewPlantingRecord = newPlantingRecord.getStatus();
    PlantingRecordStatus finishedStatus = statusService.findFinishedStatus();
    PlantingRecordStatus waitingStatus = statusService.findWaitingStatus();
    PlantingRecordStatus inDevelopmentStatus = statusService.findInDevelopmentStatus();
    PlantingRecordStatus optimalDevelopmentStatus = statusService.findOptimalDevelopmentStatus();

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
     * Inicialmente un registro de plantacion que tiene el estado
     * "En desarrollo" o el estado "Desarrollo optimo" NO tiene la
     * necesidad de agua de riego de un cultivo en la fecha actual
     * [mm/dia], lo cual se realiza para hacer que el usuario ejecute
     * el proceso del calculo de la necesidad de agua de riego de
     * un cultivo en la fecha actual [mm/dia]. La manera en la que
     * el usuario realiza esto es mediante el boton "Calcular" de
     * la pagina de registros de plantacion.
     */
    if (statusService.equals(statusNewPlantingRecord, inDevelopmentStatus)
        || statusService.equals(statusNewPlantingRecord, optimalDevelopmentStatus)) {
      newPlantingRecord.setCropIrrigationWaterNeed(cropIrrigationWaterNeedNotAvailableButCalculable);
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
     * El estado actual del registro de plantacion a modificar
     * puede cambiar dependiendo de:
     * - si es "Muerto" y el usuario NO se desea que el registro
     * mantenga este estado.
     * - si NO es "Muerto" y si la fecha de siembra o la fecha
     * de cosecha del registro es modificada o si ambas son
     * modificadas.
     */
    PlantingRecordStatus modifiedPlantingRecordStatus = currentStatusModifiedPlantingRecord;

    /*
     * Si un registro de plantacion a modificar tiene el estado
     * muerto y NO se desea que mantenga ese estado luego
     * de su modificacion, se establece su proximo estado. El
     * estado de un registro de plantacion se calcula con base
     * en la fecha de siembra y la fecha de cosecha.
     */
    if (statusService.equals(currentStatusModifiedPlantingRecord, deadStatus) && !maintainDeadStatus) {
      modifiedPlantingRecordStatus = statusService.calculateStatus(modifiedPlantingRecord);
      plantingRecordService.unsetDeathDate(plantingRecordId);
    }

    /*
     * Si el registro de plantacion a modificar NO tiene el
     * estado "Muerto" y su fecha de siembra o su fecha de
     * cosecha es modificada o si ambas son modificadas, se
     * calcula y asigna su proximo estado. El estado de un
     * registro de plantacion se calcula con base en su fecha
     * de siembra y su fecha de cosecha.
     * 
     * El estado de un registro de plantacion a modificar,
     * que NO tiene el estado "Muerto", debe ser calculado
     * y modificado si una de sus fechas es modificada o
     * si sus dos fechas son modificadas.
     */
    if (!statusService.equals(currentStatusModifiedPlantingRecord, deadStatus)
        && (UtilDate.compareTo(modifiedSeedDate, currentSeedDate) != 0
            || UtilDate.compareTo(modifiedHarvestDate, currentHarvestDate) != 0)) {
      modifiedPlantingRecordStatus = statusService.calculateStatus(modifiedPlantingRecord);
    }

    /*
     * Asignacion del posible nuevo estado del registro de
     * plantacion a modificar. En caso de que el estado no
     * sea nuevo, al registro de plantacion a modificar
     * se le asigna el estado que tenia antes de la
     * peticion de modificacion.
     */
    modifiedPlantingRecord.setStatus(modifiedPlantingRecordStatus);

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
     * ningna utilidad tener tales datos.
     */
    if (statusService.equals(modifiedPlantingRecordStatus, finishedStatus) || (statusService.equals(modifiedPlantingRecordStatus, waitingStatus))) {
      modifiedPlantingRecord.setCropIrrigationWaterNeed(notAvailable);
      plantingRecordService.updateTotalAmountWaterAvailable(plantingRecordId, 0);
      plantingRecordService.updateOptimalIrrigationLayer(plantingRecordId, 0);
    }

    /*
     * Si el registro de plantacion modificado tiene el estado
     * "En desarrollo" o el estado "Desarrollo optimo" (*) y
     * tiene una parcela o un cultivo distinto a los originales,
     * se asigna el caracter "-" a la necesidad de agua de riego
     * de dicho registro para hacer que el usuario ejecute el
     * proceso del calculo de la necesidad de agua de riego de
     * un cultivo en la fecha actual [mm/dia]. La manera en la
     * que el usuario realiza esto es mediante el boton "Calcular"
     * de la pagina de registros de plantacion. Tambien se asigna
     * el caracter "-" a la necesidad de agua de riego de un
     * registro de plantacion en desarrollo perteneciente a una
     * parcela a la que se le modifica el suelo. Esto esta
     * programado en el metodo modify de la clase ParcelRestServlet.
     * 
     * El simbolo "-" (guion) se utiliza para representar que
     * la necesidad de agua de riego de un cultivo en la fecha
     * actual [mm/dia] no esta disponible, pero es calculable.
     * Esta situacion ocurre unicamente para un registro de
     * plantacion en desarrollo.
     * 
     * La lamina total de agua disponible (dt) [mm] y la lamina
     * de riego optima (drop) [mm] estan en funcion de un suelo
     * y un cultivo. Una parcela tiene un suelo y un registro de
     * plantacion tiene una parcela y un cultivo. Por lo tanto,
     * si se modifica la parcela y/o el cultivo de un registro
     * de plantacion en desarrollo, se establece el valor 0 en
     * las laminas de dicho registro.
     * 
     * La lamina total de agua disponible (dt) representa la
     * capacidad de almacenamiento de agua que tiene un suelo para
     * el cultivo que tiene sembrado. La lamina de riego optima (drop)
     * representa la cantidad maxima de agua que puede perder un
     * suelo, que tiene un cultivo sembrado, a partir de la cual
     * no conviene que pierda mas agua, sino que se le debe añadir
     * agua hasta llevar su nivel de humedad a capacidad de campo.
     * Capacidad de campo es la capacidad de almacenamiento de agua
     * que tiene un suelo. Un suelo que esta en capacidad de campo
     * es un suelo lleno de agua, pero no anegado. El motivo por
     * el cual se habla de llevar el nivel de humedad del suelo,
     * que tiene un cultivo sembrado, a capacidad de campo es
     * que el objetivo de la aplicacion es informar al usuario
     * la cantidad de agua que debe reponer en la fecha actual
     * (es decir, hoy) para llevar el nivel de humedad del suelo,
     * en el que tiene un cultivo sembrado, a capacidad de campo.
     * Esto es la cantidad de agua de riego [mm] que debe usar
     * el usuario para llenar el suelo en el que tiene un cultivo
     * sembrado, pero sin anegarlo.
     * 
     * (*) El que un registro de plantacion tenga el estado
     * "En desarrollo" o el estado "Desarrollo optimo" depende
     * del valor de la bandera suelo de las opciones de la parcela
     * a la que pertenece. Si un registro de plantacion tiene
     * una fecha de siembra y una fecha de cosecha de tal manera
     * que la fecha actual (es decir, hoy) esta dentro del periodo
     * definido por ambas y la bandera suelo de las opciones de
     * la parcela, a la que pertenece, NO esta activa, su estado
     * sera "En desarrollo". En cambio, si esta activa su estado
     * sera "Desarrollo optimo".
     */
    if ((statusService.equals(modifiedPlantingRecordStatus, inDevelopmentStatus)
        || statusService.equals(modifiedPlantingRecordStatus, optimalDevelopmentStatus))
        && (!parcelService.equals(modifiedParcel, currentParcel) || !cropService.equals(modifiedCrop, currentCrop))) {
      modifiedPlantingRecord.setCropIrrigationWaterNeed(cropIrrigationWaterNeedNotAvailableButCalculable);
      plantingRecordService.updateTotalAmountWaterAvailable(plantingRecordId, 0);
      plantingRecordService.updateOptimalIrrigationLayer(plantingRecordId, 0);
    }

    /*
     * Si el estado actual del registro de plantacion modificado
     * es distinto del nuevo estado y este es "En desarrollo" o
     * "Desarrollo optimo" (*), se asigna el caracter "-" a la
     * necesidad de agua de riego de dicho registro por los
     * siguientes dos motivos. Primero porque calcular la necesidad
     * de agua de riego de un cultivo en la fecha actual (es decir,
     * hoy) utilizando datos de suelo hace que dicho calculo este
     * en funcion del suelo. Por lo tanto, si cambia el suelo se
     * debe realizar el calculo de la necesidad de agua de riego
     * de un cultivo en la fecha actual en funcion del nuevo suelo.
     * Segundo para hacer que el usuario ejecute el proceso del
     * calculo de la necesidad de agua de riego de un cultivo en
     * la fecha actual (es decir, hoy) [mm/dia]. La manera en la
     * que el usuario realiza esto es mediante el boton "Calcular"
     * de la pagina de registros de plantacion. Tambien se asigna
     * el caracter "-" a la necesidad de agua de riego de un
     * cultivo en la fecha actual de un registro de plantacion
     * en desarrollo perteneciente a una parcela a la que se le
     * modifica el suelo. Esto esta programado en el metodo modify()
     * de la clase ParcelRestServlet.
     * 
     * Este control es para el caso en el que se modifica un
     * registro de plantacion que tiene la parcela y el cultivo
     * originales y que originalmente NO tiene un estado de
     * desarrollo, pero lo adquiere al calcular su proximo
     * estado. En esta situacion se asigna el caracter "-"
     * a la necesidad de agua de riego de un registro de
     * plantacion en desarrollo, ya que un registro de
     * plantacion en dicho estado NO tiene la necesidad de
     * agua de riego calculada.
     * 
     * El simbolo "-" (guion) se utiliza para representar que
     * la necesidad de agua de riego de un cultivo en la fecha
     * actual [mm/dia] no esta disponible, pero es calculable.
     * Esta situacion ocurre unicamente para un registro de
     * plantacion en desarrollo.
     * 
     * (*) El que un registro de plantacion tenga el estado
     * "En desarrollo" o el estado "Desarrollo optimo" depende
     * del valor de la bandera suelo de las opciones de la parcela
     * a la que pertenece. Si un registro de plantacion tiene
     * una fecha de siembra y una fecha de cosecha de tal manera
     * que la fecha actual (es decir, hoy) esta dentro del periodo
     * definido por ambas y la bandera suelo de las opciones de
     * la parcela, a la que pertenece, NO esta activa, su estado
     * sera "En desarrollo". En cambio, si esta activa su estado
     * sera "Desarrollo optimo".
     */
    if (!statusService.equals(currentStatus, modifiedPlantingRecordStatus) &&
        (statusService.equals(modifiedPlantingRecordStatus, inDevelopmentStatus)
            || statusService.equals(modifiedPlantingRecordStatus, optimalDevelopmentStatus))) {
      modifiedPlantingRecord.setCropIrrigationWaterNeed(cropIrrigationWaterNeedNotAvailableButCalculable);
    }

    /*
     * Si el estado del registro de plantacion modificado es "En
     * desarrollo" o "Desarrollo optimo" (*) y la fecha de siembra
     * es modificada, se asigna el caracter "-" (guion) a la necesidad
     * de agua de riego de un cultivo en la fecha actual (es decir,
     * hoy) de un registro de plantacion en desarrollo. Esto se
     * hace para que el usuario ejecute el proceso del calculo de
     * la necesidad de agua de riego de un cultivo en la fecha
     * actual [mm/dia].
     * 
     * El simbolo "-" (guion) se utiliza para representar que
     * la necesidad de agua de riego de un cultivo en la fecha
     * actual [mm/dia] no esta disponible, pero es calculable.
     * Esta situacion ocurre unicamente para un registro de
     * plantacion en desarrollo.
     * 
     * (*) El que un registro de plantacion tenga el estado
     * "En desarrollo" o el estado "Desarrollo optimo" depende
     * del valor de la bandera suelo de las opciones de la parcela
     * a la que pertenece. Si un registro de plantacion tiene
     * una fecha de siembra y una fecha de cosecha de tal manera
     * que la fecha actual (es decir, hoy) esta dentro del periodo
     * definido por ambas y la bandera suelo de las opciones de
     * la parcela, a la que pertenece, NO esta activa, su estado
     * sera "En desarrollo". En cambio, si esta activa su estado
     * sera "Desarrollo optimo".
     */
    if ((statusService.equals(modifiedPlantingRecordStatus, inDevelopmentStatus)
        || statusService.equals(modifiedPlantingRecordStatus, optimalDevelopmentStatus))
        && UtilDate.compareTo(modifiedSeedDate, currentSeedDate) != 0) {
      modifiedPlantingRecord.setCropIrrigationWaterNeed(cropIrrigationWaterNeedNotAvailableButCalculable);
    }

    /*
     * Si inicialmente el registro de plantacion a modificar
     * tiene el estado "Finalizado" o el estado "En espera" y
     * en su modificacion adquiere el estado "Desarrollo optimo",
     * se deben calcular y asignar la lamina total de agua disponible
     * (dt) [mm] y la lamina de riego optima (drop) [mm] porque
     * la presencia de dicho estado en un registro de plantacion
     * significa que la bandera suelo de las opciones de la
     * parcela, a la que pertenece un registro de plantacion,
     * esta activa (*) (**), y, por ende, significa que el
     * usuario desea calcular la necesidad de agua de riego de
     * un cultivo en la fecha actual (es decir, hoy) utilizando
     * datos de suelo.
     * 
     * A la lamina de riego optima (drop) se le asigna el signo
     * negativo (-) para poder compararla con el acumulado del
     * deficit de agua por dia [mm/dia], el cual es negativo y
     * es calculado desde la fecha de siembra de un cultivo hasta
     * la fecha inmediatamente anterior a la fecha actual. La
     * lamina de riego optima representa la cantidad maxima de
     * agua que puede perder un suelo para el cultivo que tiene
     * sembrado, a partir de la cual NO conviene que pierda mas
     * agua, sino que se le debe añadir agua hasta llevar su
     * nivel de humedad a capacidad de campo. Capacidad de campo
     * es la capacidad de almacenamiento de agua que tiene un
     * suelo. Un suelo que esta en capacidad de campo es un
     * suelo lleno de agua, pero no anegado. El motivo por el
     * cual se habla de llevar el nivel de humedad del suelo,
     * que tiene un cultivo sembrado, a capacidad de campo es
     * que el objetivo de la aplicacion es informar al usuario
     * la cantidad de agua que debe reponer en la fecha actual
     * (es decir, hoy) para llevar el nivel de humedad del suelo,
     * en el que tiene un cultivo sembrado, a capacidad de campo.
     * Esto es la cantidad de agua de riego [mm] que debe usar
     * el usuario para llenar el suelo en el que tiene un cultivo
     * sembrado, pero sin anegarlo.
     * 
     * El suelo agricola tiene dos limites: capacidad de campo
     * (limite superior) y punto de marchitez permanente (limite
     * inferior). La lamina de riego optima tambien se la conoce
     * como umbral de riego, debido a lo que representa.
     * 
     * La lamina total de agua disponible (dt) [mm] y la lamina
     * de riego optima (drop) [mm] estan en funcion de un suelo
     * y un cultivo. La lamina total de agua disponible (dt)
     * representa la capacidad de almacenamiento de agua que
     * tiene un suelo para el cultivo que tiene sembrado. La
     * lamina de riego optima (drop) representa la cantidad
     * maxima de agua que puede perder un suelo para el cultivo
     * que tiene sembrado, a partir de la cual no conviene
     * que pierda mas agua, sino que se le debe añadir agua.
     * 
     * (*) La aplicacion tiene un control para evitar que la
     * bandera suelo, perteneciente a las opciones de una
     * parcela, sea activada para una parcela que no tiene un
     * suelo asignado. Por lo tanto, si dicha bandera esta
     * activa, la parcela correspondiente a las opciones de
     * dicha bandera, tiene un suelo asignado. Gracias a este
     * control no es necesario implementar un control con la
     * condicion != null para el suelo de una parcela. Dicho
     * control esta implementado en el metodo modify() de la
     * clase OptionRestServlet.
     * 
     * (**) Este significado se debe a que el metodo calculateStatus()
     * de la clase PlantingRecordStatusServiceBean calcula el
     * estado de un registro de plantacion teniendo en cuenta
     * la bandera suelo de las opciones de la parcela a la que
     * pertenece un registro de plantacion. Si dicha bandera
     * esta activa y la fecha actual (es decir, hoy) esta
     * dentro del periodo definido por la fecha de siembra
     * y la fecha de cosecha de un registro de plantacion,
     * este adquiere el estado "Desarrollo optimo".
     */
    if ((statusService.equals(currentStatusModifiedPlantingRecord, finishedStatus) || statusService.equals(currentStatusModifiedPlantingRecord, waitingStatus))
        && statusService.equals(modifiedPlantingRecordStatus, optimalDevelopmentStatus)) {
      plantingRecordService.updateTotalAmountWaterAvailable(plantingRecordId, WaterMath.calculateTotalAmountWaterAvailable(modifiedCrop, modifiedParcel.getSoil()));
      plantingRecordService.updateOptimalIrrigationLayer(plantingRecordId, WaterMath.calculateNegativeOptimalIrrigationLayer(modifiedCrop, modifiedParcel.getSoil()));
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

    PlantingRecordStatus statusGivenPlantingRecord = plantingRecordService.find(plantingRecordId).getStatus();
    PlantingRecordStatus finishedStatus = statusService.findFinishedStatus();

    /*
     * Si el estado del registro de plantacion a eliminar es el
     * estado "Finalizado", la aplicacion retorna el mensaje HTTP
     * 400 (Bad request) junto con el mensaje "No esta permitido
     * eliminar un registro de plantacion finalizado" y no se
     * realiza la operacion solicitada
     */
    if (statusService.equals(statusGivenPlantingRecord, finishedStatus)) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.DELETE_FINISHED_PLANTING_RECORD_NOT_ALLOWED)))
          .build();
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
     * ***********************************************************
     * A partir de aqui comienza el codigo necesario para calcular
     * la necesidad de agua de riego de un cultivo en la fecha
     * actual [mm/dia]
     * ***********************************************************
     */

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
    String stringIrrigationWaterNeedCurrentDate = null;

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

    try {
      /*
       * Ejecuta el proceso del calculo de la necesidad de agua
       * de riego de un cultivo en la fecha actual [mm/dia]. Esto
       * es que ejecuta los metodos necesarios para calcular y
       * actualizar la necesidad de agua de riego de un cultivo
       * (en desarrollo) en la fecha actual.
       */
      stringIrrigationWaterNeedCurrentDate = runCalculationIrrigationWaterNeedCurrentDateTwo(developingPlantingRecord);
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
       * 401 a la aplicacion del lado del navegador web junto con
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
        return Response.status(Response.Status.UNAUTHORIZED)
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
       * la necesidad de agua de riego de un cultivo" y no se
       * realiza la operacion solicitada
       */
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.UNKNOW_ERROR_IN_IRRIGATION_WATER_NEED_CALCULATION, SourceUnsatisfiedResponse.WATER_NEED_CROP)))
          .build();
    }

    /*
     * El valor de esta variable se utiliza para representar
     * la situacion en la que NO se calcula el acumulado del
     * deficit de agua por dia de dias previos a una fecha de
     * un balance hidrico de suelo de una parcela que tiene
     * un cultivo sembrado y en desarrollo. Esta situacion
     * ocurre cuando el nivel de humedad de un suelo, que tiene
     * un cultivo sembrado, es estrictamente menor al doble de
     * la capacidad de almacenamiento de agua del mismo.
     */
    String notCalculated = soilWaterBalanceService.getNotCalculated();

    /*
     * Si la necesidad de agua de riego de un cultivo (en
     * desarrollo) en la fecha actual [mm/dia] es "NC"
     * (valor de la variable notCalculated) significa que
     * el algoritmo utilizado para calcular dicha necesidad
     * es el que utiliza el suelo para ello, ya que al
     * utilizar este algoritmo se retorna el valor "NC"
     * para representar la situacion en la que NO se
     * calcula el acumulado del deficit de agua por dia
     * del balance hidrico de una parcela que tiene un
     * cultivo sembrado y en desarrollo. Esta situacion
     * ocurre cuando el nivel de humedad de un suelo, que
     * tiene un cultivo sembrado, es estrictamente menor
     * al doble de la capacidad de almacenamiento de agua
     * de un suelo que tiene un cultivo sembrado.
     * 
     * Por lo tanto, la aplicacion del lado servidor asigna
     * la abreviatura "n/a" (no disponible) a la necesidad de
     * agua de riego de un cultivo en la fecha actual [mm/dia]
     * de un registro de plantacion en desarrollo y no se
     * realiza la operacion solicitada.
     * 
     * En esta situacion, la aplicacion retorna el mensaje
     * HTTP 400 (Bad request) informando de que el cultivo
     * para el que se deseo calcular su necesidad de agua
     * de riego en la fecha actual [mm/dia], esta muerto.
     */
    if (stringIrrigationWaterNeedCurrentDate != null && stringIrrigationWaterNeedCurrentDate.equals(notCalculated)) {
      plantingRecordService.updateCropIrrigationWaterNeed(developingPlantingRecord.getId(), notAvailable);
      plantingRecordService.updateDateDeath(developingPlantingRecord.getId(), UtilDate.getCurrentDate());

      String message = "El cultivo murió por ser el nivel de humedad del suelo, en el que está sembrado, estrictamente menor al doble"
          + " de la capacidad de almacenamiento de agua del suelo (2 * " + developingPlantingRecord.getTotalAmountWaterAvailable()
          + " = " + (2 * developingPlantingRecord.getTotalAmountWaterAvailable())  + ")";

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
  private String runCalculationIrrigationWaterNeedCurrentDateTwo(PlantingRecord developingPlantingRecord) throws IOException {
    /*
     * Persiste los registros climaticos de la parcela de un registro
     * de plantacion en desarrollo desde la fecha de siembra hasta la
     * fecha inmediatamente anterior a la fecha actual, si NO existen
     * en la base de datos subyacente. Estos registros climaticos son
     * obtenidos del servicio meteorologico utilizado por la aplicacion.
     */
    requestPastClimateRecordsTwo(developingPlantingRecord);

    /*
     * Calcula la ETo y la ETc de los registros climaticos de la parcela
     * de un registro de plantacion en desarrollo previamente obtenidos.
     * La ETc es necesaria para calcular los balances hidricos de suelo
     * de una parcela que tiene un cultivo en desarrollo. 
     */
    calculateEtsPastClimateRecordsTwo(developingPlantingRecord);

    /*
     * Persiste el balance hidrico de la fecha de siembra de un cultivo,
     * si no existe en la base de datos subyacente. En caso contrario,
     * lo modifica. Este paso es el primer paso necesario para el
     * calculo de los balances hidricos de suelo de una parcela que
     * tiene un cultivo sembrado. Este calculo se realiza para
     * calcular la necesidad de agua de riego de un cultivo en la
     * fecha actual [mm/dia].
     * 
     * El balance hidrico de la fecha de siembra de un cultivo tiene
     * el valor 0 en todos sus atributos porque en la fecha de siembra
     * de un cultivo se parte del suelo a capacidad de campo, esto es
     * que el suelo esta lleno de agua, pero no anegado.
     */
    persistSoilWaterBalanceSeedDate(developingPlantingRecord);

    String notCalculated = soilWaterBalanceService.getNotCalculated();
    Parcel parcel = developingPlantingRecord.getParcel();
    Calendar seedDate = developingPlantingRecord.getSeedDate();

    /*
     * La necesidad de agua de riego de un cultivo en la fecha actual
     * se determina con el acumulado del deficit de agua por dia [mm/dia]
     * de la fecha inmediatamente anterior a la fecha actual. Por este
     * motivo se recupera de la base de datos subyacente el balance
     * hidrico de suelo de la parcela, que tiene tiene un cultivo
     * sembrado y en desarrollo, de la fecha inmediatamente anterior
     * a la fecha actual.
     */
    Calendar yesterday = UtilDate.getYesterdayDate();
    Calendar currentDate = UtilDate.getCurrentDate();

    /*
     * Si la fecha de siembra de un cultivo es igual a la fecha actual
     * (es decir, hoy), la necesidad de agua de riego de un cultivo
     * en la fecha actual [mm/dia] es el acumulado del deficit de agua
     * por dia [mm/dia] de la fecha actual
     */
    if (UtilDate.compareTo(seedDate, currentDate) == 0) {
      return soilWaterBalanceService.find(parcel.getId(), currentDate).getAccumulatedWaterDeficitPerDay();
    }

    /*
     * Si la fecha de siembra de un cultivo es igual a la fecha
     * inmediatamente anterior a la fecha actual (es decir, hoy),
     * la necesidad de agua de riego de un cultivo en la fecha
     * actual [mm/dia] es el acumulado del deficit de agua por dia
     * [mm/dia] de la fecha inmediatamente anterior a la fecha
     * actual
     */
    if (UtilDate.compareTo(seedDate, yesterday) == 0) {
      return soilWaterBalanceService.find(parcel.getId(), yesterday).getAccumulatedWaterDeficitPerDay();
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
     * del deficit de agua por dia [mm/dia] del dia inmediatamente
     * anterior a la fecha actual
     */
    String stringAccumulatedWaterDeficitPerDay = soilWaterBalanceService.find(parcel.getId(), yesterday).getAccumulatedWaterDeficitPerDay();

    /*
     * Si el valor del acumulado del deficit de agua por dia [mm/dia]
     * de ayer es "NC" (no calculado), significa dos cosas:
     * - que el algoritmo utilizado para calcular la necesidad de
     * agua de riego de un cultivo en la fecha actual [mm/dia] es
     * el que utiliza el suelo para ello,
     * - y que el nivel de humedad del suelo, que tiene un cultivo
     * sembrado, es estrictamente menor al doble de la capacidad de
     * almacenamiento de agua del suelo.
     * 
     * Por lo tanto, se retorna "NC" para indicar que la necesidad de
     * agua de riego de un cultivo en la fecha actual [mm/dia] no se
     * calculo, ya que el cultivo esta muerto debido a que el nivel
     * de humedad del suelo, en el que esta sembrado, es estrictamente
     * menor al doble de la capacidad de almacenamiento de agua
     * del suelo.
     */
    if (stringAccumulatedWaterDeficitPerDay.equals(notCalculated)) {
      return notCalculated;
    }

    double totalIrrigationWaterCurrentDate = irrigationRecordService.calculateTotalIrrigationWaterCurrentDate(parcel.getId());
    double accumulatedWaterDeficitPerDay = Double.parseDouble(stringAccumulatedWaterDeficitPerDay);

    /*
     * Calculo de la necesidad de agua de riego de un cultivo
     * en la fecha actual [mm/dia]. El motivo por el cual este
     * calculo corresponde a la fecha actual es que la cantidad
     * total de agua de riego de un cultivo [mm/dia] es de la
     * fecha actual y el acumulado del deficit de agua por dia
     * [mm] es del dia inmediatamente anterior a la fecha actual
     * (es decir, hoy). En cambio, si en este metodo se utiliza
     * la cantidad total de agua de riego de ayer y el acumulado
     * del deficit de agua por dia de antes de ayer, la necesidad
     * de agua de riego de un cultivo calculada es de ayer. Por
     * lo tanto, lo que determina la fecha de la necesidad de agua
     * de riego de un cultivo es la fecha de la cantidad total
     * de agua de riego de un cultivo y la fecha del acumulado
     * del deficit de agua por dia.
     */
    return String.valueOf(WaterMath.calculateIrrigationWaterNeed(totalIrrigationWaterCurrentDate, accumulatedWaterDeficitPerDay));
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
  private void requestPastClimateRecordsTwo(PlantingRecord developingPlantingRecord) throws IOException {
    Calendar seedDate = developingPlantingRecord.getSeedDate();

    /*
     * Fecha inmediatamente anterior a la fecha actual
     * (es decir, hoy)
     */
    Calendar yesterday = UtilDate.getYesterdayDate();

    /*
     * Si la fecha de siembra de un cultivo es igual a la fecha
     * actual (es decir, hoy), NO se solicita ni se persiste el
     * registro climatico de la fecha actual, ya que en la fecha
     * de siembra se parte del suelo en capacidad de campo, esto
     * es que el suelo esta lleno de agua, pero no anegado. En
     * esta situacion, el acumulado del deficit de agua por dia
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

    /*
     * A partir de la fecha inmediatamente siguiente a la fecha
     * de siembra de un cultivo se solicitan y persisten los
     * registros climaticos de una parcela que tiene un cultivo
     * en desarrollo en la fecha actual (es decir, hoy)
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
       * Si una parcela dada NO tiene un registro climatico
       * perteneciente a una fecha, se lo solicita al servicio
       * meteorologico utilizado y se lo persiste
       */
      if (!climateRecordService.checkExistence(pastDate, parcel)) {
        newClimateRecord = ClimateClient.getForecast(parcel, pastDate.getTimeInMillis() / 1000);
        climateRecordService.create(newClimateRecord);
      }

      /*
       * Suma un uno al numero de dia en el año de una fecha
       * pasada dada para obtener el siguiente registro climatico
       * correspondiente a una fecha pasada
       */
      pastDate.set(Calendar.DAY_OF_YEAR, pastDate.get(Calendar.DAY_OF_YEAR) + 1);
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
  private void calculateEtsPastClimateRecordsTwo(PlantingRecord developingPlantingRecord) {
    Calendar seedDate = developingPlantingRecord.getSeedDate();

    /*
     * Fecha inmediatamente anterior a la fecha actual
     * (es decir, hoy)
     */
    Calendar yesterday = UtilDate.getYesterdayDate();

    /*
     * Si la fecha de siembra de un cultivo es igual a la fecha
     * actual (es decir, hoy), NO se calculan la ETo y la ETc
     * del registro climatico de la fecha actual, ya que NO se
     * lo persiste debido a que en la fecha de siembra se parte
     * del suelo en capacidad de campo, esto es que el suelo
     * esta lleno de agua, pero no anegado. En esta situacion,
     * el acumulado del deficit de agua por dia [mm/dia] de la
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
     * el acumulado del deficit de agua por dia [mm/dia] del dia
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
     * A partir de la fecha inmediatamente siguiente a la fecha
     * de siembra de un cultivo se calculan los balances hidricos
     * de suelo de una parcela que tiene un cultivo en desarrollo
     * en la fecha actual (es decir, hoy)
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

    /*
     * Fecha inmediatamente anterior a la fecha actual
     * (es decir, hoy)
     */
    Calendar yesterday = UtilDate.getYesterdayDate();

    /*
     * Si la fecha de siembra de un cultivo es igual a la fecha
     * actual (es decir, hoy), NO se calcula el balance hidrico
     * de la fecha actual, ya que en la fecha de siembra se parte
     * del suelo en capacidad de campo, esto es que el suelo esta
     * lleno de agua, pero no anegado. En esta situacion, el
     * acumulado del deficit de agua por dia [mm/dia] de la fecha
     * actual es 0. Por lo tanto, la necesidad de agua de riego
     * de un cultivo en la fecha actual [mm/dia] es 0.
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
     * suelo esta lleno de agua, pero no anegado. En esta situacion,
     * el acumulado del deficit de agua por dia [mm/dia] del dia
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

    double totalIrrigationWaterCurrentDate = 0.0;
    double evaporatedWater = 0.0;
    double waterProvidedPerDay = 0.0;
    double waterDeficitPerDay = 0.0;
    double accumulatedWaterDeficitPerDay = 0.0;
    double accumulatedWaterDeficitPerPreviousDay = 0.0;
    double totalAmountWaterAvailable = 0.0;
    double optimalIrrigationLayer = WaterMath.calculateOptimalIrrigationLayer(crop, parcel.getSoil());

    String stringAccumulatedWaterDeficitPerPreviousDay = null;
    String stringAccumulatedWaterDeficitPerDay = null;
    String notCalculated = soilWaterBalanceService.getNotCalculated();

    Collection<IrrigationRecord> irrigationRecords = null;

    PlantingRecordStatus optimalDevelopmentStatus = statusService.findOptimalDevelopmentStatus();
    PlantingRecordStatus developmentAtRiskWiltingStatus = statusService.findDevelopmentAtRiskWiltingStatus();
    PlantingRecordStatus developmentInWiltingStatus = statusService.findDevelopmentInWiltingStatus();
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
       * en un dia (fecha) [mm/dia] y del deficit de agua en un
       * dia (fecha) [mm/dia]
       */
      climateRecord = climateRecordService.find(pastDate, parcel);
      irrigationRecords = irrigationRecordService.findAllByParcelIdAndDate(parcel.getId(), pastDate);

      waterProvidedPerDay = climateRecord.getPrecip() + WaterMath.sumTotalAmountIrrigationWaterGivenDate(climateRecord.getDate(), irrigationRecords);
      waterDeficitPerDay = WaterMath.calculateWaterDeficitPerDay(climateRecord, irrigationRecords);
      evaporatedWater = soilWaterBalanceService.getEvaporatedWater(climateRecord);

      /*
       * Obtiene el acumulado del deficit de agua por dia del
       * balance hidrico de suelo de la fecha inmediatamente
       * a una fecha pasada
       */
      yesterdayDateFromDate = UtilDate.getYesterdayDateFromDate(pastDate);
      stringAccumulatedWaterDeficitPerPreviousDay = soilWaterBalanceService.find(parcel.getId(), yesterdayDateFromDate).getAccumulatedWaterDeficitPerDay();

      /*
       * Si el acumulado del deficit de agua por dia de la fecha
       * inmediatamente anterior a una fecha pasada NO es NC (no
       * calculado), significa que el cultivo correspondiente a
       * este calculo de balances hidricos de suelo NO murio en
       * la fecha pasada, por lo tanto, se calcula el acumulado
       * del deficit de agua por dia [mm/dia] de la fecha pasada,
       * lo cual se realiza para calcular el balance hidrico de
       * suelo, en el que esta sembrado un cultivo, de la fecha
       * pasada. En caso contrario, significa que el cultivo
       * murio en la fecha pasada, por lo tanto, NO se calcula
       * el acumulado del deficit de agua por dia [mm/dia] de
       * la fecha pasada, lo cual se representa mediante la
       * asignacion de la sigla "NC" a la variable de tipo String
       * del acumulado del deficit de agua por dia [mm/dia]
       * de un balance hidrico.
       */
      if (!stringAccumulatedWaterDeficitPerPreviousDay.equals(notCalculated)) {
        accumulatedWaterDeficitPerPreviousDay = Double.parseDouble(stringAccumulatedWaterDeficitPerPreviousDay);

        /*
         * El acumulado del deficit de agua por dia [mm/dia] de
         * una fecha depende del acumulado del deficit de agua
         * por dia de la fecha inmdiatamente anterior
         */
        accumulatedWaterDeficitPerDay = WaterMath.accumulateWaterDeficitPerDay(waterDeficitPerDay, accumulatedWaterDeficitPerPreviousDay);
        stringAccumulatedWaterDeficitPerDay = String.valueOf(accumulatedWaterDeficitPerDay);

        /*
         * Si la bandera suelo de una parcela esta activa, se
         * comprueba el nivel de humedad del suelo para establecer
         * el estado del registro de plantacion en desarrollo para
         * el que se calcula la necesidad de agua de riego de su
         * cultivo en la fecha actual [mm/dia]
         */
        if (parcel.getOption().getSoilFlag()) {
          totalAmountWaterAvailable = WaterMath.calculateTotalAmountWaterAvailable(crop, parcel.getSoil());

          /*
           * Si el acumulado del deficit de agua por dia [mm/dia] de
           * dias previos a la fecha actual es estrictamente menor al
           * doble del negativo de la capacidad de almacenamiento de
           * agua del suelo, significa que el nivel de humedad del
           * suelo, que tiene un cultivo sembrado, es estrictamente
           * menor al doble de la capacidad de almacenamiento de agua
           * del suelo. En esta situacion el cultivo esta muerto y el
           * registro de plantacion en desarrollo adquiere el estado
           * "Muerto".
           * 
           * Si un cultivo esta muerto NO sirve verificar en que punto
           * se encuentra el nivel de humedad del suelo con respecto a
           * la capacidad de campo, el umbral de riego, la capacidad de
           * almacenamiento de agua del suelo y el doble de la capacidad
           * de almacenamiento de agua del suelo.
           * 
           * Las raices de un cultivo pueden crecer más allá de la capacidad
           * de almacenamiento de agua del suelo, con lo cual un cultivo
           * puede absorber el agua que hay en el punto de marchitez permanente
           * del suelo (*) y la que hay debajo de este punto. Las raices
           * de un cultivo no crecen más allá del doble de la capacidad
           * de almacenamiento de agua del suelo, con lo cual un cultivo
           * no puede absorber el agua que hay en el doble de la capacidad
           * de almacenamiento de agua del suelo ni la que hay debajo de
           * este punto. Por lo tanto, si el nivel de humedad del suelo,
           * en el que está sembrado un cultivo, es estrictamente menor
           * al doble de la capacidad de almacenamiento de agua del mismo,
           * el cultivo no puede absorber el agua que hay en este punto
           * ni la que hay debajo de este punto, lo cual produce como
           * consecuencia la marchitez, y, por ende, la muerte del cultivo.
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
           * el acumulado del deficit de agua por dia [mm/dia] es menor
           * o igual a cero.
           */
          if (accumulatedWaterDeficitPerDay < -(2 * totalAmountWaterAvailable)) {
            stringAccumulatedWaterDeficitPerDay = notCalculated;
            plantingRecordService.setStatus(developingPlantingRecord.getId(), deadStatus);
          } else {
            totalIrrigationWaterCurrentDate = irrigationRecordService.calculateTotalIrrigationWaterCurrentDate(parcel.getId());

            /*
             * Si la suma entre el acumulado del deficit de agua por dia
             * [mm/dia] de dias previos a la fecha actual y la cantidad
             * total de agua de riego de la fecha actual (es decir, hoy)
             * [mm/dia] es menor o igual a la capacidad de campo (0) del
             * suelo y estrictamente mayor a la lamina de riego optima
             * (drop) [mm] negativa, significa que en la fecha actual el
             * nivel de humedad del suelo, que tiene un cultivo sembrado,
             * es menor o igual a la capacidad de campo (0) del suelo y
             * estrictamente mayor a la lamina de riego optima. En esta
             * situacion, el registro de plantacion en desarrollo adquiere
             * el estado "Desarrollo optimo".
             * 
             * El motivo por el cual se coloca el signo negativo a la
             * lamina de riego optima es que el acumulado del deficit
             * de agua por dia [mm/dia] es menor o igual a cero.
             */
            if ((accumulatedWaterDeficitPerDay + totalIrrigationWaterCurrentDate) <= 0
                && (accumulatedWaterDeficitPerDay + totalIrrigationWaterCurrentDate) > -(optimalIrrigationLayer)) {
              plantingRecordService.setStatus(developingPlantingRecord.getId(), optimalDevelopmentStatus);
            }

            /*
             * Si la suma entre el acumulado del deficit de agua por dia
             * [mm/dia] de dias previos a la fecha actual y la cantidad
             * total de agua de riego de la fecha actual (es decir, hoy)
             * [mm/dia] es menor o igual a la lamina de riego optima (drop)
             * [mm] negativa y estrictamente mayor al negativo de la
             * capacidad de almacenamiento de agua del suelo (dt) [mm],
             * significa que en la fecha actual el nivel de humedad del
             * suelo, que tiene un cultivo sembrado, es menor o igual a
             * la lamina de riego optima y estrictamente mayor a la
             * capacidad de almacenamiento de agua del suelo. En esta
             * situacion, el registro de plantacion en desarrollo
             * adquiere el estado "Desarrollo en riesgo de marchitez".
             * 
             * El motivo por el cual se coloca el signo negativo a la
             * lamina de riego optima y a la capacidad de almacenamiento
             * de agua del suelo es que el acumulado del deficit de
             * agua por dia [mm/dia] es menor o igual a cero.
             */
            if ((accumulatedWaterDeficitPerDay + totalIrrigationWaterCurrentDate) <= -(optimalIrrigationLayer)
                && (accumulatedWaterDeficitPerDay + totalIrrigationWaterCurrentDate) > -(totalAmountWaterAvailable)) {
              plantingRecordService.setStatus(developingPlantingRecord.getId(), developmentAtRiskWiltingStatus);
            }

            /*
             * Si la suma entre el acumulado del deficit de agua por dia
             * [mm/dia] de dias previos a la fecha actual y la cantidad
             * total de agua de riego de la fecha actual (es decir, hoy)
             * [mm/dia] es menor o igual al negativo de la capacidad de
             * almacenamiento de agua del suelo [mm] y estrictamente
             * mayor al doble del negativo de la capacidad de almacenamiento
             * de agua del suelo [mm], significa que el nivel de humedad
             * del suelo en la fecha actual es menor o igual a la capacidad
             * de almacenamiento de agua del suelo y estrictamente mayor
             * al doble de la capacidad de almacenamiento de agua del
             * suelo. En esta situacion, el registro de plantacion en
             * desarrollo adquiere el estado "Desarrollo en marchitez".
             * 
             * El motivo por el cual se coloca el signo negativo a la
             * capacidad de almacenamiento de agua del suelo y a su doble
             * es que el acumulado del deficit de agua por dia [mm/dia]
             * es menor o igual a cero.
             */
            if ((accumulatedWaterDeficitPerDay + totalIrrigationWaterCurrentDate) <= -(totalAmountWaterAvailable)
                && (accumulatedWaterDeficitPerDay + totalIrrigationWaterCurrentDate) > -(2 * totalAmountWaterAvailable)) {
              plantingRecordService.setStatus(developingPlantingRecord.getId(), developmentInWiltingStatus);
            }

          } // End if

        } // End if

      } else {
        stringAccumulatedWaterDeficitPerDay = notCalculated;
      }

      /*
       * Si el balance hidrico de suelo de una parcela y una
       * fecha NO existe en la base de datos subyacente, se lo
       * crea y persiste. En caso contrario, se lo actualiza.
       */
      if (!soilWaterBalanceService.checkExistence(parcel.getId(), pastDate)) {
        soilWaterBalance = new SoilWaterBalance();
        soilWaterBalance.setDate(pastDate);
        soilWaterBalance.setParcelName(parcel.getName());
        soilWaterBalance.setCropName(crop.getName());
        soilWaterBalance.setWaterProvided(waterProvidedPerDay);
        soilWaterBalance.setEvaporatedWater(evaporatedWater);
        soilWaterBalance.setWaterDeficitPerDay(waterDeficitPerDay);
        soilWaterBalance.setAccumulatedWaterDeficitPerDay(stringAccumulatedWaterDeficitPerDay);

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
        soilWaterBalance = soilWaterBalanceService.find(parcel.getId(), pastDate);
        soilWaterBalanceService.update(soilWaterBalance.getId(), crop.getName(), evaporatedWater,
            waterProvidedPerDay, waterDeficitPerDay, stringAccumulatedWaterDeficitPerDay);
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
      soilWaterBalance.setWaterProvided(0);
      soilWaterBalance.setEvaporatedWater(0);
      soilWaterBalance.setWaterDeficitPerDay(0);
      soilWaterBalance.setAccumulatedWaterDeficitPerDay(String.valueOf(0));

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
      soilWaterBalance.setWaterProvided(0);
      soilWaterBalance.setEvaporatedWater(0);
      soilWaterBalance.setWaterDeficitPerDay(0);
      soilWaterBalance.setAccumulatedWaterDeficitPerDay(String.valueOf(0));

      /*
       * Realiza las modificaciones del balance hidrico
       * de suelo de la fecha de siembra de un cultivo
       */
      soilWaterBalanceService.modify(parcel.getId(), seedDate, soilWaterBalance);
    }

  }

  /**
   * Ejecuta el proceso del calculo de la necesidad de agua
   * de riego de un cultivo en la fecha actual. Esto es que
   * ejecuta los metodos que se necesitan para calcular y
   * actualizar la necesidad de agua de riego de un cultivo
   * en la fecha actual.
   * 
   * Estos metodos son el metodo para recuperar los registros
   * climaticos de una parcela (*) previos a la fecha actual
   * (es decir, hoy), el metodo para calcular la ETo y la ETc
   * de los registros climaticos de una parcela previos a la
   * fecha actual, el metodo para calcular la necesidad de
   * agua de riego de un cultivo (en desarrollo) en la fecha
   * actual (**) y el metodo para actualizar el atributo de
   * la necesidad de agua de riego del registro de plantacion
   * (en desarrollo) que contiene el cultivo para el cual se
   * solicita calcular su necesidad de agua de riego en la
   * fecha actual.
   * 
   * (*) Esta parcela es la parcela que tiene sembrado el
   * cultivo (en desarrollo) para el cual se solicita calcular
   * su necesidad de agua de riego en la fecha actual.
   * (**) Esto se realiza mediante los registros climaticos
   * de una parcela (*) previos a la fecha actual.
   * 
   * @param user
   * @param developingPlantingRecord
   * @return double que representa la necesidad de agua
   * de riego de un cultivo (en desarrollo) en la fecha
   * actual [mm/dia]
   */
  private double runCalculationIrrigationWaterNeedCurrentDate(User user, PlantingRecord developingPlantingRecord) throws IOException {
    /*
     * Persiste pastDaysReference registros climaticos anteriores
     * a la fecha actual pertenecientes a una parcela dada que tiene
     * un cultivo sembrado y en desarrollo en la fecha actual. Estos
     * registros climaticos son obtenidos del servicio meteorologico
     * utilizado por la aplicacion.
     */
    requestPastClimateRecords(user.getId(), developingPlantingRecord);

    /*
     * Calcula la ETo y la ETc de pastDaysReference registros
     * climaticos anteriores a la fecha actual pertenecientes a una
     * parcela dada que tiene un cultivo sembrado y en desarrollo en
     * la fecha actual
     */
    calculateEtsPastClimateRecords(user.getId(), developingPlantingRecord);

    /*
     * ********************************************************
     * Calculo de la necesidad de agua de riego de un cultivo
     * (en desarrollo) en la fecha actual [mm/dia] en funcion
     * de la ETc (o la ETo si la ETc = 0, lo cual ocurre cuando
     * no hay un cultivo sembrado), el agua de lluvia y el agua
     * de riego de pastDaysReference dias anteriores a la fecha
     * actual, y la cantidad total de agua de riego en la fecha
     * actual
     * ********************************************************
     */
    return calculateIrrigationWaterNeedCurrentDate(user.getId(), developingPlantingRecord);
  }

  /**
   * Persiste una cantidad pastDaysReference de registros
   * climaticos anteriores a la fecha actual pertenecientes a
   * una parcela que tiene un cultivo sembrado y en desarrollo
   * en la fecha actual. Estos registros climaticos son obtenidos
   * del servicio meteorologico utilizado por la aplicacion.
   * 
   * El valor de pastDaysReference depende de cada usuario y
   * solo puede ser entre un limite minimo y un limite maximo,
   * los cuales estan definidos en la clase OptionServiceBean.
   * 
   * @param userId
   * @param developingPlantingRecord
   */
  private void requestPastClimateRecords(int userId, PlantingRecord developingPlantingRecord) throws IOException {
    /*
     * Esta variable representa la cantidad de registros climaticos
     * del pasado (es decir, anteriores a la fecha actual) que la
     * aplicacion recuperara del servicio meteorologico utilizado
     * y de los cuales calculara su ETo y ETc con el fin de calcular
     * la necesidad de agua de riego de un cultivo en la fecha actual
     */
    int pastDaysReference = 0;

    /*
     * Parcela que tiene un cultivo plantado y en desarrollo en
     * la fecha actual
     */
    Parcel givenParcel = developingPlantingRecord.getParcel();
    Option parcelOption = givenParcel.getOption();

    /*
     * Estas fechas son utilizadas para comprobar si existe el
     * ultimo riego registrado para una parcela dentro de los
     * ultimos 30 dias, si el usuario activa la opcion de calcular
     * la necesidad de agua de riego de un cultivo en la fecha
     * actual a partir del ultimo riego registrado para una
     * parcela dentro de los ultimos 30 dias. En caso de que
     * exista en la base de datos subyacente el ultimo riego
     * registrado para una parcela dentro de los ultimos 30 dias,
     * estas fechas tambien se utilizan para obtener el registro
     * de riego correspondiente a dicho riego.
     */
    Calendar minorDate = UtilDate.getPastDateFromOffset(optionService.getValueThirtyDays());
    Calendar majorDate = UtilDate.getYesterdayDate();

    /*
     * Fecha a partir de la cual se obtienen los registros
     * climaticos del pasado pertenecientes a una parcela
     * que tiene un cultivo sembrado y en desarrollo en la
     * fecha actual
     */
    Calendar givenPastDate = null;
    ClimateRecord newClimateRecord;

    /*
     * Si la opcion de calcular la necesidad de agua de riego de
     * un cultivo en la fecha actual a partir del ultimo riego
     * registrado para una parcela dentro de los ultimos 30 dias,
     * esta activa, y existe dicho riego en la base de datos
     * subyacente, se utiliza la fecha del ultimo riego registrado
     * como fecha a partir de la cual obtener los registros
     * climaticos del pasado y se calcula la cantidad de dias
     * pasados a utilizar como referencia para obtener un registro
     * climatico para cada uno de ellos mediante la diferencia
     * entre el numero de dia en el año de la fecha del ultimo
     * riego y el numero de dia en el año de la fecha inmediatamente
     * anterior a la fecha actual
     */
    if (parcelOption.getFlagLastIrrigationThirtyDays() && irrigationRecordService.checkExistenceLastBetweenDates(userId, givenParcel.getId(), minorDate, majorDate)) {
      /*
       * La fecha a partir de la que se deben recuperar los registros
       * climaticos del pasado (es decir, anteriores a la fecha actual)
       * para una parcela es la fecha del ultimo riego registrado de una
       * parcela
       */
      Calendar dateLastIrrigationRecord = irrigationRecordService.findLastBetweenDates(userId, givenParcel.getId(), minorDate, majorDate).getDate();
      givenPastDate = Calendar.getInstance();
      givenPastDate.set(Calendar.YEAR, dateLastIrrigationRecord.get(Calendar.YEAR));
      givenPastDate.set(Calendar.MONTH, dateLastIrrigationRecord.get(Calendar.MONTH));
      givenPastDate.set(Calendar.DAY_OF_YEAR, dateLastIrrigationRecord.get(Calendar.DAY_OF_YEAR));

      /*
       * A la resta entre estas dos fechas se le suma un uno para
       * incluir la fecha mayor en el resultado, ya que dicha fecha
       * cuenta como un dia del pasado (es decir, anterior a la
       * fecha actual) para el cual se debe recuperar un registro
       * climatico. La variable majorDate contiene la referencia
       * a un objeto de tipo Calendar que contiene la fecha
       * inmediatamente anterior a la fecha actual.
       */
      pastDaysReference = UtilDate.calculateDifferenceBetweenDates(givenPastDate, majorDate) + 1;
    }

    /*
     * Si la opcion de calcular la necesidad de agua de riego de
     * un cultivo en la fecha actual a partir del ultimo riego
     * registrado para una parcela dentro de los ultimos 30 dias,
     * NO esta activa, o si NO existe el ultimo riego registrado
     * para una parcela dentro de los ultimos 30 dias, en caso de
     * que dicha opcion este activa, se utiliza la cantidad de
     * dias pasados como referencia de las opciones del usuario
     * para obtener un registro climatico para cada uno de ellos
     * y se calcula la fecha pasada (es decir, anterior a la
     * fecha actual) a partir de la cual obtener los registros
     * climaticos mediante dicha cantidad
     */
    if (!parcelOption.getFlagLastIrrigationThirtyDays() || !irrigationRecordService.checkExistenceLastBetweenDates(userId, givenParcel.getId(), minorDate, majorDate)) {
      pastDaysReference = parcelOption.getPastDaysReference();
      givenPastDate = UtilDate.getPastDateFromOffset(pastDaysReference);
    }

    /*
     * Crea y persiste pastDaysReference registros climaticos
     * anteriores a la fecha actual pertenecientes a una parcela que
     * tiene un cultivo plantado y en desarrollo en la fecha actual.
     * Estos registros climaticos van desde la fecha contenida en el
     * objeto de tipo Calendar referenciado por la variable de tipo
     * por referencia givenPastDate, hasta la fecha inmediatamente
     * anterior a la fecha actual. Las dos sentencias if anteriores
     * contienen la manera en la que se calcula la fecha referenciada
     * por givenPastDate.
     */
    for (int i = 0; i < pastDaysReference; i++) {

      /*
       * Si una parcela dada NO tiene un registro climatico
       * de una fecha anterior a la fecha actual, se lo solicita
       * al servicio meteorologico utilizado y se lo persiste
       */
      if (!climateRecordService.checkExistence(givenPastDate, givenParcel)) {
        newClimateRecord = ClimateClient.getForecast(givenParcel, givenPastDate.getTimeInMillis() / 1000);
        climateRecordService.create(newClimateRecord);
      }

      /*
       * Suma un uno al numero de dia en el año de una fecha
       * pasada dada para obtener el siguiente registro climatico
       * correspondiente a una fecha pasada
       */
      givenPastDate.set(Calendar.DAY_OF_YEAR, givenPastDate.get(Calendar.DAY_OF_YEAR) + 1);
    }

  }

  /**
   * Calcula y actualiza la ETo y la ETc de pastDaysReference
   * registros climaticos anteriores a la fecha actual pertenecientes
   * a una parcela que tiene un cultivo sembrado y en desarrollo en
   * la fecha actual.
   * 
   * El valor de pastDaysReference depende de cada usuario y
   * solo puede ser entre un limite minimo y un limite maximo,
   * los cuales estan definidos en la clase OptionServiceBean.
   * 
   * @param userId
   * @param developingPlantingRecord
   */
  private void calculateEtsPastClimateRecords(int userId, PlantingRecord developingPlantingRecord) {
    /*
     * Esta variable representa la cantidad de registros climaticos
     * del pasado (es decir, anteriores a la fecha actual) que la
     * aplicacion recuperara del servicio meteorologico utilizado
     * y de los cuales calculara su ETo y ETc con el fin de calcular
     * la necesidad de agua de riego de un cultivo en la fecha actual
     */
    int pastDaysReference = 0;

    /*
     * Parcela que tiene un cultivo plantado y en desarrollo en
     * la fecha actual
     */
    Parcel givenParcel = developingPlantingRecord.getParcel();
    Option parcelOption = givenParcel.getOption();

    /*
     * Estas fechas son utilizadas para comprobar si existe el
     * ultimo riego registrado para una parcela dentro de los
     * ultimos 30 dias, si el usuario activa la opcion de calcular
     * la necesidad de agua de riego de un cultivo en la fecha
     * actual a partir del ultimo riego registrado para una
     * parcela dentro de los ultimos 30 dias. En caso de que
     * exista en la base de datos subyacente el ultimo riego
     * registrado para una parcela dentro de los ultimos 30
     * dias, estas fechas tambien se utilizan para obtener
     * el registro de riego correspondiente a dicho riego.
     */
    Calendar minorDate = UtilDate.getPastDateFromOffset(optionService.getValueThirtyDays());
    Calendar majorDate = UtilDate.getYesterdayDate();

    /*
     * Fecha a partir de la cual se obtienen los registros
     * climaticos del pasado pertenecientes a una parcela
     * que tiene un cultivo sembrado y en desarrollo en la
     * fecha actual
     */
    Calendar givenPastDate = null;
    ClimateRecord givenClimateRecord;
    PlantingRecord givenPlantingRecord;

    double eto = 0.0;
    double etc = 0.0;

    /*
     * Si la opcion de calcular la necesidad de agua de riego de
     * un cultivo en la fecha actual a partir del ultimo riego
     * registrado para una parcela dentro de los ultimos 30 dias,
     * esta activa, y existe dicho riego en la base de datos
     * subyacente, se utiliza la fecha del ultimo riego registrado
     * como fecha a partir de la cual obtener los registros
     * climaticos del pasado y se calcula la cantidad de dias
     * pasados a utilizar como referencia para obtener un registro
     * climatico para cada uno de ellos mediante la diferencia
     * entre el numero de dia en el año de la fecha del ultimo
     * riego y el numero de dia en el año de la fecha inmediatamente
     * anterior a la fecha actual
     */
    if (parcelOption.getFlagLastIrrigationThirtyDays() && irrigationRecordService.checkExistenceLastBetweenDates(userId, givenParcel.getId(), minorDate, majorDate)) {
      /*
       * La fecha a partir de la que se deben recuperar los registros
       * climaticos del pasado (es decir, anteriores a la fecha actual)
       * para una parcela es la fecha del ultimo riego registrado de una
       * parcela
       */
      Calendar dateLastIrrigationRecord = irrigationRecordService.findLastBetweenDates(userId, givenParcel.getId(), minorDate, majorDate).getDate();
      givenPastDate = Calendar.getInstance();
      givenPastDate.set(Calendar.YEAR, dateLastIrrigationRecord.get(Calendar.YEAR));
      givenPastDate.set(Calendar.MONTH, dateLastIrrigationRecord.get(Calendar.MONTH));
      givenPastDate.set(Calendar.DAY_OF_YEAR, dateLastIrrigationRecord.get(Calendar.DAY_OF_YEAR));

      /*
       * A la resta entre estas dos fechas se le suma un uno para
       * incluir la fecha mayor en el resultado, ya que dicha fecha
       * cuenta como un dia del pasado (es decir, anterior a la
       * fecha actual) para el cual se debe recuperar un registro
       * climatico. La variable majorDate contiene la referencia
       * a un objeto de tipo Calendar que contiene la fecha
       * inmediatamente anterior a la fecha actual.
       */
      pastDaysReference = UtilDate.calculateDifferenceBetweenDates(givenPastDate, majorDate) + 1;
    }

    /*
     * Si la opcion de calcular la necesidad de agua de riego de
     * un cultivo en la fecha actual a partir del ultimo riego
     * registrado para una parcela dentro de los ultimos 30 dias,
     * NO esta activa, o si NO existe el ultimo riego registrado
     * para una parcela dentro de los ultimos 30 dias, en caso de
     * que dicha opcion este activa, se utiliza la cantidad de
     * dias pasados como referencia de las opciones del usuario
     * para obtener un registro climatico para cada uno de ellos
     * y se calcula la fecha pasada (es decir, anterior a la
     * fecha actual) a partir de la cual obtener los registros
     * climaticos mediante dicha cantidad
     */
    if (!parcelOption.getFlagLastIrrigationThirtyDays() || !irrigationRecordService.checkExistenceLastBetweenDates(userId, givenParcel.getId(), minorDate, majorDate)) {
      pastDaysReference = parcelOption.getPastDaysReference();
      givenPastDate = UtilDate.getPastDateFromOffset(pastDaysReference);
    }

    /*
     * Calcula la ETo y la ETc de pastDaysReference registros
     * climaticos anteriores a la fecha actual pertenecientes a una
     * parcela que tiene un cultivo plantado y en desarrollo en la fecha
     * actual. Estos registros climaticos van desde la fecha resultante
     * de la resta entre el numero de dia en el año de la fecha actual
     * y pastDaysReference, hasta la fecha inmediatamente anterior
     * a la fecha actual.
     */
    for (int i = 0; i < pastDaysReference; i++) {

      /*
       * Si una parcela dada tiene un registro climatico de una
       * fecha anterior a la fecha actual, calcula la ETo y la
       * ETc del mismo
       */
      if (climateRecordService.checkExistence(givenPastDate, givenParcel)) {
        givenClimateRecord = climateRecordService.find(givenPastDate, givenParcel);

        eto = calculateEtoForClimateRecord(givenClimateRecord);

        /*
         * Si la parcela dada tiene un registro de plantacion en
         * el que la fecha pasada dada esta entre la fecha de siembra
         * y la fecha de cosecha del mismo, se obtiene el kc
         * (coeficiente de cultivo) del cultivo de este registro para
         * poder calcular la ETc (evapotranspiracion del cultivo bajo
         * condiciones estandar) de dicho cultivo.
         * 
         * En otras palabras, lo que hace esta instruccion if es
         * preguntar "¿la parcela dada tuvo o tiene un cultivo
         * sembrado en la fecha dada?". En caso afirmativo se
         * obtiene el kc del cultivo para calcular su ETc, la
         * cual se asignara al correspondiente registro climatico.
         */
        if (plantingRecordService.checkExistence(givenParcel, givenPastDate)) {
          givenPlantingRecord = plantingRecordService.find(givenParcel, givenPastDate);
          etc = calculateEtcForClimateRecord(eto, givenPlantingRecord, givenPastDate);
        }

        climateRecordService.updateEtoAndEtc(givenPastDate, givenParcel, eto, etc);

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
      givenPastDate.set(Calendar.DAY_OF_YEAR, givenPastDate.get(Calendar.DAY_OF_YEAR) + 1);
    } // End for

  }

  /**
   * Calcula la necesidad de agua de riego de un cultivo en la fecha
   * actual [mm/dia] en funcion de la cantidad total de agua de riego
   * de la fecha actual, una coleccion de registros climaticos y una
   * coleccion de registros de riego, siendo todos ellos previos a la
   * fecha actual y pertenecientes a una misma parcela, la cual tiene
   * el cultivo para el cual se calcula la necesidad de agua de riego
   * en la fecha actual
   * 
   * @param userId
   * @param developingPlantingRecord
   * @return double que representa la necesidad de agua de riego
   * de un cultivo en la fecha actual [mm/dia]
   */
  private double calculateIrrigationWaterNeedCurrentDate(int userId, PlantingRecord developingPlantingRecord) {
    /*
     * Estas fechas se utilizan para obtener de la base de datos
     * subyacente los registros climaticos y los registros de riego
     * previos a la fecha actual (es decir, hoy) y pertenecientes a
     * una misma parcela, los cuales se utilizan para calcular la
     * necesidad de agua de riego de un cultivo en la fecha actual.
     * 
     * La variable dateFrom representa la fecha a partir de la cual
     * se obtienen los registros climaticos y los registros de riego
     * de una parcela previos a la fecha actual. En cambio, la variable
     * dateUntil representa la fecha hasta la cual se obtienen los
     * registros climaticos y los registros de riego de una parcela
     * previos a la fecha actual.
     * 
     * La fecha para la que se calcula la necesidad de agua de riego
     * de un cultivo esta determinada por los registros climaticos y
     * los registros de riego que se seleccionan como previos a una
     * fecha dada, siendo ambos grupos de registros pertenecientes a
     * una misma parcela.
     * 
     * Por ejemplo, si se seleccionan los registros climaticos y los
     * registros de riego de una parcela dada previos a la fecha
     * actual (es decir, hoy), la necesidad de agua de riego de un
     * cultivo calculada con estos registros corresponde a la fecha
     * actual. En cambio, si se seleccionan los registros climaticos
     * y los registros de riego de una parcela dada previos a la
     * fecha actual + X dias, donde X > 0, la necesidad de agua de
     * riego de un cultivo calculada con estos registros corresponde
     * a la fecha actual + X dias.
     * 
     * En el caso de este metodo, la variable dateUntil contiene la
     * fecha inmediatamente anterior a la fecha actual (es decir,
     * hoy) y la variable dateFrom contiene una fecha igual a X dias,
     * donde X > 0, anteriores a la fecha inmediatamente anterior a
     * la fecha actual, porque el objetivo de este metodo es calcular
     * la necesidad de agua de riego de un cultivo en la fecha actual.
     * 
     * Se puede calcular la necesidad de agua de riego de un cultivo
     * en una fecha pasada (es decir, anterior a la fecha actual),
     * pero esto no tiene sentido si lo que se busca es determinar
     * la necesidad de agua de riego de un cultivo en la fecha
     * actual o en una fecha posterior a la fecha actual. Este no
     * es el caso de este metodo, ya que, como se dijo anteriormente,
     * el objetivo del mismo es calcular la necesidad de agua de
     * riego de un cultivo en la fecha actual, motivo por el cual
     * se utilizan los registros climaticos y los registros de riego
     * previos a la fecha actual y pertenecientes a una misma parcela.
     */
    Calendar dateFrom = null;
    Calendar dateUntil = UtilDate.getYesterdayDate();

    Parcel givenParcel = developingPlantingRecord.getParcel();
    Option parcelOption = givenParcel.getOption();

    /*
     * Estas fechas son utilizadas para comprobar si existe el
     * ultimo riego registrado para una parcela dentro de los
     * ultimos 30 dias, si el usuario activa la opcion de calcular
     * la necesidad de agua de riego de un cultivo en la fecha
     * actual a partir del ultimo riego registrado para una
     * parcela dentro de los ultimos 30 dias. En caso de que
     * exista en la base de datos subyacente el ultimo riego
     * registrado para una parcela dentro de los ultimos 30
     * dias, estas fechas tambien se utilizan para obtener
     * el registro de riego correspondiente a dicho riego.
     */
    Calendar minorDate = UtilDate.getPastDateFromOffset(optionService.getValueThirtyDays());
    Calendar majorDate = UtilDate.getYesterdayDate();

    /*
     * Si la opcion de calcular la necesidad de agua de riego de
     * un cultivo en la fecha actual a partir del ultimo riego
     * registrado para una parcela dentro de los ultimos 30 dias,
     * esta activa, y existe dicho riego en la base de datos
     * subyacente, se utiliza la fecha del ultimo riego registrado
     * como fecha a partir de la cual obtener los registros climaticos
     * y los registros de riego de una parcela dada, siendo todos
     * ellos previos a la fecha actual, ya que lo se que busca
     * con este metodo es calcular la necesidad de agua de riego
     * de un cultivo en la fecha actual [mm/dia]
     */
    if (parcelOption.getFlagLastIrrigationThirtyDays() && irrigationRecordService.checkExistenceLastBetweenDates(userId, givenParcel.getId(), minorDate, majorDate)) {
      /*
       * La fecha a partir de la que se deben obtener los registros
       * climaticos y los registros de riego previos a la fecha
       * actual, siendo todos ellos pertenecientes a una misma
       * parcela, es la fecha del ultimo riego registrado de una
       * parcela
       */
      Calendar dateLastIrrigationRecord = irrigationRecordService.findLastBetweenDates(userId, givenParcel.getId(), minorDate, majorDate).getDate();
      dateFrom = Calendar.getInstance();
      dateFrom.set(Calendar.YEAR, dateLastIrrigationRecord.get(Calendar.YEAR));
      dateFrom.set(Calendar.MONTH, dateLastIrrigationRecord.get(Calendar.MONTH));
      dateFrom.set(Calendar.DAY_OF_YEAR, dateLastIrrigationRecord.get(Calendar.DAY_OF_YEAR));
    }

    /*
     * Si la opcion de calcular la necesidad de agua de riego de
     * un cultivo en la fecha actual a partir del ultimo riego
     * registrado para una parcela dentro de los ultimos 30 dias,
     * NO esta activa, o si NO existe el ultimo riego registrado
     * para una parcela dentro de los ultimos 30 dias, en caso de
     * que dicha opcion este activa, se utiliza la cantidad de dias
     * pasados como referencia de las opciones del usuario para
     * calcular la fecha pasada (es decir, anterior a la fecha
     * actual) a partir de la cual obtener los registros climaticos
     * y los registros de riego de una parcela dada, siendo
     * todos ellos previos a la fecha actual, ya que lo que se
     * busca con este metodo es calcular la necesidad de agua
     * de riego de un cultivo en la fecha actual [mm/dia]
     */
    if (!parcelOption.getFlagLastIrrigationThirtyDays() || !irrigationRecordService.checkExistenceLastBetweenDates(userId, givenParcel.getId(), minorDate, majorDate)) {
      dateFrom = UtilDate.getPastDateFromOffset(parcelOption.getPastDaysReference());
    }

    double totalIrrigationWaterCurrentDate = irrigationRecordService.calculateTotalIrrigationWaterCurrentDate(givenParcel.getId());

    /*
     * Obtiene de la base de datos subyacente los registros
     * climaticos y los registros de reigo de una parcela dada
     * que estan comprendidos entre una fecha desde y una fecha
     * hasta. La fecha hasta es el dia inmediatamente anterior a
     * la fecha actual (es decir, hoy) y la fecha desde es una
     * cantidad X > 0 de dias anteriores a la fecha inmediatamente
     * anterior a la fecha actual.
     * 
     * El motivo de esto es que este metodo tiene como objetivo
     * calcular la necesidad de agua de riego de un cultivo en la
     * fecha actual, para lo cual se requieren los registros
     * climaticos y los registros de riego de una parcela previos
     * a la fecha actual.
     */
    Collection<ClimateRecord> climateRecords = climateRecordService.findAllByParcelIdAndPeriod(userId, givenParcel.getId(), dateFrom, dateUntil);
    Collection<IrrigationRecord> irrigationRecords = irrigationRecordService.findAllByParcelIdAndPeriod(userId, givenParcel.getId(), dateFrom, dateUntil);

    /*
     * Genera los balances hidricos de suelo asociados al cultivo
     * para el que se calcula su necesidad de agua de riego en la
     * fecha actual [mm/dia]. El motivo de esto es para que el
     * usuario pueda ver la manera en la que la aplicacion calculo
     * dicha necesidad. El valor del deficit acumulado de agua del
     * mas actual de estos registros es la necesidad de agua de
     * riego del cultivo en la fecha actual. Por lo tanto, este
     * valor debe ser igual al valor del campo "Necesidad de agua
     * de riego de hoy [mm/dia]" de la ventana que se despliega
     * en la pagina web de lista de registros de plantacion cuando
     * se presiona el boton "Calcular" sobre un registro de plantacion
     * en desarrollo.
     */
    soilWaterBalanceService.generateSoilWaterBalances(developingPlantingRecord.getParcel(),
        developingPlantingRecord.getCrop().getName(), climateRecords,
        irrigationRecords);

    /*
     * Se debe invocar el metodo merge() de la clase ParcelServiceBean
     * para persistir los elementos que se hayan agregado a
     * la coleccion soilWaterBalances de una parcela durante
     * la ejecucion del metodo generateSoilWaterBalances de
     * la clase generateSoilWaterBalances(). De lo contrario,
     * la base de datos subyacente quedara en un estado
     * inconsistente.
     */
    parcelService.merge(givenParcel);

    double cropIrrigationWaterNeedCurrentDate = 0.0;

    /*
     * Si la bandera suelo NO esta activa se calcula la necesidad
     * de agua de riego de un cultivo en la fecha actual [mm/dia]
     * mediante el algoritmo que NO utiliza datos de suelo para
     * ello.
     * 
     * En otras palabras, se calcula la necesidad de agua de riego
     * de un cultivo en la fecha actual [mm/dia] sin tener en cuenta
     * el suelo de la parcela en la que esta sembrado, independientemente
     * de si la parcela tiene o no asignado un suelo.
     */
    if (!parcelOption.getSoilFlag()) {
      cropIrrigationWaterNeedCurrentDate = WaterNeedWos.calculateIrrigationWaterNeed(totalIrrigationWaterCurrentDate, climateRecords, irrigationRecords);
    }

    /*
     * Si la bandera suelo esta activa se calcula la necesidad
     * de agua de riego de un cultivo en la fecha actual [mm/dia]
     * mediante el algoritmo que utiliza datos de suelo para ello.
     * 
     * En otras palabras, se calcula la necesidad de agua de riego
     * de un cultivo en la fecha actual [mm/dia] teniendo en cuenta
     * el suelo de la parcela en la que esta sembrado.
     * 
     * Este algoritmo puede retornar el valor -1, el cual representa
     * la situacion en la que el nivel de humedad del suelo, que tiene
     * un cultivo sembrado, esta en el punto de marchitez permanente,
     * en el cual un cultivo no puede extraer agua del suelo y no
     * puede recuperarse de la perdida hidrica aunque la humedad
     * ambiental sea saturada.
     */
    if (parcelOption.getSoilFlag()) {
      cropIrrigationWaterNeedCurrentDate = WaterNeedWs.calculateIrrigationWaterNeed(totalIrrigationWaterCurrentDate,
          developingPlantingRecord.getCrop(), givenParcel.getSoil(), climateRecords, irrigationRecords);
    }

    return cropIrrigationWaterNeedCurrentDate;
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
    Parcel givenParcel = givenClimateRecord.getParcel();
    double extraterrestrialSolarRadiation = solarService.getRadiation(givenParcel.getLatitude(),
        monthService.getMonth(givenClimateRecord.getDate().get(Calendar.MONTH)), latitudeService.find(givenParcel.getLatitude()),
        latitudeService.findPreviousLatitude(givenParcel.getLatitude()),
        latitudeService.findNextLatitude(givenParcel.getLatitude()));

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

}
