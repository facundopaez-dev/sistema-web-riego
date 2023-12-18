package model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "SOIL")
public class Soil {

  @Id
  @Column(name = "ID")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  @Column(name = "NAME", unique = true, nullable = false)
  private String name;

  @Column(name = "APPARENT_SPECIFIC_WEIGHT", nullable = false)
  private double apparentSpecificWeight;

  @Column(name = "FIELD_CAPACITY", nullable = false)
  private double fieldCapacity;

  @Column(name = "PERMANENT_WILTING_POINT", nullable = false)
  private double permanentWiltingPoint;

  @Column(name = "ACTIVE", nullable = false)
  private boolean active;

  public Soil() {

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

  public double getApparentSpecificWeight() {
    return apparentSpecificWeight;
  }

  public void setApparentSpecificWeight(double apparentSpecificWeight) {
    this.apparentSpecificWeight = apparentSpecificWeight;
  }

  public double getFieldCapacity() {
    return fieldCapacity;
  }

  public void setFieldCapacity(double fieldCapacity) {
    this.fieldCapacity = fieldCapacity;
  }

  public double getPermanentWiltingPoint() {
    return permanentWiltingPoint;
  }

  public void setPermanentWiltingPoint(double permanentWiltingPoint) {
    this.permanentWiltingPoint = permanentWiltingPoint;
  }

  public boolean getActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  @Override
  public String toString() {
    return String.format(
        "ID: %d\nSuelo: %s\nPeso especifico aparente: %.2f\nCapacidad de campo: %.2f\nPunto de marchitez permanente: %.2f\nActivo: %b\n",
        id,
        name,
        apparentSpecificWeight,
        fieldCapacity,
        permanentWiltingPoint,
        active);
  }

}
