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
     * deficit de humedad por dia de un balance hidrico de suelo
     * de una parcela que tiene un cultivo sembrado y en
     * desarrollo. Esta situacion ocurre cuando la perdida de
     * humedad del suelo de un conjunto de dias es estrictamente
     * mayor al doble de la capacidad de almacenamiento de agua
     * del suelo. Esto se representa mediante la condicion de
     * que el acumulado del deficit de humedad por dia sea estrictamente
     * menor al negativo del doble de la capacidad de almacenamiento
     * de agua del suelo, ya que el acumulado del deficit de
     * agua por dia puede ser negativo o cero. Cuando es negativo
     * representa que en un periodo de dias hubo perdida de humedad
     * en el suelo. En cambio, cuando es igual a cero representa
     * que la perdida de humedad que hubo en el suelo en un periodo
     * de dias esta totalmente cubierta. Esto es que el suelo
     * esta en capacidad de campo, lo significa que el suelo
     * esta lleno de agua o en su maxima capacidad de almacenamiento
     * de agua, pero no anegado.
     * 
     * Cuando la perdida de humedad del suelo, que tiene un
     * cultivo sembrado, de un conjunto de dias es estrictamente
     * mayor al doble de la capacidad de almacenamiento de agua
     * del suelo (representado mediante la conidicion de que el
     * acumulado del deficit de humedad por dia sea estrictamente
     * menor al negativo del doble de la capacidad de almacenamiento
     * de agua del suelo), el cultivo esta muerto, ya que ningun
     * cultivo puede sobrevivir con dicha perdida de humedad.
     * Por lo tanto, la presencia del valor "NC" (no calculado)
     * tambien representa la muerte de un cultivo.
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
            chosenSoilWaterBalanace.setWaterProvidedPerDay(modifiedSoilWaterBalance.getWaterProvidedPerDay());
            chosenSoilWaterBalanace.setSoilMoistureLossPerDay(modifiedSoilWaterBalance.getSoilMoistureLossPerDay());
            chosenSoilWaterBalanace.setSoilMoistureDeficitPerDay(modifiedSoilWaterBalance.getSoilMoistureDeficitPerDay());
            chosenSoilWaterBalanace.setAccumulatedSoilMoistureDeficitPerDay(modifiedSoilWaterBalance.getAccumulatedSoilMoistureDeficitPerDay());
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
     * @param dateFrom
     * @param dateUntil
     * @return referencia a un objeto de tipo Collection que
     * contiene los balances hidricos de suelo de una parcela
     * pertenecientes al periodo definido por una fecha desde
     * y una fecha hasta
     */
    public Collection<SoilWaterBalance> findAllFromDateFromToDateUntil(int parcelId, Calendar dateFrom, Calendar dateUntil) {
        Query query = getEntityManager().createQuery("SELECT s FROM Parcel p JOIN p.soilWaterBalances s WHERE (p.id = :parcelId AND s.date >= :dateFrom AND s.date <= :dateUntil) ORDER BY s.date");
        query.setParameter("parcelId", parcelId);
        query.setParameter("dateFrom", dateFrom);
        query.setParameter("dateUntil", dateUntil);

        return (Collection) query.getResultList();
    }

    /**
     * @param parcelId
     * @param dateFrom
     * @return referencia a un objeto de tipo Collection que
     * contiene los balances hidricos de suelo de una parcela
     * pertenecientes al periodo definido por una fecha desde
     * y la fecha inmediatamente anterior a la fecha actual
     * (es decir, hoy)
     */
    public Collection<SoilWaterBalance> findAllFromSeedDateUntilYesterday(int parcelId, Calendar dateFrom) {
        Query query = getEntityManager().createQuery("SELECT s FROM Parcel p JOIN p.soilWaterBalances s WHERE (p.id = :parcelId AND s.date >= :dateFrom AND s.date <= :yesterday) ORDER BY s.date");
        query.setParameter("parcelId", parcelId);
        query.setParameter("dateFrom", dateFrom);
        query.setParameter("yesterday", UtilDate.getYesterdayDate());

        return (Collection) query.getResultList();
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
     * Actualiza el balance hidrico de suelo correspondiente a un
     * ID dado con un nombre de cultivo, una cantidad de perdida
     * de humedad de suelo [mm/dia], una cantidad de agua provista
     * [mm/dia], un deficit de humedad de suelo [mm/dia] y un deficit
     * acumulado de humedad de suelo [mm/dia]
     * 
     * @param id
     * @param cropName
     * @param soilMoistureLossPerDay
     * @param waterProvidedPerDay
     * @param soilMoistureDeficitPerDay
     * @param accumulatedSoilMoistureDeficitPerDay
     */
    public void update(int id, String cropName, double soilMoistureLossPerDay, double waterProvidedPerDay, double soilMoistureDeficitPerDay, String accumulatedSoilMoistureDeficitPerDay) {
        Query query = getEntityManager().createQuery("UPDATE SoilWaterBalance s SET s.cropName = :cropName, s.soilMoistureLossPerDay = :soilMoistureLossPerDay, s.waterProvidedPerDay = :waterProvidedPerDay, s.soilMoistureDeficitPerDay = :soilMoistureDeficitPerDay, s.accumulatedSoilMoistureDeficitPerDay = :accumulatedSoilMoistureDeficitPerDay WHERE s.id = :id");
        query.setParameter("cropName", cropName);
        query.setParameter("soilMoistureLossPerDay", soilMoistureLossPerDay);
        query.setParameter("waterProvidedPerDay", waterProvidedPerDay);
        query.setParameter("soilMoistureDeficitPerDay", soilMoistureDeficitPerDay);
        query.setParameter("accumulatedSoilMoistureDeficitPerDay", accumulatedSoilMoistureDeficitPerDay);
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
     * Un cultivo no muere cuando el nivel de humedad del
     * suelo, en el que esta sembrado, esta en el punto
     * de marchitez permanente del suelo o por debajo de
     * este punto, ya que esto depende de cada cultivo.
     * 
     * Un cultivo muere cuando la perdida de humedad del
     * suelo, en el que esta sembrado, es estrictamente
     * mayor al doble de la capacidad de almacenamiento
     * de agua del suelo. Esto se debe a que ningun cultivo
     * tiene la capacidad de sobrevivir con dicha perdida
     * de humedad de suelo. Esta es la convencion utilizada
     * por la aplicacion para determinar la muerte de un
     * cultivo.
     * 
     * La capacidad de almacenamiento de agua del suelo
     * esta en funcion de la capacidad de campo del suelo,
     * del punto de marchitez permanente del suelo, de la
     * densidad aparente del suelo y de la profundidad
     * radicular de un cultivo. Esto se debe a que la
     * capacidad de almacenamiento de agua del suelo esta
     * determinada por la lamina total de agua disponible
     * (dt) [mm].
     * 
     * El nivel de humedad del suelo esta determinado
     * por la cantidad agua que ingresa al suelo y la
     * cantidad de agua que pierde el suelo.
     * 
     * @param parcelId
     * @param cropDeathDate
     * @return double que representa la perdida de humedad
     * de suelo que causa la muerte de un cultivo
     */
    public double calculateCropDeathSoilMoistureLoss(int parcelId, Calendar cropDeathDate) {
        /*
         * Obtiene la perdida de humedad del suelo del dia
         * inmediatamente anterior a la fecha de muerte de
         * un cultivo
         */
        double soilMoistureLossFromDateBeforeDateCropDeath = Double
                .parseDouble(find(parcelId, UtilDate.getYesterdayDateFromDate(cropDeathDate)).getAccumulatedSoilMoistureDeficitPerDay());
        double soilMoistureLossDeathDate = find(parcelId, cropDeathDate).getSoilMoistureDeficitPerDay();

        return Math.abs(soilMoistureLossFromDateBeforeDateCropDeath + soilMoistureLossDeathDate);
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

        double soilMoistureDeficitPerDay = 0.0;
        double accumulatedSoilMoistureDeficitPerDay = 0.0;
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
             * Calcula el deficit de humedad por dia [mm/dia] de un
             * cultivo en una fecha, debido a que un registro climatico
             * y un registro de riego tienen una fecha, y a que el metodo
             * generateSoilWaterBalances debe ser invocado unicamente
             * cuando se calcula la necesidad de agua de riego de un
             * cultivo en la fecha actual [mm/dia]
             */
            soilMoistureDeficitPerDay = WaterMath.calculateWaterDeficitPerDay(currentClimateRecord, irrigationRecords);

            /*
             * Calcula el deficit acumulado de agua por dia [mm/dia] de
             * un cultivo en una fecha, debido a que un registro climatico
             * y un registro de riego tienen una fecha, y a que el metodo
             * generateSoilWaterBalances debe ser invocado unicamente
             * cuando se calcula la necesidad de agua de riego de un
             * cultivo en la fecha actual [mm/dia]
             */
            accumulatedSoilMoistureDeficitPerDay = WaterMath.accumulateSoilMoistureDeficitPerDay(soilMoistureDeficitPerDay, accumulatedSoilMoistureDeficitPerDay);

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
             * deficit de humedad por dia [mm/dia] y su acumulado del deficit
             * de agua por dia de dias previos a una fecha [mm/dia].
             */
            if (!checkExistence(parcel.getId(), currentClimateRecord.getDate())) {
                newSoilWaterBalance = new SoilWaterBalance();
                newSoilWaterBalance.setDate(currentClimateRecord.getDate());
                newSoilWaterBalance.setParcelName(parcel.getName());
                newSoilWaterBalance.setCropName(cropName);
                newSoilWaterBalance.setSoilMoistureLossPerDay(currentClimateRecord.getEtc());
                newSoilWaterBalance.setWaterProvidedPerDay(waterProvidedPerDay);
                newSoilWaterBalance.setSoilMoistureDeficitPerDay(soilMoistureDeficitPerDay);
                newSoilWaterBalance.setAccumulatedSoilMoistureDeficitPerDay(String.valueOf(accumulatedSoilMoistureDeficitPerDay));

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
                update(givenSoilWaterBalance.getId(), cropName, currentClimateRecord.getEtc(), waterProvidedPerDay, soilMoistureDeficitPerDay, String.valueOf(accumulatedSoilMoistureDeficitPerDay));
            }

        } // End for

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
        Query query = entityManager.createQuery("FROM " + SoilWaterBalance.class.getSimpleName() + " e" + where.toString() + " ORDER BY e.date");
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
