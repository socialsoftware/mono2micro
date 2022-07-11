package pt.ist.socialsoftware.mono2micro.history.repository;

import org.springframework.data.mongodb.repository.DeleteQuery;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import pt.ist.socialsoftware.mono2micro.history.model.DecompositionHistory;

public interface HistoryRepository extends MongoRepository<DecompositionHistory, String> {
    DecompositionHistory findByName(String name);

    void deleteByName(String name);

    //@DeleteQuery(value = "{'historyDepth': {$gt: ?0}}")
    //void deleteFutureEntries(Long historyDepth);
}