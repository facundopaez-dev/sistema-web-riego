package stateless;

import java.util.Calendar;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import model.PasswordResetLink;
import model.User;
import util.UtilDate;

@Stateless
public class PasswordResetLinkServiceBean {

  /*
   * Instance variables
   */
  @PersistenceContext(unitName = "swcar")
  private EntityManager entityManager;

  public void setEntityManager(EntityManager localEntityManager) {
    entityManager = localEntityManager;
  }

  public EntityManager getEntityManager() {
    return entityManager;
  }

  /**
   * @param user
   * @return referencia a un objeto de tipo PasswordResetLink
   */
  public PasswordResetLink create(User user) {
    PasswordResetLink newPasswordResetLink = getInstance(user);
    getEntityManager().persist(newPasswordResetLink);
    return newPasswordResetLink;
  }

  /**
   * La fecha de expiracion de un enlace de restablecimiento de contraseña
   * es de 60 minutos a partir de la hora en la que se lo emite, la cual,
   * es la hora en la que un usuario solicita el restablecimiento de la
   * contraseña de su cuenta. La manera en la que se hace esto es mediante
   * el valor 3600000, el cual, es 60 minutos en milisegundos.
   * 
   * @param user
   * @return referencia a un objeto de tipo PasswordResetLink que
   * representa un enlace de restablecimimiento de la contraseña de la
   * cuenta de un usuario, el cual, tiene un tiempo de expiracion de
   * 60 minutos desde la fecha y la hora desde las que se lo emitio,
   * las cuales, son la fecha y la hora en las que un usuario solicita
   * el restablecimiento de la contraseña de su cuenta
   */
  private PasswordResetLink getInstance(User user) {
    /*
     * El objeto de tipo Calendar referenciado por la referencia
     * contenida en la variable de tipo por referencia dateIssue
     * de tipo Calendar, contiene la fecha y la hora en las que
     * se crea un enlace de restablecimiento de contraseña
     */
    Calendar dateIssue = UtilDate.getCurrentDate();

    /*
     * La fecha y la hora de expiracion de un enlace de restablecimiento
     * de contraseña se establece en 60 minutos (3600000 en milisegundos)
     * a partir de la fecha y la hora en la que se emite (crea) dicho enlace
     */
    Calendar expirationDate = Calendar.getInstance();
    expirationDate.setTimeInMillis(dateIssue.getTimeInMillis() + 3600000);

    PasswordResetLink newPasswordResetLink = new PasswordResetLink();
    newPasswordResetLink.setDateIssue(dateIssue);
    newPasswordResetLink.setExpirationDate(expirationDate);
    newPasswordResetLink.setUser(user);

    return newPasswordResetLink;
  }

  /**
   * Establece el atributo consumed del enlace de restablecimiento
   * correspondiente al ID dado, en uno.
   * 
   * Un enlace de restablecimiento de contraseña es consumido cuando el
   * usuario que solicito dicho restablecimiento, accede a dicho enlace
   * y restablece su contraseña.
   * 
   * @param id
   */
  public void setConsumed(int id) {
    Query query = getEntityManager().createQuery("UPDATE PasswordResetLink p SET p.consumed = 1 WHERE p.id = :givenId");
    query.setParameter("givenId", id);
    query.executeUpdate();
  }

  /**
   * @param passwordResetLinkId
   * @param email
   * @return referencia a un objeto de tipo PasswordResetLink en
   * caso de encontrarse en la base de datos subyacente, el enlace
   * de restablecimiento de contraseña correspondiente al ID y al
   * correo electronico dados, en caso contrario null
   */
  public PasswordResetLink find(int passwordResetLinkId, String email) {
    Query query = getEntityManager().createQuery("SELECT p FROM PasswordResetLink p WHERE (p.id = :passwordResetLinkId AND p.user.id = (SELECT x.id FROM Email e JOIN e.user x WHERE e.address = :email))");
    query.setParameter("passwordResetLinkId", passwordResetLinkId);
    query.setParameter("email", email);

    PasswordResetLink givenPasswordResetLink = null;

    try {
      givenPasswordResetLink = (PasswordResetLink) query.getSingleResult();
    } catch (NoResultException e) {
      e.printStackTrace();
    }

    return givenPasswordResetLink;
  }

  /**
   * Retorna true si y solo si existe en la base de datos subyacente un
   * enlace de restablecimiento de contraseña con el ID dado y asociado
   * al correo electronico dado
   * 
   * @param passwordResetLinkId
   * @param userEmail
   * @return true si existe en la base de datos subyacente un enlace de
   * restablecimiento de contraseña con el ID dado y asociado al correo
   * electronico dado, en caso contrario false. Tambien retorna false en
   * el caso en el que el correo electronico tiene el valor nulo.
   */
  public boolean checkExistence(int passwordResetLinkId, String userEmail) {
    /*
     * Si la direccion de correo electronico de usuario tiene el
     * valor null, se retorna false, ya que realizar la operacion
     * de este metodo con un correo electronico que tiene el valor
     * null es similar a realizar dicha operacion con un inexistente
     * enlace de restablecimiento de contraseña.
     * 
     * Con este control se evita realizar una consulta a la base de
     * datos comparando la direccion de correo electronico con el
     * valor null. Si no se realiza este control y se realiza esta
     * consulta a la base de datos, ocurre la excepcion
     * SQLSyntaxErrorException, debido a que la comparacion de
     * un atributo con el valor null incumple la sintaxis del
     * proveedor del motor de base de datos.
     */
    if (userEmail == null) {
      return false;
    }

    return (find(passwordResetLinkId, userEmail) != null);
  }

  /**
   * Retorna true si y solo si el enlace de restablecimiento de contraseña
   * correspondiente al ID dado y asociado al correo electronico dado,
   * expiro.
   * 
   * Un enlace de restablecimiento de contraseña expira cuando su fecha
   * de expiracion esta antes de la fecha actual.
   * 
   * Hay que tener en cuenta que este metodo debe ser invocado luego de
   * invocar al metodo checkExistence(int passwordResetLinkId, String userEmail)
   * de esta clase. Esto se debe a que se puede consultar si un enlace
   * de restablecimiento de contraseña expiro o no mediante un correo
   * electronico con el valor null. En este caso, si se hace esta
   * consulta sin invocar primero al metodo checkExistence mencionado,
   * ocurrira la excepcion SQLSyntaxErrorException, ya que la comparacion
   * de un atributo con el valor null incumple la sintanxis del proveedor
   * del motor de base de datos.
   * 
   * @param passwordResetLinkId
   * @param userEmail
   * @return true si el enlace de restablecimiento de contraseña correspondiente
   * al ID dado y asociado al correo electronico dado, expiro, en caso
   * contrario false
   */
  public boolean checkExpiration(int passwordResetLinkId, String userEmail) {
    PasswordResetLink givenPasswordResetLink = find(passwordResetLinkId, userEmail);

    /*
     * Si la fecha de expiracion de un enlace de restablecimiento de contraseña
     * es estrictamente menor (esta antes) que la fecha actual, dicho enlace
     * expiro, por lo tanto, se retorna true.
     * 
     * La documentacion del metodo before de la clase Calendar dice que
     * este metodo es equivalente a: compareTo(when) < 0.
     */
    if (givenPasswordResetLink.getExpirationDate().before(UtilDate.getCurrentDate())) {
      return true;
    }

    return false;
  }

  /**
   * Retorna true si y solo si el enlace de restablecimiento de contraseña
   * correspondiente al ID dado y asociado al correo electronico dado,
   * fue consumido.
   * 
   * Un enlace de restablecimiento de contraseña es consumido cuando el
   * usuario que solicito dicho restablecimiento, accede a dicho enlace
   * y restablece su contraseña.
   * 
   * Hay que tener en cuenta que este metodo debe ser invocado luego de
   * invocar al metodo checkExistence(int passwordResetLinkId, String userEmail)
   * de esta clase. Esto se debe a que se puede consultar si un enlace
   * de restablecimiento de contraseña expiro o no mediante un correo
   * electronico con el valor null. En este caso, si se hace esta
   * consulta sin invocar primero al metodo checkExistence mencionado,
   * ocurrira la excepcion SQLSyntaxErrorException, ya que la comparacion
   * de un atributo con el valor null incumple la sintanxis del proveedor
   * del motor de base de datos.
   * 
   * @param passwordResetLinkId
   * @param userEmail
   * @return true si el enlace de restablecimiento de contraseña correspondiente
   * al ID dado y asociado al correo electronico dado, fue consumido, en
   * caso contrario false
   */
  public boolean checkConsumed(int passwordResetLinkId, String userEmail) {
    PasswordResetLink givenPasswordResetLink = find(passwordResetLinkId, userEmail);
    return givenPasswordResetLink.getConsumed();
  }

}
