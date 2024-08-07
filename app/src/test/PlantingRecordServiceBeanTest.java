import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.ArrayList;
import java.util.Collection;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import model.PlantingRecord;
import model.PlantingRecordStatus;
import model.TypeCrop;
import model.Parcel;
import model.User;
import model.Crop;
import model.GeographicLocation;
import model.Option;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import stateless.ParcelServiceBean;
import stateless.PlantingRecordServiceBean;
import stateless.PlantingRecordStatusServiceBean;
import stateless.TypeCropServiceBean;
import stateless.UserServiceBean;
import stateless.CropServiceBean;
import stateless.GeographicLocationServiceBean;
import stateless.OptionServiceBean;
import util.UtilDate;

public class PlantingRecordServiceBeanTest {

        private static EntityManager entityManager;
        private static EntityManagerFactory entityManagerFactory;

        private static ParcelServiceBean parcelService;
        private static GeographicLocationServiceBean geographicLocationService;
        private static UserServiceBean userService;
        private static CropServiceBean cropService;
        private static PlantingRecordServiceBean plantingRecordService;
        private static PlantingRecordStatusServiceBean plantingRecordStatusService;
        private static TypeCropServiceBean typeCropService;
        private static OptionServiceBean optionService;

        private static User testUser;
        private static Parcel testParcel;
        private static Crop testCrop;
        private static PlantingRecordStatus testStatus;

        private static Collection<Parcel> parcels;
        private static Collection<GeographicLocation> geographicLocations;
        private static Collection<PlantingRecord> plantingRecords;
        private static Collection<PlantingRecordStatus> plantingRecordStatuses;
        private static Collection<User> users;
        private static Collection<Crop> crops;
        private static Collection<TypeCrop> typesCrop;
        private static Collection<Option> options;

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

                geographicLocationService = new GeographicLocationServiceBean();
                geographicLocationService.setEntityManager(entityManager);

                userService = new UserServiceBean();
                userService.setEntityManager(entityManager);

                cropService = new CropServiceBean();
                cropService.setEntityManager(entityManager);

                plantingRecordService = new PlantingRecordServiceBean();
                plantingRecordService.setEntityManager(entityManager);

                plantingRecordStatusService = new PlantingRecordStatusServiceBean();
                plantingRecordStatusService.setEntityManager(entityManager);

                typeCropService = new TypeCropServiceBean();
                typeCropService.setEntityManager(entityManager);

                optionService = new OptionServiceBean();
                optionService.setEntityManager(entityManager);

                parcels = new ArrayList<>();
                plantingRecords = new ArrayList<>();
                plantingRecordStatuses = new ArrayList<>();
                users = new ArrayList<>();
                crops = new ArrayList<>();
                options = new ArrayList<>();
                geographicLocations = new ArrayList<>();
                typesCrop = new ArrayList<>();

                /*
                 * Creacion y persistencia de un usuario de prueba
                 */
                testUser = new User();
                testUser.setUsername("testOneCheckByDate");
                testUser.setName("Peter");
                testUser.setLastName("Doe");
                testUser.setEmail("testOneCheckByDate@eservice.com");

                entityManager.getTransaction().begin();
                testUser = userService.create(testUser);
                entityManager.getTransaction().commit();

                /*
                 * Creacion y persistencia de una ubicacion geografica para
                 * una parcela de prueba
                 */
                GeographicLocation testGeographicLocation = new GeographicLocation();
                testGeographicLocation.setLatitude(1);
                testGeographicLocation.setLongitude(1);

                entityManager.getTransaction().begin();
                testGeographicLocation = geographicLocationService.create(testGeographicLocation);
                entityManager.getTransaction().commit();

                /*
                 * Creacion y persistencia de una opcion para una parcela
                 * de prueba
                 */
                entityManager.getTransaction().begin();
                Option testParcelOption = optionService.create();
                entityManager.getTransaction().commit();

                /*
                 * Creacion y persistencia de una opcion para una parcela
                 * de prueba
                 */
                testParcel = new Parcel();
                testParcel.setName("Erie");
                testParcel.setHectares(2);
                testParcel.setOption(testParcelOption);
                testParcel.setGeographicLocation(testGeographicLocation);
                testParcel.setUser(testUser);

                entityManager.getTransaction().begin();
                testParcel = parcelService.create(testParcel);
                entityManager.getTransaction().commit();

                /*
                 * Creacion y persistencia de un tipo de cultivo de prueba
                 */
                TypeCrop testTypeCrop = new TypeCrop();
                testTypeCrop.setName("test type crop name");

                entityManager.getTransaction().begin();
                testTypeCrop = typeCropService.create(testTypeCrop);
                entityManager.getTransaction().commit();

                /*
                 * Creacion y persistencia de un cultivo de prueba
                 */
                testCrop = new Crop();
                testCrop.setName("test crop");
                testCrop.setInitialStage(1);
                testCrop.setDevelopmentStage(1);
                testCrop.setMiddleStage(1);
                testCrop.setFinalStage(1);
                testCrop.setInitialKc(1);
                testCrop.setMiddleKc(1);
                testCrop.setFinalKc(1);
                testCrop.setLifeCycle(1);
                testCrop.setActive(true);
                testCrop.setTypeCrop(testTypeCrop);

                entityManager.getTransaction().begin();
                testCrop = cropService.create(testCrop);
                entityManager.getTransaction().commit();

                /*
                 * Creacion y persistencia de un estado de prueba
                 */
                testStatus = new PlantingRecordStatus();
                testStatus.setName("test status");
                testStatus.setDescription("test status description");

                entityManager.getTransaction().begin();
                entityManager.persist(testStatus);
                entityManager.getTransaction().commit();

                /*
                 * Se añaden los datos creados a una coleccion para
                 * su respectiva eliminacion de la base de datos
                 * subyacente. Esto se hace con el fin de hacer que
                 * la base de datos quede en su estado original.
                 */
                geographicLocations.add(testGeographicLocation);
                options.add(testParcelOption);
                parcels.add(testParcel);
                users.add(testUser);
                crops.add(testCrop);
                typesCrop.add(testTypeCrop);
                plantingRecordStatuses.add(testStatus);
        }

        @Test
        public void testOneCheckByDate() {
                System.out.println("*********************************** Prueba uno del metodo checkByDate() ***********************************");
                printDescriptionMethodToTest();
                System.out.println();
                System.out.println("# Descripcion de la prueba unitaria");
                System.out.println("En esta prueba se demuestra el correcto funcionamiento de este metodo utilizando una fecha que esta en el periodo");
                System.out.println("definido por la fecha de siembra y la fecha de cosecha de un registro de plantacion de prueba. En este caso, el");
                System.out.println("valor devuelto por checkByDate() es true.");
                System.out.println();

                /*
                 * Fechas para el registro de plantacion de prueba
                 */
                Calendar seedDate = Calendar.getInstance();
                seedDate.set(Calendar.YEAR, 2018);
                seedDate.set(Calendar.MONTH, MARCH);
                seedDate.set(Calendar.DAY_OF_MONTH, 5);

                Calendar harvestDate = Calendar.getInstance();
                harvestDate.set(Calendar.YEAR, 2018);
                harvestDate.set(Calendar.MONTH, JULY);
                harvestDate.set(Calendar.DAY_OF_MONTH, 24);

                /*
                 * Creacion y persistencia de un registro de plantacion de prueba
                 */
                PlantingRecord newPlantingRecord = new PlantingRecord();
                newPlantingRecord.setSeedDate(seedDate);
                newPlantingRecord.setHarvestDate(harvestDate);
                newPlantingRecord.setParcel(testParcel);
                newPlantingRecord.setCrop(testCrop);
                newPlantingRecord.setCropIrrigationWaterNeed("0");
                newPlantingRecord.setStatus(testStatus);

                entityManager.getTransaction().begin();
                newPlantingRecord = plantingRecordService.create(newPlantingRecord);
                entityManager.getTransaction().commit();

                plantingRecords.add(newPlantingRecord);

                /*
                 * Seccion de prueba
                 */
                Calendar testDate = Calendar.getInstance();
                testDate.set(Calendar.YEAR, 2018);
                testDate.set(Calendar.MONTH, APRIL);
                testDate.set(Calendar.DAY_OF_MONTH, 12);

                System.out.println("# Ejecucion de la prueba unitaria");
                System.out.println("Fecha de siembra del registro de plantacion de prueba: " + UtilDate.formatDate(newPlantingRecord.getSeedDate()));
                System.out.println("Fecha de cosecha del registro de plantacion de prueba: " + UtilDate.formatDate(newPlantingRecord.getHarvestDate()));
                System.out.println();
                System.out.println("Fecha de prueba: " + UtilDate.formatDate(testDate));
                System.out.println("La fecha de prueba esta en el periodo definido por la fecha de siembra y la fecha de cosecha del registro de");
                System.out.println("plantacion de prueba.");
                System.out.println();

                boolean expectedResult = true;
                boolean result = plantingRecordService.checkByDate(testUser.getId(), testParcel, testDate);

                System.out.println("* Valor que se espera que devuelva el metodo checkByDate(): " + expectedResult);
                System.out.println("* Valor devuelto por el metodo checkByDate(): " + result);
                System.out.println();

                assertTrue(expectedResult == result);

                System.out.println("- Prueba pasada satisfactoriamente");
        }

        @Test
        public void testTwoCheckByDate() {
                System.out.println("*********************************** Prueba dos del metodo checkByDate() ***********************************");
                printDescriptionMethodToTest();
                System.out.println();
                System.out.println("# Descripcion de la prueba unitaria");
                System.out.println("En esta prueba se demuestra el correcto funcionamiento de este metodo utilizando una fecha que esta en el periodo");
                System.out.println("definido por la fecha de siembra y la fecha de cosecha de un registro de plantacion de prueba. La fecha de prueba");
                System.out.println("es igual a la fecha de siembra del registro de plantacion de prueba. Este caso es un caso de borde, y el valor");
                System.out.println("devuelto por checkByDate() es true.");
                System.out.println();

                /*
                 * Fechas para el registro de plantacion de prueba
                 */
                Calendar seedDate = Calendar.getInstance();
                seedDate.set(Calendar.YEAR, 2019);
                seedDate.set(Calendar.MONTH, MARCH);
                seedDate.set(Calendar.DAY_OF_MONTH, 5);

                Calendar harvestDate = Calendar.getInstance();
                harvestDate.set(Calendar.YEAR, 2019);
                harvestDate.set(Calendar.MONTH, JULY);
                harvestDate.set(Calendar.DAY_OF_MONTH, 24);

                /*
                 * Creacion y persistencia de un registro de plantacion de prueba
                 */
                PlantingRecord newPlantingRecord = new PlantingRecord();
                newPlantingRecord.setSeedDate(seedDate);
                newPlantingRecord.setHarvestDate(harvestDate);
                newPlantingRecord.setParcel(testParcel);
                newPlantingRecord.setCrop(testCrop);
                newPlantingRecord.setCropIrrigationWaterNeed("0");
                newPlantingRecord.setStatus(testStatus);

                entityManager.getTransaction().begin();
                newPlantingRecord = plantingRecordService.create(newPlantingRecord);
                entityManager.getTransaction().commit();

                plantingRecords.add(newPlantingRecord);

                /*
                 * Seccion de prueba
                 */
                Calendar testDate = Calendar.getInstance();
                testDate.set(Calendar.YEAR, 2019);
                testDate.set(Calendar.MONTH, MARCH);
                testDate.set(Calendar.DAY_OF_MONTH, 5);

                System.out.println("# Ejecucion de la prueba unitaria");
                System.out.println("Fecha de siembra del registro de plantacion de prueba: " + UtilDate.formatDate(newPlantingRecord.getSeedDate()));
                System.out.println("Fecha de cosecha del registro de plantacion de prueba: " + UtilDate.formatDate(newPlantingRecord.getHarvestDate()));
                System.out.println();
                System.out.println("Fecha de prueba: " + UtilDate.formatDate(testDate));
                System.out.println("La fecha de prueba esta en el periodo definido por la fecha de siembra y la fecha de cosecha del registro de");
                System.out.println("plantacion de prueba.");
                System.out.println();

                boolean expectedResult = true;
                boolean result = plantingRecordService.checkByDate(testUser.getId(), testParcel, testDate);

                System.out.println("* Valor que se espera que devuelva el metodo checkByDate(): " + expectedResult);
                System.out.println("* Valor devuelto por el metodo checkByDate(): " + result);
                System.out.println();

                assertTrue(expectedResult == result);

                System.out.println("- Prueba pasada satisfactoriamente");
        }

        @Test
        public void testThreeCheckByDate() {
                System.out.println("*********************************** Prueba tres del metodo checkByDate() ***********************************");
                printDescriptionMethodToTest();
                System.out.println();
                System.out.println("# Descripcion de la prueba unitaria");
                System.out.println("En esta prueba se demuestra el correcto funcionamiento de este metodo utilizando una fecha que esta en el periodo");
                System.out.println("definido por la fecha de siembra y la fecha de cosecha de un registro de plantacion de prueba. La fecha de prueba");
                System.out.println("es igual a la fecha de cosecha del registro de plantacion de prueba. Este caso es un caso de borde, y el valor");
                System.out.println("devuelto por checkByDate() es true.");
                System.out.println();

                /*
                 * Fechas para el registro de plantacion de prueba
                 */
                Calendar seedDate = Calendar.getInstance();
                seedDate.set(Calendar.YEAR, 2020);
                seedDate.set(Calendar.MONTH, MARCH);
                seedDate.set(Calendar.DAY_OF_MONTH, 5);

                Calendar harvestDate = Calendar.getInstance();
                harvestDate.set(Calendar.YEAR, 2020);
                harvestDate.set(Calendar.MONTH, JULY);
                harvestDate.set(Calendar.DAY_OF_MONTH, 24);

                /*
                 * Creacion y persistencia de un registro de plantacion de prueba
                 */
                PlantingRecord newPlantingRecord = new PlantingRecord();
                newPlantingRecord.setSeedDate(seedDate);
                newPlantingRecord.setHarvestDate(harvestDate);
                newPlantingRecord.setParcel(testParcel);
                newPlantingRecord.setCrop(testCrop);
                newPlantingRecord.setCropIrrigationWaterNeed("0");
                newPlantingRecord.setStatus(testStatus);

                entityManager.getTransaction().begin();
                newPlantingRecord = plantingRecordService.create(newPlantingRecord);
                entityManager.getTransaction().commit();

                plantingRecords.add(newPlantingRecord);

                /*
                 * Seccion de prueba
                 */
                Calendar testDate = Calendar.getInstance();
                testDate.set(Calendar.YEAR, 2020);
                testDate.set(Calendar.MONTH, JULY);
                testDate.set(Calendar.DAY_OF_MONTH, 24);

                System.out.println("# Ejecucion de la prueba unitaria");
                System.out.println("Fecha de siembra del registro de plantacion de prueba: " + UtilDate.formatDate(newPlantingRecord.getSeedDate()));
                System.out.println("Fecha de cosecha del registro de plantacion de prueba: " + UtilDate.formatDate(newPlantingRecord.getHarvestDate()));
                System.out.println();
                System.out.println("Fecha de prueba: " + UtilDate.formatDate(testDate));
                System.out.println("La fecha de prueba esta en el periodo definido por la fecha de siembra y la fecha de cosecha del registro de");
                System.out.println("plantacion de prueba.");
                System.out.println();

                boolean expectedResult = true;
                boolean result = plantingRecordService.checkByDate(testUser.getId(), testParcel, testDate);

                System.out.println("* Valor que se espera que devuelva el metodo checkByDate(): " + expectedResult);
                System.out.println("* Valor devuelto por el metodo checkByDate(): " + result);
                System.out.println();

                assertTrue(expectedResult == result);

                System.out.println("- Prueba pasada satisfactoriamente");
        }

        @Test
        public void testFourCheckByDate() {
                System.out.println("*********************************** Prueba cuatro del metodo checkByDate() ***********************************");
                printDescriptionMethodToTest();
                System.out.println();
                System.out.println("# Descripcion de la prueba unitaria");
                System.out.println("En esta prueba se demuestra el correcto funcionamiento de este metodo utilizando una fecha que NO esta en el periodo");
                System.out.println("definido por la fecha de siembra y la fecha de cosecha de un registro de plantacion de prueba. En este caso, el");
                System.out.println("valor devuelto por checkByDate() es false.");
                System.out.println();

                /*
                 * Fechas para el registro de plantacion de prueba
                 */
                Calendar seedDate = Calendar.getInstance();
                seedDate.set(Calendar.YEAR, 2021);
                seedDate.set(Calendar.MONTH, JUNE);
                seedDate.set(Calendar.DAY_OF_MONTH, 1);

                Calendar harvestDate = Calendar.getInstance();
                harvestDate.set(Calendar.YEAR, 2021);
                harvestDate.set(Calendar.MONTH, SEPTEMBER);
                harvestDate.set(Calendar.DAY_OF_MONTH, 20);

                /*
                 * Creacion y persistencia de un registro de plantacion de prueba
                 */
                PlantingRecord newPlantingRecord = new PlantingRecord();
                newPlantingRecord.setSeedDate(seedDate);
                newPlantingRecord.setHarvestDate(harvestDate);
                newPlantingRecord.setParcel(testParcel);
                newPlantingRecord.setCrop(testCrop);
                newPlantingRecord.setCropIrrigationWaterNeed("0");
                newPlantingRecord.setStatus(testStatus);

                entityManager.getTransaction().begin();
                newPlantingRecord = plantingRecordService.create(newPlantingRecord);
                entityManager.getTransaction().commit();

                plantingRecords.add(newPlantingRecord);

                /*
                 * Seccion de prueba
                 */
                Calendar testDate = Calendar.getInstance();
                testDate.set(Calendar.YEAR, 2021);
                testDate.set(Calendar.MONTH, MARCH);
                testDate.set(Calendar.DAY_OF_MONTH, 20);

                System.out.println("# Ejecucion de la prueba unitaria");
                System.out.println("Fecha de siembra del registro de plantacion de prueba: " + UtilDate.formatDate(newPlantingRecord.getSeedDate()));
                System.out.println("Fecha de cosecha del registro de plantacion de prueba: " + UtilDate.formatDate(newPlantingRecord.getHarvestDate()));
                System.out.println();
                System.out.println("Fecha de prueba: " + UtilDate.formatDate(testDate));
                System.out.println("La fecha de prueba NO esta en el periodo definido por la fecha de siembra y la fecha de cosecha del registro de");
                System.out.println("plantacion de prueba.");
                System.out.println();

                boolean expectedResult = false;
                boolean result = plantingRecordService.checkByDate(testUser.getId(), testParcel, testDate);

                System.out.println("* Valor que se espera que devuelva el metodo checkByDate(): " + expectedResult);
                System.out.println("* Valor devuelto por el metodo checkByDate(): " + result);
                System.out.println();

                assertTrue(expectedResult == result);

                System.out.println("- Prueba pasada satisfactoriamente");
        }

        @Test
        public void testFiveCheckByDate() {
                System.out.println("*********************************** Prueba cinco del metodo checkByDate() ***********************************");
                printDescriptionMethodToTest();
                System.out.println();
                System.out.println("# Descripcion de la prueba unitaria");
                System.out.println("En esta prueba se demuestra el correcto funcionamiento de este metodo utilizando una fecha que NO esta en el periodo");
                System.out.println("definido por la fecha de siembra y la fecha de cosecha de un registro de plantacion de prueba. La fecha de prueba es");
                System.out.println("igual al dia inmediatamente anterior a la fecha de siembra del registro de plantacion de prueba. Este caso es un");
                System.out.println("caso de borde, y el valor devuelto por checkByDate() es false.");
                System.out.println();

                /*
                 * Fechas para el registro de plantacion de prueba
                 */
                Calendar seedDate = Calendar.getInstance();
                seedDate.set(Calendar.YEAR, 2022);
                seedDate.set(Calendar.MONTH, JUNE);
                seedDate.set(Calendar.DAY_OF_MONTH, 1);

                Calendar harvestDate = Calendar.getInstance();
                harvestDate.set(Calendar.YEAR, 2022);
                harvestDate.set(Calendar.MONTH, SEPTEMBER);
                harvestDate.set(Calendar.DAY_OF_MONTH, 20);

                /*
                 * Creacion y persistencia de un registro de plantacion de prueba
                 */
                PlantingRecord newPlantingRecord = new PlantingRecord();
                newPlantingRecord.setSeedDate(seedDate);
                newPlantingRecord.setHarvestDate(harvestDate);
                newPlantingRecord.setParcel(testParcel);
                newPlantingRecord.setCrop(testCrop);
                newPlantingRecord.setCropIrrigationWaterNeed("0");
                newPlantingRecord.setStatus(testStatus);

                entityManager.getTransaction().begin();
                newPlantingRecord = plantingRecordService.create(newPlantingRecord);
                entityManager.getTransaction().commit();

                plantingRecords.add(newPlantingRecord);

                /*
                 * Seccion de prueba
                 */
                Calendar testDate = Calendar.getInstance();
                testDate.set(Calendar.YEAR, 2022);
                testDate.set(Calendar.MONTH, MAY);
                testDate.set(Calendar.DAY_OF_MONTH, 31);

                System.out.println("# Ejecucion de la prueba unitaria");
                System.out.println("Fecha de siembra del registro de plantacion de prueba: " + UtilDate.formatDate(newPlantingRecord.getSeedDate()));
                System.out.println("Fecha de cosecha del registro de plantacion de prueba: " + UtilDate.formatDate(newPlantingRecord.getHarvestDate()));
                System.out.println();
                System.out.println("Fecha de prueba: " + UtilDate.formatDate(testDate));
                System.out.println("La fecha de prueba NO esta en el periodo definido por la fecha de siembra y la fecha de cosecha del registro de");
                System.out.println("plantacion de prueba.");
                System.out.println();

                boolean expectedResult = false;
                boolean result = plantingRecordService.checkByDate(testUser.getId(), testParcel, testDate);

                System.out.println("* Valor que se espera que devuelva el metodo checkByDate(): " + expectedResult);
                System.out.println("* Valor devuelto por el metodo checkByDate(): " + result);
                System.out.println();

                assertTrue(expectedResult == result);

                System.out.println("- Prueba pasada satisfactoriamente");
        }

        @Test
        public void testSixCheckByDate() {
                System.out.println("*********************************** Prueba seis del metodo checkByDate() ***********************************");
                printDescriptionMethodToTest();
                System.out.println();
                System.out.println("# Descripcion de la prueba unitaria");
                System.out.println("En esta prueba se demuestra el correcto funcionamiento de este metodo utilizando una fecha que NO esta en el periodo");
                System.out.println("definido por la fecha de siembra y la fecha de cosecha de un registro de plantacion de prueba. La fecha de prueba es");
                System.out.println("igual al dia inmediatamente siguiente a la fecha de cosecha del registro de plantacion de prueba. Este caso es un");
                System.out.println("caso de borde, y el valor devuelto por checkByDate() es false.");
                System.out.println();

                /*
                 * Fechas para el registro de plantacion de prueba
                 */
                Calendar seedDate = Calendar.getInstance();
                seedDate.set(Calendar.YEAR, 2023);
                seedDate.set(Calendar.MONTH, JUNE);
                seedDate.set(Calendar.DAY_OF_MONTH, 1);

                Calendar harvestDate = Calendar.getInstance();
                harvestDate.set(Calendar.YEAR, 2023);
                harvestDate.set(Calendar.MONTH, SEPTEMBER);
                harvestDate.set(Calendar.DAY_OF_MONTH, 20);

                /*
                 * Creacion y persistencia de un registro de plantacion de prueba
                 */
                PlantingRecord newPlantingRecord = new PlantingRecord();
                newPlantingRecord.setSeedDate(seedDate);
                newPlantingRecord.setHarvestDate(harvestDate);
                newPlantingRecord.setParcel(testParcel);
                newPlantingRecord.setCrop(testCrop);
                newPlantingRecord.setCropIrrigationWaterNeed("0");
                newPlantingRecord.setStatus(testStatus);

                entityManager.getTransaction().begin();
                newPlantingRecord = plantingRecordService.create(newPlantingRecord);
                entityManager.getTransaction().commit();

                plantingRecords.add(newPlantingRecord);

                /*
                 * Seccion de prueba
                 */
                Calendar testDate = Calendar.getInstance();
                testDate.set(Calendar.YEAR, 2023);
                testDate.set(Calendar.MONTH, SEPTEMBER);
                testDate.set(Calendar.DAY_OF_MONTH, 21);

                System.out.println("# Ejecucion de la prueba unitaria");
                System.out.println("Fecha de siembra del registro de plantacion de prueba: " + UtilDate.formatDate(newPlantingRecord.getSeedDate()));
                System.out.println("Fecha de cosecha del registro de plantacion de prueba: " + UtilDate.formatDate(newPlantingRecord.getHarvestDate()));
                System.out.println();
                System.out.println("Fecha de prueba: " + UtilDate.formatDate(testDate));
                System.out.println("La fecha de prueba NO esta en el periodo definido por la fecha de siembra y la fecha de cosecha del registro de");
                System.out.println("plantacion de prueba.");
                System.out.println();

                boolean expectedResult = false;
                boolean result = plantingRecordService.checkByDate(testUser.getId(), testParcel, testDate);

                System.out.println("* Valor que se espera que devuelva el metodo checkByDate(): " + expectedResult);
                System.out.println("* Valor devuelto por el metodo checkByDate(): " + result);
                System.out.println();

                assertTrue(expectedResult == result);

                System.out.println("- Prueba pasada satisfactoriamente");
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

                for (PlantingRecordStatus currentPlantingRecordStatus : plantingRecordStatuses) {
                        entityManager.remove(currentPlantingRecordStatus);
                }

                for (Crop currentCrop : crops) {
                        cropService.physicallyRemove(currentCrop.getId());
                }

                for (TypeCrop currentTypeCrop : typesCrop) {
                        entityManager.remove(currentTypeCrop);
                }

                for (User currentUser : users) {
                        userService.remove(currentUser.getId());
                }

                for (Parcel currentParcel : parcels) {
                        parcelService.remove(currentParcel.getId());
                }

                for (Option currentOption : options) {
                        optionService.remove(currentOption.getId());
                }

                for (GeographicLocation currentGeographicLocation : geographicLocations) {
                        geographicLocationService.remove(currentGeographicLocation.getId());
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
                System.out.println("El metodo checkByDate() de la clase PlantingRecordRestServlet comprueba si una fecha esta en el periodo definido");
                System.out.println("por la fecha de siembra y la fecha de cosecha de un registro de plantacion de una parcela correspondiente a un");
                System.out.println("usuario. Si una fecha esta en dicho periodo, el metodo checkByDate devuelve true. En caso contrario, false.");
        }

}
