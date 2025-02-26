package stateless;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Collection;
import java.util.Map;
import java.util.Calendar;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import model.Crop;
import model.Parcel;
import model.PlantingRecord;
import model.PlantingRecordStatus;
import util.UtilDate;

@Stateless
public class PlantingRecordServiceBean {

  @PersistenceContext(unitName = "swcar")
  protected EntityManager entityManager;

  private final String NON_EXISTENT_CROP = "Cultivo inexistente";

  /*
   * El valor de esta constante se asigna a la necesidad de
   * agua de riego [mm/dia] de un registro de plantacion
   * para el que no se puede calcular dicha necesidad, lo
   * cual, ocurre cuando no se tiene la evapotranspiracion
   * del cultivo bajo condiciones estandar (ETc) [mm/dia]
   * ni la precipitacion [mm/dia], siendo ambos valores de
   * la fecha actual.
   * 
   * El valor de esta constante tambien se asigna a la
   * necesidad de agua de riego de un registro de plantacion
   * finalizado o en espera, ya que NO tiene ninguna utilidad
   * que un registro de plantacion en uno de estos estados
   * tenga un valor numerico mayor o igual a cero en la
   * necesidad de agua de riego.
   * 
   * La abreviatura "n/a" significa "no disponible".
   */
  private final String NOT_AVAILABLE = "n/a";

  /*
   * Este simbolo se utiliza para representar que la necesidad
   * de agua de riego de un cultivo en la fecha actual (es decir,
   * hoy) [mm/dia] NO esta disponible, pero se puede calcular. Esta
   * situacion ocurre unicamente para un registro de plantacion que
   * tiene el estado "En desarrollo" o el estado "Desarrollo optimo".
   * El que un registro de plantacion tenga el estado "En desarrollo"
   * o el estado "Desarrollo optimo" depende de la fecha de siembra,
   * la fecha de cosecha y la bandera suelo de las opciones de la
   * parcela a la que pertenece. Si la fecha de siembra y la fecha
   * de cosecha se eligen de tal manera que la fecha actual (es decir,
   * hoy) esta dentro del periodo definido por ambas y la bandera
   * suelo esta activa, el registro adquiere el estado "En desarrollo".
   * En caso contrario, adquiere el estado "Desarrollo optimo".
   */
  private final String CROP_IRRIGATION_WATER_NEED_NOT_AVAILABLE_BUT_CALCULABLE = "-";

  public String getNotAvailable() {
    return NOT_AVAILABLE;
  }

  public String getNonExistentCrop() {
    return NON_EXISTENT_CROP;
  }

  public String getCropIrrigationWaterNotAvailableButCalculable() {
    return CROP_IRRIGATION_WATER_NEED_NOT_AVAILABLE_BUT_CALCULABLE;
  }

  public void setEntityManager(EntityManager localEntityManager) {
    entityManager = localEntityManager;
  }

  public EntityManager getEntityManager() {
    return entityManager;
  }

  /**
   * Persiste un registro de plantacion en la base de datos subyacente
   * 
   * @param newPlantingRecord
   * @return referencia a un objeto de tipo PlantingRecord persistido
   * en la base de datos subyacente
   */
  public PlantingRecord create(PlantingRecord newPlantingRecord) {
    getEntityManager().persist(newPlantingRecord);
    return newPlantingRecord;
  }

  public PlantingRecord remove(int plantingRecordId) {
    PlantingRecord givenPlantingRecord = find(plantingRecordId);

    if (givenPlantingRecord != null) {
      getEntityManager().remove(givenPlantingRecord);
      return givenPlantingRecord;
    }

    return null;
  }

  /**
   * Elimina fisicamente un registro de plantacion perteneciente a
   * una parcela de un usuario
   * 
   * @param userId
   * @param plantingRecordId
   * @return referencia a un objeto de tipo PlantingRecord en
   * caso de eliminarse de la base de datos subyacente el registro
   * de plantacion que tiene el ID dado y que esta asociado a una
   * parcela de un usuario que tiene el ID de usuario dado, en
   * caso contrario null
   */
  public PlantingRecord remove(int userId, int plantingRecordId) {
    PlantingRecord givenPlantingRecord = findByUserId(userId, plantingRecordId);

    if (givenPlantingRecord != null) {
      getEntityManager().remove(givenPlantingRecord);
      return givenPlantingRecord;
    }

    return null;
  }

  /**
   * Elimina los registros de plantacion asociados a las parcelas
   * de un usuario
   * 
   * @param userId
   */
  public void deletePlantingRecordsByUserId(int userId) {
    Query query = entityManager.createQuery("DELETE FROM PlantingRecord r WHERE r.parcel IN (SELECT x FROM Parcel x WHERE x.user.id = :userId)");
    query.setParameter("userId", userId);
    query.executeUpdate();
  }

  /**
   * Modifica un registro de plantacion perteneciente a una parcela
   * de un usuario
   * 
   * @param userId
   * @param plantingRecordId
   * @param modifiedPlantingRecord
   * @return referencia a un objeto de tipo PlantingRecord si el
   * registro de plantacion a modificar pertenece a una parcela del
   * usuario con el ID dado, en caso contrario null
   */
  public PlantingRecord modify(int userId, int plantingRecordId, PlantingRecord modifiedPlantingRecord) {
    PlantingRecord chosenPlantingRecord = findByUserId(userId, plantingRecordId);

    if (chosenPlantingRecord != null) {
      chosenPlantingRecord.setSeedDate(modifiedPlantingRecord.getSeedDate());
      chosenPlantingRecord.setHarvestDate(modifiedPlantingRecord.getHarvestDate());
      chosenPlantingRecord.setCropIrrigationWaterNeed(modifiedPlantingRecord.getCropIrrigationWaterNeed());
      chosenPlantingRecord.setParcel(modifiedPlantingRecord.getParcel());
      chosenPlantingRecord.setCrop(modifiedPlantingRecord.getCrop());
      chosenPlantingRecord.setModifiable(modifiedPlantingRecord.getModifiable());
      chosenPlantingRecord.setStatus(modifiedPlantingRecord.getStatus());
      return chosenPlantingRecord;
    }

    return null;
  }

  public PlantingRecord find(int id){
    return getEntityManager().find(PlantingRecord.class, id);
  }

  /**
   * Retorna el registro de plantacion de una parcela
   * 
   * @param plantingRecordId
   * @param givenParcel
   * @return referencia a un objeto de tipo PlantingRecord que
   * representa el registro de plantacion de una parcela en
   * caso de encontrarse en la base de datos subyacente el
   * registro de plantacion correspondiente al ID y la parcela
   * dados, en caso contrario null
   */
  public PlantingRecord find(int plantingRecordId, Parcel givenParcel) {
    Query query = getEntityManager().createQuery("SELECT r FROM PlantingRecord r WHERE (r.id = :plantingRecordId AND r.parcel = :givenParcel)");
    query.setParameter("plantingRecordId", plantingRecordId);
    query.setParameter("givenParcel", givenParcel);

    PlantingRecord plantingRecord = null;

    try {
      plantingRecord = (PlantingRecord) query.getSingleResult();
    } catch(NoResultException e) {
      e.printStackTrace();
    }

    return plantingRecord;
  }

  /**
   * @param givenParcel
   * @param givenDate
   * @return referencia a un objeto de tipo PlantingRecord que
   * representa que un cultivo estuvo sembrado en una parcela
   * dada en una fecha dada
   */
  public PlantingRecord find(Parcel givenParcel, Calendar givenDate) {
    Query query = getEntityManager().createQuery("SELECT r FROM PlantingRecord r WHERE (r.parcel = :givenParcel AND r.seedDate <= :givenDate AND :givenDate <= r.harvestDate)");
    query.setParameter("givenParcel", givenParcel);
    query.setParameter("givenDate", givenDate);

    PlantingRecord plantingRecord = null;

    try {
      plantingRecord = (PlantingRecord) query.getSingleResult();
    } catch(NoResultException e) {
      e.printStackTrace();
    }

    return plantingRecord;
  }

  /**
   * Un registro de plantacion con estado "En espera" cuya fecha
   * de siembra y fecha de cosecha definen un periodo que incluye
   * la fecha actual, debe cambiar su estado a "En desarrollo" o
   * "Desarrollo optimo". Esto se debe a que un registro adquiere
   * inicialmente uno de estos estados cuando la fecha actual se
   * encuentra entre la fecha de siembra y la fecha de cosecha.
   * El objetivo de este metodo es identificar dicho registro
   * para actualizar su estado a uno de los estados de desarrollo
   * mencionados.
   * 
   * @param userId
   * @param parcelId
   * @return referencia a un objeto de tipo PlantingRecord que
   * representa un registro de plantacion con el estado "En
   * espera", cuya fecha de siembra y fecha de cosecha incluyen
   * la fecha actual. Si se cumple esta condicion, se retorna el
   * registro; en caso contrario, se retorna null.
   */
  public PlantingRecord findPlantingRecordInWaitingForDevelopment(int userId, int parcelId) {
    Query query = getEntityManager().createQuery("SELECT r FROM PlantingRecord r JOIN r.parcel p WHERE p.user.id = :userId AND p.id = :parcelId AND UPPER(r.status.name) = UPPER('En espera') AND r.seedDate <= CURRENT_DATE AND CURRENT_DATE <= r.harvestDate ORDER BY r.seedDate");
    query.setParameter("userId", userId);
    query.setParameter("parcelId", parcelId);

    PlantingRecord plantingRecord = null;

    try {
      plantingRecord = (PlantingRecord) query.getSingleResult();
    } catch(NoResultException e) {
      e.printStackTrace();
    }

    return plantingRecord;

  }

  /**
   * @param userId
   * @param parcelId
   * @return true si existe un registro de plantacion con el
   * estado "En espera" de una parcela de un usuario, cuya
   * fecha de siembra y fecha de cosecha incluyen la fecha
   * actual; en caso contrario, false.
   */
  public boolean checkWaitingPlantingRecordForDevelopment(int userId, int parcelId) {
    return findPlantingRecordInWaitingForDevelopment(userId, parcelId) != null;
  }

  /**
   * Retorna true si y solo si una parcela tiene un registro
   * de plantacion en el que la fecha dada este entre la fecha
   * de siembra y la fecha de cosecha del mismo.
   * 
   * @param givenParcel
   * @param givenDate
   * @return true si una parcela tiene un registro de plantacion
   * en el que la fecha dada este entre la fecha de siembra y la
   * fecha de cosecha del mismo, en caso contrario false
   */
  public boolean checkExistence(Parcel givenParcel, Calendar givenDate) {
    return (find(givenParcel, givenDate) != null);
  }

  /**
   * Retorna un registro de plantacion perteneciente a una de las
   * parcelas de un usuario
   * 
   * @param userId
   * @param plantingRecordId
   * @return referencia a un objeto de tipo PlantingRecord que
   * representa el registro de plantacion de una parcela de un
   * usuario en caso de encontrarse en la base de datos subyacente
   * el registro de plantacion con el ID dado y asociado al usuario
   * del ID dado, en caso contrario null
   */
  public PlantingRecord findByUserId(int userId, int plantingRecordId) {
    Query query = getEntityManager().createQuery("SELECT r FROM PlantingRecord r JOIN r.parcel p WHERE r.id = :plantingRecordId AND p.user.id = :userId");
    query.setParameter("plantingRecordId", plantingRecordId);
    query.setParameter("userId", userId);

    PlantingRecord plantingRecord = null;

    try {
      plantingRecord = (PlantingRecord) query.getSingleResult();
    } catch(NoResultException e) {
      e.printStackTrace();
    }

    return plantingRecord;
  }

  /**
   * @param userId
   * @param harvestDate
   * @return referencia a un objeto de tipo PlantingRecord que
   * representa el registro de plantacion que tiene una fecha
   * de cosecha
   */
  public PlantingRecord findOneByHarvestDate(int userId, Calendar harvestDate) {
    Query query = getEntityManager().createQuery("SELECT r FROM PlantingRecord r JOIN r.parcel p WHERE r.harvestDate = :harvestDate AND p.user.id = :userId");
    query.setParameter("userId", userId);
    query.setParameter("harvestDate", harvestDate);

    PlantingRecord plantingRecord = null;

    try {
      plantingRecord = (PlantingRecord) query.getSingleResult();
    } catch(NoResultException e) {
      e.printStackTrace();
    }

    return plantingRecord;
  }

  /**
   * Retorna los registros de plantacion de las parcelas
   * de un usuario
   * 
   * @param userId
   * @return referencia a un objeto de tipo Collection que
   * contiene los registros de plantacion de las parcelas
   * del usuario con el ID dado
   */
  public Collection<PlantingRecord> findAll(int userId) {
    Query query = getEntityManager().createQuery("SELECT r FROM PlantingRecord r JOIN r.parcel p WHERE (p.user.id = :userId) ORDER BY r.id");
    query.setParameter("userId", userId);

    return (Collection) query.getResultList();
  }

  /**
   * Retorna los registros de plantacion de una parcela
   * 
   * @param givenParcel
   * @return referencia a un objeto de tipo Collection que
   * contiene los registros de plantacion de un parcela
   */
  public Collection<PlantingRecord> findAll(Parcel givenParcel) {
    Query query = getEntityManager().createQuery("SELECT r FROM PlantingRecord r JOIN r.parcel p WHERE (p = :givenParcel) ORDER BY r.id");
    query.setParameter("givenParcel", givenParcel);

    return (Collection) query.getResultList();
  }

  /**
   * Retorna los registros de plantacion de una parcela
   * excepto aquel que tiene el ID dado.
   * 
   * Este metodo es necesario para comprobar si las fechas
   * de un registro de plantacion modificado de una parcela
   * estan superpuestas con las fechas de los demas registros
   * de plantacion de la misma parcela. Para realizar correctamente
   * esta comprobacion se debe excluir el registro de plantacion
   * sobre el que se busca determinar si sus fechas se superponen
   * con las fechas de los demas registros de plantacion de
   * la misma parcela.
   * 
   * @param givenParcel
   * @param plantingRecordId
   * @return referencia a un objeto de tipo Collection que
   * contiene los registros de plantacion de un parcela
   * excepto aquel que tiene el ID dado
   */
  public Collection<PlantingRecord> findAllExceptOne(Parcel givenParcel, int plantingRecordId) {
    Query query = getEntityManager().createQuery("SELECT r FROM PlantingRecord r JOIN r.parcel p WHERE (r.id != :givenPlantingRecordId AND p = :givenParcel) ORDER BY r.id");
    query.setParameter("givenParcel", givenParcel);
    query.setParameter("givenPlantingRecordId", plantingRecordId);

    return (Collection) query.getResultList();
  }

  /**
   * Retorna los registros de plantacion de una parcela mediante
   * el nombre de una parcela y el ID del usuario al que pertenece
   * una parcela
   * 
   * @param userId
   * @param parcelName
   * @return referencia a un objeto de tipo Collection que
   * contiene los registros de plantacion de la parcela que
   * tiene el nombre dado y que pertenece al usuario con el
   * ID dado
   */
  public Collection<PlantingRecord> findAllByParcelName(int userId, String parcelName) {
    Query query = getEntityManager().createQuery("SELECT r FROM PlantingRecord r JOIN r.parcel p WHERE (p.name = :parcelName AND p.user.id = :userId) ORDER BY r.seedDate");
    query.setParameter("userId", userId);
    query.setParameter("parcelName", parcelName);

    return (Collection) query.getResultList();
  }

  /**
   * @param userId
   * @param parcelId
   * @param dateFrom
   * @return referencia a un objeto de tipo Collection que
   * contiene todos los registros de plantacion de una
   * parcela que tienen una fecha de siembra mayor o igual
   * a la fecha desde elegida
   */
  public Collection<PlantingRecord> findAllByDateGreaterThanOrEqual(int userId, int parcelId, Calendar dateFrom) {
    Query query = getEntityManager().createQuery("SELECT r FROM PlantingRecord r JOIN r.parcel p WHERE (r.seedDate >= :dateFrom AND p.id = :parcelId AND p.user.id = :userId) ORDER BY r.seedDate");
    query.setParameter("userId", userId);
    query.setParameter("parcelId", parcelId);
    query.setParameter("dateFrom", dateFrom);

    return (Collection) query.getResultList();
  }

  /**
   * @param userId
   * @param parcelId
   * @param dateUntil
   * @return referencia a un objeto de tipo Collection que
   * contiene todos los registros de plantacion de una
   * parcela que tienen una fecha de cosecha menor o igual
   * a la fecha hasta elegida
   */
  public Collection<PlantingRecord> findAllByDateLessThanOrEqual(int userId, int parcelId, Calendar dateUntil) {
    Query query = getEntityManager().createQuery("SELECT r FROM PlantingRecord r JOIN r.parcel p WHERE (r.harvestDate <= :dateUntil AND p.id = :parcelId AND p.user.id = :userId) ORDER BY r.harvestDate");
    query.setParameter("userId", userId);
    query.setParameter("parcelId", parcelId);
    query.setParameter("dateUntil", dateUntil);

    return (Collection) query.getResultList();
  }

  /**
   * @param userId
   * @param parcelId
   * @param dateFrom
   * @param dateUntil
   * @return referencia a un objeto de tipo Collection que
   * contiene todos los registros de plantacion de una parcela
   * que tienen una fecha de siembra mayor o igual a la fecha
   * desde elegida y una fecha de cosecha menor o igual a la
   * fecha hasta elegida
   */
  public Collection<PlantingRecord> findByAllFilterParameters(int userId, int parcelId, Calendar dateFrom, Calendar dateUntil) {
    Query query = getEntityManager().createQuery("SELECT r FROM PlantingRecord r JOIN r.parcel p WHERE (r.seedDate >= :dateFrom AND r.harvestDate <= :dateUntil AND p.id = :parcelId AND p.user.id = :userId) ORDER BY r.seedDate");
    query.setParameter("userId", userId);
    query.setParameter("parcelId", parcelId);
    query.setParameter("dateFrom", dateFrom);
    query.setParameter("dateUntil", dateUntil);

    return (Collection) query.getResultList();
  }

  /**
   * Retorna los registros de plantacion finalizados de una
   * parcela que estan dentro de un periodo definido por dos
   * fechas
   * 
   * @param parcelId
   * @param dateFrom
   * @param dateUntil
   * @return referencia a un objeto de tipo Collection que
   * contiene los registros de plantacion finalizados de
   * una parcela que se encuentran dentro de un periodo
   * definido por dos fechas
   */
  public List<PlantingRecord> findAllFinishedByPeriod(int parcelId, Calendar dateFrom, Calendar dateUntil) {
    /*
     * Con esta condicion se seleccionan todos los registros de
     * plantacion finalizados (*) de una parcela que estan entre
     * una fecha desde y una fecha hasta.
     * 
     * Con la primera condicion se selecciona el registro de
     * plantacion finalizado (*) de una parcela que tiene su fecha
     * de siembra mayor o igual a la fecha desde y menor o igual
     * a la fecha hasta, y su fecha de cosecha estrictamente mayor
     * a la fecha hasta. Es decir, se selecciona el registro de
     * plantacion finalizado de una parcela que tiene unicamente
     * su fecha de siembra dentro del periodo definido por la
     * fecha desde y la fecha hasta.
     * 
     * Con la segunda condicion se selecciona el registro de
     * plantacion finalizado (*) de una parcela que tiene su fecha
     * de siembra mayor o igual a la fecha desde y su fecha de
     * cosecha menor o igual a la fecha hasta. Es decir, se
     * selecciona el registro de plantacion que tiene su fecha
     * de siembra y su fecha de cosecha dentro del periodo
     * definido por la fecha desde y la fecha hasta.
     * 
     * Con la tercera conidicon se selecciona el registro de
     * plantacion finalizado (*) de una parcela que tiene su
     * fecha de cosecha mayor o igual a la fecha desde y menor
     * igual a la fecha hasta, y su fecha de siembra estrictamente
     * menor a la fecha desde. Es decir, se selecciona el registro
     * de plantacion finalizado de una parcela que tiene unicamente
     * su fecha de cosecha dentro del periodo definido por la
     * fecha desde y la fecha hasta.
     * 
     * (*) El ID para el estado finalizado de un registro de
     * plantacion es el 1, siempre y cuando no se modifique el
     * orden en el que se ejecutan las instrucciones de insercion
     * del archivo plantingRecordStatusInserts.sql de la ruta
     * app/etc/sql.
     */
    String conditionWhere = "r.parcel.id = :parcelId AND UPPER(r.status.name) = UPPER('Finalizado') AND "
        + "((:dateFrom <= r.seedDate AND r.seedDate <= :dateUntil AND r.harvestDate > :dateUntil) OR "
        + "(r.seedDate >= :dateFrom AND r.harvestDate <= :dateUntil) OR "
        + "(:dateFrom <= r.harvestDate AND r.harvestDate <= :dateUntil AND r.seedDate < :dateFrom))";

    Query query = getEntityManager().createQuery("SELECT r FROM PlantingRecord r WHERE " + conditionWhere + " ORDER BY r.seedDate");
    query.setParameter("parcelId", parcelId);
    query.setParameter("dateFrom", dateFrom);
    query.setParameter("dateUntil", dateUntil);

    return (List) query.getResultList();
  }

  /**
   * Retorna todos los registros de plantacion finalizados de
   * una parcela de un usuario
   * 
   * @param userId
   * @param parcelId
   * @return referencia a un objeto de tipo Collection que
   * contiene los registros de plantacion finalizados de una
   * parcela de un usuario
   */
  public Collection<PlantingRecord> findAllFinishedByParcelId(int userId, int parcelId) {
    Query query = getEntityManager().createQuery("SELECT r FROM PlantingRecord r JOIN r.parcel p WHERE (UPPER(r.status.name) = UPPER('Finalizado') AND p.id = :parcelId AND p.user.id = :userId) ORDER BY r.id");
    query.setParameter("userId", userId);
    query.setParameter("parcelId", parcelId);

    return (Collection) query.getResultList();
  }

  /**
   * Retorna todos los registros de plantacion en espera de
   * una parcela de un usuario
   * 
   * @param userId
   * @param parcelId
   * @return referencia a un objeto de tipo Collection que
   * contiene los registros de plantacion en espera de una
   * parcela de un usuario
   */
  public Collection<PlantingRecord> findAllInWaitingByParcelId(int userId, int parcelId) {
    Query query = getEntityManager().createQuery("SELECT r FROM PlantingRecord r JOIN r.parcel p WHERE (UPPER(r.status.name) = UPPER('En espera') AND p.id = :parcelId AND p.user.id = :userId) ORDER BY r.id");
    query.setParameter("userId", userId);
    query.setParameter("parcelId", parcelId);

    return (Collection) query.getResultList();
  }

  /**
   * Retorna todos los registros de plantacion finalizados
   * de todas las parcelas registradas en la base de datos
   * subyacente
   * 
   * @return referencia a un objeto de tipo Collection que
   * contiene todos los registros de plantacion finalizados
   * de todas las parcelas registradas en la base de datos
   * subyacente
   */
  public Collection<PlantingRecord> findAllFinished() {
    Query query = getEntityManager().createQuery("SELECT p FROM PlantingRecord p WHERE p.status.name = 'Finalizado' ORDER BY p.id");
    return (Collection) query.getResultList();
  }

  /**
   * Retorna todos los registros de plantacion en desarrollo
   * de todas las parcelas registradas en la base de datos
   * subyacente.
   * 
   * Este metodo es para el metodo automatico modifyStatus
   * de la clase PlantingRecordManager. El metodo modifyStatus
   * se ocupa de comprobar si la fecha de cosecha de un
   * registro de plantacion presuntamente en desarrollo
   * es estrictamente menor a la fecha actual y en base
   * a esto establece el estado finalizado en el registro.
   * 
   * @return referencia a un objeto de tipo Collection que
   * contiene todos los registros de plantacion en desarrollo
   * de todas las parcelas registradas en la base de datos
   * subyacente
   */
  public Collection<PlantingRecord> findAllInDevelopment() {
    Query query = getEntityManager().createQuery("SELECT r FROM PlantingRecord r JOIN r.status s WHERE s IN (SELECT t FROM PlantingRecordStatus t WHERE UPPER(t.name) LIKE (CONCAT('%', UPPER('Desarrollo'), '%'))) ORDER BY r.id");
    return (Collection) query.getResultList();
  }

  /**
   * Retorna una coleccion que contiene el registro de plantacion
   * en espera mas antiguo de los registros de plantacion en espera
   * de cada una de las parcelas registradas en la base de datos
   * subyacente.
   * 
   * Este metodo es para el metodo automatico modifyToInDevelopmentStatus
   * de la clase PlantingRecordManager. El metodo modifyToInDevelopmentStatus
   * se ocupa de comprobar si un registro de plantacion presuntamente
   * en espera, esta en espera y en base a esto establece el
   * estado en desarrollo en el mismo.
   * 
   * @return referencia a un objeto de tipo Collection que
   * contiene el registro de plantacion en espera mas antiguo
   * de los registros de plantacion en espera de cada una de
   * las parcelas registradas en la base de datos subyacente
   */
  public Collection<PlantingRecord> findAllInWaiting() {
    Query query = getEntityManager().createQuery("SELECT r FROM PlantingRecord r WHERE r.seedDate IN (SELECT MIN(t.seedDate) FROM PlantingRecord t WHERE t.status.name = 'En espera' GROUP BY t.parcel)");
    return (Collection) query.getResultList();
  }

  /**
   * Retorna todos los registros de plantacion finalizados
   * de una parcela de un usuario que estan en un periodo
   * definido por dos fechas
   * 
   * @param userId
   * @param parcelId
   * @param dateFrom
   * @param dateUntil
   * @return referencia a un objeto de tipo Collection que
   * contiene los registros de plantacion finalizados de una
   * parcela de un usuario que estan en un periodo definido
   * por dos fechas
   */
  public Collection<PlantingRecord> findAllByParcelIdAndPeriod(int userId, int parcelId, Calendar dateFrom, Calendar dateUntil) {
    Query selectFinishedPlantingRecordsByPeriod = createFinishedPlantingRecordsQuery(userId, parcelId, dateFrom, dateUntil);
    return (Collection) selectFinishedPlantingRecordsByPeriod.getResultList();
  }

  /**
   * @param parcelId
   * @return referencia a un objeto de tipo PlantingRecord que
   * representa un registro de plantacion que tiene un estado
   * de desarrollo (en desarrollo, desarrollo optimo, desarrollo
   * en riesgo de marchitez, desarrollo en marchitez), si existe
   * en la base de datos subyacente. En caso contrario, retorna
   * null.
   */
  public PlantingRecord findInDevelopment(int parcelId) {
    Query query = getEntityManager().createQuery("SELECT r FROM PlantingRecord r JOIN r.parcel p JOIN r.status s WHERE (p.id = :parcelId AND s IN (SELECT t FROM PlantingRecordStatus t WHERE UPPER(t.name) LIKE (CONCAT('%', UPPER('Desarrollo'), '%'))))");
    query.setParameter("parcelId", parcelId);

    PlantingRecord plantingRecord = null;

    try {
      plantingRecord = (PlantingRecord) query.getSingleResult();
    } catch(NoResultException e) {
      e.printStackTrace();
    }

    return plantingRecord;
  }

  /**
   * @param parcelId
   * @return referencia a un objeto de tipo PlantingRecord que
   * representa un registro de plantacion que tiene un estado
   * de desarrollo relacionado al uso de datos de suelo (desarrollo
   * optimo, desarrollo en riesgo de marchitez, desarrollo en
   * marchitez), si existe en la base de datos subyacente.
   * En caso contrario, retorna null.
   */
  public PlantingRecord findInDevelopmentRelatedToSoil(int parcelId) {
    Query query = getEntityManager().createQuery("SELECT r FROM PlantingRecord r JOIN r.parcel p JOIN r.status s WHERE (p.id = :parcelId AND (s.name = 'Desarrollo óptimo' OR s.name = 'Desarrollo en riesgo de marchitez' OR s.name = 'Desarrollo en marchitez'))");
    query.setParameter("parcelId", parcelId);

    PlantingRecord plantingRecord = null;

    try {
      plantingRecord = (PlantingRecord) query.getSingleResult();
    } catch(NoResultException e) {
      e.printStackTrace();
    }

    return plantingRecord;
  }

  /**
   * Busca un registro de plantacion, correspondiente a una parcela
   * dada de un usuario dado, en el que una fecha dada este en el
   * periodo definido por su fecha de siembra y su fecha de cosecha
   * 
   * @param userId
   * @param givenParcel
   * @param givenDate
   * @return referencia a un objeto de tipo PlantingRecord si
   * se encuentra el registro de plantacion de una parcela dada
   * correspondiente a un usuario dado, en el que la fecha dada
   * este en el periodo definido por su fecha de siembra y su
   * fecha de cosecha
   */
  public PlantingRecord findByDate(int userId, Parcel parcel, Calendar givenDate) {
    /*
     * Selecciona el registro de plantacion de una parcela correspondiente
     * a un usuario, en el que una fecha dada este en el periodo definido
     * por su fecha de siembra y su fecha de cosecha
     */
    Query query = getEntityManager().createQuery("SELECT r FROM PlantingRecord r JOIN r.parcel p WHERE (p = :parcel AND p.user.id = :userId AND :date >= r.seedDate AND :date <= r.harvestDate)");
    query.setParameter("userId", userId);
    query.setParameter("parcel", parcel);
    query.setParameter("date", givenDate);

    PlantingRecord plantingRecord = null;

    try {
      plantingRecord = (PlantingRecord) query.getSingleResult();
    } catch(NoResultException e) {
      e.printStackTrace();
    }

    return plantingRecord;
  }

  /**
   * Actualiza el estado de todos los registros de plantacion
   * asociados a una parcela de un usuario, asignando el valor
   * "Finalizado" al estado y los valores "n/a", 0 y 0 a los
   * atributos de "necesidad de agua de riego de cultivo",
   * "lamina total de agua disponible" y "lamina de riego optima",
   * respectivamente. Esta actualizacion se aplica solo a los
   * registros cuya fecha de cosecha sea anterior a la fecha
   * actual (hoy) y cuyo estado sea un estado de desarrollo
   * (en desarrollo, desarrollo optimo, desarrollo en riesgo
   * de machitez, desarrollo en marchitez).
   * 
   * @param userId
   * @param parcelId
   * @param finishedStatus
   */
  public void setFinishedStatusByUserIdAndParcelId(int userId, int parcelId, PlantingRecordStatus finishedStatus) {
    Query query = getEntityManager().createQuery("UPDATE PlantingRecord r SET r.cropIrrigationWaterNeed = :notAvailable, r.totalAmountWaterAvailable = 0, r.optimalIrrigationLayer = 0, r.status = :finishedStatus WHERE r.parcel.user.id = :userId AND r.parcel.id = :parcelId AND UPPER(r.status.name) LIKE (CONCAT('%', UPPER('Desarrollo'), '%')) AND r.harvestDate < CURRENT_DATE");
    query.setParameter("userId", userId);
    query.setParameter("parcelId", parcelId);
    query.setParameter("finishedStatus", finishedStatus);
    query.setParameter("notAvailable", NOT_AVAILABLE);

    query.executeUpdate();
  }

  /**
   * @param userId
   * @param givenParcel
   * @param givenDate
   * @return true si existe un registro de plantacion de una parcela
   * dada correspondiente a un usuario dado, en el que la fecha
   * dada este en el periodo definido por su fecha de siembra y su
   * fecha de cosecha. En caso contrario, false.
   */
  public boolean checkByDate(int userId, Parcel givenParcel, Calendar givenDate) {
    return (findByDate(userId, givenParcel, givenDate) != null);
  }

  /**
   * Retorna true si y solo si una parcela tiene un registro de
   * plantacion en desarrollo. Esto significa que retorna true
   * si y solo si una parcela tiene un cultivo en desarrollo.
   * 
   * @param givenParcel
   * @return true si la parcela dada tiene un registro de
   * plantacion en desarrollo, false en caso contrario
   */
  public boolean checkOneInDevelopment(int parcelId) {
    return (findInDevelopment(parcelId) != null);
  }

  /**
   * @param parcelId
   * @return true si una parcela tiene un registro de
   * plantacion que tiene un estado de desarrollo
   * relacionado al uso de datos de suelo (desarrollo
   * optimo, desarrollo en riesgo de machitez, desarrollo
   * en marchitez). En caso contrario, false.
   */
  public boolean checkOneInDevelopmentRelatedToSoil(int parcelId) {
    return (findInDevelopmentRelatedToSoil(parcelId) != null);
  }

  /**
   * Retorna true si y solo si una parcela de un usuario tiene al
   * menos un registro de plantacion con el estado "Finalizado"
   * 
   * @param userId
   * @param parcelId
   * @return true si una parcela de un usuario tiene al menos un
   * registro de plantacion con el estado "Finalizado", en caso
   * contrario false
   */
  public boolean hasFinishedPlantingRecords(int userId, int parcelId) {
    Query query = getEntityManager().createQuery("SELECT r FROM PlantingRecord r JOIN r.parcel p WHERE p.user.id = :userId AND p.id = :parcelId AND UPPER(r.status.name) = UPPER('Finalizado')");
    query.setParameter("userId", userId);
    query.setParameter("parcelId", parcelId);
    query.setMaxResults(1);

    return !query.getResultList().isEmpty();
  }

  /**
   * Retorna true si y solo si una parcela de un usuario tiene
   * al menos un registro de plantacion con el estado "Finalizado"
   * en un periodo definido por dos fechas
   * 
   * @param userId
   * @param parcelId
   * @param dateFrom
   * @param dateUntil
   * @return true si una parcela de un usuario tiene al menos
   * un registro de plantacion con el estado "Finalizado" en
   * un periodo definido por dos fechas, en caso contrario
   * false
   */
  public boolean hasFinishedPlantingRecordsInPeriod(int userId, int parcelId, Calendar dateFrom, Calendar dateUntil) {
    Query selectFinishedPlantingRecordsByPeriod = createFinishedPlantingRecordsQuery(userId, parcelId, dateFrom, dateUntil);
    selectFinishedPlantingRecordsByPeriod.setMaxResults(1);

    return !selectFinishedPlantingRecordsByPeriod.getResultList().isEmpty();
  }

  /**
   * Retorna true si y solo si una parcela de un usuario tiene
   * al menos un registro de plantacion con el estado "En espera"
   * 
   * @param userId
   * @param parcelId
   * @return true si una parcela de un usuario tiene al menos
   * un registro de plantacion con el estado "En espera", en
   * caso contrario false
   */
  public boolean hasInWaitingPlantingRecords(int userId, int parcelId) {
    Query query = getEntityManager().createQuery("SELECT r FROM PlantingRecord r JOIN r.parcel p WHERE p.user.id = :userId AND p.id = :parcelId AND UPPER(r.status.name) = UPPER('En espera')");
    query.setParameter("userId", userId);
    query.setParameter("parcelId", parcelId);
    query.setMaxResults(1);

    return !query.getResultList().isEmpty();
  }

  /**
   * @param userId
   * @param parcelId
   * @param dateFrom
   * @param dateUntil
   * @return referencia a un objeto de tipo Query que contiene
   * una consulta JPQL para obtener de la base de datos los
   * registros de plantacion con el estado "Finalizado",
   * pertenecientes a una parcela de un usuario, y que estan
   * comprendidos en un periodo definido por dos fechas
   */
  private Query createFinishedPlantingRecordsQuery(int userId, int parcelId, Calendar dateFrom, Calendar dateUntil) {
    /*
     * Con esta condicion se seleccionan todos los registros de
     * plantacion finalizados (*) de una parcela que estan entre
     * una fecha desde y una fecha hasta.
     * 
     * Con la primera condicion se selecciona el registro de
     * plantacion finalizado (*) de una parcela que tiene su fecha
     * de siembra mayor o igual a la fecha desde y menor o igual
     * a la fecha hasta, y su fecha de cosecha estrictamente mayor
     * a la fecha hasta. Es decir, se selecciona el registro de
     * plantacion finalizado de una parcela que tiene unicamente
     * su fecha de siembra dentro del periodo definido por la
     * fecha desde y la fecha hasta.
     * 
     * Con la segunda condicion se selecciona el registro de
     * plantacion finalizado (*) de una parcela que tiene su fecha
     * de siembra mayor o igual a la fecha desde y su fecha de
     * cosecha menor o igual a la fecha hasta. Es decir, se
     * selecciona el registro de plantacion que tiene su fecha
     * de siembra y su fecha de cosecha dentro del periodo
     * definido por la fecha desde y la fecha hasta.
     * 
     * Con la tercera conidicon se selecciona el registro de
     * plantacion finalizado (*) de una parcela que tiene su
     * fecha de cosecha mayor o igual a la fecha desde y menor
     * igual a la fecha hasta, y su fecha de siembra estrictamente
     * menor a la fecha desde. Es decir, se selecciona el registro
     * de plantacion finalizado de una parcela que tiene unicamente
     * su fecha de cosecha dentro del periodo definido por la
     * fecha desde y la fecha hasta.
     * 
     * (*) El ID para el estado finalizado de un registro de
     * plantacion es el 1, siempre y cuando no se modifique el
     * orden en el que se ejecutan las instrucciones de insercion
     * del archivo plantingRecordStatusInserts.sql de la ruta
     * app/etc/sql.
     * 
     * El motivo por el cual esta condicion esta en este metodo
     * es que se la usa para calcular los siguientes datos de
     * una parcela:
     * - el cultivo que mas veces se planto,
     * - el cultivo que menos veces se planto,
     * - el cultivo plantado con el mayor ciclo de vida,
     * - el cultivo plantado con el menor ciclo de vida y
     * - la cantidad de dias en los que una parcela no tuvo
     * ningun cultivo plantado.
     * 
     * Estos datos son parte del informe estadistico de una
     * parcela.
     */
    String dateCondition = "(:dateFrom <= r.seedDate AND r.seedDate <= :dateUntil AND r.harvestDate > :dateUntil) OR "
        + "(r.seedDate >= :dateFrom AND r.harvestDate <= :dateUntil) OR "
        + "(:dateFrom <= r.harvestDate AND r.harvestDate <= :dateUntil AND r.seedDate < :dateFrom)";

    String conditionWhere = "p.id = :parcelId AND UPPER(r.status.name) = UPPER('Finalizado') AND p.user.id = :userId AND (" + dateCondition + ")";

    /*
     * Selecciona los registros de plantacion finalizados
     * de una parcela de un usuario que estan en un periodo
     * definido por dos fechas
     */
    Query selectPlantingRecordsByPeriod = getEntityManager().createQuery("SELECT r FROM PlantingRecord r JOIN r.parcel p WHERE " + conditionWhere + " ORDER BY r.seedDate");
    selectPlantingRecordsByPeriod.setParameter("userId", userId);
    selectPlantingRecordsByPeriod.setParameter("parcelId", parcelId);
    selectPlantingRecordsByPeriod.setParameter("dateFrom", dateFrom);
    selectPlantingRecordsByPeriod.setParameter("dateUntil", dateUntil);

    return selectPlantingRecordsByPeriod;
  }

  /**
   * Comprueba si un registro de plantacion pertenece a un usuario
   * dado, mediante la relacion muchos a uno que hay entre los
   * modelos de datos PlantingRecord y Parcel.
   * 
   * Retorna true si y solo si el registro de plantacion correspondiente
   * al ID dado pertenece al usuario con el ID dado.
   * 
   * @param userId
   * @param plantingRecordId
   * @return true si se encuentra el registro de plantacion con el
   * ID y el ID de usuario provistos, false en caso contrario
   */
  public boolean checkUserOwnership(int userId, int plantingRecordId) {
    return (findByUserId(userId, plantingRecordId) != null);
  }

  /**
   * Comprueba la existencia de un registro de plantacion en la
   * base de datos subyacente. Retorna true si y solo si existe
   * el registro de plantacion con el ID dado.
   * 
   * @param id
   * @return true si el registro de plantacion con el ID dado
   * existe en la base de datos subyacente, false en caso contrario
   */
  public boolean checkExistence(int id) {
    return (getEntityManager().find(PlantingRecord.class, id) != null);
  }

  /**
   * Retorna true si y solo si la fecha de siembra de un registro
   * de plantacion es estrictamente mayor (posterior) que la fecha
   * actual, la cual esta contenida en la referencia al objeto de
   * tipo Calendar devuelta por el metodo getCurrentDate() de la
   * clase UtilDate de la aplicacion.
   * 
   * @param givenPlantingRecord
   * @return true si la fecha de siembra del objeto de tipo PlantingRecord
   * referenciado por la referencia contenida en la variable de tipo por
   * referencia givenPlantingRecord de tipo PlantingRecord, es mayor
   * estricta (posterior) a la fecha actual, false en caso contrario
   */
  public boolean isFromFuture(PlantingRecord givenPlantingRecord) {
    return givenPlantingRecord.getSeedDate().after(UtilDate.getCurrentDate());
  }

  /**
   * @param parcelId
   * @param dateFrom
   * @param dateUntil
   * @return entero que representa la cantidad de dias en los que
   * una parcela tuvo un cultivo plantado dentro de un periodo
   * definido por dos fechas, contemplando los años bisiestos y
   * no bisiestos que pueden haber en dicho periodo, si una parcela
   * tiene registros de plantacion finalizados en el periodo en
   * el que se quiere obtener dicha cantidad. En caso contrario,
   * -1, valor que se utiliza para representar informacion no
   * disponible.
   */
  public int calculateDaysWithCrops(int parcelId, Calendar dateFrom, Calendar dateUntil) {
    Calendar seedDate;
    Calendar harvestDate;
    int daysWithCrops = 0;

    /*
     * Obtiene todos los registros de plantacion finalizados
     * de una parcela que estan dentro de un periodo definido
     * por dos fechas
     */
    List<PlantingRecord> plantingRecords = findAllFinishedByPeriod(parcelId, dateFrom, dateUntil);

    /*
     * Si la parcela correspondiente al ID dado no tiene ningun
     * registro de plantacion finalizado en un periodo definido
     * por dos fechas, se retorna -1 como valor indicativo de
     * que la cantidad de dias en los que una parcela tuvo un
     * cultivo plantado en un periodo dado, no esta disponible
     */
    if (plantingRecords.size() == 0) {
      return -1;
    }

    for (PlantingRecord currentPlantingRecord : plantingRecords) {
      seedDate = currentPlantingRecord.getSeedDate();
      harvestDate = currentPlantingRecord.getHarvestDate();

      /*
       * Si la fecha de siembra mayor o igual a la fecha desde y
       * menor o igual a la fecha hasta, y la fecha de cosecha es
       * estrictamente mayor a la fecha hasta, la cantidad de dias
       * en los que una parcela tuvo un cultivo plantado se calcula
       * como la diferencia entre la fecha de siembra y la fecha
       * hasta mas uno. A esta diferencia se le suma un uno para
       * incluir a la fecha de siembra en el resultado, ya que la
       * misma cuenta como un dia en el que una parcela tuvo un
       * cultivo plantado.
       * 
       * El motivo por el cual se realiza este calculo es que la
       * fecha hasta esta dentro del periodo definido por la fecha
       * de siembra y la fecha de cosecha.
       * 
       *              fecha siembra                 fecha cosecha
       *<------------------[-----------------------------]------>
       *
       *    fecha desde                  fecha hasta
       *<-------[----------------------------]------------------>
       */
      if (UtilDate.compareTo(seedDate, dateFrom) >= 0 && UtilDate.compareTo(seedDate, dateUntil) <= 0 && UtilDate.compareTo(harvestDate, dateUntil) > 0) {
        daysWithCrops = daysWithCrops + UtilDate.calculateDifferenceBetweenDates(seedDate, dateUntil) + 1;
      }

      /*
       * Si la fecha de siembra es mayor o igual a la fecha desde
       * y la fecha de cosecha es menor o igual a la fecha hasta,
       * la cantidad de dias en los que una parcela tuvo un cultivo
       * plantado se calcula como la diferencia entre la fecha de
       * siembra y la fecha de cosecha mas uno. A esta diferencia
       * se le suma un uno para incluir a la fecha de siembra en
       * el resultado, ya que la misma cuenta como un dia en el que
       * una parcela tuvo un cultivo plantado.
       * 
       *        fecha siembra                 fecha cosecha
       *<------------[-----------------------------]------------>
       *
       *    fecha desde                           fecha hasta
       *<-----[-------------------------------------------]----->
       */
      if (UtilDate.compareTo(seedDate, dateFrom) >= 0 && UtilDate.compareTo(harvestDate, dateUntil) <= 0) {
        daysWithCrops = daysWithCrops + UtilDate.calculateDifferenceBetweenDates(currentPlantingRecord.getSeedDate(), currentPlantingRecord.getHarvestDate()) + 1;
      }

      /*
       * Si la fecha de cosecha es mayor o igual a la fecha desde
       * y menor o igual a la fecha hasta, y la fecha de siembra
       * es estrictamente menor a la fecha desde, la cantidad de
       * dias en los que una parcela tuvo un cultivo plantado se
       * calcula como la diferencia entre la fecha desde y la
       * fecha de cosecha mas uno. A esta diferencia se le suma
       * un uno para incluir a la fecha desde en el resultado,
       * ya que la misma cuenta como un dia en el que una parcela
       * tuvo un cultivo plantado porque esta dentro del periodo
       * definido por la fecha de siembra y la fecha de cosecha.
       * 
       *  fecha siembra             fecha cosecha
       *<------[-------------------------]----------------------->
       *
       *            fecha desde                  fecha hasta
       *<----------------[----------------------------]---------->
       */
      if (UtilDate.compareTo(harvestDate, dateFrom) >= 0 && UtilDate.compareTo(harvestDate, dateUntil) <= 0 && UtilDate.compareTo(seedDate, dateFrom) < 0) {
        daysWithCrops = daysWithCrops + UtilDate.calculateDifferenceBetweenDates(dateFrom, harvestDate) + 1;
      }

    }

    return daysWithCrops;
  }

  /**
   * @param parcelId
   * @param dateFrom
   * @param dateUntil
   * @return entero que representa la cantidad de dias en los que
   * una parcela no tuvo ningun cultivo plantado dentro de un periodo
   * definido por dos fechas, contemplando los años bisiestos y no
   * bisiestos que pueden haber en dicho periodo, si una parcela
   * tiene registros de plantacion finalizados en el periodo en el
   * que se quiere obtener dicha cantidad. En caso contrario, -1,
   * valor que representa informacion no disponible.
   */
  public int calculateDaysWithoutCrops(int parcelId, Calendar dateFrom, Calendar dateUntil) {
    int daysWithoutCrops = 0;
    int daysDifference = 0;

    /*
     * Obtiene todos los registros de plantacion finalizados
     * de una parcela que estan dentro de un periodo definido
     * por dos fechas
     */
    List<PlantingRecord> plantingRecords = findAllFinishedByPeriod(parcelId, dateFrom, dateUntil);

    /*
     * Si la parcela correspondiente al ID dado no tiene ningun
     * registro de plantacion finalizado en un periodo definido
     * por dos fechas, se retorna -1 como valor indicativo de
     * que la cantidad de dias en los que una parcela no tuvo
     * ningun cultivo plantado en un periodo dado, no esta
     * disponible
     */
    if (plantingRecords.size() == 0) {
      return -1;
    }

    /*
     * Calcula la diferencia de dias que hay entre la fecha
     * desde y la fecha de siembra del primer registro de
     * plantacion finalizado de una parcela, el cual, esta
     * dentro de un periodo definido por dos fechas
     */
    daysWithoutCrops = calculateDifferenceDateFromAndSeedDate(dateFrom, plantingRecords.get(0).getSeedDate());

    /*
     * Calcula la diferencia de dias que hay entre la fecha
     * de cosecha del ultimo registro de plantacion finalizado
     * de una parcela y la fecha hasta, el cual, esta dentro
     * de un periodo definido por dos fechas
     */
    daysWithoutCrops = daysWithoutCrops + calculateDifferenceHarvestDateAndDateUntil(plantingRecords.get(plantingRecords.size() - 1).getHarvestDate(), dateUntil);

    Calendar harvestDateCurrentPlantingRecord;
    Calendar seedDateNextPlantingRecord;

    for (int i = 0; i < plantingRecords.size() - 1; i++) {
      harvestDateCurrentPlantingRecord = plantingRecords.get(i).getHarvestDate();
      seedDateNextPlantingRecord = plantingRecords.get(i + 1).getSeedDate();

      daysDifference = UtilDate.calculateDifferenceBetweenDates(harvestDateCurrentPlantingRecord, seedDateNextPlantingRecord);

      /*
       * Si la diferencia de dias que hay entre la fecha de cosecha
       * del registro de plantacion actual y la fecha de siembra del
       * siguiente registro de plantacion, es mayor a 1, significa
       * que la fecha de siembra del siguiente registro de plantacion
       * NO es el dia inmediatamente siguiente a la fecha de cosecha
       * del registro de plantacion actual.
       * 
       * Por lo tanto, a la cantidad de dias en los que una parcela
       * no tuvo ningun cultivo plantado desde una fecha desde hasta
       * una fecha hasta, se le suma la diferencia de dias que hay
       * entre la fecha de siembra del siguiente registro de plantacion
       * y la fecha de cosecha del registro de plantacion actual
       * menos uno, ya que la fecha de siembra no cuenta como un dia
       * en el que una parcela no tuvo ningun cultivo plantado.
       * 
       * Si la diferencia de dias entre la fecha de siembra del
       * siguiente registro de plantacion y la fecha de cosecha del
       * registro de plantacion actual, es igual 1, significa que la
       * fecha de siembra del siguiente registro de plantacion es el
       * dia inmediatamente siguiente a la fecha de cosecha del
       * registro de plantacion actual. Por lo tanto, no se cuenta
       * esta diferencia como un dia en el que una parcela no tuvo
       * ningun cultivo plantado.
       * 
       * Si se elimina esta instruccion if, y la fecha de cosecha
       * del registro de plantacion actual y la fecha de siembra
       * del siguiente registro de plantacion, son iguales, la
       * diferencia de dias entre ambas fechas sera cero, con lo
       * cual, se restara una unidad a la cantidad de dias en los
       * que una parcela no tuvo ningun cultivo plantado, lo cual,
       * es erroneo.
       */
      if (daysDifference > 1) {
        daysWithoutCrops = daysWithoutCrops + (daysDifference - 1);
      }

    }

    return daysWithoutCrops;
  }

  /**
   * @param dateFrom
   * @param seedDate
   * @return entero que representa la cantidad de dias de diferencia
   * que hay entre una desde y una fecha de siembra contemplando los
   * años bisiestos y no bisiestos que hay entre ambas fechas
   */
  private int calculateDifferenceDateFromAndSeedDate(Calendar dateFrom, Calendar seedDate) {
    return UtilDate.calculateDifferenceBetweenDates(dateFrom, seedDate);
  }

  /**
   * @param harvestDate
   * @param dateUntil
   * @return entero que representa la cantidad de dias de diferencia
   * que hay entre una fecha de cosecha y una fecha hasta contemplando
   * los años bisiestos y no bisiestos que hay entre ambas fechas.
   * En el caso en el que la fecha hasta sea estrictamente menor a
   * la fecha de cosecha, retorna 0.
   */
  private int calculateDifferenceHarvestDateAndDateUntil(Calendar harvestDate, Calendar dateUntil) {
    /*
     * Este control es para el caso en el que la fecha desde (*)
     * es estrictamente menor a la fecha de siembra de un
     * registro de plantacion finalizado de un conjunto de
     * registros de plantacion finalizados de una parcela, y la
     * fecha hasta es mayor o igual a la fecha de siembra y es
     * estrictamente menor a la fecha de cosecha de dicho
     * registro. En este caso, no hay ningun dia entre la fecha
     * de cosecha y la fecha hasta que cuente como un dia en el
     * que una parcela no tuvo ningun cultivo plantado. Por lo
     * tanto, en este caso se retorna 0 como la cantidad de dias
     * en los que una parcela no tuvo ningun cultivo plantado
     * entre una fecha de cosecha y una fecha hasta.
     * 
     * (*) Un informe estadistico de una parcela se genera a
     * partir de una fecha desde y una fecha hasta.
     */
    if (UtilDate.compareTo(dateUntil, harvestDate) < 0) {
      return 0;
    }

    return UtilDate.calculateDifferenceBetweenDates(harvestDate, dateUntil);
  }

  /**
   * @param plantingRecord
   * @return true si un registro de plantacion esta finalizado.
   * En caso contrario, false, lo cual puede indicar que un
   * registro de plantacion esta en desarrollo o en espera.
   */
  public boolean isFinished(PlantingRecord plantingRecord) {
    /*
     * Si la fecha de cosecha de un registro de plantacion es
     * estrictamente menor (es decir, anterior) a la fecha actual,
     * el registro de plantacion esta finalizado
     */
    if (UtilDate.compareTo(plantingRecord.getHarvestDate(), UtilDate.getCurrentDate()) < 0) {
      return true;
    }

    return false;
  }

  /**
   * @param plantingRecord
   * @return true si un registro de plantacion esta en desarrollo.
   * En caso contrario, false, lo cual puede indicar que un registro
   * de plantacion esta finalizado o en espera.
   */
  public boolean isInDevelopment(PlantingRecord plantingRecord) {
    Calendar currentDate = UtilDate.getCurrentDate();

    /*
     * Si la fecha de siembra de un registro de plantacion es menor
     * o igual a la fecha actual y su fecha de cosecha es mayor o igual
     * a la fecha actual, el registro de plantacion esta en desarrollo
     */
    if ((UtilDate.compareTo(plantingRecord.getSeedDate(), currentDate) <= 0) && (UtilDate.compareTo(plantingRecord.getHarvestDate(), currentDate) >= 0)) {
      return true;
    }

    return false;
  }

  /**
   * Establece un estado en un registro de plantacion en la
   * base de datos subyacente
   * 
   * @param plantingRecordId
   * @param plantingRecordStatus
   */
  public void setStatus(int plantingRecordId, PlantingRecordStatus plantingRecordStatus) {
    Query query = entityManager.createQuery("UPDATE PlantingRecord p SET p.status = :givenStatus WHERE p.id = :givenId");
    query.setParameter("givenStatus", plantingRecordStatus);
    query.setParameter("givenId", plantingRecordId);
    query.executeUpdate();
  }

  /**
   * Actualiza la necesidad de agua de riego de un registro
   * de plantacion en la base de datos subyacente
   * 
   * @param plantingRecordId
   * @param cropIrrigationWaterNeed [mm/dia]
   */
  public void updateCropIrrigationWaterNeed(int plantingRecordId, String cropIrrigationWaterNeed) {
    Query query = entityManager.createQuery("UPDATE PlantingRecord p SET p.cropIrrigationWaterNeed = :cropIrrigationWaterNeed WHERE p.id = :plantingRecordId");
    query.setParameter("plantingRecordId", plantingRecordId);
    query.setParameter("cropIrrigationWaterNeed", cropIrrigationWaterNeed);
    query.executeUpdate();
  }

  /**
   * Actualiza la lamina total de agua disponible (dt)
   * [mm] de un registro de plantacion en la base de
   * datos subyacente
   * 
   * @param plantingRecordId
   * @param totalAmountWaterAvailable
   */
  public void updateTotalAmountWaterAvailable(int plantingRecordId, double totalAmountWaterAvailable) {
    Query query = entityManager.createQuery("UPDATE PlantingRecord p SET p.totalAmountWaterAvailable = :totalAmountWaterAvailable WHERE p.id = :plantingRecordId");
    query.setParameter("totalAmountWaterAvailable", totalAmountWaterAvailable);
    query.setParameter("plantingRecordId", plantingRecordId);
    query.executeUpdate();
  }

  /**
   * Actualiza la lamina de riego optima (drop) (umbral de
   * riego) [mm] de un registro de plantacion en la base
   * de datos subyacente
   * 
   * @param plantingRecordId
   * @param optimalIrrigationLayer
   */
  public void updateOptimalIrrigationLayer(int plantingRecordId, double optimalIrrigationLayer) {
    Query query = entityManager.createQuery("UPDATE PlantingRecord p SET p.optimalIrrigationLayer = :optimalIrrigationLayer WHERE p.id = :plantingRecordId");
    query.setParameter("optimalIrrigationLayer", optimalIrrigationLayer);
    query.setParameter("plantingRecordId", plantingRecordId);
    query.executeUpdate();
  }

  /**
   * Establece la fecha de muerte de un registro de
   * plantacion
   * 
   * @param plantingRecordId
   */
  public void setDeathDate(int plantingRecordId, Calendar deathDate) {
    Query query = entityManager.createQuery("UPDATE PlantingRecord p SET p.deathDate = :deathDate WHERE p.id = :plantingRecordId");
    query.setParameter("plantingRecordId", plantingRecordId);
    query.setParameter("deathDate", deathDate);
    query.executeUpdate();
  }

  /**
   * Elimina la fecha de muerte de un registro de
   * plantacion
   * 
   * @param plantingRecordId
   */
  public void unsetDeathDate(int plantingRecordId) {
    Query query = entityManager.createQuery("UPDATE PlantingRecord p SET p.deathDate = NULL WHERE p.id = :plantingRecordId");
    query.setParameter("plantingRecordId", plantingRecordId);
    query.executeUpdate();
  }

  /**
   * Retorna true si y solo si existe al menos un registro de
   * plantacion cuyo rango de fechas se superpone con la fecha
   * de siembra y/o la fecha de cosecha de un nuevo registro de
   * plantacion.
   * 
   * @param userId
   * @param parcelId
   * @param newSeedDate
   * @param newHarvestDate
   * @return true si una nueva fecha de siembra y/o una nueva
   * fecha de cosecha se superponen con el rango de fechas
   * [fecha de siembra, fecha de cosecha] de los registros de
   * plantacion de una parcela perteneciente a un usuario;
   * false en caso contrario.
   */
  public boolean checkDateOverlapOnCreation(int userId, int parcelId, Calendar newSeedDate, Calendar newHarvestDate) {
    /*
     * Condiciones de superposicion de fechas:
     * 1. r.seedDate BETWEEN :newSeedDate AND :newHarvestDate
     * 2. r.harvestDate BETWEEN :newSeedDate AND :newHarvestDate
     * 3. :newSeedDate BETWEEN r.seedDate AND r.harvestDate
     * 4. :newHarvestDate BETWEEN r.seedDate AND r.harvestDate
     * 
     * Con la primera condicion se comprueba si la fecha de siembra
     * de un registro de plantacion existente esta dentro del rango
     * de fechas de un nuevo registro de plantacion.
     * 
     * Con la segunda condicion se comprueba si la fecha de cosecha
     * de un registro de plantacion existente esta dentro del rango
     * de fechas de un nuevo registro de plantacion.
     * 
     * Con la tercera condicion se comprueba si la fecha de siembra
     * de un nuevo registro de plantacion esta dentro del rango de
     * fechas de un registro de plantacion existente.
     * 
     * Con la cuarta condicion se comprueba si la fecha de cosecha
     * de un nuevo registro de plantacion esta dentro del rango de
     * fechas de un registro de plantacion existente.
     */
    Query query = entityManager.createQuery("SELECT r FROM PlantingRecord r WHERE r.parcel.user.id = :userId AND r.parcel.id = :parcelId AND (r.seedDate BETWEEN :newSeedDate AND :newHarvestDate) OR (r.harvestDate BETWEEN :newSeedDate AND :newHarvestDate) OR (:newSeedDate BETWEEN r.seedDate AND r.harvestDate) OR (:newHarvestDate BETWEEN r.seedDate AND r.harvestDate)");
    query.setParameter("userId", userId);
    query.setParameter("parcelId", parcelId);
    query.setParameter("newSeedDate", newSeedDate);
    query.setParameter("newHarvestDate", newHarvestDate);
    query.setMaxResults(1);

    return !query.getResultList().isEmpty();
  }

  /**
   * Retorna true si y solo si existe al menos un registro de
   * plantacion cuyo rango de fechas se superpone con la fecha
   * de siembra y/o la fecha de cosecha modificadas de un registro
   * de plantacion.
   * 
   * @param userId
   * @param parcelId
   * @param modifiedPlantingRecordId
   * @param modifiedSeedDate
   * @param modifiedHarvestDate
   * 
   * @return true si una fecha de siembra modificada y/o una
   * fecha de cosecha modificada se superponen con el rango
   * de fechas [fecha de siembra, fecha de cosecha] de los
   * registros de plantacion de una parcela perteneciente a
   * un usuario; false en caso contrario.
   */
  public boolean checkDateOverlapOnModification(int userId, int parcelId, int modifiedPlantingRecordId, Calendar modifiedSeedDate, Calendar modifiedHarvestDate) {
    /*
     * Condiciones de superposicion de fechas:
     * 1. r.seedDate BETWEEN :modifiedSeedDate AND :modifiedHarvestDate
     * 2. r.harvestDate BETWEEN :modifiedSeedDate AND :modifiedHarvestDate
     * 3. :modifiedSeedDate BETWEEN r.seedDate AND r.harvestDate
     * 4. :modifiedHarvestDate BETWEEN r.seedDate AND r.harvestDate
     * 
     * Con la primera condicion se comprueba si la fecha de siembra
     * de un registro de plantacion existente esta dentro del rango
     * de fechas modificadas de un registro de plantacion.
     * 
     * Con la segunda condicion se comprueba si la fecha de cosecha
     * de un registro de plantacion existente esta dentro del rango
     * de fechas modificadas de un registro de plantacion.
     * 
     * Con la tercera condicion se comprueba si la fecha de siembra
     * modificada de un registro de plantacion esta dentro del rango
     * de fechas de un registro de plantacion existente.
     * 
     * Con la cuarta condicion se comprueba si la fecha de cosecha
     * modificada de un registro de plantacion esta dentro del rango
     * de fechas de un registro de plantacion existente.
     * 
     * La condicion r.id != modifiedPlantingRecordId excluye el registro
     * de plantacion modificado del conjunto de registros utilizados
     * para verificar si su fecha de siembra y/o cosecha se superpone
     * con los rangos de fechas de los demas registros. Si no se
     * aplica esta condicion, podria haber una superposicion con el
     * mismo registro, lo cual no es el objetivo de la verificacion.
     */
    Query query = entityManager.createQuery("SELECT r FROM PlantingRecord r WHERE r.parcel.user.id = :userId AND r.parcel.id = :parcelId AND r.id != :modifiedPlantingRecordId AND ((r.seedDate BETWEEN :modifiedSeedDate AND :modifiedHarvestDate) OR (r.harvestDate BETWEEN :modifiedSeedDate AND :modifiedHarvestDate) OR (:modifiedSeedDate BETWEEN r.seedDate AND r.harvestDate) OR (:modifiedHarvestDate BETWEEN r.seedDate AND r.harvestDate))");
    query.setParameter("userId", userId);
    query.setParameter("parcelId", parcelId);
    query.setParameter("modifiedPlantingRecordId", modifiedPlantingRecordId);
    query.setParameter("modifiedSeedDate", modifiedSeedDate);
    query.setParameter("modifiedHarvestDate", modifiedHarvestDate);
    query.setMaxResults(1);

    return !query.getResultList().isEmpty();
  }

  /**
   * Retorna true si y solo si un registro de plantacion es modificable.
   * 
   * Hay que tener en cuenta que este metodo debe ser invocado
   * luego de invocar al metodo checkExistence de esta clase,
   * ya que si no se hace esto puede ocurrir la excepcion
   * NoResultException, la cual, ocurre cuando se invoca el
   * metodo getSingleResult de la clase Query para buscar
   * un dato inexistente en la base de datos subyacente.
   * 
   * @param id
   * @return true si un registro de plantacion es modificable,
   * false en caso contrario
   */
  public boolean isModifiable(int id) {
    return find(id).getModifiable();
  }

  /**
   * @param id
   * @return referencia a un objeto de tipo PlantingRecord que
   * representa el registro de plantacion finalizado de una parcela
   * en caso de encontrarse en la base de datos subyacente el
   * registro de plantacion finalizado correspondiente al ID dado,
   * en caso contrario null
   */
  public PlantingRecord findByFinishedStatus(int id) {
    Query query = getEntityManager().createQuery("SELECT r FROM PlantingRecord r JOIN r.status s WHERE (r.id = :givenId AND s.name = 'Finalizado')");
    query.setParameter("givenId", id);

    PlantingRecord finishedPlantingRecord = null;

    try {
      finishedPlantingRecord = (PlantingRecord) query.getSingleResult();
    } catch(NoResultException e) {
      e.printStackTrace();
    }

    return finishedPlantingRecord;
  }

  /**
   * Comprueba la existencia de un registro de plantacion finalizado
   * en la base de datos subyacente. Retorna true si y solo si existe
   * el registro de plantacion finalizado con el ID dado.
   * 
   * @param id
   * @return true si el registro de plantacion finalizado con el
   * ID dado existe en la base de datos subyacente, en caso contrario
   * false
   */
  public boolean checkFinishedStatus(int id) {
    return (findByFinishedStatus(id) != null);
  }

  /**
   * @param id
   * @return referencia a un objeto de tipo PlantingRecord que
   * representa el registro de plantacion en espera de una parcela
   * en caso de encontrarse en la base de datos subyacente el
   * registro de plantacion en espera correspondiente al ID dado,
   * en caso contrario null
   */
  public PlantingRecord findByWaitingStatus(int id) {
    Query query = getEntityManager().createQuery("SELECT r FROM PlantingRecord r JOIN r.status s WHERE (r.id = :givenId AND s.name = 'En espera')");
    query.setParameter("givenId", id);

    PlantingRecord waitingPlantingRecord = null;

    try {
      waitingPlantingRecord = (PlantingRecord) query.getSingleResult();
    } catch(NoResultException e) {
      e.printStackTrace();
    }

    return waitingPlantingRecord;
  }

  /**
   * Comprueba la existencia de un registro de plantacion en espera
   * en la base de datos subyacente. Retorna true si y solo si existe
   * el registro de plantacion en espera con el ID dado.
   * 
   * @param id
   * @return true si el registro de plantacion en espera con el ID dado
   * existe en la base de datos subyacente, en caso contrario false
   */
  public boolean checkWaitingStatus(int id) {
    return (findByWaitingStatus(id) != null);
  }

  /**
   * @param id
   * @return referencia a un objeto de tipo PlantingRecord que
   * representa el registro de plantacion que tiene el estado
   * muerto, si existe en la base de datos subyacente. En caso
   * contrario, null.
   */
  public PlantingRecord findByDeadStatus(int id) {
    Query query = getEntityManager().createQuery("SELECT r FROM PlantingRecord r JOIN r.status s WHERE (r.id = :givenId AND s.name = 'Muerto')");
    query.setParameter("givenId", id);

    PlantingRecord witheredPlantingRecord = null;

    try {
      witheredPlantingRecord = (PlantingRecord) query.getSingleResult();
    } catch(NoResultException e) {
      e.printStackTrace();
    }

    return witheredPlantingRecord;
  }

  /**
   * Comprueba la existencia de un registro de plantacion muerto
   * en la base de datos subyacente. Retorna true si y solo si
   * existe el registro de plantacion muerto con el ID dado.
   * 
   * @param id
   * @return true si el registro de plantacion muerto con el ID dado
   * existe en la base de datos subyacente, en caso contrario false
   */
  public boolean checkDeadStatus(int id) {
    return (findByDeadStatus(id) != null);
  }

  public Page<PlantingRecord> findAllPagination(int userId, Integer page, Integer cantPerPage, Map<String, String> parameters) throws ParseException {
    SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
    Date date;
    Calendar calendarDate;

    // Genera el WHERE dinámicamente
    StringBuffer where = new StringBuffer(" WHERE 1=1 AND e.parcel.user.id = :userId");

    if (parameters != null) {

      for (String param : parameters.keySet()) {
        Method method;

        try {
          method = PlantingRecord.class.getMethod("get" + capitalize(param));

          if (method == null || parameters.get(param) == null || parameters.get(param).isEmpty()) {
            continue;
          }

          switch (method.getReturnType().getSimpleName()) {
            case "String":
              where.append(" AND UPPER(e.");
              where.append(param);
              where.append(") LIKE UPPER('%");
              where.append(parameters.get(param));
              where.append("%')");
              break;
            case "Parcel":
              where.append(" AND UPPER(e.");
              where.append(param);
              where.append(".name");
              where.append(") LIKE UPPER('%");
              where.append(parameters.get(param));
              where.append("%')");
              break;
            case "Crop":
              where.append(" AND UPPER(e.");
              where.append(param);
              where.append(".name");
              where.append(") LIKE UPPER('%");
              where.append(parameters.get(param));
              where.append("%')");
              break;
            case "Calendar":

              if (param.equals("seedDate")) {
                date = new Date(dateFormatter.parse(parameters.get(param)).getTime());
                calendarDate = UtilDate.toCalendar(date);
                where.append(" AND e.");
                where.append(param);
                where.append(" >= ");
                where.append("'" + UtilDate.convertDateToYyyyMmDdFormat(calendarDate) + "'");
              }

              if (param.equals("harvestDate")) {
                date = new Date(dateFormatter.parse(parameters.get(param)).getTime());
                calendarDate = UtilDate.toCalendar(date);
                where.append(" AND e.");
                where.append(param);
                where.append(" <= ");
                where.append("'" + UtilDate.convertDateToYyyyMmDdFormat(calendarDate) + "'");
              }

              break;
            default:
              where.append(" AND e.");
              where.append(param);
              where.append(" = ");
              where.append(parameters.get(param));
              break;
          }

        } catch (NoSuchMethodException | SecurityException e) {
          e.printStackTrace();
        }

      } // End for

    } // End if

    // Cuenta el total de resultados
    Query countQuery = entityManager.createQuery("SELECT COUNT(e.id) FROM " + PlantingRecord.class.getSimpleName() + " e" + where.toString());
    countQuery.setParameter("userId", userId);

    // Pagina
    Query query = entityManager.createQuery("FROM " + PlantingRecord.class.getSimpleName() + " e" + where.toString() + " ORDER BY e.seedDate");
    query.setMaxResults(cantPerPage);
    query.setFirstResult((page - 1) * cantPerPage);
    query.setParameter("userId", userId);

    Integer count = ((Long) countQuery.getSingleResult()).intValue();
    Integer lastPage = (int) Math.ceil((double) count / (double) cantPerPage);

    // Arma la respuesta
    Page<PlantingRecord> resultPage = new Page<PlantingRecord>(page, count, page > 1 ? page - 1 : page, page < lastPage ? page + 1 : lastPage, lastPage, query.getResultList());
    return resultPage;
  }

  private String capitalize(final String line) {
    return Character.toUpperCase(line.charAt(0)) + line.substring(1);
  }

}
