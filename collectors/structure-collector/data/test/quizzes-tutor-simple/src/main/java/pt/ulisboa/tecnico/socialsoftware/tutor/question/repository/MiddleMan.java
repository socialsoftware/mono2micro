package pt.ulisboa.tecnico.socialsoftware.tutor.question.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question;

import java.util.List;
import java.util.Optional;

@NoRepositoryBean
public interface MiddleMan<Q, M> extends JpaRepository<Q, Long> {
    @Query(value = "SELECT * FROM questions q WHERE q.key = :key", nativeQuery = true)
    Optional<Question> findByKey(Integer key);
}