package pt.ulisboa.tecnico.socialsoftware.tutor.question.dto;
public class TopicConjunctionDto implements java.io.Serializable {
    private java.lang.Integer id;

    private java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto> topics = new java.util.ArrayList<>();

    public TopicConjunctionDto() {
    }

    public TopicConjunctionDto(pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.TopicConjunction topicConjunction) {
        this.id = topicConjunction.getId();
        this.topics = topicConjunction.getTopics().stream().map(pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto::new).collect(java.util.stream.Collectors.toList());
    }

    public java.lang.Integer getId() {
        return id;
    }

    public void setId(java.lang.Integer id) {
        this.id = id;
    }

    public java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto> getTopics() {
        return topics;
    }

    public void setTopics(java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto> topics) {
        this.topics = topics;
    }

    @java.lang.Override
    public java.lang.String toString() {
        return (((("TopicConjunctionDto{" + "id=") + id) + ", topics=") + topics) + '}';
    }

    public void addTopic(pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto topic) {
        this.topics.add(topic);
    }
}