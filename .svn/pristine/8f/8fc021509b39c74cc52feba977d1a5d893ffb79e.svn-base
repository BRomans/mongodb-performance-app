angular.module('message', []).controller('message', function($http) {
	var self = this;
	$http.get('http://localhost:9000/health/').then(function(response) {
		self.resource = response.data;
		console.log(self.resource);
	});
	$http.get('/health/').then(function(response) {
		self.ui = response.data;
		console.log(self.ui);
	});
	$http.get('http://localhost:9000/health/').then(function(response) {
        self.mongo = response.data;
        console.log(self.mongo);
    });
});
