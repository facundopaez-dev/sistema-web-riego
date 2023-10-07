package stateless;

import java.util.Calendar;
import java.util.Collection;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import model.CropWaterActivityLog;

@Stateless
public class CropWaterActivityLogServiceBean {

    @PersistenceContext(unitName = "swcar")
    private EntityManager entityManager;

    public void setEntityManager(EntityManager localEntityManager) {
        entityManager = localEntityManager;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    /**
     * @param newCropWaterActivityLog
     * @return referencia a un objeto de tipo CropWaterActivityLog
     */
    public CropWaterActivityLog create(CropWaterActivityLog newCropWaterActivityLog) {
        getEntityManager().persist(newCropWaterActivityLog);
        return newCropWaterActivityLog;
    }

    /**
     * Actualiza el registro de actividad hidrica de cultivo
     * correspondiente a un ID dado con un nombre de cultivo,
     * una cantidad de agua evaporada [mm/dia], una cantidad
     * de agua [mm/dia], un deficit de agua evaporada [mm/dia]
     * y un deficit acumulado de agua evaporada [mm/dia]
     * 
     * @param id
     * @param cropName
     * @param evaporatedWater
     * @param water
     * @param deficit
     * @param accumulatedDeficit
     */
    public void update(int id, String cropName, double evaporatedWater, double water, double  deficit, double accumulatedDeficit) {
        Query query = getEntityManager().createQuery("UPDATE CropWaterActivityLog c SET c.cropName = :cropName, c.evaporatedWater = :evaporatedWater, c.water = :water, c.deficit = :deficit, c.accumulatedDeficit = :accumulatedDeficit WHERE c.id = :id");
        query.setParameter("cropName", cropName);
        query.setParameter("evaporatedWater", evaporatedWater);
        query.setParameter("water", water);
        query.setParameter("deficit", deficit);
        query.setParameter("accumulatedDeficit", accumulatedDeficit);
        query.setParameter("id", id);
        query.executeUpdate();
    }

    /**
     * @param userId
     * @param parcelName
     * @param cropName
     * @return referencia a un objeto de tipo Collection que
     * contiene todos los registros de actividad hidrica de
     * cultivo asociados a un usuario que tienen un nombre
     * de parcela y un nombre de cultivo
     */
    public Collection<CropWaterActivityLog> findAllByParcelNameAndCropName(int userId, String parcelName, String cropName) {
        Query query = getEntityManager().createQuery("SELECT c FROM CropWaterActivityLog c WHERE (c.userId = :userId AND c.parcelName = :parcelName AND c.cropName = :cropName) ORDER BY c.date");
        query.setParameter("userId", userId);
        query.setParameter("parcelName", parcelName);
        query.setParameter("cropName", cropName);

        return (Collection) query.getResultList();
    }

    /**
     * @param userId
     * @param parcelName
     * @param cropName
     * @param dateFrom
     * @return referencia a un objeto de tipo Collection que
     * contiene todos los registros de actividad hidrica de
     * cultivo asociados a un usuario que tienen un nombre de
     * parcela, un nombre de cultivo y una fecha mayor o igual
     * a una fecha desde
     */
    public Collection<CropWaterActivityLog> findAllByDateGreaterThanOrEqual(int userId, String parcelName, String cropName, Calendar dateFrom) {
        Query query = getEntityManager().createQuery("SELECT c FROM CropWaterActivityLog c WHERE (c.userId = :userId AND c.parcelName = :parcelName AND c.cropName = :cropName AND c.date >= :dateFrom) ORDER BY c.date");
        query.setParameter("userId", userId);
        query.setParameter("parcelName", parcelName);
        query.setParameter("cropName", cropName);
        query.setParameter("dateFrom", dateFrom);

        return (Collection) query.getResultList();
    }

    /**
     * @param userId
     * @param parcelName
     * @param cropName
     * @param dateUntil
     * @return referencia a un objeto de tipo Collection que
     * contiene todos los registros de actividad hidrica de
     * cultivo asociados a un usuario que tienen un nombre de
     * parcela, un nombre de cultivo y una fecha menor o igual
     * a una fecha hasta
     */
    public Collection<CropWaterActivityLog> findAllByDateLessThanOrEqual(int userId, String parcelName, String cropName, Calendar dateUntil) {
        Query query = getEntityManager().createQuery("SELECT c FROM CropWaterActivityLog c WHERE (c.userId = :userId AND c.parcelName = :parcelName AND c.cropName = :cropName AND c.date <= :dateUntil) ORDER BY c.date");
        query.setParameter("userId", userId);
        query.setParameter("parcelName", parcelName);
        query.setParameter("cropName", cropName);
        query.setParameter("dateUntil", dateUntil);

        return (Collection) query.getResultList();
    }

    /**
     * @param userId
     * @param parcelName
     * @param cropName
     * @param dateFrom
     * @param dateUntil
     * @return referencia a un objeto de tipo Collection que
     * contiene todos los registros de actividad hidrica de
     * cultivo asociados a un usuario que tienen una fecha
     * mayor o igual a una fecha desde y menor o igual a una
     * fecha hasta, un nombre de parcela y un nombre de cultivo
     */
    public Collection<CropWaterActivityLog> findByAllFilterParameters(int userId, String parcelName, String cropName, Calendar dateFrom, Calendar dateUntil) {
        Query query = getEntityManager().createQuery("SELECT c FROM CropWaterActivityLog c WHERE (c.userId = :userId AND c.date >= :dateFrom AND c.date <= :dateUntil AND c.parcelName = :parcelName AND c.cropName = :cropName) ORDER BY c.date");
        query.setParameter("userId", userId);
        query.setParameter("parcelName", parcelName);
        query.setParameter("cropName", cropName);
        query.setParameter("dateFrom", dateFrom);
        query.setParameter("dateUntil", dateUntil);

        return (Collection) query.getResultList();
    }

}
