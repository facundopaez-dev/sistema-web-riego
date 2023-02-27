package model;

/*
 * SignupFormData es la clase que se utiliza para tomar los
 * datos ingresados por el usuario en el formulario de
 * registro de usuario
 */
public class SignupFormData {

  private String username;
  private String name;
  private String lastName;
  private String email;
  private String password;
  private String passwordConfirmed;

  public SignupFormData() {

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
