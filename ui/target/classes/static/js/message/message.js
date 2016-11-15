angular.module('message', []).controller('message', function($http) {
	var self = this;
	$http.get('http://localhost:9000/api/status/').then(function(response) {
		self.resource = response.data;
	});
	$http.get('/status/').then(function(response) {
		self.ui = response.data;
	});
	$http.get('http://172.16.34.76:27017/').then(function(response) {
		self.mongo = "Connected";
	}), function () {
		//handle error
		self.mongo = "Not Connected :(";
	};
});
