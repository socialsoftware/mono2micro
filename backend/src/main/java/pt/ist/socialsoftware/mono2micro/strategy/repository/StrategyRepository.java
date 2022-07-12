package pt.ist.socialsoftware.mono2micro.strategy.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import pt.ist.socialsoftware.mono2micro.strategy.domain.Strategy;

public interface StrategyRepository extends MongoRepository<Strategy, String> {
    boolean existsByName(String name);
    Strategy findByName(String name);

    void deleteByName(String name);
}
