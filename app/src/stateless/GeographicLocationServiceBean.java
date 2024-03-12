package stateless;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import model.GeographicLocation;

@Stateless
public class GeographicLocationServiceBean {

  @PersistenceContext(unitName = "swcar")
  protected EntityManager entityManager;

  public void setEntityManager(EntityManager localEntityManager) {
    entityManager = localEntityManager;
  }

  public EntityManager getEntityManager() {
    return entityManager;
  }


  /**
   * Persiste en la base de datos subyacente una instancia
   * de tipo GeographicLocation
   * 
   * @param newGeographicLocation
   * @return referencia a un objeto de tipo GeographicLocation
   */
  public GeographicLocation create(GeographicLocation newGeographicLocation) {
    entityManager.persist(newGeographicLocation);
    return newGeographicLocation;
  }

  /**
   * @param  id
   * @return referencia a un objeto de tipo GeographicLocation
   * en caso de eliminarse de la base de datos subyacente la
   * ubicacion geografica correspondiente al ID dado, en caso
   * contrario null (referencia a nada)
   */
  public GeographicLocation remove(int id) {
    GeographicLocation geographicLocation = find(id);

    if (geographicLocation != null) {
      entityManager.remove(geographicLocation);
      return geographicLocation;
    }

    return null;
  }

  public GeographicLocation find(int id) {
    return getEntityManager().find(GeographicLocation.class, id);
  }

  /**
   * @param geographicLocationId
   * @param modifiedGeographicLocation
   * @return referencia a un objeto de tipo GeographicLocation
   * si se modifica la ubicacion geografica con el ID provisto,
   * en caso contrario null
   */
  public GeographicLocation modify(int geographicLocationId, GeographicLocation modifiedGeographicLocation) {
    GeographicLocation geographicLocation = find(geographicLocationId);

    if (geographicLocation != null) {
      geographicLocation.setLatitude(modifiedGeographicLocation.getLatitude());
      geographicLocation.setLongitude(modifiedGeographicLocation.getLongitude());
      return geographicLocation;
    }

    return null;
  }

}
