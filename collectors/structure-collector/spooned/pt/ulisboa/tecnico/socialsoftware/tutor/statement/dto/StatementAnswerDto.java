package pt.ulisboa.tecnico.socialsoftware.tutor.statement.dto;
public class StatementAnswerDto implements java.io.Serializable {
    private java.lang.Integer timeTaken;

    private java.lang.Integer sequence;

    private java.lang.Integer optionId;

    public StatementAnswerDto() {
    }

    public StatementAnswerDto(pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswer questionAnswer) {
        this.timeTaken = questionAnswer.getTimeTaken();
        this.sequence = questionAnswer.getSequence();
        if (questionAnswer.getOption() != null) {
            this.optionId = questionAnswer.getOption().getId();
        }
    }

    public java.lang.Integer getOptionId() {
        return optionId;
    }

    public void setOptionId(java.lang.Integer optionId) {
        this.optionId = optionId;
    }

    public java.lang.Integer getTimeTaken() {
        return timeTaken;
    }

    public void setTimeTaken(java.lang.Integer timeTaken) {
        this.timeTaken = timeTaken;
    }

    public java.lang.Integer getSequence() {
        return sequence;
    }

    public void setSequence(java.lang.Integer sequence) {
        this.sequence = sequence;
    }

    @java.lang.Override
    public java.lang.String toString() {
        return (((((("StatementAnswerDto{" + ", optionId=") + optionId) + ", timeTaken=") + timeTaken) + ", sequence=") + sequence) + '}';
    }
}