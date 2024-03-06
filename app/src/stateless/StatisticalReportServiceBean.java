package stateless;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Map;
import java.util.Collection;
import java.util.ArrayList;
import java.util.List;
import java.util.Calendar;
import java.util.Date;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import model.PlantingRecord;
import model.StatisticalReport;
import util.UtilDate;

@Stateless
public class StatisticalReportServiceBean {

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

  public StatisticalReport create(StatisticalReport newStatisticalReport) {
    getEntityManager().persist(newStatisticalReport);
    return newStatisticalReport;
  }

  /**
   * Elimina fisicamente un informe estadistico perteneciente a una
   * parcela de un usuario
   * 
   * @param userId
   * @param statisticalReportId
   * @return referencia a un objeto de tipo StatisticalReport en
   * caso de eliminarse de la base de datos subyacente el informe
   * estadistico que tiene el ID dado y que esta asociado a una
   * parcela de un usuario que tiene el ID de usuario dado, en
   * caso contrario null
   */
  public StatisticalReport remove(int userId, int statisticalReportId) {
    StatisticalReport givenStatisticalReport = find(userId, statisticalReportId);

    if (givenStatisticalReport != null) {
      getEntityManager().remove(givenStatisticalReport);
      return givenStatisticalReport;
    }

    return null;
  }

  /**
   * Retorna un informe estadistico perteneciente a una de las
   * parcelas de un usuario si y solo si existe en la base
   * de datos subyacente
   * 
   * @param userId
   * @param statisticalReportId
   * @return referencia a un objeto de tipo StatisticalReport
   * que representa un informe estadistico en caso de existir
   * en la base de datos subyacente un informe estadistico con
   * el ID dado asociado a una parcela del usuario del ID dado.
   * En caso contrario, null.
   */
  public StatisticalReport find(int userId, int statisticalReportId) {
    Query query = getEntityManager().createQuery("SELECT s FROM StatisticalReport s JOIN s.parcel p WHERE (s.id = :statisticalReportId AND p IN (SELECT t FROM User u JOIN u.parcels t WHERE u.id = :userId))");
    query.setParameter("statisticalReportId", statisticalReportId);
    query.setParameter("userId", userId);

    StatisticalReport givenStatisticalReport = null;

    try {
      givenStatisticalReport = (StatisticalReport) query.getSingleResult();
    } catch (NoResultException e) {
      e.printStackTrace();
    }

    return givenStatisticalReport;
  }

  /**
   * Retorna los informes estadisticos de las parcelas de un
   * usuario
   * 
   * @param userId
   * @return referencia a un objeto de tipo Collection que
   * contiene los informes estadisticos de las parcelas
   * pertenecientes al usuario con el ID dado
   */
  public Collection<StatisticalReport> findAll(int userId) {
    Query query = getEntityManager().createQuery("SELECT s FROM StatisticalReport s JOIN s.parcel p WHERE p IN (SELECT t FROM User u JOIN u.parcels t WHERE u.id = :userId) ORDER BY s.id");
    query.setParameter("userId", userId);

    return (Collection) query.getResultList();
  }

  /**
   * Retorna los informes estadisticos de una parcela mediante
   * el nombre de una parcela y el ID del usuario al que pertenece
   * una parcela
   * 
   * @param userId
   * @param parcelName
   * @return referencia a un objeto de tipo Collection que
   * contiene los informes estadisticos de la parcela que tiene
   * el nombre dado y que pertenece al usuario con el ID dado
   */
  public Collection<StatisticalReport> findAllByParcelName(int userId, String parcelName) {
    Query query = getEntityManager().createQuery("SELECT s FROM StatisticalReport s JOIN s.parcel p WHERE (p.name = :parcelName AND p IN (SELECT t FROM User u JOIN u.parcels t WHERE u.id = :userId)) ORDER BY s.id");
    query.setParameter("userId", userId);
    query.setParameter("parcelName", parcelName);

    return (Collection) query.getResultList();
  }

  /**
   * @param userId
   * @param parcelId
   * @param dateFrom
   * @return referencia a un objeto de tipo Collection que
   * contiene todos los informes estadisticos de una parcela
   * que tienen una fecha desde mayor o igual a la fecha
   * desde elegida
   */
  public Collection<StatisticalReport> findAllByDateGreaterThanOrEqual(int userId, int parcelId, Calendar dateFrom) {
    Query query = getEntityManager().createQuery("SELECT s FROM StatisticalReport s JOIN s.parcel p WHERE (s.dateFrom >= :dateFrom AND p.id = :parcelId AND p IN (SELECT t FROM User u JOIN u.parcels t WHERE u.id = :userId)) ORDER BY s.dateFrom");
    query.setParameter("userId", userId);
    query.setParameter("parcelId", parcelId);
    query.setParameter("dateFrom", dateFrom);

    return (Collection) query.getResultList();
  }

  /**
   * @param userId
   * @param parcelId
   * @param dateUntil
   * @return referencia a un objeto de tipo Collection que
   * contiene todos los informes estadisticos de una parcela
   * que tienen una fecha menor o igual a la fecha hasta
   * elegida
   */
  public Collection<StatisticalReport> findAllByDateLessThanOrEqual(int userId, int parcelId, Calendar dateUntil) {
    Query query = getEntityManager().createQuery("SELECT s FROM StatisticalReport s JOIN s.parcel p WHERE (s.dateUntil <= :dateUntil AND p.id = :parcelId AND p IN (SELECT t FROM User u JOIN u.parcels t WHERE u.id = :userId)) ORDER BY s.dateUntil");
    query.setParameter("userId", userId);
    query.setParameter("parcelId", parcelId);
    query.setParameter("dateUntil", dateUntil);

    return (Collection) query.getResultList();
  }

  /**
   * @param userId
   * @param parcelId
   * @param dateFrom
   * @param dateUntil
   * @return referencia a un objeto de tipo Collection que
   * contiene todos los informes estadisticos de una parcela
   * que tienen una fecha desde mayor o igual a la fecha
   * desde elegida y una fecha hasta menor o igual a la
   * fecha hasta elegida
   */
  public Collection<StatisticalReport> findByAllFilterParameters(int userId, int parcelId, Calendar dateFrom, Calendar dateUntil) {
    Query query = getEntityManager().createQuery("SELECT s FROM StatisticalReport s JOIN s.parcel p WHERE (s.dateFrom >= :dateFrom AND s.dateUntil <= :dateUntil AND p.id = :parcelId AND p IN (SELECT t FROM User u JOIN u.parcels t WHERE u.id = :userId)) ORDER BY s.dateFrom");
    query.setParameter("userId", userId);
    query.setParameter("parcelId", parcelId);
    query.setParameter("dateFrom", dateFrom);
    query.setParameter("dateUntil", dateUntil);

    return (Collection) query.getResultList();
  }

  /**
   * @param userId
   * @param parcelId
   * @param dateFrom
   * @param dateUntil
   * @return referencia a un objeto de tipo StatisticalReport
   * si en la base de datos subyacente existe un informe
   * estadistico con una fecha desde y una fecha hasta
   * asociado a una parcela de un usuario
   */
  public StatisticalReport findByDates(int userId, int parcelId, Calendar dateFrom, Calendar dateUntil) {
    Query query = getEntityManager().createQuery("SELECT s FROM StatisticalReport s JOIN s.parcel p WHERE (s.dateFrom = :dateFrom AND s.dateUntil = :dateUntil AND p.id = :parcelId AND p IN (SELECT t FROM User u JOIN u.parcels t WHERE u.id = :userId)) ORDER BY s.id");
    query.setParameter("userId", userId);
    query.setParameter("parcelId", parcelId);
    query.setParameter("dateFrom", dateFrom);
    query.setParameter("dateUntil", dateUntil);

    StatisticalReport statisticalReport = null;

    try {
      statisticalReport = (StatisticalReport) query.getSingleResult();
    } catch (NoResultException e) {
      e.printStackTrace();
    }

    return statisticalReport;
  }

  /**
   * Retorna true si y solo si existe un informe estadistico
   * con una fecha desde y una fecha hasta asociado a una
   * parcela de un usuario
   * 
   * @param userId
   * @param parcelId
   * @param dateFrom
   * @param dateUntil
   * @return true si en la base de datos subyacente existe un
   * informe estadistico con una fecha desde y una fecha hasta
   * asociado a una parcela de un usuario. En caso contrario,
   * false.
   */
  public boolean checkExistence(int userId, int parcelId, Calendar dateFrom, Calendar dateUntil) {
    /*
     * Si una de las variables de tipo por referencia de tipo
     * Calendar tiene el valor null, significa que una de las
     * fechas NO esta definida. En este caso, se retorna false,
     * ya que realizar la busqueda de un informe estadistico con
     * una variable de tipo Calendar con el valor null es similar
     * a buscar un informe estadistico inexistente en la base
     * de datos subyacente.
     * 
     * Con este control se evita realizar una consulta a la base
     * de datos comparando una o ambas fechas con el valor null.
     * Si no se realiza este control y se realiza esta consulta
     * a la base de datos, ocurre la excepcion SQLSyntaxErrorException,
     * debido a que la comparacion de un atributo con el valor
     * null incumple la sintaxis del proveedor del motor de base
     * de datos.
     */
    if (dateFrom == null || dateUntil == null) {
      return false;
    }

    return (findByDates(userId, parcelId, dateFrom, dateUntil)  != null);
  }

  /**
   * Comprueba si un informe estadistico pertenece a un usuario
   * dado, mediante la relacion muchos a uno que hay entre los
   * modelos de datos StatisticalReport y Parcel.
   * 
   * Retorna true si y solo si un infrorme estadistico pertenece
   * a un usuario.
   * 
   * @param userId
   * @param statisticalReportId
   * @return true si se encuentra el informe estadistico con el
   * ID y el ID de usuario provistos, false en caso contrario
   */
  public boolean checkUserOwnership(int userId, int statisticalReportId) {
    return (find(userId, statisticalReportId) != null);
  }

  /**
   * Comprueba la existencia de un informe estadistico en la base
   * de datos subyacente. Retorna true si y solo si existe el
   * informe estadistico con el ID dado.
   * 
   * @param id
   * @return true si el informe estadistico con el ID dado existe
   * en la base de datos subyacente, false en caso contrario
   */
  public boolean checkExistence(int id) {
    return (getEntityManager().find(StatisticalReport.class, id) != null);
  }

  /**
   * @param cropNames
   * @param seedYears
   * @return referencia a un objeto de tipo List<String> que
   * contiene las etiquetas <cultivo> (<año>) para el grafico
   * de barras que lo requiera
   */
  public List<String> setLabelsWithCropAndYear(List<String> cropNames, List<Integer> seedYears) {
    List<String> labels = new ArrayList<>();

    for (int i = 0; i < cropNames.size(); i++) {
      labels.add(new String(cropNames.get(i) + " (" + seedYears.get(i) + ")"));
    }

    return labels;
  }

  /**
   * @param parcelId
   * @param dateFrom
   * @param dateUntil
   * @return referencia a un objeto de tipo List<Integer>
   * que contiene los ciclos de vida de los cultivos
   * sembrados en una parcela en un periodo definido por
   * dos fechas
   */
  public List<Integer> findLifeCyclesCropsPlantedPerPeriod(int parcelId, Calendar dateFrom, Calendar dateUntil) {
    /*
     * Con las condiciones de las fechas se seleccionan todos los
     * registros de plantacion finalizados (*) de una parcela que
     * estan entre una fecha desde y una fecha hasta.
     * 
     * Con la primera condicion se selecciona el registro de
     * plantacion finalizado (*) de una parcela que tiene su fecha
     * de siembra mayor o igual a la fecha desde y menor o igual
     * a la fecha hasta, y su fecha de cosecha estrictamente mayor
     * a la fecha hasta. Es decir, se selecciona el registro de
     * plantacion finalizado de una parcela que tiene unicamente
     * su fecha de siembra dentro del periodo definido por la
     * fecha desde y la fecha hasta.
     * 
     * Con la segunda condicion se selecciona el registro de
     * plantacion finalizado (*) de una parcela que tiene su fecha
     * de siembra mayor o igual a la fecha desde y su fecha de
     * cosecha menor o igual a la fecha hasta. Es decir, se
     * selecciona el registro de plantacion que tiene su fecha
     * de siembra y su fecha de cosecha dentro del periodo
     * definido por la fecha desde y la fecha hasta.
     * 
     * Con la tercera conidicon se selecciona el registro de
     * plantacion finalizado (*) de una parcela que tiene su
     * fecha de cosecha mayor o igual a la fecha desde y menor
     * igual a la fecha hasta, y su fecha de siembra estrictamente
     * menor a la fecha desde. Es decir, se selecciona el registro
     * de plantacion finalizado de una parcela que tiene unicamente
     * su fecha de cosecha dentro del periodo definido por la
     * fecha desde y la fecha hasta.
     * 
     * (*) El ID para el estado finalizado de un registro de
     * plantacion es el 1, siempre y cuando no se modifique el
     * orden en el que se ejecutan las instrucciones de insercion
     * del archivo plantingRecordStatusInserts.sql de la ruta
     * app/etc/sql.
     */
    String subQuery = "SELECT DISTINCT(NAME) AS CROP_NAME, LIFE_CYCLE FROM "
        + "CROP JOIN (SELECT SEED_DATE, FK_CROP AS CROP_ID FROM PLANTING_RECORD WHERE "
        + "FK_PARCEL = ?1 AND FK_STATUS = 1 AND "
        + "((?2 <= SEED_DATE AND SEED_DATE <= ?3 AND HARVEST_DATE > ?3) OR "
        + "(SEED_DATE >= ?2 AND HARVEST_DATE <= ?3) OR "
        + "(?2 <= HARVEST_DATE AND HARVEST_DATE <= ?3 AND SEED_DATE < ?2)) "
        + "ORDER BY SEED_DATE) AS RESULT_TABLE ON ID = RESULT_TABLE.CROP_ID";

    String stringQuery = "SELECT RESULT_TABLE_TWO.LIFE_CYCLE FROM (" + subQuery
        + ") AS RESULT_TABLE_TWO";

    Query query = getEntityManager().createNativeQuery(stringQuery);
    query.setParameter(1, parcelId);
    query.setParameter(2, dateFrom);
    query.setParameter(3, dateUntil);

    List<Integer> result = null;

    try {
      result = query.getResultList();
    } catch (NoResultException e) {
      e.printStackTrace();
    }

    return result;
  }

  /**
   * @param parcelId
   * @param dateFrom
   * @param dateUntil
   * @return referencia a un objeto de tipo List<String>
   * que contiene los ciclos de vida de los cultivos
   * sembrados en una parcela en un periodo definido por
   * dos fechas
   */
  public List<String> findNamesCropPlantedPerPeriod(int parcelId, Calendar dateFrom, Calendar dateUntil) {
    /*
     * Con las condiciones de las fechas se seleccionan todos los
     * registros de plantacion finalizados (*) de una parcela que
     * estan entre una fecha desde y una fecha hasta.
     * 
     * Con la primera condicion se selecciona el registro de
     * plantacion finalizado (*) de una parcela que tiene su fecha
     * de siembra mayor o igual a la fecha desde y menor o igual
     * a la fecha hasta, y su fecha de cosecha estrictamente mayor
     * a la fecha hasta. Es decir, se selecciona el registro de
     * plantacion finalizado de una parcela que tiene unicamente
     * su fecha de siembra dentro del periodo definido por la
     * fecha desde y la fecha hasta.
     * 
     * Con la segunda condicion se selecciona el registro de
     * plantacion finalizado (*) de una parcela que tiene su fecha
     * de siembra mayor o igual a la fecha desde y su fecha de
     * cosecha menor o igual a la fecha hasta. Es decir, se
     * selecciona el registro de plantacion que tiene su fecha
     * de siembra y su fecha de cosecha dentro del periodo
     * definido por la fecha desde y la fecha hasta.
     * 
     * Con la tercera conidicon se selecciona el registro de
     * plantacion finalizado (*) de una parcela que tiene su
     * fecha de cosecha mayor o igual a la fecha desde y menor
     * igual a la fecha hasta, y su fecha de siembra estrictamente
     * menor a la fecha desde. Es decir, se selecciona el registro
     * de plantacion finalizado de una parcela que tiene unicamente
     * su fecha de cosecha dentro del periodo definido por la
     * fecha desde y la fecha hasta.
     * 
     * (*) El ID para el estado finalizado de un registro de
     * plantacion es el 1, siempre y cuando no se modifique el
     * orden en el que se ejecutan las instrucciones de insercion
     * del archivo plantingRecordStatusInserts.sql de la ruta
     * app/etc/sql.
     */
    String subQuery = "SELECT DISTINCT(NAME) AS CROP_NAME, LIFE_CYCLE FROM "
        + "CROP JOIN (SELECT SEED_DATE, FK_CROP AS CROP_ID FROM PLANTING_RECORD WHERE "
        + "FK_PARCEL = ?1 AND FK_STATUS = 1 AND "
        + "((?2 <= SEED_DATE AND SEED_DATE <= ?3 AND HARVEST_DATE > ?3) OR "
        + "(SEED_DATE >= ?2 AND HARVEST_DATE <= ?3) OR "
        + "(?2 <= HARVEST_DATE AND HARVEST_DATE <= ?3 AND SEED_DATE < ?2)) "
        + "ORDER BY SEED_DATE) AS RESULT_TABLE ON ID = RESULT_TABLE.CROP_ID";

    String stringQuery = "SELECT RESULT_TABLE_TWO.CROP_NAME FROM (" + subQuery
        + ") AS RESULT_TABLE_TWO";

    Query query = getEntityManager().createNativeQuery(stringQuery);
    query.setParameter(1, parcelId);
    query.setParameter(2, dateFrom);
    query.setParameter(3, dateUntil);

    List<String> result = null;

    try {
      result = query.getResultList();
    } catch (NoResultException e) {
      e.printStackTrace();
    }

    return result;
  }

  /**
   * @param parcelId
   * @param dateFrom
   * @param dateUntil
   * @return referencia a un objeto de tipo List<Integer> que
   * contiene los valores que representan la cantidad total de
   * plantaciones por año que hubo en una parcela en un periodo
   * definido por dos fechas
   */
  public List<Integer> calculateTotalNumberPlantationsPerYear(int parcelId, Calendar dateFrom, Calendar dateUntil) {
    String subQuery = "SELECT RESULT_TABLE_TWO.SEED_YEAR, COUNT(RESULT_TABLE_TWO.CROP_ID) AS TOTAL_NUMBER_PLANTATIONS_PER_YEAR "
        + "FROM (SELECT YEAR(RESULT_TABLE.SEED_DATE) AS SEED_YEAR, RESULT_TABLE.CROP_ID FROM "
        + "(SELECT SEED_DATE, FK_CROP AS CROP_ID FROM PLANTING_RECORD "
        + "WHERE FK_PARCEL = ?1 AND FK_STATUS = 1 AND "
        + "((?2 <= SEED_DATE AND SEED_DATE <= ?3 AND HARVEST_DATE > ?3) OR "
        + "(SEED_DATE >= ?2 AND HARVEST_DATE <= ?3) "
        + "OR (?2 <= HARVEST_DATE AND HARVEST_DATE <= ?3 AND SEED_DATE < ?2)) "
        + "ORDER BY SEED_DATE) AS RESULT_TABLE) AS RESULT_TABLE_TWO "
        + "GROUP BY RESULT_TABLE_TWO.SEED_YEAR";

    String stringQuery = "SELECT RESULT_TABLE_THREE.TOTAL_NUMBER_PLANTATIONS_PER_YEAR FROM (" + subQuery
        + ") AS RESULT_TABLE_THREE";

    Query query = getEntityManager().createNativeQuery(stringQuery);
    query.setParameter(1, parcelId);
    query.setParameter(2, dateFrom);
    query.setParameter(3, dateUntil);

    List<Integer> result = null;

    try {
      result = query.getResultList();
    } catch (NoResultException e) {
      e.printStackTrace();
    }

    return result;
  }

  /**
   * @param parcelId
   * @param dateFrom
   * @param dateUntil
   * @return referencia a un objeto de tipo List<String> que
   * contiene los años para los que se calcula la cantidad
   * total de plantaciones por año sobre una parcela en un
   * periodo definido por dos fechas
   */
  public List<String> findYearsOfCalculationTotalNumberPlantationsPerYear(int parcelId, Calendar dateFrom, Calendar dateUntil) {
    String subQuery = "SELECT RESULT_TABLE_TWO.SEED_YEAR, COUNT(RESULT_TABLE_TWO.CROP_ID) AS TOTAL_NUMBER_PLANTATIONS_PER_YEAR "
        + "FROM (SELECT YEAR(RESULT_TABLE.SEED_DATE) AS SEED_YEAR, RESULT_TABLE.CROP_ID FROM "
        + "(SELECT SEED_DATE, FK_CROP AS CROP_ID FROM PLANTING_RECORD "
        + "WHERE FK_PARCEL = ?1 AND FK_STATUS = 1 AND "
        + "((?2 <= SEED_DATE AND SEED_DATE <= ?3 AND HARVEST_DATE > ?3) OR "
        + "(SEED_DATE >= ?2 AND HARVEST_DATE <= ?3) "
        + "OR (?2 <= HARVEST_DATE AND HARVEST_DATE <= ?3 AND SEED_DATE < ?2)) "
        + "ORDER BY SEED_DATE) AS RESULT_TABLE) AS RESULT_TABLE_TWO "
        + "GROUP BY RESULT_TABLE_TWO.SEED_YEAR";

    String stringQuery = "SELECT RESULT_TABLE_THREE.SEED_YEAR FROM (" + subQuery
        + ") AS RESULT_TABLE_THREE";

    Query query = getEntityManager().createNativeQuery(stringQuery);
    query.setParameter(1, parcelId);
    query.setParameter(2, dateFrom);
    query.setParameter(3, dateUntil);

    List<String> result = new ArrayList<>();

    try {

      for (Integer currentValue : (List<Integer>) query.getResultList()) {
        result.add(String.valueOf(currentValue));
      }

    } catch (NoResultException e) {
      e.printStackTrace();
    }

    return result;
  }

  /**
   * @param parcelId
   * @param dateFrom
   * @param dateUntil
   * @return referencia a un objeto de tipo List<Integer> que
   * contiene los valores que representan la cantidad total de
   * agua utilizada por año para el riego de cultivos en un
   * periodo definido por dos fechas
   */
  public List<Integer> calculateTotalAmountCropIrrigationWaterPerYear(int parcelId, Calendar dateFrom, Calendar dateUntil) {
    String subQuery = "SELECT RESULT_TABLE_TWO.IRRIGATION_YEAR, SUM(RESULT_TABLE_TWO.IRRIGATION_DONE) AS TOTAL_AMOUNT_CROP_IRRIGATION_WATER_PER_YEAR "
        + "FROM (SELECT YEAR(RESULT_TABLE.IRRIGATION_DATE) AS IRRIGATION_YEAR, RESULT_TABLE.IRRIGATION_DONE "
        + "FROM (SELECT DATE AS IRRIGATION_DATE, IRRIGATION_DONE FROM IRRIGATION_RECORD "
        + "WHERE FK_PARCEL = ?1 AND FK_CROP IS NOT NULL AND ?2 <= DATE AND DATE <= ?3 "
        + "ORDER BY DATE) AS RESULT_TABLE) AS RESULT_TABLE_TWO "
        + "GROUP BY RESULT_TABLE_TWO.IRRIGATION_YEAR";

    String stringQuery = "SELECT CAST(CEIL(RESULT_TABLE_THREE.TOTAL_AMOUNT_CROP_IRRIGATION_WATER_PER_YEAR) AS INTEGER) FROM (" + subQuery
        + ") AS RESULT_TABLE_THREE";

    Query query = getEntityManager().createNativeQuery(stringQuery);
    query.setParameter(1, parcelId);
    query.setParameter(2, dateFrom);
    query.setParameter(3, dateUntil);

    List<Integer> result = null;

    try {
      result = query.getResultList();
    } catch (NoResultException e) {
      e.printStackTrace();
    }

    return result;
  }

  /**
   * @param parcelId
   * @param dateFrom
   * @param dateUntil
   * @return referencia a un objeto de tipo List<String> que
   * contiene los años para los que se calcula la cantidad
   * total de agua utilizda para el riego de cultivos en un
   * periodo definido por dos fechas
   */
  public List<String> findYearsOfCalculationTotalAmountCropIrrigationWaterPerYear(int parcelId, Calendar dateFrom, Calendar dateUntil) {
    String subQuery = "SELECT RESULT_TABLE_TWO.IRRIGATION_YEAR, SUM(RESULT_TABLE_TWO.IRRIGATION_DONE) AS TOTAL_AMOUNT_CROP_IRRIGATION_WATER_PER_YEAR "
        + "FROM (SELECT YEAR(RESULT_TABLE.IRRIGATION_DATE) AS IRRIGATION_YEAR, RESULT_TABLE.IRRIGATION_DONE "
        + "FROM (SELECT DATE AS IRRIGATION_DATE, IRRIGATION_DONE FROM IRRIGATION_RECORD "
        + "WHERE FK_PARCEL = ?1 AND FK_CROP IS NOT NULL AND ?2 <= DATE AND DATE <= ?3 "
        + "ORDER BY DATE) AS RESULT_TABLE) AS RESULT_TABLE_TWO "
        + "GROUP BY RESULT_TABLE_TWO.IRRIGATION_YEAR";

    String stringQuery = "SELECT RESULT_TABLE_THREE.IRRIGATION_YEAR FROM (" + subQuery
        + ") AS RESULT_TABLE_THREE";


    Query query = getEntityManager().createNativeQuery(stringQuery);
    query.setParameter(1, parcelId);
    query.setParameter(2, dateFrom);
    query.setParameter(3, dateUntil);

    List<String> result = new ArrayList<>();

    try {

      for (Integer currentValue : (List<Integer>) query.getResultList()) {
        result.add(String.valueOf(currentValue));
      }

    } catch (NoResultException e) {
      e.printStackTrace();
    }

    return result;
  }

  /**
   * @param parcelId
   * @param dateFrom
   * @param dateUntil
   * @return referencia a un objeto de tipo List<Integer> que
   * contiene los valores que representan la cantidad toal de
   * cosecha por año en una parcela en un periodo definido
   * por dos fechas
   */
  public List<Integer> calculateTotalHarvestAmountPerYear(int parcelId, Calendar dateFrom, Calendar dateUntil) {
    String subQuery = "SELECT RESULT_TABLE_TWO.HARVEST_YEAR, SUM(RESULT_TABLE_TWO.HARVEST_AMOUNT) AS TOTAL_HARVEST_AMOUNT_PER_YEAR "
        + "FROM (SELECT YEAR(RESULT_TABLE.HARVEST_DATE) AS HARVEST_YEAR, RESULT_TABLE.HARVEST_AMOUNT FROM "
        + "(SELECT DATE AS HARVEST_DATE, HARVEST_AMOUNT FROM HARVEST WHERE "
        + "FK_PARCEL = ?1 AND ?2 <= DATE AND DATE <= ?3 "
        + "ORDER BY HARVEST_DATE) AS RESULT_TABLE) AS RESULT_TABLE_TWO "
        + "GROUP BY RESULT_TABLE_TWO.HARVEST_YEAR";

    String stringQuery = "SELECT CAST(CEIL(RESULT_TABLE_THREE.TOTAL_HARVEST_AMOUNT_PER_YEAR) AS INTEGER) FROM (" + subQuery
        + ") AS RESULT_TABLE_THREE";

    Query query = getEntityManager().createNativeQuery(stringQuery);
    query.setParameter(1, parcelId);
    query.setParameter(2, dateFrom);
    query.setParameter(3, dateUntil);

    List<Integer> result = null;

    try {
      result = query.getResultList();
    } catch (NoResultException e) {
      e.printStackTrace();
    }

    return result;
  }

  /**
   * @param parcelId
   * @param dateFrom
   * @param dateUntil
   * @return referencia a un objeto de tipo List<String> que
   * contiene los años para los que se calcula la cantidad
   * total de cosecha por año de una parcela en un periodo
   * definido por dos fechas
   */
  public List<String> findYearsOfCalculationTotalAmountHarvestPerYear(int parcelId, Calendar dateFrom, Calendar dateUntil) {
    String subQuery = "SELECT RESULT_TABLE_TWO.HARVEST_YEAR, SUM(RESULT_TABLE_TWO.HARVEST_AMOUNT) AS TOTAL_HARVEST_AMOUNT_PER_YEAR "
        + "FROM (SELECT YEAR(RESULT_TABLE.HARVEST_DATE) AS HARVEST_YEAR, RESULT_TABLE.HARVEST_AMOUNT FROM "
        + "(SELECT DATE AS HARVEST_DATE, HARVEST_AMOUNT FROM HARVEST WHERE "
        + "FK_PARCEL = ?1 AND ?2 <= DATE AND DATE <= ?3 "
        + "ORDER BY HARVEST_DATE) AS RESULT_TABLE) AS RESULT_TABLE_TWO "
        + "GROUP BY RESULT_TABLE_TWO.HARVEST_YEAR";

    String stringQuery = "SELECT RESULT_TABLE_THREE.HARVEST_YEAR FROM (" + subQuery
        + ") AS RESULT_TABLE_THREE";

    Query query = getEntityManager().createNativeQuery(stringQuery);
    query.setParameter(1, parcelId);
    query.setParameter(2, dateFrom);
    query.setParameter(3, dateUntil);

    List<String> result = new ArrayList<>();

    try {

      for (Integer currentValue : (List<Integer>) query.getResultList()) {
        result.add(String.valueOf(currentValue));
      }

    } catch (NoResultException e) {
      e.printStackTrace();
    }

    return result;
  }

  /**
   * @param parcelId
   * @param dateFrom
   * @param dateUntil
   * @return referencia a un objeto de tipo List<Integer> que
   * contiene los valores que representan la cantidad total de
   * agua de lluvia que cayo por año sobre una parcela en un
   * periodo definido por dos fechas
   */
  public List<Integer> calculateTotalAmountRainwaterPerYear(int parcelId, Calendar dateFrom, Calendar dateUntil) {
    String subQuery = "SELECT RESULT_TABLE_TWO.YEAR_DATE_CLIMATE_RECORD, SUM(RESULT_TABLE_TWO.PRECIP) AS TOTAL_PRECIP_PER_YEAR "
        + "FROM (SELECT YEAR(RESULT_TABLE.DATE_CLIMATE_RECORD) AS YEAR_DATE_CLIMATE_RECORD, RESULT_TABLE.PRECIP "
        + "FROM (SELECT DATE AS DATE_CLIMATE_RECORD, PRECIP FROM CLIMATE_RECORD "
        + "WHERE FK_PARCEL = ?1 AND ?2 <= DATE AND DATE <= ?3 AND (TYPE_PRECIP_ONE = 1 "
        + "OR TYPE_PRECIP_TWO = 1 OR TYPE_PRECIP_THREE = 1 OR TYPE_PRECIP_FOUR = 1) "
        + "ORDER BY DATE) AS RESULT_TABLE) AS RESULT_TABLE_TWO "
        + "GROUP BY RESULT_TABLE_TWO.YEAR_DATE_CLIMATE_RECORD";

    String stringQuery = "SELECT CAST(CEIL(RESULT_TABLE_THREE.TOTAL_PRECIP_PER_YEAR) AS INTEGER) FROM (" + subQuery
        + ") AS RESULT_TABLE_THREE";

    Query query = getEntityManager().createNativeQuery(stringQuery);
    query.setParameter(1, parcelId);
    query.setParameter(2, dateFrom);
    query.setParameter(3, dateUntil);

    List<Integer> result = null;

    try {
      result = query.getResultList();
    } catch (NoResultException e) {
      e.printStackTrace();
    }

    return result;
  }

  /**
   * @param parcelId
   * @param dateFrom
   * @param dateUntil
   * @return referencia a un objeto de tipo List<String> que
   * contiene los años para los que se calcula la cantidad
   * total de agua lluvia que cayo sobre una parcela en un
   * periodo definido por dos fechas
   */
  public List<String> findYearsOfCalculationTotalAmountRainwater(int parcelId, Calendar dateFrom, Calendar dateUntil) {
    String subQuery = "SELECT RESULT_TABLE_TWO.YEAR_DATE_CLIMATE_RECORD, SUM(RESULT_TABLE_TWO.PRECIP) AS TOTAL_PRECIP_PER_YEAR "
        + "FROM (SELECT YEAR(RESULT_TABLE.DATE_CLIMATE_RECORD) AS YEAR_DATE_CLIMATE_RECORD, RESULT_TABLE.PRECIP "
        + "FROM (SELECT DATE AS DATE_CLIMATE_RECORD, PRECIP FROM CLIMATE_RECORD "
        + "WHERE FK_PARCEL = ?1 AND ?2 <= DATE AND DATE <= ?3 AND (TYPE_PRECIP_ONE = 1 "
        + "OR TYPE_PRECIP_TWO = 1 OR TYPE_PRECIP_THREE = 1 OR TYPE_PRECIP_FOUR = 1) "
        + "ORDER BY DATE) AS RESULT_TABLE) AS RESULT_TABLE_TWO "
        + "GROUP BY RESULT_TABLE_TWO.YEAR_DATE_CLIMATE_RECORD";

    String stringQuery = "SELECT RESULT_TABLE_THREE.YEAR_DATE_CLIMATE_RECORD FROM (" + subQuery
        + ") AS RESULT_TABLE_THREE";

    Query query = getEntityManager().createNativeQuery(stringQuery);
    query.setParameter(1, parcelId);
    query.setParameter(2, dateFrom);
    query.setParameter(3, dateUntil);

    List<String> result = new ArrayList<>();

    try {

      for (Integer currentValue : (List<Integer>) query.getResultList()) {
        result.add(String.valueOf(currentValue));
      }

    } catch (NoResultException e) {
      e.printStackTrace();
    }

    return result;
  }

  /**
   * @param parcelId
   * @param dateFrom
   * @param dateUntil
   * @return double que representa la cantidad total de agua
   * de lluvia que cayo sobre una parcela en un periodo
   * definido por dos fechas
   */
  public double calculateTotalAmountRainwaterPerPeriod(int parcelId, Calendar dateFrom, Calendar dateUntil) {
    /*
     * El ID del tipo de precipitacion correspondiente a la lluvia
     * es igual 1, siempre y cuando no se modifique el orden en el
     * que se ejecutan las instrucciones del archivo typePrecipInserts.sql
     */
    String stringQuery = "SELECT SUM(PRECIP) AS TOTAL_AMOUNT_RAINWATER FROM CLIMATE_RECORD WHERE "
        + "FK_PARCEL = ?1 AND ?2 <= DATE AND DATE <= ?3 AND "
        + "(TYPE_PRECIP_ONE = 1 OR TYPE_PRECIP_TWO = 1 OR TYPE_PRECIP_THREE = 1 OR TYPE_PRECIP_FOUR = 1)";

    Query query = getEntityManager().createNativeQuery(stringQuery);
    query.setParameter(1, parcelId);
    query.setParameter(2, dateFrom);
    query.setParameter(3, dateUntil);

    return (double) query.getSingleResult();
  }

  /**
   * @param parcelId
   * @param dateFrom
   * @param dateUntil
   * @return referencia a un objeto de tipo Integer que contiene
   * el valor que representa el promedio del agua de lluvia que
   * cayo sobre una parcela en un periodo definido por dos fechas
   */
  public Integer calculateAverageRainwaterPerPeriod(int parcelId, Calendar dateFrom, Calendar dateUntil) {
    /*
     * El ID del tipo de precipitacion correspondiente a la lluvia
     * es igual 1, siempre y cuando no se modifique el orden en el
     * que se ejecutan las instrucciones del archivo typePrecipInserts.sql
     */
    String stringQuery = "SELECT CAST(CEIL(AVG(PRECIP)) AS INTEGER) FROM CLIMATE_RECORD WHERE "
        + "FK_PARCEL = ?1 AND ?2 <= DATE AND DATE <= ?3 AND "
        + "(TYPE_PRECIP_ONE = 1 OR TYPE_PRECIP_TWO = 1 OR TYPE_PRECIP_THREE = 1 OR TYPE_PRECIP_FOUR = 1)";

    Query query = getEntityManager().createNativeQuery(stringQuery);
    query.setParameter(1, parcelId);
    query.setParameter(2, dateFrom);
    query.setParameter(3, dateUntil);

    return (Integer) query.getSingleResult();
  }

  /*
   * ********************************************************
   * A partir de aqui comienzan los metodos relacionados a la
   * generacion de informes estadisticos que se tratan sobre
   * la cantidad de plantaciones por cultivo
   * ********************************************************
   */

  /**
   * @param parcelId
   * @param dateFrom
   * @param dateUntil
   * @return referencia a un objeto de tipo List<Integer> que
   * contiene los numeros que representan la cantidad total de
   * veces que se plantaron los cultivos en una parcela en un
   * periodo definido por dos fechas
   */
  public List<Integer> calculateTotalNumberPlantationsPerCrop(int parcelId, Calendar dateFrom, Calendar dateUntil) {
    /*
     * Con las condiciones de las fechas se seleccionan todos los
     * registros de plantacion finalizados (*) de una parcela que
     * estan entre una fecha desde y una fecha hasta.
     * 
     * Con la primera condicion se selecciona el registro de
     * plantacion finalizado (*) de una parcela que tiene su fecha
     * de siembra mayor o igual a la fecha desde y menor o igual
     * a la fecha hasta, y su fecha de cosecha estrictamente mayor
     * a la fecha hasta. Es decir, se selecciona el registro de
     * plantacion finalizado de una parcela que tiene unicamente
     * su fecha de siembra dentro del periodo definido por la
     * fecha desde y la fecha hasta.
     * 
     * Con la segunda condicion se selecciona el registro de
     * plantacion finalizado (*) de una parcela que tiene su fecha
     * de siembra mayor o igual a la fecha desde y su fecha de
     * cosecha menor o igual a la fecha hasta. Es decir, se
     * selecciona el registro de plantacion que tiene su fecha
     * de siembra y su fecha de cosecha dentro del periodo
     * definido por la fecha desde y la fecha hasta.
     * 
     * Con la tercera conidicon se selecciona el registro de
     * plantacion finalizado (*) de una parcela que tiene su
     * fecha de cosecha mayor o igual a la fecha desde y menor
     * igual a la fecha hasta, y su fecha de siembra estrictamente
     * menor a la fecha desde. Es decir, se selecciona el registro
     * de plantacion finalizado de una parcela que tiene unicamente
     * su fecha de cosecha dentro del periodo definido por la
     * fecha desde y la fecha hasta.
     * 
     * (*) El ID para el estado finalizado de un registro de
     * plantacion es el 1, siempre y cuando no se modifique el
     * orden en el que se ejecutan las instrucciones de insercion
     * del archivo plantingRecordStatusInserts.sql de la ruta
     * app/etc/sql.
     */
    String subQuery = "SELECT FK_CROP, COUNT(FK_CROP) AS NUMBER_PLANTATIONS FROM PLANTING_RECORD "
        + "WHERE FK_PARCEL = ?1 AND FK_STATUS = 1 AND "
        + "((?2 <= SEED_DATE AND SEED_DATE <= ?3 AND HARVEST_DATE > ?3) OR "
        + "(SEED_DATE >= ?2 AND HARVEST_DATE <= ?3) OR "
        + "(?2 <= HARVEST_DATE AND HARVEST_DATE <= ?3 AND SEED_DATE < ?2)) "
        + "GROUP BY FK_CROP";

    String stringQuery = "SELECT RESULT_TABLE.NUMBER_PLANTATIONS FROM (" + subQuery
        + ") AS RESULT_TABLE";

    Query query = getEntityManager().createNativeQuery(stringQuery);
    query.setParameter(1, parcelId);
    query.setParameter(2, dateFrom);
    query.setParameter(3, dateUntil);

    List<Integer> result = null;

    try {
      result = query.getResultList();
    } catch (NoResultException e) {
      e.printStackTrace();
    }

    return result;
  }

  /**
   * @param parcelId
   * @param dateFrom
   * @param dateUntil
   * @return referencia a un objeto de tipo List<String> que
   * contiene los nombres de los cultivos para los que se
   * calcula la cantidad total de veces que se plantaron en
   * una parcela en un periodo definido por dos fechas
   */
  public List<String> findCropNamesCalculatedPerTotalNumberPlantationsPerCrop(int parcelId, Calendar dateFrom, Calendar dateUntil) {
    /*
     * Con las condiciones de las fechas se seleccionan todos los
     * registros de plantacion finalizados (*) de una parcela que
     * estan entre una fecha desde y una fecha hasta.
     * 
     * Con la primera condicion se selecciona el registro de
     * plantacion finalizado (*) de una parcela que tiene su fecha
     * de siembra mayor o igual a la fecha desde y menor o igual
     * a la fecha hasta, y su fecha de cosecha estrictamente mayor
     * a la fecha hasta. Es decir, se selecciona el registro de
     * plantacion finalizado de una parcela que tiene unicamente
     * su fecha de siembra dentro del periodo definido por la
     * fecha desde y la fecha hasta.
     * 
     * Con la segunda condicion se selecciona el registro de
     * plantacion finalizado (*) de una parcela que tiene su fecha
     * de siembra mayor o igual a la fecha desde y su fecha de
     * cosecha menor o igual a la fecha hasta. Es decir, se
     * selecciona el registro de plantacion que tiene su fecha
     * de siembra y su fecha de cosecha dentro del periodo
     * definido por la fecha desde y la fecha hasta.
     * 
     * Con la tercera conidicon se selecciona el registro de
     * plantacion finalizado (*) de una parcela que tiene su
     * fecha de cosecha mayor o igual a la fecha desde y menor
     * igual a la fecha hasta, y su fecha de siembra estrictamente
     * menor a la fecha desde. Es decir, se selecciona el registro
     * de plantacion finalizado de una parcela que tiene unicamente
     * su fecha de cosecha dentro del periodo definido por la
     * fecha desde y la fecha hasta.
     * 
     * (*) El ID para el estado finalizado de un registro de
     * plantacion es el 1, siempre y cuando no se modifique el
     * orden en el que se ejecutan las instrucciones de insercion
     * del archivo plantingRecordStatusInserts.sql de la ruta
     * app/etc/sql.
     */
    String subQuery = "SELECT FK_CROP, COUNT(FK_CROP) AS NUMBER_PLANTATIONS FROM PLANTING_RECORD "
        + "WHERE FK_PARCEL = ?1 AND FK_STATUS = 1 AND "
        + "((?2 <= SEED_DATE AND SEED_DATE <= ?3 AND HARVEST_DATE > ?3) OR "
        + "(SEED_DATE >= ?2 AND HARVEST_DATE <= ?3) OR "
        + "(?2 <= HARVEST_DATE AND HARVEST_DATE <= ?3 AND SEED_DATE < ?2)) "
        + "GROUP BY FK_CROP";

    String stringQuery = "SELECT NAME FROM CROP JOIN (" + subQuery
        + ") AS RESULT_TABLE ON CROP.ID = RESULT_TABLE.FK_CROP";

    Query query = getEntityManager().createNativeQuery(stringQuery);
    query.setParameter(1, parcelId);
    query.setParameter(2, dateFrom);
    query.setParameter(3, dateUntil);

    List<String> result = null;

    try {
      result = query.getResultList();
    } catch (NoResultException e) {
      e.printStackTrace();
    }

    return result;
  }

  /**
   * @param parcelId
   * @param dateFrom
   * @param dateUntil
   * @return referencia a un objeto de tipo List<Integer> que
   * contiene los numeros que representan la cantidad total
   * de veces que se plantaron los cultivos por año en una
   * parcela en un periodo definido por dos fechas
   */
  public List<Integer> calculateTotalNumberPlantationsPerCropAndYear(int parcelId, Calendar dateFrom, Calendar dateUntil) {
    /*
     * Con las condiciones de las fechas se seleccionan todos los
     * registros de plantacion finalizados (*) de una parcela que
     * estan entre una fecha desde y una fecha hasta.
     * 
     * Con la primera condicion se selecciona el registro de
     * plantacion finalizado (*) de una parcela que tiene su fecha
     * de siembra mayor o igual a la fecha desde y menor o igual
     * a la fecha hasta, y su fecha de cosecha estrictamente mayor
     * a la fecha hasta. Es decir, se selecciona el registro de
     * plantacion finalizado de una parcela que tiene unicamente
     * su fecha de siembra dentro del periodo definido por la
     * fecha desde y la fecha hasta.
     * 
     * Con la segunda condicion se selecciona el registro de
     * plantacion finalizado (*) de una parcela que tiene su fecha
     * de siembra mayor o igual a la fecha desde y su fecha de
     * cosecha menor o igual a la fecha hasta. Es decir, se
     * selecciona el registro de plantacion que tiene su fecha
     * de siembra y su fecha de cosecha dentro del periodo
     * definido por la fecha desde y la fecha hasta.
     * 
     * Con la tercera conidicon se selecciona el registro de
     * plantacion finalizado (*) de una parcela que tiene su
     * fecha de cosecha mayor o igual a la fecha desde y menor
     * igual a la fecha hasta, y su fecha de siembra estrictamente
     * menor a la fecha desde. Es decir, se selecciona el registro
     * de plantacion finalizado de una parcela que tiene unicamente
     * su fecha de cosecha dentro del periodo definido por la
     * fecha desde y la fecha hasta.
     * 
     * (*) El ID para el estado finalizado de un registro de
     * plantacion es el 1, siempre y cuando no se modifique el
     * orden en el que se ejecutan las instrucciones de insercion
     * del archivo plantingRecordStatusInserts.sql de la ruta
     * app/etc/sql.
     */
    String subQuery = "SELECT RESULT_TABLE_TWO.YEAR_SEED_DATE, RESULT_TABLE_TWO.CROP_ID, COUNT(RESULT_TABLE_TWO.CROP_ID) AS NUMBER_PLANTATIONS FROM "
        + "(SELECT YEAR(RESULT_TABLE.SEED_DATE) AS YEAR_SEED_DATE, RESULT_TABLE.CROP_ID FROM "
        + "(SELECT SEED_DATE, FK_CROP AS CROP_ID FROM "
        + "PLANTING_RECORD "
        + "WHERE FK_PARCEL = ?1 AND FK_STATUS = 1 AND ((?2 <= SEED_DATE AND SEED_DATE <= ?3 AND HARVEST_DATE > ?3) "
        + "OR (SEED_DATE >= ?2 AND HARVEST_DATE <= ?3) "
        + "OR (?2 <= HARVEST_DATE AND HARVEST_DATE <= ?3 AND SEED_DATE < ?2)) "
        + "ORDER BY SEED_DATE) AS RESULT_TABLE) AS RESULT_TABLE_TWO "
        + "GROUP BY RESULT_TABLE_TWO.YEAR_SEED_DATE, RESULT_TABLE_TWO.CROP_ID";

    String stringQuery = "SELECT RESULT_TABLE_THREE.NUMBER_PLANTATIONS FROM (" + subQuery + ") AS RESULT_TABLE_THREE";

    Query query = getEntityManager().createNativeQuery(stringQuery);
    query.setParameter(1, parcelId);
    query.setParameter(2, dateFrom);
    query.setParameter(3, dateUntil);

    List<Integer> result = null;

    try {
      result = query.getResultList();
    } catch (NoResultException e) {
      e.printStackTrace();
    }

    return result;
  }

  /**
   * @param parcelId
   * @param dateFrom
   * @param dateUntil
   * @return referencia a un objeto de tipo List<String> que
   * contiene los nombres de los cultivos para los que se calcula
   * la cantidad total de veces que se plantaron por año en
   * una parcela en un periodo definido por dos fechas
   */
  public List<String> findCropNamesCalculatedPerTotalNumberPlantationsPerCropAndYear(int parcelId, Calendar dateFrom, Calendar dateUntil) {
    /*
     * Con las condiciones de las fechas se seleccionan todos los
     * registros de plantacion finalizados (*) de una parcela que
     * estan entre una fecha desde y una fecha hasta.
     * 
     * Con la primera condicion se selecciona el registro de
     * plantacion finalizado (*) de una parcela que tiene su fecha
     * de siembra mayor o igual a la fecha desde y menor o igual
     * a la fecha hasta, y su fecha de cosecha estrictamente mayor
     * a la fecha hasta. Es decir, se selecciona el registro de
     * plantacion finalizado de una parcela que tiene unicamente
     * su fecha de siembra dentro del periodo definido por la
     * fecha desde y la fecha hasta.
     * 
     * Con la segunda condicion se selecciona el registro de
     * plantacion finalizado (*) de una parcela que tiene su fecha
     * de siembra mayor o igual a la fecha desde y su fecha de
     * cosecha menor o igual a la fecha hasta. Es decir, se
     * selecciona el registro de plantacion que tiene su fecha
     * de siembra y su fecha de cosecha dentro del periodo
     * definido por la fecha desde y la fecha hasta.
     * 
     * Con la tercera conidicon se selecciona el registro de
     * plantacion finalizado (*) de una parcela que tiene su
     * fecha de cosecha mayor o igual a la fecha desde y menor
     * igual a la fecha hasta, y su fecha de siembra estrictamente
     * menor a la fecha desde. Es decir, se selecciona el registro
     * de plantacion finalizado de una parcela que tiene unicamente
     * su fecha de cosecha dentro del periodo definido por la
     * fecha desde y la fecha hasta.
     * 
     * (*) El ID para el estado finalizado de un registro de
     * plantacion es el 1, siempre y cuando no se modifique el
     * orden en el que se ejecutan las instrucciones de insercion
     * del archivo plantingRecordStatusInserts.sql de la ruta
     * app/etc/sql.
     */
    String subQuery = "SELECT RESULT_TABLE_TWO.YEAR_SEED_DATE, RESULT_TABLE_TWO.CROP_ID, COUNT(RESULT_TABLE_TWO.CROP_ID) AS NUMBER_PLANTATIONS FROM "
        + "(SELECT YEAR(RESULT_TABLE.SEED_DATE) AS YEAR_SEED_DATE, RESULT_TABLE.CROP_ID FROM "
        + "(SELECT SEED_DATE, FK_CROP AS CROP_ID FROM "
        + "PLANTING_RECORD "
        + "WHERE FK_PARCEL = ?1 AND FK_STATUS = 1 AND ((?2 <= SEED_DATE AND SEED_DATE <= ?3 AND HARVEST_DATE > ?3) "
        + "OR (SEED_DATE >= ?2 AND HARVEST_DATE <= ?3) "
        + "OR (?2 <= HARVEST_DATE AND HARVEST_DATE <= ?3 AND SEED_DATE < ?2)) "
        + "ORDER BY SEED_DATE) AS RESULT_TABLE) AS RESULT_TABLE_TWO "
        + "GROUP BY RESULT_TABLE_TWO.YEAR_SEED_DATE, RESULT_TABLE_TWO.CROP_ID";

    String stringQuery = "SELECT NAME FROM CROP JOIN (" + subQuery
        + ") AS RESULT_TABLE_THREE ON CROP.ID = RESULT_TABLE_THREE.CROP_ID";

    Query query = getEntityManager().createNativeQuery(stringQuery);
    query.setParameter(1, parcelId);
    query.setParameter(2, dateFrom);
    query.setParameter(3, dateUntil);

    List<String> result = null;

    try {
      result = query.getResultList();
    } catch (NoResultException e) {
      e.printStackTrace();
    }

    return result;
  }

  /**
   * @param parcelId
   * @param dateFrom
   * @param dateUntil
   * @return referencia a un objeto de tipo List<Integer> que
   * contiene los años en los que se sembraron los cultivos
   * para los que se calcula la cantidad total de veces que se
   * plantaron por año en una parcela en un periodo definido
   * por dos fechas
   */
  public List<Integer> findYearsCalculatedPerTotalNumberPlantationsPerCropAndYear(int parcelId, Calendar dateFrom, Calendar dateUntil) {
    /*
     * Con las condiciones de las fechas se seleccionan todos los
     * registros de plantacion finalizados (*) de una parcela que
     * estan entre una fecha desde y una fecha hasta.
     * 
     * Con la primera condicion se selecciona el registro de
     * plantacion finalizado (*) de una parcela que tiene su fecha
     * de siembra mayor o igual a la fecha desde y menor o igual
     * a la fecha hasta, y su fecha de cosecha estrictamente mayor
     * a la fecha hasta. Es decir, se selecciona el registro de
     * plantacion finalizado de una parcela que tiene unicamente
     * su fecha de siembra dentro del periodo definido por la
     * fecha desde y la fecha hasta.
     * 
     * Con la segunda condicion se selecciona el registro de
     * plantacion finalizado (*) de una parcela que tiene su fecha
     * de siembra mayor o igual a la fecha desde y su fecha de
     * cosecha menor o igual a la fecha hasta. Es decir, se
     * selecciona el registro de plantacion que tiene su fecha
     * de siembra y su fecha de cosecha dentro del periodo
     * definido por la fecha desde y la fecha hasta.
     * 
     * Con la tercera conidicon se selecciona el registro de
     * plantacion finalizado (*) de una parcela que tiene su
     * fecha de cosecha mayor o igual a la fecha desde y menor
     * igual a la fecha hasta, y su fecha de siembra estrictamente
     * menor a la fecha desde. Es decir, se selecciona el registro
     * de plantacion finalizado de una parcela que tiene unicamente
     * su fecha de cosecha dentro del periodo definido por la
     * fecha desde y la fecha hasta.
     * 
     * (*) El ID para el estado finalizado de un registro de
     * plantacion es el 1, siempre y cuando no se modifique el
     * orden en el que se ejecutan las instrucciones de insercion
     * del archivo plantingRecordStatusInserts.sql de la ruta
     * app/etc/sql.
     */
    String subQuery = "SELECT RESULT_TABLE_TWO.YEAR_SEED_DATE, RESULT_TABLE_TWO.CROP_ID, COUNT(RESULT_TABLE_TWO.CROP_ID) AS NUMBER_PLANTATIONS FROM "
        + "(SELECT YEAR(RESULT_TABLE.SEED_DATE) AS YEAR_SEED_DATE, RESULT_TABLE.CROP_ID FROM "
        + "(SELECT SEED_DATE, FK_CROP AS CROP_ID FROM "
        + "PLANTING_RECORD "
        + "WHERE FK_PARCEL = ?1 AND FK_STATUS = 1 AND ((?2 <= SEED_DATE AND SEED_DATE <= ?3 AND HARVEST_DATE > ?3) "
        + "OR (SEED_DATE >= ?2 AND HARVEST_DATE <= ?3) "
        + "OR (?2 <= HARVEST_DATE AND HARVEST_DATE <= ?3 AND SEED_DATE < ?2)) "
        + "ORDER BY SEED_DATE) AS RESULT_TABLE) AS RESULT_TABLE_TWO "
        + "GROUP BY RESULT_TABLE_TWO.YEAR_SEED_DATE, RESULT_TABLE_TWO.CROP_ID";

    String stringQuery = "SELECT RESULT_TABLE_THREE.YEAR_SEED_DATE FROM (" + subQuery + ") AS RESULT_TABLE_THREE";

    Query query = getEntityManager().createNativeQuery(stringQuery);
    query.setParameter(1, parcelId);
    query.setParameter(2, dateFrom);
    query.setParameter(3, dateUntil);

    List<Integer> result = null;

    try {
      result = query.getResultList();
    } catch (NoResultException e) {
      e.printStackTrace();
    }

    return result;
  }

  /**
   * @param parcelId
   * @param dateFrom
   * @param dateUntil
   * @return entero que representa la cantidad total de
   * plantaciones que se hicieron en una parcela en un
   * periodo definido por dos fechas
   */
  public Long calculateTotalNumberPlantationsPerPeriod(int parcelId, Calendar dateFrom, Calendar dateUntil) {
    /*
     * Con esta condicion se seleccionan todos los registros de
     * plantacion finalizados (*) de una parcela que estan entre
     * una fecha desde y una fecha hasta.
     * 
     * Con la primera condicion se selecciona el registro de
     * plantacion finalizado (*) de una parcela que tiene su fecha
     * de siembra mayor o igual a la fecha desde y menor o igual
     * a la fecha hasta, y su fecha de cosecha estrictamente mayor
     * a la fecha hasta. Es decir, se selecciona el registro de
     * plantacion finalizado de una parcela que tiene unicamente
     * su fecha de siembra dentro del periodo definido por la
     * fecha desde y la fecha hasta.
     * 
     * Con la segunda condicion se selecciona el registro de
     * plantacion finalizado (*) de una parcela que tiene su fecha
     * de siembra mayor o igual a la fecha desde y su fecha de
     * cosecha menor o igual a la fecha hasta. Es decir, se
     * selecciona el registro de plantacion que tiene su fecha
     * de siembra y su fecha de cosecha dentro del periodo
     * definido por la fecha desde y la fecha hasta.
     * 
     * Con la tercera conidicon se selecciona el registro de
     * plantacion finalizado (*) de una parcela que tiene su
     * fecha de cosecha mayor o igual a la fecha desde y menor
     * igual a la fecha hasta, y su fecha de siembra estrictamente
     * menor a la fecha desde. Es decir, se selecciona el registro
     * de plantacion finalizado de una parcela que tiene unicamente
     * su fecha de cosecha dentro del periodo definido por la
     * fecha desde y la fecha hasta.
     * 
     * (*) El ID para el estado finalizado de un registro de
     * plantacion es el 1, siempre y cuando no se modifique el
     * orden en el que se ejecutan las instrucciones de insercion
     * del archivo plantingRecordStatusInserts.sql de la ruta
     * app/etc/sql.
     */
    String subCondition = "(:dateFrom <= r.seedDate AND r.seedDate <= :dateUntil AND r.harvestDate > :dateUntil) OR "
        + "(r.seedDate >= :dateFrom AND r.harvestDate <= :dateUntil) OR "
        + "(:dateFrom <= r.harvestDate AND r.harvestDate <= :dateUntil AND r.seedDate < :dateFrom)";

    String stringQuery = "SELECT COUNT(r.id) FROM PlantingRecord r WHERE r.parcel.id = :parcelId AND r.status.id = 1 AND (" + subCondition + ")";

    Query query = getEntityManager().createQuery(stringQuery);
    query.setParameter("parcelId", parcelId);
    query.setParameter("dateFrom", dateFrom);
    query.setParameter("dateUntil", dateUntil);

    return (Long) query.getSingleResult();
  }

  /*
   * ******************************************************
   * A partir de aqui comienzan los metodos relacionados con
   * la generacion de informes estadisticos que se tratan
   * sobre la cantidad de agua de riego por cultivo
   * ******************************************************
   */

  /**
   * @param parcelId
   * @param dateFrom
   * @param dateUntil
   * @return referencia a un objeto de tipo List<Integer> que
   * contiene la cantidad total de agua utilizada para el
   * riego para cada uno de los cultivos sembrados en una
   * parcela en un periodo definido por dos fechas
   */
  public List<Integer> calculateTotalAmountIrrigationWaterPerCrop(int parcelId, Calendar dateFrom, Calendar dateUntil) {
    String subQuery = "SELECT FK_CROP AS CROP_ID, SUM(IRRIGATION_DONE) AS TOTAL_AMOUNT_IRRIGATION_WATER FROM "
        + "IRRIGATION_RECORD WHERE FK_PARCEL = ?1 AND ?2 <= DATE AND DATE <= ?3 AND FK_CROP IS NOT NULL "
        + "GROUP BY FK_CROP";

    String stringQuery = "SELECT CAST(CEIL(RESULT_TABLE.TOTAL_AMOUNT_IRRIGATION_WATER) AS INTEGER) FROM (" + subQuery + ") AS RESULT_TABLE";

    Query query = getEntityManager().createNativeQuery(stringQuery);
    query.setParameter(1, parcelId);
    query.setParameter(2, dateFrom);
    query.setParameter(3, dateUntil);

    List<Integer> result = null;

    try {
      result = query.getResultList();
    } catch (NoResultException e) {
      e.printStackTrace();
    }

    return result;
  }

  /**
   * @param parcelId
   * @param dateFrom
   * @param dateUntil
   * @return referencia a un objeto de tipo List<String> que
   * contiene los nombres de los cultivos para los que se
   * calcula la cantidad total de agua de riego utilizada
   * en cada uno de ellos sembrados en una parcela en un
   * periodo definido por dos fechas
   */
  public List<String> findCropNamesCalculatedPerTotalAmountIrrigationWaterPerCrop(int parcelId, Calendar dateFrom, Calendar dateUntil) {
    String subQuery = "SELECT FK_CROP AS CROP_ID "
        + "FROM IRRIGATION_RECORD WHERE "
        + "FK_PARCEL = ?1 AND ?2 <= DATE AND DATE <= ?3 AND FK_CROP IS NOT NULL "
        + "GROUP BY FK_CROP";

    String stringQuery = "SELECT NAME FROM CROP JOIN (" + subQuery + ") AS RESULT_TABLE ON CROP.ID = RESULT_TABLE.CROP_ID";

    Query query = getEntityManager().createNativeQuery(stringQuery);
    query.setParameter(1, parcelId);
    query.setParameter(2, dateFrom);
    query.setParameter(3, dateUntil);

    List<String> result = null;

    try {
      result = query.getResultList();
    } catch (NoResultException e) {
      e.printStackTrace();
    }

    return result;
  }

  /**
   * @param parcelId
   * @param dateFrom
   * @param dateUntil
   * @return referencia a un objeto de tipo List<Integer> que
   * contiene la cantidad total de agua utilizada por año para
   * el riego para cada uno de los cultivos sembrados en una
   * parcela en un periodo definido por dos fechas
   */
  public List<Integer> calculateTotalAmountIrrigationWaterPerCropAndYear(int parcelId, Calendar dateFrom, Calendar dateUntil) {
    String subQuery = "SELECT RESULT_TABLE_TWO.IRRIGATION_YEAR, RESULT_TABLE_TWO.CROP_ID, SUM(RESULT_TABLE_TWO.IRRIGATION_DONE) AS TOTAL_AMOUNT_CROP_IRRIGATION_WATER "
        + "FROM (SELECT YEAR(RESULT_TABLE.DATE) AS IRRIGATION_YEAR, RESULT_TABLE.CROP_ID, RESULT_TABLE.IRRIGATION_DONE FROM "
        + "(SELECT DATE, FK_CROP AS CROP_ID, IRRIGATION_DONE "
        + "FROM IRRIGATION_RECORD WHERE FK_PARCEL = ?1 AND ?2 <= DATE AND DATE <= ?3 "
        + "ORDER BY DATE) AS RESULT_TABLE) AS RESULT_TABLE_TWO "
        + "GROUP BY RESULT_TABLE_TWO.IRRIGATION_YEAR, RESULT_TABLE_TWO.CROP_ID";

    String stringQuery = "SELECT CAST(CEIL(RESULT_TABLE_THREE.TOTAL_AMOUNT_CROP_IRRIGATION_WATER) AS INTEGER) FROM ("
        + subQuery + ") AS RESULT_TABLE_THREE";

    Query query = getEntityManager().createNativeQuery(stringQuery);
    query.setParameter(1, parcelId);
    query.setParameter(2, dateFrom);
    query.setParameter(3, dateUntil);

    List<Integer> result = null;

    try {
      result = query.getResultList();
    } catch (NoResultException e) {
      e.printStackTrace();
    }

    return result;
  }

  /**
   * @param parcelId
   * @param dateFrom
   * @param dateUntil
   * @return referencia a un objeto de tipo List<String> que
   * contiene los nombres de los cultivos para los que se
   * calcula la cantidad total de agua de riego utilizada
   * por año en cada uno de ellos, esto es de aquellos
   * cultivos sembrados en una parcela en un periodo
   * definido por dos fechas
   */
  public List<String> findCropNamesCalculatedPerTotalAmountIrrigationWaterPerCropAndYear(int parcelId, Calendar dateFrom, Calendar dateUntil) {
    String subQuery = "SELECT RESULT_TABLE_TWO.IRRIGATION_YEAR, RESULT_TABLE_TWO.CROP_ID, SUM(RESULT_TABLE_TWO.IRRIGATION_DONE) AS TOTAL_AMOUNT_CROP_IRRIGATION_WATER "
        + "FROM (SELECT YEAR(RESULT_TABLE.DATE) AS IRRIGATION_YEAR, RESULT_TABLE.CROP_ID, RESULT_TABLE.IRRIGATION_DONE FROM "
        + "(SELECT DATE, FK_CROP AS CROP_ID, IRRIGATION_DONE "
        + "FROM IRRIGATION_RECORD WHERE FK_PARCEL = ?1 AND ?2 <= DATE AND DATE <= ?3 "
        + "ORDER BY DATE) AS RESULT_TABLE) AS RESULT_TABLE_TWO "
        + "GROUP BY RESULT_TABLE_TWO.IRRIGATION_YEAR, RESULT_TABLE_TWO.CROP_ID";

    String stringQuery = "SELECT NAME FROM CROP JOIN ("
        + subQuery + ") AS RESULT_TABLE_THREE ON ID = RESULT_TABLE_THREE.CROP_ID";

    Query query = getEntityManager().createNativeQuery(stringQuery);
    query.setParameter(1, parcelId);
    query.setParameter(2, dateFrom);
    query.setParameter(3, dateUntil);

    List<String> result = null;

    try {
      result = query.getResultList();
    } catch (NoResultException e) {
      e.printStackTrace();
    }

    return result;
  }

  /**
   * @param parcelId
   * @param dateFrom
   * @param dateUntil
   * @return referencia a un objeto de tipo List<Integer> que
   * contiene los años en los que se regaron los cultivos para
   * los que se calcula la cantidad total de agua que se utilizo
   * por año para regarlos, esto es de los cultivos sembrados
   * en una parcela en un periodo definido por dos fechas
   */
  public List<Integer> findYearsCalculatedPerTotalAmountIrrigationWaterPerCropAndYear(int parcelId, Calendar dateFrom, Calendar dateUntil) {
    String subQuery = "SELECT RESULT_TABLE_TWO.IRRIGATION_YEAR, RESULT_TABLE_TWO.CROP_ID, SUM(RESULT_TABLE_TWO.IRRIGATION_DONE) AS TOTAL_AMOUNT_CROP_IRRIGATION_WATER "
        + "FROM (SELECT YEAR(RESULT_TABLE.DATE) AS IRRIGATION_YEAR, RESULT_TABLE.CROP_ID, RESULT_TABLE.IRRIGATION_DONE FROM "
        + "(SELECT DATE, FK_CROP AS CROP_ID, IRRIGATION_DONE "
        + "FROM IRRIGATION_RECORD WHERE FK_PARCEL = ?1 AND ?2 <= DATE AND DATE <= ?3 "
        + "ORDER BY DATE) AS RESULT_TABLE) AS RESULT_TABLE_TWO "
        + "GROUP BY RESULT_TABLE_TWO.IRRIGATION_YEAR, RESULT_TABLE_TWO.CROP_ID";

    String stringQuery = "SELECT RESULT_TABLE_THREE.IRRIGATION_YEAR FROM ("
        + subQuery + ") AS RESULT_TABLE_THREE";

    Query query = getEntityManager().createNativeQuery(stringQuery);
    query.setParameter(1, parcelId);
    query.setParameter(2, dateFrom);
    query.setParameter(3, dateUntil);

    List<Integer> result = null;

    try {
      result = query.getResultList();
    } catch (NoResultException e) {
      e.printStackTrace();
    }

    return result;
  }

  /**
   * @param parcelId
   * @param dateFrom
   * @param dateUntil
   * @return referencia a un objeto de tipo Integer que contiene
   * la cantidad total de agua utilizada para el riego de cultivos
   * en un periodo definido por dos fechas
   */
  public Integer calculateTotalAmountCropIrrigationWaterPerPeriod(int parcelId, Calendar dateFrom, Calendar dateUntil) {
    String stringQuery = "SELECT CAST(CEIL(SUM(IRRIGATION_DONE)) AS INTEGER) FROM IRRIGATION_RECORD WHERE "
        + "FK_PARCEL = ?1 AND FK_CROP IS NOT NULL AND ?2 <= DATE AND DATE <= ?3";

    Query query = getEntityManager().createNativeQuery(stringQuery);
    query.setParameter(1, parcelId);
    query.setParameter(2, dateFrom);
    query.setParameter(3, dateUntil);

    return (Integer) query.getSingleResult();
  }

  /**
   * @param parcelId
   * @param dateFrom
   * @param dateUntil
   * @return double que representa el promedio del agua utilizada
   * para el riego de los cultivos sembrados en una parcela en un
   * periodo definido por dos fechas
   */
  public double calculateAverageCropIrrigationWater(int parcelId, Calendar dateFrom, Calendar dateUntil) {
    Query query = getEntityManager().createQuery("SELECT AVG(i.irrigationDone) FROM IrrigationRecord i WHERE i.parcel.id = :parcelId AND i.crop IS NOT NULL AND :dateFrom <= i.date AND i.date <= :dateUntil");
    query.setParameter("parcelId", parcelId);
    query.setParameter("dateFrom", dateFrom);
    query.setParameter("dateUntil", dateUntil);

    return (double) query.getSingleResult();
  }

  /*
   * ********************************************************
   * A partir de aqui comienzan los metodos relacionados a la
   * generacion de informes estadisticos que se tratan sobre
   * la cantidad cosechada (rendimiento) por cultivo
   * ********************************************************
   */

  /**
   * @param parcelId
   * @param dateFrom
   * @param dateUntil
   * @return referencia a un objeto de tipo List<Integer>
   * que contiene la cantidad total cosechada [kg] por
   * cultivo de cada uno de los cultivos cosechados en
   * una parcela en un periodo definido por dos fechas
   */
  public List<Integer> calculateTotalHarvestPerCrop(int parcelId, Calendar dateFrom, Calendar dateUntil) {
    String subQuery = "SELECT FK_CROP AS CROP_ID, SUM(HARVEST_AMOUNT) AS TOTAL_HARVEST FROM "
        + "HARVEST WHERE FK_PARCEL = ?1 AND ?2 <= DATE AND DATE <= ?3 "
        + "GROUP BY FK_CROP";

    String stringQuery = "SELECT CAST(CEIL(RESULT_TABLE.TOTAL_HARVEST) AS INTEGER) FROM (" + subQuery
        + ") AS RESULT_TABLE";

    Query query = getEntityManager().createNativeQuery(stringQuery);
    query.setParameter(1, parcelId);
    query.setParameter(2, dateFrom);
    query.setParameter(3, dateUntil);

    List<Integer> result = null;

    try {
      result = query.getResultList();
    } catch (NoResultException e) {
      e.printStackTrace();
    }

    return result;
  }

  /**
   * @param parcelId
   * @param dateFrom
   * @param dateUntil
   * @return referencia a un objeto de tipo List<String> que
   * contiene los nombres de los cultivos para los que se
   * calcula su cantidad total cosechada (rendimiento), esto
   * es de los cultivos cosechados en una parcela en un
   * periodo definido por dos fechas
   */
  public List<String> findCropNamesCalculatedPerTotalHarvestPerCrop(int parcelId, Calendar dateFrom, Calendar dateUntil) {
    String subQuery = "SELECT FK_CROP AS CROP_ID, SUM(HARVEST_AMOUNT) AS TOTAL_HARVEST FROM "
        + "HARVEST WHERE FK_PARCEL = ?1 AND ?2 <= DATE AND DATE <= ?3 "
        + "GROUP BY FK_CROP";

    String stringQuery = "SELECT NAME FROM CROP JOIN (" + subQuery
        + ") AS RESULT_TABLE ON ID = RESULT_TABLE.CROP_ID";

    Query query = getEntityManager().createNativeQuery(stringQuery);
    query.setParameter(1, parcelId);
    query.setParameter(2, dateFrom);
    query.setParameter(3, dateUntil);

    List<String> result = null;

    try {
      result = query.getResultList();
    } catch (NoResultException e) {
      e.printStackTrace();
    }

    return result;
  }

  /**
   * @param parcelId
   * @param dateFrom
   * @param dateUntil
   * @return referencia a un objeto de tipo List<Integer>
   * que contiene la cantidad total cosechada [kg] por
   * cultivo y año de cada uno de los cultivos cosechados
   * en una parcela en un periodo definido por dos fechas
   */
  public List<Integer> calculateTotalHarvestPerCropAndYear(int parcelId, Calendar dateFrom, Calendar dateUntil) {
    String subQuery = "SELECT RESULT_TABLE_TWO.HARVEST_YEAR, RESULT_TABLE_TWO.CROP_ID, SUM(RESULT_TABLE_TWO.HARVEST_AMOUNT) AS TOTAL_AMOUNT "
        + "FROM (SELECT YEAR(RESULT_TABLE.HARVEST_DATE) AS HARVEST_YEAR, RESULT_TABLE.CROP_ID, RESULT_TABLE.HARVEST_AMOUNT FROM "
        + "(SELECT DATE AS HARVEST_DATE, FK_CROP AS CROP_ID, HARVEST_AMOUNT FROM HARVEST "
        + "WHERE FK_PARCEL = ?1 AND ?2 <= DATE AND DATE <= ?3 "
        + "ORDER BY DATE) AS RESULT_TABLE) AS RESULT_TABLE_TWO "
        + "GROUP BY RESULT_TABLE_TWO.HARVEST_YEAR, RESULT_TABLE_TWO.CROP_ID";

    String stringQuery = "SELECT CAST(CEIL(RESULT_TABLE_THREE.TOTAL_AMOUNT) AS INTEGER) FROM (" + subQuery
        + ") AS RESULT_TABLE_THREE";

    Query query = getEntityManager().createNativeQuery(stringQuery);
    query.setParameter(1, parcelId);
    query.setParameter(2, dateFrom);
    query.setParameter(3, dateUntil);

    List<Integer> result = null;

    try {
      result = query.getResultList();
    } catch (NoResultException e) {
      e.printStackTrace();
    }

    return result;
  }

  /**
   * @param parcelId
   * @param dateFrom
   * @param dateUntil
   * @return referencia a un objeto de tipo List<String>
   * que contiene los nombres de los cultivos para los
   * que se calcula su cantidad total cosechada [kg] por
   * año, esto es de los cultivos cosechados en una parcela
   * en un periodo definido por dos fechas
   */
  public List<String> findCropNamesCalculatedPerTotalHarvestPerCropAndYear(int parcelId, Calendar dateFrom, Calendar dateUntil) {
    String subQuery = "SELECT RESULT_TABLE_TWO.HARVEST_YEAR, RESULT_TABLE_TWO.CROP_ID, SUM(RESULT_TABLE_TWO.HARVEST_AMOUNT) AS TOTAL_AMOUNT "
        + "FROM (SELECT YEAR(RESULT_TABLE.HARVEST_DATE) AS HARVEST_YEAR, RESULT_TABLE.CROP_ID, RESULT_TABLE.HARVEST_AMOUNT FROM "
        + "(SELECT DATE AS HARVEST_DATE, FK_CROP AS CROP_ID, HARVEST_AMOUNT FROM HARVEST "
        + "WHERE FK_PARCEL = ?1 AND ?2 <= DATE AND DATE <= ?3 "
        + "ORDER BY DATE) AS RESULT_TABLE) AS RESULT_TABLE_TWO "
        + "GROUP BY RESULT_TABLE_TWO.HARVEST_YEAR, RESULT_TABLE_TWO.CROP_ID";

    String stringQuery = "SELECT NAME FROM CROP JOIN (" + subQuery
        + ") AS RESULT_TABLE_THREE ON ID = RESULT_TABLE_THREE.CROP_ID";

    Query query = getEntityManager().createNativeQuery(stringQuery);
    query.setParameter(1, parcelId);
    query.setParameter(2, dateFrom);
    query.setParameter(3, dateUntil);

    List<String> result = null;

    try {
      result = query.getResultList();
    } catch (NoResultException e) {
      e.printStackTrace();
    }

    return result;
  }

  /**
   * @param parcelId
   * @param dateFrom
   * @param dateUntil
   * @return referencia a un objeto de tipo List<Integer> que
   * contiene los años en los que se cosecharon los cultivos
   * para los que se calcula la cantidad total cosechada [kg]
   * por año, esto es de los cultivos cosechados en una parcela
   * en un periodo definido por dos fechas
   */
  public List<Integer> findYearsCalculatedPerTotalHarvestPerCropAndYear(int parcelId, Calendar dateFrom, Calendar dateUntil) {
    String subQuery = "SELECT RESULT_TABLE_TWO.HARVEST_YEAR, RESULT_TABLE_TWO.CROP_ID, SUM(RESULT_TABLE_TWO.HARVEST_AMOUNT) AS TOTAL_AMOUNT "
        + "FROM (SELECT YEAR(RESULT_TABLE.HARVEST_DATE) AS HARVEST_YEAR, RESULT_TABLE.CROP_ID, RESULT_TABLE.HARVEST_AMOUNT FROM "
        + "(SELECT DATE AS HARVEST_DATE, FK_CROP AS CROP_ID, HARVEST_AMOUNT FROM HARVEST "
        + "WHERE FK_PARCEL = ?1 AND ?2 <= DATE AND DATE <= ?3 "
        + "ORDER BY DATE) AS RESULT_TABLE) AS RESULT_TABLE_TWO "
        + "GROUP BY RESULT_TABLE_TWO.HARVEST_YEAR, RESULT_TABLE_TWO.CROP_ID";

    String stringQuery = "SELECT RESULT_TABLE_THREE.HARVEST_YEAR FROM (" + subQuery
        + ") AS RESULT_TABLE_THREE";

    Query query = getEntityManager().createNativeQuery(stringQuery);
    query.setParameter(1, parcelId);
    query.setParameter(2, dateFrom);
    query.setParameter(3, dateUntil);

    List<Integer> result = null;

    try {
      result = query.getResultList();
    } catch (NoResultException e) {
      e.printStackTrace();
    }

    return result;
  }

  /**
   * @param parcelId
   * @param dateFrom
   * @param dateUntil
   * @return double que representa el promedio de las cantidades
   * cosechadas de los cultivos cosechados en una parcela en un
   * periodo definido por dos fechas
   */
  public double calculateAverageHarvest(int parcelId, Calendar dateFrom, Calendar dateUntil) {
    Query query = getEntityManager().createQuery("SELECT AVG(h.harvestAmount) FROM Harvest h WHERE h.parcel.id = :parcelId AND h.crop IS NOT NULL AND :dateFrom <= h.date AND h.date <= :dateUntil");
    query.setParameter("parcelId", parcelId);
    query.setParameter("dateFrom", dateFrom);
    query.setParameter("dateUntil", dateUntil);

    return (double) query.getSingleResult();
  }

  /**
   * @param parcelId
   * @param dateFrom
   * @param dateUntil
   * @return referencia a un objeto de tipo Integer que contiene
   * la cantidad total cosechada de los cultivos cosechados en
   * una parcela en un periodo definido por dos fechas
   */
  public Integer calculateTotalHarvestPerPeriod(int parcelId, Calendar dateFrom, Calendar dateUntil) {
    String stringQuery = "SELECT CAST(CEIL(SUM(HARVEST_AMOUNT)) AS INTEGER) FROM HARVEST WHERE "
        + "FK_PARCEL = ?1 AND FK_CROP IS NOT NULL AND ?2 <= DATE AND DATE <= ?3";

    Query query = getEntityManager().createNativeQuery(stringQuery);
    query.setParameter(1, parcelId);
    query.setParameter(2, dateFrom);
    query.setParameter(3, dateUntil);

    return (Integer) query.getSingleResult();
  }

  /*
   * ********************************************************
   * A partir de aqui comienzan los metodos relacionados a la
   * generacion de informes estadisticos que se tratan sobre
   * la cantidad de plantaciones por tipo de cultivo
   * ********************************************************
   */

  /**
   * @param parcelId
   * @param dateFrom
   * @param dateUntil
   * @return referencia a un objeto de tipo List<Integer> que
   * contiene los numeros que representan la cantidad total de
   * veces que se plantaron los tipos de cultivo en una parcela
   * en un periodo definido por dos fechas
   */
  public List<Integer> calculateTotalNumberPlantationsPerTypeCrop(int parcelId, Calendar dateFrom, Calendar dateUntil) {
    /*
     * Con las condiciones de las fechas se seleccionan todos los
     * registros de plantacion finalizados (*) de una parcela que
     * estan entre una fecha desde y una fecha hasta.
     * 
     * Con la primera condicion se selecciona el registro de
     * plantacion finalizado (*) de una parcela que tiene su fecha
     * de siembra mayor o igual a la fecha desde y menor o igual
     * a la fecha hasta, y su fecha de cosecha estrictamente mayor
     * a la fecha hasta. Es decir, se selecciona el registro de
     * plantacion finalizado de una parcela que tiene unicamente
     * su fecha de siembra dentro del periodo definido por la
     * fecha desde y la fecha hasta.
     * 
     * Con la segunda condicion se selecciona el registro de
     * plantacion finalizado (*) de una parcela que tiene su fecha
     * de siembra mayor o igual a la fecha desde y su fecha de
     * cosecha menor o igual a la fecha hasta. Es decir, se
     * selecciona el registro de plantacion que tiene su fecha
     * de siembra y su fecha de cosecha dentro del periodo
     * definido por la fecha desde y la fecha hasta.
     * 
     * Con la tercera conidicon se selecciona el registro de
     * plantacion finalizado (*) de una parcela que tiene su
     * fecha de cosecha mayor o igual a la fecha desde y menor
     * igual a la fecha hasta, y su fecha de siembra estrictamente
     * menor a la fecha desde. Es decir, se selecciona el registro
     * de plantacion finalizado de una parcela que tiene unicamente
     * su fecha de cosecha dentro del periodo definido por la
     * fecha desde y la fecha hasta.
     * 
     * (*) El ID para el estado finalizado de un registro de
     * plantacion es el 1, siempre y cuando no se modifique el
     * orden en el que se ejecutan las instrucciones de insercion
     * del archivo plantingRecordStatusInserts.sql de la ruta
     * app/etc/sql.
     */
    String subQuery = "SELECT FK_TYPE_CROP AS TYPE_CROP_ID, COUNT(FK_TYPE_CROP) AS TOTAL_NUMBER_PLANTATIONS_PER_TYPE_CROP FROM "
        + "PLANTING_RECORD JOIN CROP ON FK_CROP = CROP.ID WHERE "
        + "FK_PARCEL = ?1 AND FK_STATUS = 1 AND "
        + "((?2 <= SEED_DATE AND SEED_DATE <= ?3 AND HARVEST_DATE > ?3) OR "
        + "(SEED_DATE >= ?2 AND HARVEST_DATE <= ?3) OR "
        + "(?2 <= HARVEST_DATE AND HARVEST_DATE <= ?3 AND SEED_DATE < ?2)) "
        + "GROUP BY FK_TYPE_CROP";

    String stringQuery = "SELECT RESULT_TABLE.TOTAL_NUMBER_PLANTATIONS_PER_TYPE_CROP FROM (" + subQuery
        + ") AS RESULT_TABLE";

    Query query = getEntityManager().createNativeQuery(stringQuery);
    query.setParameter(1, parcelId);
    query.setParameter(2, dateFrom);
    query.setParameter(3, dateUntil);

    List<Integer> result = null;

    try {
      result = query.getResultList();
    } catch (NoResultException e) {
      e.printStackTrace();
    }

    return result;
  }

  /**
   * @param parcelId
   * @param dateFrom
   * @param dateUntil
   * @return referencia a un objeto de tipo List<String> que
   * contiene los nombres de los tipos de cultivos para los
   * que se calcula la cantidad total de veces que se plantaron
   * en una parcela en un periodo definido por dos fechas
   */
  public List<String> findTypeCropNamesCalculatedPerTotalNumberPlantationsPerTypeCrop(int parcelId, Calendar dateFrom, Calendar dateUntil) {
    /*
     * Con las condiciones de las fechas se seleccionan todos los
     * registros de plantacion finalizados (*) de una parcela que
     * estan entre una fecha desde y una fecha hasta.
     * 
     * Con la primera condicion se selecciona el registro de
     * plantacion finalizado (*) de una parcela que tiene su fecha
     * de siembra mayor o igual a la fecha desde y menor o igual
     * a la fecha hasta, y su fecha de cosecha estrictamente mayor
     * a la fecha hasta. Es decir, se selecciona el registro de
     * plantacion finalizado de una parcela que tiene unicamente
     * su fecha de siembra dentro del periodo definido por la
     * fecha desde y la fecha hasta.
     * 
     * Con la segunda condicion se selecciona el registro de
     * plantacion finalizado (*) de una parcela que tiene su fecha
     * de siembra mayor o igual a la fecha desde y su fecha de
     * cosecha menor o igual a la fecha hasta. Es decir, se
     * selecciona el registro de plantacion que tiene su fecha
     * de siembra y su fecha de cosecha dentro del periodo
     * definido por la fecha desde y la fecha hasta.
     * 
     * Con la tercera conidicon se selecciona el registro de
     * plantacion finalizado (*) de una parcela que tiene su
     * fecha de cosecha mayor o igual a la fecha desde y menor
     * igual a la fecha hasta, y su fecha de siembra estrictamente
     * menor a la fecha desde. Es decir, se selecciona el registro
     * de plantacion finalizado de una parcela que tiene unicamente
     * su fecha de cosecha dentro del periodo definido por la
     * fecha desde y la fecha hasta.
     * 
     * (*) El ID para el estado finalizado de un registro de
     * plantacion es el 1, siempre y cuando no se modifique el
     * orden en el que se ejecutan las instrucciones de insercion
     * del archivo plantingRecordStatusInserts.sql de la ruta
     * app/etc/sql.
     */
    String subQuery = "SELECT FK_TYPE_CROP AS TYPE_CROP_ID, COUNT(FK_TYPE_CROP) AS TOTAL_NUMBER_PLANTATIONS_PER_TYPE_CROP FROM "
        + "PLANTING_RECORD JOIN CROP ON FK_CROP = CROP.ID WHERE "
        + "FK_PARCEL = ?1 AND FK_STATUS = 1 AND "
        + "((?2 <= SEED_DATE AND SEED_DATE <= ?3 AND HARVEST_DATE > ?3) OR "
        + "(SEED_DATE >= ?2 AND HARVEST_DATE <= ?3) OR "
        + "(?2 <= HARVEST_DATE AND HARVEST_DATE <= ?3 AND SEED_DATE < ?2)) "
        + "GROUP BY FK_TYPE_CROP";

    String stringQuery = "SELECT NAME FROM TYPE_CROP JOIN (" + subQuery
        + ") AS RESULT_TABLE ON ID = RESULT_TABLE.TYPE_CROP_ID";

    Query query = getEntityManager().createNativeQuery(stringQuery);
    query.setParameter(1, parcelId);
    query.setParameter(2, dateFrom);
    query.setParameter(3, dateUntil);

    List<String> result = null;

    try {
      result = query.getResultList();
    } catch (NoResultException e) {
      e.printStackTrace();
    }

    return result;
  }

  /**
   * @param parcelId
   * @param dateFrom
   * @param dateUntil
   * @return referencia a un objeto de tipo List<Integer> que
   * contiene los numeros que representan la cantidad total
   * de veces que se plantaron los tipos de cultivos por año
   * en una parcela en un periodo definido por dos fechas
   */
  public List<Integer> calculateTotalNumberPlantationsPerTypeCropAndYear(int parcelId, Calendar dateFrom, Calendar dateUntil) {
    /*
     * Con las condiciones de las fechas se seleccionan todos los
     * registros de plantacion finalizados (*) de una parcela que
     * estan entre una fecha desde y una fecha hasta.
     * 
     * Con la primera condicion se selecciona el registro de
     * plantacion finalizado (*) de una parcela que tiene su fecha
     * de siembra mayor o igual a la fecha desde y menor o igual
     * a la fecha hasta, y su fecha de cosecha estrictamente mayor
     * a la fecha hasta. Es decir, se selecciona el registro de
     * plantacion finalizado de una parcela que tiene unicamente
     * su fecha de siembra dentro del periodo definido por la
     * fecha desde y la fecha hasta.
     * 
     * Con la segunda condicion se selecciona el registro de
     * plantacion finalizado (*) de una parcela que tiene su fecha
     * de siembra mayor o igual a la fecha desde y su fecha de
     * cosecha menor o igual a la fecha hasta. Es decir, se
     * selecciona el registro de plantacion que tiene su fecha
     * de siembra y su fecha de cosecha dentro del periodo
     * definido por la fecha desde y la fecha hasta.
     * 
     * Con la tercera conidicon se selecciona el registro de
     * plantacion finalizado (*) de una parcela que tiene su
     * fecha de cosecha mayor o igual a la fecha desde y menor
     * igual a la fecha hasta, y su fecha de siembra estrictamente
     * menor a la fecha desde. Es decir, se selecciona el registro
     * de plantacion finalizado de una parcela que tiene unicamente
     * su fecha de cosecha dentro del periodo definido por la
     * fecha desde y la fecha hasta.
     * 
     * (*) El ID para el estado finalizado de un registro de
     * plantacion es el 1, siempre y cuando no se modifique el
     * orden en el que se ejecutan las instrucciones de insercion
     * del archivo plantingRecordStatusInserts.sql de la ruta
     * app/etc/sql.
     */
    String subQuery = "SELECT RESULT_TABLE_TWO.SEED_YEAR, RESULT_TABLE_TWO.TYPE_CROP_ID, "
        + "COUNT(RESULT_TABLE_TWO.TYPE_CROP_ID) AS TOTAL_NUMBER_PLANTATIONS_PER_TYPE_CROP_AND_YEAR FROM "
        + "(SELECT YEAR(RESULT_TABLE.SEED_DATE) AS SEED_YEAR, RESULT_TABLE.TYPE_CROP_ID FROM "
        + "(SELECT SEED_DATE, FK_TYPE_CROP AS TYPE_CROP_ID FROM "
        + "PLANTING_RECORD JOIN CROP ON FK_CROP = CROP.ID "
        + "WHERE FK_PARCEL = ?1 AND FK_STATUS = 1 AND "
        + "((?2 <= SEED_DATE AND SEED_DATE <= ?3 AND HARVEST_DATE > ?3) OR "
        + "(SEED_DATE >= ?2 AND HARVEST_DATE <= ?3) OR "
        + "(?2 <= HARVEST_DATE AND HARVEST_DATE <= ?3 AND SEED_DATE < ?2)) "
        + "ORDER BY SEED_DATE) AS RESULT_TABLE) AS RESULT_TABLE_TWO "
        + "GROUP BY SEED_YEAR, TYPE_CROP_ID";

    String stringQuery = "SELECT RESULT_TABLE_THREE.TOTAL_NUMBER_PLANTATIONS_PER_TYPE_CROP_AND_YEAR FROM (" + subQuery
        + ") AS RESULT_TABLE_THREE";

    Query query = getEntityManager().createNativeQuery(stringQuery);
    query.setParameter(1, parcelId);
    query.setParameter(2, dateFrom);
    query.setParameter(3, dateUntil);

    List<Integer> result = null;

    try {
      result = query.getResultList();
    } catch (NoResultException e) {
      e.printStackTrace();
    }

    return result;
  }

  /**
   * @param parcelId
   * @param dateFrom
   * @param dateUntil
   * @return referencia a un objeto de tipo List<String> que
   * contiene los nombres de los tipos de cultivos para los
   * que se calcula la cantidad total de veces que se plantaron
   * por año en una parcela en un periodo definido por dos
   * fechas
   */
  public List<String> findTypeCropNamesCalculatedPerTotalNumberPlantationsPerTypeCropAndYear(int parcelId, Calendar dateFrom, Calendar dateUntil) {
    /*
     * Con las condiciones de las fechas se seleccionan todos los
     * registros de plantacion finalizados (*) de una parcela que
     * estan entre una fecha desde y una fecha hasta.
     * 
     * Con la primera condicion se selecciona el registro de
     * plantacion finalizado (*) de una parcela que tiene su fecha
     * de siembra mayor o igual a la fecha desde y menor o igual
     * a la fecha hasta, y su fecha de cosecha estrictamente mayor
     * a la fecha hasta. Es decir, se selecciona el registro de
     * plantacion finalizado de una parcela que tiene unicamente
     * su fecha de siembra dentro del periodo definido por la
     * fecha desde y la fecha hasta.
     * 
     * Con la segunda condicion se selecciona el registro de
     * plantacion finalizado (*) de una parcela que tiene su fecha
     * de siembra mayor o igual a la fecha desde y su fecha de
     * cosecha menor o igual a la fecha hasta. Es decir, se
     * selecciona el registro de plantacion que tiene su fecha
     * de siembra y su fecha de cosecha dentro del periodo
     * definido por la fecha desde y la fecha hasta.
     * 
     * Con la tercera conidicon se selecciona el registro de
     * plantacion finalizado (*) de una parcela que tiene su
     * fecha de cosecha mayor o igual a la fecha desde y menor
     * igual a la fecha hasta, y su fecha de siembra estrictamente
     * menor a la fecha desde. Es decir, se selecciona el registro
     * de plantacion finalizado de una parcela que tiene unicamente
     * su fecha de cosecha dentro del periodo definido por la
     * fecha desde y la fecha hasta.
     * 
     * (*) El ID para el estado finalizado de un registro de
     * plantacion es el 1, siempre y cuando no se modifique el
     * orden en el que se ejecutan las instrucciones de insercion
     * del archivo plantingRecordStatusInserts.sql de la ruta
     * app/etc/sql.
     */
    String subQuery = "SELECT RESULT_TABLE_TWO.SEED_YEAR, RESULT_TABLE_TWO.TYPE_CROP_ID, "
        + "COUNT(RESULT_TABLE_TWO.TYPE_CROP_ID) AS TOTAL_NUMBER_PLANTATIONS_PER_TYPE_CROP_AND_YEAR FROM "
        + "(SELECT YEAR(RESULT_TABLE.SEED_DATE) AS SEED_YEAR, RESULT_TABLE.TYPE_CROP_ID FROM "
        + "(SELECT SEED_DATE, FK_TYPE_CROP AS TYPE_CROP_ID FROM "
        + "PLANTING_RECORD JOIN CROP ON FK_CROP = CROP.ID "
        + "WHERE FK_PARCEL = ?1 AND FK_STATUS = 1 AND "
        + "((?2 <= SEED_DATE AND SEED_DATE <= ?3 AND HARVEST_DATE > ?3) OR "
        + "(SEED_DATE >= ?2 AND HARVEST_DATE <= ?3) OR "
        + "(?2 <= HARVEST_DATE AND HARVEST_DATE <= ?3 AND SEED_DATE < ?2)) "
        + "ORDER BY SEED_DATE) AS RESULT_TABLE) AS RESULT_TABLE_TWO "
        + "GROUP BY SEED_YEAR, TYPE_CROP_ID";

    String stringQuery = "SELECT NAME FROM TYPE_CROP JOIN (" + subQuery
        + ") AS RESULT_TABLE_THREE ON ID = RESULT_TABLE_THREE.TYPE_CROP_ID";

    Query query = getEntityManager().createNativeQuery(stringQuery);
    query.setParameter(1, parcelId);
    query.setParameter(2, dateFrom);
    query.setParameter(3, dateUntil);

    List<String> result = null;

    try {
      result = query.getResultList();
    } catch (NoResultException e) {
      e.printStackTrace();
    }

    return result;
  }

  /**
   * @param parcelId
   * @param dateFrom
   * @param dateUntil
   * @return referencia a un objeto de tipo List<Integer> que
   * contiene los años en los que se sembraron los tipos de
   * cultivos para los que se calcula la cantidad total de veces
   * que se plantaron por año en una parcela en un periodo
   * definido por dos fechas
   */
  public List<Integer> findYearsCalculatedPerTotalNumberPlantationsPerTypeCropAndYear(int parcelId, Calendar dateFrom, Calendar dateUntil) {
    /*
     * Con las condiciones de las fechas se seleccionan todos los
     * registros de plantacion finalizados (*) de una parcela que
     * estan entre una fecha desde y una fecha hasta.
     * 
     * Con la primera condicion se selecciona el registro de
     * plantacion finalizado (*) de una parcela que tiene su fecha
     * de siembra mayor o igual a la fecha desde y menor o igual
     * a la fecha hasta, y su fecha de cosecha estrictamente mayor
     * a la fecha hasta. Es decir, se selecciona el registro de
     * plantacion finalizado de una parcela que tiene unicamente
     * su fecha de siembra dentro del periodo definido por la
     * fecha desde y la fecha hasta.
     * 
     * Con la segunda condicion se selecciona el registro de
     * plantacion finalizado (*) de una parcela que tiene su fecha
     * de siembra mayor o igual a la fecha desde y su fecha de
     * cosecha menor o igual a la fecha hasta. Es decir, se
     * selecciona el registro de plantacion que tiene su fecha
     * de siembra y su fecha de cosecha dentro del periodo
     * definido por la fecha desde y la fecha hasta.
     * 
     * Con la tercera conidicon se selecciona el registro de
     * plantacion finalizado (*) de una parcela que tiene su
     * fecha de cosecha mayor o igual a la fecha desde y menor
     * igual a la fecha hasta, y su fecha de siembra estrictamente
     * menor a la fecha desde. Es decir, se selecciona el registro
     * de plantacion finalizado de una parcela que tiene unicamente
     * su fecha de cosecha dentro del periodo definido por la
     * fecha desde y la fecha hasta.
     * 
     * (*) El ID para el estado finalizado de un registro de
     * plantacion es el 1, siempre y cuando no se modifique el
     * orden en el que se ejecutan las instrucciones de insercion
     * del archivo plantingRecordStatusInserts.sql de la ruta
     * app/etc/sql.
     */
    String subQuery = "SELECT RESULT_TABLE_TWO.SEED_YEAR, RESULT_TABLE_TWO.TYPE_CROP_ID, "
        + "COUNT(RESULT_TABLE_TWO.TYPE_CROP_ID) AS TOTAL_NUMBER_PLANTATIONS_PER_TYPE_CROP_AND_YEAR FROM "
        + "(SELECT YEAR(RESULT_TABLE.SEED_DATE) AS SEED_YEAR, RESULT_TABLE.TYPE_CROP_ID FROM "
        + "(SELECT SEED_DATE, FK_TYPE_CROP AS TYPE_CROP_ID FROM "
        + "PLANTING_RECORD JOIN CROP ON FK_CROP = CROP.ID "
        + "WHERE FK_PARCEL = ?1 AND FK_STATUS = 1 AND "
        + "((?2 <= SEED_DATE AND SEED_DATE <= ?3 AND HARVEST_DATE > ?3) OR "
        + "(SEED_DATE >= ?2 AND HARVEST_DATE <= ?3) OR "
        + "(?2 <= HARVEST_DATE AND HARVEST_DATE <= ?3 AND SEED_DATE < ?2)) "
        + "ORDER BY SEED_DATE) AS RESULT_TABLE) AS RESULT_TABLE_TWO "
        + "GROUP BY SEED_YEAR, TYPE_CROP_ID";

    String stringQuery = "SELECT RESULT_TABLE_THREE.SEED_YEAR FROM (" + subQuery
        + ") AS RESULT_TABLE_THREE";

    Query query = getEntityManager().createNativeQuery(stringQuery);
    query.setParameter(1, parcelId);
    query.setParameter(2, dateFrom);
    query.setParameter(3, dateUntil);

    List<Integer> result = null;

    try {
      result = query.getResultList();
    } catch (NoResultException e) {
      e.printStackTrace();
    }

    return result;
  }

  /*
   * ******************************************************
   * A partir de aqui comienzan los metodos relacionados con
   * la generacion de informes estadisticos que se tratan
   * sobre la cantidad de agua de riego por tipo de cultivo
   * ******************************************************
   */

  /**
   * @param parcelId
   * @param dateFrom
   * @param dateUntil
   * @return referencia a un objeto de tipo List<Integer> que
   * contiene la cantidad total de agua utilizada para el
   * riego para cada uno de los tipos de cultivos sembrados en
   * una parcela en un periodo definido por dos fechas
   */
  public List<Integer> calculateTotalAmountIrrigationWaterPerTypeCrop(int parcelId, Calendar dateFrom, Calendar dateUntil) {
    String subQuery = "SELECT FK_TYPE_CROP AS TYPE_CROP_ID, SUM(IRRIGATION_DONE) AS TOTAL_AMOUNT_IRRIGATION_WATER FROM "
        + "IRRIGATION_RECORD JOIN CROP ON FK_CROP = CROP.ID WHERE "
        + "FK_PARCEL = ?1 AND ?2 <= DATE AND DATE <= ?3 AND FK_CROP IS NOT NULL "
        + "GROUP BY FK_TYPE_CROP";

    String stringQuery = "SELECT CAST(CEIL(RESULT_TABLE.TOTAL_AMOUNT_IRRIGATION_WATER) AS INTEGER) FROM (" + subQuery
        + ") AS RESULT_TABLE";

    Query query = getEntityManager().createNativeQuery(stringQuery);
    query.setParameter(1, parcelId);
    query.setParameter(2, dateFrom);
    query.setParameter(3, dateUntil);

    List<Integer> result = null;

    try {
      result = query.getResultList();
    } catch (NoResultException e) {
      e.printStackTrace();
    }

    return result;
  }

  /**
   * @param parcelId
   * @param dateFrom
   * @param dateUntil
   * @return referencia a un objeto de tipo List<String> que
   * contiene los nombres de los tipos de cultivos para los
   * que se calcula la cantidad total de agua de riego
   * utilizada en cada uno de ellos sembrados en una parcela
   * en un periodo definido por dos fechas
   */
  public List<String> findTypeCropNamesCalculatedPerTotalAmountIrrigationWaterPerTypeCrop(int parcelId, Calendar dateFrom, Calendar dateUntil) {
    String subQuery = "SELECT FK_TYPE_CROP AS TYPE_CROP_ID, SUM(IRRIGATION_DONE) AS TOTAL_AMOUNT_IRRIGATION_WATER FROM "
        + "IRRIGATION_RECORD JOIN CROP ON FK_CROP = CROP.ID WHERE "
        + "FK_PARCEL = ?1 AND ?2 <= DATE AND DATE <= ?3 AND FK_CROP IS NOT NULL "
        + "GROUP BY FK_TYPE_CROP";

    String stringQuery = "SELECT NAME FROM TYPE_CROP JOIN (" + subQuery + ") AS RESULT_TABLE ON ID = RESULT_TABLE.TYPE_CROP_ID";

    Query query = getEntityManager().createNativeQuery(stringQuery);
    query.setParameter(1, parcelId);
    query.setParameter(2, dateFrom);
    query.setParameter(3, dateUntil);

    List<String> result = null;

    try {
      result = query.getResultList();
    } catch (NoResultException e) {
      e.printStackTrace();
    }

    return result;
  }

  /**
   * @param parcelId
   * @param dateFrom
   * @param dateUntil
   * @return referencia a un objeto de tipo List<Integer> que
   * contiene la cantidad total de agua utilizada por año para
   * el riego para cada uno de los tipos de cultivos sembrados
   * en una parcela en un periodo definido por dos fechas
   */
  public List<Integer> calculateTotalAmountIrrigationWaterPerTypeCropAndYear(int parcelId, Calendar dateFrom, Calendar dateUntil) {
    String subQuery = "SELECT RESULT_TABLE_TWO.IRRIGATION_YEAR, RESULT_TABLE_TWO.TYPE_CROP_ID, "
        + "SUM(RESULT_TABLE_TWO.IRRIGATION_DONE) AS TOTAL_AMOUNT_IRRIGATION_WATER FROM "
        + "(SELECT YEAR(RESULT_TABLE.IRRIGATION_DATE) AS IRRIGATION_YEAR, RESULT_TABLE.TYPE_CROP_ID, RESULT_TABLE.IRRIGATION_DONE FROM "
        + "(SELECT DATE AS IRRIGATION_DATE, FK_TYPE_CROP AS TYPE_CROP_ID, IRRIGATION_DONE FROM "
        + "IRRIGATION_RECORD JOIN CROP ON FK_CROP = CROP.ID WHERE "
        + "FK_PARCEL = ?1 AND ?2 <= DATE AND DATE <= ?3 AND FK_CROP IS NOT NULL "
        + "ORDER BY DATE) AS RESULT_TABLE) AS RESULT_TABLE_TWO "
        + "GROUP BY RESULT_TABLE_TWO.IRRIGATION_YEAR, RESULT_TABLE_TWO.TYPE_CROP_ID";

    String stringQuery = "SELECT CAST(CEIL(RESULT_TABLE_THREE.TOTAL_AMOUNT_IRRIGATION_WATER) AS INTEGER) FROM ("
        + subQuery + ") AS RESULT_TABLE_THREE";

    Query query = getEntityManager().createNativeQuery(stringQuery);
    query.setParameter(1, parcelId);
    query.setParameter(2, dateFrom);
    query.setParameter(3, dateUntil);

    List<Integer> result = null;

    try {
      result = query.getResultList();
    } catch (NoResultException e) {
      e.printStackTrace();
    }

    return result;
  }

  /**
   * @param parcelId
   * @param dateFrom
   * @param dateUntil
   * @return referencia a un objeto de tipo List<String> que
   * contiene los nombres de los tipos de cultivos para los
   * que se calcula la cantidad total de agua de riego utilizada
   * por año en cada uno de ellos, esto es de aquellos tipos
   * de cultivos sembrados en una parcela en un periodo
   * definido por dos fechas
   */
  public List<String> findTypeCropNamesCalculatedPerTotalAmountIrrigationWaterPerTypeCropAndYear(int parcelId, Calendar dateFrom, Calendar dateUntil) {
    String subQuery = "SELECT RESULT_TABLE_TWO.IRRIGATION_YEAR, RESULT_TABLE_TWO.TYPE_CROP_ID, "
        + "SUM(RESULT_TABLE_TWO.IRRIGATION_DONE) AS TOTAL_AMOUNT_IRRIGATION_WATER FROM "
        + "(SELECT YEAR(RESULT_TABLE.IRRIGATION_DATE) AS IRRIGATION_YEAR, RESULT_TABLE.TYPE_CROP_ID, RESULT_TABLE.IRRIGATION_DONE FROM "
        + "(SELECT DATE AS IRRIGATION_DATE, FK_TYPE_CROP AS TYPE_CROP_ID, IRRIGATION_DONE FROM "
        + "IRRIGATION_RECORD JOIN CROP ON FK_CROP = CROP.ID WHERE "
        + "FK_PARCEL = ?1 AND ?2 <= DATE AND DATE <= ?3 AND FK_CROP IS NOT NULL "
        + "ORDER BY DATE) AS RESULT_TABLE) AS RESULT_TABLE_TWO "
        + "GROUP BY RESULT_TABLE_TWO.IRRIGATION_YEAR, RESULT_TABLE_TWO.TYPE_CROP_ID";

    String stringQuery = "SELECT NAME FROM TYPE_CROP JOIN (" + subQuery + ") AS RESULT_TABLE_THREE ON ID = RESULT_TABLE_THREE.TYPE_CROP_ID";

    Query query = getEntityManager().createNativeQuery(stringQuery);
    query.setParameter(1, parcelId);
    query.setParameter(2, dateFrom);
    query.setParameter(3, dateUntil);

    List<String> result = null;

    try {
      result = query.getResultList();
    } catch (NoResultException e) {
      e.printStackTrace();
    }

    return result;
  }

  /**
   * @param parcelId
   * @param dateFrom
   * @param dateUntil
   * @return referencia a un objeto de tipo List<Integer> que
   * contiene los años en los que se regaron los tipos de cultivos
   * para los que se calcula la cantidad total de agua que se
   * utilizo por año para regarlos, esto es de los tipos de
   * cultivos sembrados en una parcela en un periodo definido
   * por dos fechas
   */
  public List<Integer> findYearsCalculatedPerTotalAmountIrrigationWaterPerTypeCropAndYear(int parcelId, Calendar dateFrom, Calendar dateUntil) {
    String subQuery = "SELECT RESULT_TABLE_TWO.IRRIGATION_YEAR, RESULT_TABLE_TWO.TYPE_CROP_ID, "
        + "SUM(RESULT_TABLE_TWO.IRRIGATION_DONE) AS TOTAL_AMOUNT_IRRIGATION_WATER FROM "
        + "(SELECT YEAR(RESULT_TABLE.IRRIGATION_DATE) AS IRRIGATION_YEAR, RESULT_TABLE.TYPE_CROP_ID, RESULT_TABLE.IRRIGATION_DONE FROM "
        + "(SELECT DATE AS IRRIGATION_DATE, FK_TYPE_CROP AS TYPE_CROP_ID, IRRIGATION_DONE FROM "
        + "IRRIGATION_RECORD JOIN CROP ON FK_CROP = CROP.ID WHERE "
        + "FK_PARCEL = ?1 AND ?2 <= DATE AND DATE <= ?3 AND FK_CROP IS NOT NULL "
        + "ORDER BY DATE) AS RESULT_TABLE) AS RESULT_TABLE_TWO "
        + "GROUP BY RESULT_TABLE_TWO.IRRIGATION_YEAR, RESULT_TABLE_TWO.TYPE_CROP_ID";

    String stringQuery = "SELECT RESULT_TABLE_THREE.IRRIGATION_YEAR FROM ("
        + subQuery + ") AS RESULT_TABLE_THREE";

    Query query = getEntityManager().createNativeQuery(stringQuery);
    query.setParameter(1, parcelId);
    query.setParameter(2, dateFrom);
    query.setParameter(3, dateUntil);

    List<Integer> result = null;

    try {
      result = query.getResultList();
    } catch (NoResultException e) {
      e.printStackTrace();
    }

    return result;
  }

  /*
   * ********************************************************
   * A partir de aqui comienzan los metodos relacionados a la
   * generacion de informes estadisticos que se tratan sobre
   * la cantidad cosechada (rendimiento) por tipo de cultivo
   * ********************************************************
   */

  /**
   * @param parcelId
   * @param dateFrom
   * @param dateUntil
   * @return referencia a un objeto de tipo List<Integer>
   * que contiene la cantidad total cosechada [kg] por
   * tipo de cultivo de cada uno de los tipos de cultivos
   * cosechados en una parcela en un periodo definido por
   * dos fechas
   */
  public List<Integer> calculateTotalHarvestPerTypeCrop(int parcelId, Calendar dateFrom, Calendar dateUntil) {
    String subQuery = "SELECT FK_TYPE_CROP AS TYPE_CROP_ID, SUM(HARVEST_AMOUNT) AS TOTAL_HARVEST_AMOUNT FROM "
        + "HARVEST JOIN CROP ON FK_CROP = CROP.ID WHERE FK_PARCEL = ?1 AND ?2 <= DATE AND DATE <= ?3 "
        + "GROUP BY FK_TYPE_CROP";

    String stringQuery = "SELECT CAST(CEIL(RESULT_TABLE.TOTAL_HARVEST_AMOUNT) AS INTEGER) FROM (" + subQuery
        + ") AS RESULT_TABLE";

    Query query = getEntityManager().createNativeQuery(stringQuery);
    query.setParameter(1, parcelId);
    query.setParameter(2, dateFrom);
    query.setParameter(3, dateUntil);

    List<Integer> result = null;

    try {
      result = query.getResultList();
    } catch (NoResultException e) {
      e.printStackTrace();
    }

    return result;
  }

  /**
   * @param parcelId
   * @param dateFrom
   * @param dateUntil
   * @return referencia a un objeto de tipo List<String> que
   * contiene los nombres de los tipos de cultivos para los
   * que se calcula su cantidad total cosechada (rendimiento),
   * esto es de los tipos de cultivos cosechados en una parcela
   * en un periodo definido por dos fechas
   */
  public List<String> findTypeCropNamesCalculatedPerTotalHarvestPerTypeCrop(int parcelId, Calendar dateFrom, Calendar dateUntil) {
    String subQuery = "SELECT FK_TYPE_CROP AS TYPE_CROP_ID, SUM(HARVEST_AMOUNT) AS TOTAL_HARVEST_AMOUNT FROM "
        + "HARVEST JOIN CROP ON FK_CROP = CROP.ID WHERE FK_PARCEL = ?1 AND ?2 <= DATE AND DATE <= ?3 "
        + "GROUP BY FK_TYPE_CROP";

    String stringQuery = "SELECT NAME FROM TYPE_CROP JOIN (" + subQuery
        + ") AS RESULT_TABLE ON ID = RESULT_TABLE.TYPE_CROP_ID";

    Query query = getEntityManager().createNativeQuery(stringQuery);
    query.setParameter(1, parcelId);
    query.setParameter(2, dateFrom);
    query.setParameter(3, dateUntil);

    List<String> result = null;

    try {
      result = query.getResultList();
    } catch (NoResultException e) {
      e.printStackTrace();
    }

    return result;
  }

  /**
   * @param parcelId
   * @param dateFrom
   * @param dateUntil
   * @return referencia a un objeto de tipo List<Integer>
   * que contiene la cantidad total cosechada [kg] por
   * tipo de cultivo y año de cada uno de los tipos de
   * cultivos cosechados en una parcela en un periodo
   * definido por dos fechas
   */
  public List<Integer> calculateTotalHarvestPerTypeCropAndYear(int parcelId, Calendar dateFrom, Calendar dateUntil) {
    String subQuery = "SELECT RESULT_TABLE_TWO.HARVEST_YEAR, RESULT_TABLE_TWO.TYPE_CROP_ID, "
        + "SUM(RESULT_TABLE_TWO.HARVEST_AMOUNT) AS TOTAL_HARVEST_AMOUNT FROM "
        + "(SELECT YEAR(RESULT_TABLE.HARVEST_DATE) AS HARVEST_YEAR, RESULT_TABLE.TYPE_CROP_ID, RESULT_TABLE.HARVEST_AMOUNT "
        + "FROM (SELECT DATE AS HARVEST_DATE, FK_TYPE_CROP AS TYPE_CROP_ID, HARVEST_AMOUNT "
        + "FROM HARVEST JOIN CROP ON FK_CROP = CROP.ID "
        + "WHERE FK_PARCEL = ?1 AND ?2 <= DATE AND DATE <= ?3 "
        + "ORDER BY DATE) AS RESULT_TABLE) AS RESULT_TABLE_TWO "
        + "GROUP BY RESULT_TABLE_TWO.HARVEST_YEAR, RESULT_TABLE_TWO.TYPE_CROP_ID";

    String stringQuery = "SELECT CAST(CEIL(RESULT_TABLE_THREE.TOTAL_HARVEST_AMOUNT) AS INTEGER) FROM (" + subQuery
        + ") AS RESULT_TABLE_THREE";

    Query query = getEntityManager().createNativeQuery(stringQuery);
    query.setParameter(1, parcelId);
    query.setParameter(2, dateFrom);
    query.setParameter(3, dateUntil);

    List<Integer> result = null;

    try {
      result = query.getResultList();
    } catch (NoResultException e) {
      e.printStackTrace();
    }

    return result;
  }

  /**
   * @param parcelId
   * @param dateFrom
   * @param dateUntil
   * @return referencia a un objeto de tipo List<String>
   * que contiene los nombres de los tipos de cultivos
   * para los que se calcula su cantidad total cosechada
   * [kg] por año, esto es de los tipos de cultivos
   * cosechados en una parcela en un periodo definido por
   * dos fechas
   */
  public List<String> findTypeCropNamesCalculatedPerTotalHarvestPerTypeCropAndYear(int parcelId, Calendar dateFrom, Calendar dateUntil) {
    String subQuery = "SELECT RESULT_TABLE_TWO.HARVEST_YEAR, RESULT_TABLE_TWO.TYPE_CROP_ID, "
        + "SUM(RESULT_TABLE_TWO.HARVEST_AMOUNT) AS TOTAL_HARVEST_AMOUNT FROM "
        + "(SELECT YEAR(RESULT_TABLE.HARVEST_DATE) AS HARVEST_YEAR, RESULT_TABLE.TYPE_CROP_ID, RESULT_TABLE.HARVEST_AMOUNT "
        + "FROM (SELECT DATE AS HARVEST_DATE, FK_TYPE_CROP AS TYPE_CROP_ID, HARVEST_AMOUNT "
        + "FROM HARVEST JOIN CROP ON FK_CROP = CROP.ID "
        + "WHERE FK_PARCEL = ?1 AND ?2 <= DATE AND DATE <= ?3 "
        + "ORDER BY DATE) AS RESULT_TABLE) AS RESULT_TABLE_TWO "
        + "GROUP BY RESULT_TABLE_TWO.HARVEST_YEAR, RESULT_TABLE_TWO.TYPE_CROP_ID";

    String stringQuery = "SELECT NAME FROM TYPE_CROP JOIN (" + subQuery
        + ") AS RESULT_TABLE_THREE ON ID = RESULT_TABLE_THREE.TYPE_CROP_ID";

    Query query = getEntityManager().createNativeQuery(stringQuery);
    query.setParameter(1, parcelId);
    query.setParameter(2, dateFrom);
    query.setParameter(3, dateUntil);

    List<String> result = null;

    try {
      result = query.getResultList();
    } catch (NoResultException e) {
      e.printStackTrace();
    }

    return result;
  }

  /**
   * @param parcelId
   * @param dateFrom
   * @param dateUntil
   * @return referencia a un objeto de tipo List<Integer> que
   * contiene los años en los que se cosecharon los tipos de
   * cultivos para los que se calcula la cantidad total cosechada
   * [kg] por año, esto es de los tipos de cultivos cosechados en
   * una parcela en un periodo definido por dos fechas
   */
  public List<Integer> findYearsCalculatedPerTotalHarvestPerTypeCropAndYear(int parcelId, Calendar dateFrom, Calendar dateUntil) {
    String subQuery = "SELECT RESULT_TABLE_TWO.HARVEST_YEAR, RESULT_TABLE_TWO.TYPE_CROP_ID, "
        + "SUM(RESULT_TABLE_TWO.HARVEST_AMOUNT) AS TOTAL_HARVEST_AMOUNT FROM "
        + "(SELECT YEAR(RESULT_TABLE.HARVEST_DATE) AS HARVEST_YEAR, RESULT_TABLE.TYPE_CROP_ID, RESULT_TABLE.HARVEST_AMOUNT "
        + "FROM (SELECT DATE AS HARVEST_DATE, FK_TYPE_CROP AS TYPE_CROP_ID, HARVEST_AMOUNT "
        + "FROM HARVEST JOIN CROP ON FK_CROP = CROP.ID "
        + "WHERE FK_PARCEL = ?1 AND ?2 <= DATE AND DATE <= ?3 "
        + "ORDER BY DATE) AS RESULT_TABLE) AS RESULT_TABLE_TWO "
        + "GROUP BY RESULT_TABLE_TWO.HARVEST_YEAR, RESULT_TABLE_TWO.TYPE_CROP_ID";

    String stringQuery = "SELECT RESULT_TABLE_THREE.HARVEST_YEAR FROM (" + subQuery
        + ") AS RESULT_TABLE_THREE";

    Query query = getEntityManager().createNativeQuery(stringQuery);
    query.setParameter(1, parcelId);
    query.setParameter(2, dateFrom);
    query.setParameter(3, dateUntil);

    List<Integer> result = null;

    try {
      result = query.getResultList();
    } catch (NoResultException e) {
      e.printStackTrace();
    }

    return result;
  }

  public Page<StatisticalReport> findAllPagination(int userId, Integer page, Integer cantPerPage, Map<String, String> parameters) throws ParseException {
    SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
    Date date;
    Calendar calendarDate;

    // Genera el WHERE dinámicamente
    StringBuffer where = new StringBuffer(" WHERE 1=1 AND e IN (SELECT t FROM StatisticalReport t JOIN t.parcel p WHERE p IN (SELECT x FROM User u JOIN u.parcels x WHERE u.id = :userId))");

    if (parameters != null) {

      for (String param : parameters.keySet()) {
        Method method;

        try {
          method = StatisticalReport.class.getMethod("get" + capitalize(param));

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

              if (param.equals("dateFrom")) {
                date = new Date(dateFormatter.parse(parameters.get(param)).getTime());
                calendarDate = UtilDate.toCalendar(date);
                where.append(" AND e.");
                where.append(param);
                where.append(" >= ");
                where.append("'" + UtilDate.convertDateToYyyyMmDdFormat(calendarDate) + "'");
              }

              if (param.equals("dateUntil")) {
                date = new Date(dateFormatter.parse(parameters.get(param)).getTime());
                calendarDate = UtilDate.toCalendar(date);
                where.append(" AND e.");
                where.append(param);
                where.append(" <= ");
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
    Query countQuery = entityManager.createQuery("SELECT COUNT(e.id) FROM " + StatisticalReport.class.getSimpleName() + " e" + where.toString());
    countQuery.setParameter("userId", userId);

    // Pagina
    Query query = entityManager.createQuery("FROM " + StatisticalReport.class.getSimpleName() + " e" + where.toString());
    query.setMaxResults(cantPerPage);
    query.setFirstResult((page - 1) * cantPerPage);
    query.setParameter("userId", userId);

    Integer count = ((Long) countQuery.getSingleResult()).intValue();
    Integer lastPage = (int) Math.ceil((double) count / (double) cantPerPage);

    // Arma la respuesta
    Page<StatisticalReport> resultPage = new Page<StatisticalReport>(page, count, page > 1 ? page - 1 : page, page < lastPage ? page + 1 : lastPage, lastPage, query.getResultList());
    return resultPage;
  }

  private String capitalize(final String line) {
    return Character.toUpperCase(line.charAt(0)) + line.substring(1);
  }

}
