package irrigation;

import java.util.Calendar;
import java.util.Collection;
import java.lang.Math;
import model.ClimateRecord;
import model.IrrigationRecord;
import model.Crop;
import model.Soil;
import util.UtilDate;

public class WaterMath {

  /*
   * El metodo constructor tiene el modificador de acceso 'private'
   * para que ningun programador trate de instanciar esta clase
   * desde afuera, ya que todos los metodos publicos de la misma
   * son estaticos, con lo cual no se requiere una instancia de
   * esta clase para invocar a sus metodos publicos
   */
  private WaterMath() {

  }

  /**
   * @param totalIrrigationWaterGivenDate
   * @param accumulatedSoilMoistureDeficitPerDay
   * @return double que representa la necesidad de agua de
   * riego de un cultivo de una fecha [mm/dia].
   * 
   * La fecha para la que se calcula la necesidad de agua
   * de riego de un cultivo [mm/dia] depende de la fecha
   * de la cantidad total de agua de riego [mm/dia] y de
   * la fecha del acumulado del deficit de humedad por dia
   * [mm/dia], el cual debe ser de la fecha inmediatamente
   * anterior a la fecha de la cantidad total de agua de
   * riego si se quiere calcular la necesidad de agua de
   * riego de un cultivo en una fecha.
   * 
   * Para calcular la necesidad de agua de riego de un
   * cultivo en la fecha actual se debe utilizar la
   * cantidad total de agua de riego [mm/dia] de la
   * fecha actual (es decir, hoy) y el acumulado del
   * deficit de humedad por dia [mm/dia] de la fecha
   * inmediatamente a la fech actual.
   * 
   * Si este metodo se utiliza con la cantidad total de
   * agua de riego de ayer y el acumulado del deficit de
   * agua por dia de antes de ayer, la necesidad de agua
   * de riego de un cultivo calculada es de ayer.
   */
  public static double calculateIrrigationWaterNeed(double totalIrrigationWaterGivenDate, double accumulatedSoilMoistureDeficitPerDay) {

    /*
     * Si la suma entre la cantidad total de agua de riego de
     * una fecha [mm/dia] y el acumulado del deficit de humedad
     * por dia [mm/dia] de una fecha inmediatamente anterior
     * a la fecha de la cantidad total de agua de riego es
     * mayor o igual a cero, la cantidad total de humedad
     * evapotranspirada en dias previos a una fecha [mm/dia]
     * fue totalmente cubierta (satisfecha) en una fecha. Por
     * lo tanto, la necesidad de agua de riego de un cultivo
     * en una fecha es 0 [mm/dia].
     * 
     * El motivo por el cual se realiza la suma entre estos
     * dos valores es que el acumulado del deficit de humedad
     * por dia [mm/dia] de dias previos a una fecha es menor
     * o igual a cero.
     */
    if ((totalIrrigationWaterGivenDate + accumulatedSoilMoistureDeficitPerDay) >= 0) {
      return 0.0;
    }

    /*
     * Si la suma entre la cantidad total de agua de riego de
     * una fecha [mm/dia] y el acumulado del deficit de humedad
     * por dia [mm/dia] de una fecha inmediatamente anterior
     * a la fecha de la cantidad total de agua de riego es
     * estrictamente menor a cero, la cantidad total de agua
     * evaporada de dias previos a una fecha [mm/dia] NO
     * fue totalmente cubierta (satisfecha) en una fecha.
     * Por lo tanto, la necesidad de agua de riego de un
     * cultivo en una fecha [mm/dia] es el valor absoluto de
     * la suma entre estos dos valores.
     */
    return Math.abs(totalIrrigationWaterGivenDate + accumulatedSoilMoistureDeficitPerDay);
  }

  /**
   * Este metodo calcula el acumulado del deficit de humedad por dia [mm/dia]
   * de dias previos a una fecha sumando el deficit de humedad por dia
   * de cada uno de dichos dias. La fecha puede ser la fecha actual
   * (es decir, hoy) o una fecha posterior a la fecha actual. Tambien
   * puede ser una fecha del pasado (es decir, anterior a la fecha
   * actual), pero esto NO tiene sentido si lo que se busca es determinar
   * el acumulado del deficit de humedad por dia [mm/dia] de dias previos a
   * la fecha actual o a una fecha posterior a la fecha actual.
   * 
   * La fecha para la que se calcula el acumulado del deficit de humedad
   * por dia [mm/dia] de dias previos a una fecha, esta determinada por
   * los registros climaticos y los registros de riego que se seleccionan
   * como previos a una fecha dada, debiendo ser ambos grupos de
   * registros pertenecientes a una misma parcela.
   * 
   * Por ejemplo, si se seleccionan los registros climaticos y los
   * registros de riego de una parcela dada previos a la fecha actual
   * (es decir, hoy), el acumulado del deficit de humedad por dia [mm/dia]
   * de dias previos a una fecha calculado con estos registros
   * corresponde a la fecha actual. En cambio, si se seleccionan
   * los registros climaticos y los registros de riego de una parcela
   * dada previos a la fecha actual + X dias, donde X > 0, el acumulado
   * del deficit de humedad por dia [mm/dia] de dias previos a una fecha
   * corresponde a la fecha actual + X dias.
   * 
   * Se debe tener en cuenta que este metodo puede ser invocado con
   * registros climaticos y registros de riego previos a una fecha y
   * pertenecientes a una parcela que NO tiene un cultivo sembrado.
   * En caso de que ocurra esto, el acumulado del deficit de humedad
   * por dia [mm/dia] de dias previos a una fecha es de una parcela
   * en una fecha. En caso de que se invoque este metodo con registros
   * climaticos y registros de riego previos a una fecha y pertenecientes
   * a una parcela que tiene un cultivo sembrado en una fecha, el
   * acumulado del deficit de humedad por dia [mm/dia] de dias previos
   * a una fecha es de un cultivo en una fecha y representa la necesidad
   * de agua de riego de un cultivo en una fecha.
   * 
   * No se debe olvidar que la fecha para la que se calcula el acumulado
   * del deficit de humedad por dia [mm/dia] de dias previos a una fecha
   * esta determinada por los registros climaticos y los registros de
   * riego, debiendo ser todos ellos pertenecientes a una misma parcela,
   * que se seleccionan como previos a una fecha para realizar este
   * calculo.
   * 
   * @param climateRecords
   * @param irrigationRecords
   * @return double que representa el acumulado del deficit de humedad
   * por dia [mm/dias] de dias previos a una fecha calculado con un
   * conjunto de registros climaticos y un conjunto de registros de
   * riego, debiendo ser todos ellos previos a una fecha y pertenecientes
   * a una misma parcela. Si se invoca este metodo con registros
   * climaticos y registros de riego pertenecientes a una parcela
   * que tiene un cultivo sembrado en una fecha, double que representa
   * la necesidad de agua de riego de un cultivo en una fecha [mm/dia].
   * En caso contrario, double que representa el acumulado del deficit
   * de agua por dia [mm/dia] de dias previos a una fecha de una
   * parcela en una fecha.
   */
  public static double calculateAccumulatedWaterDeficitPerDay(Collection<ClimateRecord> climateRecords, Collection<IrrigationRecord> irrigationRecords) {
    double soilMoistureDeficitPerDay = 0.0;
    double accumulatedSoilMoistureDeficitPerDay = 0.0;

    /*
     * Acumula el deficit (falta) de agua por dia [mm/dia] de dias
     * previos a una fecha haciendo uso de un conjunto de registros
     * climaticos y un conjunto de registros de riego previos a una
     * fecha, debiendo ser todos ellos pertenecientes a una misma
     * parcela.
     * 
     * Si este metodo es invocado con registros climaticos y registros
     * de riego previos a una fecha pertenecientes a una misma parcela
     * que tiene un cultivo sembrado en una fecha, el valor devuelto
     * por el mismo es el acumulado del deficit de humedad por dia de
     * dias previos a una fecha de un cultivo en una fecha [mm/dia]
     * y representa la necesidad de agua de riego de un cultivo en una
     * fecha [mm/dia].
     */
    for (ClimateRecord currentClimateRecord : climateRecords) {
      /*
       * Calcula el deficit (falta) de agua por dia [mm/dia] en una
       * parcela en una fecha con un registro climatico y una coleccion
       * de registros de riego, y pertenece a una parcela y a un dia
       * porque un registro climatico y un registro de riego pertenecen
       * a una parcela y tienen una fecha (dia).
       */
      soilMoistureDeficitPerDay = calculateWaterDeficitPerDay(currentClimateRecord, irrigationRecords);

      /*
       * Acumula el deficit (falta) de agua por dia [mm/dia] de una
       * parcela en una fecha
       */
      accumulatedSoilMoistureDeficitPerDay = accumulateSoilMoistureDeficitPerDay(soilMoistureDeficitPerDay, accumulatedSoilMoistureDeficitPerDay);
    }

    return accumulatedSoilMoistureDeficitPerDay;
  }

  /**
   * @param etc
   * @param precipitation
   * @param totalAmountIrrigationWater
   * @return double que representa el deficit (falta) de humedad
   * de suelo por dia [mm/dia] en una fecha. Se dice que el deficit
   * de humedad de suelo por dia [mm/dia] es de una fecha porque la
   * ETc, la precipitacion y la cantidad total de agua de riego
   * pertenecen a una fecha
   */
  public static double calculateSoilMoistureDeficitPerDay(double etc, double precipitation, double totalAmountIrrigationWater) {
    /*
     * El deficit de humedad de suelo por dia [mm/dia] en una
     * fecha es la diferencia entre el agua provista (precipitacion
     * natural o precipitacion artificial, o ambas) [mm/dia] y
     * el agua evapotranspirada [mm/dia], la cual esta determinada
     * por la ETc (evapotranspiracion del cultivo bajo condiciones
     * estandar) [mm/dia]. Si el resultado de esta diferencia:
     * - es menor a cero significa que hay deficit (falta) de
     * humedad en el suelo [mm/dia], con lo cual hay una cantidad
     * de humedad del suelo que se debe cubrir (satisfacer) mediante
     * agua.
     * - es igual a cero significa que la cantidad de humedad
     * evapotranspirada fue cubierta (satisfecha) con una igual
     * cantidad de agua, ni agua de mas ni agua de menos.
     * - es mayor a cero significa que la cantidad de humedad
     * evapotranspirada fue cubierta (satisfecha) y que hay
     * escurrimiento de agua. En este caso, el resultado de la
     * diferencia es la cantidad de agua [mm/dia] que se escurre.
     * 
     * Solo para aclarar: estas condiciones ocurren en una
     * parcela en una fecha. La ETc, la precipitacion y la
     * cantidad total de agua de riego pertenecen a una fecha.
     */
    return ((precipitation + totalAmountIrrigationWater) - etc);
  }

  /**
   * @param climateRecord
   * @param irrigationRecords
   * @return double que representa el deficit (falta) de agua por dia
   * [mm/dia] de un cultivo en una fecha si se lo invoca con un registro
   * climatico y una coleccion de registros de riego pertenecientes a
   * una misma parcela que tiene un cultivo sembrado y en desarrollo
   * en una fecha. Se dice que el deficit de humedad por dia [mm/dia] de
   * un cultivo es de una fecha porque un registro climatico y un registro
   * de riego tienen una fecha (dia) y pertenecen a una parcela.
   */
  public static double calculateWaterDeficitPerDay(ClimateRecord climateRecord, Collection<IrrigationRecord> irrigationRecords) {
    /*
     * Obtiene la cantidad total de agua de riego utilizada en una
     * fecha para regar una parcela [mm/dia]. Si se invoca este metodo
     * con la fecha de un registro climatico y un conjunto de registros
     * de riego, con una fecha igual a la fecha del registro climatico,
     * siendo el registro climatico y el conjunto de registros de
     * riego pertenecientes a una misma parcela que tiene un cultivo
     * sembrado en una fecha, el valor devuelto por el mismo es la
     * cantidad total de agua de riego utilizada en una fecha para
     * regar un cultivo [mm/dia].
     * 
     * El motivo por el cual se habla de parcela y se usa la frase
     * "en una fecha" es que un registro de riego pertenece a una
     * parcela y tiene una fecha (dia).
     */
    double totalIrrigationWaterGivenDate = sumTotalAmountIrrigationWaterGivenDate(climateRecord.getDate(), irrigationRecords);

    /*
     * El deficit de humedad por dia [mm/dia] en una parcela en una fecha
     * (*) es la diferencia entre el agua provista (lluvia o riego, o
     * lluvia mas riego y viceversa) por dia [mm/dia] y el agua evaporada
     * por dia [mm/dia] (dada por la ETc [mm/dia] o la ETo [mm/dia]
     * si la ETc = 0) en una parcela en una fecha. Si el resultado de
     * esta diferencia es:
     * - es menor a cero significa que hay deficit (falta) de agua
     * [mm/dia], es decir, falta agua para cubrir (satisfacer) la
     * cantidad de agua evaporada.
     * - es igual a cero significa que la cantidad de agua evaporada
     * fue cubierta (satisfecha) con la cantidad exacta de agua, ni
     * agua de mas ni agua de menos.
     * - es mayor a cero significa que la cantidad de agua evaporada
     * fue cubierta (satisfecha) y que hay escurrimiento de agua. El
     * resultado de la diferencia es la cantidad de agua [mm/dia] que
     * se escurre.
     * 
     * Solo para aclarar: estas tres condiciones ocurren en una parcela
     * en una fecha.
     * 
     * (*) El motivo por el cual se usa la expresion "en una parcela
     * en una fecha" es que un registro climatico y un registro de
     * riego pertenecen a una parcela y tienen una fecha (dia).
     */
    return ((climateRecord.getPrecip() + totalIrrigationWaterGivenDate) - climateRecord.getEtc());
  }

  /**
   * @param deficitPerDay
   * @param accumulatedSoilMoistureDeficitPerDay
   * @return double que representa el acumulado del deficit (falta)
   * de humedad de suelo por dia [mm/dia]
   */
  public static double accumulateSoilMoistureDeficitPerDay(double deficitPerDay, double accumulatedSoilMoistureDeficitPerDay) {
    /*
     * Si el deficit de humedad de suelo por dia [mm/dia] es
     * negativo, se lo acumula, ya que esto es necesario para
     * determinar la necesidad de agua de riego de un cultivo en
     * una fecha en caso de que este metodo sea invocado para una
     * parcela que tiene un cultivo en desarrollo en una fecha.
     * 
     * Si el deficit de humedad de suelo por dia [mm/dia] es positivo
     * y el acumulado del deficit de humedad de suelo por dia [mm/dia]
     * es negativo, significa:
     * - que la cantidad de humedad perdida del suelo en un dia
     * esta totalmente cubierta (satisfecha) y que hay una cantidad
     * extra de agua [mm/dia], y
     * - que toda la humedad perdida del suelo en un conjunto de
     * dias previos al dia correspondiente del deficit de humedad
     * por dia, NO esta cubierta (satisfecha). En esta situacion
     * hay lugar en el suelo para almacenar agua. Por lo tanto, la
     * cantidad extra de agua del dia correspondiente del deficit
     * de humedad por dia, se almacena en el suelo.
     * 
     * En esta instruccion se concentran estas dos condiciones.
     */
    accumulatedSoilMoistureDeficitPerDay = accumulatedSoilMoistureDeficitPerDay + deficitPerDay;

    /*
     * Si el acumulado del deficit de humedad por dia [mm/dia]
     * de dias previos a una fecha es estrictamente mayor a cero,
     * significa que toda la perdida de humedad del suelo en dias
     * previos al dia correspondiente del deficit de humedad por
     * dia, fue totalmente cubierta (satisfecha) mediante preciptacion
     * (artificial y/o natural) en el dia de dicho deficit. En
     * consecuencia, el acumulado del deficit de humedad por dia,
     * de dias previos a una fecha, es 0 [mm/dia]. Esto significa
     * que en el dia correspondiente al deficit de humedad por dia,
     * el nivel de humedad del suelo esta en capacidad de campo,
     * lo cual significa que el suelo esta en capacidad de campo
     * (capacidad de campo es la condicion en la que el suelo esta
     * lleno de agua o en su maxima capacidad de almacenamiento de
     * agua, pero no anegado). Por lo tanto, en el dia del deficit
     * de humedad por dia NO hay una cantidad acumulada de perdida
     * de humedad del suelo que cubrir (satisfacer) mediante
     * precipitacion (artificial y/o natural).
     * 
     * Si el acumulado del deficit de humedad por dia de dias
     * previos a una fecha [mm/dia] es igual a cero, se esta en
     * la misma situacion. Por lo tanto, en el dia correspondiente
     * al deficit de humedad por dia, el nivel de humedad del
     * suelo esta en capacidad de campo, lo cual significa que
     * el suelo esta en capacidad de campo.
     * 
     * El metodo calculateNegativeOptimalIrrigationLayer(),
     * escrito en esta clase, tiene una explicacion de lo que
     * es la capacidad de campo.
     */
    if (accumulatedSoilMoistureDeficitPerDay > 0) {
      accumulatedSoilMoistureDeficitPerDay = 0;
    }

    return accumulatedSoilMoistureDeficitPerDay;
  }

  /**
   * @param precipPerDay
   * @param totalAmountIrrigationWaterPerDay
   * @return double que representa la cantidad total de agua
   * provista por dia [mm/dia] como resultado de la suma entre
   * la precipiacion por dia [mm/dia] y el agua de riego por
   * dia [mm/dia]
   */
  public static double calculateWaterProvidedPerDay(double precipPerDay, double totalAmountIrrigationWaterPerDay) {
    return precipPerDay + totalAmountIrrigationWaterPerDay;
  }

  /**
   * @param date
   * @param irrigationRecords
   * @return double que representa la cantidad total de agua de
   * riego utilizada en una fecha para regar una parcela [mm/dia].
   * Si se invoca este metodo con una coleccion de registros de
   * riego pertenecientes a una misma parcela que tiene un cultivo
   * sembrado en una fecha, double que representa la cantidad
   * total de agua de riego utilizada en una fecha para regar un
   * cultivo [mm/dia].
   */
  public static double sumTotalAmountIrrigationWaterGivenDate(Calendar date, Collection<IrrigationRecord> irrigationRecords) {
    double totalIrrigationWaterGivenDate = 0.0;

    for (IrrigationRecord currentIrrigationRecord : irrigationRecords) {

      /*
       * Acumula el agua de riego de todos los registros de riego
       * pertenecientes a una parcela que tienen la misma fecha.
       * De esta manera, se calcula la cantidad total de agua de
       * riego utilizada en una fecha para regar una parcela [mm/dia].
       * Si se invoca este metodo con una coleccion de registros
       * de riego pertenecientes a una misma parcela que tiene un
       * cultivo sembrado en una fecha, calcula la cantidad total
       * de agua de riego utilizada en una fecha para regar un
       * cultivo [mm/dia].
       */
      if (UtilDate.compareTo(currentIrrigationRecord.getDate(), date) == 0) {
        totalIrrigationWaterGivenDate = totalIrrigationWaterGivenDate + currentIrrigationRecord.getIrrigationDone();
      }

    }

    return totalIrrigationWaterGivenDate;
  }

  /**
   * La formula ((Wc - Wm) / 100) * pea * D (lamina total de agua
   * disponible (dt) [mm]) representa la capacidad de almacenamiento
   * de agua que tiene un suelo, la cual esta en funcion del suelo
   * (capacidad de campo, punto de marchitez permanente, peso
   * especifico aparente) y de la profundidad de las raices del
   * cultivo.
   * 
   * Wc = Capacidad de campo [gr/gr]
   * Wm = Punto de marchitez permanente [gr/gr]
   * pea = Peso especifico aparente [gr/cm3]
   * D = Profundidad radicular [m]
   * 
   * La unidad de medida [gr/gr] representa la cantidad de gramos
   * de agua que hay cada 100 gramos de tierra seca. Por ejemplo,
   * un suelo arenoso tiene una capacidad de campo de 9 [gr/gr], lo
   * cual indica que en capacidad de campo un suelo arenoso tiene
   * 9 gramos de agua cada 100 gramos de tierra seca. Esto indica
   * que un suelo arenoso tiene poca retencion de agua. Lo que
   * indica la textura del suelo es capacidad de retencion de agua
   * que tiene un suelo. La textura de un suelo se determina
   * mediante el triangulo textural.
   * 
   * @param crop
   * @param soil
   * @return double que representa la cantidad de agua que puede
   * retener un suelo en el volumen determinado por los valores
   * de un suelo (capacidad de campo, punto de marchitez permanente,
   * peso especifico aparente) y la profundidad de las raices de
   * un cultivo. Esto es la lamina total de agua disponible (dt)
   * [mm].
   */
  public static double calculateTotalAmountWaterAvailable(Crop crop, Soil soil) {
    // Capacidad de campo de un suelo
    double wc = soil.getFieldCapacity();

    // Punto de marchitez permanente de un suelo
    double wm = soil.getPermanentWiltingPoint();

    // Peso especifico aparente de un suelo
    double pea = soil.getApparentSpecificWeight();

    /*
     * La profundidad radicular promedio de un cultivo esta
     * medida en metros porque los limites del rango de la
     * profundidad radicular de un cultivo estan medidos
     * en metros. Debido a que la ETc (evapotranspiracion
     * del cultivo bajo condiciones estandar) esta medida
     * en mm/dia, se debe convertir la profundidad radicular
     * promedio de un cultivo de metros a milimetros, lo cual
     * se realiza multiplicandola por 1000.
     * 
     * La ETc, junto con el agua provista (lluvia o riego, o
     * lluvia mas riego y viceversa) [mm/dia], se utiliza para
     * calcular la necesidad de agua de riego de un cultivo en
     * una fecha [mm/dia].
     */
    double averageRoothDepth = calculateAverageRootDepth(crop) * 1000;

    return ((wc - wm) / 100) * pea * averageRoothDepth;
  }

  /**
   * La formula ((Wc - Wm) / 100) * pea * D * p (lamina de riego
   * optima (drop) [mm]) representa la cantidad maxima de agua que
   * puede perder un suelo lleno de agua, pero no anegado (esto es
   * que el nivel de humedad del suelo esta en capacidad de campo),
   * que tiene un cultivo sembrado, a partir de la cual NO conviene
   * que pierda mas agua, sino que se le debe añadir agua para llenarlo,
   * pero sin anegarlo (esto es llevar el nivel de humedad del suelo
   * a capacidad de campo). Debido a lo que representa la lamina
   * de riego optima, tambien se la denomina umbral de riego.
   * 
   * La lamina de riego optima esta en funcion del suelo (capacidad
   * de campo, punto de marchitez permanente, peso especifico aparente)
   * y de la naturaleza del cultivo (profundidad de las raices, factor
   * de agotamiento).
   * 
   * Wc = Capacidad de campo [gr/gr]
   * Wm = Punto de marchitez permanente [gr/gr]
   * pea = Peso especifico aparente [gr/cm3]
   * D = Profundidad radicular [m]
   * p = Fraccion agotamiento de la humedad en el suelo para un cultivo
   * (adimensional)
   * 
   * La unidad de medida [gr/gr] representa la cantidad de gramos
   * de agua que hay cada 100 de tierra seca. Por ejemplo, un
   * suelo arenoso tiene una capacidad de campo de 9 [gr/gr], lo
   * cual indica que en capacidad de campo un suelo arenoso tiene
   * 9 gramos de agua cada 100 gramos de tierra seca. Esto indica
   * que un suelo arenoso tiene poca retencion de agua. Lo que
   * indica la textura del suelo es capacidad de retencion de agua
   * que tiene un suelo. La textura de un suelo se determina mediante
   * el triangulo textural.
   * 
   * Un valor de 0,50 para el fraccion de agotamiento de la humedad en
   * el suelo para un cultivo (depletionFactor), representado con la
   * letra p en la formula de la lamina de riego optima (drop), es
   * utilizado comunmente para una gran variedad de cultivos.
   * 
   * Por ejemplo, si el resultado de aplicar dicha formula es 10 [mm]
   * significa que un suelo lleno de agua, pero no anegado (esto es
   * que el nivel de humedad del suelo esta en capacidad de campo),
   * que tiene un cultivo sembrado, puede perder como maximo 10 [mm]
   * de agua y NO conviene que pierda mas de esa cantidad. Por lo
   * tanto, cuando el nivel de humedad del suelo descienda (perdida
   * de humedad) a los 10 [mm] se le debe añadir agua para llevar su
   * nivel de humedad a capacidad de campo, es decir, se le debe
   * añadir agua al suelo hasta la capacidad de campo.
   * 
   * @param crop
   * @param soil
   * @return double que representa la cantidad maxima de agua que
   * puede perder un suelo lleno de agua, pero no anegado (esto es
   * que el nivel de humedad del suelo esta en capacidad de campo),
   * que tiene un cultivo sembrado, medida en [mm]. Esto es la lamina
   * de riego optima (drop) [mm].
   */
  public static double calculateOptimalIrrigationLayer(Crop crop, Soil soil) {
    return calculateTotalAmountWaterAvailable(crop, soil) * crop.getDepletionFraction();
  }

  /**
   * A la lamina de riego optima (drop) se le asigna el signo
   * negativo (-) para poder compararla con el acumulado del
   * deficit de humedad por dia [mm/dia] (*), el cual es negativo
   * y es calculado desde la fecha de siembra de un cultivo
   * hasta la fecha inmediatamente anterior a la fecha actual.
   * La lamina de riego optima representa la cantidad maxima
   * de agua que puede perder un suelo para el cultivo que
   * tiene sembrado, a partir de la cual NO conviene que pierda
   * mas agua, sino que se le debe añadir agua hasta llevar
   * su nivel de humedad a capacidad de campo. Capacidad de
   * campo es la capacidad de almacenamiento de agua que tiene
   * un suelo. Un suelo que esta en capacidad de campo es un
   * suelo lleno de agua, pero no anegado. El motivo por el
   * cual se habla de llevar el nivel de humedad del suelo,
   * que tiene un cultivo sembrado, a capacidad de campo es
   * que el objetivo de la aplicacion es informar al usuario
   * la cantidad de agua que debe reponer en la fecha actual
   * (es decir, hoy) para llevar el nivel de humedad del suelo,
   * en el que tiene un cultivo sembrado, a capacidad de campo.
   * Esto es la cantidad de agua de riego [mm] que debe usar
   * el usuario para llenar el suelo en el que tiene un cultivo
   * sembrado, pero sin anegarlo.
   * 
   * El suelo agricola tiene dos limites: capacidad de campo
   * (limite superior) y punto de marchitez permanente (limite
   * inferior). La lamina de riego optima tambien se la conoce
   * como umbral de riego, debido a lo que representa.
   * 
   * (*) El motivo de esta comparacion es determinar la necesidad
   * de agua de riego de un cultivo en la fecha actual (es decir,
   * hoy) [mm/dia]. El acumulado del deficit de humedad por dia
   * [mm/dia] no se compara unicamente con la lamina de riego
   * optima (drop) [mm] a la hora de calcular dicha necesidad.
   * Para calcular la necesidad de agua de riego de un cultivo
   * en la fecha actual (es decir, hoy) [mm/dia] se compara el
   * acumulado del deficit de humedad por dia [mm/dia], calculado
   * desde la fecha de siembra de un cultivo hasta la fecha
   * inmediatamente anterior a la fecha actual, con la capacidad
   * de campo [mm], la lamina de riego optima (umbral de riego)
   * [mm], la capacidad de almacenamiento de agua del suelo (lamina
   * total de agua disponible (dt) [mm]) y el doble de la capacidad
   * de almacenamiento de agua del suelo [mm].
   * 
   * @param crop
   * @param soil
   * @return double negativo de la cantidad maxima de agua que
   * puede perder un suelo lleno de agua, pero no anegado (esto
   * es que el nivel de humedad del suelo esta en capacidad de
   * campo), que tiene un cultivo sembrado, medida en [mm]. Esto
   * es la lamina de riego optima (drop).
   */
  public static double calculateNegativeOptimalIrrigationLayer(Crop crop, Soil soil) {
    return -1 * calculateOptimalIrrigationLayer(crop, soil);
  }

  /**
   * @param crop
   * @return double que representa el promedio de la
   * profundidad radicular de un cultivo medida en
   * metros
   */
  private static double calculateAverageRootDepth(Crop crop) {
    return (crop.getUpperLimitMaximumRootDepth() + crop.getLowerLimitMaximumRootDepth()) / 2;
  }

  /**
   * Limita la cantidad de decimales de un numero de punto
   * flotante a dos decimales
   * 
   * @param number
   * @return punto flotante con dos decimales
   */
  private static double limitToTwoDecimalPlaces(double number) {
    return (double) Math.round(number * 100d) / 100d;
  }

}
