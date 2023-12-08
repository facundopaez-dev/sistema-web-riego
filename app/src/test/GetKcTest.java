import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.Ignore;
import java.util.Calendar;
import java.lang.Math;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import stateless.CropServiceBean;
import util.UtilDate;
import model.Crop;

/*
 * Para ejecutar correctamente las pruebas unitarias de esta
 * clase es necesario ejecutar primero el comando "ant all"
 * (sin las comillas), ya que este carga la base de datos
 * subyacente con los datos necesarios para la ejecucion
 * de dichas pruebas
 */
public class GetKcTest {
  private static EntityManager entityManager;
  private static EntityManagerFactory entityMangerFactory;

  private static CropServiceBean cropService;

  private static final int JANUARY = 0;
  private static final int FEBRUARY = 1;
  private static final int MARCH = 2;
  private static final int APRIL = 3;
  private static final int MAY = 4;
  private static final int JUNE = 5;
  private static final int JULY = 6;
  private static final int AUGUST = 7;
  private static final int SEPTEMBER = 8;
  private static final int OCTOBER = 9;
  private static final int NOVEMBER = 10;
  private static final int DECEMBER = 11;

  @BeforeClass
  public static void preTest() {
    entityMangerFactory = Persistence.createEntityManagerFactory("swcar");
    entityManager = entityMangerFactory.createEntityManager();

    cropService = new CropServiceBean();
    cropService.setEntityManager(entityManager);
  }

  @Test
  public void testOne() {
    System.out.println("************************************** Prueba uno del metodo getKc **************************************");
    System.out.println("El metodo getKc de la clase CropServiceBean retorna el kc (coeficiente de cultivo) de un cultivo en base");
    System.out.println("a la cantidad de dias que ha vivido desde su fecha de siembra hasta la fecha actual.");
    System.out.println();
    System.out.println("En esta prueba se demuestra que el metodo getKc retorna el coeficiente correcto para un cultivo que esta");
    System.out.println("en la etapa inicial de su ciclo de vida.");
    System.out.println();

    Crop givenCrop = cropService.find("Tomate");

    System.out.println("* Cultivo de prueba");
    System.out.println(givenCrop);
    System.out.println();

    /*
     * Fechas a partir de las cuales se obtiene el
     * coeficiente del cultivo de prueba
     */
    Calendar seedDate = Calendar.getInstance();
    seedDate.set(Calendar.DAY_OF_MONTH, 27);
    seedDate.set(Calendar.MONTH, NOVEMBER);
    seedDate.set(Calendar.YEAR, 2019);

    Calendar givenDate = Calendar.getInstance();
    givenDate.set(Calendar.DAY_OF_MONTH, 15);
    givenDate.set(Calendar.MONTH, DECEMBER);
    givenDate.set(Calendar.YEAR, 2019);

    System.out.println("* Fechas a partir de las cuales se obtiene el coeficiente del " + givenCrop.getName());
    System.out.println("Fecha de siembra: " + UtilDate.formatDate(seedDate));
    System.out.println("Fecha actual: " + UtilDate.formatDate(givenDate));
    System.out.println();

    System.out.println(
        "Cantidad de dias de vida del " + givenCrop.getName() + " desde su fecha de siembra hasta la fecha actual: "
            + getDaysLife(seedDate, givenDate));
    System.out.println();

    System.out.println("* Periodo de cada etapa del " + givenCrop.getName());
    System.out.println("Etapa inicial: dia " + getLowerLimitInitialStage()
        + " a dia " + getUpperLimitInitialStage(givenCrop));
    System.out.println("Etapa de desarrollo: dia " + getLowerLimitDevelopmentStage(givenCrop) + " a dia "
        + getUpperLimitDevelopmentStage(givenCrop));
    System.out.println("Etapa media: dia " + getLowerLimitMiddleStage(givenCrop) + " a dia "
        + getUpperLimitMiddleStage(givenCrop));
    System.out.println("Etapa final: dia " + getLowerLimitFinalStage(givenCrop) + " a dia "
        + getUpperLimitFinalStage(givenCrop));
    System.out.println();

    /*
     * Seccion de prueba
     */
    double expectedResult = 0.6;
    double kc = cropService.getKc(givenCrop, seedDate, givenDate);

    System.out.println("* Seccion de prueba *");
    System.out.println("Kc esperado: " + expectedResult);
    System.out.println("Kc devuelto por el metodo getKc: " + kc);
    System.out.println();

    assertEquals(expectedResult, kc, 1e-8);

    System.out.println("- Prueba pasada satisfactoriamente");
  }

  @Test
  public void testTwo() {
    System.out.println("************************************** Prueba dos del metodo getKc **************************************");
    System.out.println("El metodo getKc de la clase CropServiceBean retorna el kc (coeficiente de cultivo) de un cultivo en base");
    System.out.println("a la cantidad de dias que ha vivido desde su fecha de siembra hasta la fecha actual.");
    System.out.println();
    System.out.println("En esta prueba se demuestra que el metodo getKc retorna el coeficiente correcto para un cultivo que esta");
    System.out.println("en la etapa de desarrollo de su ciclo de vida.");
    System.out.println();

    Crop givenCrop = cropService.find("Tomate");

    System.out.println("* Cultivo de prueba");
    System.out.println(givenCrop);
    System.out.println();

    /*
     * Fechas a partir de las cuales se obtiene el
     * coeficiente del cultivo de prueba
     */
    Calendar seedDate = Calendar.getInstance();
    seedDate.set(Calendar.DAY_OF_MONTH, 27);
    seedDate.set(Calendar.MONTH, NOVEMBER);
    seedDate.set(Calendar.YEAR, 2019);

    Calendar givenDate = Calendar.getInstance();
    givenDate.set(Calendar.DAY_OF_MONTH, 5);
    givenDate.set(Calendar.MONTH, JANUARY);
    givenDate.set(Calendar.YEAR, 2020);

    System.out.println("* Fechas a partir de las cuales se obtiene el coeficiente del " + givenCrop.getName());
    System.out.println("Fecha de siembra: " + UtilDate.formatDate(seedDate));
    System.out.println("Fecha actual: " + UtilDate.formatDate(givenDate));
    System.out.println();

    System.out.println(
        "Cantidad de dias de vida del " + givenCrop.getName() + " desde su fecha de siembra hasta la fecha actual: "
            + getDaysLife(seedDate, givenDate));
    System.out.println();

    System.out.println("* Periodo de cada etapa del " + givenCrop.getName());
    System.out.println("Etapa inicial: dia " + getLowerLimitInitialStage()
        + " a dia " + getUpperLimitInitialStage(givenCrop));
    System.out.println("Etapa de desarrollo: dia " + getLowerLimitDevelopmentStage(givenCrop) + " a dia "
        + getUpperLimitDevelopmentStage(givenCrop));
    System.out.println("Etapa media: dia " + getLowerLimitMiddleStage(givenCrop) + " a dia "
        + getUpperLimitMiddleStage(givenCrop));
    System.out.println("Etapa final: dia " + getLowerLimitFinalStage(givenCrop) + " a dia "
        + getUpperLimitFinalStage(givenCrop));
    System.out.println();

    /*
     * Seccion de prueba
     */
    double expectedResult = 1.15;
    double kc = cropService.getKc(givenCrop, seedDate, givenDate);

    System.out.println("* Seccion de prueba *");
    System.out.println("Kc esperado: " + expectedResult);
    System.out.println("Kc devuelto por el metodo getKc: " + kc);
    System.out.println();

    assertEquals(expectedResult, kc, 1e-8);

    System.out.println("- Prueba pasada satisfactoriamente");
  }

  @Test
  public void testThree() {
    System.out.println("************************************** Prueba tres del metodo getKc **************************************");
    System.out.println("El metodo getKc de la clase CropServiceBean retorna el kc (coeficiente de cultivo) de un cultivo en base");
    System.out.println("a la cantidad de dias que ha vivido desde su fecha de siembra hasta la fecha actual.");
    System.out.println();
    System.out.println("En esta prueba se demuestra que el metodo getKc retorna el coeficiente correcto para un cultivo que esta");
    System.out.println("en la etapa media de su ciclo de vida.");
    System.out.println();

    Crop givenCrop = cropService.find("Tomate");

    System.out.println("* Cultivo de prueba");
    System.out.println(givenCrop);
    System.out.println();

    /*
     * Fechas a partir de las cuales se obtiene el
     * coeficiente del cultivo de prueba
     */
    Calendar seedDate = Calendar.getInstance();
    seedDate.set(Calendar.DAY_OF_MONTH, 27);
    seedDate.set(Calendar.MONTH, NOVEMBER);
    seedDate.set(Calendar.YEAR, 2019);

    Calendar givenDate = Calendar.getInstance();
    givenDate.set(Calendar.DAY_OF_MONTH, 1);
    givenDate.set(Calendar.MONTH, MARCH);
    givenDate.set(Calendar.YEAR, 2020);

    System.out.println("* Fechas a partir de las cuales se obtiene el coeficiente del " + givenCrop.getName());
    System.out.println("Fecha de siembra: " + UtilDate.formatDate(seedDate));
    System.out.println("Fecha actual: " + UtilDate.formatDate(givenDate));
    System.out.println();

    System.out.println(
        "Cantidad de dias de vida del " + givenCrop.getName() + " desde su fecha de siembra hasta la fecha actual: "
            + getDaysLife(seedDate, givenDate));
    System.out.println();

    System.out.println("* Periodo de cada etapa del " + givenCrop.getName());
    System.out.println("Etapa inicial: dia " + getLowerLimitInitialStage()
        + " a dia " + getUpperLimitInitialStage(givenCrop));
    System.out.println("Etapa de desarrollo: dia " + getLowerLimitDevelopmentStage(givenCrop) + " a dia "
        + getUpperLimitDevelopmentStage(givenCrop));
    System.out.println("Etapa media: dia " + getLowerLimitMiddleStage(givenCrop) + " a dia "
        + getUpperLimitMiddleStage(givenCrop));
    System.out.println("Etapa final: dia " + getLowerLimitFinalStage(givenCrop) + " a dia "
        + getUpperLimitFinalStage(givenCrop));
    System.out.println();

    /*
     * Seccion de prueba
     */
    double expectedResult = 1.15;
    double kc = cropService.getKc(givenCrop, seedDate, givenDate);

    System.out.println("* Seccion de prueba *");
    System.out.println("Kc esperado: " + expectedResult);
    System.out.println("Kc devuelto por el metodo getKc: " + kc);
    System.out.println();

    assertEquals(expectedResult, kc, 1e-8);

    System.out.println("- Prueba pasada satisfactoriamente");
  }

  @Test
  public void testFour() {
    System.out.println("************************************** Prueba cuatro del metodo getKc **************************************");
    System.out.println("El metodo getKc de la clase CropServiceBean retorna el kc (coeficiente de cultivo) de un cultivo en base");
    System.out.println("a la cantidad de dias que ha vivido desde su fecha de siembra hasta la fecha actual.");
    System.out.println();
    System.out.println("En esta prueba se demuestra que el metodo getKc retorna el coeficiente correcto para un cultivo que esta");
    System.out.println("en la etapa final de su ciclo de vida.");
    System.out.println();

    Crop givenCrop = cropService.find("Tomate");

    System.out.println("* Cultivo de prueba");
    System.out.println(givenCrop);
    System.out.println();

    /*
     * Fechas a partir de las cuales se obtiene el
     * coeficiente del cultivo de prueba
     */
    Calendar seedDate = Calendar.getInstance();
    seedDate.set(Calendar.DAY_OF_MONTH, 27);
    seedDate.set(Calendar.MONTH, NOVEMBER);
    seedDate.set(Calendar.YEAR, 2019);

    Calendar givenDate = Calendar.getInstance();
    givenDate.set(Calendar.DAY_OF_MONTH, 9);
    givenDate.set(Calendar.MONTH, APRIL);
    givenDate.set(Calendar.YEAR, 2020);

    System.out.println("* Fechas a partir de las cuales se obtiene el coeficiente del " + givenCrop.getName());
    System.out.println("Fecha de siembra: " + UtilDate.formatDate(seedDate));
    System.out.println("Fecha actual: " + UtilDate.formatDate(givenDate));
    System.out.println();

    System.out.println(
        "Cantidad de dias de vida del " + givenCrop.getName() + " desde su fecha de siembra hasta la fecha actual: "
            + getDaysLife(seedDate, givenDate));
    System.out.println();

    System.out.println("* Periodo de cada etapa del " + givenCrop.getName());
    System.out.println("Etapa inicial: dia " + getLowerLimitInitialStage()
        + " a dia " + getUpperLimitInitialStage(givenCrop));
    System.out.println("Etapa de desarrollo: dia " + getLowerLimitDevelopmentStage(givenCrop) + " a dia "
        + getUpperLimitDevelopmentStage(givenCrop));
    System.out.println("Etapa media: dia " + getLowerLimitMiddleStage(givenCrop) + " a dia "
        + getUpperLimitMiddleStage(givenCrop));
    System.out.println("Etapa final: dia " + getLowerLimitFinalStage(givenCrop) + " a dia "
        + getUpperLimitFinalStage(givenCrop));
    System.out.println();

    /*
     * Seccion de prueba
     */
    double expectedResult = 0.80;
    double kc = cropService.getKc(givenCrop, seedDate, givenDate);

    System.out.println("* Seccion de prueba *");
    System.out.println("Kc esperado: " + expectedResult);
    System.out.println("Kc devuelto por el metodo getKc: " + kc);
    System.out.println();

    assertEquals(expectedResult, kc, 1e-8);

    System.out.println("- Prueba pasada satisfactoriamente");
  }

  @AfterClass
  public static void postTest() {
    // Cierra las conexiones
    entityManager.close();
    entityMangerFactory.close();
  }

  /**
   * @param seedDate
   * @param givenDate
   * @return entero que representa la cantidad de dias
   *         de vida de un cultivo desde su fecha de siembra
   *         (incluida) hasta una fecha dada
   */
  private int getDaysLife(Calendar seedDate, Calendar givenDate) {
    /*
     * A la diferencia de dias entre la fecha de siembra de
     * un cultivo y la fecha dada se le suma un uno para
     * incluir a la fecha de siembra en el resultado, ya que
     * esta cuenta como un dia de vida en la cantidad de dias
     * de vida de un cultivo
     */
    return UtilDate.calculateDifferenceBetweenDates(seedDate, givenDate) + 1;
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

}
