package pt.ulisboa.tecnico.socialsoftware.tutor.quiz.dto;
import org.springframework.data.annotation.Transient;
public class QuizDto implements java.io.Serializable {
    private java.lang.Integer id;

    private java.lang.Integer key;

    private boolean scramble;

    private boolean qrCodeOnly;

    private boolean oneWay;

    private java.lang.String title;

    private java.lang.String creationDate = null;

    private java.lang.String availableDate = null;

    private java.lang.String conclusionDate = null;

    private pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz.QuizType type;

    private java.lang.Integer series;

    private java.lang.String version;

    private int numberOfQuestions;

    private int numberOfAnswers;

    private java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto> questions = new java.util.ArrayList<>();

    @org.springframework.data.annotation.Transient
    private static final java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public QuizDto() {
    }

    public QuizDto(pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz quiz, boolean deepCopy) {
        this.id = quiz.getId();
        this.key = quiz.getKey();
        this.scramble = quiz.getScramble();
        this.qrCodeOnly = quiz.isQrCodeOnly();
        this.oneWay = quiz.isOneWay();
        this.title = quiz.getTitle();
        this.type = quiz.getType();
        this.series = quiz.getSeries();
        this.version = quiz.getVersion();
        this.numberOfQuestions = quiz.getQuizQuestions().size();
        this.numberOfAnswers = quiz.getQuizAnswers().size();
        if (quiz.getCreationDate() != null)
            this.creationDate = quiz.getCreationDate().format(pt.ulisboa.tecnico.socialsoftware.tutor.quiz.dto.QuizDto.formatter);

        if (quiz.getAvailableDate() != null)
            this.availableDate = quiz.getAvailableDate().format(pt.ulisboa.tecnico.socialsoftware.tutor.quiz.dto.QuizDto.formatter);

        if (quiz.getConclusionDate() != null)
            this.conclusionDate = quiz.getConclusionDate().format(pt.ulisboa.tecnico.socialsoftware.tutor.quiz.dto.QuizDto.formatter);

        if (deepCopy) {
            this.questions = quiz.getQuizQuestions().stream().sorted(java.util.Comparator.comparing(pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion::getSequence)).map(quizQuestion -> {
                pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto questionDto = new pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto(quizQuestion.getQuestion());
                questionDto.setSequence(quizQuestion.getSequence());
                return questionDto;
            }).collect(java.util.stream.Collectors.toList());
        }
    }

    public java.lang.Integer getId() {
        return id;
    }

    public void setId(java.lang.Integer id) {
        this.id = id;
    }

    public java.lang.Integer getKey() {
        return key;
    }

    public void setKey(java.lang.Integer key) {
        this.key = key;
    }

    public boolean isScramble() {
        return scramble;
    }

    public void setScramble(boolean scramble) {
        this.scramble = scramble;
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

    public java.lang.String getTitle() {
        return title;
    }

    public void setTitle(java.lang.String title) {
        this.title = title;
    }

    public java.lang.String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(java.lang.String creationDate) {
        this.creationDate = creationDate;
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

    public pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz.QuizType getType() {
        return type;
    }

    public void setType(pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz.QuizType type) {
        this.type = type;
    }

    public java.lang.Integer getSeries() {
        return series;
    }

    public void setSeries(java.lang.Integer series) {
        this.series = series;
    }

    public java.lang.String getVersion() {
        return version;
    }

    public void setVersion(java.lang.String version) {
        this.version = version;
    }

    public int getNumberOfQuestions() {
        return numberOfQuestions;
    }

    public void setNumberOfQuestions(int numberOfQuestions) {
        this.numberOfQuestions = numberOfQuestions;
    }

    public int getNumberOfAnswers() {
        return numberOfAnswers;
    }

    public void setNumberOfAnswers(int numberOfAnswers) {
        this.numberOfAnswers = numberOfAnswers;
    }

    public java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto> getQuestions() {
        return questions;
    }

    public void setQuestions(java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto> questions) {
        this.questions = questions;
    }

    public java.time.LocalDateTime getCreationDateDate() {
        if ((getCreationDate() == null) || getCreationDate().isEmpty()) {
            return null;
        }
        return java.time.LocalDateTime.parse(getCreationDate(), pt.ulisboa.tecnico.socialsoftware.tutor.quiz.dto.QuizDto.formatter);
    }

    public java.time.LocalDateTime getAvailableDateDate() {
        if ((getAvailableDate() == null) || getAvailableDate().isEmpty()) {
            return null;
        }
        return java.time.LocalDateTime.parse(getAvailableDate(), pt.ulisboa.tecnico.socialsoftware.tutor.quiz.dto.QuizDto.formatter);
    }

    public java.time.LocalDateTime getConclusionDateDate() {
        if ((getConclusionDate() == null) || getConclusionDate().isEmpty()) {
            return null;
        }
        return java.time.LocalDateTime.parse(getConclusionDate(), pt.ulisboa.tecnico.socialsoftware.tutor.quiz.dto.QuizDto.formatter);
    }

    @java.lang.Override
    public java.lang.String toString() {
        return ((((((((((((((((((((((((((((((((("QuizDto{" + "id=") + id) + ", id=") + id) + ", scramble=") + scramble) + ", title='") + title) + '\'') + ", creationDate='") + creationDate) + '\'') + ", availableDate='") + availableDate) + '\'') + ", conclusionDate='") + conclusionDate) + '\'') + ", type=") + type) + ", series=") + series) + ", version='") + version) + '\'') + ", numberOfQuestions=") + numberOfQuestions) + ", numberOfAnswers=") + numberOfAnswers) + ", questions=") + questions) + ", formatter=") + pt.ulisboa.tecnico.socialsoftware.tutor.quiz.dto.QuizDto.formatter) + '}';
    }
}