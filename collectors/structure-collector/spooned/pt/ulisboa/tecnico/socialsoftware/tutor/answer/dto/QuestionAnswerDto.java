package pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto;
public class QuestionAnswerDto implements java.io.Serializable {
    private pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto question;

    private pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OptionDto option;

    public QuestionAnswerDto() {
    }

    public QuestionAnswerDto(pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswer questionAnswer) {
        this.question = new pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto(questionAnswer.getQuizQuestion().getQuestion());
        if (questionAnswer.getOption() != null)
            this.option = new pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OptionDto(questionAnswer.getOption());

    }

    public pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto getQuestion() {
        return question;
    }

    public void setQuestion(pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto question) {
        this.question = question;
    }

    public pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OptionDto getOption() {
        return option;
    }

    public void setOption(pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OptionDto option) {
        this.option = option;
    }
}