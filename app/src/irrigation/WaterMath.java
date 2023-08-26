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
   * son estaticos, con lo cual, no se requiere una instancia de
   * esta clase para invocar a sus metodos publicos
   */
  private WaterMath() {

  }

  /**
   * Calcula la necesidad de agua de riego [mm/dia] de un cultivo
   * en la fecha actual mediante la suma de las ETc de valuePastDaysReference
   * dias anteriores a la fecha actual, la suma del agua de lluvia
   * de valuePastDaysReference dias anteriores a la fecha actual,
   * la suma del agua de riego de valuePastDaysReference dias
   * anteriores a la fecha actual y la cantidad total de agua de
   * riego de la fecha actual.
   * 
   * El valor de valuePastDaysReference depende de cada usuario y
   * solo puede ser entre un limite minimo y un limite maximo,
   * los cuales estan definidos en la clase PastDaysReferenceServiceBean.
   * 
   * @param etcSummedPastDays
   * @param summedRainwaterPastDays
   * @param summedIrrigationWaterPastDays
   * @param totalIrrigationWaterCurrentDate
   * @return double que representa la necesidad de agua de riego
   * de un cultivo en la fecha actual
   */
  public static double calculateIrrigationWaterNeed(double etcSummedPastDays, double summedRainwaterPastDays,
      double summedIrrigationWaterPastDays, double totalIrrigationWaterCurrentDate) {
    /*
     * Si la suma del agua de lluvia de valuePastDaysReference
     * dias anteriores a la fecha actual es mayor o igual a la
     * suma de las ETc de valuePastDaysReference dias anteriores
     * a la fecha actual, la necesidad de agua de riego de un
     * cultivo en la fecha actual es 0
     */
    if (summedRainwaterPastDays >= etcSummedPastDays) {
      return 0.0;
    }

    /*
     * Si la suma del agua de riego de valuePastDaysReference
     * dias anteriores a la fecha actual es mayor o igual a la
     * suma de las ETc de valuePastDaysReference dias anteriores
     * a la fecha actual, la necesidad de agua de riego de un
     * cultivo en la fecha actual es 0
     */
    if (summedIrrigationWaterPastDays >= etcSummedPastDays) {
      return 0.0;
    }

    /*
     * Si la cantidad total de agua de riego de la fecha actual es
     * mayor o igual a la suma de las ETc de valuePastDaysReference
     * dias anteriores a la fecha actual, la necesidad de agua de
     * riego de un cultivo en la fecha actual es 0
     */
    if (totalIrrigationWaterCurrentDate >= etcSummedPastDays) {
      return 0.0;
    }

    /*
     * Si la suma entre la suma del agua de lluvia de valuePastDaysReference
     * dias anteriores a la fecha actual, la suma del agua de riego
     * de valuePastDaysReference dias anteriores a la fecha actual
     * y la cantidad total de agua de riego de la fecha actual es
     * mayor o igual a la suma de las ETc de valuePastDaysReference
     * dias anteriores a la fecha actual, la necesidad de agua de
     * riego de un cultivo en la fecha actual es 0
     */
    if ((summedRainwaterPastDays + summedIrrigationWaterPastDays + totalIrrigationWaterCurrentDate) >= etcSummedPastDays) {
      return 0.0;
    }

    /*
     * Si ninguna de las condiciones anteriores se cumple, la necesidad
     * de agua de riego de un cultivo en la fecha actual es mayor a 0 y
     * se calcula como la diferencia entre la suma de las ETc de
     * valuePastDaysReference dias anteriores a la fecha actual y la
     * suma entre la suma del agua de lluvia de valuePastDaysReference
     * dias anteriores a la fecha actual, la suma del agua de riego de
     * valuePastDaysReference dias anteriores a la fecha actual y la
     * cantidad total de agua de riego de la fecha actual
     */
    return limitToTwoDecimalPlaces(etcSummedPastDays - (summedRainwaterPastDays + summedIrrigationWaterPastDays + totalIrrigationWaterCurrentDate));
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
   * actual. En cambios, si se seleccionan los registros climaticos
   * y los registros de riego de una parcela dada previos a la
   * fecha actual + X dias, donde X > 0, la necesidad de agua de
   * riego de un cultivo calculada con estos registros corresponde
   * a la fecha actual + X dias.
   * 
   * @param previousClimateRecords
   * @param previousIrrigationRecords
   * @return double que representa la necesidad de agua de riego
   * de un cultivo en una fecha dada tomando como referencia los
   * registros climaticos previos a la fecha dada y los registros
   * de riego previos a la fecha dada, perteneciendo ambos grupos
   * de registros a una parcela que tiene un cultivo sembrado y en
   * desarrollo en la fecha dada
   */
  public static double calculateIrrigationWaterNeed(Collection<ClimateRecord> previousClimateRecords, Collection<IrrigationRecord> previousIrrigationRecords) {
    double accumulatedDeficit = 0.0;
    double differencePerDay = 0.0;
    double totalIrrigationWater = 0.0;

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
       * Obtiene la cantidad total de agua de riego utilizada en una
       * parcela en una fecha dada
       */
      totalIrrigationWater = sumTotalAmountIrrigationWaterGivenDate(currentClimateRecord.getDate(), previousIrrigationRecords);

      /*
       * Cuando una parcela NO tuvo un cultivo sembrado en una fecha
       * dada, la ETc [mm/dia] del registro climatico correspondiente
       * a dicha fecha y perteneciente a una parcela dada, tiene el
       * valor 0.0. Esto se debe a que si NO hubo un cultivo sembrado
       * en una parcela en una fecha dada, NO es posible calcular la
       * ETc (evapotranspiracion del cultivo bajo condiciones estandar)
       * del mismo. Por lo tanto, en este caso se debe utilizar la ETo
       * [mm/dia] (evapotranspiracion del cultivo de referencia) para
       * calcular la diferencia [mm/dia] entre la cantidad de agua
       * provista (lluvia o riego, o lluvia mas riego) y la cantidad
       * de agua evaporada en una fecha dada en una parcela dada. En
       * el caso contrario, se debe utilizar la ETc para calcular dicha
       * diferencia.
       * 
       * El motivo de la frase "parcela dada" se debe a que un registro
       * climatico pertenece a una parcela.
       */
      if (currentClimateRecord.getEtc() == 0.0) {
        differencePerDay = (currentClimateRecord.getPrecip() + totalIrrigationWater) - currentClimateRecord.getEto();
      } else {
        differencePerDay = (currentClimateRecord.getPrecip() + totalIrrigationWater) - currentClimateRecord.getEtc();
      }

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
      if (differencePerDay < 0) {
        accumulatedDeficit = accumulatedDeficit + differencePerDay;
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
      if (differencePerDay > 0 && accumulatedDeficit < 0) {
        accumulatedDeficit = accumulatedDeficit + differencePerDay;

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
