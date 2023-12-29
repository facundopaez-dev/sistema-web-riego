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

  @Column(name = "MOST_PLANTED_CROP", nullable = false)
  private String mostPlantedCrop;

  @Column(name = "LESS_PLANTED_CROP", nullable = false)
  private String lessPlantedCrop;

  @Column(name = "CROP_LONGEST_LIFE_CYCLE_PLANTED", nullable = false)
  private String cropLongestLifeCyclePlanted;

  @Column(name = "CROP_SHORTEST_LIFE_CYCLE_PLANTED", nullable = false)
  private String cropShortestLifeCyclePlanted;

  @Column(name = "DAYS_WITHOUT_CROPS", nullable = false)
  private String daysWithoutCrops;

  @Column(name = "TOTAL_AMOUNT_RAINWATER", nullable = false)
  private String totalAmountRainwater;

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

  public String getMostPlantedCrop() {
    return mostPlantedCrop;
  }

  public void setMostPlantedCrop(String mostPlantedCrop) {
    this.mostPlantedCrop = mostPlantedCrop;
  }

  public String getLessPlantedCrop() {
    return lessPlantedCrop;
  }

  public void setLesstPlantedCrop(String lessPlantedCrop) {
    this.lessPlantedCrop = lessPlantedCrop;
  }

  public String getCropLongestLifeCyclePlanted() {
    return cropLongestLifeCyclePlanted;
  }

  public void setCropLongestLifeCyclePlanted(String cropLongestLifeCyclePlanted) {
    this.cropLongestLifeCyclePlanted = cropLongestLifeCyclePlanted;
  }

  public String getCropShortestLifeCyclePlanted() {
    return cropShortestLifeCyclePlanted;
  }

  public void setCropShortestLifeCyclePlanted(String cropShortestLifeCyclePlanted) {
    this.cropShortestLifeCyclePlanted = cropShortestLifeCyclePlanted;
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
