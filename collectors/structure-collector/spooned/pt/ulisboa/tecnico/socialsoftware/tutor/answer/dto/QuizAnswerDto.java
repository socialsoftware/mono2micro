package pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto;
public class QuizAnswerDto implements java.io.Serializable {
    private java.lang.Integer id;

    private java.lang.String name;

    private java.lang.String username;

    private java.lang.String creationDate;

    private java.lang.String answerDate;

    private java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.QuestionAnswerDto> questionAnswers = new java.util.ArrayList<>();

    public QuizAnswerDto() {
    }

    public QuizAnswerDto(pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer quizAnswer) {
        this.id = quizAnswer.getId();
        this.username = quizAnswer.getUser().getUsername();
        this.name = quizAnswer.getUser().getName();
        if (quizAnswer.getAnswerDate() != null)
            this.answerDate = quizAnswer.getAnswerDate().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        if (quizAnswer.getAnswerDate() != null)
            this.creationDate = quizAnswer.getCreationDate().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        this.questionAnswers = quizAnswer.getQuestionAnswers().stream().sorted(java.util.Comparator.comparing(qa -> qa.getQuizQuestion().getSequence())).map(pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.QuestionAnswerDto::new).collect(java.util.stream.Collectors.toList());
    }

    public java.lang.Integer getId() {
        return id;
    }

    public void setId(java.lang.Integer id) {
        this.id = id;
    }

    public java.lang.String getName() {
        return name;
    }

    public void setName(java.lang.String name) {
        this.name = name;
    }

    public java.lang.String getUsername() {
        return username;
    }

    public void setUsername(java.lang.String username) {
        this.username = username;
    }

    public java.lang.String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(java.lang.String creationDate) {
        this.creationDate = creationDate;
    }

    public java.lang.String getAnswerDate() {
        return answerDate;
    }

    public void setAnswerDate(java.lang.String answerDate) {
        this.answerDate = answerDate;
    }

    public java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.QuestionAnswerDto> getQuestionAnswers() {
        return questionAnswers;
    }

    public void setQuestionAnswers(java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.QuestionAnswerDto> questionAnswers) {
        this.questionAnswers = questionAnswers;
    }

    public void addQuestionAnswer(pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.QuestionAnswerDto questionAnswerDto) {
        this.questionAnswers.add(questionAnswerDto);
    }
}