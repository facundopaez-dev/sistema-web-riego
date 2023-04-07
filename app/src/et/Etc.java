/*
 * ETc: Evapotranspiracion del cultivo bajo condiciones estandar
 */

package et;

public class Etc {

  /**
   * Calcula la evapotranspiracion del cultivo bajo condiciones
   * estandar, la cual nos indica la cantidad de agua que va a evaporar
   * un cultivo dado bajo condiciones estandar dado el coeficiente del
   * cultivo y unos factores climaticos
   *
   * @param  cropCoefficient (kc)           [adimensional]
   * @param  minTemperature                 [°C]
   * @param  maxTemperature                 [°C]
   * @param  pressure                       [kPa]
   * @param  windSpeed                      [metros/segundo]
   * @param  dewPoint (punto de rocío)      [°C]
   * @param  extraterrestrialSolarRadiation (Ra) [MJ/metro cuadrado * dia]
   * @param  maximumInsolation (N)          [horas]
   * @param  cloudCover (n)                 [%]
   * @return cantidad de agua que va a evaporar un cultivo dado [mm/dia]
   */
  public static double getEtc(double cropCoefficient, double minTemperature, double maxTemperature, double pressure, double windSpeed, double dewPoint,
    double extraterrestrialSolarRadiation, double maximumInsolation, double cloudCover) {
    return (cropCoefficient * PenmanMonteithEto.calculateEto(minTemperature, maxTemperature, pressure, windSpeed, dewPoint, extraterrestrialSolarRadiation, maximumInsolation, cloudCover));
  }

}
