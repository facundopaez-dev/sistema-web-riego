app.service(
	"UserSrv",
	["$http",
		function ($http) {

			this.searchByPage = function (search, page, cant, callback) {
				$http.get('rest/users/findAllUsersExceptOwnUser?page=' + page + '&cant=' + cant + "&search=" + JSON.stringify(search))
					.then(function (res) {
						return callback(false, res.data)
					}, function (err) {
						return callback(err.data)
					})
			}

			this.find = function (id, callback) {
				$http.get("rest/users/" + id).then(
					function (result) {
						callback(false, result.data);
					},
					function (error) {
						callback(error);
					});
			}

			/*
			Esta funcion es para que el usuario pueda ver los
			datos de su cuenta en la lista de la pagina de
			inicio del usuario (home)
			*/
			this.findMyAccount = function (callback) {
				$http.get("rest/users/myAccount").then(
					function (result) {
						callback(false, result.data);
					},
					function (error) {
						callback(error);
					});
			}

			/*
			Esta funcion es para que el usuario pueda modificar
			los datos su cuenta al presionar el boton que tiene
			el icono de un lapiz en la pagina web de inicio
			*/
			this.findMyAccountDetails = function (callback) {
				$http.get("rest/users/myAccountDetails").then(
					function (result) {
						callback(false, result.data);
					},
					function (error) {
						callback(error);
					});
			}

			this.create = function (data, callback) {
				$http.post("rest/users", data)
					.then(
						function (result) {
							callback(false, result.data);
						},
						function (error) {
							callback(error);
						});
			}

            this.delete = function (id, callback) {
                $http.delete("rest/users/deleteUser/" + id)
                    .then(
                        function (result) {
                            callback(false, result.data);
                        },
                        function (error) {
                            callback(error);
                        });
            }

			this.modify = function (data, callback) {
				$http.put("rest/users/modify", data)
					.then(
						function (result) {
							callback(false, result.data);
						},
						function (error) {
							callback(error);
						});
			};

			this.modifySuperuserPermission = function (id, data, callback) {
				$http.put("rest/users/modifySuperuserPermission/" + id, data)
					.then(
						function (result) {
							callback(false, result.data);
						},
						function (error) {
							callback(error);
						});
			};

			this.modifyPassword = function (data, callback) {
				$http.put("rest/users/modifyPassword", data)
					.then(
						function (result) {
							callback(false, result.data);
						},
						function (error) {
							callback(error);
						});
			};

			this.sendEmailPasswordRecovery = function (data, callback) {
				$http.put("rest/users/passwordResetEmail", data)
					.then(
						function (result) {
							callback(false);
						},
						function (error) {
							callback(error);
						});
			};

			this.resetPassword = function (jwtResetPassword, data, callback) {
				$http.put("rest/users/resetPassword/" + jwtResetPassword, data)
					.then(
						function (result) {
							callback(false);
						},
						function (error) {
							callback(error);
						});
			};

			this.checkPasswordResetLink = function (jwtResetPassword, callback) {
				$http.get("rest/users/checkPasswordResetLink/" + jwtResetPassword)
					.then(
						function (result) {
							callback(false);
						},
						function (error) {
							callback(error);
						});
			};

		}
	]);
