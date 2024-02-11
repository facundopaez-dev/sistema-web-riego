package model;

import java.util.Calendar;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import util.UtilDate;

@Entity
@Table(name = "STATISTICAL_REPORT")
public class StatisticalReport {

  @Id
  @Column(name = "ID")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  @Column(name = "DATE_FROM", nullable = false)
  @Temporal(TemporalType.DATE)
  private Calendar dateFrom;

  @Column(name = "DATE_UNTIL", nullable = false)
  @Temporal(TemporalType.DATE)
  private Calendar dateUntil;

  @Column(name = "CROP_HIGHER_HARVEST", nullable = false)
  private String cropHigherHarvest;

  @Column(name = "HARVEST_AMOUNT_CROP_HIGHEST_HARVEST", nullable = false)
  private double harvestAmountCropHighestHarvest;

  @Column(name = "CROP_LOWER_HARVEST", nullable = false)
  private String cropLowerHarvest;

  @Column(name = "HARVEST_AMOUNT_CROP_LOWEST_HARVEST", nullable = false)
  private double harvestAmountCropLowestHarvest;

  @Column(name = "MOST_PLANTED_CROP", nullable = false)
  private String mostPlantedCrop;

  @Column(name = "QUANTITY_MOST_PLANTED_CROP")
  private int quantityMostPlantedCrop;

  @Column(name = "LESS_PLANTED_CROP", nullable = false)
  private String lessPlantedCrop;

  @Column(name = "QUANTITY_LESS_PLANTED_CROP")
  private int quantityLessPlantedCrop;

  @Column(name = "CROP_LONGEST_LIFE_CYCLE_PLANTED", nullable = false)
  private String cropLongestLifeCyclePlanted;

  @Column(name = "LIFE_CYCLE_CROP_LONGEST_LIFE_CYCLE_PLANTED")
  private int lifeCycleCropLongestLifeCyclePlanted;

  @Column(name = "CROP_SHORTEST_LIFE_CYCLE_PLANTED", nullable = false)
  private String cropShortestLifeCyclePlanted;

  @Column(name = "LIFE_CYCLE_CROP_SHORTEST_LIFE_CYCLE_PLANTED")
  private int lifeCycleCropShortestLifeCyclePlanted;

  @Column(name = "DAYS_WITHOUT_CROPS")
  private String daysWithoutCrops;

  @Column(name = "TOTAL_AMOUNT_RAINWATER")
  private String totalAmountRainwater;

  @Column(name = "TOTAL_AMOUNT_CROP_IRRIGATION_WATER")
  private double totalAmountCropIrrigationWater;

  @ManyToOne
  @JoinColumn(name = "FK_PARCEL", nullable = false)
  private Parcel parcel;

  public StatisticalReport() {

  }

  public int getId() {
    return id;
  }

  public Calendar getDateFrom() {
    return dateFrom;
  }

  public void setDateFrom(Calendar dateFrom) {
    this.dateFrom = dateFrom;
  }

  public Calendar getDateUntil() {
    return dateUntil;
  }

  public void setDateUntil(Calendar dateUntil) {
    this.dateUntil = dateUntil;
  }

  public String getCropHigherHarvest() {
    return cropHigherHarvest;
  }

  public void setCropHigherHarvest(String cropHigherHarvest) {
    this.cropHigherHarvest = cropHigherHarvest;
  }

  public double getHarvestAmountCropHighestHarvest() {
    return harvestAmountCropHighestHarvest;
  }

  public void setHarvestAmountCropHighestHarvest(double harvestAmountCropHighestHarvest) {
    this.harvestAmountCropHighestHarvest = harvestAmountCropHighestHarvest;
  }

  public String getCropLowerHarvest() {
    return cropLowerHarvest;
  }

  public void setCropLowerHarvest(String cropLowerHarvest) {
    this.cropLowerHarvest = cropLowerHarvest;
  }

  public double getHarvestAmountCropLowestHarvest() {
    return harvestAmountCropLowestHarvest;
  }

  public void setHarvestAmountCropLowestHarvest(double harvestAmountCropLowestHarvest) {
    this.harvestAmountCropLowestHarvest = harvestAmountCropLowestHarvest;
  }

  public String getMostPlantedCrop() {
    return mostPlantedCrop;
  }

  public void setMostPlantedCrop(String mostPlantedCrop) {
    this.mostPlantedCrop = mostPlantedCrop;
  }

  public int getQuantityMostPlantedCrop() {
    return quantityMostPlantedCrop;
  }

  public void setQuantityMostPlantedCrop(int quantityMostPlantedCrop) {
    this.quantityMostPlantedCrop = quantityMostPlantedCrop;
  }

  public String getLessPlantedCrop() {
    return lessPlantedCrop;
  }

  public void setLesstPlantedCrop(String lessPlantedCrop) {
    this.lessPlantedCrop = lessPlantedCrop;
  }

  public int getQuantityLessPlantedCrop() {
    return quantityLessPlantedCrop;
  }

  public void setQuantityLessPlantedCrop(int quantityLessPlantedCrop) {
    this.quantityLessPlantedCrop = quantityLessPlantedCrop;
  }

  public String getCropLongestLifeCyclePlanted() {
    return cropLongestLifeCyclePlanted;
  }

  public void setCropLongestLifeCyclePlanted(String cropLongestLifeCyclePlanted) {
    this.cropLongestLifeCyclePlanted = cropLongestLifeCyclePlanted;
  }

  public int getLifeCycleCropLongestLifeCyclePlanted() {
    return lifeCycleCropLongestLifeCyclePlanted;
  }

  public void setLifeCycleCropLongestLifeCyclePlanted(int lifeCycleCropLongestLifeCyclePlanted) {
    this.lifeCycleCropLongestLifeCyclePlanted = lifeCycleCropLongestLifeCyclePlanted;
  }

  public String getCropShortestLifeCyclePlanted() {
    return cropShortestLifeCyclePlanted;
  }

  public void setCropShortestLifeCyclePlanted(String cropShortestLifeCyclePlanted) {
    this.cropShortestLifeCyclePlanted = cropShortestLifeCyclePlanted;
  }

  public int getLifeCycleCropShortestLifeCyclePlanted() {
    return lifeCycleCropShortestLifeCyclePlanted;
  }

  public void setLifeCycleCropShortestLifeCyclePlanted(int lifeCycleCropShortestLifeCyclePlanted) {
    this.lifeCycleCropShortestLifeCyclePlanted = lifeCycleCropShortestLifeCyclePlanted;
  }

  public String getDaysWithoutCrops() {
    return daysWithoutCrops;
  }

  public void setDaysWithoutCrops(String daysWithoutCrops) {
    this.daysWithoutCrops = daysWithoutCrops;
  }

  public String getTotalAmountRainwater() {
    return totalAmountRainwater;
  }

  public void setTotalAmountRainwater(String totalAmountRainwater) {
    this.totalAmountRainwater = totalAmountRainwater;
  }

  public double getTotalAmountCropIrrigationWater() {
    return totalAmountCropIrrigationWater;
  }

  public void setTotalAmountCropIrrigationWater(double totalAmountCropIrrigationWater) {
    this.totalAmountCropIrrigationWater = totalAmountCropIrrigationWater;
  }

  public Parcel getParcel() {
    return parcel;
  }

  public void setParcel(Parcel parcel) {
    this.parcel = parcel;
  }

  @Override
  public String toString() {
    return String.format(
        "ID: %d\nFecha desde: %s\nFecha hasta: %s\nCultivo mas plantado: %s\nCultivo menos plantado: %s\nCultivo con el mayor ciclo de vida plantado: %s\n"
            + "Cultivo con el menor ciclo de vida plantado: %s\nCantidad de dias en los que la parcela no tuvo ningun cultivo plantado: %s\n"
            + "Cantidad total de agua de lluvia que cayo sobre la parcela en el periodo ["
            + UtilDate.formatDate(dateFrom) + ", " + UtilDate.formatDate(dateUntil)
            + "]: %s\nID de la parcela: %d\nNombre de la parcela: %s\n",
        id, UtilDate.formatDate(dateFrom), UtilDate.formatDate(dateUntil), mostPlantedCrop, lessPlantedCrop,
        cropLongestLifeCyclePlanted, cropShortestLifeCyclePlanted, daysWithoutCrops, totalAmountRainwater,
        parcel.getId(), parcel.getName());
  }

}
