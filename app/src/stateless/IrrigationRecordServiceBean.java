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
    IrrigationRecord chosenIrrigationRecord = find(userId, irrigationRecordId);

    if (chosenIrrigationRecord != null) {
      chosenIrrigationRecord.setDate(modifiedIrrigationRecord.getDate());
      chosenIrrigationRecord.setIrrigationDone(modifiedIrrigationRecord.getIrrigationDone());
      chosenIrrigationRecord.setParcel(modifiedIrrigationRecord.getParcel());
      chosenIrrigationRecord.setModifiable(modifiedIrrigationRecord.getModifiable());
      return chosenIrrigationRecord;
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
   * @return referencia a un objeto de tipo IrrigationRecord en
   * caso de encontrarse en la base de datos subyacente el registro
   * de riego con el ID dado y asociado al usuario con el ID dado,
   * en caso contrario null
   */
  public IrrigationRecord find(int userId, int irrigationRecordId) {
    Query query = entityManager.createQuery("SELECT i FROM IrrigationRecord i WHERE (i.id = :irrigationRecordId AND i.parcel.user.id = :userId)");
    query.setParameter("irrigationRecordId", irrigationRecordId);
    query.setParameter("userId", userId);

    IrrigationRecord givenIrrigationRecord = null;

    try {
      givenIrrigationRecord = (IrrigationRecord) query.getSingleResult();
    } catch (NoResultException e) {
      e.printStackTrace();
    }

    return givenIrrigationRecord;
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
   * Recupera un registro de riego de una parcela generado
   * por el sistema mediante una fecha si y solo si existe
   * en la base de datos subyacente
   * 
   * @param givenDate
   * @param givenParcel
   * @return referencia a un objeto de tipo IrrigationRecord que
   * representa el registro de riego que tiene una fecha dada,
   * pertenece a una parcela y fue generado por el sistema, si
   * existe en la base de datos subyacente. En caso contrario,
   * null.
   */
  public IrrigationRecord findGeneratedBySystem(Calendar givenDate, Parcel givenParcel) {
    Query query = entityManager.createQuery("SELECT i FROM IrrigationRecord i WHERE (i.date = :givenDate AND i.parcel = :givenParcel AND i.systemGenerated = 1)");
    query.setParameter("givenDate", givenDate);
    query.setParameter("givenParcel", givenParcel);

    IrrigationRecord irrigationRecordGenerated = null;

    try {
      irrigationRecordGenerated = (IrrigationRecord) query.getSingleResult();
    } catch (NoResultException e) {
      e.printStackTrace();
    }

    return irrigationRecordGenerated;
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
   * Retorna todos los registros de riego modificables de
   * todas las parcelas de la base de datos subyacente
   * 
   * @return referencia a un objeto de tipo Collection que
   * contiene todos los registros de riego modificables de
   * todas las parcelas de la base de datos subyacente
   */
  public Collection<IrrigationRecord> findAllModifiable() {
    Query query = entityManager.createQuery("SELECT i FROM IrrigationRecord i WHERE (i.modifiable = 1) ORDER BY i.id");
    return (Collection) query.getResultList();
  }

  /**
   * Retorna todos los registros de riego que tienen el cultivo
   * definido y el valor "n/a" (no disponible) en el atributo de
   * la necesidad de agua de riego.
   * 
   * Un registro de riego tiene el cultivo definido cuando se lo
   * crea para una parcela que tiene un registro de plantacion en
   * desarrollo (por ende, tiene un cultivo en desarrollo). Por lo
   * tanto, este metodo retorna una coleccion que contiene todos
   * los registros de riego que tienen el valor "n/a" en el atributo
   * de la necesidad de agua de riego que corresponden a parcelas
   * que tienen un cultivo en desarrollo en la fecha actual.
   * 
   * Este metodo es para el metodo automatico setIrrigationWaterNeed
   * de la clase IrrigationRecordManager.
   * 
   * @return referencia a un objeto de tipo Collection que contiene
   * los registros de riego que tienen el cultivo definido y el valor
   * "n/a" (no disponible) en el atributo de la necesidad de agua de
   * riego [mm/dia]
   */
  public Collection<IrrigationRecord> findAllUndefinedWithCrop() {
    Query query = entityManager.createQuery("SELECT i FROM IrrigationRecord i WHERE (i.irrigationWaterNeed = :notAvailable AND i.crop != null) ORDER BY i.id");
    query.setParameter("notAvailable", "n/a");

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
   * dado perteneciente una parcela del usuario con el ID dado,
   * en caso contrario false
   */
  public boolean checkUserOwnership(int userId, int irrigationRecordId) {
    return (find(userId, irrigationRecordId) != null);
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
     * de riego de una parcela en la fecha actual.
     * 
     * El metodo getInstance de la clase Calendar retorna la
     * referencia a un objeto de tipo Calendar que contiene
     * la fecha actual.
     */
    Query query = entityManager.createQuery("SELECT SUM(i.irrigationDone) FROM IrrigationRecord i WHERE (i.date = :currentDate AND i.parcel = :givenParcel)");
    query.setParameter("currentDate", Calendar.getInstance());
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
   * Comprueba si en la base de datos subyacente existe un
   * registro de riego generado por el sistema. Retorna true
   * si y solo si existe en la base de datos el registro de
   * riego generado por el sistema con una fecha dada
   * perteneciente a una parcela dada.
   * 
   * @param givenDate
   * @param givenParcel
   * @return true si el registro de riego generado por el sistema
   * con una fecha dada perteneciente a una parcela dada existe en
   * la base de datos subyacente, en caso contrario false
   */
  public boolean checkExistenceGeneratedBySystem(Calendar givenDate, Parcel givenParcel) {
    return (findGeneratedBySystem(givenDate, givenParcel) != null);
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
    Calendar dateIrrigationRecord = find(userId, irrigationRecordId).getDate();

    /*
     * Si la fecha de un registro de riego es estrictamente menor a
     * la fecha actual, se retorna true como indicativo de que este
     * registro es del pasado.
     * 
     * El metodo getInstance de la clase Calendar retorna la referencia
     * a un objeto de tipo Calendar que contiene la fecha actual.
     */
    if (UtilDate.compareTo(dateIrrigationRecord, Calendar.getInstance()) < 0) {
      return true;
    }

    return false;
  }

  /**
   * Retorna true si y solo si un registro de riego es del pasado,
   * es decir, si su fecha es estrictamente menor a la fecha
   * actual.
   * 
   * Este metodo es para el metodo automatico unsetModifiable
   * de la clase IrrigationRecordManager.
   * 
   * @param id
   * @return true si el registro de riego correspondiente al ID
   * dado es del pasado, en caso contrario false
   */
  public boolean isFromPast(int id) {
    Calendar dateIrrigationRecord = find(id).getDate();

    /*
     * Si la fecha de un registro de riego es estrictamente menor a
     * la fecha actual, se retorna true como indicativo de que este
     * registro es del pasado.
     * 
     * El metodo getInstance de la clase Calendar retorna la referencia
     * a un objeto de tipo Calendar que contiene la fecha actual.
     */
    if (UtilDate.compareTo(dateIrrigationRecord, Calendar.getInstance()) < 0) {
      return true;
    }

    return false;
  }

  /**
   * Establece el atributo modifiable de un registro de
   * riego en false. Esto se debe hacer para un registro
   * de riego del pasado, ya que un registro de riego
   * del pasado NO se debe poder modificar.
   * 
   * Este metodo es para el metodo automatico unsetModifiable
   * de la clase IrrigationRecordManager.
   * 
   * @param id
   */
  public void unsetModifiable(int id) {
    Query query = entityManager.createQuery("UPDATE IrrigationRecord i SET i.modifiable = 0 WHERE i.id = :givenId");
    query.setParameter("givenId", id);
    query.executeUpdate();
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

  /**
   * Retorna true si y solo si un registro de riego es modificable.
   * 
   * Hay que tener en cuenta que este metodo debe ser invocado
   * luego de invocar al metodo checkExistence de esta clase,
   * ya que si no se hace esto puede ocurrir la excepcion
   * NoResultException, la cual, ocurre cuando se invoca el
   * metodo getSingleResult de la clase Query para buscar
   * un dato inexistente en la base de datos subyacente.
   * 
   * @param id
   * @return true si un registro de riego es modificable,
   * false en caso contrario
   */
  public boolean isModifiable(int id) {
    return find(id).getModifiable();
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
