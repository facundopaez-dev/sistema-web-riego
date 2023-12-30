package climate;

import java.util.Calendar;
import java.util.Collection;
import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Stateless;
import stateless.ClimateRecordServiceBean;
import stateless.CropServiceBean;
import stateless.PlantingRecordServiceBean;
import stateless.ParcelServiceBean;
import stateless.SolarRadiationServiceBean;
import stateless.MonthServiceBean;
import stateless.LatitudeServiceBean;
import model.ClimateRecord;
import model.PlantingRecord;
import model.Parcel;
import util.UtilDate;
import irrigation.WaterMath;
import et.HargreavesEto;
import et.Etc;

@Stateless
public class ClimateRecordManager {

  // inject a reference to the ParcelServiceBean
  @EJB
  ParcelServiceBean parcelService;

  // inject a reference to the ClimateRecordServiceBean
  @EJB
  ClimateRecordServiceBean climateRecordService;

  // inject a reference to the SolarRadiationServiceBean
  @EJB
  SolarRadiationServiceBean solarService;

  // inject a reference to the PlantingRecordServiceBean
  @EJB
  PlantingRecordServiceBean plantingRecordService;

  // inject a reference to the CropServiceBean
  @EJB
  CropServiceBean cropService;

  // inject a reference to the MonthServiceBean
  @EJB
  MonthServiceBean monthService;

  // inject a reference to the LatitudeServiceBean
  @EJB
  LatitudeServiceBean latitudeService;

  /**
   * Obtiene y persiste de manera automatica los datos
   * meteorologicos de la fecha actual para todas las
   * parcelas activas de la base de datos subyacente.
   * 
   * Esto lo realiza cada dos horas a partir de las 00:00
   * horas, debido a que quizas en alguna hora del dia,
   * la API climatica Visual Crossing Weather puede no
   * estar disponible al momento en el que este metodo
   * se ejecuta.
   * 
   * Los datos meteorologicos son necesarios para determinar
   * la evapotranspiracion del cultivo de referencia (ETo),
   * la cual, es necesaria para determinar la evapotranspiracion
   * del cultivo bajo condiciones estandar (ETc) del cultivo
   * en desarrollo de cada parcela que tenga un cultivo en
   * desarrollo.
   * 
   * La evapotranspiracon del cultivo bajo condiciones
   * estandar (ETc) indica en milimetros por dia la cantidad
   * de agua que evapora un cultivo mediante evaporacion
   * y transpiracion. De esta manera, se sabe la cantidad
   * de agua que se le tiene que reponer a un cultivo
   * mediante el riego.
   * 
   * La segunda anotacion es para probar que el metodo hace
   * lo que se espera que haga: obtener y persistir los
   * datos meteorologicos de la fecha actual para cada
   * una de las parcelas activas de la base de datos
   * subyacente.
   */
  // @Schedule(second = "*", minute = "*", hour = "0/2", persistent = false)
  // @Schedule(second = "*/5", minute = "*", hour = "*", persistent = false)
  private void getCurrentWeatherDataset() {
    Collection<Parcel> activeParcels = parcelService.findAllActive();
    Calendar currentDate = UtilDate.getCurrentDate();
    ClimateRecord currentClimateRecord = null;
    PlantingRecord developingPlantingRecord = null;

    /*
     * Convierte el tiempo en milisegundos a segundos
     * porque el formato UNIX utiliza el tiempo en segundos
     * y se realiza esta conversion porque la API climatica
     * Visual Crossing Weather puede recibir solicitudes con
     * fechas en este formato
     */
    long unixTime = currentDate.getTimeInMillis() / 1000;
    double eto = 0.0;
    double etc = 0.0;

    /*
     * Obtiene y persiste los datos meteorologicos de la fecha
     * actual para todas las parcelas activas que NO tienen los
     * datos meteorologicos de dicha fecha
     */
    for (Parcel givenParcel : activeParcels) {
      /*
       * Si en la base de datos subyacente no existe el registro
       * climatico de la fecha actual perteneciente a una parcela
       * dada, se solicita y persiste uno para dicha parcela
       */
      if (!climateRecordService.checkExistence(currentDate, givenParcel)) {

        try {
          /*
           * Obtiene un registro climatico que contiene los datos
           * del conjunto de datos meteorologicos devuelto por la
           * API Visual Crossing Weather mediante la fecha actual
           * en formato UNIX y las coordenadas geograficas de la
           * parcela dada
           */
          currentClimateRecord = ClimateClient.getForecast(givenParcel, unixTime);
        } catch (Exception e) {
          e.printStackTrace();
          break;
        }

        eto = calculateEtoForClimateRecord(currentClimateRecord);

        /*
         * Si la parcela dada tiene un registro de plantacion en
         * desarrollo (*), se utiliza el coeficiente del cultivo
         * que tiene plantado y en desarrollo para calcular la
         * evapotranspiracion del cultivo bajo condiciones estandar
         * (ETc) [mm/dia] de dicho cultivo.
         * 
         * Si la parcela actual no tiene un cultivo plantado y en
         * desarrollo, la ETc es 0.0.
         * 
         * (*) Una parcela que tiene un registro de plantacion
         * en desarrollo es una parcela que tiene un cultivo
         * plantado y en desarrollo.
         */
        if (plantingRecordService.checkOneInDevelopment(givenParcel)) {
          developingPlantingRecord = plantingRecordService.findInDevelopment(givenParcel);
          etc = calculateEtcForCurrentClimateRecord(eto, developingPlantingRecord);
        }

        currentClimateRecord.setEto(eto);
        currentClimateRecord.setEtc(etc);

        /*
         * Persistencia de los datos meteorologicos de la fecha
         * actual para una parcela dada
         */
        climateRecordService.create(currentClimateRecord);

        /*
         * Luego de calcular la ETc de un registro climatico correspondiente
         * a una fecha y una parcela dadas, se restablece el valor por defecto
         * de esta variable para evitar el error logico de asignar la ETc de
         * un registro climatico a otro registro climatico
         */
        etc = 0.0;
      } // End if

    } // End for

  }

  /**
   * Calcula la ETo (evapotranspiracion del cultivo de referencia)
   * con los datos meteorologicos de una fecha dada, la cual esta
   * determinada por un registro climatico, ya que este tiene
   * fecha
   * 
   * @param givenClimateRecord
   * @return double que representa la ETo (evapotranspiracion del
   * cultivo de referencia) calculada en una fecha con los datos
   * meteorologicos de un registro climatico perteneciente a una
   * parcela y una fecha dada
   */
  private double calculateEtoForClimateRecord(ClimateRecord givenClimateRecord) {
    Parcel givenParcel = givenClimateRecord.getParcel();
    double extraterrestrialSolarRadiation = solarService.getRadiation(givenParcel.getLatitude(),
        monthService.getMonth(givenClimateRecord.getDate().get(Calendar.MONTH)),
        latitudeService.find(givenParcel.getLatitude()),
        latitudeService.findPreviousLatitude(givenParcel.getLatitude()),
        latitudeService.findNextLatitude(givenParcel.getLatitude()));

    /*
     * Calculo de la evapotranspiracion del cultivo de
     * referencia (ETo) de una fecha, la cual esta
     * determinada por un registro climatico, ya que
     * este tiene fecha
     */
    return HargreavesEto.calculateEto(givenClimateRecord.getMaximumTemperature(),
        givenClimateRecord.getMinimumTemperature(), extraterrestrialSolarRadiation);
  }

  /**
   * @param givenEto
   * @param givenPlantingRecord
   * @return double que representa la ETc (evapotranspiracion
   * del cultivo bajo condiciones estandar) de un cultivo
   * calculada con la ETo de la fecha actual, por lo tanto,
   * calcula la ETc de un cultivo en desarrollo en la fecha
   * actual, debido a que un registro de plantacion en
   * desarrollo representa la existencia de un cultivo plantado
   * en una parcela y en desarrollo en la fecha actual
   */
  private double calculateEtcForCurrentClimateRecord(double givenEto, PlantingRecord givenPlantingRecord) {
    return Etc.calculateEtc(givenEto, cropService.getKc(givenPlantingRecord.getCrop(), givenPlantingRecord.getSeedDate()));
  }

}
