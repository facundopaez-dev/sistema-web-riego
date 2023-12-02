app.service(
	"CropSrv",
	["$http",
		function ($http) {

			this.findAll = function (callback) {
				$http.get("rest/crops").then(
					function (result) {
						callback(false, result.data);
					},
					function (error) {
						callback(error);
					});
			}

			this.find = function (id, callback) {
				$http.get("rest/crops/" + id).then(
					function (result) {
						callback(false, result.data);
					},
					function (error) {
						callback(error);
					});
			}

			this.create = function (data, callback) {
				$http.post("rest/crops", data)
					.then(
						function (result) {
							callback(false, result.data);
						},
						function (error) {
							callback(error);
						});
			}

			this.delete = function (id, callback) {
				$http.delete("rest/crops/" + id)
					.then(
						function (result) {
							callback(false, result.data);
						},
						function (error) {
							callback(error);
						});
			}

			this.modify = function (data, callback) {
				$http.put("rest/crops/" + data.id, data)
					.then(
						function (result) {
							callback(false, result.data);
						},
						function (error) {
							callback(error);
						});
			};

			this.search = function (name, callback) {
				$http.get("rest/crops/search/?cropName=" + name).then(
					function (result) {
						callback(false, result.data);
					},
					function (error) {
						callback(error);
					});
			}

			// Esto es necesario para la busqueda que se hace cuando se ingresan caracteres
			this.findByName = function (name) {
				return $http.get("rest/crops/findByName/?cropName=" + name);
			}

			// Esto es necesario para la busqueda que se hace cuando se ingresan caracteres
			this.findActiveCropByName = function (name) {
				return $http.get("rest/crops/findActiveCropByName/?cropName=" + name);
			}

		}
	]);
