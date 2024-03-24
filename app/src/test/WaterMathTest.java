import static org.junit.Assert.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.Calendar;
import java.util.ArrayList;
import java.util.Collection;
import java.lang.Math;
import model.ClimateRecord;
import model.IrrigationRecord;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import irrigation.WaterMath;
import util.UtilDate;
import stateless.ClimateRecordServiceBean;
import stateless.IrrigationRecordServiceBean;

public class WaterMathTest {

  private static EntityManager entityManager;
  private static EntityManagerFactory entityManagerFactory;
  private static ClimateRecordServiceBean climateRecordService;
  private static IrrigationRecordServiceBean irrigationRecordService;

  private static Collection<IrrigationRecord> zeroIrrigationRecords;
  private static Collection<ClimateRecord> climateRecords;

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

    climateRecordService = new ClimateRecordServiceBean();
    climateRecordService.setEntityManager(entityManager);

    irrigationRecordService = new IrrigationRecordServiceBean();
    irrigationRecordService.setEntityManager(entityManager);

    /*
     * Esta coleccion es unicamente para aquellas pruebas unitarias
     * del metodo calculateAccumulatedWaterDeficitPerDay de la clase
     * WaterMath en las que se busca demostrar el correcto funcionamiento
     * del mismo sin hacer uso del agua de riego
     */
    zeroIrrigationRecords = new ArrayList<>();
    climateRecords = new ArrayList<>();

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
  public void testOneCalculateAccumulatedWaterDeficitPerDay() {
    System.out.println("************************************** Prueba uno del metodo calculateAccumulatedWaterDeficitPerDay ***************************************");
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

    printWaterBalanceTable(presumedCurrentDate, climateRecords);

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");

    double expectedResult = 14.0;
    double result = Math.abs(WaterMath.calculateAccumulatedWaterDeficitPerDay(climateRecords, zeroIrrigationRecords));

    System.out.println("* Valor esperado (nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateAccumulatedWaterDeficitPerDay");
    System.out.println("(nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testTwoCalculateAccumulatedWaterDeficitPerDay() {
    System.out.println("************************************** Prueba dos del metodo calculateAccumulatedWaterDeficitPerDay ***************************************");
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

    printWaterBalanceTable(presumedCurrentDate, climateRecords);

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");

    double expectedResult = 9.0;
    double result = Math.abs(WaterMath.calculateAccumulatedWaterDeficitPerDay(climateRecords, zeroIrrigationRecords));

    System.out.println("* Valor esperado (nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateAccumulatedWaterDeficitPerDay");
    System.out.println("(nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testThreeCalculateAccumulatedWaterDeficitPerDay() {
    System.out.println("************************************** Prueba tres del metodo calculateAccumulatedWaterDeficitPerDay ***************************************");
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

    printWaterBalanceTable(presumedCurrentDate, climateRecords);

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");

    double expectedResult = 5.0;
    double result = Math.abs(WaterMath.calculateAccumulatedWaterDeficitPerDay(climateRecords, zeroIrrigationRecords));

    System.out.println("* Valor esperado (nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateAccumulatedWaterDeficitPerDay");
    System.out.println("(nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testFourCalculateAccumulatedWaterDeficitPerDay() {
    System.out.println("************************************** Prueba cuatro del metodo calculateAccumulatedWaterDeficitPerDay ***************************************");
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

    printWaterBalanceTable(presumedCurrentDate, climateRecords);

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");

    double expectedResult = 0.0;
    double result = Math.abs(WaterMath.calculateAccumulatedWaterDeficitPerDay(climateRecords, zeroIrrigationRecords));

    System.out.println("* Valor esperado (nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateAccumulatedWaterDeficitPerDay");
    System.out.println("(nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testFiveCalculateAccumulatedWaterDeficitPerDay() {
    System.out.println("************************************** Prueba cinco del metodo calculateAccumulatedWaterDeficitPerDay ***************************************");
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

    printWaterBalanceTable(presumedCurrentDate, climateRecords);

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");

    double expectedResult = 0.0;
    double result = Math.abs(WaterMath.calculateAccumulatedWaterDeficitPerDay(climateRecords, zeroIrrigationRecords));

    System.out.println("* Valor esperado (nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateAccumulatedWaterDeficitPerDay");
    System.out.println("(nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testSixCalculateAccumulatedWaterDeficitPerDay() {
    System.out.println("************************************** Prueba seis del metodo calculateAccumulatedWaterDeficitPerDay ***************************************");
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

    printWaterBalanceTable(presumedCurrentDate, climateRecords);

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");

    double expectedResult = 4.0;
    double result = Math.abs(WaterMath.calculateAccumulatedWaterDeficitPerDay(climateRecords, zeroIrrigationRecords));

    System.out.println("* Valor esperado (nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateAccumulatedWaterDeficitPerDay");
    System.out.println("(nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testSevenCalculateAccumulatedWaterDeficitPerDay() {
    System.out.println("************************************** Prueba siete del metodo calculateAccumulatedWaterDeficitPerDay ***************************************");
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

    printWaterBalanceTable(presumedCurrentDate, climateRecords);

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");

    double expectedResult = 7.0;
    double result = Math.abs(WaterMath.calculateAccumulatedWaterDeficitPerDay(climateRecords, zeroIrrigationRecords));

    System.out.println("* Valor esperado (nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateAccumulatedWaterDeficitPerDay");
    System.out.println("(nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testEightCalculateAccumulatedWaterDeficitPerDay() {
    System.out.println("************************************** Prueba ocho del metodo calculateAccumulatedWaterDeficitPerDay ***************************************");
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

    printWaterBalanceTable(presumedCurrentDate, climateRecords);

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");

    double expectedResult = 4.0;
    double result = Math.abs(WaterMath.calculateAccumulatedWaterDeficitPerDay(climateRecords, zeroIrrigationRecords));

    System.out.println("* Valor esperado (nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateAccumulatedWaterDeficitPerDay");
    System.out.println("(nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testNineCalculateAccumulatedWaterDeficitPerDay() {
    System.out.println("************************************** Prueba nueve del metodo calculateAccumulatedWaterDeficitPerDay ***************************************");
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

    printWaterBalanceTable(presumedCurrentDate, climateRecords);

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");

    double expectedResult = 0.0;
    double result = Math.abs(WaterMath.calculateAccumulatedWaterDeficitPerDay(climateRecords, zeroIrrigationRecords));

    System.out.println("* Valor esperado (nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateAccumulatedWaterDeficitPerDay");
    System.out.println("(nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testTenCalculateAccumulatedWaterDeficitPerDay() {
    System.out.println("************************************** Prueba diez del metodo calculateAccumulatedWaterDeficitPerDay ***************************************");
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

    printWaterBalanceTable(presumedCurrentDate, climateRecords);

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");

    double expectedResult = 0.0;
    double result = Math.abs(WaterMath.calculateAccumulatedWaterDeficitPerDay(climateRecords, zeroIrrigationRecords));

    System.out.println("* Valor esperado (nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateAccumulatedWaterDeficitPerDay");
    System.out.println("(nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testElevenCalculateAccumulatedWaterDeficitPerDay() {
    System.out.println("************************************** Prueba once del metodo calculateAccumulatedWaterDeficitPerDay ***************************************");
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

    printWaterBalanceTable(presumedCurrentDate, climateRecords);

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");

    double expectedResult = 1.0;
    double result = Math.abs(WaterMath.calculateAccumulatedWaterDeficitPerDay(climateRecords, zeroIrrigationRecords));

    System.out.println("* Valor esperado (nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateAccumulatedWaterDeficitPerDay");
    System.out.println("(nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testTwelveCalculateAccumulatedWaterDeficitPerDay() {
    System.out.println("************************************** Prueba doce del metodo calculateAccumulatedWaterDeficitPerDay ***************************************");
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
    double result = Math.abs(WaterMath.calculateAccumulatedWaterDeficitPerDay(yesterdayClimateRecords, yesterdayIrrigationRecords));

    System.out.println("* Valor esperado (nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateAccumulatedWaterDeficitPerDay");
    System.out.println("(nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  /*
   * ***************************************************************************************************************
   * A partir de aqui comienzan las pruebas unitarias del metodo calculateWaterDeficitPerDay() de la clase WaterMath
   * ***************************************************************************************************************
   */

  @Test
  public void testOneCalculateWaterDeficitPerDay() {
    System.out.println("************************************** Prueba uno del metodo calculateWaterDeficitPerDay ***************************************");
    printDescriptionCalculateWaterDeficitPerDay();
    System.out.println();

    /*
     * Datos de prueba
     */
    double etc = 10;
    double precipitation = 2.5;
    double totalIrrigationWater = 2.5;

    System.out.println("# Datos de prueba");
    System.out.println("ETc = " + etc);
    System.out.println("Precipitacion = " + precipitation);
    System.out.println("Cantidad total de agua de riego = " + totalIrrigationWater);
    System.out.println();

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");

    double expectedResult = -5;
    double result = WaterMath.calculateWaterDeficitPerDay(etc, precipitation, totalIrrigationWater);

    System.out.println("* Valor esperado (deficit de humedad por dia [mm/dia]): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateWaterDeficitPerDay(): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testTwoCalculateWaterDeficitPerDay() {
    System.out.println("************************************** Prueba dos del metodo calculateWaterDeficitPerDay ***************************************");
    printDescriptionCalculateWaterDeficitPerDay();
    System.out.println();

    /*
     * Datos de prueba
     */
    double etc = 5;
    double precipitation = 5;
    double totalIrrigationWater = 5;

    System.out.println("# Datos de prueba");
    System.out.println("ETc = " + etc);
    System.out.println("Precipitacion = " + precipitation);
    System.out.println("Cantidad total de agua de riego = " + totalIrrigationWater);
    System.out.println();

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");

    double expectedResult = 5;
    double result = WaterMath.calculateWaterDeficitPerDay(etc, precipitation, totalIrrigationWater);

    System.out.println("* Valor esperado (deficit de humedad por dia [mm/dia]): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateWaterDeficitPerDay(): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testThreeCalculateWaterDeficitPerDay() {
    System.out.println("************************************** Prueba tres del metodo calculateWaterDeficitPerDay ***************************************");
    printDescriptionCalculateWaterDeficitPerDay();
    System.out.println();

    /*
     * Datos de prueba
     */
    double etc = 10;
    double precipitation = 5;
    double totalIrrigationWater = 5;

    System.out.println("# Datos de prueba");
    System.out.println("ETc = " + etc);
    System.out.println("Precipitacion = " + precipitation);
    System.out.println("Cantidad total de agua de riego = " + totalIrrigationWater);
    System.out.println();

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");

    double expectedResult = 0;
    double result = WaterMath.calculateWaterDeficitPerDay(etc, precipitation, totalIrrigationWater);

    System.out.println("* Valor esperado (deficit de humedad por dia [mm/dia]): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateWaterDeficitPerDay(): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  private void printDescriptionCalculateWaterDeficitPerDay() {
    System.out.println("# Descripcion del metodo a probar");
    System.out.println("El metodo calculateWaterDeficitPerDay() de la clase WaterMath calcula el deficit de humedad por dia mediante la ETc, la");
    System.out.println("precipitacion y la cantidad total de agua de riego, debiendo ser estos tres valores de una misma fecha.");
    System.out.println();
    System.out.println("Si la ETc es estrictamente mayor a la suma entre la precipitacion y la cantidad total de agua de riego, el resultado devuelto");
    System.out.println("por el metodo calculateWaterDeficitPerDay() es negativo. En cambio, si la suma entre la precipitacion y la cantidad total de");
    System.out.println("agua riego es estrictamente mayor a la ETc, el resultado devuelto es positivo. Si la ETc es igual a la suma entre la precipitacion");
    System.out.println("y la cantidad total de agua de riego, el resultado es 0.");
  }

  @AfterClass
  public static void postTest() {
    // Cierra las conexiones
    entityManager.close();
    entityManagerFactory.close();
  }

  /**
   * Imprime una tabla que contiene el dia, la ETc por dia, la
   * lluvia por dia, el deficit de agua por dia y el acumulado del
   * deficit de agua por dia de dias previos a una fecha a partir
   * de un conjunto de registros climaticos previos a una fecha
   * 
   * @param presumedCurrentDate
   * @param givenClimateRecords
   */
  private void printWaterBalanceTable(Calendar presumedCurrentDate, Collection<ClimateRecord> givenClimateRecords) {
    int day = 1;
    double differencePerDay = 0.0;
    double accumulatedWaterDeficitPerDay = 0.0;

    System.out.println("     Dia       " + "|" + "    ETc   " + "|" + "  Lluvia  " + "|" + "  Dif.  " + "|" + "  Acum. Def.  ");
    System.out.println("-----------------------------------------------------------");

    for (ClimateRecord currentClimateRecord : givenClimateRecords) {
      differencePerDay = currentClimateRecord.getPrecip() - currentClimateRecord.getEtc();
      accumulatedWaterDeficitPerDay = WaterMath.accumulateWaterDeficitPerDay(differencePerDay, accumulatedWaterDeficitPerDay);

      System.out.print(" " + day + " (" + UtilDate.formatDate(currentClimateRecord.getDate()) + ")");
      System.out.print("  |");
      System.out.print("   " + currentClimateRecord.getEtc());
      System.out.print("   |");
      System.out.print("   " + currentClimateRecord.getPrecip());
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
    System.out.println("Lluvia [mm/dia]");
    System.out.println("Diferencia (Lluvia - ETc) [mm/dia]");
    System.out.println("Acumulado del deficit de agua por dia de dias previos a una fecha [mm/dia]");
    System.out.println();
  }

  /**
   * Imprime la descripcion del metodo a probar, el cual en este
   * caso es el metodo calculateAccumulatedWaterDeficitPerDay de
   * la clase WaterMath
   */
  private void printDescriptionMethodToTest() {
    System.out.println("El metodo calculateAccumulatedWaterDeficitPerDay de la clase WaterMath calcula el acumulado del deficit de agua por dia de dias previos");
    System.out.println("a una fecha [mm/dia]. Si este metodo es invocado con una coleccion de registros climaticos y una coleccion de registros de riego previos");
    System.out.println("a una fecha pertenecientes a una misma parcela que NO tiene un cultivo sembrado en una fecha, el valor devuelto por el mismo representa");
    System.out.println("el acumulado del deficit de agua por dia de dias previos a una fecha [mm/dia] de una parcela en una fecha. En cambio, si es invocado con");
    System.out.println("una coleccion de registros climaticos y una coleccion de registros de riego previos a una fecha pertenecientes a una misma parcela que tiene");
    System.out.println("un cultivo sembrado en una fecha, el valor devuelto por el mismo representa la necesidad de agua de riego de un cultivo en una fecha [mm/dia].");
    System.out.println("En cada una de las pruebas unitarias de esta clase de pruebas unitarias suponemos que dicho metodo es invocado con una coleccion de registros");
    System.out.println("climaticos y una coleccion de registros de riego previos a una fecha y pertenecientes a una misma parcela que tiene un cultivo sembrado en una");
    System.out.println("fecha con el fin de demostrar que calcula correctamente la necesidad de agua de riego de un cultivo en una fecha [mm/dia].");
    System.out.println();
    System.out.println("La fecha para la que se calcula la necesidad de agua de riego de un cultivo esta determinada por los registros climaticos y los registros");
    System.out.println("de riego que se seleccionan como previos a una fecha, debiendo ser ambos grupos de registros pertenecientes a una misma parcela. Por ejemplo,");
    System.out.println("si se seleccionan los registros climaticos y los registros de riego de una parcela previos a la fecha actual (es decir, hoy), la necesidad");
    System.out.println("de agua de riego de un cultivo calculada con estos registros corresponde a la fecha actual. En cambio, si se seleccionan los registros");
    System.out.println("climaticos y los registros de riego de una parcela previos a la fecha actual + X dias, donde X > 0, la necesidad de agua de riego de un");
    System.out.println("cultivo calculada con estos registros corresponde a la fecha actual + X dias.");
    System.out.println();
  }

  /**
   * Establece la ETc, la lluvia y la fecha de 6 registros
   * climaticos para la prueba unitaria uno
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
   * climaticos para la prueba unitaria dos
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
   * climaticos para la prueba unitaria tres
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
   * climaticos para la prueba unitaria cuatro
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
   * climaticos para la prueba unitaria cinco
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
   * climaticos para la prueba unitaria seis
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
   * climaticos para la prueba unitaria siete
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
   * climaticos para la prueba unitaria ocho
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
   * climaticos para la prueba unitaria nueve
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
   * climaticos para la prueba unitaria diez
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
   * climaticos para la prueba unitaria once
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

}