package model;

import java.util.Calendar;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "PARCEL_OPTION")
public class Option {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private int id;

    @Column(name = "PAST_DAYS_REFERENCE", nullable = false)
    private int pastDaysReference;

    @Column(name = "SOIL_FLAG", nullable = false)
    private boolean soilFlag;

    @Column(name = "FLAG_LAST_IRRIGATION_THIRTY_DAYS", nullable = false)
    private boolean flagLastIrrigationThirtyDays;

    @Column(name = "FLAG_MESSAGE_FIELD_CAPACITY", nullable = false)
    private boolean flagMessageFieldCapacity;

    public Option() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPastDaysReference() {
        return pastDaysReference;
    }

    public void setPastDaysReference(int pastDaysReference) {
        this.pastDaysReference = pastDaysReference;
    }

    public boolean getSoilFlag() {
        return soilFlag;
    }

    public void setSoilFlag(boolean soilFlag) {
        this.soilFlag = soilFlag;
    }

    public boolean getFlagLastIrrigationThirtyDays() {
        return flagLastIrrigationThirtyDays;
    }

    public void setFlagLastIrrigationThirtyDays(boolean flagLastIrrigationThirtyDays) {
        this.flagLastIrrigationThirtyDays = flagLastIrrigationThirtyDays;
    }

    public boolean getFlagMessageFieldCapacity() {
        return flagMessageFieldCapacity;
    }

    public void setFlagMessageFieldCapacity(boolean flagMessageFieldCapacity) {
        this.flagMessageFieldCapacity = flagMessageFieldCapacity;
    }

}
