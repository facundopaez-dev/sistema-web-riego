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

    /**
     * Crea y persiste de manera automatica el registro de riego actual (de la fecha
     * actual) para las parcelas activas que tienen un cultivo en desarrollo. Esto lo
     * hace cada dos horas a partir de la hora 01.
     * 
     * La segunda anotacion @Schedule es para probar que este metodo se ejecuta
     * correctamente, es decir, que crea y persiste el registro de riego de la fecha
     * actual para las parcelas activas que tienen un cultivo en desarrollo.
     * 
     * El archivo t125Inserts.sql de la ruta app/etc/sql tiene datos para probar que
     * este metodo se ejecuta correctamente, es decir, que hace lo que se espera que
     * haga.
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
         * Crea y persiste el registro de riego de la fecha actual para
         * cada una de las parcelas activas que tienen un cultivo en
         * desarrollo
         */
        for (Parcel currentParcel : activeParcels) {

            /*
             * Si la parcela actual (la que esta siendo utilizada actualmente por
             * la instruccion for each) NO tiene el registro de riego de la fecha
             * actual generado por el sistema, tiene un registro de plantacion en
             * desarrollo (es decir, que tiene un cultivo en desarrollo) y tiene
             * el registro climatico de la fecha actual, se crea y persiste el
             * registro de riego de la fecha actual generado por el sistema para
             * dicha parcela
             */
            if (!(irrigationRecordService.checkExistenceGeneratedBySystem(currentDate, currentParcel))
                    && (plantingRecordService.checkOneInDevelopment(currentParcel))) {

                /*
                 * Si en la base de datos subyacente existe el registro climatico de
                 * la fecha actual para una parcela dada, se utiliza la evapotranspiracion
                 * del cultivo bajo condiciones estandar (ETc) [mm/dia] y la precipitacion
                 * [mm/dia] de dicho registro, entre otros datos, para calcular la necesidad
                 * de agua de riego [mm/dia] del cultivo que esta en desarrollo en la
                 * fecha actual. El resultado de este calculo se asigna al registro de
                 * riego de la fecha actual.
                 */
                if (climateRecordService.checkExistence(currentDate, currentParcel)) {
                    currentClimateRecord = climateRecordService.find(currentDate, currentParcel);
                    givenCrop = plantingRecordService.findInDevelopment(currentParcel).getCrop();

                    /*
                     * Si en la base de datos subyacente existe el registro climatico
                     * del dia inmediatamente anterior a la fecha actual, se obtiene
                     * su agua excedente para calcular la necesidad de agua de riego
                     * [mm/dia] del cultivo que esta en desarrollo en la fecha actual.
                     * En caso contrario, se asume que el agua excedente de dicho dia
                     * es 0.
                     */
                    if (climateRecordService.checkExistence(yesterdayDate, currentParcel)) {
                        excessWaterYesterday = climateRecordService.find(currentDate, currentParcel).getExcessWater();
                    }

                    totalIrrigationWaterCurrentDate = irrigationRecordService.calculateTotalIrrigationWaterCurrentDate(currentParcel);

                    /*
                     * Calculo de la necesidad de agua de riego [mm/dia] del cultivo
                     * que esta en desarrollo en la fecha actual
                     */
                    currentIrrigationWaterNeed = WaterMath.calculateIrrigationWaterNeed(currentClimateRecord.getEtc(),
                            currentClimateRecord.getPrecip(), totalIrrigationWaterCurrentDate, excessWaterYesterday);

                    /*
                     * Creacion y persistencia del registro de riego de la
                     * fecha actual para una parcela dada
                     */
                    currentIrrigationRecord = new IrrigationRecord();
                    currentIrrigationRecord.setDate(currentDate);
                    currentIrrigationRecord.setIrrigationWaterNeed(String.valueOf(currentIrrigationWaterNeed));
                    currentIrrigationRecord.setSystemGenerated(true);
                    currentIrrigationRecord.setParcel(currentParcel);
                    currentIrrigationRecord.setCrop(givenCrop);

                    irrigationRecordService.create(currentIrrigationRecord);
                }

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
    @Schedule(second = "*", minute = "*", hour = "0/23", persistent = false)
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

}