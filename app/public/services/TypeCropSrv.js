app.service(
	"TypeCropSrv",
	["$http",
		function ($http) {

			this.findAll = function (callback) {
				$http.get("rest/typesCrop").then(
					function (result) {
						callback(false, result.data);
					},
					function (error) {
						callback(error);
					});
			}

			this.find = function (id, callback) {
				$http.get("rest/typesCrop/" + id).then(
					function (result) {
						callback(false, result.data);
					},
					function (error) {
						callback(error);
					});
			}

			this.create = function (data, callback) {
				$http.post("rest/typesCrop", data)
					.then(
						function (result) {
							callback(false, result.data);
						},
						function (error) {
							callback(error);
						});
			}

			this.delete = function (id, callback) {
				$http.delete("rest/typesCrop/" + id)
					.then(
						function (result) {
							callback(false, result.data);
						},
						function (error) {
							callback(error);
						});
			}

			this.modify = function (data, callback) {
				$http.put("rest/typesCrop/" + data.id, data)
					.then(
						function (result) {
							callback(false, result.data);
						},
						function (error) {
							callback(error);
						});
			};

			this.search = function (name, callback) {
				$http.get("rest/typesCrop/search/?typeCropName=" + name).then(
					function (result) {
						callback(false, result.data);
					},
					function (error) {
						callback(error);
					});
			}

			// Esto es necesario para la busqueda que se hace cuando se ingresan caracteres
			this.findActiveTypeCropByName = function (name) {
				return $http.get("rest/typesCrop/findActiveTypeCropByName/?typeCropName=" + name);
			}

		}
	]);
