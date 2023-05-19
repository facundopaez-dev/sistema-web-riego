app.controller(
  "IrrigationRecordCtrl",
  ["$scope", "$location", "$routeParams", "IrrigationRecordSrv", "CropSrv", "ParcelSrv", "AccessManager", "ErrorResponseManager", "AuthHeaderManager",
    "LogoutManager", "ExpirationManager", "RedirectManager", "UtilDate",
    function ($scope, $location, $params, irrigationRecordService, cropService, parcelService, accessManager, errorResponseManager, authHeaderManager,
      logoutManager, expirationManager, redirectManager, utilDate) {

      console.log("IrrigationRecordCtrl loaded with action: " + $params.action)

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

      if (['new', 'edit', 'view'].indexOf($params.action) == -1) {
        alert("Acción inválida: " + $params.action);
        $location.path("/home/irrigationRecords");
      }

      const EMPTY_FORM = "Debe completar todos los campos del formulario";
      const UNDEFINED_DATE = "La fecha debe estar definida";
      const IRRIGATION_RECORD_OF_THE_FUTURE_NOT_ALLOWED = "No está permitido que un registro de riego tenga una fecha estrictamente mayor (es decir, posterior) a la fecha actual";
      const NEGATIVE_REALIZED_IRRIGATION = "El riego realizado debe ser mayor o igual a cero";
      const INDEFINITE_PARCEL = "La parcela debe estar definida";

      function find(id) {
        irrigationRecordService.find(id, function (error, data) {
          if (error) {
            console.log(error);
            errorResponseManager.checkResponse(error);
            return;
          }

          $scope.data = data;

          if ($scope.data.date != null) {
            $scope.data.date = new Date($scope.data.date);
          }
        });
      }

      $scope.create = function () {
        var currentDate = new Date();

        /*
        Si la propiedad data de $scope tiene el valor undefined,
        significa que el formulario de creacion de un registro
        de riego tiene todos sus campos vacios. Por lo tanto,
        la aplicacion muestra el mensaje dado y no ejecuta la
        instruccion que realiza la peticion HTTP correspondiente
        a este controller.
        */
        if ($scope.data == undefined) {
          alert(EMPTY_FORM);
          return;
        }

        if ($scope.data.date == undefined) {
          alert(UNDEFINED_DATE);
          return;
        }

        if (utilDate.compareTo($scope.data.date, currentDate) > 0) {
          alert(IRRIGATION_RECORD_OF_THE_FUTURE_NOT_ALLOWED);
          return;
        }

        /*
        Si el riego realizado es estrictamente menor a cero,
        la aplicacion muestra el mensaje dado y no ejecuta
        la instruccion que realiza la peticion HTTP correspondiente
        a este controller
        */
        if ($scope.data.irrigationDone < 0) {
          alert(NEGATIVE_REALIZED_IRRIGATION);
          return;
        }

        /*
        Si la parcela NO esta definida, la aplicacion muestra
        el mensaje dado y no ejecuta la instruccion que realiza
        la peticion HTTP correspondiente a este controller
        */
        if ($scope.data.parcel == undefined) {
          alert(INDEFINITE_PARCEL);
          return;
        }

        irrigationRecordService.create($scope.data, function (error, data) {
          if (error) {
            console.log(error);
            errorResponseManager.checkResponse(error);
            return;
          }

          $scope.data = data;

          if ($scope.data.date != null) {
            $scope.data.date = new Date($scope.data.date);
          }

          $location.path("/home/irrigationRecords")
        });
      }

      $scope.modify = function () {
        var currentDate = new Date();

        /*
        El motivo por el cual NO se verifica si el formulario
        tiene sus campos modificables vacios ni si la parcela
        esta definida (operaciones que se realizan en la creacion
        de un registro de riego) es que en la modificacion siempre
        se recupera un registro de riego, el cual, por defecto
        tiene el riego realizado y la parcela definidos. Por lo
        tanto, cuando se invoca a esta funcion, la propiedad data
        de $scope siempre tiene el riego realizado y la parcela
        definidos.
        */

        if ($scope.data.date == undefined) {
          alert(UNDEFINED_DATE);
          return;
        }

        if (utilDate.compareTo($scope.data.date, currentDate) > 0) {
          alert(IRRIGATION_RECORD_OF_THE_FUTURE_NOT_ALLOWED);
          return;
        }

        /*
        Si el riego realizado es estrictamente menor a cero,
        la aplicacion muestra el mensaje dado y no ejecuta
        la instruccion que realiza la peticion HTTP correspondiente
        a este controller
        */
        if ($scope.data.irrigationDone < 0) {
          alert(NEGATIVE_REALIZED_IRRIGATION);
          return;
        }

        irrigationRecordService.modify($scope.data, function (error, data) {
          if (error) {
            console.log(error);
            errorResponseManager.checkResponse(error);
            return;
          }

          $scope.data = data;

          if ($scope.data.date != null) {
            $scope.data.date = new Date($scope.data.date);
          }

          $location.path("/home/irrigationRecords")
        });
      }

      $scope.cancel = function () {
        $location.path("/home/irrigationRecords");
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

      function findAllActiveParcels() {
        parcelService.findAllActive(function (error, parcels) {
          if (error) {
            console.log("Ocurrio un error: " + error);
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

      function findAllCrops() {
        cropService.findAll(function (error, crops) {
          if (error) {
            console.log("Ocurrio un error: " + error);
            return;
          }

          $scope.crops = crops;
        })
      }

      $scope.action = $params.action;

      if ($scope.action == 'new' || $scope.action == 'edit') {
        findAllActiveParcels();
      }

      if ($scope.action == 'edit' || $scope.action == 'view') {
        find($params.id);
        findAllCrops();
      }

      /*
      En la visualizacion de un registro de riego se debe poder
      ver la parcela a la que pertenece un registro de riego
      independientemente de si la misma esta activa o inactiva.
      Para esto se deben recuperar todas las parcelas del usuario,
      tanto las activas como las inactivas.
      */
      if ($scope.action == 'view') {
        findAllParcels();
      }

    }]);