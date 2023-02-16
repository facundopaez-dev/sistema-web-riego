package servlet;

import java.io.IOException;
import javax.ejb.EJB;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import model.Session;
import stateless.SecretKeyServiceBean;
import stateless.SessionServiceBean;
import util.RequestManager;
import utilJwt.AuthHeaderManager;
import utilJwt.JwtManager;

@Path("/logout")
public class LogoutRestServlet {

  @EJB
  SecretKeyServiceBean secretKeyService;

  @EJB
  SessionServiceBean sessionService;

  @POST
  public Response logout(@Context HttpHeaders request) throws IOException {
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
     * Elimina logicamente la sesion activa del usuario que hizo la
     * peticion del cierre de su sesion. De esta manera, cuando un
     * usuario cierra su sesion activa, esta pasa a estar inactiva.
     * 
     * Si no se hace esta eliminacion, el usuario no podria abrir
     * otra sesion luego de cerrar, desde el frontend, la sesion
     * que tenia abierta, debido a que esta ultima seguiria activa.
     */
    sessionService.remove(userId);
    return Response.status(Response.Status.OK).build();
  }

}
