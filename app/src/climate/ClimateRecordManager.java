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
  IrrigationRecordServiceBean irrigationRecordService;

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

  /**
   * Calcula el agua excedente de NUMBER_DAYS (constante de la clase
   * ClimateRecordServiceBean) registros climaticos anteriores a la
   * fecha actual y el agua excedente del registro climatico de la
   * fecha actual de cada una de las parcelas activas. Esto lo hace
   * cada dos horas a partir de las 00 horas.
   * 
   * Se calcula el agua excedente porque influye en el calculo de la
   * necesidad de agua de riego de un cultivo en una fecha dada.
   * 
   * La segunda anotacion es para probar que el metodo hace lo que se
   * espera que haga.
   */
  // @Schedule(second = "*", minute = "*", hour = "0/2", persistent = false)
  @Schedule(second = "*/5", minute = "*", hour = "*", persistent = false)
  private void calculateExcessWaterForPeriod() {
    Collection<Parcel> activeParcels = parcelService.findAllActive();

    for (Parcel currentParcel : activeParcels) {
      /*
       * Calcula y actualiza la ETo (evapotranspiracion del cultivo
       * de referencia) y la ETc (evapotranspiracion del cultivo
       * bajo condiciones estandar) de NUMBER_DAYS registros
       * climaticos de una parcela activa anteriores a la fecha
       * actual y del registro climatico de la fecha actual de una
       * parcela activa.
       * 
       * El agua excedente de una parcela en una fecha dada se
       * calcula mediante la ETo o la ETc, entre otros datos.
       * La ETc se utiliza si una parcela tuvo un cultivo plantado
       * en una fecha dada. En caso contrario, se utiliza la ETo.
       * 
       * Este metodo es necesario para los casos en los que se
       * modifican los coeficientes (KCs) de un cultivo, las
       * temperaturas maximas y minimas, y/o la fecha de un
       * registro climatico. En base a la fecha de un registro
       * climatico se obtiene la radiacion solar extraterrestre,
       * la cual es necesaria para calcular la ETc de un cultivo.
       * Por lo tanto, si estos valores son modificados es necesario
       * recalcular la ETo y la ETc de los registros climaticos
       * sobre los que se va a actualizar el agua excedente, ya que
       * esta se calcula en base a la ETo o la ETc (si una parcela
       * tuvo un cultivo plantado en una fecha dada). Este es el
       * motivo por el cual este metodo se debe invocar antes de
       * los metodos calculateExcessWaterForPeriod y calculateExcessWaterCurrentDate.
       */
      calculateEtForPeriod(currentParcel);

      /*
       * Calcula el agua excedente de los NUMBER_DAYS registros
       * climaticos de una parcela activa anteriores a la fecha
       * actual
       */
      calculateExcessWaterForPeriod(currentParcel);

      /*
       * Calcula el agua excedente del registro climatico de la
       * fecha actual de una parcela activa. Para calcular correctamente
       * el agua excedente que hay en una parcela en la fecha actual
       * primero se debe calcular el agua excedente que hubo en la
       * misma en NUMBER_DAYS fechas anteriores a la fecha actual.
       * Este es el motivo por el cual este metodo se invoca despues
       * de invocar al metodo calculateExcessWaterForPeriod.
       */
      calculateExcessWaterCurrentDate(currentParcel);
    }

  }

  /**
   * Calcula el agua excedente que hay en una parcela en la fecha
   * actual y el resultado de dicho calculo lo actualiza en el
   * atributo del agua excedente del registro climatico de la
   * fecha actual de una parcela dada
   * 
   * @param givenParcel
   */
  private void calculateExcessWaterCurrentDate(Parcel givenParcel) {
    /*
     * El metodo getInstance de la clase Calendar retorna la
     * referencia a un objeto de tipo Calendar que contiene
     * la fecha actual
     */
    Calendar currentDate = Calendar.getInstance();
    Calendar yesterdayDate = UtilDate.getYesterdayDate();
    ClimateRecord currentClimateRecord = null;

    double excessWaterCurrentDate = 0.0;
    double totalIrrigationWaterCurrentDate = 0.0;
    double excessWaterYesterday = 0.0;

    /*
     * Si existe el registro climatico de la fecha actual de una parcela,
     * se calcula el agua excedente que hay en la misma en la fecha actual
     * y el resultado de este calculo se lo utiliza para actualizar el agua
     * excedente de dicho registro climatico
     */
    if (climateRecordService.checkExistence(currentDate, givenParcel)) {
      currentClimateRecord = climateRecordService.find(currentDate, givenParcel);
      totalIrrigationWaterCurrentDate = irrigationRecordService.calculateTotalIrrigationWaterCurrentDate(currentClimateRecord.getParcel());

      /*
       * Si en la base de datos subyacente existe el registro climatico
       * del dia inmediatamente anterior a la fecha actual de la parcela
       * dada, se obtiene su agua excedente. Se obtiene el agua excedente
       * del dia inmediatamente anterior a la fecha actual porque influye
       * en el calculo del agua excedente de la fecha actual. En caso
       * contrario, se asume que el agua excedente de dicho dia es 0.
       */
      if (climateRecordService.checkExistence(yesterdayDate, currentClimateRecord.getParcel())) {
        excessWaterYesterday = climateRecordService.find(yesterdayDate, currentClimateRecord.getParcel()).getExcessWater();
      } else {
        excessWaterYesterday = 0.0;
      }

      /*
       * Si la parcela del registro climatico actual tiene un
       * registro de plantacion en desarrollo (lo cual representa
       * que tiene un cultivo en desarrollo), se calcula el agua
       * excedente que hay en la misma en la fecha actual haciendo
       * uso de la ETc de dicho cultivo. En caso contrario, se calcula
       * el agua excedente que hay en la misma en la fecha actual
       * haciendo uso de la ETo del registro climatico actual.
       */
      if (plantingRecordService.checkOneInDevelopment(currentClimateRecord.getParcel())) {
        excessWaterCurrentDate = WaterMath.calculateExcessWater(currentClimateRecord.getEtc(),
            currentClimateRecord.getPrecip(), totalIrrigationWaterCurrentDate, excessWaterYesterday);
      } else {
        excessWaterCurrentDate = WaterMath.calculateExcessWater(currentClimateRecord.getEto(),
            currentClimateRecord.getPrecip(), totalIrrigationWaterCurrentDate, excessWaterYesterday);
      }

      /*
       * Actualizacion del agua excedente del registro
       * climatico de la fecha actual
       */
      climateRecordService.updateExcessWater(currentDate, currentClimateRecord.getParcel(), excessWaterCurrentDate);
    } // End if

  }

  /**
   * Calcula el agua excedente de NUMBER_DAYS registros climaticos
   * de una parcela anteriores a la fecha actual
   * 
   * @param givenParcel
   */
  private void calculateExcessWaterForPeriod(Parcel givenParcel) {
    /*
     * El valor de esta variable se utiliza:
     * - para obtener y persistir los registros climaticos de una parcela
     * anteriores a la fecha actual.
     * - para calcular el agua excedente de cada uno de los registros
     * climaticos de una parcela anteriores a la fecha actual.
     * - para recalcular la ETo y la ETc de cada uno de los registros
     * climaticos de una parcela anteriores a la fecha actual.
     */
    int numberDays = climateRecordService.getNumberDays();

    /*
     * El metodo getInstance de la clase Calendar retorna
     * la referencia a un objeto de tipo Calendar que
     * contiene la fecha actual
     */
    Calendar currentDate = Calendar.getInstance();
    Calendar givenDate = Calendar.getInstance();

    /*
     * El agua excedente de los registros climaticos de una
     * parcela anteriores a la fecha actual, se debe calcular
     * desde atras hacia adelante, ya que el agua excedente de
     * un dia es agua a favor para el dia inmediatamente siguiente.
     * 
     * Por lo tanto, se debe comenzar a calcular el agua excedente
     * de los registros climticos de una parcela anteriores a la
     * fecha actual desde el mas antiguo de ellos hasta el mas
     * actual de ellos. Estos registros climaticos son obtenidos
     * y persistidos por el metodo requestAndPersistClimateRecordsForPeriod
     * de la clase PlantingRecordManager si no existen en la base
     * de datos subyacente.
     */
    givenDate.set(Calendar.DAY_OF_YEAR, (currentDate.get(Calendar.DAY_OF_YEAR) - numberDays));

    /*
     * Esta variable se utiliza para obtener el dia inmeditamente
     * anterior a una fecha dada en la instruccion for de mas
     * abajo. Esto es necesario para obtener el agua excedente
     * de una parcela en el dia inmediatamente anterior a una
     * fecha dada.
     */
    Calendar yesterdayDate = Calendar.getInstance();

    ClimateRecord givenClimateRecord = null;

    double excessWaterGivenDate = 0.0;
    double excessWaterYesterday = 0.0;
    double totalIrrigationWaterGivenDate = 0.0;
    double givenEt = 0.0;

    /*
     * Calcula el agua excedente de cada uno de los registros
     * climaticos de una parcela anteriores a la fecha actual
     * desde el mas antiguo de ellos hasta el mas actual de
     * ellos. Estos registros climaticos son obtenidos y persistidos
     * por el metodo requestAndPersistClimateRecordsForPeriod
     * de la clase PlantingRecordManager si no existen en la base
     * de datos subyacente.
     */
    for (int i = 1; i < numberDays + 1; i++) {
      yesterdayDate.set(Calendar.DAY_OF_YEAR, (givenDate.get(Calendar.DAY_OF_YEAR) - 1));

      /*
       * Si el registro climatico del dia inmediatamente anterior
       * a una fecha dada existe, se obtiene su agua excedente
       * para calcular el agua excedente del registro climatico
       * de una fecha dada. En caso contrario, se asume que el
       * agua excedente del dia inmediatamente anterior a una
       * fecha dada es 0.0.
       */
      if (climateRecordService.checkExistence(yesterdayDate, givenParcel)) {
        excessWaterYesterday = climateRecordService.find(yesterdayDate, givenParcel).getExcessWater();
      }

      /*
       * Si existe el registro climatico de una fecha dada de una parcela,
       * se calcula el agua excedente que hay en la misma en la fecha dada
       * y el resultado de este calculo se utiliza para actualizar el agua
       * excedente del registro climatico de la fecha dada
       */
      if (climateRecordService.checkExistence(givenDate, givenParcel)) {
        /*
         * Obtiene uno de los registros climaticos de una parcela dada
         * anteriores a la fecha actual, los cuales son obtenidos y
         * persistidos por el metodo requestAndPersistClimateRecordsForPeriod
         * de la clase PlantingRecordManager si NO existen en la base de
         * datos subyacente
         */
        givenClimateRecord = climateRecordService.find(givenDate, givenParcel);
        totalIrrigationWaterGivenDate = irrigationRecordService.calculateTotalIrrigationWaterGivenDate(givenDate, givenParcel);

        /*
         * Cuando una parcela NO tiene un cultivo sembrado y en
         * desarrollo, la ETc de uno o varios de sus registros
         * climaticos tiene el valor 0.0, ya que si no hay un
         * cultivo en desarrollo NO es posible calcular la ETc
         * (evapotranspiracion del cultivo bajo condiciones
         * estandar) del mismo. Por lo tanto, se debe utilizar la
         * ETo (evapotranspiracion del cultivo de referencia) para
         * calcular el agua excedente de un registro climatico
         * en una fecha dada.
         * 
         * En caso contrario, se debe utilizar la ETc para calcular
         * el agua excedente de un registro climatico en una fecha
         * dada.
         */
        if (givenClimateRecord.getEtc() == 0.0) {
          givenEt = givenClimateRecord.getEto();
        } else {
          givenEt = givenClimateRecord.getEtc();
        }

        /*
         * Calculo del agua excedente de una parcela dada
         * en una fecha dada
         */
        excessWaterGivenDate = WaterMath.calculateExcessWater(givenEt, givenClimateRecord.getPrecip(),
            totalIrrigationWaterGivenDate, excessWaterYesterday);

        /*
         * Actualizacion del agua excedente del registro
         * climatico de una fecha dada
         */
        climateRecordService.updateExcessWater(givenDate, givenParcel, excessWaterGivenDate);
      }

      /*
       * Se restablece el valor por defecto de esta variable,
       * ya que de lo contrario se calculara erroneamente el
       * agua excedente que hay en una parcela en una fecha
       * dada
       */
      excessWaterYesterday = 0.0;

      /*
       * El agua excedente de los registros climaticos de una
       * parcela anteriores a la fecha actual se calcula desde
       * atras hacia adelante, ya que el agua excedente de un
       * dia es agua a favor para el dia inmediatamente siguiente.
       * Por lo tanto, se comienza a calcular el agua excedente
       * de estos registros climaticos desde el mas antiguo de
       * ellos. En consecuencia, para calcular el agua excedente
       * del siguiente registro climatico se debe calcular la fecha
       * siguiente.
       */
      givenDate.set(Calendar.DAY_OF_YEAR, ((currentDate.get(Calendar.DAY_OF_YEAR) - numberDays) + i));
    } // End for

  }

  /**
   * Calcula y actualiza la ETo (evapotranspiracion del cultivo
   * de referencia) y la ETc (evapotranspiracion del cultivo bajo
   * condiciones estandar) de numberDays registros climaticos de
   * una parcela anteriores a la fecha actual y del registro
   * climatico de la fecha actual de una parcela
   * 
   * @param givenParcel
   */
  private void calculateEtForPeriod(Parcel givenParcel) {
    /*
     * El valor de esta variable se utiliza:
     * - para obtener y persistir los registros climaticos de una parcela
     * anteriores a la fecha actual.
     * - para calcular el agua excedente de cada uno de los registros
     * climaticos de una parcela anteriores a la fecha actual.
     * - para recalcular la ETo y la ETc de cada uno de los registros
     * climaticos de una parcela anteriores a la fecha actual.
     */
    int numberDays = climateRecordService.getNumberDays();

    /*
     * El metodo getInstance de la clase Calendar retorna
     * la referencia a un objeto de tipo Calendar que
     * contiene la fecha actual
     */
    Calendar currentDate = Calendar.getInstance();
    Calendar givenDate = Calendar.getInstance();
    ClimateRecord givenClimateRecord = null;
    PlantingRecord givenPlantingRecord = null;

    /*
     * No hay un motivo para calcular la ET (ETo o ETc) de los
     * registros climaticos de una parcela desde la fecha
     * actual - numberDays hasta la fecha actual, es decir,
     * desde una cantidad numberDays de dias atras hasta el
     * dia actual. Esto tambien se puede hacer desde la fecha
     * actual hacia una cantidad numberDays de dias hacia atras.
     */
    givenDate.set(Calendar.DAY_OF_YEAR, (currentDate.get(Calendar.DAY_OF_YEAR) - numberDays));

    double eto = 0.0;
    double etc = 0.0;
    double kc = 0.0;
    double extraterrestrialSolarRadiation = 0.0;

    /*
     * Calcula y actualiza la ETo (evapotranspiracion del cultivo
     * de referencia) y la ETc (evapotranspiracion del cultivo
     * bajo condiciones estandar) de numberDays registros climaticos
     * de una parcela anteriores a la fecha actual y del registro
     * climatico de la fecha actual de una parcela.
     * 
     * El "+ 2" en la condicion de la instruccion for es para
     * calcular la ETo y la ETc del registro climatico de la
     * fecha actual de una parcela.
     */
    for (int i = 1; i < numberDays + 2; i++) {
      /*
       * Si existe el registro climatico de una fecha dada de una
       * parcela, se calcula y actualiza su ETo y su ETc.
       * 
       * La ETc se calcula y actualiza si una parcela tiene o tuvo
       * un cultivo plantado en una fecha dada. Esto se traduce a
       * si en una fecha dada una parcela tuvo o tiene un registro
       * de plantacion.
       */
      if (climateRecordService.checkExistence(givenDate, givenParcel)) {
        givenClimateRecord = climateRecordService.find(givenDate, givenParcel);

        /*
         * Calculo de la evapotranspiracion del cultivo
         * de referencia (ETo) en la fecha dada
         */
        extraterrestrialSolarRadiation = solarService.getRadiation(givenDate.get(Calendar.MONTH), givenParcel.getLatitude());
        eto = HargreavesEto.calculateEto(givenClimateRecord.getMaximumTemperature(), givenClimateRecord.getMinimumTemperature(), extraterrestrialSolarRadiation);

        /*
         * Si una parcela tiene un registro de plantacion en una fecha
         * dada, se calcula el KC (coeficiente de cultivo) del cultivo
         * plantado en dicha fecha para calcular la ETc (evapotranspiracion
         * del cultivo bajo condiciones estandar) del mismo
         */
        if (plantingRecordService.checkExistence(givenParcel, givenDate)) {
          givenPlantingRecord = plantingRecordService.find(givenParcel, givenDate);
          kc = cropService.getKc(givenPlantingRecord.getCrop(), givenPlantingRecord.getSeedDate(), givenDate);
          etc = Etc.calculateEtc(eto, kc);
        }

        /*
         * Actualizacion de la ETo (evapotranspiracion del cultivo de
         * referencia) y la ETc (evapotranspiracion del cultivo bajo
         * condiciones estandar) del registro climatico correspondiente
         * a una fecha y una parcela dadas
         */
        climateRecordService.updateEtoAndEtc(givenDate, givenParcel, eto, etc);

        /*
         * Luego de calcular y actualizar la ETc de un registro climatico
         * correspondiente a una fecha y una parcela dadas, se restablece
         * el valor por defecto de esta variable para evitar actualizaciones
         * erroneas en la ETc de los siguientes registros climaticos 
         */
        etc = 0.0;
      } // End if

      /*
       * Se decidio calcular la ET (ETo o ETc) de los registros
       * climaticos de una parcela desde una cantidad numberDays
       * de dias atras hasta la fecha actual, incluida. Por lo
       * tanto, para calcular la ET del siguiente registro climatico
       * se calcula la fecha siguiente.
       */
      givenDate.set(Calendar.DAY_OF_YEAR, ((currentDate.get(Calendar.DAY_OF_YEAR) - numberDays) + i));
    } // End for

  }

}
