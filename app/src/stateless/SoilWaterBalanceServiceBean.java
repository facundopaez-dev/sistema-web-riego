package stateless;

import java.util.Calendar;
import java.util.Collection;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.NoResultException;
import model.SoilWaterBalance;
import model.ClimateRecord;
import model.IrrigationRecord;
import irrigation.WaterMath;

@Stateless
public class SoilWaterBalanceServiceBean {

    @PersistenceContext(unitName = "swcar")
    private EntityManager entityManager;

    public void setEntityManager(EntityManager localEntityManager) {
        entityManager = localEntityManager;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    /**
     * @param newSoilWaterBalance
     * @return referencia a un objeto de tipo SoilWaterBalance
     */
    public SoilWaterBalance create(SoilWaterBalance newSoilWaterBalance) {
        getEntityManager().persist(newSoilWaterBalance);
        return newSoilWaterBalance;
    }

    /**
     * @param userId
     * @param date
     * @param parcelName
     * @return referencia a un objeto de tipo SoilWaterBalance si
     * se encuentra en la base de datos subyacente el balance hidrico
     * de suelo correspondiente a un usuario, una fecha y un nombre de
     * parcela. En caso contrario, null.
     */
    public SoilWaterBalance find(int userId, Calendar date, String parcelName) {
        Query query = getEntityManager().createQuery("SELECT c FROM SoilWaterBalance c WHERE (c.userId = :userId AND c.date = :date AND UPPER(c.parcelName) = UPPER(:parcelName))");
        query.setParameter("userId", userId);
        query.setParameter("date", date);
        query.setParameter("parcelName", parcelName);

        SoilWaterBalance givenSoilWaterBalance = null;

        try {
            givenSoilWaterBalance = (SoilWaterBalance) query.getSingleResult();
        } catch (NoResultException e) {
            e.printStackTrace();
        }

        return givenSoilWaterBalance;
    }

    /**
     * @param userId
     * @param date
     * @param parcelName
     * @return true si en la base de datos subyacente existe un
     * balance hidrico de suelo con el ID de usuario, la fecha y
     * el nombre de parcela. En caso contrario, false.
     */
    private boolean checkExistence(int userId, Calendar date, String parcelName) {
        return (find(userId, date, parcelName) != null);
    }

    /**
     * Actualiza el balance hidrico de suelo correspondiente a
     * un ID dado con un nombre de cultivo, una cantidad de agua
     * evaporada [mm/dia], una cantidad de agua provista [mm/dia],
     * un deficit de agua [mm/dia] y un deficit acumulado de agua
     * [mm/dia]
     * 
     * @param id
     * @param cropName
     * @param evaporatedWater
     * @param waterProvided
     * @param waterDeficit
     * @param accumulatedWaterDeficit
     */
    public void update(int id, String cropName, double evaporatedWater, double waterProvided, double waterDeficit, double accumulatedWaterDeficit) {
        Query query = getEntityManager().createQuery("UPDATE SoilWaterBalance c SET c.cropName = :cropName, c.evaporatedWater = :evaporatedWater, c.waterProvided = :waterProvided, c.waterDeficit = :waterDeficit, c.accumulatedWaterDeficit = :accumulatedWaterDeficit WHERE c.id = :id");
        query.setParameter("cropName", cropName);
        query.setParameter("evaporatedWater", evaporatedWater);
        query.setParameter("waterProvided", waterProvided);
        query.setParameter("waterDeficit", waterDeficit);
        query.setParameter("accumulatedWaterDeficit", accumulatedWaterDeficit);
        query.setParameter("id", id);
        query.executeUpdate();
    }

    /**
     * @param userId
     * @param parcelName
     * @param cropName
     * @return referencia a un objeto de tipo Collection que
     * contiene todos los balances hidricos de suelo asociados
     * a un usuario que tienen un nombre de parcela y un nombre
     * de cultivo
     */
    public Collection<SoilWaterBalance> findAllByParcelNameAndCropName(int userId, String parcelName, String cropName) {
        Query query = getEntityManager().createQuery("SELECT c FROM SoilWaterBalance c WHERE (c.userId = :userId AND c.parcelName = :parcelName AND c.cropName = :cropName) ORDER BY c.date");
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
     * contiene todos los balances hidricos de suelo asociados
     * a un usuario que tienen un nombre de parcela, un nombre
     * de cultivo y una fecha mayor o igual a una fecha desde
     */
    public Collection<SoilWaterBalance> findAllByDateGreaterThanOrEqual(int userId, String parcelName, String cropName, Calendar dateFrom) {
        Query query = getEntityManager().createQuery("SELECT c FROM SoilWaterBalance c WHERE (c.userId = :userId AND c.parcelName = :parcelName AND c.cropName = :cropName AND c.date >= :dateFrom) ORDER BY c.date");
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
     * contiene todos los balances hidricos de suelo asociados
     * a un usuario que tienen un nombre de parcela, un nombre
     * de cultivo y una fecha menor o igual a una fecha hasta
     */
    public Collection<SoilWaterBalance> findAllByDateLessThanOrEqual(int userId, String parcelName, String cropName, Calendar dateUntil) {
        Query query = getEntityManager().createQuery("SELECT c FROM SoilWaterBalance c WHERE (c.userId = :userId AND c.parcelName = :parcelName AND c.cropName = :cropName AND c.date <= :dateUntil) ORDER BY c.date");
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
     * contiene todos los balances hidricos de suelo asociados
     * a un usuario que tienen una fecha mayor o igual a una
     * fecha desde y menor o igual a una fecha hasta, un nombre
     * de parcela y un nombre de cultivo
     */
    public Collection<SoilWaterBalance> findByAllFilterParameters(int userId, String parcelName, String cropName, Calendar dateFrom, Calendar dateUntil) {
        Query query = getEntityManager().createQuery("SELECT c FROM SoilWaterBalance c WHERE (c.userId = :userId AND c.date >= :dateFrom AND c.date <= :dateUntil AND c.parcelName = :parcelName AND c.cropName = :cropName) ORDER BY c.date");
        query.setParameter("userId", userId);
        query.setParameter("parcelName", parcelName);
        query.setParameter("cropName", cropName);
        query.setParameter("dateFrom", dateFrom);
        query.setParameter("dateUntil", dateUntil);

        return (Collection) query.getResultList();
    }

    /**
     * Crea y persiste los balances hidricos de suelo con
     * determinadas fechas para una parcela que tiene un
     * cultivo en desarrollo cuando se calcula la necesidad
     * de agua de riego del mismo en la fecha actual. Si en
     * la base de datos subyacente existen los balances hidricos
     * de suelo para una parcela con determinadas fechas,
     * los actualiza.
     * 
     * @param userId
     * @param parcelName
     * @param cropName
     * @param climateRecords
     * @param irrigationRecords
     */
    public void generateSoilWaterBalances(int userId, String parcelName, String cropName, Collection<ClimateRecord> climateRecords,
            Collection<IrrigationRecord> irrigationRecords) {
        SoilWaterBalance givenSoilWaterBalance = null;

        double waterDeficitPerDay = 0.0;
        double accumulatedWaterDeficit = 0.0;
        double waterProvidedPerDay = 0.0;

        for (ClimateRecord currentClimateRecord : climateRecords) {

            /*
             * Si NO existe un balance hidrico de suelo correspondiente a
             * un usuario para una parcela en una fecha, se lo crea y persiste.
             * En caso contrario, se lo obtiene y se actualiza su nombre de
             * cultivo, su agua [mm/dia], su agua evaporada [mm/dia], su
             * deficit de agua [mm/dia] y su deficit acumulado de agua [mm/dia].
             */
            if (!checkExistence(userId, currentClimateRecord.getDate(), parcelName)) {
                /*
                 * Calcula el deficit de agua por dia [mm/dia] de un cultivo
                 * en una fecha, debido a que un registro climatico y un
                 * registro de riego tienen una fecha, y a que el metodo
                 * generateSoilWaterBalances debe ser invocado unicamente
                 * cuando se calcula la necesidad de agua de riego de un
                 * cultivo en la fecha actual [mm/dia]
                 */
                waterDeficitPerDay = WaterMath.calculateWaterDeficitPerDay(currentClimateRecord, irrigationRecords);

                /*
                 * Calcula el deficit acumulado de agua por dia [mm/dia] de
                 * un cultivo en una fecha, debido a que un registro climatico
                 * y un registro de riego tienen una fecha, y a que el metodo
                 * generateSoilWaterBalances debe ser invocado unicamente
                 * cuando se calcula la necesidad de agua de riego de un
                 * cultivo en la fecha actual [mm/dia]
                 */
                accumulatedWaterDeficit = WaterMath.calculateAccumulatedDeficitPerDay(waterDeficitPerDay, accumulatedWaterDeficit);

                /*
                 * Calcula el agua provista (lluvia o riego, o lluvia mas riego
                 * y viceversa) por dia [mm/dia] a un cultivo en fecha, debido
                 * a que un registro climatico y un registro de riego tienen
                 * una fecha, y a que el metodo generateSoilWaterBalances
                 * debe ser invocado unicamente cuando se calcula la necesidad
                 * de agua de riego de un cultivo en la fecha actual [mm/dia]
                 */
                waterProvidedPerDay = currentClimateRecord.getPrecip() + WaterMath.sumTotalAmountIrrigationWaterGivenDate(currentClimateRecord.getDate(), irrigationRecords);

                givenSoilWaterBalance = new SoilWaterBalance();
                givenSoilWaterBalance.setDate(currentClimateRecord.getDate());
                givenSoilWaterBalance.setParcelName(parcelName);
                givenSoilWaterBalance.setCropName(cropName);
                givenSoilWaterBalance.setEvaporatedWater(getEvaporatedWater(currentClimateRecord));
                givenSoilWaterBalance.setWaterProvided(waterProvidedPerDay);
                givenSoilWaterBalance.setWaterDeficit(waterDeficitPerDay);
                givenSoilWaterBalance.setAccumulatedWaterDeficit(accumulatedWaterDeficit);
                givenSoilWaterBalance.setUserId(userId);

                /*
                 * Persistencia del nuevo balance hidrico de suelo
                 */
                create(givenSoilWaterBalance);
            } else {
                waterDeficitPerDay = WaterMath.calculateWaterDeficitPerDay(currentClimateRecord, irrigationRecords);
                accumulatedWaterDeficit = WaterMath.calculateAccumulatedDeficitPerDay(waterDeficitPerDay, accumulatedWaterDeficit);
                waterProvidedPerDay = currentClimateRecord.getPrecip() + WaterMath.sumTotalAmountIrrigationWaterGivenDate(currentClimateRecord.getDate(), irrigationRecords);

                givenSoilWaterBalance = find(userId, currentClimateRecord.getDate(), parcelName);
                update(givenSoilWaterBalance.getId(), cropName, getEvaporatedWater(currentClimateRecord), waterProvidedPerDay, waterDeficitPerDay, accumulatedWaterDeficit);
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
