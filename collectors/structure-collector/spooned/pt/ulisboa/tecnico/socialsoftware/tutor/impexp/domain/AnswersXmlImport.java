package pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
@org.springframework.stereotype.Component
public class AnswersXmlImport {
    public static final java.lang.String SEQUENCE = "sequence";

    public static final java.lang.String OPTION = "option";

    private pt.ulisboa.tecnico.socialsoftware.tutor.answer.AnswerService answerService;

    private pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository questionRepository;

    private pt.ulisboa.tecnico.socialsoftware.tutor.quiz.repository.QuizRepository quizRepository;

    private pt.ulisboa.tecnico.socialsoftware.tutor.answer.repository.QuizAnswerRepository quizAnswerRepository;

    private pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository userRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private pt.ulisboa.tecnico.socialsoftware.tutor.quiz.repository.QuizQuestionRepository quizQuestionRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.OptionRepository optionRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private pt.ulisboa.tecnico.socialsoftware.tutor.answer.repository.QuestionAnswerRepository questionAnswerRepository;

    private java.util.Map<java.lang.Integer, java.util.Map<java.lang.Integer, java.lang.Integer>> questionMap;

    public void importAnswers(java.io.InputStream inputStream, pt.ulisboa.tecnico.socialsoftware.tutor.answer.AnswerService answerService, pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository questionRepository, pt.ulisboa.tecnico.socialsoftware.tutor.quiz.repository.QuizRepository quizRepository, pt.ulisboa.tecnico.socialsoftware.tutor.answer.repository.QuizAnswerRepository quizAnswerRepository, pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository userRepository) {
        this.answerService = answerService;
        this.questionRepository = questionRepository;
        this.quizRepository = quizRepository;
        this.quizAnswerRepository = quizAnswerRepository;
        this.userRepository = userRepository;
        org.jdom2.input.SAXBuilder builder = new org.jdom2.input.SAXBuilder();
        builder.setIgnoringElementContentWhitespace(true);
        org.jdom2.Document doc;
        try {
            java.io.Reader reader = new java.io.InputStreamReader(inputStream, java.nio.charset.Charset.defaultCharset());
            doc = builder.build(reader);
        } catch (java.io.FileNotFoundException e) {
            throw new pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.ANSWERS_IMPORT_ERROR, "File not found");
        } catch (org.jdom2.JDOMException e) {
            throw new pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.ANSWERS_IMPORT_ERROR, "Coding problem");
        } catch (java.io.IOException e) {
            throw new pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.ANSWERS_IMPORT_ERROR, "File type or format");
        }
        if (doc == null) {
            throw new pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.ANSWERS_IMPORT_ERROR, "File not found ot format error");
        }
        loadQuestionMap();
        importQuizAnswers(doc);
    }

    private void loadQuestionMap() {
        questionMap = questionRepository.findAll().stream().collect(java.util.stream.Collectors.toMap(pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question::getKey, question -> question.getOptions().stream().collect(java.util.stream.Collectors.toMap(pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Option::getSequence, pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Option::getId))));
    }

    public void importAnswers(java.lang.String answersXml, pt.ulisboa.tecnico.socialsoftware.tutor.answer.AnswerService answerService, pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository questionRepository, pt.ulisboa.tecnico.socialsoftware.tutor.quiz.repository.QuizRepository quizRepository, pt.ulisboa.tecnico.socialsoftware.tutor.answer.repository.QuizAnswerRepository quizAnswerRepository, pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository userRepository) {
        org.jdom2.input.SAXBuilder builder = new org.jdom2.input.SAXBuilder();
        builder.setIgnoringElementContentWhitespace(true);
        java.io.InputStream stream = new java.io.ByteArrayInputStream(answersXml.getBytes());
        importAnswers(stream, answerService, questionRepository, quizRepository, quizAnswerRepository, userRepository);
    }

    private void importQuizAnswers(org.jdom2.Document doc) {
        org.jdom2.xpath.XPathFactory xpfac = org.jdom2.xpath.XPathFactory.instance();
        org.jdom2.xpath.XPathExpression<org.jdom2.Element> xp = xpfac.compile("//quizAnswers/quizAnswer", org.jdom2.filter.Filters.element());
        for (org.jdom2.Element element : xp.evaluate(doc)) {
            importQuizAnswer(element);
        }
    }

    private void importQuizAnswer(org.jdom2.Element answerElement) {
        java.time.LocalDateTime answerDate = null;
        if (answerElement.getAttributeValue("answerDate") != null) {
            answerDate = java.time.LocalDateTime.parse(answerElement.getAttributeValue("answerDate"));
        }
        boolean completed = false;
        if (answerElement.getAttributeValue("completed") != null) {
            completed = java.lang.Boolean.parseBoolean(answerElement.getAttributeValue("completed"));
        }
        java.lang.Integer quizKey = java.lang.Integer.valueOf(answerElement.getChild("quiz").getAttributeValue("key"));
        pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz quiz = quizRepository.findByKey(quizKey).orElseThrow(() -> new pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.ANSWERS_IMPORT_ERROR, "quiz id does not exist " + quizKey));
        java.lang.Integer key = java.lang.Integer.valueOf(answerElement.getChild("user").getAttributeValue("key"));
        pt.ulisboa.tecnico.socialsoftware.tutor.user.User user = userRepository.findByKey(key);
        pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.QuizAnswerDto quizAnswerDto = answerService.createQuizAnswer(user.getId(), quiz.getId());
        pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer quizAnswer = quizAnswerRepository.findById(quizAnswerDto.getId()).orElseThrow(() -> new <QUIZ_ANSWER_NOT_FOUND>pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(quizAnswerDto.getId()));
        quizAnswer.setAnswerDate(answerDate);
        quizAnswer.setCompleted(completed);
        importQuestionAnswers(answerElement.getChild("questionAnswers"), quizAnswer);
    }

    private void importQuestionAnswers(org.jdom2.Element questionAnswersElement, pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer quizAnswer) {
        for (org.jdom2.Element questionAnswerElement : questionAnswersElement.getChildren("questionAnswer")) {
            java.lang.Integer timeTaken = null;
            if (questionAnswerElement.getAttributeValue("timeTaken") != null) {
                timeTaken = java.lang.Integer.valueOf(questionAnswerElement.getAttributeValue("timeTaken"));
            }
            int answerSequence = java.lang.Integer.parseInt(questionAnswerElement.getAttributeValue(pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.AnswersXmlImport.SEQUENCE));
            java.lang.Integer optionId = null;
            if (questionAnswerElement.getChild(pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.AnswersXmlImport.OPTION) != null) {
                java.lang.Integer questionKey = java.lang.Integer.valueOf(questionAnswerElement.getChild(pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.AnswersXmlImport.OPTION).getAttributeValue("questionKey"));
                java.lang.Integer optionSequence = java.lang.Integer.valueOf(questionAnswerElement.getChild(pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.AnswersXmlImport.OPTION).getAttributeValue(pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.AnswersXmlImport.SEQUENCE));
                optionId = questionMap.get(questionKey).get(optionSequence);
            }
            pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswer questionAnswer = quizAnswer.getQuestionAnswers().stream().filter(qa -> qa.getSequence().equals(answerSequence)).findFirst().orElseThrow(() -> new pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.QUESTION_ANSWER_NOT_FOUND, answerSequence));
            questionAnswer.setTimeTaken(timeTaken);
            if (optionId == null) {
                questionAnswer.setOption(null);
            } else {
                questionAnswer.setOption(optionRepository.findById(optionId).orElse(null));
            }
            questionAnswerRepository.save(questionAnswer);
        }
    }
}