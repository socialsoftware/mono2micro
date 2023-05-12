package pt.ulisboa.tecnico.socialsoftware.tutor.quiz.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
@org.springframework.stereotype.Repository
@org.springframework.transaction.annotation.Transactional
public interface QuizRepository extends org.springframework.data.jpa.repository.JpaRepository<pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz, java.lang.Integer> {
    @org.springframework.data.jpa.repository.Query(value = "SELECT * FROM quizzes q, course_executions c WHERE c.id = q.course_execution_id AND c.id = :executionId", nativeQuery = true)
    java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz> findQuizzes(int executionId);

    @org.springframework.data.jpa.repository.Query(value = "SELECT MAX(key) FROM quizzes", nativeQuery = true)
    java.lang.Integer getMaxQuizKey();

    @org.springframework.data.jpa.repository.Query(value = "SELECT * FROM quizzes q WHERE q.key = :key", nativeQuery = true)
    java.util.Optional<pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz> findByKey(java.lang.Integer key);
}