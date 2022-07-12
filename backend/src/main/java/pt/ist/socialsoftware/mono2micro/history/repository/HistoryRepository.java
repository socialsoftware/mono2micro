package pt.ist.socialsoftware.mono2micro.history.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import pt.ist.socialsoftware.mono2micro.history.model.DecompositionLog;

public interface HistoryRepository extends MongoRepository<DecompositionLog, String> {
    DecompositionLog findByName(String name);

    void deleteByName(String name);

    //@DeleteQuery(value = "{'historyDepth': {$gt: ?0}}")
    //void deleteFutureEntries(Long historyDepth);
}