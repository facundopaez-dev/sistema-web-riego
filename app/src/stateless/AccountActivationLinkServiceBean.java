package stateless;

import java.util.Calendar;
import java.util.Collection;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import model.AccountActivationLink;
import model.User;

@Stateless
public class AccountActivationLinkServiceBean {

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
   * Este metodo es unicamente para la prueba unitaria del metodo
   * findByUserEmail de esta clase. Si se lo utiliza para crear
   * un enlace de activacion que no tiene sus atributos establecidos,
   * habra excepciones porque el modelo de datos AccountActivationLink
   * no permite valores nulos en sus atributos.
   * 
   * La prueba unitaria del metodo findByUserEmail de esta clase se
   * encuentra en el archivo AccountActivationTest.
   * 
   * @param newAccounActivationLink
   * @return referencia a un objeto de tipo AccountActivationLink
   */
  public AccountActivationLink create(AccountActivationLink newAccounActivationLink) {
    getEntityManager().persist(newAccounActivationLink);
    return newAccounActivationLink;
  }

  /**
   * @param user
   * @return referencia a un objeto de tipo AccountActivationLink
   */
  public AccountActivationLink create(User user) {
    AccountActivationLink newAccounActivationLink = getInstance(user);
    getEntityManager().persist(newAccounActivationLink);
    return newAccounActivationLink;
  }

  /**
   * La fecha de expiracion de un enlace de activacion de cuenta es de
   * 48 horas a partir de la hora en la que se registra un usuario en
   * la aplicacion. La manera en la que se hace esto es mediante el valor
   * 172800000, el cual, es 48 horas en milisegundos.
   * 
   * @param user
   * @return referencia a un objeto de tipo AccountActivationLink que
   * representa un enlace de activacion de cuenta que tiene el correo
   * electronico dado, y un tiempo de expiracion de 48 horas desde la
   * fecha y la hora desde las que se lo emitio, las cuales, son la
   * fecha y la hora en la que un usuario se registra en la aplicacion
   */
  private AccountActivationLink getInstance(User user) {
    /*
     * El metodo getInstance de la clase Calendar retorna una
     * referencia a un objeto de tipo Calendar que contiene la
     * fecha y la hora actuales. Por lo tanto, dateIssue contiene
     * la fecha y la hora en las que se crea un enlace de activacion
     * de cuenta.
     */
    Calendar dateIssue = Calendar.getInstance();

    /*
     * La fecha y la hora de expiracion de un enlace de activacion
     * de cuenta se establece en 48 horas despues de la fecha y la
     * hora en la que se emite (crea) dicho enlace
     */
    Calendar expirationDate = Calendar.getInstance();
    expirationDate.setTimeInMillis(dateIssue.getTimeInMillis() + 172800000);

    AccountActivationLink newAccountActivationLink = new AccountActivationLink();
    newAccountActivationLink.setDateIssue(dateIssue);
    newAccountActivationLink.setExpirationDate(expirationDate);
    newAccountActivationLink.setUser(user);

    return newAccountActivationLink;
  }

  /**
   * Elimina fisicamente un enlace de activacion de cuenta de
   * usuario de la base de datos subyacente
   * 
   * @param id
   * @return referencia a un objeto de tipo AccountActivationLink
   * si se elimina el enlace de activacion de cuenta de usuario
   * correspondiente al ID dado de la base de datos subyacente, en
   * caso contrario null
   */
  public AccountActivationLink remove(int id) {
    AccountActivationLink givenAccountActivationLink = find(id);

    if (givenAccountActivationLink != null) {
      getEntityManager().remove(givenAccountActivationLink);
      return givenAccountActivationLink;
    }

    return null;
  }

  /**
   * Establece en true (1) el atributo consumed del enlace mas reciente
   * de activacion de cuenta correspondiente al correo electronico dado
   * 
   * @param userEmail
   */
  public void setConsumed(String userEmail) {
    AccountActivationLink latesAccountActivationLink = findByUserEmail(userEmail);
    latesAccountActivationLink.setConsumed(true);
  }

  public AccountActivationLink find(int id) {
    return getEntityManager().find(AccountActivationLink.class, id);
  }

  /**
   * Retorna el enlace mas reciente de activacion de cuenta de un usuario
   * si y solo existe en la base de datos subyacente.
   * 
   * La subconsulta de la consulta JPQL es para evitar que hayan errores
   * en tiempo de ejecucion en el caso en el que NO se borren los
   * enlaces de activacion de cuenta expirados de un usuario.
   * 
   * @param userEmail
   * @return referencia a un objeto de tipo AccountActivationLink si
   * se encuentra en la base de datos subyacente, el enlace de activacion
   * de cuenta mas reciente correspondiente a la direccion de correo
   * electronico de usuario dada, en caso contrario null
   */
  public AccountActivationLink findByUserEmail(String userEmail) {
    Query query = getEntityManager().createQuery("SELECT a FROM AccountActivationLink a WHERE a.user.email = :givenUserEmail AND a.dateIssue = (SELECT MAX(c.dateIssue) FROM AccountActivationLink c WHERE c.user.email = :givenUserEmail)");
    query.setParameter("givenUserEmail", userEmail);

    AccountActivationLink givenAccountActivationLink = null;

    try {
      givenAccountActivationLink = (AccountActivationLink) query.getSingleResult();
    } catch (NoResultException e) {
      e.printStackTrace();
    }

    return givenAccountActivationLink;
  }

  /**
   * Retorna todos los enlaces de activacion de cuenta, tanto los
   * consumidos como los no consumidos.
   * 
   * Un enlace de activacion de cuenta es consumido cuando el usuario
   * que lo obtuvo (por haberse registrado en la aplicacion) accede
   * a el antes de su tiempo de expiracion. Cuando esto ocurre el
   * atributo "consumed" de un enlace de activacion se establece en
   * 1 (true) en la base de datos subyacente.
   * 
   * @return referencia a un objeto de tipo Collection que contiene
   * todos los enlaces de activacion de cuenta, tanto los consumidos
   * como los no consumidos
   */
  public Collection<AccountActivationLink> findAll() {
    Query query = getEntityManager().createQuery("SELECT a FROM AccountActivationLink a ORDER BY a.id");
    return (Collection) query.getResultList();
  }

  /**
   * Retorna todos los enlaces de activacion de cuenta no consumidos,
   * es decir, que no fueron accedidos por sus respectivos usuarios
   * luego de haberse registrado en la aplicacion
   * 
   * @return referencia a un objeto de tipo Collection que contiene
   * todos los enlaces de activacion de cuenta no consumidos
   */
  public Collection<AccountActivationLink> findAllNotConsumed() {
    Query query = getEntityManager().createQuery("SELECT a FROM AccountActivationLink a WHERE a.consumed = false ORDER BY a.id");
    return (Collection) query.getResultList();
  }

  /**
   * Comprueba si existe el enlace mas reciente de activacion de
   * cuenta con el corre electronico dado. Retorna true si y solo
   * si existe en la base de datos subyacente un enlace mas reciente
   * de activacion de cuenta con el correo electronico dado.
   * 
   * @param userEmail
   * @return true si existe en la base de datos subyacente un enlace
   * mas reciente de activacion de cuenta con el correo electronico
   * dado, en caso contrario false. Tambien retorna false en el caso
   * en el que la direccion de correo electronico tiene el valor nulo.
   */
  public boolean checkExistence(String userEmail) {
    /*
     * Si la direccion de correo electronico de usuario tiene el
     * valor null, se retorna false, ya que realizar la operacion
     * de este metodo con un correo electronico que tiene el valor
     * null es similar a realizar dicha operacion con un enlace
     * de activacion de cuenta inexistente.
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

    return (findByUserEmail(userEmail) != null);
  }

  /**
   * Retorna true si y solo si el enlace mas reciente de activacion
   * de cuenta correspondiente a un correo electronico fue consumido.
   * 
   * Un enlace de activacion de cuenta es consumido cuando el usuario
   * que lo obtuvo (por haberse registrado en la aplicacion) accede
   * a el antes de su tiempo de expiracion. Cuando esto ocurre el
   * atributo "consumed" de un enlace de activacion se establece en
   * 1 (true) en la base de datos subyacente.
   * 
   * Hay que tener en cuenta que este metodo debe ser invocado luego
   * de haber invocado al metodo checkExistence de esta clase, ya
   * que de lo contrario puede ocurrir la excepcion NoResultException,
   * la cual, ocurre cuando NO existe en la base de datos subyacente
   * el dato consultado.
   * 
   * @param userEmail
   * @return true si el enlace de activacion de cuenta mas reciente
   * correspondiente a la direccion de correo electronico dada fue
   * consumido, en caso contrario false
   */
  public boolean checkConsumed(String userEmail) {
    return findByUserEmail(userEmail).getConsumed();
  }

  /**
   * Retorna true si y solo si el enlace mas reciente de activacion
   * de cuenta correspondiente al correo electronico dado, expiro.
   * 
   * Hay que tener en cuenta que este metodo debe ser invocado luego
   * de haber invocado al metodo checkExistence de esta clase, ya
   * que de lo contrario puede ocurrir la excepcion NoResultException,
   * la cual, ocurre cuando NO existe en la base de datos subyacente
   * el dato consultado.
   * 
   * @param userEmail
   * @return true si el enlace mas reciente de activacion de cuenta
   * correspondiente al correo electronico dado, expiro, en caso
   * contrario false
   */
  public boolean checkExpiration(String userEmail) {
    AccountActivationLink latestAccountActivationLink = findByUserEmail(userEmail);

    /*
     * Si la fecha de expiracion del enlace mas reciente de activacion
     * de cuenta correspondiente a la direccion de correo electronico
     * dada, es estrictamente menor (esta antes) de la fecha actual,
     * dicho enlace expiro, por lo tanto, se retorna true.
     * 
     * El metodo getInstance de la clase Calendar retorna la referencia
     * a un objeto de tipo Calendar que tiene la fecha actual.
     * 
     * La documentacion del metodo before de la clase Calendar dice que
     * este metodo es equivalente a: compareTo(when) < 0.
     */
    if (latestAccountActivationLink.getExpirationDate().before(Calendar.getInstance())) {
      return true;
    }

    return false;
  }

  /**
   * Retorna true si y solo si un enlace de activacion de cuenta expiro.
   * 
   * Un enlace de activacion de cuenta expira cuando su fecha de expiracion
   * es estrictamente menor (esta antes) que la fecha actual.
   * 
   * @param givenAccountActivationLink
   * @return true si un enlace de activacion de cuenta expiro, en caso
   * contrario false
   */
  public boolean checkExpiration(AccountActivationLink givenAccountActivationLink) {
    /*
     * Si la fecha de expiracion de un enlace de activacion de cuenta
     * es estrictamente menor (esta antes) que la fecha actual, dicho
     * enlace expiro, por lo tanto, se retorna true.
     * 
     * El metodo getInstance de la clase Calendar retorna la referencia
     * a un objeto de tipo Calendar que tiene la fecha actual.
     * 
     * La documentacion del metodo before de la clase Calendar dice que
     * este metodo es equivalente a: compareTo(when) < 0.
     */
    if (givenAccountActivationLink.getExpirationDate().before(Calendar.getInstance())) {
      return true;
    }

    return false;
  }

}
