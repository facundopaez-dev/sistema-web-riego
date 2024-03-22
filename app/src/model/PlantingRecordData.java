package model;

/*
 * Esta clase es para visualizar un registro de
 * plantacion y un grafico de la evolucion diaria
 * del nivel de humedad del suelo mediante la funcion
 * find() del controller PlantingRecordCtrl.js de
 * la ruta app/public/controllers/user
 */
public class PlantingRecordData {

    private PlantingRecord plantingRecord;
    private SoilMoistureLevelGraph soilMoistureLevelGraph;

    public PlantingRecordData() {

    }

    public PlantingRecord getPlantingRecord() {
        return plantingRecord;
    }

    public void setPlantingRecord(PlantingRecord plantingRecord) {
        this.plantingRecord = plantingRecord;
    }

    public SoilMoistureLevelGraph getSoilMoistureLevelGraph() {
        return soilMoistureLevelGraph;
    }

    public void setSoilMoistureLevelGraph(SoilMoistureLevelGraph soilMoistureLevelGraph) {
        this.soilMoistureLevelGraph = soilMoistureLevelGraph;
    }

}