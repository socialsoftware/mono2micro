package pt.ist.socialsoftware.mono2micro.history.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import pt.ist.socialsoftware.mono2micro.history.domain.History;

public interface HistoryRepository extends MongoRepository<History, String> {
    History findByName(String name);

    void deleteByName(String name);
}