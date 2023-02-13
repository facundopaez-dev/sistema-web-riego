package stateless;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import model.SecretKey;

@Stateless
public class SecretKeyServiceBean {

  private final int SECRET_KEY_ID = 1;

  @PersistenceContext(unitName = "swcar")
  protected EntityManager em;

  public void setEntityManager(EntityManager emLocal) {
    em = emLocal;
  }

  public EntityManager getEntityManager() {
    return em;
  }

  /**
   * Recupera de la base de datos subyacente la unica clave secreta
   * que hay para crear un JWT.
   * 
   * Es importante tener en cuenta que el ID de esta clave tiene que ser 1
   * en la base de datos subyacente, ya que de lo contrario la invocacion
   * a este metodo fallara.
   * 
   * @return referencia a un objeto de tipo SecretKey, el cual contiene
   * la clave secreta con la cual se firma un JWT
   */
  public SecretKey find() {
    return getEntityManager().find(SecretKey.class, SECRET_KEY_ID);
  }

}
