package model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import javax.persistence.Id;
import java.util.Calendar;

@Entity
@Table(name = "SOIL_WATER_BALANCE", uniqueConstraints = { @UniqueConstraint(columnNames = { "DATE", "PARCEL_NAME" }) })
public class SoilWaterBalance {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "DATE", nullable = false)
    @Temporal(TemporalType.DATE)
    private Calendar date;

    @Column(name = "PARCEL_NAME", nullable = false)
    private String parcelName;

    @Column(name = "CROP_NAME", nullable = false)
    private String cropName;

    /*
     * El valor de esta variable es la precipitacion
     * natural por dia [mm/dia] o la precipitacion
     * artificial (agua de riego) por dia [mm/día] o
     * la suma de ambas [mm/dia]
     */
    @Column(name = "WATER_PROVIDED_PER_DAY", nullable = false)
    private double waterProvidedPerDay;

    /*
     * La perdida de humedad del suelo esta determinada por
     * la ETc (evapotranspiracion del cultivo bajo condiciones
     * estandar) [mm/dia]
     */
    @Column(name = "SOIL_MOISTURE_LOSS", nullable = false)
    private double soilMoistureLossPerDay;

    @Column(name = "WATER_DEFICIT_PER_DAY", nullable = false)
    private double waterDeficitPerDay;

    @Column(name = "ACCUMULATED_WATER_DEFICIT_PER_DAY", nullable = false)
    private String accumulatedWaterDeficitPerDay;

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

    public String getParcelName() {
        return parcelName;
    }

    public void setParcelName(String parcelName) {
        this.parcelName = parcelName;
    }

    public String getCropName() {
        return cropName;
    }

    public void setCropName(String cropName) {
        this.cropName = cropName;
    }

    public double getWaterProvidedPerDay() {
        return waterProvidedPerDay;
    }

    public void setWaterProvidedPerDay(double waterProvidedPerDay) {
        this.waterProvidedPerDay = waterProvidedPerDay;
    }

    public double getSoilMoistureLossPerDay() {
        return soilMoistureLossPerDay;
    }

    public void setSoilMoistureLossPerDay(double soilMoistureLossPerDay) {
        this.soilMoistureLossPerDay = soilMoistureLossPerDay;
    }

    public double getWaterDeficitPerDay() {
        return waterDeficitPerDay;
    }

    public void setWaterDeficitPerDay(double waterDeficitPerDay) {
        this.waterDeficitPerDay = waterDeficitPerDay;
    }

    public String getAccumulatedWaterDeficitPerDay() {
        return accumulatedWaterDeficitPerDay;
    }

    public void setAccumulatedWaterDeficitPerDay(String accumulatedWaterDeficitPerDay) {
        this.accumulatedWaterDeficitPerDay = accumulatedWaterDeficitPerDay;
    }

}