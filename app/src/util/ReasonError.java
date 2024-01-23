package util;

/**
 * ReasonError es el enum que contiene las causas (como constantes
 * de enumeracion) por las cuales no se puede satisfacer una
 * peticion HTTP
 */
public enum ReasonError {
  USERNAME_OR_PASSWORD_INCORRECT("Nombre de usuario o contraseña incorrectos"),
  SESSION_EXPIRED("Sesión expirada"),
  NO_ACTIVE_SESSION("No tiene una sesión activa"),
  JWT_NOT_ASSOCIATED_WITH_ACTIVE_SESSION("El JWT no corresponde a una sesión activa"),
  UNAUTHORIZED_ACCESS("Acceso no autorizado"),
  RESOURCE_NOT_FOUND("Recurso no encontrado"),
  MULTIPLE_SESSIONS("No es posible tener más de una sesión abierta simultáneamente"),
  USERNAME_ALREADY_USED("Nombre de usuario ya utilizado, elija otro"),
  EMAIL_ALREADY_USED("Correo electrónico ya utilizado, elija otro"),
  INDEFINITE_SEED_DATE("La fecha de siembra debe estar definida"),
  INDEFINITE_PARCEL("La parcela debe estar definida"),
  INDEFINITE_CROP("El cultivo debe estar definido"),
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
  MALFORMED_USERNAME("El nombre de usuario debe tener una longitud de entre 4 y 15 caracteres (sin símbolos de acentuación), comenzar con caracteres alfabéticos seguido o no de números y/o guiones bajos"),
  MALFORMED_NAME("El nombre debe tener una longitud de entre 3 y 30 caracteres alfabéticos sin símbolos de acentuación, empezar con una letra mayúscula seguido de letras minúsculas, tener un espacio en blanco entre nombre y nombre si hay más de un nombre, y los nombres que vienen después del primero deben empezar con una letra mayúscula seguido de letras minúsculas"),
  MALFORMED_LAST_NAME("El apellido debe tener una longitud de entre 3 y 30 caracteres alfabéticos sin símbolos de acentuación, empezar con una letra mayúscula seguido de letras minúsculas, tener un espacio en blanco entre apellido y apellido si hay más de un apellido, y los apellidos que vienen después del primero deben empezar con una letra mayúscula seguido de letras minúsculas"),
  MALFORMED_EMAIL("La dirección de correo electrónico no es válida"),
  MALFORMED_PASSWORD("La contraseña debe tener como mínimo 8 caracteres de longitud, una letra minúscula, una letra mayúscula y un número de 0 a 9, con o sin caracteres especiales"),
  INCORRECTLY_CONFIRMED_PASSWORD("La confirmación de la contraseña no es igual a la contraseña ingresada"),
  EMPTY_DATA("Debe proporcionar todos los datos requeridos"),
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
  UNDEFINED_PARCEL_NAME("El nombre de la parcela debe estar definido"),
  INVALID_NUMBER_OF_HECTARES("La cantidad de hectáreas debe ser mayor a 0.0"),
  INVALID_PARCEL_NAME("El nombre de una parcela debe empezar con una palabra formada únicamente por caracteres alfabéticos. Puede haber más de una palabra formada únicamente por caracteres alfabéticos y puede haber palabras formadas únicamente por caracteres numéricos. Todas las palabras deben estar separadas por un espacio en blanco."),
  PARCEL_NAME_ALREADY_USED("Nombre de parcela ya utilizado, elija otro"),
  UNDEFINED_DATES("Las fechas deben estar definidas"),
  DATE_FROM_AND_DATE_UNTIL_OVERLAPPING("La fecha desde no debe ser mayor o igual a la fecha hasta"),
  CLIMATE_RECORDS_AND_PLANTING_RECORDS_DO_NOT_EXIST("La parcela seleccionada no tiene registros climáticos ni registros de plantación finalizados para generar un informe estadístico"),
  EXISTING_CROP("Ya existe un cultivo con el nombre, el mes de inicio de siembra, el mes de fin de siembra y la región elegidos"),
  UNDEFINED_CROP_NAME("El nombre del cultivo debe estar definido"),
  TYPE_CROP_UNDEFINED("El tipo del cultivo debe estar definido"),
  INVALID_CROP_NAME("El nombre de un cultivo debe empezar con una palabra formada únicamente por caracteres alfabéticos y puede tener más de una palabra formada únicamente por caracteres alfabéticos"),
  EXISTING_CLIMATE_RECORD("Ya existe un registro climático con la fecha ingresada para la parcela seleccionada"),
  UNDEFINED_DATE("La fecha debe estar definida"),
  INVALID_WIND_SPEED("La velocidad del viento debe ser un valor mayor o igual a 0.0"),
  INVALID_PRECIPITATION_PROBABILITY("La probabilidad de la precipitación debe ser un valor entre 0.0 y 100, incluido"),
  INVALID_PRECIPITATION("La precipitación debe ser un valor mayor o igual 0.0"),
  INVALID_CLOUDINESS("La nubosidad debe ser un valor entre 0.0 y 100, incluido"),
  INVALID_ATMOSPHERIC_PRESSURE("La presión atmosférica debe ser un valor mayor a 0.0"),
  MODIFICATION_WITH_PAST_SEED_DATE_NOT_ALLOWED("No está permitido modificar un registro de plantación con una fecha de siembra menor a la fecha actual (es decir, anterior a la fecha actual)"),
  MODIFICATION_WITH_FUTURE_SEED_DATE_NOT_ALLOWED("No está permitido modificar un registro de plantación con una fecha de siembra mayor a la fecha actual (es decir, posterior a la fecha actual)"),
  DELETION_PARCEL_WITH_PLANTING_RECORD_IN_DEVELOPMENT_NOT_ALLOWED("No está permitido eliminar (lógicamente) una parcela que tiene un registro de plantación en desarrollo"),
  MODIFICATION_IRRIGATION_WATER_NEED_NOT_ALLOWED("No está permitida la modificación de la necesidad de agua de riego"),
  MODIFICATION_WITH_PARCEL_HAS_PLANTING_RECORD_IN_DEVELOPMENT_NOT_ALLOWED("La parcela seleccionada ya tiene un registro de plantación en desarrollo"),
  DELETE_FINISHED_PLANTING_RECORD_NOT_ALLOWED("No está permitido eliminar un registro de plantación finalizado"),
  UNDEFINED_SEED_DATE("La fecha de siembra debe estar definida"),
  UNDEFINED_HARVEST_DATE("La fecha de cosecha debe estar definida"),
  OVERLAPPING_SEED_DATE_AND_HARVEST_DATE("La fecha de siembra no debe ser mayor ni igual a la fecha de cosecha"),
  OVERLAPPING_DATES("Hay superposición de fechas entre este registro de plantación y los demás registros de plantación de la misma parcela"),
  MODIFICATION_NON_MODIFIABLE_PLANTING_RECORD_NOT_ALLOWED("No está permitida la modificación de un registro de plantación no modificable"),
  MODIFIABILITY_PLANTING_RECORD_NOT_ALLOWED("No está permitido hacer que un registro de plantación que no tiene el estado finalizado sea no modificable"),
  IRRIGATION_RECORD_OF_THE_FUTURE_NOT_ALLOWED("No está permitido que un registro de riego tenga una fecha estrictamente mayor (es decir, posterior) a la fecha actual"),
  INVALID_REQUEST_CALCULATION_IRRIGATION_WATER_NEED("No está permitido calcular la necesidad de agua de riego de un cultivo finalizado, en espera o muerto"),
  DATE_UNTIL_FUTURE_NOT_ALLOWED("La fecha hasta no debe ser estrictamente mayor (es decir, posterior) a la fecha actual (es decir, hoy)"),
  THERE_IS_NO_CROP_IN_DEVELOPMENT("No está permitido crear un registro de riego para una parcela que no tiene un cultivo en desarrollo"),
  UNDEFINED_REGION_NAME("El nombre de la región debe estar definido"),
  INVALID_REGION_NAME("El nombre de una región debe empezar con una palabra formada únicamente por caracteres alfabéticos y puede tener más de una palabra formada únicamente por caracteres alfabéticos. Se permite el uso del punto para abreviar nombres, y el uso de la coma, y el punto y coma como separadores."),
  REGION_NAME_ALREADY_USED("Nombre de región ya utilizado, elija otro"),
  UNDEFINED_PARCEL_NAME_AND_CROP_NAME("La parcela y el cultivo deben estar definidos"),
  UNDEFINED_SOIL_NAME("El nombre del suelo debe estar definido"),
  INVALID_SOIL_NAME("El nombre de un suelo debe empezar con una palabra formada únicamente por caracteres alfabéticos y puede tener más de una palabra formada únicamente por caracteres alfabéticos"),
  SOIL_NAME_ALREADY_USED("Nombre de suelo ya utilizado, elija otro"),
  INVALID_APPARENT_SPECIFIC_WIGHT("El peso específico aparente debe ser mayor a cero"),
  INVALID_FIELD_CAPACITY("La capacidad de campo debe ser mayor a cero"),
  INVALID_PERMANENT_WILTING_POINT("El punto de marchitez permanente debe ser mayor a cero"),
  INVALID_INITIAL_STAGE("La etapa inicial debe ser mayor a cero"),
  INVALID_DEVELOPMENT_STAGE("La etapa de desarrollo debe ser mayor a cero"),
  INVALID_MIDDLE_STAGE("La etapa media debe ser mayor a cero"),
  INVALID_FINAL_STAGE("La etapa final debe ser mayor a cero"),
  INVALID_INITIAL_KC("El coeficiente inicial de cultivo (Kc inicial) debe ser mayor a 0.0"),
  INVALID_MIDDLE_KC("El coeficiente medio de cultivo (Kc medio) debe ser mayor a 0.0"),
  INVALID_FINAL_KC("El coeficiente final de cultivo (Kc final) debe ser mayor a 0.0"),
  CROP_NOT_FOUND("Cultivo inexistente"),
  SOIL_NOT_FOUND("Suelo inexistente"),
  REGION_NOT_FOUND("Región inexistente"),
  PARCEL_NOT_FOUND("Parcela inexistente"),
  TYPE_CROP_NOT_FOUND("Tipo de cultivo inexistente"),
  UNDEFINED_TYPE_CROP_NAME("El nombre del tipo de cultivo debe estar definido"),
  UNDEFINED_MONTH_NAME("El nombre del mes debe estar definido"),
  LOWER_LIMIT_MAXIMUM_ROOT_DEPTH_INVALID("El límite inferior de la profundidad radicular máxima debe ser mayor a 0.0"),
  UPPER_LIMIT_MAXIMUM_ROOT_DEPTH_INVALID("El límite superior de la profundidad radicular máxima debe ser mayor a 0.0"),
  INVALID_DEPLETION_FACTOR("El factor de agotamiento debe tener un valor entre 0.1 y 0.8"),
  UNDEFINED_SOIL("Para calcular la necesidad de agua de riego de un cultivo en la fecha actual (es decir, hoy) con datos de suelo es necesario asignar un suelo a la parcela"),
  OVERLAPPING_ROOT_DEPTH_LIMITS("El límite inferior de la profundidad radicular máxima no debe ser mayor o igual al límite superior de la profundidad radicular máxima"),
  MONTH_START_PLANTING_NON_EXISTENT("El mes de inicio de siembra elegido no existe"),
  NON_EXISTENT_END_PLANTING_MONTH("El mes de fin de siembra elegido no existe"),
  OVERLAP_BETWEEN_MONTH_START_PLANTING_AND_MONTH_END_PLANTING("El mes de inicio de siembra no debe ser mayor al mes de fin de siembra"),
  INVALID_API_KEY("La clave para solicitar datos meteorológicos al servicio meteorológico Visual Crossing Weather, los cuales son necesarios para calcular la necesidad de agua de riego de un cultivo en la fecha actual (es decir, hoy), no es la correcta"),
  REQUEST_LIMIT_EXCEEDED("La aplicación no puede calcular la necesidad de agua de riego de un cultivo porque se supero la cantidad de 1000 peticiones gratuitas por día del servicio meteorológico Visual Crossing Weather"),
  WEATHER_SERVICE_UNAVAILABLE("La aplicación no puede calcular la necesidad de agua de riego de un cultivo porque el servicio meteorológico Visual Crossing Weather no se encuentra en funcionamiento"),
  UNKNOW_ERROR_IN_IRRIGATION_WATER_NEED_CALCULATION("Se produjo un error al calcular la necesidad de agua de riego de un cultivo"),
  UNDEFINED_PARCEL_NAME_AND_DATE("La parcela y la fecha deben estar definidas"),
  EXISTING_STATISTICAL_REPORT("Ya existe un informe estadístico para las fechas y la parcela elegidas"),
  DATE_FROM_STRICTLY_GREATER_THAN_DATE_UNTIL("La fecha desde no debe ser estrictamente mayor a la fecha hasta"),
  NON_EXISTENT_PARCEL("La parcela seleccionada no existe"),
  DATE_FROM_GREATEST_TO_MAXIMUM("La fecha desde no debe ser estrictamente mayor a 9999"),
  DATE_UNTIL_GREATEST_TO_MAXIMUM("La fecha hasta no debe ser estrictamente mayor a 9999"),
  DATE_GREATEST_TO_MAXIMUM("La fecha no debe ser estrictamente mayor a 9999");

  private final String reason;

  private ReasonError(String reason) {
    this.reason = reason;
  }

  public String getReason() {
    return reason;
  }

}
