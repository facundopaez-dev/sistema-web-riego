app.service(
    "ClimateRecordSrv",
    ["$http",
        function ($http) {

            const propertyParcel = 'parcel';
            const propertyDate = 'date';

            this.findAll = function (callback) {
                $http.get("rest/climateRecords").then(
                    function (result) {
                        callback(false, result.data);
                    },
                    function (error) {
                        callback(error);
                    });
            }

            this.searchByPage = function (search, page, cant, callback) {
                var jsonSearch = JSON.stringify(search);
                const objectSearch = JSON.parse(jsonSearch);

                /*
                Si en el objeto de busqueda esta presenta unicamente la propiedad
                parcel significa que se desea filtrar unicamente por el nombre de
                la parcela. Por lo tanto, para realizar la filtracion se crea una
                cadena JSON con la propiedad parcel y su valor.
                */
                if (objectSearch.hasOwnProperty(propertyParcel) && objectSearch.parcel != null) {

                    if ((!objectSearch.hasOwnProperty(propertyDate) || (objectSearch.hasOwnProperty(propertyDate) && objectSearch.date == null))) {
                        jsonSearch = "\{\"parcel\": \"" + objectSearch.parcel.name + "\"}";
                    }

                }

                /*
                Si en el objeto de busqueda esta presenta unicamente la propiedad
                date significa que se desea filtrar unicamente por la fecha. Por lo
                tanto, para realizar la filtracion se crea una cadena JSON con la
                propiedad date y su valor.
                */
                if (objectSearch.hasOwnProperty(propertyDate) && objectSearch.date != null) {

                    if ((!objectSearch.hasOwnProperty(propertyParcel) || (objectSearch.hasOwnProperty(propertyParcel) && objectSearch.parcel == null))) {
                        jsonSearch = "\{\"date\": \"" + objectSearch.date + "\"}";
                    }

                }

                /*
                Si en el objeto de busqueda estan presentes las propiedades parcel y
                date significa que se desea filtrar por la parcela y la fecha. Por lo
                tanto, para realizar la filtracion se crea una cadena JSON con las
                propiedades parcel y date junto con sus respectivos valores.
                */
                if (objectSearch.hasOwnProperty(propertyParcel) && objectSearch.hasOwnProperty(propertyDate) && objectSearch.parcel != null && objectSearch.date != null) {
                    jsonSearch = "\{\"parcel\": \"" + objectSearch.parcel.name + "\"" + ", " + "\"date\": \"" + objectSearch.date + "\"}";
                }

                $http.get('rest/climateRecords/findAllPagination?page=' + page + '&cant=' + cant + "&search=" + jsonSearch)
                    .then(function (res) {
                        return callback(false, res.data)
                    }, function (err) {
                        return callback(err.data)
                    })
            }

            this.find = function (id, callback) {
                $http.get("rest/climateRecords/" + id).then(
                    function (result) {
                        callback(false, result.data);
                    },
                    function (error) {
                        callback(error);
                    });
            }

            this.create = function (data, callback) {
                $http.post("rest/climateRecords", data)
                    .then(
                        function (result) {
                            callback(false, result.data);
                        },
                        function (error) {
                            callback(error);
                        });
            }

            this.modify = function (data, callback) {
                $http.put("rest/climateRecords/" + data.id, data)
                    .then(
                        function (result) {
                            callback(false, result.data);
                        },
                        function (error) {
                            callback(error);
                        });
            }

            this.delete = function (id, callback) {
                $http.delete("rest/climateRecords/" + id)
                    .then(
                        function (result) {
                            callback(false, result.data);
                        },
                        function (error) {
                            callback(error);
                        });
            }

            this.filter = function (parcelName, date, callback) {
                $http.get("rest/climateRecords/filter?parcelName=" + parcelName + "&date=" + date).then(
                    function (result) {
                        callback(false, result.data);
                    },
                    function (error) {
                        callback(error);
                    });
            };

        }
    ]);
