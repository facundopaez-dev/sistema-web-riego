app.controller(
	"AdminPasswordChangeCtrl",
	["$scope", "$location", "UserSrv", "AccessManager", "ErrorResponseManager", "AuthHeaderManager", "LogoutManager",
		function ($scope, $location, userService, accessManager, errorResponseManager, authHeaderManager, logoutManager) {

			console.log("AdminPasswordChangeCtrl loaded...")

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

			/* Esta propiedad se utiliza para mostrar u ocultar el boton de
			acceso a la lista de usuarios. Dicho boton se debe mostrar si
			un usuario con permiso de administrador tiene el permiso para
			modificar el permiso de administrador. En caso contrario, se
			debe ocultar. */
			$scope.showUsersButton = accessManager.getSuperuserPermissionModifier();

			const EMPTY_FORM = "Debe completar todos los campos del formulario";
			const UNDEFINED_PASSWORD = "La contraseña debe estar definida";
			const UNDEFINED_NEW_PASSWORD = "La nueva contraseña debe estar definida";
			const UNDEFINED_CONFIRMED_NEW_PASSWORD = "La confirmación de la nueva contraseña debe estar definida";
			const MALFORMED_NEW_PASSWORD = "La nueva contraseña debe tener como mínimo 8 caracteres de longitud, una letra minúscula, una letra mayúscula y un número de 0 a 9, con o sin caracteres especiales";
			const INCORRECTLY_CONFIRMED_NEW_PASSWORD = "La confirmación de la nueva contraseña no es igual a la nueva contraseña ingresada";

			$scope.modifyPassword = function () {
				// Expresion regular para validar la nueva contraseña
				var newPasswordRegexp = /^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$/g;

				/*
				Si la propiedad data de $scope tiene el valor undefined,
				significa que el usuario presiono el boton "Modificar"
				con todos los campos vacios del formulario de modificacion
				de contraseña, por lo tanto, la aplicacion muestra el
				mensaje dado y no ejecuta la instruccion que realiza la
				peticion HTTP para modificar la contraseña del usuario
				*/
				if ($scope.data == undefined) {
					alert(EMPTY_FORM);
					return;
				}

				/*
				Si la contraseña NO esta definida cuando el usuario presiona
				el boton "Modificar", la aplicacion muestra el mensaje "La
				contraseña debe estar definida" y no ejecuta la instruccion
				que realiza la peticion HTTP para modificar la contraseña del
				usuario
				*/
				if ($scope.data.password == undefined) {
					alert(UNDEFINED_PASSWORD);
					return;
				}

				/*
				Si la nueva contraseña NO esta definida cuando el usuario presiona
				el boton "Modificar", la aplicacion muestra el mensaje "La nueva
				contraseña debe estar definida" y no ejecuta la instruccion que
				realiza la peticion HTTP para modificar la contraseña del usuario
				*/
				if ($scope.data.newPassword == undefined) {
					alert(UNDEFINED_NEW_PASSWORD);
					return;
				}

				/*
				Si la confirmacion de la nueva contraseña NO esta definida cuando
				el usuario presiona el boton "Modificar", la aplicacion muestra el
				mensaje "La confirmacion de la nueva contraseña debe estar definida"
				y no ejecuta la instruccion que realiza la peticion HTTP para modificar
				la contraseña del usuario
				*/
				if ($scope.data.newPasswordConfirmed == undefined) {
					alert(UNDEFINED_CONFIRMED_NEW_PASSWORD);
					return;
				}

				/*
				Si la nueva contraseña NO contiene como minimo 8 caracteres de longitud,
				una letra minuscula, una letra mayuscula y un numero 0 a 9, la
				aplicacion muestra el siguiente mensaje y no ejecuta la instruccion
				querealiza la solicitud HTTP para modificar la contraseña del usuario.
				
				"La nueva contraseña debe tener como minimo 8 caracteres de longitud, una
				letra minuscula, una letra mayuscula y un numero de 0 a 9, con o sin
				caracteres especiales" y no se realiza la operacion solicitada.
				*/
				if (!newPasswordRegexp.exec($scope.data.newPassword)) {
					alert(MALFORMED_NEW_PASSWORD);
					return;
				}

				/*
				Si la nueva contraseña y la confirmacion de la nueva contraseña NO
				coinciden, la aplicacion muestra el mensaje "La confirmacion de la
				nueva contraseña no es igual a la nueva contraseña ingresada" y no
				ejecuta la instruccion que realiza la peticion HTTP para modificar
				la contraseña del usuario
				*/
				if (!($scope.data.newPassword == $scope.data.newPasswordConfirmed)) {
					alert(INCORRECTLY_CONFIRMED_NEW_PASSWORD);
					return;
				}

				userService.modifyPassword($scope.data, function (error, data) {
					if (error) {
						console.log("Ocurrió un error: " + error);
						errorResponseManager.checkResponse(error);
						return;
					}

					$scope.data = data;
					$location.path("/adminHome");
				})
			}

			$scope.cancel = function () {
				$location.path("/adminHome");
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

		}]);
