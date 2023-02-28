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
  USERNAME_ALREADY_USED("Nombre de usuario ya utilizado, elija otro"),
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
  CREATION_FUTURE_PLANTING_RECORD_NOT_ALLOWED("No está permitido crear un registro de plantación con una fecha de siembra estrictamente mayor (posterior) que la fecha actual"),
  UNDEFINED_CROP_TYPE_NAME("El nombre del tipo de cultivo debe estar definido"),
  TYPE_CROP_ALREADY_EXISTING("El tipo de cultivo ingresado ya existe"),
  INVALID_CROP_TYPE_NAME("Nombre incorrecto: el nombre para un tipo de cultivo sólo puede contener letras, y un espacio en blanco entre palabra y palabra si está formado por más de una palabra"),
  UNDEFINED_USERNAME("El nombre de usuario debe estar definido"),
  UNDEFINED_NAME("El nombre debe estar definido"),
  UNDEFINED_LAST_NAME("El apellido debe estar definido"),
  UNDEFINED_EMAIL("La dirección de correo electrónico debe estar definida"),
  UNDEFINED_PASSWORD("La contraseña debe estar definida"),
  UNDEFINED_CONFIRMED_PASSWORD("La confirmación de la contraseña debe estar definida"),
  MALFORMED_USERNAME("El nombre debe usuario debe tener una longitud de entre 4 y 15 caracteres, comenzar con caracteres alfabéticos seguido o no de números y/o guiones bajos"),
  MALFORMED_NAME("El nombre debe tener una longitud de entre 3 y 30 caracteres alfabéticos, empezar con una letra mayúscula seguido de letras minúsculas, tener un espacio en blanco entre nombre y nombre si hay más de un nombre, y los nombres que vienen después del primero deben empezar con una letra mayúscula seguido de letras minúsculas"),
  MALFORMED_LAST_NAME("El apellido debe tener una longitud de entre 3 y 30 caracteres alfabéticos, empezar con una letra mayúscula seguido de letras minúsculas, tener un espacio en blanco entre apellido y apellido si hay más de un apellido, y los apellidos que vienen después del primero deben empezar con una letra mayúscula seguido de letras minúsculas"),
  MALFORMED_EMAIL("La dirección de correo electrónico no es válida"),
  MALFORMED_PASSWORD("La contraseña debe tener como mínimo 8 caracteres de longitud, una letra minúscula, una letra mayúscula y un número de 0 a 9, con o sin caracteres especiales"),
  INCORRECTLY_CONFIRMED_PASSWORD("La confirmación de la contraseña no es igual a la contraseña ingresada"),
  EMPTY_FORM("Debe completar todos los campos del formulario"),
  NEW_PASSWORD_INCORRECTLY_CONFIRMED("La confirmación de la nueva contraseña no es igual a la nueva contraseña ingresada"),
  INCORRECT_PASSWORD("Contraseña incorrecta"),
  UNDEFINED_NEW_PASSWORD("La nueva contraseña debe estar definida"),
  UNDEFINED_CONFIRMED_NEW_PASSWORD("La confirmación de la nueva contraseña debe estar definida"),
  MALFORMED_NEW_PASSWORD("La nueva contraseña debe tener como mínimo 8 caracteres de longitud, una letra minúscula, una letra mayúscula y un número de 0 a 9, con o sin caracteres especiales"),
  INCORRECTLY_CONFIRMED_NEW_PASSWORD("La confirmación de la nueva contraseña no es igual a la nueva contraseña ingresada");

  private final String reason;

  private ReasonError(String reason) {
    this.reason = reason;
  }

  public String getReason() {
    return reason;
  }

}
