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
   * Retorna el usuario que tiene el ID dado.
   * 
   * El motivo de este metodo es la directiva ng-repeat de AngularJS,
   * la cual, opera con una coleccion de datos. El objetivo es implementar
   * una pagina web para que el usuario pueda ver sus datos en una lista
   * de una fila. Es decir, lo que se quiere es que los datos del usuario
   * que tiene una sesion abierta, se vean en forma de fila, y en una unica
   * fila.
   * 
   * Para lograr este objetivo se debe retornar una coleccion con un
   * unico usuario, el que tiene una sesion abierta, porque la directiva
   * ng-repeat opera con una coleccion de datos.
   * 
   * @param userId
   * @return referencia a un objeto de tipo Collection que contiene
   *         unicamente un usuario: el correspondiente al ID dado
   */
  public Collection<User> findMyUser(int userId) {
    Query query = getEntityManager().createQuery("SELECT u FROM User u WHERE u.id = :userId");
    query.setParameter("userId", userId);

    return (Collection) query.getResultList();
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
   * Busca un usuario en la base de datos subyacente mediante una
   * direccion de correo electronico
   * 
   * @param email
   * @return referencia a un objeto de tipo User en caso de encontrarse
   *         en la base de datos subyacente, el usuario con la direccion
   *         de correo electronico provista, en caso contrario null
   */
  public User findByEmail(String email) {
    Query query = getEntityManager().createQuery("SELECT u FROM User u WHERE UPPER(u.email) = UPPER(:email)");
    query.setParameter("email", email);

    User user = null;

    try {
      user = (User) query.getSingleResult();
    } catch (NoResultException e) {
      e.printStackTrace();
    }

    return user;
  }

  /**
   * Retorna una referencia a un objeto de tipo User si y solo si el
   * nombre de usuario existe dentro del conjunto de usuarios en el
   * que NO esta el usuario del ID dado. El motivo por el cual se hace
   * esta descripcion de esta manera es que un usuario tiene un nombre
   * de usuario.
   * 
   * Este metodo es para cuando el usuario que tiene el ID dado modifica
   * su nombre de usuario. Lo que se busca con este metodo es verificar
   * que el nombre de usuario modificado NO este registrado en la cuenta
   * de otro usuario.
   * 
   * Si el nombre de usuario modificado NO esta registrado en la cuenta
   * de otro usuario, la aplicacion realiza la modificacion del nombre
   * de usuario. En caso contrario, no la realiza.
   * 
   * @param userId
   * @param username
   * @return referencia a un objeto de tipo User si el nombre de usuario
   *         existe dentro del conjunto de usuarios en el que NO esta el
   *         usuario del ID dado, en caso contrario null
   */
  public User findByUsername(int userId, String username) {
    Query query = getEntityManager().createQuery("SELECT u FROM User u WHERE (u.id != :userId AND UPPER(u.username) = UPPER(:username))");
    query.setParameter("userId", userId);
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
   * Retorna una referencia a un objeto de tipo User si y solo si la
   * direccion de correo electronico existe dentro del conjunto de
   * usuarios en el que NO esta el usuario del ID dado. El motivo por
   * el cual se hace esta descripcion de esta manera es que un usuario
   * tiene una direccion de correo electronico.
   * 
   * Este metodo es para cuando el usuario que tiene el ID dado modifica
   * su direccion de correo electronico. Lo que se busca con este metodo
   * es verificar que la direccion de correo electronica modificada NO
   * este registrada en la cuenta de otro usuario.
   * 
   * Si la direccion de correo electronico modificada NO esta registrada
   * en la cuenta de otro usuario, la aplicacion realiza la modificacion
   * de la direccion de correo electronico. En caso contrario, no la realiza.
   * 
   * @param userId
   * @param username
   * @return referencia a un objeto de tipo User si el nombre de usuario
   *         existe dentro del conjunto de usuarios en el que NO esta el
   *         usuario del ID dado, en caso contrario null
   */
  public User findByEmail(int userId, String email) {
    Query query = getEntityManager().createQuery("SELECT u FROM User u WHERE (u.id != :userId AND UPPER(u.email) = UPPER(:email))");
    query.setParameter("userId", userId);
    query.setParameter("email", email);

    User user = null;

    try {
      user = (User) query.getSingleResult();
    } catch (NoResultException e) {
      e.printStackTrace();
    }

    return user;
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

    return (findByUsernameAndPassword(username, plainPassword) != null);
  }

  /**
   * Retorna una referencia a un objeto de tipo User si y solo si
   * se encuentra en la base de datos subyacente, el usuario
   * correspondiente al nombre de usuario y la contraseña dados
   * 
   * @param username
   * @param plainPassword
   * @return referencia a un objeto de tipo User si se encuentra en
   * la base de datos subyacente, el usuario correspondiente al
   * nombre de usuario y la contraseña dados, en caso contrario null
   */
  private User findByUsernameAndPassword(String username, String plainPassword) {
    Query query = getEntityManager().createQuery("SELECT u FROM User u WHERE UPPER(u.username) = UPPER(:username) AND UPPER(u.password) = UPPER(:password)");
    query.setParameter("username", username);
    query.setParameter("password", getPasswordHash(plainPassword));

    User givenUser = null;

    try {
      givenUser = (User) query.getSingleResult();
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

  /**
   * Retorna true si y solo si el nombre de usuario existe en
   * la base de datos subyacente
   * 
   * @param username
   * @return true si el nombre de usuario existe en la base
   * de datos subyacente, en caso contrario false
   */
  public boolean checkExistenceUsername(String username) {
    /*
     * Si el nombre de usuario tiene el valor null, se retorna
     * false, ya que realizar la busqueda de un nombre de
     * usuario con este valor es similar a buscar un nombre
     * de usuario inexistente en la base de datos subyacente.
     * 
     * Con este control se evita realizar una consulta a la base
     * de datos comparando el nombre de usuario con el valor
     * null. Si no se realiza este control y se realiza esta
     * consulta a la base de datos, ocurre la excepcion
     * SQLSyntaxErrorException, debido a que la comparacion de
     * un atributo con el valor null incumple la sintaxis del
     * proveedor del motor de base de datos.
     */
    if (username == null) {
      return false;
    }

    return (findByUsername(username) != null);
  }

  /**
   * Retorna true si y solo si la direccion de correo electronico
   * existe en la base de datos subyacente
   * 
   * @param email
   * @return true si la direccion de correo electronico existe en
   * la base de datos subyacente, en caso contrario false
   */
  public boolean checkExistenceEmail(String email) {
    /*
     * Si la direccion de correo electronico tiene el valor
     * null, se retorna false, ya que realizar la busqueda
     * de una direccion de correo electronico con este valor
     * es similar a buscar una direccion de correo electronico
     * inexistente en la base de datos subyacente.
     * 
     * Con este control se evita realizar una consulta a la base
     * de datos comparando la direccion de correo electronico
     * con el valor null. Si no se realiza este control y se
     * realiza esta consulta a la base de datos, ocurre la excepcion
     * SQLSyntaxErrorException, debido a que la comparacion de
     * un atributo con el valor null incumple la sintaxis del
     * proveedor del motor de base de datos.
     */
    if (email == null) {
      return false;
    }

    return (findByEmail(email) != null);
  }

  /**
   * Retorna true si y solo si el nombre de usuario existe dentro del
   * conjunto de usuarios en el que NO esta el usuario del ID dado. El
   * motivo por el cual se hace esta descripcion de esta manera es que
   * un usuario tiene un nombre de usuario.
   * 
   * Este metodo es para cuando el usuario que tiene el ID dado modifica
   * su nombre de usuario. Lo que se busca con este metodo es verificar
   * que el nombre de usuario modificado NO este registrado en la cuenta
   * de otro usuario.
   * 
   * Si el nombre de usuario modificado NO esta registrado en la
   * cuenta de otro usuario, la aplicacion realiza la modificacion
   * del nombre de usuario. En caso contrario, no la realiza.
   * 
   * @param userId
   * @param username
   * @return true si el nombre de usuario existe dentro del conjunto
   *         de usuarios en el que NO esta el usuario del ID dado, en
   *         caso contrario false
   */
  public boolean checkExistenceUsername(int userId, String username) {
    /*
     * Si el nombre de usuario tiene el valor null, se retorna
     * false, ya que realizar la busqueda de un nombre de
     * usuario con este valor es similar a buscar un nombre
     * de usuario inexistente en la base de datos subyacente.
     * 
     * Con este control se evita realizar una consulta a la base
     * de datos comparando el nombre de usuario con el valor
     * null. Si no se realiza este control y se realiza esta
     * consulta a la base de datos, ocurre la excepcion
     * SQLSyntaxErrorException, debido a que la comparacion de
     * un atributo con el valor null incumple la sintaxis del
     * proveedor del motor de base de datos.
     */
    if (username == null) {
      return false;
    }

    return (findByUsername(userId, username) != null);
  }

  /**
   * Retorna true si y solo si la direccion de correo electronico esta
   * dentro del conjunto de usuarios en el que NO esta el usuario del
   * ID dado. El motivo por el cual se hace esta descripcion de esta
   * manera es que un usuario tiene una direccion de correo electronico.
   * 
   * Este metodo es para cuando el usuario que tiene el ID dado modifica
   * su direccion de correo electronico. Lo que se busca con este metodo
   * es verificar que la direccion de correo electronica modificada NO
   * este registrada en la cuenta de otro usuario.
   * 
   * Si la direccion de correo electronico modificada NO esta registrada
   * en la cuenta de otro usuario, la aplicacion realiza la modificacion
   * de la direccion de correo electronico. En caso contrario, no la realiza.
   * 
   * @param userId
   * @param email
   * @return true si la direccion de correo electronico existe dentro
   *         del conjunto de usuarios en el que NO esta el usuario del
   *         ID dado, en caso contrario false
   */
  public boolean checkExistenceEmail(int userId, String email) {
    /*
     * Si la direccion de correo electronico tiene el valor
     * null, se retorna false, ya que realizar la busqueda
     * de una direccion de correo electronico con este valor
     * es similar a buscar una direccion de correo electronico
     * inexistente en la base de datos subyacente.
     * 
     * Con este control se evita realizar una consulta a la base
     * de datos comparando la direccion de correo electronico
     * con el valor null. Si no se realiza este control y se
     * realiza esta consulta a la base de datos, ocurre la excepcion
     * SQLSyntaxErrorException, debido a que la comparacion de
     * un atributo con el valor null incumple la sintaxis del
     * proveedor del motor de base de datos.
     */
    if (email == null) {
      return false;
    }

    return (findByEmail(userId, email) != null);
  }

  /**
   * Modifica la contraseña del usuario correspondiente al ID dado
   * con la nueva contraseña ingresada por el mismo
   * 
   * @param userId
   * @param newPlainPassword
   */
  public void modifyPassword(int userId, String newPlainPassword) {
    Query query = getEntityManager().createQuery("UPDATE User u SET u.password = :newPassword WHERE u.id = :userId");
    query.setParameter("userId", userId);
    query.setParameter("newPassword", getPasswordHash(newPlainPassword));
    query.executeUpdate();
  }

}
