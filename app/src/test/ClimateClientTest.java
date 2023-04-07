import static org.junit.Assert.*;

import climate.ClimateClient;
import java.util.Calendar;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import model.ClimateRecord;
import model.Parcel;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class ClimateClientTest {

  private static EntityManager entityManager;
  private static EntityManagerFactory entityManagerFactory;

  @BeforeClass
  public static void preTest() {
    entityManagerFactory = Persistence.createEntityManagerFactory("swcar");
    entityManager = entityManagerFactory.createEntityManager();
  }

  @Test
  public void testOneClimateClient() {
    System.out.println("***************************** Prueba uno del cliente climatico *****************************");
    System.out.println("Pronostico");
    System.out.println("- En esta prueba se obtienen los datos meteorologicos para la ubicacion geografica dada por");
    System.out.println("la coordenada geografica (-45.86413, -67.49656) [Comodoro Rivadavia] en la fecha 12-31-2023.");
    System.out.println();

    Parcel newParcel = new Parcel();
    newParcel.setName("Comodoro Rivadavia");
    newParcel.setLatitude(-45.86413);
    newParcel.setLongitude(-67.49656);
    newParcel.setHectares(2);

    /*
     * 31 de diciembre de 2023 en tiempo UNIX
     */
    long datetimeEpoch = 1703991600;

    /*
     * Seccion de prueba
     */
    ClimateRecord givenClimateRecord = ClimateClient.getForecast(newParcel, datetimeEpoch);

    System.out.println("* Registro climatico obtenido *");
    System.out.println(givenClimateRecord);

    assertNotNull(givenClimateRecord);

    System.out.println("* Prueba pasada satisfactoriamente *");
    System.out.println();
  }

  @Test
  public void testTwoClimateClient() {
    System.out.println("***************************** Prueba dos del cliente climatico *****************************");
    System.out.println("Observacion historica");
    System.out.println("- En esta prueba se obtienen los datos meteorologicos para la ubicacion geografica dada por");
    System.out.println("la coordenada geografica (-38.71959, -62.27243) [Bahia Blanca] en la fecha 2023-01-01.");
    System.out.println();

    Parcel newParcel = new Parcel();
    newParcel.setName("Bahia Blanca");
    newParcel.setLatitude(-38.71959);
    newParcel.setLongitude(-62.27243);
    newParcel.setHectares(2);

    /*
     * 1 de enero de 2023 en tiempo UNIX
     */
    long datetimeEpoch = 1672542000;

    /*
     * Seccion de prueba
     */
    ClimateRecord givenClimateRecord = ClimateClient.getForecast(newParcel, datetimeEpoch);

    System.out.println("* Registro climatico obtenido *");
    System.out.println(givenClimateRecord);

    assertNotNull(givenClimateRecord);

    System.out.println("* Prueba pasada satisfactoriamente *");
    System.out.println();
  }

  @Test
  public void testThreeClimateClient() {
    System.out.println("***************************** Prueba tres del cliente climatico *****************************");
    System.out.println("Pronostico de condiciones actuales");
    System.out.println("- En esta prueba se obtienen los datos meteorologicos para la ubicacion geografica dada por");
    System.out.println("la coordenada geografica (-42.7692, -65.03851) [Puerto Madryn] en la fecha actual.");
    System.out.println();

    Parcel newParcel = new Parcel();
    newParcel.setName("Puerto Madryn");
    newParcel.setLatitude(-42.7692);
    newParcel.setLongitude(-65.03851);
    newParcel.setHectares(2);

    /*
     * El objeto de tipo Calendar referenciado por la referencia
     * de esta variable de tipo por referencia, contiene la fecha
     * actual
     */
    Calendar currentDate = Calendar.getInstance();

    /*
     * Seccion de prueba.
     * 
     * La fecha dada en milisegundos se divide por 1000 para
     * convetirla a segundos, ya que el metodo getForecast de
     * la clase ClimateClient opera con segundos desde la epoca
     * (1 de enero de 1970) para obtener los datos meteorologicos
     * de una ubicacion geografica en una fecha dada.
     */
    ClimateRecord givenClimateRecord = ClimateClient.getForecast(newParcel, (currentDate.getTimeInMillis() / 1000));

    System.out.println("* Registro climatico obtenido *");
    System.out.println(givenClimateRecord);

    assertNotNull(givenClimateRecord);

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
