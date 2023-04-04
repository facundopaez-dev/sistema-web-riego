package servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Calendar;
import java.util.Collection;
import javax.ejb.EJB;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import model.StatisticalReport;
import stateless.StatisticalReportServiceBean;
import stateless.SecretKeyServiceBean;
import stateless.ClimateRecordServiceBean;
import stateless.PlantingRecordServiceBean;
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
  ClimateRecordServiceBean climateRecordService;

  @EJB
  PlantingRecordServiceBean plantingRecordService;

  @EJB
  CropServiceBean cropService;

  // Mapea lista de pojo a JSON
  ObjectMapper mapper = new ObjectMapper();

  private final String DATA_NOT_AVAILABLE = "Dato no disponible";

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
     * Obtiene el ID de usuario contenido en la carga util del
     * JWT del encabezado de autorizacion de una peticion HTTP
     */
    int userId = JwtManager.getUserId(jwt, secretKeyService.find().getValue());

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
     * Si el dato solicitado no existe en la base de datos
     * subyacente, la aplicacion del lado servidor devuelve
     * el mensaje HTTP 404 (Not found) junto con el mensaje
     * "Recurso no encontrado" y no se realiza la operacion
     * solicitada
     */
    if (!statisticalReportService.checkExistence(statisticalReportId)) {
      return Response.status(Response.Status.NOT_FOUND)
          .entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.RESOURCE_NOT_FOUND))).build();
    }

    /*
     * Obtiene el JWT del valor del encabezado de autorizacion
     * de una peticion HTTP
     */
    String jwt = AuthHeaderManager.getJwt(AuthHeaderManager.getAuthHeaderValue(request));

    /*
     * Obtiene el ID de usuario contenido en la carga util del
     * JWT del encabezado de autorizacion de una peticion HTTP
     */
    int userId = JwtManager.getUserId(jwt, secretKeyService.find().getValue());

    /*
     * Si al usuario que hizo esta peticion HTTP, no le pertenece
     * el dato solicitado, la aplicacion del lado servidor
     * devuelve el mensaje HTTP 403 (Forbidden) junto con el
     * mensaje "Acceso no autorizado" (contenido en el enum
     * ReasonError) y no se realiza la operacion solicitada
     */
    if (!statisticalReportService.checkUserOwnership(userId, statisticalReportId)) {
      return Response.status(Response.Status.FORBIDDEN)
          .entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.UNAUTHORIZED_ACCESS))).build();
    }

    /*
     * Si el valor del encabezado de autorizacion de la peticion HTTP
     * dada, tiene un JWT valido, la aplicacion del lado servidor
     * devuelve el mensaje HTTP 200 (Ok) junto con los datos solicitados
     * por el cliente
     */
    return Response.status(Response.Status.OK)
        .entity(mapper.writeValueAsString(statisticalReportService.find(userId, statisticalReportId))).build();
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
     * Si el objeto de tipo String referenciado por la
     * referencia contenida en la variable de tipo por
     * referencia json de tipo String, esta vacio,
     * significa que el usuario intento crear un dato
     * con los campos vacios del formulario de registro.
     * Por lo tanto, la aplicacion del lado servidor
     * retorna el mensaje HTTP 400 (Bad request) junto
     * con el mensaje "Debe completar todos los campos
     * del formulario" y no se realiza la operacion
     * solicitada
     */
    if (json.isEmpty()) {
      return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorResponse(ReasonError.EMPTY_FORM)).build();
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
      return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorResponse(ReasonError.INDEFINITE_PARCEL))
          .build();
    }

    /*
     * Obtiene el JWT del valor del encabezado de autorizacion
     * de una peticion HTTP
     */
    String jwt = AuthHeaderManager.getJwt(AuthHeaderManager.getAuthHeaderValue(request));

    /*
     * Obtiene el ID de usuario contenido en la carga util del
     * JWT del encabezado de autorizacion de una peticion HTTP
     */
    int userId = JwtManager.getUserId(jwt, secretKeyService.find().getValue());

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
          .entity(new ErrorResponse(ReasonError.CLIMATE_RECORDS_AND_PLANTING_RECORDS_DO_NOT_EXIST)).build();
    }

    /*
     * ***********************************************
     * Controles sobre la fecha desde y la fecha hasta
     * ***********************************************
     */

    /*
     * Si la fecha desde de un registro climatico a generar
     * y persistir NO esta definida, la aplicacion del lado
     * servidor retorna el mensaje HTTP 400 (Bad request)
     * junto con el mensaje "La fecha desde debe estar
     * definida" y no se realiza la operacion solicitada
     */
    if (newStatisticalReport.getDateFrom() == null) {
      return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorResponse(ReasonError.DATE_FROM_UNDEFINED))
          .build();
    }

    /*
     * Si la fecha hasta esta definida, y la fecha desde es mayor
     * o igual a la fecha hasta, la aplicacion del lado servidor
     * retorna el mensaje HTTP 400 (Bad request) junto con el
     * mensaje "La fecha desde no debe ser mayor o igual a la
     * fecha hasta"
     */
    if ((newStatisticalReport.getDateUntil() != null)
        && (newStatisticalReport.getDateFrom().compareTo(newStatisticalReport.getDateUntil()) >= 0)) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(new ErrorResponse(ReasonError.DATE_FROM_AND_DATE_UNTIL_OVERLAPPING)).build();
    }

    /*
     * Si la diferencia entre la fecha hasta y la fecha desde
     * elegidas, es estrictamente menor al menor ciclo de vida
     * (medido en dias), la aplicacion del lado servidor retorna
     * el mensaje "La fecha hasta debe ser igual a <menor ciclo
     * de vida> dias contando a partir de la fecha desde, en este
     * caso la hasta debe ser <fecha hasta calculada>" y no se
     * realiza la operacion solicitada.
     * 
     * A la diferencia entre el numero de dia en el año de la
     * fecha hasta y el numero de dia en el año de la fecha
     * desde se le suma un uno para incluir la fecha desde,
     * ya que esta incluida en el periodo formada entre ella
     * y la fecha hasta, la cual, se genera sumando el menor
     * ciclo de vida (medido en dias) a la fecha desde menos
     * uno, el cual, es para que la cantidad de dias entre la
     * fecha desde y la fecha hasta sea igual al menor ciclo
     * de vida del ciclo de vida de los cultivos registrados
     * en la base de datos subyacente.
     */
    if ((newStatisticalReport.getDateUntil() != null)
        && ((UtilDate.calculateDifferenceBetweenDates(newStatisticalReport.getDateFrom(),
            newStatisticalReport.getDateUntil()) + 1) < cropService.findShortestLifeCycle())) {
      String message = "La fecha hasta debe ser como mínimo igual a " +
          cropService.findShortestLifeCycle()
          + " días contando a partir de la fecha desde (incluida), en este caso la fecha hasta debe ser "
          + UtilDate.formatDate(statisticalReportService.calculateDateUntil(newStatisticalReport.getDateFrom(),
              cropService.findShortestLifeCycle()));
      PersonalizedResponse newPersonalizedResponse = new PersonalizedResponse(message);
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(newPersonalizedResponse).build();
    }

    /*
     * *******************************************************************
     * Generacion automatica de la fecha hasta en caso de NO esta definida
     * *******************************************************************
     */

    /*
     * Si la fecha hasta NO esta definida, la aplicacion del
     * lado servidor la genera de manera automatica como la
     * suma entre el numero de dia en el año de la fecha desde
     * y el menor ciclo de vida (medido en dias) del cultivo
     * que tiene el menor ciclo de vida, menos uno.
     * 
     * El menos uno es para que la cantidad de dias entre la
     * fecha desde y la fecha hasta sea igual al menor ciclo
     * de vida del cultivo que tiene el menor ciclo de vida
     * registrado en la base de datos subyacente.
     */
    if (newStatisticalReport.getDateUntil() == null) {
      newStatisticalReport.setDateUntil(statisticalReportService.calculateDateUntil(newStatisticalReport.getDateFrom(),
          cropService.findShortestLifeCycle()));
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
          .entity(newPersonalizedResponse)
          .build();
    }

    generateStatisticalReport(newStatisticalReport);

    /*
     * Si el valor del encabezado de autorizacion de la peticion HTTP
     * dada, tiene un JWT valido, la aplicacion del lado servidor
     * devuelve el mensaje HTTP 200 (Ok) junto con los datos que el
     * cliente solicito persistir
     */
    return Response.status(Response.Status.OK)
        .entity(mapper.writeValueAsString(statisticalReportService.create(newStatisticalReport))).build();
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
     * Si el dato solicitado no existe en la base de datos
     * subyacente, la aplicacion del lado servidor devuelve
     * el mensaje HTTP 404 (Not found) junto con el mensaje
     * "Recurso no encontrado" y no se realiza la operacion
     * solicitada
     */
    if (!statisticalReportService.checkExistence(statisticalReportId)) {
      return Response.status(Response.Status.NOT_FOUND)
          .entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.RESOURCE_NOT_FOUND))).build();
    }

    /*
     * Obtiene el JWT del valor del encabezado de autorizacion
     * de una peticion HTTP
     */
    String jwt = AuthHeaderManager.getJwt(AuthHeaderManager.getAuthHeaderValue(request));

    /*
     * Obtiene el ID de usuario contenido en la carga util del
     * JWT del encabezado de autorizacion de una peticion HTTP
     */
    int userId = JwtManager.getUserId(jwt, secretKeyService.find().getValue());

    /*
     * Si al usuario que hizo esta peticion HTTP, no le pertenece
     * el dato solicitado, la aplicacion del lado servidor
     * devuelve el mensaje HTTP 403 (Forbidden) junto con el
     * mensaje "Acceso no autorizado" (contenido en el enum
     * ReasonError) y no se realiza la operacion solicitada
     */
    if (!statisticalReportService.checkUserOwnership(userId, statisticalReportId)) {
      return Response.status(Response.Status.FORBIDDEN)
          .entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.UNAUTHORIZED_ACCESS))).build();
    }

    /*
     * Si el valor del encabezado de autorizacion de la peticion HTTP
     * dada, tiene un JWT valido, la aplicacion del lado servidor
     * devuelve el mensaje HTTP 200 (Ok) junto con los datos que el
     * cliente solicito eliminar
     */
    return Response.status(Response.Status.OK)
        .entity(mapper.writeValueAsString(statisticalReportService.remove(userId, statisticalReportId))).build();
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
     * Obtiene el ID de usuario contenido en la carga util del
     * JWT del encabezado de autorizacion de una peticion HTTP
     */
    int userId = JwtManager.getUserId(jwt, secretKeyService.find().getValue());

    /*
     * Si el valor del encabezado de autorizacion de la peticion HTTP
     * dada, tiene un JWT valido, la aplicacion del lado servidor
     * devuelve el mensaje HTTP 200 (Ok) junto con los datos solicitados
     * por el cliente
     */
    return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(statisticalReportService.findAllByParcelName(userId, givenParcelName))).build();
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
