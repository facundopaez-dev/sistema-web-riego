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
import model.IrrigationWaterNeedFormData;
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
     * Si la fecha de un registro de riego nuevo NO esta
     * definida, la aplicacion del lado servidor retorna el
     * mensaje HTTP 400 (Bad request) junto con el mensaje
     * "La fecha debe estar definida" y no se realiza la
     * operacion solicitada
     */
    if (newIrrigationRecord.getDate() == null) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.UNDEFINED_DATE))).build();
    }

    /*
     * El metodo getInstance de la clase Calendar retorna
     * la referencia a un objeto de tipo Calendar que
     * contiene la fecha actual
     */
    Calendar currentDate = Calendar.getInstance();

    /*
     * Si la fecha de un registro de riego nuevo es estrictamente
     * mayor a la fecha actual, la aplicacion del lado servidor
     * retorna el mensaje HTTP 400 (Bad request) junto con el mensaje
     * "No esta permitido que un registro de riego tenga una fecha
     * estrictamente mayor (es decir, posterior) a la fecha actual"
     * y no se realiza la operacion solicitada.
     * 
     * De esta manera, se evita la creacion de registros de riego
     * del futuro, ya que no tiene sentido registrar la cantidad de
     * agua que se utilizara para el riego de una parcela o un cultivo.
     */
    if (UtilDate.compareTo(newIrrigationRecord.getDate(), currentDate) > 0) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.IRRIGATION_RECORD_OF_THE_FUTURE_NOT_ALLOWED))).build();
    }

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
     * Si el riego realizado es negativo, la aplicacion del lado
     * servidor retorna el mensaje HTTP 400 (Bad request) junto
     * con el mensaje "El riego realizado debe ser mayor o igual
     * a cero" y no se realiza la operacion solicitada
     */
    if (newIrrigationRecord.getIrrigationDone() < 0) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.NEGATIVE_REALIZED_IRRIGATION))).build();
    }

    /*
     * Si la parcela para la cual se crea un registro de riego,
     * tiene un cultivo en desarrollo, se establece dicho cultivo
     * en el nuevo registro de riego
     */
    if (plantingRecordService.checkOneInDevelopment(newIrrigationRecord.getParcel())) {
      newIrrigationRecord.setCrop(plantingRecordService.findInDevelopment(newIrrigationRecord.getParcel()).getCrop());
    }

    /*
     * Un registro de riego nuevo se debe poder modificar,
     * por lo tanto, se establece su atributo modifiable en
     * true
     */
    newIrrigationRecord.setModifiable(true);

    /*
     * Persistencia del nuevo registro de riego
     */
    newIrrigationRecord = irrigationRecordService.create(newIrrigationRecord);

    /*
     * Luego de persistir el nuevo registro de riego, se actualiza
     * la necesidad de agua de riego [mm/dia] del registro de
     * plantacion en desarrollo de la parcela de dicho registro
     * de riego teniendo en cuenta la cantidad total de agua de
     * de riego de la fecha actual
     */
    updateIrrigationWaterNeedDevelopingPlantingRecord(newIrrigationRecord.getParcel());

    /*
     * Luego de persistir el nuevo registro de riego, se actualiza
     * el agua excedente [mm/dia] del registro climatico de la fecha
     * actual de la parcela de dicho registro de riego
     */
    updateExcessWaterCurrentDate(newIrrigationRecord.getParcel());

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
     * Si el registro de riego a modificar NO es modificable,
     * la aplicacion del lador servidor retorna el mensaje
     * HTTP 400 (Bad request) junto con el mensaje "No esta
     * permitida la modificacion de un registro de riego no
     * modificable" y no se realiza la operacion solicitada
     */
    if (!irrigationRecordService.isModifiable(irrigationRecordId)) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.MODIFICATION_NON_MODIFIABLE_IRRIGATION_RECORD_NOT_ALLOWED)))
          .build();
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
     * Si la fecha del registro de riego modificada NO esta
     * definida, la aplicacion del lado servidor retorna el
     * mensaje HTTP 400 (Bad request) junto con el mensaje
     * "La fecha debe estar definida" y no se realiza la
     * operacion solicitada
     */
    if (modifiedIrrigationRecord.getDate() == null) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.UNDEFINED_DATE))).build();
    }

    /*
     * El metodo getInstance de la clase Calendar retorna
     * la referencia a un objeto de tipo Calendar que
     * contiene la fecha actual
     */
    Calendar currentDate = Calendar.getInstance();

    /*
     * Si la fecha del registro de riego modificado es
     * es estrictamente mayor a la fecha actual, la
     * aplicacion del lado servidor retorna el mensaje
     * HTTP 400 (Bad request) juto con el mensaje "No
     * esta permitido que un registro de riego tenga
     * una fecha estrictamente mayor (es decir, posterior)
     * a la fecha actual" y no se realiza la operacion
     * solicitada.
     * 
     * De esta manera, se evita la modificacion de registros
     * de riego con fechas futuras, ya que no tiene sentido
     * registrar la cantidad de agua que se utilizara
     * para el riego de una parcela o un cultivo.
     */
    if (UtilDate.compareTo(modifiedIrrigationRecord.getDate(), currentDate) > 0) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.IRRIGATION_RECORD_OF_THE_FUTURE_NOT_ALLOWED))).build();
    }

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

    modifiedIrrigationRecord = irrigationRecordService.modify(userId, irrigationRecordId, modifiedIrrigationRecord);

    /*
     * Luego de modificar el riego realizado de un registro de
     * riego, se actualiza la necesidad de agua de riego [mm/dia]
     * del registro de plantacion en desarrollo de la parcela de
     * dicho registro de riego teniendo en cuenta la cantidad total
     * de agua de riego de la fecha actual
     */
    updateIrrigationWaterNeedDevelopingPlantingRecord(modifiedIrrigationRecord.getParcel());

    /*
     * Luego de modificar el riego realizado de un registro de riego,
     * se actualiza el agua excedente [mm/dia] del registro climatico
     * de la fecha actual de la parcela de dicho registro de riego
     */
    updateExcessWaterCurrentDate(modifiedIrrigationRecord.getParcel());

    /*
     * Si el valor del encabezado de autorizacion de la peticion HTTP
     * dada, tiene un JWT valido, la aplicacion del lado servidor
     * devuelve el mensaje HTTP 200 (Ok) junto con los datos que el
     * cliente solicito modificar
     */
    return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(modifiedIrrigationRecord)).build();
  }

  @POST
  @Path("/fromIrrigationWaterNeedFormData")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response createFromIrrigationWaterNeedFormData(@Context HttpHeaders request, String json) throws IOException {
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

    IrrigationWaterNeedFormData irrigationWaterNeedFormData = mapper.readValue(json, IrrigationWaterNeedFormData.class);
    IrrigationRecord newIrrigationRecord = new IrrigationRecord();
    newIrrigationRecord.setDate(irrigationWaterNeedFormData.getDate());
    newIrrigationRecord.setParcel(irrigationWaterNeedFormData.getParcel());
    newIrrigationRecord.setCrop(irrigationWaterNeedFormData.getCrop());
    newIrrigationRecord.setIrrigationDone(irrigationWaterNeedFormData.getIrrigationDone());

    /*
     * Un registro de riego nuevo se debe poder modificar,
     * por lo tanto, se establece su atributo modifiable en
     * true
     */
    newIrrigationRecord.setModifiable(true);

    /*
     * ******************************************
     * Controles sobre la definicion de los datos
     * ******************************************
     */

    /*
     * Si la fecha de un registro de riego nuevo NO esta
     * definida, la aplicacion del lado servidor retorna el
     * mensaje HTTP 400 (Bad request) junto con el mensaje
     * "La fecha debe estar definida" y no se realiza la
     * operacion solicitada
     */
    if (newIrrigationRecord.getDate() == null) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.UNDEFINED_DATE))).build();
    }

    /*
     * El metodo getInstance de la clase Calendar retorna
     * la referencia a un objeto de tipo Calendar que
     * contiene la fecha actual
     */
    Calendar currentDate = Calendar.getInstance();

    /*
     * Si la fecha de un registro de riego nuevo es estrictamente
     * mayor a la fecha actual, la aplicacion del lado servidor
     * retorna el mensaje HTTP 400 (Bad request) junto con el mensaje
     * "No esta permitido que un registro de riego tenga una fecha
     * estrictamente mayor (es decir, posterior) a la fecha actual"
     * y no se realiza la operacion solicitada.
     * 
     * De esta manera, se evita la creacion de registros de riego
     * del futuro, ya que no tiene sentido registrar la cantidad de
     * agua que se utilizara para el riego de una parcela o un cultivo.
     */
    if (UtilDate.compareTo(newIrrigationRecord.getDate(), currentDate) > 0) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.IRRIGATION_RECORD_OF_THE_FUTURE_NOT_ALLOWED))).build();
    }

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
     * Si el riego realizado es negativo, la aplicacion del lado
     * servidor retorna el mensaje HTTP 400 (Bad request) junto
     * con el mensaje "El riego realizado debe ser mayor o igual
     * a cero" y no se realiza la operacion solicitada
     */
    if (newIrrigationRecord.getIrrigationDone() < 0) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.NEGATIVE_REALIZED_IRRIGATION))).build();
    }

    /*
     * Si la parcela para la cual se crea un registro de riego,
     * tiene un cultivo en desarrollo, se establece dicho cultivo
     * en el nuevo registro de riego
     */
    if (plantingRecordService.checkOneInDevelopment(newIrrigationRecord.getParcel())) {
      newIrrigationRecord.setCrop(plantingRecordService.findInDevelopment(newIrrigationRecord.getParcel()).getCrop());
    }

    /*
     * Persistencia del nuevo registro de riego
     */
    newIrrigationRecord = irrigationRecordService.create(newIrrigationRecord);

    /*
     * Luego de persistir el nuevo registro de riego, se actualiza
     * la necesidad de agua de riego [mm/dia] del registro de
     * plantacion en desarrollo de la parcela de dicho registro
     * de riego teniendo en cuenta la cantidad total de agua de
     * de riego de la fecha actual
     */
    updateIrrigationWaterNeedDevelopingPlantingRecord(newIrrigationRecord.getParcel());

    /*
     * Luego de persistir el nuevo registro de riego, se actualiza
     * el agua excedente [mm/dia] del registro climatico de la fecha
     * actual de la parcela de dicho registro de riego
     */
    updateExcessWaterCurrentDate(newIrrigationRecord.getParcel());

    /*
     * Si el valor del encabezado de autorizacion de la peticion HTTP
     * dada, tiene un JWT valido, la aplicacion del lado servidor
     * devuelve el mensaje HTTP 200 (Ok) junto con los datos que el
     * cliente solicito persistir
     */
    return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(newIrrigationRecord)).build();
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
       * Si la parcela dada tiene el registro climatico del dia inmediatamente
       * anterior a la fecha actual, se obtiene el agua excedente del mismo
       * para calcular la necesidad de agua de riego [mm/dia] del cultivo que
       * esta en desarrollo en la fecha actual. En caso contrario, se asume
       * que el agua excedente de dicho dia es 0.
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

  /**
   * Actualiza el agua excedente del registro climatico de la
   * fecha actual de una parcela dada contemplando el riego
   * realizado ingresado mediante los metodos create y modify
   * de esta clase
   * 
   * @param givenParcel
   */
  private void updateExcessWaterCurrentDate(Parcel givenParcel) {
    /*
     * El metodo getInstance de la clase Calendar retorna
     * la referencia a un objeto de tipo Calendar que
     * contiene la fecha actual.
     */
    climateRecordService.updateExcessWater(Calendar.getInstance(), givenParcel,
        calculateExcessWaterCurrentDate(givenParcel));
  }

  /**
   * @param givenParcel
   * @return punto flotante que representa el agua excedente que
   * hay en una parcela en la fecha actual
   */
  private double calculateExcessWaterCurrentDate(Parcel givenParcel) {
    /*
     * El metodo getInstance de la clase Calendar retorna
     * la referencia a un objeto de tipo Calendar que
     * contiene la fecha actual
     */
    Calendar currentDate = Calendar.getInstance();
    ClimateRecord currentClimateRecord = null;

    double excessWaterCurrentDate = 0.0;
    double totalIrrigationWaterCurrentDate = 0.0;
    double excessWaterYesterday = 0.0;
    double etCurrentDate = 0.0;

    /*
     * Si el registro climatico de la fecha actual existe, se
     * calcula el agua excedente que hay en una parcela en la
     * fecha actual contemplando el riego realizado ingresado
     * mediante los metodos create y modify de esta clase
     */
    if (climateRecordService.checkExistence(currentDate, givenParcel)) {
      currentClimateRecord = climateRecordService.find(currentDate, givenParcel);

      /*
       * Si el registro climatico del dia inmediatamente anterior
       * a la fecha actual de una parcela, existe en la base de
       * datos subyacente, se obtiene el agua excedente de dicho
       * dia. En caso contrario, se asume que el agua excedente
       * del dia inmediatamente anterior a la fecha actual es 0.
       */
      if (climateRecordService.checkExistence(UtilDate.getYesterdayDate(), givenParcel)) {
        excessWaterYesterday = climateRecordService.find(UtilDate.getYesterdayDate(), givenParcel).getExcessWater();
      }

      /*
       * Cuando una parcela NO tiene un cultivo sembrado y en
       * desarrollo, la ETc de uno o varios de sus registros
       * climaticos tiene el valor 0.0, ya que si no hay un
       * cultivo en desarrollo NO es posible calcular la ETc
       * (evapotranspiracion del cultivo bajo condiciones
       * estandar) del mismo. Por lo tanto, se debe utilizar la
       * ETo (evapotranspiracion del cultivo de referencia) para
       * calcular el agua excedente de un registro climatico
       * en la fecha actual.
       * 
       * En caso contrario, se debe utilizar la ETc para calcular
       * el agua excedente de un registro climatico en la fecha
       * actual.
       */
      if (currentClimateRecord.getEtc() == 0.0) {
        etCurrentDate = currentClimateRecord.getEto();
      } else {
        etCurrentDate = currentClimateRecord.getEtc();
      }

      totalIrrigationWaterCurrentDate = irrigationRecordService.calculateTotalIrrigationWaterCurrentDate(givenParcel);

      /*
       * Calculo del agua excedente de una parcela dada
       * en la fecha actual
       */
      excessWaterCurrentDate = WaterMath.calculateExcessWater(etCurrentDate, currentClimateRecord.getPrecip(),
          totalIrrigationWaterCurrentDate, excessWaterYesterday);
    } // End if

    return excessWaterCurrentDate;
  }

}
