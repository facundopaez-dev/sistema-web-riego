var app = angular.module('app', ['ngRoute', 'Pagination', 'ui.bootstrap', 'leaflet-directive', 'chart.js']);

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

		.when('/home/soils', {
			templateUrl: 'partials/user/user-soil-list.html',
			controller: 'UserSoilsCtrl'
		})

		.when('/home/parcels/options/:id', {
			templateUrl: 'partials/user/parcel-option-form.html',
			controller: 'OptionCtrl'
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

		.when('/home/harvests', {
			templateUrl: 'partials/user/harvest-list.html',
			controller: 'HarvestsCtrl'
		})
		.when('/home/harvests/:action', {
			templateUrl: 'partials/user/harvest-form.html',
			controller: 'HarvestCtrl'
		})
		.when('/home/harvests/:action/:id', {
			templateUrl: 'partials/user/harvest-form.html',
			controller: 'HarvestCtrl'
		})

		.when('/home/soilWaterBalances', {
			templateUrl: 'partials/user/soil-water-balance-list.html',
			controller: 'SoilWaterBalancesCtrl'
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

		.when('/adminHome/typesCrop', {
			templateUrl: 'partials/admin/type-crop-list.html',
			controller: 'TypesCropCtrl'
		})
		.when('/adminHome/typesCrop/:action', {
			templateUrl: 'partials/admin/type-crop-form.html',
			controller: 'TypeCropCtrl'
		})
		.when('/adminHome/typesCrop/:action/:id', {
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

		.when('/adminHome/soils', {
			templateUrl: 'partials/admin/admin-soil-list.html',
			controller: 'AdminSoilsCtrl'
		})
		.when('/adminHome/soils/:action', {
			templateUrl: 'partials/admin/admin-soil-form.html',
			controller: 'AdminSoilCtrl'
		})
		.when('/adminHome/soils/:action/:id', {
			templateUrl: 'partials/admin/admin-soil-form.html',
			controller: 'AdminSoilCtrl'
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

		.when('/adminHome/users', {
			templateUrl: 'partials/admin/user-list.html',
			controller: 'UsersCtrl'
		})
		.when('/adminHome/users/:action', {
			templateUrl: 'partials/admin/admin-permission-modification-form.html',
			controller: 'PermissionModifierCtrl'
		})
		.when('/adminHome/users/:action/:id', {
			templateUrl: 'partials/admin/admin-permission-modification-form.html',
			controller: 'PermissionModifierCtrl'
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
	const SUPERUSER_KEY = "superuser";
	const SUPERUSER_PERMISSION_MODIFIER_KEY = "superuserPermissionModifier";

	return {
		/**
		 * Cuando el usuario inicia sesion satisfactoriamente, la aplicacion
		 * del lado servidor devuelve un JWT, el cual es almacenado en el
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
		 * @returns true si el usuario que tiene una sesion abierta tiene
		 * permiso de administrador, false en caso contrario
		 */
		loggedAsAdmin: function () {
			return JSON.parse($window.localStorage.getItem(SUPERUSER_KEY));
		},

		/**
		 * Esta funcion debe ser invocada cuando el usuario que inicia sesion,
		 * tiene permiso de administrador
		 */
		setAsAdmin: function () {
			$window.localStorage.setItem(SUPERUSER_KEY, JSON.stringify(true));
		},

		/**
		 * Establece el valor false en la clave superuser almacenada en el
		 * almacenamiento local del navegador web
		 */
		clearAsAdmin: function () {
			$window.localStorage.setItem(SUPERUSER_KEY, JSON.stringify(false));
		},

		/**
		 * Almacena el valor del permiso para modificar el permiso de administrador
		 * en el almacenamiento local del navegador web
		 */
		setSuperuserPermissionModifier: function (superuserPermissionModifier) {
			$window.localStorage.setItem(SUPERUSER_PERMISSION_MODIFIER_KEY, JSON.stringify(superuserPermissionModifier));
		},

		/**
		 * @returns true si el usuario que tiene una sesion abierta tiene
		 * permiso para modificar el permiso de administrador, false en
		 * caso contrario
		 */
		getSuperuserPermissionModifier: function () {
			return JSON.parse($window.localStorage.getItem(SUPERUSER_PERMISSION_MODIFIER_KEY));
		},

		/**
		 * Establece el valor false en la clave superuserPermissionModifier
		 * almacenada en el almacenamiento local del navegador web
		 */
		clearSuperuserPermissionModifier: function () {
			$window.localStorage.setItem(SUPERUSER_PERMISSION_MODIFIER_KEY, JSON.stringify(false));
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
		},

		/**
		 * Retorna el valor asociado a la clave superuserPermissionModifier
		 * de un JWT
		 * 
		 * @param jwt
		 */
		getSuperuserPermissionModifier: function () {
			var payload = this.getPayload();
			return payload.superuserPermissionModifier;
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

	const BAD_REQUEST = 400;
	const UNAUTHORIZED = 401;
	const FORBIDDEN = 403;
	const NOT_FOUND = 404;
	const TOO_MANY_REQUESTS = 429;
	const INTERNAL_SERVER_ERROR = 500;
	const SERVICE_UNAVAILABLE = 503;

	const USER_HOME_ROUTE = "/home";
	const ADMIN_HOME_ROUTE = "/adminHome";
	const USER_LOGIN_ROUTE = "/";
	const ADMIN_LOGIN_ROUTE = "/admin";
	const USER_CROP_LIST_WEB_PAGE_ROUTE = "/home/crops";
	const ADMIN_CROP_LIST_WEB_PAGE_ROUTE = "/adminHome/crops";
	const USER_SOIL_LIST_WEB_PAGE_ROUTE = "/home/soils";
	const USER_PARCEL_LIST_WEB_PAGE_ROUTE = "/home/parcels";
	const ADMIN_SOIL_LIST_WEB_PAGE_ROUTE = "/adminHome/soils";
	const ADMIN_REGION_LIST_WEB_PAGE_ROUTE = "/adminHome/regions";
	const ADMIN_TYPES_CROP_LIST_WEB_PAGE_ROUTE = "/adminHome/typesCrop";
	const USER_PLANTING_RECORD_ROUTE = "home/plantingRecords";
	const STATISTICAL_REPORT_ROUTE = "home/statisticalReports";

	/*
	El contenido de estas constantes debe ser igual al de las
	constantes de la clase Java SourceUnsatisfiedResponse, la
	cual se encuentra en la ruta app/src/util. De lo contrario,
	la funcion checkSearchResponse no funcionara correctamente.

	El motivo de estas constantes es realizar el correspondiente
	direccionamiento en caso de que en una busqueda no se encuentre
	el dato solicitado.
	*/
	const CROP = "ORIGIN_CROP";
	const SOIL = "ORIGIN_SOIL";
	const REGION = "ORIGIN_REGION";
	const TYPE_CROP = "ORIGIN_TYPE_CROP";
	const PARCEL = "ORIGIN_PARCEL";
	const WATER_NEED_CROP = "ORIGIN_NEED_WATER_CROP";
	const DEAD_CROP_WATER_NEED = "ORIGIN_DEAD_CROP_WATER_NEED";
	const NON_EXISTENT_DATA_FOR_STATISTICAL_REPORT = "ORIGIN_NON_EXISTENT_DATA_FOR_STATISTICAL_REPORT";
	const NON_EXISTENT_DATA_FOR_THE_GENERATION_OF_STATISTICAL_REPORTS = "ORIGIN_NON_EXISTENT_DATA_FOR_THE_GENERATION_OF_STATISTICAL_REPORTS";
	const NON_EXISTENT_DATA_FOR_THE_REGENERATION_OF_STATISTICAL_REPORTS = "ORIGIN_NON_EXISTENT_DATA_FOR_THE_REGENERATION_OF_STATISTICAL_REPORTS";

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
		 * Si el usuario NO tiene su sesion abierta como administrador, lo redirige
		 * a la pagina web de inicio del usuario. Si el usuario tiene su sesion
		 * abierta como administrador (siempre y cuando tenga permiso de administrador),
		 * lo redirige a la pagina web de inicio del administrador.
		 * 
		 * La mayoria de las respuestas HTTP del servidor que evalua la aplicacion
		 * del lado del navegador web son estas dos descritas.
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
			Al intentar calcular la necesidad de agua de riego de un cultivo la
			aplicacion del lado servidor puede devolver uno de los siguientes
			mensajes HTTP:
			- 400 (Bad request)
			- 429 (Too many requests)
			- 500 (Internal server error)
			- 503 (Service unavailable)

			El servicio meteorologico Visual Crossing Weather brinda 1000 peticiones
			gratuitas por dia. Si se supera esta cantidad de peticiones, dicho
			servicio devuelve el mensaje HTTP 429.

			Si el servicio meteorologico Visual Crossing Weather NO esta funcionamiento,
			devuelve el mensaje HTTP 500.

			Si la clave para realizar peticiones al servicio meteorologico Visual
			Crossing Weather no es la correcta, dicho servicio retorna el mensaje
			HTTP 401 (Unauthorized).

			La aplicacion del lado servidor recibe estos mensajes HTTP de parte
			de dicho servicio al intentar calcular la necesidad de agua de riego
			de cultivo. Cuando los recibe los devuelve a la aplicacion del lado
			del navegador web junto con un mensaje que describe el motivo por el
			cual NO pudo calcular la necesidad de agua de riego de un cultivo. En
			el caso de recibir el mensaje HTTP 401 de parte de dicho servicio,
			devuelve el mensaje HTTP 400.

			En el caso en el que el servicio meteorologico Visual Crossing Weather
			devuelve a la aplicacion del lado servidor un mensaje HTTP distinto a
			429 y 503, la aplicacion del lado servidor devuelve el mensaje HTTP
			500 a la aplicacion del lado del navegador web junto con un mensaje
			que describe que hubo un problema al calcular la necesidad de agua de
			riego de un cultivo, pero sin describir el motivo de dicho problema.

			Cuando la aplicacion del lado del navegador web recibe uno de estos
			mensajes HTTP de parte de la aplicacion del lado servidor, redirige
			al usuario a la pagina web de registros de plantacion.
			*/
			if (accessManager.isUserLoggedIn() && !accessManager.loggedAsAdmin() && error.data.sourceUnsatisfiedResponse == WATER_NEED_CROP
				&& (error.status == BAD_REQUEST || error.status == TOO_MANY_REQUESTS || error.status == INTERNAL_SERVER_ERROR || error.status == SERVICE_UNAVAILABLE)) {
				$location.path(USER_PLANTING_RECORD_ROUTE);
				return;
			}

			/*
			Si el resultado de calcular la necesidad de agua de riego de un cultivo
			en la fecha actual [mm/dia] es un cultivo muerto, redirige al usuario
			a la pagina web de registros de plantacion
			*/
			if (accessManager.isUserLoggedIn() && !accessManager.loggedAsAdmin() && error.status == BAD_REQUEST
				&& error.data.sourceUnsatisfiedResponse == DEAD_CROP_WATER_NEED) {
				$location.path(USER_PLANTING_RECORD_ROUTE);
				return;
			}

			/* Si la aplicacion del lado servidor retorna el mensaje HTTP 400
			(Bad request) junto con la fuente de solicitud insatisfecha
			NON_EXISTENT_DATA_FOR_STATISTICAL_REPORT, significa que el usuario
			intento generar informes estadisticos para una parcela que en el
			periodo elegido de generacion de informe estadisticos no tiene
			algunos de los datos necesarios para ello. Por lo tanto, la aplicacion
			del lado del navegador web le informa al usuario lo sucedido y lo
			redirige a la pagina web de lista de informe estadisticos. */
			if (accessManager.isUserLoggedIn() && !accessManager.loggedAsAdmin() && error.status == BAD_REQUEST
				&& error.data.sourceUnsatisfiedResponse == NON_EXISTENT_DATA_FOR_STATISTICAL_REPORT) {
				$location.path(STATISTICAL_REPORT_ROUTE);
				return;
			}

			/* Si la aplicacion del lado servidor retorna el mensaje HTTP 400
			(Bad request) junto con la fuente de solicitud insatisfecha
			NON_EXISTENT_DATA_FOR_THE_GENERATION_OF_STATISTICAL_REPORTS, significa
			que el usuario intento generar informes estadisticos para una parcela
			que no tiene registros de plantacion finalizados ni registros de
			riego ni registros de cosecha ni registros climaticos en el periodo
			elegido para la generacion de informes estadisticos. Por lo tanto, la
			aplicacion del lado del navegador web le informa al usuario lo sucedido
			y lo redirige a la pagina web de lista de informes estadisticos. */
			if (accessManager.isUserLoggedIn() && !accessManager.loggedAsAdmin() && error.status == BAD_REQUEST
				&& error.data.sourceUnsatisfiedResponse == NON_EXISTENT_DATA_FOR_THE_GENERATION_OF_STATISTICAL_REPORTS) {
				$location.path(STATISTICAL_REPORT_ROUTE);
				return;
			}

			/* Si la aplicacion del lado servidor retorna el mensaje HTTP 400
			(Bad request) junto con la fuente de solicitud insatisfecha
			NON_EXISTENT_DATA_FOR_THE_REGENERATION_OF_STATISTICAL_REPORTS,
			significa que el usuario intento regenerar un informe estadistico
			de una parcela que no tiene en el periodo del informe estadistico
			los datos sobre los cuales se genero dicho informe estadistico,
			los cuales son necesarios para su regeneracion. Por lo tanto, la
			aplicacion del lado del navegador web le informa al usuario lo
			sucedido y lo redirige a la pagina web de lista de informes
			estadisticos. */
			if (accessManager.isUserLoggedIn() && !accessManager.loggedAsAdmin() && error.status == BAD_REQUEST
				&& error.data.sourceUnsatisfiedResponse == NON_EXISTENT_DATA_FOR_THE_REGENERATION_OF_STATISTICAL_REPORTS) {
				$location.path(STATISTICAL_REPORT_ROUTE);
				return;
			}

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
			administrador o que tiene permiso de administrador, pero NO tiene una sesion
			abierta como administrador, intenta acceder a un recurso inexistente en la base
			de datos subyacente del sistema.

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
		},

		/**
		 * Evalua la respuesta HTTP de error devuelta por la aplicacion del lado servidor
		 * ante una peticion de busqueda.
		 * 
		 * Si el usuario realiza una busqueda en una pagina web de lista de algun dato y
		 * la respuesta devuelta es el mensaje HTTP 400 (Not found), redirige al usuario
		 * a la pagina web en la que realizo la busqueda.
		 * 
		 * @param {*} error este parametro es la respuesta HTTP de error devuelta por
		 * la aplicacion del lado servidor
		 */
		checkSearchResponse: function (error) {
			/*
			Se imprime por pantalla la causa de la respuesta HTTP de error devuelta
			por la aplicacion del lado servidor
			*/
			alert(error.data.message);

			/*
			Si el usuario busca un cultivo inexistente en la pagina web de lista de
			cultivos, redirige al usuario a la pagina web de lista de cultivos
			*/
			if (error.status == NOT_FOUND && error.data.sourceUnsatisfiedResponse == CROP && !accessManager.loggedAsAdmin()) {
				$location.path(USER_CROP_LIST_WEB_PAGE_ROUTE);
				return;
			}

			/*
			Si el usuario busca un suelo inexistente en la pagina web de lista de
			cultivos, redirige al usuario a la pagina web de lista de suelos
			*/
			if (error.status == NOT_FOUND && error.data.sourceUnsatisfiedResponse == SOIL && !accessManager.loggedAsAdmin()) {
				$location.path(USER_SOIL_LIST_WEB_PAGE_ROUTE);
				return;
			}

			/*
			Si el usuario busca una parcela inexistente en la pagina web de lista
			de parcelas, redirige al usuario a la pagina web de lista de parcelas
			*/
			if (error.status == NOT_FOUND && error.data.sourceUnsatisfiedResponse == PARCEL && !accessManager.loggedAsAdmin()) {
				$location.path(USER_PARCEL_LIST_WEB_PAGE_ROUTE);
				return;
			}

			/*
			Si el administrador busca un cultivo inexistente en la pagina web de
			lista de cultivos, redirige al administrador a la pagina web de lista
			de cultivos
			*/
			if (error.status == NOT_FOUND && error.data.sourceUnsatisfiedResponse == CROP && accessManager.loggedAsAdmin()) {
				$location.path(ADMIN_CROP_LIST_WEB_PAGE_ROUTE);
				return;
			}

			/*
			Si el administrador busca un suelo inexistente en la pagina web de
			lista de cultivos, redirige al administrador a la pagina web de lista
			de suelos
			*/
			if (error.status == NOT_FOUND && error.data.sourceUnsatisfiedResponse == SOIL && accessManager.loggedAsAdmin()) {
				$location.path(ADMIN_SOIL_LIST_WEB_PAGE_ROUTE);
				return;
			}

			/*
			Si el administrador busca una region inexistente en la pagina web de
			lista de regiones, redirige al administrador a la pagina web de lista
			de regiones
			*/
			if (error.status == NOT_FOUND && error.data.sourceUnsatisfiedResponse == REGION && accessManager.loggedAsAdmin()) {
				$location.path(ADMIN_REGION_LIST_WEB_PAGE_ROUTE);
				return;
			}

			/*
			Si el administrador busca un tipo de cultivo inexistente en la pagina web
			de lista de tipos de cultivo, redirige al administrador a la pagina web de
			lista tipos de cultivo
			*/
			if (error.status == NOT_FOUND && error.data.sourceUnsatisfiedResponse == TYPE_CROP && accessManager.loggedAsAdmin()) {
				$location.path(ADMIN_TYPES_CROP_LIST_WEB_PAGE_ROUTE);
				return;
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

				/* Asigna el valor false a la clave superuserPermissionModifier almacenada
				en el almacenamiento local del navegador web. Esto es para el caso en el
				que el administrador tiene el permiso para modificar el permiso superusuario
				(administrador) de un usuario, ya que luego del cierre de sesio no se requiere
				el valor true de dicha clave. */
				accessManager.clearSuperuserPermissionModifier();

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

	const JANUARY = 0;
	const DECEMBER = 11;
	const MAXIMUM_YEAR = 9999;

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
		 * @param {*} int 
		 * @param {*} year 
		 * @returns true si un año es bisiesto, en caso contrario false
		 */
		isLeapYear: function (year) {
			/*
			Un año es bisiesto si es divisible por 4 y no es divisible
			por 100
			*/
			if (year % 4 == 0 && !(year % 100 == 0)) {
				return true;
			}

			/*
			Un año es bisiesto si es divisible por 4, por 100 y por 400
			*/
			if (year % 4 == 0 && year % 100 == 0 && year % 400 == 0) {
				return true;
			}

			return false;
		},

		/**
		 * 
		 * @param {*} minorDate 
		 * @param {*} majorDate 
		 * @returns cantidad de dias de diferencia que hay entre dos
		 * fechas
		 */
		calculateDifferenceBetweenDates: function (minorDate, majorDate) {
			var minorDateTime = new Date(minorDate).getTime();
			var majorDateTime = new Date(majorDate).getTime();
			var yearsDifference = majorDate.getFullYear() - minorDate.getFullYear();

			/*
			Si la diferencia en años entre dos fechas es igual a 0,
			significa que las dos fechas tienen el mismo año. Por lo
			tanto, se calcula directamente la cantidad de dias que
			hay entre ambas.
			*/
			if (yearsDifference == 0) {
				// (1000 * 60 * 60 * 24) --> milisegundos -> segundos -> minutos -> horas -> dias
				return (majorDateTime - minorDateTime) / (1000 * 60 * 60 * 24);
			}

			/*
			Si la diferencia en años entre dos fechas es estrictamente
			mayor a cero, significa que el año de la fecha menor es
			menor al año de la fecha mayor por una unidad o mas. Por lo
			tanto, la cantidad de dias que hay entre dos fechas se
			calcula sumando la cantidad de dias que hay entre la fecha
			menor hasta el ultimo dia del año de la misma, la cantidad
			de dias de los años que hay entre la fecha menor y la fecha
			mayor, y la cantidad de dias que hay entre el primer dia del
			año de la fecha mayor hasta la misma.
			*/
			var lastDayDateTime = new Date(minorDate.getFullYear(), DECEMBER, 31).getTime();
			var firstDayDateTime = new Date(majorDate.getFullYear(), JANUARY, 1).getTime();
			var days = 0;

			/*
			(1000 * 60 * 60 * 24) --> milisegundos -> segundos -> minutos -> horas -> dias

			Convierte a dias el tiempo en milisegundos resultante de la
			diferencia en milisegundos entre la fecha menor y el ultimo
			dia de año de la fecha menor.

			Convierte a dias el tiempo en milisegundos resultante de la
			diferencia en milisegundos entre el primer dia en el año de
			la fecha mayor y la misma.
			*/
			var daysFromMinorDate = (lastDayDateTime - minorDateTime) / (1000 * 60 * 60 * 24);
			var daysUntileMajorDate = (majorDateTime - firstDayDateTime) / (1000 * 60 * 60 * 24);

			/*
			Acumula la cantidad de dias de los años que hay entre la
			fecha menor y la fecha mayor, sin acumular la cantidad de
			dias del año de la fecha menor ni del año de la fecha mayor
			*/
			for (let year = minorDate.getFullYear() + 1; year < majorDate.getFullYear(); year++) {

				if (!this.isLeapYear(year)) {
					days = days + 365;
				} else {
					days = days + 366;
				}

			}

			/*
			Se suma un uno para incluir unicamente la fecha menor en el
			calculo de la cantidad de dias que hay entre una fecha mayor
			y una fecha menor
			*/
			return (daysFromMinorDate + days + daysUntileMajorDate) + 1;
		},

		/**
		 * 
		 * @param {*} date 
		 * @returns string que contiene una fecha en el formato dd-MM-YYYY
		 */
		formatDate: function (date) {
			return date.getDate() + "-" + (date.getMonth() + 1) + "-" + date.getFullYear();
		},

		/**
		 * @param {*} date 
		 * @returns true si el año de una fecha es estrictamente
		 * mayor al año maximo (9999). En caso contrario, retorna
		 * false.
		 */
		yearIsGreaterThanMaximum: function (date) {

			if (date.getFullYear() > MAXIMUM_YEAR) {
				return true;
			}

			return false;
		}

	}
});

/*
WaterNeedFormManager es la factory que se utiliza para controlar el
acceso al formulario del calculo de la necesidad de agua de riego
de un cultivo. Si el usuario intenta acceder a dicho formulario
con el ID de un registro de plantacion que NO esta en desarrollo,
se debe impedir dicho acceso.
*/
app.factory('WaterNeedFormManager', function () {

	var plantingRecords = new Array();

	/*
	El estado "En desarrollo" se utiliza en el caso en el que
	la opcion del uso del suelo para el calculo de la necesidad
	de agua de riego de un cultivo en la fecha actual NO esta
	activa.

	El desarrollo optimo de un cultivo ocurre cuando el nivel de
	humedad del suelo, en el que esta sembrado, es menor o igual
	a la capacidad de campo del suelo y estrictamente mayor al
	umbral de riego.

	El desarrollo en riesgo de marchitez de un cultivo ocurre
	cuando el nivel de humedad del suelo, en el que esta sembrado,
	es menor o igual al umbral de riego y estrictamente mayor a
	la capacidad de almacenamiento de agua del suelo.

	El desarrollo en marchitez de un cultivo ocurre cuando el
	nivel de humedad del suelo, en el que esta sembrado, es
	estrictamente menor a la capacidad de almacenamiento de
	agua del suelo y mayor o igual al doble de la capacidad
	de almacenamiento de agua del suelo.
	*/
	const IN_DEVELOPMENT = "En desarrollo";
	const OPTIMAL_DEVELOPMENT = "Desarrollo óptimo";
	const DEVELOPMENT_AT_RISK_OF_WILTING = "Desarrollo en riesgo de marchitez";
	const DEVELOPMENT_IN_WILTING = "Desarrollo en marchitez";

	return {

		/**
		 * Inicializa un arreglo con los registros de plantacion
		 * asociados a las parcelas del usuario. Este arreglo se
		 * utiliza para realizar el control de acceso al formulario
		 * del calculo de la necesidad de agua de riego de un
		 * cultivo.
		 * 
		 * @param {*} collectionPlantingRecords 
		 */
		setPlantingRecords: function (collectionPlantingRecords) {
			plantingRecords = collectionPlantingRecords;
		},

		/**
		 * 
		 * @param {*} plantingRecordId 
		 * @returns true si el ID corresponde a un registro de
		 * plantacion que tiene un estado en desarrollo
		 */
		isInDevelopment: function (plantingRecordId) {

			for (let index = 0; index < plantingRecords.length; index++) {
				const currentPlantingRecord = plantingRecords[index];

				if (currentPlantingRecord.id == plantingRecordId
					&& (currentPlantingRecord.status.name == IN_DEVELOPMENT
						|| currentPlantingRecord.status.name == OPTIMAL_DEVELOPMENT
						|| currentPlantingRecord.status.name == DEVELOPMENT_AT_RISK_OF_WILTING
						|| currentPlantingRecord.status.name == DEVELOPMENT_IN_WILTING)) {
					return true;
				}

			}

			return false;
		}

	}
});

/*
ReasonError es la factory que contiene las causas por las
cuales no se puede realizar una peticion HTTTP
*/
app.factory('ReasonError', function () {

	const DATE_FROM_GREATEST_TO_MAXIMUM = "La fecha desde no debe ser estrictamente mayor a 9999";
	const DATE_UNTIL_GREATEST_TO_MAXIMUM = "La fecha hasta no debe ser estrictamente mayor a 9999";
	const DATE_GREATEST_TO_MAXIMUM = "La fecha no debe ser estrictamente mayor a 9999";

	return {

		getCauseDateFromGreatestToMaximum: function () {
			return DATE_FROM_GREATEST_TO_MAXIMUM;
		},

		getCauseDateUntilGreatestToMaximum: function () {
			return DATE_UNTIL_GREATEST_TO_MAXIMUM;
		},

		getCauseDateGreatestToMaximum: function () {
			return DATE_GREATEST_TO_MAXIMUM;
		}

	}

});