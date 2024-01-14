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
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
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
import model.StatisticalReport;
import stateless.StatisticalReportServiceBean;
import stateless.SecretKeyServiceBean;
import stateless.ClimateRecordServiceBean;
import stateless.ParcelServiceBean;
import stateless.PlantingRecordServiceBean;
import stateless.SessionServiceBean;
import stateless.CropServiceBean;
import util.PersonalizedResponse;
import util.ErrorResponse;
import util.ReasonError;
import util.RequestManager;
import util.UtilDate;
import utilJwt.AuthHeaderManager;
import utilJwt.JwtManager;

@Path("/statisticalReports")
public class StatisticalReportRestServlet {

  // inject a reference to the SecretKeyServiceBean slsb
  @EJB
  StatisticalReportServiceBean statisticalReportService;

  @EJB
  SecretKeyServiceBean secretKeyService;

  @EJB
  ParcelServiceBean parcelService;

  @EJB
  ClimateRecordServiceBean climateRecordService;

  @EJB
  PlantingRecordServiceBean plantingRecordService;

  @EJB
  CropServiceBean cropService;

  @EJB
  SessionServiceBean sessionService;

  // Mapea lista de pojo a JSON
  ObjectMapper mapper = new ObjectMapper();

  private final String DATA_NOT_AVAILABLE = "Dato no disponible";
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
    return Response.status(Response.Status.OK)
        .entity(mapper.writeValueAsString(statisticalReportService.findAll(userId))).build();
  }

  @GET
  @Path("/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response find(@Context HttpHeaders request, @PathParam("id") int statisticalReportId) throws IOException {
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
    if (!statisticalReportService.checkExistence(statisticalReportId)) {
      return Response.status(Response.Status.NOT_FOUND).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.RESOURCE_NOT_FOUND))).build();
    }

    /*
     * Si al usuario que hizo esta peticion HTTP, no le pertenece
     * el dato solicitado, la aplicacion del lado servidor
     * devuelve el mensaje HTTP 403 (Forbidden) junto con el
     * mensaje "Acceso no autorizado" (contenido en el enum
     * ReasonError) y no se realiza la operacion solicitada
     */
    if (!statisticalReportService.checkUserOwnership(userId, statisticalReportId)) {
      return Response.status(Response.Status.FORBIDDEN).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.UNAUTHORIZED_ACCESS))).build();
    }

    /*
     * Si el valor del encabezado de autorizacion de la peticion HTTP
     * dada, tiene un JWT valido, la aplicacion del lado servidor
     * devuelve el mensaje HTTP 200 (Ok) junto con los datos solicitados
     * por el cliente
     */
    return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(statisticalReportService.find(userId, statisticalReportId))).build();
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

    StatisticalReport newStatisticalReport = mapper.readValue(json, StatisticalReport.class);

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
    if (newStatisticalReport.getParcel() == null) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.INDEFINITE_PARCEL)))
          .build();
    }

    /*
     * ***********************************************************
     * Control sobre la existencia de registros climaticos y
     * registros de plantacion finalizados de la parcela para
     * la que se quiere generar y persistir un informe estadistico
     * ***********************************************************
     */

    /*
     * Si la parcela para la que se quiere generar y persistir
     * un informe estadistico, NO tiene registros climaticos ni
     * registros de plantacion finalizados, la aplicacion del
     * lado servidor retorna el mensaje HTTP 400 (Bad request)
     * junto con el mensaje "La parcela seleccionada no tiene
     * registros climaticos ni registros de plantacion finalizados
     * para generar un informe estadistico" y no se realiza la
     * operacion solicitada
     */
    if (!(climateRecordService.hasClimateRecords(userId, newStatisticalReport.getParcel().getId()))
        && !(plantingRecordService.hasFinishedPlantingRecords(userId, newStatisticalReport.getParcel().getId()))) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.CLIMATE_RECORDS_AND_PLANTING_RECORDS_DO_NOT_EXIST)))
          .build();
    }

    Calendar dateFrom = newStatisticalReport.getDateFrom();
    Calendar dateUntil = newStatisticalReport.getDateUntil();

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
     * Si la cantidad de dias entre la fecha desde y la fecha
     * hasta, es estrictamente menor al menor ciclo de vida
     * (medido en dias), la aplicacion del lado servidor retorna
     * el mensaje "La fecha hasta debe ser igual a <menor ciclo
     * de vida> (ciclo de vida del cultivo con el menor ciclo
     * de vida) dias contando a partir de la fecha desde. En
     * este caso la hasta debe ser <fecha hasta calculada>."
     * y no se realiza la operacion solicitada.
     * 
     * A la diferencia entre el numero de dia en el año de la
     * fecha desde y el numero de dia en el año de la fecha
     * hasta se le suma un uno para incluir la fecha desde,
     * ya que esta incluida en el periodo formada entre ella
     * y la fecha hasta.
     */
    if ((UtilDate.calculateDifferenceBetweenDates(dateFrom, dateUntil) + 1) < cropService.findShortestLifeCycle()) {
      String message = "La fecha hasta debe ser como mínimo igual a " +
          cropService.findShortestLifeCycle()
          + " (ciclo de vida del cultivo con el menor ciclo de vida) días contando a partir de la fecha desde (incluida). "
          + "En este caso la fecha hasta debe ser "
          + UtilDate.formatDate(statisticalReportService.calculateDateUntil(newStatisticalReport.getDateFrom(),
              cropService.findShortestLifeCycle()))
          + ".";
      PersonalizedResponse newPersonalizedResponse = new PersonalizedResponse(message);
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(mapper.writeValueAsString(newPersonalizedResponse)).build();
    }

    /*
     * Si la parcela para la que se quiere generar y persistir
     * un informe estadistico, NO tiene registros climaticos ni
     * registros de plantacion finalizados en el periodo definido
     * por una fecha desde y una fecha hasta, la aplicacion del
     * lado servidor retorna el mensaje HTTP 400 (Bad request)
     * junto con el mensaje "La parcela seleccionada no tiene
     * registros climaticos ni registros de plantacion finalizados
     * en el periodo [<fecha desde>, <fecha hasta>] para generar
     * un informe estadistico" y no se realiza la operacion
     * solicitada
     */
    if (!(climateRecordService.hasClimateRecords(userId, newStatisticalReport.getParcel().getId(),
        newStatisticalReport.getDateFrom(),
        newStatisticalReport.getDateUntil()))
        && !(plantingRecordService.hasFinishedPlantingRecords(userId, newStatisticalReport.getParcel().getId(),
            newStatisticalReport.getDateFrom(),
            newStatisticalReport.getDateUntil()))) {
      String message = "La parcela seleccionada no tiene registros climaticos ni registros de plantacion finalizados en el período ["
          + UtilDate.formatDate(newStatisticalReport.getDateFrom()) + ", "
          + UtilDate.formatDate(newStatisticalReport.getDateUntil())
          + "] para generar un informe estadistico";
      PersonalizedResponse newPersonalizedResponse = new PersonalizedResponse(message);
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(mapper.writeValueAsString(newPersonalizedResponse))
          .build();
    }

    /*
     * Si en la base de datos subyacente existe un informe
     * estadistico con la fecha desde y la fecha hasta
     * elegidas asociado a una parcela de un usuario, la
     * aplicacion del lado servidor retorna el mensaje
     * HTTP 400 (Bad request) junto con el mensaje "Ya
     * existe un informe estadistico con las fechas y
     * la parcela elegidas" y no se realiza la operacion
     * solicitada
     */
    if (statisticalReportService.checkExistence(userId, newStatisticalReport.getParcel().getId(), dateFrom, dateUntil)) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.EXISTING_STATISTICAL_REPORT)))
          .build();
    }

    generateStatisticalReport(newStatisticalReport);

    /*
     * Si el valor del encabezado de autorizacion de la peticion HTTP
     * dada, tiene un JWT valido, la aplicacion del lado servidor
     * devuelve el mensaje HTTP 200 (Ok) junto con los datos que el
     * cliente solicito persistir
     */
    return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(statisticalReportService.create(newStatisticalReport))).build();
  }

  @DELETE
  @Path("/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response remove(@Context HttpHeaders request, @PathParam("id") int statisticalReportId) throws IOException {
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
    if (!statisticalReportService.checkExistence(statisticalReportId)) {
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
    if (!statisticalReportService.checkUserOwnership(userId, statisticalReportId)) {
      return Response.status(Response.Status.FORBIDDEN)
          .entity(mapper.writeValueAsString(mapper.writeValueAsString(new ErrorResponse(ReasonError.UNAUTHORIZED_ACCESS)))).build();
    }

    /*
     * Si el valor del encabezado de autorizacion de la peticion HTTP
     * dada, tiene un JWT valido, la aplicacion del lado servidor
     * devuelve el mensaje HTTP 200 (Ok) junto con los datos que el
     * cliente solicito eliminar
     */
    return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(statisticalReportService.remove(userId, statisticalReportId))).build();
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
          .entity(mapper.writeValueAsString(statisticalReportService.findAllByParcelName(userId, parcelName)))
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
          .entity(mapper.writeValueAsString(statisticalReportService.findAllByDateGreaterThanOrEqual(userId, parcel.getId(), UtilDate.toCalendar(dateFrom))))
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
          .entity(mapper.writeValueAsString(statisticalReportService.findAllByDateLessThanOrEqual(userId, parcel.getId(), UtilDate.toCalendar(dateUntil))))
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
        .entity(mapper.writeValueAsString(statisticalReportService.findByAllFilterParameters(userId, parcel.getId(), dateFromCalendar, dateUntilCalendar)))
        .build();
  }

  /**
   * Establece los atributos de un objeto de tipo StatisticalReport
   * 
   * @param statisticalReport
   */
  private void generateStatisticalReport(StatisticalReport statisticalReport) {
    int parcelId = statisticalReport.getParcel().getId();
    Calendar dateFrom = statisticalReport.getDateFrom();
    Calendar dateUntil = statisticalReport.getDateUntil();

    String mostPlantedCrop = plantingRecordService.findMostPlantedCrop(parcelId, dateFrom, dateUntil);
    String leastPlantedCrop = plantingRecordService.findLeastPlantedCrop(parcelId, dateFrom, dateUntil);
    String cropLongestLifeCyclePlanted = plantingRecordService.findCropWithLongestLifeCycle(parcelId, dateFrom,
        dateUntil);
    String cropShortestLifeCyclePlanted = plantingRecordService.findCropWithShortestLifeCycle(parcelId, dateFrom,
        dateUntil);
    int daysWithoutCrops = plantingRecordService.calculateDaysWithoutCrops(parcelId, dateFrom, dateUntil);
    double totalAmountRainwter = climateRecordService.calculateAmountRainwaterByPeriod(parcelId, dateFrom, dateUntil);

    statisticalReport.setMostPlantedCrop(mostPlantedCrop);
    statisticalReport.setLesstPlantedCrop(leastPlantedCrop);
    statisticalReport.setCropLongestLifeCyclePlanted(cropLongestLifeCyclePlanted);
    statisticalReport.setCropShortestLifeCyclePlanted(cropShortestLifeCyclePlanted);

    /*
     * El metodo calculateDaysWithoutCrops retorna el valor
     * -1 cuando una parcela no tiene registros de plantacion
     * finalizados en el periodo definido por dos fechas en
     * el que se quiere generar un informe estadistico para
     * la misma. Dicho valor representa informacion no
     * disponible.
     */
    if (daysWithoutCrops >= 0) {
      statisticalReport.setDaysWithoutCrops(String.valueOf(daysWithoutCrops));
    } else {
      statisticalReport.setDaysWithoutCrops(DATA_NOT_AVAILABLE);
    }

    /*
     * El metodo calculateAmountRainwaterByPeriod retorna el
     * valor -1.0 cuando una parcela no tiene registros
     * climaticos en el periodo definido por dos fechas en
     * el que se quiere generar un informe estadistico para
     * la misma. Dicho valor representa informacion no
     * disponible.
     */
    if (totalAmountRainwter >= 0.0) {
      statisticalReport.setTotalAmountRainwater(String.valueOf(totalAmountRainwter));
    } else {
      statisticalReport.setTotalAmountRainwater(DATA_NOT_AVAILABLE);
    }

  }

}
