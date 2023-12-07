package irrigation;

import model.ClimateRecord;
import model.IrrigationRecord;
import java.util.Collection;

/*
 * Esta clase representa (y contiene en forma de metodo) el algoritmo
 * del calculo, sin umbral de riego, de la necesidad de agua de riego
 * de un cultivo en una fecha [mm/dia]. La palabra "Wit" al final del
 * nombre de la clase proviene de la frase en ingles "Without Irrigation
 * Threshold" que en español significa "sin umbra de riego".
 */
public class WaterNeedWit {

    /*
     * El metodo constructor tiene el modificador de acceso 'private'
     * para que ningun programador trate de instanciar esta clase
     * desde afuera, ya que todos los metodos publicos de la misma
     * son estaticos, con lo cual no se requiere una instancia de
     * esta clase para invocar a sus metodos publicos
     */
    private WaterNeedWit() {

    }

    /**
     * Calcula la necesidad de agua de riego de un cultivo en una
     * fecha [mm/dia] dada utilizando la cantidad total de agua de
     * riego de una fecha dada, una coleccion de registros climaticos
     * y una coleccion de registros de riego, siendo todos ellos
     * previos a una fecha y pertenecientes a una misma parcela.
     * 
     * La fecha puede ser la fecha actual (es decir, hoy), una fecha
     * futura (es decir, posterior a la fecha actual) o una fecha
     * pasada (es decir, anterior a la fecha actual). No tiene sentido
     * que la fecha sea del pasado si lo que se busca es determinar
     * la necesidad de agua de riego de un cultivo en la fecha actual
     * (es decir, hoy) o en una fecha posterior a la fecha actual.
     * 
     * La fecha para la que se calcula la necesidad de agua de riego
     * de un cultivo esta determinada por los registros climaticos y
     * los registros de riego que se seleccionan como previos a una
     * fecha dada, siendo ambos conjuntos de registros pertenecientes
     * a una parcela dada.
     * 
     * Por ejemplo, si se seleccionan los registros climaticos y los
     * registros de riego de una parcela dada previos a la fecha
     * actual (es decir, hoy), la necesidad de agua de riego de un
     * cultivo calculada con estos registros corresponde a la fecha
     * actual. En cambio, si se seleccionan los registros climaticos
     * y los registros de riego de una parcela dada previos a la
     * fecha actual + X dias, donde X > 0, la necesidad de agua de
     * riego de un cultivo calculada con estos registros corresponde
     * a la fecha actual + X dias.
     * 
     * Se debe tener en cuenta que este metodo puede ser invocado
     * con registros climaticos y registros de riego previos a una
     * fecha pertenecientes a una parcela que NO tiene un cultivo
     * sembrado. En caso de que ocurra esto, el valor devuelto por
     * el mismo es el acumulado del deficit de agua por dia de dias
     * previos a una fecha de de una parcela en una fecha [mm/dia].
     * En caso de que se invoque este metodo con registros climaticos
     * y registros de riego previos a una fecha pertenecientes a
     * una parcela que tiene un cultivo sembrado en una fecha, el
     * valor devuelto por el mismo es el acumulado del deficit de
     * agua por dia de dias previos a una fecha de un cultivo en
     * una fecha [mm/dia] y representa la necesidad de agua de riego
     * de un cultivo en una fecha.
     * 
     * @param totalIrrigationWaterGivenDate
     * @param previousClimateRecords
     * @param previousIrrigationRecords
     * @return double que representa la necesidad de agua de riego
     * de un cultivo en una fecha [mm/dia], si se invoca este metodo
     * para una parcela que tiene un cultivo sembrado. En caso contrario,
     * double que representa el acumulado del deficit de agua por dia
     * de una parcela en una fecha [mm/dia].
     */
    public static double calculateIrrigationWaterNeed(double totalIrrigationWaterGivenDate, Collection<ClimateRecord> previousClimateRecords,
            Collection<IrrigationRecord> previousIrrigationRecords) {
        /*
         * El acumulado del deficit (falta) de agua por dia [mm/dia]
         * de dias previos a una fecha es la cantidad acumulada de agua
         * evaporada en dias previos a una fecha que no fue cubierta
         * (satisfecha).
         * 
         * Si el acumulado del deficit de agua por dia de dias previos
         * a una fecha es igual a 0, significa que la cantidad de agua
         * evaporada en dias previos a una fecha fue cubierta (satisfecha),
         * por lo tanto, NO hay una cantidad de agua evaporada de dias
         * previos que se deba reponer (satisfacer) mediante el riego
         * en una fecha.
         * 
         * En cambio, si el acumulado del deficit de agua por dia de
         * dias previos a una fecha es menor a 0, significa que la
         * cantidad de agua evaporada en dias previos a una fecha NO
         * fue cubierta (satisfecha), por lo tanto, hay una cantidad de
         * agua evaporada de dias previos que se debe reponer mediante
         * el riego en una fecha.
         * 
         * Hay que tener en cuenta que el metodo calculateAccumulatedWaterDeficitPerDay
         * retorna un double igual a cero o un double mayor a cero. El
         * motivo por el cual retorna un double mayor a cero en lugar
         * de un double menor a cero es que calcula el valor absoluto
         * del acumulado del deficit de agua por dia [mm/dia] de dias
         * previos a una fecha. En consecuencia, cuando el acumulado del
         * deficit de agua por dia de dias previos a una fecha es menor
         * a cero (negativo), retorna un acumulado del deficit de agua
         * por dia de dias previos a una fecha mayor a cero (positivo).
         * 
         * Por lo tanto, un acumulado del deficit de agua por dia [mm/dia]
         * de dias previos a una fecha positivo representa que la cantidad
         * de agua evaporada de dias previos a una fecha NO fue cubierta
         * (satisfecha), con lo cual hay una cantidad de agua evaporada
         * que se debe reponer mediante el riego en una fecha.
         */
        double accumulatedWaterDeficitPerDay = WaterMath.calculateAccumulatedWaterDeficitPerDay(previousClimateRecords, previousIrrigationRecords);

        /*
         * Si la cantidad total de agua de riego de una fecha [mm/dia]
         * es mayor o igual al acumulado del deficit (falta) de agua por
         * dia [mm/dia] de dias previos a una fecha, la necesidad de
         * agua de riego de un cultivo en una fecha es 0 [mm/dia]
         */
        if (totalIrrigationWaterGivenDate >= accumulatedWaterDeficitPerDay) {
            return 0.0;
        }

        /*
         * Si el acumulado del deficit (falta) de agua por dia [mm/dia] de
         * dias previos a una fecha es estrictamente mayor a la cantidad
         * total de agua de riego de una fecha [mm/dia], la necesidad
         * de agua de riego de un cultivo en una fecha [mm/dia] se
         * calcula como la diferencia entre estas dos variables
         */
        return accumulatedWaterDeficitPerDay - totalIrrigationWaterGivenDate;
    }

}