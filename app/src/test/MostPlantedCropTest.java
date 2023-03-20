import static org.junit.Assert.*;

import java.util.Calendar;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import stateless.ParcelServiceBean;
import stateless.PlantingRecordServiceBean;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import model.Parcel;
import util.UtilDate;

/*
 * Para que las pruebas unitarias sean ejecutadas correctamente
 * es necesario ejecutar el comando "ant t100" (sin las comillas),
 * ya que este se ocupa de cargar la base de datos subyacente con
 * los datos que se necesitan para probar el correcto funcionamiento
 * del metodo findMostPlantedCrop (y, por ende, del metodo
 * searchMostPlantedCrop) de la clase PlantingRecordServiceBean
 */
public class MostPlantedCropTest {

  private static EntityManager entityManager;
  private static EntityManagerFactory entityManagerFactory;
  private static PlantingRecordServiceBean plantingRecordService;
  private static ParcelServiceBean parcelService;

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

    plantingRecordService = new PlantingRecordServiceBean();
    plantingRecordService.setEntityManager(entityManager);

    parcelService = new ParcelServiceBean();
    parcelService.setEntityManager(entityManager);
  }

  @Test
  public void testOneFindMostPlantedCrop() {
    System.out.println("********************************** Prueba uno del metodo findMostPlantedCrop **********************************");
    System.out.println("- Existe el cultivo que mas veces fue plantado porque hay un cultivo que tiene mayoria de registros de");
    System.out.println("plantacion finalizados en un periodo de fechas.");
    System.out.println();
    System.out.println("Dada una parcela con 30 registros de plantacion, todos ellos en el estado 'Finalizado', de los cuales 5 son de");
    System.out.println("tomate, 10 de patata y 15 de alfalfa, y un periodo de fechas que abarca los 30 registros de plantacion, la");
    System.out.println("aplicacion debera retornar 'Alfalfa' como el cultivo que mas se planto en la parcela dada en el periodo dado.");
    System.out.println();

    /*
     * Parcela de prueba
     */
    Parcel givenParcel = parcelService.find(1);

    System.out.println("* Parcela de prueba");
    System.out.println(givenParcel);

    System.out.println("Cantidad de registros de plantacion finalizados de esta parcela: " + plantingRecordService.findAll(givenParcel).size());
    System.out.println();

    /*
     * Periodo dado por dos fechas en el cual se obtendra
     * el cultivo que mas veces fue plantado de los cultivos
     * plantados en una parcela
     */
    Calendar dateFrom = Calendar.getInstance();
    dateFrom.set(Calendar.YEAR, 2023);
    dateFrom.set(Calendar.MONTH, APRIL);
    dateFrom.set(Calendar.DAY_OF_MONTH, 1);

    Calendar dateUntil = Calendar.getInstance();
    dateUntil.set(Calendar.YEAR, 2043);
    dateUntil.set(Calendar.MONTH, SEPTEMBER);
    dateUntil.set(Calendar.DAY_OF_MONTH, 22);

    System.out.println("* Periodo en el cual se obtendra el cultivo que mas veces fue plantado en la parcela con ID = " + givenParcel.getId());
    System.out.println("Fecha desde: " + UtilDate.formatDate(dateFrom));
    System.out.println("Fecha hasta: " + UtilDate.formatDate(dateUntil));
    System.out.println();

    /*
     * Seccion de prueba
     */
    String expectedResult = "Alfalfa";
    String result = plantingRecordService.findMostPlantedCrop(givenParcel.getId(), dateFrom, dateUntil);

    System.out.println("Resultado esperado: " + expectedResult);
    System.out.println("* Valor devuelto por el metodo findMostPlantedCrop: " + result);
    System.out.println();

    assertTrue(expectedResult.equals(result));

    System.out.println("* Prueba pasada satisfactoriamente *");
  }

  @Test
  public void testTwoFindMostPlantedCrop() {
    System.out.println("********************************** Prueba dos del metodo findMostPlantedCrop **********************************");
    System.out.println("- Existe el cultivo que mas veces fue plantado porque hay un cultivo que es el unico que tiene registros de");
    System.out.println("plantacion finalizados en un periodo de fechas.");
    System.out.println();
    System.out.println("Dada una parcela con 10 registros de plantacion, todos ellos en el estado 'Finalizado', y teniendo al cultivo");
    System.out.println("tomate, y un periodo de fechas que abarca los 10 registros de plantacion, la aplicacion debera retornar 'Tomate'");
    System.out.println("como el cultivo que mas se planto en la parcela dada en el periodo dado.");
    System.out.println();

    /*
     * Parcela de prueba
     */
    Parcel givenParcel = parcelService.find(2);

    System.out.println("* Parcela de prueba");
    System.out.println(givenParcel);

    System.out.println("Cantidad de registros de plantacion finalizados de esta parcela: " + plantingRecordService.findAll(givenParcel).size());
    System.out.println();

    /*
     * Periodo dado por dos fechas en el cual se obtendra
     * el cultivo que mas veces fue plantado de los cultivos
     * plantados en una parcela
     */
    Calendar dateFrom = Calendar.getInstance();
    dateFrom.set(Calendar.YEAR, 2023);
    dateFrom.set(Calendar.MONTH, APRIL);
    dateFrom.set(Calendar.DAY_OF_MONTH, 1);

    Calendar dateUntil = Calendar.getInstance();
    dateUntil.set(Calendar.YEAR, 2027);
    dateUntil.set(Calendar.MONTH, JULY);
    dateUntil.set(Calendar.DAY_OF_MONTH, 22);

    System.out.println("* Periodo en el cual se obtendra el cultivo que mas veces fue plantado en la parcela con ID = " + givenParcel.getId());
    System.out.println("Fecha desde: " + UtilDate.formatDate(dateFrom));
    System.out.println("Fecha hasta: " + UtilDate.formatDate(dateUntil));
    System.out.println();

    /*
     * Seccion de prueba
     */
    String expectedResult = "Tomate";
    String result = plantingRecordService.findMostPlantedCrop(givenParcel.getId(), dateFrom, dateUntil);

    System.out.println("Resultado esperado: " + expectedResult);
    System.out.println("* Valor devuelto por el metodo findMostPlantedCrop: " + result);
    System.out.println();

    assertTrue(expectedResult.equals(result));

    System.out.println("* Prueba pasada satisfactoriamente *");
  }

  @Test
  public void testThreeFindMostPlantedCrop() {
    System.out.println("********************************** Prueba tres del metodo findMostPlantedCrop **********************************");
    System.out.println("- No existe el cultivo que mas veces fue plantado porque se tiene igual cantidad de registros de plantacion");
    System.out.println("finalizados para cada cultivo en un periodo de fechas.");
    System.out.println();
    System.out.println("Dada una parcela con 30 registros de plantacion, estando todos ellos en el estado 'Finalizado', de los cuales 10");
    System.out.println("son de algodon, 10 de banana y 10 de cebada, y un periodo de fechas que abarca los 30 registros de plantacion,");
    System.out.println("la aplicacion debera retornar como resultado la cadena 'Cultivo inexistente'.");
    System.out.println();

    /*
     * Parcela de prueba
     */
    Parcel givenParcel = parcelService.find(3);

    System.out.println("* Parcela de prueba");
    System.out.println(givenParcel);

    System.out.println("Cantidad de registros de plantacion finalizados de esta parcela: " + plantingRecordService.findAll(givenParcel).size());
    System.out.println();

    /*
     * Periodo dado por dos fechas en el cual se obtendra
     * el cultivo que mas veces fue plantado de los cultivos
     * plantados en una parcela
     */
    Calendar dateFrom = Calendar.getInstance();
    dateFrom.set(Calendar.YEAR, 2023);
    dateFrom.set(Calendar.MONTH, APRIL);
    dateFrom.set(Calendar.DAY_OF_MONTH, 1);

    Calendar dateUntil = Calendar.getInstance();
    dateUntil.set(Calendar.YEAR, 2040);
    dateUntil.set(Calendar.MONTH, DECEMBER);
    dateUntil.set(Calendar.DAY_OF_MONTH, 12);

    System.out.println("* Periodo en el cual se obtendra el cultivo que mas veces fue plantado en la parcela con ID = " + givenParcel.getId());
    System.out.println("Fecha desde: " + UtilDate.formatDate(dateFrom));
    System.out.println("Fecha hasta: " + UtilDate.formatDate(dateUntil));
    System.out.println();

    /*
     * Seccion de prueba
     */
    String expectedResult = "Cultivo inexistente";
    String result = plantingRecordService.findMostPlantedCrop(givenParcel.getId(), dateFrom, dateUntil);

    System.out.println("Resultado esperado: " + expectedResult);
    System.out.println("* Valor devuelto por el metodo findMostPlantedCrop: " + result);
    System.out.println();

    assertTrue(expectedResult.equals(result));

    System.out.println("* Prueba pasada satisfactoriamente *");
  }

  @Test
  public void testFourFindMostPlantedCrop() {
    System.out.println("********************************** Prueba cuatro del metodo findMostPlantedCrop **********************************");
    System.out.println("- No existe el cultivo que mas veces fue plantado porque NO hay registros de plantacion finalizados.");
    System.out.println();
    System.out.println("Dada una parcela que no tiene ningun registro de plantacion en el estado 'Finalizado' en un periodo definido por una");
    System.out.println("fecha desde y una fecha hasta, la aplicacion debera retornar como resultado la cadena 'Cultivo inexistente'.");
    System.out.println();

    /*
     * Parcela de prueba
     */
    Parcel givenParcel = parcelService.find(4);

    System.out.println("* Parcela de prueba");
    System.out.println(givenParcel);

    System.out.println("Cantidad de registros de plantacion finalizados de esta parcela: " + plantingRecordService.findAll(givenParcel).size());
    System.out.println();

    /*
     * Periodo dado por dos fechas en el cual se obtendra
     * el cultivo que mas veces fue plantado de los cultivos
     * plantados en una parcela
     */
    Calendar dateFrom = Calendar.getInstance();
    dateFrom.set(Calendar.YEAR, 2023);
    dateFrom.set(Calendar.MONTH, APRIL);
    dateFrom.set(Calendar.DAY_OF_MONTH, 1);

    Calendar dateUntil = Calendar.getInstance();
    dateUntil.set(Calendar.YEAR, 2030);
    dateUntil.set(Calendar.MONTH, DECEMBER);
    dateUntil.set(Calendar.DAY_OF_MONTH, 12);

    System.out.println("* Periodo en el cual se obtendra el cultivo que mas veces fue plantado en la parcela con ID = " + givenParcel.getId());
    System.out.println("Fecha desde: " + UtilDate.formatDate(dateFrom));
    System.out.println("Fecha hasta: " + UtilDate.formatDate(dateUntil));
    System.out.println();

    /*
     * Seccion de prueba
     */
    String expectedResult = "Cultivo inexistente";
    String result = plantingRecordService.findMostPlantedCrop(givenParcel.getId(), dateFrom, dateUntil);

    System.out.println("Resultado esperado: " + expectedResult);
    System.out.println("* Valor devuelto por el metodo findMostPlantedCrop: " + result);
    System.out.println();

    assertTrue(expectedResult.equals(result));

    System.out.println("* Prueba pasada satisfactoriamente *");
  }

  @AfterClass
  public static void postTest() {
    // Cierra las conexiones
    entityManager.close();
    entityManagerFactory.close();
  }

}
