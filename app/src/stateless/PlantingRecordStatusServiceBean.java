package stateless;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import model.PlantingRecordStatus;

@Stateless
public class PlantingRecordStatusServiceBean {

  @PersistenceContext(unitName="swcar")
  protected EntityManager entityManager;

  public void setEntityManager(EntityManager emLocal){
    entityManager = emLocal;
  }

  public EntityManager getEntityManager() {
    return entityManager;
  }

  /**
   * 
   * @param id
   * @return referencia a un objeto de tipo PlantingRecordStatus
   * correspondiente al ID dado
   */
  public PlantingRecordStatus find(int id){
    return getEntityManager().find(PlantingRecordStatus.class, id);
  }

}
