package servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Calendar;
import java.util.Collection;
import javax.ejb.EJB;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
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
   * El valor de esta constante se asigna al atributo
   * irrigationWaterNeed de un registro de plantacion
   * nuevo para representar que un registro de plantacion
   * recien creado no tiene calculada la necesidad de
   * agua de riego del cultivo que contiene.
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
     * Si la parcela para la que se quiere crear un registro
     * de plantacion, tiene un registro de plantacion en
     * desarrollo, la aplicacion del lado servidor retorna el
     * mensaje HTTP 400 (Bad request) junto con el mensaje
     * "No esta permitido crear un registro de plantacion
     * para una parcela que tiene un registro de plantacion
     * en desarrollo" y no se realiza la operacion solicitada
     */
    if (plantingRecordService.checkOneInDevelopment(newPlantingRecord.getParcel())) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.CREATION_NOT_ALLOWED_IN_DEVELOPMENT)))
          .build();
    }

    /*
     * Se establece la fecha actual como la fecha de siembra
     * del nuevo registro de plantacion. El motivo de este
     * cambio es que no tiene sentido permitir la creacion
     * de un registro de plantacion del pasado ni del
     * futuro, ya que la aplicacion no tiene los datos
     * meteorologicos del pasado ni del futuro de la
     * ubicacion geografica de una parcela.
     * 
     * Los datos meteorologicos son necesarios para calcular
     * la evapotranspiracion del cultivo de referencia (ETo)
     * y la evapotranspiracion del cultivo (ETc), la cual, es
     * necesaria para determinar la cantidad de agua de riego
     * que necesita un cultivo plantado en una parcela.
     */
    newPlantingRecord.setSeedDate(Calendar.getInstance());

    /*
     * Se calcula la fecha de cosecha del cultivo del nuevo
     * registro de plantacion en funcion de la fecha de siembra
     * y el ciclo de vida del cultivo
     */
    Calendar harvestDate = cropService.calculateHarvestDate(newPlantingRecord.getSeedDate(), newPlantingRecord.getCrop());
    newPlantingRecord.setHarvestDate(harvestDate);

    /*
     * Se establece el estado del nuevo registro de plantacion
     * en base a la fecha de cosecha de su cultivo
     */
    newPlantingRecord.setStatus(statusService.calculateStatus(newPlantingRecord.getHarvestDate()));

    /*
     * Un registro de plantacion nuevo es un registro de
     * plantacion modificable, ya que tiene el estado
     * "En desarrollo" debido a que, por ser nuevo, su
     * fecha de cosecha es mayor o igual a la fecha actual
     */
    newPlantingRecord.setModifiable(true);

    /*
     * Un registro de plantacion nuevo no tiene calculada
     * la necesidad de agua de riego del cultivo que contiene,
     * por lo tanto, se asigna el valor "n/a" (no disponible)
     * a su atributo irrigationWaterNeed
     */
    newPlantingRecord.setIrrigationWaterNeed(NOT_AVAILABLE);

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
     * Si el registro de plantacion correspondiente al ID dado,
     * esta finalizado, la aplicacion del lado servidor retorna
     * el mensaje HTTP 400 (Bad request) junto con el mensaje
     * "No esta permitida la modificacion de un registro de
     * plantacion finalizado" y no se realiza la operacion
     * solicitada.
     * 
     * El metodo automatico unsetModifiable de la clase
     * PlantingRecordManager se ocupa de asignar el valor
     * false al atributo modifiable de un registro de
     * plantacion finalizado.
     */
    if (plantingRecordService.isFinished(plantingRecordId)) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.MODIFICATION_NON_MODIFIABLE_PLANTING_RECORD_NOT_ALLOWED))).build();
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
    Calendar currentSeedDate = currentPlantingRecord.getSeedDate();

    /*
     * Si la fecha de siembra del registro de plantacion a modificar
     * esta definida y es distinta a la fecha de siembra actual de
     * dicho registro, se realizan las siguientes operaciones:
     * 
     * - se comprueba si la fecha de siembra modificada es menor
     * o igual a la fecha de cosecha del ultimo registro de
     * plantacion finalizado de la parcela elegida
     * 
     * - se comprueba si la fecha de siembra modificada es menor
     * o mayor a la fecha actual
     */
    if ((modifiedPlantingRecord.getSeedDate() != null) && (UtilDate.compareTo(modifiedPlantingRecord.getSeedDate(), currentSeedDate) != 0)) {
      /*
       * Si la parcela correspondiente al registro de plantacion
       * a modificar, tiene un ultimo registro de plantacion finalizado,
       * se comprueba si hay superposicion entre la fecha de siembra
       * del registro de plantacion a modificar y la fecha de cosecha
       * del ultimo registro de plantacion finalizado de la parcela
       * elegida
       */
      if (plantingRecordService.hasLastFinished(modifiedPlantingRecord.getParcel())) {
        PlantingRecord lastFinishedPlantingRecord = plantingRecordService.findLastFinished(modifiedPlantingRecord.getParcel());

        /*
         * Si la fecha de siembra del registro de plantacion a
         * modificar es menor o igual a la fecha de cosecha del
         * ultimo registro de plantacion finalizado de la parcela
         * elegida, la aplicacion retorna el mensaje HTTP 400
         * (Bad request) junto con el mensaje "No esta permitido
         * modificar un registro de plantacion con una fecha de
         * siembra menor o igual a la fecha de cosecha del
         * ultimo registro de plantacion finalizado de la
         * parcela elegida" y no se realiza la operacion
         * solicitada
         */
        if (UtilDate.compareTo(modifiedPlantingRecord.getSeedDate(), lastFinishedPlantingRecord.getHarvestDate()) <= 0) {
          return Response.status(Response.Status.BAD_REQUEST)
              .entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.OVERLAP_BETWEEN_SEED_DATE_AND_HARVEST_DATE_WITH_LAST_FINISHED_PLANTING_RECORD)))
              .build();
        }

      } // End if

      Calendar currentDate = Calendar.getInstance();

      /*
       * Si la fecha de siembra del registro de plantacion a modificar
       * es menor (anterior) a la fecha actual, la aplicacion del lado
       * servidor retorna el mensaje HTTP 400 (Bad request) junto con
       * el mensaje "No esta permitido modificar un registro de plantacion
       * con una fecha de siembra menor a la fecha actual (es decir,
       * anterior a la fecha actual)" y no se realiza la operacion
       * solicitada
       */
      if (UtilDate.compareTo(modifiedPlantingRecord.getSeedDate(), currentDate) < 0) {
        return Response.status(Response.Status.BAD_REQUEST)
            .entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.MODIFICATION_WITH_PAST_SEED_DATE_NOT_ALLOWED)))
            .build();
      }

      /*
       * Si la fecha de siembra del registro de plantacion a
       * modificar es mayor (posterior) a la fecha actual, la
       * aplicacion del lado servidor retorna el mensaje HTTP
       * 400 (Bad request) junto con el mensaje "No esta permitido
       * modificar un registro de plantacion con una fecha de
       * siembra mayor a la fecha actual (es decir, posterior
       * a la fecha actual)" y no se realiza la operacion
       * solicitada
       */
      if (UtilDate.compareTo(modifiedPlantingRecord.getSeedDate(), currentDate) > 0) {
        return Response.status(Response.Status.BAD_REQUEST)
            .entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.MODIFICATION_WITH_FUTURE_SEED_DATE_NOT_ALLOWED)))
            .build();
      }

    } // End if

    /*
     * Si la fecha de siembra del registro de plantacion a
     * modificar NO esta definida, se le asigna la fecha
     * de siembra que tiene actualmente en la base de
     * datos subyacente. En otras palabras, si la fecha
     * de siembra NO esta definida en la modificacion
     * de un registro de plantacion, dicho registro
     * tiene la misma fecha de siembra.
     */
    if (modifiedPlantingRecord.getSeedDate() == null) {
      modifiedPlantingRecord.setSeedDate(currentSeedDate);
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
     * Se calcula la fecha de cosecha del registro de plantacion
     * modificado en base a la nueva fecha de siembra y al
     * nuevo cultivo
     */
    modifiedPlantingRecord.setHarvestDate(cropService.calculateHarvestDate(modifiedPlantingRecord.getSeedDate(), modifiedPlantingRecord.getCrop()));

    /*
     * Se establece el estado del registro de plantacion a
     * modificar en base a la fecha de cosecha de su cultivo
     */
    modifiedPlantingRecord.setStatus(statusService.calculateStatus(modifiedPlantingRecord.getHarvestDate()));

    /*
     * Si el valor del encabezado de autorizacion de la peticion HTTP
     * dada, tiene un JWT valido, la aplicacion del lado servidor
     * devuelve el mensaje HTTP 200 (Ok) junto con los datos que el
     * cliente solicito actualizar
     */
    return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(plantingRecordService.modify(userId, plantingRecordId, modifiedPlantingRecord))).build();
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

    /*
     * **********************************************
     * Calculo de la necesidad de agua de riego de un
     * cultivo en la fecha actual
     * **********************************************
     */

    PlantingRecord givenPlantingRecord = plantingRecordService.find(plantingRecordId);
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
    if (climateRecordService.checkExistence(UtilDate.getTomorrowDate(), givenParcel)) {
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
       * Calculo de la necesidad de agua de riego de
       * un cultivo en la fecha actual
       */
      totalIrrigationWaterCurrentDate = irrigationRecordService.calculateTotalIrrigationWaterCurrentDate(givenParcel);
      irrigationWaterNeedCurrentDate = WaterMath.calculateIrrigationWaterNeed(currentClimateRecord.getEtc(),
          currentClimateRecord.getPrecip(), totalIrrigationWaterCurrentDate, excessWaterYesterday);
    }

    /*
     * Si en la base de datos subyacente NO existe el registro
     * climatico de la fecha actual para la parcela dada, se lo
     * solicita al servicio metereologico utilizado y se lo persiste.
     * 
     * Se hace esto porque lo que se busca con este metodo REST
     * es calcular la necesidad de agua de riego de un cultivo
     * en la fecha actual. Para esto se debe calcular la ETc
     * (evapotranspiracion del cultivo bajo condiciones estandar)
     * de la fecha actual, con lo cual, se la debe calcular con
     * datos meteorologicos de la fecha actual.
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

    /*
     * Se actualiza el atributo irrigationWaterNeed del registro
     * de plantacion en desarrollo sobre el que se solicita calcular
     * la necesidad de agua de riego del cultivo que contiene
     */
    plantingRecordService.updateIrrigationWaterNeed(plantingRecordId, givenParcel, String.valueOf(irrigationWaterNeedCurrentDate));

    IrrigationRecord newIrrigationRecord = new IrrigationRecord();
    newIrrigationRecord.setDate(currentDate);
    newIrrigationRecord.setIrrigationWaterNeed(String.valueOf(irrigationWaterNeedCurrentDate));
    newIrrigationRecord.setParcel(givenParcel);

    /*
     * Si el valor del encabezado de autorizacion de la peticion HTTP
     * dada, tiene un JWT valido, la aplicacion del lado servidor
     * devuelve el mensaje HTTP 200 (Ok) junto con los datos
     * pertinentes
     */
    return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(newIrrigationRecord)).build();
  }

}
