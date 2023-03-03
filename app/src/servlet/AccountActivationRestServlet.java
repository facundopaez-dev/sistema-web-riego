package servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import javax.ejb.EJB;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.PathParam;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import stateless.AccountActivationLinkServiceBean;
import stateless.UserServiceBean;
import util.ReasonError;
import util.ErrorResponse;
import util.ReasonSuccess;
import util.SuccessfullyResponse;

@Path("/activateAccount")
public class AccountActivationRestServlet {

  @EJB AccountActivationLinkServiceBean accountActivationLinkService;
  @EJB UserServiceBean userService;

  // Mapea lista de pojo a JSON
  ObjectMapper mapper = new ObjectMapper();

  @PUT
  @Path("/{email}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response activateUser(@PathParam("email") String email) throws IOException {
    /*
     * Si en la base de datos subyacente NO existe un enlace de
     * activacion de cuenta con la direccion de correo electronico
     * dada, la aplicacion del lado servidor retorna el mensaje HTTP
     * 400 (Bad request) y no se realiza la operacion solicitada
     */
    if (!accountActivationLinkService.checkExistence(email)) {
      return Response.status(Response.Status.BAD_REQUEST).build();
    }

    /*
     * Si el enlace de activacion de cuenta correspondiente a la
     * direccion de correo electronico dada, fue consumido (es decir,
     * fue accedido antes de su tiempo de expiracion), la aplicacion
     * del lado servidor retorna el mensaje HTTP 400 (Bad request)
     * y no se realiza la operacion solicitada
     */
    if (accountActivationLinkService.checkConsumed(email)) {
      return Response.status(Response.Status.BAD_REQUEST).build();
    }

    /*
     * Si el enlace de activacion de cuenta correspondiente a la
     * direccion de correo electronico dada, expiro, la aplicacion
     * del lado servidor retorna el mensaje HTTP 400 (Bad request)
     * junto con el mensaje "Enlace de activacion de cuenta expirado,
     * vuelva a registrarse" y no se relaiza la operacion solicitada
     */
    if (accountActivationLinkService.checkExpiration(email)) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.ACCOUNT_ACTIVATION_LINK_EXPIRED))).build();
    }

    /*
     * Si se cumplen las validaciones para la activacion de la
     * cuenta de un usuario, se marca el enlace de activacion
     * correspondiente como consumido y se activa la cuenta
     * del usuario
     */
    accountActivationLinkService.setConsumed(email);
    userService.activateUser(email);

    /*
     * Si se cumplen las validaciones, se activa la cuenta del
     * usuario correspondiente al enlace de activacion que
     * realiza la peticion a este metodo REST, y la aplicacion
     * del lado servidor retorna el mensaje HTTP 200 (Ok)
     * junto con el mensaje "Cuenta satisfactoriamente activada"
     */
    return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(new SuccessfullyResponse(ReasonSuccess.ACCOUNT_ACTIVATED))).build();
  }

}
