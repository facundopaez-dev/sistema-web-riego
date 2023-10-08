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
@Table(name = "CROP_WATER_ACTIVITY_LOG", uniqueConstraints = { @UniqueConstraint(columnNames = { "DATE", "PARCEL_NAME", "CROP_NAME", "USER_ID" }) })
public class CropWaterActivityLog {

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
     * El valor de esta variable es el agua de lluvia por
     * dia [mm/dia] o el agua de riego por dia [mm/dia] o
     * la suma de ambas [mm/dia]
     */
    @Column(name = "WATER_PROVIDED", nullable = false)
    private double waterProvided;

    /*
     * El agua evaporada [mm/dia] puede estar indicada por la
     * ETc (evapotranspiracion del cultivo bajo condiciones
     * estandar) o la ETo (evapotranspiracion del cultivo de
     * referencia) en caso de que la ETc = 0, lo cual ocurre
     * cuando no hay un cultivo sembrado en una parcela dada
     */
    @Column(name = "EVAPORATED_WATER", nullable = false)
    private double evaporatedWater;

    @Column(name = "WATER_DEFICIT", nullable = false)
    private double waterDeficit;

    @Column(name = "ACCUMULATED_WATER_DEFICIT", nullable = false)
    private double accumulatedWaterDeficit;

    @Column(name = "USER_ID", nullable = false)
    private int userId;

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

    public double getWaterProvided() {
        return waterProvided;
    }

    public void setWaterProvided(double waterProvided) {
        this.waterProvided = waterProvided;
    }

    public double getEvaporatedWater() {
        return evaporatedWater;
    }

    public void setEvaporatedWater(double evaporatedWater) {
        this.evaporatedWater = evaporatedWater;
    }

    public double getWaterDeficit() {
        return waterDeficit;
    }

    public void setWaterDeficit(double waterDeficit) {
        this.waterDeficit = waterDeficit;
    }

    public double getAccumulatedWaterDeficit() {
        return accumulatedWaterDeficit;
    }

    public void setAccumulatedWaterDeficit(double accumulatedWaterDeficit) {
        this.accumulatedWaterDeficit = accumulatedWaterDeficit;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

}