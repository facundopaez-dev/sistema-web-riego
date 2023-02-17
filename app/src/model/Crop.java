package model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
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

  @Column(name = "ACTIVE", nullable = false)
  private boolean active;

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

  public boolean getActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  @Override
  public String toString() {
    return String.format(
      "Cultivo: %s\nID: %d\nDuración de la etapa inicial: %d días\nDuración de la etapa de desarrollo: %d\nDuración de la etapa media: %d días\nDuración de la etapa final: %d días\nKc inicial: %.2f\nKc medio: %.2f\nKc final: %.2f\n",
      name,
      id,
      initialStage,
      developmentStage,
      middleStage,
      finalStage,
      initialKc,
      middleKc,
      finalKc
    );
  }

}
