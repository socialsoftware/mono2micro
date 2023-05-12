package pt.ulisboa.tecnico.socialsoftware.tutor.statement.dto;
public class StatementQuizDto implements java.io.Serializable {
    private java.lang.Integer id;

    private java.lang.Integer quizAnswerId;

    private java.lang.String title;

    private boolean qrCodeOnly;

    private boolean oneWay;

    private java.lang.String availableDate;

    private java.lang.String conclusionDate;

    private java.lang.Long secondsToAvailability;

    private java.lang.Long secondsToSubmission;

    private java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.statement.dto.StatementQuestionDto> questions = new java.util.ArrayList<>();

    private java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.statement.dto.StatementAnswerDto> answers = new java.util.ArrayList<>();

    public StatementQuizDto() {
    }

    public StatementQuizDto(pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer quizAnswer) {
        this.id = quizAnswer.getQuiz().getId();
        this.quizAnswerId = quizAnswer.getId();
        this.title = quizAnswer.getQuiz().getTitle();
        this.qrCodeOnly = quizAnswer.getQuiz().isQrCodeOnly();
        this.oneWay = quizAnswer.getQuiz().isOneWay();
        if (quizAnswer.getQuiz().getAvailableDate() != null) {
            this.availableDate = quizAnswer.getQuiz().getAvailableDate().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        }
        if (quizAnswer.getQuiz().getConclusionDate() != null) {
            this.conclusionDate = quizAnswer.getQuiz().getConclusionDate().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            if (quizAnswer.getQuiz().getType().equals(pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz.QuizType.IN_CLASS)) {
                this.secondsToSubmission = java.time.temporal.ChronoUnit.SECONDS.between(java.time.LocalDateTime.now(), quizAnswer.getQuiz().getConclusionDate());
            }
        }
        this.questions = quizAnswer.getQuestionAnswers().stream().map(pt.ulisboa.tecnico.socialsoftware.tutor.statement.dto.StatementQuestionDto::new).sorted(java.util.Comparator.comparing(pt.ulisboa.tecnico.socialsoftware.tutor.statement.dto.StatementQuestionDto::getSequence)).collect(java.util.stream.Collectors.toList());
        this.answers = quizAnswer.getQuestionAnswers().stream().map(pt.ulisboa.tecnico.socialsoftware.tutor.statement.dto.StatementAnswerDto::new).sorted(java.util.Comparator.comparing(pt.ulisboa.tecnico.socialsoftware.tutor.statement.dto.StatementAnswerDto::getSequence)).collect(java.util.stream.Collectors.toList());
    }

    public java.lang.Integer getId() {
        return id;
    }

    public void setId(java.lang.Integer id) {
        this.id = id;
    }

    public java.lang.Integer getQuizAnswerId() {
        return quizAnswerId;
    }

    public void setQuizAnswerId(java.lang.Integer quizAnswerId) {
        this.quizAnswerId = quizAnswerId;
    }

    public java.lang.String getTitle() {
        return title;
    }

    public void setTitle(java.lang.String title) {
        this.title = title;
    }

    public boolean isQrCodeOnly() {
        return qrCodeOnly;
    }

    public void setQrCodeOnly(boolean qrCodeOnly) {
        this.qrCodeOnly = qrCodeOnly;
    }

    public boolean isOneWay() {
        return oneWay;
    }

    public void setOneWay(boolean oneWay) {
        this.oneWay = oneWay;
    }

    public java.lang.String getAvailableDate() {
        return availableDate;
    }

    public void setAvailableDate(java.lang.String availableDate) {
        this.availableDate = availableDate;
    }

    public java.lang.String getConclusionDate() {
        return conclusionDate;
    }

    public void setConclusionDate(java.lang.String conclusionDate) {
        this.conclusionDate = conclusionDate;
    }

    public java.lang.Long getSecondsToAvailability() {
        return secondsToAvailability;
    }

    public void setSecondsToAvailability(java.lang.Long secondsToAvailability) {
        this.secondsToAvailability = secondsToAvailability;
    }

    public java.lang.Long getSecondsToSubmission() {
        return secondsToSubmission;
    }

    public void setSecondsToSubmission(java.lang.Long secondsToSubmission) {
        this.secondsToSubmission = secondsToSubmission;
    }

    public java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.statement.dto.StatementQuestionDto> getQuestions() {
        return questions;
    }

    public void setQuestions(java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.statement.dto.StatementQuestionDto> questions) {
        this.questions = questions;
    }

    public java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.statement.dto.StatementAnswerDto> getAnswers() {
        return answers;
    }

    public void setAnswers(java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.statement.dto.StatementAnswerDto> answers) {
        this.answers = answers;
    }

    @java.lang.Override
    public java.lang.String toString() {
        return ((((((((((((((((((((("StatementQuizDto{" + "id=") + id) + ", quizAnswerId=") + quizAnswerId) + ", title='") + title) + '\'') + ", availableDate='") + availableDate) + '\'') + ", conclusionDate='") + conclusionDate) + '\'') + ", secondsToAvailability=") + secondsToAvailability) + ", secondsToSubmission=") + secondsToSubmission) + ", questions=") + questions) + ", answers=") + answers) + '}';
    }
}