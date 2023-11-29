app.service(
    "SoilWaterBalanceSrv",
    ["$http",
        function ($http) {

            this.filter = function (dateFrom, dateUntil, parcelName, cropName, callback) {
                $http.get("rest/soilWaterBalances/filter?dateFrom=" + dateFrom + "&dateUntil=" + dateUntil + "&parcelName=" + parcelName + "&cropName=" + cropName).then(
                    function (result) {
                        callback(false, result.data);
                    },
                    function (error) {
                        callback(error);
                    });
            };

        }
    ]);
