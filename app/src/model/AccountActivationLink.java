package model;

import java.util.Calendar;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import util.UtilDate;

/*
 * AccountActivationLink es la clase que se utiliza para
 * controlar la vigencia y expiracion de un enlace de
 * activacion de cuenta. Dicho enlace es enviado a la
 * casilla de correo electronico del usuario cuando este
 * se registra en la aplicacion.
 */
@Entity
@Table(name = "ACCOUNT_ACTIVATION_LINK")
public class AccountActivationLink {

  @Id
  @Column(name = "ID")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  @Column(name = "DATE_ISSUE", nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Calendar dateIssue;

  @Column(name = "EXPIRATION_DATE", nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Calendar expirationDate;

  @Column(name = "CONSUMED", nullable = false)
  private boolean consumed;

  @OneToOne
  @JoinColumn(name = "FK_USER")
  private User user;

  public AccountActivationLink() {

  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public Calendar getDateIssue() {
    return dateIssue;
  }

  public void setDateIssue(Calendar dateIssue) {
    this.dateIssue = dateIssue;
  }

  public Calendar getExpirationDate() {
    return expirationDate;
  }

  public void setExpirationDate(Calendar expirationDate) {
    this.expirationDate = expirationDate;
  }

  public boolean getConsumed() {
    return consumed;
  }

  public void setConsumed(boolean consumed) {
    this.consumed = consumed;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  @Override
  public String toString() {
    return String.format(
        "ID: %d\nFecha de emisión: %s\nFecha de emision en tiempo UNIX: %d\nFecha de expiración: %s\nFecha de expiracion en tiempo UNIX: %d\nID de usuario: %d\nEnlace consumido: %b\n",
        id,
        UtilDate.formatDate(dateIssue),
        dateIssue.getTimeInMillis(),
        UtilDate.formatDate(expirationDate),
        expirationDate.getTimeInMillis(),
        user.getId(),
        consumed);
  }

}
