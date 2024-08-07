package servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.Calendar;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import javax.ejb.EJB;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import stateless.SoilWaterBalanceServiceBean;
import stateless.SecretKeyServiceBean;
import stateless.SessionServiceBean;
import stateless.ParcelServiceBean;
import stateless.Page;
import util.ErrorResponse;
import util.ReasonError;
import util.RequestManager;
import util.UtilDate;
import utilJwt.AuthHeaderManager;
import utilJwt.JwtManager;
import model.Parcel;
import model.SoilWaterBalance;

@Path("/soilWaterBalances")
public class SoilWaterBalanceRestServlet {

    @EJB SoilWaterBalanceServiceBean soilWaterBalanceService;
    @EJB SecretKeyServiceBean secretKeyService;
    @EJB SessionServiceBean sessionService;
    @EJB ParcelServiceBean parcelService;

    // mapea lista de pojo a JSON
    ObjectMapper mapper = new ObjectMapper();

    private final String UNDEFINED_VALUE = "undefined";
    private final String NULL_VALUE = "null";

    @GET
    @Path("/findAllPagination")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findAllPagination(@Context HttpHeaders request, @QueryParam("page") Integer page,
            @QueryParam("cant") Integer cant, @QueryParam("search") String search) throws IOException, ParseException {
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

        Map<String, String> map = new HashMap<String, String>();

        // Convert JSON string to Map
        map = mapper.readValue(search, new TypeReference<Map<String, String>>() {});

        /*
         * Si el valor del encabezado de autorizacion de la peticion HTTP
         * dada, tiene un JWT valido, la aplicacion del lado servidor
         * devuelve el mensaje HTTP 200 (Ok) junto con los datos solicitados
         * por el cliente
         */
        return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(soilWaterBalanceService.findAllPagination(userId, page, cant, map))).build();
    }

    @GET
    @Path("/filter")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findByFilterParameters(@Context HttpHeaders request, @QueryParam("dateFrom") String stringDateFrom,
            @QueryParam("dateUntil") String stringDateUntil, @QueryParam("parcelName") String parcelName,
            @QueryParam("cropName") String cropName) throws IOException, ParseException {
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
         * Si el nombre de la parcela y el nombre del cultivo NO estan definidos,
         * la aplicacion del lado servidor retorna el mensaje HTTP 400 (Bad request)
         * junto con el mensaje "La parcela y el cultivo deben estar definidos" y
         * no se realiza la operacion solicitada
         */
        if (parcelName == null || cropName == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.UNDEFINED_PARCEL_NAME_AND_CROP_NAME))).build();
        }

        /*
         * Si el usuario que realiza esta peticion NO tiene una
         * parcela con el nombre elegido, la aplicacion del lado
         * servidor retorna el mensaje HTTP 404 (Resource not found)
         * junto con el mensaje "La parcela seleccionada no existe"
         * y no se realiza la peticion solicitada
         */
        if (!parcelService.checkExistence(userId, parcelName)) {
            return Response.status(Response.Status.NOT_FOUND).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.NON_EXISTENT_PARCEL))).build();
        }

        /*
         * En la aplicacion del lado del navegador Web las variables
         * correspondientes a la fecha desde y a la fecha hasta pueden
         * tener el valor undefined o el valor null. Estos valores
         * representan que una variable no tiene un valor asignado.
         * En este caso indican que una variable no tiene asignada
         * una fecha. Cuando esto ocurre y se realiza una peticion
         * HTTP a este metodo con una de estas variables con uno de
         * dichos dos valores, las variables de tipo String stringDateFrom
         * y stringDateUntil tienen como contenido la cadena "undefined"
         * o la cadena "null". En Java para representar adecuadamente
         * que la fecha desde y/o la fecha hasta NO tienen una fecha
         * asignada, en caso de que provengan de la aplicacion del
         * lado del navegador web con el valor undefined o el valor
         * null, se debe asignar el valor null a las variables
         * stringDateFrom y stringDateUntil.
         */
        if (stringDateFrom != null && (stringDateFrom.equals(UNDEFINED_VALUE) || stringDateFrom.equals(NULL_VALUE))) {
            stringDateFrom = null;
        }

        if (stringDateUntil != null && (stringDateUntil.equals(UNDEFINED_VALUE) || stringDateUntil.equals(NULL_VALUE))) {
            stringDateUntil = null;
        }

        Parcel parcel = parcelService.find(userId, parcelName);
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        Date dateFrom;
        Date dateUntil;

        Collection<SoilWaterBalance> soilWaterBalances = soilWaterBalanceService.findAllByParcelIdAndCropName(parcel.getId(), cropName);

        /*
         * Actualiza instancias de tipo SoilWaterBalance desde la base
         * de datos subyacente, sobrescribiendo los cambios realizados
         * en ellas, si los hubiere. Esto se realiza para recuperar
         * los balances hidricos de suelo con los datos con las fechas
         * con las que estan almacenados en la base de datos subyacente.
         * De lo contrario, se los obtiene con fechas distintas a las
         * fechas con las que estan almacenados.
         */
        soilWaterBalanceService.refreshSoilWaterBalances(soilWaterBalances);

        /*
         * Siempre y cuando no se elimine el control por el nombre
         * de parcela y el nombre del cultivo indefinidos, si la
         * fecha desde y la fecha hasta NO estan definidas, la
         * aplicacion del lado servidor retorna una coleccion de
         * balances hidricos de suelo asociados a un usuario que
         * tienen un nombre de parcela y un nombre de cultivo
         */
        if (stringDateFrom == null && stringDateUntil == null) {
            return Response.status(Response.Status.OK)
                    .entity(mapper.writeValueAsString(soilWaterBalanceService.findAllByParcelIdAndCropName(parcel.getId(), cropName)))
                    .build();
        }

        /*
         * Si la fecha desde elegida es estrictamente mayor al año
         * maximo (9999), la aplicacion del lado servidor retorna
         * el mensaje HTTP 400 (Bad request) junto con el mensaje
         * "La fecha desde no debe ser estrictamente mayor a 9999"
         * y no se realiza la operacion solicitada
         */
        if (stringDateFrom != null) {
            dateFrom = new Date(dateFormatter.parse(stringDateFrom).getTime());

            if (UtilDate.yearIsGreaterThanMaximum(UtilDate.toCalendar(dateFrom))) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.DATE_FROM_GREATEST_TO_MAXIMUM)))
                        .build();
            }

        }

        /*
         * Si la fecha desde elegida es estrictamente mayor al año
         * maximo (9999), la aplicacion del lado servidor retorna
         * el mensaje HTTP 400 (Bad request) junto con el mensaje
         * "La fecha hasta no debe ser estrictamente mayor a 9999"
         * y no se realiza la operacion solicitada
         */
        if (stringDateUntil != null) {
            dateUntil = new Date(dateFormatter.parse(stringDateUntil).getTime());

            if (UtilDate.yearIsGreaterThanMaximum(UtilDate.toCalendar(dateUntil))) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.DATE_UNTIL_GREATEST_TO_MAXIMUM)))
                        .build();
            }

        }

        /*
         * Siempre y cuando no se elimine el control por el nombre
         * de parcela y el nombre del cultivo indefinidos, si la
         * fecha desde esta definida y la fecha hasta NO esta
         * definida, la aplicacion del lado servidor retorna una
         * coleccion de balances hidricos de suelo asociados a un
         * usuario que tienen un nombre de parcela, un nombre de
         * cultivo y una fecha mayor o igual a una fecha desde
         */
        if (stringDateFrom != null && stringDateUntil == null) {
            dateFrom = new Date(dateFormatter.parse(stringDateFrom).getTime());
            return Response.status(Response.Status.OK)
                    .entity(mapper.writeValueAsString(soilWaterBalanceService.findAllByDateGreaterThanOrEqual(parcel.getId(), cropName, UtilDate.toCalendar(dateFrom))))
                    .build();
        }

        /*
         * Siempre y cuando no se elimine el control por el nombre
         * de parcela y el nombre del cultivo indefinidos, si la
         * fecha desde NO esta definida y la fecha hasta esta
         * definida, la aplicacion del lado servidor retorna una
         * coleccion de balances hidricos de suelo asociados a un
         * usuario que tienen un nombre de parcela, un nombre de
         * cultivo y una fecha menor o igual a una fecha hasta
         */
        if (stringDateFrom == null && stringDateUntil != null) {
            dateUntil = new Date(dateFormatter.parse(stringDateUntil).getTime());
            return Response.status(Response.Status.OK)
                    .entity(mapper.writeValueAsString(soilWaterBalanceService.findAllByDateLessThanOrEqual(parcel.getId(), cropName, UtilDate.toCalendar(dateUntil))))
                    .build();
        }

        /*
         * Si la fecha desde y la fecha hasta estan definidas, y la
         * fecha desde NO es estrictamente mayor a la fecha hasta, y
         * el nombre de la parcela y el nombre del cultivo estan definidos,
         * la aplicacion del lado del servidor retorna una coleccion
         * de balances hidricos de suelo asociados a un usuario que
         * tienen una fecha mayor o igual a una fecha desde y menor
         * o igual a una fecha hasta, un nombre de parcela y un nombre
         * de cultivo
         */
        dateFrom = new Date(dateFormatter.parse(stringDateFrom).getTime());
        dateUntil = new Date(dateFormatter.parse(stringDateUntil).getTime());

        Calendar dateFromCalendar = UtilDate.toCalendar(dateFrom);
        Calendar dateUntilCalendar = UtilDate.toCalendar(dateUntil);

        /*
         * Si la fecha desde es estrictamente mayor a la fecha hasta, la
         * aplicacion del lado servidor retorna el mensaje HTTP 400 (Bad
         * request) junto con el mensaje "La fecha desde no debe ser
         * estrictamente mayor a la fecha hasta" y no se realiza la
         * operacion solicitada
         */
        if (UtilDate.compareTo(dateFromCalendar, dateUntilCalendar) > 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.DATE_FROM_STRICTLY_GREATER_THAN_DATE_UNTIL)))
                    .build();
        }

        /*
         * Si el valor del encabezado de autorizacion de la peticion
         * HTTP dada, tiene un JWT valido, la aplicacion del lado
         * servidor devuelve el mensaje HTTP 200 (Ok) junto con los
         * datos solicitados por el cliente
         */
        return Response.status(Response.Status.OK)
                .entity(mapper.writeValueAsString(soilWaterBalanceService.findByAllFilterParameters(parcel.getId(),
                        cropName, dateFromCalendar, dateUntilCalendar)))
                .build();
    }

}
