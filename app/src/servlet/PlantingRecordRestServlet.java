package servlet;

import climate.ClimateLogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import et.Eto;
import irrigation.WaterMath;
import java.io.IOException;
import java.lang.Math;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import javax.ejb.EJB;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import model.ClimateRecord;
import model.Crop;
import model.InstanceParcelStatus;
import model.PlantingRecord;
import model.IrrigationLog;
import model.Parcel;
import stateless.ClimateRecordServiceBean;
import stateless.CropServiceBean;
import stateless.InstanceParcelStatusServiceBean;
import stateless.PlantingRecordServiceBean;
import stateless.IrrigationLogServiceBean;
import stateless.MaximumInsolationServiceBean;
import stateless.ParcelServiceBean;
import stateless.SolarRadiationServiceBean;
import util.UtilDate;

@Path("/plantingRecords")
public class PlantingRecordRestServlet {

  // inject a reference to the PlantingRecordServiceBean slsb
  @EJB PlantingRecordServiceBean plantingRecordService;

  // inject a reference to the ClimateRecordServiceBean slsb
  @EJB ClimateRecordServiceBean climateRecordServiceBean;

  // inject a reference to the CropServiceBean slsb
  @EJB CropServiceBean cropService;

  // inject a reference to the IrrigationLogServiceBean slsb
  @EJB IrrigationLogServiceBean irrigationLogService;

  // inject a reference to the InstanceParcelStatusServiceBean slsb
  @EJB InstanceParcelStatusServiceBean statusService;

  // inject a reference to the ParcelServiceBean slsb
  @EJB ParcelServiceBean serviceParcel;

  // inject a reference to the SolarRadiationServiceBean
  @EJB SolarRadiationServiceBean solarService;

  // inject a reference to the MaximumInsolationServiceBean
  @EJB MaximumInsolationServiceBean insolationService;

  // mapea lista de pojo a JSON
  ObjectMapper mapper = new ObjectMapper();

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public String findAll() throws IOException {
    Collection<PlantingRecord> plantingRecords = plantingRecordService.findAll();
    return mapper.writeValueAsString(plantingRecords);
  }

  @GET
  @Path("/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public String find(@PathParam("id") int id) throws IOException {
    PlantingRecord plantingRecord = plantingRecordService.find(id);
    return mapper.writeValueAsString(plantingRecord);
  }

  @GET
  @Path("/findCurrentPlantingRecord/{parcelId}")
  @Produces(MediaType.APPLICATION_JSON)
  public String findCurrentPlantingRecord(@PathParam("parcelId") int parcelId) throws IOException {
    Parcel givenParcel = serviceParcel.find(parcelId);
    PlantingRecord plantingRecord = plantingRecordService.findInDevelopment(givenParcel);
    return mapper.writeValueAsString(plantingRecord);
  }

  @GET
  @Path("/findNewestPlantingRecord/{parcelId}")
  @Produces(MediaType.APPLICATION_JSON)
  public String findNewestPlantingRecord(@PathParam("parcelId") int parcelId) throws IOException {
    Parcel givenParcel = serviceParcel.find(parcelId);
    PlantingRecord newestPlantingRecord = plantingRecordService.findLastFinished(givenParcel);
    return mapper.writeValueAsString(newestPlantingRecord);
  }

  // @GET
  // @Path("/checkStageCropLife/{idCrop}")
  // @Produces(MediaType.APPLICATION_JSON)
  // public String checkStageCropLife(@PathParam("idCrop") int idCrop, @QueryParam("fechaSiembra") String fechaSiembra,
  // @QueryParam("fechaCosecha") String fechaCosecha) throws IOException, ParseException {
  //   Crop choosenCrop = cropService.find(idCrop);
  //
  //   SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
  //   Calendar seedDate = Calendar.getInstance();
  //   Calendar harvestDate = Calendar.getInstance();
  //
  //   /*
  //    * Fechas convertidas de String a Date
  //    */
  //   Date dateSeedDate = new Date(dateFormatter.parse(fechaSiembra).getTime());
  //   Date dateHarvestDate = new Date(dateFormatter.parse(fechaCosecha).getTime());
  //
  //   seedDate.set(dateSeedDate.getYear(), dateSeedDate.getMonth(), dateSeedDate.getDate());
  //   harvestDate.set(dateHarvestDate.getYear(), dateHarvestDate.getMonth(), dateHarvestDate.getDate());
  //
  //   /*
  //    * Si la diferencia en dias entre la fecha de siembra
  //    * y la fecha de cosecha ingresadas es mayor a la cantidad
  //    * total de dias de vida que vive el cultivo dado, retorna
  //    * el cultivo dado como "error"
  //    */
  //   if (excessStageLife(choosenCrop, seedDate, harvestDate)) {
  //     return mapper.writeValueAsString(choosenCrop);
  //   }
  //
  //   return mapper.writeValueAsString(null);
  // }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  public String create(String json) throws IOException  {
    PlantingRecord newPlantingRecord = mapper.readValue(json, PlantingRecord.class);
    PlantingRecord lastPlantingRecord = plantingRecordService.findLastFinished(newPlantingRecord.getParcel());

    /*
     * Si la fecha de cosecha del ultimo registro de plantacion finalizado
     * es mayor o igual que la fecha de siembra del nuevo registro de
     * plantacion, entonces no se tiene que persistir el nuevo registro de
     * plantacion
     */
    if ((lastPlantingRecord != null) && ((lastPlantingRecord.getHarvestDate().compareTo(newPlantingRecord.getSeedDate())) >= 0)) {
      return null;
    }

    /*
     * El registro de plantacion actual es el que esta en el
     * estado "En desarrollo"
     */
    PlantingRecord currentPlantingRecord = plantingRecordService.findInDevelopment(newPlantingRecord.getParcel());

    /*
     * Si no hay un registro historico actual de parcela
     * entonces se procede a crear el nuevo registro historico
     * de parcela, el cual es el actual porque su cultivo al
     * estar en este nuevo registro historico actual de parcela
     * aun no ha llegado a su fecha de cosecha
     */
    if (currentPlantingRecord == null) {
      /*
       * En funcion de la fecha de siembra del cultivo dado y
       * de la suma de sus dias de vida (suma de la cantidad de
       * dias que dura cada una de sus etapas), se calcula
       * la fecha de cosecha del cultivo dado
       */
      Calendar harvestDate = cropService.calculateHarvestDate(newPlantingRecord.getSeedDate(), newPlantingRecord.getCrop());
      newPlantingRecord.setHarvestDate(harvestDate);
      newPlantingRecord.setStatus(getStatus(harvestDate));

      newPlantingRecord = plantingRecordService.create(newPlantingRecord);
      return mapper.writeValueAsString(newPlantingRecord);
    }

    return null;
  }

  @DELETE
  @Path("/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public String remove(@PathParam("id") int id) throws IOException {
    PlantingRecord givenPlantingRecord = plantingRecordService.remove(id);
    return mapper.writeValueAsString(givenPlantingRecord);
  }

  @PUT
  @Path("/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public String modify(@PathParam("id") int id, String json) throws IOException  {
    PlantingRecord givenPlantingRecord = mapper.readValue(json,PlantingRecord.class);

    PlantingRecord previuosPlantingRecord = plantingRecordService.find(givenPlantingRecord.getParcel(), givenPlantingRecord.getId() - 1);
    PlantingRecord nextPlantingRecord = plantingRecordService.find(givenPlantingRecord.getParcel(), givenPlantingRecord.getId() + 1);

    /*
     * Si la fecha de cosecha del registro de plantacion anterior al que
     * se va a modificar es mayor a la fecha de siembra del registro de
     * plantacion que se va a modificar, no se tiene que realizar la modificacion
     */
    if ((previuosPlantingRecord != null) && ((previuosPlantingRecord.getHarvestDate().compareTo(givenPlantingRecord.getSeedDate()) == 0)
    || (previuosPlantingRecord.getHarvestDate().compareTo(givenPlantingRecord.getSeedDate()) > 0))) {
      return null;
    }

    /*
     * Si la fecha de cosecha del registro de plantacion que se va a modificar
     * es mayor que la fecha de siembra del siguiente registro de plantacion
     * que se va a modificar, no se tiene que realizar la modificacion
     */
    if ((nextPlantingRecord != null) && ((givenPlantingRecord.getHarvestDate().compareTo(nextPlantingRecord.getSeedDate()) == 0)
    || (givenPlantingRecord.getHarvestDate().compareTo(nextPlantingRecord.getSeedDate()) > 0))) {
      return null;
    }

    /*
     * Si la fecha de siembra y la fecha de cosecha del registro de
     * plantacion que se va a modificar coinciden, no se tiene que
     * realizar la modificacion
     */
    if ((givenPlantingRecord.getSeedDate() != null) && (givenPlantingRecord.getHarvestDate() != null) && (givenPlantingRecord.getSeedDate().compareTo(givenPlantingRecord.getHarvestDate()) == 0)) {
      return null;
    }

    // if (givenPlantingRecord.getStatus().getName().equals("Finalizado")) {
    //   givenPlantingRecord = plantingRecordService.modify(id, givenPlantingRecord);
    //   return mapper.writeValueAsString(givenPlantingRecord);
    // }

    /*
     * NOTE: Falta hacerlo funcionar
     *
     * Si la diferencia en dias entre la fecha de siembra y la fecha
     * de cosecha ingresadas no es mayor que la cantidad de dias que
     * dura la etapa de vida del cultivo dado se realiza la modificacion
     * del registro historico de parcela dado
     */
    // if (!(excessStageLife(givenPlantingRecord.getCrop(), givenPlantingRecord.getSeedDate(), givenPlantingRecord.getHarvestDate()))) {
    //   givenPlantingRecord.setStatus(getStatus(givenPlantingRecord.getHarvestDate()));
    //   givenPlantingRecord = plantingRecordService.modify(id, givenPlantingRecord);
    //   return mapper.writeValueAsString(givenPlantingRecord);
    // }

    givenPlantingRecord.setStatus(getStatus(givenPlantingRecord.getHarvestDate()));
    givenPlantingRecord = plantingRecordService.modify(id, givenPlantingRecord);
    return mapper.writeValueAsString(givenPlantingRecord);
  }

  @GET
  @Path("/suggestedIrrigation/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public String getSuggestedIrrigation(@PathParam("id") int id) throws IOException {
    PlantingRecord givenPlantingRecord = plantingRecordService.find(id);
    Parcel parcel = givenPlantingRecord.getParcel();
    double suggestedIrrigationToday = 0.0;
    double tomorrowPrecipitation = 0.0;

    ClimateLogService climateLogService = ClimateLogService.getInstance();
    ClimateRecord yesterdayClimateLog = null;
    double yesterdayEto = 0.0;
    double yesterdayEtc = 0.0;
    double extraterrestrialSolarRadiation = 0.0;
    double maximumInsolation = 0.0;

    /*
     * Fecha actual del sistema
     */
    Calendar currentDate = Calendar.getInstance();

    /*
     * Fecha del dia de mañana para solicitar la precipitacion
     * del dia de mañana
     */
    Calendar tomorrowDate = UtilDate.getTomorrowDate();

    /*
     * Solicita el registro del clima del dia de mañana
     */
    ClimateRecord tomorrowClimateLog = climateLogService.getClimateLog(parcel.getLatitude(), parcel.getLongitude(), (tomorrowDate.getTimeInMillis() / 1000));
    tomorrowPrecipitation = tomorrowClimateLog.getRainWater();

    /*
     * Fecha del dia inmediatamente anterior a la fecha
     * actual del sistema
     */
    Calendar yesterdayDate = UtilDate.getYesterdayDate();

    /*
     * Cantidad total de agua utilizada en los riegos
     * realizados en el dia de hoy
     */
    double totalIrrigationWaterToday = irrigationLogService.getTotalWaterIrrigationToday(parcel);

    /*
     * Si el registro climatico del dia de ayer no existe en
     * la base de datos, se lo tiene que pedir y se lo tiene
     * que persistir en la base de datos subyacente
     */
    if (!(climateRecordServiceBean.exist(yesterdayDate, parcel))) {
      yesterdayClimateLog = climateLogService.getClimateLog(parcel.getLatitude(), parcel.getLongitude(), (yesterdayDate.getTimeInMillis() / 1000));

      extraterrestrialSolarRadiation = solarService.getRadiation(yesterdayDate.get(Calendar.MONTH), parcel.getLatitude());
      maximumInsolation = insolationService.getInsolation(yesterdayDate.get(Calendar.MONTH), parcel.getLatitude());

      /*
       * Evapotranspiracion del cultivo de referencia (ETo) con las
       * condiciones climaticas del registro climatico del dia de ayer
       */
      yesterdayEto = Eto.getEto(yesterdayClimateLog.getTemperatureMin(), yesterdayClimateLog.getTemperatureMax(), yesterdayClimateLog.getPressure(), yesterdayClimateLog.getWindSpeed(),
      yesterdayClimateLog.getDewPoint(), extraterrestrialSolarRadiation, maximumInsolation, yesterdayClimateLog.getCloudCover());

      /*
       * Evapotranspiracion del cultivo bajo condiciones esntandar (ETc)
       * del cultivo dado con la ETo del dia de ayer
       */
      yesterdayEtc = cropService.getKc(givenPlantingRecord.getCrop(), givenPlantingRecord.getSeedDate()) * yesterdayEto;

      yesterdayClimateLog.setEto(yesterdayEto);
      yesterdayClimateLog.setEtc(yesterdayEtc);
      yesterdayClimateLog.setParcel(parcel);
      climateRecordServiceBean.create(yesterdayClimateLog);
    }

    /*
     * Recupera el registro climatico de la parcela
     * de la fecha anterior a la fecha actual
     */
    ClimateRecord climateLog = climateRecordServiceBean.find(yesterdayDate, parcel);
    suggestedIrrigationToday = WaterMath.getSuggestedIrrigation(parcel.getHectare(), climateLog.getEtc(), climateLog.getEto(), climateLog.getRainWater(), climateLog.getWaterAccumulated(), totalIrrigationWaterToday);

    IrrigationLog newIrrigationLog = new IrrigationLog();
    newIrrigationLog.setDate(currentDate);
    newIrrigationLog.setSuggestedIrrigation(suggestedIrrigationToday);
    newIrrigationLog.setTomorrowPrecipitation(WaterMath.truncateToThreeDecimals(tomorrowPrecipitation));
    newIrrigationLog.setParcel(parcel);

    return mapper.writeValueAsString(newIrrigationLog);
  }

  /**
   * @param  harvestDate [fecha de cosecha]
   * @return el estado "En desarrollo" si la fecha de cosecha esta
   * despues de la fecha actual del sistema y el estado "Finalizado"
   * si la fecha de cosecha esta antes de la fecha actual del sistema
   * o si es igual a la misma
   */
  private InstanceParcelStatus getStatus(Calendar harvestDate) {
    Calendar yesterdayCurrentDate = Calendar.getInstance();
    yesterdayCurrentDate.set(Calendar.DAY_OF_YEAR, yesterdayCurrentDate.get(Calendar.DAY_OF_YEAR) - 1);

    /*
     * Si la fecha de cosecha del registro historico de parcela
     * esta despues de la fecha actual del sistema - un dia retorna el
     * estado "En desarrollo"
     */
    if ((harvestDate.compareTo(yesterdayCurrentDate)) > 0) {
      return statusService.find(2);
    }

    /*
     * En cambio si la fecha de cosecha del registro historico
     * de parcela esta antes de la fecha actual del sistema
     * o es igual a la misma, retorna el estado "Finalizado"
     */
    return statusService.find(1);
  }

  /**
   * @param  givenCrop
   * @param  seedDate    [fecha de siembra]
   * @param  harvestDate [fecha de cosecha]
   * @return verdadero si la diferencia en dias entre la fecha de
   * siembra y la fecha de cosecha es mayor que la cantidad de dias
   * que dura la etapa de vida del cultivo dado, en caso contrario
   * retorna falso
   */
  private boolean excessStageLife(Crop givenCrop, Calendar seedDate, Calendar harvestDate) {
    int differenceBetweenDates = 0;
    int totalDaysLife = givenCrop.getInitialStage() + givenCrop.getDevelopmentStage() + givenCrop.getMiddleStage() + givenCrop.getFinalStage();

    /*
     * Si los años de la fecha de siembra y de la fecha de cosecha
     * son iguales, entonces la diferencia en dias entre ambas fechas
     * se calcula de forma directa
     */
    if ((seedDate.get(Calendar.YEAR)) == (harvestDate.get(Calendar.YEAR))) {
      differenceBetweenDates = harvestDate.get(Calendar.DAY_OF_YEAR) - seedDate.get(Calendar.DAY_OF_YEAR);
    }

    /*
     * Si la diferencia entre los años de la fecha de siembra y la fecha
     * de cosecha ingresadas es igual a uno, la diferencia en dias entre
     * ambas fechas se calcula de la siguiente forma:
     *
     * Diferencia en dias entre ambas fechas = (365 - numero del dia en el
     * año de la fecha de siembra + 1) - numero del dia en el año de la
     * fecha de cosecha
     */
    if ((harvestDate.get(Calendar.YEAR) - seedDate.get(Calendar.YEAR)) == 1) {
      differenceBetweenDates = (365 - seedDate.get(Calendar.DAY_OF_YEAR) + 1) + harvestDate.get(Calendar.DAY_OF_YEAR);
    }

    /*
     * Si la diferencia entre los años de la fecha de siembra y la fecha
     * de cosecha ingresadas es mayor a uno, la diferencia en dias entre
     * ambas fechas se calcula de la siguiente forma:
     *
     * Diferencia en dias entre ambas fechas = (año de la fecha de cosecha -
     * año de la fecha de siembra) * 365 - (365 - numero del dia en el año
     * de la fecha de siembra + 1) - numero del dia en el año de la fecha
     * de cosecha
     */
    if ((harvestDate.get(Calendar.YEAR) - seedDate.get(Calendar.YEAR)) > 1) {
      differenceBetweenDates = ((harvestDate.get(Calendar.YEAR) - seedDate.get(Calendar.YEAR)) * 365) - (365 - seedDate.get(Calendar.DAY_OF_YEAR) + 1) - harvestDate.get(Calendar.DAY_OF_YEAR);
    }

    /*
     * Si la diferencia en dias entre la fecha de siembra
     * y la fecha de cosecha ingresadas es mayor a la cantidad
     * total de dias de vida que vive el cultivo dado, retorna
     * verdadero
     */
    if (differenceBetweenDates > totalDaysLife) {
      return true;
    }

    return false;
  }

}
