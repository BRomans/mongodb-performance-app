package it.tai.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Created by miche on 27/10/2016.
 */
public class Elaboration {

    private long startTime;
    private long totalNoOfEntries;
    private long currentNoOfEntries;

    private long averageEntryPutTime;
    private long averageEntryPGetTime;
    private long totalNoOfRejectedPut;
    private long totalNoOfRejectedGet;

    private Collection<Elaboration> parallelTasks = new ArrayList<>();

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getTotalNoOfEntries() {
        return totalNoOfEntries;
    }

    public void setTotalNoOfEntries(long totalNoOfEntries) {
        this.totalNoOfEntries = totalNoOfEntries;
    }

    public long getCurrentNoOfEntries() {
        return currentNoOfEntries;
    }

    public void setCurrentNoOfEntries(long currentNoOfEntries) {
        this.currentNoOfEntries = currentNoOfEntries;
    }

    public long getAverageEntryPutTime() {
        return averageEntryPutTime;
    }

    public void setAverageEntryPutTime(long averageEntryPutTime) {
        this.averageEntryPutTime = averageEntryPutTime;
    }

    public long getAverageEntryPGetTime() {
        return averageEntryPGetTime;
    }

    public void setAverageEntryPGetTime(long averageEntryPGetTime) {
        this.averageEntryPGetTime = averageEntryPGetTime;
    }

    public long getTotalNoOfRejectedPut() {
        return totalNoOfRejectedPut;
    }

    public void setTotalNoOfRejectedPut(long totalNoOfRejectedPut) {
        this.totalNoOfRejectedPut = totalNoOfRejectedPut;
    }

    public long getTotalNoOfRejectedGet() {
        return totalNoOfRejectedGet;
    }

    public void setTotalNoOfRejectedGet(long totalNoOfRejectedGet) {
        this.totalNoOfRejectedGet = totalNoOfRejectedGet;
    }

    public Collection<Elaboration> getParallelTasks() {
        return Collections.unmodifiableCollection(parallelTasks);
    }

    public void addTask(Elaboration subTask) {
        parallelTasks.add(subTask);
    }



}
