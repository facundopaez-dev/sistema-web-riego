import static org.junit.Assert.*;

import climate.ClimateClient;
import java.util.Calendar;
import java.util.ArrayList;
import java.util.Collection;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import model.ClimateRecord;
import model.Parcel;
import model.User;
import model.Option;
import model.TypePrecipitation;
import model.GeographicLocation;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import stateless.ParcelServiceBean;
import stateless.UserServiceBean;
import stateless.ClimateRecordServiceBean;
import stateless.GeographicLocationServiceBean;
import stateless.OptionServiceBean;
import stateless.TypePrecipitationServiceBean;
import util.UtilDate;

public class ClimateRecordServiceBeanTest {

  private static EntityManager entityManager;
  private static EntityManagerFactory entityManagerFactory;

  private static ParcelServiceBean parcelService;
  private static UserServiceBean userService;
  private static OptionServiceBean optionService;
  private static ClimateRecordServiceBean climateRecordService;
  private static TypePrecipitationServiceBean typePrecipitationService;
  private static GeographicLocationServiceBean geographicLocationService;

  private static Collection<Parcel> parcels;
  private static Collection<ClimateRecord> climateRecords;
  private static Collection<TypePrecipitation> typePrecipitations;
  private static Collection<User> users;
  private static Collection<Option> options;
  private static Collection<GeographicLocation> geographicLocations;

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

    userService = new UserServiceBean();
    userService.setEntityManager(entityManager);

    optionService = new OptionServiceBean();
    optionService.setEntityManager(entityManager);

    climateRecordService = new ClimateRecordServiceBean();
    climateRecordService.setEntityManager(entityManager);

    typePrecipitationService = new TypePrecipitationServiceBean();
    typePrecipitationService.setEntityManager(entityManager);

    geographicLocationService = new GeographicLocationServiceBean();
    geographicLocationService.setEntityManager(entityManager);

    parcels = new ArrayList<>();
    climateRecords = new ArrayList<>();
    typePrecipitations = new ArrayList<>();
    users = new ArrayList<>();
    options = new ArrayList<>();
    geographicLocations = new ArrayList<>();
  }

  @Test
  public void testOneFindAllByParcelIdAndPeriod() {
    System.out.println("******************************** Prueba uno del metodo findAllByParcelIdAndPeriod() ********************************");
    System.out.println("# Descripcion del metodo a probar");
    System.out.println("El metodo findAllByParcelIdAndPeriod de la clase ClimateRecordServiceBean devuelve una coleccion de registros");
    System.out.println("climaticos de una parcela de un usuario que estan en un periodo definido por dos fechas. En el caso en el que una");
    System.out.println("parcela no tiene registros climaticos en un periodo definido por dos fechas, devuelve una coleccion vacia (0");
    System.out.println("elementos).");
    System.out.println();

    Calendar dateFrom = Calendar.getInstance();
    dateFrom.set(Calendar.YEAR, 2023);
    dateFrom.set(Calendar.MONTH, JANUARY);
    dateFrom.set(Calendar.DAY_OF_MONTH, 1);

    Calendar dateUntil = Calendar.getInstance();
    dateUntil.set(Calendar.YEAR, 2023);
    dateUntil.set(Calendar.MONTH, JANUARY);
    dateUntil.set(Calendar.DAY_OF_MONTH, 6);

    System.out.println("# Descripcion de la prueba unitaria");
    System.out.println("Para demostrar el correcto funcionamiento de este metodo se utilizan tres registros climaticos que estan en el");
    System.out.println("periodo [" + UtilDate.formatDate(dateFrom) + ", " + UtilDate.formatDate(dateUntil) + "].");
    System.out.println();
    System.out.println("Con la primera fecha y la segunda fecha del periodo como fecha desde y fecha hasta, respectivamente, el metodo");
    System.out.println("findAllByParcelIdAndPeriod debe devolver una coleccion con tres registros climaticos. Por lo tanto, el tamaño");
    System.out.println("de dicha coleccion debe ser igual a 3.");
    System.out.println();

    /*
     * Creacion y persistencia de una ubicacion geografica
     * para parcelas de prueba
     */
    GeographicLocation testGeographicLocation = new GeographicLocation();
    testGeographicLocation.setLatitude(1);
    testGeographicLocation.setLongitude(1);

    entityManager.getTransaction().begin();
    testGeographicLocation = geographicLocationService.create(testGeographicLocation);
    entityManager.getTransaction().commit();

    geographicLocations.add(testGeographicLocation);

    /*
     * Persistencia de una opcion para la parcela de prueba
     */
    entityManager.getTransaction().begin();
    Option parcelOption = optionService.create();
    entityManager.getTransaction().commit();

    options.add(parcelOption);

    /*
     * Persistencia de una parcela de prueba
     */
    Parcel testParcel = new Parcel();
    testParcel.setName("Erie");
    testParcel.setHectares(2);
    testParcel.setOption(parcelOption);
    testParcel.setGeographicLocation(testGeographicLocation);

    entityManager.getTransaction().begin();
    testParcel = parcelService.create(testParcel);
    entityManager.getTransaction().commit();

    parcels.add(testParcel);

    /*
     * Persistencia de un usuario de prueba
     */
    User testUser = new User();
    testUser.setUsername("tmiller");
    testUser.setName("Tyler");
    testUser.setLastName("Miller");
    testUser.setEmail("tmiller@email.com");
    testUser.setParcels(new ArrayList<>());
    testUser.getParcels().add(testParcel);

    entityManager.getTransaction().begin();
    testUser = userService.create(testUser);
    entityManager.getTransaction().commit();

    users.add(testUser);

    /*
     * Creacion de fechas para los registros climaticos de prueba
     */
    Calendar firstDate = UtilDate.getCurrentDate();
    firstDate.set(Calendar.YEAR, 2023);
    firstDate.set(Calendar.MONTH, JANUARY);
    firstDate.set(Calendar.DAY_OF_MONTH, 2);

    Calendar secondDate = UtilDate.getCurrentDate();
    secondDate.set(Calendar.YEAR, 2023);
    secondDate.set(Calendar.MONTH, JANUARY);
    secondDate.set(Calendar.DAY_OF_MONTH, 3);

    Calendar thirdDate = UtilDate.getCurrentDate();
    thirdDate.set(Calendar.YEAR, 2023);
    thirdDate.set(Calendar.MONTH, JANUARY);
    thirdDate.set(Calendar.DAY_OF_MONTH, 4);

    /*
     * Persistencia de registros climaticos de prueba
     */
    ClimateRecord firstClimateRecord = new ClimateRecord();
    firstClimateRecord.setDate(firstDate);
    firstClimateRecord.setParcel(testParcel);

    ClimateRecord secondClimateRecord = new ClimateRecord();
    secondClimateRecord.setDate(secondDate);
    secondClimateRecord.setParcel(testParcel);

    ClimateRecord thirdClimateRecord = new ClimateRecord();
    thirdClimateRecord.setDate(thirdDate);
    thirdClimateRecord.setParcel(testParcel);

    entityManager.getTransaction().begin();
    firstClimateRecord = climateRecordService.create(firstClimateRecord);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    secondClimateRecord = climateRecordService.create(secondClimateRecord);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    thirdClimateRecord = climateRecordService.create(thirdClimateRecord);
    entityManager.getTransaction().commit();

    climateRecords.add(firstClimateRecord);
    climateRecords.add(secondClimateRecord);
    climateRecords.add(thirdClimateRecord);

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");
    Collection<ClimateRecord> recoveredClimateRecords = climateRecordService.findAllByParcelIdAndPeriod(testUser.getId(), testParcel.getId(), dateFrom, dateUntil);

    int expectedSize = 3;
    int size = recoveredClimateRecords.size();

    System.out.println("* Tamaño esperado de la coleccion devuelta por findAllByParcelIdAndPeriod: " + expectedSize);
    System.out.println("* Tamaño de la coleccion devuelta por findAllByParcelIdAndPeriod: " + size);
    System.out.println();

    assertTrue(expectedSize == size);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();

    System.out.println("# Impresion de los registros climaticos recuperados en el periodo [" + UtilDate.formatDate(dateFrom) + ", " + UtilDate.formatDate(dateUntil) + "]");

    for (ClimateRecord currentClimateRecord : recoveredClimateRecords) {
      System.out.println(currentClimateRecord);
    }

  }

  @Test
  public void testTwoFindAllByParcelIdAndPeriod() {
    System.out.println("******************************** Prueba dos del metodo findAllByParcelIdAndPeriod() ********************************");
    System.out.println("# Descripcion del metodo a probar");
    System.out.println("El metodo findAllByParcelIdAndPeriod de la clase ClimateRecordServiceBean devuelve una coleccion de registros");
    System.out.println("climaticos de una parcela de un usuario que estan en un periodo definido por dos fechas. En el caso en el que una");
    System.out.println("parcela no tiene registros climaticos en un periodo definido por dos fechas, devuelve una coleccion vacia (0");
    System.out.println("elementos).");
    System.out.println();

    Calendar dateFrom = Calendar.getInstance();
    dateFrom.set(Calendar.YEAR, 2023);
    dateFrom.set(Calendar.MONTH, JANUARY);
    dateFrom.set(Calendar.DAY_OF_MONTH, 10);

    Calendar dateUntil = Calendar.getInstance();
    dateUntil.set(Calendar.YEAR, 2023);
    dateUntil.set(Calendar.MONTH, JANUARY);
    dateUntil.set(Calendar.DAY_OF_MONTH, 15);

    System.out.println("# Descripcion de la prueba unitaria");
    System.out.println("Para demostrar el correcto funcionamiento de este metodo se utiliza una parcela que tiene registros climaticos");
    System.out.print("que NO estan en el periodo [" + UtilDate.formatDate(dateFrom) + ", " + UtilDate.formatDate(dateUntil) + "].");
    System.out.println(" Por lo tanto, el metodo findAllByParcelIdAndPeriod debe");
    System.out.println("devolver una coleccion vacia cuando se le pasa como argumento la primera fecha y la segunda fecha del periodo");
    System.out.println("como fecha desde y fecha hasta, respectivamente. El tamaño de una coleccion vacia es igual a 0.");
    System.out.println();

    /*
     * Creacion y persistencia de una ubicacion geografica
     * para parcelas de prueba
     */
    GeographicLocation testGeographicLocation = new GeographicLocation();
    testGeographicLocation.setLatitude(1);
    testGeographicLocation.setLongitude(1);

    entityManager.getTransaction().begin();
    testGeographicLocation = geographicLocationService.create(testGeographicLocation);
    entityManager.getTransaction().commit();

    geographicLocations.add(testGeographicLocation);

    /*
     * Persistencia de una opcion para la parcela de prueba
     */
    entityManager.getTransaction().begin();
    Option parcelOption = optionService.create();
    entityManager.getTransaction().commit();

    options.add(parcelOption);

    /*
     * Persistencia de una parcela de prueba
     */
    Parcel testParcel = new Parcel();
    testParcel.setName("Erie");
    testParcel.setHectares(2);
    testParcel.setOption(parcelOption);
    testParcel.setGeographicLocation(testGeographicLocation);

    entityManager.getTransaction().begin();
    testParcel = parcelService.create(testParcel);
    entityManager.getTransaction().commit();

    parcels.add(testParcel);

    /*
     * Persistencia de un usuario de prueba
     */
    User testUser = new User();
    testUser.setUsername("smiller");
    testUser.setName("Sam");
    testUser.setLastName("Miller");
    testUser.setEmail("smiller@eservice.com");
    testUser.setParcels(new ArrayList<>());
    testUser.getParcels().add(testParcel);

    entityManager.getTransaction().begin();
    testUser = userService.create(testUser);
    entityManager.getTransaction().commit();

    users.add(testUser);

    /*
     * Creacion de fechas para los registros climaticos de prueba
     */
    Calendar firstDate = UtilDate.getCurrentDate();
    firstDate.set(Calendar.YEAR, 2023);
    firstDate.set(Calendar.MONTH, JANUARY);
    firstDate.set(Calendar.DAY_OF_MONTH, 2);

    Calendar secondDate = UtilDate.getCurrentDate();
    secondDate.set(Calendar.YEAR, 2023);
    secondDate.set(Calendar.MONTH, JANUARY);
    secondDate.set(Calendar.DAY_OF_MONTH, 3);

    Calendar thirdDate = UtilDate.getCurrentDate();
    thirdDate.set(Calendar.YEAR, 2023);
    thirdDate.set(Calendar.MONTH, JANUARY);
    thirdDate.set(Calendar.DAY_OF_MONTH, 4);

    /*
     * Persistencia de registros climaticos de prueba
     */
    ClimateRecord firstClimateRecord = new ClimateRecord();
    firstClimateRecord.setDate(firstDate);
    firstClimateRecord.setParcel(testParcel);

    ClimateRecord secondClimateRecord = new ClimateRecord();
    secondClimateRecord.setDate(secondDate);
    secondClimateRecord.setParcel(testParcel);

    ClimateRecord thirdClimateRecord = new ClimateRecord();
    thirdClimateRecord.setDate(thirdDate);
    thirdClimateRecord.setParcel(testParcel);

    entityManager.getTransaction().begin();
    firstClimateRecord = climateRecordService.create(firstClimateRecord);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    secondClimateRecord = climateRecordService.create(secondClimateRecord);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    thirdClimateRecord = climateRecordService.create(thirdClimateRecord);
    entityManager.getTransaction().commit();

    climateRecords.add(firstClimateRecord);
    climateRecords.add(secondClimateRecord);
    climateRecords.add(thirdClimateRecord);

    System.out.println("Registro climatico del ID = " + firstClimateRecord.getId());
    System.out.println("Fecha: " + UtilDate.formatDate(firstClimateRecord.getDate()));
    System.out.println();

    System.out.println("Registro climatico del ID = " + secondClimateRecord.getId());
    System.out.println("Fecha: " + UtilDate.formatDate(secondClimateRecord.getDate()));
    System.out.println();

    System.out.println("Registro climatico del ID = " + thirdClimateRecord.getId());
    System.out.println("Fecha: " + UtilDate.formatDate(thirdClimateRecord.getDate()));
    System.out.println();

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");
    Collection<ClimateRecord> recoveredClimateRecords = climateRecordService.findAllByParcelIdAndPeriod(testUser.getId(), testParcel.getId(), dateFrom, dateUntil);

    int expectedSize = 0;
    int size = recoveredClimateRecords.size();

    System.out.println("* Tamaño esperado de la coleccion devuelta por findAllByParcelIdAndPeriod: " + expectedSize);
    System.out.println("* Tamaño de la coleccion devuelta por findAllByParcelIdAndPeriod: " + size);
    System.out.println();

    assertTrue(expectedSize == size);

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
    for (TypePrecipitation currentTypePrecipitation : typePrecipitations) {
      typePrecipitationService.remove(currentTypePrecipitation.getId());
    }

    for (ClimateRecord currenClimateRecord : climateRecords) {
      climateRecordService.remove(currenClimateRecord.getId());
    }

    for (Parcel currentParcel : parcels) {
      parcelService.remove(currentParcel.getId());
    }

    for (GeographicLocation currentGeographicLocation : geographicLocations) {
      geographicLocationService.remove(currentGeographicLocation.getId());
    }

    for (User currentUser : users) {
      userService.remove(currentUser.getId());
    }

    for (Option currentOption : options) {
      optionService.remove(currentOption.getId());
    }

    entityManager.getTransaction().commit();

    // Cierra las conexiones
    entityManager.close();
    entityManagerFactory.close();
  }

}
