app.controller(
    "SoilWaterBalancesCtrl",
    ["$scope", "$location", "SoilWaterBalanceSrv", "ParcelSrv", "CropSrv", "AccessManager", "ErrorResponseManager", "AuthHeaderManager", "LogoutManager",
        function ($scope, $location, soilWaterBalanceSrv, parcelSrv, cropSrv, accessManager, errorResponseManager, authHeaderManager, logoutManager) {

            console.log("SoilWaterBalancesCtrl loaded...")

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
            web de inicio de sesion del usuario para poder acceder a la pagina web
            de creacion, edicion o visualizacion de un dato correspondiente
            a este controller.
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

            // Esto es necesario para la busqueda que se hace cuando se ingresan caracteres
            $scope.findParcelByName = function (parcelName) {
                return parcelSrv.findByName(parcelName).
                    then(function (response) {
                        var parcels = [];
                        for (var i = 0; i < response.data.length; i++) {
                            parcels.push(response.data[i]);
                        }

                        return parcels;
                    });
            }

            $scope.findCropByName = function (cropName) {
                return cropSrv.findByName(cropName).
                    then(function (response) {
                        var crops = [];
                        for (var i = 0; i < response.data.length; i++) {
                            crops.push(response.data[i]);
                        }

                        return crops;
                    });
            }

            const UNDEFINED_PARCEL_NAME_AND_CROP_NAME = "La parcela y el cultivo deben estar definidos";

            /*
            Trae el listado de balances hidricos de suelo que tienen
            el nombre de parcela, el nombre del cultivo, la fecha desde
            y la fecha hasta elegidos
            */
            $scope.retrieve = function () {
                /*
                Si las propiedades parcel y crop tienen tienen valor undefined,
                significa que NO se cargo una parcela y un cultivo en los campos
                de la parcela y del cultivo para recuperar balances hidricos de
                suelo. Por lo tanto, la aplicacion muestra el mensaje dado y no
                ejecuta instruccion que realiza la peticion HTTP correspondiente
                esta funcion.
                */
                if ($scope.parcel == undefined || $scope.crop == undefined) {
                    alert(UNDEFINED_PARCEL_NAME_AND_CROP_NAME);
                    return;
                }

                var newDateFrom = null;
                var newDateUntil = null;

                /*
                Si la fecha desde y/o la fecha hasta estan definidas (es decir,
                tienen un valor asignado), se crean variables con las fechas
                elegidas usando el formato yyyy-MM-dd. El motivo de esto es
                que el metodo findByFilterParameters de la clase REST
                SoilWaterBalanceRestServlet de la aplicacion del lado servidor,
                utiliza la fecha desde y la fecha hasta en el formato yyyy-MM-dd
                para recuperar balances hidricos de suelo.
                */
                if ($scope.dateFrom != undefined || $scope.dateFrom != null) {
                    newDateFrom = $scope.dateFrom.getFullYear() + "-" + ($scope.dateFrom.getMonth() + 1) + "-" + $scope.dateFrom.getDate();
                }

                if ($scope.dateUntil != undefined || $scope.dateUntil != null) {
                    newDateUntil = $scope.dateUntil.getFullYear() + "-" + ($scope.dateUntil.getMonth() + 1) + "-" + $scope.dateUntil.getDate();
                }

                soilWaterBalanceSrv.filter(newDateFrom, newDateUntil, $scope.parcel.name, $scope.crop.name, function (error, data) {
                    if (error) {
                        console.log(error);
                        errorResponseManager.checkResponse(error);
                        return;
                    }

                    $scope.data = data;
                })

            }

            $scope.clearSearchFields = function () {
                /*
                Estas instrucciones son para eliminar el contenido de los
                campos de filtracion
                */
                $scope.dateFrom = undefined;
                $scope.dateUntil = undefined;
                $scope.parcel = undefined;
                $scope.crop = undefined;
            }

        }]);
