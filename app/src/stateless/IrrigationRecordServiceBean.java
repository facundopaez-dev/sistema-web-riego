package stateless;

import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.lang.NullPointerException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Date;
import java.util.Calendar;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import model.Crop;
import model.IrrigationRecord;
import model.Parcel;
import util.UtilDate;

@Stateless
public class IrrigationRecordServiceBean {

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

  public IrrigationRecord create(IrrigationRecord newIrrigationRecord) {
    getEntityManager().persist(newIrrigationRecord);
    return newIrrigationRecord;
  }

  /**
   * Elimina fisicamente un registro de riego perteneciente a
   * una parcela de un usuario
   * 
   * @param userId
   * @param irrigationRecordId
   * @return referencia a un objeto de tipo IrrigationRecord en
   * caso de eliminarse de la base de datos subyacente el registro
   * de riego que tiene el ID dado y que esta asociado a una
   * parcela de un usuario que tiene el ID de usuario dado, en
   * caso contrario null
   */
  public IrrigationRecord remove(int userId, int irrigationRecordId) {
    IrrigationRecord givenIrrigationRecord = findByUserId(userId, irrigationRecordId);

    if (givenIrrigationRecord != null) {
      getEntityManager().remove(givenIrrigationRecord);
      return givenIrrigationRecord;
    }

    return null;
  }

  public IrrigationRecord remove(int irrigationRecordId) {
    IrrigationRecord givenIrrigationRecord = find(irrigationRecordId);

    if (givenIrrigationRecord != null) {
      getEntityManager().remove(givenIrrigationRecord);
      return givenIrrigationRecord;
    }

    return null;
  }

  /**
   * Elimina los registros de riego asociados a las parcelas
   * de un usuario
   * 
   * @param userId
   */
  public void deleteIrrigationRecordsByUserId(int userId) {
    Query query = entityManager.createQuery("DELETE FROM IrrigationRecord i WHERE i.parcel IN (SELECT x FROM Parcel x WHERE x.user.id = :userId)");
    query.setParameter("userId", userId);
    query.executeUpdate();
  }

  /**
   * Modifica un registro de riego perteneciente a una parcela
   * de un usuario
   * 
   * @param userId
   * @param irrigationRecordId
   * @param modifiedIrrigationRecord
   * @return referencia a un objeto de tipo IrrigationRecord que
   * contiene las modificaciones realizadas en caso de encontrarse
   * en la base de datos subyacente el registro de riego con el ID
   * dado, en caso contrario null
   */
  public IrrigationRecord modify(int userId, int irrigationRecordId, IrrigationRecord modifiedIrrigationRecord) {
    IrrigationRecord chosenIrrigationRecord = findByUserId(userId, irrigationRecordId);

    if (chosenIrrigationRecord != null) {
      chosenIrrigationRecord.setDate(modifiedIrrigationRecord.getDate());
      chosenIrrigationRecord.setIrrigationDone(modifiedIrrigationRecord.getIrrigationDone());
      chosenIrrigationRecord.setParcel(modifiedIrrigationRecord.getParcel());
      chosenIrrigationRecord.setCrop(modifiedIrrigationRecord.getCrop());
      return chosenIrrigationRecord;
    }

    return null;
  }

  public IrrigationRecord find(int id) {
    return getEntityManager().find(IrrigationRecord.class, id);
  }

  /**
   * Retorna un registro de riego perteneciente a una de las
   * parcelas de un usuario
   * 
   * @param userId
   * @param irrigationRecordId
   * @return referencia a un objeto de tipo IrrigationRecord que
   * representa el registro de riego de una parcela de un
   * usuario en caso de encontrarse en la base de datos subyacente
   * el registro de riego con el ID dado y asociado al usuario
   * del ID dado, en caso contrario null
   */
  public IrrigationRecord findByUserId(int userId, int irrigationRecordId) {
    Query query = getEntityManager().createQuery("SELECT i FROM IrrigationRecord i WHERE i.id = :irrigationRecordId AND i.parcel.user.id = :userId");
    query.setParameter("irrigationRecordId", irrigationRecordId);
    query.setParameter("userId", userId);

    IrrigationRecord irrigationRecord = null;

    try {
      irrigationRecord = (IrrigationRecord) query.getSingleResult();
    } catch(NoResultException e) {
      e.printStackTrace();
    }

    return irrigationRecord;
  }

  /**
   * Retorna todos los registros de riego de todas las
   * parcelas de un usuario
   * 
   * @param userId
   * @return referencia a un objeto de tipo Collection que
   * contiene todos los registros de riego de todas las
   * parcelas del usuario con el ID dado
   */
  public Collection<IrrigationRecord> findAll(int userId) {
    Query query = entityManager.createQuery("SELECT i FROM IrrigationRecord i WHERE i.parcel.user.id = :userId ORDER BY i.id");
    query.setParameter("userId", userId);

    return (Collection) query.getResultList();
  }

  /**
   * Retorna los registros de riego de una parcela mediante
   * el nombre de una parcela y el ID del usuario al que pertenece
   * una parcela
   * 
   * @param userId
   * @param parcelName
   * @return referencia a un objeto de tipo Collection que
   * contiene los registros de riego de la parcela que tiene
   * el nombre dado y que pertenece al usuario con el ID dado
   */
  public Collection<IrrigationRecord> findAllByParcelName(int userId, String parcelName) {
    Query query = getEntityManager().createQuery("SELECT i FROM IrrigationRecord i JOIN i.parcel p WHERE (p.name = :parcelName AND p.user.id = :userId) ORDER BY i.id");
    query.setParameter("userId", userId);
    query.setParameter("parcelName", parcelName);

    return (Collection) query.getResultList();
  }

  /**
   * Retorna los registros de riego de una parcela mediante
   * el nombre de una parcela, una fecha y el ID del usuario
   * al que pertenece una parcela
   * 
   * @param userId
   * @param parcelName
   * @param date
   * @return referencia a un objeto de tipo Collection que
   * contiene los registros de riego que tienen una fecha
   * pertenecientes a una parcela que tiene un nombre, la
   * cual pertenece a un usuario
   */
  public Collection<IrrigationRecord> findAllByParcelNameAndDate(int userId, String parcelName, Calendar date) {
    Query query = getEntityManager().createQuery("SELECT i FROM IrrigationRecord i JOIN i.parcel p WHERE (i.date = :date AND p.name = :parcelName AND p.user.id = :userId) ORDER BY i.id");
    query.setParameter("userId", userId);
    query.setParameter("parcelName", parcelName);
    query.setParameter("date", date);

    return (Collection) query.getResultList();
  }

  /**
   * @param parcelId
   * @param date
   * @return referencia a un objeto de tipo Collection que
   * contiene todos los registros de riego de una parcela
   * que tienen una fecha
   */
  public Collection<IrrigationRecord> findAllByParcelIdAndDate(int parcelId, Calendar date) {
    Query query = entityManager.createQuery("SELECT i FROM IrrigationRecord i WHERE (i.date = :date AND i.parcel.id = :parcelId)");
    query.setParameter("date", date);
    query.setParameter("parcelId", parcelId);

    return (Collection) query.getResultList();
  }

  /**
   * Retorna todos los registros de riego de una parcela de
   * un usuario, que tienen un cultivo y que estan en un periodo
   * definido por dos fechas, si una parcela tiene registros
   * de riego con un cultivo asginado en un periodo.
   * 
   * @param userId
   * @param parcelId
   * @param dateFrom
   * @param dateUntil
   * @return referencia a un objeto de tipo Collection que contiene
   * todos los registros de riego de una parcela de un usuario, que
   * tienen un cultivo y que estan en un periodo definido por dos
   * fechas. En caso contrario, referencia a un objeto de tipo
   * Collection vacio (con 0 elementos).
   */
  public Collection<IrrigationRecord> findAllWithCropBetweenDates(int userId, int parcelId, Calendar dateFrom, Calendar dateUntil) {
    Query query = getEntityManager().createQuery("SELECT i FROM IrrigationRecord i JOIN i.parcel p WHERE (i.crop IS NOT NULL AND :dateFrom <= i.date AND i.date <= :dateUntil AND p.id = :parcelId AND p.user.id = :userId) ORDER BY i.date");
    query.setParameter("userId", userId);
    query.setParameter("parcelId", parcelId);
    query.setParameter("dateFrom", dateFrom);
    query.setParameter("dateUntil", dateUntil);

    return (Collection) query.getResultList();
  }

  /**
   * Elimina los registros de riego de una parcela que tienen
   * una fecha que se encuentra en el periodo definido por
   * una fecha desde y una fecha hasta
   * 
   * @param userId
   * @param parcelId
   * @param dateFrom
   * @param dateUntil
   */
  public void deleteBetweenDates(int userId, int parcelId, Calendar dateFrom, Calendar dateUntil) {
    Query query = getEntityManager().createQuery("DELETE FROM IrrigationRecord i WHERE (:dateFrom <= i.date AND i.date <= :dateUntil AND i.parcel.id = :parcelId AND i.parcel.user.id = :userId)");
    query.setParameter("userId", userId);
    query.setParameter("parcelId", parcelId);
    query.setParameter("dateFrom", dateFrom);
    query.setParameter("dateUntil", dateUntil);

    query.executeUpdate();
  }

  /**
   * @param parcelId
   * @return referencia a un objeto de tipo List<IrrigationRecord>
   * que contiene todos los registros de riego con cultivo
   * pertenecientes a una parcela
   */
  public List<IrrigationRecord> findAllWithCrops(int parcelId) {
    Query query = getEntityManager().createQuery("SELECT i FROM IrrigationRecord i WHERE i.parcel.id = :parcelId AND i.crop IS NOT NULL");
    query.setParameter("parcelId", parcelId);

    return query.getResultList();
  }

  /**
   * Modifica el cultivo de los registros de riego
   * perteneciente a una parcela que estan en un
   * periodo definido por dos fechas
   * 
   * @param parcelId
   * @param crop
   * @param dateFrom
   * @param dateUntil
   */
  public void modifyCropInPeriod(int parcelId, Crop crop, Calendar dateFrom, Calendar dateUntil) {
    Query query = entityManager.createQuery("UPDATE IrrigationRecord i SET i.crop = :crop WHERE i.parcel.id = :parcelId AND :dateFrom <= i.date AND i.date <= :dateUntil");
    query.setParameter("parcelId", parcelId);
    query.setParameter("crop", crop);
    query.setParameter("dateFrom", dateFrom);
    query.setParameter("dateUntil", dateUntil);
    query.executeUpdate();
  }

  /**
   * Retorna true si y solo si una parcela tiene registros de
   * riego con cultivo
   * 
   * @param parcelId
   * @param dateFrom
   * @param dateUntil
   * @return true si una parcela tiene registros de riego
   * con cultivo pertenecientes, en caso contrario false
   */
  public boolean hasIrrigationRecordsWithCrops(int parcelId) {
    return !findAllWithCrops(parcelId).isEmpty();
  }

  /**
   * @param parcelId
   * @param dateFrom
   * @param dateUntil
   * @return referencia a un objeto de tipo List<IrrigationRecord>
   * que contiene todos los registros de riego que tienen un
   * cultivo y que estan comprendidos en un periodo definido por
   * dos fechas
   */
  public List<IrrigationRecord> findAllWithCropAndByPeriod(int parcelId, Calendar dateFrom, Calendar dateUntil) {
    Query query = getEntityManager().createQuery("SELECT i FROM IrrigationRecord i WHERE i.parcel.id = :parcelId AND i.crop IS NOT NULL AND :dateFrom <= i.date AND i.date <= :dateUntil");
    query.setParameter("parcelId", parcelId);
    query.setParameter("dateFrom", dateFrom);
    query.setParameter("dateUntil", dateUntil);

    return query.getResultList();
  }

  /**
   * @param userId
   * @param parcelId
   * @param dateFrom
   * @param dateUntil
   * @return referencia a un objeto de tipo List<IrrigationRecord>
   * que contiene todos los registros de riego de una parcela de
   * un usuario que tienen un cultivo y que estan comprendidos en
   * un periodo definido por dos fechas
   */
  public List<IrrigationRecord> findAllWithCropAndByPeriod(int userId, int parcelId, Calendar dateFrom, Calendar dateUntil) {
    String conditionWhere = "p.id = :parcelId AND i.crop IS NOT NULL AND :dateFrom <= i.date AND i.date <= :dateUntil AND p.user.id = :userId";

    Query query = getEntityManager().createQuery("SELECT i FROM IrrigationRecord i JOIN i.parcel p WHERE " + conditionWhere);
    query.setParameter("parcelId", parcelId);
    query.setParameter("dateFrom", dateFrom);
    query.setParameter("dateUntil", dateUntil);
    query.setParameter("userId", userId);
    
    return query.getResultList();
  }

  /**
   * Retorna true si y solo si una parcela tiene registros de
   * riego con cultivo pertenecientes a un periodo definido
   * por dos fechas
   * 
   * @param parcelId
   * @param dateFrom
   * @param dateUntil
   * @return true si una parcela tiene registros de riego
   * con cultivo pertenecientes a un periodo definido por
   * dos fechas, en caso contrario false
   */
  public boolean hasIrrigationRecordsWithCrops(int parcelId, Calendar dateFrom, Calendar dateUntil) {
    return !findAllWithCropAndByPeriod(parcelId, dateFrom, dateUntil).isEmpty();
  }

  /**
   * Retorna true si y solo si una parcela de un usuario tiene
   * registros de riego con cultivo pertenecientes a un periodo
   * definido por dos fechas
   * 
   * @param parcelId
   * @param dateFrom
   * @param dateUntil
   * @return true si una parcela de un usuario tiene registros
   * de riego con cultivo pertenecientes a un periodo definido
   * por dos fechas, en caso contrario false
   */
  public boolean hasIrrigationRecordsWithCrops(int userId, int parcelId, Calendar dateFrom, Calendar dateUntil) {
    return !findAllWithCropAndByPeriod(userId, parcelId, dateFrom, dateUntil).isEmpty();
  }

  /**
   * Comprueba la existencia de un registro de riego en la base
   * de datos subyacente. Retorna true si y solo si existe el
   * registro de riego con el ID dado.
   * 
   * @param id
   * @return true si el registro de riego con el ID dado existe
   * en la base de datos subyacente, en caso contrario false
   */
  public boolean checkExistence(int id) {
    return (getEntityManager().find(IrrigationRecord.class, id) != null);
  }

  /**
   * Comprueba si un registro de riego pertenece a un usuario
   * a traves de la relacion muchos a uno que hay entre la
   * entidad parcela y la entidad usuario.
   * 
   * Retorna true si y solo si un registro de riego pertenece
   * a una parcela de un usuario.
   * 
   * @param userId
   * @param irrigationRecordId
   * @return true si se encuentra el registro de riego con el ID
   * dado perteneciente una parcela del usuario con el ID dado,
   * en caso contrario false
   */
  public boolean checkUserOwnership(int userId, int irrigationRecordId) {
    return (findByUserId(userId, irrigationRecordId) != null);
  }

  /**
   * @param parcelId
   * @return double que representa la cantidad total de agua
   * de riego utilizada en una parcela en la fecha actual.
   * Si la parcela tiene un cultivo sembrado y en desarrollo
   * en la fecha actual, el double representa la cantidad total
   * de agua utilizada para regar un cultivo en la fecha actual.
   */
  public double calculateTotalIrrigationWaterCurrentDate(int parcelId) {
    /*
     * Suma el riego realizado de cada uno de los registros
     * de riego de una parcela en la fecha actual
     */
    Query query = entityManager.createQuery("SELECT SUM(i.irrigationDone) FROM IrrigationRecord i JOIN i.parcel p WHERE (i.date = :currentDate AND p.id = :parcelId)");
    query.setParameter("currentDate", UtilDate.getCurrentDate());
    query.setParameter("parcelId", parcelId);

    double totalIrrigationWaterCurrentDate = 0.0;

    try {
      /*
       * Si se realiza la consulta JPQL de este metodo en SQL
       * con una parcela que no tiene ningun registro de riego
       * asociado en un periodo definido por dos fechas, se
       * observara que el valor devuelto es NULL. Por lo tanto,
       * es necesario contemplar este caso en el codigo fuente
       * de este metodo.
       * 
       * En caso de que se solicite la suma del agua de riego de
       * la fecha actual de una parcela que no tiene ningun registro
       * de riego con la fecha actual, se retorna el valor 0.0
       */
      totalIrrigationWaterCurrentDate = (double) query.getSingleResult();
    } catch (NullPointerException e) {
      e.printStackTrace();
    }

    return totalIrrigationWaterCurrentDate;
  }

  /**
   * @param givenDate
   * @param givenParcel
   * @return punto flotante que representa la cantidad total de
   *         agua de riego utilizada para un cultivo en una fecha
   *         dada
   */
  public double calculateTotalIrrigationWaterGivenDate(Calendar givenDate, Parcel givenParcel) {
    /*
     * Suma el riego realizado de cada uno de los registros
     * de riego de una fecha dada pertenecientes a una parcela
     * dada
     */
    Query query = entityManager.createQuery("SELECT SUM(i.irrigationDone) FROM IrrigationRecord i WHERE (i.date = :givenDate AND i.parcel = :givenParcel)");
    query.setParameter("givenDate", givenDate);
    query.setParameter("givenParcel", givenParcel);

    double result = 0.0;

    try {
      result = (double) query.getSingleResult();
    } catch (NullPointerException e) {
      e.printStackTrace();
    }

    return result;
  }

  /**
   * Retorna true si y solo si un registro de riego perteneciente a
   * una parcela de un usuario es del pasado, es decir, si su fecha
   * es estrictamente menor a la fecha actual.
   * 
   * Hay que tener en cuenta que este metodo debe ser invocado luego
   * de haber invocado al metodo checkExistence de esta clase, ya
   * que de lo contrario puede ocurrir la excepcion NoResultException,
   * la cual, ocurre cuando NO existe en la base de datos subyacente
   * el dato consultado.
   * 
   * El motivo por el cual no se usa el metodo before de la clase
   * Calendar para determinar si la fecha de un registro de riego es
   * pasada o no es que dicho metodo utiliza el tiempo en milisegundos
   * UTC desde la epoca (1 de enero de 1970) de dos objetos Calendar
   * para determinar si el tiempo de uno es anterior al tiempo del otro.
   * La documentacion de la clase Calendar no dice esto del metodo before,
   * pero dice que este metodo es equivalente a usar al metodo compareTo
   * de la siguiente manera: compareTo(when) < 0. El metodo compareTo(when)
   * utiliza el tiempo en milisegundos UTC desde la epoca (*) para
   * comparar el tiempo de dos objetos de tipo Calendar.
   * 
   * Estos son los motivos por los cuales digo que el metodo before
   * de la clase Calendar utiliza el tiempo en milisegundos UTC desde
   * la epoca de dos objetos Calendar para determinar si el tiempo de uno
   * es anterior al tiempo del otro, y por los cuales no se utiliza dicho
   * metodo para determinar si un registro de riego es del pasado o no.
   * 
   * (*) debido a que usa el metodo getTimeInMillis, el cual, retorna
   * el tiempo en milisegundos UTC desde la epoca (ver codigo fuente
   * de dicho metodo para comprobar que lo que estoy diciendo es correcto).
   * 
   * Este metodo es para evitar que el usuario modifique un registro de
   * riego del pasado (es decir, uno que tiene su fecha estrictamente
   * menor que la fecha actual).
   * 
   * @param userId
   * @param irrigationRecordId
   * @return true si el registro de riego con el ID dado y asociado
   * al usuario del ID dado es del pasado, en caso contrario false
   */
  public boolean isFromPast(int userId, int irrigationRecordId) {
    Calendar dateIrrigationRecord = findByUserId(userId, irrigationRecordId).getDate();

    /*
     * Si la fecha de un registro de riego es estrictamente menor a
     * la fecha actual, se retorna true como indicativo de que este
     * registro es del pasado
     */
    if (UtilDate.compareTo(dateIrrigationRecord, UtilDate.getCurrentDate()) < 0) {
      return true;
    }

    return false;
  }

  /**
   * @param parcelId
   * @return double que representa la cantidad total de agua
   * utilizada para el riego de un cultivo en la fecha actual
   * (es decir, hoy) [mm/dia]
   */
  public double calculateTotalAmountCropIrrigationWaterForCurrentDate(int parcelId) {
    Query query = entityManager.createQuery("SELECT SUM(i.irrigationDone) FROM IrrigationRecord i WHERE i.parcel.id = :parcelId AND i.crop IS NOT NULL AND i.date = :date");
    query.setParameter("parcelId", parcelId);
    query.setParameter("date", UtilDate.getCurrentDate());

    double result = 0.0;

    try {
      result = (double) query.getSingleResult();
    } catch (NullPointerException e) {
      e.printStackTrace();
    }

    return result;
  }

  /**
   * @param parcelId
   * @param date
   * @return double que representa la cantidad total de agua
   * utilizada para el riego de un cultivo en una fecha [mm/dia]
   */
  public double calculateTotalAmountCropIrrigationWaterForDate(int parcelId, Calendar date) {
    Query query = entityManager.createQuery("SELECT SUM(i.irrigationDone) FROM IrrigationRecord i WHERE i.parcel.id = :parcelId AND i.crop IS NOT NULL AND i.date = :date");
    query.setParameter("parcelId", parcelId);
    query.setParameter("date", date);

    double result = 0.0;

    try {
      result = (double) query.getSingleResult();
    } catch (NullPointerException e) {
      e.printStackTrace();
    }

    return result;
  }

  /**
   * @param parcelId
   * @param dateFrom
   * @param dateUntil
   * @return double que representa la cantidad total de agua
   * utilizada para el riego de cultivos sembrados y cosechados
   * en una parcela durante un periodo definido por dos fechas
   */
  public double calculateTotalAmountCropIrrigationWater(int parcelId, Calendar dateFrom, Calendar dateUntil) {
    /*
     * Selecciona la fecha de siembra y la fecha de cosecha de
     * un conjunto de registros de plantacion finalizados de una
     * parcela que cumplen con una de las siguientes condiciones:
     * - la fecha desde es mayor o igual a la fecha de siembra y
     * menor o igual a la fecha de cosecha, y la fecha hasta es
     * estrictamente mayor a la fecha de cosecha.
     * - la fecha de siembra es mayor o igual a la fecha desde y
     * la fecha de cosecha es menor o igual a la fecha hasta.
     * - la fecha hasta es mayor o igual a la fecha de siembra y
     * menor o igual a la fecha de cosecha, y la fecha desde es
     * estrictamente menor a la fecha de siembra.
     * 
     * El estado "Finalizado" tiene el ID 1, siempre y cuando no
     * se modifique el orden en el que se ejecutan las instrucciones
     * del archivo plantingRecordStatusInserts.sql de la ruta
     * app/etc/sql.
     */
    String dateSelectionQuery = "SELECT FK_PARCEL, SEED_DATE, HARVEST_DATE FROM PLANTING_RECORD "
        + "WHERE FK_PARCEL = ?1 AND FK_STATUS = 1 AND "
        + "((SEED_DATE <= ?2 AND ?2 <= HARVEST_DATE AND ?3 > HARVEST_DATE) "
        + "OR (SEED_DATE >= ?2 AND HARVEST_DATE <= ?3) "
        + "OR (SEED_DATE <= ?3 AND ?3 <= HARVEST_DATE AND ?2 < SEED_DATE)) "
        + "ORDER BY SEED_DATE";

    /*
     * Calcula la cantidad total de agua utilizada para el riego
     * de cultivos sembrados y cosechados en una parcela durante un
     * periodo definido por dos fechas. Hay que tener en cuenta que
     * esta consulta calcula dicha cantidad unicamente para cultivos
     * cosechados (finalizados), ya que la consulta de seleccion de
     * fechas de registros de plantacion selecciona las fechas de
     * registros de plantacion que tiene el estado "Finalizado", el
     * cual tiene el ID 1 siempre y cuando no se modifique el orden
     * de su sentencia INSERT en el archivo plantingRecordStatusInserts.sql
     * de la ruta app/etc/sql.
     */
    String queryString = "SELECT SUM(IRRIGATION_DONE) FROM IRRIGATION_RECORD "
        + "WHERE FK_PARCEL = ?1 AND ?2 <= DATE AND DATE <= ?3 "
        + "AND DATE IN (SELECT DISTINCT DATE FROM IRRIGATION_RECORD JOIN (" + dateSelectionQuery + ") AS DATES ON "
        + "IRRIGATION_RECORD.FK_PARCEL = DATES.FK_PARCEL WHERE "
        + "IRRIGATION_RECORD.FK_PARCEL = ?1 AND DATES.SEED_DATE <= DATE AND DATE <= DATES.HARVEST_DATE)";

    Query query = getEntityManager().createNativeQuery(queryString);
    query.setParameter(1, parcelId);
    query.setParameter(2, dateFrom);
    query.setParameter(3, dateUntil);

    double totalAmountCropIrrigationWater = 0.0;

    try {
      totalAmountCropIrrigationWater = (double) query.getSingleResult();
    } catch (NullPointerException e) {
      e.printStackTrace();
    }

    return totalAmountCropIrrigationWater;
  }

  /**
   * @param parcelId
   * @param cropId
   * @param dateFrom
   * @param dateUntil
   * @return double que representa la cantidad total de agua
   * utilizada para el riego de un cultivo en un periodo definido
   * por dos fechas
   */
  public double calculateAmounIrrigationWaterForCrop(int parcelId, int cropId, Calendar dateFrom, Calendar dateUntil) {
    Query query = entityManager.createQuery("SELECT SUM(i.irrigationDone) FROM IrrigationRecord i WHERE i.parcel.id = :parcelId AND i.crop.id = :cropId AND i.date >= :dateFrom AND i.date <= :dateUntil");
    query.setParameter("parcelId", parcelId);
    query.setParameter("cropId", cropId);
    query.setParameter("dateFrom", dateFrom);
    query.setParameter("dateUntil", dateUntil);

    double result = 0.0;

    try {
      result = (double) query.getSingleResult();
    } catch (NullPointerException e) {
      e.printStackTrace();
    }

    return result;
  }

  public Page<IrrigationRecord> findAllPagination(int userId, Integer page, Integer cantPerPage, Map<String, String> parameters) throws ParseException {
    SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
    Date date;
    Calendar calendarDate;

    // Genera el WHERE dinámicamente
    StringBuffer where = new StringBuffer(" WHERE 1=1 AND e.parcel.user.id = :userId");

    if (parameters != null) {

      for (String param : parameters.keySet()) {
        Method method;

        try {
          method = IrrigationRecord.class.getMethod("get" + capitalize(param));

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
            case "Crop":
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
    Query countQuery = entityManager.createQuery("SELECT COUNT(e.id) FROM " + IrrigationRecord.class.getSimpleName() + " e" + where.toString());
    countQuery.setParameter("userId", userId);

    // Pagina
    Query query = entityManager.createQuery("FROM " + IrrigationRecord.class.getSimpleName() + " e" + where.toString() + " ORDER BY e.date");
    query.setMaxResults(cantPerPage);
    query.setFirstResult((page - 1) * cantPerPage);
    query.setParameter("userId", userId);

    Integer count = ((Long) countQuery.getSingleResult()).intValue();
    Integer lastPage = (int) Math.ceil((double) count / (double) cantPerPage);

    // Arma la respuesta
    Page<IrrigationRecord> resultPage = new Page<IrrigationRecord>(page, count, page > 1 ? page - 1 : page, page < lastPage ? page + 1 : lastPage, lastPage, query.getResultList());
    return resultPage;
  }

  private String capitalize(final String line) {
    return Character.toUpperCase(line.charAt(0)) + line.substring(1);
  }

}
