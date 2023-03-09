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
      chosenIrrigationRecord.setIrrigationDone(modifiedIrrigationRecord.getIrrigationDone());
      chosenIrrigationRecord.setParcel(modifiedIrrigationRecord.getParcel());
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
   * Retorna todos los registros de riego modificables de
   * todas las parcelas de un usuario
   * 
   * @return referencia a un objeto de tipo Collection que
   * contiene todos los registros de riego modificables de
   * todas las parcelas del usuario con el ID dado
   */
  public Collection<IrrigationRecord> findAllModifiable() {
    Query query = entityManager.createQuery("SELECT i FROM IrrigationRecord i WHERE (i.modifiable = 1) ORDER BY i.id");
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

  /**
   * Retorna true si y solo si un registro de riego perteneciente a
   * una parcela de un usuario es del pasado, es decir, si su fecha
   * es estrictamente menor que la fecha actual.
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
     * El metodo getInstance de la clase Calendar retorna la referencia
     * a un objeto de tipo Calendar que contiene la fecha actual
     */
    Calendar currentDate = Calendar.getInstance();

    /*
     * Si el año, el mes o el dia de la fecha de un registro de
     * riego es estrictamente menor al año, el mes o el dia de
     * la fecha actual, se retorna true como indicacion de que
     * un registro de riego es del pasado
     */
    if (dateIrrigationRecord.get(Calendar.YEAR) < currentDate.get(Calendar.YEAR)) {
      return true;
    }

    if (dateIrrigationRecord.get(Calendar.MONTH) < currentDate.get(Calendar.MONTH)) {
      return true;
    }

    if (dateIrrigationRecord.get(Calendar.DAY_OF_YEAR) < currentDate.get(Calendar.DAY_OF_YEAR)) {
      return true;
    }

    return false;
  }

  /**
   * Retorna true si y solo si un registro de riego es del pasado,
   * es decir, si su fecha es estrictamente menor que la fecha
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
     * El metodo getInstance de la clase Calendar retorna la referencia
     * a un objeto de tipo Calendar que contiene la fecha actual
     */
    Calendar currentDate = Calendar.getInstance();

    /*
     * Si el año, el mes o el dia de la fecha de un registro de
     * riego es estrictamente menor al año, el mes o el dia de
     * la fecha actual, se retorna true como indicacion de que
     * un registro de riego es del pasado
     */
    if (dateIrrigationRecord.get(Calendar.YEAR) < currentDate.get(Calendar.YEAR)) {
      return true;
    }

    if (dateIrrigationRecord.get(Calendar.MONTH) < currentDate.get(Calendar.MONTH)) {
      return true;
    }

    if (dateIrrigationRecord.get(Calendar.DAY_OF_YEAR) < currentDate.get(Calendar.DAY_OF_YEAR)) {
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
