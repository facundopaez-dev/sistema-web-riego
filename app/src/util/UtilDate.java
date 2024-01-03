package util;

import java.util.Calendar;
import java.util.Date;

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
   * @return referencia a un objeto de tipo Calendar que
   * contiene la fecha actual
   */
  public static Calendar getCurrentDate() {
    /*
     * El metodo getInstance de la clase Calendar
     * retorna la referencia a un objeto de tipo
     * Calendar que contiene la fecha actual
     */
    return Calendar.getInstance();
  }

  /**
   * @return referencia a un objeto de tipo Calendar que
   * contiene la fecha inmediatamente anterior a la fecha
   * actual
   */
  public static Calendar getYesterdayDate() {
    /*
     * El metodo getInstance de la clase Calendar retorna
     * la referencia a un objeto de tipo Calendar que
     * contiene la fecha actual. Para obtener el dia
     * inmediatamente anterior al dia actual (es decir,
     * el dia de ayer) se debe restar uno al numero de
     * dia en el año de la fecha actual.
     */
    Calendar yesterdayDate = Calendar.getInstance();
    yesterdayDate.set(Calendar.DAY_OF_YEAR, (yesterdayDate.get(Calendar.DAY_OF_YEAR) - 1));
    return yesterdayDate;
  }

  /**
   * @param date
   * @return referencia a un objeto de tipo Calendar que
   * contiene la fecha inmediatamente anterior a otra fecha
   */
  public static Calendar getYesterdayDateFromDate(Calendar date) {
    /*
     * El metodo getInstance de la clase Calendar retorna
     * la referencia a un objeto de tipo Calendar que
     * contiene la fecha actual. Por lo tanto, para que
     * este metodo devuelva una fecha inmediatamente anterior
     * a otra fecha se debe realizar un tratamiento sobre
     * dicho objeto.
     */
    Calendar yesterdayDateFromDate = Calendar.getInstance();
    int days = date.get(Calendar.DAY_OF_YEAR) - 1;

    /*
     * Si la resta de uno al numero de dia en el año de una
     * fecha es igual a 0, significa que la fecha es el dia
     * 1 de enero de un año. Por lo tanto, la fecha
     * inmediatamente anterior a 1 de enero de un año es
     * el dia 31 de diciembre del año inmediatamente anterior.
     */
    if (days == 0) {
      yesterdayDateFromDate.set(Calendar.YEAR, date.get(Calendar.YEAR) - 1);

      /*
       * Si el año de la fecha inmediatamente anterior a otra fecha
       * es bisiesto, el numero de dia en el año del dia 31 de
       * diciembre es 366. En caso contrario, es 365.
       */
      if (isLeapYear(yesterdayDateFromDate.get(Calendar.YEAR))) {
        yesterdayDateFromDate.set(Calendar.DAY_OF_YEAR, 366);
      } else {
        yesterdayDateFromDate.set(Calendar.DAY_OF_YEAR, 365);
      }

      return yesterdayDateFromDate;
    }

    /*
     * Si la resta de uno al numero de dia en el año de una
     * fecha es estrictamente mayor a 0, significa que el
     * año de la fecha inmediatamente anterior a otra es
     * igual al año de la misma
     */
    yesterdayDateFromDate.set(Calendar.YEAR, date.get(Calendar.YEAR));
    yesterdayDateFromDate.set(Calendar.DAY_OF_YEAR, date.get(Calendar.DAY_OF_YEAR) - 1);
    return yesterdayDateFromDate;    
  }

  /**
   * Retorna true si y solo si un año es bisiesto
   * 
   * @param year
   * @return true si un año es bisiesto, en caso contrario false
   */
  public static boolean isLeapYear(int year) {
    /*
     * Un año es bisiesto si es divisible por 4 y no es divisible
     * por 100
     */
    if (year % 4 == 0 && !(year % 100 == 0)) {
      return true;
    }

    /*
     * Un año es bisiesto si es divisible por 4, por 100 y por 400
     */
    if (year % 4 == 0 && year % 100 == 0 && year % 400 == 0) {
      return true;
    }

    return false;
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
    if ((compareTo(dateTwo, dateOne) >= 0) && (sameYear(dateOne, dateTwo))) {
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

  /**
   * Calcula la cantidad de dias que hay entre dos fechas
   * dadas.
   * 
   * Hay que tener en cuenta que para que este metodo funcione
   * correctamente, la fecha uno NO debe ser estrictamente
   * mayor a la fecha dos. Pienso que lo mejor es hacer que
   * este metodo compruebe si la fecha uno es estrictamente
   * mayor a la fecha dos, y en caso de que lo sea, este metodo
   * debe lanzar una excepcion que describa el problema ocurrido.
   * Por falta de tiempo no implemento esto.
   * 
   * Por lo tanto, se debe comprobar si la fecha uno es
   * estrictamente mayor a la fecha dos antes de invocar
   * a este metodo para evitar errores logicos, y, por ende,
   * para evitar que la aplicacion tenga un comportamiento
   * erroneo.
   * 
   * @param dateOne
   * @param dateTwo
   * @return entero que representa la cantidad de dias de
   *         diferencia que hay entre dos fechas dadas
   *         contemplando los años bisiestos y no bisiestos
   *         que hay entre ellas
   */
  public static int calculateDifferenceBetweenDates(Calendar dateOne, Calendar dateTwo) {
    /*
     * Si la fecha uno y la fecha dos tienen el mismo año,
     * se calcula directamente la diferencia entre el
     * numero de dia en el año de la fecha dos y el
     * numero de dia en el año de la fecha uno
     */
    if (sameYear(dateOne, dateTwo)) {
      return calculateDifferenceDaysWithinSameYear(dateOne, dateTwo);
    }

    int daysDifference = 0;
    int daysYear = 365;

    /*
     * Si el año de la fecha uno es bisiesto, se utiliza
     * 366 dias para calcular la cantidad de dias que hay
     * entre la fecha dos y la fecha uno
     */
    if (UtilDate.isLeapYear(dateOne.get(Calendar.YEAR))) {
      daysYear = 366;
    }

    /*
     * Si entre el año de la fecha dos y el año de la fecha uno
     * hay una unidad de diferencia, la cantidad de dias que hay
     * entre las dos fechas se calcula de la siguiente manera:
     * 
     * dias de diferencia = numero de dias en el año de la fecha
     * uno - numero de dia en el año de la fecha uno
     * 
     * dias de diferencia = dias de diferencia + numero de dia
     * en el año de la fecha dos
     */
    daysDifference = daysYear - dateOne.get(Calendar.DAY_OF_YEAR);
    daysDifference = daysDifference + dateTwo.get(Calendar.DAY_OF_YEAR);

    /*
     * Si hay mas de una unidad de diferencia entre el año de
     * la fecha dos y el año de la fecha uno, a la diferencia
     * de dias que hay entre la fecha dos y la fecha uno se
     * suma la cantidad de dias de los años que hay entre las
     * dos fechas, excluyendo el año de ambas, ya que a estos
     * se los tiene en cuenta en los calculos anteriores
     */
    daysDifference = daysDifference + calculateDifferenceDaysThroughYears(dateOne.get(Calendar.YEAR), dateTwo.get(Calendar.YEAR));
    return daysDifference;
  }

  /**
   * @param dateOne
   * @param dateTwo
   * @return -1 si la fecha uno es estrictamente menor a la fecha
   * dos, 0 si la fecha uno y la fecha dos son iguales y 1 si la
   * fecha uno es estrictamente mayor a la fecha dos
   */
  public static int compareTo(Calendar dateOne, Calendar dateTwo) {
    /*
     * Si el año de la fecha uno es menor al año de la fecha
     * dos, se retorna -1 como indicativo de que la fecha
     * uno es estrictamente menor a la fecha dos
     */
    if (dateOne.get(Calendar.YEAR) < dateTwo.get(Calendar.YEAR)) {
      return -1;
    }

    /*
     * Si el año de la fecha uno es mayor al año de la fecha
     * dos, se retorna 1 como indicativo de que la fecha
     * uno es estrictamente mayor a la fecha dos
     */
    if (dateOne.get(Calendar.YEAR) > dateTwo.get(Calendar.YEAR)) {
      return 1;
    }

    /*
     * Si el numero de dia en el año de la fecha uno es
     * menor al numero de dia en el año de la fecha dos,
     * se retorna -1 como indicativo de que la fecha uno
     * es estrictamente menor a la fecha dos
     */
    if (dateOne.get(Calendar.DAY_OF_YEAR) < dateTwo.get(Calendar.DAY_OF_YEAR)) {
      return -1;
    }

    /*
     * Si el numero de dia en el año de la fecha uno es
     * mayor al numero de dia en el año de la fecha dos,
     * se retorna 1 como indicativo de que la fecha uno
     * es estrictamente mayor a la fecha dos
     */
    if (dateOne.get(Calendar.DAY_OF_YEAR) > dateTwo.get(Calendar.DAY_OF_YEAR)) {
      return 1;
    }

    return 0;
  }

  /**
   * @param offset
   * @return referencia a un objeto de tipo Calendar que contiene
   * una fecha pasada calculada a partir de la resta entre un
   * numero entero mayor a cero (desplazamiento) y la fecha actual
   */
  public static Calendar getPastDateFromOffset(int offset) {
    Calendar pastDate = getCurrentDate();
    pastDate.set(Calendar.DAY_OF_YEAR, (pastDate.get(Calendar.DAY_OF_YEAR) - offset));
    return pastDate;
  }

  /**
   * @param givenDate
   * @return referencia a un objeto de tipo Calendar que
   * contiene la fecha contenida en un objeto de tipo Date
   */
  public static Calendar toCalendar(Date givenDate){ 
    Calendar date = Calendar.getInstance();
    date.setTime(givenDate);
    return date;
  }

}
