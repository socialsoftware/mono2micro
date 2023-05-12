package pt.ulisboa.tecnico.socialsoftware.tutor.question.dto;
public class OptionDto implements java.io.Serializable {
    private java.lang.Integer id;

    private java.lang.Integer sequence;

    private boolean correct;

    private java.lang.String content;

    public OptionDto() {
    }

    public OptionDto(pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Option option) {
        this.id = option.getId();
        this.sequence = option.getSequence();
        this.content = option.getContent();
        this.correct = option.getCorrect();
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

    public boolean getCorrect() {
        return correct;
    }

    public void setCorrect(boolean correct) {
        this.correct = correct;
    }

    public java.lang.String getContent() {
        return content;
    }

    public void setContent(java.lang.String content) {
        this.content = content;
    }

    @java.lang.Override
    public java.lang.String toString() {
        return ((((((((("OptionDto{" + "id=") + id) + ", id=") + id) + ", correct=") + correct) + ", content='") + content) + '\'') + '}';
    }
}