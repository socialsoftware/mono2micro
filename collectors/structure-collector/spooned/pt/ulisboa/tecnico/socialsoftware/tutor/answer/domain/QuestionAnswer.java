package pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain;
import javax.persistence.*;
@pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.Entity
@pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.Table(name = "question_answers")
public class QuestionAnswer implements pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.DomainEntity {
    @pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.Id
    @pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.GeneratedValue(strategy = GenerationType.IDENTITY)
    private java.lang.Integer id;

    @pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.Column(name = "time_taken")
    private java.lang.Integer timeTaken;

    @pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.ManyToOne
    @pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.JoinColumn(name = "quiz_question_id")
    private pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion quizQuestion;

    @pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.ManyToOne
    @pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.JoinColumn(name = "quiz_answer_id")
    private pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer quizAnswer;

    @pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.ManyToOne
    @pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.JoinColumn(name = "option_id")
    private pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Option option;

    private java.lang.Integer sequence;

    public QuestionAnswer() {
    }

    public QuestionAnswer(pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer quizAnswer, pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion quizQuestion, java.lang.Integer timeTaken, pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Option option, int sequence) {
        this.timeTaken = timeTaken;
        this.quizAnswer = quizAnswer;
        quizAnswer.addQuestionAnswer(this);
        this.quizQuestion = quizQuestion;
        quizQuestion.addQuestionAnswer(this);
        this.option = option;
        if (option != null) {
            option.addQuestionAnswer(this);
        }
        this.sequence = sequence;
    }

    public QuestionAnswer(pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer quizAnswer, pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion quizQuestion, int sequence) {
        this.quizAnswer = quizAnswer;
        quizAnswer.addQuestionAnswer(this);
        this.quizQuestion = quizQuestion;
        quizQuestion.addQuestionAnswer(this);
        this.sequence = sequence;
    }

    public void remove() {
        quizAnswer = null;
        quizQuestion.getQuestionAnswers().remove(this);
        quizQuestion = null;
        if (option != null) {
            option.getQuestionAnswers().remove(this);
            option = null;
        }
    }

    @java.lang.Override
    public void accept(pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.Visitor visitor) {
        visitor.visitQuestionAnswer(this);
    }

    public java.lang.Integer getId() {
        return id;
    }

    public void setId(java.lang.Integer id) {
        this.id = id;
    }

    public java.lang.Integer getTimeTaken() {
        return timeTaken;
    }

    public void setTimeTaken(java.lang.Integer timeTaken) {
        this.timeTaken = timeTaken;
    }

    public pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion getQuizQuestion() {
        return quizQuestion;
    }

    public void setQuizQuestion(pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion quizQuestion) {
        this.quizQuestion = quizQuestion;
    }

    public pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer getQuizAnswer() {
        return quizAnswer;
    }

    public void setQuizAnswer(pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer quizAnswer) {
        this.quizAnswer = quizAnswer;
    }

    public pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Option getOption() {
        return option;
    }

    public void setOption(pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Option option) {
        this.option = option;
    }

    public java.lang.Integer getSequence() {
        return sequence;
    }

    public void setSequence(java.lang.Integer sequence) {
        this.sequence = sequence;
    }

    @java.lang.Override
    public java.lang.String toString() {
        return (((((("QuestionAnswer{" + "id=") + id) + ", timeTaken=") + timeTaken) + ", sequence=") + sequence) + '}';
    }

    public boolean isCorrect() {
        return (getOption() != null) && getOption().getCorrect();
    }
}