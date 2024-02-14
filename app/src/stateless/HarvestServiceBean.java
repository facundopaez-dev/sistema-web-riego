package stateless;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Collection;
import java.util.Map;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import model.Harvest;
import model.PlantingRecord;
import util.UtilDate;

@Stateless
public class HarvestServiceBean {

  /*
   * Instance variables
   */
  @PersistenceContext(unitName = "swcar")
  private EntityManager entityManager;

  private final String NON_EXISTENT_CROP = "Cultivo inexistente";

  public void setEntityManager(EntityManager localEntityManager) {
    entityManager = localEntityManager;
  }

  public EntityManager getEntityManager() {
    return entityManager;
  }

  public Harvest create(Harvest newHarvest) {
    getEntityManager().persist(newHarvest);
    return newHarvest;
  }

  /**
   * Elimina fisicamente una cosecha mediante su ID
   * 
   * @param id
   * @return referencia a un objeto de tipo Harvest en
   * caso de eliminarse de la base de datos subyacente
   * la cosecha con el ID dado, en caso contrario null
   */
  public Harvest remove(int id) {
    Harvest harvest = find(id);

    if (harvest != null) {
      getEntityManager().remove(harvest);
      return harvest;
    }

    return null;
  }

  /**
   * Elimina fisicamente una cosecha
   * 
   * @param userId
   * @param harvestId
   * @return referencia a un objeto de tipo Harvest si la cosecha
   * a eliminar pertenece al usuario con el ID dado, null en
   * caso contrario
   */
  public Harvest remove(int userId, int harvestId) {
    Harvest harvest = find(userId, harvestId);

    if (harvest != null) {
      getEntityManager().remove(harvest);
      return harvest;
    }

    return null;
  }

  /**
   * @param userId
   * @param harvestId
   * @param modifiedHarvest
   * @return referencia a un objeto de tipo Harvest si se
   * modifica la cosecha correspondiente al ID de usuario
   * y al ID de cosecha
   */
  public Harvest modify(int userId, int harvestId, Harvest modifiedHarvest) {
    Harvest chosenHarvest = find(userId, harvestId);

    if (chosenHarvest != null) {
      chosenHarvest.setDate(modifiedHarvest.getDate());
      chosenHarvest.setHarvestAmount(modifiedHarvest.getHarvestAmount());
      chosenHarvest.setParcel(modifiedHarvest.getParcel());
      chosenHarvest.setCrop(modifiedHarvest.getCrop());
      return chosenHarvest;
    }

    return null;
  }

  public Harvest find(int id) {
    return getEntityManager().find(Harvest.class, id);
  }

  /**
   * Comprueba la existencia de una cosecha en la base de datos
   * subyacente. Retorna true si y solo si existe la cosecha
   * con el ID dado.
   * 
   * @param id
   * @return true si la cosecha con el ID dado existe en la
   * base de datos subyacente, false en caso contrario
   */
  public boolean checkExistence(int id) {
    return (getEntityManager().find(Harvest.class, id) != null);
  }

  /**
   * @param userId
   * @param harvestId
   * @return referencia a un objeto de tipo Harvest si en la
   * base de datos subyacente existe la cosecha correspondiente
   * al ID de usuario y al ID de cosecha. En caso contrario,
   * retorna null.
   */
  public Harvest find(int userId, int harvestId) {
    Query query = entityManager.createQuery("SELECT h FROM Harvest h JOIN h.parcel p WHERE h.id = :harvestId AND p IN (SELECT t FROM User u JOIN u.parcels t WHERE u.id = :userId)");
    query.setParameter("userId", userId);
    query.setParameter("harvestId", harvestId);

    Harvest harvest = null;

    try {
      harvest = (Harvest) query.getSingleResult();
    } catch (NoResultException e) {
      e.printStackTrace();
    }

    return harvest;
  }

  /**
   * Retorna el nombre del cultivo que mayor rendimiento (mayor
   * cantidad de kilogramos cosechados) tuvo de los cultivos
   * cosechados en una parcela durante un periodo definido por
   * dos fechas si y solo si existe tal cultivo
   * 
   * @param parcelId
   * @param dateFrom
   * @param dateUntil
   * @return referencia a un objeto de tipo String que contiene
   * el nombre del cultivo que mayor rendimiento (mayor cantidad
   * de kilogramos cosechados) tuvo de los cultivos cosechados en
   * una parcela durante un periodo definido por dos fechas, si
   * existe dicho cultivo. En caso contrario, retorna la referencia
   * a un objeto de tipo String que contiene la cadena "Cultivo
   * inexistente".
   */
  public String findCropHighestHarvest(int parcelId, Calendar dateFrom, Calendar dateUntil) {
    Collection<String> cropNames = searchCropHighestHarvest(parcelId, dateFrom, dateUntil);

    /*
     * Si la coleccion devuelta por el metodo searchCropHighestHarvest,
     * esta vacia o su tamaño es mayor a 1 significa que no se encontro
     * el cultivo con mayor rendimiento (mayor cantidad de kilogramos
     * cosechados) o que existe mas de un cultivo con mayor rendimiento
     * en una parcela durante un periodo dado por dos fechas.
     * 
     * En ambos casos se retorna la cadena "Cultivo no existente". En
     * el segundo caso se retorna dicha cadena porque el cultivo que
     * mayor rendimiento tuvo en una parcela durante un periodo definido
     * por dos fechas es uno solo.
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
   * de los cultivos que mayor rendimiento (cantidad de kilogramos
   * cosechados) tuvieron en una parcela durante un periodo definido
   * por dos fechas. En el caso en el que no existen tales cultivos,
   * retorna una referencia a un objeto de tipo Collection vacio, es
   * decir, que no tiene ninguna referencia a un objeto de tipo String.
   */
  private Collection<String> searchCropHighestHarvest(int parcelId, Calendar dateFrom, Calendar dateUntil) {
    /*
     * Calcula la cantidad total de los kilogramos cosechados
     * de cada uno de los cultivos cosechados en una parcela
     * durante un periodo definido por dos fechas y selecciona
     * la cantidad maxima. De esta manera, se obtiene la cantidad
     * maxima de kilogramos cosechados de un cultivo sembrado
     * en una parcela durante un periodo definido por dos fechas.
     */
    String queryToCalculateHighestHarvest = "SELECT MAX(TOTALS.TOTAL) FROM "
        + "(SELECT FK_CROP, SUM(HARVEST_AMOUNT) AS TOTAL FROM HARVEST WHERE ?1 <= HARVEST.DATE AND HARVEST.DATE <= ?2 AND "
        + "FK_PARCEL = ?3 GROUP BY FK_CROP) AS TOTALS";

    /*
     * Selecciona el ID del cultivo que tuvo el mayor rendimiento
     * (mayor cantidad de kilogramos cosechados) de los cultivos
     * cosechados en una parcela durante un periodo definido por
     * dos fechas. Hay que tener en cuenta que esta consulta puede
     * retornar mas de un ID de cultivo, ya que puede ocurrir que
     * haya mas de un cultivo con un mayor rendimiento con respecto
     * a los cultivos cosechados en una parcela durante un periodo
     * definido por dos fechas.
     */
    String queryToSelectIdCropHighestHarvest = "SELECT FK_CROP FROM "
        + "HARVEST WHERE ?1 <= HARVEST.DATE AND HARVEST.DATE <= ?2 AND FK_PARCEL = ?3 GROUP BY FK_CROP "
        + "HAVING SUM(HARVEST_AMOUNT) = (" + queryToCalculateHighestHarvest + ")";

    /*
     * Selecciona el nombre del cultivo que tuvo el mayor rendimiento
     * (mayor cantidad de kilgoramos cosechados) de los cultivos
     * cosechados en una parcela durante un periodo definido por dos
     * fechas. Hay que esta consulta puede retornar mas de un nombre
     * de cultivo, ya que puede ocurrir que haya mas de un cultivo
     * con un mayor rendimiento con respecto a los cultivos cosechados
     * en una parcela durante un periodo definido por dos fechas.
     */
    String queryString = "SELECT CROP.NAME FROM CROP WHERE CROP.ID = (" + queryToSelectIdCropHighestHarvest + ")";

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
   * @param parcelId
   * @param dateFrom
   * @param dateUntil
   * @return double que representa la cantidad de kilogramos
   * del cultivo cosechado en una parcela durante un periodo
   * definido por dos fechas que tuvo la mayor cantidad de
   * kilogramos cosechados
   */
  public double higherHarvest(int parcelId, Calendar dateFrom, Calendar dateUntil) {
    /*
     * Calcula la cantidad total de los kilogramos cosechados
     * de cada uno de los cultivos cosechados en una parcela
     * durante un periodo definido por dos fechas y selecciona
     * la cantidad maxima. De esta manera, se obtiene la cantidad
     * maxima de kilogramos cosechados de un cultivo sembrado
     * en una parcela durante un periodo definido por dos fechas.
     */
    String queryString = "SELECT MAX(TOTALS.TOTAL) FROM "
        + "(SELECT FK_CROP, SUM(HARVEST_AMOUNT) AS TOTAL FROM HARVEST WHERE ?1 <= HARVEST.DATE AND HARVEST.DATE <= ?2 AND "
        + "FK_PARCEL = ?3 GROUP BY FK_CROP) AS TOTALS";

    Query query = getEntityManager().createNativeQuery(queryString);
    query.setParameter(1, dateFrom);
    query.setParameter(2, dateUntil);
    query.setParameter(3, parcelId);

    double quantityMostPlantedCrop = 0;

    try {
      quantityMostPlantedCrop = (double) query.getSingleResult();
    } catch (NullPointerException e) {
      e.printStackTrace();
    }

    return quantityMostPlantedCrop;
  }

  /**
   * Retorna el nombre del cultivo que menor rendimiento (menor
   * cantidad de kilogramos cosechados) tuvo de los cultivos
   * cosechados en una parcela durante un periodo definido por
   * dos fechas si y solo si existe tal cultivo
   * 
   * @param parcelId
   * @param dateFrom
   * @param dateUntil
   * @return referencia a un objeto de tipo String que contiene
   * el nombre del cultivo que menor rendimiento (menor cantidad
   * de kilogramos cosechados) tuvo de los cultivos cosechados en
   * una parcela durante un periodo definido por dos fechas, si
   * existe dicho cultivo. En caso contrario, retorna la referencia
   * a un objeto de tipo String que contiene la cadena "Cultivo
   * inexistente".
   */
  public String findCropLowerHarvest(int parcelId, Calendar dateFrom, Calendar dateUntil) {
    Collection<String> cropNames = searchCropLowerHarvest(parcelId, dateFrom, dateUntil);

    /*
     * Si la coleccion devuelta por el metodo searchCropHighestHarvest,
     * esta vacia o su tamaño es menor a 1 significa que no se encontro
     * el cultivo con menor rendimiento (menor cantidad de kilogramos
     * cosechados) o que existe mas de un cultivo con menor rendimiento
     * en una parcela durante un periodo dado por dos fechas.
     * 
     * En ambos casos se retorna la cadena "Cultivo no existente". En
     * el segundo caso se retorna dicha cadena porque el cultivo que
     * menor rendimiento tuvo en una parcela durante un periodo definido
     * por dos fechas es uno solo.
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
   * de los cultivos que menor rendimiento (cantidad de kilogramos
   * cosechados) tuvieron en una parcela durante un periodo definido
   * por dos fechas. En el caso en el que no existen tales cultivos,
   * retorna una referencia a un objeto de tipo Collection vacio, es
   * decir, que no tiene ninguna referencia a un objeto de tipo String.
   */
  private Collection<String> searchCropLowerHarvest(int parcelId, Calendar dateFrom, Calendar dateUntil) {
    /*
     * Calcula la cantidad total de los kilogramos cosechados
     * de cada uno de los cultivos cosechados en una parcela
     * durante un periodo definido por dos fechas y selecciona
     * la cantidad minima. De esta manera, se obtiene la cantidad
     * minima de kilogramos cosechados de un cultivo sembrado
     * en una parcela durante un periodo definido por dos fechas.
     */
    String queryToCalculateHighestHarvest = "SELECT MIN(TOTALS.TOTAL) FROM "
        + "(SELECT FK_CROP, SUM(HARVEST_AMOUNT) AS TOTAL FROM HARVEST WHERE ?1 <= HARVEST.DATE AND HARVEST.DATE <= ?2 AND "
        + "FK_PARCEL = ?3 GROUP BY FK_CROP) AS TOTALS";

    /*
     * Selecciona el ID del cultivo que tuvo el menor rendimiento
     * (menor cantidad de kilogramos cosechados) de los cultivos
     * cosechados en una parcela durante un periodo definido por
     * dos fechas. Hay que tener en cuenta que esta consulta puede
     * retornar mas de un ID de cultivo, ya que puede ocurrir que
     * haya mas de un cultivo con un menor rendimiento con respecto
     * a los cultivos cosechados en una parcela durante un periodo
     * definido por dos fechas.
     */
    String queryToSelectIdCropHighestHarvest = "SELECT FK_CROP FROM "
        + "HARVEST WHERE ?1 <= HARVEST.DATE AND HARVEST.DATE <= ?2 AND FK_PARCEL = ?3 GROUP BY FK_CROP "
        + "HAVING SUM(HARVEST_AMOUNT) = (" + queryToCalculateHighestHarvest + ")";

    /*
     * Selecciona el nombre del cultivo que tuvo el menor rendimiento
     * (menor cantidad de kilgoramos cosechados) de los cultivos
     * cosechados en una parcela durante un periodo definido por dos
     * fechas. Hay que esta consulta puede retornar mas de un nombre
     * de cultivo, ya que puede ocurrir que haya mas de un cultivo
     * con un menor rendimiento con respecto a los cultivos cosechados
     * en una parcela durante un periodo definido por dos fechas.
     */
    String queryString = "SELECT CROP.NAME FROM CROP WHERE CROP.ID = (" + queryToSelectIdCropHighestHarvest + ")";

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
   * @param parcelId
   * @param dateFrom
   * @param dateUntil
   * @return double que representa la cantidad de kilogramos
   * del cultivo cosechado en una parcela durante un periodo
   * definido por dos fechas que tuvo la menor cantidad de
   * kilogramos cosechados
   */
  public double lowerHarvest(int parcelId, Calendar dateFrom, Calendar dateUntil) {
    /*
     * Calcula la cantidad total de los kilogramos cosechados
     * de cada uno de los cultivos cosechados en una parcela
     * durante un periodo definido por dos fechas y selecciona
     * la cantidad minima. De esta manera, se obtiene la cantidad
     * minima de kilogramos cosechados de un cultivo sembrado
     * en una parcela durante un periodo definido por dos fechas.
     */
    String queryString = "SELECT MIN(TOTALS.TOTAL) FROM "
        + "(SELECT FK_CROP, SUM(HARVEST_AMOUNT) AS TOTAL FROM HARVEST WHERE ?1 <= HARVEST.DATE AND HARVEST.DATE <= ?2 AND "
        + "FK_PARCEL = ?3 GROUP BY FK_CROP) AS TOTALS";

    Query query = getEntityManager().createNativeQuery(queryString);
    query.setParameter(1, dateFrom);
    query.setParameter(2, dateUntil);
    query.setParameter(3, parcelId);

    double quantityMostPlantedCrop = 0;

    try {
      quantityMostPlantedCrop = (double) query.getSingleResult();
    } catch (NullPointerException e) {
      e.printStackTrace();
    }

    return quantityMostPlantedCrop;
  }

  /**
   * Retorna true si y solo si una cosecha pertenece a un
   * usuario
   * 
   * @param userId
   * @param harvestId
   * @return true si una cosecha pertenece a un usuario,
   * en caso contrario false
   */
  public boolean checkUserOwnership(int userId, int harvestId) {
    return (find(userId, harvestId) != null);
  }

  /**
   * @param date
   * @param plantingRecords
   * @return true si la fecha es igual a una fecha de cosecha
   * de uno de los registros de plantacion finalizados de una
   * parcela
   */
  public boolean dateEqualToHarvestDate(Calendar date, Collection<PlantingRecord> finishedPlantingRecords) {

    for (PlantingRecord currentPlantingRecord : finishedPlantingRecords) {

      /*
       * El metodo compareTo() de la clase UtilDate retorna 0 cuando dos
       * fechas son iguales
       */
      if (UtilDate.compareTo(date, currentPlantingRecord.getHarvestDate()) == 0) {
        return true;
      }

    }

    return false;
  }

  public Page<Harvest> findAllPagination(int userId, Integer page, Integer cantPerPage, Map<String, String> parameters) throws ParseException {
    SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
    Date date;
    Calendar calendarDate;

    // Genera el WHERE dinámicamente
    StringBuffer where = new StringBuffer(" WHERE 1=1 AND e IN (SELECT h FROM Harvest h JOIN h.parcel p WHERE p IN (SELECT t FROM User u JOIN u.parcels t WHERE u.id = :userId))");

    if (parameters != null) {

      for (String param : parameters.keySet()) {
        Method method;

        try {
          method = Harvest.class.getMethod("get" + capitalize(param));

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
            case "Calendar":

              if (param.equals("date")) {
                date = new Date(dateFormatter.parse(parameters.get(param)).getTime());
                calendarDate = UtilDate.toCalendar(date);
                where.append(" AND e.date");
                where.append(" >= ");
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
    Query countQuery = entityManager.createQuery("SELECT COUNT(e.id) FROM " + Harvest.class.getSimpleName() + " e" + where.toString());
    countQuery.setParameter("userId", userId);

    // Pagina
    Query query = entityManager.createQuery("FROM " + Harvest.class.getSimpleName() + " e" + where.toString());
    query.setMaxResults(cantPerPage);
    query.setFirstResult((page - 1) * cantPerPage);
    query.setParameter("userId", userId);

    Integer count = ((Long) countQuery.getSingleResult()).intValue();
    Integer lastPage = (int) Math.ceil((double) count / (double) cantPerPage);

    // Arma la respuesta
    Page<Harvest> resultPage = new Page<Harvest>(page, count, page > 1 ? page - 1 : page, page < lastPage ? page + 1 : lastPage, lastPage, query.getResultList());
    return resultPage;
  }

  private String capitalize(final String line) {
    return Character.toUpperCase(line.charAt(0)) + line.substring(1);
  }

}
