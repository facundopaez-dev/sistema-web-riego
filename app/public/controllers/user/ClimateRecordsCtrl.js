app.controller(
	"ClimateRecordsCtrl",
	["$scope", "ClimateRecordSrv",
		function ($scope, climateRecordSrv) {
			console.log("ClimateRecordsCtrl loaded...")

			function findAll() {
				climateRecordSrv.findAll(function (error, data) {
					if (error) {
						alert("Ocurrió un error: " + error);
						return;
					}

					$scope.data = data;
				})
			}

			findAll();
		}]);
