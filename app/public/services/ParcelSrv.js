app.service(
	"ParcelSrv",
	["$http",
		function ($http) {

			this.findAll = function (callback) {
				$http.get("rest/parcels/findAllParcels").then(
					function (result) {
						callback(false, result.data);
					},
					function (error) {
						callback(error);
					});
			}

			this.findAllActive = function (callback) {
				$http.get("rest/parcels/findAllActive").then(
					function (result) {
						callback(false, result.data);
					},
					function (error) {
						callback(error);
					});
			}

			this.searchByPage = function (search, page, cant, callback) {
				$http.get('rest/parcels?page=' + page + '&cant=' + cant + "&search=" + JSON.stringify(search))
					.then(function (res) {
						return callback(false, res.data)
					}, function (err) {
						return callback(err.data)
					})
			}

			this.find = function (id, callback) {
				$http.get("rest/parcels/" + id).then(
					function (result) {
						callback(false, result.data);
					},
					function (error) {
						callback(error);
					});
			}

			this.create = function (data, callback) {
				$http.post("rest/parcels", data)
					.then(
						function (result) {
							callback(false, result.data);
						},
						function (error) {
							callback(error);
						});
			}

			this.modify = function (data, callback) {
				$http.put("rest/parcels/" + data.id, data)
					.then(
						function (result) {
							callback(false, result.data);
						},
						function (error) {
							callback(error);
						});
			};

			this.delete = function (id, callback) {
				$http.delete("rest/parcels/" + id)
					.then(
						function (result) {
							callback(false, result.data);
						},
						function (error) {
							callback(error);
						});
			}

		}
	]);
