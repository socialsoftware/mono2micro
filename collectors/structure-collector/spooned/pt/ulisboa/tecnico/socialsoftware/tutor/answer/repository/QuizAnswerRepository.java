package pt.ulisboa.tecnico.socialsoftware.tutor.answer.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
@org.springframework.stereotype.Repository
@org.springframework.transaction.annotation.Transactional
public interface QuizAnswerRepository extends org.springframework.data.jpa.repository.JpaRepository<pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer, java.lang.Integer> {
    @org.springframework.data.jpa.repository.Query(value = "SELECT * FROM quiz_answers qa WHERE qa.user_id = :userId AND qa.quiz_id = :quizId", nativeQuery = true)
    java.util.Optional<pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer> findQuizAnswer(java.lang.Integer quizId, java.lang.Integer userId);

    @org.springframework.data.jpa.repository.Query(value = "SELECT * FROM quiz_answers qa JOIN quizzes q ON qa.quiz_id = q.id WHERE (NOT qa.completed AND q.conclusion_date < :now) OR (qa.completed AND NOT qa.used_in_statistics)", nativeQuery = true)
    java.util.Set<pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer> findQuizAnswersToClose(java.time.LocalDateTime now);
}