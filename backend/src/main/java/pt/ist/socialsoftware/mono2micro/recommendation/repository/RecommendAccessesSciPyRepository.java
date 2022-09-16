package pt.ist.socialsoftware.mono2micro.recommendation.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import pt.ist.socialsoftware.mono2micro.recommendation.domain.RecommendAccessesSciPy;

public interface RecommendAccessesSciPyRepository extends MongoRepository<RecommendAccessesSciPy, String> {
    boolean existsByName(String name);
}
