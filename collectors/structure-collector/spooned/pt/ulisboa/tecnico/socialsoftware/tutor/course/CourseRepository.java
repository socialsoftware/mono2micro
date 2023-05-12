package pt.ulisboa.tecnico.socialsoftware.tutor.course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
@org.springframework.stereotype.Repository
@org.springframework.transaction.annotation.Transactional
public interface CourseRepository extends org.springframework.data.jpa.repository.JpaRepository<pt.ulisboa.tecnico.socialsoftware.tutor.course.Course, java.lang.Integer> {
    @org.springframework.data.jpa.repository.Query(value = "select * from courses c where c.name = :name and c.type = :type", nativeQuery = true)
    java.util.Optional<pt.ulisboa.tecnico.socialsoftware.tutor.course.Course> findByNameType(java.lang.String name, java.lang.String type);
}