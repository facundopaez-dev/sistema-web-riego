import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import model.PlantingRecord;
import model.Parcel;
import model.User;
import model.Option;
import stateless.PlantingRecordServiceBean;
import stateless.PlantingRecordStatusServiceBean;
import stateless.ParcelServiceBean;
import stateless.UserServiceBean;
import stateless.CropServiceBean;
import stateless.OptionServiceBean;
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
    private static PlantingRecordStatusServiceBean statusService;
    private static CropServiceBean cropService;
    private static ParcelServiceBean parcelService;
    private static UserServiceBean userService;
    private static OptionServiceBean optionService;

    private static User testUser;
    private static Parcel testParcel;
    private static Option parcelOption;

    private static Collection<User> users;
    private static Collection<Parcel> parcels;
    private static Collection<Option> options;
    private static Collection<PlantingRecord> plantingRecords;

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

        statusService = new PlantingRecordStatusServiceBean();
        statusService.setEntityManager(entityManager);

        cropService = new CropServiceBean();
        cropService.setEntityManager(entityManager);

        parcelService = new ParcelServiceBean();
        parcelService.setEntityManager(entityManager);

        userService = new UserServiceBean();
        userService.setEntityManager(entityManager);

        optionService = new OptionServiceBean();
        optionService.setEntityManager(entityManager);

        parcels = new ArrayList<>();
        users = new ArrayList<>();
        options = new ArrayList<>();
        plantingRecords = new ArrayList<>();

        /*
         * Creacion y persistencia de un usuario de prueba
         */
        testUser = new User();
        testUser.setUsername("FindAllByPeriodTestTestUser");
        testUser.setName("FindAllByPeriodTestTestUserName");
        testUser.setLastName("FindAllByPeriodTestLastName");
        testUser.setEmail("FindAllByPeriodTestTestUser@email");

        entityManager.getTransaction().begin();
        testUser = userService.create(testUser);
        entityManager.getTransaction().commit();

        users.add(testUser);

        /*
         * Creacion y persistencia de una opcion para una
         * parcela de prueba
         */
        entityManager.getTransaction().begin();
        parcelOption = optionService.create();
        entityManager.getTransaction().commit();

        options.add(parcelOption);

        /*
         * Creacion y persistencia de una parcela de prueba
         */
        testParcel = new Parcel(); 
        testParcel.setName("FindAllByPeriodTestTestParcel");
        testParcel.setOption(parcelOption);

        entityManager.getTransaction().begin();
        testParcel = parcelService.create(testParcel);
        entityManager.getTransaction().commit();

        parcels.add(testParcel);
    }

    @Test
    public void testOneFindAllByPeriod() {
        System.out.println("******************************** Prueba uno del metodo findAllByPeriod ********************************");
        printDescriptionMethodToTest();
        System.out.println();
        System.out.println("En esta prueba unitaria se utiliza un registro de plantacion que cumple la primera condicion. Por lo");
        System.out.println("tanto el metodo findAllByPeriod() debe retornar un registro de plantacion.");
        System.out.println();

        /*
         * Creacion de las fechas de siembra y de cosecha
         * para los registros de plantacion de prueba
         */
        Calendar seedDate = Calendar.getInstance();
        seedDate.set(Calendar.YEAR, 2021);
        seedDate.set(Calendar.MONTH, JANUARY);
        seedDate.set(Calendar.DAY_OF_MONTH, 1);

        Calendar harvestDate = Calendar.getInstance();
        harvestDate.set(Calendar.YEAR, 2021);
        harvestDate.set(Calendar.MONTH, MAY);
        harvestDate.set(Calendar.DAY_OF_MONTH, 15);

        /*
         * Creacion y persistencia de registros de plantacion
         * de prueba
         */
        PlantingRecord testPlantingRecord = new PlantingRecord();
        testPlantingRecord.setParcel(testParcel);
        testPlantingRecord.setCrop(cropService.find(1));
        testPlantingRecord.setSeedDate(seedDate);
        testPlantingRecord.setHarvestDate(harvestDate);
        testPlantingRecord.setCropIrrigationWaterNeed("");
        testPlantingRecord.setStatus(statusService.findFinishedStatus());

        entityManager.getTransaction().begin();
        testPlantingRecord = plantingRecordService.create(testPlantingRecord);
        entityManager.getTransaction().commit();

        plantingRecords.add(testPlantingRecord);

        /*
         * Fecha desde y fecha hasta utilizadas para recuperar
         * los registros de plantacion finalizados de una
         * parcela
         */
        Calendar dateFrom = Calendar.getInstance();
        dateFrom.set(Calendar.YEAR, 2020);
        dateFrom.set(Calendar.MONTH, DECEMBER);
        dateFrom.set(Calendar.DAY_OF_MONTH, 1);

        Calendar dateUntil = Calendar.getInstance();
        dateUntil.set(Calendar.YEAR, 2021);
        dateUntil.set(Calendar.MONTH, MARCH);
        dateUntil.set(Calendar.DAY_OF_MONTH, 1);

        System.out.println("Fechas utilizadas para recuperar los registros de plantacion de la parcela que tiene ID = " + testParcel.getId());
        System.out.println("Fecha desde: " + UtilDate.formatDate(dateFrom));
        System.out.println("Fecha hasta: " + UtilDate.formatDate(dateUntil));
        System.out.println();

        System.out.println("Registro de plantacion de prueba");
        System.out.println("Fecha de siembra del registro de plantacion uno: " + UtilDate.formatDate(testPlantingRecord.getSeedDate()));
        System.out.println("Fecha de cosecha del registro de plantacion uno: " + UtilDate.formatDate(testPlantingRecord.getHarvestDate()));
        System.out.println("ID de la parcela del registro de plantacion uno: " + testPlantingRecord.getParcel().getId());
        System.out.println();

        /*
         * Seccion de prueba
         */
        System.out.println("* Seccion de prueba *");
        List<PlantingRecord> listPlantingRecords = plantingRecordService.findAllByPeriod(testParcel.getId(), dateFrom, dateUntil);

        int expectedResult = 1;
        int result = listPlantingRecords.size();

        System.out.println("Resultado esperado: " + expectedResult);
        System.out.println("Cantidad de registros de plantacion finalizados recuperados en el periodo ["
                + UtilDate.formatDate(dateFrom) + ", " + UtilDate.formatDate(dateUntil) + "]: "
                + listPlantingRecords.size());
        System.out.println();

        assertTrue(expectedResult == result);

        System.out.println("- Prueba pasada satisfactoriamente");
        System.out.println();
    }

    @Test
    public void testTwoFindAllByPeriod() {
        System.out.println("******************************** Prueba dos del metodo findAllByPeriod ********************************");
        printDescriptionMethodToTest();
        System.out.println();
        System.out.println("En esta prueba unitaria se utiliza un registro de plantacion que cumple la segunda condicion. Por lo");
        System.out.println("tanto el metodo findAllByPeriod() debe retornar un registro de plantacion.");
        System.out.println();

        /*
         * Creacion de las fechas de siembra y de cosecha
         * para los registros de plantacion de prueba
         */
        Calendar seedDate = Calendar.getInstance();
        seedDate.set(Calendar.YEAR, 2022);
        seedDate.set(Calendar.MONTH, JANUARY);
        seedDate.set(Calendar.DAY_OF_MONTH, 1);

        Calendar harvestDate = Calendar.getInstance();
        harvestDate.set(Calendar.YEAR, 2022);
        harvestDate.set(Calendar.MONTH, FEBRUARY);
        harvestDate.set(Calendar.DAY_OF_MONTH, 15);

        /*
         * Creacion y persistencia de registros de plantacion
         * de prueba
         */
        PlantingRecord testPlantingRecord = new PlantingRecord();
        testPlantingRecord.setParcel(testParcel);
        testPlantingRecord.setCrop(cropService.find(1));
        testPlantingRecord.setSeedDate(seedDate);
        testPlantingRecord.setHarvestDate(harvestDate);
        testPlantingRecord.setCropIrrigationWaterNeed("");
        testPlantingRecord.setStatus(statusService.findFinishedStatus());

        entityManager.getTransaction().begin();
        testPlantingRecord = plantingRecordService.create(testPlantingRecord);
        entityManager.getTransaction().commit();

        plantingRecords.add(testPlantingRecord);

        /*
         * Fecha desde y fecha hasta utilizadas para recuperar
         * los registros de plantacion finalizados de una
         * parcela
         */
        Calendar dateFrom = Calendar.getInstance();
        dateFrom.set(Calendar.YEAR, 2021);
        dateFrom.set(Calendar.MONTH, DECEMBER);
        dateFrom.set(Calendar.DAY_OF_MONTH, 1);

        Calendar dateUntil = Calendar.getInstance();
        dateUntil.set(Calendar.YEAR, 2022);
        dateUntil.set(Calendar.MONTH, MARCH);
        dateUntil.set(Calendar.DAY_OF_MONTH, 1);

        System.out.println("Fechas utilizadas para recuperar los registros de plantacion de la parcela que tiene ID = " + testParcel.getId());
        System.out.println("Fecha desde: " + UtilDate.formatDate(dateFrom));
        System.out.println("Fecha hasta: " + UtilDate.formatDate(dateUntil));
        System.out.println();

        System.out.println("Registro de plantacion de prueba");
        System.out.println("Fecha de siembra del registro de plantacion de prueba: " + UtilDate.formatDate(testPlantingRecord.getSeedDate()));
        System.out.println("Fecha de cosecha del registro de plantacion de prueba: " + UtilDate.formatDate(testPlantingRecord.getHarvestDate()));
        System.out.println("ID de la parcela del registro de plantacion de prueba: " + testPlantingRecord.getParcel().getId());
        System.out.println();

        /*
         * Seccion de prueba
         */
        System.out.println("* Seccion de prueba *");
        List<PlantingRecord> listPlantingRecords = plantingRecordService.findAllByPeriod(testParcel.getId(), dateFrom, dateUntil);

        int expectedResult = 1;
        int result = listPlantingRecords.size();

        System.out.println("Resultado esperado: " + expectedResult);
        System.out.println("Cantidad de registros de plantacion finalizados recuperados en el periodo ["
                + UtilDate.formatDate(dateFrom) + ", " + UtilDate.formatDate(dateUntil) + "]: "
                + listPlantingRecords.size());
        System.out.println();

        assertTrue(expectedResult == result);

        System.out.println("- Prueba pasada satisfactoriamente");
        System.out.println();
    }

    @Test
    public void testThreeFindAllByPeriod() {
        System.out.println("******************************** Prueba tres del metodo findAllByPeriod ********************************");
        printDescriptionMethodToTest();
        System.out.println();
        System.out.println("En esta prueba unitaria se utiliza un registro de plantacion que cumple la tercera condicion. Por lo");
        System.out.println("tanto el metodo findAllByPeriod() debe retornar un registro de plantacion.");
        System.out.println();

        /*
         * Creacion de las fechas de siembra y de cosecha
         * para los registros de plantacion de prueba
         */
        Calendar seedDate = Calendar.getInstance();
        seedDate.set(Calendar.YEAR, 2023);
        seedDate.set(Calendar.MONTH, JANUARY);
        seedDate.set(Calendar.DAY_OF_MONTH, 1);

        Calendar harvestDate = Calendar.getInstance();
        harvestDate.set(Calendar.YEAR, 2023);
        harvestDate.set(Calendar.MONTH, APRIL);
        harvestDate.set(Calendar.DAY_OF_MONTH, 15);

        /*
         * Creacion y persistencia de un registro de plantacion
         * de prueba
         */
        PlantingRecord testPlantingRecord = new PlantingRecord();
        testPlantingRecord.setParcel(testParcel);
        testPlantingRecord.setCrop(cropService.find(1));
        testPlantingRecord.setSeedDate(seedDate);
        testPlantingRecord.setHarvestDate(harvestDate);
        testPlantingRecord.setCropIrrigationWaterNeed("");
        testPlantingRecord.setStatus(statusService.findFinishedStatus());

        entityManager.getTransaction().begin();
        testPlantingRecord = plantingRecordService.create(testPlantingRecord);
        entityManager.getTransaction().commit();

        plantingRecords.add(testPlantingRecord);

        /*
         * Fecha desde y fecha hasta utilizadas para recuperar
         * los registros de plantacion finalizados de una
         * parcela
         */
        Calendar dateFrom = Calendar.getInstance();
        dateFrom.set(Calendar.YEAR, 2023);
        dateFrom.set(Calendar.MONTH, FEBRUARY);
        dateFrom.set(Calendar.DAY_OF_MONTH, 1);

        Calendar dateUntil = Calendar.getInstance();
        dateUntil.set(Calendar.YEAR, 2023);
        dateUntil.set(Calendar.MONTH, MAY);
        dateUntil.set(Calendar.DAY_OF_MONTH, 1);

        System.out.println("Fechas utilizadas para recuperar registros de plantacion de la parcela que tiene ID = " + testParcel.getId());
        System.out.println("Fecha desde: " + UtilDate.formatDate(dateFrom));
        System.out.println("Fecha hasta: " + UtilDate.formatDate(dateUntil));
        System.out.println();

        System.out.println("Datos del registro de plantacion de prueba");
        System.out.println("Fecha de siembra del registro de plantacion de prueba: " + UtilDate.formatDate(testPlantingRecord.getSeedDate()));
        System.out.println("Fecha de cosecha del registro de plantacion de prueba: " + UtilDate.formatDate(testPlantingRecord.getHarvestDate()));
        System.out.println("ID de la parcela del registro de plantacion de prueba: " + testPlantingRecord.getParcel().getId());
        System.out.println();

        /*
         * Seccion de prueba
         */
        System.out.println("* Seccion de prueba *");
        List<PlantingRecord> listPlantingRecords = plantingRecordService.findAllByPeriod(testParcel.getId(), dateFrom, dateUntil);

        int expectedResult = 1;
        int result = listPlantingRecords.size();

        System.out.println("Resultado esperado: " + expectedResult);
        System.out.println("Cantidad de registros de plantacion finalizados recuperados en el periodo ["
                + UtilDate.formatDate(dateFrom) + ", " + UtilDate.formatDate(dateUntil) + "]: "
                + listPlantingRecords.size());
        System.out.println();

        assertTrue(expectedResult == result);

        System.out.println("- Prueba pasada satisfactoriamente");
        System.out.println();
    }

    @Test
    public void testFourFindAllByPeriod() {
        System.out.println("******************************** Prueba cuatro del metodo findAllByPeriod ********************************");
        printDescriptionMethodToTest();
        System.out.println();
        System.out.println("En esta prueba unitaria se utiliza un registro de plantacion que NO cumple con ninguna de las condiciones.");
        System.out.println("Por lo tanto el metodo findAllByPeriod() NO debe retornar ningun registro de plantacion.");
        System.out.println();

        /*
         * Creacion de las fechas de siembra y de cosecha
         * para los registros de plantacion de prueba
         */
        Calendar seedDate = Calendar.getInstance();
        seedDate.set(Calendar.YEAR, 2024);
        seedDate.set(Calendar.MONTH, JANUARY);
        seedDate.set(Calendar.DAY_OF_MONTH, 1);

        Calendar harvestDate = Calendar.getInstance();
        harvestDate.set(Calendar.YEAR, 2024);
        harvestDate.set(Calendar.MONTH, APRIL);
        harvestDate.set(Calendar.DAY_OF_MONTH, 15);

        /*
         * Creacion y persistencia de un registro de plantacion
         * de prueba
         */
        PlantingRecord testPlantingRecord = new PlantingRecord();
        testPlantingRecord.setParcel(testParcel);
        testPlantingRecord.setCrop(cropService.find(1));
        testPlantingRecord.setSeedDate(seedDate);
        testPlantingRecord.setHarvestDate(harvestDate);
        testPlantingRecord.setCropIrrigationWaterNeed("");
        testPlantingRecord.setStatus(statusService.findFinishedStatus());

        entityManager.getTransaction().begin();
        testPlantingRecord = plantingRecordService.create(testPlantingRecord);
        entityManager.getTransaction().commit();

        plantingRecords.add(testPlantingRecord);

        /*
         * Fecha desde y fecha hasta utilizadas para recuperar
         * los registros de plantacion finalizados de una
         * parcela
         */
        Calendar dateFrom = Calendar.getInstance();
        dateFrom.set(Calendar.YEAR, 2021);
        dateFrom.set(Calendar.MONTH, FEBRUARY);
        dateFrom.set(Calendar.DAY_OF_MONTH, 1);

        Calendar dateUntil = Calendar.getInstance();
        dateUntil.set(Calendar.YEAR, 2021);
        dateUntil.set(Calendar.MONTH, MAY);
        dateUntil.set(Calendar.DAY_OF_MONTH, 1);

        System.out.println("Fechas utilizadas para recuperar registros de plantacion de la parcela que tiene ID = " + testParcel.getId());
        System.out.println("Fecha desde: " + UtilDate.formatDate(dateFrom));
        System.out.println("Fecha hasta: " + UtilDate.formatDate(dateUntil));
        System.out.println();

        System.out.println("Datos del registro de plantacion de prueba");
        System.out.println("Fecha de siembra del registro de plantacion de prueba: " + UtilDate.formatDate(testPlantingRecord.getSeedDate()));
        System.out.println("Fecha de cosecha del registro de plantacion de prueba: " + UtilDate.formatDate(testPlantingRecord.getHarvestDate()));
        System.out.println("ID de la parcela del registro de plantacion de prueba: " + testPlantingRecord.getParcel().getId());
        System.out.println();

        /*
         * Seccion de prueba
         */
        System.out.println("* Seccion de prueba *");
        List<PlantingRecord> listPlantingRecords = plantingRecordService.findAllByPeriod(testParcel.getId(), dateFrom, dateUntil);

        int expectedResult = 0;
        int result = listPlantingRecords.size();

        System.out.println("Resultado esperado: " + expectedResult);
        System.out.println("Cantidad de registros de plantacion finalizados recuperados en el periodo ["
                + UtilDate.formatDate(dateFrom) + ", " + UtilDate.formatDate(dateUntil) + "]: "
                + listPlantingRecords.size());
        System.out.println();

        assertTrue(expectedResult == result);

        System.out.println("- Prueba pasada satisfactoriamente");
        System.out.println();
    }

    @AfterClass
    public static void postTest() {
        entityManager.getTransaction().begin();

        /*
         * Se eliminan de la base de datos subyacente los datos persistidos
         * durante la ejecucion de las pruebas unitarias para que la misma
         * quede en su estado original, es decir, para dejarla en el estado
         * en el que estaba antes de que se persistieran dichos datos
         */
        for (PlantingRecord currentPlantingRecord : plantingRecords) {
            plantingRecordService.remove(currentPlantingRecord.getId());
        }

        for (Option currentOption : options) {
            optionService.remove(currentOption.getId());
        }

        for (Parcel currentParcel : parcels) {
            parcelService.remove(currentParcel.getId());
        }

        for (User currentUser : users) {
            userService.remove(currentUser.getId());
        }

        entityManager.getTransaction().commit();

        // Cierra las conexiones
        entityManager.close();
        entityManagerFactory.close();
    }

    /**
     * Imprime la descripcion del metodo a probar
     */
    private void printDescriptionMethodToTest() {
        System.out.println("El metodo findAllByPeriod() de la clase PlantingRecordService recupera de la base de datos subyacente los");
        System.out.println("registros de plantacion finalizados de una parcela que cumplen con una de las siguientes condiciones:");
        System.out.println("- la fecha de siembra es mayor o igual a una fecha desde y menor o igual a una fecha hasta, y la fecha");
        System.out.println("de cosecha es estrictamente mayor a una fecha hasta.");
        System.out.println("- la fecha de siembra es mayor o igual a una fecha desde y la fecha de cosecha es menor o igual a una");
        System.out.println("fecha hasta.");
        System.out.println("- la fecha de cosecha es mayor o igual a una fecha desde y menor o igual a una fecha hasta, y la fecha");
        System.out.println("de siembra es estrictamente menor a una fecha desde.");
    }

}
