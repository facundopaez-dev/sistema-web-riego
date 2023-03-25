package plantingRecord;

import java.util.Collection;
import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Stateless;
import model.PlantingRecord;
import stateless.PlantingRecordServiceBean;
import stateless.PlantingRecordStatusServiceBean;

@Stateless
public class PlantingRecordManager {

    // inject a reference to the PlantingRecordServiceBean
    @EJB
    PlantingRecordServiceBean plantingRecordService;

    @EJB
    PlantingRecordStatusServiceBean plantingRecordStatusService;

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

}