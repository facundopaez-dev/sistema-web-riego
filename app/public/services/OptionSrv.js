app.service(
    "OptionSrv",
    ["$http",
        function ($http) {

            this.find = function (callback) {
                $http.get("rest/user/option").then(
                    function (result) {
                        callback(false, result.data);
                    },
                    function (error) {
                        callback(error);
                    });
            }

            this.modify = function (data, callback) {
                $http.put("rest/user/option", data)
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
