package stateless;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.NoResultException;
import java.util.Collection;
import model.TypeCrop;

@Stateless
public class TypeCropServiceBean {

  @PersistenceContext(unitName = "swcar")
  protected EntityManager entityManager;

  public void setEntityManager(EntityManager localEntityManager) {
    entityManager = localEntityManager;
  }

  public EntityManager getEntityManager() {
    return entityManager;
  }

  /**
   * Persiste en la base de datos subyacente una instancia
   * de tipo TypeCrop
   * 
   * @param newTypeCrop
   * @return referencia a un objeto de tipo TypeCrop
   */
  public TypeCrop create(TypeCrop newTypeCrop) {
    getEntityManager().persist(newTypeCrop);
    return newTypeCrop;
  }

  /**
   * Modifica el nombre de un tipo de cultivo
   * 
   * @param typeCropId
   * @param modifiedTypeCrop
   * @return referencia a un objeto de tipo TypeCrop que contiene las
   *         modificaciones realizadas si se encuentra el tipo de
   *         cultivo con el ID dado, null en caso contrario
   */
  public TypeCrop modify(int typeCropId, TypeCrop modifiedTypeCrop) {
    TypeCrop chosenTypeCrop = find(typeCropId);

    if (chosenTypeCrop != null) {
      chosenTypeCrop.setName(modifiedTypeCrop.getName());
      chosenTypeCrop.setActive(modifiedTypeCrop.getActive());
      return chosenTypeCrop;
    }

    return null;
  }

  /**
   * Elimina de forma logica de la base de datos subyacente el tipo
   * de cultivo que tiene el identificador dado
   *
   * @param  id
   * @return referencia a un objeto de tipo TypeCrop en caso de eliminarse
   * de la base de datos subyacente el tipo de cultivo correspondiente al
   * ID dado, en caso contrario null (referencia a nada)
   */
  public TypeCrop remove(int id) {
    TypeCrop givenTypeCrop = find(id);

    if (givenTypeCrop != null) {
      givenTypeCrop.setActive(false);
      return givenTypeCrop;
    }

    return null;
  }

  /**
   * 
   * @return referencia a un objeto de tipo Collection que
   * contiene todos los tipos de cultivos
   */
  public Collection<TypeCrop> findAll() {
    Query query = getEntityManager().createQuery("SELECT t FROM TypeCrop t ORDER BY t.id");
    return (Collection) query.getResultList();
  }

  public TypeCrop find(int id) {
    return getEntityManager().find(TypeCrop.class, id);
  }

  /**
   * Retorna el tipo de cultivo que tiene el nombre dado si
   * y solo si existe en la base de datos subyacente un
   * tipo de cultivo con el nombre dado
   * 
   * @param cropTypeName
   * @return referencia a un objeto de tipo TypeCrop que
   * representa el tipo de cultivo que tiene el nombre
   * dado, si existe en la base de datos subyacente. En
   * caso contrario, null.
   */
  public TypeCrop findByName(String cropTypeName) {
    Query query = getEntityManager().createQuery("SELECT t FROM TypeCrop t WHERE UPPER(t.name) = UPPER(:cropTypeName)");
    query.setParameter("cropTypeName", cropTypeName);

    TypeCrop typeCrop = null;

    try {
      typeCrop = (TypeCrop) query.getSingleResult();
    } catch (NoResultException e) {
      e.printStackTrace();
    }

    return typeCrop;
  }

  /**
   * Retorna los tipos de cultivo activos que tienen un nombre que
   * coincide con el nombre de tipo de cultivo dado. Este metodo es
   * para el ingreso del tipo de cultivo en el formulario de creacion
   * y modificacion de un dato asociado a un tipo de cultivo, como
   * un cultivo, por ejemplo.
   * 
   * @param typeCropName
   * @return referencia a un objeto de tipo Collection que contiene
   * todos los tipos de cultivo activos que tienen un nombre que
   * coincide con el nombre de tipo de cultivo dado
   */
  public Collection<TypeCrop> findActiveTypeCropByName(String typeCropName) {
    StringBuffer queryStr = new StringBuffer("SELECT t FROM TypeCrop t");

    if (typeCropName != null) {
      queryStr.append(" WHERE (UPPER(t.name) LIKE :name AND t.active = TRUE)");
    }

    Query query = entityManager.createQuery(queryStr.toString());

    if (typeCropName != null) {
      query.setParameter("name", "%" + typeCropName.toUpperCase() + "%");
    }

    Collection<TypeCrop> operators = (Collection) query.getResultList();
    return operators;
  }

  /**
   * Comprueba la existencia de un cultivo tipo de cultivo en
   * la base de datos subyacente. Retorna true si y solo si
   * existe el tipo de cultivo con el ID dado.
   * 
   * @param id
   * @return true si el tipo de cultivo con el ID dado existe
   * en la base de datos subyacente, false en caso contrario
   */
  public boolean checkExistence(int id) {
    return (getEntityManager().find(TypeCrop.class, id) != null);
  }

  /**
   * Retorna true si y solo si existe un tipo de cultivo con
   * el nombre dado en la base de datos subyacente
   * 
   * @param cropTypeName
   * @return true si existe el tipo de cultivo con el nombre
   * dado en la base de datos subyacente, false en caso contrario.
   * Tambien retorna false en el caso en el que el argumento tiene
   * el valor null.
   */
  public boolean checkExistence(String cropTypeName) {
    /*
     * Si el nombre del tipo de cultivo tiene el valor null, se
     * retorna false, ya que realizar la busqueda de un tipo de
     * cultivo con un nombre con este valor es similar a buscar
     * un tipo de cultivo inexistente en la base de datos subyacente.
     * 
     * Con este control se evita realizar una consulta a la base de
     * datos comparando el nombre del tipo de cultivo con el valor
     * null. Si no se realiza este control y se realiza esta
     * consulta a la base de datos, ocurre la excepcion
     * SQLSyntaxErrorException, debido a que la comparacion de
     * un atributo con el valor null incumple la sintaxis del
     * proveedor del motor de base de datos.
     */
    if (cropTypeName == null) {
      return false;
    }

    return (findByName(cropTypeName) != null);
  }

  /**
   * Retorna el tipo de cultivo que tiene el nombre dado y un
   * ID distinto al del tipo de cultivo del ID dado, si y solo
   * si existe en la base de datos subyacente
   * 
   * @param id
   * @param name
   * @return referencia a un objeto de tipo TypeCrop que
   *         representa al tipo de cultivo que tiene un ID
   *         distinto al ID dado y un nombre igual al nombre
   *         dado, si existe en la base de datos subyacente.
   *         En caso contrario, null.
   */
  private TypeCrop findRepeated(int id, String name) {
    /*
     * Esta consulta obtiene el tipo de cultivo que tiene su
     * nombre igual al nombre de un tipo de cultivo del
     * conjunto de tipos de cultivos en el que NO esta el
     * tipo de cultivo del ID dado
     */
    Query query = getEntityManager().createQuery("SELECT t FROM TypeCrop t WHERE (t.id != :cropTypeId AND UPPER(t.name) = UPPER(:cropTypeName))");
    query.setParameter("cropTypeId", id);
    query.setParameter("cropTypeName", name);

    TypeCrop typeCrop = null;

    try {
      typeCrop = (TypeCrop) query.getSingleResult();
    } catch (NoResultException e) {
      e.printStackTrace();
    }

    return typeCrop;
  }

  /**
   * Retorna true si y solo si en la base de datos subyacente
   * existe un tipo de cultivo con un nombre igual al nombre
   * dado y un ID distinto al ID dado
   * 
   * @param id
   * @param name
   * @return true si en la base de datos subyacente existe un
   *         tipo de cultivo con un nombre igual al nombre
   *         dado y un ID distinto al ID dado, en caso contrario
   *         false
   */
  public boolean checkRepeated(int id, String name) {
    return (findRepeated(id, name) != null);
  }

}
