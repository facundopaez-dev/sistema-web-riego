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
@Table(name = "CROP_WATER_ACTIVITY_LOG", uniqueConstraints = { @UniqueConstraint(columnNames = { "DATE", "PARCEL_NAME", "CROP_NAME" }) })
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
     * dia [mm/dia] y/o el agua de riego por dia [mm/dia]
     */
    @Column(name = "WATER", nullable = false)
    private double water;

    /*
     * El agua evaporada [mm/dia] puede estar indicada por la
     * ETc (evapotranspiracion del cultivo bajo condiciones
     * estandar) o la ETo (evapotranspiracion del cultivo de
     * referencia) en caso de que la ETc = 0, lo cual ocurre
     * cuando no hay un cultivo sembrado en una parcela dada
     */
    @Column(name = "EVAPORATED_WATER", nullable = false)
    private double evaporatedWater;

    @Column(name = "DEFICIT", nullable = false)
    private double deficit;

    @Column(name = "ACCUMULATED_DEFICIT", nullable = false)
    private double accumulatedDeficit;

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

    public double getWater() {
        return water;
    }

    public void setWater(double water) {
        this.water = water;
    }

    public double getEvaporatedWater() {
        return evaporatedWater;
    }

    public void setEvaporatedWater(double evaporatedWater) {
        this.evaporatedWater = evaporatedWater;
    }

    public double getDeficit() {
        return deficit;
    }

    public void setDeficit(double deficit) {
        this.deficit = deficit;
    }

    public double getAccumulatedDeficit() {
        return accumulatedDeficit;
    }

    public void setAccumulatedDeficit(double accumulatedDeficit) {
        this.accumulatedDeficit = accumulatedDeficit;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

}