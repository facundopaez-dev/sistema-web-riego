package servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Calendar;
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

  /*
   * El valor de esta constante se asigna a la necesidad de
   * agua de riego [mm/dia] de un registro de plantacion
   * para el que no se puede calcular dicha necesidad, lo cual,
   * ocurre cuando no se tiene la evapotranspiracion del cultivo
   * bajo condiciones estandar (ETc) [mm/dia] ni la precipitacion
   * [mm/dia], siendo ambos valores de la fecha actual.
   * 
   * La abreviatura "n/a" significa "no disponible".
   */
  private final String NOT_AVAILABLE = "n/a";
  private final String IRRIGATION_WATER_NEED_NOT_AVAILABLE_BUT_CALCULABLE = "-";

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
    return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(plantingRecordService.findAll(userId))).build();
  }

  @GET
  @Path("/findAllByParcelName/{parcelName}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response findAllByParcelName(@Context HttpHeaders request, @PathParam("parcelName") String givenParcelName) throws IOException {
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
    return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(plantingRecordService.findAllByParcelName(userId, givenParcelName))).build();
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
     * Se establece el estado del nuevo registro de plantacion
     * en base a la fecha de siembra y la fecha de cosecha de
     * su cultivo
     */
    newPlantingRecord.setStatus(statusService.calculateStatus(newPlantingRecord));

    PlantingRecordStatus statusNewPlantingRecord = newPlantingRecord.getStatus();
    PlantingRecordStatus finishedStatus = statusService.findFinishedStatus();
    PlantingRecordStatus developmentStatus = statusService.findDevelopmentStatus();
    PlantingRecordStatus waitingStatus = statusService.findWaitingStatus();

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
      newPlantingRecord.setIrrigationWaterNeed(NOT_AVAILABLE);
    }

    /*
     * Inicialmente un registro de plantacion que tiene el estado
     * "En desarrollo" NO tiene la necesidad de agua de riego de
     * un cultivo en la fecha actual [mm/dia], lo cual se realiza
     * para hacer que el usuario ejecute el proceso del calculo de
     * la necesidad de agua de riego de un cultivo en la fecha actual
     * [mm/dia]. La manera en la que el usuario realiza esto es
     * mediante el boton "Calcular" de la pagina de registros de
     * plantacion.
     */
    if (statusService.equals(statusNewPlantingRecord, developmentStatus)) {
      newPlantingRecord.setIrrigationWaterNeed(IRRIGATION_WATER_NEED_NOT_AVAILABLE_BUT_CALCULABLE);
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
  @Path("/{id}/{maintainWitheredStatus}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response modify(@Context HttpHeaders request, @PathParam("id") int plantingRecordId,
      @PathParam("maintainWitheredStatus") boolean maintainWitheredStatus, String json) throws IOException {
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

    Calendar seedDate = modifiedPlantingRecord.getSeedDate();
    Calendar harvestDate = modifiedPlantingRecord.getHarvestDate();

    PlantingRecordStatus currentStatus = currentPlantingRecord.getStatus();
    PlantingRecordStatus finishedStatus = statusService.findFinishedStatus();
    PlantingRecordStatus developmentStatus = statusService.findDevelopmentStatus();
    PlantingRecordStatus waitingStatus = statusService.findWaitingStatus();

    /*
     * Si el registro de plantacion modificado tiene el atributo
     * modifiable en false y si el registro de plantacion correspondiente
     * al registro de plantacion modificado tiene el estado "En
     * desarrollo" o el estado "En espera", la aplicacion del lado
     * servidor retorna el mensaje HTTP 400 (Bad request) junto con
     * el mensaje "No esta permitido hacer que un registro de
     * plantacion en desarrollo o en espera sea no modificable" y
     * no se realiza la operacion solicitada
     */
    if (!modifiedPlantingRecord.getModifiable() && (statusService.equals(currentStatus, developmentStatus) || statusService.equals(currentStatus, waitingStatus))) {
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
    if (seedDate == null) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.UNDEFINED_SEED_DATE))).build();
    }

    /*
     * Si la fecha de cosecha de un registro de plantacion modificado
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
        && (plantingRecordService.checkOneInDevelopment(modifiedPlantingRecord.getParcel()))) {
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
    if (!modifiedPlantingRecord.getIrrigationWaterNeed().equals(currentPlantingRecord.getIrrigationWaterNeed())) {
      return Response.status(Response.Status.BAD_REQUEST)
        .entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.MODIFICATION_IRRIGATION_WATER_NEED_NOT_ALLOWED))).build();
    }

    /*
     * Si un registro de plantacion a modificar tiene el estado
     * marchitado y NO se desea que mantenga ese estado luego
     * de su modificado, se calcula su proximo estado
     */
    if (!maintainWitheredStatus) {
      /*
       * Se establece el estado de un registro de plantacion
       * modificado en base a la fecha de siembra y la fecha de
       * cosecha de su cultivo
       */
      modifiedPlantingRecord.setStatus(statusService.calculateStatus(modifiedPlantingRecord));
      plantingRecordService.unsetWiltingDate(modifiedPlantingRecord.getId());
    }

    PlantingRecordStatus modifiedPlantingRecordStatus = modifiedPlantingRecord.getStatus();

    /*
     * Un registro de plantacion tiene el estado "Finalizado"
     * cuando es del pasado (es decir, tanto su fecha de siembra
     * como su fecha de cosecha son estrictamente menores a la
     * fecha actual).
     * 
     * Un registro de plantacion tiene el estado "En espera"
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
    if (statusService.equals(modifiedPlantingRecordStatus, finishedStatus) || (statusService.equals(modifiedPlantingRecordStatus, waitingStatus))) {
      modifiedPlantingRecord.setIrrigationWaterNeed(NOT_AVAILABLE);
    }

    /*
     * Si el registro de plantacion modificado tiene el estado "En
     * desarrollo" y tiene una parcela o un cultivo distinto a los
     * originales, se asigna el caracter "-" a la necesidad de agua
     * de riego de dicho registro para hacer que el usuario ejecute
     * el proceso del calculo de la necesidad de agua de riego de
     * un cultivo en la fecha actual [mm/dia]. La manera en la que
     * el usuario realiza esto es mediante el boton "Calcular" de
     * la pagina de registros de plantacion. Tambien se asigna el
     * caracter "-" a la necesidad de agua de riego de un cultivo
     * en la fecha actual de un registro de plantacion en desarrollo
     * perteneciente a una parcela a la que se le modifica el
     * suelo. Esto esta programado en el metodo modify de la clase
     * ParcelRestServlet.
     */
    if (statusService.equals(modifiedPlantingRecordStatus, developmentStatus)
        && (!parcelService.equals(modifiedParcel, currentParcel) || !cropService.equals(modifiedCrop, currentCrop))) {
      modifiedPlantingRecord.setIrrigationWaterNeed(IRRIGATION_WATER_NEED_NOT_AVAILABLE_BUT_CALCULABLE);
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
  @Path("/irrigationWaterNeed/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getIrrigationWaterNeed(@Context HttpHeaders request, @PathParam("id") int plantingRecordId) throws IOException {
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
     * finalizado, en espera o marchitado, la aplicacion del lado
     * servidor retorna el mensaje HTTP 400 (Bad request) junto
     * con el mensaje "No esta permitido calcular la necesidad
     * de agua de riego de un cultivo finalizado, en espera o
     * marchitado" y no se realiza la operacion solicitada
     */
    if (plantingRecordService.checkFinishedStatus(plantingRecordId)
        || plantingRecordService.checkWaitingStatus(plantingRecordId)
        || plantingRecordService.checkWitheredStatus(plantingRecordId)) {
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
    Parcel givenParcel = developingPlantingRecord.getParcel();
    Option parcelOption = givenParcel.getOption();

    double etcCurrentDate = 0.0;

    /*
     * Si la parcela de un registro de plantacion tiene la bandera
     * suelo activa, se calcula la lamina de riego optima (drop)
     * (umbral de riego) [mm] de la fecha actual y la lamina total
     * de agua disponible (dt) [mm] y se las asigna al registro de
     * plantacion en desarrollo.
     * 
     * La lamina de riego optima (drop) [mm] es de la fecha actual
     * porque el factor de agotamiento (p) con la que se la calcula
     * se debe ajustar a la ETc de la fecha actual, ya que se busca
     * calcular la necesidad de agua de riego de un cultivo en la
     * fecha actual [mm/dia]. 
     */
    if (parcelOption.getSoilFlag()) {
      double etoCurrentDate = 0.0;

      Crop givenCrop = developingPlantingRecord.getCrop();
      ClimateRecord currentClimateRecord;

      /*
       * Si en la base de datos subyacente existe para una parcela
       * el registro climatico de la fecha actual, se obtiene su
       * ETc para calcuar la lamina de riego optima (drop). De lo
       * contrario, se lo solicita al servicio climatico, se lo
       * persiste y se obtiene su ETc para calcular dicha lamina.
       */
      if (climateRecordService.checkExistence(UtilDate.getCurrentDate(), givenParcel)) {
        currentClimateRecord = climateRecordService.find(UtilDate.getCurrentDate(), givenParcel);
        etcCurrentDate = currentClimateRecord.getEtc();
      } else {
        currentClimateRecord  = ClimateClient.getForecast(givenParcel, UtilDate.getCurrentDate().getTimeInMillis() / 1000);

        double extraterrestrialSolarRadiation = solarService.getRadiation(givenParcel.getLatitude(),
        monthService.getMonth(currentClimateRecord.getDate().get(Calendar.MONTH)), latitudeService.find(givenParcel.getLatitude()),
        latitudeService.findPreviousLatitude(givenParcel.getLatitude()),
        latitudeService.findNextLatitude(givenParcel.getLatitude()));

        etoCurrentDate = HargreavesEto.calculateEto(currentClimateRecord.getMaximumTemperature(), currentClimateRecord.getMinimumTemperature(), extraterrestrialSolarRadiation);
        etcCurrentDate = Etc.calculateEtc(etoCurrentDate, cropService.getKc(givenCrop, developingPlantingRecord.getSeedDate()));

        currentClimateRecord.setEto(etoCurrentDate);
        currentClimateRecord.setEtc(etcCurrentDate);

        /*
         * Persistencia del registro climatico de la fecha actual (hoy)
         */
        climateRecordService.create(currentClimateRecord);
      }

      /*
       * Actualizacion de la lamina total de agua disponible (dt) [mm]
       * de un registro de plantacion en la base de datos subyacente
       */
      plantingRecordService.updateTotalAmountWaterAvailable(developingPlantingRecord.getId(),
          WaterMath.calculateTotalAmountWaterAvailable(givenCrop, givenParcel.getSoil()));

      /*
       * Actualizacion de la lamina de riego optima (drop) [mm] de
       * un registro de plantacion en la base de datos subyacente.
       * A esta se le asigna el signo negativo (-) porque representa
       * la cantidad maxima de agua que puede perder un suelo, que
       * tiene un cultivo sembrado, a partir de la cual NO conviene
       * perder mas agua, sino que se le debe añadir agua
       */
      plantingRecordService.updateOptimalIrrigationLayer(developingPlantingRecord.getId(),
          (-1 * WaterMath.calculateOptimalIrrigationLayer(etcCurrentDate, givenCrop, givenParcel.getSoil())));
    }

    /*
     * Ejecuta el proceso del calculo de la necesidad de agua
     * de riego de un cultivo en la fecha actual [mm/dia]. Esto
     * es que ejecuta los metodos necesarios para calcular y
     * actualizar la necesidad de agua de riego de un cultivo
     * (en desarrollo) en la fecha actual.
     */
    double irrigationWaterNeedCurrentDate = runCalculationIrrigationWaterNeedCurrentDate(userService.find(userId), developingPlantingRecord);

    /*
     * Si la necesidad de agua de riego de un cultivo (en
     * desarrollo) en la fecha actual [mm/dia] es negativa
     * significa dos cosas:
     * - que el algoritmo utilizado para calcular dicha
     * necesidad es el que utiliza datos de suelo para ello,
     * ya que este es el unico de los dos algoritmos del
     * calculo de la necesidad de agua de riego de un cultivo
     * en una fecha [mm/dia] que puede retornar -1,
     * - y que el nivel de humedad del suelo de una parcela
     * que tiene un cultivo sembrado y en desarrollo, esta
     * en el punto de marchitez permanente, en el cual un
     * cultivo no puede extraer agua del suelo y no puede
     * recuperarse de la perdida hidrica aunque la humedad
     * ambiental sea saturada.
     * 
     * Por lo tanto, la aplicacion del lado servidor asigna
     * la abreviatura "n/a" (no disponible) a la necesidad
     * de agua de riego de un cultivo en la fecha actual
     * [mm/dia] de un registro de plantacion en desarrollo
     * y retorna el mensaje HTTP 400 (Bad request) junto con
     * el mensaje dado, y no se realiza la operacion solicitada.
     */
    if (irrigationWaterNeedCurrentDate < 0.0) {
      plantingRecordService.updateIrrigationWaterNeed(developingPlantingRecord.getId(), developingPlantingRecord.getParcel(), NOT_AVAILABLE);
      plantingRecordService.setStatus(developingPlantingRecord.getId(), statusService.findWitheredStatus());
      plantingRecordService.updateWiltingDate(developingPlantingRecord.getId(), UtilDate.getCurrentDate());

      String message = "Utilizando los datos climáticos y de riego de " + parcelOption.getPastDaysReference()
          + " (valor de las opciones de la parcela) días previos a la fecha actual,"
          + " el cultivo se ha marchitado porque el nivel de humedad del suelo, en el que está sembrado, está en el punto de marchitez permanente."
          + " Esto se debe a que el nivel de humedad del suelo es menor a la capacidad de almacenamiento de agua del suelo.";

      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(message, SourceUnsatisfiedResponse.WATER_NEED_CROP))).build();
    }

    /*
     * *****************************************************
     * Actualizacion del atributo "necesidad agua riego" del
     * registro de plantacion en desarrollo, que tiene el
     * cultivo para el que se solicita calcular su necesidad
     * de agua de riego en la fecha actual [mm/dia], con el
     * valor de de dicha necesidad de agua de riego
     * *****************************************************
     */
    plantingRecordService.updateIrrigationWaterNeed(developingPlantingRecord.getId(), developingPlantingRecord.getParcel(), String.valueOf(irrigationWaterNeedCurrentDate));

    /*
     * Datos del formulario del calculo de la necesidad de
     * agua de riego de un cultivo en desarrollo en la fecha
     * actual. Este formulario se despliega en pantalla cuando
     * el usuario presiona el boton que tiene la etiqueta
     * Calcular sobre un registro de plantacion en desarrollo.
     */
    IrrigationWaterNeedFormData irrigationWaterNeedFormData = new IrrigationWaterNeedFormData();
    irrigationWaterNeedFormData.setParcel(givenParcel);
    irrigationWaterNeedFormData.setCrop(developingPlantingRecord.getCrop());
    irrigationWaterNeedFormData.setIrrigationWaterNeed(irrigationWaterNeedCurrentDate);
    irrigationWaterNeedFormData.setIrrigationDone(irrigationWaterNeedCurrentDate);

    /*
     * Si el valor del encabezado de autorizacion de la peticion HTTP
     * dada, tiene un JWT valido, la aplicacion del lado servidor
     * devuelve el mensaje HTTP 200 (Ok) junto con los datos
     * pertinentes
     */
    return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(irrigationWaterNeedFormData)).build();
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
  private double runCalculationIrrigationWaterNeedCurrentDate(User user, PlantingRecord developingPlantingRecord) {
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
  private void requestPastClimateRecords(int userId, PlantingRecord developingPlantingRecord) {
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

    double totalIrrigationWaterCurrentDate = irrigationRecordService.calculateTotalIrrigationWaterCurrentDate(givenParcel);

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

    double irrigationWaterNeedCurrentDate = 0.0;

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
      irrigationWaterNeedCurrentDate = WaterNeedWos.calculateIrrigationWaterNeed(totalIrrigationWaterCurrentDate, climateRecords, irrigationRecords);
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
      irrigationWaterNeedCurrentDate = WaterNeedWs.calculateIrrigationWaterNeed(totalIrrigationWaterCurrentDate,
          developingPlantingRecord.getCrop(), givenParcel.getSoil(), climateRecords, irrigationRecords);
    }

    return irrigationWaterNeedCurrentDate;
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
