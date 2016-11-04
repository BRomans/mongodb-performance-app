angular.module('message', []).controller('message', function($http) {
	var self = this;
	$http.get('http://localhost:9000/api/status/').then(function(response) {
		self.resource = response.data;
	});
	$http.get('/status/').then(function(response) {
		self.ui = response.data;
	});
	$http.get('http://localhost:27017/').then(function(response) {
		self.mongo = response.data;
	});
});
