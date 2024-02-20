package stateless;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.Collection;
import model.TypePrecipitation;

@Stateless
public class TypePrecipitationServiceBean {

  @PersistenceContext(unitName = "swcar")
  private EntityManager entityManager;

  public void setEntityManager(EntityManager localEntityManager) {
    entityManager = localEntityManager;
  }

  public EntityManager getEntityManager() {
    return entityManager;
  }

  /**
   * Elimina un tipo de precipitacion fisicamente mediante su ID
   * 
   * @param id
   * @return referencia a un objeto de tipo TypePrecipitation en
   * caso de eliminarse de la base de datos subyacente el
   * tipo de precipitacion con el ID dado, en caso contrario
   * null
   */
  public TypePrecipitation remove(int id) {
    TypePrecipitation givenTypePrecipitation = find(id);

    if (givenTypePrecipitation != null) {
      getEntityManager().remove(givenTypePrecipitation);
      return givenTypePrecipitation;
    }

    return null;
  }

  public TypePrecipitation find(int id) {
    return getEntityManager().find(TypePrecipitation.class, id);
  }

  /**
   * @return entero que representa el ID del tipo de precipitacion
   * lluvia
   */
  public int findRainId() {
    Query query = entityManager.createQuery("SELECT t.id FROM TypePrecipitation t WHERE UPPER(t.name) = UPPER('rain')");
    return (int) query.getSingleResult();
  }

  /**
   * @return referencia a un objeto de tipo Collection que
   * contiene todos los tipos de precipitacion registrados
   * en la base de datos subyacente
   */
  public Collection<TypePrecipitation> findAll() {
    Query query = entityManager.createQuery("SELECT t FROM TypePrecipitation t");
    return (Collection) query.getResultList();
  }

}
