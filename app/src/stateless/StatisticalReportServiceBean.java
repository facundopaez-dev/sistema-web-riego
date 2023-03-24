package stateless;

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

  public StatisticalReport create(StatisticalReport newClimateRecord) {
    getEntityManager().persist(newClimateRecord);
    return newClimateRecord;
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
    StatisticalReport givenClimateRecord = find(userId, statisticalReportId);

    if (givenClimateRecord != null) {
      getEntityManager().remove(givenClimateRecord);
      return givenClimateRecord;
    }

    return null;
  }

  /**
   * Retorna un informe estadistico perteneciente a una de las
   * parcelas de un usuario
   * 
   * @param userId
   * @param statisticalReportId
   * @return referencia a un objeto de tipo StatisticalReport
   * perteneciente a una parcela del usuario con el ID dado
   */
  public StatisticalReport find(int userId, int statisticalReportId) {
    Query query = getEntityManager().createQuery("SELECT s FROM StatisticalReport s JOIN s.parcel p WHERE (s.id = :statisticalReportId AND p.user.id = :userId)");
    query.setParameter("statisticalReportId", statisticalReportId);
    query.setParameter("userId", userId);

    return (StatisticalReport) query.getSingleResult();
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
    Query query = getEntityManager().createQuery("SELECT s FROM StatisticalReport s JOIN s.parcel p WHERE (p.user.id = :userId) ORDER BY s.id");
    query.setParameter("userId", userId);

    return (Collection) query.getResultList();
  }

  /**
   * Retorna los informes estadisticos de una parcela mediante
   * el nombre de una parcela y el ID del usuario al que pertenece
   * una parcela
   * 
   * @param userId
   * @param givenParcelName
   * @return referencia a un objeto de tipo Collection que
   * contiene los informes estadisticos de la parcela que tiene
   * el nombre dado y que pertenece al usuario con el ID dado
   */
  public Collection<StatisticalReport> findAllByParcelName(int userId, String givenParcelName) {
    Query query = getEntityManager().createQuery("SELECT s FROM StatisticalReport s WHERE (s.parcel.name = :givenParcelName AND s.parcel.user.id = :userId) ORDER BY s.id");
    query.setParameter("userId", userId);
    query.setParameter("givenParcelName", givenParcelName);

    return (Collection) query.getResultList();
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
    boolean result = false;

    try {
      find(userId, statisticalReportId);
      result = true;
    } catch (NoResultException e) {
      e.printStackTrace();
    }

    return result;
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
     * año, la fecha hasta esta en el año de la fecha desde
     */
    if (numberDayYearDateUntil <= daysYear) {
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

}
