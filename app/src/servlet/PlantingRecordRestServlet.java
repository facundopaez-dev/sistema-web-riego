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
     * Si un registro de plantacion nuevo tiene el estado "En
     * desarrollo" o el estado "En espera", se debe poder modificar,
     * por lo tanto, se asigna el valor true a su atributo modifiable
     */
    if ((statusService.equals(givenStatus, developmentStatus)) || (statusService.equals(givenStatus, waitingStatus))) {
      newPlantingRecord.setModifiable(true);
    }

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
       * **********************************************
       * Calculo de la necesidad de agua de riego de un
       * cultivo en desarrollo en la fecha actual
       * **********************************************
       */
      newPlantingRecord.setIrrigationWaterNeed(String.valueOf(calculateIrrigationWaterNeed(userId, newPlantingRecord)));
    }

    /*
     * Si el valor del encabezado de autorizacion de la peticion HTTP
     * dada, tiene un JWT valido y no se cumplen las condiciones de
     * los controles para la creacion de un registro de plantacion, la
     * aplicacion del lado servidor devuelve el mensaje HTTP 200 (Ok)
     * junto con los datos que el cliente solicito persistir
     */
    return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(plantingRecordService.create(newPlantingRecord))).build();
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
       * Se calcula la necesidad de agua de riego del registro de
       * plantacion (modificado) en base a la parcela y al cultivo
       * modificados. Esto es que se calcula la necesidad de agua
       * de riego del cultivo modificado perteneciente a la parcela
       * modificada.
       */
      modifiedPlantingRecord.setIrrigationWaterNeed(String.valueOf(calculateIrrigationWaterNeed(userId, modifiedPlantingRecord)));
    }

    /*
     * Si el valor del encabezado de autorizacion de la peticion HTTP
     * dada, tiene un JWT valido, la aplicacion del lado servidor
     * devuelve el mensaje HTTP 200 (Ok) junto con los datos que el
     * cliente solicito actualizar
     */
    return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(plantingRecordService.modify(userId, plantingRecordId, modifiedPlantingRecord))).build();
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

    double extraterrestrialSolarRadiation = 0.0;
    double etoCurrentDate = 0.0;
    double etcCurrentDate = 0.0;
    double totalIrrigationWaterCurrentDate = 0.0;
    double irrigationWaterNeedCurrentDate = 0.0;
    double excessWaterYesterday = 0.0;

    /*
     * Si el registro climatico del dia inmediatamente anterior
     * a la fecha actual de una parcela, existe, se obtiene el
     * agua excedente de dicho dia. En caso contrario, se asume
     * que el agua excedente del dia inmediatamente anterior a
     * la fecha actual es 0.
     */
    if (climateRecordService.checkExistence(UtilDate.getYesterdayDate(), givenParcel)) {
      excessWaterYesterday = climateRecordService.find(UtilDate.getYesterdayDate(), givenParcel).getExcessWater();
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

      /*
       * Calculo de la evapotranspiracion del cultivo
       * bajo condiciones estandar (ETc) de la fecha
       * actual
       */
      etcCurrentDate = Etc.calculateEtc(etoCurrentDate, cropService.getKc(givenPlantingRecord.getCrop(), givenPlantingRecord.getSeedDate()));

      totalIrrigationWaterCurrentDate = irrigationRecordService.calculateTotalIrrigationWaterCurrentDate(givenParcel);

      /*
       * Calculo de la necesidad de agua de riego [mm/dia]
       * de un cultivo en la fecha actual
       */
      irrigationWaterNeedCurrentDate = WaterMath.calculateIrrigationWaterNeed(etcCurrentDate,
          currentClimateRecord.getPrecip(), totalIrrigationWaterCurrentDate, excessWaterYesterday);

      /*
       * Actualiza los atributos eto y etc del registro
       * climatico actual ya obtenido y persistido
       */
      currentClimateRecord.setEto(etoCurrentDate);
      currentClimateRecord.setEtc(etcCurrentDate);
      climateRecordService.modify(userId, currentClimateRecord.getId(), currentClimateRecord);
    }

    return irrigationWaterNeedCurrentDate;
  }

}
