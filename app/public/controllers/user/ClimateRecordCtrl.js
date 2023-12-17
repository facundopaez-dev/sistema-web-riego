app.controller(
  "ClimateRecordCtrl",
  ["$scope", "$location", "$routeParams", "ClimateRecordSrv", "ParcelSrv", "AccessManager", "ErrorResponseManager", "AuthHeaderManager", "LogoutManager",
    "ExpirationManager", "RedirectManager", "UtilDate",
    function ($scope, $location, $params, climateRecordService, parcelService, accessManager, errorResponseManager, authHeaderManager, logoutManager, expirationManager,
      redirectManager, utilDate) {

      console.log("ClimateRecordCtrl loaded with action: " + $params.action)

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
        $location.path("/home/climateRecords");
      }

      function find(id) {
        climateRecordService.find(id, function (error, data) {
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

      const EMPTY_FORM = "Debe completar todos los campos del formulario";
      const UNDEFINED_DATE = "La fecha debe estar definida";
      const UNDEFINED_MINIMUM_TEMPERATURE = "La temepratura mínima debe estar definida";
      const UNDEFINED_MAXIMUM_TEMPERATURE = "La temepratura máxima debe estar definida";
      const UNDEFINED_WIND_SPEED = "La velocidad del viento debe estar definida";
      const UNDEFINED_PROBABILITY_PRECIPITATION = "La probabilidad de la precipitación debe estar definida";
      const UNDEFINED_PRECIPITATION = "La precipitación debe estar definida";
      const UNDEFINED_CLOUDINESS = "La nubosidad debe estar definida";
      const UNDEFINED_ATMOSPHERIC_PRESSURE = "La presión atmosférica debe estar definida";
      const UNDEFINED_DEW_POINT = "El punto de rocío debe estar definido";
      const UNDEFINED_PARCEL = "La parcela debe estar definida";
      const INVALID_WIND_SPEED = "La velocidad del viento debe ser un valor mayor o igual a 0.0";
      const INVALID_PRECIPITATION_PROBABILITY = "La probabilidad de la precipitación debe ser un valor entre 0.0 y 100, incluido";
      const INVALID_PRECIPITATION = "La precipitación debe ser un valor mayor o igual 0.0";
      const INVALID_CLOUDINESS = "La nubosidad debe ser un valor entre 0.0 y 100, incluido";
      const INVALID_ATMOSPHERIC_PRESSURE = "La presión atmosférica debe ser un valor mayor a 0.0";

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

        if ($scope.data.date == undefined) {
          alert(UNDEFINED_DATE);
          return;
        }

        if ($scope.data.parcel == undefined) {
          alert(UNDEFINED_PARCEL);
          return;
        }

        if ($scope.data.minimumTemperature == undefined) {
          alert(UNDEFINED_MINIMUM_TEMPERATURE);
          return;
        }

        if ($scope.data.maximumTemperature == undefined) {
          alert(UNDEFINED_MAXIMUM_TEMPERATURE);
          return;
        }

        if ($scope.data.windSpeed == undefined) {
          alert(UNDEFINED_WIND_SPEED);
          return;
        }

        if ($scope.data.windSpeed < 0.0) {
          alert(INVALID_WIND_SPEED);
          return;
        }

        if ($scope.data.precipProbability == undefined) {
          alert(UNDEFINED_PROBABILITY_PRECIPITATION);
          return;
        }

        if ($scope.data.precipProbability < 0.0 || $scope.data.precipProbability > 100) {
          alert(INVALID_PRECIPITATION_PROBABILITY);
          return;
        }

        if ($scope.data.precip == undefined) {
          alert(UNDEFINED_PRECIPITATION);
          return;
        }

        if ($scope.data.precip < 0.0) {
          alert(INVALID_PRECIPITATION);
          return;
        }

        if ($scope.data.cloudCover == undefined) {
          alert(UNDEFINED_CLOUDINESS);
          return;
        }

        if ($scope.data.cloudCover < 0.0 || $scope.data.cloudCover > 100) {
          alert(INVALID_CLOUDINESS);
          return;
        }

        if ($scope.data.atmosphericPressure == undefined) {
          alert(UNDEFINED_ATMOSPHERIC_PRESSURE);
          return;
        }

        if ($scope.data.atmosphericPressure <= 0.0) {
          alert(INVALID_ATMOSPHERIC_PRESSURE);
          return;
        }

        if ($scope.data.dewPoint == undefined) {
          alert(UNDEFINED_DEW_POINT);
          return;
        }

        climateRecordService.create($scope.data, function (error, data) {
          if (error) {
            console.log(error);
            errorResponseManager.checkResponse(error);
            return;
          }

          $scope.data = data;

          if ($scope.data.date != null) {
            $scope.data.date = new Date($scope.data.date);
          }

          $location.path("/home/climateRecords")
        });
      }

      $scope.modify = function () {
        /*
        **********************************
        Validacion de los datos de entrada
        **********************************
        */

        if ($scope.data.date == undefined) {
          alert(UNDEFINED_DATE);
          return;
        }

        if ($scope.data.parcel == undefined) {
          alert(UNDEFINED_PARCEL);
          return;
        }

        if ($scope.data.minimumTemperature == undefined) {
          alert(UNDEFINED_MINIMUM_TEMPERATURE);
          return;
        }

        if ($scope.data.maximumTemperature == undefined) {
          alert(UNDEFINED_MAXIMUM_TEMPERATURE);
          return;
        }

        if ($scope.data.windSpeed == undefined) {
          alert(UNDEFINED_WIND_SPEED);
          return;
        }

        if ($scope.data.windSpeed < 0.0) {
          alert(INVALID_WIND_SPEED);
          return;
        }

        if ($scope.data.precipProbability == undefined) {
          alert(UNDEFINED_PROBABILITY_PRECIPITATION);
          return;
        }

        if ($scope.data.precipProbability < 0.0 || $scope.data.precipProbability > 100) {
          alert(INVALID_PRECIPITATION_PROBABILITY);
          return;
        }

        if ($scope.data.precip == undefined) {
          alert(UNDEFINED_PRECIPITATION);
          return;
        }

        if ($scope.data.precip < 0.0) {
          alert(INVALID_PRECIPITATION);
          return;
        }

        if ($scope.data.cloudCover == undefined) {
          alert(UNDEFINED_CLOUDINESS);
          return;
        }

        if ($scope.data.cloudCover < 0.0 || $scope.data.cloudCover > 100) {
          alert(INVALID_CLOUDINESS);
          return;
        }

        if ($scope.data.atmosphericPressure == undefined) {
          alert(UNDEFINED_ATMOSPHERIC_PRESSURE);
          return;
        }

        if ($scope.data.atmosphericPressure <= 0.0) {
          alert(INVALID_ATMOSPHERIC_PRESSURE);
          return;
        }

        if ($scope.data.dewPoint == undefined) {
          alert(UNDEFINED_DEW_POINT);
          return;
        }

        climateRecordService.modify($scope.data, function (error, data) {
          if (error) {
            console.log(error);
            errorResponseManager.checkResponse(error);
            return;
          }

          $scope.data = data;

          if ($scope.data.date != null) {
            $scope.data.date = new Date($scope.data.date);
          }

          $location.path("/home/climateRecords")
        });
      }

      $scope.cancel = function () {
        $location.path("/home/climateRecords");
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

      if ($scope.action == 'edit' || $scope.action == 'view') {
        find($params.id);
      }

    }]);