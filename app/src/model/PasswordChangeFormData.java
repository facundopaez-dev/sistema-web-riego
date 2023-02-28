package model;

/*
 * PasswordChangeFormData es la clase que se utiliza para tomar los
 * datos ingresados por el usuario en el formulario de modificacion
 * de contraseña
 */
public class PasswordChangeFormData {

  private String password;
  private String newPassword;
  private String newPasswordConfirmed;

  public PasswordChangeFormData() {

  }

  public String getPassword() {
    return password;
  }

  public String getNewPassword() {
    return newPassword;
  }

  public String getNewPasswordConfirmed() {
    return newPasswordConfirmed;
  }

}
