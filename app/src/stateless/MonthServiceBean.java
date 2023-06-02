package stateless;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
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
   * @param id
   * @return mes correspondiente al identificador dado
   */
  public Month find(int id) {
    return getEntityManager().find(Month.class, id);
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

}
