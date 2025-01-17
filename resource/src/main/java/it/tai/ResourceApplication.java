package it.tai;

import io.github.benas.randombeans.api.EnhancedRandom;
import it.tai.domain.Elaboration;
import it.tai.domain.Fattura;
import it.tai.domain.QueryElaboration;
import it.tai.repository.FatturaRepository;
import it.tai.services.LoadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.*;

import static io.github.benas.randombeans.api.EnhancedRandom.random;


//mongodb ip address: 172.16.34.76 pass:123ciuc0
@SpringBootApplication
@RestController
@RequestMapping("/api")
public class ResourceApplication implements CommandLineRunner {

    @Autowired
    private LoadService loadService;

    private static String[] ARGS;

    @RequestMapping("/")
	@CrossOrigin(origins="*", maxAge=3600)
	public Elaboration none() {
        return loadService.getCurrentElaboration().orElse(null);
	}


    @RequestMapping("/refresh")
    @CrossOrigin(origins="*", maxAge=3600)
    public Elaboration getElaboration() {
        return loadService.getCurrentElaboration().orElse(null);
    }

    @RequestMapping("/clearDb")
    @CrossOrigin(origins="*", maxAge=3600)
    public Elaboration clearDb() {
        return loadService.clearCurrentRepository().orElse(null);
    }

    @RequestMapping(value = "/start",method = RequestMethod.POST)
    @CrossOrigin(origins="*", maxAge=2000)
    public Elaboration startElaboration(@RequestBody PostBody post){
        if(post.getNumOfEntries() == null){
            post.setNumOfEntries(Long.valueOf(10000));
        }
        if(post.getParallelism() == null) {
            post.setParallelism(2);
        }
        if(post.getElaborationTypes() == null){
            post.setElaborationTypes(2);
        }
        return loadService.startElaboration(post.getNumOfEntries(), post.getParallelism(), post.getElaborationTypes()).orElse(null);
    }

    @RequestMapping(value = "/launchCount")
    @CrossOrigin(origins="*", maxAge=3600)
    public QueryElaboration launchCount(){
        return loadService.launchCountQuery().orElse(null);
    }

    @RequestMapping(value = "/launchComplex")
    @CrossOrigin(origins="*", maxAge=3600)
    public QueryElaboration launchComplex(){
        return loadService.launchComplexQuery().orElse(null);
    }

    @RequestMapping(value = "/launch",method = RequestMethod.POST)
    @CrossOrigin(origins="*", maxAge=3600)
    public QueryElaboration launchQuery(@RequestBody PostBody post){
        if(post.getQuery() == null){
            post.setQuery("No query inserted");
        }
        if(post.getFlag() == null){
            post.setFlag(0);
        }
        loadService.launchQuery(post.getQuery(), post.getFlag());

        return loadService.getQueryElaboration();
    }

    @RequestMapping(value = "/stop",method = RequestMethod.POST)
    @CrossOrigin(origins="*", maxAge=3600)
    public Elaboration stopElaboration() {
        return loadService.stopElaboration().orElse(null);
    }

	public static void main(String[] args) {
        SpringApplication.run(ResourceApplication.class, args);

	}

    @Override
    public void run(String... args) throws Exception {
        ARGS = args;
        if(ARGS.length > 0){
            if(ARGS[0].equals("Y") && ARGS.length==1){
                System.out.println("Resource server launched with command line options");
                Boolean exit = false;
                Scanner scan = new Scanner(System.in);
                String option;
                Integer parallelism = 0;
                while(!exit){
                    System.out.println("Choose an option: \n1)Start load test\n2)Clear Database \n3)Stop Current Elaboration \n4)Check Metrics \n5)Exit");
                    option = scan.next();
                    switch(option){
                        case "1":
                            System.out.println("Select options to start load test");
                            System.out.println("How many entries?");
                            Integer entries = Integer.valueOf(scan.next());
                            System.out.println("How many threads?");
                            parallelism = Integer.valueOf(scan.next());
                            System.out.println("Elaboration type?(PUT=1, GET=2, PUT&GET=3)");
                            Integer elaborationType = Integer.valueOf(scan.next());
                            loadService.startElaboration(entries, parallelism,elaborationType);
                            break;
                        case "2":
                            System.out.println("Clearing Database...");
                            loadService.clearCurrentRepository();
                            break;
                        case "3":
                            loadService.stopElaboration();
                            break;
                        case "4":
                            System.out.println("Not yet implemented");
                            /*for(int i=0; i<= parallelism; i++) {
                                System.out.println("Thread: " + loadService.getCurrentElaboration().get().getParallelTasks().iterator());
                                System.out.println("Average Put Time: " + loadService.getCurrentElaboration().get().getAverageEntryPutTime());
                                System.out.println("Average Get Time: " + loadService.getCurrentElaboration().get().getAverageEntryPGetTime());
                                System.out.println("Max Put Time: " + loadService.getCurrentElaboration().get().getMaxPutTime());
                                System.out.println("Min Put Time: " + loadService.getCurrentElaboration().get().getMinPutTime());
                                System.out.println("Max Get Time: " + loadService.getCurrentElaboration().get().getMaxGetTime());
                                System.out.println("Min Put Time: " + loadService.getCurrentElaboration().get().getMinGetTime());
                            }*/
                            //Optional<Elaboration> data = loadService.getCurrentElaboration();
                            break;
                        case "5":
                            System.out.println("Exit Resource Application? (Y/N)");
                            option = scan.next();
                            if(option.equals("Y") || option.equals("y")){
                                exit = true;
                                System.exit(0);
                            }
                            break;
                        default:
                            System.out.println("No option has been selected!");
                            break;
                    }
                }


            }
        }else{
            System.out.println("No command line arguments");
        }
    }
}
class PostBody{
    private Long numOfEntries;
    private Integer parallelism;
    private Integer elaborationTypes;
    private String query;


    private Integer flag;

    public Long getNumOfEntries() {
        return numOfEntries;
    }

    public void setNumOfEntries(Long numOfEntries) {
        this.numOfEntries = numOfEntries;
    }

    public Integer getParallelism() {
        return parallelism;
    }

    public void setParallelism(Integer parallelism) {
        this.parallelism = parallelism;
    }

    public Integer getElaborationTypes() {
        return elaborationTypes;
    }

    public void setElaborationTypes(Integer elaborationTypes) {
        this.elaborationTypes = elaborationTypes;
    }
    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public Integer getFlag() {
        return flag;
    }

    public void setFlag(Integer flag) {
        this.flag = flag;
    }


    PostBody(){}

    PostBody(String query){
        this.query = query;
    }

    PostBody(String query, Integer flag){
        this.query = query;
        this.flag = flag;
    }

    PostBody(Long numOfEntries, Integer parallelism, Integer elaborationTypes){
        this.numOfEntries = numOfEntries;
        this.parallelism = parallelism;
        this.elaborationTypes = elaborationTypes;
    }

}
class Message {
    private String id = UUID.randomUUID().toString();
    private String content;


    Message() {}

    public Message(String content) {
        this.content = content;
    }

    public String getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

}
