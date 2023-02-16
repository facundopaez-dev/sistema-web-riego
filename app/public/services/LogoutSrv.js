app.service(
	"LogoutSrv",
	["$http",
		function ($http) {

			this.logout = function (callback) {
				$http.post("rest/logout")
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
