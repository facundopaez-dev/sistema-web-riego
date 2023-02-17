package stateless;

import java.util.Collection;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import model.User;
import org.apache.commons.codec.digest.DigestUtils;

@Stateless
public class UserServiceBean {

  @PersistenceContext(unitName = "swcar")
  protected EntityManager entityManager;

  public void setEntityManager(EntityManager emLocal) {
    entityManager = emLocal;
  }

  public EntityManager getEntityManager() {
    return entityManager;
  }

  public User create(User newUser) {
    getEntityManager().persist(newUser);
    return newUser;
  }

  /**
   * Elimina fisicamente un usuario de la base de datos subyacente.
   * Este metodo es para las pruebas unitarias en las que se
   * persisten usuarios, los cuales, deben ser borrados de la
   * base de datos subyacente luego de la ejecucion de dichas
   * pruebas.
   * 
   * @param id
   * @return referencia a un objeto de tipo User con su variable
   * de instancia "active" en false en caso de encontrarse en la
   * base de datos subyacente el usuario con el ID dado, en caso
   * contrario null
   */
  public User remove(int id) {
    User givenUser = find(id);

    if (givenUser != null) {
      getEntityManager().remove(givenUser);
      return givenUser;
    }

    return null;
  }

  /**
   * Modifica el nombre de usuario, el nombre, el apellido y la
   * direccion del correo electronico de un usuario en la base
   * de datos subyacente
   * 
   * @param id
   * @param modifiedUser
   * @return referencia a un objeto de tipo User que tiene algunos
   * (nombre de usuario, nombre, apellido y correo electronico) de
   * sus datos modificados en caso de encontrarse en la base de
   * datos subyacente el usuario con el ID dado, en caso contrario
   * null
   */
  public User modify(int id, User modifiedUser) {
    User givenUser = find(id);

    if (givenUser != null) {
      givenUser.setUsername(modifiedUser.getUsername());
      givenUser.setName(modifiedUser.getName());
      givenUser.setLastName(modifiedUser.getLastName());
      givenUser.setEmail(modifiedUser.getEmail());
      return givenUser;
    }

    return null;
  }

  public User find(int id) {
    return getEntityManager().find(User.class, id);
  }

  /**
   * @return referencia a un objeto de tipo Collection que contiene
   * todos los usuarios (activos e inactivos), excepto el usuario
   * admin
   */
  public Collection<User> findAll() {
    Query query = getEntityManager().createQuery("SELECT u FROM User u WHERE u.username != :username ORDER BY u.id");
    query.setParameter("username", "admin");

    return (Collection) query.getResultList();
  }

  /**
   * Busca un usuario en la base de datos subyacente mediante un
   * nombre de usuario
   *
   * @param username el nombre de usuario que se usa para buscar
   * en la base de datos subyacente, el usuario que tiene el nombre
   * de usuario provisto
   * @return referencia a un objeto de tipo User en caso de encontrarse
   * en la base de datos subyacente, el usuario con el nombre de usuario
   * provisto, null en caso contrario
   */
  public User findByUsername(String username) {
    Query query = getEntityManager().createQuery("SELECT u FROM User u WHERE UPPER(u.username) = UPPER(:username)");
    query.setParameter("username", username);

    User user = null;

    try {
      user = (User) query.getSingleResult();
    } catch (NoResultException e) {
      e.printStackTrace();
    }

    return user;
  }

  /**
   * Realiza la autenticacion de un usuario mediante el nombre de usuario
   * y la contraseña que provee
   *
   * @param username el nombre de usuario que se usa para autentificar
   * (demostrar que el usuario es quien dice ser) la cuenta del usuario
   * que inicia sesion
   * @param plainPassword la contraseña que se usa para autentificar
   * (demostrar que el usuario es quien dice ser) la cuenta del usuario
   * que inicia sesion
   * @return true si el nombre de usuario y la contraseña provistos son
   * iguales al nombre de usuario y la contraseña que estan almacenados
   * en la base de datos subyacente, false en caso contrario
   */
  public boolean authenticate(String username, String plainPassword) {
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

    Query query = getEntityManager().createQuery("SELECT u FROM User u WHERE UPPER(u.username) = UPPER(:username) AND UPPER(u.password) = UPPER(:password)");
    query.setParameter("username", username);
    query.setParameter("password", getPasswordHash(plainPassword));

    boolean result = false;

    try {
      query.getSingleResult();
      result = true;
    } catch (NoResultException e) {
      e.printStackTrace();
    }

    return result;
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

  /**
   * Comprueba si el usuario con el nombre de usuario provisto, tiene
   * el permiso de administrador (super usuario)
   *
   * @param username el nombre de usuario que se usa para comprobar si
   * el usuario con el nombre de usuario provisto, tiene el permiso de
   * administrador
   * @return true si el usuario con el nombre de usuario provisto, tiene
   * el permiso de administrador, false en caso contrario
   */
  public boolean checkSuperuserPermission(String username) {
    User givenUser = findByUsername(username);
    return givenUser.getSuperuser();
  }

}
