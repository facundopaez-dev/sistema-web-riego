/*
 * Generador de registros historicos de riego para
 * cada parcela existente en el sistema
 *
 * Esta clase es el modulo encargado de crear y almacenar
 * de forma automatica registros historicos de riego para
 * cada parcela para cada dia del año siempre y cuando no
 * exista ningun registro historico de riego en el dia
 * dado para cada parcela existente em eñ sistema
 */

package irrigation;

import climate.ClimateClient;
import irrigation.WaterMath;
import java.lang.Math;
import java.util.Calendar;
import java.util.Collection;
import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Stateless;
import model.ClimateRecord;
import model.IrrigationRecord;
import model.Parcel;
import stateless.ClimateRecordServiceBean;
import stateless.IrrigationRecordServiceBean;
import stateless.ParcelServiceBean;
import util.UtilDate;

@Stateless
public class IrrigationRecordGenerator {

  // inject a reference to the ParcelServiceBean
  @EJB ParcelServiceBean parcelService;

  // inject a reference to the IrrigationRecordServiceBean
  @EJB IrrigationRecordServiceBean irrigationRecordService;

  // inject a reference to the ClimateRecordServiceBean
  @EJB ClimateRecordServiceBean climateRecordServiceBean;

  /**
   * Este metodo tiene la finalidad crear y almacenar
   * un unico registro historico de riego para cada
   * dia para cada parcela que no tenga ningun registro
   * historico de riego en el dia dado, lo cual hara a
   * partir de las 23:59 en la fecha actual del sistema
   * y sera en esta hora porque es al final del dia cuando
   * se puede saber si cada parcela existente en el sistema
   * tiene o no registros historicos de riego en el dia dado
   *
   * @param second="*"
   * @param minute="59"
   * @param hour="23"
   * @param persistent=false
   */
  // @Schedule(second="*", minute="59", hour="23", persistent=false)
  // @Schedule(second="*/5", minute="*", hour="*", persistent=false)
  private void execute() {
    Collection<Parcel> parcels = parcelService.findAll();
    Calendar currentDate = Calendar.getInstance();

    /*
     * Registro actual de riego, es decir,
     * registro de riego para el dia de hoy
     */
    IrrigationRecord currentIrrigationRecord = null;

    /*
     * Riego sugerido actual, es decir,
     * riego sugerido para el dia de hoy
     */
    double currentSuggestedIrrigation = 0.0;

    /*
     * Variables para almacenar temporalmente los
     * datos del dia de ayer
     */
    ClimateRecord yesterdayClimateLog = null;
    double yesterdayEto = 0.0;
    double yesterdayEtc = 0.0;
    double yesterdayPrecip = 0.0;
    double waterAccumulatedYesterday = 0.0;

    /*
     * Fecha inmediatamente siguiente a la fecha
     * actual para recuperar el registro climatico,
     * de una parcela dada, en la fecha siguiente
     * a la fecha actual
     */
    Calendar tomorrowDate = UtilDate.getTomorrowDate();
    ClimateRecord tomorrowClimateLog = null;

    /*
     * Fecha inmediatamente anterior a la fecha
     * actual para recuperar el registro climatico,
     * de una parcela dada, en la fecha anterior a
     * la fecha actual
     */
    Calendar yesterdayDate = UtilDate.getYesterdayDate();

    for (Parcel currentParcel : parcels) {
      /*
       * Si la parcela dada no tiene asociado un registro historico
       * de riego en la fecha dada (en este caso la actual), se tiene
       * que crear uno para la fecha dada y asociarlo a la misma
       */
      if (!irrigationRecordService.exist(currentDate, currentParcel)) {
        currentIrrigationRecord = new IrrigationRecord();

        /*
         * Establece la fecha del registro de riego
         */
        currentIrrigationRecord.setDate(currentDate);

        /*
         * Recupera el registro climatico de la parcela
         * dada de la fecha inmediatamente anterior a la
         * fecha actual
         */
        // TODO: Seguridad, mas adelante refactorizar
        yesterdayClimateLog = climateRecordServiceBean.find(yesterdayDate, currentParcel);
        yesterdayEto = yesterdayClimateLog.getEto();
        yesterdayEtc = yesterdayClimateLog.getEtc();

        /*
         * El atributo precip del modelo de datos ClimateRecord representa
         * la precipitacion del dia en milimetros. La unidad en la que se
         * mide este dato corresponde a la API Visual Crossing Weather y
         * al grupo de unidades en el que se le solicita datos metereologicos.
         */
        yesterdayPrecip = yesterdayClimateLog.getPrecip();
        waterAccumulatedYesterday = yesterdayClimateLog.getWaterAccumulated();

        /*
         * Lo que esta dentro de la sentencia de seleccion se ejecuta
         * si no hay un registro historico de riego de la parcela dada
         * en la fecha dada (la de hoy, en este caso), con lo cual al
         * no haber registros historicos de riego de la parcela dada
         * en la fecha dada, la cantidad total de agua utilizada en
         * el riego en el dia de hoy es 0.0
         */
        currentSuggestedIrrigation = WaterMath.getSuggestedIrrigation(currentParcel.getHectare(), yesterdayEtc, yesterdayEto, yesterdayPrecip, waterAccumulatedYesterday, 0.0);
        currentIrrigationRecord.setSuggestedIrrigation(currentSuggestedIrrigation);

        /*
         * Se recuperan los datos metereologicos del dia de mañana
         * y de ellos se usa la precipitacion del dia de mañana para
         * establecerla como precipitacion de dicho dia en el registro
         * de riego del dia de hoy.
         * 
         * La precipitacion del dia de mañana en este registro de
         * riego solo es un dato informativo, NO se lo usa con otro
         * fin mas que para informar, en el registro de riego del dia
         * de hoy, la precipitacion del dia de mañana.
         */
        tomorrowClimateLog = ClimateClient.getForecast(currentParcel, (tomorrowDate.getTimeInMillis() / 1000));
        currentIrrigationRecord.setTomorrowPrecipitation(WaterMath.truncateToThreeDecimals(tomorrowClimateLog.getPrecip()));

        /*
         * Si este bloque de codigo fuente se ejecuta es porque
         * el usuario cliente del sistema nunca ha registrado
         * los riegos que ha realizado en el dia actual (si
         * es que los realizo) para la parcela dada, con lo
         * cual, el riego realizado en el dia de hoy sera
         * establecido por el sistema con el valor 0.0 de
         * forma predetermianda
         *
         * Si el usuario nunca ha ingresado su riego
         * realizado en el dia de hoy, el sistema mismo
         * lo establece en cero porque tiene que tener
         * un registro del riego que no ha realizado el
         * usuario cliente
         */
        currentIrrigationRecord.setIrrigationDone(0.0);
        currentIrrigationRecord.setParcel(currentParcel);

        /*
         * Se crea en la base de datos subyacente el
         * registro historico de riego asociado a la
         * parcela dada
         */
        irrigationRecordService.create(currentIrrigationRecord);
      } // End if

    } // End for

  }

}
