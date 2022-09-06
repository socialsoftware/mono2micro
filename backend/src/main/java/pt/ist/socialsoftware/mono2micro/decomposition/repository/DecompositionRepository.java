package pt.ist.socialsoftware.mono2micro.decomposition.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;

public interface DecompositionRepository extends MongoRepository<Decomposition, String> {
    Decomposition findByName(String name);

    void deleteByName(String name);
}
