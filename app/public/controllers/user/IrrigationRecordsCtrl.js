app.controller(
	"IrrigationRecordsCtrl",
	["$scope", "$location", "$route", "IrrigationRecordSrv", "ParcelSrv", "AccessManager", "ErrorResponseManager", "AuthHeaderManager", "LogoutManager",
		function ($scope, $location, $route, irrigationRecordService, parcelSrv, accessManager, errorResponseManager, authHeaderManager, logoutManager) {

			console.log("IrrigationRecordsCtrl loaded...")

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
				irrigationRecordService.findAll(function (error, data) {
					if (error) {
						console.log("Ocurrió un error: " + error);
						errorResponseManager.checkResponse(error);
						return;
					}

					$scope.data = data;
				})
			}

			$scope.delete = function (id) {
				console.log("Deleting: " + id)

				var result = window.confirm("¿Desea eliminar el elemento seleccionado?");

				if (result === false) {
					return;
				}

				irrigationRecordService.delete(id, function (error) {
					if (error) {
						console.log(error);
						errorResponseManager.checkResponse(error);
						return;
					}

					$location.path("/home/irrigationRecords");
					$route.reload()
				});
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

			// Esto es necesario para la busqueda que se hace cuando se ingresan caracteres
			$scope.findParcelByName = function (parcelName) {
				return parcelSrv.findByName(parcelName).
					then(function (response) {
						var parcels = [];
						for (var i = 0; i < response.data.length; i++) {
							parcels.push(response.data[i]);
						}

						return parcels;
					});
			}

			const UNDEFINED_PARCEL_AND_DATE = "La parcela y la fecha deben estar definidas";
			const UNDEFINED_PARCEL = "La parcela debe estar definida";

			$scope.filter = function () {

				/*
				Si las propiedades parcel y date de $scope tienen el valor
				undefined, significa que NO se eligio una parcela ni una
				fecha para filtrar registros de riego. Por lo tanto, la
				aplicacion muestra el mensaje dado y no ejecuta la instrccion
				que realiza la peticion HTTP correspondiente a esta funcion.
				*/
				if ($scope.parcel == undefined && $scope.date == undefined) {
					alert(UNDEFINED_PARCEL_AND_DATE);
					return;
				}

				/*
				Si la propiedad parcel de $scope tiene el valor undefined,
				significa que NO se cargo una parcela en el campo del nombre
				de una parcela para filtrar registros de riego. Por lo tanto,
				la aplicacion muestra el mensaje dado y no ejecuta la instruccion
				que realiza la peticion HTTP correspondiente esta funcion.
				*/
				if ($scope.parcel == undefined && $scope.date != undefined) {
					alert(UNDEFINED_PARCEL);
					return;
				}

				var newDate = null;

				/*
				Si la fecha esta definida (es decir, tiene un valor asignado),
				se crea una variable con la fecha elegida usando el formato
				yyyy-MM-dd. El motivo de esto es que el metodo filter de la
				clase REST IrrigationRecordRestServlet de la aplicacion del lado
				servidor, utiliza la fecha en el formato yyyy-MM-dd para
				recuperar registros de riego de la base de datos subyacente.
				*/
				if ($scope.date != undefined || $scope.date != null) {
					newDate = $scope.date.getFullYear() + "-" + ($scope.date.getMonth() + 1) + "-" + $scope.date.getDate();
				}

				irrigationRecordService.filter($scope.parcel.name, newDate, function (error, data) {
					if (error) {
						console.log(error);
						errorResponseManager.checkResponse(error);
						return;
					}

					$scope.data = data;
				})
			}

			/*
			Trae el listado de todos los registros de riego de todas
			las parcelas del usuario cuando este presiona el boton
			"Reiniciar listado"
			*/
			$scope.reset = function () {
				/*
				Esta instruccion es para eliminar el contenido de los
				campos del filtro de los datos correspondientes a este
				controller
				*/
				$scope.parcel = undefined;
				$scope.date = undefined;
				findAll();
			}

			findAll();
		}]);
