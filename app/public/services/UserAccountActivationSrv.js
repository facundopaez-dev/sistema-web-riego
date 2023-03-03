app.service(
	"UserAccountActivationSrv",
	["$http",
		function ($http) {

			this.activateAccount = function (email, callback) {
				$http.put("rest/activateAccount/" + email)
					.then(
						function (result) {
							callback(false);
						},
						function (error) {
							callback(error);
						});
			};

		}
	]);
