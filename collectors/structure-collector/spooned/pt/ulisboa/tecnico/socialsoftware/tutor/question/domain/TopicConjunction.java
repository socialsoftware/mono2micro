package pt.ulisboa.tecnico.socialsoftware.tutor.question.domain;
import javax.persistence.*;
@pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Entity
@pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Table(name = "topic_conjunctions")
public class TopicConjunction {
    @pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Id
    @pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.GeneratedValue(strategy = GenerationType.IDENTITY)
    private java.lang.Integer id;

    @pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.ManyToMany(cascade = CascadeType.ALL, mappedBy = "topicConjunctions")
    private java.util.Set<pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic> topics = new java.util.HashSet<>();

    @pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.ManyToOne(fetch = FetchType.LAZY)
    @pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.JoinColumn(name = "assessment_id")
    private pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Assessment assessment;

    public java.lang.Integer getId() {
        return id;
    }

    public void setId(java.lang.Integer id) {
        this.id = id;
    }

    public java.util.Set<pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic> getTopics() {
        return topics;
    }

    public pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Assessment getAssessment() {
        return assessment;
    }

    public void setAssessment(pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Assessment assessment) {
        this.assessment = assessment;
    }

    public void addTopic(pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic topic) {
        topics.add(topic);
    }

    public void remove() {
        getTopics().forEach(topic -> topic.getTopicConjunctions().remove(this));
        getTopics().clear();
        this.assessment.getTopicConjunctions().remove(this);
        this.assessment = null;
    }

    @java.lang.Override
    public boolean equals(java.lang.Object o) {
        if (this == o)
            return true;

        if ((o == null) || (getClass() != o.getClass()))
            return false;

        pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.TopicConjunction that = ((pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.TopicConjunction) (o));
        return id.equals(that.id) && java.util.Objects.equals(topics, that.topics);
    }

    @java.lang.Override
    public int hashCode() {
        return java.util.Objects.hash(id, topics);
    }

    @java.lang.Override
    public java.lang.String toString() {
        return (((("TopicConjunction{" + "id=") + id) + ", topics=") + topics) + '}';
    }

    public void updateTopics(java.util.Set<pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic> newTopics) {
        java.util.Set<pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic> toRemove = this.topics.stream().filter(topic -> !newTopics.contains(topic)).collect(java.util.stream.Collectors.toSet());
        toRemove.forEach(topic -> {
            this.topics.remove(topic);
            topic.getTopicConjunctions().remove(this);
        });
        newTopics.stream().filter(topic -> !this.topics.contains(topic)).forEach(topic -> {
            this.topics.add(topic);
            topic.addTopicConjunction(this);
        });
    }

    public java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question> getQuestions() {
        return this.topics.stream().flatMap(topic -> topic.getQuestions().stream()).filter(question -> question.getTopics().equals(this.topics)).collect(java.util.stream.Collectors.toList());
    }
}