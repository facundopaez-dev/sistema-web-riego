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
import stateless.LatitudeServiceBean;
import climate.ClimateClient;
import et.HargreavesEto;
import et.Etc;
import irrigation.WaterMath;
import model.ClimateRecord;
import model.Crop;
import model.IrrigationRecord;
import model.Parcel;
import model.PlantingRecord;
import model.PlantingRecordStatus;
import model.IrrigationWaterNeedFormData;
import util.ErrorResponse;
import util.ReasonError;
import util.RequestManager;
import util.UtilDate;
import utilJwt.AuthHeaderManager;
import utilJwt.JwtManager;

@Path("/plantingRecords")
public class PlantingRecordRestServlet {

  // inject a reference to the PlantingRecordServiceBean
  @EJB PlantingRecordServiceBean plantingRecordService;

  // inject a reference to the ParcelServiceBean
  @EJB ParcelServiceBean parcelService;

  // inject a reference to the ClimateRecordServiceBean
  @EJB ClimateRecordServiceBean climateRecordService;

  // inject a reference to the CropServiceBean
  @EJB CropServiceBean cropService;

  // inject a reference to the IrrigationRecordServiceBean
  @EJB IrrigationRecordServiceBean irrigationRecordService;

  // inject a reference to the PlantingRecordStatusServiceBean
  @EJB PlantingRecordStatusServiceBean statusService;

  // inject a reference to the SolarRadiationServiceBean
  @EJB SolarRadiationServiceBean solarService;

  // inject a reference to the MaximumInsolationServiceBean
  @EJB MaximumInsolationServiceBean insolationService;

  // inject a reference to the SecretKeyServiceBean
  @EJB SecretKeyServiceBean secretKeyService;

  // inject a reference to the MonthServiceBean
  @EJB MonthServiceBean monthService;

  // inject a reference to the LatitudeServiceBean
  @EJB LatitudeServiceBean latitudeService;

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
     * Si el objeto correspondiente a la referencia contenida
     * en la variable de tipo por referencia de tipo String json,
     * esta vacio, significa que el formulario del dato correspondiente
     * a esta clase, esta vacio. Por lo tanto, la aplicacion del
     * lado servidor retorna el mensaje HTTP 400 (Bad request)
     * junto con el mensaje "Debe completar todos los campos
     * del formulario" y no se realiza la operacion solicitada
     */
    if (json.isEmpty()) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.EMPTY_FORM))).build();
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

    PlantingRecordStatus givenStatus = newPlantingRecord.getStatus();
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
    if (statusService.equals(givenStatus, finishedStatus) || (statusService.equals(givenStatus, waitingStatus))) {
      newPlantingRecord.setIrrigationWaterNeed(NOT_AVAILABLE);
    }

    /*
     * Inicialmente la necesidad de agua de riego de un registro
     * de plantacion que tiene el estado "En desarrollo" es 0
     */
    if (statusService.equals(givenStatus, developmentStatus)) {
      newPlantingRecord.setIrrigationWaterNeed(String.valueOf(0));
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
     * Si un registro de plantacion nuevo tiene el estado "En
     * desarrollo", se calcula la necesidad de agua de riego
     * del cultivo que esta en desarrollo en la fecha actual.
     * Esto se hace porque un registro de plantacion representa
     * a un cultivo sembrado. Por lo tanto, si hay un registro
     * de plantacion en desarrollo es porque hay un cultivo en
     * desarrollo, y al existir este cultivo se debe calcular
     * la necesidad de agua de riego del mismo.
     */
    if (statusService.equals(givenStatus, developmentStatus)) {
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
       * Calcula el agua excedente para NUMBER_DAYS registros
       * climaticos de una parcela anteriores a la fecha actual
       */
      calculateExcessWaterForPeriodOnCreationAndCalculateIrrigationWaterNeed(newPlantingRecord.getParcel());

      /*
       * **********************************************
       * Calculo de la necesidad de agua de riego de un
       * cultivo en desarrollo en la fecha actual
       * **********************************************
       */
      plantingRecordService.updateIrrigationWaterNeed(newPlantingRecord.getId(), newPlantingRecord.getParcel(),
          String.valueOf(calculateIrrigationWaterNeedCurrentDate(newPlantingRecord)));
    }

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
  @Path("/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response modify(@Context HttpHeaders request, @PathParam("id") int plantingRecordId, String json) throws IOException {
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
    if (!plantingRecordService.checkExistence(plantingRecordId)) {
      return Response.status(Response.Status.NOT_FOUND).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.RESOURCE_NOT_FOUND))).build();
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
    if (!plantingRecordService.checkUserOwnership(userId, plantingRecordId)) {
      return Response.status(Response.Status.FORBIDDEN).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.UNAUTHORIZED_ACCESS))).build();
    }

    /*
     * Si el registro de plantacion a modificar NO es modificable,
     * la aplicacion del lador servidor retorna el mensaje
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
     * junto con el mensaje "Debe completar todos los campos
     * del formulario" y no se realiza la operacion solicitada
     */
    if (json.isEmpty()) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.EMPTY_FORM))).build();
    }

    PlantingRecord modifiedPlantingRecord = mapper.readValue(json, PlantingRecord.class);
    PlantingRecord currentPlantingRecord = plantingRecordService.find(plantingRecordId);

    Calendar seedDate = modifiedPlantingRecord.getSeedDate();
    Calendar harvestDate = modifiedPlantingRecord.getHarvestDate();

    PlantingRecordStatus givenStatus = currentPlantingRecord.getStatus();
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
    if (!modifiedPlantingRecord.getModifiable() && (statusService.equals(givenStatus, developmentStatus) || statusService.equals(givenStatus, waitingStatus))) {
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
     * Se establece el estado de un registro de plantacion
     * modificado en base a la fecha de siembra y la fecha de
     * cosecha de su cultivo
     */
    modifiedPlantingRecord.setStatus(statusService.calculateStatus(modifiedPlantingRecord));
    PlantingRecordStatus modifiedStatus = modifiedPlantingRecord.getStatus();

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
    if (statusService.equals(modifiedStatus, finishedStatus) || (statusService.equals(modifiedStatus, waitingStatus))) {
      modifiedPlantingRecord.setIrrigationWaterNeed(NOT_AVAILABLE);
    }

    /*
     * Se persisten los cambios realizados en el registro
     * de plantacion
     */
    modifiedPlantingRecord = plantingRecordService.modify(userId, plantingRecordId, modifiedPlantingRecord);

    /*
     * Si un registro de plantacion modificado tiene el estado "En
     * desarrollo", se calcula la necesidad de agua de riego
     * del cultivo que esta en desarrollo en la fecha actual.
     * Esto se hace porque un registro de plantacion representa
     * a un cultivo sembrado. Por lo tanto, si hay un registro
     * de plantacion en desarrollo es porque hay un cultivo en
     * desarrollo, y al existir este cultivo se debe calcular
     * la necesidad de agua de riego del mismo.
     */
    if (statusService.equals(modifiedStatus, developmentStatus)) {

      /*
       * Calcula el agua excedente para NUMBER_DAYS registros
       * climaticos de una parcela anteriores a la fecha actual
       */
      calculateExcessWaterForPeriodOnModification(currentPlantingRecord, modifiedPlantingRecord.getCrop(),
          modifiedPlantingRecord.getParcel(), modifiedPlantingRecord.getSeedDate(),
          modifiedPlantingRecord.getHarvestDate());

      /*
       * ********************************************************
       * Se calcula la necesidad de agua de riego del registro de
       * plantacion (modificado) en base a la parcela y al cultivo
       * modificados. Esto es que se calcula la necesidad de agua
       * de riego del cultivo en desarrollo en la fecha actual.
       * ********************************************************
       */
      plantingRecordService.updateIrrigationWaterNeed(plantingRecordId, modifiedPlantingRecord.getParcel(),
          String.valueOf(calculateIrrigationWaterNeedCurrentDate(modifiedPlantingRecord)));
    }

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
    if (!plantingRecordService.checkUserOwnership(userId, plantingRecordId)) {
      return Response.status(Response.Status.FORBIDDEN).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.UNAUTHORIZED_ACCESS))).build();
    }

    /*
     * Si se intenta calcular la necesidad de agua de riego de
     * un cultivo perteneciente a un registro de plantacion finalizado
     * o en espera, la aplicacion del lado servidor retorna el
     * mensaje HTTP 400 (Bad request) junto con el mensaje "No
     * esta permitido calcular la necesidad de agua de riego de
     * un cultivo finalizado o en espera" y no se realiza la
     * operacion solicitada
     */
    if (plantingRecordService.checkFinishedStatus(plantingRecordId) || plantingRecordService.checkWaitingStatus(plantingRecordId)) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.INVALID_REQUEST_CALCULATION_IRRIGATION_WATER_NEED))).build();
    }

    PlantingRecord givenPlantingRecord = plantingRecordService.find(plantingRecordId);
    Parcel givenParcel = givenPlantingRecord.getParcel();

    /*
     * Calcula el agua excedente para NUMBER_DAYS registros
     * climaticos de una parcela anteriores a la fecha actual
     */
    calculateExcessWaterForPeriodOnCreationAndCalculateIrrigationWaterNeed(givenParcel);

    /*
     * **********************************************
     * Calculo de la necesidad de agua de riego de un
     * cultivo en desarrollo en la fecha actual
     * **********************************************
     */
    double irrigationWaterNeedCurrentDate = calculateIrrigationWaterNeedCurrentDate(givenPlantingRecord);

    /*
     * Se actualiza el atributo irrigationWaterNeed del registro
     * de plantacion en desarrollo sobre el que se solicita calcular
     * la necesidad de agua de riego del cultivo que contiene
     */
    plantingRecordService.updateIrrigationWaterNeed(plantingRecordId, givenParcel, String.valueOf(irrigationWaterNeedCurrentDate));

    IrrigationWaterNeedFormData irrigationWaterNeedFormData = new IrrigationWaterNeedFormData();
    irrigationWaterNeedFormData.setDate(UtilDate.getCurrentDate());
    irrigationWaterNeedFormData.setIrrigationWaterNeed(irrigationWaterNeedCurrentDate);
    irrigationWaterNeedFormData.setParcel(givenParcel);
    irrigationWaterNeedFormData.setCrop(givenPlantingRecord.getCrop());

    /*
     * Si el valor del encabezado de autorizacion de la peticion HTTP
     * dada, tiene un JWT valido, la aplicacion del lado servidor
     * devuelve el mensaje HTTP 200 (Ok) junto con los datos
     * pertinentes
     */
    return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(irrigationWaterNeedFormData)).build();
  }

  /**
   * Calcula la necesidad de agua de riego de un cultivo en
   * desarrollo en la fecha actual
   * 
   * @param developingPlantingRecord
   * @return double que representa la necesidad de agua de
   * riego de un cultivo en desarrollo en la fecha actual
   */
  private double calculateIrrigationWaterNeedCurrentDate(PlantingRecord developingPlantingRecord) {
    /*
     * Obtiene el registro climatico perteneciente a una
     * parcela de la fecha actual con la ETo, la ETc y el
     * agua excedente actualizadas. El motivo de esto es
     * que para calcular correctamente la necesidad de agua
     * de riego de un cultivo en desarrollo en la fecha actual
     * se debe utilizar estos datos actualizados.
     */
    ClimateRecord currentClimateRecord = getUpdatedCurrentClimateRecord(developingPlantingRecord);
    double totalIrrigationWaterCurrentDate = irrigationRecordService.calculateTotalIrrigationWaterCurrentDate(developingPlantingRecord.getParcel());

    return WaterMath.calculateIrrigationWaterNeed(currentClimateRecord.getEtc(), currentClimateRecord.getPrecip(),
        totalIrrigationWaterCurrentDate, getExcessWaterYesterdayFromDate(developingPlantingRecord.getParcel(), UtilDate.getCurrentDate()));
  }

  /**
   * @param developingPlantingRecord
   * @return referencia a un objeto de tipo ClimateRecord
   * que representa el registro climatico de la fecha
   * actual perteneciente a una parcela dada con la ETc,
   * la ETo y el agua excedente actualizadas
   */
  private ClimateRecord getUpdatedCurrentClimateRecord(PlantingRecord developingPlantingRecord) {
    /*
     * Actualizacion de la ETo, la ETc y el agua excedente del
     * registro climatico de la fecha actual perteneciente a una
     * parcela dada. Estas actualizaciones son necesarias por si
     * la temperatura minima, la temperatura maxima y los coeficientes
     * de cultivo (KCs) de un cultivo son modificados. En caso
     * de ser asi, se debe calcular nuevamente la ETo, la ETc
     * y el agua excedente del registro climatico de la fecha
     * actual perteneciente a una parcela dada.
     */
    updateEtoAndEtcForCurrentClimateRecord(developingPlantingRecord);
    updateExcessWaterForCurrentClimateRecord(developingPlantingRecord);
    return getCurrentClimateRecord(developingPlantingRecord.getParcel());
  }

  /**
   * Calcula y actualiza la ETo y la ETc de un registro
   * climatico de la fecha actual perteneciente a una
   * parcela
   * 
   * @param developingPlantingRecord
   */
  private void updateEtoAndEtcForCurrentClimateRecord(PlantingRecord developingPlantingRecord) {
    ClimateRecord currentClimateRecord = getCurrentClimateRecord(developingPlantingRecord.getParcel());
    double currentEto = calculateEtoForClimateRecord(currentClimateRecord);
    double currentEtc = calculateEtcForClimateRecord(currentEto, developingPlantingRecord);

    climateRecordService.updateEtoAndEtc(UtilDate.getCurrentDate(), developingPlantingRecord.getParcel(), currentEto, currentEtc);
  }

  /**
   * Calcula y actualiza el agua excedente de un registro climatico
   * de la fecha actual perteneciente a una parcela.
   * 
   * Hay que tener en cuenta que este metodo debe ser invocado luego
   * de invocar al metodo updateEtoAndEtcForCurrentClimateRecord
   * de esta clase, ya que se requieren la ETo y la ETc actualizadas
   * para calcular correctamente el agua excedente de un registro
   * climatico de la fecha actual.
   * 
   * @param developingPlantingRecord
   */
  private void updateExcessWaterForCurrentClimateRecord(PlantingRecord developingPlantingRecord) {
    ClimateRecord currentClimateRecord = getCurrentClimateRecord(developingPlantingRecord.getParcel());
    double currentEto = currentClimateRecord.getEto();
    double currentEtc = currentClimateRecord.getEtc();
    double excessWaterCurrentDate = calculateExcessWaterForClimateRecord(currentEto, currentEtc, currentClimateRecord);

    climateRecordService.updateExcessWater(UtilDate.getCurrentDate(), developingPlantingRecord.getParcel(), excessWaterCurrentDate);
  }

  /**
   * @param givenParcel
   * @return referencia a un objeto de tipo ClimateRecord
   * que representa el registro climatico de la fecha
   * actual perteneciente a una parcela dada
   */
  private ClimateRecord getCurrentClimateRecord(Parcel givenParcel) {
    ClimateRecord currentClimateRecord = null;

    /*
     * Si existe el registro climatico de la fecha actual perteneciente
     * a una parcela, se lo retorna.
     * 
     * El metodo automatico getCurrentWeatherDataset de la clase
     * ClimateRecordManager se ocupa de obtener y persistir, en
     * forma de registro climatico, los datos meteorologicos de
     * cada dia para todas las parcelas activas de la base de datos
     * subyacente. Ademas, de esto calcula la ETo (evapotranspiracion
     * del cultivo de referencia) y la ETc (evapotranspiracion del
     * cultivo bajo condiciones estandar) de cada registro climatico
     * que persiste.
     * 
     * El metodo getCurrentWeatherDataset calcula la ETc si la
     * parcela para la que obtiene y persiste datos meteorologicos,
     * tiene un cultivo sembrado y en desarrollo en la fecha
     * actual.
     */
    if (climateRecordService.checkExistence(UtilDate.getCurrentDate(), givenParcel)) {
      currentClimateRecord = climateRecordService.find(UtilDate.getCurrentDate(), givenParcel);
    }

    /*
     * Si NO existe el registro climatico de la fecha actual perteneciente
     * a una parcela dada, se lo solicita al servicio meteorologico
     * utilizado y se lo persiste. Se hace esto porque este metodo
     * es para el metodo calculateIrrigationWaterNeedCurrentDate, el
     * cual tiene la responsabilidad de calcular la necesidad de agua
     * de riego de un cultivo en desarrollo en la fecha actual. Para
     * realizar dicho calculo se requiere la ETo (*) de la fecha actual,
     * la cual se debe calcular con los datos meteorologicos de la fecha
     * actual. Ademas de la ETo, se requieren de otros datos: ETc (**)
     * de la fecha actual, agua de lluvia de la fecha actual, agua de
     * riego de la fecha actual y el agua excedente del dia inmediatamente
     * anterior a la fecha actual.
     * 
     * (*) evapotranspiracion del cultivo de referencia
     * (**) evapotranspiracion del cultivo bajo condiciones
     * estandar
     */
    if (!climateRecordService.checkExistence(UtilDate.getCurrentDate(), givenParcel)) {
      currentClimateRecord = climateRecordService.persistCurrentClimateRecord(givenParcel);
    }

    return currentClimateRecord;
  }

  /**
   * Calcula la ETo (evapotranspiracion del cultivo de referencia)
   * con los datos meteorologicos de una fecha dada, la cual esta
   * determinada por un registro climatico, ya que este tiene
   * fecha
   * 
   * @param givenClimateRecord
   * @return double que representa la ETo (evapotranspiracion del
   * cultivo de referencia) calculada en una fecha con los datos
   * meteorologicos de un registro climatico perteneciente a una
   * parcela de una fecha dada
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
     * este tiene fecha
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
   * @return double que representa la ETc (evapotranspiracion
   * del cultivo bajo condiciones estandar) de un cultivo
   * calculada con una ETo de una fecha dada
   */
  private double calculateEtcForClimateRecord(double givenEto, PlantingRecord givenPlantingRecord) {
    return Etc.calculateEtc(givenEto, cropService.getKc(givenPlantingRecord.getCrop(), givenPlantingRecord.getSeedDate()));
  }

  /**
   * @param etoGivenDate
   * @param etcGivenDate
   * @param givenClimateRecord
   * @return double que representa el agua excedente que hay
   * en una parcela en una fecha dada, la cual esta determinada
   * por un registro climatico, ya que este tiene fecha
   */
  private double calculateExcessWaterForClimateRecord(double etoGivenDate, double etcGivenDate, ClimateRecord givenClimateRecord) {
    double totalIrrigationWaterGivenDate = irrigationRecordService
        .calculateTotalIrrigationWaterGivenDate(givenClimateRecord.getDate(), givenClimateRecord.getParcel());
    return WaterMath.calculateExcessWater(etoGivenDate, etcGivenDate, givenClimateRecord.getPrecip(),
        totalIrrigationWaterGivenDate, getExcessWaterYesterdayFromDate(givenClimateRecord.getParcel(), givenClimateRecord.getDate()));
  }

  /**
   * @param givenParcel
   * @param givenDate
   * @return double que representa el agua excedente del dia
   * inmediatamente anterior a una fecha si existe el registro
   * climatico de dicho dia perteneciente a una parcela dada.
   * Si no existe, double que representa el agua excedente de
   * antes de ayer si existe el registro climatico de dicha dia
   * perteneciente a una parcela dada. En caso de que no exista
   * ninguno de estos dos registros de una parcela dada, 0.0.
   */
  private double getExcessWaterYesterdayFromDate(Parcel givenParcel, Calendar givenDate) {
    double excessWaterYesterday = 0.0;

    /*
     * Obtiene la fecha inmediatamente anterior a una fecha
     * dada
     */
    Calendar yesterdayDate = UtilDate.getYesterdayDateFromDate(givenDate);

    /*
     * Si el registro climatico perteneciente a una parcela del
     * dia inmediatamente anterior a una fecha, existe, se obtiene
     * el agua excedente de dicho dia. En caso contrario, si el
     * registro climatico del dia anterior al dia inmediatamente
     * anterior a una fecha (esto es, el dia antes de ayer) existe,
     * se obtiene el agua excedente de dicho dia.
     * 
     * Si NO existen ninguno de estos dos registros climaticos para
     * una parcela dada, se asume que el agua excedente del dia
     * inmediatamente anterior a una fecha es 0.
     */
    if (climateRecordService.checkExistence(yesterdayDate, givenParcel)) {
      excessWaterYesterday = climateRecordService.find(yesterdayDate, givenParcel).getExcessWater();
    } else {
      Calendar dateBeforeYesterday = Calendar.getInstance();
      dateBeforeYesterday.set(Calendar.DAY_OF_YEAR, (yesterdayDate.get(Calendar.DAY_OF_YEAR) - 1));

      if (climateRecordService.checkExistence(dateBeforeYesterday, givenParcel)) {
        excessWaterYesterday = climateRecordService.find(dateBeforeYesterday, givenParcel).getExcessWater();
      }

    }

    return excessWaterYesterday;
  }

  /**
   * Calcula el agua excedente de NUMBER_DAYS registros climaticos
   * de una parcela anteriores a la fecha actual
   * 
   * @param givenParcel
   */
  private void calculateExcessWaterForPeriodOnCreationAndCalculateIrrigationWaterNeed(Parcel givenParcel) {
    /*
     * Solicita y persiste una cantidad NUMBER_DAYS de
     * registros climaticos de una parcela anteriores a
     * la fecha actual, si no existen en la base de datos
     * subyacente
     */
    requestAndPersistClimateRecordsForPeriod(givenParcel);

    // 1. Antes de calcular el agua excedente para un periodo se debe calcular la ETo y la ETc

    /*
     * Calcula el agua excedente de NUMBER_DAYS registros
     * climaticos de una parcela anteriores a la fecha actual
     */
    calculateExcessWaterForPeriod(givenParcel);
  }

  /**
   * Calcula el agua excedente de NUMBER_DAYS registros climaticos
   * de una parcela anteriores a la fecha actual
   * 
   * @param originalPlantingRecord
   * @param modifiedCrop
   * @param modifiedParcel
   * @param modifiedSeedDate
   */
  private void calculateExcessWaterForPeriodOnModification(PlantingRecord originalPlantingRecord, Crop modifiedCrop,
      Parcel modifiedParcel, Calendar modifiedSeedDate, Calendar modifiedHarvestDate) {
    Crop originalCrop = originalPlantingRecord.getCrop();
    Parcel originalParcel = originalPlantingRecord.getParcel();
    Calendar originalSeedDate = originalPlantingRecord.getSeedDate();
    Calendar originalHarvestDate = originalPlantingRecord.getHarvestDate();

    // 1. Si se cambia la parcela, la fecha de siembra o la fecha de cosecha se deben solicitar y persistir los registros climaticos
    // 2. Si se cambia el cultivo no es necesario solicitar y persistir los registros climaticos

    /*
     * Si el cultivo fue modificado y la parcela no (esto
     * es en la modificacion de un registro de plantacion),
     * se recalcula la ETo (evapotranspiracion del cultivo
     * de referencia) y la ETc (evapotranspiracion del cultivo
     * bajo condiciones estandar) de los NUMBER_DAYS registros
     * climaticos de una parcela anteriores a la fecha actual.
     * Luego, se calcula el agua excedente de los mismos.
     */
    if (!(cropService.equals(originalCrop, modifiedCrop)) && (parcelService.equals(originalParcel, modifiedParcel))) {
      calculateEtForPeriod(originalParcel);
      calculateExcessWaterForPeriod(originalParcel);
    }

    /*
     * Si el cultivo no fue modificado y la parcela si o
     * si el cultivo y la parcela fueron modificados, se
     * solicitan y persisten NUMBER_DAYS registros climaticos
     * de una parcela anteriores a la fecha actual. Luego,
     * se recalcula la ETo y la ETc de los mismos. Por
     * ultimo, se calcula el agua excedente de los mismos.
     */
    if ((cropService.equals(originalCrop, modifiedCrop) && !parcelService.equals(originalParcel, modifiedParcel))
        || (!cropService.equals(originalCrop, modifiedCrop) && !parcelService.equals(originalParcel, modifiedParcel))) {
      /*
       * Solicita y persiste una cantidad NUMBER_DAYS de
       * registros climaticos de una parcela anteriores a
       * la fecha actual, si no existen en la base de datos
       * subyacente
       */
      requestAndPersistClimateRecordsForPeriod(modifiedParcel);
      calculateEtForPeriod(modifiedParcel);
      calculateExcessWaterForPeriod(modifiedParcel);
    }

    /*
     * Si la fecha de siembra fue modificada, pero el cultivo y
     * la parcela no, se solicitan y persisten NUMBER_DAYS
     * registros climaticos de una parcela anteriores a la
     * fecha actual. Luego, se recalcula la ETo y la ETc de
     * los mismos. Por ultimo, se calcula el agua excedente
     * de los mismos.
     */
    if ((UtilDate.compareTo(originalSeedDate, modifiedSeedDate) != 0)
        && (cropService.equals(originalCrop, modifiedCrop)) && (parcelService.equals(originalParcel, modifiedParcel))) {
      requestAndPersistClimateRecordsForPeriod(modifiedParcel);
      calculateEtForPeriod(modifiedParcel);
      calculateExcessWaterForPeriod(modifiedParcel);
    }

    /*
     * Si la fecha de cosecha fue modificada, pero el cultivo y
     * la parcela no, se solicitan y persisten NUMBER_DAYS
     * registros climaticos de una parcela anteriores a la
     * fecha actual. Luego, se recalcula la ETo y la ETc de
     * los mismos. Por ultimo, se calcula el agua excedente
     * de los mismos.
     */
    if ((UtilDate.compareTo(originalHarvestDate, modifiedHarvestDate) != 0)
        && (cropService.equals(originalCrop, modifiedCrop)) && (parcelService.equals(originalParcel, modifiedParcel))) {
      requestAndPersistClimateRecordsForPeriod(modifiedParcel);
      calculateEtForPeriod(modifiedParcel);
      calculateExcessWaterForPeriod(modifiedParcel);
    }

  }

  /**
   * Crea y persiste los registros climaticos de una parcela
   * anteriores a la fecha actual, si NO existen en la base
   * de datos subyacente. La cantidad de registros climaticos
   * anteriores a la fecha actual que se crearan y persistiran
   * esta determinada por el valor de la constante NUMBER_DAYS,
   * la cual se encuentra en la clase ClimateRecordServiceBean.
   * 
   * @param givenParcel
   */
  private void requestAndPersistClimateRecordsForPeriod(Parcel givenParcel) {
    /*
     * El valor de esta variable se utiliza:
     * - para obtener y persistir los registros climaticos de una parcela
     * anteriores a la fecha actual.
     * - para calcular el agua excedente de cada uno de los registros
     * climaticos de una parcela anteriores a la fecha actual.
     * - para recalcular la ETc de cada uno de los registros climaticos
     * de una parcela anteriores a la fecha actual.
     */
    int numberDays = climateRecordService.getNumberDays();

    Calendar currentDate = UtilDate.getCurrentDate();
    Calendar pastDate = Calendar.getInstance();

    ClimateRecord newClimateRecord = null;
    PlantingRecord givenPlantingRecord = null;

    double eto = 0.0;
    double etc = 0.0;
    double kc = 0.0;
    double extraterrestrialSolarRadiation = 0.0;

    /*
     * Crea y persiste una cantidad NUMBER_DAYS de registros
     * climaticos de una parcela anteriores a la fecha actual
     */
    for (int i = 1; i < numberDays + 1; i++) {

      /*
       * De esta manera se obtiene cada una de las fechas
       * anteriores a la fecha actual hasta la fecha
       * resultante de la resta entre el numero de dia de
       * la fecha actual y numberDays
       */
      pastDate.set(Calendar.DAY_OF_YEAR, (currentDate.get(Calendar.DAY_OF_YEAR) - i));

      /*
       * Si en la base de datos subyacente NO existe el registro climatico
       * con la fecha dada perteneciente a una parcela dada, se lo solicita
       * la API climatica y se lo persiste
       */
      if (!climateRecordService.checkExistence(pastDate, givenParcel)) {
        newClimateRecord = ClimateClient.getForecast(givenParcel, pastDate.getTimeInMillis() / 1000);
        eto = calculateEtoForClimateRecord(newClimateRecord);

        /*
         * Si la parcela dada tiene un registro de plantacion en
         * el que la fecha dada esta entre la fecha de siembra y
         * la fecha de cosecha del mismo, se obtiene el kc (coeficiente
         * de cultivo) del cultivo de este registro para poder
         * calcular la ETc (evapotranspiracion del cultivo bajo
         * condiciones estandar) de dicho cultivo.
         * 
         * En otras palabras, lo que hace esta instruccion if es
         * preguntar "¿la parcela dada tuvo o tiene un cultivo
         * sembrado en la fecha dada?". En caso afirmativo se
         * obtiene el kc del cultivo para calcular su ETc, la
         * cual se asignara al nuevo registro climatico.
         */
        if (plantingRecordService.checkExistence(givenParcel, pastDate)) {
          givenPlantingRecord = plantingRecordService.find(givenParcel, pastDate);
          etc = calculateEtcForClimateRecord(eto, givenPlantingRecord);
        }

        /*
         * Asignacion de los valores calculados de la ETo
         * (evapotranspiracion del cultivo de referencia) y
         * la ETc (evapotranspiracion del cultivo bajo condiciones
         * estandar) al nuevo registro climatico
         */
        newClimateRecord.setEto(eto);
        newClimateRecord.setEtc(etc);

        /*
         * Persistencia del nuevo registro climatico
         */
        climateRecordService.create(newClimateRecord);

        /*
         * Luego de calcular la ETc de un nuevo registro climatico,
         * se debe restablecer el valor por defecto de esta variable
         * para evitar el error logico de asignar la ETc de un registro
         * climatico a otro registro climatico
         */
        etc = 0.0;
      } // End if

    } // End for

  }

  /**
   * Calcula la ETo (evapotranspiracion del cultivo de referencia) y
   * la ETc (evapotranspiracion del cultivo bajo condiciones estandar)
   * de los NUMBER_DAYS registros de plantacion de una parcela anteriores
   * a la fecha actual.
   * 
   * El motivo de este metodo es que la temperatura maxima y la temperatura
   * minima de un registro climatico, y los coeficientes de un cultivo
   * (KCs) pueden ser modificados. En caso de ser asi, se debe calcular
   * nuevamente la ETo (evapotranspiracion del cultivo de referencia) y
   * la ETc (evapotranspiracion del cultivo bajo condiciones estandar)
   * de un registro climatico.
   * 
   * @param givenParcel
   */
  private void calculateEtForPeriod(Parcel givenParcel) {
    /*
     * El valor de esta variable se utiliza:
     * - para obtener y persistir los registros climaticos de una parcela
     * anteriores a la fecha actual.
     * - para calcular el agua excedente de cada uno de los registros
     * climaticos de una parcela anteriores a la fecha actual.
     * - para recalcular la ETc de cada uno de los registros climaticos
     * de una parcela anteriores a la fecha actual.
     */
    int numberDays = climateRecordService.getNumberDays();

    Calendar currentDate = UtilDate.getCurrentDate();
    Calendar pastDate = Calendar.getInstance();
    PlantingRecord givenPlantingRecord = null;
    ClimateRecord pastClimateRecord = null;

    double eto = 0.0;
    double etc = 0.0;
    double kc = 0.0;
    double extraterrestrialSolarRadiation = 0.0;

    /*
     * Calcula la ETo (evapotranspiracion del cultivo de referencia) y la
     * ETc (evapotranspiracion del cultivo bajo condiciones estandar)
     * de NUMBER_DAYS registros climaticos de una parcela anteriores
     * a la fecha actual
     */
    for (int i = 1; i < numberDays + 1; i++) {

      /*
       * De esta manera se obtiene cada una de las fechas
       * anteriores a la fecha actual hasta la fecha
       * resultante de la resta entre el numero de dia de
       * la fecha actual y numberDays
       */
      pastDate.set(Calendar.DAY_OF_YEAR, (currentDate.get(Calendar.DAY_OF_YEAR) - i));

      /*
       * Si existe el registro climatico de una fecha pasada perteneciente
       * a una parcela dada, se calcula su ETo y su ETc
       */
      if (climateRecordService.checkExistence(pastDate, givenParcel)) {
        pastClimateRecord = climateRecordService.find(pastDate, givenParcel);
        eto = calculateEtoForClimateRecord(pastClimateRecord);

        /*
         * Si la parcela dada tiene un registro de plantacion en
         * el que la fecha dada esta entre la fecha de siembra y
         * la fecha de cosecha del mismo, se obtiene el kc (coeficiente
         * de cultivo) del cultivo de este registro para poder
         * calcular la ETc (evapotranspiracion del cultivo bajo
         * condiciones estandar) de dicho cultivo.
         * 
         * En otras palabras, lo que hace esta instruccion if es
         * preguntar "¿la parcela dada tuvo o tiene un cultivo
         * sembrado en la fecha dada?". En caso afirmativo se
         * obtiene el kc del cultivo para calcular su ETc, la
         * cual se asignara a un registro climatico.
         */
        if (plantingRecordService.checkExistence(givenParcel, pastDate)) {
          givenPlantingRecord = plantingRecordService.find(givenParcel, pastDate);
          etc = calculateEtcForClimateRecord(eto, givenPlantingRecord);
        }

        /*
         * Actualizacion de la ETo y la ETc del registro climatico
         * de una fecha anterior a la fecha actual
         */
        climateRecordService.updateEtoAndEtc(pastDate, givenParcel, eto, etc);

        /*
         * Luego de calcular la ETc de un registro climatico, se debe
         * restablecer el valor por defecto de esta variable para evitar
         * el error logico de asignar la ETc de un registro climatico a
         * otro registro climatico
         */
        etc = 0.0;
      } // End if

    } // End for

  }

  /**
   * Calcula el agua excedente de los registros climaticos de
   * una parcela anteriores a la fecha actual. Las fechas de
   * estos registros climaticos estan comprendidas en el
   * conjunto de fechas que van desde el dia inmediatamente
   * anterior a la fecha actual hasta una cantidad de dias
   * hacia atras. Esta cantidad de dias esta determinada por
   * el valor de la constante NUMBER_DAYS.
   * 
   * @param givenParcel
   */
  private void calculateExcessWaterForPeriod(Parcel givenParcel) {
    /*
     * El valor de esta variable se utiliza:
     * - para obtener y persistir los registros climaticos de una parcela
     * anteriores a la fecha actual.
     * - para calcular el agua excedente de cada uno de los registros
     * climaticos de una parcela anteriores a la fecha actual.
     * - para recalcular la ETc de cada uno de los registros climaticos
     * de una parcela anteriores a la fecha actual.
     */
    int numberDays = climateRecordService.getNumberDays();

    /*
     * El metodo getInstance de la clase Calendar retorna
     * la referencia a un objeto de tipo Calendar que
     * contiene la fecha actual
     */
    Calendar currentDate = Calendar.getInstance();
    Calendar givenDate = Calendar.getInstance();

    /*
     * Esta variable se utiliza para obtener el dia inmeditamente
     * anterior a una fecha dada en la instruccion for de mas
     * abajo. Esto es necesario para obtener el agua excedente
     * de una parcela en el dia inmediatamente anterior a una
     * fecha dada.
     */
    Calendar yesterdayDate = Calendar.getInstance();

    ClimateRecord givenClimateRecord = null;

    double excessWaterGivenDate = 0.0;
    double excessWaterYesterday = 0.0;
    double totalIrrigationWaterGivenDate = 0.0;
    double givenEt = 0.0;

    /*
     * El agua excedente de los registros climaticos de una
     * parcela anteriores a la fecha actual, se debe calcular
     * desde atras hacia adelante, ya que el agua excedente de
     * un dia es agua a favor para el dia inmediatamente siguiente.
     * 
     * Por lo tanto, se debe comenzar a calcular el agua excedente
     * de los registros climticos de una parcela anteriores a la
     * fecha actual desde el registro climatico mas antiguo de
     * ellos hasta el mas actual de ellos. Estos registros
     * climaticos son obtenidos y persistidos por el metodo
     * requestAndPersistClimateRecordsForPeriod si no existen
     * en la base de datos subyacente.
     */
    givenDate.set(Calendar.DAY_OF_YEAR, (currentDate.get(Calendar.DAY_OF_YEAR) - numberDays));

    /*
     * Calcula el agua excedente de cada uno de los registros
     * climaticos de una parcela anteriores a la fecha actual
     * desde el mas antiguo de ellos hasta el mas actual de
     * ellos. Estos registros climaticos son obtenidos y persistidos
     * por el metodo requestAndPersistClimateRecordsForPeriod
     * si no existen en la base de datos subyacente.
     */
    for (int i = 1; i < numberDays + 1; i++) {
      yesterdayDate.set(Calendar.DAY_OF_YEAR, (givenDate.get(Calendar.DAY_OF_YEAR) - 1));

      /*
       * Si el registro climatico del dia inmediatamente anterior
       * a una fecha dada existe, se obtiene su agua excedente
       * para calcular el agua excedente del registro climatico
       * de una fecha dada. En caso contrario, se asume que el
       * agua excedente del dia inmediatamente anterior a una
       * fecha dada es 0.0.
       */
      if (climateRecordService.checkExistence(yesterdayDate, givenParcel)) {
        excessWaterYesterday = climateRecordService.find(yesterdayDate, givenParcel).getExcessWater();
      } else {
        excessWaterYesterday = 0.0; 
      }

      /*
       * Obtiene uno de los registros climaticos de una parcela dada
       * anteriores a la fecha actual, los cuales son obtenidos y
       * persistidos por el metodo requestAndPersistClimateRecordsForPeriod
       * si NO existen en la base de datos subyacente
       */
      givenClimateRecord = climateRecordService.find(givenDate, givenParcel);
      totalIrrigationWaterGivenDate = irrigationRecordService.calculateTotalIrrigationWaterGivenDate(givenDate, givenParcel);

      /*
       * Cuando una parcela NO tiene un cultivo sembrado y en
       * desarrollo, la ETc de uno o varios de sus registros
       * climaticos tiene el valor 0.0, ya que si no hay un
       * cultivo en desarrollo NO es posible calcular la ETc
       * (evapotranspiracion del cultivo bajo condiciones
       * estandar) del mismo. Por lo tanto, se debe utilizar la
       * ETo (evapotranspiracion del cultivo de referencia) para
       * calcular el agua excedente de un registro climatico
       * en una fecha dada.
       * 
       * En caso contrario, se debe utilizar la ETc para calcular
       * el agua excedente de un registro climatico en una fecha
       * dada.
       */
      if (givenClimateRecord.getEtc() == 0.0) {
        givenEt = givenClimateRecord.getEto();
      } else {
        givenEt = givenClimateRecord.getEtc();
      }

      /*
       * Calculo del agua excedente de una parcela dada
       * en una fecha dada
       */
      excessWaterGivenDate = WaterMath.calculateExcessWater(givenEt, givenClimateRecord.getPrecip(), totalIrrigationWaterGivenDate, excessWaterYesterday);

      /*
       * Actualizacion del agua excedente del registro
       * climatico de una fecha dada
       */
      climateRecordService.updateExcessWater(givenDate, givenParcel, excessWaterGivenDate);

      /*
       * El agua excedente de los registros climaticos de una
       * parcela anteriores a la fecha actual se calcula desde
       * atras hacia adelante, ya que el agua excedente de un
       * dia es agua a favor para el dia inmediatamente siguiente.
       * Por lo tanto, se comienza a calcular el agua excedente
       * de estos registros climaticos desde el registro climatico
       * mas antiguo de ellos. En consecuencia, para calcular el
       * agua excedente del siguiente registro climatico se debe
       * calcular la fecha siguiente.
       */
      givenDate.set(Calendar.DAY_OF_YEAR, ((currentDate.get(Calendar.DAY_OF_YEAR) - numberDays) + i));
    } // End for

  }

}
