app.controller(
  "ParcelsCtrl",
  ["$scope", "$location", "$route", "ParcelSrv", "AccessManager", "ErrorResponseManager", "AuthHeaderManager", "LogoutManager",
    function ($scope, $location, $route, parcelService, accessManager, errorResponseManager, authHeaderManager, logoutManager) {

      console.log("ParcelsCtrl loaded...")

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

      function findAll() {
        parcelService.findAll(function (error, data) {
          if (error) {
            console.log("Ocurrió un error: " + error);
            errorResponseManager.checkResponse(error);
            return;
          }

          $scope.data = data;
        })
      }

      $scope.delete = function (id) {
        console.log("Deleting: " + id)

        parcelService.delete(id, function (error, data) {
          if (error) {
            console.log(error);
            errorResponseManager.checkResponse(error);
            return;
          }

          $location.path("/home/parcels");
          $route.reload()
        });
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

      const UNDEFINED_PARCEL_NAME = "El nombre de la parcela debe estar definido";

      $scope.searchParcel = function () {
        /*
        Si esta propiedad de $scope tiene el valor undefined y se
        presiona el boton "Buscar", significa que NO se ingreso un
        nombre en el campo de busqueda para realizar la busqueda de
        un dato correspondiente a este controller. Por lo tanto, la
        aplicacion muestra el mensaje dado y no ejecuta la instruccion
        que realiza la peticion HTTP correspondiente esta funcion.
        */
        if ($scope.parcelName == undefined) {
          alert(UNDEFINED_PARCEL_NAME);
          return;
        }

        parcelService.search($scope.parcelName, function (error, data) {
          if (error) {
            console.log(error);
            $scope.parcelName = undefined;
            errorResponseManager.checkSearchResponse(error);
            return;
          }

          $scope.data = data;
        })
      }

      /* Esto es necesario para la paginacion */
      var $ctrl = this;

      $scope.service = parcelService;
      $scope.listElement = []
      $scope.cantPerPage = 20
      /* Esto es necesario para la paginacion */

      /*
      Reinicia el listado de los datos correspondientes a este controller
      cuando se presiona el boton "Reiniciar listado". Esto significa que
      recupera todos los datos correspondientes a este controller.
      */
      $scope.reset = function () {
        /*
        Esta instruccion es para eliminar el contenido del campo
        del menu de busqueda de un dato correspondientes a este
        controller
        */
        $scope.parcelName = undefined;
        findAll();
      }

      // findAll();
    }]);
