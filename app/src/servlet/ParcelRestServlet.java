package servlet;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Calendar;
import java.util.Collection;
import javax.ejb.EJB;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
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
import model.Parcel;
import stateless.ParcelServiceBean;
import stateless.SecretKeyServiceBean;
import stateless.UserServiceBean;
import stateless.PlantingRecordServiceBean;
import util.ErrorResponse;
import util.ReasonError;
import util.RequestManager;
import utilJwt.AuthHeaderManager;
import utilJwt.JwtManager;

@Path("/parcels")
public class ParcelRestServlet {

  // inject a reference to the ParcelServiceBean slsb
  @EJB ParcelServiceBean parcelService;
  @EJB SecretKeyServiceBean secretKeyService;
  @EJB UserServiceBean userService;
  @EJB PlantingRecordServiceBean plantingRecordService;

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
    return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(parcelService.findAll(userId))).build();
  }

  @GET
  @Path("/actives")
  @Produces(MediaType.APPLICATION_JSON)
  public Response findAllActive(@Context HttpHeaders request) throws IOException {
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
    return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(parcelService.findAllActive(userId))).build();
  }

  // Esto es necesario para la busqueda que se hace cuando se ingresan caracteres
  @GET
  @Path("/findByName")
  public Response findByName(@Context HttpHeaders request, @QueryParam("parcelName") String parcelName) throws IOException {
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
    return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(parcelService.findByName(userId, parcelName))).build();
  }

  @GET
  @Path("/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response find(@Context HttpHeaders request, @PathParam("id") int parcelId) throws IOException {
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
    if (!parcelService.checkExistence(parcelId)) {
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
    if (!parcelService.checkUserOwnership(userId, parcelId)) {
      return Response.status(Response.Status.FORBIDDEN).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.UNAUTHORIZED_ACCESS))).build();
    }

    /*
     * Si el valor del encabezado de autorizacion de la peticion HTTP
     * dada, tiene un JWT valido, la aplicacion del lado servidor
     * devuelve el mensaje HTTP 200 (Ok) junto con los datos solicitados
     * por el cliente
     */
    return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(parcelService.find(userId, parcelId))).build();
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
     * significa que el usuario intento registrar una
     * parcela con los campos vacios del formulario de
     * registro. Por lo tanto, la aplicacion del lado
     * servidor retorna el mensaje HTTP 400 (Bad request)
     * junto con el mensaje "Debe completar todos los
     * campos del formulario" y no se realiza la operacion
     * solicitada
     */
    if (json.isEmpty()) {
      return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorResponse(ReasonError.EMPTY_FORM)).build();
    }

    Parcel newParcel = mapper.readValue(json, Parcel.class);

    /*
     * Si el nombre de la nueva parcela no esta definido, la
     * aplicacion del lado servidor retorna el mensaje HTTP
     * 400 (Bad request) junto con el mensaje "El nombre de
     * la parcela debe estar definido" y no se realiza la
     * operacion solicitada
     */
    if (newParcel.getName() == null) {
      return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorResponse(ReasonError.PARCEL_NAME_UNDEFINED)).build();
    }

    /*
     * Si el nombre de la nueva parcela NO empieza con caracteres
     * alfabeticos, seguido de un espacio en blanco entre palabra
     * y palabra en el caso en el que el nombre este formado por
     * mas de una palabra, la aplicacion del lado servidor retorna
     * el mensaje HTTP 400 (Bad request) junto con el mensaje "El
     * nombre de una parcela debe empezar con una palabra formada
     * unicamente por caracteres alfabeticos y puede tener mas de
     * una palabra formada por caracteres alfanuméricos" y no se
     * realiza la operacion solicitada
     */
    if (!newParcel.getName().matches("^[A-Za-zÀ-ÿ]+(\\s[A-Za-zÀ-ÿ]*[0-9]*[A-Za-zÀ-ÿ]*)*$")) {
      return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorResponse(ReasonError.INVALID_PARCEL_NAME)).build();
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
     * Si dentro del conjunto de parcelas del usuario hay una
     * parcela con el nombre de la nueva parcela, la aplicacion
     * del lado servidor retorna el mensaje HTTP 400 (Bad request)
     * junto con el mensaje "Nombre de parcela ya utilizado, elija
     * otro" y no se realiza la operacion solicitada
     */
    if (parcelService.checkExistence(userId, newParcel.getName())) {
      return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorResponse(ReasonError.PARCEL_NAME_ALREADY_USED)).build();
    }

    /*
     * Si la cantidad de hectareas de la nueva parcela es menor
     * o igual a 0.0, la aplicacion del lado servidor retorna el
     * mensaje HTTP 400 (Bad request) junto con el mensaje "La
     * cantidad de hectareas debe ser mayor a 0.0" y no se realiza
     * la operacion solicitada
     */
    if (newParcel.getHectares() <= 0.0) {
      return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorResponse(ReasonError.INVALID_NUMBER_OF_HECTARES)).build();
    }

    /*
     * Si el valor del encabezado de autorizacion de la peticion HTTP
     * dada, tiene un JWT valido, la aplicacion del lado servidor
     * devuelve el mensaje HTTP 200 (Ok) junto con los datos que el
     * cliente solicito persistir
     */
    newParcel.setUser(userService.find(userId));
    return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(parcelService.create(newParcel))).build();
  }

  @DELETE
  @Path("/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response remove(@Context HttpHeaders request, @PathParam("id") int parcelId) throws IOException {
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
    if (!parcelService.checkExistence(parcelId)) {
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
    if (!parcelService.checkUserOwnership(userId, parcelId)) {
      return Response.status(Response.Status.FORBIDDEN).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.UNAUTHORIZED_ACCESS))).build();
    }

    /*
     * Si la parcela que se quiere eliminar (logicamente)
     * tiene un registro de plantacion en desarrollo, la
     * aplicacion del lado servidor retorna el mensaje HTTP
     * 400 (Bad request) junto con el mensaje "No esta permitido
     * eliminar (logicamente) una parcela que tiene un registro
     * de plantacion en desarrollo" y no se realiza la
     * operacion solicitada
     */
    if (plantingRecordService.checkOneInDevelopment(parcelService.find(parcelId))) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(
          new ErrorResponse(ReasonError.DELETION_PARCEL_WITH_PLANTING_RECORD_IN_DEVELOPMENT_NOT_ALLOWED))).build();
    }

    /*
     * Si el valor del encabezado de autorizacion de la peticion HTTP
     * dada, tiene un JWT valido, la aplicacion del lado servidor
     * devuelve el mensaje HTTP 200 (Ok) junto con los datos que el
     * cliente solicito eliminar
     */
    return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(parcelService.remove(userId, parcelId))).build();
  }

  @PUT
  @Path("/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response modify(@Context HttpHeaders request, @PathParam("id") int parcelId, String json) throws IOException {
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
    if (!parcelService.checkExistence(parcelId)) {
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
    if (!parcelService.checkUserOwnership(userId, parcelId)) {
      return Response.status(Response.Status.FORBIDDEN).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.UNAUTHORIZED_ACCESS))).build();
    }

    /*
     * Si el objeto de tipo String referenciado por la
     * referencia contenida en la variable de tipo por
     * referencia json de tipo String, esta vacio,
     * significa que el usuario intento modificar una
     * parcela con datos indefinidos. Por lo tanto, la
     * aplicacion del lado servidor retorna el mensaje
     * HTTP 400 (Bad request) junto con el mensaje "Debe
     * completar todos los campos del formulario" y no
     * se realiza la operacion solicitada
     */
    if (json.isEmpty()) {
      return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorResponse(ReasonError.EMPTY_FORM)).build();
    }

    Parcel modifiedParcel = mapper.readValue(json, Parcel.class);

    /*
     * Si el nombre de la parcela a modificar no esta definido,
     * la aplicacion del lado servidor retorna el mensaje HTTP
     * 400 (Bad request) junto con el mensaje "El nombre de
     * la parcela debe estar definido" y no se realiza la
     * operacion solicitada
     */
    if (modifiedParcel.getName() == null) {
      return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorResponse(ReasonError.PARCEL_NAME_UNDEFINED)).build();
    }

    /*
     * Si el nombre de la parcela a modificar NO empieza con
     * una cadena formada unicamente por caracteres alfabeticos
     * seguida por mas de una cadena formada por caracteres alfanumericos
     * en caso de que el nombre este formado por mas de una palabra,
     * la aplicacion del lado servidor retorna el mensaje HTTP 400
     * (Bad request) junto con el mensaje "El nombre de una parcela
     * debe empezar con una palabra formada unicamente por caracteres
     * alfabeticos y puede tener mas de una palabra formada por caracteres
     * alfanuméricos" y no se realiza la operacion solicitada
     */
    if (!modifiedParcel.getName().matches("^[A-Za-zÀ-ÿ]+(\\s[A-Za-zÀ-ÿ]*[0-9]*[A-Za-zÀ-ÿ]*)*$")) {
      return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorResponse(ReasonError.INVALID_PARCEL_NAME)).build();
    }

    /*
     * Si dentro del conjunto de parcelas del usuario hay una
     * parcela que tiene el nombre de la parcela a modificar, la
     * aplicacion del lado servidor retorna el mensaje HTTP 400
     * (Bad request) junto con el mensaje "Nombre de parcela ya
     * utilizado, elija otro" y no se realiza la operacion
     * solicitada
     */
    if (parcelService.checkRepeated(userId, parcelId, modifiedParcel.getName())) {
      return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorResponse(ReasonError.PARCEL_NAME_ALREADY_USED)).build();
    }

    /*
     * Si la cantidad de hectareas de la parcela a modificar es
     * menor o igual a 0.0, la aplicacion del lado servidor retorna
     * el mensaje HTTP 400 (Bad request) junto con el mensaje "La
     * cantidad de hectareas debe ser mayor a 0.0" y no se realiza
     * la operacion solicitada
     */
    if (modifiedParcel.getHectares() <= 0.0) {
      return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorResponse(ReasonError.INVALID_NUMBER_OF_HECTARES)).build();
    }

    /*
     * Si el valor del encabezado de autorizacion de la peticion HTTP
     * dada, tiene un JWT valido, y se pasan todos los controles para
     * la modificacion del dato correspondiente a este bloque de codigo
     * de esta API REST, la aplicacion del lado servidor devuelve el
     * mensaje HTTP 200 (Ok) junto con el dato que el cliente solicito
     * modificar
     */
    return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(parcelService.modify(userId, parcelId, modifiedParcel))).build();
  }

}
