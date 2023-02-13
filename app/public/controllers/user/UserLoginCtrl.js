app.controller(
    "UserLoginCtrl",
    ["$scope", "$location", "AuthSrv", "JwtManager", "AuthHeaderManager", "AccessManager", "ErrorResponseManager",
        function ($scope, $location, authService, jwtManager, authHeaderManager, accessManager, errorResponseManager) {

            $scope.login = function () {
                authService.authenticateUser($scope.data, function (error, data) {
                    /*
                    Si la autenticacion del usuario falla por uno de los siguientes motivos:
                    1. No hay una cuenta registrada con el nombre de usuario dado. En otras palabras, no existe el usuario ingresado.
                    2. El nombre de usuario y/o la contraseña ingresados son incorrectos.

                    No se debe redirigir al usuario a la pagina de inicio. En otras palabras, no se le debe mostrar la pagina de
                    inicio.
                    */
                    if (error) {
                        console.log(error);
                        errorResponseManager.checkResponse(error);
                        return;
                    }

                    /*
                    Si el flujo de ejecucion de esta funcion llega a este punto, es porque la autenticacion
                    del usuario fue exitosa. Por lo tanto, se almacena el JWT, devuelto por el servidor,
                    en el almacenamiento local del navegador web y se redirecciona al usuario a la
                    pagina de inicio.
                    */
                    jwtManager.setJwt(data.jwt);

                    /*
                    Cuando el usuario inicia sesion satisfactoriamente, se establece su JWT en el
                    encabezado de autorizacion HTTP para todas las peticiones HTTP, ya que se usa
                    JWT para la autenticacion, la autorizacion y las operaciones con datos
                    */
                    authHeaderManager.setJwtAuthHeader();
                    $location.path("/home");
                });
            }

        }]);
