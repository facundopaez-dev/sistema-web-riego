package plantingRecord;

import java.util.Calendar;
import java.util.Collection;
import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Stateless;
import stateless.PlantingRecordServiceBean;
import stateless.PlantingRecordStatusServiceBean;
import stateless.CropServiceBean;
import stateless.IrrigationRecordServiceBean;
import stateless.ClimateRecordServiceBean;
import stateless.SolarRadiationServiceBean;
import stateless.MonthServiceBean;
import stateless.LatitudeServiceBean;
import model.Parcel;
import model.PlantingRecord;
import model.ClimateRecord;
import irrigation.WaterMath;
import util.UtilDate;
import et.HargreavesEto;
import et.Etc;
import climate.ClimateClient;

@Stateless
public class PlantingRecordManager {

    // inject a reference to the PlantingRecordServiceBean
    @EJB
    PlantingRecordServiceBean plantingRecordService;

    // inject a reference to the plantingRecordStatusService
    @EJB
    PlantingRecordStatusServiceBean plantingRecordStatusService;

    // inject a reference to the CropServiceBean
    @EJB
    CropServiceBean cropService;

    // inject a reference to the IrrigationRecordServiceBean
    @EJB
    IrrigationRecordServiceBean irrigationRecordService;

    // inject a reference to the ClimateRecordServiceBean
    @EJB
    ClimateRecordServiceBean climateRecordService;

    // inject a reference to the SolarRadiationServiceBean
    @EJB
    SolarRadiationServiceBean solarService;

    // inject a reference to the MonthServiceBean
    @EJB
    MonthServiceBean monthService;

    // inject a reference to the LatitudeServiceBean
    @EJB
    LatitudeServiceBean latitudeService;

    /*
     * El valor de esta constante se asigna a la necesidad de
     * agua de riego [mm/dia] de un registro de plantacion en
     * desarrollo para el que no se puede calcular dicha
     * necesidad, lo cual, ocurre cuando no se tiene la
     * evapotranspiracion del cultivo bajo condiciones estandar
     * (ETc) [mm/dia] ni la precipitacion [mm/dia], siendo
     * ambos valores de la fecha actual.
     * 
     * La abreviatura "n/a" significa "no disponible".
     */
    private final String NOT_AVAILABLE = "n/a";

    /*
     * Establece de manera automatica el estado finalizado de un registro de
     * plantacion presuntamente en desarrollo. Esto lo hace cada 24 horas a
     * partir de las 00 horas.
     * 
     * La segunda anotacion @Schedule es para probar que este metodo se
     * ejecuta correctamente, es decir, que establece el estado finalizado
     * en un registro de plantacion presuntamente en desarrollo.
     * 
     * El archivo t110Inserts.sql de la ruta app/etc/sql tiene datos para
     * probar que este metodo se ejecuta correctamente, es decir, que hace
     * lo que se espera que haga.
     */
    @Schedule(second = "*", minute = "*", hour = "0/23", persistent = false)
    // @Schedule(second = "*/10", minute = "*", hour = "*", persistent = false)
    public void modifyToFinishedStatus() {
        Collection<PlantingRecord> plantingRecords = plantingRecordService.findAllInDevelopment();

        for (PlantingRecord currentPlantingRecord : plantingRecords) {
            /*
             * Si un registro de plantacion presuntamente en desarrollo,
             * NO esta en desarrollo, se establece el estado finalizado
             * en el mismo.
             * 
             * Un registro de plantacion en desarrollo, esta en desarrollo
             * si su fecha de siembra es menor o igual a la fecha actual y
             * su fecha de cosecha es mayor o igual a la fecha actual.
             * En cambio, si su fecha de cosecha es estrictamente menor
             * a la fecha actual, se debe establecer el estado finalizado
             * en el mismo.
             */
            if (!plantingRecordService.checkDevelopmentStatus(currentPlantingRecord)) {
                plantingRecordService.setStatus(currentPlantingRecord.getId(), plantingRecordStatusService.findFinishedStatus());
            }

        }

    }

    /*
     * Establece de manera automatica el estado en desarrollo en el registro
     * de plantacion presuntamente en espera mas antiguo de los registros de
     * plantacion en espera de cada una de las parcelas registradas en la base
     * de datos subyacente. Esto lo hace cada 24 horas a parit de las 01 horas,
     * una hora despues de la ejecucion del metodo modifyToFinishedStatus de
     * esta clase.
     * 
     * La segunda anotacion @Schedule es para probar que este metodo se
     * ejecuta correctamente, es decir, que establece el estado en desarrollo
     * en un registro de plantacion presuntamente en espera.
     */
    @Schedule(second = "*", minute = "*", hour = "1/23", persistent = false)
    // @Schedule(second = "*/10", minute = "*", hour = "*", persistent = false)
    public void modifyToInDevelopmentStatus() {
        /*
         * Obtiene una coleccion que contiene el registro de
         * plantacion en espera mas antiguo de los registros
         * de plantacion en espera de cada una de las parcelas
         * registradas en la base de datos subyacente
         */
        Collection<PlantingRecord> plantingRecords = plantingRecordService.findAllInWaiting();

        for (PlantingRecord currentPlantingRecord : plantingRecords) {
            /*
             * Si un registro de plantacion presuntamente en espera, NO
             * esta en espera, se establece el estado en desarrollo en
             * el mismo.
             * 
             * Un registro de plantacion en espera, esta en espera si su
             * fecha de siembra es estrictamente mayor (es decir, posterior)
             * a la fecha actual. En cambio, si su fecha de siembra es menor
             * o igual a la fecha actual, se debe establecer el estado en
             * desarrollo en el mismo.
             */
            if (!plantingRecordService.checkWaitingStatus(currentPlantingRecord)) {
                plantingRecordService.setStatus(currentPlantingRecord.getId(), plantingRecordStatusService.findDevelopmentStatus());
            }

        }

    }

    /**
     * Establece de manera automatica la necesidad de agua de riego [mm/dia]
     * (atributo irrigationWaterNeed) de un registro de plantacion en desarrollo
     * cada dos horas a partir de la hora 01.
     * 
     * La segunda anotacion @Schedule es para probar que este metodo se ejecuta
     * correctamente, es decir, que asigna un valor al atributo irrigationWaterNeed
     * de un registro de plantacion en desarrollo.
     * 
     * El archivo t125Inserts.sql de la ruta app/etc/sql tiene datos para probar que
     * este metodo se ejecuta correctamente, es decir, que hace lo que se espera que
     * haga.
     */
    @Schedule(second = "*", minute = "*", hour = "1/2", persistent = false)
    // @Schedule(second = "*/5", minute = "*", hour = "*", persistent = false)
    private void setIrrigationWaterNeed() {
        Collection<PlantingRecord> plantingRecords = plantingRecordService.findAllInDevelopment();

        /*
         * El metod getInstance de la clase Calendar retorna
         * la referencia a un objeto de tipo Calendar que
         * contiene la fecha actual
         */
        Calendar currentDate = Calendar.getInstance();
        Calendar yesterdayDate = UtilDate.getYesterdayDate();
        ClimateRecord currentClimateRecord = null;
        Parcel givenParcel = null;

        double currentIrrigationWaterNeed = 0.0;
        double excessWaterYesterday = 0.0;
        double totalIrrigationWaterCurrentDate = 0.0;

        /*
         * Establece la necesidad de agua de riego [mm/dia] de la fecha
         * actual en cada uno de los registros de plantacion en desarrollo
         * de todas las parcelas.
         * 
         * Esto es que establece la necesidad de agua de riego [mm/dia] de
         * la fecha actual de cada cultivo en desarrollo de cada una de
         * las parcelas.
         */
        for (PlantingRecord currentPlantingRecord : plantingRecords) {
            givenParcel = currentPlantingRecord.getParcel();

            /*
             * Si en la base de datos subyacente existe el registro climatico
             * de la fecha actual para una parcela dada, se utilizan la
             * evapotranspiracion del cultivo bajo condiciones estandar (ETc)
             * [mm/dia] y la precipitacion [mm/dia] de dicho registro climatico,
             * entre otros datos, para calcular la necesidad de agua de riego
             * [mm/dia] del cultivo que esta en desarrollo en la fecha actual
             */
            if (climateRecordService.checkExistence(currentDate, givenParcel)) {
                /*
                 * Solicita y persiste una cantidad NUMBER_DAYS de
                 * registros climaticos de una parcela anteriores a
                 * la fecha actual, si no existen en la base de datos
                 * subyacente
                 */
                requestAndPersistClimateRecordsForPeriod(givenParcel);

                /*
                 * Calcula el agua excedente de NUMBER_DAYS registros
                 * climaticos de una parcela anteriores a la fecha actual
                 */
                calculateExcessWaterForPeriod(givenParcel);

                /*
                 * Obtiene de la base de datos subyacente el registro climatico
                 * de la fecha actual de una parcela dada
                 */
                currentClimateRecord = climateRecordService.find(currentDate, givenParcel);

                /*
                 * Si la parcela dada tiene el registro climatico del dia inmediatamente
                 * anterior a la fecha actual, se obtiene el agua excedente del mismo
                 * para calcular la necesidad de agua de riego [mm/dia] del cultivo que
                 * esta en desarrollo en la fecha actual. En caso contrario, se asume
                 * que el agua excedente de dicho dia es 0.
                 */
                if (climateRecordService.checkExistence(yesterdayDate, givenParcel)) {
                    excessWaterYesterday = climateRecordService.find(yesterdayDate, givenParcel).getExcessWater();
                } else {
                    excessWaterYesterday = 0.0;
                }

                totalIrrigationWaterCurrentDate = irrigationRecordService.calculateTotalIrrigationWaterCurrentDate(givenParcel);

                /*
                 * Calculo de la necesidad de agua de riego [mm/dia] del cultivo
                 * que esta en desarrollo en la fecha actual
                 */
                currentIrrigationWaterNeed = WaterMath.calculateIrrigationWaterNeed(currentClimateRecord.getEtc(),
                        currentClimateRecord.getPrecip(), totalIrrigationWaterCurrentDate, excessWaterYesterday);

                plantingRecordService.updateIrrigationWaterNeed(currentPlantingRecord.getId(), givenParcel, String.valueOf(currentIrrigationWaterNeed));
            }

            /*
             * Si en la base de datos subyacente NO existe el registro climatico de
             * la fecha actual para una parcela dada, NO se tienen la ETc ni la
             * precipitacion de la fecha actual para calcular la necesidad de agua
             * de riego [mm/dia] del cultivo que esta en desarrollo en la fecha
             * actual.
             * 
             * Por lo tanto, se asigna el valor "n/a" (no disponible) a la necesidad
             * de agua de riego [mm/dia] de la fecha actual del registro de plantacion
             * en desarrollo actual (el que esta siendo actualmente utilizado por la
             * instruccion for each).
             */
            if (!climateRecordService.checkExistence(currentDate, givenParcel)) {
                plantingRecordService.updateIrrigationWaterNeed(currentPlantingRecord.getId(), givenParcel, NOT_AVAILABLE);
            }

        } // End for

    }

    /**
     * Crea y persiste los registros climaticos de una
     * parcela anteriores a la fecha actual, si no estan
     * en la base de datos subyacente. La cantidad de
     * registros climaticos anteriores a la fecha actual
     * que se crearan y persistiran esta determinada por
     * el valor de la constante NUMBER_DAYS.
     * 
     * @param givenParcel
     */
    private void requestAndPersistClimateRecordsForPeriod(Parcel givenParcel) {
        /*
         * El valor de esta variable se utiliza:
         * - para obtener y persistir los registros climaticos de una parcela
         * anteriores a la fecha actual.
         * - para calcular el agua excedente de cada uno de los registros
         * climaticos de una parcela anteriores a la fecha actual.
         * - para recalcular la ETc de cada uno de los registros climaticos
         * de una parcela anteriores a la fecha actual.
         */
        int numberDays = climateRecordService.getNumberDays();

        /*
         * El metodo getInstance de la clase Calendar retorna
         * la referencia a un objeto de tipo Calendar que
         * contiene la fecha actual
         */
        Calendar currentDate = Calendar.getInstance();
        Calendar givenDate = Calendar.getInstance();

        ClimateRecord newClimateRecord = null;
        PlantingRecord givenPlantingRecord = null;

        double eto = 0.0;
        double etc = 0.0;
        double kc = 0.0;
        double extraterrestrialSolarRadiation = 0.0;

        /*
         * Crea y persiste una cantidad NUMBER_DAYS de registros
         * climaticos de una parcela con fechas anteriores a la
         * fecha actual
         */
        for (int i = 1; i < numberDays + 1; i++) {

            /*
             * De esta manera se obtiene cada una de las fechas
             * anteriores a la fecha actual
             */
            givenDate.set(Calendar.DAY_OF_YEAR, (currentDate.get(Calendar.DAY_OF_YEAR) - i));

            /*
             * Si en la base de datos subyacente NO existe el registro
             * climatico con la fecha dada para una parcela dada, se
             * lo solicita la API climatica y se lo persiste
             */
            if (!climateRecordService.checkExistence(givenDate, givenParcel)) {
                newClimateRecord = ClimateClient.getForecast(givenParcel, givenDate.getTimeInMillis() / 1000);

                extraterrestrialSolarRadiation = solarService.getRadiation(givenParcel.getLatitude(),
                        monthService.getMonth(currentDate.get(Calendar.MONTH)),
                        latitudeService.find(givenParcel.getLatitude()),
                        latitudeService.findPreviousLatitude(givenParcel.getLatitude()),
                        latitudeService.findNextLatitude(givenParcel.getLatitude()));

                /*
                 * Calculo de la evapotranspiracion del cultivo
                 * de referencia (ETo) en la fecha dada
                 */
                eto = HargreavesEto.calculateEto(newClimateRecord.getMaximumTemperature(),
                        newClimateRecord.getMinimumTemperature(), extraterrestrialSolarRadiation);

                /*
                 * Si la parcela dada tiene un registro de plantacion en
                 * el que la fecha dada esta entre la fecha de siembra y
                 * la fecha de cosecha del mismo, se obtiene el kc (coeficiente
                 * de cultivo) del cultivo de este registro para poder
                 * calcular la ETc (evapotranspiracion del cultivo bajo
                 * condiciones estandar) de dicho cultivo.
                 * 
                 * En otras palabras, lo que hace esta instruccion if es
                 * preguntar "¿la parcela dada tuvo o tiene un cultivo
                 * sembrado en la fecha dada?". En caso afirmativo se
                 * obtiene el kc del cultivo para calcular su ETc, la
                 * cual se asignara al nuevo registro climatico.
                 */
                if (plantingRecordService.checkExistence(givenParcel, givenDate)) {
                    givenPlantingRecord = plantingRecordService.find(givenParcel, givenDate);

                    /*
                     * Para obtener el kc (coeficiente de cultivo) que tuvo
                     * el cultivo en la fecha dada, se debe utilizar la fecha
                     * dada como fecha hasta en la invocacion del metodo
                     * getKc de la clase CropServiceBean
                     */
                    kc = cropService.getKc(givenPlantingRecord.getCrop(), givenPlantingRecord.getSeedDate(), givenDate);
                    etc = Etc.calculateEtc(eto, kc);
                } else {
                    /*
                     * Si la parcela dada NO tiene un registro de plantacion
                     * en el que la fecha dada esta entre la fecha de siembra
                     * y la fecha de cosecha del mismo, la ETc (evapotranspiracion
                     * del cultivo bajo condiciones estandar) es 0.0, ya que
                     * la inexistencia de un registro de plantacion representa
                     * la inexistencia de un cultivo sembrado en una parcela
                     */
                    etc = 0.0;
                }

                /*
                 * Asignacion de los valores calculados de la ETo
                 * (evapotranspiracion del cultivo de referencia) y
                 * la ETc (evapotranspiracion del cultivo bajo condiciones
                 * estandar) al nuevo registro climatico
                 */
                newClimateRecord.setEto(eto);
                newClimateRecord.setEtc(etc);

                /*
                 * Persistencia del nuevo registro climatico
                 */
                climateRecordService.create(newClimateRecord);
            } // End if

        } // End for

    }

    /**
     * Calcula el agua excedente de los registros climaticos de
     * una parcela anteriores a la fecha actual. Las fechas de
     * estos registros climaticos estan comprendidas en el
     * conjunto de fechas que van desde el dia inmediatamente
     * anterior a la fecha actual hasta una cantidad de dias
     * hacia atras. Esta cantidad de dias esta determinada por
     * el valor de la constante NUMBER_DAYS.
     * 
     * @param givenParcel
     */
    private void calculateExcessWaterForPeriod(Parcel givenParcel) {
        /*
         * El valor de esta variable se utiliza:
         * - para obtener y persistir los registros climaticos de una parcela
         * anteriores a la fecha actual.
         * - para calcular el agua excedente de cada uno de los registros
         * climaticos de una parcela anteriores a la fecha actual.
         * - para recalcular la ETc de cada uno de los registros climaticos
         * de una parcela anteriores a la fecha actual.
         */
        int numberDays = climateRecordService.getNumberDays();

        /*
         * El metodo getInstance de la clase Calendar retorna
         * la referencia a un objeto de tipo Calendar que
         * contiene la fecha actual
         */
        Calendar currentDate = Calendar.getInstance();
        Calendar givenDate = Calendar.getInstance();

        /*
         * Esta variable se utiliza para obtener el dia inmeditamente
         * anterior a una fecha dada en la instruccion for de mas
         * abajo. Esto es necesario para obtener el agua excedente
         * de una parcela en el dia inmediatamente anterior a una
         * fecha dada.
         */
        Calendar yesterdayDate = Calendar.getInstance();

        ClimateRecord givenClimateRecord = null;

        double excessWaterGivenDate = 0.0;
        double excessWaterYesterday = 0.0;
        double totalIrrigationWaterGivenDate = 0.0;
        double givenEt = 0.0;

        /*
         * El agua excedente de los registros climaticos de una
         * parcela anteriores a la fecha actual, se debe calcular
         * desde atras hacia adelante, ya que el agua excedente de
         * un dia es agua a favor para el dia inmediatamente siguiente.
         * 
         * Por lo tanto, se debe comenzar a calcular el agua excedente
         * de los registros climticos de una parcela anteriores a la
         * fecha actual desde el registro climatico mas antiguo de
         * ellos hasta el mas actual de ellos. Estos registros
         * climaticos son obtenidos y persistidos por el metodo
         * requestAndPersistClimateRecordsForPeriod si no existen
         * en la base de datos subyacente.
         */
        givenDate.set(Calendar.DAY_OF_YEAR, (currentDate.get(Calendar.DAY_OF_YEAR) - numberDays));

        /*
         * Calcula el agua excedente de cada uno de los registros
         * climaticos de una parcela anteriores a la fecha actual
         * desde el mas antiguo de ellos hasta el mas actual de
         * ellos. Estos registros climaticos son obtenidos y persistidos
         * por el metodo requestAndPersistClimateRecordsForPeriod
         * si no existen en la base de datos subyacente.
         */
        for (int i = 1; i < numberDays + 1; i++) {
            yesterdayDate.set(Calendar.DAY_OF_YEAR, (givenDate.get(Calendar.DAY_OF_YEAR) - 1));

            /*
             * Si el registro climatico del dia inmediatamente anterior
             * a una fecha dada existe, se obtiene su agua excedente
             * para calcular el agua excedente del registro climatico
             * de una fecha dada. En caso contrario, se asume que el
             * agua excedente del dia inmediatamente anterior a una
             * fecha dada es 0.0.
             */
            if (climateRecordService.checkExistence(yesterdayDate, givenParcel)) {
                excessWaterYesterday = climateRecordService.find(yesterdayDate, givenParcel).getExcessWater();
            } else {
                excessWaterYesterday = 0.0;
            }

            /*
             * Obtiene uno de los registros climaticos de una parcela dada
             * anteriores a la fecha actual, los cuales son obtenidos y
             * persistidos por el metodo requestAndPersistClimateRecordsForPeriod
             * si NO existen en la base de datos subyacente
             */
            givenClimateRecord = climateRecordService.find(givenDate, givenParcel);
            totalIrrigationWaterGivenDate = irrigationRecordService.calculateTotalIrrigationWaterGivenDate(givenDate,
                    givenParcel);

            /*
             * Cuando una parcela NO tiene un cultivo sembrado y en
             * desarrollo, la ETc de uno o varios de sus registros
             * climaticos tiene el valor 0.0, ya que si no hay un
             * cultivo en desarrollo NO es posible calcular la ETc
             * (evapotranspiracion del cultivo bajo condiciones
             * estandar) del mismo. Por lo tanto, se debe utilizar la
             * ETo (evapotranspiracion del cultivo de referencia) para
             * calcular el agua excedente de un registro climatico
             * en una fecha dada.
             * 
             * En caso contrario, se debe utilizar la ETc para calcular
             * el agua excedente de un registro climatico en una fecha
             * dada.
             */
            if (givenClimateRecord.getEtc() == 0.0) {
                givenEt = givenClimateRecord.getEto();
            } else {
                givenEt = givenClimateRecord.getEtc();
            }

            /*
             * Calculo del agua excedente de una parcela dada
             * en una fecha dada
             */
            excessWaterGivenDate = WaterMath.calculateExcessWater(givenEt, givenClimateRecord.getPrecip(),
                    totalIrrigationWaterGivenDate, excessWaterYesterday);

            /*
             * Actualizacion del agua excedente del registro
             * climatico de una fecha dada
             */
            climateRecordService.updateExcessWater(givenDate, givenParcel, excessWaterGivenDate);

            /*
             * El agua excedente de los registros climaticos de una
             * parcela anteriores a la fecha actual se calcula desde
             * atras hacia adelante, ya que el agua excedente de un
             * dia es agua a favor para el dia inmediatamente siguiente.
             * Por lo tanto, se comienza a calcular el agua excedente
             * de estos registros climaticos desde el registro climatico
             * mas antiguo de ellos. En consecuencia, para calcular el
             * agua excedente del siguiente registro climatico se debe
             * calcular la fecha siguiente.
             */
            givenDate.set(Calendar.DAY_OF_YEAR, ((currentDate.get(Calendar.DAY_OF_YEAR) - numberDays) + i));
        } // End for

    }

}