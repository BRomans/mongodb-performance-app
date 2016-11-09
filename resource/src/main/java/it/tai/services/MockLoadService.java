package it.tai.services;

import it.tai.domain.Elaboration;
import it.tai.domain.Fattura;
import it.tai.domain.QueryElaboration;
import it.tai.repository.FatturaRepository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;
import java.util.stream.IntStream;

import static io.github.benas.randombeans.api.EnhancedRandom.random;

/**
 * Created by miche on 27/10/2016.
 */
@Service
public class MockLoadService implements LoadService {

    @Autowired
    private FatturaRepository repository;

    private Elaboration mockElaboration = null;

    private QueryElaboration myQuery = null;

    private ExecutorService executor;

    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
    private ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();
    //private Logger log = Logger.getLogger(MockLoadService.class.getName());
    private final static Log LOG = LogFactory.getLog(MockLoadService.class);

    @Override
    public Optional<Elaboration> getCurrentElaboration() {
        LOG.info("getCurrentElaboration");
        Optional<Elaboration> result = Optional.empty();

        readLock.lock();

        try {
            result = Optional.ofNullable(mockElaboration);
        } finally {
            readLock.unlock();
        }

        return result;
    }

    @Override
    public Optional<Elaboration> startElaboration(long numOfEntries, int parallelism, int elaborationTypes) {
        LOG.info("startElaboration: " + numOfEntries + " - " + parallelism + " - " + elaborationTypes);
        writeLock.lock();
        repository.deleteAll();
        try {
            if (null == mockElaboration) {
                long now = System.currentTimeMillis();
                mockElaboration = new Elaboration();
                mockElaboration.setTotalNoOfEntries(numOfEntries);
                mockElaboration.setStartTime(now);

                if (null != executor) {
                    executor.shutdownNow();
                }
                executor = Executors.newFixedThreadPool(parallelism);

                IntStream.range(0, parallelism).forEach(idx -> {
                    Elaboration subtask = new Elaboration();
                    subtask.setTotalNoOfEntries(numOfEntries / parallelism);
                    subtask.setStartTime(now);
                    mockElaboration.addTask(subtask);

                    executor.execute(() -> {
                        LOG.info("executor : " + idx + " => " + subtask);
                        int myEntryCount = 0;
                        int failureCount = 0;
                        long maxPutTime = 0;
                        long minPutTime = 0;
                        long maxGetTime = 0;
                        long minGetTime = 0;
                        long failureRate;
                        Instant startTime = Instant.now();
                        Instant stopTime;
                        while (!Thread.currentThread().isInterrupted() && myEntryCount < subtask.getTotalNoOfEntries()) {
                            long start = System.currentTimeMillis();

                            try {
                                TimeUnit.MILLISECONDS.sleep(0);
                                Fattura fatturaMock = random(Fattura.class);

                                repository.save(fatturaMock);
                                if(!repository.exists(fatturaMock.getId())){
                                    System.out.println("Missing record!");
                                    failureCount++;
                                }

                                if(failureCount > 0) {
                                    failureRate = failureCount / myEntryCount;
                                }else
                                    failureRate = 0;
                                subtask.setCurrentNoOfEntries(subtask.getCurrentNoOfEntries() + 1);
                                subtask.setFailureRate(failureRate);

                                long newTime =  System.currentTimeMillis()- start;
                                long newAvgPut = (subtask.getAverageEntryPutTime() * myEntryCount + newTime) / (myEntryCount + 1);

                                if ((elaborationTypes & ELABORATION_TYPE_GET) > 0) {
                                    repository.findById(fatturaMock.getId());
                                    newTime =  System.currentTimeMillis()- start;
                                    long newAvgGet = (subtask.getAverageEntryPGetTime() * myEntryCount + newTime) / (myEntryCount + 1);
                                    subtask.setAverageEntryPGetTime(newAvgGet);
                                    if(minGetTime == 0){
                                        minGetTime = newTime;
                                        subtask.setMinGetTime(minGetTime);
                                    }
                                    if(maxGetTime < newTime){
                                        maxGetTime = newTime;
                                        subtask.setMaxGetTime(maxGetTime);
                                    }
                                    if(newTime < minGetTime){
                                        minGetTime = newTime;
                                        subtask.setMinGetTime(minGetTime);
                                    }
                                }
                                if ((elaborationTypes & ELABORATION_TYPE_PUT) > 0) {
                                    subtask.setAverageEntryPutTime(newAvgPut);
                                    if(minPutTime == 0){
                                        minPutTime = newTime;
                                        subtask.setMinGetTime(minPutTime);
                                    }
                                    if(maxPutTime < newTime){
                                        maxPutTime = newTime;
                                        subtask.setMaxPutTime(maxPutTime);
                                    }
                                    if(newTime < minPutTime){
                                        minPutTime = newTime;
                                        subtask.setMinPutTime(minPutTime);
                                    }

                                }
                                myEntryCount++;
                            } catch (InterruptedException ex) {
                                LOG.error(ex);
                            }
                            stopTime = Instant.now();
                            subtask.setTotalTime(Duration.between(startTime, stopTime).getSeconds());
                        }
                    });
                });


            }
        } finally {
            writeLock.unlock();
        }


        return Optional.ofNullable(mockElaboration);
    }

    @Override
    public Optional<Elaboration> stopElaboration() {
        LOG.info("stopElaboration");
        writeLock.lock();

        Elaboration result = mockElaboration;
        try {
            if (null != result) {
                // try to mock stop
                mockElaboration = null;
                if (null != executor) {
                    executor.shutdownNow();
                }
            }
        } finally {
            writeLock.unlock();
        }


        return Optional.ofNullable(result);
    }



    @Override
    public QueryElaboration launchQuery(String query, Integer flag){
        Instant startTime = Instant.now();
        Instant stopTime;
        writeLock.lock();
        try {
            myQuery =  new QueryElaboration(query, flag, repository);
            myQuery.executionResultState(true);

        }finally {
            writeLock.unlock();
        }
        stopTime = Instant.now();
        myQuery.setExecutionTime(Duration.between(startTime, stopTime).getSeconds());
        System.out.println("Execution time:" + myQuery.getExecutionTime());
        return myQuery;
    }

    @Override
    public QueryElaboration getQueryElaboration() {
        LOG.info("getQueryElaboration");
        QueryElaboration result;
        readLock.lock();
        try {
            result = myQuery;
        } finally {
            readLock.unlock();
        }

        return result;
    }
}
