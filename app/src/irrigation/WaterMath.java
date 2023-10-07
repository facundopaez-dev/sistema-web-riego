package irrigation;

import java.util.Calendar;
import java.util.Collection;
import java.lang.Math;
import model.ClimateRecord;
import model.IrrigationRecord;
import util.UtilDate;

public class WaterMath {

  /*
   * El metodo constructor tiene el modificador de acceso 'private'
   * para que ningun programador trate de instanciar esta clase
   * desde afuera, ya que todos los metodos publicos de la misma
   * son estaticos, con lo cual no se requiere una instancia de
   * esta clase para invocar a sus metodos publicos
   */
  private WaterMath() {

  }

  /**
   * Calcula la necesidad de agua de riego de un cultivo en una
   * fecha [mm/dia] dada utilizando la cantidad total de agua de
   * riego de una fecha dada, una coleccion de registros climaticos
   * y una coleccion de registros de riego, siendo todos ellos
   * previos a una fecha dada y pertenecientes a una misma parcela.
   * 
   * La fecha dada puede ser la fecha actual (es decir, hoy),
   * una fecha futura (es decir, posterior a la fecha actual)
   * o una fecha pasada (es decir, anterior a la fecha actual).
   * No tiene sentido que la fecha dada sea del pasado si lo
   * que se busca es determinar la necesidad de agua de riego
   * de un cultivo en la fecha actual o en una fecha posterior
   * a la fecha actual.
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
   * @param totalIrrigationWaterGivenDate
   * @param previousClimateRecords
   * @param previousIrrigationRecords
   * @return double que representa la necesidad de agua de
   * riego de un cultivo en una fecha dada [mm/dia]
   */
  public static double calculateIrrigationWaterNeed(double totalIrrigationWaterGivenDate, Collection<ClimateRecord> previousClimateRecords,
      Collection<IrrigationRecord> previousIrrigationRecords) {
    /*
     * El deficit (falta) acumulado de agua [mm/dia] de dias
     * previos a una fecha dada es la cantidad acumulada de
     * agua evaporada en dias previos a una fecha dada que
     * no fue cubierta (satisfecha).
     * 
     * Si el deficit acumulado de agua de dias previos a una
     * fecha dada es igual a 0, significa que la cantidad de
     * agua evaporada en dias previos a una fecha dada fue cubierta
     * (satisfecha), por lo tanto, NO hay una cantidad de agua
     * evaporada que se deba reponer (satisfacer) mediante el
     * riego en una fecha dada.
     * 
     * En cambio, si el deficit acumulado de agua de dias previos
     * a una fecha dada es menor a 0, significa que la cantidad
     * de agua evaporada en dias previos a una fecha dada NO fue
     * cubierta (satisfecha), por lo tanto, hay una cantidad
     * evaporada que se debe reponer mediante el riego en una
     * fecha dada.
     * 
     * Hay que tener en cuenta que el metodo calculateIrrigationWaterNeed
     * sobrecargado con la coleccion de registros climaticos y
     * la coleccion de registros de riego retorna un double
     * igual a cero o un double mayor a cero. El motivo por el
     * cual retorna un double mayor a cero en lugar de un double
     * menor a cero es que calcula el valor absoluto del deficit
     * acumulado de agua de dias previos a una fecha dada. En
     * consecuencia, cuando el deficit acumulado de agua de
     * dias previos a una fecha dada es menor a cero (negativo)
     * el metodo sobrecargado calculateIrrigationWaterNeed retorna
     * un deficit acumulado de dias previos a una fecha dada
     * positivo (mayor a cero).
     * 
     * Por lo tanto, un deficit acumulado de agua de dias previos
     * a una fecha dada positivo representa que la cantidad de agua
     * evaporada en dias previos a una fecha dada NO fue cubierta
     * (satisfecha), con lo cual hay una cantidad de agua evaporada
     * que se debe reponer mediante el riego en una fecha dada.
     */
    double accumulatedDeficit = calculateIrrigationWaterNeed(previousClimateRecords, previousIrrigationRecords);

    /*
     * Si la cantidad total de agua de riego de una fecha dada
     * [mm/dia] es mayor o igual al deficit (falta) acumulado
     * de agua [mm/dia] de dias previos a una fecha dada, la
     * necesidad de agua de riego de un cultivo en una fecha
     * dada es 0 [mm/dia]
     */
    if (totalIrrigationWaterGivenDate >= accumulatedDeficit) {
      return 0.0;
    }

    /*
     * Si el deficit (falta) acumulado de agua [mm/dia] de dias
     * previos a una fecha dada es estrictamente mayor a la cantidad
     * total de agua de riego de una fecha dada [mm/dia], la necesidad
     * de agua de riego de un cultivo en una fecha dada [mm/dia] se
     * calcula como la diferencia entre estas dos variables
     */
    return limitToTwoDecimalPlaces(accumulatedDeficit - totalIrrigationWaterGivenDate);
  }

  /**
   * Este metodo calcula la necesidad de agua de riego de un cultivo
   * en una fecha dada, la cual puede ser la fecha actual (es decir,
   * hoy) o una fecha posterior a la fecha actual. Esta fecha tambien
   * puede ser una fecha del pasado (es decir, anterior a la fecha
   * actual), pero esto no tiene sentido si lo que se busca es
   * determinar la necesidad de agua de riego de un cultivo en la
   * fecha actual o en una fecha posterior a la fecha actual.
   * 
   * La fecha para la que se calcula la necesidad de agua de riego
   * de un cultivo esta determinada por los registros climaticos y
   * los registros de riego que se seleccionan como previos a una
   * fecha dada, siendo ambos grupos de registros pertenecientes a
   * una parcela dada.
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
   * @param previousClimateRecords
   * @param previousIrrigationRecords
   * @return double que representa la necesidad de agua de riego
   * de un cultivo en una fecha dada [mm/dia] calculada con una
   * coleccion de registros climaticos y una coleccion de registros
   * de riego, siendo todos ellos previos a una fecha dada y pertenecientes
   * a una misma parcela que tiene un cultivo sembrado y en desarrollo
   * en una fecha dada
   */
  public static double calculateIrrigationWaterNeed(Collection<ClimateRecord> previousClimateRecords, Collection<IrrigationRecord> previousIrrigationRecords) {
    double deficitPerDay = 0.0;
    double accumulatedDeficit = 0.0;

    /*
     * Calcula por dia la diferencia entre la cantidad de agua provista
     * (lluvia o riego, o lluvia mas riego) y la cantidad de agua evaporada
     * mediante la formula (H2O - ETc) si la ETc > 0. Si la ETc = 0,
     * calcula esta diferencia mediante la formula (H2O - ETo). La ETo,
     * la ETc y el H2O (lluvia o riego, o lluvia mas riego) estan medidas
     * en mm/dia. Por lo tanto, la diferencia entre la cantidad de agua
     * provista y la cantidad de agua evaporada esta medida en mm/dia.
     * 
     * Si la diferencia entre la cantidad de agua provista [mm/dia] y la
     * cantidad de agua evaporada [mm/dia] (dada por la ETc, o la ETo si
     * la ETc = 0):
     * - es menor a cero significa que hay deficit (falta) de agua [mm/dia]
     * para satisfacer la cantidad de agua evaporada.
     * - es igual a cero significa que la cantidad de agua evaporada fue
     * satisfecha.
     * - es mayor a cero significa que la cantidad de agua evaporada fue
     * cubierta (satisfecha) y que hay escurrimiento de agua. El resultado
     * de esta diferencia es la cantidad de agua [mm/dia] que se escurre.
     */
    for (ClimateRecord currentClimateRecord : previousClimateRecords) {
      /*
       * Calcula el deficit (falta) de agua por dia [mm/dia] en una
       * parcela en una fecha porque un registro climatico y un registro
       * de riego pertenecen a una parcela y tienen una fecha (dia).
       * 
       * Si se invoca este metodo para una parcela que tiene un cultivo
       * sembrado y en desarrollo, el deficit de agua por dia [mm/dia]
       * calculado es el deficit de agua por dia [mm/dia] de un cultivo
       * en una fecha.
       */
      deficitPerDay = calculateDeficitPerDay(currentClimateRecord, previousIrrigationRecords);

      /*
       * Si la diferencia [mm/dia] entre la cantidad de agua provista
       * (lluvia o riego, o lluvia mas riego) [mm/dia] y la cantidad
       * de agua evaporada [mm/dia] (dada por la ETc, o la ETo si la
       * ETc = 0) es menor a cero significa que toda o parte de la
       * cantidad de agua evaporada no fue cubierta (satisfecha). A
       * esto se lo denomina deficit (falta) de agua para satisfacer
       * la cantidad de agua evaporada. Por lo tanto, se acumula el
       * deficit de agua por dia para determinar la necesidad de agua
       * de riego de un cultivo en una fecha dada.
       */
      if (deficitPerDay < 0) {
        accumulatedDeficit = accumulatedDeficit + deficitPerDay;
      }

      /*
       * Si la diferencia [mm/dia] entre la cantidad de agua provista
       * (lluvia o riego, o lluvia mas riego) [mm/dia] y la cantidad
       * de agua evaporada [mm/dia] (dada por la ETc, o la ETo si la
       * ETc = 0) es mayor a cero significa que la cantidad de agua
       * evaporada en un dia previo a una fecha dada, fue totalmente
       * cubierta y que hay una cantidad extra de agua [mm/dia].
       * 
       * Si el deficit acumulado de agua [mm/dia] es menor a cero
       * significa que la cantidad de agua evaporada en dias previos
       * a una fecha dada no fue cubierta (satisfecha). Esta condicion
       * representa la situacion en la que hay lugar en el suelo para
       * almacenar agua.
       * 
       * Si ambas condiciones ocurren al mismo tiempo significa que
       * la cantidad extra de agua, resultante de la diferencia entre
       * la cantidad de agua provista (lluvia o riego, o lluvia mas
       * riego) de un dia previo a una fecha dada y la cantidad de
       * agua evaporada (dada por la ETc, o la ETo si la ETc = 0) de
       * un dia previo a una fecha dada, se almacena en el suelo, ya
       * que este tiene lugar para almacenar mas agua, lo cual se
       * debe a que hay un deficit acumulado de agua.
       */
      if (deficitPerDay > 0 && accumulatedDeficit < 0) {
        accumulatedDeficit = accumulatedDeficit + deficitPerDay;

        /*
         * Si el deficit acumulado de agua [mm/dia] despues de sumarle
         * una cantidad extra de agua [mm/dia], es mayor a cero, significa
         * que el deficit acumulado de agua fue totalmente cubierto (satisfecho).
         * Es decir, se satisfizo la cantidad acumulada de agua evaporada
         * en dias previos a una fecha dada. Por lo tanto, ya no hay una
         * cantidad de agua evaporada que cubrir (satisfacer). En consecuencia,
         * el deficit acumulado de agua es 0.
         */
        if (accumulatedDeficit > 0) {
          accumulatedDeficit = 0;
        }

      }

    } // End for

    /*
     * El deficit (falta) acumulado [mm/dia] de agua representa la necesidad
     * de agua de riego [mm/dia] de un cultivo en una fecha dada. El objetivo
     * es determinar la cantidad de agua [mm/dia] que se debe utilizar en una
     * fecha dada para regar un cultivo actualmente en desarrollo. Para
     * determinar esta cantidad se acumula la cantidad de agua que falto para
     * cubrir la cantidad de agua evaporada (dada por la ETc [mm/dia], o la ETo
     * [mm/dia] si la ETc = 0) de un conjunto de dias previos a una fecha dada.
     * Esto se hace asi porque el objetivo es regar un cultivo a reposicion en
     * una fecha dada (*), la cual puede ser la fecha actual (hoy) o una fecha
     * posterior a la fecha actual. Es decir, el objetivo es regar en el dia o
     * en la fecha actual (hoy), o en un dia o fecha posterior al dia o fecha
     * actual, lo que falto regar en los dias previos a la fecha en la que se
     * quiere determinar la necesidad de agua de riego de un cultivo actualmente
     * en desarrollo.
     * 
     * (*) Esta fecha puede ser una fecha del pasado (es decir, anterior a la
     * fecha actual), pero esto no tiene sentido si lo que se busca es determinar
     * la necesidad de agua de riego de un cultivo en la fecha actual (es decir,
     * hoy) o en una fecha posterior a la fecha actual.
     */
    return Math.abs(limitToTwoDecimalPlaces(accumulatedDeficit));
  }

  /**
   * Calcula el deficit (falta) de agua por dia [mm/dia] en una parcela
   * en una fecha. El motivo por el cual calcula el deficit de agua por
   * dia [mm/dia] en una parcela en una fecha es que un registro climatico
   * y un registro de riego pertenecen a una parcela y tienen una fecha
   * (dia).
   * 
   * Si este metodo es invocado para una parcela que tiene un cultivo
   * sembrado y en desarrollo, el deficit (falta) de agua por dia [mm/dia]
   * calculado en una fecha sera el deficit de agua por dia [mm/dia] de
   * un cultivo en una fecha.
   * 
   * @param climateRecord
   * @param irrigationRecords
   * @return double que representa el deficit (falta) de agua por dia
   * [mm/dia] en una parcela en una fecha o de un cultivo en una fecha
   * en caso de que se invoque este metodo con una parcela que tiene
   * un cultivo sembrado y en desarrollo
   */
  public static double calculateDeficitPerDay(ClimateRecord climateRecord, Collection<IrrigationRecord> irrigationRecords) {
    double deficitPerDay = 0.0;

    /*
     * Obtiene la cantidad total de agua de riego utilizada en una
     * parcela en una fecha. Si la parcela para la que se invoca
     * este metodo tiene un cultivo sembrado y en desarrollo, el
     * valor devuelto por el mismo sera la cantidad total de agua
     * de riego utilizada en un cultivo (sembrado en una parcela)
     * en una fecha.
     * 
     * El motivo por el cual se habla de la parcela y se usa la
     * expresion "en una fecha" es que un registro de riego
     * pertenece a una parcela y tiene una fecha (dia).
     */
    double totalIrrigationWater = sumTotalAmountIrrigationWaterGivenDate(climateRecord.getDate(), irrigationRecords);

    /*
     * Cuando una parcela NO tuvo un cultivo sembrado en una fecha
     * dada, la ETc [mm/dia] del registro climatico correspondiente
     * a dicha fecha y perteneciente a una parcela dada, tiene el
     * valor 0.0. Esto se debe a que si NO hubo un cultivo sembrado
     * en una parcela en una fecha dada, NO es posible calcular la
     * ETc (evapotranspiracion del cultivo bajo condiciones estandar)
     * del mismo. Por lo tanto, en este caso se debe utilizar la ETo
     * [mm/dia] (evapotranspiracion del cultivo de referencia) para
     * calcular el deficit (falta) de agua por dia [mm/dia]. En caso
     * contrario, se debe utilizar la ETc.
     * 
     * El deficit de agua por dia [mm/dia] en una parcela en una fecha
     * (*) es la diferencia entre el agua provista (lluvia o riego, o
     * lluvia mas riego y viceversa) por dia [mm/dia] y el agua evaporada
     * por dia [mm/dia] (dada por la ETc [mm/dia] o la ETo [mm/dia]
     * si la ETc = 0) en una parcela en una fecha. Si el resultado de
     * esta diferencia es:
     * - es menor a cero significa que hay deficit (falta) de agua
     * [mm/dia], es decir, falta agua para cubrir (satisfacer) la
     * cantidad de agua evaporada.
     * - es igual a cero significa que la cantidad de agua evaporada
     * fue cubierta (satisfecha) con la cantidad exacta de agua, ni
     * agua de mas ni agua de menos.
     * - es mayor a cero significa que la cantidad de agua evaporada
     * fue cubierta (satisfecha) y que hay escurrimiento de agua. El
     * resultado de la diferencia es la cantidad de agua [mm/dia] que
     * se escurre.
     * 
     * (*) El motivo por el cual se usa la expresion "en una parcela
     * en una fecha" es que un registro climatico y un registro de
     * riego pertenecen a una parcela y tienen una fecha (dia).
     */
    if (climateRecord.getEtc() == 0.0) {
      deficitPerDay = (climateRecord.getPrecip() + totalIrrigationWater) - climateRecord.getEto();
    } else {
      deficitPerDay = (climateRecord.getPrecip() + totalIrrigationWater) - climateRecord.getEtc();
    }

    return deficitPerDay;
  }

  /**
   * @param givenDate
   * @param previousIrrigationRecords
   * @return double que representa la cantidad total de agua
   * de riego utilizada en una parcela en una fecha dada
   */
  private static double sumTotalAmountIrrigationWaterGivenDate(Calendar givenDate, Collection<IrrigationRecord> previousIrrigationRecords) {
    double totalIrrigationWater = 0.0;

    for (IrrigationRecord currentIrrigationRecord : previousIrrigationRecords) {

      /*
       * Acumula el agua de riego de todos los registros de riego
       * pertenecientes a una parcela que tienen la misma fecha.
       * De esta manera, se calcula la cantidad total de agua de
       * riego utilizada en una parcela en una fecha dada.
       */
      if (UtilDate.compareTo(currentIrrigationRecord.getDate(), givenDate) == 0) {
        totalIrrigationWater = totalIrrigationWater + currentIrrigationRecord.getIrrigationDone();
      }

    }

    return totalIrrigationWater;
  }

  /**
   * Limita la cantidad de decimales de un numero de punto
   * flotante a dos decimales
   * 
   * @param number
   * @return punto flotante con dos decimales
   */
  private static double limitToTwoDecimalPlaces(double number) {
    return (double) Math.round(number * 100d) / 100d;
  }

}
