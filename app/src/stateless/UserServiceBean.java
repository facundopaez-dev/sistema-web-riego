package stateless;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import model.User;

@Stateless
public class UserServiceBean {

  @PersistenceContext(unitName = "swcar")
  protected EntityManager entityManager;

  public void setEntityManager(EntityManager localEntityManager) {
    entityManager = localEntityManager;
  }

  public EntityManager getEntityManager() {
    return entityManager;
  }

  public User create(User newUser) {
    getEntityManager().persist(newUser);
    return newUser;
  }

  /**
   * Elimina fisicamente un usuario de la base de datos subyacente
   * 
   * @param id
   * @return referencia a un objeto de tipo User en caso de eliminarse
   * de la base de datos subyacente, el usuario correspondiente al ID
   * dado, en caso contrario null
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
   * Modifica el permiso de administrador de un usuario
   * 
   * @param userId
   * @param superuser
   */
  public void modifySuperuserPermission(int userId, boolean superuser) {
    Query query = entityManager.createQuery("UPDATE User u SET u.superuser = :superuser WHERE u.id = :userId");
    query.setParameter("superuser", superuser);
    query.setParameter("userId", userId);
    query.executeUpdate();
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

  /**
   * Segun la documentacion web de la clase EntityManager, el metodo
   * merge() de esta clase fusiona el estado de una entidad en el
   * contexto de persistencia actual.
   * 
   * Los siguientes dos parrafos pertenecen a la pagina 161 del
   * libro "Pro JPA 2 Mastering the JavaTM Persistente API".
   * 
   * Devolver una instancia administrada distinta de la entidad
   * original es una parte fundamental del proceso de fusion.
   * Si ya existe una instancia de entidad con el mismo identificador
   * en el contexto de persistencia, el proveedor sobrescribira
   * su estado con el estado de la entidad que se esta fusionando,
   * pero la version administrada que ya existia debe devolverse
   * al cliente para que pueda ser usada. Si el proveedor no
   * actualiza una instancia en el contexto de persistencia,
   * cualquier referencia a esa instancia sera inconsistente
   * con el nuevo estado en el que se fusionara.
   * 
   * Cuando se invoca merge() en una nueva entidad, se comporta
   * de manera similar a la operacion persist(). Agrega la entidad
   * al contexto de persistencia, pero en lugar de agregar la
   * instancia de la entidad original, crea una nueva copia y
   * administra esa instancia. La copia creada por la operacion
   * merge() persiste como si se invocara el metodo persist()
   * en ella.
   * 
   * ************************************************************
   * 
   * Debido a la funcion que cumple el metodo merge() de la clase
   * EntityManager:
   * - la tabla de union que existe entre las entidades User y
   * Parcel en la base de datos subyacente, es escrita en funcion
   * de una instancia de tipo User y su coleccion de parcelas
   * llamada parcels.
   * - la tabla que existe para la entidad Parcel en la base de
   * datos subyacente, es actualizada con los cambios realizados
   * en los elementos de la coleccion parcels de una instancia
   * de tipo User.
   * 
   * @param user
   * @return referencia a un objeto de tipo User que contiene
   * los cambios realizados en el durante la ejecucion de la
   * aplicacion, si se invoca este metodo con una entidad de
   * tipo User administrada. En cambio, si se lo invoca con
   * una nueva entidad de tipo User, la persiste.
   */
  public User merge(User user) {
    return entityManager.merge(user);
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
   * @param parcelId
   * @return referencia a un objeto de tipo User si en la
   * base de datos subyacente existe un usuario que tiene
   * una parcela que tiene el ID dado, en caso contrario
   * null
   */
  public User findByParcelId(int parcelId) {
    Query query = getEntityManager().createQuery("SELECT u FROM User u JOIN u.parcels t WHERE t.id = :parcelId");
    query.setParameter("parcelId", parcelId);

    User user = null;

    try {
      user = (User) query.getSingleResult();
    } catch (NoResultException e) {
      e.printStackTrace();
    }

    return user;
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
   * Activa al usuario que tiene el correo electronico dado.
   * De esta manera, el usuario puede iniciar sesion en la
   * aplicacion.
   * 
   * Establece en true (1) el atributo active del usuario
   * correspondiente al correo electronico dado.
   * 
   * @param email
   */
  public void activateUser(String email) {
    User givenUser = findByEmail(email);
    givenUser.setActive(true);
  }

  /**
   * Retorna true si y solo si el usuario correspondiente al correo
   * electronico dado, esta activo.
   * 
   * Hay que tener en cuenta que este metodo debe ser invocado luego
   * de invocar al metodo checkExistenceEmail(String email) de esta
   * clase. Esto se debe a que se puede consultar si un usuario esta
   * activo o no mediante un correo electronico con el valor null.
   * En este caso, si se hace esta consulta sin invocar primero al
   * metodo checkExistenceEmail(String email) ocurrira la excepcion
   * SQLSyntaxErrorException, ya que la comparacion de un atributo
   * con el valor null incumple la sintaxis del proveedor del motor
   * de base de datos.
   * 
   * @param email
   * @return true si el usuario correspondiente al correo
   * electronico dado, esta activo, en caso contrario false
   */
  public boolean isActive(String email) {
    return findByEmail(email).getActive();
  }

  public Page<User> findAllActiveUsersExceptOwnUser(int userId, Integer page, Integer cantPerPage, Map<String, String> parameters) {
    // Genera el WHERE dinámicamente
    StringBuffer where = new StringBuffer(" WHERE 1=1 AND e.id != :userId AND e.active = TRUE");

    if (parameters != null) {

      for (String param : parameters.keySet()) {
        Method method;

        try {
          method = User.class.getMethod("get" + capitalize(param));

          if (method == null || parameters.get(param) == null || parameters.get(param).isEmpty()) {
            continue;
          }

          switch (method.getReturnType().getSimpleName()) {
            case "String":
              where.append(" AND UPPER(e.");
              where.append(param);
              where.append(") LIKE UPPER('%");
              where.append(parameters.get(param));
              where.append("%')");
              break;
            default:
              where.append(" AND e.");
              where.append(param);
              where.append(" = ");
              where.append(parameters.get(param));
              break;
          }
        } catch (NoSuchMethodException | SecurityException e) {
          e.printStackTrace();
        }

      } // End for

    } // End if

    // Cuenta el total de resultados
    Query countQuery = entityManager.createQuery("SELECT COUNT(e.id) FROM " + User.class.getSimpleName() + " e" + where.toString());
    countQuery.setParameter("userId", userId);

    // Pagina
    Query query = entityManager.createQuery("FROM " + User.class.getSimpleName() + " e" + where.toString() + " ORDER BY e.id");
    query.setMaxResults(cantPerPage);
    query.setFirstResult((page - 1) * cantPerPage);
    query.setParameter("userId", userId);

    Integer count = ((Long) countQuery.getSingleResult()).intValue();
    Integer lastPage = (int) Math.ceil((double) count / (double) cantPerPage);

    // Arma la respuesta
    Page<User> resultPage = new Page<User>(page, count, page > 1 ? page - 1 : page, page < lastPage ? page + 1 : lastPage, lastPage, query.getResultList());
    return resultPage;
  }

  private String capitalize(final String line) {
    return Character.toUpperCase(line.charAt(0)) + line.substring(1);
  }

}