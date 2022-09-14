package pt.ist.socialsoftware.mono2micro.representation.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import pt.ist.socialsoftware.mono2micro.representation.domain.AccessesRepresentation;

public interface AccessesRepresentationRepository extends MongoRepository<AccessesRepresentation, String> {
}
