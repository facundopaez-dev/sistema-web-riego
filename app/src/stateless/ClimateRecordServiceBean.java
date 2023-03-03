package stateless;

import java.util.Calendar;
import java.util.Collection;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import model.ClimateRecord;
import model.Parcel;

@Stateless
public class ClimateRecordServiceBean {

  /*
   * Instance variables
   */
  @PersistenceContext(unitName = "swcar")
  private EntityManager entityManager;

  public void setEntityManager(EntityManager localEntityManager) {
    entityManager = localEntityManager;
  }

  public EntityManager getEntityManager() {
    return entityManager;
  }

  public ClimateRecord create(ClimateRecord newClimateRecord) {
    getEntityManager().persist(newClimateRecord);
    return newClimateRecord;
  }

  /**
   * Retorna un registro climatico perteneciente a una de las
   * parcelas de un usuario
   * 
   * @param userId
   * @param climateRecordId
   * @return referencia a un objeto de tipo ClimateRecord perteneciente
   * a una parcela del usuario con el ID dado
   */
  public ClimateRecord find(int userId, int climateRecordId) {
    Query query = getEntityManager().createQuery("SELECT c FROM ClimateRecord c JOIN c.parcel p WHERE (c.id = :climateRecordId AND p.user.id = :userId)");
    query.setParameter("climateRecordId", climateRecordId);
    query.setParameter("userId", userId);

    return (ClimateRecord) query.getSingleResult();
  }

  /**
   * Retorna los registros climaticos de las parcelas de un
   * usuario
   * 
   * @param userId
   * @return referencia a un objeto de tipo Collection que
   * contiene los registros climaticos de las parcelas
   * pertenecientes al usuario con el ID dado
   */
  public Collection<ClimateRecord> findAll(int userId) {
    Query query = getEntityManager().createQuery("SELECT c FROM ClimateRecord c JOIN c.parcel p WHERE (p.user.id = :userId) ORDER BY c.id");
    query.setParameter("userId", userId);

    return (Collection) query.getResultList();
  }

  /**
   * Modifica un registro climatico perteneciente a una parcela
   * de un usuario
   *
   * @param userId
   * @param climateRecordId
   * @param modifiedClimateRecord
   * @return referencia a un objeto de tipo ClimateRecord si el registro climatico
   * a modificar pertenece a una parcela de un usuario con el ID dado, null en caso
   * contrario
   */
  public ClimateRecord modify(int userId, int climateRecordId, ClimateRecord modifiedClimateRecord) {
    ClimateRecord chosenClimateRecord = find(userId, climateRecordId);

    if (chosenClimateRecord != null) {
      chosenClimateRecord.setDate(modifiedClimateRecord.getDate());
      chosenClimateRecord.setPrecip(modifiedClimateRecord.getPrecip());
      chosenClimateRecord.setPrecipProbability(modifiedClimateRecord.getPrecipProbability());
      chosenClimateRecord.setDewPoint(modifiedClimateRecord.getDewPoint());
      chosenClimateRecord.setAtmosphericPressure(modifiedClimateRecord.getAtmosphericPressure());
      chosenClimateRecord.setWindSpeed(modifiedClimateRecord.getWindSpeed());
      chosenClimateRecord.setCloudCover(modifiedClimateRecord.getCloudCover());
      chosenClimateRecord.setMinimumTemperature(modifiedClimateRecord.getMinimumTemperature());
      chosenClimateRecord.setMaximumTemperature(modifiedClimateRecord.getMaximumTemperature());
      chosenClimateRecord.setWaterAccumulated(modifiedClimateRecord.getWaterAccumulated());
      chosenClimateRecord.setEto(modifiedClimateRecord.getEto());
      chosenClimateRecord.setEtc(modifiedClimateRecord.getEtc());
      chosenClimateRecord.setParcel(modifiedClimateRecord.getParcel());
      return chosenClimateRecord;
    }

    return null;
  }

  /**
   * Comprueba si un registro climatico pertenece a un usuario
   * dado, mediante la relacion muchos a uno que hay entre los
   * modelos de datos ClimateRecord y Parcel.
   * 
   * Retorna true si y solo si el registro climatico pertenece
   * al usuario dado.
   * 
   * @param userId
   * @param climateRecordId
   * @return true si se encuentra el registro climatico con el
   * ID y el ID de usuario provistos, false en caso contrario
   */
  public boolean checkUserOwnership(int userId, int climateRecordId) {
    boolean result = false;

    try {
      find(userId, climateRecordId);
      result = true;
    } catch (NoResultException e) {
      e.printStackTrace();
    }

    return result;
  }

  /**
   * Comprueba la existencia de un registro climatico en la base
   * de datos subyacente. Retorna true si y solo si existe el
   * registro climatico con el ID dado.
   * 
   * @param id
   * @return true si el registro climatico con el ID dado existe
   * en la base de datos subyacente, false en caso contrario
   */
  public boolean checkExistence(int id) {
    return (getEntityManager().find(ClimateRecord.class, id) != null);
  }

  /**
   * @param  givenDate
   * @param  givenParcel
   * @return registro climatico de la parcela dada en
   * la fecha dada
   */
  public ClimateRecord find(Calendar givenDate, Parcel givenParcel) {
    Query query = entityManager.createQuery("SELECT c FROM ClimateRecord c WHERE c.date = :date AND c.parcel = :parcel");
    query.setParameter("date", givenDate);
    query.setParameter("parcel", givenParcel);

    return (ClimateRecord) query.getSingleResult();
  }

  /**
   * Comprueba si la parcela dada tiene un registro
   * climatico asociado (en la base de datos) y si lo
   * tiene retorna verdadero, en caso contrario retorna
   * falso
   *
   * @param  date
   * @param  parcel
   * @return verdadero en caso de enccontrar un registro del clima
   * con la fecha y la parcela dadas, en caso contrario retorna falso
   */
  public boolean exist(Calendar date, Parcel parcel) {
    Query query = entityManager.createQuery("SELECT c FROM ClimateRecord c WHERE (c.date = :date AND c.parcel = :parcel)");
    query.setParameter("date", date);
    query.setParameter("parcel", parcel);

    boolean result = false;

    try {
      query.getSingleResult();
      result = true;
    } catch(NoResultException e) {
      e.printStackTrace();
    }

    return result;
  }

  /**
   * Establece el agua acumulada del registro climatico
   * del dia de hoy de una parcela dada
   *
   * @param givenDate
   * @param givenParcel
   * @param waterAccumulated [milimetros]
   */
  public void updateWaterAccumulatedToday(Calendar givenDate, Parcel givenParcel, double waterAccumulated) {
    Query query = entityManager.createQuery("UPDATE ClimateRecord c SET c.waterAccumulated = :waterAccumulated WHERE (c.date = :givenDate AND c.parcel = :givenParcel)");
    query.setParameter("givenDate", givenDate);
    query.setParameter("givenParcel", givenParcel);
    query.setParameter("waterAccumulated", waterAccumulated);
    query.executeUpdate();
  }

}
