import static org.junit.Assert.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.Calendar;
import java.util.ArrayList;
import java.util.Collection;
import model.ClimateRecord;
import model.IrrigationRecord;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import irrigation.WaterMath;
import irrigation.WaterNeedWit;
import util.UtilDate;

public class WaterNeedWitTest {

  private static Collection<ClimateRecord> testClimateRecords;
  private static Collection<IrrigationRecord> testIrrigationRecords;
  private static Collection<IrrigationRecord> zeroIrrigationRecords;

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

  private static IrrigationRecord irrigationRecordOne;
  private static IrrigationRecord irrigationRecordTwo;
  private static IrrigationRecord irrigationRecordThree;
  private static IrrigationRecord irrigationRecordFour;
  private static IrrigationRecord irrigationRecordFive;
  private static IrrigationRecord irrigationRecordSix;

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
    zeroIrrigationRecords = new ArrayList<>();
    testClimateRecords = new ArrayList<>();
    testIrrigationRecords = new ArrayList<>();

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

    /*
     * Presunta fecha actual para las pruebas unitarias
     */
    presumedCurrentDate = Calendar.getInstance();
    presumedCurrentDate.set(Calendar.YEAR, 2023);
    presumedCurrentDate.set(Calendar.MONTH, JANUARY);
    presumedCurrentDate.set(Calendar.DAY_OF_MONTH, 7);

    /*
     * Creacion de registros climaticos de prueba
     */
    climateRecordOne = new ClimateRecord();
    climateRecordTwo = new ClimateRecord();
    climateRecordThree = new ClimateRecord();
    climateRecordFour = new ClimateRecord();
    climateRecordFive = new ClimateRecord();
    climateRecordSix = new ClimateRecord();

    climateRecordOne.setDate(dayOne);
    climateRecordTwo.setDate(dayTwo);
    climateRecordThree.setDate(dayThree);
    climateRecordFour.setDate(dayFour);
    climateRecordFive.setDate(dayFive);
    climateRecordSix.setDate(daySix);

    testClimateRecords.add(climateRecordOne);
    testClimateRecords.add(climateRecordTwo);
    testClimateRecords.add(climateRecordThree);
    testClimateRecords.add(climateRecordFour);
    testClimateRecords.add(climateRecordFive);
    testClimateRecords.add(climateRecordSix);

    /*
     * Creacion de registros de riego de prueba
     */
    irrigationRecordOne = new IrrigationRecord();
    irrigationRecordTwo = new IrrigationRecord();
    irrigationRecordThree = new IrrigationRecord();
    irrigationRecordFour = new IrrigationRecord();
    irrigationRecordFive = new IrrigationRecord();
    irrigationRecordSix = new IrrigationRecord();

    irrigationRecordOne.setDate(dayOne);
    irrigationRecordTwo.setDate(dayTwo);
    irrigationRecordThree.setDate(dayThree);
    irrigationRecordFour.setDate(dayFour);
    irrigationRecordFive.setDate(dayFive);
    irrigationRecordSix.setDate(daySix);

    testIrrigationRecords.add(irrigationRecordOne);
    testIrrigationRecords.add(irrigationRecordTwo);
    testIrrigationRecords.add(irrigationRecordThree);
    testIrrigationRecords.add(irrigationRecordFour);
    testIrrigationRecords.add(irrigationRecordFive);
    testIrrigationRecords.add(irrigationRecordSix);
  }

  /*
   * Las pruebas unitarias estan escritas en el siguiente orden:
   * 1. Registros climaticos previos a una fecha con precipitacion == 0, registros de riego previos a una fecha con riego realizado == 0
   * y cantidad total de agua de riego de una fecha == 0.
   * 
   * 2. Registros climaticos previos a una fecha con precipitacion > 0, registros de riego previos a una fecha con riego realizado == 0
   * y cantidad total de agua de riego de una fecha == 0.
   * 
   * 3. Registros climaticos previos a una fecha con precipitacion == 0, registros de riego previos a una fecha con riego realizado > 0
   * y cantidad total de agua de riego de una fecha == 0.
   * 
   * 4. Registros climaticos previos a una fecha con precipitacion > 0, registros de riego previos a una fecha con riego realizado > 0
   * y cantidad total de agua de riego de una fecha == 0.
   * 
   * 5. Registros climaticos previos a una fecha con precipitacion == 0, registros de riego previos a una fecha con riego realizado == 0
   * y cantidad total de agua de riego de una fecha > 0.
   * 
   * 6. Registros climaticos previos a una fecha con precipitacion > 0, registros de riego previos a una fecha con riego realizado == 0
   * y cantidad total de agua de riego de una fecha > 0.
   * 
   * 7. Registros climaticos previos a una fecha con precipitacion == 0, registros de riego previos a una fecha con riego realizado > 0
   * y cantidad total de agua de riego de una fecha > 0.
   * 
   * 8. Registros climaticos previos a una fecha con precipitacion > 0, registros de riego previos a una fecha con riego realizado > 0
   * y cantidad total de agua de riego de una fecha > 0.
   * 
   * 9. Los registros climaticos previos a una fecha pueden tener una precipitacion mayor o igual a 0, los registros de riego previos
   * a una fecha pueden tener un riego realizado mayor o igual a 0 y la cantidad total de agua de riego de una fecha puede ser mayor
   * o igual a 0, pero la suma entre el acumulado del deficit de agua por dia de dias previos a una fecha y la cantidad total de agua
   * de riego de una fecha es mayor o igual a 0.
   */

  @Test
  public void testOneCalculateIrrigationWaterNeed() {
    System.out.println("************************************** Prueba uno del metodo calculateIrrigationWaterNeed ***************************************");
    printDescriptionMethodToTest();

    System.out.println("# Descripcion de la prueba unitaria");

    /*
     * Establece los datos de los registros para
     * esta prueba
     */
    setRecordsValuesOne();

    System.out.println("Los datos con los que se calculara la necesidad de agua de riego de un cultivo en la fecha actual (" + UtilDate.formatDate(presumedCurrentDate) + ") son los siguientes:");
    System.out.println();

    printWaterBalanceTable(presumedCurrentDate, testClimateRecords, zeroIrrigationRecords);

    double totalIrrigationWaterCurrentDate = 0.0;

    printAccumulatedAmountRainWater(presumedCurrentDate, testClimateRecords);
    printAccumulatedAmountIrrigationWater(presumedCurrentDate, zeroIrrigationRecords);
    System.out.println();
    printAccumulatedWaterDeficitPerDay(presumedCurrentDate, testClimateRecords, zeroIrrigationRecords);
    printTotalIrrigationWaterCurrentDate(presumedCurrentDate, totalIrrigationWaterCurrentDate);

    System.out.println();
    printMessageSituationOne();
    System.out.println();

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");

    double expectedResult = 8.5;
    double result = WaterNeedWit.calculateIrrigationWaterNeed(totalIrrigationWaterCurrentDate, testClimateRecords, zeroIrrigationRecords);

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
     * Establece los datos de los registros para
     * esta prueba
     */
    setRecordsValuesTwo();

    System.out.println("Los datos con los que se calculara la necesidad de agua de riego de un cultivo en la fecha actual (" + UtilDate.formatDate(presumedCurrentDate) + ") son los siguientes:");
    System.out.println();

    printWaterBalanceTable(presumedCurrentDate, testClimateRecords, zeroIrrigationRecords);

    double totalIrrigationWaterCurrentDate = 0.0;

    printAccumulatedAmountRainWater(presumedCurrentDate, testClimateRecords);
    printAccumulatedAmountIrrigationWater(presumedCurrentDate, zeroIrrigationRecords);
    System.out.println();
    printAccumulatedWaterDeficitPerDay(presumedCurrentDate, testClimateRecords, zeroIrrigationRecords);
    printTotalIrrigationWaterCurrentDate(presumedCurrentDate, totalIrrigationWaterCurrentDate);

    System.out.println();
    printMessageSituationOne();
    System.out.println();

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");

    double expectedResult = 2;
    double result = WaterNeedWit.calculateIrrigationWaterNeed(totalIrrigationWaterCurrentDate, testClimateRecords, zeroIrrigationRecords);

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
     * Establece los datos de los registros para
     * esta prueba
     */
    setRecordsValuesThree();

    System.out.println("Los datos con los que se calculara la necesidad de agua de riego de un cultivo en la fecha actual (" + UtilDate.formatDate(presumedCurrentDate) + ") son los siguientes:");
    System.out.println();

    printWaterBalanceTable(presumedCurrentDate, testClimateRecords, testIrrigationRecords);

    double totalIrrigationWaterCurrentDate = 0.0;

    printAccumulatedAmountRainWater(presumedCurrentDate, testClimateRecords);
    printAccumulatedAmountIrrigationWater(presumedCurrentDate, testIrrigationRecords);
    System.out.println();
    printAccumulatedWaterDeficitPerDay(presumedCurrentDate, testClimateRecords, testIrrigationRecords);
    printTotalIrrigationWaterCurrentDate(presumedCurrentDate, totalIrrigationWaterCurrentDate);

    System.out.println();
    printMessageSituationOne();
    System.out.println();

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");

    double expectedResult = 2;
    double result = WaterNeedWit.calculateIrrigationWaterNeed(totalIrrigationWaterCurrentDate, testClimateRecords, testIrrigationRecords);

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
     * Establece los datos de los registros para
     * esta prueba
     */
    setRecordsValuesFour();

    System.out.println("Los datos con los que se calculara la necesidad de agua de riego de un cultivo en la fecha actual (" + UtilDate.formatDate(presumedCurrentDate) + ") son los siguientes:");
    System.out.println();

    printWaterBalanceTable(presumedCurrentDate, testClimateRecords, testIrrigationRecords);

    double totalIrrigationWaterCurrentDate = 0.0;

    printAccumulatedAmountRainWater(presumedCurrentDate, testClimateRecords);
    printAccumulatedAmountIrrigationWater(presumedCurrentDate, testIrrigationRecords);
    System.out.println();
    printAccumulatedWaterDeficitPerDay(presumedCurrentDate, testClimateRecords, testIrrigationRecords);
    printTotalIrrigationWaterCurrentDate(presumedCurrentDate, totalIrrigationWaterCurrentDate);

    System.out.println();
    printMessageSituationOne();
    System.out.println();

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");

    double expectedResult = 1.0;
    double result = WaterNeedWit.calculateIrrigationWaterNeed(totalIrrigationWaterCurrentDate, testClimateRecords, testIrrigationRecords);

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
     * Establece los datos de los registros para
     * esta prueba
     */
    setRecordsValuesFive();

    System.out.println("Los datos con los que se calculara la necesidad de agua de riego de un cultivo en la fecha actual (" + UtilDate.formatDate(presumedCurrentDate) + ") son los siguientes:");
    System.out.println();

    printWaterBalanceTable(presumedCurrentDate, testClimateRecords, zeroIrrigationRecords);

    double totalIrrigationWaterCurrentDate = 4.5;

    printAccumulatedAmountRainWater(presumedCurrentDate, testClimateRecords);
    printAccumulatedAmountIrrigationWater(presumedCurrentDate, zeroIrrigationRecords);
    System.out.println();
    printAccumulatedWaterDeficitPerDay(presumedCurrentDate, testClimateRecords, zeroIrrigationRecords);
    printTotalIrrigationWaterCurrentDate(presumedCurrentDate, totalIrrigationWaterCurrentDate);

    System.out.println();
    printMessageSituationOne();
    System.out.println();

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");

    double expectedResult = 4.0;
    double result = WaterNeedWit.calculateIrrigationWaterNeed(totalIrrigationWaterCurrentDate, testClimateRecords, zeroIrrigationRecords);

    System.out.println("* Valor esperado (nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateIrrigationWaterNeed (nec. agua riego");
    System.out.println("[mm/dia] de un cultivo en fecha actual): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testSixCalculateIrrigationWaterNeed() {
    System.out.println("************************************** Prueba seis del metodo calculateIrrigationWaterNeed ***************************************");
    printDescriptionMethodToTest();

    System.out.println("# Descripcion de la prueba unitaria");

    /*
     * Establece los datos de los registros para
     * esta prueba
     */
    setRecordsValuesSix();

    System.out.println("Los datos con los que se calculara la necesidad de agua de riego de un cultivo en la fecha actual (" + UtilDate.formatDate(presumedCurrentDate) + ") son los siguientes:");
    System.out.println();

    printWaterBalanceTable(presumedCurrentDate, testClimateRecords, zeroIrrigationRecords);

    double totalIrrigationWaterCurrentDate = 1.0;

    printAccumulatedAmountRainWater(presumedCurrentDate, testClimateRecords);
    printAccumulatedAmountIrrigationWater(presumedCurrentDate, zeroIrrigationRecords);
    System.out.println();
    printAccumulatedWaterDeficitPerDay(presumedCurrentDate, testClimateRecords, zeroIrrigationRecords);
    printTotalIrrigationWaterCurrentDate(presumedCurrentDate, totalIrrigationWaterCurrentDate);

    System.out.println();
    printMessageSituationOne();
    System.out.println();

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");

    double expectedResult = 1;
    double result = WaterNeedWit.calculateIrrigationWaterNeed(totalIrrigationWaterCurrentDate, testClimateRecords, zeroIrrigationRecords);

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

    /*
     * Establece los datos de los registros para
     * esta prueba
     */
    setRecordsValuesSeven();

    System.out.println("Los datos con los que se calculara la necesidad de agua de riego de un cultivo en la fecha actual (" + UtilDate.formatDate(presumedCurrentDate) + ") son los siguientes:");
    System.out.println();

    printWaterBalanceTable(presumedCurrentDate, testClimateRecords, testIrrigationRecords);

    double totalIrrigationWaterCurrentDate = 1.5;

    printAccumulatedAmountRainWater(presumedCurrentDate, testClimateRecords);
    printAccumulatedAmountIrrigationWater(presumedCurrentDate, testIrrigationRecords);
    System.out.println();
    printAccumulatedWaterDeficitPerDay(presumedCurrentDate, testClimateRecords, testIrrigationRecords);
    printTotalIrrigationWaterCurrentDate(presumedCurrentDate, totalIrrigationWaterCurrentDate);

    System.out.println();
    printMessageSituationOne();
    System.out.println();

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");

    double expectedResult = 0.5;
    double result = WaterNeedWit.calculateIrrigationWaterNeed(totalIrrigationWaterCurrentDate, testClimateRecords, testIrrigationRecords);

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

    /*
     * Establece los datos de los registros para
     * esta prueba
     */
    setRecordsValuesEight();

    System.out.println("Los datos con los que se calculara la necesidad de agua de riego de un cultivo en la fecha actual (" + UtilDate.formatDate(presumedCurrentDate) + ") son los siguientes:");
    System.out.println();

    printWaterBalanceTable(presumedCurrentDate, testClimateRecords, testIrrigationRecords);

    double totalIrrigationWaterCurrentDate = 0.2;

    printAccumulatedAmountRainWater(presumedCurrentDate, testClimateRecords);
    printAccumulatedAmountIrrigationWater(presumedCurrentDate, testIrrigationRecords);
    System.out.println();
    printAccumulatedWaterDeficitPerDay(presumedCurrentDate, testClimateRecords, testIrrigationRecords);
    printTotalIrrigationWaterCurrentDate(presumedCurrentDate, totalIrrigationWaterCurrentDate);

    System.out.println();
    printMessageSituationOne();
    System.out.println();

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");

    double expectedResult = 0.8;
    double result = WaterNeedWit.calculateIrrigationWaterNeed(totalIrrigationWaterCurrentDate, testClimateRecords, testIrrigationRecords);

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

    /*
     * Establece los datos de los registros para
     * esta prueba
     */
    setRecordsValuesNine();

    System.out.println("Los datos con los que se calculara la necesidad de agua de riego de un cultivo en la fecha actual (" + UtilDate.formatDate(presumedCurrentDate) + ") son los siguientes:");
    System.out.println();

    printWaterBalanceTable(presumedCurrentDate, testClimateRecords, testIrrigationRecords);

    double totalIrrigationWaterCurrentDate = 4.0;

    printAccumulatedAmountRainWater(presumedCurrentDate, testClimateRecords);
    printAccumulatedAmountIrrigationWater(presumedCurrentDate, testIrrigationRecords);
    System.out.println();
    printAccumulatedWaterDeficitPerDay(presumedCurrentDate, testClimateRecords, testIrrigationRecords);
    printTotalIrrigationWaterCurrentDate(presumedCurrentDate, totalIrrigationWaterCurrentDate);

    System.out.println();
    printMessageSituationTwo();
    System.out.println();

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");

    double expectedResult = 0.0;
    double result = WaterNeedWit.calculateIrrigationWaterNeed(totalIrrigationWaterCurrentDate, testClimateRecords, testIrrigationRecords);

    System.out.println("* Valor esperado (nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateIrrigationWaterNeed (nec. agua riego");
    System.out.println("[mm/dia] de un cultivo en fecha actual): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  /**
   * La situacion uno es aquella en la que la suma entre el acumulado
   * del deficit de agua por dia de dias previos a una fecha [mm/dia]
   * y la cantidad total de agua de riego de una fecha [mm/dia] es
   * estrictamente menor a 0 (cero) [mm/dia]
   */
  private void printMessageSituationOne() {
    System.out.println("Al ser");
    System.out.println("- el resultado de la suma entre el acumulado del deficit de agua por dia de dias previos a la fecha actual [mm/dia] y la cantidad total");
    System.out.println("de agua de riego de la fecha actual [mm/dia], estrictamente menor a 0 (cero),");
    System.out.println();
    System.out.println("el valor devuelto por el metodo calculateIrrigationWaterNeed debe ser el valor absoluto de dicha suma. Por lo tanto, la necesidad de agua");
    System.out.println("de riego de un cultivo en la fecha actual [mm/dia] es el resultado del valor absoluto de la suma entre el acumulado del deficit de agua");
    System.out.println("por dia de dias previos a la fecha actual y la cantidad total de agua de riego de la fecha actual.");
  }

  /**
   * La situacion uno es aquella en la que la suma entre el acumulado
   * del deficit de agua por dia de dias previos a una fecha [mm/dia]
   * y la cantidad total de agua de riego de una fecha [mm/dia] es
   * mayor o igual a 0 (cero) [mm/dia]
   */
  private void printMessageSituationTwo() {
    System.out.println("Al ser");
    System.out.println("- el resultado de la suma entre el acumulado del deficit de agua por dia de dias previos a la fecha actual [mm/dia] y la cantidad total");
    System.out.println("de agua de riego de la fecha actual [mm/dia], mayor o igual a 0 (cero),");
    System.out.println();
    System.out.println("el valor devuelto por el metodo calculateIrrigationWaterNeed debe ser 0. Por lo tanto, la necesidad de agua de riego de un cultivo en la");
    System.out.println("fecha actual es 0 [mm/dia].");
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
    System.out.println("Diferencia (H2O - ETc) [mm/dia]");
    System.out.println("Acumulado del deficit de agua por dia de dias previos a una fecha (dia) [mm/dia]");
    System.out.println();
  }

  /**
   * @param presumedCurrentDate
   * @param totalIrrigationWaterCurrentDate
   */
  private void printTotalIrrigationWaterCurrentDate(Calendar presumedCurrentDate, double totalIrrigationWaterCurrentDate) {
    System.out.println("- Cantidad total de agua de riego en la fecha actual (hoy) "
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
   * @param presumedCurrenteDate
   * @param givenClimateRecords
   */
  private void printAccumulatedAmountRainWater(Calendar presumedCurrenteDate, Collection<ClimateRecord> givenClimateRecords) {
    System.out.println("- Cantidad acumulada de agua de lluvia de dias previos a la fecha actual (hoy) "
        + UtilDate.formatDate(presumedCurrenteDate) + ": " + calculateAccumulatedRain(givenClimateRecords) + " [mm]");
  }

  /**
   * @param presumedCurrenteDate
   * @param givenIrrigationRecords
   */
  private void printAccumulatedAmountIrrigationWater(Calendar presumedCurrenteDate, Collection<IrrigationRecord> givenIrrigationRecords) {
    System.out.println("- Cantidad acumulada de agua de riego de dias previos a la fecha actual (hoy) "
        + UtilDate.formatDate(presumedCurrenteDate) + ": " + calculateAccumulatedIrrigationWater(givenIrrigationRecords) + " [mm]");
  }

  /**
   * @param climateRecords
   * @return double que representa el acumulado del
   * agua de lluvia de una coleccion de registros
   * climaticos
   */
  private double calculateAccumulatedRain(Collection<ClimateRecord> climateRecords) {
    return climateRecords.stream().mapToDouble(ClimateRecord::getPrecip).sum();
  }

  /**
   * @param irrigationRecords
   * @return double que representa el acumulado del
   * agua de riego de una coleccion de registros
   * climaticos
   */
  private double calculateAccumulatedIrrigationWater(Collection<IrrigationRecord> irrigationRecords) {
    return irrigationRecords.stream().mapToDouble(IrrigationRecord::getIrrigationDone).sum();
  }

  /**
   * Imprime la descripcion del metodo a probar, el cual en este
   * caso es el metodo calculateIrrigationWaterNeed de la clase
   * WaterNeedWit
   */
  private void printDescriptionMethodToTest() {
    System.out.println("El metodo calculateIrrigationWaterNeed de la clase WaterNeedWit calcula la necesidad de agua de riego [mm/dia] de un cultivo en una");
    System.out.println("fecha, si se lo invoca con una coleccción de registros climaticos y una coleccion de registros de riego previos a una fecha pertenecientes");
    System.out.println("a una misma parcela que tiene un cultivo sembrado en una fecha. La fecha para la que se calcula la necesidad de agua de riego de un");
    System.out.println("cultivo puede ser la fecha actual (es decir, hoy) o una fecha posterior a la fecha actual. Pero tambien puede ser una fecha del pasado");
    System.out.println("(es decir, anterior a la fecha actual), pero esto no tiene sentido si lo que se busca es determinar la necesidad de agua de riego de");
    System.out.println("un cultivo en la fecha actual o en una fecha posterior a la fecha actual.");
    System.out.println();
    System.out.println("Si este metodo es invocado con una coleccion de registros climaticos y una coleccion de registros de riego previos a una fecha pertenecientes");
    System.out.println("a una parcela que NO tiene un cultivo sembrado en una fecha, el valor devuelto por el mismo representa el acumulado del deficit de");
    System.out.println("agua por dia de dias previos a una fecha de una parcela en una fecha [mm/dia].");
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

  /*
   * **************************************************************
   * A partir de aqui se encuentran los metodos para establecer los
   * datos de los registros utilizados en las pruebas unitarias
   * **************************************************************
   */

  /**
   * Establece los datos de los registros utilizados
   * en la prueba unitaria uno
   */
  private void setRecordsValuesOne() {
    climateRecordOne.setEtc(2);
    climateRecordOne.setPrecip(0);

    climateRecordTwo.setEtc(1);
    climateRecordTwo.setPrecip(0);

    climateRecordThree.setEtc(1);
    climateRecordThree.setPrecip(0);

    climateRecordFour.setEtc(2);
    climateRecordFour.setPrecip(0);

    climateRecordFive.setEtc(1.5);
    climateRecordFive.setPrecip(0);

    climateRecordSix.setEtc(1);
    climateRecordSix.setPrecip(0);
  }

  /**
   * Establece los datos de los registros utilizados
   * en la prueba unitaria dos
   */
  private void setRecordsValuesTwo() {
    climateRecordOne.setEtc(2);
    climateRecordOne.setPrecip(1);

    climateRecordTwo.setEtc(1);
    climateRecordTwo.setPrecip(2);

    climateRecordThree.setEtc(3);
    climateRecordThree.setPrecip(4);

    climateRecordFour.setEtc(2);
    climateRecordFour.setPrecip(1.5);

    climateRecordFive.setEtc(1.5);
    climateRecordFive.setPrecip(1);

    climateRecordSix.setEtc(3);
    climateRecordSix.setPrecip(2);
  }

  /**
   * Establece los datos de los registros utilizados
   * en la prueba unitaria tres
   */
  private void setRecordsValuesThree() {
    climateRecordOne.setEtc(2);
    climateRecordOne.setPrecip(0);

    climateRecordTwo.setEtc(1);
    climateRecordTwo.setPrecip(0);

    climateRecordThree.setEtc(3);
    climateRecordThree.setPrecip(0);

    climateRecordFour.setEtc(2);
    climateRecordFour.setPrecip(0);

    climateRecordFive.setEtc(1.5);
    climateRecordFive.setPrecip(0);

    climateRecordSix.setEtc(3);
    climateRecordSix.setPrecip(0);

    irrigationRecordOne.setIrrigationDone(1);
    irrigationRecordTwo.setIrrigationDone(2);
    irrigationRecordThree.setIrrigationDone(4);
    irrigationRecordFour.setIrrigationDone(1.5);
    irrigationRecordFive.setIrrigationDone(1);
    irrigationRecordSix.setIrrigationDone(2);
  }

  /**
   * Establece los datos de los registros utilizados
   * en la prueba unitaria cuatro
   */
  private void setRecordsValuesFour() {
    climateRecordOne.setEtc(3);
    climateRecordOne.setPrecip(1);

    climateRecordTwo.setEtc(2);
    climateRecordTwo.setPrecip(2);

    climateRecordThree.setEtc(5);
    climateRecordThree.setPrecip(3);

    climateRecordFour.setEtc(2);
    climateRecordFour.setPrecip(2);

    climateRecordFive.setEtc(3.5);
    climateRecordFive.setPrecip(2.5);

    climateRecordSix.setEtc(4);
    climateRecordSix.setPrecip(1);

    irrigationRecordOne.setIrrigationDone(1);
    irrigationRecordTwo.setIrrigationDone(2);
    irrigationRecordThree.setIrrigationDone(4);
    irrigationRecordFour.setIrrigationDone(1.5);
    irrigationRecordFive.setIrrigationDone(1);
    irrigationRecordSix.setIrrigationDone(2);
  }

  /**
   * Establece los datos de los registros utilizados
   * en la prueba unitaria cinco
   */
  private void setRecordsValuesFive() {
    climateRecordOne.setEtc(2);
    climateRecordOne.setPrecip(0);

    climateRecordTwo.setEtc(1);
    climateRecordTwo.setPrecip(0);

    climateRecordThree.setEtc(1);
    climateRecordThree.setPrecip(0);

    climateRecordFour.setEtc(2);
    climateRecordFour.setPrecip(0);

    climateRecordFive.setEtc(1.5);
    climateRecordFive.setPrecip(0);

    climateRecordSix.setEtc(1);
    climateRecordSix.setPrecip(0);
  }

  /**
   * Establece los datos de los registros utilizados
   * en la prueba unitaria seis
   */
  private void setRecordsValuesSix() {
    climateRecordOne.setEtc(2);
    climateRecordOne.setPrecip(1);

    climateRecordTwo.setEtc(1);
    climateRecordTwo.setPrecip(2);

    climateRecordThree.setEtc(3);
    climateRecordThree.setPrecip(4);

    climateRecordFour.setEtc(2);
    climateRecordFour.setPrecip(1.5);

    climateRecordFive.setEtc(1.5);
    climateRecordFive.setPrecip(1);

    climateRecordSix.setEtc(3);
    climateRecordSix.setPrecip(2);
  }

  /**
   * Establece los datos de los registros utilizados
   * en la prueba unitaria sieite
   */
  private void setRecordsValuesSeven() {
    climateRecordOne.setEtc(2);
    climateRecordOne.setPrecip(0);

    climateRecordTwo.setEtc(1);
    climateRecordTwo.setPrecip(0);

    climateRecordThree.setEtc(3);
    climateRecordThree.setPrecip(0);

    climateRecordFour.setEtc(2);
    climateRecordFour.setPrecip(0);

    climateRecordFive.setEtc(1.5);
    climateRecordFive.setPrecip(0);

    climateRecordSix.setEtc(3);
    climateRecordSix.setPrecip(0);

    irrigationRecordOne.setIrrigationDone(1);
    irrigationRecordTwo.setIrrigationDone(2);
    irrigationRecordThree.setIrrigationDone(4);
    irrigationRecordFour.setIrrigationDone(1.5);
    irrigationRecordFive.setIrrigationDone(1);
    irrigationRecordSix.setIrrigationDone(2);
  }

  /**
   * Establece los datos de los registros utilizados
   * en la prueba unitaria ocho
   */
  private void setRecordsValuesEight() {
    climateRecordOne.setEtc(3);
    climateRecordOne.setPrecip(1);

    climateRecordTwo.setEtc(2);
    climateRecordTwo.setPrecip(2);

    climateRecordThree.setEtc(5);
    climateRecordThree.setPrecip(3);

    climateRecordFour.setEtc(2);
    climateRecordFour.setPrecip(2);

    climateRecordFive.setEtc(3.5);
    climateRecordFive.setPrecip(2.5);

    climateRecordSix.setEtc(4);
    climateRecordSix.setPrecip(1);

    irrigationRecordOne.setIrrigationDone(1);
    irrigationRecordTwo.setIrrigationDone(2);
    irrigationRecordThree.setIrrigationDone(4);
    irrigationRecordFour.setIrrigationDone(1.5);
    irrigationRecordFive.setIrrigationDone(1);
    irrigationRecordSix.setIrrigationDone(2);
  }

  /**
   * Establece los datos de los registros utilizados
   * en la prueba unitaria nueve
   */
  private void setRecordsValuesNine() {
    climateRecordOne.setEtc(3);
    climateRecordOne.setPrecip(1);

    climateRecordTwo.setEtc(2);
    climateRecordTwo.setPrecip(2);

    climateRecordThree.setEtc(5);
    climateRecordThree.setPrecip(3);

    climateRecordFour.setEtc(2);
    climateRecordFour.setPrecip(2);

    climateRecordFive.setEtc(3.5);
    climateRecordFive.setPrecip(2.5);

    climateRecordSix.setEtc(4);
    climateRecordSix.setPrecip(1);

    irrigationRecordOne.setIrrigationDone(1);
    irrigationRecordTwo.setIrrigationDone(2);
    irrigationRecordThree.setIrrigationDone(4);
    irrigationRecordFour.setIrrigationDone(1.5);
    irrigationRecordFive.setIrrigationDone(1);
    irrigationRecordSix.setIrrigationDone(2);
  }

}