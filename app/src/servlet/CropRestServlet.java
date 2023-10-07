package servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
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
import model.Crop;
import stateless.CropServiceBean;
import stateless.SecretKeyServiceBean;
import util.ErrorResponse;
import util.PersonalizedResponse;
import util.ReasonError;
import util.RequestManager;
import utilJwt.AuthHeaderManager;
import utilJwt.JwtManager;

@Path("/crops")
public class CropRestServlet {

  // inject a reference to the CropServiceBean slsb
  @EJB CropServiceBean cropService;
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
     * Si el valor del encabezado de autorizacion de la peticion HTTP
     * dada, tiene un JWT valido, la aplicacion del lado servidor
     * devuelve el mensaje HTTP 200 (Ok) junto con los datos solicitados
     * por el cliente
     */
    return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(cropService.findAll())).build();
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
     * Si el valor del encabezado de autorizacion de la peticion HTTP
     * dada, tiene un JWT valido, la aplicacion del lado servidor
     * devuelve el mensaje HTTP 200 (Ok) junto con los datos solicitados
     * por el cliente
     */
    return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(cropService.findAllActive())).build();
  }

  // Esto es necesario para la busqueda que se hace cuando se ingresan caracteres
  @GET
  @Path("/findByName")
  public Response findByName(@Context HttpHeaders request, @QueryParam("cropName") String cropName) throws IOException {
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
    return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(cropService.findByNameTypeAhead(cropName))).build();
  }

  @GET
  @Path("/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response find(@Context HttpHeaders request, @PathParam("id") int id) throws IOException {
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
    if (!cropService.checkExistence(id)) {
      return Response.status(Response.Status.NOT_FOUND).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.RESOURCE_NOT_FOUND))).build();
    }

    /*
     * Obtiene el JWT del valor del encabezado de autorizacion
     * de una peticion HTTP
     */
    String jwt = AuthHeaderManager.getJwt(AuthHeaderManager.getAuthHeaderValue(request));

    /*
     * Si el valor del encabezado de autorizacion de la peticion HTTP
     * dada, tiene un JWT valido, la aplicacion del lado servidor
     * devuelve el mensaje HTTP 200 (Ok) junto con los datos solicitados
     * por el cliente
     */
    return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(cropService.find(id))).build();
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
     * Obtiene el JWT del valor del encabezado de autorizacion
     * de una peticion HTTP
     */
    String jwt = AuthHeaderManager.getJwt(AuthHeaderManager.getAuthHeaderValue(request));

    /*
     * Si el usuario que solicita esta operacion no tiene el permiso de
     * administrador (superuser), la aplicacion del lado servidor devuelve
     * el mensaje HTTP 403 (Forbidden) junto con el mensaje "Acceso no
     * autorizado" (esta contenido en el enum ReasonError) y no se realiza
     * la operacion solicitada
     */
    if (!JwtManager.getSuperuser(jwt, secretKeyService.find().getValue())) {
      return Response.status(Response.Status.FORBIDDEN).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.UNAUTHORIZED_ACCESS))).build();
    }

    /*
     * ************************************************
     * Control sobre el llenado del formulario del dato
     * correspondiente a este clase
     * ************************************************
     */

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

    Crop newCrop = mapper.readValue(json, Crop.class);

    /*
     * Si el nombre del cultivo a crear NO esta definido, la
     * aplicacion del lado servidor retorna el mensaje HTTP
     * 400 (Bad request) junto con el mensaje "El nombre del
     * cultivo debe estar definido" y no se realiza la
     * operacion solicitada
     */
    if (newCrop.getName() == null) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.UNDEFINED_CROP_NAME))).build();
    }

    /*
     * Si el nombre del cultivo NO contiene unicamente letras, y
     * un espacio en blanco entre palabra y palabra si esta formado
     * por mas de una palabra, la aplicacion del lado servidor retorna
     * el mensaje HTTP 400 (Bad request) junto con el mensaje "El nombre
     * de un cultivo debe empezar con una palabra formada unicamente por
     * caracteres alfabeticos y puede tener mas de una palabra formada
     * unicamente por caracteres alfabeticos" y no se realiza la
     * operacion solicitada
     */
    if (!newCrop.getName().matches("^[A-Za-zÀ-ÿ]+(\\s[A-Za-zÀ-ÿ]+)*$")) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.INVALID_CROP_NAME))).build();
    }

    /*
     * Si en la base de datos subyacente existe un cultivo con el
     * nombre, el mes de inicio de siembra, el mes de fin de siembra
     * y la region del cultivo nuevo, la aplicacion del lado servidor
     * retorna el mensaje HTTP 400 (Bad request) junto con el mensaje
     * "Ya existe un cultivo con el nombre, el mes de inicio de siembra,
     * el mes de fin de siembra y la region elegidos" y no se realiza
     * la operacion solicitada
     */
    if (cropService.checkExistence(newCrop)) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.EXISTING_CROP))).build();
    }

    /*
     * Si el tipo del cultivo a crear NO esta definido, la
     * aplicacion del lado servidor retorna el mensaje HTTP
     * 400 (Bad request) junto con el mensaje "El tipo del
     * cultivo debe estar definido" y no se realiza la
     * operacion solicitada
     */
    if (newCrop.getTypeCrop() == null) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.TYPE_CROP_UNDEFINED))).build();
    }

    String message = null;

    /*
     * **********************************************
     * Controles sobre las etapas de vida del cultivo
     * **********************************************
     */

    /*
     * Si una de las etapas de vida del cultivo a crear tiene
     * un valor menor o igual a cero, la aplicacion del lado
     * servidor retorna el mensaje HTTP 400 (Bad request)
     * junto con el mensaje "El <nombre del campo de etapa
     * de vida> debe ser mayor a cero" y no se realiza la
     * operacion solicitada
     */
    if (newCrop.getInitialStage() <= 0) {
      message = "La etapa inicial debe ser mayor a cero";
      PersonalizedResponse personalizedResponse = new PersonalizedResponse(message);
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(personalizedResponse)).build();
    }

    if (newCrop.getDevelopmentStage() <= 0) {
      message = "La etapa de desarrollo debe ser mayor a cero";
      PersonalizedResponse personalizedResponse = new PersonalizedResponse(message);
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(personalizedResponse)).build();
    }

    if (newCrop.getMiddleStage() <= 0) {
      message = "La etapa media debe ser mayor a cero";
      PersonalizedResponse personalizedResponse = new PersonalizedResponse(message);
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(personalizedResponse)).build();
    }

    if (newCrop.getFinalStage() <= 0) {
      message = "La etapa final debe ser mayor a cero";
      PersonalizedResponse personalizedResponse = new PersonalizedResponse(message);
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(personalizedResponse)).build();
    }

    /*
     * ********************************************
     * Controles sobre los coeficientes del cultivo
     * ********************************************
     */

    /*
     * Si uno de los coeficientes del cultivo a crear tiene
     * un valor menor o igual a 0.0, la aplicacion del lado
     * servidor retorna el mensaje HTTP 400 (Bad request)
     * junto con el mensaje "El <nombre de campo de coeficiente
     * de cultivo> debe ser mayor a 0.0" y no se realiza
     * la operacion solicitada
     */
    if (newCrop.getInitialKc() <= 0.0) {
      message = "El coeficiente inicial del cultivo debe ser mayor a 0.0";
      PersonalizedResponse personalizedResponse = new PersonalizedResponse(message);
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(personalizedResponse)).build();      
    }

    if (newCrop.getMiddleKc() <= 0.0) {
      message = "El coeficiente medio del cultivo debe ser mayor a 0.0";
      PersonalizedResponse personalizedResponse = new PersonalizedResponse(message);
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(personalizedResponse)).build();      
    }

    if (newCrop.getFinalKc() <= 0.0) {
      message = "El coeficiente final del cultivo debe ser mayor a 0.0";
      PersonalizedResponse personalizedResponse = new PersonalizedResponse(message);
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(personalizedResponse)).build();      
    }

    /*
     * Si el valor del encabezado de autorizacion de la peticion HTTP
     * dada, tiene un JWT valido, la aplicacion del lado servidor
     * devuelve el mensaje HTTP 200 (Ok) junto con los datos que el
     * cliente solicito persistir
     */
    return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(cropService.create(newCrop))).build();
  }

  @DELETE
  @Path("/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response remove(@Context HttpHeaders request, @PathParam("id") int id) throws IOException {
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
    if (!cropService.checkExistence(id)) {
      return Response.status(Response.Status.NOT_FOUND).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.RESOURCE_NOT_FOUND))).build();
    }

    /*
     * Obtiene el JWT del valor del encabezado de autorizacion
     * de una peticion HTTP
     */
    String jwt = AuthHeaderManager.getJwt(AuthHeaderManager.getAuthHeaderValue(request));

    /*
     * Si el usuario que solicita esta operacion no tiene el permiso de
     * administrador (superuser), la aplicacion del lado servidor devuelve
     * el mensaje HTTP 403 (Forbidden) junto con el mensaje "Acceso no
     * autorizado" (esta contenido en el enum ReasonError) y no se realiza
     * la operacion solicitada
     */
    if (!JwtManager.getSuperuser(jwt, secretKeyService.find().getValue())) {
      return Response.status(Response.Status.FORBIDDEN).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.UNAUTHORIZED_ACCESS))).build();
    }

    /*
     * Si el valor del encabezado de autorizacion de la peticion HTTP
     * dada, tiene un JWT valido, la aplicacion del lado servidor
     * devuelve el mensaje HTTP 200 (Ok) junto con los datos que el
     * cliente solicito eliminar
     */
    return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(cropService.remove(id))).build();
  }

  @PUT
  @Path("/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response modify(@Context HttpHeaders request, @PathParam("id") int id, String json) throws IOException {
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
    if (!cropService.checkExistence(id)) {
      return Response.status(Response.Status.NOT_FOUND).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.RESOURCE_NOT_FOUND))).build();
    }

    /*
     * Obtiene el JWT del valor del encabezado de autorizacion
     * de una peticion HTTP
     */
    String jwt = AuthHeaderManager.getJwt(AuthHeaderManager.getAuthHeaderValue(request));

    /*
     * Si el usuario que solicita esta operacion no tiene el permiso de
     * administrador (superuser), la aplicacion del lado servidor devuelve
     * el mensaje HTTP 403 (Forbidden) junto con el mensaje "Acceso no
     * autorizado" (esta contenido en el enum ReasonError) y no se realiza
     * la operacion solicitada
     */
    if (!JwtManager.getSuperuser(jwt, secretKeyService.find().getValue())) {
      return Response.status(Response.Status.FORBIDDEN).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.UNAUTHORIZED_ACCESS))).build();
    }

    /*
     * ************************************************
     * Control sobre el llenado del formulario del dato
     * correspondiente a este clase
     * ************************************************
     */

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

    Crop modifiedCrop = mapper.readValue(json, Crop.class);

    /*
     * Si el nombre del cultivo a modificar NO esta definido, la
     * aplicacion del lado servidor retorna el mensaje HTTP
     * 400 (Bad request) junto con el mensaje "El nombre del
     * cultivo debe estar definido" y no se realiza la
     * operacion solicitada
     */
    if (modifiedCrop.getName() == null) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.UNDEFINED_CROP_NAME))).build();
    }

    /*
     * Si el nombre del cultivo NO contiene unicamente letras, y
     * un espacio en blanco entre palabra y palabra si esta formado
     * por mas de una palabra, la aplicacion del lado servidor retorna
     * el mensaje HTTP 400 (Bad request) junto con el mensaje "El nombre
     * de un cultivo debe empezar con una palabra formada unicamente por
     * caracteres alfabeticos y puede tener mas de una palabra formada
     * unicamente por caracteres alfabeticos" y no se realiza la
     * operacion solicitada
     */
    if (!modifiedCrop.getName().matches("^[A-Za-zÀ-ÿ]+(\\s[A-Za-zÀ-ÿ]+)*$")) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.INVALID_CROP_NAME))).build();
    }

    /*
     * Si el cultivo modificado tiene un nombre, un mes de inicio
     * de siembra, un mes de fin de siembra y una region iguales
     * al nombre, al mes de inicio de siembra, al mes de fin de
     * siembra y a la region de otro cultivo, la aplicacion del
     * lado servidor retorna el mensaje HTTP 400 (Bad request)
     * junto con el mensaje "Ya existe un cultivo con el nombre,
     * el mes de inicio de siembra, el mes de fin de siembra y la
     * region elegidos" y no se realiza la operacion solicitada
     */
    if (cropService.checkRepeated(id, modifiedCrop)) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.EXISTING_CROP))).build();
    }

    /*
     * Si el tipo del cultivo a modificar NO esta definido, la
     * aplicacion del lado servidor retorna el mensaje HTTP
     * 400 (Bad request) junto con el mensaje "El tipo del
     * cultivo debe estar definido" y no se realiza la
     * operacion solicitada
     */
    if (modifiedCrop.getTypeCrop() == null) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.TYPE_CROP_UNDEFINED))).build();
    }

    String message = null;

    /*
     * **********************************************
     * Controles sobre las etapas de vida del cultivo
     * **********************************************
     */

    /*
     * Si una de las etapas de vida del cultivo a modificar
     * tiene un valor menor o igual a cero, la aplicacion del
     * lado servidor retorna el mensaje HTTP 400 (Bad request)
     * junto con el mensaje "El <nombre del campo de etapa
     * de vida> debe ser mayor a cero" y no se realiza la
     * operacion solicitada
     */
    if (modifiedCrop.getInitialStage() <= 0) {
      message = "La etapa inicial debe ser mayor a cero";
      PersonalizedResponse personalizedResponse = new PersonalizedResponse(message);
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(personalizedResponse)).build();
    }

    if (modifiedCrop.getDevelopmentStage() <= 0) {
      message = "La etapa de desarrollo debe ser mayor a cero";
      PersonalizedResponse personalizedResponse = new PersonalizedResponse(message);
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(personalizedResponse)).build();
    }

    if (modifiedCrop.getMiddleStage() <= 0) {
      message = "La etapa media debe ser mayor a cero";
      PersonalizedResponse personalizedResponse = new PersonalizedResponse(message);
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(personalizedResponse)).build();
    }

    if (modifiedCrop.getFinalStage() <= 0) {
      message = "La etapa final debe ser mayor a cero";
      PersonalizedResponse personalizedResponse = new PersonalizedResponse(message);
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(personalizedResponse)).build();
    }

    /*
     * ********************************************
     * Controles sobre los coeficientes del cultivo
     * ********************************************
     */

    /*
     * Si uno de los coeficientes del cultivo a modificar
     * tiene un valor menor o igual a 0.0, la aplicacion del
     * lado servidor retorna el mensaje HTTP 400 (Bad request)
     * junto con el mensaje "El <nombre de campo de coeficiente
     * de cultivo> debe ser mayor a 0.0" y no se realiza
     * la operacion solicitada
     */
    if (modifiedCrop.getInitialKc() <= 0.0) {
      message = "El coeficiente inicial del cultivo debe ser mayor a 0.0";
      PersonalizedResponse personalizedResponse = new PersonalizedResponse(message);
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(personalizedResponse)).build();      
    }

    if (modifiedCrop.getMiddleKc() <= 0.0) {
      message = "El coeficiente medio del cultivo debe ser mayor a 0.0";
      PersonalizedResponse personalizedResponse = new PersonalizedResponse(message);
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(personalizedResponse)).build();      
    }

    if (modifiedCrop.getFinalKc() <= 0.0) {
      message = "El coeficiente final del cultivo debe ser mayor a 0.0";
      PersonalizedResponse personalizedResponse = new PersonalizedResponse(message);
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(personalizedResponse)).build();      
    }

    /*
     * Si el valor del encabezado de autorizacion de la peticion HTTP
     * dada, tiene un JWT valido, la aplicacion del lado servidor
     * devuelve el mensaje HTTP 200 (Ok) junto con los datos que el
     * cliente solicito actualizar
     */
    return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(cropService.modify(id, modifiedCrop))).build();
  }

}
