package weatherApiClasses;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder(
  {
    "datetimeEpoch",
    "tempmax",
    "tempmin",
    "dew",
    "humidity",
    "precip",
    "precipprob",
    "preciptype",
    "windspeed",
    "pressure",
    "cloudcover",
  }
)
public class Day {

  @JsonProperty("datetimeEpoch")
  private long datetimeEpoch;

  @JsonProperty("tempmax")
  private double tempmax;

  @JsonProperty("tempmin")
  private double tempmin;

  @JsonProperty("dew")
  private double dew;

  @JsonProperty("humidity")
  private double humidity;

  @JsonProperty("precip")
  private double precip;

  @JsonProperty("precipprob")
  private double precipprob;

  @JsonProperty("preciptype")
  private List<String> preciptype;

  @JsonProperty("windspeed")
  private double windspeed;

  @JsonProperty("pressure")
  private double pressure;

  @JsonProperty("cloudcover")
  private double cloudcover;

  public void setDatetimeEpoch(long datetimeEpoch) {
    this.datetimeEpoch = datetimeEpoch;
  }

  public long getDatetimeEpoch() {
    return datetimeEpoch;
  }

  public void setTempmax(double tempmax) {
    this.tempmax = tempmax;
  }

  public double getTempmax() {
    return tempmax;
  }

  public void setTempmin(double tempmin) {
    this.tempmin = tempmin;
  }

  public double getTempmin() {
    return tempmin;
  }

  public void setDew(double dew) {
    this.dew = dew;
  }

  public double getDew() {
    return dew;
  }

  public void setHumidity(double humidity) {
    this.humidity = humidity;
  }

  public double getHumidity() {
    return humidity;
  }

  public void setPrecip(double precip) {
    this.precip = precip;
  }

  public double getPrecip() {
    return precip;
  }

  public void setPrecipprob(double precipprob) {
    this.precipprob = precipprob;
  }

  public double getPrecipprob() {
    return precipprob;
  }

  public void setPreciptype(List<String> preciptype) {
    this.preciptype = preciptype;
  }

  public List<String> getPreciptype() {
    return preciptype;
  }

  public void setWindspeed(double windspeed) {
    this.windspeed = windspeed;
  }

  public double getWindspeed() {
    return windspeed;
  }

  public void setPressure(double pressure) {
    this.pressure = pressure;
  }

  public double getPressure() {
    return pressure;
  }

  public void setCloudcover(double cloudcover) {
    this.cloudcover = cloudcover;
  }

  public double getCloudcover() {
    return cloudcover;
  }

}
