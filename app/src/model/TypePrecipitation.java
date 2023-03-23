package model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.ManyToOne;
import javax.persistence.JoinColumn;

@Entity
@Table(name = "TYPE_PRECIPITATION")
public class TypePrecipitation {

  @Id
  @Column(name = "ID")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  @Column(name = "NAME", nullable = false)
  private String name;

  @ManyToOne
  @JoinColumn(name = "FK_CLIMATE_RECORD")
  private ClimateRecord climateRecord;

  public TypePrecipitation() {

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

  public void setClimateRecord(ClimateRecord climateRecord) {
    this.climateRecord = climateRecord;
  }

  @Override
  public String toString() {
    return String.format(
        "ID: %d\nTipo de precipitacion: %s\nID de registro climatico: %d\n",
        id,
        name,
        climateRecord.getId());
  }

}
