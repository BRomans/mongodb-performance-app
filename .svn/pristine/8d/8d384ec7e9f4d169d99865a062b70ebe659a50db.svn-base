package it.tai.domain;

import it.tai.repository.FatturaRepository;
import org.springframework.data.mongodb.core.query.Query;

import static org.springframework.data.mongodb.core.WriteResultChecking.LOG;


/**
 * Created by miche on 08/11/2016.
 */
public class QueryElaboration extends Query {

    private long executionTime;
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

    public void saveOne(Elaboration repository){

    }

    public void findOne(Elaboration repository){

    }

    public void complexQuery(FatturaRepository repository){
        System.out.println(repository.findByCompany("ww"));

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
