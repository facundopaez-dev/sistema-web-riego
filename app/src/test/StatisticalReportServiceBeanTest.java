import static org.junit.Assert.*;

import java.util.Calendar;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import stateless.StatisticalReportServiceBean;
import util.UtilDate;

public class StatisticalReportServiceBeanTest {

  private static EntityManager entityManager;
  private static EntityManagerFactory entityManagerFactory;
  private static StatisticalReportServiceBean statisticalReportService;

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

  @BeforeClass
  public static void preTest() {
    entityManagerFactory = Persistence.createEntityManagerFactory("swcar");
    entityManager = entityManagerFactory.createEntityManager();

    statisticalReportService = new StatisticalReportServiceBean();
    statisticalReportService.setEntityManager(entityManager);
  }

  @Test
  public void testOneCalculateDateUntil() {
    System.out.println("******************************** Prueba uno del metodo calculateDateUntil ********************************");
    System.out.println("El metodo calculateDateUntil de la clase StatisticalReportServiceBean calcula la fecha hasta de un informe");
    System.out.println("estadistico a partir de la fecha desde y el menor ciclo de vida de los cultivos registrados en la base de");
    System.out.println("datos subyacente.");
    System.out.println();
    System.out.println("En esta prueba unitaria se demuestra que el metodo calculateDateUntil calcula la fecha hasta correctamente");
    System.out.println("en el caso en el que dicha fecha tiene el mismo año que la fecha desde. Para esto se utiliza la fecha desde");
    System.out.println("1/10/2022 y el valor 35 como el menor ciclo de vida (medido en dias).");
    System.out.println("La fecha hasta calculada a partir de estos dos valores NO es mayor a la cantidad de dias del año de la fecha");
    System.out.println("desde, por lo tanto, la fecha hasta tiene el mismo año que la fecha desde.");
    System.out.println();

    /*
     * El numero de dia en el año de la fecha 1/10/2022 es
     * 274, segun el cuadro A2.5 de la pagina 215 del libro
     * Evapotranspiracion del cultivo 56 de la FAO.
     * 
     * El metodo calculateDateUntil utiliza la siguiente
     * formula para calcular la fecha hasta:
     * 
     * numero de dia en el año de la fecha hasta =
     * numero de dia en el año de la fecha desde +
     * menor ciclo de vida - 1
     * 
     * numero de dia en el año de la fecha hasta =
     * 274 + 35 - 1 = 308
     * 
     * El valor 308 no es mayor a la cantidad de dias del
     * año 2022. Por lo tanto, la fecha hasta tiene el
     * mismo año que la fecha desde.
     * 
     * El numero de dia en el año 308 es la fecha
     * 4/11/2022. Por lo tanto, la fecha hasta es la
     * fecha 4/11/2022.
     */

    /*
     * Fecha desde a partir de la cual se calcula la
     * fecha hasta
     */
    Calendar dateFrom = Calendar.getInstance();
    dateFrom.set(Calendar.YEAR, 2022);
    dateFrom.set(Calendar.MONTH, OCTOBER);
    dateFrom.set(Calendar.DAY_OF_MONTH, 1);

    /*
     * Menor ciclo de vida utilizado para el calculo
     * de la fecha hasta
     */
    int shorterLifeCycle = 35;

    Calendar expectedDate = Calendar.getInstance();
    expectedDate.set(Calendar.YEAR, 2022);
    expectedDate.set(Calendar.MONTH, NOVEMBER);
    expectedDate.set(Calendar.DAY_OF_MONTH, 4);

    System.out.println("* Valores utilizados para calcular la fecha hasta");
    System.out.println("Fecha desde: " + UtilDate.formatDate(dateFrom));
    System.out.println("Valor utilizado como el menor ciclo de vida (medido en dias): " + shorterLifeCycle);
    System.out.println();

    /*
     * Seccion de prueba
     */
    Calendar dateUntil = statisticalReportService.calculateDateUntil(dateFrom, shorterLifeCycle);

    System.out.println("* Seccion de prueba *");
    System.out.println("Fecha hasta esperada: "  + UtilDate.formatDate(expectedDate));
    System.out.println("Fecha hasta devuelta por el metodo calculateDateUntil: "  + UtilDate.formatDate(dateUntil));
    System.out.println();

    /*
     * Si el año, el mes y el dia de la fecha esperada es igual
     * al año, el mes y el dia de la fecha hasta, la fecha hasta
     * fue calculada correctamente
     */
    assertTrue((expectedDate.get(Calendar.YEAR) == dateUntil.get(Calendar.YEAR))
        && (expectedDate.get(Calendar.MONTH) == dateUntil.get(Calendar.MONTH))
        && (expectedDate.get(Calendar.DAY_OF_YEAR) == dateUntil.get(Calendar.DAY_OF_YEAR)));

    System.out.println("- Prueba pasada satisfactoriamente");
  }

  @Test
  public void testTwoCalculateDateUntil() {
    System.out.println("******************************** Prueba dos del metodo calculateDateUntil ********************************");
    System.out.println("El metodo calculateDateUntil de la clase StatisticalReportServiceBean calcula la fecha hasta de un informe");
    System.out.println("estadistico a partir de la fecha desde y el menor ciclo de vida de los cultivos registrados en la base de");
    System.out.println("datos subyacente.");
    System.out.println();
    System.out.println("En esta prueba unitaria se demuestra que el metodo calculateDateUntil calcula la fecha hasta correctamente");
    System.out.println("en el caso en el que dicha fecha tiene un año mas que la fecha desde. Para esto se utiliza la fecha desde");
    System.out.println("1/12/2022 y el valor 35 como el menor ciclo de vida (medido en dias).");
    System.out.println("La fecha hasta calculada a partir de estos dos valores es mayor a la cantidad de dias del año de la fecha");
    System.out.println("desde, por lo tanto, la fecha hasta tiene un año mas que la fecha desde.");
    System.out.println();

    /*
     * El numero de dia en el año de la fecha 1/12/2022 es
     * 335, segun el cuadro A2.5 de la pagina 215 del libro
     * Evapotranspiracion del cultivo 56 de la FAO.
     * 
     * El metodo calculateDateUntil utiliza la siguiente
     * formula para calcular la fecha hasta:
     * 
     * numero de dia en el año de la fecha hasta =
     * numero de dia en el año de la fecha desde +
     * menor ciclo de vida - 1
     * 
     * numero de dia en el año de la fecha hasta =
     * 335 + 35 - 1 = 369
     * 
     * El valor 369 es mayor a la cantidad de dias del año
     * 2022. En este caso, el metodo calculateDateUntil
     * utiliza una segunda formula para calcular la
     * fecha hasta, y es la siguiente:
     * 
     * numero de dia en el año de la fecha hasta =
     * menor ciclo de vida - (cantidad de dias en el
     * año de la fecha desde - numero de dia en el año
     * de la fecha desde) - 1
     * 
     * numero de dia en el año de la fecha hasta =
     * 35 - (365 - 335) - 1 = 4
     * 
     * El numero de dia en el año 4 siendo 369 > 365
     * (cantidad de dias del año 2022 de la fecha desde)
     * es la fecha 4/1/2023. Por lo tanto, la fecha hasta
     * es la fecha 4/1/2023.
     */

    /*
     * Fecha desde a partir de la cual se calcula la
     * fecha hasta
     */
    Calendar dateFrom = Calendar.getInstance();
    dateFrom.set(Calendar.YEAR, 2022);
    dateFrom.set(Calendar.MONTH, DECEMBER);
    dateFrom.set(Calendar.DAY_OF_MONTH, 1);

    /*
     * Menor ciclo de vida utilizado para el calculo
     * de la fecha hasta
     */
    int shorterLifeCycle = 35;

    Calendar expectedDate = Calendar.getInstance();
    expectedDate.set(Calendar.YEAR, 2023);
    expectedDate.set(Calendar.MONTH, JANUARY);
    expectedDate.set(Calendar.DAY_OF_MONTH, 4);

    System.out.println("* Valores utilizados para calcular la fecha hasta");
    System.out.println("Fecha desde: " + UtilDate.formatDate(dateFrom));
    System.out.println("Valor utilizado como el menor ciclo de vida (medido en dias): " + shorterLifeCycle);
    System.out.println();

    /*
     * Seccion de prueba
     */
    Calendar dateUntil = statisticalReportService.calculateDateUntil(dateFrom, shorterLifeCycle);

    System.out.println("* Seccion de prueba *");
    System.out.println("Fecha hasta esperada: "  + UtilDate.formatDate(expectedDate));
    System.out.println("Fecha hasta devuelta por el metodo calculateDateUntil: "  + UtilDate.formatDate(dateUntil));
    System.out.println();

    /*
     * Si el año, el mes y el dia de la fecha esperada es igual
     * al año, el mes y el dia de la fecha hasta, la fecha hasta
     * fue calculada correctamente
     */
    assertTrue((expectedDate.get(Calendar.YEAR) == dateUntil.get(Calendar.YEAR))
        && (expectedDate.get(Calendar.MONTH) == dateUntil.get(Calendar.MONTH))
        && (expectedDate.get(Calendar.DAY_OF_YEAR) == dateUntil.get(Calendar.DAY_OF_YEAR)));

    System.out.println("- Prueba pasada satisfactoriamente");
  }

  @AfterClass
  public static void postTest() {
    // Cierra las conexiones
    entityManager.close();
    entityManagerFactory.close();
  }

}
