import static org.junit.Assert.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import model.AccountActivationLink;
import stateless.AccountActivationLinkServiceBean;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Calendar;

public class AccountActivationTest {

  private static EntityManager entityManager;
  private static EntityManagerFactory entityManagerFactory;
  private static AccountActivationLinkServiceBean accountActivationLinkService;
  private static Collection<AccountActivationLink> accountActivationLinks;

  @BeforeClass
  public static void preTest() {
    entityManagerFactory = Persistence.createEntityManagerFactory("swcar");
    entityManager = entityManagerFactory.createEntityManager();

    accountActivationLinkService = new AccountActivationLinkServiceBean();
    accountActivationLinkService.setEntityManager(entityManager);

    accountActivationLinks = new ArrayList<>();
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
     * Creacion de enalces de activacion
     */
    String givenUserEmail = "testuserone@eservice.com";

    AccountActivationLink firstNewAccountActivationLink = new AccountActivationLink();
    firstNewAccountActivationLink.setDateIssue(firstDateIssue);
    firstNewAccountActivationLink.setExpirationDate(firstExpirationDate);
    firstNewAccountActivationLink.setUserEmail(givenUserEmail);

    AccountActivationLink secondNewAccountActivationLink = new AccountActivationLink();
    secondNewAccountActivationLink.setDateIssue(secondDateIssue);
    secondNewAccountActivationLink.setExpirationDate(secondExpirationDate);
    secondNewAccountActivationLink.setUserEmail(givenUserEmail);

    AccountActivationLink threeNewAccountActivationLink = new AccountActivationLink();
    threeNewAccountActivationLink.setDateIssue(threeDateIssue);
    threeNewAccountActivationLink.setExpirationDate(threeExpirationDate);
    threeNewAccountActivationLink.setUserEmail(givenUserEmail);

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
     * Se agregan los enlaces de activacion para su posterior
     * eliminacion de la base de datos subyacente
     */
    accountActivationLinks.add(firstNewAccountActivationLink);
    accountActivationLinks.add(secondNewAccountActivationLink);
    accountActivationLinks.add(threeNewAccountActivationLink);

    /*
     * Se imprimen por pantalla los enlaces de activacion de cuenta
     * creados y persistidos para visualizar por el tiempo UNIX de
     * cada uno de ellos cual es el mas reciente. Esto se hace para
     * demostrar que el metodo findByUserEmail de la clase
     * AccountActivationLinkServiceBean retorna el enlace de activacion
     * de cuenta mas reciente de un usuario registrado.
     */
    System.out.println("* Impresion de los enlaces de activacion de la cuenta del usuario que tiene el correo");
    System.out.println("electronico " + givenUserEmail);
    System.out.println();

    for (AccountActivationLink currentAccountActivationLink : accountActivationLinks) {
      System.out.println(currentAccountActivationLink);
    }

    /*
     * Seccion de prueba
     */
    AccountActivationLink latestAccountActivationLink = accountActivationLinkService.findByUserEmail(givenUserEmail);

    System.out.println("Valor esperado (ID del enlace de activacion mas reciente): " + threeNewAccountActivationLink.getId());
    System.out.println("* Resultado obtenido (ID del enlace de activacion mas reciente): " + latestAccountActivationLink.getId());
    System.out.println();

    assertEquals(threeNewAccountActivationLink.getId(), latestAccountActivationLink.getId());

    System.out.println("* Prueba pasada satisfactoriamente *");
  }

  @Test
  public void testOneCheckConsumed() {
    System.out.println("*************************** Prueba del metodo checkConsumed ***************************");
    System.out.println("- El metodo checkConsumed de la clase AccountActivationLink retorna true si el enlace");
    System.out.println("de activacion de la cuenta de un usuario fue consumido, y false en caso contrario.");
    System.out.println();
    System.out.println("En esta prueba se crea y persiste un enlace de activacion con su variable consumed en");
    System.out.println("false. Por lo tanto, el metodo checkConsumed debe retornar el valor booleano false.");
    System.out.println();

    /*
     * Creacion y persistencia de un enlace de activacion
     */
    String givenUserEmail = "testusertwo@eservice.com";
    AccountActivationLink newAccountActivationLink;

    entityManager.getTransaction().begin();
    newAccountActivationLink = accountActivationLinkService.create(givenUserEmail);
    entityManager.getTransaction().commit();

    /*
     * Se agrega el enlace de activacion a una coleccion para
     * su posterior eliminacion de la base de datos subyacente
     */
    accountActivationLinks.add(newAccountActivationLink);

    /*
     * Seccion de prueba
     */
    boolean result = accountActivationLinkService.checkConsumed(newAccountActivationLink.getUserEmail());

    System.out.println("Valor esperado: " + false);
    System.out.println("* Resultado obtenido: " + result);
    System.out.println();

    assertFalse(result);

    System.out.println("* Prueba pasada satisfactoriamente *");
  }

  @AfterClass
  public static void postTest() {
    entityManager.getTransaction().begin();

    /*
     * Elimina de la base de datos subyacente los enlaces de activation
     * de cuenta persistidos durante la ejecucion de las pruebas unitarias
     * para dejarla en su estado original, es decir, para dejarla en el
     * estado en el que estaba antes de que se persistieran los enlaces
     * de activacion de cuenta creados
     */
    for (AccountActivationLink currentAccontActivationLink : accountActivationLinks) {
      accountActivationLinkService.remove(currentAccontActivationLink.getId());
    }

    entityManager.getTransaction().commit();

    // Cierra las conexiones
    entityManager.close();
    entityManagerFactory.close();
  }

}
