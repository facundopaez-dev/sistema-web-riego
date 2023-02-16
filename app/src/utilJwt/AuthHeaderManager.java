package utilJwt;

import javax.ws.rs.core.HttpHeaders;

/**
 * AuthHeaderManager es la clase que se utiliza para
 * obtener el valor del encabezado de autorizacion de
 * una peticion HTTP y realizar controles sobre dicho
 * valor
 */
public class AuthHeaderManager {

  /*
   * Por convencion, se usa la palabra "Bearer" en el encabezado de
   * autorizacion de una peticion HTTP para indicar que se usa un
   * JWT para autenticacion (principalmente), y ademas y opcionalmente,
   * tambien para autorizacion.
   * 
   * Por lo tanto, si el primer valor del encabezado de autorizacion de
   * una peticion HTTP es la palabra "Bearer", entonces el segundo
   * valor es un JWT.
   * 
   * Estas constantes se utilizan para recuperar el JWT del valor
   * del encabezado de autorizacion de una peticion HTTP.
   */
  private static final String BEARER = "Bearer";
  private static final String EMPTY_STRING = "";

  /*
   * Esta expresion regular se utiliza para validar que el valor del
   * encabezado de autorizacion de una peticion HTTP, cumple con la
   * convencion de JWT. En consecuencia, se valida que dicho valor
   * es un JWT.
   */
  private static final String JWT_REGEX = "Bearer\\s([a-zA-Z]|[0-9])+[.]([a-zA-Z]|[0-9])+[.]([a-zA-Z]|[0-9]|[_]|[-])+";

  /*
   * El metodo constructor tiene el modificador de acceso 'private'
   * para que ningun programador trate de instanciar esta clase
   * desde afuera, ya que todos los metodos publicos de la misma
   * son estaticos, con lo cual, no se requiere una instancia de
   * esta clase para invocar a sus metodos publicos
   */
  private AuthHeaderManager() {

  }

  /**
   * Obtiene el valor del encabezado de autorizacion de una peticion
   * HTTP
   * 
   * @param request referencia de tipo HttpHeaders a traves de
   * la cual se obtiene el valor del encabezado de autorizacion
   * de una peticion HTTP
   * @return referencia a un objeto de tipo String que contiene
   * el valor del encabezado de autorizacion de una peticion
   * HTTP. En otras palabras, retorna el valor del encabezado
   * de autorizacion de una peticion HTTP. Si el encabezado
   * HTTP Authorization no esta presente, retorna null.
   * Si el encabezado HTTP Authorization esta presente, pero
   * no tiene valor, retorna una cadena vacia. Si el encabezado
   * HTTP Authorization esta presente mas de una vez, los
   * valores se unen y se separan por un caracter ','.
   */
  public static String getAuthHeaderValue(HttpHeaders request) {
    return request.getHeaderString(HttpHeaders.AUTHORIZATION);
  }

  /**
   * Retorna true si y solo si el valor del encabezado de autorizacion
   * de una peticion HTTP, existe.
   * 
   * El metodo getHeaderString() de la clase HttpHeaders del paquete
   * javax.ws.rs.core, puede retornar valores distintos y uno de
   * ellos es null, el cual, lo retorna cuando el encabezado de
   * una peticion HTTP del que se quiere recuperar su valor, no esta
   * presente en la misma. Esto es entendible porque si el encabezado
   * del que se quiere recuperar su valor, no esta presente en una
   * peticion HTTP, entonces dicho valor no existe.
   * 
   * @param authHeaderValue
   * @return true si la variable de tipo por referencia authHeaderValue de
   * tipo String contiene una referencia, false en caso contrario
   */
  public static boolean isPresent(String authHeaderValue) {
    return (authHeaderValue != null);
  }

  /**
   * Retorna true si y solo si el valor del encabezado de autorizacion
   * de una peticion HTTP, esta vacio.
   * 
   * El metodo getHeaderString() de la clase HttpHeaders del paquete
   * javax.ws.rs.core, puede retornar valores distintos y uno de
   * ellos es una referencia a un objeto vacio de tipo String. Este
   * valor lo retorna cuando el encabezado de una peticion HTTP del
   * que se quiere recuperar su valor, esta presente, pero esta vacio.
   * 
   * @param authHeaderValue
   * @return true si el objeto referenciado por la variable de tipo
   * por referencia dada de tipo String, esta vacio, false en caso
   * contrario
   */
  public static boolean isEmpty(String authHeaderValue) {
    return authHeaderValue.isEmpty();
  }
  
  /**
   * Retorna true si y solo si el valor del encabezado de autorizacion
   * de una peticion HTTP, cumple con la convencion de JWT.
   * 
   * Por convencion, se usa la palabra "Bearer" en el encabezado de
   * autorizacion de una peticion HTTP para indicar que se usa un
   * JWT para autenticacion (principalmente), y ademas y opcionalmente,
   * tambien para autorizacion.
   * 
   * Por lo tanto, si el primer valor del encabezado de autorizacion de
   * una peticion HTTP es la palabra "Bearer", entonces el segundo
   * valor es un JWT.
   * 
   * @param authHeaderValue
   * @return true si el valor del encabezado de autorizacion de una
   * peticion HTTP, cumple con la convencion de JWT, false en caso
   * contario
   */
  public static boolean checkJwtConvention(String authHeaderValue) {
    /*
     * Si la cadena de caracteres contenida en el objeto
     * referenciado por la variable de tipo por referencia
     * de tipo String dada, tiene el formato de la expresion
     * regular dada, el valor del encabezado de autorizacion
     * de una peticion HTTP cumple con la convencion de JWT,
     * y, por lo tanto, se retorna el valor booleano true
     */
    if (authHeaderValue.matches(JWT_REGEX)) {
      return true;
    }
    
    return false;
  }

  /**
   * Retorna el JWT del valor del encabezado de autorizacion de una
   * peticion HTTP si y solo si dicho valor cumple con la convencion
   * de JWT
   * 
   * @param authHeaderValue
   * @return una referencia a un objeto de tipo String que
   * contiene el JWT del valor del encabezado de autorizacion de
   * una peticion HTTTP, si dicho valor cumple con la convencion
   * de JWT. En caso contrario, una referencia a un objeto de
   * tipo String que contiene una cadena vacia.
   */
  public static String getJwt(String authHeaderValue) {
    /*
     * Si el valor del encabezado de autorizacion de una peticion
     * HTTP cumple con la convencion de JWT, se retorna el JWT
     * contenido en dicho valor
     */
    if (checkJwtConvention(authHeaderValue)) {
      return authHeaderValue.replaceAll(BEARER, EMPTY_STRING).trim();
    }

    return EMPTY_STRING;
  }

}
