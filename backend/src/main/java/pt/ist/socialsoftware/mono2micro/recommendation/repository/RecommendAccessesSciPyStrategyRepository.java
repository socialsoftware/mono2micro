package pt.ist.socialsoftware.mono2micro.recommendation.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import pt.ist.socialsoftware.mono2micro.recommendation.domain.RecommendAccessesSciPy;

public interface RecommendAccessesSciPyStrategyRepository  extends MongoRepository<RecommendAccessesSciPy, String> {
    RecommendAccessesSciPy findByName(String strategyName);
    @Query(value = "{'name': ?0}", fields = "{'recommendationResultName': 1}")
    RecommendAccessesSciPy getRecommendationResultName(String strategyName);
}
