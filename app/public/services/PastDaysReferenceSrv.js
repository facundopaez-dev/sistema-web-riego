app.service(
    "PastDaysReferenceSrv",
    ["$http",
        function ($http) {

            this.findAll = function (callback) {
                $http.get("rest/pastDaysReferences").then(
                    function (result) {
                        callback(false, result.data);
                    },
                    function (error) {
                        callback(error);
                    });
            };

            this.find = function (id, callback) {
                $http.get("rest/pastDaysReferences/" + id).then(
                    function (result) {
                        callback(false, result.data);
                    },
                    function (error) {
                        callback(error);
                    });
            }

            this.modify = function (data, callback) {
                $http.put("rest/pastDaysReferences/" + data.id, data)
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
