package stateless;

import java.util.Calendar;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import model.PlantingRecord;
import model.PlantingRecordStatus;
import util.UtilDate;

@Stateless
public class PlantingRecordStatusServiceBean {

  @PersistenceContext(unitName = "swcar")
  protected EntityManager entityManager;

  public void setEntityManager(EntityManager localEntityManager) {
    entityManager = localEntityManager;
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
  public PlantingRecordStatus findFinishedStatus() {
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
   * @return referencia a un objeto de tipo PlantingRecordStatus que
   * representa el estado "En espera"
   */
  public PlantingRecordStatus findWaitingStatus() {
    Query query = getEntityManager().createQuery("SELECT p FROM PlantingRecordStatus p WHERE p.name = 'En espera'");
    return (PlantingRecordStatus) query.getSingleResult();
  }

  /**
   * @return referencia a un objeto de tipo PlantingRecordStatus que
   * representa el estado "Marchitado"
   */
  public PlantingRecordStatus findWitheredStatus() {
    Query query = getEntityManager().createQuery("SELECT p FROM PlantingRecordStatus p WHERE p.name = 'Marchitado'");
    return (PlantingRecordStatus) query.getSingleResult();
  }

  /**
   * Calcula el estado de un registro de plantacion en base a su
   * fecha de siembra y su fecha de cosecha. Un registro de plantacion
   * representa la siembra de un cultivo. Por lo tanto, este metodo
   * calcula el estado de un cultivo sembrado en una parcela en base
   * a su fecha de siembra y su fecha de cosecha.
   * 
   * @param givenPlantingRecord
   * @return una referencia a un objeto de tipo PlantingRecordStatus
   * que representa el estado "Finalizado" si la fecha de cosecha de
   * un registro de plantacion es estrictamente menor a la fecha actual,
   * que representa el estado "En desarrollo" si la fecha actual esta
   * entre la fecha de siembra y la fecha de cosecha de un registro de
   * plantacion o que representa el estado "En espera" si la fecha de
   * siembra de un registro de plantacion es estrictamente mayor a la
   * fecha actual
   */
  public PlantingRecordStatus calculateStatus(PlantingRecord givenPlantingRecord) {
    Calendar currentDate = UtilDate.getCurrentDate();
    Calendar seedDate = givenPlantingRecord.getSeedDate();
    Calendar harvestDate = givenPlantingRecord.getHarvestDate();

    /*
     * Si la fecha de cosecha de un registro de plantacion
     * es estrictamente menor a la fecha actual, el estado
     * de un registro de plantacion es "Finalizado"
     */
    if (UtilDate.compareTo(harvestDate, currentDate) < 0) {
      return findFinishedStatus();
    }

    /*
     * Si la fecha actual es mayor o igual a la fecha de
     * siembra y es menor o igual a la fecha de cosecha
     * de un registro de plantacion, el estado de un
     * registro de plantacion es "En desarrollo"
     */
    if ((UtilDate.compareTo(currentDate, seedDate) >= 0) && (UtilDate.compareTo(currentDate, harvestDate) <= 0)) {
      return findDevelopmentStatus();
    }

    /*
     * Si la fecha de siembra de un registro de plantacion
     * es estrictamente mayor a la fecha actual, el estado
     * de un registro de plantacion es "En espera"
     */
    return findWaitingStatus();
  }

  /**
   * @param statusOne
   * @param statusTwo
   * @return true si el estado uno es igual al estado dos, en
   * caso contrario false
   */
  public boolean equals(PlantingRecordStatus statusOne, PlantingRecordStatus statusTwo) {
    return statusOne.getName().equals(statusTwo.getName());
  }

}
