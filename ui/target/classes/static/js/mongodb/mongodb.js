/**
 * Created by miche on 25/10/2016.
 */
angular.module('mongodb', []).controller('mongodb', function ($http, $timeout, $interval) {
    var self = this;
    self.elaborationType = 2;
    self.autoCheck = 2;
    self.queryFlag = 2;
    var promise;



    //define state of the buttons
    self.state = {
        start: false,
        stop: false,
        check: false,
        checking: false,
    };

    self.moreless = {
        more: true,
        less: false,

    };

    //retrieve data from resource server to set up GUI on launch
    $http.get('http://localhost:9000/api/').then(function (response) {
        self.metrics = response.data;
        console.log('Elaborazione attuale: ', response.data);
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

    //call refresh() 1 or n times, depending on the ON/OFF option
    self.check = function () {
        var autoCheck = self.autoCheck;
        console.log('check function invoked');
        console.log("Value of autocheck: " + autoCheck);
        self.state.checking = true;
        self.updateGraph();
        console.log(self.state.checking);
        if (autoCheck == 2) {
            $timeout(function () {
                self.refresh();
                self.state.checking = false;
            }, 2000);
        }
        if (autoCheck == 1) {
            console.log(self.state.checking);
            promise = $interval(function () {
                self.refresh();
                console.log("Refreshed again");
            }, 1000);
        }

    };

    //stop refresh() function if ON option is active
    self.stopAutoCheck = function () {
        $interval.cancel(promise);
        self.state.checking = false;
        console.log(self.state.checking);
    };

    //retrieve data from resource server
    self.refresh = function () {
        $http.get('http://localhost:9000/api/refresh/').then(function (response) {
            console.log("Refresh request");
            self.metrics = response.data
            console.log('Elaborazione attuale: ', response.data);
        }, function () {
            console.error("errore di retrieval data");
        }).finally(function () {

        });

    };

    //start the stress test
    self.start = function () {
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
        console.log('Start function invoked', data);
        var config = {
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded;charset=utf-8;'
            }
        };
        // $timeout(function() {
        $http.post('http://localhost:9000/api/start/', data)
            .then(
                function (response) {
                    console.log("Start request");
                    self.metrics = response.data;
                    console.log('Elaborazione attuale: ', response.data);
                }, function (response) {
                    //handle error
                    console.error("Impossibile eseguire start()");
                    self.metrics = response.statusText;
                    console.log('Contenuto della response: ', response.data);
                    console.log('Status della response: ', response.statusText);
                }).finally(function () {
            self.state.checking = false;
        });

        // }, 2000);

    };

    //stop the stress test
    self.stop = function () {
        console.log('Stop function invoked');
        self.state.stop = false;
        self.state.check = false;
        self.state.start = true;
        self.stopAutoCheck();
        self.showLess();
        var data = {};
        var config = {
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded;charset=utf-8;'
            }
        };
        $timeout(function () {
            $http.post('http://localhost:9000/api/stop/', data, config)
                .then(
                    function (response) {
                        console.log("Stop request");
                        self.metrics = response.data;
                        console.log('Elaborazione attuale: ', response.data);
                    }, function () {
                        //handle error
                        console.error("Impossibile eseguire stop()");
                    }).finally(function () {
                self.state.checking = false;
            });
        }, 2000);

    };

    //launch a test query during test phase
    self.launchQuery = function (queryFlag) {
        var query = self.queryStr;

        console.log("flag:", queryFlag);
        self.state.stop = true;
        self.state.check = true;
        self.state.start = false;
        var data = {
            'query': query,
            'flag': queryFlag,
        };
        console.log('Start function invoked', data);
        var config = {
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded;charset=utf-8;'
            }
        };
        // $timeout(function() {
        $http.post('http://localhost:9000/api/launch/', data)
            .then(
                function (response) {
                    console.log("Query launched");
                }, function (response) {
                    //handle error
                    console.error("Impossibile eseguire launch()");
                }).finally(function () {
            self.state.checking = false;
        });

        // }, 2000);

    };

    //show more options
    self.showMore = function () {
        self.moreless.more = false;
        self.moreless.less = true;
    };

    //show less options
    self.showLess = function () {
        self.moreless.less = false;
        self.moreless.more = true;
    };

    self.updateGraph = function(){
        MG.data_graphic({
            title: "Downloads",
            description: "This graphic shows a time-series of downloads.",
            data: [{'date':new Date('2014-11-01'),'value':12},
                {'date':new Date('2014-11-02'),'value':18}],
            width: 600,
            height: 250,
            target: '#downloads',
            x_accessor: 'date',
            y_accessor: 'value',
        })
    }
});
