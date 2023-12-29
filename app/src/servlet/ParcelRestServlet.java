package servlet;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Calendar;
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
import model.Parcel;
import model.PlantingRecord;
import model.Soil;
import model.User;
import stateless.ParcelServiceBean;
import stateless.SecretKeyServiceBean;
import stateless.UserServiceBean;
import stateless.PlantingRecordServiceBean;
import stateless.SessionServiceBean;
import stateless.SoilServiceBean;
import stateless.OptionServiceBean;
import util.ErrorResponse;
import util.ReasonError;
import util.RequestManager;
import util.SourceUnsatisfiedResponse;
import utilJwt.AuthHeaderManager;
import utilJwt.JwtManager;

@Path("/parcels")
public class ParcelRestServlet {

  // inject a reference to the ParcelServiceBean slsb
  @EJB ParcelServiceBean parcelService;
  @EJB SecretKeyServiceBean secretKeyService;
  @EJB UserServiceBean userService;
  @EJB PlantingRecordServiceBean plantingRecordService;
  @EJB SessionServiceBean sessionService;
  @EJB OptionServiceBean optionService;
  @EJB SoilServiceBean soilService;

  // Mapea lista de pojo a JSON
  ObjectMapper mapper = new ObjectMapper();

  private final String UNDEFINED_VALUE = "undefined";
  private final String NAME_REGULAR_EXPRESSION = "^[A-Za-zÀ-ÿ]+(\\s[A-Za-zÀ-ÿ]*[0-9]*)*$";
  private final String IRRIGATION_WATER_NEED_NOT_AVAILABLE_BUT_CALCULABLE = "-";

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
    return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(parcelService.findAll(userId))).build();
  }

  @GET
  @Path("/search")
  public Response search(@Context HttpHeaders request, @QueryParam("parcelName") String parcelName) throws IOException {
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
       * Si el nombre del dato correspondiente a esta clase NO
       * esta definido, la aplicacion del lado servidor retorna
       * el mensaje HTTP 400 (Bad request) junto con el mensaje
       * "El nombre de <dato> debe estar definido" y no se realiza
       * la operacion solicitada
       */
      if (parcelName == null || parcelName.equals(UNDEFINED_VALUE)) {
          return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorResponse(ReasonError.UNDEFINED_PARCEL_NAME)).build();
      }

      /*
       * Si el dato solicitado no existe en la base de datos
       * subyacente, la aplicacion del lado servidor devuelve
       * el mensaje HTTP 404 (Not found) junto con el mensaje
       * "<dato> inexistente" y la fuente u origen en donde
       * NO se pudo satisfacer la solicitud, y no se realiza
       * la operacion solicitada
       */
      if (!parcelService.checkExistenceForSearch(userId, parcelName)) {
          return Response.status(Response.Status.NOT_FOUND).entity(new ErrorResponse(ReasonError.PARCEL_NOT_FOUND, SourceUnsatisfiedResponse.PARCEL)).build();
      }

      /*
       * Si el valor del encabezado de autorizacion de la peticion HTTP
       * dada, tiene un JWT valido, la aplicacion del lado servidor
       * devuelve el mensaje HTTP 200 (Ok) junto con los datos solicitados
       * por el cliente
       */
      return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(parcelService.search(userId, parcelName))).build();
  }

  /*
   * Este metodo es necesario para el autocompletado de un campo
   * correspondiente a una parcela activa o inactiva, como el campo
   * de parcela de una pagina web de lista de datos que estan asociados
   * a una parcela, como la pagina web de lista de balances hidricos,
   * por ejemplo.
   */
  @GET
  @Path("/findByName")
  public Response findByName(@Context HttpHeaders request, @QueryParam("parcelName") String parcelName) throws IOException {
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
     * Si el nombre del dato correspondiente a esta clase NO
     * esta definido, la aplicacion del lado servidor retorna
     * el mensaje HTTP 400 (Bad request) junto con el mensaje
     * "El nombre de <dato> debe estar definido" y no se realiza
     * la operacion solicitada
     */
    if (parcelName == null || parcelName.equals(UNDEFINED_VALUE)) {
      return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorResponse(ReasonError.UNDEFINED_PARCEL_NAME)).build();
    }

    /*
     * Si el valor del encabezado de autorizacion de la peticion HTTP
     * dada, tiene un JWT valido, la aplicacion del lado servidor
     * devuelve el mensaje HTTP 200 (Ok) junto con los datos solicitados
     * por el cliente
     */
    return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(parcelService.findByName(userId, parcelName))).build();
  }

  /*
   * Este metodo es necesario para el autocompletado de un campo
   * correspondiente a una parcela activa, como el campo de parcela
   * del formulario de creacion y modificacion de un registro de
   * plantacion, por ejemplo.
   */
  @GET
  @Path("/findActiveParcelByName")
  public Response findActiveParcelByName(@Context HttpHeaders request, @QueryParam("parcelName") String parcelName) throws IOException {
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
    return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(parcelService.findActiveParcelByName(userId, parcelName))).build();
  }

  @GET
  @Path("/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response find(@Context HttpHeaders request, @PathParam("id") int parcelId) throws IOException {
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
     * Si el dato solicitado no existe en la base de datos
     * subyacente, la aplicacion del lado servidor devuelve
     * el mensaje HTTP 404 (Not found) junto con el mensaje
     * "Recurso no encontrado" y no se realiza la operacion
     * solicitada
     */
    if (!parcelService.checkExistence(parcelId)) {
      return Response.status(Response.Status.NOT_FOUND).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.RESOURCE_NOT_FOUND))).build();
    }

    /*
     * Si al usuario que hizo esta peticion HTTP, no le pertenece
     * el dato solicitado, la aplicacion del lado servidor
     * devuelve el mensaje HTTP 403 (Forbidden) junto con el
     * mensaje "Acceso no autorizado" (contenido en el enum
     * ReasonError) y no se realiza la operacion solicitada
     */
    if (!parcelService.checkUserOwnership(userId, parcelId)) {
      return Response.status(Response.Status.FORBIDDEN).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.UNAUTHORIZED_ACCESS))).build();
    }

    /*
     * Si el valor del encabezado de autorizacion de la peticion HTTP
     * dada, tiene un JWT valido, la aplicacion del lado servidor
     * devuelve el mensaje HTTP 200 (Ok) junto con los datos solicitados
     * por el cliente
     */
    return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(parcelService.find(userId, parcelId))).build();
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

    Parcel newParcel = mapper.readValue(json, Parcel.class);

    /*
     * Si el nombre de la nueva parcela no esta definido, la
     * aplicacion del lado servidor retorna el mensaje HTTP
     * 400 (Bad request) junto con el mensaje "El nombre de
     * la parcela debe estar definido" y no se realiza la
     * operacion solicitada
     */
    if (newParcel.getName() == null) {
      return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorResponse(ReasonError.UNDEFINED_PARCEL_NAME)).build();
    }

    /*
     * Si el nombre de la nueva parcela NO empieza con caracteres
     * alfabeticos, la aplicacion del lado servidor retorna el
     * mensaje HTTP 400 (Bad request) junto con el mensaje "El
     * nombre de una parcela debe empezar con una palabra formada
     * unicamente por caracteres alfabeticos. Puede haber mas de
     * una palabra formada unicamente por caracteres alfabeticos
     * y puede haber palabras formadas unicamente por caracteres
     * numericos. Todas las palabras deben estar separadas por un
     * espacio en blanco." y no se realiza la operacion solicitada
     */
    if (!newParcel.getName().matches(NAME_REGULAR_EXPRESSION)) {
      return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorResponse(ReasonError.INVALID_PARCEL_NAME)).build();
    }

    /*
     * Si dentro del conjunto de parcelas del usuario hay una
     * parcela con el nombre de la nueva parcela, la aplicacion
     * del lado servidor retorna el mensaje HTTP 400 (Bad request)
     * junto con el mensaje "Nombre de parcela ya utilizado, elija
     * otro" y no se realiza la operacion solicitada
     */
    if (parcelService.checkExistence(userId, newParcel.getName())) {
      return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorResponse(ReasonError.PARCEL_NAME_ALREADY_USED)).build();
    }

    /*
     * Si la cantidad de hectareas de la nueva parcela es menor
     * o igual a 0.0, la aplicacion del lado servidor retorna el
     * mensaje HTTP 400 (Bad request) junto con el mensaje "La
     * cantidad de hectareas debe ser mayor a 0.0" y no se realiza
     * la operacion solicitada
     */
    if (newParcel.getHectares() <= 0.0) {
      return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorResponse(ReasonError.INVALID_NUMBER_OF_HECTARES)).build();
    }

    /*
     * Elimina los espacios en blanco de los extremos del nombre
     * de una parcela y reduce a uno los espacios en blanco entre
     * palabra y palabra del nombre de una parcela que tengan una
     * longitud mayor o igual a dos espacios en blanco, si el nombre
     * de una parcela esta formado por mas de una palabra
     */
    newParcel.setName(parcelService.setBlankSpacesInNameToOne(newParcel.getName()));
    newParcel.setOption(optionService.create());

    /*
     * Persistencia de una parcela
     */
    newParcel = parcelService.create(newParcel);

    /*
     * Persiste la relacion expresada mediante la
     * añadidura de una nueva parcela a la coleccion
     * de parcelas de un usuario
     */
    User user = userService.find(userId);
    user.getParcels().add(newParcel);
    userService.merge(user);

    /*
     * Si el valor del encabezado de autorizacion de la peticion HTTP
     * dada, tiene un JWT valido, la aplicacion del lado servidor
     * devuelve el mensaje HTTP 200 (Ok) junto con los datos que el
     * cliente solicito persistir
     */
    return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(newParcel)).build();
  }

  @DELETE
  @Path("/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response remove(@Context HttpHeaders request, @PathParam("id") int parcelId) throws IOException {
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
     * Si el dato solicitado no existe en la base de datos
     * subyacente, la aplicacion del lado servidor devuelve
     * el mensaje HTTP 404 (Not found) junto con el mensaje
     * "Recurso no encontrado" y no se realiza la operacion
     * solicitada
     */
    if (!parcelService.checkExistence(parcelId)) {
      return Response.status(Response.Status.NOT_FOUND).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.RESOURCE_NOT_FOUND))).build();
    }

    /*
     * Si al usuario que hizo esta peticion HTTP, no le pertenece
     * el dato solicitado, la aplicacion del lado servidor
     * devuelve el mensaje HTTP 403 (Forbidden) junto con el
     * mensaje "Acceso no autorizado" (contenido en el enum
     * ReasonError) y no se realiza la operacion solicitada
     */
    if (!parcelService.checkUserOwnership(userId, parcelId)) {
      return Response.status(Response.Status.FORBIDDEN).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.UNAUTHORIZED_ACCESS))).build();
    }

    /*
     * Si la parcela que se quiere eliminar (logicamente)
     * tiene un registro de plantacion en desarrollo, la
     * aplicacion del lado servidor retorna el mensaje HTTP
     * 400 (Bad request) junto con el mensaje "No esta permitido
     * eliminar (logicamente) una parcela que tiene un registro
     * de plantacion en desarrollo" y no se realiza la
     * operacion solicitada
     */
    if (plantingRecordService.checkOneInDevelopment(parcelService.find(parcelId))) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(
          new ErrorResponse(ReasonError.DELETION_PARCEL_WITH_PLANTING_RECORD_IN_DEVELOPMENT_NOT_ALLOWED))).build();
    }

    /*
     * Si el valor del encabezado de autorizacion de la peticion HTTP
     * dada, tiene un JWT valido, la aplicacion del lado servidor
     * devuelve el mensaje HTTP 200 (Ok) junto con los datos que el
     * cliente solicito eliminar
     */
    return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(parcelService.remove(userId, parcelId))).build();
  }

  @PUT
  @Path("/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response modify(@Context HttpHeaders request, @PathParam("id") int parcelId, String json) throws IOException {
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
     * Si el dato solicitado no existe en la base de datos
     * subyacente, la aplicacion del lado servidor devuelve
     * el mensaje HTTP 404 (Not found) junto con el mensaje
     * "Recurso no encontrado" y no se realiza la operacion
     * solicitada
     */
    if (!parcelService.checkExistence(parcelId)) {
      return Response.status(Response.Status.NOT_FOUND).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.RESOURCE_NOT_FOUND))).build();
    }

    /*
     * Si al usuario que hizo esta peticion HTTP, no le pertenece
     * el dato solicitado, la aplicacion del lado servidor
     * devuelve el mensaje HTTP 403 (Forbidden) junto con el
     * mensaje "Acceso no autorizado" (contenido en el enum
     * ReasonError) y no se realiza la operacion solicitada
     */
    if (!parcelService.checkUserOwnership(userId, parcelId)) {
      return Response.status(Response.Status.FORBIDDEN).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.UNAUTHORIZED_ACCESS))).build();
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

    Parcel modifiedParcel = mapper.readValue(json, Parcel.class);
    Soil modifiedSoil = modifiedParcel.getSoil();
    Soil currentSoil = parcelService.find(parcelId).getSoil();

    /*
     * Si el nombre de la parcela a modificar no esta definido,
     * la aplicacion del lado servidor retorna el mensaje HTTP
     * 400 (Bad request) junto con el mensaje "El nombre de
     * la parcela debe estar definido" y no se realiza la
     * operacion solicitada
     */
    if (modifiedParcel.getName() == null) {
      return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorResponse(ReasonError.UNDEFINED_PARCEL_NAME)).build();
    }

    /*
     * Si el nombre de la parcela a modificar NO empieza con
     * caracteres alfabeticos, la aplicacion del lado servidor
     * retorna el mensaje HTTP 400 (Bad request) junto con el
     * mensaje "El nombre de una parcela debe empezar con una
     * palabra formada unicamente por caracteres alfabeticos.
     * Puede haber mas de una palabra formada unicamente por
     * caracteres alfabeticos y puede haber palabras formadas
     * unicamente por caracteres numericos. Todas las palabras
     * deben estar separadas por un espacio en blanco." y no
     * se realiza la operacion solicitada
     */
    if (!modifiedParcel.getName().matches(NAME_REGULAR_EXPRESSION)) {
      return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorResponse(ReasonError.INVALID_PARCEL_NAME)).build();
    }

    /*
     * Si dentro del conjunto de parcelas del usuario hay una
     * parcela que tiene el nombre de la parcela a modificar, la
     * aplicacion del lado servidor retorna el mensaje HTTP 400
     * (Bad request) junto con el mensaje "Nombre de parcela ya
     * utilizado, elija otro" y no se realiza la operacion
     * solicitada
     */
    if (parcelService.checkRepeated(userId, parcelId, modifiedParcel.getName())) {
      return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorResponse(ReasonError.PARCEL_NAME_ALREADY_USED)).build();
    }

    /*
     * Si la cantidad de hectareas de la parcela a modificar es
     * menor o igual a 0.0, la aplicacion del lado servidor retorna
     * el mensaje HTTP 400 (Bad request) junto con el mensaje "La
     * cantidad de hectareas debe ser mayor a 0.0" y no se realiza
     * la operacion solicitada
     */
    if (modifiedParcel.getHectares() <= 0.0) {
      return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorResponse(ReasonError.INVALID_NUMBER_OF_HECTARES)).build();
    }

    /*
     * Si en la modificacion de una parcela, el suelo NO esta
     * definido, la bandera umbral de riego de la opcion asociada
     * a dicha parcela es establecida en false, ya que para
     * calcular, mediante un umbral de riego, la necesidad de
     * agua de riego de un cultivo en la fecha actual se necesitan
     * datos de suelo (capacidad de campo, punto de marchitez
     * permanente y peso especifico aparente), los cuales
     * estan presentes en un suelo
     */
    if (modifiedParcel.getSoil() == null) {
      optionService.unsetSoilFlag(modifiedParcel.getOption().getId());
    }

    /*
     * Si la parcela modificada tiene un registro de plantacion
     * "En desarrollo" y un suelo distinto al original, se asigna
     * el caracter "-" a la necesidad de agua de riego de un cultivo
     * en la fecha actual de dicho registro para hacer que el usuario
     * ejecute el proceso del calculo de la necesidad de agua de
     * riego de un cultivo en la fecha actual [mm/dia]. La manera
     * en la que el usuario realiza esto es mediante el boton
     * "Calcular" de la pagina de registros de plantacion.
     */
    if (plantingRecordService.checkOneInDevelopment(modifiedParcel) && !soilService.equals(modifiedSoil, currentSoil)) {
      PlantingRecord developingPlantingRecord = plantingRecordService.findInDevelopment(modifiedParcel);
      plantingRecordService.updateIrrigationWaterNeed(developingPlantingRecord.getId(), modifiedParcel, IRRIGATION_WATER_NEED_NOT_AVAILABLE_BUT_CALCULABLE);
    }

    /*
     * Elimina los espacios en blanco de los extremos del nombre
     * de una parcela y reduce a uno los espacios en blanco entre
     * palabra y palabra del nombre de una parcela que tengan una
     * longitud mayor o igual a dos espacios en blanco, si el nombre
     * de una parcela esta formado por mas de una palabra
     */
    modifiedParcel.setName(parcelService.setBlankSpacesInNameToOne(modifiedParcel.getName()));

    /*
     * Si el valor del encabezado de autorizacion de la peticion HTTP
     * dada, tiene un JWT valido, y se pasan todos los controles para
     * la modificacion del dato correspondiente a este bloque de codigo
     * de esta API REST, la aplicacion del lado servidor devuelve el
     * mensaje HTTP 200 (Ok) junto con el dato que el cliente solicito
     * modificar
     */
    return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(parcelService.modify(userId, parcelId, modifiedParcel))).build();
  }

}
