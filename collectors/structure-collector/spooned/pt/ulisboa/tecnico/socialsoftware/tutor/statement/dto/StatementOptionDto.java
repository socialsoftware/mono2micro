package pt.ulisboa.tecnico.socialsoftware.tutor.statement.dto;
public class StatementOptionDto implements java.io.Serializable {
    private java.lang.Integer optionId;

    private java.lang.String content;

    public StatementOptionDto(pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Option option) {
        this.optionId = option.getId();
        this.content = option.getContent();
    }

    public java.lang.Integer getOptionId() {
        return optionId;
    }

    public void setOptionId(java.lang.Integer optionId) {
        this.optionId = optionId;
    }

    public java.lang.String getContent() {
        return content;
    }

    public void setContent(java.lang.String content) {
        this.content = content;
    }

    @java.lang.Override
    public java.lang.String toString() {
        return ((((("StatementOptionDto{" + "optionId=") + optionId) + ", content='") + content) + '\'') + '}';
    }
}