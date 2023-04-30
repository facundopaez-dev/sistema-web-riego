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

  this.modify = function (data, callback) {
    $http.put("rest/plantingRecords/" + data.id, data)
      .then(
        function (result) {
          callback(false, result.data);
        },
        function (error) {
          callback(error);
        });
  };

  this.calculateIrrigationWaterNeed = function (id, callback) {
    $http.get("rest/plantingRecords/irrigationWaterNeed/" + id).then(
      function (result) {
        callback(false, result.data);
      },
      function (error) {
        callback(error);
      });
  }

  this.findAllByParcelName = function (parcelName, callback) {
    $http.get("rest/plantingRecords/findAllByParcelName/" + parcelName).then(
      function (result) {
        callback(false, result.data);
      },
      function (error) {
        callback(error);
      });
  };

}]);
