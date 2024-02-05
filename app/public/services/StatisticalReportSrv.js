app.service(
    "StatisticalReportSrv",
    ["$http",
        function ($http) {

            this.findAll = function (callback) {
                $http.get("rest/statisticalReports").then(
                    function (result) {
                        callback(false, result.data);
                    },
                    function (error) {
                        callback(error);
                    });
            }

            this.searchByPage = function (search, page, cant, callback) {
                $http.get('rest/statisticalReports/findAllPagination?page=' + page + '&cant=' + cant + "&search=" + JSON.stringify(search))
                    .then(function (res) {
                        return callback(false, res.data)
                    }, function (err) {
                        return callback(err.data)
                    })
            }

            this.find = function (id, callback) {
                $http.get("rest/statisticalReports/" + id).then(
                    function (result) {
                        callback(false, result.data);
                    },
                    function (error) {
                        callback(error);
                    });
            }

            this.create = function (data, callback) {
                $http.post("rest/statisticalReports", data)
                    .then(
                        function (result) {
                            callback(false, result.data);
                        },
                        function (error) {
                            callback(error);
                        });
            }

            this.delete = function (id, callback) {
                $http.delete("rest/statisticalReports/" + id)
                    .then(
                        function (result) {
                            callback(false, result.data);
                        },
                        function (error) {
                            callback(error);
                        });
            }

            this.filter = function (parcelName, dateFrom, dateUntil, callback) {
                $http.get("rest/statisticalReports/filter?parcelName=" + parcelName + "&dateFrom=" + dateFrom + "&dateUntil=" + dateUntil).then(
                    function (result) {
                        callback(false, result.data);
                    },
                    function (error) {
                        callback(error);
                    });
            };

        }
    ]);
