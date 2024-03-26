app.controller(
	"PlantingRecordsCtrl",
	["$scope", "$location", "$route", "PlantingRecordSrv", "ParcelSrv", "CropSrv", "ReasonError", "UtilDate", "WaterNeedFormManager", "AccessManager", "ErrorResponseManager",
		"AuthHeaderManager", "LogoutManager",
		function ($scope, $location, $route, plantingRecordSrv, parcelSrv, cropSrv, reasonError, utilDate, waterNeedFormManager, accessManager, errorResponseManager,
			authHeaderManager, logoutManager) {

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

					/*
					La coleccion de registros de plantacion se añade a un arreglo
					para realizar el control de acceso al formulario del calculo
					de la necesidad de agua de riego de un cultivo
					*/
					waterNeedFormManager.setPlantingRecords(data);
				})
			}

			$scope.delete = function (id) {
				console.log("Deleting: " + id)

				var result = window.confirm("¿Desea eliminar el elemento seleccionado?");

				if (result === false) {
					return;
				}

				plantingRecordSrv.delete(id, function (error) {
					if (error) {
						console.log(error);
						errorResponseManager.checkResponse(error);
						return;
					}

					$location.path("/home/plantingRecords");
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
					});;
			}

			$scope.findCropByName = function (cropName) {
				return cropSrv.findByName(cropName).
					then(function (response) {
						var crops = [];
						for (var i = 0; i < response.data.length; i++) {
							crops.push(response.data[i]);
						}

						return crops;
					});
			}

			const UNDEFINED_PARCEL = "La parcela debe estar definida";
			const DATE_FROM_AND_DATE_UNTIL_OVERLAPPING = "La fecha desde no debe ser mayor o igual a la fecha hasta";

			$scope.filter = function () {
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

				/*
				Si la fecha desde es estrictamente mayor a 9999, la aplicacion
				imprime el mensaje dado y no se realiza la peticion HTTP
				correspondiente a esta funcion
				*/
				if ($scope.dateFrom != undefined && utilDate.yearIsGreaterThanMaximum($scope.dateFrom)) {
					alert(reasonError.getCauseDateFromGreatestToMaximum());
					return;
				}

				/*
				Si la fecha hasta es estrictamente mayor a 9999, la aplicacion
				muestra el mensaje dado y no se realiza la peticion HTTP
				correspondiente a esta funcion
				*/
				if ($scope.dateUntil != undefined && utilDate.yearIsGreaterThanMaximum($scope.dateUntil)) {
					alert(reasonError.getCauseDateUntilGreatestToMaximum());
					return;
				}

				/*
				Si la fecha desde es mayor o igual a la fecha hasta, la aplicacion
				muestra el mensaje dado y no ejecuta la instruccion que realiza la
				peticion HTTP correspondiente a esta funcion
				*/
				if ($scope.dateFrom != undefined && $scope.dateUntil != undefined && $scope.dateFrom >= $scope.dateUntil) {
					alert(DATE_FROM_AND_DATE_UNTIL_OVERLAPPING);
					return;
				}

				var newDateFrom = null;
				var newDateUntil = null;

				/*
				Si la fecha desde y/o la fecha hasta estan definidas (es decir,
				tienen un valor asignado), se crean variables con las fechas
				elegidas usando el formato yyyy-MM-dd. El motivo de esto es
				que el metodo findByFilterParameters de la clase REST
				PlantingRecordRestServlet de la aplicacion del lado servidor,
				utiliza la fecha desde y la fecha hasta en el formato yyyy-MM-dd
				para recuperar balances hidricos de suelo de la base de datos
				subyacente.
				*/
				if ($scope.dateFrom != undefined || $scope.dateFrom != null) {
					newDateFrom = $scope.dateFrom.getFullYear() + "-" + ($scope.dateFrom.getMonth() + 1) + "-" + $scope.dateFrom.getDate();
				}

				if ($scope.dateUntil != undefined || $scope.dateUntil != null) {
					newDateUntil = $scope.dateUntil.getFullYear() + "-" + ($scope.dateUntil.getMonth() + 1) + "-" + $scope.dateUntil.getDate();
				}

				plantingRecordSrv.filter($scope.parcel.name, newDateFrom, newDateUntil, function (error, data) {
					if (error) {
						console.log(error);
						errorResponseManager.checkResponse(error);
						return;
					}

					$scope.data = data;
				})
			}

			/* Esto es necesario para la paginacion */
			var $ctrl = this;

			$scope.service = plantingRecordSrv;
			$scope.listElement = []
			$scope.cantPerPage = 10
			/* Esto es necesario para la paginacion */

			/*
			Trae el listado de todos los registros de plantacion de
			todas las parcelas del usuario cuando este presiona el
			boton "Reiniciar listado"
			*/
			$scope.reset = function () {
				/*
				Estas instrucciones son para eliminar el contenido de los
				campos de filtracion
				*/
				$scope.parcel = undefined;
				$scope.dateFrom = undefined;
				$scope.dateUntil = undefined;
				findAll();
			}

			// findAll();
		}]);
