package model;

import java.util.Calendar;

/*
 * IrrigationWaterNeedFormData es la clase que se utiliza para mostrar
 * los datos resultantes del calculo de la necesidad de agua de riego
 * de un cultivo en desarrollo
 */
public class IrrigationWaterNeedFormData {

	private Calendar date;
	private Parcel parcel;
	private Crop crop;
	private double irrigationWaterNeed;
	private double irrigationDone;

	public IrrigationWaterNeedFormData() {

	}

	public Calendar getDate() {
		return date;
	}

	public void setDate(Calendar date) {
		this.date = date;
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

	public double getIrrigationWaterNeed() {
		return irrigationWaterNeed;
	}

	public void setIrrigationWaterNeed(double irrigationWaterNeed) {
		this.irrigationWaterNeed = irrigationWaterNeed;
	}

	public double getIrrigationDone() {
		return irrigationDone;
	}

	public void setIrrigationDone(double irrigationDone) {
		this.irrigationDone = irrigationDone;
	}

}
