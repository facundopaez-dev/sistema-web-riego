app.service(
	"TypeCropSrv",
	["$http",
		function ($http) {

			this.findAll = function (callback) {
				$http.get("rest/typeCrops").then(
					function (result) {
						callback(false, result.data);
					},
					function (error) {
						callback(error);
					});
			}

			this.create = function (data, callback) {
				$http.post("rest/typeCrops", data)
					.then(
						function (result) {
							callback(false, result.data);
						},
						function (error) {
							callback(error);
						});
			}

		}
	]);