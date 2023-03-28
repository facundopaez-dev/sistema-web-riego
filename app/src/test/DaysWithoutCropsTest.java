import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.List;
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
import model.PlantingRecord;
import util.UtilDate;

/*
 * Para que las pruebas unitarias sean ejecutadas correctamente
 * es necesario ejecutar el comando "ant t104" (sin las comillas),
 * ya que este se ocupa de cargar la base de datos subyacente con
 * los datos que se necesitan para probar el correcto funcionamiento
 * del metodo calculateDaysWithoutCrops de la clase PlantingRecordServiceBean
 */
public class DaysWithoutCropsTest {

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
  public void testOneDaysWithoutCrop() {
    System.out.println("********************************** Prueba uno del metodo calculateDaysWithoutCrops **********************************");
    System.out.println("- No hay diferencia de dias entre fechas. Por lo tanto, la cantidad de dias en los que una parcela no tuvo ningun");
    System.out.println("cultivo plantado es 0.");
    System.out.println();
    System.out.println("Dada una parcela que tiene 8 registros de plantacion finalizados, estando cada uno de ellos inmediatamente a continuacion");
    System.out.println("de otro (es decir, la fecha de siembra de un registro de plantacion esta inmediatamente un dia despues de la fecha de");
    System.out.println("cosecha de un registro de plantacion previo), y la fecha desde coincide con la fecha de siembra del primer registro de");
    System.out.println("plantacion y la fecha hasta coincide con la fecha de cosecha del ultimo registro de plantacion, la aplicacion debera");
    System.out.println("retornar 0 como la cantidad de dias en los que la parcela no tuvo ningun cultivo plantado.");
    System.out.println();

    /*
     * Parcela de prueba
     */
    Parcel givenParcel = parcelService.find(1);

    System.out.println("* Parcela de prueba");
    System.out.println(givenParcel);

    /*
     * Estas fechas definen el periodo dentro del cual
     * se calcula la cantidad de dias en los que una
     * parcela no tuvo ningun cultivo plantado haciendo
     * uso de sus registros de plantacion finalizados
     * y comprendidos en dicho periodo
     */
    Calendar dateFrom = Calendar.getInstance();
    dateFrom.set(Calendar.YEAR, 2023);
    dateFrom.set(Calendar.MONTH, JANUARY);
    dateFrom.set(Calendar.DAY_OF_MONTH, 1);

    Calendar dateUntil = Calendar.getInstance();
    dateUntil.set(Calendar.YEAR, 2025);
    dateUntil.set(Calendar.MONTH, OCTOBER);
    dateUntil.set(Calendar.DAY_OF_MONTH, 31);

    System.out.println("* Periodo en el cual se obtendra la cantidad de dias en los que la parcela con ID = "
        + givenParcel.getId() + " no tuvo ningun cultivo plantado");
    System.out.println("Fecha desde: " + UtilDate.formatDate(dateFrom));
    System.out.println("Fecha hasta: " + UtilDate.formatDate(dateUntil));
    System.out.println();

    List<PlantingRecord> plantingRecords = plantingRecordService.findAllByPeriod(givenParcel.getId(), dateFrom, dateUntil);

    System.out.println("Fecha de siembra del primer registro de plantacion: "
        + UtilDate.formatDate(plantingRecords.get(0).getSeedDate()));
    System.out.println("Fecha de cosecha del ultimo registro de plantacion: "
        + UtilDate.formatDate(plantingRecords.get(plantingRecords.size() - 1).getHarvestDate()));
    System.out.println();

    System.out.println("Cantidad de registros de plantacion finalizados de esta parcela en el periodo ["
        + UtilDate.formatDate(dateFrom) + ", " + UtilDate.formatDate(dateUntil) + "]: "
        + plantingRecords.size());
    System.out.println();

    /*
     * Seccion de prueba
     */
    int expectedResult = 0;
    int result = plantingRecordService.calculateDaysWithoutCrops(givenParcel.getId(), dateFrom, dateUntil);

    System.out.println("* Seccion de prueba *");
    System.out.println("Resultado esperado: " + expectedResult);
    System.out.println("Valor devuelto por el metodo calculateDaysWithoutCrops: " + result);
    System.out.println();

    assertTrue(expectedResult == result);

    System.out.println("- Prueba pasada satisfactoriamente");
  }

  @Test
  public void testTwoDaysWithoutCrop() {
    System.out.println("********************************** Prueba dos del metodo calculateDaysWithoutCrops **********************************");
    System.out.println("- Hay diferencia de dias unicamente entre la fecha desde del periodo y la fecha de siembra del primer registro de plantacion.");
    System.out.println();
    System.out.println("Dada una parcela que tiene 5 registros de plantacion finalizados, estando cada uno de ellos inmediatamente a continuacion");
    System.out.println("de otro (es decir, la fecha de siembra de un registro de plantacion esta inmediatamente un dia despues de la fecha de cosecha");
    System.out.println("de un registro de plantacion previo), y la fecha desde esta 30 dias antes de la fecha de siembra del primer registro de plantacion");
    System.out.println("y la fecha hasta coincide con la fecha de cosecha del ultimo registro de plantacion, la aplicacion debera retornar 30 como la");
    System.out.println("cantidad de dias en los que la parcela no tuvo ningun cultivo plantado.");
    System.out.println();

    /*
     * Parcela de prueba
     */
    Parcel givenParcel = parcelService.find(2);

    System.out.println("* Parcela de prueba");
    System.out.println(givenParcel);

    /*
     * Estas fechas definen el periodo dentro del cual
     * se calcula la cantidad de dias en los que una
     * parcela no tuvo ningun cultivo plantado haciendo
     * uso de sus registros de plantacion finalizados
     * y comprendidos en dicho periodo
     */
    Calendar dateFrom = Calendar.getInstance();
    dateFrom.set(Calendar.YEAR, 2023);
    dateFrom.set(Calendar.MONTH, JANUARY);
    dateFrom.set(Calendar.DAY_OF_MONTH, 1);

    Calendar dateUntil = Calendar.getInstance();
    dateUntil.set(Calendar.YEAR, 2024);
    dateUntil.set(Calendar.MONTH, OCTOBER);
    dateUntil.set(Calendar.DAY_OF_MONTH, 16);

    System.out.println("* Periodo en el cual se obtendra la cantidad de dias en los que la parcela con ID = "
        + givenParcel.getId() + " no tuvo ningun cultivo plantado");
    System.out.println("Fecha desde: " + UtilDate.formatDate(dateFrom));
    System.out.println("Fecha hasta: " + UtilDate.formatDate(dateUntil));
    System.out.println();

    List<PlantingRecord> plantingRecords = plantingRecordService.findAllByPeriod(givenParcel.getId(), dateFrom, dateUntil);

    System.out.println("Fecha de siembra del primer registro de plantacion: "
        + UtilDate.formatDate(plantingRecords.get(0).getSeedDate()));
    System.out.println("Fecha de cosecha del ultimo registro de plantacion: "
        + UtilDate.formatDate(plantingRecords.get(plantingRecords.size() - 1).getHarvestDate()));
    System.out.println();

    System.out.println("Cantidad de registros de plantacion finalizados de esta parcela en el periodo ["
        + UtilDate.formatDate(dateFrom) + ", " + UtilDate.formatDate(dateUntil) + "]: "
        + plantingRecords.size());
    System.out.println();

    /*
     * Seccion de prueba
     */
    int expectedResult = 30;
    int result = plantingRecordService.calculateDaysWithoutCrops(givenParcel.getId(), dateFrom, dateUntil);

    System.out.println("* Seccion de prueba *");
    System.out.println("Resultado esperado: " + expectedResult);
    System.out.println("Valor devuelto por el metodo calculateDaysWithoutCrops: " + result);
    System.out.println();

    assertTrue(expectedResult == result);

    System.out.println("- Prueba pasada satisfactoriamente");
  }

  @Test
  public void testThreeDaysWithoutCrop() {
    System.out.println("********************************** Prueba tres del metodo calculateDaysWithoutCrops **********************************");
    System.out.println("- Hay diferencia de dias unicamente entre la fecha de cosecha del ultimo registro de plantacion y la fecha hasta del periodo.");
    System.out.println();
    System.out.println("Dada una parcela que tiene 5 registros de plantacion finalizados, estando cada uno de ellos inmediatamente a continuacion");
    System.out.println("de otro (es decir, la fecha de siembra de un registro de plantacion esta inmediatamente un dia despues de la fecha de cosecha");
    System.out.println("de un registro de plantacion previo), y la fecha desde coincide con la fecha de siembra del primer registro de plantacion y");
    System.out.println("la fecha hasta esta 15 dias despues de la fecha de cosecha del ultimo registro de plantacion, la aplicacion debera retornar");
    System.out.println("15 como la cantidad de dias en los que la parcela no tuvo ningun cultivo plantado.");
    System.out.println();

    /*
     * Parcela de prueba
     */
    Parcel givenParcel = parcelService.find(3);

    System.out.println("* Parcela de prueba");
    System.out.println(givenParcel);

    /*
     * Estas fechas definen el periodo dentro del cual
     * se calcula la cantidad de dias en los que una
     * parcela no tuvo ningun cultivo plantado haciendo
     * uso de sus registros de plantacion finalizados
     * y comprendidos en dicho periodo
     */
    Calendar dateFrom = Calendar.getInstance();
    dateFrom.set(Calendar.YEAR, 2023);
    dateFrom.set(Calendar.MONTH, JANUARY);
    dateFrom.set(Calendar.DAY_OF_MONTH, 1);

    Calendar dateUntil = Calendar.getInstance();
    dateUntil.set(Calendar.YEAR, 2024);
    dateUntil.set(Calendar.MONTH, DECEMBER);
    dateUntil.set(Calendar.DAY_OF_MONTH, 30);

    System.out.println("* Periodo en el cual se obtendra la cantidad de dias en los que la parcela con ID = "
        + givenParcel.getId() + " no tuvo ningun cultivo plantado");
    System.out.println("Fecha desde: " + UtilDate.formatDate(dateFrom));
    System.out.println("Fecha hasta: " + UtilDate.formatDate(dateUntil));
    System.out.println();

    List<PlantingRecord> plantingRecords = plantingRecordService.findAllByPeriod(givenParcel.getId(), dateFrom, dateUntil);

    System.out.println("Fecha de siembra del primer registro de plantacion: "
        + UtilDate.formatDate(plantingRecords.get(0).getSeedDate()));
    System.out.println("Fecha de cosecha del ultimo registro de plantacion: "
        + UtilDate.formatDate(plantingRecords.get(plantingRecords.size() - 1).getHarvestDate()));
    System.out.println();

    System.out.println("Cantidad de registros de plantacion finalizados de esta parcela en el periodo ["
        + UtilDate.formatDate(dateFrom) + ", " + UtilDate.formatDate(dateUntil) + "]: "
        + plantingRecords.size());
    System.out.println();

    /*
     * Seccion de prueba
     */
    int expectedResult = 15;
    int result = plantingRecordService.calculateDaysWithoutCrops(givenParcel.getId(), dateFrom, dateUntil);

    System.out.println("* Seccion de prueba *");
    System.out.println("Resultado esperado: " + expectedResult);
    System.out.println("Valor devuelto por el metodo calculateDaysWithoutCrops: " + result);
    System.out.println();

    assertTrue(expectedResult == result);

    System.out.println("- Prueba pasada satisfactoriamente");
  }

  @Test
  public void testFourDaysWithoutCrop() {
    System.out.println("********************************** Prueba cuatro del metodo calculateDaysWithoutCrops **********************************");
    System.out.println("- Hay diferencia de dias unicamente entre las fechas de los registros de plantacion.");
    System.out.println();
    System.out.println("Dada una parcela que tiene 5 registros de plantacion finalizados con el siguiente espacio temporal entre ellos:");
    System.out.println("- entre la fecha de cosecha del primer registro de plantacion y la fecha de siembra del segundo registro de plantacion hay");
    System.out.println("10 dias de separacion.");
    System.out.println("- entre la fecha de cosecha del segundo registro de plantacion y la fecha de siembra del tercer registro de plantacion hay");
    System.out.println("5 dias de separacion.");
    System.out.println("- entre la fecha de cosecha del tercer registro de plantacion y la fecha de siembra del cuarto registro de plantacion hay 2");
    System.out.println("dias de separacion.");
    System.out.println("- entre la fecha de cosecha del cuarto registro de plantacion y la fecha de siembra del quinto registro de plantacion hay 7");
    System.out.println("dias de separacion.");
    System.out.println();
    System.out.println("La fecha desde coincide con la fecha de siembra del primer registro de plantacion y la fecha hasta coincide con la fecha de");
    System.out.println("cosecha del ultimo registro de plantacion. Por lo tanto, la aplicacion debera retornar 24 como la cantidad de dias en los que");
    System.out.println("la parcela no tuvo ningun cultivo plantado.");
    System.out.println();

    /*
     * Parcela de prueba
     */
    Parcel givenParcel = parcelService.find(4);

    System.out.println("* Parcela de prueba");
    System.out.println(givenParcel);

    /*
     * Estas fechas definen el periodo dentro del cual
     * se calcula la cantidad de dias en los que una
     * parcela no tuvo ningun cultivo plantado haciendo
     * uso de sus registros de plantacion finalizados
     * y comprendidos en dicho periodo
     */
    Calendar dateFrom = Calendar.getInstance();
    dateFrom.set(Calendar.YEAR, 2023);
    dateFrom.set(Calendar.MONTH, JANUARY);
    dateFrom.set(Calendar.DAY_OF_MONTH, 1);

    Calendar dateUntil = Calendar.getInstance();
    dateUntil.set(Calendar.YEAR, 2024);
    dateUntil.set(Calendar.MONTH, OCTOBER);
    dateUntil.set(Calendar.DAY_OF_MONTH, 31);

    System.out.println("* Periodo en el cual se obtendra la cantidad de dias en los que la parcela con ID = "
        + givenParcel.getId() + " no tuvo ningun cultivo plantado");
    System.out.println("Fecha desde: " + UtilDate.formatDate(dateFrom));
    System.out.println("Fecha hasta: " + UtilDate.formatDate(dateUntil));
    System.out.println();

    List<PlantingRecord> plantingRecords = plantingRecordService.findAllByPeriod(givenParcel.getId(), dateFrom, dateUntil);

    System.out.println("Fecha de siembra del primer registro de plantacion: "
        + UtilDate.formatDate(plantingRecords.get(0).getSeedDate()));
    System.out.println("Fecha de cosecha del ultimo registro de plantacion: "
        + UtilDate.formatDate(plantingRecords.get(plantingRecords.size() - 1).getHarvestDate()));
    System.out.println();

    System.out.println("Cantidad de registros de plantacion finalizados de esta parcela en el periodo ["
        + UtilDate.formatDate(dateFrom) + ", " + UtilDate.formatDate(dateUntil) + "]: "
        + plantingRecords.size());
    System.out.println();

    /*
     * Seccion de prueba
     */
    int expectedResult = 24;
    int result = plantingRecordService.calculateDaysWithoutCrops(givenParcel.getId(), dateFrom, dateUntil);

    System.out.println("* Seccion de prueba *");
    System.out.println("Resultado esperado: " + expectedResult);
    System.out.println("Valor devuelto por el metodo calculateDaysWithoutCrops: " + result);
    System.out.println();

    assertTrue(expectedResult == result);

    System.out.println("- Prueba pasada satisfactoriamente");
  }

  @Test
  public void testFiveDaysWithoutCrop() {
    System.out.println("********************************** Prueba cinco del metodo calculateDaysWithoutCrops **********************************");
    System.out.println("- Hay diferencia de dias entre las fechas del periodo y las fechas de los registros de plantacion e incluso entre las fechas");
    System.out.println("mismas de dichos registros.");
    System.out.println();
    System.out.println("Dada una parcela que tiene 5 registros de plantacion finalizados con el siguiente espacio temporal entre ellos:");
    System.out.println("- entre la fecha de cosecha del primer registro de plantacion y la fecha de siembra del segundo registro de plantacion hay");
    System.out.println("15 dias de separacion.");
    System.out.println("- entre la fecha de cosecha del segundo registro de plantacion y la fecha de siembra del tercer registro de plantacion hay");
    System.out.println("12 dias de separacion.");
    System.out.println("- entre la fecha de cosecha del tercer registro de plantacion y la fecha de siembra del cuarto registro de plantacion hay 7");
    System.out.println("dias de separacion.");
    System.out.println("- entre la fecha de cosecha del cuarto registro de plantacion y la fecha de siembra del quinto registro de plantacion hay 5");
    System.out.println("dias de separacion.");
    System.out.println();
    System.out.println("La fecha desde esta 13 dias antes de la fecha de siembra del primer registro de plantacion y la fecha hasta esta 9 dias despues");
    System.out.println("de la fecha de cosecha del ultimo registro de plantacion. Por lo tanto, la aplicacion debera retornar 61 como la cantidad de dias");
    System.out.println("en los que la parcela no tuvo ningun cultivo plantado.");
    System.out.println();

    /*
     * Parcela de prueba
     */
    Parcel givenParcel = parcelService.find(5);

    System.out.println("* Parcela de prueba");
    System.out.println(givenParcel);

    /*
     * Estas fechas definen el periodo dentro del cual
     * se calcula la cantidad de dias en los que una
     * parcela no tuvo ningun cultivo plantado haciendo
     * uso de sus registros de plantacion finalizados
     * y comprendidos en dicho periodo
     */
    Calendar dateFrom = Calendar.getInstance();
    dateFrom.set(Calendar.YEAR, 2022);
    dateFrom.set(Calendar.MONTH, DECEMBER);
    dateFrom.set(Calendar.DAY_OF_MONTH, 19);

    Calendar dateUntil = Calendar.getInstance();
    dateUntil.set(Calendar.YEAR, 2025);
    dateUntil.set(Calendar.MONTH, APRIL);
    dateUntil.set(Calendar.DAY_OF_MONTH, 3);

    System.out.println("* Periodo en el cual se obtendra la cantidad de dias en los que la parcela con ID = "
        + givenParcel.getId() + " no tuvo ningun cultivo plantado");
    System.out.println("Fecha desde: " + UtilDate.formatDate(dateFrom));
    System.out.println("Fecha hasta: " + UtilDate.formatDate(dateUntil));
    System.out.println();

    List<PlantingRecord> plantingRecords = plantingRecordService.findAllByPeriod(givenParcel.getId(), dateFrom, dateUntil);

    System.out.println("Fecha de siembra del primer registro de plantacion: "
        + UtilDate.formatDate(plantingRecords.get(0).getSeedDate()));
    System.out.println("Fecha de cosecha del ultimo registro de plantacion: "
        + UtilDate.formatDate(plantingRecords.get(plantingRecords.size() - 1).getHarvestDate()));
    System.out.println();

    System.out.println("Cantidad de registros de plantacion finalizados de esta parcela en el periodo ["
        + UtilDate.formatDate(dateFrom) + ", " + UtilDate.formatDate(dateUntil) + "]: "
        + plantingRecords.size());
    System.out.println();

    /*
     * Seccion de prueba
     */
    int expectedResult = 61;
    int result = plantingRecordService.calculateDaysWithoutCrops(givenParcel.getId(), dateFrom, dateUntil);

    System.out.println("* Seccion de prueba *");
    System.out.println("Resultado esperado: " + expectedResult);
    System.out.println("Valor devuelto por el metodo calculateDaysWithoutCrops: " + result);
    System.out.println();

    assertTrue(expectedResult == result);

    System.out.println("- Prueba pasada satisfactoriamente");
  }

  @Test
  public void testSixDaysWithoutCrop() {
    System.out.println("********************************** Prueba seis del metodo calculateDaysWithoutCrops **********************************");
    System.out.println("- Hay diferencia de años entre la fecha desde y la fecha de siembra del primer registro de plantacion, hay diferencia de años");
    System.out.println("y dias entre las fechas de los registros de plantacion, y hay diferencia de años entre la fecha de cosecha del ultimo registro");
    System.out.println("de plantacion y la fecha hasta.");
    System.out.println();
    System.out.println("Dada una parcela que tiene 5 registros de plantacion finalizados con el siguiente espacio temporal entre ellos:");
    System.out.println("- entre la fecha de cosecha del primer registro de plantacion y la fecha de siembra del segundo registro de plantacion hay 365");
    System.out.println("dias de separacion.");
    System.out.println("- entre la fecha de cosecha del segundo registro de plantacion y la fecha de siembra del tercer registro de plantacion hay 32");
    System.out.println("dias de separacion.");
    System.out.println("- entre la fecha de cosecha del tercer registro de plantacion y la fecha de siembra del cuarto registro de plantacion hay 27");
    System.out.println("dias de separacion.");
    System.out.println("- entre la fecha de cosecha del cuarto registro de plantacion y la fecha de siembra del quinto registro de plantacion hay 390");
    System.out.println("dias de separacion.");
    System.out.println();
    System.out.println("La fecha desde esta 730 dias antes de la fecha de siembra del primer registro de plantacion y la fecha esta 430 dias despues de");
    System.out.println("la fecha de cosecha del ultimo registro de plantacion. Por lo tanto, la aplicacion debera retornar 1974 como la cantidad de dias");
    System.out.println("en los que una parcela no tuvo ningun cultivo plantado.");
    System.out.println();

    /*
     * Parcela de prueba
     */
    Parcel givenParcel = parcelService.find(6);

    System.out.println("* Parcela de prueba");
    System.out.println(givenParcel);

    /*
     * Estas fechas definen el periodo dentro del cual
     * se calcula la cantidad de dias en los que una
     * parcela no tuvo ningun cultivo plantado haciendo
     * uso de sus registros de plantacion finalizados
     * y comprendidos en dicho periodo
     */
    Calendar dateFrom = Calendar.getInstance();
    dateFrom.set(Calendar.YEAR, 2020);
    dateFrom.set(Calendar.MONTH, JANUARY);
    dateFrom.set(Calendar.DAY_OF_MONTH, 2);

    Calendar dateUntil = Calendar.getInstance();
    dateUntil.set(Calendar.YEAR, 2027);
    dateUntil.set(Calendar.MONTH, MARCH);
    dateUntil.set(Calendar.DAY_OF_MONTH, 21);

    System.out.println("* Periodo en el cual se obtendra la cantidad de dias en los que la parcela con ID = "
        + givenParcel.getId() + " no tuvo ningun cultivo plantado");
    System.out.println("Fecha desde: " + UtilDate.formatDate(dateFrom));
    System.out.println("Fecha hasta: " + UtilDate.formatDate(dateUntil));
    System.out.println();

    List<PlantingRecord> plantingRecords = plantingRecordService.findAllByPeriod(givenParcel.getId(), dateFrom, dateUntil);

    System.out.println("Fecha de siembra del primer registro de plantacion: "
        + UtilDate.formatDate(plantingRecords.get(0).getSeedDate()));
    System.out.println("Fecha de cosecha del ultimo registro de plantacion: "
        + UtilDate.formatDate(plantingRecords.get(plantingRecords.size() - 1).getHarvestDate()));
    System.out.println();

    System.out.println("Cantidad de registros de plantacion finalizados de esta parcela en el periodo ["
        + UtilDate.formatDate(dateFrom) + ", " + UtilDate.formatDate(dateUntil) + "]: "
        + plantingRecords.size());
    System.out.println();

    /*
     * Seccion de prueba
     */
    int expectedResult = 1974;
    int result = plantingRecordService.calculateDaysWithoutCrops(givenParcel.getId(), dateFrom, dateUntil);

    System.out.println("* Seccion de prueba *");
    System.out.println("Resultado esperado: " + expectedResult);
    System.out.println("Valor devuelto por el metodo calculateDaysWithoutCrops: " + result);
    System.out.println();

    assertTrue(expectedResult == result);

    System.out.println("- Prueba pasada satisfactoriamente");
  }

  @Test
  public void testSeveDaysWithoutCrop() {
    System.out.println("********************************** Prueba siete del metodo calculateDaysWithoutCrops **********************************");
    System.out.println("- Calculo de la cantidad de dias en los que una parcela no tuvo ningun cultivo plantado para una parcela que no tiene");
    System.out.println("ningun registro de plantacion finalizado en un periodo definido por dos fechas.");
    System.out.println();
    System.out.println("Dada una parcela que no tiene ningun registro de plantacion finalizado en un periodo definido por una fecha desde y una");
    System.out.println("fecha hasta, la aplicacion debera retornar -1 como valor indicativo de que la cantidad de dias en los que una parcela no");
    System.out.println("tuvo ningun cultivo plantado en el periodo dado, no esta disponible.");
    System.out.println();

    /*
     * Parcela de prueba
     */
    Parcel givenParcel = parcelService.find(7);

    System.out.println("* Parcela de prueba");
    System.out.println(givenParcel);

    /*
     * Estas fechas definen el periodo dentro del cual
     * se calcula la cantidad de dias en los que una
     * parcela no tuvo ningun cultivo plantado haciendo
     * uso de sus registros de plantacion finalizados
     * y comprendidos en dicho periodo
     */
    Calendar dateFrom = Calendar.getInstance();
    dateFrom.set(Calendar.YEAR, 2020);
    dateFrom.set(Calendar.MONTH, JANUARY);
    dateFrom.set(Calendar.DAY_OF_MONTH, 2);

    Calendar dateUntil = Calendar.getInstance();
    dateUntil.set(Calendar.YEAR, 2021);
    dateUntil.set(Calendar.MONTH, FEBRUARY);
    dateUntil.set(Calendar.DAY_OF_MONTH, 2);

    System.out.println("* Periodo en el cual se obtendra la cantidad de dias en los que la parcela con ID = "
        + givenParcel.getId() + " no tuvo ningun cultivo plantado");
    System.out.println("Fecha desde: " + UtilDate.formatDate(dateFrom));
    System.out.println("Fecha hasta: " + UtilDate.formatDate(dateUntil));
    System.out.println();

    List<PlantingRecord> plantingRecords = plantingRecordService.findAllByPeriod(givenParcel.getId(), dateFrom, dateUntil);

    System.out.println("Cantidad de registros de plantacion finalizados de esta parcela en el periodo ["
        + UtilDate.formatDate(dateFrom) + ", " + UtilDate.formatDate(dateUntil) + "]: "
        + plantingRecords.size());
    System.out.println();

    /*
     * Seccion de prueba
     */
    int expectedResult = -1;
    int result = plantingRecordService.calculateDaysWithoutCrops(givenParcel.getId(), dateFrom, dateUntil);

    System.out.println("* Seccion de prueba *");
    System.out.println("Resultado esperado: " + expectedResult);
    System.out.println("Valor devuelto por el metodo calculateDaysWithoutCrops: " + result);
    System.out.println();

    assertTrue(expectedResult == result);

    System.out.println("- Prueba pasada satisfactoriamente");
  }

  @Test
  public void testEightDaysWithoutCrop() {
    System.out.println("********************************** Prueba ocho del metodo calculateDaysWithoutCrops **********************************");
    System.out.println("- La fecha desde del periodo es estrictamente menor a la fecha de siembra de un registro de plantacion finalizado, y la");
    System.out.println("fecha hasta del periodo es igual a la fecha de siembra de dicho registro.");
    System.out.println();
    System.out.println("Dada una parcela que tiene un registro de plantacion finalizado con la fecha de siembra 31/1/2023 y la fecha de cosecha");
    System.out.println("24/6/2023, y un periodo definido por la fecha desde 1/1/2023 y y la fecha hasta 31/1/2023, la aplicacion debera retornar");
    System.out.println("30 como la cantidad de dias en los que una parcela no tuvo ningun cultivo plantado.");
    System.out.println();

    /*
     * Parcela de prueba
     */
    Parcel givenParcel = parcelService.find(8);

    System.out.println("* Parcela de prueba");
    System.out.println(givenParcel);

    /*
     * Estas fechas definen el periodo dentro del cual
     * se calcula la cantidad de dias en los que una
     * parcela no tuvo ningun cultivo plantado haciendo
     * uso de sus registros de plantacion finalizados
     * y comprendidos en dicho periodo
     */
    Calendar dateFrom = Calendar.getInstance();
    dateFrom.set(Calendar.YEAR, 2023);
    dateFrom.set(Calendar.MONTH, JANUARY);
    dateFrom.set(Calendar.DAY_OF_MONTH, 1);

    Calendar dateUntil = Calendar.getInstance();
    dateUntil.set(Calendar.YEAR, 2023);
    dateUntil.set(Calendar.MONTH, JANUARY);
    dateUntil.set(Calendar.DAY_OF_MONTH, 31);

    System.out.println("* Periodo en el cual se obtendra la cantidad de dias en los que la parcela con ID = "
        + givenParcel.getId() + " no tuvo ningun cultivo plantado");
    System.out.println("Fecha desde: " + UtilDate.formatDate(dateFrom));
    System.out.println("Fecha hasta: " + UtilDate.formatDate(dateUntil));
    System.out.println();

    List<PlantingRecord> plantingRecords = plantingRecordService.findAllByPeriod(givenParcel.getId(), dateFrom, dateUntil);

    System.out.println("Cantidad de registros de plantacion finalizados de esta parcela en el periodo ["
        + UtilDate.formatDate(dateFrom) + ", " + UtilDate.formatDate(dateUntil) + "]: "
        + plantingRecords.size());
    System.out.println();

    System.out.println("Fecha de siembra del registro de plantacion finalizado de la parcela con ID = "
    + givenParcel.getId() + ": " + UtilDate.formatDate(plantingRecords.get(0).getSeedDate()));

    System.out.println("Fecha de cosecha del registro de plantacion finalizado de la parcela con ID = "
    + givenParcel.getId() + ": " + UtilDate.formatDate(plantingRecords.get(0).getHarvestDate()));
    System.out.println();

    /*
     * Seccion de prueba
     */
    int expectedResult = 30;
    int result = plantingRecordService.calculateDaysWithoutCrops(givenParcel.getId(), dateFrom, dateUntil);

    System.out.println("* Seccion de prueba *");
    System.out.println("Resultado esperado: " + expectedResult);
    System.out.println("Valor devuelto por el metodo calculateDaysWithoutCrops: " + result);
    System.out.println();

    assertTrue(expectedResult == result);

    System.out.println("- Prueba pasada satisfactoriamente");
  }

  @Test
  public void testNine() {
    System.out.println("********************************** Prueba nueve del metodo calculateDaysWithoutCrops **********************************");
    System.out.println("- La fecha desde del periodo es igual a la fecha de siembra de un registro de plantacion finalizado, y la fecha hasta es");
    System.out.println("estrictamente mayor a la fecha de siembra y menor o igual a la fecha de cosecha de dicho registro.");
    System.out.println();
    System.out.println("Dada una parcela que tiene un registro de plantacion finalizado con la fecha de siembra 31/1/2023 y la fecha de cosecha");
    System.out.println("24/6/2023, y un periodo definido por la fecha desde 31/1/2023 y y la fecha hasta 22/5/2023, la aplicacion debera retornar");
    System.out.println("0 como la cantidad de dias en los que una parcela no tuvo ningun cultivo plantado.");
    System.out.println();

    /*
     * Parcela de prueba
     */
    Parcel givenParcel = parcelService.find(9);

    System.out.println("* Parcela de prueba");
    System.out.println(givenParcel);

    /*
     * Estas fechas definen el periodo dentro del cual
     * se calcula la cantidad de dias en los que una
     * parcela no tuvo ningun cultivo plantado haciendo
     * uso de sus registros de plantacion finalizados
     * y comprendidos en dicho periodo
     */
    Calendar dateFrom = Calendar.getInstance();
    dateFrom.set(Calendar.YEAR, 2023);
    dateFrom.set(Calendar.MONTH, JANUARY);
    dateFrom.set(Calendar.DAY_OF_MONTH, 31);

    Calendar dateUntil = Calendar.getInstance();
    dateUntil.set(Calendar.YEAR, 2023);
    dateUntil.set(Calendar.MONTH, MAY);
    dateUntil.set(Calendar.DAY_OF_MONTH, 22);

    System.out.println("* Periodo en el cual se obtendra la cantidad de dias en los que la parcela con ID = "
        + givenParcel.getId() + " no tuvo ningun cultivo plantado");
    System.out.println("Fecha desde: " + UtilDate.formatDate(dateFrom));
    System.out.println("Fecha hasta: " + UtilDate.formatDate(dateUntil));
    System.out.println();

    List<PlantingRecord> plantingRecords = plantingRecordService.findAllByPeriod(givenParcel.getId(), dateFrom, dateUntil);

    System.out.println("Cantidad de registros de plantacion finalizados de esta parcela en el periodo ["
        + UtilDate.formatDate(dateFrom) + ", " + UtilDate.formatDate(dateUntil) + "]: "
        + plantingRecords.size());
    System.out.println();

    System.out.println("Fecha de siembra del registro de plantacion finalizado de la parcela con ID = "
    + givenParcel.getId() + ": " + UtilDate.formatDate(plantingRecords.get(0).getSeedDate()));

    System.out.println("Fecha de cosecha del registro de plantacion finalizado de la parcela con ID = "
    + givenParcel.getId() + ": " + UtilDate.formatDate(plantingRecords.get(0).getHarvestDate()));
    System.out.println();

    /*
     * Seccion de prueba
     */
    int expectedResult = 0;
    int result = plantingRecordService.calculateDaysWithoutCrops(givenParcel.getId(), dateFrom, dateUntil);

    System.out.println("* Seccion de prueba *");
    System.out.println("Resultado esperado: " + expectedResult);
    System.out.println("Valor devuelto por el metodo calculateDaysWithoutCrops: " + result);
    System.out.println();

    assertTrue(expectedResult == result);

    System.out.println("- Prueba pasada satisfactoriamente");
  }

  @Test
  public void testTen() {
    System.out.println("********************************** Prueba diez del metodo calculateDaysWithoutCrops **********************************");
    System.out.println("- La fecha hasta del periodo es estrictamente mayor a la fecha de cosecha de un registro de plantacion finalizado, y la");
    System.out.println("fecha desde del periodo es igual a la fecha de cosecha de dicho registro.");
    System.out.println();
    System.out.println("Dada una parcela que tiene un registro de plantacion finalizado con la fecha de siembra 14/6/2025 y la fecha de cosecha");
    System.out.println("31/10/2025, y un periodo definido por la fecha desde 31/10/2025 y y la fecha hasta 30/11/2025, la aplicacion debera retornar");
    System.out.println("30 como la cantidad de dias en los que una parcela no tuvo ningun cultivo plantado.");
    System.out.println();

    /*
     * Parcela de prueba
     */
    Parcel givenParcel = parcelService.find(10);

    System.out.println("* Parcela de prueba");
    System.out.println(givenParcel);

    /*
     * Estas fechas definen el periodo dentro del cual
     * se calcula la cantidad de dias en los que una
     * parcela no tuvo ningun cultivo plantado haciendo
     * uso de sus registros de plantacion finalizados
     * y comprendidos en dicho periodo
     */
    Calendar dateFrom = Calendar.getInstance();
    dateFrom.set(Calendar.YEAR, 2025);
    dateFrom.set(Calendar.MONTH, OCTOBER);
    dateFrom.set(Calendar.DAY_OF_MONTH, 31);

    Calendar dateUntil = Calendar.getInstance();
    dateUntil.set(Calendar.YEAR, 2025);
    dateUntil.set(Calendar.MONTH, NOVEMBER);
    dateUntil.set(Calendar.DAY_OF_MONTH, 30);

    System.out.println("* Periodo en el cual se obtendra la cantidad de dias en los que la parcela con ID = "
        + givenParcel.getId() + " no tuvo ningun cultivo plantado");
    System.out.println("Fecha desde: " + UtilDate.formatDate(dateFrom));
    System.out.println("Fecha hasta: " + UtilDate.formatDate(dateUntil));
    System.out.println();

    List<PlantingRecord> plantingRecords = plantingRecordService.findAllByPeriod(givenParcel.getId(), dateFrom, dateUntil);

    System.out.println("Cantidad de registros de plantacion finalizados de esta parcela en el periodo ["
        + UtilDate.formatDate(dateFrom) + ", " + UtilDate.formatDate(dateUntil) + "]: "
        + plantingRecords.size());
    System.out.println();

    System.out.println("Fecha de siembra del registro de plantacion finalizado de la parcela con ID = "
    + givenParcel.getId() + ": " + UtilDate.formatDate(plantingRecords.get(0).getSeedDate()));

    System.out.println("Fecha de cosecha del registro de plantacion finalizado de la parcela con ID = "
    + givenParcel.getId() + ": " + UtilDate.formatDate(plantingRecords.get(0).getHarvestDate()));
    System.out.println();

    /*
     * Seccion de prueba
     */
    int expectedResult = 30;
    int result = plantingRecordService.calculateDaysWithoutCrops(givenParcel.getId(), dateFrom, dateUntil);

    System.out.println("* Seccion de prueba *");
    System.out.println("Resultado esperado: " + expectedResult);
    System.out.println("Valor devuelto por el metodo calculateDaysWithoutCrops: " + result);
    System.out.println();

    assertTrue(expectedResult == result);

    System.out.println("- Prueba pasada satisfactoriamente");
  }

  @Test
  public void testEleven() {
    System.out.println("********************************** Prueba once del metodo calculateDaysWithoutCrops **********************************");
    System.out.println("- La fecha hasta del periodo es igual a la fecha de cosecha de un registro de plantacion finalizado, y la fecha desde del");
    System.out.println("periodo es mayor o igual a la fecha de siembra y es estrictamente menor a la fecha de cosecha de dicho registro.");
    System.out.println();
    System.out.println("Dada una parcela que tiene un registro de plantacion finalizado con la fecha de siembra 14/6/2025 y la fecha de cosecha");
    System.out.println("31/10/2025, y un periodo definido por la fecha desde 5/7/2025 y y la fecha hasta 31/10/2025, la aplicacion debera retornar");
    System.out.println("0 como la cantidad de dias en los que una parcela no tuvo ningun cultivo plantado.");
    System.out.println();

    /*
     * Parcela de prueba
     */
    Parcel givenParcel = parcelService.find(11);

    System.out.println("* Parcela de prueba");
    System.out.println(givenParcel);

    /*
     * Estas fechas definen el periodo dentro del cual
     * se calcula la cantidad de dias en los que una
     * parcela no tuvo ningun cultivo plantado haciendo
     * uso de sus registros de plantacion finalizados
     * y comprendidos en dicho periodo
     */
    Calendar dateFrom = Calendar.getInstance();
    dateFrom.set(Calendar.YEAR, 2025);
    dateFrom.set(Calendar.MONTH, JULY);
    dateFrom.set(Calendar.DAY_OF_MONTH, 5);

    Calendar dateUntil = Calendar.getInstance();
    dateUntil.set(Calendar.YEAR, 2025);
    dateUntil.set(Calendar.MONTH, OCTOBER);
    dateUntil.set(Calendar.DAY_OF_MONTH, 31);

    System.out.println("* Periodo en el cual se obtendra la cantidad de dias en los que la parcela con ID = "
        + givenParcel.getId() + " no tuvo ningun cultivo plantado");
    System.out.println("Fecha desde: " + UtilDate.formatDate(dateFrom));
    System.out.println("Fecha hasta: " + UtilDate.formatDate(dateUntil));
    System.out.println();

    List<PlantingRecord> plantingRecords = plantingRecordService.findAllByPeriod(givenParcel.getId(), dateFrom, dateUntil);

    System.out.println("Cantidad de registros de plantacion finalizados de esta parcela en el periodo ["
        + UtilDate.formatDate(dateFrom) + ", " + UtilDate.formatDate(dateUntil) + "]: "
        + plantingRecords.size());
    System.out.println();

    System.out.println("Fecha de siembra del registro de plantacion finalizado de la parcela con ID = "
    + givenParcel.getId() + ": " + UtilDate.formatDate(plantingRecords.get(0).getSeedDate()));

    System.out.println("Fecha de cosecha del registro de plantacion finalizado de la parcela con ID = "
    + givenParcel.getId() + ": " + UtilDate.formatDate(plantingRecords.get(0).getHarvestDate()));
    System.out.println();

    /*
     * Seccion de prueba
     */
    int expectedResult = 0;
    int result = plantingRecordService.calculateDaysWithoutCrops(givenParcel.getId(), dateFrom, dateUntil);

    System.out.println("* Seccion de prueba *");
    System.out.println("Resultado esperado: " + expectedResult);
    System.out.println("Valor devuelto por el metodo calculateDaysWithoutCrops: " + result);
    System.out.println();

    assertTrue(expectedResult == result);

    System.out.println("- Prueba pasada satisfactoriamente");
  }

  @Test
  public void testTwelve() {
    System.out.println("********************************** Prueba doce del metodo calculateDaysWithoutCrops **********************************");
    System.out.println("- La fecha desde del periodo tiene un año menos que la fecha de siembra del primer registro de plantacion finalizado, y");
    System.out.println("la fecha hasta es mayor o igual a la fecha de siembra del ultimo registro de plantacion finalizado y tiene un año menos");
    System.out.println("que la fecha de cosecha de este registro de plantacion.");
    System.out.println();
    System.out.println("Dada una parcela que tiene 4 registros de plantacion, los cuales, tienen las siguientes fechas:");
    System.out.println("- 1. fecha de siembra: 1/1/2023, fecha de cosecha 10/4/2023");
    System.out.println("- 2. fecha de siembra: 21/4/2023, fecha de cosecha: 8/8/2023, dias sin cultivo plantado entre la fecha de siembra y la");
    System.out.println("fecha de cosecha del registro de plantacion anterior: 10");
    System.out.println("- 3. fecha de siembra: 14/8/2023, fecha de cosecha: 16/12/2023, dias sin cultivo plantado entre la fecha de siembra y la");
    System.out.println("fecha de cosecha del registro de plantacion anterior: 5");
    System.out.println("- 4. fecha de siembra: 19/12/2023, fecha de cosecha: 4/5/2024, dias sin cultivo plantado entre la fecha de siembra y la");
    System.out.println("fecha de cosecha del registro de plantacion anterior: 2");
    System.out.println();
    System.out.println("Dado el periodo definido por la fecha desde 28/11/2022 (hay 34 dias entre esta fecha y la fecha 31/12/2023, la cual es");
    System.out.println("inmediatamente anterior a la fecha de siembra del primer registro de plantacion) y la fecha hasta 31/12/2023 (esta dentro");
    System.out.println("del periodo definido por la fecha de siembra y la fecha de cosecha del ultimo registro de plantacion), la aplicacion debera");
    System.out.println("retornar 51 como la cantidad de dias en los que una parcela no tuvo ningun cultivo plantado.");
    System.out.println();

    /*
     * Parcela de prueba
     */
    Parcel givenParcel = parcelService.find(12);

    System.out.println("* Parcela de prueba");
    System.out.println(givenParcel);

    /*
     * Estas fechas definen el periodo dentro del cual
     * se calcula la cantidad de dias en los que una
     * parcela no tuvo ningun cultivo plantado haciendo
     * uso de sus registros de plantacion finalizados
     * y comprendidos en dicho periodo
     */
    Calendar dateFrom = Calendar.getInstance();
    dateFrom.set(Calendar.YEAR, 2022);
    dateFrom.set(Calendar.MONTH, NOVEMBER);
    dateFrom.set(Calendar.DAY_OF_MONTH, 28);

    Calendar dateUntil = Calendar.getInstance();
    dateUntil.set(Calendar.YEAR, 2023);
    dateUntil.set(Calendar.MONTH, DECEMBER);
    dateUntil.set(Calendar.DAY_OF_MONTH, 31);

    System.out.println("* Periodo en el cual se obtendra la cantidad de dias en los que la parcela con ID = "
        + givenParcel.getId() + " no tuvo ningun cultivo plantado");
    System.out.println("Fecha desde: " + UtilDate.formatDate(dateFrom));
    System.out.println("Fecha hasta: " + UtilDate.formatDate(dateUntil));
    System.out.println();

    List<PlantingRecord> plantingRecords = plantingRecordService.findAllByPeriod(givenParcel.getId(), dateFrom, dateUntil);

    System.out.println("Cantidad de registros de plantacion finalizados de esta parcela en el periodo ["
        + UtilDate.formatDate(dateFrom) + ", " + UtilDate.formatDate(dateUntil) + "]: "
        + plantingRecords.size());
    System.out.println();

    System.out.println("Fecha de siembra del primer registro de plantacion finalizado de la parcela con ID = "
    + givenParcel.getId() + ": " + UtilDate.formatDate(plantingRecords.get(0).getSeedDate()));

    System.out.println("Fecha de cosecha del ultimo registro de plantacion finalizado de la parcela con ID = "
    + givenParcel.getId() + ": " + UtilDate.formatDate(plantingRecords.get(plantingRecords.size() - 1).getHarvestDate()));
    System.out.println();

    /*
     * Seccion de prueba
     */
    int expectedResult = 51;
    int result = plantingRecordService.calculateDaysWithoutCrops(givenParcel.getId(), dateFrom, dateUntil);

    System.out.println("* Seccion de prueba *");
    System.out.println("Resultado esperado: " + expectedResult);
    System.out.println("Valor devuelto por el metodo calculateDaysWithoutCrops: " + result);
    System.out.println();

    assertTrue(expectedResult == result);

    System.out.println("- Prueba pasada satisfactoriamente");
  }

  @AfterClass
  public static void postTest() {
    // Cierra las conexiones
    entityManager.close();
    entityManagerFactory.close();
  }

}
