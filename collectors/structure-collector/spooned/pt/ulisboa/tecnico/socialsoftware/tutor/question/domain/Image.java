package pt.ulisboa.tecnico.socialsoftware.tutor.question.domain;
import javax.persistence.*;
@pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Entity
@pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Table(name = "images")
public class Image implements pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.DomainEntity {
    @pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Id
    @pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.GeneratedValue(strategy = GenerationType.IDENTITY)
    private java.lang.Integer id;

    private java.lang.String url;

    private java.lang.Integer width;

    @pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.OneToOne
    @pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.JoinColumn(name = "question_id")
    private pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question question;

    public Image() {
    }

    public Image(pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.ImageDto imageDto) {
        this.url = imageDto.getUrl();
        this.width = imageDto.getWidth();
    }

    @java.lang.Override
    public void accept(pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.Visitor visitor) {
        visitor.visitImage(this);
    }

    public java.lang.Integer getId() {
        return id;
    }

    public void setId(java.lang.Integer id) {
        this.id = id;
    }

    public pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question getQuestion() {
        return question;
    }

    public void setQuestion(pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question question) {
        this.question = question;
    }

    public java.lang.String getUrl() {
        return url;
    }

    public void setUrl(java.lang.String url) {
        this.url = url;
    }

    public java.lang.Integer getWidth() {
        return width;
    }

    public void setWidth(java.lang.Integer width) {
        this.width = width;
    }

    @java.lang.Override
    public java.lang.String toString() {
        return ((((((("Image{" + "id=") + id) + ", url='") + url) + '\'') + ", width=") + width) + '}';
    }
}