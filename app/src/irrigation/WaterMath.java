package irrigation;

import java.lang.Math;

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
   * Calcula la necesidad de agua de riego [mm/dia] de un
   * cultivo en la fecha actual en base a la ETc (evapotranspiracion
   * del cultivo bajo condiciones estandar) de la fecha
   * actual, la precipitacion de la fecha actual, la cantidad
   * total de agua de riego de la fecha actual y el
   * agua excedente de ayer
   * 
   * @param etcCurrentDate                  [mm/dia]
   * @param precipitationCurrentDate        [mm/dia]
   * @param totalIrrigationWaterCurrentDate [mm/dia]
   * @param excessWaterYesterday            [mm/dia]
   * @return punto flotante que representa la necesidad de
   *         agua de riego [mm/dia] de un cultivo en la fecha
   *         actual
   */
  public static double calculateIrrigationWaterNeed(double etcCurrentDate, double precipitationCurrentDate,
      double totalIrrigationWaterCurrentDate, double excessWaterYesterday) {
    /*
     * Si la suma entre la precipitacion de la fecha actual,
     * la cantidad total de agua de riego de la fecha actual
     * y el agua excedente de ayer es mayor o igual a la ETc
     * de la fecha actual, la necesidad de agua de riego de
     * un cultivo en la fecha actual es 0, ya que si se da
     * esta condicion, la ETc (milimetros de agua por dia)
     * de la fecha actual es cubierta
     */
    if ((precipitationCurrentDate + totalIrrigationWaterCurrentDate + excessWaterYesterday) >= etcCurrentDate) {
      return 0.0;
    }

    /*
     * Si la ETc de la fecha actual es mayor a la suma
     * entre la precipitacion de la fecha actual, la
     * cantidad total de agua de riego de hoy y el
     * agua excedente de ayer, la necesidad de agua de
     * riego de un cultivo en la fecha actual es la
     * diferencia entre la ETc de la fecha actual y la
     * suma entre la precipitacion de la fecha actual,
     * la cantidad total de agua de riego de la fecha
     * actual y el agua excedente de ayer.
     * 
     * Si se da esta condicion, la ETc (milimetros de
     * agua por dia) de la fecha actual no es cubierta,
     * por lo tanto, hay una cantidad de agua que debe
     * ser cubierta mediante agua de riego. Dicha cantidad
     * se calcula haciendo la diferencia descrita en el
     * parrafo anterior.
     */
    return limitToTwoDecimalPlaces(etcCurrentDate - (precipitationCurrentDate + totalIrrigationWaterCurrentDate + excessWaterYesterday));
  }

  /**
   * Calcula la cantidad de agua acumulada [milimetros] del dia hoy
   * en funcion de la ETc, la ETo (en caso de que la ETc sea cero, lo cual
   * se debe a que no hubo un cultivo sembrado en el dia de ayer en la
   * parcela dada), la cantidad de agua de lluvia, la cantidad de agua
   * acumulada (siendo todos estos valores del dia de ayer) y la cantidad
   * total de agua de riego utilizada en el dia de hoy
   *
   * @param  yesterdayEtc              [ETc del dia de ayer] [milimetros]
   * @param  yesterdayEto              [ETo del dia de ayer] [milimetros]
   * @param  yesterdayRainWater        [cantidad de agua de lluvia del dia de ayer] [milimetros]
   * @param  waterAccumulatedYesterday [cantidad de agua acumulada del dia de ayer] [milimetros]
   * @param  totalIrrigationWaterToday [cantidad total de agua utilizada en los riegos del dia de hoy] [milimetros]
   * @return la cantidad de agua [milimetros] acumulada en el dia
   * de hoy, la cual es la cantidad de agua a favor del dia de hoy
   * para el dia de mañana
   */
  public static double getWaterAccumulatedToday(double yesterdayEtc, double yesterdayEto, double yesterdayRainWater,
  double waterAccumulatedYesterday, double totalIrrigationWaterToday) {

    double yesterdayEvapotranspiration = 0.0;
    double waterAccumulatedToday = 0.0;

    /*
     * La ETc es cero cuando no hay un cultivo sembrado
     * en la parcela dada, en cambio es mayor a cero
     * cuando hay un cultivo sembrado en la parcela dada
     *
     * Si la ETc del dia de ayer fue cero (porque no hubo
     * cultivo plantado en el dia de ayer en la parcela
     * dada) se utiliza la ETo del dia de ayer para
     * calcular la cantidad de agua acumulada del dia
     * de hoy, la cual es agua a favor del dia de hoy
     * para el dia de mañana
     *
     * Si la ETc del dia de ayer no fue cero (porque hubo
     * un cultivo plantado en el dia de ayer en la parcela
     * dada), entonces se la utiliza para calcular la cantidad
     * de agua acumulada del dia de hoy, la cual es agua a favor
     * del dia de hoy para el dia de mañana
     */
    if (yesterdayEtc == 0.0) {
      yesterdayEvapotranspiration = yesterdayEto;
    } else {
      yesterdayEvapotranspiration = yesterdayEtc;
    }

    /*
     * Si el agua de lluvia del dia de ayer mas la cantidad de agua
     * acumulada del dia de ayer mas la cantidad total de agua utilizada
     * en el riego del dia de hoy es mayor que la evapotranspiracion del
     * dia de ayer, entonces la cantidad de agua acumulada [milimetros]
     * del dia de hoy (la cual es agua a favor para mañana) es la
     * diferencia entre la suma de las cantidades de agua mencionadas
     * y la evapotranspiracion del dia de ayer
     */
    if ((yesterdayRainWater + waterAccumulatedYesterday + totalIrrigationWaterToday) > yesterdayEvapotranspiration) {
      waterAccumulatedToday = (yesterdayRainWater + waterAccumulatedYesterday + totalIrrigationWaterToday) - yesterdayEvapotranspiration;
    }

    return waterAccumulatedToday;
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
