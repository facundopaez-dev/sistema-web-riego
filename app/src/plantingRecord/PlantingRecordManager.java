package plantingRecord;

import java.util.Calendar;
import java.util.Collection;
import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Stateless;
import stateless.PlantingRecordServiceBean;
import stateless.PlantingRecordStatusServiceBean;
import stateless.IrrigationRecordServiceBean;
import stateless.ClimateRecordServiceBean;
import model.Parcel;
import model.PlantingRecord;
import model.ClimateRecord;
import irrigation.WaterMath;
import util.UtilDate;

@Stateless
public class PlantingRecordManager {

    // inject a reference to the PlantingRecordServiceBean
    @EJB
    PlantingRecordServiceBean plantingRecordService;

    @EJB
    PlantingRecordStatusServiceBean plantingRecordStatusService;

    // inject a reference to the IrrigationRecordServiceBean
    @EJB
    IrrigationRecordServiceBean irrigationRecordService;

    // inject a reference to the ClimateRecordServiceBean
    @EJB
    ClimateRecordServiceBean climateRecordService;

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
     * Establece de manera automatica el estado fianlizado de un registro de
     * plantacion presuntamente en desarrollo en el caso en el que la fecha
     * de cosecha de este sea estrictamente menor a la fecha actual. Esto lo
     * hace cada 24 horas a parit de las 00 horas.
     * 
     * La segunda anotacion @Schedule es para probar que este metodo se
     * ejecuta correctamente, es decir, que establece el estado finalizado
     * en un registro de plantacion presuntamente en desarrollo que tiene
     * su fecha de cosecha estrictamente menor a la fecha actual.
     * 
     * El archivo t110Inserts.sql de la ruta app/etc/sql tiene datos para
     * probar que este metodo se ejecuta correctamente, es decir, que hace
     * lo que se espera que haga.
     */
    @Schedule(second = "*", minute = "*", hour = "0/23", persistent = false)
    // @Schedule(second = "*/10", minute = "*", hour = "*", persistent = false)
    public void modifyStatus() {
        Collection<PlantingRecord> plantingRecords = plantingRecordService.findAllInDevelopment();

        for (PlantingRecord currentPlantingRecord : plantingRecords) {
            /*
             * Si un registro de plantacion presuntamente en desarrollo,
             * NO esta en desarrollo, se establece el estado finalizado
             * en el.
             * 
             * Un registro de plantacion en desarrollo, esta en desarrollo
             * si su fecha de cosecha es mayor o igual a la fecha actual.
             * En cambio, si su fecha de cosecha es estrictamente menor
             * a la fecha actual, se debe establecer el estado finalizado
             * en el mismo.
             */
            if (!plantingRecordService.checkDevelopmentStatus(currentPlantingRecord)) {
                plantingRecordService.setStatus(currentPlantingRecord.getId(), plantingRecordStatusService.findFinished());
            }

        }

    }

    /*
     * Establece de manera automatica el atributo modifiable de un registro
     * de plantacion finalizado en false, ya que un registro de plantacion
     * finalizado NO se debe poder modificar. Esto lo hace cada 24 horas a
     * partir de la hora 01, una hora despues de la ejecucion automatica del
     * metodo modifyStatus de esta clase.
     * 
     * La segunda anotacion @Schedule es para probar que este metodo se ejecuta
     * correctamente, es decir, que establece el atributo modifiable de un registro
     * de plantacion finalizado en false.
     * 
     * El archivo plantingRecordInserts.sql de la ruta app/etc/sql tiene datos
     * para probar que este metodo se ejecuta correctamente, es decir, que hace
     * lo que se espera que haga.
     */
    @Schedule(second = "*", minute = "*", hour = "1/23", persistent = false)
    // @Schedule(second = "*/10", minute = "*", hour = "*", persistent = false)
    public void unsetModifiable() {
        Collection<PlantingRecord> finishedPlantingRecords = plantingRecordService.findAllFinished();

        /*
         * Establece en false el atributo modifiable de un registro de
         * plantacion finalizado, ya que un registro de plantacion
         * finalizado NO se debe poder modificar
         */
        for (PlantingRecord currentPlantingRecord : finishedPlantingRecords) {
            plantingRecordService.unsetModifiable(currentPlantingRecord.getId());
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

}