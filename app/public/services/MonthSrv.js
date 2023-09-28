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

		}
	]);
