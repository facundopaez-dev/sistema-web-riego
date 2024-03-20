package servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.Calendar;
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
import model.Crop;
import model.GeographicLocation;
import model.Option;
import model.Parcel;
import model.PlantingRecord;
import model.Soil;
import model.User;
import stateless.ParcelServiceBean;
import stateless.SecretKeyServiceBean;
import stateless.UserServiceBean;
import stateless.PlantingRecordServiceBean;
import stateless.PlantingRecordStatusServiceBean;
import stateless.SessionServiceBean;
import stateless.SoilServiceBean;
import stateless.GeographicLocationServiceBean;
import stateless.OptionServiceBean;
import stateless.Page;
import util.ErrorResponse;
import util.ReasonError;
import util.RequestManager;
import util.SourceUnsatisfiedResponse;
import utilJwt.AuthHeaderManager;
import utilJwt.JwtManager;
import irrigation.WaterMath;

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
  @EJB PlantingRecordStatusServiceBean statusService;
  @EJB GeographicLocationServiceBean geographicLocationService;

  // Mapea lista de pojo a JSON
  ObjectMapper mapper = new ObjectMapper();

  private final String UNDEFINED_VALUE = "undefined";
  private final String NAME_REGULAR_EXPRESSION = "^[A-Za-zÀ-ÿ]+(\\s[A-Za-zÀ-ÿ]*[0-9]*)*$";

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
     * Si el valor del encabezado de autorizacion de la peticion HTTP
     * dada, tiene un JWT valido, la aplicacion del lado servidor
     * devuelve el mensaje HTTP 200 (Ok) junto con los datos solicitados
     * por el cliente
     */
    return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(parcelService.findAll(userId))).build();
  }

  @GET
  @Path("/findAllPagination")
  @Produces(MediaType.APPLICATION_JSON)
  public Response findAllPagination(@Context HttpHeaders request, @QueryParam("page") Integer page, @QueryParam("cant") Integer cant, @QueryParam("search") String search) throws IOException {
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

      Map<String, String> map = new HashMap<String, String>();

      // Convert JSON string to Map
      map = mapper.readValue(search, new TypeReference<Map<String, String>>(){});

      /*
       * Si el valor del encabezado de autorizacion de la peticion HTTP
       * dada, tiene un JWT valido, la aplicacion del lado servidor
       * devuelve el mensaje HTTP 200 (Ok) junto con los datos solicitados
       * por el cliente
       */
      Page<Parcel> parcels = parcelService.findAllPagination(userId, page, cant, map);
      return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(parcels)).build();
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
      if (parcelName == null || parcelName.equals(UNDEFINED_VALUE)) {
          return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.UNDEFINED_PARCEL_NAME))).build();
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
        return Response.status(Response.Status.NOT_FOUND)
            .entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.PARCEL_NOT_FOUND, SourceUnsatisfiedResponse.PARCEL)))
            .build();
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
    if (parcelName == null || parcelName.equals(UNDEFINED_VALUE)) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.UNDEFINED_PARCEL_NAME))).build();
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
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.UNDEFINED_PARCEL_NAME))).build();
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
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.INVALID_PARCEL_NAME))).build();
    }

    /*
     * Si dentro del conjunto de parcelas del usuario hay una
     * parcela con el nombre de la nueva parcela, la aplicacion
     * del lado servidor retorna el mensaje HTTP 400 (Bad request)
     * junto con el mensaje "Nombre de parcela ya utilizado, elija
     * otro" y no se realiza la operacion solicitada
     */
    if (parcelService.checkExistence(userId, newParcel.getName())) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.PARCEL_NAME_ALREADY_USED))).build();
    }

    /*
     * Si la cantidad de hectareas de la nueva parcela es menor
     * o igual a 0.0, la aplicacion del lado servidor retorna el
     * mensaje HTTP 400 (Bad request) junto con el mensaje "La
     * cantidad de hectareas debe ser mayor a 0.0" y no se realiza
     * la operacion solicitada
     */
    if (newParcel.getHectares() <= 0.0) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.INVALID_NUMBER_OF_HECTARES))).build();
    }

    /*
     * Elimina los espacios en blanco de los extremos del nombre
     * de una parcela y reduce a uno los espacios en blanco entre
     * palabra y palabra del nombre de una parcela que tengan una
     * longitud mayor o igual a dos espacios en blanco, si el nombre
     * de una parcela esta formado por mas de una palabra
     */
    newParcel.setName(parcelService.setBlankSpacesInNameToOne(newParcel.getName()));

    /*
     * Una parcela nueva esta inicialmente activa
     */
    newParcel.setActive(true);
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
          .entity(mapper.writeValueAsString(mapper.writeValueAsString(new ErrorResponse(ReasonError.JWT_NOT_ASSOCIATED_WITH_ACTIVE_SESSION))))
          .build();
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
    if (plantingRecordService.checkOneInDevelopment(parcelService.find(parcelId).getId())) {
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
    Soil currentSoil = parcelService.find(parcelId).getSoil();
    Soil modifiedSoil = modifiedParcel.getSoil();

    /*
     * Si el nombre de la parcela a modificar no esta definido,
     * la aplicacion del lado servidor retorna el mensaje HTTP
     * 400 (Bad request) junto con el mensaje "El nombre de
     * la parcela debe estar definido" y no se realiza la
     * operacion solicitada
     */
    if (modifiedParcel.getName() == null) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.UNDEFINED_PARCEL_NAME))).build();
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
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.INVALID_PARCEL_NAME))).build();
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
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.PARCEL_NAME_ALREADY_USED))).build();
    }

    /*
     * Si la cantidad de hectareas de la parcela a modificar es
     * menor o igual a 0.0, la aplicacion del lado servidor retorna
     * el mensaje HTTP 400 (Bad request) junto con el mensaje "La
     * cantidad de hectareas debe ser mayor a 0.0" y no se realiza
     * la operacion solicitada
     */
    if (modifiedParcel.getHectares() <= 0.0) {
      return Response.status(Response.Status.BAD_REQUEST).entity(mapper.writeValueAsString(new ErrorResponse(ReasonError.INVALID_NUMBER_OF_HECTARES))).build();
    }

    /*
     * El simbolo de esta variable se utiliza para representar que la
     * necesidad de agua de riego de un cultivo en la fecha actual
     * [mm/dia] no esta disponible, pero se puede calcular. Esta situacion
     * ocurre unicamente para un registro de plantacion en desarrollo.
     */
    String cropIrrigationWaterNeedNotAvailableButCalculable = plantingRecordService.getCropIrrigationWaterNotAvailableButCalculable();

    GeographicLocation currentGeographicLocation = parcelService.find(parcelId).getGeographicLocation();
    double currentLatitude = currentGeographicLocation.getLatitude();
    double currentLongitude = currentGeographicLocation.getLongitude();

    /*
     * Si la latitud y/o la longitud de la ubicacion geografica de
     * una parcela es modificada, se actualiza la latitud y/o longitud
     * de la ubicacion geografica en la base de datos subyacente y la
     * bandera modifiedGeographicLocationFlag de una parcela se establece
     * en true para hacer que la aplicacion obtenga los datos meteorologicos
     * de la nueva ubicacion geografica
     */
    if (modifiedParcel.getGeographicLocation().getLatitude() != currentLatitude || modifiedParcel.getGeographicLocation().getLongitude() != currentLongitude) {
      geographicLocationService.modify(currentGeographicLocation.getId(), modifiedParcel.getGeographicLocation());
      parcelService.setModifiedGeographicLocationFlag(parcelId);
    }

    /*
     * Si una parcela tiene un registro de plantacion que tiene un
     * estado de desarrollo (en desarrollo, desarrollo optimo,
     * desarrollo en riesgo de marchitez, desarrollo en marchitez)
     * y si se modifica la latitud y/o la longitud de una parcela,
     * se establece el valor "-" (no disponible, pero calculable)
     * en el atributo "necesidad de agua de riego de un cultivo
     * en la fecha actual" de dicho registro de plantacion, ya
     * que al cambiar la ubicacion geografica de una parcela
     * cambian las condiciones climaticas y la radiacion solar
     * extraterrestre a los que esta expuesto un cultivo sembrado
     * en una parcela. Estos datos son necesarios para calcular la
     * necesidad de agua de riego de un cultivo en la fecha actual.
     * Por lo tanto, al cambiar la ubicacion geografica de una
     * parcela cambia el valor de la necesidad de agua de riego de
     * un cultivo en la fecha actual. Por este motivo se asigna
     * el caracter "-" al atributo "necesidad de agua de riego
     * de un cultivo en la fecha actual" del registro de
     * plantacion en desarrollo de una parcela.
     */
    if ((modifiedParcel.getGeographicLocation().getLatitude() != currentLatitude
        || modifiedParcel.getGeographicLocation().getLongitude() != currentLongitude)
        && plantingRecordService.checkOneInDevelopment(parcelId)) {
      int developingPlantingRecordId = plantingRecordService.findInDevelopment(parcelId).getId();
      plantingRecordService.updateCropIrrigationWaterNeed(developingPlantingRecordId, cropIrrigationWaterNeedNotAvailableButCalculable);
    }

    /*
     * Si en la modifcacion de una parcela NO se asigna un
     * suelo, se realizan las siguientes operaciones:
     * - la bandera suelo de las opciones de la parcela
     * se establece en false, ya que NO es posible calcular
     * la necesidad de agua de riego de un cultivo en la
     * fecha actual (es decir, hoy) sin un suelo asignado
     * a la parcela, en la que hay un cultivo sembrado, y
     * con dicha bandera activa.
     * - si la parcela tiene un registro de plantacion que
     * tiene un estado de desarrollo relacionado al uso de
     * datos de suelo (desarrollo optimo, desarrollo en
     * riesgo de marchitez, desarrollo en marchitez) se
     * realizan las siguientes operaciones:
     * -- se asigna el valor 0 a la lamina total de agua
     * disponible (dt) [mm] y a la lamina de riego optima
     * (drop) [mm] del registro, ya que estos datos estan
     * en funcion del suelo y del cultivo sembrado, con
     * lo cual si la parcela no tiene un suelo asignado,
     * no es posible disponer de ellos.
     * -- se establece el estado "En desarrollo" en el
     * registro, ya que al no tener la parcela un suelo
     * asignado NO es posible determinar en que punto se
     * encuentra el nivel de humedad del suelo, en el que
     * hay un cultivo sembrado, con respecto a la
     * capacidad de campo, el umbral de riego (lamina de
     * riego optima), la capacidad de almacenamiento de
     * agua del suelo y el doble de la capacidad de
     * almacenamiento de agua del suelo.
     * 
     * Con datos de suelo es posible determinar en que
     * punto se encuentra el nivel de humedad del suelo,
     * que tiene un cultivo sembrado, con respecto a la
     * capacidad de campo, el umbral de riego (lamina de
     * riego optima), la capacidad de almacenamiento de
     * agua del suelo y el doble de la capacidad de
     * almacenamiento de agua del suelo. Con base en esto
     * se establece que si el nivel de humedad del suelo,
     * que tiene un cultivo sembrado, es:
     * - menor o igual a la capaciadad de campo y estrictamente
     * mayor al umbral de riego, el estado del registro de
     * plantacion correspondiente es "Desarrollo optimo".
     * - es menor o igual al umbral de riego y estrictamente
     * mayor a la capacidad de almacenamiento de agua del suelo,
     * el estado del registro de plantacion correspondiente
     * es "Desarrollo en riesgo de marchitez".
     * - es menor o igual a la capacidad de almacenamiento
     * de agua del suelo y estrictamente mayor al doble de
     * la capacidad de almacenamiento de agua del suelo, el
     * estado del registro de plantacion correspondiente es
     * "Desarrollo en marchitez".
     * - es estrictamente menor al doble de la capacidad
     * de almacenamiento de agua del suelo, el estado del
     * registro de plantacion correspondiente es "Muerto".
     */
    if (modifiedParcel.getSoil() == null) {
      optionService.unsetSoilFlag(modifiedParcel.getOption().getId());

      if (plantingRecordService.checkOneInDevelopmentRelatedToSoil(parcelId)) {
        int developingPlantingRecordId = plantingRecordService.findInDevelopment(parcelId).getId();
        plantingRecordService.updateTotalAmountWaterAvailable(developingPlantingRecordId, 0);
        plantingRecordService.updateOptimalIrrigationLayer(developingPlantingRecordId, 0);
        plantingRecordService.setStatus(developingPlantingRecordId, statusService.findInDevelopmentStatus());
      }

    }

    /*
     * Obtiene las opciones actualizadas de la parcela a modificar,
     * ya que si el suelo de la parcela a modificar NO esta definido,
     * la bandera suelo de las opciones de una parcela es establecida
     * en false
     */
    Option parcelOption = optionService.find(modifiedParcel.getOption().getId());

    /*
     * Si la bandera suelo de las opciones de la parcela a modificar,
     * esta activa, y si la parcela a modificar tiene un registro de
     * plantacion en un estado de desarrollo relacionado al uso de
     * datos de suelo (desarrollo optimo, desarrollo en riesgo de
     * marchitez, desarrollo en marchitez) (*) y tiene un suelo
     * asignado, el cual es distinto al actual:
     * - se asigna el caracter "-" (**) al atributo "necesidad de agua
     * de riego de un cultivo" (***) de dicho registro, ya que calcular
     * la necesidad de agua de riego de un cultivo en la fecha actual
     * (es decir, hoy) utilizando datos de suelo hace que dicho calculo
     * este en funcion del suelo. Por lo tanto, si se modifica el
     * suelo se debe realizar el calculo de la necesidad de agua de
     * riego de un cultivo en la fecha actual en funcion del nuevo
     * suelo.
     * - se calculan y asignan la lamina total de agua disponible
     * (dt) [mm] y la lamina de riego optima (drop) [mm], debido
     * a que estan en funcion del suelo y del cultivo.
     * - se establece el estado "Desarrollo optimo" en dicho registro
     * por los siguientes motivos. Primero porque "Desarrollo
     * optimo" es el estado inicial cuando se calcula la necesidad
     * de agua de riego de un cultivo en la fecha actual (es decir,
     * hoy) [mm] utilizando datos de suelo. Segundo porque al cambiar
     * el suelo cambian la capacidad de almacenamiento de agua del
     * suelo (lamina total de agua disponible (dt) [mm]) y el umbral
     * de riego (lamina de riego optima (drop) [mm]), debido a que
     * estan en funcion del suelo y del cultivo. Por lo tanto, se
     * debe calcular la necesidad de agua de riego de un cultivo en
     * la fecha actual (es decir, hoy) en funcion del nuevo suelo,
     * lo cual puede producir un cambio de estado de un registro de
     * plantacion desde el estado inicial "Desarrollo optimo" a uno
     * de los demas estados relacionados al uso de datos de suelo
     * (desarrollo en riesgo de marchitez, desarrollo en marchitez,
     * muerto).
     * 
     * A la lamina de riego optima (drop) se le asigna el signo
     * negativo (-) para poder compararla con el acumulado del
     * deficit de agua por dia [mm/dia], el cual es negativo y
     * es calculado desde la fecha de siembra de un cultivo hasta
     * la fecha inmediatamente anterior a la fecha actual. La
     * lamina de riego optima representa la cantidad maxima de
     * agua que puede perder un suelo para el cultivo que tiene
     * sembrado, a partir de la cual NO conviene que pierda mas
     * agua, sino que se le debe añadir agua hasta llevar su
     * nivel de humedad a capacidad de campo. Capacidad de campo
     * es la capacidad de almacenamiento de agua que tiene un
     * suelo. Un suelo que esta en capacidad de campo es un
     * suelo lleno de agua, pero no anegado. El motivo por el
     * cual se habla de llevar el nivel de humedad del suelo,
     * que tiene un cultivo sembrado, a capacidad de campo es
     * que el objetivo de la aplicacion es informar al usuario
     * la cantidad de agua que debe reponer en la fecha actual
     * (es decir, hoy) para llevar el nivel de humedad del suelo,
     * en el que tiene un cultivo sembrado, a capacidad de campo.
     * Esto es la cantidad de agua de riego [mm] que debe usar
     * el usuario para llenar el suelo en el que tiene un cultivo
     * sembrado, pero sin anegarlo.
     * 
     * El suelo agricola tiene dos limites: capacidad de campo
     * (limite superior) y punto de marchitez permanente (limite
     * inferior). La lamina de riego optima tambien se la conoce
     * como umbral de riego, debido a lo que representa.
     * 
     * La presencia de un suelo en una parcela significa que la
     * bandera suelo de las opciones de una parcela, esta activa.
     * Esto se debe a que la aplicacion tiene un control para
     * evitar que dicha bandera sea activada para una parcela
     * que NO tiene un suelo asignado. Este control esta
     * implementado en el metodo modify() de la clase OptionRestServlet.
     * Gracias a este control no es necesario implementar un control
     * con la condición != null para el suelo de una parcela.
     * 
     * (*) Cuando se utiliza un suelo para calcular la necesidad
     * de agua de riego de un cultivo en la fecha actual (es decir,
     * hoy), los estados utilizados para un registro de plantacion
     * son "Desarrollo optimo", "Desarrollo en riesgo de marchitez",
     * "Desarrollo en marchitez" y "Muerto", de los cuales los tres
     * primeros son de desarrollo.
     * 
     * (**) El caracter "-" (guion) se utiliza para representar
     * que la necesidad de agua de riego de un cultivo en la fecha
     * actual (es decir, hoy) [mm/dia] NO esta disponible, pero se
     * puede calcular. Esta situacion ocurre unicamente para un
     * registro de plantacion que tiene un estado de desarrollo
     * (en desarrollo, desarrollo optimo, desarrollo en riesgo de
     * marchitez, desarrollo en marchitez).
     * 
     * (***) El atributo "necesidad de agua de riego de un cultivo"
     * de un registro de plantacion es la necesidad de agua de riego
     * de un cultivo en la fecha actual (es decir, hoy).
     */
    if (parcelOption.getSoilFlag() && plantingRecordService.checkOneInDevelopmentRelatedToSoil(parcelId)
        && modifiedSoil != null && !soilService.equals(modifiedSoil, currentSoil)) {
      int developingPlantingRecordId = plantingRecordService.findInDevelopment(parcelId).getId();
      Crop developingCrop = plantingRecordService.findInDevelopment(parcelId).getCrop();

      plantingRecordService.updateCropIrrigationWaterNeed(developingPlantingRecordId, cropIrrigationWaterNeedNotAvailableButCalculable);
      plantingRecordService.updateTotalAmountWaterAvailable(developingPlantingRecordId, WaterMath.calculateTotalAmountWaterAvailable(developingCrop, modifiedSoil));
      plantingRecordService.updateOptimalIrrigationLayer(developingPlantingRecordId, WaterMath.calculateOptimalIrrigationLayer(developingCrop, modifiedSoil));
      plantingRecordService.setStatus(developingPlantingRecordId, statusService.findOptimalDevelopmentStatus());
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
