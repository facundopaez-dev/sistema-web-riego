app.controller(
  "PlantingRecordCtrl",
  ["$scope", "$location", "$route", "$routeParams", "PlantingRecordSrv", "CropSrv", "ParcelSrv", "AccessManager", "ErrorResponseManager", "AuthHeaderManager",
    "LogoutManager", function ($scope, $location, $route, $params, plantingRecordService, cropService, parcelService, accessManager, errorResponseManager,
      authHeaderManager, logoutManager) {
      console.log("PlantingRecordCtrl loaded with action: " + $params.action)

      /*
      Si el usuario que tiene una sesion abierta tiene permiso de
      administrador, se lo redirige a la pagina de inicio del
      administrador. De esta manera, un administrador debe cerrar
      la sesion que abrio a traves de la pagina web de inicio de sesion
      del administrador, y luego abrir una sesion a traves de la pagina
      web de inicio de sesion del usuario para poder acceder a la pagina
      de inicio del usuario.
      */
      if (accessManager.isUserLoggedIn() && accessManager.loggedAsAdmin()) {
        $location.path("/adminHome");
        return;
      }

      /*
      Cuando el usuario abre una sesion satisfactoriamente y no la cierra,
      y accede a la aplicacion web mediante una nueva pestaña, el encabezado
      de autorizacion HTTP tiene el valor undefined. En consecuencia, las
      peticiones HTTP con este encabezado no seran respondidas por la
      aplicacion del lado servidor, ya que esta opera con JWT para la
      autenticacion, la autorizacion y las operaciones con recursos
      (lectura, modificacion y creacion).

      Este es el motivo por el cual se hace este control. Si el encabezado
      HTTP de autorizacion tiene el valor undefined, se le asigna el JWT
      del usuario.

      De esta manera, cuando el usuario abre una sesion satisfactoriamente
      y no la cierra, y accede a la aplicacion web mediante una nueva pestaña,
      el encabezado HTTP de autorizacion contiene el JWT del usuario, y, por
      ende, la peticion HTTP que se realice en la nueva pestaña, sera respondida
      por la aplicacion del lado servidor.
      */
      if (authHeaderManager.isUndefined()) {
        authHeaderManager.setJwtAuthHeader();
      }

      if (['new', 'edit', 'view'].indexOf($params.action) == -1) {
        alert("Acción inválida: " + $params.action);
        $location.path("/home/plantingRecords");
      }

      function find(id) {
        plantingRecordService.find(id, function (error, data) {
          if (error) {
            console.log(error);
            errorResponseManager.checkResponse(error);
            return;
          }

          $scope.data = data;

          if ($scope.data.seedDate != null) {
            $scope.data.seedDate = new Date($scope.data.seedDate);
          }

          if ($scope.data.harvestDate != null) {
            $scope.data.harvestDate = new Date($scope.data.harvestDate);
          }
        });
      }

      $scope.create = function () {
        /*
        Comprueba que los campos no esten vacios e impide
        que se ingresen los campos vacios
         */
        if (isNull($scope.data.seedDate)) {
          alert("La fecha de siembra debe estar definida");
          return;
        }

        if (isNull($scope.data.parcel)) {
          alert("La parcela debe estar definida");
          return;
        }

        if (isNull($scope.data.crop)) {
          alert("El cultivo debe estar definido");
          return;
        }

        /*
        Si ya hay un registro historico de parcela de la parcela dada
        en el estado "En desarrollo", entonces no se tiene que crear
        el nuevo registro historico de parcela
         */
        plantingRecordService.findCurrentPlantingRecord($scope.data.parcel.id, function (error, data) {
          /*
          Todo este bloque de codigo sin el bloque de codigo llamado
          findCurrentPlantingRecord (el de la clase PlantingRecordServiceBean, Java)
          no sirve, ya que este bloque de codigo solo muestra el cartel que esta mas
          abajo y no hace nada mas que eso, mientras que el metodo Java mencionado
          utilizado en el metodo create de la PlantingRecordRestServlet permite
          determinar si se tiene que persistir o no el nuevo registro de plantacion
           */

          // TODO: Borrar este bloque de codigo cuando se implmente este control en el backend

          if (error) {
            console.log(error);
            return;
          }

          $scope.data = data;

          if ($scope.data != null) {
            alert("No está permitido crear un registro de plantación habiendo otro en el estado 'En desarrollo' para la misma parcela");
            return;
          }

        });

        /*
        Si el ultimo registro historico de parcela esta en el estado
        "Finalizado" pero la fecha de siembra del nuevo registro
        historico de parcela esta detras de la fecha de cosecha del
        registro historico de parcela anterior, entonces no se tiene
        que crear el nuevo registro historico de parcela
         */

        plantingRecordService.create($scope.data, function (error, data) {
          if (error) {
            console.log(error);
            errorResponseManager.checkResponse(error);
            return;
          }

          $scope.data = data;

          if ($scope.data.seedDate != null) {
            $scope.data.seedDate = new Date($scope.data.seedDate);
          }

          if ($scope.data.harvestDate != null) {
            $scope.data.harvestDate = new Date($scope.data.harvestDate);
          }

          $location.path("/home/plantingRecords");
        });

      }

      $scope.modify = function () {
        /*
        Comprueba que los campos de fecha no esten vacios
        e impide que se ingresen los campos vacios
         */
        if (isNull($scope.data.seedDate) || isNull($scope.data.harvestDate)) {
          alert("Las fechas deben estar definidas");
          return;
        }

        /*
        Si la fecha de siembra y la fecha de cosecha estan cruzadas
        o superpuestas no se realiza la modificacion
         */
        if ((firstDateAfterSecondDate($scope.data.seedDate, $scope.data.harvestDate)) == true) {
          alert("La fecha de cosecha tiene que estar después de la fecha de siembra");
          return;
        }

        /*
        Comprobar que la diferencia de dias entre la fecha de siembra y
        la fecha de cosecha no sea mayor que la etapa de vida del cultivo
        dado
         */
        // plantingRecordService.checkStageCropLife($scope.data.crop, $scope.data.seedDate, $scope.data.harvestDate, function(error, data) {
        //   if(error) {
        //     alert(error.statusText);
        //     return;
        //   }
        //
        //   let result = data;
        //   let totalDaysLife = $scope.data.crop.etInicial + $scope.data.crop.etDesarrollo + $scope.data.crop.etMedia + $scope.data.crop.etFinal;
        //
        //   if (result != null) {
        //     alert("La diferencia en días entre ambas fechas es mayor a la cantidad de días de la etapa de vida (" + totalDaysLife + ") del cultivo seleccionado");
        //     return;
        //   }
        //
        // });

        plantingRecordService.modify($scope.data, function (error, data) {
          if (error) {
            console.log(error);
            errorResponseManager.checkResponse(error);
            return;
          }

          $scope.data = data;
          $location.path("/home/plantingRecords")
          $route.reload();
        });
      }

      $scope.cancel = function () {
        $location.path("/home/plantingRecords");
      }

      function findAllActiveCrops() {
        cropService.findAllActive(function (error, crops) {
          if (error) {
            alert("Ocurrio un error: " + error);
            return;
          }
          $scope.crops = crops;
        })
      }

      function findAllActiveParcels() {
        parcelService.findAllActive(function (error, parcels) {
          if (error) {
            alert("Ocurrio un error: " + error);
            return;
          }

          $scope.parcels = parcels;
        })
      }

      /*
      Comprueba si la primera fecha esta despues de
      la segunda fecha, es decir, si estan cruzadas o
      superpuestas
       */
      function firstDateAfterSecondDate(firstDate, secondDate) {
        /*
        Si la primera fecha es mayor o igual que la segunda fecha
        retorna verdadero, en caso contrario retorna falso
         */
        if (Date.parse(firstDate) >= Date.parse(secondDate)) {
          return true;
        }

        return false;
      }

      // function firstDateBeforeSecondDate(firstDate, secondDate) {
      //   /*
      //   Si la primera fecha es menor o igual que la segunda fecha
      //   retorna verdadero, en caso contrario retorna falso
      //    */
      //   if (Date.parse(secondDate) >= Date.parse(firstDate)) {
      //     return true;
      //   }
      //
      //   return false;
      // }

      function isNull(givenValue) {
        if (givenValue == null) {
          return true;
        }

        return false;
      }

      $scope.logout = function () {
        /*
        LogoutManager es la factory encargada de realizar el cierre de
        sesion del usuario. Durante el cierre de sesion, la funcion
        logout de la factory mencionada, realiza la peticion HTTP de
        cierre de sesion (elimina logicamente la sesion activa del
        usuario en la base de datos, la cual, esta en el lado servidor),
        la eliminacion del JWT del usuario, el borrado del contenido del
        encabezado HTTP de autorizacion, el establecimiento en false del
        valor asociado a la clave "superuser" del almacenamiento local del
        navegador web y la redireccion a la pagina web de inicio de sesion
        correspondiente dependiendo si el usuario inicio sesion como
        administrador o no.
        */
        logoutManager.logout();
      }

      $scope.action = $params.action;

      if ($scope.action == 'new' || $scope.action == 'edit' || $scope.action == 'view') {
        findAllActiveParcels();
        findAllActiveCrops();
      }

      if ($scope.action == 'edit' || $scope.action == 'view') {
        find($params.id);
      }

    }]);
