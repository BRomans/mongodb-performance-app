package it.tai.repository;
import it.tai.domain.Fattura;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Created by miche on 25/10/2016.
 */
public interface FatturaRepository extends MongoRepository<Fattura, String> {

    public Fattura findByUsername(String username);
    public List<Fattura> findByCompany(String company);
    public List<Fattura> findByCompanyOrderByFirstNameAsc(String company);
    public Fattura findById(String id);


}