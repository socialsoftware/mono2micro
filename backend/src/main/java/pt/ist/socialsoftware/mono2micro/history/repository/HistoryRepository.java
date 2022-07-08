package pt.ist.socialsoftware.mono2micro.history.repository;

import org.springframework.data.mongodb.repository.DeleteQuery;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import pt.ist.socialsoftware.mono2micro.history.model.DecompositionHistory;

public interface HistoryRepository extends MongoRepository<DecompositionHistory, String> {
    @Query("{'codebaseName': ?0, 'strategyName': ?1, 'decompositionName': ?2}")
    DecompositionHistory findByDecomposition(String codebaseName, String strategyName, String decomposition);

    @DeleteQuery("{'codebaseName': ?0}")
    void deleteByCodebase(String codebaseName);

    @DeleteQuery("{'codebaseName': ?0, 'strategyName': ?1}")
    void deleteByStrategy(String codebaseName, String strategyName);

    @DeleteQuery("{'codebaseName': ?0, 'strategyName': ?1, 'decompositionName': ?2}")
    void deleteByDecomposition(String codebaseName, String strategyName, String decompositionName);

    //@DeleteQuery(value = "{'historyDepth': {$gt: ?0}}")
    //void deleteFutureEntries(Long historyDepth);
}