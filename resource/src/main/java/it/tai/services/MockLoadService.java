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
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
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

    private QueryElaboration countQuery = new QueryElaboration(0L, 0L);

    private QueryElaboration complexQuery = new QueryElaboration(0L, 0L);

    private QueryElaboration myQuery = null;

    private ExecutorService executor;

    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
    private ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();
    //private Logger log = Logger.getLogger(MockLoadService.class.getName());
    private final static Log LOG = LogFactory.getLog(MockLoadService.class);

    @Override
    public Optional<Elaboration> getCurrentElaboration() {
        //LOG.info("getCurrentElaboration");
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
                                Fattura fatturaMock = random(Fattura.class, "id", "rIndex");
                                Long rIndex = ThreadLocalRandom.current().nextLong(1, 3000000  );
                                fatturaMock.setrIndex(rIndex);
                                repository.insert(fatturaMock);


                                if (failureCount > 0) {
                                    failureRate = failureCount / myEntryCount;
                                } else {
                                    failureRate = 0;
                                }
                                subtask.setCurrentNoOfEntries(myEntryCount + 1);
                                subtask.setFailureRate(failureRate);

                                long newPutTime = System.currentTimeMillis() - start;
                                long newAvgPut = (subtask.getAverageEntryPutTime() * myEntryCount + newPutTime) / (myEntryCount + 1);

                                if ((elaborationTypes & ELABORATION_TYPE_GET) > 0) {
                                    repository.findById(fatturaMock.getId());
                                    long newGetTime = System.currentTimeMillis() - start;
                                    long newAvgGet = (subtask.getAverageEntryPGetTime() * myEntryCount + newGetTime) / (myEntryCount + 1);
                                    subtask.setAverageEntryPGetTime(newAvgGet);
                                    if (minGetTime == 0) {
                                        minGetTime = newGetTime;
                                        subtask.setMinGetTime(minGetTime);
                                    }
                                    if (maxGetTime < newGetTime) {
                                        maxGetTime = newGetTime;
                                        subtask.setMaxGetTime(maxGetTime);
                                    }
                                    if (newGetTime < minGetTime) {
                                        minGetTime = newGetTime;
                                        subtask.setMinGetTime(minGetTime);
                                    }
                                }
                                if ((elaborationTypes & ELABORATION_TYPE_PUT) > 0) {
                                    subtask.setAverageEntryPutTime(newAvgPut);
                                    if (minPutTime == 0) {
                                        minPutTime = newPutTime;
                                        subtask.setMinGetTime(minPutTime);
                                    }
                                    if (maxPutTime < newPutTime) {
                                        maxPutTime = newPutTime;
                                        subtask.setMaxPutTime(maxPutTime);
                                    }
                                    if (newPutTime < minPutTime) {
                                        minPutTime = newPutTime;
                                        subtask.setMinPutTime(minPutTime);
                                    }

                                }

                            } catch (Exception ex) {
                                LOG.error(ex);
                                failureCount++;
                            } finally {
                                myEntryCount++;
                            }

                            stopTime = Instant.now();
                            subtask.setTotalTime(Duration.between(startTime, stopTime).getSeconds());
                            complexQuery.setTotalTime(Duration.between(startTime, stopTime).getSeconds());
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
    public Optional<Elaboration> clearCurrentRepository() {
        LOG.info("clearCurrentRepository");
        writeLock.lock();
        Elaboration result = mockElaboration;
        try {
            repository.deleteAll();
            LOG.info("Count: " + repository.count());
        } finally {
            writeLock.unlock();
        }

        return Optional.ofNullable(result);
    }

    @Override
    public Optional<QueryElaboration> launchCountQuery() {
        LOG.info("countAll launched");
        Instant startTime = Instant.now();
        Instant stopTime;
        writeLock.lock();
        QueryElaboration result = countQuery;
        try {
            if (null != result) {
                result.countAll(repository);
                stopTime = Instant.now();
                result.setExecutionTime(Duration.between(startTime, stopTime).toMillis());
            }
        } finally {
            writeLock.unlock();
        }

        return Optional.ofNullable(result);
    }

    @Override
    public Optional<QueryElaboration> launchComplexQuery() {
        LOG.info("complex Query launched");
        Instant startTime = Instant.now();
        Instant stopTime;
        writeLock.lock();
        QueryElaboration result = countQuery;
        try {
            if (null != result) {
                result.complexQuery(repository);
                stopTime = Instant.now();
                result.setExecutionTime(Duration.between(startTime, stopTime).toMillis());
            }
        } finally {
            writeLock.unlock();
        }

        return Optional.ofNullable(result);
    }

    @Override
    public QueryElaboration launchQuery(String query, Integer flag) {
        return null;
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
