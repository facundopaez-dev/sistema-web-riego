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
 * es necesario ejecutar el comando "ant t102" (sin las comillas),
 * ya que este se ocupa de cargar la base de datos subyacente con
 * los datos que se necesitan para probar el correcto funcionamiento
 * del metodo findCropWithLongestLifeCycle (y, por ende, del metodo
 * searchCropWithLongestLifeCycle) de la clase PlantingRecordServiceBean
 */
public class CropLongestLifeCycleTest {

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
  public void testOneFindCropWithLongestLifeCycle() {
    System.out.println("*********************************** Prueba uno del metodo findCropWithLongestLifeCycle ***********************************");
    System.out.println("- Cada registro de plantacion tiene un cultivo diferente y uno de los cultivos tiene el mayor ciclo de vida. Por lo tanto,");
    System.out.println(" este es el cultivo plantado con el mayor ciclo de vida.");
    System.out.println();
    System.out.println("Dada una parcela con 7 registros de plantacion, estando todos ellos en el estado 'Finalzado', de los cuales:");
    System.out.println("- uno es de tomate (ciclo de vida del tomate: 145 dias),");
    System.out.println("- uno es de maiz (grano) (ciclo de vida del maiz de campo: 125 dias),");
    System.out.println("- uno es de banana (ciclo de vida de la banana: 330 dias),");
    System.out.println("- uno es de alcachofa (ciclo de vida de la alcachofa: 360 dias),");
    System.out.println("- uno es de mani (ciclo de vida del mani: 130 dias),");
    System.out.println("- uno es de cebada (ciclo de vida: 120 dias), y");
    System.out.println("- uno es de haba de soja (ciclo de vida: 85 dias).");
    System.out.println();
    System.out.println("Y un periodo de fechas que abarca los 7 registros de plantacion, la aplicacion debera retornar 'Alcachofa' como el cultivo");
    System.out.println("plantado, en la parcela dada, con el mayor ciclo de vida.");
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
     * el cultivo que tiene el mayor ciclo de vida de los
     * cultivos plantados en una parcela
     */
    Calendar dateFrom = Calendar.getInstance();
    dateFrom.set(Calendar.YEAR, 2023);
    dateFrom.set(Calendar.MONTH, MARCH);
    dateFrom.set(Calendar.DAY_OF_MONTH, 1);

    Calendar dateUntil = Calendar.getInstance();
    dateUntil.set(Calendar.YEAR, 2026);
    dateUntil.set(Calendar.MONTH, JULY);
    dateUntil.set(Calendar.DAY_OF_MONTH, 22);


    System.out.println(
        "* Periodo en el cual se obtendra el cultivo que tiene el mayor ciclo de vida de los cultivos plantados en la parcela con ID = "
            + givenParcel.getId());
    System.out.println("Fecha desde: " + UtilDate.formatDate(dateFrom));
    System.out.println("Fecha hasta: " + UtilDate.formatDate(dateUntil));
    System.out.println();

    /*
     * Seccion de prueba
     */
    String expectedResult = "Alcachofa";
    String result = plantingRecordService.findCropWithLongestLifeCycle(givenParcel.getId(), dateFrom, dateUntil);

    System.out.println("Resultado esperado: " + expectedResult);
    System.out.println("* Valor devuelto por el metodo findCropWithLongestLifeCycle: " + result);
    System.out.println();

    assertTrue(expectedResult.equals(result));

    System.out.println("* Prueba pasada satisfactoriamente *");
  }

  @Test
  public void testTwoFindCropWithLongestLifeCycle() {
    System.out.println("*********************************** Prueba dos del metodo findCropWithLongestLifeCycle ***********************************");
    System.out.println("- Todos los registros de plantacion contienen el mismo cultivo. Por lo tanto, el cultivo plantado con el mayor ciclo de");
    System.out.println("vida es el que figura en cada uno de estos registros de plantacion.");
    System.out.println();
    System.out.println("Dada una parcela con 5 registros de plantacion, estando todos ellos en el estado 'Finalizado', los cuales tienen al cultivo");
    System.out.println("alcachofa (ciclo de vida de la alcachofa: 360 dias), y un periodo de fechas que abarca los 5 registros de plantacion, la");
    System.out.println("aplicacion debera retornar 'Alcachofa' como el cultivo plantado, en la parcela dada, con el mayor ciclo de vida.");
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
     * el cultivo que tiene el mayor ciclo de vida de los
     * cultivos plantados en una parcela
     */
    Calendar dateFrom = Calendar.getInstance();
    dateFrom.set(Calendar.YEAR, 2023);
    dateFrom.set(Calendar.MONTH, MARCH);
    dateFrom.set(Calendar.DAY_OF_MONTH, 1);

    Calendar dateUntil = Calendar.getInstance();
    dateUntil.set(Calendar.YEAR, 2027);
    dateUntil.set(Calendar.MONTH, MARCH);
    dateUntil.set(Calendar.DAY_OF_MONTH, 22);


    System.out.println(
        "* Periodo en el cual se obtendra el cultivo que tiene el mayor ciclo de vida de los cultivos plantados en la parcela con ID = "
            + givenParcel.getId());
    System.out.println("Fecha desde: " + UtilDate.formatDate(dateFrom));
    System.out.println("Fecha hasta: " + UtilDate.formatDate(dateUntil));
    System.out.println();

    /*
     * Seccion de prueba
     */
    String expectedResult = "Alcachofa";
    String result = plantingRecordService.findCropWithLongestLifeCycle(givenParcel.getId(), dateFrom, dateUntil);

    System.out.println("Resultado esperado: " + expectedResult);
    System.out.println("* Valor devuelto por el metodo findCropWithLongestLifeCycle: " + result);
    System.out.println();

    assertTrue(expectedResult.equals(result));

    System.out.println("* Prueba pasada satisfactoriamente *");
  }

  @Test
  public void testThreeFindCropWithLongestLifeCycle() {
    System.out.println("*********************************** Prueba tres del metodo findCropWithLongestLifeCycle ***********************************");
    System.out.println("- Hay varios registros de plantacion, algunos de los cuales tienen el mismo cultivo, el cual, es el cultivo con el mayor");
    System.out.println("ciclo de vida. Por lo tanto, este es el cultivo plantado con el mayor ciclo de vida.");
    System.out.println();
    System.out.println("Dada una parcela con 8 registros historicos de plantacion, estando todos ellos en el estado 'Finalizado', y teniendo:");
    System.out.println("- uno al cultivo lechuga (ciclo de vida de la lechuga: 75 dias),");
    System.out.println("- uno al cultivo brocoli (ciclo de vida del brocoli: 135 dias),");
    System.out.println("- uno al cultivo repollo (ciclo de vida del repollo: 165 dias),");
    System.out.println("- uno al cultivo mani (ciclo de vida del mani: 130 dias), y");
    System.out.println("- cuatro de ellos al cultivo uvas de mesa (ciclo de vida de las uvas de mesa: 365 dias).");
    System.out.println();
    System.out.println("Y un periodo de fechas que abarca los 8 registros de plantacion, la aplicacion debera retornar 'Uvas de mesa' como el cultivo");
    System.out.println("plantado, en la parcela dada, con el mayor ciclo de vida.");
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
     * el cultivo que tiene el mayor ciclo de vida de los
     * cultivos plantados en una parcela
     */
    Calendar dateFrom = Calendar.getInstance();
    dateFrom.set(Calendar.YEAR, 2023);
    dateFrom.set(Calendar.MONTH, MARCH);
    dateFrom.set(Calendar.DAY_OF_MONTH, 1);

    Calendar dateUntil = Calendar.getInstance();
    dateUntil.set(Calendar.YEAR, 2027);
    dateUntil.set(Calendar.MONTH, OCTOBER);
    dateUntil.set(Calendar.DAY_OF_MONTH, 30);


    System.out.println(
        "* Periodo en el cual se obtendra el cultivo que tiene el mayor ciclo de vida de los cultivos plantados en la parcela con ID = "
            + givenParcel.getId());
    System.out.println("Fecha desde: " + UtilDate.formatDate(dateFrom));
    System.out.println("Fecha hasta: " + UtilDate.formatDate(dateUntil));
    System.out.println();

    /*
     * Seccion de prueba
     */
    String expectedResult = "Uvas de mesa";
    String result = plantingRecordService.findCropWithLongestLifeCycle(givenParcel.getId(), dateFrom, dateUntil);

    System.out.println("Resultado esperado: " + expectedResult);
    System.out.println("* Valor devuelto por el metodo findCropWithLongestLifeCycle: " + result);
    System.out.println();

    assertTrue(expectedResult.equals(result));

    System.out.println("* Prueba pasada satisfactoriamente *");
  }

  @Test
  public void testFourFindCropWithLongestLifeCycle() {
    System.out.println("*********************************** Prueba cuatro del metodo findCropWithLongestLifeCycle ***********************************");
    System.out.println("- Hay varios registros de plantacion y cada uno de ellos tiene un cultivo diferente, y cada cultivo tiene el mismo ciclo de");
    System.out.println("vida. Por lo tanto, no existe el cultivo plantado con el mayor ciclo de vida.");
    System.out.println();
    System.out.println("Dada una parcela con 5 registros de plantacion, estando todos ellos en el estado 'Finalizado', y teniendo:");
    System.out.println("- uno al cultivo berenjena (ciclo de vida de la berenjena: 130 dias),");
    System.out.println("- uno al cultivo pepino (ciclo de vida del pepino: 130 dias),");
    System.out.println("- uno al cultivo patata/papa (ciclo de vida de la patata/papa: 130 dias),");
    System.out.println("- uno al cultivo mani (ciclo de vida del mani: 130 dias), y");
    System.out.println("- uno al cultivo girasol (ciclo de vida del girasol: 130 dias).");
    System.out.println();
    System.out.println("Y un periodo que abarca los 5 registros de plantacion, la aplicacion debera retornar como resultado la cadena 'Cultivo inexistente'.");
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
     * el cultivo que tiene el mayor ciclo de vida de los
     * cultivos plantados en una parcela
     */
    Calendar dateFrom = Calendar.getInstance();
    dateFrom.set(Calendar.YEAR, 2023);
    dateFrom.set(Calendar.MONTH, MARCH);
    dateFrom.set(Calendar.DAY_OF_MONTH, 1);

    Calendar dateUntil = Calendar.getInstance();
    dateUntil.set(Calendar.YEAR, 2024);
    dateUntil.set(Calendar.MONTH, OCTOBER);
    dateUntil.set(Calendar.DAY_OF_MONTH, 29);

    System.out.println(
        "* Periodo en el cual se obtendra el cultivo que tiene el mayor ciclo de vida de los cultivos plantados en la parcela con ID = "
            + givenParcel.getId());
    System.out.println("Fecha desde: " + UtilDate.formatDate(dateFrom));
    System.out.println("Fecha hasta: " + UtilDate.formatDate(dateUntil));
    System.out.println();

    /*
     * Seccion de prueba
     */
    String expectedResult = "Cultivo inexistente";
    String result = plantingRecordService.findCropWithLongestLifeCycle(givenParcel.getId(), dateFrom, dateUntil);

    System.out.println("Resultado esperado: " + expectedResult);
    System.out.println("* Valor devuelto por el metodo findCropWithLongestLifeCycle: " + result);
    System.out.println();

    assertTrue(expectedResult.equals(result));

    System.out.println("* Prueba pasada satisfactoriamente *");
  }

  @Test
  public void testFiveFindCropWithLongestLifeCycle() {
    System.out.println("*********************************** Prueba cinco del metodo findCropWithLongestLifeCycle ***********************************");
    System.out.println("- No existen registros de plantacion en el estado 'Finalizado'. Por lo tanto, no existe el cultivo plantado con el mayor ciclo");
    System.out.println("de vida.");
    System.out.println();
    System.out.println("Dada una parcela que no tiene ningun registro de plantacion en el estado 'Finalizado' en un periodo definido por una fecha desde");
    System.out.println("y una fecha hasta, la aplicacion debera retornar como resultado la cadena 'Cultivo inexistente'.");
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
     * el cultivo que tiene el mayor ciclo de vida de los
     * cultivos plantados en una parcela
     */
    Calendar dateFrom = Calendar.getInstance();
    dateFrom.set(Calendar.YEAR, 2023);
    dateFrom.set(Calendar.MONTH, MARCH);
    dateFrom.set(Calendar.DAY_OF_MONTH, 1);

    Calendar dateUntil = Calendar.getInstance();
    dateUntil.set(Calendar.YEAR, 2027);
    dateUntil.set(Calendar.MONTH, OCTOBER);
    dateUntil.set(Calendar.DAY_OF_MONTH, 30);


    System.out.println(
        "* Periodo en el cual se obtendra el cultivo que tiene el mayor ciclo de vida de los cultivos plantados en la parcela con ID = "
            + givenParcel.getId());
    System.out.println("Fecha desde: " + UtilDate.formatDate(dateFrom));
    System.out.println("Fecha hasta: " + UtilDate.formatDate(dateUntil));
    System.out.println();

    /*
     * Seccion de prueba
     */
    String expectedResult = "Cultivo inexistente";
    String result = plantingRecordService.findCropWithLongestLifeCycle(givenParcel.getId(), dateFrom, dateUntil);

    System.out.println("Resultado esperado: " + expectedResult);
    System.out.println("* Valor devuelto por el metodo findCropWithLongestLifeCycle: " + result);
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
