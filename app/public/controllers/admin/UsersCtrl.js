app.controller(
	"UsersCtrl",
	["$scope", "$location", "$route", "UserSrv",
		function ($scope, $location, $route, userService) {
			console.log("UsersCtrl loaded...")

			function findAll() {
				userService.findAll(function (error, data) {
					if (error) {
						alert("Ocurrió un error: " + error);
						return;
					}

					$scope.data = data;
				})
			}

			$scope.delete = function (id) {

				console.log("Deleting: " + id)

				userService.delete(id, function (error, data) {
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
