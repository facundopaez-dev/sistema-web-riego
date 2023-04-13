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
  DATE_OVERLAP_WITH_PREVIOUS_PLANTING_RECORD("La fecha de siembra no debe ser anterior o igual a la fecha de cosecha del registro de plantación inmediatamente anterior"),
  DATE_OVERLAP_WITH_NEXT_PLANTING_RECORD("La fecha de cosecha no debe ser posterior o igual a la fecha de siembra del registro de plantación inmediatamente siguiente"),
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
  INCORRECTLY_CONFIRMED_NEW_PASSWORD("La confirmación de la nueva contraseña no es igual a la nueva contraseña ingresada"),
  ACCOUNT_ACTIVATION_LINK_EXPIRED("Enlace de activación de cuenta expirado, vuelva a registrarse"),
  THERE_IS_NO_ACCOUNT_WITH_EMAIL_ADDRESS_ENTERED("No existe una cuenta con la dirección de correo electrónico ingresada"),
  INVALID_PASSWORD_RESET_LINK("Enlace de restablecimiento de contraseña inválido"),
  PASSWORD_RESET_LINK_EXPIRED("Enlace de restablecimiento de contraseña expirado"),
  INACTIVE_USER_TO_RECOVER_PASSWORD("Para recuperar su contraseña primero debe activar su cuenta mediante el correo electrónico de confirmación de registro"),
  NEGATIVE_REALIZED_IRRIGATION("El riego realizado debe ser mayor o igual a cero"),
  MODIFICATION_PAST_IRRIGATION_RECORD_NOT_ALLOWED("No está permitida la modificación de un registro de riego del pasado (es decir, uno que tiene una fecha anterior a la fecha actual)"),
  PARCEL_NAME_UNDEFINED("El nombre de la parcela debe estar definido"),
  INVALID_NUMBER_OF_HECTARES("La cantidad de hectáreas debe ser mayor a 0.0"),
  INVALID_PARCEL_NAME("El nombre de una parcela debe empezar con una palabra formada únicamente por caracteres alfabéticos y puede tener más de una palabra formada únicamente por caracteres alfanuméricos"),
  PARCEL_NAME_ALREADY_USED("Nombre de parcela ya utilizado, elija otro"),
  DATE_FROM_UNDEFINED("La fecha desde debe estar definida"),
  DATE_FROM_AND_DATE_UNTIL_OVERLAPPING("La fecha desde no debe ser mayor o igual a la fecha hasta"),
  CLIMATE_RECORDS_AND_PLANTING_RECORDS_DO_NOT_EXIST("La parcela seleccionada no tiene registros climáticos ni registros de plantación finalizados para generar un informe estadístico"),
  CROP_NAME_ALREADY_USED("Nombre de cultivo ya utilizado, elija otro"),
  UNDEFINED_CROP_NAME("El nombre del cultivo debe estar definido"),
  TYPE_CROP_UNDEFINED("El tipo del cultivo debe estar definido"),
  INVALID_CROP_NAME("El nombre de un cultivo debe empezar con una palabra formada únicamente por caracteres alfabéticos y puede tener más de una palabra formada únicamente por caracteres alfabéticos"),
  EXISTING_CLIMATE_RECORD("Ya existe un registro climático con la fecha ingresada para la parcela seleccionada"),
  CREATION_PAST_CLIMATE_RECORD_NOT_ALLOWED("No está permitida la creación de un registro climático del pasado (es decir, uno que tiene una fecha anterior a la fecha actual)"),
  UNDEFINED_DATE("La fecha debe estar definida"),
  INVALID_WIND_SPEED("La velocidad del viento debe ser un valor mayor o igual a 0.0"),
  INVALID_PRECIPITATION_PROBABILITY("La probabilidad de la precipitación debe ser un valor entre 0.0 y 100, incluido"),
  INVALID_PRECIPITATION("La precipitación debe ser un valor mayor o igual 0.0"),
  INVALID_EXCESS_WATER("El agua excedente debe ser un valor mayor o igual a 0.0"),
  INVALID_CLOUDINESS("La nubosidad debe ser un valor entre 0.0 y 100, incluido"),
  INVALID_ATMOSPHERIC_PRESSURE("La presión atmosférica debe ser un valor mayor a 0.0"),
  INVALID_ETO("La evapotranspiración del cultivo de referencia (ETo) debe ser un valor mayor o igual a 0.0"),
  INVALID_ETC("La evapotranspiración del cultivo (ETc) debe ser un valor mayor o igual a 0.0"),
  MODIFICATION_CLIMATE_RECORD_WITH_PAST_DATE_NOT_ALLOWED("No está permitido modificar un registro climático con una fecha del pasado (es decir, una fecha anterior a la fecha actual)"),
  MODIFICATION_PAST_CLIMATE_RECORD_NOT_ALLOWED("No está permitida la modificación de un registro climático del pasado (es decir, uno que tiene una fecha anterior a la fecha actual)"),
  OVERLAP_BETWEEN_SEED_DATE_AND_HARVEST_DATE_WITH_LAST_FINISHED_PLANTING_RECORD("No está permitido modificar un registro de plantación con una fecha de siembra menor o igual a la fecha de cosecha del último registro de plantación finalizado de la parcela elegida"),
  MODIFICATION_WITH_PAST_SEED_DATE_NOT_ALLOWED("No está permitido modificar un registro de plantación con una fecha de siembra menor a la fecha actual (es decir, anterior a la fecha actual)"),
  MODIFICATION_WITH_FUTURE_SEED_DATE_NOT_ALLOWED("No está permitido modificar un registro de plantación con una fecha de siembra mayor a la fecha actual (es decir, posterior a la fecha actual)"),
  MODIFICATION_NON_MODIFIABLE_PLANTING_RECORD_NOT_ALLOWED("No está permitida la modificación de un registro de plantación finalizado"),
  DELETION_PARCEL_WITH_PLANTING_RECORD_IN_DEVELOPMENT_NOT_ALLOWED("No está permitido eliminar (lógicamente) una parcela que tiene un registro de plantación en desarrollo"),
  MODIFICATION_IRRIGATION_WATER_NEED_NOT_ALLOWED("No está permitida la modificación de la necesidad de agua de riego");

  private final String reason;

  private ReasonError(String reason) {
    this.reason = reason;
  }

  public String getReason() {
    return reason;
  }

}
