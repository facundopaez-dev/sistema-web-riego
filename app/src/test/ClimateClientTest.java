import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.Collection;
import java.util.ArrayList;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Ignore;
import model.ClimateRecord;
import model.GeographicLocation;
import model.Parcel;
import model.TypePrecipitation;
import util.UtilDate;
import climate.ClimateClient;

public class ClimateClientTest {

  private static Collection<TypePrecipitation> typesPrecip;

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
  public static void preTest(){
    TypePrecipitation typePrecipOne = new TypePrecipitation();
    typePrecipOne.setName("rain");
    typePrecipOne.setSpanishName("Lluvia");

    TypePrecipitation typePrecipTwo = new TypePrecipitation();
    typePrecipTwo.setName("ice");
    typePrecipTwo.setSpanishName("Granizo");

    TypePrecipitation typePrecipThree = new TypePrecipitation();
    typePrecipThree.setName("snow");
    typePrecipThree.setSpanishName("Nieve");

    TypePrecipitation typePrecipFour = new TypePrecipitation();
    typePrecipFour.setName("freezing rain");
    typePrecipFour.setSpanishName("Lluvia gélida");

    typesPrecip = new ArrayList<>();
    typesPrecip.add(typePrecipOne);
    typesPrecip.add(typePrecipTwo);
    typesPrecip.add(typePrecipThree);
    typesPrecip.add(typePrecipFour);
  }

  @Test
  public void testOne() {
    Calendar date = Calendar.getInstance();
    date.set(Calendar.YEAR, 2023);
    date.set(Calendar.MONTH, DECEMBER);
    date.set(Calendar.DAY_OF_MONTH, 31);

    double latitude = 38.89511;
    double longitude = -77.03637;

    System.out.println("***************************** Prueba uno del cliente climatico *****************************");
    System.out.println("Pronostico");
    System.out.println("- En esta prueba se obtienen los datos meteorologicos para la ubicacion geografica dada por");
    System.out.println("la coordenada geografica (" + latitude + ", " + longitude + ") [Washington] en la fecha " + UtilDate.convertDateToDdMmYyyy(date) + ".");
    System.out.println();

    GeographicLocation newGeographicLocation = new GeographicLocation();
    newGeographicLocation.setLatitude(latitude);
    newGeographicLocation.setLongitude(longitude);

    Parcel newParcel = new Parcel();
    newParcel.setName("Washington");
    newParcel.setGeographicLocation(newGeographicLocation);
    newParcel.setHectares(2);

    /*
     * Seccion de prueba
     */
    ClimateRecord climateRecord = null;

    try {
      climateRecord = ClimateClient.getForecast(newParcel, date, typesPrecip);
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
  public void testTwo() {
    Calendar date = Calendar.getInstance();
    date.set(Calendar.YEAR, 2023);
    date.set(Calendar.MONTH, JANUARY);
    date.set(Calendar.DAY_OF_MONTH, 1);

    double latitude = 40.4165;
    double longitude = -3.70256;

    System.out.println("***************************** Prueba dos del cliente climatico *****************************");
    System.out.println("Observacion historica");
    System.out.println("- En esta prueba se obtienen los datos meteorologicos para la ubicacion geografica dada por");
    System.out.println("la coordenada geografica (" + latitude + ", " + longitude + ") [Madrid] en la fecha " + UtilDate.convertDateToDdMmYyyy(date) + ".");
    System.out.println();

    GeographicLocation newGeographicLocation = new GeographicLocation();
    newGeographicLocation.setLatitude(latitude);
    newGeographicLocation.setLongitude(longitude);

    Parcel newParcel = new Parcel();
    newParcel.setName("Madrid");
    newParcel.setGeographicLocation(newGeographicLocation);
    newParcel.setHectares(2);

    /*
     * Seccion de prueba
     */
    ClimateRecord climateRecord = null;

    try {
      climateRecord = ClimateClient.getForecast(newParcel, date, typesPrecip);
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
  public void testThree() {
    double latitude = 51.50853;
    double longitude = -0.12574;

    System.out.println("***************************** Prueba tres del cliente climatico *****************************");
    System.out.println("Pronostico de condiciones actuales");
    System.out.println("- En esta prueba se obtienen los datos meteorologicos para la ubicacion geografica dada por");
    System.out.println("la coordenada geografica (" + latitude + ", " + longitude + ") [Londres] en la fecha actual (es decir,");
    System.out.println("hoy).");
    System.out.println();

    GeographicLocation newGeographicLocation = new GeographicLocation();
    newGeographicLocation.setLatitude(latitude);
    newGeographicLocation.setLongitude(longitude);

    Parcel newParcel = new Parcel();
    newParcel.setName("Londres");
    newParcel.setGeographicLocation(newGeographicLocation);
    newParcel.setHectares(2);

    Calendar currentDate = UtilDate.getCurrentDate();

    /*
     * Seccion de prueba
     */
    ClimateRecord climateRecord = null;

    try {
      climateRecord = ClimateClient.getForecast(newParcel, currentDate, typesPrecip);
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