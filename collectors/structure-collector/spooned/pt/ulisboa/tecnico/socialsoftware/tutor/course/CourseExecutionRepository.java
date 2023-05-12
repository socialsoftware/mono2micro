package pt.ulisboa.tecnico.socialsoftware.tutor.course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
@org.springframework.stereotype.Repository
@org.springframework.transaction.annotation.Transactional
public interface CourseExecutionRepository extends org.springframework.data.jpa.repository.JpaRepository<pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution, java.lang.Integer> {
    @org.springframework.data.jpa.repository.Query(value = "SELECT * FROM course_executions ce WHERE ce.acronym = :acronym AND ce.academic_term = :academicTerm AND ce.type = :type", nativeQuery = true)
    java.util.Optional<pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution> findByAcronymAcademicTermType(java.lang.String acronym, java.lang.String academicTerm, java.lang.String type);
}