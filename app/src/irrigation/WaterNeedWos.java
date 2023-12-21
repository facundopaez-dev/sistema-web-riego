package irrigation;

import model.ClimateRecord;
import model.IrrigationRecord;
import java.util.Collection;

/*
 * Esta clase representa (y contiene en forma de metodo) el algoritmo
 * del calculo, sin datos de suelo o sin suelo, de la necesidad de agua
 * de riego de un cultivo en una fecha [mm/dia]. La palabra "Wos" al
 * final del nombre de la clase proviene de la frase en ingles "WithOut
 * Soil" que en español significa "sin suelo".
 */
public class WaterNeedWos {

    /*
     * El metodo constructor tiene el modificador de acceso 'private'
     * para que ningun programador trate de instanciar esta clase
     * desde afuera, ya que todos los metodos publicos de la misma
     * son estaticos, con lo cual no se requiere una instancia de
     * esta clase para invocar a sus metodos publicos
     */
    private WaterNeedWos() {

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
     * previos a una fecha de una parcela en una fecha [mm/dia]. En
     * caso de que se invoque este metodo con registros climaticos
     * y registros de riego previos a una fecha pertenecientes a
     * una parcela que tiene un cultivo sembrado en una fecha, el
     * valor devuelto por el mismo es el acumulado del deficit de
     * agua por dia de dias previos a una fecha de un cultivo en
     * una fecha [mm/dia] y representa la necesidad de agua de riego
     * de un cultivo en una fecha [mm/dia].
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
         * evaporada en dias previos a una fecha [mm/dia] que NO fue
         * cubierta (satisfecha).
         * 
         * Si el acumulado del deficit de agua por dia de dias previos
         * a una fecha es mayor o igual a 0, significa que la cantidad
         * de agua evaporada en dias previos a una fecha fue cubierta
         * (satisfecha), por lo tanto, NO hay una cantidad de agua
         * evaporada de dias previos a una fecha que se deba reponer
         * (satisfacer) mediante el riego en una fecha.
         * 
         * En cambio, si el acumulado del deficit de agua por dia de
         * dias previos a una fecha es estrictamente menor a 0, significa
         * que la cantidad de agua evaporada en dias previos a una fecha
         * NO fue cubierta (satisfecha), por lo tanto, hay una cantidad
         * de agua evaporada de dias previos a una fecha que se debe
         * reponer mediante el riego en una fecha.
         */
        double accumulatedWaterDeficitPerDay = WaterMath.calculateAccumulatedWaterDeficitPerDay(previousClimateRecords, previousIrrigationRecords);

        /*
         * Si este metodo es invocado con una coleccion de registros
         * climaticos y una coleccion de registros de riego previos a
         * una fecha pertenecientes a una parcela que NO tiene un cultivo
         * sembrado, el valor devuelto por el mismo representa el acumulado
         * del deficit de agua por dia de dias previos a una fecha de
         * una parcela en una fecha [mm/dia]. En cambio, si es invocado
         * con una coleccion de registros climaticos y una coleccion de
         * registros de riego previos a una fecha pertenecientes a una
         * parcela que tiene un cultivo sembrado en una fecha, el valor
         * devuelto por el mismo representa la necesidad de agua de riego
         * de un cultivo en una fecha [mm/dia].
         * 
         * El metodo calculateAccumulatedWaterDeficitPerDay de la clase
         * WaterMath calcula el acumulado del deficit de agua por dia
         * de un conjunto de dias previos a una fecha [mm/dia] sin tener
         * en cuenta la cantidad total de agua de riego utilizada para
         * regar una parcela o un cultivo en una fecha. Por este motivo,
         * si se quiere obtener correctamente la necesidad de agua de
         * riego de un cultivo en una fecha [mm/dia] se debe realizar la
         * suma entre el acumulado del deficit de agua por dia de dias
         * previos a una fecha [mm/dia] y la cantidad total de agua de
         * riego utilizada en una fecha para regar un cultivo [mm/dia].
         * 
         * El motivo por el cual se realiza la suma entre el acumulado
         * del deficit de agua por dia de dias previos a una fecha [mm/dia]
         * y la cantidad total de agua de riego utilizada en una fecha
         * para regar una parcela o un cultivo [mm/dia] es que dicho
         * valor acumulado puede ser menor (negativo) o igual a cero y
         * dicha cantidad es (y debe serlo) mayor igual a cero. En el
         * comentario anterior se explica lo que representa que el
         * acumulado del deficit de agua por dia de dias previos a una
         * fecha [mm/dia] sea menor o igual a cero.
         * 
         * Con respecto a determinar la necesidad de agua de riego de
         * un cultivo en una fecha [mm/dia], si el resultado de la suma
         * entre el acumulado del deficit de agua por dia de dias previos
         * a una fecha y la cantidad total de agua de riego utilizada en
         * una fecha para regar un cultivo es estrictamente menor a cero
         * significa que hay una cantidad acumulada de agua evaporada de
         * dias previos a una fecha [mm/dia] que NO fue cubierta (satisfecha).
         * Esta cantidad representa la necesidad de agua de riego de un
         * cultivo en una fecha [mm/dia] y se la calcula como el valor
         * absoluto de la suma entre el acumulado del deficit de agua
         * por dia de dias previos a una fecha [mm/dia] y la cantidad
         * total de agua de riego utilizada en una fecha para regar un
         * cultivo [mm/dia].
         */
        if ((accumulatedWaterDeficitPerDay + totalIrrigationWaterGivenDate) < 0.0) {
            return Math.abs(accumulatedWaterDeficitPerDay + totalIrrigationWaterGivenDate);
        }

        /*
         * Con respecto a determinar la necesidad de agua de riego de
         * un cultivo en una fecha [mm/dia], si el resultado de la suma
         * entre el acumulado del deficit de agua por dia de dias previos
         * a una fecha [mm/dia] y la cantidad total de agua de riego
         * utilizada en una fecha para regar un cultivo [mm/dia] es
         * mayor o igual a 0 significa que la cantidad acumulada de agua
         * evaporada de dias previos a una fecha [mm/dia] fue totalmente
         * cubierta (satisfecha). Por lo tanto, la necesidad de agua de
         * riego de un cultivo en una fecha [mm/dia] es cero, si este
         * metodo se invoca con registros climaticos y registros de riego
         * previos a una fecha pertenecientes a una parcela que tiene un
         * cultivo sembrado en una fecha. En caso contrario, el valor
         * devuelto por este metodo representa el acumulado del deficit
         * de agua por dia de una parcela en una fecha [mm/dia].
         */
        return 0.0;
    }

}