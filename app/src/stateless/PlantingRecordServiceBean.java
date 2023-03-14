package stateless;

import java.util.Calendar;
import java.util.Calendar;
import java.util.Collection;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import model.Crop;
import model.Parcel;
import model.PlantingRecord;

@Stateless
public class PlantingRecordServiceBean {

  @PersistenceContext(unitName = "swcar")
  protected EntityManager entityManager;
  private final String NON_EXISTENT_CROP = "Cultivo inexistente";

  public void setEntityManager(EntityManager emLocal) {
    entityManager = emLocal;
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
   * su nombre y el ID del usuario al que pertenece
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
   * Retorna true si y solo si una parcela tiene un registro de
   * plantacion inmediatamente anterior al registro de plantacion
   * correspondiente al ID de referencia
   * 
   * @param referencePlantingRecordId
   * @param givenParcel
   * @return true si una parcela tiene un registro de plantacion
   * inmediatamente anterior al registro de plantacion correspondiente
   * al ID dado, false en caso contrario
   */
  public boolean checkPrevious(int referencePlantingRecordId, Parcel givenParcel) {
    return (find(referencePlantingRecordId - 1, givenParcel) != null);
  }

  /**
   * Retorna true si y solo si una parcela tiene un registro de
   * plantacion inmediatamente a continuacion del registro de
   * plantacion correspondiente al ID de referencia
   * 
   * @param referencePlantingRecordId
   * @param givenParcel
   * @return true si una parcela tiene un registro de plantacion
   * inmediatamente a continuacion del registro de plantacion
   * correspondiente al ID dado, false en caso contrario
   */
  public boolean checkNext(int referencePlantingRecordId, Parcel givenParcel) {
    return (find(referencePlantingRecordId + 1, givenParcel) != null);
  }

  /**
   * Retorna true si y solo si una parcela tiene registros de
   * plantacion
   * 
   * @return true si una parcela tiene registros de plantacion,
   * false en caso contrario
   */
  public boolean thereIsPlantingRecords(Parcel givenParcel) {
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

}
