app.controller(
  "PlantingRecordCtrl",
  ["$scope", "$location", "$route", "$routeParams", "PlantingRecordSrv", "CropSrv", "ParcelSrv", "AccessManager", "ErrorResponseManager", "AuthHeaderManager",
    "LogoutManager", "ExpirationManager", "RedirectManager", "CropManager", "UtilDate",
    function ($scope, $location, $route, $params, plantingRecordService, cropService, parcelService, accessManager, errorResponseManager, authHeaderManager,
      logoutManager, expirationManager, redirectManager, cropManager, utilDate) {

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

          if ($scope.data.deathDate != null) {
            $scope.data.deathDate = new Date($scope.data.deathDate);
          }

        });
      }

      /*
      Algunos de los estados que puede tener un registro
      de plantacion
      */
      const IN_DEVELOPMENT = "En desarrollo";
      const OPTIMAL_DEVELOPMENT_STATE = "Desarrollo óptimo";
      const DEVELOPMENT_AT_RISK_WILTING_STATE = "Desarrollo en riesgo de marchitez";
      const DEVELOPMENT_IN_WILTING_STATE = "Desarrollo en marchitez";
      const ON_HOLD = "En espera";
      const DEAD_STATUS = "Muerto";

      /*
      Constantes de mensaje en caso de que los datos de entrada
      no sean los correctos
      */
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

          /*
          Si el cultivo elegido tiene un estado en desarrollo (en desarrollo,
          desarrollo optimo, desarrollo en riesgo de marchitez o desarrollo en
          marchitez) o el estado "En espera" y la cantidad de dias entre la fecha
          de siembra y la fecha de cosecha elegidas para el mismo es mayor a su
          ciclo de vida, la aplicacion muestra un mensaje de advertencia sugiriendo
          cual debe ser la fecha de cosecha
          */
          if (($scope.data.status.name === IN_DEVELOPMENT
            || $scope.data.status.name === OPTIMAL_DEVELOPMENT_STATE
            || $scope.data.status.name === DEVELOPMENT_AT_RISK_WILTING_STATE
            || $scope.data.status.name === DEVELOPMENT_IN_WILTING_STATE
            || $scope.data.status.name === ON_HOLD) &&
            cropManager.lifeCycleExceeded($scope.data.crop.lifeCycle, $scope.data.seedDate, $scope.data.harvestDate)) {
            var suggestedHarvestDate = cropManager.calculateSuggestedHarvestDate($scope.data.seedDate, $scope.data.crop.lifeCycle);
            alert(cropManager.getLifeCycleExceededWarning($scope.data.seedDate, $scope.data.harvestDate, $scope.data.crop.lifeCycle, suggestedHarvestDate));
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

        if ($scope.data.parcel == undefined) {
          alert(UNDEFINED_PARCEL);
          return;
        }

        if ($scope.data.crop == undefined) {
          alert(UNDEFINED_CROP);
          return;
        }

        const currentDate = new Date();
        let maintainDeadStatus = false;

        /*
        Si el registro de plantacion a modificar tiene el estado muerto y
        existe la posibilidad de que adquiera al estado "Finalizado" (si la
        fecha de cosecha elegida es anterior a la fecha actual) o un estado
        en desarrollo (si la fecha actual es mayor o igual a la fecha de
        siembra elegida y menor o igual a la fecha de cosecha elegida, es
        decir, si la fecha actual esta entre la fecha de siembra y la fecha
        de cosecha elegidas), se pide al usuario que confirme si desea que
        el registro de plantacion mantenga el estado muerto luego de la
        modificacion
        */
        if (($scope.data.status.name == DEAD_STATUS)
          && (utilDate.compareTo($scope.data.harvestDate, currentDate) < 0
            || (utilDate.compareTo(currentDate, $scope.data.seedDate) >= 0 && utilDate.compareTo(currentDate, $scope.data.harvestDate) <= 0))) {
          var message = "Si se modifica el registro de plantación con una fecha de cosecha anterior a la fecha actual, adquirirá el estado finalizado. "
            + "En cambio, si se lo modifica con fechas de tal manera que la fecha actual (hoy) esté en el período definido por las fechas elegidas, el "
            + "registro de plantación adquirirá un estado de desarrollo (en desarrollo o desarrollo óptimo dependiendo de si se tiene en cuenta el suelo o no). "
            + "¿Desea que el registro de plantación mantenga el estado muerto luego de la modificación?";

          maintainDeadStatus = confirm(message);
        }

        plantingRecordService.modify($scope.data, maintainDeadStatus, function (error, data) {
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

          /*
          Si el cultivo elegido tiene un estado en desarrollo (en desarrollo,
          desarrollo optimo, desarrollo en riesgo de marchitez o desarrollo en
          marchitez) o el estado "En espera" y la cantidad de dias entre la fecha
          de siembra y la fecha de cosecha elegidas para el mismo es mayor a su
          ciclo de vida, la aplicacion muestra un mensaje de advertencia sugiriendo
          cual debe ser la fecha de cosecha
          */
          if (($scope.data.status.name === IN_DEVELOPMENT
            || $scope.data.status.name === OPTIMAL_DEVELOPMENT_STATE
            || $scope.data.status.name === DEVELOPMENT_AT_RISK_WILTING_STATE
            || $scope.data.status.name === DEVELOPMENT_IN_WILTING_STATE
            || $scope.data.status.name === ON_HOLD) &&
            cropManager.lifeCycleExceeded($scope.data.crop.lifeCycle, $scope.data.seedDate, $scope.data.harvestDate)) {
            var suggestedHarvestDate = cropManager.calculateSuggestedHarvestDate($scope.data.seedDate, $scope.data.crop.lifeCycle);
            alert(cropManager.getLifeCycleExceededWarning($scope.data.seedDate, $scope.data.harvestDate, $scope.data.crop.lifeCycle, suggestedHarvestDate));
          }

          $location.path("/home/plantingRecords")
          $route.reload();
        });
      }

      $scope.cancel = function () {
        $location.path("/home/plantingRecords");
      }

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

      // Esto es necesario para la busqueda que se hace cuando se ingresan caracteres
      $scope.findActiveCropByName = function (cropName) {
        return cropService.findActiveCropByName(cropName).
          then(function (response) {
            var crops = [];
            for (var i = 0; i < response.data.length; i++) {
              crops.push(response.data[i]);
            }

            return crops;
          });;
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

      if ($scope.action == 'edit' || $scope.action == 'view') {
        find($params.id);
      }

    }]);
