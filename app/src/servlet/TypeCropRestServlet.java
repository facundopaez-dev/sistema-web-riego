package servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Collection;
import javax.ejb.EJB;
import javax.ws.rs.Consumes;
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
import model.TypeCrop;
import stateless.TypeCropServiceBean;
import stateless.SecretKeyServiceBean;
import util.ErrorResponse;
import util.ReasonError;
import util.RequestManager;
import utilJwt.AuthHeaderManager;
import utilJwt.JwtManager;

@Path("/typeCrops")
public class TypeCropRestServlet {

  // inject a reference to the TypeCropServiceBean slsb
  @EJB TypeCropServiceBean typeCropService;
  @EJB SecretKeyServiceBean secretKeyService;

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
    return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(typeCropService.findAll())).build();
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
     * Si el usuario que solicita esta operacion no tiene el permiso de
     * administrador (superuser), la aplicacion del lado servidor devuelve
     * el mensaje HTTP 403 (Forbidden) junto con el mensaje "Acceso no
     * autorizado" (esta contenido en el enum ReasonError) y no se realiza
     * la operacion solicitada
     */
    if (!JwtManager.getSuperuser(jwt, secretKeyService.find().getValue())) {
      return Response.status(Response.Status.FORBIDDEN).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.UNAUTHORIZED_ACCESS))).build();
    }

    TypeCrop newTypeCrop = mapper.readValue(json, TypeCrop.class);

    /*
     * Si el nombre del tipo de cultivo NO esta definido, la aplicacion
     * del lado servidor devuelve el mensaje HTTP 400 (Bad request)
     * junto con el mensaje "El nombre del tipo de cultivo debe estar
     * definido" y no se realiza la operacion solicitada
     */
    if (newTypeCrop.getName() == null) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.UNDEFINED_CROP_TYPE_NAME))).build();
    }

    /*
     * Si el nombre del tipo de cultivo NO contiene unicamente letras,
     * y un espacio en blanco entre palabra y palabra si llega a ser
     * necesario, la aplicacion del lado servidor devuelve el mensaje
     * HTTP 400 (Bad request) junto con el mensaje "Nombre incorrecto:
     * el nombre para un tipo de cultivo sólo puede contener letras, y
     * un espacio en blanco entre palabra y palabra si llega a ser
     * necesario" y no se realiza la operacion solicitada
     */
    if (!newTypeCrop.getName().matches("[A-Za-zÀ-ÿ]+(\\s[A-Za-zÀ-ÿ]+)*")) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.INVALID_CROP_TYPE_NAME))).build();
    }

    /*
     * Si el nombre del tipo de cultivo ingresado, ya existe en la
     * base de datos subyacente, la aplciacion del lado servidor
     * devuelve el mensaje HTTP 400 (Bad request) junto con el
     * mensaje "El tipo de cultivo ingresado ya existe" y no se
     * realiza la operacion solicitada
     */
    if (typeCropService.checkExistence(newTypeCrop.getName())) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.TYPE_CROP_ALREADY_EXISTING))).build();
    }

    /*
     * Si el valor del encabezado de autorizacion de la peticion HTTP
     * dada, tiene un JWT valido, la aplicacion del lado servidor
     * devuelve el mensaje HTTP 200 (Ok) junto con los datos que el
     * cliente solicito persistir
     */
    return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(typeCropService.create(newTypeCrop))).build();
  }

}
