package pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
public class QuizzesXmlImport {
    private pt.ulisboa.tecnico.socialsoftware.tutor.quiz.QuizService quizService;

    private pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository questionRepository;

    private pt.ulisboa.tecnico.socialsoftware.tutor.quiz.repository.QuizQuestionRepository quizQuestionRepository;

    private pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository courseExecutionRepository;

    public void importQuizzes(java.io.InputStream inputStream, pt.ulisboa.tecnico.socialsoftware.tutor.quiz.QuizService quizService, pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository questionRepository, pt.ulisboa.tecnico.socialsoftware.tutor.quiz.repository.QuizQuestionRepository quizQuestionRepository, pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository courseExecutionRepository) {
        this.quizService = quizService;
        this.questionRepository = questionRepository;
        this.quizQuestionRepository = quizQuestionRepository;
        this.courseExecutionRepository = courseExecutionRepository;
        org.jdom2.input.SAXBuilder builder = new org.jdom2.input.SAXBuilder();
        builder.setIgnoringElementContentWhitespace(true);
        org.jdom2.Document doc;
        try {
            java.io.Reader reader = new java.io.InputStreamReader(inputStream, java.nio.charset.Charset.defaultCharset());
            doc = builder.build(reader);
        } catch (java.io.FileNotFoundException e) {
            throw new pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.QUIZZES_IMPORT_ERROR, "File not found");
        } catch (org.jdom2.JDOMException e) {
            throw new pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.QUIZZES_IMPORT_ERROR, "Coding problem");
        } catch (java.io.IOException e) {
            throw new pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.QUIZZES_IMPORT_ERROR, "File type or format");
        }
        if (doc == null) {
            throw new pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.QUIZZES_IMPORT_ERROR, "File not found ot format error");
        }
        importQuizzes(doc);
    }

    public void importQuizzes(java.lang.String quizzesXml, pt.ulisboa.tecnico.socialsoftware.tutor.quiz.QuizService quizService, pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository questionRepository, pt.ulisboa.tecnico.socialsoftware.tutor.quiz.repository.QuizQuestionRepository quizQuestionRepository, pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository courseExecutionRepository) {
        org.jdom2.input.SAXBuilder builder = new org.jdom2.input.SAXBuilder();
        builder.setIgnoringElementContentWhitespace(true);
        java.io.InputStream stream = new java.io.ByteArrayInputStream(quizzesXml.getBytes());
        importQuizzes(stream, quizService, questionRepository, quizQuestionRepository, courseExecutionRepository);
    }

    private void importQuizzes(org.jdom2.Document doc) {
        org.jdom2.xpath.XPathFactory xpfac = org.jdom2.xpath.XPathFactory.instance();
        org.jdom2.xpath.XPathExpression<org.jdom2.Element> xp = xpfac.compile("//quizzes/quiz", org.jdom2.filter.Filters.element());
        for (org.jdom2.Element element : xp.evaluate(doc)) {
            importQuiz(element);
        }
    }

    private void importQuiz(org.jdom2.Element quizElement) {
        java.lang.String courseExecutionType = quizElement.getAttributeValue("courseExecutionType");
        java.lang.String acronym = quizElement.getAttributeValue("acronym");
        java.lang.String academicTerm = quizElement.getAttributeValue("academicTerm");
        java.lang.Integer key = java.lang.Integer.valueOf(quizElement.getAttributeValue("key"));
        boolean scramble = false;
        if (quizElement.getAttributeValue("scramble") != null) {
            scramble = java.lang.Boolean.parseBoolean(quizElement.getAttributeValue("scramble"));
        }
        boolean qrCodeOnly = false;
        if (quizElement.getAttributeValue("qrCodeOnly") != null) {
            qrCodeOnly = java.lang.Boolean.parseBoolean(quizElement.getAttributeValue("qrCodeOnly"));
        }
        boolean oneWay = false;
        if (quizElement.getAttributeValue("oneWay") != null) {
            oneWay = java.lang.Boolean.parseBoolean(quizElement.getAttributeValue("oneWay"));
        }
        java.lang.String title = quizElement.getAttributeValue("title");
        java.lang.String creationDate = null;
        if (quizElement.getAttributeValue("creationDate") != null) {
            creationDate = quizElement.getAttributeValue("creationDate");
        }
        java.lang.String availableDate = null;
        if (quizElement.getAttributeValue("availableDate") != null) {
            availableDate = quizElement.getAttributeValue("availableDate");
        }
        java.lang.String conclusionDate = null;
        if (quizElement.getAttributeValue("conclusionDate") != null) {
            conclusionDate = quizElement.getAttributeValue("conclusionDate");
        }
        java.lang.String type = quizElement.getAttributeValue("type");
        java.lang.Integer series = null;
        if (quizElement.getAttributeValue("series") != null) {
            series = java.lang.Integer.valueOf(quizElement.getAttributeValue("series"));
        }
        java.lang.String version = quizElement.getAttributeValue("version");
        pt.ulisboa.tecnico.socialsoftware.tutor.quiz.dto.QuizDto quizDto = new pt.ulisboa.tecnico.socialsoftware.tutor.quiz.dto.QuizDto();
        quizDto.setKey(key);
        quizDto.setScramble(scramble);
        quizDto.setQrCodeOnly(qrCodeOnly);
        quizDto.setOneWay(oneWay);
        quizDto.setTitle(title);
        quizDto.setCreationDate(creationDate);
        quizDto.setAvailableDate(availableDate);
        quizDto.setConclusionDate(conclusionDate);
        quizDto.setType(pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz.QuizType.valueOf(type));
        quizDto.setSeries(series);
        quizDto.setVersion(version);
        pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution courseExecution = this.courseExecutionRepository.findByAcronymAcademicTermType(acronym, academicTerm, courseExecutionType).orElseThrow(() -> new pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.COURSE_EXECUTION_NOT_FOUND, acronym));
        pt.ulisboa.tecnico.socialsoftware.tutor.quiz.dto.QuizDto quizDto2 = quizService.createQuiz(courseExecution.getId(), quizDto);
        importQuizQuestions(quizElement.getChild("quizQuestions"), quizDto2);
    }

    private void importQuizQuestions(org.jdom2.Element quizQuestionsElement, pt.ulisboa.tecnico.socialsoftware.tutor.quiz.dto.QuizDto quizDto) {
        for (org.jdom2.Element quizQuestionElement : quizQuestionsElement.getChildren("quizQuestion")) {
            java.lang.Integer sequence = java.lang.Integer.valueOf(quizQuestionElement.getAttributeValue("sequence"));
            int questionKey = java.lang.Integer.parseInt(quizQuestionElement.getAttributeValue("questionKey"));
            pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question question = questionRepository.findByKey(questionKey).orElseThrow(() -> new pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.QUESTION_NOT_FOUND, questionKey));
            pt.ulisboa.tecnico.socialsoftware.tutor.quiz.dto.QuizQuestionDto quizQuestionDto = quizService.addQuestionToQuiz(question.getId(), quizDto.getId());
            pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion quizQuestion = quizQuestionRepository.findById(quizQuestionDto.getId()).orElseThrow(() -> new <QUIZ_QUESTION_NOT_FOUND>pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(quizQuestionDto.getId()));
            quizQuestion.setSequence(sequence);
        }
    }
}