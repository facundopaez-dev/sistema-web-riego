package stateless;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import model.Latitude;
import model.MaximumInsolation;
import model.Month;

@Stateless
public class MaximumInsolationServiceBean {

  @PersistenceContext(unitName = "swcar")
  private EntityManager entityManager;

  public void setEntityManager(EntityManager localEntityManager) {
    entityManager = localEntityManager;
  }

  public EntityManager getEntityManager() {
    return entityManager;
  }

  /**
   * Recupera de la base de datos subyacente la maxima
   * insolacion diaria (N) correspondiente al mes y la
   * latitud dados
   *
   * La latitud va de 0 a -70 grados decimales porque
   * en la base de datos estan cargadas las insolaciones
   * maximas diarias del hemisferio sur
   *
   * @param  month [1 .. 12]
   * @param  latitude [0 .. -70]
   * @return insolacion maxima diaria [MJ/metro cuadrado * dia]
   */
  public MaximumInsolation find(Month month, Latitude latitude) {
    Query query = entityManager.createQuery("SELECT m FROM MaximumInsolation m WHERE m.month = :month AND m.decimalLatitude = :latitude");
    query.setParameter("month", month);
    query.setParameter("latitude", latitude);
    return (MaximumInsolation) query.getSingleResult();
  }

  /**
   * @param doubleLatitude
   * @param month
   * @param latitude
   * @param previousLatitude
   * @param nextLatitude
   * @return double que representa la insolacion maxima correspondiente
   * a un mes y una latitud
   */
  public double getInsolation(double doubleLatitude, Month month, Latitude latitude, Latitude previousLatitude, Latitude nextLatitude) {
    MaximumInsolation previousInsolation = null;
    MaximumInsolation nextInsolation = null;
    int intLatitude = (int) doubleLatitude;

    /*
     * Si la latitud es impar se recuperan las latitudes
     * aledañas a la latitud impar y a partir de estas
     * dos latitudes se recuperan sus correspondientes
     * insolaciones maximas con las cuales se calcula
     * y retorna la insolacion maxima promedio
     */
    if ((intLatitude % 2) != 0) {
      previousInsolation = find(month, previousLatitude);
      nextInsolation = find(month, nextLatitude);
      return ((previousInsolation.getInsolation() + nextInsolation.getInsolation()) / 2.0);
    }

    /*
     * Si la latitud no es impar se retorna
     * la insolacion maxima sin promediarla
     */
    return find(month, latitude).getInsolation();
  }

}
