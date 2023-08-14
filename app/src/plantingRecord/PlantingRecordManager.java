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
    // @Schedule(second = "*", minute = "*", hour = "0/23", persistent = false)
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
    // @Schedule(second = "*", minute = "*", hour = "1/23", persistent = false)
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
    // @Schedule(second = "*", minute = "*", hour = "1/2", persistent = false)
    // @Schedule(second = "*/5", minute = "*", hour = "*", persistent = false)
    private void setIrrigationWaterNeed() {
        Collection<PlantingRecord> developingPlantingRecords = plantingRecordService.findAllInDevelopment();
        Parcel givenParcel = null;

        double currentIrrigationWaterNeed = 0.0;

        /*
         * Establece la necesidad de agua de riego [mm/dia] de la fecha
         * actual en cada uno de los registros de plantacion en desarrollo
         * de todas las parcelas.
         * 
         * Esto es que establece la necesidad de agua de riego [mm/dia] de
         * la fecha actual de cada cultivo en desarrollo de cada una de
         * las parcelas. Esto se hace en funcion de la ETc del dia anterior
         * o de los dias anteriores a la fecha actual, del agua de lluvia
         * del dia anterior o de los dias anteriores a la fecha actual y del
         * agua de riego del dia anterior o de los dias anteriores a la fecha
         * actual.
         */
        for (PlantingRecord developingPlantingRecord : developingPlantingRecords) {
            givenParcel = developingPlantingRecord.getParcel();

            /*
             * Persiste NUMBER_DAYS registros climaticos anteriores a la
             * fecha actual pertenecientes a una parcela dada que tiene
             * un cultivo sembrado y en desarrollo en la fecha actual. Estos
             * registros climaticos son obtenidos del servicio meteorologico
             * utilizado por la aplicacion.
             */
            requestPastClimateRecords(developingPlantingRecord);

            /*
             * Calcula la ETo y la ETc de NUMBER_DAYS registros climaticos
             * anteriores a la fecha actual pertenecientes a una parcela
             * dada que tiene un cultivo sembrado y en desarrollo en la
             * fecha actual
             */
            calculateEtsPastClimateRecords(developingPlantingRecord);

            /*
             * Calculo de la necesidad de agua de riego en la fecha actual
             * de un cultivo sembrado y en desarrollo en una parcela
             */
            currentIrrigationWaterNeed = calculateIrrigationWaterNeedCurrentDate(givenParcel.getUser().getId(), givenParcel);

            /*
             * Actualizacion de la necesidad de agua de riego del
             * registro de plantacion en desarrollo correspondiente
             * al cultivo en desarrollo en la fecha actual
             */
            plantingRecordService.updateIrrigationWaterNeed(developingPlantingRecord.getId(), givenParcel, String.valueOf(currentIrrigationWaterNeed));
        } // End for

    }

    /**
     * Persiste NUMBER_DAYS registros climaticos anteriores a la
     * fecha actual pertenecientes a una parcela que tiene un
     * cultivo sembrado y en desarrollo en la fecha actual.
     * Estos registros climaticos son obtenidos del servicio
     * meteorologico utilizado por la aplicacion.
     * 
     * @param developingPlantingRecord
     */
    private void requestPastClimateRecords(PlantingRecord developingPlantingRecord) {
        /*
         * Variable utilizada para obtener una cantidad determinada
         * de registros climaticos anteriores a la fecha actual
         * pertenecientes a una parcela que tiene un cultivo
         * sembrado y en desarrollo en la fecha actual
         */
        int numberDays = climateRecordService.getNumberDays();

        /*
         * Parcela que tiene un cultivo plantado y en desarrollo en
         * la fecha actual
         */
        Parcel givenParcel = developingPlantingRecord.getParcel();

        /*
         * Fecha a partir de la cual se obtienen los registros
         * climaticos del pasado pertenecientes a una parcela
         * que tiene un cultivo sembrado y en desarrollo en la
         * fecha actual
         */
        Calendar givenPastDate = UtilDate.getPastDateFromOffset(numberDays);
        ClimateRecord newClimateRecord;

        /*
         * Crea y persiste NUMBER_DAYS registros climaticos
         * anteriores a la fecha actual pertenecientes a una
         * parcela que tiene un cultivo plantado y en desarrollo
         * en la fecha actual. Estos registros climaticos van
         * desde la fecha resultante de la resta entre el numero
         * de dia en el año de la fecha actual y numberDays, hasta
         * la fecha inmediatamente anterior a la fecha actual.
         */
        for (int i = 0; i < numberDays; i++) {

            /*
             * Si una parcela dada NO tiene un registro climatico
             * de una fecha anterior a la fecha actual, se lo solicita
             * al servicio meteorologico utilizado y se lo persiste
             */
            if (!climateRecordService.checkExistence(givenPastDate, givenParcel)) {
                newClimateRecord = ClimateClient.getForecast(givenParcel, givenPastDate.getTimeInMillis() / 1000);
                climateRecordService.create(newClimateRecord);
            }

            /*
             * Suma un uno al numero de dia en el año de una fecha
             * pasada dada para obtener el siguiente registro climatico
             * correspondiente a una fecha pasada
             */
            givenPastDate.set(Calendar.DAY_OF_YEAR, givenPastDate.get(Calendar.DAY_OF_YEAR) + 1);
        }

    }

    /**
     * Calcula y actualiza la ETo y la ETc de registros
     * climaticos anteriores a la fecha actual pertenecientes
     * a una parcela que tiene un cultivo sembrado y en
     * desarrollo en la fecha actual
     * 
     * @param developingPlantingRecord
     */
    private void calculateEtsPastClimateRecords(PlantingRecord developingPlantingRecord) {
        /*
         * Variable utilizada para calcular la ETo (evapotranspiracion
         * del cultivo de referencia) y la ETc (evapotranspiracion del
         * cultivo bajo condiciones estandar) de registros climaticos
         * anteriores a la fecha actual pertenecientes a una parcela
         * que tiene un cultivo sembrado y en desarrollo en la fecha
         * actual
         */
        int numberDays = climateRecordService.getNumberDays();

        /*
         * Parcela que tiene un cultivo plantado y en desarrollo en
         * la fecha actual
         */
        Parcel givenParcel = developingPlantingRecord.getParcel();

        /*
         * Fecha a partir de la cual se recuperan de la base de
         * datos subyacente los registros climaticos del pasado
         * pertenecientes a una parcela que tiene un cultivo
         * sembrado y en desarrollo en la fecha actual
         */
        Calendar givenPastDate = UtilDate.getPastDateFromOffset(numberDays);
        ClimateRecord givenClimateRecord;

        double eto = 0.0;
        double etc = 0.0;

        /*
         * Calcula la ETo y la ETc de NUMBER_DAYS registros climaticos
         * anteriores a la fecha actual pertenecientes a una
         * parcela que tiene un cultivo plantado y en desarrollo
         * en la fecha actual. Estos registros climaticos van
         * desde la fecha resultante de la resta entre el numero
         * de dia en el año de la fecha actual y numberDays, hasta
         * la fecha inmediatamente anterior a la fecha actual.
         */
        for (int i = 0; i < numberDays; i++) {

            /*
             * Si una parcela dada tiene un registro climatico de una
             * fecha anterior a la fecha actual, calcula la ETo y la
             * ETc del mismo
             */
            if (climateRecordService.checkExistence(givenPastDate, givenParcel)) {
                givenClimateRecord = climateRecordService.find(givenPastDate, givenParcel);

                eto = calculateEtoForClimateRecord(givenClimateRecord);
                etc = calculateEtcForClimateRecord(eto, developingPlantingRecord, givenPastDate);

                climateRecordService.updateEtoAndEtc(givenPastDate, givenParcel, eto, etc);
            }

            /*
             * Suma un uno al numero de dia en el año de una fecha
             * pasada dada para obtener el siguiente registro climatico
             * correspondiente a una fecha pasada
             */
            givenPastDate.set(Calendar.DAY_OF_YEAR, givenPastDate.get(Calendar.DAY_OF_YEAR) + 1);
        }

    }

    /**
     * Calcula la necesidad de agua de riego de un cultivo en
     * desarrollo en la fecha actual en funcion de la suma de
     * la ETc de NUMBER_DAYS dias anteriores a la fecha actual,
     * la suma del agua de lluvia de NUMBER_DAYS dias anteriores
     * a la fecha actual, la suma del agua de riego de NUMBER_DAYS
     * dias anteriores a la fecha actual y la cantidad total del
     * agua de riego de la fecha actual
     * 
     * @param developingPlantingRecord
     * @return double que representa la necesidad de agua de
     *         riego en la fecha actual de un cultivo en desarrollo
     */
    private double calculateIrrigationWaterNeedCurrentDate(int userId, Parcel givenParcel) {
        double etcSummedPastDays = climateRecordService.sumEtcPastDays(userId, givenParcel.getId());
        double summedRainwaterPastDays = climateRecordService.sumRainwaterPastDays(userId, givenParcel.getId());
        double summedIrrigationWaterPastDays = irrigationRecordService.sumIrrigationWaterPastDays(userId, givenParcel.getId(), climateRecordService.getNumberDays());
        double totalIrrigationWaterCurrentDate = irrigationRecordService.calculateTotalIrrigationWaterCurrentDate(givenParcel);

        return WaterMath.calculateIrrigationWaterNeed(etcSummedPastDays, summedRainwaterPastDays, summedIrrigationWaterPastDays, totalIrrigationWaterCurrentDate);
    }

    /**
     * Calcula la ETo (evapotranspiracion del cultivo de referencia)
     * con los datos meteorologicos de una fecha dada, la cual esta
     * determinada por un registro climatico, ya que un registro
     * climatico tiene fecha
     * 
     * @param givenClimateRecord
     * @return double que representa la ETo (evapotranspiracion del
     * cultivo de referencia) calculada en una fecha con los datos
     * meteorologicos de un registro climatico que tiene una fecha
     * y pertenece a una parcela
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
         * un registro climatico tiene fecha
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

}