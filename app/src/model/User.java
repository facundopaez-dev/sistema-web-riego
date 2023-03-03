package model;

import java.util.Calendar;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "IRRIGATION_SYSTEM_USER")
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "ID")
  private int id;

  @Column(name = "USERNAME", nullable = false, unique = true)
  private String username;

  @Column(name = "PASSWORD", nullable = false)
  private String password;

  @Column(name = "NAME", nullable = false)
  private String name;

  @Column(name = "LAST_NAME", nullable = false)
  private String lastName;

  @Column(name = "EMAIL", nullable = false, unique = true)
  private String email;

  @Column(name = "ACTIVE", nullable = false)
  private boolean active;

  @Column(name = "SUPERUSER", nullable = false)
  private boolean superuser;

  public User() {

  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public boolean getActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public boolean getSuperuser() {
    return superuser;
  }

  public void setSuperuser(boolean superuser) {
    this.superuser = superuser;
  }

  @Override
  public String toString() {
    return String.format(
      "ID: %d\nUsername: %s\nPassword: %s\nName: %s\nLast name: %s\nEmail: %s\nActive: %b\nSuperuser: %b\n",
      id,
      username,
      password,
      name,
      lastName,
      email,
      active,
      superuser
    );
  }

}
