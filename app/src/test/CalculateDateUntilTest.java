import static org.junit.Assert.*;

import java.util.Calendar;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import stateless.CropServiceBean;
import stateless.StatisticalReportServiceBean;
import util.UtilDate;

/*
 * Para ejecutar correctamente las pruebas unitarias de esta
 * clase es necesario ejecutar el comando "ant all" (sin las
 * comillas), el cual, carga la base de datos subyacente con
 * los datos necesarios para la ejecucion de dichas pruebas
 */
public class CalculateDateUntilTest {

  private static EntityManager entityManager;
  private static EntityManagerFactory entityManagerFactory;
  private static CropServiceBean cropService;
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

    cropService = new CropServiceBean();
    cropService.setEntityManager(entityManager);

    statisticalReportService = new StatisticalReportServiceBean();
    statisticalReportService.setEntityManager(entityManager);
  }

  @Test
  public void testOneCalculatDateUntil() {
    System.out.println("**************************** Prueba uno del metodo calculateDateUntil ****************************");
    System.out.println("El metodo calculateDateUntil de la clase StatisticalReportServiceBean calcula la fecha hasta de un");
    System.out.println("informe estadistico en base a una fecha desde (incluida) y el menor ciclo de vida del ciclo de vida");
    System.out.println("de los cultivos registros en la base de datos subyacente.");
    System.out.println();
    System.out.println("Este metodo es para cuando NO se define la fecha hasta de un informe estadistico en el formulario de");
    System.out.println("generacion de un informe estadistico de una parcela.");
    System.out.println();
    System.out.println("En esta prueba unitaria se prueba que el metodo calculateDateUntil calcula correctamente la fecha");
    System.out.println("hasta dentro del año de la fecha desde.");
    System.out.println();

    /*
     * Fecha desde a partir de la cual se calcula
     * la fecha hasta
     */
    Calendar dateFrom = Calendar.getInstance();
    dateFrom.set(Calendar.YEAR, 2023);
    dateFrom.set(Calendar.MONTH, JANUARY);
    dateFrom.set(Calendar.DAY_OF_MONTH, 1);

    System.out.println("Fecha desde " + UtilDate.formatDate(dateFrom));
    System.out
        .println("Menor ciclo de vida del ciclo de vida de los cultivos registrados en la base de datos subyacente: "
            + cropService.findShortestLifeCycle());
    System.out.println();

    Calendar expectedDate = Calendar.getInstance();
    expectedDate.set(Calendar.YEAR, 2023);
    expectedDate.set(Calendar.MONTH, FEBRUARY);
    expectedDate.set(Calendar.DAY_OF_MONTH, 4);

    /*
     * Seccion de prueba
     */
    Calendar dateUntil = statisticalReportService.calculateDateUntil(dateFrom, cropService.findShortestLifeCycle());

    System.out.println("* Seccion de prueba *");
    System.out.println("Fecha hasta esperada: " + UtilDate.formatDate(expectedDate));
    System.out.println("Fecha hasta calculada por el metodo calculateDateUntil: " + UtilDate.formatDate(dateUntil));
    System.out.println();

    assertTrue((expectedDate.get(Calendar.YEAR) == dateUntil.get(Calendar.YEAR))
        && (expectedDate.get(Calendar.MONTH) == dateUntil.get(Calendar.MONTH))
        && (expectedDate.get(Calendar.DAY_OF_YEAR) == dateUntil.get(Calendar.DAY_OF_YEAR)));

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testTwoCalculatDateUntil() {
    System.out.println("**************************** Prueba dos del metodo calculateDateUntil ****************************");
    System.out.println("El metodo calculateDateUntil de la clase StatisticalReportServiceBean calcula la fecha hasta de un");
    System.out.println("informe estadistico en base a una fecha desde (incluida) y el menor ciclo de vida del ciclo de vida");
    System.out.println("de los cultivos registros en la base de datos subyacente.");
    System.out.println();
    System.out.println("Este metodo es para cuando NO se define la fecha hasta de un informe estadistico en el formulario de");
    System.out.println("generacion de un informe estadistico de una parcela.");
    System.out.println();
    System.out.println("En esta prueba unitaria se prueba que el metodo calculateDateUntil calcula correctamente la fecha");
    System.out.println("hasta con un año distinto al año de la fecha desde.");
    System.out.println();

    /*
     * Fecha desde a partir de la cual se calcula
     * la fecha hasta
     */
    Calendar dateFrom = Calendar.getInstance();
    dateFrom.set(Calendar.YEAR, 2023);
    dateFrom.set(Calendar.MONTH, DECEMBER);
    dateFrom.set(Calendar.DAY_OF_MONTH, 30);

    System.out.println("Fecha desde " + UtilDate.formatDate(dateFrom));
    System.out
        .println("Menor ciclo de vida del ciclo de vida de los cultivos registrados en la base de datos subyacente: "
            + cropService.findShortestLifeCycle());
    System.out.println();

    Calendar expectedDate = Calendar.getInstance();
    expectedDate.set(Calendar.YEAR, 2024);
    expectedDate.set(Calendar.MONTH, FEBRUARY);
    expectedDate.set(Calendar.DAY_OF_MONTH, 2);

    /*
     * Seccion de prueba
     */
    Calendar dateUntil = statisticalReportService.calculateDateUntil(dateFrom, cropService.findShortestLifeCycle());

    System.out.println("* Seccion de prueba *");
    System.out.println("Fecha hasta esperada: " + UtilDate.formatDate(expectedDate));
    System.out.println("Fecha hasta calculada por el metodo calculateDateUntil: " + UtilDate.formatDate(dateUntil));
    System.out.println();

    assertTrue((expectedDate.get(Calendar.YEAR) == dateUntil.get(Calendar.YEAR))
        && (expectedDate.get(Calendar.MONTH) == dateUntil.get(Calendar.MONTH))
        && (expectedDate.get(Calendar.DAY_OF_YEAR) == dateUntil.get(Calendar.DAY_OF_YEAR)));

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @AfterClass
  public static void postTest() {
    // Cierra las conexiones
    entityManager.close();
    entityManagerFactory.close();
  }

}