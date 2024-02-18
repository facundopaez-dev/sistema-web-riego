import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import stateless.CropServiceBean;
import stateless.OptionServiceBean;
import stateless.ParcelServiceBean;
import stateless.PlantingRecordServiceBean;
import stateless.PlantingRecordStatusServiceBean;
import stateless.UserServiceBean;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import model.User;
import model.Parcel;
import model.Option;
import model.PlantingRecord;
import util.UtilDate;

public class DaysWithCropsTest {

    private static EntityManager entityManager;
    private static EntityManagerFactory entityManagerFactory;
    private static PlantingRecordServiceBean plantingRecordService;
    private static CropServiceBean cropService;
    private static ParcelServiceBean parcelService;
    private static UserServiceBean userService;
    private static OptionServiceBean optionService;
    private static PlantingRecordStatusServiceBean statusService;

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
        testUser.setUsername("DaysWithCropsTest");
        testUser.setName("DaysWithCropsTestUserName");
        testUser.setLastName("DaysWithCropsTestLastName");
        testUser.setEmail("DaysWithCropsTest@email");

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
        testParcel.setName("DaysWithCropsTestParcel");
        testParcel.setOption(parcelOption);

        entityManager.getTransaction().begin();
        testParcel = parcelService.create(testParcel);
        entityManager.getTransaction().commit();

        parcels.add(testParcel);
    }

    @Test
    public void testOne() {
        System.out.println("******************************* Prueba uno del metodo calculateDaysWithCrops *******************************");
        printDescriptionMethodToTest();
        System.out.println();
        System.out.println("# Descripcion de la prueba unitaria");
        System.out.println("En esta prueba unitaria para demostrar el correcto funcionamiento del metodo calculateDaysWithCrops() se utiliza");
        System.out.println("un registro de plantacion finalizado que tiene una fecha de siembra mayor o igual a una fecha desde y menor");
        System.out.println("o igual a una fecha hasta, y una fecha de cosecha estrictamente mayor a una fecha hasta. Por lo tanto, el metodo");
        System.out.println("calculateDaysWithCrops() debe retornar la cantidad de dias que hay entre la fecha de siembra y la fecha hasta");
        System.out.println("como la cantidad de dias en los que una parcela tuvo un cultivo plantado.");
        System.out.println();
        System.out.println("Situacion representada graficamente:");
        System.out.println();
        System.out.println("              fecha siembra                 fecha cosecha");
        System.out.println("<------------------[-----------------------------]------>");
        System.out.println("   fecha desde                  fecha hasta");
        System.out.println("<-------[----------------------------]------------------>");
        System.out.println();

        /*
         * Fecha desde y fecha hasta para la prueba
         * unitaria
         */
        Calendar dateFrom = Calendar.getInstance();
        dateFrom.set(Calendar.YEAR, 2022);
        dateFrom.set(Calendar.MONTH, APRIL);
        dateFrom.set(Calendar.DAY_OF_MONTH, 1);

        Calendar dateUntil = Calendar.getInstance();
        dateUntil.set(Calendar.YEAR, 2022);
        dateUntil.set(Calendar.MONTH, APRIL);
        dateUntil.set(Calendar.DAY_OF_MONTH, 30);

        /*
         * Creacion y persistencia de un registro de
         * plantacion de prueba
         */
        Calendar seedDate = Calendar.getInstance();
        seedDate.set(Calendar.YEAR, 2022);
        seedDate.set(Calendar.MONTH, APRIL);
        seedDate.set(Calendar.DAY_OF_MONTH, 20);

        Calendar harvestDate = Calendar.getInstance();
        harvestDate.set(Calendar.YEAR, 2022);
        harvestDate.set(Calendar.MONTH, MAY);
        harvestDate.set(Calendar.DAY_OF_MONTH, 10);

        PlantingRecord testPlantingRecordOne = new PlantingRecord();
        testPlantingRecordOne.setSeedDate(seedDate);
        testPlantingRecordOne.setHarvestDate(harvestDate);
        testPlantingRecordOne.setParcel(testParcel);
        testPlantingRecordOne.setCrop(cropService.find(1));
        testPlantingRecordOne.setStatus(statusService.findFinishedStatus());
        testPlantingRecordOne.setCropIrrigationWaterNeed("-");

        entityManager.getTransaction().begin();
        testPlantingRecordOne = plantingRecordService.create(testPlantingRecordOne);
        entityManager.getTransaction().commit();

        plantingRecords.add(testPlantingRecordOne);

        /*
         * Seccion de prueba
         */
        System.out.println("# Seccion de prueba");
        System.out.println("Periodo dentro del cual se calculara la cantidad de dias en los que la parcela con ID = " + testParcel.getId() + " tuvo un cultivo");
        System.out.println("plantado:");
        System.out.println("Fecha desde: " + UtilDate.formatDate(dateFrom));
        System.out.println("Fecha hasta: " + UtilDate.formatDate(dateUntil));
        System.out.println();

        System.out.println("Datos del registro de plantacion de prueba:");
        System.out.println("Fecha de siembra: " + UtilDate.formatDate(testPlantingRecordOne.getSeedDate()));
        System.out.println("Fecha de cosecha: " + UtilDate.formatDate(testPlantingRecordOne.getHarvestDate()));
        System.out.println("ID de la parcela a la que pertenece el registro de plantacion: " + testPlantingRecordOne.getParcel().getId());
        System.out.println();

        int expectedResult = 11;
        int result = plantingRecordService.calculateDaysWithCrops(testParcel.getId(), dateFrom, dateUntil);

        System.out.println("Resultado esperado: " + expectedResult);
        System.out.println("Resultado devuelto por el metodo calculateDaysWithCrops(): " + result);
        System.out.println();

        assertTrue(expectedResult == result);

        System.out.println("- Prueba pasada satisfactoriamente");
        System.out.println();
    }

    @Test
    public void testTwo() {
        System.out.println("******************************* Prueba dos del metodo calculateDaysWithCrops *******************************");
        printDescriptionMethodToTest();
        System.out.println();
        System.out.println("# Descripcion de la prueba unitaria");
        System.out.println("En esta prueba unitaria para demostrar el correcto funcionamiento del metodo calculateDaysWithCrops() se utiliza");
        System.out.println("un registro de plantacion finalizado que tiene una fecha de siembra mayor o igual a una fecha desde y una fecha");
        System.out.println("de cosecha menor o igual a una fecha hasta. Por lo tanto, el metodo calculateDaysWithCrops() debe retornar la");
        System.out.println("cantidad de dias que hay entre la fecha de siembra y la fecha de cosecha del registro de plantacion como la");
        System.out.println("cantidad de dias en los que una parcela tuvo un cultivo plantado.");
        System.out.println();
        System.out.println("Situacion representada graficamente:");
        System.out.println();
        System.out.println("        fecha siembra                 fecha cosecha");
        System.out.println("<------------[-----------------------------]------------>");
        System.out.println("    fecha desde                           fecha hasta");
        System.out.println("<-----[-------------------------------------------]----->");
        System.out.println();

        /*
         * Fecha desde y fecha hasta para la prueba
         * unitaria
         */
        Calendar dateFrom = Calendar.getInstance();
        dateFrom.set(Calendar.YEAR, 2023);
        dateFrom.set(Calendar.MONTH, JANUARY);
        dateFrom.set(Calendar.DAY_OF_MONTH, 15);

        Calendar dateUntil = Calendar.getInstance();
        dateUntil.set(Calendar.YEAR, 2024);
        dateUntil.set(Calendar.MONTH, FEBRUARY);
        dateUntil.set(Calendar.DAY_OF_MONTH, 1);

        /*
         * Creacion y persistencia de un registro de
         * plantacion de prueba
         */
        Calendar seedDate = Calendar.getInstance();
        seedDate.set(Calendar.YEAR, 2024);
        seedDate.set(Calendar.MONTH, JANUARY);
        seedDate.set(Calendar.DAY_OF_MONTH, 1);

        Calendar harvestDate = Calendar.getInstance();
        harvestDate.set(Calendar.YEAR, 2024);
        harvestDate.set(Calendar.MONTH, JANUARY);
        harvestDate.set(Calendar.DAY_OF_MONTH, 31);

        PlantingRecord testPlantingRecordOne = new PlantingRecord();
        testPlantingRecordOne.setSeedDate(seedDate);
        testPlantingRecordOne.setHarvestDate(harvestDate);
        testPlantingRecordOne.setParcel(testParcel);
        testPlantingRecordOne.setCrop(cropService.find(1));
        testPlantingRecordOne.setStatus(statusService.findFinishedStatus());
        testPlantingRecordOne.setCropIrrigationWaterNeed("-");

        entityManager.getTransaction().begin();
        testPlantingRecordOne = plantingRecordService.create(testPlantingRecordOne);
        entityManager.getTransaction().commit();

        plantingRecords.add(testPlantingRecordOne);

        /*
         * Seccion de prueba
         */
        System.out.println("# Seccion de prueba");
        System.out.println("Periodo dentro del cual se calculara la cantidad de dias en los que la parcela con ID = " + testParcel.getId() + " tuvo un cultivo");
        System.out.println("plantado:");
        System.out.println("Fecha desde: " + UtilDate.formatDate(dateFrom));
        System.out.println("Fecha hasta: " + UtilDate.formatDate(dateUntil));
        System.out.println();

        System.out.println("Datos del registro de plantacion de prueba:");
        System.out.println("Fecha de siembra: " + UtilDate.formatDate(testPlantingRecordOne.getSeedDate()));
        System.out.println("Fecha de cosecha: " + UtilDate.formatDate(testPlantingRecordOne.getHarvestDate()));
        System.out.println("ID de la parcela a la que pertenece el registro de plantacion: " + testPlantingRecordOne.getParcel().getId());
        System.out.println();

        int expectedResult = 31;
        int result = plantingRecordService.calculateDaysWithCrops(testParcel.getId(), dateFrom, dateUntil);

        System.out.println("Resultado esperado: " + expectedResult);
        System.out.println("Resultado devuelto por el metodo calculateDaysWithCrops(): " + result);
        System.out.println();

        assertTrue(expectedResult == result);

        System.out.println("- Prueba pasada satisfactoriamente");
        System.out.println();
    }

    @Test
    public void testThree() {
        System.out.println("******************************* Prueba tres del metodo calculateDaysWithCrops *******************************");
        printDescriptionMethodToTest();
        System.out.println();
        System.out.println("# Descripcion de la prueba unitaria");
        System.out.println("En esta prueba unitaria para demostrar el correcto funcionamiento del metodo calculateDaysWithCrops() se utiliza");
        System.out.println("un registro de plantacion finalizado que tiene una fecha de cosecha mayor o igual a una fecha desde y menor");
        System.out.println("o igual a una fecha hasta, y una fecha de siembra estrictamente menor a una fecha desde. Por lo tanto, el metodo");
        System.out.println("calculateDaysWithCrops() debe retornar la cantidad de dias que hay entre la fecha desde y la fecha de cosecha");
        System.out.println("como la cantidad de dias en los que una parcela tuvo un cultivo plantado.");
        System.out.println();
        System.out.println("Situacion representada graficamente:");
        System.out.println();
        System.out.println("  fecha siembra             fecha cosecha");
        System.out.println("<------[-------------------------]----------------------->");
        System.out.println("            fecha desde                  fecha hasta");
        System.out.println("<----------------[----------------------------]---------->");
        System.out.println();

        /*
         * Fecha desde y fecha hasta para la prueba
         * unitaria
         */
        Calendar dateFrom = Calendar.getInstance();
        dateFrom.set(Calendar.YEAR, 2025);
        dateFrom.set(Calendar.MONTH, JULY);
        dateFrom.set(Calendar.DAY_OF_MONTH, 18);

        Calendar dateUntil = Calendar.getInstance();
        dateUntil.set(Calendar.YEAR, 2025);
        dateUntil.set(Calendar.MONTH, AUGUST);
        dateUntil.set(Calendar.DAY_OF_MONTH, 2);

        /*
         * Creacion y persistencia de un registro de
         * plantacion de prueba
         */
        Calendar seedDate = Calendar.getInstance();
        seedDate.set(Calendar.YEAR, 2025);
        seedDate.set(Calendar.MONTH, JUNE);
        seedDate.set(Calendar.DAY_OF_MONTH, 15);

        Calendar harvestDate = Calendar.getInstance();
        harvestDate.set(Calendar.YEAR, 2025);
        harvestDate.set(Calendar.MONTH, JULY);
        harvestDate.set(Calendar.DAY_OF_MONTH, 30);

        PlantingRecord testPlantingRecordOne = new PlantingRecord();
        testPlantingRecordOne.setSeedDate(seedDate);
        testPlantingRecordOne.setHarvestDate(harvestDate);
        testPlantingRecordOne.setParcel(testParcel);
        testPlantingRecordOne.setCrop(cropService.find(1));
        testPlantingRecordOne.setStatus(statusService.findFinishedStatus());
        testPlantingRecordOne.setCropIrrigationWaterNeed("-");

        entityManager.getTransaction().begin();
        testPlantingRecordOne = plantingRecordService.create(testPlantingRecordOne);
        entityManager.getTransaction().commit();

        plantingRecords.add(testPlantingRecordOne);

        /*
         * Seccion de prueba
         */
        System.out.println("# Seccion de prueba");
        System.out.println("Periodo dentro del cual se calculara la cantidad de dias en los que la parcela con ID = " + testParcel.getId() + " tuvo un cultivo");
        System.out.println("plantado:");
        System.out.println("Fecha desde: " + UtilDate.formatDate(dateFrom));
        System.out.println("Fecha hasta: " + UtilDate.formatDate(dateUntil));
        System.out.println();

        System.out.println("Datos del registro de plantacion de prueba:");
        System.out.println("Fecha de siembra: " + UtilDate.formatDate(testPlantingRecordOne.getSeedDate()));
        System.out.println("Fecha de cosecha: " + UtilDate.formatDate(testPlantingRecordOne.getHarvestDate()));
        System.out.println("ID de la parcela a la que pertenece el registro de plantacion: " + testPlantingRecordOne.getParcel().getId());
        System.out.println();

        int expectedResult = 13;
        int result = plantingRecordService.calculateDaysWithCrops(testParcel.getId(), dateFrom, dateUntil);

        System.out.println("Resultado esperado: " + expectedResult);
        System.out.println("Resultado devuelto por el metodo calculateDaysWithCrops(): " + result);
        System.out.println();

        assertTrue(expectedResult == result);

        System.out.println("- Prueba pasada satisfactoriamente");
        System.out.println();
    }

    @Test
    public void testFour() {
        System.out.println("******************************* Prueba cuatro del metodo calculateDaysWithCrops *******************************");
        printDescriptionMethodToTest();
        System.out.println();
        System.out.println("# Descripcion de la prueba unitaria");
        System.out.println("En esta prueba unitaria para demostrar el correcto funcionamiento del metodo calculateDaysWithCrops() se utiliza");
        System.out.println("un registro de plantacion finalizado que tiene una fecha de siembra mayor o igual a una fecha desde y menor");
        System.out.println("o igual a una fecha hasta, y una fecha de cosecha estrictamente mayor a una fecha hasta. Por lo tanto, el metodo");
        System.out.println("calculateDaysWithCrops() debe retornar la cantidad de dias que hay entre la fecha de siembra y la fecha hasta");
        System.out.println("como la cantidad de dias en los que una parcela tuvo un cultivo plantado.");
        System.out.println();
        System.out.println("Situacion representada graficamente:");
        System.out.println();
        System.out.println("              fecha siembra                 fecha cosecha");
        System.out.println("<------------------[-----------------------------]------>");
        System.out.println("   fecha desde                  fecha hasta");
        System.out.println("<-------[----------------------------]------------------>");
        System.out.println();
        System.out.println("Nota: Esta prueba unitaria es el caso de la prueba unitaria uno, pero con años diferentes.");
        System.out.println();

        /*
         * Fecha desde y fecha hasta para la prueba
         * unitaria
         */
        Calendar dateFrom = Calendar.getInstance();
        dateFrom.set(Calendar.YEAR, 2019);
        dateFrom.set(Calendar.MONTH, DECEMBER);
        dateFrom.set(Calendar.DAY_OF_MONTH, 1);

        Calendar dateUntil = Calendar.getInstance();
        dateUntil.set(Calendar.YEAR, 2020);
        dateUntil.set(Calendar.MONTH, JANUARY);
        dateUntil.set(Calendar.DAY_OF_MONTH, 15);

        /*
         * Creacion y persistencia de un registro de
         * plantacion de prueba
         */
        Calendar seedDate = Calendar.getInstance();
        seedDate.set(Calendar.YEAR, 2019);
        seedDate.set(Calendar.MONTH, DECEMBER);
        seedDate.set(Calendar.DAY_OF_MONTH, 15);

        Calendar harvestDate = Calendar.getInstance();
        harvestDate.set(Calendar.YEAR, 2020);
        harvestDate.set(Calendar.MONTH, JANUARY);
        harvestDate.set(Calendar.DAY_OF_MONTH, 30);

        PlantingRecord testPlantingRecordOne = new PlantingRecord();
        testPlantingRecordOne.setSeedDate(seedDate);
        testPlantingRecordOne.setHarvestDate(harvestDate);
        testPlantingRecordOne.setParcel(testParcel);
        testPlantingRecordOne.setCrop(cropService.find(1));
        testPlantingRecordOne.setStatus(statusService.findFinishedStatus());
        testPlantingRecordOne.setCropIrrigationWaterNeed("-");

        entityManager.getTransaction().begin();
        testPlantingRecordOne = plantingRecordService.create(testPlantingRecordOne);
        entityManager.getTransaction().commit();

        plantingRecords.add(testPlantingRecordOne);

        /*
         * Seccion de prueba
         */
        System.out.println("# Seccion de prueba");
        System.out.println("Periodo dentro del cual se calculara la cantidad de dias en los que la parcela con ID = " + testParcel.getId() + " tuvo un cultivo");
        System.out.println("plantado:");
        System.out.println("Fecha desde: " + UtilDate.formatDate(dateFrom));
        System.out.println("Fecha hasta: " + UtilDate.formatDate(dateUntil));
        System.out.println();

        System.out.println("Datos del registro de plantacion de prueba:");
        System.out.println("Fecha de siembra: " + UtilDate.formatDate(testPlantingRecordOne.getSeedDate()));
        System.out.println("Fecha de cosecha: " + UtilDate.formatDate(testPlantingRecordOne.getHarvestDate()));
        System.out.println("ID de la parcela a la que pertenece el registro de plantacion: " + testPlantingRecordOne.getParcel().getId());
        System.out.println();

        int expectedResult = 32;
        int result = plantingRecordService.calculateDaysWithCrops(testParcel.getId(), dateFrom, dateUntil);

        System.out.println("Resultado esperado: " + expectedResult);
        System.out.println("Resultado devuelto por el metodo calculateDaysWithCrops(): " + result);
        System.out.println();

        assertTrue(expectedResult == result);

        System.out.println("- Prueba pasada satisfactoriamente");
        System.out.println();
    }

    @Test
    public void testFive() {
        System.out.println("******************************* Prueba cinco del metodo calculateDaysWithCrops *******************************");
        printDescriptionMethodToTest();
        System.out.println();
        System.out.println("# Descripcion de la prueba unitaria");
        System.out.println("En esta prueba unitaria para demostrar el correcto funcionamiento del metodo calculateDaysWithCrops() se utiliza");
        System.out.println("un registro de plantacion finalizado que tiene una fecha de siembra mayor o igual a una fecha desde y una fecha");
        System.out.println("de cosecha menor o igual a una fecha hasta. Por lo tanto, el metodo calculateDaysWithCrops() debe retornar la");
        System.out.println("cantidad de dias que hay entre la fecha de siembra y la fecha de cosecha del registro de plantacion como la");
        System.out.println("cantidad de dias en los que una parcela tuvo un cultivo plantado.");
        System.out.println();
        System.out.println("Situacion representada graficamente:");
        System.out.println();
        System.out.println("        fecha siembra                 fecha cosecha");
        System.out.println("<------------[-----------------------------]------------>");
        System.out.println("    fecha desde                           fecha hasta");
        System.out.println("<-----[-------------------------------------------]----->");
        System.out.println();
        System.out.println("Nota: Esta prueba unitaria es el caso de la prueba unitaria dos, pero con años diferentes.");
        System.out.println();

        /*
         * Fecha desde y fecha hasta para la prueba
         * unitaria
         */
        Calendar dateFrom = Calendar.getInstance();
        dateFrom.set(Calendar.YEAR, 2026);
        dateFrom.set(Calendar.MONTH, DECEMBER);
        dateFrom.set(Calendar.DAY_OF_MONTH, 1);

        Calendar dateUntil = Calendar.getInstance();
        dateUntil.set(Calendar.YEAR, 2027);
        dateUntil.set(Calendar.MONTH, JANUARY);
        dateUntil.set(Calendar.DAY_OF_MONTH, 15);

        /*
         * Creacion y persistencia de un registro de
         * plantacion de prueba
         */
        Calendar seedDate = Calendar.getInstance();
        seedDate.set(Calendar.YEAR, 2026);
        seedDate.set(Calendar.MONTH, DECEMBER);
        seedDate.set(Calendar.DAY_OF_MONTH, 12);

        Calendar harvestDate = Calendar.getInstance();
        harvestDate.set(Calendar.YEAR, 2027);
        harvestDate.set(Calendar.MONTH, JANUARY);
        harvestDate.set(Calendar.DAY_OF_MONTH, 5);

        PlantingRecord testPlantingRecordOne = new PlantingRecord();
        testPlantingRecordOne.setSeedDate(seedDate);
        testPlantingRecordOne.setHarvestDate(harvestDate);
        testPlantingRecordOne.setParcel(testParcel);
        testPlantingRecordOne.setCrop(cropService.find(1));
        testPlantingRecordOne.setStatus(statusService.findFinishedStatus());
        testPlantingRecordOne.setCropIrrigationWaterNeed("-");

        entityManager.getTransaction().begin();
        testPlantingRecordOne = plantingRecordService.create(testPlantingRecordOne);
        entityManager.getTransaction().commit();

        plantingRecords.add(testPlantingRecordOne);

        /*
         * Seccion de prueba
         */
        System.out.println("# Seccion de prueba");
        System.out.println("Periodo dentro del cual se calculara la cantidad de dias en los que la parcela con ID = " + testParcel.getId() + " tuvo un cultivo");
        System.out.println("plantado:");
        System.out.println("Fecha desde: " + UtilDate.formatDate(dateFrom));
        System.out.println("Fecha hasta: " + UtilDate.formatDate(dateUntil));
        System.out.println();

        System.out.println("Datos del registro de plantacion de prueba:");
        System.out.println("Fecha de siembra: " + UtilDate.formatDate(testPlantingRecordOne.getSeedDate()));
        System.out.println("Fecha de cosecha: " + UtilDate.formatDate(testPlantingRecordOne.getHarvestDate()));
        System.out.println("ID de la parcela a la que pertenece el registro de plantacion: " + testPlantingRecordOne.getParcel().getId());
        System.out.println();

        int expectedResult = 25;
        int result = plantingRecordService.calculateDaysWithCrops(testParcel.getId(), dateFrom, dateUntil);

        System.out.println("Resultado esperado: " + expectedResult);
        System.out.println("Resultado devuelto por el metodo calculateDaysWithCrops(): " + result);
        System.out.println();

        assertTrue(expectedResult == result);

        System.out.println("- Prueba pasada satisfactoriamente");
        System.out.println();
    }

    @Test
    public void testSix() {
        System.out.println("******************************* Prueba seis del metodo calculateDaysWithCrops *******************************");
        printDescriptionMethodToTest();
        System.out.println();
        System.out.println("# Descripcion de la prueba unitaria");
        System.out.println("En esta prueba unitaria para demostrar el correcto funcionamiento del metodo calculateDaysWithCrops() se utiliza");
        System.out.println("un registro de plantacion finalizado que tiene una fecha de cosecha mayor o igual a una fecha desde y menor");
        System.out.println("o igual a una fecha hasta, y una fecha de siembra estrictamente menor a una fecha desde. Por lo tanto, el metodo");
        System.out.println("calculateDaysWithCrops() debe retornar la cantidad de dias que hay entre la fecha desde y la fecha de cosecha");
        System.out.println("como la cantidad de dias en los que una parcela tuvo un cultivo plantado.");
        System.out.println();
        System.out.println("Situacion representada graficamente:");
        System.out.println();
        System.out.println("  fecha siembra             fecha cosecha");
        System.out.println("<------[-------------------------]----------------------->");
        System.out.println("            fecha desde                  fecha hasta");
        System.out.println("<----------------[----------------------------]---------->");
        System.out.println();
        System.out.println("Nota: Esta prueba unitaria es el caso de la prueba unitaria tres, pero con años diferentes.");
        System.out.println();

        /*
         * Fecha desde y fecha hasta para la prueba
         * unitaria
         */
        Calendar dateFrom = Calendar.getInstance();
        dateFrom.set(Calendar.YEAR, 2024);
        dateFrom.set(Calendar.MONTH, DECEMBER);
        dateFrom.set(Calendar.DAY_OF_MONTH, 20);

        Calendar dateUntil = Calendar.getInstance();
        dateUntil.set(Calendar.YEAR, 2025);
        dateUntil.set(Calendar.MONTH, JANUARY);
        dateUntil.set(Calendar.DAY_OF_MONTH, 31);

        /*
         * Creacion y persistencia de un registro de
         * plantacion de prueba
         */
        Calendar seedDate = Calendar.getInstance();
        seedDate.set(Calendar.YEAR, 2024);
        seedDate.set(Calendar.MONTH, DECEMBER);
        seedDate.set(Calendar.DAY_OF_MONTH, 1);

        Calendar harvestDate = Calendar.getInstance();
        harvestDate.set(Calendar.YEAR, 2025);
        harvestDate.set(Calendar.MONTH, JANUARY);
        harvestDate.set(Calendar.DAY_OF_MONTH, 17);

        PlantingRecord testPlantingRecordOne = new PlantingRecord();
        testPlantingRecordOne.setSeedDate(seedDate);
        testPlantingRecordOne.setHarvestDate(harvestDate);
        testPlantingRecordOne.setParcel(testParcel);
        testPlantingRecordOne.setCrop(cropService.find(1));
        testPlantingRecordOne.setStatus(statusService.findFinishedStatus());
        testPlantingRecordOne.setCropIrrigationWaterNeed("-");

        entityManager.getTransaction().begin();
        testPlantingRecordOne = plantingRecordService.create(testPlantingRecordOne);
        entityManager.getTransaction().commit();

        plantingRecords.add(testPlantingRecordOne);

        /*
         * Seccion de prueba
         */
        System.out.println("# Seccion de prueba");
        System.out.println("Periodo dentro del cual se calculara la cantidad de dias en los que la parcela con ID = " + testParcel.getId() + " tuvo un cultivo");
        System.out.println("plantado:");
        System.out.println("Fecha desde: " + UtilDate.formatDate(dateFrom));
        System.out.println("Fecha hasta: " + UtilDate.formatDate(dateUntil));
        System.out.println();

        System.out.println("Datos del registro de plantacion de prueba:");
        System.out.println("Fecha de siembra: " + UtilDate.formatDate(testPlantingRecordOne.getSeedDate()));
        System.out.println("Fecha de cosecha: " + UtilDate.formatDate(testPlantingRecordOne.getHarvestDate()));
        System.out.println("ID de la parcela a la que pertenece el registro de plantacion: " + testPlantingRecordOne.getParcel().getId());
        System.out.println();

        int expectedResult = 29;
        int result = plantingRecordService.calculateDaysWithCrops(testParcel.getId(), dateFrom, dateUntil);

        System.out.println("Resultado esperado: " + expectedResult);
        System.out.println("Resultado devuelto por el metodo calculateDaysWithCrops(): " + result);
        System.out.println();

        assertTrue(expectedResult == result);

        System.out.println("- Prueba pasada satisfactoriamente");
        System.out.println();
    }

    @Test
    public void testSeven() {
        System.out.println("******************************* Prueba siete del metodo calculateDaysWithCrops *******************************");
        printDescriptionMethodToTest();
        System.out.println();
        System.out.println("# Descripcion de la prueba unitaria");
        System.out.println("En esta prueba unitaria para demostrar el correcto funcionamiento del metodo calculateDaysWithCrops() se utiliza");
        System.out.println("una parcela que no tiene registros de plantacion finalizados en el periodo definido por una fecha desde y una fecha");
        System.out.println("hasta. Por lo tanto, el metodo calculateDaysWithCrops() debe retornar -1.");
        System.out.println();

        /*
         * Fecha desde y fecha hasta para la prueba
         * unitaria
         */
        Calendar dateFrom = Calendar.getInstance();
        dateFrom.set(Calendar.YEAR, 2021);
        dateFrom.set(Calendar.MONTH, DECEMBER);
        dateFrom.set(Calendar.DAY_OF_MONTH, 20);

        Calendar dateUntil = Calendar.getInstance();
        dateUntil.set(Calendar.YEAR, 2022);
        dateUntil.set(Calendar.MONTH, JANUARY);
        dateUntil.set(Calendar.DAY_OF_MONTH, 31);

        /*
         * Creacion y persistencia de un registro de
         * plantacion de prueba
         */
        Calendar seedDate = Calendar.getInstance();
        seedDate.set(Calendar.YEAR, 2023);
        seedDate.set(Calendar.MONTH, DECEMBER);
        seedDate.set(Calendar.DAY_OF_MONTH, 1);

        Calendar harvestDate = Calendar.getInstance();
        harvestDate.set(Calendar.YEAR, 2024);
        harvestDate.set(Calendar.MONTH, JANUARY);
        harvestDate.set(Calendar.DAY_OF_MONTH, 17);

        PlantingRecord testPlantingRecordOne = new PlantingRecord();
        testPlantingRecordOne.setSeedDate(seedDate);
        testPlantingRecordOne.setHarvestDate(harvestDate);
        testPlantingRecordOne.setParcel(testParcel);
        testPlantingRecordOne.setCrop(cropService.find(1));
        testPlantingRecordOne.setStatus(statusService.findFinishedStatus());
        testPlantingRecordOne.setCropIrrigationWaterNeed("-");

        entityManager.getTransaction().begin();
        testPlantingRecordOne = plantingRecordService.create(testPlantingRecordOne);
        entityManager.getTransaction().commit();

        plantingRecords.add(testPlantingRecordOne);

        /*
         * Seccion de prueba
         */
        System.out.println("# Seccion de prueba");
        System.out.println("Periodo dentro del cual se calculara la cantidad de dias en los que la parcela con ID = " + testParcel.getId() + " tuvo un cultivo");
        System.out.println("plantado:");
        System.out.println("Fecha desde: " + UtilDate.formatDate(dateFrom));
        System.out.println("Fecha hasta: " + UtilDate.formatDate(dateUntil));
        System.out.println();

        System.out.println("Datos del registro de plantacion de prueba:");
        System.out.println("Fecha de siembra: " + UtilDate.formatDate(testPlantingRecordOne.getSeedDate()));
        System.out.println("Fecha de cosecha: " + UtilDate.formatDate(testPlantingRecordOne.getHarvestDate()));
        System.out.println("ID de la parcela a la que pertenece el registro de plantacion: " + testPlantingRecordOne.getParcel().getId());
        System.out.println();

        int expectedResult = -1;
        int result = plantingRecordService.calculateDaysWithCrops(testParcel.getId(), dateFrom, dateUntil);

        System.out.println("Resultado esperado: " + expectedResult);
        System.out.println("Resultado devuelto por el metodo calculateDaysWithCrops(): " + result);
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
        for (PlantingRecord currenPlantingRecord : plantingRecords) {
            plantingRecordService.remove(currenPlantingRecord.getId());
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
        System.out.println("# Descripcion del metodo a probar");
        System.out.println("El metodo calculateDaysWithCrops() de la clase PlantingRecordService calcula la cantidad de dias en los que");
        System.out.println("una parcela tuvo un cultivo plantado si se lo invoca para una parcela que tiene registros de plantacion");
        System.out.println("finalizados en un periodo definido por dos fechas. En cambio, retorna -1 si se lo invoca para una parcela que");
        System.out.println("NO tiene registros de plantacion finalizados en un periodo definido por dos fechas.");
    }

}