package model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class Crop {

  @Id
  @Column(name = "ID")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  @Column(name = "NAME", nullable = false, unique = true)
  private String name;

  @Column(name = "INITIAL_STAGE", nullable = false)
  private int initialStage;

  @Column(name = "DEVELOPMENT_STAGE", nullable = false)
  private int developmentStage;

  @Column(name = "MIDDLE_STAGE", nullable = false)
  private int middleStage;

  @Column(name = "FINAL_STAGE", nullable = false)
  private int finalStage;

  @Column(name = "INITIAL_KC", nullable = false)
  private double initialKc;

  @Column(name = "MIDDLE_KC", nullable = false)
  private double middleKc;

  @Column(name = "FINAL_KC", nullable = false)
  private double finalKc;

  @Column(name = "LIFE_CYCLE", nullable = false)
  private int lifeCycle;

  @Column(name = "ACTIVE", nullable = false)
  private boolean active;

  @ManyToOne
  @JoinColumn(name = "FK_TYPE_CROP", nullable = false)
  private TypeCrop typeCrop;

  @ManyToOne
  @JoinColumn(name = "FK_REGION")
  private Region region;

  public Crop() {

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

  public int getInitialStage() {
    return initialStage;
  }

  public void setInitialStage(int initialStage) {
    this.initialStage = initialStage;
  }

  public int getDevelopmentStage() {
    return developmentStage;
  }

  public void setDevelopmentStage(int developmentStage) {
    this.developmentStage = developmentStage;
  }

  public int getMiddleStage() {
    return middleStage;
  }

  public void setMiddleStage(int middleStage) {
    this.middleStage = middleStage;
  }

  public int getFinalStage() {
    return finalStage;
  }

  public void setFinalStage(int finalStage) {
    this.finalStage = finalStage;
  }

  public double getInitialKc() {
    return initialKc;
  }

  public void setInitialKc(double initialKc) {
    this.initialKc = initialKc;
  }

  public double getMiddleKc() {
    return middleKc;
  }

  public void setMiddleKc(double middleKc) {
    this.middleKc = middleKc;
  }

  public double getFinalKc() {
    return finalKc;
  }

  public void setFinalKc(double finalKc) {
    this.finalKc = finalKc;
  }

  public int getLifeCycle() {
    return lifeCycle;
  }

  public void setLifeCycle(int lifeCycle) {
    this.lifeCycle = lifeCycle;
  }

  public boolean getActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public TypeCrop getTypeCrop() {
    return typeCrop;
  }

  public void setTypeCrop(TypeCrop typeCrop) {
    this.typeCrop = typeCrop;
  }

  public Region getRegion() {
    return region;
  }

  public void setRegion(Region region) {
    this.region = region;
  }

  @Override
  public String toString() {
    return String.format(
        "ID: %d\nCultivo: %s\nTipo de cultivo: %s\nEtapa inicial (días): %d\nEtapa de desarrollo (días): %d\nEtapa media (días): %d\nEtapa final (días): %d\nKc inicial: %.2f\nKc medio: %.2f\nKc final: %.2f\nCiclo de vida (días): %d\nActivo: %b",
        id,
        name,
        typeCrop.getName(),
        initialStage,
        developmentStage,
        middleStage,
        finalStage,
        initialKc,
        middleKc,
        finalKc,
        lifeCycle,
        active);
  }

}
