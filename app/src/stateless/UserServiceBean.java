package stateless;

import java.util.Collection;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import model.User;

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
   * Elimina logicamente un usuario de la base de datos subyacente
   * 
   * @param id
   * @return referencia a un objeto de tipo User con su variable
   * de instancia "active" en false en caso de encontrarse en la
   * base de datos subyacente el usuario con el ID dado, en caso
   * contrario null
   */
  public User delete(int id) {
    User givenUser = find(id);

    if (givenUser != null) {
      givenUser.setActive(false);
      return givenUser;
    }

    return null;
  }

  /**
   * Elimina fisicamente un usuario de la base de datos subyacente
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
    Query query = getEntityManager().createQuery("SELECT u FROM User u WHERE u.username != :username");
    query.setParameter("username", "admin");

    return (Collection) query.getResultList();
  }

}
