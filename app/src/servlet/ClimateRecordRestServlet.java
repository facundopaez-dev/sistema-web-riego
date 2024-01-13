package servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Collection;
import java.util.Calendar;
import java.util.Date;
import javax.ejb.EJB;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.DELETE;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import model.ClimateRecord;
import model.PlantingRecord;
import model.Parcel;
import stateless.ClimateRecordServiceBean;
import stateless.CropServiceBean;
import stateless.SolarRadiationServiceBean;
import stateless.LatitudeServiceBean;
import stateless.MonthServiceBean;
import stateless.PlantingRecordServiceBean;
import stateless.PlantingRecordStatusServiceBean;
import stateless.SecretKeyServiceBean;
import stateless.SessionServiceBean;
import util.ErrorResponse;
import util.ReasonError;
import util.RequestManager;
import util.UtilDate;
import utilJwt.AuthHeaderManager;
import utilJwt.JwtManager;
import climate.ClimateClient;
import et.Etc;
import et.HargreavesEto;

@Path("/climateRecords")
public class ClimateRecordRestServlet {

  // inject a reference to the ClimateRecordServiceBean slsb
  @EJB ClimateRecordServiceBean climateRecordService;
  @EJB PlantingRecordServiceBean plantingRecordService;
  @EJB CropServiceBean cropService;
  @EJB PlantingRecordStatusServiceBean plantingRecordStatusService;
  @EJB SecretKeyServiceBean secretKeyService;
  @EJB SessionServiceBean sessionService;
  @EJB SolarRadiationServiceBean solarService;
  @EJB LatitudeServiceBean latitudeService;
  @EJB MonthServiceBean monthService;

  // Mapea lista de pojo a JSON
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
      return Response.status(Response.Status.UNAUTHORIZED).entity(new ErrorResponse(ReasonError.NO_ACTIVE_SESSION)).build();
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
      return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorResponse(ReasonError.JWT_NOT_ASSOCIATED_WITH_ACTIVE_SESSION)).build();
    }

    /*
     * Si el valor del encabezado de autorizacion de la peticion HTTP
     * dada, tiene un JWT valido, la aplicacion del lado servidor
     * devuelve el mensaje HTTP 200 (Ok) junto con los datos solicitados
     * por el cliente
     */
    return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(climateRecordService.findAll(userId))).build();
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
      return Response.status(Response.Status.UNAUTHORIZED).entity(new ErrorResponse(ReasonError.NO_ACTIVE_SESSION)).build();
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
      return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorResponse(ReasonError.JWT_NOT_ASSOCIATED_WITH_ACTIVE_SESSION)).build();
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

    Collection<ClimateRecord> climateRecords = null;

    /*
     * Si el nombre de la parcela esta definido y la fecha
     * NO esta definida, la aplicacion del lado servidor
     * retorna una coleccion de registros climaticos
     * pertenecientes a una parcela de un usuario
     */
    if (parcelName != null && stringDate == null) {
      climateRecords = climateRecordService.findAllByParcelName(userId, parcelName);

      /*
       * Actualiza instancias de tipo ClimateRecord desde la base de
       * datos subyacente, sobrescribiendo los cambios realizados en
       * ellas, si los hubiere
       */
      climateRecordService.refreshClimateRecords(climateRecords);
      return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(climateRecords)).build();
    }

    SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

    /*
     * Si la fecha esta definida, la aplicacion del lado servidor
     * la convierte a un objeto de tipo Date que contiene la
     * fecha ingresada por el usuario en el fitro de la pagina
     * web de registros climaticos
     */
    Date date = new Date(dateFormatter.parse(stringDate).getTime());
    climateRecords = climateRecordService.findAllByParcelNameAndDate(userId, parcelName, UtilDate.toCalendar(date));

    /*
     * Actualiza instancias de tipo ClimateRecord desde la base de
     * datos subyacente, sobrescribiendo los cambios realizados en
     * ellas, si los hubiere
     */
    climateRecordService.refreshClimateRecords(climateRecords);

    /*
     * Si el valor del encabezado de autorizacion de la peticion HTTP
     * dada, tiene un JWT valido, la aplicacion del lado servidor
     * devuelve el mensaje HTTP 200 (Ok) junto con los datos solicitados
     * por el cliente
     */
    return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(climateRecords)).build();
  }

  @GET
  @Path("/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response find(@Context HttpHeaders request, @PathParam("id") int climateRecordId) throws IOException {
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
     * Si el dato solicitado no existe en la base de datos
     * subyacente, la aplicacion del lado servidor devuelve
     * el mensaje HTTP 404 (Not found) junto con el mensaje
     * "Recurso no encontrado" y no se realiza la operacion
     * solicitada
     */
    if (!climateRecordService.checkExistence(climateRecordId)) {
      return Response.status(Response.Status.NOT_FOUND).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.RESOURCE_NOT_FOUND))).build();
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
      return Response.status(Response.Status.UNAUTHORIZED).entity(new ErrorResponse(ReasonError.NO_ACTIVE_SESSION)).build();
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
      return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorResponse(ReasonError.JWT_NOT_ASSOCIATED_WITH_ACTIVE_SESSION)).build();
    }

    /*
     * Si al usuario que hizo esta peticion HTTP, no le pertenece
     * el registro climatico solicitado (debido a que ninguna de
     * sus parcelas tiene el registro climatico solicitado), la
     * aplicacion del lado servidor devuelve el mensaje HTTP 403
     * (Forbidden) junto con el mensaje "Acceso no autorizado"
     * (contenido en el enum ReasonError) y no se realiza la
     * operacion solicitada
     */
    if (!climateRecordService.checkUserOwnership(userId, climateRecordId)) {
      return Response.status(Response.Status.FORBIDDEN).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.UNAUTHORIZED_ACCESS))).build();
    }

    /*
     * Si el valor del encabezado de autorizacion de la peticion HTTP
     * dada, tiene un JWT valido, la aplicacion del lado servidor
     * devuelve el mensaje HTTP 200 (Ok) junto con los datos solicitados
     * por el cliente
     */
    return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(climateRecordService.findByUserId(userId, climateRecordId))).build();
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
      return Response.status(Response.Status.UNAUTHORIZED).entity(new ErrorResponse(ReasonError.NO_ACTIVE_SESSION)).build();
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
      return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorResponse(ReasonError.JWT_NOT_ASSOCIATED_WITH_ACTIVE_SESSION)).build();
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

    ClimateRecord newClimateRecord = mapper.readValue(json, ClimateRecord.class);

    /*
     * Si la fecha NO esta definida, la aplicacion del lado
     * servidor retorna el mensaje HTTP 400 (Bad request)
     * junto con el mensaje "La fecha debe estar definida"
     * y no se realiza la operacion solicitada
     */
    if (newClimateRecord.getDate() == null) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.UNDEFINED_DATE))).build();
    }

    /*
     * Si la velocidad del viento tiene un valor menor a 0.0,
     * la aplicacion del lado servidor retorna el mensaje
     * HTTP 400 (Bad request) junto con el mensaje "La
     * velocidad del viento debe ser un valor mayor o
     * igual a 0.0" y no se realiza la operacion solicitada
     */
    if (newClimateRecord.getWindSpeed() < 0.0) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.INVALID_WIND_SPEED))).build();
    }

    /*
     * Si la probabilidad de la precipitacion tiene un valor
     * menor a 0.0 o mayor a 100, la aplicacion del lado
     * servidor retorna el mensaje HTTP 400 (Bad request) junto
     * con el mensaje "La probabilidad de la precipitacion debe
     * ser un valor mayor o igual a 0.0" y no se realiza la
     * operacion solicitada
     */
    if (newClimateRecord.getPrecipProbability() < 0.0 || newClimateRecord.getPrecipProbability() > 100) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.INVALID_PRECIPITATION_PROBABILITY))).build();
    }

    /*
     * Si la precipitacion tiene un valor menor a 0.0, la
     * aplicacion del lado servidor retorna el mensaje
     * HTTP 400 (Bad request) junto con el mensaje "La
     * precipitacion debe ser un valor mayor o igual a
     * 0.0" y no se realiza la operacion solicitada
     */
    if (newClimateRecord.getPrecip() < 0.0) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.INVALID_PRECIPITATION))).build();
    }

    /*
     * Si la nubosidad tiene un valor menor a 0.0 o mayor
     * a 100, la aplicacion del lado servidor retorna el
     * mensaje HTTP 400 (Bad request) junto con el mensaje
     * "La nubosidad debe ser un valor entre 0.0 y 100,
     * incluido" y no se realiza la operacion solicitada
     */
    if (newClimateRecord.getCloudCover() < 0.0 || newClimateRecord.getCloudCover() > 100) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.INVALID_CLOUDINESS))).build();
    }

    /*
     * Si la presion atmosferica tiene un valor menor o igual
     * a 0.0, la aplicacion del lado servidor retorna el mensaje
     * HTTP 400 (Bad request) junto con el mensaje "La presion
     * atmosferica debe ser un valor mayor a 0.0"
     */
    if (newClimateRecord.getAtmosphericPressure() <= 0.0) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.INVALID_ATMOSPHERIC_PRESSURE))).build();
    }

    /*
     * Si la parcela NO esta definida, la aplicacion del
     * lado servidor retorna el mensaje HTTP 400 (Bad
     * request) junto con el mensaje "La parcela debe
     * estar definida" y no se realiza la operacion
     * solicitada
     */
    if (newClimateRecord.getParcel() == null) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.INDEFINITE_PARCEL))).build();
    }

    /*
     * Si el registro climatico a crear para una parcela,
     * tiene una fecha igual a la fecha de otro registro
     * climatico de la misma parcela, la aplicacion del
     * lado servidor retorna el mensaje HTTP 400 (Bad
     * request) junto con el mensaje "Ya existe un registro
     * climatico con la fecha ingresada para la parcela
     * seleccionada" y no se realiza la operacion solicitada
     */
    if (climateRecordService.checkExistence(newClimateRecord.getDate(), newClimateRecord.getParcel())) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.EXISTING_CLIMATE_RECORD))).build();
    }

    /*
     * Calculo de la ETo (evapotranspiracion del cultivo
     * de referencia) [mm/dia] para el registro climatico
     */
    Parcel givenParcel = newClimateRecord.getParcel();
    double extraterrestrialSolarRadiation = solarService.getRadiation(givenParcel.getLatitude(),
        monthService.getMonth(newClimateRecord.getDate().get(Calendar.MONTH)), latitudeService.find(givenParcel.getLatitude()),
        latitudeService.findPreviousLatitude(givenParcel.getLatitude()),
        latitudeService.findNextLatitude(givenParcel.getLatitude()));

    double eto = HargreavesEto.calculateEto(newClimateRecord.getMaximumTemperature(), newClimateRecord.getMinimumTemperature(), extraterrestrialSolarRadiation);

    /*
     * Calculo de la ETc (evapotranspiracion del cultivo
     * bajo condiciones estandar) [mm/dia] para el registro
     * climatico
     */
    PlantingRecord givenPlantingRecord = null;
    double etc = 0.0;

    /*
     * Si una parcela tiene un registro de plantacion que tiene
     * un periodo, el cual esta definido por una fecha de siembra
     * y una fecha de cosecha, dentro del cual esta la fecha de
     * un registro climatico, y si este registro de plantacion
     * tiene el estado "En espera", NO se calcula la ETc del
     * cultivo correspondiente a este registro de plantacion,
     * ya que NO es logico calcular la ETc de un cultivo perteneciente
     * a un registro de plantacion que tiene el estado "En espera".
     * Un registro de plantacion que tiene el estado "En espera"
     * representa la planificacion de la siembra de un cultivo,
     * por lo tanto, NO es logico calcular la ETc de un cultivo
     * que esta planificado a ser sembrado.
     */
    if (plantingRecordService.checkExistence(givenParcel, newClimateRecord.getDate())) {
      givenPlantingRecord = plantingRecordService.find(givenParcel, newClimateRecord.getDate());

      /*
       * Si un registro de plantacion que tiene un periodo, el
       * cual esta definido por una fecha de siembra y una fecha
       * de cosecha, dentro del cual esta la fecha de un registro
       * climatico, tiene el estado "Finalizado", se calcula la
       * ETc del cultivo correspondiente a este registro de
       * plantacion, ya que la existencia de un registro de
       * plantacion finalizado representa la situacion en la que
       * una parcela tuvo un cultivo sembrado en una fecha, el
       * cual luego de un tiempo fue cosechado
       */
      if (plantingRecordStatusService.equals(givenPlantingRecord.getStatus(), plantingRecordStatusService.findFinishedStatus())) {
        etc = Etc.calculateEtc(eto, cropService.getKc(givenPlantingRecord.getCrop(), givenPlantingRecord.getSeedDate(), newClimateRecord.getDate()));
      }

      /*
       * Si un registro de plantacion que tiene un periodo, el
       * cual esta definido por una fecha de siembra y una fecha
       * de cosecha, dentro del cual esta la fecha de un registro
       * climatico, tiene un estado en desarrollo (en desarrollo,
       * desarrollo optimo, desarrollo en riesgo de marchitez o
       * desarrollo en marchitez), se calcula la ETc del cultivo
       * correspondiente a este registro de plantacion, ya que la
       * existencia de un registro de plantacion en desarrollo
       * representa la situacion en la que una parcela tiene un
       * cultivo sembrado y en desarrollo
       */
      if (plantingRecordStatusService.equals(givenPlantingRecord.getStatus(), plantingRecordStatusService.findInDevelopmentStatus())
          || plantingRecordStatusService.equals(givenPlantingRecord.getStatus(), plantingRecordStatusService.findOptimalDevelopmentStatus())
          || plantingRecordStatusService.equals(givenPlantingRecord.getStatus(), plantingRecordStatusService.findDevelopmentAtRiskWiltingStatus())
          || plantingRecordStatusService.equals(givenPlantingRecord.getStatus(), plantingRecordStatusService.findDevelopmentInWitheringStatus())) {
        etc = Etc.calculateEtc(eto, cropService.getKc(givenPlantingRecord.getCrop(), givenPlantingRecord.getSeedDate()));
      }

    }

    /*
     * Asignacion de la ETo y la ETc al registro climatico
     */
    newClimateRecord.setEto(eto);
    newClimateRecord.setEtc(etc);

    /*
     * Si el valor del encabezado de autorizacion de la peticion HTTP
     * dada, tiene un JWT valido, la aplicacion del lado servidor
     * devuelve el mensaje HTTP 200 (Ok) junto con los datos que el
     * cliente solicito persistir
     */
    return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(climateRecordService.create(newClimateRecord))).build();
  }

  @PUT
  @Path("/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response modify(@Context HttpHeaders request, @PathParam("id") int climateRecordId, String json) throws IOException {
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
      return Response.status(Response.Status.UNAUTHORIZED).entity(new ErrorResponse(ReasonError.NO_ACTIVE_SESSION)).build();
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
      return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorResponse(ReasonError.JWT_NOT_ASSOCIATED_WITH_ACTIVE_SESSION)).build();
    }

    /*
     * Si el dato solicitado no existe en la base de datos
     * subyacente, la aplicacion del lado servidor devuelve
     * el mensaje HTTP 404 (Not found) junto con el mensaje
     * "Recurso no encontrado" y no se realiza la operacion
     * solicitada
     */
    if (!climateRecordService.checkExistence(climateRecordId)) {
      return Response.status(Response.Status.NOT_FOUND).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.RESOURCE_NOT_FOUND))).build();
    }

    /*
     * Si al usuario que hizo esta peticion HTTP, no le pertenece
     * el registro climatico solicitado (debido a que ninguna de
     * sus parcelas tiene el registro climatico solicitado), la
     * aplicacion del lado servidor devuelve el mensaje HTTP 403
     * (Forbidden) junto con el mensaje "Acceso no autorizado"
     * (contenido en el enum ReasonError) y no se realiza la
     * operacion solicitada
     */
    if (!climateRecordService.checkUserOwnership(userId, climateRecordId)) {
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

    ClimateRecord modifiedClimateRecord = mapper.readValue(json, ClimateRecord.class);

    /*
     * Si la fecha NO esta definida, la aplicacion del lado
     * servidor retorna el mensaje HTTP 400 (Bad request)
     * junto con el mensaje "La fecha debe estar definida"
     * y no se realiza la operacion solicitada
     */
    if (modifiedClimateRecord.getDate() == null) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.UNDEFINED_DATE))).build();
    }

    /*
     * Si la velocidad del viento tiene un valor menor a 0.0,
     * la aplicacion del lado servidor retorna el mensaje
     * HTTP 400 (Bad request) junto con el mensaje "La
     * velocidad del viento debe ser un valor mayor o
     * igual a 0.0" y no se realiza la operacion solicitada
     */
    if (modifiedClimateRecord.getWindSpeed() < 0.0) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.INVALID_WIND_SPEED))).build();
    }

    /*
     * Si la probabilidad de la precipitacion tiene un valor
     * menor a 0.0 o mayor a 100, la aplicacion del lado
     * servidor retorna el mensaje HTTP 400 (Bad request) junto
     * con el mensaje "La probabilidad de la precipitacion debe
     * ser un valor mayor o igual a 0.0" y no se realiza la
     * operacion solicitada
     */
    if (modifiedClimateRecord.getPrecipProbability() < 0.0 || modifiedClimateRecord.getPrecipProbability() > 100) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.INVALID_PRECIPITATION_PROBABILITY))).build();
    }

    /*
     * Si la precipitacion tiene un valor menor a 0.0, la
     * aplicacion del lado servidor retorna el mensaje
     * HTTP 400 (Bad request) junto con el mensaje "La
     * precipitacion debe ser un valor mayor o igual a
     * 0.0" y no se realiza la operacion solicitada
     */
    if (modifiedClimateRecord.getPrecip() < 0.0) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.INVALID_PRECIPITATION))).build();
    }

    /*
     * Si la nubosidad tiene un valor menor a 0.0 o mayor
     * a 100, la aplicacion del lado servidor retorna el
     * mensaje HTTP 400 (Bad request) junto con el mensaje
     * "La nubosidad debe ser un valor entre 0.0 y 100,
     * incluido" y no se realiza la operacion solicitada
     */
    if (modifiedClimateRecord.getCloudCover() < 0.0 || modifiedClimateRecord.getCloudCover() > 100) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.INVALID_CLOUDINESS))).build();
    }

    /*
     * Si la presion atmosferica tiene un valor menor o igual
     * a 0.0, la aplicacion del lado servidor retorna el mensaje
     * HTTP 400 (Bad request) junto con el mensaje "La presion
     * atmosferica debe ser un valor mayor a 0.0"
     */
    if (modifiedClimateRecord.getAtmosphericPressure() <= 0.0) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.INVALID_ATMOSPHERIC_PRESSURE))).build();
    }

    /*
     * Si la parcela NO esta definida, la aplicacion del
     * lado servidor retorna el mensaje HTTP 400 (Bad
     * request) junto con el mensaje "La parcela debe
     * estar definida" y no se realiza la operacion
     * solicitada
     */
    if (modifiedClimateRecord.getParcel() == null) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.INDEFINITE_PARCEL))).build();
    }

    /*
     * Si el registro climatico a modificar de una parcela,
     * tiene una fecha igual a la fecha de otro registro
     * climatico de la misma parcela, la aplicacion del
     * lado servidor retorna el mensaje HTTP 400 (Bad
     * request) junto con el mensaje "Ya existe un registro
     * climatico con la fecha ingresada para la parcela
     * seleccionada" y no se realiza la operacion solicitada
     */
    if (climateRecordService.checkRepeated(climateRecordId, modifiedClimateRecord.getParcel(), modifiedClimateRecord.getDate())) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.EXISTING_CLIMATE_RECORD))).build();
    }

    /*
     * Calculo de la ETo (evapotranspiracion del cultivo
     * de referencia) [mm/dia] para el registro climatico
     */
    Parcel givenParcel = modifiedClimateRecord.getParcel();
    double extraterrestrialSolarRadiation = solarService.getRadiation(givenParcel.getLatitude(),
        monthService.getMonth(modifiedClimateRecord.getDate().get(Calendar.MONTH)), latitudeService.find(givenParcel.getLatitude()),
        latitudeService.findPreviousLatitude(givenParcel.getLatitude()),
        latitudeService.findNextLatitude(givenParcel.getLatitude()));

    double eto = HargreavesEto.calculateEto(modifiedClimateRecord.getMaximumTemperature(), modifiedClimateRecord.getMinimumTemperature(), extraterrestrialSolarRadiation);

    /*
     * Calculo de la ETc (evapotranspiracion del cultivo
     * bajo condiciones estandar) [mm/dia] para el registro
     * climatico
     */
    PlantingRecord givenPlantingRecord = null;
    double etc = 0.0;

    /*
     * Si una parcela tiene un registro de plantacion que tiene
     * un periodo, el cual esta definido por una fecha de siembra
     * y una fecha de cosecha, dentro del cual esta la fecha de
     * un registro climatico, y si este registro de plantacion
     * tiene el estado "En espera", NO se calcula la ETc del
     * cultivo correspondiente a este registro de plantacion,
     * ya que NO es logico calcular la ETc de un cultivo perteneciente
     * a un registro de plantacion que tiene el estado "En espera".
     * Un registro de plantacion que tiene el estado "En espera"
     * representa la planificacion de la siembra de un cultivo,
     * por lo tanto, NO es logico calcular la ETc de un cultivo
     * que esta planificado a ser sembrado.
     */
    if (plantingRecordService.checkExistence(givenParcel, modifiedClimateRecord.getDate())) {
      givenPlantingRecord = plantingRecordService.find(givenParcel, modifiedClimateRecord.getDate());

      /*
       * Si un registro de plantacion que tiene un periodo, el
       * cual esta definido por una fecha de siembra y una fecha
       * de cosecha, dentro del cual esta la fecha de un registro
       * climatico, tiene el estado "Finalizado", se calcula la
       * ETc del cultivo correspondiente a este registro de
       * plantacion, ya que la existencia de un registro de
       * plantacion finalizado representa la situacion en la que
       * una parcela tuvo un cultivo sembrado en una fecha, el
       * cual luego de un tiempo fue cosechado
       */
      if (plantingRecordStatusService.equals(givenPlantingRecord.getStatus(), plantingRecordStatusService.findFinishedStatus())) {
        etc = Etc.calculateEtc(eto, cropService.getKc(givenPlantingRecord.getCrop(), givenPlantingRecord.getSeedDate(), modifiedClimateRecord.getDate()));
      }

      /*
       * Si un registro de plantacion que tiene un periodo, el
       * cual esta definido por una fecha de siembra y una fecha
       * de cosecha, dentro del cual esta la fecha de un registro
       * climatico, tiene un estado en desarrollo (en desarrollo,
       * desarrollo optimo, desarrollo en riesgo de marchitez o
       * desarrollo en marchitez), se calcula la ETc del cultivo
       * correspondiente a este registro de plantacion, ya que la
       * existencia de un registro de plantacion en desarrollo
       * representa la situacion en la que una parcela tiene un
       * cultivo sembrado y en desarrollo
       */
      if (plantingRecordStatusService.equals(givenPlantingRecord.getStatus(), plantingRecordStatusService.findInDevelopmentStatus())
          || plantingRecordStatusService.equals(givenPlantingRecord.getStatus(), plantingRecordStatusService.findOptimalDevelopmentStatus())
          || plantingRecordStatusService.equals(givenPlantingRecord.getStatus(), plantingRecordStatusService.findDevelopmentAtRiskWiltingStatus())
          || plantingRecordStatusService.equals(givenPlantingRecord.getStatus(), plantingRecordStatusService.findDevelopmentInWitheringStatus())) {
        etc = Etc.calculateEtc(eto, cropService.getKc(givenPlantingRecord.getCrop(), givenPlantingRecord.getSeedDate()));
      }

    }

    /*
     * Asignacion de la ETo y la ETc al registro climatico
     */
    modifiedClimateRecord.setEto(eto);
    modifiedClimateRecord.setEtc(etc);

    /*
     * Si el valor del encabezado de autorizacion de la peticion HTTP
     * dada, tiene un JWT valido, la aplicacion del lado servidor
     * devuelve el mensaje HTTP 200 (Ok) junto con los datos que el
     * cliente solicito actualizar
     */
    return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(climateRecordService.modify(userId, climateRecordId, modifiedClimateRecord))).build();
  }

  @DELETE
  @Path("/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response remove(@Context HttpHeaders request, @PathParam("id") int climateRecordId) throws IOException {
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
      return Response.status(Response.Status.UNAUTHORIZED).entity(new ErrorResponse(ReasonError.NO_ACTIVE_SESSION)).build();
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
      return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorResponse(ReasonError.JWT_NOT_ASSOCIATED_WITH_ACTIVE_SESSION)).build();
    }

    /*
     * Si el dato solicitado no existe en la base de datos
     * subyacente, la aplicacion del lado servidor devuelve
     * el mensaje HTTP 404 (Not found) junto con el mensaje
     * "Recurso no encontrado" y no se realiza la operacion
     * solicitada
     */
    if (!climateRecordService.checkExistence(climateRecordId)) {
      return Response.status(Response.Status.NOT_FOUND).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.RESOURCE_NOT_FOUND))).build();
    }

    /*
     * Si al usuario que hizo esta peticion HTTP, no le pertenece
     * el dato solicitado, la aplicacion del lado servidor
     * devuelve el mensaje HTTP 403 (Forbidden) junto con el
     * mensaje "Acceso no autorizado" (contenido en el enum
     * ReasonError) y no se realiza la operacion solicitada
     */
    if (!climateRecordService.checkUserOwnership(userId, climateRecordId)) {
      return Response.status(Response.Status.FORBIDDEN).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.UNAUTHORIZED_ACCESS))).build();
    }

    /*
     * Si el valor del encabezado de autorizacion de la peticion HTTP
     * dada, tiene un JWT valido, la aplicacion del lado servidor
     * devuelve el mensaje HTTP 200 (Ok) junto con los datos que el
     * cliente solicito eliminar
     */
    return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(climateRecordService.remove(userId, climateRecordId))).build();
  }

  /**
   * Este metodo REST esta para probar que la invocacion a la API
   * climatica Visual Crossing Weather funciona como corresponde
   * 
   * @param latitude
   * @param longitude
   * @param time
   * @return conjunto de datos meteorologicos obtenidos en un dia
   *         para una ubicacion geografica
   * @throws IOException
   */
  @GET
  @Path("/apiCallTest")
  @Produces(MediaType.APPLICATION_JSON)
	public String getForecast(@QueryParam("latitude") double latitude, @QueryParam("longitude") double longitude, @QueryParam("time") long time) throws IOException {
    Parcel givenParcel = new Parcel();
    givenParcel.setLatitude(latitude);
    givenParcel.setLongitude(longitude);
    return mapper.writeValueAsString(ClimateClient.getForecast(givenParcel, time));
	}

}
