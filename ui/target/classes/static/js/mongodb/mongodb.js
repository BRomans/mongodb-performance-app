/**
 * Created by miche on 25/10/2016.
 */
angular.module('mongodb', []).controller('mongodb', function($http, $timeout, $interval) {
    var self = this;
    self.elaborationType = 2;
    self.autoCheck = 2;
    var promise;
    self.state={
        start: false,
        stop: false,
        check: false,
        checking : false,
    };
    $http.get('http://172.16.33.56:9000/api/').then(function(response) {
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
        var autoCheck = self.autoCheck;
        console.log('check function invoked');
        console.log("Value of autocheck: " + autoCheck);
        self.state.checking  = true;
        console.log(self.state.checking);
        if(autoCheck == 2){
            $timeout(function() {
                self.refresh();
                self.state.checking = false;
            }, 2000);
        }
        if(autoCheck == 1){
            console.log(self.state.checking);
            promise = $interval(function(){
                self.refresh();
                console.log("Refreshed again");
            }, 1000);
        }

    };

    self.stopAutoCheck = function(){
        $interval.cancel(promise);
        self.state.checking  = false;
        console.log(self.state.checking);
    };

    self.refresh = function(){
        $http.get('http://172.16.33.56:9000/api/refresh/').then(function(response){
            console.log("Refresh request");
            self.metrics = response.data
            console.log('Elaborazione attuale: ',response.data);
        },function(){
            console.error("errore di retrieval data");
        }).finally(function() {

        });

    };
    self.start = function() {
        var elaborationType = self.elaborationType;
        var parallelism = self.parallelism;
        var inputNumOfEntries = self.inputNumOfEntries;

        self.state.stop = true;
        self.state.check = true;
        self.state.start = false;
        var data = {
            'numOfEntries': inputNumOfEntries,
            'parallelism': parallelism,
            'elaborationTypes': elaborationType
        };
        console.log('Start function invoked',data );
        var config = {
            headers : {
                'Content-Type': 'application/x-www-form-urlencoded;charset=utf-8;'
            }
        };
       // $timeout(function() {
        $http.post('http://172.16.33.56:9000/api/start/', data)
        .then(
            function(response){
                console.log("Start request");
                self.metrics = response.data;
                console.log('Elaborazione attuale: ',response.data);
            },function(response){
                //handle error
                console.error("Impossibile eseguire start()");
                self.metrics = response.statusText;
                console.log('Contenuto della response: ',response.data);
                console.log('Status della response: ',response.statusText);
            }).finally(function() {
                self.state.checking  = false;
            });

       // }, 2000);

    };
    self.stop = function() {
        console.log('Stop function invoked');
        self.state.stop = false;
        self.state.check = false;
        self.state.start = true;
        self.stopAutoCheck();
        var data = {
        };
        var config = {
            headers : {
                'Content-Type': 'application/x-www-form-urlencoded;charset=utf-8;'
            }
        };
        $timeout(function() {
            $http.post('http://172.16.33.56:9000/api/stop/', data, config)
                .then(
                    function(response){
                        console.log("Stop request");
                        self.metrics = response.data;
                        console.log('Elaborazione attuale: ',response.data);
                    },function(){
                        //handle error
                        console.error("Impossibile eseguire stop()");
                    }).finally(function() {
                        self.state.checking  = false;
                    });
            }, 2000);

    };

});
