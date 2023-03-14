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
 * es necesario ejecutar el comando "ant t101" (sin las comillas),
 * ya que este se ocupa de cargar la base de datos subyacente con
 * los datos que se necesitan para probar el correcto funcionamiento
 * del metodo findLeastPlantedCrop (y, por ende, del metodo
 * searchLeastPlantedCrop) de la clase PlantingRecordServiceBean
 */
public class LeastPlantedCropTest {

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
  public void testOneFindLeastPlantedCrop() {
    System.out.println("********************************** Prueba uno del metodo findLeastPlantedCrop **********************************");
    System.out.println("- Existe el cultivo que menos veces fue plantado porque hay un cultivo que tiene minoria de registros de plantacion");
    System.out.println("finalizados en un periodo de fechas.");
    System.out.println();
    System.out.println("Dada una parcela con 25 registros de plantacion, todos ellos en el estado 'Finalizado', de los cuales 5 son de");
    System.out.println("uva de vino, 8 de uvas de mesa y 12 de girasol, y un periodo de fechas que abarca los 25 registros de plantacion,");
    System.out.println("la aplicacion debera retornar 'Uvas de vino' como el cultivo que menos se planto en la parcela dada en el periodo");
    System.out.println("dado.");
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
     * el cultivo que menos veces fue plantado de los cultivos
     * plantados en una parcela
     */
    Calendar dateFrom = Calendar.getInstance();
    dateFrom.set(Calendar.YEAR, 2023);
    dateFrom.set(Calendar.MONTH, APRIL);
    dateFrom.set(Calendar.DAY_OF_MONTH, 1);

    Calendar dateUntil = Calendar.getInstance();
    dateUntil.set(Calendar.YEAR, 2041);
    dateUntil.set(Calendar.MONTH, JANUARY);
    dateUntil.set(Calendar.DAY_OF_MONTH, 1);

    System.out.println("* Periodo en el cual se obtendra el cultivo que menos veces fue plantado en la parcela con ID = " + givenParcel.getId());
    System.out.println("Fecha desde: " + UtilDate.formatDate(dateFrom));
    System.out.println("Fecha hasta: " + UtilDate.formatDate(dateUntil));
    System.out.println();

    /*
     * Seccion de prueba
     */
    String expectedResult = "Uvas de vino";
    String result = plantingRecordService.findLeastPlantedCrop(givenParcel.getId(), dateFrom, dateUntil);

    System.out.println("Resultado esperado: " + expectedResult);
    System.out.println("* Valor devuelto por el metodo findLeastPlantedCrop: " + result);
    System.out.println();

    assertTrue(expectedResult.equals(result));

    System.out.println("* Prueba pasada satisfactoriamente *");
  }

  @Test
  public void testTwoFindLeastPlantedCrop() {
    System.out.println("********************************** Prueba dos del metodo findLeastPlantedCrop **********************************");
    System.out.println("- Existe el cultivo que menos veces fue plantado porque hay un cultivo que es el unico que tiene registros de");
    System.out.println("plantacion finalizados en un periodo de fechas.");
    System.out.println();
    System.out.println("Dada una parcela con 10 registros de plantacion, todos ellos en el estado 'Finalizado', y teniendo al cultivo");
    System.out.println("'Pepino', y un periodo de fechas que abarca los 10 registros de plantacion, la aplicacion debera retornar 'Pepino'");
    System.out.println("como el cultivo que menos se planto en la parcela dada en el periodo dado.");
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
     * el cultivo que menos veces fue plantado de los cultivos
     * plantados en una parcela
     */
    Calendar dateFrom = Calendar.getInstance();
    dateFrom.set(Calendar.YEAR, 2023);
    dateFrom.set(Calendar.MONTH, JANUARY);
    dateFrom.set(Calendar.DAY_OF_MONTH, 22);

    Calendar dateUntil = Calendar.getInstance();
    dateUntil.set(Calendar.YEAR, 2026);
    dateUntil.set(Calendar.MONTH, NOVEMBER);
    dateUntil.set(Calendar.DAY_OF_MONTH, 5);

    System.out.println("* Periodo en el cual se obtendra el cultivo que menos veces fue plantado en la parcela con ID = " + givenParcel.getId());
    System.out.println("Fecha desde: " + UtilDate.formatDate(dateFrom));
    System.out.println("Fecha hasta: " + UtilDate.formatDate(dateUntil));
    System.out.println();

    /*
     * Seccion de prueba
     */
    String expectedResult = "Pepino";
    String result = plantingRecordService.findLeastPlantedCrop(givenParcel.getId(), dateFrom, dateUntil);

    System.out.println("Resultado esperado: " + expectedResult);
    System.out.println("* Valor devuelto por el metodo findLeastPlantedCrop: " + result);
    System.out.println();

    assertTrue(expectedResult.equals(result));

    System.out.println("* Prueba pasada satisfactoriamente *");
  }

  @Test
  public void testThreeFindLeastPlantedCrop() {
    System.out.println("********************************** Prueba tres del metodo findLeastPlantedCrop **********************************");
    System.out.println("- No existe el cultivo que menos veces fue plantado porque se tiene igual cantidad de registros de plantacion");
    System.out.println("finalizados para cada cultivo en un periodo de fechas.");
    System.out.println();
    System.out.println("Dada una parcela con 30 registros de plantacion, estando todos ellos en el estado 'Finalizado', de los cuales 10");
    System.out.println("son de cebada, 10 de lechuga y 10 de repollo, y un periodo de fechas que abarca los 30 registros de plantacion,");
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
     * el cultivo que menos veces fue plantado de los cultivos
     * plantados en una parcela
     */
    Calendar dateFrom = Calendar.getInstance();
    dateFrom.set(Calendar.YEAR, 2023);
    dateFrom.set(Calendar.MONTH, MAY);
    dateFrom.set(Calendar.DAY_OF_MONTH, 14);

    Calendar dateUntil = Calendar.getInstance();
    dateUntil.set(Calendar.YEAR, 2034);
    dateUntil.set(Calendar.MONTH, JANUARY);
    dateUntil.set(Calendar.DAY_OF_MONTH, 1);

    System.out.println("* Periodo en el cual se obtendra el cultivo que menos veces fue plantado en la parcela con ID = " + givenParcel.getId());
    System.out.println("Fecha desde: " + UtilDate.formatDate(dateFrom));
    System.out.println("Fecha hasta: " + UtilDate.formatDate(dateUntil));
    System.out.println();

    /*
     * Seccion de prueba
     */
    String expectedResult = "Cultivo inexistente";
    String result = plantingRecordService.findLeastPlantedCrop(givenParcel.getId(), dateFrom, dateUntil);

    System.out.println("Resultado esperado: " + expectedResult);
    System.out.println("* Valor devuelto por el metodo findLeastPlantedCrop: " + result);
    System.out.println();

    assertTrue(expectedResult.equals(result));

    System.out.println("* Prueba pasada satisfactoriamente *");
  }

  @Test
  public void testFourFindLeastPlantedCrop() {
    System.out.println("********************************** Prueba cuatro del metodo findLeastPlantedCrop **********************************");
    System.out.println("- No existe el cultivo que menos veces fue plantado porque NO hay registros de plantacion finalizados.");
    System.out.println();
    System.out.println("Dada una parcela que no tiene ningun registro de plantacion en el estado 'Finalizado', la aplicacion debera retornar");
    System.out.println("como resultado la cadena 'Cultivo inexistente'.");
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
     * el cultivo que menos veces fue plantado de los cultivos
     * plantados en una parcela
     */
    Calendar dateFrom = Calendar.getInstance();
    dateFrom.set(Calendar.YEAR, 2023);
    dateFrom.set(Calendar.MONTH, SEPTEMBER);
    dateFrom.set(Calendar.DAY_OF_MONTH, 27);

    Calendar dateUntil = Calendar.getInstance();
    dateUntil.set(Calendar.YEAR, 2025);
    dateUntil.set(Calendar.MONTH, JULY);
    dateUntil.set(Calendar.DAY_OF_MONTH, 26);

    System.out.println("* Periodo en el cual se obtendra el cultivo que menos veces fue plantado en la parcela con ID = " + givenParcel.getId());
    System.out.println("Fecha desde: " + UtilDate.formatDate(dateFrom));
    System.out.println("Fecha hasta: " + UtilDate.formatDate(dateUntil));
    System.out.println();

    /*
     * Seccion de prueba
     */
    String expectedResult = "Cultivo inexistente";
    String result = plantingRecordService.findLeastPlantedCrop(givenParcel.getId(), dateFrom, dateUntil);

    System.out.println("Resultado esperado: " + expectedResult);
    System.out.println("* Valor devuelto por el metodo findLeastPlantedCrop: " + result);
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
