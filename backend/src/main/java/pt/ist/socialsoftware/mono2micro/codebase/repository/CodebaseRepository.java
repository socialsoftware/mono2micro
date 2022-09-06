package pt.ist.socialsoftware.mono2micro.codebase.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import pt.ist.socialsoftware.mono2micro.codebase.domain.Codebase;

import java.util.List;

public interface CodebaseRepository extends MongoRepository<Codebase, String> {

    boolean existsByName(String codebaseName);
    Codebase findByName(String codebaseName);

    @Query(value = "{}", fields = "{'_id': 1, 'isEmpty': 1}") // Will only get id and if it contains strategies
    List<Codebase> getCodebases();

    @Query(value = "{'name': ?0}", fields = "{'strategies': 1}") // Will get the id and the strategies
    Codebase getCodebaseStrategies(String codebaseName);
}