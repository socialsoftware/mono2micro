package pt.ist.socialsoftware.mono2micro.recommendation.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import pt.ist.socialsoftware.mono2micro.recommendation.domain.Recommendation;

public interface RecommendationRepository extends MongoRepository<Recommendation, String> {
}
