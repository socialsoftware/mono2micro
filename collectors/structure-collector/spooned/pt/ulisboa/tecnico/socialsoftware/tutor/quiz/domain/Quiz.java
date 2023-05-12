package pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain;
import javax.persistence.*;
@pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Entity
@pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Table(name = "quizzes", indexes = { @pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Index(name = "quizzes_indx_0", columnList = "key") })
public class Quiz implements pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.DomainEntity {
    public enum QuizType {

        EXAM,
        TEST,
        GENERATED,
        PROPOSED,
        IN_CLASS;}

    @pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Id
    @pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.GeneratedValue(strategy = GenerationType.IDENTITY)
    private java.lang.Integer id;

    @pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Column(unique = true, nullable = false)
    private java.lang.Integer key;

    @pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Column(name = "creation_date")
    private java.time.LocalDateTime creationDate;

    @pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Column(name = "available_date")
    private java.time.LocalDateTime availableDate;

    @pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Column(name = "conclusion_date")
    private java.time.LocalDateTime conclusionDate;

    @pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Column(columnDefinition = "boolean default false")
    private boolean scramble = false;

    @pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Column(columnDefinition = "boolean default false")
    private boolean qrCodeOnly = false;

    @pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Column(columnDefinition = "boolean default false")
    private boolean oneWay = false;

    @pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Column(nullable = false)
    private java.lang.String title = "Title";

    @pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Enumerated(EnumType.STRING)
    private pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz.QuizType type;

    private java.lang.Integer series;

    private java.lang.String version;

    @pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.OneToMany(cascade = CascadeType.ALL, mappedBy = "quiz", fetch = FetchType.LAZY, orphanRemoval = true)
    private java.util.Set<pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion> quizQuestions = new java.util.HashSet<>();

    @pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.OneToMany(cascade = CascadeType.ALL, mappedBy = "quiz", fetch = FetchType.LAZY, orphanRemoval = true)
    private java.util.Set<pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer> quizAnswers = new java.util.HashSet<>();

    @pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.ManyToOne
    @pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.JoinColumn(name = "course_execution_id")
    private pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution courseExecution;

    public Quiz() {
    }

    public Quiz(pt.ulisboa.tecnico.socialsoftware.tutor.quiz.dto.QuizDto quizDto) {
        checkQuestions(quizDto.getQuestions());
        this.key = quizDto.getKey();
        setTitle(quizDto.getTitle());
        this.type = quizDto.getType();
        this.scramble = quizDto.isScramble();
        this.qrCodeOnly = quizDto.isQrCodeOnly();
        this.oneWay = quizDto.isOneWay();
        this.creationDate = quizDto.getCreationDateDate();
        setAvailableDate(quizDto.getAvailableDateDate());
        setConclusionDate(quizDto.getConclusionDateDate());
        this.series = quizDto.getSeries();
        this.version = quizDto.getVersion();
    }

    @java.lang.Override
    public void accept(pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.Visitor visitor) {
        visitor.visitQuiz(this);
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

    public boolean getScramble() {
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

    public void setOneWay(boolean noBack) {
        this.oneWay = noBack;
    }

    public java.lang.String getTitle() {
        return title;
    }

    public void setTitle(java.lang.String title) {
        checkTitle(title);
        this.title = title;
    }

    public java.time.LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(java.time.LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public java.time.LocalDateTime getAvailableDate() {
        return availableDate;
    }

    public void setAvailableDate(java.time.LocalDateTime availableDate) {
        checkAvailableDate(availableDate);
        this.availableDate = availableDate;
    }

    public java.time.LocalDateTime getConclusionDate() {
        return conclusionDate;
    }

    public void setConclusionDate(java.time.LocalDateTime conclusionDate) {
        checkConclusionDate(conclusionDate);
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

    public java.util.Set<pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion> getQuizQuestions() {
        return quizQuestions;
    }

    public java.util.Set<pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer> getQuizAnswers() {
        return quizAnswers;
    }

    public pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution getCourseExecution() {
        return courseExecution;
    }

    public void setCourseExecution(pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution courseExecution) {
        this.courseExecution = courseExecution;
        courseExecution.addQuiz(this);
    }

    public void addQuizQuestion(pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion quizQuestion) {
        this.quizQuestions.add(quizQuestion);
    }

    public void addQuizAnswer(pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer quizAnswer) {
        this.quizAnswers.add(quizAnswer);
    }

    @java.lang.Override
    public java.lang.String toString() {
        return (((((((((((((((((((((("Quiz{" + "id=") + id) + ", creationDate=") + creationDate) + ", availableDate=") + availableDate) + ", conclusionDate=") + conclusionDate) + ", scramble=") + scramble) + ", title='") + title) + '\'') + ", type=") + type) + ", id=") + id) + ", series=") + series) + ", version='") + version) + '\'') + '}';
    }

    private void checkTitle(java.lang.String title) {
        if ((title == null) || (title.trim().length() == 0)) {
            throw new pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.QUIZ_NOT_CONSISTENT, "Title");
        }
    }

    private void checkAvailableDate(java.time.LocalDateTime availableDate) {
        if (this.type.equals(pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz.QuizType.PROPOSED) && (availableDate == null)) {
            throw new pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.QUIZ_NOT_CONSISTENT, "Available date");
        }
        if (((this.type.equals(pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz.QuizType.PROPOSED) && (this.availableDate != null)) && (this.conclusionDate != null)) && conclusionDate.isBefore(availableDate)) {
            throw new pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.QUIZ_NOT_CONSISTENT, "Available date");
        }
    }

    private void checkConclusionDate(java.time.LocalDateTime conclusionDate) {
        if (((this.type.equals(pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz.QuizType.PROPOSED) && (conclusionDate != null)) && (availableDate != null)) && conclusionDate.isBefore(availableDate)) {
            throw new pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.QUIZ_NOT_CONSISTENT, ("Conclusion date " + conclusionDate) + availableDate);
        }
    }

    private void checkQuestions(java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto> questions) {
        if (questions != null) {
            for (pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto questionDto : questions) {
                if (questionDto.getSequence() != (questions.indexOf(questionDto) + 1)) {
                    throw new pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.QUIZ_NOT_CONSISTENT, "sequence of questions not correct");
                }
            }
        }
    }

    public void remove() {
        checkCanChange();
        courseExecution.getQuizzes().remove(this);
        courseExecution = null;
    }

    public void checkCanChange() {
        if (!quizAnswers.isEmpty()) {
            throw new pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.QUIZ_HAS_ANSWERS);
        }
        getQuizQuestions().forEach(pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion::checkCanRemove);
    }

    public void generate(java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question> questions) {
        java.util.stream.IntStream.range(0, questions.size()).forEach(index -> new pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion(this, questions.get(index), index));
        this.setAvailableDate(java.time.LocalDateTime.now());
        this.setCreationDate(java.time.LocalDateTime.now());
        this.setType(pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz.QuizType.GENERATED);
        this.title = "Generated Quiz";
    }
}