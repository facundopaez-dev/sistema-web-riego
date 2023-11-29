app.controller(
    "RegionCtrl",
    ["$scope", "$location", "$routeParams", "RegionSrv", "AccessManager", "ErrorResponseManager", "AuthHeaderManager", "LogoutManager",
        "ExpirationManager", "RedirectManager",
        function ($scope, $location, $params, regionService, accessManager, errorResponseManager, authHeaderManager, logoutManager, expirationManager,
            redirectManager) {

            console.log("RegionCtrl loaded with action: " + $params.action)

            /*
            Si el usuario NO tiene una sesion abierta, se le impide el acceso a
            la pagina web correspondiente a este controller y se lo redirige a
            la pagina web de inicio de sesion correspondiente
            */
            if (!accessManager.isUserLoggedIn()) {
                $location.path("/admin");
                return;
            }

            /*
            Si el usuario que tiene una sesion abierta no tiene permiso de administrador,
            no se le da acceso a la pagina correspondiente a este controller y se lo redirige
            a la pagina de inicio del usuario
            */
            if (accessManager.isUserLoggedIn() && !accessManager.loggedAsAdmin()) {
                $location.path("/home");
                return;
            }

            /*
            Cada vez que el usuario presiona los botones para crear, editar o
            ver un dato correspondiente a este controller, se debe comprobar
            si su JWT expiro o no. En el caso en el que JWT expiro, se redirige
            al usuario a la pagina web de inicio de sesion correspondiente. En caso
            contrario, se realiza la accion solicitada por el usuario mediante
            el boton pulsado.
            */
            if (expirationManager.isExpire()) {
                expirationManager.displayExpiredSessionMessage();

                /*
                Elimina el JWT del usuario del almacenamiento local del navegador
                web y del encabezado de autorizacion HTTP, ya que un JWT expirado
                no es valido para realizar peticiones HTTP a la aplicacion del
                lado servidor
                */
                expirationManager.clearUserState();

                /*
                Redirige al usuario a la pagina web de inicio de sesion en funcion
                de si inicio sesion como usuario o como administrador. Si inicio
                sesion como usuario, redirige al usuario a la pagina web de
                inicio de sesion del usuario. En cambio, si inicio sesion como
                administrador, redirige al administrador a la pagina web de
                inicio de sesion del administrador.
                */
                redirectManager.redirectUser();
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

            if (['new', 'edit'].indexOf($params.action) == -1) {
                alert("Acción inválida: " + $params.action);
                $location.path("/adminHome/regions");
            }

            function find(id) {
                regionService.find(id, function (error, data) {
                    if (error) {
                        console.log(error);
                        errorResponseManager.checkResponse(error);
                        return;
                    }

                    $scope.data = data;
                });
            }

            const EMPTY_FORM = "Debe completar todos los campos del formulario";
            const UNDEFINED_REGION_NAME = "El nombre de la región debe estar definido";
            const INVALID_REGION_NAME = "El nombre de una región debe empezar con una palabra formada únicamente por caracteres alfabéticos y puede tener más de una palabra formada únicamente por caracteres alfabéticos. Se permite el uso del punto para abreviar nombres, y el uso de la coma, y el punto y coma como separadores.";

            $scope.create = function () {
                // Expresion regular para validar el nombre de la region
                var nameRegexp = /^[A-Za-zÀ-ÿ]+[.]{0,1}[,]{0,1}[;]{0,1}(\s[A-Za-zÀ-ÿ]+[.]{0,1}[,]{0,1}[;]{0,1})*$/g;

                /*
                Si la propeidad data de $scope tiene el valor undefined,
                significa que el formulario correspondiente a esta funcion
                esta totalmente vacio. Por lo tanto, la aplicacion muestra
                el mensaje dado y no realiza la operacion solicitada.
                */
                if ($scope.data == undefined) {
                    alert(EMPTY_FORM);
                    return;
                }

                /*
                Si el nombre del dato correspondiente a este controller
                NO esta definido, la aplicacion muestra el mensaje dado
                y no realiza la operacion solicitada
                */
                if ($scope.data.name == undefined) {
                    alert(UNDEFINED_REGION_NAME);
                    return;
                }

                /*
                Si el nombre del dato correspondiente a este controller
                NO contiene unicamente letras, y un espacio en blanco entre
                palabra y palabra si esta formado por mas de una palabra, la
                aplicacion muestra el mensaje dado y no realiza la operacion
                solicitada
                */
                if (!nameRegexp.exec($scope.data.name)) {
                    alert(INVALID_REGION_NAME);
                    return;
                }

                regionService.create($scope.data, function (error, data) {
                    if (error) {
                        console.log(error);
                        errorResponseManager.checkResponse(error);
                        return;
                    }

                    $scope.data = data;
                    $location.path("/adminHome/regions")
                });
            }

            $scope.modify = function () {
                // Expresion regular para validar el nombre de la región
                var nameRegexp = /^[A-Za-zÀ-ÿ]+[.]{0,1}[,]{0,1}[;]{0,1}(\s[A-Za-zÀ-ÿ]+[.]{0,1}[,]{0,1}[;]{0,1})*$/g;

                /*
                Si el nombre del dato correspondiente a este controller
                NO contiene unicamente letras, y un espacio en blanco entre
                palabra y palabra si esta formado por mas de una palabra, la
                aplicacion muestra el mensaje dado y no realiza la operacion
                solicitada
                */
                if (!nameRegexp.exec($scope.data.name)) {
                    alert(INVALID_REGION_NAME);
                    return;
                }

                regionService.modify($scope.data, function (error, data) {
                    if (error) {
                        console.log(error);
                        errorResponseManager.checkResponse(error);
                        return;
                    }

                    $scope.data = data;
                    $location.path("/adminHome/regions")
                });
            }

            $scope.cancel = function () {
                $location.path("/adminHome/regions");
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

            $scope.action = $params.action;

            if ($scope.action == 'edit') {
                find($params.id);
            }

        }]);
