app.controller(
  "PlantingRecordCtrl",
  ["$scope", "$location", "$route", "$routeParams", "PlantingRecordSrv", "CropSrv", "ParcelSrv", "AccessManager", "ErrorResponseManager", "AuthHeaderManager",
    "LogoutManager", "ExpirationManager", "RedirectManager", "UtilDate", function ($scope, $location, $route, $params, plantingRecordService, cropService, parcelService,
      accessManager, errorResponseManager, authHeaderManager, logoutManager, expirationManager, redirectManager, utilDate) {

      console.log("PlantingRecordCtrl loaded with action: " + $params.action)

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

      if (['new', 'edit', 'view'].indexOf($params.action) == -1) {
        alert("Acción inválida: " + $params.action);
        $location.path("/home/plantingRecords");
      }

      function find(id) {
        plantingRecordService.find(id, function (error, data) {
          if (error) {
            console.log(error);
            errorResponseManager.checkResponse(error);
            return;
          }

          $scope.data = data;

          if ($scope.data.seedDate != null) {
            $scope.data.seedDate = new Date($scope.data.seedDate);
            currentSeedDate = $scope.data.seedDate;
          }

          if ($scope.data.harvestDate != null) {
            $scope.data.harvestDate = new Date($scope.data.harvestDate);
          }
        });
      }

      const EMPTY_FORM = "Debe completar todos los campos del formulario";
      const UNDEFINED_PARCEL = "La parcela debe estar definida";
      const UNDEFINED_CROP = "El cultivo debe estar definido";
      const MODIFICATION_WITH_PAST_SEED_DATE_NOT_ALLOWED = "No está permitido modificar un registro de plantación con una fecha de siembra menor a la fecha actual (es decir, anterior a la fecha actual)";
      const MODIFICATION_WITH_FUTURE_SEED_DATE_NOT_ALLOWED = "No está permitido modificar un registro de plantación con una fecha de siembra mayor a la fecha actual (es decir, posterior a la fecha actual)";

      $scope.create = function () {
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
        **********************************
        Validacion de los datos de entrada
        **********************************
         */

        if ($scope.data.parcel == undefined) {
          alert(UNDEFINED_PARCEL);
          return;
        }

        if ($scope.data.crop == undefined) {
          alert(UNDEFINED_CROP);
          return;
        }

        plantingRecordService.create($scope.data, function (error, data) {
          if (error) {
            console.log(error);
            errorResponseManager.checkResponse(error);
            return;
          }

          $scope.data = data;

          if ($scope.data.seedDate != null) {
            $scope.data.seedDate = new Date($scope.data.seedDate);
          }

          if ($scope.data.harvestDate != null) {
            $scope.data.harvestDate = new Date($scope.data.harvestDate);
          }

          $location.path("/home/plantingRecords");
        });

      }

      /*
      A esta variable se le asigna la fecha de siembra original
      de un registro de plantacion cuando se busca un registro
      de plantacion mediante la funcion find para modificacion
      */
      var currentSeedDate = new Date();

      $scope.modify = function () {
        var currentDate = new Date();

        /*
        Si la fecha de siembra esta definida y es distinta a la fecha
        de siembra que tiene actualmente el registro de plantacion a
        modificar, se comprueba si es menor o mayor a la fecha actual
        */
        if (($scope.data.seedDate != undefined) && (utilDate.compareTo(currentSeedDate, $scope.data.seedDate) != 0)) {
          /*
          Si la fecha de siembra elegida es menor a la fecha actual,
          se muestra el mensaje dado y no se realiza la operacion
          solicitada
          */
          if (utilDate.compareTo($scope.data.seedDate, currentDate) < 0) {
            alert(MODIFICATION_WITH_PAST_SEED_DATE_NOT_ALLOWED);
            return;
          }

          /*
          Si la fecha de siembra elegida es mayor a la fecha actual,
          se muestra el mensaje dado y no se realiza la operacion
          solicitada
          */
          if (utilDate.compareTo($scope.data.seedDate, currentDate) > 0) {
            alert(MODIFICATION_WITH_FUTURE_SEED_DATE_NOT_ALLOWED);
            return;
          }

        }

        plantingRecordService.modify($scope.data, function (error, data) {
          if (error) {
            console.log(error);
            errorResponseManager.checkResponse(error);
            return;
          }

          $scope.data = data;
          $location.path("/home/plantingRecords")
          $route.reload();
        });
      }

      $scope.cancel = function () {
        $location.path("/home/plantingRecords");
      }

      function findAllActiveCrops() {
        cropService.findAllActive(function (error, crops) {
          if (error) {
            alert("Ocurrio un error: " + error);
            return;
          }

          $scope.crops = crops;
        })
      }

      function findAllActiveParcels() {
        parcelService.findAllActive(function (error, parcels) {
          if (error) {
            alert("Ocurrio un error: " + error);
            return;
          }

          $scope.parcels = parcels;
        })
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

      if ($scope.action == 'new' || $scope.action == 'edit' || $scope.action == 'view') {
        findAllActiveParcels();
        findAllActiveCrops();
      }

      if ($scope.action == 'edit' || $scope.action == 'view') {
        find($params.id);
      }

    }]);
