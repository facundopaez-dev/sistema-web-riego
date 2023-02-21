package weatherApiClasses;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder(
  {
    "queryCost",
    "latitude",
    "longitude",
    "resolvedAddress",
    "address",
    "timezone",
    "tzoffset",
    "days",
    "stations",
  }
)
public class Forecast {

  @JsonProperty("queryCost")
  private int queryCost;

  @JsonProperty("latitude")
  private double latitude;

  @JsonProperty("longitude")
  private double longitude;

  @JsonProperty("resolvedAddress")
  private String resolvedAddress;

  @JsonProperty("address")
  private String address;

  @JsonProperty("timezone")
  private String timezone;

  @JsonProperty("tzoffset")
  private int tzoffset;

  @JsonProperty("days")
  private List<Day> days;

  public void setQueryCost(int queryCost) {
    this.queryCost = queryCost;
  }

  public int getQueryCost() {
    return queryCost;
  }

  public void setLatitude(double latitude) {
    this.latitude = latitude;
  }

  public double getLatitude() {
    return latitude;
  }

  public void setLongitude(double longitude) {
    this.longitude = longitude;
  }

  public double getLongitude() {
    return longitude;
  }

  public void setResolvedAddress(String resolvedAddress) {
    this.resolvedAddress = resolvedAddress;
  }

  public String getResolvedAddress() {
    return resolvedAddress;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public String getAddress() {
    return address;
  }

  public void setTimezone(String timezone) {
    this.timezone = timezone;
  }

  public String getTimezone() {
    return timezone;
  }

  public void setTzoffset(int tzoffset) {
    this.tzoffset = tzoffset;
  }

  public int getTzoffset() {
    return tzoffset;
  }

  public void setDays(List<Day> days) {
    this.days = days;
  }

  public List<Day> getDays() {
    return days;
  }

  /**
   * Retorna el conjunto de datos metereologicos solicitados
   * para un dia (una fecha) en una ubicacion geografica
   * 
   * @return referencia a un objeto de tipo Day que contiene
   * los datos metereologicos solicitados para una ubcacion
   * geografica en un dia dado (una fecha dada)
   */
  public Day getDay() {
    return days.get(0);
  }

}
