package pt.ulisboa.tecnico.socialsoftware.tutor.question.domain;
import javax.persistence.*;
@pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Entity
@pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Table(name = "options")
public class Option implements pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.DomainEntity {
    @pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Id
    @pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.GeneratedValue(strategy = GenerationType.IDENTITY)
    private java.lang.Integer id;

    private java.lang.Integer sequence;

    @pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Column(columnDefinition = "boolean default false")
    private boolean correct;

    @pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Column(columnDefinition = "TEXT")
    private java.lang.String content;

    @pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.ManyToOne(fetch = FetchType.LAZY)
    @pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.JoinColumn(name = "question_id")
    private pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question question;

    @pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.OneToMany(cascade = CascadeType.ALL, mappedBy = "quizAnswer", orphanRemoval = true)
    private java.util.Set<pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswer> questionAnswers = new java.util.HashSet<>();

    public Option() {
    }

    public Option(pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OptionDto option) {
        this.sequence = option.getSequence();
        this.content = option.getContent();
        this.correct = option.getCorrect();
    }

    @java.lang.Override
    public void accept(pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.Visitor visitor) {
        visitor.visitOption(this);
    }

    public java.lang.Integer getId() {
        return id;
    }

    public void setId(java.lang.Integer id) {
        this.id = id;
    }

    public java.lang.Integer getSequence() {
        if (sequence == null) {
            getQuestion().setOptionsSequence();
        }
        return sequence;
    }

    public void setSequence(java.lang.Integer sequence) {
        this.sequence = sequence;
    }

    public boolean getCorrect() {
        return correct;
    }

    public void setCorrect(boolean correct) {
        this.correct = correct;
    }

    public java.lang.String getContent() {
        return content;
    }

    public void setContent(java.lang.String content) {
        this.content = content;
    }

    public pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question getQuestion() {
        return question;
    }

    public void setQuestion(pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question question) {
        this.question = question;
    }

    public java.util.Set<pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswer> getQuestionAnswers() {
        return questionAnswers;
    }

    public void addQuestionAnswer(pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswer questionAnswer) {
        questionAnswers.add(questionAnswer);
    }

    @java.lang.Override
    public java.lang.String toString() {
        return ((((((((("Option{" + "id=") + id) + ", sequence=") + sequence) + ", correct=") + correct) + ", content='") + content) + '\'') + '}';
    }

    public void remove() {
        this.question = null;
        this.questionAnswers.clear();
    }
}