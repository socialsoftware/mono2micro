package pt.ulisboa.tecnico.socialsoftware.tutor.statement.dto;
public class SolvedQuizDto implements java.io.Serializable {
    private pt.ulisboa.tecnico.socialsoftware.tutor.statement.dto.StatementQuizDto statementQuiz;

    private java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.CorrectAnswerDto> correctAnswers = new java.util.ArrayList<>();

    private java.lang.String answerDate;

    public SolvedQuizDto() {
    }

    public SolvedQuizDto(pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer quizAnswer) {
        this.statementQuiz = new pt.ulisboa.tecnico.socialsoftware.tutor.statement.dto.StatementQuizDto(quizAnswer);
        this.correctAnswers = quizAnswer.getQuestionAnswers().stream().sorted(java.util.Comparator.comparing(pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswer::getSequence)).map(pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.CorrectAnswerDto::new).collect(java.util.stream.Collectors.toList());
        this.answerDate = quizAnswer.getAnswerDate().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    public pt.ulisboa.tecnico.socialsoftware.tutor.statement.dto.StatementQuizDto getStatementQuiz() {
        return statementQuiz;
    }

    public void setStatementQuiz(pt.ulisboa.tecnico.socialsoftware.tutor.statement.dto.StatementQuizDto statementQuiz) {
        this.statementQuiz = statementQuiz;
    }

    public java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.CorrectAnswerDto> getCorrectAnswers() {
        return correctAnswers;
    }

    public void setCorrectAnswers(java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.CorrectAnswerDto> correctAnswers) {
        this.correctAnswers = correctAnswers;
    }

    public java.lang.String getAnswerDate() {
        return answerDate;
    }

    public void setAnswerDate(java.lang.String answerDate) {
        this.answerDate = answerDate;
    }

    @java.lang.Override
    public java.lang.String toString() {
        return ((((((("SolvedQuizDto{" + "statementQuiz=") + statementQuiz) + ", correctAnswers=") + correctAnswers) + ", answerDate='") + answerDate) + '\'') + '}';
    }
}