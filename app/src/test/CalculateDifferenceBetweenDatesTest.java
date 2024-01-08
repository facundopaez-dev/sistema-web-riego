import static org.junit.Assert.*;

import java.util.Calendar;
import org.junit.Ignore;
import org.junit.Test;
import util.UtilDate;

/*
 * Esta clase contiene pruebas unitarias del metodo
 * calculateDifferenceBetweenDates de la clase UtilDate
 */
public class CalculateDifferenceBetweenDatesTest {

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
  public void testOne() {
    System.out.println("********************* Prueba uno del metodo calculateDifferenceBetweenDates *********************");
    System.out.println("El metodo calculateDifferenceBetweenDates de la clase UtilDate calcula la diferencia de dias que");
    System.out.println("hay entre dos fechas dadas.");
    System.out.println();
    System.out.println("En esta prueba se demuestra que el metodo calculateDifferenceBetweenDates calcula correctamente");
    System.out.println("la diferencia de dias que hay entre dos fechas dadas cuando dos fechas estan en el mismo año.");
    System.out.println();

    Calendar dateOne = Calendar.getInstance();
    dateOne.set(Calendar.YEAR, 2023);
    dateOne.set(Calendar.MONTH, JANUARY);
    dateOne.set(Calendar.DAY_OF_MONTH, 1);

    Calendar dateTwo = Calendar.getInstance();
    dateTwo.set(Calendar.YEAR, 2023);
    dateTwo.set(Calendar.MONTH, JANUARY);
    dateTwo.set(Calendar.DAY_OF_MONTH, 31);

    System.out.println("Fechas utilizadas para la prueba");
    System.out.println("Fecha uno: " + UtilDate.formatDate(dateOne));
    System.out.println("Fecha dos: " + UtilDate.formatDate(dateTwo));
    System.out.println();

    /*
     * Seccion de prueba
     */
    int expectedResult = 30;
    int daysDifference = UtilDate.calculateDifferenceBetweenDates(dateOne, dateTwo);

    System.out.println("* Seccion de prueba *");
    System.out.println("Resultado esperado: " + expectedResult);
    System.out.println("Resultado devuelto por el metodo calculateDifferenceBetweenDates: " + daysDifference);
    System.out.println();

    assertTrue(expectedResult == daysDifference);

    System.out.println("- Prueba pasada satisfactoriamente");
  }

  @Test
  public void testTwo() {
    System.out.println("********************* Prueba dos del metodo calculateDifferenceBetweenDates *********************");
    System.out.println("El metodo calculateDifferenceBetweenDates de la clase UtilDate calcula la diferencia de dias que");
    System.out.println("hay entre dos fechas dadas.");
    System.out.println();
    System.out.println("En esta prueba se demuestra que el metodo calculateDifferenceBetweenDates calcula correctamente");
    System.out.println("la diferencia de dias que hay entre dos fechas dadas cuando el año de una fecha es mayor por una");
    System.out.println("unidad al año de la otra fecha.");
    System.out.println();

    Calendar dateOne = Calendar.getInstance();
    dateOne.set(Calendar.YEAR, 2022);
    dateOne.set(Calendar.MONTH, DECEMBER);
    dateOne.set(Calendar.DAY_OF_MONTH, 1);

    Calendar dateTwo = Calendar.getInstance();
    dateTwo.set(Calendar.YEAR, 2023);
    dateTwo.set(Calendar.MONTH, JANUARY);
    dateTwo.set(Calendar.DAY_OF_MONTH, 31);

    System.out.println("Fechas utilizadas para la prueba");
    System.out.println("Fecha uno: " + UtilDate.formatDate(dateOne));
    System.out.println("Fecha dos: " + UtilDate.formatDate(dateTwo));
    System.out.println();

    /*
     * Seccion de prueba
     */
    int expectedResult = 61;
    int daysDifference = UtilDate.calculateDifferenceBetweenDates(dateOne, dateTwo);

    System.out.println("* Seccion de prueba *");
    System.out.println("Resultado esperado: " + expectedResult);
    System.out.println("Resultado devuelto por el metodo calculateDifferenceBetweenDates: " + daysDifference);
    System.out.println();

    assertTrue(expectedResult == daysDifference);

    System.out.println("- Prueba pasada satisfactoriamente");
  }

  @Test
  public void testThree() {
    System.out.println("********************* Prueba tres del metodo calculateDifferenceBetweenDates *********************");
    System.out.println("El metodo calculateDifferenceBetweenDates de la clase UtilDate calcula la diferencia de dias que");
    System.out.println("hay entre dos fechas dadas.");
    System.out.println();
    System.out.println("En esta prueba se demuestra que el metodo calculateDifferenceBetweenDates calcula correctamente la");
    System.out.println("diferencia de dias que hay entre dos fechas dadas cuando hay mas de una unidad de diferencia entre");
    System.out.println("el año de una fecha y el año de otra fecha.");
    System.out.println();

    Calendar dateOne = Calendar.getInstance();
    dateOne.set(Calendar.YEAR, 2021);
    dateOne.set(Calendar.MONTH, DECEMBER);
    dateOne.set(Calendar.DAY_OF_MONTH, 1);

    Calendar dateTwo = Calendar.getInstance();
    dateTwo.set(Calendar.YEAR, 2024);
    dateTwo.set(Calendar.MONTH, JANUARY);
    dateTwo.set(Calendar.DAY_OF_MONTH, 16);

    System.out.println("Fechas utilizadas para la prueba");
    System.out.println("Fecha uno: " + UtilDate.formatDate(dateOne));
    System.out.println("Fecha dos: " + UtilDate.formatDate(dateTwo));
    System.out.println();

    /*
     * Seccion de prueba
     */
    int expectedResult = 776;
    int daysDifference = UtilDate.calculateDifferenceBetweenDates(dateOne, dateTwo);

    System.out.println("* Seccion de prueba *");
    System.out.println("Resultado esperado: " + expectedResult);
    System.out.println("Resultado devuelto por el metodo calculateDifferenceBetweenDates: " + daysDifference);
    System.out.println();

    assertTrue(expectedResult == daysDifference);

    System.out.println("- Prueba pasada satisfactoriamente");
  }

  @Test
  public void testFour() {
    System.out.println("********************* Prueba cuatro del metodo calculateDifferenceBetweenDates *********************");
    System.out.println("El metodo calculateDifferenceBetweenDates de la clase UtilDate calcula la diferencia de dias que");
    System.out.println("hay entre dos fechas dadas.");
    System.out.println();
    System.out.println("En esta prueba se demuestra que el metodo calculateDifferenceBetweenDates calcula correctamente la");
    System.out.println("diferencia de dias que hay entre dos fechas dadas cuando el año de una fecha es mayor por una");
    System.out.println("unidad al año de la otra fecha, y el año de la primera fecha es bisiesto.");
    System.out.println();

    Calendar dateOne = Calendar.getInstance();
    dateOne.set(Calendar.YEAR, 2024);
    dateOne.set(Calendar.MONTH, JANUARY);
    dateOne.set(Calendar.DAY_OF_MONTH, 1);

    Calendar dateTwo = Calendar.getInstance();
    dateTwo.set(Calendar.YEAR, 2025);
    dateTwo.set(Calendar.MONTH, JANUARY);
    dateTwo.set(Calendar.DAY_OF_MONTH, 1);

    System.out.println("Fechas utilizadas para la prueba");
    System.out.println("Fecha uno: " + UtilDate.formatDate(dateOne));
    System.out.println("Fecha dos: " + UtilDate.formatDate(dateTwo));
    System.out.println();

    /*
     * Seccion de prueba
     */
    int expectedResult = 366;
    int daysDifference = UtilDate.calculateDifferenceBetweenDates(dateOne, dateTwo);

    System.out.println("* Seccion de prueba *");
    System.out.println("Resultado esperado: " + expectedResult);
    System.out.println("Resultado devuelto por el metodo calculateDifferenceBetweenDates: " + daysDifference);
    System.out.println();

    assertTrue(expectedResult == daysDifference);

    System.out.println("- Prueba pasada satisfactoriamente");
  }

  @Test
  public void testFive() {
    System.out.println("********************* Prueba cinco del metodo calculateDifferenceBetweenDates *********************");
    System.out.println("El metodo calculateDifferenceBetweenDates de la clase UtilDate calcula la diferencia de dias que");
    System.out.println("hay entre dos fechas dadas.");
    System.out.println();
    System.out.println("En esta prueba se demuestra que el metodo calculateDifferenceBetweenDates calcula correctamente la");
    System.out.println("diferencia de dias que hay entre dos fechas dadas cuando hay mas de una unidad de diferencia entre");
    System.out.println("el año de una fecha y el año de otra fecha, y el año de la segunda fecha es bisiesto.");
    System.out.println();

    Calendar dateOne = Calendar.getInstance();
    dateOne.set(Calendar.YEAR, 2021);
    dateOne.set(Calendar.MONTH, DECEMBER);
    dateOne.set(Calendar.DAY_OF_MONTH, 1);

    Calendar dateTwo = Calendar.getInstance();
    dateTwo.set(Calendar.YEAR, 2024);
    dateTwo.set(Calendar.MONTH, MARCH);
    dateTwo.set(Calendar.DAY_OF_MONTH, 1);

    System.out.println("Fechas utilizadas para la prueba");
    System.out.println("Fecha uno: " + UtilDate.formatDate(dateOne));
    System.out.println("Fecha dos: " + UtilDate.formatDate(dateTwo));
    System.out.println();

    /*
     * Seccion de prueba
     */
    int expectedResult = 821;
    int daysDifference = UtilDate.calculateDifferenceBetweenDates(dateOne, dateTwo);

    System.out.println("* Seccion de prueba *");
    System.out.println("Resultado esperado: " + expectedResult);
    System.out.println("Resultado devuelto por el metodo calculateDifferenceBetweenDates: " + daysDifference);
    System.out.println();

    assertTrue(expectedResult == daysDifference);

    System.out.println("- Prueba pasada satisfactoriamente");
  }

  @Test
  public void testSix() {
    System.out.println("********************* Prueba seis del metodo calculateDifferenceBetweenDates *********************");
    System.out.println("El metodo calculateDifferenceBetweenDates de la clase UtilDate calcula la diferencia de dias que");
    System.out.println("hay entre dos fechas dadas.");
    System.out.println();
    System.out.println("En esta prueba se demuestra que el metodo calculateDifferenceBetweenDates calcula correctamente la");
    System.out.println("diferencia de dias que hay entre dos fechas dadas cuando hay mas de una unidad de diferencia entre");
    System.out.println("el año de una fecha y el año de otra fecha, y el año de la segunda fecha es bisiesto.");
    System.out.println();

    Calendar dateOne = Calendar.getInstance();
    dateOne.set(Calendar.YEAR, 2019);
    dateOne.set(Calendar.MONTH, JANUARY);
    dateOne.set(Calendar.DAY_OF_MONTH, 1);

    Calendar dateTwo = Calendar.getInstance();
    dateTwo.set(Calendar.YEAR, 2024);
    dateTwo.set(Calendar.MONTH, JANUARY);
    dateTwo.set(Calendar.DAY_OF_MONTH, 31);

    System.out.println("Fechas utilizadas para la prueba");
    System.out.println("Fecha uno: " + UtilDate.formatDate(dateOne));
    System.out.println("Fecha dos: " + UtilDate.formatDate(dateTwo));
    System.out.println();

    /*
     * Seccion de prueba
     */
    int expectedResult = 1856;
    int daysDifference = UtilDate.calculateDifferenceBetweenDates(dateOne, dateTwo);

    System.out.println("* Seccion de prueba *");
    System.out.println("Resultado esperado: " + expectedResult);
    System.out.println("Resultado devuelto por el metodo calculateDifferenceBetweenDates: " + daysDifference);
    System.out.println();

    assertTrue(expectedResult == daysDifference);

    System.out.println("- Prueba pasada satisfactoriamente");
  }

}
