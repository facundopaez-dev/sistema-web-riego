import static org.junit.Assert.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import stateless.ParcelServiceBean;
import stateless.PlantingRecordServiceBean;
import stateless.PlantingRecordStatusServiceBean;
import stateless.CropServiceBean;
import model.Parcel;
import model.Crop;
import model.PlantingRecord;
import model.PlantingRecordStatus;
import java.util.Calendar;
import java.util.Random;

public class PlantingRecordGeneratorTest {

  private static EntityManager entityManager;
  private static EntityManagerFactory entityManagerFactory;

  private static ParcelServiceBean parcelService;
  private static PlantingRecordServiceBean plantingRecordService;
  private static CropServiceBean cropService;
  private static PlantingRecordStatusServiceBean plantingRecordStatusService;

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

    plantingRecordService = new PlantingRecordServiceBean();
    plantingRecordService.setEntityManager(entityManager);

    cropService = new CropServiceBean();
    cropService.setEntityManager(entityManager);

    plantingRecordStatusService = new PlantingRecordStatusServiceBean();
    plantingRecordStatusService.setEntityManager(entityManager);
  }

  @Test
  public void test() {
    System.out
        .println("********************** Ejecucion del generador de registros de plantacion **********************");

    /*
     * En este arreglo residen los IDs de los cultivos
     * en la cantidad en la que se quiere crear registros
     * de plantacion con ellos. Por ejemplo, si se quiere
     * crear 5 registros de plantacion para el tomate, se
     * debe repetir 5 veces el ID del tomate dentro de este
     * arreglo.
     */
    int[] cropsIds = { 13, 13, 13, 13, 13, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 37, 37, 37, 37, 37, 37, 37, 37, 37,
        37, 37, 37, 37, 37, 37 };

    System.out.println("Cantidad de registros de plantacion a crear y persisitir: " + cropsIds.length);
    System.out.println();
    System.out
        .println("El cuadro A2.5 de la pagina 215 del libro 'Evapotranspiracion del cultivo, numero 56' de la FAO");
    System.out.println("tiene el numero de dia en el año para cada dia del año, incluido el numero de dia en el año");
    System.out.println("para cada dia de un año bisiesto.");
    System.out.println();

    /*
     * Esta variable se utiliza para establecer un dia en
     * el año de la nueva fecha de siembra. Dicho dia se
     * calcula a partir de la suma entre el numero de dia
     * en el año de una fecha de cosecha y un numero
     * aleatorio entre 1 y 31.
     * 
     * La nueva fecha de siembra es para cada nuevo registro
     * de plantacion que se crea y persiste.
     */
    int dayYearNewSeedDate = 0;
    int numberPlantingRecordsPersisted = 0;
    int randomValue = 0;

    /*
     * Se usa el tiempo en milisegundos como semilla para
     * generar numeros aleatorios diferentes en cada ejecucion
     * de esta prueba
     */
    Random randomValueGenerator = new Random(System.currentTimeMillis());
    PlantingRecord newPlantingRecord;
    PlantingRecordStatus finishedStatus = plantingRecordStatusService.findFinishedStatus();
    Parcel givenParcel = parcelService.find(1);
    Crop givenCrop;
    Calendar harvestDate;

    /*
     * Fecha de siembra a partir de la cual se crea el primer
     * registro de plantacion. El segundo registro de plantacion
     * se crea con una fecha de siembra igual a una cantidad
     * aleatoria distinta de cero de dias despues de la fecha de
     * cosecha del primer registro de plantacion. El tercer registro
     * de plantacion se crea con una fecha de siembra igual a una
     * cantidad aleatoria distinta de cero de dias despues de la
     * fecha cosecha del segundo registro de plantacion. Y asi
     * sucesivamente sea hace con cada uno de los registros de
     * plantacion a crear y persistir.
     * 
     * Lo que quiero decir con esto es que el par de fechas
     * (fecha de siembra, fecha de cosecha) de los registros de
     * plantacion a crear y persistir se ve afectada por la
     * fecha de siembra con la que se inicia la creacion y
     * persistencia de registros de plantacion.
     */
    Calendar seedDate = Calendar.getInstance();
    seedDate.set(Calendar.YEAR, 2023);
    seedDate.set(Calendar.MONTH, JANUARY);
    seedDate.set(Calendar.DAY_OF_MONTH, 1);

    for (int i = 0; i < cropsIds.length; i++) {
      givenCrop = cropService.find(cropsIds[i]);
      harvestDate = cropService.calculateHarvestDate(seedDate, givenCrop);

      /*
       * Se establecen los datos del nuevo registro de
       * plantacion
       */
      newPlantingRecord = new PlantingRecord();
      newPlantingRecord.setSeedDate(seedDate);
      newPlantingRecord.setHarvestDate(harvestDate);
      newPlantingRecord.setCropIrrigationWaterNeed("n/a");
      newPlantingRecord.setParcel(givenParcel);
      newPlantingRecord.setCrop(givenCrop);
      newPlantingRecord.setStatus(finishedStatus);

      /*
       * Se persiste el nuevo registro de plantacion
       */
      entityManager.getTransaction().begin();
      newPlantingRecord = plantingRecordService.create(newPlantingRecord);
      entityManager.getTransaction().commit();

      numberPlantingRecordsPersisted++;

      System.out.println(newPlantingRecord);

      /*
       * Se modifica la fecha de siembra para que su valor sea una cantidad
       * aleatoria distinta de cero de dias despues de la fecha de cosecha,
       * lo cual, se hace para que la fecha de siembra del siguiente registro
       * de plantacion a crear y persistir sea estrictamente mayor (es decir,
       * este despues) que la fecha de cosecha del registro de plantacion
       * anteriormente creado y persistido.
       * 
       * El dia en el año de la nueva fecha de siembra es el resultado de la
       * suma entre el numero de dia en el año de una fecha de cosecha (la cual,
       * se calcula en base a una fecha de siembra y al ciclo de vida un cultivo)
       * y un numero aleatorio entre 1 y 31.
       */
      randomValue = randomValueGenerator.nextInt(31) + 1;
      dayYearNewSeedDate = harvestDate.get(Calendar.DAY_OF_YEAR) + randomValue;
      seedDate.set(Calendar.DAY_OF_YEAR, dayYearNewSeedDate);
      seedDate.set(Calendar.YEAR, harvestDate.get(Calendar.YEAR));

      System.out.println(
          "Numero de dia en el año para la nueva fecha de siembra (fecha de siembra de un nuevo registro de plantacion a crear y persistir): "
              + dayYearNewSeedDate + " = "
              + harvestDate.get(Calendar.DAY_OF_YEAR) + " (numero de dia en el año de una fecha de cosecha)" + " + "
              + randomValue
              + " (numero aleatorio entre 1 y 31)");
      System.out.println();
    }

    System.out.println("Cantidad de registros de plantacion persistidos: " + numberPlantingRecordsPersisted);
  }

  @AfterClass
  public static void postTest() {
    // Cierra las conexiones
    entityManager.close();
    entityManagerFactory.close();
  }

}
