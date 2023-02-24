app.service(
	"SignupSrv",
	["$http",
		function ($http) {

			this.signup = function (data, callback) {
				$http.post("rest/signup", data)
					.then(
						function () {
							callback(false);
						},
						function (error) {
							callback(error);
						});
			}

		}
	]);
