package pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain;
import javax.persistence.*;
@pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.Entity
@pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.Table(name = "quiz_answers")
public class QuizAnswer implements pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.DomainEntity {
    @pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.Id
    @pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.GeneratedValue(strategy = GenerationType.IDENTITY)
    private java.lang.Integer id;

    @pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.Column(name = "creation_date")
    private java.time.LocalDateTime creationDate;

    @pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.Column(name = "answer_date")
    private java.time.LocalDateTime answerDate;

    private boolean completed;

    @pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.Column(columnDefinition = "boolean default false")
    private boolean usedInStatistics;

    @pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.ManyToOne(fetch = FetchType.LAZY)
    @pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.JoinColumn(name = "user_id")
    private pt.ulisboa.tecnico.socialsoftware.tutor.user.User user;

    @pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.ManyToOne(fetch = FetchType.LAZY)
    @pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.JoinColumn(name = "quiz_id")
    private pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz quiz;

    @pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.OneToMany(cascade = CascadeType.ALL, mappedBy = "quizAnswer", fetch = FetchType.LAZY, orphanRemoval = true)
    private java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswer> questionAnswers = new java.util.ArrayList<>();

    public QuizAnswer() {
    }

    public QuizAnswer(pt.ulisboa.tecnico.socialsoftware.tutor.user.User user, pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz quiz) {
        this.completed = false;
        this.usedInStatistics = false;
        this.user = user;
        user.addQuizAnswer(this);
        this.quiz = quiz;
        quiz.addQuizAnswer(this);
        java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion> quizQuestions = new java.util.ArrayList<>(quiz.getQuizQuestions());
        if (quiz.getScramble()) {
            java.util.Collections.shuffle(quizQuestions);
        }
        for (int i = 0; i < quizQuestions.size(); i++) {
            new pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswer(this, quizQuestions.get(i), i);
        }
    }

    @java.lang.Override
    public void accept(pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.Visitor visitor) {
        visitor.visitQuizAnswer(this);
    }

    public java.lang.Integer getId() {
        return id;
    }

    public void setId(java.lang.Integer id) {
        this.id = id;
    }

    public java.time.LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(java.time.LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public java.time.LocalDateTime getAnswerDate() {
        return answerDate;
    }

    public void setAnswerDate(java.time.LocalDateTime answerDate) {
        this.answerDate = answerDate;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public boolean isUsedInStatistics() {
        return usedInStatistics;
    }

    public void setUsedInStatistics(boolean usedInStatistics) {
        this.usedInStatistics = usedInStatistics;
    }

    public pt.ulisboa.tecnico.socialsoftware.tutor.user.User getUser() {
        return user;
    }

    public void setUser(pt.ulisboa.tecnico.socialsoftware.tutor.user.User user) {
        this.user = user;
    }

    public pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz getQuiz() {
        return quiz;
    }

    public void setQuiz(pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz quiz) {
        this.quiz = quiz;
    }

    public void setQuestionAnswers(java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswer> questionAnswers) {
        this.questionAnswers = questionAnswers;
    }

    public java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswer> getQuestionAnswers() {
        if (questionAnswers == null) {
            questionAnswers = new java.util.ArrayList<>();
        }
        return questionAnswers;
    }

    public void addQuestionAnswer(pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswer questionAnswer) {
        if (questionAnswers == null) {
            questionAnswers = new java.util.ArrayList<>();
        }
        questionAnswers.add(questionAnswer);
    }

    public void remove() {
        user.getQuizAnswers().remove(this);
        user = null;
        quiz.getQuizAnswers().remove(this);
        quiz = null;
        questionAnswers.clear();
    }

    public boolean canResultsBePublic(pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution courseExecution) {
        return (isCompleted() && (getQuiz().getCourseExecution() == courseExecution)) && (!(getQuiz().getType().equals(pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz.QuizType.IN_CLASS) && getQuiz().getConclusionDate().isAfter(java.time.LocalDateTime.now())));
    }

    public void calculateStatistics() {
        if (!this.usedInStatistics) {
            user.increaseNumberOfQuizzes(getQuiz().getType());
            getQuestionAnswers().forEach(questionAnswer -> {
                user.increaseNumberOfAnswers(getQuiz().getType());
                if ((questionAnswer.getOption() != null) && questionAnswer.getOption().getCorrect()) {
                    user.increaseNumberOfCorrectAnswers(getQuiz().getType());
                }
            });
            getQuestionAnswers().forEach(questionAnswer -> questionAnswer.getQuizQuestion().getQuestion().addAnswerStatistics(questionAnswer));
            this.usedInStatistics = true;
        }
    }
}