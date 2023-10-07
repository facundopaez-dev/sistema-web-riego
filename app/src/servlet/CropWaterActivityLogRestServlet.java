package servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
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
import stateless.CropWaterActivityLogServiceBean;
import stateless.SecretKeyServiceBean;
import util.ErrorResponse;
import util.ReasonError;
import util.RequestManager;
import util.UtilDate;
import utilJwt.AuthHeaderManager;
import utilJwt.JwtManager;

@Path("/cropWaterActivityLogs")
public class CropWaterActivityLogRestServlet {

    @EJB CropWaterActivityLogServiceBean cropWaterActivityLogService;

    @EJB SecretKeyServiceBean secretKeyService;

    // mapea lista de pojo a JSON
    ObjectMapper mapper = new ObjectMapper();

    private final String UNDEFINED_VALUE = "undefined";
    private final String NULL_VALUE = "null";

    @GET
    @Path("/filter")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findByFilterParameters(@Context HttpHeaders request, @QueryParam("dateFrom") String stringDateFrom,
            @QueryParam("dateUntil") String stringDateUntil, @QueryParam("parcelName") String parcelName,
            @QueryParam("cropName") String cropName) throws IOException, ParseException {
        Response givenResponse = RequestManager.validateAuthHeader(request, secretKeyService.find());
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        Date dateFrom;
        Date dateUntil;

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
         * lado del servidor con el valor undefined o el valor null,
         * se debe asignar el valor null a las variables stringDateFrom
         * y stringDateUntil.
         */
        if (stringDateFrom != null && (stringDateFrom.equals(UNDEFINED_VALUE) || stringDateFrom.equals(NULL_VALUE))) {
            stringDateFrom = null;
        }

        if (stringDateUntil != null && (stringDateUntil.equals(UNDEFINED_VALUE) || stringDateUntil.equals(NULL_VALUE))) {
            stringDateUntil = null;
        }

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
         * Si el nombre de la parcela y el nombre del cultivo NO estan definidos,
         * la aplicacion del lado servidor retorna el mensaje HTTP 400 (Bad request)
         * junto con el mensaje "La parcela y el cultivo deben estar definidos" y
         * no se realiza la operacion solicitada
         */
        if (parcelName == null || cropName == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.UNDEFINED_PARCEL_NAME_AND_CROP_NAME))).build();
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
         * Siempre y cuando no se elimine el control por el nombre
         * de parcela y el nombre del cultivo indefinidos, si la
         * fecha desde y la fecha hasta NO estan definidos, la
         * aplicacion del lado servidor retorna una coleccion de
         * registros de actividad hidrica de cultivo asociados a
         * un usuario que tienen un nombre de parcela y un nombre
         * de cultivo
         */
        if (stringDateFrom == null && stringDateUntil == null) {
            return Response.status(Response.Status.OK)
                    .entity(mapper.writeValueAsString(cropWaterActivityLogService.findAllByParcelNameAndCropName(userId, parcelName, cropName)))
                    .build();
        }

        /*
         * Siempre y cuando no se elimine el control por el nombre
         * de parcela y el nombre del cultivo indefinidos, si la
         * fecha desde esta definida y la fecha hasta NO esta
         * definida, la aplicacion del lado servidor retorna una
         * coleccion de registros de actividad hidrica de cultivo
         * asociados a un usuario que tienen un nombre de parcela,
         * un nombre de cultivo y una fecha mayor o igual a una
         * fecha desde
         */
        if (stringDateFrom != null && stringDateUntil == null) {
            dateFrom = new Date(dateFormatter.parse(stringDateFrom).getTime());
            return Response.status(Response.Status.OK)
                    .entity(mapper.writeValueAsString(cropWaterActivityLogService.findAllByDateGreaterThanOrEqual(userId, parcelName, cropName, UtilDate.toCalendar(dateFrom))))
                    .build();
        }

        /*
         * Siempre y cuando no se elimine el control por el nombre
         * de parcela y el nombre del cultivo indefinidos, si la
         * fecha desde NO esta definida y la fecha hasta esta
         * definida, la aplicacion del lado servidor retorna una
         * coleccion de registros de actividad hidrica de cultivo
         * asociados a un usuario que tienen un nombre de parcela,
         * un nombre de cultivo y una fecha menor o igual a una
         * fecha hasta
         */
        if (stringDateFrom == null && stringDateUntil != null) {
            dateUntil = new Date(dateFormatter.parse(stringDateUntil).getTime());
            return Response.status(Response.Status.OK)
                    .entity(mapper.writeValueAsString(cropWaterActivityLogService.findAllByDateLessThanOrEqual(userId, parcelName, cropName, UtilDate.toCalendar(dateUntil))))
                    .build();
        }

        /*
         * Si la fecha desde, la fecha hasta, el nombre de la parcela
         * y el nombre del cultivo estan definidos, la aplicacion del
         * lado del servidor retorna una coleccion de registros de
         * actividad hidrica de cultivo asociados a un usuario que
         * tienen una fecha mayor o igual a una fecha desde y menor
         * o igual a una fecha hasta, un nombre de parcela y un nombre
         * de cultivo
         */

        /*
         * Si el valor del encabezado de autorizacion de la peticion HTTP
         * dada, tiene un JWT valido, la aplicacion del lado servidor
         * devuelve el mensaje HTTP 200 (Ok) junto con los datos solicitados
         * por el cliente
         */
        dateFrom = new Date(dateFormatter.parse(stringDateFrom).getTime());
        dateUntil = new Date(dateFormatter.parse(stringDateUntil).getTime());

        return Response.status(Response.Status.OK)
                .entity(mapper.writeValueAsString(cropWaterActivityLogService.findByAllFilterParameters(userId,
                        parcelName, cropName, UtilDate.toCalendar(dateFrom), UtilDate.toCalendar(dateUntil))))
                .build();
    }

}
