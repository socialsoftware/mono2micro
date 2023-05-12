package pt.ulisboa.tecnico.socialsoftware.tutor.question.domain;
import javax.persistence.*;
@pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Entity
@pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Table(name = "topics")
public class Topic {
    @java.lang.SuppressWarnings("unused")
    public enum Status {

        DISABLED,
        REMOVED,
        AVAILABLE;}

    @pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Id
    @pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.GeneratedValue(strategy = GenerationType.IDENTITY)
    private java.lang.Integer id;

    private java.lang.String name;

    @pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.ManyToMany
    private java.util.Set<pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question> questions = new java.util.HashSet<>();

    @pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.TopicConjunction> topicConjunctions = new java.util.ArrayList<>();

    @pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.ManyToOne
    @pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.JoinColumn(name = "course_id")
    private pt.ulisboa.tecnico.socialsoftware.tutor.course.Course course;

    public Topic() {
    }

    public Topic(pt.ulisboa.tecnico.socialsoftware.tutor.course.Course course, pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto topicDto) {
        this.name = topicDto.getName();
        this.course = course;
        course.addTopic(this);
    }

    public java.lang.Integer getId() {
        return id;
    }

    public void setId(java.lang.Integer id) {
        this.id = id;
    }

    public java.lang.String getName() {
        return name;
    }

    public void setName(java.lang.String name) {
        this.name = name;
    }

    public java.util.Set<pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question> getQuestions() {
        return questions;
    }

    public java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.TopicConjunction> getTopicConjunctions() {
        return topicConjunctions;
    }

    public pt.ulisboa.tecnico.socialsoftware.tutor.course.Course getCourse() {
        return course;
    }

    public void setCourse(pt.ulisboa.tecnico.socialsoftware.tutor.course.Course course) {
        this.course = course;
    }

    public void addTopicConjunction(pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.TopicConjunction topicConjunction) {
        this.topicConjunctions.add(topicConjunction);
    }

    public void addQuestion(pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question question) {
        this.questions.add(question);
    }

    @java.lang.Override
    public boolean equals(java.lang.Object o) {
        if (this == o)
            return true;

        if ((o == null) || (getClass() != o.getClass()))
            return false;

        pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic topic = ((pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic) (o));
        return name.equals(topic.name);
    }

    @java.lang.Override
    public int hashCode() {
        return java.util.Objects.hash(name);
    }

    @java.lang.Override
    public java.lang.String toString() {
        return ((((("Topic{" + "id=") + id) + ", name='") + name) + '\'') + '}';
    }

    public void remove() {
        getCourse().getTopics().remove(this);
        course = null;
        getQuestions().forEach(question -> question.getTopics().remove(this));
        getQuestions().clear();
        this.topicConjunctions.forEach(topicConjunction -> topicConjunction.getTopics().remove(this));
    }
}