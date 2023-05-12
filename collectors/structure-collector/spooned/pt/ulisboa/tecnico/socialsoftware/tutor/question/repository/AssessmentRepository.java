package pt.ulisboa.tecnico.socialsoftware.tutor.question.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
@org.springframework.stereotype.Repository
@org.springframework.transaction.annotation.Transactional
public interface AssessmentRepository extends org.springframework.data.jpa.repository.JpaRepository<pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Assessment, java.lang.Integer> {
    @org.springframework.data.jpa.repository.Query(value = "SELECT * FROM assessments a, course_executions c WHERE c.id = :courseExecutionId AND c.id = a.course_execution_id", nativeQuery = true)
    java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Assessment> findByExecutionCourseId(int courseExecutionId);
}