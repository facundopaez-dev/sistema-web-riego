package stateless;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import model.Parcel;

@Stateless
public class ParcelServiceBean {

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

  public Parcel create(Parcel newParcel) {
    getEntityManager().persist(newParcel);
    return newParcel;
  }

  /**
   * Elimina logicamente una parcela de un usuario
   * 
   * @param userId
   * @param parcelId
   * @return referencia a un objeto de tipo Parcel si la parcela
   * a eliminar pertenece al usuario con el ID dado, null en
   * caso contrario
   */
  public Parcel remove(int userId, int parcelId) {
    Parcel givenParcel = find(userId, parcelId);

    if (givenParcel != null) {
      givenParcel.setActive(false);
      return givenParcel;
    }

    return null;
  }

  /**
   * Modifica una parcela de un usuario
   *
   * @param userId
   * @param parcelId
   * @param modifiedParcel
   * @return referencia a un objeto de tipo Parcel si se modifica
   * la parcela con el ID y el ID de usuario provistos, null en
   * caso contrario
   */
  public Parcel modify(int userId, int parcelId, Parcel modifiedParcel) {
    Parcel chosenParcel = find(userId, parcelId);

    if (chosenParcel != null) {
      /*
       * TODO: Leer
       * Probablemente se tenga que hacer una validacion
       * que impida que un mismo usuario cliente cargue
       * para si mismo mas de una parcela con nombre
       * repetido
       */
      chosenParcel.setName(modifiedParcel.getName());
      chosenParcel.setHectare(modifiedParcel.getHectare());
      chosenParcel.setLongitude(modifiedParcel.getLongitude());
      chosenParcel.setLatitude(modifiedParcel.getLatitude());
      chosenParcel.setActive(modifiedParcel.getActive());
      return chosenParcel;
    }

    return null;
  }

  public Parcel find(int id) {
    return getEntityManager().find(Parcel.class, id);
  }

  /**
   * Retorna una parcela de un usuario si y solo si se encuentra
   * en la base de datos subyacente
   * 
   * @param userId
   * @param parcelId
   * @return referencia a un objeto de tipo Parcel que representa
   * una parcela de un usuario en caso de encontrarse en la base
   * de datos subyacente la parcela con el ID dado asociada al
   * usuario del ID dado, en caso contrario null
   */
  public Parcel find(int userId, int parcelId) {
    Query query = entityManager.createQuery("SELECT p FROM Parcel p WHERE (p.id = :parcelId AND p.user.id = :userId)");
    query.setParameter("parcelId", parcelId);
    query.setParameter("userId", userId);

    Parcel givenParcel = null;

    try {
      givenParcel = (Parcel) query.getSingleResult();
    } catch (Exception e) {
      e.printStackTrace();
    }

    return givenParcel;
  }

  /**
   * Retorna las parcelas de un usuario que tienen un nombre que
   * coincide con el nombre de parcela dado
   * 
   * @param parcelName
   * @return referencia a un objeto de tipo Collection que contiene
   * todas las parcelas del usuario con el ID dado que tienen un
   * nombre que coincide con el nombre de parcela dado
   */
  public Collection<Parcel> findByName(int userId, String parcelName) {
    StringBuffer queryStr = new StringBuffer("SELECT p FROM Parcel p");

    if (parcelName != null) {
      queryStr.append(" WHERE (p.user.id = :userId AND UPPER(p.name) LIKE :name)");
    }

    Query query = entityManager.createQuery(queryStr.toString());
    query.setParameter("userId", userId);

    if (parcelName != null) {
      query.setParameter("name", "%" + parcelName.toUpperCase() + "%");
    }

    Collection<Parcel> operators = (Collection) query.getResultList();
    return operators;
  }

  /**
   * Retorna true si y solo si una parcela pertenece a un
   * usuario
   * 
   * @param userId
   * @param parcelId
   * @return true si la parcela del ID dado pertenece al
   * usuario con el ID dado, en caso contrario false
   */
  public boolean checkUserOwnership(int userId, int parcelId) {
    return (find(userId, parcelId) != null);
  }

  /**
   * Retorna todas las parcelas registradas en la base de
   * datos subyacente, por lo tanto, retorna todas las
   * parcelas de todos los usuarios registrados en dicha
   * base de datos
   * 
   * @return referencia a un objeto de tipo Collection que
   * contiene todas las parcelas registradas en la base de
   * datos subyacente
   */
  public Collection<Parcel> findAll() {
    Query query = entityManager.createQuery("SELECT p FROM Parcel p ORDER BY p.id");
    return (Collection) query.getResultList();
  }

  /**
   * Retorna las parcelas de un usuario
   * 
   * @param userId
   * @return referencia a un objeto de tipo Collection que
   * contiene todas las parcelas del usuario con el ID dado
   */
  public Collection<Parcel> findAll(int userId) {
    Query query = entityManager.createQuery("SELECT p FROM Parcel p WHERE p.user.id = :userId ORDER BY p.id");
    query.setParameter("userId", userId);

    return (Collection) query.getResultList();
  }

  /**
   * Retorna las parcelas activas de un usuario
   * 
   * @param userId
   * @return referencia a un objeto de tipo Collection que
   * contiene todas las parcelas activas del usuario con
   * el ID dado
   */
  public Collection<Parcel> findAllActive(int userId) {
    Query query = getEntityManager().createQuery("SELECT p FROM Parcel p WHERE (p.user.id = :userId AND p.active = TRUE) ORDER BY p.id");
    query.setParameter("userId", userId);

    return (Collection) query.getResultList();
  }

  /**
   * Comprueba la existencia de una parcela en la base de datos
   * subyacente. Retorna true si y solo si existe la parcela
   * con el ID dado.
   * 
   * @param id
   * @return true si la parcela con el ID dado existe en la
   * base de datos subyacente, false en caso contrario
   */
  public boolean checkExistence(int id) {
    return (getEntityManager().find(Parcel.class, id) != null);
  }

  public Page<Parcel> findByPage(Integer page, Integer cantPerPage, Map<String, String> parameters) {
    // Genero el WHERE dinámicamente
    StringBuffer where = new StringBuffer(" WHERE 1=1");

    if (parameters != null)
    for (String param : parameters.keySet()) {
      Method method;
      try {
        method = Parcel.class.getMethod("get" + capitalize(param));
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
        .createQuery("SELECT COUNT(e.id) FROM " + Parcel.class.getSimpleName() + " e" + where.toString());

    // Pagino
    Query query = getEntityManager().createQuery("FROM " + Parcel.class.getSimpleName() + " e" + where.toString());
    query.setMaxResults(cantPerPage);
    query.setFirstResult((page - 1) * cantPerPage);
    Integer count = ((Long) countQuery.getSingleResult()).intValue();
    Integer lastPage = (int) Math.ceil((double) count / (double) cantPerPage);

    // Armo respuesta
    Page<Parcel> resultPage = new Page<Parcel>(page, count, page > 1 ? page - 1 : page,
        page > lastPage ? page + 1 : lastPage, lastPage, query.getResultList());
    return resultPage;
  }

  private String capitalize(final String line) {
    return Character.toUpperCase(line.charAt(0)) + line.substring(1);
  }

}
