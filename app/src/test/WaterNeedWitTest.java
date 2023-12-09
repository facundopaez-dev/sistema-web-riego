import static org.junit.Assert.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.Calendar;
import java.util.ArrayList;
import java.util.Collection;
import model.ClimateRecord;
import model.IrrigationRecord;
import model.Parcel;
import model.User;
import model.Option;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import irrigation.WaterMath;
import irrigation.WaterNeedWit;
import util.UtilDate;
import stateless.UserServiceBean;
import stateless.OptionServiceBean;
import stateless.ParcelServiceBean;
import stateless.ClimateRecordServiceBean;
import stateless.IrrigationRecordServiceBean;

public class WaterNeedWitTest {

  private static EntityManager entityManager;
  private static EntityManagerFactory entityManagerFactory;
  private static UserServiceBean userService;
  private static OptionServiceBean optionService;
  private static ParcelServiceBean parcelService;
  private static ClimateRecordServiceBean climateRecordService;
  private static IrrigationRecordServiceBean irrigationRecordService;

  private static Collection<IrrigationRecord> zeroIrrigationRecords;
  private static Collection<ClimateRecord> climateRecords;
  private static Collection<ClimateRecord> climateRecordsToBeDeleted;
  private static Collection<IrrigationRecord> irrigationRecordsToBeDeleted;
  private static Collection<Parcel> parcels;
  private static Collection<User> users;
  private static Collection<Option> options;

  private static Calendar presumedCurrentDate;
  private static Calendar dayOne;
  private static Calendar dayTwo;
  private static Calendar dayThree;
  private static Calendar dayFour;
  private static Calendar dayFive;
  private static Calendar daySix;

  private static ClimateRecord climateRecordOne;
  private static ClimateRecord climateRecordTwo;
  private static ClimateRecord climateRecordThree;
  private static ClimateRecord climateRecordFour;
  private static ClimateRecord climateRecordFive;
  private static ClimateRecord climateRecordSix;

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

    userService = new UserServiceBean();
    userService.setEntityManager(entityManager);

    optionService = new OptionServiceBean();
    optionService.setEntityManager(entityManager);

    parcelService = new ParcelServiceBean();
    parcelService.setEntityManager(entityManager);

    climateRecordService = new ClimateRecordServiceBean();
    climateRecordService.setEntityManager(entityManager);

    irrigationRecordService = new IrrigationRecordServiceBean();
    irrigationRecordService.setEntityManager(entityManager);

    zeroIrrigationRecords = new ArrayList<>();
    climateRecords = new ArrayList<>();
    climateRecordsToBeDeleted = new ArrayList<>();
    irrigationRecordsToBeDeleted = new ArrayList<>();
    parcels = new ArrayList<>();
    users = new ArrayList<>();
    options = new ArrayList<>();

    climateRecordOne = new ClimateRecord();
    climateRecordTwo = new ClimateRecord();
    climateRecordThree = new ClimateRecord();
    climateRecordFour = new ClimateRecord();
    climateRecordFive = new ClimateRecord();
    climateRecordSix = new ClimateRecord();

    climateRecords.add(climateRecordOne);
    climateRecords.add(climateRecordTwo);
    climateRecords.add(climateRecordThree);
    climateRecords.add(climateRecordFour);
    climateRecords.add(climateRecordFive);
    climateRecords.add(climateRecordSix);

    presumedCurrentDate = Calendar.getInstance();
    presumedCurrentDate.set(Calendar.YEAR, 2023);
    presumedCurrentDate.set(Calendar.MONTH, JANUARY);
    presumedCurrentDate.set(Calendar.DAY_OF_MONTH, 7);

    /*
     * Creacion de fechas para los registros climaticos
     */
    dayOne = Calendar.getInstance();
    dayOne.set(Calendar.YEAR, 2023);
    dayOne.set(Calendar.MONTH, JANUARY);
    dayOne.set(Calendar.DAY_OF_MONTH, 1);

    dayTwo = Calendar.getInstance();
    dayTwo.set(Calendar.YEAR, 2023);
    dayTwo.set(Calendar.MONTH, JANUARY);
    dayTwo.set(Calendar.DAY_OF_MONTH, 2);

    dayThree = Calendar.getInstance();
    dayThree.set(Calendar.YEAR, 2023);
    dayThree.set(Calendar.MONTH, JANUARY);
    dayThree.set(Calendar.DAY_OF_MONTH, 3);

    dayFour = Calendar.getInstance();
    dayFour.set(Calendar.YEAR, 2023);
    dayFour.set(Calendar.MONTH, JANUARY);
    dayFour.set(Calendar.DAY_OF_MONTH, 4);

    dayFive = Calendar.getInstance();
    dayFive.set(Calendar.YEAR, 2023);
    dayFive.set(Calendar.MONTH, JANUARY);
    dayFive.set(Calendar.DAY_OF_MONTH, 5);

    daySix = Calendar.getInstance();
    daySix.set(Calendar.YEAR, 2023);
    daySix.set(Calendar.MONTH, JANUARY);
    daySix.set(Calendar.DAY_OF_MONTH, 6);
  }

  @Test
  public void testOneCalculateIrrigationWaterNeed() {
    System.out.println("************************************** Prueba uno del metodo calculateIrrigationWaterNeed ***************************************");
    printDescriptionMethodToTest();

    System.out.println("# Descripcion de la prueba unitaria");

    /*
     * Establece la ETc, la lluvia y la fecha de
     * 6 registros climaticos para esta prueba
     * unitaria
     */
    setClimateRecordsValuesOne();

    System.out.println("Los datos con los que se calculara la necesidad de agua de riego de un cultivo en la fecha actual (" + UtilDate.formatDate(presumedCurrentDate) + ") son los siguientes:");
    System.out.println();

    printWaterBalanceTable(presumedCurrentDate, climateRecords, zeroIrrigationRecords);

    double totalIrrigationWaterCurrentDate = 0.0;

    printAccumulatedWaterDeficitPerDay(presumedCurrentDate, climateRecords, zeroIrrigationRecords);
    printTotalIrrigationWaterCurrentDate(presumedCurrentDate, totalIrrigationWaterCurrentDate);

    System.out.println();
    System.out.println("Al ser el resultado de la suma entre el acumulado del deficit de agua por dia de dias previos a la fecha actual y la cantidad");
    System.out.println("total de agua de riego de la fecha actual, mayor o igual a 0 (cero), el valor devuelto por el metodo calculateIrrigationWaterNeed");
    System.out.println("de la clase WaterNeedWit debe ser 0 [mm/dia]. Por lo tanto, la necesidad de agua de riego de un cultivo en la fecha actual es 0");
    System.out.println("[mm/dia].");
    System.out.println();

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");

    double expectedResult = 0.0;
    double result = WaterNeedWit.calculateIrrigationWaterNeed(totalIrrigationWaterCurrentDate, climateRecords, zeroIrrigationRecords);

    System.out.println("* Valor esperado (nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateIrrigationWaterNeed (nec. agua riego");
    System.out.println("[mm/dia] de un cultivo en fecha actual): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testTwoCalculateIrrigationWaterNeed() {
    System.out.println("************************************** Prueba dos del metodo calculateIrrigationWaterNeed ***************************************");
    printDescriptionMethodToTest();

    System.out.println("# Descripcion de la prueba unitaria");

    /*
     * Establece la ETc, la lluvia y la fecha de
     * 6 registros climaticos para esta prueba
     * unitaria
     */
    setClimateRecordsValuesTwo();

    System.out.println("Los datos con los que se calculara la necesidad de agua de riego de un cultivo en la fecha actual (" + UtilDate.formatDate(presumedCurrentDate) + ") son los siguientes:");
    System.out.println();

    printWaterBalanceTable(presumedCurrentDate, climateRecords, zeroIrrigationRecords);

    double totalIrrigationWaterCurrentDate = 5.0;

    printAccumulatedWaterDeficitPerDay(presumedCurrentDate, climateRecords, zeroIrrigationRecords);
    printTotalIrrigationWaterCurrentDate(presumedCurrentDate, totalIrrigationWaterCurrentDate);

    System.out.println();
    System.out.println("Al ser el resultado de la suma entre el acumulado del deficit de agua por dia de dias previos a la fecha actual y la cantidad");
    System.out.println("total de agua de riego de la fecha actual, mayor o igual a 0 (cero), el valor devuelto por el metodo calculateIrrigationWaterNeed");
    System.out.println("de la clase WaterNeedWit debe ser 0 [mm/dia]. Por lo tanto, la necesidad de agua de riego de un cultivo en la fecha actual es 0");
    System.out.println("[mm/dia].");
    System.out.println();

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");

    double expectedResult = 0.0;
    double result = WaterNeedWit.calculateIrrigationWaterNeed(totalIrrigationWaterCurrentDate, climateRecords, zeroIrrigationRecords);

    System.out.println("* Valor esperado (nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateIrrigationWaterNeed (nec. agua riego");
    System.out.println("[mm/dia] de un cultivo en fecha actual): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testThreeCalculateIrrigationWaterNeed() {
    System.out.println("************************************** Prueba tres del metodo calculateIrrigationWaterNeed ***************************************");
    printDescriptionMethodToTest();

    System.out.println("# Descripcion de la prueba unitaria");

    /*
     * Establece la ETc, la lluvia y la fecha de
     * 6 registros climaticos para esta prueba
     * unitaria
     */
    setClimateRecordsValuesThree();

    System.out.println("Los datos con los que se calculara la necesidad de agua de riego de un cultivo en la fecha actual (" + UtilDate.formatDate(presumedCurrentDate) + ") son los siguientes:");
    System.out.println();

    printWaterBalanceTable(presumedCurrentDate, climateRecords, zeroIrrigationRecords);

    double totalIrrigationWaterCurrentDate = 14.0;

    printAccumulatedWaterDeficitPerDay(presumedCurrentDate, climateRecords, zeroIrrigationRecords);
    printTotalIrrigationWaterCurrentDate(presumedCurrentDate, totalIrrigationWaterCurrentDate);

    System.out.println();
    System.out.println("Al ser el resultado de la suma entre el acumulado del deficit de agua por dia de dias previos a la fecha actual y la cantidad");
    System.out.println("total de agua de riego de la fecha actual, mayor o igual a 0 (cero), el valor devuelto por el metodo calculateIrrigationWaterNeed");
    System.out.println("de la clase WaterNeedWit debe ser 0 [mm/dia]. Por lo tanto, la necesidad de agua de riego de un cultivo en la fecha actual es 0");
    System.out.println("[mm/dia].");
    System.out.println();

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");

    double expectedResult = 0.0;
    double result = WaterNeedWit.calculateIrrigationWaterNeed(totalIrrigationWaterCurrentDate, climateRecords, zeroIrrigationRecords);

    System.out.println("* Valor esperado (nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateIrrigationWaterNeed (nec. agua riego");
    System.out.println("[mm/dia] de un cultivo en fecha actual): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testFourCalculateIrrigationWaterNeed() {
    System.out.println("************************************** Prueba cuatro del metodo calculateIrrigationWaterNeed ***************************************");
    printDescriptionMethodToTest();

    System.out.println("# Descripcion de la prueba unitaria");

    /*
     * Establece la ETc, la lluvia y la fecha de
     * 6 registros climaticos para esta prueba
     * unitaria
     */
    setClimateRecordsValuesFour();

    System.out.println("Los datos con los que se calculara la necesidad de agua de riego de un cultivo en la fecha actual (" + UtilDate.formatDate(presumedCurrentDate) + ") son los siguientes:");
    System.out.println();

    printWaterBalanceTable(presumedCurrentDate, climateRecords, zeroIrrigationRecords);

    double totalIrrigationWaterCurrentDate = 21.0;

    printAccumulatedWaterDeficitPerDay(presumedCurrentDate, climateRecords, zeroIrrigationRecords);
    printTotalIrrigationWaterCurrentDate(presumedCurrentDate, totalIrrigationWaterCurrentDate);

    System.out.println();
    System.out.println("Al ser el resultado de la suma entre el acumulado del deficit de agua por dia de dias previos a la fecha actual y la cantidad");
    System.out.println("total de agua de riego de la fecha actual, mayor o igual a 0 (cero), el valor devuelto por el metodo calculateIrrigationWaterNeed");
    System.out.println("de la clase WaterNeedWit debe ser 0 [mm/dia]. Por lo tanto, la necesidad de agua de riego de un cultivo en la fecha actual es 0");
    System.out.println("[mm/dia].");
    System.out.println();

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");

    double expectedResult = 0.0;
    double result = WaterNeedWit.calculateIrrigationWaterNeed(totalIrrigationWaterCurrentDate, climateRecords, zeroIrrigationRecords);

    System.out.println("* Valor esperado (nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateIrrigationWaterNeed (nec. agua riego");
    System.out.println("[mm/dia] de un cultivo en fecha actual): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testFiveCalculateIrrigationWaterNeed() {
    System.out.println("************************************** Prueba cinco del metodo calculateIrrigationWaterNeed ***************************************");
    printDescriptionMethodToTest();

    System.out.println("# Descripcion de la prueba unitaria");

    /*
     * Establece la ETc, la lluvia y la fecha de
     * 6 registros climaticos para esta prueba
     * unitaria
     */
    setClimateRecordsValuesFive();

    System.out.println("Los datos con los que se calculara la necesidad de agua de riego de un cultivo en la fecha actual (" + UtilDate.formatDate(presumedCurrentDate) + ") son los siguientes:");
    System.out.println();

    printWaterBalanceTable(presumedCurrentDate, climateRecords, zeroIrrigationRecords);

    double totalIrrigationWaterCurrentDate = 7.0;

    printAccumulatedWaterDeficitPerDay(presumedCurrentDate, climateRecords, zeroIrrigationRecords);
    printTotalIrrigationWaterCurrentDate(presumedCurrentDate, totalIrrigationWaterCurrentDate);

    System.out.println();
    System.out.println("Al ser el resultado de la suma entre el acumulado del deficit de agua por dia de dias previos a la fecha actual y la cantidad");
    System.out.println("total de agua de riego de la fecha actual, estrictamente menor a 0 (cero), el valor devuelto por el metodo calculateIrrigationWaterNeed");
    System.out.println("de la clase WaterNeedWit debe ser el valor absoluto del resultado de dicha suma [mm/dia]. Por lo tanto, la necesidad de agua");
    System.out.println("de riego de un cultivo en la fecha actual es mayor o igual a 0 (cero) [mm/dia].");
    System.out.println();

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");

    double expectedResult = 7.0;
    double result = WaterNeedWit.calculateIrrigationWaterNeed(totalIrrigationWaterCurrentDate, climateRecords, zeroIrrigationRecords);

    System.out.println("* Valor esperado (nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateIrrigationWaterNeed (nec. agua riego");
    System.out.println("[mm/dia] de un cultivo en fecha actual): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  /*
   * ****************************************************************
   * A partir de aca comienzan las pruebas unitarias del metodo
   * calculateIrrigationWaterNeed de la clase NeedWaterWit haciendo
   * uso del metodo findAllByParcelIdAndPeriod de la clase
   * ClimateRecordServiceBean. El motivo de esto es que el metodo
   * calculateIrrigationWaterNeed hasta ahora NO fue probado con el
   * metodo findAllByParcelIdAndPeriod.
   * ****************************************************************
   */

  @Test
  public void testSixCalculateIrrigationWaterNeed() {
    System.out.println("************************************** Prueba seis del metodo calculateIrrigationWaterNeed ***************************************");
    printDescriptionMethodToTest();

    System.out.println("# Descripcion de la prueba unitaria");
    System.out.println("Para esta prueba se utilizan 6 registros climaticos previos a la fecha actual " + UtilDate.formatDate(presumedCurrentDate) + ".");
    System.out.println("Suponemos que la fecha actual es " + UtilDate.formatDate(presumedCurrentDate) + ".");
    System.out.println();

    /*
     * Fechas a partir de las cuales se recuperaran los
     * registros climaticos y los registros de riego de
     * una parcela de prueba de la base de datos subyacente
     */
    Calendar dateFrom = Calendar.getInstance();
    dateFrom.set(Calendar.YEAR, 2023);
    dateFrom.set(Calendar.MONTH, JANUARY);
    dateFrom.set(Calendar.DAY_OF_MONTH, 1);

    Calendar dateUntil = Calendar.getInstance();
    dateUntil.set(Calendar.YEAR, 2023);
    dateUntil.set(Calendar.MONTH, JANUARY);
    dateUntil.set(Calendar.DAY_OF_MONTH, 6);

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
    givenUser.setUsername("giovanni");
    givenUser.setName("Giovanni");
    givenUser.setLastName("Auditore");
    givenUser.setEmail("giovanni@eservice.com");
    givenUser.setPassword("Giovanni");

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
     * Persistencia de registros climaticos de prueba
     */
    ClimateRecord climateRecordOne = new ClimateRecord();
    climateRecordOne.setEtc(10);
    climateRecordOne.setPrecip(5);
    climateRecordOne.setDate(dayOne);
    climateRecordOne.setParcel(givenParcel);

    ClimateRecord climateRecordTwo = new ClimateRecord();
    climateRecordTwo.setEtc(5);
    climateRecordTwo.setPrecip(2);
    climateRecordTwo.setDate(dayTwo);
    climateRecordTwo.setParcel(givenParcel);

    ClimateRecord climateRecordThree = new ClimateRecord();
    climateRecordThree.setEtc(2);
    climateRecordThree.setPrecip(1);
    climateRecordThree.setDate(dayThree);
    climateRecordThree.setParcel(givenParcel);

    ClimateRecord climateRecordFour = new ClimateRecord();
    climateRecordFour.setEtc(3);
    climateRecordFour.setPrecip(1);
    climateRecordFour.setDate(dayFour);
    climateRecordFour.setParcel(givenParcel);

    ClimateRecord climateRecordFive = new ClimateRecord();
    climateRecordFive.setEtc(1);
    climateRecordFive.setPrecip(0);
    climateRecordFive.setDate(dayFive);
    climateRecordFive.setParcel(givenParcel);

    ClimateRecord climateRecordSix = new ClimateRecord();
    climateRecordSix.setEtc(4);
    climateRecordSix.setPrecip(2);
    climateRecordSix.setDate(daySix);
    climateRecordSix.setParcel(givenParcel);

    entityManager.getTransaction().begin();
    climateRecordOne = climateRecordService.create(climateRecordOne);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordTwo = climateRecordService.create(climateRecordTwo);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordThree = climateRecordService.create(climateRecordThree);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordFour = climateRecordService.create(climateRecordFour);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordFive = climateRecordService.create(climateRecordFive);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordSix = climateRecordService.create(climateRecordSix);
    entityManager.getTransaction().commit();

    climateRecordsToBeDeleted.add(climateRecordOne);
    climateRecordsToBeDeleted.add(climateRecordTwo);
    climateRecordsToBeDeleted.add(climateRecordThree);
    climateRecordsToBeDeleted.add(climateRecordFour);
    climateRecordsToBeDeleted.add(climateRecordFive);
    climateRecordsToBeDeleted.add(climateRecordSix);

    /*
     * Recupera los registros climaticos recientemente
     * persistidos
     */
    Collection<ClimateRecord> recoveredClimateRecords = climateRecordService.findAllByParcelIdAndPeriod(givenUser.getId(), givenParcel.getId(), dateFrom, dateUntil);

    System.out.println("Los datos con los que se calculara la necesidad de agua de riego de un cultivo en la fecha actual (" + UtilDate.formatDate(presumedCurrentDate) + ") son los siguientes:");
    System.out.println();

    printWaterBalanceTable(presumedCurrentDate, recoveredClimateRecords, zeroIrrigationRecords);

    double totalIrrigationWaterCurrentDate = 0.0;

    printAccumulatedWaterDeficitPerDay(presumedCurrentDate, recoveredClimateRecords, zeroIrrigationRecords);
    printTotalIrrigationWaterCurrentDate(presumedCurrentDate, totalIrrigationWaterCurrentDate);

    System.out.println();
    System.out.println("Al ser el resultado de la suma entre el acumulado del deficit de agua por dia de dias previos a la fecha actual y la cantidad");
    System.out.println("total de agua de riego de la fecha actual, estrictamente menor a 0 (cero), el valor devuelto por el metodo calculateIrrigationWaterNeed");
    System.out.println("de la clase WaterNeedWit debe ser el valor absoluto del resultado de dicha suma [mm/dia]. Por lo tanto, la necesidad de agua");
    System.out.println("de riego de un cultivo en la fecha actual es mayor o igual a 0 (cero) [mm/dia].");
    System.out.println();

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");

    double expectedResult = 14.0;

    /*
     * El primer parametro de este metodo calculateIrrigationWaterNeed
     * es la cantidad total de agua de riego de una fecha dada. En este
     * caso, se le pasa el valor 0 como argumento porque suponemos que
     * la cantidad total de agua de riego de la supuesta fecha actual
     * es 0 para facilitar la tarea de probarlo.
     */
    double result = WaterNeedWit.calculateIrrigationWaterNeed(totalIrrigationWaterCurrentDate, recoveredClimateRecords, zeroIrrigationRecords);

    System.out.println("* Valor esperado (nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateIrrigationWaterNeed (nec. agua riego");
    System.out.println("[mm/dia] de un cultivo en fecha actual): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testSevenCalculateIrrigationWaterNeed() {
    System.out.println("************************************** Prueba siete del metodo calculateIrrigationWaterNeed ***************************************");
    printDescriptionMethodToTest();

    System.out.println("# Descripcion de la prueba unitaria");
    System.out.println("Para esta prueba se utilizan 6 registros climaticos previos a la fecha actual " + UtilDate.formatDate(presumedCurrentDate) + ".");
    System.out.println("Suponemos que la fecha actual es " + UtilDate.formatDate(presumedCurrentDate) + ".");
    System.out.println();

    /*
     * Fechas a partir de las cuales se recuperaran los
     * registros climaticos y los registros de riego de
     * una parcela de prueba de la base de datos subyacente
     */
    Calendar dateFrom = Calendar.getInstance();
    dateFrom.set(Calendar.YEAR, 2023);
    dateFrom.set(Calendar.MONTH, JANUARY);
    dateFrom.set(Calendar.DAY_OF_MONTH, 1);

    Calendar dateUntil = Calendar.getInstance();
    dateUntil.set(Calendar.YEAR, 2023);
    dateUntil.set(Calendar.MONTH, JANUARY);
    dateUntil.set(Calendar.DAY_OF_MONTH, 6);

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
    givenUser.setUsername("alyx");
    givenUser.setName("Alyx");
    givenUser.setLastName("Vance");
    givenUser.setEmail("alyx@eservice.com");
    givenUser.setPassword("Alyx");

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
     * Persistencia de registros climaticos de prueba
     */
    ClimateRecord climateRecordOne = new ClimateRecord();
    climateRecordOne.setEtc(10);
    climateRecordOne.setPrecip(5);
    climateRecordOne.setDate(dayOne);
    climateRecordOne.setParcel(givenParcel);

    ClimateRecord climateRecordTwo = new ClimateRecord();
    climateRecordTwo.setEtc(5);
    climateRecordTwo.setPrecip(5);
    climateRecordTwo.setDate(dayTwo);
    climateRecordTwo.setParcel(givenParcel);

    ClimateRecord climateRecordThree = new ClimateRecord();
    climateRecordThree.setEtc(2);
    climateRecordThree.setPrecip(1);
    climateRecordThree.setDate(dayThree);
    climateRecordThree.setParcel(givenParcel);

    ClimateRecord climateRecordFour = new ClimateRecord();
    climateRecordFour.setEtc(3);
    climateRecordFour.setPrecip(3);
    climateRecordFour.setDate(dayFour);
    climateRecordFour.setParcel(givenParcel);

    ClimateRecord climateRecordFive = new ClimateRecord();
    climateRecordFive.setEtc(4);
    climateRecordFive.setPrecip(1);
    climateRecordFive.setDate(dayFive);
    climateRecordFive.setParcel(givenParcel);

    ClimateRecord climateRecordSix = new ClimateRecord();
    climateRecordSix.setEtc(2);
    climateRecordSix.setPrecip(2);
    climateRecordSix.setDate(daySix);
    climateRecordSix.setParcel(givenParcel);

    entityManager.getTransaction().begin();
    climateRecordOne = climateRecordService.create(climateRecordOne);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordTwo = climateRecordService.create(climateRecordTwo);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordThree = climateRecordService.create(climateRecordThree);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordFour = climateRecordService.create(climateRecordFour);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordFive = climateRecordService.create(climateRecordFive);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordSix = climateRecordService.create(climateRecordSix);
    entityManager.getTransaction().commit();

    climateRecordsToBeDeleted.add(climateRecordOne);
    climateRecordsToBeDeleted.add(climateRecordTwo);
    climateRecordsToBeDeleted.add(climateRecordThree);
    climateRecordsToBeDeleted.add(climateRecordFour);
    climateRecordsToBeDeleted.add(climateRecordFive);
    climateRecordsToBeDeleted.add(climateRecordSix);

    /*
     * Recupera los registros climaticos recientemente
     * persistidos
     */
    Collection<ClimateRecord> recoveredClimateRecords = climateRecordService.findAllByParcelIdAndPeriod(givenUser.getId(), givenParcel.getId(), dateFrom, dateUntil);

    System.out.println("Los datos con los que se calculara la necesidad de agua de riego de un cultivo en la fecha actual (" + UtilDate.formatDate(presumedCurrentDate) + ") son los siguientes:");
    System.out.println();

    printWaterBalanceTable(presumedCurrentDate, recoveredClimateRecords, zeroIrrigationRecords);

    double totalIrrigationWaterCurrentDate = 0.0;

    printAccumulatedWaterDeficitPerDay(presumedCurrentDate, recoveredClimateRecords, zeroIrrigationRecords);
    printTotalIrrigationWaterCurrentDate(presumedCurrentDate, totalIrrigationWaterCurrentDate);

    System.out.println();
    System.out.println("Al ser el resultado de la suma entre el acumulado del deficit de agua por dia de dias previos a la fecha actual y la cantidad");
    System.out.println("total de agua de riego de la fecha actual, estrictamente menor a 0 (cero), el valor devuelto por el metodo calculateIrrigationWaterNeed");
    System.out.println("de la clase WaterNeedWit debe ser el valor absoluto del resultado de dicha suma [mm/dia]. Por lo tanto, la necesidad de agua");
    System.out.println("de riego de un cultivo en la fecha actual es mayor o igual a 0 (cero) [mm/dia].");
    System.out.println();

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");

    double expectedResult = 9.0;

    /*
     * El primer parametro de este metodo calculateIrrigationWaterNeed
     * es la cantidad total de agua de riego de una fecha dada. En este
     * caso, se le pasa el valor 0 como argumento porque suponemos que
     * la cantidad total de agua de riego de la supuesta fecha actual
     * es 0 para facilitar la tarea de probarlo.
     */
    double result = WaterNeedWit.calculateIrrigationWaterNeed(totalIrrigationWaterCurrentDate, recoveredClimateRecords, zeroIrrigationRecords);

    System.out.println("* Valor esperado (nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateIrrigationWaterNeed (nec. agua riego");
    System.out.println("[mm/dia] de un cultivo en fecha actual): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testEightCalculateIrrigationWaterNeed() {
    System.out.println("************************************** Prueba ocho del metodo calculateIrrigationWaterNeed ***************************************");
    printDescriptionMethodToTest();

    System.out.println("# Descripcion de la prueba unitaria");
    System.out.println("Para esta prueba se utilizan 6 registros climaticos previos a la fecha actual " + UtilDate.formatDate(presumedCurrentDate) + ".");
    System.out.println("Suponemos que la fecha actual es " + UtilDate.formatDate(presumedCurrentDate) + ".");
    System.out.println();

    /*
     * Fechas a partir de las cuales se recuperaran los
     * registros climaticos y los registros de riego de
     * una parcela de prueba de la base de datos subyacente
     */
    Calendar dateFrom = Calendar.getInstance();
    dateFrom.set(Calendar.YEAR, 2023);
    dateFrom.set(Calendar.MONTH, JANUARY);
    dateFrom.set(Calendar.DAY_OF_MONTH, 1);

    Calendar dateUntil = Calendar.getInstance();
    dateUntil.set(Calendar.YEAR, 2023);
    dateUntil.set(Calendar.MONTH, JANUARY);
    dateUntil.set(Calendar.DAY_OF_MONTH, 6);

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
    givenUser.setUsername("eli");
    givenUser.setName("Eli");
    givenUser.setLastName("Vance");
    givenUser.setEmail("eli@eservice.com");
    givenUser.setPassword("Eli");

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
     * Persistencia de registros climaticos de prueba
     */
    ClimateRecord climateRecordOne = new ClimateRecord();
    climateRecordOne.setEtc(10);
    climateRecordOne.setPrecip(5);
    climateRecordOne.setDate(dayOne);
    climateRecordOne.setParcel(givenParcel);

    ClimateRecord climateRecordTwo = new ClimateRecord();
    climateRecordTwo.setEtc(5);
    climateRecordTwo.setPrecip(5);
    climateRecordTwo.setDate(dayTwo);
    climateRecordTwo.setParcel(givenParcel);

    ClimateRecord climateRecordThree = new ClimateRecord();
    climateRecordThree.setEtc(2);
    climateRecordThree.setPrecip(1);
    climateRecordThree.setDate(dayThree);
    climateRecordThree.setParcel(givenParcel);

    ClimateRecord climateRecordFour = new ClimateRecord();
    climateRecordFour.setEtc(3);
    climateRecordFour.setPrecip(3);
    climateRecordFour.setDate(dayFour);
    climateRecordFour.setParcel(givenParcel);

    ClimateRecord climateRecordFive = new ClimateRecord();
    climateRecordFive.setEtc(4);
    climateRecordFive.setPrecip(5);
    climateRecordFive.setDate(dayFive);
    climateRecordFive.setParcel(givenParcel);

    ClimateRecord climateRecordSix = new ClimateRecord();
    climateRecordSix.setEtc(2);
    climateRecordSix.setPrecip(2);
    climateRecordSix.setDate(daySix);
    climateRecordSix.setParcel(givenParcel);

    entityManager.getTransaction().begin();
    climateRecordOne = climateRecordService.create(climateRecordOne);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordTwo = climateRecordService.create(climateRecordTwo);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordThree = climateRecordService.create(climateRecordThree);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordFour = climateRecordService.create(climateRecordFour);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordFive = climateRecordService.create(climateRecordFive);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordSix = climateRecordService.create(climateRecordSix);
    entityManager.getTransaction().commit();

    climateRecordsToBeDeleted.add(climateRecordOne);
    climateRecordsToBeDeleted.add(climateRecordTwo);
    climateRecordsToBeDeleted.add(climateRecordThree);
    climateRecordsToBeDeleted.add(climateRecordFour);
    climateRecordsToBeDeleted.add(climateRecordFive);
    climateRecordsToBeDeleted.add(climateRecordSix);

    /*
     * Recupera los registros climaticos recientemente
     * persistidos
     */
    Collection<ClimateRecord> recoveredClimateRecords = climateRecordService.findAllByParcelIdAndPeriod(givenUser.getId(), givenParcel.getId(), dateFrom, dateUntil);

    System.out.println("Los datos con los que se calculara la necesidad de agua de riego de un cultivo en la fecha actual (" + UtilDate.formatDate(presumedCurrentDate) + ") son los siguientes:");
    System.out.println();

    printWaterBalanceTable(presumedCurrentDate, recoveredClimateRecords, zeroIrrigationRecords);

    double totalIrrigationWaterCurrentDate = 0.0;

    printAccumulatedWaterDeficitPerDay(presumedCurrentDate, recoveredClimateRecords, zeroIrrigationRecords);
    printTotalIrrigationWaterCurrentDate(presumedCurrentDate, totalIrrigationWaterCurrentDate);

    System.out.println();
    System.out.println("Al ser el resultado de la suma entre el acumulado del deficit de agua por dia de dias previos a la fecha actual y la cantidad");
    System.out.println("total de agua de riego de la fecha actual, estrictamente menor a 0 (cero), el valor devuelto por el metodo calculateIrrigationWaterNeed");
    System.out.println("de la clase WaterNeedWit debe ser el valor absoluto del resultado de dicha suma [mm/dia]. Por lo tanto, la necesidad de agua");
    System.out.println("de riego de un cultivo en la fecha actual es mayor o igual a 0 (cero) [mm/dia].");
    System.out.println();

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");

    double expectedResult = 5.0;

    /*
     * El primer parametro de este metodo calculateIrrigationWaterNeed
     * es la cantidad total de agua de riego de una fecha dada. En este
     * caso, se le pasa el valor 0 como argumento porque suponemos que
     * la cantidad total de agua de riego de la supuesta fecha actual
     * es 0 para facilitar la tarea de probarlo.
     */
    double result = WaterNeedWit.calculateIrrigationWaterNeed(totalIrrigationWaterCurrentDate, recoveredClimateRecords, zeroIrrigationRecords);

    System.out.println("* Valor esperado (nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateIrrigationWaterNeed (nec. agua riego");
    System.out.println("[mm/dia] de un cultivo en fecha actual): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testNineCalculateIrrigationWaterNeed() {
    System.out.println("************************************** Prueba nueve del metodo calculateIrrigationWaterNeed ***************************************");
    printDescriptionMethodToTest();

    System.out.println("# Descripcion de la prueba unitaria");
    System.out.println("Para esta prueba se utilizan 6 registros climaticos previos a la fecha actual " + UtilDate.formatDate(presumedCurrentDate) + ".");
    System.out.println("Suponemos que la fecha actual es " + UtilDate.formatDate(presumedCurrentDate) + ".");
    System.out.println();

    /*
     * Fechas a partir de las cuales se recuperaran los
     * registros climaticos y los registros de riego de
     * una parcela de prueba de la base de datos subyacente
     */
    Calendar dateFrom = Calendar.getInstance();
    dateFrom.set(Calendar.YEAR, 2023);
    dateFrom.set(Calendar.MONTH, JANUARY);
    dateFrom.set(Calendar.DAY_OF_MONTH, 1);

    Calendar dateUntil = Calendar.getInstance();
    dateUntil.set(Calendar.YEAR, 2023);
    dateUntil.set(Calendar.MONTH, JANUARY);
    dateUntil.set(Calendar.DAY_OF_MONTH, 6);

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
    givenUser.setUsername("vorti");
    givenUser.setName("Vorti");
    givenUser.setLastName("Vance");
    givenUser.setEmail("vorti@eservice.com");
    givenUser.setPassword("Vorti");

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
     * Persistencia de registros climaticos de prueba
     */
    ClimateRecord climateRecordOne = new ClimateRecord();
    climateRecordOne.setEtc(10);
    climateRecordOne.setPrecip(15);
    climateRecordOne.setDate(dayOne);
    climateRecordOne.setParcel(givenParcel);

    ClimateRecord climateRecordTwo = new ClimateRecord();
    climateRecordTwo.setEtc(5);
    climateRecordTwo.setPrecip(6);
    climateRecordTwo.setDate(dayTwo);
    climateRecordTwo.setParcel(givenParcel);

    ClimateRecord climateRecordThree = new ClimateRecord();
    climateRecordThree.setEtc(2);
    climateRecordThree.setPrecip(3);
    climateRecordThree.setDate(dayThree);
    climateRecordThree.setParcel(givenParcel);

    ClimateRecord climateRecordFour = new ClimateRecord();
    climateRecordFour.setEtc(3);
    climateRecordFour.setPrecip(4);
    climateRecordFour.setDate(dayFour);
    climateRecordFour.setParcel(givenParcel);

    ClimateRecord climateRecordFive = new ClimateRecord();
    climateRecordFive.setEtc(1);
    climateRecordFive.setPrecip(2);
    climateRecordFive.setDate(dayFive);
    climateRecordFive.setParcel(givenParcel);

    ClimateRecord climateRecordSix = new ClimateRecord();
    climateRecordSix.setEtc(2.5);
    climateRecordSix.setPrecip(5);
    climateRecordSix.setDate(daySix);
    climateRecordSix.setParcel(givenParcel);

    entityManager.getTransaction().begin();
    climateRecordOne = climateRecordService.create(climateRecordOne);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordTwo = climateRecordService.create(climateRecordTwo);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordThree = climateRecordService.create(climateRecordThree);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordFour = climateRecordService.create(climateRecordFour);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordFive = climateRecordService.create(climateRecordFive);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordSix = climateRecordService.create(climateRecordSix);
    entityManager.getTransaction().commit();

    climateRecordsToBeDeleted.add(climateRecordOne);
    climateRecordsToBeDeleted.add(climateRecordTwo);
    climateRecordsToBeDeleted.add(climateRecordThree);
    climateRecordsToBeDeleted.add(climateRecordFour);
    climateRecordsToBeDeleted.add(climateRecordFive);
    climateRecordsToBeDeleted.add(climateRecordSix);

    /*
     * Recupera los registros climaticos recientemente
     * persistidos
     */
    Collection<ClimateRecord> recoveredClimateRecords = climateRecordService.findAllByParcelIdAndPeriod(givenUser.getId(), givenParcel.getId(), dateFrom, dateUntil);

    System.out.println("Los datos con los que se calculara la necesidad de agua de riego de un cultivo en la fecha actual (" + UtilDate.formatDate(presumedCurrentDate) + ") son los siguientes:");
    System.out.println();

    printWaterBalanceTable(presumedCurrentDate, recoveredClimateRecords, zeroIrrigationRecords);

    double totalIrrigationWaterCurrentDate = 0.0;

    printAccumulatedWaterDeficitPerDay(presumedCurrentDate, recoveredClimateRecords, zeroIrrigationRecords);
    printTotalIrrigationWaterCurrentDate(presumedCurrentDate, totalIrrigationWaterCurrentDate);

    System.out.println();
    System.out.println("Al ser el resultado de la suma entre el acumulado del deficit de agua por dia de dias previos a la fecha actual y la cantidad");
    System.out.println("total de agua de riego de la fecha actual, mayor o igual a 0 (cero), el valor devuelto por el metodo calculateIrrigationWaterNeed");
    System.out.println("de la clase WaterNeedWit debe ser 0 [mm/dia]. Por lo tanto, la necesidad de agua de riego de un cultivo en la fecha actual es 0");
    System.out.println("[mm/dia].");
    System.out.println();

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");

    double expectedResult = 0.0;

    /*
     * El primer parametro de este metodo calculateIrrigationWaterNeed
     * es la cantidad total de agua de riego de una fecha dada. En este
     * caso, se le pasa el valor 0 como argumento porque suponemos que
     * la cantidad total de agua de riego de la supuesta fecha actual
     * es 0 para facilitar la tarea de probarlo.
     */
    double result = WaterNeedWit.calculateIrrigationWaterNeed(totalIrrigationWaterCurrentDate, recoveredClimateRecords, zeroIrrigationRecords);

    System.out.println("* Valor esperado (nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateIrrigationWaterNeed (nec. agua riego");
    System.out.println("[mm/dia] de un cultivo en fecha actual): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testTenCalculateIrrigationWaterNeed() {
    System.out.println("************************************** Prueba diez del metodo calculateIrrigationWaterNeed ***************************************");
    printDescriptionMethodToTest();

    System.out.println("# Descripcion de la prueba unitaria");
    System.out.println("Para esta prueba se utilizan 6 registros climaticos previos a la fecha actual " + UtilDate.formatDate(presumedCurrentDate) + ".");
    System.out.println("Suponemos que la fecha actual es " + UtilDate.formatDate(presumedCurrentDate) + ".");
    System.out.println();

    /*
     * Fechas a partir de las cuales se recuperaran los
     * registros climaticos y los registros de riego de
     * una parcela de prueba de la base de datos subyacente
     */
    Calendar dateFrom = Calendar.getInstance();
    dateFrom.set(Calendar.YEAR, 2023);
    dateFrom.set(Calendar.MONTH, JANUARY);
    dateFrom.set(Calendar.DAY_OF_MONTH, 1);

    Calendar dateUntil = Calendar.getInstance();
    dateUntil.set(Calendar.YEAR, 2023);
    dateUntil.set(Calendar.MONTH, JANUARY);
    dateUntil.set(Calendar.DAY_OF_MONTH, 6);

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
    givenUser.setUsername("barney");
    givenUser.setName("Barney");
    givenUser.setLastName("Vance");
    givenUser.setEmail("barney@eservice.com");
    givenUser.setPassword("Barney");

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
     * Persistencia de registros climaticos de prueba
     */
    ClimateRecord climateRecordOne = new ClimateRecord();
    climateRecordOne.setEtc(10);
    climateRecordOne.setPrecip(10);
    climateRecordOne.setDate(dayOne);
    climateRecordOne.setParcel(givenParcel);

    ClimateRecord climateRecordTwo = new ClimateRecord();
    climateRecordTwo.setEtc(5);
    climateRecordTwo.setPrecip(5);
    climateRecordTwo.setDate(dayTwo);
    climateRecordTwo.setParcel(givenParcel);

    ClimateRecord climateRecordThree = new ClimateRecord();
    climateRecordThree.setEtc(2);
    climateRecordThree.setPrecip(2);
    climateRecordThree.setDate(dayThree);
    climateRecordThree.setParcel(givenParcel);

    ClimateRecord climateRecordFour = new ClimateRecord();
    climateRecordFour.setEtc(3);
    climateRecordFour.setPrecip(3);
    climateRecordFour.setDate(dayFour);
    climateRecordFour.setParcel(givenParcel);

    ClimateRecord climateRecordFive = new ClimateRecord();
    climateRecordFive.setEtc(1);
    climateRecordFive.setPrecip(1);
    climateRecordFive.setDate(dayFive);
    climateRecordFive.setParcel(givenParcel);

    ClimateRecord climateRecordSix = new ClimateRecord();
    climateRecordSix.setEtc(2.5);
    climateRecordSix.setPrecip(2.5);
    climateRecordSix.setDate(daySix);
    climateRecordSix.setParcel(givenParcel);

    entityManager.getTransaction().begin();
    climateRecordOne = climateRecordService.create(climateRecordOne);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordTwo = climateRecordService.create(climateRecordTwo);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordThree = climateRecordService.create(climateRecordThree);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordFour = climateRecordService.create(climateRecordFour);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordFive = climateRecordService.create(climateRecordFive);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordSix = climateRecordService.create(climateRecordSix);
    entityManager.getTransaction().commit();

    climateRecordsToBeDeleted.add(climateRecordOne);
    climateRecordsToBeDeleted.add(climateRecordTwo);
    climateRecordsToBeDeleted.add(climateRecordThree);
    climateRecordsToBeDeleted.add(climateRecordFour);
    climateRecordsToBeDeleted.add(climateRecordFive);
    climateRecordsToBeDeleted.add(climateRecordSix);

    /*
     * Recupera los registros climaticos recientemente
     * persistidos
     */
    Collection<ClimateRecord> recoveredClimateRecords = climateRecordService.findAllByParcelIdAndPeriod(givenUser.getId(), givenParcel.getId(), dateFrom, dateUntil);

    System.out.println("Los datos con los que se calculara la necesidad de agua de riego de un cultivo en la fecha actual (" + UtilDate.formatDate(presumedCurrentDate) + ") son los siguientes:");
    System.out.println();

    printWaterBalanceTable(presumedCurrentDate, recoveredClimateRecords, zeroIrrigationRecords);

    double totalIrrigationWaterCurrentDate = 0.0;

    printAccumulatedWaterDeficitPerDay(presumedCurrentDate, recoveredClimateRecords, zeroIrrigationRecords);
    printTotalIrrigationWaterCurrentDate(presumedCurrentDate, totalIrrigationWaterCurrentDate);

    System.out.println();
    System.out.println("Al ser el resultado de la suma entre el acumulado del deficit de agua por dia de dias previos a la fecha actual y la cantidad");
    System.out.println("total de agua de riego de la fecha actual, mayor o igual a 0 (cero), el valor devuelto por el metodo calculateIrrigationWaterNeed");
    System.out.println("de la clase WaterNeedWit debe ser 0 [mm/dia]. Por lo tanto, la necesidad de agua de riego de un cultivo en la fecha actual es 0");
    System.out.println("[mm/dia].");
    System.out.println();

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");

    double expectedResult = 0.0;

    /*
     * El primer parametro de este metodo calculateIrrigationWaterNeed
     * es la cantidad total de agua de riego de una fecha dada. En este
     * caso, se le pasa el valor 0 como argumento porque suponemos que
     * la cantidad total de agua de riego de la supuesta fecha actual
     * es 0 para facilitar la tarea de probarlo.
     */
    double result = WaterNeedWit.calculateIrrigationWaterNeed(totalIrrigationWaterCurrentDate, recoveredClimateRecords, zeroIrrigationRecords);

    System.out.println("* Valor esperado (nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateIrrigationWaterNeed (nec. agua riego");
    System.out.println("[mm/dia] de un cultivo en fecha actual): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testElevenCalculateIrrigationWaterNeed() {
    System.out.println("************************************** Prueba once del metodo calculateIrrigationWaterNeed ***************************************");
    printDescriptionMethodToTest();

    System.out.println("# Descripcion de la prueba unitaria");
    System.out.println("Para esta prueba se utilizan 6 registros climaticos previos a la fecha actual " + UtilDate.formatDate(presumedCurrentDate) + ".");
    System.out.println("Suponemos que la fecha actual es " + UtilDate.formatDate(presumedCurrentDate) + ".");
    System.out.println();

    /*
     * Fechas a partir de las cuales se recuperaran los
     * registros climaticos y los registros de riego de
     * una parcela de prueba de la base de datos subyacente
     */
    Calendar dateFrom = Calendar.getInstance();
    dateFrom.set(Calendar.YEAR, 2023);
    dateFrom.set(Calendar.MONTH, JANUARY);
    dateFrom.set(Calendar.DAY_OF_MONTH, 1);

    Calendar dateUntil = Calendar.getInstance();
    dateUntil.set(Calendar.YEAR, 2023);
    dateUntil.set(Calendar.MONTH, JANUARY);
    dateUntil.set(Calendar.DAY_OF_MONTH, 6);

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
    givenUser.setUsername("william");
    givenUser.setName("William");
    givenUser.setLastName("Vance");
    givenUser.setEmail("william@eservice.com");
    givenUser.setPassword("William");

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
     * Persistencia de registros climaticos de prueba
     */
    ClimateRecord climateRecordOne = new ClimateRecord();
    climateRecordOne.setEtc(10);
    climateRecordOne.setPrecip(10);
    climateRecordOne.setDate(dayOne);
    climateRecordOne.setParcel(givenParcel);

    ClimateRecord climateRecordTwo = new ClimateRecord();
    climateRecordTwo.setEtc(5);
    climateRecordTwo.setPrecip(10);
    climateRecordTwo.setDate(dayTwo);
    climateRecordTwo.setParcel(givenParcel);

    ClimateRecord climateRecordThree = new ClimateRecord();
    climateRecordThree.setEtc(2);
    climateRecordThree.setPrecip(10);
    climateRecordThree.setDate(dayThree);
    climateRecordThree.setParcel(givenParcel);

    ClimateRecord climateRecordFour = new ClimateRecord();
    climateRecordFour.setEtc(3);
    climateRecordFour.setPrecip(0);
    climateRecordFour.setDate(dayFour);
    climateRecordFour.setParcel(givenParcel);

    ClimateRecord climateRecordFive = new ClimateRecord();
    climateRecordFive.setEtc(4);
    climateRecordFive.setPrecip(1);
    climateRecordFive.setDate(dayFive);
    climateRecordFive.setParcel(givenParcel);

    ClimateRecord climateRecordSix = new ClimateRecord();
    climateRecordSix.setEtc(2);
    climateRecordSix.setPrecip(4);
    climateRecordSix.setDate(daySix);
    climateRecordSix.setParcel(givenParcel);

    entityManager.getTransaction().begin();
    climateRecordOne = climateRecordService.create(climateRecordOne);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordTwo = climateRecordService.create(climateRecordTwo);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordThree = climateRecordService.create(climateRecordThree);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordFour = climateRecordService.create(climateRecordFour);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordFive = climateRecordService.create(climateRecordFive);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordSix = climateRecordService.create(climateRecordSix);
    entityManager.getTransaction().commit();

    climateRecordsToBeDeleted.add(climateRecordOne);
    climateRecordsToBeDeleted.add(climateRecordTwo);
    climateRecordsToBeDeleted.add(climateRecordThree);
    climateRecordsToBeDeleted.add(climateRecordFour);
    climateRecordsToBeDeleted.add(climateRecordFive);
    climateRecordsToBeDeleted.add(climateRecordSix);

    /*
     * Recupera los registros climaticos recientemente
     * persistidos
     */
    Collection<ClimateRecord> recoveredClimateRecords = climateRecordService.findAllByParcelIdAndPeriod(givenUser.getId(), givenParcel.getId(), dateFrom, dateUntil);

    System.out.println("Los datos con los que se calculara la necesidad de agua de riego de un cultivo en la fecha actual (" + UtilDate.formatDate(presumedCurrentDate) + ") son los siguientes:");
    System.out.println();

    printWaterBalanceTable(presumedCurrentDate, recoveredClimateRecords, zeroIrrigationRecords);

    double totalIrrigationWaterCurrentDate = 0.0;

    printAccumulatedWaterDeficitPerDay(presumedCurrentDate, recoveredClimateRecords, zeroIrrigationRecords);
    printTotalIrrigationWaterCurrentDate(presumedCurrentDate, totalIrrigationWaterCurrentDate);

    System.out.println();
    System.out.println("Al ser el resultado de la suma entre el acumulado del deficit de agua por dia de dias previos a la fecha actual y la cantidad");
    System.out.println("total de agua de riego de la fecha actual, estrictamente menor a 0 (cero), el valor devuelto por el metodo calculateIrrigationWaterNeed");
    System.out.println("de la clase WaterNeedWit debe ser el valor absoluto del resultado de dicha suma [mm/dia]. Por lo tanto, la necesidad de agua");
    System.out.println("de riego de un cultivo en la fecha actual es mayor o igual a 0 (cero) [mm/dia].");
    System.out.println();

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");

    double expectedResult = 4.0;

    /*
     * El primer parametro de este metodo calculateIrrigationWaterNeed
     * es la cantidad total de agua de riego de una fecha dada. En este
     * caso, se le pasa el valor 0 como argumento porque suponemos que
     * la cantidad total de agua de riego de la supuesta fecha actual
     * es 0 para facilitar la tarea de probarlo.
     */
    double result = WaterNeedWit.calculateIrrigationWaterNeed(totalIrrigationWaterCurrentDate, recoveredClimateRecords, zeroIrrigationRecords);

    System.out.println("* Valor esperado (nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateIrrigationWaterNeed (nec. agua riego");
    System.out.println("[mm/dia] de un cultivo en fecha actual): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testTwelveCalculateIrrigationWaterNeed() {
    System.out.println("************************************** Prueba doce del metodo calculateIrrigationWaterNeed ***************************************");
    printDescriptionMethodToTest();

    System.out.println("# Descripcion de la prueba unitaria");
    System.out.println("Para esta prueba se utilizan 6 registros climaticos previos a la fecha actual " + UtilDate.formatDate(presumedCurrentDate) + ".");
    System.out.println("Suponemos que la fecha actual es " + UtilDate.formatDate(presumedCurrentDate) + ".");
    System.out.println();

    /*
     * Fechas a partir de las cuales se recuperaran los
     * registros climaticos y los registros de riego de
     * una parcela de prueba de la base de datos subyacente
     */
    Calendar dateFrom = Calendar.getInstance();
    dateFrom.set(Calendar.YEAR, 2023);
    dateFrom.set(Calendar.MONTH, JANUARY);
    dateFrom.set(Calendar.DAY_OF_MONTH, 1);

    Calendar dateUntil = Calendar.getInstance();
    dateUntil.set(Calendar.YEAR, 2023);
    dateUntil.set(Calendar.MONTH, JANUARY);
    dateUntil.set(Calendar.DAY_OF_MONTH, 6);

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
    givenUser.setUsername("tom");
    givenUser.setName("Tom");
    givenUser.setLastName("Vance");
    givenUser.setEmail("tom@eservice.com");
    givenUser.setPassword("Tom");

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
     * Persistencia de registros climaticos de prueba
     */
    ClimateRecord climateRecordOne = new ClimateRecord();
    climateRecordOne.setEtc(15);
    climateRecordOne.setPrecip(10);
    climateRecordOne.setDate(dayOne);
    climateRecordOne.setParcel(givenParcel);

    ClimateRecord climateRecordTwo = new ClimateRecord();
    climateRecordTwo.setEtc(12);
    climateRecordTwo.setPrecip(13);
    climateRecordTwo.setDate(dayTwo);
    climateRecordTwo.setParcel(givenParcel);

    ClimateRecord climateRecordThree = new ClimateRecord();
    climateRecordThree.setEtc(6);
    climateRecordThree.setPrecip(6);
    climateRecordThree.setDate(dayThree);
    climateRecordThree.setParcel(givenParcel);

    ClimateRecord climateRecordFour = new ClimateRecord();
    climateRecordFour.setEtc(2);
    climateRecordFour.setPrecip(1);
    climateRecordFour.setDate(dayFour);
    climateRecordFour.setParcel(givenParcel);

    ClimateRecord climateRecordFive = new ClimateRecord();
    climateRecordFive.setEtc(4);
    climateRecordFive.setPrecip(2);
    climateRecordFive.setDate(dayFive);
    climateRecordFive.setParcel(givenParcel);

    ClimateRecord climateRecordSix = new ClimateRecord();
    climateRecordSix.setEtc(7);
    climateRecordSix.setPrecip(7);
    climateRecordSix.setDate(daySix);
    climateRecordSix.setParcel(givenParcel);

    entityManager.getTransaction().begin();
    climateRecordOne = climateRecordService.create(climateRecordOne);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordTwo = climateRecordService.create(climateRecordTwo);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordThree = climateRecordService.create(climateRecordThree);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordFour = climateRecordService.create(climateRecordFour);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordFive = climateRecordService.create(climateRecordFive);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordSix = climateRecordService.create(climateRecordSix);
    entityManager.getTransaction().commit();

    climateRecordsToBeDeleted.add(climateRecordOne);
    climateRecordsToBeDeleted.add(climateRecordTwo);
    climateRecordsToBeDeleted.add(climateRecordThree);
    climateRecordsToBeDeleted.add(climateRecordFour);
    climateRecordsToBeDeleted.add(climateRecordFive);
    climateRecordsToBeDeleted.add(climateRecordSix);

    /*
     * Recupera los registros climaticos recientemente
     * persistidos
     */
    Collection<ClimateRecord> recoveredClimateRecords = climateRecordService.findAllByParcelIdAndPeriod(givenUser.getId(), givenParcel.getId(), dateFrom, dateUntil);

    System.out.println("Los datos con los que se calculara la necesidad de agua de riego de un cultivo en la fecha actual (" + UtilDate.formatDate(presumedCurrentDate) + ") son los siguientes:");
    System.out.println();

    printWaterBalanceTable(presumedCurrentDate, recoveredClimateRecords, zeroIrrigationRecords);

    double totalIrrigationWaterCurrentDate = 0.0;

    printAccumulatedWaterDeficitPerDay(presumedCurrentDate, recoveredClimateRecords, zeroIrrigationRecords);
    printTotalIrrigationWaterCurrentDate(presumedCurrentDate, totalIrrigationWaterCurrentDate);

    System.out.println();
    System.out.println("Al ser el resultado de la suma entre el acumulado del deficit de agua por dia de dias previos a la fecha actual y la cantidad");
    System.out.println("total de agua de riego de la fecha actual, estrictamente menor a 0 (cero), el valor devuelto por el metodo calculateIrrigationWaterNeed");
    System.out.println("de la clase WaterNeedWit debe ser el valor absoluto del resultado de dicha suma [mm/dia]. Por lo tanto, la necesidad de agua");
    System.out.println("de riego de un cultivo en la fecha actual es mayor o igual a 0 (cero) [mm/dia].");
    System.out.println();

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");

    double expectedResult = 7.0;

    /*
     * El primer parametro de este metodo calculateIrrigationWaterNeed
     * es la cantidad total de agua de riego de una fecha dada. En este
     * caso, se le pasa el valor 0 como argumento porque suponemos que
     * la cantidad total de agua de riego de la supuesta fecha actual
     * es 0 para facilitar la tarea de probarlo.
     */
    double result = WaterNeedWit.calculateIrrigationWaterNeed(totalIrrigationWaterCurrentDate, recoveredClimateRecords, zeroIrrigationRecords);

    System.out.println("* Valor esperado (nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateIrrigationWaterNeed (nec. agua riego");
    System.out.println("[mm/dia] de un cultivo en fecha actual): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testThirteenCalculateIrrigationWaterNeed() {
    System.out.println("************************************** Prueba trece del metodo calculateIrrigationWaterNeed ***************************************");
    printDescriptionMethodToTest();

    System.out.println("# Descripcion de la prueba unitaria");
    System.out.println("Para esta prueba se utilizan 6 registros climaticos previos a la fecha actual " + UtilDate.formatDate(presumedCurrentDate) + ".");
    System.out.println("Suponemos que la fecha actual es " + UtilDate.formatDate(presumedCurrentDate) + ".");
    System.out.println();

    /*
     * Fechas a partir de las cuales se recuperaran los
     * registros climaticos y los registros de riego de
     * una parcela de prueba de la base de datos subyacente
     */
    Calendar dateFrom = Calendar.getInstance();
    dateFrom.set(Calendar.YEAR, 2023);
    dateFrom.set(Calendar.MONTH, JANUARY);
    dateFrom.set(Calendar.DAY_OF_MONTH, 1);

    Calendar dateUntil = Calendar.getInstance();
    dateUntil.set(Calendar.YEAR, 2023);
    dateUntil.set(Calendar.MONTH, JANUARY);
    dateUntil.set(Calendar.DAY_OF_MONTH, 6);

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
    givenUser.setUsername("jerry");
    givenUser.setName("Jerry");
    givenUser.setLastName("Vance");
    givenUser.setEmail("jerry@eservice.com");
    givenUser.setPassword("Jerry");

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
     * Persistencia de registros climaticos de prueba
     */
    ClimateRecord climateRecordOne = new ClimateRecord();
    climateRecordOne.setEtc(15);
    climateRecordOne.setPrecip(15);
    climateRecordOne.setDate(dayOne);
    climateRecordOne.setParcel(givenParcel);

    ClimateRecord climateRecordTwo = new ClimateRecord();
    climateRecordTwo.setEtc(12);
    climateRecordTwo.setPrecip(16);
    climateRecordTwo.setDate(dayTwo);
    climateRecordTwo.setParcel(givenParcel);

    ClimateRecord climateRecordThree = new ClimateRecord();
    climateRecordThree.setEtc(6);
    climateRecordThree.setPrecip(8);
    climateRecordThree.setDate(dayThree);
    climateRecordThree.setParcel(givenParcel);

    ClimateRecord climateRecordFour = new ClimateRecord();
    climateRecordFour.setEtc(2);
    climateRecordFour.setPrecip(0);
    climateRecordFour.setDate(dayFour);
    climateRecordFour.setParcel(givenParcel);

    ClimateRecord climateRecordFive = new ClimateRecord();
    climateRecordFive.setEtc(4);
    climateRecordFive.setPrecip(1);
    climateRecordFive.setDate(dayFive);
    climateRecordFive.setParcel(givenParcel);

    ClimateRecord climateRecordSix = new ClimateRecord();
    climateRecordSix.setEtc(5);
    climateRecordSix.setPrecip(6);
    climateRecordSix.setDate(daySix);
    climateRecordSix.setParcel(givenParcel);

    entityManager.getTransaction().begin();
    climateRecordOne = climateRecordService.create(climateRecordOne);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordTwo = climateRecordService.create(climateRecordTwo);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordThree = climateRecordService.create(climateRecordThree);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordFour = climateRecordService.create(climateRecordFour);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordFive = climateRecordService.create(climateRecordFive);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordSix = climateRecordService.create(climateRecordSix);
    entityManager.getTransaction().commit();

    climateRecordsToBeDeleted.add(climateRecordOne);
    climateRecordsToBeDeleted.add(climateRecordTwo);
    climateRecordsToBeDeleted.add(climateRecordThree);
    climateRecordsToBeDeleted.add(climateRecordFour);
    climateRecordsToBeDeleted.add(climateRecordFive);
    climateRecordsToBeDeleted.add(climateRecordSix);

    /*
     * Recupera los registros climaticos recientemente
     * persistidos
     */
    Collection<ClimateRecord> recoveredClimateRecords = climateRecordService.findAllByParcelIdAndPeriod(givenUser.getId(), givenParcel.getId(), dateFrom, dateUntil);

    System.out.println("Los datos con los que se calculara la necesidad de agua de riego de un cultivo en la fecha actual (" + UtilDate.formatDate(presumedCurrentDate) + ") son los siguientes:");
    System.out.println();

    printWaterBalanceTable(presumedCurrentDate, recoveredClimateRecords, zeroIrrigationRecords);

    double totalIrrigationWaterCurrentDate = 0.0;

    printAccumulatedWaterDeficitPerDay(presumedCurrentDate, recoveredClimateRecords, zeroIrrigationRecords);
    printTotalIrrigationWaterCurrentDate(presumedCurrentDate, totalIrrigationWaterCurrentDate);

    System.out.println();
    System.out.println("Al ser el resultado de la suma entre el acumulado del deficit de agua por dia de dias previos a la fecha actual y la cantidad");
    System.out.println("total de agua de riego de la fecha actual, estrictamente menor a 0 (cero), el valor devuelto por el metodo calculateIrrigationWaterNeed");
    System.out.println("de la clase WaterNeedWit debe ser el valor absoluto del resultado de dicha suma [mm/dia]. Por lo tanto, la necesidad de agua");
    System.out.println("de riego de un cultivo en la fecha actual es mayor o igual a 0 (cero) [mm/dia].");
    System.out.println();

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");

    double expectedResult = 4.0;

    /*
     * El primer parametro de este metodo calculateIrrigationWaterNeed
     * es la cantidad total de agua de riego de una fecha dada. En este
     * caso, se le pasa el valor 0 como argumento porque suponemos que
     * la cantidad total de agua de riego de la supuesta fecha actual
     * es 0 para facilitar la tarea de probarlo.
     */
    double result = WaterNeedWit.calculateIrrigationWaterNeed(totalIrrigationWaterCurrentDate, recoveredClimateRecords, zeroIrrigationRecords);

    System.out.println("* Valor esperado (nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateIrrigationWaterNeed (nec. agua riego");
    System.out.println("[mm/dia] de un cultivo en fecha actual): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testFourteenCalculateIrrigationWaterNeed() {
    System.out.println("************************************** Prueba catorce del metodo calculateIrrigationWaterNeed ***************************************");
    printDescriptionMethodToTest();

    System.out.println("# Descripcion de la prueba unitaria");
    System.out.println("Para esta prueba se utilizan 6 registros climaticos previos a la fecha actual " + UtilDate.formatDate(presumedCurrentDate) + ".");
    System.out.println("Suponemos que la fecha actual es " + UtilDate.formatDate(presumedCurrentDate) + ".");
    System.out.println();

    /*
     * Fechas a partir de las cuales se recuperaran los
     * registros climaticos y los registros de riego de
     * una parcela de prueba de la base de datos subyacente
     */
    Calendar dateFrom = Calendar.getInstance();
    dateFrom.set(Calendar.YEAR, 2023);
    dateFrom.set(Calendar.MONTH, JANUARY);
    dateFrom.set(Calendar.DAY_OF_MONTH, 1);

    Calendar dateUntil = Calendar.getInstance();
    dateUntil.set(Calendar.YEAR, 2023);
    dateUntil.set(Calendar.MONTH, JANUARY);
    dateUntil.set(Calendar.DAY_OF_MONTH, 6);

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
    givenUser.setUsername("leon");
    givenUser.setName("Leon");
    givenUser.setLastName("Vance");
    givenUser.setEmail("leon@eservice.com");
    givenUser.setPassword("Leon");

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
     * Persistencia de registros climaticos de prueba
     */
    ClimateRecord climateRecordOne = new ClimateRecord();
    climateRecordOne.setEtc(15);
    climateRecordOne.setPrecip(15);
    climateRecordOne.setDate(dayOne);
    climateRecordOne.setParcel(givenParcel);

    ClimateRecord climateRecordTwo = new ClimateRecord();
    climateRecordTwo.setEtc(12);
    climateRecordTwo.setPrecip(12);
    climateRecordTwo.setDate(dayTwo);
    climateRecordTwo.setParcel(givenParcel);

    ClimateRecord climateRecordThree = new ClimateRecord();
    climateRecordThree.setEtc(6);
    climateRecordThree.setPrecip(2);
    climateRecordThree.setDate(dayThree);
    climateRecordThree.setParcel(givenParcel);

    ClimateRecord climateRecordFour = new ClimateRecord();
    climateRecordFour.setEtc(2);
    climateRecordFour.setPrecip(0);
    climateRecordFour.setDate(dayFour);
    climateRecordFour.setParcel(givenParcel);

    ClimateRecord climateRecordFive = new ClimateRecord();
    climateRecordFive.setEtc(4);
    climateRecordFive.setPrecip(1);
    climateRecordFive.setDate(dayFive);
    climateRecordFive.setParcel(givenParcel);

    ClimateRecord climateRecordSix = new ClimateRecord();
    climateRecordSix.setEtc(5);
    climateRecordSix.setPrecip(14);
    climateRecordSix.setDate(daySix);
    climateRecordSix.setParcel(givenParcel);

    entityManager.getTransaction().begin();
    climateRecordOne = climateRecordService.create(climateRecordOne);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordTwo = climateRecordService.create(climateRecordTwo);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordThree = climateRecordService.create(climateRecordThree);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordFour = climateRecordService.create(climateRecordFour);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordFive = climateRecordService.create(climateRecordFive);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordSix = climateRecordService.create(climateRecordSix);
    entityManager.getTransaction().commit();

    climateRecordsToBeDeleted.add(climateRecordOne);
    climateRecordsToBeDeleted.add(climateRecordTwo);
    climateRecordsToBeDeleted.add(climateRecordThree);
    climateRecordsToBeDeleted.add(climateRecordFour);
    climateRecordsToBeDeleted.add(climateRecordFive);
    climateRecordsToBeDeleted.add(climateRecordSix);

    /*
     * Recupera los registros climaticos recientemente
     * persistidos
     */
    Collection<ClimateRecord> recoveredClimateRecords = climateRecordService.findAllByParcelIdAndPeriod(givenUser.getId(), givenParcel.getId(), dateFrom, dateUntil);

    System.out.println("Los datos con los que se calculara la necesidad de agua de riego de un cultivo en la fecha actual (" + UtilDate.formatDate(presumedCurrentDate) + ") son los siguientes:");
    System.out.println();

    printWaterBalanceTable(presumedCurrentDate, recoveredClimateRecords, zeroIrrigationRecords);

    double totalIrrigationWaterCurrentDate = 0.0;

    printAccumulatedWaterDeficitPerDay(presumedCurrentDate, recoveredClimateRecords, zeroIrrigationRecords);
    printTotalIrrigationWaterCurrentDate(presumedCurrentDate, totalIrrigationWaterCurrentDate);

    System.out.println();
    System.out.println("Al ser el resultado de la suma entre el acumulado del deficit de agua por dia de dias previos a la fecha actual y la cantidad");
    System.out.println("total de agua de riego de la fecha actual, mayor o igual a 0 (cero), el valor devuelto por el metodo calculateIrrigationWaterNeed");
    System.out.println("de la clase WaterNeedWit debe ser 0 [mm/dia]. Por lo tanto, la necesidad de agua de riego de un cultivo en la fecha actual es 0");
    System.out.println("[mm/dia].");
    System.out.println();

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");

    double expectedResult = 0.0;

    /*
     * El primer parametro de este metodo calculateIrrigationWaterNeed
     * es la cantidad total de agua de riego de una fecha dada. En este
     * caso, se le pasa el valor 0 como argumento porque suponemos que
     * la cantidad total de agua de riego de la supuesta fecha actual
     * es 0 para facilitar la tarea de probarlo.
     */
    double result = WaterNeedWit.calculateIrrigationWaterNeed(totalIrrigationWaterCurrentDate, recoveredClimateRecords, zeroIrrigationRecords);

    System.out.println("* Valor esperado (nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateIrrigationWaterNeed (nec. agua riego");
    System.out.println("[mm/dia] de un cultivo en fecha actual): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testFifteenCalculateIrrigationWaterNeed() {
    System.out.println("************************************** Prueba quince del metodo calculateIrrigationWaterNeed ***************************************");
    printDescriptionMethodToTest();

    System.out.println("# Descripcion de la prueba unitaria");
    System.out.println("Para esta prueba se utilizan 6 registros climaticos previos a la fecha actual " + UtilDate.formatDate(presumedCurrentDate) + ".");
    System.out.println("Suponemos que la fecha actual es " + UtilDate.formatDate(presumedCurrentDate) + ".");
    System.out.println();

    /*
     * Fechas a partir de las cuales se recuperaran los
     * registros climaticos y los registros de riego de
     * una parcela de prueba de la base de datos subyacente
     */
    Calendar dateFrom = Calendar.getInstance();
    dateFrom.set(Calendar.YEAR, 2023);
    dateFrom.set(Calendar.MONTH, JANUARY);
    dateFrom.set(Calendar.DAY_OF_MONTH, 1);

    Calendar dateUntil = Calendar.getInstance();
    dateUntil.set(Calendar.YEAR, 2023);
    dateUntil.set(Calendar.MONTH, JANUARY);
    dateUntil.set(Calendar.DAY_OF_MONTH, 6);

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
    givenUser.setUsername("claire");
    givenUser.setName("Claire");
    givenUser.setLastName("Redfield");
    givenUser.setEmail("claire@eservice.com");
    givenUser.setPassword("Claire");

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
     * Persistencia de registros climaticos de prueba
     */
    ClimateRecord climateRecordOne = new ClimateRecord();
    climateRecordOne.setEtc(15);
    climateRecordOne.setPrecip(15);
    climateRecordOne.setDate(dayOne);
    climateRecordOne.setParcel(givenParcel);

    ClimateRecord climateRecordTwo = new ClimateRecord();
    climateRecordTwo.setEtc(12);
    climateRecordTwo.setPrecip(12);
    climateRecordTwo.setDate(dayTwo);
    climateRecordTwo.setParcel(givenParcel);

    ClimateRecord climateRecordThree = new ClimateRecord();
    climateRecordThree.setEtc(6);
    climateRecordThree.setPrecip(2);
    climateRecordThree.setDate(dayThree);
    climateRecordThree.setParcel(givenParcel);

    ClimateRecord climateRecordFour = new ClimateRecord();
    climateRecordFour.setEtc(2);
    climateRecordFour.setPrecip(0);
    climateRecordFour.setDate(dayFour);
    climateRecordFour.setParcel(givenParcel);

    ClimateRecord climateRecordFive = new ClimateRecord();
    climateRecordFive.setEtc(4);
    climateRecordFive.setPrecip(1);
    climateRecordFive.setDate(dayFive);
    climateRecordFive.setParcel(givenParcel);

    ClimateRecord climateRecordSix = new ClimateRecord();
    climateRecordSix.setEtc(5);
    climateRecordSix.setPrecip(20);
    climateRecordSix.setDate(daySix);
    climateRecordSix.setParcel(givenParcel);

    entityManager.getTransaction().begin();
    climateRecordOne = climateRecordService.create(climateRecordOne);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordTwo = climateRecordService.create(climateRecordTwo);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordThree = climateRecordService.create(climateRecordThree);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordFour = climateRecordService.create(climateRecordFour);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordFive = climateRecordService.create(climateRecordFive);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordSix = climateRecordService.create(climateRecordSix);
    entityManager.getTransaction().commit();

    climateRecordsToBeDeleted.add(climateRecordOne);
    climateRecordsToBeDeleted.add(climateRecordTwo);
    climateRecordsToBeDeleted.add(climateRecordThree);
    climateRecordsToBeDeleted.add(climateRecordFour);
    climateRecordsToBeDeleted.add(climateRecordFive);
    climateRecordsToBeDeleted.add(climateRecordSix);

    /*
     * Recupera los registros climaticos recientemente
     * persistidos
     */
    Collection<ClimateRecord> recoveredClimateRecords = climateRecordService.findAllByParcelIdAndPeriod(givenUser.getId(), givenParcel.getId(), dateFrom, dateUntil);

    System.out.println("Los datos con los que se calculara la necesidad de agua de riego de un cultivo en la fecha actual (" + UtilDate.formatDate(presumedCurrentDate) + ") son los siguientes:");
    System.out.println();

    printWaterBalanceTable(presumedCurrentDate, recoveredClimateRecords, zeroIrrigationRecords);

    double totalIrrigationWaterCurrentDate = 0.0;

    printAccumulatedWaterDeficitPerDay(presumedCurrentDate, recoveredClimateRecords, zeroIrrigationRecords);
    printTotalIrrigationWaterCurrentDate(presumedCurrentDate, totalIrrigationWaterCurrentDate);

    System.out.println();
    System.out.println("Al ser el resultado de la suma entre el acumulado del deficit de agua por dia de dias previos a la fecha actual y la cantidad");
    System.out.println("total de agua de riego de la fecha actual, mayor o igual a 0 (cero), el valor devuelto por el metodo calculateIrrigationWaterNeed");
    System.out.println("de la clase WaterNeedWit debe ser 0 [mm/dia]. Por lo tanto, la necesidad de agua de riego de un cultivo en la fecha actual es 0");
    System.out.println("[mm/dia].");
    System.out.println();

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");

    double expectedResult = 0.0;

    /*
     * El primer parametro de este metodo calculateIrrigationWaterNeed
     * es la cantidad total de agua de riego de una fecha dada. En este
     * caso, se le pasa el valor 0 como argumento porque suponemos que
     * la cantidad total de agua de riego de la supuesta fecha actual
     * es 0 para facilitar la tarea de probarlo.
     */
    double result = WaterNeedWit.calculateIrrigationWaterNeed(totalIrrigationWaterCurrentDate, recoveredClimateRecords, zeroIrrigationRecords);

    System.out.println("* Valor esperado (nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateIrrigationWaterNeed (nec. agua riego");
    System.out.println("[mm/dia] de un cultivo en fecha actual): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testSixteenCalculateIrrigationWaterNeed() {
    System.out.println("************************************** Prueba dieciseis del metodo calculateIrrigationWaterNeed ***************************************");
    printDescriptionMethodToTest();

    System.out.println("# Descripcion de la prueba unitaria");
    System.out.println("Para esta prueba se utilizan 6 registros climaticos previos a la fecha actual " + UtilDate.formatDate(presumedCurrentDate) + ".");
    System.out.println("Suponemos que la fecha actual es " + UtilDate.formatDate(presumedCurrentDate) + ".");
    System.out.println();

    /*
     * Fechas a partir de las cuales se recuperaran los
     * registros climaticos y los registros de riego de
     * una parcela de prueba de la base de datos subyacente
     */
    Calendar dateFrom = Calendar.getInstance();
    dateFrom.set(Calendar.YEAR, 2023);
    dateFrom.set(Calendar.MONTH, JANUARY);
    dateFrom.set(Calendar.DAY_OF_MONTH, 1);

    Calendar dateUntil = Calendar.getInstance();
    dateUntil.set(Calendar.YEAR, 2023);
    dateUntil.set(Calendar.MONTH, JANUARY);
    dateUntil.set(Calendar.DAY_OF_MONTH, 6);

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
    givenUser.setUsername("chris");
    givenUser.setName("Chris");
    givenUser.setLastName("Redfield");
    givenUser.setEmail("chris@eservice.com");
    givenUser.setPassword("Chris");

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
     * Persistencia de registros climaticos de prueba
     */
    ClimateRecord climateRecordOne = new ClimateRecord();
    climateRecordOne.setEtc(15);
    climateRecordOne.setPrecip(10);
    climateRecordOne.setDate(dayOne);
    climateRecordOne.setParcel(givenParcel);

    ClimateRecord climateRecordTwo = new ClimateRecord();
    climateRecordTwo.setEtc(5);
    climateRecordTwo.setPrecip(10);
    climateRecordTwo.setDate(dayTwo);
    climateRecordTwo.setParcel(givenParcel);

    ClimateRecord climateRecordThree = new ClimateRecord();
    climateRecordThree.setEtc(8);
    climateRecordThree.setPrecip(4);
    climateRecordThree.setDate(dayThree);
    climateRecordThree.setParcel(givenParcel);

    ClimateRecord climateRecordFour = new ClimateRecord();
    climateRecordFour.setEtc(2);
    climateRecordFour.setPrecip(6);
    climateRecordFour.setDate(dayFour);
    climateRecordFour.setParcel(givenParcel);

    ClimateRecord climateRecordFive = new ClimateRecord();
    climateRecordFive.setEtc(8);
    climateRecordFive.setPrecip(2);
    climateRecordFive.setDate(dayFive);
    climateRecordFive.setParcel(givenParcel);

    ClimateRecord climateRecordSix = new ClimateRecord();
    climateRecordSix.setEtc(5);
    climateRecordSix.setPrecip(10);
    climateRecordSix.setDate(daySix);
    climateRecordSix.setParcel(givenParcel);

    entityManager.getTransaction().begin();
    climateRecordOne = climateRecordService.create(climateRecordOne);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordTwo = climateRecordService.create(climateRecordTwo);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordThree = climateRecordService.create(climateRecordThree);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordFour = climateRecordService.create(climateRecordFour);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordFive = climateRecordService.create(climateRecordFive);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordSix = climateRecordService.create(climateRecordSix);
    entityManager.getTransaction().commit();

    climateRecordsToBeDeleted.add(climateRecordOne);
    climateRecordsToBeDeleted.add(climateRecordTwo);
    climateRecordsToBeDeleted.add(climateRecordThree);
    climateRecordsToBeDeleted.add(climateRecordFour);
    climateRecordsToBeDeleted.add(climateRecordFive);
    climateRecordsToBeDeleted.add(climateRecordSix);

    /*
     * Recupera los registros climaticos recientemente
     * persistidos
     */
    Collection<ClimateRecord> recoveredClimateRecords = climateRecordService.findAllByParcelIdAndPeriod(givenUser.getId(), givenParcel.getId(), dateFrom, dateUntil);

    System.out.println("Los datos con los que se calculara la necesidad de agua de riego de un cultivo en la fecha actual (" + UtilDate.formatDate(presumedCurrentDate) + ") son los siguientes:");
    System.out.println();

    printWaterBalanceTable(presumedCurrentDate, recoveredClimateRecords, zeroIrrigationRecords);

    double totalIrrigationWaterCurrentDate = 0.0;

    printAccumulatedWaterDeficitPerDay(presumedCurrentDate, recoveredClimateRecords, zeroIrrigationRecords);
    printTotalIrrigationWaterCurrentDate(presumedCurrentDate, totalIrrigationWaterCurrentDate);

    System.out.println();
    System.out.println("Al ser el resultado de la suma entre el acumulado del deficit de agua por dia de dias previos a la fecha actual y la cantidad");
    System.out.println("total de agua de riego de la fecha actual, estrictamente menor a 0 (cero), el valor devuelto por el metodo calculateIrrigationWaterNeed");
    System.out.println("de la clase WaterNeedWit debe ser el valor absoluto del resultado de dicha suma [mm/dia]. Por lo tanto, la necesidad de agua");
    System.out.println("de riego de un cultivo en la fecha actual es mayor o igual a 0 (cero) [mm/dia].");
    System.out.println();

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");

    double expectedResult = 1.0;

    /*
     * El primer parametro de este metodo calculateIrrigationWaterNeed
     * es la cantidad total de agua de riego de una fecha dada. En este
     * caso, se le pasa el valor 0 como argumento porque suponemos que
     * la cantidad total de agua de riego de la supuesta fecha actual
     * es 0 para facilitar la tarea de probarlo.
     */
    double result = WaterNeedWit.calculateIrrigationWaterNeed(totalIrrigationWaterCurrentDate, recoveredClimateRecords, zeroIrrigationRecords);

    System.out.println("* Valor esperado (nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateIrrigationWaterNeed (nec. agua riego");
    System.out.println("[mm/dia] de un cultivo en fecha actual): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  /*
   * ****************************************************************
   * A partir de aca comienzan las pruebas unitarias del metodo
   * calculateIrrigationWaterNeed de la clase NeedWaterWit haciendo
   * uso del metodo findAllByParcelIdAndPeriod de las clases
   * ClimateRecordServiceBean e IrrigationRecordServiceBean. El motivo
   * de esto es que el metodo calculateIrrigationWaterNeed hasta ahora
   * NO fue probado con el metodo findAllByParcelIdAndPeriod de las
   * clases mencionadas.
   * 
   * La idea de estas pruebas unitarias es demostrar que el metodo
   * calculateIrrigationWater de la clase NeedWaterWit calcula
   * correctamente la necesidad de agua de riego de un cultivo en
   * una fecha dada utilizando unicamente el agua de riego de dias
   * previos a la fecha actual. Por este motivo los registros climaticos
   * de prueba tendran precipitacion igual a 0.
   * ****************************************************************
   */

  @Test
  public void testSeventeenCalculateIrrigationWaterNeed() {
    System.out.println("************************************** Prueba diecisiete del metodo calculateIrrigationWaterNeed ***************************************");
    printDescriptionMethodToTest();

    System.out.println("# Descripcion de la prueba unitaria");
    System.out.println("Para esta prueba se utilizan 6 registros climaticos y 6 registros de riego, todos ellos previos a la fecha actual " + UtilDate.formatDate(presumedCurrentDate) + ".");
    System.out.println("Suponemos que la fecha actual es " + UtilDate.formatDate(presumedCurrentDate) + ".");
    System.out.println();

    /*
     * Fechas a partir de las cuales se recuperaran los
     * registros climaticos y los registros de riego de
     * una parcela de prueba de la base de datos subyacente
     */
    Calendar dateFrom = Calendar.getInstance();
    dateFrom.set(Calendar.YEAR, 2023);
    dateFrom.set(Calendar.MONTH, JANUARY);
    dateFrom.set(Calendar.DAY_OF_MONTH, 1);

    Calendar dateUntil = Calendar.getInstance();
    dateUntil.set(Calendar.YEAR, 2023);
    dateUntil.set(Calendar.MONTH, JANUARY);
    dateUntil.set(Calendar.DAY_OF_MONTH, 6);

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
    givenUser.setUsername("elizabeth");
    givenUser.setName("Elizabeth");
    givenUser.setLastName("Cross");
    givenUser.setEmail("elizabeth@eservice.com");
    givenUser.setPassword("Elizabeth");

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
     * Persistencia de registros climaticos de prueba
     */
    ClimateRecord climateRecordOne = new ClimateRecord();
    climateRecordOne.setEtc(10);
    climateRecordOne.setDate(dayOne);
    climateRecordOne.setParcel(givenParcel);

    ClimateRecord climateRecordTwo = new ClimateRecord();
    climateRecordTwo.setEtc(5);
    climateRecordTwo.setDate(dayTwo);
    climateRecordTwo.setParcel(givenParcel);

    ClimateRecord climateRecordThree = new ClimateRecord();
    climateRecordThree.setEtc(2);
    climateRecordThree.setDate(dayThree);
    climateRecordThree.setParcel(givenParcel);

    ClimateRecord climateRecordFour = new ClimateRecord();
    climateRecordFour.setEtc(3);
    climateRecordFour.setDate(dayFour);
    climateRecordFour.setParcel(givenParcel);

    ClimateRecord climateRecordFive = new ClimateRecord();
    climateRecordFive.setEtc(1);
    climateRecordFive.setDate(dayFive);
    climateRecordFive.setParcel(givenParcel);

    ClimateRecord climateRecordSix = new ClimateRecord();
    climateRecordSix.setEtc(4);
    climateRecordSix.setDate(daySix);
    climateRecordSix.setParcel(givenParcel);

    entityManager.getTransaction().begin();
    climateRecordOne = climateRecordService.create(climateRecordOne);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordTwo = climateRecordService.create(climateRecordTwo);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordThree = climateRecordService.create(climateRecordThree);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordFour = climateRecordService.create(climateRecordFour);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordFive = climateRecordService.create(climateRecordFive);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordSix = climateRecordService.create(climateRecordSix);
    entityManager.getTransaction().commit();

    climateRecordsToBeDeleted.add(climateRecordOne);
    climateRecordsToBeDeleted.add(climateRecordTwo);
    climateRecordsToBeDeleted.add(climateRecordThree);
    climateRecordsToBeDeleted.add(climateRecordFour);
    climateRecordsToBeDeleted.add(climateRecordFive);
    climateRecordsToBeDeleted.add(climateRecordSix);

    /*
     * Persistencia de registros de riego de prueba
     */
    IrrigationRecord irrigationRecordOne = new IrrigationRecord();
    irrigationRecordOne.setDate(dayOne);
    irrigationRecordOne.setParcel(givenParcel);
    irrigationRecordOne.setIrrigationDone(5);

    IrrigationRecord irrigationRecordTwo = new IrrigationRecord();
    irrigationRecordTwo.setDate(dayTwo);
    irrigationRecordTwo.setParcel(givenParcel);
    irrigationRecordTwo.setIrrigationDone(2);

    IrrigationRecord irrigationRecordThree = new IrrigationRecord();
    irrigationRecordThree.setDate(dayThree);
    irrigationRecordThree.setParcel(givenParcel);
    irrigationRecordThree.setIrrigationDone(1);

    IrrigationRecord irrigationRecordFour = new IrrigationRecord();
    irrigationRecordFour.setDate(dayFour);
    irrigationRecordFour.setParcel(givenParcel);
    irrigationRecordFour.setIrrigationDone(1);

    IrrigationRecord irrigationRecordFive = new IrrigationRecord();
    irrigationRecordFive.setDate(dayFive);
    irrigationRecordFive.setParcel(givenParcel);
    irrigationRecordFive.setIrrigationDone(0);

    IrrigationRecord irrigationRecordSix = new IrrigationRecord();
    irrigationRecordSix.setDate(daySix);
    irrigationRecordSix.setParcel(givenParcel);
    irrigationRecordSix.setIrrigationDone(2);

    entityManager.getTransaction().begin();
    irrigationRecordOne = irrigationRecordService.create(irrigationRecordOne);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    irrigationRecordTwo = irrigationRecordService.create(irrigationRecordTwo);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    irrigationRecordThree = irrigationRecordService.create(irrigationRecordThree);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    irrigationRecordFour = irrigationRecordService.create(irrigationRecordFour);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    irrigationRecordFive = irrigationRecordService.create(irrigationRecordFive);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    irrigationRecordSix = irrigationRecordService.create(irrigationRecordSix);
    entityManager.getTransaction().commit();

    irrigationRecordsToBeDeleted.add(irrigationRecordOne);
    irrigationRecordsToBeDeleted.add(irrigationRecordTwo);
    irrigationRecordsToBeDeleted.add(irrigationRecordThree);
    irrigationRecordsToBeDeleted.add(irrigationRecordFour);
    irrigationRecordsToBeDeleted.add(irrigationRecordFive);
    irrigationRecordsToBeDeleted.add(irrigationRecordSix);

    /*
     * Recupera los registros climaticos recientemente
     * persistidos
     */
    Collection<ClimateRecord> recoveredClimateRecords = climateRecordService.findAllByParcelIdAndPeriod(givenUser.getId(), givenParcel.getId(), dateFrom, dateUntil);

    /*
     * Recupera los registros de riego recientemente
     * persistidos
     */
    Collection<IrrigationRecord> recoveredIrrigationRecords = irrigationRecordService.findAllByParcelIdAndPeriod(givenUser.getId(), givenParcel.getId(), dateFrom, dateUntil);

    System.out.println("Los datos con los que se calculara la necesidad de agua de riego de un cultivo en la fecha actual (" + UtilDate.formatDate(presumedCurrentDate) + ") son los siguientes:");
    System.out.println();

    printWaterBalanceTable(presumedCurrentDate, recoveredClimateRecords, recoveredIrrigationRecords);

    double totalIrrigationWaterCurrentDate = 0.0;

    printAccumulatedWaterDeficitPerDay(presumedCurrentDate, recoveredClimateRecords, recoveredIrrigationRecords);
    printTotalIrrigationWaterCurrentDate(presumedCurrentDate, totalIrrigationWaterCurrentDate);

    System.out.println();
    System.out.println("Al ser el resultado de la suma entre el acumulado del deficit de agua por dia de dias previos a la fecha actual y la cantidad");
    System.out.println("total de agua de riego de la fecha actual, estrictamente menor a 0 (cero), el valor devuelto por el metodo calculateIrrigationWaterNeed");
    System.out.println("de la clase WaterNeedWit debe ser el valor absoluto del resultado de dicha suma [mm/dia]. Por lo tanto, la necesidad de agua");
    System.out.println("de riego de un cultivo en la fecha actual es mayor o igual a 0 (cero) [mm/dia].");
    System.out.println();

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");

    double expectedResult = 14.0;

    /*
     * El primer parametro de este metodo calculateIrrigationWaterNeed
     * es la cantidad total de agua de riego de una fecha dada. En este
     * caso, se le pasa el valor 0 como argumento porque suponemos que
     * la cantidad total de agua de riego de la supuesta fecha actual
     * es 0 para facilitar la tarea de probarlo.
     */
    double result = WaterNeedWit.calculateIrrigationWaterNeed(totalIrrigationWaterCurrentDate, recoveredClimateRecords, recoveredIrrigationRecords);

    System.out.println("* Valor esperado (nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateIrrigationWaterNeed (nec. agua riego");
    System.out.println("[mm/dia] de un cultivo en fecha actual): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testEighteenCalculateIrrigationWaterNeed() {
    System.out.println("************************************** Prueba dieciocho del metodo calculateIrrigationWaterNeed ***************************************");
    printDescriptionMethodToTest();

    System.out.println("# Descripcion de la prueba unitaria");
    System.out.println("Para esta prueba se utilizan 6 registros climaticos y 6 registros de riego, todos ellos previos a la fecha actual " + UtilDate.formatDate(presumedCurrentDate) + ".");
    System.out.println("Suponemos que la fecha actual es " + UtilDate.formatDate(presumedCurrentDate) + ".");
    System.out.println();

    /*
     * Fechas a partir de las cuales se recuperaran los
     * registros climaticos y los registros de riego de
     * una parcela de prueba de la base de datos subyacente
     */
    Calendar dateFrom = Calendar.getInstance();
    dateFrom.set(Calendar.YEAR, 2023);
    dateFrom.set(Calendar.MONTH, JANUARY);
    dateFrom.set(Calendar.DAY_OF_MONTH, 1);

    Calendar dateUntil = Calendar.getInstance();
    dateUntil.set(Calendar.YEAR, 2023);
    dateUntil.set(Calendar.MONTH, JANUARY);
    dateUntil.set(Calendar.DAY_OF_MONTH, 6);

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
    givenUser.setUsername("jacob");
    givenUser.setName("Jacob");
    givenUser.setLastName("temple");
    givenUser.setEmail("jacob@eservice.com");
    givenUser.setPassword("Jacob");

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
     * Persistencia de registros climaticos de prueba
     */
    ClimateRecord climateRecordOne = new ClimateRecord();
    climateRecordOne.setEtc(10);
    climateRecordOne.setDate(dayOne);
    climateRecordOne.setParcel(givenParcel);

    ClimateRecord climateRecordTwo = new ClimateRecord();
    climateRecordTwo.setEtc(5);
    climateRecordTwo.setDate(dayTwo);
    climateRecordTwo.setParcel(givenParcel);

    ClimateRecord climateRecordThree = new ClimateRecord();
    climateRecordThree.setEtc(2);
    climateRecordThree.setDate(dayThree);
    climateRecordThree.setParcel(givenParcel);

    ClimateRecord climateRecordFour = new ClimateRecord();
    climateRecordFour.setEtc(3);
    climateRecordFour.setDate(dayFour);
    climateRecordFour.setParcel(givenParcel);

    ClimateRecord climateRecordFive = new ClimateRecord();
    climateRecordFive.setEtc(4);
    climateRecordFive.setDate(dayFive);
    climateRecordFive.setParcel(givenParcel);

    ClimateRecord climateRecordSix = new ClimateRecord();
    climateRecordSix.setEtc(2);
    climateRecordSix.setDate(daySix);
    climateRecordSix.setParcel(givenParcel);

    entityManager.getTransaction().begin();
    climateRecordOne = climateRecordService.create(climateRecordOne);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordTwo = climateRecordService.create(climateRecordTwo);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordThree = climateRecordService.create(climateRecordThree);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordFour = climateRecordService.create(climateRecordFour);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordFive = climateRecordService.create(climateRecordFive);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordSix = climateRecordService.create(climateRecordSix);
    entityManager.getTransaction().commit();

    climateRecordsToBeDeleted.add(climateRecordOne);
    climateRecordsToBeDeleted.add(climateRecordTwo);
    climateRecordsToBeDeleted.add(climateRecordThree);
    climateRecordsToBeDeleted.add(climateRecordFour);
    climateRecordsToBeDeleted.add(climateRecordFive);
    climateRecordsToBeDeleted.add(climateRecordSix);

    /*
     * Persistencia de registros de riego de prueba
     */
    IrrigationRecord irrigationRecordOne = new IrrigationRecord();
    irrigationRecordOne.setDate(dayOne);
    irrigationRecordOne.setParcel(givenParcel);
    irrigationRecordOne.setIrrigationDone(5);

    IrrigationRecord irrigationRecordTwo = new IrrigationRecord();
    irrigationRecordTwo.setDate(dayTwo);
    irrigationRecordTwo.setParcel(givenParcel);
    irrigationRecordTwo.setIrrigationDone(5);

    IrrigationRecord irrigationRecordThree = new IrrigationRecord();
    irrigationRecordThree.setDate(dayThree);
    irrigationRecordThree.setParcel(givenParcel);
    irrigationRecordThree.setIrrigationDone(1);

    IrrigationRecord irrigationRecordFour = new IrrigationRecord();
    irrigationRecordFour.setDate(dayFour);
    irrigationRecordFour.setParcel(givenParcel);
    irrigationRecordFour.setIrrigationDone(3);

    IrrigationRecord irrigationRecordFive = new IrrigationRecord();
    irrigationRecordFive.setDate(dayFive);
    irrigationRecordFive.setParcel(givenParcel);
    irrigationRecordFive.setIrrigationDone(1);

    IrrigationRecord irrigationRecordSix = new IrrigationRecord();
    irrigationRecordSix.setDate(daySix);
    irrigationRecordSix.setParcel(givenParcel);
    irrigationRecordSix.setIrrigationDone(2);

    entityManager.getTransaction().begin();
    irrigationRecordOne = irrigationRecordService.create(irrigationRecordOne);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    irrigationRecordTwo = irrigationRecordService.create(irrigationRecordTwo);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    irrigationRecordThree = irrigationRecordService.create(irrigationRecordThree);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    irrigationRecordFour = irrigationRecordService.create(irrigationRecordFour);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    irrigationRecordFive = irrigationRecordService.create(irrigationRecordFive);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    irrigationRecordSix = irrigationRecordService.create(irrigationRecordSix);
    entityManager.getTransaction().commit();

    irrigationRecordsToBeDeleted.add(irrigationRecordOne);
    irrigationRecordsToBeDeleted.add(irrigationRecordTwo);
    irrigationRecordsToBeDeleted.add(irrigationRecordThree);
    irrigationRecordsToBeDeleted.add(irrigationRecordFour);
    irrigationRecordsToBeDeleted.add(irrigationRecordFive);
    irrigationRecordsToBeDeleted.add(irrigationRecordSix);

    /*
     * Recupera los registros climaticos recientemente
     * persistidos
     */
    Collection<ClimateRecord> recoveredClimateRecords = climateRecordService.findAllByParcelIdAndPeriod(givenUser.getId(), givenParcel.getId(), dateFrom, dateUntil);

    /*
     * Recupera los registros de riego recientemente
     * persistidos
     */
    Collection<IrrigationRecord> recoveredIrrigationRecords = irrigationRecordService.findAllByParcelIdAndPeriod(givenUser.getId(), givenParcel.getId(), dateFrom, dateUntil);

    System.out.println("Los datos con los que se calculara la necesidad de agua de riego de un cultivo en la fecha actual (" + UtilDate.formatDate(presumedCurrentDate) + ") son los siguientes:");
    System.out.println();

    printWaterBalanceTable(presumedCurrentDate, recoveredClimateRecords, recoveredIrrigationRecords);

    double totalIrrigationWaterCurrentDate = 0.0;

    printAccumulatedWaterDeficitPerDay(presumedCurrentDate, recoveredClimateRecords, recoveredIrrigationRecords);
    printTotalIrrigationWaterCurrentDate(presumedCurrentDate, totalIrrigationWaterCurrentDate);

    System.out.println();
    System.out.println("Al ser el resultado de la suma entre el acumulado del deficit de agua por dia de dias previos a la fecha actual y la cantidad");
    System.out.println("total de agua de riego de la fecha actual, estrictamente menor a 0 (cero), el valor devuelto por el metodo calculateIrrigationWaterNeed");
    System.out.println("de la clase WaterNeedWit debe ser el valor absoluto del resultado de dicha suma [mm/dia]. Por lo tanto, la necesidad de agua");
    System.out.println("de riego de un cultivo en la fecha actual es mayor o igual a 0 (cero) [mm/dia].");
    System.out.println();

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");

    double expectedResult = 9.0;

    /*
     * El primer parametro de este metodo calculateIrrigationWaterNeed
     * es la cantidad total de agua de riego de una fecha dada. En este
     * caso, se le pasa el valor 0 como argumento porque suponemos que
     * la cantidad total de agua de riego de la supuesta fecha actual
     * es 0 para facilitar la tarea de probarlo.
     */
    double result = WaterNeedWit.calculateIrrigationWaterNeed(totalIrrigationWaterCurrentDate, recoveredClimateRecords, recoveredIrrigationRecords);

    System.out.println("* Valor esperado (nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateIrrigationWaterNeed (nec. agua riego");
    System.out.println("[mm/dia] de un cultivo en fecha actual): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testNineteenCalculateIrrigationWaterNeed() {
    System.out.println("************************************** Prueba diecinueve del metodo calculateIrrigationWaterNeed ***************************************");
    printDescriptionMethodToTest();

    System.out.println("# Descripcion de la prueba unitaria");
    System.out.println("Para esta prueba se utilizan 6 registros climaticos y 6 registros de riego, todos ellos previos a la fecha actual " + UtilDate.formatDate(presumedCurrentDate) + ".");
    System.out.println("Suponemos que la fecha actual es " + UtilDate.formatDate(presumedCurrentDate) + ".");
    System.out.println();

    /*
     * Fechas a partir de las cuales se recuperaran los
     * registros climaticos y los registros de riego de
     * una parcela de prueba de la base de datos subyacente
     */
    Calendar dateFrom = Calendar.getInstance();
    dateFrom.set(Calendar.YEAR, 2023);
    dateFrom.set(Calendar.MONTH, JANUARY);
    dateFrom.set(Calendar.DAY_OF_MONTH, 1);

    Calendar dateUntil = Calendar.getInstance();
    dateUntil.set(Calendar.YEAR, 2023);
    dateUntil.set(Calendar.MONTH, JANUARY);
    dateUntil.set(Calendar.DAY_OF_MONTH, 6);

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
    givenUser.setUsername("isaac");
    givenUser.setName("Isaac");
    givenUser.setLastName("Clarke");
    givenUser.setEmail("isaac@eservice.com");
    givenUser.setPassword("Isaac");

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
     * Persistencia de registros climaticos de prueba
     */
    ClimateRecord climateRecordOne = new ClimateRecord();
    climateRecordOne.setEtc(10);
    climateRecordOne.setDate(dayOne);
    climateRecordOne.setParcel(givenParcel);

    ClimateRecord climateRecordTwo = new ClimateRecord();
    climateRecordTwo.setEtc(5);
    climateRecordTwo.setDate(dayTwo);
    climateRecordTwo.setParcel(givenParcel);

    ClimateRecord climateRecordThree = new ClimateRecord();
    climateRecordThree.setEtc(2);
    climateRecordThree.setDate(dayThree);
    climateRecordThree.setParcel(givenParcel);

    ClimateRecord climateRecordFour = new ClimateRecord();
    climateRecordFour.setEtc(3);
    climateRecordFour.setDate(dayFour);
    climateRecordFour.setParcel(givenParcel);

    ClimateRecord climateRecordFive = new ClimateRecord();
    climateRecordFive.setEtc(4);
    climateRecordFive.setDate(dayFive);
    climateRecordFive.setParcel(givenParcel);

    ClimateRecord climateRecordSix = new ClimateRecord();
    climateRecordSix.setEtc(2);
    climateRecordSix.setDate(daySix);
    climateRecordSix.setParcel(givenParcel);

    entityManager.getTransaction().begin();
    climateRecordOne = climateRecordService.create(climateRecordOne);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordTwo = climateRecordService.create(climateRecordTwo);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordThree = climateRecordService.create(climateRecordThree);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordFour = climateRecordService.create(climateRecordFour);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordFive = climateRecordService.create(climateRecordFive);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordSix = climateRecordService.create(climateRecordSix);
    entityManager.getTransaction().commit();

    climateRecordsToBeDeleted.add(climateRecordOne);
    climateRecordsToBeDeleted.add(climateRecordTwo);
    climateRecordsToBeDeleted.add(climateRecordThree);
    climateRecordsToBeDeleted.add(climateRecordFour);
    climateRecordsToBeDeleted.add(climateRecordFive);
    climateRecordsToBeDeleted.add(climateRecordSix);

    /*
     * Persistencia de registros de riego de prueba
     */
    IrrigationRecord irrigationRecordOne = new IrrigationRecord();
    irrigationRecordOne.setDate(dayOne);
    irrigationRecordOne.setParcel(givenParcel);
    irrigationRecordOne.setIrrigationDone(5);

    IrrigationRecord irrigationRecordTwo = new IrrigationRecord();
    irrigationRecordTwo.setDate(dayTwo);
    irrigationRecordTwo.setParcel(givenParcel);
    irrigationRecordTwo.setIrrigationDone(5);

    IrrigationRecord irrigationRecordThree = new IrrigationRecord();
    irrigationRecordThree.setDate(dayThree);
    irrigationRecordThree.setParcel(givenParcel);
    irrigationRecordThree.setIrrigationDone(1);

    IrrigationRecord irrigationRecordFour = new IrrigationRecord();
    irrigationRecordFour.setDate(dayFour);
    irrigationRecordFour.setParcel(givenParcel);
    irrigationRecordFour.setIrrigationDone(3);

    IrrigationRecord irrigationRecordFive = new IrrigationRecord();
    irrigationRecordFive.setDate(dayFive);
    irrigationRecordFive.setParcel(givenParcel);
    irrigationRecordFive.setIrrigationDone(5);

    IrrigationRecord irrigationRecordSix = new IrrigationRecord();
    irrigationRecordSix.setDate(daySix);
    irrigationRecordSix.setParcel(givenParcel);
    irrigationRecordSix.setIrrigationDone(2);

    entityManager.getTransaction().begin();
    irrigationRecordOne = irrigationRecordService.create(irrigationRecordOne);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    irrigationRecordTwo = irrigationRecordService.create(irrigationRecordTwo);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    irrigationRecordThree = irrigationRecordService.create(irrigationRecordThree);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    irrigationRecordFour = irrigationRecordService.create(irrigationRecordFour);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    irrigationRecordFive = irrigationRecordService.create(irrigationRecordFive);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    irrigationRecordSix = irrigationRecordService.create(irrigationRecordSix);
    entityManager.getTransaction().commit();

    irrigationRecordsToBeDeleted.add(irrigationRecordOne);
    irrigationRecordsToBeDeleted.add(irrigationRecordTwo);
    irrigationRecordsToBeDeleted.add(irrigationRecordThree);
    irrigationRecordsToBeDeleted.add(irrigationRecordFour);
    irrigationRecordsToBeDeleted.add(irrigationRecordFive);
    irrigationRecordsToBeDeleted.add(irrigationRecordSix);

    /*
     * Recupera los registros climaticos recientemente
     * persistidos
     */
    Collection<ClimateRecord> recoveredClimateRecords = climateRecordService.findAllByParcelIdAndPeriod(givenUser.getId(), givenParcel.getId(), dateFrom, dateUntil);

    /*
     * Recupera los registros de riego recientemente
     * persistidos
     */
    Collection<IrrigationRecord> recoveredIrrigationRecords = irrigationRecordService.findAllByParcelIdAndPeriod(givenUser.getId(), givenParcel.getId(), dateFrom, dateUntil);

    System.out.println("Los datos con los que se calculara la necesidad de agua de riego de un cultivo en la fecha actual (" + UtilDate.formatDate(presumedCurrentDate) + ") son los siguientes:");
    System.out.println();

    printWaterBalanceTable(presumedCurrentDate, recoveredClimateRecords, recoveredIrrigationRecords);

    double totalIrrigationWaterCurrentDate = 0.0;

    printAccumulatedWaterDeficitPerDay(presumedCurrentDate, recoveredClimateRecords, recoveredIrrigationRecords);
    printTotalIrrigationWaterCurrentDate(presumedCurrentDate, totalIrrigationWaterCurrentDate);

    System.out.println();
    System.out.println("Al ser el resultado de la suma entre el acumulado del deficit de agua por dia de dias previos a la fecha actual y la cantidad");
    System.out.println("total de agua de riego de la fecha actual, estrictamente menor a 0 (cero), el valor devuelto por el metodo calculateIrrigationWaterNeed");
    System.out.println("de la clase WaterNeedWit debe ser el valor absoluto del resultado de dicha suma [mm/dia]. Por lo tanto, la necesidad de agua");
    System.out.println("de riego de un cultivo en la fecha actual es mayor o igual a 0 (cero) [mm/dia].");
    System.out.println();

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");

    double expectedResult = 5.0;

    /*
     * El primer parametro de este metodo calculateIrrigationWaterNeed
     * es la cantidad total de agua de riego de una fecha dada. En este
     * caso, se le pasa el valor 0 como argumento porque suponemos que
     * la cantidad total de agua de riego de la supuesta fecha actual
     * es 0 para facilitar la tarea de probarlo.
     */
    double result = WaterNeedWit.calculateIrrigationWaterNeed(totalIrrigationWaterCurrentDate, recoveredClimateRecords, recoveredIrrigationRecords);

    System.out.println("* Valor esperado (nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateIrrigationWaterNeed (nec. agua riego");
    System.out.println("[mm/dia] de un cultivo en fecha actual): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testTwentyCalculateIrrigationWaterNeed() {
    System.out.println("************************************** Prueba veinte del metodo calculateIrrigationWaterNeed ***************************************");
    printDescriptionMethodToTest();

    System.out.println("# Descripcion de la prueba unitaria");
    System.out.println("Para esta prueba se utilizan 6 registros climaticos y 6 registros de riego, todos ellos previos a la fecha actual " + UtilDate.formatDate(presumedCurrentDate) + ".");
    System.out.println("Suponemos que la fecha actual es " + UtilDate.formatDate(presumedCurrentDate) + ".");
    System.out.println();

    /*
     * Fechas a partir de las cuales se recuperaran los
     * registros climaticos y los registros de riego de
     * una parcela de prueba de la base de datos subyacente
     */
    Calendar dateFrom = Calendar.getInstance();
    dateFrom.set(Calendar.YEAR, 2023);
    dateFrom.set(Calendar.MONTH, JANUARY);
    dateFrom.set(Calendar.DAY_OF_MONTH, 1);

    Calendar dateUntil = Calendar.getInstance();
    dateUntil.set(Calendar.YEAR, 2023);
    dateUntil.set(Calendar.MONTH, JANUARY);
    dateUntil.set(Calendar.DAY_OF_MONTH, 6);

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
    givenUser.setUsername("zach");
    givenUser.setName("Hammond");
    givenUser.setLastName("hammond");
    givenUser.setEmail("zach@eservice.com");
    givenUser.setPassword("Hammond");

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
     * Persistencia de registros climaticos de prueba
     */
    ClimateRecord climateRecordOne = new ClimateRecord();
    climateRecordOne.setEtc(10);
    climateRecordOne.setDate(dayOne);
    climateRecordOne.setParcel(givenParcel);

    ClimateRecord climateRecordTwo = new ClimateRecord();
    climateRecordTwo.setEtc(5);
    climateRecordTwo.setDate(dayTwo);
    climateRecordTwo.setParcel(givenParcel);

    ClimateRecord climateRecordThree = new ClimateRecord();
    climateRecordThree.setEtc(2);
    climateRecordThree.setDate(dayThree);
    climateRecordThree.setParcel(givenParcel);

    ClimateRecord climateRecordFour = new ClimateRecord();
    climateRecordFour.setEtc(3);
    climateRecordFour.setDate(dayFour);
    climateRecordFour.setParcel(givenParcel);

    ClimateRecord climateRecordFive = new ClimateRecord();
    climateRecordFive.setEtc(1);
    climateRecordFive.setDate(dayFive);
    climateRecordFive.setParcel(givenParcel);

    ClimateRecord climateRecordSix = new ClimateRecord();
    climateRecordSix.setEtc(2.5);
    climateRecordSix.setDate(daySix);
    climateRecordSix.setParcel(givenParcel);

    entityManager.getTransaction().begin();
    climateRecordOne = climateRecordService.create(climateRecordOne);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordTwo = climateRecordService.create(climateRecordTwo);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordThree = climateRecordService.create(climateRecordThree);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordFour = climateRecordService.create(climateRecordFour);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordFive = climateRecordService.create(climateRecordFive);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordSix = climateRecordService.create(climateRecordSix);
    entityManager.getTransaction().commit();

    climateRecordsToBeDeleted.add(climateRecordOne);
    climateRecordsToBeDeleted.add(climateRecordTwo);
    climateRecordsToBeDeleted.add(climateRecordThree);
    climateRecordsToBeDeleted.add(climateRecordFour);
    climateRecordsToBeDeleted.add(climateRecordFive);
    climateRecordsToBeDeleted.add(climateRecordSix);

    /*
     * Persistencia de registros de riego de prueba
     */
    IrrigationRecord irrigationRecordOne = new IrrigationRecord();
    irrigationRecordOne.setDate(dayOne);
    irrigationRecordOne.setParcel(givenParcel);
    irrigationRecordOne.setIrrigationDone(15);

    IrrigationRecord irrigationRecordTwo = new IrrigationRecord();
    irrigationRecordTwo.setDate(dayTwo);
    irrigationRecordTwo.setParcel(givenParcel);
    irrigationRecordTwo.setIrrigationDone(6);

    IrrigationRecord irrigationRecordThree = new IrrigationRecord();
    irrigationRecordThree.setDate(dayThree);
    irrigationRecordThree.setParcel(givenParcel);
    irrigationRecordThree.setIrrigationDone(3);

    IrrigationRecord irrigationRecordFour = new IrrigationRecord();
    irrigationRecordFour.setDate(dayFour);
    irrigationRecordFour.setParcel(givenParcel);
    irrigationRecordFour.setIrrigationDone(4);

    IrrigationRecord irrigationRecordFive = new IrrigationRecord();
    irrigationRecordFive.setDate(dayFive);
    irrigationRecordFive.setParcel(givenParcel);
    irrigationRecordFive.setIrrigationDone(2);

    IrrigationRecord irrigationRecordSix = new IrrigationRecord();
    irrigationRecordSix.setDate(daySix);
    irrigationRecordSix.setParcel(givenParcel);
    irrigationRecordSix.setIrrigationDone(5);

    entityManager.getTransaction().begin();
    irrigationRecordOne = irrigationRecordService.create(irrigationRecordOne);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    irrigationRecordTwo = irrigationRecordService.create(irrigationRecordTwo);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    irrigationRecordThree = irrigationRecordService.create(irrigationRecordThree);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    irrigationRecordFour = irrigationRecordService.create(irrigationRecordFour);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    irrigationRecordFive = irrigationRecordService.create(irrigationRecordFive);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    irrigationRecordSix = irrigationRecordService.create(irrigationRecordSix);
    entityManager.getTransaction().commit();

    irrigationRecordsToBeDeleted.add(irrigationRecordOne);
    irrigationRecordsToBeDeleted.add(irrigationRecordTwo);
    irrigationRecordsToBeDeleted.add(irrigationRecordThree);
    irrigationRecordsToBeDeleted.add(irrigationRecordFour);
    irrigationRecordsToBeDeleted.add(irrigationRecordFive);
    irrigationRecordsToBeDeleted.add(irrigationRecordSix);

    /*
     * Recupera los registros climaticos recientemente
     * persistidos
     */
    Collection<ClimateRecord> recoveredClimateRecords = climateRecordService.findAllByParcelIdAndPeriod(givenUser.getId(), givenParcel.getId(), dateFrom, dateUntil);

    /*
     * Recupera los registros de riego recientemente
     * persistidos
     */
    Collection<IrrigationRecord> recoveredIrrigationRecords = irrigationRecordService.findAllByParcelIdAndPeriod(givenUser.getId(), givenParcel.getId(), dateFrom, dateUntil);

    System.out.println("Los datos con los que se calculara la necesidad de agua de riego de un cultivo en la fecha actual (" + UtilDate.formatDate(presumedCurrentDate) + ") son los siguientes:");
    System.out.println();

    printWaterBalanceTable(presumedCurrentDate, recoveredClimateRecords, recoveredIrrigationRecords);

    double totalIrrigationWaterCurrentDate = 0.0;

    printAccumulatedWaterDeficitPerDay(presumedCurrentDate, recoveredClimateRecords, recoveredIrrigationRecords);
    printTotalIrrigationWaterCurrentDate(presumedCurrentDate, totalIrrigationWaterCurrentDate);

    System.out.println();
    System.out.println("Al ser el resultado de la suma entre el acumulado del deficit de agua por dia de dias previos a la fecha actual y la cantidad");
    System.out.println("total de agua de riego de la fecha actual, mayor o igual a 0 (cero), el valor devuelto por el metodo calculateIrrigationWaterNeed");
    System.out.println("de la clase WaterNeedWit debe ser 0 [mm/dia]. Por lo tanto, la necesidad de agua de riego de un cultivo en la fecha actual es 0");
    System.out.println("[mm/dia].");
    System.out.println();

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");

    double expectedResult = 0.0;

    /*
     * El primer parametro de este metodo calculateIrrigationWaterNeed
     * es la cantidad total de agua de riego de una fecha dada. En este
     * caso, se le pasa el valor 0 como argumento porque suponemos que
     * la cantidad total de agua de riego de la supuesta fecha actual
     * es 0 para facilitar la tarea de probarlo.
     */
    double result = WaterNeedWit.calculateIrrigationWaterNeed(totalIrrigationWaterCurrentDate, recoveredClimateRecords, recoveredIrrigationRecords);

    System.out.println("* Valor esperado (nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateIrrigationWaterNeed (nec. agua riego");
    System.out.println("[mm/dia] de un cultivo en fecha actual): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testTwentyOneCalculateIrrigationWaterNeed() {
    System.out.println("************************************** Prueba veintiuno del metodo calculateIrrigationWaterNeed ***************************************");
    printDescriptionMethodToTest();

    System.out.println("# Descripcion de la prueba unitaria");
    System.out.println("Para esta prueba se utilizan 6 registros climaticos y 6 registros de riego, todos ellos previos a la fecha actual " + UtilDate.formatDate(presumedCurrentDate) + ".");
    System.out.println("Suponemos que la fecha actual es " + UtilDate.formatDate(presumedCurrentDate) + ".");
    System.out.println();

    /*
     * Fechas a partir de las cuales se recuperaran los
     * registros climaticos y los registros de riego de
     * una parcela de prueba de la base de datos subyacente
     */
    Calendar dateFrom = Calendar.getInstance();
    dateFrom.set(Calendar.YEAR, 2023);
    dateFrom.set(Calendar.MONTH, JANUARY);
    dateFrom.set(Calendar.DAY_OF_MONTH, 1);

    Calendar dateUntil = Calendar.getInstance();
    dateUntil.set(Calendar.YEAR, 2023);
    dateUntil.set(Calendar.MONTH, JANUARY);
    dateUntil.set(Calendar.DAY_OF_MONTH, 6);

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
    givenUser.setUsername("chen");
    givenUser.setName("Chen");
    givenUser.setLastName("temple");
    givenUser.setEmail("chen@eservice.com");
    givenUser.setPassword("Chen");

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
     * Persistencia de registros climaticos de prueba
     */
    ClimateRecord climateRecordOne = new ClimateRecord();
    climateRecordOne.setEtc(10);
    climateRecordOne.setDate(dayOne);
    climateRecordOne.setParcel(givenParcel);

    ClimateRecord climateRecordTwo = new ClimateRecord();
    climateRecordTwo.setEtc(5);
    climateRecordTwo.setDate(dayTwo);
    climateRecordTwo.setParcel(givenParcel);

    ClimateRecord climateRecordThree = new ClimateRecord();
    climateRecordThree.setEtc(2);
    climateRecordThree.setDate(dayThree);
    climateRecordThree.setParcel(givenParcel);

    ClimateRecord climateRecordFour = new ClimateRecord();
    climateRecordFour.setEtc(3);
    climateRecordFour.setDate(dayFour);
    climateRecordFour.setParcel(givenParcel);

    ClimateRecord climateRecordFive = new ClimateRecord();
    climateRecordFive.setEtc(1);
    climateRecordFive.setDate(dayFive);
    climateRecordFive.setParcel(givenParcel);

    ClimateRecord climateRecordSix = new ClimateRecord();
    climateRecordSix.setEtc(2.5);
    climateRecordSix.setDate(daySix);
    climateRecordSix.setParcel(givenParcel);

    entityManager.getTransaction().begin();
    climateRecordOne = climateRecordService.create(climateRecordOne);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordTwo = climateRecordService.create(climateRecordTwo);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordThree = climateRecordService.create(climateRecordThree);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordFour = climateRecordService.create(climateRecordFour);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordFive = climateRecordService.create(climateRecordFive);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordSix = climateRecordService.create(climateRecordSix);
    entityManager.getTransaction().commit();

    climateRecordsToBeDeleted.add(climateRecordOne);
    climateRecordsToBeDeleted.add(climateRecordTwo);
    climateRecordsToBeDeleted.add(climateRecordThree);
    climateRecordsToBeDeleted.add(climateRecordFour);
    climateRecordsToBeDeleted.add(climateRecordFive);
    climateRecordsToBeDeleted.add(climateRecordSix);

    /*
     * Persistencia de registros de riego de prueba
     */
    IrrigationRecord irrigationRecordOne = new IrrigationRecord();
    irrigationRecordOne.setDate(dayOne);
    irrigationRecordOne.setParcel(givenParcel);
    irrigationRecordOne.setIrrigationDone(10);

    IrrigationRecord irrigationRecordTwo = new IrrigationRecord();
    irrigationRecordTwo.setDate(dayTwo);
    irrigationRecordTwo.setParcel(givenParcel);
    irrigationRecordTwo.setIrrigationDone(5);

    IrrigationRecord irrigationRecordThree = new IrrigationRecord();
    irrigationRecordThree.setDate(dayThree);
    irrigationRecordThree.setParcel(givenParcel);
    irrigationRecordThree.setIrrigationDone(2);

    IrrigationRecord irrigationRecordFour = new IrrigationRecord();
    irrigationRecordFour.setDate(dayFour);
    irrigationRecordFour.setParcel(givenParcel);
    irrigationRecordFour.setIrrigationDone(3);

    IrrigationRecord irrigationRecordFive = new IrrigationRecord();
    irrigationRecordFive.setDate(dayFive);
    irrigationRecordFive.setParcel(givenParcel);
    irrigationRecordFive.setIrrigationDone(1);

    IrrigationRecord irrigationRecordSix = new IrrigationRecord();
    irrigationRecordSix.setDate(daySix);
    irrigationRecordSix.setParcel(givenParcel);
    irrigationRecordSix.setIrrigationDone(2.5);

    entityManager.getTransaction().begin();
    irrigationRecordOne = irrigationRecordService.create(irrigationRecordOne);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    irrigationRecordTwo = irrigationRecordService.create(irrigationRecordTwo);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    irrigationRecordThree = irrigationRecordService.create(irrigationRecordThree);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    irrigationRecordFour = irrigationRecordService.create(irrigationRecordFour);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    irrigationRecordFive = irrigationRecordService.create(irrigationRecordFive);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    irrigationRecordSix = irrigationRecordService.create(irrigationRecordSix);
    entityManager.getTransaction().commit();

    irrigationRecordsToBeDeleted.add(irrigationRecordOne);
    irrigationRecordsToBeDeleted.add(irrigationRecordTwo);
    irrigationRecordsToBeDeleted.add(irrigationRecordThree);
    irrigationRecordsToBeDeleted.add(irrigationRecordFour);
    irrigationRecordsToBeDeleted.add(irrigationRecordFive);
    irrigationRecordsToBeDeleted.add(irrigationRecordSix);

    /*
     * Recupera los registros climaticos recientemente
     * persistidos
     */
    Collection<ClimateRecord> recoveredClimateRecords = climateRecordService.findAllByParcelIdAndPeriod(givenUser.getId(), givenParcel.getId(), dateFrom, dateUntil);

    /*
     * Recupera los registros de riego recientemente
     * persistidos
     */
    Collection<IrrigationRecord> recoveredIrrigationRecords = irrigationRecordService.findAllByParcelIdAndPeriod(givenUser.getId(), givenParcel.getId(), dateFrom, dateUntil);

    System.out.println("Los datos con los que se calculara la necesidad de agua de riego de un cultivo en la fecha actual (" + UtilDate.formatDate(presumedCurrentDate) + ") son los siguientes:");
    System.out.println();

    printWaterBalanceTable(presumedCurrentDate, recoveredClimateRecords, recoveredIrrigationRecords);

    double totalIrrigationWaterCurrentDate = 0.0;

    printAccumulatedWaterDeficitPerDay(presumedCurrentDate, recoveredClimateRecords, recoveredIrrigationRecords);
    printTotalIrrigationWaterCurrentDate(presumedCurrentDate, totalIrrigationWaterCurrentDate);

    System.out.println();
    System.out.println("Al ser el resultado de la suma entre el acumulado del deficit de agua por dia de dias previos a la fecha actual y la cantidad");
    System.out.println("total de agua de riego de la fecha actual, mayor o igual a 0 (cero), el valor devuelto por el metodo calculateIrrigationWaterNeed");
    System.out.println("de la clase WaterNeedWit debe ser 0 [mm/dia]. Por lo tanto, la necesidad de agua de riego de un cultivo en la fecha actual es 0");
    System.out.println("[mm/dia].");
    System.out.println();

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");

    double expectedResult = 0.0;

    /*
     * El primer parametro de este metodo calculateIrrigationWaterNeed
     * es la cantidad total de agua de riego de una fecha dada. En este
     * caso, se le pasa el valor 0 como argumento porque suponemos que
     * la cantidad total de agua de riego de la supuesta fecha actual
     * es 0 para facilitar la tarea de probarlo.
     */
    double result = WaterNeedWit.calculateIrrigationWaterNeed(totalIrrigationWaterCurrentDate, recoveredClimateRecords, recoveredIrrigationRecords);

    System.out.println("* Valor esperado (nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateIrrigationWaterNeed (nec. agua riego");
    System.out.println("[mm/dia] de un cultivo en fecha actual): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testTwentyTwoCalculateIrrigationWaterNeed() {
    System.out.println("************************************** Prueba veintidos del metodo calculateIrrigationWaterNeed ***************************************");
    printDescriptionMethodToTest();

    System.out.println("# Descripcion de la prueba unitaria");
    System.out.println("Para esta prueba se utilizan 6 registros climaticos y 6 registros de riego, todos ellos previos a la fecha actual " + UtilDate.formatDate(presumedCurrentDate) + ".");
    System.out.println("Suponemos que la fecha actual es " + UtilDate.formatDate(presumedCurrentDate) + ".");
    System.out.println();

    /*
     * Fechas a partir de las cuales se recuperaran los
     * registros climaticos y los registros de riego de
     * una parcela de prueba de la base de datos subyacente
     */
    Calendar dateFrom = Calendar.getInstance();
    dateFrom.set(Calendar.YEAR, 2023);
    dateFrom.set(Calendar.MONTH, JANUARY);
    dateFrom.set(Calendar.DAY_OF_MONTH, 1);

    Calendar dateUntil = Calendar.getInstance();
    dateUntil.set(Calendar.YEAR, 2023);
    dateUntil.set(Calendar.MONTH, JANUARY);
    dateUntil.set(Calendar.DAY_OF_MONTH, 6);

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
    givenUser.setUsername("nicole");
    givenUser.setName("Nicole");
    givenUser.setLastName("brennan");
    givenUser.setEmail("nicole@eservice.com");
    givenUser.setPassword("Nicole");

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
     * Persistencia de registros climaticos de prueba
     */
    ClimateRecord climateRecordOne = new ClimateRecord();
    climateRecordOne.setEtc(10);
    climateRecordOne.setDate(dayOne);
    climateRecordOne.setParcel(givenParcel);

    ClimateRecord climateRecordTwo = new ClimateRecord();
    climateRecordTwo.setEtc(5);
    climateRecordTwo.setDate(dayTwo);
    climateRecordTwo.setParcel(givenParcel);

    ClimateRecord climateRecordThree = new ClimateRecord();
    climateRecordThree.setEtc(2);
    climateRecordThree.setDate(dayThree);
    climateRecordThree.setParcel(givenParcel);

    ClimateRecord climateRecordFour = new ClimateRecord();
    climateRecordFour.setEtc(3);
    climateRecordFour.setDate(dayFour);
    climateRecordFour.setParcel(givenParcel);

    ClimateRecord climateRecordFive = new ClimateRecord();
    climateRecordFive.setEtc(4);
    climateRecordFive.setDate(dayFive);
    climateRecordFive.setParcel(givenParcel);

    ClimateRecord climateRecordSix = new ClimateRecord();
    climateRecordSix.setEtc(2);
    climateRecordSix.setDate(daySix);
    climateRecordSix.setParcel(givenParcel);

    entityManager.getTransaction().begin();
    climateRecordOne = climateRecordService.create(climateRecordOne);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordTwo = climateRecordService.create(climateRecordTwo);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordThree = climateRecordService.create(climateRecordThree);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordFour = climateRecordService.create(climateRecordFour);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordFive = climateRecordService.create(climateRecordFive);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordSix = climateRecordService.create(climateRecordSix);
    entityManager.getTransaction().commit();

    climateRecordsToBeDeleted.add(climateRecordOne);
    climateRecordsToBeDeleted.add(climateRecordTwo);
    climateRecordsToBeDeleted.add(climateRecordThree);
    climateRecordsToBeDeleted.add(climateRecordFour);
    climateRecordsToBeDeleted.add(climateRecordFive);
    climateRecordsToBeDeleted.add(climateRecordSix);

    /*
     * Persistencia de registros de riego de prueba
     */
    IrrigationRecord irrigationRecordOne = new IrrigationRecord();
    irrigationRecordOne.setDate(dayOne);
    irrigationRecordOne.setParcel(givenParcel);
    irrigationRecordOne.setIrrigationDone(10);

    IrrigationRecord irrigationRecordTwo = new IrrigationRecord();
    irrigationRecordTwo.setDate(dayTwo);
    irrigationRecordTwo.setParcel(givenParcel);
    irrigationRecordTwo.setIrrigationDone(10);

    IrrigationRecord irrigationRecordThree = new IrrigationRecord();
    irrigationRecordThree.setDate(dayThree);
    irrigationRecordThree.setParcel(givenParcel);
    irrigationRecordThree.setIrrigationDone(10);

    IrrigationRecord irrigationRecordFour = new IrrigationRecord();
    irrigationRecordFour.setDate(dayFour);
    irrigationRecordFour.setParcel(givenParcel);
    irrigationRecordFour.setIrrigationDone(0);

    IrrigationRecord irrigationRecordFive = new IrrigationRecord();
    irrigationRecordFive.setDate(dayFive);
    irrigationRecordFive.setParcel(givenParcel);
    irrigationRecordFive.setIrrigationDone(1);

    IrrigationRecord irrigationRecordSix = new IrrigationRecord();
    irrigationRecordSix.setDate(daySix);
    irrigationRecordSix.setParcel(givenParcel);
    irrigationRecordSix.setIrrigationDone(4);

    entityManager.getTransaction().begin();
    irrigationRecordOne = irrigationRecordService.create(irrigationRecordOne);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    irrigationRecordTwo = irrigationRecordService.create(irrigationRecordTwo);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    irrigationRecordThree = irrigationRecordService.create(irrigationRecordThree);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    irrigationRecordFour = irrigationRecordService.create(irrigationRecordFour);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    irrigationRecordFive = irrigationRecordService.create(irrigationRecordFive);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    irrigationRecordSix = irrigationRecordService.create(irrigationRecordSix);
    entityManager.getTransaction().commit();

    irrigationRecordsToBeDeleted.add(irrigationRecordOne);
    irrigationRecordsToBeDeleted.add(irrigationRecordTwo);
    irrigationRecordsToBeDeleted.add(irrigationRecordThree);
    irrigationRecordsToBeDeleted.add(irrigationRecordFour);
    irrigationRecordsToBeDeleted.add(irrigationRecordFive);
    irrigationRecordsToBeDeleted.add(irrigationRecordSix);

    /*
     * Recupera los registros climaticos recientemente
     * persistidos
     */
    Collection<ClimateRecord> recoveredClimateRecords = climateRecordService.findAllByParcelIdAndPeriod(givenUser.getId(), givenParcel.getId(), dateFrom, dateUntil);

    /*
     * Recupera los registros de riego recientemente
     * persistidos
     */
    Collection<IrrigationRecord> recoveredIrrigationRecords = irrigationRecordService.findAllByParcelIdAndPeriod(givenUser.getId(), givenParcel.getId(), dateFrom, dateUntil);

    System.out.println("Los datos con los que se calculara la necesidad de agua de riego de un cultivo en la fecha actual (" + UtilDate.formatDate(presumedCurrentDate) + ") son los siguientes:");
    System.out.println();

    printWaterBalanceTable(presumedCurrentDate, recoveredClimateRecords, recoveredIrrigationRecords);

    double totalIrrigationWaterCurrentDate = 0.0;

    printAccumulatedWaterDeficitPerDay(presumedCurrentDate, recoveredClimateRecords, recoveredIrrigationRecords);
    printTotalIrrigationWaterCurrentDate(presumedCurrentDate, totalIrrigationWaterCurrentDate);

    System.out.println();
    System.out.println("Al ser el resultado de la suma entre el acumulado del deficit de agua por dia de dias previos a la fecha actual y la cantidad");
    System.out.println("total de agua de riego de la fecha actual, estrictamente menor a 0 (cero), el valor devuelto por el metodo calculateIrrigationWaterNeed");
    System.out.println("de la clase WaterNeedWit debe ser el valor absoluto del resultado de dicha suma [mm/dia]. Por lo tanto, la necesidad de agua");
    System.out.println("de riego de un cultivo en la fecha actual es mayor o igual a 0 (cero) [mm/dia].");
    System.out.println();

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");

    double expectedResult = 4.0;

    /*
     * El primer parametro de este metodo calculateIrrigationWaterNeed
     * es la cantidad total de agua de riego de una fecha dada. En este
     * caso, se le pasa el valor 0 como argumento porque suponemos que
     * la cantidad total de agua de riego de la supuesta fecha actual
     * es 0 para facilitar la tarea de probarlo.
     */
    double result = WaterNeedWit.calculateIrrigationWaterNeed(totalIrrigationWaterCurrentDate, recoveredClimateRecords, recoveredIrrigationRecords);

    System.out.println("* Valor esperado (nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateIrrigationWaterNeed (nec. agua riego");
    System.out.println("[mm/dia] de un cultivo en fecha actual): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testTwentyThreeCalculateIrrigationWaterNeed() {
    System.out.println("************************************** Prueba veintitres del metodo calculateIrrigationWaterNeed ***************************************");
    printDescriptionMethodToTest();

    System.out.println("# Descripcion de la prueba unitaria");
    System.out.println("Para esta prueba se utilizan 6 registros climaticos y 6 registros de riego, todos ellos previos a la fecha actual " + UtilDate.formatDate(presumedCurrentDate) + ".");
    System.out.println("Suponemos que la fecha actual es " + UtilDate.formatDate(presumedCurrentDate) + ".");
    System.out.println();

    /*
     * Fechas a partir de las cuales se recuperaran los
     * registros climaticos y los registros de riego de
     * una parcela de prueba de la base de datos subyacente
     */
    Calendar dateFrom = Calendar.getInstance();
    dateFrom.set(Calendar.YEAR, 2023);
    dateFrom.set(Calendar.MONTH, JANUARY);
    dateFrom.set(Calendar.DAY_OF_MONTH, 1);

    Calendar dateUntil = Calendar.getInstance();
    dateUntil.set(Calendar.YEAR, 2023);
    dateUntil.set(Calendar.MONTH, JANUARY);
    dateUntil.set(Calendar.DAY_OF_MONTH, 6);

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
    givenUser.setUsername("mercer");
    givenUser.setName("Mercer");
    givenUser.setLastName("mallus");
    givenUser.setEmail("mercer@eservice.com");
    givenUser.setPassword("Mercer");

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
     * Persistencia de registros climaticos de prueba
     */
    ClimateRecord climateRecordOne = new ClimateRecord();
    climateRecordOne.setEtc(15);
    climateRecordOne.setDate(dayOne);
    climateRecordOne.setParcel(givenParcel);

    ClimateRecord climateRecordTwo = new ClimateRecord();
    climateRecordTwo.setEtc(12);
    climateRecordTwo.setDate(dayTwo);
    climateRecordTwo.setParcel(givenParcel);

    ClimateRecord climateRecordThree = new ClimateRecord();
    climateRecordThree.setEtc(6);
    climateRecordThree.setDate(dayThree);
    climateRecordThree.setParcel(givenParcel);

    ClimateRecord climateRecordFour = new ClimateRecord();
    climateRecordFour.setEtc(2);
    climateRecordFour.setDate(dayFour);
    climateRecordFour.setParcel(givenParcel);

    ClimateRecord climateRecordFive = new ClimateRecord();
    climateRecordFive.setEtc(4);
    climateRecordFive.setDate(dayFive);
    climateRecordFive.setParcel(givenParcel);

    ClimateRecord climateRecordSix = new ClimateRecord();
    climateRecordSix.setEtc(7);
    climateRecordSix.setDate(daySix);
    climateRecordSix.setParcel(givenParcel);

    entityManager.getTransaction().begin();
    climateRecordOne = climateRecordService.create(climateRecordOne);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordTwo = climateRecordService.create(climateRecordTwo);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordThree = climateRecordService.create(climateRecordThree);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordFour = climateRecordService.create(climateRecordFour);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordFive = climateRecordService.create(climateRecordFive);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordSix = climateRecordService.create(climateRecordSix);
    entityManager.getTransaction().commit();

    climateRecordsToBeDeleted.add(climateRecordOne);
    climateRecordsToBeDeleted.add(climateRecordTwo);
    climateRecordsToBeDeleted.add(climateRecordThree);
    climateRecordsToBeDeleted.add(climateRecordFour);
    climateRecordsToBeDeleted.add(climateRecordFive);
    climateRecordsToBeDeleted.add(climateRecordSix);

    /*
     * Persistencia de registros de riego de prueba
     */
    IrrigationRecord irrigationRecordOne = new IrrigationRecord();
    irrigationRecordOne.setDate(dayOne);
    irrigationRecordOne.setParcel(givenParcel);
    irrigationRecordOne.setIrrigationDone(10);

    IrrigationRecord irrigationRecordTwo = new IrrigationRecord();
    irrigationRecordTwo.setDate(dayTwo);
    irrigationRecordTwo.setParcel(givenParcel);
    irrigationRecordTwo.setIrrigationDone(13);

    IrrigationRecord irrigationRecordThree = new IrrigationRecord();
    irrigationRecordThree.setDate(dayThree);
    irrigationRecordThree.setParcel(givenParcel);
    irrigationRecordThree.setIrrigationDone(6);

    IrrigationRecord irrigationRecordFour = new IrrigationRecord();
    irrigationRecordFour.setDate(dayFour);
    irrigationRecordFour.setParcel(givenParcel);
    irrigationRecordFour.setIrrigationDone(1);

    IrrigationRecord irrigationRecordFive = new IrrigationRecord();
    irrigationRecordFive.setDate(dayFive);
    irrigationRecordFive.setParcel(givenParcel);
    irrigationRecordFive.setIrrigationDone(2);

    IrrigationRecord irrigationRecordSix = new IrrigationRecord();
    irrigationRecordSix.setDate(daySix);
    irrigationRecordSix.setParcel(givenParcel);
    irrigationRecordSix.setIrrigationDone(7);

    entityManager.getTransaction().begin();
    irrigationRecordOne = irrigationRecordService.create(irrigationRecordOne);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    irrigationRecordTwo = irrigationRecordService.create(irrigationRecordTwo);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    irrigationRecordThree = irrigationRecordService.create(irrigationRecordThree);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    irrigationRecordFour = irrigationRecordService.create(irrigationRecordFour);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    irrigationRecordFive = irrigationRecordService.create(irrigationRecordFive);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    irrigationRecordSix = irrigationRecordService.create(irrigationRecordSix);
    entityManager.getTransaction().commit();

    irrigationRecordsToBeDeleted.add(irrigationRecordOne);
    irrigationRecordsToBeDeleted.add(irrigationRecordTwo);
    irrigationRecordsToBeDeleted.add(irrigationRecordThree);
    irrigationRecordsToBeDeleted.add(irrigationRecordFour);
    irrigationRecordsToBeDeleted.add(irrigationRecordFive);
    irrigationRecordsToBeDeleted.add(irrigationRecordSix);

    /*
     * Recupera los registros climaticos recientemente
     * persistidos
     */
    Collection<ClimateRecord> recoveredClimateRecords = climateRecordService.findAllByParcelIdAndPeriod(givenUser.getId(), givenParcel.getId(), dateFrom, dateUntil);

    /*
     * Recupera los registros de riego recientemente
     * persistidos
     */
    Collection<IrrigationRecord> recoveredIrrigationRecords = irrigationRecordService.findAllByParcelIdAndPeriod(givenUser.getId(), givenParcel.getId(), dateFrom, dateUntil);

    System.out.println("Los datos con los que se calculara la necesidad de agua de riego de un cultivo en la fecha actual (" + UtilDate.formatDate(presumedCurrentDate) + ") son los siguientes:");
    System.out.println();

    printWaterBalanceTable(presumedCurrentDate, recoveredClimateRecords, recoveredIrrigationRecords);

    double totalIrrigationWaterCurrentDate = 0.0;

    printAccumulatedWaterDeficitPerDay(presumedCurrentDate, recoveredClimateRecords, recoveredIrrigationRecords);
    printTotalIrrigationWaterCurrentDate(presumedCurrentDate, totalIrrigationWaterCurrentDate);

    System.out.println();
    System.out.println("Al ser el resultado de la suma entre el acumulado del deficit de agua por dia de dias previos a la fecha actual y la cantidad");
    System.out.println("total de agua de riego de la fecha actual, estrictamente menor a 0 (cero), el valor devuelto por el metodo calculateIrrigationWaterNeed");
    System.out.println("de la clase WaterNeedWit debe ser el valor absoluto del resultado de dicha suma [mm/dia]. Por lo tanto, la necesidad de agua");
    System.out.println("de riego de un cultivo en la fecha actual es mayor o igual a 0 (cero) [mm/dia].");
    System.out.println();

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");

    double expectedResult = 7.0;

    /*
     * El primer parametro de este metodo calculateIrrigationWaterNeed
     * es la cantidad total de agua de riego de una fecha dada. En este
     * caso, se le pasa el valor 0 como argumento porque suponemos que
     * la cantidad total de agua de riego de la supuesta fecha actual
     * es 0 para facilitar la tarea de probarlo.
     */
    double result = WaterNeedWit.calculateIrrigationWaterNeed(totalIrrigationWaterCurrentDate, recoveredClimateRecords, recoveredIrrigationRecords);

    System.out.println("* Valor esperado (nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateIrrigationWaterNeed (nec. agua riego");
    System.out.println("[mm/dia] de un cultivo en fecha actual): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testTwentyFourCalculateIrrigationWaterNeed() {
    System.out.println("************************************** Prueba veinticuatro del metodo calculateIrrigationWaterNeed ***************************************");
    printDescriptionMethodToTest();

    System.out.println("# Descripcion de la prueba unitaria");
    System.out.println("Para esta prueba se utilizan 6 registros climaticos y 6 registros de riego, todos ellos previos a la fecha actual " + UtilDate.formatDate(presumedCurrentDate) + ".");
    System.out.println("Suponemos que la fecha actual es " + UtilDate.formatDate(presumedCurrentDate) + ".");
    System.out.println();

    /*
     * Fechas a partir de las cuales se recuperaran los
     * registros climaticos y los registros de riego de
     * una parcela de prueba de la base de datos subyacente
     */
    Calendar dateFrom = Calendar.getInstance();
    dateFrom.set(Calendar.YEAR, 2023);
    dateFrom.set(Calendar.MONTH, JANUARY);
    dateFrom.set(Calendar.DAY_OF_MONTH, 1);

    Calendar dateUntil = Calendar.getInstance();
    dateUntil.set(Calendar.YEAR, 2023);
    dateUntil.set(Calendar.MONTH, JANUARY);
    dateUntil.set(Calendar.DAY_OF_MONTH, 6);

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
    givenUser.setUsername("kendra");
    givenUser.setName("Kendra");
    givenUser.setLastName("daniels");
    givenUser.setEmail("kendra@eservice.com");
    givenUser.setPassword("Kendra");

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
     * Persistencia de registros climaticos de prueba
     */
    ClimateRecord climateRecordOne = new ClimateRecord();
    climateRecordOne.setEtc(15);
    climateRecordOne.setDate(dayOne);
    climateRecordOne.setParcel(givenParcel);

    ClimateRecord climateRecordTwo = new ClimateRecord();
    climateRecordTwo.setEtc(12);
    climateRecordTwo.setDate(dayTwo);
    climateRecordTwo.setParcel(givenParcel);

    ClimateRecord climateRecordThree = new ClimateRecord();
    climateRecordThree.setEtc(6);
    climateRecordThree.setDate(dayThree);
    climateRecordThree.setParcel(givenParcel);

    ClimateRecord climateRecordFour = new ClimateRecord();
    climateRecordFour.setEtc(2);
    climateRecordFour.setDate(dayFour);
    climateRecordFour.setParcel(givenParcel);

    ClimateRecord climateRecordFive = new ClimateRecord();
    climateRecordFive.setEtc(4);
    climateRecordFive.setDate(dayFive);
    climateRecordFive.setParcel(givenParcel);

    ClimateRecord climateRecordSix = new ClimateRecord();
    climateRecordSix.setEtc(5);
    climateRecordSix.setDate(daySix);
    climateRecordSix.setParcel(givenParcel);

    entityManager.getTransaction().begin();
    climateRecordOne = climateRecordService.create(climateRecordOne);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordTwo = climateRecordService.create(climateRecordTwo);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordThree = climateRecordService.create(climateRecordThree);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordFour = climateRecordService.create(climateRecordFour);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordFive = climateRecordService.create(climateRecordFive);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordSix = climateRecordService.create(climateRecordSix);
    entityManager.getTransaction().commit();

    climateRecordsToBeDeleted.add(climateRecordOne);
    climateRecordsToBeDeleted.add(climateRecordTwo);
    climateRecordsToBeDeleted.add(climateRecordThree);
    climateRecordsToBeDeleted.add(climateRecordFour);
    climateRecordsToBeDeleted.add(climateRecordFive);
    climateRecordsToBeDeleted.add(climateRecordSix);

    /*
     * Persistencia de registros de riego de prueba
     */
    IrrigationRecord irrigationRecordOne = new IrrigationRecord();
    irrigationRecordOne.setDate(dayOne);
    irrigationRecordOne.setParcel(givenParcel);
    irrigationRecordOne.setIrrigationDone(15);

    IrrigationRecord irrigationRecordTwo = new IrrigationRecord();
    irrigationRecordTwo.setDate(dayTwo);
    irrigationRecordTwo.setParcel(givenParcel);
    irrigationRecordTwo.setIrrigationDone(16);

    IrrigationRecord irrigationRecordThree = new IrrigationRecord();
    irrigationRecordThree.setDate(dayThree);
    irrigationRecordThree.setParcel(givenParcel);
    irrigationRecordThree.setIrrigationDone(8);

    IrrigationRecord irrigationRecordFour = new IrrigationRecord();
    irrigationRecordFour.setDate(dayFour);
    irrigationRecordFour.setParcel(givenParcel);
    irrigationRecordFour.setIrrigationDone(0);

    IrrigationRecord irrigationRecordFive = new IrrigationRecord();
    irrigationRecordFive.setDate(dayFive);
    irrigationRecordFive.setParcel(givenParcel);
    irrigationRecordFive.setIrrigationDone(1);

    IrrigationRecord irrigationRecordSix = new IrrigationRecord();
    irrigationRecordSix.setDate(daySix);
    irrigationRecordSix.setParcel(givenParcel);
    irrigationRecordSix.setIrrigationDone(6);

    entityManager.getTransaction().begin();
    irrigationRecordOne = irrigationRecordService.create(irrigationRecordOne);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    irrigationRecordTwo = irrigationRecordService.create(irrigationRecordTwo);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    irrigationRecordThree = irrigationRecordService.create(irrigationRecordThree);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    irrigationRecordFour = irrigationRecordService.create(irrigationRecordFour);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    irrigationRecordFive = irrigationRecordService.create(irrigationRecordFive);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    irrigationRecordSix = irrigationRecordService.create(irrigationRecordSix);
    entityManager.getTransaction().commit();

    irrigationRecordsToBeDeleted.add(irrigationRecordOne);
    irrigationRecordsToBeDeleted.add(irrigationRecordTwo);
    irrigationRecordsToBeDeleted.add(irrigationRecordThree);
    irrigationRecordsToBeDeleted.add(irrigationRecordFour);
    irrigationRecordsToBeDeleted.add(irrigationRecordFive);
    irrigationRecordsToBeDeleted.add(irrigationRecordSix);

    /*
     * Recupera los registros climaticos recientemente
     * persistidos
     */
    Collection<ClimateRecord> recoveredClimateRecords = climateRecordService.findAllByParcelIdAndPeriod(givenUser.getId(), givenParcel.getId(), dateFrom, dateUntil);

    /*
     * Recupera los registros de riego recientemente
     * persistidos
     */
    Collection<IrrigationRecord> recoveredIrrigationRecords = irrigationRecordService.findAllByParcelIdAndPeriod(givenUser.getId(), givenParcel.getId(), dateFrom, dateUntil);

    System.out.println("Los datos con los que se calculara la necesidad de agua de riego de un cultivo en la fecha actual (" + UtilDate.formatDate(presumedCurrentDate) + ") son los siguientes:");
    System.out.println();

    printWaterBalanceTable(presumedCurrentDate, recoveredClimateRecords, recoveredIrrigationRecords);

    double totalIrrigationWaterCurrentDate = 0.0;

    printAccumulatedWaterDeficitPerDay(presumedCurrentDate, recoveredClimateRecords, recoveredIrrigationRecords);
    printTotalIrrigationWaterCurrentDate(presumedCurrentDate, totalIrrigationWaterCurrentDate);

    System.out.println();
    System.out.println("Al ser el resultado de la suma entre el acumulado del deficit de agua por dia de dias previos a la fecha actual y la cantidad");
    System.out.println("total de agua de riego de la fecha actual, estrictamente menor a 0 (cero), el valor devuelto por el metodo calculateIrrigationWaterNeed");
    System.out.println("de la clase WaterNeedWit debe ser el valor absoluto del resultado de dicha suma [mm/dia]. Por lo tanto, la necesidad de agua");
    System.out.println("de riego de un cultivo en la fecha actual es mayor o igual a 0 (cero) [mm/dia].");
    System.out.println();

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");

    double expectedResult = 4.0;

    /*
     * El primer parametro de este metodo calculateIrrigationWaterNeed
     * es la cantidad total de agua de riego de una fecha dada. En este
     * caso, se le pasa el valor 0 como argumento porque suponemos que
     * la cantidad total de agua de riego de la supuesta fecha actual
     * es 0 para facilitar la tarea de probarlo.
     */
    double result = WaterNeedWit.calculateIrrigationWaterNeed(totalIrrigationWaterCurrentDate, recoveredClimateRecords, recoveredIrrigationRecords);

    System.out.println("* Valor esperado (nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateIrrigationWaterNeed (nec. agua riego");
    System.out.println("[mm/dia] de un cultivo en fecha actual): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testTwentyFiveCalculateIrrigationWaterNeed() {
    System.out.println("************************************** Prueba veinticinco del metodo calculateIrrigationWaterNeed ***************************************");
    printDescriptionMethodToTest();

    System.out.println("# Descripcion de la prueba unitaria");
    System.out.println("Para esta prueba se utilizan 6 registros climaticos y 6 registros de riego, todos ellos previos a la fecha actual " + UtilDate.formatDate(presumedCurrentDate) + ".");
    System.out.println("Suponemos que la fecha actual es " + UtilDate.formatDate(presumedCurrentDate) + ".");
    System.out.println();

    /*
     * Fechas a partir de las cuales se recuperaran los
     * registros climaticos y los registros de riego de
     * una parcela de prueba de la base de datos subyacente
     */
    Calendar dateFrom = Calendar.getInstance();
    dateFrom.set(Calendar.YEAR, 2023);
    dateFrom.set(Calendar.MONTH, JANUARY);
    dateFrom.set(Calendar.DAY_OF_MONTH, 1);

    Calendar dateUntil = Calendar.getInstance();
    dateUntil.set(Calendar.YEAR, 2023);
    dateUntil.set(Calendar.MONTH, JANUARY);
    dateUntil.set(Calendar.DAY_OF_MONTH, 6);

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
    givenUser.setUsername("ludmila");
    givenUser.setName("Ludmila");
    givenUser.setLastName("bolt");
    givenUser.setEmail("lumdila@eservice.com");
    givenUser.setPassword("Ludmila");

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
     * Persistencia de registros climaticos de prueba
     */
    ClimateRecord climateRecordOne = new ClimateRecord();
    climateRecordOne.setEtc(15);
    climateRecordOne.setDate(dayOne);
    climateRecordOne.setParcel(givenParcel);

    ClimateRecord climateRecordTwo = new ClimateRecord();
    climateRecordTwo.setEtc(12);
    climateRecordTwo.setDate(dayTwo);
    climateRecordTwo.setParcel(givenParcel);

    ClimateRecord climateRecordThree = new ClimateRecord();
    climateRecordThree.setEtc(6);
    climateRecordThree.setDate(dayThree);
    climateRecordThree.setParcel(givenParcel);

    ClimateRecord climateRecordFour = new ClimateRecord();
    climateRecordFour.setEtc(2);
    climateRecordFour.setDate(dayFour);
    climateRecordFour.setParcel(givenParcel);

    ClimateRecord climateRecordFive = new ClimateRecord();
    climateRecordFive.setEtc(4);
    climateRecordFive.setDate(dayFive);
    climateRecordFive.setParcel(givenParcel);

    ClimateRecord climateRecordSix = new ClimateRecord();
    climateRecordSix.setEtc(5);
    climateRecordSix.setDate(daySix);
    climateRecordSix.setParcel(givenParcel);

    entityManager.getTransaction().begin();
    climateRecordOne = climateRecordService.create(climateRecordOne);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordTwo = climateRecordService.create(climateRecordTwo);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordThree = climateRecordService.create(climateRecordThree);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordFour = climateRecordService.create(climateRecordFour);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordFive = climateRecordService.create(climateRecordFive);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordSix = climateRecordService.create(climateRecordSix);
    entityManager.getTransaction().commit();

    climateRecordsToBeDeleted.add(climateRecordOne);
    climateRecordsToBeDeleted.add(climateRecordTwo);
    climateRecordsToBeDeleted.add(climateRecordThree);
    climateRecordsToBeDeleted.add(climateRecordFour);
    climateRecordsToBeDeleted.add(climateRecordFive);
    climateRecordsToBeDeleted.add(climateRecordSix);

    /*
     * Persistencia de registros de riego de prueba
     */
    IrrigationRecord irrigationRecordOne = new IrrigationRecord();
    irrigationRecordOne.setDate(dayOne);
    irrigationRecordOne.setParcel(givenParcel);
    irrigationRecordOne.setIrrigationDone(15);

    IrrigationRecord irrigationRecordTwo = new IrrigationRecord();
    irrigationRecordTwo.setDate(dayTwo);
    irrigationRecordTwo.setParcel(givenParcel);
    irrigationRecordTwo.setIrrigationDone(12);

    IrrigationRecord irrigationRecordThree = new IrrigationRecord();
    irrigationRecordThree.setDate(dayThree);
    irrigationRecordThree.setParcel(givenParcel);
    irrigationRecordThree.setIrrigationDone(2);

    IrrigationRecord irrigationRecordFour = new IrrigationRecord();
    irrigationRecordFour.setDate(dayFour);
    irrigationRecordFour.setParcel(givenParcel);
    irrigationRecordFour.setIrrigationDone(0);

    IrrigationRecord irrigationRecordFive = new IrrigationRecord();
    irrigationRecordFive.setDate(dayFive);
    irrigationRecordFive.setParcel(givenParcel);
    irrigationRecordFive.setIrrigationDone(1);

    IrrigationRecord irrigationRecordSix = new IrrigationRecord();
    irrigationRecordSix.setDate(daySix);
    irrigationRecordSix.setParcel(givenParcel);
    irrigationRecordSix.setIrrigationDone(14);

    entityManager.getTransaction().begin();
    irrigationRecordOne = irrigationRecordService.create(irrigationRecordOne);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    irrigationRecordTwo = irrigationRecordService.create(irrigationRecordTwo);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    irrigationRecordThree = irrigationRecordService.create(irrigationRecordThree);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    irrigationRecordFour = irrigationRecordService.create(irrigationRecordFour);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    irrigationRecordFive = irrigationRecordService.create(irrigationRecordFive);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    irrigationRecordSix = irrigationRecordService.create(irrigationRecordSix);
    entityManager.getTransaction().commit();

    irrigationRecordsToBeDeleted.add(irrigationRecordOne);
    irrigationRecordsToBeDeleted.add(irrigationRecordTwo);
    irrigationRecordsToBeDeleted.add(irrigationRecordThree);
    irrigationRecordsToBeDeleted.add(irrigationRecordFour);
    irrigationRecordsToBeDeleted.add(irrigationRecordFive);
    irrigationRecordsToBeDeleted.add(irrigationRecordSix);

    /*
     * Recupera los registros climaticos recientemente
     * persistidos
     */
    Collection<ClimateRecord> recoveredClimateRecords = climateRecordService.findAllByParcelIdAndPeriod(givenUser.getId(), givenParcel.getId(), dateFrom, dateUntil);

    /*
     * Recupera los registros de riego recientemente
     * persistidos
     */
    Collection<IrrigationRecord> recoveredIrrigationRecords = irrigationRecordService.findAllByParcelIdAndPeriod(givenUser.getId(), givenParcel.getId(), dateFrom, dateUntil);

    System.out.println("Los datos con los que se calculara la necesidad de agua de riego de un cultivo en la fecha actual (" + UtilDate.formatDate(presumedCurrentDate) + ") son los siguientes:");
    System.out.println();

    printWaterBalanceTable(presumedCurrentDate, recoveredClimateRecords, recoveredIrrigationRecords);

    double totalIrrigationWaterCurrentDate = 0.0;

    printAccumulatedWaterDeficitPerDay(presumedCurrentDate, recoveredClimateRecords, recoveredIrrigationRecords);
    printTotalIrrigationWaterCurrentDate(presumedCurrentDate, totalIrrigationWaterCurrentDate);

    System.out.println();
    System.out.println("Al ser el resultado de la suma entre el acumulado del deficit de agua por dia de dias previos a la fecha actual y la cantidad");
    System.out.println("total de agua de riego de la fecha actual, mayor o igual a 0 (cero), el valor devuelto por el metodo calculateIrrigationWaterNeed");
    System.out.println("de la clase WaterNeedWit debe ser 0 [mm/dia]. Por lo tanto, la necesidad de agua de riego de un cultivo en la fecha actual es 0");
    System.out.println("[mm/dia].");
    System.out.println();

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");

    double expectedResult = 0.0;

    /*
     * El primer parametro de este metodo calculateIrrigationWaterNeed
     * es la cantidad total de agua de riego de una fecha dada. En este
     * caso, se le pasa el valor 0 como argumento porque suponemos que
     * la cantidad total de agua de riego de la supuesta fecha actual
     * es 0 para facilitar la tarea de probarlo.
     */
    double result = WaterNeedWit.calculateIrrigationWaterNeed(totalIrrigationWaterCurrentDate, recoveredClimateRecords, recoveredIrrigationRecords);

    System.out.println("* Valor esperado (nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateIrrigationWaterNeed (nec. agua riego");
    System.out.println("[mm/dia] de un cultivo en fecha actual): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testTwentySixCalculateIrrigationWaterNeed() {
    System.out.println("************************************** Prueba veintiseis del metodo calculateIrrigationWaterNeed ***************************************");
    printDescriptionMethodToTest();

    System.out.println("# Descripcion de la prueba unitaria");
    System.out.println("Para esta prueba se utilizan 6 registros climaticos y 6 registros de riego, todos ellos previos a la fecha actual " + UtilDate.formatDate(presumedCurrentDate) + ".");
    System.out.println("Suponemos que la fecha actual es " + UtilDate.formatDate(presumedCurrentDate) + ".");
    System.out.println();

    /*
     * Fechas a partir de las cuales se recuperaran los
     * registros climaticos y los registros de riego de
     * una parcela de prueba de la base de datos subyacente
     */
    Calendar dateFrom = Calendar.getInstance();
    dateFrom.set(Calendar.YEAR, 2023);
    dateFrom.set(Calendar.MONTH, JANUARY);
    dateFrom.set(Calendar.DAY_OF_MONTH, 1);

    Calendar dateUntil = Calendar.getInstance();
    dateUntil.set(Calendar.YEAR, 2023);
    dateUntil.set(Calendar.MONTH, JANUARY);
    dateUntil.set(Calendar.DAY_OF_MONTH, 6);

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
    givenUser.setUsername("camila");
    givenUser.setName("Camila");
    givenUser.setLastName("bolt");
    givenUser.setEmail("camila@eservice.com");
    givenUser.setPassword("Camila");

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
     * Persistencia de registros climaticos de prueba
     */
    ClimateRecord climateRecordOne = new ClimateRecord();
    climateRecordOne.setEtc(15);
    climateRecordOne.setDate(dayOne);
    climateRecordOne.setParcel(givenParcel);

    ClimateRecord climateRecordTwo = new ClimateRecord();
    climateRecordTwo.setEtc(12);
    climateRecordTwo.setDate(dayTwo);
    climateRecordTwo.setParcel(givenParcel);

    ClimateRecord climateRecordThree = new ClimateRecord();
    climateRecordThree.setEtc(6);
    climateRecordThree.setDate(dayThree);
    climateRecordThree.setParcel(givenParcel);

    ClimateRecord climateRecordFour = new ClimateRecord();
    climateRecordFour.setEtc(2);
    climateRecordFour.setDate(dayFour);
    climateRecordFour.setParcel(givenParcel);

    ClimateRecord climateRecordFive = new ClimateRecord();
    climateRecordFive.setEtc(4);
    climateRecordFive.setDate(dayFive);
    climateRecordFive.setParcel(givenParcel);

    ClimateRecord climateRecordSix = new ClimateRecord();
    climateRecordSix.setEtc(5);
    climateRecordSix.setDate(daySix);
    climateRecordSix.setParcel(givenParcel);

    entityManager.getTransaction().begin();
    climateRecordOne = climateRecordService.create(climateRecordOne);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordTwo = climateRecordService.create(climateRecordTwo);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordThree = climateRecordService.create(climateRecordThree);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordFour = climateRecordService.create(climateRecordFour);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordFive = climateRecordService.create(climateRecordFive);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordSix = climateRecordService.create(climateRecordSix);
    entityManager.getTransaction().commit();

    climateRecordsToBeDeleted.add(climateRecordOne);
    climateRecordsToBeDeleted.add(climateRecordTwo);
    climateRecordsToBeDeleted.add(climateRecordThree);
    climateRecordsToBeDeleted.add(climateRecordFour);
    climateRecordsToBeDeleted.add(climateRecordFive);
    climateRecordsToBeDeleted.add(climateRecordSix);

    /*
     * Persistencia de registros de riego de prueba
     */
    IrrigationRecord irrigationRecordOne = new IrrigationRecord();
    irrigationRecordOne.setDate(dayOne);
    irrigationRecordOne.setParcel(givenParcel);
    irrigationRecordOne.setIrrigationDone(15);

    IrrigationRecord irrigationRecordTwo = new IrrigationRecord();
    irrigationRecordTwo.setDate(dayTwo);
    irrigationRecordTwo.setParcel(givenParcel);
    irrigationRecordTwo.setIrrigationDone(12);

    IrrigationRecord irrigationRecordThree = new IrrigationRecord();
    irrigationRecordThree.setDate(dayThree);
    irrigationRecordThree.setParcel(givenParcel);
    irrigationRecordThree.setIrrigationDone(2);

    IrrigationRecord irrigationRecordFour = new IrrigationRecord();
    irrigationRecordFour.setDate(dayFour);
    irrigationRecordFour.setParcel(givenParcel);
    irrigationRecordFour.setIrrigationDone(0);

    IrrigationRecord irrigationRecordFive = new IrrigationRecord();
    irrigationRecordFive.setDate(dayFive);
    irrigationRecordFive.setParcel(givenParcel);
    irrigationRecordFive.setIrrigationDone(1);

    IrrigationRecord irrigationRecordSix = new IrrigationRecord();
    irrigationRecordSix.setDate(daySix);
    irrigationRecordSix.setParcel(givenParcel);
    irrigationRecordSix.setIrrigationDone(20);

    entityManager.getTransaction().begin();
    irrigationRecordOne = irrigationRecordService.create(irrigationRecordOne);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    irrigationRecordTwo = irrigationRecordService.create(irrigationRecordTwo);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    irrigationRecordThree = irrigationRecordService.create(irrigationRecordThree);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    irrigationRecordFour = irrigationRecordService.create(irrigationRecordFour);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    irrigationRecordFive = irrigationRecordService.create(irrigationRecordFive);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    irrigationRecordSix = irrigationRecordService.create(irrigationRecordSix);
    entityManager.getTransaction().commit();

    irrigationRecordsToBeDeleted.add(irrigationRecordOne);
    irrigationRecordsToBeDeleted.add(irrigationRecordTwo);
    irrigationRecordsToBeDeleted.add(irrigationRecordThree);
    irrigationRecordsToBeDeleted.add(irrigationRecordFour);
    irrigationRecordsToBeDeleted.add(irrigationRecordFive);
    irrigationRecordsToBeDeleted.add(irrigationRecordSix);

    /*
     * Recupera los registros climaticos recientemente
     * persistidos
     */
    Collection<ClimateRecord> recoveredClimateRecords = climateRecordService.findAllByParcelIdAndPeriod(givenUser.getId(), givenParcel.getId(), dateFrom, dateUntil);

    /*
     * Recupera los registros de riego recientemente
     * persistidos
     */
    Collection<IrrigationRecord> recoveredIrrigationRecords = irrigationRecordService.findAllByParcelIdAndPeriod(givenUser.getId(), givenParcel.getId(), dateFrom, dateUntil);

    System.out.println("Los datos con los que se calculara la necesidad de agua de riego de un cultivo en la fecha actual (" + UtilDate.formatDate(presumedCurrentDate) + ") son los siguientes:");
    System.out.println();

    printWaterBalanceTable(presumedCurrentDate, recoveredClimateRecords, recoveredIrrigationRecords);

    double totalIrrigationWaterCurrentDate = 0.0;

    printAccumulatedWaterDeficitPerDay(presumedCurrentDate, recoveredClimateRecords, recoveredIrrigationRecords);
    printTotalIrrigationWaterCurrentDate(presumedCurrentDate, totalIrrigationWaterCurrentDate);

    System.out.println();
    System.out.println("Al ser el resultado de la suma entre el acumulado del deficit de agua por dia de dias previos a la fecha actual y la cantidad");
    System.out.println("total de agua de riego de la fecha actual, mayor o igual a 0 (cero), el valor devuelto por el metodo calculateIrrigationWaterNeed");
    System.out.println("de la clase WaterNeedWit debe ser 0 [mm/dia]. Por lo tanto, la necesidad de agua de riego de un cultivo en la fecha actual es 0");
    System.out.println("[mm/dia].");
    System.out.println();

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");

    double expectedResult = 0.0;

    /*
     * El primer parametro de este metodo calculateIrrigationWaterNeed
     * es la cantidad total de agua de riego de una fecha dada. En este
     * caso, se le pasa el valor 0 como argumento porque suponemos que
     * la cantidad total de agua de riego de la supuesta fecha actual
     * es 0 para facilitar la tarea de probarlo.
     */
    double result = WaterNeedWit.calculateIrrigationWaterNeed(0, recoveredClimateRecords, recoveredIrrigationRecords);

    System.out.println("* Valor esperado (nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateIrrigationWaterNeed (nec. agua riego");
    System.out.println("[mm/dia] de un cultivo en fecha actual): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testTwentySevenCalculateIrrigationWaterNeed() {
    System.out.println("************************************** Prueba veintisiete del metodo calculateIrrigationWaterNeed ***************************************");
    printDescriptionMethodToTest();

    System.out.println("# Descripcion de la prueba unitaria");
    System.out.println("Para esta prueba se utilizan 6 registros climaticos y 6 registros de riego, todos ellos previos a la fecha actual " + UtilDate.formatDate(presumedCurrentDate) + ".");
    System.out.println("Suponemos que la fecha actual es " + UtilDate.formatDate(presumedCurrentDate) + ".");
    System.out.println();

    /*
     * Fechas a partir de las cuales se recuperaran los
     * registros climaticos y los registros de riego de
     * una parcela de prueba de la base de datos subyacente
     */
    Calendar dateFrom = Calendar.getInstance();
    dateFrom.set(Calendar.YEAR, 2023);
    dateFrom.set(Calendar.MONTH, JANUARY);
    dateFrom.set(Calendar.DAY_OF_MONTH, 1);

    Calendar dateUntil = Calendar.getInstance();
    dateUntil.set(Calendar.YEAR, 2023);
    dateUntil.set(Calendar.MONTH, JANUARY);
    dateUntil.set(Calendar.DAY_OF_MONTH, 6);

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
    givenUser.setUsername("tanenbaum");
    givenUser.setName("Andrew");
    givenUser.setLastName("tanenbaum");
    givenUser.setEmail("andrew@eservice.com");
    givenUser.setPassword("Andrew");

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
     * Persistencia de registros climaticos de prueba
     */
    ClimateRecord climateRecordOne = new ClimateRecord();
    climateRecordOne.setEtc(15);
    climateRecordOne.setDate(dayOne);
    climateRecordOne.setParcel(givenParcel);

    ClimateRecord climateRecordTwo = new ClimateRecord();
    climateRecordTwo.setEtc(5);
    climateRecordTwo.setDate(dayTwo);
    climateRecordTwo.setParcel(givenParcel);

    ClimateRecord climateRecordThree = new ClimateRecord();
    climateRecordThree.setEtc(8);
    climateRecordThree.setDate(dayThree);
    climateRecordThree.setParcel(givenParcel);

    ClimateRecord climateRecordFour = new ClimateRecord();
    climateRecordFour.setEtc(2);
    climateRecordFour.setDate(dayFour);
    climateRecordFour.setParcel(givenParcel);

    ClimateRecord climateRecordFive = new ClimateRecord();
    climateRecordFive.setEtc(8);
    climateRecordFive.setDate(dayFive);
    climateRecordFive.setParcel(givenParcel);

    ClimateRecord climateRecordSix = new ClimateRecord();
    climateRecordSix.setEtc(5);
    climateRecordSix.setDate(daySix);
    climateRecordSix.setParcel(givenParcel);

    entityManager.getTransaction().begin();
    climateRecordOne = climateRecordService.create(climateRecordOne);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordTwo = climateRecordService.create(climateRecordTwo);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordThree = climateRecordService.create(climateRecordThree);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordFour = climateRecordService.create(climateRecordFour);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordFive = climateRecordService.create(climateRecordFive);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordSix = climateRecordService.create(climateRecordSix);
    entityManager.getTransaction().commit();

    climateRecordsToBeDeleted.add(climateRecordOne);
    climateRecordsToBeDeleted.add(climateRecordTwo);
    climateRecordsToBeDeleted.add(climateRecordThree);
    climateRecordsToBeDeleted.add(climateRecordFour);
    climateRecordsToBeDeleted.add(climateRecordFive);
    climateRecordsToBeDeleted.add(climateRecordSix);

    /*
     * Persistencia de registros de riego de prueba
     */
    IrrigationRecord irrigationRecordOne = new IrrigationRecord();
    irrigationRecordOne.setDate(dayOne);
    irrigationRecordOne.setParcel(givenParcel);
    irrigationRecordOne.setIrrigationDone(10);

    IrrigationRecord irrigationRecordTwo = new IrrigationRecord();
    irrigationRecordTwo.setDate(dayTwo);
    irrigationRecordTwo.setParcel(givenParcel);
    irrigationRecordTwo.setIrrigationDone(10);

    IrrigationRecord irrigationRecordThree = new IrrigationRecord();
    irrigationRecordThree.setDate(dayThree);
    irrigationRecordThree.setParcel(givenParcel);
    irrigationRecordThree.setIrrigationDone(4);

    IrrigationRecord irrigationRecordFour = new IrrigationRecord();
    irrigationRecordFour.setDate(dayFour);
    irrigationRecordFour.setParcel(givenParcel);
    irrigationRecordFour.setIrrigationDone(6);

    IrrigationRecord irrigationRecordFive = new IrrigationRecord();
    irrigationRecordFive.setDate(dayFive);
    irrigationRecordFive.setParcel(givenParcel);
    irrigationRecordFive.setIrrigationDone(2);

    IrrigationRecord irrigationRecordSix = new IrrigationRecord();
    irrigationRecordSix.setDate(daySix);
    irrigationRecordSix.setParcel(givenParcel);
    irrigationRecordSix.setIrrigationDone(10);

    entityManager.getTransaction().begin();
    irrigationRecordOne = irrigationRecordService.create(irrigationRecordOne);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    irrigationRecordTwo = irrigationRecordService.create(irrigationRecordTwo);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    irrigationRecordThree = irrigationRecordService.create(irrigationRecordThree);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    irrigationRecordFour = irrigationRecordService.create(irrigationRecordFour);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    irrigationRecordFive = irrigationRecordService.create(irrigationRecordFive);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    irrigationRecordSix = irrigationRecordService.create(irrigationRecordSix);
    entityManager.getTransaction().commit();

    irrigationRecordsToBeDeleted.add(irrigationRecordOne);
    irrigationRecordsToBeDeleted.add(irrigationRecordTwo);
    irrigationRecordsToBeDeleted.add(irrigationRecordThree);
    irrigationRecordsToBeDeleted.add(irrigationRecordFour);
    irrigationRecordsToBeDeleted.add(irrigationRecordFive);
    irrigationRecordsToBeDeleted.add(irrigationRecordSix);

    /*
     * Recupera los registros climaticos recientemente
     * persistidos
     */
    Collection<ClimateRecord> recoveredClimateRecords = climateRecordService.findAllByParcelIdAndPeriod(givenUser.getId(), givenParcel.getId(), dateFrom, dateUntil);

    /*
     * Recupera los registros de riego recientemente
     * persistidos
     */
    Collection<IrrigationRecord> recoveredIrrigationRecords = irrigationRecordService.findAllByParcelIdAndPeriod(givenUser.getId(), givenParcel.getId(), dateFrom, dateUntil);

    System.out.println("Los datos con los que se calculara la necesidad de agua de riego de un cultivo en la fecha actual (" + UtilDate.formatDate(presumedCurrentDate) + ") son los siguientes:");
    System.out.println();

    printWaterBalanceTable(presumedCurrentDate, recoveredClimateRecords, recoveredIrrigationRecords);

    double totalIrrigationWaterCurrentDate = 0.0;

    printAccumulatedWaterDeficitPerDay(presumedCurrentDate, recoveredClimateRecords, recoveredIrrigationRecords);
    printTotalIrrigationWaterCurrentDate(presumedCurrentDate, totalIrrigationWaterCurrentDate);

    System.out.println();
    System.out.println("Al ser el resultado de la suma entre el acumulado del deficit de agua por dia de dias previos a la fecha actual y la cantidad");
    System.out.println("total de agua de riego de la fecha actual, estrictamente menor a 0 (cero), el valor devuelto por el metodo calculateIrrigationWaterNeed");
    System.out.println("de la clase WaterNeedWit debe ser el valor absoluto del resultado de dicha suma [mm/dia]. Por lo tanto, la necesidad de agua");
    System.out.println("de riego de un cultivo en la fecha actual es mayor o igual a 0 (cero) [mm/dia].");
    System.out.println();

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");

    double expectedResult = 1.0;

    /*
     * El primer parametro de este metodo calculateIrrigationWaterNeed
     * es la cantidad total de agua de riego de una fecha dada. En este
     * caso, se le pasa el valor 0 como argumento porque suponemos que
     * la cantidad total de agua de riego de la supuesta fecha actual
     * es 0 para facilitar la tarea de probarlo.
     */
    double result = WaterNeedWit.calculateIrrigationWaterNeed(totalIrrigationWaterCurrentDate, recoveredClimateRecords, recoveredIrrigationRecords);

    System.out.println("* Valor esperado (nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateIrrigationWaterNeed (nec. agua riego");
    System.out.println("[mm/dia] de un cultivo en fecha actual): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  /*
   * *********************************************************************
   * A partir de aca comienzan las dos pruebas en las que se demuestra
   * el correcto funcionamiento del metodo calculateIrrigationWaterNeed
   * de la clase WaterNeedWit con una coleccion de registros climaticos
   * que tienen una precipitacion mayor a cero y con una coleccion de
   * registros de riego que tienen un riego realizado mayor a cero.
   * 
   * El motivo de esto es que en ninguna de las pruebas unitarias anteriores
   * se probo el metodo calculateIrrigationWaterNeed de la clase WaterNeedWit
   * de esta manera. En algunas de las pruebas unitarias anteriores se
   * prueba dicho metodo con una coleccion de registros climaticos que
   * tienen una precipitacion mayor a cero y con una coleccion de registros
   * de riego que tienen un riego realizado mayor a cero. En otras se lo
   * prueba con una coleccion de registros climaticos que tienen una
   * precipitacion igual a cero y con una coleccion de registros de
   * riego que tienen un riego realizado mayor a cero.
   * *********************************************************************
   */

  @Test
  public void testTwentyEightCalculateIrrigationWaterNeed() {
    System.out.println("************************************** Prueba veintiocho del metodo calculateIrrigationWaterNeed ***************************************");
    printDescriptionMethodToTest();

    System.out.println("# Descripcion de la prueba unitaria");
    System.out.println("Para esta prueba se utilizan 6 registros climaticos y 6 registros de riego, todos ellos previos a la fecha actual " + UtilDate.formatDate(presumedCurrentDate) + ".");
    System.out.println("Suponemos que la fecha actual es " + UtilDate.formatDate(presumedCurrentDate) + ".");
    System.out.println();
    System.out.println("Los registros climaticos tienen una precipitacion mayor a cero y los registros de riego tienen un riego realizado mayor a cero. El");
    System.out.println("motivo de esto es que en ninguna de las pruebas unitarias anteriores se probo el metodo calculateIrrigationWaterNeed de la clase");
    System.out.println("WaterNeedWit de esta manera. En algunas de las pruebas unitarias anteriores se prueba dicho metodo con una coleccion de registros");
    System.out.println("climaticos que tienen una precipitacion mayor a cero y con una coleccion de registros de riego que tienen un riego realizado mayor");
    System.out.println("a cero. En otras se lo prueba con una coleccion de registros climaticos que tienen una precipitacion igual a cero y con una coleccion");
    System.out.println("de registros de riego que tienen un riego realizado mayor a cero.");
    System.out.println();

    /*
     * Fechas a partir de las cuales se recuperaran los
     * registros climaticos y los registros de riego de
     * una parcela de prueba de la base de datos subyacente
     */
    Calendar dateFrom = Calendar.getInstance();
    dateFrom.set(Calendar.YEAR, 2023);
    dateFrom.set(Calendar.MONTH, JANUARY);
    dateFrom.set(Calendar.DAY_OF_MONTH, 1);

    Calendar dateUntil = Calendar.getInstance();
    dateUntil.set(Calendar.YEAR, 2023);
    dateUntil.set(Calendar.MONTH, JANUARY);
    dateUntil.set(Calendar.DAY_OF_MONTH, 6);

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
    givenUser.setUsername("kurose");
    givenUser.setName("James");
    givenUser.setLastName("kurose");
    givenUser.setEmail("james@eservice.com");
    givenUser.setPassword("James");

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
     * Persistencia de registros climaticos de prueba
     */
    ClimateRecord climateRecordOne = new ClimateRecord();
    climateRecordOne.setEtc(15);
    climateRecordOne.setDate(dayOne);
    climateRecordOne.setParcel(givenParcel);
    climateRecordOne.setPrecip(2);

    ClimateRecord climateRecordTwo = new ClimateRecord();
    climateRecordTwo.setEtc(9);
    climateRecordTwo.setDate(dayTwo);
    climateRecordTwo.setParcel(givenParcel);
    climateRecordTwo.setPrecip(4);

    ClimateRecord climateRecordThree = new ClimateRecord();
    climateRecordThree.setEtc(8);
    climateRecordThree.setDate(dayThree);
    climateRecordThree.setParcel(givenParcel);
    climateRecordThree.setPrecip(1);

    ClimateRecord climateRecordFour = new ClimateRecord();
    climateRecordFour.setEtc(2);
    climateRecordFour.setDate(dayFour);
    climateRecordFour.setParcel(givenParcel);
    climateRecordFour.setPrecip(1);

    ClimateRecord climateRecordFive = new ClimateRecord();
    climateRecordFive.setEtc(8);
    climateRecordFive.setDate(dayFive);
    climateRecordFive.setParcel(givenParcel);
    climateRecordFive.setPrecip(5);

    ClimateRecord climateRecordSix = new ClimateRecord();
    climateRecordSix.setEtc(5);
    climateRecordSix.setDate(daySix);
    climateRecordSix.setParcel(givenParcel);
    climateRecordSix.setPrecip(7);

    entityManager.getTransaction().begin();
    climateRecordOne = climateRecordService.create(climateRecordOne);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordTwo = climateRecordService.create(climateRecordTwo);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordThree = climateRecordService.create(climateRecordThree);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordFour = climateRecordService.create(climateRecordFour);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordFive = climateRecordService.create(climateRecordFive);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordSix = climateRecordService.create(climateRecordSix);
    entityManager.getTransaction().commit();

    climateRecordsToBeDeleted.add(climateRecordOne);
    climateRecordsToBeDeleted.add(climateRecordTwo);
    climateRecordsToBeDeleted.add(climateRecordThree);
    climateRecordsToBeDeleted.add(climateRecordFour);
    climateRecordsToBeDeleted.add(climateRecordFive);
    climateRecordsToBeDeleted.add(climateRecordSix);

    /*
     * Persistencia de registros de riego de prueba
     */
    IrrigationRecord irrigationRecordOne = new IrrigationRecord();
    irrigationRecordOne.setDate(dayOne);
    irrigationRecordOne.setParcel(givenParcel);
    irrigationRecordOne.setIrrigationDone(1.5);

    IrrigationRecord irrigationRecordTwo = new IrrigationRecord();
    irrigationRecordTwo.setDate(dayTwo);
    irrigationRecordTwo.setParcel(givenParcel);
    irrigationRecordTwo.setIrrigationDone(2.5);

    IrrigationRecord irrigationRecordThree = new IrrigationRecord();
    irrigationRecordThree.setDate(dayThree);
    irrigationRecordThree.setParcel(givenParcel);
    irrigationRecordThree.setIrrigationDone(3);

    IrrigationRecord irrigationRecordFour = new IrrigationRecord();
    irrigationRecordFour.setDate(dayFour);
    irrigationRecordFour.setParcel(givenParcel);
    irrigationRecordFour.setIrrigationDone(6);

    IrrigationRecord irrigationRecordFive = new IrrigationRecord();
    irrigationRecordFive.setDate(dayFive);
    irrigationRecordFive.setParcel(givenParcel);
    irrigationRecordFive.setIrrigationDone(2);

    IrrigationRecord irrigationRecordSix = new IrrigationRecord();
    irrigationRecordSix.setDate(daySix);
    irrigationRecordSix.setParcel(givenParcel);
    irrigationRecordSix.setIrrigationDone(4);

    entityManager.getTransaction().begin();
    irrigationRecordOne = irrigationRecordService.create(irrigationRecordOne);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    irrigationRecordTwo = irrigationRecordService.create(irrigationRecordTwo);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    irrigationRecordThree = irrigationRecordService.create(irrigationRecordThree);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    irrigationRecordFour = irrigationRecordService.create(irrigationRecordFour);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    irrigationRecordFive = irrigationRecordService.create(irrigationRecordFive);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    irrigationRecordSix = irrigationRecordService.create(irrigationRecordSix);
    entityManager.getTransaction().commit();

    irrigationRecordsToBeDeleted.add(irrigationRecordOne);
    irrigationRecordsToBeDeleted.add(irrigationRecordTwo);
    irrigationRecordsToBeDeleted.add(irrigationRecordThree);
    irrigationRecordsToBeDeleted.add(irrigationRecordFour);
    irrigationRecordsToBeDeleted.add(irrigationRecordFive);
    irrigationRecordsToBeDeleted.add(irrigationRecordSix);

    /*
     * Recupera los registros climaticos recientemente
     * persistidos
     */
    Collection<ClimateRecord> recoveredClimateRecords = climateRecordService.findAllByParcelIdAndPeriod(givenUser.getId(), givenParcel.getId(), dateFrom, dateUntil);

    /*
     * Recupera los registros de riego recientemente
     * persistidos
     */
    Collection<IrrigationRecord> recoveredIrrigationRecords = irrigationRecordService.findAllByParcelIdAndPeriod(givenUser.getId(), givenParcel.getId(), dateFrom, dateUntil);

    System.out.println("Los datos con los que se calculara la necesidad de agua de riego de un cultivo en la fecha actual (" + UtilDate.formatDate(presumedCurrentDate) + ") son los siguientes:");
    System.out.println();

    printWaterBalanceTable(presumedCurrentDate, recoveredClimateRecords, recoveredIrrigationRecords);

    double totalIrrigationWaterCurrentDate = 0.0;

    printAccumulatedWaterDeficitPerDay(presumedCurrentDate, recoveredClimateRecords, recoveredIrrigationRecords);
    printTotalIrrigationWaterCurrentDate(presumedCurrentDate, totalIrrigationWaterCurrentDate);

    System.out.println();
    System.out.println("Al ser el resultado de la suma entre el acumulado del deficit de agua por dia de dias previos a la fecha actual y la cantidad");
    System.out.println("total de agua de riego de la fecha actual, estrictamente menor a 0 (cero), el valor devuelto por el metodo calculateIrrigationWaterNeed");
    System.out.println("de la clase WaterNeedWit debe ser el valor absoluto del resultado de dicha suma [mm/dia]. Por lo tanto, la necesidad de agua");
    System.out.println("de riego de un cultivo en la fecha actual es mayor o igual a 0 (cero) [mm/dia].");
    System.out.println();

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");

    double expectedResult = 8.0;

    /*
     * El primer parametro de este metodo calculateIrrigationWaterNeed
     * es la cantidad total de agua de riego de una fecha dada. En este
     * caso, se le pasa el valor 0 como argumento porque suponemos que
     * la cantidad total de agua de riego de la supuesta fecha actual
     * es 0 para facilitar la tarea de probarlo.
     */
    double result = WaterNeedWit.calculateIrrigationWaterNeed(totalIrrigationWaterCurrentDate, recoveredClimateRecords, recoveredIrrigationRecords);

    System.out.println("* Valor esperado (nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateIrrigationWaterNeed (nec. agua riego");
    System.out.println("[mm/dia] de un cultivo en fecha actual): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testTwentyNineCalculateIrrigationWaterNeed() {
    System.out.println("************************************** Prueba veintinueve del metodo calculateIrrigationWaterNeed ***************************************");
    printDescriptionMethodToTest();

    System.out.println("# Descripcion de la prueba unitaria");
    System.out.println("Para esta prueba se utilizan 6 registros climaticos y 6 registros de riego, todos ellos previos a la fecha actual " + UtilDate.formatDate(presumedCurrentDate) + ".");
    System.out.println("Suponemos que la fecha actual es " + UtilDate.formatDate(presumedCurrentDate) + ".");
    System.out.println();
    System.out.println("Los registros climaticos tienen una precipitacion mayor a cero y los registros de riego tienen un riego realizado mayor a cero. El");
    System.out.println("motivo de esto es que en ninguna de las pruebas unitarias anteriores se probo el metodo calculateIrrigationWaterNeed de la clase");
    System.out.println("WaterNeedWit de esta manera. En algunas de las pruebas unitarias anteriores se prueba dicho metodo con una coleccion de registros");
    System.out.println("climaticos que tienen una precipitacion mayor a cero y con una coleccion de registros de riego que tienen un riego realizado mayor");
    System.out.println("a cero. En otras se lo prueba con una coleccion de registros climaticos que tienen una precipitacion igual a cero y con una coleccion");
    System.out.println("de registros de riego que tienen un riego realizado mayor a cero.");
    System.out.println();

    /*
     * Fechas a partir de las cuales se recuperaran los
     * registros climaticos y los registros de riego de
     * una parcela de prueba de la base de datos subyacente
     */
    Calendar dateFrom = Calendar.getInstance();
    dateFrom.set(Calendar.YEAR, 2023);
    dateFrom.set(Calendar.MONTH, JANUARY);
    dateFrom.set(Calendar.DAY_OF_MONTH, 1);

    Calendar dateUntil = Calendar.getInstance();
    dateUntil.set(Calendar.YEAR, 2023);
    dateUntil.set(Calendar.MONTH, JANUARY);
    dateUntil.set(Calendar.DAY_OF_MONTH, 6);

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
    givenUser.setUsername("stark");
    givenUser.setName("Tony");
    givenUser.setLastName("stark");
    givenUser.setEmail("stark@eservice.com");
    givenUser.setPassword("Tony");

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
     * Persistencia de registros climaticos de prueba
     */
    ClimateRecord climateRecordOne = new ClimateRecord();
    climateRecordOne.setEtc(5);
    climateRecordOne.setDate(dayOne);
    climateRecordOne.setParcel(givenParcel);
    climateRecordOne.setPrecip(2);

    ClimateRecord climateRecordTwo = new ClimateRecord();
    climateRecordTwo.setEtc(7);
    climateRecordTwo.setDate(dayTwo);
    climateRecordTwo.setParcel(givenParcel);
    climateRecordTwo.setPrecip(3);

    ClimateRecord climateRecordThree = new ClimateRecord();
    climateRecordThree.setEtc(5);
    climateRecordThree.setDate(dayThree);
    climateRecordThree.setParcel(givenParcel);
    climateRecordThree.setPrecip(3);

    ClimateRecord climateRecordFour = new ClimateRecord();
    climateRecordFour.setEtc(1.2);
    climateRecordFour.setDate(dayFour);
    climateRecordFour.setParcel(givenParcel);
    climateRecordFour.setPrecip(1);

    ClimateRecord climateRecordFive = new ClimateRecord();
    climateRecordFive.setEtc(6.5);
    climateRecordFive.setDate(dayFive);
    climateRecordFive.setParcel(givenParcel);
    climateRecordFive.setPrecip(2.5);

    ClimateRecord climateRecordSix = new ClimateRecord();
    climateRecordSix.setEtc(4);
    climateRecordSix.setDate(daySix);
    climateRecordSix.setParcel(givenParcel);
    climateRecordSix.setPrecip(3);

    entityManager.getTransaction().begin();
    climateRecordOne = climateRecordService.create(climateRecordOne);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordTwo = climateRecordService.create(climateRecordTwo);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordThree = climateRecordService.create(climateRecordThree);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordFour = climateRecordService.create(climateRecordFour);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordFive = climateRecordService.create(climateRecordFive);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    climateRecordSix = climateRecordService.create(climateRecordSix);
    entityManager.getTransaction().commit();

    climateRecordsToBeDeleted.add(climateRecordOne);
    climateRecordsToBeDeleted.add(climateRecordTwo);
    climateRecordsToBeDeleted.add(climateRecordThree);
    climateRecordsToBeDeleted.add(climateRecordFour);
    climateRecordsToBeDeleted.add(climateRecordFive);
    climateRecordsToBeDeleted.add(climateRecordSix);

    /*
     * Persistencia de registros de riego de prueba
     */
    IrrigationRecord irrigationRecordOne = new IrrigationRecord();
    irrigationRecordOne.setDate(dayOne);
    irrigationRecordOne.setParcel(givenParcel);
    irrigationRecordOne.setIrrigationDone(1.5);

    IrrigationRecord irrigationRecordTwo = new IrrigationRecord();
    irrigationRecordTwo.setDate(dayTwo);
    irrigationRecordTwo.setParcel(givenParcel);
    irrigationRecordTwo.setIrrigationDone(2.5);

    IrrigationRecord irrigationRecordThree = new IrrigationRecord();
    irrigationRecordThree.setDate(dayThree);
    irrigationRecordThree.setParcel(givenParcel);
    irrigationRecordThree.setIrrigationDone(3);

    IrrigationRecord irrigationRecordFour = new IrrigationRecord();
    irrigationRecordFour.setDate(dayFour);
    irrigationRecordFour.setParcel(givenParcel);
    irrigationRecordFour.setIrrigationDone(6);

    IrrigationRecord irrigationRecordFive = new IrrigationRecord();
    irrigationRecordFive.setDate(dayFive);
    irrigationRecordFive.setParcel(givenParcel);
    irrigationRecordFive.setIrrigationDone(2);

    IrrigationRecord irrigationRecordSix = new IrrigationRecord();
    irrigationRecordSix.setDate(daySix);
    irrigationRecordSix.setParcel(givenParcel);
    irrigationRecordSix.setIrrigationDone(4);

    entityManager.getTransaction().begin();
    irrigationRecordOne = irrigationRecordService.create(irrigationRecordOne);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    irrigationRecordTwo = irrigationRecordService.create(irrigationRecordTwo);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    irrigationRecordThree = irrigationRecordService.create(irrigationRecordThree);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    irrigationRecordFour = irrigationRecordService.create(irrigationRecordFour);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    irrigationRecordFive = irrigationRecordService.create(irrigationRecordFive);
    entityManager.getTransaction().commit();

    entityManager.getTransaction().begin();
    irrigationRecordSix = irrigationRecordService.create(irrigationRecordSix);
    entityManager.getTransaction().commit();

    irrigationRecordsToBeDeleted.add(irrigationRecordOne);
    irrigationRecordsToBeDeleted.add(irrigationRecordTwo);
    irrigationRecordsToBeDeleted.add(irrigationRecordThree);
    irrigationRecordsToBeDeleted.add(irrigationRecordFour);
    irrigationRecordsToBeDeleted.add(irrigationRecordFive);
    irrigationRecordsToBeDeleted.add(irrigationRecordSix);

    /*
     * Recupera los registros climaticos recientemente
     * persistidos
     */
    Collection<ClimateRecord> recoveredClimateRecords = climateRecordService.findAllByParcelIdAndPeriod(givenUser.getId(), givenParcel.getId(), dateFrom, dateUntil);

    /*
     * Recupera los registros de riego recientemente
     * persistidos
     */
    Collection<IrrigationRecord> recoveredIrrigationRecords = irrigationRecordService.findAllByParcelIdAndPeriod(givenUser.getId(), givenParcel.getId(), dateFrom, dateUntil);

    System.out.println("Los datos con los que se calculara la necesidad de agua de riego de un cultivo en la fecha actual (" + UtilDate.formatDate(presumedCurrentDate) + ") son los siguientes:");
    System.out.println();

    printWaterBalanceTable(presumedCurrentDate, recoveredClimateRecords, recoveredIrrigationRecords);

    double totalIrrigationWaterCurrentDate = 0.0;

    printAccumulatedWaterDeficitPerDay(presumedCurrentDate, recoveredClimateRecords, recoveredIrrigationRecords);
    printTotalIrrigationWaterCurrentDate(presumedCurrentDate, totalIrrigationWaterCurrentDate);

    System.out.println();
    System.out.println("Al ser el resultado de la suma entre el acumulado del deficit de agua por dia de dias previos a la fecha actual y la cantidad");
    System.out.println("total de agua de riego de la fecha actual, mayor o igual a 0 (cero), el valor devuelto por el metodo calculateIrrigationWaterNeed");
    System.out.println("de la clase WaterNeedWit debe ser 0 [mm/dia]. Por lo tanto, la necesidad de agua de riego de un cultivo en la fecha actual es 0");
    System.out.println("[mm/dia].");
    System.out.println();

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");

    double expectedResult = 0.0;

    /*
     * El primer parametro de este metodo calculateIrrigationWaterNeed
     * es la cantidad total de agua de riego de una fecha dada. En este
     * caso, se le pasa el valor 0 como argumento porque suponemos que
     * la cantidad total de agua de riego de la supuesta fecha actual
     * es 0 para facilitar la tarea de probarlo.
     */
    double result = WaterNeedWit.calculateIrrigationWaterNeed(totalIrrigationWaterCurrentDate, recoveredClimateRecords, recoveredIrrigationRecords);

    System.out.println("* Valor esperado (nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateIrrigationWaterNeed (nec. agua riego");
    System.out.println("[mm/dia] de un cultivo en fecha actual): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

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
    for (ClimateRecord currenClimateRecord : climateRecordsToBeDeleted) {
      climateRecordService.remove(currenClimateRecord.getId());
    }

    for (IrrigationRecord currentIrrigationRecord : irrigationRecordsToBeDeleted) {
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

  /**
   * Imprime una tabla que contiene el dia, la ETc por dia, el
   * agua (lluvia o riego, o lluvia mas riego y viceversa) por
   * dia, el deficit de agua por dia y el acumulado del deficit
   * de agua por dia de dias previos a una fecha
   * 
   * @param presumedCurrentDate
   * @param givenClimateRecords
   * @param givenIrrigationRecords
   */
  private void printWaterBalanceTable(Calendar presumedCurrentDate, Collection<ClimateRecord> givenClimateRecords,
      Collection<IrrigationRecord> givenIrrigationRecords) {
    int day = 1;
    double differencePerDay = 0.0;
    double accumulatedWaterDeficitPerDay = 0.0;
    double waterProvidedGivenDate = 0.0;

    System.out.println("     Dia       " + "|" + "    ETc   " + "|" + "   H2O   " + "|" + "  Dif.  " + "|" + "  Acum. Def.  ");
    System.out.println("-----------------------------------------------------------");

    for (ClimateRecord currentClimateRecord : givenClimateRecords) {
      waterProvidedGivenDate = currentClimateRecord.getPrecip() + WaterMath.sumTotalAmountIrrigationWaterGivenDate(currentClimateRecord.getDate(), givenIrrigationRecords);
      differencePerDay = waterProvidedGivenDate - currentClimateRecord.getEtc();
      accumulatedWaterDeficitPerDay = WaterMath.accumulateWaterDeficitPerDay(differencePerDay, accumulatedWaterDeficitPerDay);

      System.out.print(" " + day + " (" + UtilDate.formatDate(currentClimateRecord.getDate()) + ")");
      System.out.print("  |");
      System.out.print("   " + currentClimateRecord.getEtc());
      System.out.print("   |");
      System.out.print("   " + waterProvidedGivenDate);
      System.out.print("    |");
      System.out.print("  " + differencePerDay);
      System.out.print("  |");
      System.out.print("   " + accumulatedWaterDeficitPerDay);
      System.out.println();

      day++;
    }

    System.out.print(" " + day + " (" + UtilDate.formatDate(presumedCurrentDate) + ")");
    System.out.print("  |");
    System.out.print("    -    |");
    System.out.print("     -    |");
    System.out.print("    -    |");
    System.out.print("    -  ");
    System.out.println();
    System.out.println();

    System.out.println("ETc [mm/dia]");
    System.out.println("H2O (lluvia o riego, o lluvia mas riego y viceversa) [mm/dia]");
    System.out.println("Diferencia (Lluvia - ETc) [mm/dia]");
    System.out.println("Acumulado del deficit de agua por dia de dias previos a una fecha (dia) [mm/dia]");
    System.out.println();
  }

  /**
   * @param presumedCurrentDate
   * @param totalIrrigationWaterCurrentDate
   */
  private void printTotalIrrigationWaterCurrentDate(Calendar presumedCurrentDate, double totalIrrigationWaterCurrentDate) {
    System.out.println("- La cantidad total de agua de riego en la fecha actual (hoy) "
        + UtilDate.formatDate(presumedCurrentDate) + " es: " + totalIrrigationWaterCurrentDate + " [mm/dia].");
  }

  /**
   * @param presumedCurrentDate
   * @param givenClimateRecords
   * @param givenIrrigationRecords
   */
  private void printAccumulatedWaterDeficitPerDay(Calendar presumedCurrentDate, Collection<ClimateRecord> givenClimateRecords,
      Collection<IrrigationRecord> givenIrrigationRecords) {
    System.out.println("- Acumulado del deficit de agua por dia de dias previos a la fecha actual (hoy) " + UtilDate.formatDate(presumedCurrentDate)
        + ": " + WaterMath.calculateAccumulatedWaterDeficitPerDay(givenClimateRecords, givenIrrigationRecords) + " [mm/dia].");
  }

  /**
   * Imprime la descripcion del metodo a probar, el cual en este
   * caso es el metodo calculateIrrigationWaterNeed de la clase
   * WaterNeedWit
   */
  private void printDescriptionMethodToTest() {
    System.out.println("El metodo calculateIrrigationWaterNeed de la clase calculateIrrigationWaterNeed calcula la necesidad de agua de riego [mm/dia] de un");
    System.out.println("cultivo en una fecha, si se lo invoca con una coleccción de registros climaticos y una coleccion de registros de riego previos a una");
    System.out.println("fecha pertenecientes a una misma parcela que tiene un cultivo sembrado en una fecha. La fecha para la que se calcula la necesidad de");
    System.out.println("agua de riego de un cultivo puede ser la fecha actual (es decir, hoy) o una fecha posterior a la fecha actual. Pero tambien puede ser");
    System.out.println("una fecha del pasado (es decir, anterior a la fecha actual), pero esto no tiene sentido si lo que se busca es determinar la necesidad");
    System.out.println("de agua de riego de un cultivo en la fecha actual o en una fecha posterior a la fecha actual.");
    System.out.println();
    System.out.println("Si este metodo es invocado con una coleccion de registros climaticos y una coleccion de registros de riego previos a una fecha pertenecientes");
    System.out.println("a una parcela que NO tiene un cultivo sembrado en una fecha, el valor devuelto por el mismo representa el acumulado del deficit de agua");
    System.out.println("por dia de dias previos a una fecha de una parcela en una fecha [mm/dia].");
    System.out.println();
    System.out.println("El metodo calculateIrrigationWaterNeed utiliza la cantidad total de agua de riego de una fecha y el acumulado del deficit de agua por");
    System.out.println("dia de dias previos a una fecha para calcular la necesidad de agua de riego de un cultivo en una fecha. El acumulado del deficit de");
    System.out.println("agua por dia de dias previos a una fecha representa la cantidad acumulada de agua evaporada en dias previos a una fecha que NO fue");
    System.out.println("cubierta (satisfecha) y que se debe reponer mediante el riego en una fecha, y se calcula a partir de una coleccion de registros");
    System.out.println("climaticos y una coleccion de registros de riego, debiendo ser todos ellos previos a una fecha y pertenecientes a una misma parcela.");
    System.out.println();
    System.out.println("- Si el resultado de la suma entre el acumulado del deficit de agua por dia [mm/dia] de dias previos a una fecha y la cantidad total");
    System.out.println("de agua de riego de una fecha [mm/dia] es mayor o igual a 0 (cero), el metodo calculateIrrigationWaterNeed retorna 0. Por lo tanto,");
    System.out.println("la necesidad de agua de riego de un cultivo en una fecha es 0 [mm/dia]. Esto se debe a que un resultado mayor o igual a cero de la");
    System.out.println("suma entre el acumulado del deficit de agua por dia de dias previos a una fecha y la cantidad total de agua de riego de una fecha,");
    System.out.println("indica que NO hay una cantidad de agua evaporada de dias previos a una fecha que cubrir (satisfacer) mediante el riego en una fecha.");
    System.out.println();
    System.out.println("- Si el resultado de la suma entre el acumulado del deficit de agua por dia [mm/dia] de dias previos a una fecha y la cantidad total");
    System.out.println("de agua de riego de una fecha [mm/dia] es estrictamente menor a 0 (cero), el metodo calculateIrrigationWaterNeed retorna el valor");
    System.out.println("absoluto del resultado de dicha suma. Por lo tanto, la necesidad de agua de riego de un cultivo en una fecha [mm/dia] es estrictamente");
    System.out.println("mayor a cero [mm/dia]. Esto se debe a que un resultado negativo de la suma entre el acumulado del deficit de agua por dia de dias");
    System.out.println("previos a una fecha y la cantidad total de agua de riego de una fecha, indica que hay una cantidad de agua evaporada de dias previos");
    System.out.println("a una fecha que cubrir (satisfacer) mediante el riego en una fecha.");
    System.out.println();
    System.out.println("La fecha para la que se calcula la necesidad de agua de riego de un cultivo esta determinada por los registros climaticos y los");
    System.out.println("registros de riego que se seleccionan como previos a una fecha, siendo todos ellos pertenecientes a una misma parcela. Por ejemplo,");
    System.out.println("si se seleccionan los registros climaticos y los registros de riego de una parcela previos a la fecha actual (es decir, hoy), la");
    System.out.println("necesidad de agua de riego de un cultivo calculada con estos registros corresponde a la fecha actual. En cambio, si se seleccionan");
    System.out.println("los registros climaticos y los registros de riego de una parcela previos a la fecha actual + X dias, donde X > 0, la necesidad de");
    System.out.println("agua de riego de un cultivo calculada con estos registros corresponde a la fecha actual + X dias.");
    System.out.println();
  }

  /**
   * Establece la ETc, la lluvia y la fecha de 6 registros
   * climaticos para la pruebta unitaria uno
   */
  private void setClimateRecordsValuesOne() {
    climateRecordOne.setEtc(15);
    climateRecordOne.setPrecip(15);
    climateRecordOne.setDate(dayOne);

    climateRecordTwo.setEtc(12);
    climateRecordTwo.setPrecip(12);
    climateRecordTwo.setDate(dayTwo);

    climateRecordThree.setEtc(6);
    climateRecordThree.setPrecip(2);
    climateRecordThree.setDate(dayThree);

    climateRecordFour.setEtc(2);
    climateRecordFour.setPrecip(0);
    climateRecordFour.setDate(dayFour);

    climateRecordFive.setEtc(4);
    climateRecordFive.setPrecip(1);
    climateRecordFive.setDate(dayFive);

    climateRecordSix.setEtc(5);
    climateRecordSix.setPrecip(20);
    climateRecordSix.setDate(daySix);
  }

  /**
   * Establece la ETc, la lluvia y la fecha de 6 registros
   * climaticos para la pruebta unitaria dos
   */
  private void setClimateRecordsValuesTwo() {
    climateRecordOne.setEtc(15);
    climateRecordOne.setPrecip(15);
    climateRecordOne.setDate(dayOne);

    climateRecordTwo.setEtc(12);
    climateRecordTwo.setPrecip(12);
    climateRecordTwo.setDate(dayTwo);

    climateRecordThree.setEtc(6);
    climateRecordThree.setPrecip(2);
    climateRecordThree.setDate(dayThree);

    climateRecordFour.setEtc(2);
    climateRecordFour.setPrecip(0);
    climateRecordFour.setDate(dayFour);

    climateRecordFive.setEtc(4);
    climateRecordFive.setPrecip(1);
    climateRecordFive.setDate(dayFive);

    climateRecordSix.setEtc(5);
    climateRecordSix.setPrecip(20);
    climateRecordSix.setDate(daySix);
  }

  /**
   * Establece la ETc, la lluvia y la fecha de 6 registros
   * climaticos para la pruebta unitaria tres
   */
  private void setClimateRecordsValuesThree() {
    climateRecordOne.setEtc(10);
    climateRecordOne.setPrecip(5);
    climateRecordOne.setDate(dayOne);

    climateRecordTwo.setEtc(5);
    climateRecordTwo.setPrecip(2);
    climateRecordTwo.setDate(dayTwo);

    climateRecordThree.setEtc(2);
    climateRecordThree.setPrecip(1);
    climateRecordThree.setDate(dayThree);

    climateRecordFour.setEtc(3);
    climateRecordFour.setPrecip(1);
    climateRecordFour.setDate(dayFour);

    climateRecordFive.setEtc(1);
    climateRecordFive.setPrecip(0);
    climateRecordFive.setDate(dayFive);

    climateRecordSix.setEtc(4);
    climateRecordSix.setPrecip(2);
    climateRecordSix.setDate(daySix);
  }

  /**
   * Establece la ETc, la lluvia y la fecha de 6 registros
   * climaticos para la pruebta unitaria cuatro
   */
  private void setClimateRecordsValuesFour() {
    climateRecordOne.setEtc(10);
    climateRecordOne.setPrecip(5);
    climateRecordOne.setDate(dayOne);

    climateRecordTwo.setEtc(5);
    climateRecordTwo.setPrecip(2);
    climateRecordTwo.setDate(dayTwo);

    climateRecordThree.setEtc(2);
    climateRecordThree.setPrecip(1);
    climateRecordThree.setDate(dayThree);

    climateRecordFour.setEtc(3);
    climateRecordFour.setPrecip(1);
    climateRecordFour.setDate(dayFour);

    climateRecordFive.setEtc(1);
    climateRecordFive.setPrecip(0);
    climateRecordFive.setDate(dayFive);

    climateRecordSix.setEtc(4);
    climateRecordSix.setPrecip(2);
    climateRecordSix.setDate(daySix);
  }

  /**
   * Establece la ETc, la lluvia y la fecha de 6 registros
   * climaticos para la pruebta unitaria cinco
   */
  private void setClimateRecordsValuesFive() {
    climateRecordOne.setEtc(10);
    climateRecordOne.setPrecip(5);
    climateRecordOne.setDate(dayOne);

    climateRecordTwo.setEtc(5);
    climateRecordTwo.setPrecip(2);
    climateRecordTwo.setDate(dayTwo);

    climateRecordThree.setEtc(2);
    climateRecordThree.setPrecip(1);
    climateRecordThree.setDate(dayThree);

    climateRecordFour.setEtc(3);
    climateRecordFour.setPrecip(1);
    climateRecordFour.setDate(dayFour);

    climateRecordFive.setEtc(1);
    climateRecordFive.setPrecip(0);
    climateRecordFive.setDate(dayFive);

    climateRecordSix.setEtc(4);
    climateRecordSix.setPrecip(2);
    climateRecordSix.setDate(daySix);
  }

}