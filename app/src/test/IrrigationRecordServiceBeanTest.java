import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.ArrayList;
import java.util.Collection;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import model.IrrigationRecord;
import model.Option;
import model.Parcel;
import model.User;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import stateless.ParcelServiceBean;
import stateless.IrrigationRecordServiceBean;
import stateless.UserServiceBean;
import stateless.OptionServiceBean;
import util.UtilDate;

public class IrrigationRecordServiceBeanTest {

  private static EntityManager entityManager;
  private static EntityManagerFactory entityManagerFactory;

  private static ParcelServiceBean parcelService;
  private static IrrigationRecordServiceBean irrigationRecordService;
  private static UserServiceBean userService;
  private static OptionServiceBean optionService;

  private static Collection<Parcel> parcels;
  private static Collection<IrrigationRecord> irrigationRecords;
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

    irrigationRecordService = new IrrigationRecordServiceBean();
    irrigationRecordService.setEntityManager(entityManager);

    optionService = new OptionServiceBean();
    optionService.setEntityManager(entityManager);

    parcels = new ArrayList<>();
    irrigationRecords = new ArrayList<>();
    users = new ArrayList<>();
    options = new ArrayList<>();
  }

  @Test
  public void testFindLastBetweenDates() {
    Calendar minorDate = Calendar.getInstance();
    minorDate.set(Calendar.YEAR, 2023);
    minorDate.set(Calendar.MONTH, JANUARY);
    minorDate.set(Calendar.DAY_OF_MONTH, 1);

    Calendar majorDate = Calendar.getInstance();
    majorDate.set(Calendar.YEAR, 2023);
    majorDate.set(Calendar.MONTH, JANUARY);
    majorDate.set(Calendar.DAY_OF_MONTH, 30);

    System.out.println("********************************** Prueba del metodo findLastBetweenDates **********************************");
    System.out.println("El metodo findLastBetweenDates de la clase IrrigationRecordServiceBean recupera el ultimo registro de riego");
    System.out.println("de una parcela en un periodo definido por dos fechas dadas.");
    System.out.println();
    System.out.println("Para demostrar el correcto funcionamiento de este metodo se utilizaran tres registros de riego y las fechas");
    System.out.print(UtilDate.formatDate(minorDate) + " y " + UtilDate.formatDate(majorDate) + " para definir el periodo dentro del");
    System.out.println("cual recuperar el ultimo registro de riego de una");
    System.out.println("parcela dada.");
    System.out.println();

    /*
     * Persistencia de una opcion para un usuario
     */
    entityManager.getTransaction().begin();
    Option parcelOption = optionService.create();
    entityManager.getTransaction().commit();

    options.add(parcelOption);

    /*
     * Persistencia de un usuario de prueba
     */
    User givenUser = new User();
    givenUser.setUsername("matt95");
    givenUser.setName("Matt");
    givenUser.setLastName("Miller");
    givenUser.setEmail("matt@email.com");
    givenUser.setPassword("Matt");

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
    givenParcel.setOption(parcelOption);

    entityManager.getTransaction().begin();
    givenParcel = parcelService.create(givenParcel);
    entityManager.getTransaction().commit();

    parcels.add(givenParcel);

    /*
     * Creacion de fechas para los registros de riego de prueba
     */
    Calendar firstDate = UtilDate.getCurrentDate();
    firstDate.set(Calendar.YEAR, 2023);
    firstDate.set(Calendar.MONTH, JANUARY);
    firstDate.set(Calendar.DAY_OF_MONTH, 1);

    Calendar secondDate = UtilDate.getCurrentDate();
    secondDate.set(Calendar.YEAR, 2023);
    secondDate.set(Calendar.MONTH, JANUARY);
    secondDate.set(Calendar.DAY_OF_MONTH, 2);

    Calendar thirdDate = UtilDate.getCurrentDate();
    thirdDate.set(Calendar.YEAR, 2023);
    thirdDate.set(Calendar.MONTH, JANUARY);
    thirdDate.set(Calendar.DAY_OF_MONTH, 3);

    /*
     * Persistencia de registros de riego de prueba
     */
    IrrigationRecord firstIrrigationRecord = new IrrigationRecord();
    firstIrrigationRecord.setDate(firstDate);
    firstIrrigationRecord.setIrrigationDone(2);
    firstIrrigationRecord.setParcel(givenParcel);

    IrrigationRecord secondIrrigationRecord = new IrrigationRecord();
    secondIrrigationRecord.setDate(secondDate);
    secondIrrigationRecord.setIrrigationDone(2);
    secondIrrigationRecord.setParcel(givenParcel);

    IrrigationRecord thirdIrrigationRecord = new IrrigationRecord();
    thirdIrrigationRecord.setDate(thirdDate);
    thirdIrrigationRecord.setIrrigationDone(2);
    thirdIrrigationRecord.setParcel(givenParcel);

    entityManager.getTransaction().begin();
    firstIrrigationRecord = irrigationRecordService.create(firstIrrigationRecord);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    secondIrrigationRecord = irrigationRecordService.create(secondIrrigationRecord);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    thirdIrrigationRecord = irrigationRecordService.create(thirdIrrigationRecord);
    entityManager.getTransaction().commit();

    irrigationRecords.add(firstIrrigationRecord);
    irrigationRecords.add(secondIrrigationRecord);
    irrigationRecords.add(thirdIrrigationRecord);

    System.out.println("* Registros de riego de prueba *");
    System.out.println("ID: " + firstIrrigationRecord.getId());
    System.out.println("Fecha: " + UtilDate.formatDate(firstIrrigationRecord.getDate()));
    System.out.println("Riego realizado: " + firstIrrigationRecord.getIrrigationDone());
    System.out.println();

    System.out.println("ID: " + secondIrrigationRecord.getId());
    System.out.println("Fecha: " + UtilDate.formatDate(secondIrrigationRecord.getDate()));
    System.out.println("Riego realizado: " + secondIrrigationRecord.getIrrigationDone());
    System.out.println();

    System.out.println("ID: " + thirdIrrigationRecord.getId());
    System.out.println("Fecha: " + UtilDate.formatDate(thirdIrrigationRecord.getDate()));
    System.out.println("Riego realizado: " + thirdIrrigationRecord.getIrrigationDone());
    System.out.println();

    /*
     * Seccion de prueba
     */
    System.out.println("* Seccion de prueba *");
    IrrigationRecord expectedIrrigationRecord = thirdIrrigationRecord;
    IrrigationRecord lastIrrigationRecord = irrigationRecordService.findLastBetweenDates(givenUser.getId(), givenParcel.getId(), minorDate, majorDate);

    System.out.println("* ID del ultimo registro de riego esperado: " + expectedIrrigationRecord.getId());
    System.out.println("* ID del ultimo registro de riego devuelto por el metodo findLastBetweenDates: " + lastIrrigationRecord.getId());
    System.out.println();

    assertTrue(expectedIrrigationRecord.getId() == lastIrrigationRecord.getId());

    System.out.println("- Prueba pasada satisfcatoriamente");
  }

  @Test
  public void testOneFindAllByParcelIdAndPeriod() {
    System.out.println("******************************** Prueba uno del metodo findAllByParcelIdAndPeriod ********************************");
    System.out.println("# Descripcion del metodo a probar");
    System.out.println("El metodo findAllByParcelIdAndPeriod de la clase IrrigationRecordServiceBean devuelve una coleccion de registros");
    System.out.println("de riego de una parcela de un usuario que estan en un periodo definido por dos fechas. En el caso en el que una");
    System.out.println("parcela no tiene registros de riego en un periodo definido por dos fechas, devuelve una coleccion vacia (0");
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
    System.out.println("Para demostrar el correcto funcionamiento de este metodo se utilizan tres registros de riego que estan en el");
    System.out.println("periodo [" + UtilDate.formatDate(dateFrom) + ", " + UtilDate.formatDate(dateUntil) + "].");
    System.out.println();
    System.out.println("Con la primera fecha y la segunda fecha del periodo como fecha desde y fecha hasta, respectivamente, el metodo");
    System.out.println("findAllByParcelIdAndPeriod debe devolver una coleccion con tres registros de riego. Por lo tanto, el tamaño de");
    System.out.println("dicha coleccion debe ser igual a 3.");
    System.out.println();

    /*
     * Persistencia de una opcion para el usuario de prueba
     */
    entityManager.getTransaction().begin();
    Option parcelOption = optionService.create();
    entityManager.getTransaction().commit();

    options.add(parcelOption);

    /*
     * Persistencia de un usuario de prueba
     */
    User givenUser = new User();
    givenUser.setUsername("maxmiller");
    givenUser.setName("Max");
    givenUser.setLastName("Miller");
    givenUser.setEmail("maxmiller@eservice.com");
    givenUser.setPassword("Max");

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
    givenParcel.setOption(parcelOption);

    entityManager.getTransaction().begin();
    givenParcel = parcelService.create(givenParcel);
    entityManager.getTransaction().commit();

    parcels.add(givenParcel);

    /*
     * Creacion de fechas para los registros de riego de prueba
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
     * Persistencia de registros de riego de prueba
     */
    IrrigationRecord firstIrrigationRecord = new IrrigationRecord();
    firstIrrigationRecord.setDate(firstDate);
    firstIrrigationRecord.setParcel(givenParcel);

    IrrigationRecord secondIrrigationRecord = new IrrigationRecord();
    secondIrrigationRecord.setDate(secondDate);
    secondIrrigationRecord.setParcel(givenParcel);

    IrrigationRecord thirdIrrigationRecord = new IrrigationRecord();
    thirdIrrigationRecord.setDate(thirdDate);
    thirdIrrigationRecord.setParcel(givenParcel);

    entityManager.getTransaction().begin();
    firstIrrigationRecord = irrigationRecordService.create(firstIrrigationRecord);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    secondIrrigationRecord = irrigationRecordService.create(secondIrrigationRecord);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    thirdIrrigationRecord = irrigationRecordService.create(thirdIrrigationRecord);
    entityManager.getTransaction().commit();

    irrigationRecords.add(firstIrrigationRecord);
    irrigationRecords.add(secondIrrigationRecord);
    irrigationRecords.add(thirdIrrigationRecord);

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");
    Collection<IrrigationRecord> recoveredIrrigationRecords = irrigationRecordService.findAllByParcelIdAndPeriod(givenUser.getId(), givenParcel.getId(), dateFrom, dateUntil);

    int expectedSize = 3;
    int size = recoveredIrrigationRecords.size();

    System.out.println("* Tamaño esperado de la coleccion devuelta por findAllByParcelIdAndPeriod: " + expectedSize);
    System.out.println("* Tamaño de la coleccion devuelta por findAllByParcelIdAndPeriod: " + size);
    System.out.println();

    assertTrue(expectedSize == size);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();

    System.out.println("# Impresion de los registros de riego recuperados en el periodo [" + UtilDate.formatDate(dateFrom) + ", " + UtilDate.formatDate(dateUntil) + "]");

    for (IrrigationRecord currentIrrigationRecord : recoveredIrrigationRecords) {
      System.out.println("ID: " + currentIrrigationRecord.getId());
      System.out.println("Fecha: " + UtilDate.formatDate(currentIrrigationRecord.getDate()));
      System.out.println("Riego realizado: " + currentIrrigationRecord.getIrrigationDone());
      System.out.println();
    }

  }

  @Test
  public void testTwoFindAllByParcelIdAndPeriod() {
    System.out.println("******************************** Prueba dos del metodo findAllByParcelIdAndPeriod ********************************");
    System.out.println("# Descripcion del metodo a probar");
    System.out.println("El metodo findAllByParcelIdAndPeriod de la clase IrrigationRecordServiceBean devuelve una coleccion de registros");
    System.out.println("de riego de una parcela de un usuario que estan en un periodo definido por dos fechas. En el caso en el que una");
    System.out.println("parcela no tiene registros de riego en un periodo definido por dos fechas, devuelve una coleccion vacia (0");
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
    System.out.println("Para demostrar el correcto funcionamiento de este metodo se utiliza una parcela que tiene registros de riego");
    System.out.print("que NO estan en el periodo [" + UtilDate.formatDate(dateFrom) + ", " + UtilDate.formatDate(dateUntil) + "].");
    System.out.println(" Por lo tanto, el metodo findAllByParcelIdAndPeriod debe");
    System.out.println("devolver una coleccion vacia cuando se le pasa como argumento la primera fecha y la segunda fecha del periodo");
    System.out.println("como fecha desde y fecha hasta, respectivamente. El tamaño de una coleccion vacia es igual a 0.");
    System.out.println();

    /*
     * Persistencia de una opcion para el usuario de prueba
     */
    entityManager.getTransaction().begin();
    Option parcelOption = optionService.create();
    entityManager.getTransaction().commit();

    options.add(parcelOption);

    /*
     * Persistencia de un usuario de prueba
     */
    User givenUser = new User();
    givenUser.setUsername("gmiller");
    givenUser.setName("Gordon");
    givenUser.setLastName("Miller");
    givenUser.setEmail("smiller@eservice.com");
    givenUser.setPassword("Gordon");

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
    givenParcel.setOption(parcelOption);

    entityManager.getTransaction().begin();
    givenParcel = parcelService.create(givenParcel);
    entityManager.getTransaction().commit();

    parcels.add(givenParcel);

    /*
     * Creacion de fechas para los registros de riego de prueba
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
     * Persistencia de registros de riego de prueba
     */
    IrrigationRecord firstIrrigationRecord = new IrrigationRecord();
    firstIrrigationRecord.setDate(firstDate);
    firstIrrigationRecord.setParcel(givenParcel);

    IrrigationRecord secondIrrigationRecord = new IrrigationRecord();
    secondIrrigationRecord.setDate(secondDate);
    secondIrrigationRecord.setParcel(givenParcel);

    IrrigationRecord thirdIrrigationRecord = new IrrigationRecord();
    thirdIrrigationRecord.setDate(thirdDate);
    thirdIrrigationRecord.setParcel(givenParcel);

    entityManager.getTransaction().begin();
    firstIrrigationRecord = irrigationRecordService.create(firstIrrigationRecord);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    secondIrrigationRecord = irrigationRecordService.create(secondIrrigationRecord);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    thirdIrrigationRecord = irrigationRecordService.create(thirdIrrigationRecord);
    entityManager.getTransaction().commit();

    irrigationRecords.add(firstIrrigationRecord);
    irrigationRecords.add(secondIrrigationRecord);
    irrigationRecords.add(thirdIrrigationRecord);

    System.out.println("Registro de riego del ID = " + firstIrrigationRecord.getId());
    System.out.println("Fecha: " + UtilDate.formatDate(firstIrrigationRecord.getDate()));
    System.out.println();

    System.out.println("Registro de riego del ID = " + secondIrrigationRecord.getId());
    System.out.println("Fecha: " + UtilDate.formatDate(secondIrrigationRecord.getDate()));
    System.out.println();

    System.out.println("Registro de riego del ID = " + thirdIrrigationRecord.getId());
    System.out.println("Fecha: " + UtilDate.formatDate(thirdIrrigationRecord.getDate()));
    System.out.println();

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");
    Collection<IrrigationRecord> recoveredIrrigationRecords = irrigationRecordService.findAllByParcelIdAndPeriod(givenUser.getId(), givenParcel.getId(), dateFrom, dateUntil);

    int expectedSize = 0;
    int size = recoveredIrrigationRecords.size();

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
    for (IrrigationRecord currentIrrigationRecord : irrigationRecords) {
      irrigationRecordService.remove(currentIrrigationRecord.getId());
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
