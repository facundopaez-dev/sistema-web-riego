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