package pt.ist.socialsoftware.mono2micro.functionality;

import org.springframework.data.mongodb.repository.MongoRepository;
import pt.ist.socialsoftware.mono2micro.functionality.domain.Functionality;

public interface FunctionalityRepository extends MongoRepository<Functionality, String> {
}
