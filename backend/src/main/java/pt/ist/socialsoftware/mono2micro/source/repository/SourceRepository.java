package pt.ist.socialsoftware.mono2micro.source.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import pt.ist.socialsoftware.mono2micro.source.domain.Source;

public interface SourceRepository extends MongoRepository<Source, String> {
    void deleteById(String id);
}
