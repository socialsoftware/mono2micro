package pt.ist.socialsoftware.mono2micro.log.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import pt.ist.socialsoftware.mono2micro.log.domain.Log;

public interface LogRepository extends MongoRepository<Log, String> {
    Log findByName(String name);

    void deleteByName(String name);

    //@DeleteQuery(value = "{'currentLogOperationDepth': {$gt: ?0}}")
    //void deleteFutureOperations(Long currentLogOperationDepth);
}