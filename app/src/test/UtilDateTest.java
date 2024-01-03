import static org.junit.Assert.*;

import java.util.Calendar;
import org.junit.Ignore;
import org.junit.Test;
import util.UtilDate;

public class UtilDateTest {

  private static final int JANUARY = 0;
  private static final int FEBRUARY = 1;
  private static final int MARCH = 2;
  private static final int APRIL = 3;
  private static final int MAY = 4;
  private static final int JUNE = 5;
  private static final int JULY = 6;
  private static final int AUGUST = 7;
  private static final int SEPTEMBER = 8;
  private static final int OCTOBER = 9;
  private static final int NOVEMBER = 10;
  private static final int DECEMBER = 11;

  @Test
  public void testGetPastDateFromOffset() {
    System.out.println("**************************** Prueba uno del metodo getPastDateFromOffset ****************************");
    System.out.println("El metodo getPastDateFromOffset de la clase UtilDate calcula una fecha pasada a partir de la fecha actual");
    System.out.println("y un desplazamiento mayor a cero.");
    System.out.println();

    int offest = 3;

    /*
     * Calculo de la fecha pasada a partir de la fecha actual
     * y un desplazamiento mayor a cero
     */
    Calendar givenDate = UtilDate.getPastDateFromOffset(offest);
    System.out.println("Fecha pasada calculada a partir de la fecha actual y un desplazamiento = " + offest + ": " + UtilDate.formatDate(givenDate));

    /*
     * Seccion de prueba
     */
    Calendar expectedDate = UtilDate.getCurrentDate();
    expectedDate.set(Calendar.DAY_OF_YEAR, (expectedDate.get(Calendar.DAY_OF_YEAR) - offest));
    System.out.println();

    System.out.println("* Seccion de prueba *");
    System.out.println("Fecha pasada esperada: " + UtilDate.formatDate(expectedDate));
    System.out.println("Fecha pasada calculada por el metodo getPastDateFromOffset: " + UtilDate.formatDate(givenDate));
    System.out.println();

    assertTrue((expectedDate.get(Calendar.YEAR) == givenDate.get(Calendar.YEAR))
        && (expectedDate.get(Calendar.MONTH) == givenDate.get(Calendar.MONTH))
        && (expectedDate.get(Calendar.DAY_OF_YEAR) == givenDate.get(Calendar.DAY_OF_YEAR)));

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testOneCompareTo() {
    System.out.println("************************* Prueba uno del metodo compareTo *************************");
    System.out.println("El metodo compareTo de la clase UtilDate compara dos fechas. Retorna 0 si las fechas");
    System.out.println("uno y dos son iguales, -1 si la fecha uno es menor a la fecha dos y 1 si la fecha uno");
    System.out.println("es mayor a la fecha dos.");
    System.out.println();
    System.out.println("Para demostrar el correcto funcionamiento de este metodo, en este prueba se utilizan");
    System.out.println("una fecha uno y feccha dos iguales. Por lo tanto, el metodo compareTo retorna el valor 0.");
    System.out.println();

    Calendar dateOne = UtilDate.getCurrentDate();
    Calendar dateTwo = UtilDate.getCurrentDate();

    System.out.println("Fecha uno: " + UtilDate.formatDate(dateOne));
    System.out.println("Fecha dos: " + UtilDate.formatDate(dateTwo));
    System.out.println();

    /*
     * Seccion de prueba
     */
    int expectedResult = 0;
    int result = UtilDate.compareTo(dateOne, dateTwo);

    System.out.println("* Resultado esperado: " + expectedResult);
    System.out.println("* Resultado devuelto por el metodo compareTo de la clase UtilDate: " + expectedResult);
    System.out.println();

    assertTrue(expectedResult == result);

    System.out.println("- Prueba pasada satisfactoriamente");
  }

  @Test
  public void testTwoCompareTo() {
    System.out.println("************************* Prueba dos del metodo compareTo *************************");
    System.out.println("El metodo compareTo de la clase UtilDate compara dos fechas. Retorna 0 si las fechas");
    System.out.println("uno y dos son iguales, -1 si la fecha uno es menor a la fecha dos y 1 si la fecha uno");
    System.out.println("es mayor a la fecha dos.");
    System.out.println();
    System.out.println("Para demostrar el correcto funcionamiento de este metodo, en este prueba se utiliza");
    System.out.println("una fecha uno menor a una fecha dos. Por lo tanto, el metodo compareTo retorna el valor -1.");
    System.out.println();

    Calendar dateOne = UtilDate.getYesterdayDate();
    Calendar dateTwo = UtilDate.getCurrentDate();

    System.out.println("Fecha uno: " + UtilDate.formatDate(dateOne));
    System.out.println("Fecha dos: " + UtilDate.formatDate(dateTwo));
    System.out.println();

    /*
     * Seccion de prueba
     */
    int expectedResult = -1;
    int result = UtilDate.compareTo(dateOne, dateTwo);

    System.out.println("* Resultado esperado: " + expectedResult);
    System.out.println("* Resultado devuelto por el metodo compareTo de la clase UtilDate: " + expectedResult);
    System.out.println();

    assertTrue(expectedResult == result);

    System.out.println("- Prueba pasada satisfactoriamente");
  }

  @Test
  public void testThreeCompareTo() {
    System.out.println("************************* Prueba tres del metodo compareTo *************************");
    System.out.println("El metodo compareTo de la clase UtilDate compara dos fechas. Retorna 0 si las fechas");
    System.out.println("uno y dos son iguales, -1 si la fecha uno es menor a la fecha dos y 1 si la fecha uno");
    System.out.println("es mayor a la fecha dos.");
    System.out.println();
    System.out.println("Para demostrar el correcto funcionamiento de este metodo, en este prueba se utiliza");
    System.out.println("una fecha uno mayor a una fecha dos. Por lo tanto, el metodo compareTo retorna el valor 1.");
    System.out.println();

    Calendar dateOne = UtilDate.getCurrentDate();
    Calendar dateTwo = UtilDate.getYesterdayDate();

    System.out.println("Fecha uno: " + UtilDate.formatDate(dateOne));
    System.out.println("Fecha dos: " + UtilDate.formatDate(dateTwo));
    System.out.println();

    /*
     * Seccion de prueba
     */
    int expectedResult = 1;
    int result = UtilDate.compareTo(dateOne, dateTwo);

    System.out.println("* Resultado esperado: " + expectedResult);
    System.out.println("* Resultado devuelto por el metodo compareTo de la clase UtilDate: " + expectedResult);
    System.out.println();

    assertTrue(expectedResult == result);

    System.out.println("- Prueba pasada satisfactoriamente");
  }

  @Test
  public void testOneGetYesterdayDateFromDate() {
    System.out.println("**************************** Prueba uno del metodo getYesterdayDateFromDate ****************************");
    System.out.println("El metodo getYesterdayDateFromDate() de la clase UtilDate retorna la fecha inmediatamente anterior a la");
    System.out.println("fecha que se le pasa como argumento.");
    System.out.println();
    System.out.println("Para demostrar el correcto funcionamiento de este metodo, en esta prueba se utiliza la fecha 1-3-2023.");
    System.out.println("Por lo tanto, el metodo getYesterdayDateFromDate() debe retornar la fecha 28-2-2023.");
    System.out.println();

    Calendar date = Calendar.getInstance();
    date.set(Calendar.YEAR, 2023);
    date.set(Calendar.MONTH, MARCH);
    date.set(Calendar.DAY_OF_MONTH, 1);

    Calendar expectedDate = Calendar.getInstance();
    expectedDate.set(Calendar.YEAR, 2023);
    expectedDate.set(Calendar.MONTH, FEBRUARY);
    expectedDate.set(Calendar.DAY_OF_MONTH, 28);

    /*
     * Seccion de prueba
     */
    Calendar resultingDate = UtilDate.getYesterdayDateFromDate(date);

    System.out.println("Fecha a partir de la cual obtener la fecha inmediatamente anterior: " + UtilDate.formatDate(date));
    System.out.println("Fecha esperada: " + UtilDate.formatDate(expectedDate));
    System.out.println("Fecha devuelta por el metodo getYesterdayDateFromDate(): " + UtilDate.formatDate(resultingDate));
    System.out.println();

    assertTrue(UtilDate.compareTo(resultingDate, expectedDate) == 0);

    System.out.println("- Prueba pasada satisfactoriamente");
  }

  @Test
  public void testTwoGetYesterdayDateFromDate() {
    System.out.println("**************************** Prueba dos del metodo getYesterdayDateFromDate ****************************");
    System.out.println("El metodo getYesterdayDateFromDate() de la clase UtilDate retorna la fecha inmediatamente anterior a la");
    System.out.println("fecha que se le pasa como argumento.");
    System.out.println();
    System.out.println("Para demostrar el correcto funcionamiento de este metodo, en esta prueba se utiliza la fecha 1-1-2023.");
    System.out.println("Por lo tanto, el metodo getYesterdayDateFromDate() debe retornar la fecha 31-12-2022.");
    System.out.println();

    Calendar date = Calendar.getInstance();
    date.set(Calendar.YEAR, 2023);
    date.set(Calendar.MONTH, JANUARY);
    date.set(Calendar.DAY_OF_MONTH, 1);

    Calendar expectedDate = Calendar.getInstance();
    expectedDate.set(Calendar.YEAR, 2022);
    expectedDate.set(Calendar.MONTH, DECEMBER);
    expectedDate.set(Calendar.DAY_OF_MONTH, 31);

    /*
     * Seccion de prueba
     */
    Calendar resultingDate = UtilDate.getYesterdayDateFromDate(date);

    System.out.println("Fecha a partir de la cual obtener la fecha inmediatamente anterior: " + UtilDate.formatDate(date));
    System.out.println("Fecha esperada: " + UtilDate.formatDate(expectedDate));
    System.out.println("Fecha devuelta por el metodo getYesterdayDateFromDate(): " + UtilDate.formatDate(resultingDate));
    System.out.println("Numero de dia en el año de la fecha (devuelta) " + UtilDate.formatDate(resultingDate) + ": " + resultingDate.get(Calendar.DAY_OF_YEAR));
    System.out.println();

    assertTrue(UtilDate.compareTo(resultingDate, expectedDate) == 0);

    System.out.println("- Prueba pasada satisfactoriamente");
  }

  @Test
  public void testThreeGetYesterdayDateFromDate() {
    System.out.println("**************************** Prueba tres del metodo getYesterdayDateFromDate ****************************");
    System.out.println("El metodo getYesterdayDateFromDate() de la clase UtilDate retorna la fecha inmediatamente anterior a la");
    System.out.println("fecha que se le pasa como argumento.");
    System.out.println();
    System.out.println("Para demostrar el correcto funcionamiento de este metodo, en esta prueba se utiliza la fecha 1-3-2020.");
    System.out.println("Por lo tanto, el metodo getYesterdayDateFromDate() debe retornar la fecha 29-2-2020 (año bisiesto).");
    System.out.println();

    Calendar date = Calendar.getInstance();
    date.set(Calendar.YEAR, 2020);
    date.set(Calendar.MONTH, MARCH);
    date.set(Calendar.DAY_OF_MONTH, 1);

    Calendar expectedDate = Calendar.getInstance();
    expectedDate.set(Calendar.YEAR, 2020);
    expectedDate.set(Calendar.MONTH, FEBRUARY);
    expectedDate.set(Calendar.DAY_OF_MONTH, 29);

    /*
     * Seccion de prueba
     */
    Calendar resultingDate = UtilDate.getYesterdayDateFromDate(date);

    System.out.println("Fecha a partir de la cual obtener la fecha inmediatamente anterior: " + UtilDate.formatDate(date));
    System.out.println("Fecha esperada: " + UtilDate.formatDate(expectedDate));
    System.out.println("Fecha devuelta por el metodo getYesterdayDateFromDate(): " + UtilDate.formatDate(resultingDate));
    System.out.println();

    assertTrue(UtilDate.compareTo(resultingDate, expectedDate) == 0);

    System.out.println("- Prueba pasada satisfactoriamente");
  }

  @Test
  public void testFourGetYesterdayDateFromDate() {
    System.out.println("**************************** Prueba cuatro del metodo getYesterdayDateFromDate ****************************");
    System.out.println("El metodo getYesterdayDateFromDate() de la clase UtilDate retorna la fecha inmediatamente anterior a la");
    System.out.println("fecha que se le pasa como argumento.");
    System.out.println();
    System.out.println("Para demostrar el correcto funcionamiento de este metodo, en esta prueba se utiliza la fecha 1-1-2021.");
    System.out.println("Por lo tanto, el metodo getYesterdayDateFromDate() debe retornar la fecha 31-12-2020 (año bisiesto).");
    System.out.println();

    Calendar date = Calendar.getInstance();
    date.set(Calendar.YEAR, 2021);
    date.set(Calendar.MONTH, JANUARY);
    date.set(Calendar.DAY_OF_MONTH, 1);

    Calendar expectedDate = Calendar.getInstance();
    expectedDate.set(Calendar.YEAR, 2020);
    expectedDate.set(Calendar.MONTH, DECEMBER);
    expectedDate.set(Calendar.DAY_OF_MONTH, 31);

    /*
     * Seccion de prueba
     */
    Calendar resultingDate = UtilDate.getYesterdayDateFromDate(date);

    System.out.println("Fecha a partir de la cual obtener la fecha inmediatamente anterior: " + UtilDate.formatDate(date));
    System.out.println("Fecha esperada: " + UtilDate.formatDate(expectedDate));
    System.out.println("Fecha devuelta por el metodo getYesterdayDateFromDate(): " + UtilDate.formatDate(resultingDate));
    System.out.println("Numero de dia en el año de la fecha (devuelta) " + UtilDate.formatDate(resultingDate) + ": " + resultingDate.get(Calendar.DAY_OF_YEAR));
    System.out.println();

    assertTrue(UtilDate.compareTo(resultingDate, expectedDate) == 0);

    System.out.println("- Prueba pasada satisfactoriamente");
  }

  @Test
  public void testFiveGetYesterdayDateFromDate() {
    System.out.println("**************************** Prueba cinco del metodo getYesterdayDateFromDate ****************************");
    System.out.println("El metodo getYesterdayDateFromDate() de la clase UtilDate retorna la fecha inmediatamente anterior a la");
    System.out.println("fecha que se le pasa como argumento.");
    System.out.println();
    System.out.println("Para demostrar el correcto funcionamiento de este metodo, en esta prueba se utiliza la fecha 2-1-2021.");
    System.out.println("Por lo tanto, el metodo getYesterdayDateFromDate() debe retornar la fecha 1-1-2021.");
    System.out.println();

    Calendar date = Calendar.getInstance();
    date.set(Calendar.YEAR, 2021);
    date.set(Calendar.MONTH, JANUARY);
    date.set(Calendar.DAY_OF_MONTH, 2);

    Calendar expectedDate = Calendar.getInstance();
    expectedDate.set(Calendar.YEAR, 2021);
    expectedDate.set(Calendar.MONTH, JANUARY);
    expectedDate.set(Calendar.DAY_OF_MONTH, 1);

    /*
     * Seccion de prueba
     */
    Calendar resultingDate = UtilDate.getYesterdayDateFromDate(date);

    System.out.println("Fecha a partir de la cual obtener la fecha inmediatamente anterior: " + UtilDate.formatDate(date));
    System.out.println("Fecha esperada: " + UtilDate.formatDate(expectedDate));
    System.out.println("Fecha devuelta por el metodo getYesterdayDateFromDate(): " + UtilDate.formatDate(resultingDate));
    System.out.println();

    assertTrue(UtilDate.compareTo(resultingDate, expectedDate) == 0);

    System.out.println("- Prueba pasada satisfactoriamente");
  }

  @Test
  public void testOneGetNextDateFromDate() {
    System.out.println("**************************** Prueba uno del metodo getNextDateFromDate ****************************");
    System.out.println("El metodo getNextDateFromDate() de la clase UtilDate retorna la fecha inmediatamente siguiente a la");
    System.out.println("fecha que se le pasa como argumento.");
    System.out.println();
    System.out.println("Para demostrar el correcto funcionamiento de este metodo, en esta prueba se utiliza la fecha 31-12-2022.");
    System.out.println("Por lo tanto, el metodo getNextDateFromDate() debe retornar la fecha 1-1-2023.");
    System.out.println();

    Calendar date = Calendar.getInstance();
    date.set(Calendar.YEAR, 2022);
    date.set(Calendar.MONTH, DECEMBER);
    date.set(Calendar.DAY_OF_MONTH, 31);

    Calendar expectedDate = Calendar.getInstance();
    expectedDate.set(Calendar.YEAR, 2023);
    expectedDate.set(Calendar.MONTH, JANUARY);
    expectedDate.set(Calendar.DAY_OF_MONTH, 1);

    /*
     * Seccion de prueba
     */
    Calendar resultingDate = UtilDate.getNextDateFromDate(date);

    System.out.println("Fecha a partir de la cual obtener la fecha inmediatamente siguiente: " + UtilDate.formatDate(date));
    System.out.println("Fecha esperada: " + UtilDate.formatDate(expectedDate));
    System.out.println("Fecha devuelta por el metodo getNextDateFromDate(): " + UtilDate.formatDate(resultingDate));
    System.out.println();

    assertTrue(UtilDate.compareTo(resultingDate, expectedDate) == 0);

    System.out.println("- Prueba pasada satisfactoriamente");
  }

  @Test
  public void testTwoGetNextDateFromDate() {
    System.out.println("**************************** Prueba dos del metodo getNextDateFromDate ****************************");
    System.out.println("El metodo getNextDateFromDate() de la clase UtilDate retorna la fecha inmediatamente siguiente a la");
    System.out.println("fecha que se le pasa como argumento.");
    System.out.println();
    System.out.println("Para demostrar el correcto funcionamiento de este metodo, en esta prueba se utiliza la fecha 29-2-2020.");
    System.out.println("Por lo tanto, el metodo getNextDateFromDate() debe retornar la fecha 1-3-2020 (año bisiesto).");
    System.out.println();

    Calendar date = Calendar.getInstance();
    date.set(Calendar.YEAR, 2020);
    date.set(Calendar.MONTH, FEBRUARY);
    date.set(Calendar.DAY_OF_MONTH, 29);

    Calendar expectedDate = Calendar.getInstance();
    expectedDate.set(Calendar.YEAR, 2020);
    expectedDate.set(Calendar.MONTH, MARCH);
    expectedDate.set(Calendar.DAY_OF_MONTH, 1);

    /*
     * Seccion de prueba
     */
    Calendar resultingDate = UtilDate.getNextDateFromDate(date);

    System.out.println("Fecha a partir de la cual obtener la fecha inmediatamente siguiente: " + UtilDate.formatDate(date));
    System.out.println("Fecha esperada: " + UtilDate.formatDate(expectedDate));
    System.out.println("Fecha devuelta por el metodo getNextDateFromDate(): " + UtilDate.formatDate(resultingDate));
    System.out.println();

    assertTrue(UtilDate.compareTo(resultingDate, expectedDate) == 0);

    System.out.println("- Prueba pasada satisfactoriamente");
  }

  @Test
  public void testThreeGetNextDateFromDate() {
    System.out.println("**************************** Prueba tres del metodo getNextDateFromDate ****************************");
    System.out.println("El metodo getNextDateFromDate() de la clase UtilDate retorna la fecha inmediatamente siguiente a la");
    System.out.println("fecha que se le pasa como argumento.");
    System.out.println();
    System.out.println("Para demostrar el correcto funcionamiento de este metodo, en esta prueba se utiliza la fecha 28-2-2020.");
    System.out.println("Por lo tanto, el metodo getNextDateFromDate() debe retornar la fecha 29-2-2020 (año bisiesto).");
    System.out.println();

    Calendar date = Calendar.getInstance();
    date.set(Calendar.YEAR, 2020);
    date.set(Calendar.MONTH, FEBRUARY);
    date.set(Calendar.DAY_OF_MONTH, 28);

    Calendar expectedDate = Calendar.getInstance();
    expectedDate.set(Calendar.YEAR, 2020);
    expectedDate.set(Calendar.MONTH, FEBRUARY);
    expectedDate.set(Calendar.DAY_OF_MONTH, 29);

    /*
     * Seccion de prueba
     */
    Calendar resultingDate = UtilDate.getNextDateFromDate(date);

    System.out.println("Fecha a partir de la cual obtener la fecha inmediatamente siguiente: " + UtilDate.formatDate(date));
    System.out.println("Fecha esperada: " + UtilDate.formatDate(expectedDate));
    System.out.println("Fecha devuelta por el metodo getNextDateFromDate(): " + UtilDate.formatDate(resultingDate));
    System.out.println();

    assertTrue(UtilDate.compareTo(resultingDate, expectedDate) == 0);

    System.out.println("- Prueba pasada satisfactoriamente");
  }

  @Test
  public void testFourGetNextDateFromDate() {
    System.out.println("**************************** Prueba cuatro del metodo getNextDateFromDate ****************************");
    System.out.println("El metodo getNextDateFromDate() de la clase UtilDate retorna la fecha inmediatamente siguiente a la");
    System.out.println("fecha que se le pasa como argumento.");
    System.out.println();
    System.out.println("Para demostrar el correcto funcionamiento de este metodo, en esta prueba se utiliza la fecha 1-1-2023.");
    System.out.println("Por lo tanto, el metodo getNextDateFromDate() debe retornar la fecha 2-1-2023.");
    System.out.println();

    Calendar date = Calendar.getInstance();
    date.set(Calendar.YEAR, 2023);
    date.set(Calendar.MONTH, JANUARY);
    date.set(Calendar.DAY_OF_MONTH, 1);

    Calendar expectedDate = Calendar.getInstance();
    expectedDate.set(Calendar.YEAR, 2023);
    expectedDate.set(Calendar.MONTH, JANUARY);
    expectedDate.set(Calendar.DAY_OF_MONTH, 2);

    /*
     * Seccion de prueba
     */
    Calendar resultingDate = UtilDate.getNextDateFromDate(date);

    System.out.println("Fecha a partir de la cual obtener la fecha inmediatamente siguiente: " + UtilDate.formatDate(date));
    System.out.println("Fecha esperada: " + UtilDate.formatDate(expectedDate));
    System.out.println("Fecha devuelta por el metodo getNextDateFromDate(): " + UtilDate.formatDate(resultingDate));
    System.out.println();

    assertTrue(UtilDate.compareTo(resultingDate, expectedDate) == 0);

    System.out.println("- Prueba pasada satisfactoriamente");
  }

}