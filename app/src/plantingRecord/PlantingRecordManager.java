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
import stateless.TypePrecipitationServiceBean;
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
    @EJB TypePrecipitationServiceBean typePrecipService;

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
         * El valor de esta constante se utiliza para representar
         * la situacion en la que NO se calcula el acumulado del
         * deficit de agua por dia de un balance hidrico de suelo
         * de una parcela que tiene un cultivo sembrado y en
         * desarrollo. Esta situacion ocurre cuando la perdida de
         * humedad del suelo de un conjunto de dias es estrictamente
         * mayor al doble de la capacidad de almacenamiento de agua
         * del suelo. Esto se representa mediante la condicion de
         * que el acumulado del deficit de agua por dia sea estrictamente
         * menor al negativo del doble de la capacidad de almacenamiento
         * de agua del suelo, ya que el acumulado del deficit de
         * agua por dia puede ser negativo o cero. Cuando es negativo
         * representa que en un periodo de dias hubo perdida de humedad
         * en el suelo. En cambio, cuando es igual a cero representa
         * que la perdida de humedad que hubo en el suelo en un periodo
         * de dias esta totalmente cubierta. Esto es que el suelo
         * esta en capacidad de campo, lo significa que el suelo
         * esta lleno de agua o en su maxima capacidad de almacenamiento
         * de agua, pero no anegado.
         * 
         * Cuando la perdida de humedad del suelo, que tiene un
         * cultivo sembrado, de un conjunto de dias es estrictamente
         * mayor al doble de la capacidad de almacenamiento de agua
         * del suelo (representado mediante la conidicion de que el
         * acumulado del deficit de agua por dia sea estrictamente
         * menor al negativo del doble de la capacidad de almacenamiento
         * de agua del suelo), el cultivo esta muerto, ya que ningun
         * cultivo puede sobrevivir con dicha perdida de humedad.
         * Por lo tanto, la presencia del valor "NC" (no calculado)
         * tambien representa la muerte de un cultivo.
         */
        String notCalculated = soilWaterBalanceService.getNotCalculated();
        String stringIrrigationWaterNeedCurrentDate = soilWaterBalanceService.getNotCalculated();

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
             * Si el flujo de ejecucion llego a esta linea de codigo fuente
             * es porque efectivamente se calculo la necesidad de agua de
             * riego de un cultivo en la fecha actual (es decir, hoy), por
             * lo tanto, se asigna el valor false a la variable bandera
             * flagNotGenerateSoilMoistureGraph del registro de plantacion
             * para que se pueda visualizar su grafico de evolucion diaria
             * del nivel de humedad del suelo en caso de que dicho registro
             * pertenezca a una parcela que tiene la bandera suelo activa
             * en sus opciones
             */
            plantingRecordService.unsetFlagNotGenerateSoilMoistureGraph(developingPlantingRecord.getId());

            /*
             * Si la necesidad de agua de riego de un cultivo (en desarrollo)
             * en la fecha actual (es decir, hoy) [mm/dia] NO es "NC" (no
             * calculado) entonces es un numero, ya que el metodo
             * runCalculationIrrigationWaterNeedCurrentDateTwo() retorna
             * unicamente uno de los siguientes valores: un numero o "NC".
             * En el caso en el que dicha necesidad es un numero, se utiliza
             * dicho numero para actualizar el atributo "necesidad de agua
             * de riego de un cultivo" (*) del registro de plantacion
             * correspondiente que tiene un estado de desarrollo (en
             * desarrollo, desarrollo optimo, desarrollo en riesgo de marchitez,
             * desarrollo en marchitez).
             * 
             * (*) El atributo "necesidad de agua de riego de un cultivo" de
             * un registro de plantacion es la necesidad de agua de riego de
             * un cultivo en la fecha actual (es decir, hoy) [mm/dia].
             */
            if (!stringIrrigationWaterNeedCurrentDate.equals(notCalculated)) {
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
            }

            /*
             * Si la necesidad de agua de riego de un cultivo (en desarrollo)
             * en la fecha actual (es decir, hoy) [mm/dia] es "NC" (No Calculado,
             * valor de la variable notCalculated) significa que el algoritmo
             * utilizado para calcular dicha necesidad es el que utiliza datos
             * de suelo, ya que al utilizar este algoritmo se retorna el valor
             * "NC" para representar la situacion en la que NO se calcula el
             * acumulado del deficit de agua por dia del balance hidrico de una
             * parcela que tiene un cultivo sembrado y en desarrollo. Esta
             * situacion ocurre cuando la perdida de humedad del suelo, que
             * tiene un cultivo sembrado, es estrictamente mayor al doble de
             * la capacidad de almacenamiento de agua del mismo.
             * 
             * Cuando la perdida de humedad del suelo, que tiene un cultivo,
             * sembrado es estrictamente mayor al doble de la capacidad de
             * almacenamiento de agua del mismo, el cultivo muere. Cuando
             * ocurre esto, la aplicacion ejecuta las siguientes operaciones
             * sobre un registro de plantacion que tiene una parcela que
             * tiene la bandera suelo activa en sus opciones:
             * 1. Establece el estado "Muerto".
             * 2. Asigna valor "n/a" (no disponible) en el atributo "necesidad
             * de agua de riego de un cultivo" (*).
             * 3. Establece la fecha de muerte.
             * 
             * El establecimiento del estado "Muerto" y de la fecha de muerte
             * se realiza en el metodo calculateSoilWaterBalances() de esta
             * clase.
             * 
             * (*) El atributo "necesidad de agua de riego de un cultivo" de
             * un registro de plantacion es la necesidad de agua de riego de
             * un cultivo en la fecha actual (es decir, hoy) [mm/dia].
             */
            if (stringIrrigationWaterNeedCurrentDate.equals(notCalculated)) {
                plantingRecordService.updateCropIrrigationWaterNeed(developingPlantingRecord.getId(), notAvailable);
            }

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
         * fecha actual (es decir, hoy) [mm/dia].
         * 
         * El balance hidrico de la fecha de siembra de un cultivo tiene
         * el valor 0 en todos sus atributos porque en la fecha de siembra
         * de un cultivo se parte del suelo a capacidad de campo, esto es
         * que el suelo esta lleno de agua o en su maxima capacidad de
         * almacenamiento de agua, pero no anegado.
         */
        persistSoilWaterBalanceSeedDate(developingPlantingRecord);

        /*
         * El valor de esta constante se utiliza para representar
         * la situacion en la que NO se calcula el acumulado del
         * deficit de agua por dia de un balance hidrico de suelo
         * de una parcela que tiene un cultivo sembrado y en
         * desarrollo. Esta situacion ocurre cuando la perdida de
         * humedad del suelo de un conjunto de dias es estrictamente
         * mayor al doble de la capacidad de almacenamiento de agua
         * del suelo. Esto se representa mediante la condicion de
         * que el acumulado del deficit de agua por dia sea estrictamente
         * menor al negativo del doble de la capacidad de almacenamiento
         * de agua del suelo, ya que el acumulado del deficit de
         * agua por dia puede ser negativo o cero. Cuando es negativo
         * representa que en un periodo de dias hubo perdida de humedad
         * en el suelo. En cambio, cuando es igual a cero representa
         * que la perdida de humedad que hubo en el suelo en un periodo
         * de dias esta totalmente cubierta. Esto es que el suelo
         * esta en capacidad de campo, lo significa que el suelo
         * esta lleno de agua o en su maxima capacidad de almacenamiento
         * de agua, pero no anegado.
         * 
         * Cuando la perdida de humedad del suelo, que tiene un
         * cultivo sembrado, de un conjunto de dias es estrictamente
         * mayor al doble de la capacidad de almacenamiento de agua
         * del suelo (representado mediante la conidicion de que el
         * acumulado del deficit de agua por dia sea estrictamente
         * menor al negativo del doble de la capacidad de almacenamiento
         * de agua del suelo), el cultivo esta muerto, ya que ningun
         * cultivo puede sobrevivir con dicha perdida de humedad.
         * Por lo tanto, la presencia del valor "NC" (no calculado)
         * tambien representa la muerte de un cultivo.
         */
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
        String stringAccumulatedWaterDeficitPerDayFromYesterday = soilWaterBalanceService.find(parcel.getId(), yesterday).getAccumulatedWaterDeficitPerDay();

        /*
         * Si el valor del acumulado del deficit de agua por dia [mm/dia]
         * de ayer es "NC" (no calculado), significa dos cosas:
         * - que el algoritmo utilizado para calcular la necesidad de
         * agua de riego de un cultivo en la fecha actual [mm/dia] es
         * el que utiliza el suelo para ello,
         * - y que la perdida de humeda del suelo, que tiene un cultivo
         * sembrado, fue estrictamente mayor al doble de la capacidad de
         * almacenamiento de agua del suelo.
         * 
         * Por lo tanto, se retorna "NC" para indicar que la necesidad de
         * agua de riego de un cultivo en la fecha actual [mm/dia] no se
         * calculo, ya que el cultivo esta muerto debido a que ningun
         * cultivo puede sobrevivir con una perdida de humedad del suelo
         * estrictamente mayor al doble de la capacidad de almacenamiento
         * de agua del suelo.
         */
        if (stringAccumulatedWaterDeficitPerDayFromYesterday.equals(notCalculated)) {
            return notCalculated;
        }

        double totalIrrigationWaterCurrentDate = irrigationRecordService.calculateTotalIrrigationWaterCurrentDate(parcel.getId());
        double accumulatedWaterDeficitPerDayFromYesterday = Double.parseDouble(stringAccumulatedWaterDeficitPerDayFromYesterday);

        /*
         * Si la bandera suelo de una parcela esta activa, se
         * comprueba el nivel de humedad del suelo para establecer
         * el estado del registro de plantacion en desarrollo para
         * el que se calcula la necesidad de agua de riego de su
         * cultivo en la fecha actual (es decir, hoy) [mm/dia].
         * 
         * El metodo calculateStatusRelatedToSoilMoistureLevel()
         * de la clase PlantingRecordStatusService se ocupa de
         * comprobar el nivel de humedad del suelo y retorna el
         * estado correspondiente.
         */
        if (parcel.getOption().getSoilFlag()) {
            plantingRecordService.setStatus(developingPlantingRecord.getId(),
                    statusService.calculateStatusRelatedToSoilMoistureLevel(totalIrrigationWaterCurrentDate,
                            accumulatedWaterDeficitPerDayFromYesterday, developingPlantingRecord));
        }

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
        return String.valueOf(WaterMath.calculateIrrigationWaterNeed(totalIrrigationWaterCurrentDate, accumulatedWaterDeficitPerDayFromYesterday));
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
        ClimateRecord climateRecord = null;

        /*
         * Los balances hidricos de suelo se calculan a partir de la
         * fecha inmediatamente siguiente a la fecha de siembra de un
         * cultivo hasta la fecha inmediatamente anterior a la fecha
         * actual (es decir, hoy). Por este motivo se solicitan (al
         * servicio meteorologico utilizado por la aplicacion) y persisten
         * los registros climaticos (contienen datos meteorologicos)
         * de una parcela, que tiene un cultivo sembrado, desde la
         * fecha inmediatamente siguiente a la fecha de siembra de
         * un cultivo hasta la fecha inmediatamente anterior a la
         * fecha actual (es decir, hoy). Los datos meteorologicos de
         * cada uno de los dias perteneciente al periodo definido por
         * ambas fechas, son necesarios para calcular la ETo (evapotranspiracion
         * del cultivo de referencia) [mm/dia] y la ETc (evapotranspiracion
         * del cultivo bajo condiciones estandar) [mm/dia] de cada
         * uno de los dias de dicho periodo. La ETo y la ETc de cada
         * uno de los dias de dicho periodo son necesarias para calcular
         * el balance hidrico de suelo de cada uno de los dias pertencientes
         * al periodo, lo cual da como resultado un conjunto de balances
         * hidricos de suelo calculados desde la fecha inmediatamente
         * siguiente a la fecha de siembra de un cultivo hasta la
         * fecha inmediatamente anterior a la fecha actual (es decir,
         * hoy). Estos son necesarios para determinar el acumulado
         * del deficit agua por dia [mm/dia] del dia inmediatamente
         * anterior a la fecha actual en la que esta sembrado un
         * cultivo. Este valor acumulado es necesario porque en base
         * a el se determina la necesidad de agua de riego de un
         * cultivo en la fecha actual (es decir, hoy) [mm/dia].
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
             * Si una parcela NO tiene un registro climatico perteneciente
             * a una fecha, se lo solicita al servicio meteorologico
             * utilizado y se lo persiste
             */
            if (!climateRecordService.checkExistence(pastDate, parcel)) {
                newClimateRecord = ClimateClient.getForecast(parcel, pastDate, typePrecipService.findAll());
                climateRecordService.create(newClimateRecord);
            }

            /*
             * Si existe el registro climatico perteneciente a una parcela
             * y una fecha y si la ubicacion geografica de una parcela fue
             * modificada, se solicitan los datos meteorologicos correspondientes
             * a una fecha y una nueva ubicacion geografica. Esto es necesario
             * para actualizar los datos meteorologicos de los registros
             * climaticos comprendidos en un periodo definido por dos fechas
             * pertenecientes a una parcela que fueron obtenidos antes de la
             * modificacion de la ubicacion geografica de la misma.
             */
            if (climateRecordService.checkExistence(pastDate, parcel) && parcel.getModifiedGeographicLocationFlag()) {
                climateRecord = climateRecordService.find(pastDate, parcel);
                climateRecordService.modify(climateRecord.getId(), ClimateClient.getForecast(parcel, pastDate, typePrecipService.findAll()));
            }

            /*
             * Suma un uno al numero de dia en el año de una fecha
             * pasada dada para obtener el siguiente registro climatico
             * correspondiente a una fecha pasada
             */
            pastDate.set(Calendar.DAY_OF_YEAR, pastDate.get(Calendar.DAY_OF_YEAR) + 1);
        } // End for

        /*
         * Luego de actualizar los registros climaticos comprendidos en
         * un periodo definido por dos fechas pertenecientes a una parcela,
         * con los datos meteorologicos de las fechas de dicho periodo y de
         * la nueva ubicacion geografica de una parcela, se establece la
         * bandera modifiedGeographicLocationFlag de una parcela en false
         * para evitar que la aplicacion solicite nuevamente los datos
         * meteorologicos de la nueva ubicacion geografica de una parcela
         * al calcular la necesidad de agua de riego de un cultivo (en
         * desarrollo) en la fecha actual
         */
        if (parcel.getModifiedGeographicLocationFlag()) {
            parcelService.unsetModifiedGeographicLocationFlag(parcel.getId());
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
         * Los balances hidricos de suelo se calculan a partir de la
         * fecha inmediatamente siguiente a la fecha de siembra de un
         * cultivo hasta la fecha inmediatamente anterior a la fecha
         * actual (es decir, hoy). Por este motivo se calculan la ETo
         * (evapotranspiracion del cultivo de referencia) [mm/dia] y
         * la ETc (evapotranspiracion del cultivo bajo condiciones
         * estandar) [mm/dia] de los registros climaticos (contienen
         * datos meteorologicos), pertenecientes a una parcela que
         * tiene un cultivo sembrado, a partir de la fecha inmediatamente
         * siguiente a la fecha de siembra de un cultivo hasta la fecha
         * inmediatamente anterior a la fecha actual. La ETo y la ETc
         * de cada uno de los dias pertenecientes al periodo definido
         * por la fecha inemdiatamente siguiente a la fecha de siembra
         * de un cultivo y la fecha inmediatamente anterior a la
         * fecha actual, son necesarias para el calculo del balance
         * hidrico de suelo de cada uno de esos dias. Esto da como
         * resultado un conjunto de balances hidricos de suelo
         * calculados desde la fecha inmediatamente siguiente a la
         * fecha de siembra de un cultivo hasta la fecha inmediatamente
         * anterior a la fecha actual, los cuales son necesarios para
         * determinar el acumulado del deficit agua por dia [mm/dia]
         * del dia inmediatamente anterior a la fecha actual en la
         * que esta sembrado un cultivo. Este valor acumulado es
         * necesario porque en base a el se determina la necesidad de
         * agua de riego de un cultivo en la fecha actual (es decir,
         * hoy) [mm/dia].
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
            soilWaterBalance.setWaterProvidedPerDay(0);
            soilWaterBalance.setSoilMoistureLossPerDay(0);
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
            soilWaterBalance.setWaterProvidedPerDay(0);
            soilWaterBalance.setSoilMoistureLossPerDay(0);
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
        Calendar yesterday = UtilDate.getYesterdayDate();

        /*
         * Si la fecha de siembra de un cultivo es igual a la fecha
         * actual (es decir, hoy), NO se calcula el balance hidrico
         * de la fecha actual, ya que en la fecha de siembra se parte
         * del suelo en capacidad de campo, esto es que el suelo esta
         * lleno de agua o en maxima capacidad de almacenamiento de
         * agua, pero no anegado. En esta situacion, el acumulado del
         * deficit de agua por dia [mm/dia] de la fecha actual es 0.
         * Por lo tanto, la necesidad de agua de riego de un cultivo
         * en la fecha actual [mm/dia] es 0.
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
         * suelo esta lleno de agua o en su maxima capacidad de
         * almacenamiento de agua, pero no anegado. En esta situacion,
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
        Calendar soilWaterBalanceDate = null;

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
        int parcelId = parcel.getId();

        /*
         * El valor de esta variable es la precipitacion
         * natural por dia [mm/dia] o la precipitacion
         * artificial (agua de riego) por dia [mm/dia] o
         * la suma de ambas [mm/dia]
         */
        double waterProvidedPerDay = 0.0;
        double waterDeficitPerDay = 0.0;
        double soilMoistureLossPerDay = 0.0;
        double accumulatedWaterDeficitPerDay = 0.0;
        double accumulatedWaterDeficitPerPreviousDay = 0.0;
        double totalAmountCropIrrigationWaterPerDay = 0.0;

        /*
         * Esta variable representa la capacidad de almacenamiento
         * de agua del suelo [mm], la cual esta determinada por
         * la lamina total de agua disponible (dt) [mm]
         */
        double totalAmountWaterAvailable = 0.0;

        String stringAccumulatedWaterDeficitPerPreviousDay = null;
        String stringAccumulatedWaterDeficitPerDay = null;

        /*
         * El valor de esta constante se utiliza para representar
         * la situacion en la que NO se calcula el acumulado del
         * deficit de agua por dia de un balance hidrico de suelo
         * de una parcela que tiene un cultivo sembrado y en
         * desarrollo. Esta situacion ocurre cuando la perdida de
         * humedad del suelo de un conjunto de dias es estrictamente
         * mayor al doble de la capacidad de almacenamiento de agua
         * del suelo. Esto se representa mediante la condicion de
         * que el acumulado del deficit de agua por dia sea estrictamente
         * menor al negativo del doble de la capacidad de almacenamiento
         * de agua del suelo, ya que el acumulado del deficit de
         * agua por dia puede ser negativo o cero. Cuando es negativo
         * representa que en un periodo de dias hubo perdida de humedad
         * en el suelo. En cambio, cuando es igual a cero representa
         * que la perdida de humedad que hubo en el suelo en un periodo
         * de dias esta totalmente cubierta. Esto es que el suelo
         * esta en capacidad de campo, lo significa que el suelo
         * esta lleno de agua o en su maxima capacidad de almacenamiento
         * de agua, pero no anegado.
         * 
         * Cuando la perdida de humedad del suelo, que tiene un
         * cultivo sembrado, de un conjunto de dias es estrictamente
         * mayor al doble de la capacidad de almacenamiento de agua
         * del suelo (representado mediante la conidicion de que el
         * acumulado del deficit de agua por dia sea estrictamente
         * menor al negativo del doble de la capacidad de almacenamiento
         * de agua del suelo), el cultivo esta muerto, ya que ningun
         * cultivo puede sobrevivir con dicha perdida de humedad.
         * Por lo tanto, la presencia del valor "NC" (no calculado)
         * tambien representa la muerte de un cultivo.
         */
        String notCalculated = soilWaterBalanceService.getNotCalculated();
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

            /*
             * Actualiza la entidad de tipo ClimateRecord con los datos
             * de la base de datos subyacente obteniendo de esta manera
             * una referencia a un objeto de tipo ClimateRecord consistente.
             * Esto significa que el objeto contiene los datos que tiene
             * la entidad correspondiente en la base de datos subyacente.
             * 
             * Esta instruccion es necesaria para que este metodo
             * persista balances hidricos de suelo de forma consistente.
             * Esto es que contengan los datos que deben contener en
             * funcion de los registros climaticos obtenidos (de
             * Visual Crossing Weather) y persisitdos. Los balances
             * hidricos de suelo pertenecen a una parcela que tiene
             * un registro de plantacion que tiene un estado de
             * desarrollo (en desarrollo, desarrollo optimo, desarrollo
             * en riesgo de marchitez, desarrollo en marchitez).
             */
            climateRecordService.refresh(climateRecord);

            totalAmountCropIrrigationWaterPerDay = irrigationRecordService.calculateTotalAmountCropIrrigationWaterForDate(parcelId, pastDate);
            waterProvidedPerDay = WaterMath.calculateWaterProvidedPerDay(climateRecord.getPrecip(), totalAmountCropIrrigationWaterPerDay);
            waterDeficitPerDay = WaterMath.calculateWaterDeficitPerDay(climateRecord.getEtc(), climateRecord.getPrecip(), totalAmountCropIrrigationWaterPerDay);
            soilMoistureLossPerDay = climateRecord.getEtc();

            /*
             * Obtiene el acumulado del deficit de agua por dia del
             * balance hidrico de suelo de la fecha inmediatamente
             * a una fecha pasada
             */
            yesterdayDateFromDate = UtilDate.getYesterdayDateFromDate(pastDate);
            stringAccumulatedWaterDeficitPerPreviousDay = soilWaterBalanceService.find(parcelId, yesterdayDateFromDate).getAccumulatedWaterDeficitPerDay();

            /*
             * Si el acumulado del deficit de agua por dia de la fecha
             * inmediatamente anterior a una fecha pasada NO es NC (no
             * calculado), significa que el cultivo correspondiente a
             * este calculo de balances hidricos de suelo NO murio en
             * la fecha inmediatamente anterior a la fecha pasada, por
             * lo tanto, se calcula el acumulado del deficit de agua por
             * dia [mm/dia] de la fecha pasada, lo cual se realiza para
             * calcular el balance hidrico de suelo, en el que esta
             * sembrado un cultivo, de la fecha pasada. En caso contrario,
             * significa que el cultivo murio en la fecha inmediatamente
             * anterior a la fecha pasada, por lo tanto, NO se calcula el
             * acumulado del deficit de agua por dia [mm/dia] de la fecha
             * pasada. Cuando un cultivo esta muerto se asigna la sigla
             * "NC" (No Calculado) a la variable de tipo String
             * accumulatedWaterDeficitPerDay [mm/dia] de un balance
             * hidrico.
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
                 * Si el estado del registro de plantacion que contiene el
                 * cultivo para el que se calcula la necesidad de agua de
                 * riego en la fecha actual (es decir, hoy) [mm/dia], NO
                 * tiene el estado "Muerto" y si la parcela a la que
                 * pertenece tiene la bandera suelo activa en sus opciones,
                 * se comprueba si el acumulado del deficit de agua por
                 * dia de una fecha pasada es estrictamente menor al doble
                 * de la capacidad de almacenamiento de agua del suelo que
                 * contiene la parcela. Si lo es, el cultivo que contiene
                 * el registro de plantacion ha muerto en una fecha
                 * pasada. En caso contrario, no ha muerto en una fecha
                 * pasada.
                 * 
                 * Lo que se busca determinar con esta comprobacion es
                 * determinar si la perdida de humedad del suelo, que
                 * tiene un cultivo sembrado, es estrictamente mayor al
                 * doble de la capacidad de almacenamiento de agua del
                 * suelo en una fecha pasada. Si lo es, el cultivo murio
                 * en una fecha pasada, ya que ningún cultivo puede
                 * sobrevivir con dicha perdida. El motivo de esto es
                 * que cuando el acumulado del deficit de agua por dia
                 * es negativo representa que en un periodo de dias hubo
                 * perdida de humedad en el suelo. El acumulado del deficit
                 * de agua por dia tambien puede ser cero, ademas de negativo.
                 * Cuando es igual a cero representa que la perdida de
                 * humedad que hubo en el suelo en un periodo de dias esta
                 * totalmente cubierta. Esto es que el suelo esta en capacidad
                 * de campo, lo que significa que el suelo esta lleno de
                 * agua o en su maxima capacidad de almacenamiento de agua,
                 * pero no anegado.
                 */
                if (!statusService.equals(developingPlantingRecord.getStatus(), deadStatus) && parcel.getOption().getSoilFlag()) {

                    /*
                     * El suelo de una parcela debe ser obtenido unicamente
                     * si la bandera suelo de las opciones de una parcela
                     * esta activa. Esto se debe a que la aplicacion
                     * permite que la bandera suelo de las opciones de una
                     * parcela sea activada si y solo si una parcela tiene
                     * un suelo asignado. Esto esta implementado como un
                     * control en el metodo modify() de la clase OptionRestServlet.
                     */
                    totalAmountWaterAvailable = WaterMath.calculateTotalAmountWaterAvailable(crop, parcel.getSoil());

                    /*
                     * Si el acumulado del deficit de agua por dia [mm/dia] de
                     * dias previos a una fecha es estrictamente menor al negativo
                     * del doble de la capacidad de almacenamiento de agua del
                     * suelo, significa que la perdida de humedad del suelo, que
                     * tiene un cultivo sembrado, de los dias previos a una fecha
                     * es estrictamente mayor al doble de la capacidad de almacenamiento
                     * de agua del suelo. En esta situacion el cultivo esta muerto
                     * y el registro de plantacion en desarrollo adquiere el estado
                     * "Muerto". Todo esto se debe a que un acumulado del deficit
                     * de agua por dia [mm/dia] negativo representa que en un
                     * conjunto de dias hubo perdida de humedad en el suelo.
                     * 
                     * Las raices de un cultivo pueden crecer mas alla de la
                     * capacidad de almacenamiento de agua del suelo [mm], con
                     * lo cual un cultivo puede absorber el agua que hay en el
                     * punto de marchitez permanente del suelo (*) y la que hay
                     * debajo de este punto. Las raices de un cultivo no crecen
                     * mas alla del doble de la capacidad de almacenamiento de
                     * agua del suelo [mm], con lo cual un cultivo no puede
                     * absorber el agua que hay debajo de este punto. Por lo tanto,
                     * si el nivel de humedad del suelo, en el que esta sembrado
                     * un cultivo, es estrictamente menor al doble de la capacidad
                     * de almacenamiento de agua del mismo, el cultivo no puede
                     * absorber el agua que hay debajo de este punto, lo cual
                     * produce la muerte del cultivo, ya que ningun cultivo puede
                     * sobrevivir con una perdida de humedad del suelo estrictamente
                     * mayor al doble de la capacidad de almacenamiento de agua
                     * del suelo.
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
                        plantingRecordService.setDeathDate(developingPlantingRecord.getId(), pastDate);
                        plantingRecordService.setStatus(developingPlantingRecord.getId(), deadStatus);
                    }

                } // End if

            } else {
                stringAccumulatedWaterDeficitPerDay = notCalculated;
            }

            /*
             * Si el balance hidrico de suelo de una parcela y una
             * fecha NO existe en la base de datos subyacente, se lo
             * crea y persiste. En caso contrario, se lo actualiza.
             */
            if (!soilWaterBalanceService.checkExistence(parcelId, pastDate)) {
                soilWaterBalanceDate = Calendar.getInstance();
                soilWaterBalanceDate.set(Calendar.YEAR, pastDate.get(Calendar.YEAR));
                soilWaterBalanceDate.set(Calendar.MONTH, pastDate.get(Calendar.MONTH));
                soilWaterBalanceDate.set(Calendar.DAY_OF_MONTH, pastDate.get(Calendar.DAY_OF_MONTH));

                soilWaterBalance = new SoilWaterBalance();
                soilWaterBalance.setDate(soilWaterBalanceDate);
                soilWaterBalance.setParcelName(parcel.getName());
                soilWaterBalance.setCropName(crop.getName());
                soilWaterBalance.setWaterProvidedPerDay(waterProvidedPerDay);
                soilWaterBalance.setSoilMoistureLossPerDay(soilMoistureLossPerDay);
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
                soilWaterBalance = soilWaterBalanceService.find(parcelId, pastDate);
                soilWaterBalanceService.update(soilWaterBalance.getId(), crop.getName(), soilMoistureLossPerDay,
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
        Parcel parcel = givenClimateRecord.getParcel();
        double extraterrestrialSolarRadiation = solarService.getRadiation(parcel.getGeographicLocation().getLatitude(),
                monthService.getMonth(givenClimateRecord.getDate().get(Calendar.MONTH)),
                latitudeService.find(parcel.getGeographicLocation().getLatitude()),
                latitudeService.findPreviousLatitude(parcel.getGeographicLocation().getLatitude()),
                latitudeService.findNextLatitude(parcel.getGeographicLocation().getLatitude()));

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

}