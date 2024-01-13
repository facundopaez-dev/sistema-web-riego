app.controller(
  "StatisticalReportCtrl",
  ["$scope", "$location", "$routeParams", "StatisticalReportSrv", "ParcelSrv", "AccessManager", "ErrorResponseManager", "AuthHeaderManager", "LogoutManager",
    "ExpirationManager", "RedirectManager",
    function ($scope, $location, $params, statisticalReportService, parcelService, accessManager, errorResponseManager, authHeaderManager, logoutManager, expirationManager,
      redirectManager) {

      console.log("StatisticalReportCtrl loaded with action: " + $params.action)

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

      if (['new', 'view'].indexOf($params.action) == -1) {
        alert("Acción inválida: " + $params.action);
        $location.path("/home/statisticalReports");
      }

      const EMPTY_FORM = "Debe completar todos los campos del formulario";
      const UNDEFINED_DATES = "Las fechas deben estar definidas";
      const INDEFINITE_PARCEL = "La parcela debe estar definida";
      const DATE_FROM_AND_DATE_UNTIL_OVERLAPPING = "La fecha desde no debe ser mayor o igual a la fecha hasta";
      const DATE_UNTIL_FUTURE_NOT_ALLOWED = "La fecha hasta no debe ser estrictamente mayor (es decir, posterior) a la fecha actual (es decir, hoy)";

      function find(id) {
        statisticalReportService.find(id, function (error, data) {
          if (error) {
            console.log(error);
            errorResponseManager.checkResponse(error);
            return;
          }

          $scope.data = data;

          if ($scope.data.dateFrom != null) {
            $scope.data.dateFrom = new Date($scope.data.dateFrom);
          }

          if ($scope.data.dateUntil != null) {
            $scope.data.dateUntil = new Date($scope.data.dateUntil);
          }

        });
      }

      $scope.create = function () {
        /*
        Si la propiedad data de $scope tiene el valor undefined,
        significa que los campos del formulario correspondiente
        a este controller, estan vacios. Por lo tanto, la aplicacion
        muestra el mensaje dado y no ejecuta la instruccion que
        realiza la peticion HTTP correspondiente a esta funcion.
        */
        if ($scope.data == undefined) {
          alert(EMPTY_FORM);
          return;
        }

        /*
        Si una de las fechas NO esta definida, la aplicacion muestra
        el mensaje dado y no ejecuta la instruccion que realiza la
        peticion HTTP correspondiente a esta funcion
        */
        if ($scope.data.dateFrom == undefined || $scope.data.dateUntil == undefined) {
          alert(UNDEFINED_DATES);
          return;
        }

        /*
        Si la parcela NO esta definida, la aplicacion muestra el
        mensaje dado y no ejecuta la instruccion que realiza la
        peticion HTTP correspondiente a esta funcion
        */
        if ($scope.data.parcel == undefined) {
          alert(INDEFINITE_PARCEL);
          return;
        }

        var currentDate = new Date();

        /*
        Si la fecha hasta es posterior a la fecha actual, la aplicacion
        muestra el mensaje dado y no ejecuta la instruccion que realiza
        la peticion HTTP correspondiente a esta funcion
        */
        if ($scope.data.dateUntil > currentDate) {
          alert(DATE_UNTIL_FUTURE_NOT_ALLOWED);
          return;
        }

        /*
        Si la fecha desde es mayor o igual a la fecha hasta, la aplicacion
        muestra el mensaje dado y no ejecuta la instruccion que realiza la
        peticion HTTP correspondiente a esta funcion
        */
        if ($scope.data.dateFrom >= $scope.data.dateUntil) {
          alert(DATE_FROM_AND_DATE_UNTIL_OVERLAPPING);
          return;
        }

        statisticalReportService.create($scope.data, function (error, data) {
          if (error) {
            console.log(error);
            errorResponseManager.checkResponse(error);
            return;
          }

          $scope.data = data;

          if ($scope.data.dateFrom != null) {
            $scope.data.dateFrom = new Date($scope.data.dateFrom);
          }

          if ($scope.data.dateUntil != null) {
            $scope.data.dateUntil = new Date($scope.data.dateUntil);
          }

          $location.path("/home/statisticalReports")
        });
      }

      $scope.cancel = function () {
        $location.path("/home/statisticalReports");
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

      // Esto es necesario para la busqueda que se hace cuando se ingresan caracteres
      $scope.findActiveParcelByName = function (parcelName) {
        return parcelService.findActiveParcelByName(parcelName).
          then(function (response) {
            var parcels = [];
            for (var i = 0; i < response.data.length; i++) {
              parcels.push(response.data[i]);
            }

            return parcels;
          });;
      }

      if ($scope.action == 'view') {
        find($params.id);
      }

    }]);