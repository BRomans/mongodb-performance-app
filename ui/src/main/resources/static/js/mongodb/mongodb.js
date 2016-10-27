/**
 * Created by miche on 25/10/2016.
 */
angular.module('mongodb', []).controller('mongodb', function($http, $timeout) {
    var self = this;
    self.state={
        start: false,
        stop: false,
        check: false,
        checking : false,
    };
    $http.get('http://localhost:9000/api/').then(function(response) {
        self.metrics = response.data;
        console.log('Elaborazione attuale: ',response.data);
        if (!response.data) {
            // case NO elaboration active
            self.state.start = true;
            self.state.stop = false;
            self.state.check = false;
        } else {
            // case elaboration active
            self.state.stop = true;
            self.state.start = false;
            self.state.check = true;
        }
    });

    self.check = function() {
        console.log('check function invoked');
        self.state.checking  = true;
        $timeout(function() {
            $http.get('http://localhost:9000/api/refresh/').then(function(response){
                console.log("Refresh request");
                self.metrics = response.data

                console.log('Elaborazione attuale: ',response.data);
            },function(){
                //handle error
                console.error("errore di retrieval data");
            }).finally(function() {
                self.state.checking  = false;
            });
        }, 2000);

    };
    self.start = function() {
        console.log('Start function invoked');
        self.state.stop = true;
        self.state.check = true;
        self.state.start = false;
        $timeout(function() {
            $http.post('http://localhost:9000/api/start/',
                {
                    numOfEntries: 100000,
                    parallelism: 4,
                    elaborationTypes: 3
                }).then(function(response){
                console.log("Start request");
                self.metrics = response.data;
                console.log('Elaborazione attuale: ',response.data);
            },function(){
                //handle error
                console.error("errore di retrieval data");
            }).finally(function() {
                self.state.checking  = false;
            });
        }, 2000);

    };
    self.stop = function() {
        console.log('Stop function invoked');
        self.state.stop = false;
        self.state.check = false;
        self.state.start = true;
        $timeout(function() {
            $http.get('http://localhost:9000/api/stop/').then(function(response){
                console.log("Stop request");
                self.metrics = response.data;
                console.log('Elaborazione attuale: ',response.data);
            },function(){
                //handle error
                console.error("errore di retrieval data");
            }).finally(function() {
                self.state.checking  = false;
            });
        }, 2000);

    };

});