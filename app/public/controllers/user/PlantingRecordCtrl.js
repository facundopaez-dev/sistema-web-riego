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
          }

          if ($scope.data.harvestDate != null) {
            $scope.data.harvestDate = new Date($scope.data.harvestDate);
          }
        });
      }

      const EMPTY_FORM = "Debe completar todos los campos del formulario";
      const UNDEFINED_SEED_DATE = "La fecha de siembra debe estar definida";
      const UNDEFINED_HARVEST_DATE = "La fecha de cosecha debe estar definida";
      const UNDEFINED_PARCEL = "La parcela debe estar definida";
      const UNDEFINED_CROP = "El cultivo debe estar definido";
      const OVERLAPPING_SEED_DATE_AND_HARVEST_DATE = "La fecha de siembra no debe ser mayor ni igual a la fecha de cosecha";

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

        if ($scope.data.seedDate == undefined) {
          alert(UNDEFINED_SEED_DATE);
          return;
        }

        if ($scope.data.harvestDate == undefined) {
          alert(UNDEFINED_HARVEST_DATE);
          return;
        }

        if (utilDate.compareTo($scope.data.seedDate, $scope.data.harvestDate) >= 0) {
          alert(OVERLAPPING_SEED_DATE_AND_HARVEST_DATE);
          return;
        }

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

      $scope.modify = function () {
        /*
        **********************************
        Validacion de los datos de entrada
        **********************************
         */

        if ($scope.data.seedDate == undefined) {
          alert(UNDEFINED_SEED_DATE);
          return;
        }

        if ($scope.data.harvestDate == undefined) {
          alert(UNDEFINED_HARVEST_DATE);
          return;
        }

        if (utilDate.compareTo($scope.data.seedDate, $scope.data.harvestDate) >= 0) {
          alert(OVERLAPPING_SEED_DATE_AND_HARVEST_DATE);
          return;
        }

        plantingRecordService.modify($scope.data, function (error, data) {
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

      function findAllCrops() {
        cropService.findAll(function (error, crops) {
          if (error) {
            console.log("Ocurrio un error: " + error);
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

      function findAllParcels() {
        parcelService.findAll(function (error, parcels) {
          if (error) {
            console.log("Ocurrio un error: " + error);
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

      if ($scope.action == 'new' || $scope.action == 'edit') {
        findAllActiveParcels();
        findAllActiveCrops();
      }

      if ($scope.action == 'edit' || $scope.action == 'view') {
        find($params.id);
      }

      /*
      En la visualizacion de un dato correspondiente a este
      controller se debe poder visualizar la parcela y el
      cultivo al que pertenece un dato, independientemente
      de si la parcela y el cultivo estan activos o inactivos.
      Para esto se deben recuperar todas las parcelas del usuario
      y todos los cultivos registrados en la base de datos
      subyacente de la aplicacion, tanto las parcelas activas
      como las inactivas, y los cultivos activos como los
      inactivos.
      */
      if ($scope.action == 'view') {
        findAllParcels();
        findAllCrops();
      }

    }]);
