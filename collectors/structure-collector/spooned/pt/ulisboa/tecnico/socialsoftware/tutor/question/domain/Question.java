package pt.ulisboa.tecnico.socialsoftware.tutor.question.domain;
import javax.persistence.*;
@pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Entity
@pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Table(name = "questions", indexes = { @pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Index(name = "question_indx_0", columnList = "key") })
public class Question implements pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.DomainEntity {
    @java.lang.SuppressWarnings("unused")
    public enum Status {

        DISABLED,
        REMOVED,
        AVAILABLE;}

    @pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Id
    @pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.GeneratedValue(strategy = GenerationType.IDENTITY)
    private java.lang.Integer id;

    private java.lang.Integer key;

    @pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Column(columnDefinition = "TEXT")
    private java.lang.String content;

    private java.lang.String title;

    @pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Column(name = "number_of_answers", columnDefinition = "integer default 0")
    private java.lang.Integer numberOfAnswers = 0;

    @pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Column(name = "number_of_correct", columnDefinition = "integer default 0")
    private java.lang.Integer numberOfCorrect = 0;

    @pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Enumerated(EnumType.STRING)
    private pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question.Status status = pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question.Status.DISABLED;

    @pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.OneToOne(cascade = CascadeType.ALL, mappedBy = "question")
    private pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Image image;

    @pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Column(name = "creation_date")
    private java.time.LocalDateTime creationDate;

    @pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.OneToMany(cascade = CascadeType.ALL, mappedBy = "question", fetch = FetchType.EAGER, orphanRemoval = true)
    private java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Option> options = new java.util.ArrayList<>();

    @pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.OneToMany(cascade = CascadeType.ALL, mappedBy = "question", orphanRemoval = true)
    private java.util.Set<pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion> quizQuestions = new java.util.HashSet<>();

    @pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.ManyToMany(mappedBy = "questions")
    private java.util.Set<pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic> topics = new java.util.HashSet<>();

    @pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.ManyToOne
    @pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.JoinColumn(name = "course_id")
    private pt.ulisboa.tecnico.socialsoftware.tutor.course.Course course;

    public Question() {
    }

    public Question(pt.ulisboa.tecnico.socialsoftware.tutor.course.Course course, pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto questionDto) {
        checkConsistentQuestion(questionDto);
        this.title = questionDto.getTitle();
        this.key = questionDto.getKey();
        this.content = questionDto.getContent();
        this.status = pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question.Status.valueOf(questionDto.getStatus());
        this.creationDate = java.time.LocalDateTime.parse(questionDto.getCreationDate(), pt.ulisboa.tecnico.socialsoftware.tutor.course.Course.formatter);
        this.course = course;
        course.addQuestion(this);
        if (questionDto.getImage() != null) {
            pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Image img = new pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Image(questionDto.getImage());
            setImage(img);
            img.setQuestion(this);
        }
        int index = 0;
        for (pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OptionDto optionDto : questionDto.getOptions()) {
            optionDto.setSequence(index++);
            pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Option option = new pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Option(optionDto);
            this.options.add(option);
            option.setQuestion(this);
        }
    }

    @java.lang.Override
    public void accept(pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.Visitor visitor) {
        visitor.visitQuestion(this);
    }

    public java.lang.Integer getId() {
        return id;
    }

    public void setId(java.lang.Integer id) {
        this.id = id;
    }

    public java.lang.Integer getKey() {
        if (this.key == null)
            generateKeys();

        return key;
    }

    private void generateKeys() {
        java.lang.Integer max = this.course.getQuestions().stream().filter(question -> question.key != null).map(pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question::getKey).max(java.util.Comparator.comparing(java.lang.Integer::valueOf)).orElse(0);
        java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question> nullKeyQuestions = this.course.getQuestions().stream().filter(question -> question.key == null).collect(java.util.stream.Collectors.toList());
        for (pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question question : nullKeyQuestions) {
            max = max + 1;
            question.key = max;
        }
    }

    public void setKey(java.lang.Integer key) {
        this.key = key;
    }

    public java.lang.String getContent() {
        return content;
    }

    public void setContent(java.lang.String content) {
        this.content = content;
    }

    public pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question.Status getStatus() {
        return status;
    }

    public void setStatus(pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question.Status status) {
        this.status = status;
    }

    public java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Option> getOptions() {
        return options;
    }

    public pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Image getImage() {
        return image;
    }

    public void setImage(pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Image image) {
        this.image = image;
        image.setQuestion(this);
    }

    public java.lang.String getTitle() {
        return title;
    }

    public void setTitle(java.lang.String title) {
        this.title = title;
    }

    public java.util.Set<pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion> getQuizQuestions() {
        return quizQuestions;
    }

    public java.lang.Integer getNumberOfAnswers() {
        return numberOfAnswers;
    }

    public void setNumberOfAnswers(java.lang.Integer numberOfAnswers) {
        this.numberOfAnswers = numberOfAnswers;
    }

    public java.lang.Integer getNumberOfCorrect() {
        return numberOfCorrect;
    }

    public void setNumberOfCorrect(java.lang.Integer numberOfCorrect) {
        this.numberOfCorrect = numberOfCorrect;
    }

    public java.util.Set<pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic> getTopics() {
        return topics;
    }

    public java.time.LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(java.time.LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public pt.ulisboa.tecnico.socialsoftware.tutor.course.Course getCourse() {
        return course;
    }

    public void setCourse(pt.ulisboa.tecnico.socialsoftware.tutor.course.Course course) {
        this.course = course;
    }

    public void addOption(pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Option option) {
        options.add(option);
    }

    public void addQuizQuestion(pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion quizQuestion) {
        quizQuestions.add(quizQuestion);
    }

    public void addTopic(pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic topic) {
        topics.add(topic);
    }

    public void remove() {
        canRemove();
        getCourse().getQuestions().remove(this);
        course = null;
        getTopics().forEach(topic -> topic.getQuestions().remove(this));
        getTopics().clear();
    }

    @java.lang.Override
    public java.lang.String toString() {
        return (((((((((((((((((((((("Question{" + "id=") + id) + ", key=") + key) + ", content='") + content) + '\'') + ", title='") + title) + '\'') + ", numberOfAnswers=") + numberOfAnswers) + ", numberOfCorrect=") + numberOfCorrect) + ", status=") + status) + ", image=") + image) + ", options=") + options) + ", topics=") + topics) + '}';
    }

    public java.lang.Integer getCorrectOptionId() {
        return this.getOptions().stream().filter(pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Option::getCorrect).findAny().map(pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Option::getId).orElse(null);
    }

    public void addAnswerStatistics(pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswer questionAnswer) {
        numberOfAnswers++;
        if ((questionAnswer.getOption() != null) && questionAnswer.getOption().getCorrect()) {
            numberOfCorrect++;
        }
    }

    public java.lang.Integer getDifficulty() {
        if (numberOfAnswers == 0) {
            return null;
        }
        return (numberOfCorrect * 100) / numberOfAnswers;
    }

    public void update(pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto questionDto) {
        checkConsistentQuestion(questionDto);
        setTitle(questionDto.getTitle());
        setContent(questionDto.getContent());
        questionDto.getOptions().forEach(optionDto -> {
            pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Option option = getOptionById(optionDto.getId());
            if (option == null) {
                throw new pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.OPTION_NOT_FOUND, optionDto.getId());
            }
            option.setContent(optionDto.getContent());
            option.setCorrect(optionDto.getCorrect());
        });
    }

    private void checkConsistentQuestion(pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto questionDto) {
        if (((questionDto.getTitle().trim().length() == 0) || (questionDto.getContent().trim().length() == 0)) || questionDto.getOptions().stream().anyMatch(optionDto -> optionDto.getContent().trim().length() == 0)) {
            throw new pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.QUESTION_MISSING_DATA);
        }
        if (questionDto.getOptions().stream().filter(pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OptionDto::getCorrect).count() != 1) {
            throw new pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.QUESTION_MULTIPLE_CORRECT_OPTIONS);
        }
        if ((!questionDto.getOptions().stream().filter(pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OptionDto::getCorrect).findAny().equals(getOptions().stream().filter(pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Option::getCorrect).findAny().map(pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OptionDto::new))) && getQuizQuestions().stream().flatMap(quizQuestion -> quizQuestion.getQuestionAnswers().stream()).findAny().isPresent()) {
            throw new pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.QUESTION_CHANGE_CORRECT_OPTION_HAS_ANSWERS);
        }
    }

    public void updateTopics(java.util.Set<pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic> newTopics) {
        java.util.Set<pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic> toRemove = this.topics.stream().filter(topic -> !newTopics.contains(topic)).collect(java.util.stream.Collectors.toSet());
        toRemove.forEach(topic -> {
            this.topics.remove(topic);
            topic.getQuestions().remove(this);
        });
        newTopics.stream().filter(topic -> !this.topics.contains(topic)).forEach(topic -> {
            this.topics.add(topic);
            topic.getQuestions().add(this);
        });
    }

    private pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Option getOptionById(java.lang.Integer id) {
        return getOptions().stream().filter(option -> option.getId().equals(id)).findAny().orElse(null);
    }

    private void canRemove() {
        if (!getQuizQuestions().isEmpty()) {
            throw new pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.QUESTION_IS_USED_IN_QUIZ, getQuizQuestions().iterator().next().getQuiz().getTitle());
        }
    }

    public void setOptionsSequence() {
        int index = 0;
        for (pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Option option : getOptions()) {
            option.setSequence(index++);
        }
    }

    public boolean belongsToAssessment(pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Assessment chosenAssessment) {
        return chosenAssessment.getTopicConjunctions().stream().map(pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.TopicConjunction::getTopics).collect(java.util.stream.Collectors.toList()).contains(this.topics);
    }
}