app.service(
	"StatisticalDataSrv",
	["$http",
		function ($http) {

			this.findAll = function (callback) {
				$http.get("rest/statisticalData").then(
					function (result) {
						callback(false, result.data);
					},
					function (error) {
						callback(error);
					});
			}

		}
	]);
