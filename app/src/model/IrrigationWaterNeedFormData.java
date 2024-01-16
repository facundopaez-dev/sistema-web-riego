package model;

import java.util.Calendar;

/*
 * IrrigationWaterNeedFormData es la clase que se utiliza para mostrar
 * los datos resultantes del calculo de la cantidad (o necesidad) de
 * agua de riego de un cultivo (en desarrollo) en la fecha actual
 * [mm/dia]
 */
public class IrrigationWaterNeedFormData {

	private Parcel parcel;
	private Crop crop;
	private double cropIrrigationWaterNeed;
	private double irrigationDone;

	public IrrigationWaterNeedFormData() {

	}

	public Parcel getParcel() {
		return parcel;
	}

	public void setParcel(Parcel parcel) {
		this.parcel = parcel;
	}

	public Crop getCrop() {
		return crop;
	}

	public void setCrop(Crop crop) {
		this.crop = crop;
	}

	public double getCropIrrigationWaterNeed() {
		return cropIrrigationWaterNeed;
	}

	public void setCropIrrigationWaterNeed(double cropIrrigationWaterNeed) {
		this.cropIrrigationWaterNeed = cropIrrigationWaterNeed;
	}

	public double getIrrigationDone() {
		return irrigationDone;
	}

	public void setIrrigationDone(double irrigationDone) {
		this.irrigationDone = irrigationDone;
	}

}
