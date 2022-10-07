package pt.ist.socialsoftware.mono2micro.similarity.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import pt.ist.socialsoftware.mono2micro.similarity.domain.Similarity;

public interface SimilarityRepository extends MongoRepository<Similarity, String> {
    boolean existsByName(String name);
    Similarity findByName(String name);
    void deleteByName(String name);
}
