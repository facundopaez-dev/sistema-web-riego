app.service(
	"MonthSrv",
	["$http",
		function ($http) {

			this.findAll = function (callback) {
				$http.get("rest/months").then(
					function (result) {
						callback(false, result.data);
					},
					function (error) {
						callback(error);
					});
			}

			// Esto es necesario para la busqueda que se hace cuando se ingresan caracteres
			this.findByName = function (name) {
				return $http.get("rest/months/findByName/?monthName=" + name);
			}


		}
	]);
