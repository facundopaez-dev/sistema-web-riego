app.controller(
	"AdminCropsCtrl",
	["$scope", "$location", "$route", "CropSrv",
		function ($scope, $location, $route, cropService) {
			console.log("AdminCropsCtrl loaded...")

			function findAll() {
				cropService.findAll(function (error, data) {
					if (error) {
						alert("Ocurrió un error: " + error);
						return;
					}

					$scope.data = data;
				})
			}

			$scope.delete = function (id) {

				console.log("Deleting: " + id)

				cropService.delete(id, function (error, data) {
					if (error) {
						console.log(error);
						return;
					}

					$location.path("/");
					$route.reload()
				});
			}

			findAll();
		}]);
