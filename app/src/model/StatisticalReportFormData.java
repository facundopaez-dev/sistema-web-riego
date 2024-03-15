package model;

import java.util.Calendar;

public class StatisticalReportFormData {

    private Calendar dateFrom;
    private Calendar dateUntil;
    private Parcel parcel;
    private int[] statisticalDataNumbers;

    public StatisticalReportFormData() {

    }

    public Calendar getDateFrom() {
        return dateFrom;
    }

    public Calendar getDateUntil() {
        return dateUntil;
    }

    public Parcel getParcel() {
        return parcel;
    }

    public int[] getStatisticalDataNumbers() {
        return statisticalDataNumbers;
    }

}