package plantingRecord;

import java.io.IOException;
import java.util.Calendar;
import java.util.Collection;
import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Stateless;
import stateless.PlantingRecordServiceBean;
import stateless.PlantingRecordStatusServiceBean;
import stateless.CropServiceBean;
import stateless.IrrigationRecordServiceBean;
import stateless.ClimateRecordServiceBean;
import stateless.SolarRadiationServiceBean;
import stateless.UserServiceBean;
import stateless.ParcelServiceBean;
import stateless.MonthServiceBean;
import stateless.OptionServiceBean;
import stateless.LatitudeServiceBean;
import stateless.SoilWaterBalanceServiceBean;
import model.Parcel;
import model.PlantingRecord;
import model.ClimateRecord;
import model.IrrigationRecord;
import model.Option;
import model.Crop;
import model.SoilWaterBalance;
import model.PlantingRecordStatus;
import irrigation.WaterNeedWos;
import irrigation.WaterNeedWs;
import irrigation.WaterMath;
import util.UtilDate;
import et.HargreavesEto;
import et.Etc;
import climate.ClimateClient;

@Stateless
public class PlantingRecordManager {

    // inject a reference to the PlantingRecordServiceBean
    @EJB PlantingRecordServiceBean plantingRecordService;
    @EJB PlantingRecordStatusServiceBean statusService;
    @EJB CropServiceBean cropService;
    @EJB IrrigationRecordServiceBean irrigationRecordService;
    @EJB ClimateRecordServiceBean climateRecordService;
    @EJB SolarRadiationServiceBean solarService;
    @EJB MonthServiceBean monthService;
    @EJB LatitudeServiceBean latitudeService;
    @EJB OptionServiceBean optionService;
    @EJB SoilWaterBalanceServiceBean soilWaterBalanceService;
    @EJB ParcelServiceBean parcelService;
    @EJB UserServiceBean userService;

    /*
     * Establece de manera automatica el estado finalizado de un registro de
     * plantacion presuntamente en desarrollo. Esto lo hace cada 24 horas a
     * partir de las 00 horas.
     * 
     * La segunda anotacion @Schedule es para probar que este metodo se
     * ejecuta correctamente, es decir, que establece el estado finalizado
     * en un registro de plantacion presuntamente en desarrollo.
     * 
     * El archivo t110Inserts.sql de la ruta app/etc/sql tiene datos para
     * probar que este metodo se ejecuta correctamente, es decir, que hace
     * lo que se espera que haga.
     */
    // @Schedule(second = "*", minute = "*", hour = "0/23", persistent = false)
    // @Schedule(second = "*/10", minute = "*", hour = "*", persistent = false)
    public void modifyToFinishedStatus() {
        /*
         * El valor de esta constante se asigna a la necesidad de
         * agua de riego [mm/dia] de un registro de plantacion
         * para el que no se puede calcular dicha necesidad, lo
         * cual, ocurre cuando no se tiene la evapotranspiracion
         * del cultivo bajo condiciones estandar (ETc) [mm/dia]
         * ni la precipitacion [mm/dia], siendo ambos valores de
         * la fecha actual.
         * 
         * El valor de esta constante tambien se asigna a la
         * necesidad de agua de riego de un registro de plantacion
         * finalizado o en espera, ya que NO tiene ninguna utilidad
         * que un registro de plantacion en uno de estos estados
         * tenga un valor numerico mayor o igual a cero en la
         * necesidad de agua de riego.
         * 
         * La abreviatura "n/a" significa "no disponible".
         */
        String notAvailable = plantingRecordService.getNotAvailable();
        Collection<PlantingRecord> developingPlantingRecords = plantingRecordService.findAllInDevelopment();

        for (PlantingRecord currentPlantingRecord : developingPlantingRecords) {
            /*
             * Si un registro de plantacion presuntamente en desarrollo,
             * NO esta en desarrollo, se establece el estado finalizado
             * en el mismo.
             * 
             * Un registro de plantacion en desarrollo, esta en desarrollo
             * si su fecha de siembra es menor o igual a la fecha actual y
             * su fecha de cosecha es mayor o igual a la fecha actual.
             * En cambio, si su fecha de cosecha es estrictamente menor
             * (es decir, anterior) a la fecha actual, se debe establecer
             * el estado finalizado en el mismo.
             * 
             * Se asigna el valor "n/a" a la necesidad de agua de riego de
             * un registro de plantacion finalizado porque no tiene ninguna
             * utilidad que un registro de plantacion en este estado tenga
             * un valor numerico mayor o igual a cero en la necesidad de
             * agua de riego.
             * 
             * Se asigna el valor 0 a la lamina total de agua disponible
             * (dt) [mm] y a la lamina de riego optima (drop) [mm] de un
             * registro de plantacion que tiene el estado finalizado, ya
             * que en este estado NO tiene ningna utilidad tener tales
             * datos.
             */
            if (plantingRecordService.isFinished(currentPlantingRecord)) {
                plantingRecordService.updateCropIrrigationWaterNeed(currentPlantingRecord.getId(), notAvailable);
                plantingRecordService.updateTotalAmountWaterAvailable(currentPlantingRecord.getId(), 0);
                plantingRecordService.updateOptimalIrrigationLayer(currentPlantingRecord.getId(), 0);
                plantingRecordService.setStatus(currentPlantingRecord.getId(), statusService.findFinishedStatus());
            }

        }

    }

    /*
     * Establece de manera automatica un estado de desarrollo en el registro
     * de plantacion presuntamente en espera mas antiguo de los registros de
     * plantacion en espera de cada una de las parcelas registradas en la base
     * de datos subyacente. Esto lo hace cada 24 horas a parit de las 01 horas,
     * una hora despues de la ejecucion del metodo modifyToFinishedStatus de
     * esta clase.
     * 
     * La segunda anotacion @Schedule es para probar que este metodo se
     * ejecuta correctamente, es decir, que establece el estado en desarrollo
     * en un registro de plantacion presuntamente en espera.
     */
    // @Schedule(second = "*", minute = "*", hour = "1/23", persistent = false)
    // @Schedule(second = "*/10", minute = "*", hour = "*", persistent = false)
    public void modifyToDevelopmentStatus() {
        /*
         * El simbolo de esta variable se utiliza para representar que la
         * necesidad de agua de riego de un cultivo en la fecha actual [mm/dia]
         * no esta disponible, pero se puede calcular. Esta situacion
         * ocurre unicamente para un registro de plantacion en desarrollo.
         */
        String cropIrrigationWaterNeedNotAvailableButCalculable = plantingRecordService.getCropIrrigationWaterNotAvailableButCalculable();

        /*
         * Obtiene una coleccion que contiene el registro de
         * plantacion en espera mas antiguo de los registros
         * de plantacion en espera de cada una de las parcelas
         * registradas en la base de datos subyacente
         */
        Collection<PlantingRecord> pendingPlantingRecords = plantingRecordService.findAllInWaiting();

        for (PlantingRecord currentPlantingRecord : pendingPlantingRecords) {
            /*
             * Si un registro de plantacion presuntamente en espera, NO
             * esta en espera, se le asigna un estado de desarrollo
             * (en desarrollo o desarrollo optimo) dependiendo del valor
             * de la bandera suelo de la parcela a la que pertenece.
             * 
             * Un registro de plantacion en espera, esta en espera si
             * su fecha de siembra es estrictamente mayor (es decir,
             * posterior) a la fecha actual. En cambio, si su fecha de
             * siembra es menor o igual a la fecha actual y su fecha
             * de cosecha es mayor o igual a la fecha actual, se debe
             * establecer un estado en desarrollo en el mismo.
             * 
             * Se asigna el simbolo "-" (guion) a la necesidad de agua
             * de riego de un registro de plantacion en desarrollo. Dicho
             * simbolo se utiliza para representar que la necesidad de
             * agua de riego de un cultivo en la fecha actual [mm/dia]
             * no esta disponible, pero es calculable. Esta situacion
             * ocurre unicamente para un registro de plantacion en
             * desarrollo.
             */
            if (plantingRecordService.isInDevelopment(currentPlantingRecord)) {
                plantingRecordService.updateCropIrrigationWaterNeed(currentPlantingRecord.getId(), cropIrrigationWaterNeedNotAvailableButCalculable);

                /*
                 * Si un registro de plantacion presuntamente en espera tiene
                 * una parcela que tiene la bandera suelo NO activa, se asigna
                 * el estado "En desarrollo" al registro de plantacion. En caso
                 * contrario, se le asigna el estado "Desarrollo optimo".
                 */
                if (!currentPlantingRecord.getParcel().getOption().getSoilFlag()) {
                    plantingRecordService.setStatus(currentPlantingRecord.getId(), statusService.findInDevelopmentStatus());
                } else {
                    plantingRecordService.setStatus(currentPlantingRecord.getId(), statusService.findOptimalDevelopmentStatus());
                }

            }

        } // End for

    }

    /**
     * Establece de manera automatica la necesidad de agua de riego [mm/dia]
     * (atributo cropIrrigationWaterNeed) de un registro de plantacion en desarrollo
     * cada dos horas a partir de la hora 01.
     * 
     * La segunda anotacion @Schedule es para probar que este metodo se ejecuta
     * correctamente, es decir, que asigna un valor al atributo cropIrrigationWaterNeed
     * de un registro de plantacion en desarrollo.
     * 
     * El archivo t125Inserts.sql de la ruta app/etc/sql tiene datos para probar que
     * este metodo se ejecuta correctamente, es decir, que hace lo que se espera que
     * haga.
     */
    // @Schedule(second = "*", minute = "*", hour = "1/2", persistent = false)
    // @Schedule(second = "*/5", minute = "*", hour = "*", persistent = false)
    private void setCropIrrigationWaterNeed() {
        /*
         * El valor de esta variable se utiliza para representar
         * la situacion en la que NO se calcula el acumulado del
         * deficit de agua por dia de dias previos a una fecha de
         * un balance hidrico de suelo de una parcela que tiene
         * un cultivo sembrado y en desarrollo. Esta situacion
         * ocurre cuando el nivel de humedad de un suelo, que tiene
         * un cultivo sembrado, es estrictamente menor al doble de
         * la capacidad de almacenamiento de agua del mismo.
         */
        String notCalculated = soilWaterBalanceService.getNotCalculated();

        /*
         * El valor de esta constante se asigna a la necesidad de
         * agua de riego [mm/dia] de un registro de plantacion
         * para el que no se puede calcular dicha necesidad, lo
         * cual, ocurre cuando no se tiene la evapotranspiracion
         * del cultivo bajo condiciones estandar (ETc) [mm/dia]
         * ni la precipitacion [mm/dia], siendo ambos valores de
         * la fecha actual.
         * 
         * El valor de esta constante tambien se asigna a la
         * necesidad de agua de riego de un registro de plantacion
         * finalizado o en espera, ya que NO tiene ninguna utilidad
         * que un registro de plantacion en uno de estos estados
         * tenga un valor numerico mayor o igual a cero en la
         * necesidad de agua de riego.
         * 
         * La abreviatura "n/a" significa "no disponible".
         */
        String notAvailable = plantingRecordService.getNotAvailable();
        String stringIrrigationWaterNeedCurrentDate = null;

        Collection<PlantingRecord> developingPlantingRecords = plantingRecordService.findAllInDevelopment();

        /*
         * Establece la necesidad de agua de riego [mm/dia] de la fecha
         * actual (es decir, hoy) en cada uno de los registros de plantacion
         * en desarrollo de todas las parcelas.
         * 
         * Esto es que establece la necesidad de agua de riego [mm/dia] de
         * la fecha actual de cada cultivo en desarrollo de cada una de
         * las parcelas. La necesidad de agua de riego de un cultivo en la
         * fecha actual se calcula en funcion de la ETc del dia anterior
         * o de los dias anteriores a la fecha actual, del agua de lluvia
         * del dia anterior o de los dias anteriores a la fecha actual, del
         * agua de riego del dia anterior o de los dias anteriores a la fecha
         * actual y de la cantidad total de agua de riego de la fecha actual.
         */
        for (PlantingRecord developingPlantingRecord : developingPlantingRecords) {

            try {
                /*
                 * Ejecuta el proceso del calculo de la necesidad de agua
                 * de riego de un cultivo en la fecha actual [mm/dia]. Esto
                 * es que ejecuta los metodos necesarios para calcular y
                 * actualizar la necesidad de agua de riego de un cultivo
                 * (en desarrollo) en la fecha actual.
                 */
                stringIrrigationWaterNeedCurrentDate = runCalculationIrrigationWaterNeedCurrentDateTwo(developingPlantingRecord);
            } catch (Exception e) {
                e.printStackTrace();

                /*
                 * La aplicacion del lado servidor requiere de un servicio
                 * meteorologico para obtener los datos climaticos de una
                 * ubicacion geografica (*) que se necesitan para calcular
                 * la necesidad de agua de riego de un cultivo en la fecha
                 * actual (es decir, hoy). Si el servicio meteorologico
                 * utilizado le devuelve a la aplicacion del lado servidor
                 * un mensaje HTTP de error, NO es posible calcular la
                 * necesidad de agua de riego de un cultivo en la fecha
                 * actual. Por lo tanto, se establece el valor "n/a" (no
                 * disponible) en el atributo de la necesidad de agua de
                 * riego de un cultivo del registro de plantacion en
                 * desarrollo para el que se solicito calcular la necesidad
                 * de agua de riego de un cultivo en la fecha actual.
                 * 
                 * (*) Un cultivo se siembra en una parcela y una parcela
                 * tiene una ubicacion geografica. Para calcular la
                 * necesidad de agua de riego de un cultivo en una fecha
                 * cualquiera se requieren los datos climaticos de la
                 * fecha y de la ubicacion geografica de la parcela en
                 * la que esta sembrado un cultivo.
                 */
                plantingRecordService.updateCropIrrigationWaterNeed(developingPlantingRecord.getId(), notAvailable);
                break;
            }

            /*
             * Si la necesidad de agua de riego de un cultivo (en
             * desarrollo) en la fecha actual [mm/dia] es "NC"
             * (valor de la variable notCalculated) significa que
             * el algoritmo utilizado para calcular dicha necesidad
             * es el que utiliza el suelo para ello, ya que al
             * utilizar este algoritmo se retorna el valor "NC"
             * para representar la situacion en la que NO se
             * calcula el acumulado del deficit de agua por dia
             * del balance hidrico de una parcela que tiene un
             * cultivo sembrado y en desarrollo. Esta situacion
             * ocurre cuando el nivel de humedad de un suelo, que
             * tiene un cultivo sembrado, es estrictamente menor
             * al doble de la capacidad de almacenamiento de agua
             * de un suelo que tiene un cultivo sembrado.
             * 
             * Por lo tanto, la aplicacion del lado servidor asigna
             * la abreviatura "n/a" (no disponible) a la necesidad de
             * agua de riego de un cultivo en la fecha actual [mm/dia]
             * de un registro de plantacion en desarrollo y no se
             * realiza la operacion solicitada.
             * 
             * En esta situacion, la aplicacion retorna el mensaje
             * HTTP 400 (Bad request) informando de que el cultivo
             * para el que se deseo calcular su necesidad de agua
             * de riego en la fecha actual [mm/dia], esta muerto.
             */
            if (stringIrrigationWaterNeedCurrentDate != null && stringIrrigationWaterNeedCurrentDate.equals(notCalculated)) {
                plantingRecordService.updateCropIrrigationWaterNeed(developingPlantingRecord.getId(), notAvailable);
                plantingRecordService.updateDateDeath(developingPlantingRecord.getId(), UtilDate.getCurrentDate());
            }

            /*
             * Si el valor de la necesidad de agua de riego de un
             * cultivo en la fecha actual [mm/dia] NO es igual al
             * valor "NC", significa que es un valor numerico. Por
             * lo tanto, se lo convierte a double, ya que dicha
             * necesidad esta expresada como double.
             */
            double cropIrrigationWaterNeedCurrentDate = Math.abs(Double.parseDouble(stringIrrigationWaterNeedCurrentDate));

            /*
             * *****************************************************
             * Actualizacion del atributo "necesidad agua riego de
             * cultivo" del registro de plantacion en desarrollo, el
             * cual tiene el cultivo para el que se solicita calcular
             * su necesidad de agua de riego en la fecha actual [mm/dia],
             * con el valor de de dicha necesidad de agua de riego
             * *****************************************************
             */
            plantingRecordService.updateCropIrrigationWaterNeed(developingPlantingRecord.getId(), String.valueOf(cropIrrigationWaterNeedCurrentDate));
        } // End for

    }

    /**
     * @param developingPlantingRecord
     * @return referencia a un objeto de tipo String que contiene el
     * valor utilizado para determinar la necesidad de agua de riego
     * de un cultivo en la fecha actual [mm/dia]
     * @throws IOException
     */
    private String runCalculationIrrigationWaterNeedCurrentDateTwo(PlantingRecord developingPlantingRecord) throws IOException {
        /*
         * Persiste los registros climaticos de la parcela de un registro
         * de plantacion en desarrollo desde la fecha de siembra hasta la
         * fecha inmediatamente anterior a la fecha actual, si NO existen
         * en la base de datos subyacente. Estos registros climaticos son
         * obtenidos del servicio meteorologico utilizado por la aplicacion.
         */
        requestPastClimateRecordsTwo(developingPlantingRecord);

        /*
         * Calcula la ETo y la ETc de los registros climaticos de la parcela
         * de un registro de plantacion en desarrollo previamente obtenidos.
         * La ETc es necesaria para calcular los balances hidricos de suelo
         * de una parcela que tiene un cultivo en desarrollo.
         */
        calculateEtsPastClimateRecordsTwo(developingPlantingRecord);

        /*
         * Persiste el balance hidrico de la fecha de siembra de un cultivo,
         * si no existe en la base de datos subyacente. En caso contrario,
         * lo modifica. Este paso es el primer paso necesario para el
         * calculo de los balances hidricos de suelo de una parcela que
         * tiene un cultivo sembrado. Este calculo se realiza para
         * calcular la necesidad de agua de riego de un cultivo en la
         * fecha actual [mm/dia].
         * 
         * El balance hidrico de la fecha de siembra de un cultivo tiene
         * el valor 0 en todos sus atributos porque en la fecha de siembra
         * de un cultivo se parte del suelo a capacidad de campo, esto es
         * que el suelo esta lleno de agua, pero no anegado.
         */
        persistSoilWaterBalanceSeedDate(developingPlantingRecord);

        String notCalculated = soilWaterBalanceService.getNotCalculated();
        Parcel parcel = developingPlantingRecord.getParcel();
        Calendar seedDate = developingPlantingRecord.getSeedDate();

        /*
         * La necesidad de agua de riego de un cultivo en la fecha actual
         * se determina con el acumulado del deficit de agua por dia [mm/dia]
         * de la fecha inmediatamente anterior a la fecha actual. Por este
         * motivo se recupera de la base de datos subyacente el balance
         * hidrico de suelo de la parcela, que tiene tiene un cultivo
         * sembrado y en desarrollo, de la fecha inmediatamente anterior
         * a la fecha actual.
         */
        Calendar yesterday = UtilDate.getYesterdayDate();
        Calendar currentDate = UtilDate.getCurrentDate();

        /*
         * Si la fecha de siembra de un cultivo es igual a la fecha actual
         * (es decir, hoy), la necesidad de agua de riego de un cultivo
         * en la fecha actual [mm/dia] es el acumulado del deficit de agua
         * por dia [mm/dia] de la fecha actual
         */
        if (UtilDate.compareTo(seedDate, currentDate) == 0) {
            return soilWaterBalanceService.find(parcel.getId(), currentDate).getAccumulatedWaterDeficitPerDay();
        }

        /*
         * Si la fecha de siembra de un cultivo es igual a la fecha
         * inmediatamente anterior a la fecha actual (es decir, hoy),
         * la necesidad de agua de riego de un cultivo en la fecha
         * actual [mm/dia] es el acumulado del deficit de agua por dia
         * [mm/dia] de la fecha inmediatamente anterior a la fecha
         * actual
         */
        if (UtilDate.compareTo(seedDate, yesterday) == 0) {
            return soilWaterBalanceService.find(parcel.getId(), yesterday).getAccumulatedWaterDeficitPerDay();
        }

        /*
         * Calcula y persiste los balances hidricos de suelo de una parcela,
         * que tiene un cultivo en desarrollo, para calcular la necesidad
         * de agua de riego del mismo en la fecha actual [mm/dia]. Esto se
         * realiza si y solo si la cantidad de dias entre la fecha de siembra
         * de un cultivo y la fecha actual (es decir, hoy) es mayor o igual
         * a dos.
         */
        calculateSoilWaterBalances(developingPlantingRecord);

        /*
         * La necesidad de agua de riego de un cultivo en la fecha actual
         * (es decir, hoy) [mm/dia] se determina en funcion del acumulado
         * del deficit de agua por dia [mm/dia] del dia inmediatamente
         * anterior a la fecha actual
         */
        String stringAccumulatedWaterDeficitPerDay = soilWaterBalanceService.find(parcel.getId(), yesterday).getAccumulatedWaterDeficitPerDay();

        /*
         * Si el valor del acumulado del deficit de agua por dia [mm/dia]
         * de ayer es "NC" (no calculado), significa dos cosas:
         * - que el algoritmo utilizado para calcular la necesidad de
         * agua de riego de un cultivo en la fecha actual [mm/dia] es
         * el que utiliza el suelo para ello,
         * - y que el nivel de humedad del suelo, que tiene un cultivo
         * sembrado, es estrictamente menor al doble de la capacidad de
         * almacenamiento de agua del suelo.
         * 
         * Por lo tanto, se retorna "NC" para indicar que la necesidad de
         * agua de riego de un cultivo en la fecha actual [mm/dia] no se
         * calculo, ya que el cultivo esta muerto debido a que el nivel
         * de humedad del suelo, en el que esta sembrado, es estrictamente
         * menor al doble de la capacidad de almacenamiento de agua
         * del suelo.
         */
        if (stringAccumulatedWaterDeficitPerDay.equals(notCalculated)) {
            return notCalculated;
        }

        double totalIrrigationWaterCurrentDate = irrigationRecordService.calculateTotalIrrigationWaterCurrentDate(parcel.getId());
        double accumulatedWaterDeficitPerDay = Double.parseDouble(stringAccumulatedWaterDeficitPerDay);

        /*
         * Calculo de la necesidad de agua de riego de un cultivo
         * en la fecha actual [mm/dia]. El motivo por el cual este
         * calculo corresponde a la fecha actual es que la cantidad
         * total de agua de riego de un cultivo [mm/dia] es de la
         * fecha actual y el acumulado del deficit de agua por dia
         * [mm] es del dia inmediatamente anterior a la fecha actual
         * (es decir, hoy). En cambio, si en este metodo se utiliza
         * la cantidad total de agua de riego de ayer y el acumulado
         * del deficit de agua por dia de antes de ayer, la necesidad
         * de agua de riego de un cultivo calculada es de ayer. Por
         * lo tanto, lo que determina la fecha de la necesidad de agua
         * de riego de un cultivo es la fecha de la cantidad total
         * de agua de riego de un cultivo y la fecha del acumulado
         * del deficit de agua por dia.
         */
        return String.valueOf(WaterMath.calculateIrrigationWaterNeed(totalIrrigationWaterCurrentDate, accumulatedWaterDeficitPerDay));
    }

    /**
     * Persiste los registros climaticos de una parcela, que
     * tiene un cultivo en desarrollo, desde la fecha
     * inmediatamente siguiente a la fecha de siembra hasta
     * la fecha inmediatamente anterior a la fecha actual
     * (es decir, hoy)
     * 
     * @param developingPlantingRecord
     */
    private void requestPastClimateRecordsTwo(PlantingRecord developingPlantingRecord) throws IOException {
        Calendar seedDate = developingPlantingRecord.getSeedDate();

        /*
         * Fecha inmediatamente anterior a la fecha actual
         * (es decir, hoy)
         */
        Calendar yesterday = UtilDate.getYesterdayDate();

        /*
         * Si la fecha de siembra de un cultivo es igual a la fecha
         * actual (es decir, hoy), NO se solicita ni se persiste el
         * registro climatico de la fecha actual, ya que en la fecha
         * de siembra se parte del suelo en capacidad de campo, esto
         * es que el suelo esta lleno de agua, pero no anegado. En
         * esta situacion, el acumulado del deficit de agua por dia
         * [mm/dia] de la fecha actual es 0. Por lo tanto, la necesidad
         * de agua de riego de un cultivo en la fecha actual [mm/dia]
         * es 0.
         */
        if (UtilDate.compareTo(seedDate, UtilDate.getCurrentDate()) == 0) {
            return;
        }

        /*
         * Si la fecha de siembra de un cultivo es igual a la fecha
         * inmediatamente anterior a la fecha actual (es decir, hoy),
         * NO se solicita ni persiste el registro climatico de la
         * fecha inmediatamente anterior a la fecha actual, ya que
         * en la fecha de siembra se parte del suelo en capacidad
         * de campo, esto es que el suelo esta lleno de agua, pero
         * no anegado. En esta situacion, el acumulado del deficit
         * de agua por dia [mm/dia] del dia inmediatamente anterior
         * es 0. Por lo tanto, la necesidad de agua de riego de
         * un cultivo en la fecha actual [mm/dia] es 0.
         */
        if (UtilDate.compareTo(seedDate, yesterday) == 0) {
            return;
        }

        Parcel parcel = developingPlantingRecord.getParcel();
        ClimateRecord newClimateRecord = null;

        /*
         * A partir de la fecha inmediatamente siguiente a la fecha
         * de siembra de un cultivo se solicitan y persisten los
         * registros climaticos de una parcela que tiene un cultivo
         * en desarrollo en la fecha actual (es decir, hoy)
         */
        Calendar dateFollowingSeedDate = UtilDate.getNextDateFromDate(seedDate);
        Calendar pastDate = Calendar.getInstance();
        pastDate.set(Calendar.YEAR, dateFollowingSeedDate.get(Calendar.YEAR));
        pastDate.set(Calendar.MONTH, dateFollowingSeedDate.get(Calendar.MONTH));
        pastDate.set(Calendar.DAY_OF_YEAR, dateFollowingSeedDate.get(Calendar.DAY_OF_YEAR));

        /*
         * Los registros climaticos a obtener pertenecen al periodo
         * definido por la fecha inmediatamente siguiente a la fecha
         * de siembra y la fecha inmediatamente anterior a la fecha
         * actual (es decir, hoy).
         * 
         * Se debe sumar un uno al resultado de esta diferencia para
         * que este metodo persista el registro climatico de la fecha
         * inmediatamente anterior a la fecha actual.
         */
        int days = UtilDate.calculateDifferenceBetweenDates(dateFollowingSeedDate, yesterday) + 1;

        /*
         * Crea y persiste los registros climaticos desde la fecha
         * inmediatamente siguiente a la fecha de siembra hasta
         * la fecha inmediatamente anterior a la fecha actual (es
         * decir, hoy), pertenecientes a una parcela que tiene un
         * cultivo en desarrollo en la fecha actual
         */
        for (int i = 0; i < days; i++) {

            /*
             * Si una parcela dada NO tiene un registro climatico
             * perteneciente a una fecha, se lo solicita al servicio
             * meteorologico utilizado y se lo persiste
             */
            if (!climateRecordService.checkExistence(pastDate, parcel)) {
                newClimateRecord = ClimateClient.getForecast(parcel, pastDate.getTimeInMillis() / 1000);
                climateRecordService.create(newClimateRecord);
            }

            /*
             * Suma un uno al numero de dia en el año de una fecha
             * pasada dada para obtener el siguiente registro climatico
             * correspondiente a una fecha pasada
             */
            pastDate.set(Calendar.DAY_OF_YEAR, pastDate.get(Calendar.DAY_OF_YEAR) + 1);
        }

    }

    /**
     * Calcula y actualiza la ETo y la ETc de los registros
     * climaticos, pertenecientes a una parcela que tiene un
     * cultivo en desarrollo en la fecha actual, comprendidos
     * en el periodo definido por la fecha inmediatamente
     * siguiente a la fecha de siembra de un cultivo y la
     * fecha inmediatamente anterior a la fecha actual (es
     * decir, hoy)
     * 
     * @param developingPlantingRecord
     */
    private void calculateEtsPastClimateRecordsTwo(PlantingRecord developingPlantingRecord) {
        Calendar seedDate = developingPlantingRecord.getSeedDate();

        /*
         * Fecha inmediatamente anterior a la fecha actual
         * (es decir, hoy)
         */
        Calendar yesterday = UtilDate.getYesterdayDate();

        /*
         * Si la fecha de siembra de un cultivo es igual a la fecha
         * actual (es decir, hoy), NO se calculan la ETo y la ETc
         * del registro climatico de la fecha actual, ya que NO se
         * lo persiste debido a que en la fecha de siembra se parte
         * del suelo en capacidad de campo, esto es que el suelo
         * esta lleno de agua, pero no anegado. En esta situacion,
         * el acumulado del deficit de agua por dia [mm/dia] de la
         * fecha actual es 0. Por lo tanto, la necesidad de agua de
         * riego de un cultivo en la fecha actual [mm/dia] es 0.
         */
        if (UtilDate.compareTo(seedDate, UtilDate.getCurrentDate()) == 0) {
            return;
        }

        /*
         * Si la fecha de siembra de un cultivo es igual a la fecha
         * inmediatamente anterior a la fecha actual (es decir, hoy),
         * NO se calculan la ETo y la ETc del registro climatico de
         * la fecha inmediatamente anterior a la fecha actual, ya que
         * NO se lo persiste debido a que en la fecha de siembra se
         * parte del suelo en capacidad de campo, esto es que el suelo
         * esta lleno de agua, pero no anegado. En esta situacion,
         * el acumulado del deficit de agua por dia [mm/dia] del dia
         * inmediatamente anterior a la fecha actual es 0. Por lo tanto,
         * la necesidad de agua de riego de un cultivo en la fecha
         * actual [mm/dia] es 0.
         */
        if (UtilDate.compareTo(seedDate, yesterday) == 0) {
            return;
        }

        Parcel parcel = developingPlantingRecord.getParcel();
        ClimateRecord climateRecord = null;

        /*
         * A partir de la fecha inmediatamente siguiente a la fecha
         * de siembra de un cultivo se calculan los balances hidricos
         * de suelo de una parcela que tiene un cultivo en desarrollo
         * en la fecha actual (es decir, hoy)
         */
        Calendar dateFollowingSeedDate = UtilDate.getNextDateFromDate(seedDate);
        Calendar pastDate = Calendar.getInstance();
        pastDate.set(Calendar.YEAR, dateFollowingSeedDate.get(Calendar.YEAR));
        pastDate.set(Calendar.MONTH, dateFollowingSeedDate.get(Calendar.MONTH));
        pastDate.set(Calendar.DAY_OF_YEAR, dateFollowingSeedDate.get(Calendar.DAY_OF_YEAR));

        /*
         * Los registros climaticos para los que se calcula
         * su ETo y su ETc pertenecen al periodo definido por
         * la fecha inmediatamente siguiente a la fecha de
         * siembra de un cultivo y la fecha inmediatamente
         * anterior a la fecha actual (es decir, hoy).
         * 
         * Se debe sumar un uno al resultado de esta diferencia
         * para que este metodo calcule la ETo y la ETc del
         * registro climatico de la fecha inmediatamente
         * anterior a la fecha actual.
         */
        int days = UtilDate.calculateDifferenceBetweenDates(dateFollowingSeedDate, yesterday) + 1;

        double eto = 0.0;
        double etc = 0.0;

        /*
         * Calcula la ETo y la ETc de los registros climaticos,
         * pertenecientes a una parcela que tiene un cultivo
         * en desarrollo en la fecha actual, comprendidos en
         * el periodo definido por la fecha inmediatamente
         * siguiente a la fecha de siembra de un cultivo y la
         * fecha inmediatamente anterior a la fecha actual (es
         * decir, hoy)
         */
        for (int i = 0; i < days; i++) {

            if (climateRecordService.checkExistence(pastDate, parcel)) {
                climateRecord = climateRecordService.find(pastDate, parcel);

                eto = calculateEtoForClimateRecord(climateRecord);
                etc = calculateEtcForClimateRecord(eto, developingPlantingRecord, pastDate);

                climateRecordService.updateEtoAndEtc(pastDate, parcel, eto, etc);

                /*
                 * Luego de calcular la ETc de un registro climatico, se debe
                 * restablecer el valor por defecto de esta variable para evitar
                 * el error logico de asignar la ETc de un registro climatico a
                 * otro registro climatico
                 */
                etc = 0.0;
            }

            /*
             * Suma un uno al numero de dia en el año de una fecha
             * pasada dada para obtener el siguiente registro climatico
             * correspondiente a una fecha pasada
             */
            pastDate.set(Calendar.DAY_OF_YEAR, pastDate.get(Calendar.DAY_OF_YEAR) + 1);
        } // End for

    }

    /**
     * Persiste el balance hidrico de suelo de una parcela,
     * que tiene un cultivo sembrado, correspondiente a la
     * fecha de siembra de un cultivo, si NO existe en la
     * base de datos subyacente. En caso contrario, lo
     * modifica.
     * 
     * El balance hidrico de suelo de la fecha de siembra
     * tiene todos sus valores numericos en 0, ya que en
     * el dia de la fecha de siembra de un cultivo, un
     * suelo deberia estar en capacidad de campo, esto
     * es que esta lleno de agua, pero no anegado.
     * 
     * @param developingPlantingRecord
     */
    private void persistSoilWaterBalanceSeedDate(PlantingRecord developingPlantingRecord) {
        Parcel parcel = developingPlantingRecord.getParcel();
        Calendar seedDate = developingPlantingRecord.getSeedDate();
        SoilWaterBalance soilWaterBalance = null;

        if (!soilWaterBalanceService.checkExistence(parcel.getId(), seedDate)) {
            soilWaterBalance = new SoilWaterBalance();
            soilWaterBalance.setDate(seedDate);
            soilWaterBalance.setParcelName(parcel.getName());
            soilWaterBalance.setCropName(developingPlantingRecord.getCrop().getName());
            soilWaterBalance.setWaterProvided(0);
            soilWaterBalance.setEvaporatedWater(0);
            soilWaterBalance.setWaterDeficitPerDay(0);
            soilWaterBalance.setAccumulatedWaterDeficitPerDay(String.valueOf(0));

            /*
             * Persistencia del balance hidrico
             */
            soilWaterBalance = soilWaterBalanceService.create(soilWaterBalance);

            /*
             * Se debe invocar el metodo merge() de la clase ParcelServiceBean
             * para persistir los elementos que se hayan agregado a
             * la coleccion soilWaterBalances de una parcela. De lo
             * contrario, la base de datos subyacente quedara en un
             * estado inconsistente.
             */
            parcel.getSoilWaterBalances().add(soilWaterBalance);
            parcelService.merge(parcel);
        } else {
            soilWaterBalance = soilWaterBalanceService.find(parcel.getId(), seedDate);
            soilWaterBalance.setParcelName(parcel.getName());
            soilWaterBalance.setCropName(developingPlantingRecord.getCrop().getName());
            soilWaterBalance.setWaterProvided(0);
            soilWaterBalance.setEvaporatedWater(0);
            soilWaterBalance.setWaterDeficitPerDay(0);
            soilWaterBalance.setAccumulatedWaterDeficitPerDay(String.valueOf(0));

            /*
             * Realiza las modificaciones del balance hidrico
             * de suelo de la fecha de siembra de un cultivo
             */
            soilWaterBalanceService.modify(parcel.getId(), seedDate, soilWaterBalance);
        }

    }

    /**
     * Calcula y persiste los balances hidricos de suelo de
     * una parcela, que tiene un cultivo sembrado, desde la
     * fecha inmediatamente siguiente a la fecha de siembra
     * hasta la fecha inmediatamente anterior a la fecha
     * actual (es decir, hoy)
     * 
     * @param developingPlantingRecord
     */
    private void calculateSoilWaterBalances(PlantingRecord developingPlantingRecord) {
        Calendar seedDate = developingPlantingRecord.getSeedDate();

        /*
         * Fecha inmediatamente anterior a la fecha actual
         * (es decir, hoy)
         */
        Calendar yesterday = UtilDate.getYesterdayDate();

        /*
         * Si la fecha de siembra de un cultivo es igual a la fecha
         * actual (es decir, hoy), NO se calcula el balance hidrico
         * de la fecha actual, ya que en la fecha de siembra se parte
         * del suelo en capacidad de campo, esto es que el suelo esta
         * lleno de agua, pero no anegado. En esta situacion, el
         * acumulado del deficit de agua por dia [mm/dia] de la fecha
         * actual es 0. Por lo tanto, la necesidad de agua de riego
         * de un cultivo en la fecha actual [mm/dia] es 0.
         */
        if (UtilDate.compareTo(seedDate, UtilDate.getCurrentDate()) == 0) {
            return;
        }

        /*
         * Si la fecha de siembra de un cultivo es igual a la fecha
         * inmediatamente anterior a la fecha actual (es decir, hoy),
         * NO se calcula el balance hidrico de la fecha inmediatamente
         * anterior a la fecha actual, ya que en la fecha de siembra
         * se parte del suelo en capacidad de campo, esto es que el
         * suelo esta lleno de agua, pero no anegado. En esta situacion,
         * el acumulado del deficit de agua por dia [mm/dia] del dia
         * inmediatamente anterior es 0. Por lo tanto, la necesidad
         * de agua de riego de un cultivo en la fecha actual [mm/dia]
         * es 0.
         */
        if (UtilDate.compareTo(seedDate, yesterday) == 0) {
            return;
        }

        Parcel parcel = developingPlantingRecord.getParcel();
        Crop crop = developingPlantingRecord.getCrop();
        SoilWaterBalance soilWaterBalance = null;
        ClimateRecord climateRecord = null;

        /*
         * Los balances hidricos de suelo de una parcela, que
         * tiene un cultivo sembrado, se calculan desde la
         * fecha inmediatamente siguiente a la fecha de
         * siembra de un cultivo hasta la fecha inmediatamente
         * anterior a la fecha actual (es decir, hoy)
         */
        Calendar pastDate = UtilDate.getNextDateFromDate(seedDate);
        Calendar yesterdayDateFromDate = null;

        /*
         * Los balances hidricos de suelo de una parcela, que
         * tiene un cultivo sembrado, se calculan desde la
         * fecha inmediatamente siguiente a la fecha de siembra
         * hasta la fecha inmediatamente anterior a la fecha
         * actual (es decir, hoy).
         * 
         * Se debe sumar un uno al resultado de esta diferencia
         * para que este metodo calcule el balance hidrico de
         * suelo de la fecha inmediatamente anterior a la
         * fecha actual.
         */
        int days = UtilDate.calculateDifferenceBetweenDates(pastDate, yesterday) + 1;

        double totalIrrigationWaterCurrentDate = 0.0;
        double evaporatedWater = 0.0;
        double waterProvidedPerDay = 0.0;
        double waterDeficitPerDay = 0.0;
        double accumulatedWaterDeficitPerDay = 0.0;
        double accumulatedWaterDeficitPerPreviousDay = 0.0;
        double totalAmountWaterAvailable = 0.0;
        double optimalIrrigationLayer = WaterMath.calculateOptimalIrrigationLayer(crop, parcel.getSoil());

        String stringAccumulatedWaterDeficitPerPreviousDay = null;
        String stringAccumulatedWaterDeficitPerDay = null;
        String notCalculated = soilWaterBalanceService.getNotCalculated();

        Collection<IrrigationRecord> irrigationRecords = null;

        PlantingRecordStatus optimalDevelopmentStatus = statusService.findOptimalDevelopmentStatus();
        PlantingRecordStatus developmentAtRiskWiltingStatus = statusService.findDevelopmentAtRiskWiltingStatus();
        PlantingRecordStatus developmentInWiltingStatus = statusService.findDevelopmentInWiltingStatus();
        PlantingRecordStatus deadStatus = statusService.findDeadStatus();

        /*
         * Calcula los balances hidricos de suelo de una parcela,
         * que tiene un cultivo en desarrollo en la fecha actual
         * (es decir, hoy), desde la fecha inmediatamente siguiente
         * a la fecha de siembra hasta la fecha inmediatamente
         * anterior a la fecha actual
         */
        for (int i = 0; i < days; i++) {

            /*
             * Obtencion del registro climatico y de los registros de
             * riego de una fecha para el calculo del agua provista
             * en un dia (fecha) [mm/dia] y del deficit de agua en un
             * dia (fecha) [mm/dia]
             */
            climateRecord = climateRecordService.find(pastDate, parcel);
            irrigationRecords = irrigationRecordService.findAllByParcelIdAndDate(parcel.getId(), pastDate);

            waterProvidedPerDay = climateRecord.getPrecip() + WaterMath.sumTotalAmountIrrigationWaterGivenDate(climateRecord.getDate(), irrigationRecords);
            waterDeficitPerDay = WaterMath.calculateWaterDeficitPerDay(climateRecord, irrigationRecords);
            evaporatedWater = soilWaterBalanceService.getEvaporatedWater(climateRecord);

            /*
             * Obtiene el acumulado del deficit de agua por dia del
             * balance hidrico de suelo de la fecha inmediatamente
             * a una fecha pasada
             */
            yesterdayDateFromDate = UtilDate.getYesterdayDateFromDate(pastDate);
            stringAccumulatedWaterDeficitPerPreviousDay = soilWaterBalanceService.find(parcel.getId(), yesterdayDateFromDate).getAccumulatedWaterDeficitPerDay();

            /*
             * Si el acumulado del deficit de agua por dia de la fecha
             * inmediatamente anterior a una fecha pasada NO es NC (no
             * calculado), significa que el cultivo correspondiente a
             * este calculo de balances hidricos de suelo NO murio en
             * la fecha pasada, por lo tanto, se calcula el acumulado
             * del deficit de agua por dia [mm/dia] de la fecha pasada,
             * lo cual se realiza para calcular el balance hidrico de
             * suelo, en el que esta sembrado un cultivo, de la fecha
             * pasada. En caso contrario, significa que el cultivo
             * murio en la fecha pasada, por lo tanto, NO se calcula
             * el acumulado del deficit de agua por dia [mm/dia] de
             * la fecha pasada, lo cual se representa mediante la
             * asignacion de la sigla "NC" a la variable de tipo String
             * del acumulado del deficit de agua por dia [mm/dia]
             * de un balance hidrico.
             */
            if (!stringAccumulatedWaterDeficitPerPreviousDay.equals(notCalculated)) {
                accumulatedWaterDeficitPerPreviousDay = Double.parseDouble(stringAccumulatedWaterDeficitPerPreviousDay);

                /*
                 * El acumulado del deficit de agua por dia [mm/dia] de
                 * una fecha depende del acumulado del deficit de agua
                 * por dia de la fecha inmdiatamente anterior
                 */
                accumulatedWaterDeficitPerDay = WaterMath.accumulateWaterDeficitPerDay(waterDeficitPerDay, accumulatedWaterDeficitPerPreviousDay);
                stringAccumulatedWaterDeficitPerDay = String.valueOf(accumulatedWaterDeficitPerDay);

                /*
                 * Si la bandera suelo de una parcela esta activa, se
                 * comprueba el nivel de humedad del suelo para establecer
                 * el estado del registro de plantacion en desarrollo para
                 * el que se calcula la necesidad de agua de riego de su
                 * cultivo en la fecha actual [mm/dia]
                 */
                if (parcel.getOption().getSoilFlag()) {
                    totalAmountWaterAvailable = WaterMath.calculateTotalAmountWaterAvailable(crop, parcel.getSoil());

                    /*
                     * Si el acumulado del deficit de agua por dia [mm/dia] de
                     * dias previos a la fecha actual es estrictamente menor al
                     * doble del negativo de la capacidad de almacenamiento de
                     * agua del suelo, significa que el nivel de humedad del
                     * suelo, que tiene un cultivo sembrado, es estrictamente
                     * menor al doble de la capacidad de almacenamiento de agua
                     * del suelo. En esta situacion el cultivo esta muerto y el
                     * registro de plantacion en desarrollo adquiere el estado
                     * "Muerto".
                     * 
                     * Si un cultivo esta muerto NO sirve verificar en que punto
                     * se encuentra el nivel de humedad del suelo con respecto a
                     * la capacidad de campo, el umbral de riego, la capacidad de
                     * almacenamiento de agua del suelo y el doble de la capacidad
                     * de almacenamiento de agua del suelo.
                     * 
                     * Las raices de un cultivo pueden crecer más allá de la capacidad
                     * de almacenamiento de agua del suelo, con lo cual un cultivo
                     * puede absorber el agua que hay en el punto de marchitez permanente
                     * del suelo (*) y la que hay debajo de este punto. Las raices
                     * de un cultivo no crecen más allá del doble de la capacidad
                     * de almacenamiento de agua del suelo, con lo cual un cultivo
                     * no puede absorber el agua que hay en el doble de la capacidad
                     * de almacenamiento de agua del suelo ni la que hay debajo de
                     * este punto. Por lo tanto, si el nivel de humedad del suelo,
                     * en el que está sembrado un cultivo, es estrictamente menor
                     * al doble de la capacidad de almacenamiento de agua del mismo,
                     * el cultivo no puede absorber el agua que hay en este punto
                     * ni la que hay debajo de este punto, lo cual produce como
                     * consecuencia la marchitez, y, por ende, la muerte del cultivo.
                     * 
                     * (*) La capacidad de almacenamiento de agua del suelo:
                     * - es en funcion de la profundidad radicular de un cultivo y
                     * de otros datos, ya que esta dada por la lamina total de agua
                     * disponible (dt) [mm], y
                     * - tiene dos extremos: capacidad de campo (extremo superior)
                     * y punto de marchitez permanente (extremo inferior).
                     * 
                     * El motivo por el cual se coloca el signo negativo al doble
                     * de la capacidad de almacenamiento de agua del suelo es que
                     * el acumulado del deficit de agua por dia [mm/dia] es menor
                     * o igual a cero.
                     */
                    if (accumulatedWaterDeficitPerDay < -(2 * totalAmountWaterAvailable)) {
                        stringAccumulatedWaterDeficitPerDay = notCalculated;
                        plantingRecordService.setStatus(developingPlantingRecord.getId(), deadStatus);
                    } else {
                        totalIrrigationWaterCurrentDate = irrigationRecordService.calculateTotalIrrigationWaterCurrentDate(parcel.getId());

                        /*
                         * Si la suma entre el acumulado del deficit de agua por dia
                         * [mm/dia] de dias previos a la fecha actual y la cantidad
                         * total de agua de riego de la fecha actual (es decir, hoy)
                         * [mm/dia] es menor o igual a la capacidad de campo (0) del
                         * suelo y estrictamente mayor a la lamina de riego optima
                         * (drop) [mm] negativa, significa que en la fecha actual el
                         * nivel de humedad del suelo, que tiene un cultivo sembrado,
                         * es menor o igual a la capacidad de campo (0) del suelo y
                         * estrictamente mayor a la lamina de riego optima. En esta
                         * situacion, el registro de plantacion en desarrollo adquiere
                         * el estado "Desarrollo optimo".
                         * 
                         * El motivo por el cual se coloca el signo negativo a la
                         * lamina de riego optima es que el acumulado del deficit
                         * de agua por dia [mm/dia] es menor o igual a cero.
                         */
                        if ((accumulatedWaterDeficitPerDay + totalIrrigationWaterCurrentDate) <= 0
                                && (accumulatedWaterDeficitPerDay + totalIrrigationWaterCurrentDate) > -(optimalIrrigationLayer)) {
                            plantingRecordService.setStatus(developingPlantingRecord.getId(), optimalDevelopmentStatus);
                        }

                        /*
                         * Si la suma entre el acumulado del deficit de agua por dia
                         * [mm/dia] de dias previos a la fecha actual y la cantidad
                         * total de agua de riego de la fecha actual (es decir, hoy)
                         * [mm/dia] es menor o igual a la lamina de riego optima (drop)
                         * [mm] negativa y estrictamente mayor al negativo de la
                         * capacidad de almacenamiento de agua del suelo (dt) [mm],
                         * significa que en la fecha actual el nivel de humedad del
                         * suelo, que tiene un cultivo sembrado, es menor o igual a
                         * la lamina de riego optima y estrictamente mayor a la
                         * capacidad de almacenamiento de agua del suelo. En esta
                         * situacion, el registro de plantacion en desarrollo
                         * adquiere el estado "Desarrollo en riesgo de marchitez".
                         * 
                         * El motivo por el cual se coloca el signo negativo a la
                         * lamina de riego optima y a la capacidad de almacenamiento
                         * de agua del suelo es que el acumulado del deficit de
                         * agua por dia [mm/dia] es menor o igual a cero.
                         */
                        if ((accumulatedWaterDeficitPerDay + totalIrrigationWaterCurrentDate) <= -(optimalIrrigationLayer)
                                && (accumulatedWaterDeficitPerDay + totalIrrigationWaterCurrentDate) > -(totalAmountWaterAvailable)) {
                            plantingRecordService.setStatus(developingPlantingRecord.getId(), developmentAtRiskWiltingStatus);
                        }

                        /*
                         * Si la suma entre el acumulado del deficit de agua por dia
                         * [mm/dia] de dias previos a la fecha actual y la cantidad
                         * total de agua de riego de la fecha actual (es decir, hoy)
                         * [mm/dia] es menor o igual al negativo de la capacidad de
                         * almacenamiento de agua del suelo [mm] y estrictamente
                         * mayor al doble del negativo de la capacidad de almacenamiento
                         * de agua del suelo [mm], significa que el nivel de humedad
                         * del suelo en la fecha actual es menor o igual a la capacidad
                         * de almacenamiento de agua del suelo y estrictamente mayor
                         * al doble de la capacidad de almacenamiento de agua del
                         * suelo. En esta situacion, el registro de plantacion en
                         * desarrollo adquiere el estado "Desarrollo en marchitez".
                         * 
                         * El motivo por el cual se coloca el signo negativo a la
                         * capacidad de almacenamiento de agua del suelo y a su doble
                         * es que el acumulado del deficit de agua por dia [mm/dia]
                         * es menor o igual a cero.
                         */
                        if ((accumulatedWaterDeficitPerDay + totalIrrigationWaterCurrentDate) <= -(totalAmountWaterAvailable)
                                && (accumulatedWaterDeficitPerDay + totalIrrigationWaterCurrentDate) > -(2 * totalAmountWaterAvailable)) {
                            plantingRecordService.setStatus(developingPlantingRecord.getId(), developmentInWiltingStatus);
                        }

                    } // End if

                } // End if

            } else {
                stringAccumulatedWaterDeficitPerDay = notCalculated;
            }

            /*
             * Si el balance hidrico de suelo de una parcela y una
             * fecha NO existe en la base de datos subyacente, se lo
             * crea y persiste. En caso contrario, se lo actualiza.
             */
            if (!soilWaterBalanceService.checkExistence(parcel.getId(), pastDate)) {
                soilWaterBalance = new SoilWaterBalance();
                soilWaterBalance.setDate(pastDate);
                soilWaterBalance.setParcelName(parcel.getName());
                soilWaterBalance.setCropName(crop.getName());
                soilWaterBalance.setWaterProvided(waterProvidedPerDay);
                soilWaterBalance.setEvaporatedWater(evaporatedWater);
                soilWaterBalance.setWaterDeficitPerDay(waterDeficitPerDay);
                soilWaterBalance.setAccumulatedWaterDeficitPerDay(stringAccumulatedWaterDeficitPerDay);

                /*
                 * Persistencia del balance hidrico
                 */
                soilWaterBalance = soilWaterBalanceService.create(soilWaterBalance);

                /*
                 * Se debe invocar el metodo merge() de la clase ParcelServiceBean
                 * para persistir los elementos que se hayan agregado a
                 * la coleccion soilWaterBalances de una parcela. De lo
                 * contrario, la base de datos subyacente quedara en un
                 * estado inconsistente.
                 */
                parcel.getSoilWaterBalances().add(soilWaterBalance);
                parcelService.merge(parcel);
            } else {
                soilWaterBalance = soilWaterBalanceService.find(parcel.getId(), pastDate);
                soilWaterBalanceService.update(soilWaterBalance.getId(), crop.getName(), evaporatedWater,
                        waterProvidedPerDay, waterDeficitPerDay, stringAccumulatedWaterDeficitPerDay);
            }

            /*
             * Suma un uno al numero de dia en el año de una fecha
             * pasada dada para obtener el siguiente registro climatico
             * correspondiente a una fecha pasada
             */
            pastDate.set(Calendar.DAY_OF_YEAR, pastDate.get(Calendar.DAY_OF_YEAR) + 1);
        } // End for

    }

    /**
     * Persiste una cantidad pastDaysReference de registros
     * climaticos anteriores a la fecha actual pertenecientes a
     * una parcela que tiene un cultivo sembrado y en desarrollo
     * en la fecha actual. Estos registros climaticos son obtenidos
     * del servicio meteorologico utilizado por la aplicacion.
     * 
     * El valor de pastDaysReference depende de cada usuario y
     * solo puede ser entre un limite minimo y un limite maximo,
     * los cuales estan definidos en la clase OptionServiceBean.
     * 
     * @param userId
     * @param parcelOption
     * @param developingPlantingRecord
     */
    private void requestPastClimateRecords(int userId, Option parcelOption, PlantingRecord developingPlantingRecord) throws IOException {
        /*
         * Esta variable representa la cantidad de registros climaticos
         * del pasado (es decir, anteriores a la fecha actual) que la
         * aplicacion recuperara del servicio meteorologico utilizado
         * y de los cuales calculara su ETo y ETc con el fin de calcular
         * la necesidad de agua de riego de un cultivo en la fecha actual
         */
        int pastDaysReference = 0;

        /*
         * Parcela que tiene un cultivo plantado y en desarrollo en
         * la fecha actual
         */
        Parcel givenParcel = developingPlantingRecord.getParcel();

        /*
         * Estas fechas son utilizadas para comprobar si existe el
         * ultimo riego registrado para una parcela dentro de los
         * ultimos 30 dias, si el usuario activa la opcion de calcular
         * la necesidad de agua de riego de un cultivo en la fecha
         * actual a partir del ultimo riego registrado para una
         * parcela dentro de los ultimos 30 dias. En caso de que
         * exista en la base de datos subyacente el ultimo riego
         * registrado para una parcela dentro de los ultimos 30
         * dias, estas fechas tambien se utilizan para obtener el
         * registro de riego correspondiente a dicho riego.
         */
        Calendar minorDate = UtilDate.getPastDateFromOffset(optionService.getValueThirtyDays());
        Calendar majorDate = UtilDate.getYesterdayDate();

        /*
         * Fecha a partir de la cual se obtienen los registros
         * climaticos del pasado pertenecientes a una parcela
         * que tiene un cultivo sembrado y en desarrollo en la
         * fecha actual
         */
        Calendar givenPastDate = null;
        ClimateRecord newClimateRecord;

        /*
         * Si la opcion de calcular la necesidad de agua de riego de
         * un cultivo en la fecha actual a partir del ultimo riego
         * registrado para una parcela dentro de los ultimos 30 dias,
         * esta activa, y existe dicho riego en la base de datos
         * subyacente, se utiliza la fecha del ultimo riego registrado
         * como fecha a partir de la cual obtener los registros climaticos
         * del pasado y se calcula la cantidad de dias pasados a utilizar
         * como referencia para obtener un registro climatico para
         * cada uno de ellos mediante la diferencia entre el numero
         * de dia en el año de la fecha del ultimo riego y el numero
         * de dia en el año de la fecha inmediatamente anterior a
         * la fecha actual
         */
        if (parcelOption.getFlagLastIrrigationThirtyDays() && irrigationRecordService.checkExistenceLastBetweenDates(userId, givenParcel.getId(), minorDate, majorDate)) {
            /*
             * La fecha a partir de la que se deben recuperar los registros
             * climaticos del pasado (es decir, anteriores a la fecha actual)
             * para una parcela es la fecha del ultimo riego registrado de una
             * parcela
             */
            Calendar dateLastIrrigationRecord = irrigationRecordService.findLastBetweenDates(userId, givenParcel.getId(), minorDate, majorDate).getDate();
            givenPastDate = Calendar.getInstance();
            givenPastDate.set(Calendar.YEAR, dateLastIrrigationRecord.get(Calendar.YEAR));
            givenPastDate.set(Calendar.MONTH, dateLastIrrigationRecord.get(Calendar.MONTH));
            givenPastDate.set(Calendar.DAY_OF_YEAR, dateLastIrrigationRecord.get(Calendar.DAY_OF_YEAR));

            /*
             * A la resta entre estas dos fechas se le suma un uno para
             * incluir la fecha mayor en el resultado, ya que dicha fecha
             * cuenta como un dia del pasado (es decir, anterior a la
             * fecha actual) para el cual se debe recuperar un registro
             * climatico. La variable majorDate contiene la referencia
             * a un objeto de tipo Calendar que contiene la fecha
             * inmediatamente anterior a la fecha actual.
             */
            pastDaysReference = UtilDate.calculateDifferenceBetweenDates(givenPastDate, majorDate) + 1;
        }

        /*
         * Si la opcion de calcular la necesidad de agua de riego de
         * un cultivo en la fecha actual a partir del ultimo riego
         * registrado para una parcela dentro de los ultimos 30 dias,
         * NO esta activa, o si NO existe el ultimo riego registrado
         * para una parcela dentro de los ultimos 30 dias, en caso de
         * que dicha opcion este activa, se utiliza la cantidad de
         * dias pasados como referencia de las opciones del usuario
         * para obtener un registro climatico para cada uno de ellos
         * y se calcula la fecha pasada (es decir, anterior a la
         * fecha actual) a partir de la cual obtener los registros
         * climaticos mediante dicha cantidad
         */
        if (!parcelOption.getFlagLastIrrigationThirtyDays() || !irrigationRecordService.checkExistenceLastBetweenDates(userId, givenParcel.getId(), minorDate, majorDate)) {
            pastDaysReference = parcelOption.getPastDaysReference();
            givenPastDate = UtilDate.getPastDateFromOffset(pastDaysReference);
        }

        /*
         * Crea y persiste pastDaysReference registros climaticos
         * anteriores a la fecha actual pertenecientes a una parcela que
         * tiene un cultivo plantado y en desarrollo en la fecha actual.
         * Estos registros climaticos van desde la fecha contenida en el
         * objeto de tipo Calendar referenciado por la variable de tipo
         * por referencia givenPastDate, hasta la fecha inmediatamente
         * anterior a la fecha actual. Las dos sentencias if anteriores
         * contienen la manera en la que se calcula la fecha referenciada
         * por givenPastDate.
         */
        for (int i = 0; i < pastDaysReference; i++) {

            /*
             * Si una parcela dada NO tiene un registro climatico
             * de una fecha anterior a la fecha actual, se lo solicita
             * al servicio meteorologico utilizado y se lo persiste
             */
            if (!climateRecordService.checkExistence(givenPastDate, givenParcel)) {
                newClimateRecord = ClimateClient.getForecast(givenParcel, givenPastDate.getTimeInMillis() / 1000);
                climateRecordService.create(newClimateRecord);
            }

            /*
             * Suma un uno al numero de dia en el año de una fecha
             * pasada dada para obtener el siguiente registro climatico
             * correspondiente a una fecha pasada
             */
            givenPastDate.set(Calendar.DAY_OF_YEAR, givenPastDate.get(Calendar.DAY_OF_YEAR) + 1);
        }

    }

    /**
     * Calcula y actualiza la ETo y la ETc de pastDaysReference
     * registros climaticos anteriores a la fecha actual pertenecientes
     * a una parcela que tiene un cultivo sembrado y en desarrollo en
     * la fecha actual.
     * 
     * El valor de pastDaysReference depende de cada usuario y
     * solo puede ser entre un limite minimo y un limite maximo,
     * los cuales estan definidos en la clase OptionServiceBean.
     * 
     * @param userId
     * @param parcelOption
     * @param developingPlantingRecord
     */
    private void calculateEtsPastClimateRecords(int userId, Option parcelOption, PlantingRecord developingPlantingRecord) {
        /*
         * Esta variable representa la cantidad de registros climaticos
         * del pasado (es decir, anteriores a la fecha actual) que la
         * aplicacion recuperara del servicio meteorologico utilizado
         * y de los cuales calculara su ETo y ETc con el fin de calcular
         * la necesidad de agua de riego de un cultivo en la fecha actual
         */
        int pastDaysReference = 0;

        /*
         * Parcela que tiene un cultivo plantado y en desarrollo en
         * la fecha actual
         */
        Parcel givenParcel = developingPlantingRecord.getParcel();

        /*
         * Estas fechas son utilizadas para comprobar si existe el
         * ultimo riego registrado para una parcela dentro de los
         * ultimos 30 dias, si el usuario activa la opcion de calcular
         * la necesidad de agua de riego de un cultivo en la fecha
         * actual a partir del ultimo riego registrado para una
         * parcela dentro de los ultimos 30 dias. En caso de que
         * exista en la base de datos subyacente el ultimo riego
         * registrado para una parcela dentro de los ultimos 30 dias,
         * estas fechas tambien se utilizan para obtener el registro
         * de riego correspondiente a dicho riego.
         */
        Calendar minorDate = UtilDate.getPastDateFromOffset(optionService.getValueThirtyDays());
        Calendar majorDate = UtilDate.getYesterdayDate();

        /*
         * Fecha a partir de la cual se obtienen los registros
         * climaticos del pasado pertenecientes a una parcela
         * que tiene un cultivo sembrado y en desarrollo en la
         * fecha actual
         */
        Calendar givenPastDate = null;
        ClimateRecord givenClimateRecord;
        PlantingRecord givenPlantingRecord;

        double eto = 0.0;
        double etc = 0.0;

        /*
         * Si la opcion de calcular la necesidad de agua de riego de
         * un cultivo en la fecha actual a partir del ultimo riego
         * registrado para una parcela dentro de los ultimos 30 dias,
         * esta activa, y existe dicho riego en la base de datos
         * subyacente, se utiliza la fecha del ultimo riego registrado
         * como fecha a partir de la cual obtener los registros climaticos
         * del pasado y se calcula la cantidad de dias pasados a utilizar
         * como referencia para obtener un registro climatico para
         * cada uno de ellos mediante la diferencia entre el numero
         * de dia en el año de la fecha del ultimo riego y el numero
         * de dia en el año de la fecha inmediatamente anterior a
         * la fecha actual
         */
        if (parcelOption.getFlagLastIrrigationThirtyDays() && irrigationRecordService.checkExistenceLastBetweenDates(userId, givenParcel.getId(), minorDate, majorDate)) {
            /*
             * La fecha a partir de la que se deben recuperar los registros
             * climaticos del pasado (es decir, anteriores a la fecha actual)
             * para una parcela es la fecha del ultimo riego registrado de una
             * parcela
             */
            Calendar dateLastIrrigationRecord = irrigationRecordService.findLastBetweenDates(userId, givenParcel.getId(), minorDate, majorDate).getDate();
            givenPastDate = Calendar.getInstance();
            givenPastDate.set(Calendar.YEAR, dateLastIrrigationRecord.get(Calendar.YEAR));
            givenPastDate.set(Calendar.MONTH, dateLastIrrigationRecord.get(Calendar.MONTH));
            givenPastDate.set(Calendar.DAY_OF_YEAR, dateLastIrrigationRecord.get(Calendar.DAY_OF_YEAR));

            /*
             * A la resta entre estas dos fechas se le suma un uno para
             * incluir la fecha mayor en el resultado, ya que dicha fecha
             * cuenta como un dia del pasado (es decir, anterior a la
             * fecha actual) para el cual se debe recuperar un registro
             * climatico. La variable majorDate contiene la referencia
             * a un objeto de tipo Calendar que contiene la fecha
             * inmediatamente anterior a la fecha actual.
             */
            pastDaysReference = UtilDate.calculateDifferenceBetweenDates(givenPastDate, majorDate) + 1;
        }

        /*
         * Si la opcion de calcular la necesidad de agua de riego de
         * un cultivo en la fecha actual a partir del ultimo riego
         * registrado para una parcela dentro de los ultimos 30 dias,
         * NO esta activa, o si NO existe el ultimo riego registrado
         * para una parcela dentro de los ultimos 30 dias, en caso de
         * que dicha opcion este activa, se utiliza la cantidad de dias
         * pasados como referencia de las opciones del usuario para
         * obtener un registro climatico para cada uno de ellos y
         * se calcula la fecha pasada (es decir, anterior a la
         * fecha actual) a partir de la cual obtener los registros
         * climaticos mediante dicha cantidad
         */
        if (!parcelOption.getFlagLastIrrigationThirtyDays() || !irrigationRecordService.checkExistenceLastBetweenDates(userId, givenParcel.getId(), minorDate, majorDate)) {
            pastDaysReference = parcelOption.getPastDaysReference();
            givenPastDate = UtilDate.getPastDateFromOffset(pastDaysReference);
        }

        /*
         * Calcula la ETo y la ETc de pastDaysReference registros
         * climaticos anteriores a la fecha actual pertenecientes a una
         * parcela que tiene un cultivo plantado y en desarrollo en la fecha
         * actual. Estos registros climaticos van desde la fecha resultante
         * de la resta entre el numero de dia en el año de la fecha actual
         * y pastDaysReference, hasta la fecha inmediatamente anterior
         * a la fecha actual.
         */
        for (int i = 0; i < pastDaysReference; i++) {

            /*
             * Si una parcela dada tiene un registro climatico de una
             * fecha anterior a la fecha actual, calcula la ETo y la
             * ETc del mismo
             */
            if (climateRecordService.checkExistence(givenPastDate, givenParcel)) {
                givenClimateRecord = climateRecordService.find(givenPastDate, givenParcel);

                eto = calculateEtoForClimateRecord(givenClimateRecord);

                /*
                 * Si la parcela dada tiene un registro de plantacion en
                 * el que la fecha pasada dada esta entre la fecha de siembra
                 * y la fecha de cosecha del mismo, se obtiene el kc
                 * (coeficiente de cultivo) del cultivo de este registro para
                 * poder calcular la ETc (evapotranspiracion del cultivo bajo
                 * condiciones estandar) de dicho cultivo.
                 * 
                 * En otras palabras, lo que hace esta instruccion if es
                 * preguntar "¿la parcela dada tuvo o tiene un cultivo
                 * sembrado en la fecha dada?". En caso afirmativo se
                 * obtiene el kc del cultivo para calcular su ETc, la
                 * cual se asignara al correspondiente registro climatico.
                 */
                if (plantingRecordService.checkExistence(givenParcel, givenPastDate)) {
                    givenPlantingRecord = plantingRecordService.find(givenParcel, givenPastDate);
                    etc = calculateEtcForClimateRecord(eto, givenPlantingRecord, givenPastDate);
                }

                climateRecordService.updateEtoAndEtc(givenPastDate, givenParcel, eto, etc);

                /*
                 * Luego de calcular la ETc de un registro climatico, se debe
                 * restablecer el valor por defecto de esta variable para evitar
                 * el error logico de asignar la ETc de un registro climatico a
                 * otro registro climatico
                 */
                etc = 0.0;
            }

            /*
             * Suma un uno al numero de dia en el año de una fecha
             * pasada dada para obtener el siguiente registro climatico
             * correspondiente a una fecha pasada
             */
            givenPastDate.set(Calendar.DAY_OF_YEAR, givenPastDate.get(Calendar.DAY_OF_YEAR) + 1);
        } // End for

    }

    /**
     * Calcula la necesidad de agua de riego de un cultivo en la fecha
     * actual [mm/dia] en funcion de la cantidad total de agua de riego
     * de la fecha actual, una coleccion de registros climaticos y una
     * coleccion de registros de riego, siendo todos ellos previos a la
     * fecha actual y pertenecientes a una misma parcela, la cual tiene
     * el cultivo para el cual se calcula la necesidad de agua de riego
     * en la fecha actual
     * 
     * @param userId
     * @param developingPlantingRecord
     * @return double que representa la necesidad de agua de riego
     * de un cultivo en la fecha actual [mm/dia]
     */
    private double calculateIrrigationWaterNeedCurrentDate(int userId, PlantingRecord developingPlantingRecord) {
        /*
         * Estas fechas se utilizan para obtener de la base de datos
         * subyacente los registros climaticos y los registros de riego
         * previos a la fecha actual (es decir, hoy) y pertenecientes a
         * una misma parcela, los cuales se utilizan para calcular la
         * necesidad de agua de riego de un cultivo en la fecha actual.
         * 
         * La variable dateFrom representa la fecha a partir de la cual
         * se obtienen los registros climaticos y los registros de riego
         * de una parcela previos a la fecha actual. En cambio, la variable
         * dateUntil representa la fecha hasta la cual se obtienen los
         * registros climaticos y los registros de riego de una parcela
         * previos a la fecha actual.
         * 
         * La fecha para la que se calcula la necesidad de agua de riego
         * de un cultivo esta determinada por los registros climaticos y
         * los registros de riego que se seleccionan como previos a una
         * fecha dada, siendo ambos grupos de registros pertenecientes a
         * una misma parcela.
         * 
         * Por ejemplo, si se seleccionan los registros climaticos y los
         * registros de riego de una parcela dada previos a la fecha
         * actual (es decir, hoy), la necesidad de agua de riego de un
         * cultivo calculada con estos registros corresponde a la fecha
         * actual. En cambio, si se seleccionan los registros climaticos
         * y los registros de riego de una parcela dada previos a la
         * fecha actual + X dias, donde X > 0, la necesidad de agua de
         * riego de un cultivo calculada con estos registros corresponde
         * a la fecha actual + X dias.
         * 
         * En el caso de este metodo, la variable dateUntil contiene la
         * fecha inmediatamente anterior a la fecha actual (es decir,
         * hoy) y la variable dateFrom contiene una fecha igual a X dias,
         * donde X > 0, anteriores a la fecha inmediatamente anterior a
         * la fecha actual, porque el objetivo de este metodo es calcular
         * la necesidad de agua de riego de un cultivo en la fecha actual.
         * 
         * Se puede calcular la necesidad de agua de riego de un cultivo
         * en una fecha pasada (es decir, anterior a la fecha actual),
         * pero esto no tiene sentido si lo que se busca es determinar
         * la necesidad de agua de riego de un cultivo en la fecha
         * actual o en una fecha posterior a la fecha actual. Este no
         * es el caso de este metodo, ya que, como se dijo anteriormente,
         * el objetivo del mismo es calcular la necesidad de agua de
         * riego de un cultivo en la fecha actual, motivo por el cual
         * se utilizan los registros climaticos y los registros de riego
         * previos a la fecha actual y pertenecientes a una misma parcela.
         */
        Calendar dateFrom = null;
        Calendar dateUntil = UtilDate.getYesterdayDate();

        Parcel givenParcel = developingPlantingRecord.getParcel();
        Option parcelOption = givenParcel.getOption();

        /*
         * Estas fechas son utilizadas para comprobar si existe el
         * ultimo riego registrado para una parcela dentro los ultimos
         * 30 dias, si el usuario activa la opcion de calcular la
         * necesidad de agua de riego de un cultivo en la fecha
         * actual a partir del ultimo riego registrado para una
         * parcela dentro de los ultimos 30 dias. En caso de que
         * exista en la base de datos subyacente el ultimo riego
         * registrado para una parcela dentro de los ultimos 30 dias,
         * estas fechas tambien se utilizan para obtener el registro
         * de riego correspondiente a dicho riego.
         */
        Calendar minorDate = UtilDate.getPastDateFromOffset(optionService.getValueThirtyDays());
        Calendar majorDate = UtilDate.getYesterdayDate();

        /*
         * Si la opcion de calcular la necesidad de agua de riego de
         * un cultivo en la fecha actual a partir del ultimo riego
         * registrado para una parcela dentro de los ultimos 30 dias,
         * esta activa, y existe dicho riego en la base de datos
         * subyacente, se utiliza la fecha del ultimo riego registrado
         * como fecha a partir de la cual obtener los registros climaticos
         * y los registros de riego de una parcela dada, siendo todos
         * ellos previos a la fecha actual, ya que lo se que busca
         * con este metodo es calcular la necesidad de agua de riego
         * de un cultivo en la fecha actual [mm/dia]
         */
        if (parcelOption.getFlagLastIrrigationThirtyDays() && irrigationRecordService.checkExistenceLastBetweenDates(userId, givenParcel.getId(), minorDate, majorDate)) {
            /*
             * La fecha a partir de la que se deben obtener los registros
             * climaticos y los registros de riego previos a la fecha
             * actual, siendo todos ellos pertenecientes a una misma
             * parcela, es la fecha del ultimo riego registrado de una
             * parcela
             */
            Calendar dateLastIrrigationRecord = irrigationRecordService.findLastBetweenDates(userId, givenParcel.getId(), minorDate, majorDate).getDate();
            dateFrom = Calendar.getInstance();
            dateFrom.set(Calendar.YEAR, dateLastIrrigationRecord.get(Calendar.YEAR));
            dateFrom.set(Calendar.MONTH, dateLastIrrigationRecord.get(Calendar.MONTH));
            dateFrom.set(Calendar.DAY_OF_YEAR, dateLastIrrigationRecord.get(Calendar.DAY_OF_YEAR));
        }

        /*
         * Si la opcion de calcular la necesidad de agua de riego de
         * un cultivo en la fecha actual a partir del ultimo riego
         * registrado para una parcela dentro de los ultimos 30 dias,
         * NO esta activa, o si NO existe el ultimo riego registrado
         * para una parcela dentro de los ultimos 30 dias, en caso de
         * que dicha opcion este activa, se utiliza la cantidad de
         * dias pasados como referencia de las opciones del usuario
         * para calcular la fecha pasada (es decir, anterior a la
         * fecha actual) a partir de la cual obtener los registros
         * climaticos y los registros de riego de una parcela dada,
         * siendo todos ellos previos a la fecha actual, ya que lo
         * que se busca con este metodo es calcular la necesidad de
         * agua de riego de un cultivo en la fecha actual [mm/dia]
         */
        if (!parcelOption.getFlagLastIrrigationThirtyDays() || !irrigationRecordService.checkExistenceLastBetweenDates(userId, givenParcel.getId(), minorDate, majorDate)) {
            dateFrom = UtilDate.getPastDateFromOffset(parcelOption.getPastDaysReference());
        }

        double totalIrrigationWaterCurrentDate = irrigationRecordService.calculateTotalIrrigationWaterCurrentDate(givenParcel.getId());

        /*
         * Obtiene de la base de datos subyacente los registros
         * climaticos y los registros de reigo de una parcela dada
         * que estan comprendidos entre una fecha desde y una fecha
         * hasta. La fecha hasta es el dia inmediatamente anterior a
         * la fecha actual (es decir, hoy) y la fecha desde es una
         * cantidad X > 0 de dias anteriores a la fecha inmediatamente
         * anterior a la fecha actual.
         * 
         * El motivo de esto es que este metodo tiene como objetivo
         * calcular la necesidad de agua de riego de un cultivo en la
         * fecha actual, para lo cual se requieren los registros
         * climaticos y los registros de riego de una parcela previos
         * a la fecha actual.
         */
        Collection<ClimateRecord> climateRecords = climateRecordService.findAllByParcelIdAndPeriod(userId, givenParcel.getId(), dateFrom, dateUntil);
        Collection<IrrigationRecord> irrigationRecords = irrigationRecordService.findAllByParcelIdAndPeriod(userId, givenParcel.getId(), dateFrom, dateUntil);

        /*
         * Genera los balances hidricos de suelo asociados al cultivo
         * para el que se calcula su necesidad de agua de riego en la
         * fecha actual [mm/dia]. El motivo de esto es para que el
         * usuario pueda ver la manera en la que la aplicacion calculo
         * dicha necesidad. El valor del deficit acumulado de agua del
         * mas actual de estos registros es la necesidad de agua de
         * riego del cultivo en la fecha actual. Por lo tanto, este
         * valor debe ser igual al valor del campo "Necesidad de agua
         * de riego de hoy [mm/dia]" de la ventana que se despliega
         * en la pagina web de lista de registros de plantacion cuando
         * se presiona el boton "Calcular" sobre un registro de plantacion
         * en desarrollo.
         */
        soilWaterBalanceService.generateSoilWaterBalances(developingPlantingRecord.getParcel(),
                developingPlantingRecord.getCrop().getName(), climateRecords, irrigationRecords);

        /*
         * Se debe invocar el metodo merge() de la clase ParcelServiceBean
         * para persistir los elementos que se hayan agregado a
         * la coleccion soilWaterBalances de una parcela durante
         * la ejecucion del metodo generateSoilWaterBalances de
         * la clase generateSoilWaterBalances(). De lo contrario,
         * la base de datos subyacente quedara en un estado
         * inconsistente.
         */
        parcelService.merge(givenParcel);

        double cropIrrigationWaterNeedCurrentDate = 0.0;

        /*
         * Si la bandera suelo NO esta activa se calcula la necesidad
         * de agua de riego de un cultivo en la fecha actual [mm/dia]
         * mediante el algoritmo que NO utiliza datos de suelo para
         * ello.
         * 
         * En otras palabras, se calcula la necesidad de agua de riego
         * de un cultivo en la fecha actual [mm/dia] sin tener en cuenta
         * el suelo de la parcela en la que esta sembrado, independientemente
         * de si la parcela tiene o no asignado un suelo.
         */
        if (!parcelOption.getSoilFlag()) {
            cropIrrigationWaterNeedCurrentDate = WaterNeedWos.calculateIrrigationWaterNeed(totalIrrigationWaterCurrentDate, climateRecords, irrigationRecords);
        }

        /*
         * Si la bandera suelo esta activa se calcula la necesidad
         * de agua de riego de un cultivo en la fecha actual [mm/dia]
         * mediante el algoritmo que utiliza datos de suelo para ello.
         * 
         * En otras palabras, se calcula la necesidad de agua de riego
         * de un cultivo en la fecha actual [mm/dia] teniendo en cuenta
         * el suelo de la parcela en la que esta sembrado.
         * 
         * Este algoritmo puede retornar el valor -1, el cual representa
         * la situacion en la que el nivel de humedad del suelo, que tiene
         * un cultivo sembrado, esta en el punto de marchitez permanente,
         * en el cual un cultivo no puede extraer agua del suelo y no
         * puede recuperarse de la perdida hidrica aunque la humedad
         * ambiental sea saturada.
         */
        if (parcelOption.getSoilFlag()) {
            cropIrrigationWaterNeedCurrentDate = WaterNeedWs.calculateIrrigationWaterNeed(totalIrrigationWaterCurrentDate,
                    developingPlantingRecord.getCrop(), givenParcel.getSoil(), climateRecords, irrigationRecords);
        }

        return cropIrrigationWaterNeedCurrentDate;
    }

    /**
     * Calcula la ETo (evapotranspiracion del cultivo de referencia)
     * con los datos meteorologicos de una fecha dada, la cual esta
     * determinada por un registro climatico, ya que un registro
     * climatico tiene fecha
     * 
     * @param givenClimateRecord
     * @return double que representa la ETo (evapotranspiracion del
     * cultivo de referencia) calculada en una fecha con los datos
     * meteorologicos de un registro climatico que tiene una fecha
     * y pertenece a una parcela
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
         * un registro climatico tiene fecha
         */
        return HargreavesEto.calculateEto(givenClimateRecord.getMaximumTemperature(), givenClimateRecord.getMinimumTemperature(), extraterrestrialSolarRadiation);
    }

    /**
     * Hay que tener en cuenta que este metodo calcula la ETc
     * de un cultivo para una fecha dada, ya que la ETo es de
     * una fecha dada. Si la ETo es de la fecha X, la ETc
     * calculada sera de la fecha X.
     * 
     * @param givenEto
     * @param givenPlantingRecord
     * @param dateUntil
     * @return double que representa la ETc (evapotranspiracion
     * del cultivo bajo condiciones estandar) de un cultivo
     * calculada con la ETo de una fecha dada, por lo tanto,
     * calcula la ETc de un cultivo que estuvo en desarollo
     * en una fecha dada
     */
    private double calculateEtcForClimateRecord(double givenEto, PlantingRecord givenPlantingRecord, Calendar dateUntil) {
        return Etc.calculateEtc(givenEto, cropService.getKc(givenPlantingRecord.getCrop(), givenPlantingRecord.getSeedDate(), dateUntil));
    }

    /**
     * Actualiza la lamina total de agua disponible (dt) [mm]
     * y la lamina de riego optima (drop) [mm] de un registro
     * de plantacion en desarrollo
     * 
     * @param developingPlantingRecord
     */
    private void updateIrrigationSheets(PlantingRecord developingPlantingRecord) throws IOException {
        Parcel givenParcel = developingPlantingRecord.getParcel();
        Option parcelOption = givenParcel.getOption();

        /*
         * Si la parcela de un registro de plantacion tiene la bandera
         * suelo activa, se calcula la lamina de riego optima (drop)
         * (umbral de riego) [mm] de la fecha actual y la lamina total
         * de agua disponible (dt) [mm] y se las asigna al registro de
         * plantacion en desarrollo
         */
        if (parcelOption.getSoilFlag()) {
            Crop givenCrop = developingPlantingRecord.getCrop();

            /*
             * Actualizacion de la lamina total de agua disponible (dt) [mm]
             * de un registro de plantacion en la base de datos subyacente
             */
            plantingRecordService.updateTotalAmountWaterAvailable(developingPlantingRecord.getId(),
                    WaterMath.calculateTotalAmountWaterAvailable(givenCrop, givenParcel.getSoil()));

            /*
             * Actualizacion de la lamina de riego optima (drop) [mm] de
             * un registro de plantacion en la base de datos subyacente.
             * A esta se le asigna el signo negativo (-) para poder
             * compararla con el acumulado del deficit de agua por dia
             * [mm/dia], el cual es negativo y es calculado desde la
             * fecha de siembra de un cultivo hasta la fecha inmediatamente
             * anterior a la fecha actual. La lamina de riego optima
             * representa la cantidad maxima de agua que puede perder
             * un suelo para el cultivo que tiene sembrado, a partir de
             * la cual NO conviene que pierda mas agua, sino que se le
             * debe añadir agua hasta llevar su nivel de humedad a capacidad
             * de campo. Capacidad de campo es la capacidad de almacenamiento
             * de agua que tiene un suelo. Un suelo que esta en capacidad
             * de campo es un suelo lleno de agua, pero no anegado. El
             * motivo por el cual se habla de llevar el nivel de humedad
             * del suelo, que tiene un cultivo sembrado, a capacidad de
             * campo es que el objetivo de la aplicacion es informar al
             * usuario la cantidad de agua que debe reponer en la fecha
             * actual (es decir, hoy) para llevar el nivel de humedad
             * del suelo, en el que tiene un cultivo sembrado, a capacidad
             * de campo. Esto es la cantidad de agua de riego [mm] que
             * debe usar el usuario para llenar el suelo en el que tiene
             * un cultivo sembrado, pero sin anegarlo.
             * 
             * El suelo agricola tiene dos limites: capacidad de campo
             * (limite superior) y punto de marchitez permanente (limite
             * inferior). La lamina de riego optima tambien se la conoce
             * como umbral de riego, debido a lo que representa.
             */
            plantingRecordService.updateOptimalIrrigationLayer(developingPlantingRecord.getId(),
                    WaterMath.calculateNegativeOptimalIrrigationLayer(givenCrop, givenParcel.getSoil()));
        }

    }

}