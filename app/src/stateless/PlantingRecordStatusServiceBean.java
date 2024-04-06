package stateless;

import java.util.Calendar;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import irrigation.WaterMath;
import model.PlantingRecord;
import model.PlantingRecordStatus;
import model.Crop;
import model.Option;
import model.Parcel;
import util.UtilDate;

@Stateless
public class PlantingRecordStatusServiceBean {

  @PersistenceContext(unitName = "swcar")
  protected EntityManager entityManager;

  public void setEntityManager(EntityManager localEntityManager) {
    entityManager = localEntityManager;
  }

  public EntityManager getEntityManager() {
    return entityManager;
  }

  /**
   * @param id
   * @return referencia a un objeto de tipo PlantingRecordStatus
   * correspondiente al ID dado
   */
  public PlantingRecordStatus find(int id) {
    return getEntityManager().find(PlantingRecordStatus.class, id);
  }

  /**
   * @return referencia a un objeto de tipo PlantingRecordStatus que
   * representa el estado "Finalizado"
   */
  public PlantingRecordStatus findFinishedStatus() {
    Query query = getEntityManager().createQuery("SELECT p FROM PlantingRecordStatus p WHERE UPPER(p.name) = UPPER('Finalizado')");
    return (PlantingRecordStatus) query.getSingleResult();
  }

  /**
   * @return referencia a un objeto de tipo PlantingRecordStatus que
   * representa el estado "En desarrollo"
   */
  public PlantingRecordStatus findInDevelopmentStatus() {
    Query query = getEntityManager().createQuery("SELECT p FROM PlantingRecordStatus p WHERE p.name = 'En desarrollo'");
    return (PlantingRecordStatus) query.getSingleResult();
  }

  /**
   * @return referencia a un objeto de tipo PlantingRecordStatus que
   * representa el estado "Desarrollo optimo"
   */
  public PlantingRecordStatus findOptimalDevelopmentStatus() {
    Query query = getEntityManager().createQuery("SELECT p FROM PlantingRecordStatus p WHERE UPPER(p.name) = UPPER('Desarrollo óptimo')");
    return (PlantingRecordStatus) query.getSingleResult();
  }

  /**
   * @return referencia a un objeto de tipo PlantingRecordStatus que
   * representa el estado "Desarrollo en riesgo de marchitez"
   */
  public PlantingRecordStatus findDevelopmentAtRiskWiltingStatus() {
    Query query = getEntityManager().createQuery("SELECT p FROM PlantingRecordStatus p WHERE UPPER(p.name) = UPPER('Desarrollo en riesgo de marchitez')");
    return (PlantingRecordStatus) query.getSingleResult();
  }

  /**
   * @return referencia a un objeto de tipo PlantingRecordStatus que
   * representa el estado "Desarrollo en marchitez"
   */
  public PlantingRecordStatus findDevelopmentInWiltingStatus() {
    Query query = getEntityManager().createQuery("SELECT p FROM PlantingRecordStatus p WHERE UPPER(p.name) = UPPER('Desarrollo en marchitez')");
    return (PlantingRecordStatus) query.getSingleResult();
  }

  /**
   * @return referencia a un objeto de tipo PlantingRecordStatus que
   * representa el estado "En espera"
   */
  public PlantingRecordStatus findWaitingStatus() {
    Query query = getEntityManager().createQuery("SELECT p FROM PlantingRecordStatus p WHERE UPPER(p.name) = UPPER('En espera')");
    return (PlantingRecordStatus) query.getSingleResult();
  }

  /**
   * @return referencia a un objeto de tipo PlantingRecordStatus que
   * representa el estado "Muerto"
   */
  public PlantingRecordStatus findDeadStatus() {
    Query query = getEntityManager().createQuery("SELECT p FROM PlantingRecordStatus p WHERE UPPER(p.name) = UPPER('Muerto')");
    return (PlantingRecordStatus) query.getSingleResult();
  }

  /**
   * Calcula el estado de un registro de plantacion en base a su
   * fecha de siembra y su fecha de cosecha. Un registro de plantacion
   * representa la siembra de un cultivo. Por lo tanto, este metodo
   * calcula el estado de un cultivo sembrado en una parcela en base
   * a su fecha de siembra y su fecha de cosecha.
   * 
   * @param plantingRecord
   * @return una referencia a un objeto de tipo PlantingRecordStatus
   * que representa el estado "Finalizado" si la fecha de cosecha de
   * un registro de plantacion es estrictamente menor a la fecha actual,
   * que representa el estado "En desarrollo" si la fecha actual esta
   * entre la fecha de siembra y la fecha de cosecha de un registro de
   * plantacion o que representa el estado "En espera" si la fecha de
   * siembra de un registro de plantacion es estrictamente mayor a la
   * fecha actual
   */
  public PlantingRecordStatus calculateStatus(PlantingRecord plantingRecord) {
    Calendar currentDate = UtilDate.getCurrentDate();
    Calendar seedDate = plantingRecord.getSeedDate();
    Calendar harvestDate = plantingRecord.getHarvestDate();
    Option parcelOption = plantingRecord.getParcel().getOption();

    /*
     * Si la fecha de cosecha de un registro de plantacion
     * es estrictamente menor a la fecha actual, el estado
     * de un registro de plantacion es "Finalizado"
     */
    if (UtilDate.compareTo(harvestDate, currentDate) < 0) {
      return findFinishedStatus();
    }

    /*
     * Si la bandera suelo NO esta activa y la fecha actual es
     * mayor o igual a la fecha de siembra y es menor o igual a
     * la fecha de cosecha de un registro de plantacion, el estado
     * de un registro de plantacion es "En desarrollo"
     */
    if (!parcelOption.getSoilFlag() && UtilDate.compareTo(currentDate, seedDate) >= 0 && UtilDate.compareTo(currentDate, harvestDate) <= 0) {
      return findInDevelopmentStatus();
    }

    /*
     * Si la bandera suelo esta activa y la fecha actual es mayor
     * o igual a la fecha de siembra y es menor o igual a la fecha
     * de cosecha de un registro de plantacion, el estado de un
     * registro de plantacion es "Desarrollo optimo"
     */
    if (parcelOption.getSoilFlag() && UtilDate.compareTo(currentDate, seedDate) >= 0 && UtilDate.compareTo(currentDate, harvestDate) <= 0) {
      return findOptimalDevelopmentStatus();
    }

    /*
     * Si la fecha de siembra de un registro de plantacion
     * es estrictamente mayor a la fecha actual, el estado
     * de un registro de plantacion es "En espera"
     */
    return findWaitingStatus();
  }

  /**
   * @param statusOne
   * @param statusTwo
   * @return true si el estado uno es igual al estado dos, en
   * caso contrario false
   */
  public boolean equals(PlantingRecordStatus statusOne, PlantingRecordStatus statusTwo) {
    return statusOne.getName().equals(statusTwo.getName());
  }

  /**
   * Calcula el estado de un registro de plantacion en desarrollo en
   * funcion del punto en el que se encuentra el nivel de humedad del
   * suelo en el que esta sembrado el cultivo de dicho registro. Esto
   * lo realiza en funcion de la cantidad total de agua de riego de
   * cultivo de la fecha actual (es decir, hoy) [mm/dia], del acumulado
   * del deficit de humedad por dia del dia inmediatamente anterior a la
   * fecha actual [mm/dia], de la capacidad de almacenamiento de agua
   * del suelo [mm] en el que esta sembrado un cultivo, del umbral de
   * riego [mm], del punto de marchitez permanente del suelo y del
   * negativo del doble de la capacidad de almacenamiento de agua [mm].
   * 
   * El estado de un registro de plantacion es el estado de un cultivo,
   * ya que un registro de plantacion representa la siembra de un
   * cultivo. Por lo tanto, el estado que retorna este metodo es el
   * estado de un cultivo.
   * 
   * @param totalAmountCropIrrigationWaterCurrentDate [mm/dia]
   * @param accumulatedSoilMoistureDeficitPerDayFromYesterday [mm/dia]
   * @param developingPlantingRecord
   * @return referencia a un objeto de tipo PlantingRecordStatus
   * que representa el estado de un registro de plantacion en
   * desarrollo calculado en funcion del nivel de humedad del
   * suelo en el que se encuentra sembrado el cultivo que contiene.
   * Este estado tambien representa el estado de un cultivo.
   */
  public PlantingRecordStatus calculateStatusRelatedToSoilMoistureLevel(double totalAmountCropIrrigationWaterCurrentDate,
      double accumulatedSoilMoistureDeficitPerDayFromYesterday, PlantingRecord developingPlantingRecord) {
    Parcel parcel = developingPlantingRecord.getParcel();
    Crop crop = developingPlantingRecord.getCrop();

    double soilMoistureLevel = 0.0;
    double permanentWiltingPoint = 0.0;
    double totalAmountWaterAvailable = 0.0;
    double optimalIrrigationLayer = 0.0;
    double fieldCapacity = 0.0;

    totalAmountWaterAvailable = WaterMath.calculateTotalAmountWaterAvailable(crop, parcel.getSoil());
    optimalIrrigationLayer = WaterMath.calculateOptimalIrrigationLayer(crop, parcel.getSoil());

    /*
     * La capacidad de campo se iguala a la capacidad de
     * almacenamiento de agua del suelo [mm] para realizar
     * las comparaciones del nivel de humedad del suelo con
     * respecto a la capacidad de almacenamiento de agua del
     * suelo [mm], la cual esta determinada por la lamina
     * total de agua disponible [mm] (dt)
     */
    fieldCapacity = totalAmountWaterAvailable;

    /*
     * El deficit de humedad de suelo por dia [mm/dia] puede ser
     * negativo, cero o positivo. Cuando es negativo representa
     * que en un dia hubo perdida de humedad en el suelo no
     * satisfecha (cubierta). En cambio, cuando es igual o mayor
     * a cero representa que la perdida de humedad que hubo en el
     * suelo en un dia esta totalmente satisfecha (cubierta). La
     * perdida de humedad de suelo por dia esta determinada por la
     * ETc (evapotranspiracion del cultivo bajo condiciones estandar)
     * [mm/dia].
     * 
     * El acumulado del deficit de humedad de suelo por dia [mm/dia]
     * es el resultado de sumar el deficit de humedad de suelo
     * por dia de cada uno de los dias de un conjunto de dias.
     * Por lo tanto, es la cantidad total de perdida de humedad
     * que hubo en el suelo dentro en un periodo de dias. El
     * acumulado del deficit de humedad de suelo por dia puede ser
     * negativo o cero. Cuando es negativo representa que en un
     * periodo de dias hubo una perdida de humedad en el suelo no
     * cubierta (satisfecha). En cambio, cuando es igual a cero
     * representa que la perdida de humedad que hubo en el suelo
     * en un periodo de dias esta totalmente cubierta (satisfecha).
     * Si el acumulado del deficit de humedad de suelo por dia es
     * negativo significa que el suelo NO esta en capacidad de
     * campo. Si el acumulado del deficit de humedad de suelo por
     * dia es igual a cero significa que el suelo esta en capacidad
     * de campo. Capacidad de campo es la condicion en la que el
     * suelo agricola esta lleno de agua o en su maxima capacidad
     * de almacenamiento de agua, pero no anegado.
     * 
     * Se suma la capacidad de almacenamiento de agua del suelo
     * (la capacidad de campo es igualada a este valor en la
     * linea de codigo fuente inmediatamente anterior) al resultado
     * de la suma entre el acumulado del deficit de humedad de
     * suelo por dia del dia inmediatamente anterior a la fecha
     * actual [mm/dia] y la cantidad total de agua de riego de
     * cultivo por dia de la fecha actual [mm/dia] porque lo que
     * se busca es determinar el punto en el que se encuentra el
     * nivel de humedad del suelo, que tiene un cultivo sembrado,
     * con respecto a la capacidad de campo del suelo para establecer
     * el estado de un registro de plantacion en desarrollo perteneciente
     * a una parcela que tiene la bandera suelo activa en sus
     * opciones.
     * 
     * La capacidad de campo del suelo es un valor estrictamente
     * mayor a cero, ya que es igualada a la capacidad de almacenamiento
     * de agua del suelo, la cual es estrictamente mayor a
     * cero. La cantidad total de agua de riego de cultivo por
     * dia es un valor mayor o igual a cero. En cambio, el acumulado
     * del deficit de humedad de suelo por dia es un valor negativo
     * o igual a cero. Por este motivo se realiza la suma entre
     * la capacidad de campo del suelo y el resultado de la suma
     * entre el acumulado del deficit de humedad de suelo por dia
     * del dia inmediatamente anterior a la fecha actual y la
     * cantidad total de agua de riego de cultivo por dia de la
     * fecha actual para determinar el punto en el que se encuentra
     * el nivel de humedad del suelo que tiene un cultivo sembrado.
     * Esta determinacion se realiza con el fin de calcular el
     * estado de un registro de plantacion que tiene un estado
     * de desarrollo relacionado al uso de datos de suelo (desarrollo
     * optimo, desarrollo en riesgo de marchitez, desarrollo en
     * marchitez). De esta manera, tambien se determina el estado
     * del cultivo perteneciente a un registro de plantacion que
     * utiliza un estado de desarrollo relacionado al uso de datos
     * de suelo, ya que un registro de plantacion representa la
     * siembra de un cultivo.
     */
    soilMoistureLevel = fieldCapacity + (accumulatedSoilMoistureDeficitPerDayFromYesterday + totalAmountCropIrrigationWaterCurrentDate);

    /*
     * Si el nivel de humedad del suelo [mm], que tiene un cultivo
     * sembrado, es menor o igual al punto de marchitez permanente
     * (0) [mm] y mayor o igual al negativo del doble de la capacidad
     * de almacenamiento del suelo [mm], el estado del registro de
     * plantacion correspondiente a dicho cultivo es "Desarrollo en
     * marchitez", y, por ende, este es el estado del cultivo.
     */
    if (soilMoistureLevel <= permanentWiltingPoint && soilMoistureLevel >= -(2 * totalAmountWaterAvailable)) {
      return findDevelopmentInWiltingStatus();
    }

    /*
     * Si el nivel de humedad del suelo [mm], que tiene un cultivo
     * sembrado, es menor o igual al umbral de riego [mm] y estrictamente
     * mayor al punto de marchitez permanente (0) [mm], el estado
     * del registro de plantacion correspondiente a dicho cultivo
     * es "Desarrollo en riesgo de marchitez", y, por ende, este es
     * el estado del cultivo.
     * 
     * El umbral de riego esta determinado por la lamina de riego
     * optima (drop) [mm].
     */
    if (soilMoistureLevel <= optimalIrrigationLayer && soilMoistureLevel > permanentWiltingPoint) {
      return findDevelopmentAtRiskWiltingStatus();
    }

    /*
     * Si no se ejecuta el bloque then de ninguna de las instrucciones
     * if anteriores significa que el nivel de humedad del suelo [mm],
     * que tiene un cultivo sembrado, es menor o igual a la capacidad
     * de campo [mm] (*) y estrictamente mayor al umbral de riego [mm]
     * (**), por lo tanto, el estado del registro de plantacion
     * correspondiente a dicho cultivo es "Desarrollo optimo", y, por
     * ende, este es el estado del cultivo.
     * 
     * (*) No hay que olvidar que la capacidad de campo es igualada
     * a la capacidad de almacenamiento de agua del suelo. Esto se hace
     * para determinar el nivel de humedad del suelo, que tiene un
     * cultivo sembrado, con respecto a la capacidad de almacenamiento
     * de agua del suelo. Esto se debe a que se parte desde la condicion
     * de suelo a capacidad de campo en la fecha de siembra de un cultivo
     * para que la aplicacion informe cada dia en funcion de la perdida
     * de humedad del suelo y de la capacidad de almacenamiento de agua
     * del suelo, la cantidad de agua de riego que se debe proveer al
     * suelo para llevarlo a la condicion de capacidad de campo. La
     * perdida de humedad del suelo esta determinada por la ETc
     * (evapotranspiracion del cultivo bajo condiciones estandar) [mm/dia].
     * La condicion de suelo en capacidad de campo significa que el
     * suelo esta lleno de agua o en su maxima capacidad de almacenamiento
     * de agua, pero no anegado. La maxima capacidad de almacenamiento
     * de agua del suelo, o simplemente capacidad de almacenamiento
     * de agua del suelo, esta determinada por la lamina total de agua
     * disponible (dt) [mm].
     * 
     * (**) El umbral de riego esta determinado por la lamina de riego
     * optima (drop) [mm].
     */
    return findOptimalDevelopmentStatus();
  }

}
