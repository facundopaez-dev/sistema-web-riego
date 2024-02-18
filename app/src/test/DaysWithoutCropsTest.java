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

public class DaysWithoutCropsTest {

    private static EntityManager entityManager;
    private static EntityManagerFactory entityManagerFactory;
    private static PlantingRecordServiceBean plantingRecordService;
    private static CropServiceBean cropService;
    private static ParcelServiceBean parcelService;
    private static UserServiceBean userService;
    private static OptionServiceBean optionService;
    private static PlantingRecordStatusServiceBean statusService;

    private static User testUser;

    private static Parcel testParcelOne;
    private static Parcel testParcelTwo;
    private static Parcel testParcelThree;
    private static Parcel testParcelFour;
    private static Parcel testParcelFive;
    private static Parcel testParcelSix;
    private static Parcel testParcelSeven;
    private static Parcel testParcelEight;
    private static Parcel testParcelNine;

    private static Option parcelOptionOne;
    private static Option parcelOptionTwo;
    private static Option parcelOptionThree;
    private static Option parcelOptionFour;
    private static Option parcelOptionFive;
    private static Option parcelOptionSix;
    private static Option parcelOptionSeven;
    private static Option parcelOptionEight;
    private static Option parcelOptionNine;

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
        testUser.setUsername("DaysWithoutCropsTest");
        testUser.setName("DaysWithoutCropsTestUserName");
        testUser.setLastName("DaysWithoutCropsTestLastName");
        testUser.setEmail("DaysWithoutCropsTest@email");

        entityManager.getTransaction().begin();
        testUser = userService.create(testUser);
        entityManager.getTransaction().commit();

        users.add(testUser);

        /*
         * Creacion y persistencia de una opcion para una
         * parcela de prueba
         */
        entityManager.getTransaction().begin();
        parcelOptionOne = optionService.create();
        parcelOptionTwo = optionService.create();
        parcelOptionThree = optionService.create();
        parcelOptionFour = optionService.create();
        parcelOptionFive = optionService.create();
        parcelOptionSix = optionService.create();
        parcelOptionSeven = optionService.create();
        parcelOptionEight = optionService.create();
        parcelOptionNine = optionService.create();
        entityManager.getTransaction().commit();

        options.add(parcelOptionOne);
        options.add(parcelOptionTwo);
        options.add(parcelOptionThree);
        options.add(parcelOptionFour);
        options.add(parcelOptionFive);
        options.add(parcelOptionSix);
        options.add(parcelOptionSeven);
        options.add(parcelOptionEight);
        options.add(parcelOptionNine);

        /*
         * Creacion y persistencia de una parcela de prueba
         */
        testParcelOne = new Parcel();
        testParcelOne.setName("testParcelOne");
        testParcelOne.setOption(parcelOptionOne);

        testParcelTwo = new Parcel();
        testParcelTwo.setName("testParcelTwo");
        testParcelTwo.setOption(parcelOptionTwo);

        testParcelThree = new Parcel();
        testParcelThree.setName("testParcelThree");
        testParcelThree.setOption(parcelOptionThree);

        testParcelFour = new Parcel();
        testParcelFour.setName("testParcelFour");
        testParcelFour.setOption(parcelOptionFour);

        testParcelFive = new Parcel();
        testParcelFive.setName("testParcelFive");
        testParcelFive.setOption(parcelOptionFive);

        testParcelSix = new Parcel();
        testParcelSix.setName("testParcelSix");
        testParcelSix.setOption(parcelOptionSix);

        testParcelSeven = new Parcel();
        testParcelSeven.setName("testParcelSeven");
        testParcelSeven.setOption(parcelOptionSeven);

        testParcelEight = new Parcel();
        testParcelEight.setName("testParcelEight");
        testParcelEight.setOption(parcelOptionEight);

        testParcelNine = new Parcel();
        testParcelNine.setName("testParcelNine");
        testParcelNine.setOption(parcelOptionNine);

        entityManager.getTransaction().begin();
        testParcelOne = parcelService.create(testParcelOne);
        testParcelOne = parcelService.create(testParcelOne);
        testParcelTwo= parcelService.create(testParcelTwo);
        testParcelThree = parcelService.create(testParcelThree);
        testParcelFour = parcelService.create(testParcelFour);
        testParcelFive = parcelService.create(testParcelFive);
        testParcelSix = parcelService.create(testParcelSix);
        testParcelSeven = parcelService.create(testParcelSeven);
        testParcelEight = parcelService.create(testParcelEight);
        testParcelNine = parcelService.create(testParcelNine);
        entityManager.getTransaction().commit();

        parcels.add(testParcelOne);
        parcels.add(testParcelTwo);
        parcels.add(testParcelThree);
        parcels.add(testParcelFour);
        parcels.add(testParcelFive);
        parcels.add(testParcelSix);
        parcels.add(testParcelSeven);
        parcels.add(testParcelEight);
        parcels.add(testParcelNine);
    }

    @Test
    public void testOne() {
        System.out.println("******************************* Prueba uno del metodo calculateDaysWithoutCrops *******************************");
        printDescriptionMethodToTest();
        System.out.println();
        System.out.println("# Descripcion de la prueba unitaria");
        System.out.println("En esta prueba unitaria para demostrar el correcto funcionamiento del metodo calculateDaysWithoutCrops() se utiliza");
        System.out.println("un registro de plantacion finalizado que tiene una fecha de siembra igual a una fecha desde y una fecha de cosecha");
        System.out.println("igual a una fecha hasta. Por lo tanto, el metodo calculateDaysWithoutCrops() debe retornar 0 la cantidad de dias en");
        System.out.println("los que una parcela NO tuvo ningun cultivo plantado.");
        System.out.println();

        /*
         * Fecha desde y fecha hasta para la prueba
         * unitaria
         */
        Calendar dateFrom = Calendar.getInstance();
        dateFrom.set(Calendar.YEAR, 2022);
        dateFrom.set(Calendar.MONTH, JANUARY);
        dateFrom.set(Calendar.DAY_OF_MONTH, 1);

        Calendar dateUntil = Calendar.getInstance();
        dateUntil.set(Calendar.YEAR, 2022);
        dateUntil.set(Calendar.MONTH, JANUARY);
        dateUntil.set(Calendar.DAY_OF_MONTH, 31);

        /*
         * Creacion y persistencia de un registro de
         * plantacion de prueba
         */
        Calendar seedDate = Calendar.getInstance();
        seedDate.set(Calendar.YEAR, 2022);
        seedDate.set(Calendar.MONTH, JANUARY);
        seedDate.set(Calendar.DAY_OF_MONTH, 1);

        Calendar harvestDate = Calendar.getInstance();
        harvestDate.set(Calendar.YEAR, 2022);
        harvestDate.set(Calendar.MONTH, JANUARY);
        harvestDate.set(Calendar.DAY_OF_MONTH, 31);

        PlantingRecord testPlantingRecord = new PlantingRecord();
        testPlantingRecord.setSeedDate(seedDate);
        testPlantingRecord.setHarvestDate(harvestDate);
        testPlantingRecord.setParcel(testParcelOne);
        testPlantingRecord.setCrop(cropService.find(1));
        testPlantingRecord.setStatus(statusService.findFinishedStatus());
        testPlantingRecord.setCropIrrigationWaterNeed("-");

        entityManager.getTransaction().begin();
        testPlantingRecord = plantingRecordService.create(testPlantingRecord);
        entityManager.getTransaction().commit();

        plantingRecords.add(testPlantingRecord);

        /*
         * Seccion de prueba
         */
        System.out.println("# Seccion de prueba");
        System.out.println("Periodo dentro del cual se calculara la cantidad de dias en los que la parcela con ID = " + testParcelOne.getId() + " NO tuvo ningun cultivo");
        System.out.println("plantado:");
        System.out.println("Fecha desde: " + UtilDate.formatDate(dateFrom));
        System.out.println("Fecha hasta: " + UtilDate.formatDate(dateUntil));
        System.out.println();

        System.out.println("Datos del registro de plantacion de prueba:");
        System.out.println("Fecha de siembra: " + UtilDate.formatDate(testPlantingRecord.getSeedDate()));
        System.out.println("Fecha de cosecha: " + UtilDate.formatDate(testPlantingRecord.getHarvestDate()));
        System.out.println("ID de la parcela a la que pertenece el registro de plantacion: " + testPlantingRecord.getParcel().getId());
        System.out.println();

        int expectedResult = 0;
        int result = plantingRecordService.calculateDaysWithoutCrops(testParcelOne.getId(), dateFrom, dateUntil);

        System.out.println("Resultado esperado: " + expectedResult);
        System.out.println("Resultado devuelto por el metodo calculateDaysWithoutCrops(): " + result);
        System.out.println();

        assertTrue(expectedResult == result);

        System.out.println("- Prueba pasada satisfactoriamente");
        System.out.println();
    }

    @Test
    public void testTwo() {
        System.out.println("******************************* Prueba dos del metodo calculateDaysWithoutCrops *******************************");
        printDescriptionMethodToTest();
        System.out.println();
        System.out.println("# Descripcion de la prueba unitaria");
        System.out.println("En esta prueba unitaria para demostrar el correcto funcionamiento del metodo calculateDaysWithoutCrops() se utiliza");
        System.out.println("un registro de plantacion finalizado que tiene una fecha de siembra mayor o igual a una fecha desde y menor");
        System.out.println("o igual a una fecha hasta, y una fecha de cosecha estrictamente mayor a una fecha hasta. Por lo tanto, el metodo");
        System.out.println("calculateDaysWithoutCrops() debe retornar la diferencia entre la fecha de siembra y la fecha desde como la cantidad");
        System.out.println("de dias en los que una parcela NO tuvo ningun cultivo plantado.");
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
        dateFrom.set(Calendar.MONTH, DECEMBER);
        dateFrom.set(Calendar.DAY_OF_MONTH, 15);

        Calendar dateUntil = Calendar.getInstance();
        dateUntil.set(Calendar.YEAR, 2023);
        dateUntil.set(Calendar.MONTH, FEBRUARY);
        dateUntil.set(Calendar.DAY_OF_MONTH, 12);

        /*
         * Creacion y persistencia de un registro de
         * plantacion de prueba
         */
        Calendar seedDate = Calendar.getInstance();
        seedDate.set(Calendar.YEAR, 2023);
        seedDate.set(Calendar.MONTH, JANUARY);
        seedDate.set(Calendar.DAY_OF_MONTH, 15);

        Calendar harvestDate = Calendar.getInstance();
        harvestDate.set(Calendar.YEAR, 2023);
        harvestDate.set(Calendar.MONTH, FEBRUARY);
        harvestDate.set(Calendar.DAY_OF_MONTH, 18);

        PlantingRecord testPlantingRecord = new PlantingRecord();
        testPlantingRecord.setSeedDate(seedDate);
        testPlantingRecord.setHarvestDate(harvestDate);
        testPlantingRecord.setParcel(testParcelTwo);
        testPlantingRecord.setCrop(cropService.find(1));
        testPlantingRecord.setStatus(statusService.findFinishedStatus());
        testPlantingRecord.setCropIrrigationWaterNeed("-");

        entityManager.getTransaction().begin();
        testPlantingRecord = plantingRecordService.create(testPlantingRecord);
        entityManager.getTransaction().commit();

        plantingRecords.add(testPlantingRecord);

        /*
         * Seccion de prueba
         */
        System.out.println("# Seccion de prueba");
        System.out.println("Periodo dentro del cual se calculara la cantidad de dias en los que la parcela con ID = " + testParcelTwo.getId() + " NO tuvo ningun cultivo");
        System.out.println("plantado:");
        System.out.println("Fecha desde: " + UtilDate.formatDate(dateFrom));
        System.out.println("Fecha hasta: " + UtilDate.formatDate(dateUntil));
        System.out.println();

        System.out.println("Datos del registro de plantacion de prueba:");
        System.out.println("Fecha de siembra: " + UtilDate.formatDate(testPlantingRecord.getSeedDate()));
        System.out.println("Fecha de cosecha: " + UtilDate.formatDate(testPlantingRecord.getHarvestDate()));
        System.out.println("ID de la parcela a la que pertenece el registro de plantacion: " + testPlantingRecord.getParcel().getId());
        System.out.println();

        int expectedResult = 31;
        int result = plantingRecordService.calculateDaysWithoutCrops(testParcelTwo.getId(), dateFrom, dateUntil);

        System.out.println("Resultado esperado: " + expectedResult);
        System.out.println("Resultado devuelto por el metodo calculateDaysWithoutCrops(): " + result);
        System.out.println();

        assertTrue(expectedResult == result);

        System.out.println("- Prueba pasada satisfactoriamente");
        System.out.println();
    }

    @Test
    public void testThree() {
        System.out.println("******************************* Prueba tres del metodo calculateDaysWithoutCrops *******************************");
        printDescriptionMethodToTest();
        System.out.println();
        System.out.println("# Descripcion de la prueba unitaria");
        System.out.println("En esta prueba unitaria para demostrar el correcto funcionamiento del metodo calculateDaysWithoutCrops() se utiliza");
        System.out.println("un registro de plantacion finalizado que tiene una fecha de cosecha mayor o igual a una fecha desde y menor");
        System.out.println("o igual a una fecha hasta, y una fecha de siembra estrictamente menor a una fecha desde. Por lo tanto, el metodo");
        System.out.println("calculateDaysWithoutCrops() debe retornar la diferencia entre fecha hasta y la fecha de cosecha como la cantidad");
        System.out.println("de dias en los que una parcela NO tuvo ningun cultivo plantado.");
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
        dateFrom.set(Calendar.YEAR, 2023);
        dateFrom.set(Calendar.MONTH, DECEMBER);
        dateFrom.set(Calendar.DAY_OF_MONTH, 20);

        Calendar dateUntil = Calendar.getInstance();
        dateUntil.set(Calendar.YEAR, 2024);
        dateUntil.set(Calendar.MONTH, MARCH);
        dateUntil.set(Calendar.DAY_OF_MONTH, 10);

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
        harvestDate.set(Calendar.MONTH, FEBRUARY);
        harvestDate.set(Calendar.DAY_OF_MONTH, 20);

        PlantingRecord testPlantingRecord = new PlantingRecord();
        testPlantingRecord.setSeedDate(seedDate);
        testPlantingRecord.setHarvestDate(harvestDate);
        testPlantingRecord.setParcel(testParcelThree);
        testPlantingRecord.setCrop(cropService.find(1));
        testPlantingRecord.setStatus(statusService.findFinishedStatus());
        testPlantingRecord.setCropIrrigationWaterNeed("-");

        entityManager.getTransaction().begin();
        testPlantingRecord = plantingRecordService.create(testPlantingRecord);
        entityManager.getTransaction().commit();

        plantingRecords.add(testPlantingRecord);

        /*
         * Seccion de prueba
         */
        System.out.println("# Seccion de prueba");
        System.out.println("Periodo dentro del cual se calculara la cantidad de dias en los que la parcela con ID = " + testParcelThree.getId() + " NO tuvo ningun cultivo");
        System.out.println("plantado:");
        System.out.println("Fecha desde: " + UtilDate.formatDate(dateFrom));
        System.out.println("Fecha hasta: " + UtilDate.formatDate(dateUntil));
        System.out.println();

        System.out.println("Datos del registro de plantacion de prueba:");
        System.out.println("Fecha de siembra: " + UtilDate.formatDate(testPlantingRecord.getSeedDate()));
        System.out.println("Fecha de cosecha: " + UtilDate.formatDate(testPlantingRecord.getHarvestDate()));
        System.out.println("ID de la parcela a la que pertenece el registro de plantacion: " + testPlantingRecord.getParcel().getId());
        System.out.println();

        int expectedResult = 19;
        int result = plantingRecordService.calculateDaysWithoutCrops(testParcelThree.getId(), dateFrom, dateUntil);

        System.out.println("Resultado esperado: " + expectedResult);
        System.out.println("Resultado devuelto por el metodo calculateDaysWithoutCrops(): " + result);
        System.out.println();

        assertTrue(expectedResult == result);

        System.out.println("- Prueba pasada satisfactoriamente");
        System.out.println();
    }

    @Test
    public void testFour() {
        System.out.println("******************************* Prueba cuatro del metodo calculateDaysWithoutCrops *******************************");
        printDescriptionMethodToTest();
        System.out.println();
        System.out.println("# Descripcion de la prueba unitaria");
        System.out.println("En esta prueba unitaria para demostrar el correcto funcionamiento del metodo calculateDaysWithoutCrops() se utiliza");
        System.out.println("un registro de plantacion finalizado que tiene una fecha de siembra estrictamente mayor a una fecha desde y una fecha");
        System.out.println("de cosecha estrictamente menor una fecha hasta. Por lo tanto, el metodo calculateDaysWithoutCrops() debe retornar");
        System.out.println("la suma de la diferencia entre la fecha de siembra y la fecha desde, con la diferencia entre la fecha de cosecha y");
        System.out.println("la fecha hasta como la cantidad de dias en los que una parcela NO tuvo ningun cultivo plantado.");
        System.out.println();

        /*
         * Fecha desde y fecha hasta para la prueba
         * unitaria
         */
        Calendar dateFrom = Calendar.getInstance();
        dateFrom.set(Calendar.YEAR, 2019);
        dateFrom.set(Calendar.MONTH, NOVEMBER);
        dateFrom.set(Calendar.DAY_OF_MONTH, 25);

        Calendar dateUntil = Calendar.getInstance();
        dateUntil.set(Calendar.YEAR, 2020);
        dateUntil.set(Calendar.MONTH, FEBRUARY);
        dateUntil.set(Calendar.DAY_OF_MONTH, 11);

        /*
         * Creacion y persistencia de un registro de
         * plantacion de prueba
         */
        Calendar seedDate = Calendar.getInstance();
        seedDate.set(Calendar.YEAR, 2019);
        seedDate.set(Calendar.MONTH, DECEMBER);
        seedDate.set(Calendar.DAY_OF_MONTH, 1);

        Calendar harvestDate = Calendar.getInstance();
        harvestDate.set(Calendar.YEAR, 2020);
        harvestDate.set(Calendar.MONTH, JANUARY);
        harvestDate.set(Calendar.DAY_OF_MONTH, 31);

        PlantingRecord testPlantingRecord = new PlantingRecord();
        testPlantingRecord.setSeedDate(seedDate);
        testPlantingRecord.setHarvestDate(harvestDate);
        testPlantingRecord.setParcel(testParcelFour);
        testPlantingRecord.setCrop(cropService.find(1));
        testPlantingRecord.setStatus(statusService.findFinishedStatus());
        testPlantingRecord.setCropIrrigationWaterNeed("-");

        entityManager.getTransaction().begin();
        testPlantingRecord = plantingRecordService.create(testPlantingRecord);
        entityManager.getTransaction().commit();

        plantingRecords.add(testPlantingRecord);

        /*
         * Seccion de prueba
         */
        System.out.println("# Seccion de prueba");
        System.out.println("Periodo dentro del cual se calculara la cantidad de dias en los que la parcela con ID = " + testParcelFour.getId() + " NO tuvo ningun cultivo");
        System.out.println("plantado:");
        System.out.println("Fecha desde: " + UtilDate.formatDate(dateFrom));
        System.out.println("Fecha hasta: " + UtilDate.formatDate(dateUntil));
        System.out.println();

        System.out.println("Datos del registro de plantacion de prueba:");
        System.out.println("Fecha de siembra: " + UtilDate.formatDate(testPlantingRecord.getSeedDate()));
        System.out.println("Fecha de cosecha: " + UtilDate.formatDate(testPlantingRecord.getHarvestDate()));
        System.out.println("ID de la parcela a la que pertenece el registro de plantacion: " + testPlantingRecord.getParcel().getId());
        System.out.println();

        int expectedResult = 17;
        int result = plantingRecordService.calculateDaysWithoutCrops(testParcelFour.getId(), dateFrom, dateUntil);

        System.out.println("Resultado esperado: " + expectedResult);
        System.out.println("Resultado devuelto por el metodo calculateDaysWithoutCrops(): " + result);
        System.out.println();

        assertTrue(expectedResult == result);

        System.out.println("- Prueba pasada satisfactoriamente");
        System.out.println();
    }

    @Test
    public void testFive() {
        System.out.println("******************************* Prueba cinco del metodo calculateDaysWithoutCrops *******************************");
        printDescriptionMethodToTest();
        System.out.println();
        System.out.println("# Descripcion de la prueba unitaria");
        System.out.println("En esta prueba unitaria para demostrar el correcto funcionamiento del metodo calculateDaysWithoutCrops() se utiliza");
        System.out.println("un registro de plantacion finalizado que tiene una fecha de siembra estrictamente mayor a una fecha desde y una fecha");
        System.out.println("de cosecha estrictamente menor una fecha hasta. Por lo tanto, el metodo calculateDaysWithoutCrops() debe retornar");
        System.out.println("la suma de la diferencia entre la fecha de siembra y la fecha desde, con la diferencia entre la fecha de cosecha y");
        System.out.println("la fecha hasta como la cantidad de dias en los que una parcela NO tuvo ningun cultivo plantado.");
        System.out.println();

        /*
         * Fecha desde y fecha hasta para la prueba
         * unitaria
         */
        Calendar dateFrom = Calendar.getInstance();
        dateFrom.set(Calendar.YEAR, 2019);
        dateFrom.set(Calendar.MONTH, NOVEMBER);
        dateFrom.set(Calendar.DAY_OF_MONTH, 25);

        Calendar dateUntil = Calendar.getInstance();
        dateUntil.set(Calendar.YEAR, 2020);
        dateUntil.set(Calendar.MONTH, FEBRUARY);
        dateUntil.set(Calendar.DAY_OF_MONTH, 11);

        /*
         * Creacion y persistencia de un registro de
         * plantacion de prueba
         */
        Calendar seedDate = Calendar.getInstance();
        seedDate.set(Calendar.YEAR, 2019);
        seedDate.set(Calendar.MONTH, DECEMBER);
        seedDate.set(Calendar.DAY_OF_MONTH, 1);

        Calendar harvestDate = Calendar.getInstance();
        harvestDate.set(Calendar.YEAR, 2020);
        harvestDate.set(Calendar.MONTH, JANUARY);
        harvestDate.set(Calendar.DAY_OF_MONTH, 31);

        PlantingRecord testPlantingRecord = new PlantingRecord();
        testPlantingRecord.setSeedDate(seedDate);
        testPlantingRecord.setHarvestDate(harvestDate);
        testPlantingRecord.setParcel(testParcelFive);
        testPlantingRecord.setCrop(cropService.find(1));
        testPlantingRecord.setStatus(statusService.findFinishedStatus());
        testPlantingRecord.setCropIrrigationWaterNeed("-");

        entityManager.getTransaction().begin();
        testPlantingRecord = plantingRecordService.create(testPlantingRecord);
        entityManager.getTransaction().commit();

        plantingRecords.add(testPlantingRecord);

        /*
         * Seccion de prueba
         */
        System.out.println("# Seccion de prueba");
        System.out.println("Periodo dentro del cual se calculara la cantidad de dias en los que la parcela con ID = " + testParcelFive.getId() + " NO tuvo ningun cultivo");
        System.out.println("plantado:");
        System.out.println("Fecha desde: " + UtilDate.formatDate(dateFrom));
        System.out.println("Fecha hasta: " + UtilDate.formatDate(dateUntil));
        System.out.println();

        System.out.println("Datos del registro de plantacion de prueba:");
        System.out.println("Fecha de siembra: " + UtilDate.formatDate(testPlantingRecord.getSeedDate()));
        System.out.println("Fecha de cosecha: " + UtilDate.formatDate(testPlantingRecord.getHarvestDate()));
        System.out.println("ID de la parcela a la que pertenece el registro de plantacion: " + testPlantingRecord.getParcel().getId());
        System.out.println();

        int expectedResult = 17;
        int result = plantingRecordService.calculateDaysWithoutCrops(testParcelFive.getId(), dateFrom, dateUntil);

        System.out.println("Resultado esperado: " + expectedResult);
        System.out.println("Resultado devuelto por el metodo calculateDaysWithoutCrops(): " + result);
        System.out.println();

        assertTrue(expectedResult == result);

        System.out.println("- Prueba pasada satisfactoriamente");
        System.out.println();
    }

    @Test
    public void testSix() {
        System.out.println("******************************* Prueba seis del metodo calculateDaysWithoutCrops *******************************");
        printDescriptionMethodToTest();
        System.out.println();
        System.out.println("# Descripcion de la prueba unitaria");
        System.out.println("En esta prueba unitaria para demostrar el correcto funcionamiento del metodo calculateDaysWithoutCrops() se utilizan");
        System.out.println("cuatro registros de plantacion.");
        System.out.println("- Entre la fecha de cosecha del registro de plantacion uno y la fecha de siembra del registro de plantacion dos");
        System.out.println("NO hay ningun dia de diferencia porque la segunda es el dia inmediatamente siguiente a la primera.");
        System.out.println("- Entre la fecha de cosecha del registro de plantacion dos y la fecha de siembra del registro de plantacion tres");
        System.out.println("hay DOS dias de diferencia.");
        System.out.println("- Entre la fecha de cosecha del registro de plantacion tres y la fecha de siembra del registro de plantacion cuatro");
        System.out.println("hay TRES dias de diferencia.");
        System.out.println();
        System.out.println("La fecha desde es igual a la fecha de siembra del registro de plantacion uno y la fecha hasta es igual a la fecha de");
        System.out.println("cosecha del registro de plantacion cuatro.");
        System.out.println();
        System.out.println("Por lo tanto, el metodo calculateDaysWithoutCrops() debe retornar 5 como la cantidad de dias en los que una parcela");
        System.out.println("NO tuvo ningun cultivo plantado.");
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
        dateUntil.set(Calendar.MONTH, APRIL);
        dateUntil.set(Calendar.DAY_OF_MONTH, 25);

        /*
         * Creacion y persistencia de un registro de
         * plantacion de prueba
         */
        Calendar seedDateOne = Calendar.getInstance();
        seedDateOne.set(Calendar.YEAR, 2019);
        seedDateOne.set(Calendar.MONTH, DECEMBER);
        seedDateOne.set(Calendar.DAY_OF_MONTH, 1);

        Calendar harvestDateOne = Calendar.getInstance();
        harvestDateOne.set(Calendar.YEAR, 2020);
        harvestDateOne.set(Calendar.MONTH, JANUARY);
        harvestDateOne.set(Calendar.DAY_OF_MONTH, 31);

        PlantingRecord testPlantingRecordOne = new PlantingRecord();
        testPlantingRecordOne.setSeedDate(seedDateOne);
        testPlantingRecordOne.setHarvestDate(harvestDateOne);
        testPlantingRecordOne.setParcel(testParcelSix);
        testPlantingRecordOne.setCrop(cropService.find(1));
        testPlantingRecordOne.setStatus(statusService.findFinishedStatus());
        testPlantingRecordOne.setCropIrrigationWaterNeed("-");

        Calendar seedDateTwo = Calendar.getInstance();
        seedDateTwo.set(Calendar.YEAR, 2020);
        seedDateTwo.set(Calendar.MONTH, FEBRUARY);
        seedDateTwo.set(Calendar.DAY_OF_MONTH, 1);

        Calendar harvestDateTwo = Calendar.getInstance();
        harvestDateTwo.set(Calendar.YEAR, 2020);
        harvestDateTwo.set(Calendar.MONTH, MARCH);
        harvestDateTwo.set(Calendar.DAY_OF_MONTH, 1);

        PlantingRecord testPlantingRecordTwo = new PlantingRecord();
        testPlantingRecordTwo.setSeedDate(seedDateTwo);
        testPlantingRecordTwo.setHarvestDate(harvestDateTwo);
        testPlantingRecordTwo.setParcel(testParcelSix);
        testPlantingRecordTwo.setCrop(cropService.find(1));
        testPlantingRecordTwo.setStatus(statusService.findFinishedStatus());
        testPlantingRecordTwo.setCropIrrigationWaterNeed("-");

        Calendar seedDateThree = Calendar.getInstance();
        seedDateThree.set(Calendar.YEAR, 2020);
        seedDateThree.set(Calendar.MONTH, MARCH);
        seedDateThree.set(Calendar.DAY_OF_MONTH, 4);

        Calendar harvestDateThree = Calendar.getInstance();
        harvestDateThree.set(Calendar.YEAR, 2020);
        harvestDateThree.set(Calendar.MONTH, MARCH);
        harvestDateThree.set(Calendar.DAY_OF_MONTH, 31);

        PlantingRecord testPlantingRecordThree = new PlantingRecord();
        testPlantingRecordThree.setSeedDate(seedDateThree);
        testPlantingRecordThree.setHarvestDate(harvestDateThree);
        testPlantingRecordThree.setParcel(testParcelSix);
        testPlantingRecordThree.setCrop(cropService.find(1));
        testPlantingRecordThree.setStatus(statusService.findFinishedStatus());
        testPlantingRecordThree.setCropIrrigationWaterNeed("-");

        Calendar seedDateFour = Calendar.getInstance();
        seedDateFour.set(Calendar.YEAR, 2020);
        seedDateFour.set(Calendar.MONTH, APRIL);
        seedDateFour.set(Calendar.DAY_OF_MONTH, 4);

        Calendar harvestDateFour = Calendar.getInstance();
        harvestDateFour.set(Calendar.YEAR, 2020);
        harvestDateFour.set(Calendar.MONTH, APRIL);
        harvestDateFour.set(Calendar.DAY_OF_MONTH, 25);

        PlantingRecord testPlantingRecordFour = new PlantingRecord();
        testPlantingRecordFour.setSeedDate(seedDateFour);
        testPlantingRecordFour.setHarvestDate(harvestDateFour);
        testPlantingRecordFour.setParcel(testParcelSix);
        testPlantingRecordFour.setCrop(cropService.find(1));
        testPlantingRecordFour.setStatus(statusService.findFinishedStatus());
        testPlantingRecordFour.setCropIrrigationWaterNeed("-");

        entityManager.getTransaction().begin();
        testPlantingRecordOne = plantingRecordService.create(testPlantingRecordOne);
        testPlantingRecordTwo = plantingRecordService.create(testPlantingRecordTwo);
        testPlantingRecordThree = plantingRecordService.create(testPlantingRecordThree);
        testPlantingRecordFour = plantingRecordService.create(testPlantingRecordFour);
        entityManager.getTransaction().commit();

        plantingRecords.add(testPlantingRecordOne);
        plantingRecords.add(testPlantingRecordTwo);
        plantingRecords.add(testPlantingRecordThree);
        plantingRecords.add(testPlantingRecordFour);

        /*
         * Seccion de prueba
         */
        System.out.println("# Seccion de prueba");
        System.out.println("Periodo dentro del cual se calculara la cantidad de dias en los que la parcela con ID = " + testParcelSix.getId() + " NO tuvo ningun cultivo");
        System.out.println("plantado:");
        System.out.println("Fecha desde: " + UtilDate.formatDate(dateFrom));
        System.out.println("Fecha hasta: " + UtilDate.formatDate(dateUntil));
        System.out.println();

        System.out.println("Datos de los registros de plantacion de prueba:");
        System.out.println("Fecha de siembra del registro de plantacion uno: " + UtilDate.formatDate(testPlantingRecordOne.getSeedDate()));
        System.out.println("Fecha de cosecha del registro de plantacion uno: " + UtilDate.formatDate(testPlantingRecordOne.getHarvestDate()));
        System.out.println("ID de la parcela a la que pertenece el registro de plantacion uno: " + testPlantingRecordOne.getParcel().getId());
        System.out.println();

        System.out.println("Fecha de siembra del registro de plantacion dos: " + UtilDate.formatDate(testPlantingRecordTwo.getSeedDate()));
        System.out.println("Fecha de cosecha del registro de plantacion dos: " + UtilDate.formatDate(testPlantingRecordTwo.getHarvestDate()));
        System.out.println("ID de la parcela a la que pertenece el registro de plantacion dos: " + testPlantingRecordTwo.getParcel().getId());
        System.out.println();

        System.out.println("Fecha de siembra del registro de plantacion tres: " + UtilDate.formatDate(testPlantingRecordThree.getSeedDate()));
        System.out.println("Fecha de cosecha del registro de plantacion tres: " + UtilDate.formatDate(testPlantingRecordThree.getHarvestDate()));
        System.out.println("ID de la parcela a la que pertenece el registro de plantacion tres: " + testPlantingRecordThree.getParcel().getId());
        System.out.println();

        System.out.println("Fecha de siembra del registro de plantacion cuatro: " + UtilDate.formatDate(testPlantingRecordFour.getSeedDate()));
        System.out.println("Fecha de cosecha del registro de plantacion cuatro: " + UtilDate.formatDate(testPlantingRecordFour.getHarvestDate()));
        System.out.println("ID de la parcela a la que pertenece el registro de plantacion cuatro: " + testPlantingRecordFour.getParcel().getId());
        System.out.println();

        int expectedResult = 5;
        int result = plantingRecordService.calculateDaysWithoutCrops(testParcelSix.getId(), dateFrom, dateUntil);

        System.out.println("Resultado esperado: " + expectedResult);
        System.out.println("Resultado devuelto por el metodo calculateDaysWithoutCrops(): " + result);
        System.out.println();

        assertTrue(expectedResult == result);

        System.out.println("- Prueba pasada satisfactoriamente");
        System.out.println();
    }

    @Test
    public void testSeven() {
        System.out.println("******************************* Prueba siete del metodo calculateDaysWithoutCrops *******************************");
        printDescriptionMethodToTest();
        System.out.println();
        System.out.println("# Descripcion de la prueba unitaria");
        System.out.println("En esta prueba unitaria para demostrar el correcto funcionamiento del metodo calculateDaysWithoutCrops() se utilizan");
        System.out.println("cuatro registros de plantacion.");
        System.out.println("- Entre la fecha de cosecha del registro de plantacion uno y la fecha de siembra del registro de plantacion dos");
        System.out.println("NO hay ningun dia de diferencia porque la segunda es el dia inmediatamente siguiente a la primera.");
        System.out.println("- Entre la fecha de cosecha del registro de plantacion dos y la fecha de siembra del registro de plantacion tres");
        System.out.println("hay DOS dias de diferencia.");
        System.out.println("- Entre la fecha de cosecha del registro de plantacion tres y la fecha de siembra del registro de plantacion cuatro");
        System.out.println("hay TRES dias de diferencia.");
        System.out.println();
        System.out.println("La fecha desde esta tres dias antes de la fecha de siembra del registro de plantacion uno y la fecha hasta esta tres dias");
        System.out.println("despues de la fecha de cosecha del registro de plantacion cuatro.");
        System.out.println();
        System.out.println("Por lo tanto, el metodo calculateDaysWithoutCrops() debe retornar 11 como la cantidad de dias en los que una parcela");
        System.out.println("NO tuvo ningun cultivo plantado.");
        System.out.println();

        /*
         * Fecha desde y fecha hasta para la prueba
         * unitaria
         */
        Calendar dateFrom = Calendar.getInstance();
        dateFrom.set(Calendar.YEAR, 2019);
        dateFrom.set(Calendar.MONTH, NOVEMBER);
        dateFrom.set(Calendar.DAY_OF_MONTH, 28);

        Calendar dateUntil = Calendar.getInstance();
        dateUntil.set(Calendar.YEAR, 2020);
        dateUntil.set(Calendar.MONTH, APRIL);
        dateUntil.set(Calendar.DAY_OF_MONTH, 28);

        /*
         * Creacion y persistencia de un registro de
         * plantacion de prueba
         */
        Calendar seedDateOne = Calendar.getInstance();
        seedDateOne.set(Calendar.YEAR, 2019);
        seedDateOne.set(Calendar.MONTH, DECEMBER);
        seedDateOne.set(Calendar.DAY_OF_MONTH, 1);

        Calendar harvestDateOne = Calendar.getInstance();
        harvestDateOne.set(Calendar.YEAR, 2020);
        harvestDateOne.set(Calendar.MONTH, JANUARY);
        harvestDateOne.set(Calendar.DAY_OF_MONTH, 31);

        PlantingRecord testPlantingRecordOne = new PlantingRecord();
        testPlantingRecordOne.setSeedDate(seedDateOne);
        testPlantingRecordOne.setHarvestDate(harvestDateOne);
        testPlantingRecordOne.setParcel(testParcelSeven);
        testPlantingRecordOne.setCrop(cropService.find(1));
        testPlantingRecordOne.setStatus(statusService.findFinishedStatus());
        testPlantingRecordOne.setCropIrrigationWaterNeed("-");

        Calendar seedDateTwo = Calendar.getInstance();
        seedDateTwo.set(Calendar.YEAR, 2020);
        seedDateTwo.set(Calendar.MONTH, FEBRUARY);
        seedDateTwo.set(Calendar.DAY_OF_MONTH, 1);

        Calendar harvestDateTwo = Calendar.getInstance();
        harvestDateTwo.set(Calendar.YEAR, 2020);
        harvestDateTwo.set(Calendar.MONTH, MARCH);
        harvestDateTwo.set(Calendar.DAY_OF_MONTH, 1);

        PlantingRecord testPlantingRecordTwo = new PlantingRecord();
        testPlantingRecordTwo.setSeedDate(seedDateTwo);
        testPlantingRecordTwo.setHarvestDate(harvestDateTwo);
        testPlantingRecordTwo.setParcel(testParcelSeven);
        testPlantingRecordTwo.setCrop(cropService.find(1));
        testPlantingRecordTwo.setStatus(statusService.findFinishedStatus());
        testPlantingRecordTwo.setCropIrrigationWaterNeed("-");

        Calendar seedDateThree = Calendar.getInstance();
        seedDateThree.set(Calendar.YEAR, 2020);
        seedDateThree.set(Calendar.MONTH, MARCH);
        seedDateThree.set(Calendar.DAY_OF_MONTH, 4);

        Calendar harvestDateThree = Calendar.getInstance();
        harvestDateThree.set(Calendar.YEAR, 2020);
        harvestDateThree.set(Calendar.MONTH, MARCH);
        harvestDateThree.set(Calendar.DAY_OF_MONTH, 31);

        PlantingRecord testPlantingRecordThree = new PlantingRecord();
        testPlantingRecordThree.setSeedDate(seedDateThree);
        testPlantingRecordThree.setHarvestDate(harvestDateThree);
        testPlantingRecordThree.setParcel(testParcelSeven);
        testPlantingRecordThree.setCrop(cropService.find(1));
        testPlantingRecordThree.setStatus(statusService.findFinishedStatus());
        testPlantingRecordThree.setCropIrrigationWaterNeed("-");

        Calendar seedDateFour = Calendar.getInstance();
        seedDateFour.set(Calendar.YEAR, 2020);
        seedDateFour.set(Calendar.MONTH, APRIL);
        seedDateFour.set(Calendar.DAY_OF_MONTH, 4);

        Calendar harvestDateFour = Calendar.getInstance();
        harvestDateFour.set(Calendar.YEAR, 2020);
        harvestDateFour.set(Calendar.MONTH, APRIL);
        harvestDateFour.set(Calendar.DAY_OF_MONTH, 25);

        PlantingRecord testPlantingRecordFour = new PlantingRecord();
        testPlantingRecordFour.setSeedDate(seedDateFour);
        testPlantingRecordFour.setHarvestDate(harvestDateFour);
        testPlantingRecordFour.setParcel(testParcelSeven);
        testPlantingRecordFour.setCrop(cropService.find(1));
        testPlantingRecordFour.setStatus(statusService.findFinishedStatus());
        testPlantingRecordFour.setCropIrrigationWaterNeed("-");

        entityManager.getTransaction().begin();
        testPlantingRecordOne = plantingRecordService.create(testPlantingRecordOne);
        testPlantingRecordTwo = plantingRecordService.create(testPlantingRecordTwo);
        testPlantingRecordThree = plantingRecordService.create(testPlantingRecordThree);
        testPlantingRecordFour = plantingRecordService.create(testPlantingRecordFour);
        entityManager.getTransaction().commit();

        plantingRecords.add(testPlantingRecordOne);
        plantingRecords.add(testPlantingRecordTwo);
        plantingRecords.add(testPlantingRecordThree);
        plantingRecords.add(testPlantingRecordFour);

        /*
         * Seccion de prueba
         */
        System.out.println("# Seccion de prueba");
        System.out.println("Periodo dentro del cual se calculara la cantidad de dias en los que la parcela con ID = " + testParcelSeven.getId() + " NO tuvo ningun cultivo");
        System.out.println("plantado:");
        System.out.println("Fecha desde: " + UtilDate.formatDate(dateFrom));
        System.out.println("Fecha hasta: " + UtilDate.formatDate(dateUntil));
        System.out.println();

        System.out.println("Datos de los registros de plantacion de prueba:");
        System.out.println("Fecha de siembra del registro de plantacion uno: " + UtilDate.formatDate(testPlantingRecordOne.getSeedDate()));
        System.out.println("Fecha de cosecha del registro de plantacion uno: " + UtilDate.formatDate(testPlantingRecordOne.getHarvestDate()));
        System.out.println("ID de la parcela a la que pertenece el registro de plantacion uno: " + testPlantingRecordOne.getParcel().getId());
        System.out.println();

        System.out.println("Fecha de siembra del registro de plantacion dos: " + UtilDate.formatDate(testPlantingRecordTwo.getSeedDate()));
        System.out.println("Fecha de cosecha del registro de plantacion dos: " + UtilDate.formatDate(testPlantingRecordTwo.getHarvestDate()));
        System.out.println("ID de la parcela a la que pertenece el registro de plantacion dos: " + testPlantingRecordTwo.getParcel().getId());
        System.out.println();

        System.out.println("Fecha de siembra del registro de plantacion tres: " + UtilDate.formatDate(testPlantingRecordThree.getSeedDate()));
        System.out.println("Fecha de cosecha del registro de plantacion tres: " + UtilDate.formatDate(testPlantingRecordThree.getHarvestDate()));
        System.out.println("ID de la parcela a la que pertenece el registro de plantacion tres: " + testPlantingRecordThree.getParcel().getId());
        System.out.println();

        System.out.println("Fecha de siembra del registro de plantacion cuatro: " + UtilDate.formatDate(testPlantingRecordFour.getSeedDate()));
        System.out.println("Fecha de cosecha del registro de plantacion cuatro: " + UtilDate.formatDate(testPlantingRecordFour.getHarvestDate()));
        System.out.println("ID de la parcela a la que pertenece el registro de plantacion cuatro: " + testPlantingRecordFour.getParcel().getId());
        System.out.println();

        int expectedResult = 11;
        int result = plantingRecordService.calculateDaysWithoutCrops(testParcelSeven.getId(), dateFrom, dateUntil);

        System.out.println("Resultado esperado: " + expectedResult);
        System.out.println("Resultado devuelto por el metodo calculateDaysWithoutCrops(): " + result);
        System.out.println();

        assertTrue(expectedResult == result);

        System.out.println("- Prueba pasada satisfactoriamente");
        System.out.println();
    }

    @Test
    public void testEight() {
        System.out.println("******************************* Prueba ocho del metodo calculateDaysWithoutCrops *******************************");
        printDescriptionMethodToTest();
        System.out.println();
        System.out.println("# Descripcion de la prueba unitaria");
        System.out.println("En esta prueba unitaria para demostrar el correcto funcionamiento del metodo calculateDaysWithoutCrops() se utilizan");
        System.out.println("cuatro registros de plantacion.");
        System.out.println("- Entre la fecha de cosecha del registro de plantacion uno y la fecha de siembra del registro de plantacion dos");
        System.out.println("NO hay ningun dia de diferencia porque la segunda es el dia inmediatamente siguiente a la primera.");
        System.out.println("- Entre la fecha de cosecha del registro de plantacion dos y la fecha de siembra del registro de plantacion tres");
        System.out.println("hay 366 dias de diferencia.");
        System.out.println("- Entre la fecha de cosecha del registro de plantacion tres y la fecha de siembra del registro de plantacion cuatro");
        System.out.println("hay 365 dias de diferencia.");
        System.out.println();
        System.out.println("La fecha desde esta 365 dias antes de la fecha de siembra del registro de plantacion uno y la fecha hasta esta 367 dias");
        System.out.println("despues de la fecha de cosecha del registro de plantacion cuatro.");
        System.out.println();
        System.out.println("Por lo tanto, el metodo calculateDaysWithoutCrops() debe retornar 1463 como la cantidad de dias en los que una parcela");
        System.out.println("NO tuvo ningun cultivo plantado.");
        System.out.println();

        /*
         * Fecha desde y fecha hasta para la prueba
         * unitaria
         */
        Calendar dateFrom = Calendar.getInstance();
        dateFrom.set(Calendar.YEAR, 2017);
        dateFrom.set(Calendar.MONTH, JANUARY);
        dateFrom.set(Calendar.DAY_OF_MONTH, 1);

        Calendar dateUntil = Calendar.getInstance();
        dateUntil.set(Calendar.YEAR, 2025);
        dateUntil.set(Calendar.MONTH, JANUARY);
        dateUntil.set(Calendar.DAY_OF_MONTH, 1);

        /*
         * Creacion y persistencia de un registro de
         * plantacion de prueba
         */
        Calendar seedDateOne = Calendar.getInstance();
        seedDateOne.set(Calendar.YEAR, 2018);
        seedDateOne.set(Calendar.MONTH, JANUARY);
        seedDateOne.set(Calendar.DAY_OF_MONTH, 1);

        Calendar harvestDateOne = Calendar.getInstance();
        harvestDateOne.set(Calendar.YEAR, 2019);
        harvestDateOne.set(Calendar.MONTH, JUNE);
        harvestDateOne.set(Calendar.DAY_OF_MONTH, 12);

        PlantingRecord testPlantingRecordOne = new PlantingRecord();
        testPlantingRecordOne.setSeedDate(seedDateOne);
        testPlantingRecordOne.setHarvestDate(harvestDateOne);
        testPlantingRecordOne.setParcel(testParcelEight);
        testPlantingRecordOne.setCrop(cropService.find(1));
        testPlantingRecordOne.setStatus(statusService.findFinishedStatus());
        testPlantingRecordOne.setCropIrrigationWaterNeed("-");

        Calendar seedDateTwo = Calendar.getInstance();
        seedDateTwo.set(Calendar.YEAR, 2019);
        seedDateTwo.set(Calendar.MONTH, JUNE);
        seedDateTwo.set(Calendar.DAY_OF_MONTH, 13);

        Calendar harvestDateTwo = Calendar.getInstance();
        harvestDateTwo.set(Calendar.YEAR, 2019);
        harvestDateTwo.set(Calendar.MONTH, DECEMBER);
        harvestDateTwo.set(Calendar.DAY_OF_MONTH, 31);

        PlantingRecord testPlantingRecordTwo = new PlantingRecord();
        testPlantingRecordTwo.setSeedDate(seedDateTwo);
        testPlantingRecordTwo.setHarvestDate(harvestDateTwo);
        testPlantingRecordTwo.setParcel(testParcelEight);
        testPlantingRecordTwo.setCrop(cropService.find(1));
        testPlantingRecordTwo.setStatus(statusService.findFinishedStatus());
        testPlantingRecordTwo.setCropIrrigationWaterNeed("-");

        Calendar seedDateThree = Calendar.getInstance();
        seedDateThree.set(Calendar.YEAR, 2021);
        seedDateThree.set(Calendar.MONTH, JANUARY);
        seedDateThree.set(Calendar.DAY_OF_MONTH, 1);

        Calendar harvestDateThree = Calendar.getInstance();
        harvestDateThree.set(Calendar.YEAR, 2021);
        harvestDateThree.set(Calendar.MONTH, DECEMBER);
        harvestDateThree.set(Calendar.DAY_OF_MONTH, 31);

        PlantingRecord testPlantingRecordThree = new PlantingRecord();
        testPlantingRecordThree.setSeedDate(seedDateThree);
        testPlantingRecordThree.setHarvestDate(harvestDateThree);
        testPlantingRecordThree.setParcel(testParcelEight);
        testPlantingRecordThree.setCrop(cropService.find(1));
        testPlantingRecordThree.setStatus(statusService.findFinishedStatus());
        testPlantingRecordThree.setCropIrrigationWaterNeed("-");

        Calendar seedDateFour = Calendar.getInstance();
        seedDateFour.set(Calendar.YEAR, 2023);
        seedDateFour.set(Calendar.MONTH, JANUARY);
        seedDateFour.set(Calendar.DAY_OF_MONTH, 1);

        Calendar harvestDateFour = Calendar.getInstance();
        harvestDateFour.set(Calendar.YEAR, 2023);
        harvestDateFour.set(Calendar.MONTH, DECEMBER);
        harvestDateFour.set(Calendar.DAY_OF_MONTH, 31);

        PlantingRecord testPlantingRecordFour = new PlantingRecord();
        testPlantingRecordFour.setSeedDate(seedDateFour);
        testPlantingRecordFour.setHarvestDate(harvestDateFour);
        testPlantingRecordFour.setParcel(testParcelEight);
        testPlantingRecordFour.setCrop(cropService.find(1));
        testPlantingRecordFour.setStatus(statusService.findFinishedStatus());
        testPlantingRecordFour.setCropIrrigationWaterNeed("-");

        entityManager.getTransaction().begin();
        testPlantingRecordOne = plantingRecordService.create(testPlantingRecordOne);
        testPlantingRecordTwo = plantingRecordService.create(testPlantingRecordTwo);
        testPlantingRecordThree = plantingRecordService.create(testPlantingRecordThree);
        testPlantingRecordFour = plantingRecordService.create(testPlantingRecordFour);
        entityManager.getTransaction().commit();

        plantingRecords.add(testPlantingRecordOne);
        plantingRecords.add(testPlantingRecordTwo);
        plantingRecords.add(testPlantingRecordThree);
        plantingRecords.add(testPlantingRecordFour);

        /*
         * Seccion de prueba
         */
        System.out.println("# Seccion de prueba");
        System.out.println("Periodo dentro del cual se calculara la cantidad de dias en los que la parcela con ID = " + testParcelEight.getId() + " NO tuvo ningun cultivo");
        System.out.println("plantado:");
        System.out.println("Fecha desde: " + UtilDate.formatDate(dateFrom));
        System.out.println("Fecha hasta: " + UtilDate.formatDate(dateUntil));
        System.out.println();

        System.out.println("Datos de los registros de plantacion de prueba:");
        System.out.println("Fecha de siembra del registro de plantacion uno: " + UtilDate.formatDate(testPlantingRecordOne.getSeedDate()));
        System.out.println("Fecha de cosecha del registro de plantacion uno: " + UtilDate.formatDate(testPlantingRecordOne.getHarvestDate()));
        System.out.println("ID de la parcela a la que pertenece el registro de plantacion uno: " + testPlantingRecordOne.getParcel().getId());
        System.out.println();

        System.out.println("Fecha de siembra del registro de plantacion dos: " + UtilDate.formatDate(testPlantingRecordTwo.getSeedDate()));
        System.out.println("Fecha de cosecha del registro de plantacion dos: " + UtilDate.formatDate(testPlantingRecordTwo.getHarvestDate()));
        System.out.println("ID de la parcela a la que pertenece el registro de plantacion dos: " + testPlantingRecordTwo.getParcel().getId());
        System.out.println();

        System.out.println("Fecha de siembra del registro de plantacion tres: " + UtilDate.formatDate(testPlantingRecordThree.getSeedDate()));
        System.out.println("Fecha de cosecha del registro de plantacion tres: " + UtilDate.formatDate(testPlantingRecordThree.getHarvestDate()));
        System.out.println("ID de la parcela a la que pertenece el registro de plantacion tres: " + testPlantingRecordThree.getParcel().getId());
        System.out.println();

        System.out.println("Fecha de siembra del registro de plantacion cuatro: " + UtilDate.formatDate(testPlantingRecordFour.getSeedDate()));
        System.out.println("Fecha de cosecha del registro de plantacion cuatro: " + UtilDate.formatDate(testPlantingRecordFour.getHarvestDate()));
        System.out.println("ID de la parcela a la que pertenece el registro de plantacion cuatro: " + testPlantingRecordFour.getParcel().getId());
        System.out.println();

        int expectedResult = 1463;
        int result = plantingRecordService.calculateDaysWithoutCrops(testParcelEight.getId(), dateFrom, dateUntil);

        System.out.println("Resultado esperado: " + expectedResult);
        System.out.println("Resultado devuelto por el metodo calculateDaysWithoutCrops(): " + result);
        System.out.println();

        assertTrue(expectedResult == result);

        System.out.println("- Prueba pasada satisfactoriamente");
        System.out.println();
    }

    @Test
    public void testNine() {
        System.out.println("******************************* Prueba nueve del metodo calculateDaysWithoutCrops *******************************");
        printDescriptionMethodToTest();
        System.out.println();
        System.out.println("# Descripcion de la prueba unitaria");
        System.out.println("En esta prueba unitaria para demostrar el correcto funcionamiento del metodo calculateDaysWithoutCrops() se utiliza");
        System.out.println("una parcela que no tiene registros de plantacion finalizados en el periodo definido por una fecha desde y una fecha");
        System.out.println("hasta. Por lo tanto, el metodo calculateDaysWithoutCrops() debe retornar -1.");
        System.out.println();

        /*
         * Fecha desde y fecha hasta para la prueba
         * unitaria
         */
        Calendar dateFrom = Calendar.getInstance();
        dateFrom.set(Calendar.YEAR, 2017);
        dateFrom.set(Calendar.MONTH, JANUARY);
        dateFrom.set(Calendar.DAY_OF_MONTH, 1);

        Calendar dateUntil = Calendar.getInstance();
        dateUntil.set(Calendar.YEAR, 2017);
        dateUntil.set(Calendar.MONTH, JANUARY);
        dateUntil.set(Calendar.DAY_OF_MONTH, 31);

        /*
         * Creacion y persistencia de un registro de
         * plantacion de prueba
         */
        Calendar seedDate = Calendar.getInstance();
        seedDate.set(Calendar.YEAR, 2022);
        seedDate.set(Calendar.MONTH, JANUARY);
        seedDate.set(Calendar.DAY_OF_MONTH, 1);

        Calendar harvestDate = Calendar.getInstance();
        harvestDate.set(Calendar.YEAR, 2022);
        harvestDate.set(Calendar.MONTH, JANUARY);
        harvestDate.set(Calendar.DAY_OF_MONTH, 31);

        PlantingRecord testPlantingRecord = new PlantingRecord();
        testPlantingRecord.setSeedDate(seedDate);
        testPlantingRecord.setHarvestDate(harvestDate);
        testPlantingRecord.setParcel(testParcelNine);
        testPlantingRecord.setCrop(cropService.find(1));
        testPlantingRecord.setStatus(statusService.findFinishedStatus());
        testPlantingRecord.setCropIrrigationWaterNeed("-");

        entityManager.getTransaction().begin();
        testPlantingRecord = plantingRecordService.create(testPlantingRecord);
        entityManager.getTransaction().commit();

        plantingRecords.add(testPlantingRecord);

        /*
         * Seccion de prueba
         */
        System.out.println("# Seccion de prueba");
        System.out.println("Periodo dentro del cual se calculara la cantidad de dias en los que la parcela con ID = " + testParcelNine.getId() + " NO tuvo ningun cultivo");
        System.out.println("plantado:");
        System.out.println("Fecha desde: " + UtilDate.formatDate(dateFrom));
        System.out.println("Fecha hasta: " + UtilDate.formatDate(dateUntil));
        System.out.println();

        System.out.println("Datos del registro de plantacion de prueba:");
        System.out.println("Fecha de siembra: " + UtilDate.formatDate(testPlantingRecord.getSeedDate()));
        System.out.println("Fecha de cosecha: " + UtilDate.formatDate(testPlantingRecord.getHarvestDate()));
        System.out.println("ID de la parcela a la que pertenece el registro de plantacion: " + testPlantingRecord.getParcel().getId());
        System.out.println();

        int expectedResult = -1;
        int result = plantingRecordService.calculateDaysWithoutCrops(testParcelNine.getId(), dateFrom, dateUntil);

        System.out.println("Resultado esperado: " + expectedResult);
        System.out.println("Resultado devuelto por el metodo calculateDaysWithoutCrops(): " + result);
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
        System.out.println("El metodo calculateDaysWithoutCrops() de la clase PlantingRecordService calcula la cantidad de dias en los que");
        System.out.println("una parcela NO tuvo ningun cultivo plantado cuando se lo invoca para una parcela que tiene registros de plantacion");
        System.out.println("finalizados en un periodo definido por dos fechas. En cambio, retorna -1 si se lo invoca para una parcela que NO");
        System.out.println("registros de plantacion finalizados en un periodo definido por dos fechas.");
    }

}