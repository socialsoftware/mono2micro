package pt.ist.socialsoftware.mono2micro.strategy.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import pt.ist.socialsoftware.mono2micro.strategy.domain.RecommendAccessesSciPyStrategy;

public interface RecommendAccessesSciPyStrategyRepository  extends MongoRepository<RecommendAccessesSciPyStrategy, String> {
    RecommendAccessesSciPyStrategy findByName(String strategyName);
    @Query(value = "{'name': ?0}", fields = "{'recommendationResultName': 1}")
    RecommendAccessesSciPyStrategy getRecommendationResultName(String strategyName);
}
