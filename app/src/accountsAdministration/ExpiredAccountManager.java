package accountsAdministration;

import java.util.Collection;
import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Stateless;
import stateless.UserServiceBean;
import stateless.AccountActivationLinkServiceBean;
import stateless.PastDaysReferenceServiceBean;
import model.AccountActivationLink;

/*
 * ExpiredAccountManager es la clase que se utiliza para eliminar
 * los enlaces de activacion expirados y NO consumidos, y las cuentas
 * registradas asociadas a los mismos.
 */
@Stateless
public class ExpiredAccountManager {

    // inject a reference to the UserServiceBean
    @EJB
    UserServiceBean userService;

    // inject a reference to the AccountActivationLinkServiceBean
    @EJB
    AccountActivationLinkServiceBean accountActivationLinkService;

    @EJB
    PastDaysReferenceServiceBean pastDaysReferenceService;

    /**
     * Elimina de manera automatica los enlaces de activacion de cuenta NO
     * consumidos y expirados, y las cuentas registradas asociadas a los mismos
     * cada 24 horas a partir de las 00 horas. Tambien elimina el PastDaysReference
     * asociado a una cuenta NO activada.
     * 
     * Si no se hace esto, un usuario que se registro, pero que no activo su
     * cuenta antes del tiempo de expiracion de su respectivo enlace de
     * activacion, no podra registrarse la proxima vez que lo intente, ya que
     * el nombre de usuario y el correo electronico son unicos en la aplicacion.
     * Esto se lo puede ver en la clase User.
     * 
     * La segunda anotacion @Schedule es para probar que este metodo se
     * ejecuta correctamente, es decir, que elimina los enlaces de activacion
     * de cuenta NO consumidos y expirados, y las cuentas registradas asociadas
     * a los mismos.
     */
    @Schedule(second = "*", minute = "*", hour = "0/23", persistent = false)
    // @Schedule(second = "*/10", minute = "*", hour = "*", persistent = false)
    private void deleteExpiredAccounts() {
        /*
         * Obtiene de la base de datos subyacente todos los enlaces de
         * activacion NO consumidos, es decir, todos aquellos enlaces de
         * activacion que no fueron accedidos por sus respectivos usuarios
         * para la activacion de sus cuentas
         */
        Collection<AccountActivationLink> accountActivationLinksNotConsumed = accountActivationLinkService.findAllNotConsumed();

        for (AccountActivationLink currentAccountActivationLinkNotConsumed : accountActivationLinksNotConsumed) {
            /*
             * Si un enlace de activacion de cuenta NO consumido, expiro
             * (es decir, no fue accedido por su respectivo usuario antes
             * de su tiempo de expiracion), se lo elimina de la base de
             * datos subyacente junto con la cuenta NO activada a la que
             * esta asociado. Tambien se elimina el PastDaysReference
             * asociado a la cuenta NO activada.
             */
            if (accountActivationLinkService.checkExpiration(currentAccountActivationLinkNotConsumed)) {
                accountActivationLinkService.remove(currentAccountActivationLinkNotConsumed.getId());
                pastDaysReferenceService.removeByUserId(currentAccountActivationLinkNotConsumed.getUser().getId());
                userService.remove(currentAccountActivationLinkNotConsumed.getUser().getId());
            }

        }

    }

}