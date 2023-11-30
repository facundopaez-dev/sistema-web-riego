package stateless;

import java.lang.Math;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.Collection;
import java.util.Map;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.NoResultException;
import model.Crop;
import model.Month;
import model.Region;
import util.UtilDate;

@Stateless
public class CropServiceBean {

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
   * de tipo Crop
   * 
   * @param newCrop
   * @return referencia a un objeto de tipo Crop
   */
  public Crop create(Crop newCrop) {
    getEntityManager().persist(newCrop);
    return newCrop;
  }

  /**
   * Elimina fisicamente un cultivo de la base de datos subyacente
   * 
   * @param id
   * @return referencia a un objeto de tipo Crop en caso de
   * eliminar fisicamente un cultivo de la base de datos
   * subyacente, en caso contrario null
   */
  public Crop physicallyRemove(int id) {
    Crop givenCrop = find(id);

    if (givenCrop != null) {
      getEntityManager().remove(givenCrop);
      return givenCrop;
    }

    return null;
  }

  /**
   * Elimina de forma logica de la base de datos subyacente el cultivo
   * que tiene el identificador dado
   *
   * @param  id
   * @return referencia a un objeto de tipo Crop en caso de eliminarse
   * de la base de datos subyacente el cultivo correspondiente al ID
   * dado, en caso contrario null (referencia a nada)
   */
  public Crop remove(int id) {
    Crop givenCrop = find(id);

    if (givenCrop != null) {
      givenCrop.setActive(false);
      return givenCrop;
    }

    return null;
  }

  /**
   * Modifica un cultivo mediante su ID
   * 
   * @param id
   * @param modifiedCrop
   * @return referencia a un objeto de tipo Crop que contiene las
   * modificaciones del cultivo pasado como parametro en caso de
   * encontrarse en la base de datos subyacente el cultivo con el
   * ID dado, null en caso contrario
   */
  public Crop modify(int id, Crop modifiedCrop) {
    Crop givenCrop = find(id);

    if (givenCrop != null) {
      givenCrop.setName(modifiedCrop.getName());
      givenCrop.setInitialStage(modifiedCrop.getInitialStage());
      givenCrop.setDevelopmentStage(modifiedCrop.getDevelopmentStage());
      givenCrop.setMiddleStage(modifiedCrop.getMiddleStage());
      givenCrop.setFinalStage(modifiedCrop.getFinalStage());
      givenCrop.setInitialKc(modifiedCrop.getInitialKc());
      givenCrop.setMiddleKc(modifiedCrop.getMiddleKc());
      givenCrop.setFinalKc(modifiedCrop.getFinalKc());
      givenCrop.setLifeCycle(modifiedCrop.getLifeCycle());
      givenCrop.setActive(modifiedCrop.getActive());
      givenCrop.setPlantingStartMonth(modifiedCrop.getPlantingStartMonth());
      givenCrop.setEndPlantingMonth(modifiedCrop.getEndPlantingMonth());
      givenCrop.setTypeCrop(modifiedCrop.getTypeCrop());
      givenCrop.setRegion(modifiedCrop.getRegion());
      return givenCrop;
    }

    return null;
  }

  /**
   * Determina si dos cultivos son iguales comparando el nombre, el
   * mes de inicio de siembra, el mes de fin de siembra y la region
   * de ambos. Retorna true si y solo si dos cultivos tienen el mismo
   * nombre, el mismo mes de inicio de siembra, el mismo mes de fin
   * de siembra y la misma region.
   * 
   * @param cropOne
   * @param cropTwo
   * @return true si dos cultivos son iguales, en caso contrario
   * false
   */
  public boolean equals(Crop cropOne, Crop cropTwo) {
    String nameOne = cropOne.getName();
    Month plantingStartMonthOne = cropOne.getPlantingStartMonth();
    Month endPlantingMonthOne = cropOne.getEndPlantingMonth();
    Region regionOne = cropOne.getRegion();

    String nameTwo = cropTwo.getName();
    Month plantingStartMonthTwo = cropTwo.getPlantingStartMonth();
    Month endPlantingMonthTwo = cropTwo.getEndPlantingMonth();
    Region regionTwo = cropTwo.getRegion();

    /*
     * Uno de los extremos es comparar el nombre de dos cultivos dados
     * en el caso en el que el mes de inicio de siembra, el mes de fin
     * de siembra y la region de ellos sean null
     */
    if (nameOne.equals(nameTwo)
        && plantingStartMonthOne == null && endPlantingMonthOne == null && regionOne == null
        && plantingStartMonthTwo == null && endPlantingMonthTwo == null && regionTwo == null) {
      return true;
    }

    /*
     * Uno de los casos de comparacion es que el mes de inicio de siembra
     * de dos cultivos dados sea distinto de null, y el mes de fin de
     * siembra y la region de ambos sean null
     */
    if (plantingStartMonthOne != null && endPlantingMonthOne == null && regionOne == null
        && plantingStartMonthTwo != null && endPlantingMonthTwo == null && regionTwo == null) {

      if (nameOne.equals(nameTwo) && plantingStartMonthOne.getName().equals(plantingStartMonthTwo.getName())) {
        return true;
      }

    } // End if

    /*
     * Uno de los casos de comparacion es que el mes de fin de siembra
     * de dos cultivos dados sea distinto de null, y el mes de inicio
     * de siembra y la region de ambos sean null
     */
    if (plantingStartMonthOne == null && endPlantingMonthOne != null && regionOne == null
        && plantingStartMonthTwo == null && endPlantingMonthTwo != null && regionTwo == null) {

      if (nameOne.equals(nameTwo) && endPlantingMonthOne.getName().equals(endPlantingMonthTwo.getName())) {
        return true;
      }

    } // End if

    /*
     * Uno de los casos de comparacion es que la region de dos cultivos
     * dados sea distinta de null, y el mes de inicio de siembra y el
     * mes de fin de siembra de ambos sean null
     */
    if (plantingStartMonthOne == null && endPlantingMonthOne == null && regionOne != null
        && plantingStartMonthTwo == null && endPlantingMonthTwo == null && regionTwo != null) {

      if (nameOne.equals(nameTwo) && regionOne.getName().equals(regionTwo.getName())) {
        return true;
      }

    } // End if

    /*
     * Uno de los casos de comparacion es que el mes de inicio de siembra
     * y el mes de fin de siembra de dos cultivos dados sean distintos de
     * null, y la region de ambos sea null
     */
    if (plantingStartMonthOne != null && endPlantingMonthOne != null && regionOne == null
        && plantingStartMonthTwo != null && endPlantingMonthTwo != null && regionTwo == null) {

      if (nameOne.equals(nameTwo)
          && plantingStartMonthOne.getName().equals(plantingStartMonthTwo.getName())
          && endPlantingMonthOne.getName().equals(endPlantingMonthTwo.getName())) {
        return true;
      }

    } // End if

    /*
     * Uno de los casos de comparacion es que el mes de inicio de siembra
     * y la region de dos cultivos dados sean distintos de null, y el mes
     * de fin de siembra de ambos sea null
     */
    if (plantingStartMonthOne != null && endPlantingMonthOne == null && regionOne != null
        && plantingStartMonthTwo != null && endPlantingMonthTwo == null && regionTwo != null) {

      if (nameOne.equals(nameTwo)
          && plantingStartMonthOne.getName().equals(plantingStartMonthTwo.getName())
          && regionOne.getName().equals(regionTwo.getName())) {
        return true;
      }

    } // End if

    /*
     * Uno de los casos de comparacion es que el mes de fin de siembra
     * y la region de dos cultivos dados sean distintos de null, y el
     * mes de inicio de siembra sea null
     */
    if (plantingStartMonthOne == null && endPlantingMonthOne != null && regionOne != null
        && plantingStartMonthTwo == null && endPlantingMonthTwo != null && regionTwo != null) {

      if (nameOne.equals(nameTwo)
          && endPlantingMonthOne.getName().equals(endPlantingMonthTwo.getName())
          && regionOne.getName().equals(regionTwo.getName())) {
        return true;
      }

    } // End if

    /*
     * Uno de los extremos es comparar el nombre, el mes de inicio de
     * siembra, el mes de fin de siembra y la region de dos cultivos
     * dados
     */
    if (plantingStartMonthOne != null && endPlantingMonthOne != null && regionOne != null
        && plantingStartMonthTwo != null && endPlantingMonthTwo != null && regionTwo != null) {

      if (nameOne.equals(nameTwo)
          && plantingStartMonthOne.getName().equals(plantingStartMonthTwo.getName())
          && endPlantingMonthOne.getName().equals(endPlantingMonthTwo.getName())
          && regionOne.getName().equals(regionTwo.getName())) {
        return true;
      }

    } // End if

    return false;
  }

  /**
   * @param crop
   * @return entero que representa el ciclo de vida (dias)
   * de un cultivo
   */
  public int calculateLifeCycle(Crop crop) {
    return (crop.getInitialStage() + crop.getDevelopmentStage() + crop.getMiddleStage() + crop.getFinalStage());
  }

  public Crop find(int id) {
    return getEntityManager().find(Crop.class, id);
  }

  /**
   * Retorna los cultivos que tienen un nombre que coincide con el
   * nombre dado, esten activos o inactivos (eliminados logicamente).
   * Este metodo es para el filtro implementado en las paginas web
   * de lista de datos que estan asociados a cultivos activos o
   * inactivos, como la pagina web de lista de balances hidricos,
   * por ejemplo.
   * 
   * @param cropName
   * @return referencia a un objeto de tipo Collection que contiene
   * todos los cultivos activos o inactivos (eliminados logicamente)
   * que tienen un nombre que coincide con el nombre dado
   */
  public Collection<Crop> findByNameTypeAhead(String cropName) {
    StringBuffer queryStr = new StringBuffer("SELECT c FROM Crop c");

    if (cropName != null) {
      queryStr.append(" WHERE (UPPER(c.name) LIKE :name)");
    }

    Query query = entityManager.createQuery(queryStr.toString() + " ORDER BY c.name");

    if (cropName != null) {
      query.setParameter("name", "%" + cropName.toUpperCase() + "%");
    }

    Collection<Crop> operators = (Collection) query.getResultList();
    return operators;
  }

  /**
   * Retorna los cultivos activos que tienen un nombre que coincide
   * con el nombre dado. Este metodo es para el ingreso del cultivo
   * en el formulario de creacion y modificacion de un dato que
   * esta asociado a un cultivo, como un registro de plantacion,
   * por ejemplo.
   * 
   * @param cropName
   * @return referencia a un objeto de tipo Collection que contiene
   * todos los cultivos activos que tienen un nombre que coincide con
   * el nombre dado
   */
  public Collection<Crop> findByNameActiveCrop(String cropName) {
    StringBuffer queryStr = new StringBuffer("SELECT c FROM Crop c");

    if (cropName != null) {
      queryStr.append(" WHERE (UPPER(c.name) LIKE :name AND c.active = TRUE)");
    }

    Query query = entityManager.createQuery(queryStr.toString() + " ORDER BY c.name");

    if (cropName != null) {
      query.setParameter("name", "%" + cropName.toUpperCase() + "%");
    }

    Collection<Crop> operators = (Collection) query.getResultList();
    return operators;
  }

  /**
   * Retorna el cultivo que tiene el nombre dado si y solo si
   * existe en la base de datos subyacente un cultivo con el
   * nombre dado
   * 
   * @param cropName
   * @return referencia a un objeto de tipo Crop que representa
   * el cultivo que tiene el nombre dado, si existe en la base
   * de datos subyacente. En caso contrario, retorna null.
   */
  public Crop findByName(String cropName) {
    Query query = getEntityManager().createQuery("SELECT c FROM Crop c WHERE UPPER(c.name) = UPPER(:cropGivenName)");
    query.setParameter("cropGivenName", cropName);

    Crop givenCrop = null;

    try {
      givenCrop = (Crop) query.getSingleResult();
    } catch (NoResultException e) {
      e.printStackTrace();
    }

    return givenCrop;
  }

  /**
   * @return referencia a un objeto de tipo Collection que
   * contiene todos los cultivos, tanto los eliminados
   * logicamente (inactivos) como los que no
   */
  public Collection<Crop> findAll() {
    Query query = getEntityManager().createQuery("SELECT c FROM Crop c ORDER BY c.id");
    return (Collection) query.getResultList();
  }

  /**
   * @return referencia a un objeto de tipo Collection que
   * contiene todos los cultivos que tienen el mismo nombre,
   * tanto los eliminados logicamente (inactivos) como los
   * que no
   */
  public Collection<Crop> findAllByName(String name) {
    Query query = getEntityManager().createQuery("SELECT c FROM Crop c WHERE (UPPER(c.name) = UPPER(:name)) ORDER BY c.id");
    query.setParameter("name", name);

    return (Collection) query.getResultList();
  }

  /**
   * @return referencia a un objeto de tipo Collection que
   * contiene todos los cultivos que tienen el mismo nombre,
   * tanto los eliminados logicamente (inactivos) como los
   * que no, pero que no tiene el cultivo del ID dado
   */
  public Collection<Crop> findAllByNameExceptId(int id, String name) {
    Query query = getEntityManager().createQuery("SELECT c FROM Crop c WHERE (c.id != :id AND UPPER(c.name) = UPPER(:name)) ORDER BY c.id");
    query.setParameter("id", id);
    query.setParameter("name", name);

    return (Collection) query.getResultList();
  }

  /**
   * Comprueba la existencia de un cultivo en la base de datos
   * subyacente. Retorna true si y solo si existe el cultivo
   * con el ID dado.
   * 
   * @param id
   * @return true si el cultivo con el ID dado existe en la
   * base de datos subyacente, false en caso contrario
   */
  public boolean checkExistence(int id) {
    return (getEntityManager().find(Crop.class, id) != null);
  }

  /**
   * Determina si un cultivo existe en la base de datos subyacente.
   * Retorna true si y solo si en la base de datos subyacente
   * existe un cultivo con el nombre, el mes de inicio de siembra,
   * el mes de fin de siembra y la region de un cultivo dado.
   * 
   * Hay que tener en cuenta que para usar este metodo primero
   * se debe realizar un control que verifique si el nombre del
   * cultivo pasado como argumento en la invocacion de este
   * metodo, es null. Esto se debe a que si se realiza una
   * consulta a la base de datos comparando el nombre de un
   * cultivo con el valor null, ocurre la excepcion SQLSyntax-
   * ErrorException, debido a que la comparacion de un atributo
   * con el valor null incumple la sintaxis del proveedor del
   * motor de base de datos.
   * 
   * @param crop
   * @return true si existe un cultivo en la base de datos
   * subaycente, en caso contrario false
   */
  public boolean checkExistence(Crop crop) {
    /*
     * Obtiene todos los cultivos que tienen el mismo nombre
     * de un cultivo dado, esten activos o inactivos (eliminados
     * logicamente)
     */
    Collection<Crop> crops = findAllByName(crop.getName());

    for (Crop currentCrop : crops) {

      if (equals(crop, currentCrop)) {
        return true;
      }

    }

    return false;
  }

  /**
   * Este metodo es para la modificacion de un cultivo. Cuando
   * se modifica un cultivo se debe evitar que el cultivo
   * modificado sea igual a otro cultivo. Esto es que se debe
   * evitar que tenga un nombre, un mes de inicio de siembra,
   * un mes de fin de siembra y una region iguales al nombre,
   * al mes de inicio de siembra, al mes de fin de siembra y
   * a la region de otro cultivo.
   * 
   * @param id
   * @param crop
   * @return true si en la base de datos subyacente existe un
   * cultivo con el nombre, el mes de inicio de siembra, el mes
   * de fin de siembra y la region de un cultivo dado
   */
  public boolean checkRepeated(int id, Crop crop) {
    /*
     * Obtiene todos los cultivos que tienen el mismo nombre
     * de un cultivo dado, esten activos o inactivos (eliminados
     * logicamente), excepto el cultivo del ID dado
     */
    Collection<Crop> crops = findAllByNameExceptId(id, crop.getName());

    for (Crop currentCrop : crops) {

      if (equals(crop, currentCrop)) {
        return true;
      }

    }

    return false;
  }

  /**
   * Calcula el kc (coeficiente de cultivo) de un cultivo en
   * funcion de su fecha de siembra y una fecha hasta dada.
   * Para esto se determina la cantidad de dias de vida de un
   * cultivo desde su fecha de siembra (incluida) hasta una
   * fecha hasta dada. En base a la cantidad de dias de vida de
   * un cultivo se determina la etapa en la que se encuentra
   * un cultivo en su ciclo de vida, y en base a la etapa se
   * retorna el kc de un cultivo.
   * 
   * Este metodo es para la clase de pruebas unitarias GetKcTest
   * y el metodo calculateEtsPastClimateRecords de las clases
   * PlantingRecordRestServlet y PlantingRecordManager.
   * 
   * @param crop
   * @param seedDate [fecha de siembra de un cultivo dado]
   * @param dateUntil
   * @return double que representa el kc (coeficiente de cultivo)
   * de un cultivo en funcion de la etapa de su ciclo de vida en
   * la que se encuentra tomando como referencia su fecha de siembra
   * y una fecha hasta dada
   */
  public double getKc(Crop crop, Calendar seedDate, Calendar dateUntil) {
    /*
     * A la diferencia de dias entre la fecha de siembra de
     * un cultivo y una fecha hasta, se le suma un uno para
     * incluir a la fecha de siembra en el resultado, ya que
     * esta cuenta como un dia de vida en la cantidad de dias
     * de vida transcurridos de un cultivo
     */
    int daysLife = UtilDate.calculateDifferenceBetweenDates(seedDate, dateUntil) + 1;
    return calculateKc(crop, daysLife);
  }

  /**
   * Calcula el kc (coficiente de cultivo) de un cultivo en
   * funcion de su fecha de siembra y la fecha actual.
   * Para esto se determina la cantidad de dias de vida de un
   * cultivo desde su fecha de siembra (incluida) hasta la
   * fecha actual. En base a la cantidad de dias de vida de
   * un cultivo se determina la etapa en la que se encuentra
   * un cultivo en su ciclo de vida, y en base a la etapa se
   * retorna el kc de un cultivo.
   * 
   * La fecha de siembra esta incluida en la cantidad de dias
   * de vida de un cultivo porque cuenta como un dia de vida
   * para un cultivo sembrado.
   *
   * @param crop
   * @param seedDate [fecha de siembra de un cultivo dado]
   * @return numero de punto flotante que representa el kc
   * (coeficiente de cultivo) de un cultivo en funcion de
   * la etapa de su ciclo de vida en la que se encuentra
   * teniendo en cuenta su fecha de siembra y la fecha
   * actual
   */
  public double getKc(Crop crop, Calendar seedDate) {
    /*
     * A la diferencia de dias entre la fecha de siembra de
     * un cultivo y la fecha actual, se le suma un uno para
     * incluir a la fecha de siembra en el resultado, ya que
     * esta cuenta como un dia de vida en la cantidad de dias
     * de vida transcurridos de un cultivo.
     */
    int daysLife = UtilDate.calculateDifferenceBetweenDates(seedDate, UtilDate.getCurrentDate()) + 1;
    return calculateKc(crop, daysLife);
  }

  /**
   * Retorna el kc (coeficiente del cultivo) de un cultivo en
   * funcion de la etapa de su ciclo de vida en la que se
   * encuentre. Para ello utiliza las cuatro etapas del
   * ciclo de vida de un cultivo y la cantidad de dias de
   * vida que tiene desde su fecha de siembra hasta la fecha
   * actual, incluidas.
   * 
   * Si la cantidad de dias de vida de un cultivo es menor o
   * mayor a su ciclo de vida, este metodo retorna 0.0 como
   * kc. En este caso lo mejor es hacer que el metodo lance
   * una excepcion, pero por falta de tiempo no implemento
   * esto.
   * 
   * @param givenCrop
   * @param elapsedDaysLifeCrop
   * @return kc (coeficiente de cultivo) de un cultivo en
   * funcion de la etapa de su ciclo de vida en la que se
   * encuentre, si la cantidad de dias de vida del cultivo
   * no es mayor ni menor a su ciclo de vida. En caso
   * contrario, 0.0.
   */
  private double calculateKc(Crop givenCrop, int elapsedDaysLifeCrop) {
    /*
     * Si la cantidad de dias de vida transcurridos de un cultivo
     * desde su fecha de siembra hasta la fecha actual, esta dentro
     * de los limites de la etapa inicial del ciclo de vida de un
     * cultivo, se retorna el kc (coficiente de cultivo) inicial 
     */
    if ((getLowerLimitInitialStage() <= elapsedDaysLifeCrop) && (elapsedDaysLifeCrop <= getUpperLimitInitialStage(givenCrop))) {
      return givenCrop.getInitialKc();
    }

    /*
     * Si la cantidad de dias de vida transcurridos de un cultivo
     * desde su fecha de siembra hasta la fecha actual, esta dentro
     * de los limites de la etapa de desarrollo del ciclo de vida
     * de un cultivo, se retorna el kc (coficiente de cultivo) medio
     */
    if ((getLowerLimitDevelopmentStage(givenCrop) <= elapsedDaysLifeCrop) && (elapsedDaysLifeCrop <= getUpperLimitDevelopmentStage(givenCrop))) {
      return givenCrop.getMiddleKc();
    }

    /*
     * Si la cantidad de dias de vida transcurridos de un cultivo
     * desde su fecha de siembra hasta la fecha actual, esta dentro
     * de los limites de la etapa media del ciclo de vida de un
     * cultivo, se retorna el kc (coficiente de cultivo) medio
     */
    if ((getLowerLimitMiddleStage(givenCrop) <= elapsedDaysLifeCrop) && (elapsedDaysLifeCrop <= getUpperLimitMiddleStage(givenCrop))) {
      return givenCrop.getMiddleKc();
    }

    /*
     * Si la cantidad de dias de vida transcurridos de un cultivo
     * desde su fecha de siembra hasta la fecha actual, esta dentro
     * de los limites de la etapa final del ciclo de vida de un
     * cultivo, se retorna el kc (coficiente de cultivo) final
     */
    if ((getLowerLimitFinalStage(givenCrop) <= elapsedDaysLifeCrop) && (elapsedDaysLifeCrop <= getUpperLimitFinalStage(givenCrop))) {
      return givenCrop.getFinalKc();
    }

    return 0.0;
  }

  /**
   * La figura 25 de la pagina 100 del libro "Evapotranspiracion
   * del cultivo" de la FAO muestra las cuatro etapas del ciclo
   * de vida de un cultivo. Primero esta la etapa inicial, segundo
   * la etapa de desarrolo, tercero la etapa media (mitad de
   * temporada) y cuarto la etapa final.
   * 
   * Este es el motivo por el cual el limite inferior de la
   * etapa inicial es uno (un dia) y su limite superior es la
   * cantidad de dias que dura esta etapa.
   * 
   * @return entero que representa el primer dia de la etapa
   * inicial del ciclo de vida de un cultivo
   */
  private int getLowerLimitInitialStage() {
    return 1;
  }

  /**
   * @param givenCrop
   * @return entero que representa el ultimo dia de la
   * etapa inicial del ciclo de vida de un cultivo
   */
  private int getUpperLimitInitialStage(Crop givenCrop) {
    return givenCrop.getInitialStage();
  }

  /**
   * La figura 25 de la pagina 100 del libro "Evapotranspiracion
   * del cultivo" de la FAO muestra las cuatro etapas del ciclo
   * de vida de un cultivo. Primero esta la etapa inicial, segundo
   * la etapa de desarrolo, tercero la etapa media (mitad de
   * temporada) y cuarto la etapa final.
   * 
   * Este es el motivo por el cual el limite inferior de la
   * etapa de desarrollo es el limite superior de la etapa
   * inicial mas uno, y su limite superior es la suma entre la
   * cantidad de dias que dura la etapa inicial y la cantidad
   * de dias que dura la etapa de desarrollo.
   * 
   * @param givenCrop
   * @return entero que representa el primer dia de la etapa
   * de desarrollo del ciclo de vida de un cultivo
   */
  private int getLowerLimitDevelopmentStage(Crop givenCrop) {
    return getUpperLimitInitialStage(givenCrop) + 1;
  }

  /**
   * @param givenCrop
   * @return entero que representa el ultimo dia de la etapa
   * de desarrollo del ciclo de vida de un cultivo
   */
  private int getUpperLimitDevelopmentStage(Crop givenCrop) {
    return (givenCrop.getInitialStage() + givenCrop.getDevelopmentStage());
  }

  /**
   * La figura 25 de la pagina 100 del libro "Evapotranspiracion
   * del cultivo" de la FAO muestra las cuatro etapas del ciclo
   * de vida de un cultivo. Primero esta la etapa inicial, segundo
   * la etapa de desarrolo, tercero la etapa media (mitad de
   * temporada) y cuarto la etapa final.
   * 
   * Este es el motivo por el cual el limite inferior de la
   * etapa media es el limite superior de la etapa de desarrollo
   * mas uno, y su limite superior es la suma entre la cantidad
   * de dias que dura la etapa inicial, la cantidad de dias que
   * dura la etapa de desarrollo y la cantidad de dias que dura
   * la etapa media.
   * 
   * @param givenCrop
   * @return entero que representa el primer dia de la etapa
   * media del ciclo de vida de un cultivo
   */
  private int getLowerLimitMiddleStage(Crop givenCrop) {
    return getUpperLimitDevelopmentStage(givenCrop) + 1;
  }

  /**
   * @param givenCrop
   * @return entero que representa el ultimo dia de la etapa
   * media del ciclo de vida de un cultivo
   */
  private int getUpperLimitMiddleStage(Crop givenCrop) {
    return (givenCrop.getInitialStage() + givenCrop.getDevelopmentStage() + givenCrop.getMiddleStage());
  }

  /**
   * La figura 25 de la pagina 100 del libro "Evapotranspiracion
   * del cultivo" de la FAO muestra las cuatro etapas del ciclo
   * de vida de un cultivo. Primero esta la etapa inicial, segundo
   * la etapa de desarrolo, tercero la etapa media (mitad de
   * temporada) y cuarto la etapa final.
   * 
   * Este es el motivo por el cual el limite inferior de la
   * etapa final es el limite superior de la etapa media mas uno,
   * y su limite superior es la suma entre la cantidad de dias que
   * dura la etapa inicial, la cantidad de dias que dura la etapa
   * de desarrollo, la cantidad de dias que dura la etapa media y
   * la cantidad de dias que dura la etapa final.
   * 
   * @param givenCrop
   * @return entero que representa el primer dia de la etapa
   * final del ciclo de vida de un cultivo
   */
  private int getLowerLimitFinalStage(Crop givenCrop) {
    return getUpperLimitMiddleStage(givenCrop) + 1;
  }

  /**
   * @param givenCrop
   * @return entero que representa el ultimo dia de la etapa
   * final del ciclo de vida de un cultivo
   */
  private int getUpperLimitFinalStage(Crop givenCrop) {
    return (givenCrop.getInitialStage() + givenCrop.getDevelopmentStage() + givenCrop.getMiddleStage() + givenCrop.getFinalStage());
  }

  /**
   * Calcula la fecha de cosecha de un cultivo a partir de su fecha de
   * siembra y su ciclo de vida
   * 
   * @param seedDate
   * @param plantedCrop
   * @return referencia a un objeto de tipo Calendar que contiene
   * la fecha de cosecha de un cultivo, la cual es calculada a partir
   * de la fecha de siembra y el ciclo de vida del mismo
   */
  public Calendar calculateHarvestDate(Calendar seedDate, Crop plantedCrop) {
    Calendar harvestDate = Calendar.getInstance();
    int daysYear = 365;

    /*
     * Dias de vida del cultivo sembrado a partir de su fecha
     * de siembra
     */
    int daysCropLife = seedDate.get(Calendar.DAY_OF_YEAR) + plantedCrop.getLifeCycle();

    /*
     * Esta variable representa los dias de vida del cultivo
     * en el año en el que se lo siembra. Esta variable y la
     * variable daysLifeFollowingYear son necesarias para
     * calcular la fecha de cosecha en el caso en el que
     * la cantidad de dias de vida del cultivo calculada
     * a partir de su fecha de siembra, supera la cantidad
     * de dias del año
     */
    int daysLifePlantingYear = 0;

    /*
     * Esta variable representa los dias de vida del cultivo
     * en el año siguiente al año en el que se lo siembra.
     * Esta variable recibe un valor mayor a cero si la cantidad
     * de dias de vida del cultivo sembrado calculada a partir de
     * su fecha de siembra, es mayor a la cantidad de dias del año.
     */
    int daysLifeFollowingYear = 0;

    /*
     * Si el año de la fecha de siembra es bisiesto, la cantidad
     * de dias en el año que se tiene que utilizar para calcular
     * la fecha de cosecha de un cultivo sembrado es 366
     */
    if (UtilDate.isLeapYear(seedDate.get(Calendar.YEAR))) {
      daysYear = 366;
    }

    /*
     * Si la cantidad de dias de vida del cultivo sembrado calculada a
     * partir de su fecha de siembra, supera la cantidad de dias del año,
     * la fecha de cosecha tiene un año mas que la fecha de siembra y su
     * dia se calcula de la siguiente manera:
     * 
     * dias de vida del cultivo en el año de la fecha de siembra =
     *  dias del año - numero de dia de la fecha de siembra en el año + 1 (*)
     * 
     * dias de vida del cultivo en el año siguiente a la fecha de siembra =
     *  ciclo de vida del cultivo sembrado - dias de vida del cultivo en el
     *  año de la fecha de siembra
     * 
     * Con el segundo valor se establece el dia en el año de la fecha
     * de cosecha.
     * 
     * (*) El "+ 1" se debe a que para calcular la fecha de cosecha se cuenta
     * desde el dia de la fecha de siembra, y para incluir a este dia en el
     * calculo de la cantidad de dias de vida del cultivo en el año de la fecha
     * de siembra, se suma un uno.
     */
    if (daysCropLife > daysYear) {
      daysLifePlantingYear = daysYear - seedDate.get(Calendar.DAY_OF_YEAR) + 1;
      daysLifeFollowingYear = plantedCrop.getLifeCycle() - daysLifePlantingYear;
      harvestDate.set(Calendar.DAY_OF_YEAR, daysLifeFollowingYear);
      harvestDate.set(Calendar.YEAR, seedDate.get(Calendar.YEAR) + 1);
      return harvestDate;
    }

    /*
     * Si la cantidad de dias de vida del cultivo sembrado calculada a
     * partir de su fecha de siembra, NO supera la cantidad de dias del
     * año, la fecha de cosecha tiene el mismo año que la fecha de
     * siembra y el dia de la fecha de cosecha es el numero de dia de
     * la fecha de siembra en el año mas los dias de vida del cultivo
     * en el año en el que se lo siembra menos un dia (*).
     * 
     * (*) El "- 1" se debe a que para calcular la fecha de cosecha se
     * cuenta desde el dia de la fecha de siembra y no desde el dia
     * siguiente al mismo.
     */
    harvestDate.set(Calendar.DAY_OF_YEAR, daysCropLife - 1);
    harvestDate.set(Calendar.YEAR, seedDate.get(Calendar.YEAR));
    return harvestDate;
  }

  /**
   * Retorna el menor ciclo de vida del ciclo de vida de
   * los cultivos si y solo si hay cultivos registrados
   * en la base de datos subyacente. En caso contrario,
   * esta consulta retornara el valor null, lo cual,
   * generara problemas.
   * 
   * @return menor ciclo de vida del ciclo de vida de los
   * cultivos registrados en la base de datos subyacente
   */
  public int findShortestLifeCycle() {
    Query query = getEntityManager().createQuery("SELECT MIN(c.lifeCycle) FROM Crop c");
    return (int) query.getSingleResult();
  }

  public Page<Crop> findByPage(Integer page, Integer cantPerPage, Map<String, String> parameters) {
    // Genero el WHERE dinámicamente
    StringBuffer where = new StringBuffer(" WHERE 1=1");
    if (parameters != null)
    for (String param : parameters.keySet()) {
      Method method;
      try {
        method = Crop.class.getMethod("get" + capitalize(param));
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
    .createQuery("SELECT COUNT(e.id) FROM " + Crop.class.getSimpleName() + " e" + where.toString());

    // Pagino
    Query query = getEntityManager().createQuery("FROM " + Crop.class.getSimpleName() + " e" + where.toString());
    query.setMaxResults(cantPerPage);
    query.setFirstResult((page - 1) * cantPerPage);
    Integer count = ((Long) countQuery.getSingleResult()).intValue();
    Integer lastPage = (int) Math.ceil((double) count / (double) cantPerPage);

    // Armo respuesta
    Page<Crop> resultPage = new Page<Crop>(page, count, page > 1 ? page - 1 : page,
    page > lastPage ? page + 1 : lastPage, lastPage, query.getResultList());
    return resultPage;
  }

  private String capitalize(final String line) {
    return Character.toUpperCase(line.charAt(0)) + line.substring(1);
  }

}
