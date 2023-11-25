package stateless;

import java.util.Calendar;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import model.Session;
import model.User;
import util.UtilDate;

@Stateless
public class SessionServiceBean {

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

  public Session create(Session newSession) {
    getEntityManager().persist(newSession);
    return newSession;
  }

  /**
   * @param user
   * @param dateIssue
   * @param expirationDate
   * @return referencia a un objeto de tipo Session creado
   * con los datos provistos mediante los parametros
   */
  public Session create(User user, Calendar dateIssue, Calendar expirationDate) {
    Session newSession = new Session();
    newSession.setUser(user);
    newSession.setDateIssue(dateIssue);
    newSession.setExpirationDate(expirationDate);

    getEntityManager().persist(newSession);
    return newSession;
  }

  /**
   * Elimina logicamente la sesion actual que abrio el usuario
   * con el ID dado. Esta eliminacion logica se realiza cuando
   * el usuario cierra su sesion actual.
   * 
   * Se denomina "sesion actual" a la ultima sesion vigente
   * que tiene un usuario.
   * 
   * Cuando el usuario cierra la sesion que tiene abierta sin que
   * esta haya llegado a su tiempo de expiracion, se registra
   * este cierre en la base de datos. Si no se hace esto, el
   * usuario no puede iniciar sesion nuevamente, debido a que
   * la sesion que cerro todavia no ha llegado a su tiempo de
   * expiracion.
   * 
   * Una sesion que NO ha llegado a su tiempo de expiracion,
   * pero que fue cerrada, es una sesion NO vigente.
   * 
   * Una sesion que ha llegado a su tiempo de expiracion,
   * pero que NO fue cerrada, es una sesion NO vigente.
   * 
   * @param userId
   * @return referencia a un objeto de tipo Session con su
   * variable closed en false, si se encuentra la sesion
   * del usuario con el ID dado, null en caso contrario
   */
  public Session remove(int userId) {
    Session givenSession = findLastSession(userId);

    if (givenSession != null) {
      givenSession.setClosed(true);
      return givenSession;
    }

    return null;
  }

  /**
   * Retorna true si y solo si el usuario con el ID dado tiene
   * una sesion activa.
   * 
   * Se denomina "sesion activa" a aquella que NO ha expirado o
   * que sin haber expirado NO ha sido cerrada por el usuario que
   * la abrio.
   * 
   * Una sesion expira cuando su fecha de expiracion esta antes
   * de la fecha actual.
   * 
   * @param userId
   * @return true si el usuario con el ID dado tiene una sesion
   * activa, false en caso contrario
   */
  public boolean checkActiveSession(int userId) {
    /*
     * La ultima sesion del usuario es la unica sesion de todas
     * las sesiones del usuario que puede estar activa
     */
    Session lastSession = findLastSession(userId);

    /*
     * Cuando el usuario inicia sesion por primera vez, no tiene
     * ninguna sesion registrada en la base de datos subyacente,
     * ni activa ni inactiva. Por lo tanto, se retorna false
     * como indicador de que el usuario que inicia sesion por
     * primera vez, NO tiene una sesion activa.
     */
    if (lastSession == null) {
      return false;
    }

    /*
     * Si la fecha de expiracion de la ultima sesion registrada del
     * usuario, esta antes de la fecha actual del sistema, se
     * retorna false.
     * 
     * Una sesion que tiene su fecha de expiracion antes de la fecha
     * actual, es una sesion inactiva.
     */
    if (lastSession.getExpirationDate().before(UtilDate.getCurrentDate())) {
      return false;
    }

    /*
     * Si el usuario tiene abierta una sesion que no ha expirado (lo cual,
     * se sabe por el control anterior) y la cierra el mismo, se retorna
     * false.
     * 
     * Una sesion que no ha expirado, pero que ha sido cerrada por el
     * usuario que la abrio, es una sesion inactiva.
     */
    if (lastSession.getClosed()) {
      return false;
    }

    /*
     * Si el usuario que intenta abrir una sesion con su cuenta,
     * tiene una sesion activa, se retorna true como indicador
     * de esto
     */
    return true;
  }

  /**
   * Retorna la ultima sesion que abrio el usuario con el
   * ID dado.
   * 
   * @param userId
   * @return referencia a un objeto de tipo Session si
   * se encuentra la ultima sesion que abrio el usuario
   * con el ID dado, null en caso contrario
   */
  public Session findLastSession(int userId) {
    Query query = getEntityManager().createQuery("SELECT s FROM Session s WHERE s.id = (SELECT MAX(s.id) FROM Session s JOIN s.user p WHERE p.id = :userId)");
    query.setParameter("userId", userId);

    Session givenSession = null;

    try {
      givenSession = (Session) query.getSingleResult();
    } catch (NoResultException e) {
      e.printStackTrace();
    }

    return givenSession;
  }

  /**
   * Elimina fisicamente la sesion que tiene el ID dado.
   * 
   * @param sessionId
   * @return referencia a un objeto de tipo Session si existe
   * una sesion con el ID dado, null en caso contrario
   */
  public Session delete(int sessionId) {
    Session givenSession = getEntityManager().find(Session.class, sessionId);

    if (givenSession != null) {
      getEntityManager().remove(givenSession);
      return givenSession;
    }

    return null;
  }

}
