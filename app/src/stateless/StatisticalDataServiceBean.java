package stateless;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.Collection;
import model.StatisticalData;

@Stateless
public class StatisticalDataServiceBean {

  @PersistenceContext(unitName = "swcar")
  private EntityManager entityManager;

  public void setEntityManager(EntityManager localEntityManager) {
    entityManager = localEntityManager;
  }

  public EntityManager getEntityManager() {
    return entityManager;
  }

  public StatisticalData find(int id) {
    return getEntityManager().find(StatisticalData.class, id);
  }

  /**
   * @return referencia a un objeto de tipo Collection que
   * contiene todos los datos estadisticos a partir de los
   * cuales se generan graficos estadisticos
   */
  public Collection<StatisticalData> findAll() {
    Query query = entityManager.createQuery("SELECT s FROM StatisticalData s");
    return (Collection) query.getResultList();
  }

}
