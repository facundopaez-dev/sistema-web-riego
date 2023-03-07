app.controller(
  "ResetPasswordCtrl",
  ["$scope", "$location", "$routeParams", "UserSrv", "ErrorResponseManager",
    function ($scope, $location, $params, userService, errorResponseManager) {

      console.log("ResetPasswordCtrl loaded...");

      const EMPTY_FORM = "Debe completar todos los campos del formulario";
      const UNDEFINED_NEW_PASSWORD = "La nueva contraseña debe estar definida";
      const UNDEFINED_CONFIRMED_NEW_PASSWORD = "La confirmación de la nueva contraseña debe estar definida";
      const MALFORMED_NEW_PASSWORD = "La nueva contraseña debe tener como mínimo 8 caracteres de longitud, una letra minúscula, una letra mayúscula y un número de 0 a 9, con o sin caracteres especiales";
      const INCORRECTLY_CONFIRMED_NEW_PASSWORD = "La confirmación de la nueva contraseña no es igual a la nueva contraseña ingresada";

      $scope.resetPassword = function () {
        // Expresion regular para validar la nueva contraseña
        var newPasswordRegexp = /^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{7,}$/g;

        /*
        Si la propiedad data de $scope tiene el valor undefined,
        significa que todos los campos del formulario correspondiente
        a este controller estan vacios, por lo tanto, la aplicacion
        muestra el mensaje dado y no ejecuta la instruccion que realiza
        la peticion HTTP correspondiente a este contoller
        */
        if ($scope.data == undefined) {
          alert(EMPTY_FORM);
          return;
        }

        /*
        Si la nueva contraseña NO esta definida, la aplicacion muestra
        el mensaje dado y no ejecuta la instruccion que realiza la
        peticion HTTP correspondiente a este controller
        */
        if ($scope.data.newPassword == undefined) {
          alert(UNDEFINED_NEW_PASSWORD);
          return;
        }

        /*
        Si la confirmacion de la nueva contraseña NO esta definida, la
        aplicacion muestra el mensaje dado y no ejecuta la instruccion
        que realiza la peticion HTTP correspondiente a este controller
        */
        if ($scope.data.newPasswordConfirmed == undefined) {
          alert(UNDEFINED_CONFIRMED_NEW_PASSWORD);
          return;
        }

        /*
        Si la nueva contraseña NO contiene como minimo 8 caracteres de longitud,
        una letra minuscula, una letra mayuscula y un numero 0 a 9, la
        aplicacion muestra el siguiente mensaje y no ejecuta la instruccion
        que realiza la peticion HTTP correspondiente a este controller.
        
        "La nueva contraseña debe tener como minimo 8 caracteres de longitud, una
        letra minuscula, una letra mayuscula y un numero de 0 a 9, con o sin
        caracteres especiales" y no se realiza la operacion solicitada.
        */
        if (!newPasswordRegexp.exec($scope.data.newPassword)) {
          alert(MALFORMED_NEW_PASSWORD);
          return;
        }

        /*
        Si la nueva contraseña y la confirmacion de la nueva contraseña NO
        coinciden, la aplicacion muestra el mensaje "La confirmacion de la
        nueva contraseña no es igual a la nueva contraseña ingresada" y no
        ejecuta la instruccion que realiza la peticion HTTP correspondiente
        a este controller
        */
        if (!($scope.data.newPassword == $scope.data.newPasswordConfirmed)) {
          alert(INCORRECTLY_CONFIRMED_NEW_PASSWORD);
          return;
        }

        userService.resetPassword($params.jwtResetPassword, $scope.data, function (error) {
          if (error) {
            console.log("Ocurrió un error: " + error);
            errorResponseManager.checkResponse(error);
            $location.path("/");
            return;
          }

          alert("Contraseña restablecida satisfactoriamente");
          $location.path("/");
        })
      }

      $scope.cancel = function () {
        $location.path("/");
      }

      /*
      Realiza controles sobre un enlace de restablecimiento de contraseña,
      los cuales, son los siguientes:
      - comprobar su existencia en la base de subyacente,
      - comprobar si fue consumido, y
      - comprobar si expiro.

      Un enlace de restablecimiento de contraseña es consumido
      cuando el usuario que solicito dicho restablecimiento,
      accede a dicho enlace y restablece su contraseña.
      */
      function checkPasswordResetLink() {
        userService.checkPasswordResetLink($params.jwtResetPassword, function (error) {
          if (error) {
            console.log("Ocurrió un error: " + error);
            errorResponseManager.checkResponse(error);
            $location.path("/");
            return;
          }

        })
      }

      checkPasswordResetLink();
    }]);
