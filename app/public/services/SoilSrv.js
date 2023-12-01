app.service(
    "SoilSrv",
    ["$http",
        function ($http) {

            this.findAll = function (callback) {
                $http.get("rest/soils").then(
                    function (result) {
                        callback(false, result.data);
                    },
                    function (error) {
                        callback(error);
                    });
            }

            this.find = function (id, callback) {
                $http.get("rest/soils/" + id).then(
                    function (result) {
                        callback(false, result.data);
                    },
                    function (error) {
                        callback(error);
                    });
            }

            this.create = function (data, callback) {
                $http.post("rest/soils", data)
                    .then(
                        function (result) {
                            callback(false, result.data);
                        },
                        function (error) {
                            callback(error);
                        });
            }

            this.delete = function (id, callback) {
                $http.delete("rest/soils/" + id)
                    .then(
                        function (result) {
                            callback(false, result.data);
                        },
                        function (error) {
                            callback(error);
                        });
            }

            this.modify = function (data, callback) {
                $http.put("rest/soils/" + data.id, data)
                    .then(
                        function (result) {
                            callback(false, result.data);
                        },
                        function (error) {
                            callback(error);
                        });
            };

            // Esto es necesario para la busqueda que se hace cuando se ingresan caracteres
            this.findActiveSoilByName = function (name) {
                return $http.get("rest/soils/findActiveSoilByName/?soilName=" + name);
            }

        }
    ]);
