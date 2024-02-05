package stateless;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Collection;
import java.util.Calendar;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
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
   * Calcula una fecha hasta a partir de una fecha desde
   * mas el menor ciclo de vida (medido en dias) del ciclo
   * de vida de los cultivos registrados en la base de datos
   * subyacente
   * 
   * @param dateFrom
   * @param shorterLifeCycle
   * @return referencia a un objeto de tipo Calendar que
   * contiene la fecha hasta calculada a partir de la
   * fecha desde y el menor ciclo de vida (medido en dias)
   * del ciclo de vida de los cultivos registrados en la
   * base de datos subyacente
   */
  public Calendar calculateDateUntil(Calendar dateFrom, int shorterLifeCycle) {
    Calendar dateUntil = Calendar.getInstance();

    /*
     * A la suma entre el numero de dia en el año de la fecha
     * desde y el menor ciclo de vida (medido en dias) se le
     * resta un uno para generar la fecha hasta contando desde
     * la fecha desde.
     * 
     * Si no se hace esto, la fecha hasta sera igual al numero
     * de dia en el año de la fecha desde mas el menor ciclo de
     * vida mas un dia, lo cual, da como resultado que la cantidad
     * de dias entre la fecha desde y la fecha hasta sea un dia
     * mayor al menor ciclo de vida.
     */
    int numberDayYearDateUntil = dateFrom.get(Calendar.DAY_OF_YEAR) + shorterLifeCycle - 1;
    int daysYear = 365;

    /*
     * Si el año de la fecha desde es bisiesto, la cantidad de
     * dias en el año utilizada para calcular la fecha hasta
     * es 366
     */
    if (UtilDate.isLeapYear(dateFrom.get(Calendar.YEAR))) {
      daysYear = 366;
    }

    /*
     * Si la cantidad de dias de la fecha hasta generada a
     * partir del numero de dia en el año de la fecha desde
     * y el menor ciclo de vida (medido en dias) menos uno,
     * es menor o igual a la cantidad de dias que hay en el
     * año de la fecha desde, la fecha hasta esta en el mismo
     * año que la fecha desde
     */
    if (numberDayYearDateUntil <= daysYear) {
      dateUntil.set(Calendar.YEAR, dateFrom.get(Calendar.YEAR));
      dateUntil.set(Calendar.DAY_OF_YEAR, numberDayYearDateUntil);
      return dateUntil;
    }

    /*
     * Si la cantidad de dias de la fecha hasta calculada de la
     * suma entre el numero de dia en el año de la fecha desde y
     * el menor ciclo de vida (medido en dias) menos uno, es mayor
     * a la cantidad de dias en el año, la fecha hasta esta en el
     * año siguiente al año de la fecha desde y su numero de dia
     * en el año se calcula de la siguiente manera:
     * 
     * numero de dia de la fecha hasta = menor ciclo de vida
     * - (numero de dias del año - numero de dia en el año de
     * la fecha desde) - 1
     * 
     * El "- 1" es para generar la fecha hasta contando desde
     * la fecha desde. Si no se realiza esta resta, la fecha
     * hasta sera igual al numero de dia en el año de la fecha
     * desde mas el menor ciclo de vida mas un dia, lo cual, da
     * como resultado que la cantidad de dias entre la fecha desde
     * y la fecha hasta sea un dia mayor al menor ciclo de vida.
     */
    numberDayYearDateUntil = shorterLifeCycle - (daysYear - dateFrom.get(Calendar.DAY_OF_YEAR)) - 1;
    dateUntil.set(Calendar.YEAR, (dateFrom.get(Calendar.YEAR) + 1));
    dateUntil.set(Calendar.DAY_OF_YEAR, numberDayYearDateUntil);
    return dateUntil;
  }

  public Page<StatisticalReport> findAllPagination(int userId, Integer page, Integer cantPerPage, Map<String, String> parameters) {
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
            default:
              where.append(" AND e.");
              where.append(param);
              where.append(" = ");
              where.append(parameters.get(param));
              break;
          }

        } catch (NoSuchMethodException | SecurityException e) {
          // TODO Auto-generated catch block
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
