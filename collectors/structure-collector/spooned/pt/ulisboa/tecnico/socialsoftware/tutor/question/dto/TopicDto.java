package pt.ulisboa.tecnico.socialsoftware.tutor.question.dto;
public class TopicDto implements java.io.Serializable {
    private java.lang.Integer id;

    private java.lang.String name;

    private java.lang.Integer numberOfQuestions;

    public TopicDto() {
    }

    public TopicDto(pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic topic) {
        this.id = topic.getId();
        this.name = topic.getName();
        this.numberOfQuestions = topic.getQuestions().size();
    }

    public TopicDto(pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto topicDto) {
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

    public java.lang.Integer getNumberOfQuestions() {
        return numberOfQuestions;
    }

    public void setNumberOfQuestions(java.lang.Integer numberOfQuestions) {
        this.numberOfQuestions = numberOfQuestions;
    }

    @java.lang.Override
    public java.lang.String toString() {
        return ((((("TopicDto{" + "id=") + id) + ", name='") + name) + '\'') + '}';
    }
}