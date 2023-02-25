package climate;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import model.ClimateRecord;
import model.Parcel;
import weatherApiClasses.Day;
import weatherApiClasses.Forecast;
import javax.net.ssl.TrustManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;
import javax.net.ssl.HttpsURLConnection;

/*
 * ClimateCliente es la clase que se utiliza para obtener datos
 * metereologicos. Esto se hace mediante una peticion a la API
 * Visual Crossing Weather.
 * 
 * Los datos metereologicos se obtienen en base a una ubicacion
 * geografica dada por latitud y longitud, y a una fecha dada.
 * Estos datos son necesarios para calcular la evapotranspiracion
 * del cultivo de referencia (*) [ETo], la cual, nos indica la cantidad
 * de agua que va a evaporar un cultivo dado, y al saber esto
 * sabremos la cantidad de agua que tendremos que reponer al
 * cultivo mediante el riego.
 * 
 * (*) El cultivo de referencia es el pasto, segun la pagina
 * 6 del libro Evapotranspiracion del cultivo, estudio FAO
 * riego y drenaje 56.
 */
public class ClimateClient {

  /*
   * URL incompleto del servicio web a utilizar para
   * obtener los datos metereologicos en base a una fecha
   * y una coordenada geografica
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
   * JSON a POJO y viceversa
   */
  private static Gson gson = new Gson();

  /**
   * Retorna los datos metereologicos para una ubicacion
   * geografica en una fecha dada.
   * 
   * @param givenParcel
   * @param datetimeEpoch [tiempo UNIX]
   * @return referencia a un objeto de tipo ClimateRecord que
   *         contiene los datos metereologicos obtenidos en base a una
   *         coordenada geografica y una fecha en tiempo UNIX
   */
  public static ClimateRecord getForecast(Parcel givenParcel, long datetimeEpoch) {
    ClimateRecord newClimateRecord = new ClimateRecord();
    newClimateRecord.setParcel(givenParcel);

    /*
     * Obtiene los datos metereologicos para una parcela
     * mediante su latitud y longitud, en una fecha dada
     */
    Forecast newForecast = requestWeatherData(givenParcel.getLatitude(), givenParcel.getLongitude(), datetimeEpoch);

    /*
     * Asigna los datos metereologicos contenidos en el objeto
     * referenciado por la referencia de givenForecast al objeto
     * referenciado por la referencia de givenClimateRecord
     */
    setClimateRecord(newClimateRecord, newForecast);
    return newClimateRecord;
  }

  /**
   * Obtiene los datos metereologicos para una ubicacion geografica
   * en una fecha dada
   * 
   * @param latitude      [grados decimales]
   * @param longitude     [grados decimales]
   * @param datetimeEpoch [tiempo UNIX]
   * @return referencia a un objeto de tipo Forecast que
   *         contiene los datos metereologicos obtenidos para una
   *         latitud y una longitud, en una fecha en tiempo UNIX
   */
  private static Forecast requestWeatherData(double latitude, double longitude, long datetimeEpoch) {
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
    }

    /*
     * Contendra el resultado de la llamada a la API
     * metereologica utilizada (en este caso Visual
     * Crossing Weather) en formato JSON
     */
    String resultApiCall = null;

    try {
      URL url = new URL(getCompleteWeatherUrl(latitude, longitude, datetimeEpoch));

      HttpURLConnection httpUrlConnection = (HttpURLConnection) url.openConnection();
      httpUrlConnection.setRequestMethod("GET");
      httpUrlConnection.setRequestProperty("Accept", "application/json");

      /*
       * Si el codigo de respuesta del servidor sobre el cual
       * se invoca el servicio web deseado es distinto de 200
       * (ok, sin problemas) hay un problema, con lo cual
       * se lanza una excepcion
       */
      if (httpUrlConnection.getResponseCode() != 200) {
        throw new RuntimeException("Failed : HTTP Error code : " + httpUrlConnection.getResponseCode());
      }

      InputStreamReader inputStreamReader = new InputStreamReader(httpUrlConnection.getInputStream());
      BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

      resultApiCall = bufferedReader.readLine();

      /*
       * De esta forman se liberan estos
       * recursos utilizados lo que probablemente
       * libere el espacio de memoria que esten
       * utilizando estas variables
       */
      httpUrlConnection.disconnect();
      inputStreamReader.close();
    } catch (Exception e) {
      e.printStackTrace();
    }

    /*
     * Convierte el conjunto de datos metereologicos obtenido
     * para una ubicacion geografica en una fecha dada, de
     * formato JSON a formato POJO
     */
    return gson.fromJson(resultApiCall, Forecast.class);
  }

  /**
   * Retorna el URL completo con la latitud, la longitud, la fecha en
   * tiempo UNIX, la clave proporcionada por la API Visual Crossing
   * Weather, el sistema de unidad de medidas metric, el parametro de
   * consulta include para obtener los datos metereologicos de un dia,
   * el tiempo desde la epoca (1 de enero de 1970), la temperatura maxima,
   * la temperatura minima, el punto de rocio, la humedad, la precipitacion
   * por hora, la probabilidad de precipitacion, el tipo de precipitacion,
   * la velocidad del viento, la presion atmosferica y la nubosidad
   * 
   * @param latitude      [grados decimales]
   * @param longitude     [grados decimales]
   * @param datetimeEpoch [tiempo UNIX]
   * @return referencia a un objeto de tipo String que contiene el URL necesario
   *         para realizar una solicitud a la API metereologica Visual Crossing
   *         Weather
   */
  private static String getCompleteWeatherUrl(double latitude, double longitude, long datetimeEpoch) {
    return INCOMPLETE_WEATHER_URL + latitude + "," + longitude + "/" + datetimeEpoch + "?" + API_KEY + QUERY_STRING;
  }

  /**
   * Asigna los datos metereologicos contenidos en el objeto
   * referenciado por la referencia de givenForecast al objeto
   * referenciado por la referencia de givenClimateRecord
   * 
   * @param givenClimateRecord
   * @param givenForecast
   */
  private static void setClimateRecord(ClimateRecord givenClimateRecord, Forecast givenForecast) {
    /*
     * Obtencion de los datos metereologicos solicitados
     * en un dia (una fecha) para una ubicacion geografica
     */
    Day givenDay = givenForecast.getDays().get(0);

    Calendar date = Calendar.getInstance();

    /*
     * El tiempo UNIX esta en segundos, con lo cual, se lo
     * debe multiplicar por 1000 para convetirlo en milisegundos
     */
    date.setTimeInMillis(givenDay.getDatetimeEpoch() * 1000);

    givenClimateRecord.setDate(date);
    givenClimateRecord.setDewPoint(givenDay.getDew());
    givenClimateRecord.setAtmosphericPressure(givenDay.getPressure());

    /*
     * Los datos metereologicos obtenidos de una llamada a la API
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
     * este medida en metros por segundo. Por este motivo se
     * convierte la velocidad del viento de kilometros por hora
     * a metros por segundo dentro del metodo setWindSpeed de
     * la clase ClimateRecord.
     */
    givenClimateRecord.setWindSpeed(givenDay.getWindspeed());
    givenClimateRecord.setCloudCover(givenDay.getCloudcover());
    givenClimateRecord.setMinimumTemperature(givenDay.getTempmin());
    givenClimateRecord.setMaximumTemperature(givenDay.getTempmax());

    /*
     * Visual Crossing Weather retorna cuatro tipos de precipitaciones:
     * rain, snow, freezing rain e ice.
     * 
     * Visual Crossing Weather utiliza el valor null dentro del conjunto
     * de datos metereologicos para indicar la ausencia de datos, como
     * informacion metereologica faltante o datos desconocidos. No se
     * utiliza el valor null para indicar un valor cero. Por ejemplo,
     * un valor de precipitación desconocido se marcará como vacío o
     * null. Una cantidad cero de precipitación se indicará con el
     * valor cero. Este es el motivo por el cual se realiza un control
     * sobre el tipo de precipitacion verificando si tiene el valor
     * null.
     */
    if ((givenDay.getPreciptype() != null)) {
      givenClimateRecord.setPrecipPerHour(givenDay.getPrecip());
      givenClimateRecord.setPrecipProbability(givenDay.getPrecipprob());

      /*
       * Segun la documentacion de Visual Crossing Weather del siguiente
       * enlace:
       * https://www.visualcrossing.com/resources/documentation/weather-data/weather-data-documentation/
       * 
       * hay cuatro tipos de precipitaciones: rain, freezing rain, snow e ice.
       * 
       * El ingeniero agronomo que me asesora me dijo que se debe usar todo
       * tipo de precipitacion. Me dijo que tanto la nieve como el granizo
       * (debe ser ice) aportan agua cuando se derriten. No menciono a la
       * lluvia helada (freezing rain), pero por lo que me dijo de la nieve
       * y el granizo, se entiende que la lluvia helada tambien aporta agua
       * cuando se derrite.
       * 
       * En cuanto a la nieve me dijo que 1 centimetro de nieve NO son 10
       * milimetros de agua, sino que es 1 milimetro de agua. Por lo tanto,
       * en un ejemplo en el que haya 10 centimetros de nieve, hay en total
       * 10 milimetros de agua. Esta la conversion de nieve a agua liquida.
       * 
       * El dato metereologico precip indica la cantidad de precipitacion
       * por hora en el dia de la fecha y la ubicacion para la cual se
       * solicitan datos metereologicos. Esto es lo que dice la documentacion
       * de Visual Crossing Weather del siguiente enlace:
       * 
       * https://www.visualcrossing.com/resources/blog/how-to-replace-the-dark-sky-api-using-the-visual-crossing-timeline-weather-api/
       * 
       * Si el grupo de unidades utilizado para solicitar datos metereologicos
       * es metric, precip es la cantidad de precipitacion en milimetros por
       * hora. Esto lo se por la documentacion de Visual Crossing Weather del
       * siguiente enlace:
       * 
       * https://www.visualcrossing.com/resources/documentation/weather-api/unit-groups-and-measurement-units/
       * 
       * Por lo tanto, para obtener la cantidad total de precipitacion en
       * milimetros para el dia y la ubicacion geografica para los cuales
       * se solicitan datos metereologicos, se debe multiplicar precip por 24.
       * 
       * Si precip representa la cantidad de nieve en milimetros por hora
       * (siempre y cuando preciptype sea snow), no es necesario realizar
       * ninguna conversion de centimetros a milimetros, ya que los
       * datos metereologico se solicitan con el grupo de unidades metric,
       * el cual, mide la precipitacion en milimetros.
       */
      givenClimateRecord.setTotalPrecipitation(givenDay.getPrecip() * 24);
    }

    /*
     * Si el tipo de precipitacion tiene el valor null en el conjunto
     * de datos metereologicos devuelto por una llamada a la API Visual
     * Crossing Weather (VSC), las variables de instancia precipPerHour,
     * precipProbability y totalPrecipitation de una instancia de
     * tipo ClimateRecord se inicializan en 0.0, debido a que son
     * variables de instancia.
     */
  }

}
