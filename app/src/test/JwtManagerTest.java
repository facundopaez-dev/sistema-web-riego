import static org.junit.Assert.*;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.Verification;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import model.User;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import stateless.SecretKeyServiceBean;
import stateless.UserServiceBean;
import utilJwt.JwtManager;

public class JwtManagerTest {
  private static UserServiceBean userService;
  private static SecretKeyServiceBean secretKeyService;
  private static EntityManager entityManager;
  private static EntityManagerFactory entityManagerFactory;
  private static Collection<User> users;
  private static String secretKey;

  @BeforeClass
  public static void preTest() {
    entityManagerFactory = Persistence.createEntityManagerFactory("swcar");
    entityManager = entityManagerFactory.createEntityManager();

    userService = new UserServiceBean();
    userService.setEntityManager(entityManager);

    secretKeyService = new SecretKeyServiceBean();
    secretKeyService.setEntityManager(entityManager);
    
    users = new ArrayList<>();
    secretKey = secretKeyService.find().getValue();
  }

  @Test
  public void testOneValidateJwt() {
    System.out.println("******************** Prueba uno del metodo validateJwt() ********************");
    System.out.println("- En esta prueba se realiza la validacion de un JWT (JSON Web Token) valido, el");
    System.out.println("cual, lo es porque no ha expirado y su firma coincide con los datos de su encabezado");
    System.out.println("y de su carga util. Por lo tanto, el metodo validateJwt() de la clase JwtManager");
    System.out.println("debe retornar el valor booleano true.");
    System.out.println();
    
    // **** Creacion y persistencia de un usuario ****
    User newUser = createUser("jackie", "jackie", "John", "Doe", "john@eservice.com");

    // Se persiste el usuario creado
    entityManager.getTransaction().begin();
    newUser = userService.create(newUser);
    entityManager.getTransaction().commit();

    printUserData(newUser);

    /*
     * Se agrega el usuario creado a una coleccion para su posterior
     * eliminacion de la base de datos subyacente, lo cual se hace
     * para que la misma tenga el estado que tenia antes de la ejecucion
     * de esta prueba unitaria
     */
    users.add(newUser);

    // **** Creacion de un JWT con el ID y el permiso del usuario creado ****
    String jwt = JwtManager.createJwt(newUser.getId(), newUser.getSuperuser(), secretKey);
    
    printDecodedPayload(jwt);

    // **** Seccion de prueba ****
    boolean result = JwtManager.validateJwt(jwt, secretKey);

    assertTrue(result);

    printResult(result);
    printSuccessfulMessage();
  }

  @Test
  public void testTwoValidateJwt() {
    System.out.println("******************** Prueba dos del metodo validateJwt() ********************");
    System.out.println("- En esta prueba se realiza la validacion de un JWT (JSON Web Token) que NO es");
    System.out.println("valido, ya que el valor de UNA de las claves de su encabezado fue alterado. En");
    System.out.println("consecuencia, la firma del JWT NO coincide con los datos de su encabezado. Por");
    System.out.println("lo tanto, el metodo validateJwt() de la clase JwtManager debe retornar el valor");
    System.out.println("booleano false.");
    System.out.println();
    System.out.println("La manera en la que se realiza esta prueba es reemplazando el encabezado de un JWT");
    System.out.println("por un encabezado que tiene un valor distinto para la clave 'typ'.");
    System.out.println();

    // **** Creacion y persistencia de un usuario ****
    User newUser = createUser("jackson", "jackson", "Jackson", "Doe", "jackson@eservice.com");
    
    // Se persiste el usuario creado
    entityManager.getTransaction().begin();
    newUser = userService.create(newUser);
    entityManager.getTransaction().commit();

    printUserData(newUser);

    /*
     * Se agrega el usuario creado a una coleccion para su posterior
     * eliminacion de la base de datos subyacente, lo cual se hace
     * para que la misma tenga el estado que tenia antes de la ejecucion
     * de esta prueba unitaria
     */
    users.add(newUser);

    // **** Creacion del primer JWT con el ID y el permiso del usuario creado ****
    String firstJwt = JwtManager.createJwt(newUser.getId(), newUser.getSuperuser(), secretKey);

    System.out.println("Primer JWT");
    printJwt(firstJwt);

    System.out.println("Encabezado del primer JWT");
    printHeader(firstJwt);

    System.out.println("Encabezado decodificado del primer JWT");
    printDecodedHeader(firstJwt);

    // **** Creacion del segundo JWT con el ID y el permiso del usuario creado ****
    /*
     * El encabezado de este JWT se usa para reemplazar el encabezado del primer JWT,
     * lo cual, se hace para demostrar que el metodo validateJwt() de la clase JwtManager
     * retorna el valor booleano false cuando se le pasa como argumento un JWT al que
     * se le modifico su encabezado
     */
    Map<String, Object> headerClaims = new HashMap<>();
    headerClaims.put("typ", "COR");

    String secondJwt = createJwt(newUser.getId(), newUser.getSuperuser(), headerClaims);

    System.out.println("Segundo JWT");
    printJwt(secondJwt);
    
    System.out.println("Encabezado del segundo JWT");
    printHeader(secondJwt);

    System.out.println("Encabezado decodificado del segundo JWT");
    printDecodedHeader(secondJwt);

    // **** Reemplazo del encabezado del primer JWT por el encabezado del segundo JWT ****
    firstJwt = firstJwt.replaceFirst(getHeader(firstJwt), getHeader(secondJwt));

    System.out.println("Primer JWT con su encabezado reemplazado por el encabezado del segundo JWT");
    printJwt(firstJwt);

    // **** Seccion de prueba ****
    boolean result = JwtManager.validateJwt(firstJwt, secretKey);

    assertFalse(result);

    printResult(result);
    printSuccessfulMessage();
  }

  @Test
  public void testThreeValidateJwt() {
    System.out.println("******************** Prueba tres del metodo validateJwt() ********************");
    System.out.println("- En esta prueba se realiza la validacion de un JWT (JSON Web Token) que NO es");
    System.out.println("valido, ya que el valor de UNA de las claves de su encabezado fue alterado. En");
    System.out.println("consecuencia, la firma del JWT NO coincide con los datos de su encabezado. Por");
    System.out.println("lo tanto, el metodo validateJwt() de la clase JwtManager debe retornar el valor");
    System.out.println("booleano false.");
    System.out.println();
    System.out.println("La manera en la que se realiza esta prueba es reemplazando el encabezado de un JWT");
    System.out.println("por un encabezado que tiene un valor distinto para la clave 'alg'.");
    System.out.println();

    // **** Creacion y persistencia de un usuario ****
    User newUser = createUser("jeremy", "jeremy", "Jeremy", "Doe", "jeremy@eservie.com");
    
    // Se persiste el usuario creado
    entityManager.getTransaction().begin();
    newUser = userService.create(newUser);
    entityManager.getTransaction().commit();

    printUserData(newUser);

    /*
     * Se agrega el usuario creado a una coleccion para su posterior
     * eliminacion de la base de datos subyacente, lo cual se hace
     * para que la misma tenga el estado que tenia antes de la ejecucion
     * de esta prueba unitaria
     */
    users.add(newUser);

    // **** Creacion del primer JWT con el ID y el permiso del usuario creado ****
    String firstJwt = JwtManager.createJwt(newUser.getId(), newUser.getSuperuser(), secretKey);

    System.out.println("Primer JWT");
    printJwt(firstJwt);

    System.out.println("Encabezado del primer JWT");
    printHeader(firstJwt);

    System.out.println("Encabezado decodificado del primer JWT");
    printDecodedHeader(firstJwt);

    // **** Creacion del segundo JWT con el ID y el permiso del usuario creado ****
    /*
     * El encabezado de este JWT se usa para reemplazar el encabezado del primer JWT,
     * lo cual, se hace para demostrar que el metodo validateJwt() de la clase JwtManager
     * retorna el valor booleano false cuando se le pasa como argumento un JWT al que
     * se le modifico su encabezado
     */
    String secondJwt = createJwtHMAC384(newUser.getId(), newUser.getSuperuser());

    System.out.println("Segundo JWT");
    printJwt(secondJwt);
    
    System.out.println("Encabezado del segundo JWT");
    printHeaderHMAC384(secondJwt);

    System.out.println("Encabezado decodificado del segundo JWT");
    printDecodedHeaderHMAC384(secondJwt);

    // **** Reemplazo del encabezado del primer JWT por el encabezado del segundo JWT ****
    firstJwt = firstJwt.replaceFirst(getHeader(firstJwt), getHeaderHMAC384(secondJwt));

    System.out.println("Primer JWT con su encabezado reemplazado por el encabezado del segundo JWT");
    printJwt(firstJwt);

    // **** Seccion de prueba ****
    boolean result = JwtManager.validateJwt(firstJwt, secretKey);

    assertFalse(result);

    printResult(result);
    printSuccessfulMessage();
  }

  @Test
  public void testFourValidateJwt() {
    System.out.println("******************** Prueba cuatro del metodo validateJwt() ********************");
    System.out.println("- En esta prueba se realiza la validacion de un JWT (JSON Web Token) que NO es");
    System.out.println("valido, ya que los valores de las claves de su encabezado fueron alterados.");
    System.out.println("En consecuencia, la firma del JWT NO coincide con los datos de su encabezado. Por lo");
    System.out.println("tanto, el metodo validateJwt() de la clase JwtManager debe retornar el valor booleano");
    System.out.println("false.");
    System.out.println();
    System.out.println("La manera en la que se realiza esta prueba es reemplazando el encabezado de un JWT por");
    System.out.println("un encabezado que tiene un valor distinto para las claves 'typ' y 'alg'.");
    System.out.println();

    // **** Creacion y persistencia de un usuario ****
    User newUser = createUser("jenna", "jenna", "Jenna", "Doe", "jenna@eservice.com");
    
    // Se persiste el usuario creado
    entityManager.getTransaction().begin();
    newUser = userService.create(newUser);
    entityManager.getTransaction().commit();

    printUserData(newUser);
    
    /*
     * Se agrega el usuario creado a una coleccion para su posterior
     * eliminacion de la base de datos subyacente, lo cual se hace
     * para que la misma tenga el estado que tenia antes de la ejecucion
     * de esta prueba unitaria
     */
    users.add(newUser);

    // **** Creacion del primer JWT con el ID y el permiso del usuario creado ****
    String firstJwt = JwtManager.createJwt(newUser.getId(), newUser.getSuperuser(), secretKey);

    System.out.println("Primer JWT");
    printJwt(firstJwt);

    System.out.println("Encabezado del primer JWT");
    printHeader(firstJwt);

    System.out.println("Encabezado decodificado del primer JWT");
    printDecodedHeader(firstJwt);

    // **** Creacion del segundo JWT con el ID y el permiso del usuario creado ****
    /*
     * El encabezado de este JWT se usa para reemplazar el encabezado del primer JWT,
     * lo cual, se hace para demostrar que el metodo validateJwt() de la clase JwtManager
     * retorna el valor booleano false cuando se le pasa como argumento un JWT al que
     * se le modifico su encabezado
     */
    Map<String, Object> headerClaims = new HashMap<>();
    headerClaims.put("typ", "COR");

    String secondJwt = createJwtHMAC384(newUser.getId(), newUser.getSuperuser(), headerClaims);

    System.out.println("Segundo JWT");
    printJwt(secondJwt);
    
    System.out.println("Encabezado del segundo JWT");
    printHeaderHMAC384(secondJwt);

    System.out.println("Encabezado decodificado del segundo JWT");
    printDecodedHeaderHMAC384(secondJwt);

    // **** Reemplazo del encabezado del primer JWT por el encabezado del segundo JWT ****
    firstJwt = firstJwt.replaceFirst(getHeader(firstJwt), getHeaderHMAC384(secondJwt));

    System.out.println("Primer JWT con su encabezado reemplazado por el encabezado del segundo JWT");
    printJwt(firstJwt);

    // **** Seccion de prueba ****
    boolean result = JwtManager.validateJwt(firstJwt, secretKey);

    assertFalse(result);

    printResult(result);
    printSuccessfulMessage();
  }

  @Test
  public void testFiveValidateJwt() {
    System.out.println("******************** Prueba cinco del metodo validateJwt() ********************");
    System.out.println("- En esta prueba se realiza la validacion de un JWT (JSON Web Token) que NO es");
    System.out.println("valido, ya que los valores de las claves de su carga util fueron alterados. En");
    System.out.println("consecuencia, la firma del JWT NO coincide con los datos de su carga util. Por lo");
    System.out.println("tanto, el metodo validateJwt() de la clase JwtManager debe retornar el valor booleano");
    System.out.println("false.");
    System.out.println();
    System.out.println("La manera en la que se realiza esta prueba es reemplazando la carga util de un JWT");
    System.out.println("por la carga util de otro JWT.");
    System.out.println();

    // **** Creacion y persistencia de usuarios ****
    User jersey = createUser("jersey", "jersey", "Jersey", "Doe", "jersey@eservice.com");
    jersey.setSuperuser(true);

    User tony = createUser("tony", "tony", "Tony", "Stark", "tony@eservice.com");
    
    // Se persiste el usuario creado
    entityManager.getTransaction().begin();
    jersey = userService.create(jersey);
    tony = userService.create(tony);
    entityManager.getTransaction().commit();

    printUserData(jersey);
    printUserData(tony);
    
    /*
     * Se agrega el usuario creado a una coleccion para su posterior
     * eliminacion de la base de datos subyacente, lo cual se hace
     * para que la misma tenga el estado que tenia antes de la ejecucion
     * de esta prueba unitaria
     */
    users.add(jersey);
    users.add(tony);

    // **** Creacion del primer JWT con el ID y el permiso del primer usuario ****
    String firstJwt = JwtManager.createJwt(jersey.getId(), jersey.getSuperuser(), secretKey);

    System.out.println("Primer JWT");
    printJwt(firstJwt);

    System.out.println("Carga util del primer JWT");
    printPayload(firstJwt);

    System.out.println("Carga util decodificada del primer JWT");
    printDecodedPayload(firstJwt);

    /*
     * **** Creacion de la fecha de emision y de la fecha de expiracion ****
     * La fecha de emision es el tiempo actual menos 6 minutos.
     * La fecha de expiracion es el tiempo de la fecha de emision mas 20
     * minutos.
     * 
     * La fecha de emision y la fecha de expiracion se crean de esta manera
     * para que el segundo JWT tenga distintos valores de emision y expiracion
     * en comparacion con el primer JWT. Esto se hace asi porque en esta prueba
     * se busca probar que el metodo validateJwt(), de la clase JwtManager, retorna
     * el valor booleano false en el caso en el que los valores de todas las claves
     * de la carga util de un JWT son alterados.
     */
    Date dateIssue = new Date();
    dateIssue.setTime(System.currentTimeMillis() - 360000);

    Date expirationDate = new Date();
    expirationDate.setTime(dateIssue.getTime() + 1200000);

    // **** Creacion del segundo JWT con el ID y el permiso del segundo usuario ****
    String secondJwt = createJwt(tony.getId(), tony.getSuperuser(), dateIssue, expirationDate);

    System.out.println("Segundo JWT");
    printJwt(secondJwt);
    
    System.out.println("Carga util del segundo JWT");
    printPayload(secondJwt);

    System.out.println("Carga util decodificada del segundo JWT");
    printDecodedPayload(secondJwt);

    // **** Reemplazo de la carga util del primer JWT por la carga util del segundo JWT ****
    firstJwt = firstJwt.replaceFirst(getPayload(firstJwt), getPayload(secondJwt));

    System.out.println("Primer JWT con su carga util reemplazada por la carga util del segundo JWT");
    printJwt(firstJwt);

    // **** Seccion de prueba ****
    boolean result = JwtManager.validateJwt(firstJwt, secretKey);

    assertFalse(result);

    printResult(result);
    printSuccessfulMessage();
  }

  @Test
  public void testSixValidateJwt() {
    System.out.println("******************** Prueba seis del metodo validateJwt() ********************");
    System.out.println("- En esta prueba se realiza la validacion de un JWT (JSON Web Token) que NO es");
    System.out.println("valido, ya que el valor de UNA de las claves de su carga util fue alterado.");
    System.out.println("En consecuencia, la firma del JWT NO coincide con los datos de su carga util. Por lo");
    System.out.println("tanto, el metodo validateJwt() de la clase JwtManager debe retornar el valor booleano");
    System.out.println("false.");
    System.out.println();
    System.out.println("La manera en la que se realiza esta prueba es reemplazando la carga util de un JWT que");
    System.out.println("tiene el par 'superuser: false' por la carga util de un JWT que tiene el par 'superuser: true'.");
    System.out.println();

    // **** Creacion y persistencia de usuario ****
    User newUser = createUser("bob", "bob", "Bob", "Doe", "bob@eservice.com");
    
    // Se persiste el usuario creado
    entityManager.getTransaction().begin();
    newUser = userService.create(newUser);
    entityManager.getTransaction().commit();

    printUserData(newUser);
    
    /*
     * Se agrega el usuario creado a una coleccion para su posterior
     * eliminacion de la base de datos subyacente, lo cual se hace
     * para que la misma tenga el estado que tenia antes de la ejecucion
     * de esta prueba unitaria
     */
    users.add(newUser);

    /*
     * ****
     * Creacion de la fecha de emision y de la fecha de expiracion para hacer
     * que el primer JWT y el segundo JWT tengan las mismas fechas de emision
     * y de expiracion. De esta manera y usando el mismo usuario, pero con el
     * permiso de administrador modificado, se consigue crear un segundo JWT
     * que difiere del primer JWT unicamente en el valor de la clave 'superuser'.
     * ****
     */
    Date dateIssue = new Date();
    Date expirationDate = new Date();
    expirationDate.setTime(dateIssue.getTime() + 60000);

    // **** Creacion del primer JWT con el ID y el permiso del primer usuario ****
    String firstJwt = createJwt(newUser.getId(), newUser.getSuperuser(), dateIssue, expirationDate);

    System.out.println("Primer JWT");
    printJwt(firstJwt);

    System.out.println("Carga util del primer JWT");
    printPayload(firstJwt);

    System.out.println("Carga util decodificada del primer JWT");
    printDecodedPayload(firstJwt);

    /*
     * ****
     * Se modifica el permiso de administrador del usuario para la creacion del
     * segundo JWT. Esto se hace para reemplazar la carga util del primer JWT
     * con la carga util del segundo JWT con el fin de demostrar que el metodo
     * validateJwt() de la clase JwtManager retorna el valor booleano false
     * cuando se le pasa como argumento un JWT que tiene su carga util alterada
     * (es decir, distinta a la carga util con la que fue creado).
     * ****
     */
    newUser.setSuperuser(true);

    // **** Creacion del segundo JWT con el ID y el permiso del segundo usuario ****
    String secondJwt = createJwt(newUser.getId(), newUser.getSuperuser(), dateIssue, expirationDate);

    System.out.println("Segundo JWT");
    printJwt(secondJwt);
    
    System.out.println("Carga util del segundo JWT");
    printPayload(secondJwt);

    System.out.println("Carga util decodificada del segundo JWT");
    printDecodedPayload(secondJwt);

    // **** Reemplazo de la carga util del primer JWT por la carga util del segundo JWT ****
    firstJwt = firstJwt.replaceFirst(getPayload(firstJwt), getPayload(secondJwt));

    System.out.println("Primer JWT con su carga util reemplazada por la carga util del segundo JWT");
    printJwt(firstJwt);

    // **** Seccion de prueba ****
    boolean result = JwtManager.validateJwt(firstJwt, secretKey);

    assertFalse(result);

    printResult(result);
    printSuccessfulMessage();
  }

  @Test
  public void testSevenValidateJwt() {
    System.out.println("******************** Prueba siete del metodo validateJwt() ********************");
    System.out.println("- En esta prueba se realiza la validacion de un JWT (JSON Web Token) que NO es");
    System.out.println("valido, ya que ha expirado. Por lo tanto, el metodo validateJwt() de la clase");
    System.out.println("JwtManager debe retornar el valor booleano false.");
    System.out.println();
    System.out.println("La manera en la que se realiza esta prueba es haciendo que una fecha de expiracion");
    System.out.println("dada, sea anterior a una fecha de emision dada.");
    System.out.println();

    // **** Creacion y persistencia de usuario ****
    User newUser = createUser("eisenhower", "eisenhower", "Eisenhower", "Doe", "eisenhower@eservice.com");
    
    // Se persiste el usuario creado
    entityManager.getTransaction().begin();
    newUser = userService.create(newUser);
    entityManager.getTransaction().commit();

    printUserData(newUser);
    
    /*
     * Se agrega el usuario creado a una coleccion para su posterior
     * eliminacion de la base de datos subyacente, lo cual se hace
     * para que la misma tenga el estado que tenia antes de la ejecucion
     * de esta prueba unitaria
     */
    users.add(newUser);

    /*
     * **** Creacion de la fecha de emision y de la fecha de expiracion ****
     * La fecha de expiracion esta un minuto antes de la fecha de emision
     * para hacer que el JWT sea un JWT expirado
     */
    Date dateIssue = new Date();
    Date expirationDate = new Date();
    expirationDate.setTime(dateIssue.getTime() - 1000);

    System.out.println("Fecha de emision: " + dateIssue);
    System.out.println("Fecha de expiracion: " + expirationDate);
    System.out.println();
    System.out.println("La fecha de expiracion es UN segundo anterior a la fecha de emision");
    System.out.println();

    // **** Creacion de un JWT con el ID y el permiso del usuario creado ****
    String jwt = createJwt(newUser.getId(), newUser.getSuperuser(), dateIssue, expirationDate);

    System.out.println("JWT");
    printJwt(jwt);

    // **** Seccion de prueba ****
    boolean result = JwtManager.validateJwt(jwt, secretKey);

    assertFalse(result);

    printResult(result);
    printSuccessfulMessage();
  }

  @Test
  public void testEightValidateJwt() {
    System.out.println("******************** Prueba ocho del metodo validateJwt() ********************");
    System.out.println("- En esta prueba se realiza la validacion de un JWT (JSON Web Token) que NO es");
    System.out.println("valido, ya que su encabezado y su carga util fueron alterados. En consecuencia,");
    System.out.println("la firma del JWT NO coincide con los datos de su encabezado y carga util. Por lo");
    System.out.println("tanto, el metodo validateJwt() de la clase JwtManager debe retornar el valor booleano");
    System.out.println("false.");
    System.out.println();
    System.out.println("La manera en la que se realiza esta prueba es reemplazando el encabezado y la carga");
    System.out.println("la carga util de un JWT por el encabezado y la carga util de otro JWT.");
    System.out.println();

    // **** Creacion y persistencia de usuarios ****
    User tom = createUser("tom", "tom", "Tom", "Doe", "tom@eservice.com");
    tom.setSuperuser(true);

    User jerry = createUser("jerry", "jerry", "Jerry", "Stark", "jerry@eservice.com");
    
    // Se persiste el usuario creado
    entityManager.getTransaction().begin();
    tom = userService.create(tom);
    jerry = userService.create(jerry);
    entityManager.getTransaction().commit();

    printUserData(tom);
    printUserData(jerry);
    
    /*
     * Se agrega el usuario creado a una coleccion para su posterior
     * eliminacion de la base de datos subyacente, lo cual se hace
     * para que la misma tenga el estado que tenia antes de la ejecucion
     * de esta prueba unitaria
     */
    users.add(tom);
    users.add(jerry);

    // **** Creacion del primer JWT con el ID y el permiso del primer usuario ****
    String firstJwt = JwtManager.createJwt(tom.getId(), tom.getSuperuser(), secretKey);

    System.out.println("Primer JWT");
    printJwt(firstJwt);

    System.out.println("Encabezado del primer JWT");
    printHeader(firstJwt);

    System.out.println("Carga util del primer JWT");
    printPayload(firstJwt);

    System.out.println("Encabezado decodificado del primer JWT");
    printDecodedHeader(firstJwt);

    System.out.println("Carga util decodificada del primer JWT");
    printDecodedPayload(firstJwt);

    // **** Creacion del segundo JWT con el ID y el permiso del segundo usuario ****
    String secondJwt = createJwtHMAC384(jerry.getId(), jerry.getSuperuser());

    System.out.println("Segundo JWT");
    printJwt(secondJwt);

    System.out.println("Encabezado del segundo JWT");
    printHeaderHMAC384(secondJwt);

    System.out.println("Carga util del segundo JWT");
    printPayloadHMAC384(secondJwt);

    System.out.println("Encabezado decodificado del segundo JWT");
    printDecodedHeaderHMAC384(secondJwt);

    System.out.println("Carga util decodificada del segundo JWT");
    printDecodedPayloadHMAC384(secondJwt);

    /*
     * ****
     * Reemplazo del encabezado y de la carga util del primer JWT por el encabezado
     * y la carga util del segundo JWT
     * ****
     */
    String firstJwtHeader = getHeader(firstJwt);
    String firstJwtPayload = getPayload(firstJwt);

    firstJwt = firstJwt.replaceFirst(firstJwtHeader, getHeaderHMAC384(secondJwt));
    firstJwt = firstJwt.replaceFirst(firstJwtPayload, getPayloadHMAC384(secondJwt));

    System.out.println("Primer JWT con su encabezado y su carga util reemplazados por el encabezado y la carga util del segundo JWT");
    printJwt(firstJwt);

    // **** Seccion de prueba ****
    boolean result = JwtManager.validateJwt(firstJwt, secretKey);

    assertFalse(result);

    printResult(result);
    printSuccessfulMessage();
  }

  @Test
  public void testNineValidateJwt() {
    System.out.println("******************** Prueba nueve del metodo validateJwt() ********************");
    System.out.println("- En esta prueba se realiza la validacion de un JWT (JSON Web Token) que NO es");
    System.out.println("valido, ya que su firma fue alterada. Por lo tanto, el metodo validateJwt() de la");
    System.out.println("clase JwtManager debe retornar el valor booleano false.");
    System.out.println(); 

    // **** Creacion y persistencia de usuario ****
    User newUser = createUser("willy", "willy", "Willy", "Doe", "willy@eservice.com");
    
    // Se persiste el usuario creado
    entityManager.getTransaction().begin();
    newUser = userService.create(newUser);
    entityManager.getTransaction().commit();

    printUserData(newUser);

    /*
     * Se agrega el usuario creado a una coleccion para su posterior
     * eliminacion de la base de datos subyacente, lo cual se hace
     * para que la misma tenga el estado que tenia antes de la ejecucion
     * de esta prueba unitaria
     */
    users.add(newUser);

    // **** Creacion de un JWT con el ID y el permiso del usuario creado ****
    String jwt = JwtManager.createJwt(newUser.getId(), newUser.getSuperuser(), secretKey);

    System.out.println("JWT");
    printJwt(jwt);

    System.out.println("Carga util decodificada del JWT");
    printDecodedPayload(jwt);

    System.out.println("Firma del JWT");
    printSignature(jwt);
    
    // **** Alteracion de la firma del JWT ****
    String alterChain = "---XYZ";
    
    jwt = jwt + alterChain;

    System.out.println("JWT con su firma alterada");
    printJwt(jwt);

    // **** Seccion de prueba ****
    boolean result = JwtManager.validateJwt(jwt, secretKey);

    assertFalse(result);

    printResult(result);
    printSuccessfulMessage();
  }

  @Test
  public void testGetUserId() {
    System.out.println("******************** Prueba del metodo getUserId() ********************");
    System.out.println("- En esta prueba se verifica que el metodo getUserId() de la clase JwtManager");
    System.out.println("retorna el ID de usuario contenido en la carga util de un JWT dado.");
    System.out.println();

    // **** Creacion y persistencia de usuario ****
    User newUser = createUser("leon", "leon", "Leon", "Doe", "leon@service.com");
    
    // Se persiste el usuario creado
    entityManager.getTransaction().begin();
    newUser = userService.create(newUser);
    entityManager.getTransaction().commit();

    printUserData(newUser);

    /*
     * Se agrega el usuario creado a una coleccion para su posterior
     * eliminacion de la base de datos subyacente, lo cual se hace
     * para que la misma tenga el estado que tenia antes de la ejecucion
     * de esta prueba unitaria
     */
    users.add(newUser);

    // **** Creacion de un JWT con el ID y el permiso del usuario creado ****
    String jwt = JwtManager.createJwt(newUser.getId(), newUser.getSuperuser(), secretKey);

    System.out.println("JWT");
    printJwt(jwt);

    System.out.println("Carga util decodificada del JWT");
    printDecodedPayload(jwt);

    // **** Seccion de prueba ****
    int obtainedUserId = JwtManager.getUserId(jwt, secretKey);

    assertEquals(newUser.getId(), obtainedUserId);
    
    System.out.println("ID de usuario devuelto por el metodo getUserId(): " + obtainedUserId);
    System.out.println();
    
    printSuccessfulMessage();
  }

  @Test
  public void testOneGetSuperuser() {
    System.out.println("******************** Prueba uno del metodo getSuperuser() ********************");
    System.out.println("- En esta prueba se verifica que el metodo getSuperuser() de la clase JwtManager");
    System.out.println("retorna el permiso de administrador (super usuario) contenido en la carga util de un JWT dado.");
    System.out.println();
    System.out.println("En este caso, dicho metodo sera probado con un usuario que NO tiene el permiso de administrador.");
    System.out.println("Por lo tanto, el valor que debe retornar getSuperuser() es false.");
    System.out.println();

    // **** Creacion y persistencia de usuario ****
    User newUser = createUser("luke", "luke", "Luke", "Doe", "luke@eservice.com");
    
    // Se persiste el usuario creado
    entityManager.getTransaction().begin();
    newUser = userService.create(newUser);
    entityManager.getTransaction().commit();

    printUserData(newUser);

    /*
     * Se agrega el usuario creado a una coleccion para su posterior
     * eliminacion de la base de datos subyacente, lo cual se hace
     * para que la misma tenga el estado que tenia antes de la ejecucion
     * de esta prueba unitaria
     */
    users.add(newUser);

    // **** Creacion de un JWT con el ID y el permiso del usuario creado ****
    String jwt = JwtManager.createJwt(newUser.getId(), newUser.getSuperuser(), secretKey);

    System.out.println("JWT");
    printJwt(jwt);

    System.out.println("Carga util decodificada del JWT");
    printDecodedPayload(jwt);

    // **** Seccion de prueba ****
    boolean obtainedSuperuser = JwtManager.getSuperuser(jwt, secretKey);

    assertTrue(newUser.getSuperuser() == obtainedSuperuser);
    
    System.out.println("Permiso de administrador devuelto por el metodo getSuperuser(): " + obtainedSuperuser);
    System.out.println();
    
    printSuccessfulMessage();
  }

  @Test
  public void testTwoGetSuperuser() {
    System.out.println("******************** Prueba dos del metodo getSuperuser() ********************");
    System.out.println("- En esta prueba se verifica que el metodo getSuperuser() de la clase JwtManager");
    System.out.println("retorna el permiso de administrador (super usuario) contenido en la carga util de un JWT dado.");
    System.out.println();
    System.out.println("En este caso, dicho metodo sera probado con un usuario que tiene el permiso de administrador.");
    System.out.println("Por lo tanto, el valor que debe retornar getSuperuser() es true.");
    System.out.println();

    // **** Creacion y persistencia de usuario ****
    User newUser = createUser("george", "george", "George", "Doe", "george@eservice");
    newUser.setSuperuser(true);
    
    // Se persiste el usuario creado
    entityManager.getTransaction().begin();
    newUser = userService.create(newUser);
    entityManager.getTransaction().commit();

    printUserData(newUser);

    /*
     * Se agrega el usuario creado a una coleccion para su posterior
     * eliminacion de la base de datos subyacente, lo cual se hace
     * para que la misma tenga el estado que tenia antes de la ejecucion
     * de esta prueba unitaria
     */
    users.add(newUser);

    // **** Creacion de un JWT con el ID y el permiso del usuario creado ****
    String jwt = JwtManager.createJwt(newUser.getId(), newUser.getSuperuser(), secretKey);

    System.out.println("JWT");
    printJwt(jwt);

    System.out.println("Carga util decodificada del JWT");
    printDecodedPayload(jwt);

    // **** Seccion de prueba ****
    boolean obtainedSuperuser = JwtManager.getSuperuser(jwt, secretKey);
    
    assertTrue(newUser.getSuperuser() == obtainedSuperuser);
    
    System.out.println("Permiso de administrador devuelto por el metodo getSuperuser(): " + obtainedSuperuser);
    System.out.println();
    
    printSuccessfulMessage();
  }

  @AfterClass
  public static void postTest() {
    entityManager.getTransaction().begin();
    
    /*
     * Elimina de la base de datos subyacente los usuarios persistidos durante la
     * ejecucion de la prueba unitaria para dejarla en su estado original,
     * es decir, para dejarla en el estado en el que estaba antes de que se
     * persistieran los usuarios creados
     */
    for (User currentUser : users) {
      userService.remove(currentUser.getId());
    }

    entityManager.getTransaction().commit();

    // Cierra las conexiones
    entityManager.close();
    entityManagerFactory.close();
  }

  /**
   * Crea y retorna un usuario con los datos provistos en los
   * parametros
   * 
   * @param username
   * @param password
   * @return referencia a un objeto de tipo User
   */
  private User createUser(String username, String password, String name, String lastName, String email) {
    User newUser = new User();
    newUser.setUsername(username);
    newUser.setPassword(password);
    newUser.setName(name);
    newUser.setLastName(lastName);
    newUser.setEmail(email);

    return newUser;
  }

  /**
   * Imprime los datos de un usuario
   * 
   * @param givenUser
   */
  private void printUserData(User givenUser) {
    System.out.println("Datos del usuario persistido");
    System.out.println("ID: " + givenUser.getId());
    System.out.println("Nombre de usuario: " + givenUser.getUsername());
    System.out.println("Contraseña: " + givenUser.getPassword());
    System.out.println("Permiso de super usuario (administrador): " + givenUser.getSuperuser());
    System.out.println();
  }

  /**
   * Imprime el encabezado de un JWT decodificado de Base64
   * 
   * @param jwt
   */
  private void printDecodedHeader(String jwt) {
    System.out.println(getDecodedHeader(jwt));
    System.out.println();
  }

  /**
   * Imprime el encabezado decodificado de Base64 de un JWT
   * creado con el algoritmo HMAC384
   * 
   * @param jwt
   */
  private void printDecodedHeaderHMAC384(String jwt) {
    System.out.println(getDecodedHeaderHMAC384(jwt));
    System.out.println();
  }

  /**
   * Imprime la carga util de un JWT decodificada de Base64
   * 
   * @param jwt
   */
  private void printDecodedPayload(String jwt) {
    System.out.println(getDecodedPayload(jwt));
    System.out.println();
  }

  /**
   * Imprime la carga util decodificada de un JWT creado con
   * el algoritmo HMAC384
   * 
   * @param jwt
   */
  private void printDecodedPayloadHMAC384(String jwt) {
    System.out.println(getDecodedPayloadHMAC384(jwt));
    System.out.println();
  }

  /**
   * Imprime el mensaje de prueba satisfactoria
   */
  private void printSuccessfulMessage() {
    System.out.println("* Prueba pasada satisfactoriamente.");
    System.out.println();
  }

  /**
   * Imprime el resultado devuelto por el metodo validateJwt()
   * de la clase JwtManager
   * 
   * @param result
   */
  private void printResult(boolean result) {
    System.out.println("Resultado devuelto por el metodo validateJwt(): " + result);
    System.out.println();
  }

  /**
   * Imprime un JWT
   * 
   * @param jwt
   */
  private void printJwt(String jwt) {
    System.out.println(jwt);
    System.out.println();
  }
  
  /**
   * Imprime el encabezado en Base64 de un JWT creado
   * con el algoritmo HMAC256
   * 
   * @param jwt
   */
  private void printHeader(String jwt) {
    System.out.println(getHeader(jwt));
    System.out.println();
  }

  /**
   * Imprime la carga util en Base64 de un JWT creado
   * con el algoritmo HMAC256
   * 
   * @param jwt
   */
  private void printPayload(String jwt) {
    System.out.println(getPayload(jwt));
    System.out.println();
  }

  /**
   * Imprime la firma en Base64 de un JWT creado
   * con el algoritmo HMAC256
   * 
   * @param jwt
   */
  private void printSignature(String jwt) {
    System.out.println(getSignature(jwt));
    System.out.println();
  }

  /**
   * Imprime el encabezado en Base64 de un JWT creado
   * con el algoritmo HMAC384
   * 
   * @param jwt
   */
  private void printHeaderHMAC384(String jwt) {
    System.out.println(getHeaderHMAC384(jwt));
    System.out.println();
  }

  /**
   * Imprime la carga util en Base64 de un JWT creado
   * con el algoritmo HMAC384
   * 
   * @param jwt
   */
  private void printPayloadHMAC384(String jwt) {
    System.out.println(getPayloadHMAC384(jwt));
    System.out.println();
  }

  /**
   * Crea y retorna una instancia de JWTVerifier
   * 
   * @return referencia a un objeto de tipo JWTVerifier que usa el algoritmo
   * HMAC256
   */
  private static JWTVerifier buildJwtVerifier() {
    Verification verification = JWT.require(Algorithm.HMAC256(secretKey));
    return verification.build();
  }

  /**
   * Crea y retorna una instancia de JWTVerifier que utiliza el algoritmo
   * HMAC384
   * 
   * @return referencia a un objeto de tipo JWTVerifier que usa el algoritmo
   * HMAC384
   */
  private static JWTVerifier buildJwtVerifierHMAC384() {
    Verification verification = JWT.require(Algorithm.HMAC384(secretKey));
    return verification.build();
  }

  /**
   * Crea un JWT con el ID y el permiso de administrador de un usuario, y
   * una coleccion de pares clave:valor para el encabezado del JWT
   * 
   * @param userId
   * @param superuserPermission
   * @param headerClaims coleccion de pares clave:valor para el encabezado del JWT a crear
   * @return referencia a un objeto de tipo String que contiene un JWT
   * creado con el algoritmo HMAC256
   */
  private String createJwt(int userId, boolean superuserPermission, Map<String, Object> headerClaims) {
    JWTCreator.Builder jwtCreator = JWT.create();
    jwtCreator.withClaim("userId", userId);
    jwtCreator.withClaim("superuser", superuserPermission);
    jwtCreator.withHeader(headerClaims);

    return jwtCreator.sign(Algorithm.HMAC256(secretKey));
  }

  /**
   * Crea un JWT con el ID y el permiso de un usuario, una fecha
   * de emision y una fecha de expiracion
   * 
   * @param userId ID de un usuario
   * @param superuserPermission permiso de un usuario
   * @param dateIssue fecha de emision de un JWT
   * @param expirationDate fecha de expiracion de un JWT
   * @return referencia a un objeto de tipo String que contiene un JWT
   * creado con el algoritmo HMAC256
   */
  private String createJwt(int userId, boolean superuserPermission, Date dateIssue, Date expirationDate) {
    JWTCreator.Builder jwtCreator = JWT.create();
    jwtCreator.withClaim("userId", userId);
    jwtCreator.withClaim("superuser", superuserPermission);
    jwtCreator.withIssuedAt(dateIssue);
    jwtCreator.withExpiresAt(expirationDate);

    return jwtCreator.sign(Algorithm.HMAC256(secretKey));
  }

  /**
   * Crea un JWT con el ID y el permiso de administrador de un usuario
   * mediante el algoritmo HMAC384
   * 
   * @param userId
   * @param superuserPermission
   * @return referencia a un objeto de tipo String que contiene un JWT
   * creado con el algoritmo HMAC384
   */
  private String createJwtHMAC384(int userId, boolean superuserPermission) {
    JWTCreator.Builder jwtCreator = JWT.create();
    jwtCreator.withClaim("userId", userId);
    jwtCreator.withClaim("superuser", superuserPermission);

    return jwtCreator.sign(Algorithm.HMAC384(secretKey));
  }

  /**
   * Crea un JWT con el ID y el permiso de administrador de un usuario, y
   * una coleccion de pares clave:valor para el encabezado, mediante el
   * algoritmo HMAC384
   * 
   * @param userId
   * @param superuserPermission
   * @param headerClaims coleccion de pares clave:valor para el encabezado del JWT a crear
   * @return referencia a un objeto de tipo String que contiene un JWT
   * creado con el algoritmo HMAC384
   */
  private String createJwtHMAC384(int userId, boolean superuserPermission, Map<String, Object> headerClaims) {
    JWTCreator.Builder jwtCreator = JWT.create();
    jwtCreator.withClaim("userId", userId);
    jwtCreator.withClaim("superuser", superuserPermission);
    jwtCreator.withHeader(headerClaims);

    return jwtCreator.sign(Algorithm.HMAC384(secretKey));
  }

  /**
   * Obtiene el encabezado de un JWT creado con el algoritmo HMAC256
   * 
   * @param jwt
   * @return referencia a un objeto de tipo String que contiene el encabezado
   * de un JWT
   */
  private String getHeader(String jwt) {
    JWTVerifier jwtVerifier = buildJwtVerifier();
    DecodedJWT decodedJwt = jwtVerifier.verify(jwt);

    return decodedJwt.getHeader();
  }

  /**
   * Obtiene la carga util de un JWT creado con el algoritmo HMAC256
   * 
   * @param jwt
   * @return referencia a un objeto de tipo String que contiene la carga util
   * de un JWT
   */
  private String getPayload(String jwt) {
    JWTVerifier jwtVerifier = buildJwtVerifier();
    DecodedJWT decodedJwt = jwtVerifier.verify(jwt);

    return decodedJwt.getPayload();
  }

  /**
   * Obtiene la firma de un JWT creado con el algoritmo HMAC256
   * 
   * @param jwt
   * @return referencia a un objeto de tipo String que contiene la firma
   * de un JWT
   */
  private String getSignature(String jwt) {
    JWTVerifier jwtVerifier = buildJwtVerifier();
    DecodedJWT decodedJwt = jwtVerifier.verify(jwt);

    return decodedJwt.getSignature();
  }

  /**
   * Obtiene el encabezado decodificado de un JWT creado con el
   * algoritmo HMAC256
   * 
   * @param jwt
   * @return referencia a un objeto de tipo String que contiene el
   * encabezado decodificado de un JWT
   */
  private String getDecodedHeader(String jwt) {
    JWTVerifier jwtVerifier = buildJwtVerifier();
    DecodedJWT decodedJwt = jwtVerifier.verify(jwt);
    byte[] decoded = Base64.getDecoder().decode(decodedJwt.getHeader());

    return new String(decoded, StandardCharsets.UTF_8);
  }

  /**
   * Obtiene la carga util decodificada de un JWT creado con
   * el algoritmo HMAC256
   * 
   * @param jwt
   * @return referencia a un objeto de tipo String que contiene la
   * carga util decodificada de un JWT
   */
  private String getDecodedPayload(String jwt) {
    JWTVerifier jwtVerifier = buildJwtVerifier();
    DecodedJWT decodedJwt = jwtVerifier.verify(jwt);
    byte[] decoded = Base64.getDecoder().decode(decodedJwt.getPayload());

    return new String(decoded, StandardCharsets.UTF_8);
  }

  /**
   * Obtiene el encabezado de un JWT creado con el algoritmo HMAC384
   * 
   * @param jwt
   * @return referencia a un objeto de tipo String que contiene el encabezado
   * de un JWT creado con el algoritmo HMAC384
   */
  private String getHeaderHMAC384(String jwt) {
    JWTVerifier jwtVerifier = buildJwtVerifierHMAC384();
    DecodedJWT decodedJwt = jwtVerifier.verify(jwt);

    return decodedJwt.getHeader();
  }

  /**
   * Obtiene la carga util de un JWT creado con el algoritmo HMAC384
   * 
   * @param jwt
   * @return referencia a un objeto de tipo String que contiene la carga util
   * de un JWT creado con el algoritmo HMAC384
   */
  private String getPayloadHMAC384(String jwt) {
    JWTVerifier jwtVerifier = buildJwtVerifierHMAC384();
    DecodedJWT decodedJwt = jwtVerifier.verify(jwt);

    return decodedJwt.getPayload();
  }

  /**
   * Obtiene el encabezado decodificado de un JWT creado con el
   * algoritmo HMAC384
   * 
   * @param jwt
   * @return referencia a un objeto de tipo String que contiene el
   * encabezado decodificado de un JWT creado con el algoritmo HMAC384
   */
  private String getDecodedHeaderHMAC384(String jwt) {
    JWTVerifier jwtVerifier = buildJwtVerifierHMAC384();
    DecodedJWT decodedJwt = jwtVerifier.verify(jwt);
    byte[] decoded = Base64.getDecoder().decode(decodedJwt.getHeader());

    return new String(decoded, StandardCharsets.UTF_8);
  }

  /**
   * Obtiene la carga util decodificada de un JWT creado con
   * el algoritmo HMAC384
   * 
   * @param jwt
   * @return referencia a un objeto de tipo String que tiene la carga util
   * decodificada de un JWT creado con el algoritmo HMAC384
   */
  private String getDecodedPayloadHMAC384(String jwt) {
    JWTVerifier jwtVerifier = buildJwtVerifierHMAC384();
    DecodedJWT decodedJwt = jwtVerifier.verify(jwt);
    byte[] decoded = Base64.getDecoder().decode(decodedJwt.getPayload());

    return new String(decoded, StandardCharsets.UTF_8);
  }

}
