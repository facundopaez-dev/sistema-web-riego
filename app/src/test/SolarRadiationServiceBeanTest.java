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
import stateless.SolarRadiationServiceBean;
import stateless.LatitudeServiceBean;
import stateless.MonthServiceBean;
import model.SolarRadiation;
import model.Latitude;
import model.Month;

public class SolarRadiationServiceBeanTest {

  private static EntityManager entityManager;
  private static EntityManagerFactory entityMangerFactory;

  private static SolarRadiationServiceBean solarService;
  private static LatitudeServiceBean latitudeService;
  private static MonthServiceBean monthService;

  private static Latitude testLatitudeOne;
  private static Latitude testLatitudeTwo;
  private static Latitude testLatitudeThree;

  private static Month testMonth;

  private static SolarRadiation testSolarRadiationOne;
  private static SolarRadiation testSolarRadiationTwo;
  private static SolarRadiation testSolarRadiationThree;

  private static List<Latitude> latitudes;
  private static List<Month> months;
  private static List<SolarRadiation> solarRadiations;

  @BeforeClass
  public static void preTest() {
    entityMangerFactory = Persistence.createEntityManagerFactory("swcar");
    entityManager = entityMangerFactory.createEntityManager();

    solarService = new SolarRadiationServiceBean();
    solarService.setEntityManager(entityManager);

    latitudeService = new LatitudeServiceBean();
    latitudeService.setEntityManager(entityManager);

    monthService = new MonthServiceBean();
    monthService.setEntityManager(entityManager);

    latitudes = new ArrayList<>();
    months = new ArrayList<>();
    solarRadiations = new ArrayList<>();

    /*
     * Creacion y persistencia de latitudes de prueba
     */
    testLatitudeOne = new Latitude();
    testLatitudeOne.setLatitude(0);

    testLatitudeTwo = new Latitude();
    testLatitudeTwo.setLatitude(2);

    testLatitudeThree = new Latitude();
    testLatitudeThree.setLatitude(4);

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
     * Creacion y persistencia de radiaciones solares de prueba
     */
    testSolarRadiationOne = new SolarRadiation();
    testSolarRadiationOne.setRadiation(6);
    testSolarRadiationOne.setLatitude(testLatitudeOne);
    testSolarRadiationOne.setMonth(testMonth);

    testSolarRadiationTwo = new SolarRadiation();
    testSolarRadiationTwo.setRadiation(8);
    testSolarRadiationTwo.setLatitude(testLatitudeTwo);
    testSolarRadiationTwo.setMonth(testMonth);

    testSolarRadiationThree = new SolarRadiation();
    testSolarRadiationThree.setRadiation(10);
    testSolarRadiationThree.setLatitude(testLatitudeThree);
    testSolarRadiationThree.setMonth(testMonth);

    entityManager.getTransaction().begin();
    entityManager.persist(testSolarRadiationOne);
    entityManager.persist(testSolarRadiationTwo);
    entityManager.persist(testSolarRadiationThree);
    entityManager.getTransaction().commit();

    latitudes.add(testLatitudeOne);
    latitudes.add(testLatitudeTwo);
    latitudes.add(testLatitudeThree);

    months.add(testMonth);

    solarRadiations.add(testSolarRadiationOne);
    solarRadiations.add(testSolarRadiationTwo);
    solarRadiations.add(testSolarRadiationThree);
  }

  @Test
  public void testOneGetRadiation() {
    System.out.println("****************************** Prueba uno del metodo getRadiation() de la clase SolarServiceBean ******************************");
    printDescriptionMethodToTest();
    System.out.println();
    System.out.println("# Descripcion de la prueba unitaria");
    System.out.println("En esta prueba se utiliza una latitud par. Por lo tanto, el metodo getRadiation() debe retornar la radiacion solar extraterrestre");
    System.out.println("correspondiente del mes solicitado.");
    System.out.println();

    System.out.println("# Radiaciones solares de prueba");
    System.out.println("Radiacion solar 1");
    System.out.println("- ID: " + testSolarRadiationOne.getId());
    System.out.println("- Mes: " + testSolarRadiationOne.getMonth().getName());
    System.out.println("- Latitud: " + testSolarRadiationOne.getLatitude().getLatitude());
    System.out.println("- Valor: " + testSolarRadiationOne.getRadiation());
    System.out.println();

    System.out.println("Radiacion solar 2");
    System.out.println("- ID: " + testSolarRadiationTwo.getId());
    System.out.println("- Mes: " + testSolarRadiationTwo.getMonth().getName());
    System.out.println("- Latitud: " + testSolarRadiationTwo.getLatitude().getLatitude());
    System.out.println("- Valor: " + testSolarRadiationTwo.getRadiation());
    System.out.println();

    System.out.println("Radiacion solar 3");
    System.out.println("- ID: " + testSolarRadiationThree.getId());
    System.out.println("- Mes: " + testSolarRadiationThree.getMonth().getName());
    System.out.println("- Latitud: " + testSolarRadiationThree.getLatitude().getLatitude());
    System.out.println("- Valor: " + testSolarRadiationThree.getRadiation());
    System.out.println();

    /*
     * Seccion de prueba
     */
    System.out.println("* Ejecucion de la prueba unitaria *");

    double testLatitudeValue = 2;
    System.out.println("Valor de latitud de prueba: " + testLatitudeValue);
    System.out.println("Mes de prueba: " + testMonth.getName());
    System.out.println();

    double expectedResult = 8.0;
    double result = solarService.getRadiation(testLatitudeValue, testMonth, testLatitudeTwo,
        latitudeService.findPreviousLatitude(testLatitudeValue), latitudeService.findNextLatitude(testLatitudeValue));

    System.out.println("Valor esperado: " + expectedResult);
    System.out.println("Valor devuelto por el metodo getRadiation(): " + result);
    System.out.println();

    assertEquals(expectedResult, result, 0.001);

    System.out.println("- Prueba pasada satisfactoriamente");
  }

  @Test
  public void testTwoGetRadiation() {
    System.out.println("****************************** Prueba dos del metodo getRadiation() de la clase SolarServiceBean ******************************");
    printDescriptionMethodToTest();
    System.out.println();
    System.out.println("# Descripcion de la prueba unitaria");
    System.out.println("En esta prueba se utiliza una latitud impar. Por lo tanto, el metodo getRadiation() debe retornar el promedio de las radiaciones");
    System.out.println("solares extraterrestres de las latitudes aledañas a la latitud del mes solicitado.");
    System.out.println();

    System.out.println("# Radiaciones solares de prueba");
    System.out.println("Radiacion solar 1");
    System.out.println("- ID: " + testSolarRadiationOne.getId());
    System.out.println("- Mes: " + testSolarRadiationOne.getMonth().getName());
    System.out.println("- Latitud: " + testSolarRadiationOne.getLatitude().getLatitude());
    System.out.println("- Valor: " + testSolarRadiationOne.getRadiation());
    System.out.println();

    System.out.println("Radiacion solar 2");
    System.out.println("- ID: " + testSolarRadiationTwo.getId());
    System.out.println("- Mes: " + testSolarRadiationTwo.getMonth().getName());
    System.out.println("- Latitud: " + testSolarRadiationTwo.getLatitude().getLatitude());
    System.out.println("- Valor: " + testSolarRadiationTwo.getRadiation());
    System.out.println();

    System.out.println("Radiacion solar 3");
    System.out.println("- ID: " + testSolarRadiationThree.getId());
    System.out.println("- Mes: " + testSolarRadiationThree.getMonth().getName());
    System.out.println("- Latitud: " + testSolarRadiationThree.getLatitude().getLatitude());
    System.out.println("- Valor: " + testSolarRadiationThree.getRadiation());
    System.out.println();

    /*
     * Seccion de prueba
     */
    System.out.println("* Ejecucion de la prueba unitaria *");

    double testLatitudeValue = 3;
    System.out.println("Valor de latitud de prueba: " + testLatitudeValue);
    System.out.println("Mes de prueba: " + testMonth.getName());
    System.out.println();

    double expectedResult = 9.0;
    double result = solarService.getRadiation(testLatitudeValue, testMonth, testLatitudeTwo,
        latitudeService.findPreviousLatitude(testLatitudeValue), latitudeService.findNextLatitude(testLatitudeValue));

    System.out.println("Valor esperado: " + expectedResult);
    System.out.println("Valor devuelto por el metodo getRadiation(): " + result);
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

    for (SolarRadiation currentSolarRadiation : solarRadiations) {
      entityManager.remove(currentSolarRadiation);
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
    System.out.println("El metodo getRadiation() de la clase SolarServiceBean retorna la radiacion solar extraterrestre con base a una latitud y un mes.");
    System.out.println("Si la latitud es impar retorna el promedio de las radiaciones solares extraterrestres de las latitudes aledañas a la latitud impar");
    System.out.println("del mes solicitado. En cambio, si la latitud es par retorna la radiacion solar extraterrestre correspondiente del mes solicitado.");
  }

}
