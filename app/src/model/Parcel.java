package model;

import java.util.Collection;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.OneToOne;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;

@Entity
@Table(name = "PARCEL", uniqueConstraints = { @UniqueConstraint(columnNames = { "NAME", "FK_USER" }) })
public class Parcel {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "ID")
  private int id;

  @Column(name = "NAME", nullable = false)
  private String name;

  @Column(name = "HECTARES", nullable = false)
  private double hectares;

  @Column(name = "LATITUDE", nullable = false)
  private double latitude; // en grados decimales

  @Column(name = "LONGITUDE", nullable = false)
  private double longitude; // en grados decimales

  @Column(name = "ACTIVE", nullable = false)
  private boolean active;

  @OneToOne
  @JoinColumn(name = "FK_OPTION", nullable = false, unique = true)
  private Option option;

  @ManyToOne
  @JoinColumn(name = "FK_SOIL")
  private Soil soil;

  @OneToMany
  @JoinTable(name = "PARCEL_SWB", joinColumns = @JoinColumn(name = "FK_PARCEL"), inverseJoinColumns = @JoinColumn(name = "FK_SOIL_WATER_BALANCE"))
  private Collection<SoilWaterBalance> soilWaterBalances;

  public Parcel() {

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
   * Returns value of name
   * 
   * @return name
   */
  public String getName() {
    return name;
  }

  /**
   * Sets new value of name
   * 
   * @param name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Returns value of hectares
   * 
   * @return
   */
  public double getHectares() {
    return hectares;
  }

  /**
   * Sets new value of hectares
   * 
   * @param
   */
  public void setHectares(double hectares) {
    this.hectares = hectares;
  }

  /**
   * Returns value of latitude
   * 
   * @return
   */
  public double getLatitude() {
    return latitude;
  }

  /**
   * Sets new value of latitude
   * 
   * @param
   */
  public void setLatitude(double latitude) {
    this.latitude = latitude;
  }

  /**
   * Returns value of longitude
   * 
   * @return
   */
  public double getLongitude() {
    return longitude;
  }

  /**
   * Sets new value of longitude
   * 
   * @param
   */
  public void setLongitude(double longitude) {
    this.longitude = longitude;
  }

  /**
   * Returns value of active
   * 
   * @return
   */
  public boolean getActive() {
    return active;
  }

  /**
   * Sets new value of active
   * 
   * @param
   */
  public void setActive(boolean active) {
    this.active = active;
  }

  public Option getOption() {
    return option;
  }

  public void setOption(Option option) {
    this.option = option;
  }

  public Soil getSoil() {
    return soil;
  }

  public void setSoil(Soil soil) {
    this.soil = soil;
  }

  public Collection<SoilWaterBalance> getSoilWaterBalances() {
    return soilWaterBalances;
  }

  public void setSoilWaterBalances(Collection<SoilWaterBalance> soilWaterBalances) {
    this.soilWaterBalances = soilWaterBalances;
  }

  @Override
  public String toString() {
    return String.format(
        "ID: %d\nNombre: %s\nHectáreas: %f\nLatitud: %f\nLongitud: %f\nActiva: %b\nID de usuario: %d\n",
        id,
        name,
        hectares,
        latitude,
        longitude,
        active);
  }

}
