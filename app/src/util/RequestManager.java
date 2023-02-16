package util;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import model.SecretKey;
import utilJwt.AuthHeaderManager;
import utilJwt.JwtManager;

/**
 * RequestManager es la clase que se utiliza para comprobar
 * que el encabezado de autorizacion de una peticion HTTP
 * cumple con las siguientes condiciones:
 * - Esta presente.
 * - No esta vacio.
 * - Cumple con la convencion de JWT.
 * - El JWT que contiene es valido.
 */
public class RequestManager {

  /*
   * El metodo constructor tiene el modificador de acceso 'private'
   * para que ningun programador trate de instanciar esta clase
   * desde afuera, ya que todos los metodos publicos de la misma
   * son estaticos, con lo cual, no se requiere una instancia de
   * esta clase para invocar a sus metodos publicos
   */
  private RequestManager() {

  }

  /**
   * Comprueba que el encabezado de autorizacion de una peticion
   * HTTP sea valido, esto es que cumpla con las siguientes
   * condiciones:
   * - Esta presente.
   * - No esta vacio.
   * - Cumple con la convencion de JWT.
   * - El JWT que contiene es valido.
   *
   * @param request referencia de tipo HttpHeaders a traves de
   * la cual se obtiene el valor del encabezado de autorizacion
   * de una peticion HTTP
   * @param secretKey clave secreta con la que se firma un JWT
   * @return mensaje HTTP 400 (Bad request) si el encabezado de
   * autorizacion de una peticion HTTP no esta presente, esta
   * vacio o no cumple con la convencion de JWT. Mensaje HTTP
   * 401 (Unauthorized) si el JWT del encabezado de autorizacion
   * de una peticion HTTP no es valido. Mensaje HTTP 202 (Accepted)
   * si el encabezado de autorizacion de una peticion HTTP cumple
   * con las siguientes condiciones: esta presente, no esta
   * vacio, cumple con la convencion de JWT y contiene un JWT
   * valido.
   */
  public static Response validateAuthHeader(HttpHeaders request, SecretKey secretKey) {
    String authHeaderValue = AuthHeaderManager.getAuthHeaderValue(request);

    /*
     * Si el encabezado de autorizacion de la peticion HTTP dada,
     * NO esta presente en la misma, no existe el valor de dicho
     * encabezado. Por lo tanto, la aplicacion del lado servidor
     * devuelve el mensaje HTTP 400 (Bad Request).
     */
    if (!AuthHeaderManager.isPresent(authHeaderValue)) {
      return Response.status(Response.Status.BAD_REQUEST).build();
    }

    /*
     * Si el encabezado de autorizacion de la peticion HTTP dada,
     * esta presente en la misma, pero esta vacio, el valor de dicho
     * encabezado esta vacio. Por lo tanto, la aplicacion del lado
     * servidor devuelve el mensaje 400 (Bad Request).
     */
    if (AuthHeaderManager.isEmpty(authHeaderValue)) {
      return Response.status(Response.Status.BAD_REQUEST).build();
    }

    /*
     * Si el valor del encabezado de autorizacion de la peticion
     * dada, NO cumple con la convencion de JWT, la aplicacion del
     * lado servidor devuelve el mensaje HTTP 400 (Bad Request)
     */
    if (!AuthHeaderManager.checkJwtConvention(authHeaderValue)) {
      return Response.status(Response.Status.BAD_REQUEST).build();
    }

    String jwt = AuthHeaderManager.getJwt(authHeaderValue);

    /*
     * Si el JWT obtenido del valor del encabezado de autorizacion
     * de la peticion HTTP dada, expiro, la aplicacion del lado
     * servidor devuelve el mensaje HTTP 401 (Unauthorized) junto
     * con el motivo "Sesion expirada", el cual, esta contenido
     * en el enum ReasonError
     */
    if (JwtManager.isExpired(jwt, secretKey.getValue())) {
      return Response.status(Response.Status.UNAUTHORIZED).entity(new ErrorResponse(ReasonError.SESSION_EXPIRED)).build();
    }

    /*
     * Si la firma del JWT, obtenido del valor del encabezado de
     * autorizacion de la peticion HTTP, no coincide con los datos
     * de su encabezado y carga util, la aplicacion del lado
     * servidor devuelve el mensaje HTTP 401 (Unauthorized)
     */
    if (!JwtManager.validateJwt(jwt, secretKey.getValue())) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    /*
     * Si el encabezado de autorizacion de la peticion HTTP dada cumple
     * con las siguientes condiciones:
     * - Esta presente.
     * - No esta vacio.
     * - Cumple con la convencion de JWT.
     * - Contiene un JWT valido.
     * 
     * dicha peticion HTTP es valida, por lo tanto, se retorna una
     * respuesta que tiene el estado ACCEPTED
     */
    return Response.accepted().build();
  }

  /**
   * Retorna true si y solo si la respuesta dada tiene el estado
   * ACCEPTED.
   * 
   * Este metodo se utiliza para comprobar que el estado de la
   * respuesta obtenida de validar el encabezado de autorizacion
   * de una peticion HTTP, sea ACCEPTED. Si esto es asi, significa
   * que dicha peticion es valida, debido a que su encabezado de
   * autorizacion cumple con las siguientes condiciones:
   * - Esta presente.
   * - No esta vacio.
   * - Cumple con la convencion de JWT.
   * - Contiene un JWT valido.
   * 
   * En el caso en el que dicha respuesta no tiene el estado
   * ACCEPTED, significa que dicha peticion no es valida, debido
   * a que no cumple con las condiciones mencionadas.
   * 
   * @param givenResponse
   * @return true si la respuesta dada tiene el estado ACCEPTED,
   * false en caso contrario
   */
  public static boolean isAccepted(Response givenResponse) {
    return (givenResponse.getStatus() == Response.Status.ACCEPTED.getStatusCode());
  }

}
