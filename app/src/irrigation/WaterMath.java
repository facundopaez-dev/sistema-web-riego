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
   * para una parcela que NO tiene un cultivo sembrado. En caso
   * de que ocurra esto, el valor devuelto por el mismo sera el
   * deficit acumulado de agua por dia de una parcela en una
   * fecha [mm/dia]. En caso de que se invoque este metodo para
   * una parcela que tiene un cultivo sembrado y en desarrollo
   * en una fecha, el valor devuelto por el mismo sera el
   * deficit acumulado de agua por dia de un cultivo en una
   * fecha [mm/dia], el cual representa la necesidad de agua
   * de riego de un cultivo en una fecha.
   * 
   * @param totalIrrigationWaterGivenDate
   * @param previousClimateRecords
   * @param previousIrrigationRecords
   * @return double que representa la necesidad de agua de
   * riego de un cultivo en una fecha [mm/dia], si se invoca
   * este metodo para una parcela que tiene un cultivo sembrado
   * y en desarrollo. En caso contrario, double que representa
   * el deficit acumulado de agua por dia de una parcela en una
   * fecha [mm/dia].
   */
  public static double calculateIrrigationWaterNeed(double totalIrrigationWaterGivenDate, Collection<ClimateRecord> previousClimateRecords,
      Collection<IrrigationRecord> previousIrrigationRecords) {
    /*
     * El deficit (falta) acumulado de agua por dia [mm/dia] de
     * dias previos a una fecha es la cantidad acumulada de agua
     * evaporada en dias previos a una fecha que no fue cubierta
     * (satisfecha).
     * 
     * Si el deficit acumulado de agua por dia de dias previos a una
     * fecha es igual a 0, significa que la cantidad de agua evaporada
     * en dias previos a una fecha fue cubierta (satisfecha), por lo
     * tanto, NO hay una cantidad de agua evaporada que se deba
     * reponer (satisfacer) mediante el riego en una fecha.
     * 
     * En cambio, si el deficit acumulado de agua por dia de dias
     * previos a una fecha es menor a 0, significa que la cantidad
     * de agua evaporada en dias previos a una fecha NO fue cubierta
     * (satisfecha), por lo tanto, hay una cantidad evaporada que
     * se debe reponer mediante el riego en una fecha.
     * 
     * Hay que tener en cuenta que el metodo calculateIrrigationWaterNeed
     * sobrecargado con la coleccion de registros climaticos y
     * la coleccion de registros de riego retorna un double
     * igual a cero o un double mayor a cero. El motivo por el
     * cual retorna un double mayor a cero en lugar de un double
     * menor a cero es que calcula el valor absoluto del deficit
     * acumulado de agua por dia [mm/dia] de dias previos a una
     * fecha. En consecuencia, cuando el deficit acumulado de agua
     * por dia de dias previos a una fecha es menor a cero (negativo),
     * el metodo sobrecargado calculateIrrigationWaterNeed retorna un
     * deficit acumulado de agua por dia de dias previos a una fecha
     * positivo (mayor a cero).
     * 
     * Por lo tanto, un deficit acumulado de agua por dia de dias
     * previos a una fecha positivo representa que la cantidad de
     * agua evaporada en dias previos a una fecha NO fue cubierta
     * (satisfecha), con lo cual hay una cantidad de agua evaporada
     * que se debe reponer mediante el riego en una fecha.
     */
    double accumulatedDeficit = calculateIrrigationWaterNeed(previousClimateRecords, previousIrrigationRecords);

    /*
     * Si la cantidad total de agua de riego de una fecha [mm/dia]
     * es mayor o igual al deficit (falta) acumulado de agua por
     * dia [mm/dia] de dias previos a una fecha, la necesidad de
     * agua de riego de un cultivo en una fecha es 0 [mm/dia]
     */
    if (totalIrrigationWaterGivenDate >= accumulatedDeficit) {
      return 0.0;
    }

    /*
     * Si el deficit (falta) acumulado de agua por dia [mm/dia] de
     * dias previos a una fecha es estrictamente mayor a la cantidad
     * total de agua de riego de una fecha [mm/dia], la necesidad
     * de agua de riego de un cultivo en una fecha [mm/dia] se
     * calcula como la diferencia entre estas dos variables
     */
    return accumulatedDeficit - totalIrrigationWaterGivenDate;
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
   * Se debe tener en cuenta que este metodo puede ser invocado
   * para una parcela que NO tiene un cultivo sembrado. En caso
   * de que ocurra esto, el valor devuelto por el mismo sera el
   * deficit acumulado de agua por dia de una parcela en una
   * fecha [mm/dia]. En caso de que se invoque este metodo para
   * una parcela que tiene un cultivo sembrado y en desarrollo
   * en una fecha, el valor devuelto por el mismo sera el
   * deficit acumulado de agua por dia de un cultivo en una
   * fecha [mm/dia], el cual representa la necesidad de agua
   * de riego de un cultivo en una fecha.
   * 
   * @param climateRecords
   * @param irrigationRecords
   * @return double que representa la necesidad de agua de riego
   * de un cultivo en una fecha [mm/dia] calculada con un conjunto
   * de registros climaticos y un conjunto de registros de riego,
   * siendo todos ellos previos a una fecha y pertenecientes a una
   * misma parcela, si se invoca este metodo para una parcela que
   * tiene un cultivo sembrado y en desarrollo en una fecha. En
   * caso contrario, double que representa el deficit acumulado
   * de agua por dia de una parcela en una fecha [mm/dia].
   */
  public static double calculateIrrigationWaterNeed(Collection<ClimateRecord> climateRecords, Collection<IrrigationRecord> irrigationRecords) {
    double deficitPerDay = 0.0;
    double accumulatedDeficit = 0.0;

    /*
     * Calcula el deficit (falta) acumulado de agua por dia [mm/dia]
     * en una parcela en una fecha mediante el deficit de agua por
     * dia [mm/dia] en una parcela en una fecha. El motivo por el
     * cual se menciona la palabra "parcela" y se utiliza la expresion
     * "en una fecha" es que el deficit de agua por dia se calcula
     * en base a un registro climatico y una coleccion de registros
     * de riego, y un registro climatico y un registro de riego
     * pertenecen a una parcela y tienen una fecha (dia).
     * 
     * Si el metodo calculateIrrigationWaterNeed sobrecargado con una
     * coleccion de registros climaticos y una coleccion de registros
     * de riego es invocado para una parcela que tiene un cultivo
     * sembrado y en desarrollo en una fecha, el valor devuelto por
     * el mismo sera el deficit acumulado de agua por dia de un cultivo
     * en una fecha [mm/dia]. El ultimo comentario de este metodo
     * explica que el deficit acumulado de agua por dia de un cultivo
     * en una fecha [mm/dia] representa la necesidad de agua de riego
     * del mismo en una fecha [mm/dia].
     */
    for (ClimateRecord currentClimateRecord : climateRecords) {
      /*
       * Calcula el deficit (falta) de agua por dia [mm/dia] en una
       * parcela en una fecha porque un registro climatico y un registro
       * de riego pertenecen a una parcela y tienen una fecha (dia).
       * 
       * Si se invoca este metodo para una parcela que tiene un cultivo
       * sembrado y en desarrollo en una fecha, el deficit de agua por dia
       * [mm/dia] calculado es el deficit de agua por dia [mm/dia] de un
       * cultivo en una fecha.
       */
      deficitPerDay = calculateDeficitPerDay(currentClimateRecord, irrigationRecords);

      /*
       * Calcula el deficit (falta) acumulado de agua por dia [mm/dia]
       * en una parcela en una fecha porque para ello utiliza el deficit
       * de agua por dia, el cual es calculado en base a un registro
       * climatico y una coleccion de registros de riego, y un registro
       * climatico y un registro de riego pertenecen a una parcela y
       * tienen una fecha (dia).
       * 
       * Si se invoca este metodo para una parcela que tiene un cultivo
       * sembrado y en desarrollo en una fecha, el deficit acumulado
       * de agua por dia [mm/dia] es el deficit acumulado de agua por
       * dia [mm/dia] de un cultivo en una fecha.
       */
      accumulatedDeficit = calculateAccumulatedDeficit(deficitPerDay, accumulatedDeficit);
    }

    /*
     * El deficit (falta) acumulado de agua por dia [mm/dia] representa
     * la necesidad de agua de riego [mm/dia] de un cultivo en una fecha.
     * El objetivo es determinar la cantidad de agua [mm/dia] que se debe
     * utilizar en una fecha para regar un cultivo. Para determinar esta
     * cantidad se acumula la cantidad de agua que falto para cubrir la
     * cantidad de agua evaporada de un conjunto de dias previos a una
     * fecha. Esto es el deficit acumulado de agua por dia [mm/dia].
     * El motivo por el cual se realiza este calculo es que se busca
     * regar un cultivo a reposicion en una fecha (*), la cual puede ser
     * la fecha actual (es decir, hoy) o una fecha posterior a la fecha
     * actual. Es decir, el objetivo es regar en el dia o fecha actual
     * (hoy), o en un dia o fecha posterior al dia o fecha actual, lo que
     * falto regar en los dias previos a la fecha en la que se quiere
     * determinar la necesidad de agua de riego de un cultivo sembrado
     * y en desarrollo.
     * 
     * (*) Esta fecha puede ser una fecha del pasado (es decir, anterior
     * a la fecha actual), pero esto no tiene sentido si lo que se busca
     * es determinar la necesidad de agua de riego de un cultivo en la
     * fecha actual (es decir, hoy) o en una fecha posterior a la fecha
     * actual.
     */
    return Math.abs(accumulatedDeficit);
  }

  /**
   * Calcula el deficit (falta) de agua por dia [mm/dia] en una parcela
   * en una fecha. El motivo por el cual calcula el deficit de agua por
   * dia [mm/dia] en una parcela en una fecha es que un registro climatico
   * y un registro de riego pertenecen a una parcela y tienen una fecha
   * (dia).
   * 
   * Si este metodo es invocado para una parcela que tiene un cultivo
   * sembrado y en desarrollo en una fecha, el deficit (falta) de agua
   * por dia [mm/dia] calculado en una fecha sera el deficit de agua
   * por dia [mm/dia] de un cultivo en una fecha.
   * 
   * @param climateRecord
   * @param irrigationRecords
   * @return double que representa el deficit (falta) de agua por dia
   * [mm/dia] en una parcela en una fecha o de un cultivo en una fecha
   * en caso de que se invoque este metodo con una parcela que tiene
   * un cultivo sembrado y en desarrollo en una fecha
   */
  public static double calculateDeficitPerDay(ClimateRecord climateRecord, Collection<IrrigationRecord> irrigationRecords) {
    double deficitPerDay = 0.0;

    /*
     * Obtiene la cantidad total de agua de riego utilizada en una
     * parcela en una fecha. Si la parcela para la que se invoca
     * este metodo tiene un cultivo sembrado y en desarrollo en
     * una fecha, el valor devuelto por el mismo sera la cantidad
     * total de agua de riego utilizada en un cultivo (sembrado en
     * una parcela) en una fecha.
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
     * Solo para aclarar: estas tres condiciones ocurren en una parcela
     * en una fecha.
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
   * Calcula el deficit (falta) acumulado de agua por dia en una
   * parcela en una fecha [mm/dia] porque para ello utiliza el
   * deficit de agua por dia [mm/dia], el cual es calculado en
   * base a un registro climatico y una coleccion de registros
   * de riego, y un registro climatico y un registro de riego
   * pertenecen a una parcela y tienen una fecha (dia).
   * 
   * Si este metodo es invocado para una parcela que tiene un
   * cultivo sembrado y en desarrollo en una fecha, el deficit
   * acumulado de agua por dia [mm/dia] sera el deficit acumulado
   * de agua por dia [mm/dia] de un cultivo en una fecha.
   * 
   * @param deficitPerDay
   * @param accumulatedDeficit
   * @return double que representa el deficit (falta) acumulado
   * de agua por dia en una parcela en una fecha [mm/dia] o de
   * un cultivo en una fecha en caso de que se invoque este metodo
   * para una parcela que tiene un cultivo sembrado y en desarrollo
   * en una fecha
   */
  public static double calculateAccumulatedDeficit(double deficitPerDay, double accumulatedDeficit) {
    /*
     * El deficit de agua por dia [mm/dia] en una parcela en una
     * fecha (*) es la diferencia entre el agua provista (lluvia
     * o riego, o lluvia mas riego y viceversa) por dia [mm/dia]
     * y el agua evaporada por dia [mm/dia] (dada por la ETc [mm/dia]
     * o la ETo [mm/dia] si la ETc = 0) en una parcela en una fecha.
     * Si el resultado de esta diferencia es menor a cero significa
     * que toda o parte de la cantidad de agua evaporada en una
     * parcela en una fecha no fue cubierta (satisfecha). Este valor
     * es acumulado porque es necesario para determinar la necesidad
     * de agua de riego de un cultivo en una fecha, en caso de que
     * este metodo sea invocado para una parcela que tiene un cultivo
     * sembrado y en desarrollo en una fecha.
     * 
     * (*) El motivo por el cual se usa la expresion "en una parcela
     * en una fecha" es que un registro climatico y un registro de
     * riego pertenecen a una parcela y tienen una fecha (dia).
     */
    if (deficitPerDay < 0) {
      accumulatedDeficit = accumulatedDeficit + deficitPerDay;
    }

    /*
     * Si el deficit de agua por dia en una parcela en una fecha
     * [mm/dia] (definicion en el comentario anterior) es mayor a
     * cero significa que en una fecha la cantidad de agua evaporada
     * fue totalmente cubierta (satisfecha) y que hubo una cantidad
     * extra de agua [mm/dia].
     * 
     * Si el deficit acumulado de agua por dia en una parcela en
     * una fecha [mm/dia] es menor a cero significa que la cantidad
     * de agua evaporada en una parcela en un conjunto de dias
     * previos al dia correspondiente del deficit de agua por dia,
     * no fue cubierta (satisfecha). Esta condicion representa la
     * situacion en la que hay lugar en el suelo para almacenar
     * agua.
     * 
     * Si ambas condiciones ocurren al mismo tiempo significan
     * las siguientes cosas:
     * - que en el dia correspondiente al deficit de agua por
     * dia, hay lugar en el suelo para almacenar agua, ya que
     * un deficit acumulado de agua por dia menor a cero indica
     * que la cantidad acumulado de agua evaporada de un conjunto
     * de dias previos al dia correspondiente del deficit de agua
     * por dia, no fue cubierta (satisfecha). El deficit acumulado
     * de agua por dia es la sumatoria del deficit de agua por
     * dia de un conjunto de dias.
     * - que la cantidad de agua extra del dia correspondiente
     * al deficit de agua por dia se almacena en el suelo, ya
     * que este tiene lugar para almacenar mas agua.
     */
    if (deficitPerDay > 0 && accumulatedDeficit < 0) {
      accumulatedDeficit = accumulatedDeficit + deficitPerDay;

      /*
       * Si el deficit acumulado de agua por dia [mm/dia] despues de
       * sumarle una cantidad extra de agua [mm/dia], es mayor a cero,
       * significa que el deficit acumulado de agua por dia fue
       * totalmente (satisfecho). Es decir, se satisfizo la cantidad
       * acumulada de agua evaporada de un conjunto de dias previos
       * al dia correspondiente del deficit de agua por dia. Por lo
       * tanto, en el dia del deficit de agua por dia no hay una
       * cantidad de agua evaporada que cubrir (satisfacer). En
       * consecuencia, el deficit acumulado de agua por dia en una
       * parcela en una fecha o de un cultivo en una fecha en caso
       * de que es invoque este metodo para una parcela que tiene un
       * cultivo sembrado y en desarrollo en una fecha, es 0.
       */
      if (accumulatedDeficit > 0) {
        accumulatedDeficit = 0;
      }

    }

    return accumulatedDeficit;
  }

  /**
   * Calcula la cantidad total de agua de riego utilizada en
   * una parcela en una fecha [mm/dia] porque un registro de
   * riego pertenece a una parcela y tiene una fecha (dia).
   * En caso de que este metodo se invoque para una parcela
   * que tiene un cultivo sembrado y en desarrollo en una
   * fecha, la cantidad total de agua de riego calculada sera
   * la cantidad total de agua de riego utilizada en un cultivo
   * (sembrado en una parcela, obviamente) en una fecha [mm/dia].
   * 
   * @param date
   * @param irrigationRecords
   * @return double que representa la cantidad total de agua
   * de riego utilizada en una parcela en una fecha [mm/dia]
   * o en un cultivo (sembrado en una parcela) en una fecha
   * [mm/dia] en caso de que se invoque este metodo para una
   * parcela que tiene un cultivo sembrado y en desarrollo
   * en una fecha
   */
  public static double sumTotalAmountIrrigationWaterGivenDate(Calendar date, Collection<IrrigationRecord> irrigationRecords) {
    double totalIrrigationWater = 0.0;

    for (IrrigationRecord currentIrrigationRecord : irrigationRecords) {

      /*
       * Acumula el agua de riego de todos los registros de riego
       * pertenecientes a una parcela que tienen la misma fecha.
       * De esta manera, se calcula la cantidad total de agua de
       * riego utilizada en una parcela en una fecha [mm/dia] o
       * en un cultivo (sembrado en una parcela) en una fecha
       * [mm/dia] en caso de que se invoque este metodo para una
       * parcela que tiene un cultivo sembrado y en desarrollo
       * en una fecha.
       */
      if (UtilDate.compareTo(currentIrrigationRecord.getDate(), date) == 0) {
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
