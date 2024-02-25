import static org.junit.Assert.*;

import java.util.Calendar;
import org.junit.Ignore;
import org.junit.Test;
import util.UtilDate;

public class CalculateDateUntilTest {

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
  public void testOneCalculatDateUntil() {
    /*
     * Datos a partir de los cuales se calcula la
     * fecha hasta
     */
    int amountDays = 5;
    Calendar dateFrom = Calendar.getInstance();
    dateFrom.set(Calendar.YEAR, 2023);
    dateFrom.set(Calendar.MONTH, DECEMBER);
    dateFrom.set(Calendar.DAY_OF_MONTH, 25);

    Calendar expectedDate = Calendar.getInstance();
    expectedDate.set(Calendar.YEAR, 2023);
    expectedDate.set(Calendar.MONTH, DECEMBER);
    expectedDate.set(Calendar.DAY_OF_MONTH, 30);

    System.out.println("**************************** Prueba uno del metodo calculateDateUntil ****************************");
    printDescriptionMethodToTest();
    System.out.println();
    System.out.println("# Descripcion de la prueba unitaria");
    System.out.println("Para demostrar el correcto funcionamiento del metodo calculateDateUntil() en esta prueba se utiliza");
    System.out.print("la fecha desde " + UtilDate.formatDate(dateFrom) + " y la cantidad " + amountDays + ". Por lo tanto, el ");
    System.out.println("metodo calculateDateUntil() debe retornar");
    System.out.println("la fecha " + UtilDate.formatDate(expectedDate) + ".");
    System.out.println();

    /*
     * Seccion de prueba
     */
    Calendar dateUntil = UtilDate.calculateDateUntil(dateFrom, amountDays);

    System.out.println("* Seccion de prueba *");
    System.out.println("Fecha desde: " + UtilDate.formatDate(dateFrom));
    System.out.println("Cantidad (dias): " + amountDays);
    System.out.println("Fecha hasta esperada: " + UtilDate.formatDate(expectedDate));
    System.out.println("Fecha hasta calculada por el metodo calculateDateUntil(): " + UtilDate.formatDate(dateUntil));
    System.out.println();

    assertTrue((expectedDate.get(Calendar.YEAR) == dateUntil.get(Calendar.YEAR))
        && (expectedDate.get(Calendar.MONTH) == dateUntil.get(Calendar.MONTH))
        && (expectedDate.get(Calendar.DAY_OF_YEAR) == dateUntil.get(Calendar.DAY_OF_YEAR)));

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testTwoCalculatDateUntil() {
    /*
     * Datos a partir de los cuales se calcula la
     * fecha hasta
     */
    int amountDays = 15;
    Calendar dateFrom = Calendar.getInstance();
    dateFrom.set(Calendar.YEAR, 2023);
    dateFrom.set(Calendar.MONTH, DECEMBER);
    dateFrom.set(Calendar.DAY_OF_MONTH, 25);

    Calendar expectedDate = Calendar.getInstance();
    expectedDate.set(Calendar.YEAR, 2024);
    expectedDate.set(Calendar.MONTH, JANUARY);
    expectedDate.set(Calendar.DAY_OF_MONTH, 9);

    System.out.println("**************************** Prueba dos del metodo calculateDateUntil ****************************");
    printDescriptionMethodToTest();
    System.out.println();
    System.out.println("# Descripcion de la prueba unitaria");
    System.out.println("Para demostrar el correcto funcionamiento del metodo calculateDateUntil() en esta prueba se utiliza");
    System.out.print("la fecha desde " + UtilDate.formatDate(dateFrom) + " y la cantidad " + amountDays + ". Por lo tanto, el ");
    System.out.println("metodo calculateDateUntil() debe retornar");
    System.out.println("la fecha " + UtilDate.formatDate(expectedDate) + ".");
    System.out.println();

    /*
     * Seccion de prueba
     */
    Calendar dateUntil = UtilDate.calculateDateUntil(dateFrom, amountDays);

    System.out.println("* Seccion de prueba *");
    System.out.println("Fecha desde: " + UtilDate.formatDate(dateFrom));
    System.out.println("Cantidad (dias): " + amountDays);
    System.out.println("Fecha hasta esperada: " + UtilDate.formatDate(expectedDate));
    System.out.println("Fecha hasta calculada por el metodo calculateDateUntil(): " + UtilDate.formatDate(dateUntil));
    System.out.println();

    assertTrue((expectedDate.get(Calendar.YEAR) == dateUntil.get(Calendar.YEAR))
        && (expectedDate.get(Calendar.MONTH) == dateUntil.get(Calendar.MONTH))
        && (expectedDate.get(Calendar.DAY_OF_YEAR) == dateUntil.get(Calendar.DAY_OF_YEAR)));

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testThreeCalculatDateUntil() {
    /*
     * Datos a partir de los cuales se calcula la
     * fecha hasta
     */
    int amountDays = 28;
    Calendar dateFrom = Calendar.getInstance();
    dateFrom.set(Calendar.YEAR, 2020);
    dateFrom.set(Calendar.MONTH, FEBRUARY);
    dateFrom.set(Calendar.DAY_OF_MONTH, 1);

    Calendar expectedDate = Calendar.getInstance();
    expectedDate.set(Calendar.YEAR, 2020);
    expectedDate.set(Calendar.MONTH, FEBRUARY);
    expectedDate.set(Calendar.DAY_OF_MONTH, 29);

    System.out.println("**************************** Prueba tres del metodo calculateDateUntil ****************************");
    printDescriptionMethodToTest();
    System.out.println();
    System.out.println("# Descripcion de la prueba unitaria");
    System.out.println("Para demostrar el correcto funcionamiento del metodo calculateDateUntil() en esta prueba se utiliza");
    System.out.print("la fecha desde " + UtilDate.formatDate(dateFrom) + " y la cantidad " + amountDays + ". Por lo tanto, el ");
    System.out.println("metodo calculateDateUntil() debe retornar");
    System.out.println("la fecha " + UtilDate.formatDate(expectedDate) + ".");
    System.out.println();

    /*
     * Seccion de prueba
     */
    Calendar dateUntil = UtilDate.calculateDateUntil(dateFrom, amountDays);

    System.out.println("* Seccion de prueba *");
    System.out.println("Fecha desde: " + UtilDate.formatDate(dateFrom));
    System.out.println("Cantidad (dias): " + amountDays);
    System.out.println("Fecha hasta esperada: " + UtilDate.formatDate(expectedDate));
    System.out.println("Fecha hasta calculada por el metodo calculateDateUntil(): " + UtilDate.formatDate(dateUntil));
    System.out.println();

    assertTrue((expectedDate.get(Calendar.YEAR) == dateUntil.get(Calendar.YEAR))
        && (expectedDate.get(Calendar.MONTH) == dateUntil.get(Calendar.MONTH))
        && (expectedDate.get(Calendar.DAY_OF_YEAR) == dateUntil.get(Calendar.DAY_OF_YEAR)));

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testFourCalculatDateUntil() {
    /*
     * Datos a partir de los cuales se calcula la
     * fecha hasta
     */
    int amountDays = 90;
    Calendar dateFrom = Calendar.getInstance();
    dateFrom.set(Calendar.YEAR, 2019);
    dateFrom.set(Calendar.MONTH, DECEMBER);
    dateFrom.set(Calendar.DAY_OF_MONTH, 1);

    Calendar expectedDate = Calendar.getInstance();
    expectedDate.set(Calendar.YEAR, 2020);
    expectedDate.set(Calendar.MONTH, FEBRUARY);
    expectedDate.set(Calendar.DAY_OF_MONTH, 29);

    System.out.println("**************************** Prueba cuatro del metodo calculateDateUntil ****************************");
    printDescriptionMethodToTest();
    System.out.println();
    System.out.println("# Descripcion de la prueba unitaria");
    System.out.println("Para demostrar el correcto funcionamiento del metodo calculateDateUntil() en esta prueba se utiliza");
    System.out.print("la fecha desde " + UtilDate.formatDate(dateFrom) + " y la cantidad " + amountDays + ". Por lo tanto, el ");
    System.out.println("metodo calculateDateUntil() debe retornar");
    System.out.println("la fecha " + UtilDate.formatDate(expectedDate) + ".");
    System.out.println();

    /*
     * Seccion de prueba
     */
    Calendar dateUntil = UtilDate.calculateDateUntil(dateFrom, amountDays);

    System.out.println("* Seccion de prueba *");
    System.out.println("Fecha desde: " + UtilDate.formatDate(dateFrom));
    System.out.println("Cantidad (dias): " + amountDays);
    System.out.println("Fecha hasta esperada: " + UtilDate.formatDate(expectedDate));
    System.out.println("Fecha hasta calculada por el metodo calculateDateUntil(): " + UtilDate.formatDate(dateUntil));
    System.out.println();

    assertTrue((expectedDate.get(Calendar.YEAR) == dateUntil.get(Calendar.YEAR))
        && (expectedDate.get(Calendar.MONTH) == dateUntil.get(Calendar.MONTH))
        && (expectedDate.get(Calendar.DAY_OF_YEAR) == dateUntil.get(Calendar.DAY_OF_YEAR)));

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  /**
   * Imprime por pantalla la descripcion del metodo a probar
   */
  public void printDescriptionMethodToTest() {
    System.out.println("# Descripcion del metodo a probar");
    System.out.println("El metodo calculateDateUntil() de la clase UtilDate calcula una fecha a partir de la suma entre el");
    System.out.println("numero de dia en el año de una fecha y una cantidad (dias). A la fecha calculada se la conoce como");
    System.out.println("fecha hasta y a la fecha a partir de la cual se la calcula se conoce como fecha desde.");
  }

}