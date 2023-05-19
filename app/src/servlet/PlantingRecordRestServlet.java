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
          String.valueOf(calculateIrrigationWaterNeed(userId, newPlantingRecord)));
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
       * Se calcula la necesidad de agua de riego del registro de
       * plantacion (modificado) en base a la parcela y al cultivo
       * modificados. Esto es que se calcula la necesidad de agua
       * de riego del cultivo modificado perteneciente a la parcela
       * modificada.
       */
      plantingRecordService.updateIrrigationWaterNeed(plantingRecordId, modifiedPlantingRecord.getParcel(),
          String.valueOf(calculateIrrigationWaterNeed(userId, modifiedPlantingRecord)));
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
  public Response calculateIrrigationWaterNeed(@Context HttpHeaders request, @PathParam("id") int plantingRecordId) throws IOException {
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
    double irrigationWaterNeedCurrentDate = calculateIrrigationWaterNeed(userId, givenPlantingRecord);

    /*
     * Se actualiza el atributo irrigationWaterNeed del registro
     * de plantacion en desarrollo sobre el que se solicita calcular
     * la necesidad de agua de riego del cultivo que contiene
     */
    plantingRecordService.updateIrrigationWaterNeed(plantingRecordId, givenParcel, String.valueOf(irrigationWaterNeedCurrentDate));

    IrrigationRecord newIrrigationRecord = new IrrigationRecord();

    /*
     * El metodo getInstance de la clase Calendar retorna la
     * referencia a un objeto de tipo Calendar que contiene la
     * fecha actual
     */
    newIrrigationRecord.setDate(Calendar.getInstance());
    newIrrigationRecord.setIrrigationWaterNeed(String.valueOf(irrigationWaterNeedCurrentDate));
    newIrrigationRecord.setParcel(givenParcel);
    newIrrigationRecord.setCrop(givenPlantingRecord.getCrop());

    /*
     * Si el valor del encabezado de autorizacion de la peticion HTTP
     * dada, tiene un JWT valido, la aplicacion del lado servidor
     * devuelve el mensaje HTTP 200 (Ok) junto con los datos
     * pertinentes
     */
    return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(newIrrigationRecord)).build();
  }

  /**
   * Calcula la necesidad de agua de riego de un cultivo en
   * desarrollo en la fecha actual
   * 
   * @param userId
   * @param givenPlantingRecord
   * @return punto flotante que representa la necesidad de agua
   * de riego de un cultivo en desarrollo en la fecha actual
   */
  private double calculateIrrigationWaterNeed(int userId, PlantingRecord givenPlantingRecord) {
    Parcel givenParcel = givenPlantingRecord.getParcel();
    ClimateRecord currentClimateRecord = null;

    /*
     * El metodo getInstance de la clase Calendar retorna
     * la referencia a un objeto de tipo Calendar que
     * contiene la fecha actual
     */
    Calendar currentDate = Calendar.getInstance();
    Calendar yesterdayDate = UtilDate.getYesterdayDate();

    double extraterrestrialSolarRadiation = 0.0;
    double etoCurrentDate = 0.0;
    double etcCurrentDate = 0.0;
    double totalIrrigationWaterCurrentDate = 0.0;
    double irrigationWaterNeedCurrentDate = 0.0;
    double excessWaterCurrrentDate = 0.0;
    double excessWaterYesterday = 0.0;

    /*
     * Si el registro climatico del dia inmediatamente anterior
     * a la fecha actual de una parcela, existe en la base de
     * datos subyacente, se obtiene el agua excedente de dicho
     * dia. En caso contrario, si el registro climatico del dia
     * anterior al dia inmediatamente anterior a la fecha actual
     * (esto es, el dia antes de ayer) existe en la base de datos
     * subyacente, se obtiene el agua excedente de dicho dia.
     * 
     * Si en la base de datos subyacente NO existen ninguno de
     * estos dos registros climaticos para la parcela dada, se
     * asume que el agua excedente del dia inmediatamente anterior
     * a la fecha actual es 0.
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

    /*
     * Si en la base de datos existe el registro climatico de
     * la fecha actual para la parcela dada, se utiliza su ETc
     * (evapotranspiracion del cultivo bajo condiciones estandar)
     * para calcular la necesidad de agua de riego de un cultivo
     * en la fecha actual.
     * 
     * El metodo automatico getCurrentWeatherDataset de la clase
     * ClimateRecordManager se ocupa de obtener y persistir los datos
     * meteorologicos de cada dia para todas las parcelas activas
     * de la base de datos subyacente. Ademas, de esto calcula
     * la ETo (evapotranspiracion del cultivo de referencia) y
     * la ETc (evapotranspiracion del cultivo bajo condiciones
     * estandar) de cada registro climatico que persiste.
     * 
     * El metodo getCurrentWeatherDataset calcula la ETc si la
     * parcela para la que obtiene y persiste datos meteorologicos,
     * tiene un cultivo sembrado y en desarrollo.
     */
    if (climateRecordService.checkExistence(currentDate, givenParcel)) {
      currentClimateRecord = climateRecordService.find(currentDate, givenParcel);

      /*
       * Calculo de la evapotranspiracion del cultivo bajo
       * condiciones estandar (ETc) de la fecha actual.
       * 
       * Es necesario calcular la ETc del cultivo de un registro
       * de plantacion en desarrollo, ya que dicho cultivo puede
       * ser modificado por otro cultivo, con lo cual cambia la
       * ETc, y al cambiar la ETc cambia la necesidad de agua de
       * riego de un cultivo, y, por ende, cambia el valor del
       * atributo de la necesidad de agua de riego de un registro
       * de plantacion en desarrollo.
       */
      etcCurrentDate = Etc.calculateEtc(currentClimateRecord.getEto(),
          cropService.getKc(givenPlantingRecord.getCrop(), givenPlantingRecord.getSeedDate()));
      currentClimateRecord.setEtc(etcCurrentDate);

      totalIrrigationWaterCurrentDate = irrigationRecordService.calculateTotalIrrigationWaterCurrentDate(givenParcel);

      /*
       * Calculo del agua excedente de una parcela en la fecha
       * actual
       */
      excessWaterCurrrentDate = WaterMath.calculateExcessWater(currentClimateRecord.getEtc(), currentClimateRecord.getPrecip(),
          totalIrrigationWaterCurrentDate, excessWaterYesterday);
      currentClimateRecord.setExcessWater(excessWaterCurrrentDate);

      /*
       * Calculo de la necesidad de agua de riego de
       * un cultivo en la fecha actual
       */
      irrigationWaterNeedCurrentDate = WaterMath.calculateIrrigationWaterNeed(currentClimateRecord.getEtc(),
          currentClimateRecord.getPrecip(), totalIrrigationWaterCurrentDate, excessWaterYesterday);

      /*
       * Actualiza la ETc del registro climatico de la fecha
       * actual, ya que si el cultivo de un registro de plantacion
       * en desarrollo es modificado por otro cultivo, cambia
       * la ETc, y, por ende, se debe actualizar la ETc del
       * registro climatico actual
       */
      climateRecordService.modify(userId, currentClimateRecord.getId(), currentClimateRecord);
    }

    /*
     * Si en la base de datos subyacente NO existe el registro
     * climatico de la fecha actual para la parcela dada, se lo
     * solicita al servicio metereologico utilizado y se lo
     * persiste.
     * 
     * Se hace esto porque lo que se busca con este metodo es
     * calcular la necesidad de agua de riego de un cultivo en
     * desarrollo en la fecha actual. Para esto se debe calcular
     * la ETc (evapotranspiracion del cultivo bajo condiciones
     * estandar) de la fecha actual, con lo cual, se la debe
     * calcular con datos meteorologicos de la fecha actual.
     */
    if (!climateRecordService.checkExistence(currentDate, givenParcel)) {
      currentClimateRecord = climateRecordService.persistCurrentClimateRecord(givenParcel);

      /*
       * Calculo de la evapotranspiracion del cultivo
       * de referencia (ETo) de la fecha actual
       */
      extraterrestrialSolarRadiation = solarService.getRadiation(currentDate.get(Calendar.MONTH), givenParcel.getLatitude());
      etoCurrentDate = HargreavesEto.calculateEto(currentClimateRecord.getMaximumTemperature(),
          currentClimateRecord.getMinimumTemperature(), extraterrestrialSolarRadiation);
      currentClimateRecord.setEto(etoCurrentDate);

      /*
       * Calculo de la evapotranspiracion del cultivo
       * bajo condiciones estandar (ETc) de la fecha
       * actual
       */
      etcCurrentDate = Etc.calculateEtc(etoCurrentDate, cropService.getKc(givenPlantingRecord.getCrop(), givenPlantingRecord.getSeedDate()));
      currentClimateRecord.setEtc(etcCurrentDate);

      totalIrrigationWaterCurrentDate = irrigationRecordService.calculateTotalIrrigationWaterCurrentDate(givenParcel);

      /*
       * Calculo del agua excedente de una parcela en la fecha
       * actual
       */
      excessWaterCurrrentDate = WaterMath.calculateExcessWater(currentClimateRecord.getEtc(), currentClimateRecord.getPrecip(),
          totalIrrigationWaterCurrentDate, excessWaterYesterday);
      currentClimateRecord.setExcessWater(excessWaterCurrrentDate);

      /*
       * Calculo de la necesidad de agua de riego [mm/dia]
       * de un cultivo en la fecha actual
       */
      irrigationWaterNeedCurrentDate = WaterMath.calculateIrrigationWaterNeed(etcCurrentDate,
          currentClimateRecord.getPrecip(), totalIrrigationWaterCurrentDate, excessWaterYesterday);

      /*
       * Actualiza la ETo (evapotranspiracion del cultivo de
       * referencia), la ETc (evapotranspiracion del cultivo
       * bajo condiciones estandar) y el agua excedente del
       * registro climatico de la fecha actual ya obtenido
       * y persistido al comienzo de esta instruccion if
       */
      climateRecordService.modify(userId, currentClimateRecord.getId(), currentClimateRecord);
    }

    return irrigationWaterNeedCurrentDate;
  }

  /*
   * El valor de esta constante se utiliza para obtener y
   * persistir los registros climaticos de una parcela anteriores
   * a la fecha actual. Tambien se lo utiliza para calcular el
   * agua excedente de cada uno de estos registros climaticos.
   */
  private final int NUMBER_DAYS = 7;

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
      recalculateEtClimateRecordsForPeriod(originalParcel);
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
      recalculateEtClimateRecordsForPeriod(modifiedParcel);
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
      recalculateEtClimateRecordsForPeriod(modifiedParcel);
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
      recalculateEtClimateRecordsForPeriod(modifiedParcel);
      calculateExcessWaterForPeriod(modifiedParcel);
    }

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
    givenDate.set(Calendar.DAY_OF_YEAR, (currentDate.get(Calendar.DAY_OF_YEAR) - NUMBER_DAYS));

    /*
     * Calcula el agua excedente de cada uno de los registros
     * climaticos de una parcela anteriores a la fecha actual
     * desde el mas antiguo de ellos hasta el mas actual de
     * ellos. Estos registros climaticos son obtenidos y persistidos
     * por el metodo requestAndPersistClimateRecordsForPeriod
     * si no existen en la base de datos subyacente.
     */
    for (int i = 1; i < NUMBER_DAYS + 1; i++) {
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
      givenDate.set(Calendar.DAY_OF_YEAR, ((currentDate.get(Calendar.DAY_OF_YEAR) - NUMBER_DAYS) + i));
    } // End for

  }

  /**
   * Crea y persiste los registros climaticos de una
   * parcela anteriores a la fecha actual, si no estan
   * en la base de datos subyacente. La cantidad de
   * registros climaticos anteriores a la fecha actual
   * que se crearan y persistiran esta determinada por
   * el valor de la constante NUMBER_DAYS.
   * 
   * @param givenParcel
   */
  private void requestAndPersistClimateRecordsForPeriod(Parcel givenParcel) {
    /*
     * El metodo getInstance de la clase Calendar retorna
     * la referencia a un objeto de tipo Calendar que
     * contiene la fecha actual
     */
    Calendar currentDate = Calendar.getInstance();
    Calendar givenDate = Calendar.getInstance();

    ClimateRecord newClimateRecord = null;
    PlantingRecord givenPlantingRecord = null;

    double eto = 0.0;
    double etc = 0.0;
    double kc = 0.0;
    double extraterrestrialSolarRadiation = 0.0;

    /*
     * Crea y persiste una cantidad NUMBER_DAYS de registros
     * climaticos de una parcela con fechas anteriores a la
     * fecha actual
     */
    for (int i = 1; i < NUMBER_DAYS + 1; i++) {

      /*
       * De esta manera se obtiene cada una de las fechas
       * anteriores a la fecha actual
       */
      givenDate.set(Calendar.DAY_OF_YEAR, (currentDate.get(Calendar.DAY_OF_YEAR) - i));

      /*
       * Si en la base de datos subyacente NO existe el registro
       * climatico con la fecha dada para una parcela dada, se
       * lo solicita la API climatica y se lo persiste
       */
      if (!climateRecordService.checkExistence(givenDate, givenParcel)) {
        newClimateRecord = ClimateClient.getForecast(givenParcel, givenDate.getTimeInMillis() / 1000);

        /*
         * Calculo de la evapotranspiracion del cultivo
         * de referencia (ETo) en la fecha dada
         */
        extraterrestrialSolarRadiation = solarService.getRadiation(givenDate.get(Calendar.MONTH), givenParcel.getLatitude());
        eto = HargreavesEto.calculateEto(newClimateRecord.getMaximumTemperature(), newClimateRecord.getMinimumTemperature(), extraterrestrialSolarRadiation);

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
        if (plantingRecordService.checkExistence(givenParcel, givenDate)) {
          givenPlantingRecord = plantingRecordService.find(givenParcel, givenDate);

          /*
           * Para obtener el kc (coeficiente de cultivo) que tuvo
           * el cultivo en la fecha dada, se debe utilizar la fecha
           * dada como fecha hasta en la invocacion del metodo
           * getKc de la clase CropServiceBean
           */
          kc = cropService.getKc(givenPlantingRecord.getCrop(), givenPlantingRecord.getSeedDate(), givenDate);
          etc = Etc.calculateEtc(eto, kc);
        } else {
          /*
           * Si la parcela dada NO tiene un registro de plantacion
           * en el que la fecha dada esta entre la fecha de siembra
           * y la fecha de cosecha del mismo, la ETc (evapotranspiracion
           * del cultivo bajo condiciones estandar) es 0.0, ya que
           * la inexistencia de un registro de plantacion representa
           * la inexistencia de un cultivo sembrado en una parcela
           */
          etc = 0.0;
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
      } // End if

    } // End for

  }

  /**
   * Recalcula la ETo (evapotranspiracion del cultivo de referencia)
   * y la ETc (evapotranspiracion del cultivo bajo condiciones estandar)
   * de los NUMBER_DAYS registros de plantacion anteriores a la fecha
   * actual. Es necesario hacer esto cuando se modifica un registro
   * climatico y/o el cultivo de un registro de plantacion. Este es
   * el motivo de este metodo.
   * 
   * @param givenParcel
   */
  private void recalculateEtClimateRecordsForPeriod(Parcel givenParcel) {
    /*
     * El metodo getInstance de la clase Calendar retorna
     * la referencia a un objeto de tipo Calendar que
     * contiene la fecha actual
     */
    Calendar currentDate = Calendar.getInstance();
    Calendar givenDate = Calendar.getInstance();

    double eto = 0.0;
    double etc = 0.0;
    double kc = 0.0;
    double extraterrestrialSolarRadiation = 0.0;

    PlantingRecord givenPlantingRecord = null;
    PlantingRecordStatus givenPlantingRecordStatus = null;
    PlantingRecordStatus developmentStatus = statusService.findDevelopmentStatus();
    ClimateRecord givenClimateRecord = null;

    /*
     * Recalcula la ETo (evapotranspiracion del cultivo de referencia)
     * y la ETc (evapotranspiracion del cultivo bajo condiciones estandar)
     * de los NUMBER_DAYS registros climaticos de una parcela anteriores
     * a la fecha actual
     */
    for (int i = 1; i < NUMBER_DAYS + 1; i++) {

      /*
       * De esta manera se obtiene cada una de las fechas
       * anteriores a la fecha actual
       */
      givenDate.set(Calendar.DAY_OF_YEAR, (currentDate.get(Calendar.DAY_OF_YEAR) - i));

      if (plantingRecordService.checkExistence(givenParcel, givenDate)) {
        givenPlantingRecord = plantingRecordService.find(givenParcel, givenDate);
        givenPlantingRecordStatus = givenPlantingRecord.getStatus();

        if (statusService.equals(givenPlantingRecordStatus, developmentStatus)) {

          /*
           * Si existe el registro climatico de una fecha dada de
           * una parcela dada, y la parcela tiene un registro de
           * plantacion en desarrollo en el que la fecha dada
           * esta entre la fecha de siembra y la fecha de cosecha
           * del mismo, se recalcula la ETo y la ETc del registro
           * climatico
           */
          if (climateRecordService.checkExistence(givenDate, givenParcel)) {
            givenClimateRecord = climateRecordService.find(givenDate, givenParcel);

            /*
             * Calculo de la evapotranspiracion del cultivo
             * de referencia (ETo) en la fecha dada
             */
            extraterrestrialSolarRadiation = solarService.getRadiation(givenDate.get(Calendar.MONTH), givenParcel.getLatitude());
            eto = HargreavesEto.calculateEto(givenClimateRecord.getMaximumTemperature(), givenClimateRecord.getMinimumTemperature(), extraterrestrialSolarRadiation);

            /*
             * Calculo de la evapotranspiracion del cultivo
             * bajo condiciones estandar (ETc) en la fecha
             * dada
             */
            kc = cropService.getKc(givenPlantingRecord.getCrop(), givenPlantingRecord.getSeedDate(), givenDate);
            etc = Etc.calculateEtc(eto, kc);

            givenClimateRecord.setEto(eto);
            givenClimateRecord.setEtc(etc);
    
            /*
             * Se actualiza el registro climatico de una fecha
             * dada de una parcela con los nuevos valores de la
             * ETo y ETc
             */
            climateRecordService.modify(givenClimateRecord.getId(), givenClimateRecord);
          } // End if

        } // End if

      } // End if

    } // End for

  }

}
