package pt.ist.socialsoftware.mono2micro.recommendation.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import pt.ist.socialsoftware.mono2micro.recommendation.domain.RecommendAccessesSciPy;

public interface RecommendAccessesSciPyRepository extends MongoRepository<RecommendAccessesSciPy, String> {
    boolean existsByName(String name);
    RecommendAccessesSciPy findByName(String recommendationName);
    @Query(value = "{'name': ?0}", fields = "{'recommendationResultName': 1}")
    RecommendAccessesSciPy getRecommendationResultName(String recommendationName);
}
