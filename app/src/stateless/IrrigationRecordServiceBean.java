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
import util.UtilDate;

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
   * Elimina fisicamente un registro de riego perteneciente a
   * una parcela de un usuario
   * 
   * @param userId
   * @param irrigationRecordId
   * @return referencia a un objeto de tipo IrrigationRecord en
   * caso de eliminarse de la base de datos subyacente el registro
   * de riego que tiene el ID dado y que esta asociado a una
   * parcela de un usuario que tiene el ID de usuario dado, en
   * caso contrario null
   */
  public IrrigationRecord remove(int userId, int irrigationRecordId) {
    IrrigationRecord givenIrrigationRecord = findByUserId(userId, irrigationRecordId);

    if (givenIrrigationRecord != null) {
      getEntityManager().remove(givenIrrigationRecord);
      return givenIrrigationRecord;
    }

    return null;
  }

  public IrrigationRecord remove(int irrigationRecordId) {
    IrrigationRecord givenIrrigationRecord = find(irrigationRecordId);

    if (givenIrrigationRecord != null) {
      getEntityManager().remove(givenIrrigationRecord);
      return givenIrrigationRecord;
    }

    return null;
  }

  /**
   * Modifica un registro de riego perteneciente a una parcela
   * de un usuario
   * 
   * @param userId
   * @param irrigationRecordId
   * @param modifiedIrrigationRecord
   * @return referencia a un objeto de tipo IrrigationRecord que
   * contiene las modificaciones realizadas en caso de encontrarse
   * en la base de datos subyacente el registro de riego con el ID
   * dado, en caso contrario null
   */
  public IrrigationRecord modify(int userId, int irrigationRecordId, IrrigationRecord modifiedIrrigationRecord) {
    IrrigationRecord chosenIrrigationRecord = findByUserId(userId, irrigationRecordId);

    if (chosenIrrigationRecord != null) {
      chosenIrrigationRecord.setDate(modifiedIrrigationRecord.getDate());
      chosenIrrigationRecord.setIrrigationDone(modifiedIrrigationRecord.getIrrigationDone());
      chosenIrrigationRecord.setParcel(modifiedIrrigationRecord.getParcel());
      chosenIrrigationRecord.setCrop(modifiedIrrigationRecord.getCrop());
      return chosenIrrigationRecord;
    }

    return null;
  }

  public IrrigationRecord find(int id) {
    return getEntityManager().find(IrrigationRecord.class, id);
  }

  /**
   * Recupera un registro de riego de una parcela mediante
   * una fecha si y solo si existe en la base de datos
   * subyacente
   * 
   * @param givenDate
   * @param givenParcel
   * @return referencia a un objeto de tipo IrrigationRecord que
   * representa el registro de riego que tiene una fecha dada
   * y pertenece a una parcela, si existe en la base de datos
   * subyacente. En caso contrario, null.
   */
  public IrrigationRecord find(Calendar givenDate, Parcel givenParcel) {
    Query query = entityManager.createQuery("SELECT i FROM IrrigationRecord i WHERE (i.date = :givenDate AND i.parcel = :givenParcel)");
    query.setParameter("givenDate", givenDate);
    query.setParameter("givenParcel", givenParcel);

    IrrigationRecord givenIrrigationRecord = null;

    try {
      givenIrrigationRecord = (IrrigationRecord) query.getSingleResult();
    } catch (NoResultException e) {
      e.printStackTrace();
    }

    return givenIrrigationRecord;
  }

  /**
   * Retorna un registro de riego perteneciente a una de las
   * parcelas de un usuario
   * 
   * @param userId
   * @param irrigationRecordId
   * @return referencia a un objeto de tipo IrrigationRecord que
   * representa el registro de riego de una parcela de un
   * usuario en caso de encontrarse en la base de datos subyacente
   * el registro de riego con el ID dado y asociado al usuario
   * del ID dado, en caso contrario null
   */
  public IrrigationRecord findByUserId(int userId, int irrigationRecordId) {
    Query query = getEntityManager().createQuery("SELECT i FROM IrrigationRecord i WHERE (i.id = :irrigationRecordId AND i.parcel.user.id = :userId)");
    query.setParameter("irrigationRecordId", irrigationRecordId);
    query.setParameter("userId", userId);

    IrrigationRecord irrigationRecord = null;

    try {
      irrigationRecord = (IrrigationRecord) query.getSingleResult();
    } catch(NoResultException e) {
      e.printStackTrace();
    }

    return irrigationRecord;
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
   * Retorna los registros de riego de una parcela mediante
   * el nombre de una parcela y el ID del usuario al que pertenece
   * una parcela
   * 
   * @param userId
   * @param givenParcelName
   * @return referencia a un objeto de tipo Collection que
   * contiene los registros de riego de la parcela que tiene
   * el nombre dado y que pertenece al usuario con el ID dado
   */
  public Collection<IrrigationRecord> findAllByParcelName(int userId, String givenParcelName) {
    Query query = getEntityManager().createQuery("SELECT i FROM IrrigationRecord i WHERE (i.parcel.name = :givenParcelName AND i.parcel.user.id = :userId) ORDER BY i.id");
    query.setParameter("userId", userId);
    query.setParameter("givenParcelName", givenParcelName);

    return (Collection) query.getResultList();
  }

  /**
   * @param givenUserId
   * @param givenParcelId
   * @param givenMinorDate
   * @param givenMajorDate
   * @return referencia a un objeto de tipo IrrigationRecord que
   * representa el ultimo registro de riego creado para una parcela
   * de un usuario dado entre dos fechas dadas, si existe dicho
   * registro en la base de datos subyacente. En caso contrario,
   * null.
   */
  public IrrigationRecord findLastBetweenDates(int givenUserId, int givenParcelId, Calendar givenMinorDate, Calendar givenMajorDate) {
    /*
     * Selecciona el ID mas grande del conjunto de registros de
     * riego pertenecientes a una parcela de un usuario dado
     * que estan entre dos fechas dadas
     */
    String subQuery = "(SELECT MAX(i.id) FROM IrrigationRecord i WHERE (i.parcel.user.id = :userId AND i.parcel.id = :parcelId AND "
        + "i.date >= :minorDate AND i.date <= :majorDate))";

    /*
     * Selecciona el ultimo registro de riego de una parcela de
     * un usuario dado en un periodo definido por dos fechas
     * dadas
     */
    Query query = entityManager.createQuery("SELECT i FROM IrrigationRecord i WHERE i.id = " + subQuery);
    query.setParameter("userId", givenUserId);
    query.setParameter("parcelId", givenParcelId);
    query.setParameter("minorDate", givenMinorDate);
    query.setParameter("majorDate", givenMajorDate);

    IrrigationRecord givenIrrigationRecord = null;

    try {
      givenIrrigationRecord = (IrrigationRecord) query.getSingleResult();
    } catch (NoResultException e) {
      e.printStackTrace();
    }

    return givenIrrigationRecord;
  }

  /**
   * Retorna todos los registros de riego de una parcela de
   * un usuario que estan en un periodo definido por dos fechas,
   * si una parcela tiene registros de riego en un periodo dado.
   * 
   * @param userId
   * @param parcelId
   * @param dateFrom
   * @param dateUntil
   * @return referencia a un objeto de tipo Collection que
   * contiene todos los registros de riego de una parcela de un
   * usuario que estan en un periodo definido por dos fechas.
   * En caso contrario, referencia a un objeto de tipo Collection
   * vacio (0 elementos).
   */
  public Collection<IrrigationRecord> findAllByParcelIdAndPeriod(int userId, int parcelId, Calendar dateFrom, Calendar dateUntil) {
    Query query = getEntityManager().createQuery("SELECT i FROM IrrigationRecord i JOIN i.parcel p WHERE (p.id = :parcelId AND p.user.id = :userId AND :dateFrom <= i.date AND i.date <= :dateUntil) ORDER BY i.date");
    query.setParameter("userId", userId);
    query.setParameter("parcelId", parcelId);
    query.setParameter("dateFrom", dateFrom);
    query.setParameter("dateUntil", dateUntil);

    return (Collection) query.getResultList();
  }

  /**
   * @param givenUserId
   * @param givenParcelId
   * @param givenMinorDate
   * @param givenMajorDate
   * @return true si existe el ultimo registro de riego de una
   * parcela de un usuario dado en un periodo definido por dos
   * fechas. En caso contrario, false.
   */
  public boolean checkExistenceLastBetweenDates(int givenUserId, int givenParcelId, Calendar givenMinorDate, Calendar givenMajorDate) {
    return (findLastBetweenDates(givenUserId, givenParcelId, givenMinorDate, givenMajorDate) != null);
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
   * dado perteneciente una parcela del usuario con el ID dado,
   * en caso contrario false
   */
  public boolean checkUserOwnership(int userId, int irrigationRecordId) {
    return (findByUserId(userId, irrigationRecordId) != null);
  }

  /**
   * @param givenParcel
   * @return punto flotante que representa la cantidad total de
   *         agua de riego utilizada para un cultivo en la fecha
   *         actual
   */
  public double calculateTotalIrrigationWaterCurrentDate(Parcel givenParcel) {
    /*
     * Suma el riego realizado de cada uno de los registros
     * de riego de una parcela en la fecha actual
     */
    Query query = entityManager.createQuery("SELECT SUM(i.irrigationDone) FROM IrrigationRecord i WHERE (i.date = :currentDate AND i.parcel = :givenParcel)");
    query.setParameter("currentDate", UtilDate.getCurrentDate());
    query.setParameter("givenParcel", givenParcel);

    double totalIrrigationWaterCurrentDate = 0.0;

    try {
      /*
       * Si se realiza la consulta JPQL de este metodo en SQL
       * con una parcela que no tiene ningun registro de riego
       * asociado en un periodo definido por dos fechas, se
       * observara que el valor devuelto es NULL. Por lo tanto,
       * es necesario contemplar este caso en el codigo fuente
       * de este metodo.
       * 
       * En caso de que se solicite la suma del agua de riego de
       * la fecha actual de una parcela que no tiene ningun registro
       * de riego con la fecha actual, se retorna el valor 0.0
       */
      totalIrrigationWaterCurrentDate = (double) query.getSingleResult();
    } catch (NullPointerException e) {
      e.printStackTrace();
    }

    return totalIrrigationWaterCurrentDate;
  }

  /**
   * @param givenDate
   * @param givenParcel
   * @return punto flotante que representa la cantidad total de
   *         agua de riego utilizada para un cultivo en una fecha
   *         dada
   */
  public double calculateTotalIrrigationWaterGivenDate(Calendar givenDate, Parcel givenParcel) {
    /*
     * Suma el riego realizado de cada uno de los registros
     * de riego de una fecha dada pertenecientes a una parcela
     * dada
     */
    Query query = entityManager.createQuery("SELECT SUM(i.irrigationDone) FROM IrrigationRecord i WHERE (i.date = :givenDate AND i.parcel = :givenParcel)");
    query.setParameter("givenDate", givenDate);
    query.setParameter("givenParcel", givenParcel);

    double result = 0.0;

    try {
      result = (double) query.getSingleResult();
    } catch (NullPointerException e) {
      e.printStackTrace();
    }

    return result;
  }

  /**
   * Comprueba la existencia de un registro de riego en la base
   * de datos subyacente. Retorna true si y solo si existe en
   * la base de datos el registro de riego con una fecha dada
   * perteneciente a una parcela dada.
   * 
   * @param givenDate
   * @param givenParcel
   * @return true si el registro de riego con una fecha dada
   * perteneciente a una parcela dada existe en la base de datos
   * subyacente, en caso contrario false
   */
  public boolean checkExistence(Calendar givenDate, Parcel givenParcel) {
    return (find(givenDate, givenParcel) != null);
  }

  /**
   * Retorna true si y solo si un registro de riego perteneciente a
   * una parcela de un usuario es del pasado, es decir, si su fecha
   * es estrictamente menor a la fecha actual.
   * 
   * Hay que tener en cuenta que este metodo debe ser invocado luego
   * de haber invocado al metodo checkExistence de esta clase, ya
   * que de lo contrario puede ocurrir la excepcion NoResultException,
   * la cual, ocurre cuando NO existe en la base de datos subyacente
   * el dato consultado.
   * 
   * El motivo por el cual no se usa el metodo before de la clase
   * Calendar para determinar si la fecha de un registro de riego es
   * pasada o no es que dicho metodo utiliza el tiempo en milisegundos
   * UTC desde la epoca (1 de enero de 1970) de dos objetos Calendar
   * para determinar si el tiempo de uno es anterior al tiempo del otro.
   * La documentacion de la clase Calendar no dice esto del metodo before,
   * pero dice que este metodo es equivalente a usar al metodo compareTo
   * de la siguiente manera: compareTo(when) < 0. El metodo compareTo(when)
   * utiliza el tiempo en milisegundos UTC desde la epoca (*) para
   * comparar el tiempo de dos objetos de tipo Calendar.
   * 
   * Estos son los motivos por los cuales digo que el metodo before
   * de la clase Calendar utiliza el tiempo en milisegundos UTC desde
   * la epoca de dos objetos Calendar para determinar si el tiempo de uno
   * es anterior al tiempo del otro, y por los cuales no se utiliza dicho
   * metodo para determinar si un registro de riego es del pasado o no.
   * 
   * (*) debido a que usa el metodo getTimeInMillis, el cual, retorna
   * el tiempo en milisegundos UTC desde la epoca (ver codigo fuente
   * de dicho metodo para comprobar que lo que estoy diciendo es correcto).
   * 
   * Este metodo es para evitar que el usuario modifique un registro de
   * riego del pasado (es decir, uno que tiene su fecha estrictamente
   * menor que la fecha actual).
   * 
   * @param userId
   * @param irrigationRecordId
   * @return true si el registro de riego con el ID dado y asociado
   * al usuario del ID dado es del pasado, en caso contrario false
   */
  public boolean isFromPast(int userId, int irrigationRecordId) {
    Calendar dateIrrigationRecord = findByUserId(userId, irrigationRecordId).getDate();

    /*
     * Si la fecha de un registro de riego es estrictamente menor a
     * la fecha actual, se retorna true como indicativo de que este
     * registro es del pasado
     */
    if (UtilDate.compareTo(dateIrrigationRecord, UtilDate.getCurrentDate()) < 0) {
      return true;
    }

    return false;
  }

  /**
   * Actualiza la necesidad de agua de riego del registro
   * de riego de una parcela
   * 
   * @param id
   * @param givenParcel
   * @param irrigationWaterNeed [mm/dia]
   */
  public void updateIrrigationWaterNeed(int id, Parcel givenParcel, String irrigationWaterNeed) {
    Query query = entityManager.createQuery("UPDATE IrrigationRecord i SET i.irrigationWaterNeed = :irrigationWaterNeed WHERE (i.id = :givenId AND i.parcel = :givenParcel)");
    query.setParameter("givenId", id);
    query.setParameter("givenParcel", givenParcel);
    query.setParameter("irrigationWaterNeed", irrigationWaterNeed);
    query.executeUpdate();
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
