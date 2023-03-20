package model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * SecretKey es la clase que se utiliza para almacenar en la base de
 * datos subyacente, la clave secreta con la que se firma un JWT, el
 * cual se crea cuando un usuario inicia sesion satisfactoriamente.
 *
 * La clave secreta la debe conocer unicamente el servidor.
 */
@Entity
@Table(name = "SECRET_KEY")
public class SecretKey {

  @Id
  @Column(name = "ID")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  @Column(name = "VALUE", nullable = false)
  private String value;

  public SecretKey() {
  }

  public int getId() {
    return id;
  }

  public String getValue() {
    return value;
  }

}
