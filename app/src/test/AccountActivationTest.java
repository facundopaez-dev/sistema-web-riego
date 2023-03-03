import static org.junit.Assert.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import model.AccountActivationLink;
import model.User;
import stateless.AccountActivationLinkServiceBean;
import stateless.UserServiceBean;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Calendar;

public class AccountActivationTest {

  private static EntityManager entityManager;
  private static EntityManagerFactory entityManagerFactory;
  private static AccountActivationLinkServiceBean accountActivationLinkService;
  private static UserServiceBean userService;
  private static Collection<AccountActivationLink> accountActivationLinks;
  private static Collection<User> users;

  @BeforeClass
  public static void preTest() {
    entityManagerFactory = Persistence.createEntityManagerFactory("swcar");
    entityManager = entityManagerFactory.createEntityManager();

    accountActivationLinkService = new AccountActivationLinkServiceBean();
    accountActivationLinkService.setEntityManager(entityManager);

    userService = new UserServiceBean();
    userService.setEntityManager(entityManager);

    accountActivationLinks = new ArrayList<>();
    users = new ArrayList<>();
  }

  @Test
  public void testFindByUserEmail() {
    System.out.println("*********************************** Prueba del metodo findByUserEmail ***********************************");
    System.out.println("- El metodo findByUserEmail de la clase AccountActivationLinkServiceBean retorna el enlace de");
    System.out.println("activacion de cuenta mas reciente de un usuario registrado.");
    System.out.println();
    System.out.println("En esta prueba se crean y persisten consecutivamente tres enlaces de activacion de cuenta con el mismo");
    System.out.println("correo electronico. La fecha de expiracion del primero tiene una distancia temporal de 60 minutos con su");
    System.out.println("fecha de emision.");
    System.out.println();
    System.out.println("La fecha de emision del segundo esta 2 horas despues de la fecha actual y su fecha de expiracion esta 2");
    System.out.println("horas despues de su fecha de emision.");
    System.out.println();
    System.out.println("La fecha de emision del tercero esta 3 horas despues de la fecha actual y su fecha de expiracion esta 3");
    System.out.println("horas despues de su fecha de emision.");
    System.out.println();
    System.out.println("Por lo tanto, el metodo findByUserEmail debe retornar el tercer enlace de activacion de cuenta.");
    System.out.println();

    /*
     * Creacion de la fecha de emision y de la fecha de expiracion del
     * primer enlace de activacion de cuenta. La fecha de expiracion
     * esta 60 minutos despues de la fecha de emision.
     */
    Calendar firstDateIssue = Calendar.getInstance();
    Calendar firstExpirationDate = Calendar.getInstance();
    firstExpirationDate.setTimeInMillis(firstDateIssue.getTimeInMillis() + 3600000);

    /*
     * Creacion de la fecha de emision y la fecha de expiracion del
     * segundo enlace de activacion de cuenta. La fecha de emision
     * esta dos horas despues de la fecha actual y la fecha de expiracion
     * esta dos horas despues de la fecha de emision.
     */
    Calendar secondDateIssue = Calendar.getInstance();
    secondDateIssue.setTimeInMillis(secondDateIssue.getTimeInMillis() + 7200000);

    Calendar secondExpirationDate = Calendar.getInstance();
    secondExpirationDate.setTimeInMillis(secondDateIssue.getTimeInMillis() + 7200000);

    /*
     * Creacion de la fecha de emision y la fecha de expiracion del
     * tercer enlace de activacion de cuenta. La fecha de emision esta
     * tres horas despues de la fecha actual y la fecha de expiracion
     * esta tres horas despues de la fecha de emision.
     */
    Calendar threeDateIssue = Calendar.getInstance();
    threeDateIssue.setTimeInMillis(threeDateIssue.getTimeInMillis() + 10800000);

    Calendar threeExpirationDate = Calendar.getInstance();
    threeExpirationDate.setTimeInMillis(threeDateIssue.getTimeInMillis() + 10800000);

    /*
     * Creacion y persistencia de un usuario para los
     * enlaces de activacion
     */
    User givenUser = new User();
    givenUser.setUsername("testuserone");
    givenUser.setName("Jackson");
    givenUser.setLastName("Doe");
    givenUser.setPassword("Ultra secret password");
    givenUser.setEmail("testuserone@eservice.com");

    givenUser = userService.create(givenUser);

    /*
     * Creacion de enlaces de activacion
     */
    AccountActivationLink firstNewAccountActivationLink = new AccountActivationLink();
    firstNewAccountActivationLink.setDateIssue(firstDateIssue);
    firstNewAccountActivationLink.setExpirationDate(firstExpirationDate);
    firstNewAccountActivationLink.setUser(givenUser);

    AccountActivationLink secondNewAccountActivationLink = new AccountActivationLink();
    secondNewAccountActivationLink.setDateIssue(secondDateIssue);
    secondNewAccountActivationLink.setExpirationDate(secondExpirationDate);
    secondNewAccountActivationLink.setUser(givenUser);

    AccountActivationLink threeNewAccountActivationLink = new AccountActivationLink();
    threeNewAccountActivationLink.setDateIssue(threeDateIssue);
    threeNewAccountActivationLink.setExpirationDate(threeExpirationDate);
    threeNewAccountActivationLink.setUser(givenUser);

    /*
     * Persistencia de enlaces de activacion
     */
    entityManager.getTransaction().begin();
    firstNewAccountActivationLink = accountActivationLinkService.create(firstNewAccountActivationLink);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    secondNewAccountActivationLink = accountActivationLinkService.create(secondNewAccountActivationLink);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    threeNewAccountActivationLink = accountActivationLinkService.create(threeNewAccountActivationLink);
    entityManager.getTransaction().commit();

    /*
     * Los datos creados se agregan a una coleccion para su
     * posterior eliminacion de la base de datos subyacente
     */
    accountActivationLinks.add(firstNewAccountActivationLink);
    accountActivationLinks.add(secondNewAccountActivationLink);
    accountActivationLinks.add(threeNewAccountActivationLink);
    users.add(givenUser);

    /*
     * Se imprimen por pantalla los enlaces de activacion de cuenta
     * creados y persistidos para visualizar por el tiempo UNIX de
     * cada uno de ellos cual es el mas reciente. Esto se hace para
     * demostrar que el metodo findByUserEmail de la clase
     * AccountActivationLinkServiceBean retorna el enlace de activacion
     * de cuenta mas reciente de un usuario registrado.
     */
    System.out.println("* Impresion de los enlaces de activacion de la cuenta del usuario que tiene el correo");
    System.out.println("electronico " + givenUser.getEmail());
    System.out.println();

    for (AccountActivationLink currentAccountActivationLink : accountActivationLinks) {
      System.out.println(currentAccountActivationLink);
    }

    /*
     * Seccion de prueba
     */
    AccountActivationLink latestAccountActivationLink = accountActivationLinkService.findByUserEmail(givenUser.getEmail());

    System.out.println("Valor esperado (ID del enlace de activacion mas reciente): " + threeNewAccountActivationLink.getId());
    System.out.println("* Resultado obtenido (ID del enlace de activacion mas reciente): " + latestAccountActivationLink.getId());
    System.out.println();

    /*
     * Si el ID del enlace de activacion de cuenta mas reciente
     * obtenido de la base de datos subyacente, es igual al ID
     * del tercer enlace de activacion de cuenta creado y persistido,
     * significa que el metodo findByUserEmail de la clase
     * AccountActivationLinkServiceBean retorna el enlace de activacion
     * de cuenta mas reciente de un usuario, y, por ende, la prueba es
     * pasada satisfactoriamente
     */
    assertTrue(latestAccountActivationLink.getId() == threeNewAccountActivationLink.getId());

    System.out.println("* Prueba pasada satisfactoriamente *");
  }

  @Test
  public void testOneCheckConsumed() {
    System.out.println("*************************** Prueba uno del metodo checkConsumed ***************************");
    System.out.println("- El metodo checkConsumed de la clase AccountActivationLink retorna true si el enlace");
    System.out.println("de activacion de la cuenta de un usuario fue consumido, y false en caso contrario.");
    System.out.println();
    System.out.println("En esta prueba se crea y persiste un enlace de activacion con su variable consumed en");
    System.out.println("false. Por lo tanto, el metodo checkConsumed debe retornar el valor booleano false.");
    System.out.println();

    /*
     * Creacion y persistencia de un usuario para un
     * enlace de activacion
     */
    User givenUser = new User();
    givenUser.setUsername("testusertwo");
    givenUser.setName("Jackie");
    givenUser.setLastName("Doe");
    givenUser.setPassword("Ultra secret password");
    givenUser.setEmail("testusertwo@eservice.com");

    givenUser = userService.create(givenUser);

    /*
     * Creacion y persistencia de un enlace de activacion
     */
    entityManager.getTransaction().begin();
    AccountActivationLink newAccountActivationLink = accountActivationLinkService.create(givenUser);
    entityManager.getTransaction().commit();

    /*
     * Los datos creados se agregan a una coleccion para su
     * posterior eliminacion de la base de datos subyacente
     */
    accountActivationLinks.add(newAccountActivationLink);
    users.add(givenUser);

    System.out.println("* Impresion de un enlace de activacion de cuenta");
    System.out.println(newAccountActivationLink);

    /*
     * Seccion de prueba
     */
    boolean result = accountActivationLinkService.checkConsumed(newAccountActivationLink.getUser().getEmail());

    System.out.println("Valor esperado: " + false);
    System.out.println("* Resultado obtenido: " + result);
    System.out.println();

    assertFalse(result);

    System.out.println("* Prueba pasada satisfactoriamente *");
  }

  @Test
  public void testTwoCheckConsumed() {
    System.out.println("*************************** Prueba dos del metodo checkConsumed ***************************");
    System.out.println("- El metodo checkConsumed de la clase AccountActivationLink retorna true si el enlace");
    System.out.println("de activacion de la cuenta de un usuario fue consumido, y false en caso contrario.");
    System.out.println();
    System.out.println("En esta prueba se crea y persiste un enlace de activacion con su variable consumed en");
    System.out.println("true. Por lo tanto, el metodo checkConsumed debe retornar el valor booleano true.");
    System.out.println();

    /*
     * Creacion y persistencia de un usuario para un
     * enlace de activacion
     */
    User givenUser = new User();
    givenUser.setUsername("testuserthree");
    givenUser.setName("Joe");
    givenUser.setLastName("Doe");
    givenUser.setPassword("Ultra secret password");
    givenUser.setEmail("testuserthree@eservice.com");

    givenUser = userService.create(givenUser);

    /*
     * Creacion de la fecha de emision y de la fecha de expiracion para
     * un enlace de activacion de cuenta. La fecha de expiracion esta
     * 60 minutos despues de la fecha de emision.
     */
    Calendar dateIssue = Calendar.getInstance();
    Calendar expirationDate = Calendar.getInstance();
    expirationDate.setTimeInMillis(dateIssue.getTimeInMillis() + 3600000);

    /*
     * Creacion y persistencia de un enlace de activacion
     */
    AccountActivationLink newAccountActivationLink = new AccountActivationLink();
    newAccountActivationLink.setDateIssue(dateIssue);
    newAccountActivationLink.setExpirationDate(expirationDate);
    newAccountActivationLink.setUser(givenUser);
    newAccountActivationLink.setConsumed(true);

    entityManager.getTransaction().begin();
    newAccountActivationLink = accountActivationLinkService.create(newAccountActivationLink);
    entityManager.getTransaction().commit();

    /*
     * Los datos creados se agregan a una coleccion para su
     * posterior eliminacion de la base de datos subyacente
     */
    accountActivationLinks.add(newAccountActivationLink);
    users.add(givenUser);

    System.out.println("* Impresion de un enlace de activacion de cuenta");
    System.out.println(newAccountActivationLink);

    /*
     * Seccion de prueba
     */
    boolean result = accountActivationLinkService.checkConsumed(newAccountActivationLink.getUser().getEmail());

    System.out.println("Valor esperado: " + true);
    System.out.println("* Resultado obtenido: " + result);
    System.out.println();

    assertTrue(result);

    System.out.println("* Prueba pasada satisfactoriamente *");
  }

  @AfterClass
  public static void postTest() {
    entityManager.getTransaction().begin();

    /*
     * Se eliminan de la base de datos subyacente los datos persistidos
     * durante la ejecucion de las pruebas unitarias para que la misma
     * quede en su estado original, es decir, para dejarla en el estado
     * en el que estaba antes de que se persistieran dichos datos
     */
    for (User currentUser : users) {
      userService.remove(currentUser.getId());
    }

    for (AccountActivationLink currentAccontActivationLink : accountActivationLinks) {
      accountActivationLinkService.remove(currentAccontActivationLink.getId());
    }

    entityManager.getTransaction().commit();

    // Cierra las conexiones
    entityManager.close();
    entityManagerFactory.close();
  }

}
