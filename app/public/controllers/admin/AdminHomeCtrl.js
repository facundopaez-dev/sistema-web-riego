app.controller(
    "AdminHomeCtrl",
    ["$scope", "$location", "AuthHeaderManager", "AccessManager", "LogoutManager", "ExpirationManager", "RedirectManager",
        function ($scope, $location, authHeaderManager, accessManager, logoutManager, expirationManager, redirectManager) {

            console.log("AdminHomeCtrl loaded...")

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
            Cuando el usuario accede a la pagina de inicio, se debe comprobar
            si su JWT expiro o no. En el caso en el que JWT expiro, se redirige
            al usuario a la pagina web de inicio de sesion correspondiente. En caso
            contrario, se deja al usuario en la pagina de inicio.
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

        }]);