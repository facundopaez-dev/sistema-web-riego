import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.ArrayList;
import java.util.Collection;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import model.IrrigationRecord;
import model.Parcel;
import model.User;
import model.PlantingRecord;
import model.ClimateRecord;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import stateless.ParcelServiceBean;
import stateless.IrrigationRecordServiceBean;
import stateless.UserServiceBean;
import stateless.CropServiceBean;
import stateless.ClimateRecordServiceBean;
import util.UtilDate;
import irrigation.WaterMath;

public class CalculateIrrigationWaterNeedTest {

  private static EntityManager entityManager;
  private static EntityManagerFactory entityManagerFactory;
  private static ParcelServiceBean parcelService;
  private static IrrigationRecordServiceBean irrigationRecordService;
  private static UserServiceBean userService;
  private static CropServiceBean cropService;
  private static ClimateRecordServiceBean climateRecordService;
  private static Collection<Parcel> parcels;
  private static Collection<IrrigationRecord> irrigationRecords;
  private static Collection<User> users;
  private static Collection<ClimateRecord> climateRecords;

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

    cropService = new CropServiceBean();
    cropService.setEntityManager(entityManager);

    climateRecordService = new ClimateRecordServiceBean();
    climateRecordService.setEntityManager(entityManager);

    parcels = new ArrayList<>();
    irrigationRecords = new ArrayList<>();
    users = new ArrayList<>();
    climateRecords = new ArrayList<>();
  }

  @Test
  public void testOneCalculateIrrigationWaterNeed() {
    System.out.println("**************************** Prueba uno del metodo calculateIrrigationWaterNeed ****************************");
    System.out.println("Sea X una cantidad mayor a cero, el metodo calculateIrrigationWaterNeed de la clase WaterMath retorna");
    System.out.println("la necesidad de agua de riego en la fecha actual de un cultivo en desarrollo en funcion de la suma de");
    System.out.println("la ETc de X de dias anteriores a la fecha actual, la suma del agua de lluvia de X dias anteriores a la");
    System.out.println("fecha actual, la suma del agua de riego de X dias anteriores a la fecha actual y la cantidad total del");
    System.out.println("agua de riego de la fecha actual.");
    System.out.println();
    System.out.println("Este metodo devuelve una necesidad de agua de riego igual a cero si una de las sumas de agua o la suma de las");
    System.out.println("sumas de agua es mayor o igual a la suma de las ETc. En cambio, retorna una necesidad de agua de riego mayor a");
    System.out.println("cero si ninguna de las sumas de agua y ni la suma de las sumas de agua es mayor a la suma de las ETc.");
    System.out.println();
    System.out.println("Para demostrar el correcto funcionamiento de este metodo se utiliza un registro climatico del dia inmediatamente");
    System.out.println("anterior a la fecha actual, el cual tiene una ETc MAYOR a cero y una cantidad de agua de lluvia IGUAL cero.");
    System.out.println("Tambien se utiliza un registro de riego del dia inmediatamente anterior a la fecha actual, el cual tiene una");
    System.out.println("cantidad de agua de riego IGUAL a cero. No se utiliza ningun registro de riego de la fecha actual, con lo cual");
    System.out.println("se asume que la cantidad total de agua de riego de la fecha actual es IGUAL a cero. Por lo tanto, la necesidad");
    System.out.println("de agua de riego en la fecha actual para un cultivo en desarrollo devuelta por el metodo calculateIrrigationWaterNeed");
    System.out.println("es MAYOR a cero.");
    System.out.println();

    /*
     * Persistencia de un usuario de prueba
     */
    User givenUser = new User();
    givenUser.setUsername("miller695");
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

    /*
     * Persistencia de registros climaticos de prueba
     */
    ClimateRecord firstClimateRecord = new ClimateRecord();
    firstClimateRecord.setDate(firstDate);
    firstClimateRecord.setEtc(0.4374347820832177);
    firstClimateRecord.setParcel(givenParcel);

    entityManager.getTransaction().begin();
    firstClimateRecord = climateRecordService.create(firstClimateRecord);
    entityManager.getTransaction().commit();

    climateRecords.add(firstClimateRecord);

    /*
     * Persistencia de registros de riego de prueba
     */
    IrrigationRecord firstIrrigationRecord = new IrrigationRecord();
    firstIrrigationRecord.setDate(firstDate);
    firstIrrigationRecord.setIrrigationDone(0.0);
    firstIrrigationRecord.setParcel(givenParcel);

    entityManager.getTransaction().begin();
    firstIrrigationRecord = irrigationRecordService.create(firstIrrigationRecord);
    entityManager.getTransaction().commit();

    irrigationRecords.add(firstIrrigationRecord);

    /*
     * El valor de esta variable se utiliza para sumar el
     * agua de riego de una cantidad numberDays de registros
     * de riego inmediatamente anteriores a la fecha actual
     */
    int numberDays = climateRecords.size();

    /*
     * Seccion de prueba
     */
    System.out.println("ETc del dia inmediatamente anterior a la fecha actual: " + climateRecordService.sumEtcPastDays(givenUser.getId(), givenParcel.getId()));
    System.out.println("Agua de lluvia del dia inmediatamente anterior a la fecha actual: " + climateRecordService.sumRainwaterPastDays(givenUser.getId(), givenParcel.getId()));
    System.out.println("Agua de riego del dia inmediatamente anterior a la fecha actual: " + irrigationRecordService
          .sumIrrigationWaterPastDays(givenUser.getId(), givenParcel.getId(), numberDays));
    System.out.println("Cantidad total de agua de riego de la fecha actual: " + irrigationRecordService.calculateTotalIrrigationWaterCurrentDate(givenParcel));
    System.out.println();
    
    double result = calculateIrrigationWaterNeedCurrentDate(givenUser.getId(), givenParcel);

    System.out.println("* Resultado devuelto por el metodo calculateIrrigationWaterNeedCurrentDate: " + result);
    System.out.println();

    assertTrue(result > 0);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testTwoCalculateIrrigationWaterNeed() {
    System.out.println("**************************** Prueba dos del metodo calculateIrrigationWaterNeed ****************************");
    System.out.println("Sea X una cantidad mayor a cero, el metodo calculateIrrigationWaterNeed de la clase WaterMath retorna");
    System.out.println("la necesidad de agua de riego en la fecha actual de un cultivo en desarrollo en funcion de la suma de");
    System.out.println("la ETc de X de dias anteriores a la fecha actual, la suma del agua de lluvia de X dias anteriores a la");
    System.out.println("fecha actual, la suma del agua de riego de X dias anteriores a la fecha actual y la cantidad total del");
    System.out.println("agua de riego de la fecha actual.");
    System.out.println();
    System.out.println("Este metodo devuelve una necesidad de agua de riego igual a cero si una de las sumas de agua o la suma de las");
    System.out.println("sumas de agua es mayor o igual a la suma de las ETc. En cambio, retorna una necesidad de agua de riego mayor a");
    System.out.println("cero si ninguna de las sumas de agua y ni la suma de las sumas de agua es mayor a la suma de las ETc.");
    System.out.println();
    System.out.println("Para demostrar el correcto funcionamiento de este metodo se utiliza un registro climatico del dia inmediatamente");
    System.out.println("anterior a la fecha actual, el cual tiene una ETc MAYOR a cero y una cantidad de agua de lluvia MAYOR a la ETc.");
    System.out.println("Tambien se utiliza un registro de riego del dia inmediatamente anterior a la fecha actual, el cual tiene una");
    System.out.println("cantidad de agua de riego IGUAL a cero. No se utiliza ningun registro de riego de la fecha actual, con lo cual");
    System.out.println("se asume que la cantidad total de agua de riego de la fecha actual es IGUAL a cero. Por lo tanto, la necesidad de");
    System.out.println("agua de riego en la fecha actual para un cultivo en desarrollo devuelta por el metodo calculateIrrigationWaterNeed");
    System.out.println("es IGUAL a cero.");
    System.out.println();

    /*
     * Persistencia de un usuario de prueba
     */
    User givenUser = new User();
    givenUser.setUsername("miller615");
    givenUser.setName("Audra");
    givenUser.setLastName("Miller");
    givenUser.setEmail("apil@email.com");
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

    /*
     * Persistencia de registros climaticos de prueba
     */
    ClimateRecord firstClimateRecord = new ClimateRecord();
    firstClimateRecord.setDate(firstDate);
    firstClimateRecord.setEtc(0.4374347820832177);
    firstClimateRecord.setPrecip(2);
    firstClimateRecord.setParcel(givenParcel);

    entityManager.getTransaction().begin();
    firstClimateRecord = climateRecordService.create(firstClimateRecord);
    entityManager.getTransaction().commit();

    climateRecords.add(firstClimateRecord);

    /*
     * Persistencia de registros de riego de prueba
     */
    IrrigationRecord firstIrrigationRecord = new IrrigationRecord();
    firstIrrigationRecord.setDate(firstDate);
    firstIrrigationRecord.setIrrigationDone(0.0);
    firstIrrigationRecord.setParcel(givenParcel);

    entityManager.getTransaction().begin();
    firstIrrigationRecord = irrigationRecordService.create(firstIrrigationRecord);
    entityManager.getTransaction().commit();

    irrigationRecords.add(firstIrrigationRecord);

    /*
     * El valor de esta variable se utiliza para sumar el
     * agua de riego de una cantidad numberDays de registros
     * de riego inmediatamente anteriores a la fecha actual
     */
    int numberDays = climateRecords.size();

    /*
     * Seccion de prueba
     */
    System.out.println("ETc del dia inmediatamente anterior a la fecha actual: " + climateRecordService.sumEtcPastDays(givenUser.getId(), givenParcel.getId()));
    System.out.println("Agua de lluvia del dia inmediatamente anterior a la fecha actual: " + climateRecordService.sumRainwaterPastDays(givenUser.getId(), givenParcel.getId()));
    System.out.println("Agua de riego del dia inmediatamente anterior a la fecha actual: " + irrigationRecordService
          .sumIrrigationWaterPastDays(givenUser.getId(), givenParcel.getId(), numberDays));
    System.out.println("Cantidad total de agua de riego de la fecha actual: " + irrigationRecordService.calculateTotalIrrigationWaterCurrentDate(givenParcel));
    System.out.println();
    
    double result = calculateIrrigationWaterNeedCurrentDate(givenUser.getId(), givenParcel);

    System.out.println("* Resultado devuelto por el metodo calculateIrrigationWaterNeedCurrentDate: " + result);
    System.out.println();

    assertTrue(result == 0.0);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testThreeCalculateIrrigationWaterNeed() {
    System.out.println("**************************** Prueba tres del metodo calculateIrrigationWaterNeed ****************************");
    System.out.println("Sea X una cantidad mayor a cero, el metodo calculateIrrigationWaterNeed de la clase WaterMath retorna");
    System.out.println("la necesidad de agua de riego en la fecha actual de un cultivo en desarrollo en funcion de la suma de");
    System.out.println("la ETc de X de dias anteriores a la fecha actual, la suma del agua de lluvia de X dias anteriores a la");
    System.out.println("fecha actual, la suma del agua de riego de X dias anteriores a la fecha actual y la cantidad total del");
    System.out.println("agua de riego de la fecha actual.");
    System.out.println();
    System.out.println("Este metodo devuelve una necesidad de agua de riego igual a cero si una de las sumas de agua o la suma de las");
    System.out.println("sumas de agua es mayor o igual a la suma de las ETc. En cambio, retorna una necesidad de agua de riego mayor a");
    System.out.println("cero si ninguna de las sumas de agua y ni la suma de las sumas de agua es mayor a la suma de las ETc.");
    System.out.println();
    System.out.println("Para demostrar el correcto funcionamiento de este metodo se utiliza un registro climatico del dia inmediatamente");
    System.out.println("anterior a la fecha actual, el cual tiene una ETc MAYOR a cero y una cantidad de agua de lluvia IGUAL a cero.");
    System.out.println("Tambien se utiliza un registro de riego del dia inmediatamente anterior a la fecha actual, el cual tiene una");
    System.out.println("cantidad de agua de riego MAYOR a la ETc. No se utiliza ningun registro de riego de la fecha actual, con lo cual");
    System.out.println("se asume que la cantidad total de agua de riego de la fecha actual es IGUAL a cero. Por lo tanto, la necesidad de");
    System.out.println("agua de riego en la fecha actual para un cultivo en desarrollo devuelta por el metodo calculateIrrigationWaterNeed");
    System.out.println("es IGUAL a cero.");
    System.out.println();

    /*
     * Persistencia de un usuario de prueba
     */
    User givenUser = new User();
    givenUser.setUsername("miller635");
    givenUser.setName("Audra");
    givenUser.setLastName("Miller");
    givenUser.setEmail("astrid@email.com");
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

    /*
     * Persistencia de registros climaticos de prueba
     */
    ClimateRecord firstClimateRecord = new ClimateRecord();
    firstClimateRecord.setDate(firstDate);
    firstClimateRecord.setEtc(0.4374347820832177);
    firstClimateRecord.setParcel(givenParcel);

    entityManager.getTransaction().begin();
    firstClimateRecord = climateRecordService.create(firstClimateRecord);
    entityManager.getTransaction().commit();

    climateRecords.add(firstClimateRecord);

    /*
     * Persistencia de registros de riego de prueba
     */
    IrrigationRecord firstIrrigationRecord = new IrrigationRecord();
    firstIrrigationRecord.setDate(firstDate);
    firstIrrigationRecord.setIrrigationDone(2);
    firstIrrigationRecord.setParcel(givenParcel);

    entityManager.getTransaction().begin();
    firstIrrigationRecord = irrigationRecordService.create(firstIrrigationRecord);
    entityManager.getTransaction().commit();

    irrigationRecords.add(firstIrrigationRecord);

    /*
     * El valor de esta variable se utiliza para sumar el
     * agua de riego de una cantidad numberDays de registros
     * de riego inmediatamente anteriores a la fecha actual
     */
    int numberDays = climateRecords.size();

    /*
     * Seccion de prueba
     */
    System.out.println("ETc del dia inmediatamente anterior a la fecha actual: " + climateRecordService.sumEtcPastDays(givenUser.getId(), givenParcel.getId()));
    System.out.println("Agua de lluvia del dia inmediatamente anterior a la fecha actual: " + climateRecordService.sumRainwaterPastDays(givenUser.getId(), givenParcel.getId()));
    System.out.println("Agua de riego del dia inmediatamente anterior a la fecha actual: " + irrigationRecordService
          .sumIrrigationWaterPastDays(givenUser.getId(), givenParcel.getId(), numberDays));
    System.out.println("Cantidad total de agua de riego de la fecha actual: " + irrigationRecordService.calculateTotalIrrigationWaterCurrentDate(givenParcel));
    System.out.println();
    
    double result = calculateIrrigationWaterNeedCurrentDate(givenUser.getId(), givenParcel);

    System.out.println("* Resultado devuelto por el metodo calculateIrrigationWaterNeedCurrentDate: " + result);
    System.out.println();

    assertTrue(result == 0.0);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testFourCalculateIrrigationWaterNeed() {
    System.out.println("**************************** Prueba cuatro del metodo calculateIrrigationWaterNeed ****************************");
    System.out.println("Sea X una cantidad mayor a cero, el metodo calculateIrrigationWaterNeed de la clase WaterMath retorna");
    System.out.println("la necesidad de agua de riego en la fecha actual de un cultivo en desarrollo en funcion de la suma de");
    System.out.println("la ETc de X de dias anteriores a la fecha actual, la suma del agua de lluvia de X dias anteriores a la");
    System.out.println("fecha actual, la suma del agua de riego de X dias anteriores a la fecha actual y la cantidad total del");
    System.out.println("agua de riego de la fecha actual.");
    System.out.println();
    System.out.println("Este metodo devuelve una necesidad de agua de riego igual a cero si una de las sumas de agua o la suma de las");
    System.out.println("sumas de agua es mayor o igual a la suma de las ETc. En cambio, retorna una necesidad de agua de riego mayor a");
    System.out.println("cero si ninguna de las sumas de agua y ni la suma de las sumas de agua es mayor a la suma de las ETc.");
    System.out.println();
    System.out.println("Para demostrar el correcto funcionamiento de este metodo se utiliza un registro climatico del dia inmediatamente");
    System.out.println("anterior a la fecha actual, el cual tiene una ETc MAYOR a cero y una cantidad de agua de lluvia IGUAL a cero.");
    System.out.println("Tambien se utiliza un registro de riego del dia inmediatamente anterior a la fecha actual, el cual tiene una");
    System.out.println("cantidad de agua de riego IGUAL a cero. Se utiliza un registro de riego de la fecha actual, el cual tiene una");
    System.out.println("cantidad total de agua de riego MAYOR a la ETc. En consecuencia, la cantidad total de agua de riego de la fecha");
    System.out.println("actual es MAYOR a la ETc. Por lo tanto, la necesidad de agua de riego en la fecha actual para un cultivo en");
    System.out.println("desarrollo devuelta por el metodo calculateIrrigationWaterNeed es IGUAL a cero.");
    System.out.println();

    /*
     * Persistencia de un usuario de prueba
     */
    User givenUser = new User();
    givenUser.setUsername("miller625");
    givenUser.setName("Audra");
    givenUser.setLastName("Miller");
    givenUser.setEmail("axil@email.com");
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

    /*
     * Persistencia de registros climaticos de prueba
     */
    ClimateRecord firstClimateRecord = new ClimateRecord();
    firstClimateRecord.setDate(firstDate);
    firstClimateRecord.setEtc(0.4374347820832177);
    firstClimateRecord.setParcel(givenParcel);

    entityManager.getTransaction().begin();
    firstClimateRecord = climateRecordService.create(firstClimateRecord);
    entityManager.getTransaction().commit();

    climateRecords.add(firstClimateRecord);

    /*
     * Persistencia de registros de riego de prueba
     */
    IrrigationRecord firstIrrigationRecord = new IrrigationRecord();
    firstIrrigationRecord.setDate(firstDate);
    firstIrrigationRecord.setParcel(givenParcel);

    IrrigationRecord currentIrrigationRecord = new IrrigationRecord();
    currentIrrigationRecord.setDate(UtilDate.getCurrentDate());
    currentIrrigationRecord.setParcel(givenParcel);
    currentIrrigationRecord.setIrrigationDone(2);

    entityManager.getTransaction().begin();
    firstIrrigationRecord = irrigationRecordService.create(firstIrrigationRecord);
    currentIrrigationRecord = irrigationRecordService.create(currentIrrigationRecord);
    entityManager.getTransaction().commit();

    irrigationRecords.add(firstIrrigationRecord);
    irrigationRecords.add(currentIrrigationRecord);

    /*
     * El valor de esta variable se utiliza para sumar el
     * agua de riego de una cantidad numberDays de registros
     * de riego inmediatamente anteriores a la fecha actual
     */
    int numberDays = climateRecords.size();

    /*
     * Seccion de prueba
     */
    System.out.println("ETc del dia inmediatamente anterior a la fecha actual: " + climateRecordService.sumEtcPastDays(givenUser.getId(), givenParcel.getId()));
    System.out.println("Agua de lluvia del dia inmediatamente anterior a la fecha actual: " + climateRecordService.sumRainwaterPastDays(givenUser.getId(), givenParcel.getId()));
    System.out.println("Agua de riego del dia inmediatamente anterior a la fecha actual: " + irrigationRecordService
          .sumIrrigationWaterPastDays(givenUser.getId(), givenParcel.getId(), numberDays));
    System.out.println("Cantidad total de agua de riego de la fecha actual: " + irrigationRecordService.calculateTotalIrrigationWaterCurrentDate(givenParcel));
    System.out.println();
    
    double result = calculateIrrigationWaterNeedCurrentDate(givenUser.getId(), givenParcel);

    System.out.println("* Resultado devuelto por el metodo calculateIrrigationWaterNeedCurrentDate: " + result);
    System.out.println();

    assertTrue(result == 0.0);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testFiveCalculateIrrigationWaterNeed() {
    System.out.println("**************************** Prueba cinco del metodo calculateIrrigationWaterNeed ****************************");
    System.out.println("Sea X una cantidad mayor a cero, el metodo calculateIrrigationWaterNeed de la clase WaterMath retorna");
    System.out.println("la necesidad de agua de riego en la fecha actual de un cultivo en desarrollo en funcion de la suma de");
    System.out.println("la ETc de X de dias anteriores a la fecha actual, la suma del agua de lluvia de X dias anteriores a la");
    System.out.println("fecha actual, la suma del agua de riego de X dias anteriores a la fecha actual y la cantidad total del");
    System.out.println("agua de riego de la fecha actual.");
    System.out.println();
    System.out.println("Este metodo devuelve una necesidad de agua de riego igual a cero si una de las sumas de agua o la suma de las");
    System.out.println("sumas de agua es mayor o igual a la suma de las ETc. En cambio, retorna una necesidad de agua de riego mayor a");
    System.out.println("cero si ninguna de las sumas de agua y ni la suma de las sumas de agua es mayor a la suma de las ETc.");
    System.out.println();
    System.out.println("Para demostrar el correcto funcionamiento de este metodo se utiliza un registro climatico del dia inmediatamente");
    System.out.println("anterior a la fecha actual, el cual tiene una ETc MAYOR a cero y una cantidad de agua de lluvia MAYOR a cero,");
    System.out.println("pero MENOR a la ETC. Tambien se utiliza un registro de riego del dia inmediatamente anterior a la fecha actual,");
    System.out.println("el cual tiene una cantidad de agua de riego MAYOR a cero, pero MENOR a la ETc. Se utiliza un registro de riego de");
    System.out.println("la fecha actual, el cual tiene una cantidad total de agua de riego MAYOR a cero, pero MENOR a la ETc. La suma entre");
    System.out.println("cada una de estas cantidades de agua da como resultado un numero MAYOR a la ETc. Por lo tanto, la necesidad de agua");
    System.out.println("de riego en la fecha actual para un cultivo en desarrollo devuelta por el metodo calculateIrrigationWaterNeed es IGUAL");
    System.out.println("a cero.");
    System.out.println();

    /*
     * Persistencia de un usuario de prueba
     */
    User givenUser = new User();
    givenUser.setUsername("miller6115");
    givenUser.setName("Audra");
    givenUser.setLastName("Miller");
    givenUser.setEmail("amil@email.com");
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

    /*
     * Persistencia de registros climaticos de prueba
     */
    ClimateRecord firstClimateRecord = new ClimateRecord();
    firstClimateRecord.setDate(firstDate);
    firstClimateRecord.setEtc(0.4374347820832177);
    firstClimateRecord.setPrecip(0.1);
    firstClimateRecord.setParcel(givenParcel);

    entityManager.getTransaction().begin();
    firstClimateRecord = climateRecordService.create(firstClimateRecord);
    entityManager.getTransaction().commit();

    climateRecords.add(firstClimateRecord);

    /*
     * Persistencia de registros de riego de prueba
     */
    IrrigationRecord firstIrrigationRecord = new IrrigationRecord();
    firstIrrigationRecord.setDate(firstDate);
    firstIrrigationRecord.setParcel(givenParcel);
    firstIrrigationRecord.setIrrigationDone(0.2);

    IrrigationRecord currentIrrigationRecord = new IrrigationRecord();
    currentIrrigationRecord.setDate(UtilDate.getCurrentDate());
    currentIrrigationRecord.setParcel(givenParcel);
    currentIrrigationRecord.setIrrigationDone(0.2);

    entityManager.getTransaction().begin();
    firstIrrigationRecord = irrigationRecordService.create(firstIrrigationRecord);
    currentIrrigationRecord = irrigationRecordService.create(currentIrrigationRecord);
    entityManager.getTransaction().commit();

    irrigationRecords.add(firstIrrigationRecord);
    irrigationRecords.add(currentIrrigationRecord);

    /*
     * El valor de esta variable se utiliza para sumar el
     * agua de riego de una cantidad numberDays de registros
     * de riego inmediatamente anteriores a la fecha actual
     */
    int numberDays = climateRecords.size();

    /*
     * Seccion de prueba
     */
    System.out.println("ETc del dia inmediatamente anterior a la fecha actual: " + climateRecordService.sumEtcPastDays(givenUser.getId(), givenParcel.getId()));
    System.out.println("Agua de lluvia del dia inmediatamente anterior a la fecha actual: " + climateRecordService.sumRainwaterPastDays(givenUser.getId(), givenParcel.getId()));
    System.out.println("Agua de riego del dia inmediatamente anterior a la fecha actual: " + irrigationRecordService
          .sumIrrigationWaterPastDays(givenUser.getId(), givenParcel.getId(), numberDays));
    System.out.println("Cantidad total de agua de riego de la fecha actual: " + irrigationRecordService.calculateTotalIrrigationWaterCurrentDate(givenParcel));
    System.out.println();
    
    double result = calculateIrrigationWaterNeedCurrentDate(givenUser.getId(), givenParcel);

    System.out.println("* Resultado devuelto por el metodo calculateIrrigationWaterNeedCurrentDate: " + result);
    System.out.println();

    assertTrue(result == 0.0);

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

    for (ClimateRecord currenClimateRecord : climateRecords) {
      climateRecordService.remove(currenClimateRecord.getId());
    }

    entityManager.getTransaction().commit();

    // Cierra las conexiones
    entityManager.close();
    entityManagerFactory.close();
  }

  /**
   * Calcula la necesidad de agua de riego de un cultivo en
   * desarrollo en la fecha actual en funcion de la suma de
   * la ETc de NUMBER_DAYS dias anteriores a la fecha actual,
   * la suma del agua de lluvia de NUMBER_DAYS dias anteriores
   * a la fecha actual, la suma del agua de riego de NUMBER_DAYS
   * dias anteriores a la fecha actual y la cantidad total del
   * agua de riego de la fecha actual
   * 
   * @param developingPlantingRecord
   * @return double que representa la necesidad de agua de
   * riego en la fecha actual de un cultivo en desarrollo
   */
  private double calculateIrrigationWaterNeedCurrentDate(int userId, Parcel givenParcel) {
    double etcSummedPastDays = climateRecordService.sumEtcPastDays(userId, givenParcel.getId());
    double summedRainwaterPastDays = climateRecordService.sumRainwaterPastDays(userId, givenParcel.getId());
    double summedIrrigationWaterPastDays = irrigationRecordService.sumIrrigationWaterPastDays(userId, givenParcel.getId(), climateRecordService.getNumberDays());
    double totalIrrigationWaterCurrentDate = irrigationRecordService.calculateTotalIrrigationWaterCurrentDate(givenParcel);

    return WaterMath.calculateIrrigationWaterNeed(etcSummedPastDays, summedRainwaterPastDays, summedIrrigationWaterPastDays, totalIrrigationWaterCurrentDate);
  }

}
