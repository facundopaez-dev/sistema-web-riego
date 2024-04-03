import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.AfterClass;
import org.junit.Ignore;
import java.util.List;
import java.util.ArrayList;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.NoResultException;
import stateless.MaximumInsolationServiceBean;
import stateless.LatitudeServiceBean;
import stateless.MonthServiceBean;
import model.MaximumInsolation;
import model.Latitude;
import model.Month;

public class MaximumInsolationServiceBeanTest {

  private static EntityManager entityManager;
  private static EntityManagerFactory entityMangerFactory;

  private static MaximumInsolationServiceBean insolationService;
  private static LatitudeServiceBean latitudeService;
  private static MonthServiceBean monthService;

  private static Latitude testLatitudeOne;
  private static Latitude testLatitudeTwo;
  private static Latitude testLatitudeThree;

  private static Month testMonth;

  private static MaximumInsolation testMaximumInsolationOne;
  private static MaximumInsolation testMaximumInsolationTwo;
  private static MaximumInsolation testMaximumInsolationThree;

  private static List<Latitude> latitudes;
  private static List<Month> months;
  private static List<MaximumInsolation> maximumInsolations;

  @BeforeClass
  public static void preTest() {
    entityMangerFactory = Persistence.createEntityManagerFactory("swcar");
    entityManager = entityMangerFactory.createEntityManager();

    insolationService = new MaximumInsolationServiceBean();
    insolationService.setEntityManager(entityManager);

    latitudeService = new LatitudeServiceBean();
    latitudeService.setEntityManager(entityManager);

    monthService = new MonthServiceBean();
    monthService.setEntityManager(entityManager);

    latitudes = new ArrayList<>();
    months = new ArrayList<>();
    maximumInsolations = new ArrayList<>();

    /*
     * Creacion y persistencia de latitudes de prueba
     */
    testLatitudeOne = new Latitude();
    testLatitudeOne.setLatitude(6);

    testLatitudeTwo = new Latitude();
    testLatitudeTwo.setLatitude(8);

    testLatitudeThree = new Latitude();
    testLatitudeThree.setLatitude(10);

    entityManager.getTransaction().begin();
    testLatitudeOne = latitudeService.create(testLatitudeOne);
    testLatitudeTwo = latitudeService.create(testLatitudeTwo);
    testLatitudeThree = latitudeService.create(testLatitudeThree);
    entityManager.getTransaction().commit();

    /*
     * Creacion y persistencia de un mes de prueba
     */
    testMonth = new Month();
    testMonth.setName("Enero");

    entityManager.getTransaction().begin();
    testMonth = monthService.create(testMonth);
    entityManager.getTransaction().commit();

    /*
     * Creacion y persistencia de insolaciones maximas de prueba
     */
    testMaximumInsolationOne = new MaximumInsolation();
    testMaximumInsolationOne.setInsolation(8);
    testMaximumInsolationOne.setLatitude(testLatitudeOne);
    testMaximumInsolationOne.setMonth(testMonth);

    testMaximumInsolationTwo = new MaximumInsolation();
    testMaximumInsolationTwo.setInsolation(10);
    testMaximumInsolationTwo.setLatitude(testLatitudeTwo);
    testMaximumInsolationTwo.setMonth(testMonth);

    testMaximumInsolationThree = new MaximumInsolation();
    testMaximumInsolationThree.setInsolation(12);
    testMaximumInsolationThree.setLatitude(testLatitudeThree);
    testMaximumInsolationThree.setMonth(testMonth);

    entityManager.getTransaction().begin();
    entityManager.persist(testMaximumInsolationOne);
    entityManager.persist(testMaximumInsolationTwo);
    entityManager.persist(testMaximumInsolationThree);
    entityManager.getTransaction().commit();

    latitudes.add(testLatitudeOne);
    latitudes.add(testLatitudeTwo);
    latitudes.add(testLatitudeThree);

    months.add(testMonth);

    maximumInsolations.add(testMaximumInsolationOne);
    maximumInsolations.add(testMaximumInsolationTwo);
    maximumInsolations.add(testMaximumInsolationThree);
  }

  @Test
  public void testOneGetRadiation() {
    System.out.println("****************************** Prueba uno del metodo getInsolation() de la clase MaximumInsolationServiceBean ******************************");
    printDescriptionMethodToTest();
    System.out.println();
    System.out.println("# Descripcion de la prueba unitaria");
    System.out.println("En esta prueba se utiliza una latitud par. Por lo tanto, el metodo getInsolation() debe retornar la insolacion maxima correspondiente");
    System.out.println("del mes solicitado.");
    System.out.println();

    System.out.println("# Insolaciones maximas de prueba");
    System.out.println("Insolacion maxima 1");
    System.out.println("- ID: " + testMaximumInsolationOne.getId());
    System.out.println("- Mes: " + testMaximumInsolationOne.getMonth().getName());
    System.out.println("- Latitud: " + testMaximumInsolationOne.getLatitude().getLatitude());
    System.out.println("- Valor: " + testMaximumInsolationOne.getInsolation());
    System.out.println();

    System.out.println("Insolacion maxima 2");
    System.out.println("- ID: " + testMaximumInsolationTwo.getId());
    System.out.println("- Mes: " + testMaximumInsolationTwo.getMonth().getName());
    System.out.println("- Latitud: " + testMaximumInsolationTwo.getLatitude().getLatitude());
    System.out.println("- Valor: " + testMaximumInsolationTwo.getInsolation());
    System.out.println();

    System.out.println("Insolacion maxima 3");
    System.out.println("- ID: " + testMaximumInsolationThree.getId());
    System.out.println("- Mes: " + testMaximumInsolationThree.getMonth().getName());
    System.out.println("- Latitud: " + testMaximumInsolationThree.getLatitude().getLatitude());
    System.out.println("- Valor: " + testMaximumInsolationThree.getInsolation());
    System.out.println();

    /*
     * Seccion de prueba
     */
    System.out.println("* Ejecucion de la prueba unitaria *");

    double testLatitudeValue = 8;
    System.out.println("Valor de latitud de prueba: " + testLatitudeValue);
    System.out.println("Mes de prueba: " + testMonth.getName());
    System.out.println();

    double expectedResult = 10;
    double result = insolationService.getInsolation(testLatitudeValue, testMonth, testLatitudeTwo,
        latitudeService.findPreviousLatitude(testLatitudeValue), latitudeService.findNextLatitude(testLatitudeValue));

    System.out.println("Valor esperado: " + expectedResult);
    System.out.println("Valor devuelto por el metodo getInsolation(): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
  }

  @Test
  public void testTwoGetRadiation() {
    System.out.println("****************************** Prueba dos del metodo getInsolation() de la clase MaximumInsolationServiceBean ******************************");
    printDescriptionMethodToTest();
    System.out.println();
    System.out.println("# Descripcion de la prueba unitaria");
    System.out.println("En esta prueba se utiliza una latitud impar. Por lo tanto, el metodo getInsolation() debe retornar el promedio de las insolaciones maximas");
    System.out.println("de las latitudes aledañas a la latitud del mes solicitado.");
    System.out.println();

    System.out.println("# Insolaciones maximas de prueba");
    System.out.println("Insolacion maxima 1");
    System.out.println("- ID: " + testMaximumInsolationOne.getId());
    System.out.println("- Mes: " + testMaximumInsolationOne.getMonth().getName());
    System.out.println("- Latitud: " + testMaximumInsolationOne.getLatitude().getLatitude());
    System.out.println("- Valor: " + testMaximumInsolationOne.getInsolation());
    System.out.println();

    System.out.println("Insolacion maxima 2");
    System.out.println("- ID: " + testMaximumInsolationTwo.getId());
    System.out.println("- Mes: " + testMaximumInsolationTwo.getMonth().getName());
    System.out.println("- Latitud: " + testMaximumInsolationTwo.getLatitude().getLatitude());
    System.out.println("- Valor: " + testMaximumInsolationTwo.getInsolation());
    System.out.println();

    System.out.println("Insolacion maxima 3");
    System.out.println("- ID: " + testMaximumInsolationThree.getId());
    System.out.println("- Mes: " + testMaximumInsolationThree.getMonth().getName());
    System.out.println("- Latitud: " + testMaximumInsolationThree.getLatitude().getLatitude());
    System.out.println("- Valor: " + testMaximumInsolationThree.getInsolation());
    System.out.println();

    /*
     * Seccion de prueba
     */
    System.out.println("* Ejecucion de la prueba unitaria *");

    double testLatitudeValue = 9;
    System.out.println("Valor de latitud de prueba: " + testLatitudeValue);
    System.out.println("Mes de prueba: " + testMonth.getName());
    System.out.println();

    double expectedResult = 11.0;
    double result = insolationService.getInsolation(testLatitudeValue, testMonth, testLatitudeTwo,
        latitudeService.findPreviousLatitude(testLatitudeValue), latitudeService.findNextLatitude(testLatitudeValue));

    System.out.println("Valor esperado: " + expectedResult);
    System.out.println("Valor devuelto por el metodo getInsolation(): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
  }

  @AfterClass
  public static void postTest() {
    /*
     * Elimina todos los datos de la base de datos
     * que que hayan sido ingresados y no eliminados de la misma
     * en los metodos preTest() y de @Test
     */
    entityManager.getTransaction().begin();

    for (Latitude currentLatitude : latitudes) {
      entityManager.remove(currentLatitude);
    }

    for (Month currentMonth : months) {
      entityManager.remove(currentMonth);
    }

    for (MaximumInsolation currentMaximumInsolation : maximumInsolations) {
      entityManager.remove(currentMaximumInsolation);
    }

    entityManager.getTransaction().commit();

    /*
     * Cierra las conexiones, cosa que hace
     * que se liberen los recursos utilizados
     * por el administrador de entidades y su fabrica
     */
    entityManager.close();
    entityMangerFactory.close();
  }

  /**
   * Imprime la descripcion del metodo a probar
   */
  private void printDescriptionMethodToTest() {
    System.out.println("# Descripcion del metodo a probar");
    System.out.println("El metodo getInsolation() de la clase MaximumInsolationServiceBean retorna la insolacion maxima con base a una latitud y un mes.");
    System.out.println("Si la latitud es impar retorna el promedio de las insolaciones maximas de las latitudes aledañas a la latitud impar del mes");
    System.out.println("solicitado. En cambio, si la latitud es par retorna la insolacion maxima correspondiente del mes solicitado.");
  }

}
