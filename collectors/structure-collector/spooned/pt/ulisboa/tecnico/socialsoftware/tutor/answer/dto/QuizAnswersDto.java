package pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto;
public class QuizAnswersDto implements java.io.Serializable {
    private java.lang.Long secondsToSubmission;

    private java.util.List<java.lang.Integer> correctSequence;

    private java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.QuizAnswerDto> quizAnswers = new java.util.ArrayList<>();

    public java.lang.Long getSecondsToSubmission() {
        return secondsToSubmission;
    }

    public void setSecondsToSubmission(java.lang.Long secondsToSubmission) {
        this.secondsToSubmission = secondsToSubmission;
    }

    public java.util.List<java.lang.Integer> getCorrectSequence() {
        return correctSequence;
    }

    public void setCorrectSequence(java.util.List<java.lang.Integer> correctSequence) {
        this.correctSequence = correctSequence;
    }

    public java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.QuizAnswerDto> getQuizAnswers() {
        return quizAnswers;
    }

    public void setQuizAnswers(java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.QuizAnswerDto> quizAnswers) {
        this.quizAnswers = quizAnswers;
    }
}