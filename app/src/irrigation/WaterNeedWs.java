package irrigation;

import model.Crop;
import model.Parcel;
import model.Soil;
import model.ClimateRecord;
import model.IrrigationRecord;
import java.util.Collection;

/*
 * Esta clase representa (y contiene en forma de metodo) el algoritmo
 * del calculo, con datos de suelo o con suelo, de la necesidad de agua
 * de riego de un cultivo en una fecha [mm/dia]. La palabra "Ws" al final
 * del nombre de la clase proviene de la frase en ingles "With Soil"
 * que en español significa "con suelo".
 */
public class WaterNeedWs {

    /*
     * El metodo constructor tiene el modificador de acceso 'private'
     * para que ningun programador trate de instanciar esta clase
     * desde afuera, ya que todos los metodos publicos de la misma
     * son estaticos, con lo cual no se requiere una instancia de
     * esta clase para invocar a sus metodos publicos
     */
    private WaterNeedWs() {

    }

    /**
     * @param totalIrrigationWaterGivenDate
     * @param crop
     * @param soil
     * @param previousClimateRecords
     * @param previousIrrigationRecords
     * @return double que representa la necesidad de agua de riego
     * de un cultivo en una fecha [mm/dia]
     */
    public static double calculateIrrigationWaterNeed(double totalIrrigationWaterGivenDate, Crop crop, Soil soil,
            Collection<ClimateRecord> previousClimateRecords,
            Collection<IrrigationRecord> previousIrrigationRecords) {

        /*
         * Este valor representa la capacidad de almacenamiento de agua de
         * un suelo [mm]. Esto es la lamina total de agua disponible (dt).
         */
        double totalAmountWaterAvailable = WaterMath.calculateTotalAmountWaterAvailable(crop, soil);

        /*
         * El acumulado del deficit de agua por dia de dias previos a
         * una fecha [mm/dia] representa la cantidad acumulada de agua
         * evaporada de dias previos a una fecha de un cultivo. ¿Por
         * que de un cultivo? Porque este metodo en su firma tiene
         * como parametro una referencia de tipo Crop.
         */
        double accumulatedWaterDeficitPerDay = WaterMath.calculateAccumulatedWaterDeficitPerDay(previousClimateRecords, previousIrrigationRecords);

        /*
         * Si el acumulado del deficit de agua por dia de dias previos
         * a una fecha [mm/dia] es estrictamente menor a la capacidad
         * de almacenamiento de agua de un suelo [mm] negativa, significa
         * que el nivel de humedad [mm] de un suelo, que tiene un cultivo
         * sembrado, esta en el punto de marchitez permanente, en el
         * cual un cultivo no puede extraer agua del suelo y no puede
         * recuperarse de la perdida hidrica aunque la humedad ambiental
         * sea saturada. Esta situacion se representa con el retorno de
         * -1.
         */
        if (accumulatedWaterDeficitPerDay < (- totalAmountWaterAvailable)) {
            return -1;
        }

        /*
         * Si la suma entre el acumulado del deficit de agua por dia
         * de dias previos a una fecha [mm/dia] y la cantidad total de
         * agua de riego de una fecha [mm/dia] es mayor o igual a la
         * capacidad de almacenamiento de agua de un suelo [mm] negativa
         * y estrictamente menor a 0 [mm] (capacidad de campo de suelo),
         * significa que el nivel de humedad [mm] de un suelo, que tiene
         * un cultivo sembrado, es mayor o igual a la capacidad de
         * almacenamiento de agua de un suelo y es estrictamente menor
         * a la capacidad de campo de suelo. Por lo tanto, la necesidad
         * de agua de riego de un cultivo en una fecha [mm/dia] es el
         * valor absoluto de la suma entre el acumulado del deficit de
         * agua por dia de dias previos a una fecha [mm/dia] y la
         * cantidad total de agua de riego de una fecha [mm/dia]. El
         * motivo por el cual se realiza la suma de estos dos valores
         * y se aplica el valor absoluto a la misma es que el acumulado
         * del deficit de agua por dia de dias previos a una fecha es
         * menor o igual a 0. Por este motivo tambien se le asigna el
         * signo negativo (-) a la capacidad de almacenamiento de agua
         * de un suelo [mm] para realizar la comparacion entre este
         * valor y el resultado de la suma entre el acumulado del deficit
         * de agua por dia de dias previos a una fecha [mm/dia] y la
         * cantidad total de agua de riego de una fecha [mm/dia].
         * 
         * Que el acumulado del deficit de agua por dia de dias previos
         * a una fecha [mm/dia] sea igual a la capacidad de campo de
         * un suelo (0 [mm]), que tiene un cultivo sembrado, significa
         * que el suelo esta lleno de agua, y quiza anegado, ya que el
         * algoritmo que calcula dicho valor acumulado le asigna el
         * valor 0 cuando es estrictamente mayor a 0.
         * 
         * Que la suma entre el acumulado del deficit de agua por dia
         * de dias previos a una fecha [mm/dia] y la cantidad total de
         * agua de riego de una fecha [mm/dia] sea mayor o igual a la
         * capacidad de campo (0 [mm]) de un suelo, que tiene un cultivo
         * sembrado, significa que el suelo esta lleno de agua, y quiza
         * anegado, ya que, dependiendo de la cantidad de agua que se
         * le agrege a un suelo que esta lleno de agua, puede suceder
         * que este se anegue.
         * 
         * Si un suelo esta lleno de agua (esto es que el nivel de humedad
         * del suelo esta en capacidad de campo) y se le agrega mas
         * agua, la misma se escurre, pero, como se dijo anteriormente,
         * dependiendo de la cantidad de agua que se le agregue a un
         * suelo lleno de agua, puede suceder que este se anegue.
         */
        if ((accumulatedWaterDeficitPerDay + totalIrrigationWaterGivenDate) >= (- totalAmountWaterAvailable)
                && (accumulatedWaterDeficitPerDay + totalIrrigationWaterGivenDate) < 0) {
            return Math.abs(accumulatedWaterDeficitPerDay + totalIrrigationWaterGivenDate);
        }

        /*
         * Si el resultado de la suma entre el acumulado del deficit de
         * agua por dia de dias previos a una fecha [mm/dia] y la cantidad
         * total de agua de riego de una fecha [mm/dia] es mayor o igual
         * 0 (capacidad de campo) [mm], significa que el nivel de humedad
         * del suelo, que tiene un cultivo sembrado, esta en capacidad de
         * campo (0 [mm]), es decir, el suelo esta lleno de agua, y quiza
         * anegado (*). Por lo tanto, la necesidad de agua de riego de un
         * cultivo en una fecha es 0 [mm/dia].
         * 
         * (*) En el comentario anterior se explica el "y quiza anegado".
         */
        return 0.0;
    }

}