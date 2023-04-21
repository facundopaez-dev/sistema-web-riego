app.controller(
  "UserAccountCtrl",
  ["$scope", "$location", "$routeParams", "UserSrv", "AccessManager", "ErrorResponseManager", "AuthHeaderManager", "LogoutManager", "ExpirationManager",
    "RedirectManager",
    function ($scope, $location, $params, userService, accessManager, errorResponseManager, authHeaderManager, logoutManager, expirationManager, redirectManager) {

      console.log("UserAccountCtrl loaded with action: " + $params.action);

      /*
      *******************************************************************
      Este controller es para que el usuario pueda modificar los datos de
      su cuenta
      *******************************************************************
      */

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

      const EMPTY_FORM = "Debe completar todos los campos del formulario";
      const UNDEFINED_USERNAME = "El nombre de usuario debe estar definido";
      const UNDEFINED_NAME = "El nombre debe estar definido";
      const UNDEFINED_LAST_NAME = "El apellido debe estar definido";
      const UNDEFINED_EMAIL = "La dirección de correo electrónico debe estar definida";
      const MALFORMED_USERNAME = "El nombre de usuario debe tener una longitud de entre 4 y 15 caracteres, comenzar con caracteres alfabéticos (sin símbolos de acentuación) seguido o no de números y/o guiones bajos";
      const MALFORMED_NAME = "El nombre debe tener una longitud de entre 3 y 30 caracteres alfabéticos sin símbolos de acentuación, empezar con una letra mayúscula seguido de letras minúsculas, tener un espacio en blanco entre nombre y nombre si hay mas de un nombre, y los nombres que vienen después del primero deben empezar con una letra mayúscula seguido de letras minúsculas";
      const MALFORMED_LAST_NAME = "El apellido debe tener una longitud de entre 3 y 30 caracteres alfabéticos sin símbolos de acentuación, empezar con una letra mayúscula seguido de letras minúsculas, tener un espacio en blanco entre apellido y apellido si hay más de un apellido, y los apellidos que vienen después del primero deben empezar con una letra mayúscula seguido de letras minúsculas";
      const MALFORMED_EMAIL = "La dirección de correo electrónico no es válida";

      if (['edit'].indexOf($params.action) == -1) {
        alert("Acción inválida: " + $params.action);
        $location.path("/home");
      }

      function find(id) {
        userService.find(id, function (error, data) {
          if (error) {
            console.log(error);
            errorResponseManager.checkResponse(error);
            return;
          }

          $scope.data = data;
        });
      }

      $scope.modify = function () {
        /*
        Expresiones regulares para validar los datos de entrada
        del formulario
        */
        var usernameRegexp = /^[A-Za-z][A-Za-z0-9_]{3,14}$/g;
        var nameRegexp = /^[A-Z](?=.{2,29}$)[a-z]+(?:\s[A-Z][a-z]+)*$/g;
        var lastNameRegexp = /^[A-Z](?=.{2,29}$)[a-z]+(?:\s[A-Z][a-z]+)*$/g;
        var emailRegexp = /^(?=.{1,64}@)[a-z0-9_-]+(\.[a-z0-9_-]+)*@[^-][a-z0-9-]+(\.[a-z0-9-]+)*(\.[a-z]{2,})$/g;

        /*
        Si la propiedad data de $scope tiene el valor undefined,
        significa que el usuario presiono el boton "Modificar"
        con todos los campos vacios del formulario, por lo tanto,
        la aplicacion muestra el mensaje dado y no ejecuta la
        instruccion que realiza la peticion HTTP para registrar
        a un usuario
        */
        if ($scope.data == undefined) {
          alert(EMPTY_FORM);
          return;
        }

        /*
        ******************************************
        Controles sobre la definicion de los datos
        ******************************************
        */

        /*
        Si el nombre de usuario NO esta definido cuando el usuario
        presiona el boton "Modificar", la aplicacion muestra el
        mensaje "El nombre de usuario debe estar definido" y no
        ejecuta la instruccion que realiza la peticion HTTP
        correspondiente a este controller
        */
        if ($scope.data.username == undefined) {
          alert(UNDEFINED_USERNAME);
          return;
        }

        /*
        Si el nombre NO esta definido cuando el usuario presiona
        el boton "Modificar", la aplicacion muestra el mensaje
        "El nombre debe estar definido" y no ejecuta la instruccion
        que realiza la peticion HTTP correspondiente a este controller
        */
        if ($scope.data.name == undefined) {
          alert(UNDEFINED_NAME);
          return;
        }

        /*
        Si el apellido NO esta definido cuando el usuario presiona
        el boton "Modificar", la aplicacion muestra el mensaje
        "El apellido debe estar definido" y no ejecuta la instruccion
        que realiza la peticion HTTP correspondiente a este controller
        */
        if ($scope.data.lastName == undefined) {
          alert(UNDEFINED_LAST_NAME);
          return;
        }

        /*
        Si la direccion de correo electronico NO esta definida
        cuando el usuario presiona el boton "Modificar", la
        aplicacion muestra el mensaje "La direccion de correo
        electronico debe estar definida" y no ejecuta la instruccion
        que realiza la peticion HTTP correspondiente a este controller
        */
        if ($scope.data.email == undefined) {
          alert(UNDEFINED_EMAIL);
          return;
        }

        /*
        *************************************
        Controles sobre la forma de los datos
        *************************************
        */

        /*
        Si el nombre de usuario NO tiene una longitud de entre 4
        y 15 caracteres, y NO empieza con caracteres alfabeticos
        con o sin numeros y/o guiones bajos, la aplicacion muestra
        el siguiente mensaje y no ejecuta la instruccion que realiza
        la peticion HTTP correspondiente a este controller:
        
        "El nombre de usuario debe tener una longitud de entre 4 y
        15 caracteres, comenzar con caracteres alfabeticos (sin simbolos
        de acentuacion) seguido o no de numeros y/o guiones bajos".
        */
        if (!usernameRegexp.exec($scope.data.username)) {
          alert(MALFORMED_USERNAME);
          return;
        }

        /*
        Si el nombre NO tiene una longitud entre 3 y 30 caracteres
        alfabeticos, NO empieza con una letra mayuscula seguida de
        letras minusculas, NO tiene un espacio en blanco entre nombre
        y nombre en el caso en el que el usuario tenga mas de un nombre,
        y los nombres que vienen a continuacion del primero NO empiezan
        con una letra mayuscula seguida de letras minusculas, la aplicacion
        muestra el siguiente mensaje y no ejecuta la la instruccion que
        realiza la peticion HTTP correspondiente a este controller.
        
        "El nombre debe tener una longitud de entre 3 y 30 caracteres
        alfabeticos sin simbolos de acentuacion, empezar con una letra
        mayuscula seguido de letras minusculas, tener un espacio en blanco
        entre nombre y nombre si hay mas de un nombre, y los nombres que
        vienen despues del primero deben empezar con una letra mayuscula
        seguido de letras minusculas".
        */
        if (!nameRegexp.exec($scope.data.name)) {
          alert(MALFORMED_NAME);
          return;
        }

        /*
        Si el apellido NO tiene una longitud de entre 3 y 30 caracteres
        alfabeticos, NO empieza con una letra mayuscula seguida de
        letras minusculas, NO tiene un espacio en blanco entre apellido
        y apellido en el caso en el que el usuario tenga mas de un apellido,
        y los apellidos que vienen a continuacion del primero NO empiezan
        con una letra mayuscula seguida de letras minusculas, la aplicacion
        muestra el siguiente mensaje y no ejecuta la instruccion que realiza
        la peticion HTTP para registrar un usuario.
  
        "El apellido debe tener una longitud de entre 3 y 30 caracteres
        alfabeticos sin simbolos de acentuacion, empezar con una letra
        mayuscula seguido de letras minusculas, tener un espacio en blanco
        entre apellido y apellido si hay mas de un apellido, y los apellidos
        que vienen despues del primero deben empezar con una letra mayuscula
        seguido de letras minusculas".
        */
        if (!lastNameRegexp.exec($scope.data.lastName)) {
          alert(MALFORMED_LAST_NAME);
          return;
        }

        /*
        Si la direccion de correo electronico NO es valida, la aplicacion
        muestra el mensaje "La direccion de correo electronico no es valida"
        y no ejecuta la instruccion que realiza la solicitud HTTP para
        registrar a un usuario
        */
        if (!emailRegexp.exec($scope.data.email)) {
          alert(MALFORMED_EMAIL);
          return;
        }

        userService.modify($scope.data, function (error, data) {
          if (error) {
            console.log(error);
            errorResponseManager.checkResponse(error);
            return;
          }

          $scope.data = data;
          $location.path("/home")
        });
      }

      $scope.cancel = function () {
        $location.path("/home");
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
