var app = angular.module('app', ['ngRoute', 'Pagination', 'ui.bootstrap', 'leaflet-directive']);

// ui.bootstrap es necesario para la busqueda que se hace cuando se ingresan caracteres
// Pagination es necesario para la paginacion

app.config(['$routeProvider', function (routeprovider) {
	routeprovider

		/* Ruta para el registro de usuario */
		.when('/signup', {
			templateUrl: 'partials/user/sign-up-form.html',
			controller: 'SignupCtrl'
		})

		/* Ruta para la activacion de la cuenta del usuario */
		.when('/activateAccount/:email', {
			templateUrl: 'partials/user/user-account-activation.html',
			controller: 'UserAccountActivationCtrl'
		})

		/* Ruta para la pagina web en la que se ingresa el correo electronico para restablecer la contraseña */
		.when('/passwordResetEmail', {
			templateUrl: 'partials/email-password-recovery-form.html',
			controller: 'EmailPasswordRecoveryCtrl'
		})

		/* Ruta para el formulario de restablecimiento de la contraseña */
		.when('/resetPassword/:jwtResetPassword', {
			templateUrl: 'partials/reset-password-form.html',
			controller: 'ResetPasswordCtrl'
		})

		/* Rutas del usuario */
		.when('/', {
			templateUrl: 'partials/user/user-login.html',
			controller: 'UserLoginCtrl'
		})

		.when('/home', {
			templateUrl: 'partials/user/home.html',
			controller: 'HomeCtrl'
		})
		.when('/home/account/modifyAccountData', {
			templateUrl: 'partials/user/user-account-data-modification-form.html',
			controller: 'ModificationUserAccountDataCtrl'
		})
		.when('/home/account/modifyPassword', {
			templateUrl: 'partials/user/user-password-change-form.html',
			controller: 'UserPasswordChangeCtrl'
		})
		.when('/home/account/options', {
			templateUrl: 'partials/user/user-option-form.html',
			controller: 'OptionCtrl'
		})

		.when('/home/crops', {
			templateUrl: 'partials/user/user-crop-list.html',
			controller: 'UserCropsCtrl'
		})
		.when('/home/crops/:action', {
			templateUrl: 'partials/user/user-crop-form.html',
			controller: 'UserCropCtrl'
		})
		.when('/home/crops/:action/:id', {
			templateUrl: 'partials/user/user-crop-form.html',
			controller: 'UserCropCtrl'
		})

		.when('/home/parcels', {
			templateUrl: 'partials/user/parcel-list.html',
			controller: 'ParcelsCtrl'
		})

		.when('/home/parcels/:action', {
			templateUrl: 'partials/user/parcel-form.html',
			controller: 'ParcelCtrl'
		})

		.when('/home/parcels/:action/:id', {
			templateUrl: 'partials/user/parcel-form.html',
			controller: 'ParcelCtrl'
		})

		.when('/home/irrigationRecords', {
			templateUrl: 'partials/user/irrigation-record-list.html',
			controller: 'IrrigationRecordsCtrl'
		})
		.when('/home/irrigationRecords/:action', {
			templateUrl: 'partials/user/irrigation-record-form.html',
			controller: 'IrrigationRecordCtrl'
		})
		.when('/home/irrigationRecords/:action/:id', {
			templateUrl: 'partials/user/irrigation-record-form.html',
			controller: 'IrrigationRecordCtrl'
		})

		.when('/home/climateRecords', {
			templateUrl: 'partials/user/climate-record-list.html',
			controller: 'ClimateRecordsCtrl'
		})
		.when('/home/climateRecords/:action', {
			templateUrl: 'partials/user/climate-record-form.html',
			controller: 'ClimateRecordCtrl'
		})
		.when('/home/climateRecords/:action/:id', {
			templateUrl: 'partials/user/climate-record-form.html',
			controller: 'ClimateRecordCtrl'
		})

		.when('/home/plantingRecords', {
			templateUrl: 'partials/user/planting-record-list.html',
			controller: 'PlantingRecordsCtrl'
		})
		.when('/home/plantingRecords/:action', {
			templateUrl: 'partials/user/planting-record-form.html',
			controller: 'PlantingRecordCtrl'
		})
		.when('/home/plantingRecords/:action/:id', {
			templateUrl: 'partials/user/planting-record-form.html',
			controller: 'PlantingRecordCtrl'
		})

		.when('/home/statisticalReports', {
			templateUrl: 'partials/user/statistical-report-list.html',
			controller: 'StatisticalReportsCtrl'
		})
		.when('/home/statisticalReports/:action', {
			templateUrl: 'partials/user/statistical-report-form.html',
			controller: 'StatisticalReportCtrl'
		})
		.when('/home/statisticalReports/:action/:id', {
			templateUrl: 'partials/user/statistical-report-form.html',
			controller: 'StatisticalReportCtrl'
		})

		.when('/home/cropWaterActivityLogs', {
			templateUrl: 'partials/user/crop-water-activity-log-list.html',
			controller: 'CropWaterActivityLogsCtrl'
		})

		/* Rutas del administrador */

		/*
		Se utiliza la pagina web de inicio de sesion del usuario como
		pagina web de inicio de sesion del administrador porque NO es
		util implementar dos paginas web iguales en contenido, pero
		con nombres diferentes, para el mismo proposito
		*/
		.when('/admin', {
			templateUrl: 'partials/user/user-login.html',
			controller: 'AdminLoginCtrl'
		})

		.when('/adminHome', {
			templateUrl: 'partials/admin/admin-home.html',
			controller: 'AdminHomeCtrl'
		})
		.when('/adminHome/account/modifyAccountData', {
			templateUrl: 'partials/admin/admin-account-data-modification-form.html',
			controller: 'AdminAccountDataModificationCtrl'
		})
		.when('/adminHome/account/modifyPassword', {
			templateUrl: 'partials/admin/admin-password-change-form.html',
			controller: 'AdminPasswordChangeCtrl'
		})

		.when('/adminHome/users', {
			templateUrl: 'partials/admin/user-list.html',
			controller: 'UsersCtrl'
		})

		.when('/adminHome/typeCrops', {
			templateUrl: 'partials/admin/type-crop-list.html',
			controller: 'TypeCropsCtrl'
		})
		.when('/adminHome/typeCrops/:action', {
			templateUrl: 'partials/admin/type-crop-form.html',
			controller: 'TypeCropCtrl'
		})
		.when('/adminHome/typeCrops/:action/:id', {
			templateUrl: 'partials/admin/type-crop-form.html',
			controller: 'TypeCropCtrl'
		})

		.when('/adminHome/crops', {
			templateUrl: 'partials/admin/admin-crop-list.html',
			controller: 'AdminCropsCtrl'
		})
		.when('/adminHome/crops/:action', {
			templateUrl: 'partials/admin/admin-crop-form.html',
			controller: 'AdminCropCtrl'
		})
		.when('/adminHome/crops/:action/:id', {
			templateUrl: 'partials/admin/admin-crop-form.html',
			controller: 'AdminCropCtrl'
		})

		.when('/adminHome/regions', {
			templateUrl: 'partials/admin/region-list.html',
			controller: 'RegionsCtrl'
		})
		.when('/adminHome/regions/:action', {
			templateUrl: 'partials/admin/region-form.html',
			controller: 'RegionCtrl'
		})
		.when('/adminHome/regions/:action/:id', {
			templateUrl: 'partials/admin/region-form.html',
			controller: 'RegionCtrl'
		})

		.otherwise({
			templateUrl: 'partials/404.html'
		})
}])

/*
AccessManager es la factory que se utiliza para controlar el acceso a
las paginas web dependiendo si el usuario tiene una sesion abierta o no,
y si tiene permiso de administrador o no
*/
app.factory('AccessManager', ['JwtManager', '$window', function (jwtManager, $window) {
	/*
	El valor booleano establecido y accedido en el almacenamiento local del
	navegador web a traves de esta constante, se utiliza para evitar que un
	administrador con una sesion abierta como administrador, acceda a las
	paginas web a las que accede un usuario. De esta manera, un administrador
	debe cerrar la sesion que abrio a traves de la pagina web de inicio de
	sesion de administrador, y luego abrir una sesion a traves de la pagina
	web de inicio de sesion de usuario, para acceder a las paginas web a las
	que accede un usuario.
	*/
	const KEY = "superuser";

	return {
		/**
		 * Cuando el usuario inicia sesion satisfactoriamente, la aplicacion
		 * del lado servidor devuelve un JWT, el cual, es almacenado en el
		 * almacenamiento local del navegador web por la funcion setJwt
		 * de la factory JwtManager
		 * 
		 * @returns true si el usuario tiene una sesion abierta, false
		 * en caso contrario
		 */
		isUserLoggedIn: function () {
			/*
			Si el valor devuelto por getJwt NO es null, se retorna
			el valor booleano true
			*/
			if (jwtManager.getJwt()) {
				return true;
			}

			return false;
		},

		/**
		 * Cuando el usuario que inicia sesion tiene permiso de administrador,
		 * la variable booleana loggedAsSuperuser se establece en true.
		 * 
		 * Cuando el usuario que cierra su sesion tiene permiso de administrador,
		 * la variable booleana loggedAsSuperuser se establece en false.
		 * 
		 * @returns true si el usuario que tiene una sesion abierta tiene
		 * permiso de administrador, false en caso contrario
		 */
		loggedAsAdmin: function () {
			return JSON.parse($window.localStorage.getItem(KEY));
		},

		/**
		 * Esta funcion debe ser invocada cuando el usuario que inicia sesion,
		 * tiene permiso de administrador
		 */
		setAsAdmin: function () {
			$window.localStorage.setItem(KEY, JSON.stringify(true));
		},

		/**
		 * Esta funcion debe ser invocada cuando el usuario que cierra su sesion,
		 * tiene permiso de administrador
		 */
		clearAsAdmin: function () {
			$window.localStorage.setItem(KEY, JSON.stringify(false));
		}
	}
}]);

/*
JwtManager es la factory que se utiliza para el almacenamiento,
obtencion y eliminacion de JWT
*/
app.factory('JwtManager', function ($window) {
	/*
	Con el valor de esta constante se obtiene el JWT del usuario que se
	autentica satisfactoriamente
	*/
	const KEY = "loggedUser";

	return {
		/*
		Cuando el usuario se autentica satisfactoriamente, se debe invocar
		a esta funcion para almacenar el JWT del usuario en el almacenamiento
		de sesion del navegador web
		*/
		setJwt: function (jwt) {
			$window.localStorage.setItem(KEY, jwt);
		},

		/*
		Esta funcion es necesaria para establecer el JWT (del usuario
		que se autentica satisfactoriamente) en el encabezado de
		autorizacion de cada peticion HTTP sea del cliente REST que
		sea, un navegador web, una aplicacion del estilo POSTMAN, etc.
		*/
		getJwt: function () {
			return $window.localStorage.getItem(KEY);
		},

		/*
		Esta funcion debe ser invocada cuando el usuario cierra su
		sesion, momento en el cual se debe eliminar su JWT del
		almacenamiento local del navegador web
		*/
		removeJwt: function () {
			$window.localStorage.removeItem(KEY);
		},

		/**
		 * 
		 * @returns carga util del JWT del usuario
		 */
		getPayload: function () {
			var token = this.getJwt();
			var base64Url = token.split('.')[1];
			var base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
			var jsonPayload = decodeURIComponent(window.atob(base64).split('').map(function (c) {
				return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
			}).join(''));

			return JSON.parse(jsonPayload);
		}
	}
});

/*
AuthHeaderManager es la factory que se utiliza para establecer el
JWT, del usuario que inicia sesion, en el encabezado de autorizacion
de HTTP para cada peticion HTTP y eliminar el contenido de dicho
encabezado cuando el usuario cierra su sesion
*/
app.factory('AuthHeaderManager', ['$http', 'JwtManager', function ($http, jwtManager) {
	return {
		/*
		Cuando el usuario se autentica satisfactoriamente, se debe invocar
		a esta funcion para establecer su JWT en el encabezado de autorizacion
		HTTP para cada peticion HTTP que se realice, ya que se usa JWT para la
		autenticacion, la autorizacion y las operaciones con datos.

		Por convencion, se usa la palabra "Bearer" en el encabezado de
		autorizacion de una peticion HTTP para indicar que se usa un
		JWT para autenticacion (principalmente), y ademas y opcionalmente,
		tambien para autorizacion.

		Por lo tanto, si el primer valor del encabezado de autorizacion de
		una peticion HTTP es la palabra "Bearer", entonces el segundo
		valor es un JWT.
		*/
		setJwtAuthHeader: function () {
			$http.defaults.headers.common.Authorization = 'Bearer ' + jwtManager.getJwt();
		},

		/*
		Cuando el usuario cierra su sesion, se debe invocar a esta funcion
		para eliminar el contenido del encabezado de autorizacion HTTP, ya
		que si no se hace esto, cada peticion HTTP se realizara con el
		mismo JWT independientemente de la cuenta que se utilice para
		iniciar sesion, lo cual, producira que la aplicacion devuelva
		datos que no son del usuario que tiene una sesion abierta. Por
		lo tanto, no eliminar el contenido del encabezado de autorizacion
		HTTP produce un comportamiento incorrecto por parte de la aplicacion.
		*/
		clearAuthHeader: function () {
			$http.defaults.headers.common.Authorization = '';
		},

		/**
		 * @returns true si el encabezado HTTP de autorizacion tiene el
		 * valor undefined, false en caso contrario
		 */
		isUndefined: function () {
			if ($http.defaults.headers.common.Authorization === undefined) {
				return true;
			}

			return false;
		}
	}
}]);

/*
ErrorResponseManager es la factory que se utiliza para el control de
las respuestas HTTP 401 (Unauthorized) y 403 (Forbidden) devueltas
por la aplicacion del lado servidor
*/
app.factory('ErrorResponseManager', ['$location', 'AccessManager', 'JwtManager', function ($location, accessManager, jwtManager) {

	const UNAUTHORIZED = 401;
	const FORBIDDEN = 403;
	const NOT_FOUND = 404;
	const USER_HOME_ROUTE = "/home";
	const ADMIN_HOME_ROUTE = "/adminHome";
	const USER_LOGIN_ROUTE = "/";
	const ADMIN_LOGIN_ROUTE = "/admin";

	return {
		/**
		 * Evalua la respuesta HTTP de error devuelta por la aplicacion del lado servidor.
		 * 
		 * Si la respuesta devuelta es el mensaje HTTP 401 (Unauthorized), redirige
		 * al usuario a la pagina web de inicio de sesion correspondiente. Si el
		 * usuario NO inicio sesion como administrador, lo redirige a la pagina
		 * web de inicio de sesion del usuario. Si el usuario inicio sesion como
		 * administrador (siempre y cuando tenga permiso de administrador), lo
		 * redirige a la pagina web de inicio de sesion del administrador.
		 * 
		 * Si la respuesta devuelta es el mensaje HTTP 403 (Forbidden) o el mensaje
		 * 404 (Not found), redirige al usuario a la pagina web de inicio correspondiente.
		 * Si el usuario NO tiene su sesion abierta como administrador, lo redirige a la
		 * pagina web de inicio del usuario. Si el usuario tiene su sesion abierta como
		 * administrador (siempre y cuando tenga permiso de  administrador), lo redirige
		 * a la pagina web de inicio del administrador.
		 * 
		 * @param {*} error este parametro es la respuesta HTTP de error devuelta por
		 * la aplicacion del lado servidor
		 */
		checkResponse: function (error) {
			/*
			Se imprime por pantalla la causa de la respuesta HTTP de error devuelta
			por la aplicacion del lado servidor
			*/
			alert(error.data.message);

			/*
			Si el usuario NO tiene una sesion abierta, si esta en la pagina web de
			inicio de sesion del usuario y si la respuesta HTTP de error devuelta por
			la aplicacion del lado servidor es la 401 (Unauthorized), se redirige al
			usuario a la pagina web de inicio de sesion mencionada.

			Este control es para el caso de un fallido intento de inicio de sesion
			a traves de la pagina web de inicio de sesion del usuario.
			*/
			if ((!accessManager.isUserLoggedIn()) && ($location.url() === USER_LOGIN_ROUTE) && (error.status == UNAUTHORIZED)) {
				$location.path(USER_LOGIN_ROUTE);
				return;
			}

			/*
			Si el usuario NO tiene una sesion abierta, si esta en la pagina web de
			inicio de sesion del administrador y si la respuesta HTTP de error
			devuelta por la aplicacion del lado servidor es la 401 (Unauthorized)
			o la 403 (Forbidden), se redirige al usuario a la pagina web de inicio
			de sesion mencionada.

			Este control es para el caso de un fallido intento de inicio de sesion
			a traves de la pagina web de inicio de sesion del administrador. Este
			control cubre tanto el caso en el que el usuario NO tiene permiso de
			administrador como el caso en el que si lo tiene.
			*/
			if ((!accessManager.isUserLoggedIn()) && ($location.url() === ADMIN_LOGIN_ROUTE) && (error.status == UNAUTHORIZED || error.status == FORBIDDEN)) {
				$location.path(ADMIN_LOGIN_ROUTE);
				return;
			}

			/*
			************************************************************************************
			Los siguientes controles se ocupan de manejar las respuestas HTTP de error devueltas
			por la aplicacion del lado servidor, para el caso en el que usuario SI tiene una
			sesion abierta, y lo hacen tanto para los casos en los que el usuario NO tiene permiso
			de administrador como para los casos en los que el usuario SI tiene dicho permiso
			************************************************************************************
			*/

			/*
			Si el usuario para el cual la aplicacion del lado servidor devuelve el mensaje
			HTTP 401 (Unauthorized), NO inicio sesion como administrador, se lo redirige a la
			pagina web de inicio de sesion del usuario.

			Este control es para el caso en el que el JWT del usuario que tiene una sesion
			abierta, expira. Ante un intento de inicio de sesion satisfactorio, la
			aplicacion del lado servidor retorna un JWT, el cual, tiene una cantidad
			de tiempo en el que se lo debe utilizar. Por lo tanto, cuando pasa esa
			cantidad de tiempo, un JWT expira. En consecuencia, un JWT NO es valido y
			al no ser valido, se lo debe eliminar del almacenamiento en el que se lo guarde.
			Si no se realiza esta eliminacion, la aplicacion del lado del navegador web
			tendra un comportamiento incorrecto, el cual, consiste en que mostrara al usuario
			la pagina de inicio del usuario o la pagina de inicio del administrador,
			dependiendo de si la sesion se abrio como usuario o como administrador (siempre y
			cuando el usuario tenga permiso de administrador). Este comportamiento es incorrecto
			porque un JWT no valido corresponde a no tener una sesion abierta, y si no se tiene
			una sesion abierta, no se debe mostrar ningun menu al usuario tenga este o no permiso
			de administrador.

			Cuando se realiza una peticion HTTP con un JWT expirado, la aplicacion del
			lado servidor retorna el mensaje HTTP 401 (Unauthorized) junto con el
			mensaje "Sesion expirada". Este caso es el motivo por el cual se realiza
			este control.
			*/
			if (error.status == UNAUTHORIZED && !accessManager.loggedAsAdmin()) {
				jwtManager.removeJwt();
				$location.path(USER_LOGIN_ROUTE);
				return;
			}

			/*
			Si el usuario para el cual la aplicacion del lado servidor devuelve el mensaje
			HTTP 401 (Unauthorized), inicio sesion como administrador (siempre y cuando
			tenga permiso de administrador), se lo redirige a la pagina web de inicio
			de sesion del administrador.

			Este control es para el caso en el que el JWT del administrador que tiene una
			sesion abierta, expira. Ante un intento de inicio de sesion satisfactorio, la
			aplicacion del lado servidor retorna un JWT, el cual, tiene una cantidad
			de tiempo en el que se lo debe utilizar. Por lo tanto, cuando pasa esa
			cantidad de tiempo, un JWT expira. En consecuencia, un JWT NO es valido y
			al no ser valido, se lo debe eliminar del almacenamiento en el que se lo guarde.
			Si no se realiza esta eliminacion, la aplicacion del lado del navegador web
			tendra un comportamiento incorrecto, el cual, consiste en que mostrara al usuario
			la pagina de inicio del usuario o la pagina de inicio del administrador,
			dependiendo de si la sesion se abrio como usuario o como administrador (siempre y
			cuando el usuario tenga permiso de administrador). Este comportamiento es incorrecto
			porque un JWT no valido corresponde a no tener una sesion abierta, y si no se tiene
			una sesion abierta, no se debe mostrar ningun menu al usuario tenga este o no permiso
			de administrador.

			Cuando se realiza una peticion HTTP con un JWT expirado, la aplicacion del
			lado servidor retorna el mensaje HTTP 401 (Unauthorized) junto con el
			mensaje "Sesion expirada". Este caso es el motivo por el cual se realiza
			este control.
			*/
			if (error.status == UNAUTHORIZED && accessManager.loggedAsAdmin()) {
				jwtManager.removeJwt();
				$location.path(ADMIN_LOGIN_ROUTE);
				return;
			}

			/*
			Si el usuario para el cual la aplicacion del lado servidor devuelve el mensaje
			HTTP 403 (Forbidden), NO tiene su sesion abierta como administrador, se lo
			redirige a la pagina web de inicio del usuario.

			Este control es para los siguientes casos:
			- un usuario que NO tiene permiso de administrador, intenta acceder a un
			recurso para el cual se requiere dicho permiso.
			- un usuario que NO tiene permiso de administrador, intenta acceder a un
			recurso de otro usuario, como una parcela, por ejemplo.

			Cuando ocurren estos casos, la aplicacion del lado servidor retorna el
			mensaje HTTP 403 (Forbidden) junto con el mensaje "Acceso no autorizado".
			Estos casos son el motivo por el cual se realiza este control.
			*/
			if (error.status == FORBIDDEN && !accessManager.loggedAsAdmin()) {
				$location.path(USER_HOME_ROUTE);
				return;
			}

			/*
			Si el usuario para el cual la aplicacion del lado servidor devuelve el mensaje
			HTTP 403 (Forbidden), tiene su sesion abierta como administrador, se lo
			redirige a la pagina web de inicio del administrador.

			Este control es para el caso en el que un usuario con permiso de administrador,
			intenta acceder a un recurso de otro usuario, como una parcela, por ejemplo.

			Cuando ocurre este caso, la aplicacion del lado servidor retorna el mensaje
			HTTP 403 (Forbidden) junto con el mensaje "Acceso no autorizado". Este caso
			es el motivo por el cual se realiza este control.

			Aunque este codigo no sea ejecutado debido a la instruccion if que evalua
			si el usuario inicio sesion como administrador en el controller de cada
			pagina web que es accedida por el usuario (tarea #73), lo mismo queda
			escrito, ya que dicha instruccion puede ser eliminada.
			*/
			if (error.status == FORBIDDEN && accessManager.loggedAsAdmin()) {
				$location.path(ADMIN_HOME_ROUTE);
				return;
			}

			/*
			Si el usuario para el cual la aplicacion del lado servidor devuelve el mensaje
			HTTP 404 (Not found), NO tiene su sesion abierta como administrador, se lo
			redirige a la pagina web de inicio del usuario.

			Este control es para el caso en el que un usuario que NO tiene permiso de
			administrador, intenta acceder a un recurso inexistente en la base de datos
			subyacente del sistema.

			Cuando ocurre este caso, la aplicacion del lado servidor retorna el mensaje
			HTTP 404 (Not found) junto con el mensaje "Recurso no encontrado". Este
			caso es el motivo por el cual se realiza este control.
			*/
			if (error.status == NOT_FOUND && !accessManager.loggedAsAdmin()) {
				$location.path(USER_HOME_ROUTE);
				return;
			}

			/*
			Si el usuario para el cual la aplicacion del lado servidor devuelve el mensaje
			HTTP 404 (Not found), tiene su sesion abierta como administrador, se lo
			redirige a la pagina web de inicio del administrador.

			Este control es para el caso en el que un usuario que tiene permiso de
			administrador, intenta acceder a un recurso inexistente en la base de datos
			subyacente del sistema.

			Cuando ocurre este caso, la aplicacion del lado servidor retorna el mensaje
			HTTP 404 (Not found) junto con el mensaje "Recurso no encontrado". Este
			caso es el motivo por el cual se realiza este control.
			*/
			if (error.status == NOT_FOUND && accessManager.loggedAsAdmin()) {
				$location.path(ADMIN_HOME_ROUTE);
			}
		}
	}
}]);

/*
LogoutManager es la factory que se utiliza para realizar el cierre
de sesion del usuario tenga este o no permiso de administrador
*/
app.factory('LogoutManager', ['JwtManager', 'ErrorResponseManager', 'AuthHeaderManager', 'RedirectManager', 'LogoutSrv',
	function (jwtManager, errorResponseManager, authHeaderManager, redirectManager, logoutSrv) {
		return {
			/**
			 * Esta funcion realiza el cierre de sesion del usuario. Durante
			 * este cierre realiza la peticion HTTP de cierre de sesion (elimina
			 * logicamente la sesion activa del usuario en la base de datos, la
			 * cual, esta en el lado servidor), la eliminacion del JWT del usuario,
			 * el borrado del contenido del encabezado HTTP de autorizacion, el
			 * establecimiento en false del valor asociado a la clave "superuser"
			 * del almacenamiento local del navegador web y la redireccion a la
			 * pagina web de inicio de sesion correspondiente dependiendo si el
			 * usuario inicio sesion como administrador o no.
			 */
			logout: function () {
				/*
				Con esta peticion se elimina logicamente de la base de datos
				(en el backend) la sesion activa del usuario. Si no se hace
				esta eliminacion lo que sucedera es que, cuando el usuario
				que abrio y cerro su sesion, intente abrir otra sesion, la
				aplicacion no se lo permitira, ya que la sesion anteriormente
				cerrada aun sigue activa.
		
				Cuando se elimina logicamente una sesion activa de la base
				de datos subyacente (en el backend), la sesion pasa a estar
				inactiva. De esta manera, el usuario que abrio y cerro su
				sesion, puede abrir nuevamente otra sesion.
				*/
				logoutSrv.logout(function (error) {
					if (error) {
						console.log(error);
						errorResponseManager.checkResponse(error);
					}
				});

				/*
				Cuando el usuario cliente cierra su sesion, se elimina su JWT
				del almacenamiento local del navegador web
				*/
				jwtManager.removeJwt();

				/*
				Cuando el usuario cierra su sesion, se elimina el contenido
				del encabezado de autorizacion HTTP, ya que de no hacerlo la
				aplicacion usara el mismo JWT para todas las peticiones HTTP,
				lo cual, producira que la aplicacion del lado servidor
				devuelva datos que no pertenecen al usuario que tiene una
				sesion abierta
				*/
				authHeaderManager.clearAuthHeader();

				/*
				Redirige al usuario a la pagina web de inicio de sesion en
				funcion de si inicio sesion como usuario o como administrador.
				Si el usuario inicio sesion como usuario, y la cierra, la
				aplicacion lo redirige a la pagina web de inicio de sesion del
				usuario. En cambio, si inicio sesion como administrador, y la
				cierra, la aplicacion lo redirige a la pagina web de inicio de
				sesion del administrador.
				*/
				redirectManager.redirectUser();
			}
		}
	}
]);

/*
ExpirationManager es la factory que se utiliza para verificar si
la sesion del usuario expiro o no, y en base a esto realizar
determinadas acciones, como eliminar el JWT del usuario del
almacenamiento local del navegador, por ejemplo
*/
app.factory('ExpirationManager', ['JwtManager', 'AuthHeaderManager', function (jwtManager, authHeaderManager) {
	return {
		/**
		 * 
		 * @returns true si el JWT del usuario expiro, false
		 * en caso contrario
		 */
		isExpire: function () {
			var currentTime = this.getTimestampInSeconds();

			/*
			Obtiene la carga util del JWT que esta en el
			almacenamiento local del navegador web.

			Este JWT es del usuario y es devuelto por la
			aplicacion del lado servidor ante un satisfactorio
			inicio de sesion del usuario.
			*/
			var payload = jwtManager.getPayload();

			/*
			Si el tiempo actual es estrictamente mayor que el
			tiempo de expiracion del JWT del usuario, significa
			que el JWT expiro, por lo tanto, la sesion del usuario
			expiro.
			*/
			if (currentTime > payload.exp) {
				return true;
			}

			return false;
		},

		/**
		 * Elimina el JWT del usuario del almacenamiento local del
		 * navegador web y del encabezado de autorizacion HTTP.
		 * 
		 * Esta funcion debe ser invocada cuando el JWT del usuario
		 * expira (sesion expirada), ya que un JWT expirado no es
		 * valido para realizar peticiones HTTP, con lo cual, se lo
		 * debe eliminar del almacenamiento local del navegador web
		 * y del encabezado de autorizacion HTTP.
		 */
		clearUserState: function () {
			jwtManager.removeJwt();
			authHeaderManager.clearAuthHeader();
		},

		displayExpiredSessionMessage: function () {
			alert("Sesión expirada");
		},

		/**
		 * 
		 * @returns el tiempo actual en segundos
		 */
		getTimestampInSeconds: function () {
			return Math.floor(Date.now() / 1000)
		}
	}
}]);

/*
RedirectManager es la factory que se utiliza para redirigir
al usuario a la pagina de inicio de sesion correspondiente (*)
cuando el usuario cierra su sesion y cuando esta expira.

(*) Si el usuario inicio sesion como usuario, esto es a traves
de la pagina web de inicio de sesion del usuario, esta
factory redirige al usuario a dicha pagina web. En cambio,
si el usuario inicio sesion como administrador, esto es a
traves de la pagina web de inicio de sesion del administrador,
esta factory redirige al administrador a dicha pagina web.
*/
app.factory('RedirectManager', ['AccessManager', '$location', function (accessManager, $location) {
	return {
		redirectUser: function () {
			/*
			Si el usuario inicio sesion como administrador (siempre y cuando
			tenga permiso de administrador), se establece en false el valor
			asociado a la clave "superuser" y se redirige al usuario a la
			pagina web de inicio de sesion del administrador.

			Esta descripcion es para cuando el usuario cierra su sesion (lo cual,
			se hace a traves de la factory LogoutManager) y para cuando expira la
			sesion del usuario (lo cual, se comprueba mediante la factory ExpirationManager).
			*/
			if (accessManager.loggedAsAdmin()) {
				/*
				Cuando un administrador cierra su sesion o cuando expira su sesion, la variable
				booleana que se utiliza para controlar su acceso a las paginas web a las que
				accede un usuario, se establece en false, ya que de no hacerlo dicha variable
				tendria el valor true y se le impediria el acceso a dichas paginas web a un
				administrador cuando inicie sesion a traves de la pagina de inicio de sesion
				del usuario
				*/
				accessManager.clearAsAdmin();

				/*
				Cuando el administrador cierra su sesion o cuando expira su sesion, se lo
				redirige a la pagina web de inicio de sesion del administrador
				*/
				$location.path("/admin");
				return;
			}

			/*
			Cuando el usuario cliente cierra su sesion o cuando expira su sesion, se lo
			redirige a la pagina de inicio de sesion del usuario
			*/
			$location.path("/");
		}
	}
}]);

/*
UtilDate es la factory creada para todas las operaciones
con fechas.
*/
app.factory('UtilDate', function () {
	return {
		/**
		 * 
		 * @param {*} dateOne 
		 * @param {*} dateTwo 
		 * @returns -1 si la fecha uno es estrictamente menor a la
		 * fecha dos, 0 si la fecha uno y la fecha dos son iguales
		 * y 1 si la fecha uno es estrictamente mayor a la fecha dos
		 */
		compareTo: function (dateOne, dateTwo) {
			if (dateOne.getFullYear() < dateTwo.getFullYear()) {
				return -1;
			}

			if (dateOne.getFullYear() > dateTwo.getFullYear()) {
				return 1;
			}

			if (dateOne.getMonth() < dateTwo.getMonth()) {
				return -1;
			}

			if (dateOne.getMonth() > dateTwo.getMonth()) {
				return 1;
			}

			if (dateOne.getDate() < dateTwo.getDate()) {
				return -1;
			}

			if (dateOne.getDate() > dateTwo.getDate()) {
				return 1;
			}

			return 0;
		},

		/**
		 * 
		 * @param {*} minorDate 
		 * @param {*} majorDate 
		 * @returns la diferencia en dias entre dos fechas dadas
		 */
		calculateDifferenceBetweenDates: function (minorDate, majorDate) {
			var minorDateTime = new Date(minorDate).getTime();
			var majorDateTime = new Date(majorDate).getTime();

			// (1000 * 60 * 60 * 24) --> milisegundos -> segundos -> minutos -> horas -> dias
			return (majorDateTime - minorDateTime) / (1000 * 60 * 60 * 24);
		},

		/**
		 * 
		 * @param {*} seedDate 
		 * @param {*} lifeCycle 
		 * @returns la fecha de cosecha de un cultivo calculada en funcion
		 * de su fecha de siembra y su ciclo de vida
		 */
		calculateSuggestedHarvestDate: function (seedDate, lifeCycle) {
			/*
			El ciclo de vida de un cultivo esta medido en dias y el metodo getTime
			de la clase Date retorna un numero en milisegundos desde el 1 de enero
			de 1970 00:00:00 UTC. Por lo tanto, para calcular la fecha de cosecha
			de un cultivo a partir de su fecha de siembra y su ciclo de vida es
			necesario convertir el ciclo de vida a milisegundos.

			A la suma entre la fecha de siembra en milisegundos y el ciclo de vida
			de un cultivo en milisegundos se le resta un uno porque el ciclo de vida
			de un cultivo comienza desde su fecha de siembra y no desde el dia
			inmediatamente siguiente a la misma.

			Por ejemplo, si la fecha de siembra de un cultivo X es el 1-1-2023 y su
			ciclo de vida es de 30 dias, la fecha de cosecha del mismo es el resultado
			de la siguiente operacion: 1 + 30 - 1 = 30, esto es 30-1-2023. En cambio,
			si no se restara un uno a esta operacion, la fecha de cosecha del cultivo
			X seria 31-1-2023, lo cual es incorrecto porque el ciclo de vida de un
			cultivo comienza desde su fecha de siembra y no desde el dia inmediatamente
			siguiente a la misma.
			*/
			var harvestTime = seedDate.getTime() + (lifeCycle * 24 * 60 * 60 * 1000) - 1;
			var harvestDate = new Date(harvestTime);

			return harvestDate;
		},

		/**
		 * 
		 * @param {*} date 
		 * @returns string que contiene una fecha en el formato dd-MM-YYYY
		 */
		formatDate: function (date) {
			return date.getDate() + "-" + (date.getMonth() + 1) + "-" + date.getFullYear();
		}

	}
});