app.service(
	"TypeCropSrv",
	["$http",
		function ($http) {

			this.findAll = function (callback) {
				$http.get("rest/typeCrops").then(
					function (result) {
						callback(false, result.data);
					},
					function (error) {
						callback(error);
					});
			}

			this.findAllActive = function (callback) {
				$http.get("rest/typeCrops/actives").then(
					function (result) {
						callback(false, result.data);
					},
					function (error) {
						callback(error);
					});
			}

			this.find = function (id, callback) {
				$http.get("rest/typeCrops/" + id).then(
					function (result) {
						callback(false, result.data);
					},
					function (error) {
						callback(error);
					});
			}

			this.create = function (data, callback) {
				$http.post("rest/typeCrops", data)
					.then(
						function (result) {
							callback(false, result.data);
						},
						function (error) {
							callback(error);
						});
			}

			this.delete = function (id, callback) {
				$http.delete("rest/typeCrops/" + id)
					.then(
						function (result) {
							callback(false, result.data);
						},
						function (error) {
							callback(error);
						});
			}

			this.modify = function (data, callback) {
				$http.put("rest/typeCrops/" + data.id, data)
					.then(
						function (result) {
							callback(false, result.data);
						},
						function (error) {
							callback(error);
						});
			};

		}
	]);
