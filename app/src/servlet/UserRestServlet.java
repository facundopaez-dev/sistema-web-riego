package servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import javax.ejb.EJB;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import stateless.UserServiceBean;
import stateless.PasswordServiceBean;
import stateless.EmailServiceBean;
import stateless.AccountActivationLinkServiceBean;
import stateless.PasswordResetLinkServiceBean;
import stateless.SessionServiceBean;
import stateless.TypePrecipitationServiceBean;
import stateless.ClimateRecordServiceBean;
import stateless.SoilWaterBalanceServiceBean;
import stateless.HarvestServiceBean;
import stateless.IrrigationRecordServiceBean;
import stateless.PlantingRecordServiceBean;
import stateless.ParcelServiceBean;
import stateless.OptionServiceBean;
import stateless.GeographicLocationServiceBean;
import stateless.StatisticalGraphServiceBean;
import stateless.SecretKeyServiceBean;
import stateless.Page;
import util.ErrorResponse;
import util.ReasonError;
import util.SuccessfullyResponse;
import util.ReasonSuccess;
import util.RequestManager;
import utilJwt.AuthHeaderManager;
import utilJwt.JwtManager;
import model.User;
import model.Email;
import model.Parcel;
import model.UserData;
import model.DataEmailFormPasswordReset;
import model.PasswordChangeFormData;
import model.PasswordResetFormData;
import model.PasswordResetLink;
import util.EmailManager;

@Path("/users")
public class UserRestServlet {

  // inject a reference to the UserServiceBean slsb
  @EJB UserServiceBean userService;
  @EJB PasswordServiceBean passwordService;
  @EJB EmailServiceBean emailService;
  @EJB AccountActivationLinkServiceBean accountActivationLinkService;
  @EJB PasswordResetLinkServiceBean passwordResetLinkService;
  @EJB SessionServiceBean sessionService;
  @EJB TypePrecipitationServiceBean typePrecipitationService;
  @EJB ClimateRecordServiceBean climateRecordService;
  @EJB SoilWaterBalanceServiceBean soilWaterBalanceService;
  @EJB HarvestServiceBean harvestService;
  @EJB IrrigationRecordServiceBean irrigationRecordService;
  @EJB PlantingRecordServiceBean plantingRecordService;
  @EJB ParcelServiceBean parcelService;
  @EJB OptionServiceBean optionService;
  @EJB GeographicLocationServiceBean geographicLocationService;
  @EJB StatisticalGraphServiceBean statisticalGraphService;
  @EJB SecretKeyServiceBean secretKeyService;

  // Mapea lista de pojo a JSON
  ObjectMapper mapper = new ObjectMapper();

  @GET
  @Path("/findAllUsersExceptOwnUser")
  @Produces(MediaType.APPLICATION_JSON)
  public Response findAllUsersExceptOwnUser(@Context HttpHeaders request, @QueryParam("page") Integer page,
      @QueryParam("cant") Integer cant, @QueryParam("search") String search) throws IOException {
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
      return Response.status(Response.Status.UNAUTHORIZED).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.NO_ACTIVE_SESSION))).build();
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
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.JWT_NOT_ASSOCIATED_WITH_ACTIVE_SESSION)))
          .build();
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
     * Si el usuario que solicita esta operacion no tiene el permiso
     * para modificar el permiso de administrador (superuser), la
     * aplicacion del lado servidor devuelve el mensaje HTTP 403
     * (Forbidden) junto con el mensaje "Acceso no autorizado" (esta
     * contenido en el enum ReasonError) y no se realiza la operacion
     * solicitada
     */
    if (!JwtManager.getSuperuserPermissionModifier(jwt, secretKeyService.find().getValue())) {
      return Response.status(Response.Status.FORBIDDEN).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.UNAUTHORIZED_ACCESS))).build();
    }

    Map<String, String> map = new HashMap<String, String>();

    // Convert JSON string to Map
    map = mapper.readValue(search, new TypeReference<Map<String, String>>() {});

    /*
     * Si el valor del encabezado de autorizacion de la peticion HTTP
     * dada, tiene un JWT valido, la aplicacion del lado servidor
     * devuelve el mensaje HTTP 200 (Ok) junto con los datos solicitados
     * por el cliente
     */
    Page<User> users = userService.findAllUsersExceptOwnUser(userId, page, cant, map);
    return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(users)).build();
  }

  @PUT
  @Path("/modifySuperuserPermission/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response modifySuperuserPermission(@Context HttpHeaders request, @PathParam("id") int targetUserId, String json) throws IOException {
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
      return Response.status(Response.Status.UNAUTHORIZED).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.NO_ACTIVE_SESSION))).build();
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
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.JWT_NOT_ASSOCIATED_WITH_ACTIVE_SESSION)))
          .build();
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
     * Si el usuario que solicita esta operacion no tiene el permiso
     * para modificar el permiso de administrador (superuser), la
     * aplicacion del lado servidor devuelve el mensaje HTTP 403
     * (Forbidden) junto con el mensaje "Acceso no autorizado" (esta
     * contenido en el enum ReasonError) y no se realiza la operacion
     * solicitada
     */
    if (!JwtManager.getSuperuserPermissionModifier(jwt, secretKeyService.find().getValue())) {
      return Response.status(Response.Status.FORBIDDEN).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.UNAUTHORIZED_ACCESS))).build();
    }

    /*
     * Si un usuario con permiso de administrador y con permiso de
     * modificar el permiso de administrador, intenta modificar su
     * permiso de administrador, la aplicacion del lado servidor
     * retorna el mensaje HTTP 400 (Bad request) junto con un
     * mensaje que indica lo sucedido y no se realiza la operacion
     * solicitada
     */
    if (userId == targetUserId) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.OWN_MODIFICATION_ADMIN_PERMISSION_NOT_ALLOWED)))
          .build();
    }

    /*
     * Si el usuario al que se le desea modificar el permiso de
     * administrador, NO esta activo, la aplicacion del lado
     * servidor retorna el mensaje HTTP 400 (Bad request) junto
     * con un mensaje que indica lo sucedido y no se realiza la
     * operacion solicitada
     */
    if (!userService.find(targetUserId).getActive()) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.INACTIVE_USER_ADMIN_PERMISSION_MODIFICATION)))
          .build();
    }

    User modifiedUser = mapper.readValue(json, User.class);
    userService.modifySuperuserPermission(targetUserId, modifiedUser.getSuperuser());

    /*
     * Si el valor del encabezado de autorizacion de la peticion HTTP
     * dada, tiene un JWT valido, y se cumplen las condiciones que se
     * deben cumplir para modificar un dato correspondiente a esta
     * clase, la aplicacion del lado servidor devuelve el mensaje
     * HTTP 200 (Ok)
     */
    return Response.status(Response.Status.OK).build();
  }

  @DELETE
  @Path("/deleteUser/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response deleteUser(@Context HttpHeaders request, @PathParam("id") int targetUserId, String json) throws IOException {
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
      return Response.status(Response.Status.UNAUTHORIZED).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.NO_ACTIVE_SESSION))).build();
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
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.JWT_NOT_ASSOCIATED_WITH_ACTIVE_SESSION)))
          .build();
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
     * Si el usuario que solicita esta operacion NO tiene el permiso de
     * eliminacion de usuario, la aplicacion del lado servidor devuelve
     * el mensaje HTTP 403 (Forbidden) junto con el mensaje "Acceso no
     * autorizado" (esta contenido en el enum ReasonError) y no se realiza
     * la operacion solicitada
     */
    if (!JwtManager.getUserDeletionPermission(jwt, secretKeyService.find().getValue())) {
      return Response.status(Response.Status.FORBIDDEN).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.UNAUTHORIZED_ACCESS))).build();
    }

    /*
     * Si un usuario con permiso de administrador y con permiso de
     * eliminacion de usuario, intenta eliminar su propio usuario,
     * la aplicacion del lado servidor retorna el mensaje HTTP 400
     * (Bad request) junto con un mensaje que indica lo sucedido y
     * no se realiza la operacion solicitada
     */
    if (userId == targetUserId) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.SELF_DELETION_NOT_ALLOWED)))
          .build();
    }

    /*
     * Para poder eliminar un usuario se debe eliminar sus datos
     * asociados
     */
    deleteUserAndAssociatedData(targetUserId);

    /*
     * Si el valor del encabezado de autorizacion de la peticion HTTP
     * dada, tiene un JWT valido, y se cumplen las condiciones que se
     * deben cumplir para realizar esta operacion, la aplicacion del
     * lado servidor devuelve el mensaje HTTP 200 (Ok)
     */
    return Response.status(Response.Status.OK).build();
  }

  @GET
  @Path("/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response find(@Context HttpHeaders request, @PathParam("id") int targetUserId) throws IOException {
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
      return Response.status(Response.Status.UNAUTHORIZED).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.NO_ACTIVE_SESSION))).build();
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
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.JWT_NOT_ASSOCIATED_WITH_ACTIVE_SESSION)))
          .build();
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
     * Si el usuario que solicita esta operacion no tiene el permiso
     * para modificar el permiso de administrador (superuser), la
     * aplicacion del lado servidor devuelve el mensaje HTTP 403
     * (Forbidden) junto con el mensaje "Acceso no autorizado" (esta
     * contenido en el enum ReasonError) y no se realiza la operacion
     * solicitada
     */
    if (!JwtManager.getSuperuserPermissionModifier(jwt, secretKeyService.find().getValue())) {
      return Response.status(Response.Status.FORBIDDEN).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.UNAUTHORIZED_ACCESS))).build();
    }

    /*
     * Si el valor del encabezado de autorizacion de la peticion HTTP
     * dada, tiene un JWT valido, la aplicacion del lado servidor
     * devuelve el mensaje HTTP 200 (Ok) junto con los datos solicitados
     * por el cliente
     */
    return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(userService.find(targetUserId))).build();
	}

  /*
   * Este metodo es para que el usuario pueda modificar
   * los datos de su cuenta al presionar el boton que
   * tiene el icono de un lapiz en la pagina web de
   * inicio
   */
  @GET
  @Path("/myAccountDetails")
  @Produces(MediaType.APPLICATION_JSON)
  public Response findMyAccountDetails(@Context HttpHeaders request) throws IOException {
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
      return Response.status(Response.Status.UNAUTHORIZED).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.NO_ACTIVE_SESSION))).build();
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
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.JWT_NOT_ASSOCIATED_WITH_ACTIVE_SESSION)))
          .build();
    }

    UserData userData = setUserData(userService.find(userId));

    /*
     * Si el valor del encabezado de autorizacion de la peticion HTTP
     * dada, tiene un JWT valido, la aplicacion del lado servidor
     * devuelve el mensaje HTTP 200 (Ok) junto con los datos solicitados
     * por el cliente
     */
    return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(userData)).build();
	}

  /*
   * Este metodo es para que el usuario pueda ver los datos de
   * su cuenta en la lista de la pagina de inicio del usuario
   * (home)
   */
  @GET
  @Path("/myAccount")
  @Produces(MediaType.APPLICATION_JSON)
	public Response findMyAccount(@Context HttpHeaders request) throws IOException {
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
      return Response.status(Response.Status.UNAUTHORIZED).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.NO_ACTIVE_SESSION))).build();
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
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.JWT_NOT_ASSOCIATED_WITH_ACTIVE_SESSION)))
          .build();
    }

    User user = userService.find(userId);

    UserData userData = setUserData(user);

    /*
     * Se utiliza una coleccion para que el usuario visualice
     * los datos de su cuenta en forma renglon en la pagina
     * web de inicio (home)
     */
    Collection<UserData> data = new ArrayList<>();
    data.add(userData);

    /*
     * Si el valor del encabezado de autorizacion de la peticion HTTP
     * dada, tiene un JWT valido, la aplicacion del lado servidor
     * devuelve el mensaje HTTP 200 (Ok) junto con los datos solicitados
     * por el cliente
     */
    return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(data)).build();
	}

  @PUT
  @Path("/modify")
  @Produces(MediaType.APPLICATION_JSON)
  public Response modify(@Context HttpHeaders request, String json) throws IOException {
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
      return Response.status(Response.Status.UNAUTHORIZED).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.NO_ACTIVE_SESSION))).build();
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
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.JWT_NOT_ASSOCIATED_WITH_ACTIVE_SESSION)))
          .build();
    }

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

    UserData modifiedUserData = mapper.readValue(json, UserData.class);

    /*
     * ******************************************
     * Controles sobre la definicion de los datos
     * ******************************************
     */

    /*
     * Si el nombre de usuario NO esta definido, la aplicacion
     * del lado servidor retorna el mensaje HTTP 400 (Bad request)
     * junto con el mensaje "El nombre de usuario debe estar
     * definido" y no se realiza la operacion solicitada
     */
    if (modifiedUserData.getUsername() == null) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.UNDEFINED_USERNAME))).build();
    }

    /*
     * Si el nombre NO esta definido, la aplicacion del lado
     * servidor retorna el mensaje HTTP 400 (Bad request) junto
     * con el mensaje "El nombre debe estar definido" y no se
     * realiza la operacion solicitada
     */
    if (modifiedUserData.getName() == null) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.UNDEFINED_NAME))).build();
    }

    /*
     * Si el apellido NO esta definido, la aplicacion del
     * lado servidor retorna el mensaje HTTP 400 (Bad request)
     * junto con el mensaje "El apellido debe estar definido"
     * y no se realiza la operacion solicitada
     */
    if (modifiedUserData.getLastName() == null) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.UNDEFINED_LAST_NAME))).build();
    }

    /*
     * Si la direccion de correo electronico NO esta definida,
     * la aplicacion del lado servidor retorna el mensaje HTTP
     * 400 (Bad request) junto con el mensaje "La dirección de
     * correo electrónico debe estar definida" y no se realiza
     * la operacion solicitada
     */
    if (modifiedUserData.getEmail() == null) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.UNDEFINED_EMAIL))).build();
    }

    /*
     * *************************************
     * Controles sobre la forma de los datos
     * *************************************
     */

    /*
     * Si el nombre de usuario NO tiene una longitud de entre 4
     * y 15 caracteres, y NO empieza con caracteres alfabeticos
     * seguido o no de numeros y/o guiones bajos, la aplicacion
     * del lado servidor retorna el mensaje 400 (Bad request) junto
     * con el mensaje "El nombre de usuario debe tener una longitud
     * de entre 4 y 15 caracteres, comenzar con caracteres alfabeticos
     * (sin simbolos de acentuacion) seguido o no de numeros y/o guiones
     * bajos" y no se realiza la operacion solicitada
     */
    if (!modifiedUserData.getUsername().matches("^[A-Za-z][A-Za-z0-9_]{3,14}$")) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.MALFORMED_USERNAME))).build();
    }

    /*
     * Si el nombre NO tiene una longitud entre 3 y 30 caracteres
     * alfabeticos, NO empieza con una letra mayuscula seguida de
     * letras minusculas, NO tiene un espacio en blanco entre nombre
     * y nombre en el caso en el que el usuario tenga mas de un nombre,
     * y los nombres que vienen a continuacion del primero NO empiezan
     * con una letra mayuscula seguida de letras minusculas, la aplicacion
     * del lado servidor retorna el mensaje HTTP 400 (Bad request) junto
     * con el mensaje "El nombre debe tener una longitud de entre 3 y 30
     * caracteres alfabeticos sin simbolos de acentuacion, empezar con una
     * letra mayuscula seguido de letras minusculas, tener un espacio en
     * blanco entre nombre y nombre si hay mas de un nombre, y los nombres
     * que vienen despues del primero deben empezar con una letra mayuscula
     * seguido de letras minusculas" y no se realiza la operacion solicitada.
     * 
     * La expresion [A-Z] hace que el nombre deba empezar con una letra
     * mayuscula.
     * 
     * La expresion (?=.{2,29}$) hace que el nombre deba tener una
     * longitud de entre 3 y 30 caracteres.
     * 
     * La expresion [a-z]+ hace que el nombre deba tener una o mas
     * letras minusculas.
     * 
     * La expresion (?:\\h[A-Z][a-z]+)* hace que todo nombre que
     * este despues del primero, este precedido por un espacio en
     * blanco, y que empiece con una letra mayuscula seguido de
     * letras minusculas.
     */
    if (!modifiedUserData.getName().matches("^[A-Z](?=.{2,29}$)[a-z]+(?:\\h[A-Z][a-z]+)*$")) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.MALFORMED_NAME))).build();
    }

    /*
     * Si el apellido NO tiene una longitud de entre 3 y 30 caracteres
     * alfabeticos, NO empieza con una letra mayuscula seguida de
     * letras minusculas, NO tiene un espacio en blanco entre apellido
     * y apellido en el caso en el que el usuario tenga mas de un apellido,
     * y los apellidos que vienen a continuacion del primero NO empiezan
     * con una letra mayuscula seguida de letras minusculas, la aplicacion
     * del lado servidor retorna el mensaje HTTP 400 (Bad request) junto
     * con el mensaje "El apellido debe tener una longitud de entre 3 y 30
     * caracteres alfabeticos sin simbolos de acentuacion, empezar con una
     * letra mayuscula seguido de letras minusculas, tener un espacio en
     * blanco entre apellido y apellido si hay mas de un apellido, y los
     * apellidos que vienen despues del primero deben empezar con una letra
     * mayuscula seguido de letras minusculas" y no se realiza la operacion
     * solicitada.
     */
    if (!modifiedUserData.getLastName().matches("^[A-Z](?=.{2,29}$)[a-z]+(?:\\h[A-Z][a-z]+)*$")) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.MALFORMED_LAST_NAME))).build();
    }

    /*
     * Si la direccion de correo electronico NO es valida, la aplicacion
     * del lado servidor retorna el mensaje HTTP 400 (Bad request) junto
     * con el mensaje "La direccion de correo electronico no es valida" y
     * no se realiza la operacion solicitada
     */
    if (!modifiedUserData.getEmail().matches("^(?=.{1,64}@)[\\p{L}0-9_-]+(\\.[\\p{L}0-9_-]+)*@[^-][\\p{L}0-9-]+(\\.[\\p{L}0-9-]+)*(\\.[\\p{L}]{2,})$")) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.MALFORMED_EMAIL))).build();
    }

    /*
     * ****************************************************************************************
     * Controles sobre la existencia del nombre de usuario y la direccion de correo electronico
     * ****************************************************************************************
     */

    /*
     * Si el nombre de usuario ingresado en el formulario, existe
     * en la base de datos subyacente, la aplicacion del lado servidor
     * retorna el mensaje HTTP 400 (Bad request) junto con el mensaje
     * "Nombre de usuario ya utilizado, elija otro" y no se realiza la
     * operacion solicitada
     */
    if (userService.checkExistenceUsername(userId, modifiedUserData.getUsername())) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.USERNAME_ALREADY_USED))).build();
    }

    /*
     * Si la direccion de correo electronico ingresada en el formulario,
     * existe en la base de datos subyacente, la aplicacion del lado
     * servidor retorna el mensaje HTTP 400 (Bad request) junto con el
     * mensaje "Correo electronico ya utilizado, elija otro" y no se
     * realiza la operacion solicitada
     */
    if (emailService.checkExistenceEmail(userId, modifiedUserData.getEmail())) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.EMAIL_ALREADY_USED))).build();
    }

    Email myModifiedEmail = emailService.findEmailByUserId(userId);
    myModifiedEmail.setAddress(modifiedUserData.getEmail());

    emailService.modify(myModifiedEmail.getId(), myModifiedEmail);

    User myModifiedUser = new User();
    myModifiedUser.setUsername(modifiedUserData.getUsername());
    myModifiedUser.setName(modifiedUserData.getName());
    myModifiedUser.setLastName(modifiedUserData.getLastName());

    myModifiedUser = userService.modify(userId, myModifiedUser);

    /*
     * Si el valor del encabezado de autorizacion de la peticion HTTP
     * dada, tiene un JWT valido, y se cumplen las condiciones que se
     * deben cumplir para modificar un dato correspondiente a esta
     * clase, la aplicacion del lado servidor devuelve el mensaje
     * HTTP 200 (Ok) junto con los datos que el cliente solicito
     * modificar
     */
    return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(myModifiedUser)).build();
  }

  @PUT
  @Path("/modifyPassword")
  @Produces(MediaType.APPLICATION_JSON)
  public Response modifyPassword(@Context HttpHeaders request, String json) throws IOException {
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
      return Response.status(Response.Status.UNAUTHORIZED).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.NO_ACTIVE_SESSION))).build();
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
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.JWT_NOT_ASSOCIATED_WITH_ACTIVE_SESSION)))
          .build();
    }

    /*
     * ******************************************
     * Controles sobre la definicion de los datos
     * ******************************************
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

    PasswordChangeFormData newPasswordData = mapper.readValue(json, PasswordChangeFormData.class);

    /*
     * Si el usuario presiona el boton "Modificar" con el campo de la
     * contraseña vacio, la aplicacion del lado servidor retorna el
     * mensaje HTTP 400 (Bad request) junto con el mensaje "La contraseña
     * debe estar definida" y no se realiza la operacion solicitada
     */
    if (newPasswordData.getPassword() == null) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.UNDEFINED_PASSWORD))).build();
    }

    /*
     * Si el usuario presiona el boton "Modificar" con el campo de la
     * nueva contraseña vacio, la aplicacion del lado servidor retorna
     * el mensaje HTTP 400 (Bad request) junto con el mensaje "La nueva
     * contraseña debe estar definida" y no se realiza la operacion
     * solicitada
     */
    if (newPasswordData.getNewPassword() == null) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.UNDEFINED_NEW_PASSWORD))).build();
    }

    /*
     * Si el usuario presiona el boton "Modificar" con el campo de la
     * confirmacion de la nueva contraseña vacio, la aplicacion del
     * lado servidor retorna el mensaje HTTP 400 (Bad request) junto
     * con el mensaje "La confirmacion de la nueva contraseña debe
     * estar definida" y no se realiza la operacion solicitada
     */
    if (newPasswordData.getNewPasswordConfirmed() == null) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.UNDEFINED_CONFIRMED_NEW_PASSWORD))).build();
    }

    /*
     * *************************************
     * Controles sobre la forma de los datos
     * *************************************
     */

    /*
     * Si la nueva contraseña NO contiene como minimo 8 caracteres de longitud,
     * una letra minuscula, una letra mayuscula y un numero 0 a 9, la
     * aplicacion del lado servidor retorna el mensaje HTTP 400 (Bad request)
     * junto con el mensaje "La contraseña debe tener como minimo 8
     * caracteres de longitud, una letra minuscula, una letra mayuscula y un
     * numero de 0 a 9, con o sin caracteres especiales" y no se realiza
     * la operacion solicitada
     */
    if (!(newPasswordData.getNewPassword().matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$"))) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.MALFORMED_NEW_PASSWORD))).build();
    }

    /*
     * ********************************************************************************************
     * Control sobre la igualdad entre la nueva contraseña y la confirmacion de la nueva contraseña
     * ********************************************************************************************
     */

    /*
     * Si la nueva contraseña y la confirmacion de la nueva contraseña
     * NO son iguales, la aplicacion del lado servidor retorna el mensaje
     * HTTP 400 (Bad request) junto con el mensaje "La confirmación de la
     * nueva contraseña no es igual a la nueva contraseña ingresada"
     */
    if (!(newPasswordData.getNewPassword().equals(newPasswordData.getNewPasswordConfirmed()))) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.INCORRECTLY_CONFIRMED_NEW_PASSWORD))).build();
    }

    /*
     * *************************
     * Autenticacion del usuario
     * *************************
     */

    /*
     * Se recupera el usuario completo de la base de datos
     * subyacente para autenticar que realmente el es el
     * que quiere modificar su contraseña
     */
    User givenUser = userService.find(userId);

    /*
     * Si la contraseña ingresada por el usuario no es la correcta,
     * la aplicacion del lado servidor retorna el mensaje HTTP 400
     * (Bad request) junto con el mensaje "Contraseña incorrecta"
     * y no se realiza la operacion solicitada
     */
    if (!(passwordService.authenticateUser(givenUser.getUsername(), newPasswordData.getPassword()))) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.INCORRECT_PASSWORD))).build();
    }

    /*
     * Si se cumplen todos los controles, la aplicacion del lado
     * servidor modifica la contraseña del usuario y retorna el
     * mensaje HTTP 200 (Ok)
     */
    passwordService.modify(userId, newPasswordData.getNewPassword());
    return Response.status(Response.Status.OK).build();
  }

  @PUT
  @Path("/passwordResetEmail")
  @Produces(MediaType.APPLICATION_JSON)
  public Response sendEmailPasswordRecovery(String json) throws IOException {
    /*
     * ******************************************
     * Controles sobre la definicion de los datos
     * ******************************************
     */

    /*
     * Si el objeto de tipo String referenciado por la referencia
     * contenida en la variable de tipo por referencia json de tipo
     * String, esta vacio, significa que el usuario NO completo el
     * formulario con su correo electronico para el restablecimiento
     * de la contraseña de su cuenta. Por lo tanto, la aplicacion del
     * lado servidor retorna el mensaje HTTP 400 (Bad request) junto
     * con el mensaje "La direcion de correo electronico debe estar
     * definida" y no se realiza la operacion solicitada
     */
    if (json.isEmpty()) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.UNDEFINED_EMAIL))).build();
    }

    DataEmailFormPasswordReset dataEmailFormPasswordReset = mapper.readValue(json, DataEmailFormPasswordReset.class);

    /*
     * ***********************************
     * Control sobre la forma de los datos
     * ***********************************
     */

    /*
     * Si la direccion de correo electronico NO es valida, la aplicacion
     * del lado servidor retorna el mensaje HTTP 400 (Bad request) junto
     * con el mensaje "La direccion de correo electronico no es valida" y
     * no se realiza la operacion solicitada
     */
    if (!dataEmailFormPasswordReset.getEmail().matches("^(?=.{1,64}@)[\\p{L}0-9_-]+(\\.[\\p{L}0-9_-]+)*@[^-][\\p{L}0-9-]+(\\.[\\p{L}0-9-]+)*(\\.[\\p{L}]{2,})$")) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.MALFORMED_EMAIL))).build();
    }

    /*
     * ****************************************
     * Control sobre la existencia de los datos
     * ****************************************
     */

    /*
     * Si la direccion de correo electronico NO esta registrada en ninguna
     * cuenta, la aplicacion del lado servidor retorna el mensaje HTTP 400
     * (Bad request) junto con el mensaje "No existe una cuenta con la dirección
     * de correo electrónico ingresada" y no se realiza la operacion solicitada
     */
    if (!emailService.checkExistenceEmail(dataEmailFormPasswordReset.getEmail())) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.THERE_IS_NO_ACCOUNT_WITH_EMAIL_ADDRESS_ENTERED)))
          .build();
    }

    /*
     * *********************************************************
     * Control sobre el estado (si esta activo o no) del usuario
     * correspondiente al correo electronico dado
     * *********************************************************
     */

    /*
     * Si la cuenta correspondiente al correo electronico ingresado por
     * el usuario para recuperar su contraseña, esta inactiva, la aplicacion
     * del lado servidor retorna el mensaje HTTP 400 (Bad request) junto con
     * el mensaje "Para recuperar su contraseña primero debe activar su cuenta
     * mediante el correo electronico de confirmacion de registro" y no se realiza
     * la operacion solicitada
     */
    if (!userService.isActive(userService.findUserByEmail(dataEmailFormPasswordReset.getEmail()).getId())) {
        return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.INACTIVE_USER_TO_RECOVER_PASSWORD))).build();
    }

    /*
     * Si se cumplen todos los controles:
     * - se crea y persiste un enlace de restablecimiento de contraseña,
     * - se crea un JWT con el ID de dicho enlace y el correo electronico
     * ingresado por el usuario para el restablecimiento de la contraseña
     * de su cuenta, y
     * - se envia a dicho correo un correo electronico que contiene el
     * enlace de restablecimiento de contraseña, el cual, tiene el JWT
     * creado.
     */
    PasswordResetLink givenPasswordResetLink = passwordResetLinkService.create(userService.findUserByEmail(dataEmailFormPasswordReset.getEmail()));
    String jwtResetPassword = JwtManager.createJwt(givenPasswordResetLink.getId(), dataEmailFormPasswordReset.getEmail(), secretKeyService.find().getValue());
    EmailManager.sendPasswordResetEmail(dataEmailFormPasswordReset.getEmail(), jwtResetPassword);

    /*
     * Si se cumplen todos los controles, la aplicacion del lado
     * servidor envia un correo para el restablecimiento de la
     * contraseña a la direccion de correo electronico ingresada
     * por el usuario para ello, y retorna el mensaje HTTP 200
     * (Ok) junto con el mensaje "Correo electrónico de restablecimiento
     * de contraseña enviado a su casilla de correo electrónico"
     */
    return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(new SuccessfullyResponse(ReasonSuccess.PASSWORD_RESET_EMAIL_SENT))).build();
  }

  @PUT
  @Path("/resetPassword/{jwtResetPassword}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response resetPassword(@PathParam("jwtResetPassword") String jwt, String json) throws IOException {
    /*
     * *****************************************************
     * Controles sobre el JWT del enlace de restablecimiento
     * de contraseña
     * *****************************************************
     */

    /*
     * Si el JWT para el restablecimiento de la contraseñ esta
     * vacio, la aplicacion del lado servidor retorna el mensaje
     * HTTP 400 (Bad request) junto con el mensaje "Enlace de
     * restablecimiento de contraseña invalido" y no se realiza
     * la operacion solicitada
     */
    if (jwt.isEmpty()) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.INVALID_PASSWORD_RESET_LINK))).build();
    }

    /*
     * Si la firma del JWT que esta como parametro de ruta en un
     * enlace de restablecimiento de contraseña, NO coincide con
     * los datos de su encabezado y carga util, la aplicacion del
     * lado servidor retorna el mensaje HTTP 400 (Bad request) junto
     * con el mensaje "Enlace de restablecimiento de contraseña
     * invalido" y no se realiza la operacion solicitada
     */
    if (!JwtManager.validateJwt(jwt, secretKeyService.find().getValue())) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.INVALID_PASSWORD_RESET_LINK))).build();
    }

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

    PasswordResetFormData passwordResetFormData = mapper.readValue(json, PasswordResetFormData.class);

    /*
     * ******************************************
     * Controles sobre la definicion de los datos
     * ******************************************
     */

    /*
     * Si la nueva contraseña NO esta definida, la aplicacion del
     * lado servidor retorna el mensaje HTTP 400 (Bad request)
     * junto con el mensaje "La nueva contraseña debe estar
     * definida" y no se realiza la operacion solicitada
     */
    if (passwordResetFormData.getNewPassword() == null) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.UNDEFINED_NEW_PASSWORD))).build();
    }

    /*
     * Si la confirmacion de la nueva contraseña NO esta definida,
     * la aplicacion del lado servidor retorna el mensaje HTTP 400
     * (Bad request) junto con el mensaje "La confirmación de la
     * nueva contraseña debe estar definida" y no se realiza la
     * operacion solicitada
     */
    if (passwordResetFormData.getNewPasswordConfirmed() == null) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.UNDEFINED_CONFIRMED_NEW_PASSWORD))).build();
    }

    /*
     * *************************************
     * Controles sobre la forma de los datos
     * *************************************
     */

    /*
     * Si la nueva contraseña NO contiene como minimo 8 caracteres de longitud,
     * una letra minuscula, una letra mayuscula y un numero 0 a 9, la
     * aplicacion del lado servidor retorna el mensaje HTTP 400 (Bad request)
     * junto con el mensaje "La nueva contraseña debe tener como minimo 8
     * caracteres de longitud, una letra minuscula, una letra mayuscula y un
     * numero de 0 a 9, con o sin caracteres especiales" y no se realiza
     * la operacion solicitada
     */
    if (!passwordResetFormData.getNewPassword().matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$")) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.MALFORMED_NEW_PASSWORD))).build();
    }

    /*
     * ********************************************************************************************
     * Control sobre la igualdad entre la nueva contraseña y la confirmacion de la nueva contraseña
     * ********************************************************************************************
     */

    /*
     * Si la la nueva contraseña y la confirmacion de la nueva contraseña
     * NO son iguales, la aplicacion del lado servidor retorna el mensaje
     * HTTP 400 (Bad request) junto con el mensaje "La confirmación de la
     * nueva contraseña no es igual a la nueva contraseña ingresada"
     */
    if (!(passwordResetFormData.getNewPassword().equals(passwordResetFormData.getNewPasswordConfirmed()))) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.INCORRECTLY_CONFIRMED_NEW_PASSWORD))).build();
    }

    /*
     * ***********************************************************
     * Controles sobre el enlace de restablecimiento de contraseña
     * ***********************************************************
     */

    int passwordResetLinkId = JwtManager.getPasswordResetLinkId(jwt, secretKeyService.find().getValue());
    String userEmail = JwtManager.getUserEmail(jwt, secretKeyService.find().getValue());

    /*
     * Si en la base de datos subyacente NO existe un enlace de
     * restablecimiento de contraseña con el ID dado y asociado
     * al correo electronico dado, la aplicacion del lado servidor
     * retorna el mensaje HTTP 400 (Bad request) junto con el mensaje
     * "Enlace de restablecimiento de contraseña invalido" y no se
     * realiza la operacion solicitada
     */
    if (!passwordResetLinkService.checkExistence(passwordResetLinkId, userEmail)) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.INVALID_PASSWORD_RESET_LINK))).build();
    }

    /*
     * Si el enlace de restablecimiento de contraseña correspondiente
     * al ID dado y asociado al correo electronico dado, ya fue consumido
     * (es decir, fue utilizado por su respectivo usuario para el restablecimiento
     * de la contraseña de su cuenta), la aplicacion del lado servidor
     * retorna el mensaje HTTP 400 (Bad request) junto con el mensaje
     * "Enlace de restablecimiento de contraseña invalido" y no realiza
     * la operacion solicitada
     */
    if (passwordResetLinkService.checkConsumed(passwordResetLinkId, userEmail)) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.INVALID_PASSWORD_RESET_LINK))).build();
    }

    /*
     * Si el enlace de restablecimiento de contraseña correspondiente
     * al ID dado y asociado al correo electronico dado, expiro, la
     * aplicacion del lado servidor retorna el mensaje HTTP 400 (Bad
     * request) junto con el mensaje "Enlace de restablecimiento de
     * contraseña expirado" y no se realiza la operacion solicitada
     */
    if (passwordResetLinkService.checkExpiration(passwordResetLinkId, userEmail)) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.PASSWORD_RESET_LINK_EXPIRED))).build();
    }

    /*
     * Si se pasan todos los controles para el restablecimiento de la
     * contraseña de la cuenta de un usuario, la aplicacion del lado
     * servidor modifica la contraseña del usuario correspondiente
     * al correo electronico dado con la nueva contraseña, establece
     * el atributo consumed del enlace de restablecimiento de
     * contraseña dado en 1, y retorna el mensaje HTTP 200 (Ok) junto
     * con el mensaje "Contraseña restablecida satisfactoriamente".
     * 
     * Un enlace de restablecimiento de contraseña es consumido cuando
     * el usuario que solicito dicho restablecimiento, accede a dicho
     * enlace y restablece su contraseña.
     */
    passwordService.modify(userService.findUserByEmail(userEmail).getId(), passwordResetFormData.getNewPassword());
    passwordResetLinkService.setConsumed(passwordResetLinkId);
    return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(new SuccessfullyResponse(ReasonSuccess.PASSWORD_RESET_SUCCESSFULLY))).build();
  }

  /*
   * Este metodo REST es unicamente para realizar controles
   * sobre un enlace de restablecimiento de contraseña, los
   * cuales, son los siguientes:
   * - comprobar su existencia en la base de datos subyacente,
   * - comprobar si fue consumido, y
   * - comprobar si expiro.
   * 
   * Un enlace de restablecimiento de contraseña es consumido
   * cuando el usuario que solicito dicho restablecimiento,
   * accede a dicho enlace y restablece su contraseña.
   */
  @GET
  @Path("/checkPasswordResetLink/{jwtResetPassword}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response checkPasswordResetLink(@PathParam("jwtResetPassword") String jwt) throws IOException {
    /*
     * *****************************************************
     * Controles sobre el JWT del enlace de restablecimiento
     * de contraseña
     * *****************************************************
     */

    /*
     * Si el JWT para el restablecimiento de la contraseñ esta
     * vacio, la aplicacion del lado servidor retorna el mensaje
     * HTTP 400 (Bad request) junto con el mensaje "Enlace de
     * restablecimiento de contraseña invalido" y no se realiza
     * la operacion solicitada
     */
    if (jwt.isEmpty()) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.INVALID_PASSWORD_RESET_LINK))).build();
    }

    /*
     * Si la firma del JWT que esta como parametro de ruta en un
     * enlace de restablecimiento de contraseña, no coincide con
     * los datos de su encabezado y carga util, la aplicacion del
     * lado servidor retorna el mensaje HTTP 400 (Bad request) junto
     * con el mensaje "Enlace de restablecimiento de contraseña
     * invalido" y no se realiza la operacion solicitada
     */
    if (!JwtManager.validateJwt(jwt, secretKeyService.find().getValue())) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.INVALID_PASSWORD_RESET_LINK))).build();
    }

    /*
     * ***********************************************************
     * Controles sobre el enlace de restablecimiento de contraseña
     * ***********************************************************
     */

    int passwordResetLinkId = JwtManager.getPasswordResetLinkId(jwt, secretKeyService.find().getValue());
    String userEmail = JwtManager.getUserEmail(jwt, secretKeyService.find().getValue());

    /*
     * Si en la base de datos subyacente NO existe un enlace de
     * restablecimiento de contraseña con el ID dado y asociado
     * al correo electronico dado, la aplicacion del lado servidor
     * retorna el mensaje HTTP 400 (Bad request) junto con el mensaje
     * "Enlace de restablecimiento de contraseña invalido" y no se
     * realiza la operacion solicitada
     */
    if (!passwordResetLinkService.checkExistence(passwordResetLinkId, userEmail)) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.INVALID_PASSWORD_RESET_LINK))).build();
    }

    /*
     * Si el enlace de restablecimiento de contraseña correspondiente
     * al ID dado y asociado al correo electronico dado, ya fue consumido
     * (es decir, fue utilizado por su respectivo usuario para el restablecimiento
     * de la contraseña de su cuenta), la aplicacion del lado servidor
     * retorna el mensaje HTTP 400 (Bad request) junto con el mensaje
     * "Enlace de restablecimiento de contraseña invalido" y no realiza
     * la operacion solicitada
     */
    if (passwordResetLinkService.checkConsumed(passwordResetLinkId, userEmail)) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.INVALID_PASSWORD_RESET_LINK))).build();
    }

    /*
     * Si el enlace de restablecimiento de contraseña correspondiente
     * al ID dado y asociado al correo electronico dado, expiro, la
     * aplicacion del lado servidor retorna el mensaje HTTP 400 (Bad
     * request) junto con el mensaje "Enlace de restablecimiento de
     * contraseña expirado" y no se realiza la operacion solicitada
     */
    if (passwordResetLinkService.checkExpiration(passwordResetLinkId, userEmail)) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.PASSWORD_RESET_LINK_EXPIRED))).build();
    }

    /*
     * Si se pasan los controles sobre el enlace de restablecimiento
     * de contraseña, la aplicacion del lado servidor retorna el
     * mensaje HTTP 200 (Ok)
     */
    return Response.status(Response.Status.OK).build();
  }

  /**
   * @param user
   * @return referencia a un objeto de tipo UserData que
   * contiene el nombre de usuario, el nombre, el apellido
   * y el correo electronico de un usuario
   */
  private UserData setUserData(User user) {
    UserData newUserData = new UserData();
    newUserData.setId(user.getId());
    newUserData.setUsername(user.getUsername());
    newUserData.setName(user.getName());
    newUserData.setLastName(user.getLastName());
    newUserData.setEmail(user.getEmail().getAddress());
    newUserData.setSuperuser(user.getSuperuser());

    return newUserData;
  }

  /**
   * Elimina fisicamente los datos asociados a un usuario:
   * - clave de cuenta
   * - correo electronico
   * - enlaces de activacion de cuenta
   * - enlaces de restablecimiento de clave
   * - sesiones de cuenta
   * - registros climaticos
   * - balances hidricos de suelo
   * - registros de cosecha
   * - registros de riego
   * - registros de plantacion
   * - informes estadisticos
   * - parcelas
   * - opciones de parcelas
   * - ubicaciones geograficas de parcelas
   * 
   * @param userId
   */
  private void deleteUserAndAssociatedData(int userId) {
    passwordService.removeByUserId(userId);
    accountActivationLinkService.removeByUserId(userId);
    passwordResetLinkService.removeByUserId(userId);
    sessionService.removeByUserId(userId);

    climateRecordService.deleteClimateRecordsByUserId(userId);
    soilWaterBalanceService.deleteSoilWaterBalancesByUserId(userId);
    harvestService.deleteHarvestRecordsByUserId(userId);
    irrigationRecordService.deleteIrrigationRecordsByUserId(userId);
    plantingRecordService.deletePlantingRecordsByUserId(userId);

    /*
     * En la base de datos subyacente, la tabla de parcelas contiene una
     * clave foranea hacia las tablas de opciones y ubicaciones geograficas,
     * dado que la clase Parcel incluye una opcion y una ubicacion geografica.
     * Por esta razon, no es posible eliminar una opcion o una ubicacion
     * geografica sin antes eliminar la parcela asociada.
     * 
     * Para eliminar las opciones y ubicaciones geograficas vinculadas a una
     * parcela que se va a eliminar, es necesario obtener los IDs de estas
     * entidades antes de proceder con la eliminacion de la parcela, ya que
     * es a traves de la parcela del usuario como se accede a la opcion y la
     * ubicacion geografica correspondientes.
     */
    Collection<Long> optionIds = optionService.findOptionIdsByUserId(userId); 
    Collection<Long> geographicLocationIds = geographicLocationService.findGeographicLocationIdsByUserId(userId); 

    /*
     * En la base de datos subyacente, la tabla de graficos estadisticos
     * tiene una clave foranea hacia la tabla de parcelas, ya que la
     * clase StatisticalGraph contiene una parcela. Por esta razon, no es
     * posible eliminar una parcela sin antes eliminar un grafico
     * estadistico.
     */
    statisticalGraphService.deleteStatisticalGraphsByUserId(userId);
    parcelService.deleteParcelsByUserId(userId);
    optionService.deleteOptionsByIds(optionIds);
    geographicLocationService.deleteGeographicLocationsByIds(optionIds);

    int emailId = userService.find(userId).getEmail().getId();

    /*
     * Despues de eliminar los datos dependientes del usuario, se puede
     * proceder a eliminar al usuario
     */
    userService.remove(userId);

    /*
     * En la base de datos subyacente, la tabla de usuarios tiene
     * una clave foranea a la tabla de correos electronicos. Por
     * lo tanto, para eliminar un correo electronico primero se
     * debe eliminar un usuario.
     */
    emailService.remove(emailId);
  }

}
