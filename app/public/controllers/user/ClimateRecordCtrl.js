app.controller(
  "ClimateRecordCtrl",
  ["$scope", "$location", "$routeParams", "ClimateRecordSrv",
    function ($scope, $location, $params, climateRecordService) {

      console.log("ClimateRecordCtrl loaded with action: " + $params.action)

      if (['new', 'edit', 'view'].indexOf($params.action) == -1) {
        alert("Acción inválida: " + $params.action);
        $location.path("/");
      }

      function find(id) {
        climateRecordService.find(id, function (error, data) {
          if (error) {
            console.log(error);
            return;
          }

          $scope.data = data;

          if ($scope.data.date != null) {
            $scope.data.date = new Date($scope.data.date);
          }
        });
      }

      $scope.create = function () {
        climateRecordService.create($scope.data, function (error, data) {
          if (error) {
            console.log(error);
            return;
          }

          $scope.data = data;
          $location.path("/")
        });
      }

      $scope.modify = function () {
        climateRecordService.modify($scope.data, function (error, data) {
          if (error) {
            console.log(error);
            return;
          }

          $scope.data = data;
          $location.path("/")
        });
      }

      $scope.cancel = function () {
        $location.path("/");
      }

      $scope.action = $params.action;

      if ($scope.action == 'edit' || $scope.action == 'view') {
        find($params.id);
      }

    }]);
