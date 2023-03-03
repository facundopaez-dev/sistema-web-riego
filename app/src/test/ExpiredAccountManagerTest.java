import static org.junit.Assert.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import java.util.Collection;
import java.lang.Math;
import model.AccountActivationLink;
import stateless.AccountActivationLinkServiceBean;
import stateless.UserServiceBean;

import accountsAdministration.ExpiredAccountManager;

public class ExpiredAccountManagerTest {

  private static EntityManager entityManager;
  private static EntityManagerFactory entityManagerFactory;
  private static AccountActivationLinkServiceBean accountActivationLinkService;
  private static UserServiceBean userService;
  private static Collection<AccountActivationLink> accountActivationLinks;

  @BeforeClass
  public static void preTest() {
    entityManagerFactory = Persistence.createEntityManagerFactory("swcar");
    entityManager = entityManagerFactory.createEntityManager();

    userService = new UserServiceBean();
    userService.setEntityManager(entityManager);

    accountActivationLinkService = new AccountActivationLinkServiceBean();
    accountActivationLinkService.setEntityManager(entityManager);
  }

  /*
   * NOTA: Para ejecutar esta prueba se debe ejecutar el comando
   * "ant t89" (sin las comillas), el cual, carga la base de datos
   * subyacente con datos de ejemplo, los cuales, son para la ejecucion
   * de esta prueba.
   * 
   * Para que esta carga de datos sea exitosa es necesario que en la
   * tabla IRRIGATION_SYSTEM_USER no haya ningun registro que tenga su
   * clave primaria como clave foranea en otra tabla de la base de datos,
   * ya que el comando mencionado en el parrafo atenrior elimina el contenido
   * de la tabla IRRIGATION_SYSTEM_USER.
   * 
   * El codigo fuente que hay dentro de la instruccion for es igual al
   * codigo fuente que hay dentro de la instruccion for del metodo
   * deleteExpiredAccounts con la unica diferencia de que este ultimo
   * no tiene instrucciones para imprimir por pantalla. Lo que se busca
   * con esta prueba unitaria es demostrar que el metodo deleteExpiredAccounts
   * funciona correctamente, es decir, que elimina los enlaces de
   * activacion de cuenta NO consumidos y expirados, y las cuentas
   * registradas asociadas a los mismos.
   */
  @Test
  public void testDeleteExpiredAccounts() {
    /*
     * Contiene todos los enlaces de activacion, tanto los
     * consumidos como los no consumidos
     */
    Collection<AccountActivationLink> accountActivationLinks = accountActivationLinkService.findAll();
    Collection<AccountActivationLink> accountActivationLinksNotConsumed = accountActivationLinkService.findAllNotConsumed();

    System.out.println("* Cantidad total de enlaces de activacion de cuenta: " + accountActivationLinks.size());
    System.out.println("* Cantidad de enlaces de activacion de cuenta NO consumidos: " + accountActivationLinksNotConsumed.size());
    System.out.println();

    entityManager.getTransaction().begin();

    for (AccountActivationLink currentAccountActivationLinkNotConsumed : accountActivationLinksNotConsumed) {
      /*
       * Si un enlace de activacion de cuenta NO consumido, expiro
       * (es decir, no fue accedido por su respectivo usuario antes
       * de su tiempo de expiracion), se lo elimina de la base de
       * datos subyacente, y tambien se elimina de la misma la cuenta
       * NO activada asociada a dicho enlace
       */
      if (accountActivationLinkService.checkExpiration(currentAccountActivationLinkNotConsumed)) {
        System.out.println("Enlace de activacion (ID = " + currentAccountActivationLinkNotConsumed.getId() + ") NO consumido y expirado, eliminado.");
        System.out.println("Correo electronico asociado a este enlace: " + currentAccountActivationLinkNotConsumed.getUser().getEmail() + ".");
        System.out.println("Cuenta (NO activada) con el correo electronico " + currentAccountActivationLinkNotConsumed.getUser().getEmail() + " eliminada.");
        System.out.println();

        accountActivationLinkService.remove(currentAccountActivationLinkNotConsumed.getId());
        userService.remove(currentAccountActivationLinkNotConsumed.getUser().getId());
      }

    }

    entityManager.getTransaction().commit();

    /*
     * Seccion de prueba
     */
    int amountConsumed = Math.abs(accountActivationLinks.size() - accountActivationLinksNotConsumed.size());
    int amountActivationLink = accountActivationLinkService.findAll().size();

    System.out.println("Cantidad esperada de enlaces de activacion luego de la eliminacion de enlaces de activacion NO consumidos y expirados: " + amountConsumed);
    System.out.println("* Cantidad de enlaces de activacion que hay en la base de datos: " + amountActivationLink);
    System.out.println();

    /*
     * Si la cantidad de enlaces de activacion de cuenta consumidos
     * es igual a la cantidad de enlaces de activacion de cuenta
     * que hay en la base de datos luego de la eliminacion de los
     * enlaces de activacion de cuenta NO consumidos y expirados,
     * el codigo fuente para la eliminacion de estos ultimos
     * funciona correctamente, y, por ende, esta prueba es pasada
     * satisfactoriamente
     */
    assertTrue(amountConsumed == amountActivationLink);

    System.out.println("* Prueba pasada satisfactoriamente *");
  }

  @AfterClass
  public static void postTest() {
    // Cierra las conexiones
    entityManager.close();
    entityManagerFactory.close();
  }

}
