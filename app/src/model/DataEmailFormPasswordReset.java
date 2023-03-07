package model;

/*
 * DataEmailFormPasswordReset es la clase que se utiliza para tomar el
 * correo electronico del formulario que se utiliza para el envio de un
 * correo electronico para el restablecimiento de la contraseña de la
 * cuenta del usuario
 */
public class DataEmailFormPasswordReset {

  private String email;

  public DataEmailFormPasswordReset() {

  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

}
