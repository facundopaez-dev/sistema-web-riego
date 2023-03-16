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
 * es necesario ejecutar el comando "ant t103" (sin las comillas),
 * ya que este se ocupa de cargar la base de datos subyacente con
 * los datos que se necesitan para probar el correcto funcionamiento
 * del metodo findCropWithShortestLifeCycle (y, por ende, del metodo
 * searchCropWithShortestLifeCycle) de la clase PlantingRecordServiceBean
 */
public class CropShortestLifeCycleTest {

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
  public void testOneFindCropWithShortestLifeCycle() {
    System.out.println("*********************************** Prueba uno del metodo findCropWithShortestLifeCycle ***********************************");
    System.out.println("- Cada registro de plantacion tiene un cultivo diferente y uno de los cultivos tiene el menor ciclo de vida. Por lo tanto,");
    System.out.println("este es el cultivo plantado con el menor ciclo de vida.");
    System.out.println();
    System.out.println("Dada una parcela con 8 registros de plantacion, estando todos ellos en el estado 'Finalizado', y teniendo:");
    System.out.println("- uno al cultivo frilojes/judias verdes (ciclo de vida de las judias verdes: 90 dias),");
    System.out.println("- uno al cultivo frijoles/judias secas (ciclo de vida de las judias secas: 110 dias),");
    System.out.println("- uno al cultivo repollo (ciclo de vida del repollo: 165 dias),");
    System.out.println("- uno al cultivo alcachofa (ciclo de vida: 360 dias),");
    System.out.println("- uno al cultivo tomate (ciclo de vida del tomate: 145 dias),");
    System.out.println("- uno al cultivo maiz (grano) (ciclo de vida del maiz de campo: 125 dias),");
    System.out.println("- uno al cultivo banana (ciclo de vida de la banana: 330 dias), y");
    System.out.println("- uno al cultivo mani (ciclo de vida del mani: 130 dias).");
    System.out.println();
    System.out.println("Y un periodo de fechas que abarca los 8 registros de plantacion, la aplicacion debera retornar 'Frijoles/judias verdes'");
    System.out.println("como el cultivo plantado, en la parcela dada, con el menor ciclo de vida.");
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
     * el cultivo que tiene el menor ciclo de vida de los
     * cultivos plantados en una parcela
     */
    Calendar dateFrom = Calendar.getInstance();
    dateFrom.set(Calendar.YEAR, 2023);
    dateFrom.set(Calendar.MONTH, MARCH);
    dateFrom.set(Calendar.DAY_OF_MONTH, 1);

    Calendar dateUntil = Calendar.getInstance();
    dateUntil.set(Calendar.YEAR, 2027);
    dateUntil.set(Calendar.MONTH, FEBRUARY);
    dateUntil.set(Calendar.DAY_OF_MONTH, 22);

    System.out.println(
        "* Periodo en el cual se obtendra el cultivo que tiene el menor ciclo de vida de los cultivos plantados en la parcela con ID = "
            + givenParcel.getId());
    System.out.println("Fecha desde: " + UtilDate.formatDate(dateFrom));
    System.out.println("Fecha hasta: " + UtilDate.formatDate(dateUntil));
    System.out.println();

    /*
     * Seccion de prueba
     */
    String expectedResult = "Frijoles/Judías verdes";
    String result = plantingRecordService.findCropWithShortestLifeCycle(givenParcel.getId(), dateFrom, dateUntil);

    System.out.println("Resultado esperado: " + expectedResult);
    System.out.println("* Valor devuelto por el metodo findCropWithShortestLifeCycle: " + result);
    System.out.println();

    assertTrue(expectedResult.equals(result));

    System.out.println("* Prueba pasada satisfactoriamente *");
  }

  @Test
  public void testTwoFindCropWithShortestLifeCycle() {
    System.out.println("*********************************** Prueba dos del metodo findCropWithShortestLifeCycle ***********************************");
    System.out.println("- Todos los registros de plantacion contienen el mismo cultivo. Por lo tanto, este el cultivo plantado con el menor ciclo de");
    System.out.println("vida.");
    System.out.println();
    System.out.println("Dada una parcela con 5 registros de plantacion, estando todos ellos en el estado 'Finalizado', y todos ellos teniendo al");
    System.out.println("cultivo algodon (ciclo de vida del algodon: 195 dias), y un periodo de fechas que abarca los 5 registros de plantacion, la");
    System.out.println("aplicacion deberá retornar 'Algodon' como el cultivo plantado, en la parcela dada, con el menor ciclo de vida.");
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
     * el cultivo que tiene el menor ciclo de vida de los
     * cultivos plantados en una parcela
     */
    Calendar dateFrom = Calendar.getInstance();
    dateFrom.set(Calendar.YEAR, 2023);
    dateFrom.set(Calendar.MONTH, FEBRUARY);
    dateFrom.set(Calendar.DAY_OF_MONTH, 1);

    Calendar dateUntil = Calendar.getInstance();
    dateUntil.set(Calendar.YEAR, 2025);
    dateUntil.set(Calendar.MONTH, OCTOBER);
    dateUntil.set(Calendar.DAY_OF_MONTH, 15);

    System.out.println(
        "* Periodo en el cual se obtendra el cultivo que tiene el menor ciclo de vida de los cultivos plantados en la parcela con ID = "
            + givenParcel.getId());
    System.out.println("Fecha desde: " + UtilDate.formatDate(dateFrom));
    System.out.println("Fecha hasta: " + UtilDate.formatDate(dateUntil));
    System.out.println();

    /*
     * Seccion de prueba
     */
    String expectedResult = "Algodón";
    String result = plantingRecordService.findCropWithShortestLifeCycle(givenParcel.getId(), dateFrom, dateUntil);

    System.out.println("Resultado esperado: " + expectedResult);
    System.out.println("* Valor devuelto por el metodo findCropWithShortestLifeCycle: " + result);
    System.out.println();

    assertTrue(expectedResult.equals(result));

    System.out.println("* Prueba pasada satisfactoriamente *");
  }

  @Test
  public void testThreeFindCropWithShortestLifeCycle() {
    System.out.println("*********************************** Prueba tres del metodo findCropWithShortestLifeCycle ***********************************");
    System.out.println("- Hay varios registros de plantacion, algunos de los cuales tienen el mismo cultivo, el cual, es el cultivo con el menor ciclo");
    System.out.println("de vida. Por lo tanto, este es el cultivo plantado con el menor ciclo de vida.");
    System.out.println();
    System.out.println("Dada una parcela con 8 registros de plantacion, estando todos ellos en el estado 'Finalizado', y teniendo:");
    System.out.println("- tres al cultivo lechuga (ciclo de vida de la lechuga: 75 dias),");
    System.out.println("- uno al cultivo brocoli (ciclo de vida del brocoli: 135 dias),");
    System.out.println("- uno al cultivo repollo (ciclo de vida del repollo: 165 dias),");
    System.out.println("- uno al cultivo uvas de mesa (ciclo de vida de las uvas de mesa: 365 dias),");
    System.out.println("- uno es de alcachofa (ciclo de vida: 360 dias), y");
    System.out.println("- uno es de tomate (ciclo de vida del tomate: 145 dias).");
    System.out.println();
    System.out.println("Y un periodo de fechas que abarca los 8 registros de plantacion, la aplicacion debera retornar 'Lechuga' como el cultivo");
    System.out.println("plantado, en la parcela dada, con el menor ciclo de vida.");
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
     * el cultivo que tiene el menor ciclo de vida de los
     * cultivos plantados en una parcela
     */
    Calendar dateFrom = Calendar.getInstance();
    dateFrom.set(Calendar.YEAR, 2023);
    dateFrom.set(Calendar.MONTH, FEBRUARY);
    dateFrom.set(Calendar.DAY_OF_MONTH, 1);

    Calendar dateUntil = Calendar.getInstance();
    dateUntil.set(Calendar.YEAR, 2026);
    dateUntil.set(Calendar.MONTH, OCTOBER);
    dateUntil.set(Calendar.DAY_OF_MONTH, 15);

    System.out.println(
        "* Periodo en el cual se obtendra el cultivo que tiene el menor ciclo de vida de los cultivos plantados en la parcela con ID = "
            + givenParcel.getId());
    System.out.println("Fecha desde: " + UtilDate.formatDate(dateFrom));
    System.out.println("Fecha hasta: " + UtilDate.formatDate(dateUntil));
    System.out.println();

    /*
     * Seccion de prueba
     */
    String expectedResult = "Lechuga";
    String result = plantingRecordService.findCropWithShortestLifeCycle(givenParcel.getId(), dateFrom, dateUntil);

    System.out.println("Resultado esperado: " + expectedResult);
    System.out.println("* Valor devuelto por el metodo findCropWithShortestLifeCycle: " + result);
    System.out.println();

    assertTrue(expectedResult.equals(result));

    System.out.println("* Prueba pasada satisfactoriamente *");
  }

  @Test
  public void testFourFindCropWithShortestLifeCycle() {
    System.out.println("*********************************** Prueba cuatro del metodo findCropWithShortestLifeCycle ***********************************");
    System.out.println("- Hay varios registros de plantacion y cada uno de ellos tiene un cultivo diferente, y cada cultivo tiene el mismo ciclo de");
    System.out.println("vida. Por lo tanto, no existe el cultivo plantado con el menor ciclo de vida.");
    System.out.println();
    System.out.println("Dada una parcela con 5 registros de plantacion, estando todos ellos en el estado 'Finalizado', y teniendo:");
    System.out.println("- uno al cultivo berenjena (ciclo de vida de la berenjena: 130 dias),");
    System.out.println("- uno al cultivo pepino (ciclo de vida del pepino: 130 dias),");
    System.out.println("- uno al cultivo patata/papa (ciclo de vida de la patata/papa: 130 dias),");
    System.out.println("- uno al cultivo girasol (ciclo de vida del girasol: 130 dias), y");
    System.out.println("- uno al cultivo mani (ciclo de vida del sorgo: 130 dias).");
    System.out.println();
    System.out.println("Y un periodo de fechas que abarca los 5 registros de plantacion, la aplicacion debera retornar como resultado la cadena");
    System.out.println("'Cultivo inexistente'.");
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
     * el cultivo que tiene el menor ciclo de vida de los
     * cultivos plantados en una parcela
     */
    Calendar dateFrom = Calendar.getInstance();
    dateFrom.set(Calendar.YEAR, 2023);
    dateFrom.set(Calendar.MONTH, FEBRUARY);
    dateFrom.set(Calendar.DAY_OF_MONTH, 1);

    Calendar dateUntil = Calendar.getInstance();
    dateUntil.set(Calendar.YEAR, 2025);
    dateUntil.set(Calendar.MONTH, OCTOBER);
    dateUntil.set(Calendar.DAY_OF_MONTH, 15);

    System.out.println(
        "* Periodo en el cual se obtendra el cultivo que tiene el menor ciclo de vida de los cultivos plantados en la parcela con ID = "
            + givenParcel.getId());
    System.out.println("Fecha desde: " + UtilDate.formatDate(dateFrom));
    System.out.println("Fecha hasta: " + UtilDate.formatDate(dateUntil));
    System.out.println();

    /*
     * Seccion de prueba
     */
    String expectedResult = "Cultivo inexistente";
    String result = plantingRecordService.findCropWithShortestLifeCycle(givenParcel.getId(), dateFrom, dateUntil);

    System.out.println("Resultado esperado: " + expectedResult);
    System.out.println("* Valor devuelto por el metodo findCropWithShortestLifeCycle: " + result);
    System.out.println();

    assertTrue(expectedResult.equals(result));

    System.out.println("* Prueba pasada satisfactoriamente *");
  }

  @Test
  public void testFiveFindCropWithShortestLifeCycle() {
    System.out.println("*********************************** Prueba cinco del metodo findCropWithShortestLifeCycle ***********************************");
    System.out.println("- No existen registros de plantacion en el estado 'Finalizado'. Por lo tanto, no existe el cultivo plantado con el menor ciclo");
    System.out.println("de vida.");
    System.out.println();
    System.out.println("Dada una parcela que no tiene registros de plantacion en el estado 'Finalizado', la aplicacion debe retornar como resultado la");
    System.out.println("cadena 'Cultivo inexistente'.");
    System.out.println();

    /*
     * Parcela de prueba
     */
    Parcel givenParcel = parcelService.find(5);

    System.out.println("* Parcela de prueba");
    System.out.println(givenParcel);

    System.out.println("Cantidad de registros de plantacion finalizados de esta parcela: " + plantingRecordService.findAll(givenParcel).size());
    System.out.println();

    /*
     * Periodo dado por dos fechas en el cual se obtendra
     * el cultivo que tiene el menor ciclo de vida de los
     * cultivos plantados en una parcela
     */
    Calendar dateFrom = Calendar.getInstance();
    dateFrom.set(Calendar.YEAR, 2023);
    dateFrom.set(Calendar.MONTH, FEBRUARY);
    dateFrom.set(Calendar.DAY_OF_MONTH, 1);

    Calendar dateUntil = Calendar.getInstance();
    dateUntil.set(Calendar.YEAR, 2025);
    dateUntil.set(Calendar.MONTH, OCTOBER);
    dateUntil.set(Calendar.DAY_OF_MONTH, 15);

    System.out.println(
        "* Periodo en el cual se obtendra el cultivo que tiene el menor ciclo de vida de los cultivos plantados en la parcela con ID = "
            + givenParcel.getId());
    System.out.println("Fecha desde: " + UtilDate.formatDate(dateFrom));
    System.out.println("Fecha hasta: " + UtilDate.formatDate(dateUntil));
    System.out.println();

    /*
     * Seccion de prueba
     */
    String expectedResult = "Cultivo inexistente";
    String result = plantingRecordService.findCropWithShortestLifeCycle(givenParcel.getId(), dateFrom, dateUntil);

    System.out.println("Resultado esperado: " + expectedResult);
    System.out.println("* Valor devuelto por el metodo findCropWithShortestLifeCycle: " + result);
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
