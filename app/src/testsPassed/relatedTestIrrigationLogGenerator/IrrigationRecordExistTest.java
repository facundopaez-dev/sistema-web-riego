import static org.junit.Assert.*;

import java.util.Calendar;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import model.Parcel;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import stateless.IrrigationRecordServiceBean;
import stateless.ParcelServiceBean;
import util.FormatDate;
import util.UtilDate;

public class IrrigationRecordExistTest {
  private static IrrigationRecordServiceBean irrigationRecordService;
  private static ParcelServiceBean parcelService;
  private static EntityManager entityManager;
  private static EntityManagerFactory entityMangerFactory;

  @BeforeClass
  public static void preTest(){
    entityMangerFactory = Persistence.createEntityManagerFactory("swcar");
    entityManager = entityMangerFactory.createEntityManager();

    irrigationRecordService = new IrrigationRecordServiceBean();
    irrigationRecordService.setEntityManager(entityManager);

    parcelService = new ParcelServiceBean();
    parcelService.setEntityManager(entityManager);
  }

  /*
   * NOTA: Si se hace mas de una prueba, modificar
   * "El siguiente bloque de codigo fuente ... tiene" por
   * "Los siguientes bloques de codigo fuente ... tienen"
   *
   * El siguiente bloque de codigo fuente de
   * prueba unitaria tiene la finalidad de probar
   * el correcto funcionamiento del bloque de codigo
   * fuente llamado exist(Calendar givenDate, Parcel givenParcel)
   * de la clase IrrigationRecordServiceBean, el cual tiene la
   * responsabilidad de retornar verdadero si existe un
   * regostro historico de riego asociado a la parcela y
   * a la fecha dada, y falso en el caso de que no
   * exista tal registro historico de riego
   *
   * *** NOTA ***
   * El metodo exist(Calendar givenDate, Parcel givenParcel)
   * de la clase IrrigationRecordServiceBean es necesario para
   * el modulo de creacion y almacenamiento de registros
   * historicos de riego para cada parcela existente en
   * el sistema
   */
  @Test
  public void testPositiveExistIrrigationRecord() {
    System.out.println("Prueba unitaria positiva de existencia de un registro de riego dado una fecha y una parcela");
    System.out.println();

    Parcel choosenparcel = parcelService.find(1);
    Calendar currentDate = UtilDate.getCurrentDate();
    currentDate.set(Calendar.DAY_OF_YEAR, currentDate.get(Calendar.DAY_OF_YEAR) - 1);

    System.out.println("*** Fecha actual ***");
    System.out.println("Fecha actual: " + FormatDate.formatDate(currentDate));
    System.out.println("Número del día del año: " + currentDate.get(Calendar.DAY_OF_YEAR));
    System.out.println();

    System.out.println("El registro historico de riego existe: " + (irrigationRecordService.exist(currentDate, choosenparcel) ? "Sí" : "No"));

    System.out.println("*** Fin de prueba positiva de existencia ***");
    System.out.println();
  }

  @Test
  public void methodTest() {

  }

  @AfterClass
  public static void postTest() {
    /*
     * Cierra las conexiones, cosa que hace
     * que se liberen los recursos utilizados
     * por el administrador de entidades y su fabrica
     */
    entityManager.close();
    entityMangerFactory.close();
  }

}
