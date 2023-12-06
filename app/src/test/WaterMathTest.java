import static org.junit.Assert.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.Calendar;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.lang.Math;
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
import util.UtilDate;
import stateless.UserServiceBean;
import stateless.OptionServiceBean;
import stateless.ParcelServiceBean;
import stateless.ClimateRecordServiceBean;
import stateless.IrrigationRecordServiceBean;

public class WaterMathTest {

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

    /*
     * Esta coleccion es unicamente para aquellas pruebas unitarias
     * del metodo calculateIrrigationWaterNeed de la clase WaterMath
     * en las que se busca demostrar el correcto funcionamiento del
     * mismo sin hacer uso del agua de riego
     */
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
  public void testOneCalculateIrrigationWater() {
    System.out.println("************************************** Prueba uno del metodo calculateIrrigationWater ***************************************");
    printDescriptionMethodToTest();

    System.out.println("# Descripcion de la prueba unitaria");
    System.out.println("Para esta prueba se utilizan 6 registros climaticos previos a la fecha actual " + UtilDate.formatDate(presumedCurrentDate) + ".");
    System.out.println("Suponemos que la fecha actual es " + UtilDate.formatDate(presumedCurrentDate) + ".");
    System.out.println();

    /*
     * Establece la ETc, la lluvia y la fecha de
     * 6 registros climaticos para esta prueba
     * unitaria
     */
    setClimateRecordsValuesOne();

    System.out.println("Los datos con los que se calculara la necesidad de agua de riego de un cultivo en la fecha actual (" + UtilDate.formatDate(presumedCurrentDate) + ") son los siguientes:");
    System.out.println();
    printClimateRecords(presumedCurrentDate, climateRecords);

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");

    double expectedResult = 14.0;
    double result = WaterMath.calculateIrrigationWaterNeed(climateRecords, zeroIrrigationRecords);

    System.out.println("* Valor esperado (nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateIrrigationWaterNeed");
    System.out.println("(nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testTwoCalculateIrrigationWater() {
    System.out.println("************************************** Prueba dos del metodo calculateIrrigationWater ***************************************");
    printDescriptionMethodToTest();

    System.out.println("# Descripcion de la prueba unitaria");
    System.out.println("Para esta prueba se utilizan 6 registros climaticos previos a la fecha actual " + UtilDate.formatDate(presumedCurrentDate) + ".");
    System.out.println("Suponemos que la fecha actual es " + UtilDate.formatDate(presumedCurrentDate) + ".");
    System.out.println();

    /*
     * Establece la ETc, la lluvia y la fecha de
     * 6 registros climaticos para esta prueba
     * unitaria
     */
    setClimateRecordsValuesTwo();

    System.out.println("Los datos con los que se calculara la necesidad de agua de riego de un cultivo en la fecha actual (" + UtilDate.formatDate(presumedCurrentDate) + ") son los siguientes:");
    System.out.println();
    printClimateRecords(presumedCurrentDate, climateRecords);

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");

    double expectedResult = 9.0;
    double result = WaterMath.calculateIrrigationWaterNeed(climateRecords, zeroIrrigationRecords);

    System.out.println("* Valor esperado (nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateIrrigationWaterNeed");
    System.out.println("(nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testThreeCalculateIrrigationWater() {
    System.out.println("************************************** Prueba tres del metodo calculateIrrigationWater ***************************************");
    printDescriptionMethodToTest();

    System.out.println("# Descripcion de la prueba unitaria");
    System.out.println("Para esta prueba se utilizan 6 registros climaticos previos a la fecha actual " + UtilDate.formatDate(presumedCurrentDate) + ".");
    System.out.println("Suponemos que la fecha actual es " + UtilDate.formatDate(presumedCurrentDate) + ".");
    System.out.println();

    /*
     * Establece la ETc, la lluvia y la fecha de
     * 6 registros climaticos para esta prueba
     * unitaria
     */
    setClimateRecordsValuesThree();

    System.out.println("Los datos con los que se calculara la necesidad de agua de riego de un cultivo en la fecha actual (" + UtilDate.formatDate(presumedCurrentDate) + ") son los siguientes:");
    System.out.println();
    printClimateRecords(presumedCurrentDate, climateRecords);

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");

    double expectedResult = 5.0;
    double result = WaterMath.calculateIrrigationWaterNeed(climateRecords, zeroIrrigationRecords);

    System.out.println("* Valor esperado (nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateIrrigationWaterNeed");
    System.out.println("(nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testFourCalculateIrrigationWater() {
    System.out.println("************************************** Prueba cuatro del metodo calculateIrrigationWater ***************************************");
    printDescriptionMethodToTest();

    System.out.println("# Descripcion de la prueba unitaria");
    System.out.println("Para esta prueba se utilizan 6 registros climaticos previos a la fecha actual " + UtilDate.formatDate(presumedCurrentDate) + ".");
    System.out.println("Suponemos que la fecha actual es " + UtilDate.formatDate(presumedCurrentDate) + ".");
    System.out.println();

    /*
     * Establece la ETc, la lluvia y la fecha de
     * 6 registros climaticos para esta prueba
     * unitaria
     */
    setClimateRecordsValuesFour();

    System.out.println("Los datos con los que se calculara la necesidad de agua de riego de un cultivo en la fecha actual (" + UtilDate.formatDate(presumedCurrentDate) + ") son los siguientes:");
    System.out.println();
    printClimateRecords(presumedCurrentDate, climateRecords);

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");

    double expectedResult = 0.0;
    double result = WaterMath.calculateIrrigationWaterNeed(climateRecords, zeroIrrigationRecords);

    System.out.println("* Valor esperado (nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateIrrigationWaterNeed");
    System.out.println("(nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testFiveCalculateIrrigationWater() {
    System.out.println("************************************** Prueba cinco del metodo calculateIrrigationWater ***************************************");
    printDescriptionMethodToTest();

    System.out.println("# Descripcion de la prueba unitaria");
    System.out.println("Para esta prueba se utilizan 6 registros climaticos previos a la fecha actual " + UtilDate.formatDate(presumedCurrentDate) + ".");
    System.out.println("Suponemos que la fecha actual es " + UtilDate.formatDate(presumedCurrentDate) + ".");
    System.out.println();

    /*
     * Establece la ETc, la lluvia y la fecha de
     * 6 registros climaticos para esta prueba
     * unitaria
     */
    setClimateRecordsValuesFive();

    System.out.println("Los datos con los que se calculara la necesidad de agua de riego de un cultivo en la fecha actual (" + UtilDate.formatDate(presumedCurrentDate) + ") son los siguientes:");
    System.out.println();
    printClimateRecords(presumedCurrentDate, climateRecords);

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");

    double expectedResult = 0.0;
    double result = WaterMath.calculateIrrigationWaterNeed(climateRecords, zeroIrrigationRecords);

    System.out.println("* Valor esperado (nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateIrrigationWaterNeed");
    System.out.println("(nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testSixCalculateIrrigationWater() {
    System.out.println("************************************** Prueba seis del metodo calculateIrrigationWater ***************************************");
    printDescriptionMethodToTest();

    System.out.println("# Descripcion de la prueba unitaria");
    System.out.println("Para esta prueba se utilizan 6 registros climaticos previos a la fecha actual " + UtilDate.formatDate(presumedCurrentDate) + ".");
    System.out.println("Suponemos que la fecha actual es " + UtilDate.formatDate(presumedCurrentDate) + ".");
    System.out.println();

    /*
     * Establece la ETc, la lluvia y la fecha de
     * 6 registros climaticos para esta prueba
     * unitaria
     */
    setClimateRecordsValuesSix();

    System.out.println("Los datos con los que se calculara la necesidad de agua de riego de un cultivo en la fecha actual (" + UtilDate.formatDate(presumedCurrentDate) + ") son los siguientes:");
    System.out.println();
    printClimateRecords(presumedCurrentDate, climateRecords);

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");

    double expectedResult = 4.0;
    double result = WaterMath.calculateIrrigationWaterNeed(climateRecords, zeroIrrigationRecords);

    System.out.println("* Valor esperado (nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateIrrigationWaterNeed");
    System.out.println("(nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testSevenCalculateIrrigationWater() {
    System.out.println("************************************** Prueba siete del metodo calculateIrrigationWater ***************************************");
    printDescriptionMethodToTest();

    System.out.println("# Descripcion de la prueba unitaria");
    System.out.println("Para esta prueba se utilizan 6 registros climaticos previos a la fecha actual " + UtilDate.formatDate(presumedCurrentDate) + ".");
    System.out.println("Suponemos que la fecha actual es " + UtilDate.formatDate(presumedCurrentDate) + ".");
    System.out.println();

    /*
     * Establece la ETc, la lluvia y la fecha de
     * 6 registros climaticos para esta prueba
     * unitaria
     */
    setClimateRecordsValuesSeven();

    System.out.println("Los datos con los que se calculara la necesidad de agua de riego de un cultivo en la fecha actual (" + UtilDate.formatDate(presumedCurrentDate) + ") son los siguientes:");
    System.out.println();
    printClimateRecords(presumedCurrentDate, climateRecords);

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");

    double expectedResult = 7.0;
    double result = WaterMath.calculateIrrigationWaterNeed(climateRecords, zeroIrrigationRecords);

    System.out.println("* Valor esperado (nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateIrrigationWaterNeed");
    System.out.println("(nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testEightCalculateIrrigationWater() {
    System.out.println("************************************** Prueba ocho del metodo calculateIrrigationWater ***************************************");
    printDescriptionMethodToTest();

    System.out.println("# Descripcion de la prueba unitaria");
    System.out.println("Para esta prueba se utilizan 6 registros climaticos previos a la fecha actual " + UtilDate.formatDate(presumedCurrentDate) + ".");
    System.out.println("Suponemos que la fecha actual es " + UtilDate.formatDate(presumedCurrentDate) + ".");
    System.out.println();

    /*
     * Establece la ETc, la lluvia y la fecha de
     * 6 registros climaticos para esta prueba
     * unitaria
     */
    setClimateRecordsValuesEight();

    System.out.println("Los datos con los que se calculara la necesidad de agua de riego de un cultivo en la fecha actual (" + UtilDate.formatDate(presumedCurrentDate) + ") son los siguientes:");
    System.out.println();
    printClimateRecords(presumedCurrentDate, climateRecords);

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");

    double expectedResult = 4.0;
    double result = WaterMath.calculateIrrigationWaterNeed(climateRecords, zeroIrrigationRecords);

    System.out.println("* Valor esperado (nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateIrrigationWaterNeed");
    System.out.println("(nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testNineCalculateIrrigationWater() {
    System.out.println("************************************** Prueba nueve del metodo calculateIrrigationWater ***************************************");
    printDescriptionMethodToTest();

    System.out.println("# Descripcion de la prueba unitaria");
    System.out.println("Para esta prueba se utilizan 6 registros climaticos previos a la fecha actual " + UtilDate.formatDate(presumedCurrentDate) + ".");
    System.out.println("Suponemos que la fecha actual es " + UtilDate.formatDate(presumedCurrentDate) + ".");
    System.out.println();

    /*
     * Establece la ETc, la lluvia y la fecha de
     * 6 registros climaticos para esta prueba
     * unitaria
     */
    setClimateRecordsValuesNine();

    System.out.println("Los datos con los que se calculara la necesidad de agua de riego de un cultivo en la fecha actual (" + UtilDate.formatDate(presumedCurrentDate) + ") son los siguientes:");
    System.out.println();
    printClimateRecords(presumedCurrentDate, climateRecords);

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");

    double expectedResult = 0.0;
    double result = WaterMath.calculateIrrigationWaterNeed(climateRecords, zeroIrrigationRecords);

    System.out.println("* Valor esperado (nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateIrrigationWaterNeed");
    System.out.println("(nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testTenCalculateIrrigationWater() {
    System.out.println("************************************** Prueba diez del metodo calculateIrrigationWater ***************************************");
    printDescriptionMethodToTest();

    System.out.println("# Descripcion de la prueba unitaria");
    System.out.println("Para esta prueba se utilizan 6 registros climaticos previos a la fecha actual " + UtilDate.formatDate(presumedCurrentDate) + ".");
    System.out.println("Suponemos que la fecha actual es " + UtilDate.formatDate(presumedCurrentDate) + ".");
    System.out.println();

    /*
     * Establece la ETc, la lluvia y la fecha de
     * 6 registros climaticos para esta prueba
     * unitaria
     */
    setClimateRecordsValuesTen();

    System.out.println("Los datos con los que se calculara la necesidad de agua de riego de un cultivo en la fecha actual (" + UtilDate.formatDate(presumedCurrentDate) + ") son los siguientes:");
    System.out.println();
    printClimateRecords(presumedCurrentDate, climateRecords);

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");

    double expectedResult = 0.0;
    double result = WaterMath.calculateIrrigationWaterNeed(climateRecords, zeroIrrigationRecords);

    System.out.println("* Valor esperado (nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateIrrigationWaterNeed");
    System.out.println("(nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testElevenCalculateIrrigationWater() {
    System.out.println("************************************** Prueba once del metodo calculateIrrigationWater ***************************************");
    printDescriptionMethodToTest();

    System.out.println("# Descripcion de la prueba unitaria");
    System.out.println("Para esta prueba se utilizan 6 registros climaticos previos a la fecha actual " + UtilDate.formatDate(presumedCurrentDate) + ".");
    System.out.println("Suponemos que la fecha actual es " + UtilDate.formatDate(presumedCurrentDate) + ".");
    System.out.println();

    /*
     * Establece la ETc, la lluvia y la fecha de
     * 6 registros climaticos para esta prueba
     * unitaria
     */
    setClimateRecordsValuesEleven();

    System.out.println("Los datos con los que se calculara la necesidad de agua de riego de un cultivo en la fecha actual (" + UtilDate.formatDate(presumedCurrentDate) + ") son los siguientes:");
    System.out.println();
    printClimateRecords(presumedCurrentDate, climateRecords);

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");

    double expectedResult = 1.0;
    double result = WaterMath.calculateIrrigationWaterNeed(climateRecords, zeroIrrigationRecords);

    System.out.println("* Valor esperado (nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateIrrigationWaterNeed");
    System.out.println("(nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testTwelveCalculateIrrigationWater() {
    System.out.println("************************************** Prueba doce del metodo calculateIrrigationWater ***************************************");
    printDescriptionMethodToTest();

    System.out.println("# Descripcion de la prueba unitaria");
    System.out.println("Para esta prueba se utiliza 1 registro climatico, el cual es inmediatamente anterior a la fecha actual "
            + UtilDate.formatDate(presumedCurrentDate) + ".");
    System.out.println("Suponemos que la fecha actual es " + UtilDate.formatDate(presumedCurrentDate) + ".");
    System.out.println();

    System.out.println("Tambien se utiliza un conjunto de 2 registros de riego, los cuales pertenecen a la fecha inmediatamente anterior a la fecha");
    System.out.println("actual.");
    System.out.println();

    /*
     * Creacion de un registro climatico
     */
    ClimateRecord yesterdayClimateRecord = new ClimateRecord();
    yesterdayClimateRecord.setEtc(10);
    yesterdayClimateRecord.setPrecip(2);
    yesterdayClimateRecord.setDate(daySix);

    Collection<ClimateRecord> yesterdayClimateRecords = new ArrayList<>();
    yesterdayClimateRecords.add(yesterdayClimateRecord);

    /*
     * Creacion de registros de riego
     */
    IrrigationRecord irrigationRecordOne = new IrrigationRecord();
    irrigationRecordOne.setDate(daySix);
    irrigationRecordOne.setIrrigationDone(1.5);

    IrrigationRecord irrigationRecordTwo = new IrrigationRecord();
    irrigationRecordTwo.setDate(daySix);
    irrigationRecordTwo.setIrrigationDone(2.0);

    Collection<IrrigationRecord> yesterdayIrrigationRecords = new ArrayList<>();
    yesterdayIrrigationRecords.add(irrigationRecordOne);
    yesterdayIrrigationRecords.add(irrigationRecordTwo);

    System.out.println("Los datos con los que se calculara la necesidad de agua de riego de un cultivo en la fecha actual (" + UtilDate.formatDate(presumedCurrentDate) + ") son del dia");
    System.out.println("inmediatamente anterior al dia actual y son los siguientes:");
    System.out.println("Fecha: " + UtilDate.formatDate(daySix));
    System.out.println("ETc = " + yesterdayClimateRecord.getEtc() + " mm/dia");
    System.out.println("Lluvia = " + yesterdayClimateRecord.getPrecip() + " mm/dia");
    System.out.println("Riego realizado ayer (reg. riego 1): " + irrigationRecordOne.getIrrigationDone() + " mm/dia");
    System.out.println("Riego realizado ayer (reg. riego 2): " + irrigationRecordTwo.getIrrigationDone() + " mm/dia");
    System.out.println();

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");

    double expectedResult = 4.5;
    double result = WaterMath.calculateIrrigationWaterNeed(yesterdayClimateRecords, yesterdayIrrigationRecords);

    System.out.println("* Valor esperado (nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateIrrigationWaterNeed");
    System.out.println("(nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  /*
   * ****************************************************************
   * A partir de aca comienzan las pruebas unitarias del metodo
   * calculateIrrigationWaterNeed de la clase WaterMath sobrecargado
   * con la cantidad total de agua de riego de una fecha dada, una
   * coleccion de registros climaticos y una coleccion de registros
   * de riego, siendo todos ellos previos a una fecha dada y
   * pertenecientes a una misma parcela
   * ****************************************************************
   */

  @Test
  public void testThirteenCalculateIrrigationWater() {
    System.out.println("************************************** Prueba trece del metodo sobrecargado calculateIrrigationWater ***************************************");
    printDescriptionTestOverloadedCalculateIrrigationWaterNeed();

    System.out.println("# Descripcion de la prueba unitaria");

    /*
     * Establece la ETc, la lluvia y la fecha de
     * 6 registros climaticos para esta prueba
     * unitaria
     */
    setClimateRecordsValuesThirteen();

    System.out.println("Los datos con los que se calculara la necesidad de agua de riego de un cultivo en la fecha actual (" + UtilDate.formatDate(presumedCurrentDate) + ") son los siguientes:");
    System.out.println();
    printClimateRecordsTestOverloadedCalculateIrrigationWaterNeed(presumedCurrentDate, climateRecords);

    double totalIrrigationWaterGivenDate = 0.0;

    System.out.println("La cantidad total de agua de riego en la fecha actual es: " + totalIrrigationWaterGivenDate + " [mm/dia].");
    System.out.println();
    System.out.println("Al ser la cantidad total de agua de riego de la fecha actual mayor o igual al deficit acumulado de agua de dias previos a la");
    System.out.println("fecha actual, el valor devuelto por el metodo sobrecargado calculateIrrigationWater de la clase WaterMath debe ser 0 [mm/dia].");
    System.out.println("Por lo tanto, la necesidad de agua de riego de un cultivo en la fecha actual es 0 [mm/dia].");
    System.out.println();

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");

    double expectedResult = 0.0;
    double result = WaterMath.calculateIrrigationWaterNeed(totalIrrigationWaterGivenDate, climateRecords, zeroIrrigationRecords);

    System.out.println("* Valor esperado (nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateIrrigationWaterNeed sobrecargado (nec. agua riego [mm/dia] de un cultivo en");
    System.out.println("fecha actual): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testFourteenCalculateIrrigationWater() {
    System.out.println("************************************** Prueba catorce del metodo sobrecargado calculateIrrigationWater ***************************************");
    printDescriptionTestOverloadedCalculateIrrigationWaterNeed();

    System.out.println("# Descripcion de la prueba unitaria");

    /*
     * Establece la ETc, la lluvia y la fecha de
     * 6 registros climaticos para esta prueba
     * unitaria
     */
    setClimateRecordsValuesFourteen();

    System.out.println("Los datos con los que se calculara la necesidad de agua de riego de un cultivo en la fecha actual (" + UtilDate.formatDate(presumedCurrentDate) + ") son los siguientes:");
    System.out.println();
    printClimateRecordsTestOverloadedCalculateIrrigationWaterNeed(presumedCurrentDate, climateRecords);

    double totalIrrigationWaterGivenDate = 5.0;

    System.out.println("La cantidad total de agua de riego en la fecha actual es: " + totalIrrigationWaterGivenDate + " [mm/dia].");
    System.out.println();
    System.out.println("Al ser la cantidad total de agua de riego de la fecha actual mayor o igual al deficit acumulado de agua de dias previos a la");
    System.out.println("fecha actual, el valor devuelto por el metodo sobrecargado calculateIrrigationWater de la clase WaterMath debe ser 0 [mm/dia].");
    System.out.println("Por lo tanto, la necesidad de agua de riego de un cultivo en la fecha actual es 0 [mm/dia].");
    System.out.println();

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");

    double expectedResult = 0.0;
    double result = WaterMath.calculateIrrigationWaterNeed(totalIrrigationWaterGivenDate, climateRecords, zeroIrrigationRecords);

    System.out.println("* Valor esperado (nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateIrrigationWaterNeed sobrecargado (nec. agua riego [mm/dia] de un cultivo en");
    System.out.println("fecha actual): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testFifteenCalculateIrrigationWater() {
    System.out.println("************************************** Prueba quince del metodo sobrecargado calculateIrrigationWater ***************************************");
    printDescriptionTestOverloadedCalculateIrrigationWaterNeed();

    System.out.println("# Descripcion de la prueba unitaria");

    /*
     * Establece la ETc, la lluvia y la fecha de
     * 6 registros climaticos para esta prueba
     * unitaria
     */
    setClimateRecordsValuesFifteen();

    System.out.println("Los datos con los que se calculara la necesidad de agua de riego de un cultivo en la fecha actual (" + UtilDate.formatDate(presumedCurrentDate) + ") son los siguientes:");
    System.out.println();
    printClimateRecordsTestOverloadedCalculateIrrigationWaterNeed(presumedCurrentDate, climateRecords);

    double totalIrrigationWaterGivenDate = 14.0;

    System.out.println("La cantidad total de agua de riego en la fecha actual es: " + totalIrrigationWaterGivenDate + " [mm/dia].");
    System.out.println();
    System.out.println("Al ser la cantidad total de agua de riego de la fecha actual mayor o igual al deficit acumulado de agua de dias previos a la");
    System.out.println("fecha actual, el valor devuelto por el metodo sobrecargado calculateIrrigationWater de la clase WaterMath debe ser 0 [mm/dia].");
    System.out.println("Por lo tanto, la necesidad de agua de riego de un cultivo en la fecha actual es 0 [mm/dia].");
    System.out.println();

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");

    double expectedResult = 0.0;
    double result = WaterMath.calculateIrrigationWaterNeed(totalIrrigationWaterGivenDate, climateRecords, zeroIrrigationRecords);

    System.out.println("* Valor esperado (nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateIrrigationWaterNeed sobrecargado (nec. agua riego [mm/dia] de un cultivo en");
    System.out.println("fecha actual): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testSixteenCalculateIrrigationWater() {
    System.out.println("************************************** Prueba dieciseis del metodo sobrecargado calculateIrrigationWater ***************************************");
    printDescriptionTestOverloadedCalculateIrrigationWaterNeed();

    System.out.println("# Descripcion de la prueba unitaria");

    /*
     * Establece la ETc, la lluvia y la fecha de
     * 6 registros climaticos para esta prueba
     * unitaria
     */
    setClimateRecordsValuesSixteen();

    System.out.println("Los datos con los que se calculara la necesidad de agua de riego de un cultivo en la fecha actual (" + UtilDate.formatDate(presumedCurrentDate) + ") son los siguientes:");
    System.out.println();
    printClimateRecordsTestOverloadedCalculateIrrigationWaterNeed(presumedCurrentDate, climateRecords);

    double totalIrrigationWaterGivenDate = 21.0;

    System.out.println("La cantidad total de agua de riego en la fecha actual es: " + totalIrrigationWaterGivenDate + " [mm/dia].");
    System.out.println();
    System.out.println("Al ser la cantidad total de agua de riego de la fecha actual mayor o igual al deficit acumulado de agua de dias previos a la");
    System.out.println("fecha actual, el valor devuelto por el metodo sobrecargado calculateIrrigationWater de la clase WaterMath debe ser 0 [mm/dia].");
    System.out.println("Por lo tanto, la necesidad de agua de riego de un cultivo en la fecha actual es 0 [mm/dia].");
    System.out.println();

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");

    double expectedResult = 0.0;
    double result = WaterMath.calculateIrrigationWaterNeed(totalIrrigationWaterGivenDate, climateRecords, zeroIrrigationRecords);

    System.out.println("* Valor esperado (nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateIrrigationWaterNeed sobrecargado (nec. agua riego [mm/dia] de un cultivo en");
    System.out.println("fecha actual): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testSeventeenCalculateIrrigationWater() {
    System.out.println("************************************** Prueba diecisiete del metodo sobrecargado calculateIrrigationWater ***************************************");
    printDescriptionTestOverloadedCalculateIrrigationWaterNeed();

    System.out.println("# Descripcion de la prueba unitaria");

    /*
     * Establece la ETc, la lluvia y la fecha de
     * 6 registros climaticos para esta prueba
     * unitaria
     */
    setClimateRecordsValuesSeventeen();

    System.out.println("Los datos con los que se calculara la necesidad de agua de riego de un cultivo en la fecha actual (" + UtilDate.formatDate(presumedCurrentDate) + ") son los siguientes:");
    System.out.println();
    printClimateRecordsTestOverloadedCalculateIrrigationWaterNeed(presumedCurrentDate, climateRecords);

    double totalIrrigationWaterGivenDate = 7.0;

    System.out.println("La cantidad total de agua de riego en la fecha actual es: " + totalIrrigationWaterGivenDate + " [mm/dia].");
    System.out.println();
    System.out.println("Al ser la cantidad total de agua de riego de la fecha actual estrictamente menor al deficit acumulado de agua de dias previos a la");
    System.out.println("fecha actual, el valor devuelto por el metodo sobrecargado calculateIrrigationWater de la clase WaterMath debe ser el resultado de");
    System.out.println("la diferencia entre el deficit acumulado de agua de dias previos a la fecha actual y la cantidad de agua de riego de la fecha actual.");
    System.out.println();

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");

    double expectedResult = 7.0;
    double result = WaterMath.calculateIrrigationWaterNeed(totalIrrigationWaterGivenDate, climateRecords, zeroIrrigationRecords);

    System.out.println("* Valor esperado (nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateIrrigationWaterNeed sobrecargado (nec. agua riego [mm/dia] de un cultivo en");
    System.out.println("fecha actual): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  /*
   * ****************************************************************
   * A partir de aca comienzan las pruebas unitarias del metodo
   * calculateIrrigationWaterNeed de la clase WaterMath sobrecargado
   * con la cantidad total de agua de riego de una fecha dada, una
   * coleccion de registros climaticos y una coleccion de registros
   * de riego, siendo todos ellos previos a una fecha dada y
   * pertenecientes a una misma parcela, haciendo uso del metodo
   * findAllByParcelIdAndPeriod de la clase ClimateRecordServiceBean
   * junto con los datos de las pruebas unitarias 1 a 11. El motivo
   * de esto es que este metodo calculateIrrigationWaterNeed hasta
   * ahora NO fue probado con el metodo findAllByParcelIdAndPeriod
   * de la clase ClimateRecordServiceBean.
   * ****************************************************************
   */

  @Test
  public void testEighteenCalculateIrrigationWater() {
    System.out.println("************************************** Prueba dieciocho del metodo sobrecargado calculateIrrigationWater ***************************************");
    printDescriptionTestOverloadedCalculateIrrigationWaterNeed();

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
    Option userOption = optionService.create();
    entityManager.getTransaction().commit();

    options.add(userOption);

    /*
     * Persistencia de un usuario de prueba
     */
    User givenUser = new User();
    givenUser.setUsername("giovanni");
    givenUser.setName("Giovanni");
    givenUser.setLastName("Auditore");
    givenUser.setEmail("giovanni@eservice.com");
    givenUser.setPassword("Giovanni");
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
    printClimateRecords(presumedCurrentDate, recoveredClimateRecords);

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
    double result = WaterMath.calculateIrrigationWaterNeed(0, recoveredClimateRecords, zeroIrrigationRecords);

    System.out.println("* Valor esperado (nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateIrrigationWaterNeed");
    System.out.println("(nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testNineteenCalculateIrrigationWater() {
    System.out.println("************************************** Prueba diecinueve del metodo sobrecargado calculateIrrigationWater ***************************************");
    printDescriptionTestOverloadedCalculateIrrigationWaterNeed();

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
    Option userOption = optionService.create();
    entityManager.getTransaction().commit();

    options.add(userOption);

    /*
     * Persistencia de un usuario de prueba
     */
    User givenUser = new User();
    givenUser.setUsername("alyx");
    givenUser.setName("Alyx");
    givenUser.setLastName("Vance");
    givenUser.setEmail("alyx@eservice.com");
    givenUser.setPassword("Alyx");
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
    printClimateRecords(presumedCurrentDate, recoveredClimateRecords);

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
    double result = WaterMath.calculateIrrigationWaterNeed(0, recoveredClimateRecords, zeroIrrigationRecords);

    System.out.println("* Valor esperado (nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateIrrigationWaterNeed");
    System.out.println("(nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testTwentyCalculateIrrigationWater() {
    System.out.println("************************************** Prueba veinte del metodo sobrecargado calculateIrrigationWater ***************************************");
    printDescriptionTestOverloadedCalculateIrrigationWaterNeed();

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
    Option userOption = optionService.create();
    entityManager.getTransaction().commit();

    options.add(userOption);

    /*
     * Persistencia de un usuario de prueba
     */
    User givenUser = new User();
    givenUser.setUsername("eli");
    givenUser.setName("Eli");
    givenUser.setLastName("Vance");
    givenUser.setEmail("eli@eservice.com");
    givenUser.setPassword("Eli");
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
    printClimateRecords(presumedCurrentDate, recoveredClimateRecords);

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
    double result = WaterMath.calculateIrrigationWaterNeed(0, recoveredClimateRecords, zeroIrrigationRecords);

    System.out.println("* Valor esperado (nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateIrrigationWaterNeed");
    System.out.println("(nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testTwentyOneCalculateIrrigationWater() {
    System.out.println("************************************** Prueba veintiuno del metodo sobrecargado calculateIrrigationWater ***************************************");
    printDescriptionTestOverloadedCalculateIrrigationWaterNeed();

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
    Option userOption = optionService.create();
    entityManager.getTransaction().commit();

    options.add(userOption);

    /*
     * Persistencia de un usuario de prueba
     */
    User givenUser = new User();
    givenUser.setUsername("vorti");
    givenUser.setName("Vorti");
    givenUser.setLastName("Vance");
    givenUser.setEmail("vorti@eservice.com");
    givenUser.setPassword("Vorti");
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
    printClimateRecords(presumedCurrentDate, recoveredClimateRecords);

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
    double result = WaterMath.calculateIrrigationWaterNeed(0, recoveredClimateRecords, zeroIrrigationRecords);

    System.out.println("* Valor esperado (nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateIrrigationWaterNeed");
    System.out.println("(nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testTwentyTwoCalculateIrrigationWater() {
    System.out.println("************************************** Prueba veintidos del metodo sobrecargado calculateIrrigationWater ***************************************");
    printDescriptionTestOverloadedCalculateIrrigationWaterNeed();

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
    Option userOption = optionService.create();
    entityManager.getTransaction().commit();

    options.add(userOption);

    /*
     * Persistencia de un usuario de prueba
     */
    User givenUser = new User();
    givenUser.setUsername("barney");
    givenUser.setName("Barney");
    givenUser.setLastName("Vance");
    givenUser.setEmail("barney@eservice.com");
    givenUser.setPassword("Barney");
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
    printClimateRecords(presumedCurrentDate, recoveredClimateRecords);

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
    double result = WaterMath.calculateIrrigationWaterNeed(0, recoveredClimateRecords, zeroIrrigationRecords);

    System.out.println("* Valor esperado (nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateIrrigationWaterNeed");
    System.out.println("(nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testTwentyThreeCalculateIrrigationWater() {
    System.out.println("************************************** Prueba ventitres del metodo sobrecargado calculateIrrigationWater ***************************************");
    printDescriptionTestOverloadedCalculateIrrigationWaterNeed();

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
    Option userOption = optionService.create();
    entityManager.getTransaction().commit();

    options.add(userOption);

    /*
     * Persistencia de un usuario de prueba
     */
    User givenUser = new User();
    givenUser.setUsername("william");
    givenUser.setName("William");
    givenUser.setLastName("Vance");
    givenUser.setEmail("william@eservice.com");
    givenUser.setPassword("William");
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
    printClimateRecords(presumedCurrentDate, recoveredClimateRecords);

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
    double result = WaterMath.calculateIrrigationWaterNeed(0, recoveredClimateRecords, zeroIrrigationRecords);

    System.out.println("* Valor esperado (nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateIrrigationWaterNeed");
    System.out.println("(nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testTwentyFourCalculateIrrigationWater() {
    System.out.println("************************************** Prueba veinticuatro del metodo sobrecargado calculateIrrigationWater ***************************************");
    printDescriptionTestOverloadedCalculateIrrigationWaterNeed();

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
    Option userOption = optionService.create();
    entityManager.getTransaction().commit();

    options.add(userOption);

    /*
     * Persistencia de un usuario de prueba
     */
    User givenUser = new User();
    givenUser.setUsername("tom");
    givenUser.setName("Tom");
    givenUser.setLastName("Vance");
    givenUser.setEmail("tom@eservice.com");
    givenUser.setPassword("Tom");
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
    printClimateRecords(presumedCurrentDate, recoveredClimateRecords);

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
    double result = WaterMath.calculateIrrigationWaterNeed(0, recoveredClimateRecords, zeroIrrigationRecords);

    System.out.println("* Valor esperado (nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateIrrigationWaterNeed");
    System.out.println("(nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testTwentyFiveCalculateIrrigationWater() {
    System.out.println("************************************** Prueba veinticinco del metodo sobrecargado calculateIrrigationWater ***************************************");
    printDescriptionTestOverloadedCalculateIrrigationWaterNeed();

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
    Option userOption = optionService.create();
    entityManager.getTransaction().commit();

    options.add(userOption);

    /*
     * Persistencia de un usuario de prueba
     */
    User givenUser = new User();
    givenUser.setUsername("jerry");
    givenUser.setName("Jerry");
    givenUser.setLastName("Vance");
    givenUser.setEmail("jerry@eservice.com");
    givenUser.setPassword("Jerry");
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
    printClimateRecords(presumedCurrentDate, recoveredClimateRecords);

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
    double result = WaterMath.calculateIrrigationWaterNeed(0, recoveredClimateRecords, zeroIrrigationRecords);

    System.out.println("* Valor esperado (nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateIrrigationWaterNeed");
    System.out.println("(nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testTwentySixCalculateIrrigationWater() {
    System.out.println("************************************** Prueba veintiseis del metodo sobrecargado calculateIrrigationWater ***************************************");
    printDescriptionTestOverloadedCalculateIrrigationWaterNeed();

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
    Option userOption = optionService.create();
    entityManager.getTransaction().commit();

    options.add(userOption);

    /*
     * Persistencia de un usuario de prueba
     */
    User givenUser = new User();
    givenUser.setUsername("leon");
    givenUser.setName("Leon");
    givenUser.setLastName("Vance");
    givenUser.setEmail("leon@eservice.com");
    givenUser.setPassword("Leon");
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
    printClimateRecords(presumedCurrentDate, recoveredClimateRecords);

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
    double result = WaterMath.calculateIrrigationWaterNeed(0, recoveredClimateRecords, zeroIrrigationRecords);

    System.out.println("* Valor esperado (nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateIrrigationWaterNeed");
    System.out.println("(nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testTwentySevenCalculateIrrigationWater() {
    System.out.println("************************************** Prueba veintisiete del metodo sobrecargado calculateIrrigationWater ***************************************");
    printDescriptionTestOverloadedCalculateIrrigationWaterNeed();

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
    Option userOption = optionService.create();
    entityManager.getTransaction().commit();

    options.add(userOption);

    /*
     * Persistencia de un usuario de prueba
     */
    User givenUser = new User();
    givenUser.setUsername("claire");
    givenUser.setName("Claire");
    givenUser.setLastName("Redfield");
    givenUser.setEmail("claire@eservice.com");
    givenUser.setPassword("Claire");
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
    printClimateRecords(presumedCurrentDate, recoveredClimateRecords);

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
    double result = WaterMath.calculateIrrigationWaterNeed(0, recoveredClimateRecords, zeroIrrigationRecords);

    System.out.println("* Valor esperado (nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateIrrigationWaterNeed");
    System.out.println("(nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testTwentyEightCalculateIrrigationWater() {
    System.out.println("************************************** Prueba veintiocho del metodo sobrecargado calculateIrrigationWater ***************************************");
    printDescriptionTestOverloadedCalculateIrrigationWaterNeed();

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
    Option userOption = optionService.create();
    entityManager.getTransaction().commit();

    options.add(userOption);

    /*
     * Persistencia de un usuario de prueba
     */
    User givenUser = new User();
    givenUser.setUsername("chris");
    givenUser.setName("Chris");
    givenUser.setLastName("Redfield");
    givenUser.setEmail("chris@eservice.com");
    givenUser.setPassword("Chris");
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
    printClimateRecords(presumedCurrentDate, recoveredClimateRecords);

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
    double result = WaterMath.calculateIrrigationWaterNeed(0, recoveredClimateRecords, zeroIrrigationRecords);

    System.out.println("* Valor esperado (nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateIrrigationWaterNeed");
    System.out.println("(nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  /*
   * ****************************************************************
   * A partir de aca comienzan las pruebas unitarias del metodo
   * calculateIrrigationWaterNeed de la clase WaterMath sobrecargado
   * con la cantidad total de agua de riego de una fecha dada, una
   * coleccion de registros climaticos y una coleccion de registros
   * de riego, siendo todos ellos previos a una fecha dada y
   * pertenecientes a una misma parcela, haciendo uso del metodo
   * findAllByParcelIdAndPeriod de las clases ClimateRecordServiceBean
   * e IrrigationRecordServiceBean junto con los datos de las pruebas
   * unitarias 1 a 11. El motivo de esto es que este metodo
   * calculateIrrigationWaterNeed hasta ahora NO fue probado con el
   * metodo findAllByParcelIdAndPeriod de las clases
   * ClimateRecordServiceBean e IrrigationRecordServiceBean.
   * 
   * La idea de estas pruebas unitarias es demostrar que el metodo
   * calculateIrrigationWater de la clase WaterMath sobrecargado con
   * la cantidad total de agua de riego de una fecha dada calcula
   * correctamente la necesidad de agua de riego de un cultivo en
   * una fecha dada utilizando unicamente el agua de riego de dias
   * previos a la fecha actual. Por este motivo los registros climaticos
   * de prueba tendran precipitacion igual a 0, mientras que los
   * registros de riego tendran como riego realizado los valores de
   * las precipitaciones utilizados en los registros climaticos de
   * las pruebas unitarias 1 a 11 de esta clase. Se reutilizan los
   * datos de la precipitacion para hacer mas facil la tarea de probar
   * el metodo calculateIrrigationWater.
   * ****************************************************************
   */

  @Test
  public void testTwentyNineCalculateIrrigationWater() {
    System.out.println("************************************** Prueba veintinueve del metodo sobrecargado calculateIrrigationWater ***************************************");
    printDescriptionTestOverloadedCalculateIrrigationWaterNeed();

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
    Option userOption = optionService.create();
    entityManager.getTransaction().commit();

    options.add(userOption);

    /*
     * Persistencia de un usuario de prueba
     */
    User givenUser = new User();
    givenUser.setUsername("elizabeth");
    givenUser.setName("Elizabeth");
    givenUser.setLastName("Cross");
    givenUser.setEmail("elizabeth@eservice.com");
    givenUser.setPassword("Elizabeth");
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
    printIrrigationRecords(presumedCurrentDate, (List) recoveredIrrigationRecords, (List) recoveredClimateRecords);

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
    double result = WaterMath.calculateIrrigationWaterNeed(0, recoveredClimateRecords, recoveredIrrigationRecords);

    System.out.println("* Valor esperado (nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateIrrigationWaterNeed");
    System.out.println("(nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testThirtyCalculateIrrigationWater() {
    System.out.println("************************************** Prueba treinta del metodo sobrecargado calculateIrrigationWater ***************************************");
    printDescriptionTestOverloadedCalculateIrrigationWaterNeed();

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
    Option userOption = optionService.create();
    entityManager.getTransaction().commit();

    options.add(userOption);

    /*
     * Persistencia de un usuario de prueba
     */
    User givenUser = new User();
    givenUser.setUsername("jacob");
    givenUser.setName("Jacob");
    givenUser.setLastName("temple");
    givenUser.setEmail("jacob@eservice.com");
    givenUser.setPassword("Jacob");
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
    printIrrigationRecords(presumedCurrentDate, (List) recoveredIrrigationRecords, (List) recoveredClimateRecords);

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
    double result = WaterMath.calculateIrrigationWaterNeed(0, recoveredClimateRecords, recoveredIrrigationRecords);

    System.out.println("* Valor esperado (nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateIrrigationWaterNeed");
    System.out.println("(nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testThirtyOneCalculateIrrigationWater() {
    System.out.println("************************************** Prueba treintaiuno del metodo sobrecargado calculateIrrigationWater ***************************************");
    printDescriptionTestOverloadedCalculateIrrigationWaterNeed();

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
    Option userOption = optionService.create();
    entityManager.getTransaction().commit();

    options.add(userOption);

    /*
     * Persistencia de un usuario de prueba
     */
    User givenUser = new User();
    givenUser.setUsername("isaac");
    givenUser.setName("Isaac");
    givenUser.setLastName("Clarke");
    givenUser.setEmail("isaac@eservice.com");
    givenUser.setPassword("Isaac");
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
    printIrrigationRecords(presumedCurrentDate, (List) recoveredIrrigationRecords, (List) recoveredClimateRecords);

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
    double result = WaterMath.calculateIrrigationWaterNeed(0, recoveredClimateRecords, recoveredIrrigationRecords);

    System.out.println("* Valor esperado (nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateIrrigationWaterNeed");
    System.out.println("(nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testThirtyTwoCalculateIrrigationWater() {
    System.out.println("************************************** Prueba treintaidos del metodo sobrecargado calculateIrrigationWater ***************************************");
    printDescriptionTestOverloadedCalculateIrrigationWaterNeed();

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
    Option userOption = optionService.create();
    entityManager.getTransaction().commit();

    options.add(userOption);

    /*
     * Persistencia de un usuario de prueba
     */
    User givenUser = new User();
    givenUser.setUsername("zach");
    givenUser.setName("Hammond");
    givenUser.setLastName("hammond");
    givenUser.setEmail("zach@eservice.com");
    givenUser.setPassword("Hammond");
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
    printIrrigationRecords(presumedCurrentDate, (List) recoveredIrrigationRecords, (List) recoveredClimateRecords);

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
    double result = WaterMath.calculateIrrigationWaterNeed(0, recoveredClimateRecords, recoveredIrrigationRecords);

    System.out.println("* Valor esperado (nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateIrrigationWaterNeed");
    System.out.println("(nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testThirtyThreeCalculateIrrigationWater() {
    System.out.println("************************************** Prueba treintaitres del metodo sobrecargado calculateIrrigationWater ***************************************");
    printDescriptionTestOverloadedCalculateIrrigationWaterNeed();

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
    Option userOption = optionService.create();
    entityManager.getTransaction().commit();

    options.add(userOption);

    /*
     * Persistencia de un usuario de prueba
     */
    User givenUser = new User();
    givenUser.setUsername("chen");
    givenUser.setName("Chen");
    givenUser.setLastName("temple");
    givenUser.setEmail("chen@eservice.com");
    givenUser.setPassword("Chen");
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
    printIrrigationRecords(presumedCurrentDate, (List) recoveredIrrigationRecords, (List) recoveredClimateRecords);

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
    double result = WaterMath.calculateIrrigationWaterNeed(0, recoveredClimateRecords, recoveredIrrigationRecords);

    System.out.println("* Valor esperado (nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateIrrigationWaterNeed");
    System.out.println("(nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testThirtyFourCalculateIrrigationWater() {
    System.out.println("************************************** Prueba treintaicuatro del metodo sobrecargado calculateIrrigationWater ***************************************");
    printDescriptionTestOverloadedCalculateIrrigationWaterNeed();

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
    Option userOption = optionService.create();
    entityManager.getTransaction().commit();

    options.add(userOption);

    /*
     * Persistencia de un usuario de prueba
     */
    User givenUser = new User();
    givenUser.setUsername("nicole");
    givenUser.setName("Nicole");
    givenUser.setLastName("brennan");
    givenUser.setEmail("nicole@eservice.com");
    givenUser.setPassword("Nicole");
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
    printIrrigationRecords(presumedCurrentDate, (List) recoveredIrrigationRecords, (List) recoveredClimateRecords);

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
    double result = WaterMath.calculateIrrigationWaterNeed(0, recoveredClimateRecords, recoveredIrrigationRecords);

    System.out.println("* Valor esperado (nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateIrrigationWaterNeed");
    System.out.println("(nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testThirtyFiveCalculateIrrigationWater() {
    System.out.println("************************************** Prueba treintaicinco del metodo sobrecargado calculateIrrigationWater ***************************************");
    printDescriptionTestOverloadedCalculateIrrigationWaterNeed();

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
    Option userOption = optionService.create();
    entityManager.getTransaction().commit();

    options.add(userOption);

    /*
     * Persistencia de un usuario de prueba
     */
    User givenUser = new User();
    givenUser.setUsername("mercer");
    givenUser.setName("Mercer");
    givenUser.setLastName("mallus");
    givenUser.setEmail("mercer@eservice.com");
    givenUser.setPassword("Mercer");
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
    printIrrigationRecords(presumedCurrentDate, (List) recoveredIrrigationRecords, (List) recoveredClimateRecords);

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
    double result = WaterMath.calculateIrrigationWaterNeed(0, recoveredClimateRecords, recoveredIrrigationRecords);

    System.out.println("* Valor esperado (nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateIrrigationWaterNeed");
    System.out.println("(nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testThirtySixCalculateIrrigationWater() {
    System.out.println("************************************** Prueba treintaiseis del metodo sobrecargado calculateIrrigationWater ***************************************");
    printDescriptionTestOverloadedCalculateIrrigationWaterNeed();

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
    Option userOption = optionService.create();
    entityManager.getTransaction().commit();

    options.add(userOption);

    /*
     * Persistencia de un usuario de prueba
     */
    User givenUser = new User();
    givenUser.setUsername("kendra");
    givenUser.setName("Kendra");
    givenUser.setLastName("daniels");
    givenUser.setEmail("kendra@eservice.com");
    givenUser.setPassword("Kendra");
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
    printIrrigationRecords(presumedCurrentDate, (List) recoveredIrrigationRecords, (List) recoveredClimateRecords);

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
    double result = WaterMath.calculateIrrigationWaterNeed(0, recoveredClimateRecords, recoveredIrrigationRecords);

    System.out.println("* Valor esperado (nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateIrrigationWaterNeed");
    System.out.println("(nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testThirtySevenCalculateIrrigationWater() {
    System.out.println("************************************** Prueba treintaisiete del metodo sobrecargado calculateIrrigationWater ***************************************");
    printDescriptionTestOverloadedCalculateIrrigationWaterNeed();

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
    Option userOption = optionService.create();
    entityManager.getTransaction().commit();

    options.add(userOption);

    /*
     * Persistencia de un usuario de prueba
     */
    User givenUser = new User();
    givenUser.setUsername("ludmila");
    givenUser.setName("Ludmila");
    givenUser.setLastName("bolt");
    givenUser.setEmail("lumdila@eservice.com");
    givenUser.setPassword("Ludmila");
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
    printIrrigationRecords(presumedCurrentDate, (List) recoveredIrrigationRecords, (List) recoveredClimateRecords);

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
    double result = WaterMath.calculateIrrigationWaterNeed(0, recoveredClimateRecords, recoveredIrrigationRecords);

    System.out.println("* Valor esperado (nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateIrrigationWaterNeed");
    System.out.println("(nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testThirtyEightCalculateIrrigationWater() {
    System.out.println("************************************** Prueba treintaiocho del metodo sobrecargado calculateIrrigationWater ***************************************");
    printDescriptionTestOverloadedCalculateIrrigationWaterNeed();

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
    Option userOption = optionService.create();
    entityManager.getTransaction().commit();

    options.add(userOption);

    /*
     * Persistencia de un usuario de prueba
     */
    User givenUser = new User();
    givenUser.setUsername("camila");
    givenUser.setName("Camila");
    givenUser.setLastName("bolt");
    givenUser.setEmail("camila@eservice.com");
    givenUser.setPassword("Camila");
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
    printIrrigationRecords(presumedCurrentDate, (List) recoveredIrrigationRecords, (List) recoveredClimateRecords);

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
    double result = WaterMath.calculateIrrigationWaterNeed(0, recoveredClimateRecords, recoveredIrrigationRecords);

    System.out.println("* Valor esperado (nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateIrrigationWaterNeed");
    System.out.println("(nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testThirtyNineCalculateIrrigationWater() {
    System.out.println("************************************** Prueba treintainueve del metodo sobrecargado calculateIrrigationWater ***************************************");
    printDescriptionTestOverloadedCalculateIrrigationWaterNeed();

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
    Option userOption = optionService.create();
    entityManager.getTransaction().commit();

    options.add(userOption);

    /*
     * Persistencia de un usuario de prueba
     */
    User givenUser = new User();
    givenUser.setUsername("tanenbaum");
    givenUser.setName("Andrew");
    givenUser.setLastName("tanenbaum");
    givenUser.setEmail("andrew@eservice.com");
    givenUser.setPassword("Andrew");
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
    printIrrigationRecords(presumedCurrentDate, (List) recoveredIrrigationRecords, (List) recoveredClimateRecords);

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
    double result = WaterMath.calculateIrrigationWaterNeed(0, recoveredClimateRecords, recoveredIrrigationRecords);

    System.out.println("* Valor esperado (nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateIrrigationWaterNeed");
    System.out.println("(nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testFourtyCalculateIrrigationWaterNeed() {
    System.out.println("************************************** Prueba cuarenta del metodo sobrecargado calculateIrrigationWater ***************************************");
    printDescriptionTestOverloadedCalculateIrrigationWaterNeed();

    System.out.println("# Descripcion de la prueba unitaria");
    System.out.println("El objetivo de esta prueba es demostrar con un ejemplo simple que el metodo calculateIrrigationWater de la clase WaterMath sobrecargado");
    System.out.println("con la cantidad total de agua de riego de una fecha dada, calcula correctamente la necesidad de agua de riego de un cultivo en una fecha");
    System.out.println("dada cuando se le pasa como argumento registros climaticos y registros de riego que tienen valores mayores a cero en la precipitacion");
    System.out.println("y en el riego realizado, respectivamente.");
    System.out.println();
    System.out.println("Para esta prueba se utilizan 1 registro climatico y 1 registro de riego, siendo ambos del dia inmediatamente anterior a la fecha actual");
    System.out.println(UtilDate.formatDate(presumedCurrentDate) + "." + " Suponemos que la fecha actual es " + UtilDate.formatDate(presumedCurrentDate) + ".");
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
    Option userOption = optionService.create();
    entityManager.getTransaction().commit();

    options.add(userOption);

    /*
     * Persistencia de un usuario de prueba
     */
    User givenUser = new User();
    givenUser.setUsername("kurose");
    givenUser.setName("James");
    givenUser.setLastName("kurose");
    givenUser.setEmail("james@eservice.com");
    givenUser.setPassword("James");
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
     * Persistencia de registros climaticos de prueba
     */
    ClimateRecord climateRecordOne = new ClimateRecord();
    climateRecordOne.setEtc(10);
    climateRecordOne.setPrecip(2.5);
    climateRecordOne.setDate(daySix);
    climateRecordOne.setParcel(givenParcel);

    entityManager.getTransaction().begin();
    climateRecordOne = climateRecordService.create(climateRecordOne);
    entityManager.getTransaction().commit();

    climateRecordsToBeDeleted.add(climateRecordOne);

    /*
     * Persistencia de registros de riego de prueba
     */
    IrrigationRecord irrigationRecordOne = new IrrigationRecord();
    irrigationRecordOne.setDate(daySix);
    irrigationRecordOne.setParcel(givenParcel);
    irrigationRecordOne.setIrrigationDone(2.5);

    entityManager.getTransaction().begin();
    irrigationRecordOne = irrigationRecordService.create(irrigationRecordOne);
    entityManager.getTransaction().commit();

    irrigationRecordsToBeDeleted.add(irrigationRecordOne);

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
    System.out.println("- Datos de la fecha " + UtilDate.formatDate(climateRecordOne.getDate()));
    System.out.println("ETc [mm/dia]: " + climateRecordOne.getEtc());
    System.out.println("Lluvia [mm/dia]: " + climateRecordOne.getPrecip());
    System.out.println("Riego [mm/dia]: " + irrigationRecordOne.getIrrigationDone());
    System.out.println("Deficit acumulado de agua [mm/dia]: " + ((climateRecordOne.getPrecip() + irrigationRecordOne.getIrrigationDone()) - climateRecordOne.getEtc()));
    System.out.println();

    System.out.println("Necesidad de agua de riego [mm/dia] de un cultivo en la fecha actual (hoy) " + UtilDate.formatDate(presumedCurrentDate)
        + ": " + Math.abs(((climateRecordOne.getPrecip() + irrigationRecordOne.getIrrigationDone()) - climateRecordOne.getEtc())));
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
    double result = WaterMath.calculateIrrigationWaterNeed(0, recoveredClimateRecords, recoveredIrrigationRecords);

    System.out.println("* Valor esperado (nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateIrrigationWaterNeed");
    System.out.println("(nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + result);
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
   * Imprime por pantalla una tabla que contiene el dia, la
   * ETc por dia, la lluvia por dia, el deficit de agua por
   * dia y el deficit acumulado de agua creada a partir de
   * un conjunto de registros climaticos previos a una
   * fecha dada
   * 
   * @param presumedCurrentDate
   * @param givenClimateRecords
   * @return double que representa el deficit acumulado de
   * agua de dias previos a una fecha dada [mm/dia]
   */
  private double printTable(Calendar presumedCurrentDate, Collection<ClimateRecord> givenClimateRecords) {
    int day = 1;
    double differencePerDay = 0.0;
    double accumulatedDeficit = 0.0;

    System.out.println("     Dia       " + "|" + "    ETc   " + "|" + "  Lluvia  " + "|" + "  Dif.  " + "|" + "  Def. Acum.  ");
    System.out.println("-----------------------------------------------------------");

    for (ClimateRecord currentClimateRecord : givenClimateRecords) {
      differencePerDay = calculateDifferencePerDay(currentClimateRecord);
      accumulatedDeficit = calculateAccumulatedDeficitPerDay(differencePerDay, accumulatedDeficit);

      System.out.print(" " + day + " (" + UtilDate.formatDate(currentClimateRecord.getDate()) + ")");
      System.out.print("  |");
      System.out.print("   " + currentClimateRecord.getEtc());
      System.out.print("   |");
      System.out.print("   " + currentClimateRecord.getPrecip());
      System.out.print("    |");
      System.out.print("  " + differencePerDay);
      System.out.print("  |");
      System.out.print("   " + accumulatedDeficit);
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
    System.out.println("Lluvia [mm/dia]");
    System.out.println("Diferencia (Lluvia - ETc) [mm/dia]");
    System.out.println("Deficit acumulado [mm/dia]");
    System.out.println();

    return accumulatedDeficit;
  }

  /**
   * Imprime una tabla con el dia, la ETc por dia, la lluvia por
   * dia, el deficit (diferencia entre el H2O y la ETc) de agua
   * por dia y el deficit acumulado de agua para el metodo
   * calculateIrrigationWaterNeed de la clase WaterMath que
   * tiene como parametros una coleccion de registros climaticos
   * y una coleccion de registros de riego, siendo todo ellos
   * previos a una fecha dada y pertenecientes a una misma parcela
   * 
   * @param presumedCurrentDate
   * @param givenClimateRecords
   */
  private void printClimateRecords(Calendar presumedCurrentDate, Collection<ClimateRecord> givenClimateRecords) {
    System.out.println("Necesidad de agua de riego [mm/dia] de un cultivo en la fecha actual (hoy) " + UtilDate.formatDate(presumedCurrentDate)
        + ": " + Math.abs(printTable(presumedCurrentDate, givenClimateRecords)));
    System.out.println();
  }

  /**
   * Imprime una tabla con el dia, la ETc por dia, la lluvia por dia,
   * el deficit (diferencia entre el H2O y la ETc) de agua por dia y
   * el deficit acumulado de agua para el metodo calculateIrrigationWaterNeed
   * de la clase WaterMath sobrecargado con la cantidad total de
   * agua de riego de una fecha dada, una coleccion de registros
   * climaticos y una coleccion de registros de riego, siendo todos
   * ellos previos a una fecha dada y pertenecientes a una misma
   * parcela
   * 
   * @param presumedCurrentDate
   * @param givenClimateRecords
   */
  private void printClimateRecordsTestOverloadedCalculateIrrigationWaterNeed(Calendar presumedCurrentDate, Collection<ClimateRecord> givenClimateRecords) {
    System.out.println("Deficit acumulado de agua [mm/dia] de dias previos a la fecha actual (hoy) " + UtilDate.formatDate(presumedCurrentDate)
        + ": " + Math.abs(printTable(presumedCurrentDate, givenClimateRecords)));
    System.out.println();
  }

  /**
   * Imprime por pantalla una tabla que contiene el dia, la
   * ETc por dia, el riego por dia, el deficit de agua por
   * dia y el deficit acumulado de agua creada a partir de
   * un conjunto de registros climaticos previos a una
   * fecha dada
   * 
   * @param presumedCurrentDate
   * @param givenIrrigationRecords
   * @param givenClimateRecords
   */
  private void printIrrigationRecords(Calendar presumedCurrentDate, List<IrrigationRecord> givenIrrigationRecords,
      List<ClimateRecord> givenClimateRecords) {
    int day = 1;
    double differencePerDay = 0.0;
    double accumulatedDeficit = 0.0;
    IrrigationRecord currentIrrigationRecord = null;
    ClimateRecord currentClimateRecord = null;

    System.out.println("     Dia       " + "|" + "    ETc   " + "|" + "  Riego  " + "|" + "  Dif.  " + "|" + "  Def. Acum.  ");
    System.out.println("-----------------------------------------------------------");

    for (int i = 0; i < givenIrrigationRecords.size(); i++) {
      currentIrrigationRecord = givenIrrigationRecords.get(i);
      currentClimateRecord = givenClimateRecords.get(i);

      differencePerDay = calculateDifferencePerDay(currentIrrigationRecord, currentClimateRecord);
      accumulatedDeficit = calculateAccumulatedDeficitPerDay(differencePerDay, accumulatedDeficit);

      System.out.print(" " + day + " (" + UtilDate.formatDate(currentIrrigationRecord.getDate()) + ")");
      System.out.print("  |");
      System.out.print("   " + currentClimateRecord.getEtc());
      System.out.print("   |");
      System.out.print("   " + currentIrrigationRecord.getIrrigationDone());
      System.out.print("    |");
      System.out.print("  " + differencePerDay);
      System.out.print("  |");
      System.out.print("   " + accumulatedDeficit);
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
    System.out.println("Riego [mm/dia]");
    System.out.println("Diferencia (Riego - ETc) [mm/dia]");
    System.out.println("Deficit acumulado [mm/dia]");
    System.out.println();

    System.out.println("Necesidad de agua de riego [mm/dia] de un cultivo en la fecha actual (hoy) " + UtilDate.formatDate(presumedCurrentDate)
        + ": " + Math.abs(accumulatedDeficit));
    System.out.println();
  }

  /**
   * Imprime la descripcion del metodo a probar, el cual en este
   * caso es el metodo calculateIrrigationWaterNeed
   * (Collection<ClimateRecord> previousClimateRecords, Collection<IrrigationRecord> previousIrrigationRecords)
   * de la clase WaterMath
   */
  private void printDescriptionMethodToTest() {
    System.out.println("El metodo calculateIrrigationWater se utiliza para calcular la necesidad de agua de riego [mm/dia] de un cultivo en una fecha");
    System.out.println("dada utilizando como referencia registros climaticos y registros de riego previos a la fecha dada. Esta fecha puede ser la");
    System.out.println("fecha actual (es decir, hoy) o una fecha posterior a la fecha actual. Pero tambien puede ser una fecha del pasado (es decir,");
    System.out.println("anterior a la fecha actual), pero esto no tiene sentido si lo que se busca es determinar la necesidad de agua de riego de un");
    System.out.println("cultivo en la fecha actual o en una fecha posterior a la fecha actual.");
    System.out.println();
    System.out.println("La fecha para la que se calcula la necesidad de agua de riego de un cultivo esta determinada por los registros climaticos y");
    System.out.println("los registros de riego que se seleccionan como previos a una fecha dada, siendo ambos grupos de registros pertenecientes a");
    System.out.println("una parcela dada. Por ejemplo, si se seleccionan los registros climaticos y los registros de riego de una parcela dada previos");
    System.out.println("a la fecha actual (es decir, hoy), la necesidad de agua de riego de un cultivo calculada con estos registros corresponde a la");
    System.out.println("fecha actual. En cambio, si se seleccionan los registros climaticos y los registros de riego de una parcela dada previos a");
    System.out.println("la fecha actual + X dias, donde X > 0, la necesidad de agua de riego de un cultivo calculada con estos registros corresponde");
    System.out.println("a la fecha actual + X dias.");
    System.out.println();
  }

  /**
   * Imprime la descripcion del metodo a probar, el cual en este
   * caso es el metodo calculateIrrigationWaterNeed de la clase
   * WaterMath sobrecargado con la cantidad total de agua de riego
   * de una fecha dada, una coleccion de registros climaticos y una
   * coleccion de registros de riego, siendo todos ellos previos
   * a una fecha dada y pertenecientes a una misma parcela
   */
  private void printDescriptionTestOverloadedCalculateIrrigationWaterNeed() {
    System.out.println("El metodo calculateIrrigationWater sobrecargado con la cantidad total de agua de riego de una fecha dada, una coleccion de registros");
    System.out.println("climaticos y una coleccion de registros de riego, siendo todos ellos previos a una fecha dada y pertenecientes a una misma parcela,");
    System.out.println("se utiliza para calcular la necesidad de agua de riego [mm/dia] de un cultivo en una fecha dada utilizando los parametros con los que");
    System.out.println("esta sobrecargado. La fecha dada puede ser la fecha actual (es decir, hoy) o una fecha posterior a la fecha actual. Pero tambien puede");
    System.out.println("ser una fecha del pasado (es decir, anterior a la fecha actual), pero esto no tiene sentido si lo que se busca es determinar la necesidad");
    System.out.println("de agua de riego de un cultivo en la fecha actual o en una fecha posterior a la fecha actual.");
    System.out.println();
    System.out.println("El metodo calculateIrrigationWater utiliza la cantidad total de agua de riego de una fecha dada y el deficit acumulado de agua de");
    System.out.println("dias previos a una fecha dada para calcular la necesidad de agua de riego de un cultivo en una fecha dada. El deficit acumulado de");
    System.out.println("agua de dias previos a una fecha dada representa la cantidad de agua evaporada en dias previos a una fecha dada que no fue cubierta");
    System.out.println("(satisfecha) y que se debe reponer mediante el riego en una fecha dada, y se calcula a partir de una coleccion de registros climaticos");
    System.out.println("y una coleccion de registros de riego, siendo todos ellos previos a una fecha dada y pertenecientes a una misma parcela.");
    System.out.println();
    System.out.println("- Si la cantidad total de agua de riego de una fecha dada es mayor o igual al deficit acumulado de agua de dias previos a una fecha");
    System.out.println("dada, el metodo calculateIrrigationWater retorna 0. Por lo tanto, la necesidad de agua de riego de un cultivo en una fecha dada es 0.");
    System.out.println("Esto se debe a que si la cantidad total de agua de riego de una fecha dada cubre mayoritariamente o exactamente el deficit acumulado");
    System.out.println("de agua de dias previos a una fecha dada, NO hay una cantidad de agua evaporada en dias previos a una fecha dada que cubrir (satisfacer)");
    System.out.println("mediante el riego en una fecha dada, por lo tanto, la necesidad de agua de riego de un cultivo en una fecha dada es 0.");
    System.out.println();
    System.out.println("- Si la cantidad total de agua de riego de una fecha dada es estricatamente menor al deficit acumulado de agua de dias previos a");
    System.out.println("una fecha dada, el metodo calculateIrrigationWater retorna la diferencia entre el deficit acumulado de agua de dias previos a una fecha");
    System.out.println("y la cantidad total de agua de riego de una fecha dada. Por lo tanto, la necesidad de agua de riego de un cultivo en una fecha dada es");
    System.out.println("el resultado de dicha diferencia. Esta diferencia se debe a que si la cantidad total de agua de riego de una fecha dada NO cubre");
    System.out.println("mayoritariamente o exactamente el deficit acumulado de agua de dias previos a una fecha dada, hay una cantidad de agua evaporada en dias");
    System.out.println("previos a fecha dada que cubrir (satisfacer) mediante el riego en una fecha dada. Esta cantidad se calcula mediante la diferencia entre");
    System.out.println("el deficit acumulado de agua de dias previos a una fecha dada y la cantidad total de agua de riego de una fecha dada, y el resultado de");
    System.out.println("esta diferencia es mayor a 0.");
    System.out.println();
    System.out.println("La fecha para la que se calcula la necesidad de agua de riego de un cultivo esta determinada por los registros climaticos y los registros");
    System.out.println("de riego que se seleccionan como previos a una fecha dada, siendo todos ellos pertenecientes a una misma parcela. Por ejemplo, si se");
    System.out.println("seleccionan los registros climaticos y los registros de riego de una parcela dada previos a la fecha actual (es decir, hoy), la necesidad");
    System.out.println("de agua de riego de un cultivo calculada con estos registros corresponde a la fecha actual. En cambio, si se seleccionan los registros");
    System.out.println("climaticos y los registros de riego de una parcela dada previos a la fecha actual + X dias, donde X > 0, la necesidad de agua de riego");
    System.out.println("de un cultivo calculada con estos registros corresponde a la fecha actual + X dias.");
    System.out.println();
  }

  /**
   * @param givenClimateRecord
   * @return double que representa la diferencia entre la
   * cantidad de agua de lluvia y la ETc, ambas medidas
   * en mm/dia y correspondientes a un mismo dia
   */
  private double calculateDifferencePerDay(ClimateRecord givenClimateRecord) {
    return (givenClimateRecord.getPrecip() - givenClimateRecord.getEtc());
  }

  /**
   * @param givenIrrigationRecord
   * @param givenClimateRecord
   * @return double que representa la diferencia entre la
   * cantidad de agua de riego y la ETc, ambas medidas
   * en mm/dia y correspondientes a un mismo dia
   */
  private double calculateDifferencePerDay(IrrigationRecord givenIrrigationRecord, ClimateRecord givenClimateRecord) {
    return (givenIrrigationRecord.getIrrigationDone() - givenClimateRecord.getEtc());
  }

  /**
   * @param differencePerDay
   * @param accumulatedDeficit
   * 
   * @return double que representa el deficit (falta) acumulado de agua
   * por dia [mm/dia], el cual es el resultado de sumar la diferencia
   * entre la lluvia y la ETc de cada dia. El deficit acumulado de agua
   * representa la necesidad de agua de riego de un cultivo en una fecha
   * dada.
   */
  private double calculateAccumulatedDeficitPerDay(double differencePerDay, double accumulatedDeficit) {
    /*
     * Si la diferencia [mm/dia] entre la cantidad de agua provista
     * (lluvia o riego, o lluvia mas riego) [mm/dia] y la cantidad
     * de agua evaporada [mm/dia] (dada por la ETc, o la ETo si la
     * ETc = 0) es menor a cero significa que toda o parte de la
     * cantidad de agua evaporada no fue cubierta (satisfecha). A
     * esto se lo denomina deficit (falta) de agua para satisfacer
     * la cantidad de agua evaporada. Por lo tanto, se acumula el
     * deficit de agua por dia para determinar la necesidad de agua
     * de riego de un cultivo en una fecha dada.
     */
    if (differencePerDay < 0) {
      accumulatedDeficit = accumulatedDeficit + differencePerDay;
    }

    /*
     * Si la diferencia [mm/dia] entre la cantidad de agua provista
     * (lluvia o riego, o lluvia mas riego) [mm/dia] y la cantidad
     * de agua evaporada [mm/dia] (dada por la ETc, o la ETo si la
     * ETc = 0) es mayor a cero significa que la cantidad de agua
     * evaporada en un dia previo a una fecha dada, fue totalmente
     * cubierta y que hay una cantidad extra de agua [mm/dia].
     * 
     * Si el deficit acumulado de agua [mm/dia] es menor a cero
     * significa que la cantidad de agua evaporada en dias previos
     * a una fecha dada no fue cubierta (satisfecha). Esta condicion
     * representa la situacion en la que hay lugar en el suelo para
     * almacenar agua.
     * 
     * Si ambas condiciones ocurren al mismo tiempo significa que
     * la cantidad extra de agua, resultante de la diferencia entre
     * la cantidad de agua provista (lluvia o riego, o lluvia mas
     * riego) de un dia previo a una fecha dada y la cantidad de
     * agua evaporada (dada por la ETc, o la ETo si la ETc = 0) de
     * un dia previo a una fecha dada, se almacena en el suelo, ya
     * que este tiene lugar para almacenar mas agua, lo cual se
     * debe a que hay un deficit acumulado de agua.
     */
    if (differencePerDay > 0 && accumulatedDeficit < 0) {
      accumulatedDeficit = accumulatedDeficit + differencePerDay;

      /*
       * Si el deficit acumulado de agua [mm/dia] despues de sumarle
       * una cantidad extra de agua [mm/dia], es mayor a cero, significa
       * que el deficit acumulado de agua fue totalmente cubierto (satisfecho).
       * Es decir, se satisfizo la cantidad acumulada de agua evaporada
       * en dias previos a una fecha dada. Por lo tanto, ya no hay una
       * cantidad de agua evaporada que cubrir (satisfacer). En consecuencia,
       * el deficit acumulado de agua es 0.
       */
      if (accumulatedDeficit > 0) {
        accumulatedDeficit = 0;
      }

    }

    return accumulatedDeficit;
  }

  /**
   * Establece la ETc, la lluvia y la fecha de 6 registros
   * climaticos para la pruebta unitaria uno
   */
  private void setClimateRecordsValuesOne() {
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
   * climaticos para la pruebta unitaria dos
   */
  private void setClimateRecordsValuesTwo() {
    climateRecordOne.setEtc(10);
    climateRecordOne.setPrecip(5);
    climateRecordOne.setDate(dayOne);

    climateRecordTwo.setEtc(5);
    climateRecordTwo.setPrecip(5);
    climateRecordTwo.setDate(dayTwo);

    climateRecordThree.setEtc(2);
    climateRecordThree.setPrecip(1);
    climateRecordThree.setDate(dayThree);

    climateRecordFour.setEtc(3);
    climateRecordFour.setPrecip(3);
    climateRecordFour.setDate(dayFour);

    climateRecordFive.setEtc(4);
    climateRecordFive.setPrecip(1);
    climateRecordFive.setDate(dayFive);

    climateRecordSix.setEtc(2);
    climateRecordSix.setPrecip(2);
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
    climateRecordTwo.setPrecip(5);
    climateRecordTwo.setDate(dayTwo);

    climateRecordThree.setEtc(2);
    climateRecordThree.setPrecip(1);
    climateRecordThree.setDate(dayThree);

    climateRecordFour.setEtc(3);
    climateRecordFour.setPrecip(3);
    climateRecordFour.setDate(dayFour);

    climateRecordFive.setEtc(4);
    climateRecordFive.setPrecip(5);
    climateRecordFive.setDate(dayFive);

    climateRecordSix.setEtc(2);
    climateRecordSix.setPrecip(2);
    climateRecordSix.setDate(daySix);
  }

  /**
   * Establece la ETc, la lluvia y la fecha de 6 registros
   * climaticos para la pruebta unitaria cuatro
   */
  private void setClimateRecordsValuesFour() {
    climateRecordOne.setEtc(10);
    climateRecordOne.setPrecip(15);
    climateRecordOne.setDate(dayOne);

    climateRecordTwo.setEtc(5);
    climateRecordTwo.setPrecip(6);
    climateRecordTwo.setDate(dayTwo);

    climateRecordThree.setEtc(2);
    climateRecordThree.setPrecip(3);
    climateRecordThree.setDate(dayThree);

    climateRecordFour.setEtc(3);
    climateRecordFour.setPrecip(4);
    climateRecordFour.setDate(dayFour);

    climateRecordFive.setEtc(1);
    climateRecordFive.setPrecip(2);
    climateRecordFive.setDate(dayFive);

    climateRecordSix.setEtc(2.5);
    climateRecordSix.setPrecip(5);
    climateRecordSix.setDate(daySix);
  }

  /**
   * Establece la ETc, la lluvia y la fecha de 6 registros
   * climaticos para la pruebta unitaria cinco
   */
  private void setClimateRecordsValuesFive() {
    climateRecordOne.setEtc(10);
    climateRecordOne.setPrecip(10);
    climateRecordOne.setDate(dayOne);

    climateRecordTwo.setEtc(5);
    climateRecordTwo.setPrecip(5);
    climateRecordTwo.setDate(dayTwo);

    climateRecordThree.setEtc(2);
    climateRecordThree.setPrecip(2);
    climateRecordThree.setDate(dayThree);

    climateRecordFour.setEtc(3);
    climateRecordFour.setPrecip(3);
    climateRecordFour.setDate(dayFour);

    climateRecordFive.setEtc(1);
    climateRecordFive.setPrecip(1);
    climateRecordFive.setDate(dayFive);

    climateRecordSix.setEtc(2.5);
    climateRecordSix.setPrecip(2.5);
    climateRecordSix.setDate(daySix);
  }

  /**
   * Establece la ETc, la lluvia y la fecha de 6 registros
   * climaticos para la pruebta unitaria seis
   */
  private void setClimateRecordsValuesSix() {
    climateRecordOne.setEtc(10);
    climateRecordOne.setPrecip(10);
    climateRecordOne.setDate(dayOne);

    climateRecordTwo.setEtc(5);
    climateRecordTwo.setPrecip(10);
    climateRecordTwo.setDate(dayTwo);

    climateRecordThree.setEtc(2);
    climateRecordThree.setPrecip(10);
    climateRecordThree.setDate(dayThree);

    climateRecordFour.setEtc(3);
    climateRecordFour.setPrecip(0);
    climateRecordFour.setDate(dayFour);

    climateRecordFive.setEtc(4);
    climateRecordFive.setPrecip(1);
    climateRecordFive.setDate(dayFive);

    climateRecordSix.setEtc(2);
    climateRecordSix.setPrecip(4);
    climateRecordSix.setDate(daySix);
  }

  /**
   * Establece la ETc, la lluvia y la fecha de 6 registros
   * climaticos para la pruebta unitaria siete
   */
  private void setClimateRecordsValuesSeven() {
    climateRecordOne.setEtc(15);
    climateRecordOne.setPrecip(10);
    climateRecordOne.setDate(dayOne);

    climateRecordTwo.setEtc(12);
    climateRecordTwo.setPrecip(13);
    climateRecordTwo.setDate(dayTwo);

    climateRecordThree.setEtc(6);
    climateRecordThree.setPrecip(6);
    climateRecordThree.setDate(dayThree);

    climateRecordFour.setEtc(2);
    climateRecordFour.setPrecip(1);
    climateRecordFour.setDate(dayFour);

    climateRecordFive.setEtc(4);
    climateRecordFive.setPrecip(2);
    climateRecordFive.setDate(dayFive);

    climateRecordSix.setEtc(7);
    climateRecordSix.setPrecip(7);
    climateRecordSix.setDate(daySix);
  }

  /**
   * Establece la ETc, la lluvia y la fecha de 6 registros
   * climaticos para la pruebta unitaria ocho
   */
  private void setClimateRecordsValuesEight() {
    climateRecordOne.setEtc(15);
    climateRecordOne.setPrecip(15);
    climateRecordOne.setDate(dayOne);

    climateRecordTwo.setEtc(12);
    climateRecordTwo.setPrecip(16);
    climateRecordTwo.setDate(dayTwo);

    climateRecordThree.setEtc(6);
    climateRecordThree.setPrecip(8);
    climateRecordThree.setDate(dayThree);

    climateRecordFour.setEtc(2);
    climateRecordFour.setPrecip(0);
    climateRecordFour.setDate(dayFour);

    climateRecordFive.setEtc(4);
    climateRecordFive.setPrecip(1);
    climateRecordFive.setDate(dayFive);

    climateRecordSix.setEtc(5);
    climateRecordSix.setPrecip(6);
    climateRecordSix.setDate(daySix);
  }

  /**
   * Establece la ETc, la lluvia y la fecha de 6 registros
   * climaticos para la pruebta unitaria nueve
   */
  private void setClimateRecordsValuesNine() {
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
    climateRecordSix.setPrecip(14);
    climateRecordSix.setDate(daySix);
  }

  /**
   * Establece la ETc, la lluvia y la fecha de 6 registros
   * climaticos para la pruebta unitaria diez
   */
  private void setClimateRecordsValuesTen() {
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
   * climaticos para la pruebta unitaria once
   */
  private void setClimateRecordsValuesEleven() {
    climateRecordOne.setEtc(15);
    climateRecordOne.setPrecip(10);
    climateRecordOne.setDate(dayOne);

    climateRecordTwo.setEtc(5);
    climateRecordTwo.setPrecip(10);
    climateRecordTwo.setDate(dayTwo);

    climateRecordThree.setEtc(8);
    climateRecordThree.setPrecip(4);
    climateRecordThree.setDate(dayThree);

    climateRecordFour.setEtc(2);
    climateRecordFour.setPrecip(6);
    climateRecordFour.setDate(dayFour);

    climateRecordFive.setEtc(8);
    climateRecordFive.setPrecip(2);
    climateRecordFive.setDate(dayFive);

    climateRecordSix.setEtc(5);
    climateRecordSix.setPrecip(10);
    climateRecordSix.setDate(daySix);
  }

  /**
   * Establece la ETc, la lluvia y la fecha de 6 registros
   * climaticos para la pruebta unitaria trece
   */
  private void setClimateRecordsValuesThirteen() {
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
   * climaticos para la pruebta unitaria catorce
   */
  private void setClimateRecordsValuesFourteen() {
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
   * climaticos para la pruebta unitaria quince
   */
  private void setClimateRecordsValuesFifteen() {
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
   * climaticos para la pruebta unitaria dieciseis
   */
  private void setClimateRecordsValuesSixteen() {
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
   * climaticos para la pruebta unitaria diecisiete
   */
  private void setClimateRecordsValuesSeventeen() {
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
