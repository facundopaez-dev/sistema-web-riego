package servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import javax.ejb.EJB;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import stateless.SecretKeyServiceBean;
import stateless.UserServiceBean;
import util.ErrorResponse;
import util.ReasonError;
import util.RequestManager;
import utilJwt.AuthHeaderManager;
import utilJwt.JwtManager;
import model.User;
import model.PasswordChangeFormData;

@Path("/users")
public class UserRestServlet {

  // inject a reference to the UserServiceBean slsb
  @EJB UserServiceBean userService;
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
     * devuelve el mensaje HTTP 200 (Ok) junto con los datos solicitados
     * por el cliente
     */
    return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(userService.findAll())).build();
	}

  @GET
  @Path("/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response find(@Context HttpHeaders request, @PathParam("id") int userId) throws IOException {
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
     * Si el valor del encabezado de autorizacion de la peticion HTTP
     * dada, tiene un JWT valido, la aplicacion del lado servidor
     * devuelve el mensaje HTTP 200 (Ok) junto con los datos solicitados
     * por el cliente
     */
    return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(userService.find(userId))).build();
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
    return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(userService.findMyUser(userId))).build();
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
     * Obtiene el ID de usuario contenido en la carga util del
     * JWT del encabezado de autorizacion de una peticion HTTP
     */
    int userId = JwtManager.getUserId(jwt, secretKeyService.find().getValue());

    /*
     * Si el objeto de tipo String referenciado por la referencia
     * contenida en el variable de tipo por referencia json de tipo
     * String, esta vacio, significa que el usuario quiso modificar
     * sus datos con el formulario de modificacion de datos totalmente
     * vacio. Por lo tanto, la aplicacion del lado servidor retorna el
     * mensaje HTTP 400 (Bad request) junto con el mensaje "Debe completar
     * todos los campos del formulario" y no se realiza la
     * operacion solicitada
     */
    if (json.isEmpty()) {
      return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorResponse(ReasonError.EMPTY_FORM)).build();
    }

    User myModifiedUser = mapper.readValue(json, User.class);

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
    if (myModifiedUser.getUsername() == null) {
      return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorResponse(ReasonError.UNDEFINED_USERNAME)).build();
    }

    /*
     * Si el nombre NO esta definido, la aplicacion del lado
     * servidor retorna el mensaje HTTP 400 (Bad request) junto
     * con el mensaje "El nombre debe estar definido" y no se
     * realiza la operacion solicitada
     */
    if (myModifiedUser.getName() == null) {
      return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorResponse(ReasonError.UNDEFINED_NAME)).build();
    }

    /*
     * Si el apellido NO esta definido, la aplicacion del
     * lado servidor retorna el mensaje HTTP 400 (Bad request)
     * junto con el mensaje "El apellido debe estar definido"
     * y no se realiza la operacion solicitada
     */
    if (myModifiedUser.getLastName() == null) {
      return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorResponse(ReasonError.UNDEFINED_LAST_NAME)).build();
    }

    /*
     * Si la direccion de correo electronico NO esta definida,
     * la aplicacion del lado servidor retorna el mensaje HTTP
     * 400 (Bad request) junto con el mensaje "La dirección de
     * correo electrónico debe estar definida" y no se realiza
     * la operacion solicitada
     */
    if (myModifiedUser.getEmail() == null) {
      return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorResponse(ReasonError.UNDEFINED_EMAIL)).build();
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
     * con el mensaje "El nombre debe usuario debe tener una longitud
     * de entre 4 y 15 caracteres, comenzar con caracteres alfabeticos
     * seguido o no de numeros y/o guiones bajos" y no se realiza la
     * operacion solicitada
     */
    if (!myModifiedUser.getUsername().matches("^[A-Za-z][A-Za-z0-9_]{3,14}$")) {
      return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorResponse(ReasonError.MALFORMED_USERNAME)).build();
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
     * caracteres alfabeticos, empezar con una letra mayuscula seguido
     * de letras minusculas, tener un espacio en blanco entre nombre y
     * nombre si hay mas de un nombre, y los nombres que vienen despues
     * del primero deben empezar con una letra mayuscula seguido de letras
     * minusculas" y no se realiza la operacion solicitada.
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
    if (!myModifiedUser.getName().matches("^[A-Z](?=.{2,29}$)[a-z]+(?:\\h[A-Z][a-z]+)*$")) {
      return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorResponse(ReasonError.MALFORMED_NAME)).build();
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
     * caracteres alfabeticos, empezar con una letra mayuscula seguido de
     * letras minusculas, tener un espacio en blanco entre apellido y apellido
     * si hay mas de un apellido, y los apellidos que vienen despues del primero
     * deben empezar con una letra mayuscula seguido de letras minusculas" y
     * no se realiza la operacion solicitada.
     */
    if (!myModifiedUser.getLastName().matches("^[A-Z](?=.{2,29}$)[a-z]+(?:\\h[A-Z][a-z]+)*$")) {
      return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorResponse(ReasonError.MALFORMED_LAST_NAME)).build();
    }

    /*
     * Si la direccion de correo electronico NO es valida, la aplicacion
     * del lado servidor retorna el mensaje HTTP 400 (Bad request) junto
     * con el mensaje "La direccion de correo electronico no es valida" y
     * no se realiza la operacion solicitada
     */
    if (!myModifiedUser.getEmail().matches("^(?=.{1,64}@)[\\p{L}0-9_-]+(\\.[\\p{L}0-9_-]+)*@[^-][\\p{L}0-9-]+(\\.[\\p{L}0-9-]+)*(\\.[\\p{L}]{2,})$")) {
      return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorResponse(ReasonError.MALFORMED_EMAIL)).build();
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
    if (userService.checkExistenceUsername(userId, myModifiedUser.getUsername())) {
      return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorResponse(ReasonError.USERNAME_ALREADY_USED)).build();
    }

    /*
     * Si la direccion de correo electronico ingresada en el formulario,
     * existe en la base de datos subyacente, la aplicacion del lado
     * servidor retorna el mensaje HTTP 400 (Bad request) junto con el
     * mensaje "Correo electronico ya utilizado, elija otro" y no se
     * realiza la operacion solicitada
     */
    if (userService.checkExistenceEmail(userId, myModifiedUser.getEmail())) {
      return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorResponse(ReasonError.EMAIL_ALREADY_USED)).build();
    }

    /*
     * Si el valor del encabezado de autorizacion de la peticion HTTP
     * dada, tiene un JWT valido, y se cumplen las condiciones que se
     * deben cumplir para modificar un dato correspondiente a esta
     * clase, la aplicacion del lado servidor devuelve el mensaje
     * HTTP 200 (Ok) junto con los datos que el cliente solicito
     * modificar
     */
    return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(userService.modify(userId, myModifiedUser))).build();
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
     * ******************************************
     * Controles sobre la definicion de los datos
     * ******************************************
     */

    /*
     * Si el objeto de tipo String referenciado por la referencia
     * contenida en el variable de tipo por referencia json de tipo
     * String, esta vacio, significa que el usuario quiso modificar
     * su contraseña con el formulario de modificacion de contraseña
     * totalmente vacio. Por lo tanto, la aplicacion del lado servidor
     * retorna el mensaje HTTP 400 (Bad request) junto con el mensaje
     * "Debe completar todos los campos del formulario" y no se realiza
     * la operacion solicitada
     */
    if (json.isEmpty()) {
      return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorResponse(ReasonError.EMPTY_FORM)).build();
    }

    PasswordChangeFormData newPasswordData = mapper.readValue(json, PasswordChangeFormData.class);

    /*
     * Si el usuario presiona el boton "Modificar" con el campo de la
     * contraseña vacio, la aplicacion del lado servidor retorna el
     * mensaje HTTP 400 (Bad request) junto con el mensaje "La contraseña
     * debe estar definida" y no se realiza la operacion solicitada
     */
    if (newPasswordData.getPassword() == null) {
      return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorResponse(ReasonError.UNDEFINED_PASSWORD)).build();
    }

    /*
     * Si el usuario presiona el boton "Modificar" con el campo de la
     * nueva contraseña vacio, la aplicacion del lado servidor retorna
     * el mensaje HTTP 400 (Bad request) junto con el mensaje "La nueva
     * contraseña debe estar definida" y no se realiza la operacion
     * solicitada
     */
    if (newPasswordData.getNewPassword() == null) {
      return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorResponse(ReasonError.UNDEFINED_NEW_PASSWORD)).build();
    }

    /*
     * Si el usuario presiona el boton "Modificar" con el campo de la
     * confirmacion de la nueva contraseña vacio, la aplicacion del
     * lado servidor retorna el mensaje HTTP 400 (Bad request) junto
     * con el mensaje "La confirmacion de la nueva contraseña debe
     * estar definida" y no se realiza la operacion solicitada
     */
    if (newPasswordData.getNewPasswordConfirmed() == null) {
      return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorResponse(ReasonError.UNDEFINED_CONFIRMED_NEW_PASSWORD)).build();
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
    if (!(newPasswordData.getNewPassword().matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{7,}$"))) {
      return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorResponse(ReasonError.MALFORMED_NEW_PASSWORD)).build();
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
    if (!(newPasswordData.getNewPassword().equals(newPasswordData.getNewPasswordConfirmed()))) {
      return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorResponse(ReasonError.INCORRECTLY_CONFIRMED_NEW_PASSWORD)).build();
    }

    /*
     * *************************
     * Autenticacion del usuario
     * *************************
     */

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
    if (!(userService.authenticate(givenUser.getUsername(), newPasswordData.getPassword()))) {
      return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorResponse(ReasonError.INCORRECT_PASSWORD)).build();
    }

    /*
     * Si se cumplen todos los controles, la aplicacion del lado
     * servidor modifica la contraseña del usuario y retorna el
     * mensaje HTTP 200 (Ok)
     */
    userService.modifyPassword(userId, newPasswordData.getNewPassword());
    return Response.status(Response.Status.OK).build();
  }

}
