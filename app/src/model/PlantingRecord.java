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
@Table(name = "PLANTING_RECORD")
public class PlantingRecord {

  @Id
  @Column(name = "ID")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  @Column(name = "SEED_DATE", nullable = false)
  @Temporal(TemporalType.DATE)
  private Calendar seedDate;

  @Column(name = "HARVEST_DATE", nullable = false)
  @Temporal(TemporalType.DATE)
  private Calendar harvestDate;

  /*
   * Un registro de plantacion finalizado es un registro de plantacion
   * que NO se debe poder modificar.
   * 
   * Un registro de plantacion finalizado tiene su atributo modifiable
   * en false, mientras que un registro de plantacion en desarrollo
   * lo tiene en true.
   * 
   * Esta variable es para mostrar u ocultar el boton de modificacion
   * de registro de plantacion en la interfaz grafica del usuario. Si
   * un registro de plantacion tiene su atributo modifiable en false,
   * se oculta el boton de modificacion. En cambio, si lo tiene en
   * true, se muestra el boton de modificacion.
   * 
   * La manera en la que esta variable adquiere el valor booleano
   * false es mediante el metodo unsetModifiable de la clase
   * PlantingRecordManager. Este metodo establece el atributo
   * modifiable de un registro de plantacion finalizado en false, ya
   * que un registro de plantacion finalizado NO se debe poder
   * modificar.
   */
  @Column(name = "MODIFIABLE", nullable = false)
  private boolean modifiable;

  /*
   * El valor de este atributo es asignado por el metodo automatico
   * setIrrigationWaterNeed de la clase PlantingRecordManager.
   * 
   * El motivo de este atributo y del metodo setIrrigationWaterNeed
   * es que el usuario sepa la necesidad de agua de riego [mm/dia]
   * de un cultivo en desarrollo sin tener que presionar un boton
   * para ello.
   */
  @Column(name = "IRRIGATION_WATER_NEED", nullable = false)
  private String irrigationWaterNeed;

  @ManyToOne
  @JoinColumn(name = "FK_PARCEL", nullable = false)
  private Parcel parcel;

  @ManyToOne
  @JoinColumn(name = "FK_CROP", nullable = false)
  private Crop crop;

  @ManyToOne
  @JoinColumn(name = "FK_STATUS", nullable = false)
  private PlantingRecordStatus status;

  public PlantingRecord() {

  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public Calendar getSeedDate() {
    return seedDate;
  }

  public void setSeedDate(Calendar seedDate) {
    this.seedDate = seedDate;
  }

  public Calendar getHarvestDate() {
    return harvestDate;
  }

  public void setHarvestDate(Calendar harvestDate) {
    this.harvestDate = harvestDate;
  }

  public boolean getModifiable() {
    return modifiable;
  }

  public void setModifiable(boolean modifiable) {
    this.modifiable = modifiable;
  }

  public String getIrrigationWaterNeed() {
    return irrigationWaterNeed;
  }

  public void setIrrigationWaterNeed(String irrigationWaterNeed) {
    this.irrigationWaterNeed = irrigationWaterNeed;
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

  public PlantingRecordStatus getStatus() {
    return status;
  }

  public void setStatus(PlantingRecordStatus status) {
    this.status = status;
  }

  @Override
  public String toString() {
    return String.format(
        "ID: %d\nFecha de siembra: %s\nFecha de cosecha: %s\nNecesidad de agua de riego [mm/dia]: %s\nParcela: %s (ID = %d)\nCultivo: %s\nEstado: %s\nModificable: %b\n",
        id,
        UtilDate.formatDate(seedDate),
        UtilDate.formatDate(harvestDate),
        irrigationWaterNeed,
        parcel.getName(),
        parcel.getId(),
        crop.getName(),
        status.getName(),
        modifiable);
  }

}
