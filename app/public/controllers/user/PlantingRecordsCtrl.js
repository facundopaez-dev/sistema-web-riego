app.controller(
	"PlantingRecordsCtrl",
	["$scope", "$location", "PlantingRecordSrv", "ParcelSrv", "IrrigationRecordSrv", "AccessManager", "ErrorResponseManager", "AuthHeaderManager", "LogoutManager",
		function ($scope, $location, plantingRecordSrv, parcelSrv, irrigationRecordService, accessManager, errorResponseManager, authHeaderManager, logoutManager) {

			console.log("Cargando PlantingRecordsCtrl...")

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
				plantingRecordSrv.findAll(function (error, data) {
					if (error) {
						console.log("Ocurrió un error: " + error);
						errorResponseManager.checkResponse(error);
						return;
					}

					$scope.data = data;
				})
			}

			$scope.calculateSuggestedIrrigation = function (id) {
				plantingRecordSrv.calculateSuggestedIrrigation(id, function (error, irrigationRecord) {
					if (error) {
						console.log(error);
						errorResponseManager.checkResponse(error);
						return;
					}

					/*
					Si esta instruccion no esta, no se puede ver el riego
					sugerido en el modal
					*/
					$scope.irrigationRecord = irrigationRecord;
				});
			}

			$scope.saveIrrigationRecord = function () {
				if ($scope.irrigationRecord.irrigationDone >= 0) {
					irrigationRecordService.save($scope.irrigationRecord, function (error, irrigationRecord) {
						if (error) {
							console.log(error);
							errorResponseManager.checkResponse(error);
							return;
						}

						$scope.irrigationRecord = irrigationRecord;
					});
				} else {
					alert("El riego realizado debe ser mayor o igual a cero");
				}

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
			$scope.findParcel = function (parcelName) {
				return parcelSrv.findByName(parcelName).
					then(function (response) {
						var parcels = [];
						for (var i = 0; i < response.data.length; i++) {
							parcels.push(response.data[i]);
						}

						return parcels;
					});;
			}

			const UNDEFINED_PARCEL = "La parcela debe estar definida";

			/*
			Trae el listado de registros de plantacion pertenecientes
			a la parcela con el nombre dado
			*/
			$scope.findAllByParcelName = function () {
				/*
				Si la propiedad parcel de $scope tiene el valor undefined,
				significa que NO se cargo una parcela en el campo del nombre
				de parcela para filtrar registros de plantacion por una parcela.
				Por lo tanto, la aplicacion muestra el mensaje dado y no ejecuta
				la instruccion que realiza la peticion HTTP correspondiente esta
				funcion.
				*/
				if ($scope.parcel == undefined) {
					alert(UNDEFINED_PARCEL);
					return;
				}

				plantingRecordSrv.findAllByParcelName($scope.parcel.name, function (error, data) {
					if (error) {
						console.log(error);
						errorResponseManager.checkResponse(error);
						return;
					}

					$scope.data = data;
				})
			}

			$scope.cancel = function () {
				$location.path("/home/plantingRecords");
			}

			/*
			Trae el listado de todos los registros de plantacion de
			todas las parcelas del usuario cuando este presiona el
			boton "Reiniciar listado"
			*/
			$scope.reset = function () {
				/*
				Esta instruccion es para eliminar el contenido del campo
				del nombre de parcela cuando el usuario presiona el boton
				de reinicio del listado de registros de plantacion
				*/
				$scope.parcel = undefined;
				findAll();
			}

			findAll();
		}]);
