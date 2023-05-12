package pt.ulisboa.tecnico.socialsoftware.tutor.quiz.dto;
public class QuizQuestionDto implements java.io.Serializable {
    private java.lang.Integer id;

    private java.lang.Integer sequence;

    public QuizQuestionDto() {
    }

    public QuizQuestionDto(pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion quizQuestion) {
        this.id = quizQuestion.getId();
        this.sequence = quizQuestion.getSequence();
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

    @java.lang.Override
    public java.lang.String toString() {
        return (((("QuizQuestionDto{" + "id=") + id) + ", sequence=") + sequence) + '}';
    }
}