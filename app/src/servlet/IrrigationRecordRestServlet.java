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
import model.ClimateRecord;
import model.IrrigationRecord;
import model.Parcel;
import stateless.ClimateRecordServiceBean;
import stateless.IrrigationRecordServiceBean;
import stateless.PlantingRecordServiceBean;
import stateless.SecretKeyServiceBean;
import util.UtilDate;
import util.ErrorResponse;
import util.ReasonError;
import util.RequestManager;
import utilJwt.AuthHeaderManager;
import utilJwt.JwtManager;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;

@Path("/irrigationRecords")
public class IrrigationRecordRestServlet {

  // inject a reference to the IrrigationRecordServiceBean slsb
  @EJB
  IrrigationRecordServiceBean irrigationRecordService;

  @EJB
  PlantingRecordServiceBean plantingRecordService;

  @EJB
  ClimateRecordServiceBean climateRecordServiceBean;

  @EJB
  SecretKeyServiceBean secretKeyService;

  // mapea lista de pojo a JSON
  ObjectMapper mapper = new ObjectMapper();

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

    /*
     * Si el valor del encabezado de autorizacion de la peticion HTTP
     * dada, tiene un JWT valido, la aplicacion del lado servidor
     * devuelve el mensaje HTTP 200 (Ok) junto con los datos que el
     * cliente solicito persistir
     */
    return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(irrigationRecordService.create(newIrrigationRecord))).build();

    /*
     * TODO: Esto tiene que ser activado en el despliegue
     * final de la aplicacion cuando este listo y en
     * funcionamiento el modulo que obtiene y almacena
     * los registros climaticos de cada parcela para
     * cada dia del año
     */
    // setWaterAccumulatedToday(newIrrigationRecord.getParcel());
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
   * Establece la cantidad de agua acumulada en el registro
   * climatico del dia de hoy haciendo uso de la cantidad
   * de agua de lluvia del dia de ayer, de la cantidad de
   * agua acumulada del dia de ayer (agua a favor para el
   * dia de hoy), de la ETc del dia de ayer, de la ETo
   * del dia de ayer (en caso de que la ETc sea igual cero
   * debido a que ayer no haya habido un cultivo sembrado
   * en la parcela dada) y de la cantidad total de agua
   * utilizada en el riego del dia de hoy
   *
   * @param givenParcel
   */
  private void setWaterAccumulatedToday(Parcel givenParcel) {
    double yesterdayEto = 0.0;
    double yesterdayEtc = 0.0;
    double yesterdayPrecip = 0.0;
    double waterAccumulatedYesterday = 0.0;
    double totalIrrigationWaterToday = 0.0;
    double waterAccumulatedToday = 0.0;

    /*
     * Fecha actual para actualizar el atributo
     * agua acumulada del dia de hoy del registro
     * climatico del dia de hoy (por esto la fecha
     * actual) de la parcela dada
     */
    Calendar currentDate = Calendar.getInstance();

    /*
     * Fecha inmediatamente anterior a la fecha actual
     * para recuperar, de la base de datos, el registro
     * climatico del dia de ayer
     */
    Calendar yesterdayDate = UtilDate.getYesterdayDate();

    ClimateRecord yesterdayClimateLog = climateRecordServiceBean.find(yesterdayDate, givenParcel);
    yesterdayEto = yesterdayClimateLog.getEto();
    yesterdayEtc = yesterdayClimateLog.getEtc();

    /*
     * El atributo precip del modelo de datos ClimateRecord representa
     * la precipitacion del dia en milimetros. La unidad en la que se
     * mide este dato corresponde a la API Visual Crossing Weather y
     * al grupo de unidades en el que se le solicita datos meteorologicos.
     */
    yesterdayPrecip = yesterdayClimateLog.getPrecip();
    waterAccumulatedYesterday = yesterdayClimateLog.getExcessWater();

    totalIrrigationWaterToday = irrigationRecordService.calculateTotalIrrigationWaterCurrentDate(givenParcel);

    waterAccumulatedToday = 0.0;
    // waterAccumulatedToday = WaterMath.getWaterAccumulatedToday(yesterdayEtc, yesterdayEto, yesterdayPrecip,
    //     waterAccumulatedYesterday, totalIrrigationWaterToday);
    climateRecordServiceBean.updateExcessWater(currentDate, givenParcel, waterAccumulatedToday);
  }

}
