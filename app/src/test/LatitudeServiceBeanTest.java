import static org.junit.Assert.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import stateless.LatitudeServiceBean;
import model.Latitude;

public class LatitudeServiceBeanTest {

  private static EntityManager entityManager;
  private static EntityManagerFactory entityManagerFactory;
  private static LatitudeServiceBean latitudeService;

  @BeforeClass
  public static void preTest() {
    entityManagerFactory = Persistence.createEntityManagerFactory("swcar");
    entityManager = entityManagerFactory.createEntityManager();

    latitudeService = new LatitudeServiceBean();
    latitudeService.setEntityManager(entityManager);
  }

  @Test
  public void testOneFind() {
    System.out.println("************************** Prueba uno del metodo find(double latitude) **************************");
    System.out.println("El metodo find(double latitude) de la clase LatitudeServiceBean retorna una latitud par si el valor");
    System.out.println("que se le pasa como parametro tiene parte entera par. En caso contrario, retorna el valor null.");
    System.out.println();
    System.out.println("En esta prueba se demuestra que el metodo find(double latitude) retorna el valor null cuando se le");
    System.out.println("pasa como argumento un valor que tiene parte entera impar.");
    System.out.println();

    double doubleLatitude = 3.5;
    Latitude latitude = latitudeService.find(doubleLatitude);
    Latitude expectedValue = null;

    System.out.println("Latitud: " + doubleLatitude);
    System.out.println("Valor esperado: " + expectedValue);
    System.out.println("* Valor devuelto por el metodo find: " + latitude);
    System.out.println();

    /*
     * Seccion de prueba
     */
    assertNull(latitude);
    System.out.println("* Prueba pasada satisfactoriamente *");
  }

  @Test
  public void testTwoFind() {
    System.out.println("************************** Prueba dos del metodo find(double latitude) **************************");
    System.out.println("El metodo find(double latitude) de la clase LatitudeServiceBean retorna una latitud par si el valor");
    System.out.println("que se le pasa como parametro tiene parte entera par. En caso contrario, retorna el valor null.");
    System.out.println();
    System.out.println("En esta prueba se demuestra que el metodo find(double latitude) retorna una latitud par cuando se");
    System.out.println("le pasa como argumento un valor que tiene parte entera par.");
    System.out.println();

    double doubleLatitude = 2.5;
    Latitude latitude = latitudeService.find(doubleLatitude);
    int expectedValue = 2;

    System.out.println("Latitud: " + doubleLatitude);
    System.out.println("Valor esperado: " + expectedValue);
    System.out.println("* Valor devuelto por el metodo find: " + latitude.getLatitude());
    System.out.println();

    /*
     * Seccion de prueba
     */
    assertTrue(expectedValue == latitude.getLatitude());
    System.out.println("* Prueba pasada satisfactoriamente *");
  }

  @Test
  public void testOneFindPreviousLatitude() {
    System.out.println("********************************* Prueba uno del metodo findPreviousLatitude *********************************");
    System.out.println("El metodo findPreviousLatitude de la clase LatitudeServiceBean retorna la latitud -2 cuando se le pasa como");
    System.out.println("argumento un valor de latitud igual a 0. Si el valor de latitud es mayor a cero, retorna la latitud par");
    System.out.println("inmediatamente inferior. Si el valor de latitud es menor a cero, retorna la latitud par inmediatamente superior.");
    System.out.println();
    System.out.println("En esta prueba se demuestra que el metodo findPreviousLatitude retorna la latitud -2 cuando se le pasa 0.0 como");
    System.out.println("argumento.");
    System.out.println();

    double doubleLatitude = 0.0;
    Latitude latitude = latitudeService.findPreviousLatitude(doubleLatitude);
    int expectedValue = -2;

    System.out.println("Latitud: " + doubleLatitude);
    System.out.println("Valor esperado: " + expectedValue);
    System.out.println("* Valor devuelto por el metodo findPreviousLatitude: " + latitude.getLatitude());
    System.out.println();

    /*
     * Seccion de prueba
     */
    assertTrue(expectedValue == latitude.getLatitude());
    System.out.println("* Prueba pasada satisfactoriamente *");
  }

  @Test
  public void testTwoFindPreviousLatitude() {
    System.out.println("********************************* Prueba dos del metodo findPreviousLatitude *********************************");
    System.out.println("El metodo findPreviousLatitude de la clase LatitudeServiceBean retorna la latitud -2 cuando se le pasa como");
    System.out.println("argumento un valor de latitud igual a 0. Si el valor de latitud es mayor a cero, retorna la latitud par");
    System.out.println("inmediatamente inferior. Si el valor de latitud es menor a cero, retorna la latitud par inmediatamente superior.");
    System.out.println();
    System.out.println("En esta prueba se demuestra que el metodo findPreviousLatitude retorna la latitud par inmediatamente inferior cuando");
    System.out.println("se le pasa un valor de latitud impar y mayor a cero como argumento.");
    System.out.println();

    double doubleLatitude = 3.0;
    Latitude latitude = latitudeService.findPreviousLatitude(doubleLatitude);
    int expectedValue = 2;

    System.out.println("Latitud: " + doubleLatitude);
    System.out.println("Valor esperado: " + expectedValue);
    System.out.println("* Valor devuelto por el metodo findPreviousLatitude: " + latitude.getLatitude());
    System.out.println();

    /*
     * Seccion de prueba
     */
    assertTrue(expectedValue == latitude.getLatitude());
    System.out.println("* Prueba pasada satisfactoriamente *");
  }

  @Test
  public void testThreeFindPreviousLatitude() {
    System.out.println("********************************* Prueba tres del metodo findPreviousLatitude *********************************");
    System.out.println("El metodo findPreviousLatitude de la clase LatitudeServiceBean retorna la latitud -2 cuando se le pasa como");
    System.out.println("argumento un valor de latitud igual a 0. Si el valor de latitud es mayor a cero, retorna la latitud par");
    System.out.println("inmediatamente inferior. Si el valor de latitud es menor a cero, retorna la latitud par inmediatamente superior.");
    System.out.println();
    System.out.println("En esta prueba se demuestra que el metodo findPreviousLatitude retorna la latitud par inmediatamente inferior cuando");
    System.out.println("se le pasa un valor de latitud par y mayor a cero como argumento.");
    System.out.println();

    double doubleLatitude = 4.0;
    Latitude latitude = latitudeService.findPreviousLatitude(doubleLatitude);
    int expectedValue = 2;

    System.out.println("Latitud: " + doubleLatitude);
    System.out.println("Valor esperado: " + expectedValue);
    System.out.println("* Valor devuelto por el metodo findPreviousLatitude: " + latitude.getLatitude());
    System.out.println();

    /*
     * Seccion de prueba
     */
    assertTrue(expectedValue == latitude.getLatitude());
    System.out.println("* Prueba pasada satisfactoriamente *");
  }

  @Test
  public void testFourFindPreviousLatitude() {
    System.out.println("********************************* Prueba cuatro del metodo findPreviousLatitude *********************************");
    System.out.println("El metodo findPreviousLatitude de la clase LatitudeServiceBean retorna la latitud -2 cuando se le pasa como");
    System.out.println("argumento un valor de latitud igual a 0. Si el valor de latitud es mayor a cero, retorna la latitud par");
    System.out.println("inmediatamente inferior. Si el valor de latitud es menor a cero, retorna la latitud par inmediatamente superior.");
    System.out.println();
    System.out.println("En esta prueba se demuestra que el metodo findPreviousLatitude retorna la latitud par inmediatamente superior cuando");
    System.out.println("se le pasa un valor de latitud impar y menor a cero como argumento.");
    System.out.println();

    double doubleLatitude = -3.0;
    Latitude latitude = latitudeService.findPreviousLatitude(doubleLatitude);
    int expectedValue = -2;

    System.out.println("Latitud: " + doubleLatitude);
    System.out.println("Valor esperado: " + expectedValue);
    System.out.println("* Valor devuelto por el metodo findPreviousLatitude: " + latitude.getLatitude());
    System.out.println();

    /*
     * Seccion de prueba
     */
    assertTrue(expectedValue == latitude.getLatitude());
    System.out.println("* Prueba pasada satisfactoriamente *");
  }

  @Test
  public void testFiveFindPreviousLatitude() {
    System.out.println("********************************* Prueba cinco del metodo findPreviousLatitude *********************************");
    System.out.println("El metodo findPreviousLatitude de la clase LatitudeServiceBean retorna la latitud -2 cuando se le pasa como");
    System.out.println("argumento un valor de latitud igual a 0. Si el valor de latitud es mayor a cero, retorna la latitud par");
    System.out.println("inmediatamente inferior. Si el valor de latitud es menor a cero, retorna la latitud par inmediatamente superior.");
    System.out.println();
    System.out.println("En esta prueba se demuestra que el metodo findPreviousLatitude retorna la latitud par inmediatamente superior cuando");
    System.out.println("se le pasa un valor de latitud par y menor a cero como argumento.");
    System.out.println();

    double doubleLatitude = -4.0;
    Latitude latitude = latitudeService.findPreviousLatitude(doubleLatitude);
    int expectedValue = -2;

    System.out.println("Latitud: " + doubleLatitude);
    System.out.println("Valor esperado: " + expectedValue);
    System.out.println("* Valor devuelto por el metodo findPreviousLatitude: " + latitude.getLatitude());
    System.out.println();

    /*
     * Seccion de prueba
     */
    assertTrue(expectedValue == latitude.getLatitude());
    System.out.println("* Prueba pasada satisfactoriamente *");
  }

  @Test
  public void testOneFindNextLatitude() {
    System.out.println("********************************* Prueba uno del metodo findNextLatitude *********************************");
    System.out.println("El metodo findNextLatitude de la clase LatitudeServiceBean retorna la latitud 2 cuando se le pasa como");
    System.out.println("argumento un valor de latitud igual a 0. Si el valor de latitud es mayor a cero, retorna la latitud par");
    System.out.println("inmediatamente superior. Si el valor de latitud es menor a cero, retorna la latitud par inmediatamente inferior.");
    System.out.println();
    System.out.println("En esta prueba se demuestra que el metodo findNextLatitude retorna la latitud 2 cuando se le pasa 0.0 como");
    System.out.println("argumento.");
    System.out.println();

    double doubleLatitude = 0.0;
    Latitude latitude = latitudeService.findNextLatitude(doubleLatitude);
    int expectedValue = 2;

    System.out.println("Latitud: " + doubleLatitude);
    System.out.println("Valor esperado: " + expectedValue);
    System.out.println("* Valor devuelto por el metodo findNextLatitude: " + latitude.getLatitude());
    System.out.println();

    /*
     * Seccion de prueba
     */
    assertTrue(expectedValue == latitude.getLatitude());
    System.out.println("* Prueba pasada satisfactoriamente *");
  }

  @Test
  public void testTwoFindNextLatitude() {
    System.out.println("********************************* Prueba dos del metodo findNextLatitude *********************************");
    System.out.println("El metodo findNextLatitude de la clase LatitudeServiceBean retorna la latitud 2 cuando se le pasa como");
    System.out.println("argumento un valor de latitud igual a 0. Si el valor de latitud es mayor a cero, retorna la latitud par");
    System.out.println("inmediatamente superior. Si el valor de latitud es menor a cero, retorna la latitud par inmediatamente inferior.");
    System.out.println();
    System.out.println("En esta prueba se demuestra que el metodo findNextLatitude retorna la latitud par inmediatamente superior cuando");
    System.out.println("se le pasa un valor de latitud impar y mayor a cero como argumento.");
    System.out.println();

    double doubleLatitude = 3.0;
    Latitude latitude = latitudeService.findNextLatitude(doubleLatitude);
    int expectedValue = 4;

    System.out.println("Latitud: " + doubleLatitude);
    System.out.println("Valor esperado: " + expectedValue);
    System.out.println("* Valor devuelto por el metodo findNextLatitude: " + latitude.getLatitude());
    System.out.println();

    /*
     * Seccion de prueba
     */
    assertTrue(expectedValue == latitude.getLatitude());
    System.out.println("* Prueba pasada satisfactoriamente *");
  }

  @Test
  public void testThreeFindNextLatitude() {
    System.out.println("********************************* Prueba tres del metodo findNextLatitude *********************************");
    System.out.println("El metodo findNextLatitude de la clase LatitudeServiceBean retorna la latitud 2 cuando se le pasa como");
    System.out.println("argumento un valor de latitud igual a 0. Si el valor de latitud es mayor a cero, retorna la latitud par");
    System.out.println("inmediatamente superior. Si el valor de latitud es menor a cero, retorna la latitud par inmediatamente inferior.");
    System.out.println();
    System.out.println("En esta prueba se demuestra que el metodo findNextLatitude retorna la latitud par inmediatamente superior cuando");
    System.out.println("se le pasa un valor de latitud par y mayor a cero como argumento.");
    System.out.println();

    double doubleLatitude = 4.0;
    Latitude latitude = latitudeService.findNextLatitude(doubleLatitude);
    int expectedValue = 6;

    System.out.println("Latitud: " + doubleLatitude);
    System.out.println("Valor esperado: " + expectedValue);
    System.out.println("* Valor devuelto por el metodo findNextLatitude: " + latitude.getLatitude());
    System.out.println();

    /*
     * Seccion de prueba
     */
    assertTrue(expectedValue == latitude.getLatitude());
    System.out.println("* Prueba pasada satisfactoriamente *");
  }

  @Test
  public void testFourFindNextLatitude() {
    System.out.println("********************************* Prueba cuatro del metodo findNextLatitude *********************************");
    System.out.println("El metodo findNextLatitude de la clase LatitudeServiceBean retorna la latitud 2 cuando se le pasa como");
    System.out.println("argumento un valor de latitud igual a 0. Si el valor de latitud es mayor a cero, retorna la latitud par");
    System.out.println("inmediatamente superior. Si el valor de latitud es menor a cero, retorna la latitud par inmediatamente inferior.");
    System.out.println();
    System.out.println("En esta prueba se demuestra que el metodo findNextLatitude retorna la latitud par inmediatamente inferior cuando");
    System.out.println("se le pasa un valor de latitud impar y menor a cero como argumento.");
    System.out.println();

    double doubleLatitude = -3.0;
    Latitude latitude = latitudeService.findNextLatitude(doubleLatitude);
    int expectedValue = -4;

    System.out.println("Latitud: " + doubleLatitude);
    System.out.println("Valor esperado: " + expectedValue);
    System.out.println("* Valor devuelto por el metodo findNextLatitude: " + latitude.getLatitude());
    System.out.println();

    /*
     * Seccion de prueba
     */
    assertTrue(expectedValue == latitude.getLatitude());
    System.out.println("* Prueba pasada satisfactoriamente *");
  }

  @Test
  public void testFiveFindNextLatitude() {
    System.out.println("********************************* Prueba cinco del metodo findNextLatitude *********************************");
    System.out.println("El metodo findNextLatitude de la clase LatitudeServiceBean retorna la latitud 2 cuando se le pasa como");
    System.out.println("argumento un valor de latitud igual a 0. Si el valor de latitud es mayor a cero, retorna la latitud par");
    System.out.println("inmediatamente superior. Si el valor de latitud es menor a cero, retorna la latitud par inmediatamente inferior.");
    System.out.println();
    System.out.println("En esta prueba se demuestra que el metodo findNextLatitude retorna la latitud par inmediatamente inferior cuando");
    System.out.println("se le pasa un valor de latitud par y menor a cero como argumento.");
    System.out.println();

    double doubleLatitude = -4.0;
    Latitude latitude = latitudeService.findNextLatitude(doubleLatitude);
    int expectedValue = -6;

    System.out.println("Latitud: " + doubleLatitude);
    System.out.println("Valor esperado: " + expectedValue);
    System.out.println("* Valor devuelto por el metodo findNextLatitude: " + latitude.getLatitude());
    System.out.println();

    /*
     * Seccion de prueba
     */
    assertTrue(expectedValue == latitude.getLatitude());
    System.out.println("* Prueba pasada satisfactoriamente *");
  }

  @AfterClass
  public static void postTest() {
    // Cierra las conexiones
    entityManager.close();
    entityManagerFactory.close();
  }

}
