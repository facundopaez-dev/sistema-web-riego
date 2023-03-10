app.service(
  "IrrigationRecordSrv",
  ["$http",
    function ($http) {

      this.findAll = function (callback) {
        $http.get("rest/irrigationRecords").then(
          function (result) {
            callback(false, result.data);
          },
          function (error) {
            callback(error);
          });
      }

      this.find = function (id, callback) {
        $http.get("rest/irrigationRecords/" + id).then(
          function (result) {
            callback(false, result.data);
          },
          function (error) {
            callback(error);
          });
      }

      this.create = function (data, callback) {
        $http.post("rest/irrigationRecords", data)
          .then(
            function (result) {
              callback(false, result.data);
            },
            function (error) {
              callback(error);
            });
      }

      this.modify = function (data, callback) {
        $http.put("rest/irrigationRecords/" + data.id, data)
          .then(
            function (result) {
              callback(false, result.data);
            },
            function (error) {
              callback(error);
            });
      };

      this.findAllByParcelName = function (parcelName, callback) {
        $http.get("rest/irrigationRecords/findAllByParcelName/" + parcelName).then(
          function (result) {
            callback(false, result.data);
          },
          function (error) {
            callback(error);
          });
      };

    }
  ]);
