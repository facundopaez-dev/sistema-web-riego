package servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Collection;
import javax.ejb.EJB;
import javax.ws.rs.Consumes;
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
import model.ClimateRecord;
import stateless.ClimateRecordServiceBean;
import stateless.SecretKeyServiceBean;
import util.ErrorResponse;
import util.ReasonError;
import util.RequestManager;
import utilJwt.AuthHeaderManager;
import utilJwt.JwtManager;
import climate.ClimateClient;
import model.Parcel;

@Path("/climateRecords")
public class ClimateRecordRestServlet {

  // inject a reference to the ClimateRecordServiceBean slsb
  @EJB ClimateRecordServiceBean climateRecordService;
  @EJB SecretKeyServiceBean secretKeyService;

  // Mapea lista de pojo a JSON
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
    return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(climateRecordService.findAll(userId))).build();
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
    return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(climateRecordService.findAllByParcelName(userId, givenParcelName))).build();
  }

  @GET
  @Path("/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response find(@Context HttpHeaders request, @PathParam("id") int climateRecordId) throws IOException {
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
    if (!climateRecordService.checkExistence(climateRecordId)) {
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
     * el registro climatico solicitado (debido a que ninguna de
     * sus parcelas tiene el registro climatico solicitado), la
     * aplicacion del lado servidor devuelve el mensaje HTTP 403
     * (Forbidden) junto con el mensaje "Acceso no autorizado"
     * (contenido en el enum ReasonError) y no se realiza la
     * operacion solicitada
     */
    if (!climateRecordService.checkUserOwnership(userId, climateRecordId)) {
      return Response.status(Response.Status.FORBIDDEN).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.UNAUTHORIZED_ACCESS))).build();
    }

    /*
     * Si el valor del encabezado de autorizacion de la peticion HTTP
     * dada, tiene un JWT valido, la aplicacion del lado servidor
     * devuelve el mensaje HTTP 200 (Ok) junto con los datos solicitados
     * por el cliente
     */
    return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(climateRecordService.find(userId, climateRecordId))).build();
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

    ClimateRecord newClimateRecord = mapper.readValue(json, ClimateRecord.class);

    /*
     * Si la fecha NO esta definida, la aplicacion del lado
     * servidor retorna el mensaje HTTP 400 (Bad request)
     * junto con el mensaje "La fecha debe estar definida"
     * y no se realiza la operacion solicitada
     */
    if (newClimateRecord.getDate() == null) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.UNDEFINED_DATE))).build();
    }

    /*
     * Si la velocidad del viento tiene un valor menor a 0.0,
     * la aplicacion del lado servidor retorna el mensaje
     * HTTP 400 (Bad request) junto con el mensaje "La
     * velocidad del viento debe ser un valor mayor o
     * igual a 0.0" y no se realiza la operacion solicitada
     */
    if (newClimateRecord.getWindSpeed() < 0.0) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.INVALID_WIND_SPEED))).build();
    }

    /*
     * Si la probabilidad de la precipitacion tiene un valor
     * menor a 0.0 o mayor a 100, la aplicacion del lado
     * servidor retorna el mensaje HTTP 400 (Bad request) junto
     * con el mensaje "La probabilidad de la precipitacion debe
     * ser un valor mayor o igual a 0.0" y no se realiza la
     * operacion solicitada
     */
    if (newClimateRecord.getPrecipProbability() < 0.0 || newClimateRecord.getPrecipProbability() > 100) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.INVALID_PRECIPITATION_PROBABILITY))).build();
    }

    /*
     * Si la precipitacion tiene un valor menor a 0.0, la
     * aplicacion del lado servidor retorna el mensaje
     * HTTP 400 (Bad request) junto con el mensaje "La
     * precipitacion debe ser un valor mayor o igual a
     * 0.0" y no se realiza la operacion solicitada
     */
    if (newClimateRecord.getPrecip() < 0.0) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.INVALID_PRECIPITATION))).build();
    }

    /*
     * Si el agua excedente tiene un valor menor a 0.0,
     * la aplicacion del lado servidor retorna el mensaje
     * HTTP 400 (Bad request) junto con el mensaje "El
     * agua excedente debe ser un valor mayor o igual a
     * 0.0" y no se realiza la operacion solicitada
     */
    if (newClimateRecord.getExcessWater() < 0.0) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.INVALID_EXCESS_WATER))).build();
    }

    /*
     * Si la nubosidad tiene un valor menor a 0.0 o mayor
     * a 100, la aplicacion del lado servidor retorna el
     * mensaje HTTP 400 (Bad request) junto con el mensaje
     * "La nubosidad debe ser un valor entre 0.0 y 100,
     * incluido" y no se realiza la operacion solicitada
     */
    if (newClimateRecord.getCloudCover() < 0.0 || newClimateRecord.getCloudCover() > 100) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.INVALID_CLOUDINESS))).build();
    }

    /*
     * Si la presion atmosferica tiene un valor menor o igual
     * a 0.0, la aplicacion del lado servidor retorna el mensaje
     * HTTP 400 (Bad request) junto con el mensaje "La presion
     * atmosferica debe ser un valor mayor a 0.0"
     */
    if (newClimateRecord.getAtmosphericPressure() <= 0.0) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.INVALID_ATMOSPHERIC_PRESSURE))).build();
    }

    /*
     * Si la evapotranspiracion del cultivo de referencia (ETo)
     * tiene un valor menor a 0.0, la aplicacion del lado
     * servidor retorna el mensaje HTTP 400 (Bad request)
     * junto con el mensaje "La evapotranspiracion del cultivo
     * de referencia (ETo) debe ser un valor mayor o igual
     * a 0.0" y no se realiza la operacion solicitada
     */
    if (newClimateRecord.getEto() < 0.0) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.INVALID_ETO))).build();
    }

    /*
     * Si la evapotranspiracion del cultivo (ETc) tiene un
     * valor menor a 0.0, la aplicacion del lado servidor
     * retorna el mensaje HTTP 400 (Bad request) junto
     * con el mensaje "La evapotranspiracion del cultivo
     * (ETc) debe ser un valor mayor o igual a 0.0" y no
     * se realiza la operacion solicitada
     */
    if (newClimateRecord.getEtc() < 0.0) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.INVALID_ETC))).build();
    }

    /*
     * Si la parcela NO esta definida, la aplicacion del
     * lado servidor retorna el mensaje HTTP 400 (Bad
     * request) junto con el mensaje "La parcela debe
     * estar definida" y no se realiza la operacion
     * solicitada
     */
    if (newClimateRecord.getParcel() == null) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.INDEFINITE_PARCEL))).build();
    }

    /*
     * Si el registro climatico a crear tiene una fecha estrictamente
     * menor a la fecha actual, la aplicacion del lado servidor
     * retorna el mensaje HTTP 400 (Bad request) junto con el
     * mensaje "No esta permitida la creacion de un registro
     * climatico del pasado (es decir, uno que tiene una fecha
     * anterior a la fecha actual)" y no se realiza la operacion
     * solicitada
     */
    if (climateRecordService.isFromPast(newClimateRecord)) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.CREATION_PAST_CLIMATE_RECORD_NOT_ALLOWED))).build();
    }

    /*
     * Si el registro climatico a crear para una parcela,
     * tiene una fecha igual a la fecha de otro registro
     * climatico de la misma parcela, la aplicacion del
     * lado servidor retorna el mensaje HTTP 400 (Bad
     * request) junto con el mensaje "Ya existe un registro
     * climatico con la fecha ingresada para la parcela
     * seleccionada" y no se realiza la operacion solicitada
     */
    if (climateRecordService.checkExistence(newClimateRecord.getDate(), newClimateRecord.getParcel())) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.EXISTING_CLIMATE_RECORD))).build();
    }

    /*
     * Se establece en true la variable modifiable de un
     * registro climatico nuevo porque un registro un
     * registro climatico con una fecha mayor o igual a
     * la fecha actual es modificable.
     * 
     * La oracion "con una fecha mayor o igual a la
     * fecha actual" se debe a la instruccion if que
     * comprueba si la fecha de un nuevo registro
     * climatico es del pasado. Dicha instruccion esta
     * en este metodo REST.
     */
    newClimateRecord.setModifiable(true);

    /*
     * Si el valor del encabezado de autorizacion de la peticion HTTP
     * dada, tiene un JWT valido, la aplicacion del lado servidor
     * devuelve el mensaje HTTP 200 (Ok) junto con los datos que el
     * cliente solicito persistir
     */
    return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(climateRecordService.create(newClimateRecord))).build();
  }

  @PUT
  @Path("/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response modify(@Context HttpHeaders request, @PathParam("id") int climateRecordId, String json) throws IOException {
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
    if (!climateRecordService.checkExistence(climateRecordId)) {
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
     * el registro climatico solicitado (debido a que ninguna de
     * sus parcelas tiene el registro climatico solicitado), la
     * aplicacion del lado servidor devuelve el mensaje HTTP 403
     * (Forbidden) junto con el mensaje "Acceso no autorizado"
     * (contenido en el enum ReasonError) y no se realiza la
     * operacion solicitada
     */
    if (!climateRecordService.checkUserOwnership(userId, climateRecordId)) {
      return Response.status(Response.Status.FORBIDDEN).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.UNAUTHORIZED_ACCESS))).build();
    }

    /*
     * Si el registro climatico a modificar es del pasado (es decir,
     * es anterior a la fecha actual), la aplicacion del lado servidor
     * retorna el mensaje HTTP 400 (Bad request) junto con el mensaje
     * "No esta permitida la modifcacion de un regitro climatico del
     * pasado (es decir, que tiene una fecha anterior a la fecha
     * actual)" y no se realiza la operacion solicitada
     */
    if (climateRecordService.isFromPast(climateRecordService.find(climateRecordId))) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.MODIFICATION_PAST_CLIMATE_RECORD_NOT_ALLOWED))).build();
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

    ClimateRecord modifiedClimateRecord = mapper.readValue(json, ClimateRecord.class);

    /*
     * Si la fecha NO esta definida, la aplicacion del lado
     * servidor retorna el mensaje HTTP 400 (Bad request)
     * junto con el mensaje "La fecha debe estar definida"
     * y no se realiza la operacion solicitada
     */
    if (modifiedClimateRecord.getDate() == null) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.UNDEFINED_DATE))).build();
    }

    /*
     * Si la fecha modificada del registro climatico a modificar
     * es del pasado (es decir, es anterior a la fecha actual),
     * la aplicacion del lado servidor retorna el mensaje HTTP
     * 400 (Bad request) junto con el mensaje "No esta permitido
     * modificar un registro climatico con una fecha del pasado
     * (es decir, una fecha anterior a la fecha actual)" y no se
     * realiza la operacion solicitada
     */
    if (climateRecordService.isFromPast(modifiedClimateRecord)) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.MODIFICATION_CLIMATE_RECORD_WITH_PAST_DATE_NOT_ALLOWED))).build();
    }

    /*
     * Si la velocidad del viento tiene un valor menor a 0.0,
     * la aplicacion del lado servidor retorna el mensaje
     * HTTP 400 (Bad request) junto con el mensaje "La
     * velocidad del viento debe ser un valor mayor o
     * igual a 0.0" y no se realiza la operacion solicitada
     */
    if (modifiedClimateRecord.getWindSpeed() < 0.0) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.INVALID_WIND_SPEED))).build();
    }

    /*
     * Si la probabilidad de la precipitacion tiene un valor
     * menor a 0.0 o mayor a 100, la aplicacion del lado
     * servidor retorna el mensaje HTTP 400 (Bad request) junto
     * con el mensaje "La probabilidad de la precipitacion debe
     * ser un valor mayor o igual a 0.0" y no se realiza la
     * operacion solicitada
     */
    if (modifiedClimateRecord.getPrecipProbability() < 0.0 || modifiedClimateRecord.getPrecipProbability() > 100) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.INVALID_PRECIPITATION_PROBABILITY))).build();
    }

    /*
     * Si la precipitacion tiene un valor menor a 0.0, la
     * aplicacion del lado servidor retorna el mensaje
     * HTTP 400 (Bad request) junto con el mensaje "La
     * precipitacion debe ser un valor mayor o igual a
     * 0.0" y no se realiza la operacion solicitada
     */
    if (modifiedClimateRecord.getPrecip() < 0.0) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.INVALID_PRECIPITATION))).build();
    }

    /*
     * Si el agua excedente tiene un valor menor a 0.0,
     * la aplicacion del lado servidor retorna el mensaje
     * HTTP 400 (Bad request) junto con el mensaje "El
     * agua excedente debe ser un valor mayor o igual a
     * 0.0" y no se realiza la operacion solicitada
     */
    if (modifiedClimateRecord.getExcessWater() < 0.0) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.INVALID_EXCESS_WATER))).build();
    }

    /*
     * Si la nubosidad tiene un valor menor a 0.0 o mayor
     * a 100, la aplicacion del lado servidor retorna el
     * mensaje HTTP 400 (Bad request) junto con el mensaje
     * "La nubosidad debe ser un valor entre 0.0 y 100,
     * incluido" y no se realiza la operacion solicitada
     */
    if (modifiedClimateRecord.getCloudCover() < 0.0 || modifiedClimateRecord.getCloudCover() > 100) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.INVALID_CLOUDINESS))).build();
    }

    /*
     * Si la presion atmosferica tiene un valor menor o igual
     * a 0.0, la aplicacion del lado servidor retorna el mensaje
     * HTTP 400 (Bad request) junto con el mensaje "La presion
     * atmosferica debe ser un valor mayor a 0.0"
     */
    if (modifiedClimateRecord.getAtmosphericPressure() <= 0.0) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.INVALID_ATMOSPHERIC_PRESSURE))).build();
    }

    /*
     * Si la evapotranspiracion del cultivo de referencia (ETo)
     * tiene un valor menor a 0.0, la aplicacion del lado
     * servidor retorna el mensaje HTTP 400 (Bad request)
     * junto con el mensaje "La evapotranspiracion del cultivo
     * de referencia (ETo) debe ser un valor mayor o igual
     * a 0.0" y no se realiza la operacion solicitada
     */
    if (modifiedClimateRecord.getEto() < 0.0) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.INVALID_ETO))).build();
    }

    /*
     * Si la evapotranspiracion del cultivo (ETc) tiene un
     * valor menor a 0.0, la aplicacion del lado servidor
     * retorna el mensaje HTTP 400 (Bad request) junto
     * con el mensaje "La evapotranspiracion del cultivo
     * (ETc) debe ser un valor mayor o igual a 0.0" y no
     * se realiza la operacion solicitada
     */
    if (modifiedClimateRecord.getEtc() < 0.0) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.INVALID_ETC))).build();
    }

    /*
     * Si la parcela NO esta definida, la aplicacion del
     * lado servidor retorna el mensaje HTTP 400 (Bad
     * request) junto con el mensaje "La parcela debe
     * estar definida" y no se realiza la operacion
     * solicitada
     */
    if (modifiedClimateRecord.getParcel() == null) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.INDEFINITE_PARCEL))).build();
    }

    /*
     * Si el registro climatico a modificar de una parcela,
     * tiene una fecha igual a la fecha de otro registro
     * climatico de la misma parcela, la aplicacion del
     * lado servidor retorna el mensaje HTTP 400 (Bad
     * request) junto con el mensaje "Ya existe un registro
     * climatico con la fecha ingresada para la parcela
     * seleccionada" y no se realiza la operacion solicitada
     */
    if (climateRecordService.checkRepeated(climateRecordId, modifiedClimateRecord.getParcel(), modifiedClimateRecord.getDate())) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.EXISTING_CLIMATE_RECORD))).build();
    }

    /*
     * Si el valor del encabezado de autorizacion de la peticion HTTP
     * dada, tiene un JWT valido, la aplicacion del lado servidor
     * devuelve el mensaje HTTP 200 (Ok) junto con los datos que el
     * cliente solicito actualizar
     */
    return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(climateRecordService.modify(userId, climateRecordId, modifiedClimateRecord))).build();
  }

  /**
   * Este metodo REST esta para probar que la invocacion a la API
   * climatica Visual Crossing Weather funciona como corresponde
   * 
   * @param latitude
   * @param longitude
   * @param time
   * @return conjunto de datos meteorologicos obtenidos en un dia
   *         para una ubicacion geografica
   * @throws IOException
   */
  @GET
  @Path("/apiCallTest")
  @Produces(MediaType.APPLICATION_JSON)
	public String getForecast(@QueryParam("latitude") double latitude, @QueryParam("longitude") double longitude, @QueryParam("time") long time) throws IOException {
    Parcel givenParcel = new Parcel();
    givenParcel.setLatitude(latitude);
    givenParcel.setLongitude(longitude);
    return mapper.writeValueAsString(ClimateClient.getForecast(givenParcel, time));
	}

}
