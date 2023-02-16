package stateless;

import java.util.Calendar;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import model.PlantingRecordStatus;

@Stateless
public class PlantingRecordStatusServiceBean {

  @PersistenceContext(unitName = "swcar")
  protected EntityManager entityManager;

  public void setEntityManager(EntityManager emLocal) {
    entityManager = emLocal;
  }

  public EntityManager getEntityManager() {
    return entityManager;
  }

  /**
   * @param id
   * @return referencia a un objeto de tipo PlantingRecordStatus
   * correspondiente al ID dado
   */
  public PlantingRecordStatus find(int id) {
    return getEntityManager().find(PlantingRecordStatus.class, id);
  }

  /**
   * @return referencia a un objeto de tipo PlantingRecordStatus que
   * representa el estado "Finalizado"
   */
  public PlantingRecordStatus findFinished() {
    Query query = getEntityManager().createQuery("SELECT p FROM PlantingRecordStatus p WHERE p.name = 'Finalizado'");
    return (PlantingRecordStatus) query.getSingleResult();
  }

  /**
   * @return referencia a un objeto de tipo PlantingRecordStatus que
   * representa el estado "En desarrollo"
   */
  public PlantingRecordStatus findDevelopmentStatus() {
    Query query = getEntityManager().createQuery("SELECT p FROM PlantingRecordStatus p WHERE p.name = 'En desarrollo'");
    return (PlantingRecordStatus) query.getSingleResult();
  }

  /**
   * Calcula el estado de un registro de plantacion en base a su fecha
   * de cosecha. Esto es que calcula el estado para un cultivo sembrado
   * en una parcela en base a su fecha de cosecha.
   * 
   * @param harvestDate
   * @return una referencia a un objeto de tipo PlantingRecordStatus
   * que representa el estado "En desarrollo" si la fecha de cosecha
   * esta estrictamente despues de la fecha actual o una referencia
   * a un objeto de tipo PlantingRecordStatus que representa el
   * estado "Finalizado" si la fecha de cosecha es anterior o igual
   * a la fecha actual
   */
  public PlantingRecordStatus calculateStatus(Calendar harvestDate) {
    Calendar currentDate = Calendar.getInstance();

    /*
     * Si la fecha de cosecha es estrictamente mayor que
     * la fecha actual del sistema, se retorna el estado
     * "En desarrollo". El "estrictamente mayor" es por
     * lo que dice la documentacion del metodo after de
     * la clase Calendar del JDK
     */
    if (harvestDate.after(currentDate)) {
      return findDevelopmentStatus();
    }

    /*
     * Si la fecha de cosecha no es estricamente mayor que
     * la fecha actual, significa que o es anterior o es
     * igual a la fecha actual, por lo tanto, se retorna
     * el estado "Finalizado"
     */
    return findFinished();
  }

}
