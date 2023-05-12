package pt.ulisboa.tecnico.socialsoftware.tutor.question.dto;
public class AssessmentDto implements java.io.Serializable {
    private java.lang.Integer id;

    private java.lang.Integer sequence;

    private java.lang.Integer numberOfQuestions;

    private java.lang.String title;

    private java.lang.String status;

    private java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicConjunctionDto> topicConjunctions;

    public AssessmentDto() {
    }

    public AssessmentDto(pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Assessment assessment) {
        this.id = assessment.getId();
        this.sequence = assessment.getSequence();
        this.numberOfQuestions = assessment.getQuestions().size();
        this.title = assessment.getTitle();
        this.status = assessment.getStatus().name();
        this.topicConjunctions = assessment.getTopicConjunctions().stream().map(pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicConjunctionDto::new).collect(java.util.stream.Collectors.toList());
    }

    public java.lang.Integer getId() {
        return id;
    }

    public void setId(java.lang.Integer id) {
        this.id = id;
    }

    public java.lang.Integer getSequence() {
        return sequence;
    }

    public void setSequence(java.lang.Integer sequence) {
        this.sequence = sequence;
    }

    public java.lang.Integer getNumberOfQuestions() {
        return numberOfQuestions;
    }

    public void setNumberOfQuestions(java.lang.Integer numberOfQuestions) {
        this.numberOfQuestions = numberOfQuestions;
    }

    public java.lang.String getTitle() {
        return title;
    }

    public void setTitle(java.lang.String title) {
        this.title = title;
    }

    public java.lang.String getStatus() {
        return status;
    }

    public void setStatus(java.lang.String status) {
        this.status = status;
    }

    public java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicConjunctionDto> getTopicConjunctions() {
        return topicConjunctions;
    }

    public void setTopicConjunctions(java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicConjunctionDto> topicConjunctions) {
        this.topicConjunctions = topicConjunctions;
    }

    @java.lang.Override
    public java.lang.String toString() {
        return (((((((((((((("AssessmentDto{" + "id=") + id) + ", sequence=") + sequence) + ", numberOfQuestions=") + numberOfQuestions) + ", title='") + title) + '\'') + ", status='") + status) + '\'') + ", topicConjunctions=") + topicConjunctions) + '}';
    }

    public void addTopicConjunction(pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicConjunctionDto topicConjunctionDto) {
        this.topicConjunctions.add(topicConjunctionDto);
    }
}