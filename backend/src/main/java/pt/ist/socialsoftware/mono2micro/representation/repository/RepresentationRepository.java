package pt.ist.socialsoftware.mono2micro.representation.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import pt.ist.socialsoftware.mono2micro.representation.domain.Representation;

public interface RepresentationRepository extends MongoRepository<Representation, String> {
    void deleteById(String id);
}
