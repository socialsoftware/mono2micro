package pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain;
import javax.persistence.*;
@pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Entity
@pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Table(name = "quiz_questions")
public class QuizQuestion implements pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.DomainEntity {
    @pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Id
    @pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.GeneratedValue(strategy = GenerationType.IDENTITY)
    private java.lang.Integer id;

    @pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.ManyToOne
    @pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.JoinColumn(name = "quiz_id")
    private pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz quiz;

    @pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.ManyToOne
    @pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.JoinColumn(name = "question_id")
    private pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question question;

    @pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.OneToMany(cascade = CascadeType.ALL, mappedBy = "quizQuestion", fetch = FetchType.LAZY, orphanRemoval = true)
    private java.util.Set<pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswer> questionAnswers = new java.util.HashSet<>();

    private java.lang.Integer sequence;

    public QuizQuestion() {
    }

    public QuizQuestion(pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz quiz, pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question question, java.lang.Integer sequence) {
        this.quiz = quiz;
        this.quiz.addQuizQuestion(this);
        this.question = question;
        question.addQuizQuestion(this);
        this.sequence = sequence;
    }

    @java.lang.Override
    public void accept(pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.Visitor visitor) {
        visitor.visitQuizQuestion(this);
    }

    public pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz getQuiz() {
        return quiz;
    }

    public void setQuiz(pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz quiz) {
        this.quiz = quiz;
    }

    public pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question getQuestion() {
        return question;
    }

    public void setQuestion(pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question question) {
        this.question = question;
    }

    public java.lang.Integer getSequence() {
        return sequence;
    }

    public java.lang.Integer getId() {
        return id;
    }

    public void setId(java.lang.Integer id) {
        this.id = id;
    }

    public java.util.Set<pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswer> getQuestionAnswers() {
        return questionAnswers;
    }

    public void setSequence(java.lang.Integer sequence) {
        this.sequence = sequence;
    }

    public void addQuestionAnswer(pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswer questionAnswer) {
        questionAnswers.add(questionAnswer);
    }

    @java.lang.Override
    public java.lang.String toString() {
        return (("QuizQuestion{" + "id=") + id) + '}';
    }

    public void remove() {
        this.quiz.getQuizQuestions().remove(this);
        quiz = null;
        this.question.getQuizQuestions().remove(this);
        question = null;
    }

    void checkCanRemove() {
        if (!questionAnswers.isEmpty()) {
            throw new pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.QUIZ_QUESTION_HAS_ANSWERS);
        }
    }
}