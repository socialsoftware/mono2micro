package pt.ist.socialsoftware.mono2micro.decomposition.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.AccessesSciPyDecomposition;

public interface AccessesSciPyDecompositionRepository extends MongoRepository<AccessesSciPyDecomposition, String> {
    AccessesSciPyDecomposition findByName(String name);
}
