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
import util.UtilDate;

@Entity
@Table(name = "IRRIGATION_RECORD")
public class IrrigationRecord {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "ID")
  private int id;

  @Column(name = "DATE", nullable = false)
  @Temporal(TemporalType.DATE)
  private Calendar date;

  /*
   * Riego realizado [mm/dia]
   * 
   * Esta variable es establecida por el usuario y representa
   * la cantidad de agua en milimetros por dia utilizada
   * para el riego de un cultivo.
   */
  @Column(name = "IRRIGATION_DONE", nullable = false)
  private double irrigationDone;

  @ManyToOne
  @JoinColumn(name = "FK_PARCEL", nullable = false)
  private Parcel parcel;

  @ManyToOne
  @JoinColumn(name = "FK_CROP")
  private Crop crop;

  public IrrigationRecord() {

  }

  /**
   * Returns value of id
   * 
   * @return
   */
  public int getId() {
    return id;
  }

  /**
   * Returns value of date
   * 
   * @return date
   */
  public Calendar getDate() {
    return date;
  }

  /**
   * Sets new value of date
   * 
   * @param date
   */
  public void setDate(Calendar date) {
    this.date = date;
  }

  /**
   * Returns value of irrigationDone
   * 
   * @return
   */
  public double getIrrigationDone() {
    return irrigationDone;
  }

  /**
   * Sets new value of irrigationDone
   * 
   * @param
   */
  public void setIrrigationDone(double irrigationDone) {
    this.irrigationDone = irrigationDone;
  }

  /**
   * Returns value of parcel
   * 
   * @return
   */
  public Parcel getParcel() {
    return parcel;
  }

  /**
   * Sets new value of parcel
   * 
   * @param
   */
  public void setParcel(Parcel parcel) {
    this.parcel = parcel;
  }

  /**
   * Returns value of crop
   * 
   * @return
   */
  public Crop getCrop() {
    return crop;
  }

  /**
   * Sets new value of crop
   * 
   * @param
   */
  public void setCrop(Crop crop) {
    this.crop = crop;
  }

  @Override
  public String toString() {
    return String.format(
        "ID: %d\nFecha: %s\nRiego realizado: %f [mm/día]\nID de parcela: %d\nCultivo: %s\n",
        id,
        UtilDate.formatDate(date),
        irrigationDone,
        parcel.getId(),
        crop.getName());
  }

}
