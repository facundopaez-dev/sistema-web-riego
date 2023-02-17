import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import model.User;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import stateless.UserServiceBean;

public class UserServiceBeanTest {

  private static UserServiceBean userService;
  private static EntityManager entityManager;
  private static EntityManagerFactory entityManagerFactory;
  private static Collection<User> users;

  @BeforeClass
  public static void preTest() {
    entityManagerFactory = Persistence.createEntityManagerFactory("swcar");
    entityManager = entityManagerFactory.createEntityManager();
    userService = new UserServiceBean();
    userService.setEntityManager(entityManager);
    users = new ArrayList<>();
  }

  @Test
  public void testModify() {
    System.out.println("*********************************** Prueba del metodo modify ***********************************");
    System.out.println("- El metodo modify de la clase UserServiceBean modifica el nombre de usuario, el");
    System.out.println("nombre, el apellido y el correo electronico de un usuario en la base de datos");
    System.out.println("subyacente.");
    System.out.println();
    System.out.println("En este prueba se recuperara un usuario de la base de datos subyacente, se lo mostrara y luego");
    System.out.println("se modificaran su nombre de usuario, su nombre, su apellido y su correo electronico.");
    System.out.println("Por ultimo, se lo recuperara de la base de datos nuevamente para demostrar que sus datos");
    System.out.println("fueron modificados.");
    System.out.println();

    String givenUsername = "Otherusername";

    /*
     * Obtencion de un usuario para la prueba
     */
    User givenUser = userService.find(3);
    printUserData(givenUser);

    /*
     * Modificacion del usuario obtenido
     */
    givenUser.setUsername(givenUsername);
    givenUser.setName("Othername");
    givenUser.setLastName("Other last name");
    givenUser.setEmail("other@eservice.com");

    userService.modify(givenUser.getId(), givenUser);
    System.out.println("* Modificación realizada");
    System.out.println();

    /*
     * Seccion de prueba
     */
    givenUser = userService.find(givenUser.getId());
    printUserData(givenUser);

    /*
     * Si la modificacion de los datos del usuario obtenido de la
     * base de datos subyacente, fue satisfactoriamente realizada,
     * su nombre de usuario debe ser igual al nombre de usuario
     * con el que se lo modifico 
     */
    boolean result = givenUser.getUsername().equals(givenUsername);

    System.out.println("Resultado esperado de comprobar si el nombre de usuario fue modificado: " + true);
    System.out.println("* Valor obtenido: " + result);
    System.out.println();

    assertTrue(result);

    System.out.println("* Prueba ejecutada satisfactoriamente *");
  }

  @Test
  public void testFindAll() {
    System.out.println("******************************** Prueba del metodo findAll ********************************");
    System.out.println("- El metodo findAll de la clase UserServiceBean obtiene todos los usuarios excepto el");
    System.out.println("usuario que tiene el nombre de usuario admin. Esto es asi para que la aplicacion tenga");
    System.out.println("por defecto un usuario con el permiso de administrador al que no se pueda dar de baja ni");
    System.out.println("se le pueda quitar dicho permiso.");
    System.out.println();
    System.out.println("En esta prueba se demostrara que el conjunto de datos devuelto por el metodo findAll");
    System.out.println("contiene todos los usuarios registrados en la base de datos subyacente excepto el usuario");
    System.out.println("con el nombre de usuario admin.");
    System.out.println();

    /*
     * El usuario admin es el usuario con el ID = 1, siempre
     * y cuando no se lo persista luego de otro usuario
     */
    User givenUser = userService.find(1);
    printUserData(givenUser);

    /*
     * Seccion de prueba
     */
    Collection<User> users = userService.findAll();
    System.out.println("Cantidad de usuarios registrados en la base de datos subyacente - 1 = " + users.size());

    /*
     * El resultado del metodo contains debe ser false porque
     * el usuario admin no esta en el conjunto de datos devuelto
     * por el metodo findAll de la clase UserServiceBean
     */
    boolean result = users.contains(givenUser.getUsername());

    System.out.println("Resultado esperado de comprobar si admin esta en el conjunto de usuarios obtenido: " + false);
    System.out.println("* Valor obtenido: " + result);
    System.out.println();

    assertFalse(result);

    System.out.println("* Prueba ejecutada satisfactoriamente *");
  }

  @Test
  public void testAuthenticateOne() {
    System.out.println("***************************** Prueba uno del metodo authenticate() *****************************");
    System.out.println("- En este prueba se realiza la autenticacion de un usuario existente en la base de datos");
    System.out.println("subyacente, y el nombre de usuario y la contraseña utilizados para la autenticacion del mismo");
    System.out.println("son iguales a los que estan almacenados en la base de datos. El metodo authenticate de la clase");
    System.out.println("UserServiceBean retorna true si el nombre de usuario y la contraseña provistos coinciden con los");
    System.out.println("que estan almacenados en la base de datos subyacente, y false en caso contrario.");
    System.out.println();
    System.out.println("En este caso, el metodo authenticate() de la clase UserServiceBean retorna el valor booleano");
    System.out.println("true.");
    System.out.println();

    /*
     * Esta contraseña plana se usa para autentificar al
     * usuario de esta prueba
     */
    String plainPassword = "Ultra secret password";
    String username = "dominic";

    /*
     * Creacion de un usuario de prueba
     */
    User newUser = createUser(username, userService.getPasswordHash(plainPassword), "Dominic", "Doe", "dominic@eservice.com");
    printUserData(newUser);

    /*
     * Persistencia del usuario creado
     */
    entityManager.getTransaction().begin();
    newUser = userService.create(newUser);
    entityManager.getTransaction().commit();

    /*
     * Se agrega el usuario creado a una coleccion para su posterior
     * eliminacion de la base de datos subyacente, lo cual se hace
     * para que la misma tenga el estado que tenia antes de la ejecucion
     * de esta prueba unitaria
     */
    users.add(newUser);

    /*
     * Seccion de prueba
     */
    System.out.println("Datos utilizados para la autenticacion del usuario");
    System.out.println("Nombre de usuario (correcto): " + username);
    System.out.println("Contraseña en texto plano (correcta): " + plainPassword);
    System.out.println();

    boolean authenticatedUser = userService.authenticate(username, plainPassword);

    System.out.println("Valor esperado: " + true);
    System.out.println("* Resultado obtenido de la autenticacion del usuario: " + authenticatedUser);
    System.out.println();

    assertTrue(authenticatedUser);

    System.out.println("* Prueba pasada satisfactoriamente.");
    System.out.println();
  }

  @Test
  public void testAuthenticateTwo() {
    System.out.println("***************************** Prueba dos del metodo authenticate() *****************************");
    System.out.println("- En este prueba se realiza la autenticacion de un usuario existente en la base de datos");
    System.out.println("subyacente, y el nombre de usuario utilizado para la autenticacion del mismo coincide con el que");
    System.out.println("esta almacenado en la base de datos, pero la contraseña no. El metodo authenticate de la clase");
    System.out.println("UserServiceBean retorna true si el nombre de usuario y la contraseña provistos coinciden con los");
    System.out.println("que estan almacenados en la base de datos subyacente, y false en caso contrario.");
    System.out.println();
    System.out.println("En este caso, el metodo authenticate() de la clase UserServiceBean retorna el valor booleano");
    System.out.println("false.");
    System.out.println();

    /*
     * Esta contraseña plana se usa para autentificar al
     * usuario de esta prueba
     */
    String plainPassword = "Ultra secret password";
    String username = "matthew";

    /*
     * El hash de la contraseña correcta se usa para mostrar
     * la diferencia con el hash de la contraseña incorrecta
     */
    String correctPassword = "Awesome password";
    String hashCorrectPassword = userService.getPasswordHash(correctPassword);

    /*
     * Creacion de un usuario de prueba
     */
    User newUser = createUser(username, hashCorrectPassword, "Matthew", "Doe", "matthew@eservice.com");
    printUserData(newUser);

    /*
     * Persistencia del usuario creado
     */
    entityManager.getTransaction().begin();
    newUser = userService.create(newUser);
    entityManager.getTransaction().commit();

    /*
     * Se agrega el usuario creado a una coleccion para su posterior
     * eliminacion de la base de datos subyacente, lo cual se hace
     * para que la misma tenga el estado que tenia antes de la ejecucion
     * de esta prueba unitaria
     */
    users.add(newUser);

    /*
     * Seccion de prueba
     */
    System.out.println("Datos utilizados para la autenticacion del usuario");
    System.out.println("Nombre de usuario (correcto): " + username);
    System.out.println("Contraseña en texto plano (incorrecta): " + plainPassword);
    System.out.println();
    System.out.println("Hash de la contraseña incorrecta: " + userService.getPasswordHash(plainPassword));
    System.out.println("Hash de la contraseña correcta: " + hashCorrectPassword);
    System.out.println();

    boolean authenticatedUser = userService.authenticate(username, plainPassword);

    System.out.println("Valor esperado: " + false);
    System.out.println("* Resultado obtenido de la autenticacion del usuario: " + authenticatedUser);
    System.out.println();

    assertFalse(authenticatedUser);

    System.out.println("* Prueba pasada satisfactoriamente.");
    System.out.println();
  }

  @Test
  public void testAuthenticateThree() {
    System.out.println("***************************** Prueba tres del metodo authenticate() *****************************");
    System.out.println("- En este prueba se realiza la autenticacion de un usuario existente en la base de datos");
    System.out.println("subyacente, y la contraseña utilizada para la autenticacion del mismo coincide con la que esta");
    System.out.println("almacenada en la base de datos, pero el nombre de usuario no. El metodo authenticate de la clase");
    System.out.println("UserServiceBean retorna true si el nombre de usuario y la contraseña provistos coinciden con los");
    System.out.println("que estan almacenados en la base de datos subyacente, y false en caso contrario.");
    System.out.println();
    System.out.println("En este caso, el metodo authenticate() de la clase UserServiceBean retorna el valor booleano");
    System.out.println("false.");
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
    String plainPassword = "Ultra secret password";
    String incorrectUsername = username + "_91";

    /*
     * Creacion de un usuario de prueba
     */
    User newUser = createUser(username, userService.getPasswordHash(plainPassword), "Brian", "Doe", "brian@eservice.com");
    printUserData(newUser);

    /*
     * Persistencia del usuario creado
     */
    entityManager.getTransaction().begin();
    newUser = userService.create(newUser);
    entityManager.getTransaction().commit();

    /*
     * Se agrega el usuario creado a una coleccion para su posterior
     * eliminacion de la base de datos subyacente, lo cual se hace
     * para que la misma tenga el estado que tenia antes de la ejecucion
     * de esta prueba unitaria
     */
    users.add(newUser);

    /*
     * Seccion de prueba
     */
    System.out.println("Datos utilizados para la autenticacion del usuario");
    System.out.println("Nombre de usuario (incorrecto): " + incorrectUsername);
    System.out.println("Contraseña en texto plano (correcta): " + plainPassword);
    System.out.println();

    boolean authenticatedUser = userService.authenticate(incorrectUsername, plainPassword);

    System.out.println("Valor esperado: " + false);
    System.out.println("* Resultado obtenido de la autenticacion del usuario: " + authenticatedUser);
    System.out.println();

    assertFalse(authenticatedUser);

    System.out.println("* Prueba pasada satisfactoriamente.");
    System.out.println();
  }

  @Test
  public void testAuthenticateFour() {
    System.out.println("***************************** Prueba cuatro del metodo authenticate() *****************************");
    System.out.println("- En este prueba se realiza la autenticacion de un usuario inexistente en la base de datos");
    System.out.println("subyacente. El metodo authenticate de la clase UserServiceBean retorna true si el nombre de usuario");
    System.out.println("y la contraseña provistos coinciden con los que estan almacenados en la base de datos subyacente, y");
    System.out.println("false en caso contrario.");
    System.out.println();
    System.out.println("En este caso, el metodo authenticate() de la clase UserServiceBean retorna el valor booleano");
    System.out.println("false, ya que autenticar a un usuario inexistente es similar a autenticar a un usuario que provee");
    System.out.println("un nombre de usuario y/o una contraseña que no coinciden con los que estan almacenados en la base");
    System.out.println("de datos.");
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

    boolean authenticatedUser = userService.authenticate(username, password);

    System.out.println("Valor esperado: " + false);
    System.out.println("* Resultado obtenido de la autenticacion del usuario: " + authenticatedUser);
    System.out.println();

    assertFalse(authenticatedUser);

    System.out.println("* Prueba pasada satisfactoriamente.");
    System.out.println();
  }

  @Test
  public void testAuthenticateFive() {
    System.out.println("***************************** Prueba cinco del metodo authenticate() *****************************");
    System.out.println("- En esta prueba se invoca al metodo authenticate de la clase UserServieceBean con el valor null");
    System.out.println("como argumento en el nombre de usuario y la contraseña. Este metodo retorna true si el nombre de");
    System.out.println("usuario y la contraseña provistos coinciden con los que estan almacenados en la base de datos");
    System.out.println("subyacente, y false en caso contrario.");
    System.out.println();
    System.out.println("En este caso, el metodo authenticate retorna el valor booleano false porque invocarlo con el valor");
    System.out.println("null como argumento del nombre de usuario y/o la contraseña es como invocarlo con un usuario que NO");
    System.out.println("esta registrado en la base de datos subyacente.");
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
    System.out.println("Nombre de usuario (inexistente): " + username);
    System.out.println("Contraseña (inexistente): " + password);
    System.out.println();

    boolean authenticatedUser = userService.authenticate(username, password);

    System.out.println("Valor esperado: " + false);
    System.out.println("* Resultado obtenido de la autenticacion del usuario: " + authenticatedUser);
    System.out.println();

    assertFalse(authenticatedUser);

    System.out.println("* Prueba pasada satisfactoriamente.");
    System.out.println();
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
    System.out.println("Datos de un usuario de prueba");
    System.out.println(givenUser);
  }

}
