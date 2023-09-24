package plantingRecord;

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
import stateless.MonthServiceBean;
import stateless.OptionServiceBean;
import stateless.LatitudeServiceBean;
import model.Parcel;
import model.PlantingRecord;
import model.ClimateRecord;
import model.IrrigationRecord;
import model.Option;
import model.User;
import irrigation.WaterMath;
import util.UtilDate;
import et.HargreavesEto;
import et.Etc;
import climate.ClimateClient;

@Stateless
public class PlantingRecordManager {

    // inject a reference to the PlantingRecordServiceBean
    @EJB
    PlantingRecordServiceBean plantingRecordService;

    @EJB
    PlantingRecordStatusServiceBean plantingRecordStatusService;

    @EJB
    CropServiceBean cropService;

    @EJB
    IrrigationRecordServiceBean irrigationRecordService;

    @EJB
    ClimateRecordServiceBean climateRecordService;

    @EJB
    SolarRadiationServiceBean solarService;

    @EJB
    MonthServiceBean monthService;

    @EJB
    LatitudeServiceBean latitudeService;

    @EJB
    OptionServiceBean optionService;

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
        Collection<PlantingRecord> plantingRecords = plantingRecordService.findAllInDevelopment();

        for (PlantingRecord currentPlantingRecord : plantingRecords) {
            /*
             * Si un registro de plantacion presuntamente en desarrollo,
             * NO esta en desarrollo, se establece el estado finalizado
             * en el mismo.
             * 
             * Un registro de plantacion en desarrollo, esta en desarrollo
             * si su fecha de siembra es menor o igual a la fecha actual y
             * su fecha de cosecha es mayor o igual a la fecha actual.
             * En cambio, si su fecha de cosecha es estrictamente menor
             * a la fecha actual, se debe establecer el estado finalizado
             * en el mismo.
             */
            if (!plantingRecordService.checkDevelopmentStatus(currentPlantingRecord)) {
                plantingRecordService.setStatus(currentPlantingRecord.getId(), plantingRecordStatusService.findFinishedStatus());
            }

        }

    }

    /*
     * Establece de manera automatica el estado en desarrollo en el registro
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
    public void modifyToInDevelopmentStatus() {
        /*
         * Obtiene una coleccion que contiene el registro de
         * plantacion en espera mas antiguo de los registros
         * de plantacion en espera de cada una de las parcelas
         * registradas en la base de datos subyacente
         */
        Collection<PlantingRecord> plantingRecords = plantingRecordService.findAllInWaiting();

        for (PlantingRecord currentPlantingRecord : plantingRecords) {
            /*
             * Si un registro de plantacion presuntamente en espera, NO
             * esta en espera, se establece el estado en desarrollo en
             * el mismo.
             * 
             * Un registro de plantacion en espera, esta en espera si su
             * fecha de siembra es estrictamente mayor (es decir, posterior)
             * a la fecha actual. En cambio, si su fecha de siembra es menor
             * o igual a la fecha actual, se debe establecer el estado en
             * desarrollo en el mismo.
             */
            if (!plantingRecordService.checkWaitingStatus(currentPlantingRecord)) {
                plantingRecordService.setStatus(currentPlantingRecord.getId(), plantingRecordStatusService.findDevelopmentStatus());
            }

        }

    }

    /**
     * Establece de manera automatica la necesidad de agua de riego [mm/dia]
     * (atributo irrigationWaterNeed) de un registro de plantacion en desarrollo
     * cada dos horas a partir de la hora 01.
     * 
     * La segunda anotacion @Schedule es para probar que este metodo se ejecuta
     * correctamente, es decir, que asigna un valor al atributo irrigationWaterNeed
     * de un registro de plantacion en desarrollo.
     * 
     * El archivo t125Inserts.sql de la ruta app/etc/sql tiene datos para probar que
     * este metodo se ejecuta correctamente, es decir, que hace lo que se espera que
     * haga.
     */
    // @Schedule(second = "*", minute = "*", hour = "1/2", persistent = false)
    // @Schedule(second = "*/5", minute = "*", hour = "*", persistent = false)
    private void setIrrigationWaterNeed() {
        Collection<PlantingRecord> developingPlantingRecords = plantingRecordService.findAllInDevelopment();
        Parcel givenParcel = null;
        User givenUser = null;

        double currentIrrigationWaterNeed = 0.0;

        /*
         * Establece la necesidad de agua de riego [mm/dia] de la fecha
         * actual en cada uno de los registros de plantacion en desarrollo
         * de todas las parcelas.
         * 
         * Esto es que establece la necesidad de agua de riego [mm/dia] de
         * la fecha actual de cada cultivo en desarrollo de cada una de
         * las parcelas. Esto se hace en funcion de la ETc del dia anterior
         * o de los dias anteriores a la fecha actual, del agua de lluvia
         * del dia anterior o de los dias anteriores a la fecha actual y del
         * agua de riego del dia anterior o de los dias anteriores a la fecha
         * actual.
         */
        for (PlantingRecord developingPlantingRecord : developingPlantingRecords) {
            givenParcel = developingPlantingRecord.getParcel();
            givenUser = developingPlantingRecord.getParcel().getUser();

            /*
             * Persiste pastDaysReference registros climaticos anteriores a la
             * fecha actual pertenecientes a una parcela dada que tiene
             * un cultivo sembrado y en desarrollo en la fecha actual. Estos
             * registros climaticos son obtenidos del servicio meteorologico
             * utilizado por la aplicacion.
             */
            requestPastClimateRecords(givenUser.getId(), givenUser.getOption(), developingPlantingRecord);

            /*
             * Calcula la ETo y la ETc de pastDaysReference registros climaticos
             * anteriores a la fecha actual pertenecientes a una parcela dada que
             * tiene un cultivo sembrado y en desarrollo en la fecha actual
             */
            calculateEtsPastClimateRecords(givenUser.getId(), givenUser.getOption(), developingPlantingRecord);

            /*
             * Calculo de la necesidad de agua de riego en la fecha actual
             * de un cultivo sembrado y en desarrollo en una parcela
             */
            currentIrrigationWaterNeed = calculateIrrigationWaterNeedCurrentDate(givenUser.getId(), givenParcel, givenUser.getOption());

            /*
             * Actualizacion de la necesidad de agua de riego del
             * registro de plantacion en desarrollo correspondiente
             * al cultivo en desarrollo en la fecha actual
             */
            plantingRecordService.updateIrrigationWaterNeed(developingPlantingRecord.getId(), givenParcel, String.valueOf(currentIrrigationWaterNeed));
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
     * @param userOption
     * @param developingPlantingRecord
     */
    private void requestPastClimateRecords(int userId, Option userOption, PlantingRecord developingPlantingRecord) {
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
         * ultimo riego registrado para una parcela en los ultimos
         * 30 dias, si el usuario activa la opcion de calcular la
         * necesidad de agua de riego de un cultivo en la fecha
         * actual a partir del ultimo riego registrado para una
         * parcela en los ultimos 30 dias. En caso de que exista
         * en la base de datos subyacente el ultimo riego registrado
         * para una parcela en los ultimos 30 dias, estas fechas
         * tambien se utilizan para obtener el registro de riego
         * correspondiente a dicho riego.
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
         * registrado para una parcela en los ultimos 30 dias, esta
         * activa, y existe dicho riego en la base de datos subyacente,
         * se utiliza la fecha del ultimo riego registrado como fecha
         * a partir de la cual obtener los registros climaticos del
         * pasado y se calcula la cantidad de dias pasados a utilizar
         * como referencia para obtener un registro climatico para
         * cada uno de ellos mediante la diferencia entre el numero
         * de dia en el año de la fecha del ultimo riego y el numero
         * de dia en el año de la fecha inmediatamente anterior a
         * la fecha actual
         */
        if (userOption.getThirtyDaysFlag() && irrigationRecordService.checkExistenceLastBetweenDates(userId, givenParcel.getId(), minorDate, majorDate)) {
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
         * registrado para una parcela en los ultimos 30 dias, NO
         * esta activa, o si NO existe el ultimo riego registrado
         * para una parcela en los ultimos 30 dias, en caso de que
         * dicha opcion este activa, se utiliza la cantidad de dias
         * pasados como referencia de las opciones del usuario para
         * obtener un registro climatico para cada uno de ellos y
         * se calcula la fecha pasada (es decir, anterior a la
         * fecha actual) a partir de la cual obtener los registros
         * climaticos mediante dicha cantidad
         */
        if (!userOption.getThirtyDaysFlag() || !irrigationRecordService.checkExistenceLastBetweenDates(userId, givenParcel.getId(), minorDate, majorDate)) {
            pastDaysReference = userOption.getPastDaysReference();
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
     * @param userOption
     * @param developingPlantingRecord
     */
    private void calculateEtsPastClimateRecords(int userId, Option userOption, PlantingRecord developingPlantingRecord) {
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
         * ultimo riego registrado para una parcela en los ultimos
         * 30 dias, si el usuario activa la opcion de calcular la
         * necesidad de agua de riego de un cultivo en la fecha
         * actual a partir del ultimo riego registrado para una
         * parcela en los ultimos 30 dias. En caso de que exista
         * en la base de datos subyacente el ultimo riego registrado
         * para una parcela en los ultimos 30 dias, estas fechas
         * tambien se utilizan para obtener el registro de riego
         * correspondiente a dicho riego.
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
         * registrado para una parcela en los ultimos 30 dias, esta
         * activa, y existe dicho riego en la base de datos subyacente,
         * se utiliza la fecha del ultimo riego registrado como fecha
         * a partir de la cual obtener los registros climaticos del
         * pasado y se calcula la cantidad de dias pasados a utilizar
         * como referencia para obtener un registro climatico para
         * cada uno de ellos mediante la diferencia entre el numero
         * de dia en el año de la fecha del ultimo riego y el numero
         * de dia en el año de la fecha inmediatamente anterior a
         * la fecha actual
         */
        if (userOption.getThirtyDaysFlag() && irrigationRecordService.checkExistenceLastBetweenDates(userId, givenParcel.getId(), minorDate, majorDate)) {
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
         * registrado para una parcela en los ultimos 30 dias, NO
         * esta activa, o si NO existe el ultimo riego registrado
         * para una parcela en los ultimos 30 dias, en caso de que
         * dicha opcion este activa, se utiliza la cantidad de dias
         * pasados como referencia de las opciones del usuario para
         * obtener un registro climatico para cada uno de ellos y
         * se calcula la fecha pasada (es decir, anterior a la
         * fecha actual) a partir de la cual obtener los registros
         * climaticos mediante dicha cantidad
         */
        if (!userOption.getThirtyDaysFlag() || !irrigationRecordService.checkExistenceLastBetweenDates(userId, givenParcel.getId(), minorDate, majorDate)) {
            pastDaysReference = userOption.getPastDaysReference();
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
     * @param givenParcel
     * @param userOption
     * @return double que representa la necesidad de agua de riego
     * de un cultivo en la fecha actual [mm/dia]
     */
    private double calculateIrrigationWaterNeedCurrentDate(int userId, Parcel givenParcel, Option userOption) {
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

        /*
         * Estas fechas son utilizadas para comprobar si existe el
         * ultimo riego registrado para una parcela en los ultimos
         * 30 dias, si el usuario activa la opcion de calcular la
         * necesidad de agua de riego de un cultivo en la fecha
         * actual a partir del ultimo riego registrado para una
         * parcela en los ultimos 30 dias. En caso de que exista
         * en la base de datos subyacente el ultimo riego registrado
         * para una parcela en los ultimos 30 dias, estas fechas
         * tambien se utilizan para obtener el registro de riego
         * correspondiente a dicho riego.
         */
        Calendar minorDate = UtilDate.getPastDateFromOffset(optionService.getValueThirtyDays());
        Calendar majorDate = UtilDate.getYesterdayDate();

        /*
         * Si la opcion de calcular la necesidad de agua de riego de
         * un cultivo en la fecha actual a partir del ultimo riego
         * registrado para una parcela en los ultimos 30 dias, esta
         * activa, y existe dicho riego en la base de datos subyacente,
         * se utiliza la fecha del ultimo riego registrado como fecha
         * a partir de la cual obtener los registros climaticos y
         * los registros de riego de una parcela dada, siendo todos
         * ellos previos a la fecha actual, ya que lo se que busca
         * con este metodo es calcular la necesidad de agua de riego
         * de un cultivo en la fecha actual [mm/dia]
         */
        if (userOption.getThirtyDaysFlag() && irrigationRecordService.checkExistenceLastBetweenDates(userId, givenParcel.getId(), minorDate, majorDate)) {
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
         * registrado para una parcela en los ultimos 30 dias, NO
         * esta activa, o si NO existe el ultimo riego registrado
         * para una parcela en los ultimos 30 dias, en caso de que
         * dicha opcion este activa, se utiliza la cantidad de dias
         * pasados como referencia de las opciones del usuario para
         * calcular la fecha pasada (es decir, anterior a la fecha
         * actual) a partir de la cual obtener los registros climaticos
         * y los registros de riego de una parcela dada, siendo
         * todos ellos previos a la fecha actual, ya que lo que se
         * busca con este metodo es calcular la necesidad de agua
         * de riego de un cultivo en la fecha actual [mm/dia]
         */
        if (!userOption.getThirtyDaysFlag() || !irrigationRecordService.checkExistenceLastBetweenDates(userId, givenParcel.getId(), minorDate, majorDate)) {
            dateFrom = UtilDate.getPastDateFromOffset(userOption.getPastDaysReference());
        }

        double totalIrrigationWaterCurrentDate = irrigationRecordService.calculateTotalIrrigationWaterCurrentDate(givenParcel);

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

        return WaterMath.calculateIrrigationWaterNeed(totalIrrigationWaterCurrentDate, climateRecords, irrigationRecords);
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

}