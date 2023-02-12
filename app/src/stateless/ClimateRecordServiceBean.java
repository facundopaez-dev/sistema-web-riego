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
public  class ClimateRecordServiceBean {

  /*
   * Instance variables
   */
  @PersistenceContext(unitName="swcar")
  private EntityManager entityManager;

  public void setEntityManager(EntityManager localEntityManager){
    entityManager = localEntityManager;
  }

  public EntityManager getEntityManager() {
    return entityManager;
  }

  public ClimateRecord create(ClimateRecord newClimateRecord) {
    getEntityManager().persist(newClimateRecord);
    return newClimateRecord;
  }

  public ClimateRecord find(int id) {
    return getEntityManager().find(ClimateRecord.class, id);
  }

  public Collection<ClimateRecord> findAll() {
    Query query = getEntityManager().createQuery("SELECT c FROM ClimateRecord c ORDER BY c.id");
    return (Collection) query.getResultList();
  }

  /**
   * Modifica los datos de un registro climatico en la base de
   * datos subyacente
   * 
   * @param id
   * @param modifiedClimateRecord
   * @return referencia a un objeto de tipo ClimateRecord que tiene
   * sus datos modificados en caso de encontrarse en la base de datos
   * subyacente el registro climatico con el ID dado, en caso contrario
   * null
   */
  public ClimateRecord modify(int id, ClimateRecord modifiedClimateRecord) {
    ClimateRecord choosenClimateRecord = find(id);

    if (choosenClimateRecord != null) {
      choosenClimateRecord.setDate(modifiedClimateRecord.getDate());
      choosenClimateRecord.setTimezone(modifiedClimateRecord.getTimezone());
      choosenClimateRecord.setPrecipIntensity(modifiedClimateRecord.getPrecipIntensity());
      choosenClimateRecord.setPrecipProbability(modifiedClimateRecord.getPrecipProbability());
      choosenClimateRecord.setDewPoint(modifiedClimateRecord.getDewPoint());
      choosenClimateRecord.setPressure(modifiedClimateRecord.getPressure());
      choosenClimateRecord.setWindSpeed(modifiedClimateRecord.getWindSpeed());
      choosenClimateRecord.setCloudCover(modifiedClimateRecord.getCloudCover());
      choosenClimateRecord.setTemperatureMin(modifiedClimateRecord.getTemperatureMin());
      choosenClimateRecord.setTemperatureMax(modifiedClimateRecord.getTemperatureMax());
      choosenClimateRecord.setRainWater(modifiedClimateRecord.getRainWater());
      choosenClimateRecord.setWaterAccumulated(modifiedClimateRecord.getWaterAccumulated());
      choosenClimateRecord.setEto(modifiedClimateRecord.getEto());
      choosenClimateRecord.setEtc(modifiedClimateRecord.getEtc());
      choosenClimateRecord.setParcel(modifiedClimateRecord.getParcel());
      return choosenClimateRecord;
    }

    return null;
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