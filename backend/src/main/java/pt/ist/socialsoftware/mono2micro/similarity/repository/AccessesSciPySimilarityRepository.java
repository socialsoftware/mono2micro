package pt.ist.socialsoftware.mono2micro.similarity.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import pt.ist.socialsoftware.mono2micro.similarity.domain.AccessesSciPySimilarity;

public interface AccessesSciPySimilarityRepository extends MongoRepository<AccessesSciPySimilarity, String> {
    boolean existsByName(String name);

    AccessesSciPySimilarity findByName(String similarityName);

    @Query(value = "{'name': ?0}", fields = "{'dendrogramName': 1}")
    AccessesSciPySimilarity getDendrogramImage(String dendrogramName);
}
