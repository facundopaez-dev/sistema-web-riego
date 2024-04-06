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
     * del metodo calculateCropIrrigationWaterNeed() de la clase
     * WaterMath en las que se busca demostrar el correcto funcionamiento
     * del mismo sin hacer uso del agua de riego de cultivo de un
     * conjunto de dias
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
  public void testOneCalculateCropIrrigationWaterNeed() {
    System.out.println("************************************** Prueba uno del metodo calculateCropIrrigationWaterNeed() ***************************************");
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

    printWaterBalanceTable(presumedCurrentDate, climateRecords, zeroIrrigationRecords);

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");

    /*
     * Calcula el acumulado del deficit (falta) de humedad por
     * dia [mm/dia] de un conjunto de dias
     */
    double totalCropIrrigationWaterPresumedCurrentDate = 0;
    double accumulatedSoilMoistureDeficitPerDay = calculateAccumulatedSoilMoistureDeficitPerDay(climateRecords, zeroIrrigationRecords);
    double result = WaterMath.calculateCropIrrigationWaterNeed(totalCropIrrigationWaterPresumedCurrentDate, accumulatedSoilMoistureDeficitPerDay);
    double expectedResult = 14;

    System.out.println("* Cantidad total de agua de riego de cultivo de la presunta fecha actual: " + totalCropIrrigationWaterPresumedCurrentDate + " [mm/dia]");
    System.out.println("* Valor esperado (nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateCropIrrigationWaterNeed()");
    System.out.println("(nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testTwoCalculateCropIrrigationWaterNeed() {
    System.out.println("************************************** Prueba dos del metodo calculateCropIrrigationWaterNeed() ***************************************");
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

    printWaterBalanceTable(presumedCurrentDate, climateRecords, zeroIrrigationRecords);

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");

    /*
     * Calcula el acumulado del deficit (falta) de humedad por
     * dia [mm/dia] de un conjunto de dias
     */
    double totalCropIrrigationWaterPresumedCurrentDate = 0;
    double accumulatedSoilMoistureDeficitPerDay = calculateAccumulatedSoilMoistureDeficitPerDay(climateRecords, zeroIrrigationRecords);
    double result = WaterMath.calculateCropIrrigationWaterNeed(totalCropIrrigationWaterPresumedCurrentDate, accumulatedSoilMoistureDeficitPerDay);
    double expectedResult = 9.0;

    System.out.println("* Cantidad total de agua de riego de cultivo de la presunta fecha actual: " + totalCropIrrigationWaterPresumedCurrentDate + " [mm/dia]");
    System.out.println("* Valor esperado (nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateCropIrrigationWaterNeed()");
    System.out.println("(nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testThreeCalculateCropIrrigationWaterNeed() {
    System.out.println("************************************** Prueba tres del metodo calculateCropIrrigationWaterNeed() ***************************************");
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

    printWaterBalanceTable(presumedCurrentDate, climateRecords, zeroIrrigationRecords);

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");

    /*
     * Calcula el acumulado del deficit (falta) de humedad por
     * dia [mm/dia] de un conjunto de dias
     */
    double totalCropIrrigationWaterPresumedCurrentDate = 0;
    double accumulatedSoilMoistureDeficitPerDay = calculateAccumulatedSoilMoistureDeficitPerDay(climateRecords, zeroIrrigationRecords);
    double result = WaterMath.calculateCropIrrigationWaterNeed(totalCropIrrigationWaterPresumedCurrentDate, accumulatedSoilMoistureDeficitPerDay);
    double expectedResult = 5.0;

    System.out.println("* Cantidad total de agua de riego de cultivo de la presunta fecha actual: " + totalCropIrrigationWaterPresumedCurrentDate + " [mm/dia]");
    System.out.println("* Valor esperado (nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateCropIrrigationWaterNeed()");
    System.out.println("(nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testFourCalculateCropIrrigationWaterNeed() {
    System.out.println("************************************** Prueba cuatro del metodo calculateCropIrrigationWaterNeed() ***************************************");
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

    printWaterBalanceTable(presumedCurrentDate, climateRecords, zeroIrrigationRecords);

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");

    /*
     * Calcula el acumulado del deficit (falta) de humedad por
     * dia [mm/dia] de un conjunto de dias
     */
    double totalCropIrrigationWaterPresumedCurrentDate = 0;
    double accumulatedSoilMoistureDeficitPerDay = calculateAccumulatedSoilMoistureDeficitPerDay(climateRecords, zeroIrrigationRecords);
    double result = WaterMath.calculateCropIrrigationWaterNeed(totalCropIrrigationWaterPresumedCurrentDate, accumulatedSoilMoistureDeficitPerDay);
    double expectedResult = 0.0;

    System.out.println("* Cantidad total de agua de riego de cultivo de la presunta fecha actual: " + totalCropIrrigationWaterPresumedCurrentDate + " [mm/dia]");
    System.out.println("* Valor esperado (nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateCropIrrigationWaterNeed()");
    System.out.println("(nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testFiveCalculateCropIrrigationWaterNeed() {
    System.out.println("************************************** Prueba cinco del metodo calculateCropIrrigationWaterNeed() ***************************************");
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

    printWaterBalanceTable(presumedCurrentDate, climateRecords, zeroIrrigationRecords);

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");

    /*
     * Calcula el acumulado del deficit (falta) de humedad por
     * dia [mm/dia] de un conjunto de dias
     */
    double totalCropIrrigationWaterPresumedCurrentDate = 0;
    double accumulatedSoilMoistureDeficitPerDay = calculateAccumulatedSoilMoistureDeficitPerDay(climateRecords, zeroIrrigationRecords);
    double result = WaterMath.calculateCropIrrigationWaterNeed(totalCropIrrigationWaterPresumedCurrentDate, accumulatedSoilMoistureDeficitPerDay);
    double expectedResult = 0.0;

    System.out.println("* Cantidad total de agua de riego de cultivo de la presunta fecha actual: " + totalCropIrrigationWaterPresumedCurrentDate + " [mm/dia]");
    System.out.println("* Valor esperado (nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateCropIrrigationWaterNeed()");
    System.out.println("(nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testSixCalculateCropIrrigationWaterNeed() {
    System.out.println("************************************** Prueba seis del metodo calculateCropIrrigationWaterNeed() ***************************************");
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

    printWaterBalanceTable(presumedCurrentDate, climateRecords, zeroIrrigationRecords);

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");

    /*
     * Calcula el acumulado del deficit (falta) de humedad por
     * dia [mm/dia] de un conjunto de dias
     */
    double totalCropIrrigationWaterPresumedCurrentDate = 0;
    double accumulatedSoilMoistureDeficitPerDay = calculateAccumulatedSoilMoistureDeficitPerDay(climateRecords, zeroIrrigationRecords);
    double result = WaterMath.calculateCropIrrigationWaterNeed(totalCropIrrigationWaterPresumedCurrentDate, accumulatedSoilMoistureDeficitPerDay);
    double expectedResult = 4.0;

    System.out.println("* Cantidad total de agua de riego de cultivo de la presunta fecha actual: " + totalCropIrrigationWaterPresumedCurrentDate + " [mm/dia]");
    System.out.println("* Valor esperado (nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateCropIrrigationWaterNeed()");
    System.out.println("(nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testSevenCalculateCropIrrigationWaterNeed() {
    System.out.println("************************************** Prueba siete del metodo calculateCropIrrigationWaterNeed() ***************************************");
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

    printWaterBalanceTable(presumedCurrentDate, climateRecords, zeroIrrigationRecords);

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");

    /*
     * Calcula el acumulado del deficit (falta) de humedad por
     * dia [mm/dia] de un conjunto de dias
     */
    double totalCropIrrigationWaterPresumedCurrentDate = 0;
    double accumulatedSoilMoistureDeficitPerDay = calculateAccumulatedSoilMoistureDeficitPerDay(climateRecords, zeroIrrigationRecords);
    double result = WaterMath.calculateCropIrrigationWaterNeed(totalCropIrrigationWaterPresumedCurrentDate, accumulatedSoilMoistureDeficitPerDay);
    double expectedResult = 7.0;

    System.out.println("* Cantidad total de agua de riego de cultivo de la presunta fecha actual: " + totalCropIrrigationWaterPresumedCurrentDate + " [mm/dia]");
    System.out.println("* Valor esperado (nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateCropIrrigationWaterNeed()");
    System.out.println("(nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testEightCalculateCropIrrigationWaterNeed() {
    System.out.println("************************************** Prueba ocho del metodo calculateCropIrrigationWaterNeed() ***************************************");
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

    printWaterBalanceTable(presumedCurrentDate, climateRecords, zeroIrrigationRecords);

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");

    /*
     * Calcula el acumulado del deficit (falta) de humedad por
     * dia [mm/dia] de un conjunto de dias
     */
    double totalCropIrrigationWaterPresumedCurrentDate = 0;
    double accumulatedSoilMoistureDeficitPerDay = calculateAccumulatedSoilMoistureDeficitPerDay(climateRecords, zeroIrrigationRecords);
    double result = WaterMath.calculateCropIrrigationWaterNeed(totalCropIrrigationWaterPresumedCurrentDate, accumulatedSoilMoistureDeficitPerDay);
    double expectedResult = 4.0;

    System.out.println("* Cantidad total de agua de riego de cultivo de la presunta fecha actual: " + totalCropIrrigationWaterPresumedCurrentDate + " [mm/dia]");
    System.out.println("* Valor esperado (nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateCropIrrigationWaterNeed()");
    System.out.println("(nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testNineCalculateCropIrrigationWaterNeed() {
    System.out.println("************************************** Prueba nueve del metodo calculateCropIrrigationWaterNeed() ***************************************");
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

    printWaterBalanceTable(presumedCurrentDate, climateRecords, zeroIrrigationRecords);

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");

    /*
     * Calcula el acumulado del deficit (falta) de humedad por
     * dia [mm/dia] de un conjunto de dias
     */
    double totalCropIrrigationWaterPresumedCurrentDate = 0;
    double accumulatedSoilMoistureDeficitPerDay = calculateAccumulatedSoilMoistureDeficitPerDay(climateRecords, zeroIrrigationRecords);
    double result = WaterMath.calculateCropIrrigationWaterNeed(totalCropIrrigationWaterPresumedCurrentDate, accumulatedSoilMoistureDeficitPerDay);
    double expectedResult = 0.0;

    System.out.println("* Cantidad total de agua de riego de cultivo de la presunta fecha actual: " + totalCropIrrigationWaterPresumedCurrentDate + " [mm/dia]");
    System.out.println("* Valor esperado (nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateCropIrrigationWaterNeed()");
    System.out.println("(nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testTenCalculateCropIrrigationWaterNeed() {
    System.out.println("************************************** Prueba diez del metodo calculateCropIrrigationWaterNeed() ***************************************");
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

    printWaterBalanceTable(presumedCurrentDate, climateRecords, zeroIrrigationRecords);

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");

    /*
     * Calcula el acumulado del deficit (falta) de humedad por
     * dia [mm/dia] de un conjunto de dias
     */
    double totalCropIrrigationWaterPresumedCurrentDate = 0;
    double accumulatedSoilMoistureDeficitPerDay = calculateAccumulatedSoilMoistureDeficitPerDay(climateRecords, zeroIrrigationRecords);
    double result = WaterMath.calculateCropIrrigationWaterNeed(totalCropIrrigationWaterPresumedCurrentDate, accumulatedSoilMoistureDeficitPerDay);
    double expectedResult = 0.0;

    System.out.println("* Cantidad total de agua de riego de cultivo de la presunta fecha actual: " + totalCropIrrigationWaterPresumedCurrentDate + " [mm/dia]");
    System.out.println("* Valor esperado (nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateCropIrrigationWaterNeed()");
    System.out.println("(nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testElevenCalculateCropIrrigationWaterNeed() {
    System.out.println("************************************** Prueba once del metodo calculateCropIrrigationWaterNeed() ***************************************");
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

    printWaterBalanceTable(presumedCurrentDate, climateRecords, zeroIrrigationRecords);

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");

    /*
     * Calcula el acumulado del deficit (falta) de humedad por
     * dia [mm/dia] de un conjunto de dias
     */
    double totalCropIrrigationWaterPresumedCurrentDate = 0;
    double accumulatedSoilMoistureDeficitPerDay = calculateAccumulatedSoilMoistureDeficitPerDay(climateRecords, zeroIrrigationRecords);
    double result = WaterMath.calculateCropIrrigationWaterNeed(totalCropIrrigationWaterPresumedCurrentDate, accumulatedSoilMoistureDeficitPerDay);
    double expectedResult = 1.0;

    System.out.println("* Cantidad total de agua de riego de cultivo de la presunta fecha actual: " + totalCropIrrigationWaterPresumedCurrentDate + " [mm/dia]");
    System.out.println("* Valor esperado (nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateCropIrrigationWaterNeed()");
    System.out.println("(nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testTwelveCalculateCropIrrigationWaterNeed() {
    System.out.println("************************************** Prueba doce del metodo calculateCropIrrigationWaterNeed() ***************************************");
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
    setClimateRecordsValuesTwelve();

    System.out.println("Los datos con los que se calculara la necesidad de agua de riego de un cultivo en la fecha actual (" + UtilDate.formatDate(presumedCurrentDate) + ") son los siguientes:");
    System.out.println();

    printWaterBalanceTable(presumedCurrentDate, climateRecords, zeroIrrigationRecords);

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");

    /*
     * Calcula el acumulado del deficit (falta) de humedad por
     * dia [mm/dia] de un conjunto de dias
     */
    double totalCropIrrigationWaterPresumedCurrentDate = 16;
    double accumulatedSoilMoistureDeficitPerDay = calculateAccumulatedSoilMoistureDeficitPerDay(climateRecords, zeroIrrigationRecords);
    double result = WaterMath.calculateCropIrrigationWaterNeed(totalCropIrrigationWaterPresumedCurrentDate, accumulatedSoilMoistureDeficitPerDay);
    double expectedResult = 0;

    System.out.println("* Cantidad total de agua de riego de cultivo de la presunta fecha actual: " + totalCropIrrigationWaterPresumedCurrentDate + " [mm/dia]");
    System.out.println("* Valor esperado (nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateCropIrrigationWaterNeed()");
    System.out.println("(nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testThirteenCalculateCropIrrigationWaterNeed() {
    System.out.println("************************************** Prueba trece del metodo calculateCropIrrigationWaterNeed() ***************************************");
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
    setClimateRecordsValuesThirteen();

    System.out.println("Los datos con los que se calculara la necesidad de agua de riego de un cultivo en la fecha actual (" + UtilDate.formatDate(presumedCurrentDate) + ") son los siguientes:");
    System.out.println();

    printWaterBalanceTable(presumedCurrentDate, climateRecords, zeroIrrigationRecords);

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");

    /*
     * Calcula el acumulado del deficit (falta) de humedad por
     * dia [mm/dia] de un conjunto de dias
     */
    double totalCropIrrigationWaterPresumedCurrentDate = 14;
    double accumulatedSoilMoistureDeficitPerDay = calculateAccumulatedSoilMoistureDeficitPerDay(climateRecords, zeroIrrigationRecords);
    double result = WaterMath.calculateCropIrrigationWaterNeed(totalCropIrrigationWaterPresumedCurrentDate, accumulatedSoilMoistureDeficitPerDay);
    double expectedResult = 0;

    System.out.println("* Cantidad total de agua de riego de cultivo de la presunta fecha actual: " + totalCropIrrigationWaterPresumedCurrentDate + " [mm/dia]");
    System.out.println("* Valor esperado (nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateCropIrrigationWaterNeed()");
    System.out.println("(nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testFourteenCalculateCropIrrigationWaterNeed() {
    System.out.println("************************************** Prueba catorce del metodo calculateCropIrrigationWaterNeed() ***************************************");
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
    setClimateRecordsValuesFourteen();

    System.out.println("Los datos con los que se calculara la necesidad de agua de riego de un cultivo en la fecha actual (" + UtilDate.formatDate(presumedCurrentDate) + ") son los siguientes:");
    System.out.println();

    printWaterBalanceTable(presumedCurrentDate, climateRecords, zeroIrrigationRecords);

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");

    /*
     * Calcula el acumulado del deficit (falta) de humedad por
     * dia [mm/dia] de un conjunto de dias
     */
    double totalCropIrrigationWaterPresumedCurrentDate = 2;
    double accumulatedSoilMoistureDeficitPerDay = calculateAccumulatedSoilMoistureDeficitPerDay(climateRecords, zeroIrrigationRecords);
    double result = WaterMath.calculateCropIrrigationWaterNeed(totalCropIrrigationWaterPresumedCurrentDate, accumulatedSoilMoistureDeficitPerDay);
    double expectedResult = 12;

    System.out.println("* Cantidad total de agua de riego de cultivo de la presunta fecha actual: " + totalCropIrrigationWaterPresumedCurrentDate + " [mm/dia]");
    System.out.println("* Valor esperado (nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateCropIrrigationWaterNeed()");
    System.out.println("(nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testFifteenCalculateCropIrrigationWaterNeed() {
    System.out.println("************************************** Prueba quince del metodo calculateCropIrrigationWaterNeed() ***************************************");
    printDescriptionMethodToTest();

    System.out.println("# Descripcion de la prueba unitaria");
    System.out.println("Para esta prueba se utiliza 1 registro climatico, el cual es inmediatamente anterior a la fecha actual "
            + UtilDate.formatDate(presumedCurrentDate) + ".");
    System.out.println("Suponemos que la fecha actual es " + UtilDate.formatDate(presumedCurrentDate) + ".");
    System.out.println();

    System.out.println("Tambien se utiliza un conjunto de 2 registros de riego, los cuales pertenecen a la fecha inmediatamente anterior a la presunta fecha");
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

    /*
     * Calcula el acumulado del deficit (falta) de humedad por
     * dia [mm/dia] de un conjunto de dias
     */
    double totalCropIrrigationWaterPresumedCurrentDate = 0;
    double accumulatedSoilMoistureDeficitPerDay = calculateAccumulatedSoilMoistureDeficitPerDay(yesterdayClimateRecords, yesterdayIrrigationRecords);
    double result = WaterMath.calculateCropIrrigationWaterNeed(totalCropIrrigationWaterPresumedCurrentDate, accumulatedSoilMoistureDeficitPerDay);
    double expectedResult = 4.5;

    System.out.println("* Cantidad total de agua de riego de cultivo de la presunta fecha actual: " + totalCropIrrigationWaterPresumedCurrentDate + " [mm/dia]");
    System.out.println("* Valor esperado (nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateCropIrrigationWaterNeed()");
    System.out.println("(nec. agua riego [mm/dia] de un cultivo en la fecha actual): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  /*
   * **********************************************************************************************************************
   * A partir de aqui comienzan las pruebas unitarias del metodo calculateSoilMoistureDeficitPerDay() de la clase WaterMath
   * **********************************************************************************************************************
   */

  @Test
  public void testOneCalculateWaterDeficitPerDay() {
    System.out.println("************************************** Prueba uno del metodo calculateSoilMoistureDeficitPerDay() ***************************************");
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
    double result = WaterMath.calculateSoilMoistureDeficitPerDay(etc, precipitation, totalIrrigationWater);

    System.out.println("* Valor esperado (deficit de humedad por dia [mm/dia]): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateSoilMoistureDeficitPerDay(): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testTwoCalculateWaterDeficitPerDay() {
    System.out.println("************************************** Prueba dos del metodo calculateSoilMoistureDeficitPerDay() ***************************************");
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

    double expectedResult = 5.0;
    double result = WaterMath.calculateSoilMoistureDeficitPerDay(etc, precipitation, totalIrrigationWater);

    System.out.println("* Valor esperado (deficit de humedad por dia [mm/dia]): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateSoilMoistureDeficitPerDay(): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testThreeCalculateWaterDeficitPerDay() {
    System.out.println("************************************** Prueba tres del metodo calculateSoilMoistureDeficitPerDay() ***************************************");
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
    double result = WaterMath.calculateSoilMoistureDeficitPerDay(etc, precipitation, totalIrrigationWater);

    System.out.println("* Valor esperado (deficit de humedad por dia [mm/dia]): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateSoilMoistureDeficitPerDay(): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  private void printDescriptionCalculateWaterDeficitPerDay() {
    System.out.println("# Descripcion del metodo a probar");
    System.out.println("El metodo calculateSoilMoistureDeficitPerDay() de la clase WaterMath calcula el deficit de humedad por dia mediante la ETc, la");
    System.out.println("precipitacion y la cantidad total de agua de riego, debiendo ser estos tres valores de una misma fecha.");
    System.out.println();
    System.out.println("Si la ETc es estrictamente mayor a la suma entre la precipitacion y la cantidad total de agua de riego, el resultado devuelto");
    System.out.println("por el metodo calculateSoilMoistureDeficitPerDay() es negativo. En cambio, si la suma entre la precipitacion y la cantidad total de");
    System.out.println("agua riego es estrictamente mayor a la ETc, el resultado devuelto es positivo. Si la ETc es igual a la suma entre la precipitacion");
    System.out.println("y la cantidad total de agua de riego, el resultado es 0.");
  }

  /*
   * ***********************************************************************************************************************
   * A partir de aqui comienzan las pruebas unitarias del metodo accumulateSoilMoistureDeficitPerDay() de la clase WaterMath
   * ***********************************************************************************************************************
   */

  @Test
  public void testOneAccumulateWaterDeficitPerDay() {
    System.out.println("************************************** Prueba uno del metodo accumulateSoilMoistureDeficitPerDay() ***************************************");
    printDescriptionAccumulateWaterDeficitPerDay();
    System.out.println();

    /*
     * Datos de prueba
     */
    double soilMoistureDeficitPerDay = -5;
    double accumulatedSoilMoistureDeficitPerDay = -1;

    System.out.println("# Datos de prueba");
    System.out.println("Deficit de humedad por dia [mm/dia] = " + soilMoistureDeficitPerDay);
    System.out.println("Acumulado del deficit de humedad por dia [mm/dia] = " + accumulatedSoilMoistureDeficitPerDay);
    System.out.println();

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");

    double expectedResult = -6;
    double result = WaterMath.accumulateSoilMoistureDeficitPerDay(soilMoistureDeficitPerDay, accumulatedSoilMoistureDeficitPerDay);

    System.out.println("* Valor esperado (acumulado del deficit de humedad por dia [mm/dia]): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo accumulateSoilMoistureDeficitPerDay(): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testTwoAccumulateWaterDeficitPerDay() {
    System.out.println("************************************** Prueba dos del metodo accumulateSoilMoistureDeficitPerDay() ***************************************");
    printDescriptionAccumulateWaterDeficitPerDay();
    System.out.println();

    /*
     * Datos de prueba
     */
    double soilMoistureDeficitPerDay = 5;
    double accumulatedSoilMoistureDeficitPerDay = -5;

    System.out.println("# Datos de prueba");
    System.out.println("Deficit de humedad por dia [mm/dia] = " + soilMoistureDeficitPerDay);
    System.out.println("Acumulado del deficit de humedad por dia [mm/dia] = " + accumulatedSoilMoistureDeficitPerDay);
    System.out.println();

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");

    double expectedResult = 0;
    double result = WaterMath.accumulateSoilMoistureDeficitPerDay(soilMoistureDeficitPerDay, accumulatedSoilMoistureDeficitPerDay);

    System.out.println("* Valor esperado (acumulado del deficit de humedad por dia [mm/dia]): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo accumulateSoilMoistureDeficitPerDay(): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testThreeAccumulateWaterDeficitPerDay() {
    System.out.println("************************************** Prueba tres del metodo accumulateSoilMoistureDeficitPerDay() ***************************************");
    printDescriptionAccumulateWaterDeficitPerDay();
    System.out.println();

    /*
     * Datos de prueba
     */
    double soilMoistureDeficitPerDay = 10;
    double accumulatedSoilMoistureDeficitPerDay = -5;

    System.out.println("# Datos de prueba");
    System.out.println("Deficit de humedad por dia [mm/dia] = " + soilMoistureDeficitPerDay);
    System.out.println("Acumulado del deficit de humedad por dia [mm/dia] = " + accumulatedSoilMoistureDeficitPerDay);
    System.out.println();

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");

    double expectedResult = 0;
    double result = WaterMath.accumulateSoilMoistureDeficitPerDay(soilMoistureDeficitPerDay, accumulatedSoilMoistureDeficitPerDay);

    System.out.println("* Valor esperado (acumulado del deficit de humedad por dia [mm/dia]): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo accumulateSoilMoistureDeficitPerDay(): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testFourAccumulateWaterDeficitPerDay() {
    System.out.println("************************************** Prueba cuatro del metodo accumulateSoilMoistureDeficitPerDay() ***************************************");
    printDescriptionAccumulateWaterDeficitPerDay();
    System.out.println();

    /*
     * Datos de prueba
     */
    double soilMoistureDeficitPerDay = 1;
    double accumulatedSoilMoistureDeficitPerDay = -10;

    System.out.println("# Datos de prueba");
    System.out.println("Deficit de humedad por dia [mm/dia] = " + soilMoistureDeficitPerDay);
    System.out.println("Acumulado del deficit de humedad por dia [mm/dia] = " + accumulatedSoilMoistureDeficitPerDay);
    System.out.println();

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");

    double expectedResult = -9;
    double result = WaterMath.accumulateSoilMoistureDeficitPerDay(soilMoistureDeficitPerDay, accumulatedSoilMoistureDeficitPerDay);

    System.out.println("* Valor esperado (acumulado del deficit de humedad por dia [mm/dia]): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo accumulateSoilMoistureDeficitPerDay(): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  private void printDescriptionAccumulateWaterDeficitPerDay() {
    System.out.println("# Descripcion del metodo a probar");
    System.out.println("El metodo accumulateSoilMoistureDeficitPerDay() de la clase WaterMath acumula el deficit de humedad por dia. Si el resultado de acumular");
    System.out.println("el deficit de humedad por dia es un numero negativo (estrictamente menor a cero), retorna un numero negativo. Si el resultado de");
    System.out.println("acumular el deficit de humedad por dia es un numero positivo (estrictamente mayor a cero), retorna cero. Si el resultado de acumular");
    System.out.println("el deficit de humedad por dia es igual a cero, retorna cero.");
  }

  @AfterClass
  public static void postTest() {
    // Cierra las conexiones
    entityManager.close();
    entityManagerFactory.close();
  }

  /**
   * Imprime una tabla que contiene el dia, la precipitacion [mm/dia], la
   * ETc [mm/dia], el deficit de humedad de suelo [mm/dia] y el acumulado
   * del deficit de humedad de suelo [mm/dia] calculados a partir de un
   * conjunto de registros climaticos y de un conjunto de registros de
   * riego
   * 
   * @param presumedCurrentDate
   * @param climateRecords
   * @param irrigationRecords
   */
  private void printWaterBalanceTable(Calendar presumedCurrentDate, Collection<ClimateRecord> climateRecords, Collection<IrrigationRecord> irrigationRecords) {
    int day = 1;
    double soilMoistureDeficitPerDay = 0.0;
    double waterProvidedPerDay = 0.0;
    double totalIrrigationWaterPerDay = 0.0;
    double accumulatedSoilMoistureDeficitPerDay = 0.0;

    System.out.println("     Dia       " + "|" + "  Precip.  " + "|" + "    ETc   " + "|" + "  Def.  " + "|" + "  Acum. Def.  ");
    System.out.println("-----------------------------------------------------------");

    for (ClimateRecord currentClimateRecord : climateRecords) {
      totalIrrigationWaterPerDay = calculateTotalAmountIrrigationWaterPerDay(currentClimateRecord.getDate(), irrigationRecords);
      waterProvidedPerDay = currentClimateRecord.getPrecip() + totalIrrigationWaterPerDay;
      soilMoistureDeficitPerDay = WaterMath.calculateSoilMoistureDeficitPerDay(currentClimateRecord.getEtc(), currentClimateRecord.getPrecip(), totalIrrigationWaterPerDay);
      accumulatedSoilMoistureDeficitPerDay = WaterMath.accumulateSoilMoistureDeficitPerDay(soilMoistureDeficitPerDay, accumulatedSoilMoistureDeficitPerDay);

      System.out.print(" " + day + " (" + UtilDate.formatDate(currentClimateRecord.getDate()) + ")");
      System.out.print("  |");
      System.out.print("   " + waterProvidedPerDay);
      System.out.print("   |");
      System.out.print("   " + currentClimateRecord.getEtc());
      System.out.print("    |");
      System.out.print("  " + soilMoistureDeficitPerDay);
      System.out.print("  |");
      System.out.print("   " + accumulatedSoilMoistureDeficitPerDay);
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

    System.out.println("Precip. [mm/dia] = Precip. artificial y/o natural [mm/dia]");
    System.out.println("ETc [mm/dia]");
    System.out.println("Def. [mm/dia] = Deficit de humedad de suelo por dia (Lluvia - ETc) [mm/dia]");
    System.out.println("Acum. Def. [mm/dia] = Acumulado del deficit de humedad de suelo por dia de dias previos a una fecha [mm/dia]");
    System.out.println();
  }

  /**
   * Imprime la descripcion del metodo a probar, el cual en este
   * caso es el metodo calculateCropIrrigationWaterNeed() de
   * la clase WaterMath
   */
  private void printDescriptionMethodToTest() {
    System.out.println("# Descripcion del metodo a probar");
    System.out.println("El metodo calculateCropIrrigationWaterNeed() de la clase WaterMath calcula la necesidad de agua de riego de un cultivo en una fecha [mm/dia]");
    System.out.println("con base en la cantidad total de agua de riego de cultivo por dia de esa fecha [mm/dia] y el acumulado del deficit de humedad de suelo");
    System.out.println("por dia [mm/dia] de la fecha inmediatamente anterior a la fecha de la cantidad total de agua de riego de cultivo por dia. Si la suma");
    System.out.println("entre ambos valores es mayor o igual a cero, el valor devuelto por el metodo es cero. En cambio, si la suma entre ambos valores es");
    System.out.println("estrictamente menor a cero, el valor devuelto por el metodo es el valor absoluto de dicha suma. El motivo por el cual se realiza esta");
    System.out.println("suma es que la cantidad total de agua de riego de cultivo por dia es mayor o igual a cero y el acumulado del deficit de humedad de");
    System.out.println("suelo por dia es menor o igual a cero.");
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

  /**
   * Establece la ETc, la lluvia y la fecha de 6 registros
   * climaticos para la prueba unitaria doce
   */
  private void setClimateRecordsValuesTwelve() {
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
   * climaticos para la prueba unitaria trece
   */
  private void setClimateRecordsValuesThirteen() {
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
   * climaticos para la prueba unitaria catorce
   */
  private void setClimateRecordsValuesFourteen() {
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
   * @param climateRecords
   * @param irrigationRecords
   * @return double que representa el acumulado del deficit (falta)
   * de humedad de suelo [mm/dia] de un conjunto de dias
   */
  private static double calculateAccumulatedSoilMoistureDeficitPerDay(Collection<ClimateRecord> climateRecords, Collection<IrrigationRecord> irrigationRecords) {
    double totalIrrigationWaterPerDay = 0.0;
    double soilMoistureDeficitPerDay = 0.0;
    double accumulatedSoilMoistureDeficitPerDay = 0.0;

    /*
     * Acumula el deficit (falta) de humedad de suelo por dia [mm/dia]
     * de un conjunto de dias haciendo uso de un conjunto de registros
     * climaticos y un conjunto de registros de riego, debiendo ser
     * todos ellos pertenecientes a una misma parcela, ya que de lo
     * contrario el resultado sera incorrecto
     */
    for (ClimateRecord currentClimateRecord : climateRecords) {
      totalIrrigationWaterPerDay = calculateTotalAmountIrrigationWaterPerDay(currentClimateRecord.getDate(), irrigationRecords);

      /*
       * Calcula el deficit (falta) de humedad de suelo por dia [mm/dia]
       * de una fecha, ya que la ETc, la precipitacion y la cantidad total
       * de agua de riego pertenecen a una fecha.
       * 
       * ETc = evapotranspiracion del cultivo bajo condiciones estandar
       */
      soilMoistureDeficitPerDay = WaterMath.calculateSoilMoistureDeficitPerDay(currentClimateRecord.getEtc(), currentClimateRecord.getPrecip(), totalIrrigationWaterPerDay);
      accumulatedSoilMoistureDeficitPerDay = WaterMath.accumulateSoilMoistureDeficitPerDay(soilMoistureDeficitPerDay, accumulatedSoilMoistureDeficitPerDay);
    }

    return accumulatedSoilMoistureDeficitPerDay;
  }

  /**
   * @param date
   * @param irrigationRecords
   * @return double que representa la cantidad total de agua de
   * riego de una fecha [mm/dia]
   */
  private static double calculateTotalAmountIrrigationWaterPerDay(Calendar date, Collection<IrrigationRecord> irrigationRecords) {
    double totalIrrigationWaterGivenDate = 0.0;

    for (IrrigationRecord currentIrrigationRecord : irrigationRecords) {

      /*
       * Acumula el agua de riego de todos los registros de riego
       * pertenecientes a una parcela que tienen la misma fecha.
       * De esta manera, se calcula la cantidad total de agua de
       * riego de una fecha [mm/dia].
       */
      if (UtilDate.compareTo(currentIrrigationRecord.getDate(), date) == 0) {
        totalIrrigationWaterGivenDate = totalIrrigationWaterGivenDate + currentIrrigationRecord.getIrrigationDone();
      }

    }

    return totalIrrigationWaterGivenDate;
  }

}