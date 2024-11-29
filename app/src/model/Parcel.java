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
import javax.persistence.CascadeType;
import javax.persistence.OneToOne;

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

  @Column(name = "ACTIVE", nullable = false)
  private boolean active;

  @Column(name = "MODIFIED_GEOGRAPHIC_LOCATION_FLAG")
  private boolean modifiedGeographicLocationFlag;

  @OneToOne(cascade = CascadeType.PERSIST)
  @JoinColumn(name = "FK_GEOGRAPHIC_LOCATION", nullable = false, unique = true)
  private GeographicLocation geographicLocation;

  @OneToOne(cascade = CascadeType.PERSIST)
  @JoinColumn(name = "FK_OPTION", nullable = false, unique = true)
  private Option option;

  @ManyToOne
  @JoinColumn(name = "FK_USER", nullable = false)
  private User user;

  @ManyToOne
  @JoinColumn(name = "FK_SOIL")
  private Soil soil;

  public Parcel() {

  }

  public int getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public double getHectares() {
    return hectares;
  }

  public void setHectares(double hectares) {
    this.hectares = hectares;
  }

  public boolean getActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public boolean getModifiedGeographicLocationFlag() {
    return modifiedGeographicLocationFlag;
  }

  public void setModifiedGeographicLocationFlag(boolean modifiedGeographicLocationFlag) {
    this.modifiedGeographicLocationFlag = modifiedGeographicLocationFlag;
  }

  public GeographicLocation getGeographicLocation() {
    return geographicLocation;
  }

  public void setGeographicLocation(GeographicLocation geographicLocation) {
    this.geographicLocation = geographicLocation;
  }

  public Option getOption() {
    return option;
  }

  public void setOption(Option option) {
    this.option = option;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public Soil getSoil() {
    return soil;
  }

  public void setSoil(Soil soil) {
    this.soil = soil;
  }

  @Override
  public String toString() {
    return String.format(
        "ID: %d\nNombre: %s\nHectáreas: %f\nLatitud: %f\nLongitud: %f\nActiva: %b\nID del usuario al que pertenece: %d\n",
        id,
        name,
        hectares,
        geographicLocation.getLatitude(),
        geographicLocation.getLongitude(),
        active,
        user.getId());
  }

}
