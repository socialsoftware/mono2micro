package pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto;
public class CorrectAnswerDto implements java.io.Serializable {
    private java.lang.Integer correctOptionId;

    private java.lang.Integer sequence;

    public CorrectAnswerDto(pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswer questionAnswer) {
        this.correctOptionId = questionAnswer.getQuizQuestion().getQuestion().getCorrectOptionId();
        this.sequence = questionAnswer.getSequence();
    }

    public java.lang.Integer getCorrectOptionId() {
        return correctOptionId;
    }

    public void setCorrectOptionId(java.lang.Integer correctOptionId) {
        this.correctOptionId = correctOptionId;
    }

    public java.lang.Integer getSequence() {
        return sequence;
    }

    public void setSequence(java.lang.Integer sequence) {
        this.sequence = sequence;
    }

    @java.lang.Override
    public java.lang.String toString() {
        return (((("CorrectAnswerDto{" + "correctOptionId=") + correctOptionId) + ", sequence=") + sequence) + '}';
    }
}