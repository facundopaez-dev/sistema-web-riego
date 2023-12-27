app.controller(
  "ParcelCtrl",
  ["$scope", "$location", "$routeParams", "ParcelSrv", "SoilSrv", "AccessManager", "ErrorResponseManager", "AuthHeaderManager", "LogoutManager", "ExpirationManager",
    "RedirectManager",
    function ($scope, $location, $params, parcelService, soilService, accessManager, errorResponseManager, authHeaderManager, logoutManager, expirationManager, redirectManager) {

      console.log("ParcelCtrl loaded with action: " + $params.action);

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

      var base64icon = "data:image/png;base64,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";

      // Variable necesaria para poder cargar el icono del marcador
      var icondata = { iconUrl: base64icon, iconAnchor: [19, 19], };

      $scope.markers = new Array();

      if (['new', 'edit', 'view', 'options'].indexOf($params.action) == -1) {
        alert("Acción inválida: " + $params.action);
        $location.path("/home/parcels");
      }

      const EMPTY_FORM = "Debe completar el formulario. El campo del suelo es opcional (es decir, puede estar vacío).";
      const PARCEL_NAME_UNDEFINED = "El nombre de la parcela debe estar definido";
      const INVALID_PARCEL_NAME = "El nombre de una parcela debe empezar con una palabra formada únicamente por caracteres alfabéticos. Puede haber más de una palabra formada únicamente por caracteres alfabéticos y puede haber palabras formadas únicamente por caracteres numéricos. Todas las palabras deben estar separadas por un espacio en blanco.";
      const INVALID_NUMBER_OF_HECTARES = "La cantidad de hectáreas debe ser mayor a 0.0";
      const UNDEFINED_GEOGRAPHIC_COORDINATE = "La coordenada geográfica de la parcela debe estar definida";

      function find(id) {
        parcelService.find(id, function (error, data) {
          if (error) {
            console.log(error);
            errorResponseManager.checkResponse(error);
            return;
          }

          $scope.data = data;

          /*
          Elimina el marcador existente en el arreglo. Si no se hace esto,
          se veria mas de un marcador en el mapa cada vez que se agregue
          uno al arreglo.
          */
          $scope.markers.pop();

          /*
          Agrega al arreglo un nuevo marcador con las coordendas geograficas
          de la parcela recuperada para que se vea en el mapa la ubicacion
          geografica de la misma
          */
          $scope.markers.push({
            lat: $scope.data.latitude,
            lng: $scope.data.longitude,
            message: "¡Soy un marcador!",
            icon: icondata
          });

          // console.log($scope.data);
        });
      }

      // Esto es necesario para la busqueda que se hace cuando se ingresan caracteres
      $scope.findActiveSoilByName = function (soilName) {
        return soilService.findActiveSoilByName(soilName).
          then(function (response) {
            var soils = [];
            for (var i = 0; i < response.data.length; i++) {
              soils.push(response.data[i]);
            }

            return soils;
          });;
      }

      $scope.create = function () {
        // Expresion regular para validar el nombre de la parcela
        var nameRegexp = /^[A-Za-zÀ-ÿ]+(\s[A-Za-zÀ-ÿ]*[0-9]*)*$/g;

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
        Si el nombre de la parcela NO esta definido, la aplicacion
        muestra el mensaje dado y no ejecuta la instruccion que
        realiza la peticion HTTP correspondiente a esta funcion
        */
        if ($scope.data.name == undefined) {
          alert(PARCEL_NAME_UNDEFINED);
          return;
        }

        /*
        Si el nombre de la parcela NO empieza con una cadena
        formada unicamente por caracteres alfabeticos, la
        aplicacion muestra el mensaje dado y no ejecuta la
        instruccion que realiza la peticion HTTP correspondiente
        a esta funcion
        */
        if (!nameRegexp.exec($scope.data.name)) {
          alert(INVALID_PARCEL_NAME);
          return;
        }

        /*
        Si la cantidad de hectareas NO esta definida o si esta definida,
        pero es menor o igual a 0.0, la aplicacion muestra el mensaje
        dado y no ejecuta la instruccion que realiza la peticion HTTP
        correspondiente a este controller
        */
        if ($scope.data.hectares == undefined || $scope.data.hectares <= 0.0) {
          alert(INVALID_NUMBER_OF_HECTARES);
          return;
        }

        /*
        Si el elemento del indice 0 del arreglo markers de $scope
        tiene el valor undefined, significa que no se selecciono
        una ubicacion geografica para la parcela mediante el mapa.
        Por lo tanto, la aplicacion muestra el mensaje dado y no
        ejecuta la instruccion que realiza la peticion HTTP
        correspondiente a esta funcion.
        */
        if ($scope.markers[0] == undefined) {
          alert(UNDEFINED_GEOGRAPHIC_COORDINATE);
          return;
        }

        /*
        Si la propiedad data de $scope NO tiene el valor undefined
        (lo cual, aparentemente ocurre cuando se completa un campo
        del formulario de la parcela) se crean (aparentemente) las
        propiedades latitude y longitude en data y se le asignan
        la latitud y la longitud elegidas en el mapa
        */
        if ($scope.data != undefined) {
          /*
          Las coordendas geograficas del marcador colocado en el mapa por
          parte del usuario, mediante el formulario de creacion, son cargadas
          en los atributos latitud y longitud de la parcela a crear
          */
          $scope.data.latitude = $scope.markers[0].lat;
          $scope.data.longitude = $scope.markers[0].lng;
        }

        parcelService.create($scope.data, function (error, data) {
          if (error) {
            console.log(error);
            errorResponseManager.checkResponse(error);
            return;
          }

          $scope.data = data;
          $location.path("/home/parcels")
        });
      }

      $scope.modify = function () {
        // Expresion regular para validar el nombre de la parcela
        var nameRegexp = /^[A-Za-zÀ-ÿ]+(\s[A-Za-zÀ-ÿ]*[0-9]*)*$/g;

        /*
        Si el nombre de la parcela NO esta definido, la aplicacion
        muestra el mensaje dado y no ejecuta la instruccion que
        realiza la peticion HTTP correspondiente a esta funcion
        */
        if ($scope.data.name == undefined) {
          alert(PARCEL_NAME_UNDEFINED);
          return;
        }

        /*
        Si el nombre de la parcela NO empieza con una cadena
        formada unicamente por caracteres alfabeticos, la
        aplicacion muestra el mensaje dado y no ejecuta la
        instruccion que realiza la peticion HTTP correspondiente
        a esta funcion
        */
        if (!nameRegexp.exec($scope.data.name)) {
          alert(INVALID_PARCEL_NAME);
          return;
        }

        /*
        Si la cantidad de hectareas NO esta definida o si esta definida,
        pero es menor o igual a 0.0, la aplicacion muestra el mensaje
        dado y no ejecuta la instruccion que realiza la peticion HTTP
        correspondiente a este controller
        */
        if ($scope.data.hectares == undefined || $scope.data.hectares <= 0.0) {
          alert(INVALID_NUMBER_OF_HECTARES);
          return;
        }

        /*
        Las coordendas geograficas del marcador colocado en el mapa por
        parte del usuario, mediante el formulario de parcela, son cargadas
        en los atributos latitud y longitud de la parcela a modificar
        */
        $scope.data.latitude = $scope.markers[0].lat;
        $scope.data.longitude = $scope.markers[0].lng;

        parcelService.modify($scope.data, function (error, data) {
          if (error) {
            console.log(error);
            errorResponseManager.checkResponse(error);
            return;
          }

          $scope.data = data;
          $location.path("/home/parcels")
        });
      }

      $scope.cancel = function () {
        $location.path("/home/parcels");
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

      angular.extend($scope, {
        Barcelona: {
          lat: 41.3825,
          lng: 2.176944,
          zoom: 12
        },
        Madrid: {
          lat: 40.095,
          lng: -3.823,
          zoom: 7
        },
        Origin: {
          lat: 0.037668,
          lng: 34.706523,
          zoom: 1
        },
        events: {}
      });

      /*
      Evento de clic para la seleccion de la ubicacion geografica de
      una parcela en el mapa
      */
      $scope.$on("leafletDirectiveMap.click", function (event, args) {
        var leafEvent = args.leafletEvent;

        /*
        Elimina el marcador existente en el arreglo.

        Se realiza esta eliminacion porque de lo contrario
        se agregaria el arreglo mas de un marcador y por ende
        se veria en el mapa mas de un marcador, lo cual, no es
        necesario en nuestro caso.
        */
        $scope.markers.pop();

        /*
        Agrega al arreglo un nuevo marcador con las coordenadas geograficas
        elegidas por el usuario cuando este hace clic en alguna parte del mapa
        */
        $scope.markers.push({
          lat: leafEvent.latlng.lat,
          lng: leafEvent.latlng.lng,
          message: "¡Soy un marcador!",
          icon: icondata
        });

        // alert("Lat: " + $scope.markers[0].lat + " Long: " + $scope.markers[0].lng);
      });

    }]);
