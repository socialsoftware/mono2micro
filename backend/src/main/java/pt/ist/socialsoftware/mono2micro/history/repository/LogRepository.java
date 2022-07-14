package pt.ist.socialsoftware.mono2micro.history.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import pt.ist.socialsoftware.mono2micro.history.model.DecompositionLog;

public interface LogRepository extends MongoRepository<DecompositionLog, String> {
    DecompositionLog findByName(String name);

    void deleteByName(String name);

    //@DeleteQuery(value = "{'currentLogOperationDepth': {$gt: ?0}}")
    //void deleteFutureOperations(Long currentLogOperationDepth);
}