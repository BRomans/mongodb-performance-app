package it.tai.domain;

import it.tai.repository.FatturaRepository;
import org.springframework.data.mongodb.core.query.Query;


/**
 * Created by miche on 08/11/2016.
 */
public class QueryElaboration extends Query {

    private long executionsQ0=0;
    private long executionsQ1=0;
    private long executionsQ2=0;
    private long executionTime;
    private String result;

    public QueryElaboration(){

    }

    public QueryElaboration(String body, Integer flag, FatturaRepository repository) {
        System.out.println("Query: " + body);
        switch (flag) {
            case 0:
                this.setExecutionsQ0(getExecutionsQ0()+1);
                System.out.println("Executions:" + this.getExecutionsQ0());
                break;
            case 1:
                repository.findAll();
                this.setExecutionsQ1(getExecutionsQ1()+1);
                System.out.println("Executions:" + this.getExecutionsQ1());
                break;
            case 2:
                long num = repository.count();
                System.out.println("Number of records: " + num);
                this.setExecutionsQ2(getExecutionsQ2()+1);
                System.out.println("Executions:" + this.getExecutionsQ2());
                break;
            case 3:
                repository.save(new Fattura("Michele", "Romani", "Tai Reietti srl", "RMNMHL93R28A470U","00123456789", "via Monviso 16",
                        "Mantova", "MN", "333117685", "46041", "28/10/1993", "mromani",
                        "password", "mromani@tai.it"));
                repository.save(new Fattura("Marco", "Strambelli", "Tai Reietti srl", "MRCSTR93S12A470U","00987654321", "via Schmitd 7",
                        "Trento", "TN", "3333455422", "38122", "12/12/1993", "mstrambelli",
                        "password", "mstrambelli@tai.it"));
                break;
            case 4:
                System.out.println(repository.findByCompanyOrderByFirstNameAsc("Tai Reietti srl"));
                break;
            default:
                System.out.println("4+");
                break;
        }
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

    public void setCounters(int executionsQ0, int executionsQ1, int executionsQ2){
        this.executionsQ0 = executionsQ0;
        this.executionsQ1 = executionsQ1;
        this.executionsQ2 = executionsQ2;
    }

    public long getExecutionsQ0() {
        return executionsQ0;
    }

    public void setExecutionsQ0(long executionsQ0) {
        this.executionsQ0 = executionsQ0;
    }

    public long getExecutionsQ1() {
        return executionsQ1;
    }

    public void setExecutionsQ1(long executionsQ1) {
        this.executionsQ1 = executionsQ1;
    }

    public long getExecutionsQ2() {
        return executionsQ2;
    }

    public void setExecutionsQ2(long executionsQ2) {
        this.executionsQ2 = executionsQ2;
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
