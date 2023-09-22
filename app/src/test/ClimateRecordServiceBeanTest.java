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
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import stateless.ParcelServiceBean;
import stateless.UserServiceBean;
import stateless.ClimateRecordServiceBean;
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
  private static Collection<Parcel> parcels;
  private static Collection<ClimateRecord> climateRecords;
  private static Collection<TypePrecipitation> typePrecipitations;
  private static Collection<User> users;
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

    userService = new UserServiceBean();
    userService.setEntityManager(entityManager);

    optionService = new OptionServiceBean();
    optionService.setEntityManager(entityManager);

    climateRecordService = new ClimateRecordServiceBean();
    climateRecordService.setEntityManager(entityManager);

    typePrecipitationService = new TypePrecipitationServiceBean();
    typePrecipitationService.setEntityManager(entityManager);

    parcels = new ArrayList<>();
    climateRecords = new ArrayList<>();
    typePrecipitations = new ArrayList<>();
    users = new ArrayList<>();
    options = new ArrayList<>();
  }

  @Test
  public void test() {
    System.out.println("Esta prueba unitaria es para probar que, cuando se obtiene un conjunto de datos meteorologicos, de una llamada a la");
    System.out.println("API Visual Crossing Weather, que contiene tipos de precipitacion y se persiste un registro climatico con este conjunto,");
    System.out.println("se persisten tambien los tipos de precipitacion.");
    System.out.println();
    System.out.println("Para demostrar en esta prueba que se persisten los tipos de precipitacion cuando se persiste un registro climatico,");
    System.out.println("se realiza una llamada a Visual Crossing Weather con una coordenada geografica de Nueva York y la fecha 14-1-2023.");
    System.out.println("Esta llamada devuelve un conjunto de datos meteorologicos en los que hay dos tipos de precipitacion: rain y snow.");
    System.out.println();

    /*
     * Creacion y persistencia de una parcela de prueba
     */
    Parcel givenParcel = new Parcel();
    givenParcel.setName("Parcela Nueva York");
    givenParcel.setHectares(2);
    givenParcel.setLatitude(40.71427);
    givenParcel.setLongitude(-74.00597);
    givenParcel.setUser(userService.find(1));

    entityManager.getTransaction().begin();
    givenParcel = parcelService.create(givenParcel);
    entityManager.getTransaction().commit();

    System.out.println("* Datos de la parcela de prueba");
    System.out.println(givenParcel);

    /*
     * Obtencion y persistencia del registro climatico
     * del 14 de enero de 2023 GMT+0000 en tiempo UNIX
     */
    long datetimeEpoch = 1673672400;

    Calendar date = Calendar.getInstance();
    date.setTimeInMillis(datetimeEpoch * 1000);

    System.out.println("* Fecha para la que se obtiene un conjunto de datos meteorologicos: " +  UtilDate.formatDate(date));
    System.out.println();

    /*
     * NOTA: Hay que tener en cuenta que en la clase ClimateClient
     * debe estar asignada la clave de la API Visual Crossing
     * Weather en el URL de solicitud de datos meteorologicos para
     * obtener dichos datos de dicha API
     */
    ClimateRecord givenClimateRecord = ClimateClient.getForecast(givenParcel, datetimeEpoch);

    entityManager.getTransaction().begin();
    givenClimateRecord = climateRecordService.create(givenClimateRecord);
    entityManager.getTransaction().commit();

    System.out.println("* Registro climatico obtenido y persistido");
    System.out.println(givenClimateRecord);

    /*
     * Los datos creados se agregan a una coleccion para su
     * posterior eliminacion de la base de datos subyacente
     */
    parcels.add(givenParcel);
    climateRecords.add(givenClimateRecord);
    typePrecipitations = givenClimateRecord.getPrecipTypes();

    /*
     * Seccion de prueba
     */
    TypePrecipitation typePrecipitationRain = (TypePrecipitation) givenClimateRecord.getPrecipTypes().toArray()[0];
    TypePrecipitation typePrecipitationSnow = (TypePrecipitation) givenClimateRecord.getPrecipTypes().toArray()[1];

    System.out.println("* Tipos de precipitacion del conjunto de datos meteorologicos obtenido para la ubicacion geografica y fecha dadas");
    System.out.println(typePrecipitationRain);
    System.out.println(typePrecipitationSnow);

    /*
     * Cuando se persiste un objeto en la base de datos subyacente,
     * el ORM utilizado retorna la referencia a este objeto, el
     * cual contiene su variable de instancia id con un valor
     * distinto de cero. Por lo tanto, si se persiste satisfactoriamente
     * un tipo de precipitacion, su ID debe ser distinto de cero.
     */
    assertTrue(typePrecipitationRain.getId() != 0);
    assertTrue(typePrecipitationSnow.getId() != 0);

    System.out.println("* Tipos de precipitacion persistidos");
    System.out.println("* Prueba pasada satisfactoriamente *");
    System.out.println();
  }

  /*
   * TODO: Borrar esta prueba unitaria porque no es necesario
   * el metodo sumRainwaterPastDays
   */

  @Test
  public void testSumRainwaterPastDays() {
    System.out.println("**************************** Prueba del metodo sumRainwaterPastDays ****************************");
    System.out.println("El metodo sumRainwaterPastDays de la clase ClimateRecordServiceBean suma el agua de lluvia de NUMBER_DAYS");
    System.out.println("registros climaticos anteriores a la fecha actual pertenecientes a una parcela de un usuario.");
    System.out.println();
    System.out.println("Para demostrar el correcto funcionamiento de este metodo se utilizan tres registros climaticos que");
    System.out.println("tienen una cantidad de agua de lluvia igual a dos y pertenecen a los tres dias inmediatamente anteriores");
    System.out.println("a la fecha actual.");
    System.out.println();

    /*
     * Persistencia de un usuario de prueba
     */
    User givenUser = new User();
    givenUser.setUsername("audra");
    givenUser.setName("Audra");
    givenUser.setLastName("Miller");
    givenUser.setEmail("a@email.com");
    givenUser.setPassword("Audra");

    entityManager.getTransaction().begin();
    givenUser = userService.create(givenUser);
    entityManager.getTransaction().commit();

    users.add(givenUser);

    /*
     * Persistencia de una parcela de prueba
     */
    Parcel givenParcel = new Parcel();
    givenParcel.setName("Erie");
    givenParcel.setHectares(2);
    givenParcel.setLatitude(1);
    givenParcel.setLongitude(1);
    givenParcel.setUser(givenUser);

    entityManager.getTransaction().begin();
    givenParcel = parcelService.create(givenParcel);
    entityManager.getTransaction().commit();

    parcels.add(givenParcel);

    /*
     * Creacion de fechas para los registros climaticos
     */
    Calendar firstDate = UtilDate.getCurrentDate();
    firstDate.set(Calendar.DAY_OF_YEAR, (firstDate.get(Calendar.DAY_OF_YEAR) - 1));

    Calendar secondDate = UtilDate.getCurrentDate();
    secondDate.set(Calendar.DAY_OF_YEAR, (secondDate.get(Calendar.DAY_OF_YEAR) - 2));

    Calendar thirdDate = UtilDate.getCurrentDate();
    thirdDate.set(Calendar.DAY_OF_YEAR, (thirdDate.get(Calendar.DAY_OF_YEAR) - 3));

    /*
     * Persistencia de registros climaticos de prueba
     */
    ClimateRecord firstClimateRecord = new ClimateRecord();
    firstClimateRecord.setDate(firstDate);
    firstClimateRecord.setPrecip(2);
    firstClimateRecord.setParcel(givenParcel);

    ClimateRecord secondClimateRecord = new ClimateRecord();
    secondClimateRecord.setDate(secondDate);
    secondClimateRecord.setPrecip(2);
    secondClimateRecord.setParcel(givenParcel);

    ClimateRecord thirdClimateRecord = new ClimateRecord();
    thirdClimateRecord.setDate(thirdDate);
    thirdClimateRecord.setPrecip(2);
    thirdClimateRecord.setParcel(givenParcel);

    entityManager.getTransaction().begin();
    firstClimateRecord = climateRecordService.create(firstClimateRecord);
    secondClimateRecord = climateRecordService.create(secondClimateRecord);
    thirdClimateRecord = climateRecordService.create(thirdClimateRecord);
    entityManager.getTransaction().commit();

    climateRecords.add(firstClimateRecord);
    climateRecords.add(secondClimateRecord);
    climateRecords.add(thirdClimateRecord);

    System.out.println("Agua de lluvia del registro climatico de la fecha " + UtilDate.formatDate(firstClimateRecord.getDate()) + ": " + firstClimateRecord.getPrecip());
    System.out.println("Agua de lluvia del registro climatico de la fecha " + UtilDate.formatDate(secondClimateRecord.getDate()) + ": " + secondClimateRecord.getPrecip());
    System.out.println("Agua de lluvia del registro climatico de la fecha " + UtilDate.formatDate(thirdClimateRecord.getDate()) + ": " + thirdClimateRecord.getPrecip());
    System.out.println();

    /*
     * Seccion de prueba
     */
    double expectedResult = firstClimateRecord.getPrecip() + secondClimateRecord.getPrecip() + thirdClimateRecord.getPrecip();
    // double result = climateRecordService.sumRainwaterPastDays(givenUser.getId(), givenParcel.getId());
    double result = 0.0;

    System.out.println("* Resultado esperado: " + expectedResult);
    System.out.println("* Resultado devuelto por el metodo sumRainwaterPastDays: " + result);
    System.out.println();

    assertTrue(expectedResult == result);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  /*
   * TODO: Borrar esta prueba unitaria porque no es necesario
   * el metodo sumEtcPastDays
   */

  @Test
  public void testSumEtcPastDays() {
    System.out.println("******************************** Prueba del metodo sumEtcPastDays ********************************");
    System.out.println("El metodo sumEtcPastDays de la clase ClimateRecordServiceBean suma la ETc de NUMBER_DAYS registros");
    System.out.println("climaticos anteriores a la fecha actual pertenecientes a una parcela de un usuario.");
    System.out.println();
    System.out.println("Para demostrar el correcto funcionamiento de este metodo se utilizan tres registros climaticos de");
    System.out.println("los tres dias inmediatamente anteriores a la fecha actual, los cuales tienen diferente valor de ETc.");
    System.out.println();

    /*
     * Persistencia de un usuario de prueba
     */
    User givenUser = new User();
    givenUser.setUsername("amiller");
    givenUser.setName("Audra");
    givenUser.setLastName("Miller");
    givenUser.setEmail("amiller@email.com");
    givenUser.setPassword("Audra");

    entityManager.getTransaction().begin();
    givenUser = userService.create(givenUser);
    entityManager.getTransaction().commit();

    users.add(givenUser);

    /*
     * Persistencia de una parcela de prueba
     */
    Parcel givenParcel = new Parcel();
    givenParcel.setName("Erie");
    givenParcel.setHectares(2);
    givenParcel.setLatitude(1);
    givenParcel.setLongitude(1);
    givenParcel.setUser(givenUser);

    entityManager.getTransaction().begin();
    givenParcel = parcelService.create(givenParcel);
    entityManager.getTransaction().commit();

    parcels.add(givenParcel);

    /*
     * Creacion de fechas para los registros climaticos
     */
    Calendar firstDate = UtilDate.getCurrentDate();
    firstDate.set(Calendar.DAY_OF_YEAR, (firstDate.get(Calendar.DAY_OF_YEAR) - 1));

    Calendar secondDate = UtilDate.getCurrentDate();
    secondDate.set(Calendar.DAY_OF_YEAR, (secondDate.get(Calendar.DAY_OF_YEAR) - 2));

    Calendar thirdDate = UtilDate.getCurrentDate();
    thirdDate.set(Calendar.DAY_OF_YEAR, (thirdDate.get(Calendar.DAY_OF_YEAR) - 3));

    /*
     * Persistencia de registros climaticos de prueba
     */
    ClimateRecord firstClimateRecord = new ClimateRecord();
    firstClimateRecord.setDate(firstDate);
    firstClimateRecord.setEtc(0.6497471841933746);
    firstClimateRecord.setParcel(givenParcel);

    ClimateRecord secondClimateRecord = new ClimateRecord();
    secondClimateRecord.setDate(secondDate);
    secondClimateRecord.setEtc(0.5212612716181513);
    secondClimateRecord.setParcel(givenParcel);

    ClimateRecord thirdClimateRecord = new ClimateRecord();
    thirdClimateRecord.setDate(thirdDate);
    thirdClimateRecord.setEtc(0.45906521631463165);
    thirdClimateRecord.setParcel(givenParcel);

    entityManager.getTransaction().begin();
    firstClimateRecord = climateRecordService.create(firstClimateRecord);
    secondClimateRecord = climateRecordService.create(secondClimateRecord);
    thirdClimateRecord = climateRecordService.create(thirdClimateRecord);
    entityManager.getTransaction().commit();

    climateRecords.add(firstClimateRecord);
    climateRecords.add(secondClimateRecord);
    climateRecords.add(thirdClimateRecord);

    System.out.println("ETc del registro climatico de la fecha " + UtilDate.formatDate(firstClimateRecord.getDate()) + ": " + firstClimateRecord.getEtc());
    System.out.println("ETc del registro climatico de la fecha " + UtilDate.formatDate(secondClimateRecord.getDate()) + ": " + secondClimateRecord.getEtc());
    System.out.println("ETc del registro climatico de la fecha " + UtilDate.formatDate(thirdClimateRecord.getDate()) + ": " + thirdClimateRecord.getEtc());
    System.out.println();

    /*
     * Seccion de prueba
     */
    double expectedResult = firstClimateRecord.getEtc() + secondClimateRecord.getEtc() + thirdClimateRecord.getEtc();
    // double result = climateRecordService.sumEtcPastDays(givenUser.getId(), givenParcel.getId());
    double result = 0.0;

    System.out.println("* Resultado esperado: " + expectedResult);
    System.out.println("* Resultado devuelto por el metodo sumEtcPastDays: " + result);
    System.out.println();

    assertTrue(expectedResult == result);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testOneFindAllByParcelIdAndPeriod() {
    System.out.println("******************************** Prueba uno del metodo findAllByParcelIdAndPeriod ********************************");
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
     * Persistencia de una opcion para el usuario de prueba
     */
    entityManager.getTransaction().begin();
    Option userOption = optionService.create();
    entityManager.getTransaction().commit();

    options.add(userOption);

    /*
     * Persistencia de un usuario de prueba
     */
    User givenUser = new User();
    givenUser.setUsername("tmiller");
    givenUser.setName("Tyler");
    givenUser.setLastName("Miller");
    givenUser.setEmail("tmiller@email.com");
    givenUser.setPassword("Tyler");
    givenUser.setOption(userOption);

    entityManager.getTransaction().begin();
    givenUser = userService.create(givenUser);
    entityManager.getTransaction().commit();

    users.add(givenUser);

    /*
     * Persistencia de una parcela de prueba
     */
    Parcel givenParcel = new Parcel();
    givenParcel.setName("Erie");
    givenParcel.setHectares(2);
    givenParcel.setLatitude(1);
    givenParcel.setLongitude(1);
    givenParcel.setUser(givenUser);

    entityManager.getTransaction().begin();
    givenParcel = parcelService.create(givenParcel);
    entityManager.getTransaction().commit();

    parcels.add(givenParcel);

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
    firstClimateRecord.setParcel(givenParcel);

    ClimateRecord secondClimateRecord = new ClimateRecord();
    secondClimateRecord.setDate(secondDate);
    secondClimateRecord.setParcel(givenParcel);

    ClimateRecord thirdClimateRecord = new ClimateRecord();
    thirdClimateRecord.setDate(thirdDate);
    thirdClimateRecord.setParcel(givenParcel);

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
    Collection<ClimateRecord> recoveredClimateRecords = climateRecordService.findAllByParcelIdAndPeriod(givenUser.getId(), givenParcel.getId(), dateFrom, dateUntil);

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
    System.out.println("******************************** Prueba dos del metodo findAllByParcelIdAndPeriod ********************************");
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
     * Persistencia de una opcion para el usuario de prueba
     */
    entityManager.getTransaction().begin();
    Option userOption = optionService.create();
    entityManager.getTransaction().commit();

    options.add(userOption);

    /*
     * Persistencia de un usuario de prueba
     */
    User givenUser = new User();
    givenUser.setUsername("smiller");
    givenUser.setName("Sam");
    givenUser.setLastName("Miller");
    givenUser.setEmail("smiller@eservice.com");
    givenUser.setPassword("Sam");
    givenUser.setOption(userOption);

    entityManager.getTransaction().begin();
    givenUser = userService.create(givenUser);
    entityManager.getTransaction().commit();

    users.add(givenUser);

    /*
     * Persistencia de una parcela de prueba
     */
    Parcel givenParcel = new Parcel();
    givenParcel.setName("Erie");
    givenParcel.setHectares(2);
    givenParcel.setLatitude(1);
    givenParcel.setLongitude(1);
    givenParcel.setUser(givenUser);

    entityManager.getTransaction().begin();
    givenParcel = parcelService.create(givenParcel);
    entityManager.getTransaction().commit();

    parcels.add(givenParcel);

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
    firstClimateRecord.setParcel(givenParcel);

    ClimateRecord secondClimateRecord = new ClimateRecord();
    secondClimateRecord.setDate(secondDate);
    secondClimateRecord.setParcel(givenParcel);

    ClimateRecord thirdClimateRecord = new ClimateRecord();
    thirdClimateRecord.setDate(thirdDate);
    thirdClimateRecord.setParcel(givenParcel);

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
    Collection<ClimateRecord> recoveredClimateRecords = climateRecordService.findAllByParcelIdAndPeriod(givenUser.getId(), givenParcel.getId(), dateFrom, dateUntil);

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
