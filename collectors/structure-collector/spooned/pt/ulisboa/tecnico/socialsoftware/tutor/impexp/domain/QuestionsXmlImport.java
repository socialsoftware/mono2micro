package pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
public class QuestionsXmlImport {
    private pt.ulisboa.tecnico.socialsoftware.tutor.question.QuestionService questionService;

    private pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository courseRepository;

    public void importQuestions(java.io.InputStream inputStream, pt.ulisboa.tecnico.socialsoftware.tutor.question.QuestionService questionService, pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository courseRepository) {
        this.questionService = questionService;
        this.courseRepository = courseRepository;
        org.jdom2.input.SAXBuilder builder = new org.jdom2.input.SAXBuilder();
        builder.setIgnoringElementContentWhitespace(true);
        org.jdom2.Document doc;
        try {
            java.io.Reader reader = new java.io.InputStreamReader(inputStream, java.nio.charset.Charset.defaultCharset());
            doc = builder.build(reader);
        } catch (java.io.FileNotFoundException e) {
            throw new pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.QUESTIONS_IMPORT_ERROR, "File not found");
        } catch (org.jdom2.JDOMException e) {
            throw new pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.QUESTIONS_IMPORT_ERROR, "Coding problem");
        } catch (java.io.IOException e) {
            throw new pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.QUESTIONS_IMPORT_ERROR, "File type or format");
        }
        if (doc == null) {
            throw new pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.QUESTIONS_IMPORT_ERROR, "File not found ot format error");
        }
        importQuestions(doc);
    }

    public void importQuestions(java.lang.String questionsXml, pt.ulisboa.tecnico.socialsoftware.tutor.question.QuestionService questionService, pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository courseRepository) {
        org.jdom2.input.SAXBuilder builder = new org.jdom2.input.SAXBuilder();
        builder.setIgnoringElementContentWhitespace(true);
        java.io.InputStream stream = new java.io.ByteArrayInputStream(questionsXml.getBytes());
        importQuestions(stream, questionService, courseRepository);
    }

    private void importQuestions(org.jdom2.Document doc) {
        org.jdom2.xpath.XPathFactory xpfac = org.jdom2.xpath.XPathFactory.instance();
        org.jdom2.xpath.XPathExpression<org.jdom2.Element> xp = xpfac.compile("//questions/question", org.jdom2.filter.Filters.element());
        for (org.jdom2.Element element : xp.evaluate(doc)) {
            importQuestion(element);
        }
    }

    private void importQuestion(org.jdom2.Element questionElement) {
        java.lang.String courseType = questionElement.getAttributeValue("courseType");
        java.lang.String courseName = questionElement.getAttributeValue("courseName");
        java.lang.Integer key = java.lang.Integer.valueOf(questionElement.getAttributeValue("key"));
        java.lang.String content = questionElement.getAttributeValue("content");
        java.lang.String title = questionElement.getAttributeValue("title");
        java.lang.String status = questionElement.getAttributeValue("status");
        java.lang.String creationDate = questionElement.getAttributeValue("creationDate");
        pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto questionDto = new pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto();
        questionDto.setKey(key);
        questionDto.setContent(content);
        questionDto.setTitle(title);
        questionDto.setStatus(status);
        questionDto.setCreationDate(creationDate);
        org.jdom2.Element imageElement = questionElement.getChild("image");
        if (imageElement != null) {
            java.lang.Integer width = (imageElement.getAttributeValue("width") != null) ? java.lang.Integer.valueOf(imageElement.getAttributeValue("width")) : null;
            java.lang.String url = imageElement.getAttributeValue("url");
            pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.ImageDto imageDto = new pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.ImageDto();
            imageDto.setWidth(width);
            imageDto.setUrl(url);
            questionDto.setImage(imageDto);
        }
        java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OptionDto> optionDtos = new java.util.ArrayList<>();
        for (org.jdom2.Element optionElement : questionElement.getChild("options").getChildren("option")) {
            java.lang.Integer optionSequence = java.lang.Integer.valueOf(optionElement.getAttributeValue("sequence"));
            java.lang.String optionContent = optionElement.getAttributeValue("content");
            boolean correct = java.lang.Boolean.parseBoolean(optionElement.getAttributeValue("correct"));
            pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OptionDto optionDto = new pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OptionDto();
            optionDto.setSequence(optionSequence);
            optionDto.setContent(optionContent);
            optionDto.setCorrect(correct);
            optionDtos.add(optionDto);
        }
        questionDto.setOptions(optionDtos);
        pt.ulisboa.tecnico.socialsoftware.tutor.course.Course course = courseRepository.findByNameType(courseName, courseType).orElseThrow(() -> new pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.COURSE_NOT_FOUND, courseName));
        questionService.createQuestion(course.getId(), questionDto);
    }
}