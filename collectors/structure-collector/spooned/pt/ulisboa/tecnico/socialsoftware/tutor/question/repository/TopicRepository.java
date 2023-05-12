package pt.ulisboa.tecnico.socialsoftware.tutor.question.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
@org.springframework.stereotype.Repository
@org.springframework.transaction.annotation.Transactional
public interface TopicRepository extends org.springframework.data.jpa.repository.JpaRepository<pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic, java.lang.Integer> {
    @org.springframework.data.jpa.repository.Query(value = "SELECT * FROM topics t, courses c WHERE t.course_id = c.id AND c.id = :courseId", nativeQuery = true)
    java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic> findTopics(int courseId);

    @org.springframework.data.jpa.repository.Query(value = "SELECT * FROM topics t, courses c WHERE t.course_id = c.id AND c.id = :courseId AND t.name = :name", nativeQuery = true)
    pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic findTopicByName(int courseId, java.lang.String name);
}