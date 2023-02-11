app.controller(
	"UserCropsCtrl",
	["$scope", "CropSrv",
		function ($scope, cropService) {

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

			function findAll() {
				cropService.findAll(function (error, data) {
					if (error) {
						console.log("Ocurrió un error: " + error);
						return;
					}

					$scope.data = data;
				})
			}

			findAll();
		}]);
