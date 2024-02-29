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
   * @param parcelId
   * @param dateFrom
   * @param dateUntil
   * @return referencia a un objeto de tipo List<Integer> que
   * contiene los numeros que representan la cantidad total de
   * veces que se plantaron los cultivos en una parcela en un
   * periodo definido por dos fechas
   */
  public List<Integer> calculateTotalNumberPlantationsPerCrop(int parcelId, Calendar dateFrom, Calendar dateUntil) {
    String subQuery = "SELECT FK_CROP, COUNT(FK_CROP) AS NUMBER_PLANTATIONS FROM PLANTING_RECORD "
        + "WHERE FK_PARCEL = ?1 AND FK_STATUS = 1 AND "
        + "((?2 <= SEED_DATE AND SEED_DATE <= ?3 AND HARVEST_DATE > ?3) "
        + "OR (SEED_DATE >= ?2 AND HARVEST_DATE <= ?3) OR "
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
    String subQuery = "SELECT FK_CROP, COUNT(FK_CROP) AS NUMBER_PLANTATIONS FROM PLANTING_RECORD WHERE "
        + "FK_PARCEL = ?1 AND FK_STATUS = 1 AND ((?2 <= SEED_DATE AND SEED_DATE <= ?3 AND "
        + "HARVEST_DATE > ?3) OR (SEED_DATE >= ?2 AND HARVEST_DATE <= ?3) OR (?2 <= HARVEST_DATE "
        + "AND HARVEST_DATE <= ?3 AND SEED_DATE < ?2)) GROUP BY FK_CROP";

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
    String subQuery = "SELECT YEAR(SEED_DATE) AS YEAR_SEED_DATE, FK_CROP, COUNT(FK_CROP) AS NUMBER_PLANTATIONS FROM PLANTING_RECORD "
        + "WHERE FK_PARCEL = ?1 AND FK_STATUS = 1 AND "
        + "((?2 <= SEED_DATE AND SEED_DATE <= ?3 AND HARVEST_DATE > ?3) "
        + "OR (SEED_DATE >= ?2 AND HARVEST_DATE <= ?3) OR "
        + "(?2 <= HARVEST_DATE AND HARVEST_DATE <= ?3 AND SEED_DATE < ?2)) "
        + "GROUP BY YEAR(SEED_DATE), FK_CROP ORDER BY YEAR(SEED_DATE)";

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
   * contiene los nombres de los cultivos para los que se calcula
   * la cantidad total de veces que se plantaron por año en
   * una parcela en un periodo definido por dos fechas
   */
  public List<String> findCropNamesCalculatedPerTotalNumberPlantationsPerCropAndYear(int parcelId, Calendar dateFrom, Calendar dateUntil) {
    String subQuery = "SELECT YEAR(SEED_DATE) AS YEAR_SEED_DATE, FK_CROP, COUNT(FK_CROP) AS NUMBER_PLANTATIONS FROM PLANTING_RECORD "
        + "WHERE FK_PARCEL = ?1 AND FK_STATUS = 1 AND "
        + "((?2 <= SEED_DATE AND SEED_DATE <= ?3 AND HARVEST_DATE > ?3) "
        + "OR (SEED_DATE >= ?2 AND HARVEST_DATE <= ?3) OR "
        + "(?2 <= HARVEST_DATE AND HARVEST_DATE <= ?3 AND SEED_DATE < ?2)) "
        + "GROUP BY YEAR(SEED_DATE), FK_CROP ORDER BY YEAR(SEED_DATE)";

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
   * contiene los años en los que se sembraron los cultivos
   * para los que se calcula la cantidad total de veces que se
   * plantaron por año en una parcela en un periodo definido
   * por dos fechas
   */
  public List<Integer> findSeedYearCalculatedPerTotalNumberPlantationsPerCropAndYear(int parcelId, Calendar dateFrom, Calendar dateUntil) {
    String subQuery = "SELECT YEAR(SEED_DATE) AS YEAR_SEED_DATE, FK_CROP, COUNT(FK_CROP) AS NUMBER_PLANTATIONS FROM PLANTING_RECORD "
        + "WHERE FK_PARCEL = ?1 AND FK_STATUS = 1 AND "
        + "((?2 <= SEED_DATE AND SEED_DATE <= ?3 AND HARVEST_DATE > ?3) "
        + "OR (SEED_DATE >= ?2 AND HARVEST_DATE <= ?3) OR "
        + "(?2 <= HARVEST_DATE AND HARVEST_DATE <= ?3 AND SEED_DATE < ?2)) "
        + "GROUP BY YEAR(SEED_DATE), FK_CROP ORDER BY YEAR(SEED_DATE)";

    String stringQuery = "SELECT RESULT_TABLE.YEAR_SEED_DATE FROM (" + subQuery
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
   * @param cropNames
   * @param seedYears
   * @return referencia a un objeto de tipo List<String> que
   * contiene las etiquetas <cultivo> (<año>) para el grafico
   * de barras que representa la cantidad de veces que se
   * plantaron los cultivos por año en una parcela durante
   * un periodo definido por dos fechas
   */
  public List<String> setLabelsOfCalculatedPerTotalNumberPlantationsPerCropAndYear(List<String> cropNames, List<Integer> seedYears) {
    List<String> labels = new ArrayList<>();

    for (int i = 0; i < cropNames.size(); i++) {
      labels.add(new String(cropNames.get(i) + " (" + seedYears.get(i) + ")"));
    }

    return labels;
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
