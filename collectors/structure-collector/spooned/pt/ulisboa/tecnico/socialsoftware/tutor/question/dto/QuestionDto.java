package pt.ulisboa.tecnico.socialsoftware.tutor.question.dto;
public class QuestionDto implements java.io.Serializable {
    private java.lang.Integer id;

    private java.lang.Integer key;

    private java.lang.String title;

    private java.lang.String content;

    private java.lang.Integer difficulty;

    private int numberOfAnswers = 0;

    private int numberOfGeneratedQuizzes = 0;

    private int numberOfNonGeneratedQuizzes = 0;

    private int numberOfCorrect;

    private java.lang.String creationDate = null;

    private java.lang.String status;

    private java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OptionDto> options = new java.util.ArrayList<>();

    private pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.ImageDto image;

    private java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto> topics = new java.util.ArrayList<>();

    private java.lang.Integer sequence;

    public QuestionDto() {
    }

    public QuestionDto(pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question question) {
        this.id = question.getId();
        this.title = question.getTitle();
        this.content = question.getContent();
        this.difficulty = question.getDifficulty();
        this.numberOfAnswers = question.getNumberOfAnswers();
        if (!question.getQuizQuestions().isEmpty()) {
            this.numberOfGeneratedQuizzes = ((int) (question.getQuizQuestions().stream().map(pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion::getQuiz).filter(quiz -> quiz.getType().equals(pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz.QuizType.GENERATED)).count()));
        }
        this.numberOfNonGeneratedQuizzes = question.getQuizQuestions().size() - this.numberOfGeneratedQuizzes;
        this.numberOfCorrect = question.getNumberOfCorrect();
        this.status = question.getStatus().name();
        this.options = question.getOptions().stream().map(pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OptionDto::new).collect(java.util.stream.Collectors.toList());
        this.topics = question.getTopics().stream().sorted(java.util.Comparator.comparing(pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic::getName)).map(pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto::new).collect(java.util.stream.Collectors.toList());
        if (question.getImage() != null)
            this.image = new pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.ImageDto(question.getImage());

        if (question.getCreationDate() != null)
            this.creationDate = question.getCreationDate().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

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

    public java.lang.String getTitle() {
        return title;
    }

    public void setTitle(java.lang.String title) {
        this.title = title;
    }

    public java.lang.String getContent() {
        return content;
    }

    public void setContent(java.lang.String content) {
        this.content = content;
    }

    public java.lang.Integer getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(java.lang.Integer difficulty) {
        this.difficulty = difficulty;
    }

    public int getNumberOfAnswers() {
        return numberOfAnswers;
    }

    public void setNumberOfAnswers(int numberOfAnswers) {
        this.numberOfAnswers = numberOfAnswers;
    }

    public int getNumberOfGeneratedQuizzes() {
        return numberOfGeneratedQuizzes;
    }

    public void setNumberOfGeneratedQuizzes(int numberOfGeneratedQuizzes) {
        this.numberOfGeneratedQuizzes = numberOfGeneratedQuizzes;
    }

    public int getNumberOfNonGeneratedQuizzes() {
        return numberOfNonGeneratedQuizzes;
    }

    public void setNumberOfNonGeneratedQuizzes(int numberOfNonGeneratedQuizzes) {
        this.numberOfNonGeneratedQuizzes = numberOfNonGeneratedQuizzes;
    }

    public int getNumberOfCorrect() {
        return numberOfCorrect;
    }

    public void setNumberOfCorrect(int numberOfCorrect) {
        this.numberOfCorrect = numberOfCorrect;
    }

    public java.lang.String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(java.lang.String creationDate) {
        this.creationDate = creationDate;
    }

    public java.lang.String getStatus() {
        return status;
    }

    public void setStatus(java.lang.String status) {
        this.status = status;
    }

    public java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OptionDto> getOptions() {
        return options;
    }

    public void setOptions(java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OptionDto> options) {
        this.options = options;
    }

    public pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.ImageDto getImage() {
        return image;
    }

    public void setImage(pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.ImageDto image) {
        this.image = image;
    }

    public java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto> getTopics() {
        return topics;
    }

    public void setTopics(java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto> topics) {
        this.topics = topics;
    }

    public java.lang.Integer getSequence() {
        return sequence;
    }

    public void setSequence(java.lang.Integer sequence) {
        this.sequence = sequence;
    }

    @java.lang.Override
    public java.lang.String toString() {
        return (((((((((((((((((((((((((((((((((("QuestionDto{" + "id=") + id) + ", key=") + key) + ", title='") + title) + '\'') + ", content='") + content) + '\'') + ", difficulty=") + difficulty) + ", numberOfAnswers=") + numberOfAnswers) + ", numberOfGeneratedQuizzes=") + numberOfGeneratedQuizzes) + ", numberOfNonGeneratedQuizzes=") + numberOfNonGeneratedQuizzes) + ", numberOfCorrect=") + numberOfCorrect) + ", creationDate='") + creationDate) + '\'') + ", status='") + status) + '\'') + ", options=") + options) + ", image=") + image) + ", topics=") + topics) + ", sequence=") + sequence) + '}';
    }
}