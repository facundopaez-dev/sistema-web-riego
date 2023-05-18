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
   * Calcula el agua excedente [mm/dia] de una fecha dada
   * en base a la ET, la precipitacion, la cantidad
   * total de agua de riego y el agua excedente de ayer,
   * debiendo ser los primeros tres valores de una misma
   * fecha.
   * 
   * La ET puede ser la ETc [mm/dia] (evapotranspiracion del
   * cultivo bajo condiciones estandar) si la parcela para la
   * que se calcula el agua excedente en una fecha tiene un
   * registro de plantacion en desarrollo (lo cual representa
   * que tiene un cultivo en desarrollo). En caso contrario,
   * la ET es la ETo [mm/dia] (evapotranspiracion del cultivo
   * de referencia).
   * 
   * @param et                            [mm/dia]
   * @param precipitationGivenDate        [mm/dia]
   * @param totalIrrigationWaterGivenDate [mm/dia]
   * @param excessWaterYesterday          [mm/dia]
   * @return punto flotante que representa el agua
   *         excedente [mm/dia] de una fecha dada
   */
  public static double calculateExcessWater(double et, double precipitationGivenDate,
      double totalIrrigationWaterGivenDate, double excessWaterYesterday) {
    /*
     * Si la suma entre la precipitacion, la cantidad total
     * de agua de riego y el agua excedente de ayer es mayor
     * a la ET (siendo esta y los dos primeros valores de una
     * misma fecha), hay agua excedente [mm/dia] en una fecha
     * dada. El agua excedente de una fecha dada se calcula
     * haciendo la diferencia entre la suma de la precipitacion,
     * la cantidad total de agua de riego y el agua excedente
     * de ayer, y la ET.
     * 
     * La ET puede ser la ETc [mm/dia] (evapotranspiracion del
     * cultivo bajo condiciones estandar) si la parcela para la
     * que se calcula el agua excedente en una fecha tiene un
     * registro de plantacion en desarrollo (lo cual representa
     * que tiene un cultivo en desarrollo). En caso contrario,
     * la ET es la ETo [mm/dia] (evapotranspiracion del cultivo
     * de referencia).
     */
    if ((precipitationGivenDate + totalIrrigationWaterGivenDate + excessWaterYesterday) > et) {
      return limitToTwoDecimalPlaces((precipitationGivenDate + totalIrrigationWaterGivenDate + excessWaterYesterday) - et);
    }

    /*
     * Si la ET es mayor o igual a la suma de la precipitacion,
     * la cantidad total de agua de riego y el agua excedente de
     * ayer, NO hay agua excedente [mm/dia] en una fecha dada.
     * Por lo tanto, el agua excedente de una fecha dada es 0.
     * 
     * La ET puede ser la ETc [mm/dia] (evapotranspiracion del
     * cultivo bajo condiciones estandar) si la parcela para la
     * que se calcula el agua excedente en una fecha tiene un
     * registro de plantacion en desarrollo (lo cual representa
     * que tiene un cultivo en desarrollo). En caso contrario,
     * la ET es la ETo [mm/dia] (evapotranspiracion del cultivo
     * de referencia).
     */
    return 0.0;
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
