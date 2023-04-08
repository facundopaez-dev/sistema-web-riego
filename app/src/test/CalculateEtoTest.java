import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Ignore;
import et.ClassPenmanMonteithEtoTest;
import et.HargreavesEto;

public class CalculateEtoTest {

    @Test
    public void testOneCalculateEto() {
        System.out.println("************************ Prueba unitaria del metodo calculateEto de la clase ClassPenmanMonteithEtoTest ************************");
        System.out.println("En esta prueba unitaria se demuestra que el metodo calculateEto de la clase ClassPenmanMonteithEtoTest calcula correctamente la");
        System.out.println("evapotranspiracion del cultivo de referencia (ETo) haciendo uso del ejemplo de la pagina 72 del libro 'Evapotranspiracion del");
        System.out.println("cultivo' de la FAO (Organizacion de las Nacionas Unidas para la Alimentacion y la Agricultura, en español).");
        System.out.println();
        System.out.println("La evapotranspiracion del cultivo de referencia (ETo) es necesaria para calcular la cantidad de agua de riego que necesita un");
        System.out.println("cultivo. Mediante la formula de la evapotranspiracion del cultivo bajo condiciones estandar (ETc) se determina la cantidad de");
        System.out.println("agua de riego que necesita un cultivo: ETc = ETo * Kc (coeficiente de cultivo).");
        System.out.println();

        double maximumTemperature = 21.5;
        double minimumTemperature = 12.3;
        double pressure = 1001;
        double windSpeed = 10;
        double actualDurationSunstroke = 9.25;
        double maximumInsolation = 16.1;
        double extraterrestrialSolarRadiation = 41.09;
        double meanSaturationVaporPressure = 1.997;
        double realVaporPressure = 1.409;

        System.out.println("Determinacion de la ETo con los siguientes datos:");
        System.out.println("- Temperatura maxima: " + maximumTemperature + " °C");
        System.out.println("- Temperatura minima: " + minimumTemperature + " °C");
        System.out.println("- Presion atmosferica: " + pressure + " hPa");
        System.out.println("- Velocidad del viento medida a 10 m de altura: " + windSpeed + " km/h");
        System.out.println("- Insolacion real (n): " + actualDurationSunstroke + " horas");
        System.out.println("- Duracion maxima de la insolacion (N): " + maximumInsolation + " horas");
        System.out.println("- Radiacion solar extraterreste (Ra): " + extraterrestrialSolarRadiation + " MJ/m2/dia");
        System.out.println("- Presion media de vapor de la saturacion (es): " + meanSaturationVaporPressure + " [kPa]");
        System.out.println("- Presion real de vapor (ea): " + realVaporPressure + " [kPa]");
        System.out.println();

        /*
         * Seccion de prueba
         */
        double expectedResult = 3.88;
        double eto = ClassPenmanMonteithEtoTest.calculateEto(minimumTemperature, maximumTemperature, pressure,
                windSpeed,
                extraterrestrialSolarRadiation, maximumInsolation, actualDurationSunstroke, meanSaturationVaporPressure,
                realVaporPressure);

        System.out.println("* Seccion de prueba *");
        System.out.println("ETo [mm/dia] esperada: " + expectedResult);
        System.out.println("ETo [mm/dia] devuelta por el metodo calculateEto de la clase ClassPenmanMonteithEtoTest: " + eto);
        System.out.println();

        assertEquals(expectedResult, eto, 1e-2);

        System.out.println("- Prueba pasada satisfactoriamente");
    }

    @Test
    public void testTwoCalculateEto() {
        System.out.println("***************************** Prueba unitaria del metodo calculateEto de la clase HargreavesEto *****************************");
        System.out.println("En esta prueba unitaria se demuestra que el metodo calculateEto de la clase HargreavesEto calcula correctamente la");
        System.out.println("evapotranspiracion del cultivo de referencia (ETo).");
        System.out.println();
        System.out.println("La evapotranspiracion del cultivo de referencia (ETo) es necesaria para calcular la cantidad de agua de riego que necesita un");
        System.out.println("cultivo. Mediante la formula de la evapotranspiracion del cultivo bajo condiciones estandar (ETc) se determina la cantidad de");
        System.out.println("agua de riego que necesita un cultivo: ETc = ETo * Kc (coeficiente de cultivo).");
        System.out.println();

        double maximumTemperature = 29.8;
        double minimumTemperature = 18.3;
        double extraterrestrialSolarRadiation = 36.7;

        System.out.println("Determinacion de la ETo con los siguientes datos:");
        System.out.println("- Temperatura maxima (Tmax): " + maximumTemperature + " °C");
        System.out.println("- Temperatura minima (Tmin): " + minimumTemperature + " °C");
        System.out.println("- Radiacion solar extraterrestre (Ra): " + extraterrestrialSolarRadiation + " MJ/m2/dia");
        System.out.println();

        /*
         * Seccion de prueba
         */
        double expectedResult = 4.91;
        double eto = HargreavesEto.calculateEto(maximumTemperature, minimumTemperature, extraterrestrialSolarRadiation);

        System.out.println("* Seccion de prueba *");
        System.out.println("ETo [mm/dia] esperada: " + expectedResult);
        System.out.println("ETo [mm/dia] devuelta por el metodo calculateEto de la clase HargreavesEto: " + eto);
        System.out.println();

        assertEquals(expectedResult, eto, 1e-1);

        System.out.println("- Prueba pasada satisfactoriamente");
    }

}
