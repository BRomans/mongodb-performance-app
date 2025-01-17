/**
 * Created by miche on 25/10/2016.
 */
angular.module('mongodb', []).controller('mongodb', function ($http, $timeout, $interval) {
    var self = this;
    self.address = 'localhost:9000';
    self.elaborationType = 3;
    self.autoCheck = 2;
    self.queryFlag = 2;
    var promiseFetch;
    var promiseQuery;
    var promiseDraw;
    var maxPutGraph;
    var minPutGraph;
    var maxGetGraph;
    var minGetGraph;
    var entryGraph;
    var lastExecTimeGraph = [];
    var counterAuto = 0;

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
        launchingQueries: false,
        drawingGraphs: false,
    };

    self.moreless = {
        more: true,
        less: false,

    };

    //retrieve data from resource server to set up GUI on launch
    $http.get('http://' + self.address + '/api/').then(function (response) {
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

    //retrieve data from resource server to set up GUI on launch
    self.executionState = function () {
        $http.get('http://' + self.address + '/api/').then(function (response) {
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
    };

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
                self.state.checking = false;
            }, 2000);
        }
        if (autoCheck == 1) {
            console.log(self.state.checking);
            promiseFetch = $interval(function () {
                self.fetchData();
                console.log("Refreshed again");
            }, 1000);
        }

    };

    //stop refresh() function if ON option is active
    self.stopAutoCheck = function () {
        $interval.cancel(promiseFetch);
        self.state.checking = false;
        console.log(self.state.checking);
    };

    //retrieve data from resource server
    self.fetchData = function () {
        $http.get('http://' + self.address + '/api/refresh/').then(function (response) {
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
            $http.post('http://' + self.address + '/api/start/', data)
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
        self.updateGraph();

        var data = {};
        var config = {
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded;charset=utf-8;'
            }
        };
        $timeout(function () {
            $http.post('http://' + self.address + '/api/stop/', data, config)
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
        lastExecTimeGraph = [];
    };

    //init to null value all graphs
    self.deinitGraphs = function () {
        maxPutGraph = null;
        minPutGraph = null;
        maxGetGraph = null;
        minGetGraph = null;
        entryGraph = null;
        lastExecTimeGraph = null;
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
            legend: ['thread[0]', 'thread[1]', 'thread[2]', 'thread[3]', 'thread[4]', 'thread[5]'],
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
            legend: ['thread[0]', 'thread[1]', 'thread[2]', 'thread[3]', 'thread[4]', 'thread[5]'],
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
            legend: ['thread[0]', 'thread[1]', 'thread[2]', 'thread[3]', 'thread[4]', 'thread[5]'],
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
            legend: ['thread[0]', 'thread[1]', 'thread[2]', 'thread[3]', 'thread[4]', 'thread[5]'],
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
            legend: ['thread[0]', 'thread[1]', 'thread[2]', 'thread[3]', 'thread[4]', 'thread[5]'],
            target: '#currentEntries',
            x_accessor: 'totalTime',
            y_accessor: 'currentEntries',
        });
        //plot query last execution times
       /* MG.data_graphic({
            title: "Execution time",
            description: "This graphic shows complex query execution time over time",
            data: lastExecTimeGraph,
            missing_is_zero: true,
            width: 800,
            height: 400,
            right: 40,
            target: '#lastExecTime',
            x_accessor: 'launchComplexCounter',
            y_accessor: 'executionTime',
        });*/
    };

    //draw all graphs using existing data
    self.drawGraphs = function () {
        self.state.drawingGraphs = true;
        $timeout(function () {
            self.updateGraph();
            console.log("Drawing Graphs");
            self.state.drawingGraphs = false;
        }, 2000);
    };

    //start drawing all graphs using existing data and updating them every t time
    self.autoDrawGraphs = function () {
        self.state.drawingGraphs = true;
        promiseDraw = $interval(function () {
            console.log("Auto Drawing Graphs");
            self.updateGraph();
        }, 1000);
    };

    //stop the automatic drawing of the graphs
    self.stopDraw = function () {
        $interval.cancel(promiseDraw);
        self.state.drawingGraphs = false;
    };

    //pialla all the db
    self.clearDb = function () {
        self.state.clearing = true;
        $timeout(function () {
            $http.get('http://' + self.address + '/api/clearDb/').then(function (response) {
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
    self.launchCount = function () {
        self.state.launchingCount = true;
        console.log('launchCount function invoked');
        $timeout(function () {
            $http.get('http://' + self.address + '/api/launchCount/').then(function (response) {
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
            $http.get('http://' + self.address + '/api/launchComplex/').then(function (response) {
                console.log("Complex launched");
                console.log("Complex content", response.data);
                self.complexData = response.data;
                var lastExecTime = self.complexData.executionTime;
                lastExecTimeGraph.push(lastExecTime);
                console.log("Execution time vector: " + lastExecTimeGraph);
                /*var lastExecTime = {
                    'launchComplexCounter' : self.launchComplexCounter,
                    'executionTime' : self.complexData.executionTime,
                };
                if (lastExecTimeGraph[self.launchComplexCounter] == null) {
                    lastExecTimeGraph[self.launchComplexCounter] = [];
                }
                lastExecTimeGraph[self.launchComplexCounter].push(lastExecTime);
                for(var i=0; i < lastExecTimeGraph.size; i++) {
                    console.log("Execution time vector["+ i + "]: " + lastExecTimeGraph[i]);
                }*/
            }, function () {
                //handle error
                console.error("Impossibile eseguire launchComplex()");
            }).finally(function () {
                self.counters.launchComplexCounter++;
                self.state.launchingComplex = false;
            });
        }, 2000);

    };

    self.launchQuery = function () {
        self.state.launchingQueries = true;
        console.log("Launching Queries - Auto:false");
        $timeout(function () {
            self.launchCount();
            self.launchComplex();
            self.state.launchingQueries = false;
        }, 2000);
    };

    self.launchAutoQuery = function () {
        self.state.launchingQueries = true;
        promiseQuery = $interval(function () {
            console.log("Launching Queries - Auto:true");
            console.log(self.state.launchingQueries);
            counterAuto++;
            if(counterAuto <= 750){
                self.launchComplex();
            }else{
                self.stopAutoLaunchQuery();
            }
        }, 8000);

    };

    self.stopAutoLaunchQuery = function () {
        self.state.launchingQueries = false;
        $interval.cancel(promiseQuery);
        counterAuto = 0;
        console.log(self.state.launchingQueries);
    };
});
