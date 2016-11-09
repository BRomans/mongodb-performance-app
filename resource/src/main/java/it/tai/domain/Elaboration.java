package it.tai.domain;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Created by miche on 27/10/2016.
 */
public class Elaboration {

    private long startTime;
    private long totalTime;

    private long totalNoOfEntries;
    private long currentNoOfEntries;

    private long averageEntryPutTime;
    private long averageEntryPGetTime;
    private long totalNoOfRejectedPut;
    private long totalNoOfRejectedGet;

    private long maxGetTime;
    private long minGetTime;

    private long maxPutTime;
    private long minPutTime;

    private long failureRate;


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

    public long getTotalTime() { return totalTime; }

    public void setTotalTime(long totalTime) {
        this.totalTime = totalTime;
    }

    public long getMaxGetTime() {
        return maxGetTime;
    }

    public void setMaxGetTime(long maxGetTime) {
        this.maxGetTime = maxGetTime;
    }

    public long getMinGetTime() {
        return minGetTime;
    }

    public void setMinGetTime(long minGetTime) {
        this.minGetTime = minGetTime;
    }

    public long getMaxPutTime() {
        return maxPutTime;
    }

    public void setMaxPutTime(long maxPutTime) {
        this.maxPutTime = maxPutTime;
    }

    public long getMinPutTime() {
        return minPutTime;
    }

    public void setMinPutTime(long minPutTime) {
        this.minPutTime = minPutTime;
    }

    public long getFailureRate() {
        return failureRate;
    }

    public void setFailureRate(long failureRate) {
        this.failureRate = failureRate;
    }

    public Collection<Elaboration> getParallelTasks() {
        return Collections.unmodifiableCollection(parallelTasks);
    }

    public void addTask(Elaboration subTask) {
        parallelTasks.add(subTask);
    }




}
