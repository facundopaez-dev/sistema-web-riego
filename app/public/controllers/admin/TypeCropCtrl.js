app.controller(
  "TypeCropCtrl",
  ["$scope", "$location", "$routeParams", "TypeCropSrv", "AccessManager", "ErrorResponseManager", "AuthHeaderManager", "LogoutManager",
    "ExpirationManager", "RedirectManager",
    function ($scope, $location, $params, typeCropService, accessManager, errorResponseManager, authHeaderManager, logoutManager, expirationManager, redirectManager) {

      console.log("TypeCropCtrl loaded with action: " + $params.action)

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
        $location.path("/adminHome/typesCrop");
      }

      function find(id) {
        typeCropService.find(id, function (error, data) {
          if (error) {
            console.log(error);
            errorResponseManager.checkResponse(error);
            return;
          }

          $scope.data = data;
        });
      }

      const UNDEFINED_CROP_TYPE_NAME = "El nombre del tipo de cultivo debe estar definido";
      const INVALID_NAME =
        "Nombre incorrecto: el nombre para un tipo de cultivo sólo puede contener letras, y un espacio en blanco entre palabra y palabra si está formado por más de una palabra";

      $scope.create = function () {
        // Expresion regular para validar el nombre del tipo de cultivo
        var nameRegexp = /^[A-Za-zÀ-ÿ]+(\s[A-Za-zÀ-ÿ]+)*$/g;

        /*
        Si la propiedad "data" de $scope tiene el valor undefined,
        significa que el usuario pulso el boton "Crear" con el campo
        "Nombre" vacio. Por lo tanto, no se invoca al service de tipo
        de cultivo para la creacion de un tipo de cultivo.
        */
        if ($scope.data == undefined) {
          alert(UNDEFINED_CROP_TYPE_NAME);
          return;
        }

        /*
        Si el nombre del tipo de cultivo ingresado NO es valido (es
        decir, NO contiene unicamente letras, y un espacio en blanco
        entre palabra y palabra si esta formado por mas de una palabra),
        la aplicacion muestra el siguiente mensaje y no ejecuta la instruccion
        que realiza la peticion HTTP para registrar un tipo de cultivo.

        "Nombre incorrecto: el nombre para un tipo de cultivo solo puede
        contener letras, y un espacio en blanco entre palabra y palabra
        si esta formado por mas de una palabra".
        */
        if (!nameRegexp.exec($scope.data.name)) {
          alert(INVALID_NAME);
          return;
        }

        typeCropService.create($scope.data, function (error, data) {
          if (error) {
            console.log(error);
            errorResponseManager.checkResponse(error);
            return;
          }

          $scope.data = data;
          $location.path("/adminHome/typesCrop")
        });
      }

      $scope.modify = function () {
        // Expresion regular para validar el nombre del tipo de cultivo
        var nameRegexp = /^[A-Za-zÀ-ÿ]+(\s[A-Za-zÀ-ÿ]+)*$/g;

        /*
        Si la propiedad "data" de $scope tiene el valor undefined,
        significa que el usuario pulso el boton "Modificar" con el campo
        "Nombre" vacio. Por lo tanto, no se invoca al service de tipo
        de cultivo para la modificacion de un tipo de cultivo.
        */
        if ($scope.data == undefined) {
          alert(UNDEFINED_CROP_TYPE_NAME);
          return;
        }

        /*
        Si el nombre del tipo de cultivo ingresado NO es valido (es
        decir, NO contiene unicamente letras, y un espacio en blanco
        entre palabra y palabra si esta formado por mas de una palabra),
        la aplicacion muestra el siguiente mensaje y no ejecuta la instruccion
        que realiza la peticion HTTP para modificar un tipo de cultivo.

        "Nombre incorrecto: el nombre para un tipo de cultivo solo puede
        contener letras, y un espacio en blanco entre palabra y palabra
        si esta formado por mas de una palabra".
        */
        if (!nameRegexp.exec($scope.data.name)) {
          alert(INVALID_NAME);
          return;
        }

        typeCropService.modify($scope.data, function (error, data) {
          if (error) {
            console.log(error);
            errorResponseManager.checkResponse(error);
            return;
          }

          $scope.data = data;
          $location.path("/adminHome/typesCrop")
        });
      }

      $scope.cancel = function () {
        $location.path("/adminHome/typesCrop");
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

      if ($scope.action == 'edit') {
        find($params.id);
      }

    }]);
