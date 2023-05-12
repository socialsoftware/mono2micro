package pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
public class TopicsXmlImport {
    private pt.ulisboa.tecnico.socialsoftware.tutor.question.QuestionService questionService;

    private pt.ulisboa.tecnico.socialsoftware.tutor.question.TopicService topicService;

    private pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository courseRepository;

    public void importTopics(java.io.InputStream inputStream, pt.ulisboa.tecnico.socialsoftware.tutor.question.TopicService topicService, pt.ulisboa.tecnico.socialsoftware.tutor.question.QuestionService questionService, pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository courseRepository) {
        this.topicService = topicService;
        this.questionService = questionService;
        this.courseRepository = courseRepository;
        org.jdom2.input.SAXBuilder builder = new org.jdom2.input.SAXBuilder();
        builder.setIgnoringElementContentWhitespace(true);
        org.jdom2.Document doc;
        try {
            java.io.Reader reader = new java.io.InputStreamReader(inputStream, java.nio.charset.Charset.defaultCharset());
            doc = builder.build(reader);
        } catch (java.io.FileNotFoundException e) {
            throw new pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.TOPICS_IMPORT_ERROR, "File not found");
        } catch (org.jdom2.JDOMException e) {
            throw new pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.TOPICS_IMPORT_ERROR, "Coding problem");
        } catch (java.io.IOException e) {
            throw new pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.TOPICS_IMPORT_ERROR, "File type or format");
        }
        if (doc == null) {
            throw new pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.TOPICS_IMPORT_ERROR, "File not found ot format error");
        }
        importTopics(doc);
    }

    public void importTopics(java.lang.String topicsXml, pt.ulisboa.tecnico.socialsoftware.tutor.question.TopicService topicService, pt.ulisboa.tecnico.socialsoftware.tutor.question.QuestionService questionService, pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository courseRepository) {
        org.jdom2.input.SAXBuilder builder = new org.jdom2.input.SAXBuilder();
        builder.setIgnoringElementContentWhitespace(true);
        java.io.InputStream stream = new java.io.ByteArrayInputStream(topicsXml.getBytes());
        importTopics(stream, topicService, questionService, courseRepository);
    }

    private void importTopics(org.jdom2.Document doc) {
        org.jdom2.xpath.XPathFactory xpfac = org.jdom2.xpath.XPathFactory.instance();
        org.jdom2.xpath.XPathExpression<org.jdom2.Element> xp = xpfac.compile("//topics/topic", org.jdom2.filter.Filters.element());
        for (org.jdom2.Element element : xp.evaluate(doc)) {
            importTopic(element);
        }
    }

    private void importTopic(org.jdom2.Element topicElement) {
        java.lang.String courseType = topicElement.getAttributeValue("courseType");
        java.lang.String courseName = topicElement.getAttributeValue("courseName");
        java.lang.String name = topicElement.getAttributeValue("name");
        pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto topicDto = new pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto();
        topicDto.setName(name);
        pt.ulisboa.tecnico.socialsoftware.tutor.course.Course course = courseRepository.findByNameType(courseName, courseType).orElseThrow(() -> new pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.COURSE_NOT_FOUND, courseName));
        topicService.createTopic(course.getId(), topicDto);
        for (org.jdom2.Element questionElement : topicElement.getChild("questions").getChildren("question")) {
            importQuestion(questionElement, name);
        }
    }

    private void importQuestion(org.jdom2.Element questionElement, java.lang.String name) {
        java.lang.Integer key = java.lang.Integer.valueOf(questionElement.getAttributeValue("key"));
        pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto questionDto = questionService.findQuestionByKey(key);
        pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto topicDto = new pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto();
        topicDto.setName(name);
        questionDto.getTopics().add(topicDto);
        questionService.updateQuestionTopics(questionDto.getId(), questionDto.getTopics().toArray(new pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto[0]));
    }
}