package model;

import java.util.Calendar;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "HARVEST")
public class Harvest {

  @Id
  @Column(name = "ID")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  @Column(name = "DATE", nullable = false)
  @Temporal(TemporalType.DATE)
  private Calendar date;

  @Column(name = "HARVEST_AMOUNT", nullable = false)
  private double harvestAmount;

  @ManyToOne
  @JoinColumn(name = "FK_PARCEL", nullable = false)
  private Parcel parcel;

  @ManyToOne
  @JoinColumn(name = "FK_CROP", nullable = false)
  private Crop crop;

  public Harvest() {

  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public Calendar getDate() {
    return date;
  }

  public void setDate(Calendar date) {
    this.date = date;
  }

  public double getHarvestAmount() {
    return harvestAmount;
  }

  public void setHarvestAmount(double harvestAmount) {
    this.harvestAmount = harvestAmount;
  }

  public Parcel getParcel() {
    return parcel;
  }

  public void setParcel(Parcel parcel) {
    this.parcel = parcel;
  }

  public Crop getCrop() {
    return crop;
  }

  public void setCrop(Crop crop) {
    this.crop = crop;
  }

}
