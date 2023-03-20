package stateless;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
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

}
