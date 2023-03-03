app.controller(
  "UserAccountActivationCtrl",
  ["$location", "$routeParams", "UserAccountActivationSrv", "ErrorResponseManager",
    function ($location, $params, userAccountActivationService, errorResponseManager) {

      console.log("UserAccountActivationCtrl loaded...");

      function activateAccount() {
        userAccountActivationService.activateAccount($params.email, function (error) {
          if (error) {
            console.log("Ocurrió un error: " + error);
            errorResponseManager.checkResponse(error);
            return;
          }

          alert("Cuenta satisfactoriamente activada");
          $location.path("/");
        })
      }

      activateAccount();
    }]);
