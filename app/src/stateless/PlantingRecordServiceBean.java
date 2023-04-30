package stateless;

import java.util.Calendar;
import java.util.Calendar;
import java.util.Collection;
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
  private final String FINISHED_STATUS = "Finalizado";

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
      chosenPlantingRecord.setIrrigationWaterNeed(modifiedPlantingRecord.getIrrigationWaterNeed());
      chosenPlantingRecord.setParcel(modifiedPlantingRecord.getParcel());
      chosenPlantingRecord.setCrop(modifiedPlantingRecord.getCrop());
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
    Query query = getEntityManager().createQuery("SELECT r FROM PlantingRecord r JOIN r.parcel p WHERE (r.id = :plantingRecordId AND p.user.id = :userId)");
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
   * Retorna los registros de plantacion de una parcela mediante
   * el nombre de una parcela y el ID del usuario al que pertenece
   * una parcela
   * 
   * @param userId
   * @param givenParcelName
   * @return referencia a un objeto de tipo Collection que
   * contiene los registros de plantacion de la parcela que
   * tiene el nombre dado y que pertenece al usuario con el
   * ID dado
   */
  public Collection<PlantingRecord> findAllByParcelName(int userId, String givenParcelName) {
    Query query = getEntityManager().createQuery("SELECT r FROM PlantingRecord r WHERE (r.parcel.name = :givenParcelName AND r.parcel.user.id = :userId) ORDER BY r.id");
    query.setParameter("userId", userId);
    query.setParameter("givenParcelName", givenParcelName);

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
  public List<PlantingRecord> findAllByPeriod(int parcelId, Calendar dateFrom, Calendar dateUntil) {
    /*
     * Con la primera condicion de las fechas se seleccionan los
     * registros de plantacion finalizados (*) de una parcela
     * que tienen su fecha de siembra mayor o igual a la fecha
     * desde y menor estricto a la fecha hasta o que tienen su
     * fecha de siembra mayor estricto a la fecha desde y menor
     * o igual a la fecha hasta, y que tienen su fecha de
     * cosecha mayor o igual a la fecha hasta. En otras palabras,
     * con la primera condicion de las fechas se seleccionan
     * los registros de plantacion finalizados en los que la
     * fecha desde y la fecha hasta estan dentro del periodo
     * definido por la fecha de siembra y la fecha de cosecha o
     * en los que solo la fecha hasta esta dentro del periodo
     * definido por la fecha de siembra y la fecha de cosecha.
     * 
     * Con la segunda condicion de las fechas se seleccionan los
     * registros de plantacion finalizados (*) de una parcela que
     * tienen su fecha de siembra mayor o igual a la fecha desde
     * y su fecha de cosecha menor o igual a la fecha hasta. En
     * otras palabras, con la segunda condicion de las fechas se
     * seleccionan los registros de plantacion finalizados en los
     * que la fecha de siembra y la fecha de cosecha estan dentro
     * del periodo definido por la fecha desde y la fecha hasta.
     * 
     * Con la tercera condicion de las fechas se seleccionan los
     * registros de plantacion finalizados (*) de una parcela que
     * tienen su fecha de cosecha mayor estricto que la fecha desde
     * y menor o igual que la fecha hasta o que tienen su fecha de
     * cosecha mayor o igual que la fecha desde y menor estricto
     * que la fecha hasta, y que tienen su fecha de siembra menor
     * o igual que la fecha desde. En otras palabras, con la tercera
     * condicion de las fechas se seleccionan los registros de
     * plantacion finalizados en los que la fecha desde y la fecha
     * hasta estan dentro del periodo definido por la fecha de
     * siembra y la fecha de cosecha o en los que solo la fecha
     * desde esta dentro del periodo definido por la fecha de
     * siembra y la fecha de cosecha.
     * 
     * (*) El ID para el estado finalizado de un registro de
     * plantacion es el 1, siempre y cuando no se modifique el
     * orden en el que se ejecutan las instrucciones de insercion
     * del archivo plantingRecordStatusInserts.sql de la ruta
     * app/etc/sql.
     */
    String conditionWhere = "(r.parcel.id = :givenParcelId AND r.status.id = 1 AND "
        + "((((r.seedDate >= :givenDateFrom AND :givenDateUntil > r.seedDate) OR (r.seedDate > :givenDateFrom AND :givenDateUntil >= r.seedDate)) AND :givenDateUntil <= r.harvestDate) OR "
        + "(r.seedDate >= :givenDateFrom AND r.harvestDate <= :givenDateUntil) OR "
        + "(:givenDateFrom >= r.seedDate AND ((:givenDateFrom < r.harvestDate AND :givenDateUntil >= r.harvestDate) OR (:givenDateFrom <= r.harvestDate AND :givenDateUntil > r.harvestDate)))))";

    Query query = getEntityManager()
        .createQuery("SELECT r FROM PlantingRecord r WHERE " + conditionWhere + " ORDER BY r.id");
    query.setParameter("givenParcelId", parcelId);
    query.setParameter("givenDateFrom", dateFrom);
    query.setParameter("givenDateUntil", dateUntil);

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
  public Collection<PlantingRecord> findAllByParcelId(int userId, int parcelId) {
    Query query = getEntityManager().createQuery("SELECT r FROM PlantingRecord r JOIN r.parcel p WHERE (p.id = :givenParcelId AND p.user.id = :givenUserId AND r.status.name = 'Finalizado') ORDER BY r.id");
    query.setParameter("givenUserId", userId);
    query.setParameter("givenParcelId", parcelId);

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
    Query query = getEntityManager().createQuery("SELECT r FROM PlantingRecord r WHERE r.status.name = 'En desarrollo' ORDER BY r.id");
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
   * contiene los registros de plantacion finalziados de una
   * parcela de un usuario que estan en un periodo definido
   * por dos fechas
   */
  public Collection<PlantingRecord> findAllByParcelIdAndPeriod(int userId, int parcelId, Calendar dateFrom, Calendar dateUntil) {
    /*
     * Con esta condicion se seleccionan todos los registros de
     * plantacion finalizados (*) de una parcela que estan entre
     * la fecha desde y la fecha hasta dadas.
     * 
     * Con la primera condicion se selecciona el registro de
     * plantacion finalizado (*) de una parcela que tiene su fecha
     * de siembra estrictamente menor (esta antes) que la fecha
     * desde (1), y su fecha de cosecha mayor o igual que la fecha
     * desde (1) y menor o igual que la fecha hasta (2). Es decir,
     * se selecciona el registro de plantacion finalizado de una
     * parcela que tiene unicamente su fecha de cosecha dentro
     * del periodo que va desde la fecha desde (1) a la fecha hasta
     * (2) dadas.
     * 
     * Con la segunda condicion se seleccionan los registros de
     * plantacion finalizados (*) de una parcela que tienen su
     * fecha de siembra mayor o igual que la fecha desde (1) y
     * su fecha de cosecha menor o igual que la fecha hasta (2).
     * Es decir, se seleccionan los registros de plantacion que
     * tienen su fecha de siembra y su fecha de cosecha dentro
     * del periodo que va desde la fecha desde (1) a la fecha
     * hasta (2).
     * 
     * Con la tercera conidicon se selecciona el registro de
     * plantacion finalizado (*) de una parcela que tiene su
     * fecha de cosecha estrictamente mayor (esta despues) que
     * la fecha hasta (2), y su fecha de siembra mayor o igual
     * que la fecha desde (1) y menor o igual que la fecha hasta
     * (2). Es decir, se selecciona el registro de plantacion
     * finalizado de una parcela que tiene unicamente su fecha
     * de siembra dentro del periodo que va desde la fecha desde
     * (1) a la fecha hasta (2).
     * 
     * (*) El ID para el estado finalizado de un registro de
     * plantacion es el 1, siempre y cuando no se modifique el
     * orden en el que se ejecutan las instrucciones de insercion
     * del archivo plantingRecordStatusInserts.sql de la ruta
     * app/etc/sql.
     * 
     * El motivo por el cual esta condicion esta en este metodo
     * es que se la usa para obtener los siguientes datos de
     * una parcela:
     * - el cultivo que mas veces se planto,
     * - el cultivo que menos veces se planto,
     * - el cultivo plantado con el mayor ciclo de vida,
     * - el cultivo plantado con el menor ciclo de vida, y
     * - la cantidad de dias en los que una parcela no tuvo
     * ningun cultivo plantado.
     * 
     * Estos datos son parte del informe estadistico de una
     * parcela.
     */
    String dateCondition = "(:givenDateFrom > r.seedDate AND :givenDateFrom <= r.harvestDate AND r.harvestDate <= :givenDateUntil) OR "
        + "(r.seedDate >= :givenDateFrom AND r.harvestDate <= :givenDateUntil) OR "
        + "(:givenDateUntil < r.harvestDate AND :givenDateFrom <= r.seedDate AND r.seedDate <= :givenDateUntil)";

    String conditionWhere = "(p.id = :givenParcelId AND p.user.id = :givenUserId AND r.status.name = 'Finalizado' AND (" + dateCondition + "))";

    /*
     * Selecciona los registros de plantacion finalizados
     * de una parcela de un usuario que estan en un periodo
     * definido por dos fechas
     */
    Query query = getEntityManager().createQuery("SELECT r FROM PlantingRecord r JOIN r.parcel p WHERE " + conditionWhere + " ORDER BY r.id");
    query.setParameter("givenUserId", userId);
    query.setParameter("givenParcelId", parcelId);
    query.setParameter("givenDateFrom", dateFrom);
    query.setParameter("givenDateUntil", dateUntil);

    return (Collection) query.getResultList();
  }

  /**
   * Retorna el registro de plantacion en desarrollo de una
   * parcela, si existe.
   * 
   * @param givenParcel
   * @return referencia a un objeto de tipo PlantingRecord que
   * representa un registro de plantacion en el estado "En
   * desarrollo" de una parcela, si existe dicho registro. En
   * caso contrario, retornan null.
   */
  public PlantingRecord findInDevelopment(Parcel givenParcel) {
    Query query = getEntityManager().createQuery("SELECT r FROM PlantingRecord r JOIN r.parcel p JOIN r.status s WHERE (s.name = 'En desarrollo' AND p = :parcel)");
    query.setParameter("parcel", givenParcel);

    PlantingRecord plantingRecord = null;

    try {
      plantingRecord = (PlantingRecord) query.getSingleResult();
    } catch(NoResultException e) {
      e.printStackTrace();
    }

    return plantingRecord;
  }

  /**
   * Retorna el ultimo registro de plantacion finalizado de
   * una parcela, si existe.
   * 
   * @param givenParcel
   * @return referencia a un objeto de tipo PlantingRecord que
   * representa el ultimo registro de plantacion en el estado
   * "Finalizado" de una parcela, si existe dicho registro.
   * En caso contrario, retorna null.
   */
  public PlantingRecord findLastFinished(Parcel givenParcel) {
    Query query = getEntityManager().createQuery("SELECT r FROM PlantingRecord r WHERE r.id = (SELECT MAX(r.id) FROM PlantingRecord r JOIN r.parcel p JOIN r.status s WHERE (s.name = 'Finalizado' AND p = :parcel))");
    query.setParameter("parcel", givenParcel);

    PlantingRecord plantingRecord = null;

    try {
      plantingRecord = (PlantingRecord) query.getSingleResult();
    } catch(NoResultException e) {
      e.printStackTrace();
    }

    return plantingRecord;
  }

  /**
   * Retorna true si y solo si una parcela tiene un ultimo
   * registro de plantacion finalizado
   * 
   * @param givenParcel
   * @return true si una parcela tiene un ultimo registro de
   * plantacion finalizado, en caso contrario false
   */
  public boolean hasLastFinished(Parcel givenParcel) {
    return (findLastFinished(givenParcel) != null);
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
  public boolean checkOneInDevelopment(Parcel givenParcel) {
    return (findInDevelopment(givenParcel) != null);
  }

  /**
   * Retorna true si y solo si una parcela de un usuario tiene
   * registros de plantacion finalizados
   * 
   * @param userId
   * @param parcelId
   * @return true si una parcela de un usuario tiene registros
   * de plantacion finalizados, en caso contrario false
   */
  public boolean hasFinishedPlantingRecords(int userId, int parcelId) {
    return !findAllByParcelId(userId, parcelId).isEmpty();
  }

  /**
   * Retorna true si y solo si una parcela de un usuario tiene
   * registros de plantacion finalizados en un periodo definido
   * por dos fechas
   * 
   * @param userId
   * @param parcelId
   * @param dateFrom
   * @param dateUntil
   * @return true si una parcela de un usuario tiene registros
   * de plantacion finalizados en un periodo definido por dos
   * fechas, en caso contrario false
   */
  public boolean hasFinishedPlantingRecords(int userId, int parcelId, Calendar dateFrom, Calendar dateUntil) {
    return !findAllByParcelIdAndPeriod(userId, parcelId, dateFrom, dateUntil).isEmpty();
  }

  /**
   * Retorna true si y solo si una parcela tiene registros de
   * plantacion
   * 
   * @return true si una parcela tiene registros de plantacion,
   * false en caso contrario
   */
  public boolean hasPlantingRecords(Parcel givenParcel) {
    return !findAll(givenParcel).isEmpty();
  }

  /**
   * Retorna true si y solo si la primera fecha es mayor o
   * igual a la segunda fecha
   * 
   * @param firstDate
   * @param secondDate
   * @return true si la primera fecha es mayor o igual a
   * la segunda fecha, false en caso contrario
   */
  public boolean checkDateOverlap(Calendar firstDate, Calendar secondDate) {
    /*
     * Si el resultado de esta comparacion es mayor o igual
     * a cero, significa que la primera fecha es mayor a
     * la segunda fecha o que es igual a ella. Si se da uno
     * de estos casos, las fechas estan superpuestas, por lo,
     * tanto, se retorna true.
     */
    if (firstDate.compareTo(secondDate) >= 0) {
      return true;
    }

    return false;
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
   * de plantacion es estrictamente mayor (posterior) que la fecha actual,
   * la cual, esta contenida en la referencia al objeto de tipo
   * Calendar devuelta por el metodo getInstance() de la clase clase
   * Calendar (ver documentacion de esta clase para mas informacion).
   * 
   * @param givenPlantingRecord
   * @return true si la fecha de siembra del objeto de tipo PlantingRecord
   * referenciado por la referencia contenida en la variable de tipo por
   * referencia givenPlantingRecord de tipo PlantingRecord, es mayor
   * estricta (posterior) a la fecha actual, false en caso contrario
   */
  public boolean isFromFuture(PlantingRecord givenPlantingRecord) {
    return givenPlantingRecord.getSeedDate().after(Calendar.getInstance());
  }

  /**
   * @param parcelId
   * @param dateFrom
   * @param dateUntil
   * @return referencia a un objeto de tipo Collection que contiene
   * referencias a objetos de tipo String que contienen los nombres
   * de los cultivos que mas veces fueron plantados de los cultivos
   * plantados en una parcela durante un periodo dado por dos fechas.
   * En el caso en el que no existen tales cultivos, retorna una
   * referencia a un objeto de tipo Collection vacio, es decir, que
   * no tiene ninguna referencia a un objeto de tipo String.
   */
  private Collection<String> searchMostPlantedCrop(int parcelId, Calendar dateFrom, Calendar dateUntil) {
    /*
     * Cuenta la cantidad de veces que fue plantado cada uno de
     * los cultivos que se plantaron en una parcela en un periodo
     * dado por dos fechas, y selecciona la cantidad mas grande.
     * 
     * Hay que tener en cuenta que puede haber mas de un cultivo
     * que haya sido plantado en mayor medida y la misma cantidad
     * de veces en una parcela durante un periodo dado por dos fechas.
     * Por lo tanto, la consulta (queryString) correspondiente a esta
     * condicion puede retornar mas de un nombre de cultivo como
     * los cultivos que mas veces fueron plantados de los cultivos
     * plantados en una parcela en un periodo dado por dos fechas.
     */
    String conditionHaving = "(SELECT MAX(SUBQUERY.AMOUNT_CROP) FROM (SELECT FK_CROP, COUNT(FK_CROP) AS AMOUNT_CROP FROM PLANTING_RECORD "
        + "WHERE (((?1 > SEED_DATE AND ?1 <= HARVEST_DATE AND HARVEST_DATE <= ?2) OR "
        + "(SEED_DATE >= ?1 AND HARVEST_DATE <= ?2) OR "
        + "(?2 < HARVEST_DATE AND ?1 <= SEED_DATE AND SEED_DATE <= ?2)) AND FK_PARCEL = ?3 AND FK_STATUS = 1) GROUP BY FK_CROP) AS SUBQUERY))";

    /*
     * Con esta condicion se seleccionan todos los registros de
     * plantacion finalizados (*) de una parcela que estan entre
     * la fecha desde y la fecha hasta dadas.
     * 
     * Con la primera condicion se selecciona el registro de
     * plantacion finalizado (*) de una parcela que tiene su fecha
     * de siembra estrictamente menor (esta antes) que la fecha
     * desde (1), y su fecha de cosecha mayor o igual que la fecha
     * desde (1) y menor o igual que la fecha hasta (2). Es decir,
     * se selecciona el registro de plantacion finalizado de una
     * parcela que tiene unicamente su fecha de cosecha dentro
     * del periodo que va desde la fecha desde (1) a la fecha hasta
     * (2) dadas.
     * 
     * Con la segunda condicion se seleccionan los registros de
     * plantacion finalizados (*) de una parcela que tienen su
     * fecha de siembra mayor o igual que la fecha desde (1) y
     * su fecha de cosecha menor o igual que la fecha hasta (2).
     * Es decir, se seleccionan los registros de plantacion que
     * tienen su fecha de siembra y su fecha de cosecha dentro
     * del periodo que va desde la fecha desde (1) a la fecha
     * hasta (2).
     * 
     * Con la tercera conidicon se selecciona el registro de
     * plantacion finalizado (*) de una parcela que tiene su
     * fecha de cosecha estrictamente mayor (esta despues) que
     * la fecha hasta (2), y su fecha de siembra mayor o igual
     * que la fecha desde (1) y menor o igual que la fecha hasta
     * (2). Es decir, se selecciona el registro de plantacion
     * finalizado de una parcela que tiene unicamente su fecha
     * de siembra dentro del periodo que va desde la fecha desde
     * (1) a la fecha hasta (2).
     * 
     * (*) El ID para el estado finalizado de un registro de
     * plantacion es el 1, siempre y cuando no se modifique el
     * orden en el que se ejecutan las instrucciones de insercion
     * del archivo plantingRecordStatusInserts.sql de la ruta
     * app/etc/sql.
     */
    String conditionWhere = "((?1 > SEED_DATE AND ?1 <= HARVEST_DATE AND HARVEST_DATE <= ?2) OR "
        + "(SEED_DATE >= ?1 AND HARVEST_DATE <= ?2) OR "
        + "(?2 < HARVEST_DATE AND ?1 <= SEED_DATE AND SEED_DATE <= ?2)) AND FK_PARCEL = ?3 AND FK_STATUS = 1 ";

    /*
     * Selecciona el ID del cultivo que mas veces fue plantado de
     * los cultivos plantados en una parcela durante un periodo
     * dado por dos fechas.
     * 
     * Hay que tener en cuenta que puede haber mas de un cultivo que
     * haya sido plantado en mayor medida y la misma cantidad de veces
     * en una parcela durante un periodo dado por dos fechas. Por lo
     * tanto, esta subconsulta puede seleccionar el ID de mas de un
     * cultivo como los IDs de los cultivos que mas veces fueron
     * plantados de los cultivos plantados en una parcela durante un
     * periodo dado por dos fechas.
     */
    String subQuery = "(SELECT FK_CROP FROM PLANTING_RECORD WHERE " + conditionWhere
        + "GROUP BY FK_CROP HAVING COUNT(FK_CROP) = " + conditionHaving;

    /*
     * Selecciona el nombre del cultivo que mas veces fue plantado
     * de los cultivos plantados en una parcela durante el periodo
     * dado por dos fechas.
     * 
     * Esta consulta SQL opera unicamente con los registros de
     * plantacion de una parcela que estan en el estado "Finalizado".
     * Por lo tanto, selecciona el nombre del cultivo que mas veces
     * fue plantado de los cultivos plantados en una parcela durante
     * el periodo dado por dos fechas haciendo uso unicamente de los
     * registros de plantacion finalizados de una parcela.
     * 
     * Hay que tener en cuenta que puede haber mas de un cultivo que
     * haya sido plantado en mayor medida y la misma cantidad de veces
     * en una parcela durante un periodo dado por dos fechas. Por lo
     * tanto, esta consulta puede seleccionar el nombre de mas de un
     * cultivo como los nombres de los cultivos que mas veces fueron
     * plantados en una parcela durante un periodo dado por dos fechas.
     */
    String queryString = "SELECT NAME FROM CROP WHERE ID IN " + subQuery;

    Query query = getEntityManager().createNativeQuery(queryString);
    query.setParameter(1, dateFrom);
    query.setParameter(2, dateUntil);
    query.setParameter(3, parcelId);

    Collection<String> cropNames = null;

    try {
      cropNames = (Collection) query.getResultList();
    } catch (NoResultException e) {
      e.printStackTrace();
    }

    return cropNames;
  }

  /**
   * Retorna el nombre del cultivo que mas veces fue plantado de los
   * cultivos plantados en una parcela durante un periodo dado por
   * dos fechas si y solo si existe tal cultivo
   * 
   * @param parcelId
   * @param dateFrom
   * @param dateUntil
   * @return referencia a un objeto de tipo String que contiene
   * el nombre del cultivo que mas veces fue plantado de los
   * cultivos plantados en una parcela durante un periodo dado
   * por dos fechas, si existe dicho cultivo, en caso contrario
   * retorna la referencia a un objeto de tipo String que contiene
   * la cadena "Cultivo inexistente"
   */
  public String findMostPlantedCrop(int parcelId, Calendar dateFrom, Calendar dateUntil) {
    Collection<String> cropNames = searchMostPlantedCrop(parcelId, dateFrom, dateUntil);

    /*
     * Si la coleccion devuelta por el metodo searchMostPlantedCrop,
     * esta vacia o su tamaño es mayor a 1 significa que no se encontro
     * el cultivo que mas veces fue plantado o que existe mas de un cultivo
     * plantado en mayor medida de los cultivos plantados en una parcela
     * durante un periodo dado por dos fechas.
     * 
     * En ambos casos se retorna la cadena "Cultivo no existente". En
     * el segundo caso se retorna dicha cadena porque el cultivo que
     * mas veces fue plantado de los cultivos plantados en una parcela
     * durante un periodo dado por dos fechas es uno solo.
     */
    if (cropNames.isEmpty() || cropNames.size() > 1) {
      return NON_EXISTENT_CROP;
    }

    return (String) cropNames.toArray()[0];
  }

  /**
   * @param parcelId
   * @param dateFrom
   * @param dateUntil
   * @return referencia a un objeto de tipo Collection que contiene
   * referencias a objetos de tipo String que contienen los nombres
   * de los cultivos que menos veces fueron plantados de los cultivos
   * plantados en una parcela durante un periodo dado por dos fechas.
   * En el caso en el que no existen tales cultivos, retorna una
   * referencia a un objeto de tipo Collection vacio, es decir, que
   * no tiene ninguna referencia a un objeto de tipo String.
   */
  private Collection<String> searchLeastPlantedCrop(int parcelId, Calendar dateFrom, Calendar dateUntil) {
    /*
     * Cuenta la cantidad de veces que fue plantado cada uno de
     * los cultivos que se plantaron en una parcela en un periodo
     * dado por dos fechas, y selecciona la cantidad mas pequeña.
     * 
     * Hay que tener en cuenta que puede haber mas de un cultivo
     * que haya sido plantado en menor medida y la misma cantidad
     * de veces en una parcela durante un periodo dado por dos fechas.
     * Por lo tanto, la consulta (queryString) correspondiente a esta
     * condicion puede retornar mas de un nombre de cultivo como
     * los cultivos que menos veces fueron plantados de los cultivos
     * plantados en una parcela en un periodo dado por dos fechas.
     */
    String conditionHaving = "(SELECT MIN(SUBQUERY.AMOUNT_CROP) FROM (SELECT FK_CROP, COUNT(FK_CROP) AS AMOUNT_CROP FROM PLANTING_RECORD "
        + "WHERE (((?1 > SEED_DATE AND ?1 <= HARVEST_DATE AND HARVEST_DATE <= ?2) OR "
        + "(SEED_DATE >= ?1 AND HARVEST_DATE <= ?2) OR "
        + "(?2 < HARVEST_DATE AND ?1 <= SEED_DATE AND SEED_DATE <= ?2)) AND FK_PARCEL = ?3 AND FK_STATUS = 1) GROUP BY FK_CROP) AS SUBQUERY))";

    /*
     * Con esta condicion se seleccionan todos los registros de
     * plantacion finalizados (*) de una parcela que estan entre
     * la fecha desde y la fecha hasta dadas.
     * 
     * Con la primera condicion se selecciona el registro de
     * plantacion finalizado (*) de una parcela que tiene su fecha
     * de siembra estrictamente menor (esta antes) que la fecha
     * desde (1), y su fecha de cosecha mayor o igual que la fecha
     * desde (1) y menor o igual que la fecha hasta (2). Es decir,
     * se selecciona el registro de plantacion finalizado de una
     * parcela que tiene unicamente su fecha de cosecha dentro
     * del periodo que va desde la fecha desde (1) a la fecha hasta
     * (2) dadas.
     * 
     * Con la segunda condicion se seleccionan los registros de
     * plantacion finalizados (*) de una parcela que tienen su
     * fecha de siembra mayor o igual que la fecha desde (1) y
     * su fecha de cosecha menor o igual que la fecha hasta (2).
     * Es decir, se seleccionan los registros de plantacion que
     * tienen su fecha de siembra y su fecha de cosecha dentro
     * del periodo que va desde la fecha desde (1) a la fecha
     * hasta (2).
     * 
     * Con la tercera conidicon se selecciona el registro de
     * plantacion finalizado (*) de una parcela que tiene su
     * fecha de cosecha estrictamente mayor (esta despues) que
     * la fecha hasta (2), y su fecha de siembra mayor o igual
     * que la fecha desde (1) y menor o igual que la fecha hasta
     * (2). Es decir, se selecciona el registro de plantacion
     * finalizado de una parcela que tiene unicamente su fecha
     * de siembra dentro del periodo que va desde la fecha desde
     * (1) a la fecha hasta (2).
     * 
     * (*) El ID para el estado finalizado de un registro de
     * plantacion es el 1, siempre y cuando no se modifique el
     * orden en el que se ejecutan las instrucciones de insercion
     * del archivo plantingRecordStatusInserts.sql de la ruta
     * app/etc/sql.
     */
    String conditionWhere = "((?1 > SEED_DATE AND ?1 <= HARVEST_DATE AND HARVEST_DATE <= ?2) OR "
        + "(SEED_DATE >= ?1 AND HARVEST_DATE <= ?2) OR "
        + "(?2 < HARVEST_DATE AND ?1 <= SEED_DATE AND SEED_DATE <= ?2)) AND FK_PARCEL = ?3 AND FK_STATUS = 1 ";

    /*
     * Selecciona el ID del cultivo que menos veces fue plantado de
     * los cultivos plantados en una parcela durante un periodo
     * dado por dos fechas.
     * 
     * Hay que tener en cuenta que puede haber mas de un cultivo que
     * haya sido plantado en menor medida y la misma cantidad de veces
     * en una parcela durante un periodo dado por dos fechas. Por lo
     * tanto, esta subconsulta puede seleccionar el ID de mas de un
     * cultivo como los IDs de los cultivos que menos veces fueron
     * plantados de los cultivos plantados en una parcela durante un
     * periodo dado por dos fechas.
     */
    String subQuery = "(SELECT FK_CROP FROM PLANTING_RECORD WHERE " + conditionWhere
        + "GROUP BY FK_CROP HAVING COUNT(FK_CROP) = " + conditionHaving;

    /*
     * Selecciona el nombre del cultivo que menos veces fue plantado
     * de los cultivos plantados en una parcela durante el periodo
     * dado por dos fechas.
     * 
     * Esta consulta SQL opera unicamente con los registros de
     * plantacion de una parcela que estan en el estado "Finalizado".
     * Por lo tanto, selecciona el nombre del cultivo que menos veces
     * fue plantado de los cultivos plantados en una parcela durante
     * el periodo dado por dos fechas haciendo uso unicamente de los
     * registros de plantacion finalizados de una parcela.
     * 
     * Hay que tener en cuenta que puede haber mas de un cultivo que
     * haya sido plantado en menor medida y la misma cantidad de veces
     * en una parcela durante un periodo dado por dos fechas. Por lo
     * tanto, esta consulta puede seleccionar el nombre de mas de un
     * cultivo como los nombres de los cultivos que menos veces fueron
     * plantados en una parcela durante un periodo dado por dos fechas.
     */
    String queryString = "SELECT NAME FROM CROP WHERE ID IN " + subQuery;

    Query query = getEntityManager().createNativeQuery(queryString);
    query.setParameter(1, dateFrom);
    query.setParameter(2, dateUntil);
    query.setParameter(3, parcelId);

    Collection<String> cropNames = null;

    try {
      cropNames = (Collection) query.getResultList();
    } catch (NoResultException e) {
      e.printStackTrace();
    }

    return cropNames;
  }

  /**
   * Retorna el nombre del cultivo que menos veces fue plantado de los
   * cultivos plantados en una parcela durante un periodo dado por
   * dos fechas si y solo si existe tal cultivo
   * 
   * @param parcelId
   * @param dateFrom
   * @param dateUntil
   * @return referencia a un objeto de tipo String que contiene
   * el nombre del cultivo que menos veces fue plantado de los
   * cultivos plantados en una parcela durante un periodo dado
   * por dos fechas, si existe dicho cultivo, en caso contrario
   * retorna la referencia a un objeto de tipo String que contiene
   * la cadena "Cultivo inexistente"
   */
  public String findLeastPlantedCrop(int parcelId, Calendar dateFrom, Calendar dateUntil) {
    Collection<String> cropNames = searchLeastPlantedCrop(parcelId, dateFrom, dateUntil);

    /*
     * Si la coleccion devuelta por el metodo searchLeastPlantedCrop,
     * esta vacia o su tamaño es mayor a 1 significa que no se encontro
     * el cultivo que menos veces fue plantado o que existe mas de un cultivo
     * plantado en menor medida de los cultivos plantados en una parcela
     * durante un periodo dado por dos fechas.
     * 
     * En ambos casos se retorna la cadena "Cultivo no existente". En
     * el segundo caso se retorna dicha cadena porque el cultivo que
     * menos veces fue plantado de los cultivos plantados en una parcela
     * durante un periodo dado por dos fechas, es uno solo.
     */
    if (cropNames.isEmpty() || cropNames.size() > 1) {
      return NON_EXISTENT_CROP;
    }

    return (String) cropNames.toArray()[0];
  }

  /**
   * @param parcelId
   * @param dateFrom
   * @param dateUntil
   * @return referencia a un objeto de tipo Collection que contiene
   * referencias a objetos de tipo String que contienen los nombres
   * de los cultivos plantados en una parcela durante un periodo dado
   * por dos fechas, que tienen el mayor ciclo de vida.
   * En el caso en el que no existen tales cultivos, retorna una
   * referencia a un objeto de tipo Collection vacio, es decir, que
   * no tiene ninguna referencia a un objeto de tipo String.
   */
  private Collection<String> searchCropWithLongestLifeCycle(int parcelId, Calendar dateFrom, Calendar dateUntil) {
    /*
     * Selecciona el ciclo de vida mas grande de los cultivos
     * plantados y finalizados en una parcela durante un
     * periodo dado por dos fechas.
     * 
     * La manera en la que realiza esto es mediante los
     * registros de plantacion finalizados de una parcela
     * que estan comprendidos en un periodo dado por dos
     * fechas.
     */
    String subQuery = "(SELECT MAX(LIFE_CYCLE) FROM PLANTING_RECORD JOIN CROP ON PLANTING_RECORD.FK_CROP = CROP.ID WHERE "
        + "(((?1 > SEED_DATE AND ?1 <= HARVEST_DATE AND HARVEST_DATE <= ?2) OR "
        + "(SEED_DATE >= ?1 AND HARVEST_DATE <= ?2) OR "
        + "(?2 < HARVEST_DATE AND ?1 <= SEED_DATE AND SEED_DATE <= ?2)) AND "
        + "FK_PARCEL = ?3 AND FK_STATUS = 1))";

    /*
     * Con las condiciones de las fechas se seleccionan todos los
     * registros de plantacion finalizados (*) de una parcela que
     * estan entre la fecha desde y la fecha hasta dadas.
     * 
     * Con la primera condicion se selecciona el registro de
     * plantacion finalizado (*) de una parcela que tiene su fecha
     * de siembra estrictamente menor (esta antes) que la fecha
     * desde (1), y su fecha de cosecha mayor o igual que la fecha
     * desde (1) y menor o igual que la fecha hasta (2). Es decir,
     * se selecciona el registro de plantacion finalizado de una
     * parcela que tiene unicamente su fecha de cosecha dentro
     * del periodo que va desde la fecha desde (1) a la fecha hasta
     * (2) dadas.
     * 
     * Con la segunda condicion se seleccionan los registros de
     * plantacion finalizados (*) de una parcela que tienen su
     * fecha de siembra mayor o igual que la fecha desde (1) y
     * su fecha de cosecha menor o igual que la fecha hasta (2).
     * Es decir, se seleccionan los registros de plantacion que
     * tienen su fecha de siembra y su fecha de cosecha dentro
     * del periodo que va desde la fecha desde (1) a la fecha
     * hasta (2).
     * 
     * Con la tercera conidicon se selecciona el registro de
     * plantacion finalizado (*) de una parcela que tiene su
     * fecha de cosecha estrictamente mayor (esta despues) que
     * la fecha hasta (2), y su fecha de siembra mayor o igual
     * que la fecha desde (1) y menor o igual que la fecha hasta
     * (2). Es decir, se selecciona el registro de plantacion
     * finalizado de una parcela que tiene unicamente su fecha
     * de siembra dentro del periodo que va desde la fecha desde
     * (1) a la fecha hasta (2).
     * 
     * (*) El ID para el estado finalizado de un registro de
     * plantacion es el 1, siempre y cuando no se modifique el
     * orden en el que se ejecutan las instrucciones de insercion
     * del archivo plantingRecordStatusInserts.sql de la ruta
     * app/etc/sql.
     * 
     * Sin las condiciones de las fechas, la consulta (queryString)
     * correspondiente a estas seleccionaria el nombre de un
     * cultivo que tiene un ciclo de vida igual al obtenido por
     * la subconsulta, pero perteneciente a registros de plantacion
     * que no estan dentro del periodo dado por dos fechas, si
     * existen dichos registros. En consecuencia, erroneamente se
     * entenderia esto como que hubo mas de un cultivo plantado y
     * finalizado con el mayor ciclo de vida en una parcela durante
     * un periodo dado por dos fechas.
     */
    String conditionWhere = "((?1 > SEED_DATE AND ?1 <= HARVEST_DATE AND HARVEST_DATE <= ?2) OR "
        + "(SEED_DATE >= ?1 AND HARVEST_DATE <= ?2) OR  "
        + "(?2 < HARVEST_DATE AND ?1 <= SEED_DATE AND SEED_DATE <= ?2)) AND "
        + "FK_PARCEL = ?3 AND FK_STATUS = 1 AND LIFE_CYCLE = " + subQuery;

    /*
     * Selecciona el nombre del cultivo que tiene el mayor ciclo de
     * vida de los cultivos plantados en una parcela durante el periodo
     * dado por dos fechas.
     * 
     * Esta consulta SQL opera unicamente con los registros de
     * plantacion de una parcela que estan en el estado "Finalizado".
     * Por lo tanto, selecciona el nombre del cultivo que tiene el
     * mayor ciclo de vida de los cultivos plantados en una parcela
     * durante el periodo dado por dos fechas haciendo uso unicamente
     * de los registros de plantacion finalizados de una parcela.
     * 
     * Hay que tener en cuenta que puede haber mas de un cultivo que
     * haya sido plantado en una parcela durante un periodo dado por
     * dos fechas y que tenga un ciclo de vida igual al ciclo de vida
     * mas grande. Por lo tanto, esta consulta puede seleccionar el
     * nombre de mas de un cultivo como los nombres de los cultivos
     * que tienen el mayor ciclo de vida de los cultivos plantados
     * en una parcela durante un periodo dado por dos fechas.
     */
    String queryString = "SELECT DISTINCT NAME FROM PLANTING_RECORD JOIN CROP ON PLANTING_RECORD.FK_CROP = CROP.ID WHERE "
        + conditionWhere;

    Query query = getEntityManager().createNativeQuery(queryString);
    query.setParameter(1, dateFrom);
    query.setParameter(2, dateUntil);
    query.setParameter(3, parcelId);

    Collection<String> cropNames = null;

    try {
      cropNames = (Collection) query.getResultList();
    } catch (NoResultException e) {
      e.printStackTrace();
    }

    return cropNames;
  }

  /**
   * Retorna el nombre del cultivo que tiene el mayor ciclo de vida
   * de los cultivos plantados en una parcela durante un periodo dado
   * por dos fechas si y solo si existe tal cultivo
   * 
   * @param parcelId
   * @param dateFrom
   * @param dateUntil
   * @return referencia a un objeto de tipo String que contiene
   * el nombre del cultivo que tiene el ciclo de vida mas grande
   * de los cultivos plantados en una parcela durante un periodo
   * dado por dos fechas, si existe dicho cultivo, en caso contrario
   * retorna la referencia a un objeto de tipo String que contiene
   * la cadena "Cultivo inexistente"
   */
  public String findCropWithLongestLifeCycle(int parcelId, Calendar dateFrom, Calendar dateUntil) {
    Collection<String> cropNames = searchCropWithLongestLifeCycle(parcelId, dateFrom, dateUntil);

    /*
     * Si la coleccion devuelta por el metodo searchCropWithLongestLifeCycle,
     * esta vacia o su tamaño es mayor a 1 significa que no se encontro
     * el cultivo que tiene el mayor ciclo de vida de los cultivos plantados
     * en una parcela durante un periodo dado por dos fechas.
     * 
     * En ambos casos se retorna la cadena "Cultivo no existente". En
     * el segundo caso se retorna dicha cadena porque el cultivo que
     * tiene el mayor ciclo de vida de los cultivos plantados en una
     * parcela durante un periodo dado por dos fechas, es uno solo.
     */
    if (cropNames.isEmpty() || cropNames.size() > 1) {
      return NON_EXISTENT_CROP;
    }

    return (String) cropNames.toArray()[0];
  }

  /**
   * @param parcelId
   * @param dateFrom
   * @param dateUntil
   * @return referencia a un objeto de tipo Collection que contiene
   * referencias a objetos de tipo String que contienen los nombres
   * de los cultivos plantados en una parcela durante un periodo dado
   * por dos fechas, que tienen el menor ciclo de vida.
   * En el caso en el que no existen tales cultivos, retorna una
   * referencia a un objeto de tipo Collection vacio, es decir, que
   * no tiene ninguna referencia a un objeto de tipo String.
   */
  private Collection<String> searchCropWithShortestLifeCycle(int parcelId, Calendar dateFrom, Calendar dateUntil) {
    /*
     * Selecciona el ciclo de vida mas pequeño de los cultivos
     * plantados y finalizados en una parcela durante un
     * periodo dado por dos fechas.
     * 
     * La manera en la que realiza esto es mediante los
     * registros de plantacion finalizados de una parcela
     * que estan comprendidos en un periodo dado por dos
     * fechas.
     */
    String subQuery = "(SELECT MIN(LIFE_CYCLE) FROM PLANTING_RECORD JOIN CROP ON PLANTING_RECORD.FK_CROP = CROP.ID WHERE "
        + "(((?1 > SEED_DATE AND ?1 <= HARVEST_DATE AND HARVEST_DATE <= ?2) OR "
        + "(SEED_DATE >= ?1 AND HARVEST_DATE <= ?2) OR "
        + "(?2 < HARVEST_DATE AND ?1 <= SEED_DATE AND SEED_DATE <= ?2)) AND "
        + "FK_PARCEL = ?3 AND FK_STATUS = 1))";

    /*
     * Con las condiciones de las fechas se seleccionan todos los
     * registros de plantacion finalizados (*) de una parcela que
     * estan entre la fecha desde y la fecha hasta dadas.
     * 
     * Con la primera condicion se selecciona el registro de
     * plantacion finalizado (*) de una parcela que tiene su fecha
     * de siembra estrictamente menor (esta antes) que la fecha
     * desde (1), y su fecha de cosecha mayor o igual que la fecha
     * desde (1) y menor o igual que la fecha hasta (2). Es decir,
     * se selecciona el registro de plantacion finalizado de una
     * parcela que tiene unicamente su fecha de cosecha dentro
     * del periodo que va desde la fecha desde (1) a la fecha hasta
     * (2) dadas.
     * 
     * Con la segunda condicion se seleccionan los registros de
     * plantacion finalizados (*) de una parcela que tienen su
     * fecha de siembra mayor o igual que la fecha desde (1) y
     * su fecha de cosecha menor o igual que la fecha hasta (2).
     * Es decir, se seleccionan los registros de plantacion que
     * tienen su fecha de siembra y su fecha de cosecha dentro
     * del periodo que va desde la fecha desde (1) a la fecha
     * hasta (2).
     * 
     * Con la tercera conidicon se selecciona el registro de
     * plantacion finalizado (*) de una parcela que tiene su
     * fecha de cosecha estrictamente mayor (esta despues) que
     * la fecha hasta (2), y su fecha de siembra mayor o igual
     * que la fecha desde (1) y menor o igual que la fecha hasta
     * (2). Es decir, se selecciona el registro de plantacion
     * finalizado de una parcela que tiene unicamente su fecha
     * de siembra dentro del periodo que va desde la fecha desde
     * (1) a la fecha hasta (2).
     * 
     * (*) El ID para el estado finalizado de un registro de
     * plantacion es el 1, siempre y cuando no se modifique el
     * orden en el que se ejecutan las instrucciones de insercion
     * del archivo plantingRecordStatusInserts.sql de la ruta
     * app/etc/sql.
     * 
     * Sin las condiciones de las fechas, la consulta (queryString)
     * correspondiente a estas seleccionaria el nombre de un
     * cultivo que tiene un ciclo de vida igual al obtenido por
     * la subconsulta, pero perteneciente a registros de plantacion
     * que no estan dentro del periodo dado por dos fechas, si
     * existen dichos registros. En consecuencia, erroneamente se
     * entenderia esto como que hubo mas de un cultivo plantado y
     * finalizado con el menor ciclo de vida en una parcela durante
     * un periodo dado por dos fechas.
     */
    String conditionWhere = "((?1 > SEED_DATE AND ?1 <= HARVEST_DATE AND HARVEST_DATE <= ?2) OR "
        + "(SEED_DATE >= ?1 AND HARVEST_DATE <= ?2) OR  "
        + "(?2 < HARVEST_DATE AND ?1 <= SEED_DATE AND SEED_DATE <= ?2)) AND "
        + "FK_PARCEL = ?3 AND FK_STATUS = 1 AND LIFE_CYCLE = " + subQuery;

    /*
     * Selecciona el nombre del cultivo que tiene el menor ciclo de
     * vida de los cultivos plantados en una parcela durante el periodo
     * dado por dos fechas.
     * 
     * Esta consulta SQL opera unicamente con los registros de
     * plantacion de una parcela que estan en el estado "Finalizado".
     * Por lo tanto, selecciona el nombre del cultivo que tiene el
     * menor ciclo de vida de los cultivos plantados en una parcela
     * durante el periodo dado por dos fechas haciendo uso unicamente
     * de los registros de plantacion finalizados de una parcela.
     * 
     * Hay que tener en cuenta que puede haber mas de un cultivo que
     * haya sido plantado en una parcela durante un periodo dado por
     * dos fechas y que tenga un ciclo de vida igual al ciclo de vida
     * mas pequeño. Por lo tanto, esta consulta puede seleccionar el
     * nombre de mas de un cultivo como los nombres de los cultivos
     * que tienen el menor ciclo de vida de los cultivos plantados
     * en una parcela durante un periodo dado por dos fechas.
     */
    String queryString = "SELECT DISTINCT NAME FROM PLANTING_RECORD JOIN CROP ON PLANTING_RECORD.FK_CROP = CROP.ID WHERE "
        + conditionWhere;

    Query query = getEntityManager().createNativeQuery(queryString);
    query.setParameter(1, dateFrom);
    query.setParameter(2, dateUntil);
    query.setParameter(3, parcelId);

    Collection<String> cropNames = null;

    try {
      cropNames = (Collection) query.getResultList();
    } catch (NoResultException e) {
      e.printStackTrace();
    }

    return cropNames;
  }

  /**
   * Retorna el nombre del cultivo que tiene el menor ciclo de vida
   * de los cultivos plantados en una parcela durante un periodo dado
   * por dos fechas si y solo si existe tal cultivo
   * 
   * @param parcelId
   * @param dateFrom
   * @param dateUntil
   * @return referencia a un objeto de tipo String que contiene
   * el nombre del cultivo que tiene el ciclo de vida mas pequeño
   * de los cultivos plantados en una parcela durante un periodo
   * dado por dos fechas, si existe dicho cultivo, en caso contrario
   * retorna la referencia a un objeto de tipo String que contiene
   * la cadena "Cultivo inexistente"
   */
  public String findCropWithShortestLifeCycle(int parcelId, Calendar dateFrom, Calendar dateUntil) {
    Collection<String> cropNames = searchCropWithShortestLifeCycle(parcelId, dateFrom, dateUntil);

    /*
     * Si la coleccion devuelta por el metodo searchCropWithShortestLifeCycle,
     * esta vacia o su tamaño es mayor a 1 significa que no se encontro
     * el cultivo que tiene el menor ciclo de vida de los cultivos plantados
     * en una parcela durante un periodo dado por dos fechas.
     * 
     * En ambos casos se retorna la cadena "Cultivo no existente". En
     * el segundo caso se retorna dicha cadena porque el cultivo que
     * tiene el menor ciclo de vida de los cultivos plantados en una
     * parcela durante un periodo dado por dos fechas, es uno solo.
     */
    if (cropNames.isEmpty() || cropNames.size() > 1) {
      return NON_EXISTENT_CROP;
    }

    return (String) cropNames.toArray()[0];
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
    List<PlantingRecord> plantingRecords = findAllByPeriod(parcelId, dateFrom, dateUntil);

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
   * Retorna true si y solo si la fecha de cosecha de un
   * registro de plantacion presuntamente en desarrollo es
   * mayor o igual a la fecha actual. Un registro de
   * plantacion que presuntamente esta en desarrollo y tiene
   * su fecha de cosecha estrictamente menor a la fecha actual,
   * es un registro de plantacion finalizado. En cambio,
   * un registro de plantacion que tiene su fecha de cosecha
   * igual o mayor a la fecha actual es un registro de
   * plantacion en desarrollo.
   * 
   * Este metodo es para el metodo automatico modifyStatus
   * de la clase PlantingRecordManager. El metodo modifyStatus
   * se ocupa de comprobar si la fecha de cosecha de un
   * registro de plantacion presuntamente en desarrollo
   * es estrictamente menor a la fecha actual y en base
   * a esto establece el estado finalizado en el registro.
   * 
   * @param plantingRecord
   * @return true si la fecha de cosecha de un registro de
   * plantacion presuntamente en desarrollo es mayor o igual
   * a la fecha actual, en caso contrario false.
   * Tambien retorna false en el caso en el que el estado
   * de un registro de plantacion sea el estado finalizado.
   */
  public boolean checkDevelopmentStatus(PlantingRecord plantingRecord) {
    /*
     * Si el registro de plantacion dado esta en el estado "Finalizado",
     * se retorna false como indicador de que no esta en el estado "En
     * desarrollo"
     */
    if (plantingRecord.getStatus().getName().equals(FINISHED_STATUS)) {
      return false;
    }

    /*
     * Si la fecha de cosecha de un registro de plantacion presuntamente
     * en desarrollo es mayor o igual a la fecha actual, se retorna true
     * como indicativo de que este registro esta en desarrollo.
     * 
     * El metodo getInstance de la clase Calendar retorna la referencia
     * a un objeto de tipo Calendar que contiene la fecha actual.
     */
    if (UtilDate.compareTo(plantingRecord.getHarvestDate(), Calendar.getInstance()) >= 0) {
      return true;
    }

    /*
     * Si la fecha de cosecha de un registro de plantacion presuntamente
     * en desarrollo es estrictamente menor a la fecha actual, se retorna
     * false como indicativo de que este registro de plantacion NO esta
     * en desarrollo, o en otras palabras, que esta finalizado
     */
    return false;
  }

  /**
   * Establece un estado en un registro de plantacion
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
   * Establece el atributo modifiable de un registro de
   * plantacion en false. Esto se debe hacer para un registro
   * de plantacion finalizado, ya que un registro de
   * plantacion finalizado NO se debe poder modificar.
   * 
   * Este metodo es para el metodo automatico unsetModifiable
   * de la clase PlantingRecordManager.
   * 
   * @param id
   */
  public void unsetModifiable(int id) {
    Query query = entityManager.createQuery("UPDATE PlantingRecord p SET p.modifiable = 0 WHERE p.id = :givenId");
    query.setParameter("givenId", id);
    query.executeUpdate();
  }

  /**
   * Retorna true si y solo si un registro de plantacion tiene
   * el estado finalizado.
   * 
   * Hay que tener en cuenta que este metodo debe ser invocado
   * luego de invocar al metodo checkExistence de esta clase,
   * ya que si no se hace esto puede ocurrir la excepcion
   * NoResultException, la cual, ocurre cuando se invoca el
   * metodo getSingleResult de la clase Query para buscar
   * un dato inexistente en la base de datos subyacente.
   * 
   * @param id
   * @return true si un registro de plantacion es
   * modificable, en caso contrario false
   */
  public boolean isFinished(int id) {
    return find(id).getStatus().getName().equals(FINISHED_STATUS);
  }

  /**
   * Actualiza la necesidad de agua de riego del registro
   * de plantacion de una parcela
   * 
   * @param id
   * @param givenParcel
   * @param irrigationWaterNeed [mm/dia]
   */
  public void updateIrrigationWaterNeed(int id, Parcel givenParcel, String irrigationWaterNeed) {
    Query query = entityManager.createQuery("UPDATE PlantingRecord p SET p.irrigationWaterNeed = :irrigationWaterNeed WHERE (p.id = :givenId AND p.parcel = :givenParcel)");
    query.setParameter("givenId", id);
    query.setParameter("givenParcel", givenParcel);
    query.setParameter("irrigationWaterNeed", irrigationWaterNeed);
    query.executeUpdate();
  }

}
