package stateless;

import java.lang.reflect.Method;
import java.util.Calendar;
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

  public Page<Harvest> findAllPagination(int userId, Integer page, Integer cantPerPage, Map<String, String> parameters) {
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
