package pt.ulisboa.tecnico.socialsoftware.tutor.question.domain;
import javax.persistence.*;
@pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Entity
@pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Table(name = "assessments")
public class Assessment {
    @java.lang.SuppressWarnings("unused")
    public enum Status {

        DISABLED,
        AVAILABLE,
        REMOVED;}

    @pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Id
    @pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.GeneratedValue(strategy = GenerationType.IDENTITY)
    private java.lang.Integer id;

    private java.lang.String title;

    private java.lang.Integer sequence = 0;

    @pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Enumerated(EnumType.STRING)
    private pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Assessment.Status status = pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Assessment.Status.DISABLED;

    @pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.ManyToOne(fetch = FetchType.LAZY)
    @pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.JoinColumn(name = "course_execution_id")
    private pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution courseExecution;

    @pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.OneToMany(cascade = CascadeType.ALL, mappedBy = "assessment", fetch = FetchType.EAGER, orphanRemoval = true)
    private java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.TopicConjunction> topicConjunctions = new java.util.ArrayList<>();

    public Assessment() {
    }

    public Assessment(pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution courseExecution, java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.TopicConjunction> topicConjunctions, pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.AssessmentDto assessmentDto) {
        setTitle(assessmentDto.getTitle());
        setStatus(pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Assessment.Status.valueOf(assessmentDto.getStatus()));
        setSequence(assessmentDto.getSequence());
        setCourseExecution(courseExecution);
        courseExecution.addAssessment(this);
        this.topicConjunctions = topicConjunctions;
        topicConjunctions.forEach(topicConjunction -> topicConjunction.setAssessment(this));
    }

    public java.lang.Integer getId() {
        return id;
    }

    public void setId(java.lang.Integer id) {
        this.id = id;
    }

    public java.lang.String getTitle() {
        return title;
    }

    public void setTitle(java.lang.String title) {
        this.title = title;
    }

    public java.lang.Integer getSequence() {
        return sequence;
    }

    public void setSequence(java.lang.Integer sequence) {
        this.sequence = sequence;
    }

    public pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Assessment.Status getStatus() {
        return status;
    }

    public void setStatus(pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Assessment.Status status) {
        this.status = status;
    }

    public pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution getCourseExecution() {
        return courseExecution;
    }

    public void setCourseExecution(pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution courseExecution) {
        this.courseExecution = courseExecution;
    }

    public java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.TopicConjunction> getTopicConjunctions() {
        return topicConjunctions;
    }

    @java.lang.Override
    public java.lang.String toString() {
        return ((((((((((("Assessment{" + "id=") + id) + ", id=") + id) + ", title='") + title) + '\'') + ", status=") + status) + ", topicConjunctions=") + topicConjunctions) + '}';
    }

    public void addTopicConjunction(pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.TopicConjunction topicConjunction) {
        this.topicConjunctions.add(topicConjunction);
    }

    public void remove() {
        new java.util.ArrayList<>(getTopicConjunctions()).forEach(pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.TopicConjunction::remove);
        getTopicConjunctions().clear();
    }

    public java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question> getQuestions() {
        return this.topicConjunctions.stream().flatMap(topicConjunction -> topicConjunction.getQuestions().stream()).collect(java.util.stream.Collectors.toList());
    }
}