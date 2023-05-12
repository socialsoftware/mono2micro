package pt.ulisboa.tecnico.socialsoftware.tutor.question;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
@org.springframework.stereotype.Service
public class AssessmentService {
    @java.lang.SuppressWarnings("unused")
    private static org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(pt.ulisboa.tecnico.socialsoftware.tutor.question.AssessmentService.class);

    @org.springframework.beans.factory.annotation.Autowired
    private pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.AssessmentRepository assessmentRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.TopicRepository topicRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.TopicConjunctionRepository topicConjunctionRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository courseExecutionRepository;

    @org.springframework.transaction.annotation.Transactional(isolation = org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseDto findAssessmentCourseExecution(int assessmentId) {
        return assessmentRepository.findById(assessmentId).map(pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Assessment::getCourseExecution).map(pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseDto::new).orElseThrow(() -> new <ASSESSMENT_NOT_FOUND>pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(assessmentId));
    }

    @org.springframework.retry.annotation.Retryable(value = { java.sql.SQLException.class }, backoff = @org.springframework.retry.annotation.Backoff(delay = 5000))
    @org.springframework.transaction.annotation.Transactional(isolation = org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.AssessmentDto> findAssessments(int courseExecutionId) {
        return assessmentRepository.findByExecutionCourseId(courseExecutionId).stream().map(pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.AssessmentDto::new).collect(java.util.stream.Collectors.toList());
    }

    @org.springframework.retry.annotation.Retryable(value = { java.sql.SQLException.class }, backoff = @org.springframework.retry.annotation.Backoff(delay = 5000))
    @org.springframework.transaction.annotation.Transactional(isolation = org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.AssessmentDto> findAvailableAssessments(int courseExecutionId) {
        return assessmentRepository.findByExecutionCourseId(courseExecutionId).stream().filter(assessment -> assessment.getStatus() == pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Assessment.Status.AVAILABLE).map(pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.AssessmentDto::new).sorted(java.util.Comparator.comparing(pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.AssessmentDto::getSequence, java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder()))).collect(java.util.stream.Collectors.toList());
    }

    @org.springframework.retry.annotation.Retryable(value = { java.sql.SQLException.class }, backoff = @org.springframework.retry.annotation.Backoff(delay = 5000))
    @org.springframework.transaction.annotation.Transactional(isolation = org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.AssessmentDto createAssessment(int executionId, pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.AssessmentDto assessmentDto) {
        pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution courseExecution = courseExecutionRepository.findById(executionId).orElseThrow(() -> new <COURSE_EXECUTION_NOT_FOUND>pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(executionId));
        java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.TopicConjunction> topicConjunctions = assessmentDto.getTopicConjunctions().stream().map(topicConjunctionDto -> {
            pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.TopicConjunction topicConjunction = new pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.TopicConjunction();
            java.util.Set<pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic> newTopics = topicConjunctionDto.getTopics().stream().map(topicDto -> topicRepository.findById(topicDto.getId()).orElseThrow()).collect(java.util.stream.Collectors.toSet());
            topicConjunction.updateTopics(newTopics);
            return topicConjunction;
        }).collect(java.util.stream.Collectors.toList());
        pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Assessment assessment = new pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Assessment(courseExecution, topicConjunctions, assessmentDto);
        assessmentRepository.save(assessment);
        return new pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.AssessmentDto(assessment);
    }

    @org.springframework.retry.annotation.Retryable(value = { java.sql.SQLException.class }, backoff = @org.springframework.retry.annotation.Backoff(delay = 5000))
    @org.springframework.transaction.annotation.Transactional(isolation = org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.AssessmentDto updateAssessment(java.lang.Integer assessmentId, pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.AssessmentDto assessmentDto) {
        pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Assessment assessment = assessmentRepository.findById(assessmentId).orElseThrow(() -> new <ASSESSMENT_NOT_FOUND>pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(assessmentId));
        assessment.setTitle(assessmentDto.getTitle());
        assessment.setStatus(pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Assessment.Status.valueOf(assessmentDto.getStatus()));
        assessment.setSequence(assessmentDto.getSequence());
        // remove TopicConjunction that are not in the Dto
        assessment.getTopicConjunctions().stream().filter(topicConjunction -> assessmentDto.getTopicConjunctions().stream().noneMatch(topicConjunctionDto -> topicConjunction.getId().equals(topicConjunctionDto.getId()))).collect(java.util.stream.Collectors.toList()).forEach(pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.TopicConjunction::remove);
        for (pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicConjunctionDto topicConjunctionDto : assessmentDto.getTopicConjunctions()) {
            // topicConjunction already existed
            if (topicConjunctionDto.getId() != null) {
                pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.TopicConjunction topicConjunction = topicConjunctionRepository.findById(topicConjunctionDto.getId()).orElseThrow(() -> new <TOPIC_CONJUNCTION_NOT_FOUND>pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(topicConjunctionDto.getId()));
                java.util.Set<pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic> newTopics = topicConjunctionDto.getTopics().stream().map(topicDto -> topicRepository.findById(topicDto.getId()).orElseThrow()).collect(java.util.stream.Collectors.toSet());
                topicConjunction.updateTopics(newTopics);
            } else {
                // new topicConjunction
                pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.TopicConjunction topicConjunction = new pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.TopicConjunction();
                java.util.Set<pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic> newTopics = topicConjunctionDto.getTopics().stream().map(topicDto -> topicRepository.findById(topicDto.getId()).orElseThrow(() -> new <TOPIC_NOT_FOUND>pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(topicDto.getId()))).collect(java.util.stream.Collectors.toSet());
                topicConjunction.updateTopics(newTopics);
                assessment.addTopicConjunction(topicConjunction);
                topicConjunction.setAssessment(assessment);
                topicConjunctionRepository.save(topicConjunction);
            }
        }
        return new pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.AssessmentDto(assessment);
    }

    @org.springframework.retry.annotation.Retryable(value = { java.sql.SQLException.class }, backoff = @org.springframework.retry.annotation.Backoff(delay = 5000))
    @org.springframework.transaction.annotation.Transactional(isolation = org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public void removeAssessment(java.lang.Integer assessmentId) {
        pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Assessment assessment = assessmentRepository.findById(assessmentId).orElseThrow(() -> new <ASSESSMENT_NOT_FOUND>pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(assessmentId));
        assessment.remove();
        assessmentRepository.delete(assessment);
    }

    @org.springframework.retry.annotation.Retryable(value = { java.sql.SQLException.class }, backoff = @org.springframework.retry.annotation.Backoff(delay = 5000))
    @org.springframework.transaction.annotation.Transactional(isolation = org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public void assessmentSetStatus(java.lang.Integer assessmentId, pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Assessment.Status status) {
        pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Assessment assessment = assessmentRepository.findById(assessmentId).orElseThrow(() -> new <ASSESSMENT_NOT_FOUND>pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(assessmentId));
        assessment.setStatus(status);
    }

    @org.springframework.retry.annotation.Retryable(value = { java.sql.SQLException.class }, backoff = @org.springframework.retry.annotation.Backoff(delay = 5000))
    @org.springframework.transaction.annotation.Transactional(isolation = org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public void resetDemoAssessments() {
        this.assessmentRepository.findByExecutionCourseId(pt.ulisboa.tecnico.socialsoftware.tutor.config.Demo.COURSE_EXECUTION_ID).stream().filter(assessment -> assessment.getId() > 10).forEach(assessment -> assessmentRepository.delete(assessment));
    }
}