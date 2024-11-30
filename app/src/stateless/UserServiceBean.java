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
    User chosenUser = find(id);

    if (chosenUser != null) {
      chosenUser.setUsername(modifiedUser.getUsername());
      chosenUser.setName(modifiedUser.getName());
      chosenUser.setLastName(modifiedUser.getLastName());
      return chosenUser;
    }

    return null;
  }

  public User find(int id) {
    return getEntityManager().find(User.class, id);
  }

  /**
   * Busca un usuario en la base de datos subyacente mediante una
   * direccion de correo electronico
   * 
   * @param address
   * @return referencia a un objeto de tipo User en caso de encontrarse
   * en la base de datos subyacente, el usuario con la direccion
   * de correo electronico provista, en caso contrario null
   */
  public User findUserByEmail(String address) {
    Query query = getEntityManager().createQuery("SELECT u FROM User u JOIN u.email e WHERE UPPER(e.address) = UPPER(:address)");
    query.setParameter("address", address);

    User user = null;

    try {
      user = (User) query.getSingleResult();
    } catch (NoResultException e) {
      e.printStackTrace();
    }

    return user;
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
   * Activa un usuario correspondienta a un ID. De esta manera,
   * el usuario puede iniciar sesion en la aplicacion.
   * 
   * @param userId
   */
  public void activateUser(int userId) {
    Query query = entityManager.createQuery("UPDATE User u SET u.active = TRUE WHERE u.id = :userId");
    query.setParameter("userId", userId);
    query.executeUpdate();
  }

  /**
   * Retorna true si y solo si el usuario correspondiente a un ID
   * esta activo
   * 
   * @param userId
   * @return true si el usuario correspondiente a un ID esta
   * activo, en caso contrario false
   */
  public boolean isActive(int userId) {
    return find(userId).getActive();
  }

  public Page<User> findAllUsersExceptOwnUser(int userId, Integer page, Integer cantPerPage, Map<String, String> parameters) {
    // Genera el WHERE dinámicamente
    StringBuffer where = new StringBuffer(" WHERE 1=1 AND e.id != :userId");

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