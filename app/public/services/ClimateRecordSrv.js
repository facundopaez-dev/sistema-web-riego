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

            this.searchByPage = function (search, page, cant, callback) {
                $http
                    .get('rest/climateRecords/findAllPagination?page=' + page + '&cant=' + cant)
                    .then(
                        function (res) {
                            return callback(false, res.data)
                        },
                        function (err) {
                            return callback(err.data)
                        }
                    )
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

            this.delete = function (id, callback) {
                $http.delete("rest/climateRecords/" + id)
                    .then(
                        function (result) {
                            callback(false, result.data);
                        },
                        function (error) {
                            callback(error);
                        });
            }

            this.filter = function (parcelName, date, callback) {
                $http.get("rest/climateRecords/filter?parcelName=" + parcelName + "&date=" + date).then(
                    function (result) {
                        callback(false, result.data);
                    },
                    function (error) {
                        callback(error);
                    });
            };

        }
    ]);
