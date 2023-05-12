package pt.ulisboa.tecnico.socialsoftware.tutor.statement.dto;
public class StatementQuestionDto implements java.io.Serializable {
    private java.lang.String content;

    private java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.statement.dto.StatementOptionDto> options;

    private pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.ImageDto image;

    private java.lang.Integer sequence;

    public StatementQuestionDto(pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswer questionAnswer) {
        this.content = questionAnswer.getQuizQuestion().getQuestion().getContent();
        if (questionAnswer.getQuizQuestion().getQuestion().getImage() != null) {
            this.image = new pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.ImageDto(questionAnswer.getQuizQuestion().getQuestion().getImage());
        }
        this.options = questionAnswer.getQuizQuestion().getQuestion().getOptions().stream().map(pt.ulisboa.tecnico.socialsoftware.tutor.statement.dto.StatementOptionDto::new).collect(java.util.stream.Collectors.toList());
        this.sequence = questionAnswer.getSequence();
    }

    public java.lang.String getContent() {
        return content;
    }

    public void setContent(java.lang.String content) {
        this.content = content;
    }

    public java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.statement.dto.StatementOptionDto> getOptions() {
        return options;
    }

    public void setOptions(java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.statement.dto.StatementOptionDto> options) {
        this.options = options;
    }

    public pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.ImageDto getImage() {
        return image;
    }

    public void setImage(pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.ImageDto image) {
        this.image = image;
    }

    public java.lang.Integer getSequence() {
        return sequence;
    }

    public void setSequence(java.lang.Integer sequence) {
        this.sequence = sequence;
    }

    @java.lang.Override
    public java.lang.String toString() {
        return ((((((((("StatementQuestionDto{" + ", content='") + content) + '\'') + ", options=") + options) + ", image=") + image) + ", sequence=") + sequence) + '}';
    }
}