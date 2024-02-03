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
import model.Region;
import stateless.RegionServiceBean;
import stateless.SecretKeyServiceBean;
import stateless.SessionServiceBean;
import stateless.Page;
import util.ErrorResponse;
import util.PersonalizedResponse;
import util.ReasonError;
import util.RequestManager;
import util.SourceUnsatisfiedResponse;
import utilJwt.AuthHeaderManager;
import utilJwt.JwtManager;

@Path("/regions")
public class RegionRestServlet {

    @EJB RegionServiceBean regionService;
    @EJB SecretKeyServiceBean secretKeyService;
    @EJB SessionServiceBean sessionService;

    // Mapea lista de pojo a JSON
    ObjectMapper mapper = new ObjectMapper();

    /*
     * Expresion regular para validar el nombre de una
     * region
     */
    private final String NAME_REGULAR_EXPRESSION = "^[A-Za-zÀ-ÿ]+[.]{0,1}[,]{0,1}[;]{0,1}(\\s[A-Za-zÀ-ÿ]+[.]{0,1}[,]{0,1}[;]{0,1})*$";
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
         * Si el valor del encabezado de autorizacion de la peticion HTTP
         * dada, tiene un JWT valido, la aplicacion del lado servidor
         * devuelve el mensaje HTTP 200 (Ok) junto con los datos solicitados
         * por el cliente
         */
        return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(regionService.findAll())).build();
    }

    @GET
    @Path("/findAllPagination")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findAllPagination(@Context HttpHeaders request, @QueryParam("page") Integer page, @QueryParam("cant") Integer cant) throws IOException {
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
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.NO_ACTIVE_SESSION))).build();
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
         * Si el valor del encabezado de autorizacion de la peticion HTTP
         * dada, tiene un JWT valido, la aplicacion del lado servidor
         * devuelve el mensaje HTTP 200 (Ok) junto con los datos solicitados
         * por el cliente
         */
        Page<Region> regions = regionService.findAllPagination(page, cant, null);
        return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(regions)).build();
    }

    @GET
    @Path("/search")
    public Response search(@Context HttpHeaders request, @QueryParam("regionName") String regionName) throws IOException {
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
         * Si el nombre del dato correspondiente a esta clase NO
         * esta definido, la aplicacion del lado servidor retorna
         * el mensaje HTTP 400 (Bad request) junto con el mensaje
         * "El nombre de <dato> debe estar definido" y no se realiza
         * la operacion solicitada
         */
        if (regionName == null || regionName.equals(UNDEFINED_VALUE)) {
            return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.UNDEFINED_REGION_NAME))).build();
        }

        /*
         * Si el dato solicitado no existe en la base de datos
         * subyacente, la aplicacion del lado servidor devuelve
         * el mensaje HTTP 404 (Not found) junto con el mensaje
         * "<dato> inexistente" y la fuente u origen en donde
         * NO se pudo satisfacer la solicitud, y no se realiza
         * la operacion solicitada
         */
        if (!regionService.checkExistenceForSearch(regionName)) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.REGION_NOT_FOUND, SourceUnsatisfiedResponse.REGION)))
                    .build();
        }

        /*
         * Si el valor del encabezado de autorizacion de la peticion HTTP
         * dada, tiene un JWT valido, la aplicacion del lado servidor
         * devuelve el mensaje HTTP 200 (Ok) junto con los datos solicitados
         * por el cliente
         */
        return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(regionService.search(regionName))).build();
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
         * Si el dato solicitado no existe en la base de datos
         * subyacente, la aplicacion del lado servidor devuelve
         * el mensaje HTTP 404 (Not found) junto con el mensaje
         * "Recurso no encontrado" y no se realiza la operacion
         * solicitada
         */
        if (!regionService.checkExistence(id)) {
            return Response.status(Response.Status.NOT_FOUND).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.RESOURCE_NOT_FOUND))).build();
        }

        /*
         * Si el valor del encabezado de autorizacion de la peticion HTTP
         * dada, tiene un JWT valido, la aplicacion del lado servidor
         * devuelve el mensaje HTTP 200 (Ok) junto con los datos solicitados
         * por el cliente
         */
        return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(regionService.find(id))).build();
    }

    /*
     * Este metodo es necesario para el autocompletado de un campo
     * correspondiente a una region activa, como el campo de region
     * del formulario de creacion y modificacion de un cultivo, por
     * ejemplo.
     */
    @GET
    @Path("/findActiveRegionByName")
    public Response findActiveRegionByName(@Context HttpHeaders request, @QueryParam("regionName") String regionName) throws IOException {
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
         * Si el valor del encabezado de autorizacion de la peticion HTTP
         * dada, tiene un JWT valido, la aplicacion del lado servidor
         * devuelve el mensaje HTTP 200 (Ok) junto con los datos solicitados
         * por el cliente
         */
        return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(regionService.findActiveRegionByName(regionName))).build();
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

        Region newRegion = mapper.readValue(json, Region.class);

        /*
         * Si el nombre de la region a crear NO esta definido, la
         * aplicacion del lado servidor retorna el mensaje HTTP
         * 400 (Bad request) junto con el mensaje "El nombre de
         * la region debe estar definido" y no se realiza la
         * operacion solicitada
         */
        if (newRegion.getName() == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.UNDEFINED_REGION_NAME))).build();
        }

        /*
         * Si el nombre de la region NO contiene unicamente letras, y un
         * espacio en blanco entre palabra y palabra si esta formado por
         * mas de una palabra, la aplicacion del lado servidor retorna el
         * mensaje HTTP 400 (Bad request) junto con el mensaje "El nombre
         * de una region debe empezar con una palabra formada unicamente
         * por caracteres alfabeticos y puede tener más de una palabra
         * formada unicamente por caracteres alfabeticos. Se permite el
         * uso del punto para abreviar nombres, y el uso de la coma, y
         * el punto y coma como separadores." y no se realiza la operacion
         * solicitada
         */
        if (!newRegion.getName().matches(NAME_REGULAR_EXPRESSION)) {
            return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.INVALID_REGION_NAME))).build();
        }

        /*
         * Si en la base de datos subyacente existe una region con el
         * nombre de la region nueva, la aplicacion del lado servidor
         * retorna el mensaje HTTP 400 (Bad request) junto con el
         * mensaje "Nombre de region ya utilizado, elija otro" y
         * no se realiza la operacion solicitada
         */
        if (regionService.checkExistence(newRegion.getName())) {
            return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.REGION_NAME_ALREADY_USED))).build();
        }

        /*
         * Si el valor del encabezado de autorizacion de la peticion HTTP
         * dada, tiene un JWT valido, la aplicacion del lado servidor
         * devuelve el mensaje HTTP 200 (Ok) junto con los datos que el
         * cliente solicito persistir
         */
        return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(regionService.create(newRegion))).build();
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
         * Si el dato solicitado no existe en la base de datos
         * subyacente, la aplicacion del lado servidor devuelve
         * el mensaje HTTP 404 (Not found) junto con el mensaje
         * "Recurso no encontrado" y no se realiza la operacion
         * solicitada
         */
        if (!regionService.checkExistence(id)) {
            return Response.status(Response.Status.NOT_FOUND).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.RESOURCE_NOT_FOUND))).build();
        }

        /*
         * Si el valor del encabezado de autorizacion de la peticion HTTP
         * dada, tiene un JWT valido, la aplicacion del lado servidor
         * devuelve el mensaje HTTP 200 (Ok) junto con los datos que el
         * cliente solicito eliminar
         */
        return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(regionService.remove(id))).build();
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
         * Si el dato solicitado no existe en la base de datos
         * subyacente, la aplicacion del lado servidor devuelve
         * el mensaje HTTP 404 (Not found) junto con el mensaje
         * "Recurso no encontrado" y no se realiza la operacion
         * solicitada
         */
        if (!regionService.checkExistence(id)) {
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

        Region modifiedRegion = mapper.readValue(json, Region.class);

        /*
         * Si el nombre de la region a modificar NO esta definido,
         * la aplicacion del lado servidor retorna el mensaje HTTP
         * 400 (Bad request) junto con el mensaje "El nombre de
         * la region debe estar definido" y no se realiza la
         * operacion solicitada
         */
        if (modifiedRegion.getName() == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.UNDEFINED_REGION_NAME))).build();
        }

        /*
         * Si el nombre de la region NO contiene unicamente letras, y un
         * espacio en blanco entre palabra y palabra si esta formado por
         * mas de una palabra, la aplicacion del lado servidor retorna el
         * mensaje HTTP 400 (Bad request) junto con el mensaje "El nombre
         * de una region debe empezar con una palabra formada unicamente
         * por caracteres alfabeticos y puede tener más de una palabra
         * formada unicamente por caracteres alfabeticos. Se permite el
         * uso del punto para abreviar nombres, y el uso de la coma, y
         * el punto y coma como separadores." y no se realiza la operacion
         * solicitada
         */
        if (!modifiedRegion.getName().matches(NAME_REGULAR_EXPRESSION)) {
            return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.INVALID_REGION_NAME))).build();
        }

        /*
         * Si la region modificada tiene un nombre igual al nombre de
         * otra region, la aplicacion del lado servidor retorna el
         * mensaje HTTP 400 (Bad request) junto con el mensaje
         * "Nombre de región ya utilizado, elija otro" y no se
         * realiza la operacion solicitada
         */
        if (regionService.checkRepeated(id, modifiedRegion.getName())) {
            return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.REGION_NAME_ALREADY_USED))).build();
        }

        /*
         * Si el valor del encabezado de autorizacion de la peticion HTTP
         * dada, tiene un JWT valido, la aplicacion del lado servidor
         * devuelve el mensaje HTTP 200 (Ok) junto con los datos que el
         * cliente solicito actualizar
         */
        return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(regionService.modify(id, modifiedRegion))).build();
    }

}
