import static org.junit.Assert.*;

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

public class WaterMathTest {

  private static Collection<IrrigationRecord> irrigationRecords;
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
    irrigationRecords = new ArrayList<>();
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
  public void testOneCalculateIrrigationWater() {
    System.out.println("************************************** Prueba uno del metodo calculateIrrigationWater ***************************************");
    printDescriptionMethodToTest();

    System.out.println("# Descripcion de la prueba unitaria");
    System.out.println("Para esta prueba se utilizan 6 registros climaticos previos a la fecha actual " + UtilDate.formatDate(presumedCurrentDate) + ".");
    System.out.println("Suponemos que la fecha actual es " + UtilDate.formatDate(presumedCurrentDate));
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
    double result = WaterMath.calculateIrrigationWaterNeed(climateRecords, irrigationRecords);

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
    System.out.println("Suponemos que la fecha actual es " + UtilDate.formatDate(presumedCurrentDate));
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
    double result = WaterMath.calculateIrrigationWaterNeed(climateRecords, irrigationRecords);

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
    System.out.println("Suponemos que la fecha actual es " + UtilDate.formatDate(presumedCurrentDate));
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
    double result = WaterMath.calculateIrrigationWaterNeed(climateRecords, irrigationRecords);

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
    System.out.println("Suponemos que la fecha actual es " + UtilDate.formatDate(presumedCurrentDate));
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
    double result = WaterMath.calculateIrrigationWaterNeed(climateRecords, irrigationRecords);

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
    System.out.println("Suponemos que la fecha actual es " + UtilDate.formatDate(presumedCurrentDate));
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
    double result = WaterMath.calculateIrrigationWaterNeed(climateRecords, irrigationRecords);

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
    System.out.println("Suponemos que la fecha actual es " + UtilDate.formatDate(presumedCurrentDate));
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
    double result = WaterMath.calculateIrrigationWaterNeed(climateRecords, irrigationRecords);

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
    System.out.println("Suponemos que la fecha actual es " + UtilDate.formatDate(presumedCurrentDate));
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
    double result = WaterMath.calculateIrrigationWaterNeed(climateRecords, irrigationRecords);

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
    System.out.println("Suponemos que la fecha actual es " + UtilDate.formatDate(presumedCurrentDate));
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
    double result = WaterMath.calculateIrrigationWaterNeed(climateRecords, irrigationRecords);

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
    System.out.println("Suponemos que la fecha actual es " + UtilDate.formatDate(presumedCurrentDate));
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
    double result = WaterMath.calculateIrrigationWaterNeed(climateRecords, irrigationRecords);

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
    System.out.println("Suponemos que la fecha actual es " + UtilDate.formatDate(presumedCurrentDate));
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
    double result = WaterMath.calculateIrrigationWaterNeed(climateRecords, irrigationRecords);

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
    System.out.println("Suponemos que la fecha actual es " + UtilDate.formatDate(presumedCurrentDate));
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
    double result = WaterMath.calculateIrrigationWaterNeed(climateRecords, irrigationRecords);

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
    System.out.println("Suponemos que la fecha actual es " + UtilDate.formatDate(presumedCurrentDate));
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

  /**
   * Imprime una tabla con el dia, la ETc, la lluvia, el deficit
   * (diferencia entre el H2O y la ETc) y el deficit acumulado
   * 
   * @param presumedCurrentDate
   * @param givenClimateRecords
   */
  private void printClimateRecords(Calendar presumedCurrentDate, Collection<ClimateRecord> givenClimateRecords) {
    int day = 1;
    double differencePerDay = 0.0;
    double accumulatedDeficit = 0.0;

    System.out.println("     Dia       " + "|" + "    ETc   " + "|" + "  Lluvia  " + "|" + "  Dif.  " + "|" + "  Def. Acum.  ");
    System.out.println("-----------------------------------------------------------");

    for (ClimateRecord currentClimateRecord : givenClimateRecords) {
      differencePerDay = calculateDifferencePerDay(currentClimateRecord);
      accumulatedDeficit = calculateAccumulatedDeficit(differencePerDay, accumulatedDeficit);

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
    System.out.println("fecha actual. En cambios, si se seleccionan los registros climaticos y los registros de riego de una parcela dada previos a");
    System.out.println("la fecha actual + X dias, donde X > 0, la necesidad de agua de riego de un cultivo calculada con estos registros corresponde");
    System.out.println("a la fecha actual + X dias.");
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
   * @param differencePerDay
   * @param accumulatedDeficit
   * 
   * @return double que representa el deficit (falta) acumulado de agua
   * [mm/dia], el cual es el resultado de sumar la diferencia entre la
   * lluvia y la ETc de cada dia. El deficit acumulado de agua representa
   * la necesidad de agua de riego de un cultivo en una fecha dada.
   */
  private double calculateAccumulatedDeficit(double differencePerDay, double accumulatedDeficit) {
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

}
