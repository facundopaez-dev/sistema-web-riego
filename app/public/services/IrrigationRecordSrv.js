app.service(
  "IrrigationRecordSrv",
  ["$http",
    function ($http) {

      this.save = function (data, callback) {
        $http.post("rest/irrigationRecords", data)
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
