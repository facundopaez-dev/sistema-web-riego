import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.ArrayList;
import java.util.Collection;
import model.ClimateRecord;
import model.IrrigationRecord;
import model.Parcel;
import model.Crop;
import model.Soil;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import irrigation.WaterMath;
import irrigation.WaterNeedIt;
import util.UtilDate;

public class WaterNeedItTest {

  private static Collection<IrrigationRecord> zeroIrrigationRecords;
  private static Collection<ClimateRecord> testClimateRecords;
  private static Collection<IrrigationRecord> testIrrigationRecords;
  private static Collection<ClimateRecord> climateRecordsToShow;

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
  private static ClimateRecord climateRecordSeven;

  private static IrrigationRecord irrigationRecordOne;
  private static IrrigationRecord irrigationRecordTwo;
  private static IrrigationRecord irrigationRecordThree;
  private static IrrigationRecord irrigationRecordFour;
  private static IrrigationRecord irrigationRecordFive;
  private static IrrigationRecord irrigationRecordSix;

  private static Crop testCrop;
  private static Soil testSoil;

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
    /*
     * Creacion de colecciones
     */
    zeroIrrigationRecords = new ArrayList<>();
    testClimateRecords = new ArrayList<>();
    testIrrigationRecords = new ArrayList<>();
    climateRecordsToShow = new ArrayList<>();

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

    /*
     * El valor de la ETc de este registro climatico es para
     * utilizarlo en el metodo calculateIrrigationWaterNeed
     * de la clase WaterNeedIt. Este registro climatico es
     * el registro climatico de la presunta fecha actual,
     * con lo cual su ETc es de la presunta fecha actual.
     * Esto es asi porque para calcular correctamente la
     * necesidad de agua de riego de un cultivo en la
     * presunta fecha actual se requiere ajustar el factor
     * de agotamiento (p) a la ETc de la presunta fecha
     * actual, con base al cual se calcula la lamina de
     * riego optima (drop), con base a la cual se calcula
     * la necesidad de agua de riego de un cultivo en la
     * presunta fecha actual.
     */
    climateRecordSeven = new ClimateRecord();
    climateRecordSeven.setDate(presumedCurrentDate);
    climateRecordSeven.setEtc(5);

    testClimateRecords.add(climateRecordOne);
    testClimateRecords.add(climateRecordTwo);
    testClimateRecords.add(climateRecordThree);
    testClimateRecords.add(climateRecordFour);
    testClimateRecords.add(climateRecordFive);
    testClimateRecords.add(climateRecordSix);

    /*
     * Esta coleccion contiene toddos los registros
     * climaticos de prueba para imprimir una tabla
     * que contiene el dia, la ETc por dia, el agua
     * (H2O) provista por dia y el acumulado del
     * deficit de agua por dia
     */
    climateRecordsToShow.add(climateRecordOne);
    climateRecordsToShow.add(climateRecordTwo);
    climateRecordsToShow.add(climateRecordThree);
    climateRecordsToShow.add(climateRecordFour);
    climateRecordsToShow.add(climateRecordFive);
    climateRecordsToShow.add(climateRecordSix);
    climateRecordsToShow.add(climateRecordSeven);

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

    testCrop = new Crop();
    testCrop.setName("Lechuga");
    testCrop.setLowerLimitMaximumRootDepth(0.3);
    testCrop.setUpperLimitMaximumRootDepth(0.5);
    testCrop.setDepletionFactor(0.30);

    testSoil = new Soil();
    testSoil.setName("Arenoso");
    testSoil.setApparentSpecificWeight(1.65);
    testSoil.setFieldCapacity(9);
    testSoil.setPermanentWiltingPoint(4);
  }

  /*
   * Las pruebas unitarias estan escritas en el siguiente orden:
   * 1. Registros climaticos previos a una fecha con precipitacion > 0, registros de riego previos a una fecha con riego realizado == 0
   * (para esto se puede utilizar una coleccion vacia de registros de riego, lo cual es igual a tener una coleccion de registros de riego
   * que tienen riego realizado == 0), acumulado del deficit de agua por dia de dias previos a una fecha estrictamente menor a la lamina
   * de riego optima (drop) negativa de una fecha y cantidad total de agua de riego de una fecha == 0.
   * 
   * 2. Registros climaticos previos a una fecha con precipitacion > 0, registros de riego previos a una fecha con riego realizado == 0
   * (para esto se puede utilizar una coleccion vacia de registros de riego, lo cual es igual a tener una coleccion de registros de riego
   * que tienen riego realizado == 0), acumulado del deficit de agua por dia de dias previos a una fecha igual a la lamina de riego optima
   * (drop) negativa de una fecha y cantidad total de agua de riego de una fecha == 0.
   * 
   * 3. Registros climaticos previos a una fecha con precipitacion > 0, registros de riego previos a una fecha con riego realizado == 0
   * (para esto se puede utilizar una coleccion vacia de registros de riego, lo cual es igual a tener una coleccion de registros de riego
   * que tienen riego realizado == 0), acumulado del deficit de agua por dia de dias previos a una fecha estrictamente mayor a la lamina
   * de riego optima (drop) negativa de una fecha y estrictamente menor a la capacidad de campo (0 [mm]) del suelo y cantidad total de
   * agua de riego de una fecha == 0.
   * 
   * 4. Registros climaticos previos a una fecha con precipitacion > 0, registros de riego previos a una fecha con riego realizado == 0
   * (para esto se puede utilizar una coleccion vacia de registros de riego, lo cual es igual a tener una coleccion de registros de riego
   * que tienen riego realizado == 0), acumulado del deficit de agua por dia de dias previos a una fecha igual a la capacidad de campo
   * (0 [mm]) del suelo y cantidad total de agua de riego de una fecha == 0.
   * 
   * 5. Una de estas cuatro pruebas unitarias debe ser realizada con registros climaticos previos a una fecha con precipitacion == 0 y
   * registros de riego previos a una fecha con riego realizado > 0. Esta prueba es para demostrar que la necesidad de agua de riego de
   * un cultivo en una fecha es calculada correctamente en el caso en el que se tiene una coleccion de registros climaticos previos a
   * una fecha con precipitacion == 0 y una coleccion de registros de riego previos a una fecha con riego realizado > 0. Con realizar
   * solo una prueba de este tipo es suficiente para realizar tal demostracion.
   * 
   * 6. Registros climaticos previos a una fecha con precipitacion > 0, registros de riego previos a una fecha con riego realizado == 0
   * (para esto se puede utilizar una coleccion vacia de registros de riego, lo cual es igual a tener una coleccion de registros de riego
   * que tienen riego realizado == 0), acumulado del deficit de agua por dia de dias previos a una fecha estrictamente menor a la lamina
   * de riego optima (drop) negativa de una fecha y cantidad total de agua de riego de una fecha > 0.
   * 
   * 7. Registros climaticos previos a una fecha con precipitacion > 0, registros de riego previos a una fecha con riego realizado == 0
   * (para esto se puede utilizar una coleccion vacia de registros de riego, lo cual es igual a tener una coleccion de registros de riego
   * que tienen riego realizado == 0), acumulado del deficit de agua por dia de dias previos a una fecha igual a la lamina de riego optima
   * (drop) negativa de una fecha y cantidad total de agua de riego de una fecha > 0.
   * 
   * 8. Registros climaticos previos a una fecha con precipitacion > 0, registros de riego previos a una fecha con riego realizado == 0
   * (para esto se puede utilizar una coleccion vacia de registros de riego, lo cual es igual a tener una coleccion de registros de riego
   * que tienen riego realizado == 0), acumulado del deficit de agua por dia de dias previos a una fecha estrictamente mayor a la lamina
   * de riego optima (drop) negativa de una fecha y estrictamente menor a la capacidad de campo (0 [mm]) del suelo, cantidad total de agua
   * de riego de una fecha > 0 y suma entre el acumulado del deficit de agua por dia de dias previos a una fecha [mm/dia] y la cantidad
   * total de agua de riego de una fecha [mm/dia] estrictamente menor a la capacidad de campo (0 [mm]) del suelo.
   * 
   * 9. Registros climaticos previos a una fecha con precipitacion > 0, registros de riego previos a una fecha con riego realizado == 0
   * (para esto se puede utilizar una coleccion vacia de registros de riego, lo cual es igual a tener una coleccion de registros de riego
   * que tienen riego realizado == 0), acumulado del deficit de agua por dia de dias previos a una fecha igual a la capacidad de campo
   * (0 [mm]) del suelo y cantidad total de agua de riego de una fecha > 0.
   * 
   * 10. Registros climaticos previos a una fecha con precipitacion > 0, registros de riego previos a una fecha con riego realizado == 0
   * (para esto se puede utilizar una coleccion vacia de registros de riego, lo cual es igual a tener una coleccion de registros de riego
   * que tienen riego realizado == 0), acumulado del deficit de agua por dia de dias previos a una fecha estrictamente mayor a la lamina
   * de riego optima (drop) de una fecha y estrictamente menor a la capacidad de campo (0 [mm]) del suelo, cantidad total de agua de riego
   * de una fecha > 0 y suma entre el acumulado del deficit de agua por dia de dias previos a una fecha [mm/dia] y la cantidad total de
   * agua de riego de una fecha [mm/dia] mayor o igual a la capacidad de campo (0 [mm]) del suelo.
   * 
   * 11. Se debe realizar una de estas pruebas unitarias con registros climaticos previos a una fecha con precipitacion > 0, registros de
   * riego previos a una fecha con riego realizado > 0 y cantidad total de agua de riego de una fecha == 0.
   * 
   * 12. Se debe realizar una de estas pruebas unitarias con registros climaticos previos a una fecha con precipitacion > 0, registros de
   * riego previos a una fecha con riego realizado > 0 y cantidad total de agua de riego de una fecha > 0.
   * 
   * Que el acumulado del deficit de agua por dia de dias previos a una fecha [mm/dia] sea igual a la capacidad de campo (0 [mm]) de un
   * suelo, que tiene un cultivo sembrado, significa que el suelo esta lleno de agua, y quiza anegado.
   * Que la suma entre el acumulado del deficit de agua por dia de dias previos a una fecha [mm/dia] y la cantidad total de agua de riego
   * de una fecha [mm/dia] sea mayor o igual a la capacidad de campo (0 [mm]) de un suelo, que tiene un cultivo sembrado, significa que
   * el suelo esta lleno de agua, y quiza anegado.
   * 
   * La frase 'y quiza anegado' se debe a que, si a un suelo que esta lleno de agua, se le agrega mas agua, la misma se escurre, pero
   * dependiendo de la cantidad de agua que se le agregue a un suelo lleno de agua, puede suceder que se anegue.
   */

  @Test
  public void testOneCalculateIrrigationWaterNeed() {
    System.out.println("**************************************** Prueba uno del metodo calculateIrrigationWaterNeed *****************************************");
    printDescriptionMethodToTest();

    System.out.println("# Descripcion de la prueba unitaria");

    /*
     * Establece los datos de los registros para
     * esta prueba
     */
    setRecordsValuesOne();

    System.out.println("Los datos con los que se calculara la necesidad de agua de riego de un cultivo en la fecha actual (" + UtilDate.formatDate(presumedCurrentDate) + ") son los siguientes:");
    System.out.println();
    printTestCrop(testCrop);
    System.out.println();
    printTestSoil(testSoil);
    System.out.println();

    printWaterBalanceTable(presumedCurrentDate, climateRecordsToShow, zeroIrrigationRecords);

    double totalIrrigationWaterCurrentDate = 0.0;
    double accumulatedWaterDeficitPerDay = WaterMath.calculateAccumulatedWaterDeficitPerDay(testClimateRecords, zeroIrrigationRecords);
    double optimalIrrigationLayer = WaterNeedIt.calculateOptimalIrrigationLayer(climateRecordSeven.getEtc(), testCrop, testSoil);
    double totalAmountWaterAvailable = WaterNeedIt.calculateTotalAmountWaterAvailable(testCrop, testSoil);

    printAccumulatedAmountRainWater(presumedCurrentDate, testClimateRecords);
    printAccumulatedAmountIrrigationWater(presumedCurrentDate, zeroIrrigationRecords);
    printTotalAmountWaterAvailable(totalAmountWaterAvailable);
    System.out.println();
    printTotalIrrigationWaterCurrentDate(presumedCurrentDate, totalIrrigationWaterCurrentDate);
    printAccumulatedWaterDeficitPerDay(presumedCurrentDate, accumulatedWaterDeficitPerDay);
    printOptimalIrrigationLayer(optimalIrrigationLayer);

    System.out.println();
    printMessageSituationOne();
    System.out.println();

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");

    /*
     * Al metodo calculateIrrigationWaterNeed de la clase
     * WaterNeedIt se le pasa como argumento la ETc de la
     * presunta fecha actual porque el registro climatico
     * siete tiene la presunta fecha actual. El motivo de
     * este pasaje es que, para calcular correctamente la
     * necesidad de agua de riego de un cultivo en la
     * presunta fecha actual, se requiere ajustar el
     * factor de agotamiento (p) a la ETc de la presunta
     * fecha actual, con base a la cual se calcula la
     * lamina de riego optima (drop) de la presunta
     * fecha actual, con base a la cual se calcula la
     * necesidad de agua de riego de un cultivo en la
     * presunta fecha actual.
     */
    double expectedResult = optimalIrrigationLayer;
    double result = WaterNeedIt.calculateIrrigationWaterNeed(totalIrrigationWaterCurrentDate,
        climateRecordSeven.getEtc(), testCrop, testSoil, testClimateRecords, zeroIrrigationRecords);

    System.out.println("* Valor esperado (nec. agua riego de un cultivo en la fecha actual [mm/dia]): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateIrrigationWaterNeed (nec. agua riego");
    System.out.println("de un cultivo en fecha actual [mm/dia]): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testTwoCalculateIrrigationWaterNeed() {
    System.out.println("**************************************** Prueba dos del metodo calculateIrrigationWaterNeed *****************************************");
    printDescriptionMethodToTest();

    System.out.println("# Descripcion de la prueba unitaria");

    /*
     * Establece los datos de los registros para
     * esta prueba
     */
    setRecordsValuesTwo();

    System.out.println("Los datos con los que se calculara la necesidad de agua de riego de un cultivo en la fecha actual (" + UtilDate.formatDate(presumedCurrentDate) + ") son los siguientes:");
    System.out.println();
    printTestCrop(testCrop);
    System.out.println();
    printTestSoil(testSoil);
    System.out.println();

    printWaterBalanceTable(presumedCurrentDate, climateRecordsToShow, zeroIrrigationRecords);

    double totalIrrigationWaterCurrentDate = 0.0;
    double accumulatedWaterDeficitPerDay = WaterMath.calculateAccumulatedWaterDeficitPerDay(testClimateRecords, zeroIrrigationRecords);
    double optimalIrrigationLayer = WaterNeedIt.calculateOptimalIrrigationLayer(climateRecordSeven.getEtc(), testCrop, testSoil);
    double totalAmountWaterAvailable = WaterNeedIt.calculateTotalAmountWaterAvailable(testCrop, testSoil);

    printAccumulatedAmountRainWater(presumedCurrentDate, testClimateRecords);
    printAccumulatedAmountIrrigationWater(presumedCurrentDate, zeroIrrigationRecords);
    printTotalAmountWaterAvailable(totalAmountWaterAvailable);
    System.out.println();
    printTotalIrrigationWaterCurrentDate(presumedCurrentDate, totalIrrigationWaterCurrentDate);
    printAccumulatedWaterDeficitPerDay(presumedCurrentDate, accumulatedWaterDeficitPerDay);
    printOptimalIrrigationLayer(optimalIrrigationLayer);

    System.out.println();
    printMessageSituationOne();
    System.out.println();

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");

    /*
     * Al metodo calculateIrrigationWaterNeed de la clase
     * WaterNeedIt se le pasa como argumento la ETc de la
     * presunta fecha actual porque el registro climatico
     * siete tiene la presunta fecha actual. El motivo de
     * este pasaje es que, para calcular correctamente la
     * necesidad de agua de riego de un cultivo en la
     * presunta fecha actual, se requiere ajustar el
     * factor de agotamiento (p) a la ETc de la presunta
     * fecha actual, con base a la cual se calcula la
     * lamina de riego optima (drop) de la presunta
     * fecha actual, con base a la cual se calcula la
     * necesidad de agua de riego de un cultivo en la
     * presunta fecha actual.
     */
    double expectedResult = optimalIrrigationLayer;
    double result = WaterNeedIt.calculateIrrigationWaterNeed(totalIrrigationWaterCurrentDate,
        climateRecordSeven.getEtc(), testCrop, testSoil, testClimateRecords, zeroIrrigationRecords);

    System.out.println("* Valor esperado (nec. agua riego de un cultivo en la fecha actual [mm/dia]): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateIrrigationWaterNeed (nec. agua riego");
    System.out.println("de un cultivo en fecha actual [mm/dia]): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testThreeCalculateIrrigationWaterNeed() {
    System.out.println("**************************************** Prueba tres del metodo calculateIrrigationWaterNeed *****************************************");
    printDescriptionMethodToTest();

    System.out.println("# Descripcion de la prueba unitaria");

    /*
     * Establece los datos de los registros para
     * esta prueba
     */
    setRecordsValuesThree();

    System.out.println("Los datos con los que se calculara la necesidad de agua de riego de un cultivo en la fecha actual (" + UtilDate.formatDate(presumedCurrentDate) + ") son los siguientes:");
    System.out.println();
    printTestCrop(testCrop);
    System.out.println();
    printTestSoil(testSoil);
    System.out.println();

    printWaterBalanceTable(presumedCurrentDate, climateRecordsToShow, zeroIrrigationRecords);

    double totalIrrigationWaterCurrentDate = 0.0;
    double accumulatedWaterDeficitPerDay = WaterMath.calculateAccumulatedWaterDeficitPerDay(testClimateRecords, zeroIrrigationRecords);
    double optimalIrrigationLayer = WaterNeedIt.calculateOptimalIrrigationLayer(climateRecordSeven.getEtc(), testCrop, testSoil);
    double totalAmountWaterAvailable = WaterNeedIt.calculateTotalAmountWaterAvailable(testCrop, testSoil);

    printAccumulatedAmountRainWater(presumedCurrentDate, testClimateRecords);
    printAccumulatedAmountIrrigationWater(presumedCurrentDate, zeroIrrigationRecords);
    printTotalAmountWaterAvailable(totalAmountWaterAvailable);
    System.out.println();
    printTotalIrrigationWaterCurrentDate(presumedCurrentDate, totalIrrigationWaterCurrentDate);
    printAccumulatedWaterDeficitPerDay(presumedCurrentDate, accumulatedWaterDeficitPerDay);
    printOptimalIrrigationLayer(optimalIrrigationLayer);

    System.out.println();
    printMessageSituationTwo();
    System.out.println();

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");

    /*
     * Al metodo calculateIrrigationWaterNeed de la clase
     * WaterNeedIt se le pasa como argumento la ETc de la
     * presunta fecha actual porque el registro climatico
     * siete tiene la presunta fecha actual. El motivo de
     * este pasaje es que, para calcular correctamente la
     * necesidad de agua de riego de un cultivo en la
     * presunta fecha actual, se requiere ajustar el
     * factor de agotamiento (p) a la ETc de la presunta
     * fecha actual, con base a la cual se calcula la
     * lamina de riego optima (drop) de la presunta
     * fecha actual, con base a la cual se calcula la
     * necesidad de agua de riego de un cultivo en la
     * presunta fecha actual.
     */
    double expectedResult = Math.abs(accumulatedWaterDeficitPerDay);
    double result = WaterNeedIt.calculateIrrigationWaterNeed(totalIrrigationWaterCurrentDate,
        climateRecordSeven.getEtc(), testCrop, testSoil, testClimateRecords, zeroIrrigationRecords);

    System.out.println("* Valor esperado (nec. agua riego de un cultivo en la fecha actual [mm/dia]): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateIrrigationWaterNeed (nec. agua riego");
    System.out.println("de un cultivo en fecha actual [mm/dia]): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testFourCalculateIrrigationWaterNeed() {
    System.out.println("**************************************** Prueba cuatro del metodo calculateIrrigationWaterNeed *****************************************");
    printDescriptionMethodToTest();

    System.out.println("# Descripcion de la prueba unitaria");

    /*
     * Establece los datos de los registros para
     * esta prueba
     */
    setRecordsValuesFour();

    System.out.println("Los datos con los que se calculara la necesidad de agua de riego de un cultivo en la fecha actual (" + UtilDate.formatDate(presumedCurrentDate) + ") son los siguientes:");
    System.out.println();
    printTestCrop(testCrop);
    System.out.println();
    printTestSoil(testSoil);
    System.out.println();

    printWaterBalanceTable(presumedCurrentDate, climateRecordsToShow, zeroIrrigationRecords);

    double totalIrrigationWaterCurrentDate = 0.0;
    double accumulatedWaterDeficitPerDay = WaterMath.calculateAccumulatedWaterDeficitPerDay(testClimateRecords, zeroIrrigationRecords);
    double optimalIrrigationLayer = WaterNeedIt.calculateOptimalIrrigationLayer(climateRecordSeven.getEtc(), testCrop, testSoil);
    double totalAmountWaterAvailable = WaterNeedIt.calculateTotalAmountWaterAvailable(testCrop, testSoil);

    printAccumulatedAmountRainWater(presumedCurrentDate, testClimateRecords);
    printAccumulatedAmountIrrigationWater(presumedCurrentDate, zeroIrrigationRecords);
    printTotalAmountWaterAvailable(totalAmountWaterAvailable);
    System.out.println();
    printTotalIrrigationWaterCurrentDate(presumedCurrentDate, totalIrrigationWaterCurrentDate);
    printAccumulatedWaterDeficitPerDay(presumedCurrentDate, accumulatedWaterDeficitPerDay);
    printOptimalIrrigationLayer(optimalIrrigationLayer);

    System.out.println();
    printMessageSituationThree();
    System.out.println();

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");

    /*
     * Al metodo calculateIrrigationWaterNeed de la clase
     * WaterNeedIt se le pasa como argumento la ETc de la
     * presunta fecha actual porque el registro climatico
     * siete tiene la presunta fecha actual. El motivo de
     * este pasaje es que, para calcular correctamente la
     * necesidad de agua de riego de un cultivo en la
     * presunta fecha actual, se requiere ajustar el
     * factor de agotamiento (p) a la ETc de la presunta
     * fecha actual, con base a la cual se calcula la
     * lamina de riego optima (drop) de la presunta
     * fecha actual, con base a la cual se calcula la
     * necesidad de agua de riego de un cultivo en la
     * presunta fecha actual.
     */
    double expectedResult = 0;
    double result = WaterNeedIt.calculateIrrigationWaterNeed(totalIrrigationWaterCurrentDate,
        climateRecordSeven.getEtc(), testCrop, testSoil, testClimateRecords, zeroIrrigationRecords);

    System.out.println("* Valor esperado (nec. agua riego de un cultivo en la fecha actual [mm/dia]): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateIrrigationWaterNeed (nec. agua riego");
    System.out.println("de un cultivo en fecha actual [mm/dia]): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testFiveCalculateIrrigationWaterNeed() {
    System.out.println("**************************************** Prueba cinco del metodo calculateIrrigationWaterNeed *****************************************");
    printDescriptionMethodToTest();

    System.out.println("# Descripcion de la prueba unitaria");

    /*
     * Establece los datos de los registros para
     * esta prueba
     */
    setRecordsValuesFive();

    System.out.println("Los datos con los que se calculara la necesidad de agua de riego de un cultivo en la fecha actual (" + UtilDate.formatDate(presumedCurrentDate) + ") son los siguientes:");
    System.out.println();
    printTestCrop(testCrop);
    System.out.println();
    printTestSoil(testSoil);
    System.out.println();

    System.out.println();
    printWaterBalanceTable(presumedCurrentDate, climateRecordsToShow, testIrrigationRecords);

    double totalIrrigationWaterCurrentDate = 0.0;
    double accumulatedWaterDeficitPerDay = WaterMath.calculateAccumulatedWaterDeficitPerDay(testClimateRecords, testIrrigationRecords);
    double optimalIrrigationLayer = WaterNeedIt.calculateOptimalIrrigationLayer(climateRecordSeven.getEtc(), testCrop, testSoil);
    double totalAmountWaterAvailable = WaterNeedIt.calculateTotalAmountWaterAvailable(testCrop, testSoil);

    printAccumulatedAmountRainWater(presumedCurrentDate, testClimateRecords);
    printAccumulatedAmountIrrigationWater(presumedCurrentDate, testIrrigationRecords);
    printTotalAmountWaterAvailable(totalAmountWaterAvailable);
    System.out.println();
    printTotalIrrigationWaterCurrentDate(presumedCurrentDate, totalIrrigationWaterCurrentDate);
    printAccumulatedWaterDeficitPerDay(presumedCurrentDate, accumulatedWaterDeficitPerDay);
    printOptimalIrrigationLayer(optimalIrrigationLayer);

    System.out.println();
    printMessageSituationOne();
    System.out.println();

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");

    /*
     * Al metodo calculateIrrigationWaterNeed de la clase
     * WaterNeedIt se le pasa como argumento la ETc de la
     * presunta fecha actual porque el registro climatico
     * siete tiene la presunta fecha actual. El motivo de
     * este pasaje es que, para calcular correctamente la
     * necesidad de agua de riego de un cultivo en la
     * presunta fecha actual, se requiere ajustar el
     * factor de agotamiento (p) a la ETc de la presunta
     * fecha actual, con base a la cual se calcula la
     * lamina de riego optima (drop) de la presunta
     * fecha actual, con base a la cual se calcula la
     * necesidad de agua de riego de un cultivo en la
     * presunta fecha actual.
     */
    double expectedResult = optimalIrrigationLayer;
    double result = WaterNeedIt.calculateIrrigationWaterNeed(totalIrrigationWaterCurrentDate,
        climateRecordSeven.getEtc(), testCrop, testSoil, testClimateRecords, testIrrigationRecords);

    System.out.println("* Valor esperado (nec. agua riego de un cultivo en la fecha actual [mm/dia]): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateIrrigationWaterNeed (nec. agua riego");
    System.out.println("de un cultivo en fecha actual [mm/dia]): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testSixCalculateIrrigationWaterNeed() {
    System.out.println("**************************************** Prueba seis del metodo calculateIrrigationWaterNeed *****************************************");
    printDescriptionMethodToTest();

    System.out.println("# Descripcion de la prueba unitaria");

    /*
     * Establece los datos de los registros para
     * esta prueba
     */
    setRecordsValuesSix();

    System.out.println("Los datos con los que se calculara la necesidad de agua de riego de un cultivo en la fecha actual (" + UtilDate.formatDate(presumedCurrentDate) + ") son los siguientes:");
    System.out.println();
    printTestCrop(testCrop);
    System.out.println();
    printTestSoil(testSoil);
    System.out.println();

    printWaterBalanceTable(presumedCurrentDate, climateRecordsToShow, zeroIrrigationRecords);

    double totalIrrigationWaterCurrentDate = 3.9;
    double accumulatedWaterDeficitPerDay = WaterMath.calculateAccumulatedWaterDeficitPerDay(testClimateRecords, zeroIrrigationRecords);
    double optimalIrrigationLayer = WaterNeedIt.calculateOptimalIrrigationLayer(climateRecordSeven.getEtc(), testCrop, testSoil);
    double totalAmountWaterAvailable = WaterNeedIt.calculateTotalAmountWaterAvailable(testCrop, testSoil);

    printAccumulatedAmountRainWater(presumedCurrentDate, testClimateRecords);
    printAccumulatedAmountIrrigationWater(presumedCurrentDate, zeroIrrigationRecords);
    printTotalAmountWaterAvailable(totalAmountWaterAvailable);
    System.out.println();
    printTotalIrrigationWaterCurrentDate(presumedCurrentDate, totalIrrigationWaterCurrentDate);
    printAccumulatedWaterDeficitPerDay(presumedCurrentDate, accumulatedWaterDeficitPerDay);
    printOptimalIrrigationLayer(optimalIrrigationLayer);

    System.out.println();
    printMessageSituationFour();
    System.out.println();

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");

    /*
     * Al metodo calculateIrrigationWaterNeed de la clase
     * WaterNeedIt se le pasa como argumento la ETc de la
     * presunta fecha actual porque el registro climatico
     * siete tiene la presunta fecha actual. El motivo de
     * este pasaje es que, para calcular correctamente la
     * necesidad de agua de riego de un cultivo en la
     * presunta fecha actual, se requiere ajustar el
     * factor de agotamiento (p) a la ETc de la presunta
     * fecha actual, con base a la cual se calcula la
     * lamina de riego optima (drop) de la presunta
     * fecha actual, con base a la cual se calcula la
     * necesidad de agua de riego de un cultivo en la
     * presunta fecha actual.
     */
    double expectedResult = optimalIrrigationLayer - totalIrrigationWaterCurrentDate;
    double result = WaterNeedIt.calculateIrrigationWaterNeed(totalIrrigationWaterCurrentDate,
        climateRecordSeven.getEtc(), testCrop, testSoil, testClimateRecords, zeroIrrigationRecords);

    System.out.println("* Valor esperado (nec. agua riego de un cultivo en la fecha actual [mm/dia]): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateIrrigationWaterNeed (nec. agua riego");
    System.out.println("de un cultivo en fecha actual [mm/dia]): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testSevenCalculateIrrigationWaterNeed() {
    System.out.println("**************************************** Prueba siete del metodo calculateIrrigationWaterNeed *****************************************");
    printDescriptionMethodToTest();

    System.out.println("# Descripcion de la prueba unitaria");

    /*
     * Establece los datos de los registros para
     * esta prueba
     */
    setRecordsValuesSeven();

    System.out.println("Los datos con los que se calculara la necesidad de agua de riego de un cultivo en la fecha actual (" + UtilDate.formatDate(presumedCurrentDate) + ") son los siguientes:");
    System.out.println();
    printTestCrop(testCrop);
    System.out.println();
    printTestSoil(testSoil);
    System.out.println();

    printWaterBalanceTable(presumedCurrentDate, climateRecordsToShow, zeroIrrigationRecords);

    double totalIrrigationWaterCurrentDate = 1.9;
    double accumulatedWaterDeficitPerDay = WaterMath.calculateAccumulatedWaterDeficitPerDay(testClimateRecords, zeroIrrigationRecords);
    double optimalIrrigationLayer = WaterNeedIt.calculateOptimalIrrigationLayer(climateRecordSeven.getEtc(), testCrop, testSoil);
    double totalAmountWaterAvailable = WaterNeedIt.calculateTotalAmountWaterAvailable(testCrop, testSoil);

    printAccumulatedAmountRainWater(presumedCurrentDate, testClimateRecords);
    printAccumulatedAmountIrrigationWater(presumedCurrentDate, zeroIrrigationRecords);
    printTotalAmountWaterAvailable(totalAmountWaterAvailable);
    System.out.println();
    printTotalIrrigationWaterCurrentDate(presumedCurrentDate, totalIrrigationWaterCurrentDate);
    printAccumulatedWaterDeficitPerDay(presumedCurrentDate, accumulatedWaterDeficitPerDay);
    printOptimalIrrigationLayer(optimalIrrigationLayer);

    System.out.println();
    printMessageSituationFour();
    System.out.println();

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");

    /*
     * Al metodo calculateIrrigationWaterNeed de la clase
     * WaterNeedIt se le pasa como argumento la ETc de la
     * presunta fecha actual porque el registro climatico
     * siete tiene la presunta fecha actual. El motivo de
     * este pasaje es que, para calcular correctamente la
     * necesidad de agua de riego de un cultivo en la
     * presunta fecha actual, se requiere ajustar el
     * factor de agotamiento (p) a la ETc de la presunta
     * fecha actual, con base a la cual se calcula la
     * lamina de riego optima (drop) de la presunta
     * fecha actual, con base a la cual se calcula la
     * necesidad de agua de riego de un cultivo en la
     * presunta fecha actual.
     */
    double expectedResult = optimalIrrigationLayer - totalIrrigationWaterCurrentDate;
    double result = WaterNeedIt.calculateIrrigationWaterNeed(totalIrrigationWaterCurrentDate,
        climateRecordSeven.getEtc(), testCrop, testSoil, testClimateRecords, zeroIrrigationRecords);

    System.out.println("* Valor esperado (nec. agua riego de un cultivo en la fecha actual [mm/dia]): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateIrrigationWaterNeed (nec. agua riego");
    System.out.println("de un cultivo en fecha actual [mm/dia]): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testEightCalculateIrrigationWaterNeed() {
    System.out.println("**************************************** Prueba ocho del metodo calculateIrrigationWaterNeed *****************************************");
    printDescriptionMethodToTest();

    System.out.println("# Descripcion de la prueba unitaria");

    /*
     * Establece los datos de los registros para
     * esta prueba
     */
    setRecordsValuesEight();

    System.out.println("Los datos con los que se calculara la necesidad de agua de riego de un cultivo en la fecha actual (" + UtilDate.formatDate(presumedCurrentDate) + ") son los siguientes:");
    System.out.println();
    printTestCrop(testCrop);
    System.out.println();
    printTestSoil(testSoil);
    System.out.println();

    printWaterBalanceTable(presumedCurrentDate, climateRecordsToShow, zeroIrrigationRecords);

    double totalIrrigationWaterCurrentDate = 2;
    double accumulatedWaterDeficitPerDay = WaterMath.calculateAccumulatedWaterDeficitPerDay(testClimateRecords, zeroIrrigationRecords);
    double optimalIrrigationLayer = WaterNeedIt.calculateOptimalIrrigationLayer(climateRecordSeven.getEtc(), testCrop, testSoil);
    double totalAmountWaterAvailable = WaterNeedIt.calculateTotalAmountWaterAvailable(testCrop, testSoil);

    printAccumulatedAmountRainWater(presumedCurrentDate, testClimateRecords);
    printAccumulatedAmountIrrigationWater(presumedCurrentDate, zeroIrrigationRecords);
    printTotalAmountWaterAvailable(totalAmountWaterAvailable);
    System.out.println();
    printTotalIrrigationWaterCurrentDate(presumedCurrentDate, totalIrrigationWaterCurrentDate);
    printAccumulatedWaterDeficitPerDay(presumedCurrentDate, accumulatedWaterDeficitPerDay);
    printOptimalIrrigationLayer(optimalIrrigationLayer);

    System.out.println();
    printMessageSituationFive();
    System.out.println();

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");

    /*
     * Al metodo calculateIrrigationWaterNeed de la clase
     * WaterNeedIt se le pasa como argumento la ETc de la
     * presunta fecha actual porque el registro climatico
     * siete tiene la presunta fecha actual. El motivo de
     * este pasaje es que, para calcular correctamente la
     * necesidad de agua de riego de un cultivo en la
     * presunta fecha actual, se requiere ajustar el
     * factor de agotamiento (p) a la ETc de la presunta
     * fecha actual, con base a la cual se calcula la
     * lamina de riego optima (drop) de la presunta
     * fecha actual, con base a la cual se calcula la
     * necesidad de agua de riego de un cultivo en la
     * presunta fecha actual.
     */
    double expectedResult = Math.abs(accumulatedWaterDeficitPerDay + totalIrrigationWaterCurrentDate);
    double result = WaterNeedIt.calculateIrrigationWaterNeed(totalIrrigationWaterCurrentDate,
        climateRecordSeven.getEtc(), testCrop, testSoil, testClimateRecords, zeroIrrigationRecords);

    System.out.println("* Valor esperado (nec. agua riego de un cultivo en la fecha actual [mm/dia]): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateIrrigationWaterNeed (nec. agua riego");
    System.out.println("de un cultivo en fecha actual [mm/dia]): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testNineCalculateIrrigationWaterNeed() {
    System.out.println("**************************************** Prueba nueve del metodo calculateIrrigationWaterNeed *****************************************");
    printDescriptionMethodToTest();

    System.out.println("# Descripcion de la prueba unitaria");

    /*
     * Establece los datos de los registros para
     * esta prueba
     */
    setRecordsValuesNine();

    System.out.println("Los datos con los que se calculara la necesidad de agua de riego de un cultivo en la fecha actual (" + UtilDate.formatDate(presumedCurrentDate) + ") son los siguientes:");
    System.out.println();
    printTestCrop(testCrop);
    System.out.println();
    printTestSoil(testSoil);
    System.out.println();

    printWaterBalanceTable(presumedCurrentDate, climateRecordsToShow, zeroIrrigationRecords);

    double totalIrrigationWaterCurrentDate = 1;
    double accumulatedWaterDeficitPerDay = WaterMath.calculateAccumulatedWaterDeficitPerDay(testClimateRecords, zeroIrrigationRecords);
    double optimalIrrigationLayer = WaterNeedIt.calculateOptimalIrrigationLayer(climateRecordSeven.getEtc(), testCrop, testSoil);
    double totalAmountWaterAvailable = WaterNeedIt.calculateTotalAmountWaterAvailable(testCrop, testSoil);

    printAccumulatedAmountRainWater(presumedCurrentDate, testClimateRecords);
    printAccumulatedAmountIrrigationWater(presumedCurrentDate, zeroIrrigationRecords);
    printTotalAmountWaterAvailable(totalAmountWaterAvailable);
    System.out.println();
    printTotalIrrigationWaterCurrentDate(presumedCurrentDate, totalIrrigationWaterCurrentDate);
    printAccumulatedWaterDeficitPerDay(presumedCurrentDate, accumulatedWaterDeficitPerDay);
    printOptimalIrrigationLayer(optimalIrrigationLayer);

    System.out.println();
    printMessageSituationThree();
    System.out.println();

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");

    /*
     * Al metodo calculateIrrigationWaterNeed de la clase
     * WaterNeedIt se le pasa como argumento la ETc de la
     * presunta fecha actual porque el registro climatico
     * siete tiene la presunta fecha actual. El motivo de
     * este pasaje es que, para calcular correctamente la
     * necesidad de agua de riego de un cultivo en la
     * presunta fecha actual, se requiere ajustar el
     * factor de agotamiento (p) a la ETc de la presunta
     * fecha actual, con base a la cual se calcula la
     * lamina de riego optima (drop) de la presunta
     * fecha actual, con base a la cual se calcula la
     * necesidad de agua de riego de un cultivo en la
     * presunta fecha actual.
     */
    double expectedResult = 0;
    double result = WaterNeedIt.calculateIrrigationWaterNeed(totalIrrigationWaterCurrentDate,
        climateRecordSeven.getEtc(), testCrop, testSoil, testClimateRecords, zeroIrrigationRecords);

    System.out.println("* Valor esperado (nec. agua riego de un cultivo en la fecha actual [mm/dia]): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateIrrigationWaterNeed (nec. agua riego");
    System.out.println("de un cultivo en fecha actual [mm/dia]): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testTenCalculateIrrigationWaterNeed() {
    System.out.println("**************************************** Prueba diez del metodo calculateIrrigationWaterNeed *****************************************");
    printDescriptionMethodToTest();

    System.out.println("# Descripcion de la prueba unitaria");

    /*
     * Establece los datos de los registros para
     * esta prueba
     */
    setRecordsValuesTen();

    System.out.println("Los datos con los que se calculara la necesidad de agua de riego de un cultivo en la fecha actual (" + UtilDate.formatDate(presumedCurrentDate) + ") son los siguientes:");
    System.out.println();
    printTestCrop(testCrop);
    System.out.println();
    printTestSoil(testSoil);
    System.out.println();

    printWaterBalanceTable(presumedCurrentDate, climateRecordsToShow, zeroIrrigationRecords);

    double totalIrrigationWaterCurrentDate = 5;
    double accumulatedWaterDeficitPerDay = WaterMath.calculateAccumulatedWaterDeficitPerDay(testClimateRecords, zeroIrrigationRecords);
    double optimalIrrigationLayer = WaterNeedIt.calculateOptimalIrrigationLayer(climateRecordSeven.getEtc(), testCrop, testSoil);
    double totalAmountWaterAvailable = WaterNeedIt.calculateTotalAmountWaterAvailable(testCrop, testSoil);

    printAccumulatedAmountRainWater(presumedCurrentDate, testClimateRecords);
    printAccumulatedAmountIrrigationWater(presumedCurrentDate, zeroIrrigationRecords);
    printTotalAmountWaterAvailable(totalAmountWaterAvailable);
    System.out.println();
    printTotalIrrigationWaterCurrentDate(presumedCurrentDate, totalIrrigationWaterCurrentDate);
    printAccumulatedWaterDeficitPerDay(presumedCurrentDate, accumulatedWaterDeficitPerDay);
    printOptimalIrrigationLayer(optimalIrrigationLayer);

    System.out.println();
    printMessageSituationThree();
    System.out.println();

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");

    /*
     * Al metodo calculateIrrigationWaterNeed de la clase
     * WaterNeedIt se le pasa como argumento la ETc de la
     * presunta fecha actual porque el registro climatico
     * siete tiene la presunta fecha actual. El motivo de
     * este pasaje es que, para calcular correctamente la
     * necesidad de agua de riego de un cultivo en la
     * presunta fecha actual, se requiere ajustar el
     * factor de agotamiento (p) a la ETc de la presunta
     * fecha actual, con base a la cual se calcula la
     * lamina de riego optima (drop) de la presunta
     * fecha actual, con base a la cual se calcula la
     * necesidad de agua de riego de un cultivo en la
     * presunta fecha actual.
     */
    double expectedResult = 0;
    double result = WaterNeedIt.calculateIrrigationWaterNeed(totalIrrigationWaterCurrentDate,
        climateRecordSeven.getEtc(), testCrop, testSoil, testClimateRecords, zeroIrrigationRecords);

    System.out.println("* Valor esperado (nec. agua riego de un cultivo en la fecha actual [mm/dia]): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateIrrigationWaterNeed (nec. agua riego");
    System.out.println("de un cultivo en fecha actual [mm/dia]): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testElevenCalculateIrrigationWaterNeed() {
    System.out.println("**************************************** Prueba once del metodo calculateIrrigationWaterNeed *****************************************");
    printDescriptionMethodToTest();

    System.out.println("# Descripcion de la prueba unitaria");

    /*
     * Establece los datos de los registros para
     * esta prueba
     */
    setRecordsValuesEleven();

    System.out.println("Los datos con los que se calculara la necesidad de agua de riego de un cultivo en la fecha actual (" + UtilDate.formatDate(presumedCurrentDate) + ") son los siguientes:");
    System.out.println();
    printTestCrop(testCrop);
    System.out.println();
    printTestSoil(testSoil);
    System.out.println();

    printWaterBalanceTable(presumedCurrentDate, climateRecordsToShow, testIrrigationRecords);

    double totalIrrigationWaterCurrentDate = 0;
    double accumulatedWaterDeficitPerDay = WaterMath.calculateAccumulatedWaterDeficitPerDay(testClimateRecords, testIrrigationRecords);
    double optimalIrrigationLayer = WaterNeedIt.calculateOptimalIrrigationLayer(climateRecordSeven.getEtc(), testCrop, testSoil);
    double totalAmountWaterAvailable = WaterNeedIt.calculateTotalAmountWaterAvailable(testCrop, testSoil);

    printAccumulatedAmountRainWater(presumedCurrentDate, testClimateRecords);
    printAccumulatedAmountIrrigationWater(presumedCurrentDate, testIrrigationRecords);
    printTotalAmountWaterAvailable(totalAmountWaterAvailable);
    System.out.println();
    printTotalIrrigationWaterCurrentDate(presumedCurrentDate, totalIrrigationWaterCurrentDate);
    printAccumulatedWaterDeficitPerDay(presumedCurrentDate, accumulatedWaterDeficitPerDay);
    printOptimalIrrigationLayer(optimalIrrigationLayer);

    System.out.println();
    printMessageSituationTwo();
    System.out.println();

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");

    /*
     * Al metodo calculateIrrigationWaterNeed de la clase
     * WaterNeedIt se le pasa como argumento la ETc de la
     * presunta fecha actual porque el registro climatico
     * siete tiene la presunta fecha actual. El motivo de
     * este pasaje es que, para calcular correctamente la
     * necesidad de agua de riego de un cultivo en la
     * presunta fecha actual, se requiere ajustar el
     * factor de agotamiento (p) a la ETc de la presunta
     * fecha actual, con base a la cual se calcula la
     * lamina de riego optima (drop) de la presunta
     * fecha actual, con base a la cual se calcula la
     * necesidad de agua de riego de un cultivo en la
     * presunta fecha actual.
     */
    double expectedResult = Math.abs(accumulatedWaterDeficitPerDay);
    double result = WaterNeedIt.calculateIrrigationWaterNeed(totalIrrigationWaterCurrentDate,
        climateRecordSeven.getEtc(), testCrop, testSoil, testClimateRecords, testIrrigationRecords);

    System.out.println("* Valor esperado (nec. agua riego de un cultivo en la fecha actual [mm/dia]): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateIrrigationWaterNeed (nec. agua riego");
    System.out.println("de un cultivo en fecha actual [mm/dia]): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @Test
  public void testTwelveCalculateIrrigationWaterNeed() {
    System.out.println("**************************************** Prueba doce del metodo calculateIrrigationWaterNeed *****************************************");
    printDescriptionMethodToTest();

    System.out.println("# Descripcion de la prueba unitaria");

    /*
     * Establece los datos de los registros para
     * esta prueba
     */
    setRecordsValuesTwelve();

    System.out.println("Los datos con los que se calculara la necesidad de agua de riego de un cultivo en la fecha actual (" + UtilDate.formatDate(presumedCurrentDate) + ") son los siguientes:");
    System.out.println();
    printTestCrop(testCrop);
    System.out.println();
    printTestSoil(testSoil);
    System.out.println();

    printWaterBalanceTable(presumedCurrentDate, climateRecordsToShow, testIrrigationRecords);

    double totalIrrigationWaterCurrentDate = 2;
    double accumulatedWaterDeficitPerDay = WaterMath.calculateAccumulatedWaterDeficitPerDay(testClimateRecords, testIrrigationRecords);
    double optimalIrrigationLayer = WaterNeedIt.calculateOptimalIrrigationLayer(climateRecordSeven.getEtc(), testCrop, testSoil);
    double totalAmountWaterAvailable = WaterNeedIt.calculateTotalAmountWaterAvailable(testCrop, testSoil);

    printAccumulatedAmountRainWater(presumedCurrentDate, testClimateRecords);
    printAccumulatedAmountIrrigationWater(presumedCurrentDate, testIrrigationRecords);
    printTotalAmountWaterAvailable(totalAmountWaterAvailable);
    System.out.println();
    printTotalIrrigationWaterCurrentDate(presumedCurrentDate, totalIrrigationWaterCurrentDate);
    printAccumulatedWaterDeficitPerDay(presumedCurrentDate, accumulatedWaterDeficitPerDay);
    printOptimalIrrigationLayer(optimalIrrigationLayer);

    System.out.println();
    printMessageSituationFive();
    System.out.println();

    /*
     * Seccion de prueba
     */
    System.out.println("# Ejecucion de la prueba unitaria");

    /*
     * Al metodo calculateIrrigationWaterNeed de la clase
     * WaterNeedIt se le pasa como argumento la ETc de la
     * presunta fecha actual porque el registro climatico
     * siete tiene la presunta fecha actual. El motivo de
     * este pasaje es que, para calcular correctamente la
     * necesidad de agua de riego de un cultivo en la
     * presunta fecha actual, se requiere ajustar el
     * factor de agotamiento (p) a la ETc de la presunta
     * fecha actual, con base a la cual se calcula la
     * lamina de riego optima (drop) de la presunta
     * fecha actual, con base a la cual se calcula la
     * necesidad de agua de riego de un cultivo en la
     * presunta fecha actual.
     */
    double expectedResult = Math.abs(accumulatedWaterDeficitPerDay + totalIrrigationWaterCurrentDate);
    double result = WaterNeedIt.calculateIrrigationWaterNeed(totalIrrigationWaterCurrentDate,
        climateRecordSeven.getEtc(), testCrop, testSoil, testClimateRecords, testIrrigationRecords);

    System.out.println("* Valor esperado (nec. agua riego de un cultivo en la fecha actual [mm/dia]): " + expectedResult);
    System.out.println("* Valor devuelto por el metodo calculateIrrigationWaterNeed (nec. agua riego");
    System.out.println("de un cultivo en fecha actual [mm/dia]): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  /**
   * La situacion uno es aquella en la que el nivel de humedad de un
   * suelo (*) [mm] en una fecha, que tiene un cultivo sembrado, es
   * menor o igual a lamina de riego optima (drop) [mm] negativa de
   * una fecha y la cantidad total de agua de riego de una fecha
   * [mm/dia] es igual a cero.
   * 
   * (*) El nivel de humedad de un suelo esta dado por el acumulado
   * del deficit de agua por dia de dias previos a una fecha [mm/dia],
   * el cual puede ser mayor a cero. Que el acumulado del deficit de
   * agua por dia de dias previos a una fecha [mm/dia] sea mayor a
   * cero, significa que la cantidad de agua que hay en un suelo es
   * mayor a la que este puede almacenar. En esta situacion la
   * cantidad excedente de agua se escurre.
   * 
   * Cuando el acumulado del deficit de agua por dia de dias previos
   * a una fecha [mm/dia] es mayor a cero, la aplicacion le asigna el
   * valor 0, ya que lo que se busca es acumular la cantidad de agua
   * evaporada por dia que NO fue cubierta en dias previos a una fecha
   * (y no acumular el excedente de agua) para calcular la necesidad
   * de agua de riego de un cultivo en una fecha [mm/dia]. Dicha cantidad
   * tiene el signo negativo (-), ya que es un deficit y un deficit
   * tiene el signo negativo (-).
   */
  private void printMessageSituationOne() {
    System.out.println("Al ser:");
    System.out.println("- el acumulado del deficit de agua por dia de dias previos a la fecha actual [mm/dia] menor o igual a la lamina de riego optima");
    System.out.println("(drop) negativa [mm] de la fecha actual, y");
    System.out.println("- la cantidad total de agua de riego de la fecha actual [mm/dia] igual a cero,");
    System.out.println();
    System.out.println("el valor devuelto por el metodo calculateIrrigationWaterNeed de la clase WaterNeedIt debe ser la lamina de riego optima (drop)");
    System.out.println("[mm] de la fecha actual. Por lo tanto, la necesidad de agua de riego de un cultivo en la fecha actual [mm/dia] es la lamina de");
    System.out.println("riego optima (drop) [mm] de la fecha actual.");
  }

  /**
   * La situacion dos es aquella en la que el nivel de humedad
   * de un suelo (*) [mm] en una fecha, que tiene un cultivo sembrado,
   * es estrictamente mayor a la lamina de riego optima (drop) [mm]
   * negativa de una fecha y es estrictamente menor a la capacidad
   * de campo del suelo, y la cantidad total de agua de riego de
   * una fecha [mm/dia] es igual a cero
   */
  private void printMessageSituationTwo() {
    System.out.println("Al ser:");
    System.out.println("- el acumulado del deficit de agua por dia de dias previos a la fecha actual [mm/dia] estrictamente mayor a la lamina de");
    System.out.println("riego optima (drop) negativa [mm] de la fecha actual y estrictamente menor a la capacidad de campo (0 [mm]) del suelo, y");
    System.out.println("- la cantidad total de agua de riego de la fecha actual [mm/dia] igual a cero,");
    System.out.println();
    System.out.println("el valor devuelto por el metodo calculateIrrigationWaterNeed de la clase WaterNeedIt debe ser el valor absoluto del acumulado del");
    System.out.println("deficit de agua por dia de dias previos a la fecha actual [mm/dia]. Por lo tanto, la necesidad de agua de riego de un cultivo en");
    System.out.println("la fecha actual [mm/dia] es el valor absoluto del acumulado del deficit de agua por dia de dias previos a la fecha actual [mm/dia].");
  }

  /**
   * La situacion tres es aquella en la que el nivel de humedad
   * de un suelo (*) [mm] en una fecha, que tiene un cultivo sembrado,
   * es mayor o igual a la capacidad de campo (0 [mm]) del suelo,
   * debido a que la suma entre el acumulado del deficit de agua
   * por dia de dias previos a una fecha [mm/dia] y la cantidad
   * total de agua de riego de una fecha [mm/dia] es mayor o
   * igual a la capacidad de campo (0 [mm]) del suelo
   */
  private void printMessageSituationThree() {
    System.out.println("Al ser:");
    System.out.println("- la suma entre el acumulado del deficit de agua por dia de dias previos a la fecha actual [mm/dia] y la cantidad total de agua");
    System.out.println("de riego de la fecha actual [mm/dia] mayor o igual a la capacidad de campo (0 [mm]) del suelo,");
    System.out.println();
    System.out.println("el valor devuelto por el metodo calculateIrrigationWaterNeed de la clase WaterNeedIt debe ser 0. Por lo tanto, la necesidad");
    System.out.println("de agua de riego de un cultivo en la fecha actual [mm/dia] es 0.");
    System.out.println();
    System.out.println("Que el acumulado del deficit de agua por dia de dias previos a una fecha [mm/dia] sea igual a la capacidad de campo (0 [mm])");
    System.out.println("de un suelo, que tiene un cultivo sembrado, significa que el suelo esta lleno de agua, y quiza anegado. Por este motivo la");
    System.out.println("necesidad de agua de riego de un cultivo en una fecha [mm/dia] es 0.");
    System.out.println("Que la suma entre el acumulado del deficit de agua por dia de dias previos a una fecha [mm/dia] y la cantidad total de agua de");
    System.out.println("riego de una fecha [mm/dia] sea mayor o igual a la capacidad de campo (0 [mm]) del suelo, que tiene un cultivo sembrado, significa");
    System.out.println("que el suelo esta lleno de agua, y quiza anegado. Por este motivo la necesidad de agua de riego de un cultivo en una fecha es 0");
    System.out.println("[mm/dia].");
    System.out.println();
    System.out.println("La frase 'y quiza anegado' se debe a que, si a un suelo que esta lleno de agua, se le agrega mas agua, la misma se escurre, pero");
    System.out.println("dependiendo de la cantidad de agua que se le agregue a un suelo lleno de agua, puede suceder que se anegue.");
  }

  /**
   * La situacion cuatro es aquella en la que el nivel de humedad
   * de un suelo (*) [mm] en una fecha, que tiene un cultivo sembrado,
   * es menor o igual a lamina de riego optima (drop) [mm] negativa
   * de una fecha y la cantidad total de agua de riego de una fecha
   * [mm/dia] es estrictamente mayor a cero
   */
  private void printMessageSituationFour() {
    System.out.println("Al ser:");
    System.out.println("- el acumulado del deficit de agua por dia de dias previos a la fecha actual [mm/dia] menor o igual a la lamina de riego optima");
    System.out.println("(drop) negativa [mm] de la fecha actual, y");
    System.out.println("- la cantidad total de agua de riego de la fecha actual [mm/dia] estrictamente mayor a cero,");
    System.out.println();
    System.out.println("el valor devuelto por el metodo calculateIrrigationWaterNeed de la clase WaterNeedIt debe ser el resultado de la diferencia entre");
    System.out.println("lamina de riego optima (drop) [mm] de la fecha actual y la cantidad total de agua de riego de la fecha actual [mm/dia]. Por lo tanto,");
    System.out.println("la necesidad de agua de riego de un cultivo en la fecha actual [mm/dia] es el resultado de esta diferencia [mm/dia].");
  }

  /**
   * La situacion cinco es aquella en la que el nivel de humedad
   * de un suelo (*) [mm] en una fecha, que tiene un cultivo sembrado,
   * es estrictamente mayor a la lamina de riego optima (drop) [mm]
   * negativa y estrictamente menor a la capacidad de campo (0 [mm])
   * del suelo y la cantidad total de agua de riego de una fecha
   * [mm/dia] es estrictamente mayor a cero
   */
  private void printMessageSituationFive() {
    System.out.println("Al ser:");
    System.out.println("- el acumulado del deficit de agua por dia de dias previos a la fecha actual [mm/dia] estrictamente mayor a la lamina riego");
    System.out.println("optima (drop) negativa [mm] de la fecha actual y estrictamente menor a la capacidad de campo (0 [mm]) del suelo, y");
    System.out.println("- la cantidad total de agua de reigo de la fecha actual [mm/dia] estrictamente mayor a cero,");
    System.out.println();
    System.out.println("el valor devuelto por el metodo calculateIrrigationWaterNeed de la clase WaterNeedIt debe ser el valor absoluto de la suma");
    System.out.println("entre el acumulado del deficit de agua por dia de dias previos a la fecha actual [mm/dia] y la cantidad total de agua de riego");
    System.out.println("de la fecha actual [mm/dia]. Por lo tanto, la necesidad de agua de riego de un cultivo en la fecha actual [mm/dia] es el valor");
    System.out.println("absoluto de esta suma.");
  }

  /**
   * Imprime los datos de un cultivo de prueba
   * 
   * @param testCrop
   */
  private void printTestCrop(Crop testCrop) {
    System.out.println("Datos del cultivo de prueba");
    System.out.println("Nombre: " + testCrop.getName());
    System.out.println("Profundidad radicular [m]: " + testCrop.getLowerLimitMaximumRootDepth() + "-" + testCrop.getUpperLimitMaximumRootDepth());
    System.out.println("Factor de agotamiento (p): " + testCrop.getDepletionFactor());
  }

  /**
   * Imprime los datos de un suelo de prueba
   * 
   * @param testSoil
   */
  private void printTestSoil(Soil testSoil) {
    System.out.println("Datos del suelo de prueba");
    System.out.println("Nombre: " + testSoil.getName());
    System.out.println("Peso especifico aparente [gr%cm3]: " + testSoil.getApparentSpecificWeight());
    System.out.println("Capacidad de campo [gr%gr]: " + testSoil.getFieldCapacity());
    System.out.println("Punto de marchitez permanente [gr%gr]: " + testSoil.getPermanentWiltingPoint());
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

      /*
       * SI la fecha del registro climatico actualmente leido
       * es estrictamente menor (anterior) a la presunta fecha
       * actual, se imprimen sus datos, ya que con base a ellos
       * se calcula la necesidad de agua de riego de un cultivo
       * en la presunta fecha actual
       */
      if (UtilDate.compareTo(currentClimateRecord.getDate(), presumedCurrentDate) < 0) {
        System.out.print("  |");
        System.out.print("   " + currentClimateRecord.getEtc());
        System.out.print("   |");
        System.out.print("   " + waterProvidedGivenDate);
        System.out.print("    |");
        System.out.print("  " + String.format("%.3f", differencePerDay));
        System.out.print("  |");
        System.out.print("   " + accumulatedWaterDeficitPerDay);
      }

      /*
       * Si la fecha del registro climatico actualmente leido
       * es igual a la presunta fecha actual, solo se imprime
       * la ETc del mismo. Esto se debe a que quiero mostrar
       * la ETc de la presunta fecha actual, con base a la cual
       * se ajusta el factor de agotamiento (p) para calcular
       * la lamina de riego optima (drop) de la presunta fecha
       * actual, la cual se utiliza para calcular la necesidad
       * de agua de riego de un cultivo en la presunta fecha
       * actual
       */
      if (UtilDate.compareTo(currentClimateRecord.getDate(), presumedCurrentDate) == 0) {
        System.out.print("  |");
        System.out.print("   " + currentClimateRecord.getEtc());
        System.out.print("   |");
        System.out.print("    - ");
        System.out.print("    |");
        System.out.print("    - ");
        System.out.print("   |");
        System.out.print("    -");
      }

      System.out.println();

      day++;
    }

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
        + UtilDate.formatDate(presumedCurrentDate) + ": " + totalIrrigationWaterCurrentDate + " [mm/dia]");
  }

  /**
   * @param presumedCurrentDate
   * @param accumulatedWaterDeficitPerDay
   */
  private void printAccumulatedWaterDeficitPerDay(Calendar presumedCurrentDate, double accumulatedWaterDeficitPerDay) {
    System.out.println("- Acumulado del deficit de agua por dia de dias previos a la fecha actual (hoy) " + UtilDate.formatDate(presumedCurrentDate)
        + ": " + accumulatedWaterDeficitPerDay + " [mm/dia]");
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
   * @param totalAmountWaterAvailable
   */
  private void printTotalAmountWaterAvailable(double totalAmountWaterAvailable) {
    System.out.println("- Capacidad de almacenamiento de agua del suelo (dt): " + totalAmountWaterAvailable + " [mm]");
  }

  /**
   * @param optimalIrrigationLayer
   */
  private void printOptimalIrrigationLayer(double optimalIrrigationLayer) {
    System.out.println("- Lamina de riego optima (drop) de la fecha actual: " + optimalIrrigationLayer + " [mm]");
  }

  /**
   * Imprime la descripcion del metodo a probar, el cual en este
   * caso es el metodo calculateIrrigationWaterNeed de la clase
   * WaterNeedWit
   */
  private void printDescriptionMethodToTest() {
    System.out.println("El metodo calculateIrrigationWaterNeed de la clase calculateIrrigationWaterNeed calcula la necesidad de agua de riego [mm/dia] de un");
    System.out.println("cultivo en una fecha mediante los datos de un suelo, un factor de agotamiento, una coleccion de registros climaticos y una coleccion");
    System.out.println("de registros de riego, debiendo ser todos ellos previos a una fecha y pertenecientes a una misma parcela que tiene un cultivo sembrado");
    System.out.println("en una fecha. La fecha para la que se calcula la necesidad de agua de riego de un cultivo puede ser la fecha actual (es decir, hoy)");
    System.out.println("o una fecha posterior a la fecha actual. Pero tambien puede ser una fecha del pasado (es decir, anterior a la fecha actual), pero esto");
    System.out.println("no tiene sentido si lo que se busca es determinar la necesidad de agua de riego de un cultivo en la fecha actual o en una fecha posterior");
    System.out.println("a la fecha actual.");
    System.out.println();
    System.out.println("El metodo calculateIrrigationWaterNeed utiliza la cantidad total de agua de riego de una fecha [mm/dia], el acumulado del deficit de");
    System.out.println("agua por dia de dias previos a una fecha [mm/dia] y la lamina de riego optima [mm] de una fecha para calcular la necesidad de agua de");
    System.out.println("riego de un cultivo en una fecha [mm/dia].");
    System.out.println();
    System.out.println("El acumulado del deficit de agua por dia de dias previos a una fecha representa la cantidad acumulada de agua evaporada en dias previos");
    System.out.println("a una fecha que NO fue cubierta (satisfecha) y que se debe reponer mediante el riego en una fecha, y se calcula a partir de una coleccion");
    System.out.println("de registros climaticos y una coleccion de registros de riego, debiendo ser todos ellos previos a una fecha y pertenecientes a una");
    System.out.println("misma parcela.");
    System.out.println();
    System.out.println("- Si el resultado de la suma entre el acumulado del deficit de agua por dia de dias previos a una fecha [mm/dia] y la cantidad total");
    System.out.println("de agua de riego de una fecha [mm/dia] es menor o igual a la lamina de riego optima (drop) negativa [mm] de una fecha, significa que");
    System.out.println("el nivel de humedad [mm] de un suelo que tiene un cultivo sembrado esta por debajo de dicha lamina. En consecuencia, el metodo calculateIrrigationWaterNeed");
    System.out.println("retorna la lamina de riego optima (drop) de una fecha como la necesidad de agua de riego de un cultivo en una fecha [mm/dia]. Por lo");
    System.out.println("tanto, la necesidad de agua de riego de un cultivo en una fecha es estrictamente mayor a 0 (cero).");
    System.out.println();
    System.out.println("- Si el resultado de la suma entre el acumulado del deficit de agua por dia de dias previos a una fecha [mm/dia] y la cantidad total");
    System.out.println("de agua de riego de una fecha [mm/dia] es estrictamente mayor a la lamina de riego optima (drop) [mm] negativa de una fecha y estrictamente");
    System.out.println("menor a 0 (capacidad de campo), significa que el nivel de humedad [mm] de un suelo que tiene un cultivo sembrado esta por encima de dicha");
    System.out.println("lamina y por debajo de la capacidad de campo. En consecuencia, el metodo calculateIrrigationWaterNeed retorna el valor absoluto de la");
    System.out.println("suma entre el acumulado del deficit de agua por dia de dias previos a una fecha [mm/dia] y la cantidad total de agua de riego de una");
    System.out.println("fecha [mm/dia] como la necesidad de agua de riego de un cultivo en una fecha [mm/dia]. Por lo tanto, la necesidad de agua de riego de");
    System.out.println("un cultivo en una fecha es estrictamente mayor a 0 (cero).");
    System.out.println();
    System.out.println("- Si el resultado de la suma entre el acumulado del deficit de agua por dia de dias previos a una fecha [mm/dia] y la cantidad total");
    System.out.println("de agua de riego de una fecha [mm/dia] es igual a cero, significa que el nivel de humedad [mm] de un suelo que tiene un cultivo sembrado");
    System.out.println("esta en capacidad de campo, es decir, el suelo esta lleno de agua, pero no anegado. En consecuencia, el metodo calculateIrrigationWaterNeed");
    System.out.println("retorna 0 como la necesidad de agua de riego de un cultivo en una fecha [mm/dia]. Por lo tanto, la necesidad de agua de riego de un");
    System.out.println("cultivo en una fecha es 0 (cero).");
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
   * Establece los datos de los registros utilizados
   * en la prueba unitaria uno
   */
  private void setRecordsValuesOne() {
    climateRecordOne.setEtc(5);
    climateRecordOne.setPrecip(3);

    climateRecordTwo.setEtc(3);
    climateRecordTwo.setPrecip(2);

    climateRecordThree.setEtc(2);
    climateRecordThree.setPrecip(2);

    climateRecordFour.setEtc(2);
    climateRecordFour.setPrecip(0);

    climateRecordFive.setEtc(1.5);
    climateRecordFive.setPrecip(0);

    climateRecordSix.setEtc(4);
    climateRecordSix.setPrecip(0);
  }

  /**
   * Establece los datos de los registros utilizados
   * en la prueba unitaria dos
   */
  private void setRecordsValuesTwo() {
    climateRecordOne.setEtc(5);
    climateRecordOne.setPrecip(3);

    climateRecordTwo.setEtc(3);
    climateRecordTwo.setPrecip(2);

    climateRecordThree.setEtc(4);
    climateRecordThree.setPrecip(2);

    climateRecordFour.setEtc(3);
    climateRecordFour.setPrecip(1);

    climateRecordFive.setEtc(5);
    climateRecordFive.setPrecip(3);

    climateRecordSix.setEtc(2);
    climateRecordSix.setPrecip(1.1);
  }

  /**
   * Establece los datos de los registros utilizados
   * en la prueba unitaria tres
   */
  private void setRecordsValuesThree() {
    climateRecordOne.setEtc(5);
    climateRecordOne.setPrecip(5);

    climateRecordTwo.setEtc(5);
    climateRecordTwo.setPrecip(3);

    climateRecordThree.setEtc(5);
    climateRecordThree.setPrecip(6);

    climateRecordFour.setEtc(3);
    climateRecordFour.setPrecip(0);

    climateRecordFive.setEtc(2);
    climateRecordFive.setPrecip(1);

    climateRecordSix.setEtc(3);
    climateRecordSix.setPrecip(3);
  }

  /**
   * Establece los datos de los registros utilizados
   * en la prueba unitaria cuatro
   */
  private void setRecordsValuesFour() {
    climateRecordOne.setEtc(2);
    climateRecordOne.setPrecip(3);

    climateRecordTwo.setEtc(3);
    climateRecordTwo.setPrecip(3);

    climateRecordThree.setEtc(5);
    climateRecordThree.setPrecip(2);

    climateRecordFour.setEtc(4);
    climateRecordFour.setPrecip(2);

    climateRecordFive.setEtc(3);
    climateRecordFive.setPrecip(6);

    climateRecordSix.setEtc(2);
    climateRecordSix.setPrecip(4);
  }

  /**
   * Establece los datos de los registros utilizados
   * en la prueba unitaria cinco
   */
  private void setRecordsValuesFive() {
    climateRecordOne.setEtc(5);
    climateRecordOne.setPrecip(0);

    climateRecordTwo.setEtc(3);
    climateRecordTwo.setPrecip(0);

    climateRecordThree.setEtc(2);
    climateRecordThree.setPrecip(0);

    climateRecordFour.setEtc(2);
    climateRecordFour.setPrecip(0);

    climateRecordFive.setEtc(2);
    climateRecordFive.setPrecip(0);

    climateRecordSix.setEtc(4);
    climateRecordSix.setPrecip(0);

    irrigationRecordOne.setIrrigationDone(0);
    irrigationRecordTwo.setIrrigationDone(1);
    irrigationRecordThree.setIrrigationDone(1);
    irrigationRecordFour.setIrrigationDone(1);
    irrigationRecordFive.setIrrigationDone(3);
    irrigationRecordSix.setIrrigationDone(1);
  }

  /**
   * Establece los datos de los registros utilizados
   * en la prueba unitaria seis
   */
  private void setRecordsValuesSix() {
    climateRecordOne.setEtc(2);
    climateRecordOne.setPrecip(1);

    climateRecordTwo.setEtc(5);
    climateRecordTwo.setPrecip(3);

    climateRecordThree.setEtc(4);
    climateRecordThree.setPrecip(1);

    climateRecordFour.setEtc(3);
    climateRecordFour.setPrecip(2);

    climateRecordFive.setEtc(5);
    climateRecordFive.setPrecip(2);

    climateRecordSix.setEtc(2);
    climateRecordSix.setPrecip(0);
  }

  /**
   * Establece los datos de los registros utilizados
   * en la prueba unitaria siete
   */
  private void setRecordsValuesSeven() {
    climateRecordOne.setEtc(5);
    climateRecordOne.setPrecip(3);

    climateRecordTwo.setEtc(3);
    climateRecordTwo.setPrecip(2);

    climateRecordThree.setEtc(4);
    climateRecordThree.setPrecip(2);

    climateRecordFour.setEtc(3);
    climateRecordFour.setPrecip(1);

    climateRecordFive.setEtc(5);
    climateRecordFive.setPrecip(3);

    climateRecordSix.setEtc(2);
    climateRecordSix.setPrecip(1.1);
  }

  /**
   * Establece los datos de los registros utilizados
   * en la prueba unitaria ocho
   */
  private void setRecordsValuesEight() {
    climateRecordOne.setEtc(5);
    climateRecordOne.setPrecip(5);

    climateRecordTwo.setEtc(5);
    climateRecordTwo.setPrecip(3);

    climateRecordThree.setEtc(5);
    climateRecordThree.setPrecip(6);

    climateRecordFour.setEtc(3);
    climateRecordFour.setPrecip(0);

    climateRecordFive.setEtc(2);
    climateRecordFive.setPrecip(1);

    climateRecordSix.setEtc(3);
    climateRecordSix.setPrecip(3);
  }

  /**
   * Establece los datos de los registros utilizados
   * en la prueba unitaria nueve
   */
  private void setRecordsValuesNine() {
    climateRecordOne.setEtc(2);
    climateRecordOne.setPrecip(3);

    climateRecordTwo.setEtc(3);
    climateRecordTwo.setPrecip(3);

    climateRecordThree.setEtc(5);
    climateRecordThree.setPrecip(2);

    climateRecordFour.setEtc(4);
    climateRecordFour.setPrecip(2);

    climateRecordFive.setEtc(3);
    climateRecordFive.setPrecip(6);

    climateRecordSix.setEtc(2);
    climateRecordSix.setPrecip(4);
  }

  /**
   * Establece los datos de los registros utilizados
   * en la prueba unitaria diez
   */
  private void setRecordsValuesTen() {
    climateRecordOne.setEtc(5);
    climateRecordOne.setPrecip(5);

    climateRecordTwo.setEtc(3);
    climateRecordTwo.setPrecip(2);

    climateRecordThree.setEtc(3);
    climateRecordThree.setPrecip(3);

    climateRecordFour.setEtc(2);
    climateRecordFour.setPrecip(1);

    climateRecordFive.setEtc(4);
    climateRecordFive.setPrecip(5);

    climateRecordSix.setEtc(3);
    climateRecordSix.setPrecip(2);
  }

  /**
   * Establece los datos de los registros utilizados
   * en la prueba unitaria once
   */
  private void setRecordsValuesEleven() {
    climateRecordOne.setEtc(5);
    climateRecordOne.setPrecip(1);

    climateRecordTwo.setEtc(3);
    climateRecordTwo.setPrecip(0.5);

    climateRecordThree.setEtc(3);
    climateRecordThree.setPrecip(0.5);

    climateRecordFour.setEtc(2);
    climateRecordFour.setPrecip(2);

    climateRecordFive.setEtc(2);
    climateRecordFive.setPrecip(0);

    climateRecordSix.setEtc(4);
    climateRecordSix.setPrecip(5);

    irrigationRecordOne.setIrrigationDone(2);
    irrigationRecordTwo.setIrrigationDone(0.5);
    irrigationRecordThree.setIrrigationDone(0.5);
    irrigationRecordFour.setIrrigationDone(0);
    irrigationRecordFive.setIrrigationDone(3);
    irrigationRecordSix.setIrrigationDone(1);
  }

  /**
   * Establece los datos de los registros utilizados
   * en la prueba unitaria doce
   */
  private void setRecordsValuesTwelve() {
    climateRecordOne.setEtc(5);
    climateRecordOne.setPrecip(1);

    climateRecordTwo.setEtc(3);
    climateRecordTwo.setPrecip(0.5);

    climateRecordThree.setEtc(3);
    climateRecordThree.setPrecip(0.5);

    climateRecordFour.setEtc(2);
    climateRecordFour.setPrecip(2);

    climateRecordFive.setEtc(2);
    climateRecordFive.setPrecip(0);

    climateRecordSix.setEtc(4);
    climateRecordSix.setPrecip(5);

    irrigationRecordOne.setIrrigationDone(2);
    irrigationRecordTwo.setIrrigationDone(0.5);
    irrigationRecordThree.setIrrigationDone(0.5);
    irrigationRecordFour.setIrrigationDone(0);
    irrigationRecordFive.setIrrigationDone(3);
    irrigationRecordSix.setIrrigationDone(1);
  }

}