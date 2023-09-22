package stateless;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.lang.NullPointerException;
import java.util.Calendar;
import java.util.Collection;
import model.ClimateRecord;
import model.Parcel;
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
  public ClimateRecord find(int userId, int climateRecordId) {
    Query query = getEntityManager().createQuery("SELECT c FROM ClimateRecord c JOIN c.parcel p WHERE (c.id = :climateRecordId AND p.user.id = :userId)");
    query.setParameter("climateRecordId", climateRecordId);
    query.setParameter("userId", userId);

    ClimateRecord givenClimateRecord = null;

    try {
      givenClimateRecord = (ClimateRecord) query.getSingleResult();
    } catch (NoResultException e) {
      e.printStackTrace();
    }

    return givenClimateRecord;
  }

  /**
   * Retorna un registro climatico perteneciente a una de las
   * parcelas de un usuario
   * 
   * @param userId
   * @param climateRecordId
   * @return referencia a un objeto de tipo ClimateRecord que
   * representa el registro climatico de una parcela de un
   * usuario en caso de encontrarse en la base de datos subyacente
   * el registro climatico con el ID dado y asociado al usuario
   * del ID dado, en caso contrario null
   */
  public ClimateRecord findByUserId(int userId, int climateRecordId) {
    Query query = getEntityManager().createQuery("SELECT c FROM ClimateRecord c JOIN c.parcel p WHERE (c.id = :climateRecordId AND p.user.id = :userId)");
    query.setParameter("climateRecordId", climateRecordId);
    query.setParameter("userId", userId);

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
    Query query = getEntityManager().createQuery("SELECT c FROM ClimateRecord c JOIN c.parcel p WHERE (p.user.id = :userId) ORDER BY c.id");
    query.setParameter("userId", userId);

    return (Collection) query.getResultList();
  }

  /**
   * Retorna los registros climaticos de una parcela mediante
   * el nombre de una parcela y el ID del usuario al que pertenece
   * una parcela
   * 
   * @param userId
   * @param givenParcelName
   * @return referencia a un objeto de tipo Collection que
   * contiene los registros climaticos de la parcela que tiene
   * el nombre dado y que pertenece al usuario con el ID dado
   */
  public Collection<ClimateRecord> findAllByParcelName(int userId, String givenParcelName) {
    Query query = getEntityManager().createQuery("SELECT c FROM ClimateRecord c WHERE (c.parcel.name = :givenParcelName AND c.parcel.user.id = :userId) ORDER BY c.date");
    query.setParameter("userId", userId);
    query.setParameter("givenParcelName", givenParcelName);

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
    Query query = getEntityManager().createQuery("SELECT c FROM ClimateRecord c JOIN c.parcel p WHERE (p.id = :givenParcelId AND p.user.id = :givenUserId) ORDER BY c.id");
    query.setParameter("givenUserId", userId);
    query.setParameter("givenParcelId", parcelId);

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
    Query query = getEntityManager().createQuery("SELECT c FROM ClimateRecord c JOIN c.parcel p WHERE (p.id = :givenParcelId AND p.user.id = :givenUserId AND :givenDateFrom <= c.date AND c.date <= :givenDateUntil) ORDER BY c.date");
    query.setParameter("givenUserId", userId);
    query.setParameter("givenParcelId", parcelId);
    query.setParameter("givenDateFrom", dateFrom);
    query.setParameter("givenDateUntil", dateUntil);

    return (Collection) query.getResultList();
  }

  /**
   * Retorna todos los registros climaticos modificables de
   * todas las parcelas de la base de datos subyacente
   * 
   * @return referencia a un objeto de tipo Collection que
   * contiene todos los registros climaticos modificables de
   * todas las parcelas de la base de datos subyacente
   */
  public Collection<ClimateRecord> findAllModifiable() {
    Query query = entityManager.createQuery("SELECT c FROM ClimateRecord c WHERE c.modifiable = 1 ORDER BY c.id");
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
      chosenClimateRecord.setEto(modifiedClimateRecord.getEto());
      chosenClimateRecord.setEtc(modifiedClimateRecord.getEtc());
      chosenClimateRecord.setModifiable(modifiedClimateRecord.getModifiable());
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
      chosenClimateRecord.setDewPoint(modifiedClimateRecord.getDewPoint());
      chosenClimateRecord.setAtmosphericPressure(modifiedClimateRecord.getAtmosphericPressure());
      chosenClimateRecord.setWindSpeed(modifiedClimateRecord.getWindSpeed());
      chosenClimateRecord.setCloudCover(modifiedClimateRecord.getCloudCover());
      chosenClimateRecord.setMinimumTemperature(modifiedClimateRecord.getMinimumTemperature());
      chosenClimateRecord.setMaximumTemperature(modifiedClimateRecord.getMaximumTemperature());
      chosenClimateRecord.setEto(modifiedClimateRecord.getEto());
      chosenClimateRecord.setEtc(modifiedClimateRecord.getEtc());
      chosenClimateRecord.setModifiable(modifiedClimateRecord.getModifiable());
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
   * Retorna true si y solo si un registro climatico pertenece
   * a un usuario.
   * 
   * @param userId
   * @param climateRecordId
   * @return true si se encuentra el registro climatico con el
   * ID y el ID de usuario provistos, false en caso contrario
   */
  public boolean checkUserOwnership(int userId, int climateRecordId) {
    return (find(userId, climateRecordId) != null);
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

  /**
   * Retorna true si y solo si un registro climatico es modificable.
   * 
   * Hay que tener en cuenta que este metodo debe ser invocado
   * luego de invocar al metodo checkExistence de esta clase,
   * ya que si no se hace esto puede ocurrir la excepcion
   * NoResultException, la cual, ocurre cuando se invoca el
   * metodo getSingleResult de la clase Query para buscar
   * un dato inexistente en la base de datos subyacente.
   * 
   * @param id
   * @return true si un registro climatico es modificable,
   * false en caso contrario
   */
  public boolean isModifiable(int id) {
    return find(id).getModifiable();
  }

  /**
   * Obtiene y persiste el registro climatico de la fecha actual.
   * Este metodo es para el metodo calculateIrrigationWaterNeed
   * de la clase PlantingRecordRestServlet.
   * 
   * @param givenParcel
   * @return referencia a un objeto de tipo ClimateRecord que
   * representa el registro climatico de la fecha actual
   */
  public ClimateRecord persistCurrentClimateRecord(Parcel givenParcel) {
    /*
     * Se divide el tiempo en milisegundos de la fecha actual
     * entre 1000 porque el metodo estatico getForecast de la
     * clase ClimateClient utiliza el tiempo en formato UNIX,
     * el cual, son los segundos trancurridos desde el 1 de
     * enero de 1970 (epoca).
     */
    ClimateRecord currentClimateRecord = ClimateClient.getForecast(givenParcel, (UtilDate.getCurrentDate().getTimeInMillis() / 1000));
    return create(currentClimateRecord);
  }

  /**
   * Suma el agua de lluvia de valuePastDaysReference registros
   * climaticos anteriores a la fecha actual pertenecientes a
   * una parcela.
   * 
   * El valor de valuePastDaysReference depende de cada usuario y
   * solo puede ser entre un limite minimo y un limite maximo,
   * los cuales estan definidos en la clase PastDaysReferenceServiceBean.
   * 
   * @param givenUserId
   * @param givenParcelId
   * @param valuePastDaysReference
   * @return double que representa la suma del agua de lluvia
   * que cayo sobre una parcela en valuePastDaysReference dias
   * anteriores a la fecha actual, siendo la parcela perteneciente
   * a un usuario dado
   */
  public double sumRainwaterPastDays(int givenUserId, int givenParcelId, int valuePastDaysReference) {
    Calendar periodUpperDate = UtilDate.getYesterdayDate();

    /*
     * La constante valuePastDaysReference pertenece a esta clase
     * y se la utiliza para obtener el limite inferior de un periodo
     * de fechas anteriores a la fecha actual, siendo el limite
     * superior de este periodo la fecha inmediatamente anterior
     * a la fecha actual
     */
    Calendar lowerDatePeriod = UtilDate.getPastDateFromOffset(valuePastDaysReference);

    /*
     * Con esta condicion, la consulta selecciona todos los
     * registros climaticos de una parcela que estan
     * comprendidos en un periodo definido por dos fechas
     */
    String conditionWhere = "(c.parcel.user.id = :userId AND c.parcel.id = :parcelId AND :lowerDatePeriod <= c.date AND c.date <= :periodUpperDate)";

    /*
     * Suma la cantidad de agua de lluvia de valuePastDaysReference
     * registros climaticos anteriores a la fecha actual pertenecientes
     * a una parcela de un usuario, los cuales estan comprendidos en un
     * periodo definido por dos fechas, obteniendo la cantidad total
     * de agua de lluvia que cayo sobre una parcela en dicho periodo
     */
    Query query = entityManager.createQuery("SELECT SUM(c.precip) FROM ClimateRecord c WHERE " + conditionWhere);
    query.setParameter("userId", givenUserId);
    query.setParameter("parcelId", givenParcelId);
    query.setParameter("lowerDatePeriod", lowerDatePeriod);
    query.setParameter("periodUpperDate", periodUpperDate);

    double summedRainwaterPastDays = 0.0;

    try {
      /*
       * Si se realiza la consulta JPQL de este metodo en SQL
       * con una parcela que no tiene ningun registro climatico
       * asociado en un periodo definido por dos fechas, se
       * observara que el valor devuelto es NULL. Por lo tanto,
       * es necesario contemplar este caso en el codigo fuente
       * de este metodo.
       * 
       * En caso de que se solicite la suma del agua de lluvia
       * que cayo sobre una parcela en valuePastDaysReference
       * dias anteriores a la fecha actual y la parcela no tiene
       * ningun registro climatico con las fechas de dichos dias,
       * se retorna el valor 0.0.
       */
      summedRainwaterPastDays = (double) query.getSingleResult();
    } catch (NullPointerException e) {
      e.printStackTrace();
    }

    return summedRainwaterPastDays;
  }

  /**
   * Suma la ETc de valuePastDaysReference registros climaticos
   * anteriores a la fecha actual pertenecientes a una parcela.
   * La ETc es de un cultivo sembrado en una parcela, la cual
   * pertenece a un usuario.
   * 
   * El valor de valuePastDaysReference depende de cada usuario y
   * solo puede ser entre un limite minimo y un limite maximo,
   * los cuales estan definidos en la clase PastDaysReferenceServiceBean.
   * 
   * @param givenUserId
   * @param givenParcelId
   * @param valuePastDaysReference
   * @return double que representa la suma de la ETc de valuePastDaysReference
   * registros climaticos anteriores a la fecha actual pertenecientes a una
   * parcela dada, la cual pertenece a un usuario dado
   */
  public double sumEtcPastDays(int givenUserId, int givenParcelId, int valuePastDaysReference) {
    Calendar periodUpperDate = UtilDate.getYesterdayDate();

    /*
     * La constante valuePastDaysReference pertenece a esta clase
     * y se la utiliza para obtener el limite inferior de un periodo
     * de fechas anteriores a la fecha actual, siendo el limite superior
     * de este periodo la fecha inmediatamente anterior a la fecha actual
     */
    Calendar lowerDatePeriod = UtilDate.getPastDateFromOffset(valuePastDaysReference);

    /*
     * Con esta condicion, la consulta selecciona todos los
     * registros climaticos de una parcela que estan
     * comprendidos en un periodo definido por dos fechas
     */
    String conditionWhere = "(c.parcel.user.id = :userId AND c.parcel.id = :parcelId AND :lowerDatePeriod <= c.date AND c.date <= :periodUpperDate)";

    /*
     * Suma la ETc de valuePastDaysReference registros climaticos
     * anteriores a la fecha actual pertenecientes a una parcela de
     * un usuario, los cuales estan comprendidos en un periodo
     * definido por dos fechas, obteniendo la ETc total de un
     * cultivo en dicho periodo
     */
    Query query = entityManager.createQuery("SELECT SUM(c.etc) FROM ClimateRecord c WHERE " + conditionWhere);
    query.setParameter("userId", givenUserId);
    query.setParameter("parcelId", givenParcelId);
    query.setParameter("lowerDatePeriod", lowerDatePeriod);
    query.setParameter("periodUpperDate", periodUpperDate);

    double etcSummedPastDays = 0.0;

    try {
      /*
       * Si se realiza la consulta JPQL de este metodo en SQL
       * con una parcela que NO tiene ningun registro climatico
       * asociado en un periodo definido por dos fechas, se
       * observara que el valor devuelto es NULL. Por lo tanto,
       * es necesario contemplar este caso en el codigo fuente
       * de este metodo.
       * 
       * En caso de que se solicite la suma de la ETc de un cultivo
       * plantado sobre una parcela en valuePastDaysReference dias
       * anteriores a la fecha actual y la parcela no tiene ningun
       * registro climatico con las fechas de dichos dias, se retorna
       * el valor 0.0.
       */
      etcSummedPastDays = (double) query.getSingleResult();
    } catch (NullPointerException e) {
      e.printStackTrace();
    }

    return etcSummedPastDays;
  }

}
