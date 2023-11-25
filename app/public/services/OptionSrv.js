app.service(
    "OptionSrv",
    ["$http",
        function ($http) {

            this.find = function (id, callback) {
                $http.get("rest/options/" + id).then(
                    function (result) {
                        callback(false, result.data);
                    },
                    function (error) {
                        callback(error);
                    });
            }

            this.modify = function (data, callback) {
                $http.put("rest/options/" + data.id, data)
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
