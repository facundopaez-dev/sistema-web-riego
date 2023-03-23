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
import java.lang.NullPointerException;

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
   * Elimina un registro climatico fisicamente mediante su ID
   * 
   * @param id
   * @return referencia a un objeto de tipo ClimateRecord en
   * caso de eliminarse de la base de datos subyacente el
   * registro climatico con el ID dado, en caso contrario
   * null
   */
  public ClimateRecord remove(int id) {
    ClimateRecord givenClimateRecord = find(id);

    if (givenClimateRecord != null) {
      getEntityManager().remove(givenClimateRecord);
      return givenClimateRecord;
    }

    return null;
  }

  public ClimateRecord find(int id) {
    return getEntityManager().find(ClimateRecord.class, id);
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
   * Retorna todos los registros climaticos de una parcela
   * de un usuario
   * 
   * @param userId
   * @param parcelId
   * @return referencia a un objeto de tipo Collection que
   * contiene los registros climaticos de una parcela de un
   * usuario
   */
  public Collection<ClimateRecord> findAllByParcelId(int userId, int parcelId) {
    Query query = getEntityManager().createQuery("SELECT c FROM ClimateRecord c JOIN c.parcel p WHERE (p.id = :givenParcelId AND p.user.id = :givenUserId) ORDER BY c.id");
    query.setParameter("givenUserId", userId);
    query.setParameter("givenParcelId", parcelId);

    return (Collection) query.getResultList();
  }

  /**
   * Retorna todos los registros climaticos de una parcela
   * de un usuario que estan en un periodo definido por dos
   * fechas
   * 
   * @param userId
   * @param parcelId
   * @param dateFrom
   * @param dateUntil
   * @return referencia a un objeto de tipo Collection que
   * contiene los registros climaticos de una parcela de un
   * usuario que estan en un periodo definido por dos fechas
   */
  public Collection<ClimateRecord> findAllByParcelIdAndPeriod(int userId, int parcelId, Calendar dateFrom, Calendar dateUntil) {
    Query query = getEntityManager().createQuery("SELECT c FROM ClimateRecord c JOIN c.parcel p WHERE (p.id = :givenParcelId AND p.user.id = :givenUserId AND :givenDateFrom <= c.date AND c.date <= :givenDateUntil) ORDER BY c.id");
    query.setParameter("givenUserId", userId);
    query.setParameter("givenParcelId", parcelId);
    query.setParameter("givenDateFrom", dateFrom);
    query.setParameter("givenDateUntil", dateUntil);

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

  /**
   * Calcula la cantidad total de agua de lluvia [mm/periodo]
   * que cayo sobre una parcela en un periodo definido por dos
   * fechas si y solo si una parcela tiene registros climaticos
   * en el periodo en el que se quiere calcular dicha cantidad.
   * 
   * @param parcelId
   * @param dateFrom
   * @param dateUntil
   * @return cantidad total de agua de lluvia que cayo sobre
   * una parcela en un periodo definido por dos fechas, si
   * una parcela tiene registros climaticos en el periodo en
   * el que se quiere obtener dicha cantidad. En caso contrario,
   * -1.0, valor que representa informacion no disponible.
   */
  public double calculateAmountRainwaterByPeriod(int parcelId, Calendar dateFrom, Calendar dateUntil) {
    /*
     * Con esta condicion, la consulta selecciona todos los
     * registros climaticos de una parcela que estan
     * comprendidos en un periodo definido por dos fechas en
     * los que esta la lluvia como una de las precipitaciones
     */
    String conditionWhere = "(c.parcel.id = :givenParcelId AND :givenDateFrom <= c.date AND c.date <= :givenDateUntil AND t.name = 'rain')";

    /*
     * Suma la cantidad de agua de lluvia de cada uno de los
     * registros climaticos de una parcela que estan comprendidos
     * en un periodo definido por dos fechas, obteniendo la
     * cantidad total de agua de lluvia que cayo sobre una
     * parcela en un periodo dado
     */
    Query query = entityManager.createQuery("SELECT SUM(c.precip) FROM TypePrecipitation t JOIN t.climateRecord c WHERE " + conditionWhere);
    query.setParameter("givenParcelId", parcelId);
    query.setParameter("givenDateFrom", dateFrom);
    query.setParameter("givenDateUntil", dateUntil);

    double amountRainwater = -1.0;

    try {
      /*
       * Si se realiza la consulta JPQL de este metodo en SQL
       * con una parcela que no tiene ningun registro climatico
       * asociado en un periodo definido por dos fechas, se
       * observara que el valor devuelto es NULL. Por lo tanto,
       * es necesario contemplar este caso en el codigo fuente
       * de este metodo.
       * 
       * En caso de que se solicite la cantidad total de agua
       * de lluvia que cayo sobre una parcela en un periodo
       * definido por dos fechas para una parcela que no
       * tiene ningun registro climatico asociado en un
       * periodo dado, se retorna el valor -1.0, el cual
       * indica informacion no disponible.
       */
      amountRainwater = (double) query.getSingleResult();
    } catch (NullPointerException e) {
      e.printStackTrace();
    }

    return amountRainwater;
  }

  /**
   * Retorna true si y solo si una parcela de un usuario
   * tiene registros climaticos
   * 
   * @param userId
   * @param parcelId
   * @return true si una parcela tiene registros climaticos,
   * en caso contrario false
   */
  public boolean hasClimateRecords(int userId, int parcelId) {
    return !findAllByParcelId(userId, parcelId).isEmpty();
  }

  /**
   * Retorna true si y solo si una parcela de un usuario
   * tiene registros climaticos en un periodo definido por
   * dos fechas
   * 
   * @param userId
   * @param parcelId
   * @param dateFrom
   * @param dateUntil
   * @return true si una parcela tiene registros climaticos,
   * en caso contrario false
   */
  public boolean hasClimateRecords(int userId, int parcelId, Calendar dateFrom, Calendar dateUntil) {
    return !findAllByParcelIdAndPeriod(userId, parcelId, dateFrom, dateUntil).isEmpty();
  }

}
