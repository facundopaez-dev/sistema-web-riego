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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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
     * La perdida de humedad de suelo esta determinada por
     * la ETc (evapotranspiracion del cultivo bajo condiciones
     * estandar) [mm/dia]
     */
    @Column(name = "SOIL_MOISTURE_LOSS", nullable = false)
    private double soilMoistureLossPerDay;

    /*
     * El deficit de humedad de suelo por dia [mm/dia] es el
     * resultado de la diferencia entre el agua provista por
     * dia y la perdida de humedad de suelo por dia. Puede
     * ser negativo, positivo o igual a cero. Cuando es negativo
     * representa que en un dia la perdida de humedad del suelo
     * NO fue cubierta (satisfecha). Cuando es igual o mayor a
     * cero representa que la perdida de humedad del suelo en
     * un dia fue totalmente cubierta.
     */
    @Column(name = "SOIL_MOSITURE_DEFICIT_PER_DAY", nullable = false)
    private double soilMoistureDeficitPerDay;

    /*
     * El acumulado del deficit de humedadad de suelo por dia
     * [mm/dia] es el resultado de acumular el deficit de
     * humedad de suelo por dia. Puede ser negativo o cero.
     * Cuando es negativo representa que en un periodo de dias
     * hubo perdida de humedad en el suelo. En cambio, cuando
     * es igual a cero representa que la perdida de humedad que
     * hubo en el suelo en un periodo de dias esta totalmente
     * cubierta. Esto es que el suelo esta en capacidad de campo,
     * lo que significa que el suelo esta lleno de agua o en
     * su maxima capacidad de almacenamiento de agua, pero no
     * anegado.
     */
    @Column(name = "ACCUMULATED_SOIL_MOISTURE_DEFICIT_PER_DAY", nullable = false)
    private String accumulatedSoilMoistureDeficitPerDay;

    @ManyToOne
    @JoinColumn(name = "FK_PARCEL", nullable = false)
    private Parcel parcel;

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

    public double getSoilMoistureDeficitPerDay() {
        return soilMoistureDeficitPerDay;
    }

    public void setSoilMoistureDeficitPerDay(double soilMoistureDeficitPerDay) {
        this.soilMoistureDeficitPerDay = soilMoistureDeficitPerDay;
    }

    public String getAccumulatedSoilMoistureDeficitPerDay() {
        return accumulatedSoilMoistureDeficitPerDay;
    }

    public void setAccumulatedSoilMoistureDeficitPerDay(String accumulatedSoilMoistureDeficitPerDay) {
        this.accumulatedSoilMoistureDeficitPerDay = accumulatedSoilMoistureDeficitPerDay;
    }

    public Parcel getParcel() {
        return parcel;
    }

    public void setParcel(Parcel parcel) {
        this.parcel = parcel;
    }

}