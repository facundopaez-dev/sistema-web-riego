package servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import javax.ejb.EJB;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import model.PastDaysReference;
import stateless.PastDaysReferenceServiceBean;
import stateless.SecretKeyServiceBean;
import util.ErrorResponse;
import util.ReasonError;
import util.RequestManager;
import utilJwt.AuthHeaderManager;
import utilJwt.JwtManager;

@Path("/pastDaysReferences")
public class PastDaysReferenceServlet {

    @EJB
    PastDaysReferenceServiceBean pastDaysReferenceService;

    @EJB
    SecretKeyServiceBean secretKeyService;

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
        return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(pastDaysReferenceService.findAll(userId))).build();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response find(@Context HttpHeaders request, @PathParam("id") int pastDaysReferenceId) throws IOException {
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
        return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(pastDaysReferenceService.find(userId, pastDaysReferenceId))).build();
    }

    @PUT
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response modify(@Context HttpHeaders request, @PathParam("id") int pastDaysReferenceId, String json) throws IOException {
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
        if (!pastDaysReferenceService.checkExistence(pastDaysReferenceId)) {
            return Response.status(Response.Status.NOT_FOUND).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.RESOURCE_NOT_FOUND))).build();
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
         * Si al usuario que hizo esta peticion HTTP, no le pertenece
         * el dato solicitado, la aplicacion del lado servidor
         * devuelve el mensaje HTTP 403 (Forbidden) junto con el
         * mensaje "Acceso no autorizado" (contenido en el enum
         * ReasonError) y no se realiza la operacion solicitada
         */
        if (!pastDaysReferenceService.checkUserOwnership(userId, pastDaysReferenceId)) {
            return Response.status(Response.Status.FORBIDDEN).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.UNAUTHORIZED_ACCESS))).build();
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

        PastDaysReference modifiedPastDaysReference = mapper.readValue(json, PastDaysReference.class);
        int modifiedValue = modifiedPastDaysReference.getValue();

        /*
         * Si la cantidad de dias pasados tomados como referencia para
         * calcular la necesidad de agua de riego en la fecha actual de
         * un cultivo en desarrollo es estrictamente menor al limite
         * inferior o estrictamente mayor al limite superior, la aplicacion
         * del lado servidor retorna el mensaje HTTP 400 (Bad request)
         * junto con el mensaje "La cantidad de días pasados utilizados
         * como referencia para el cálculo de la necesidad de agua de
         * riego de un cultivo solo puede ser un número entre 1 y 7" y
         * no se realiza la operacion solicitada
         */
        if (modifiedValue < pastDaysReferenceService.getLowerLimitPastDays() || modifiedValue > pastDaysReferenceService.getUpperLimitPastDays()) {
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
        return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(pastDaysReferenceService.modify(userId, pastDaysReferenceId, modifiedPastDaysReference))).build();
    }

}
