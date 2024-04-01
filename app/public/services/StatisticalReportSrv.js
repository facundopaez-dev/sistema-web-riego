app.service(
    "StatisticalReportSrv",
    ["$http",
        function ($http) {

            const propertyParcel = 'parcel';
            const propertyDateFrom = 'dateFrom';
            const propertyDateUntil = 'dateUntil';

            this.findAll = function (callback) {
                $http.get("rest/statisticalReports").then(
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
                parcel significa que se desea filtrar los registros de plantacion
                unicamente por el nombre de la parcela. Por lo tanto, para realizar
                la filtracion se crea una cadena JSON con la propiedad parcel y su
                valor.
                */
                if (objectSearch.hasOwnProperty(propertyParcel) && objectSearch.parcel != null) {

                    if ((!objectSearch.hasOwnProperty(propertyDateFrom) || (objectSearch.hasOwnProperty(propertyDateFrom) && objectSearch.dateFrom == null))
                        && (!objectSearch.hasOwnProperty(propertyDateUntil) || (objectSearch.hasOwnProperty(propertyDateUntil) && objectSearch.dateUntil == null))) {
                        jsonSearch = "\{\"parcel\": \"" + objectSearch.parcel.name + "\"}";
                    }

                }

                /*
                Si en el objeto de busqueda esta presenta unicamente la propiedad
                dateFrom significa que se desea filtrar los registros de plantacion
                unicamente por la fecha desde. Por lo tanto, para realizar la
                filtracion se crea una cadena JSON con la propiedad dateFrom y su
                valor.
                */
                if (objectSearch.hasOwnProperty(propertyDateFrom) && objectSearch.dateFrom != null) {

                    if ((!objectSearch.hasOwnProperty(propertyParcel) || (objectSearch.hasOwnProperty(propertyParcel) && objectSearch.parcel == null))
                        && (!objectSearch.hasOwnProperty(propertyDateUntil) || (objectSearch.hasOwnProperty(propertyDateUntil) && objectSearch.dateUntil == null))) {
                        jsonSearch = "\{\"dateFrom\": \"" + objectSearch.dateFrom + "\"}";
                    }

                }

                /*
                Si en el objeto de busqueda esta presenta unicamente la propiedad
                dateUntil significa que se desea filtrar los registros de plantacion
                unicamente por la fecha hasta. Por lo tanto, para realizar la
                filtracion se crea una cadena JSON con la propiedad dateUntil y su
                valor.
                */
                if (objectSearch.hasOwnProperty(propertyDateUntil) && objectSearch.dateUntil != null) {

                    if ((!objectSearch.hasOwnProperty(propertyParcel) || (objectSearch.hasOwnProperty(propertyParcel) && objectSearch.parcel == null))
                        && (!objectSearch.hasOwnProperty(propertyDateFrom) || (objectSearch.hasOwnProperty(propertyDateFrom) && objectSearch.dateFrom == null))) {
                        jsonSearch = "\{\"dateUntil\": \"" + objectSearch.dateUntil + "\"}";
                    }

                }

                /*
                Si en el objeto de busqueda estan presentes unicamente las propiedades
                parcel y dateFrom significa que se desea filtrar los registros de
                plantacion unicamente por la parcela y la fecha desde. Por lo
                tanto, para realizar la filtracion se crea una cadena JSON con las
                propiedades parcel y dateFrom junto con sus respectivos valores.
                */
                if (objectSearch.hasOwnProperty(propertyParcel) && objectSearch.hasOwnProperty(propertyDateFrom) && objectSearch.parcel != null && objectSearch.dateFrom != null) {

                    if (!objectSearch.hasOwnProperty(propertyDateUntil) || (objectSearch.hasOwnProperty(propertyDateUntil) && objectSearch.dateUntil == null)) {
                        jsonSearch = "\{\"parcel\": \"" + objectSearch.parcel.name + "\"" + ", " + "\"dateFrom\": \"" + objectSearch.dateFrom + "\"}";
                    }

                }

                /*
                Si en el objeto de busqueda estan presentes unicamente las propiedades
                parcel y dateUntil significa que se desea filtrar los registros de
                plantacion unicamente por la parcela y la fecha hasta. Por lo
                tanto, para realizar la filtracion se crea una cadena JSON con las
                propiedades parcel y dateUntil junto con sus respectivos valores.
                */
                if (objectSearch.hasOwnProperty(propertyParcel) && objectSearch.hasOwnProperty(propertyDateUntil) && objectSearch.parcel != null && objectSearch.dateUntil != null) {

                    if (!objectSearch.hasOwnProperty(propertyDateFrom) || (objectSearch.hasOwnProperty(propertyDateFrom) && objectSearch.dateFrom == null)) {
                        jsonSearch = "\{\"parcel\": \"" + objectSearch.parcel.name + "\"" + ", " + "\"dateUntil\": \"" + objectSearch.dateUntil + "\"}";
                    }

                }

                /*
                Si en el objeto de busqueda estan presentes unicamente las propiedades
                dateFrom y dateUntil significa que se desea filtrar los registros de
                plantacion unicamente por la fecha desde y la fecha hasta. Por
                lo tanto, para realizar la filtracion se crea una cadena JSON con las
                propiedades dateFrom y dateUntil junto con sus respectivos valores.
                */
                if (objectSearch.hasOwnProperty(propertyDateFrom) && objectSearch.hasOwnProperty(propertyDateUntil) && objectSearch.dateFrom != null && objectSearch.dateUntil != null) {

                    if (!objectSearch.hasOwnProperty(propertyParcel) || (objectSearch.hasOwnProperty(propertyParcel) && objectSearch.parcel == null)) {
                        jsonSearch = "\{\"dateFrom\": \"" + objectSearch.dateFrom + "\"" + ", " + "\"dateUntil\": \"" + objectSearch.dateUntil + "\"}";
                    }

                }

                /*
                Si en el objeto de busqueda estan presentes las propiedades parcel,
                dateFrom y dateUntil significa que se desea filtrar los registros de
                plantacion por la parcela, la fecha desde y la fecha hasta.
                Por lo tanto, para realizar la filtracion se crea una cadena JSON con
                las propiedades parcel, dateFrom y dateUntil junto con sus respectivos
                valores.
                */
                if (objectSearch.hasOwnProperty(propertyParcel) && objectSearch.hasOwnProperty(propertyDateFrom) && objectSearch.hasOwnProperty(propertyDateUntil)
                    && objectSearch.parcel != null && objectSearch.dateFrom != null && objectSearch.dateUntil != null) {
                    jsonSearch = "\{\"parcel\": \"" + objectSearch.parcel.name + "\"" + ", " + "\"dateFrom\": \"" + objectSearch.dateFrom + "\"" + ", " + "\"dateUntil\": \"" + objectSearch.dateUntil + "\"}";
                }

                $http.get('rest/statisticalReports/findAllPagination?page=' + page + '&cant=' + cant + "&search=" + jsonSearch)
                    .then(function (res) {
                        return callback(false, res.data)
                    }, function (err) {
                        return callback(err.data)
                    })
            }

            this.find = function (id, callback) {
                $http.get("rest/statisticalReports/" + id).then(
                    function (result) {
                        callback(false, result.data);
                    },
                    function (error) {
                        callback(error);
                    });
            }

            this.create = function (data, callback) {
                $http.post("rest/statisticalReports", data)
                    .then(
                        function (result) {
                            callback(false, result.data);
                        },
                        function (error) {
                            callback(error);
                        });
            }

            this.regenerate = function (id, callback) {
                $http.put("rest/statisticalReports/regenerate/" + id)
                    .then(
                        function (result) {
                            callback(false, result.data);
                        },
                        function (error) {
                            callback(error);
                        });
            }

            this.delete = function (id, callback) {
                $http.delete("rest/statisticalReports/" + id)
                    .then(
                        function (result) {
                            callback(false, result.data);
                        },
                        function (error) {
                            callback(error);
                        });
            }

            this.filter = function (parcelName, dateFrom, dateUntil, callback) {
                $http.get("rest/statisticalReports/filter?parcelName=" + parcelName + "&dateFrom=" + dateFrom + "&dateUntil=" + dateUntil).then(
                    function (result) {
                        callback(false, result.data);
                    },
                    function (error) {
                        callback(error);
                    });
            };

        }
    ]);
