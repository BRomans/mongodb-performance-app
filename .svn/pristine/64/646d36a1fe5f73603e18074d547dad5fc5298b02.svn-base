/**
 * Created by miche on 25/10/2016.
 */
angular.module('mongodb', []).controller('mongodb', function ($http, $timeout, $interval) {
    var self = this;
    self.elaborationType = 3;
    self.autoCheck = 2;
    self.queryFlag = 2;
    var promiseFetch;
    var promiseGraph;
    var maxPutGraph;
    var minPutGraph;
    var maxGetGraph;
    var minGetGraph;
    var entryGraph;

    self.counters = {
        launchCountCounter: 0,
        launchComplexCounter: 0,
    };

    //define state of the buttons
    self.state = {
        start: false,
        stop: false,
        check: false,
        checking: false,
        starting: false,
        clearing: false,
        stopping: false,
        launchingCount: false,
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
        console.log(self.state.checking);
        if (autoCheck == 2) {
            $timeout(function () {
                self.fetchData();
                self.updateGraph();
                self.state.checking = false;
            }, 2000);
        }
        if (autoCheck == 1) {
            console.log(self.state.checking);
            promiseFetch = $interval(function () {
                self.fetchData();
                console.log("Refreshed again");
                self.updateGraph();
            }, 5000);
            promiseGraph = $interval(function () {

            }, 5000);
        }

    };

    //stop refresh() function if ON option is active
    self.stopAutoCheck = function () {
        $interval.cancel(promiseFetch);
        $interval.cancel(promiseGraph);
        self.state.checking = false;
        console.log(self.state.checking);
    };

    //retrieve data from resource server
    self.fetchData = function () {
        $http.get('http://localhost:9000/api/refresh/').then(function (response) {
            console.log("Refresh request");
            self.metrics = response.data;
            console.log('Elaborazione attuale: ', response.data);
            for (var i = 0; i < self.metrics.parallelTasks.length; i++) {
                var maxPutContent = {
                    'totalTime': self.metrics.parallelTasks[i].totalTime,
                    'maxPutTime': self.metrics.parallelTasks[i].maxPutTime,
                };
                var minPutContent = {
                    'totalTime': self.metrics.parallelTasks[i].totalTime,
                    'minPutTime': self.metrics.parallelTasks[i].minPutTime,
                };
                var maxGetContent = {
                    'totalTime': self.metrics.parallelTasks[i].totalTime,
                    'maxGetTime': self.metrics.parallelTasks[i].maxGetTime,
                };
                var minGetContent = {
                    'totalTime': self.metrics.parallelTasks[i].totalTime,
                    'minGetTime': self.metrics.parallelTasks[i].minGetTime,
                };
                var entryContent = {
                    'totalTime': self.metrics.parallelTasks[i].totalTime,
                    'currentEntries': self.metrics.parallelTasks[i].currentNoOfEntries,
                }
                //init graphs
                if (maxPutGraph[i] == null) {
                    maxPutGraph[i] = [];
                }
                if (minPutGraph[i] == null) {
                    minPutGraph[i] = [];
                }
                if (maxGetGraph[i] == null) {
                    maxGetGraph[i] = [];
                }
                if (minGetGraph[i] == null) {
                    minGetGraph[i] = [];
                }
                if (entryGraph[i] == null) {
                    entryGraph[i] = [];
                }
                //populate graphs
                maxPutGraph[i].push(maxPutContent);
                minPutGraph[i].push(minPutContent);
                maxGetGraph[i].push(maxGetContent);
                minGetGraph[i].push(minGetContent);
                entryGraph[i].push(entryContent);

            }
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
        var data = {
            'numOfEntries': inputNumOfEntries,
            'parallelism': parallelism,
            'elaborationTypes': elaborationType
        };
        var config = {
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded;charset=utf-8;'
            }
        };

        self.state.starting = true;
        self.initGraphs();

        console.log('Start function invoked', data);
        $timeout(function () {
            self.state.stop = true;
            self.state.check = true;
            self.state.start = false;
            $http.post('http://localhost:9000/api/start/', data)
                .then(
                    function (response) {
                        console.log("Start request");
                        self.metrics = response.data;
                        console.log('Elaborazione attuale: ', response.data);
                        self.fetchData();
                        $timeout(function () {
                            self.updateGraph();
                        }, 2100);

                    }, function (response) {
                        //handle error
                        console.error("Impossibile eseguire start()");
                        self.status = response.statusText;
                        console.log('Contenuto della response: ', response.data);
                        console.log('Status della response: ', self.status);
                    }).finally(function () {
                self.state.checking = false;
                self.state.starting = false;
            });
        }, 2000);


    };

    //stop the stress test
    self.stop = function () {
        console.log('Stop function invoked');
        self.state.stopping = true;
        self.state.check = false;
        self.stopAutoCheck();
        self.showLess();
        self.autoCheck = 2;
        self.counters.launchCountCounter = 0;
        self.counters.launchComplexCounter = 0;
        self.countData = null;
        self.complexData = null;
        self.deinitGraphs();

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
                self.state.stop = false;
                self.state.start = true;
                self.state.checking = false;
                self.state.stopping = false;
            });
        }, 2000);

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

    //init to 0 graph values
    self.initGraphs = function () {
        maxPutGraph = [];
        minPutGraph = [];
        maxGetGraph = [];
        minGetGraph = [];
        entryGraph = [];
    };

    //init to null value all graphs
    self.deinitGraphs = function () {
        maxPutGraph = null;
        minPutGraph = null;
        maxGetGraph = null;
        minGetGraph = null;
        entryGraph = null;
    };

    //fetch data for dynamic graph
    self.updateGraph = function () {
        //plot max Put time
        MG.data_graphic({
            title: "MaxPutTime",
            description: "This graphic shows Max Put Time over execution time",
            data: maxPutGraph,
            missing_is_zero: true,
            width: 500,
            height: 280,
            right: 40,
            target: '#maxPutTime',
            x_accessor: 'totalTime',
            y_accessor: 'maxPutTime',
        });
        //plot min Put time
        MG.data_graphic({
            title: "MinPutTime",
            description: "This graphic shows Min Put Time over execution time",
            data: minPutGraph,
            missing_is_zero: true,
            width: 500,
            height: 280,
            right: 40,
            target: '#minPutTime',
            x_accessor: 'totalTime',
            y_accessor: 'minPutTime',
        });
        //plot max get time
        MG.data_graphic({
            title: "MaxGetTime",
            description: "This graphic shows Max Get Time execution over time",
            data: maxGetGraph,
            missing_is_zero: true,
            width: 500,
            height: 280,
            right: 40,
            target: '#maxGetTime',
            x_accessor: 'totalTime',
            y_accessor: 'maxGetTime',
        });
        //plot min get time
        MG.data_graphic({
            title: "MinGetTime",
            description: "This graphic shows Min Get Time execution over time",
            data: minGetGraph,
            missing_is_zero: true,
            width: 500,
            height: 280,
            right: 40,
            target: '#minGetTime',
            x_accessor: 'totalTime',
            y_accessor: 'minGetTime',
        });
        //plot current entries for each process
        MG.data_graphic({
            title: "Current Entries",
            description: "This graphic shows Current Entries over time",
            data: entryGraph,
            missing_is_zero: true,
            width: 800,
            height: 400,
            right: 40,
            target: '#currentEntries',
            x_accessor: 'totalTime',
            y_accessor: 'currentEntries',
        });
    };

    //pialla all the db
    self.clearDb = function () {
        self.state.clearing = true;
        $timeout(function () {
            $http.get('http://localhost:9000/api/clearDb/').then(function (response) {
                console.log("Clear Database");
            }, function () {
                console.error("errore di chiamata");
            }).finally(function () {
                console.log("Database piallato oh yeah")
                self.state.clearing = false;
            });
        }, 2000);
    };

    //launch count
    //launch a test query during test phase
    self.launchCount = function () {
        self.state.launchingCount = true;
        console.log('launchCount function invoked');
        $timeout(function () {
            $http.get('http://localhost:9000/api/launchCount/').then(function (response) {
                console.log("Count launched");
                console.log("Count content", response.data);
                self.countData = response.data;
            }, function () {
                //handle error
                console.error("Impossibile eseguire launchCount()");
            }).finally(function () {
                self.counters.launchCountCounter++;
                self.state.launchingCount = false;
            });
        }, 2000);

    };

    //launch complex
    self.launchComplex = function () {
        self.state.launchingComplex = true;
        console.log('launchComplex function invoked');
        $timeout(function () {
            $http.get('http://localhost:9000/api/launchComplex/').then(function (response) {
                console.log("Complex launched");
                console.log("Complex content", response.data);
                self.complexData = response.data;
            }, function () {
                //handle error
                console.error("Impossibile eseguire launchComplex()");
            }).finally(function () {
                self.counters.launchComplexCounter++;
                self.state.launchingComplex = false;
            });
        }, 2000);

    };

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
});
