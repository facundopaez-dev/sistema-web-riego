package servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Collection;
import javax.ejb.EJB;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
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
import model.Parcel;
import model.StatisticalGraph;
import stateless.StatisticalGraphServiceBean;
import stateless.StatisticalReportServiceBean;
import stateless.SecretKeyServiceBean;
import stateless.ClimateRecordServiceBean;
import stateless.ParcelServiceBean;
import stateless.PlantingRecordServiceBean;
import stateless.SessionServiceBean;
import stateless.CropServiceBean;
import stateless.HarvestServiceBean;
import stateless.IrrigationRecordServiceBean;
import stateless.Page;
import util.PersonalizedResponse;
import util.ErrorResponse;
import util.ReasonError;
import util.RequestManager;
import util.UtilDate;
import util.UtilMath;
import utilJwt.AuthHeaderManager;
import utilJwt.JwtManager;

@Path("/statisticalReports")
public class StatisticalGraphRestServlet {

  @EJB
  StatisticalReportServiceBean statisticalReportService;

  @EJB
  StatisticalGraphServiceBean statisticalGraphService;

  @EJB
  SecretKeyServiceBean secretKeyService;

  @EJB
  ParcelServiceBean parcelService;

  @EJB
  ClimateRecordServiceBean climateRecordService;

  @EJB
  PlantingRecordServiceBean plantingRecordService;

  @EJB
  IrrigationRecordServiceBean irrigationRecordService;

  @EJB
  CropServiceBean cropService;

  @EJB
  HarvestServiceBean harvestServiceBean;

  @EJB
  SessionServiceBean sessionService;

  // Mapea lista de pojo a JSON
  ObjectMapper mapper = new ObjectMapper();

  private final int DAYS_YEAR = 365;
  private final int TOTAL_AMOUNT_PLANTATIONS_PER_CROP = 1;
  private final int TOTAL_AMOUNT_PLANTATIONS_PER_CROP_AND_YEAR = 2;
  private final int TOTAL_AMOUNT_IRRIGATION_WATER_PER_CROP = 3;
  private final int TOTAL_AMOUNT_IRRIGATION_WATER_PER_CROP_AND_YEAR = 4;
  private final int TOTAL_HARVEST_PER_CROP = 5;
  private final int TOTAL_HARVEST_PER_CROP_AND_YEAR = 6;
  private final int TOTAL_AMOUNT_PLANTATIONS_PER_TYPE_CROP = 7;
  private final int TOTAL_AMOUNT_PLANTATIONS_PER_TYPE_CROP_AND_YEAR = 8;
  private final int TOTAL_AMOUNT_IRRIGATION_WATER_PER_TYPE_CROP = 9;
  private final int TOTAL_AMOUNT_IRRIGATION_WATER_PER_TYPE_CROP_AND_YEAR = 10;
  private final int TOTAL_HARVEST_PER_TYPE_CROP = 11;
  private final int TOTAL_HARVEST_PER_TYPE_CROP_AND_YEAR = 12;
  private final int LIFE_CYCLES_OF_PLANTED_CROPS = 13;
  private final int TOTAL_NUMBER_PLANTATIONS_PER_YEAR = 14;
  private final int TOTAL_AMOUNT_OF_CROP_IRRIGATION_WATER_PER_YEAR = 15;
  private final int TOTAL_AMOUNT_OF_HARVEST_PER_YEAR = 16;
  private final int TOTAL_AMOUNT_RAINWATER_PER_YEAR = 17;

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
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.JWT_NOT_ASSOCIATED_WITH_ACTIVE_SESSION)))
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
    Page<StatisticalGraph> statisticalGraphs = statisticalGraphService.findAllPagination(userId, page, cant, map);
    return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(statisticalGraphs)).build();
  }

  @GET
  @Path("/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response find(@Context HttpHeaders request, @PathParam("id") int statisticalGraphId) throws IOException {
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
    if (!statisticalGraphService.checkExistence(statisticalGraphId)) {
      return Response.status(Response.Status.NOT_FOUND).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.RESOURCE_NOT_FOUND))).build();
    }

    /*
     * Si al usuario que hizo esta peticion HTTP, no le pertenece
     * el dato solicitado, la aplicacion del lado servidor
     * devuelve el mensaje HTTP 403 (Forbidden) junto con el
     * mensaje "Acceso no autorizado" (contenido en el enum
     * ReasonError) y no se realiza la operacion solicitada
     */
    if (!statisticalGraphService.checkUserOwnership(userId, statisticalGraphId)) {
      return Response.status(Response.Status.FORBIDDEN).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.UNAUTHORIZED_ACCESS))).build();
    }

    /*
     * Si el valor del encabezado de autorizacion de la peticion HTTP
     * dada, tiene un JWT valido, la aplicacion del lado servidor
     * devuelve el mensaje HTTP 200 (Ok) junto con los datos solicitados
     * por el cliente
     */
    return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(statisticalGraphService.findByUserId(userId, statisticalGraphId))).build();
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

    StatisticalGraph newStatisticalGraph = mapper.readValue(json, StatisticalGraph.class);

    int parcelId = newStatisticalGraph.getParcel().getId();
    int statisticalDataNumber = newStatisticalGraph.getStatisticalData().getNumber();
    Calendar dateFrom = newStatisticalGraph.getDateFrom();
    Calendar dateUntil = newStatisticalGraph.getDateUntil();

    /*
     * ***********************************************
     * Controles sobre la fecha desde y la fecha hasta
     * ***********************************************
     */

    /*
     * Si la fecha desde o la fecha hasta de un informe
     * estadistico a generar NO esta definida, la aplicacion
     * del lado servidor retorna el mensaje HTTP 400 (Bad
     * request) junto con el mensaje "Las fechas deben
     * estar definidas" y no se realiza la operacion
     * solicitada
     */
    if (dateFrom == null || dateUntil == null) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.UNDEFINED_DATES))).build();
    }

    /*
     * Si la fecha desde es mayor o igual a la fecha hasta,
     * la aplicacion del lado servidor retorna el mensaje
     * HTTP 400 (Bad request) junto con el mensaje "La fecha
     * desde no debe ser mayor o igual a la fecha hasta" y
     * no se realiza la operacion solicitada
     */
    if (UtilDate.compareTo(dateFrom, dateUntil) >= 0) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.DATE_FROM_AND_DATE_UNTIL_OVERLAPPING)))
          .build();
    }

    /*
     * Si la fecha hasta es futura, es decir, posterior a la
     * fecha actual, la aplicacion del lado servidor retorna
     * el mensaje HTTP 400 (Bad request) junto con el mensaje
     * "La fecha hasta no debe ser estrictamente mayor (es decir,
     * posterior) a la fecha actual (es decir, hoy)" y no se
     * realiza la operacion solicitada
     */
    if (UtilDate.compareTo(dateUntil, UtilDate.getCurrentDate()) > 0) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.DATE_UNTIL_FUTURE_NOT_ALLOWED))).build();
    }

    /*
     * Si la cantidad de dias que hay entre la fecha desde y la
     * fecha hasta es estrictamente menor a la cantidad de dias
     * que hay en un año no bisiesto, la aplicacion del lado
     * servidor retorna el mensaje HTTP 400 (Bad request) junto
     * con el mensaje "La cantidad de dias que debe haber entre
     * la fecha desde y la fecha hasta debe ser mayor o igual
     * a 365 dias. En este caso, la fecha hasta debe ser <fecha
     * hasta calculada>." y no se realiza la operaicon solicitada
     */
    if (UtilDate.calculateDifferenceBetweenDates(dateFrom, dateUntil) < DAYS_YEAR) {
      String message = "La cantidad de días que debe haber entre la fecha desde y la fecha hasta debe ser mayor o igual a 365 días. "
          + "En este caso, la fecha hasta debe ser " + UtilDate.formatDate(UtilDate.calculateDateUntil(newStatisticalGraph.getDateFrom(), DAYS_YEAR)) + ".";
      PersonalizedResponse newPersonalizedResponse = new PersonalizedResponse(message);
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(newPersonalizedResponse)).build();
    }

    /*
     * *****************************************
     * Control sobre la definicion de la parcela
     * *****************************************
     */

    /*
     * Si la parcela NO esta definida, la aplicacion del
     * lado servidor retorna el mensaje HTTP 400 (Bad request)
     * junto con el mensaje "La parcela debe estar definida"
     * y no se realiza la operacion solicitada
     */
    if (newStatisticalGraph.getParcel() == null) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.INDEFINITE_PARCEL)))
          .build();
    }

    /*
     * Si la parcela elegida no existe en la base de datos
     * subyacente, la aplicacion del lado servidor devuelve
     * el mensaje HTTP 404 (Not found) junto con el mensaje
     * "Recurso no encontrado" y no se realiza la operacion
     * solicitada
     */
    if (!parcelService.checkExistence(parcelId)) {
      return Response.status(Response.Status.NOT_FOUND).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.RESOURCE_NOT_FOUND))).build();
    }

    /*
     * Si al usuario que hizo esta peticion HTTP, no le pertenece
     * la parcela elegida, la aplicacion del lado servidor
     * devuelve el mensaje HTTP 403 (Forbidden) junto con el
     * mensaje "Acceso no autorizado" (contenido en el enum
     * ReasonError) y no se realiza la operacion solicitada
     */
    if (!parcelService.checkUserOwnership(userId, parcelId)) {
      return Response.status(Response.Status.FORBIDDEN).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.UNAUTHORIZED_ACCESS))).build();
    }

    /*
     * Si al usuario que hizo esta peticion HTTP, no le pertenece
     * la parcela elegida, la aplicacion del lado servidor
     * devuelve el mensaje HTTP 403 (Forbidden) junto con el
     * mensaje "Acceso no autorizado" (contenido en el enum
     * ReasonError) y no se realiza la operacion solicitada
     */
    if (!parcelService.checkUserOwnership(userId, newStatisticalGraph.getParcel().getName())) {
      return Response.status(Response.Status.FORBIDDEN).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.UNAUTHORIZED_ACCESS))).build();
    }

    /*
     * *************************************************
     * Control sobre la definicion del dato estadistico
     * sobre el que se tratara el informe estadistico
     * *************************************************
     */

    /*
     * Si el dato estadistico a calcular para un informe
     * estadistico NO esta definido, la aplicacion del lado
     * servidor retorna el mensaje HTTP 400 (Bad request)
     * junto con el mensaje "El dato estadistico a calcular
     * debe estar definido" y no se realiza la operacion
     * solicitada
     */
    if (newStatisticalGraph.getStatisticalData() == null) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.UNDEFINED_STATISTICAL_DATA))).build();
    }

    /*
     * ***************************************************
     * Control sobre la posible existencia del informe
     * estadistico solicitado para las fechas y la parcela
     * elegidas
     * ***************************************************
     */

    /*
     * Si en la base de datos subyacente existe un informe
     * estadistico con la fecha desde, la fecha hasta y la
     * parcela elegidas y el dato estadistico elegido, la
     * aplicacion del lado lado servidor retorna el mensaje
     * HTTP 400 (Bad request) junto con el mensaje "Ya existe
     * un informe estadistico con las fechas, la parcela y
     * el dato estadistico elegidos" y no se realiza la
     * operacion solicitada
     */
    if (statisticalGraphService.checkExistence(userId, parcelId, dateFrom, dateUntil, newStatisticalGraph.getStatisticalData())) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.EXISTING_STATISTICAL_REPORT)))
          .build();
    }

    /*
     * ********************************************************
     * Controles sobre la existencia de registros climaticos de
     * la parcela seleccionada para generar un informe estadistico
     * ********************************************************
     */

    /*
     * Si la parcela seleccionada para generar un informe
     * estadistico NO tiene registros climaticos, la aplicacion
     * del lado servidor retorna el mensaje HTTP 400 (Bad
     * request) junto con un mensaje que indica lo sucedido
     * y no se realiza la operacion solicitada. Para generar
     * un informe estadistico:
     * - de la cantidad total de agua de lluvia que cayo por
     * año en una parcela durante un periodo definido por dos
     * fechas,
     * 
     * se requiere que la parcela seleccionada tenga registros
     * climaticos. Este es el motivo de este control.
     */
    if (statisticalDataNumber == TOTAL_AMOUNT_RAINWATER_PER_YEAR && !climateRecordService.hasClimateRecords(userId, parcelId)) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.NON_EXISTENT_CLIMATE_RECORDS)))
          .build();
    }

    /*
     * Si la parcela seleccionada para generar un informe
     * estadistico NO tiene registros climaticos, la aplicacion
     * del lado servidor retorna el mensaje HTTP 400 (Bad
     * request) junto con un mensaje que indica lo sucedido
     * y no se realiza la operacion solicitada. Para generar
     * un informe estadistico:
     * - de la cantidad total de agua de lluvia que cayo por
     * año en una parcela durante un periodo definido por dos
     * fechas,
     * 
     * se requiere que la parcela seleccionada tenga registros
     * climaticos. Este es el motivo de este control.
     */
    if (statisticalDataNumber == TOTAL_AMOUNT_RAINWATER_PER_YEAR && !climateRecordService.hasClimateRecords(userId, parcelId, dateFrom, dateUntil)) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.NON_EXISTENT_CLIMATE_RECORDS_IN_A_PERIOD)))
          .build();
    }

    /*
     * ********************************************************
     * Controles sobre la existencia de registros de plantacion
     * finalizados de la parcela seleccionada para generar un
     * informe estadistico
     * ********************************************************
     */

    /*
     * Si la parcela seleccionada para generar un informe
     * estadistico NO tiene registros de plantacion finalizados,
     * la aplicacion del lado servidor retorna el mensaje
     * HTTP 400 (Bad request) junto con un mensaje que
     * indica lo sucedido y no se realiza la operacion
     * solicitada. Para generar un informe estadistico:
     * - de la cantidad de veces que se plantaron los
     * cultivos en una parcela durante un periodo definido
     * por dos fechas
     * - de la cantidad de veces que se plantaron los
     * cultivos por año en una parcela en un periodo
     * definido por dos fechas
     * solicitada. Para generar un informe estadistico:
     * - de la cantidad de veces que se plantaron los
     * tipos de cultivos en una parcela durante un periodo
     * definido por dos fechas
     * - de la cantidad de veces que se plantaron los
     * tipos de cultivos por año en una parcela en un
     * periodo definido por dos fechas
     * - de los ciclos de vida de los cultivos sembrados
     * en una parcela en un periodo definido por dos
     * fechas
     * - de la cantidad total de plantaciones por año
     * sobre una parcela en un periodo definido por dos
     * fechas,
     * 
     * se requiere que la parcela seleccionada tenga registros
     * de plantacion finalizados. Este es el motivo de
     * este control.
     */
    if ((statisticalDataNumber == TOTAL_AMOUNT_PLANTATIONS_PER_CROP
        || statisticalDataNumber == TOTAL_AMOUNT_PLANTATIONS_PER_CROP_AND_YEAR
        || statisticalDataNumber == TOTAL_AMOUNT_PLANTATIONS_PER_TYPE_CROP
        || statisticalDataNumber == TOTAL_AMOUNT_PLANTATIONS_PER_TYPE_CROP_AND_YEAR
        || statisticalDataNumber == LIFE_CYCLES_OF_PLANTED_CROPS
        || statisticalDataNumber == TOTAL_NUMBER_PLANTATIONS_PER_YEAR)
        && !plantingRecordService.hasFinishedPlantingRecords(userId, parcelId)) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.NON_EXISTENT_PLANTING_RECORDS)))
          .build();
    }

    /*
     * Si la parcela seleccionada para generar un informe
     * estadistico NO tiene registros de plantacion finalizados
     * en el periodo definido por la fecha desde y la fecha
     * hasta elegidas, la aplicacion del lado servidor retorna
     * el mensaje HTTP 400 (Bad request) junto con un mensaje
     * que indica lo sucedido y no se realiza la operacion
     * solicitada. Para generar un informe estadistico:
     * - de la cantidad de veces que se plantaron los
     * cultivos en una parcela durante un periodo definido
     * por dos fechas o
     * - de la cantidad de veces que se plantaron los
     * cultivos por año en una parcela en un periodo
     * definido por dos fechas
     * - de los ciclos de vida de los cultivos sembrados
     * en una parcela en un periodo definido por dos
     * fechas
     * - de la cantidad total de plantaciones por año
     * sobre una parcela en un periodo definido por dos
     * fechas,
     * 
     * se requiere que la parcela seleccionada tenga registros
     * de plantacion finalizados. Este es el motivo de
     * este control.
     */
    if ((statisticalDataNumber == TOTAL_AMOUNT_PLANTATIONS_PER_CROP
        || statisticalDataNumber == TOTAL_AMOUNT_PLANTATIONS_PER_CROP_AND_YEAR
        || statisticalDataNumber == TOTAL_AMOUNT_PLANTATIONS_PER_TYPE_CROP
        || statisticalDataNumber == TOTAL_AMOUNT_PLANTATIONS_PER_TYPE_CROP_AND_YEAR
        || statisticalDataNumber == LIFE_CYCLES_OF_PLANTED_CROPS
        || statisticalDataNumber == TOTAL_NUMBER_PLANTATIONS_PER_YEAR)
        && !plantingRecordService.hasFinishedPlantingRecords(userId, parcelId, dateFrom, dateUntil)) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.NON_EXISTENT_PLANTING_RECORDS_IN_A_PERIOD)))
          .build();
    }

    /*
     * ********************************************************
     * Controles sobre la existencia de registros de riego con
     * cultivo de la parcela seleccionada para generar un informe
     * estadistico
     * ********************************************************
     */

    /*
     * Si la parcela seleccionada para generar un informe
     * estadistico NO tiene registros de riego asociados a
     * un cultivo, la aplicacion del lado servidor retorna
     * el mensaje HTTP 400 (Bad request) junto con un mensaje
     * que indica lo sucedido y no se realiza la operacion
     * solicitada. Para generar un informe estadistico:
     * - de la cantidad total de agua de riego por cultivo
     * de los cultivos plantados en una parcela durante
     * un periodo definido por dos fechas
     * - de la cantidad total de agua de riego por cultivo
     * y año de los cultivos plantados en una parcela durante
     * un periodo definido por dos fechas
     * - de la cantidad total de agua de riego por tipo de
     * cultivo de los tipos de cultivos plantados en una
     * parcela durante un periodo definido por dos fechas
     * - de la cantidad total de agua de riego por tipo de
     * cultivo y año de los tipos de cultivos plantados en
     * una parcela durante un periodo definido por dos fechas
     * - de la cantidad total de agua utilizada para el riego
     * de cultivos por año durante un periodo definido por
     * dos fechas,
     * 
     * se requiere que la parcela seleccionada tenga registros
     * de riego asociados a un cultivo. Este es el motivo de
     * este control.
     */
    if ((statisticalDataNumber == TOTAL_AMOUNT_IRRIGATION_WATER_PER_CROP
        || statisticalDataNumber == TOTAL_AMOUNT_IRRIGATION_WATER_PER_CROP_AND_YEAR
        || statisticalDataNumber == TOTAL_AMOUNT_IRRIGATION_WATER_PER_TYPE_CROP
        || statisticalDataNumber == TOTAL_AMOUNT_IRRIGATION_WATER_PER_TYPE_CROP_AND_YEAR
        || statisticalDataNumber == TOTAL_AMOUNT_OF_CROP_IRRIGATION_WATER_PER_YEAR)
        && !irrigationRecordService.hasIrrigationRecordsWithCrops(parcelId)) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.NON_EXISTENT_IRRIGATION_RECORDS_WITH_CROP)))
          .build();
    }

    /*
     * Si la parcela seleccionada para generar un informe
     * estadistico NO tiene registros de riego asociados a
     * un cultivo en el periodo definido por dos fechas, la
     * aplicacion del lado servidor retorna el mensaje HTTP
     * 400 (Bad request) junto con un mensaje que indica lo
     * sucedido y no se realiza la operacion solicitada.
     * Para generar un informe estadistico:
     * - de la cantidad total de agua de riego por cultivo
     * de los cultivos plantados en una parcela durante
     * un periodo definido por dos fechas
     * - de la cantidad total de agua de riego por cultivo
     * y año de los cultivos plantados en una parcela durante
     * un periodo definido por dos fechas
     * - de la cantidad total de agua de riego por tipo de
     * cultivo de los tipos de cultivos plantados en una
     * parcela durante un periodo definido por dos fechas
     * - de la cantidad total de agua de riego por tipo de
     * cultivo y año de los tipos de cultivos plantados en
     * una parcela durante un periodo definido por dos fechas
     * - de la cantidad total de agua utilizada para el riego
     * de cultivos por año durante un periodo definido por
     * dos fechas,
     * 
     * se requiere que la parcela seleccionada tenga registros
     * de riego asociados a un cultivo. Este es el motivo de
     * este control.
     */
    if ((statisticalDataNumber == TOTAL_AMOUNT_IRRIGATION_WATER_PER_CROP
        || statisticalDataNumber == TOTAL_AMOUNT_IRRIGATION_WATER_PER_CROP_AND_YEAR
        || statisticalDataNumber == TOTAL_AMOUNT_IRRIGATION_WATER_PER_TYPE_CROP
        || statisticalDataNumber == TOTAL_AMOUNT_IRRIGATION_WATER_PER_TYPE_CROP_AND_YEAR
        || statisticalDataNumber == TOTAL_AMOUNT_OF_CROP_IRRIGATION_WATER_PER_YEAR)
        && !irrigationRecordService.hasIrrigationRecordsWithCrops(parcelId, dateFrom, dateUntil)) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.NON_EXISTENT_IRRIGATION_RECORDS_WITH_CROP_IN_A_PERIOD)))
          .build();
    }

    /*
     * ********************************************************
     * Controles sobre la existencia de registros de cosecha de
     * la parcela seleccionada para generar un informe estadistico
     * ********************************************************
     */

    /*
     * Si la parcela seleccionada para generar un informe
     * estadistico NO tiene registros de cosecha, la aplicacion
     * del lado servidor retorna el mensaje HTTP 400 (Bad
     * request) junto con un mensaje que indica lo sucedido
     * y no se realiza la operacion solicitada. Para generar
     * un informe estadistico:
     * - de la cantidad total cosechada por cultivo de los
     * cultivos cosechados en una parcela durante un periodo
     * definido por dos fechas
     * - de la cantidad total cosechada por cultivo y año de
     * los cultivos cosechados en una parcela durante un
     * periodo definido por dos fechas
     * - de la cantidad total cosechada por tipo de cultivo
     * de los tipos de cultivos cosechados en una parcela
     * durante un periodo definido por dos fechas
     * - de la cantidad total cosechada por tipo de cultivo
     * y año de los tipos de cultivos cosechados en una
     * parcela durante un periodo definido por dos fechas
     * - de la cantidad total de cosecha por año de una parcela
     * durante un periodo definido por dos fechas,
     * 
     * se requiere que la parcela seleccionada tenga registros
     * de cosecha. Este es el motivo de este control.
     */
    if ((statisticalDataNumber == TOTAL_HARVEST_PER_CROP
        || statisticalDataNumber == TOTAL_HARVEST_PER_CROP_AND_YEAR
        || statisticalDataNumber == TOTAL_HARVEST_PER_TYPE_CROP
        || statisticalDataNumber == TOTAL_HARVEST_PER_TYPE_CROP_AND_YEAR
        || statisticalDataNumber == TOTAL_AMOUNT_OF_HARVEST_PER_YEAR)
        && !harvestServiceBean.hasHarvestRecords(parcelId)) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.NON_EXISTENT_HARVEST_RECORDS)))
          .build();
    }

    /*
     * Si la parcela seleccionada para generar un informe
     * estadistico NO tiene registros de cosecha en el periodo
     * definido por dos fechas, la aplicacion del lado servidor
     * retorna el mensaje HTTP 400 (Bad request) junto con
     * un mensaje que indica lo sucedido y no se realiza la
     * operacion solicitada. Para generar un informe estadistico:
     * - de la cantidad total cosechada por cultivo de los
     * cultivos cosechados en una parcela durante un periodo
     * definido por dos fechas o
     * - de la cantidad total cosechada por cultivo y año de
     * los cultivos cosechados en una parcela durante un
     * periodo definido por dos fechas
     * - de la cantidad total cosechada por tipo de cultivo
     * de los tipos de cultivos cosechados en una parcela
     * durante un periodo definido por dos fechas
     * - de la cantidad total cosechada por tipo de cultivo
     * y año de los tipos de cultivos cosechados en una
     * parcela durante un periodo definido por dos fechas
     * - de la cantidad total de cosecha por año de una parcela
     * durante un periodo definido por dos fechas,
     * 
     * se requiere que la parcela seleccionada tenga registros
     * de cosecha. Este es el motivo de este control.
     */
    if ((statisticalDataNumber == TOTAL_HARVEST_PER_CROP
        || statisticalDataNumber == TOTAL_HARVEST_PER_CROP_AND_YEAR
        || statisticalDataNumber == TOTAL_HARVEST_PER_TYPE_CROP
        || statisticalDataNumber == TOTAL_HARVEST_PER_TYPE_CROP_AND_YEAR
        || statisticalDataNumber == TOTAL_AMOUNT_OF_HARVEST_PER_YEAR)
        && !harvestServiceBean.hasHarvestRecords(parcelId, dateFrom, dateUntil)) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.NON_EXISTENT_HARVEST_RECORDS_IN_A_PERIOD)))
          .build();
    }

    /*
     * ***********************************
     * Generacion de informes estadisticos
     * ***********************************
     */

    /*
     * Si el numero del dato estadistico a calcular es el
     * valor de la constante TOTAL_AMOUNT_PLANTATIONS_PER_CROP,
     * se calcula la cantidad total de veces que se plantaron
     * los cultivos en una parcela durante un periodo
     * definido por dos fechas
     */
    if (statisticalDataNumber == TOTAL_AMOUNT_PLANTATIONS_PER_CROP) {
      newStatisticalGraph.setData(statisticalReportService.calculateTotalNumberPlantationsPerCrop(parcelId, dateFrom, dateUntil));
      newStatisticalGraph.setLabels(statisticalReportService.findCropNamesCalculatedPerTotalNumberPlantationsPerCrop(parcelId, dateFrom, dateUntil));
      newStatisticalGraph.setText("Y: Cantidad de plantaciones, X: Cultivo, Parcela: " + newStatisticalGraph.getParcel().getName()
              + ", Período: " + UtilDate.formatDate(dateFrom) + " - " + UtilDate.formatDate(dateUntil)
              + ", Cant. total de plantaciones: " + statisticalReportService.calculateTotalNumberPlantationsPerPeriod(parcelId, dateFrom, dateUntil));

      return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(statisticalGraphService.create(newStatisticalGraph))).build();
    }

    /*
     * Si el numero del dato estadistico a calcular es el
     * valor de la constante TOTAL_AMOUNT_PLANTATIONS_PER_CROP_AND_YEAR,
     * se calcula la cantidad total de veces que se plantaron
     * los cultivos por año en una parcela durante un periodo
     * definido por dos fechas
     */
    if (statisticalDataNumber == TOTAL_AMOUNT_PLANTATIONS_PER_CROP_AND_YEAR) {
      List<String> cropNames = statisticalReportService.findCropNamesCalculatedPerTotalNumberPlantationsPerCropAndYear(parcelId, dateFrom, dateUntil);
      List<Integer> seedYears = statisticalReportService.findYearsCalculatedPerTotalNumberPlantationsPerCropAndYear(parcelId, dateFrom, dateUntil);

      /*
       * Arma las etiquetas <cultivo> (<año>) para el grafico
       * de barras correspondiente al informe estadistico
       * solicitado
       */
      List<String> labels = statisticalReportService.setLabelsWithCropAndYear(cropNames, seedYears);

      newStatisticalGraph.setData(statisticalReportService.calculateTotalNumberPlantationsPerCropAndYear(parcelId, dateFrom, dateUntil));
      newStatisticalGraph.setLabels(labels);
      newStatisticalGraph.setText("Y: Cantidad de plantaciones, X: Cultivo (año), Parcela: " + newStatisticalGraph.getParcel().getName()
              + ", Período: " + UtilDate.formatDate(dateFrom) + " - " + UtilDate.formatDate(dateUntil)
              + ", Cant. total de plantaciones: " + statisticalReportService.calculateTotalNumberPlantationsPerPeriod(parcelId, dateFrom, dateUntil));

      return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(statisticalGraphService.create(newStatisticalGraph))).build();
    }

    /*
     * Si el numero del dato estadistico a calcular es el
     * valor de la constante TOTAL_AMOUNT_IRRIGATION_WATER_PER_CROP,
     * se calcula la cantidad total de agua de riego de los
     * cultivos plantados en una parcela durante un periodo
     * definido por dos fechas
     */
    if (statisticalDataNumber == TOTAL_AMOUNT_IRRIGATION_WATER_PER_CROP) {
      newStatisticalGraph.setData(statisticalReportService.calculateTotalAmountIrrigationWaterPerCrop(parcelId, dateFrom, dateUntil));
      newStatisticalGraph.setLabels(statisticalReportService.findCropNamesCalculatedPerTotalAmountIrrigationWaterPerCrop(parcelId, dateFrom, dateUntil));
      newStatisticalGraph.setAverage(UtilMath.truncateToTwoDigits(statisticalReportService.calculateAverageCropIrrigationWater(parcelId, dateFrom, dateUntil)));
      newStatisticalGraph.setText("Y: Cantidad de agua de riego [mm], X: Cultivo, Parcela: " + newStatisticalGraph.getParcel().getName()
              + ", Período: " + UtilDate.formatDate(dateFrom) + " - " + UtilDate.formatDate(dateUntil)
              + ", Cant. total de agua de riego [mm]: " + statisticalReportService.calculateTotalAmountCropIrrigationWaterPerPeriod(parcelId, dateFrom, dateUntil));

      return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(statisticalGraphService.create(newStatisticalGraph))).build();
    }

    /*
     * Si el numero del dato estadistico a calcular es el
     * valor de la constante TOTAL_AMOUNT_IRRIGATION_WATER_PER_CROP_AND_YEAR,
     * se calcula la cantidad total de agua de riego por
     * cultivo y año de los cultivos plantados en una parcela
     * durante un periodo definido por dos fechas
     */
    if (statisticalDataNumber == TOTAL_AMOUNT_IRRIGATION_WATER_PER_CROP_AND_YEAR) {
      List<String> cropNames = statisticalReportService.findCropNamesCalculatedPerTotalAmountIrrigationWaterPerCropAndYear(parcelId, dateFrom, dateUntil);
      List<Integer> seedYears = statisticalReportService.findYearsCalculatedPerTotalAmountIrrigationWaterPerCropAndYear(parcelId, dateFrom, dateUntil);

      /*
       * Arma las etiquetas <cultivo> (<año>) para el grafico
       * de barras correspondiente al informe estadistico
       * solicitado
       */
      List<String> labels = statisticalReportService.setLabelsWithCropAndYear(cropNames, seedYears);

      newStatisticalGraph.setData(statisticalReportService.calculateTotalAmountIrrigationWaterPerCropAndYear(parcelId, dateFrom, dateUntil));
      newStatisticalGraph.setLabels(labels);
      newStatisticalGraph.setAverage(UtilMath.truncateToTwoDigits(statisticalReportService.calculateAverageCropIrrigationWater(parcelId, dateFrom, dateUntil)));
      newStatisticalGraph.setText("Y: Cantidad de agua de riego [mm], X: Cultivo (año), Parcela: " + newStatisticalGraph.getParcel().getName()
              + ", Período: " + UtilDate.formatDate(dateFrom) + " - " + UtilDate.formatDate(dateUntil)
              + ", Cant. total de agua de riego [mm]: " + statisticalReportService.calculateTotalAmountCropIrrigationWaterPerPeriod(parcelId, dateFrom, dateUntil));

      return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(statisticalGraphService.create(newStatisticalGraph))).build();
    }

    /*
     * Si el numero del dato estadistico a calcular es el
     * valor de la constante TOTAL_HARVEST_PER_CROP,
     * se calcula la cantidad total cosechada [kg] (rendimiento)
     * de los cultivos cosechados en una parcela durante un
     * periodo definido por dos fechas
     */
    if (statisticalDataNumber == TOTAL_HARVEST_PER_CROP) {
      newStatisticalGraph.setData(statisticalReportService.calculateTotalHarvestPerCrop(parcelId, dateFrom, dateUntil));
      newStatisticalGraph.setLabels(statisticalReportService.findCropNamesCalculatedPerTotalHarvestPerCrop(parcelId, dateFrom, dateUntil));
      newStatisticalGraph.setAverage(UtilMath.truncateToTwoDigits(statisticalReportService.calculateAverageHarvest(parcelId, dateFrom, dateUntil)));
      newStatisticalGraph.setText("Y: Cantidad cosechada [kg], X: Cultivo, Parcela: " + newStatisticalGraph.getParcel().getName()
              + ", Período: " + UtilDate.formatDate(dateFrom) + " - " + UtilDate.formatDate(dateUntil)
              + ", Cant. total cosechada [kg]: " + statisticalReportService.calculateTotalHarvestPerPeriod(parcelId, dateFrom, dateUntil));

      return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(statisticalGraphService.create(newStatisticalGraph))).build();
    }

    /*
     * Si el numero del dato estadistico a calcular es el
     * valor de la constante TOTAL_HARVEST_PER_CROP_AND_YEAR,
     * se calcula la cantidad total cosechada [kg] (rendimiento)
     * por cultivo y año de los cultivos cosechados en una
     * parcela durante un periodo definido por dos fechas
     */
    if (statisticalDataNumber == TOTAL_HARVEST_PER_CROP_AND_YEAR) {
      List<String> cropNames = statisticalReportService.findCropNamesCalculatedPerTotalHarvestPerCropAndYear(parcelId, dateFrom, dateUntil);
      List<Integer> harvestYears = statisticalReportService.findYearsCalculatedPerTotalHarvestPerCropAndYear(parcelId, dateFrom, dateUntil);

      /*
       * Arma las etiquetas <cultivo> (<año>) para el grafico
       * de barras correspondiente al informe estadistico
       * solicitado
       */
      List<String> labels = statisticalReportService.setLabelsWithCropAndYear(cropNames, harvestYears);

      newStatisticalGraph.setData(statisticalReportService.calculateTotalHarvestPerCropAndYear(parcelId, dateFrom, dateUntil));
      newStatisticalGraph.setLabels(labels);
      newStatisticalGraph.setAverage(UtilMath.truncateToTwoDigits(statisticalReportService.calculateAverageHarvest(parcelId, dateFrom, dateUntil)));
      newStatisticalGraph.setText("Y: Cantidad cosechada [kg], X: Cultivo (año), Parcela: " + newStatisticalGraph.getParcel().getName()
              + ", Período: " + UtilDate.formatDate(dateFrom) + " - " + UtilDate.formatDate(dateUntil)
              + ", Cant. total cosechada [kg]: " + statisticalReportService.calculateTotalHarvestPerPeriod(parcelId, dateFrom, dateUntil));

      return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(statisticalGraphService.create(newStatisticalGraph))).build();
    }

    /*
     * Si el numero del dato estadistico a calcular es el
     * valor de la constante TOTAL_AMOUNT_PLANTATIONS_PER_TYPE_CROP,
     * se calcula la cantidad total de veces que se plantaron
     * los tipos de cultivo en una parcela durante un periodo
     * definido por dos fechas
     */
    if (statisticalDataNumber == TOTAL_AMOUNT_PLANTATIONS_PER_TYPE_CROP) {
      newStatisticalGraph.setData(statisticalReportService.calculateTotalNumberPlantationsPerTypeCrop(parcelId, dateFrom, dateUntil));
      newStatisticalGraph.setLabels(statisticalReportService.findTypeCropNamesCalculatedPerTotalNumberPlantationsPerTypeCrop(parcelId, dateFrom, dateUntil));
      newStatisticalGraph.setText("Y: Cantidad de plantaciones, X: Tipo de cultivo, Parcela: " + newStatisticalGraph.getParcel().getName()
              + ", Período: " + UtilDate.formatDate(dateFrom) + " - " + UtilDate.formatDate(dateUntil)
              + ", Cant. total de plantaciones: " + statisticalReportService.calculateTotalNumberPlantationsPerPeriod(parcelId, dateFrom, dateUntil));

      return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(statisticalGraphService.create(newStatisticalGraph))).build();
    }

    /*
     * Si el numero del dato estadistico a calcular es el
     * valor de la constante TOTAL_AMOUNT_PLANTATIONS_PER_TYPE_CROP_AND_YEAR,
     * se calcula la cantidad total de veces que se plantaron
     * los tipos de cultivos por año en una parcela durante un
     * periodo definido por dos fechas
     */
    if (statisticalDataNumber == TOTAL_AMOUNT_PLANTATIONS_PER_TYPE_CROP_AND_YEAR) {
      List<String> typeCropNames = statisticalReportService.findTypeCropNamesCalculatedPerTotalNumberPlantationsPerTypeCropAndYear(parcelId, dateFrom, dateUntil);
      List<Integer> seedYears = statisticalReportService.findYearsCalculatedPerTotalNumberPlantationsPerTypeCropAndYear(parcelId, dateFrom, dateUntil);

      /*
       * Arma las etiquetas <cultivo> (<año>) para el grafico
       * de barras correspondiente al informe estadistico
       * solicitado
       */
      List<String> labels = statisticalReportService.setLabelsWithCropAndYear(typeCropNames, seedYears);

      newStatisticalGraph.setData(statisticalReportService.calculateTotalNumberPlantationsPerTypeCropAndYear(parcelId, dateFrom, dateUntil));
      newStatisticalGraph.setLabels(labels);
      newStatisticalGraph.setText("Y: Cantidad de plantaciones, X: Tipo de cultivo (año), Parcela: " + newStatisticalGraph.getParcel().getName()
              + ", Período: " + UtilDate.formatDate(dateFrom) + " - " + UtilDate.formatDate(dateUntil)
              + ", Cant. total de plantaciones: " + statisticalReportService.calculateTotalNumberPlantationsPerPeriod(parcelId, dateFrom, dateUntil));

      return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(statisticalGraphService.create(newStatisticalGraph))).build();
    }

    /*
     * Si el numero del dato estadistico a calcular es el
     * valor de la constante TOTAL_AMOUNT_IRRIGATION_WATER_PER_TYPE_CROP,
     * se calcula la cantidad total de agua de riego de los
     * tipos de cultivos plantados en una parcela durante un
     * periodo definido por dos fechas
     */
    if (statisticalDataNumber == TOTAL_AMOUNT_IRRIGATION_WATER_PER_TYPE_CROP) {
      newStatisticalGraph.setData(statisticalReportService.calculateTotalAmountIrrigationWaterPerTypeCrop(parcelId, dateFrom, dateUntil));
      newStatisticalGraph.setLabels(statisticalReportService.findTypeCropNamesCalculatedPerTotalAmountIrrigationWaterPerTypeCrop(parcelId, dateFrom, dateUntil));
      newStatisticalGraph.setAverage(UtilMath.truncateToTwoDigits(statisticalReportService.calculateAverageCropIrrigationWater(parcelId, dateFrom, dateUntil)));
      newStatisticalGraph.setText("Y: Cantidad de agua de riego [mm], X: Tipo de cultivo, Parcela: " + newStatisticalGraph.getParcel().getName()
              + ", Período: " + UtilDate.formatDate(dateFrom) + " - " + UtilDate.formatDate(dateUntil)
              + ", Cant. total de agua de riego [mm]: " + statisticalReportService.calculateTotalAmountCropIrrigationWaterPerPeriod(parcelId, dateFrom, dateUntil));

      return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(statisticalGraphService.create(newStatisticalGraph))).build();
    }

    /*
     * Si el numero del dato estadistico a calcular es el
     * valor de la constante TOTAL_AMOUNT_IRRIGATION_WATER_PER_TYPE_CROP_AND_YEAR,
     * se calcula la cantidad total de agua de riego por
     * tipo de cultivo y año de los cultivos plantados en una
     * parcela durante un periodo definido por dos fechas
     */
    if (statisticalDataNumber == TOTAL_AMOUNT_IRRIGATION_WATER_PER_TYPE_CROP_AND_YEAR) {
      List<String> typeCropNames = statisticalReportService.findTypeCropNamesCalculatedPerTotalAmountIrrigationWaterPerTypeCropAndYear(parcelId, dateFrom, dateUntil);
      List<Integer> seedYears = statisticalReportService.findYearsCalculatedPerTotalAmountIrrigationWaterPerTypeCropAndYear(parcelId, dateFrom, dateUntil);

      /*
       * Arma las etiquetas <cultivo> (<año>) para el grafico
       * de barras correspondiente al informe estadistico
       * solicitado
       */
      List<String> labels = statisticalReportService.setLabelsWithCropAndYear(typeCropNames, seedYears);

      newStatisticalGraph.setData(statisticalReportService.calculateTotalAmountIrrigationWaterPerTypeCropAndYear(parcelId, dateFrom, dateUntil));
      newStatisticalGraph.setLabels(labels);
      newStatisticalGraph.setAverage(UtilMath.truncateToTwoDigits(statisticalReportService.calculateAverageCropIrrigationWater(parcelId, dateFrom, dateUntil)));
      newStatisticalGraph.setText("Y: Cantidad de agua de riego [mm], X: Tipo de cultivo (año), Parcela: " + newStatisticalGraph.getParcel().getName()
              + ", Período: " + UtilDate.formatDate(dateFrom) + " - " + UtilDate.formatDate(dateUntil)
              + ", Cant. total de agua de riego [mm]: " + statisticalReportService.calculateTotalAmountCropIrrigationWaterPerPeriod(parcelId, dateFrom, dateUntil));

      return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(statisticalGraphService.create(newStatisticalGraph))).build();
    }

    /*
     * Si el numero del dato estadistico a calcular es el
     * valor de la constante TOTAL_HARVEST_PER_TYPE_CROP,
     * se calcula la cantidad total cosechada [kg] (rendimiento)
     * de los tipos de cultivos cosechados en una parcela
     * durante un periodo definido por dos fechas
     */
    if (statisticalDataNumber == TOTAL_HARVEST_PER_TYPE_CROP) {
      newStatisticalGraph.setData(statisticalReportService.calculateTotalHarvestPerTypeCrop(parcelId, dateFrom, dateUntil));
      newStatisticalGraph.setLabels(statisticalReportService.findTypeCropNamesCalculatedPerTotalHarvestPerTypeCrop(parcelId, dateFrom, dateUntil));
      newStatisticalGraph.setAverage(UtilMath.truncateToTwoDigits(statisticalReportService.calculateAverageHarvest(parcelId, dateFrom, dateUntil)));
      newStatisticalGraph.setText("Y: Cantidad cosechada [kg], X: Tipo de cultivo, Parcela: " + newStatisticalGraph.getParcel().getName()
              + ", Período: " + UtilDate.formatDate(dateFrom) + " - " + UtilDate.formatDate(dateUntil)
              + ", Cant. total cosechada [kg]: " + statisticalReportService.calculateTotalHarvestPerPeriod(parcelId, dateFrom, dateUntil));

      return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(statisticalGraphService.create(newStatisticalGraph))).build();
    }

    /*
     * Si el numero del dato estadistico a calcular es el
     * valor de la constante TOTAL_HARVEST_PER_TYPE_CROP_AND_YEAR,
     * se calcula la cantidad total cosechada [kg] (rendimiento)
     * por tipo de cultivo y año de los tipos de cultivos
     * cosechados en una parcela durante un periodo definido
     * por dos fechas
     */
    if (statisticalDataNumber == TOTAL_HARVEST_PER_TYPE_CROP_AND_YEAR) {
      List<String> typeCropNames = statisticalReportService.findTypeCropNamesCalculatedPerTotalHarvestPerTypeCropAndYear(parcelId, dateFrom, dateUntil);
      List<Integer> harvestYears = statisticalReportService.findYearsCalculatedPerTotalHarvestPerTypeCropAndYear(parcelId, dateFrom, dateUntil);

      /*
       * Arma las etiquetas <cultivo> (<año>) para el grafico
       * de barras correspondiente al informe estadistico
       * solicitado
       */
      List<String> labels = statisticalReportService.setLabelsWithCropAndYear(typeCropNames, harvestYears);

      newStatisticalGraph.setData(statisticalReportService.calculateTotalHarvestPerTypeCropAndYear(parcelId, dateFrom, dateUntil));
      newStatisticalGraph.setLabels(labels);
      newStatisticalGraph.setAverage(UtilMath.truncateToTwoDigits(statisticalReportService.calculateAverageHarvest(parcelId, dateFrom, dateUntil)));
      newStatisticalGraph.setText("Y: Cantidad cosechada [kg], X: Tipo de cultivo (año), Parcela: " + newStatisticalGraph.getParcel().getName()
              + ", Período: " + UtilDate.formatDate(dateFrom) + " - " + UtilDate.formatDate(dateUntil)
              + ", Cant. total cosechada [kg]: " + statisticalReportService.calculateTotalHarvestPerPeriod(parcelId, dateFrom, dateUntil));

      return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(statisticalGraphService.create(newStatisticalGraph))).build();
    }

    /*
     * Si el numero del dato estadistico a calcular es el
     * valor de la constante LIFE_CYCLES_OF_PLANTED_CROPS,
     * se obtienen los ciclos de vida [dias] de los cultivos
     * sembrados en una parcela durante un periodo definido
     * por dos fechas
     */
    if (statisticalDataNumber == LIFE_CYCLES_OF_PLANTED_CROPS) {
      newStatisticalGraph.setData(statisticalReportService.findLifeCyclesCropsPlantedPerPeriod(parcelId, dateFrom, dateUntil));
      newStatisticalGraph.setLabels(statisticalReportService.findNamesCropPlantedPerPeriod(parcelId, dateFrom, dateUntil));
      newStatisticalGraph.setText("Y: Ciclo de vida [días], X: Cultivo, Parcela: " + newStatisticalGraph.getParcel().getName()
              + ", Período: " + UtilDate.formatDate(dateFrom) + " - " + UtilDate.formatDate(dateUntil));

      return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(statisticalGraphService.create(newStatisticalGraph))).build();
    }

    /*
     * Si el numero del dato estadistico a calcular es el
     * valor de la constante TOTAL_NUMBER_PLANTATIONS_PER_YEAR,
     * se calcula la cantidad total de plantaciones por año
     * que se realizaron sobre una parcela durante un periodo
     * definido por dos fechas
     */
    if (statisticalDataNumber == TOTAL_NUMBER_PLANTATIONS_PER_YEAR) {
      newStatisticalGraph.setData(statisticalReportService.calculateTotalNumberPlantationsPerYear(parcelId, dateFrom, dateUntil));
      newStatisticalGraph.setLabels(statisticalReportService.findYearsOfCalculationTotalNumberPlantationsPerYear(parcelId, dateFrom, dateUntil));
      newStatisticalGraph.setText("Y: Cantidad de plantaciones, X: Año, Parcela: " + newStatisticalGraph.getParcel().getName()
              + ", Período: " + UtilDate.formatDate(dateFrom) + " - " + UtilDate.formatDate(dateUntil)
              + ", Cant. total de plantaciones: "
              + statisticalReportService.calculateTotalNumberPlantationsPerPeriod(parcelId, dateFrom, dateUntil));

      return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(statisticalGraphService.create(newStatisticalGraph))).build();
    }

    /*
     * Si el numero del dato estadistico a calcular es el
     * valor de la constante TOTAL_AMOUNT_OF_CROP_IRRIGATION_WATER_PER_YEAR,
     * se calcula la cantidad total de agua de riego de cultivo
     * por año de los cultivos sembrados en una parcela durante
     * un periodo definido por dos fechas
     */
    if (statisticalDataNumber == TOTAL_AMOUNT_OF_CROP_IRRIGATION_WATER_PER_YEAR) {
      newStatisticalGraph.setData(statisticalReportService.calculateTotalAmountCropIrrigationWaterPerYear(parcelId, dateFrom, dateUntil));
      newStatisticalGraph.setLabels(statisticalReportService.findYearsOfCalculationTotalAmountCropIrrigationWaterPerYear(parcelId, dateFrom, dateUntil));
      newStatisticalGraph.setAverage(UtilMath.truncateToTwoDigits(statisticalReportService.calculateAverageCropIrrigationWater(parcelId, dateFrom, dateUntil)));
      newStatisticalGraph.setText("Y: Cantidad de agua de riego [mm], X: Año, Parcela: " + newStatisticalGraph.getParcel().getName()
              + ", Período: " + UtilDate.formatDate(dateFrom) + " - " + UtilDate.formatDate(dateUntil)
              + ", Cant. total de agua de riego [mm]: "
              + statisticalReportService.calculateTotalAmountCropIrrigationWaterPerPeriod(parcelId, dateFrom, dateUntil));

      return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(statisticalGraphService.create(newStatisticalGraph))).build();
    }

    /*
     * Si el numero del dato estadistico a calcular es el
     * valor de la constante TOTAL_AMOUNT_OF_HARVEST_PER_YEAR,
     * se calcula la cantidad total de cosecha por año de una
     * parcela durante un periodo definido por dos fechas
     */
    if (statisticalDataNumber == TOTAL_AMOUNT_OF_HARVEST_PER_YEAR) {
      newStatisticalGraph.setData(statisticalReportService.calculateTotalHarvestAmountPerYear(parcelId, dateFrom, dateUntil));
      newStatisticalGraph.setLabels(statisticalReportService.findYearsOfCalculationTotalAmountHarvestPerYear(parcelId, dateFrom, dateUntil));
      newStatisticalGraph.setAverage(UtilMath.truncateToTwoDigits(statisticalReportService.calculateAverageHarvest(parcelId, dateFrom, dateUntil)));
      newStatisticalGraph.setText("Y: Cantidad cosechada [kg], X: Año, Parcela: " + newStatisticalGraph.getParcel().getName()
              + ", Período: " + UtilDate.formatDate(dateFrom) + " - " + UtilDate.formatDate(dateUntil)
              + ", Cant. total cosechada [kg]: "
              + statisticalReportService.calculateTotalHarvestPerPeriod(parcelId, dateFrom, dateUntil));

      return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(statisticalGraphService.create(newStatisticalGraph))).build();
    }

    /*
     * Si el numero del dato estadistico a calcular NO es igual
     * al valor de ninguna de las constantes para las que hay
     * controles, se calcula la cantidad total de agua de lluvia
     * que cayo por año sobre una parcela en un periodo definido
     * por dos fechas
     */
    newStatisticalGraph.setData(statisticalReportService.calculateTotalAmountRainwaterPerYear(parcelId, dateFrom, dateUntil));
    newStatisticalGraph.setLabels(statisticalReportService.findYearsOfCalculationTotalAmountRainwater(parcelId, dateFrom, dateUntil));
    newStatisticalGraph.setAverage(statisticalReportService.calculateAverageRainwaterPerPeriod(parcelId, dateFrom, dateUntil));
    newStatisticalGraph.setText("Y: Cantidad de agua de lluvia [mm], X: Año, Parcela: " + newStatisticalGraph.getParcel().getName()
            + ", Período: " + UtilDate.formatDate(dateFrom) + " - " + UtilDate.formatDate(dateUntil)
            + ", Cant. total de agua de lluvia [mm]: "
            + statisticalReportService.calculateTotalAmountRainwaterPerPeriod(parcelId, dateFrom, dateUntil));

    /*
     * Si el valor del encabezado de autorizacion de la peticion HTTP
     * dada, tiene un JWT valido, la aplicacion del lado servidor
     * devuelve el mensaje HTTP 200 (Ok) junto con los datos que el
     * cliente solicito persistir
     */
    return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(statisticalGraphService.create(newStatisticalGraph))).build();
  }

  @PUT
  @Path("recalculate/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response recalculate(@Context HttpHeaders request, @PathParam("id") int statisticalGraphId) throws IOException {
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
    if (!statisticalGraphService.checkExistence(statisticalGraphId)) {
      return Response.status(Response.Status.NOT_FOUND)
          .entity(mapper.writeValueAsString(mapper.writeValueAsString(new ErrorResponse(ReasonError.RESOURCE_NOT_FOUND)))).build();
    }

    /*
     * Si al usuario que hizo esta peticion HTTP, no le pertenece
     * el dato solicitado, la aplicacion del lado servidor
     * devuelve el mensaje HTTP 403 (Forbidden) junto con el
     * mensaje "Acceso no autorizado" (contenido en el enum
     * ReasonError) y no se realiza la operacion solicitada
     */
    if (!statisticalGraphService.checkUserOwnership(userId, statisticalGraphId)) {
      return Response.status(Response.Status.FORBIDDEN)
          .entity(mapper.writeValueAsString(mapper.writeValueAsString(new ErrorResponse(ReasonError.UNAUTHORIZED_ACCESS)))).build();
    }

    StatisticalGraph modifiedStatisticalGraph = new StatisticalGraph();
    StatisticalGraph currentStatisticalGraph = statisticalGraphService.findByUserId(userId, statisticalGraphId);

    int statisticalDataNumber = currentStatisticalGraph.getStatisticalData().getNumber();
    int parcelId = currentStatisticalGraph.getParcel().getId();

    Parcel parcel = currentStatisticalGraph.getParcel();
    Calendar dateFrom = currentStatisticalGraph.getDateFrom();
    Calendar dateUntil = currentStatisticalGraph.getDateUntil();

    /*
     * ***********************************
     * Generacion de informes estadisticos
     * ***********************************
     */

    /*
     * Si el numero del dato estadistico a calcular es el
     * valor de la constante TOTAL_AMOUNT_PLANTATIONS_PER_CROP,
     * se calcula la cantidad total de veces que se plantaron
     * los cultivos en una parcela durante un periodo
     * definido por dos fechas
     */
    if (statisticalDataNumber == TOTAL_AMOUNT_PLANTATIONS_PER_CROP) {
      modifiedStatisticalGraph.setData(statisticalReportService.calculateTotalNumberPlantationsPerCrop(parcelId, dateFrom, dateUntil));
      modifiedStatisticalGraph.setLabels(statisticalReportService.findCropNamesCalculatedPerTotalNumberPlantationsPerCrop(parcelId, dateFrom, dateUntil));
      modifiedStatisticalGraph.setText("Y: Cantidad de plantaciones, X: Cultivo, Parcela: " + parcel.getName()
              + ", Período: " + UtilDate.formatDate(dateFrom) + " - " + UtilDate.formatDate(dateUntil)
              + ", Cant. total de plantaciones: " + statisticalReportService.calculateTotalNumberPlantationsPerPeriod(parcelId, dateFrom, dateUntil));

      statisticalGraphService.modify(userId, statisticalGraphId, modifiedStatisticalGraph);
      return Response.status(Response.Status.OK).build();
    }

    /*
     * Si el numero del dato estadistico a calcular es el
     * valor de la constante TOTAL_AMOUNT_PLANTATIONS_PER_CROP_AND_YEAR,
     * se calcula la cantidad total de veces que se plantaron
     * los cultivos por año en una parcela durante un periodo
     * definido por dos fechas
     */
    if (statisticalDataNumber == TOTAL_AMOUNT_PLANTATIONS_PER_CROP_AND_YEAR) {
      List<String> cropNames = statisticalReportService.findCropNamesCalculatedPerTotalNumberPlantationsPerCropAndYear(parcelId, dateFrom, dateUntil);
      List<Integer> seedYears = statisticalReportService.findYearsCalculatedPerTotalNumberPlantationsPerCropAndYear(parcelId, dateFrom, dateUntil);

      /*
       * Arma las etiquetas <cultivo> (<año>) para el grafico
       * de barras correspondiente al informe estadistico
       * solicitado
       */
      List<String> labels = statisticalReportService.setLabelsWithCropAndYear(cropNames, seedYears);

      modifiedStatisticalGraph.setData(statisticalReportService.calculateTotalNumberPlantationsPerCropAndYear(parcelId, dateFrom, dateUntil));
      modifiedStatisticalGraph.setLabels(labels);
      modifiedStatisticalGraph.setText("Y: Cantidad de plantaciones, X: Cultivo (año), Parcela: " + parcel.getName()
              + ", Período: " + UtilDate.formatDate(dateFrom) + " - " + UtilDate.formatDate(dateUntil)
              + ", Cant. total de plantaciones: " + statisticalReportService.calculateTotalNumberPlantationsPerPeriod(parcelId, dateFrom, dateUntil));

      statisticalGraphService.modify(userId, statisticalGraphId, modifiedStatisticalGraph);
      return Response.status(Response.Status.OK).build();
    }

    /*
     * Si el numero del dato estadistico a calcular es el
     * valor de la constante TOTAL_AMOUNT_IRRIGATION_WATER_PER_CROP,
     * se calcula la cantidad total de agua de riego de los
     * cultivos plantados en una parcela durante un periodo
     * definido por dos fechas
     */
    if (statisticalDataNumber == TOTAL_AMOUNT_IRRIGATION_WATER_PER_CROP) {
      modifiedStatisticalGraph.setData(statisticalReportService.calculateTotalAmountIrrigationWaterPerCrop(parcelId, dateFrom, dateUntil));
      modifiedStatisticalGraph.setLabels(statisticalReportService.findCropNamesCalculatedPerTotalAmountIrrigationWaterPerCrop(parcelId, dateFrom, dateUntil));
      modifiedStatisticalGraph.setAverage(UtilMath.truncateToTwoDigits(statisticalReportService.calculateAverageCropIrrigationWater(parcelId, dateFrom, dateUntil)));
      modifiedStatisticalGraph.setText("Y: Cantidad de agua de riego [mm], X: Cultivo, Parcela: " + parcel.getName()
              + ", Período: " + UtilDate.formatDate(dateFrom) + " - " + UtilDate.formatDate(dateUntil)
              + ", Cant. total de agua de riego [mm]: " + statisticalReportService.calculateTotalAmountCropIrrigationWaterPerPeriod(parcelId, dateFrom, dateUntil));

      statisticalGraphService.modify(userId, statisticalGraphId, modifiedStatisticalGraph);
      return Response.status(Response.Status.OK).build();
    }

    /*
     * Si el numero del dato estadistico a calcular es el
     * valor de la constante TOTAL_AMOUNT_IRRIGATION_WATER_PER_CROP_AND_YEAR,
     * se calcula la cantidad total de agua de riego por
     * cultivo y año de los cultivos plantados en una parcela
     * durante un periodo definido por dos fechas
     */
    if (statisticalDataNumber == TOTAL_AMOUNT_IRRIGATION_WATER_PER_CROP_AND_YEAR) {
      List<String> cropNames = statisticalReportService.findCropNamesCalculatedPerTotalAmountIrrigationWaterPerCropAndYear(parcelId, dateFrom, dateUntil);
      List<Integer> seedYears = statisticalReportService.findYearsCalculatedPerTotalAmountIrrigationWaterPerCropAndYear(parcelId, dateFrom, dateUntil);

      /*
       * Arma las etiquetas <cultivo> (<año>) para el grafico
       * de barras correspondiente al informe estadistico
       * solicitado
       */
      List<String> labels = statisticalReportService.setLabelsWithCropAndYear(cropNames, seedYears);

      modifiedStatisticalGraph.setData(statisticalReportService.calculateTotalAmountIrrigationWaterPerCropAndYear(parcelId, dateFrom, dateUntil));
      modifiedStatisticalGraph.setLabels(labels);
      modifiedStatisticalGraph.setAverage(UtilMath.truncateToTwoDigits(statisticalReportService.calculateAverageCropIrrigationWater(parcelId, dateFrom, dateUntil)));
      modifiedStatisticalGraph.setText("Y: Cantidad de agua de riego [mm], X: Cultivo (año), Parcela: " + parcel.getName()
              + ", Período: " + UtilDate.formatDate(dateFrom) + " - " + UtilDate.formatDate(dateUntil)
              + ", Cant. total de agua de riego [mm]: " + statisticalReportService.calculateTotalAmountCropIrrigationWaterPerPeriod(parcelId, dateFrom, dateUntil));

      statisticalGraphService.modify(userId, statisticalGraphId, modifiedStatisticalGraph);
      return Response.status(Response.Status.OK).build();
    }

    /*
     * Si el numero del dato estadistico a calcular es el
     * valor de la constante TOTAL_HARVEST_PER_CROP,
     * se calcula la cantidad total cosechada [kg] (rendimiento)
     * de los cultivos cosechados en una parcela durante un
     * periodo definido por dos fechas
     */
    if (statisticalDataNumber == TOTAL_HARVEST_PER_CROP) {
      modifiedStatisticalGraph.setData(statisticalReportService.calculateTotalHarvestPerCrop(parcelId, dateFrom, dateUntil));
      modifiedStatisticalGraph.setLabels(statisticalReportService.findCropNamesCalculatedPerTotalHarvestPerCrop(parcelId, dateFrom, dateUntil));
      modifiedStatisticalGraph.setAverage(UtilMath.truncateToTwoDigits(statisticalReportService.calculateAverageHarvest(parcelId, dateFrom, dateUntil)));
      modifiedStatisticalGraph.setText("Y: Cantidad cosechada [kg], X: Cultivo, Parcela: " + parcel.getName()
              + ", Período: " + UtilDate.formatDate(dateFrom) + " - " + UtilDate.formatDate(dateUntil)
              + ", Cant. total cosechada [kg]: " + statisticalReportService.calculateTotalHarvestPerPeriod(parcelId, dateFrom, dateUntil));

      statisticalGraphService.modify(userId, statisticalGraphId, modifiedStatisticalGraph);
      return Response.status(Response.Status.OK).build();
    }

    /*
     * Si el numero del dato estadistico a calcular es el
     * valor de la constante TOTAL_HARVEST_PER_CROP_AND_YEAR,
     * se calcula la cantidad total cosechada [kg] (rendimiento)
     * por cultivo y año de los cultivos cosechados en una
     * parcela durante un periodo definido por dos fechas
     */
    if (statisticalDataNumber == TOTAL_HARVEST_PER_CROP_AND_YEAR) {
      List<String> cropNames = statisticalReportService.findCropNamesCalculatedPerTotalHarvestPerCropAndYear(parcelId, dateFrom, dateUntil);
      List<Integer> harvestYears = statisticalReportService.findYearsCalculatedPerTotalHarvestPerCropAndYear(parcelId, dateFrom, dateUntil);

      /*
       * Arma las etiquetas <cultivo> (<año>) para el grafico
       * de barras correspondiente al informe estadistico
       * solicitado
       */
      List<String> labels = statisticalReportService.setLabelsWithCropAndYear(cropNames, harvestYears);

      modifiedStatisticalGraph.setData(statisticalReportService.calculateTotalHarvestPerCropAndYear(parcelId, dateFrom, dateUntil));
      modifiedStatisticalGraph.setLabels(labels);
      modifiedStatisticalGraph.setAverage(UtilMath.truncateToTwoDigits(statisticalReportService.calculateAverageHarvest(parcelId, dateFrom, dateUntil)));
      modifiedStatisticalGraph.setText("Y: Cantidad cosechada [kg], X: Cultivo (año), Parcela: " + parcel.getName()
              + ", Período: " + UtilDate.formatDate(dateFrom) + " - " + UtilDate.formatDate(dateUntil)
              + ", Cant. total cosechada [kg]: " + statisticalReportService.calculateTotalHarvestPerPeriod(parcelId, dateFrom, dateUntil));

      statisticalGraphService.modify(userId, statisticalGraphId, modifiedStatisticalGraph);
      return Response.status(Response.Status.OK).build();
    }

    /*
     * Si el numero del dato estadistico a calcular es el
     * valor de la constante TOTAL_AMOUNT_PLANTATIONS_PER_TYPE_CROP,
     * se calcula la cantidad total de veces que se plantaron
     * los tipos de cultivo en una parcela durante un periodo
     * definido por dos fechas
     */
    if (statisticalDataNumber == TOTAL_AMOUNT_PLANTATIONS_PER_TYPE_CROP) {
      modifiedStatisticalGraph.setData(statisticalReportService.calculateTotalNumberPlantationsPerTypeCrop(parcelId, dateFrom, dateUntil));
      modifiedStatisticalGraph.setLabels(statisticalReportService.findTypeCropNamesCalculatedPerTotalNumberPlantationsPerTypeCrop(parcelId, dateFrom, dateUntil));
      modifiedStatisticalGraph.setText("Y: Cantidad de plantaciones, X: Tipo de cultivo, Parcela: " + parcel.getName()
              + ", Período: " + UtilDate.formatDate(dateFrom) + " - " + UtilDate.formatDate(dateUntil)
              + ", Cant. total de plantaciones: " + statisticalReportService.calculateTotalNumberPlantationsPerPeriod(parcelId, dateFrom, dateUntil));

      statisticalGraphService.modify(userId, statisticalGraphId, modifiedStatisticalGraph);
      return Response.status(Response.Status.OK).build();
    }

    /*
     * Si el numero del dato estadistico a calcular es el
     * valor de la constante TOTAL_AMOUNT_PLANTATIONS_PER_TYPE_CROP_AND_YEAR,
     * se calcula la cantidad total de veces que se plantaron
     * los tipos de cultivos por año en una parcela durante un
     * periodo definido por dos fechas
     */
    if (statisticalDataNumber == TOTAL_AMOUNT_PLANTATIONS_PER_TYPE_CROP_AND_YEAR) {
      List<String> typeCropNames = statisticalReportService.findTypeCropNamesCalculatedPerTotalNumberPlantationsPerTypeCropAndYear(parcelId, dateFrom, dateUntil);
      List<Integer> seedYears = statisticalReportService.findYearsCalculatedPerTotalNumberPlantationsPerTypeCropAndYear(parcelId, dateFrom, dateUntil);

      /*
       * Arma las etiquetas <cultivo> (<año>) para el grafico
       * de barras correspondiente al informe estadistico
       * solicitado
       */
      List<String> labels = statisticalReportService.setLabelsWithCropAndYear(typeCropNames, seedYears);

      modifiedStatisticalGraph.setData(statisticalReportService.calculateTotalNumberPlantationsPerTypeCropAndYear(parcelId, dateFrom, dateUntil));
      modifiedStatisticalGraph.setLabels(labels);
      modifiedStatisticalGraph.setText("Y: Cantidad de plantaciones, X: Tipo de cultivo (año), Parcela: " + parcel.getName()
              + ", Período: " + UtilDate.formatDate(dateFrom) + " - " + UtilDate.formatDate(dateUntil)
              + ", Cant. total de plantaciones: " + statisticalReportService.calculateTotalNumberPlantationsPerPeriod(parcelId, dateFrom, dateUntil));

      statisticalGraphService.modify(userId, statisticalGraphId, modifiedStatisticalGraph);
      return Response.status(Response.Status.OK).build();
    }

    /*
     * Si el numero del dato estadistico a calcular es el
     * valor de la constante TOTAL_AMOUNT_IRRIGATION_WATER_PER_TYPE_CROP,
     * se calcula la cantidad total de agua de riego de los
     * tipos de cultivos plantados en una parcela durante un
     * periodo definido por dos fechas
     */
    if (statisticalDataNumber == TOTAL_AMOUNT_IRRIGATION_WATER_PER_TYPE_CROP) {
      modifiedStatisticalGraph.setData(statisticalReportService.calculateTotalAmountIrrigationWaterPerTypeCrop(parcelId, dateFrom, dateUntil));
      modifiedStatisticalGraph.setLabels(statisticalReportService.findTypeCropNamesCalculatedPerTotalAmountIrrigationWaterPerTypeCrop(parcelId, dateFrom, dateUntil));
      modifiedStatisticalGraph.setAverage(UtilMath.truncateToTwoDigits(statisticalReportService.calculateAverageCropIrrigationWater(parcelId, dateFrom, dateUntil)));
      modifiedStatisticalGraph.setText("Y: Cantidad de agua de riego [mm], X: Tipo de cultivo, Parcela: " + parcel.getName()
              + ", Período: " + UtilDate.formatDate(dateFrom) + " - " + UtilDate.formatDate(dateUntil)
              + ", Cant. total de agua de riego [mm]: " + statisticalReportService.calculateTotalAmountCropIrrigationWaterPerPeriod(parcelId, dateFrom, dateUntil));

      statisticalGraphService.modify(userId, statisticalGraphId, modifiedStatisticalGraph);
      return Response.status(Response.Status.OK).build();
    }

    /*
     * Si el numero del dato estadistico a calcular es el
     * valor de la constante TOTAL_AMOUNT_IRRIGATION_WATER_PER_TYPE_CROP_AND_YEAR,
     * se calcula la cantidad total de agua de riego por
     * tipo de cultivo y año de los cultivos plantados en una
     * parcela durante un periodo definido por dos fechas
     */
    if (statisticalDataNumber == TOTAL_AMOUNT_IRRIGATION_WATER_PER_TYPE_CROP_AND_YEAR) {
      List<String> typeCropNames = statisticalReportService.findTypeCropNamesCalculatedPerTotalAmountIrrigationWaterPerTypeCropAndYear(parcelId, dateFrom, dateUntil);
      List<Integer> seedYears = statisticalReportService.findYearsCalculatedPerTotalAmountIrrigationWaterPerTypeCropAndYear(parcelId, dateFrom, dateUntil);

      /*
       * Arma las etiquetas <cultivo> (<año>) para el grafico
       * de barras correspondiente al informe estadistico
       * solicitado
       */
      List<String> labels = statisticalReportService.setLabelsWithCropAndYear(typeCropNames, seedYears);

      modifiedStatisticalGraph.setData(statisticalReportService.calculateTotalAmountIrrigationWaterPerTypeCropAndYear(parcelId, dateFrom, dateUntil));
      modifiedStatisticalGraph.setLabels(labels);
      modifiedStatisticalGraph.setAverage(UtilMath.truncateToTwoDigits(statisticalReportService.calculateAverageCropIrrigationWater(parcelId, dateFrom, dateUntil)));
      modifiedStatisticalGraph.setText("Y: Cantidad de agua de riego [mm], X: Tipo de cultivo (año), Parcela: " + parcel.getName()
              + ", Período: " + UtilDate.formatDate(dateFrom) + " - " + UtilDate.formatDate(dateUntil)
              + ", Cant. total de agua de riego [mm]: " + statisticalReportService.calculateTotalAmountCropIrrigationWaterPerPeriod(parcelId, dateFrom, dateUntil));

      statisticalGraphService.modify(userId, statisticalGraphId, modifiedStatisticalGraph);
      return Response.status(Response.Status.OK).build();
    }

    /*
     * Si el numero del dato estadistico a calcular es el
     * valor de la constante TOTAL_HARVEST_PER_TYPE_CROP,
     * se calcula la cantidad total cosechada [kg] (rendimiento)
     * de los tipos de cultivos cosechados en una parcela
     * durante un periodo definido por dos fechas
     */
    if (statisticalDataNumber == TOTAL_HARVEST_PER_TYPE_CROP) {
      modifiedStatisticalGraph.setData(statisticalReportService.calculateTotalHarvestPerTypeCrop(parcelId, dateFrom, dateUntil));
      modifiedStatisticalGraph.setLabels(statisticalReportService.findTypeCropNamesCalculatedPerTotalHarvestPerTypeCrop(parcelId, dateFrom, dateUntil));
      modifiedStatisticalGraph.setAverage(UtilMath.truncateToTwoDigits(statisticalReportService.calculateAverageHarvest(parcelId, dateFrom, dateUntil)));
      modifiedStatisticalGraph.setText("Y: Cantidad cosechada [kg], X: Tipo de cultivo, Parcela: " + parcel.getName()
              + ", Período: " + UtilDate.formatDate(dateFrom) + " - " + UtilDate.formatDate(dateUntil)
              + ", Cant. total cosechada [kg]: " + statisticalReportService.calculateTotalHarvestPerPeriod(parcelId, dateFrom, dateUntil));

      statisticalGraphService.modify(userId, statisticalGraphId, modifiedStatisticalGraph);
      return Response.status(Response.Status.OK).build();
    }

    /*
     * Si el numero del dato estadistico a calcular es el
     * valor de la constante TOTAL_HARVEST_PER_TYPE_CROP_AND_YEAR,
     * se calcula la cantidad total cosechada [kg] (rendimiento)
     * por tipo de cultivo y año de los tipos de cultivos
     * cosechados en una parcela durante un periodo definido
     * por dos fechas
     */
    if (statisticalDataNumber == TOTAL_HARVEST_PER_TYPE_CROP_AND_YEAR) {
      List<String> typeCropNames = statisticalReportService.findTypeCropNamesCalculatedPerTotalHarvestPerTypeCropAndYear(parcelId, dateFrom, dateUntil);
      List<Integer> harvestYears = statisticalReportService.findYearsCalculatedPerTotalHarvestPerTypeCropAndYear(parcelId, dateFrom, dateUntil);

      /*
       * Arma las etiquetas <cultivo> (<año>) para el grafico
       * de barras correspondiente al informe estadistico
       * solicitado
       */
      List<String> labels = statisticalReportService.setLabelsWithCropAndYear(typeCropNames, harvestYears);

      modifiedStatisticalGraph.setData(statisticalReportService.calculateTotalHarvestPerTypeCropAndYear(parcelId, dateFrom, dateUntil));
      modifiedStatisticalGraph.setLabels(labels);
      modifiedStatisticalGraph.setAverage(UtilMath.truncateToTwoDigits(statisticalReportService.calculateAverageHarvest(parcelId, dateFrom, dateUntil)));
      modifiedStatisticalGraph.setText("Y: Cantidad cosechada [kg], X: Tipo de cultivo (año), Parcela: " + parcel.getName()
              + ", Período: " + UtilDate.formatDate(dateFrom) + " - " + UtilDate.formatDate(dateUntil)
              + ", Cant. total cosechada [kg]: " + statisticalReportService.calculateTotalHarvestPerPeriod(parcelId, dateFrom, dateUntil));

      statisticalGraphService.modify(userId, statisticalGraphId, modifiedStatisticalGraph);
      return Response.status(Response.Status.OK).build();
    }

    /*
     * Si el numero del dato estadistico a calcular es el
     * valor de la constante LIFE_CYCLES_OF_PLANTED_CROPS,
     * se obtienen los ciclos de vida [dias] de los cultivos
     * sembrados en una parcela durante un periodo definido
     * por dos fechas
     */
    if (statisticalDataNumber == LIFE_CYCLES_OF_PLANTED_CROPS) {
      modifiedStatisticalGraph.setData(statisticalReportService.findLifeCyclesCropsPlantedPerPeriod(parcelId, dateFrom, dateUntil));
      modifiedStatisticalGraph.setLabels(statisticalReportService.findNamesCropPlantedPerPeriod(parcelId, dateFrom, dateUntil));
      modifiedStatisticalGraph.setText("Y: Ciclo de vida [días], X: Cultivo, Parcela: " + parcel.getName()
              + ", Período: " + UtilDate.formatDate(dateFrom) + " - " + UtilDate.formatDate(dateUntil));

      statisticalGraphService.modify(userId, statisticalGraphId, modifiedStatisticalGraph);
      return Response.status(Response.Status.OK).build();
    }

    /*
     * Si el numero del dato estadistico a calcular es el
     * valor de la constante TOTAL_NUMBER_PLANTATIONS_PER_YEAR,
     * se calcula la cantidad total de plantaciones por año
     * que se realizaron sobre una parcela durante un periodo
     * definido por dos fechas
     */
    if (statisticalDataNumber == TOTAL_NUMBER_PLANTATIONS_PER_YEAR) {
      modifiedStatisticalGraph.setData(statisticalReportService.calculateTotalNumberPlantationsPerYear(parcelId, dateFrom, dateUntil));
      modifiedStatisticalGraph.setLabels(statisticalReportService.findYearsOfCalculationTotalNumberPlantationsPerYear(parcelId, dateFrom, dateUntil));
      modifiedStatisticalGraph.setText("Y: Cantidad de plantaciones, X: Año, Parcela: " + parcel.getName()
              + ", Período: " + UtilDate.formatDate(dateFrom) + " - " + UtilDate.formatDate(dateUntil)
              + ", Cant. total de plantaciones: "
              + statisticalReportService.calculateTotalNumberPlantationsPerPeriod(parcelId, dateFrom, dateUntil));

      statisticalGraphService.modify(userId, statisticalGraphId, modifiedStatisticalGraph);
      return Response.status(Response.Status.OK).build();
    }

    /*
     * Si el numero del dato estadistico a calcular es el
     * valor de la constante TOTAL_AMOUNT_OF_CROP_IRRIGATION_WATER_PER_YEAR,
     * se calcula la cantidad total de agua de riego de cultivo
     * por año de los cultivos sembrados en una parcela durante
     * un periodo definido por dos fechas
     */
    if (statisticalDataNumber == TOTAL_AMOUNT_OF_CROP_IRRIGATION_WATER_PER_YEAR) {
      modifiedStatisticalGraph.setData(statisticalReportService.calculateTotalAmountCropIrrigationWaterPerYear(parcelId, dateFrom, dateUntil));
      modifiedStatisticalGraph.setLabels(statisticalReportService.findYearsOfCalculationTotalAmountCropIrrigationWaterPerYear(parcelId, dateFrom, dateUntil));
      modifiedStatisticalGraph.setAverage(UtilMath.truncateToTwoDigits(statisticalReportService.calculateAverageCropIrrigationWater(parcelId, dateFrom, dateUntil)));
      modifiedStatisticalGraph.setText("Y: Cantidad de agua de riego [mm], X: Año, Parcela: " + parcel.getName()
              + ", Período: " + UtilDate.formatDate(dateFrom) + " - " + UtilDate.formatDate(dateUntil)
              + ", Cant. total de agua de riego [mm]: "
              + statisticalReportService.calculateTotalAmountCropIrrigationWaterPerPeriod(parcelId, dateFrom, dateUntil));

      statisticalGraphService.modify(userId, statisticalGraphId, modifiedStatisticalGraph);
      return Response.status(Response.Status.OK).build();
    }

    /*
     * Si el numero del dato estadistico a calcular es el
     * valor de la constante TOTAL_AMOUNT_OF_HARVEST_PER_YEAR,
     * se calcula la cantidad total de cosecha por año de una
     * parcela durante un periodo definido por dos fechas
     */
    if (statisticalDataNumber == TOTAL_AMOUNT_OF_HARVEST_PER_YEAR) {
      modifiedStatisticalGraph.setData(statisticalReportService.calculateTotalHarvestAmountPerYear(parcelId, dateFrom, dateUntil));
      modifiedStatisticalGraph.setLabels(statisticalReportService.findYearsOfCalculationTotalAmountHarvestPerYear(parcelId, dateFrom, dateUntil));
      modifiedStatisticalGraph.setAverage(UtilMath.truncateToTwoDigits(statisticalReportService.calculateAverageHarvest(parcelId, dateFrom, dateUntil)));
      modifiedStatisticalGraph.setText("Y: Cantidad cosechada [kg], X: Año, Parcela: " + parcel.getName()
              + ", Período: " + UtilDate.formatDate(dateFrom) + " - " + UtilDate.formatDate(dateUntil)
              + ", Cant. total cosechada [kg]: "
              + statisticalReportService.calculateTotalHarvestPerPeriod(parcelId, dateFrom, dateUntil));

      statisticalGraphService.modify(userId, statisticalGraphId, modifiedStatisticalGraph);
      return Response.status(Response.Status.OK).build();
    }

    /*
     * Si el numero del dato estadistico a calcular NO es igual
     * al valor de ninguna de las constantes para las que hay
     * controles, se calcula la cantidad total de agua de lluvia
     * que cayo por año sobre una parcela en un periodo definido
     * por dos fechas
     */
    modifiedStatisticalGraph.setData(statisticalReportService.calculateTotalAmountRainwaterPerYear(parcelId, dateFrom, dateUntil));
    modifiedStatisticalGraph.setLabels(statisticalReportService.findYearsOfCalculationTotalAmountRainwater(parcelId, dateFrom, dateUntil));
    modifiedStatisticalGraph.setAverage(statisticalReportService.calculateAverageRainwaterPerPeriod(parcelId, dateFrom, dateUntil));
    modifiedStatisticalGraph.setText("Y: Cantidad de agua de lluvia [mm], X: Año, Parcela: " + parcel.getName()
            + ", Período: " + UtilDate.formatDate(dateFrom) + " - " + UtilDate.formatDate(dateUntil)
            + ", Cant. total de agua de lluvia [mm]: "
            + statisticalReportService.calculateTotalAmountRainwaterPerPeriod(parcelId, dateFrom, dateUntil));

    statisticalGraphService.modify(userId, statisticalGraphId, modifiedStatisticalGraph);

    /*
     * Si el valor del encabezado de autorizacion de la peticion HTTP
     * dada, tiene un JWT valido, la aplicacion del lado servidor
     * devuelve el mensaje HTTP 200 (Ok)
     */
    return Response.status(Response.Status.OK).build();
  }

  @DELETE
  @Path("/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response remove(@Context HttpHeaders request, @PathParam("id") int statisticalGraphId) throws IOException {
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
    if (!statisticalGraphService.checkExistence(statisticalGraphId)) {
      return Response.status(Response.Status.NOT_FOUND)
          .entity(mapper.writeValueAsString(mapper.writeValueAsString(new ErrorResponse(ReasonError.RESOURCE_NOT_FOUND)))).build();
    }

    /*
     * Si al usuario que hizo esta peticion HTTP, no le pertenece
     * el dato solicitado, la aplicacion del lado servidor
     * devuelve el mensaje HTTP 403 (Forbidden) junto con el
     * mensaje "Acceso no autorizado" (contenido en el enum
     * ReasonError) y no se realiza la operacion solicitada
     */
    if (!statisticalGraphService.checkUserOwnership(userId, statisticalGraphId)) {
      return Response.status(Response.Status.FORBIDDEN)
          .entity(mapper.writeValueAsString(mapper.writeValueAsString(new ErrorResponse(ReasonError.UNAUTHORIZED_ACCESS)))).build();
    }

    /*
     * Si el valor del encabezado de autorizacion de la peticion HTTP
     * dada, tiene un JWT valido, la aplicacion del lado servidor
     * devuelve el mensaje HTTP 200 (Ok) junto con los datos que el
     * cliente solicito eliminar
     */
    return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(statisticalGraphService.remove(userId, statisticalGraphId))).build();
  }

}
