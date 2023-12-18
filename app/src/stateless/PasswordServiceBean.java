package stateless;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import model.Password;
import org.apache.commons.codec.digest.DigestUtils;

@Stateless
public class PasswordServiceBean {

  @PersistenceContext(unitName = "swcar")
  protected EntityManager entityManager;

  public void setEntityManager(EntityManager localEntityManager) {
    entityManager = localEntityManager;
  }

  public Password create(Password newPassword) {
    entityManager.persist(newPassword);
    return newPassword;
  }

  /**
   * Elimina fisicamente una contraseña de la base de datos subyacente
   * 
   * @param id
   * @return referencia a un objeto de tipo Password en caso de eliminarse
   * de la base de datos subyacente, la contraseña correspondiente al
   * ID dado, en caso contrario null
   */
  public Password remove(int id) {
    Password givenPassword = entityManager.find(Password.class, id);

    if (givenPassword != null) {
      entityManager.remove(givenPassword);
      return givenPassword;
    }

    return null;
  }

  /**
   * Modifica la contraseña del usuario correspondiente al ID dado
   * con la nueva contraseña ingresada por el mismo
   * 
   * @param userId
   * @param newPlainPassword
   */
  public void modify(int userId, String newPlainPassword) {
    Query query = entityManager.createQuery("UPDATE Password p SET p.value = :newPassword WHERE p.user.id = :userId");
    query.setParameter("userId", userId);
    query.setParameter("newPassword", getPasswordHash(newPlainPassword));
    query.executeUpdate();
  }

  /**
   * Retorna true si y solo si el nombre de usuario y la contraseña
   * provistos son iguales a uno de los pares (nombre de usuario, contraseña)
   * que estan almacenados en la base de datos subyacente. De esta manera,
   * se realiza la autenticacion del usuario que inicia sesion en la
   * aplicacion.
   *
   * @param username provisto por el usuario cuando inicia sesion en la
   * aplicacion para demostrar que es quien dice ser
   * @param plainPassword provisto por el usuario cuando inicia sesion
   * en la aplicacion para demostrar que es quien dice ser
   * @return true si el nombre de usuario y la contraseña provistos son
   * iguales a uno de los pares (nombre de usuario, contraseña) que estan
   * almacenados en la base de datos subyacente, en caso contrario false
   */
  public boolean authenticateUser(String username, String plainPassword) {
    /*
     * Si el nombre de usuario y/o la contraseña tienen el valor null,
     * se retorna false, ya que realizar la autenticacion con un nombre
     * de usuario y/o una contraseña con este valor es similar a autenticar
     * un usuario inexistente en la base de datos subyacente.
     * 
     * Con este control se evita realizar una consulta a la base de
     * datos comparando el nombre de usuario y/o la contraseña con
     * el valor null. Si no se realiza este control y se realiza esta
     * consulta a la base de datos, ocurre la excepcion
     * SQLSyntaxErrorException, debido a que la comparacion de
     * un atributo con el valor null incumple la sintaxis del
     * proveedor del motor de base de datos.
     */
    if (username == null || plainPassword == null) {
      return false;
    }

    return (findByUsernameAndPassword(username, plainPassword) != null);
  }

  /**
   * Retorna una referencia a un objeto de tipo Password si y solo si
   * se encuentra en la base de datos subyacente, el usuario
   * correspondiente al nombre de usuario y la contraseña dados
   * 
   * @param username
   * @param plainPassword
   * @return referencia a un objeto de tipo Password si se encuentra en
   * la base de datos subyacente, el usuario correspondiente al
   * nombre de usuario y la contraseña dados, en caso contrario null
   */
  private Password findByUsernameAndPassword(String username, String plainPassword) {
    Query query = entityManager.createQuery("SELECT p FROM Password p WHERE UPPER(p.user.username) = UPPER(:username) AND UPPER(p.value) = UPPER(:password)");
    query.setParameter("username", username);
    query.setParameter("password", getPasswordHash(plainPassword));

    Password givenUser = null;

    try {
      givenUser = (Password) query.getSingleResult();
    } catch (NoResultException e) {
      e.printStackTrace();
    }

    return givenUser;
  }

  /**
   * Calcula el valor hash de una contraseña plana usando el algoritmo
   * SHA256
   * 
   * @param plainPassword
   * @return una referencia a un objeto de tipo String que contiene
   * el valor hash en formato SHA256 hexadecimal de una contraseña
   * plana
   */
  public String getPasswordHash(String plainPassword) {
    return DigestUtils.sha256Hex(plainPassword);
  }

}