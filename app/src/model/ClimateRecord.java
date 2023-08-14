/*
 * ClimateRecord representa un registro que contiene los
 * datos meteorologicos obtenidos para a una fecha en
 * una ubicacion geografica.
 *
 * - Unidades de medida
 * Las unidades de medida de los datos meteorologicos
 * dependen de las unidades de medida en la que se
 * los pide en la llamada a la API del clima Visual
 * Crossing Weather.
 *
 * En nuestro caso, en la llamada a la API especificamos
 * que deseamos que los datos meteorologicos utilicen
 * el grupo de unidades metric, el cual, establece lo
 * siguiente:
 *
 * - Temperatura: Grados centigrados.
 * - Precipitacion: Milimetros.
 * - Viento y rafaga de viento: Kilometros por hora.
 * - Presion: Milibares (hectopascales).
 * 
 * Fuente: https://www.visualcrossing.com/resources/documentation/weather-api/unit-groups-and-measurement-units/
 * 
 * - Conversion de la presion atmosferica
 * La presion atmosferica se debe convertir de
 * hectopascales a kilopascales porque la formula
 * de la ETo (evapotranspiracion del cultivo de
 * referencia) utiliza la presion atmosferica en
 * kilopascales. Esto se hace en la clase Eto
 * del paquete et (abreviacion de evapotranspiracion).
 */

package model;

import java.util.Calendar;
import java.util.Collection;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.CascadeType;
import javax.persistence.UniqueConstraint;
import util.UtilDate;

@Entity
@Table(name = "CLIMATE_RECORD", uniqueConstraints = { @UniqueConstraint(columnNames = { "DATE", "FK_PARCEL" }) })
public class ClimateRecord {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "ID")
  private int id;

  /*
   * Fecha en la que solicitan los datos meteorologicos
   * para una ubicacion geografica dada por su latitud
   * y longitud
   */
  @Column(name = "DATE", nullable = false)
  @Temporal(TemporalType.DATE)
  private Calendar date;

  /*
   * Precipitacion del dia [milimetros/dia] (si se usa el
   * grupo de unidades metric para la obtencion de
   * los datos meteorologicos de la API Visual Crossing
   * Weather), segun la documentacion de Visual Crossing
   * Weather.
   * 
   * Este valor depende de la probabilidad (es decir,
   * suponiendo que ocurre alguna precipitacion).
   *
   * En la cadena de consulta para la llamada a la API
   * del clima en la clase ClimateClient, esta establecido
   * que el grupo de unidades en la cual deben ser devueltos
   * los datos meteorologicos es metric, el cual, especifica
   * que la precipitacion esta medida en milimetros, segun
   * la documentacion de Visual Crossing Weather en el
   * siguiente enlace:
   * 
   * https://www.visualcrossing.com/resources/documentation/weather-api/unit-
   * groups-and-measurement-units/
   */
  @Column(name = "PRECIP", nullable = false)
  private double precip;

  /*
   * Probabilidad de la precipitacion [0% a 100%]
   */
  @Column(name = "PRECIP_PROBABILITY", nullable = false)
  private double precipProbability;

  /*
   * Seguna la documentacion de Visual Crossing Weather, los
   * valores posibles del tipo de precipitacion devueltos por
   * esta API incluyen lluvia (rain), nieve (snow), lluvia helada
   * (freezing rain) y granizo (ice).
   * 
   * Esto se lo encuentra en el siguiente enlace:
   * 
   * https://www.visualcrossing.com/resources/documentation/weather-data/weather-
   * data-documentation/
   * 
   * Hay que tener en cuenta que una invocacion a Visual Crossing
   * Weather puede devolver un conjunto de datos meteorologicos en
   * el que preciptype tenga el valor null.
   * 
   * Segun la documentacion del enlace de este comentario, Visual
   * Crossing Weather utiliza el valor null dentro del conjunto de
   * datos meteorologicos para indicar la ausencia de datos, como
   * informacion meteorologica faltante o datos desconocidos. No se
   * utiliza el valor null para indicar un valor cero. Por ejemplo,
   * un valor de precipitacion desconocido se marcara como vacio o
   * null. Una cantidad cero de precipitacion se indicara con el
   * valor cero.
   */
  @OneToMany(mappedBy = "climateRecord", cascade = { CascadeType.PERSIST, CascadeType.REMOVE })
  private Collection<TypePrecipitation> precipTypes;

  /*
   * Punto de rocio [°C]
   */
  @Column(name = "DEW_POINT", nullable = false)
  private double dewPoint;

  /*
   * Presion atmosferica [milibares (hectopascales)] a
   * nivel del mar
   */
  @Column(name = "ATMOSPHERIC_PRESSURE", nullable = false)
  private double atmosphericPressure;

  /*
   * Velocidad del viento [kilometros/hora]
   * 
   * La formula de la ETo requiere que la velocidad
   * del viento este en metros por segundo, por lo
   * tanto, en el metodo setWindSpeed se la convierte
   * a dicha unidad de medida.
   */
  @Column(name = "WIND_SPEED", nullable = false)
  private double windSpeed;

  /*
   * Nubosidad [0% a 100%]
   */
  @Column(name = "CLOUD_COVER", nullable = false)
  private double cloudCover;

  /*
   * Temperatura minima [°C]
   */
  @Column(name = "MIN_TEMP", nullable = false)
  private double minimumTemperature;

  /*
   * Temperatura maxima [°C]
   */
  @Column(name = "MAX_TEMP", nullable = false)
  private double maximumTemperature;

  /*
   * Evapotranspiracion del cultivo de referencia (ETo)
   *
   * Este valor se calcula mediante el uso de los datos
   * meteorologicos en la formula de la ETo, y esta
   * medido en milimetros por dia [mm/dia].
   *
   * El cultivo de referencia es el pasto, segun la pagina
   * 6 del libro Evapotranspiracion del cultivo, estudio
   * FAO riego y drenaje 56.
   * 
   * Para ver la formula de la ETo dirijase a la pagina
   * numero 25 del libro mencionado.
   */
  @Column(name = "ETO", nullable = false)
  private double eto;

  /*
   * Evapotranspiracion del cultivo bajo condiciones estandar (ETc)
   *
   * Este valor se calcula utilizando el coeficiente de un cultivo
   * (kc) en particular en la siguiente multiplicacion ETc = kc * ETo.
   *
   * El valor de la ETc [mm/dia] nos indica la cantidad de agua que
   * se le tiene que reponer a un cultivo dado, mediante el riego.
   *
   * Para ver la formula de la ETc dirigase a la pagina numero 6
   * del libro Evapotranspiracion del cultivo, estudio FAO riego
   * y drenaje 56.
   */
  @Column(name = "ETC", nullable = false)
  private double etc;

  /*
   * Esta variable es para mostrar u ocultar el boton de modificacion
   * de registro climatico en la interfaz grafica del usuario. Si
   * un registro climatico tiene su atributo modifiable en false,
   * se oculta el boton de modificacion. En cambio, si lo tiene en
   * true, se muestra el boton de modificacion.
   * 
   * La manera en la que esta variable adquiere el valor booleano
   * false es por parte del usuario mediante la modificacion de un
   * registro climatico.
   */
  @Column(name = "MODIFIABLE", nullable = false)
  private boolean modifiable;

  @ManyToOne
  @JoinColumn(name = "FK_PARCEL", nullable = false)
  private Parcel parcel;

  public ClimateRecord() {

  }

  /**
   * Returns value of id
   * 
   * @return
   */
  public int getId() {
    return id;
  }

  /**
   * Returns value of date
   * 
   * @return
   */
  public Calendar getDate() {
    return date;
  }

  /**
   * Sets new value of date
   * 
   * @param
   */
  public void setDate(Calendar date) {
    this.date = date;
  }

  /**
   * Returns value of precip
   * 
   * @return
   */
  public double getPrecip() {
    return precip;
  }

  /**
   * Sets new value of precip
   * 
   * @param
   */
  public void setPrecip(double precip) {
    this.precip = precip;
  }

  /**
   * Returns value of precipProbability
   * 
   * @return
   */
  public double getPrecipProbability() {
    return precipProbability;
  }

  /**
   * Sets new value of precipProbability
   * 
   * @param
   */
  public void setPrecipProbability(double precipProbability) {
    this.precipProbability = precipProbability;
  }

  /**
   * Returns value of precipTypes
   * 
   * @return
   */
  public Collection<TypePrecipitation> getPrecipTypes() {
    return precipTypes;
  }

  /**
   * Sets new value of precipTypes
   * 
   * @param
   */
  public void setPrecipTypes(Collection<TypePrecipitation> precipTypes) {
    this.precipTypes = precipTypes;
  }

  /**
   * Returns value of dewPoint
   * 
   * @return
   */
  public double getDewPoint() {
    return dewPoint;
  }

  /**
   * Sets new value of dewPoint
   * 
   * @param
   */
  public void setDewPoint(double dewPoint) {
    this.dewPoint = dewPoint;
  }

  /**
   * Returns value of atmosphericPressure
   * 
   * @return
   */
  public double getAtmosphericPressure() {
    return atmosphericPressure;
  }

  /**
   * Sets new value of atmosphericPressure
   * 
   * @param
   */
  public void setAtmosphericPressure(double atmosphericPressure) {
    this.atmosphericPressure = atmosphericPressure;
  }

  /**
   * Returns value of windSpeed
   * 
   * @return
   */
  public double getWindSpeed() {
    return windSpeed;
  }

  /**
   * Sets new value of windSpeed
   * 
   * @param
   */
  public void setWindSpeed(double windSpeed) {
    this.windSpeed = windSpeed;
  }

  /**
   * Returns value of cloudCover
   * 
   * @return
   */
  public double getCloudCover() {
    return cloudCover;
  }

  /**
   * Sets new value of cloudCover
   * 
   * @param
   */
  public void setCloudCover(double cloudCover) {
    this.cloudCover = cloudCover;
  }

  /**
   * Returns value of minimumTemperature
   * 
   * @return
   */
  public double getMinimumTemperature() {
    return minimumTemperature;
  }

  /**
   * Sets new value of minimumTemperature
   * 
   * @param
   */
  public void setMinimumTemperature(double minimumTemperature) {
    this.minimumTemperature = minimumTemperature;
  }

  /**
   * Returns value of maximumTemperature
   * 
   * @return
   */
  public double getMaximumTemperature() {
    return maximumTemperature;
  }

  /**
   * Sets new value of maximumTemperature
   * 
   * @param
   */
  public void setMaximumTemperature(double maximumTemperature) {
    this.maximumTemperature = maximumTemperature;
  }

  /**
   * Returns value of eto
   * 
   * @return
   */
  public double getEto() {
    return eto;
  }

  /**
   * Sets new value of eto
   * 
   * @param
   */
  public void setEto(double eto) {
    this.eto = eto;
  }

  /**
   * Returns value of etc
   * 
   * @return
   */
  public double getEtc() {
    return etc;
  }

  /**
   * Sets new value of etc
   * 
   * @param
   */
  public void setEtc(double etc) {
    this.etc = etc;
  }

  /**
   * Returns value of modifiable
   * 
   * @return
   */
  public boolean getModifiable() {
    return modifiable;
  }

  /**
   * Sets new value of modifiable
   * 
   * @param
   */
  public void setModifiable(boolean modifiable) {
    this.modifiable = modifiable;
  }

  /**
   * Returns value of parcel
   * 
   * @return
   */
  public Parcel getParcel() {
    return parcel;
  }

  /**
   * Sets new value of parcel
   * 
   * @param
   */
  public void setParcel(Parcel parcel) {
    this.parcel = parcel;
  }

  @Override
  public String toString() {
    return String.format(
        "ID: %d\nLatitud: %f (grados decimales) Longitud: %f (grados decimales)\nFecha: %s\nPrecipitación del día: %f milímetros/día\nProbabilidad de precipitación: %f [porcentaje 0 - 100]\nPunto de rocío: %f °C\nPresión atmosférica: %f hectopascales (milibares)\nVelocidad del viento: %f kilómetros/por hora\nNubosidad: %f [porcentaje 0 - 100]\nTemperatura mínima: %f °C\nTemperatura máxima: %f °C\nTipos de precipitacion: %s\nModificable: %b\n",
        id,
        parcel.getLatitude(),
        parcel.getLongitude(),
        UtilDate.formatDate(date),
        precip,
        precipProbability,
        dewPoint,
        atmosphericPressure,
        windSpeed,
        cloudCover,
        minimumTemperature,
        maximumTemperature,
        precipTypes,
        modifiable);
  }

}
