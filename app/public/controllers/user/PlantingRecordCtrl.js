app.controller(
  "PlantingRecordCtrl",
  ["$scope", "$location", "$route", "$routeParams", "PlantingRecordSrv", "IrrigationRecordSrv", "CropSrv", "ParcelSrv", "AccessManager", "ErrorResponseManager",
    "AuthHeaderManager", "LogoutManager", "ExpirationManager", "RedirectManager", "UtilDate",
    function ($scope, $location, $route, $params, plantingRecordService, irrigationRecordService, cropService, parcelService, accessManager, errorResponseManager,
      authHeaderManager, logoutManager, expirationManager, redirectManager, utilDate) {

      console.log("PlantingRecordCtrl loaded with action: " + $params.action)

      /*
      Si el usuario NO tiene una sesion abierta, se le impide el acceso a
      la pagina web correspondiente a este controller y se lo redirige a
      la pagina web de inicio de sesion correspondiente
      */
      if (!accessManager.isUserLoggedIn()) {
        $location.path("/");
        return;
      }

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
      Cada vez que el usuario presiona los botones para crear, editar o
      ver un dato correspondiente a este controller, se debe comprobar
      si su JWT expiro o no. En el caso en el que JWT expiro, se redirige
      al usuario a la pagina web de inicio de sesion correspondiente. En caso
      contrario, se realiza la accion solicitada por el usuario mediante
      el boton pulsado.
      */
      if (expirationManager.isExpire()) {
        expirationManager.displayExpiredSessionMessage();

        /*
        Elimina el JWT del usuario del almacenamiento local del navegador
        web y del encabezado de autorizacion HTTP, ya que un JWT expirado
        no es valido para realizar peticiones HTTP a la aplicacion del
        lado servidor
        */
        expirationManager.clearUserState();

        /*
        Redirige al usuario a la pagina web de inicio de sesion en funcion
        de si inicio sesion como usuario o como administrador. Si inicio
        sesion como usuario, redirige al usuario a la pagina web de
        inicio de sesion del usuario. En cambio, si inicio sesion como
        administrador, redirige al administrador a la pagina web de
        inicio de sesion del administrador.
        */
        redirectManager.redirectUser();
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

      if (['new', 'edit', 'view', 'calculate'].indexOf($params.action) == -1) {
        alert("Acción inválida: " + $params.action);
        $location.path("/home/plantingRecords");
      }

      /*
      Algunos de los estados que puede tener un registro
      de plantacion
      */
      const IN_DEVELOPMENT_STATUS = "En desarrollo";
      const OPTIMAL_DEVELOPMENT_STATE = "Desarrollo óptimo";
      const DEAD_STATUS = "Muerto";

      function find(id) {
        plantingRecordService.find(id, function (error, data) {
          if (error) {
            console.log(error);
            errorResponseManager.checkResponse(error);
            return;
          }

          $scope.data = data.plantingRecord;

          if ($scope.data.seedDate != null) {
            $scope.data.seedDate = new Date($scope.data.seedDate);
          }

          if ($scope.data.harvestDate != null) {
            $scope.data.harvestDate = new Date($scope.data.harvestDate);
          }

          if ($scope.data.deathDate != null) {
            $scope.data.deathDate = new Date($scope.data.deathDate);
          }

          /*
          Variable que controla la visibilidad de la fecha de muerte
          de un cultivo en el formulario
          */
          $scope.showDateDeath = false;

          /*
          Variable que controla la visibilidad del grafico que muestra
          la evolucion diaria del nivel de humedad del suelo
          */
          $scope.showGraph = false;

          /*
          Si el estado de un registro de plantacion es "Muerto" se
          debe mostrar la fecha de muerte del cultivo perteneciente
          a dicho registro.

          Un registro de plantacion representa la siembra de un
          cultivo y tiene un estado que refleja el estado del
          cultivo.
          */
          if ($scope.data.status.name === DEAD_STATUS) {
            $scope.showDateDeath = true;
          }

          /*
          Si el estado de un registro de plantacion a visualizar
          es "Muerto" se debe mostrar el grafico de la evolucion
          diaria del nivel de humedad del suelo.

          Si el registro de plantacion a visualizar tiene el
          estado "Muerto", esto se debe a que la parcela a la que
          pertenece tiene la bandera de suelo activa. La unica
          manera en que un registro de plantacion puede adquirir
          este estado es a traves de la activacion de dicha bandera
          en las opciones de la parcela correspondiente.
          */
          if ($scope.action == 'view' && $scope.data.status.name === DEAD_STATUS) {
            $scope.showGraph = data.soilMoistureLevelGraph.showGraph;
            setSoilMoistureLevelGraphData(data.soilMoistureLevelGraph);
          }

        });
      }

      /*
      Constantes de mensaje en caso de que los datos de entrada
      no sean los correctos
      */
      const EMPTY_FORM = "Debe completar todos los campos del formulario";
      const UNDEFINED_SEED_DATE = "La fecha de siembra debe estar definida";
      const UNDEFINED_HARVEST_DATE = "La fecha de cosecha debe estar definida";
      const UNDEFINED_PARCEL = "La parcela debe estar definida";
      const UNDEFINED_CROP = "El cultivo debe estar definido";
      const OVERLAPPING_SEED_DATE_AND_HARVEST_DATE = "La fecha de siembra no debe ser mayor ni igual a la fecha de cosecha";

      $scope.create = function () {
        /*
        Si la propiedad data de $scope tiene el valor undefined,
        significa que el formulario correspondiente a esta funcion
        esta totalmente vacio. Por lo tanto, la aplicacion muestra
        el mensaje dado y no realiza la operacion solicitada.
        */
        if ($scope.data == undefined) {
          alert(EMPTY_FORM);
          return;
        }

        /*
        **********************************
        Validacion de los datos de entrada
        **********************************
         */

        if ($scope.data.seedDate == undefined) {
          alert(UNDEFINED_SEED_DATE);
          return;
        }

        if ($scope.data.harvestDate == undefined) {
          alert(UNDEFINED_HARVEST_DATE);
          return;
        }

        if (utilDate.compareTo($scope.data.seedDate, $scope.data.harvestDate) >= 0) {
          alert(OVERLAPPING_SEED_DATE_AND_HARVEST_DATE);
          return;
        }

        if ($scope.data.parcel == undefined) {
          alert(UNDEFINED_PARCEL);
          return;
        }

        if ($scope.data.crop == undefined) {
          alert(UNDEFINED_CROP);
          return;
        }

        plantingRecordService.create($scope.data, function (error, data) {
          if (error) {
            console.log(error);
            errorResponseManager.checkResponse(error);
            return;
          }

          $scope.data = data;

          /*
          Si el registro de plantacion creado y devuelto por la aplicacion
          del lado servidor tiene el estado "En desarrollo" o el estado
          "Desarrollo optimo" y la opcion de la parcela a la que pertenece
          tiene la bandera flagMessageFieldCapacity activa, la aplicacion
          muestra el mensaje dado
          */
          if (($scope.data.status.name === IN_DEVELOPMENT_STATUS || $scope.data.status.name === OPTIMAL_DEVELOPMENT_STATE) &&
            $scope.data.parcel.option.flagMessageFieldCapacity) {
            var advice = "Recuerde que un día antes de la fecha de siembra de un cultivo se debe llenar de agua el suelo, en el que realizará la "
              + "siembra, para que en la fecha de siembra el nivel de humedad del suelo esté en capacidad de campo. Esto se debe a que la aplicación "
              + "calcula la necesidad de agua de riego de un cultivo en la fecha actual como la cantidad de agua que se debe reponer para llevar el "
              + "suelo, en el que está sembrado un cultivo, a capacidad de campo partiendo desde la condición de suelo a capacidad de campo en la "
              + "fecha de siembra de un cultivo. Capacidad de campo es la condicion en la que un suelo está lleno de agua, pero no anegado. Puede "
              + "deshabilitar este aviso en el formulario de opción de una parcela.";
            alert(advice);
          }

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
        **********************************
        Validacion de los datos de entrada
        **********************************
         */

        if ($scope.data.seedDate == undefined) {
          alert(UNDEFINED_SEED_DATE);
          return;
        }

        if ($scope.data.harvestDate == undefined) {
          alert(UNDEFINED_HARVEST_DATE);
          return;
        }

        if (utilDate.compareTo($scope.data.seedDate, $scope.data.harvestDate) >= 0) {
          alert(OVERLAPPING_SEED_DATE_AND_HARVEST_DATE);
          return;
        }

        if ($scope.data.parcel == undefined) {
          alert(UNDEFINED_PARCEL);
          return;
        }

        if ($scope.data.crop == undefined) {
          alert(UNDEFINED_CROP);
          return;
        }

        const currentDate = new Date();
        let maintainDeadStatus = false;

        /*
        Si el registro de plantacion a modificar tiene el estado "Muerto"
        y la fecha de cosecha es anterior a la fecha actual (es decir,
        hoy), se solicita al usuario que confirme si desea mantener ese
        estado.
        */
        if ($scope.data.status.name == DEAD_STATUS && utilDate.compareTo($scope.data.harvestDate, currentDate) < 0) {
          var message = "¿Desea que el registro de plantación mantenga el estado muerto? En caso afirmativo, tenga en cuenta que, para conservarlo,"
            + " el único dato que puede modificar es la fecha de cosecha, la cual debe ser anterior a la fecha actual (hoy) y, al mismo tiempo, igual o"
            + " posterior a la fecha de muerte para mantener dicho estado";
          maintainDeadStatus = confirm(message);
        }

        plantingRecordService.modify($scope.data, maintainDeadStatus, function (error, data) {
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

          $location.path("/home/plantingRecords")
          $route.reload();
        });
      }

      $scope.cancel = function () {
        $location.path("/home/plantingRecords");
      }

      // Esto es necesario para la busqueda que se hace cuando se ingresan caracteres
      $scope.findActiveParcelByName = function (parcelName) {
        return parcelService.findActiveParcelByName(parcelName).
          then(function (response) {
            var parcels = [];
            for (var i = 0; i < response.data.length; i++) {
              parcels.push(response.data[i]);
            }

            return parcels;
          });;
      }

      // Esto es necesario para la busqueda que se hace cuando se ingresan caracteres
      $scope.findActiveCropByName = function (cropName) {
        return cropService.findActiveCropByName(cropName).
          then(function (response) {
            var crops = [];
            for (var i = 0; i < response.data.length; i++) {
              crops.push(response.data[i]);
            }

            return crops;
          });;
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

      function calculateCropIrrigationWaterNeed(id) {
        /*
        Propiedad de $scope utilizada para controlar la visibilidad de la
        animacion de carga en el formulario de calculo de la necesidad de
        agua de riego de un cultivo en la fecha actual (hoy) [mm/dia].
        */
        $scope.showLoadingAnimation = true;

        /*
        Variable que controla la visibilidad del grafico que muestra
        la evolucion diaria del nivel de humedad del suelo
        */
        $scope.showGraph = false;

        /*
        Variable que controla la visibilidad del nombre del suelo de
        una parcela en el formulario de calculo de la necesidad de
        agua de riego, si la parcela tiene un suelo asignado
        */
        $scope.showSoilName = false;

        plantingRecordService.calculateCropIrrigationWaterNeed(id, function (error, cropIrrigationWaterNeedData) {
          if (error) {
            console.log(error);
            errorResponseManager.checkResponse(error);
            return;
          }

          /*
          Cuando la aplicacion del lado servidor retorna la respuesta
          del calculo de la necesidad de agua de riego de un cultivo
          en la fecha actual [mm/dia], se oculta la animacion de carga.
          */
          $scope.showLoadingAnimation = false;

          /*
          Si la bandera "suelo" de las opciones de una parcela correspondiente
          al registro de plantacion que tiene el cultivo en desarrollo para el
          que se calcula la necesidad de agua de riego en la fecha actual (es
          decir, hoy) esta activa, se deben realizar las siguientes operaciones:
          - Mostrar el nombre del suelo de la parcela.
          - Configurar las propiedades del grafico que muestra la evolucion
          diaria del nivel de humedad del suelo.
          - Mostrar dicho grafico.
          */
          if (cropIrrigationWaterNeedData.parcel.option.soilFlag) {
            $scope.showSoilName = true;
            $scope.showGraph = cropIrrigationWaterNeedData.soilMoistureLevelGraph.showGraph;
            setSoilMoistureLevelGraphData(cropIrrigationWaterNeedData.soilMoistureLevelGraph);
          }

          /*
          Si esta instruccion no esta, no se puede ver la
          necesidad de agua de riego en el formulario del
          calculo de la necesidad de agua de riego de un
          cultivo
          */
          $scope.cropIrrigationWaterNeedData = cropIrrigationWaterNeedData;
        });
      }

      $scope.saveIrrigationWaterNeedData = function () {

        /*
        Este control es para el caso en el que el usuario presiona
        el boton "Aceptar" del formulario del calculo de la necesidad
        de agua de riego de un cultivo en la fecha actual con los
        campos vacios
        */
        if ($scope.cropIrrigationWaterNeedData == undefined) {
          return;
        }

        if ($scope.cropIrrigationWaterNeedData.irrigationDone >= 0) {
          irrigationRecordService.saveIrrigationWaterNeedData($scope.cropIrrigationWaterNeedData, function (error) {
            if (error) {
              console.log(error);
              errorResponseManager.checkResponse(error);
              return;
            }

            $location.path("/home/plantingRecords");
          });
        } else {
          alert("El riego realizado debe ser mayor o igual a cero");
        }

      }

      $scope.action = $params.action;

      if ($scope.action == 'edit' || $scope.action == 'view') {
        find($params.id);
      }

      if ($scope.action == 'calculate') {
        calculateCropIrrigationWaterNeed($params.id);
      }

      /**
       * Establece las propiedades del grafico de lineas que representa
       * la evolucion diaria del nivel de humedad del suelo de una
       * parcela que tiene la bandera suelo activa en sus opciones
       * 
       * @param {*} soilMoistureLevelGraph 
       */
      function setSoilMoistureLevelGraphData(soilMoistureLevelGraph) {
        var data = {
          labels: soilMoistureLevelGraph.labels,
          datasets: [
            {
              fill: true,
              backgroundColor: "rgba(32, 162, 219, 0.3)",
              data: soilMoistureLevelGraph.data,
            }
          ]
        };

        var options = {
          title: {
            display: true,
            text: soilMoistureLevelGraph.text
          },
          elements: {
            line: {
              tension: 0
            }
          },
          scales: {
            yAxes: [{
              ticks: {
                suggestedMax: soilMoistureLevelGraph.totalAmountWaterAvailable + 20,
                suggestedMin: soilMoistureLevelGraph.negativeTotalAmountWaterAvailable - 20
              }
            }]
          },
          annotation: {
            events: ['mouseenter', 'mouseleave'],
            annotations: [{
              type: 'line',
              mode: 'horizontal',
              scaleID: 'y-axis-0',
              value: soilMoistureLevelGraph.totalAmountWaterAvailable,
              borderColor: 'black',
              borderDash: [6, 6],
              borderDashOffset: 0,
              borderWidth: 2,
              label: {
                enabled: true,
                backgroundColor: 'black',
                content: 'Capacidad de campo (CC) [mm]: ' + soilMoistureLevelGraph.totalAmountWaterAvailable
              },
              onMouseleave: function (e) {
                // console.log("onMouseleave", e);
                this.options.borderColor = "black";
                this.options.label.backgroundColor = 'black';
                this.options.label.fontColor = 'white';
                graph.update();
              },
              onMouseenter: function (e) {
                // console.log("onMouseenter", e);
                this.options.borderColor = "rgba(0,0,0,0)";
                this.options.label.backgroundColor = 'rgba(0,0,0,0)';
                this.options.label.fontColor = 'rgba(0,0,0,0)';
                graph.update();
              }
            },
            {
              type: 'line',
              mode: 'horizontal',
              scaleID: 'y-axis-0',
              value: soilMoistureLevelGraph.optimalIrrigationLayer,
              borderColor: 'black',
              borderDash: [6, 6],
              borderDashOffset: 0,
              borderWidth: 2,
              label: {
                enabled: true,
                backgroundColor: 'black',
                content: 'Umbral de riego [mm]: ' + soilMoistureLevelGraph.optimalIrrigationLayer,
              },
              onMouseleave: function (e) {
                // console.log("onMouseleave", e);
                this.options.borderColor = "black";
                this.options.label.backgroundColor = 'black';
                this.options.label.fontColor = 'white';
                graph.update();
              },
              onMouseenter: function (e) {
                // console.log("onMouseenter", e);
                this.options.borderColor = "rgba(0,0,0,0)";
                this.options.label.backgroundColor = 'rgba(0,0,0,0)';
                this.options.label.fontColor = 'rgba(0,0,0,0)';
                graph.update();
              }
            },
            {
              type: 'line',
              mode: 'horizontal',
              scaleID: 'y-axis-0',
              value: 0,
              borderColor: 'black',
              borderDash: [6, 6],
              borderDashOffset: 0,
              borderWidth: 2,
              label: {
                enabled: true,
                backgroundColor: 'black',
                content: 'Pto. de marchitez permanente [mm]: 0'
              },
              onMouseleave: function (e) {
                // console.log("onMouseleave", e);
                this.options.borderColor = "black";
                this.options.label.backgroundColor = 'black';
                this.options.label.fontColor = 'white';
                graph.update();
              },
              onMouseenter: function (e) {
                // console.log("onMouseenter", e);
                this.options.borderColor = "rgba(0,0,0,0)";
                this.options.label.backgroundColor = 'rgba(0,0,0,0)';
                this.options.label.fontColor = 'rgba(0,0,0,0)';
                graph.update();
              }
            },
            {
              type: 'line',
              mode: 'horizontal',
              scaleID: 'y-axis-0',
              value: soilMoistureLevelGraph.negativeTotalAmountWaterAvailable,
              borderColor: 'black',
              borderDash: [6, 6],
              borderDashOffset: 0,
              borderWidth: 2,
              label: {
                enabled: true,
                backgroundColor: 'black',
                content: 'Lím. inferior (LI) [mm]: ' + soilMoistureLevelGraph.negativeTotalAmountWaterAvailable
                  + ' (de CC a LI hay ' + (2 * soilMoistureLevelGraph.totalAmountWaterAvailable) + ' [mm] = 2 * CC)'
              },
              onMouseleave: function (e) {
                // console.log("onMouseleave", e);
                this.options.borderColor = "black";
                this.options.label.backgroundColor = 'black';
                this.options.label.fontColor = 'white';
                graph.update();
              },
              onMouseenter: function (e) {
                // console.log("onMouseenter", e);
                this.options.borderColor = "rgba(0,0,0,0)";
                this.options.label.backgroundColor = 'rgba(0,0,0,0)';
                this.options.label.fontColor = 'rgba(0,0,0,0)';
                graph.update();
              }
            }]
          }
        };

        var ctx = document.getElementById("soilMoistureLevelGraph");

        var graph = new Chart(ctx, {
          type: 'line',
          data: data,
          options: options,
        });
      }

    }]);
