app.controller(
  "SignupCtrl",
  ["$scope", "$location", "SignupSrv", "ErrorResponseManager", function ($scope, $location, signupSrv, errorResponseManager) {

    console.log("SignupCtrl loaded...");

    const EMPTY_SIGN_UP_FORM = "Debe completar todos los campos del formulario";
    const UNDEFINED_USERNAME = "El nombre de usuario debe estar definido";
    const UNDEFINED_NAME = "El nombre debe estar definido";
    const UNDEFINED_LAST_NAME = "El apellido debe estar definido";
    const UNDEFINED_EMAIL = "La dirección de correo electrónico debe estar definida";
    const UNDEFINED_PASSWORD = "La contraseña debe estar definida";
    const UNDEFINED_CONFIRMED_PASSWORD = "La confirmación de la contraseña debe estar definida";
    const MALFORMED_USERNAME = "El nombre debe usuario debe tener una longitud de entre 4 y 15 caracteres, comenzar con caracteres alfabéticos seguido o no de números y/o guiones bajos";
    const MALFORMED_NAME = "El nombre debe tener una longitud de entre 3 y 30 caracteres alfabéticos, empezar con una letra mayúscula seguido de letras minúsculas, tener un espacio en blanco entre nombre y nombre si hay más de un nombre, y los nombres que vienen después del primero deben empezar con una letra mayúscula seguido de letras minúsculas";
    const MALFORMED_LAST_NAME = "El apellido debe tener una longitud de entre 3 y 30 caracteres alfabéticos, empezar con una letra mayúscula seguido de letras minúsculas, tener un espacio en blanco entre apellido y apellido si hay más de un apellido, y los apellidos que vienen después del primero deben empezar con una letra mayúscula seguido de letras minúsculas";
    const MALFORMED_EMAIL = "La dirección de correo electrónico no es válida";
    const MALFORMED_PASSWORD = "La contraseña debe tener como mínimo 8 caracteres de longitud, una letra minúscula, una letra mayúscula y un número de 0 a 9, con o sin caracteres especiales";
    const INCORRECTLY_CONFIRMED_PASSWORD = "La confirmación de la contraseña no es igual a la contraseña ingresada";

    $scope.signup = function () {
      /*
      Expresiones regulares para validar los datos de entrada
      del formulario de registro de usuario
      */
      var usernameRegexp = /^[A-Za-z][A-Za-z0-9_]{3,14}$/g;
      var nameRegexp = /^[A-Z](?=.{2,29}$)[a-z]+(?:\s[A-Z][a-z]+)*$/g;
      var lastNameRegexp = /^[A-Z](?=.{2,29}$)[a-z]+(?:\s[A-Z][a-z]+)*$/g;
      var emailRegexp = /^(?=.{1,64}@)[a-z0-9_-]+(\.[a-z0-9_-]+)*@[^-][a-z0-9-]+(\.[a-z0-9-]+)*(\.[a-z]{2,})$/g;
      var passwordRegexp = /^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{7,}$/g;

      /*
      Si la propiedad data de $scope tiene el valor undefined,
      significa que el usuario presiono el boton "Registrarse"
      con todos los campos vacios del formulario de registro,
      por lo tanto, la aplicacion muestra el mensaje dado y no
      ejecuta la instruccion que realiza la peticion HTTP para
      registrar a un usuario
      */
      if ($scope.data == undefined) {
        alert(EMPTY_SIGN_UP_FORM);
        return;
      }

      /*
      Si el nombre de usuario NO esta definido cuando el
      usuario presiona el boton "Registrarse", la aplicacion
      muestra el mensaje "El nombre de usuario debe estar definido"
      y no ejecuta la instruccion que realiza la peticion HTTP
      para registrar a un usuario
      */
      if ($scope.data.username == undefined) {
        alert(UNDEFINED_USERNAME);
        return;
      }

      /*
      Si el nombre NO esta definido cuando el usuario presiona
      el boton "Registrarse", la aplicacion muestra el mensaje
      "El nombre debe estar definido" y no ejecuta la instruccion
      que realiza la peticion HTTP para registrar a un usuario
      */
      if ($scope.data.name == undefined) {
        alert(UNDEFINED_NAME);
        return;
      }

      /*
      Si el apellido NO esta definido cuando el usuario presiona
      el boton "Registrarse", la aplicacion muestra el mensaje
      "El apellido debe estar definido" y no ejecuta la instruccion
      que realiza la peticion HTTP para registrar a un usuario
      */
      if ($scope.data.lastName == undefined) {
        alert(UNDEFINED_LAST_NAME);
        return;
      }

      /*
      Si la direccion de correo electronico NO esta definida
      cuando el usuario presiona el boton "Registrarse", la
      aplicacion muestra el mensaje "La direccion de correo
      electronico debe estar definida" y no ejecuta la instruccion
      que realiza la peticion HTTP para registrar a un usuario
      */
      if ($scope.data.email == undefined) {
        alert(UNDEFINED_EMAIL);
        return;
      }

      /*
      Si la contraseña NO esta definida cuando el usuario presiona
      el boton "Registrarse", la aplicacion muestra el mensaje
      "La contraseña debe estar definida" y no ejecuta la instruccion
      que realiza la peticion HTTP para registrar a un usuario
      */
      if ($scope.data.password == undefined) {
        alert(UNDEFINED_PASSWORD);
        return;
      }

      /*
      Si la confirmacion de la contraseña NO esta definida cuando
      el usuario presiona el boton "Registrarse", la aplicacion
      muestra el mensaje "La confirmacion de la contraseña debe
      estar definida" y no ejecuta la instruccion que realiza la
      peticion HTTP para registrar a un usuario
      */
      if ($scope.data.passwordConfirmed == undefined) {
        alert(UNDEFINED_CONFIRMED_PASSWORD);
        return;
      }

      /*
      Si el nombre de usuario NO tiene una longitud de entre 4
      y 15 caracteres, y NO empieza con caracteres alfabeticos
      con o sin numeros y/o guiones bajos, la aplicacion muestra
      el siguiente mensaje y no ejecuta la instruccion que realiza
      la peticion HTTP para registrar a un usuario:
      
      "El nombre debe usuario debe tener una longitud de entre 4 y
      15 caracteres, comenzar con caracteres alfabeticos seguido o
      no de numeros y/o guiones bajos".
      */
      if (!usernameRegexp.exec($scope.data.username)) {
        alert(MALFORMED_USERNAME);
        return;
      }

      /*
      Si el nombre NO tiene una longitud entre 3 y 30 caracteres
      alfabeticos, NO empieza con una letra mayuscula seguida de
      letras minusculas, NO tiene un espacio en blanco entre nombre
      y nombre en el caso en el que el usuario tenga mas de un nombre,
      y los nombres que vienen a continuacion del primero NO empiezan
      con una letra mayuscula seguida de letras minusculas, la aplicacion
      muestra el siguiente mensaje y no ejecuta la la instruccion que
      realiza la peticion HTTP para registrar a un usuario.
      
      "El nombre debe tener una longitud de entre 3 y 30 caracteres
      alfabeticos, empezar con una letra mayuscula seguido de letras
      minusculas, tener un espacio en blanco entre nombre y nombre si
      hay mas de un nombre, y los nombres que vienen despues del primero
      deben empezar con una letra mayuscula seguido de letras minusculas".
      */
      if (!nameRegexp.exec($scope.data.name)) {
        alert(MALFORMED_NAME);
        return;
      }

      /*
      Si el apellido NO tiene una longitud de entre 3 y 30 caracteres
      alfabeticos, NO empieza con una letra mayuscula seguida de
      letras minusculas, NO tiene un espacio en blanco entre apellido
      y apellido en el caso en el que el usuario tenga mas de un apellido,
      y los apellidos que vienen a continuacion del primero NO empiezan
      con una letra mayuscula seguida de letras minusculas, la aplicacion
      muestra el siguiente mensaje y no ejecuta la instruccion que realiza
      la peticion HTTP para registrar a un usuario.

      "El apellido debe tener una longitud de entre 3 y 30 caracteres
      alfabeticos, empezar con una letra mayuscula seguido de letras
      minusculas, tener un espacio en blanco entre apellido y apellido
      si hay mas de un apellido, y los apellidos que vienen despues del
      primero deben empezar con una letra mayuscula seguido de letras
      minusculas".
      */
      if (!lastNameRegexp.exec($scope.data.lastName)) {
        alert(MALFORMED_LAST_NAME);
        return;
      }

      /*
      Si la direccion de correo electronico NO es valida, la aplicacion
      muestra el mensaje "La direccion de correo electronico no es valida"
      y no ejecuta la instruccion que realiza la solicitud HTTP para
      registrar a un usuario
      */
      if (!emailRegexp.exec($scope.data.email)) {
        alert(MALFORMED_EMAIL);
        return;
      }

      /*
      Si la contraseña NO contiene como minimo 8 caracteres de longitud,
      una letra minuscula, una letra mayuscula y un numero 0 a 9, la
      aplicacion muestra el siguiente mensaje y no ejecuta la instruccion
      querealiza la solicitud HTTP para registrar a un usuario.
      
      "La contraseña debe tener como minimo 8 caracteres de longitud, una
      letra minuscula, una letra mayuscula y un numero de 0 a 9, con o sin
      caracteres especiales" y no se realiza la operacion solicitada.
      */
      if (!passwordRegexp.exec($scope.data.password)) {
        alert(MALFORMED_PASSWORD);
        return;
      }

      /*
      Si la contraseña y la confirmacion de la contraseña NO coinciden,
      la aplicacion muestra el mensaje "La confirmacion de la contraseña
      no es igual a la contraseña ingresada" y no ejecuta la instruccion
      que realiza la peticion HTTP para registrar a un usuario
      */
      if (!($scope.data.password.toUpperCase() == $scope.data.passwordConfirmed.toUpperCase())) {
        alert(INCORRECTLY_CONFIRMED_PASSWORD);
        return;
      }

      signupSrv.signup($scope.data, function (error) {
        if (error) {
          console.log(error);
          errorResponseManager.checkResponse(error);
          return;
        }

        alert("Cuenta registrada satisfactoriamente, active su cuenta mediante el correo electrónico de confirmación de registro enviado a su casilla de correo electrónico");
        $location.path("/")
      });
    }

    $scope.cancel = function () {
      $location.path("/");
    }

  }]);
