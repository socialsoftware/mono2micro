package pt.ulisboa.tecnico.socialsoftware.tutor.question;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
@org.springframework.stereotype.Service
public class TopicService {
    @org.springframework.beans.factory.annotation.Autowired
    private pt.ulisboa.tecnico.socialsoftware.tutor.question.QuestionService questionService;

    @org.springframework.beans.factory.annotation.Autowired
    private pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository courseRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.TopicRepository topicRepository;

    @org.springframework.transaction.annotation.Transactional(isolation = org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseDto findTopicCourse(int topicId) {
        return topicRepository.findById(topicId).map(pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic::getCourse).map(pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseDto::new).orElseThrow(() -> new <TOPIC_NOT_FOUND>pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(topicId));
    }

    @org.springframework.retry.annotation.Retryable(value = { java.sql.SQLException.class }, backoff = @org.springframework.retry.annotation.Backoff(delay = 5000))
    @org.springframework.transaction.annotation.Transactional(isolation = org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto> findTopics(int courseId) {
        pt.ulisboa.tecnico.socialsoftware.tutor.course.Course course = courseRepository.findById(courseId).orElseThrow(() -> new <COURSE_NOT_FOUND>pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(courseId));
        return topicRepository.findTopics(course.getId()).stream().sorted(java.util.Comparator.comparing(pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic::getName)).map(pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto::new).collect(java.util.stream.Collectors.toList());
    }

    @org.springframework.retry.annotation.Retryable(value = { java.sql.SQLException.class }, backoff = @org.springframework.retry.annotation.Backoff(delay = 5000))
    @org.springframework.transaction.annotation.Transactional(isolation = org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto createTopic(int courseId, pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto topicDto) {
        pt.ulisboa.tecnico.socialsoftware.tutor.course.Course course = courseRepository.findById(courseId).orElseThrow(() -> new <COURSE_NOT_FOUND>pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(courseId));
        if (topicRepository.findTopicByName(course.getId(), topicDto.getName()) != null) {
            throw new pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.DUPLICATE_TOPIC, topicDto.getName());
        }
        pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic topic = new pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic(course, topicDto);
        topicRepository.save(topic);
        return new pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto(topic);
    }

    @org.springframework.retry.annotation.Retryable(value = { java.sql.SQLException.class }, backoff = @org.springframework.retry.annotation.Backoff(delay = 5000))
    @org.springframework.transaction.annotation.Transactional(isolation = org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto updateTopic(java.lang.Integer topicId, pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto topicDto) {
        pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic topic = topicRepository.findById(topicId).orElseThrow(() -> new <TOPIC_NOT_FOUND>pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(topicId));
        topic.setName(topicDto.getName());
        return new pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto(topic);
    }

    @org.springframework.retry.annotation.Retryable(value = { java.sql.SQLException.class }, backoff = @org.springframework.retry.annotation.Backoff(delay = 5000))
    @org.springframework.transaction.annotation.Transactional(isolation = org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public void removeTopic(java.lang.Integer topicId) {
        pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic topic = topicRepository.findById(topicId).orElseThrow(() -> new <TOPIC_NOT_FOUND>pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(topicId));
        topic.remove();
        topicRepository.delete(topic);
    }

    @org.springframework.retry.annotation.Retryable(value = { java.sql.SQLException.class }, backoff = @org.springframework.retry.annotation.Backoff(delay = 5000))
    @org.springframework.transaction.annotation.Transactional(isolation = org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public java.lang.String exportTopics() {
        pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.TopicsXmlExport xmlExport = new pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.TopicsXmlExport();
        return xmlExport.export(topicRepository.findAll());
    }

    @org.springframework.retry.annotation.Retryable(value = { java.sql.SQLException.class }, backoff = @org.springframework.retry.annotation.Backoff(delay = 5000))
    @org.springframework.transaction.annotation.Transactional(isolation = org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public void importTopics(java.lang.String topicsXML) {
        pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.TopicsXmlImport xmlImporter = new pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.TopicsXmlImport();
        xmlImporter.importTopics(topicsXML, this, questionService, courseRepository);
    }

    @org.springframework.retry.annotation.Retryable(value = { java.sql.SQLException.class }, backoff = @org.springframework.retry.annotation.Backoff(delay = 5000))
    @org.springframework.transaction.annotation.Transactional(isolation = org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public void resetDemoTopics() {
        this.topicRepository.findTopics(pt.ulisboa.tecnico.socialsoftware.tutor.config.Demo.COURSE_ID).stream().filter(topic -> topic.getId() > 125).forEach(topic -> this.topicRepository.delete(topic));
    }
}