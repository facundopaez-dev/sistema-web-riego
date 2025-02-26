package utilJwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.Verification;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import model.User;

/**
 * JwtManager es la clase que se utiliza para la creacion
 * y validacion de un JWT
 */
public class JwtManager {

  /*
   * Estas constantes se utilizan para establecer
   * atributos en la carga util de un JWT
   */
  private static final String USER_ID = "userId";
  private static final String SUPERUSER = "superuser";
  private static final String SUPERUSER_PERMISSION_MODIFIER = "superuserPermissionModifier";
  private static final String USER_DELETION_PERMISSION = "userDeletionPermission";
  private static final String PASSWORD_RESET_LINK_ID = "passwordResetLinkId";
  private static final String USER_EMAIL = "userEmail";

  /*
   * La fecha de emision se utiliza para establecer el tiempo en el
   * que se crea un JWT y la fecha de expiracion se utiliza para
   * establecer el tiempo de expiracion de un JWT 
   */
  private static Date dateIssue = new Date();
  private static Date expirationDate = new Date();

  /*
   * Esta constante se utiliza para calcular la fecha de expiracion
   * de un JWT y su valor representa 120 minutos en milisegundos.
   * El tiempo de expiracion de un JWT se utiliza para expirar una
   * sesion abierta. Es decir, si el JWT del usuario que inicio
   * sesion en la aplicacion, expira, la sesion del usuario tambien
   * expira, con lo cual debe iniciar una nueva sesion para utilizar
   * la aplicacion.
   */
  private static final int OFFSET = 7200000;

  /*
   * Estas constantes se utilizan para recuperar los datos de
   * la carga util de un JWT, como el ID de usuario, por ejemplo
   */
  private static final String COMMA = ",";
  private static final String TWO_POINTS = ":";
  private static final String USER_ID_KEY = "\"userId\"";
  private static final String SUPERUSER_KEY = "\"superuser\"";
  private static final String SUPERUSER_PERMISSION_MODIFIER_KEY = "\"superuserPermissionModifier\"";
  private static final String USER_DELETION_PERMISSION_KEY = "\"userDeletionPermission\"";
  private static final String ISSUED_AT_KEY = "\"iat\"";
  private static final String EXPIRES_AT_KEY = "\"exp\"";
  private static final String PASSWORD_RESET_LINK_ID_KEY = "\"passwordResetLinkId\"";
  private static final String USER_EMAIL_KEY = "\"userEmail\"";

  /*
   * El metodo constructor tiene el modificador de acceso 'private'
   * para que ningun programador trate de instanciar esta clase
   * desde afuera, ya que todos los metodos publicos de la misma
   * son estaticos, con lo cual, no se requiere una instancia de
   * esta clase para invocar a sus metodos publicos
   */
  private JwtManager() {

  }

  /**
   * Crea un JWT con el ID y el permiso de un usuario, una fecha
   * de emision y una fecha de expiracion
   * 
   * @param User
   * @param secretKey clave secreta con la que se firma un JWT
   * @return referencia a un objeto de tipo String que contiene
   * un JWT
   */
  public static String createJwt(User user, String secretKey) {
    /*
     * Asigna el tiempo actual a la fecha de emision
     */
    dateIssue.setTime(System.currentTimeMillis());

    /*
     * Establece el tiempo de la fecha de expiracion como la
     * suma entre el tiempo de la fecha de emision y un desplazamiento
     */
    expirationDate.setTime(dateIssue.getTime() + OFFSET);

    JWTCreator.Builder jwtCreator = JWT.create();
    jwtCreator.withClaim(USER_ID, user.getId());
    jwtCreator.withClaim(SUPERUSER, user.getSuperuser());
    jwtCreator.withClaim(SUPERUSER_PERMISSION_MODIFIER, user.getSuperuserPermissionModifier());
    jwtCreator.withClaim(USER_DELETION_PERMISSION, user.getUserDeletionPermission());
    jwtCreator.withIssuedAt(dateIssue);
    jwtCreator.withExpiresAt(expirationDate);

    return jwtCreator.sign(Algorithm.HMAC256(secretKey));
  }

  /**
   * Crea un JWT con el ID de un enlace de restablecimiento de contraseña
   * y la direccion de correo electronico ingresada por el usuario para
   * dicho restablecimiento
   * 
   * @param passwordResetLinkId
   * @param email
   * @param secretKey clave secreta con la que se firma un JWT
   * @return referencia a un objeto de tipo String que contiene un JWT
   * utilizado para el restablecimiento de la contraseña de la cuenta
   * de un usuario
   */
  public static String createJwt(int passwordResetLinkId, String email, String secretKey) {
    JWTCreator.Builder jwtCreator = JWT.create();
    jwtCreator.withClaim(PASSWORD_RESET_LINK_ID, passwordResetLinkId);
    jwtCreator.withClaim(USER_EMAIL, email);

    return jwtCreator.sign(Algorithm.HMAC256(secretKey));
  }

  /**
   * Comprueba la expiracion de un JWT. Retorna true si y solo si
   * un JWT expiro.
   * 
   * Un JWT es creado con una cantidad de tiempo para ser usado.
   * Cuando pasa esa cantidad de tiempo, un JWT expira, con lo
   * cual, no es valido, y, en consecuencia, no sirve para
   * realizar peticiones HTTP.
   * 
   * @param jwt
   * @param secretKey clave secreta con la que se firma un JWT
   * @return true si el JWT dado expiro, false en caso contrario
   */
  public static boolean isExpired(String jwt, String secretKey) {
    boolean expired = true;

    try {
      checkExpiration(jwt, secretKey);
      expired = false;      
    } catch (TokenExpiredException e) {
      e.printStackTrace();
    }

    return expired;
  }

  /**
   * Comprueba si un JWT ha expirado o no
   * 
   * @param jwt
   * @param secretKey clave secreta con la que se firma un JWT
   */
  private static void checkExpiration(String jwt, String secretKey) {
    JWTVerifier jwtVerifier = buildJwtVerifier(secretKey);

    try {
      jwtVerifier.verify(jwt);
    } catch (JWTVerificationException givenException) {
      /*
       * Comprueba si la referencia almacenada en givenException
       * es del tipo TokenExpiredException. De ser asi, lanza
       * una excepcion TokenExpiredException.
       */
      if (givenException instanceof TokenExpiredException) {
        throw givenException;
      }

      givenException.printStackTrace();
    }

  }

  /**
   * Comprueba que un JWT sea valido, esto es que la firma de un JWT coincide
   * con los datos de su encabezado y carga util, y que un JWT no ha expirado
   * 
   * @param jwt
   * @param secretKey clave secreta con la que se firma un JWT
   * @return true si el JWT dado es valido, false en caso contrario
   */
  public static boolean validateJwt(String jwt, String secretKey) {
    JWTVerifier jwtVerifier = buildJwtVerifier(secretKey);
    boolean valid = false;

    try {
      jwtVerifier.verify(jwt);
      valid = true;      
    } catch (JWTVerificationException e) {
      e.printStackTrace();
    }

    return valid;
  }

  /**
   * Crea y retorna una instancia de JWTVerifier
   * 
   * @param secretKey clave secreta que se utiliza para firmar un JWT
   * @return una referencia a un objeto de tipo JWTVerifier
   */
  private static JWTVerifier buildJwtVerifier(String secretKey) {
    Verification verification = JWT.require(Algorithm.HMAC256(secretKey));
    return verification.build();
  }

  /**
   * Recupera el ID de usuario contenido en la carga util de un JWT
   * 
   * @param jwt
   * @param secretKey clave secreta con la que se firma un JWT
   * @return entero que contiene el ID de usuario contenido en la
   * carga util de un JWT
   */
  public static int getUserId(String jwt, String secretKey) {
    String payload = getDecodedPayload(jwt, secretKey);
    return Integer.parseInt(getValueKey(USER_ID_KEY, payload));
  }

  /**
   * Recupera el permiso de administrador (super usuario) contenido en la
   * carga util de un JWT
   * 
   * @param jwt
   * @param secretKey clave secreta con la que se firma un JWT
   * @return true si el valor asociado a la clave 'superuser' es true, false
   * si el valor asociado a la clave 'superuser' es false
   */
  public static boolean getSuperuser(String jwt, String secretKey) {
    String payload = getDecodedPayload(jwt, secretKey);
    return Boolean.parseBoolean(getValueKey(SUPERUSER_KEY, payload));
  }

  /**
   * Recupera el permiso para modificar el permiso de administrador
   * (super usuario) contenido en la carga util de un JWT
   * 
   * @param jwt
   * @param secretKey clave secreta con la que se firma un JWT
   * @return true si el valor asociado a la clave 'superuserPermissionModifier' es
   * true, false si el valor asociado a la clave 'superuserPermissionModifier' es
   * false
   */
  public static boolean getSuperuserPermissionModifier(String jwt, String secretKey) {
    String payload = getDecodedPayload(jwt, secretKey);
    return Boolean.parseBoolean(getValueKey(SUPERUSER_PERMISSION_MODIFIER_KEY, payload));
  }

  /**
   * Recupera el permiso de eliminacion de usuario contenido
   * en la carga util de un JWT
   * 
   * @param jwt
   * @param secretKey clave secreta con la que se firma un JWT
   * @return true si el valor asociado a la clave 'userDeletionPermission' es
   * true, false si el valor asociado a la clave 'userDeletionPermission' es
   * false
   */
  public static boolean getUserDeletionPermission(String jwt, String secretKey) {
    String payload = getDecodedPayload(jwt, secretKey);
    return Boolean.parseBoolean(getValueKey(USER_DELETION_PERMISSION_KEY, payload));
  }

  /**
   * Retorna la fecha de emision contenida en la carga util de un
   * JWT
   * 
   * @param jwt
   * @param secretKey clave secreta con la que se firma un JWT
   * @return referencia a un objeto de tipo Calendar que contiene
   * la fecha de emision de un JWT
   */
  public static Calendar getDateIssue(String jwt, String secretKey) {
    /*
     * Obtiene la carga util de un JWT, pero decodificada
     * de Base64
     */
    String payload = getDecodedPayload(jwt, secretKey);
    long issuedTime = Long.parseLong(getValueKey(ISSUED_AT_KEY, payload));

    /*
     * El tiempo de emision es multiplicado por 1000 para
     * convetirlo de segundos a milisegundos
     */
    Calendar dateIssue = Calendar.getInstance();
    dateIssue.setTimeInMillis(issuedTime * 1000);

    return dateIssue;
  }

  /**
   * Retorna la fecha de expiracion contenida en la carga util de un
   * JWT
   * 
   * @param jwt
   * @param secretKey clave secreta con la que se firma un JWT
   * @return referencia a un objeto de tipo Calendar que contiene
   * la fecha de expiracion de un JWT
   */
  public static Calendar getExpirationDate(String jwt, String secretKey) {
    /*
     * Obtiene la carga util de un JWT, pero decodificada
     * de Base64
     */
    String payload = getDecodedPayload(jwt, secretKey);
    long expirationTime = Long.parseLong(getValueKey(EXPIRES_AT_KEY, payload));

    /*
     * El tiempo de expiracion es multiplicado por 1000 para
     * convertirlo de segundos a milisegundos
     */
    Calendar expirationDate = Calendar.getInstance();
    expirationDate.setTimeInMillis(expirationTime * 1000);

    return expirationDate;
  }

  /**
   * Recupera el ID del enlace de restablecimiento de contraseña
   * contenido en la carga util de un JWT (de restablecimiento
   * de contraseña).
   * 
   * Este metodo es necesario para el restablecimiento de la
   * contraseña de la cuenta de un usuario.
   * 
   * @param jwt
   * @param secretKey clave secreta con la que se firma un JWT
   * @return entero que contiene el ID del enlace de restablecimiento
   * de contraseña contenido en la carga util de un JWT (de
   * restablecimiento de contraseña)
   */
  public static int getPasswordResetLinkId(String jwt, String secretKey) {
    String payload = getDecodedPayload(jwt, secretKey);
    return Integer.parseInt(getValueKey(PASSWORD_RESET_LINK_ID_KEY, payload));
  }

  /**
   * Recupera el correo electronico contenido en el carga util de
   * un JWT (de restablecimiento de contraseña).
   * 
   * Este metodo es necesario para el restablecimiento de la
   * contraseña de la cuenta de un usuario.
   * 
   * @param jwt
   * @param secretKey clave secreta con la que se firma un JWT
   * @return referencia a un objeto de tipo String que contiene
   * el correo electronico contenido en la carga util de un JWT
   * (de restablecimiento de contraseña)
   */
  public static String getUserEmail(String jwt, String secretKey) {
    String payload = getDecodedPayload(jwt, secretKey);
    return (getValueKey(USER_EMAIL_KEY, payload).replace("\"", ""));
  }

  /**
   * Obtiene el valor asociado a una clave dada de la carga util de
   * un JWT
   * 
   * @param key clave de la que se quiere obtener su valor asociado
   * de la carga util de un JWT
   * @param payload carga util decodificada de un JWT
   * @return referencia a un objeto de tipo String que contiene el valor
   * asociado a una clave dada de la carga util decodificada de un JWT
   */
  private static String getValueKey(String key, String payload) {
    /*
     * Elimina las llaves de apertura y cierre de la carga util
     * decodificada de un JWT para que las mismas no esten
     * presentes en el valor devuelto por este metodo.
     * 
     * Si no se eliminan las llaves de apertura y cierre, y
     * la clave provista es 'superuser', el valor devuelto
     * por este metodo contiene la cadena 'true' o 'false'
     * seguida de la llave de cierre, lo cual, provoca que
     * el metodo getSuperuser() retorne valores booleanos
     * incorrectos.
     * 
     * Por lo tanto, eliminar las llaves de apertura y cierre
     * garantiza el correcto funcionamiento de los metodos
     * que invoquen a este metodo.
     */
    payload = removeBraces(payload);

    /*
     * Crea un arreglo de tipo String que contiene cada uno de
     * los pares clave:valor de la carga util de un JWT
     * dividiendola por la coma
     */
    String[] keyValuePairs = payload.split(COMMA);
    String[] pair = null;
    String value = null;

    /*
     * Recorre cada uno de los pares clave:valor de la carga util
     * de un JWT hasta obtener el valor asociado a la clave provista
     * como argumento a este metodo
     */
    for (String currentPair : keyValuePairs) {
      /*
       * Crea un arreglo de tipo String que contiene la clave
       * y el valor de un par clave:valor dividiendolo por los
       * dos puntos. El primer elemento es la clave y el
       * segundo elemento es el valor.
       */
      pair = currentPair.split(TWO_POINTS);

      /*
       * Si la clave es igual a la clave provista, se obtiene el valor
       * asociado a la misma, el cual, puede ser el ID de usuario o
       * el permiso de administrador (super usuario) dependiendo
       * de lo que se desee recuperar de la carga util, decodificada
       * de un JWT, mediante la clave provista como argumento a este
       * metodo
       */
      if (pair[0].equals(key)) {
        value = pair[1];
        break;
      }

    }

    return value;
  }

  /**
   * Obtiene la carga util de un JWT, pero decodificada de Base64
   * 
   * @param jwt
   * @return referencia a un objeto de tipo String que contiene la carga
   * util de un JWT, pero decodificada de Base64
   */
  private static String getDecodedPayload(String jwt, String secretKey) {
		JWTVerifier jwtVerifier = buildJwtVerifier(secretKey);
		DecodedJWT decodedJwt = jwtVerifier.verify(jwt);
		byte[] decoded = Base64.getDecoder().decode(decodedJwt.getPayload());

		return new String(decoded, StandardCharsets.UTF_8);
	}

  /**
   * Elimina las llaves de apertura y cierre de la carga util decodificada
   * de un JWT
   * 
   * @param payload carga util decodificada de un JWT
   * @return referencia a un objeto de tipo String que contiene la carga
   * util decodificada de un JWT, pero sin las llaves de apertura y cierre
   */
  private static String removeBraces(String payload) {
    /*
     * Reemplaza las llaves de apertura y cierre de la carga util
     * decodificada de un JWT, por un espacio en blanco
     */
    return payload.replaceAll("[\\{]|[\\}]", "");
  }

}
