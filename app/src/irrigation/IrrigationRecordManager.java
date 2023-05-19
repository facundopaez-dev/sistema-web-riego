package irrigation;

import java.util.Calendar;
import java.util.Collection;
import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Stateless;
import stateless.IrrigationRecordServiceBean;
import stateless.ParcelServiceBean;
import stateless.PlantingRecordServiceBean;
import stateless.ClimateRecordServiceBean;
import model.ClimateRecord;
import model.IrrigationRecord;
import model.Parcel;
import model.Crop;
import util.UtilDate;

@Stateless
public class IrrigationRecordManager {

    // inject a reference to the IrrigationRecordServiceBean
    @EJB
    IrrigationRecordServiceBean irrigationRecordService;

    // inject a reference to the ParcelServiceBean
    @EJB
    ParcelServiceBean parcelService;

    // inject a reference to the PlantingRecordServiceBean
    @EJB
    PlantingRecordServiceBean plantingRecordService;

    // inject a reference to the ClimateRecordServiceBean
    @EJB
    ClimateRecordServiceBean climateRecordService;

    private final String NOT_AVAILABLE = "n/a";

    /**
     * Crea y persiste de manera automatica el registro de riego actual (de la
     * fecha actual) generado por el sistema para las parcelas activas que NO
     * lo tienen y tienen un registro de plantacion en desarrollo (es decir,
     * que tienen un cultivo en desarrollo). Esto lo hace cada dos horas a
     * partir de la hora 01.
     * 
     * La segunda anotacion @Schedule es para probar que este metodo se ejecuta
     * correctamente, es decir, que crea y persiste el registro de riego de la
     * fecha actual para las parcelas activas que tienen un cultivo en desarrollo.
     * 
     * El archivo t125Inserts.sql de la ruta app/etc/sql tiene datos para probar
     * que este metodo se ejecuta correctamente, es decir, que hace lo que se
     * espera que haga.
     */
    @Schedule(second = "*", minute = "*", hour = "1/2", persistent = false)
    // @Schedule(second = "*/5", minute = "*", hour = "*", persistent = false)
    private void createCurrentIrrigationRecord() {
        Collection<Parcel> activeParcels = parcelService.findAllActive();

        /*
         * El metodo getInstance de la clase Calendar retorna
         * la referencia a un objeto de tipo Calendar que
         * contiene la fecha actual
         */
        Calendar currentDate = Calendar.getInstance();
        Calendar yesterdayDate = UtilDate.getYesterdayDate();
        ClimateRecord currentClimateRecord = null;
        IrrigationRecord currentIrrigationRecord = null;
        Crop givenCrop = null;

        double currentIrrigationWaterNeed = 0.0;
        double excessWaterYesterday = 0.0;
        double totalIrrigationWaterCurrentDate = 0.0;

        /*
         * Crea y persiste el registro de riego generado por el sistema
         * en la fecha actual para cada una de las parcelas activas que
         * NO lo tienen y tienen un registro de plantacion en desarrollo
         * (es decir, tienen un cultivo en desarrollo)
         */
        for (Parcel currentParcel : activeParcels) {

            /*
             * Si la parcela actual (la que esta siendo utilizada actualmente
             * por la instruccion for each) NO tiene el registro de riego
             * generado por el sistema en la fecha actual y tiene un registro
             * de plantacion en desarrollo (es decir, tiene un cultivo en
             * desarrollo), se crea y persiste un registro de riego generado
             * por el sistema en la fecha actual
             */
            if (!irrigationRecordService.checkExistenceGeneratedBySystem(currentDate, currentParcel)) {
                currentIrrigationRecord = new IrrigationRecord();
                currentIrrigationRecord.setDate(currentDate);
                currentIrrigationRecord.setSystemGenerated(true);
                currentIrrigationRecord.setParcel(currentParcel);

                if (plantingRecordService.checkOneInDevelopment(currentParcel)) {
                    givenCrop = plantingRecordService.findInDevelopment(currentParcel).getCrop();
                    currentIrrigationRecord.setCrop(givenCrop);

                    /*
                     * Si la parcela dada tiene un registro de plantacion en desarrollo
                     * (es decir, tiene un cultivo en desarrollo) y tiene el registro
                     * climatico de la fecha actual, es posible calcular la necesidad
                     * de agua de riego [mm/dia] del cultivo que esta en desarrollo en
                     * la fecha actual. Por lo tanto, se asigna un numero mayor o igual
                     * a cero a la necesidad de agua de riego de un registro de riego
                     * generado por el sistema en la fecha actual para la parcela dada.
                     * 
                     * En cambio, si la parcela dada tiene un registro de plantacion en
                     * desarrollo (es decir, tiene un cultivo en desarrollo), pero NO
                     * tiene el registro climatico de la fecha actual, no se disponen
                     * de la evapotranspiracion del cultivo bajo condiciones estandar
                     * (ETc) [mm/dia] ni de la precipitacion [mm/dia] de la fecha actual,
                     * las cuales son necesarias para calcular la necesidad de agua de
                     * riego [mm/dia] de un cultivo en desarrollo en la fecha actual.
                     * Por lo tanto, se asigna el valor "n/a" (no disponible) a la
                     * necesidad de agua de riego de un registro de riego generado
                     * por el sistema en la fecha actual para la parcela dada.
                     */
                    if (climateRecordService.checkExistence(currentDate, currentParcel)) {
                        currentClimateRecord = climateRecordService.find(currentDate, currentParcel);

                        /*
                         * Si la parcela dada tiene el registro climatico del dia inmediatamente
                         * anterior a la fecha actual, se obtiene el agua excedente del mismo
                         * para calcular la necesidad de agua de riego [mm/dia] del cultivo que
                         * esta en desarrollo en la fecha actual. En caso contrario, se asume
                         * que el agua excedente de dicho dia es 0.
                         */
                        if (climateRecordService.checkExistence(yesterdayDate, currentParcel)) {
                            excessWaterYesterday = climateRecordService.find(currentDate, currentParcel).getExcessWater();
                        }

                        totalIrrigationWaterCurrentDate = irrigationRecordService.calculateTotalIrrigationWaterCurrentDate(currentParcel);

                        /*
                         * Calculo de la necesidad de agua de riego [mm/dia] del cultivo
                         * que esta en desarrollo en la fecha actual
                         */
                        currentIrrigationWaterNeed = WaterMath.calculateIrrigationWaterNeed(
                                currentClimateRecord.getEtc(),
                                currentClimateRecord.getPrecip(),
                                totalIrrigationWaterCurrentDate, excessWaterYesterday);

                        currentIrrigationRecord.setIrrigationWaterNeed(String.valueOf(currentIrrigationWaterNeed));
                    } else {
                        currentIrrigationRecord.setIrrigationWaterNeed(NOT_AVAILABLE);
                    }

                    /*
                     * Se persiste el registro de riego generado por el sistema
                     * en la fecha actual para la parcela dada
                     */
                    irrigationRecordService.create(currentIrrigationRecord);
                } // End if

            } // End if

        } // End for

    }

    /*
     * Establece de manera automatica el atributo modifiable de un registro de
     * riego del pasado (es decir, uno que tiene su fecha estrictamente menor
     * que la fecha actual) en false, ya que un registro de riego del pasado
     * NO se debe poder modificar. Esto lo hace cada 24 horas a partir de las
     * 00 horas.
     * 
     * La segunda anotacion @Schedule es para probar que este metodo se
     * ejecuta correctamente, es decir, que establece el atributo modifiable
     * de un registro de riego del pasado en false.
     * 
     * El archivo irrigationRecordInserts.sql de la ruta app/etc/sql tiene datos
     * para probar que este metodo se ejecuta correctamente, es decir, que hace
     * lo que se espera que haga.
     */
    // @Schedule(second = "*", minute = "*", hour = "0/23", persistent = false)
    // @Schedule(second = "*/10", minute = "*", hour = "*", persistent = false)
    private void unsetModifiable() {
        Collection<IrrigationRecord> modifiableIrrigationRecords = irrigationRecordService.findAllModifiable();

        for (IrrigationRecord currentIrrigationRecord : modifiableIrrigationRecords) {
            /*
             * Si un registro de riego modificable es del pasado (es decir, uno
             * que tiene su fecha estrictamente menor que la fecha actual), se
             * establece su atributo modifiable en false, ya que un registro de
             * riego del pasado NO se debe poder modificar
             */
            if (irrigationRecordService.isFromPast(currentIrrigationRecord.getId())) {
                irrigationRecordService.unsetModifiable(currentIrrigationRecord.getId());
            }

        }

    }

    /*
     * Modifica de manera automatica el valor "n/a" (no disponible) del atributo
     * de la necesidad de agua de riego [mm/dia] de los registros de riego que
     * tienen el cultivo definido, por un numero mayor o igual a cero, el cual
     * resulta de calcular la necesidad de agua de riego de un cultivo. Esto lo
     * hace cada 2 horas a partir de las 00 horas.
     * 
     * La segunda anotacion @Schedule es para probar que este metodo se ejecuta
     * correctamente, es decir, que modifica el atributo irrigationWaterNeed de
     * un registro de riego que tiene el cultivo definido y el valor "n/a" en
     * dicho atributo, por un numero mayor o igual a cero.
     * 
     * Este metodo es para evitar que un registro de riego creado por el usuario
     * para una parcela antes de la existencia del registro climatico actual (de
     * la fecha actual) de una parcela, se quede con el valor "n/a" (no disponible)
     * en el atributo de la necesidad de agua de riego [mm/dia] luego de la existencia
     * de dicho registro climatico.
     * 
     * El metodo automatico getCurrentWeatherDataset de la clase ClimateRecordManager
     * obtiene y persiste los datos meteorologicos de la fecha actual para cada
     * una de las parcelas activas de la base de datos subyacente. 
     */
    @Schedule(second = "*", minute = "*", hour = "0/2", persistent = false)
    // @Schedule(second = "*/5", minute = "*", hour = "*", persistent = false)
    private void setIrrigationWaterNeed() {
        /*
         * Obtiene todos los registros de riego que tienen el cultivo
         * definido y el valor "n/a" (no disponible) en el atributo de
         * la necesidad de agua de riego.
         * 
         * Si un registro de riego NO tiene el cultivo definido, no
         * es posible calcular la necesidad de agua de riego de un
         * cultivo, y, por ende, usarla para modificar el valor "n/a"
         * de la necesidad de agua de riego de un registro de riego.
         * 
         * Un registro de riego tiene el cultivo definido cuando se
         * lo crea para una parcela que tiene un registro de plantacion
         * en desarrollo (es decir, tiene un cultivo en desarrollo).
         */
        Collection<IrrigationRecord> irrigationRecords = irrigationRecordService.findAllUndefinedWithCrop();

        /*
         * El metodo getInstance de la clase Calendar retorna
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
         * Modifica el valor "n/a" (no disponible) del atributo de la necesidad
         * de agua de riego de los registros de riego que tienen el cultivo
         * definido, por un valor mayor o igual a cero, el cual resulta del
         * calculo de la necesidad de agua de riego de un cultivo
         */
        for (IrrigationRecord currentIrrigationRecord : irrigationRecords) {
            givenParcel = currentIrrigationRecord.getParcel();

            /*
             * Si la parcela dada tiene el registro climatico de la fecha
             * actual, es posible calcular la necesidad de agua de riego
             * [mm/dia] del cultivo que esta en desarrollo en la fecha
             * actual, ya que dicho registro tiene la evapotranspiracion
             * del cultivo bajo condiciones estandar (ETc) [mm/dia] y la
             * precipitacion [mm/dia] de la fecha actual, las cuales son
             * necesarias para calcular dicha necesidad. Por lo tanto,
             * se asigna un numero mayor o igual a cero a la necesidad
             * de agua de riego de un registro de riego que tiene el
             * cultivo definido y el valor "n/a" (no disponible) en su
             * atributo de la necesidad de agua de riego.
             * 
             * Un registro de riego tiene el cultivo definido cuando se
             * lo crea para una parcela que tiene un registro de plantacion
             * en desarrollo (es decir, que tiene un cultivo en desarrollo).
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
                    excessWaterYesterday = climateRecordService.find(currentDate, givenParcel).getExcessWater();
                }

                totalIrrigationWaterCurrentDate = irrigationRecordService.calculateTotalIrrigationWaterCurrentDate(givenParcel);

                /*
                 * Calculo de la necesidad de agua de riego [mm/dia] del cultivo
                 * que esta en desarrollo en la fecha actual
                 */
                currentIrrigationWaterNeed = WaterMath.calculateIrrigationWaterNeed(
                        currentClimateRecord.getEtc(),
                        currentClimateRecord.getPrecip(),
                        totalIrrigationWaterCurrentDate, excessWaterYesterday);

                /*
                 * Modifica el valor "n/a" (no disponible) del atributo de la
                 * necesidad de agua de riego del registro de riego dado por
                 * un numero mayor o igual a cero
                 */
                irrigationRecordService.updateIrrigationWaterNeed(currentIrrigationRecord.getId(),
                        givenParcel, String.valueOf(currentIrrigationWaterNeed));
            } // End if

        } // End for

    }

}