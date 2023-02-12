app.controller(
	"PlantingRecordsCtrl",
	["$scope","$location","$route","PlantingRecordSrv", "IrrigationLogSrv",
	function($scope, $location, $route, plantingRecordSrv, irrigationLogService) {
		console.log("Cargando PlantingRecordsCtrl...")

		function findAll(){
			plantingRecordSrv.findAll( function(error, data){
				if(error){
					alert("Ocurrió un error: " + error);
					return;
				}

				$scope.data = data;
			})
		}

		$scope.calculateSuggestedIrrigation = function(id){
			plantingRecordSrv.calculateSuggestedIrrigation(id, function(error, data){
				if(error){
					console.log(error);
					return;
				}

				/*
				Si esta sentencia no esta, no se puede ver el riego
				sugerido en el modal
				*/
				$scope.data = data;

				/*
				Esto impide que el modal no sea desplegado
				*/
				// $location.path("/plantingRecords");
				// $route.reload();
			});
		}

		$scope.saveIrrigationLog = function() {
			if ($scope.data.irrigationDone >= 0) {
				irrigationLogService.save($scope.data, function(error, data){
					if(error){
						console.log(error);
						return;
					}

					$scope.data = data;
				});
			} else {
				alert("El riego realizado sólo puede ser mayor o igual a cero");
			}

		}

		findAll();
	}]);
