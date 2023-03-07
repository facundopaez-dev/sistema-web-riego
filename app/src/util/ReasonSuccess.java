package util;

/**
 * ReasonSuccess es el enum que contiene las causas (como constantes
 * de enumeracion) por las cuales una operacion solicitada mediante una
 * peticion HTTP, es realizada satisfactoriamente
 */
public enum ReasonSuccess {
  ACCOUNT_ACTIVATED("Cuenta satisfactoriamente activada"),
  PASSWORD_RESET_EMAIL_SENT("Correo electrónico de restablecimiento de contraseña enviado a su casilla de correo electrónico"),
  PASSWORD_RESET_SUCCESSFULLY("Contraseña restablecida satisfactoriamente");

  private final String reason;

  private ReasonSuccess(String reason) {
    this.reason = reason;
  }

  public String getReason() {
    return reason;
  }

}
