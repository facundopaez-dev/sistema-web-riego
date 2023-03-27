import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import model.PlantingRecord;
import model.Parcel;
import stateless.PlantingRecordServiceBean;
import stateless.ParcelServiceBean;
import util.UtilDate;

/*
 * Para que las pruebas unitarias sean ejecutadas correctamente
 * es necesario ejecutar el comando "ant t104" (sin las comillas),
 * ya que este se ocupa de cargar la base de datos subyacente con
 * los datos que se necesitan para probar el correcto funcionamiento
 * del metodo findAllByPeriod de la clase PlantingRecordServiceBean
 */
public class FindAllByPeriodTest {

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
  public void testOneFindAllByPeriod() {
    System.out.println("******************************** Prueba uno del metodo findAllByPeriod ********************************");
    System.out.println("En esta prueba se demuestra que el metodo findAllByPeriod recupera un registro de plantacion finalizado");
    System.out.println("de una parcela en el caso en el que la fecha desde es igual a la fecha de siembra del registro de");
    System.out.println("plantacion y la fecha hasta es mayor estricto a la fecha de siembra y menor o igual a la fecha de cosecha");
    System.out.println("de dicho registro.");
    System.out.println();
    System.out.println("Esto es que se demuestra que el metodo findAllByPeriod recupera un registro de plantacion finalizado que");
    System.out.println("esta parcialemente dentro de un periodo definido por una fecha desde y una fecha hasta.");
    System.out.println();

    Parcel givenParcel = parcelService.find(1);

    /*
     * Fecha desde y fecha hasta utilizadas para recuperar
     * los registros de plantacion finalizados de una
     * parcela
     */
    Calendar dateFrom = Calendar.getInstance();
    dateFrom.set(Calendar.YEAR, 2023);
    dateFrom.set(Calendar.MONTH, JANUARY);
    dateFrom.set(Calendar.DAY_OF_MONTH, 1);

    Calendar dateUntil = Calendar.getInstance();
    dateUntil.set(Calendar.YEAR, 2023);
    dateUntil.set(Calendar.MONTH, APRIL);
    dateUntil.set(Calendar.DAY_OF_MONTH, 1);

    System.out.println("* Fechas utilizadas para recuperar los registros de plantacion de la parcela que tiene ID = "
        + givenParcel.getId());
    System.out.println("Fecha desde: " + UtilDate.formatDate(dateFrom));
    System.out.println("Fecha hasta: " + UtilDate.formatDate(dateUntil));
    System.out.println();

    List<PlantingRecord> plantingRecords = plantingRecordService.findAllByPeriod(givenParcel.getId(), dateFrom, dateUntil);
    PlantingRecord givenPlantingRecord = plantingRecords.get(0);

    System.out.println("Cantidad de registros de plantacion finalizados recuperados en el periodo ["
        + UtilDate.formatDate(dateFrom) + ", " + UtilDate.formatDate(dateUntil) + "]: " + plantingRecords.size());
    System.out.println();

    System.out.println("Fecha de siembra del registro de plantacion finalizado: "
        + UtilDate.formatDate(givenPlantingRecord.getSeedDate()));
    System.out.println("Fecha de cosecha del registro de plantacion finalizado: "
        + UtilDate.formatDate(givenPlantingRecord.getHarvestDate()));
    System.out.println();

    /*
     * Seccion de prueba.
     * 
     * Si la fecha desde es igual a la fecha de siembra del
     * registro de plantacion finalizado recuperado, y la
     * fecha hasta es mayor estricto a la fecha de siembra
     * y menor o igual a la fecha de cosecha de dicho
     * registro, significa que el metodo findAllByPeriod
     * recupero un registro de plantacion finalizado que
     * esta parcialmente dentro de un periodo definido
     * por una fecha desde y una fecha hasta.
     */
    assertTrue(plantingRecords.size() == 1);
    assertTrue(UtilDate.compareTo(givenPlantingRecord.getSeedDate(), dateFrom) == 0);
    assertTrue((UtilDate.compareTo(dateUntil, givenPlantingRecord.getSeedDate()) > 0)
        && ((UtilDate.compareTo(dateUntil, givenPlantingRecord.getHarvestDate())) <= 0));

    System.out.println("- Prueba pasada satisfactoriamente");
  }

  @Test
  public void testTwoFindAllByPeriod() {
    System.out.println("******************************** Prueba dos del metodo findAllByPeriod ********************************");
    System.out.println("En esta prueba se demuestra que el metodo findAllByPeriod recupera un registro de plantacion finalizado");
    System.out.println("de una parcela en el caso en el que la fecha desde es estrictamente menor a la fecha de siembra del registro");
    System.out.println("de plantacion y la fecha hasta es mayor o igual a la fecha de siembra y menor o igual a la fecha de cosecha");
    System.out.println("de dicho registro.");
    System.out.println();
    System.out.println("Esto es que se demuestra que el metodo findAllByPeriod recupera un registro de plantacion finalizado que");
    System.out.println("esta parcialemente dentro de un periodo definido por una fecha desde y una fecha hasta.");
    System.out.println();

    Parcel givenParcel = parcelService.find(1);

    /*
     * Fecha desde y fecha hasta utilizadas para recuperar
     * los registros de plantacion finalizados de una
     * parcela
     */
    Calendar dateFrom = Calendar.getInstance();
    dateFrom.set(Calendar.YEAR, 2022);
    dateFrom.set(Calendar.MONTH, NOVEMBER);
    dateFrom.set(Calendar.DAY_OF_MONTH, 1);

    Calendar dateUntil = Calendar.getInstance();
    dateUntil.set(Calendar.YEAR, 2023);
    dateUntil.set(Calendar.MONTH, APRIL);
    dateUntil.set(Calendar.DAY_OF_MONTH, 1);

    System.out.println("* Fechas utilizadas para recuperar los registros de plantacion de la parcela que tiene ID = "
        + givenParcel.getId());
    System.out.println("Fecha desde: " + UtilDate.formatDate(dateFrom));
    System.out.println("Fecha hasta: " + UtilDate.formatDate(dateUntil));
    System.out.println();

    List<PlantingRecord> plantingRecords = plantingRecordService.findAllByPeriod(givenParcel.getId(), dateFrom, dateUntil);
    PlantingRecord givenPlantingRecord = plantingRecords.get(0);

    System.out.println("Cantidad de registros de plantacion finalizados recuperados en el periodo ["
        + UtilDate.formatDate(dateFrom) + ", " + UtilDate.formatDate(dateUntil) + "]: " + plantingRecords.size());
    System.out.println();

    System.out.println("Fecha de siembra del registro de plantacion finalizado: "
        + UtilDate.formatDate(givenPlantingRecord.getSeedDate()));
    System.out.println("Fecha de cosecha del registro de plantacion finalizado: "
        + UtilDate.formatDate(givenPlantingRecord.getHarvestDate()));
    System.out.println();

    /*
     * Seccion de prueba.
     * 
     * Si la fecha desde es estrictamente menor a la fecha
     * de siembra del registro de plantacion finalizado recuperado,
     * y la fecha hasta es mayor o igual a la fecha de siembra
     * y menor o igual a la fecha de cosecha de dicho
     * registro, significa que el metodo findAllByPeriod
     * recupero un registro de plantacion finalizado que
     * esta parcialmente dentro de un periodo definido por
     * una fecha desde y una fecha hasta.
     */
    assertTrue(plantingRecords.size() == 1);
    assertTrue(UtilDate.compareTo(dateFrom, givenPlantingRecord.getSeedDate()) < 0);
    assertTrue((UtilDate.compareTo(dateUntil, givenPlantingRecord.getSeedDate()) >= 0)
        && ((UtilDate.compareTo(dateUntil, givenPlantingRecord.getHarvestDate())) <= 0));

    System.out.println("- Prueba pasada satisfactoriamente");
  }

  @Test
  public void testThreeFindAllByPeriod() {
    System.out.println("******************************** Prueba tres del metodo findAllByPeriod ********************************");
    System.out.println("En esta prueba se demuestra que el metodo findAllByPeriod recupera un registro de plantacion finalizado");
    System.out.println("de una parcela en el caso en el que la fecha hasta es igual a la fecha de cosecha del registro de");
    System.out.println("plantacion y la fecha desde es mayor o igual a la fecha de siembra y menor estricto a la fecha de cosecha");
    System.out.println("de dicho registro.");
    System.out.println();
    System.out.println("Esto es que se demuestra que el metodo findAllByPeriod recupera un registro de plantacion finalizado que");
    System.out.println("esta parcialemente dentro de un periodo definido por una fecha desde y una fecha hasta.");
    System.out.println();

    Parcel givenParcel = parcelService.find(1);

    /*
     * Fecha desde y fecha hasta utilizadas para recuperar
     * los registros de plantacion finalizados de una
     * parcela
     */
    Calendar dateFrom = Calendar.getInstance();
    dateFrom.set(Calendar.YEAR, 2025);
    dateFrom.set(Calendar.MONTH, AUGUST);
    dateFrom.set(Calendar.DAY_OF_MONTH, 8);

    Calendar dateUntil = Calendar.getInstance();
    dateUntil.set(Calendar.YEAR, 2025);
    dateUntil.set(Calendar.MONTH, OCTOBER);
    dateUntil.set(Calendar.DAY_OF_MONTH, 31);

    System.out.println("* Fechas utilizadas para recuperar los registros de plantacion de la parcela que tiene ID = "
        + givenParcel.getId());
    System.out.println("Fecha desde: " + UtilDate.formatDate(dateFrom));
    System.out.println("Fecha hasta: " + UtilDate.formatDate(dateUntil));
    System.out.println();

    List<PlantingRecord> plantingRecords = plantingRecordService.findAllByPeriod(givenParcel.getId(), dateFrom, dateUntil);
    PlantingRecord givenPlantingRecord = plantingRecords.get(0);

    System.out.println("Cantidad de registros de plantacion finalizados recuperados en el periodo ["
        + UtilDate.formatDate(dateFrom) + ", " + UtilDate.formatDate(dateUntil) + "]: " + plantingRecords.size());
    System.out.println();

    System.out.println("Fecha de siembra del registro de plantacion finalizado: "
        + UtilDate.formatDate(givenPlantingRecord.getSeedDate()));
    System.out.println("Fecha de cosecha del registro de plantacion finalizado: "
        + UtilDate.formatDate(givenPlantingRecord.getHarvestDate()));
    System.out.println();

    /*
     * Seccion de prueba.
     * 
     * Si la fecha desde es mayor o igual a la fecha de siembra
     * del registro de plantacion finalizado recuperado y es
     * menor estricto a la fecha de cosecha de dicho registro,
     * y la fecha hasta es igual a la fecha de cosecha, significa
     * que el metodo findAllByPeriod recupero un registro de
     * plantacion finalizado que esta parcialmente dentro de un
     * periodo definido por una fecha desde y una fecha hasta.
     */
    assertTrue(plantingRecords.size() == 1);
    assertTrue(UtilDate.compareTo(dateUntil, givenPlantingRecord.getHarvestDate()) == 0);
    assertTrue((UtilDate.compareTo(dateFrom, givenPlantingRecord.getSeedDate()) >= 0)
        && ((UtilDate.compareTo(dateFrom, givenPlantingRecord.getHarvestDate())) < 0));

    System.out.println("- Prueba pasada satisfactoriamente");
  }

  @Test
  public void testFourFindAllByPeriod() {
    System.out.println("******************************** Prueba cuatro del metodo findAllByPeriod ********************************");
    System.out.println("En esta prueba se demuestra que el metodo findAllByPeriod recupera un registro de plantacion finalizado");
    System.out.println("de una parcela en el caso en el que la fecha hasta es estrictamente mayor a la fecha de cosecha del registro");
    System.out.println("de plantacion y la fecha desde es mayor o igual a la fecha de siembra y menor o igual a la fecha de cosecha");
    System.out.println("de dicho registro.");
    System.out.println();
    System.out.println("Esto es que se demuestra que el metodo findAllByPeriod recupera un registro de plantacion finalizado que");
    System.out.println("esta parcialemente dentro de un periodo definido por una fecha desde y una fecha hasta.");
    System.out.println();

    Parcel givenParcel = parcelService.find(1);

    /*
     * Fecha desde y fecha hasta utilizadas para recuperar
     * los registros de plantacion finalizados de una
     * parcela
     */
    Calendar dateFrom = Calendar.getInstance();
    dateFrom.set(Calendar.YEAR, 2025);
    dateFrom.set(Calendar.MONTH, SEPTEMBER);
    dateFrom.set(Calendar.DAY_OF_MONTH, 28);

    Calendar dateUntil = Calendar.getInstance();
    dateUntil.set(Calendar.YEAR, 2026);
    dateUntil.set(Calendar.MONTH, JANUARY);
    dateUntil.set(Calendar.DAY_OF_MONTH, 1);

    System.out.println("* Fechas utilizadas para recuperar los registros de plantacion de la parcela que tiene ID = "
        + givenParcel.getId());
    System.out.println("Fecha desde: " + UtilDate.formatDate(dateFrom));
    System.out.println("Fecha hasta: " + UtilDate.formatDate(dateUntil));
    System.out.println();

    List<PlantingRecord> plantingRecords = plantingRecordService.findAllByPeriod(givenParcel.getId(), dateFrom, dateUntil);
    PlantingRecord givenPlantingRecord = plantingRecords.get(0);

    System.out.println("Cantidad de registros de plantacion finalizados recuperados en el periodo ["
        + UtilDate.formatDate(dateFrom) + ", " + UtilDate.formatDate(dateUntil) + "]: " + plantingRecords.size());
    System.out.println();

    System.out.println("Fecha de siembra del registro de plantacion finalizado: "
        + UtilDate.formatDate(givenPlantingRecord.getSeedDate()));
    System.out.println("Fecha de cosecha del registro de plantacion finalizado: "
        + UtilDate.formatDate(givenPlantingRecord.getHarvestDate()));
    System.out.println();

    /*
     * Seccion de prueba.
     * 
     * Si la fecha desde es mayor o igual a la fecha de siembra
     * del registro de plantacion finalizado recuperado y es
     * menor o igual a la fecha de cosecha de dicho registro,
     * y la fecha hasta es estrictamente mayor a la fecha de
     * cosecha, significa que el metodo findAllByPeriod recupero
     * un registro de plantacion finalizado que esta parcialmente
     * dentro de un periodo definido por una fecha desde y una
     * fecha hasta.
     */
    assertTrue(plantingRecords.size() == 1);
    assertTrue(UtilDate.compareTo(dateUntil, givenPlantingRecord.getHarvestDate()) > 0);
    assertTrue((UtilDate.compareTo(dateFrom, givenPlantingRecord.getSeedDate()) >= 0)
        && ((UtilDate.compareTo(dateFrom, givenPlantingRecord.getHarvestDate())) <= 0));

    System.out.println("- Prueba pasada satisfactoriamente");
  }

  @Test
  public void testFiveFindAllByPeriod() {
    System.out.println("******************************** Prueba cinco del metodo findAllByPeriod ********************************");
    System.out.println("En esta prueba se demuestra que el metodo findAllByPeriod recupera un registro de plantacion finalizado");
    System.out.println("de una parcela en el caso en el que la fecha desde es estrictamente menor a la fecha de siembra del registro");
    System.out.println("de plantacion y la fecha hasta es estrictamente mayor a la fecha de cosecha de dicho registro.");
    System.out.println();
    System.out.println("Esto es que se demuestra que el metodo findAllByPeriod recupera un registro de plantacion finalizado que");
    System.out.println("esta completamente dentro de un periodo definido por una fecha desde y una fecha hasta.");
    System.out.println();

    Parcel givenParcel = parcelService.find(5);

    /*
     * Fecha desde y fecha hasta utilizadas para recuperar
     * los registros de plantacion finalizados de una
     * parcela
     */
    Calendar dateFrom = Calendar.getInstance();
    dateFrom.set(Calendar.YEAR, 2022);
    dateFrom.set(Calendar.MONTH, DECEMBER);
    dateFrom.set(Calendar.DAY_OF_MONTH, 28);

    Calendar dateUntil = Calendar.getInstance();
    dateUntil.set(Calendar.YEAR, 2023);
    dateUntil.set(Calendar.MONTH, JUNE);
    dateUntil.set(Calendar.DAY_OF_MONTH, 12);

    System.out.println("* Fechas utilizadas para recuperar los registros de plantacion de la parcela que tiene ID = "
        + givenParcel.getId());
    System.out.println("Fecha desde: " + UtilDate.formatDate(dateFrom));
    System.out.println("Fecha hasta: " + UtilDate.formatDate(dateUntil));
    System.out.println();

    List<PlantingRecord> plantingRecords = plantingRecordService.findAllByPeriod(givenParcel.getId(), dateFrom, dateUntil);
    PlantingRecord givenPlantingRecord = plantingRecords.get(0);

    System.out.println("Cantidad de registros de plantacion finalizados recuperados en el periodo ["
        + UtilDate.formatDate(dateFrom) + ", " + UtilDate.formatDate(dateUntil) + "]: " + plantingRecords.size());
    System.out.println();

    System.out.println("Fecha de siembra del registro de plantacion finalizado: "
        + UtilDate.formatDate(givenPlantingRecord.getSeedDate()));
    System.out.println("Fecha de cosecha del registro de plantacion finalizado: "
        + UtilDate.formatDate(givenPlantingRecord.getHarvestDate()));
    System.out.println();

    /*
     * Seccion de prueba.
     * 
     * Si la fecha desde es estrictamente menor a la fecha de
     * siembra del registro de plantacion finalizado y la
     * fecha hasta es estrictamente mayor a la fecha de cosecha
     * de dicho registro, significa que el metodo findAllByPeriod
     * recupero un registro de plantacion finalizado que esta
     * completamente dentro de un periodo definido por una fecha
     * desde y una fecha hasta.
     */
    assertTrue(plantingRecords.size() == 1);
    assertTrue(UtilDate.compareTo(dateFrom, givenPlantingRecord.getSeedDate()) < 0);
    assertTrue(UtilDate.compareTo(dateUntil, givenPlantingRecord.getHarvestDate()) > 0);

    System.out.println("- Prueba pasada satisfactoriamente");
  }

  @AfterClass
  public static void postTest() {
    // Cierra las conexiones
    entityManager.close();
    entityManagerFactory.close();
  }

}
