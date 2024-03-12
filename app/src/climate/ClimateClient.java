package climate;

import javax.net.ssl.TrustManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;
import javax.net.ssl.HttpsURLConnection;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import com.google.gson.Gson;
import model.ClimateRecord;
import model.Parcel;
import model.TypePrecipitation;
import weatherApiClasses.Day;
import weatherApiClasses.Forecast;
import util.UtilDate;

/*
 * ClimateCliente es la clase que se utiliza para obtener datos
 * meteorologicos. Esto se hace mediante una peticion HTTP a la
 * API Visual Crossing Weather.
 * 
 * Los datos meteorologicos se obtienen en base a una fecha y a
 * una ubicacion geografica dada por latitud y longitud. Estos
 * datos son necesarios para calcular la ETo (evapotranspiracion
 * del cultivo de referencia (*)) [mm/dia], la cual es necesaria
 * para calcular la ETc (evapotranspiracion del cultivo bajo
 * condiciones estandar) [mm/dia], la cual indica la cantidad
 * de agua que evapotranspira un cultivo dado. Gracias a la ETc
 * se sabe la cantidad de agua que se debe reponer a un cultivo
 * mediante precipitacion artificial o natural.
 * 
 * (*) El cultivo de referencia es el pasto, segun la pagina 6
 * del libro Evapotranspiracion del cultivo, estudio FAO riego
 * y drenaje 56.
 */
public class ClimateClient {

  /*
   * URL incompleto del servicio web a utilizar para
   * obtener los datos meteorologicos en base a una fecha
   * y una ubicacion geografica
   */
  private static final String INCOMPLETE_WEATHER_URL = "https://weather.visualcrossing.com/VisualCrossingWebServices/rest/services/timeline/";
  private static final String API_KEY = "key=YOUR_VISUAL_CROSSING_WEATHER_API_KEY";

  /*
   * Esta cadena de consulta establece que el conjunto de
   * datos devuelto por la API Visual Crossing Weather
   * tenga las unidades del grupo de undidades metric,
   * que incluya solo el arreglo "days", que los elementos
   * que contenga sean el tiempo desde la epoca (1 de enero
   * de 1970), la temperatura maxima, la temperatura minima,
   * el punto de rocio, la humedad, la precipitacion, la
   * probabilidad de precipitacion, el tipo de precipitacion
   * (rain, snow, freezing rain, ice), la velocidad del
   * viento, la presion atmosferica y la nubosidad
   */
  private static final String QUERY_STRING = "&unitGroup=metric&include=days&elements=datetimeEpoch,tempmax,tempmin,dew,humidity,precip,precipprob,preciptype,windspeed,pressure,cloudcover";

  /*
   * Biblioteca creada por Google para convertir formato
   * JSON a POJO (Plain Old Java Object) y viceversa
   */
  private static Gson gson = new Gson();

  /**
   * Retorna los datos meteorologicos de una fecha y una
   * ubicacion geografica.
   * 
   * @param parcel
   * @param date
   * @param typesPrecip
   * @return referencia a un objeto de tipo ClimateRecord que
   * contiene los datos meteorologicos obtenidos en funcion
   * de una fecha y una ubicacion geografica
   */
  public static ClimateRecord getForecast(Parcel parcel, Calendar date, Collection<TypePrecipitation> typesPrecip) throws IOException {
    ClimateRecord newClimateRecord = new ClimateRecord();
    newClimateRecord.setParcel(parcel);

    /*
     * Obtiene los datos meteorologicos para una parcela
     * mediante su latitud y longitud, en una fecha dada
     */
    Forecast newForecast = requestWeatherData(parcel.getGeographicLocation().getLatitude(), parcel.getGeographicLocation().getLongitude(), date);

    /*
     * Asigna los datos meteorologicos contenidos en el objeto
     * referenciado por la referencia de forecast al objeto
     * referenciado por la referencia de climateRecord
     */
    assignClimateData(newClimateRecord, newForecast, typesPrecip);
    return newClimateRecord;
  }

  /**
   * Retorna los datos meteorologicos de una fecha y una
   * ubicacion geografica.
   * 
   * ATENCION: Este metodo es implementado para ser utilizado
   * por el metodo getForecast() de prueba de la clase
   * ClimateRecordRestServlet. El getForecast() es unicamente
   * para probar que la invocacion a la API climatica Visual
   * Crossing Weather funciona correctamente.
   * 
   * @param latitude
   * @param longitude
   * @param date
   * @param typesPrecip
   * @return referencia a un objeto de tipo ClimateRecord que
   * contiene los datos meteorologicos obtenidos en funcion
   * de una fecha y una ubicacion geografica
   */
  public static ClimateRecord getForecast(double latitude, double longitude, Calendar date, Collection<TypePrecipitation> typesPrecip) throws IOException {
    ClimateRecord newClimateRecord = new ClimateRecord();

    /*
     * Obtiene los datos meteorologicos para una parcela
     * mediante su latitud y longitud, en una fecha dada
     */
    Forecast newForecast = requestWeatherData(latitude, longitude, date);

    /*
     * Asigna los datos meteorologicos contenidos en el objeto
     * referenciado por la referencia de forecast al objeto
     * referenciado por la referencia de climateRecord
     */
    assignClimateData(newClimateRecord, newForecast, typesPrecip);
    return newClimateRecord;
  }

  /**
   * Obtiene los datos meteorologicos de una fecha y una
   * ubicacion geografica
   * 
   * @param latitude      [grados decimales]
   * @param longitude     [grados decimales]
   * @param date
   * @return referencia a un objeto de tipo Forecast que
   * contiene los datos meteorologicos obtenidos para una
   * latitud y una longitud, en una fecha en tiempo UNIX
   */
  private static Forecast requestWeatherData(double latitude, double longitude, Calendar date) throws IOException {
    /*
     * Si no se agrega este bloque de codigo antes del bloque de
     * codigo que realiza la invocacion a la API climatica Visual
     * Crossing Weather, la aplicacion del lado servidor lanza la
     * siguiente excepcion:
     * 
     * javax.net.ssl.SSLHandshakeException: sun.security.validator.ValidatorException:
     * PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException:
     * unable to find valid certification path to requested target
     * 
     * El motivo de esta excepcion es que la JVM no confia en el
     * certificado SSL. El certificado puede estar autofirmado o
     * puede estar firmado por una CA (autoridad de certificacion)
     * cuyo certificado no se encuentra en el almacenamiento de
     * certificados de la JVM.
     * 
     * Lo que se debe hacer para solucionar este problema es agregar
     * codigo fuente para confiar en el certificado proporcionado por
     * el host y para importar el certificado antes de consumir el URL
     * de peticion de la API climatica. Esto es lo que hace el siguiente
     * bloque de codigo fuente.
     * 
     * Esta excepcion surgio luego de eliminar la carpeta sun del
     * archivo grizzly-npn-bootstrap.jar de la siguiente ruta:
     * glassfish5/glassfish/modules/endorsed.
     * 
     * El motivo por el cual se elimino esta carpeta es para evitar
     * que la aplicacion lance la sigueinte excepcion al invocar a
     * la API climatica:
     * 
     * javax.servlet.ServletException: org.glassfish.jersey.server.ContainerException:
     * java.lang.NoClassDefFoundError: sun/security/ssl/HelloExtension
     */
    TrustManager[] trustAllCerts = new TrustManager[] {
        new X509TrustManager() {
          public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return null;
          }

          public void checkClientTrusted(
              java.security.cert.X509Certificate[] certs, String authType) {
          }

          public void checkServerTrusted(
              java.security.cert.X509Certificate[] certs, String authType) {
          }
        } };

    try {
      SSLContext sc = SSLContext.getInstance("SSL");
      sc.init(null, trustAllCerts, new java.security.SecureRandom());
      HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
    } catch (Exception e) {
      e.printStackTrace();
    }

    /*
     * Contendra el resultado de la llamada a la API
     * meteorologica utilizada (en este caso Visual
     * Crossing Weather) en formato JSON
     */
    String resultApiCall = null;
    BufferedReader bufferedReader = null;
    InputStreamReader inputStreamReader = null;
    HttpURLConnection httpUrlConnection = null;

    try {
      URL url = new URL(getCompleteWeatherUrl(latitude, longitude, date));

      httpUrlConnection = (HttpURLConnection) url.openConnection();
      httpUrlConnection.setRequestMethod("GET");
      httpUrlConnection.setRequestProperty("Accept", "application/json");

      inputStreamReader = new InputStreamReader(httpUrlConnection.getInputStream());
      bufferedReader = new BufferedReader(inputStreamReader);
      } catch (Exception e) {
        e.printStackTrace();

        /*
         * Si el codigo de respuesta del servidor sobre el cual
         * se invoca el servicio web deseado es distinto de 200
         * (Ok), significa que hubo un problema al realizar la
         * peticion, con lo cual se lanza una excepcion con el
         * codigo de la respuesta
         */
      if (httpUrlConnection.getResponseCode() != 200) {
        throw new RuntimeException(String.valueOf(httpUrlConnection.getResponseCode()));
      }

    }

    resultApiCall = bufferedReader.readLine();

    /*
     * De esta forman se liberan estos
     * recursos utilizados lo que probablemente
     * libere el espacio de memoria que esten
     * utilizando estas variables
     */
    if (inputStreamReader != null) {
      inputStreamReader.close();
    }

    if (httpUrlConnection != null) {
      httpUrlConnection.disconnect();
    }

    /*
     * Convierte el conjunto de datos meteorologicos obtenido
     * para una ubicacion geografica en una fecha dada, de
     * formato JSON a formato POJO
     */
    return gson.fromJson(resultApiCall, Forecast.class);
  }

  /**
   * Retorna el URL completo con la latitud, la longitud, la fecha
   * en formato yyyy-MM-dd, la clave proporcionada por la API Visual
   * Crossing Weather, el grupo de unidades de medida metric y el
   * parametro de consulta include para obtener los siguientes datos
   * meteorologicos correspondientes a una fecha y una ubicacion
   * geografica:
   * - tiempo desde la epoca (1 de enero de 1970)
   * - temperatura maxima
   * - temperatura minima
   * - punto de rocio
   * - humedad
   * - precipitacion
   * - probabilidad de precipitacion
   * - tipo de precipitacion
   * - velocidad del viento
   * - presion atmosferica
   * - nubosidad
   * 
   * @param latitude      [grados decimales]
   * @param longitude     [grados decimales]
   * @param date
   * @return referencia a un objeto de tipo String que contiene el URL
   * necesario para realizar una solicitud HTTP a la API meteorologica
   * Visual Crossing Weather
   */
  private static String getCompleteWeatherUrl(double latitude, double longitude, Calendar date) {
    return INCOMPLETE_WEATHER_URL + latitude + "," + longitude + "/" + UtilDate.convertDateToYyyyMmDdFormat(date) + "?" + API_KEY + QUERY_STRING;
  }

  /**
   * Asigna los datos meteorologicos contenidos en el objeto
   * referenciado por la referencia de forecast al objeto
   * referenciado por la referencia de climateRecord
   * 
   * @param climateRecord
   * @param forecast
   * @param typesPrecip
   */
  private static void assignClimateData(ClimateRecord climateRecord, Forecast forecast, Collection<TypePrecipitation> typesPrecip) {
    /*
     * Obtencion de los datos meteorologicos solicitados
     * en un dia (una fecha) para una ubicacion geografica
     */
    Day day = forecast.getDays().get(0);
    Calendar date = Calendar.getInstance();

    /*
     * El tiempo devuelto por Visual Crossing Weather es el
     * numero de segundos desde la epoca (1 de enero de 1970)
     * en formato UTC. Esto es que esta en tiempo UNIX. Para
     * convertirlo a milisegundos se lo debe multiplicar por
     * 1000.
     */
    date.setTimeInMillis(day.getDatetimeEpoch() * 1000);

    climateRecord.setDate(date);
    climateRecord.setDewPoint(day.getDew());
    climateRecord.setAtmosphericPressure(day.getPressure());

    /*
     * Los datos meteorologicos obtenidos de una llamada a la API
     * Visual Crossing Weather utilizan el grupo de unidades metric,
     * segun la cadena de consulta de la constante QUERY_STRING.
     * 
     * La documentacion de Visual Crossing Weather del siguiente
     * enlace:
     * 
     * https://www.visualcrossing.com/resources/documentation/weather-api/unit-groups-and-measurement-units/
     * 
     * indica que con el grupo de unidades metric, la velocidad
     * del viento esta medida en kilometros por hora.
     * 
     * La formula de la ETo requiere que la velocidad del viento
     * este medida en metros por segundo. Por este motivo es
     * necesario que se convierta, en alguna parte pertinente
     * del codigo fuente, la velocidad del viento de kilometros
     * por hora a metros por segundo antes de usar este dato
     * meteorologico en el metodo que calcula la ETo, el cual,
     * se llama getEto y pertenece a la clase Eto.
     */
    climateRecord.setWindSpeed(day.getWindspeed());
    climateRecord.setCloudCover(day.getCloudcover());
    climateRecord.setMinimumTemperature(day.getTempmin());
    climateRecord.setMaximumTemperature(day.getTempmax());

    /*
     * Visual Crossing Weather retorna cuatro tipos de precipitaciones:
     * rain, snow, freezing rain e ice.
     * 
     * Visual Crossing Weather utiliza el valor null dentro del conjunto
     * de datos meteorologicos para indicar la ausencia de datos, como
     * informacion meteorologica faltante o datos desconocidos. No se
     * utiliza el valor null para indicar un valor cero. Por ejemplo,
     * un valor de precipitacion desconocido se marcara como vacio o
     * null. Una cantidad cero de precipitacion se indicara con el
     * valor cero. Este es el motivo por el cual se realiza un control
     * sobre el tipo de precipitacion verificando si tiene el valor
     * null.
     */
    if ((day.getPreciptype() != null)) {
      /*
       * Segun la documentacion de Visual Crossing Weather del siguiente
       * enlace:
       * 
       * https://www.visualcrossing.com/resources/documentation/weather-data/weather-data-documentation/
       * 
       * hay cuatro tipos de precipitaciones: rain, freezing rain, snow e ice.
       * 
       * El ingeniero agronomo que me asesora me dijo que se debe utilizar
       * todo tipo de precipitacion. Me dijo que tanto la nieve como el
       * granizo (debe ser ice) aportan agua cuando se derriten. No menciono
       * a la lluvia helada (freezing rain), pero por lo que me dijo de la
       * nieve y el granizo, se entiende que la lluvia helada tambien aporta
       * agua cuando se derrite, por lo tanto, tambien se la debe utilizar.
       * 
       * En cuanto a la nieve me dijo que 1 centimetro de nieve NO son 10
       * milimetros de agua, sino que es 1 milimetro de agua. Por lo tanto,
       * en un ejemplo en el que haya 10 centimetros de nieve, hay en total
       * 10 milimetros de agua. Esta la conversion de nieve a agua liquida.
       * 
       * Si el grupo de unidades utilizado para solicitar datos meteorologicos
       * de Visual Crossing Weather es metric, precip es la cantidad de precipitacion
       * en milimetros, segun la documentacion de dicha API del siguiente enlace:
       * 
       * https://www.visualcrossing.com/resources/documentation/weather-api/unit-groups-and-measurement-units/
       * 
       * Por lo tanto, si se usa el grupo de unidades metric para solicitar
       * datos meteorologicos a la API Visual Crossing Weather y el tipo de
       * precipitacion en una llamada a dicha API es nieve, no es necesario
       * realizar la conversion de nieve a agua liquida.
       */
      climateRecord.setPrecip(day.getPrecip());
      assignPrecipTypes(day.getPrecip(), climateRecord, typesPrecip, day.getPreciptype());
      climateRecord.setPrecipProbability(day.getPrecipprob());
    }

    /*
     * Si el tipo de precipitacion tiene el valor null en el conjunto
     * de datos meteorologicos devuelto por una llamada a la API Visual
     * Crossing Weather (VSC), las variables de instancia precip y
     * precipProbability de una instancia de tipo ClimateRecord se
     * inicializan en 0.0, debido a que son variables de instancia
     * de tipo double, el cual es un tipo primitivo en Java.
     */
  }

  /**
   * Asigna los tipos de precipitacion a un registro climatico
   * 
   * @param precip
   * @param climateRecord
   * @param typesPrecip
   * @param precipTypesData
   */
  private static void assignPrecipTypes(double precip, ClimateRecord climateRecord, Collection<TypePrecipitation> typesPrecip, Collection<String> precipTypesData) {
    List<TypePrecipitation> listTypesPrecip = (List) typesPrecip;

    /*
     * La API climate Visual Crossing Weather tiene cuatro
     * tipos de precipitacion: rain, freezing rain, snow e ice.
     * Por lo tanto, en base a esta cantidad de precipitaciones
     * se asignan las precipitaciones de un registro climatico.
     */
    TypePrecipitation typePrecipOne = listTypesPrecip.get(0);
    TypePrecipitation typePrecipTwo = listTypesPrecip.get(1);
    TypePrecipitation typePrecipThree = listTypesPrecip.get(2);
    TypePrecipitation typePrecipFour = listTypesPrecip.get(3);

    /*
     * Si la precipitacion es estrictamente mayor a 0.0, se
     * asignan los tipos de precipitacion a un registro
     * climatico
     */
    if (precip > 0.0) {

      /*
       * Si uno de los tipos de precipitacion devueltos por
       * la API climatica Visual Crossing Weather es igual
       * a los tipos de precipitacion almacenados en la
       * base de datos subyacente, se asigna a un registro
       * climatico.
       * 
       * La coleccion typesPrecip contiene los tipos de
       * precipitacion almacenados en la base de datos
       * subyacente.
       */
      for (String currentPrecipTypeData : precipTypesData) {

        if (currentPrecipTypeData.equals(typePrecipOne.getName())) {
          climateRecord.setTypePrecipOne(typePrecipOne);
        }

        if (currentPrecipTypeData.equals(typePrecipTwo.getName())) {
          climateRecord.setTypePrecipTwo(typePrecipTwo);
        }

        if (currentPrecipTypeData.equals(typePrecipThree.getName())) {
          climateRecord.setTypePrecipThree(typePrecipThree);
        }

        if (currentPrecipTypeData.equals(typePrecipFour.getName())) {
          climateRecord.setTypePrecipFour(typePrecipFour);
        }

      } // End for

    } // End if

  }

}
