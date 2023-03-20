import static org.junit.Assert.*;

import climate.ClimateClient;
import java.util.Calendar;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import model.ClimateRecord;
import model.Parcel;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import stateless.ParcelServiceBean;
import stateless.ClimateRecordServiceBean;
import util.UtilDate;

/*
 * Para que las pruebas unitarias sean ejecutadas correctamente
 * es necesario ejecutar el comando "ant t105" (sin las comillas),
 * ya que este se ocupa de cargar la base de datos subyacente con
 * los datos que se necesitan para probar el correcto funcionamiento
 * del metodo calculateAmountRainwaterByPeriod de la clase
 * ClimateRecordServiceBean
 */
public class AmountRainwaterTest {

  private static EntityManager entityManager;
  private static EntityManagerFactory entityManagerFactory;
  private static ParcelServiceBean parcelService;
  private static ClimateRecordServiceBean climateRecordService;

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

    parcelService = new ParcelServiceBean();
    parcelService.setEntityManager(entityManager);

    climateRecordService = new ClimateRecordServiceBean();
    climateRecordService.setEntityManager(entityManager);
  }

  @Test
  public void testOneCalculateAmountRainwaterByPeriod() {
    System.out.println("****************************** Prueba uno del metodo calculateAmountRainwaterByPeriod ******************************");
    System.out.println("- Los registros climaticos comprendidos en un periodo definido por dos fechas, tienen una cantidad de agua de lluvia");
    System.out.println("igual a cero. Por lo tanto, la cantidad total de agua de lluvia es cero.");
    System.out.println();
    System.out.println("Dada una parcela con 30 registros climaticos, teniendo cada uno de ellos una cantidad de agua de lluvia [mm/dia] igual");
    System.out.println("a cero, y un periodo de fechas que abarca los 30 registros climaticos, la aplicacion debera retornar 0 mm/periodo como la");
    System.out.println("cantidad total de agua de lluvia que cayo sobre la parcela en el periodo dado.");
    System.out.println();

    Parcel givenParcel = parcelService.find(1);
    System.out.println("* Datos de la parcela de prueba");
    System.out.println(givenParcel);

    /*
     * Fechas que definen el periodo en el cual se obtendra
     * la cantidad total de agua de lluvia que cayo sobre
     * una parcela
     */
    Calendar dateFrom = Calendar.getInstance();
    dateFrom.set(Calendar.YEAR, 2023);
    dateFrom.set(Calendar.MONTH, JANUARY);
    dateFrom.set(Calendar.DAY_OF_MONTH, 1);

    Calendar dateUntil = Calendar.getInstance();
    dateUntil.set(Calendar.YEAR, 2023);
    dateUntil.set(Calendar.MONTH, JANUARY);
    dateUntil.set(Calendar.DAY_OF_MONTH, 30);

    System.out.println("* Periodo en el cual se obtendra la cantidad total de agua de lluvia que cayo sobre la parcela con ID = " + givenParcel.getId());
    System.out.println("[" + UtilDate.formatDate(dateFrom) + ", " + UtilDate.formatDate(dateUntil) + "]");
    System.out.println();

    /*
     * Seccion de prueba
     */
    double expectedResult = 0.0;
    double result = climateRecordService.calculateAmountRainwaterByPeriod(givenParcel.getId(), dateFrom, dateUntil);

    System.out.println("Resultado esperado: " + expectedResult);
    System.out.println("* Resultado devuelto por el metodo calculateAmountRainwaterByPeriod: " + result);
    System.out.println();

    assertEquals(expectedResult, result, 1e-8);

    System.out.println("- Prueba pasada satisfactoriamente");
  }

  @Test
  public void testTwoCalculateAmountRainwaterByPeriod() {
    System.out.println("****************************** Prueba dos del metodo calculateAmountRainwaterByPeriod ******************************");
    System.out.println("-  Los registros climaticos comprendidos en un periodo definido por dos fechas, tienen distintas cantidades de agua de");
    System.out.println("lluvia. Por lo tanto, la cantidad total de agua de lluvia es distinta de cero.");
    System.out.println();
    System.out.println("Dada una parcela con 30 registros climaticos, teniendo 10 de ellos 1 mm/dia de agua de lluvia, 5 de ellos 0 mm/dia de");
    System.out.println("agua de lluvia, 5 de ellos 1.2 mm/dia de agua de lluvia, 7 de ellos 0.5 mm/dia de agua de lluvia y 3 de ellos 0.2 mm/dia");
    System.out.println("de agua de lluvia, y un periodo de fechas que abarca los 30 registros climaticos, la aplicacion debera retornar");
    System.out.println("20.1 mm/periodo como la cantidad total de agua de lluvia que cayo sobre la parcela en el periodo dado.");
    System.out.println();

    Parcel givenParcel = parcelService.find(2);
    System.out.println("* Datos de la parcela de prueba");
    System.out.println(givenParcel);

    /*
     * Fechas que definen el periodo en el cual se obtendra
     * la cantidad total de agua de lluvia que cayo sobre
     * una parcela
     */
    Calendar dateFrom = Calendar.getInstance();
    dateFrom.set(Calendar.YEAR, 2023);
    dateFrom.set(Calendar.MONTH, JANUARY);
    dateFrom.set(Calendar.DAY_OF_MONTH, 1);

    Calendar dateUntil = Calendar.getInstance();
    dateUntil.set(Calendar.YEAR, 2023);
    dateUntil.set(Calendar.MONTH, JANUARY);
    dateUntil.set(Calendar.DAY_OF_MONTH, 30);

    System.out.println("* Periodo en el cual se obtendra la cantidad total de agua de lluvia que cayo sobre la parcela con ID = " + givenParcel.getId());
    System.out.println("[" + UtilDate.formatDate(dateFrom) + ", " + UtilDate.formatDate(dateUntil) + "]");
    System.out.println();

    /*
     * Seccion de prueba
     */
    double expectedResult = 20.1;
    double result = climateRecordService.calculateAmountRainwaterByPeriod(givenParcel.getId(), dateFrom, dateUntil);

    System.out.println("Resultado esperado: " + expectedResult);
    System.out.println("* Resultado devuelto por el metodo calculateAmountRainwaterByPeriod: " + result);
    System.out.println();

    assertEquals(expectedResult, result, 1e-8);

    System.out.println("- Prueba pasada satisfactoriamente");
  }

  @Test
  public void testThreeCalculateAmountRainwaterByPeriod() {
    System.out.println("****************************** Prueba tres del metodo calculateAmountRainwaterByPeriod ******************************");
    System.out.println("- No existen registros climaticos asociados a una parcela. Por lo tanto, la cantidad total de agua de lluvia es -1.0");
    System.out.println("(valor que representa informacion no disponible).");
    System.out.println();
    System.out.println("Dada una parcela que no tiene registros climaticos en un periodo definido por dos fechas, la aplicacion debera retornar");
    System.out.println("-1.0 como valor indicativo de que la cantidad total de agua de lluvia en el periodo dado no esta disponible.");
    System.out.println();

    Parcel givenParcel = parcelService.find(3);
    System.out.println("* Datos de la parcela de prueba");
    System.out.println(givenParcel);

    /*
     * Fechas que definen el periodo en el cual se obtendra
     * la cantidad total de agua de lluvia que cayo sobre
     * una parcela
     */
    Calendar dateFrom = Calendar.getInstance();
    dateFrom.set(Calendar.YEAR, 2023);
    dateFrom.set(Calendar.MONTH, JANUARY);
    dateFrom.set(Calendar.DAY_OF_MONTH, 1);

    Calendar dateUntil = Calendar.getInstance();
    dateUntil.set(Calendar.YEAR, 2023);
    dateUntil.set(Calendar.MONTH, JANUARY);
    dateUntil.set(Calendar.DAY_OF_MONTH, 30);

    System.out.println("* Periodo en el cual se obtendra la cantidad total de agua de lluvia que cayo sobre la parcela con ID = " + givenParcel.getId());
    System.out.println("[" + UtilDate.formatDate(dateFrom) + ", " + UtilDate.formatDate(dateUntil) + "]");
    System.out.println();

    /*
     * Seccion de prueba
     */
    double expectedResult = -1.0;
    double result = climateRecordService.calculateAmountRainwaterByPeriod(givenParcel.getId(), dateFrom, dateUntil);

    System.out.println("Resultado esperado: " + expectedResult);
    System.out.println("* Resultado devuelto por el metodo calculateAmountRainwaterByPeriod: " + result);
    System.out.println();

    assertEquals(expectedResult, result, 1e-8);

    System.out.println("- Prueba pasada satisfactoriamente");
  }

  @AfterClass
  public static void postTest() {
    // Cierra las conexiones
    entityManager.close();
    entityManagerFactory.close();
  }

}
