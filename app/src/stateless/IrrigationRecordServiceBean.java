package stateless;

import java.lang.NullPointerException;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.Collection;
import java.util.Map;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import model.IrrigationRecord;
import model.Parcel;

@Stateless
public class IrrigationRecordServiceBean {

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

  public IrrigationRecord create(IrrigationRecord newIrrigationRecord) {
    getEntityManager().persist(newIrrigationRecord);
    return newIrrigationRecord;
  }

  /**
   * Actualiza o modifica la entidad asociada al id dado
   *
   * @param  id
   * @param  modifiedIrrigationRecord
   * @return un valor no nulo en caso de modificar la entidad solicitada
   * mediante el id, en caso contrario retorna un valor nulo
   */
  public IrrigationRecord modify(int id, IrrigationRecord modifiedIrrigationRecord) {
    IrrigationRecord choosenIrrigationRecord = find(id);

    if (choosenIrrigationRecord != null) {
      choosenIrrigationRecord.setIrrigationDone(modifiedIrrigationRecord.getIrrigationDone());
      return choosenIrrigationRecord;
    }

    return null;
  }

  public IrrigationRecord find(int id) {
    return getEntityManager().find(IrrigationRecord.class, id);
  }

  /**
   * Retorna un registro de riego perteneciente a una parcela
   * de un usuario
   * 
   * @param userId
   * @param irrigationRecordId
   * @return referencia a un objeto de tipo IrrigationRecord
   * perteneciente a una parcela de un usuario con el ID dado
   */
  public IrrigationRecord find(int userId, int irrigationRecordId) {
    Query query = entityManager.createQuery("SELECT i FROM IrrigationRecord i WHERE (i.id = :irrigationRecordId AND i.parcel.user.id = :userId)");
    query.setParameter("irrigationRecordId", irrigationRecordId);
    query.setParameter("userId", userId);

    return (IrrigationRecord) query.getSingleResult();
  }

  /**
   * Retorna todos los registros de riego de todas las
   * parcelas de un usuario
   * 
   * @param userId
   * @return referencia a un objeto de tipo Collection que
   * contiene todos los registros de riego de todas las
   * parcelas del usuario con el ID dado
   */
  public Collection<IrrigationRecord> findAll(int userId) {
    Query query = entityManager.createQuery("SELECT i FROM IrrigationRecord i WHERE (i.parcel.user.id = :userId) ORDER BY i.id");
    query.setParameter("userId", userId);

    return (Collection) query.getResultList();
  }

  /**
   * Comprueba la existencia de un registro de riego en la base
   * de datos subyacente. Retorna true si y solo si existe el
   * registro de riego con el ID dado.
   * 
   * @param id
   * @return true si el registro de riego con el ID dado existe
   * en la base de datos subyacente, en caso contrario false
   */
  public boolean checkExistence(int id) {
    return (getEntityManager().find(IrrigationRecord.class, id) != null);
  }

  /**
   * Comprueba si un registro de riego pertenece a un usuario
   * a traves de la relacion muchos a uno que hay entre la
   * entidad parcela y la entidad usuario.
   * 
   * Retorna true si y solo si un registro de riego pertenece
   * a una parcela de un usuario.
   * 
   * @param userId
   * @param irrigationRecordId
   * @return true si se encuentra el registro de riego con el ID
   * perteneciente una parcela del usuario con el ID dado, en caso
   * contrario false
   */
  public boolean checkUserOwnership(int userId, int irrigationRecordId) {
    boolean result = false;

    try {
      find(userId, irrigationRecordId);
      result = true;
    } catch (NoResultException e) {
      e.printStackTrace();
    }

    return result;
  }

  /**
   * @param  givenParcel
   * @return la cantidad total de agua utilizada para el riego
   * (de un cultivo dado) por el usuario cliente en la parcela
   * dada y en la fecha actual del sistema
   */
  public double getTotalWaterIrrigationToday(Parcel givenParcel) {
    /*
     * Fecha actual del sistema
     */
    Calendar currentDate = Calendar.getInstance();

    /*
     * Suma cada riego realizado, por parte del usuario cliente,
     * para un cultivo dado, de la parcela dada en la fecha
     * actual
     */
    Query query = entityManager.createQuery("SELECT SUM(i.irrigationDone) FROM IrrigationRecord i WHERE (i.date = :currentDate AND i.parcel = :givenParcel)");
    query.setParameter("currentDate", currentDate);
    query.setParameter("givenParcel", givenParcel);

    double result = 0.0;

    try {
      result = (double) query.getSingleResult();
    } catch(NullPointerException e) {
      e.printStackTrace();
    }

    return result;
  }

  /**
   * Comprueba si la parcela dada tiene un registro
   * de riego asociado (en la base de datos) y si lo
   * tiene retorna verdadero, en caso contrario retorna
   * falso
   *
   * @param  givenDate
   * @param  givenParcel
   * @return verdadero en caso de enccontrar un registro de riego
   * con la fecha y la parcela dadas, en caso contrario retorna falso
   */
  public boolean exist(Calendar givenDate, Parcel givenParcel) {
    Query query = entityManager.createQuery("SELECT r FROM IrrigationRecord r WHERE r.date = :givenDate AND r.parcel = :givenParcel");
    query.setParameter("givenDate", givenDate);
    query.setParameter("givenParcel", givenParcel);

    boolean result = false;

    try {
      query.getSingleResult();
      result = true;
    } catch(NoResultException e) {
      e.printStackTrace();
    }

    return result;
  }

  public Page<IrrigationRecord> findByPage(Integer page, Integer cantPerPage, Map<String, String> parameters) {
    // Genero el WHERE dinámicamente
    StringBuffer where = new StringBuffer(" WHERE 1=1");
    if (parameters != null)
      for (String param : parameters.keySet()) {
        Method method;
        try {
          method = IrrigationRecord.class.getMethod("get" + capitalize(param));
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
      }

    // Cuento el total de resultados
    Query countQuery = getEntityManager()
        .createQuery("SELECT COUNT(e.id) FROM " + IrrigationRecord.class.getSimpleName() + " e" + where.toString());

    // Pagino
    Query query = getEntityManager().createQuery("FROM " + IrrigationRecord.class.getSimpleName() + " e" + where.toString());
    query.setMaxResults(cantPerPage);
    query.setFirstResult((page - 1) * cantPerPage);
    Integer count = ((Long) countQuery.getSingleResult()).intValue();
    Integer lastPage = (int) Math.ceil((double) count / (double) cantPerPage);

    // Armo respuesta
    Page<IrrigationRecord> resultPage = new Page<IrrigationRecord>(page, count, page > 1 ? page - 1 : page,
        page > lastPage ? page + 1 : lastPage, lastPage, query.getResultList());
    return resultPage;
  }

  private String capitalize(final String line) {
    return Character.toUpperCase(line.charAt(0)) + line.substring(1);
  }

}
