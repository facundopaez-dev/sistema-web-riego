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
import javax.ws.rs.PathParam;
import stateless.OptionServiceBean;
import stateless.ParcelServiceBean;
import stateless.PlantingRecordServiceBean;
import stateless.SecretKeyServiceBean;
import stateless.SessionServiceBean;
import util.ErrorResponse;
import util.ReasonError;
import util.PersonalizedResponse;
import util.RequestManager;
import utilJwt.AuthHeaderManager;
import utilJwt.JwtManager;
import model.Option;
import model.PlantingRecord;

@Path("/options")
public class OptionRestServlet {

    @EJB OptionServiceBean optionService;
    @EJB ParcelServiceBean parcelService;
    @EJB PlantingRecordServiceBean plantingRecordService;
    @EJB SecretKeyServiceBean secretKeyService;
    @EJB SessionServiceBean sessionService;

    // Mapea lista de pojo a JSON
    ObjectMapper mapper = new ObjectMapper();

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response find(@Context HttpHeaders request, @PathParam("id") int optionId) throws IOException {
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
            return Response.status(Response.Status.UNAUTHORIZED).entity(new ErrorResponse(ReasonError.NO_ACTIVE_SESSION)).build();
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
            return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorResponse(ReasonError.JWT_NOT_ASSOCIATED_WITH_ACTIVE_SESSION)).build();
        }

        /*
         * Si el valor del encabezado de autorizacion de la peticion HTTP
         * dada, tiene un JWT valido, la aplicacion del lado servidor
         * devuelve el mensaje HTTP 200 (Ok) junto con los datos solicitados
         * por el cliente
         */
        return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(optionService.find(optionId))).build();
    }

    @PUT
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response modify(@Context HttpHeaders request, @PathParam("id") int optionId, String json) throws IOException {
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
            return Response.status(Response.Status.UNAUTHORIZED).entity(new ErrorResponse(ReasonError.NO_ACTIVE_SESSION)).build();
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
            return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorResponse(ReasonError.JWT_NOT_ASSOCIATED_WITH_ACTIVE_SESSION)).build();
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
         * debe ser un número entre <limite inferior> y <limite superior>"
         * y no se realiza la operacion solicitada
         */
        if (!optionService.validatePastDaysReference(modifiedOption)) {
            String message = "La cantidad de días anteriores a la fecha actual utilizados como referencia para el cálculo de la"
                    + "necesidad de agua de riego de un cultivo en la fecha actual debe ser un número entre "
                    + optionService.getLowerLimitPastDays() + " y " + optionService.getUpperLimitPastDays();

            PersonalizedResponse personalizedResponse = new PersonalizedResponse(message);
            return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(personalizedResponse)).build();
        }

        /*
         * Si la opcion suelo NO esta activa y la parcela correspondiente
         * a dicha opcion tiene un registro de plantacion en el estado
         * "En desarrollo", se actualiza la lamina total de agua disponible
         * (dt) [mm] y la lamina de riego optima (drop) (umbral de riego)
         * [mm] del mismo con 0 en la base de datos subyacente para
         * representar que NO se utiliza el algoritmo del calculo, con
         * datos de suelo o con suelo, de la necesidad de agua de riego
         * de un cultivo en la fecha actual [mm/dia]
         */
        if (!modifiedOption.getSoilFlag() && plantingRecordService.checkOneInDevelopment(parcelService.findByOptionId(optionId))) {
            PlantingRecord developingPlantingRecord = plantingRecordService.findInDevelopment(parcelService.findByOptionId(optionId));
            plantingRecordService.updateTotalAmountWaterAvailable(developingPlantingRecord.getId(), 0);
            plantingRecordService.updateOptimalIrrigationLayer(developingPlantingRecord.getId(), 0);
        }

        /*
         * Si la bandera suelo esta activa y la parcela correspondiente
         * a la opcion de esta bandera, NO tiene asignado un suelo, la
         * aplicacion del lado servidor retorna el mensaje HTTP 400 (Bad
         * request) junto con el mensaje "Para calcular la necesidad de
         * agua de riego de un cultivo en la fecha actual con datos de
         * suelo es necesario asignar un suelo a la parcela" y no se
         * realiza la operacion solicitada
         */
        if (modifiedOption.getSoilFlag() && !parcelService.checkSoil(optionId)) {
            return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.UNDEFINED_SOIL))).build();
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
