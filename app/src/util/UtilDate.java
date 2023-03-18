package util;

import java.util.Calendar;

public class UtilDate {

  private UtilDate() {

  }

  /**
   * @param  givenDate
   * @return cadena de caracteres que tiene el
   * formato DD-MM-YYYY de la fecha dada
   */
  public static String formatDate(Calendar givenDate) {
    return (givenDate.get(Calendar.DAY_OF_MONTH) + "-" + (givenDate.get(Calendar.MONTH) + 1) + "-" + givenDate.get(Calendar.YEAR));
  }

  /**
   * @return la fecha anterior a la fecha actual del sistema
   */
  public static Calendar getYesterdayDate() {
    Calendar currentDate = Calendar.getInstance();
    Calendar yesterdayDate = Calendar.getInstance();

    /*
     * Si la fecha actual es el primero de Enero, entonces
     * la fecha anterior a la fecha actual es el 31 de Diciembre
     * del año anterior al año de la fecha actual
     *
     * Si la fecha actual no es el primero de Enero, entonces
     * la fecha anterior a la fecha actual es el dia anterior
     * a la fecha actual y ambas fechas pertenecen al mismo
     * año
     */
    if (currentDate.get(Calendar.DAY_OF_YEAR) == 1) {
      yesterdayDate.set(Calendar.DAY_OF_YEAR, 365);
      yesterdayDate.set(Calendar.YEAR, currentDate.get(Calendar.YEAR) - 1);
    } else {
      yesterdayDate.set(Calendar.DAY_OF_YEAR, currentDate.get(Calendar.DAY_OF_YEAR) - 1);
    }

    return yesterdayDate;
  }

  /**
   * @return la fecha siguiente a la fecha actual del sistema
   */
  public static Calendar getTomorrowDate() {
    Calendar currentDate = Calendar.getInstance();
    Calendar tomorrowDate = Calendar.getInstance();

    /*
     * Si la fecha actual es el ultimo dia de Diciembre,
     * es decir, 31 de Diciembre, entonces la fecha siguiente
     * a la fecha actual es el 1 de Enero del año siguiente
     * a la fecha actual
     *
     * Si la fecha actual no es el ultimo dia de Diciembre,
     * es decir, 31 de Diciembre, entonces la fecha siguiente
     * a la fecha actual es el dia de la fecha actual mas un
     * dia
     */
    if ((currentDate.get(Calendar.DAY_OF_YEAR)) == 365) {
      tomorrowDate.set(Calendar.DAY_OF_YEAR, 1);
      tomorrowDate.set(Calendar.YEAR, currentDate.get(Calendar.YEAR) + 1);
    } else {
      tomorrowDate.set(Calendar.DAY_OF_YEAR, currentDate.get(Calendar.DAY_OF_YEAR) + 1);
    }

    return tomorrowDate;
  }

  /**
   * Retorna true si y solo si un año es bisiesto
   * 
   * @param year
   * @return true si un año es bisiesto, en caso contrario false
   */
  public static boolean isLeapYear(int year) {
    return ((year % 4) == 0);
  }

  /**
   * Retorna true si y solo si la fecha uno y la fecha dos tienen el
   * mismo año
   * 
   * @param dateOne
   * @param datetTwo
   * @return true si la fecha uno y la fecha dos tienen el mismo año,
   * en caso contrario false
   */
  public static boolean sameYear(Calendar dateOne, Calendar datetTwo) {
    return (dateOne.get(Calendar.YEAR) == datetTwo.get(Calendar.YEAR));
  }

  /**
   * Retorna la cantidad de dias que hay entre la fecha dos y la
   * fecha uno, siempre y cuando la fecha dos sea mayor o igual que
   * la fecha uno y ambas tengan el mismo año
   * 
   * @param dateOne
   * @param dateTwo
   * @return cantidad de dias que hay entre la fecha dos y la
   * fecha uno, siempre y cuando la fecha dos sea mayor o igual
   * que la fecha uno y ambas tengan el mismo año. En caso
   * contrario, 0.
   */
  public static int calculateDifferenceDaysWithinSameYear(Calendar dateOne, Calendar dateTwo) {
    /*
     * Si la fecha dos es mayor o igual que la fecha uno,
     * y ambas tienen el mismo año, se retorna la diferencia
     * de dias entre ellas haciendo la resta entre el numero
     * de dia en el año de la fecha dos y el numero de dia en
     * el año de la fecha uno
     */
    if ((dateTwo.compareTo(dateOne) >= 0) && (dateOne.get(Calendar.YEAR) == dateTwo.get(Calendar.YEAR))) {
      return (dateTwo.get(Calendar.DAY_OF_YEAR) - dateOne.get(Calendar.DAY_OF_YEAR));
    }

    return 0;
  }

  /**
   * @param initialYear
   * @param finalYear
   * @return cantidad de dias que hay entre el año inicial y el
   * año final, excluyendo la cantidad toal de dias de ambos años
   * y contemplando los años bisiestos y no bisiestos que hay entre
   * ambos
   */
  public static int calculateDifferenceDaysThroughYears(int initialYear, int finalYear) {
    int daysDifference = 0;

    /*
     * Calcula la cantidad de dias que hay entre el año inicial
     * y el año final, excluyendo la cantidad total de dias de
     * ambos años y contemplando los años bisiestos y no bisiestos
     * que hay entre ambos
     */
    for (int year = initialYear + 1; year < finalYear; year++) {

      if (isLeapYear(year)) {
        daysDifference = daysDifference + 366;
      } else {
        daysDifference = daysDifference + 365;
      }

    }

    return daysDifference;
  }

}
