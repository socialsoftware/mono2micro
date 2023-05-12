package pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
public class XMLQuestionExportVisitor implements pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.Visitor {
    private org.jdom2.Element rootElement;

    private org.jdom2.Element currentElement;

    public java.lang.String export(java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question> questions) {
        createHeader();
        exportQuestions(questions);
        org.jdom2.output.XMLOutputter xml = new org.jdom2.output.XMLOutputter();
        xml.setFormat(org.jdom2.output.Format.getPrettyFormat());
        return xml.outputString(this.rootElement);
    }

    public void createHeader() {
        org.jdom2.Document jdomDoc = new org.jdom2.Document();
        rootElement = new org.jdom2.Element("questions");
        jdomDoc.setRootElement(rootElement);
        this.currentElement = rootElement;
    }

    private void exportQuestions(java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question> questions) {
        for (pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question question : questions) {
            question.accept(this);
        }
    }

    @java.lang.Override
    public void visitQuestion(pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question question) {
        java.time.format.DateTimeFormatter formatter = pt.ulisboa.tecnico.socialsoftware.tutor.course.Course.formatter;
        org.jdom2.Element questionElement = new org.jdom2.Element("question");
        questionElement.setAttribute("courseType", question.getCourse().getType().name());
        questionElement.setAttribute("courseName", question.getCourse().getName());
        questionElement.setAttribute("key", java.lang.String.valueOf(question.getKey()));
        questionElement.setAttribute("content", question.getContent());
        questionElement.setAttribute("title", question.getTitle());
        questionElement.setAttribute("status", question.getStatus().name());
        if (question.getCreationDate() != null)
            questionElement.setAttribute("creationDate", question.getCreationDate().format(formatter));

        this.currentElement.addContent(questionElement);
        this.currentElement = questionElement;
        if (question.getImage() != null)
            question.getImage().accept(this);

        org.jdom2.Element optionsElement = new org.jdom2.Element("options");
        this.currentElement.addContent(optionsElement);
        this.currentElement = optionsElement;
        for (pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Option option : question.getOptions()) {
            option.accept(this);
        }
        this.currentElement = this.rootElement;
    }

    @java.lang.Override
    public void visitImage(pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Image image) {
        org.jdom2.Element imageElement = new org.jdom2.Element("image");
        if (image.getWidth() != null) {
            imageElement.setAttribute("width", java.lang.String.valueOf(image.getWidth()));
        }
        imageElement.setAttribute("url", image.getUrl());
        this.currentElement.addContent(imageElement);
    }

    @java.lang.Override
    public void visitOption(pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Option option) {
        org.jdom2.Element optionElement = new org.jdom2.Element("option");
        optionElement.setAttribute("sequence", java.lang.String.valueOf(option.getSequence()));
        optionElement.setAttribute("content", option.getContent());
        optionElement.setAttribute("correct", java.lang.String.valueOf(option.getCorrect()));
        this.currentElement.addContent(optionElement);
    }
}