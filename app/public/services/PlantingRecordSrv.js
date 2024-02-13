app.service("PlantingRecordSrv", ["$http", function ($http) {

  const propertyParcel = 'parcel';
  const propertySeedDate = 'seedDate';
  const propertyHarvestDate = 'harvestDate';

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
    var jsonSearch = JSON.stringify(search);
    const objectSearch = JSON.parse(jsonSearch);

    /*
    Si en el objeto de busqueda esta presenta unicamente la propiedad
    parcel significa que se desea filtrar los registros de plantacion
    unicamente por el nombre de la parcela. Por lo tanto, para realizar
    la filtracion se crea una cadena JSON con la propiedad parcel y su
    valor.
    */
    if (objectSearch.hasOwnProperty(propertyParcel) && objectSearch.parcel != null) {

      if ((!objectSearch.hasOwnProperty(propertySeedDate) || (objectSearch.hasOwnProperty(propertySeedDate) && objectSearch.seedDate == null))
        && (!objectSearch.hasOwnProperty(propertyHarvestDate) || (objectSearch.hasOwnProperty(propertyHarvestDate) && objectSearch.harvestDate == null))) {
        jsonSearch = "\{\"parcel\": \"" + objectSearch.parcel.name + "\"}";
      }

    }

    /*
    Si en el objeto de busqueda esta presenta unicamente la propiedad
    seedDate significa que se desea filtrar los registros de plantacion
    unicamente por la fecha de siembra. Por lo tanto, para realizar la
    filtracion se crea una cadena JSON con la propiedad seedDate y su
    valor.
    */
    if (objectSearch.hasOwnProperty(propertySeedDate) && objectSearch.seedDate != null) {

      if ((!objectSearch.hasOwnProperty(propertyParcel) || (objectSearch.hasOwnProperty(propertyParcel) && objectSearch.parcel == null))
        && (!objectSearch.hasOwnProperty(propertyHarvestDate) || (objectSearch.hasOwnProperty(propertyHarvestDate) && objectSearch.harvestDate == null))) {
        jsonSearch = "\{\"seedDate\": \"" + objectSearch.seedDate + "\"}";
      }

    }

    /*
    Si en el objeto de busqueda esta presenta unicamente la propiedad
    harvestDate significa que se desea filtrar los registros de plantacion
    unicamente por la fecha de cosecha. Por lo tanto, para realizar la
    filtracion se crea una cadena JSON con la propiedad harvestDate y su
    valor.
    */
    if (objectSearch.hasOwnProperty(propertyHarvestDate) && objectSearch.harvestDate != null) {

      if ((!objectSearch.hasOwnProperty(propertyParcel) || (objectSearch.hasOwnProperty(propertyParcel) && objectSearch.parcel == null))
        && (!objectSearch.hasOwnProperty(propertySeedDate) || (objectSearch.hasOwnProperty(propertySeedDate) && objectSearch.seedDate == null))) {
        jsonSearch = "\{\"harvestDate\": \"" + objectSearch.harvestDate + "\"}";
      }

    }

    /*
    Si en el objeto de busqueda estan presentes unicamente las propiedades
    parcel y seedDate significa que se desea filtrar los registros de
    plantacion unicamente por la parcela y la fecha de siembra. Por lo
    tanto, para realizar la filtracion se crea una cadena JSON con las
    propiedades parcel y seedDate junto con sus respectivos valores.
    */
    if (objectSearch.hasOwnProperty(propertyParcel) && objectSearch.hasOwnProperty(propertySeedDate) && objectSearch.parcel != null && objectSearch.seedDate != null) {

      if (!objectSearch.hasOwnProperty(propertyHarvestDate) || (objectSearch.hasOwnProperty(propertyHarvestDate) && objectSearch.harvestDate == null)) {
        jsonSearch = "\{\"parcel\": \"" + objectSearch.parcel.name + "\"" + ", " + "\"seedDate\": \"" + objectSearch.seedDate + "\"}";
      }

    }

    /*
    Si en el objeto de busqueda estan presentes unicamente las propiedades
    parcel y harvestDate significa que se desea filtrar los registros de
    plantacion unicamente por la parcela y la fecha de cosecha. Por lo
    tanto, para realizar la filtracion se crea una cadena JSON con las
    propiedades parcel y harvestDate junto con sus respectivos valores.
    */
    if (objectSearch.hasOwnProperty(propertyParcel) && objectSearch.hasOwnProperty(propertyHarvestDate) && objectSearch.parcel != null && objectSearch.harvestDate != null) {

      if (!objectSearch.hasOwnProperty(propertySeedDate) || (objectSearch.hasOwnProperty(propertySeedDate) && objectSearch.seedDate == null)) {
        jsonSearch = "\{\"parcel\": \"" + objectSearch.parcel.name + "\"" + ", " + "\"harvestDate\": \"" + objectSearch.harvestDate + "\"}";
      }

    }

    /*
    Si en el objeto de busqueda estan presentes unicamente las propiedades
    seedDate y harvestDate significa que se desea filtrar los registros de
    plantacion unicamente por la fecha de siembra y la fecha de cosecha. Por
    lo tanto, para realizar la filtracion se crea una cadena JSON con las
    propiedades seedDate y harvestDate junto con sus respectivos valores.
    */
    if (objectSearch.hasOwnProperty(propertySeedDate) && objectSearch.hasOwnProperty(propertyHarvestDate) && objectSearch.seedDate != null && objectSearch.harvestDate != null) {

      if (!objectSearch.hasOwnProperty(propertyParcel) || (objectSearch.hasOwnProperty(propertyParcel) && objectSearch.parcel == null)) {
        jsonSearch = "\{\"seedDate\": \"" + objectSearch.seedDate + "\"" + ", " + "\"harvestDate\": \"" + objectSearch.harvestDate + "\"}";
      }

    }

    /*
    Si en el objeto de busqueda estan presentes las propiedades parcel,
    seedDate y harvestDate significa que se desea filtrar los registros de
    plantacion por la parcela, la fecha de siembra y la fecha de cosecha.
    Por lo tanto, para realizar la filtracion se crea una cadena JSON con
    las propiedades parcel, seedDate y harvestDate junto con sus respectivos
    valores.
    */
    if (objectSearch.hasOwnProperty(propertyParcel) && objectSearch.hasOwnProperty(propertySeedDate) && objectSearch.hasOwnProperty(propertyHarvestDate)
      && objectSearch.parcel != null && objectSearch.seedDate != null && objectSearch.harvestDate != null) {
      jsonSearch = "\{\"parcel\": \"" + objectSearch.parcel.name + "\"" + ", " + "\"seedDate\": \"" + objectSearch.seedDate + "\"" + ", " + "\"harvestDate\": \"" + objectSearch.harvestDate + "\"}";
    }

    $http.get('rest/plantingRecords/findAllPagination?page=' + page + '&cant=' + cant + "&search=" + jsonSearch)
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
