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
        Collection<PlantingRecord> developingPlantingRecords = plantingRecordService.findAllInDevelopment();

        Calendar currentDate = UtilDate.getCurrentDate();
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
        for (PlantingRecord developingPlantingRecord : developingPlantingRecords) {
            givenParcel = developingPlantingRecord.getParcel();

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
                 * Calcula y actualiza la ETo (evapotranspiracion del cultivo
                 * de referencia) y la ETc (evapotranspiracion del cultivo
                 * bajo condiciones estandar) de NUMBER_DAYS registros
                 * climaticos de una parcela anteriores a la fecha
                 * actual.
                 * 
                 * El agua excedente de una parcela en una fecha dada se
                 * calcula mediante la ETo o la ETc, entre otros datos.
                 * La ETc se utiliza si una parcela tuvo un cultivo plantado
                 * en una fecha dada. En caso contrario, se utiliza la ETo.
                 * 
                 * Este metodo es necesario para los casos en los que se
                 * modifican los coeficientes (KCs) de un cultivo, las
                 * temperaturas maximas y minimas de un registro climatico.
                 * Por lo tanto, si estos valores son modificados es necesario
                 * recalcular la ETo y la ETc de los registros climaticos
                 * sobre los que se va a actualizar el agua excedente, ya
                 * que esta se calcula en base a la ETo o la ETc (si una
                 * parcela tuvo un cultivo plantado en una fecha dada).
                 * Este es el motivo por el cual este metodo se debe
                 * invocar antes del metodo calculateExcessWaterForPeriod.
                 */
                calculateEtForPeriod(givenParcel);

                /*
                 * Calcula el agua excedente de NUMBER_DAYS registros
                 * climaticos de una parcela anteriores a la fecha actual
                 */
                calculateExcessWaterForPeriod(givenParcel);

                /*
                 * Calculo de la necesidad de agua de riego [mm/dia] de un
                 * cultivo que esta en desarrollo en la fecha actual
                 */
                currentClimateRecord = climateRecordService.find(currentDate, givenParcel);
                excessWaterYesterday = getExcessWaterYesterdayFromDate(givenParcel, currentDate);
                totalIrrigationWaterCurrentDate = irrigationRecordService.calculateTotalIrrigationWaterCurrentDate(givenParcel);
                currentIrrigationWaterNeed = WaterMath.calculateIrrigationWaterNeed(currentClimateRecord.getEtc(),
                        currentClimateRecord.getPrecip(), totalIrrigationWaterCurrentDate, excessWaterYesterday);

                /*
                 * Actualizacion de la necesidad de agua de riego de un
                 * registro de plantacion en desarrollo
                 */
                plantingRecordService.updateIrrigationWaterNeed(developingPlantingRecord.getId(), givenParcel, String.valueOf(currentIrrigationWaterNeed));
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
                plantingRecordService.updateIrrigationWaterNeed(developingPlantingRecord.getId(), givenParcel, NOT_AVAILABLE);
            }

        } // End for

    }

    /**
     * Crea y persiste los registros climaticos de una parcela
     * anteriores a la fecha actual, si NO existen en la base
     * de datos subyacente. La cantidad de registros climaticos
     * anteriores a la fecha actual que se crearan y persistiran
     * esta determinada por el valor de la constante NUMBER_DAYS,
     * la cual se encuentra en la clase ClimateRecordServiceBean.
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

        Calendar currentDate = UtilDate.getCurrentDate();
        Calendar pastDate = Calendar.getInstance();

        ClimateRecord newClimateRecord = null;
        PlantingRecord givenPlantingRecord = null;

        double eto = 0.0;
        double etc = 0.0;

        /*
         * Crea y persiste una cantidad NUMBER_DAYS de registros
         * climaticos de una parcela anteriores a la fecha actual
         */
        for (int i = 1; i < numberDays + 1; i++) {

            /*
             * De esta manera se obtiene cada una de las fechas
             * anteriores a la fecha actual hasta la fecha
             * resultante de la resta entre el numero de dia de
             * la fecha actual y numberDays
             */
            pastDate.set(Calendar.DAY_OF_YEAR, (currentDate.get(Calendar.DAY_OF_YEAR) - i));

            /*
             * Si en la base de datos subyacente NO existe el registro climatico
             * con la fecha dada perteneciente a una parcela dada, se lo solicita
             * la API climatica y se lo persiste
             */
            if (!climateRecordService.checkExistence(pastDate, givenParcel)) {
                newClimateRecord = ClimateClient.getForecast(givenParcel, pastDate.getTimeInMillis() / 1000);
                eto = calculateEtoForClimateRecord(newClimateRecord);

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
                if (plantingRecordService.checkExistence(givenParcel, pastDate)) {
                    givenPlantingRecord = plantingRecordService.find(givenParcel, pastDate);
                    etc = calculateEtcForClimateRecord(eto, givenPlantingRecord, pastDate);
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

                /*
                 * Luego de calcular la ETc de un nuevo registro climatico,
                 * se debe restablecer el valor por defecto de esta variable
                 * para evitar el error logico de asignar la ETc de un registro
                 * climatico a otro registro climatico
                 */
                etc = 0.0;
            } // End if

        } // End for

    }

    /**
     * Calcula y actualiza la ETo (evapotranspiracion del cultivo
     * de referencia) y la ETc (evapotranspiracion del cultivo bajo
     * condiciones estandar) de NUMBER_DAYS registros climaticos de
     * una parcela anteriores a la fecha actual.
     * 
     * La constante NUMBER_DAYS es de la clase ClimateRecordServiceBean.
     * 
     * @param givenParcel
     */
    private void calculateEtForPeriod(Parcel givenParcel) {
        /*
         * El valor de esta variable se utiliza:
         * - para obtener y persistir los registros climaticos de una parcela
         * anteriores a la fecha actual.
         * - para calcular el agua excedente de cada uno de los registros
         * climaticos de una parcela anteriores a la fecha actual.
         * - para recalcular la ETo y la ETc de cada uno de los registros
         * climaticos de una parcela anteriores a la fecha actual.
         */
        int numberDays = climateRecordService.getNumberDays();

        Calendar currentDate = UtilDate.getCurrentDate();
        Calendar pastDate = Calendar.getInstance();
        ClimateRecord givenClimateRecord = null;
        PlantingRecord givenPlantingRecord = null;

        /*
         * No hay un motivo para calcular la ETo y la ETc de los
         * registros climaticos de una parcela desde la fecha
         * actual - numberDays hasta el dia inmediatamente anterior
         * a la fecha actual, es decir, desde una cantidad numberDays
         * de dias atras hasta el dia inmediatamente anterior a la
         * fecha actual. Esto tambien se puede hacer desde el dia
         * inmediatamente anterior a la fecha actual hacia una
         * cantidad numberDays de dias hacia atras.
         */
        pastDate.set(Calendar.DAY_OF_YEAR, (currentDate.get(Calendar.DAY_OF_YEAR) - numberDays));

        double eto = 0.0;
        double etc = 0.0;

        /*
         * Calcula y actualiza la ETo (evapotranspiracion del cultivo
         * de referencia) y la ETc (evapotranspiracion del cultivo
         * bajo condiciones estandar) de numberDays registros climaticos
         * de una parcela anteriores a la fecha actual.
         */
        for (int i = 1; i < numberDays + 1; i++) {
            /*
             * Si existe el registro climatico de una fecha dada de una
             * parcela, se calcula y actualiza su ETo y su ETc.
             * 
             * La ETc se calcula y actualiza si una parcela tiene o tuvo
             * un cultivo plantado en una fecha dada. Esto se traduce a
             * si en una fecha dada una parcela tuvo o tiene un registro
             * de plantacion.
             */
            if (climateRecordService.checkExistence(pastDate, givenParcel)) {
                givenClimateRecord = climateRecordService.find(pastDate, givenParcel);
                eto = calculateEtoForClimateRecord(givenClimateRecord);

                /*
                 * Si una parcela tiene un registro de plantacion en una fecha
                 * dada, se calcula la ETc (evapotranspiracion del cultivo bajo
                 * condiciones estandar) del cultivo de dicho registro
                 */
                if (plantingRecordService.checkExistence(givenParcel, pastDate)) {
                    givenPlantingRecord = plantingRecordService.find(givenParcel, pastDate);
                    etc = calculateEtcForClimateRecord(eto, givenPlantingRecord, pastDate);
                }

                /*
                 * Actualizacion de la ETo (evapotranspiracion del cultivo de
                 * referencia) y la ETc (evapotranspiracion del cultivo bajo
                 * condiciones estandar) del registro climatico correspondiente
                 * a una fecha y una parcela dadas
                 */
                climateRecordService.updateEtoAndEtc(pastDate, givenParcel, eto, etc);

                /*
                 * Luego de calcular y actualizar la ETc de un registro climatico
                 * correspondiente a una fecha y una parcela dadas, se restablece
                 * el valor por defecto de esta variable para evitar el error
                 * logico de asignar la ETc de un registro climatico a otro registro
                 * climatico
                 */
                etc = 0.0;
            } // End if

            /*
             * Se decidio calcular la ETo y la ETc de los registros climaticos
             * de una parcela desde una cantidad numberDays de dias atras hasta
             * el dia inmediatamente anterior a la fecha actual. Por lo tanto,
             * para calcular la ETo y la ETc del siguiente registro climatico
             * se calcula la fecha siguiente.
             */
            pastDate.set(Calendar.DAY_OF_YEAR, ((currentDate.get(Calendar.DAY_OF_YEAR) - numberDays) + i));
        } // End for

    }

    /**
     * Calcula el agua excedente de los registros climaticos de
     * una parcela anteriores a la fecha actual. Las fechas de
     * estos registros climaticos estan comprendidas en el
     * conjunto de fechas que van desde el dia inmediatamente
     * anterior a la fecha actual hasta una cantidad de dias
     * hacia atras. Esta cantidad de dias esta determinada por
     * el valor de la constante NUMBER_DAYS de la clase
     * ClimateRecordServiceBean.
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

        Calendar currentDate = UtilDate.getCurrentDate();
        Calendar pastDate = Calendar.getInstance();
        ClimateRecord pastClimateRecord = null;
        double excessWaterPastDate = 0.0;

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
         * requestAndPersistClimateRecordsForPeriod de esta clase si
         * no existen en la base de datos subyacente.
         */
        pastDate.set(Calendar.DAY_OF_YEAR, (currentDate.get(Calendar.DAY_OF_YEAR) - numberDays));

        /*
         * Calcula el agua excedente de cada uno de los registros
         * climaticos de una parcela anteriores a la fecha actual
         * desde el mas antiguo de ellos hasta el mas actual de
         * ellos. Estos registros climaticos son obtenidos y persistidos
         * por el metodo requestAndPersistClimateRecordsForPeriod
         * de esta clase si no existen en la base de datos subyacente.
         */
        for (int i = 1; i < numberDays + 1; i++) {

            /*
             * Si existe el registro climatico de una fecha pasada
             * perteneciente a una parcela dada, se calcula y actualiza
             * el agua excedente del mismo
             */
            if (climateRecordService.checkExistence(pastDate, givenParcel)) {
                pastClimateRecord = climateRecordService.find(pastDate, givenParcel);

                /*
                 * Calculo del agua excedente que hay en una parcela en
                 * una fecha dada, la cual esta determinada por un registro
                 * climatico, ya que un registro climatico de una parcela
                 * tiene fecha
                 */
                excessWaterPastDate = calculateExcessWaterForClimateRecord(pastClimateRecord);

                /*
                 * Actualizacion del agua excedente del registro climatico
                 * de una fecha pasada, es decir, anterior a la fecha actual
                 */
                climateRecordService.updateExcessWater(pastDate, givenParcel, excessWaterPastDate);
            }

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
            pastDate.set(Calendar.DAY_OF_YEAR, ((currentDate.get(Calendar.DAY_OF_YEAR) - numberDays) + i));
        } // End for

    }

    /**
     * Calcula la ETo (evapotranspiracion del cultivo de referencia)
     * con los datos meteorologicos de una fecha dada, la cual esta
     * determinada por un registro climatico, ya que este tiene
     * fecha
     * 
     * @param givenClimateRecord
     * @return double que representa la ETo (evapotranspiracion del
     * cultivo de referencia) calculada en una fecha con los datos
     * meteorologicos de un registro climatico perteneciente a una
     * parcela de una fecha dada
     */
    private double calculateEtoForClimateRecord(ClimateRecord givenClimateRecord) {
        Parcel givenParcel = givenClimateRecord.getParcel();
        double extraterrestrialSolarRadiation = solarService.getRadiation(givenParcel.getLatitude(),
                monthService.getMonth(givenClimateRecord.getDate().get(Calendar.MONTH)),
                latitudeService.find(givenParcel.getLatitude()),
                latitudeService.findPreviousLatitude(givenParcel.getLatitude()),
                latitudeService.findNextLatitude(givenParcel.getLatitude()));

        /*
         * Calculo de la evapotranspiracion del cultivo de
         * referencia (ETo) de una fecha, la cual esta
         * determinada por un registro climatico, ya que
         * este tiene fecha
         */
        return HargreavesEto.calculateEto(givenClimateRecord.getMaximumTemperature(), givenClimateRecord.getMinimumTemperature(), extraterrestrialSolarRadiation);
    }

    /**
     * Hay que tener en cuenta que este metodo calcula la ETc
     * de un cultivo para una fecha dada, ya que la ETo es de
     * una fecha dada. Si la ETo es de la fecha X, la ETc
     * calculada sera de la fecha X.
     * 
     * @param givenEto
     * @param givenPlantingRecord
     * @param dateUntil
     * @return double que representa la ETc (evapotranspiracion
     * del cultivo bajo condiciones estandar) de un cultivo
     * calculada con la ETo de una fecha dada, por lo tanto,
     * calcula la ETc de un cultivo que estuvo en desarollo
     * en una fecha dada
     */
    private double calculateEtcForClimateRecord(double givenEto, PlantingRecord givenPlantingRecord, Calendar dateUntil) {
        return Etc.calculateEtc(givenEto, cropService.getKc(givenPlantingRecord.getCrop(), givenPlantingRecord.getSeedDate(), dateUntil));
    }

    /**
     * @param givenClimateRecord
     * @return double que representa el agua excedente que hay
     * en una parcela en una fecha dada, la cual esta determinada
     * por un registro climatico, ya que este tiene fecha
     */
    private double calculateExcessWaterForClimateRecord(ClimateRecord givenClimateRecord) {
        double totalIrrigationWaterGivenDate = irrigationRecordService
                .calculateTotalIrrigationWaterGivenDate(givenClimateRecord.getDate(), givenClimateRecord.getParcel());
        return WaterMath.calculateExcessWater(givenClimateRecord.getEto(), givenClimateRecord.getEtc(), givenClimateRecord.getPrecip(), totalIrrigationWaterGivenDate,
                getExcessWaterYesterdayFromDate(givenClimateRecord.getParcel(), givenClimateRecord.getDate()));
    }

    /**
     * @param givenParcel
     * @param givenDate
     * @return double que representa el agua excedente del dia
     * inmediatamente anterior a una fecha si existe el registro
     * climatico de dicho dia perteneciente a una parcela dada.
     * Si no existe, double que representa el agua excedente de
     * antes de ayer si existe el registro climatico de dicha dia
     * perteneciente a una parcela dada. En caso de que no exista
     * ninguno de estos dos registros de una parcela dada, 0.0.
     */
    private double getExcessWaterYesterdayFromDate(Parcel givenParcel, Calendar givenDate) {
        double excessWaterYesterday = 0.0;

        /*
         * Obtiene la fecha inmediatamente anterior a una fecha
         * dada
         */
        Calendar yesterdayDate = UtilDate.getYesterdayDateFromDate(givenDate);

        /*
         * Si el registro climatico perteneciente a una parcela del
         * dia inmediatamente anterior a una fecha, existe, se obtiene
         * el agua excedente de dicho dia. En caso contrario, si el
         * registro climatico del dia anterior al dia inmediatamente
         * anterior a una fecha (esto es, el dia antes de ayer) existe,
         * se obtiene el agua excedente de dicho dia.
         * 
         * Si NO existen ninguno de estos dos registros climaticos para
         * una parcela dada, se asume que el agua excedente del dia
         * inmediatamente anterior a una fecha es 0.
         */
        if (climateRecordService.checkExistence(yesterdayDate, givenParcel)) {
            excessWaterYesterday = climateRecordService.find(yesterdayDate, givenParcel).getExcessWater();
        } else {
            Calendar dateBeforeYesterday = Calendar.getInstance();
            dateBeforeYesterday.set(Calendar.DAY_OF_YEAR, (yesterdayDate.get(Calendar.DAY_OF_YEAR) - 1));

            if (climateRecordService.checkExistence(dateBeforeYesterday, givenParcel)) {
                excessWaterYesterday = climateRecordService.find(dateBeforeYesterday, givenParcel).getExcessWater();
            }

        }

        return excessWaterYesterday;
    }

}