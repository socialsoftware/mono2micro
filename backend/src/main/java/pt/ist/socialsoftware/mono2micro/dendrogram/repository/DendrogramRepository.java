package pt.ist.socialsoftware.mono2micro.dendrogram.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import pt.ist.socialsoftware.mono2micro.dendrogram.domain.Dendrogram;

public interface DendrogramRepository extends MongoRepository<Dendrogram, String> {
    boolean existsByName(String name);
    Dendrogram findByName(String name);

    void deleteByName(String name);
}
