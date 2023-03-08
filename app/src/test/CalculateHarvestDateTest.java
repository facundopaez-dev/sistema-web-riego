import static org.junit.Assert.*;

import java.util.Calendar;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import model.Crop;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import stateless.CropServiceBean;
import util.UtilDate;

public class CalculateHarvestDateTest {

  private static EntityManager entityManager;
  private static EntityManagerFactory entityManagerFactory;
  private static CropServiceBean cropService;

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

    cropService = new CropServiceBean();
    cropService.setEntityManager(entityManager);
  }

  @Test
  public void testOneCalculateHarvestDate() {
    System.out.println("************************** Prueba uno del metodo calculateHarvestDate **************************");
    System.out.println("- Condicion 1: La fecha de cosecha resultante tiene el mismo año que la fecha de siembra.");
    System.out.println("Para esta prueba se utilizara al cultivo rabano, el cual, tiene un ciclo de vida de 35 dias.");
    System.out.println();
    System.out.println("Dada la fecha de siembra 1/1/2023 para el rabano, el metodo del calculo de la fecha de cosecha");
    System.out.println("debe retornar como resultado la fecha 4/2/2023. Si enero tuviese 30 dias, la fecha de cosecha");
    System.out.println("seria la siguiente: 5/2/2023");
    System.out.println();

    /*
     * Impresion del cultivo de prueba
     */
    String cropName = "Rábano";
    Crop givenCrop = cropService.findByName(cropName);

    System.out.println("* Cultivo de prueba *");
    System.out.println(givenCrop);

    /*
     * Fecha de siembra para el calculo de la fecha
     * de cosecha del cultivo de prueba
     */
    Calendar seedDate = Calendar.getInstance();
    seedDate.set(Calendar.YEAR, 2023);
    seedDate.set(Calendar.MONTH, JANUARY);
    seedDate.set(Calendar.DAY_OF_MONTH, 1);
    System.out.println();

    System.out.println("Fecha de siembra para el " + cropName + ": " + UtilDate.formatDate(seedDate));
    System.out.println();

    /*
     * Seccion de prueba
     */
    Calendar expectedDate = Calendar.getInstance();
    expectedDate.set(Calendar.YEAR, 2023);
    expectedDate.set(Calendar.MONTH, FEBRUARY);
    expectedDate.set(Calendar.DAY_OF_MONTH, 4);

    Calendar harvestDate = cropService.calculateHarvestDate(seedDate, givenCrop);

    System.out.println("Fecha de cosecha esperada: " + UtilDate.formatDate(expectedDate));
    System.out.println("* Fecha de cosecha obtenida: " + UtilDate.formatDate(harvestDate));
    System.out.println();

    /*
     * Si la fecha esperada es igual a la fecha de cosecha,
     * el metodo compareTo de la clase Calendar retorna 0
     */
    assertTrue(expectedDate.compareTo(harvestDate) == 0);

    System.out.println("* Prueba pasada satisfactoriamente *");
    System.out.println();
  }

  @Test
  public void testTwoCalculateHarvestDate() {
    System.out.println("**************************** Prueba dos del metodo calculateHarvestDate ****************************");
    System.out.println("- Condicion 2: La fecha de cosecha resultante tiene un año mas que la fecha de siembra.");
    System.out.println("Para esta prueba se utilizara al cultivo rabano, el cual, tiene un ciclo de vida de 35 dias.");
    System.out.println();
    System.out.println("Dada la fecha de siembra 27/12/2022 para el rabano, el metodo del calculo de la fecha de cosecha");
    System.out.println("debe retornar como resultado la fecha 30/1/2023. Incluyendo el dia 27 de diciembre, desde este dia");
    System.out.println("al dia 31 de diciembre hay en total 5 dias.");
    System.out.println();

    /*
     * Impresion del cultivo de prueba
     */
    String cropName = "Rábano";
    Crop givenCrop = cropService.findByName(cropName);

    System.out.println("* Cultivo de prueba *");
    System.out.println(givenCrop);

    /*
     * Fecha de siembra para el calculo de la fecha
     * de cosecha del cultivo de prueba
     */
    Calendar seedDate = Calendar.getInstance();
    seedDate.set(Calendar.YEAR, 2022);
    seedDate.set(Calendar.MONTH, DECEMBER);
    seedDate.set(Calendar.DAY_OF_MONTH, 27);
    System.out.println();

    System.out.println("Fecha de siembra para el " + cropName + ": " + UtilDate.formatDate(seedDate));
    System.out.println();

    /*
     * Seccion de prueba
     */
    Calendar expectedDate = Calendar.getInstance();
    expectedDate.set(Calendar.YEAR, 2023);
    expectedDate.set(Calendar.MONTH, JANUARY);
    expectedDate.set(Calendar.DAY_OF_MONTH, 30);

    Calendar harvestDate = cropService.calculateHarvestDate(seedDate, givenCrop);

    System.out.println("Fecha de cosecha esperada: " + UtilDate.formatDate(expectedDate));
    System.out.println("* Fecha de cosecha obtenida: " + UtilDate.formatDate(harvestDate));
    System.out.println();

    /*
     * Si la fecha esperada es igual a la fecha de cosecha,
     * el metodo compareTo de la clase Calendar retorna 0
     */
    assertTrue(expectedDate.compareTo(harvestDate) == 0);

    System.out.println("* Prueba pasada satisfactoriamente *");
    System.out.println();
  }

  @Test
  public void testThreeCalculateHarvestDate() {
    System.out.println("********************************* Prueba tres del metodo calculateHarvestDate *********************************");
    System.out.println("- Condicion 3: La fecha de cosecha resultante tiene el mismo año bisiesto que la fecha de siembra.");
    System.out.println("Para esta prueba se utilizara al cultivo rabano, el cual, tiene un ciclo de vida de 35 dias.");
    System.out.println();
    System.out.println("Dada la fecha de siembra 1/2/2024 (año bisiesto) para el rabano, el metodo del calculo de la fecha de cosecha");
    System.out.println("debe retornar como resultado la fecha 6/3/2024. Si el año 2024 no fuese biesto, la fecha de cosecha seria la");
    System.out.println("la siguiente: 7/3/2024.");
    System.out.println();

    /*
     * Impresion del cultivo de prueba
     */
    String cropName = "Rábano";
    Crop givenCrop = cropService.findByName(cropName);

    System.out.println("* Cultivo de prueba *");
    System.out.println(givenCrop);

    /*
     * Fecha de siembra para el calculo de la fecha
     * de cosecha del cultivo de prueba
     */
    Calendar seedDate = Calendar.getInstance();
    seedDate.set(Calendar.YEAR, 2024);
    seedDate.set(Calendar.MONTH, FEBRUARY);
    seedDate.set(Calendar.DAY_OF_MONTH, 1);
    System.out.println();

    System.out.println("Fecha de siembra para el " + cropName + ": " + UtilDate.formatDate(seedDate));
    System.out.println();

    /*
     * Seccion de prueba
     */
    Calendar expectedDate = Calendar.getInstance();
    expectedDate.set(Calendar.YEAR, 2024);
    expectedDate.set(Calendar.MONTH, MARCH);
    expectedDate.set(Calendar.DAY_OF_MONTH, 6);

    Calendar harvestDate = cropService.calculateHarvestDate(seedDate, givenCrop);

    System.out.println("Fecha de cosecha esperada: " + UtilDate.formatDate(expectedDate));
    System.out.println("* Fecha de cosecha obtenida: " + UtilDate.formatDate(harvestDate));
    System.out.println();

    /*
     * Si la fecha esperada es igual a la fecha de cosecha,
     * el metodo compareTo de la clase Calendar retorna 0
     */
    assertTrue(expectedDate.compareTo(harvestDate) == 0);

    System.out.println("* Prueba pasada satisfactoriamente *");
    System.out.println();
  }

  @Test
  public void testFourCalculateHarvestDate() {
    System.out.println("************************************ Prueba cuatro del metodo calculateHarvestDate ************************************");
    System.out.println("- Condicion 4: La fecha de cosecha resultante tiene un año mas que la fecha de siembra, la cual, tiene un año bisiesto.");
    System.out.println("Para esta prueba se utilizara al cultivo uvas de vino, las cuales, tienen un ciclo de vida de 365 dias.");
    System.out.println();
    System.out.println("Dada la fecha de siembra 3/1/2024 (año bisiesto) para las uvas de vino, el metodo del calculo de la fecha de cosecha");
    System.out.println("debe retornar como resultado la fecha 1/1/2025.");
    System.out.println();

    /*
     * Impresion del cultivo de prueba
     */
    String cropName = "Uvas de vino";
    Crop givenCrop = cropService.findByName(cropName);

    System.out.println("* Cultivo de prueba *");
    System.out.println(givenCrop);

    /*
     * Fecha de siembra para el calculo de la fecha
     * de cosecha del cultivo de prueba
     */
    Calendar seedDate = Calendar.getInstance();
    seedDate.set(Calendar.YEAR, 2024);
    seedDate.set(Calendar.MONTH, JANUARY);
    seedDate.set(Calendar.DAY_OF_MONTH, 3);
    System.out.println();

    System.out.println("Fecha de siembra para las " + cropName + ": " + UtilDate.formatDate(seedDate));
    System.out.println();

    /*
     * Seccion de prueba
     */
    Calendar expectedDate = Calendar.getInstance();
    expectedDate.set(Calendar.YEAR, 2025);
    expectedDate.set(Calendar.MONTH, JANUARY);
    expectedDate.set(Calendar.DAY_OF_MONTH, 1);

    Calendar harvestDate = cropService.calculateHarvestDate(seedDate, givenCrop);

    System.out.println("Fecha de cosecha esperada: " + UtilDate.formatDate(expectedDate));
    System.out.println("* Fecha de cosecha obtenida: " + UtilDate.formatDate(harvestDate));
    System.out.println();

    /*
     * Si la fecha esperada es igual a la fecha de cosecha,
     * el metodo compareTo de la clase Calendar retorna 0
     */
    assertTrue(expectedDate.compareTo(harvestDate) == 0);

    System.out.println("* Prueba pasada satisfactoriamente *");
    System.out.println();
  }

  @Test
  public void testFiveCalculateHarvestDate() {
    System.out.println("******************************** Prueba cinco del metodo calculateHarvestDate ********************************");
    System.out.println("- Condicion 5: La fecha de cosecha resultante tiene un año (bisiesto) mas que la fecha de siembra.");
    System.out.println("Para esta prueba se utilizara al cultivo espinaca, el cual, tienen un ciclo de vida de 70 dias.");
    System.out.println();
    System.out.println("Dada la fecha de siembra 22/12/2023 (año no biesiesto) para la espinaca, el metodo del calculo de la fecha de");
    System.out.println("cosecha debe retornar como resultado la fecha 29/2/2024 (año bisiesto). Si 2024 no fuese bisiesto, la fecha de");
    System.out.println("cosecha seria la siguiente: 1/3/2024. Incluyendo el dia 22 de diciembre, desde este dia al dia 31 de diciembre");
    System.out.println("hay en total 10 dias.");
    System.out.println();

    /*
     * Impresion del cultivo de prueba
     */
    String cropName = "Espinaca";
    Crop givenCrop = cropService.findByName(cropName);

    System.out.println("* Cultivo de prueba *");
    System.out.println(givenCrop);

    /*
     * Fecha de siembra para el calculo de la fecha
     * de cosecha del cultivo de prueba
     */
    Calendar seedDate = Calendar.getInstance();
    seedDate.set(Calendar.YEAR, 2023);
    seedDate.set(Calendar.MONTH, DECEMBER);
    seedDate.set(Calendar.DAY_OF_MONTH, 22);
    System.out.println();

    System.out.println("Fecha de siembra para la " + cropName + ": " + UtilDate.formatDate(seedDate));
    System.out.println();

    /*
     * Seccion de prueba
     */
    Calendar expectedDate = Calendar.getInstance();
    expectedDate.set(Calendar.YEAR, 2024);
    expectedDate.set(Calendar.MONTH, FEBRUARY);
    expectedDate.set(Calendar.DAY_OF_MONTH, 29);

    Calendar harvestDate = cropService.calculateHarvestDate(seedDate, givenCrop);

    System.out.println("Fecha de cosecha esperada: " + UtilDate.formatDate(expectedDate));
    System.out.println("* Fecha de cosecha obtenida: " + UtilDate.formatDate(harvestDate));
    System.out.println();

    /*
     * Si la fecha esperada es igual a la fecha de cosecha,
     * el metodo compareTo de la clase Calendar retorna 0
     */
    assertTrue(expectedDate.compareTo(harvestDate) == 0);

    System.out.println("* Prueba pasada satisfactoriamente *");
    System.out.println();
  }

  @AfterClass
  public static void postTest() {
    // Cierra las conexiones
    entityManager.close();
    entityManagerFactory.close();
  }

}