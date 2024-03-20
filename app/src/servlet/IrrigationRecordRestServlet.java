package servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.Calendar;
import java.util.Date;
import javax.ejb.EJB;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import stateless.IrrigationRecordServiceBean;
import stateless.PlantingRecordServiceBean;
import stateless.ParcelServiceBean;
import stateless.SecretKeyServiceBean;
import stateless.SolarRadiationServiceBean;
import stateless.CropServiceBean;
import stateless.LatitudeServiceBean;
import stateless.MonthServiceBean;
import stateless.SessionServiceBean;
import stateless.SoilWaterBalanceServiceBean;
import stateless.PlantingRecordStatusServiceBean;
import stateless.Page;
import model.IrrigationRecord;
import model.Parcel;
import model.PlantingRecord;
import model.IrrigationWaterNeedFormData;
import util.UtilDate;
import util.ErrorResponse;
import util.ReasonError;
import util.RequestManager;
import utilJwt.AuthHeaderManager;
import utilJwt.JwtManager;
import et.HargreavesEto;
import et.Etc;
import java.text.SimpleDateFormat;
import java.text.ParseException;

@Path("/irrigationRecords")
public class IrrigationRecordRestServlet {

  // inject a reference to the IrrigationRecordServiceBean slsb
  @EJB
  IrrigationRecordServiceBean irrigationRecordService;

  @EJB
  PlantingRecordServiceBean plantingRecordService;

  @EJB
  SolarRadiationServiceBean solarService;

  @EJB
  LatitudeServiceBean latitudeService;

  @EJB
  SecretKeyServiceBean secretKeyService;

  @EJB
  CropServiceBean cropService;

  @EJB
  MonthServiceBean monthService;

  @EJB
  SessionServiceBean sessionService;

  @EJB
  ParcelServiceBean parcelService;

  @EJB
  SoilWaterBalanceServiceBean soilWaterBalanceService;

  @EJB
  PlantingRecordStatusServiceBean statusService;

  // mapea lista de pojo a JSON
  ObjectMapper mapper = new ObjectMapper();

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

    /*
     * Si el valor del encabezado de autorizacion de la peticion HTTP
     * dada, tiene un JWT valido, la aplicacion del lado servidor
     * devuelve el mensaje HTTP 200 (Ok) junto con los datos solicitados
     * por el cliente
     */
    return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(irrigationRecordService.findAll(userId))).build();
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
      return Response.status(Response.Status.UNAUTHORIZED)
          .entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.NO_ACTIVE_SESSION))).build();
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
    Page<IrrigationRecord> irrigationRecords = irrigationRecordService.findAllPagination(userId, page, cant, map);
    return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(irrigationRecords)).build();
  }

  @GET
  @Path("/filter")
  @Produces(MediaType.APPLICATION_JSON)
  public Response filter(@Context HttpHeaders request, @QueryParam("parcelName") String parcelName,
      @QueryParam("date") String stringDate) throws IOException, ParseException {
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

    /*
     * En la aplicacion del lado del navegador Web la variable
     * correspondiente a la fecha puede tener el valor undefined
     * o el valor null. Estos valores representan que una variable
     * no tiene un valor asignado. En este caso indican que una
     * variable no tiene asignada una fecha. Cuando esto ocurre
     * y se realiza una peticion HTTP a este metodo con esta
     * variable con uno de dichos dos valores, la variable de
     * tipo String date tiene como contenido la cadena "undefined"
     * o la cadena "null". En Java para representar adecuadamente
     * que date NO tiene una fecha asignada, en caso de que
     * provenga de la aplicacion del lado del navegador web con
     * el valor undefined o el valor null, se debe asignar el
     * valor null a la variable date.
     */
    if (stringDate != null && (stringDate.equals(UNDEFINED_VALUE) || stringDate.equals(NULL_VALUE))) {
      stringDate = null;
    }

    /*
     * Si el nombre de la parcela y la fecha NO estan definidos,
     * la aplicacion del lado servidor retorna el mensaje HTTP
     * 400 (Bad request) junto con el mensaje "La parcela y la
     * fecha deben estar definidas" y no se realiza la operacion
     * solicitada
     */
    if (parcelName == null && stringDate == null) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.UNDEFINED_PARCEL_NAME_AND_DATE)))
          .build();
    }

    /*
     * Si la fecha esta definida, pero no el nombre de la
     * parcela, la aplicacion del lador servidor retorna el
     * mensaje HTTP 400 (Bad request) junto con el mensaje
     * "La parcela debe estar definida" y no se realiza la
     * operacion solicitada
     */
    if (parcelName == null && stringDate != null) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.INDEFINITE_PARCEL)))
          .build();
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
     * Si el nombre de la parcela esta definido y la fecha
     * NO esta definida, la aplicacion del lado servidor
     * retorna una coleccion de registros de riego
     * pertenecientes a una parcela de un usuario
     */
    if (parcelName != null && stringDate == null) {
      return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(irrigationRecordService.findAllByParcelName(userId, parcelName))).build();
    }

    SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

    /*
     * Si la fecha elegida es estrictamente mayor al año maximo
     * (9999), la aplicacion del lado servidor retorna el mensaje
     * HTTP 400 (Bad request) junto con el mensaje "La fecha no
     * debe ser estrictamente mayor a 9999" y no se realiza la
     * operacion solicitada
     */
    if (stringDate != null) {
      Date date = new Date(dateFormatter.parse(stringDate).getTime());

      if (UtilDate.yearIsGreaterThanMaximum(UtilDate.toCalendar(date))) {
        return Response.status(Response.Status.BAD_REQUEST)
            .entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.DATE_GREATEST_TO_MAXIMUM)))
            .build();
      }

    }

    /*
     * Si la fecha esta definida, la aplicacion del lado servidor
     * la convierte a un objeto de tipo Date que contiene la
     * fecha ingresada por el usuario en el fitro de la pagina
     * web de registros de riego
     */
    Date date = new Date(dateFormatter.parse(stringDate).getTime());

    /*
     * Si el valor del encabezado de autorizacion de la peticion HTTP
     * dada, tiene un JWT valido, la aplicacion del lado servidor
     * devuelve el mensaje HTTP 200 (Ok) junto con los datos solicitados
     * por el cliente
     */
    return Response.status(Response.Status.OK)
        .entity(mapper.writeValueAsString(irrigationRecordService.findAllByParcelNameAndDate(userId, parcelName, UtilDate.toCalendar(date))))
        .build();
  }

  @GET
  @Path("/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response find(@Context HttpHeaders request, @PathParam("id") int irrigationRecordId) throws IOException {
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

    /*
     * Si el dato solicitado no existe en la base de datos
     * subyacente, la aplicacion del lado servidor devuelve
     * el mensaje HTTP 404 (Not found) junto con el mensaje
     * "Recurso no encontrado" y no se realiza la operacion
     * solicitada
     */
    if (!irrigationRecordService.checkExistence(irrigationRecordId)) {
      return Response.status(Response.Status.NOT_FOUND).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.RESOURCE_NOT_FOUND))).build();
    }

    /*
     * Si al usuario que hizo esta peticion HTTP, no le pertenece
     * el dato solicitado, la aplicacion del lado servidor
     * devuelve el mensaje HTTP 403 (Forbidden) junto con el
     * mensaje "Acceso no autorizado" (contenido en el enum
     * ReasonError) y no se realiza la operacion solicitada
     */
    if (!irrigationRecordService.checkUserOwnership(userId, irrigationRecordId)) {
      return Response.status(Response.Status.FORBIDDEN).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.UNAUTHORIZED_ACCESS))).build();
    }

    /*
     * Si el valor del encabezado de autorizacion de la peticion HTTP
     * dada, tiene un JWT valido, la aplicacion del lado servidor
     * devuelve el mensaje HTTP 200 (Ok) junto con los datos solicitados
     * por el cliente
     */
    return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(irrigationRecordService.findByUserId(userId, irrigationRecordId))).build();
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

    IrrigationRecord newIrrigationRecord = mapper.readValue(json, IrrigationRecord.class);

    /*
     * ******************************************
     * Controles sobre la definicion de los datos
     * ******************************************
     */

    /*
     * Si la fecha de un registro de riego nuevo NO esta
     * definida, la aplicacion del lado servidor retorna el
     * mensaje HTTP 400 (Bad request) junto con el mensaje
     * "La fecha debe estar definida" y no se realiza la
     * operacion solicitada
     */
    if (newIrrigationRecord.getDate() == null) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.UNDEFINED_DATE))).build();
    }

    /*
     * Si la fecha de un registro de riego nuevo es estrictamente
     * mayor a la fecha actual, la aplicacion del lado servidor
     * retorna el mensaje HTTP 400 (Bad request) junto con el mensaje
     * "No esta permitido que un registro de riego tenga una fecha
     * estrictamente mayor (es decir, posterior) a la fecha actual"
     * y no se realiza la operacion solicitada.
     * 
     * De esta manera, se evita la creacion de registros de riego
     * del futuro, ya que no tiene sentido registrar la cantidad de
     * agua que se utilizara para el riego de una parcela o un cultivo.
     */
    if (UtilDate.compareTo(newIrrigationRecord.getDate(), UtilDate.getCurrentDate()) > 0) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.IRRIGATION_RECORD_OF_THE_FUTURE_NOT_ALLOWED)))
          .build();
    }

    /*
     * Si la parcela NO esta definida, la aplicacion del lado
     * servidor retorna el mensaje HTTP 400 (Bad request) junto
     * con el mensaje "La parcela debe estar definida" y no se
     * realiza la operacion solicitada
     */
    if (newIrrigationRecord.getParcel() == null) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.INDEFINITE_PARCEL))).build();
    }

    /*
     * Si el riego realizado es negativo, la aplicacion del lado
     * servidor retorna el mensaje HTTP 400 (Bad request) junto
     * con el mensaje "El riego realizado debe ser mayor o igual
     * a cero" y no se realiza la operacion solicitada
     */
    if (newIrrigationRecord.getIrrigationDone() < 0) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.NEGATIVE_REALIZED_IRRIGATION))).build();
    }

    /*
     * Comprueba si la fecha de un registro de riego esta en el
     * periodo definido por la fecha de siembra y la fecha de cosecha
     * de uno de los registros de plantacion correspondiente a la
     * parcela del registro de riego. En caso afirmativo, se recupera
     * el registro de plantacion correspondiente y se obtiene su
     * cultivo para asignarlo al registro de riego.
     */
    if (plantingRecordService.checkByDate(userId, newIrrigationRecord.getParcel(), newIrrigationRecord.getDate())) {
      newIrrigationRecord.setCrop(plantingRecordService.findByDate(userId, newIrrigationRecord.getParcel(), newIrrigationRecord.getDate()).getCrop());
    }

    /*
     * Persistencia del nuevo registro de riego
     */
    newIrrigationRecord = irrigationRecordService.create(newIrrigationRecord);

    /*
     * Si el valor del encabezado de autorizacion de la peticion HTTP
     * dada, tiene un JWT valido, la aplicacion del lado servidor
     * devuelve el mensaje HTTP 200 (Ok) junto con los datos que el
     * cliente solicito persistir
     */
    return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(newIrrigationRecord)).build();
  }

  @PUT
  @Path("/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response modify(@Context HttpHeaders request, @PathParam("id") int irrigationRecordId, String json) throws IOException {
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

    /*
     * Si el dato solicitado no existe en la base de datos
     * subyacente, la aplicacion del lado servidor devuelve
     * el mensaje HTTP 404 (Not found) junto con el mensaje
     * "Recurso no encontrado" y no se realiza la operacion
     * solicitada
     */
    if (!irrigationRecordService.checkExistence(irrigationRecordId)) {
      return Response.status(Response.Status.NOT_FOUND).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.RESOURCE_NOT_FOUND))).build();
    }

    /*
     * Si al usuario que hizo esta peticion HTTP, no le pertenece
     * el dato solicitado, la aplicacion del lado servidor
     * devuelve el mensaje HTTP 403 (Forbidden) junto con el
     * mensaje "Acceso no autorizado" (contenido en el enum
     * ReasonError) y no se realiza la operacion solicitada
     */
    if (!irrigationRecordService.checkUserOwnership(userId, irrigationRecordId)) {
      return Response.status(Response.Status.FORBIDDEN).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.UNAUTHORIZED_ACCESS))).build();
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

    IrrigationRecord modifiedIrrigationRecord = mapper.readValue(json, IrrigationRecord.class);

    /*
     * ******************************************
     * Controles sobre la definicion de los datos
     * ******************************************
     */

    /*
     * Si la fecha del registro de riego modificada NO esta
     * definida, la aplicacion del lado servidor retorna el
     * mensaje HTTP 400 (Bad request) junto con el mensaje
     * "La fecha debe estar definida" y no se realiza la
     * operacion solicitada
     */
    if (modifiedIrrigationRecord.getDate() == null) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.UNDEFINED_DATE))).build();
    }

    /*
     * Si la fecha del registro de riego modificado es
     * es estrictamente mayor a la fecha actual, la
     * aplicacion del lado servidor retorna el mensaje
     * HTTP 400 (Bad request) juto con el mensaje "No
     * esta permitido que un registro de riego tenga
     * una fecha estrictamente mayor (es decir, posterior)
     * a la fecha actual" y no se realiza la operacion
     * solicitada.
     * 
     * De esta manera, se evita la modificacion de registros
     * de riego con fechas futuras, ya que no tiene sentido
     * registrar la cantidad de agua que se utilizara
     * para el riego de una parcela o un cultivo.
     */
    if (UtilDate.compareTo(modifiedIrrigationRecord.getDate(), UtilDate.getCurrentDate()) > 0) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.IRRIGATION_RECORD_OF_THE_FUTURE_NOT_ALLOWED)))
          .build();
    }

    /*
     * Si la parcela NO esta definida, la aplicacion del lado
     * servidor retorna el mensaje HTTP 400 (Bad request) junto
     * con el mensaje "La parcela debe estar definida" y no se
     * realiza la operacion solicitada
     */
    if (modifiedIrrigationRecord.getParcel() == null) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.INDEFINITE_PARCEL))).build();
    }

    /*
     * *************************************
     * Controles sobre la forma de los datos
     * *************************************
     */

    /*
     * Si el riego realizado es negativo, la aplicacion del lado
     * servidor retorna el mensaje HTTP 400 (Bad request) junto
     * con el mensaje "El riego realizado debe ser mayor o igual
     * a cero" y no se realiza la operacion solicitada
     */
    if (modifiedIrrigationRecord.getIrrigationDone() < 0) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.NEGATIVE_REALIZED_IRRIGATION))).build();
    }

    /*
     * Comprueba si la fecha de un registro de riego esta en el
     * periodo definido por la fecha de siembra y la fecha de cosecha
     * de uno de los registros de plantacion correspondiente a la
     * parcela del registro de riego. En caso afirmativo, se recupera
     * el registro de plantacion correspondiente y se obtiene su
     * cultivo para asignarlo al registro de riego. En caso negativo,
     * el registro de riego no debe tener un cultivo asignado.
     */
    if (plantingRecordService.checkByDate(userId, modifiedIrrigationRecord.getParcel(), modifiedIrrigationRecord.getDate())) {
      modifiedIrrigationRecord.setCrop(plantingRecordService.findByDate(userId, modifiedIrrigationRecord.getParcel(), modifiedIrrigationRecord.getDate()).getCrop());
    } else {
      modifiedIrrigationRecord.setCrop(null);
    }

    /*
     * Persistencia del registro de riego modificado
     */
    modifiedIrrigationRecord = irrigationRecordService.modify(userId, irrigationRecordId, modifiedIrrigationRecord);

    /*
     * Si el valor del encabezado de autorizacion de la peticion HTTP
     * dada, tiene un JWT valido, la aplicacion del lado servidor
     * devuelve el mensaje HTTP 200 (Ok) junto con los datos que el
     * cliente solicito modificar
     */
    return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(modifiedIrrigationRecord)).build();
  }

  @DELETE
  @Path("/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response remove(@Context HttpHeaders request, @PathParam("id") int irrigationRecordId) throws IOException {
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

    /*
     * Si el dato solicitado no existe en la base de datos
     * subyacente, la aplicacion del lado servidor devuelve
     * el mensaje HTTP 404 (Not found) junto con el mensaje
     * "Recurso no encontrado" y no se realiza la operacion
     * solicitada
     */
    if (!irrigationRecordService.checkExistence(irrigationRecordId)) {
      return Response.status(Response.Status.NOT_FOUND).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.RESOURCE_NOT_FOUND))).build();
    }

    /*
     * Si al usuario que hizo esta peticion HTTP, no le pertenece
     * el dato solicitado, la aplicacion del lado servidor
     * devuelve el mensaje HTTP 403 (Forbidden) junto con el
     * mensaje "Acceso no autorizado" (contenido en el enum
     * ReasonError) y no se realiza la operacion solicitada
     */
    if (!irrigationRecordService.checkUserOwnership(userId, irrigationRecordId)) {
      return Response.status(Response.Status.FORBIDDEN).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.UNAUTHORIZED_ACCESS))).build();
    }

    /*
     * Si el valor del encabezado de autorizacion de la peticion HTTP
     * dada, tiene un JWT valido, la aplicacion del lado servidor
     * devuelve el mensaje HTTP 200 (Ok) junto con los datos que el
     * cliente solicito eliminar
     */
    return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(irrigationRecordService.remove(userId, irrigationRecordId))).build();
  }

  @POST
  @Path("/fromIrrigationWaterNeedFormData")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response createFromIrrigationWaterNeedFormData(@Context HttpHeaders request, String json) throws IOException {
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

    IrrigationWaterNeedFormData irrigationWaterNeedFormData = mapper.readValue(json, IrrigationWaterNeedFormData.class);
    PlantingRecord developingPlantingRecord = plantingRecordService.findInDevelopment(irrigationWaterNeedFormData.getParcel().getId());

    int developingPlantingRecordId = developingPlantingRecord.getId();
    double irrigationDoneCurrentDate = irrigationWaterNeedFormData.getIrrigationDone();
    double cropIrrigationWaterNeed = irrigationWaterNeedFormData.getCropIrrigationWaterNeed();

    /*
     * Si el riego realizado en la fecha actual (es decir, hoy)
     * [mm/dia] es mayor o igual a la necesidad de agua de riego
     * de un cultivo en la fecha actual [mm/dia], dicha necesidad
     * es igual a 0. En cambio, si el riego realizado en la fecha
     * actual es estrictamente menor a la necesidad de agua de
     * riego de un cultivo en la fecha actual, dicha necesidad es
     * el resultado de la diferencia entre ella y el riego realizado
     * en la fecha actual. Esto se utiliza para actualizar el atributo
     * "necesidad de agua de riego de un cultivo en la fecha actual"
     * del registro de plantacion en desarrollo de una parcela.
     * 
     * Este metodo REST se invoca unicamente para el registro de
     * plantacion en desarrollo de una parcela.
     */
    if (irrigationDoneCurrentDate >= cropIrrigationWaterNeed) {
      plantingRecordService.updateCropIrrigationWaterNeed(developingPlantingRecordId, String.valueOf(0.0));
    } else {
      plantingRecordService.updateCropIrrigationWaterNeed(developingPlantingRecordId, String.valueOf(cropIrrigationWaterNeed - irrigationDoneCurrentDate));
    }

    IrrigationRecord currentIrrigationRecord = new IrrigationRecord();
    currentIrrigationRecord.setDate(UtilDate.getCurrentDate());
    currentIrrigationRecord.setParcel(irrigationWaterNeedFormData.getParcel());
    currentIrrigationRecord.setCrop(irrigationWaterNeedFormData.getCrop());
    currentIrrigationRecord.setIrrigationDone(irrigationWaterNeedFormData.getIrrigationDone());

    /*
     * ******************************************
     * Controles sobre la definicion de los datos
     * ******************************************
     */

    /*
     * Si la fecha de un registro de riego nuevo NO esta
     * definida, la aplicacion del lado servidor retorna el
     * mensaje HTTP 400 (Bad request) junto con el mensaje
     * "La fecha debe estar definida" y no se realiza la
     * operacion solicitada
     */
    if (currentIrrigationRecord.getDate() == null) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.UNDEFINED_DATE))).build();
    }

    /*
     * Si la parcela NO esta definida, la aplicacion del lado
     * servidor retorna el mensaje HTTP 400 (Bad request) junto
     * con el mensaje "La parcela debe estar definida" y no se
     * realiza la operacion solicitada
     */
    if (currentIrrigationRecord.getParcel() == null) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.INDEFINITE_PARCEL))).build();
    }

    /*
     * Si el riego realizado es negativo, la aplicacion del lado
     * servidor retorna el mensaje HTTP 400 (Bad request) junto
     * con el mensaje "El riego realizado debe ser mayor o igual
     * a cero" y no se realiza la operacion solicitada
     */
    if (currentIrrigationRecord.getIrrigationDone() < 0) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.NEGATIVE_REALIZED_IRRIGATION))).build();
    }

    /*
     * Este metodo REST es para crear un registro de riego para una
     * parcela que tiene un cultivo en desarrollo. Por lo tanto, si
     * la parcela para la cual se crea un registro de riego, NO tiene
     * un cultivo en desarrollo (lo cual se representa mediante la
     * inexistencia de un registro de plantacion en desarrollo), la
     * aplicacion del lado servidor retorna el mensaje HTTP 400 (Bad
     * request) junto con el mensaje "No esta permitido crear un
     * registro de riego para una parcela que no tiene un cultivo en
     * desarrollo" y no se realiza la operacion solicitada
     */
    if (!plantingRecordService.checkOneInDevelopment(currentIrrigationRecord.getParcel().getId())) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.THERE_IS_NO_CROP_IN_DEVELOPMENT))).build();
    }

    /*
     * Hay que tener en cuenta que esta instruccion sera ejecutada
     * sin problema alguno siempre y cuando antes se compruebe si la
     * parcela para la que se crea un registro de riego mediante
     * este metodo REST, tiene un cultivo en desarrollo, lo cual
     * se representa mediante un registro de plantacion en desarrollo
     */
    currentIrrigationRecord.setCrop(plantingRecordService.findInDevelopment(irrigationWaterNeedFormData.getParcel().getId()).getCrop());

    /*
     * Persistencia del riego realizado en la fecha actual [mm/dia]
     */
    currentIrrigationRecord = irrigationRecordService.create(currentIrrigationRecord);

    /*
     * Establece el estado del registro de plantacion en desarrollo
     * si y solo si la parcela a la que pertenece tiene la bandera
     * suelo activa en sus opciones. Esto se hace para calcular el
     * estado de dicho registro cada vez que se persiste un riego
     * realizado en el formulario del calculo de la necesidad de
     * agua de riego de un cultivo en la fecha actual.
     */
    setStatusDevelopingPlantingRecord(developingPlantingRecord);

    /*
     * Si el valor del encabezado de autorizacion de la peticion HTTP
     * dada, tiene un JWT valido, la aplicacion del lado servidor
     * devuelve el mensaje HTTP 200 (Ok) junto con los datos que el
     * cliente solicito persistir
     */
    return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(currentIrrigationRecord)).build();
  }

  /**
   * Calcula el estado de un registro de plantacion en desarrollo
   * perteneciente a una parcela que tiene la bandera suelo activa
   * en sus opciones
   * 
   * @param developingPlantingRecord
   */
  private void setStatusDevelopingPlantingRecord(PlantingRecord developingPlantingRecord) {
    Parcel parcel = developingPlantingRecord.getParcel();
    Calendar yesterday = UtilDate.getYesterdayDate();

    /*
     * Calculo del estado del registro de plantacion que tiene
     * el cultivo para el que se calcula la necesidad de agua
     * de riego en la fecha actual (es decir, hoy) [mm/dia]
     */
    String stringAccumulatedWaterDeficitPerDayFromYesterday = soilWaterBalanceService.find(parcel.getId(), yesterday).getAccumulatedWaterDeficitPerDay();
    String notCalculated = soilWaterBalanceService.getNotCalculated();

    /*
     * Si el valor del acumulado del deficit de agua por dia [mm/dia]
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
     * 
     * Si el valor del acumulado del deficit de agua por dia del dia
     * inmediatamente anterior a la fecha actual (es decir, hoy) NO
     * es "NC", significa que es un numero, por lo tanto, se calcula
     * el estado del registro de plantacion perteneciente a una parcela
     * que tiene la bandera suelo activa en sus opciones. Si dicha
     * bandera esta activa, significa que se utilizan datos de suelo
     * para calcular el agua de riego de un cultivo en la fecha actual.
     */
    if (!stringAccumulatedWaterDeficitPerDayFromYesterday.equals(notCalculated)) {
      double totalIrrigationWaterCurrentDate = irrigationRecordService.calculateTotalIrrigationWaterCurrentDate(parcel.getId());
      double accumulatedWaterDeficitPerDayFromYesterday = Double.parseDouble(stringAccumulatedWaterDeficitPerDayFromYesterday);
  
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
        plantingRecordService.setStatus(developingPlantingRecord.getId(),
            statusService.calculateStatusRelatedToSoilMoistureLevel(totalIrrigationWaterCurrentDate,
                accumulatedWaterDeficitPerDayFromYesterday, developingPlantingRecord));
      }

    } // End if

  }

}
