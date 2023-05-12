package pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
public class TopicsXmlExport {
    public java.lang.String export(java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic> topics) {
        org.jdom2.Element element = createHeader();
        exportTopics(element, topics);
        org.jdom2.output.XMLOutputter xml = new org.jdom2.output.XMLOutputter();
        xml.setFormat(org.jdom2.output.Format.getPrettyFormat());
        return xml.outputString(element);
    }

    public org.jdom2.Element createHeader() {
        org.jdom2.Document jdomDoc = new org.jdom2.Document();
        org.jdom2.Element rootElement = new org.jdom2.Element("topics");
        jdomDoc.setRootElement(rootElement);
        return rootElement;
    }

    private void exportTopics(org.jdom2.Element element, java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic> topics) {
        for (pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic topic : topics) {
            exportTopic(element, topic);
        }
    }

    private void exportTopic(org.jdom2.Element element, pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic topic) {
        org.jdom2.Element topicElement = new org.jdom2.Element("topic");
        topicElement.setAttribute("courseType", topic.getCourse().getType().name());
        topicElement.setAttribute("courseName", topic.getCourse().getName());
        topicElement.setAttribute("name", topic.getName());
        exportQuestions(topicElement, topic);
        element.addContent(topicElement);
    }

    private void exportQuestions(org.jdom2.Element topicElement, pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic topic) {
        org.jdom2.Element questionsElement = new org.jdom2.Element("questions");
        for (pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question question : topic.getQuestions()) {
            exportQuestion(questionsElement, question);
        }
        topicElement.addContent(questionsElement);
    }

    private void exportQuestion(org.jdom2.Element questionsElement, pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question question) {
        org.jdom2.Element questionElement = new org.jdom2.Element("question");
        questionElement.setAttribute("key", java.lang.String.valueOf(question.getKey()));
        questionsElement.addContent(questionElement);
    }
}