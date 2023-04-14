package servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import irrigation.WaterMath;
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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import stateless.ClimateRecordServiceBean;
import stateless.IrrigationRecordServiceBean;
import stateless.PlantingRecordServiceBean;
import stateless.SecretKeyServiceBean;
import model.ClimateRecord;
import model.IrrigationRecord;
import model.Parcel;
import util.UtilDate;
import util.ErrorResponse;
import util.ReasonError;
import util.RequestManager;
import utilJwt.AuthHeaderManager;
import utilJwt.JwtManager;


@Path("/irrigationRecords")
public class IrrigationRecordRestServlet {

  // inject a reference to the IrrigationRecordServiceBean slsb
  @EJB
  IrrigationRecordServiceBean irrigationRecordService;

  @EJB
  PlantingRecordServiceBean plantingRecordService;

  @EJB
  ClimateRecordServiceBean climateRecordService;

  @EJB
  SecretKeyServiceBean secretKeyService;

  // mapea lista de pojo a JSON
  ObjectMapper mapper = new ObjectMapper();

  /*
   * La necesidad de agua de riego de un registro de riego puede
   * tener el valor "n/a" (no disponible) en los siguientes casos:
   * - cuando la parcela a la que pertenece NO tiene un registro
   * de plantacion en desarrollo. En este caso al no haber un
   * registro de plantacion en desarrollo no hay un cultivo en
   * desarollo. Por lo tanto, no es posible calcular la
   * necesidad de agua de riego de un cultivo.
   * - cuando la parcela a la que pertenece tiene un registro
   * de plantacion en desarrollo, pero NO tiene el registro
   * climatico de la fecha actual. En este caso no se tiene la
   * evapotranspiracion del cultivo bajo condiciones estandar
   * (ETc) [mm/dia] ni la precipitacion [mm/dia] de dicha fecha,
   * por lo tanto, no es posible calcular la necesidad de agua
   * de riego de un cultivo.
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
    return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(irrigationRecordService.findAll(userId))).build();
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
    return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(irrigationRecordService.findAllByParcelName(userId, givenParcelName))).build();
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
    if (!irrigationRecordService.checkUserOwnership(userId, irrigationRecordId)) {
      return Response.status(Response.Status.FORBIDDEN).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.UNAUTHORIZED_ACCESS))).build();
    }

    /*
     * Si el valor del encabezado de autorizacion de la peticion HTTP
     * dada, tiene un JWT valido, la aplicacion del lado servidor
     * devuelve el mensaje HTTP 200 (Ok) junto con los datos solicitados
     * por el cliente
     */
    return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(irrigationRecordService.find(userId, irrigationRecordId))).build();
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
     * Si el objeto de tipo String referenciado por la referencia
     * contenida en la variable de tipo por referencia json de tipo
     * String, esta vacio, significa que el formulario correspondiente
     * a este metodo REST esta vacio (es decir, sus campos estan vacios).
     * Por lo tanto, la aplicacion del lado servidor retorna el mensaje
     * HTTP 400 (Bad request) junto con el mensaje "Debe completar todos
     * los campos del formulario" y no se realiza la operacion solicitada
     */
    if (json.isEmpty()) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.EMPTY_FORM))).build();
    }

    IrrigationRecord newIrrigationRecord = mapper.readValue(json, IrrigationRecord.class);

    /*
     * ******************************************
     * Controles sobre la definicion de los datos
     * ******************************************
     */

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
    if (newIrrigationRecord.getIrrigationDone() < 0) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.NEGATIVE_REALIZED_IRRIGATION))).build();
    }

    /*
     * El metodo getInstance de la clase Calendar retorna
     * una referencia a un objeto de tipo Calendar que
     * contiene la fecha actual.
     * 
     * Solo esta permitido crear un registro de riego con
     * la fecha actual. Este es el motivo de esta
     * instruccion.
     */
    newIrrigationRecord.setDate(Calendar.getInstance());

    /*
     * Un registro de riego que tiene su fecha igual a
     * la fecha actual es un registro de riego modificable.
     * Por lo tanto, se establece su atributo modifiable en
     * true.
     */
    newIrrigationRecord.setModifiable(true);

    /*
     * Si la parcela para la cual se crea un registro de riego,
     * tiene un cultivo en desarrollo, se establece dicho cultivo
     * en el nuevo registro de riego
     */
    if (plantingRecordService.checkOneInDevelopment(newIrrigationRecord.getParcel())) {
      newIrrigationRecord.setCrop(plantingRecordService.findInDevelopment(newIrrigationRecord.getParcel()).getCrop());
    }

    setIrrigationWaterNeed(newIrrigationRecord);
    newIrrigationRecord = irrigationRecordService.create(newIrrigationRecord);

    /*
     * Luego de persistir el nuevo registro de riego, se actualiza
     * la necesidad de agua de riego [mm/dia] del registro de
     * plantacion en desarrollo de la parcela de dicho registro
     * de riego teniendo en cuenta la cantidad total de agua de
     * riego
     */
    updateIrrigationWaterNeedDevelopingPlantingRecord(newIrrigationRecord.getParcel());

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
    if (!irrigationRecordService.checkUserOwnership(userId, irrigationRecordId)) {
      return Response.status(Response.Status.FORBIDDEN).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.UNAUTHORIZED_ACCESS))).build();
    }

    /*
     * Si el registro de riego a modificar fue generado por el
     * sistema, la aplicacion del lado servidor retorna el
     * mensaje HTTP 400 (Bad request) junto con el mensaje
     * "No esta permitida la modificacion de un registro de
     * riego generado por el sistema" y no se realiza la
     * operacion solicitada
     */
    if (irrigationRecordService.isGeneratedBySystem(irrigationRecordId)) {
      return Response.status(Response.Status.BAD_REQUEST)
        .entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.MODIFICATION_IRRIGATION_RECORD_GENERATED_BY_SYSTEM_NOT_ALLOWED))).build();
    }

    /*
     * ******************************************
     * Control sobre la temporalidad de los datos
     * ******************************************
     */

    /*
     * Si se intenta modificar un registro de riego del pasado
     * (es decir, uno que tiene su fecha estrictamente menor que
     * la fecha actual), la aplicacion del lado servidor retorna
     * el mensaje HTTP 400 (Bad request) junto con el mensaje "No
     * esta permitida la modificacion de un registro de riego del
     * pasado" y no se realiza la operacion solicitada
     */
    if (irrigationRecordService.isFromPast(userId, irrigationRecordId)) {
      return Response.status(Response.Status.BAD_REQUEST).
        entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.MODIFICATION_PAST_IRRIGATION_RECORD_NOT_ALLOWED))).build();
    }

    /*
     * Si el objeto de tipo String referenciado por la referencia
     * contenida en la variable de tipo por referencia json de tipo
     * String, esta vacio, significa que el formulario correspondiente
     * a este metodo REST esta vacio (es decir, sus campos estan vacios).
     * Por lo tanto, la aplicacion del lado servidor retorna el mensaje
     * HTTP 400 (Bad request) junto con el mensaje "Debe completar todos
     * los campos del formulario" y no se realiza la operacion solicitada
     */
    if (json.isEmpty()) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.EMPTY_FORM))).build();
    }

    IrrigationRecord modifiedIrrigationRecord = mapper.readValue(json, IrrigationRecord.class);

    /*
     * ******************************************
     * Controles sobre la definicion de los datos
     * ******************************************
     */

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
     * Si el valor del encabezado de autorizacion de la peticion HTTP
     * dada, tiene un JWT valido, la aplicacion del lado servidor
     * devuelve el mensaje HTTP 200 (Ok) junto con los datos que el
     * cliente solicito modificar
     */
    return Response.status(Response.Status.OK).
      entity(mapper.writeValueAsString(irrigationRecordService.modify(userId, irrigationRecordId, modifiedIrrigationRecord))).build();
  }

  /**
   * Establece la necesidad de agua de riego [mm/dia]
   * de un registro de riego
   * 
   * @param givenIrrigationRecord
   */
  private void setIrrigationWaterNeed(IrrigationRecord givenIrrigationRecord) {
    /*
     * El metodo getInstance de la clase Calendar retorna
     * la referencia a un objeto de tipo Calendar que
     * contiene la fecha actual
     */
    Calendar currentDate = Calendar.getInstance();
    Calendar yesterdayDate = UtilDate.getYesterdayDate();
    ClimateRecord currentClimateRecord = null;
    Parcel givenParcel = givenIrrigationRecord.getParcel();

    /*
     * Si la parcela a la que pertenece un registro de riego NO tiene
     * un registro de plantacion en desarrollo, NO hay un cultivo en
     * desarrollo para el cual calcular la necesidad de agua de riego
     * [mm/dia] de la fecha actual, por lo tanto, la necesidad de agua
     * de riego de un registro de riego es "n/a" (no disponible)
     */
    if (!plantingRecordService.checkOneInDevelopment(givenParcel)) {
      givenIrrigationRecord.setIrrigationWaterNeed(NOT_AVAILABLE);
      return;
    }

    /*
     * Si la parcela a la que pertenece un registro de riego tiene un
     * registro de plantacion en desarrollo, pero NO tiene el registro
     * climatico de la fecha actual, NO se disponen de la evapotranspiracion
     * del cultivo bajo condiciones estandar (ETc) [mm/dia] ni de la
     * precipitacion [mm/dia] de la fecha actual, las cuales son
     * datos necesarios para calcular la necesidad de agua de riego
     * [mm/dia] de un cultivo en desarrollo en la fecha actual.
     * 
     * Por lo tanto, la necesidad de agua de riego de un registro
     * de riego es "n/a" (no disponible).
     */
    if (!climateRecordService.checkExistence(currentDate, givenParcel)) {
      givenIrrigationRecord.setIrrigationWaterNeed(NOT_AVAILABLE);
      return;
    }

    currentClimateRecord = climateRecordService.find(currentDate, givenParcel);
    double currentIrrigationWaterNeed = 0.0;
    double excessWaterYesterday = 0.0;

    /*
     * Si en la base de datos subyacente existe el registro climatico
     * del dia inmediatamente anterior a la fecha actual, se obtiene
     * su agua excedente para calcular la necesidad de agua de riego
     * [mm/dia] del cultivo que esta en desarrollo en la fecha actual.
     * En caso contrario, se asume que el agua excedente de dicho dia
     * es 0.
     */
    if (climateRecordService.checkExistence(yesterdayDate, givenParcel)) {
      excessWaterYesterday = climateRecordService.find(currentDate, givenParcel).getExcessWater();
    }

    /*
     * Se calcula de la necesidad de agua de riego [mm/dia] del cultivo
     * que esta en desarrollo en la fecha actual sin tener en cuenta
     * la cantidad total de agua de riego de la fecha actual porque
     * lo que se busca con esto es que un registro de riego siempre
     * contenga la necesidad de agua de riego inicial, la cual, es
     * la que se obtiene antes realizar cualquier riego
     */
    currentIrrigationWaterNeed = WaterMath.calculateIrrigationWaterNeed(currentClimateRecord.getEtc(),
        currentClimateRecord.getPrecip(), 0, excessWaterYesterday);

    givenIrrigationRecord.setIrrigationWaterNeed(String.valueOf(currentIrrigationWaterNeed));
  }

  /**
   * Actualiza la necesidad de agua de riego [mm/dia] del registro
   * de plantacion en desarrollo de una parcela de un registro de
   * riego.
   * 
   * Hay que tener en cuenta que este metodo debe ser ejecutado
   * despues de persistir o modificar un registro de riego, ya
   * que de lo contrario NO se tendra en cuenta la cantidad total
   * de agua de riego de la fecha actual en el calculo de la
   * necesidad de agua de riego [mm/dia] de un cultivo que esta
   * en desarrollo en la fecha actual, lo cual, dara una necesidad
   * de agua de riego incorrecta.
   * 
   * @param givenParcel
   */
  private void updateIrrigationWaterNeedDevelopingPlantingRecord(Parcel givenParcel) {
    /*
     * El metodo getInstance de la clase Calendar retorna
     * la referencia a un objeto de tipo Calendar que
     * contiene la fecha actual
     */
    Calendar currentDate = Calendar.getInstance();
    Calendar yesterdayDate = UtilDate.getYesterdayDate();
    ClimateRecord currentClimateRecord = null;

    double currentIrrigationWaterNeed = 0.0;
    double excessWaterYesterday = 0.0;
    double totalIrrigationWaterCurrentDate = 0.0;

    /*
     * Si la parcela dada tiene un registro de plantacion en desarrollo
     * (es decir, tiene un cultivo en desarrollo) y tiene el registro
     * climatico de la fecha actual, se calcula la necesidad de agua
     * de riego [mm/dia] del cultivo que esta en desarrollo en la
     * fecha actual y se la utiliza para actualizar la necesidad de
     * agua de riego de dicho registro de plantacion
     */
    if (plantingRecordService.checkOneInDevelopment(givenParcel) && (climateRecordService.checkExistence(currentDate, givenParcel))) {
      currentClimateRecord = climateRecordService.find(currentDate, givenParcel);

      /*
       * Si en la base de datos subyacente existe el registro climatico
       * del dia inmediatamente anterior a la fecha actual, se obtiene
       * su agua excedente para calcular la necesidad de agua de riego
       * [mm/dia] del cultivo que esta en desarrollo en la fecha actual.
       * En caso contrario, se asume que el agua excedente de dicho dia
       * es 0.
       */
      if (climateRecordService.checkExistence(yesterdayDate, givenParcel)) {
        excessWaterYesterday = climateRecordService.find(currentDate, givenParcel).getExcessWater();
      }

      totalIrrigationWaterCurrentDate = irrigationRecordService.calculateTotalIrrigationWaterCurrentDate(givenParcel);

      /*
       * Calculo de la necesidad de agua de riego [mm/dia] del cultivo
       * que esta en desarrollo en la fecha actual
       */
      currentIrrigationWaterNeed = WaterMath.calculateIrrigationWaterNeed(currentClimateRecord.getEtc(),
          currentClimateRecord.getPrecip(), totalIrrigationWaterCurrentDate, excessWaterYesterday);

      plantingRecordService.updateIrrigationWaterNeed(plantingRecordService.findInDevelopment(givenParcel).getId(),
          givenParcel, String.valueOf(currentIrrigationWaterNeed));
    }

  }

}
