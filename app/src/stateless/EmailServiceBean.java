package stateless;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import model.Email;
import model.User;

@Stateless
public class EmailServiceBean {

  @PersistenceContext(unitName = "swcar")
  protected EntityManager entityManager;

  public void setEntityManager(EntityManager localEntityManager) {
    entityManager = localEntityManager;
  }

  public EntityManager getEntityManager() {
    return entityManager;
  }

  public Email create(Email newEmail) {
    getEntityManager().persist(newEmail);
    return newEmail;
  }

  /**
   * Elimina la dirección de correo electrónico de un
   * usuario utilizando su ID de usuario
   * 
   * @param userId
   */
  public void removeByUserId(int userId) {
    Query query = entityManager.createQuery("DELETE FROM Email e WHERE e.user.id = :userId");
    query.setParameter("userId", userId);
    query.executeUpdate();
  }

  /**
   * Modifica la direccion de un correo electronico en la base
   * de datos subyacente
   * 
   * @param id
   * @param modifiedEmail
   * @return referencia a un objeto de tipo Email que tiene su
   * direccion modificada en caso de encontrarse en la base de
   * datos subyacente el correo electronico con el ID dado, en
   * caso contrario null
   */
  public Email modify(int id, Email modifiedEmail) {
    Email chosenEmail = find(id);

    if (chosenEmail != null) {
      chosenEmail.setAddress(modifiedEmail.getAddress());
      return chosenEmail;
    }

    return null;
  }

  public Email find(int id) {
    return getEntityManager().find(Email.class, id);
  }

  /**
   * @param userId
   * @return int que representa el ID de un correo electronico
   * correspondiente al usuario con el ID provisto
   */
  public int findIdEmailByUserId(int userId) {
    Query query = getEntityManager().createQuery("SELECT e.id FROM Email e WHERE e.user.id = :userId");
    query.setParameter("userId", userId);

    return (int) query.getSingleResult();
  }

  /**
   * Busca un correo electronico en la base de datos subyacente mediante
   * un ID de usuario
   * 
   * @param userId
   * @return referencia a un objeto de tipo Email en caso de encontrarse
   * en la base de datos subyacente, el correo electronico asociado al ID
   * de usuario provisto, null en caso contrario
   */
  public Email findEmailByUserId(int userId) {
    Query query = getEntityManager().createQuery("SELECT e FROM Email e WHERE e.user.id = :userId");
    query.setParameter("userId", userId);

    Email email = null;

    try {
      email = (Email) query.getSingleResult();
    } catch (NoResultException e) {
      e.printStackTrace();
    }

    return email;
  }

  /**
   * Busca un usuario en la base de datos subyacente mediante una
   * direccion de correo electronico
   * 
   * @param email
   * @return referencia a un objeto de tipo User en caso de encontrarse
   * en la base de datos subyacente, el usuario con la direccion
   * de correo electronico provista, en caso contrario null
   */
  public User findUserByAddress(String email) {
    Query query = getEntityManager().createQuery("SELECT u FROM Email e JOIN e.user u WHERE UPPER(e.address) = UPPER(:email)");
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
   * Retorna una referencia a un objeto de tipo User si y solo si la
   * direccion de correo electronico existe dentro del conjunto de
   * usuarios en el que NO esta el usuario del ID dado. El motivo por
   * el cual se hace esta descripcion de esta manera es que un usuario
   * tiene una direccion de correo electronico.
   * 
   * Este metodo es para cuando el usuario que tiene el ID dado modifica
   * su direccion de correo electronico. Lo que se busca con este metodo
   * es verificar que la direccion de correo electronica modificada NO
   * este registrada en la cuenta de otro usuario. Si la direccion de
   * correo electronico modificada NO esta registrada en la cuenta de otro
   * usuario, la aplicacion realiza la modificacion de la direccion de
   * correo electronico. En caso contrario, no la realiza.
   * 
   * @param userId
   * @param username
   * @return referencia a un objeto de tipo User si el nombre de usuario
   * existe dentro del conjunto de usuarios en el que NO esta el
   * usuario del ID dado, en caso contrario null
   */
  public User findUserByAddress(int userId, String email) {
    Query query = getEntityManager().createQuery("SELECT u FROM Email e JOIN e.user u WHERE (u.id != :userId AND UPPER(e.address) = UPPER(:email))");
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

    return (findUserByAddress(email) != null);
  }

  /**
   * Retorna true si y solo si la direccion de correo electronico esta
   * dentro del conjunto de usuarios en el que NO esta el usuario del
   * ID dado. El motivo por el cual se hace esta descripcion de esta
   * manera es que un usuario tiene una direccion de correo electronico.
   * 
   * 
   * Este metodo es para cuando el usuario que tiene el ID dado modifica
   * su direccion de correo electronico. Lo que se busca con este metodo
   * es verificar que la direccion de correo electronica modificada NO
   * este registrada en la cuenta de otro usuario. Si la direccion de
   * correo electronico modificada NO esta registrada en la cuenta de otro
   * usuario, la aplicacion realiza la modificacion de la direccion de
   * correo electronico. En caso contrario, no la realiza.
   * 
   * @param userId
   * @param email
   * @return true si la direccion de correo electronico existe dentro
   * del conjunto de usuarios en el que NO esta el usuario del
   * ID dado, en caso contrario false
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

    return (findUserByAddress(userId, email) != null);
  }

}
