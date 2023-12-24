package stateless;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.Collection;
import model.Month;

@Stateless
public class MonthServiceBean {

  /*
   * Instance variables
   */
  @PersistenceContext(unitName = "swcar")
  private EntityManager entityManager;

  public void setEntityManager(EntityManager localEntityManager) {
    entityManager = localEntityManager;
  }

  public Month create(Month newMonth) {
    entityManager.persist(newMonth);
    return newMonth;
  }

  public EntityManager getEntityManager() {
    return entityManager;
  }

  /**
   * Se utiliza el identificador del mes como numero del mes
   * para recuperar un mes en particular
   *
   * El id = 1 corresponde al mes numero 1 (Enero)
   * El id = 2 corresponde al mes numero 2 (Febrero)
   * El id = 3 corresponde al mes numero 3 (Marzo)
   * El id = 4 corresponde al mes numero 4 (Abril)
   * El id = 5 corresponde al mes numero 5 (Mayo)
   * El id = 6 corresponde al mes numero 6 (Junio)
   * El id = 7 corresponde al mes numero 7 (Julio)
   * El id = 8 corresponde al mes numero 8 (Agosto)
   * El id = 9 corresponde al mes numero 9 (Septiembre)
   * El id = 10 corresponde al mes numero 10 (Octubre)
   * El id = 11 corresponde al mes numero 11 (Noviembre)
   * El id = 12 corresponde al mes numero 12 (Diciembre)
   * 
   * Esto se debe a que en el archivo monthInserts.sql de la
   * ruta app/etc/sql los meses son cargados en orden
   * cronologico.
   *
   * @param id
   * @return mes correspondiente al identificador dado
   */
  public Month find(int id) {
    return getEntityManager().find(Month.class, id);
  }

  /**
   * @return referencia a un objeto de tipo Collection que
   * contiene todos los meses
   */
  public Collection<Month> findAll() {
    Query query = getEntityManager().createQuery("SELECT m FROM Month m ORDER BY m.id");
    return (Collection) query.getResultList();
  }

  /**
   * @param monthNumber
   * @return referencia a un objeto de tipo Month que representa
   * un mes del año
   */
  public Month getMonth(int monthNumber) {
    /*
     * Los meses en la clase Calendar van desde cero a once,
     * mientras que en la base de datos subyacente van desde
     * uno a doce. Por este motivo, si el valor del parametro
     * monthNumber proviene de un objeto de tipo Calendar, se
     * le debe sumar un uno para recuperar el mes correcto.
     */
    return find(monthNumber + 1);
  }

  /**
   * @param monthName
   * @return referencia a un objeto de tipo Collection que contiene
   * todos los meses que tienen un nombre que contiene parcial o
   * totalmente un nombre dado
   */
  public Collection<Month> findByName(String monthName) {
    StringBuffer queryStr = new StringBuffer("SELECT m FROM Month m");

    if (monthName != null) {
      queryStr.append(" WHERE (UPPER(m.name) LIKE :name)");
    }

    Query query = entityManager.createQuery(queryStr.toString());

    if (monthName != null) {
      query.setParameter("name", "%" + monthName.toUpperCase() + "%");
    }

    Collection<Month> operators = (Collection) query.getResultList();
    return operators;
  }

  /**
   * @param name
   * @return referencia a un objeto de tipo Month que representa
   * un mes si en la base de datos subyacente hay un mes con el
   * nombre dado, en caso contrario null
   */
  public Month find(String name) {
    Query query = entityManager.createQuery("SELECT m FROM Month m WHERE UPPER(m.name) = UPPER(:monthName)");
    query.setParameter("monthName", name);

    Month givenMonth = null;

    try {
      givenMonth = (Month) query.getSingleResult();
    } catch (NoResultException e) {
      e.printStackTrace();
    }

    return givenMonth;
  }

  /**
   * @param name
   * @return true si en la base de datos subyacente existe
   * un mes con el nombre dado, en caso contrario false
   */
  public boolean checkExistence(String name) {
    /*
     * Si el nombre del mes tiene el valor null, se retorna false,
     * ya que realizar la busqueda de un mes con un nombre con este
     * valor es similar a buscar un mes inexistente en la base de
     * datos subyacente.
     * 
     * Con este control se evita realizar una consulta a la base
     * de datos comparando el nombre de un mes con el valor null.
     * Si no se realiza este control y se realiza esta consulta a
     * la base de datos, ocurre la excepcion SQLSyntaxErrorException,
     * debido a que la comparacion de un atributo con el valor
     * null incumple la sintaxis del proveedor del motor de base
     * de datos.
     */
    if (name == null) {
      return false;
    }

    return (find(name) != null);
  }

}