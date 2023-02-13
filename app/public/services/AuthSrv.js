app.service(
    "AuthSrv",
    ["$http",
        function ($http) {

            this.authenticateUser = function (data, callback) {
                $http.post("rest/auth/user", data)
                    .then(
                        function (result) {
                            callback(false, result.data);
                        },
                        function (error) {
                            callback(error);
                        });
            };

            this.authenticateAdmin = function (data, callback) {
                $http.post("rest/auth/admin", data)
                    .then(
                        function (result) {
                            callback(false, result.data);
                        },
                        function (error) {
                            callback(error);
                        });
            };

        }
    ]);
