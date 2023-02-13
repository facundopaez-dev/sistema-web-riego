package model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Month {

  /*
   * Instance variables
   */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "ID")
  private int id;

  @Column(name = "NAME", unique = true)
  private String name;

  public Month() {

  }

  /* Getters and setters */

  /**
   * Returns value of id
   * @return id
   */
  public int getId() {
    return id;
  }

  /**
   * Returns value of name
   * @return name
   */
  public String getName() {
    return name;
  }

}
