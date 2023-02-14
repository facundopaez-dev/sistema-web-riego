package model;

import java.util.Calendar;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
public class Parcel {

  /*
   * Instance variables
   */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "ID")
  private int id;

  // TODO: Establecer clave compuesta con este atributo y el identificador del usuario
  @Column(name = "NAME", nullable = false)
  private String name;

  @Column(name = "HECTARE", nullable = false)
  private double hectare;

  @Column(name = "LATITUDE", nullable = false)
  private double latitude; // en grados decimales

  @Column(name = "LONGITUDE", nullable = false)
  private double longitude; // en grados decimales

  @Column(name = "ACTIVE", nullable = false)
  private boolean active;

  @ManyToOne
  @JoinColumn(name = "FK_USER", nullable = false)
  private User user;

  public Parcel() {

  }

  /**
   * Returns value of id
   * @return
   */
  public int getId() {
    return id;
  }

  /**
   * Returns value of name
   * @return name
   */
  public String getName() {
    return name;
  }

  /**
   * Sets new value of name
   * @param name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Returns value of hectare
   * @return
   */
  public double getHectare() {
    return hectare;
  }

  /**
   * Sets new value of hectare
   * @param
   */
  public void setHectare(double hectare) {
    this.hectare = hectare;
  }

  /**
   * Returns value of latitude
   * @return
   */
  public double getLatitude() {
    return latitude;
  }

  /**
   * Sets new value of latitude
   * @param
   */
  public void setLatitude(double latitude) {
    this.latitude = latitude;
  }

  /**
   * Returns value of longitude
   * @return
   */
  public double getLongitude() {
    return longitude;
  }

  /**
   * Sets new value of longitude
   * @param
   */
  public void setLongitude(double longitude) {
    this.longitude = longitude;
  }

  /**
   * Returns value of active
   * @return
   */
  public boolean getActive() {
    return active;
  }

  /**
   * Sets new value of active
   * @param
   */
  public void setActive(boolean active) {
    this.active = active;
  }

  /**
   * Returns value of user
   * @return
   */
  public User getUser() {
    return user;
  }

  /**
   * Sets new value of user
   * @param
   */
  public void setUser(User user) {
    this.user = user;
  }

  @Override
  public String toString() {
    return String.format(
      "ID: %d\nNombre: %s\nHectarea: %f\nLatitud: %f\nLongitud: %f\nActiva: %b\nUsuario ID: %d\n",
      id,
      hectare,
      latitude,
      longitude,
      active,
      user.getId()
    );
  }

}
