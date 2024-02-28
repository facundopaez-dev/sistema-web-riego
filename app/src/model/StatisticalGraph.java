package model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Id;
import java.util.Calendar;
import java.util.Collection;

@Entity
@Table(name = "STATISTICAL_GRAPH")
public class StatisticalGraph {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "DATE_FROM", nullable = false)
    @Temporal(TemporalType.DATE)
    private Calendar dateFrom;

    @Column(name = "DATE_UNTIL", nullable = false)
    @Temporal(TemporalType.DATE)
    private Calendar dateUntil;

    @Column(name = "TEXT")
    private String text;

    private Collection<Integer> data;
    private Collection<String> labels;

    @ManyToOne
    @JoinColumn(name = "FK_PARCEL", nullable = false)
    private Parcel parcel;

    @ManyToOne
    @JoinColumn(name = "FK_STATISTICAL_DATA", nullable = false)
    private StatisticalData statisticalData;

    public StatisticalGraph() {

    }

    public int getId() {
        return id;
    }

    public Calendar getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(Calendar dateFrom) {
        this.dateFrom = dateFrom;
    }

    public Calendar getDateUntil() {
        return dateUntil;
    }

    public void setDateUntil(Calendar dateUntil) {
        this.dateUntil = dateUntil;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Collection<Integer> getData() {
        return data;
    }

    public void setData(Collection<Integer> data) {
        this.data = data;
    }

    public Collection<String> getLabels() {
        return labels;
    }

    public void setLabels(Collection<String> labels) {
        this.labels = labels;
    }

    public Parcel getParcel() {
        return parcel;
    }

    public void setParcel(Parcel parcel) {
        this.parcel = parcel;
    }

    public StatisticalData getStatisticalData() {
        return statisticalData;
    }

    public void setStatisticalData(StatisticalData statisticalData) {
        this.statisticalData = statisticalData;
    }

}