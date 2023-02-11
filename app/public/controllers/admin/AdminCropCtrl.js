app.controller(
  "AdminCropCtrl",
  ["$scope", "$location", "$routeParams", "CropSrv",
    function ($scope, $location, $params, cropService) {

      console.log("AdminCropCtrl loaded with action: " + $params.action)

      if (['new', 'edit', 'view'].indexOf($params.action) == -1) {
        alert("Acción inválida: " + $params.action);
        $location.path("/");
      }

      function find(id) {
        cropService.find(id, function (error, data) {
          if (error) {
            console.log(error);
            return;
          }
          $scope.data = data;
        });
      }

      $scope.create = function () {
        cropService.create($scope.data, function (error, data) {
          if (error) {
            console.log(error);
            return;
          }

          $scope.data = data;
          $location.path("/")
        });
      }

      $scope.modify = function () {
        cropService.modify($scope.data, function (error, data) {
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
