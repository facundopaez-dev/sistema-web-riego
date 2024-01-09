app.controller(
    "WaterNeedFormCtrl",
    ["$scope", "$location", "$routeParams", "PlantingRecordSrv", "IrrigationRecordSrv", "WaterNeedFormManager", "AccessManager", "ErrorResponseManager", "AuthHeaderManager",
        "LogoutManager",
        function ($scope, $location, $params, plantingRecordSrv, irrigationRecordService, waterNeedFormManager, accessManager, errorResponseManager, authHeaderManager,
            logoutManager) {

            console.log("Cargando WaterNeedFormCtrl...")

            /*
            Si el usuario NO tiene una sesion abierta, se le impide el acceso a
            la pagina web correspondiente a este controller y se lo redirige a
            la pagina web de inicio de sesion correspondiente
            */
            if (!accessManager.isUserLoggedIn()) {
                $location.path("/");
                return;
            }

            /*
            Si el usuario que tiene una sesion abierta tiene permiso de
            administrador, se lo redirige a la pagina de inicio del
            administrador. De esta manera, un administrador debe cerrar
            la sesion que abrio a traves de la pagina web de inicio de sesion
            del administrador, y luego abrir una sesion a traves de la pagina
            web de inicio de sesion del usuario para poder acceder a la pagina
            de inicio del usuario.
            */
            if (accessManager.isUserLoggedIn() && accessManager.loggedAsAdmin()) {
                $location.path("/adminHome");
                return;
            }

            /*
            Cuando el usuario abre una sesion satisfactoriamente y no la cierra,
            y accede a la aplicacion web mediante una nueva pestaña, el encabezado
            de autorizacion HTTP tiene el valor undefined. En consecuencia, las
            peticiones HTTP con este encabezado no seran respondidas por la
            aplicacion del lado servidor, ya que esta opera con JWT para la
            autenticacion, la autorizacion y las operaciones con recursos
            (lectura, modificacion y creacion).

            Este es el motivo por el cual se hace este control. Si el encabezado
            HTTP de autorizacion tiene el valor undefined, se le asigna el JWT
            del usuario.

            De esta manera, cuando el usuario abre una sesion satisfactoriamente
            y no la cierra, y accede a la aplicacion web mediante una nueva pestaña,
            el encabezado HTTP de autorizacion contiene el JWT del usuario, y, por
            ende, la peticion HTTP que se realice en la nueva pestaña, sera respondida
            por la aplicacion del lado servidor.
            */
            if (authHeaderManager.isUndefined()) {
                authHeaderManager.setJwtAuthHeader();
            }

            /*
            Si el registro de plantacion correspondiente a un ID NO tiene
            un estado en desarrollo (en desarrollo, desarrollo optimo,
            desarrollo en riesgo de marchitez, desarrollo en marchitez), se
            impide el acceso al formulario del calculo de la necesidad de
            agua de riego de un cultivo redireccionando a la pagina de
            registros de plantacion
            */
            if (!waterNeedFormManager.isInDevelopment($params.id)) {
                $location.path("/home/plantingRecords");
                return;
            }

            function calculateIrrigationWaterNeed(id) {
                plantingRecordSrv.calculateIrrigationWaterNeed(id, function (error, irrigationWaterNeedData) {
                    if (error) {
                        console.log(error);
                        errorResponseManager.checkResponse(error);
                        return;
                    }

                    /*
                    Si esta instruccion no esta, no se puede ver la
                    necesidad de agua de riego en el formulario del
                    calculo de la necesidad de agua de riego de un
                    cultivo
                    */
                    $scope.irrigationWaterNeedData = irrigationWaterNeedData;
                });
            }

            $scope.saveIrrigationWaterNeedData = function () {

                /*
                Este control es para el caso en el que el usuario presiona
                el boton "Aceptar" del formulario del calculo de la necesidad
                de agua de riego de un cultivo en la fecha actual con los
                campos vacios
                */
                if ($scope.irrigationWaterNeedData == undefined) {
                    return;
                }

                if ($scope.irrigationWaterNeedData.irrigationDone >= 0) {
                    irrigationRecordService.saveIrrigationWaterNeedData($scope.irrigationWaterNeedData, function (error, irrigationWaterNeedData) {
                        if (error) {
                            console.log(error);
                            errorResponseManager.checkResponse(error);
                            return;
                        }

                        $scope.irrigationWaterNeedData = irrigationWaterNeedData;
                        $location.path("/home/plantingRecords");
                    });
                } else {
                    alert("El riego realizado debe ser mayor o igual a cero");
                }

            }

            $scope.logout = function () {
                /*
                LogoutManager es la factory encargada de realizar el cierre de
                sesion del usuario. Durante el cierre de sesion, la funcion
                logout de la factory mencionada, realiza la peticion HTTP de
                cierre de sesion (elimina logicamente la sesion activa del
                usuario en la base de datos, la cual, esta en el lado servidor),
                la eliminacion del JWT del usuario, el borrado del contenido del
                encabezado HTTP de autorizacion, el establecimiento en false del
                valor asociado a la clave "superuser" del almacenamiento local del
                navegador web y la redireccion a la pagina web de inicio de sesion
                correspondiente dependiendo si el usuario inicio sesion como
                administrador o no.
                */
                logoutManager.logout();
            }

            $scope.cancel = function () {
                $location.path("/home/plantingRecords");
            }

            calculateIrrigationWaterNeed($params.id);
        }]);
