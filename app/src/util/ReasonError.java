package util;

/**
 * ReasonError es el enum que contiene las causas (como constantes
 * de enumeracion) por las cuales no se puede satisfacer una
 * peticion HTTP
 */
public enum ReasonError {
  USERNAME_OR_PASSWORD_INCORRECT("Nombre de usuario o contraseña incorrectos"),
  SESSION_EXPIRED("Sesión expirada"),
  UNAUTHORIZED_ACCESS("Acceso no autorizado"),
  RESOURCE_NOT_FOUND("Recurso no encontrado"),
  MULTIPLE_SESSIONS("No es posible tener más de una sesión abierta simultáneamente"),
  EMAIL_ALREADY_USED("Correo electrónico ya utilizado, elija otro"),
  INDEFINITE_SEED_DATE("La fecha de siembra debe estar definida"),
  INDEFINITE_PARCEL("La parcela debe estar definida"),
  INDEFINITE_CROP("El cultivo debe estar definido"),
  CREATION_NOT_ALLOWED_IN_DEVELOPMENT("No está permitido crear un registro de plantación para una parcela que tiene un registro de plantación en desarrrollo"),
  DATE_OVERLAP_ON_CREATION("No está permitido crear un registro de plantación con una fecha de siembra anterior o igual a la fecha de cosecha del último registro de plantación finalizado de una parcela"),
  OVERLAP_BETWEEN_SEED_DATE_AND_HARVEST_DATE("La fecha de siembra no debe ser mayor o igual a la fecha de cosecha"),
  DATE_OVERLAP_WITH_PREVIOUS_PLANTING_RECORD("La fecha de siembra no debe ser anterior o igual a la fecha de cosecha del registro de plantación inmediatamente anterior"),
  DATE_OVERLAP_WITH_NEXT_PLANTING_RECORD("La fecha de cosecha no debe ser posterior o igual a la fecha de siembra del registro de plantación inmediatamente siguiente"),
  INDEFINITE_DATES("Las fechas deben estar definidas"),
  CREATION_FUTURE_PLANTING_RECORD_NOT_ALLOWED("No está permitido crear un registro de plantación con una fecha de siembra estrictamente mayor (posterior) que la fecha actual");

  private final String reason;

  private ReasonError(String reason) {
    this.reason = reason;
  }

  public String getReason() {
    return reason;
  }

}
