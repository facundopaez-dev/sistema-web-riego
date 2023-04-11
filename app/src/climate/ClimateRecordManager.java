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
import stateless.IrrigationRecordServiceBean;
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

  // inject a reference to the IrrigationRecordServiceBean
  @EJB
  IrrigationRecordServiceBean irrigationService;

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
   * subyacente
   * 
   * @param second="*"
   * @param minute="*"
   * @param hour="0/2" cada dos horas a partir de las doce de la
   * noche
   * @param persistent=false
   */
  // @Schedule(second = "*", minute = "*", hour = "0/2", persistent = false)
  // @Schedule(second = "*/5", minute = "*", hour = "*", persistent = false)
  private void getCurrentWeatherDataset() {
    Collection<Parcel> activeParcels = parcelService.findAllActive();
    Calendar currentDate = Calendar.getInstance();
    ClimateRecord climateRecord = null;
    PlantingRecord plantingRecord = null;

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
    double extraterrestrialSolarRadiation = 0.0;
    double maximumInsolation = 0.0;
    double latitude = 0.0;
    double longitude = 0.0;

    /*
     * Obtiene y persiste los datos meteorologicos de la fecha
     * actual para todas las parcelas activas que NO tienen los
     * datos meteorologicos de dicha fecha
     */
    for (Parcel currentParcel : activeParcels) {
      /*
       * Si en la base de datos subyacente no existe un registro
       * climatico con la fecha actual para la parcela actual,
       * se persiste uno para dicha parcela
       */
      if (!climateRecordService.checkExistence(currentDate, currentParcel)) {
        latitude = currentParcel.getLatitude();
        longitude = currentParcel.getLongitude();

        /*
         * Retorna un registro climatico que contiene los datos
         * del conjunto de datos meteorologicos obtenido de la
         * API Visual Crossing Weather mediante la fecha actual
         * en formato UNIX y las coordenadas geograficas de la
         * parcela actual
         */
        climateRecord = ClimateClient.getForecast(currentParcel, unixTime);

        /*
         * Calculo de la evapotranspiracion del cultivo de
         * referencia (ETo) [mm/dia] en la fecha actual
         */
        extraterrestrialSolarRadiation = solarService.getRadiation(currentDate.get(Calendar.MONTH), latitude);
        eto = HargreavesEto.calculateEto(climateRecord.getMaximumTemperature(), climateRecord.getMinimumTemperature(), extraterrestrialSolarRadiation);
        climateRecord.setEto(eto);

        /*
         * Si la parcela actual tiene un registro de plantacion en
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
        if (plantingRecordService.checkOneInDevelopment(currentParcel)) {
          plantingRecord = plantingRecordService.findInDevelopment(currentParcel);

          /*
           * La formula de la evapotranspiracion del cultivo bajo
           * condiciones estandar (ETc) es la siguiente:
           * 
           * ETc = ETo * Kc
           * 
           * ETo: Evapotranspiracion del cultivo de referencia
           * Kc: Coeficiente de cultivo
           * 
           * Esta formula se encuentra en la pagina 6 del libro
           * "Evapotranspiracion del Cultivo" de la FAO.
           */
          etc = Etc.calculateEtc(eto, cropService.getKc(plantingRecord.getCrop(), plantingRecord.getSeedDate()));
        } else {
          etc = 0.0;
        }

        climateRecord.setEtc(etc);

        /*
         * Persiste los datos meteorologicos de la fecha
         * actual para una parcela en la base de datos
         * subyacente
         */
        climateRecordService.create(climateRecord);
      } // End if

    } // End for

  }

  /*
   * Establece de manera automatica el atributo modifiable de un registro
   * climatico del pasado (es decir, uno que tiene su fecha estrictamente
   * menor que la fecha actual) en false, ya que un registro climatico del
   * pasado NO se debe poder modificar. Esto lo hace cada 24 horas a partir
   * de las 00 horas.
   * 
   * La segunda anotacion @Schedule es para probar que este metodo se ejecuta
   * correctamente, es decir, que establece el atributo modifiable de un registro
   * climatico del pasado en false.
   * 
   * El archivo climateRecordInserts.sql de la ruta app/etc/sql tiene datos
   * para probar que este metodo se ejecuta correctamente, es decir, que hace
   * lo que se espera que haga.
   */
  // @Schedule(second = "*", minute = "*", hour = "0/23", persistent = false)
  // @Schedule(second = "*/10", minute = "*", hour = "*", persistent = false)
  private void unsetModifiable() {
    Collection<ClimateRecord> modifiableClimateRecords = climateRecordService.findAllModifiable();

    for (ClimateRecord currentClimateRecord : modifiableClimateRecords) {
      /*
       * Si un registro climatico modificable es del pasado (es decir, tiene
       * su fecha estrictamente menor que la fecha actual), se establece su
       * atributo modifiable en false, ya que un registro climatico del pasado
       * NO se debe poder modificar
       */
      if (climateRecordService.isFromPast(currentClimateRecord)) {
        climateRecordService.unsetModifiable(currentClimateRecord.getId());
      }

    }

  }

  /**
   * Calcula de manera automatica el agua excedente de cada registro
   * climatico de la fecha actual cada 24 horas a partir de las 23:59
   * horas. En otras palabras, calcula el agua excedente de cada
   * registro climatico que tiene la fecha actual al final del dia
   * actual, ya que si se lo hace en otro momento del dia actual se
   * puede obtener un valor desactualizado.
   * 
   * Esto se hace para cada uno de los registros climaticos de todas
   * las parcelas de la base de datos subyacente.
   * 
   * Se calcula el agua excedente porque influye en el calculo de la
   * necesidad de agua de riego de un cultivo en una fecha dada.
   * 
   * La segunda anotacion es para probar que el metodo hace lo que se
   * espera que haga: calcular el agua excedente de cada registro
   * climatico que tiene la fecha actual.
   */
  // @Schedule(second = "*", minute = "59", hour = "23/23", persistent = false)
  // @Schedule(second = "*/5", minute = "*", hour = "*", persistent = false)
  private void calculateExcessWater() {
    /*
     * El metodo getInstance de la clase Calendar retorna la
     * referencia a un objeto de tipo Calendar que contiene
     * la fecha actual
     */
    Calendar currentDate = Calendar.getInstance();
    Calendar yesterdayDate = UtilDate.getYesterdayDate();

    /*
     * Obtiene todos los registros climaticos que tienen
     * la fecha actual
     */
    Collection<ClimateRecord> climateRecords = climateRecordService.findAllByDate(currentDate);

    double excessWaterCurrentDate = 0.0;
    double totalIrrigationWaterCurrentDate = 0.0;
    double excessWaterYesterday = 0.0;

    /*
     * Calcula el agua excedente para cada registro climatico
     * que tiene la fecha actual. En otras palabras, calcula
     * el agua excedente de la fecha actual.
     */
    for (ClimateRecord currentClimateRecord : climateRecords) {
      totalIrrigationWaterCurrentDate = irrigationService.calculateTotalIrrigationWaterCurrentDate(currentClimateRecord.getParcel());

      /*
       * Si en la base de datos subyacente existe el registro climatico
       * del dia inmediatamente anterior a la fecha actual de la parcela
       * dada, se obtiene su agua excedente. Se obtiene el agua excedente
       * del dia inmediatamente anterior a la fecha actual porque influye
       * en el calculo del agua excedente de la fecha actual.
       * 
       * Si en la base de datos subyacente NO existe el registro climatico
       * del dia inmediatamente anterior a la fecha actual, se asume que
       * el agua excedente de dicho dia es 0.
       */
      if (climateRecordService.checkExistence(yesterdayDate, currentClimateRecord.getParcel())) {
        excessWaterYesterday = climateRecordService.find(yesterdayDate, currentClimateRecord.getParcel()).getExcessWater();
      } else {
        excessWaterYesterday = 0.0;
      }

      excessWaterCurrentDate = WaterMath.calculateExcessWater(currentClimateRecord.getEtc(),
          currentClimateRecord.getPrecip(), totalIrrigationWaterCurrentDate, excessWaterYesterday);

      climateRecordService.updateExcessWater(currentDate, currentClimateRecord.getParcel(), excessWaterCurrentDate);
    }

  }

}
