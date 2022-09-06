package pt.ist.socialsoftware.mono2micro.source.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import pt.ist.socialsoftware.mono2micro.source.domain.AccessesSource;

public interface AccessesSourceRepository  extends MongoRepository<AccessesSource, String> {
}
