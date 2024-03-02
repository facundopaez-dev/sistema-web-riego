package util;

public class UtilMath {

    /**
     * 
     * 
     * @param number
     * @return double que representa un numero con dos cifras
     * decimales. La cantidad de cifras decimales depende de
     * la cantidad de ceros del valor utilizado para truncar
     * un numero.
     */
    public static double truncateToTwoDigits(double number) {
        return Math.round(number * 100d) / 100d;
    }

}