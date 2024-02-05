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

      this.searchByPage = function (search, page, cant, callback) {
        $http.get('rest/irrigationRecords/findAllPagination?page=' + page + '&cant=' + cant + "&search=" + JSON.stringify(search))
          .then(function (res) {
            return callback(false, res.data)
          }, function (err) {
            return callback(err.data)
          })
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

      this.saveIrrigationWaterNeedData = function (data, callback) {
        $http.post("rest/irrigationRecords/fromIrrigationWaterNeedFormData", data)
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

      this.delete = function (id, callback) {
        $http.delete("rest/irrigationRecords/" + id)
          .then(
            function (result) {
              callback(false, result.data);
            },
            function (error) {
              callback(error);
            });
      }

      this.filter = function (parcelName, date, callback) {
        $http.get("rest/irrigationRecords/filter?parcelName=" + parcelName + "&date=" + date).then(
          function (result) {
            callback(false, result.data);
          },
          function (error) {
            callback(error);
          });
      };

    }
  ]);
