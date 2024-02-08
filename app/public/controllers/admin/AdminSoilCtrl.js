app.controller(
  "AdminSoilCtrl",
  ["$scope", "$location", "$routeParams", "SoilSrv", "AccessManager", "ErrorResponseManager", "AuthHeaderManager", "LogoutManager",
    "ExpirationManager", "RedirectManager",
    function ($scope, $location, $params, soilService, accessManager, errorResponseManager, authHeaderManager, logoutManager, expirationManager,
      redirectManager) {

      console.log("AdminSoilCtrl loaded with action: " + $params.action)

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
        $location.path("/adminHome/soils");
      }

      function find(id) {
        soilService.find(id, function (error, data) {
          if (error) {
            console.log(error);
            errorResponseManager.checkResponse(error);
            return;
          }
          $scope.data = data;
        });
      }

      const EMPTY_FORM = "Debe completar todos los campos del formulario";
      const UNDEFINED_SOIL_NAME = "El nombre del suelo debe estar definido";
      const INVALID_SOIL_NAME = "El nombre de un suelo debe empezar con una palabra formada únicamente por caracteres alfabéticos y puede tener más de una palabra formada únicamente por caracteres alfabéticos";
      const UNDEFINED_APPARENT_SPECIFIC_WEIGHT = "El peso específico aparente debe estar definido";
      const UNDEFINED_FIELD_CAPACITY = "La capacidad de campo debe estar definida";
      const UNDEFINED_PERMANENT_WILTING_POINT = "El punto de marchitez permanente debe estar definido";
      const INVALID_APPARENT_SPECIFIC_WIGHT = "El peso específico aparente debe ser mayor a cero";
      const INVALID_FIELD_CAPACITY = "La capacidad de campo debe ser mayor a cero";
      const INVALID_PERMANENT_WILTING_POINT = "El punto de marchitez permanente debe ser mayor a cero";

      $scope.create = function () {
        // Expresion regular para validar el nombre del cultivo
        var nameRegexp = /^[A-Za-zÀ-ÿ]+(\s[A-Za-zÀ-ÿ]+)*$/g;

        /*
        Si la propiedad data de $scope tiene el valor undefined,
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
          alert(UNDEFINED_SOIL_NAME);
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
          alert(INVALID_SOIL_NAME);
          return;
        }

        /*
        *********************************************
        Controles sobre los datos numericos del suelo
        *********************************************
        */

        /*
        Si el peso especifico aparente NO esta definido, la aplicacion
        muestra el mensaje dado y no realiza la operacion solicitada
        */
        if ($scope.data.apparentSpecificWeight == undefined) {
          alert(UNDEFINED_APPARENT_SPECIFIC_WEIGHT);
          return;
        }

        /*
        Si el peso especifico aparente tiene un valor menor o igual a
        cero, la aplicacion muestra el mensaje dado y no realiza la
        operacion solicitada
        */
        if ($scope.data.apparentSpecificWeight <= 0.0) {
          alert(INVALID_APPARENT_SPECIFIC_WIGHT);
          return;
        }

        /*
        Si la capacidad de campo NO esta definida, la aplicacion
        muestra el mensaje dado y no realiza la operacion solicitada
        */
        if ($scope.data.fieldCapacity == undefined) {
          alert(UNDEFINED_FIELD_CAPACITY);
          return;
        }

        /*
        Si la capacidad de campo tiene un valor menor o igual a
        cero, la aplicacion muestra el mensaje dado y no realiza
        la operacion solicitada
        */
        if ($scope.data.fieldCapacity <= 0.0) {
          alert(INVALID_FIELD_CAPACITY);
          return;
        }

        /*
        Si el punto de marchitez permanente NO esta definido, la aplicacion
        muestra el mensaje dado y no realiza la operacion solicitada
        */
        if ($scope.data.permanentWiltingPoint == undefined) {
          alert(UNDEFINED_PERMANENT_WILTING_POINT);
          return;
        }

        /*
        Si el punto de marchitez permanente tiene un valor menor o igual
        a cero, la aplicacion muestra el mensaje dado y no realiza la
        operacion solicitada
        */
        if ($scope.data.permanentWiltingPoint <= 0.0) {
          alert(INVALID_PERMANENT_WILTING_POINT);
          return;
        }

        soilService.create($scope.data, function (error, data) {
          if (error) {
            console.log(error);
            errorResponseManager.checkResponse(error);
            return;
          }

          $scope.data = data;
          $location.path("/adminHome/soils")
        });
      }

      $scope.modify = function () {
        // Expresion regular para validar el nombre del cultivo
        var nameRegexp = /^[A-Za-zÀ-ÿ]+(\s[A-Za-zÀ-ÿ]+)*$/g;

        /*
        Si el nombre del dato correspondiente a este controller
        NO contiene unicamente letras, y un espacio en blanco entre
        palabra y palabra si esta formado por mas de una palabra, la
        aplicacion muestra el mensaje dado y no realiza la operacion
        solicitada
        */
        if (!nameRegexp.exec($scope.data.name)) {
          alert(INVALID_SOIL_NAME);
          return;
        }

        /*
        *********************************************
        Controles sobre los datos numericos del suelo
        *********************************************
        */

        /*
        Si el peso especifico aparente tiene un valor menor o igual a
        cero, la aplicacion muestra el mensaje dado y no realiza la
        operacion solicitada
        */
        if ($scope.data.apparentSpecificWeight <= 0.0) {
          alert(INVALID_APPARENT_SPECIFIC_WIGHT);
          return;
        }

        /*
        Si la capacidad de campo tiene un valor menor o igual a
        cero, la aplicacion muestra el mensaje dado y no realiza
        la operacion solicitada
        */
        if ($scope.data.fieldCapacity <= 0.0) {
          alert(INVALID_FIELD_CAPACITY);
          return;
        }

        /*
        Si el punto de marchitez permanente tiene un valor menor o igual
        a cero, la aplicacion muestra el mensaje dado y no realiza la
        operacion solicitada
        */
        if ($scope.data.permanentWiltingPoint <= 0.0) {
          alert(INVALID_PERMANENT_WILTING_POINT);
          return;
        }

        soilService.modify($scope.data, function (error, data) {
          if (error) {
            console.log(error);
            errorResponseManager.checkResponse(error);
            return;
          }

          $scope.data = data;
          $location.path("/adminHome/soils")
        });
      }

      $scope.cancel = function () {
        $location.path("/adminHome/soils");
      }

      $scope.logout = function () {
        /*
        LogoutManager es la factory encargada de realizar el cierre de
        sesion del usuario. Durante el cierre de sesion, la funcion
        logout de la factory mencionada, realiza la peticion HTTP de
        cierre de sesion (elimina logicamente la sesion activa del
        usuario en la base de datos, la cual, esta en el lado servidor;
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
