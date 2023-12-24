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
import stateless.SessionServiceBean;
import util.ErrorResponse;
import util.SourceUnsatisfiedResponse;
import util.ReasonError;
import util.RequestManager;
import utilJwt.AuthHeaderManager;
import utilJwt.JwtManager;

@Path("/crops")
public class CropRestServlet {

  // inject a reference to the CropServiceBean slsb
  @EJB CropServiceBean cropService;
  @EJB SecretKeyServiceBean secretKeyService;
  @EJB SessionServiceBean sessionService;

  // Mapea lista de pojo a JSON
  ObjectMapper mapper = new ObjectMapper();

  private final String UNDEFINED_VALUE = "undefined";

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
     * Valor de la clave secreta con la que la aplicacion firma
     * un JWT
     */
    String secretKeyValue = secretKeyService.find().getValue();

    /*
     * Obtiene el ID de usuario contenido en la carga util del
     * JWT del encabezado de autorizacion de una peticion HTTP
     */
    int userId = JwtManager.getUserId(jwt, secretKeyValue);

    /*
     * Si el usuario que solicita esta operacion NO tiene una
     * sesion activa, la aplicacion del lador servidor devuelve
     * el mensaje 401 (Unauthorized) junto con el mensaje "No
     * tiene una sesion activa" y no se realiza la operacion
     * solicitada
     */
    if (!sessionService.checkActiveSession(userId)) {
      return Response.status(Response.Status.UNAUTHORIZED).entity(new ErrorResponse(ReasonError.NO_ACTIVE_SESSION)).build();
    }

    /*
     * Si la fecha de emision del JWT de un usuario NO es igual
     * a la fecha de emision de la sesion activa del usuario,
     * la aplicacion del lado servidor retorna el mensaje HTTP
     * 400 (Bad request) junto con el mensaje "El JWT no
     * corresponde a una sesión abierta" y no se realiza la
     * operacion solicitada.
     * 
     * Se debe tener en cuenta que el metodo checkDateIssueLastSession
     * de la clase SessionServiceBean debe ser invocado luego
     * de invocar el metodo checkActiveSession de la misma
     * clase, ya que de lo contrario se puede comparar la
     * fecha de emision de un JWT con una sesion inactiva,
     * lo cual es incorrecto porque la fecha de emision de un
     * JWT se debe comparar con la fecha de emision de una
     * sesion activa. El motivo por el cual puede ocurrir esta
     * comparacion con una sesion inactiva es que el metodo
     * findLastSession recupera la ultima sesion del usuario,
     * independientemente de si esta activa o inactiva.
     * 
     * Este control se implementa para evitar que se puedan
     * recuperar datos mediante peticiones HTTP haciendo uso
     * de un JWT valido, pero que es de una sesion que fue
     * cerrada por el usuario.
     */
    if (!sessionService.checkDateIssueLastSession(userId, JwtManager.getDateIssue(jwt, secretKeyValue))) {
      return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorResponse(ReasonError.JWT_NOT_ASSOCIATED_WITH_ACTIVE_SESSION)).build();
    }

    /*
     * Si el valor del encabezado de autorizacion de la peticion HTTP
     * dada, tiene un JWT valido, la aplicacion del lado servidor
     * devuelve el mensaje HTTP 200 (Ok) junto con los datos solicitados
     * por el cliente
     */
    return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(cropService.findAll())).build();
  }

  @GET
  @Path("/search")
  public Response search(@Context HttpHeaders request, @QueryParam("cropName") String cropName) throws IOException {
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
     * Valor de la clave secreta con la que la aplicacion firma
     * un JWT
     */
    String secretKeyValue = secretKeyService.find().getValue();

    /*
     * Obtiene el ID de usuario contenido en la carga util del
     * JWT del encabezado de autorizacion de una peticion HTTP
     */
    int userId = JwtManager.getUserId(jwt, secretKeyValue);

    /*
     * Si el usuario que solicita esta operacion NO tiene una
     * sesion activa, la aplicacion del lador servidor devuelve
     * el mensaje 401 (Unauthorized) junto con el mensaje "No
     * tiene una sesion activa" y no se realiza la operacion
     * solicitada
     */
    if (!sessionService.checkActiveSession(userId)) {
      return Response.status(Response.Status.UNAUTHORIZED).entity(new ErrorResponse(ReasonError.NO_ACTIVE_SESSION)).build();
    }

    /*
     * Si la fecha de emision del JWT de un usuario NO es igual
     * a la fecha de emision de la sesion activa del usuario,
     * la aplicacion del lado servidor retorna el mensaje HTTP
     * 400 (Bad request) junto con el mensaje "El JWT no
     * corresponde a una sesión abierta" y no se realiza la
     * operacion solicitada.
     * 
     * Se debe tener en cuenta que el metodo checkDateIssueLastSession
     * de la clase SessionServiceBean debe ser invocado luego
     * de invocar el metodo checkActiveSession de la misma
     * clase, ya que de lo contrario se puede comparar la
     * fecha de emision de un JWT con una sesion inactiva,
     * lo cual es incorrecto porque la fecha de emision de un
     * JWT se debe comparar con la fecha de emision de una
     * sesion activa. El motivo por el cual puede ocurrir esta
     * comparacion con una sesion inactiva es que el metodo
     * findLastSession recupera la ultima sesion del usuario,
     * independientemente de si esta activa o inactiva.
     * 
     * Este control se implementa para evitar que se puedan
     * recuperar datos mediante peticiones HTTP haciendo uso
     * de un JWT valido, pero que es de una sesion que fue
     * cerrada por el usuario.
     */
    if (!sessionService.checkDateIssueLastSession(userId, JwtManager.getDateIssue(jwt, secretKeyValue))) {
      return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorResponse(ReasonError.JWT_NOT_ASSOCIATED_WITH_ACTIVE_SESSION)).build();
    }

    /*
     * Si el nombre del dato correspondiente a esta clase NO
     * esta definido, la aplicacion del lado servidor retorna
     * el mensaje HTTP 400 (Bad request) junto con el mensaje
     * "El nombre de <dato> debe estar definido" y no se realiza
     * la operacion solicitada
     */
    if (cropName == null || cropName.equals(UNDEFINED_VALUE)) {
      return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorResponse(ReasonError.UNDEFINED_CROP_NAME)).build();
    }

    /*
     * Si el dato solicitado no existe en la base de datos
     * subyacente, la aplicacion del lado servidor devuelve
     * el mensaje HTTP 404 (Not found) junto con el mensaje
     * "<dato> inexistente" y la fuente u origen en donde
     * NO se pudo satisfacer la solicitud, y no se realiza
     * la operacion solicitada
     */
    if (!cropService.checkExistence(cropName)) {
      return Response.status(Response.Status.NOT_FOUND).entity(new ErrorResponse(ReasonError.CROP_NOT_FOUND, SourceUnsatisfiedResponse.CROP)).build();
    }

    /*
     * Si el valor del encabezado de autorizacion de la peticion HTTP
     * dada, tiene un JWT valido, la aplicacion del lado servidor
     * devuelve el mensaje HTTP 200 (Ok) junto con los datos solicitados
     * por el cliente
     */
    return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(cropService.search(cropName))).build();
  }

  /*
   * Este metodo es necesario para el autocompletado de un campo
   * correspondiente a un cultivo activo o inactivo, como el campo
   * de cultivo de una pagina web de lista de datos que estan asociados
   * a un cultivo, como la pagina web de lista de balances hidricos,
   * por ejemplo.
   */
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
     * Valor de la clave secreta con la que la aplicacion firma
     * un JWT
     */
    String secretKeyValue = secretKeyService.find().getValue();

    /*
     * Obtiene el ID de usuario contenido en la carga util del
     * JWT del encabezado de autorizacion de una peticion HTTP
     */
    int userId = JwtManager.getUserId(jwt, secretKeyValue);

    /*
     * Si el usuario que solicita esta operacion NO tiene una
     * sesion activa, la aplicacion del lador servidor devuelve
     * el mensaje 401 (Unauthorized) junto con el mensaje "No
     * tiene una sesion activa" y no se realiza la operacion
     * solicitada
     */
    if (!sessionService.checkActiveSession(userId)) {
      return Response.status(Response.Status.UNAUTHORIZED).entity(new ErrorResponse(ReasonError.NO_ACTIVE_SESSION)).build();
    }

    /*
     * Si la fecha de emision del JWT de un usuario NO es igual
     * a la fecha de emision de la sesion activa del usuario,
     * la aplicacion del lado servidor retorna el mensaje HTTP
     * 400 (Bad request) junto con el mensaje "El JWT no
     * corresponde a una sesión abierta" y no se realiza la
     * operacion solicitada.
     * 
     * Se debe tener en cuenta que el metodo checkDateIssueLastSession
     * de la clase SessionServiceBean debe ser invocado luego
     * de invocar el metodo checkActiveSession de la misma
     * clase, ya que de lo contrario se puede comparar la
     * fecha de emision de un JWT con una sesion inactiva,
     * lo cual es incorrecto porque la fecha de emision de un
     * JWT se debe comparar con la fecha de emision de una
     * sesion activa. El motivo por el cual puede ocurrir esta
     * comparacion con una sesion inactiva es que el metodo
     * findLastSession recupera la ultima sesion del usuario,
     * independientemente de si esta activa o inactiva.
     * 
     * Este control se implementa para evitar que se puedan
     * recuperar datos mediante peticiones HTTP haciendo uso
     * de un JWT valido, pero que es de una sesion que fue
     * cerrada por el usuario.
     */
    if (!sessionService.checkDateIssueLastSession(userId, JwtManager.getDateIssue(jwt, secretKeyValue))) {
      return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorResponse(ReasonError.JWT_NOT_ASSOCIATED_WITH_ACTIVE_SESSION)).build();
    }

    /*
     * Si el nombre del dato correspondiente a esta clase NO
     * esta definido, la aplicacion del lado servidor retorna
     * el mensaje HTTP 400 (Bad request) junto con el mensaje
     * "El nombre de <dato> debe estar definido" y no se realiza
     * la operacion solicitada
     */
    if (cropName == null || cropName.equals(UNDEFINED_VALUE)) {
      return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorResponse(ReasonError.UNDEFINED_CROP_NAME)).build();
    }

    /*
     * Si el valor del encabezado de autorizacion de la peticion HTTP
     * dada, tiene un JWT valido, la aplicacion del lado servidor
     * devuelve el mensaje HTTP 200 (Ok) junto con los datos solicitados
     * por el cliente
     */
    return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(cropService.findByName(cropName))).build();
  }

  /*
   * Este metodo es necesario para el autocompletado de un campo
   * correspondiente a un cultivo activo, como el campo de cultivo
   * del formulario de creacion y modificacion de un registro de
   * plantacion, por ejemplo.
   */
  @GET
  @Path("/findActiveCropByName")
  public Response findActiveCropByName(@Context HttpHeaders request, @QueryParam("cropName") String cropName) throws IOException {
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
     * Valor de la clave secreta con la que la aplicacion firma
     * un JWT
     */
    String secretKeyValue = secretKeyService.find().getValue();

    /*
     * Obtiene el ID de usuario contenido en la carga util del
     * JWT del encabezado de autorizacion de una peticion HTTP
     */
    int userId = JwtManager.getUserId(jwt, secretKeyValue);
    /*
     * Si el usuario que solicita esta operacion NO tiene una
     * sesion activa, la aplicacion del lador servidor devuelve
     * el mensaje 401 (Unauthorized) junto con el mensaje "No
     * tiene una sesion activa" y no se realiza la operacion
     * solicitada
     */
    if (!sessionService.checkActiveSession(userId)) {
      return Response.status(Response.Status.UNAUTHORIZED).entity(new ErrorResponse(ReasonError.NO_ACTIVE_SESSION)).build();
    }

    /*
     * Si la fecha de emision del JWT de un usuario NO es igual
     * a la fecha de emision de la sesion activa del usuario,
     * la aplicacion del lado servidor retorna el mensaje HTTP
     * 400 (Bad request) junto con el mensaje "El JWT no
     * corresponde a una sesión abierta" y no se realiza la
     * operacion solicitada.
     * 
     * Se debe tener en cuenta que el metodo checkDateIssueLastSession
     * de la clase SessionServiceBean debe ser invocado luego
     * de invocar el metodo checkActiveSession de la misma
     * clase, ya que de lo contrario se puede comparar la
     * fecha de emision de un JWT con una sesion inactiva,
     * lo cual es incorrecto porque la fecha de emision de un
     * JWT se debe comparar con la fecha de emision de una
     * sesion activa. El motivo por el cual puede ocurrir esta
     * comparacion con una sesion inactiva es que el metodo
     * findLastSession recupera la ultima sesion del usuario,
     * independientemente de si esta activa o inactiva.
     * 
     * Este control se implementa para evitar que se puedan
     * recuperar datos mediante peticiones HTTP haciendo uso
     * de un JWT valido, pero que es de una sesion que fue
     * cerrada por el usuario.
     */
    if (!sessionService.checkDateIssueLastSession(userId, JwtManager.getDateIssue(jwt, secretKeyValue))) {
      return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorResponse(ReasonError.JWT_NOT_ASSOCIATED_WITH_ACTIVE_SESSION)).build();
    }

    /*
     * Si el valor del encabezado de autorizacion de la peticion HTTP
     * dada, tiene un JWT valido, la aplicacion del lado servidor
     * devuelve el mensaje HTTP 200 (Ok) junto con los datos solicitados
     * por el cliente
     */
    return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(cropService.findActiveCropByName(cropName))).build();
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
     * Obtiene el JWT del valor del encabezado de autorizacion
     * de una peticion HTTP
     */
    String jwt = AuthHeaderManager.getJwt(AuthHeaderManager.getAuthHeaderValue(request));

    /*
     * Valor de la clave secreta con la que la aplicacion firma
     * un JWT
     */
    String secretKeyValue = secretKeyService.find().getValue();

    /*
     * Obtiene el ID de usuario contenido en la carga util del
     * JWT del encabezado de autorizacion de una peticion HTTP
     */
    int userId = JwtManager.getUserId(jwt, secretKeyValue);

    /*
     * Si el usuario que solicita esta operacion NO tiene una
     * sesion activa, la aplicacion del lador servidor devuelve
     * el mensaje 401 (Unauthorized) junto con el mensaje "No
     * tiene una sesion activa" y no se realiza la operacion
     * solicitada
     */
    if (!sessionService.checkActiveSession(userId)) {
      return Response.status(Response.Status.UNAUTHORIZED).entity(new ErrorResponse(ReasonError.NO_ACTIVE_SESSION)).build();
    }

    /*
     * Si la fecha de emision del JWT de un usuario NO es igual
     * a la fecha de emision de la sesion activa del usuario,
     * la aplicacion del lado servidor retorna el mensaje HTTP
     * 400 (Bad request) junto con el mensaje "El JWT no
     * corresponde a una sesión abierta" y no se realiza la
     * operacion solicitada.
     * 
     * Se debe tener en cuenta que el metodo checkDateIssueLastSession
     * de la clase SessionServiceBean debe ser invocado luego
     * de invocar el metodo checkActiveSession de la misma
     * clase, ya que de lo contrario se puede comparar la
     * fecha de emision de un JWT con una sesion inactiva,
     * lo cual es incorrecto porque la fecha de emision de un
     * JWT se debe comparar con la fecha de emision de una
     * sesion activa. El motivo por el cual puede ocurrir esta
     * comparacion con una sesion inactiva es que el metodo
     * findLastSession recupera la ultima sesion del usuario,
     * independientemente de si esta activa o inactiva.
     * 
     * Este control se implementa para evitar que se puedan
     * recuperar datos mediante peticiones HTTP haciendo uso
     * de un JWT valido, pero que es de una sesion que fue
     * cerrada por el usuario.
     */
    if (!sessionService.checkDateIssueLastSession(userId, JwtManager.getDateIssue(jwt, secretKeyValue))) {
      return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorResponse(ReasonError.JWT_NOT_ASSOCIATED_WITH_ACTIVE_SESSION)).build();
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
     * Valor de la clave secreta con la que la aplicacion firma
     * un JWT
     */
    String secretKeyValue = secretKeyService.find().getValue();

    /*
     * Obtiene el ID de usuario contenido en la carga util del
     * JWT del encabezado de autorizacion de una peticion HTTP
     */
    int userId = JwtManager.getUserId(jwt, secretKeyValue);

    /*
     * Si el usuario que solicita esta operacion NO tiene una
     * sesion activa, la aplicacion del lador servidor devuelve
     * el mensaje 401 (Unauthorized) junto con el mensaje "No
     * tiene una sesion activa" y no se realiza la operacion
     * solicitada
     */
    if (!sessionService.checkActiveSession(userId)) {
      return Response.status(Response.Status.UNAUTHORIZED).entity(new ErrorResponse(ReasonError.NO_ACTIVE_SESSION)).build();
    }

    /*
     * Si la fecha de emision del JWT de un usuario NO es igual
     * a la fecha de emision de la sesion activa del usuario,
     * la aplicacion del lado servidor retorna el mensaje HTTP
     * 400 (Bad request) junto con el mensaje "El JWT no
     * corresponde a una sesión abierta" y no se realiza la
     * operacion solicitada.
     * 
     * Se debe tener en cuenta que el metodo checkDateIssueLastSession
     * de la clase SessionServiceBean debe ser invocado luego
     * de invocar el metodo checkActiveSession de la misma
     * clase, ya que de lo contrario se puede comparar la
     * fecha de emision de un JWT con una sesion inactiva,
     * lo cual es incorrecto porque la fecha de emision de un
     * JWT se debe comparar con la fecha de emision de una
     * sesion activa. El motivo por el cual puede ocurrir esta
     * comparacion con una sesion inactiva es que el metodo
     * findLastSession recupera la ultima sesion del usuario,
     * independientemente de si esta activa o inactiva.
     * 
     * Este control se implementa para evitar que se puedan
     * recuperar datos mediante peticiones HTTP haciendo uso
     * de un JWT valido, pero que es de una sesion que fue
     * cerrada por el usuario.
     */
    if (!sessionService.checkDateIssueLastSession(userId, JwtManager.getDateIssue(jwt, secretKeyValue))) {
      return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorResponse(ReasonError.JWT_NOT_ASSOCIATED_WITH_ACTIVE_SESSION)).build();
    }

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
     * junto con el mensaje "Debe proporcionar todos los datos
     * requeridos" y no se realiza la operacion solicitada
     */
    if (json.isEmpty()) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.EMPTY_DATA))).build();
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
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.INVALID_INITIAL_STAGE))).build();
    }

    if (newCrop.getDevelopmentStage() <= 0) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.INVALID_DEVELOPMENT_STAGE))).build();
    }

    if (newCrop.getMiddleStage() <= 0) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.INVALID_MIDDLE_STAGE))).build();
    }

    if (newCrop.getFinalStage() <= 0) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.INVALID_FINAL_STAGE))).build();
    }

    /*
     * El ciclo de vida (dias) de un cultivo es la sumatoria
     * de su etapa inicial, su etapa de desarrollo, su etapa
     * media y su etapa final, las cuales se miden en dias
     */
    newCrop.setLifeCycle(cropService.calculateLifeCycle(newCrop));

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
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.INVALID_INITIAL_KC))).build();      
    }

    if (newCrop.getMiddleKc() <= 0.0) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.INVALID_MIDDLE_KC))).build();      
    }

    if (newCrop.getFinalKc() <= 0.0) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.INVALID_FINAL_KC))).build();      
    }

    /*
     * ***************************************************
     * Controles sobre el limite minimo y el limite maximo
     * de la profundidad radicular
     * ***************************************************
     */

    /*
     * Si uno de los limites de la profundidad radicular maxima
     * del cultivo a crear tiene un valor menor o igual a 0.0, la
     * aplicacion del lado servidor retorna el mensaje HTTP 400
     * (Bad request) junto con el mensaje "El <nombre del limite>
     * debe ser mayor a 0.0" y no se realiza la operacion solicitada
     */
    if (newCrop.getLowerLimitMaximumRootDepth() <= 0.0) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.LOWER_LIMIT_MAXIMUM_ROOT_DEPTH_INVALID))).build();      
    }

    if (newCrop.getUpperLimitMaximumRootDepth() <= 0.0) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.UPPER_LIMIT_MAXIMUM_ROOT_DEPTH_INVALID))).build();      
    }

    /*
     * Si el limite inferior de la profundidad radicular maxima
     * de un cultivo es mayor o igual al limite superior de la
     * profundidad radicular maxima de un cultivo, la aplicacion
     * del lado servidor retorna el mensaje HTTP 400 (Bad request)
     * junto con el mensaje "El limite inferior de la profundidad
     * radicular maxima no debe ser mayor o igual al limite superior
     * de la profundidad radicular maxima" y no se realiza la
     * operacion solicitada
     */
    if (newCrop.getLowerLimitMaximumRootDepth() >= newCrop.getUpperLimitMaximumRootDepth()) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.OVERLAPPING_ROOT_DEPTH_LIMITS))).build();      
    }

    /*
     * **************************************************
     * Controles sobre el valor del factor de agotamiento
     * **************************************************
     */

    /*
     * Si el factor de agotamiento (p) es menor al limite inferior
     * (0.1) o mayor al limite superior (0.8), la aplicacion del
     * lado servidor retorna el mensaje HTTP 400 (Bad request)
     * junto con el mensaje "El factor de agotamiento debe tener
     * un valor entre 0.1 y 0.8" y no se realiza la operacion
     * solicitada.
     * 
     * ¿De donde proviene esta limitacion del factor de agotamiento
     * (p)? La establece el libro "Evapotranspiracion del cultivo,
     * estudio FAO riego y drenaje 56" en el primer parrafo de la
     * pagina 163.
     */
    if (newCrop.getDepletionFactor() < cropService.getLowerLimitDepletionFactor()
        || newCrop.getDepletionFactor() > cropService.getUpperLimitDepletionFactor()) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.INVALID_DEPLETION_FACTOR))).build();
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
     * Obtiene el JWT del valor del encabezado de autorizacion
     * de una peticion HTTP
     */
    String jwt = AuthHeaderManager.getJwt(AuthHeaderManager.getAuthHeaderValue(request));

    /*
     * Valor de la clave secreta con la que la aplicacion firma
     * un JWT
     */
    String secretKeyValue = secretKeyService.find().getValue();

    /*
     * Obtiene el ID de usuario contenido en la carga util del
     * JWT del encabezado de autorizacion de una peticion HTTP
     */
    int userId = JwtManager.getUserId(jwt, secretKeyValue);

    /*
     * Si el usuario que solicita esta operacion NO tiene una
     * sesion activa, la aplicacion del lador servidor devuelve
     * el mensaje 401 (Unauthorized) junto con el mensaje "No
     * tiene una sesion activa" y no se realiza la operacion
     * solicitada
     */
    if (!sessionService.checkActiveSession(userId)) {
      return Response.status(Response.Status.UNAUTHORIZED).entity(new ErrorResponse(ReasonError.NO_ACTIVE_SESSION)).build();
    }

    /*
     * Si la fecha de emision del JWT de un usuario NO es igual
     * a la fecha de emision de la sesion activa del usuario,
     * la aplicacion del lado servidor retorna el mensaje HTTP
     * 400 (Bad request) junto con el mensaje "El JWT no
     * corresponde a una sesión abierta" y no se realiza la
     * operacion solicitada.
     * 
     * Se debe tener en cuenta que el metodo checkDateIssueLastSession
     * de la clase SessionServiceBean debe ser invocado luego
     * de invocar el metodo checkActiveSession de la misma
     * clase, ya que de lo contrario se puede comparar la
     * fecha de emision de un JWT con una sesion inactiva,
     * lo cual es incorrecto porque la fecha de emision de un
     * JWT se debe comparar con la fecha de emision de una
     * sesion activa. El motivo por el cual puede ocurrir esta
     * comparacion con una sesion inactiva es que el metodo
     * findLastSession recupera la ultima sesion del usuario,
     * independientemente de si esta activa o inactiva.
     * 
     * Este control se implementa para evitar que se puedan
     * recuperar datos mediante peticiones HTTP haciendo uso
     * de un JWT valido, pero que es de una sesion que fue
     * cerrada por el usuario.
     */
    if (!sessionService.checkDateIssueLastSession(userId, JwtManager.getDateIssue(jwt, secretKeyValue))) {
      return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorResponse(ReasonError.JWT_NOT_ASSOCIATED_WITH_ACTIVE_SESSION)).build();
    }

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
     * Obtiene el JWT del valor del encabezado de autorizacion
     * de una peticion HTTP
     */
    String jwt = AuthHeaderManager.getJwt(AuthHeaderManager.getAuthHeaderValue(request));

    /*
     * Valor de la clave secreta con la que la aplicacion firma
     * un JWT
     */
    String secretKeyValue = secretKeyService.find().getValue();

    /*
     * Obtiene el ID de usuario contenido en la carga util del
     * JWT del encabezado de autorizacion de una peticion HTTP
     */
    int userId = JwtManager.getUserId(jwt, secretKeyValue);

    /*
     * Si el usuario que solicita esta operacion NO tiene una
     * sesion activa, la aplicacion del lador servidor devuelve
     * el mensaje 401 (Unauthorized) junto con el mensaje "No
     * tiene una sesion activa" y no se realiza la operacion
     * solicitada
     */
    if (!sessionService.checkActiveSession(userId)) {
      return Response.status(Response.Status.UNAUTHORIZED).entity(new ErrorResponse(ReasonError.NO_ACTIVE_SESSION)).build();
    }

    /*
     * Si la fecha de emision del JWT de un usuario NO es igual
     * a la fecha de emision de la sesion activa del usuario,
     * la aplicacion del lado servidor retorna el mensaje HTTP
     * 400 (Bad request) junto con el mensaje "El JWT no
     * corresponde a una sesión abierta" y no se realiza la
     * operacion solicitada.
     * 
     * Se debe tener en cuenta que el metodo checkDateIssueLastSession
     * de la clase SessionServiceBean debe ser invocado luego
     * de invocar el metodo checkActiveSession de la misma
     * clase, ya que de lo contrario se puede comparar la
     * fecha de emision de un JWT con una sesion inactiva,
     * lo cual es incorrecto porque la fecha de emision de un
     * JWT se debe comparar con la fecha de emision de una
     * sesion activa. El motivo por el cual puede ocurrir esta
     * comparacion con una sesion inactiva es que el metodo
     * findLastSession recupera la ultima sesion del usuario,
     * independientemente de si esta activa o inactiva.
     * 
     * Este control se implementa para evitar que se puedan
     * recuperar datos mediante peticiones HTTP haciendo uso
     * de un JWT valido, pero que es de una sesion que fue
     * cerrada por el usuario.
     */
    if (!sessionService.checkDateIssueLastSession(userId, JwtManager.getDateIssue(jwt, secretKeyValue))) {
      return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorResponse(ReasonError.JWT_NOT_ASSOCIATED_WITH_ACTIVE_SESSION)).build();
    }

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
     * junto con el mensaje "Debe proporcionar todos los datos
     * requeridos" y no se realiza la operacion solicitada
     */
    if (json.isEmpty()) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.EMPTY_DATA))).build();
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
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.INVALID_INITIAL_STAGE))).build();
    }

    if (modifiedCrop.getDevelopmentStage() <= 0) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.INVALID_DEVELOPMENT_STAGE))).build();
    }

    if (modifiedCrop.getMiddleStage() <= 0) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.INVALID_MIDDLE_STAGE))).build();
    }

    if (modifiedCrop.getFinalStage() <= 0) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.INVALID_FINAL_STAGE))).build();
    }

    /*
     * El ciclo de vida (dias) de un cultivo es la sumatoria
     * de su etapa inicial, su etapa de desarrollo, su etapa
     * media y su etapa final, las cuales se miden en dias
     */
    modifiedCrop.setLifeCycle(cropService.calculateLifeCycle(modifiedCrop));

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
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.INVALID_INITIAL_KC))).build();      
    }

    if (modifiedCrop.getMiddleKc() <= 0.0) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.INVALID_MIDDLE_KC))).build();      
    }

    if (modifiedCrop.getFinalKc() <= 0.0) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.INVALID_FINAL_KC))).build();      
    }


    /*
     * ***************************************************
     * Controles sobre el limite minimo y el limite maximo
     * de la profundidad radicular
     * ***************************************************
     */

    /*
     * Si uno de los limites de la profundidad radicular maxima
     * del cultivo a modificar tiene un valor menor o igual a 0.0,
     * la aplicacion del lado servidor retorna el mensaje HTTP 400
     * (Bad request) junto con el mensaje "El <nombre del limite>
     * debe ser mayor a 0.0" y no se realiza la operacion solicitada
     */
    if (modifiedCrop.getLowerLimitMaximumRootDepth() <= 0.0) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.LOWER_LIMIT_MAXIMUM_ROOT_DEPTH_INVALID))).build();      
    }

    if (modifiedCrop.getUpperLimitMaximumRootDepth() <= 0.0) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.UPPER_LIMIT_MAXIMUM_ROOT_DEPTH_INVALID))).build();      
    }

    /*
     * Si el limite inferior de la profundidad radicular maxima
     * de un cultivo es mayor o igual al limite superior de la
     * profundidad radicular maxima de un cultivo, la aplicacion
     * del lado servidor retorna el mensaje HTTP 400 (Bad request)
     * junto con el mensaje "El limite inferior de la profundidad
     * radicular maxima no debe ser mayor o igual al limite superior
     * de la profundidad radicular maxima" y no se realiza la
     * operacion solicitada
     */
    if (modifiedCrop.getLowerLimitMaximumRootDepth() >= modifiedCrop.getUpperLimitMaximumRootDepth()) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.OVERLAPPING_ROOT_DEPTH_LIMITS))).build();      
    }

    /*
     * **************************************************
     * Controles sobre el valor del factor de agotamiento
     * **************************************************
     */

    /*
     * Si el factor de agotamiento (p) es menor al limite inferior
     * (0.1) o mayor al limite superior (0.8), la aplicacion del
     * lado servidor retorna el mensaje HTTP 400 (Bad request)
     * junto con el mensaje "El factor de agotamiento debe tener
     * un valor entre 0.1 y 0.8" y no se realiza la operacion
     * solicitada.
     * 
     * ¿De donde proviene esta limitacion del factor de agotamiento
     * (p)? La establece el libro "Evapotranspiracion del cultivo,
     * estudio FAO riego y drenaje 56" en el primer parrafo de la
     * pagina 163.
     */
    if (modifiedCrop.getDepletionFactor() < cropService.getLowerLimitDepletionFactor()
        || modifiedCrop.getDepletionFactor() > cropService.getUpperLimitDepletionFactor()) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.INVALID_DEPLETION_FACTOR))).build();
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
