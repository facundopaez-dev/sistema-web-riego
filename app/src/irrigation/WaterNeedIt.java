package irrigation;

import model.Crop;
import model.Parcel;
import model.Soil;
import model.ClimateRecord;
import model.IrrigationRecord;
import java.util.Collection;

/*
 * Esta clase representa (y contiene en forma de metodo) el algoritmo
 * del calculo, con umbral de riego, de la necesidad de agua de riego
 * de un cultivo en una fecha [mm/dia]. La palabra "It" al final del
 * nombre de la clase proviene de la frase en ingles "Irrigation
 * Threshold" que en español significa "umbral de riego".
 */
public class WaterNeedIt {

    /*
     * El metodo constructor tiene el modificador de acceso 'private'
     * para que ningun programador trate de instanciar esta clase
     * desde afuera, ya que todos los metodos publicos de la misma
     * son estaticos, con lo cual no se requiere una instancia de
     * esta clase para invocar a sus metodos publicos
     */
    private WaterNeedIt() {

    }

    /**
     * @param totalIrrigationWaterGivenDate
     * @param etcGivenDate
     * @param crop
     * @param soil
     * @param previousClimateRecords
     * @param previousIrrigationRecords
     * @return double que representa la necesidad de agua de riego
     * de un cultivo en una fecha [mm/dia]
     */
    public static double calculateIrrigationWaterNeed(double totalIrrigationWaterGivenDate, double etcGivenDate,
            Crop crop, Soil soil, Collection<ClimateRecord> previousClimateRecords,
            Collection<IrrigationRecord> previousIrrigationRecords) {

        /*
         * Este valor representa la cantidad maxima de agua que puede
         * perder un suelo regado a capacidad de campo que tiene un
         * cultivo sembrado, medida en [mm]. Esto es la lamina optima
         * de riego (drop).
         */
        double optimalIrrigationLayer = calculateOptimalIrrigationLayer(etcGivenDate, crop, soil);

        /*
         * El acumulado del deficit de agua por dia de dias previos
         * a una fecha [mm/dia] representa la cantidad acumulada de
         * agua evaporada de dias previos a una fecha de un cultivo.
         * ¿Por que de un cultivo? Porque este metodo en su firma
         * tiene como parametro una referencia de tipo Crop.
         */
        double accumulatedWaterDeficitPerDay = WaterMath.calculateAccumulatedWaterDeficitPerDay(previousClimateRecords, previousIrrigationRecords);

        /*
         * Si la cantidad total de agua de riego de una fecha [mm/dia]
         * es igual a 0 (cero) y el acumulado del deficit de agua por
         * dia de dias previos a una fecha [mm/dia] es menor o igual
         * a la lamina de riego optima (drop) negativa [mm] de una fecha,
         * significa que en una fecha no se rego y que el nivel de
         * humedad [mm] del suelo, que tiene un cultivo sembrado, es
         * igual o menor a la lamina de riego optima (drop) negativa
         * [mm] de una fecha. Por lo tanto, la necesidad de agua de
         * riego de un cultivo en una fecha [mm/dia] es la lamina de
         * riego optima (drop) [mm] de una fecha.
         */
        if (totalIrrigationWaterGivenDate == 0.0 && accumulatedWaterDeficitPerDay <= (- optimalIrrigationLayer)) {
            return optimalIrrigationLayer;
        }

        /*
         * Si la cantidad total de agua de riego de una fecha [mm/dia]
         * es estrictamente mayor a cero y el acumulado del deficit
         * de agua por dia de dias previos a una fecha [mm/dia] es
         * menor o igual a la lamina de riego optima (drop) negativa
         * [mm] de una fecha, significa que en una fecha se rego y
         * que el nivel de humedad [mm] del suelo, que tiene un cultivo
         * sembrado, es igual o menor a la lamina de riego optima
         * (drop) negativa de una fecha. Por lo tanto, la necesidad
         * de agua de riego de un cultivo en una fecha [mm/dia] es
         * la diferencia entre la lamina de riego optima (drop) de
         * una fecha y la cantidad total de agua de riego de una
         * fecha, si la diferencia entre la segunda y la primera NO
         * es mayor o igual a cero (o es estrictamente menor a cero,
         * en otras palabras). En caso contrario, la necesidad de
         * agua de riego de un cultivo en una fecha es 0 [mm/dia].
         * 
         * Que la diferencia entre la cantidad total de agua de riego
         * de una fecha [mm/dia] y la lamina de riego optima (drop)
         * [mm] de una fecha sea mayor o igual a 0 (cero), significa
         * que el nivel de humedad del suelo, que tiene un cultivo
         * sembrado, esta en capacidad de campo (0 [mm]), es decir,
         * el suelo esta lleno de agua, y quiza anegado (*).
         * 
         * (*) En el siguiente comentario se explica el "y quiza
         * anegado".
         */
        if (totalIrrigationWaterGivenDate > 0.0 && accumulatedWaterDeficitPerDay <= (- optimalIrrigationLayer)) {
            return (totalIrrigationWaterGivenDate - optimalIrrigationLayer) >= 0 ? 0 : (optimalIrrigationLayer - totalIrrigationWaterGivenDate);
        }

        /*
         * Si el acumulado del deficit de agua por dia de dias
         * previos a una fecha [mm/dia] es estrictamente mayor a
         * la lamina de riego optima (drop) negativa [mm] de una
         * fecha y la suma entre el acumulado del deficit de agua
         * por dia de dias previos a una fecha y la cantidad total
         * de agua de riego de una fecha [mm/dia] es estrictamente
         * menor a 0 (cero), significa que el nivel de humedad del
         * suelo, que tiene un cultivo sembrado, es estrictamente
         * mayor a la lamina de riego optima (drop) negativa de una
         * fecha y es estrictamente menor a la capacidad de campo
         * del suelo (0 [mm]). Por lo tanto, la necesidad de agua
         * de riego de un cultivo en una fecha [mm/dia] es el valor
         * absoluto de la diferencia entre el acumulado del deficit
         * de agua por dia de dias previos a una fecha [mm/dia] y
         * la cantidad total de agua de riego de una fecha [mm/dia].
         * 
         * Que el acumulado del deficit de agua por dia de dias
         * previos a una fecha [mm/dia] sea igual a la capacidad
         * de campo (0 [mm]) de un suelo, que tiene un cultivo
         * sembrado, significa que el suelo esta lleno de agua,
         * y quiza anegado, ya que el algoritmo que calcula dicho
         * valor acumulado le asigna el valor 0 cuando es
         * estrictamente mayor a 0.
         * 
         * Que la suma entre el acumulado del deficit de agua por
         * dia de dias previos a una fecha [mm/dia] y la cantidad
         * total de agua de riego de una fecha [mm/dia] sea mayor
         * o igual a la capacidad de campo (0 [mm]) de un suelo,
         * que tiene un cultivo sembrado, significa que el suelo
         * esta lleno de agua, y quiza anegado, ya que, dependiendo
         * de la cantidad de agua que se le agrege a un suelo
         * que esta lleno de agua, puede suceder que este se anegue.
         * 
         * Si un suelo esta lleno de agua (esto es que el nivel
         * de humedad del suelo esta en capacidad de campo) y se
         * le agrega mas agua, la misma se escurre, pero, como
         * se dijo anteriormente, dependiendo de la cantidad de
         * agua que se le agregue a un suelo lleno de agua, puede
         * suceder que este se anegue.
         */
        if (accumulatedWaterDeficitPerDay > (- optimalIrrigationLayer) && accumulatedWaterDeficitPerDay + totalIrrigationWaterGivenDate < 0) {
            return Math.abs(accumulatedWaterDeficitPerDay + totalIrrigationWaterGivenDate);
        }

        /*
         * Si el resultado de la suma entre el acumulado del deficit
         * de agua por dia de dias previos a una fecha [mm/dia] y la
         * cantidad total de agua de riego de una fecha [mm/dia] es
         * mayor o igual 0 (capacidad de campo) [mm], significa que
         * el nivel de humedad del suelo, que tiene un cultivo sembrado,
         * esta en capacidad de campo (0 [mm]), es decir, el suelo
         * esta lleno de agua, y quiza anegado (*). Por lo tanto,
         * la necesidad de agua de riego de un cultivo en una fecha
         * es 0 [mm/dia].
         * 
         * (*) En el comentario anterior se explica el "y quiza anegado".
         */
        return 0.0;
    }

    /**
     * La formula ((Wc - Wm) / 100) * pea representa la capacidad de
     * almacenamiento de agua que tiene un suelo.
     * 
     * @param crop
     * @param soil
     * @return double que representa la cantidad de agua que puede
     * retener un suelo en el volumen determinado por los valores
     * de suelo (capacidad de campo, punto de marchitez permanente,
     * peso especifico aparente) y la profundidad de las raices de
     * un cultivo, medida en [mm]. Esto es la lamina total de agua
     * disponible (dt).
     */
    public static double calculateTotalAmountWaterAvailable(Crop crop, Soil soil) {
        // Capacidad de campo de un suelo
        double wc = soil.getFieldCapacity();

        // Punto de marchitez permanente de un suelo
        double wm = soil.getPermanentWiltingPoint();

        // Peso especifico aparente de un suelo
        double pea = soil.getApparentSpecificWeight();

        /*
         * La profundidad radicular promedio de un cultivo esta
         * medida en metros porque los limites del rango de la
         * profundidad radicular de un cultivo estan medidos
         * en metros. Debido a que la ETc (evapotranspiracion
         * del cultivo bajo condiciones estandar) esta medida
         * en mm/dia, se debe convertir la profundidad radicular
         * promedio de un cultivo de metros a milimetros, lo cual
         * se realiza multiplicandola por 1000.
         * 
         * La ETc, junto con el agua provista (lluvia o riego, o
         * lluvia mas riego y viceversa) [mm/dia], se utiliza para
         * calcular la necesidad de agua de riego de un cultivo en
         * una fecha [mm/dia].
         */
        double averageRoothDepth = calculateAverageRootDepth(crop) * 1000;

        return ((wc - wm) / 100) * pea * averageRoothDepth;
    }

    /**
     * La multiplicacion entre la lamina total de agua disponible (dt),
     * calculada por el metodo calculateTotalAmountWaterAvailable de
     * esta clase, y el factor de agotamiento (depletionFactor)
     * representa la cantidad maxima de agua que puede perder un suelo
     * regado a capacidad de campo, que tiene un cultivo sembrado, a
     * partir de la cual NO puede perder mas agua, sino que se debe
     * regar nuevamente. El resultado de esta multiplicacion esta medido
     * en milimetros y se lo puede asociar a la ETo (evapotranspiracion
     * del cultivo de referencia) o a la ETc (evapotranspiracion del
     * cultivo bajo condiciones estandar) para determinar la necesidad
     * de agua de riego de un cultivo en una fecha [mm/dia].
     * 
     * Por ejemplo, si el resultado de esta multiplicacion es 10 [mm]
     * significa que un suelo regado a capacidad de campo, que tiene
     * un cultivo sembrado, puede perder como maximo 10 [mm] de agua
     * y no mas. Por lo tanto, cuando el nivel de agua del suelo
     * descienda (perdida) a los 10 [mm] se debe regar nuevamente a
     * capacidad de campo.
     * 
     * Un valor de 0,50 para el factor de agotamiento (depletionFactor),
     * representada con la letra p en la formula de la lamina optima
     * de riego (drop), es utilizado comunmente para una gran variedad
     * de cultivos.
     * 
     * @param etcGivenDate
     * @param crop
     * @param soil
     * @return double que representa la cantidad maxima de agua que
     * puede perder un suelo regado a capacidad de campo que tiene
     * un cultivo sembrado, medida en [mm]. Esto es la lamina optima
     * de riego (drop).
     */
    public static double calculateOptimalIrrigationLayer(double etcGivenDate, Crop crop, Soil soil) {
        return calculateTotalAmountWaterAvailable(crop, soil) * adjustDepletionFactorToEtc(etcGivenDate, crop.getDepletionFactor());
    }

    /**
     * @param crop
     * @return double que representa el promedio de la
     * profundidad radicular de un cultivo
     */
    private static double calculateAverageRootDepth(Crop crop) {
        return (crop.getUpperLimitMaximumRootDepth() + crop.getLowerLimitMaximumRootDepth()) / 2;
    }

    /**
     * Esta formula fue tomada de la pagina 163 del libro
     * "Evapotranspiracion del cultivo, estudio FAO riego y
     * drenaje".
     * 
     * @param etcGivenDate
     * @param cropDepletionFactor
     * @return double que representa un factor de agotamiento
     * (p) ajustado a una ETc de una fecha [mm/dia]
     */
    private static double adjustDepletionFactorToEtc(double etcGivenDate, double cropDepletionFactor) {
        return (cropDepletionFactor + 0.04 * (5 - etcGivenDate));
    }

}