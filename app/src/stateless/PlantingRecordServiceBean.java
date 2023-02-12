package stateless;

import java.util.Calendar;
import java.util.Collection;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import model.Crop;
import model.PlantingRecord;
import model.Parcel;

@Stateless
public class PlantingRecordServiceBean {

  @PersistenceContext(unitName="swcar")
  protected EntityManager entityManager;

  public void setEntityManager(EntityManager emLocal){
    entityManager = emLocal;
  }

  public EntityManager getEntityManager() {
    return entityManager;
  }

  /**
   * Persiste un registro de plantacion en la base de datos subyacente
   * 
   * @param newPlantingRecord
   * @return referencia a un objeto de tipo PlantingRecord persistido
   * en la base de datos subyacente
   */
  public PlantingRecord create(PlantingRecord newPlantingRecord){
    getEntityManager().persist(newPlantingRecord);
    return newPlantingRecord;
  }

  /**
   * Elimina un registro de plantacion de una parcela mediante un
   * cultivo
   * 
   * TODO: ¿Cual registro de plantacion elimina sabiendo que mas
   * de un registro de plantacion puede tener la misma parcela y
   * el mismo cultivo?
   * 
   * @param givenParcel
   * @param givenCrop
   * @return referencia a un objeto de tipo PlantingRecord en caso
   * de encontrarse en la base de datos subyacente el registro
   * de plantacion correspondiente a la parcela y al cultivo dados
   */
  public PlantingRecord remove(Parcel givenParcel, Crop givenCrop){
    PlantingRecord givenPlantingRecord = find(givenParcel, givenCrop);

    if (givenPlantingRecord != null) {
      getEntityManager().remove(givenPlantingRecord);
      return givenPlantingRecord;
    }

    return null;
  }

  /**
   * Elimina un registro de plantacion mediante su ID
   * 
   * @param id
   * @return referencia a un objeto de tipo PlantingRecord en caso
   * de encontrarse en la base de datos subyacente el registro
   * de plantacion con el ID dado
   */
  public PlantingRecord remove(int id){
    PlantingRecord givenPlantingRecord = find(id);

    if (givenPlantingRecord != null) {
      getEntityManager().remove(givenPlantingRecord);
      return givenPlantingRecord;
    }

    return null;
  }

  /**
   * Modifica un registro de plantacion en la base de datos subyacente
   * 
   * @param id
   * @param modifiedPlantingRecord
   * @return referencia a un objeto de tipo PlantingRecord que tiene
   * sus datos modificados en caso de encontrarse en el base de datos
   * subyacente el registro de plantacion con el ID dado, en caso
   * contrario null
   */
  public PlantingRecord modify(int id, PlantingRecord modifiedPlantingRecord){
    PlantingRecord ginvePlantingRecord = find(id);

    if (ginvePlantingRecord != null) {
      ginvePlantingRecord.setSeedDate(modifiedPlantingRecord.getSeedDate());
      ginvePlantingRecord.setHarvestDate(modifiedPlantingRecord.getHarvestDate());
      ginvePlantingRecord.setParcel(modifiedPlantingRecord.getParcel());
      ginvePlantingRecord.setCrop(modifiedPlantingRecord.getCrop());
      ginvePlantingRecord.setStatus(modifiedPlantingRecord.getStatus());
      return ginvePlantingRecord;
    }

    return null;
  }

  public PlantingRecord find(Parcel givenParcel, Crop givenCrop){
    Query query = entityManager.createQuery("SELECT e FROM PlantingRecord e where e.parcel = :givenParcel and e.crop = :givenCrop");
    query.setParameter("givenParcel", givenParcel);
    query.setParameter("givenCrop", givenCrop);

    PlantingRecord plantingRecord = null;

    try {
      plantingRecord = (PlantingRecord) query.getSingleResult();
    } catch(NoResultException e) {
      e.printStackTrace();
    }

    return plantingRecord;
  }

  public PlantingRecord find(int id){
    return getEntityManager().find(PlantingRecord.class, id);
  }

  public PlantingRecord find(Parcel givenParcel, int id) {
    Query query = getEntityManager().createQuery("SELECT i FROM PlantingRecord i WHERE i.parcel = :givenParcel AND i.id = :id");
    query.setParameter("givenParcel", givenParcel);
    query.setParameter("id", id);

    PlantingRecord plantingRecord = null;

    try {
      plantingRecord = (PlantingRecord) query.getSingleResult();
    } catch(NoResultException e) {
      e.printStackTrace();
    }

    return plantingRecord;
  }

  public Collection<PlantingRecord> findAll() {
    Query query = getEntityManager().createQuery("SELECT p FROM PlantingRecord p ORDER BY p.id");
    return (Collection) query.getResultList();
  }

  /**
   * Se considera registro historico actual de parcela a
   * aquel que esta en el estado "En desarrollo"
   *
   * Solo puede haber un unico registro historico de parcela
   * en el estado mencionado y esto es para cada parcela
   * existente en el sistema, con lo cual siempre deberia
   * haber un unico registro historico actual de parcela
   * para cada parcela existente en el sistema
   *
   * @param  givenParcel
   * @return registro historico de parcela actual, si hay uno
   * actual, en caso contrario retorna falso
   */
  public PlantingRecord findInDevelopment(Parcel givenParcel) {
    Query query = getEntityManager().createQuery("SELECT r FROM PlantingRecord r JOIN r.parcel p JOIN r.status s WHERE (s.name = 'En desarrollo' AND p = :parcel)");
    query.setParameter("parcel", givenParcel);

    PlantingRecord plantingRecord = null;

    try {
      plantingRecord = (PlantingRecord) query.getSingleResult();
    } catch(Exception e) {

    }

    return plantingRecord;
  }

  /**
   * 
   * @param givenParcel
   * @return referencia a un objeto de tipo PlantingRecord que
   * representa el ultimo registro de plantacion en el estado
   * "Finalizado" en caso de que exista, en caso contrario null
   */
  public PlantingRecord findLastFinished(Parcel givenParcel) {
    Query query = getEntityManager().createQuery("SELECT r FROM PlantingRecord r WHERE r.id = (SELECT MAX(r.id) FROM PlantingRecord r JOIN r.parcel p JOIN r.status s WHERE (s.name = 'Finalizado' AND p = :parcel))");
    query.setParameter("parcel", givenParcel);

    PlantingRecord plantingRecord = null;

    try {
      plantingRecord = (PlantingRecord) query.getSingleResult();
    } catch(Exception e) {
      e.printStackTrace();
    }

    return plantingRecord;
  }

}
