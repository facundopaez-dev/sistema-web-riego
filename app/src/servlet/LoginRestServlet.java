package servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Calendar;
import javax.ejb.EJB;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import model.SecretKey;
import model.User;
import stateless.SecretKeyServiceBean;
import stateless.SessionServiceBean;
import stateless.UserServiceBean;
import util.ErrorResponse;
import util.ReasonError;
import utilJwt.JwtManager;
import utilJwt.Token;

@Path("/auth")
public class LoginRestServlet {

  @EJB
  UserServiceBean userService;

  @EJB
  SecretKeyServiceBean secretKeyService;

  @EJB
  SessionServiceBean sessionService;

  // Mapea lista de pojo a JSON
  ObjectMapper mapper = new ObjectMapper();

  @POST
  @Path("/user")
  @Produces(MediaType.APPLICATION_JSON)
  public Response authenticateUser(String json) throws IOException {
    /*
     * Para la autenticacion del usuario se deben hacer las siguientes comprobaciones:
     * 1. La inexistencia del usuario ingresado. Si el usuario ingresado no existe (debido a que no esta registrado en el sistema), el
     * servidor debe devolver como respuesta el mensaje HTTP 401 con el mensaje "Nombre de usuario o contraseña incorrectos".
     *
     * 2. La autenticacion del usuario. Si el nombre de usuario y la contraseña no son iguales a los almacenados en la base de datos
     * subyacente, el servidor debe devolver como respuesta el mensaje HTTP 401 (UNAUTHORIZED) con el mensaje "Nombre de usuario o contraseña incorrectos".
     * La autenticacion es el proceso por el cual se confirma que algo (en este caso un usuario) es realmente quien dice ser.
     *
     * Estas dos comprobaciones se realizan al hacer la autenticacion del usuario.
     */

    User givenUser = mapper.readValue(json, User.class);

    /*
     * Si el usuario que inicia sesion NO es autentico (es decir, el
     * nombre de usuario y la contraseña ingresados no coinciden con
     * ninguno de los que estan registrados en la base de datos
     * subyacente), la aplicacion del lado servidor retorna el mensaje
     * HTTP 401 (Unauthorized) junto con el mensaje "Nombre de usuario
     * o contraseña incorrectos" y no se inicia la sesion solicitada
     */
    if (!userService.authenticate(givenUser.getUsername(), givenUser.getPassword())) {
      return Response.status(Response.Status.UNAUTHORIZED).entity(new ErrorResponse(ReasonError.USERNAME_OR_PASSWORD_INCORRECT)).build();
    }

    /*
     * Se recupera el usuario completo de la base de datos subyacente,
     * ya que el usuario obtenido como resultado del mapeo JSON a POJO
     * solo contiene el nombre de usuario y la contraseña ingresados
     * por el usuario en la pagina web de inicio de sesion
     */
    givenUser = userService.findByUsername(givenUser.getUsername());

    /*
     * Si la cuenta con la que el usuario intenta iniciar sesion NO
     * esta activada, la aplicacion del lado servidor retorna el
     * mensaje HTTP 401 (Unauthorized) junto con el mensaje "Nombre
     * de usuario o contraseña incorrectos" y no se inicia la sesion
     * solicitada
     */
    if (!givenUser.getActive()) {
      return Response.status(Response.Status.UNAUTHORIZED).entity(new ErrorResponse(ReasonError.USERNAME_OR_PASSWORD_INCORRECT)).build();
    }

    /*
     * Si el usuario tiene una sesion abierta e intenta abrir otra
     * sesion, la aplicacion del lado servidor devuelve el mensaje
     * HTTP 401 (Unauthorized) junto con el mensaje "No es posible
     * tener mas de una sesion abierta simultaneamente" (contenido
     * en el enum ReasonError).
     * 
     * Para realizar este control se necesita el ID del usuario
     * recuperado de la base de datos subyacente y no el ID del
     * usuario obtenido mediante el mapeo de JSON a POJO, ya
     * que el primero tiene el ID correcto y el segundo siempre
     * tiene el ID igual a cero.
     * 
     * El motivo por el cual el ID del segundo es cero es que
     * id es una variable de instancia de tipo int de la clase
     * User, y las variables de instancia de tipo int siempre
     * se inicialiazan de manera automatica con 0.
     * 
     * Utilizar el ID del usuario obtenido mediante el mapeo
     * de JSON a POJO, hara que este control permita que un
     * usuario abra mas de una sesion con su cuenta, lo cual
     * no es lo que se busca con dicho control.
     */
    if (sessionService.checkActiveSession(givenUser.getId())) {
      return Response.status(Response.Status.UNAUTHORIZED).entity(new ErrorResponse(ReasonError.MULTIPLE_SESSIONS)).build();
    }

    /*
     * Si el flujo de ejecucion de este metodo llega a este punto, es
     * debido a que el usuario es autentico (es quien dice ser), la
     * cuenta con la que intenta iniciar sesion esta activada y no
     * tiene una sesion activa, por lo tanto, se inicia la sesion
     * solicitada
     */

    /*
     * Recupera de la base de datos subyacente la unica clave
     * secreta que hay para generar un JWT
     */
    SecretKey secretKey = secretKeyService.find();
    Token newToken = new Token(JwtManager.createJwt(givenUser.getId(), givenUser.getSuperuser(), secretKey.getValue()));

    /*
     * Se crea y persiste una sesion activa para el usuario
     * que la abre. Esto es necesario para el control de
     * prevencion de inicio de sesion multiple realizado
     * luego de obtener el usuario mediante su nombre.
     */
    sessionService
      .create(givenUser, JwtManager.getDateIssue(newToken.getJwt(), secretKey.getValue()), JwtManager.getExpirationDate(newToken.getJwt(), secretKey.getValue()));

    /*
     * Si el usuario es autentico y NO tiene una sesion activa, el servidor
     * devuelve el mensaje HTTP 200 (Ok) junto con un JWT que tiene el ID y
     * el permiso del usuario, una fecha de emision y una fecha de expiracion
     */
    return Response.status(Response.Status.OK).entity(newToken).build();
  }

  @POST
  @Path("/admin")
  @Produces(MediaType.APPLICATION_JSON)
  public Response authenticateAdmin(String json) throws IOException {
    /*
     * Para la autenticacion del administrador se deben hacer las siguientes comprobaciones:
     * 1. La inexistencia del usuario ingresado. Si el usuario ingresado no existe (debido a que no esta registrado en el sistema), el
     * servidor debe devolver como respuesta el mensaje HTTP 401 con el mensaje "Nombre de usuario o contraseña incorrectos".
     * 
     * 2. La autenticacion del usuario. Si el nombre de usuario y la contraseña no son iguales a los almacenados en la base de datos
     * subyacente, el servidor debe devolver como respuesta el mensaje HTTP 401 (UNAUTHORIZED) con el mensaje "Nombre de usuario o contraseña incorrectos".
     * La autenticacion es el proceso por el cual se confirma que algo (en este caso un usuario) es realmente quien dice ser.
     * 
     * 3. La posesion del permiso de super usuario (administrador). Si el usuario ingresado existe, es autentico y no tiene el permiso de
     * super usuario, el servidor debe devolver como respuesta el mensaje HTTP 403 (FORBIDDEN) con el mensaje "Acceso no autorizado".
     * 
     * Las primeras dos comprobaciones se realizan al hacer la autenticacion del usuario.
     */

    User givenUser = mapper.readValue(json, User.class);

    /*
     * Si el usuario que inicia sesion NO es autentico (es decir, el
     * nombre de usuario y la contraseña ingresados no coinciden con
     * ninguno de los que estan registrados en la base de datos
     * subyacente), la aplicacion del lado servidor retorna el mensaje
     * HTTP 401 (Unauthorized) junto con el mensaje "Nombre de usuario
     * o contraseña incorrectos" y no se inicia la sesion solicitada
     */
    if (!userService.authenticate(givenUser.getUsername(), givenUser.getPassword())) {
      return Response.status(Response.Status.UNAUTHORIZED).entity(new ErrorResponse(ReasonError.USERNAME_OR_PASSWORD_INCORRECT)).build();
    }

    /*
     * Se recupera el usuario completo de la base de datos subyacente,
     * ya que el usuario obtenido como resultado del mapeo JSON a POJO
     * solo contiene el nombre de usuario y la contraseña ingresados
     * por el usuario en la pagina web de inicio de sesion
     */
    givenUser = userService.findByUsername(givenUser.getUsername());

    /*
     * Si la cuenta con la que el usuario intenta iniciar sesion NO
     * esta activada, la aplicacion del lado servidor retorna el
     * mensaje HTTP 401 (Unauthorized) junto con el mensaje "Nombre
     * de usuario o contraseña incorrectos" y no se inicia la sesion
     * solicitada
     */
    if (!givenUser.getActive()) {
      return Response.status(Response.Status.UNAUTHORIZED).entity(new ErrorResponse(ReasonError.USERNAME_OR_PASSWORD_INCORRECT)).build();
    }

    /*
     * La comprobacion del permiso de super usuario (administrador) del usuario nunca
     * va a fallar en caso de que se ingrese un usuario inexistente, ya que primero
     * se realiza la autenticacion, la cual cubre el caso de que el usuario ingresado
     * no existe.
     * 
     * Por lo tanto, siempre y cuando se realice la autenticacion del usuario
     * y luego la comprobacion del permiso de super usuario, dicha comprobacion
     * nunca va a fallar en caso de que se ingrese un usuario inexistente.
     */
    if (!userService.checkSuperuserPermission(givenUser.getUsername())) {
      return Response.status(Response.Status.FORBIDDEN).entity(new ErrorResponse(ReasonError.UNAUTHORIZED_ACCESS)).build();
    }

    /*
     * Si el usuario tiene una sesion abierta e intenta abrir otra
     * sesion, la aplicacion del lado servidor devuelve el mensaje
     * HTTP 401 (Unauthorized) junto con el mensaje "No es posible
     * tener mas de una sesion abierta simultaneamente" (contenido
     * en el enum ReasonError).
     * 
     * Para realizar este control se necesita el ID del usuario
     * recuperado de la base de datos subyacente y no el ID del
     * usuario obtenido mediante el mapeo de JSON a POJO, ya
     * que el primero tiene el ID correcto y el segundo siempre
     * tiene el ID igual a cero.
     * 
     * El motivo por el cual el ID del segundo es cero es que
     * id es una variable de instancia de tipo int de la clase
     * User, y las variables de instancia de tipo int siempre
     * se inicialiazan de manera automatica con 0.
     * 
     * Utilizar el ID del usuario obtenido mediante el mapeo
     * de JSON a POJO, hara que este control permita que un
     * usuario abra mas de una sesion con su cuenta, lo cual
     * no es lo que se busca con dicho control.
     */
    if (sessionService.checkActiveSession(givenUser.getId())) {
      return Response.status(Response.Status.UNAUTHORIZED).entity(new ErrorResponse(ReasonError.MULTIPLE_SESSIONS)).build();
    }

    /*
     * Si el flujo de ejecucion de este metodo llega a este punto, es
     * debido a que el usuario es autentico (es quien dice ser), la
     * cuenta con la que intenta iniciar sesion esta activada, tiene
     * el permiso de super usuario (administrador) y no tiene una
     * sesion activa, por lo tanto, se inicia la sesion solicitada
     */

    /*
     * Recupera de la base de datos subyacente la unica clave
     * secreta que hay para generar un JWT
     */
    SecretKey secretKey = secretKeyService.find();
    Token newToken = new Token(JwtManager.createJwt(givenUser.getId(), givenUser.getSuperuser(), secretKey.getValue()));

    /*
     * Se crea y persiste una sesion activa para el usuario
     * que la abre. Esto es necesario para el control de
     * prevencion de inicio de sesion multiple realizado
     * luego de obtener el usuario mediante su nombre.
     */
    sessionService
      .create(givenUser, JwtManager.getDateIssue(newToken.getJwt(), secretKey.getValue()), JwtManager.getExpirationDate(newToken.getJwt(), secretKey.getValue()));

    /*
     * Si el usuario es autentico y NO tiene una sesion activa, el servidor
     * devuelve el mensaje HTTP 200 (Ok) junto con un JWT que tiene el ID y
     * el permiso del usuario, una fecha de emision y una fecha de expiracion
     */
    return Response.status(Response.Status.OK).entity(newToken).build();
  }

}
