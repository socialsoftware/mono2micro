package pt.ist.socialsoftware.mono2micro.strategy.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import pt.ist.socialsoftware.mono2micro.strategy.domain.AccessesSciPyStrategy;

public interface AccessesSciPyStrategyRepository extends MongoRepository<AccessesSciPyStrategy, String> {
    boolean existsByName(String name);

    AccessesSciPyStrategy findByName(String strategyName);

    @Query(value = "{'name': ?0}", fields = "{'imageName': 1}")
    AccessesSciPyStrategy getDendrogramImage(String strategyName);
}
