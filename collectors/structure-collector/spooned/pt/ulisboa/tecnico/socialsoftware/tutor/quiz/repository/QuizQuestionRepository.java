package pt.ulisboa.tecnico.socialsoftware.tutor.quiz.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
@org.springframework.stereotype.Repository
@org.springframework.transaction.annotation.Transactional
public interface QuizQuestionRepository extends org.springframework.data.jpa.repository.JpaRepository<pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion, java.lang.Integer> {}