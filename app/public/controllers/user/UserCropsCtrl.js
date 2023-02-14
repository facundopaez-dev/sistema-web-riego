app.controller(
	"UserCropsCtrl",
	["$scope", "$location", "CropSrv", "AccessManager", "ErrorResponseManager", "AuthHeaderManager", "LogoutManager",
		function ($scope, $location, cropService, accessManager, errorResponseManager, authHeaderManager, logoutManager) {

			/*
			*****************************************************************
			Este controller es para que el usuario pueda ver los cultivos en
			una pagina web que los muestre en forma de lista, en lugar de
			verlos unicamente en una lista desplegada en el formulario de un
			registro de plantacion.

			Hay que tener en cuenta que el usuario solo puede leer un
			cultivo. El administrador es quien puede crear, leer y modificar
			un cultivo. Este es el motivo por el cual solo esta programada
			la opcion de visualizacion en el controller UserCropCtrl.js.
			*****************************************************************
			*/

			console.log("UserCropsCtrl loaded...")

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

			function findAll() {
				cropService.findAll(function (error, data) {
					if (error) {
						console.log("Ocurrió un error: " + error);
						errorResponseManager.checkResponse(error);
						return;
					}

					$scope.data = data;
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

			findAll();
		}]);
