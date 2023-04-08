package et;

import java.lang.Math;

public class PenmanMonteithEto {

  /**
   * Calcula la evapotranspiracion del cultivo de referencia (ETo)
   * mediante la formula de la ETo de la FAO Penman-Monteith. La ETo
   * es necesaria para calcular la necesidad de agua de riego de un
   * cultivo.
   * 
   * La formula de la ETo de la FAO Penman-Monteith se encuentra en
   * la pagina 25 del libro "Evapotranspiracion del cultivo" de la
   * FAO. El cultivo de referencia es el pasto bien regado, segun
   * la pagina 6 del libro mencionado.
   * 
   * La necesidad hidrica de un cultivo se calcula mediante la formula
   * de la evapotranspiracion del cultivo bajo condiciones estandar
   * (ETc): ETc = ETo * Kc (coeficiente de cultivo).
   * 
   * Hay que tener en cuenta que las unidades de las variables
   * meteorologicas dependen del servicio meteorologico utilizado.
   * 
   * @param minTemperature                 [°C]
   * @param maxTemperature                 [°C]
   * @param pressure                       [mbar (hPa)]
   * @param windSpeed                      [km/h]
   * @param dewPoint                       [°C]
   * @param extraterrestrialSolarRadiation [MJ/m2/dia]
   * @param maximumInsolation              [horas]
   * @param actualDurationSunstroke        [horas]
   * @return punto flotante que representa la evapotranspiracion
   *         del cultivo de referencia (ETo) [mm/dia]
   */
  public static double calculateEto(double minTemperature, double maxTemperature, double pressure, double windSpeed,
      double dewPoint, double extraterrestrialSolarRadiation, double maximumInsolation,
      double actualDurationSunstroke) {

    // Temperatura media del aire (T) [°C]
    double averageAirTemperature = averageAirTemperature(minTemperature, maxTemperature);

    /*
     * Pendiente de la curva de presion de saturacion de
     * vapor (letra griega delta mayuscula) [kPa/°C]
     */
    double delta = slopeVaporSaturationPressureCurve(averageAirTemperature);

    // Constante psicrometrica (letra griega gamma) [kPa/°C]
    double gamma = psychometricConstant(pressureHectoPascalsToKiloPascals(pressure));

    /*
     * Velocidad del viento a dos metros de altura (u2) [metros/segundo]
     */
    double u2 = windSpeedTwoMetersHigh(convertWindSpeedToMetersPerSecond(windSpeed));

    // Presion media de vapor de saturacion (es) [kPa]
    double es = averageSaturationVaporPressure(minTemperature, maxTemperature);

    // Presion real de vapor (ea) [kPa]
    double ea = actualVaporPressure(dewPoint);

    // Deficit de presion de vapor [kPa]
    double vaporPressureDeficit = es - ea;

    // Radiacion neta (Rn) [MJ/m2/dia]
    double rn = netRadiation(extraterrestrialSolarRadiation, ea, minTemperature, maxTemperature, maximumInsolation,
        actualDurationSunstroke);

    double numerator = 0.408 * delta * rn
        + ((gamma * (900 / (averageAirTemperature + 273))) * u2 * (vaporPressureDeficit));

    double denominator = delta + gamma * (1 + 0.34 * u2);

    return (numerator / denominator);
  }

  /**
   * Retorna la pendiente de la curva de presion de saturacion
   * de vapor (letra griega delta mayuscula).
   *
   * Los valores de la pendiente de la curva de presion de
   * saturacion de vapor para distintas temperaturas promedio
   * se pueden ver en el Anexo A2.4 de la pagina 214 del libro
   * "Evapotranspiracion del cultivo" de la FAO.
   *
   * La ecuacion de la pendiente de la curva de presion de
   * saturacion de vapor es la ecuacion numero 13 de la pagina
   * 36 del libro "Evapotranspiracion del cultivo" de la FAO.
   *
   * @param averageAirTemperature [°C]
   * @return punto flotante que representa pendiente de la cutva
   *         de presion de saturacion de vapor [kPa/°C]
   */
  public static double slopeVaporSaturationPressureCurve(double averageAirTemperature) {
    double exp = (17.27 * averageAirTemperature) / (averageAirTemperature + 237.3);
    double numerator = 4098 * (0.6108 * Math.pow(Math.E, exp));
    double denominator = Math.pow(averageAirTemperature + 237.3, 2);

    return numerator / denominator;
  }

  /**
   * Retorna la temperatura media, la cual, es calculada a
   * partir de la temperatura minima y la temperatura maxima.
   * 
   * La ecuacion de la temperatura media es la ecuacion
   * numero 9 de la pagina 32 del libro "Evapotranspiracion
   * del cultivo" de la FAO.
   * 
   * @param minTemperature [°C]
   * @param maxTemperature [°C]
   * @return punto flotante que representa la temperatura
   *         media [°C]
   */
  public static double averageAirTemperature(double minTemperature, double maxTemperature) {
    return (minTemperature + maxTemperature) / 2;
  }

  /**
   * Retorna la constante psicrometrica (letra griega gamma).
   *
   * Los valores de la constante psicrometrica para distintas
   * presiones atmosfericas se pueden ver en el cuadro A2.2
   * del Anexo dos de la pagina 212 del libro "Evapotranspiracion
   * del cultivo" de la FAO.
   *
   * La ecuacion de la constante psicrometrica es la ecuacion
   * numero 8 de la pagina numero 31 del "Evapotranspiracion
   * del cultivo" de la FAO.
   *
   * @param atmosphericPressure [kPa]
   * @return punto flotante que representa la constante
   *         psicrometrica
   */
  public static double psychometricConstant(double atmosphericPressure) {
    /*
     * Calor latente de vaporizacion [MJ/Kg], este
     * valor constante esta representado por la letra
     * griega lambda
     */
    double lambda = 2.45;

    /*
     * Calor especifico a presion constante [MJ/Kg * °C], este
     * valor constante esta representado por la palabra cp
     */
    double cp = 0.001013;

    /*
     * Cociente del peso molecular de vapor de agua/aire seco, este
     * valor esta representado por la letra griega epsilon
     */
    double epsilon = 0.622;

    return (cp * atmosphericPressure) / (epsilon * lambda);
  }

  /**
   * Retorna la velocidad del viento corregida a dos metros
   * de altura (u2).
   *
   * Este metodo hace uso del factor de conversion (calculado
   * por el metodo conversionFactorToTwoMetersHigh) para convertir
   * la velocidad del viento medida a 10 metros sobre la superficie
   * del suelo a velocidad del viento a la elevacion de dos metros
   * sobre la superficie del suelo.
   *
   * La ecuacion para corregir la velocidad del viento a dos
   * metros de altura es la ecuacion numero 47 de la pagina
   * 56 del libro "Evapotranspiracion del cultivo" de la FAO.
   *
   * @param uz velocidad del viento medida a 10 metros sobre la superficie
   *           [metros/segundo]
   * @return punto flotante que representa la velocidad del viento a dos
   *         metros sobre la superficie [metros/segundo]
   */
  public static double windSpeedTwoMetersHigh(double uz) {
    return (uz * conversionFactorToTwoMetersHigh());
  }

  /**
   * Convierte la velocidad del viento de kilometros por
   * hora a metros por segundo
   * 
   * @param windSpeed [km/hora]
   * @return punto flotante que representa la velocidad
   *         del viento en metros por segundo
   */
  private static double convertWindSpeedToMetersPerSecond(double windSpeed) {
    return (windSpeed / 3.6);
  }

  /**
   * Retorna el factor de conversion para la velocidad del
   * viento a dos metros de altura (u2).
   *
   * Los factores de conversion de la velocidad del viento
   * para distintas alturas z se pueden ver en el cuadro
   * A2.9 del Anexo dos de la pagina 220 del libro
   * "Evapotranspiracion del cultivo" de la FAO.
   *
   * La ecuacion del factor de conversion es la ecuacion
   * numero 47 de la pagina numero 56 del libro
   * "Evapotranspiracion del cultivo" de la FAO.
   * 
   * La velocidad del viento generalmente se mide a 10
   * metros sobre el suelo. Este es el motivo del numero
   * 10 en la invocacion del metodo log estatico de la
   * clase Math.
   * 
   * @return punto flotante que representa el factor de
   *         conversion para convertir la velocidad
   *         del viento medida a una altura a 10 metros
   *         sobre el suelo a velocidad del viento a la
   *         elevacion estandar de dos metros sobre la
   *         superficie del suelo
   */
  public static double conversionFactorToTwoMetersHigh() {
    return (4.87 / Math.log(67.8 * 10 - 5.42));
  }

  /**
   * Retorna la presion de saturacion de vapor e°(T).
   *
   * La presion de saturacion de vapor para distintas
   * temperaturas del aire se puede ver en el cuadro
   * A2.3 del Anexo dos de la pagina 213 del libro
   * "Evapotranspiracion del cultivo" de la FAO.
   *
   * La ecuacion de la presion de saturacion de vapor es la
   * ecuacion numero 11 de la pagina numero 12 del libro
   * "Evapotranspiracion del cultivo" de la FAO.
   *
   * @param airTemperature [°C]
   * @return punto flotante que representa al presion de
   *         saturacion de vapor a la temperatura del
   *         aire [kPa]
   */
  public static double steamSaturationPressure(double airTemperature) {
    double exp = (17.27 * airTemperature) / (airTemperature + 237.3);
    return (0.6108 * Math.pow(Math.E, exp));
  }

  /**
   * Retorna la presion media de vapor de la saturacion (es).
   *
   * La ecuacion de la presion media de vapor de la saturacion
   * es la ecuacion numero 12 de la pagina numero 36 del libro
   * "Evapotranspiracion del cultivo" de la FAO.
   *
   * @param minTemperature [°C]
   * @param maxTemperature [°C]
   * @return punto flotante que repsenta la presion media de
   *         vapor de la saturacion [kPa]
   */
  public static double averageSaturationVaporPressure(double minTemperature, double maxTemperature) {
    return (steamSaturationPressure(minTemperature) + steamSaturationPressure(maxTemperature)) / 2;
  }

  /**
   * Presion real de vapor (ea) derivada de la temperatura
   * del punto de rocio
   *
   * La ecuacion de la presion real de vapor derivada
   * de la temperatura del punto de rocio es la
   * ecuacion numero 15 de la pagina 37 del libro
   * FAO 56
   *
   * @param dewPoint punto de rocio [°C]
   * @return presion real de vapor derivada de la temperatura
   *         del punto de rocio [kPa]
   */
  public static double actualVaporPressure(double dewPoint) {
    return steamSaturationPressure(dewPoint);
  }

  /**
   * Retorna la radiacion neta (Rn).
   *
   * La ecuacion de la radiacion neta es la ecuacion
   * numero 40 de la pagina numero 53 del libro
   * "Evapotranspiracion del cultivo" de la FAO.
   *
   * La radiacion neta es la diferencia entre la radiacion
   * neta de onda corta (Rns) y la radiacion neta de onda
   * larga (Rnl).
   * 
   * @param extraterrestrialSolarRadiation radiacion solar extraterrestre (Ra)
   * @param ea                             presion real de vapor [kPa]
   * @param minTemperature                 temperatura minima [°C]
   * @param maxTemperature                 temperatura maxima [°C]
   * @param maximumInsolation              duracion maxima de insolacion (N)
   * @param actualDurationSunstroke        nubosidad (n)
   * @return punto flotante que representa la radiacion
   *         neta [MJ/metros cuadrados * dia]
   */
  public static double netRadiation(double extraterrestrialSolarRadiation, double ea, double minTemperature,
      double maxTemperature, double maximumInsolation, double actualDurationSunstroke) {
    double solarRadiation = solarRadiation(extraterrestrialSolarRadiation, maximumInsolation, actualDurationSunstroke);

    /*
     * Radiacion solar de onda corta (Rns)
     */
    double netShortWaveRadiation = netShortWaveRadiation(solarRadiation);

    /*
     * Radiacion solar de onda larga (Rnl)
     */
    double netLongWaveRadiation = netLongWaveRadiation(extraterrestrialSolarRadiation, ea, minTemperature,
        maxTemperature, solarRadiation);

    return (netShortWaveRadiation - netLongWaveRadiation);
  }

  /**
   * Retorna la radiacion neta de onda larga (Rnl).
   *
   * La ecuacion de la radiacion neta de onda larga (Rnl)
   * es la ecuacion numero 39 de la pagina 52 del libro
   * "Evapotranspiracion del cultivo" de la FAO.
   *
   * @param extraterrestrialSolarRadiation radiacion solar extraterrestre (Ra)
   * @param ea                             presion real de vapor [kPa]
   * @param minTemperature                 temperatura minima [°C]
   * @param maxTemperature                 temperatura maxima [°C]
   * @param solarRadiation                 radiacion solar (Rs) [MJ/metros
   *                                       cuadrados * dia]
   * @return punto flotante que representa la radiacion neta
   *         de onda larga [MJ/metros cuadrados * dia]
   */
  public static double netLongWaveRadiation(double extraterrestrialSolarRadiation, double ea, double minTemperature,
      double maxTemperature, double solarRadiation) {
    double firstTerm = getSigmaResult(minTemperature, maxTemperature);

    /*
     * Este segundo termino de la ecuacion 39 de la pagina
     * 52 del libro "Evapotranspiracion del cultivo" de la
     * FAO, hace uso del valor de la presion real de vapor
     * (ea) derivada del punto de rocio
     */
    double secondTerm = 0.34 - (0.14 * Math.sqrt(ea));

    /*
     * Este tercer termino de la ecuacion 39 de la pagina
     * 52 del libro "Evapotranspiracion del cultivo" hace
     * uso de la radiacion solar (Rs) y de la radiacion
     * solar en un dia despejado (Rso)
     */
    double thirdTerm = ((1.35 * solarRadiation) / solarRadiationClearDay(extraterrestrialSolarRadiation)) - 0.35;

    return (firstTerm * secondTerm * thirdTerm);
  }

  /**
   * Retorna la radiacion neta solar o de onda corta (Rns).
   *
   * La ecuacion de la radiacion neta solar es la ecuacion
   * numero 38 de la pagina 51 del libro "Evapotranspiracion
   * del cultivo" de la FAO.
   *
   * @param solarRadiation radiacion solar (Rs) [MJ/metros cuadrados * dia]
   * @return punto flotante que representa la radiacion neta
   *         solar o de onda corta [MJ/metros cuadrados * dia]
   */
  public static double netShortWaveRadiation(double solarRadiation) {
    /*
     * Coeficiente de reflexion del cultivo.
     * 
     * Este valor en la formula de la radiacion neta
     * de onda corta esta representado por la letra
     * griega alfa (Ω).
     */
    double cropReflectionCoefficient = 0.23;

    return ((1 - cropReflectionCoefficient) * solarRadiation);
  }

  /**
   * Retorna el resultado de multiplicar la constante de
   * Stefan Boltzmann por un valor de temperatura en grados
   * Kelvin.
   *
   * La ecuacion de Stefan Boltzmann es la ecuacion numero
   * 39 de la pagina 52 del libro "Evapotranspiracion del
   * cultivo" de la FAO.
   *
   * La constante de Stefan Boltzmann esta en MJ/metros cuadrados * dia.
   *
   * @param minTemperature temperatura minima [°C]
   * @param maxTemperature temperatura maxima [°C]
   * @return punto flotante resultante del producto entre la constante
   *         de Stefan Boltzmann y el promedio de las temperaturas maxima
   *         y minima en grados Kelvin
   */
  public static double getSigmaResult(double minTemperature, double maxTemperature) {
    /*
     * En ambos casos, convierte la temperatura dada en °C a
     * grados Kelvin y luego la eleva a la cuarta potencia
     */
    double minKelvinTemperature = Math.pow(toKelvin(minTemperature), 4);
    double maxKelvinTemperature = Math.pow(toKelvin(maxTemperature), 4);

    /*
     * Comunmente, en la ecuacion de Stefan Boltzmann se utiliza el
     * promedio de la temperatura maxima del aire elevada a la cuarta
     * potencia y de la temperatura minima del aire elevada a la cuarta
     * potencia para periodos de 24 horas (a diario)
     */
    return (4.903E-9 * ((minKelvinTemperature + maxKelvinTemperature) / 2.0));
  }

  /**
   * Convierte la temperatura en °C a grados Kelvin
   *
   * @param temperature temperatura [°C]
   * @return punto flotante que representa la temperatura
   *         en grados Kelvin
   */
  private static double toKelvin(double temperature) {
    return (temperature + 273.16);
  }

  /**
   * Retorna la radiacion solar en un dia despejado (Rso).
   *
   * La ecuacion de la radiacion solar en un dia despejado
   * es la ecuacion numero 37 de la pagina 51 del libro
   * "Evapotranspiracion del cultivo" de la FAO.
   *
   * @param extraterrestrialSolarRadiation radiacion solar extraterrestre (Ra)
   * @return punto flotante que representa la radiacion solar
   *         en un dia despejado [MJ/metros cuadrados * dia]
   */
  public static double solarRadiationClearDay(double extraterrestrialSolarRadiation) {
    /*
     * Elevacion de la estacion sobre el nivel del mar [m]
     *
     * Este valor en la ecuacion esta representado
     * por la letra z
     */
    double elevation = 10.0;

    return ((0.75 + (0.00002 * elevation)) * extraterrestrialSolarRadiation);
  }

  /**
   * Retorna la radiacion solar (Rs).
   *
   * La ecuacion de la radiacion solar es la ecuacion numero
   * 35 de la pagina 50 del libro "Evapotranspiracion del
   * cultivo" de la FAO.
   *
   * @param extraterrestrialSolarRadiation radiacion solar extraterrestre (Ra)
   * @param maximumInsolation              duracion maxima de insolacion (N)
   * @param actualDurationSunstroke        duracion real de la insolacion (n)
   * @return punto flotante que representa la radiacion
   *         solar [MJ/metros cuadrados * dia]
   */
  public static double solarRadiation(double extraterrestrialSolarRadiation, double maximumInsolation,
      double actualDurationSunstroke) {
    /*
     * En la ecuacion de la radiacion solar, la duracion real de
     * la insolacion, dada por la variable actualDurationSunstroke,
     * es la letra n, la duracion maxima de insolacion, dada por la
     * variable maximumInsolation, es la letra N y la radiacion
     * solar extraterrestre, dada por la variable
     * extraterrestrialSolarRadiation, es la palabra Ra
     */
    double relativeDurationInsolation = (actualDurationSunstroke / maximumInsolation);

    return ((0.25 + (0.50 * relativeDurationInsolation)) * extraterrestrialSolarRadiation);
  }

  /**
   * Convierte la presión atmosferica de hectopascales
   * a kilopascales
   *
   * @param hectoPascalPressure
   * @return punto flotante que representa la presion
   *         atmosferica en kilopascales
   */
  private static double pressureHectoPascalsToKiloPascals(double hectoPascalPressure) {
    return (hectoPascalPressure * 0.1);
  }

}
