package stateless;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import model.Latitude;

@Stateless
public class LatitudeServiceBean {

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

  public Latitude create(Latitude latitude) {
    entityManager.persist(latitude);
    return latitude;
  }

  /**
   * Recupera, haciendo uso de un valor de latitud, de
   * la base de datos subyacente un registro de la tabla
   * de latitudes convertido en objeto de tipo Latitude
   *
   * @param  latitudeValue
   * @return latitude
   */
  public Latitude find(int latitudeValue) {
    Query query = entityManager.createQuery("SELECT l FROM Latitude l WHERE l.decimalLatitude = :latitudeValue");
    query.setParameter("latitudeValue", latitudeValue);
    return (Latitude) query.getSingleResult();
  }

  /**
   * Retorna una referencia a un objeto de tipo Latitude si
   * y solo si el valor de la latitud es par. Esto se debe
   * a que los valores de las latitudes en la base de datos
   * subyacente son pares. El motivo de esto es que el libro
   * "Evapotranspiracion del cultivo" de la FAO utiliza latitudes
   * pares para las radiaciones solares extraterrestres diarias
   * (cuadro A2.6, pagina 217) y las insolaciones maximas diarias
   * (cuadro A2.7, pagina 218).
   * 
   * @param latitude
   * @return referencia a un objeto de tipo Latitude si
   * el valor de latitud es par, en caso contrario null
   */
  public Latitude find(double latitude) {
    int intLatitude = (int) latitude;

    if ((intLatitude % 2) == 0) {
      return find(intLatitude);
    }

    return null;
  }

  /**
   * @param doubleLatitude
   * @return referencia a un objeto de tipo Latitude que
   * representa la latitud previa a una latitud dada
   */
  public Latitude findPreviousLatitude(double doubleLatitude) {
    int intLatitude = (int) doubleLatitude;

    /*
     * Si la latitud es igual a cero, se retorna la
     * latitud correspondiente al grado -2 como la
     * latitud previa a la latitud cero
     */
    if (intLatitude == 0) {
      return find(intLatitude - 2);
    }

    /*
     * Si la latitud es mayor a cero, se retorna la
     * latitud positiva inmediatamente inferior a la
     * latitud mayor a cero
     */
    if (intLatitude > 0) {
      return findPositivePreviousLatitude(intLatitude);
    }

    /*
     * Si la latitud es menor a cero, se retorna la
     * latitud negativa inmediatamente superior a la
     * latitud menor a cero
     */
    return findNegativePreviousLatitude(intLatitude);
  }

  /**
   * @param latitude
   * @return referencia a un objeto de tipo Latitude que
   * representa la latitud positiva previa a una latitud
   * dada
   */
  private Latitude findPositivePreviousLatitude(int latitude) {
    /*
     * Si la latitud es impar, se retorna la latitud
     * positiva inmediatamente inferior a la latitud
     * impar
     */
    if ((latitude % 2) != 0) {
      return find(latitude - 1);
    }

    /*
     * Si la latitud es par, se retorna la latitud
     * positiva correspondiente a dos grados menos
     * desde la latitud par
     */
    return find(latitude - 2);
  }

  /**
   * @param latitude
   * @return referencia a un objeto de tipo Latitude que
   * representa la latitud negativa previa a una latitud
   * dada
   */
  private Latitude findNegativePreviousLatitude(int latitude) {
    /*
     * Si la latitud es impar, se retorna la latitud
     * negativa inmediatamente superior a la latitud
     * impar
     */
    if ((latitude % 2) != 0) {
      return find(latitude + 1);
    }

    /*
     * Si la latitud es par, se retorna la latitud
     * negativa correspondiente a dos grados mas
     * desde la latitud par
     */
    return find(latitude + 2);
  }

  /**
   * @param doubleLatitude
   * @return referencia a un objeto de tipo Latitude que
   * representa la latitud siguiente a una latitud dada
   */
  public Latitude findNextLatitude(double doubleLatitude) {
    int intLatitude = (int) doubleLatitude;

    /*
     * Si la latitud es igual a cero, se retorna la
     * latitud correspondiente al grado 2 como la
     * latitud siguiente a la latitud cero
     */
    if (intLatitude == 0) {
      return find(intLatitude + 2);
    }

    /*
     * Si la latitud es mayor a cero, se retorna la
     * latitud positiva inmediatamente superior a la
     * latitud mayor a cero
     */
    if (intLatitude > 0) {
      return findPositiveNextLatitude(intLatitude);
    }

    /*
     * Si la latitud es menor a cero, se retorna la
     * latitud negativa inmediatamente inferior a la
     * latitud menor a cero
     */
    return findNegativeNextLatitude(intLatitude);
  }

  /**
   * @param latitude
   * @return referencia a un objeto de tipo Latitude que
   * representa la latitud positiva siguiente a una latitud
   * dada
   */
  private Latitude findPositiveNextLatitude(int latitude) {
    /*
     * Si la latitud es impar, se retorna la latitud
     * inmediatamente superior a la latitud impar
     */
    if ((latitude % 2) != 0) {
      return find(latitude + 1);
    }

    /*
     * Si la latitud es par, se retorna la latitud
     * correspondiente a dos grados mas desde la
     * latitud par
     */
    return find(latitude + 2);
  }

  /**
   * @param latitude
   * @return referencia a un objeto de tipo Latitude que
   * representa la latitud negativa siguiente a una latitud
   * dada
   */
  private Latitude findNegativeNextLatitude(int latitude) {
    /*
     * Si la latitud es impar, se retorna la latitud
     * inmediatamente inferior a la latitud impar
     */
    if ((latitude % 2) != 0) {
      return find(latitude - 1);
    }

    /*
     * Si la latitud es par, se retorna la latitud
     * correspondiente a dos grados menos desde la
     * latitud par
     */
    return find(latitude - 2);
  }

}
