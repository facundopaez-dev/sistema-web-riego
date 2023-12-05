package model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name = "CROP", uniqueConstraints = { @UniqueConstraint(columnNames = { "NAME", "FK_PLANTING_START_MONTH", "FK_END_PLANTING_MONTH", "FK_REGION" }) })
public class Crop {

  @Id
  @Column(name = "ID")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  @Column(name = "NAME", nullable = false)
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

  @Column(name = "LOWER_LIMIT_MAXIMUM_ROOT_DEPTH", nullable = false)
  private double lowerLimitMaximumRootDepth;

  @Column(name = "UPPER_LIMIT_MAXIMUM_ROOT_DEPTH", nullable = false)
  private double upperLimitMaximumRootDepth;

  @ManyToOne
  @JoinColumn(name = "FK_PLANTING_START_MONTH")
  private Month plantingStartMonth;

  @ManyToOne
  @JoinColumn(name = "FK_END_PLANTING_MONTH")
  private Month endPlantingMonth;

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

  public double getLowerLimitMaximumRootDepth() {
    return lowerLimitMaximumRootDepth;
  }

  public void setLowerLimitMaximumRootDepth(double lowerLimitMaximumRootDepth) {
    this.lowerLimitMaximumRootDepth = lowerLimitMaximumRootDepth;
  }

  public double getUpperLimitMaximumRootDepth() {
    return upperLimitMaximumRootDepth;
  }

  public void setUpperLimitMaximumRootDepth(double upperLimitMaximumRootDepth) {
    this.upperLimitMaximumRootDepth = upperLimitMaximumRootDepth;
  }

  public Month getPlantingStartMonth() {
    return plantingStartMonth;
  }

  public void setPlantingStartMonth(Month plantingStartMonth) {
    this.plantingStartMonth = plantingStartMonth;
  }

  public Month getEndPlantingMonth() {
    return endPlantingMonth;
  }

  public void setEndPlantingMonth(Month endPlantingMonth) {
    this.endPlantingMonth = endPlantingMonth;
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
