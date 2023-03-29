package climate;

import et.Eto;
import java.util.Calendar;
import java.util.Collection;
import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Stateless;
import model.ClimateRecord;
import model.PlantingRecord;
import model.Parcel;
import stateless.ClimateRecordServiceBean;
import stateless.CropServiceBean;
import stateless.PlantingRecordServiceBean;
import stateless.MaximumInsolationServiceBean;
import stateless.ParcelServiceBean;
import stateless.SolarRadiationServiceBean;

@Stateless
public class ClimateRecordManager {

  // inject a reference to the ParcelServiceBean
  @EJB ParcelServiceBean parcelService;

  // inject a reference to the ClimateRecordServiceBean
  @EJB ClimateRecordServiceBean climateRecordServiceBean;

  // inject a reference to the SolarRadiationServiceBean
  @EJB SolarRadiationServiceBean solarService;

  // inject a reference to the MaximumInsolationServiceBean
  @EJB MaximumInsolationServiceBean insolationService;

  // inject a reference to the PlantingRecordServiceBean
  @EJB PlantingRecordServiceBean plantingRecordService;

  // inject a reference to the CropServiceBean
  @EJB CropServiceBean cropService;

  /**
   * Obtiene y persiste de manera automatica los datos
   * metereologicos de la fecha actual para todas las
   * parcelas activas de la base de datos subyacente.
   * 
   * Esto lo realiza cada dos horas a partir de las 00:00
   * horas, debido a que quizas en alguna hora del dia,
   * la API climatica Visual Crossing Weather puede no
   * estar disponible al momento en el que este metodo
   * se ejecuta.
   * 
   * Los datos metereologicos son necesarios para determinar
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
   * datos metereologicos de la fecha actual para cada
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
     * Obtiene y persiste los datos metereologicos de la fecha
     * actual para todas las parcelas activas que NO tienen los
     * datos metereologicos de dicha fecha
     */
    for (Parcel currentParcel : activeParcels) {
      /*
       * Si en la base de datos subyacente no existe un registro
       * climatico con la fecha actual para la parcela actual,
       * se persiste uno para dicha parcela
       */
      if (!climateRecordServiceBean.checkExistence(currentDate, currentParcel)) {
        latitude = currentParcel.getLatitude();
        longitude = currentParcel.getLongitude();

        /*
         * Retorna un registro climatico que contiene los datos
         * del conjunto de datos metereologicos obtenido de la
         * API Visual Crossing Weather mediante la fecha actual
         * en formato UNIX y las coordenadas geograficas de la
         * parcela actual
         */
        climateRecord = ClimateClient.getForecast(currentParcel, unixTime);

        extraterrestrialSolarRadiation = solarService.getRadiation(currentDate.get(Calendar.MONTH), latitude);
        maximumInsolation = insolationService.getInsolation(currentDate.get(Calendar.MONTH), latitude);

        /*
         * Con los datos metereologicos obtenidos se calcula la
         * evapotranspiracion del cultivo de referencia (ETo)
         * [mm/dia]
         */
        eto = Eto.getEto(climateRecord.getMinimumTemperature(), climateRecord.getMaximumTemperature(),
            climateRecord.getAtmosphericPressure(), climateRecord.getWindSpeed(),
            climateRecord.getDewPoint(), extraterrestrialSolarRadiation, maximumInsolation, climateRecord.getCloudCover());

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
           * condiciones estandar (ETc) [mm/dia] es la siguiente:
           * 
           * ETc = kc * ETo
           * 
           * Esta formula esta en la pagina 6 del libro "Evapotranspiracion
           * del Cultivo, 56" de la FAO.
           */
          etc = cropService.getKc(plantingRecord.getCrop(), plantingRecord.getSeedDate()) * eto;
        } else {
          etc = 0.0;
        }

        climateRecord.setEtc(etc);

        /*
         * Persiste los datos metereologicos de la fecha
         * actual para una parcela en la base de datos
         * subyacente
         */
        climateRecordServiceBean.create(climateRecord);
      } // End if

    } // End for

  }

}
