package pt.ist.socialsoftware.mono2micro.dendrogram.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import pt.ist.socialsoftware.mono2micro.dendrogram.domain.AccessesSciPyDendrogram;

public interface AccessesSciPyDendrogramRepository extends MongoRepository<AccessesSciPyDendrogram, String> {
    boolean existsByName(String name);

    AccessesSciPyDendrogram findByName(String dendrogramName);

    @Query(value = "{'name': ?0}", fields = "{'imageName': 1}")
    AccessesSciPyDendrogram getDendrogramImage(String dendrogramName);
}
