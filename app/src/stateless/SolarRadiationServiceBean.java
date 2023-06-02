package stateless;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import model.Latitude;
import model.Month;
import model.SolarRadiation;

@Stateless
public class SolarRadiationServiceBean {

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

  /**
   * Recupera de la base de datos subyacente la radiacion solar
   * extraterrestre (Ra) correspondiente al mes y latitud
   * dados
   *
   * La latitud va de 0 a -70 grados decimales porque en
   * la base de datos estan cargadas las radiaciones
   * solares extraterrestres del hemisferio sur
   *
   * @param  month [1 ... 2]
   * @param  latitude [0 .. -70]
   * @return radiacion solar extraterrestre [MJ/metro cuadrado * dia]
   */
  public SolarRadiation find(Month month, Latitude latitude) {
    Query query = entityManager.createQuery("SELECT s FROM SolarRadiation s WHERE s.month = :month AND s.decimalLatitude = :latitude");
    query.setParameter("month", month);
    query.setParameter("latitude", latitude);
    return (SolarRadiation) query.getSingleResult();
  }

  /**
   * @param doubleLatitude
   * @param month
   * @param latitude
   * @param previousLatitude
   * @param nextLatitude
   * @return double que representa la radiacion solar correspondiente
   * a un mes y una latitud
   */
  public double getRadiation(double doubleLatitude, Month month, Latitude latitude, Latitude previousLatitude, Latitude nextLatitude) {
    SolarRadiation previousRadiation = null;
    SolarRadiation nextRadiation = null;
    int intLatitude = (int) doubleLatitude;

    /*
     * Si la latitud es impar, se recuperan las radiaciones
     * solares aledañas a la radiacion solar correspondiente
     * a dicha latitud, y a partir de estas se calcula y
     * retorna la radiacion solar promedio
     */
    if ((intLatitude % 2) != 0) {
      previousRadiation = find(month, previousLatitude);
      nextRadiation = find(month, nextLatitude);
      return ((previousRadiation.getRadiation() + nextRadiation.getRadiation()) / 2.0);
    }

    /*
     * Si la latitud no es impar se retorna el valor
     * de la radiacion solar sin promediarla
     */
    return find(month, latitude).getRadiation();
  }

}
