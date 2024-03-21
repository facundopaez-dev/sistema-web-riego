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
  private static Crop testCrop;

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

    testCrop = cropService.find("Tomate");
  }

  @Test
  public void testOne() {
    System.out.println("************************************** Prueba uno del metodo getKc **************************************");
    printDescriptionMethodToTest();
    System.out.println();
    System.out.println("En esta prueba se demuestra que el metodo getKc() retorna el Kc correcto para un cultivo que esta en la");
    System.out.println("etapa inicial de su ciclo de vida.");
    System.out.println();

    System.out.println("* Cultivo de prueba");
    System.out.println(testCrop);
    System.out.println();

    /*
     * Fechas a partir de las cuales se obtiene el
     * Kc (coeficiente de cultivo) del cultivo de
     * prueba
     */
    Calendar seedDate = Calendar.getInstance();
    seedDate.set(Calendar.DAY_OF_MONTH, 27);
    seedDate.set(Calendar.MONTH, NOVEMBER);
    seedDate.set(Calendar.YEAR, 2019);

    Calendar dateUntil = Calendar.getInstance();
    dateUntil.set(Calendar.DAY_OF_MONTH, 15);
    dateUntil.set(Calendar.MONTH, DECEMBER);
    dateUntil.set(Calendar.YEAR, 2019);

    System.out.println("* Fechas a partir de las cuales se obtiene el coeficiente del " + testCrop.getName());
    System.out.println("Fecha de siembra: " + UtilDate.formatDate(seedDate));
    System.out.println("Fecha hasta: " + UtilDate.formatDate(dateUntil));
    System.out.println();

    System.out.println(
        "Cantidad de dias de vida del " + testCrop.getName() + " desde su fecha de siembra hasta la fecha hasta: "
            + getDaysLife(seedDate, dateUntil));
    System.out.println();

    System.out.println("* Periodo de cada etapa del " + testCrop.getName());
    System.out.println("Etapa inicial: dia " + getLowerLimitInitialStage()
        + " a dia " + getUpperLimitInitialStage(testCrop));
    System.out.println("Etapa de desarrollo: dia " + getLowerLimitDevelopmentStage(testCrop) + " a dia "
        + getUpperLimitDevelopmentStage(testCrop));
    System.out.println("Etapa media: dia " + getLowerLimitMiddleStage(testCrop) + " a dia "
        + getUpperLimitMiddleStage(testCrop));
    System.out.println("Etapa final: dia " + getLowerLimitFinalStage(testCrop) + " a dia "
        + getUpperLimitFinalStage(testCrop));
    System.out.println();

    /*
     * Seccion de prueba
     */
    double expectedResult = testCrop.getInitialKc();
    double kc = cropService.getKc(testCrop, seedDate, dateUntil);

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
    printDescriptionMethodToTest();
    System.out.println();
    System.out.println("En esta prueba se demuestra que el metodo getKc() retorna el Kc correcto para un cultivo que esta en la");
    System.out.println("etapa de desarrollo de su ciclo de vida.");
    System.out.println();

    System.out.println("* Cultivo de prueba");
    System.out.println(testCrop);
    System.out.println();

    /*
     * Fechas a partir de las cuales se obtiene el
     * Kc (coeficiente de cultivo) del cultivo de
     * prueba
     */
    Calendar seedDate = Calendar.getInstance();
    seedDate.set(Calendar.DAY_OF_MONTH, 27);
    seedDate.set(Calendar.MONTH, NOVEMBER);
    seedDate.set(Calendar.YEAR, 2019);

    Calendar dateUntil = Calendar.getInstance();
    dateUntil.set(Calendar.DAY_OF_MONTH, 5);
    dateUntil.set(Calendar.MONTH, JANUARY);
    dateUntil.set(Calendar.YEAR, 2020);

    System.out.println("* Fechas a partir de las cuales se obtiene el coeficiente del " + testCrop.getName());
    System.out.println("Fecha de siembra: " + UtilDate.formatDate(seedDate));
    System.out.println("Fecha hasta: " + UtilDate.formatDate(dateUntil));
    System.out.println();

    System.out.println(
        "Cantidad de dias de vida del " + testCrop.getName() + " desde su fecha de siembra hasta la fecha hasta: "
            + getDaysLife(seedDate, dateUntil));
    System.out.println();

    System.out.println("* Periodo de cada etapa del " + testCrop.getName());
    System.out.println("Etapa inicial: dia " + getLowerLimitInitialStage()
        + " a dia " + getUpperLimitInitialStage(testCrop));
    System.out.println("Etapa de desarrollo: dia " + getLowerLimitDevelopmentStage(testCrop) + " a dia "
        + getUpperLimitDevelopmentStage(testCrop));
    System.out.println("Etapa media: dia " + getLowerLimitMiddleStage(testCrop) + " a dia "
        + getUpperLimitMiddleStage(testCrop));
    System.out.println("Etapa final: dia " + getLowerLimitFinalStage(testCrop) + " a dia "
        + getUpperLimitFinalStage(testCrop));
    System.out.println();

    /*
     * Seccion de prueba
     */
    double expectedResult = testCrop.getMiddleKc();
    double kc = cropService.getKc(testCrop, seedDate, dateUntil);

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
    printDescriptionMethodToTest();
    System.out.println();
    System.out.println("En esta prueba se demuestra que el metodo getKc() retorna el Kc correcto para un cultivo que esta en la");
    System.out.println("etapa media de su ciclo de vida.");
    System.out.println();

    System.out.println("* Cultivo de prueba");
    System.out.println(testCrop);
    System.out.println();

    /*
     * Fechas a partir de las cuales se obtiene el
     * Kc (coeficiente de cultivo) del cultivo de
     * prueba
     */
    Calendar seedDate = Calendar.getInstance();
    seedDate.set(Calendar.DAY_OF_MONTH, 27);
    seedDate.set(Calendar.MONTH, NOVEMBER);
    seedDate.set(Calendar.YEAR, 2019);

    Calendar dateUntil = Calendar.getInstance();
    dateUntil.set(Calendar.DAY_OF_MONTH, 1);
    dateUntil.set(Calendar.MONTH, MARCH);
    dateUntil.set(Calendar.YEAR, 2020);

    System.out.println("* Fechas a partir de las cuales se obtiene el coeficiente del " + testCrop.getName());
    System.out.println("Fecha de siembra: " + UtilDate.formatDate(seedDate));
    System.out.println("Fecha hasta: " + UtilDate.formatDate(dateUntil));
    System.out.println();

    System.out.println(
        "Cantidad de dias de vida del " + testCrop.getName() + " desde su fecha de siembra hasta la fecha hasta: "
            + getDaysLife(seedDate, dateUntil));
    System.out.println();

    System.out.println("* Periodo de cada etapa del " + testCrop.getName());
    System.out.println("Etapa inicial: dia " + getLowerLimitInitialStage()
        + " a dia " + getUpperLimitInitialStage(testCrop));
    System.out.println("Etapa de desarrollo: dia " + getLowerLimitDevelopmentStage(testCrop) + " a dia "
        + getUpperLimitDevelopmentStage(testCrop));
    System.out.println("Etapa media: dia " + getLowerLimitMiddleStage(testCrop) + " a dia "
        + getUpperLimitMiddleStage(testCrop));
    System.out.println("Etapa final: dia " + getLowerLimitFinalStage(testCrop) + " a dia "
        + getUpperLimitFinalStage(testCrop));
    System.out.println();

    /*
     * Seccion de prueba
     */
    double expectedResult = testCrop.getMiddleKc();
    double kc = cropService.getKc(testCrop, seedDate, dateUntil);

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
    printDescriptionMethodToTest();
    System.out.println();
    System.out.println("En esta prueba se demuestra que el metodo getKc() retorna el Kc correcto para un cultivo que esta en la");
    System.out.println("etapa final de su ciclo de vida.");
    System.out.println();

    System.out.println("* Cultivo de prueba");
    System.out.println(testCrop);
    System.out.println();

    /*
     * Fechas a partir de las cuales se obtiene el
     * Kc (coeficiente de cultivo) del cultivo de
     * prueba
     */
    Calendar seedDate = Calendar.getInstance();
    seedDate.set(Calendar.DAY_OF_MONTH, 27);
    seedDate.set(Calendar.MONTH, NOVEMBER);
    seedDate.set(Calendar.YEAR, 2019);

    Calendar dateUntil = Calendar.getInstance();
    dateUntil.set(Calendar.DAY_OF_MONTH, 1);
    dateUntil.set(Calendar.MONTH, APRIL);
    dateUntil.set(Calendar.YEAR, 2020);

    System.out.println("* Fechas a partir de las cuales se obtiene el coeficiente del " + testCrop.getName());
    System.out.println("Fecha de siembra: " + UtilDate.formatDate(seedDate));
    System.out.println("Fecha hasta: " + UtilDate.formatDate(dateUntil));
    System.out.println();

    System.out.println(
        "Cantidad de dias de vida del " + testCrop.getName() + " desde su fecha de siembra hasta la fecha hasta: "
            + getDaysLife(seedDate, dateUntil));
    System.out.println();

    System.out.println("* Periodo de cada etapa del " + testCrop.getName());
    System.out.println("Etapa inicial: dia " + getLowerLimitInitialStage()
        + " a dia " + getUpperLimitInitialStage(testCrop));
    System.out.println("Etapa de desarrollo: dia " + getLowerLimitDevelopmentStage(testCrop) + " a dia "
        + getUpperLimitDevelopmentStage(testCrop));
    System.out.println("Etapa media: dia " + getLowerLimitMiddleStage(testCrop) + " a dia "
        + getUpperLimitMiddleStage(testCrop));
    System.out.println("Etapa final: dia " + getLowerLimitFinalStage(testCrop) + " a dia "
        + getUpperLimitFinalStage(testCrop));
    System.out.println();

    /*
     * Seccion de prueba
     */
    double expectedResult = testCrop.getFinalKc();
    double kc = cropService.getKc(testCrop, seedDate, dateUntil);

    System.out.println("* Seccion de prueba *");
    System.out.println("Kc esperado: " + expectedResult);
    System.out.println("Kc devuelto por el metodo getKc: " + kc);
    System.out.println();

    assertEquals(expectedResult, kc, 1e-8);

    System.out.println("- Prueba pasada satisfactoriamente");
  }

  @Test
  public void testFive() {
    System.out.println("************************************** Prueba cinco del metodo getKc **************************************");
    printDescriptionMethodToTest();
    System.out.println();
    System.out.println("En esta prueba se demuestra que el metodo getKc() retorna el Kc correcto para un cultivo que ha vivido una");
    System.out.println("cantidad de dias estrictamente mayor a la cantidad de dias que dura su ciclo de vida.");
    System.out.println();

    System.out.println("* Cultivo de prueba");
    System.out.println(testCrop);
    System.out.println();

    /*
     * Fechas a partir de las cuales se obtiene el
     * Kc (coeficiente de cultivo) del cultivo de
     * prueba
     */
    Calendar seedDate = Calendar.getInstance();
    seedDate.set(Calendar.DAY_OF_MONTH, 27);
    seedDate.set(Calendar.MONTH, NOVEMBER);
    seedDate.set(Calendar.YEAR, 2019);

    Calendar dateUntil = Calendar.getInstance();
    dateUntil.set(Calendar.DAY_OF_MONTH, 8);
    dateUntil.set(Calendar.MONTH, MAY);
    dateUntil.set(Calendar.YEAR, 2020);

    System.out.println("* Fechas a partir de las cuales se obtiene el coeficiente del " + testCrop.getName());
    System.out.println("Fecha de siembra: " + UtilDate.formatDate(seedDate));
    System.out.println("Fecha hasta: " + UtilDate.formatDate(dateUntil));
    System.out.println();

    System.out.println(
        "Cantidad de dias de vida del " + testCrop.getName() + " desde su fecha de siembra hasta la fecha hasta: "
            + getDaysLife(seedDate, dateUntil));
    System.out.println();

    System.out.println("* Periodo de cada etapa del " + testCrop.getName());
    System.out.println("Etapa inicial: dia " + getLowerLimitInitialStage()
        + " a dia " + getUpperLimitInitialStage(testCrop));
    System.out.println("Etapa de desarrollo: dia " + getLowerLimitDevelopmentStage(testCrop) + " a dia "
        + getUpperLimitDevelopmentStage(testCrop));
    System.out.println("Etapa media: dia " + getLowerLimitMiddleStage(testCrop) + " a dia "
        + getUpperLimitMiddleStage(testCrop));
    System.out.println("Etapa final: dia " + getLowerLimitFinalStage(testCrop) + " a dia "
        + getUpperLimitFinalStage(testCrop));
    System.out.println();

    /*
     * Seccion de prueba
     */
    double expectedResult = testCrop.getFinalKc();
    double kc = cropService.getKc(testCrop, seedDate, dateUntil);

    System.out.println("* Seccion de prueba *");
    System.out.println("Kc esperado: " + expectedResult);
    System.out.println("Kc devuelto por el metodo getKc: " + kc);
    System.out.println();

    assertEquals(expectedResult, kc, 1e-8);

    System.out.println("- Prueba pasada satisfactoriamente");
  }

  @Test
  public void testSix() {
    System.out.println("************************************** Prueba seis del metodo getKc **************************************");
    printDescriptionMethodToTest();
    System.out.println();
    System.out.println("En esta prueba se demuestra que el metodo getKc() retorna el Kc correcto para un cultivo que ha vivido una");
    System.out.println("una cantidad de dias estrictamente mayor por una unidad a la cantidad de dias que dura su ciclo de vida.");
    System.out.println();

    System.out.println("* Cultivo de prueba");
    System.out.println(testCrop);
    System.out.println();

    /*
     * Fechas a partir de las cuales se obtiene el
     * Kc (coeficiente de cultivo) del cultivo de
     * prueba
     */
    Calendar seedDate = Calendar.getInstance();
    seedDate.set(Calendar.DAY_OF_MONTH, 27);
    seedDate.set(Calendar.MONTH, NOVEMBER);
    seedDate.set(Calendar.YEAR, 2019);

    Calendar dateUntil = Calendar.getInstance();
    dateUntil.set(Calendar.DAY_OF_MONTH, 10);
    dateUntil.set(Calendar.MONTH, APRIL);
    dateUntil.set(Calendar.YEAR, 2020);

    System.out.println("* Fechas a partir de las cuales se obtiene el coeficiente del " + testCrop.getName());
    System.out.println("Fecha de siembra: " + UtilDate.formatDate(seedDate));
    System.out.println("Fecha hasta: " + UtilDate.formatDate(dateUntil));
    System.out.println();

    System.out.println(
        "Cantidad de dias de vida del " + testCrop.getName() + " desde su fecha de siembra hasta la fecha hasta: "
            + getDaysLife(seedDate, dateUntil));
    System.out.println();

    System.out.println("* Periodo de cada etapa del " + testCrop.getName());
    System.out.println("Etapa inicial: dia " + getLowerLimitInitialStage()
        + " a dia " + getUpperLimitInitialStage(testCrop));
    System.out.println("Etapa de desarrollo: dia " + getLowerLimitDevelopmentStage(testCrop) + " a dia "
        + getUpperLimitDevelopmentStage(testCrop));
    System.out.println("Etapa media: dia " + getLowerLimitMiddleStage(testCrop) + " a dia "
        + getUpperLimitMiddleStage(testCrop));
    System.out.println("Etapa final: dia " + getLowerLimitFinalStage(testCrop) + " a dia "
        + getUpperLimitFinalStage(testCrop));
    System.out.println();

    /*
     * Seccion de prueba
     */
    double expectedResult = testCrop.getFinalKc();
    double kc = cropService.getKc(testCrop, seedDate, dateUntil);

    System.out.println("* Seccion de prueba *");
    System.out.println("Kc esperado: " + expectedResult);
    System.out.println("Kc devuelto por el metodo getKc: " + kc);
    System.out.println();

    assertEquals(expectedResult, kc, 1e-8);

    System.out.println("- Prueba pasada satisfactoriamente");
  }

  @Test
  public void testSeven() {
    System.out.println("************************************** Prueba siete del metodo getKc **************************************");
    printDescriptionMethodToTest();
    System.out.println();
    System.out.println("En esta prueba se demuestra que el metodo getKc() retorna el Kc correcto para un cultivo que esta en la");
    System.out.println("etapa inicial de su ciclo de vida. Esta prueba es un caso de borde porque se busca demostrar que el metodo");
    System.out.println("getKc() retorna el Kc correcto para un cultivo que ha vivido una cantidad de dias igual al extremo superior");
    System.out.println("de la etapa en la que se encuentra.");
    System.out.println();

    System.out.println("* Cultivo de prueba");
    System.out.println(testCrop);
    System.out.println();

    /*
     * Fechas a partir de las cuales se obtiene el
     * Kc (coeficiente de cultivo) del cultivo de
     * prueba
     */
    Calendar seedDate = Calendar.getInstance();
    seedDate.set(Calendar.DAY_OF_MONTH, 27);
    seedDate.set(Calendar.MONTH, NOVEMBER);
    seedDate.set(Calendar.YEAR, 2019);

    Calendar dateUntil = Calendar.getInstance();
    dateUntil.set(Calendar.DAY_OF_MONTH, 26);
    dateUntil.set(Calendar.MONTH, DECEMBER);
    dateUntil.set(Calendar.YEAR, 2019);

    System.out.println("* Fechas a partir de las cuales se obtiene el coeficiente del " + testCrop.getName());
    System.out.println("Fecha de siembra: " + UtilDate.formatDate(seedDate));
    System.out.println("Fecha hasta: " + UtilDate.formatDate(dateUntil));
    System.out.println();

    System.out.println(
        "Cantidad de dias de vida del " + testCrop.getName() + " desde su fecha de siembra hasta la fecha hasta: "
            + getDaysLife(seedDate, dateUntil));
    System.out.println();

    System.out.println("* Periodo de cada etapa del " + testCrop.getName());
    System.out.println("Etapa inicial: dia " + getLowerLimitInitialStage()
        + " a dia " + getUpperLimitInitialStage(testCrop));
    System.out.println("Etapa de desarrollo: dia " + getLowerLimitDevelopmentStage(testCrop) + " a dia "
        + getUpperLimitDevelopmentStage(testCrop));
    System.out.println("Etapa media: dia " + getLowerLimitMiddleStage(testCrop) + " a dia "
        + getUpperLimitMiddleStage(testCrop));
    System.out.println("Etapa final: dia " + getLowerLimitFinalStage(testCrop) + " a dia "
        + getUpperLimitFinalStage(testCrop));
    System.out.println();

    /*
     * Seccion de prueba
     */
    double expectedResult = testCrop.getInitialKc();
    double kc = cropService.getKc(testCrop, seedDate, dateUntil);

    System.out.println("* Seccion de prueba *");
    System.out.println("Kc esperado: " + expectedResult);
    System.out.println("Kc devuelto por el metodo getKc: " + kc);
    System.out.println();

    assertEquals(expectedResult, kc, 1e-8);

    System.out.println("- Prueba pasada satisfactoriamente");
  }

  @Test
  public void testEight() {
    System.out.println("************************************** Prueba ocho del metodo getKc **************************************");
    printDescriptionMethodToTest();
    System.out.println();
    System.out.println("En esta prueba se demuestra que el metodo getKc() retorna el Kc correcto para un cultivo que esta en la");
    System.out.println("etapa de desarrollo de su ciclo de vida. Esta prueba es un caso de borde porque se busca demostrar que el");
    System.out.println("metodo getKc() retorna el Kc correcto para un cultivo que ha vivido una cantidad de dias igual al extremo");
    System.out.println("superior de la etapa en la que se encuentra.");
    System.out.println();

    System.out.println("* Cultivo de prueba");
    System.out.println(testCrop);
    System.out.println();

    /*
     * Fechas a partir de las cuales se obtiene el
     * Kc (coeficiente de cultivo) del cultivo de
     * prueba
     */
    Calendar seedDate = Calendar.getInstance();
    seedDate.set(Calendar.DAY_OF_MONTH, 27);
    seedDate.set(Calendar.MONTH, NOVEMBER);
    seedDate.set(Calendar.YEAR, 2019);

    Calendar dateUntil = Calendar.getInstance();
    dateUntil.set(Calendar.DAY_OF_MONTH, 4);
    dateUntil.set(Calendar.MONTH, FEBRUARY);
    dateUntil.set(Calendar.YEAR, 2020);

    System.out.println("* Fechas a partir de las cuales se obtiene el coeficiente del " + testCrop.getName());
    System.out.println("Fecha de siembra: " + UtilDate.formatDate(seedDate));
    System.out.println("Fecha hasta: " + UtilDate.formatDate(dateUntil));
    System.out.println();

    System.out.println(
        "Cantidad de dias de vida del " + testCrop.getName() + " desde su fecha de siembra hasta la fecha hasta: "
            + getDaysLife(seedDate, dateUntil));
    System.out.println();

    System.out.println("* Periodo de cada etapa del " + testCrop.getName());
    System.out.println("Etapa inicial: dia " + getLowerLimitInitialStage()
        + " a dia " + getUpperLimitInitialStage(testCrop));
    System.out.println("Etapa de desarrollo: dia " + getLowerLimitDevelopmentStage(testCrop) + " a dia "
        + getUpperLimitDevelopmentStage(testCrop));
    System.out.println("Etapa media: dia " + getLowerLimitMiddleStage(testCrop) + " a dia "
        + getUpperLimitMiddleStage(testCrop));
    System.out.println("Etapa final: dia " + getLowerLimitFinalStage(testCrop) + " a dia "
        + getUpperLimitFinalStage(testCrop));
    System.out.println();

    /*
     * Seccion de prueba
     */
    double expectedResult = testCrop.getMiddleKc();
    double kc = cropService.getKc(testCrop, seedDate, dateUntil);

    System.out.println("* Seccion de prueba *");
    System.out.println("Kc esperado: " + expectedResult);
    System.out.println("Kc devuelto por el metodo getKc: " + kc);
    System.out.println();

    assertEquals(expectedResult, kc, 1e-8);

    System.out.println("- Prueba pasada satisfactoriamente");
  }

  @Test
  public void testNine() {
    System.out.println("************************************** Prueba nueve del metodo getKc **************************************");
    printDescriptionMethodToTest();
    System.out.println();
    System.out.println("En esta prueba se demuestra que el metodo getKc() retorna el Kc correcto para un cultivo que esta en la");
    System.out.println("etapa media de su ciclo de vida. Esta prueba es un caso de borde porque se busca demostrar que el metodo");
    System.out.println("getKc() retorna el Kc correcto para un cultivo que ha vivido una cantidad de dias igual al extremo superior");
    System.out.println("de la etapa en la que se encuentra.");
    System.out.println();

    System.out.println("* Cultivo de prueba");
    System.out.println(testCrop);
    System.out.println();

    /*
     * Fechas a partir de las cuales se obtiene el
     * Kc (coeficiente de cultivo) del cultivo de
     * prueba
     */
    Calendar seedDate = Calendar.getInstance();
    seedDate.set(Calendar.DAY_OF_MONTH, 27);
    seedDate.set(Calendar.MONTH, NOVEMBER);
    seedDate.set(Calendar.YEAR, 2019);

    Calendar dateUntil = Calendar.getInstance();
    dateUntil.set(Calendar.DAY_OF_MONTH, 15);
    dateUntil.set(Calendar.MONTH, MARCH);
    dateUntil.set(Calendar.YEAR, 2020);

    System.out.println("* Fechas a partir de las cuales se obtiene el coeficiente del " + testCrop.getName());
    System.out.println("Fecha de siembra: " + UtilDate.formatDate(seedDate));
    System.out.println("Fecha hasta: " + UtilDate.formatDate(dateUntil));
    System.out.println();

    System.out.println(
        "Cantidad de dias de vida del " + testCrop.getName() + " desde su fecha de siembra hasta la fecha hasta: "
            + getDaysLife(seedDate, dateUntil));
    System.out.println();

    System.out.println("* Periodo de cada etapa del " + testCrop.getName());
    System.out.println("Etapa inicial: dia " + getLowerLimitInitialStage()
        + " a dia " + getUpperLimitInitialStage(testCrop));
    System.out.println("Etapa de desarrollo: dia " + getLowerLimitDevelopmentStage(testCrop) + " a dia "
        + getUpperLimitDevelopmentStage(testCrop));
    System.out.println("Etapa media: dia " + getLowerLimitMiddleStage(testCrop) + " a dia "
        + getUpperLimitMiddleStage(testCrop));
    System.out.println("Etapa final: dia " + getLowerLimitFinalStage(testCrop) + " a dia "
        + getUpperLimitFinalStage(testCrop));
    System.out.println();

    /*
     * Seccion de prueba
     */
    double expectedResult = testCrop.getMiddleKc();
    double kc = cropService.getKc(testCrop, seedDate, dateUntil);

    System.out.println("* Seccion de prueba *");
    System.out.println("Kc esperado: " + expectedResult);
    System.out.println("Kc devuelto por el metodo getKc: " + kc);
    System.out.println();

    assertEquals(expectedResult, kc, 1e-8);

    System.out.println("- Prueba pasada satisfactoriamente");
  }

  @Test
  public void testTen() {
    System.out.println("************************************** Prueba diez del metodo getKc **************************************");
    printDescriptionMethodToTest();
    System.out.println();
    System.out.println("En esta prueba se demuestra que el metodo getKc() retorna el Kc correcto para un cultivo que esta en la");
    System.out.println("etapa final de su ciclo de vida. Esta prueba es un caso de borde porque se busca demostrar que el metodo");
    System.out.println("getKc() retorna el Kc correcto para un cultivo que ha vivido una cantidad de dias igual al extremo superior");
    System.out.println("de la etapa en la que se encuentra.");
    System.out.println();

    System.out.println("* Cultivo de prueba");
    System.out.println(testCrop);
    System.out.println();

    /*
     * Fechas a partir de las cuales se obtiene el
     * Kc (coeficiente de cultivo) del cultivo de
     * prueba
     */
    Calendar seedDate = Calendar.getInstance();
    seedDate.set(Calendar.DAY_OF_MONTH, 27);
    seedDate.set(Calendar.MONTH, NOVEMBER);
    seedDate.set(Calendar.YEAR, 2019);

    Calendar dateUntil = Calendar.getInstance();
    dateUntil.set(Calendar.DAY_OF_MONTH, 9);
    dateUntil.set(Calendar.MONTH, APRIL);
    dateUntil.set(Calendar.YEAR, 2020);

    System.out.println("* Fechas a partir de las cuales se obtiene el coeficiente del " + testCrop.getName());
    System.out.println("Fecha de siembra: " + UtilDate.formatDate(seedDate));
    System.out.println("Fecha hasta: " + UtilDate.formatDate(dateUntil));
    System.out.println();

    System.out.println(
        "Cantidad de dias de vida del " + testCrop.getName() + " desde su fecha de siembra hasta la fecha hasta: "
            + getDaysLife(seedDate, dateUntil));
    System.out.println();

    System.out.println("* Periodo de cada etapa del " + testCrop.getName());
    System.out.println("Etapa inicial: dia " + getLowerLimitInitialStage()
        + " a dia " + getUpperLimitInitialStage(testCrop));
    System.out.println("Etapa de desarrollo: dia " + getLowerLimitDevelopmentStage(testCrop) + " a dia "
        + getUpperLimitDevelopmentStage(testCrop));
    System.out.println("Etapa media: dia " + getLowerLimitMiddleStage(testCrop) + " a dia "
        + getUpperLimitMiddleStage(testCrop));
    System.out.println("Etapa final: dia " + getLowerLimitFinalStage(testCrop) + " a dia "
        + getUpperLimitFinalStage(testCrop));
    System.out.println();

    /*
     * Seccion de prueba
     */
    double expectedResult = testCrop.getFinalKc();
    double kc = cropService.getKc(testCrop, seedDate, dateUntil);

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
   * Imprime la descripcion del metodo a probar
   */
  private void printDescriptionMethodToTest() {
    System.out.println("El metodo getKc() de la clase CropServiceBean retorna el Kc (coeficiente de cultivo) de un cultivo con base");
    System.out.println("a la etapa en la que se encuentra un cultivo, lo cual depende de la cantidad de dias que ha vivido desde su");
    System.out.println("fecha de siembra hasta una fecha hasta. El ciclo de vida de un cultivo tiene cuatro etapas: etapa inicial,");
    System.out.println("etapa de desarrollo, etapa media y etapa final. Estas etapas ocurren en este orden.");
    System.out.println();
    System.out.println("Si la cantidad de dias de vida que ha vivido un cultivo es estrictamente mayor a la cantidad de dias que");
    System.out.println("dura su ciclo de vida, el metodo getKc() retorna el Kc final.");
  }

  /**
   * @param seedDate
   * @param dateUntil
   * @return entero que representa la cantidad de dias
   * de vida de un cultivo desde su fecha de siembra
   * (incluida) hasta una fecha dada
   */
  private int getDaysLife(Calendar seedDate, Calendar dateUntil) {
    /*
     * A la diferencia de dias entre la fecha de siembra de
     * un cultivo y la fecha hasta se le suma un uno para
     * incluir a la fecha de siembra en el resultado, ya que
     * esta cuenta como un dia de vida en la cantidad de dias
     * de vida de un cultivo
     */
    return UtilDate.calculateDifferenceBetweenDates(seedDate, dateUntil) + 1;
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
   * @param crop
   * @return entero que representa el ultimo dia de la
   * etapa inicial del ciclo de vida de un cultivo
   */
  private int getUpperLimitInitialStage(Crop crop) {
    return crop.getInitialStage();
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
   * @param crop
   * @return entero que representa el primer dia de la etapa
   * de desarrollo del ciclo de vida de un cultivo
   */
  private int getLowerLimitDevelopmentStage(Crop crop) {
    return getUpperLimitInitialStage(crop) + 1;
  }

  /**
   * @param crop
   * @return entero que representa el ultimo dia de la etapa
   * de desarrollo del ciclo de vida de un cultivo
   */
  private int getUpperLimitDevelopmentStage(Crop crop) {
    return (crop.getInitialStage() + crop.getDevelopmentStage());
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
   * @param crop
   * @return entero que representa el primer dia de la etapa
   * media del ciclo de vida de un cultivo
   */
  private int getLowerLimitMiddleStage(Crop crop) {
    return getUpperLimitDevelopmentStage(crop) + 1;
  }

  /**
   * @param crop
   * @return entero que representa el ultimo dia de la etapa
   * media del ciclo de vida de un cultivo
   */
  private int getUpperLimitMiddleStage(Crop crop) {
    return (crop.getInitialStage() + crop.getDevelopmentStage() + crop.getMiddleStage());
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
   * @param crop
   * @return entero que representa el primer dia de la etapa
   * final del ciclo de vida de un cultivo
   */
  private int getLowerLimitFinalStage(Crop crop) {
    return getUpperLimitMiddleStage(crop) + 1;
  }

  /**
   * @param crop
   * @return entero que representa el ultimo dia de la etapa
   * final del ciclo de vida de un cultivo
   */
  private int getUpperLimitFinalStage(Crop crop) {
    return (crop.getInitialStage() + crop.getDevelopmentStage() + crop.getMiddleStage() + crop.getFinalStage());
  }

}
