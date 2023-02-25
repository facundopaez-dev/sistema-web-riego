app.service(
    "ClimateRecordSrv",
    ["$http",
        function ($http) {

            this.findAll = function (callback) {
                $http.get("rest/climateRecords").then(
                    function (result) {
                        callback(false, result.data);
                    },
                    function (error) {
                        callback(error);
                    });
            }

            this.find = function (id, callback) {
                $http.get("rest/climateRecords/" + id).then(
                    function (result) {
                        callback(false, result.data);
                    },
                    function (error) {
                        callback(error);
                    });
            }

            this.create = function (data, callback) {
                $http.post("rest/climateRecords", data)
                    .then(
                        function (result) {
                            callback(false, result.data);
                        },
                        function (error) {
                            callback(error);
                        });
            }

            this.modify = function (data, callback) {
                $http.put("rest/climateRecords/" + data.id, data)
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
