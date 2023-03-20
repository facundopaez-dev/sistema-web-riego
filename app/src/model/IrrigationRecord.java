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
  @Temporal(TemporalType.TIMESTAMP)
  private Calendar date;

  /*
   * Riego sugerido [mm/dia]
   */
  @Column(name = "SUGGESTED_IRRIGATION", nullable = false)
  private double suggestedIrrigation;

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
   * Precipitacion del dia de mañana [mm/dia]
   * 
   * Esta variable es para informar al usuario la precipitacion
   * del dia de mañana. No se utiliza su valor para calcular la
   * cantidad de agua de riego para un cultivo, y es establecida
   * por la aplicacion cuando calcula dicha cantidad.
   */
  @Column(name = "TOMORROW_PRECIPITATION", nullable = false)
  private double tomorrowPrecipitation;

  /*
   * Esta variable representa si un registro de riego es generado
   * por el sistema o por el usuario.
   * 
   * Si un registro de riego tiene su atributo systemGenerated
   * en 1, significa que fue generado por el sistema. En caso
   * contrario, significa que fue generado por el usuario.
   * 
   * El riego sugerido y la precipitacion del dia de mañana
   * en un registro de riego generado por el usuario tienen
   * el valor 0, ya que estos valores son generados por la
   * aplicacion cuando crea un registro de riego.
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
   * Returns value of suggestedIrrigation
   * 
   * @return
   */
  public double getSuggestedIrrigation() {
    return suggestedIrrigation;
  }

  /**
   * Sets new value of suggestedIrrigation
   * 
   * @param
   */
  public void setSuggestedIrrigation(double suggestedIrrigation) {
    this.suggestedIrrigation = suggestedIrrigation;
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
   * Returns value of tomorrowPrecipitation
   * 
   * @return
   */
  public double getTomorrowPrecipitation() {
    return tomorrowPrecipitation;
  }

  /**
   * Sets new value of tomorrowPrecipitation
   * 
   * @param
   */
  public void setTomorrowPrecipitation(double tomorrowPrecipitation) {
    this.tomorrowPrecipitation = tomorrowPrecipitation;
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
        "ID: %d\nFecha de riego: %s\nRiego sugerido: %f [mm/día]\nRiego realizado: %f [mm/día]\nPrecipitación del día de mañana: %f [mm/día]\nGenerado por el sistema: %b\nModificable: %b\nID de parcela: %d\nCultivo: %s\n",
        id,
        UtilDate.formatDate(date),
        suggestedIrrigation,
        irrigationDone,
        tomorrowPrecipitation,
        systemGenerated,
        modifiable,
        parcel.getId(),
        crop.getName());
  }

}
