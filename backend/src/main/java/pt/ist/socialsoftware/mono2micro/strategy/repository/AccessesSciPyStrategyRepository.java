package pt.ist.socialsoftware.mono2micro.strategy.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import pt.ist.socialsoftware.mono2micro.strategy.domain.AccessesSciPyStrategy;

public interface AccessesSciPyStrategyRepository extends MongoRepository<AccessesSciPyStrategy, String> {
    AccessesSciPyStrategy findByName(String name);
}
