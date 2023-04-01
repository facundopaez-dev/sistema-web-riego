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
   * Elimina de forma logica de la base de datos subyacente el cultivo
   * que tiene el identificador dado
   *
   * @param  id
   * @return referencia a cultivo en caso de que haya sido eliminado,
   * referencia a nada (null) en caso contrario
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
      givenCrop.setActive(modifiedCrop.getActive());
      givenCrop.setTypeCrop(modifiedCrop.getTypeCrop());
      return givenCrop;
    }

    return null;
  }

  public Crop find(int id) {
    return getEntityManager().find(Crop.class, id);
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
   * Retorna el cultivo que tiene el nombre dado y un ID
   * distinto al del cultivo del ID dado, si y solo si existe
   * en la base de datos subyacente
   * 
   * @param id
   * @return referencia a un objeto de tipo Crop que representa
   * al cultivo que tiene un ID distinto al ID dado y un nombre
   * igual al nombre dado, si existe en la base de datos
   * subyacente. En caso contrario, null.
   */
  private Crop findRepeated(int id, String name) {
    /*
     * Esta consulta obtiene el cultivo que tiene su nombre
     * igual al nombre de un cultivo del conjunto de cultivos
     * en el que NO esta el cultivo del ID dado
     */
    Query query = getEntityManager().createQuery("SELECT c FROM Crop c WHERE (c.id != :cropId AND UPPER(c.name) = UPPER(:cropName))");
    query.setParameter("cropId", id);
    query.setParameter("cropName", name);

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
   * contiene todos los cultivos activos
   */
  public Collection<Crop> findAllActive() {
    Query query = getEntityManager().createQuery("SELECT c FROM Crop c WHERE c.active = TRUE ORDER BY c.id");
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
   * Retorna true si y solo si existe un cultivo con el nombre
   * dado en la base de datos subyacente
   * 
   * @param cropName
   * @return true si existe el cultivo con el nombre dado en
   * la base de datos subyacente, false en caso contrario.
   * Tambien retorna false en el caso en el que el argumento
   * tiene el valor null.
   */
  public boolean checkExistence(String cropName) {
    /*
     * Si el nombre del cultivo tiene el valor null, se retorna
     * false, ya que realizar la busqueda de un cultivo con un
     * nombre con este valor es similar a buscar un cultivo
     * inexistente en la base de datos subyacente.
     * 
     * Con este control se evita realizar una consulta a la base
     * de datos comparando el nombre del cultivo con el valor null.
     * Si no se realiza este control y se realiza esta consulta a
     * la base de datos, ocurre la excepcion SQLSyntaxErrorException,
     * debido a que la comparacion de un atributo con el valor
     * null incumple la sintaxis del proveedor del motor de base
     * de datos.
     */
    if (cropName == null) {
      return false;
    }

    return (findByName(cropName) != null);
  }

  /**
   * Retorna true si y solo si en la base de datos subyacente
   * existe un cultivo con un nombre igual al nombre dado
   * y un ID distinto al ID dado
   * 
   * @param id
   * @return true si en la base de datos subyacente existe un
   * cultivo con un nombre igual al nombre dado y un ID
   * distinto al ID dado, en caso contrario false
   */
  public boolean checkRepeated(int id, String name) {
    return (findRepeated(id, name) != null);
  }

  /**
   * *** NOTA ***
   * Este metodo es unicamente para la clase de prueba
   * unitaria KcTest, con lo cual no sera utilizado
   * en la version final del sistema, sino que es
   * unicamente para usarlo en una prueba untiaria
   * con el fin de verificar su correcto funcionamiento
   * *** FIN DE NOTA ***
   *
   * Devuelve el kc (coeficiente del cultivo) de un
   * cultivo dado en funcion de la etapa de vida
   * en la que se encuentre la cantidad de dias
   * que ha vivido desde su fecha de siembra
   * hasta la fecha actual
   * 
   * @param crop
   * @param seedDate [fecha de siembra del cultivo dado]
   * @param currentDate
   * @return kc (coeficiente del cultivo) de un cultivo
   * dado correspondiente a la etapa de vida en la
   * que se encuentre
   */
  public double getKc(Crop crop, Calendar seedDate, Calendar currentDate) {
    int daysLife = 0;

    /*
     * Si la fecha de siembra y la fecha actual son del mismo
     * año se calcula la diferencia de dias entre ambas fechas
     * sin tener en cuenta el año debido a que pertenecen al
     * mismo año y dicha diferencia es la cantidad de dias
     * de vida que ha vivido el cultivo desde su fecha
     * de siembra hasta la fecha actual
     */
    if (seedDate.get(Calendar.YEAR) == currentDate.get(Calendar.YEAR)) {
      daysLife = currentDate.get(Calendar.DAY_OF_YEAR) - seedDate.get(Calendar.DAY_OF_YEAR);
      return calculateKc(crop, daysLife);
    }

    /*
     * Si entre la fecha de siembra y la fecha actual hay un año
     * de diferencia (lo que significa que no son del mismo año
     * pero el año de la fecha actual esta a continuacion
     * del año de la fecha de siembra) se calcula la diferencia
     * de dias entre ambas de la siguiente forma:
     *
     * Cantidad de dias de vida = Numero del dia del año de la fecha
     * actual + (365 - numero del dia del año de la fecha de siembra + 1)
     */
    if (Math.abs(seedDate.get(Calendar.YEAR) - currentDate.get(Calendar.YEAR)) == 1) {
      daysLife = currentDate.get(Calendar.DAY_OF_YEAR) + (365 - seedDate.get(Calendar.DAY_OF_YEAR) + 1);
      return calculateKc(crop, daysLife);
    }

    /*
     * NOTE: Este calculo esta mal pero no tan mal, y esto se lo puede
     * ver en la clase de prueba unitaria llamada KcTest cuando al
     * tomate se le pone una fecha de siembra con el año 1995 y una
     * fecha actual con el año 2020 dando la diferencia en dias entre
     * ambas fechas distinta a la diferencia en dias entre ambas
     * fechas en una calculadora online de dias
     *
     * Para ver lo que dice el parrafo anterior, ejecutar la prueba
     * mencionada
     *
     * Si entre la fecha de siembra y la fecha actual hay mas de un año
     * de diferencia (lo que significa que no son del mismo año y que
     * entre ambas fechas hay mas de un año de distancia) se calcula
     * la diferencia de dias entre ambas fechas de la siguiente forma:
     *
     * Cantidad de dias de vida = (Año de la fecha actual - año de la
     * fecha de siembra) * 365 - (365 - Numero del dia en el año de la
     * fecha de siembra + 1) - (365 - Numero del dia en el año de la fecha actual)
     *
     * Se multiplica daysLife por 365 para evitar posibles errores, y ademas
     * si la diferencia entre ambas fechas es de mas de un año no tiene sentido
     * calcular la cantidad de dias de vida del cultivo dado porque hasta donde
     * se sabe ninguno cultivo mas de un año
     */
    if (Math.abs(seedDate.get(Calendar.YEAR) - currentDate.get(Calendar.YEAR)) > 1) {
      daysLife = ((Math.abs(seedDate.get(Calendar.YEAR) - currentDate.get(Calendar.YEAR))) * 365) - (365 - seedDate.get(Calendar.DAY_OF_YEAR) + 1) - (365 - currentDate.get(Calendar.DAY_OF_YEAR));
      return calculateKc(crop, (daysLife * 365));
    }

    return calculateKc(crop, daysLife);
  }

  /**
   * Devuelve el kc (coeficiente del cultivo) de un
   * cultivo dado en funcion de la etapa de vida
   * en la que se encuentre la cantidad de dias
   * que ha vivido desde su fecha de siembra
   * hasta la fecha actual
   *
   * @param  crop
   * @param  seedDate [fecha de siembra del cultivo dado]
   * @return kc (coeficiente del cultivo) de un cultivo
   * dado correspondiente a la etapa de vida en la
   * que se encuentre
   */
  public double getKc(Crop crop, Calendar seedDate) {
    int daysLife = 0;
    Calendar currentDate = Calendar.getInstance();

    /*
     * Si la fecha de siembra y la fecha actual son del mismo
     * año se calcula la diferencia de dias entre ambas fechas
     * sin tener en cuenta el año debido a que pertenecen al
     * mismo año y dicha diferencia es la cantidad de dias
     * de vida que ha vivido el cultivo desde su fecha
     * de siembra hasta la fecha actual
     */
    if (seedDate.get(Calendar.YEAR) == currentDate.get(Calendar.YEAR)) {
      daysLife = currentDate.get(Calendar.DAY_OF_YEAR) - seedDate.get(Calendar.DAY_OF_YEAR);
      return calculateKc(crop, daysLife);
    }

    /*
     * Si entre la fecha de siembra y la fecha actual hay un año
     * de diferencia (lo que significa que no son del mismo año
     * pero el año de la fecha actual esta a continuacion
     * del año de la fecha de siembra) se calcula la diferencia
     * de dias entre ambas fechas de la siguiente forma:
     *
     * Cantidad de dias de vida = Numero del dia del año de la fecha
     * actual + (365 - Numero del dia del año de la fecha de siembra + 1)
     */
    if (Math.abs(seedDate.get(Calendar.YEAR) - currentDate.get(Calendar.YEAR)) == 1) {
      daysLife = currentDate.get(Calendar.DAY_OF_YEAR) + (365 - seedDate.get(Calendar.DAY_OF_YEAR) + 1);
      return calculateKc(crop, daysLife);
    }


    /*
     * NOTE: Este calculo esta mal pero no tan mal, y esto se lo puede
     * ver en la clase de prueba unitaria llamada KcTest cuando al
     * tomate se le pone una fecha de siembra con el año 1995 y una
     * fecha actual con el año 2020 dando la diferencia en dias entre
     * ambas fechas distinta a la diferencia en dias entre ambas
     * fechas en una calculadora online de dias
     *
     * Para ver lo que dice el parrafo anterior, ejecutar la prueba
     * mencionada
     *
     * Si entre la fecha de siembra y la fecha actual hay mas de un año
     * de diferencia (lo que significa que no son del mismo año y que
     * entre ambas fechas hay mas de un año de distancia) se calcula
     * la diferencia de dias entre ambas fechas de la siguiente forma:
     *
     * Cantidad de dias de vida = (Año de la fecha actual - año de la
     * fecha de siembra) * 365 - (365 - Numero del dia en el año de la
     * fecha de siembra + 1) - (365 - Numero del dia en el año de la fecha actual)
     *
     * Se multiplica daysLife por 365 para evitar posibles errores, y ademas
     * si la diferencia entre ambas fechas es de mas de un año no tiene sentido
     * calcular la cantidad de dias de vida del cultivo dado porque hasta donde
     * se sabe ninguno cultivo mas de un año
     */
    if (Math.abs(seedDate.get(Calendar.YEAR) - currentDate.get(Calendar.YEAR)) > 1) {
      daysLife = ((Math.abs(seedDate.get(Calendar.YEAR) - currentDate.get(Calendar.YEAR))) * 365) - (365 - seedDate.get(Calendar.DAY_OF_YEAR) + 1) - (365 - currentDate.get(Calendar.DAY_OF_YEAR));
      return calculateKc(crop, (daysLife * 365));
    }

    return calculateKc(crop, daysLife);
  }

  /**
   * Calcula el kc (coeficiente del cultivo) de un cultivo
   * dado verificando si la cantidad de dias de vida del mismo
   * esta dentro de una de sus tres etapas de vida
   *
   * Si la cantidad de dias de vida del cultivo dado no
   * estan dentro de ninguna de sus tres etapas de vida, este
   * metodo retorna como resultado un kc = 0.0
   *
   * La cantidad de dias de vida que ha vivido un cultivo
   * se calcula como la diferencia entre la fecha actual
   * y su fecha de siembra
   *
   * @param  crop
   * @param  daysLife [dias de vida del cultivo dado]
   * @return coeficiente del cultivo (kc) dado en funcion
   * de la etapa de vida en la que se encuentre, en caso
   * de que no este dentro de ninguna de sus tres etapas
   * de vida, este metodo retorna un kc = 0.0
   */
  private double calculateKc(Crop crop, int daysLife) {
    double zeroKc = 0.0;

    /*
     * Numero del dia en el cual comienza la etapa inicial
     * sumada a la etapa de desarrollo del cultivo dado
     */
    int initialDayInitialStage = 1;

    /*
     * Numero del dia en el cual comienza la segunda etapa
     * (la etapa media) de vida del cultivo dado
     */
    int initialDayMiddleStage = crop.getInitialStage() + crop.getDevelopmentStage() + 1;

    /*
     * Numero del dia en el cual comienza la tercera etapa
     * (etapa final) de vida del cultivo dado
     */
    int initialDayFinalStage = crop.getInitialStage() + crop.getDevelopmentStage() + crop.getMiddleStage() + 1;

    /*
     * El limite superior o ultimo dia de la etapa inicial
     * de un cultivo es igual a la suma de los dias de su
     * etapa incial mas los dias de su etapa de desarrollo
     *
     * Entre los integrantes del equipo de desarrollo se ha establecido
     * que las etapas inicial y desarrollo son una sola, y es por esto
     * que se suman los dias que duran ambas y que se toma la etapa
     * inicial como la suma de los dias que dura la etapa inicial mas
     * los dias que dura la etapa de desarrollo, quedando la etapa
     * incial como la etapa incial mas la etapa de desarrollo
     */
    int upperLimitFirstStage = crop.getInitialStage() + crop.getDevelopmentStage();

    /*
     * El limite superior o ultimo dia de la etapa media
     * de un cultivo es igual a la suma de los dias de su
     * etapa inicial mas los dias de su etapa de desarrollo
     * mas los dias de su etapa media
     */
    int upperLimitSecondStage = crop.getInitialStage() + crop.getDevelopmentStage() + crop.getMiddleStage();

    /*
     * El limite superior o ultimo dia de la etapa final
     * de un cultivo es igual a la suma de los dias de su
     * etapa incial mas los dias de su etapa de desarrollo
     * mas los dias de su etapas media mas los dias de su
     * etapa final
     */
    int upperLimitThirdStage = crop.getInitialStage() + crop.getDevelopmentStage() + crop.getMiddleStage() + crop.getFinalStage();

    /*
     * Si la cantidad de dias de vida del cultivo dado esta entre
     * entre el dia del comienzo de la etapa inicial y el maximo
     * de dias (o limite superior) que dura la misma (recordar que la etapa inicial
     * es la suma de la cantidad de dias que dura la etapa incial
     * mas la suma de la cantidad de dias que dura la etapa de
     * desarrollo), este metodo retorna el coeficiente incial (kc)
     * del cultivo dado
     *
     * Dicho en otras palabras, si la cantidad de dias de vida
     * del cultivo dado esta entre uno y el maximo de la cantidad
     * de dias resultante de la suma de los dias de la etapa inicial
     * y de la etapa de desarrollo, este metodo retorna el coeficiente
     * inicial (kc) del cultivo dado
     *
     * Entre los integrantes del equipo de desarrollo se ha establecido
     * que las etapas inicial y desarrollo son una sola, y es por esto
     * que se suman los dias que duran ambas, y por ende la etapa inicial
     * es la etapa inicial mas la etapa de desarrollo
     */
    if ((initialDayInitialStage <= daysLife) && (daysLife <= upperLimitFirstStage)) {
      return crop.getInitialKc();
    }

    /*
     * Si la cantidad de dias de vida del cultivo dado esta entre el dia
     * del comienzo de la etapa media y el maximo de dias (o limite superior) que dura la
     * misma (el cual resulta de sumar los dias que duran las
     * etapas inicial, desarrollo y media), este metodo retorna el
     * coeficiente medio (kc) del cultivo dado
     */
    if ((initialDayMiddleStage <= daysLife) && (daysLife <= upperLimitSecondStage)) {
      return crop.getMiddleKc();
    }

    /*
     * Si la cantidad de dias de vida del cultivo dado esta entre el dia
     * del comienzo de la etapa final y el maximo de dias (o limite superior) que dura la
     * misma (el cual resulta de sumar los dias que duran las etapas
     * incial, desarrollo, media y final), este metodo retorna el
     * coeficiente final (kc) del cultivo dado
     */
    if ((initialDayFinalStage <= daysLife) && (daysLife <= upperLimitThirdStage)) {
      return crop.getFinalKc();
    }

    /*
     * Si la cantidad de dias de vida del cultivo dado no esta
     * dentro de ninguna de las tres etapas del mismo, este
     * metodo retorna un coeficiente del cultivo (kc) igual a 0.0
     */
    return zeroKc;
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
