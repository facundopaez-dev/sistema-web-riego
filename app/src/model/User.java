package model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.JoinColumn;

@Entity
@Table(name = "IRRIGATION_SYSTEM_USER")
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "ID")
  private int id;

  @Column(name = "USERNAME", nullable = false, unique = true)
  private String username;

  @Column(name = "NAME", nullable = false)
  private String name;

  @Column(name = "LAST_NAME", nullable = false)
  private String lastName;

  @Column(name = "ACTIVE", nullable = false)
  private boolean active;

  @Column(name = "SUPERUSER", nullable = false)
  private boolean superuser;

  @Column(name = "SUPERUSER_PERMISSION_MODIFIER")
  private boolean superuserPermissionModifier;

  @Column(name = "USER_DELETION_PERMISSION")
  private boolean userDeletionPermission;

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

  public boolean getSuperuserPermissionModifier() {
    return superuserPermissionModifier;
  }

  public void setSuperuserPermissionModifier(boolean superuserPermissionModifier) {
    this.superuserPermissionModifier = superuserPermissionModifier;
  }

  public boolean getUserDeletionPermission() {
    return userDeletionPermission;
  }

  public void setUserDeletionPermission(boolean userDeletionPermission) {
    this.userDeletionPermission = userDeletionPermission;
  }

  @Override
  public String toString() {
    return String.format(
        "ID: %d\nUsername: %s\nName: %s\nLast name: %s\nActive: %b\nSuperuser: %b\nUser deletion permission: %b\n",
        id,
        username,
        name,
        lastName,
        active,
        superuser);
  }

}
