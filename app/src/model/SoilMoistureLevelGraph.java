package model;

import java.util.Calendar;
import java.util.Collection;

public class SoilMoistureLevelGraph {

    private String text;

	/*
	 * Capacidad de almacenamiento de agua
	 * 
	 * El valor de la capacidad de almacenamiento de agua
	 * esta determinado por la lamina total de agua
	 * disponible (dt) [mm]
	 */
	private double totalAmountWaterAvailable;

	/*
	 * Umbral de riego
	 * 
	 * El valor del umbral de riego esta determinado por
	 * la lamina de riego optima (drop) [mm]
	 */
	private double optimalIrrigationLayer;

	/*
	 * El negativo de la capacidad de almacenamiento de agua del
	 * suelo [mm] es el limite inferior que se utiliza para calcular
	 * el estado "Desarrollo en marchitez" de un cultivo perteneciente
	 * a un registro de plantacion que tiene un estado de desarrollo
	 * relacionado al uso de datos de suelo (desarrollo optimo,
	 * desarrollo en riesgo de marchitez, desarrollo en marchitez).
	 * Este valor tambien es utilizado para determinar la muerte
	 * de un cultivo. Si el nivel de humedad del suelo que tiene
	 * un cultivo sembrado es estrictamente al negativo de la
	 * capacidad de almacenamiento de agua del suelo, el cultivo
	 * esta muerto. Esto se debe a que ningun cultivo puede sobrevivir
     * con una perdida de humedad del suelo estrictamente mayor
     * al doble de la capacidad de almacenamiento de agua del suelo.
     * La suma entre la capacidad de almacenamiento de agua del
     * suelo y el valor absoluto del negativo de la misma da
     * como resultado el doble de la capacidad de almacenamiento
     * de agua de del suelo. Este es el motivo por el cual se
     * dice que un cultivo muere cuando el nivel de humedad del
     * suelo es estrictamente menor al negativo de la capacidad
     * de almacenamiento de agua del suelo.
	 * 
	 * La capacidad de almacenamiento de agua del suelo esta
	 * determinada por la lamina total de agua disponible [mm] (dt).
	 */
	private double negativeTotalAmountWaterAvailable;
	private boolean showGraph;
    private Collection<Double> data;
    private Collection<String> labels;

    public SoilMoistureLevelGraph() {

    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public double getTotalAmountWaterAvailable() {
        return totalAmountWaterAvailable;
    }

    public void setTotalAmountWaterAvailable(double totalAmountWaterAvailable) {
        this.totalAmountWaterAvailable = totalAmountWaterAvailable;
    }

    public double getOptimalIrrigationLayer() {
        return optimalIrrigationLayer;
    }

    public void setOptimalIrrigationLayer(double optimalIrrigationLayer) {
        this.optimalIrrigationLayer = optimalIrrigationLayer;
    }

    public double getNegativeTotalAmountWaterAvailable() {
        return negativeTotalAmountWaterAvailable;
    }

    public void setNegativeTotalAmountWaterAvailable(double negativeTotalAmountWaterAvailable) {
        this.negativeTotalAmountWaterAvailable = negativeTotalAmountWaterAvailable;
    }

    public boolean getShowGraph() {
        return showGraph;
    }

    public void setShowGraph(boolean showGraph) {
        this.showGraph = showGraph;
    }

    public Collection<Double> getData() {
        return data;
    }

    public void setData(Collection<Double> data) {
        this.data = data;
    }

    public Collection<String> getLabels() {
        return labels;
    }

    public void setLabels(Collection<String> labels) {
        this.labels = labels;
    }

}