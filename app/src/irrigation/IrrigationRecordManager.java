package irrigation;

import java.util.Collection;
import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Stateless;
import model.IrrigationRecord;
import stateless.IrrigationRecordServiceBean;

@Stateless
public class IrrigationRecordManager {

    // inject a reference to the IrrigationRecordServiceBean
    @EJB
    IrrigationRecordServiceBean irrigationRecordService;

    /*
     * Establece de manera automatica el atributo modifiable de un registro de
     * riego del pasado (es decir, uno que tiene su fecha estrictamente menor
     * que la fecha actual) en false, ya que un registro de riego del pasado
     * NO se dene poder modificar. Esto lo hace cada 24 horas a parit de las
     * 00 horas.
     * 
     * La segunda anotacion @Schedule es para probar que este metodo se
     * ejecuta correctamente, es decir, que elimina los enlaces de activacion
     * de cuenta NO consumidos y expirados, y las cuentas registradas asociadas
     * a los mismos.
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