package model;

/*
 * PasswordResetFormData es la clase que se utiliza para tomar
 * la nueva contraseña y la confirmacion de la nueva contraseña
 * del formulario de restablecimiento de contraseña
 */
public class PasswordResetFormData {

  private String newPassword;
  private String newPasswordConfirmed;

  public PasswordResetFormData() {

  }

  public String getNewPassword() {
    return newPassword;
  }

  public void setNewPassword(String newPassword) {
    this.newPassword = newPassword;
  }

  public String getNewPasswordConfirmed() {
    return newPasswordConfirmed;
  }

  public void setNewPasswordConfirmed(String newPasswordConfirmed) {
    this.newPasswordConfirmed = newPasswordConfirmed;
  }

}
