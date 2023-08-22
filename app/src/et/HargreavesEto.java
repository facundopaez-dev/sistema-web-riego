package et;

public class HargreavesEto {

    /**
     * Calcula la evapotranspiracion del cultivo de referencia (ETo)
     * mediante la formula de la ETo de Hargreaves y Samani. La ETo
     * es necesaria para calcular la necesidad de agua de riego de
     * un cultivo.
     * 
     * La formula de la ETo de Hargreaves se encuentra en la pagina
     * 64 del libro "Evapotranspiracion del cultivo" de la FAO, y es
     * la siguiente:
     * 
     * ETo = 0,0023 * (Tmedia + 17,8) * (Tmax - Tmin)^0,5 * Ra
     * 
     * donde:
     * - Tmedia es temperatura media
     * - Tmax es temperatura maxima
     * - Tmin es temperatura minima
     * - Ra es readiacion solar extraterrestre
     * 
     * La necesidad hidrica de un cultivo se calcula mediante la
     * formula de la evapotranspiracion del cultivo bajo condiciones
     * estandar (ETc): ETc = ETo * Kc (coeficiente de cultivo).
     * 
     * Hay que tener en cuenta que las unidades de las variables
     * meteorologicas dependen del servicio meteorologico utilizado.
     * 
     * @param maximumTemperature             [°C]
     * @param minimumTemperature             [°C]
     * @param extraterrestrialSolarRadiation [MJ/m2/dia]
     * @return punto flotante que represent la evapotranspiracion
     *         del cultivo de referencia (ETo) [mm/dia]
     */
    public static double calculateEto(double maximumTemperature, double minimumTemperature, double extraterrestrialSolarRadiation) {
        return 0.0023 * (calculateMeanTemperature(maximumTemperature, minimumTemperature) + 17.8)
                * Math.pow(maximumTemperature - minimumTemperature, 0.5)
                * toMillimetersPerDay(extraterrestrialSolarRadiation);
    }

    /**
     * @param maximumTemperature [°C]
     * @param minimumTemperature [°C]
     * @return punto flotante que representa la temperatura
     *         media calculada a partir de una temperatura maxima
     *         y una temperatura minima
     */
    private static double calculateMeanTemperature(double maximumTemperature, double minimumTemperature) {
        return ((maximumTemperature + minimumTemperature) / 2);
    }

    /**
     * Convierte la radiacion solar extraterrestre dada en
     * megajulios por metro cuadrado por dia [MJ/m2/dia]
     * a milimetros por dia.
     * 
     * @param extraterrestrialSolarRadiation
     * @return punto flotante que representa la radiacion solar
     *         extraterrestre en milimetros por dia [mm/dia]
     */
    private static double toMillimetersPerDay(double extraterrestrialSolarRadiation) {
        return extraterrestrialSolarRadiation * 0.408;
    }

}