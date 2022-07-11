package pt.ist.socialsoftware.mono2micro.decomposition.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.AccessesSciPyDecomposition;

public interface AccessesSciPyDecompositionRepository extends MongoRepository<AccessesSciPyDecomposition, String> {
    AccessesSciPyDecomposition findByName(String name);

    @Query(value = "{'name': ?0}", fields = "{'functionalities.functionalityRedesigns': 0}")
    AccessesSciPyDecomposition findByNameWithoutFunctionalityRedesigns(String name);
}
