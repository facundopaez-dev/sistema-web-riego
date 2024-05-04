app.service(
    "SoilWaterBalanceSrv",
    ["$http",
        function ($http) {

            const propertyParcel = 'parcel';
            const propertyCrop = 'crop';
            const propertyDate = 'date';

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

                    if ((!objectSearch.hasOwnProperty(propertyCrop) || (objectSearch.hasOwnProperty(propertyCrop) && objectSearch.crop == null))
                        && (!objectSearch.hasOwnProperty(propertyDate) || (objectSearch.hasOwnProperty(propertyDate) && objectSearch.date == null))) {
                        jsonSearch = "\{\"parcel\": \"" + objectSearch.parcel.name + "\"}";
                    }

                }

                /*
                Si en el objeto de busqueda esta presenta unicamente la propiedad
                crop significa que se desea filtrar unicamente por el nombre del
                cultivo. Por lo tanto, para realizar la filtracion se crea una
                cadena JSON con la propiedad cropName y su valor.
                */
                if (objectSearch.hasOwnProperty(propertyCrop) && objectSearch.crop != null) {

                    if ((!objectSearch.hasOwnProperty(propertyParcel) || (objectSearch.hasOwnProperty(propertyParcel) && objectSearch.parcel == null))
                        && (!objectSearch.hasOwnProperty(propertyDate) || (objectSearch.hasOwnProperty(propertyDate) && objectSearch.date == null))) {
                        jsonSearch = "\{\"cropName\": \"" + objectSearch.crop.name + "\"}";
                    }

                }

                /*
                Si en el objeto de busqueda esta presenta unicamente la propiedad
                date significa que se desea filtrar unicamente por la fecha. Por lo
                tanto, para realizar la filtracion se crea una cadena JSON con la
                propiedad date y su valor.
                */
                if (objectSearch.hasOwnProperty(propertyDate) && objectSearch.date != null) {

                    if ((!objectSearch.hasOwnProperty(propertyParcel) || (objectSearch.hasOwnProperty(propertyParcel) && objectSearch.parcel == null))
                        && (!objectSearch.hasOwnProperty(propertyCrop) || (objectSearch.hasOwnProperty(propertyCrop) && objectSearch.crop == null))) {
                        jsonSearch = "\{\"date\": \"" + objectSearch.date + "\"}";
                    }

                }

                /*
                Si en el objeto de busqueda estan presentes unicamente las propiedades
                parcel y crop significa que se desea filtrar unicamente por el nombre
                de la parcela y el nombre del cultivo. Por lo tanto, para realizar la
                filtracion se crea una cadena JSON con las propiedades parcel y cropName
                junto con sus respectivos valores.
                */
                if (objectSearch.hasOwnProperty(propertyParcel) && objectSearch.hasOwnProperty(propertyCrop) && objectSearch.parcel != null && objectSearch.crop != null) {

                    if (!objectSearch.hasOwnProperty(propertyDate) || (objectSearch.hasOwnProperty(propertyDate) && objectSearch.date == null)) {
                        jsonSearch = "\{\"parcel\": \"" + objectSearch.parcel.name + "\"" + ", " + "\"cropName\": \"" + objectSearch.crop.name + "\"}";
                    }

                }

                /*
                Si en el objeto de busqueda estan presentes unicamente las propiedades
                parcel y date significa que se desea filtrar unicamente por el nombre
                de la parcela y la fecha. Por lo tanto, para realizar la filtracion se
                crea una cadena JSON con las propiedades parcel y date junto con sus
                respectivos valores.
                */
                if (objectSearch.hasOwnProperty(propertyParcel) && objectSearch.hasOwnProperty(propertyDate) && objectSearch.parcel != null && objectSearch.date != null) {

                    if (!objectSearch.hasOwnProperty(propertyCrop) || (objectSearch.hasOwnProperty(propertyCrop) && objectSearch.crop == null)) {
                        jsonSearch = "\{\"parcel\": \"" + objectSearch.parcel.name + "\"" + ", " + "\"date\": \"" + objectSearch.date + "\"}";
                    }

                }

                /*
                Si en el objeto de busqueda estan presentes unicamente las propiedades
                crop y date significa que se desea filtrar unicamente por el nombre
                del cultivo y la fecha. Por lo tanto, para realizar la filtracion se
                crea una cadena JSON con las propiedades cropName y date junto con
                sus respectivos valores.
                */
                if (objectSearch.hasOwnProperty(propertyCrop) && objectSearch.hasOwnProperty(propertyDate) && objectSearch.crop != null && objectSearch.date != null) {

                    if (!objectSearch.hasOwnProperty(propertyParcel) || (objectSearch.hasOwnProperty(propertyParcel) && objectSearch.parcel == null)) {
                        jsonSearch = "\{\"cropName\": \"" + objectSearch.crop.name + "\"" + ", " + "\"date\": \"" + objectSearch.date + "\"}";
                    }

                }

                /*
                Si en el objeto de busqueda estan presentes las propiedades parcel,
                crop y date significa que se desea filtrar por el nombre de la parcela,
                el nombre del cultivo y la fecha. Por lo tanto, para realizar la filtracion
                se crea una cadena JSON con las propiedades parcel, cropName y date junto
                con sus respectivos valores.
                */
                if (objectSearch.hasOwnProperty(propertyParcel) && objectSearch.hasOwnProperty(propertyCrop) && objectSearch.hasOwnProperty(propertyDate)
                    && objectSearch.parcel != null && objectSearch.crop != null && objectSearch.date != null) {
                    jsonSearch = "\{\"parcel\": \"" + objectSearch.parcel.name + "\"" + ", " + "\"cropName\": \"" + objectSearch.crop.name + "\"" + ", " + "\"date\": \"" + objectSearch.date + "\"}";
                }

                $http.get('rest/soilWaterBalances/findAllPagination?page=' + page + '&cant=' + cant + "&search=" + jsonSearch)
                    .then(function (res) {
                        return callback(false, res.data)
                    }, function (err) {
                        return callback(err.data)
                    })
            }

            this.filter = function (dateFrom, dateUntil, parcelName, cropName, callback) {
                $http.get("rest/soilWaterBalances/filter?dateFrom=" + dateFrom + "&dateUntil=" + dateUntil + "&parcelName=" + parcelName + "&cropName=" + cropName).then(
                    function (result) {
                        callback(false, result.data);
                    },
                    function (error) {
                        callback(error);
                    });
            };

        }
    ]);
