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
import model.SoilWaterBalance;

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
   * Elimina una parcela fisicamente mediante su ID
   * 
   * @param id
   * @return referencia a un objeto de tipo Parcel en
   * caso de eliminarse de la base de datos subyacente
   * la parcela con el ID dado, en caso contrario null
   */
  public Parcel remove(int id) {
    Parcel givenParcel = find(id);

    if (givenParcel != null) {
      getEntityManager().remove(givenParcel);
      return givenParcel;
    }

    return null;
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
      chosenParcel.setName(modifiedParcel.getName());
      chosenParcel.setHectares(modifiedParcel.getHectares());
      chosenParcel.setLongitude(modifiedParcel.getLongitude());
      chosenParcel.setLatitude(modifiedParcel.getLatitude());
      chosenParcel.setActive(modifiedParcel.getActive());
      chosenParcel.setSoil(modifiedParcel.getSoil());
      return chosenParcel;
    }

    return null;
  }

  /**
   * Segun la documentacion web de la clase EntityManager, el metodo
   * merge() de esta clase fusiona el estado de una entidad en el
   * contexto de persistencia actual.
   * 
   * Los siguientes dos parrafos pertenecen a la pagina 161 del
   * libro "Pro JPA 2 Mastering the JavaTM Persistente API".
   * 
   * Devolver una instancia administrada distinta de la entidad
   * original es una parte fundamental del proceso de fusion.
   * Si ya existe una instancia de entidad con el mismo identificador
   * en el contexto de persistencia, el proveedor sobrescribira
   * su estado con el estado de la entidad que se esta fusionando,
   * pero la version administrada que ya existia debe devolverse
   * al cliente para que pueda ser usada. Si el proveedor no
   * actualiza una instancia en el contexto de persistencia,
   * cualquier referencia a esa instancia sera inconsistente
   * con el nuevo estado en el que se fusionara.
   * 
   * Cuando se invoca merge() en una nueva entidad, se comporta
   * de manera similar a la operacion persist(). Agrega la entidad
   * al contexto de persistencia, pero en lugar de agregar la
   * instancia de la entidad original, crea una nueva copia y
   * administra esa instancia. La copia creada por la operacion
   * merge() persiste como si se invocara el metodo persist()
   * en ella.
   * 
   * ************************************************************
   * 
   * Debido a la funcion que cumple el metodo merge() de la clase
   * EntityManager:
   * - la tabla de union que existe entre las entidades Parcel y
   * SoilWaterBalance en la base de datos subyacente, es escrita
   * en funcion de una instancia de tipo Parcel y su coleccion
   * de balances hidricos de suelo llamada soilWaterBalances.
   * - la tabla que existe para la entidad SoilWaterBalance en
   * la base de datos subyacente, es actualizada con los cambios
   * realizados en los elementos de la coleccion soilWaterBalances
   * de una instancia de tipo Parcel.
   * 
   * @param parcel
   * @return referencia a un objeto de tipo Parcel que contiene
   * los cambios realizados en el durante la ejecucion de la
   * aplicacion, si se invoca este metodo con una entidad de
   * tipo Parcel administrada. En cambio, si se lo invoca con
   * una nueva entidad de tipo Parcel, la persiste.
   */
  public Parcel merge(Parcel parcel) {
    return entityManager.merge(parcel);
  }

  /**
   * @param parcelOne
   * @param parcelTwo
   * @return true si la parcela uno tiene el mismo nombre que
   * la parcela dos, en caso contrario false
   */
  public boolean equals(Parcel parcelOne, Parcel parcelTwo) {

    if (parcelOne == null || parcelTwo == null) {
      return false;
    }

    if (parcelOne.getName() == null || parcelTwo.getName() == null) {
      return false;
    }

    return parcelOne.getName().equals(parcelTwo.getName());
  }

  /**
   * Este metodo es para el menu de busqueda de una parcela en
   * la pagina web de lista de parcelas de un usuario.
   * 
   * @param userId
   * @param parcelName
   * @return referencia a un objeto de tipo Collection que
   * contiene la parcela o las parcelas de un usuario que
   * tienen un nombre que contiene parcial o totalmente un
   * nombre dado. En caso contrario, retorna un objeto de
   * tipo Collection vacio.
   */
  public Collection<Parcel> search(int userId, String parcelName) {
    Query query = getEntityManager().createQuery("SELECT p FROM User u JOIN u.parcels p WHERE (u.id = :userId AND UPPER(p.name) LIKE :parcelName) ORDER BY p.name");
    query.setParameter("userId", userId);
    query.setParameter("parcelName", "%" + parcelName.toUpperCase() + "%");

    Collection<Parcel> givenParcel = null;

    try {
      givenParcel = (Collection) query.getResultList();
    } catch (NoResultException e) {
      e.printStackTrace();
    }

    return givenParcel;
  }

  public Parcel find(int id) {
    return getEntityManager().find(Parcel.class, id);
  }

  /**
   * Retorna una parcela de un usuario si y solo si existe en
   * la base de datos subyacente
   * 
   * @param userId
   * @param parcelId
   * @return referencia a un objeto de tipo Parcel que representa
   * una parcela de un usuario en caso de existir en la base de
   * datos subyacente una parcela con el ID dado asociada al
   * usuario del ID dado. En caso contrario, null.
   */
  public Parcel find(int userId, int parcelId) {
    Query query = entityManager.createQuery("SELECT p FROM User u JOIN u.parcels p WHERE (u.id = :userId AND p.id = :parcelId)");
    query.setParameter("userId", userId);
    query.setParameter("parcelId", parcelId);

    Parcel givenParcel = null;

    try {
      givenParcel = (Parcel) query.getSingleResult();
    } catch (NoResultException e) {
      e.printStackTrace();
    }

    return givenParcel;
  }

  /**
   * Retorna una parcela de un usuario mediante el nombre de
   * parcela si y solo si se encuentra en la base de datos
   * subyacente
   * 
   * @param userId
   * @param parcelName
   * @return referencia a un objeto de tipo Parcel que representa
   * una parcela de un usuario en caso de encontrarse en la base
   * de datos subyacente la parcela con el nombre dado y asociada
   * al usuario con el ID dado, en caso contrario null
   */
  public Parcel find(int userId, String parcelName) {
    Query query = entityManager.createQuery("SELECT p FROM User u JOIN u.parcels p WHERE (u.id = :userId AND UPPER(p.name) = UPPER(:parcelName))");
    query.setParameter("userId", userId);
    query.setParameter("parcelName", parcelName);

    Parcel givenParcel = null;

    try {
      givenParcel = (Parcel) query.getSingleResult();
    } catch (NoResultException e) {
      e.printStackTrace();
    }

    return givenParcel;
  }

  /**
   * Retorna una referencia a un objeto de tipo Parcel si y solo si se
   * encuentra en el conjunto de parcelas del usuario con el ID dado
   * y en el que no esta la parcela del ID dado, una parcela que tiene
   * un nombre igual al nombre dado
   * 
   * @param userId
   * @param parcelId
   * @param parcelName
   * @return referencia a un objeto de tipo Parcel que representa
   * la parcela que tiene el nombre dado y que pertenece al conjunto
   * de parcelas del usuario con el ID dado en el que no esta la parcela
   * del ID dado, en caso contrario null
   */
  public Parcel find(int userId, int parcelId, String parcelName) {
    Query query = entityManager.createQuery("SELECT p FROM User u JOIN u.parcels p WHERE (u.id = :userId AND p.id != :parcelId AND UPPER(p.name) = UPPER(:parcelName))");
    query.setParameter("userId", userId);
    query.setParameter("parcelId", parcelId);
    query.setParameter("parcelName", parcelName);

    Parcel givenParcel = null;

    try {
      givenParcel = (Parcel) query.getSingleResult();
    } catch (NoResultException e) {
      e.printStackTrace();
    }

    return givenParcel;
  }

  /**
   * Retorna las parcelas activas o inactivas de un usuario que
   * tienen un nombre que coincide con el nombre de parcela dado.
   * Este metodo es para el filtro implementado en las paginas web
   * de lista de datos que estan asociados a parcelas activas o
   * inactivas, como la pagina web de balances hidricos, por ejemplo.
   * 
   * @param userId
   * @param parcelName
   * @return referencia a un objeto de tipo Collection que contiene
   * todas las parcelas activas o inactivas del usuario con el ID dado
   * que tienen un nombre que coincide con el nombre de parcela dado
   */
  public Collection<Parcel> findByName(int userId, String parcelName) {
    StringBuffer queryStr = new StringBuffer("SELECT p FROM User u JOIN u.parcels p");

    if (parcelName != null) {
      queryStr.append(" WHERE (u.id = :userId AND UPPER(p.name) LIKE :name)");
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
   * @param optionId
   * @return referencia a un objeto de tipo Parcel si en la
   * base de datos subyacente se encuentra la parcela asociada
   * a la opcion que tiene el ID dado. En caso contrario, null.
   */
  public Parcel findByOptionId(int optionId) {
    Query query = entityManager.createQuery("SELECT p FROM User u JOIN u.parcels p WHERE p.option.id = :optionId");
    query.setParameter("optionId", optionId);

    Parcel givenParcel = null;

    try {
      givenParcel = (Parcel) query.getSingleResult();
    } catch (NoResultException e) {
      e.printStackTrace();
    }

    return givenParcel;
  }

  /**
   * @param optionId
   * @return true si una parcela asociada a una opcion, tiene
   * asignado un suelo. En caso contrario, false.
   */
  public boolean checkSoil(int optionId) {
    return (findByOptionId(optionId).getSoil() != null);
  }

  /**
   * Retorna las parcelas activas de un usuario que tienen un nombre
   * que coincide con el nombre de parcela dado. Este metodo es para
   * el ingreso de la parcela en el formulario de creacion y modificacion
   * de un dato asociado a una parcela, como un registro de plantacion,
   * por ejemplo.
   * 
   * @param userId
   * @param parcelName
   * @return referencia a un objeto de tipo Collection que contiene
   * todas las parcelas activas del usuario con el ID dado que tienen
   * un nombre que coincide con el nombre de parcela dado
   */
  public Collection<Parcel> findActiveParcelByName(int userId, String parcelName) {
    StringBuffer queryStr = new StringBuffer("SELECT p FROM User u JOIN u.parcels p");

    if (parcelName != null) {
      queryStr.append(" WHERE (u.id = :userId AND UPPER(p.name) LIKE :name AND p.active = TRUE)");
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
    Query query = entityManager.createQuery("SELECT p FROM User u JOIN u.parcels p ORDER BY p.id");
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
    Query query = entityManager.createQuery("SELECT p FROM User u JOIN u.parcels p WHERE (u.id = :userId) ORDER BY p.id");
    query.setParameter("userId", userId);

    return (Collection) query.getResultList();
  }

  /**
   * Retorna las parcelas activas de la base de datos
   * subyacente
   * 
   * @return referencia a un objeto de tipo Collection que
   * contiene todas las parcelas activas de la base de datos
   * subyacente
   */
  public Collection<Parcel> findAllActive() {
    Query query = getEntityManager().createQuery("SELECT p FROM Parcel p WHERE p.active = TRUE ORDER BY p.id");
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

  /**
   * Retorna true si y solo si el usuario con el ID dado tiene
   * una parcela con el nombre dado.
   * 
   * Este metodo es para evitar que el usuario registre
   * una parcela con un nombre igual al nombre de una
   * de sus parcelas. Es decir, es para evitar que registre
   * una parcela con un nombre repetido dentro de su conjunto
   * de parcelas.
   * 
   * @param userId
   * @param parcelName
   * @return true si el usuario con el ID dado tiene una
   * parcela con el nombre dado, en caso contrario false
   */
  public boolean checkExistence(int userId, String parcelName) {
    return (find(userId, parcelName) != null);
  }

  /**
   * Retorna true si y solo si un usuario tiene una parcela o varias
   * parcelas que tienen un nombre que contiene parcial o totalmente
   * un nombre dado.
   * 
   * @param userId
   * @param name
   * @return true si un usuario tiene una parcela o varias parcelas
   * que tienen un nombre que contiene parcial o totalmente un nombre
   * dado, false en caso contrario. Tambien retorna false en el caso
   * en el que el argumento tiene el valor null.
   */
  public boolean checkExistenceForSearch(int userId, String name) {
    /*
     * Si el nombre de la parcela tiene el valor null, se retorna
     * false, ya que realizar la busqueda de una parcela con un
     * nombre con este valor es similar a buscar una parcela
     * inexistente en la base de datos subyacente.
     * 
     * Con este control se evita realizar una consulta a la base
     * de datos comparando el nombre de una parcela con el valor null.
     * Si no se realiza este control y se realiza esta consulta a
     * la base de datos, ocurre la excepcion SQLSyntaxErrorException,
     * debido a que la comparacion de un atributo con el valor
     * null incumple la sintaxis del proveedor del motor de base
     * de datos.
     */
    if (name == null) {
      return false;
    }

    return (!search(userId, name).isEmpty());
  }

  /**
   * Retorna true si y solo si hay una parcela con el nombre dado
   * dentro del conjunto de parcelas del usuario con el ID dado en
   * el que no esta la parcela del ID dado.
   * 
   * Este metodo es para evitar que haya una parcela con un nombre
   * repetido dentro del conjunto de parcelas de un usuario durante
   * la modificacion de una parcela.
   * 
   * @param userId
   * @param parcelId
   * @param parcelName
   * @return true si hay una parcela con el nombre dado dentro del
   * conjunto de parcelas del usuario con el ID dado en el que no
   * esta la parcela del ID dado, en caso contrario false
   */
  public boolean checkRepeated(int userId, int parcelId, String parcelName) {
    return (find(userId, parcelId, parcelName) != null);
  }

  /**
   * @param parcelName
   * @return referencia a un objeto de tipo String que contiene el
   * nombre de una parcela sin espacios en blanco en los extremos
   * y con un espacio en blanco entre palabra y palabra, si el nombre
   * de una parcela esta formado por mas de una palabra
   */
  public String setBlankSpacesInNameToOne(String parcelName) {
    return parcelName.trim().replaceAll("\\s{2,}", " ");
  }

  public Page<Parcel> findAllPagination(int userId, Integer page, Integer cantPerPage, Map<String, String> parameters) {
    // Genera el WHERE dinamicante
    StringBuffer where = new StringBuffer(" WHERE 1=1 AND u IN (SELECT p FROM User i JOIN i.parcels p WHERE i.id = :userId)");

    if (parameters != null) {

      for (String param : parameters.keySet()) {
        Method method;

        try {
          method = Parcel.class.getMethod("get" + capitalize(param));

          if (method == null) {
            continue;
          }

          switch (method.getReturnType().getSimpleName()) {
            case "String":
              where.append(" AND UPPER(");
              where.append(param);
              where.append(") LIKE UPPER(");
              where.append(parameters.get(param));
              where.append(")");
              break;
            default:
              where.append(" AND ");
              where.append(param);
              where.append(" = ");
              where.append(parameters.get(param));
              break;
          }

        } catch (NoSuchMethodException | SecurityException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }

      }

    }

    // Cuenta la cantidad total de resultados
    Query countQuery = entityManager
        .createQuery("SELECT COUNT(u.id) FROM " + Parcel.class.getSimpleName() + " u" + where.toString());
    countQuery.setParameter("userId", userId);

    // Realiza la paginacion
    Query query = entityManager.createQuery("FROM " + Parcel.class.getSimpleName() + " u" + where.toString());
    query.setMaxResults(cantPerPage);
    query.setFirstResult((page - 1) * cantPerPage);
    query.setParameter("userId", userId);
    Integer count = ((Long) countQuery.getSingleResult()).intValue();
    Integer lastPage = (int) Math.ceil((double) count / (double) cantPerPage);

    // Arma la respuesta
    Page<Parcel> resultPage = new Page<Parcel>(page, count, page > 1 ? page - 1 : page,
        page > lastPage ? lastPage : page + 1, lastPage, query.getResultList());
    return resultPage;
  }

  private String capitalize(final String line) {
    return Character.toUpperCase(line.charAt(0)) + line.substring(1);
  }

}
