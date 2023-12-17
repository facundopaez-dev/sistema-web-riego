import static org.junit.Assert.*;

import java.util.Collection;
import java.util.ArrayList;
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

  private static EntityManager entityManager;
  private static EntityManagerFactory entityManagerFactory;
  private static UserServiceBean userService;
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
    System.out.println("- El metodo modify de la clase UserServiceBean modifica el nombre de usuario, el nombre, el");
    System.out.println("apellido y el correo electronico de un usuario en la base de datos subyacente.");
    System.out.println();

    /*
     * Creacion y persistencia de un usuario de prueba
     */
    User newUser = new User();
    newUser.setUsername("NewUser99");
    newUser.setName("Max");
    newUser.setLastName("Meridio");
    newUser.setEmail("max@eservice.com");

    entityManager.getTransaction().begin();
    newUser = userService.create(newUser);
    entityManager.getTransaction().commit();

    /*
     * Se añade el usuario persistido a una coleccion
     * para su posterior eliminacion de la base de
     * datos subyacente con el fin de dejar a la
     * misma en su estado original
     */
    users.add(newUser);

    /*
     * Impresion del usuario de prueba
     */
    System.out.println("Datos del usuario de prueba:");
    printUserData(newUser);

    /*
     * Modificacion del usuario de prueba
     */
    String givenUsername = "Otherusername";
    String givenName = "Othername";
    String givenLastName = "Other last name";
    String givenEmail = "other@eservice.com";

    User modifiedUser = new User();
    modifiedUser.setUsername(givenUsername);
    modifiedUser.setName(givenName);
    modifiedUser.setLastName(givenLastName);
    modifiedUser.setEmail(givenEmail);

    userService.modify(newUser.getId(), modifiedUser);

    System.out.println("Resultado de la modificación:");

    /*
     * Seccion de prueba
     */
    modifiedUser = userService.find(newUser.getId());
    printUserData(modifiedUser);

    /*
     * Si la modificacion de los datos del usuario obtenido de la
     * base de datos subyacente, fue satisfactoriamente realizada,
     * el resultado de estas operaciones debe ser true
     */
    boolean result = modifiedUser.getUsername().equals(givenUsername) && modifiedUser.getName().equals(givenName)
        && modifiedUser.getLastName().equals(givenLastName) && modifiedUser.getEmail().equals(givenEmail);

    System.out.println("Valor esperado de comprobar si el usuario fue modificado: " + true);
    System.out.println("* Valor obtenido: " + result);
    System.out.println();

    assertTrue(result);

    System.out.println("- Prueba pasada satisfactoriamente");
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
   * Imprime los datos de un usuario
   * 
   * @param givenUser
   */
  private void printUserData(User givenUser) {
    System.out.println(givenUser);
  }

}
