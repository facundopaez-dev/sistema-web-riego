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

  @Column(name = "DEATH_DATE")
  @Temporal(TemporalType.DATE)
  private Calendar deathDate;

  /*
   * Esta variable es para mostrar u ocultar el boton de modificacion
   * de registro de plantacion en la interfaz grafica del usuario. Si
   * un registro de plantacion tiene su atributo modifiable en false,
   * se oculta el boton de modificacion. En cambio, si lo tiene en
   * true, se muestra el boton de modificacion.
   * 
   * La manera en la que esta variable adquiere el valor booleano
   * false es por parte del usuario mediante la modificacion de un
   * registro de plantacion.
   */
  @Column(name = "MODIFIABLE", nullable = false)
  private boolean modifiable;

  /*
   * El valor de este atributo es asignado por el metodo automatico
   * setCropIrrigationWaterNeed de la clase PlantingRecordManager.
   * 
   * El motivo de este atributo y del metodo setCropIrrigationWaterNeed
   * es que el usuario sepa la necesidad de agua de riego [mm/dia]
   * de un cultivo en desarrollo sin tener que presionar un boton
   * para ello.
   */
  @Column(name = "CROP_IRRIGATION_WATER_NEED", nullable = false)
  private String cropIrrigationWaterNeed;

  /*
   * Lamina total de agua disponible (dt) [mm]. Esta es la
   * capacidad de almacenamiento de agua que tiene un suelo.
   */
  @Column(name = "TOTAL_AMOUNT_WATER_AVAILABLE")
  private double totalAmountWaterAvailable;

  /*
   * Lamina de riego optima (drop) [mm]. Esta es el umbral de
   * riego. En la clase WaterMath se explica lo que representa
   * esta lamina.
   */
  @Column(name = "OPTIMAL_IRRIGATION_LAYER")
  private double optimalIrrigationLayer;

  /*
   * Bandera utilizada para no generar el grafico de la evolucion
   * diaria del nivel de humedad del suelo de un registro de
   * plantacion que tiene el estado "Desarrollo optimo", "Desarrollo
   * en riesgo de marchitez", "Desarrollo en marchitez" o "Muerto"
   * y que se le modifica la fecha de siembra y/o el cultivo. Un
   * registro de plantacion utiliza uno de los estados mencionados
   * cuando la parcela a la que pertenece tiene la bandera suelo
   * activa en sus opciones.
   */
  @Column(name = "FLAG_NOT_GENERATE_SOIL_MOISTURE_LEVEL_GRAPH")
  private boolean flagNotGenerateSoilMoistureGraph;

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

  public Calendar getDeathDate() {
    return deathDate;
  }

  public void setDeathDate(Calendar deathDate) {
    this.deathDate = deathDate;
  }

  public boolean getModifiable() {
    return modifiable;
  }

  public void setModifiable(boolean modifiable) {
    this.modifiable = modifiable;
  }

  public String getCropIrrigationWaterNeed() {
    return cropIrrigationWaterNeed;
  }

  public void setCropIrrigationWaterNeed(String cropIrrigationWaterNeed) {
    this.cropIrrigationWaterNeed = cropIrrigationWaterNeed;
  }

  public double getTotalAmountWaterAvailable() {
    return totalAmountWaterAvailable;
  }

  public void setTotalAmountWaterAvailable(double totalAmountWaterAvailable) {
    this.totalAmountWaterAvailable = totalAmountWaterAvailable;
  }

  public double getOptimalIrrigationLayer() {
    return optimalIrrigationLayer;
  }

  public void setOptimalIrrigationLayer(double optimalIrrigationLayer) {
    this.optimalIrrigationLayer = optimalIrrigationLayer;
  }

  public boolean getFlagNotGenerateSoilMoistureGraph() {
    return flagNotGenerateSoilMoistureGraph;
  }

  public void setFlagNotGenerateSoilMoistureGraph(boolean flagNotGenerateSoilMoistureGraph) {
    this.flagNotGenerateSoilMoistureGraph = flagNotGenerateSoilMoistureGraph;
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
        "ID: %d\nFecha de siembra: %s\nFecha de cosecha: %s\nNecesidad de agua de riego de cultivo [mm/dia]: %s\nParcela: %s (ID = %d)\nCultivo: %s\nEstado: %s\nModificable: %b\n",
        id,
        UtilDate.formatDate(seedDate),
        UtilDate.formatDate(harvestDate),
        cropIrrigationWaterNeed,
        parcel.getName(),
        parcel.getId(),
        crop.getName(),
        status.getName(),
        modifiable);
  }

}
