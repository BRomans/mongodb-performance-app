package it.tai.domain;

import it.tai.repository.FatturaRepository;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;


import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static org.springframework.data.mongodb.core.WriteResultChecking.LOG;


/**
 * Created by miche on 08/11/2016.
 */
public class QueryElaboration extends Query {

    private long executionTime;
    private long totalTime;
    private String result;

    private Long count;

    public QueryElaboration(long executionTime, Long count){
        this.executionTime = executionTime;
        this.count = count;
    }


    public void countAll(FatturaRepository repository){
        this.setCount(repository.count());
        System.out.println("Number of records: " + this.getCount());
    }

    public void complexQuery(FatturaRepository repository){

       /* System.out.println(repository.findByCompany("ww"));
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").lt(System.currentTimeMillis()).gt(System.currentTimeMillis()-50));*/
        Long oIndex = ThreadLocalRandom.current().nextLong(1, 2999900);
        Long eIndex = oIndex+100;
        Long counter = Long.valueOf(0);
        for (Fattura fattura : repository.findByRIndexBetween(oIndex, eIndex)) {
            counter++;
        }
        this.setCount(counter);
        System.out.println("Query records retrieved: " + counter);

    }

    public long getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(long executionTime) {

        this.executionTime = executionTime;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public long getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(long totalTime) {
        this.totalTime = totalTime;
    }

    public void executionResultState(Boolean val) {
        if (val == true) {
            this.result = "Query executed with success";
            System.out.println(result);
        } else {
            this.result = "Query failed execution";
            System.out.println(result);
        }
    }
}
