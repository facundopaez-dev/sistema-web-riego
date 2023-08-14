package model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.OneToOne;
import javax.persistence.JoinColumn;

/**
 * Esta clase representa la cantidad de registros climaticos
 * del pasado (es decir, anteriores a la fecha actual) que la
 * aplicacion debe utilizar por usuario para calcular la necesidad
 * de agua de riego en la fecha actual de un cultivo sembrado
 * y en desarrollo en una parcela.
 */

@Entity
@Table(name = "PAST_DAYS_REFERENCE")
public class PastDaysReference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private int id;

    @Column(name = "VALUE", nullable = false)
    private int value;

    @OneToOne
    @JoinColumn(name = "FK_USER", unique = true, nullable = false)
    private User user;

    public PastDaysReference() {

    }

    /**
     * Returns value of id
     * 
     * @return
     */
    public int getId() {
        return id;
    }

    /**
     * Returns value of value
     * 
     * @return
     */
    public int getValue() {
        return value;
    }

    /**
     * Sets new value of value
     * 
     * @param
     */
    public void setValue(int value) {
        this.value = value;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

}