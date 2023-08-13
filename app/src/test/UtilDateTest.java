import static org.junit.Assert.*;

import java.util.Calendar;
import org.junit.Ignore;
import org.junit.Test;
import util.UtilDate;

public class UtilDateTest {

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

}