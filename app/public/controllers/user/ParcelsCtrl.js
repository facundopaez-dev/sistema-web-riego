app.controller(
  "ParcelsCtrl",
  ["$scope", "$location", "$route", "ParcelSrv",
    function ($scope, $location, $route, parcelService) {
      console.log("ParcelsCtrl loaded...")

      function findAll() {
        parcelService.findAll(function (error, data) {
          if (error) {
            alert("Ocurrió un error: " + error);
            return;
          }
          $scope.data = data;
        })
      }

      /* Esto ess necesario para la paginacion */
      var $ctrl = this;

      $scope.parcelService = parcelService;
      $scope.listElement = []
      $scope.cantPerPage = 20
      /* Esto ess necesario para la paginacion */

      $scope.delete = function (id) {
        console.log("Deleting: " + id)

        parcelService.delete(id, function (error, data) {
          if (error) {
            console.log(error);
            return;
          }

          $location.path("/parcels");
          $route.reload()
        });
      }

      findAll();
    }]);
