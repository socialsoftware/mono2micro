package pt.ist.socialsoftware.mono2micro.decompositionOperations.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import pt.ist.socialsoftware.mono2micro.decompositionOperations.domain.DecompositionOperations;

public interface DecompositionOperationsRepository extends MongoRepository<DecompositionOperations, String> {
    DecompositionOperations findByName(String name);

    void deleteByName(String name);

    //@DeleteQuery(value = "{'currentLogOperationDepth': {$gt: ?0}}")
    //void deleteFutureOperations(Long currentLogOperationDepth);
}