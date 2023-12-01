app.controller(
  "AdminCropCtrl",
  ["$scope", "$location", "$routeParams", "CropSrv", "TypeCropSrv", "RegionSrv", "MonthSrv", "AccessManager", "ErrorResponseManager", "AuthHeaderManager", "LogoutManager",
    "ExpirationManager", "RedirectManager",
    function ($scope, $location, $params, cropService, typeCropService, regionService, monthService, accessManager, errorResponseManager, authHeaderManager, logoutManager,
      expirationManager, redirectManager) {

      console.log("AdminCropCtrl loaded with action: " + $params.action)

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

      if (['new', 'edit', 'view'].indexOf($params.action) == -1) {
        alert("Acción inválida: " + $params.action);
        $location.path("/adminHome/crops");
      }

      function find(id) {
        cropService.find(id, function (error, data) {
          if (error) {
            console.log(error);
            errorResponseManager.checkResponse(error);
            return;
          }
          $scope.data = data;
        });
      }

      const EMPTY_FORM = "Debe completar todos los campos del formulario";
      const UNDEFINED_CROP_NAME = "El nombre del cultivo debe estar definido";
      const TYPE_CROP_UNDEFINED = "El tipo del cultivo debe estar definido";
      const INVALID_CROP_NAME = "El nombre de un cultivo debe empezar con una palabra formada únicamente por caracteres alfabéticos y puede tener más de una palabra formada únicamente por caracteres alfabéticos";
      const UNDEFINED_INITIAL_STAGE = "La etapa inicial debe estar definida";
      const UNDEFINED_DEVELOPMENT_STAGE = "La etapa de desarrollo debe estar definida";
      const UNDEFINED_MIDDLE_STAGE = "La etapa media debe estar definida";
      const UNDEFINED_FINAL_STAGE = "La etapa final debe estar definida";
      const INVALID_INITIAL_STAGE = "La etapa inicial debe ser mayor a cero";
      const INVALID_DEVELOPMENT_STAGE = "La etapa de desarrollo debe ser mayor a cero";
      const INVALID_MIDDLE_STAGE = "La etapa media debe ser mayor a cero";
      const INVALID_FINAL_STAGE = "La etapa final debe ser mayor a cero";
      const UNDEFINED_INITIAL_KC = "El coeficiente inicial debe estar definido";
      const UNDEFINED_MIDDLE_KC = "El coeficiente medio debe estar definido";
      const UNDEFINED_FINAL_KC = "El coeficiente final debe estar definido";
      const INVALID_INITIAL_KC = "El coeficiente inicial debe ser mayor a 0.0";
      const INVALID_MIDDLE_KC = "El coeficiente medio debe ser mayor a 0.0";
      const INVALID_FINAL_KC = "El coeficiente final debe ser mayor a 0.0";

      $scope.create = function () {
        // Expresion regular para validar el nombre del cultivo
        var nameRegexp = /^[A-Za-zÀ-ÿ]+(\s[A-Za-zÀ-ÿ]+)*$/g;

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
        Si el nombre del dato correspondiente a este controller
        NO esta definido, la aplicacion muestra el mensaje dado
        y no realiza la operacion solicitada
        */
        if ($scope.data.name == undefined) {
          alert(UNDEFINED_CROP_NAME);
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
          alert(INVALID_CROP_NAME);
          return;
        }

        /*
        Si el tipo del dato correspondiente a este controller NO
        esta definido, la aplicacion muestra el mensaje dado y no
        realiza la operacion solicitada
        */
        if ($scope.data.typeCrop == undefined) {
          alert(TYPE_CROP_UNDEFINED);
          return;
        }

        /*
        *************************************************
        Controles sobre los valores de las etapas de vida
        de un cultivo
        *************************************************
        */

        /*
        Si la etapa inicial NO esta definida, la aplicacion muestra
        el mensaje dado y no realiza la operacion solicitada
        */
        if ($scope.data.initialStage == undefined) {
          alert(UNDEFINED_INITIAL_STAGE);
          return;
        }

        /*
        Si la etapa inicial tiene un valor menor o igual a cero,
        la aplicacion muestra el mensaje dado y no realiza la
        operacion solicitada
        */
        if ($scope.data.initialStage <= 0) {
          alert(INVALID_INITIAL_STAGE);
          return;
        }

        /*
        Si la etapa de desarrollo NO esta definida, la aplicacion
        muestra el mensaje dado y no realiza la operacion solicitada
        */
        if ($scope.data.developmentStage == undefined) {
          alert(UNDEFINED_DEVELOPMENT_STAGE);
          return;
        }

        /*
        Si la etapa de desarrollo tiene un valor menor o igual a
        cero, la aplicacion muestra el mensaje dado y no realiza
        la operacion solicitada
        */
        if ($scope.data.developmentStage <= 0) {
          alert(INVALID_DEVELOPMENT_STAGE);
          return;
        }

        /*
        Si la etapa media NO esta definida, la aplicacion muestra
        el mensaje dado y no realiza la operacion solicitada
        */
        if ($scope.data.middleStage == undefined) {
          alert(UNDEFINED_MIDDLE_STAGE);
          return;
        }

        /*
        Si la etapa media tiene un valor menor o igual a cero,
        la aplicacion muestra el mensaje dado y no realiza la
        operacion solicitada
        */
        if ($scope.data.middleStage <= 0) {
          alert(INVALID_MIDDLE_STAGE);
          return;
        }

        /*
        Si la etapa final NO esta definida, la aplicacion muestra
        mensaje dado y no realiza la operacion solicitada
        */
        if ($scope.data.finalStage == undefined) {
          alert(UNDEFINED_FINAL_STAGE);
          return;
        }

        /*
        Si la etapa final tiene un valor menor o igual a cero,
        la aplicacion muestra el mensaje dado y no realiza la
        operacion solicitada
        */
        if ($scope.data.finalStage <= 0) {
          alert(INVALID_FINAL_STAGE);
          return;
        }

        /*
        **************************************************
        Controles sobre los valores de los coeficientes de
        un cultivo
        **************************************************
        */

        /*
        Si el coeficiente inicial NO esta definido, la aplicacion
        muestra el mensaje dado y no realiza la operacion solicitada
        */
        if ($scope.data.initialKc == undefined) {
          alert(UNDEFINED_INITIAL_KC);
          return;
        }

        /*
        Si el coeficiente inicial tiene un valor menor o igual a
        0.0, la aplicacion muestra el mensaje dado y no realiza la
        operacion solicitada
        */
        if ($scope.data.initialKc <= 0.0) {
          alert(INVALID_INITIAL_KC);
          return;
        }

        /*
        Si el coeficiente medio NO esta definido, la aplicacion
        muestra el mensaje dado y no realiza la operacion solicitada
        */
        if ($scope.data.middleKc == undefined) {
          alert(UNDEFINED_MIDDLE_KC);
          return;
        }

        /*
        Si el coeficiente medio tiene un valor menor o igual a
        0.0, la aplicacion muestra el mensaje dado y no realiza la
        operacion solicitada
        */
        if ($scope.data.middleKc <= 0.0) {
          alert(INVALID_MIDDLE_KC);
          return;
        }

        /*
        Si el coeficiente final NO esta definido, la aplicacion
        muestra el mensaje dado y no realiza la operacion solicitada
        */
        if ($scope.data.finalKc == undefined) {
          alert(UNDEFINED_FINAL_KC);
          return;
        }

        /*
        Si el coeficiente final tiene un valor menor o igual a
        0.0, la aplicacion muestra el mensaje dado y no realiza
        la operacion solicitada
        */
        if ($scope.data.finalKc <= 0.0) {
          alert(INVALID_FINAL_KC);
          return;
        }

        cropService.create($scope.data, function (error, data) {
          if (error) {
            console.log(error);
            errorResponseManager.checkResponse(error);
            return;
          }

          $scope.data = data;
          $location.path("/adminHome/crops")
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
          alert(INVALID_CROP_NAME);
          return;
        }

        /*
        Si el tipo del dato correspondiente a este controller NO
        esta definido, la aplicacion muestra el mensaje dado y no
        realiza la operacion solicitada
        */
        if ($scope.data.typeCrop == undefined) {
          alert(TYPE_CROP_UNDEFINED);
          return;
        }

        /*
        *************************************************
        Controles sobre los valores de las etapas de vida
        de un cultivo
        *************************************************
        */

        /*
        Si la etapa inicial tiene un valor menor o igual a cero,
        la aplicacion muestra el mensaje dado y no realiza la
        operacion solicitada
        */
        if ($scope.data.initialStage <= 0) {
          alert(INVALID_INITIAL_STAGE);
          return;
        }

        /*
        Si la etapa de desarrollo tiene un valor menor o igual a
        cero, la aplicacion muestra el mensaje dado y no realiza
        la operacion solicitada
        */
        if ($scope.data.developmentStage <= 0) {
          alert(INVALID_DEVELOPMENT_STAGE);
          return;
        }

        /*
        Si la etapa media tiene un valor menor o igual a cero,
        la aplicacion muestra el mensaje dado y no realiza la
        operacion solicitada
        */
        if ($scope.data.middleStage <= 0) {
          alert(INVALID_MIDDLE_STAGE);
          return;
        }

        /*
        Si la etapa final tiene un valor menor o igual a cero,
        la aplicacion muestra el mensaje dado y no realiza la
        operacion solicitada
        */
        if ($scope.data.finalStage <= 0) {
          alert(INVALID_FINAL_STAGE);
          return;
        }

        /*
        **************************************************
        Controles sobre los valores de los coeficientes de
        un cultivo
        **************************************************
        */

        /*
        Si el coeficiente inicial tiene un valor menor o igual a
        0.0, la aplicacion muestra el mensaje dado y no realiza la
        operacion solicitada
        */
        if ($scope.data.initialKc <= 0.0) {
          alert(INVALID_INITIAL_KC);
          return;
        }

        /*
        Si el coeficiente medio tiene un valor menor o igual a
        0.0, la aplicacion muestra el mensaje dado y no realiza la
        operacion solicitada
        */
        if ($scope.data.middleKc <= 0.0) {
          alert(INVALID_MIDDLE_KC);
          return;
        }

        /*
        Si el coeficiente final tiene un valor menor o igual a
        0.0, la aplicacion muestra el mensaje dado y no realiza
        la operacion solicitada
        */
        if ($scope.data.finalKc <= 0.0) {
          alert(INVALID_FINAL_KC);
          return;
        }

        cropService.modify($scope.data, function (error, data) {
          if (error) {
            console.log(error);
            errorResponseManager.checkResponse(error);
            return;
          }

          $scope.data = data;
          $location.path("/adminHome/crops")
        });
      }

      $scope.cancel = function () {
        $location.path("/adminHome/crops");
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
      $scope.findMonthByName = function (monthName) {
        return monthService.findByName(monthName).
          then(function (response) {
            var months = [];
            for (var i = 0; i < response.data.length; i++) {
              months.push(response.data[i]);
            }

            return months;
          });;
      }

      // Esto es necesario para la busqueda que se hace cuando se ingresan caracteres
      $scope.findActiveTypeCropByName = function (typeCropName) {
        return typeCropService.findActiveTypeCropByName(typeCropName).
          then(function (response) {
            var typesCrop = [];
            for (var i = 0; i < response.data.length; i++) {
              typesCrop.push(response.data[i]);
            }

            return typesCrop;
          });;
      }

      // Esto es necesario para la busqueda que se hace cuando se ingresan caracteres
      $scope.findActiveRegionByName = function (regionName) {
        return regionService.findActiveRegionByName(regionName).
          then(function (response) {
            var regions = [];
            for (var i = 0; i < response.data.length; i++) {
              regions.push(response.data[i]);
            }

            return regions;
          });;
      }

      if ($scope.action == 'edit' || $scope.action == 'view') {
        find($params.id);
      }

    }]);
