package pt.ist.socialsoftware.mono2micro.recommendation.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import pt.ist.socialsoftware.mono2micro.recommendation.domain.RecommendAccessesSciPy;
import pt.ist.socialsoftware.mono2micro.recommendation.domain.Recommendation;

public interface RecommendationRepository extends MongoRepository<Recommendation, String> {
    Recommendation findByName(String recommendationName);
    @Query(value = "{'name': ?0}", fields = "{'recommendationResultName': 1, '_id': 1, '_class': 1}")
    Recommendation getRecommendationResultName(String recommendationName);
}
