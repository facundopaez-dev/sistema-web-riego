package stateless;

import java.util.Calendar;
import java.util.Collection;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.NoResultException;
import model.CropWaterActivityLog;
import model.ClimateRecord;
import model.IrrigationRecord;
import irrigation.WaterMath;

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
     * @param userId
     * @param date
     * @param parcelName
     * @return referencia a un objeto de tipo CropWaterActivityLog si
     * se encuentra en la base de datos subyacente el registro de
     * actividad hidrica de cultivo correspondiente a un usuario, una
     * fecha y un nombre de parcela. En caso contrario, null.
     */
    public CropWaterActivityLog find(int userId, Calendar date, String parcelName) {
        Query query = getEntityManager().createQuery("SELECT c FROM CropWaterActivityLog c WHERE (c.userId = :userId AND c.date = :date AND UPPER(c.parcelName) = UPPER(:parcelName))");
        query.setParameter("userId", userId);
        query.setParameter("date", date);
        query.setParameter("parcelName", parcelName);

        CropWaterActivityLog givenCropWaterActivityLog = null;

        try {
            givenCropWaterActivityLog = (CropWaterActivityLog) query.getSingleResult();
        } catch (NoResultException e) {
            e.printStackTrace();
        }

        return givenCropWaterActivityLog;
    }

    /**
     * @param userId
     * @param date
     * @param parcelName
     * @return true si en la base de datos subyacente existe un
     * registro de actividad hidrica de cultivo con el ID de
     * usuario, la fecha y el nombre de parcela. En caso contrario,
     * false.
     */
    private boolean checkExistence(int userId, Calendar date, String parcelName) {
        return (find(userId, date, parcelName) != null);
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

    /**
     * Crea y persiste los registros de actividad hidrica de
     * cultivo con determinadas fechas para una parcela que
     * tiene un cultivo en desarrollo cuando se calcula la
     * necesidad de agua de riego del mismo en la fecha actual.
     * Si en la base de datos subyacente existen los registros
     * de actividad hidrica para una parcela con determinadas
     * fechas, los actualiza.
     * 
     * @param userId
     * @param parcelName
     * @param cropName
     * @param climateRecords
     * @param irrigationRecords
     */
    public void generateLogs(int userId, String parcelName, String cropName, Collection<ClimateRecord> climateRecords,
            Collection<IrrigationRecord> irrigationRecords) {
        CropWaterActivityLog givenCropWaterActivityLog = null;

        double deficitPerDay = 0.0;
        double accumulatedDeficit = 0.0;
        double waterPerDay = 0.0;

        for (ClimateRecord currentClimateRecord : climateRecords) {

            /*
             * Si NO existe un registro de actividad hidrica de cultivo
             * correspondiente a un usuario para una parcela en una fecha,
             * se lo crea y persiste. En caso contrario, se lo obtiene y
             * se actualiza su nombre de cultivo, su agua [mm/dia], su
             * agua evaporada [mm/dia], su deficit de agua [mm/dia] y su
             * deficit acumulado de agua [mm/dia]
             */
            if (!checkExistence(userId, currentClimateRecord.getDate(), parcelName)) {
                /*
                 * Calcula el deficit de agua por dia [mm/dia] de un cultivo
                 * en una fecha, debido a que un registro climatico y un
                 * registro de riego tienen una fecha, y a que el metodo
                 * generateLogs debe ser invocado unicamente cuando se
                 * calcula la necesidad de agua de riego de un cultivo
                 * en la fecha actual [mm/dia]
                 */
                deficitPerDay = WaterMath.calculateDeficitPerDay(currentClimateRecord, irrigationRecords);

                /*
                 * Calcula el deficit acumulado de agua por dia [mm/dia] de
                 * un cultivo en una fecha, debido a que un registro climatico
                 * y un registro de riego tienen una fecha, y a que el metodo
                 * generateLogs debe ser invocado unicamente cuando se calcula
                 * la necesidad de agua de riego de un cultivo en la fecha
                 * actual [mm/dia]
                 */
                accumulatedDeficit = WaterMath.calculateAccumulatedDeficit(deficitPerDay, accumulatedDeficit);

                /*
                 * Calcula el agua provista (lluvia o riego, o lluvia mas riego
                 * y viceversa) por dia [mm/dia] a un cultivo en fecha, debido
                 * a que un registro climatico y un registro de riego tienen
                 * una fecha, y a que el metodo generateLogs debe ser invocado
                 * unicamente cuando se calcula la necesidad de agua de riego
                 * de un cultivo en la fecha actual [mm/dia]
                 */
                waterPerDay = currentClimateRecord.getPrecip() + WaterMath.sumTotalAmountIrrigationWaterGivenDate(currentClimateRecord.getDate(), irrigationRecords);

                givenCropWaterActivityLog = new CropWaterActivityLog();
                givenCropWaterActivityLog.setDate(currentClimateRecord.getDate());
                givenCropWaterActivityLog.setParcelName(parcelName);
                givenCropWaterActivityLog.setCropName(cropName);
                givenCropWaterActivityLog.setEvaporatedWater(getEvaporatedWater(currentClimateRecord));
                givenCropWaterActivityLog.setWater(waterPerDay);
                givenCropWaterActivityLog.setDeficit(deficitPerDay);
                givenCropWaterActivityLog.setAccumulatedDeficit(accumulatedDeficit);
                givenCropWaterActivityLog.setUserId(userId);

                /*
                 * Persistencia del nuevo registro de actividad hidrica
                 * de cultivo
                 */
                create(givenCropWaterActivityLog);
            } else {
                deficitPerDay = WaterMath.calculateDeficitPerDay(currentClimateRecord, irrigationRecords);
                accumulatedDeficit = WaterMath.calculateAccumulatedDeficit(deficitPerDay, accumulatedDeficit);
                waterPerDay = currentClimateRecord.getPrecip() + WaterMath.sumTotalAmountIrrigationWaterGivenDate(currentClimateRecord.getDate(), irrigationRecords);

                givenCropWaterActivityLog = find(userId, currentClimateRecord.getDate(), parcelName);
                update(givenCropWaterActivityLog.getId(), cropName, getEvaporatedWater(currentClimateRecord), waterPerDay, deficitPerDay, accumulatedDeficit);
            }

        }

    }

    /**
     * @param climateRecord
     * @return double que representa el agua evaporada, la cual
     * puede ser la ETc o la ETo en caso de que la ETc = 0
     */
    private double getEvaporatedWater(ClimateRecord climateRecord) {
        /*
         * Cuando una parcela NO tuvo un cultivo sembrado en una fecha,
         * la ETc [mm/dia] del registro climatico correspondiente a
         * dicha fecha y perteneciente a una parcela, tiene el valor
         * 0.0. Esto se debe a que si NO hubo un cultivo sembrado en
         * una parcela en una fecha, NO es posible calcular la ETc
         * (evapotranspiracion del cultivo bajo condiciones estandar)
         * del mismo. Por lo tanto, en este caso se debe utilizar la ETo
         * [mm/dia] (evapotranspiracion del cultivo de referencia) para
         * calcular la diferencia entre la cantidad de agua provista
         * (lluvia o riego, o lluvia mas riego) [mm/dia] y la cantidad
         * de agua evaporada [mm/dia] en una fecha en una parcela. En
         * caso contrario, se debe utilizar la ETc.
         * 
         * El motivo de la expresion "en una fecha en una parcela" es
         * que un registro climatico pertenece a una parcela y tiene
         * una fecha.
         */
        if (climateRecord.getEtc() == 0.0) {
            return climateRecord.getEto();
        }

        return climateRecord.getEtc();
    }

}
