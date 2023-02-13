var app = angular.module('app', ['ngRoute', 'Pagination', 'ui.bootstrap', 'leaflet-directive']);

// ui.bootstrap es necesario para la busqueda que se hace cuando se ingresan caracteres
// Pagination es necesario para la paginacion

app.config(['$routeProvider', function (routeprovider) {
	routeprovider

		.when('/crops', {
			templateUrl: 'partials/user/user-crop-list.html',
			controller: 'UserCropsCtrl'
		})
		.when('/crops/:action', {
			templateUrl: 'partials/user/user-crop-form.html',
			controller: 'UserCropCtrl'
		})
		.when('/crops/:action/:id', {
			templateUrl: 'partials/user/user-crop-form.html',
			controller: 'UserCropCtrl'
		})

		.when('/climateRecords', {
			templateUrl: 'partials/user/climate-record-list.html',
			controller: 'ClimateRecordsCtrl'
		})
		.when('/climateRecords/:action', {
			templateUrl: 'partials/user/climate-record-form.html',
			controller: 'ClimateRecordCtrl'
		})
		.when('/climateRecords/:action/:id', {
			templateUrl: 'partials/user/climate-record-form.html',
			controller: 'ClimateRecordCtrl'
		})

		.when('/plantingRecords', {
			templateUrl: 'partials/user/planting-record-list.html',
			controller: 'PlantingRecordsCtrl'
		})
		.when('/plantingRecords/:action', {
			templateUrl: 'partials/user/planting-record-form.html',
			controller: 'PlantingRecordCtrl'
		})
		.when('/plantingRecords/:action/:id', {
			templateUrl: 'partials/user/planting-record-form.html',
			controller: 'PlantingRecordCtrl'
		})

		.when('/parcels', {
			templateUrl: 'partials/user/parcel-list.html',
			controller: 'ParcelsCtrl'
		})

		.when('/parcels/:action', {
			templateUrl: 'partials/user/parcel-form.html',
			controller: 'ParcelCtrl'
		})

		.when('/parcels/:action/:id', {
			templateUrl: 'partials/user/parcel-form.html',
			controller: 'ParcelCtrl'
		})

		.when('/admin/crops', {
			templateUrl: 'partials/admin/admin-crop-list.html',
			controller: 'AdminCropsCtrl'
		})
		.when('/admin/crops/:action', {
			templateUrl: 'partials/admin/admin-crop-form.html',
			controller: 'AdminCropCtrl'
		})
		.when('/admin/crops/:action/:id', {
			templateUrl: 'partials/admin/admin-crop-form.html',
			controller: 'AdminCropCtrl'
		})

		.when('/admin/users', {
			templateUrl: 'partials/admin/user-list.html',
			controller: 'UsersCtrl'
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
		}
	}
});