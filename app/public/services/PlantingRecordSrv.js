app.service("PlantingRecordSrv", ["$http", function ($http) {

  this.findAll = function (callback) {
    $http.get("rest/plantingRecords").then(
      function (result) {
        callback(false, result.data);
      },
      function (error) {
        callback(error);
      });
  };

  this.searchByPage = function (search, page, cant, callback) {
    $http.get('rest/plantingRecords/findAllPagination?page=' + page + '&cant=' + cant + "&search=" + JSON.stringify(search))
      .then(function (res) {
        return callback(false, res.data)
      }, function (err) {
        return callback(err.data)
      })
  }

  this.find = function (id, callback) {
    $http.get("rest/plantingRecords/" + id).then(
      function (result) {
        callback(false, result.data);
      },
      function (error) {
        callback(error);
      });
  };

  this.create = function (data, callback) {
    $http.post("rest/plantingRecords", data).then(
      function (result) {
        callback(false, result.data);
      },
      function (error) {
        callback(error);
      });
  };

  this.delete = function (id, callback) {
    $http.delete("rest/plantingRecords/" + id)
      .then(
        function (result) {
          callback(false, result.data);
        },
        function (error) {
          callback(error);
        });
  }

  this.modify = function (data, maintainWitheredStatus, callback) {
    $http.put("rest/plantingRecords/" + data.id + "/" + maintainWitheredStatus, data)
      .then(
        function (result) {
          callback(false, result.data);
        },
        function (error) {
          callback(error);
        });
  };

  this.calculateCropIrrigationWaterNeed = function (id, callback) {
    $http.get("rest/plantingRecords/calculateCropIrrigationWaterNeed/" + id).then(
      function (result) {
        callback(false, result.data);
      },
      function (error) {
        callback(error);
      });
  }

  this.filter = function (parcelName, dateFrom, dateUntil, callback) {
    $http.get("rest/plantingRecords/filter?parcelName=" + parcelName + "&dateFrom=" + dateFrom + "&dateUntil=" + dateUntil).then(
      function (result) {
        callback(false, result.data);
      },
      function (error) {
        callback(error);
      });
  };

}]);
