package pt.ulisboa.tecnico.socialsoftware.tutor.question.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
@pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.NoRepositoryBean
public interface MiddleMan<Q, M> extends org.springframework.data.jpa.repository.JpaRepository<Q, java.lang.Long> {
    @org.springframework.data.jpa.repository.Query(value = "SELECT * FROM questions q WHERE q.key = :key", nativeQuery = true)
    java.util.Optional<pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question> findByKey(java.lang.Integer key);
}