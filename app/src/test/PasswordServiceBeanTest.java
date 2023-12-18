import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import model.Password;
import model.User;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import stateless.PasswordServiceBean;
import stateless.UserServiceBean;

public class PasswordServiceBeanTest {

  private static EntityManager entityManager;
  private static EntityManagerFactory entityManagerFactory;

  private static UserServiceBean userService;
  private static PasswordServiceBean passwordService;

  private static Collection<User> users;
  private static Collection<Password> passwords;

  @BeforeClass
  public static void preTest() {
    entityManagerFactory = Persistence.createEntityManagerFactory("swcar");
    entityManager = entityManagerFactory.createEntityManager();

    userService = new UserServiceBean();
    userService.setEntityManager(entityManager);

    passwordService = new PasswordServiceBean();
    passwordService.setEntityManager(entityManager);

    users = new ArrayList<>();
    passwords = new ArrayList<>();
  }

  @Test
  public void testAuthenticateUserOne() {
    System.out.println("***************************** Prueba uno del metodo authenticateUser() *****************************");
    printMethodToTest();

    System.out.println("# Descripcion de la prueba unitaria");
    System.out.println("En esta prueba unitaria se utilizan un nombre de usuario y una contraseña de un usuario que coinciden");
    System.out.println("con los que estan almacenados en la base de datos subyacente. Por lo tanto, el metodo authenticateUser()");
    System.out.println("de la clase PasswordServiceBean debe retornar true.");
    System.out.println();

    /*
     * Esta contraseña plana se usa para autentificar al
     * usuario de esta prueba
     */
    String plainPassword = "Ultra secret password";
    String username = "dominic";

    /*
     * Creacion y persistencia de un usuario de prueba
     */
    User testUser = setUserData(username, "Dominic", "Doe", "dominic@eservice.com");
    printUserData(testUser);

    entityManager.getTransaction().begin();
    testUser = userService.create(testUser);
    entityManager.getTransaction().commit();

    /*
     * Creacion y persistencia de una contraseña para
     * un usuario de prueba
     */
    Password testPassword = new Password();
    testPassword.setValue(passwordService.getPasswordHash(plainPassword));
    testPassword.setUser(testUser);

    entityManager.getTransaction().begin();
    testPassword = passwordService.create(testPassword);
    entityManager.getTransaction().commit();

    /*
     * Se agregan los datos creados a una coleccion para su posterior
     * eliminacion de la base de datos subyacente, lo cual se hace
     * para que la misma tenga el estado que tenia antes de la ejecucion
     * de esta prueba unitaria
     */
    users.add(testUser);
    passwords.add(testPassword);

    /*
     * Seccion de prueba
     */
    System.out.println("Datos utilizados para la autenticacion del usuario");
    System.out.println("Nombre de usuario (correcto): " + username);
    System.out.println("Contraseña en texto plano (correcta): " + plainPassword);
    System.out.println();

    boolean authenticationResult = passwordService.authenticateUser(username, plainPassword);

    System.out.println("Resultado esperado: " + true);
    System.out.println("Resultado de la autenticacion del usuario: " + authenticationResult);
    System.out.println();

    assertTrue(authenticationResult);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testAuthenticateUserTwo() {
    System.out.println("***************************** Prueba dos del metodo authenticateUser() *****************************");
    printMethodToTest();

    System.out.println("# Descripcion de la prueba unitaria");
    System.out.println("En esta prueba unitaria se utilizan un nombre de usuario y una contraseña de un usuario, de los cuales");
    System.out.println("la contraseña NO coincide con la que esta almacenada en la base de datos subyacente. Por lo tanto,");
    System.out.println("el metodo authenticateUser() de la clase PasswordServiceBean debe retornar false.");
    System.out.println();

    /*
     * Esta contraseña plana se usa para autentificar al
     * usuario de esta prueba
     */
    String wrongPlainPassword = "Ultra secret password";
    String username = "matthew";

    /*
     * El hash de la contraseña correcta se usa para mostrar
     * la diferencia con el hash de la contraseña incorrecta
     */
    String correctPassword = "Awesome password";
    String hashCorrectPassword = passwordService.getPasswordHash(correctPassword);

    /*
     * Creacion y persistencia de un usuario de prueba
     */
    User testUser = setUserData(username, "Matthew", "Doe", "matthew@eservice.com");
    printUserData(testUser);

    entityManager.getTransaction().begin();
    testUser = userService.create(testUser);
    entityManager.getTransaction().commit();

    /*
     * Creacion y persistencia de una contraseña para
     * un usuario de prueba
     */
    Password testPassword = new Password();
    testPassword.setValue(passwordService.getPasswordHash(correctPassword));
    testPassword.setUser(testUser);

    entityManager.getTransaction().begin();
    testPassword = passwordService.create(testPassword);
    entityManager.getTransaction().commit();

    /*
     * Se agregan los datos creados a una coleccion para su posterior
     * eliminacion de la base de datos subyacente, lo cual se hace
     * para que la misma tenga el estado que tenia antes de la ejecucion
     * de esta prueba unitaria
     */
    users.add(testUser);
    passwords.add(testPassword);

    /*
     * Seccion de prueba
     */
    System.out.println("Datos utilizados para la autenticacion del usuario");
    System.out.println("Nombre de usuario (correcto): " + username);
    System.out.println("Contraseña en texto plano (incorrecta): " + wrongPlainPassword);
    System.out.println();
    System.out.println("Hash de la contraseña incorrecta: " + passwordService.getPasswordHash(wrongPlainPassword));
    System.out.println();
    System.out.println("Contraseña correcta: " + correctPassword);
    System.out.println("Hash de la contraseña correcta: " + hashCorrectPassword);
    System.out.println();

    boolean authenticationResult = passwordService.authenticateUser(username, wrongPlainPassword);

    System.out.println("Resultado esperado: " + false);
    System.out.println("Resultado de la autenticacion del usuario: " + authenticationResult);
    System.out.println();

    assertFalse(authenticationResult);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testAuthenticateUserThree() {
    System.out.println("***************************** Prueba tres del metodo authenticateUser() *****************************");
    printMethodToTest();

    System.out.println("# Descripcion de la prueba unitaria");
    System.out.println("En esta prueba unitaria se utilizan un nombre de usuario y una contraseña de un usuario, de los cuales");
    System.out.println("el nombre de usuario NO coincide con el que esta almacenado en la base de datos subyacente. Por lo");
    System.out.println("tanto, el metodo authenticateUser() de la clase PasswordServiceBean debe retornar false.");
    System.out.println();

    /*
     * Nombre de usuario con el que se crea el usuario de
     * esta prueba
     */
    String username = "brian";

    /*
     * Estos datos se utilizan para autentificar al
     * usuario de esta prueba
     */
    String incorrectUsername = username + "_91";
    String plainPassword = "Ultra secret password";

    /*
     * Creacion y persistencia de un usuario de prueba
     */
    User testUser = setUserData(username, "Brian", "Doe", "brian@eservice.com");
    printUserData(testUser);

    entityManager.getTransaction().begin();
    testUser = userService.create(testUser);
    entityManager.getTransaction().commit();

    /*
     * Creacion y persistencia de una contraseña para
     * un usuario de prueba
     */
    Password testPassword = new Password();
    testPassword.setValue(passwordService.getPasswordHash(plainPassword));
    testPassword.setUser(testUser);

    entityManager.getTransaction().begin();
    testPassword = passwordService.create(testPassword);
    entityManager.getTransaction().commit();

    /*
     * Se agregan los datos creados a una coleccion para su posterior
     * eliminacion de la base de datos subyacente, lo cual se hace
     * para que la misma tenga el estado que tenia antes de la ejecucion
     * de esta prueba unitaria
     */
    users.add(testUser);
    passwords.add(testPassword);

    /*
     * Seccion de prueba
     */
    System.out.println("Datos utilizados para la autenticacion del usuario");
    System.out.println("Nombre de usuario (incorrecto): " + incorrectUsername);
    System.out.println("Contraseña en texto plano (correcta): " + plainPassword);
    System.out.println();

    System.out.println("Hash de la contraseña: " + passwordService.getPasswordHash(plainPassword));
    System.out.println("Hash persistido de la contraseña: " + testPassword.getValue());
    System.out.println();

    boolean authenticationResult = passwordService.authenticateUser(incorrectUsername, plainPassword);

    System.out.println("Resultado esperado: " + false);
    System.out.println("Resultado de la autenticacion del usuario: " + authenticationResult);
    System.out.println();

    assertFalse(authenticationResult);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testAuthenticateUserFour() {
    System.out.println("***************************** Prueba cuatro del metodo authenticateUser() *****************************");
    printMethodToTest();

    System.out.println("En esta prueba unitaria se utilizan un nombre de usuario y una contraseña de un usuario inexistentes en");
    System.out.println("la base de datos subyacente. Autenticar un usuario inexistente es similar a autentincar un usuario que");
    System.out.println("provee un nombre de usuario incorrecto o una contraseña incorrecta o un nombre de usuario y una contraseña");
    System.out.println("incorrectos. Por lo tanto, el metodo authenticateUser() de la clase PasswordServiceBean debe retornar");
    System.out.println("false.");
    System.out.println();

    /*
     * Datos de prueba
     */
    String username = "Eisenhower";
    String password = "Super secret password";

    /*
     * Con esto se demuestra si el usuario con el nombre de
     * usuario dado, existe en la base de datos subyacente
     */
    if (userService.findByUsername(username) == null) {
      System.out.println("* AVISO *");
      System.out.println("El usuario con el nombre de usuario " + username + " NO existe en la base de datos subyacente.");
      System.out.println();
    } else {
      System.out.println("* ADVERTENCIA");
      System.out.println("El usuario con el nombre de usuario " + username + " SI existe en la base de datos subyacente.");
      System.out.println("Por lo tanto, si la contraseña provista para la autenticacion es igual a la del usuario, esta prueba fallara.");
      System.out.println();
    }

    /*
     * Seccion de prueba
     */
    System.out.println("Datos utilizados para la autenticacion del usuario (inexistente)");
    System.out.println("Nombre de usuario (inexistente): " + username);
    System.out.println("Contraseña (inexistente): " + password);
    System.out.println();

    boolean authenticationResult = passwordService.authenticateUser(username, password);

    System.out.println("Resultado esperado: " + false);
    System.out.println("Resultado de la autenticacion del usuario: " + authenticationResult);
    System.out.println();

    assertFalse(authenticationResult);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testAuthenticateUserFive() {
    System.out.println("***************************** Prueba cinco del metodo authenticateUser() *****************************");
    printMethodToTest();

    System.out.println("En esta prueba unitaria se utilizan un nombre de usuario y una contraseña de un usuario indefinidos.");
    System.out.println("Autenticar un usuario indefinido es similar a autentincar un usuario que provee un nombre de usuario");
    System.out.println("incorrecto o una contraseña incorrecta o un nombre de usuario y una contraseña incorrectos. Por lo");
    System.out.println("tanto, el metodo authenticateUser() de la clase PasswordServiceBean debe retornar false.");
    System.out.println();

    /*
     * Datos de prueba
     */
    String username = null;
    String password = null;

    /*
     * Seccion de prueba
     */
    System.out.println("Datos utilizados para la autenticacion del usuario (inexistente)");
    System.out.println("Nombre de usuario (indefinido): " + username);
    System.out.println("Contraseña (indefinida): " + password);
    System.out.println();

    boolean authenticationResult = passwordService.authenticateUser(username, password);

    System.out.println("Resultado esperado: " + false);
    System.out.println("Resultado de la autenticacion del usuario: " + authenticationResult);
    System.out.println();

    assertFalse(authenticationResult);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @AfterClass
  public static void postTest() {
    entityManager.getTransaction().begin();

    /*
     * Elimina de la base de datos subyacente los datos persistidos durante la
     * ejecucion de la prueba unitaria para dejarla en su estado original,
     * es decir, para dejarla en el estado en el que estaba antes de que se
     * persistieran los datos creados
     */
    for (User currentUser : users) {
      userService.remove(currentUser.getId());
    }

    for (Password currentPassword : passwords) {
      passwordService.remove(currentPassword.getId());
    }

    entityManager.getTransaction().commit();

    // Cierra las conexiones
    entityManager.close();
    entityManagerFactory.close();
  }

  /**
   * Imprime la descripcion del metodo a probar
   */
  private void printMethodToTest(){
    System.out.println("# Descripcion del metodo a probar");
    System.out.println("El metodo authenticateUser() de la clase PasswordServiceBean retorna true si y solo si el nombre de");
    System.out.println("usuario y el valor hash de la contraseña provistos coinciden con los que estan almacenados en la base");
    System.out.println("de datos subyacente. En caso contrario, retorna false.");
    System.out.println();
  }

  /**
   * Crea y retorna un usuario con los datos provistos en los
   * parametros
   * 
   * @param username
   * @param name
   * @param lastName
   * @param email
   * @return referencia a un objeto de tipo User
   */
  private User setUserData(String username, String name, String lastName, String email) {
    User newUser = new User();
    newUser.setUsername(username);
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
    System.out.println("Datos de un usuario de prueba");
    System.out.println(givenUser);
  }

}
