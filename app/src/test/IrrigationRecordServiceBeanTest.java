import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.ArrayList;
import java.util.Collection;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import model.IrrigationRecord;
import model.Parcel;
import model.User;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import stateless.ParcelServiceBean;
import stateless.IrrigationRecordServiceBean;
import stateless.UserServiceBean;
import util.UtilDate;

public class IrrigationRecordServiceBeanTest {

  private static EntityManager entityManager;
  private static EntityManagerFactory entityManagerFactory;
  private static ParcelServiceBean parcelService;
  private static IrrigationRecordServiceBean irrigationRecordService;
  private static UserServiceBean userService;
  private static Collection<Parcel> parcels;
  private static Collection<IrrigationRecord> irrigationRecords;
  private static Collection<User> users;

  @BeforeClass
  public static void preTest() {
    entityManagerFactory = Persistence.createEntityManagerFactory("swcar");
    entityManager = entityManagerFactory.createEntityManager();

    parcelService = new ParcelServiceBean();
    parcelService.setEntityManager(entityManager);

    userService = new UserServiceBean();
    userService.setEntityManager(entityManager);

    irrigationRecordService = new IrrigationRecordServiceBean();
    irrigationRecordService.setEntityManager(entityManager);

    parcels = new ArrayList<>();
    irrigationRecords = new ArrayList<>();
    users = new ArrayList<>();
  }

  @Test
  public void testSumRainwaterPastDays() {
    System.out.println("**************************** Prueba del metodo sumIrrigationWaterPastDays ****************************");
    System.out.println("El metodo sumIrrigationWaterPastDays de la clase IrrigationRecordServiceBean suma el agua de riego de");
    System.out.println("NUMBER_DAYS registros de riego anteriores a la fecha actual pertenecientes a una parcela de un usuario.");
    System.out.println();
    System.out.println("Para demostrar el correcto funcionamiento de este metodo se utilizan tres registros de riego que tienen");
    System.out.println("una cantidad de agua de riego igual a dos y pertenecen a los tres dias inmediatamente anteriores");
    System.out.println("a la fecha actual.");
    System.out.println();

    /*
     * Persistencia de un usuario de prueba
     */
    User givenUser = new User();
    givenUser.setUsername("miller95");
    givenUser.setName("Audra");
    givenUser.setLastName("Miller");
    givenUser.setEmail("a@email.com");
    givenUser.setPassword("Audra");

    entityManager.getTransaction().begin();
    givenUser = userService.create(givenUser);
    entityManager.getTransaction().commit();

    users.add(givenUser);

    /*
     * Persistencia de una parcela de prueba
     */
    Parcel givenParcel = new Parcel();
    givenParcel.setName("Erie");
    givenParcel.setHectares(2);
    givenParcel.setLatitude(1);
    givenParcel.setLongitude(1);
    givenParcel.setUser(givenUser);

    entityManager.getTransaction().begin();
    givenParcel = parcelService.create(givenParcel);
    entityManager.getTransaction().commit();

    parcels.add(givenParcel);

    /*
     * Creacion de fechas para los registros climaticos
     */
    Calendar firstDate = UtilDate.getCurrentDate();
    firstDate.set(Calendar.DAY_OF_YEAR, (firstDate.get(Calendar.DAY_OF_YEAR) - 1));

    Calendar secondDate = UtilDate.getCurrentDate();
    secondDate.set(Calendar.DAY_OF_YEAR, (secondDate.get(Calendar.DAY_OF_YEAR) - 2));

    Calendar thirdDate = UtilDate.getCurrentDate();
    thirdDate.set(Calendar.DAY_OF_YEAR, (thirdDate.get(Calendar.DAY_OF_YEAR) - 3));

    /*
     * Persistencia de registros de riego de prueba
     */
    IrrigationRecord firstIrrigationRecord = new IrrigationRecord();
    firstIrrigationRecord.setDate(firstDate);
    firstIrrigationRecord.setIrrigationDone(2);
    firstIrrigationRecord.setParcel(givenParcel);

    IrrigationRecord secondIrrigationRecord = new IrrigationRecord();
    secondIrrigationRecord.setDate(secondDate);
    secondIrrigationRecord.setIrrigationDone(2);
    secondIrrigationRecord.setParcel(givenParcel);

    IrrigationRecord thirdIrrigationRecord = new IrrigationRecord();
    thirdIrrigationRecord.setDate(thirdDate);
    thirdIrrigationRecord.setIrrigationDone(2);
    thirdIrrigationRecord.setParcel(givenParcel);

    entityManager.getTransaction().begin();
    firstIrrigationRecord = irrigationRecordService.create(firstIrrigationRecord);
    secondIrrigationRecord = irrigationRecordService.create(secondIrrigationRecord);
    thirdIrrigationRecord = irrigationRecordService.create(thirdIrrigationRecord);
    entityManager.getTransaction().commit();

    irrigationRecords.add(firstIrrigationRecord);
    irrigationRecords.add(secondIrrigationRecord);
    irrigationRecords.add(thirdIrrigationRecord);

    /*
     * El valor de esta variable se utiliza para sumar el
     * agua de riego de una cantidad numberDays de registros
     * de riego inmediatamente anteriores a la fecha actual
     */
    int numberDays = irrigationRecords.size();

    System.out.println("Agua de riego del registro de riego de la fecha " + UtilDate.formatDate(firstIrrigationRecord.getDate()) + ": " + firstIrrigationRecord.getIrrigationDone());
    System.out.println("Agua de riego del registro de riego de la fecha " + UtilDate.formatDate(secondIrrigationRecord.getDate()) + ": " + secondIrrigationRecord.getIrrigationDone());
    System.out.println("Agua de riego del registro de riego de la fecha " + UtilDate.formatDate(thirdIrrigationRecord.getDate()) + ": " + thirdIrrigationRecord.getIrrigationDone());
    System.out.println();

    /*
     * Seccion de prueba
     */
    double expectedResult = firstIrrigationRecord.getIrrigationDone() + secondIrrigationRecord.getIrrigationDone() + thirdIrrigationRecord.getIrrigationDone();
    double result = irrigationRecordService.sumIrrigationWaterPastDays(givenUser.getId(), givenParcel.getId(), numberDays);

    System.out.println("* Resultado esperado: " + expectedResult);
    System.out.println("* Resultado devuelto por el metodo sumIrrigationWaterPastDays: " + result);
    System.out.println();

    assertTrue(expectedResult == result);

    System.out.println("- Prueba pasada satisfactoriamente");
    System.out.println();
  }

  @AfterClass
  public static void postTest() {
    entityManager.getTransaction().begin();

    /*
     * Se eliminan de la base de datos subyacente los datos persistidos
     * durante la ejecucion de las pruebas unitarias para que la misma
     * quede en su estado original, es decir, para dejarla en el estado
     * en el que estaba antes de que se persistieran dichos datos
     */
    for (IrrigationRecord currentIrrigationRecord : irrigationRecords) {
      irrigationRecordService.remove(currentIrrigationRecord.getId());
    }

    for (Parcel currentParcel : parcels) {
      parcelService.remove(currentParcel.getId());
    }

    for (User currentUser : users) {
      userService.remove(currentUser.getId());
    }

    entityManager.getTransaction().commit();

    // Cierra las conexiones
    entityManager.close();
    entityManagerFactory.close();
  }

}
