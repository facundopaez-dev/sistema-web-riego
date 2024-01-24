import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Calendar;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import model.ClimateRecord;
import model.Parcel;
import util.UtilDate;
import climate.ClimateClient;

public class ClimateClientTest {

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

  @Test
  public void testOneClimateClient() {
    Calendar date = Calendar.getInstance();
    date.set(Calendar.YEAR, 2023);
    date.set(Calendar.MONTH, DECEMBER);
    date.set(Calendar.DAY_OF_MONTH, 31);

    double latitude = -45.86413;
    double longitude = -67.49656;

    System.out.println("***************************** Prueba uno del cliente climatico *****************************");
    System.out.println("Pronostico");
    System.out.println("- En esta prueba se obtienen los datos meteorologicos para la ubicacion geografica dada por");
    System.out.println("la coordenada geografica (" + latitude + ", " + longitude + ") [Comodoro Rivadavia] en la fecha " + UtilDate.convertDateToDdMmYyyy(date) + ".");
    System.out.println();

    Parcel newParcel = new Parcel();
    newParcel.setName("Comodoro Rivadavia");
    newParcel.setLatitude(latitude);
    newParcel.setLongitude(longitude);
    newParcel.setHectares(2);

    /*
     * Seccion de prueba
     */
    ClimateRecord climateRecord = null;

    try {
      climateRecord = ClimateClient.getForecast(newParcel, date);
    } catch (Exception e) {
      System.out.println("Codigo de estado de respuesta HTTP: " + e.getMessage());
    }

    System.out.println("* Registro climatico obtenido *");
    System.out.println(climateRecord);

    assertNotNull(climateRecord);

    System.out.println("* Prueba pasada satisfactoriamente *");
    System.out.println();
  }

  @Test
  public void testTwoClimateClient() {
    Calendar date = Calendar.getInstance();
    date.set(Calendar.YEAR, 2023);
    date.set(Calendar.MONTH, JANUARY);
    date.set(Calendar.DAY_OF_MONTH, 1);

    double latitude = -38.71959;
    double longitude = -62.27243;

    System.out.println("***************************** Prueba dos del cliente climatico *****************************");
    System.out.println("Observacion historica");
    System.out.println("- En esta prueba se obtienen los datos meteorologicos para la ubicacion geografica dada por");
    System.out.println("la coordenada geografica (" + latitude + ", " + longitude + ") [Bahia Blanca] en la fecha " + UtilDate.convertDateToDdMmYyyy(date) + ".");
    System.out.println();

    Parcel newParcel = new Parcel();
    newParcel.setName("Bahia Blanca");
    newParcel.setLatitude(latitude);
    newParcel.setLongitude(longitude);
    newParcel.setHectares(2);

    /*
     * Seccion de prueba
     */
    ClimateRecord climateRecord = null;

    try {
      climateRecord = ClimateClient.getForecast(newParcel, date);
    } catch (Exception e) {
      System.out.println("Codigo de estado de respuesta HTTP: " + e.getMessage());
    }

    System.out.println("* Registro climatico obtenido *");
    System.out.println(climateRecord);

    assertNotNull(climateRecord);

    System.out.println("* Prueba pasada satisfactoriamente *");
    System.out.println();
  }

  @Test
  public void testThreeClimateClient() {
    double latitude = -42.7692;
    double longitude = -65.03851;

    System.out.println("***************************** Prueba tres del cliente climatico *****************************");
    System.out.println("Pronostico de condiciones actuales");
    System.out.println("- En esta prueba se obtienen los datos meteorologicos para la ubicacion geografica dada por");
    System.out.println("la coordenada geografica (" + latitude + ", " + longitude + ") [Puerto Madryn] en la fecha actual (es decir,");
    System.out.println("hoy).");
    System.out.println();

    Parcel newParcel = new Parcel();
    newParcel.setName("Puerto Madryn");
    newParcel.setLatitude(latitude);
    newParcel.setLongitude(longitude);
    newParcel.setHectares(2);

    Calendar currentDate = UtilDate.getCurrentDate();

    /*
     * Seccion de prueba
     */
    ClimateRecord climateRecord = null;

    try {
      climateRecord = ClimateClient.getForecast(newParcel, currentDate);
    } catch (Exception e) {
      System.out.println("Codigo de estado de respuesta HTTP: " + e.getMessage());
    }

    System.out.println("* Registro climatico obtenido *");
    System.out.println(climateRecord);

    assertNotNull(climateRecord);

    System.out.println("* Prueba pasada satisfactoriamente *");
    System.out.println();
  }

}