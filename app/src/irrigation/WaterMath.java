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
   * Calcula la necesidad de agua de riego [mm/dia] de un cultivo
   * en la fecha actual mediante la suma de las ETc de NUMBER_DAYS
   * dias anteriores a la fecha actual, la suma del agua de lluvia
   * de NUMBER_DAYS dias anteriores a la fecha actual, la suma del
   * agua de riego de NUMBER_DAYS dias anteriores a la fecha actual
   * y la cantidad total de agua de riego de la fecha actual.
   * 
   * La constante NUMBER_DAYS pertenece a la clase ClimateRecordServiceBean.
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
     * Si la suma del agua de lluvia de NUMBER_DAYS dias anteriores
     * a la fecha actual es mayor o igual a la suma de las ETc de
     * NUMBER_DAYS dias anteriores a la fecha actual, la necesidad
     * de agua de riego de un cultivo en la fecha actual es 0
     */
    if (summedRainwaterPastDays >= etcSummedPastDays) {
      return 0.0;
    }

    /*
     * Si la suma del agua de riego de NUMBER_DAYS dias anteriores
     * a la fecha actual es mayor o igual a la suma de las ETc de
     * NUMBER_DAYS dias anteriores a la fecha actual, la necesidad
     * de agua de riego de un cultivo en la fecha actual es 0
     */
    if (summedIrrigationWaterPastDays >= etcSummedPastDays) {
      return 0.0;
    }

    /*
     * Si la cantidad total de agua de riego de la fecha actual es
     * mayor o igual a la suma de las ETc de NUMBER_DAYS dias anteriores
     * a la fecha actual, la necesidad de agua de riego de un cultivo
     * en la fecha actual es 0
     */
    if (totalIrrigationWaterCurrentDate >= etcSummedPastDays) {
      return 0.0;
    }

    /*
     * Si la suma entre la suma del agua de lluvia de NUMBER_DAYS
     * dias anteriores a la fecha actual, la suma del agua de riego
     * de NUMBER_DAYS dias anteriores a la fecha actual y la cantidad
     * total de agua de riego de la fecha actual es mayor o igual a la
     * suma de las ETc de NUMBER_DAYS dias anteriores a la fecha actual,
     * la necesidad de agua de riego de un cultivo en la fecha actual es 0
     */
    if ((summedRainwaterPastDays + summedIrrigationWaterPastDays + totalIrrigationWaterCurrentDate) >= etcSummedPastDays) {
      return 0.0;
    }

    /*
     * Si ninguna de las condiciones anteriores se cumple, la necesidad
     * de agua de riego de un cultivo en la fecha actual es mayor a 0 y
     * se calcula como la diferencia entre la suma de las ETc de NUMBER_DAYS
     * dias anteriores a la fecha actual y la suma entre la suma del agua
     * de lluvia de NUMBER_DAYS dias anteriores a la fecha actual, la
     * suma del agua de riego de NUMBER_DAYS dias anteriores a la fecha
     * actual y la cantidad total de agua de riego de la fecha actual
     */
    return limitToTwoDecimalPlaces(etcSummedPastDays - (summedRainwaterPastDays + summedIrrigationWaterPastDays + totalIrrigationWaterCurrentDate));
  }

  /**
   * Calcula el agua excedente [mm/dia] de una parcela en una
   * fecha dada en base a la ETc o la ETo, la precipitacion, la
   * cantidad total de agua de riego y el agua excedente de ayer,
   * debiendo ser los primeros cuatro valores de una misma
   * fecha.
   * 
   * @param etoGivenDate                  [mm/dia]
   * @param etcGivenDate                  [mm/dia]
   * @param precipitationGivenDate        [mm/dia]
   * @param totalIrrigationWaterGivenDate [mm/dia]
   * @param excessWaterYesterday          [mm/dia]
   * @return double que representa el agua excedente [mm/dia]
   * de una parcela en una fecha dada
   */
  public static double calculateExcessWater(double etoGivenDate, double etcGivenDate, double precipitationGivenDate,
      double totalIrrigationWaterGivenDate, double excessWaterYesterday) {
    double etGivenDate = 0.0;

    /*
     * Cuando una parcela NO tiene un cultivo sembrado y en desarrollo
     * en una fecha dada, no existe la ETc (evapotranspiracion del cultivo
     * bajo condiciones estandar) de dicho cultivo en dicha fecha. En
     * consecuencia la ETc es 0. Por lo tanto, se utiliza la ETo
     * (evapotranspiracion del cultivo de referencia) de la misma fecha
     * de la inexistente ETc para calcular el agua excedente de una
     * parcela en una fecha dada.
     * 
     * En caso contrario, se utiliza la ETc para calcular el agua excedente
     * de una parcela en una fecha dada.
     */
    if (etcGivenDate == 0.0) {
      etGivenDate = etoGivenDate;
    } else {
      etGivenDate = etcGivenDate;
    }

    /*
     * Si la suma entre la precipitacion, la cantidad total de agua
     * de riego y el agua excedente de ayer es mayor a la ET (siendo
     * esta y los dos primeros valores de una misma fecha), hay agua
     * excedente [mm/dia] en una parcela en una fecha dada. El agua
     * excedente de una parcela en una fecha dada se calcula haciendo
     * la diferencia entre la suma de la precipitacion, la cantidad
     * total de agua de riego y el agua excedente de ayer, y la ET.
     * 
     * Por el comentario de la primera instruccion if se entiende que
     * la ET es la ETo si la parcela para la que se calcula su agua
     * excedente en una fecha dada NO tiene o NO tuvo un cultivo
     * sembrado y en desarrollo en dicha fecha. En caso contrario,
     * la ET es la ETc.
     */
    if ((precipitationGivenDate + totalIrrigationWaterGivenDate + excessWaterYesterday) > etGivenDate) {
      return limitToTwoDecimalPlaces((precipitationGivenDate + totalIrrigationWaterGivenDate + excessWaterYesterday) - etGivenDate);
    }

    /*
     * Si la ET es mayor o igual a la suma de la precipitacion,
     * la cantidad total de agua de riego y el agua excedente de
     * ayer, NO hay agua excedente [mm/dia] de una parcela en una
     * fecha dada. Por lo tanto, el agua excedente de una parcela
     * en una fecha dada es 0.
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
