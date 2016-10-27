package it.tai.services;

import it.tai.domain.Elaboration;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * Created by miche on 27/10/2016.
 */
@Service
public class MockLoadService implements LoadService {

    private Elaboration mockElaboration = null;

    private ExecutorService executor;

    @Override
    public Optional<Elaboration> getCurrentElaboration() {
        return Optional.ofNullable(mockElaboration);
    }

    @Override
    public Optional<Elaboration> startElaboration(long numOfEntries, int parallelism, int elaborationTypes) {


        if (null == mockElaboration) {
            long now = System.currentTimeMillis();
            mockElaboration = new Elaboration();
            mockElaboration.setTotalNoOfEntries(numOfEntries);
            mockElaboration.setStartTime(now);

            if (null != executor) {
                executor.shutdownNow();
                executor = Executors.newFixedThreadPool(parallelism);
            }

            IntStream.range(0, parallelism).forEach(idx -> {
                Elaboration subtask = new Elaboration();
                subtask.setTotalNoOfEntries(numOfEntries/parallelism);
                subtask.setStartTime(now);
                mockElaboration.addTask(subtask);

                executor.execute(() -> {
                    int myEntryCount = 0;
                    while (!Thread.currentThread().isInterrupted() || myEntryCount < subtask.getTotalNoOfEntries()) {

                        long start = System.currentTimeMillis();

                        try {
                            TimeUnit.MILLISECONDS.sleep(30);

                            subtask.setCurrentNoOfEntries(subtask.getCurrentNoOfEntries() + 1);

                            long newTime = start - System.currentTimeMillis();
                            long newAvgGet = (subtask.getAverageEntryPGetTime() * myEntryCount + newTime) / (myEntryCount + 1);
                            long newAvgPut = (subtask.getAverageEntryPutTime() * myEntryCount + newTime) / (myEntryCount + 1);

                            if ((elaborationTypes & ELABORATION_TYPE_GET) > 0) {
                                subtask.setAverageEntryPGetTime(newAvgGet);
                            }
                            if ((elaborationTypes & ELABORATION_TYPE_PUT) > 0) {
                                subtask.setAverageEntryPutTime(newAvgPut);
                            }
                            myEntryCount++;
                        } catch (InterruptedException ex) {
                            // LOgga!
                        }
                    }
                });
            });


        }



        return Optional.ofNullable(mockElaboration);
    }

    @Override
    public Optional<Elaboration> stopElaboration() {
        Elaboration result = mockElaboration;
        if (null != result) {
            // try to mock stop
            mockElaboration = null;
            if (null != executor) {
                executor.shutdownNow();
            }
        }
        return Optional.ofNullable(result);
    }
}
