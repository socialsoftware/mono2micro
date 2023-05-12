package pt.ulisboa.tecnico.socialsoftware.tutor.question.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
@org.springframework.stereotype.Repository
@org.springframework.transaction.annotation.Transactional
public interface QuestionRepository extends pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.MiddleMan<pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question, pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.Read> {
    @org.springframework.data.jpa.repository.Query(value = "SELECT * FROM questions q WHERE q.course_id = :courseId", nativeQuery = true)
    java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question> findQuestions(int courseId);

    @org.springframework.data.jpa.repository.Query(value = "SELECT * FROM questions q WHERE q.course_id = :courseId AND q.status = 'AVAILABLE'", nativeQuery = true)
    java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question> findAvailableQuestions(int courseId);

    @org.springframework.data.jpa.repository.Query(value = "SELECT count(*) FROM questions q WHERE q.course_id = :courseId AND q.status = 'AVAILABLE'", nativeQuery = true)
    java.lang.Integer getAvailableQuestionsSize(java.lang.Integer courseId);
}