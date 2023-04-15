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
   * Necesidad de agua de riego [mm/dia] de un cultivo.
   * 
   * La necesidad de agua de riego de un registro de riego puede
   * tener el valor "n/a" (no disponible) en los siguientes casos:
   * - cuando la parcela a la que pertenece NO tiene un registro
   * de plantacion en desarrollo. En este caso al no haber un
   * registro de plantacion en desarrollo no hay un cultivo en
   * desarrollo. Por lo tanto, no es posible calcular la
   * necesidad de agua de riego de un cultivo.
   * - cuando la parcela a la que pertenece tiene un registro
   * de plantacion en desarrollo, pero NO tiene el registro
   * climatico de la fecha actual. En este caso no se tiene la
   * evapotranspiracion del cultivo bajo condiciones estandar
   * (ETc) [mm/dia] ni la precipitacion [mm/dia] de dicha fecha,
   * por lo tanto, no es posible calcular la necesidad de agua
   * de riego de un cultivo.
   */
  @Column(name = "IRRIGATION_WATER_NEED", nullable = false)
  private String irrigationWaterNeed;

  /*
   * Riego realizado [mm/dia]
   * 
   * Esta variable es establecida por el usuario y representa
   * la cantidad de agua en milimetros por dia utilizada
   * para el riego de un cultivo.
   */
  @Column(name = "IRRIGATION_DONE", nullable = false)
  private double irrigationDone;

  /*
   * Esta variable representa si un registro de riego es generado
   * por el sistema o por el usuario.
   * 
   * Si un registro de riego tiene su atributo systemGenerated
   * en 1, significa que fue generado por el sistema. En caso
   * contrario, significa que fue generado por el usuario.
   */
  @Column(name = "SYSTEM_GENERATED", nullable = false)
  private boolean systemGenerated;

  /*
   * Un registro de riego del pasado (es decir, que su fecha es
   * estrictamente menor que la fecha actual) es un registro de
   * riego que NO se debe poder modificar.
   * 
   * Un registro de riego del pasado tiene su atributo modifiable
   * en false, mientras que un registro de riego actual (es decir,
   * que su fecha es igual a la fecha actual) lo tiene en true.
   * 
   * Esta variable es para mostrar u ocultar el boton de modificacion
   * de registro de riego en la interfaz grafica del usuario. Si
   * un registro de riego tiene su atributo modifiable en false,
   * se oculta el boton de modificacion. En cambio, si lo tiene en
   * true, se muestra el boton de modificacion.
   * 
   * La manera en la que esta variable adquiere el valor booleano
   * false es mediante el metodo unsetModifiable de la clase
   * IrrigationRecordManager. Este metodo establece el atributo
   * modifiable de un registro de riego del pasado en false, ya
   * que un registro de riego del pasado NO se debe poder modificar.
   */
  @Column(name = "MODIFIABLE", nullable = false)
  private boolean modifiable;

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
   * Returns value of irrigationWaterNeed
   * 
   * @return
   */
  public String getIrrigationWaterNeed() {
    return irrigationWaterNeed;
  }

  /**
   * Sets new value of irrigationWaterNeed
   * 
   * @param
   */
  public void setIrrigationWaterNeed(String irrigationWaterNeed) {
    this.irrigationWaterNeed = irrigationWaterNeed;
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
   * Returns value of systemGenerated
   * 
   * @return
   */
  public boolean getSystemGenerated() {
    return systemGenerated;
  }

  /**
   * Sets new value of systemGenerated
   * 
   * @param
   */
  public void setSystemGenerated(boolean systemGenerated) {
    this.systemGenerated = systemGenerated;
  }

  /**
   * Returns value of modifiable
   * 
   * @return
   */
  public boolean getModifiable() {
    return modifiable;
  }

  /**
   * Sets new value of modifiable
   * 
   * @param
   */
  public void setModifiable(boolean modifiable) {
    this.modifiable = modifiable;
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
        "ID: %d\nFecha: %s\nNecesidad de agua de riego: %s [mm/día]\nRiego realizado: %f [mm/día]\nGenerado por el sistema: %b\nModificable: %b\nID de parcela: %d\nCultivo: %s\n",
        id,
        UtilDate.formatDate(date),
        irrigationWaterNeed,
        irrigationDone,
        systemGenerated,
        modifiable,
        parcel.getId(),
        crop.getName());
  }

}
