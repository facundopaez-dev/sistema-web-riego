/*
 * Esta clase permite extraer de la base de datos la radiacion solar
 * haciendo uso del numero del dia en el año del dia 15 de cada mes
 * y de una latitud en el hemisferio sur desde 0 a -70 grados decimales
 */

package model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "SOLAR_RADIATION")
public class SolarRadiation {

  /*
   * Instance variables
   */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "ID")
  private int id;

  @Column(name = "VALUE")
  private float solarRadiationValue;

  @ManyToOne
  @JoinColumn(name = "FK_LATITUDE")
  private Latitude decimalLatitude;

  @ManyToOne
  @JoinColumn(name = "FK_MONTH")
  private Month month;

  public SolarRadiation() {

  }

  public int getId() {
    return id;
  }

  public float getRadiation() {
    return solarRadiationValue;
  }

  public void setRadiation(float solarRadiationValue) {
    this.solarRadiationValue = solarRadiationValue;
  }

  public Latitude getLatitude() {
    return decimalLatitude;
  }

  public void setLatitude(Latitude decimalLatitude) {
    this.decimalLatitude = decimalLatitude;
  }

  public Month getMonth() {
    return month;
  }

  public void setMonth(Month month) {
    this.month = month;
  }

}
