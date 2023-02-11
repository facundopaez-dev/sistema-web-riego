app.controller(
  "UserCropCtrl",
  ["$scope", "$location", "$routeParams", "CropSrv",
    function ($scope, $location, $params, cropService) {

      /*
      *****************************************************************
      Este controller es para que el usuario pueda ver un cultivo en
      detalle, en lugar de verlos unicamente en una lista desplegada
      en el formulario de un registro de plantacion.

      Hay que tener en cuenta que el usuario solo puede leer un
      cultivo. El administrador es quien puede crear, leer y modificar
      un cultivo. Este es el motivo por el cual en este controller
      solo esta programada la opcion de visualizacion.
      *****************************************************************
      */

      console.log("UserCropCtrl loaded with action: " + $params.action)

      if (['view'].indexOf($params.action) == -1) {
        alert("Invalid action: " + $params.action);
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

      $scope.goBack = function () {
        $location.path("/");
      }

      $scope.action = $params.action;

      if ($scope.action == 'view') {
        find($params.id);
      }

    }]);
