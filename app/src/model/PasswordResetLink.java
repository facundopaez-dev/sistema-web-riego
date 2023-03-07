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
import javax.persistence.ManyToOne;
import util.UtilDate;

/*
 * PasswordResetLink es la clase que se utiliza para
 * representar a un enlace de restablecimiento de
 * contraseña
 */
@Entity
@Table(name = "PASSWORD_RESET_LINK")
public class PasswordResetLink {

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

  @ManyToOne
  @JoinColumn(name = "FK_USER")
  private User user;

  public PasswordResetLink() {

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
