package servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import javax.ejb.EJB;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import util.ErrorResponse;
import util.ReasonError;
import model.User;
import model.SignupFormData;
import stateless.UserServiceBean;
import stateless.AccountActivationLinkServiceBean;
import util.Email;

@Path("/signup")
public class SignupRestServlet {

  // inject a reference to the UserServiceBean slsb
  @EJB
  UserServiceBean userService;

  @EJB
  AccountActivationLinkServiceBean accountActivationLinkService;

  // Mapea lista de pojo a JSON
  ObjectMapper mapper = new ObjectMapper();

  @POST
  public Response signup(String json) throws IOException {
    /*
     * Si el objeto de tipo String referenciado por la
     * referencia contenida en la variable de tipo por
     * referencia json de tipo String, esta vacio,
     * significa que el usuario quiso registrarse con
     * el formulario de registro totalmente vacio.
     * Por lo tanto, la aplicacion del lado servidor
     * retorna el mensaje HTTP 400 (Bad request) junto
     * con el mensaje "Debe completar todos los campos
     * del formulario" y no se realiza la operacion
     * solicitada
     */
    if (json.isEmpty()) {
      return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorResponse(ReasonError.EMPTY_FORM)).build();
    }

    SignupFormData newUserData = mapper.readValue(json, SignupFormData.class);

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
    if (newUserData.getUsername() == null) {
      return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorResponse(ReasonError.UNDEFINED_USERNAME)).build();
    }

    /*
     * Si el nombre NO esta definido, la aplicacion del lado
     * servidor retorna el mensaje HTTP 400 (Bad request) junto
     * con el mensaje "El nombre debe estar definido" y no se
     * realiza la operacion solicitada
     */
    if (newUserData.getName() == null) {
      return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorResponse(ReasonError.UNDEFINED_NAME)).build();
    }

    /*
     * Si el apellido NO esta definido, la aplicacion del
     * lado servidor retorna el mensaje HTTP 400 (Bad request)
     * junto con el mensaje "El apellido debe estar definido"
     * y no se realiza la operacion solicitada
     */
    if (newUserData.getLastName() == null) {
      return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorResponse(ReasonError.UNDEFINED_LAST_NAME)).build();
    }

    /*
     * Si la direccion de correo electronico NO esta definida,
     * la aplicacion del lado servidor retorna el mensaje HTTP
     * 400 (Bad request) junto con el mensaje "La dirección de
     * correo electrónico debe estar definida" y no se realiza
     * la operacion solicitada
     */
    if (newUserData.getEmail() == null) {
      return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorResponse(ReasonError.UNDEFINED_EMAIL)).build();
    }

    /*
     * Si la contraseña NO esta definida, la aplicacion del
     * lado servidor retorna el mensaje HTTP 400 (Bad request)
     * junto con el mensaje "La contraseña debe estar definida"
     * y no se realiza la operacion solicitada
     */
    if (newUserData.getPassword() == null) {
      return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorResponse(ReasonError.UNDEFINED_PASSWORD)).build();
    }

    /*
     * Si la contraseña confirmada NO esta definida, la aplicacion
     * del lado servidor retorna el mensaje HTTP 400 (Bad request)
     * junto con el mensaje "La confirmacion de la contraseña debe
     * estar definida" y no se realiza la operacion solicitada
     */
    if (newUserData.getPasswordConfirmed() == null) {
      return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorResponse(ReasonError.UNDEFINED_CONFIRMED_PASSWORD)).build();
    }

    /*
     * *************************************
     * Controles sobre la forma de los datos
     * *************************************
     */

    /*
     * Si el nombre de usuario NO tiene una longitud de entre 4
     * y 15 caracteres, y NO empieza con caracteres alfabeticos
     * con o sin numeros y/o guiones bajos, la aplicacion del lado
     * servidor retorna el mensaje 400 (Bad request) junto con el
     * mensaje "El nombre de usuario debe tener una longitud de
     * entre 4 y 15 caracteres, comenzar con caracteres alfabeticos
     * (sin simbolos de acentuacion) seguido o no de numeros y/o
     * guiones bajos" y no se realiza la operacion solicitada
     */
    if (!newUserData.getUsername().matches("^[A-Za-z][A-Za-z0-9_]{3,14}$")) {
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
    if (!newUserData.getName().matches("^[A-Z](?=.{2,29}$)[a-z]+(?:\\h[A-Z][a-z]+)*$")) {
      return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorResponse(ReasonError.MALFORMED_NAME)).build();
    }

    /*
     * Si el apellido NO tiene una longitud de entre 3 y 30 caracteres
     * alfabeticos, NO empieza con una letra mayuscula seguida de
     * letras minusculas, NO tiene un espacio en blanco entre apellido
     * y apellido en el caso en el que el usuario tenga mas de un apellido,
     * y los apellidos que vienen a continuacion del primero NO empiezan
     * con una letra mayuscula seguida de letras minusculas, la aplicacion
     * del lado servidor retorna el mensaje HTTP 400 (Bad request) junto con
     * el mensaje "El apellido debe tener una longitud de entre 3 y 30 caracteres
     * alfabeticos sin simbolos de acentuacion, empezar con una letra mayuscula
     * seguido de letras minusculas, tener un espacio en blanco entre apellido y
     * apellido si hay mas de un apellido, y los apellidos que vienen despues del
     * primero deben empezar con una letra mayuscula seguido de letras minusculas"
     * y no se realiza la operacion solicitada
     */
    if (!newUserData.getLastName().matches("^[A-Z](?=.{2,29}$)[a-z]+(?:\\h[A-Z][a-z]+)*$")) {
      return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorResponse(ReasonError.MALFORMED_LAST_NAME)).build();
    }

    /*
     * Si la direccion de correo electronico NO es valida, la aplicacion
     * del lado servidor retorna el mensaje HTTP 400 (Bad request) junto
     * con el mensaje "La direccion de correo electronico no es valida" y
     * no se realiza la operacion solicitada
     */
    if (!newUserData.getEmail().matches("^(?=.{1,64}@)[\\p{L}0-9_-]+(\\.[\\p{L}0-9_-]+)*@[^-][\\p{L}0-9-]+(\\.[\\p{L}0-9-]+)*(\\.[\\p{L}]{2,})$")) {
      return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorResponse(ReasonError.MALFORMED_EMAIL)).build();
    }

    /*
     * Si la contraseña NO contiene como minimo 8 caracteres de longitud,
     * una letra minuscula, una letra mayuscula y un numero 0 a 9, la
     * aplicacion del lado servidor retorna el mensaje HTTP 400 (Bad request)
     * junto con el mensaje "La contraseña debe tener como minimo 8
     * caracteres de longitud, una letra minuscula, una letra mayuscula y un
     * numero de 0 a 9, con o sin caracteres especiales" y no se realiza
     * la operacion solicitada
     */
    if (!newUserData.getPassword().matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{7,}$")) {
      return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorResponse(ReasonError.MALFORMED_PASSWORD)).build();
    }

    /*
     * ********************************************************************************
     * Control sobre la igualdad entre la contraseña y la confirmacion de la contraseña
     * ********************************************************************************
     */

    /*
     * Si la contraseña y la confirmacion de la contraseña NO
     * coinciden, la aplicacion del lado servidor retorna el mensaje
     * HTTP 400 (Bad request) junto con el mensaje "La confirmacion de
     * la contraseña no es igual a la contraseña ingresada" y no se
     * realiza la operacion solicitada
     */
    if (!newUserData.getPassword().equals(newUserData.getPasswordConfirmed())) {
      return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorResponse(ReasonError.INCORRECTLY_CONFIRMED_PASSWORD)).build();
    }

    /*
     * ****************************************************************************************
     * Controles sobre la existencia del nombre de usuario y la direccion de correo electronico
     * ****************************************************************************************
     */

    /*
     * Si el nombre de usuario ingresado en el formulario de
     * registro de usuario, existe en la base de datos subyacente,
     * la aplicacion del lado servidor retorna el mensaje HTTP 400
     * (Bad request) junto con el mensaje "Nombre de usuario ya
     * utilizado, elija otro" y no se realiza la operacion solicitada
     */
    if (userService.checkExistenceUsername(newUserData.getUsername())) {
      return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorResponse(ReasonError.USERNAME_ALREADY_USED)).build();
    }

    /*
     * Si la direccion de correo electronico ingresada en el formulario
     * de registro de usuario, existe en la base de datos subyacente,
     * la aplicacion del lado servidor retorna el mensaje HTTP 400
     * (Bad request) junto con el mensaje "Correo electronico ya
     * utilizado, elija otro" y no se realiza la operacion solicitada
     */
    if (userService.checkExistenceEmail(newUserData.getEmail())) {
      return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorResponse(ReasonError.EMAIL_ALREADY_USED)).build();
    }

    /*
     * Si se cumplen todos los controles, se asignan los datos
     * ingresados por el usuario a un objeto de tipo User
     * referenciado por la referencia contenida en la variable
     * de tipo por referencia newUser de tipo User, y luego se
     * persiste el usuario en la base de datos subyacente
     */
    User newUser = new User();
    setUser(newUser, newUserData);

    /*
     * El metodo create de la clase UserServiceBean retorna una
     * referencia a un objeto de tipo User que contiene el ID
     * resultante de persistir un objeto de tipo User
     */
    newUser = userService.create(newUser);

    /*
     * Se persiste en la base de datos subyacente un enlace de
     * activacion de cuenta para el usuario registrado
     */
    accountActivationLinkService.create(newUser);

    /*
     * Si se cumplen todos los controles, se envia un correo
     * electronico de confirmacion de registro a la direccion
     * de correo electronico ingresada por el usuario en el
     * formulario de registro
     */
    Email.sendConfirmationEmail(newUser.getEmail());

    /*
     * Si se cumplen todos los controles, la aplicacion del
     * lado servidor retorna el mensaje HTTP 200 (Ok)
     */
    return Response.status(Response.Status.OK).build();
  }

  /**
   * Asigna los valores ingresados por el usuario en el formulario
   * de registro, a un objeto de tipo User referenciado por la
   * referencia contenida en la variable de tipo por referencia
   * newUser de tipo User.
   * 
   * @param newUser
   * @param newUserData
   */
  private void setUser(User newUser, SignupFormData newUserData) {
    newUser.setUsername(newUserData.getUsername());
    newUser.setName(newUserData.getName());
    newUser.setLastName(newUserData.getLastName());
    newUser.setEmail(newUserData.getEmail());
    newUser.setPassword(userService.getPasswordHash(newUserData.getPassword()));
  }

}
