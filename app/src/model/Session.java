package model;

import java.util.Calendar;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * Session es la clase que se utiliza para implementar el
 * control de inicio de sesion multiple. Esto es para
 * impedir que un usuario inicie sesion mas de una vez
 * con su cuenta.
 * 
 * Si un usuario tiene una sesion vigente, la aplicacion
 * debe impedir que inicie una sesion mas. De esta manera,
 * para que el usuario inicie una nueva sesion, debe cerrar
 * su sesion vigente o esperar a que la misma expire.
 */
@Entity
@Table(name = "USER_SESSION")
public class Session {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "ID")
  private int id;

  @Column(name = "DATE_ISSUE", nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Calendar dateIssue;

  @Column(name = "EXPIRATION_DATE", nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Calendar expirationDate;

  /*
   * Esta variable de instancia es para permitirle al usuario
   * iniciar sesion en el caso de que la cierre y la misma
   * aun sea vigente (que no ha expirado).
   */
  @Column(name = "CLOSED", nullable = false)
  private boolean closed;

  @ManyToOne
  @JoinColumn(name = "FK_USER", nullable = false)
  private User user;

  // Constructor method
  public Session() {

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
   * Returns value of dateIssue
   * 
   * @return
   */
  public Calendar getDateIssue() {
    return dateIssue;
  }

  /**
   * Sets new value of dateIssue
   * 
   * @param
   */
  public void setDateIssue(Calendar dateIssue) {
    this.dateIssue = dateIssue;
  }

  /**
   * Returns value of expirationDate
   * 
   * @return
   */
  public Calendar getExpirationDate() {
    return expirationDate;
  }

  /**
   * Sets new value of expirationDate
   * 
   * @param
   */
  public void setExpirationDate(Calendar expirationDate) {
    this.expirationDate = expirationDate;
  }

  /**
   * Returns value of closed
   * 
   * @return
   */
  public boolean getClosed() {
    return closed;
  }

  /**
   * Sets new value of closed
   * 
   * @param
   */
  public void setClosed(boolean closed) {
    this.closed = closed;
  }

  /**
   * Returns value of user
   * 
   * @return
   */
  public User getUser() {
    return user;
  }

  /**
   * Sets new value of user
   * 
   * @param
   */
  public void setUser(User user) {
    this.user = user;
  }

}