import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Ignore;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import stateless.CropServiceBean;
import model.Crop;
import model.Month;
import model.Region;

public class CropServiceBeanTest {

    private static EntityManager entityManager;
    private static EntityManagerFactory entityManagerFactory;
    private static CropServiceBean cropService;

    @BeforeClass
    public static void preTest() {
        entityManagerFactory = Persistence.createEntityManagerFactory("swcar");
        entityManager = entityManagerFactory.createEntityManager();

        cropService = new CropServiceBean();
        cropService.setEntityManager(entityManager);
    }

    @Test
    public void testOneEquals() {
        System.out.println("************************************************ Prueba uno del metodo equals ************************************************");
        printDescriptionMethodToBeTested();

        System.out.println("# Descripcion de la prueba unitaria");
        System.out.println("En esta prueba unitaria se utilizan dos cultivos que tienen el mismo nombre y que NO tienen definido el mes de inicio de");
        System.out.println("siembra, el mes de fin de siembra y la region, para demostrar el correcto funcionamiento del metodo equals en el primer");
        System.out.println("caso.");
        System.out.println();

        /*
         * Creacion de los cultivos de prueba
         */
        String cropNameOne = "Lechuga";
        String cropNameTwo = "Lechuga";

        Crop cropOne = new Crop();
        cropOne.setName(cropNameOne);

        Crop cropTwo = new Crop();
        cropTwo.setName(cropNameTwo);

        System.out.println("# Seccion de prueba");
        System.out.println("Cultivos de prueba para el metodo equals:");
        System.out.println("Cultivo 1");
        printCrop(cropOne);
        System.out.println("Cultivo 2");
        printCrop(cropTwo);

        /*
         * Seccion de prueba
         */
        boolean expectedResult = true;
        boolean result = cropService.equals(cropOne, cropTwo);

        System.out.println("* Resultado esperado: " + expectedResult);
        System.out.println("* Resultado devuelto por equals: " + result);
        System.out.println();

        assertTrue(result);

        System.out.println("* Prueba pasada satisfactoriamente *");
        System.out.println();
    }

    @Test
    public void testTwoEquals() {
        System.out.println("************************************************ Prueba dos del metodo equals ************************************************");
        printDescriptionMethodToBeTested();

        System.out.println("# Descripcion de la prueba unitaria");
        System.out.println("En esta prueba unitaria se utilizan dos cultivos que tienen el mismo nombre y el mismo mes de inicio de siembra, y que NO");
        System.out.println("tiene definido el mes de fin de siembra y la region, para demostrar el correcto funcionamiento del metodo equals en el");
        System.out.println("segundo caso.");
        System.out.println();

        /*
         * Creacion de los cultivos de prueba
         */
        String cropNameOne = "Cebolla";
        String cropNameTwo = "Cebolla";

        String nameMonthOne = "Enero";
        String nameMonthTwo = "Enero";

        Month plantingStartMonthOne = new Month();
        plantingStartMonthOne.setName(nameMonthOne);

        Month plantingStartMonthTwo = new Month();
        plantingStartMonthTwo.setName(nameMonthTwo);

        Crop cropOne = new Crop();
        cropOne.setName(cropNameOne);
        cropOne.setPlantingStartMonth(plantingStartMonthOne);

        Crop cropTwo = new Crop();
        cropTwo.setName(cropNameTwo);
        cropTwo.setPlantingStartMonth(plantingStartMonthTwo);

        System.out.println("# Seccion de prueba");
        System.out.println("Cultivos de prueba para el metodo equals:");
        System.out.println("Cultivo 1");
        printCrop(cropOne);
        System.out.println("Cultivo 2");
        printCrop(cropTwo);

        /*
         * Seccion de prueba
         */
        boolean expectedResult = true;
        boolean result = cropService.equals(cropOne, cropTwo);

        System.out.println("* Resultado esperado: " + expectedResult);
        System.out.println("* Resultado devuelto por equals: " + result);
        System.out.println();

        assertTrue(result);

        System.out.println("* Prueba pasada satisfactoriamente *");
        System.out.println();
    }

    @Test
    public void testThreeEquals() {
        System.out.println("************************************************ Prueba tres del metodo equals ************************************************");
        printDescriptionMethodToBeTested();

        System.out.println("# Descripcion de la prueba unitaria");
        System.out.println("En esta prueba unitaria se utilizan dos cultivos que tienen el mismo nombre y el mismo mes de fin de siembra, y que NO tienen");
        System.out.println("definido el mes de inicio de siembra y la region, para demostrar el correcto funcionamiento del metodo equals en el tercer");
        System.out.println("caso.");
        System.out.println();

        /*
         * Creacion de los cultivos de prueba
         */
        String cropNameOne = "Papa";
        String cropNameTwo = "Papa";

        String nameMonthOne = "Diciembre";
        String nameMonthTwo = "Diciembre";

        Month endPlantingMonthOne = new Month();
        endPlantingMonthOne.setName(nameMonthOne);

        Month endPlantingMonthTwo = new Month();
        endPlantingMonthTwo.setName(nameMonthTwo);

        Crop cropOne = new Crop();
        cropOne.setName(cropNameOne);
        cropOne.setEndPlantingMonth(endPlantingMonthOne);

        Crop cropTwo = new Crop();
        cropTwo.setName(cropNameTwo);
        cropTwo.setEndPlantingMonth(endPlantingMonthTwo);

        System.out.println("# Seccion de prueba");
        System.out.println("Cultivos de prueba para el metodo equals:");
        System.out.println("Cultivo 1");
        printCrop(cropOne);
        System.out.println("Cultivo 2");
        printCrop(cropTwo);

        /*
         * Seccion de prueba
         */
        boolean expectedResult = true;
        boolean result = cropService.equals(cropOne, cropTwo);

        System.out.println("* Resultado esperado: " + expectedResult);
        System.out.println("* Resultado devuelto por equals: " + result);
        System.out.println();

        assertTrue(result);

        System.out.println("* Prueba pasada satisfactoriamente *");
        System.out.println();
    }

    @Test
    public void testFourEquals() {
        System.out.println("************************************************ Prueba cuatro del metodo equals ************************************************");
        printDescriptionMethodToBeTested();

        System.out.println("# Descripcion de la prueba unitaria");
        System.out.println("En esta prueba unitaria se utilizan dos cultivos que tienen el mismo nombre y la misma region, y que NO tienen definido el");
        System.out.println("mes de inicio de siembra y el mes de fin de siembra, para demostrar el correcto funcionamiento del metodo equals en el cuarto");
        System.out.println("caso.");
        System.out.println();

        /*
         * Creacion de los cultivos de prueba
         */
        String cropNameOne = "Papa";
        String cropNameTwo = "Papa";

        String nameRegionOne = "California";
        String nameRegionTwo = "California";

        Region regionOne = new Region();
        regionOne.setName(nameRegionOne);

        Region regionTwo = new Region();
        regionTwo.setName(nameRegionTwo);

        Crop cropOne = new Crop();
        cropOne.setName(cropNameOne);
        cropOne.setRegion(regionOne);

        Crop cropTwo = new Crop();
        cropTwo.setName(cropNameTwo);
        cropTwo.setRegion(regionTwo);

        System.out.println("# Seccion de prueba");
        System.out.println("Cultivos de prueba para el metodo equals:");
        System.out.println("Cultivo 1");
        printCrop(cropOne);
        System.out.println("Cultivo 2");
        printCrop(cropTwo);

        /*
         * Seccion de prueba
         */
        boolean expectedResult = true;
        boolean result = cropService.equals(cropOne, cropTwo);

        System.out.println("* Resultado esperado: " + expectedResult);
        System.out.println("* Resultado devuelto por equals: " + result);
        System.out.println();

        assertTrue(result);

        System.out.println("* Prueba pasada satisfactoriamente *");
        System.out.println();
    }

    @Test
    public void testFiveEquals() {
        System.out.println("************************************************ Prueba cinco del metodo equals ************************************************");
        printDescriptionMethodToBeTested();

        System.out.println("# Descripcion de la prueba unitaria");
        System.out.println("En esta prueba unitaria se utilizan dos cultivos que tienen el mismo nombre, el mismo mes de inicio de siembra y el mismo");
        System.out.println("mes de fin de siembra, y que NO tienen definida la region, para demostrar el correcto funcionamiento del metodo equals en el");
        System.out.println("quinto caso.");
        System.out.println();

        /*
         * Creacion de los nombres para los cultivos de prueba
         */
        String cropNameOne = "Zanahoria";
        String cropNameTwo = "Zanahoria";

        /*
         * Creacion del mes de inicio de siembra y del mes de fin
         * de siembra para los cultivos de prueba
         */
        String namePlantingStartMonthOne = "Enero";
        String namePlantingStartMonthTwo = "Enero";

        Month plantingStartMonthOne = new Month();
        plantingStartMonthOne.setName(namePlantingStartMonthOne);

        Month plantingStartMonthTwo = new Month();
        plantingStartMonthTwo.setName(namePlantingStartMonthTwo);

        String nameEndPlantingMonthOne = "Diciembre";
        String nameEndPlantingMonthTwo = "Diciembre";

        Month endPlantingMonthOne = new Month();
        endPlantingMonthOne.setName(nameEndPlantingMonthOne);

        Month endPlantingMonthTwo = new Month();
        endPlantingMonthTwo.setName(nameEndPlantingMonthTwo);

        /*
         * Creacion de los cultivos de prueba
         */
        Crop cropOne = new Crop();
        cropOne.setName(cropNameOne);
        cropOne.setPlantingStartMonth(plantingStartMonthOne);
        cropOne.setEndPlantingMonth(endPlantingMonthOne);

        Crop cropTwo = new Crop();
        cropTwo.setName(cropNameTwo);
        cropTwo.setPlantingStartMonth(plantingStartMonthTwo);
        cropTwo.setEndPlantingMonth(endPlantingMonthTwo);

        System.out.println("# Seccion de prueba");
        System.out.println("Cultivos de prueba para el metodo equals:");
        System.out.println("Cultivo 1");
        printCrop(cropOne);
        System.out.println("Cultivo 2");
        printCrop(cropTwo);

        /*
         * Seccion de prueba
         */
        boolean expectedResult = true;
        boolean result = cropService.equals(cropOne, cropTwo);

        System.out.println("* Resultado esperado: " + expectedResult);
        System.out.println("* Resultado devuelto por equals: " + result);
        System.out.println();

        assertTrue(result);

        System.out.println("* Prueba pasada satisfactoriamente *");
        System.out.println();
    }

    @Test
    public void testSixEquals() {
        System.out.println("************************************************ Prueba seis del metodo equals ************************************************");
        printDescriptionMethodToBeTested();

        System.out.println("# Descripcion de la prueba unitaria");
        System.out.println("En esta prueba unitaria se utilizan dos cultivos que tienen el mismo nombre, el mismo mes de inicio de siembra y la misma");
        System.out.println("region, y que NO tienen definido el mes de fin de siembra, para demostrar el correcto funcionamiento del metodo equals en");
        System.out.println("el sexto caso.");
        System.out.println();

        /*
         * Creacion de los nombres para los cultivos de prueba
         */
        String cropNameOne = "Remolacha";
        String cropNameTwo = "Remolacha";

        /*
         * Creacion del mes de inicio de siembra para los cultivos
         * de prueba
         */
        String namePlantingStartMonthOne = "Enero";
        String namePlantingStartMonthTwo = "Enero";

        Month plantingStartMonthOne = new Month();
        plantingStartMonthOne.setName(namePlantingStartMonthOne);

        Month plantingStartMonthTwo = new Month();
        plantingStartMonthTwo.setName(namePlantingStartMonthTwo);

        /*
         * Creacion de las regiones para los cultivos de prueba
         */
        String nameRegionOne = "Región árida";
        String nameRegionTwo = "Región árida";

        Region regionOne = new Region();
        regionOne.setName(nameRegionOne);

        Region regionTwo = new Region();
        regionTwo.setName(nameRegionTwo);

        /*
         * Creacion de los cultivos de prueba
         */
        Crop cropOne = new Crop();
        cropOne.setName(cropNameOne);
        cropOne.setPlantingStartMonth(plantingStartMonthOne);
        cropOne.setRegion(regionOne);

        Crop cropTwo = new Crop();
        cropTwo.setName(cropNameTwo);
        cropTwo.setPlantingStartMonth(plantingStartMonthTwo);
        cropTwo.setRegion(regionTwo);

        System.out.println("# Seccion de prueba");
        System.out.println("Cultivos de prueba para el metodo equals:");
        System.out.println("Cultivo 1");
        printCrop(cropOne);
        System.out.println("Cultivo 2");
        printCrop(cropTwo);

        /*
         * Seccion de prueba
         */
        boolean expectedResult = true;
        boolean result = cropService.equals(cropOne, cropTwo);

        System.out.println("* Resultado esperado: " + expectedResult);
        System.out.println("* Resultado devuelto por equals: " + result);
        System.out.println();

        assertTrue(result);

        System.out.println("* Prueba pasada satisfactoriamente *");
        System.out.println();
    }

    @Test
    public void testSevenEquals() {
        System.out.println("************************************************ Prueba siete del metodo equals ************************************************");
        printDescriptionMethodToBeTested();

        System.out.println("# Descripcion de la prueba unitaria");
        System.out.println("En esta prueba unitaria se utilizan dos cultivos que tienen el mismo nombre, el mismo mes de fin de siembra y la misma");
        System.out.println("region, y que NO tienen definido el mes de inicio de siembra, para demostrar el correcto funcionamiento del metodo equals");
        System.out.println("en el septimo caso.");
        System.out.println();

        /*
         * Creacion de los nombres para los cultivos de prueba
         */
        String cropNameOne = "Calabaza";
        String cropNameTwo = "Calabaza";

        /*
         * Creacion del mes de fin de siembra para los cultivos
         * de prueba
         */
        String nameEndPlantingMonthOne = "Diciembre";
        String nameEndPlantingMonthTwo = "Diciembre";

        Month endPlantingMonthOne = new Month();
        endPlantingMonthOne.setName(nameEndPlantingMonthOne);

        Month endPlantingMonthTwo = new Month();
        endPlantingMonthTwo.setName(nameEndPlantingMonthTwo);

        /*
         * Creacion de las regiones para los cultivos de prueba
         */
        String nameRegionOne = "Mediterráneo";
        String nameRegionTwo = "Mediterráneo";

        Region regionOne = new Region();
        regionOne.setName(nameRegionOne);

        Region regionTwo = new Region();
        regionTwo.setName(nameRegionTwo);

        /*
         * Creacion de los cultivos de prueba
         */
        Crop cropOne = new Crop();
        cropOne.setName(cropNameOne);
        cropOne.setEndPlantingMonth(endPlantingMonthOne);
        cropOne.setRegion(regionOne);

        Crop cropTwo = new Crop();
        cropTwo.setName(cropNameTwo);
        cropTwo.setEndPlantingMonth(endPlantingMonthTwo);
        cropTwo.setRegion(regionTwo);

        System.out.println("# Seccion de prueba");
        System.out.println("Cultivos de prueba para el metodo equals:");
        System.out.println("Cultivo 1");
        printCrop(cropOne);
        System.out.println("Cultivo 2");
        printCrop(cropTwo);

        /*
         * Seccion de prueba
         */
        boolean expectedResult = true;
        boolean result = cropService.equals(cropOne, cropTwo);

        System.out.println("* Resultado esperado: " + expectedResult);
        System.out.println("* Resultado devuelto por equals: " + result);
        System.out.println();

        assertTrue(result);

        System.out.println("* Prueba pasada satisfactoriamente *");
        System.out.println();
    }

    @Test
    public void testEightEquals() {
        System.out.println("************************************************ Prueba ocho del metodo equals ************************************************");
        printDescriptionMethodToBeTested();

        System.out.println("# Descripcion de la prueba unitaria");
        System.out.println("En esta prueba unitaria se utilizan dos cultivos que tienen el mismo nombre, el mismo mes de inicio de siembra, el mismo mes");
        System.out.println("de fin de siembra y la misma region para demostrar el correcto funcionamiento del metodo equals en el octavo caso.");
        System.out.println();

        /*
         * Creacion de los nombres para los cultivos de prueba
         */
        String cropNameOne = "Repollo";
        String cropNameTwo = "Repollo";

        /*
         * Creacion del mes de inicio de siembra y del mes de
         * fin de siembra para los cultivos de prueba
         */
        String namePlantingStartMonthOne = "Enero";
        String namePlantingStartMonthTwo = "Enero";

        String nameEndPlantingMonthOne = "Diciembre";
        String nameEndPlantingMonthTwo = "Diciembre";

        Month plantingStartMonthOne = new Month();
        plantingStartMonthOne.setName(namePlantingStartMonthOne);

        Month plantingStartMonthTwo = new Month();
        plantingStartMonthTwo.setName(namePlantingStartMonthTwo);

        Month endPlantingMonthOne = new Month();
        endPlantingMonthOne.setName(nameEndPlantingMonthOne);

        Month endPlantingMonthTwo = new Month();
        endPlantingMonthTwo.setName(nameEndPlantingMonthTwo);

        /*
         * Creacion de las regiones para los cultivos de prueba
         */
        String nameRegionOne = "Europa";
        String nameRegionTwo = "Europa";

        Region regionOne = new Region();
        regionOne.setName(nameRegionOne);

        Region regionTwo = new Region();
        regionTwo.setName(nameRegionTwo);

        /*
         * Creacion de los cultivos de prueba
         */
        Crop cropOne = new Crop();
        cropOne.setName(cropNameOne);
        cropOne.setPlantingStartMonth(plantingStartMonthOne);
        cropOne.setEndPlantingMonth(endPlantingMonthOne);
        cropOne.setRegion(regionOne);

        Crop cropTwo = new Crop();
        cropTwo.setName(cropNameTwo);
        cropTwo.setPlantingStartMonth(plantingStartMonthTwo);
        cropTwo.setEndPlantingMonth(endPlantingMonthTwo);
        cropTwo.setRegion(regionTwo);

        System.out.println("# Seccion de prueba");
        System.out.println("Cultivos de prueba para el metodo equals:");
        System.out.println("Cultivo 1");
        printCrop(cropOne);
        System.out.println("Cultivo 2");
        printCrop(cropTwo);

        /*
         * Seccion de prueba
         */
        boolean expectedResult = true;
        boolean result = cropService.equals(cropOne, cropTwo);

        System.out.println("* Resultado esperado: " + expectedResult);
        System.out.println("* Resultado devuelto por equals: " + result);
        System.out.println();

        assertTrue(result);

        System.out.println("* Prueba pasada satisfactoriamente *");
        System.out.println();
    }

    @AfterClass
    public static void postTest() {
        // Cierra las conexiones
        entityManager.close();
        entityManagerFactory.close();
    }

    /**
     * Imrpime la descripcion del metodo a probar, el cual es
     * el metodo equals de la clase CropServiceBean
     */
    private void printDescriptionMethodToBeTested() {
        System.out.println("# Descripcion del metodo a probar");
        System.out.println("El metodo equals de la clase CropServiceBean compara dos cultivos y retorna true si son iguales y false en caso contrario.");
        System.out.println("Para determinar si dos cultivos son iguales compara el nombre, el mes de inicio de siembra, el mes de fin de siembra y la");
        System.out.println("region de ambos.");
        System.out.println();
        System.out.println("Las evaluaciones que realiza el metodo equals para determinar si dos cultivos son iguales o no son las siguientes:");
        System.out.println("1. si los dos cultivos a comparar tienen definido el nombre y NO tienen definido el mes de inicio de siembra, el mes de fin");
        System.out.println("de siembra y la region, y tienen el mismo nombre, retorna true.");
        System.out.println("2. si los dos cultivos a comparar tienen definido el nombre y el mes de inicio de siembra, y NO tienen definido el mes de fin");
        System.out.println("de siembra y la region, y tienen el mismo nombre y el mismo mes de inicio de siembra, retorna true.");
        System.out.println("3. si los dos cultivos a comparar tienen definido el nombre y el mes de fin de siembra, y NO tienen definido el mes de inicio");
        System.out.println("de siembra y la region, y tienen el mismo nombre y el mismo mes de fin de siembra, retorna true.");
        System.out.println("4. si los dos cultivos a comparar tienen definido el nombre y la region, y NO tienen definido el mes de inicio de siembra y");
        System.out.println("el mes de fin de siembra, y tienen el mismo nombre y la misma region, retorna true.");
        System.out.println("5. si los dos cultivos a comparar tienen definido el nombre, el mes de inicio de siembra y el mes de fin de siembra, y NO");
        System.out.println("tienen definida la region, y tienen el mismo nombre, el mismo mes de inicio de siembra y el mismo mes de fin de siembra,");
        System.out.println("retorna true.");
        System.out.println("6. si los dos cultivos a comparar tienen definido el nombre, el mes de inicio de siembra y la region, y NO tienen definido");
        System.out.println("el mes de fin de siembra, y tienen el mismo nombre, el mismo mes de inicio de siembra y la misma region, retorna true.");
        System.out.println("7. si los dos cultivos a comparar tienen definido el nombre, el mes de fin de siembra y la region, y NO tienen definido el");
        System.out.println("mes de inicio de siembra, y tienen el mismo nombre, el mismo mes de fin de siembra y la misma region, retorna true.");
        System.out.println("8. si los dos cultivos a comparar tienen definido el nombre, el mes de inicio de siembra, el mes de fin de siembra y la");
        System.out.println("region, y tienen el mismo nombre, el mismo mes de inicio de siembra, el mismo mes de fin de siembra y la misma region,");
        System.out.println("retorna true.");
        System.out.println();
    }

    /**
     * Imprime el nombre, el mes de inicio de siembra, el mes
     * de fin de siembra y la region de un cultivo
     * 
     * @param crop
     */
    private void printCrop(Crop crop) {
        System.out.println("Nombre: " + crop.getName());
        System.out.println("Mes de inicio de siembra: " + (crop.getPlantingStartMonth() != null ? crop.getPlantingStartMonth().getName() : "indefinido"));
        System.out.println("Mes de fin de siembra: " + (crop.getEndPlantingMonth() != null ? crop.getEndPlantingMonth().getName() : "indefinido"));
        System.out.println("Region: " + (crop.getRegion() != null ? crop.getRegion().getName() : "indefinida"));
        System.out.println();
    }

}
