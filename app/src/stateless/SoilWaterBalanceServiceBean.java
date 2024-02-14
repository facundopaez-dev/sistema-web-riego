package stateless;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Map;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.NoResultException;
import model.SoilWaterBalance;
import model.ClimateRecord;
import model.IrrigationRecord;
import model.Parcel;
import irrigation.WaterMath;
import util.UtilDate;

@Stateless
public class SoilWaterBalanceServiceBean {

    @PersistenceContext(unitName = "swcar")
    private EntityManager entityManager;

    /*
     * El valor de esta constante se utiliza para representar
     * la situacion en la que NO se calcula el acumulado del
     * deficit de agua por dia de dias previos a una fecha de
     * un balance hidrico de suelo de una parcela que tiene
     * un cultivo sembrado y en desarrollo. Esta situacion
     * ocurre cuando el nivel de humedad de un suelo, que tiene
     * un cultivo sembrado, es estrictamente menor al doble de
     * la capacidad de almacenamiento de agua del mismo.
     */
    private final String NOT_CALCULATED = "NC";

    public String getNotCalculated() {
        return NOT_CALCULATED;
    }

    public void setEntityManager(EntityManager localEntityManager) {
        entityManager = localEntityManager;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    /**
     * 
     * @param newSoilWaterBalance
     * @return referencia a un objeto de tipo SoilWaterBalance
     */
    public SoilWaterBalance create(SoilWaterBalance newSoilWaterBalance) {
        entityManager.persist(newSoilWaterBalance);
        return newSoilWaterBalance;
    }

    /**
     * Modifica el balance hidrico de suelo de una parcela y una
     * fecha
     * 
     * @param parcelId
     * @param date
     * @param modifiedSoilWaterBalance
     * @return referencia a un objeto de tipo SoilWaterBalance si
     * se modifica el balance hidrico de de una parcela y una
     * fecha. En caso contrario null.
     */
    public SoilWaterBalance modify(int parcelId, Calendar date, SoilWaterBalance modifiedSoilWaterBalance) {
        SoilWaterBalance chosenSoilWaterBalanace = find(parcelId, date);

        if (chosenSoilWaterBalanace != null) {
            chosenSoilWaterBalanace.setParcelName(modifiedSoilWaterBalance.getParcelName());
            chosenSoilWaterBalanace.setCropName(modifiedSoilWaterBalance.getCropName());
            chosenSoilWaterBalanace.setWaterProvided(modifiedSoilWaterBalance.getWaterProvided());
            chosenSoilWaterBalanace.setEvaporatedWater(modifiedSoilWaterBalance.getEvaporatedWater());
            chosenSoilWaterBalanace.setWaterDeficitPerDay(modifiedSoilWaterBalance.getWaterDeficitPerDay());
            chosenSoilWaterBalanace.setAccumulatedWaterDeficitPerDay(modifiedSoilWaterBalance.getAccumulatedWaterDeficitPerDay());
            return chosenSoilWaterBalanace;
        }

        return null;
    }

    /**
     * @param parcelId
     * @param date
     * @return referencia a un objeto de tipo SoilWaterBalance si
     * se encuentra en la base de datos subyacente el balance hidrico
     * de suelo correspondiente a una parcela y una fecha. En caso
     * contrario, null.
     */
    public SoilWaterBalance find(int parcelId, Calendar date) {
        Query query = getEntityManager().createQuery("SELECT s FROM Parcel p JOIN p.soilWaterBalances s WHERE (p.id = :parcelId AND s.date = :date)");
        query.setParameter("parcelId", parcelId);
        query.setParameter("date", date);

        SoilWaterBalance givenSoilWaterBalance = null;

        try {
            givenSoilWaterBalance = (SoilWaterBalance) query.getSingleResult();
        } catch (NoResultException e) {
            e.printStackTrace();
        }

        return givenSoilWaterBalance;
    }

    /**
     * @param parcelId
     * @param date
     * @return true si en la base de datos subyacente existe un
     * balance hidrico de suelo con un ID de parcela y una fecha.
     * En caso contrario, false.
     */
    public boolean checkExistence(int parcelId, Calendar date) {
        return (find(parcelId, date) != null);
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
     * @param waterDeficitPerDay
     * @param accumulatedWaterDeficitPerDay
     */
    public void update(int id, String cropName, double evaporatedWater, double waterProvided, double waterDeficitPerDay, String accumulatedWaterDeficitPerDay) {
        Query query = getEntityManager().createQuery("UPDATE SoilWaterBalance s SET s.cropName = :cropName, s.evaporatedWater = :evaporatedWater, s.waterProvided = :waterProvided, s.waterDeficitPerDay = :waterDeficitPerDay, s.accumulatedWaterDeficitPerDay = :accumulatedWaterDeficitPerDay WHERE s.id = :id");
        query.setParameter("cropName", cropName);
        query.setParameter("evaporatedWater", evaporatedWater);
        query.setParameter("waterProvided", waterProvided);
        query.setParameter("waterDeficitPerDay", waterDeficitPerDay);
        query.setParameter("accumulatedWaterDeficitPerDay", accumulatedWaterDeficitPerDay);
        query.setParameter("id", id);
        query.executeUpdate();
    }

    /**
     * @param parcelId
     * @param cropName
     * @return referencia a un objeto de tipo Collection que
     * contiene todos los balances hidricos de suelo de una
     * parcela que tienen un nombre de cultivo
     */
    public Collection<SoilWaterBalance> findAllByParcelIdAndCropName(int parcelId, String cropName) {
        Query query = getEntityManager().createQuery("SELECT s FROM Parcel p JOIN p.soilWaterBalances s WHERE (p.id = :parcelId AND s.cropName = :cropName) ORDER BY s.date");
        query.setParameter("parcelId", parcelId);
        query.setParameter("cropName", cropName);

        return (Collection) query.getResultList();
    }

    /**
     * @param parcelId
     * @param cropName
     * @param dateFrom
     * @return referencia a un objeto de tipo Collection que
     * contiene todos los balances hidricos de suelo de una
     * parcela que tienen una fecha mayor o igual a una
     * fecha desde
     */
    public Collection<SoilWaterBalance> findAllByDateGreaterThanOrEqual(int parcelId, String cropName, Calendar dateFrom) {
        Query query = getEntityManager().createQuery("SELECT s FROM Parcel p JOIN p.soilWaterBalances s WHERE (p.id = :parcelId AND s.cropName = :cropName AND s.date >= :dateFrom) ORDER BY s.date");
        query.setParameter("parcelId", parcelId);
        query.setParameter("cropName", cropName);
        query.setParameter("dateFrom", dateFrom);

        return (Collection) query.getResultList();
    }

    /**
     * @param parcelId
     * @param cropName
     * @param dateUntil
     * @return referencia a un objeto de tipo Collection que
     * contiene todos los balances hidricos de suelo de una
     * parcela que tienen una fecha menor o igual a una fecha
     * hasta
     */
    public Collection<SoilWaterBalance> findAllByDateLessThanOrEqual(int parcelId, String cropName, Calendar dateUntil) {
        Query query = getEntityManager().createQuery("SELECT s FROM Parcel p JOIN p.soilWaterBalances s WHERE (p.id = :parcelId AND s.cropName = :cropName AND s.date <= :dateUntil) ORDER BY s.date");
        query.setParameter("parcelId", parcelId);
        query.setParameter("cropName", cropName);
        query.setParameter("dateUntil", dateUntil);

        return (Collection) query.getResultList();
    }

    /**
     * @param parcelId
     * @param cropName
     * @param dateFrom
     * @param dateUntil
     * @return referencia a un objeto de tipo Collection que
     * contiene todos los balances hidricos de suelo de una
     * parcela que tienen una fecha mayor o igual a una
     * fecha desde y menor o igual a una fecha hasta, y un
     * nombre de cultivo
     */
    public Collection<SoilWaterBalance> findByAllFilterParameters(int parcelId, String cropName, Calendar dateFrom, Calendar dateUntil) {
        Query query = getEntityManager().createQuery("SELECT s FROM Parcel p JOIN p.soilWaterBalances s WHERE (p.id = :parcelId AND s.date >= :dateFrom AND s.date <= :dateUntil AND s.cropName = :cropName) ORDER BY s.date");
        query.setParameter("parcelId", parcelId);
        query.setParameter("cropName", cropName);
        query.setParameter("dateFrom", dateFrom);
        query.setParameter("dateUntil", dateUntil);

        return (Collection) query.getResultList();
    }

    /**
     * Actualiza el estado de instancias de tipo SoilWaterBalance
     * desde la base de datos, sobrescribiendo los cambios realizados
     * en cada una de ellas, si los hubiere
     * 
     * @param soilWaterBalances
     */
    public void refreshSoilWaterBalances(Collection<SoilWaterBalance> soilWaterBalances) {

        for (SoilWaterBalance currentSoilWaterBalance : soilWaterBalances) {
            getEntityManager().refresh(getEntityManager().merge(currentSoilWaterBalance));
        }

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
     * @param parcel
     * @param cropName
     * @param climateRecords
     * @param irrigationRecords
     */
    public void generateSoilWaterBalances(Parcel parcel, String cropName, Collection<ClimateRecord> climateRecords, Collection<IrrigationRecord> irrigationRecords) {
        SoilWaterBalance newSoilWaterBalance;
        SoilWaterBalance givenSoilWaterBalance;
        List<SoilWaterBalance> listSoilWaterBalances = (List) parcel.getSoilWaterBalances();

        double waterDeficitPerDay = 0.0;
        double accumulatedWaterDeficitPerDay = 0.0;
        double waterProvidedPerDay = 0.0;

        /*
         * Persiste o actualiza los balances hidricos de suelo de
         * una parcela que tiene un cultivo en desarrollo. Esta
         * persistencia o actualizacion se realiza en el periodo
         * determinado por la fecha de cada uno de los registros
         * climaticos de una coleccion.
         */
        for (ClimateRecord currentClimateRecord : climateRecords) {
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
            accumulatedWaterDeficitPerDay = WaterMath.accumulateWaterDeficitPerDay(waterDeficitPerDay, accumulatedWaterDeficitPerDay);

            /*
             * Calcula el agua provista (lluvia o riego, o lluvia mas riego
             * y viceversa) por dia [mm/dia] a un cultivo en fecha, debido
             * a que un registro climatico y un registro de riego tienen
             * una fecha, y a que el metodo generateSoilWaterBalances
             * debe ser invocado unicamente cuando se calcula la necesidad
             * de agua de riego de un cultivo en la fecha actual [mm/dia]
             */
            waterProvidedPerDay = currentClimateRecord.getPrecip() + WaterMath.sumTotalAmountIrrigationWaterGivenDate(currentClimateRecord.getDate(), irrigationRecords);

            /*
             * Si NO existe un balance hidrico de suelo correspondiente a
             * una parcela en una fecha, se lo crea y persiste. En caso
             * contrario, se lo obtiene y se actualiza su nombre de cultivo,
             * su agua provista [mm/dia], su agua evaporada [mm/dia], su
             * deficit de agua por dia [mm/dia] y su acumulado del deficit
             * de agua por dia de dias previos a una fecha [mm/dia].
             */
            if (!checkExistence(parcel.getId(), currentClimateRecord.getDate())) {
                newSoilWaterBalance = new SoilWaterBalance();
                newSoilWaterBalance.setDate(currentClimateRecord.getDate());
                newSoilWaterBalance.setParcelName(parcel.getName());
                newSoilWaterBalance.setCropName(cropName);
                newSoilWaterBalance.setEvaporatedWater(getEvaporatedWater(currentClimateRecord));
                newSoilWaterBalance.setWaterProvided(waterProvidedPerDay);
                newSoilWaterBalance.setWaterDeficitPerDay(waterDeficitPerDay);
                newSoilWaterBalance.setAccumulatedWaterDeficitPerDay(String.valueOf(accumulatedWaterDeficitPerDay));

                /*
                 * Persiste el nuevo balance hidrico de suelo
                 */
                newSoilWaterBalance = create(newSoilWaterBalance);

                /*
                 * Agrega el nuevo balance hidrico creado a la coleccion
                 * de balances hidricos de suelo perteneciente a una
                 * parcela
                 */
                listSoilWaterBalances.add(newSoilWaterBalance);
            } else {
                givenSoilWaterBalance = find(parcel.getId(), currentClimateRecord.getDate());
                update(givenSoilWaterBalance.getId(), cropName, getEvaporatedWater(currentClimateRecord), waterProvidedPerDay, waterDeficitPerDay, String.valueOf(accumulatedWaterDeficitPerDay));
            }

        } // End for

    }

    /**
     * @param climateRecord
     * @return double que representa el agua evaporada, la cual
     * puede ser la ETc o la ETo en caso de que la ETc = 0
     */
    public double getEvaporatedWater(ClimateRecord climateRecord) {
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

    public Page<SoilWaterBalance> findAllPagination(int userId, Integer page, Integer cantPerPage, Map<String, String> parameters) throws ParseException {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        Date date;
        Calendar calendarDate;

        // Genera el WHERE dinámicamente
        StringBuffer where = new StringBuffer(" WHERE 1=1 AND e IN (SELECT t FROM Parcel p JOIN p.soilWaterBalances t WHERE p IN (SELECT x FROM User u JOIN u.parcels x WHERE u.id = :userId))");

        if (parameters != null) {

            for (String param : parameters.keySet()) {
                Method method;

                try {
                    method = SoilWaterBalance.class.getMethod("get" + capitalize(param));

                    if (method == null || parameters.get(param) == null || parameters.get(param).isEmpty()) {
                        continue;
                    }

                    switch (method.getReturnType().getSimpleName()) {
                        case "String":

                            if (param.equals("parcelName")) {
                                where.append(" AND UPPER(e.");
                                where.append(param);
                                where.append(") LIKE UPPER('%");
                                where.append(parameters.get(param));
                                where.append("%')");
                            }

                            if (param.equals("cropName")) {
                                where.append(" AND UPPER(e.");
                                where.append(param);
                                where.append(") LIKE UPPER('%");
                                where.append(parameters.get(param));
                                where.append("%')");
                            }

                            break;
                        case "Calendar":

                            if (param.equals("date")) {
                                date = new Date(dateFormatter.parse(parameters.get(param)).getTime());
                                calendarDate = UtilDate.toCalendar(date);
                                where.append(" AND e.");
                                where.append(param);
                                where.append(" >= ");
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
        Query countQuery = entityManager.createQuery("SELECT COUNT(e.id) FROM " + SoilWaterBalance.class.getSimpleName() + " e" + where.toString());
        countQuery.setParameter("userId", userId);

        // Pagina
        Query query = entityManager.createQuery("FROM " + SoilWaterBalance.class.getSimpleName() + " e" + where.toString());
        query.setMaxResults(cantPerPage);
        query.setFirstResult((page - 1) * cantPerPage);
        query.setParameter("userId", userId);

        Integer count = ((Long) countQuery.getSingleResult()).intValue();
        Integer lastPage = (int) Math.ceil((double) count / (double) cantPerPage);

        // Arma la respuesta
        Page<SoilWaterBalance> resultPage = new Page<SoilWaterBalance>(page, count, page > 1 ? page - 1 : page, page < lastPage ? page + 1 : lastPage, lastPage, query.getResultList());
        return resultPage;
    }

    private String capitalize(final String line) {
        return Character.toUpperCase(line.charAt(0)) + line.substring(1);
    }

}
