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
