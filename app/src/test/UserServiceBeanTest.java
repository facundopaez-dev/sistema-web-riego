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
  public void testDelete() {
    System.out.println("********************************** Prueba del metodo delete **********************************");
    System.out.println("- El metodo delete de la clase UserServiceBean elimina logicamente un usuario de la base");
    System.out.println("de datos subyacente. Esto significa que establece el atributo active de un usuario en false.");
    System.out.println();
    System.out.println("En esta prueba se persistira un usuario activo (tiene su atributo active en true) en la base");
    System.out.println("de datos subyacente, el cual, luego se eliminara logicamente de la misma y se recuperara,");
    System.out.println("momento en el cual su atributo active debera tener el valor false.");
    System.out.println();

    /*
     * Creacion y persistencia de un usuario
     */
    User givenUser = new User();
    givenUser.setUsername("given");
    givenUser.setPassword("Super secret password");
    givenUser.setName("Given");
    givenUser.setLastName("Us");
    givenUser.setEmail("given@eservice.com");
    givenUser.setActive(true);

    entityManager.getTransaction().begin();
    givenUser = userService.create(givenUser);
    entityManager.getTransaction().commit();

    printUserData(givenUser);

    /*
     * Se agrega el usuario creado a una coleccion para su posterior
     * eliminacion de la base de datos subyacente, lo cual se hace
     * para que la misma tenga el estado que tenia antes de la ejecucion
     * de esta prueba unitaria
     */
    users.add(givenUser);

    /*
     * Seccion de prueba
     */
    userService.delete(givenUser.getId());

    /*
     * El valor devuelto por el metodo getActive de la clase User, invocado
     * en este usuario debe ser false porque a dicho usuario se lo elimino
     * logicamente mediante la instruccion anterior
     */
    boolean activeUser = userService.find(givenUser.getId()).getActive();

    System.out.println("Resultado esperado: " + false);
    System.out.println("* Valor obtenido: " + activeUser);
    System.out.println();

    assertFalse(activeUser);

    System.out.println("* Prueba ejecutada satisfactoriamente *");
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
    System.out.println("Datos de un usuario");
    System.out.println(givenUser);
  }

}
