app.controller(
  "EmailPasswordRecoveryCtrl",
  ["$scope", "$location", "UserSrv", "ErrorResponseManager",
    function ($scope, $location, userService, errorResponseManager,) {

      console.log("EmailPasswordRecoveryCtrl loaded...")

      const UNDEFINED_EMAIL = "La dirección de correo electrónico debe estar definida";
      const MALFORMED_EMAIL = "La dirección de correo electrónico no es válida";

      $scope.sendEmailPasswordRecovery = function () {
        // Expresion regular para validar el correo electronico
        var emailRegexp = /^(?=.{1,64}@)[a-z0-9_-]+(\.[a-z0-9_-]+)*@[^-][a-z0-9-]+(\.[a-z0-9-]+)*(\.[a-z]{2,})$/g;

        /*
        Si la propiedad data de $scope tiene el valor undefined,
        significa que el usuario NO completo el campo del correo
        electronico, por lo tanto, la aplicacion muestra el mensaje
        dado y no realiza la peticion HTTP correspondiente a este
        controller
        */
        if ($scope.data == undefined) {
          alert(UNDEFINED_EMAIL);
          return;
        }

        /*
        Si la direccion de correo electronico NO es valida, la aplicacion
        muestra el mensaje "La direccion de correo electronico no es valida"
        y no ejecuta la instruccion que realiza la peticion HTTP correspondiente
        a este controller
        */
        if (!emailRegexp.exec($scope.data.email)) {
          alert(MALFORMED_EMAIL);
          return;
        }

        userService.sendEmailPasswordRecovery($scope.data, function (error) {
          if (error) {
            console.log("Ocurrió un error: " + error);
            errorResponseManager.checkResponse(error);
            return;
          }

          alert("Correo electrónico de restablecimiento de contraseña enviado a su casilla de correo electrónico");
          $location.path("/");
        })
      }

      $scope.cancel = function () {
        $location.path("/");
      }

    }]);
