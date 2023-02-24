package model;

/*
 * UserData es la clase que se utiliza para tomar los
 * datos ingresados por el usuario en el formulario de
 * registro de usuario
 */
public class UserData {

  private String username;
  private String name;
  private String lastName;
  private String email;
  private String password;
  private String passwordConfirmed;

  public UserData() {

  }

  public String getUsername() {
    return username;
  }

  public String getName() {
    return name;
  }

  public String getLastName() {
    return lastName;
  }

  public String getEmail() {
    return email;
  }

  public String getPassword() {
    return password;
  }

  public String getPasswordConfirmed() {
    return passwordConfirmed;
  }

}
