var app = angular.module('app',['ngRoute', 'Pagination', 'ui.bootstrap', 'leaflet-directive']);

// ui.bootstrap es necesario para la busqueda que se hace cuando se ingresan caracteres
// Pagination es necesario para la paginacion

app.config(['$routeProvider', function(routeprovider) {
	routeprovider

	.when('/crops',{
		templateUrl:'partials/user/user-crop-list.html',
		controller: 'UserCropsCtrl'
	})
	.when('/crops/:action',{
		templateUrl:'partials/user/user-crop-form.html',
		controller: 'UserCropCtrl'
	})
	.when('/crops/:action/:id',{
		templateUrl:'partials/user/user-crop-form.html',
		controller: 'UserCropCtrl'
    })

	.when('/climateRecords',{
		templateUrl:'partials/user/climate-record-list.html',
		controller: 'ClimateRecordsCtrl'
	})
	.when('/climateRecords/:action',{
		templateUrl:'partials/user/climate-record-form.html',
		controller: 'ClimateRecordCtrl'
	})
	.when('/climateRecords/:action/:id',{
		templateUrl:'partials/user/climate-record-form.html',
		controller: 'ClimateRecordCtrl'
    })

	.when('/plantingRecords',{
		templateUrl:'partials/user/planting-record-list.html',
		controller: 'PlantingRecordsCtrl'
	})
	.when('/plantingRecords/:action',{
		templateUrl:'partials/user/planting-record-form.html',
		controller: 'PlantingRecordCtrl'
	})
	.when('/plantingRecords/:action/:id',{
		templateUrl:'partials/user/planting-record-form.html',
		controller: 'PlantingRecordCtrl'
	})

	.when('/admin/crops',{
		templateUrl:'partials/admin/admin-crop-list.html',
		controller: 'AdminCropsCtrl'
	})
	.when('/admin/crops/:action',{
		templateUrl:'partials/admin/admin-crop-form.html',
		controller: 'AdminCropCtrl'
	})
	.when('/admin/crops/:action/:id',{
		templateUrl:'partials/admin/admin-crop-form.html',
		controller: 'AdminCropCtrl'
    })

	.when('/admin/users',{
		templateUrl:'partials/admin/user-list.html',
		controller: 'UsersCtrl'
	})

	/* Parcel */
	.when('/parcel',{
		templateUrl:'partials/parcel-list.html',
		controller: 'ParcelsCtrl'
	})

	.when('/parcel/:action',{
		templateUrl:'partials/parcel-form.html',
		controller: 'ParcelCtrl'
	})

	.when('/parcel/:action/:id',{
		templateUrl:'partials/parcel-form.html',
		controller: 'ParcelCtrl'
	})

	/* De otra manera */
	.otherwise({
		templateUrl: 'partials/404.html'
	})
}])
