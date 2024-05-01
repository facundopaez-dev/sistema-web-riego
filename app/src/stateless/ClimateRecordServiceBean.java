package stateless;

import java.text.SimpleDateFormat;
import java.text.ParseException;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.lang.NullPointerException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.Calendar;
import java.util.Date;
import model.ClimateRecord;
import model.Parcel;
import model.TypePrecipitation;
import util.UtilDate;
import climate.ClimateClient;

@Stateless
public class ClimateRecordServiceBean {

  /*
   * Instance variables
   */
  @PersistenceContext(unitName = "swcar")
  private EntityManager entityManager;

  /*
   * El valor de esta constante se utiliza:
   * - para obtener y persistir los registros climaticos de una parcela
   * anteriores a la fecha actual.
   * - para calcular la ETo y la ETc de cada uno de los registros
   * climaticos de una parcela anteriores a la fecha actual.
   */
  private final int NUMBER_DAYS = 3;

  public int getNumberDays() {
    return NUMBER_DAYS;
  }

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
   * Elimina fisicamente un registro climatico perteneciente a
   * una parcela de un usuario
   * 
   * @param userId
   * @param climateRecordId
   * @return referencia a un objeto de tipo ClimateRecord en
   * caso de eliminarse de la base de datos subyacente el registro
   * climatico que tiene el ID dado y que esta asociado a una
   * parcela de un usuario que tiene el ID de usuario dado, en
   * caso contrario null
   */
  public ClimateRecord remove(int userId, int climateRecordId) {
    ClimateRecord givenClimateRecord = findByUserId(userId, climateRecordId);

    if (givenClimateRecord != null) {
      getEntityManager().remove(givenClimateRecord);
      return givenClimateRecord;
    }

    return null;
  }

  public ClimateRecord remove(int climateRecordId) {
    ClimateRecord givenClimateRecord = find(climateRecordId);

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
   * Actualiza el estado de una instancia de tipo ClimateRecord desde
   * la base de datos, sobrescribiendo los cambios realizados en la
   * entidad, si los hubiera.
   * 
   * @param climateRecord
   */
  public void refresh(ClimateRecord climateRecord) {
    getEntityManager().refresh(climateRecord);
  }

  /**
   * Retorna un registro climatico perteneciente a una de las
   * parcelas de un usuario si y solo si existe en la base
   * de datos subyacente
   * 
   * @param userId
   * @param climateRecordId
   * @return referencia a un objeto de tipo ClimateRecord que
   * representa un registro climatico en caso de existir en
   * la base de datos subyacente un registro climatico con
   * el ID dado asociado a una parcela del usuario del ID
   * dado. En caso contrario, null.
   */
  public ClimateRecord findByUserId(int userId, int climateRecordId) {
    Query query = getEntityManager().createQuery("SELECT c FROM ClimateRecord c JOIN c.parcel p WHERE (c.id = :climateRecordId AND p IN (SELECT t FROM User u JOIN u.parcels t WHERE u.id = :userId))");
    query.setParameter("userId", userId);
    query.setParameter("climateRecordId", climateRecordId);

    ClimateRecord climateRecord = null;

    try {
      climateRecord = (ClimateRecord) query.getSingleResult();
    } catch(NoResultException e) {
      e.printStackTrace();
    }

    return climateRecord;
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
    Query query = getEntityManager().createQuery("SELECT c FROM ClimateRecord c JOIN c.parcel p WHERE p IN (SELECT t FROM User u JOIN u.parcels t WHERE u.id = :userId) ORDER BY c.id");
    query.setParameter("userId", userId);

    return (Collection) query.getResultList();
  }

  /**
   * Retorna los registros climaticos de una parcela mediante
   * el nombre de una parcela y el ID del usuario al que pertenece
   * una parcela
   * 
   * @param userId
   * @param parcelName
   * @return referencia a un objeto de tipo Collection que
   * contiene los registros climaticos de la parcela que tiene
   * el nombre dado y que pertenece al usuario con el ID dado
   */
  public Collection<ClimateRecord> findAllByParcelName(int userId, String parcelName) {
    Query query = getEntityManager().createQuery("SELECT c FROM ClimateRecord c JOIN c.parcel p WHERE (p.name = :parcelName AND p IN (SELECT t FROM User u JOIN u.parcels t WHERE u.id = :userId)) ORDER BY c.date");
    query.setParameter("userId", userId);
    query.setParameter("parcelName", parcelName);

    return (Collection) query.getResultList();
  }

  /**
   * Retorna los registros climaticos de una parcela mediante
   * el nombre de una parcela, una fecha y el ID del usuario
   * al que pertenece una parcela
   * 
   * @param userId
   * @param parcelName
   * @param date
   * @return referencia a un objeto de tipo Collection que
   * contiene los registros climaticos que tienen una fecha
   * pertenecientes a una parcela que tiene un nombre, la
   * cual pertenece a un usuario
   */
  public Collection<ClimateRecord> findAllByParcelNameAndDate(int userId, String parcelName, Calendar date) {
    Query query = getEntityManager().createQuery("SELECT c FROM ClimateRecord c JOIN c.parcel p WHERE (c.date = :date AND p.name = :parcelName AND p IN (SELECT t FROM User u JOIN u.parcels t WHERE u.id = :userId)) ORDER BY c.id");
    query.setParameter("userId", userId);
    query.setParameter("parcelName", parcelName);
    query.setParameter("date", date);

    return (Collection) query.getResultList();
  }

  /**
   * Actualiza el estado de instancias de tipo ClimateRecord desde
   * la base de datos, sobrescribiendo los cambios realizados en
   * cada una de ellas, si los hubiere
   * 
   * @param climateRecords
   */
  public void refreshClimateRecords(Collection<ClimateRecord> climateRecords) {

    for (ClimateRecord currentClimateRecord : climateRecords) {
      getEntityManager().refresh(getEntityManager().merge(currentClimateRecord));
    }

  }

  /**
   * Retorna todos los registros climaticos de una parcela de
   * un usuario, si una parcela tiene registros climaticos.
   * 
   * @param userId
   * @param parcelId
   * @return referencia a un objeto de tipo Collection que contiene
   * todos los registros climaticos de una parcela de un usuario. En
   * el caso en el que una parcela de un usuario no tiene ningun
   * registro climatico, referencia a un objeto de tipo Collection
   * vacio (0 elementos).
   */
  public Collection<ClimateRecord> findAllByParcelId(int userId, int parcelId) {
    Query query = getEntityManager().createQuery("SELECT c FROM ClimateRecord c JOIN c.parcel p WHERE (p.id = :parcelId AND p IN (SELECT t FROM User u JOIN u.parcels t WHERE u.id = :userId)) ORDER BY c.id");
    query.setParameter("userId", userId);
    query.setParameter("parcelId", parcelId);

    return (Collection) query.getResultList();
  }

  /**
   * Retorna todos los registros climaticos de una parcela de
   * un usuario que estan en un periodo definido por dos fechas,
   * si una parcela tiene registros climaticos en un periodo dado.
   * 
   * @param userId
   * @param parcelId
   * @param dateFrom
   * @param dateUntil
   * @return referencia a un objeto de tipo Collection que contiene
   * todos los registros climaticos de una parcela de un usuario que
   * estan en un periodo definido por dos fechas. En caso contrario,
   * referencia a un objeto de tipo Collection vacio (0 elementos).
   */
  public Collection<ClimateRecord> findAllByParcelIdAndPeriod(int userId, int parcelId, Calendar dateFrom, Calendar dateUntil) {
    Query query = getEntityManager().createQuery("SELECT c FROM ClimateRecord c JOIN c.parcel p WHERE (p.id = :parcelId AND p IN (SELECT t FROM User u JOIN u.parcels t WHERE u.id = :userId) AND :dateFrom <= c.date AND c.date <= :dateUntil) ORDER BY c.date");
    query.setParameter("userId", userId);
    query.setParameter("parcelId", parcelId);
    query.setParameter("dateFrom", dateFrom);
    query.setParameter("dateUntil", dateUntil);

    return (Collection) query.getResultList();
  }

  /**
   * Retorna todos los registros climaticos que tienen
   * una fecha dada
   * 
   * @param date
   * @return referencia a un objeto de tipo Collection que
   * contiene todos los registros climaticos de una fecha
   * dada
   */
  public Collection<ClimateRecord> findAllByDate(Calendar date) {
    Query query = entityManager.createQuery("SELECT c FROM ClimateRecord c WHERE c.date = :givenDate ORDER BY c.id");
    query.setParameter("givenDate", date);

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
    ClimateRecord chosenClimateRecord = findByUserId(userId, climateRecordId);

    if (chosenClimateRecord != null) {
      chosenClimateRecord.setDate(modifiedClimateRecord.getDate());
      chosenClimateRecord.setPrecip(modifiedClimateRecord.getPrecip());
      chosenClimateRecord.setPrecipProbability(modifiedClimateRecord.getPrecipProbability());
      chosenClimateRecord.setTypePrecipOne(modifiedClimateRecord.getTypePrecipOne());
      chosenClimateRecord.setTypePrecipTwo(modifiedClimateRecord.getTypePrecipTwo());
      chosenClimateRecord.setTypePrecipThree(modifiedClimateRecord.getTypePrecipThree());
      chosenClimateRecord.setTypePrecipFour(modifiedClimateRecord.getTypePrecipFour());
      chosenClimateRecord.setDewPoint(modifiedClimateRecord.getDewPoint());
      chosenClimateRecord.setAtmosphericPressure(modifiedClimateRecord.getAtmosphericPressure());
      chosenClimateRecord.setWindSpeed(modifiedClimateRecord.getWindSpeed());
      chosenClimateRecord.setCloudCover(modifiedClimateRecord.getCloudCover());
      chosenClimateRecord.setMinimumTemperature(modifiedClimateRecord.getMinimumTemperature());
      chosenClimateRecord.setMaximumTemperature(modifiedClimateRecord.getMaximumTemperature());
      chosenClimateRecord.setEto(modifiedClimateRecord.getEto());
      chosenClimateRecord.setEtc(modifiedClimateRecord.getEtc());
      chosenClimateRecord.setParcel(modifiedClimateRecord.getParcel());
      return chosenClimateRecord;
    }

    return null;
  }

  /**
   * Modifica un registro climatico
   *
   * @param climateRecordId
   * @param modifiedClimateRecord
   * @return referencia a un objeto de tipo ClimateRecord si el registro climatico
   * a modificar existe en la base de datos subyacente, en caso contrario null
   */
  public ClimateRecord modify(int climateRecordId, ClimateRecord modifiedClimateRecord) {
    ClimateRecord chosenClimateRecord = find(climateRecordId);

    if (chosenClimateRecord != null) {
      chosenClimateRecord.setDate(modifiedClimateRecord.getDate());
      chosenClimateRecord.setPrecip(modifiedClimateRecord.getPrecip());
      chosenClimateRecord.setPrecipProbability(modifiedClimateRecord.getPrecipProbability());
      chosenClimateRecord.setTypePrecipOne(modifiedClimateRecord.getTypePrecipOne());
      chosenClimateRecord.setTypePrecipTwo(modifiedClimateRecord.getTypePrecipTwo());
      chosenClimateRecord.setTypePrecipThree(modifiedClimateRecord.getTypePrecipThree());
      chosenClimateRecord.setTypePrecipFour(modifiedClimateRecord.getTypePrecipFour());
      chosenClimateRecord.setDewPoint(modifiedClimateRecord.getDewPoint());
      chosenClimateRecord.setAtmosphericPressure(modifiedClimateRecord.getAtmosphericPressure());
      chosenClimateRecord.setWindSpeed(modifiedClimateRecord.getWindSpeed());
      chosenClimateRecord.setCloudCover(modifiedClimateRecord.getCloudCover());
      chosenClimateRecord.setMinimumTemperature(modifiedClimateRecord.getMinimumTemperature());
      chosenClimateRecord.setMaximumTemperature(modifiedClimateRecord.getMaximumTemperature());
      chosenClimateRecord.setEto(modifiedClimateRecord.getEto());
      chosenClimateRecord.setEtc(modifiedClimateRecord.getEtc());
      chosenClimateRecord.setParcel(modifiedClimateRecord.getParcel());
      return chosenClimateRecord;
    }

    return null;
  }

  /**
   * Comprueba si un registro climatico pertenece a un usuario
   * dado, mediante la relacion uno a muchos que hay entre los
   * modelos de datos User y Parcel.
   * 
   * Retorna true si y solo si un registro climatico pertenece
   * a un usuario.
   * 
   * @param userId
   * @param climateRecordId
   * @return true si se encuentra el registro climatico con el
   * ID y el ID de usuario provistos, false en caso contrario
   */
  public boolean checkUserOwnership(int userId, int climateRecordId) {
    return (findByUserId(userId, climateRecordId) != null);
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
   * Recupera un registro climatico de una parcela mediante
   * una fecha si y solo si existe en la base de datos
   * subyacente
   * 
   * @param givenDate
   * @param givenParcel
   * @return referencia a un objeto de tipo ClimateRecord que
   * representa el registro climatico que tiene una fecha dada
   * y pertenece a una parcela, si existe en la base de datos
   * subyacente. En caso contrario, null.
   */
  public ClimateRecord find(Calendar givenDate, Parcel givenParcel) {
    Query query = entityManager.createQuery("SELECT c FROM ClimateRecord c WHERE (c.date = :givenDate AND c.parcel = :givenParcel)");
    query.setParameter("givenDate", givenDate);
    query.setParameter("givenParcel", givenParcel);

    ClimateRecord givenClimateRecord = null;

    try {
      givenClimateRecord = (ClimateRecord) query.getSingleResult();
    } catch (NoResultException e) {
      e.printStackTrace();
    }

    return givenClimateRecord;
  }

  /**
   * Comprueba la existencia de un registro climatico en la base
   * de datos subyacente. Retorna true si y solo si existe en
   * la base de datos el registro climatico con una fecha dada
   * perteneciente a una parcela dada.
   * 
   * @param givenDate
   * @param givenParcel
   * @return true si el registro climatico con una fecha dada
   * perteneciente a una parcela dada existe en la base de datos
   * subyacente, en caso contrario false
   */
  public boolean checkExistence(Calendar givenDate, Parcel givenParcel) {
    return (find(givenDate, givenParcel) != null);
  }

  /**
   * @param climateRecord
   * @return true si se repiten los tipos de precipitaciones en
   * una registro climatico, en caso contrario false
   */
  public boolean checkRepeatedPrecipTypes(ClimateRecord climateRecord) {
    TypePrecipitation typePrecipOne = climateRecord.getTypePrecipOne();
    TypePrecipitation typePrecipTwo = climateRecord.getTypePrecipTwo();
    TypePrecipitation typePrecipThree = climateRecord.getTypePrecipThree();
    TypePrecipitation typePrecipFour = climateRecord.getTypePrecipFour();

    String typePrecipNameOne;
    String typePrecipNameTwo;
    String typePrecipNameThree;
    String typePrecipNameFour;

    /*
     * Si solo uno de los tipos de precipitacion de un registro
     * climatico esta definido, NO hay repeticion de tipos de
     * precipitacion
     */
    if (typePrecipOne != null && typePrecipTwo == null && typePrecipThree == null && typePrecipFour == null) {
      return false;
    }

    if (typePrecipOne == null && typePrecipTwo != null && typePrecipThree == null && typePrecipFour == null) {
      return false;
    }

    if (typePrecipOne == null && typePrecipTwo == null && typePrecipThree != null && typePrecipFour == null) {
      return false;
    }

    if (typePrecipOne == null && typePrecipTwo == null && typePrecipThree == null && typePrecipFour != null) {
      return false;
    }

    /*
     * Casos de comparacion:
     * - tipo de precipitacion uno con tipo de precitacion dos
     * - tipo de precipitacion uno con tipo de precitacion tres
     * - tipo de precipitacion uno con tipo de precitacion cuatro
     */
    if (typePrecipOne != null && typePrecipTwo != null && typePrecipThree == null && typePrecipFour == null) {
      typePrecipNameOne = typePrecipOne.getName();
      typePrecipNameTwo = typePrecipTwo.getName();

      if (typePrecipNameOne.equals(typePrecipNameTwo)) {
        return true;
      }

    }

    if (typePrecipOne != null && typePrecipTwo == null && typePrecipThree != null && typePrecipFour == null) {
      typePrecipNameOne = typePrecipOne.getName();
      typePrecipNameThree = typePrecipThree.getName();

      if (typePrecipNameOne.equals(typePrecipNameThree)) {
        return true;
      }

    }

    if (typePrecipOne != null && typePrecipTwo == null && typePrecipThree == null && typePrecipFour != null) {
      typePrecipNameOne = typePrecipOne.getName();
      typePrecipNameFour = typePrecipFour.getName();

      if (typePrecipNameOne.equals(typePrecipNameFour)) {
        return true;
      }

    }

    /*
     * Casos de comparacion:
     * - tipo de precipitacion dos con tipo de precipitacion tres
     * - tipo de precipitacion dos con tipo de precipitacion cuatro
     */
    if (typePrecipOne == null && typePrecipTwo != null && typePrecipThree != null && typePrecipFour == null) {
      typePrecipNameTwo = typePrecipTwo.getName();
      typePrecipNameThree = typePrecipThree.getName();

      if (typePrecipNameTwo.equals(typePrecipNameThree)) {
        return true;
      }

    }

    if (typePrecipOne == null && typePrecipTwo != null && typePrecipThree == null && typePrecipFour != null) {
      typePrecipNameTwo = typePrecipTwo.getName();
      typePrecipNameFour = typePrecipFour.getName();

      if (typePrecipNameTwo.equals(typePrecipNameFour)) {
        return true;
      }

    }

    /*
     * Caso de comparacion:
     * - tipo de precipitacion tres con tipo de precipitacion cuatro
     */
    if (typePrecipOne == null && typePrecipTwo == null && typePrecipThree != null && typePrecipFour != null) {
      typePrecipNameThree = typePrecipThree.getName();
      typePrecipNameFour = typePrecipFour.getName();

      if (typePrecipNameThree.equals(typePrecipNameFour)) {
        return true;
      }

    }

    /*
     * Casos de comparacion:
     * - tipo de precipitacion uno con tipo de precipitacion dos y tres
     * - tipo de precipitacion uno con tipo de precipitacion dos y cuatro
     * - tipo de precipitacion uno con tipo de precipitacion tres y cuatro
     */
    if (typePrecipOne != null && typePrecipTwo != null && typePrecipThree != null && typePrecipFour == null) {
      typePrecipNameOne = typePrecipOne.getName();
      typePrecipNameTwo = typePrecipTwo.getName();
      typePrecipNameThree = typePrecipThree.getName();

      if (typePrecipNameOne.equals(typePrecipNameTwo) || typePrecipNameOne.equals(typePrecipNameThree)
          || typePrecipNameTwo.equals(typePrecipNameThree)) {
        return true;
      }

    }

    if (typePrecipOne != null && typePrecipTwo != null && typePrecipThree == null && typePrecipFour != null) {
      typePrecipNameOne = typePrecipOne.getName();
      typePrecipNameTwo = typePrecipTwo.getName();
      typePrecipNameFour = typePrecipFour.getName();

      if (typePrecipNameOne.equals(typePrecipNameTwo) || typePrecipNameOne.equals(typePrecipNameFour)
          || typePrecipNameTwo.equals(typePrecipNameFour)) {
        return true;
      }

    }

    if (typePrecipOne != null && typePrecipTwo == null && typePrecipThree != null && typePrecipFour != null) {
      typePrecipNameOne = typePrecipOne.getName();
      typePrecipNameThree = typePrecipThree.getName();
      typePrecipNameFour = typePrecipFour.getName();

      if (typePrecipNameOne.equals(typePrecipNameThree) || typePrecipNameOne.equals(typePrecipNameFour)
          || typePrecipNameThree.equals(typePrecipNameFour)) {
        return true;
      }

    }

    /*
     * Casos de comparacion:
     * - tipo de precipitacion dos con tipo de precipitacion tres y cuatro
     */
    if (typePrecipOne == null && typePrecipTwo != null && typePrecipThree != null && typePrecipFour != null) {
      typePrecipNameTwo = typePrecipTwo.getName();
      typePrecipNameThree = typePrecipThree.getName();
      typePrecipNameFour = typePrecipFour.getName();

      if (typePrecipNameTwo.equals(typePrecipNameThree) || typePrecipNameTwo.equals(typePrecipNameFour)
          || typePrecipNameThree.equals(typePrecipNameFour)) {
        return true;
      }

    }

    /*
     * Caso de comparacion en el que todos los tipos de
     * precipitacion de un registro climatico estan
     * definidos
     */
    if (typePrecipOne != null && typePrecipTwo != null && typePrecipThree != null && typePrecipFour != null) {
      typePrecipNameOne = typePrecipOne.getName();
      typePrecipNameTwo = typePrecipTwo.getName();
      typePrecipNameThree = typePrecipThree.getName();
      typePrecipNameFour = typePrecipFour.getName();

      if (typePrecipNameOne.equals(typePrecipNameTwo) || typePrecipNameOne.equals(typePrecipNameThree) || typePrecipNameOne.equals(typePrecipNameFour)) {
        return true;
      }

      if (typePrecipNameTwo.equals(typePrecipNameThree) || typePrecipNameTwo.equals(typePrecipNameFour)) {
        return true;
      }

      if (typePrecipNameThree.equals(typePrecipNameFour)) {
        return true;
      }

    }

    return false;
  }

  /**
   * Retorna el registro climatico, de una parcela, que tiene la
   * fecha dada y un ID distinto al ID dado, si y solo si existe
   * en la base de datos subyacente
   * 
   * @param id
   * @param parcel
   * @param date
   * @return referencia a un objeto de tipo ClimateRecord que
   *         representa al registro climatico, de una parcela,
   *         que tiene una fecha igual a la fecha dada y un ID
   *         distinto al ID dado, si existe en la base de datos
   *         subyacente. En caso contrario, null.
   */
  private ClimateRecord findRepeated(int id, Parcel parcel, Calendar date) {
    /*
     * Esta consulta obtiene el registro climatico, de una
     * parcela, que tiene su fecha igual a la fecha de un
     * registro climatico del conjunto de registros climaticos
     * en el que no esta el registro climatico del ID dado
     */
    Query query = getEntityManager().createQuery("SELECT c FROM ClimateRecord c WHERE (c.id != :givenId AND c.parcel = :givenParcel AND c.date = :givenDate)");
    query.setParameter("givenId", id);
    query.setParameter("givenParcel", parcel);
    query.setParameter("givenDate", date);

    ClimateRecord givenClimateRecord = null;

    try {
      givenClimateRecord = (ClimateRecord) query.getSingleResult();
    } catch (NoResultException e) {
      e.printStackTrace();
    }

    return givenClimateRecord;
  }

  /**
   * Retorna true si y solo si en la base de datos subyacente
   * existe un registro climatico, de una parcela, con una
   * fecha igual a la fecha dada y un ID distinto al ID dado
   * 
   * @param id
   * @param parcel
   * @param date
   * @return true si en la base de datos subyacente existe un
   *         registro climatico, de una parcela, con una fecha
   *         igual a la fecha dada y un ID distinto al ID dado,
   *         en caso contrario false
   */
  public boolean checkRepeated(int id, Parcel parcel, Calendar date) {
    return (findRepeated(id, parcel, date) != null);
  }

  /**
   * Actualiza la ETo (evapotranspiracion del cultivo de referencia)
   * y la ETc (evapotranspiracion del cultivo bajo condiciones estandar)
   * de un registro climatico correspondiente a una fecha y una parcela.
   * 
   * @param givenDate
   * @param givenParcel
   * @param eto         [mm/dia]
   * @param etc         [mm/dia]
   */
  public void updateEtoAndEtc(Calendar givenDate, Parcel givenParcel, double eto, double etc) {
    Query query = entityManager.createQuery("UPDATE ClimateRecord c SET c.eto = :eto, c.etc = :etc WHERE (c.date = :givenDate AND c.parcel = :givenParcel)");
    query.setParameter("eto", eto);
    query.setParameter("etc", etc);
    query.setParameter("givenDate", givenDate);
    query.setParameter("givenParcel", givenParcel);
    query.executeUpdate();
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

  /**
   * @param userId
   * @param parcelId
   * @param dateFrom
   * @param dateUntil
   * @return long que representa la cantidad de registros climaticos
   * de una parcela pertenecientes a un periodo definido por dos
   * fechas
   */
  private long countClimateRecordsForPeriod(int userId, int parcelId, Calendar dateFrom, Calendar dateUntil) {
    Query query = entityManager.createQuery("SELECT COUNT(c) FROM ClimateRecord c JOIN c.parcel p WHERE p IN (SELECT t FROM User u JOIN u.parcels t WHERE u.id = :userId) AND p.id = :parcelId AND :dateFrom <= c.date AND c.date <= :dateUntil");
    query.setParameter("userId", userId);
    query.setParameter("parcelId", parcelId);
    query.setParameter("dateFrom", dateFrom);
    query.setParameter("dateUntil", dateUntil);

    long result = 0;

    try {
      result = (long) query.getSingleResult();
    } catch (NoResultException e) {
      e.printStackTrace();
    }

    return result;
  }

  /**
   * Comprueba si para cada dia del periodo de dias definido por
   * la fecha inmediatamente siguiente a la fecha de siembra de
   * cultivo y la fecha inmediatamente anterior a la fecha actual
   * (es decir, hoy), hay un registro climatico para una parcela
   * en particular. Es necesario comprobar si para cada dia de
   * dicho periodo hay un registro climatico porque la aplicacion
   * calcula el balance hidrico de suelo de cada dia de dicho
   * periodo para calcular la necesidad de agua de riego de un
   * cultivo en la fecha actual y para calcular un balance hidrico
   * de suelo de un dia se requieren los datos climaticos de un
   * dia.
   * 
   * El motivo por el cual no se cuenta el registro climatico de
   * la fecha de siembra de un cultivo es que la aplicacion no
   * lo persiste, ya que no se lo debe tener en cuenta para
   * calcular la necesidad de agua de riego de un cultivo en la
   * fecha actual debido a que en la fecha de siembra se parte,
   * y se debe partir, con el suelo en capacidad de campo, es
   * decir, lleno de agua o en su maxima capacidad almacenamiento
   * de agua.
   * 
   * La aplicacion no calcula el balance hidrico de suelo de la
   * fecha de siembra de un cultivo, sino que lo persiste con
   * todos sus valores en cero, ya que en la fecha de siembra
   * de un cultivo se parte, y se debe partir, con el suelo en
   * capacidad de campo. La aplicacion tiene en cuenta los
   * balances hidricos del periodo de dias definido por la fecha
   * de siembra de un cultivo y la fecha inmediatamente anterior
   * a la fecha actual para calcular la necesidad de agua de
   * riego de un cultivo en la fecha actual.
   * 
   * @param userId
   * @param parcelId
   * @param seedDate
   * @return true si para cada dia del periodo definido por
   * la fecha inmediatamente siguiente a la fecha de siembra
   * de un cultivo y la fecha inmediatamente anterior a la
   * fecha actual (es decir, hoy), hay un registro climatico
   * perteneciente a una parcela en particular. En caso,
   * contrario, false.
   */
  public boolean checkClimateRecordsToCalculateIrrigationWaterNeed(int userId, int parcelId, Calendar seedDate) {
    Calendar dateFollowingSeedDate = UtilDate.getNextDateFromDate(seedDate);
    Calendar yesterdayDate = UtilDate.getYesterdayDate();

    /*
     * Se suma un uno a la diferencia entre la fecha inmediatamente
     * siguiente a la fecha de siembra de un cultivo y la fecha
     * inmediatamente anterior a la fecha actual para incluir a
     * esta ultima en el resultado de dicha diferencia, ya que cuenta
     * como un dia en el periodo definido por ambas fechas
     */
    long days = UtilDate.calculateDifferenceBetweenDates(dateFollowingSeedDate, yesterdayDate) + 1;
    long numberClimateRecords = countClimateRecordsForPeriod(userId, parcelId, dateFollowingSeedDate, yesterdayDate);

    /*
     * Si la cantidad de dias que hay entre la fecha inmediatamente
     * siguiente a la fecha de siembra de un cultivo y la fecha
     * inmediatamente anterior a la fecha actual es igual a la
     * cantidad de registros climaticos que hay en el periodo
     * definido por dichas fechas, se retorna true como indicador
     * de que en el periodo mencionado hay un registro climatico
     * para cada uno de los dias de dicho periodo. En caso contrario,
     * se retorna false.
     * 
     * El metodo automatico getCurrentWeatherDataset() de la
     * clase Java ClimateRecordManager tiene como objetivo
     * recuperar los datos meteorologicos de la fecha actual en
     * forma de registro climatico. Gracias a este metodo, la
     * aplicacion persiste un registro climatico para cada de
     * dia que pasa. El metodo runCalculationIrrigationWaterNeedCurrentDate()
     * de las clases PlantingRecordRestServlet y PlantingRecordManager
     * persiste el registro climatico para cada uno de los
     * dias del periodo definido por la fecha inmediatamente
     * siguiente a la fecha de siembra de un cultivo y la
     * fecha inmediatamente anterior a la fecha actual.
     * El motivo por el cual no se persiste el registro
     * climatico de la fecha de siembra de un cultivo es
     * que en la fecha de siembra se parte con el suelo
     * en la condicion de capacidad de campo (suelo lleno
     * de agua o en su maxima capacidad de almacenamiento de
     * agua).
     * 
     * El segundo parrafo de este comentario es para dar a
     * entender que para cada dia del periodo definido por la
     * fecha inmediatamente siguiente a la fecha de siembra de
     * un cultivo y la fecha inmediatamente anterior a la fecha
     * actual, hay un registro climatico. Este es el motivo por
     * el cual el primer parrafo de este comentario habla de la
     * comparacion entre la cantidad de dias que hay entre las
     * fechas mencionadas y la cantidad de registros climaticos
     * que hay en el periodo definido por dichas fechas.
     */
    if (days == numberClimateRecords) {
        return true;
    }

    return false;
  }

  /**
   * Retorna true si y solo si la fecha de un registro climatico
   * es estrictamente menor a la fecha actual
   * 
   * @param climateRecord
   * @return true si la fecha de un registro climatico es anterior
   * a la fecha actual, en caso contrario false
   */
  public boolean isFromPast(ClimateRecord climateRecord) {
    return (UtilDate.compareTo(climateRecord.getDate(), UtilDate.getCurrentDate()) < 0);
  }

  public Page<ClimateRecord> findAllPagination(int userId, Integer page, Integer cantPerPage, Map<String, String> parameters) throws ParseException {
    SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
    Date date;
    Calendar calendarDate;

    // Genera el WHERE dinámicamente
    StringBuffer where = new StringBuffer(" WHERE 1=1 AND e IN (SELECT t FROM ClimateRecord t JOIN t.parcel p WHERE p IN (SELECT x FROM User u JOIN u.parcels x WHERE u.id = :userId))");

    if (parameters != null) {

      for (String param : parameters.keySet()) {
        Method method;

        try {
          method = ClimateRecord.class.getMethod("get" + capitalize(param));

          if (method == null || parameters.get(param) == null || parameters.get(param).isEmpty()) {
            continue;
          }

          switch (method.getReturnType().getSimpleName()) {
            case "String":
              where.append(" AND UPPER(e.");
              where.append(param);
              where.append(") LIKE UPPER('%");
              where.append(parameters.get(param));
              where.append("%')");
              break;
            case "Parcel":
              where.append(" AND UPPER(e.");
              where.append(param);
              where.append(".name");
              where.append(") LIKE UPPER('%");
              where.append(parameters.get(param));
              where.append("%')");
              break;
            case "Calendar":

              if (param.equals("date")) {
                date = new Date(dateFormatter.parse(parameters.get(param)).getTime());
                calendarDate = UtilDate.toCalendar(date);
                where.append(" AND e.");
                where.append(param);
                where.append(" >= ");
                where.append("'" + UtilDate.convertDateToYyyyMmDdFormat(calendarDate) + "'");
              }

              break;
            default:
              where.append(" AND e.");
              where.append(param);
              where.append(" = ");
              where.append(parameters.get(param));
              break;
          }

        } catch (NoSuchMethodException | SecurityException e) {
          e.printStackTrace();
        }

      } // End for

    } // End if

    // Cuenta el total de resultados
    Query countQuery = entityManager.createQuery("SELECT COUNT(e.id) FROM " + ClimateRecord.class.getSimpleName() + " e" + where.toString());
    countQuery.setParameter("userId", userId);

    // Pagina
    Query query = entityManager.createQuery("FROM " + ClimateRecord.class.getSimpleName() + " e" + where.toString() + " ORDER BY e.date");
    query.setMaxResults(cantPerPage);
    query.setFirstResult((page - 1) * cantPerPage);
    query.setParameter("userId", userId);

    Integer count = ((Long) countQuery.getSingleResult()).intValue();
    Integer lastPage = (int) Math.ceil((double) count / (double) cantPerPage);

    // Arma la respuesta
    Page<ClimateRecord> resultPage = new Page<ClimateRecord>(page, count, page > 1 ? page - 1 : page, page < lastPage ? page + 1 : lastPage, lastPage, query.getResultList());
    return resultPage;
  }

  private String capitalize(final String line) {
    return Character.toUpperCase(line.charAt(0)) + line.substring(1);
  }

}
