app.service(
	"TypePrecipSrv",
	["$http",
		function ($http) {

			this.findAll = function (callback) {
				$http.get("rest/typesPrecip").then(
					function (result) {
						callback(false, result.data);
					},
					function (error) {
						callback(error);
					});
			}

		}
	]);
