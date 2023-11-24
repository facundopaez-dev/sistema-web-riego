package servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import javax.ejb.EJB;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import stateless.OptionServiceBean;
import stateless.UserServiceBean;
import stateless.SecretKeyServiceBean;
import stateless.SessionServiceBean;
import util.ErrorResponse;
import util.ReasonError;
import util.RequestManager;
import utilJwt.AuthHeaderManager;
import utilJwt.JwtManager;
import model.Option;

@Path("/user/option")
public class OptionRestServlet {

    @EJB OptionServiceBean optionService;
    @EJB UserServiceBean userService;
    @EJB SecretKeyServiceBean secretKeyService;
    @EJB SessionServiceBean sessionService;

    // Mapea lista de pojo a JSON
    ObjectMapper mapper = new ObjectMapper();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response find(@Context HttpHeaders request) throws IOException {
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
         * Si el usuario que solicita esta operacion NO tiene una
         * sesion activa, la aplicacion del lador servidor devuelve
         * el mensaje 401 (Unauthorized) junto con el mensaje "No
         * tiene una sesion activa" y no se realiza la operacion
         * solicitada
         */
        if (!sessionService.checkActiveSession(userId)) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(new ErrorResponse(ReasonError.NO_ACTIVE_SESSION)).build();
        }

        int optionId = userService.find(userId).getOption().getId();

        /*
         * Si el valor del encabezado de autorizacion de la peticion HTTP
         * dada, tiene un JWT valido, la aplicacion del lado servidor
         * devuelve el mensaje HTTP 200 (Ok) junto con los datos solicitados
         * por el cliente
         */
        return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(optionService.find(optionId))).build();
    }

    @PUT
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
         * Si el objeto de tipo String referenciado por la
         * referencia contenida en la variable de tipo por
         * referencia json de tipo String, esta vacio,
         * significa que el usuario intento modificar un
         * modelo de dato correspondiente a esta clase con
         * datos indefinidos. Por lo tanto, la aplicacion del
         * lado servidor retorna el mensaje HTTP 400 (Bad request)
         * junto con el mensaje "Debe completar todos los campos
         * del formulario" y no se realiza la operacion solicitada
         */
        if (json.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorResponse(ReasonError.EMPTY_FORM)).build();
        }

        int optionId = userService.find(userId).getOption().getId();
        Option modifiedOption = mapper.readValue(json, Option.class);

        /*
         * Si la cantidad de dias pasados tomados como referencia para
         * calcular la necesidad de agua de riego en la fecha actual de
         * un cultivo en desarrollo es estrictamente menor al limite
         * inferior o estrictamente mayor al limite superior, la aplicacion
         * del lado servidor retorna el mensaje HTTP 400 (Bad request)
         * junto con el mensaje "La cantidad de días anteriores a la
         * fecha actual utilizados como referencia para el cálculo de la
         * necesidad de agua de riego de un cultivo en la fecha actual
         * debe ser un número entre 1 y 7" y no se realiza la operacion
         * solicitada
         */
        if (!optionService.validatePastDaysReference(modifiedOption)) {
            return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.INVALID_PAST_DAYS_REFERENCE))).build();
        }

        /*
         * Si el valor del encabezado de autorizacion de la peticion HTTP
         * dada, tiene un JWT valido, y se pasan todos los controles para
         * la modificacion del dato correspondiente a este bloque de codigo
         * de esta API REST, la aplicacion del lado servidor devuelve el
         * mensaje HTTP 200 (Ok) junto con el dato que el cliente solicito
         * modificar
         */
        return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(optionService.modify(optionId, modifiedOption))).build();
    }

}
