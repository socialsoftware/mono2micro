package pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
public class AnswersXmlExport {
    public static final java.lang.String SEQUENCE = "sequence";

    public java.lang.String export(java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer> quizAnswers) {
        org.jdom2.Element element = createHeader();
        exportQuizAnswers(element, quizAnswers);
        org.jdom2.output.XMLOutputter xml = new org.jdom2.output.XMLOutputter();
        xml.setFormat(org.jdom2.output.Format.getPrettyFormat());
        return xml.outputString(element);
    }

    public org.jdom2.Element createHeader() {
        org.jdom2.Document jdomDoc = new org.jdom2.Document();
        org.jdom2.Element rootElement = new org.jdom2.Element("quizAnswers");
        jdomDoc.setRootElement(rootElement);
        return rootElement;
    }

    private void exportQuizAnswers(org.jdom2.Element element, java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer> quizAnswers) {
        for (pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer quizAnswer : quizAnswers) {
            exportQuizAnswer(element, quizAnswer);
        }
    }

    private void exportQuizAnswer(org.jdom2.Element element, pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer quizAnswer) {
        org.jdom2.Element quizAnswerElement = new org.jdom2.Element("quizAnswer");
        if (quizAnswer.getAnswerDate() != null) {
            quizAnswerElement.setAttribute("answerDate", java.lang.String.valueOf(quizAnswer.getAnswerDate()));
        }
        quizAnswerElement.setAttribute("completed", java.lang.String.valueOf(quizAnswer.isCompleted()));
        org.jdom2.Element quizElement = new org.jdom2.Element("quiz");
        quizElement.setAttribute("key", java.lang.String.valueOf(quizAnswer.getQuiz().getKey()));
        quizAnswerElement.addContent(quizElement);
        org.jdom2.Element userElement = new org.jdom2.Element("user");
        userElement.setAttribute("key", java.lang.String.valueOf(quizAnswer.getUser().getKey()));
        quizAnswerElement.addContent(userElement);
        exportQuestionAnswers(quizAnswerElement, quizAnswer.getQuestionAnswers());
        element.addContent(quizAnswerElement);
    }

    private void exportQuestionAnswers(org.jdom2.Element quizAnswerElement, java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswer> questionAnswers) {
        org.jdom2.Element questionAnswersElement = new org.jdom2.Element("questionAnswers");
        for (pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswer questionAnswer : questionAnswers) {
            exportQuestionAnswer(questionAnswersElement, questionAnswer);
        }
        quizAnswerElement.addContent(questionAnswersElement);
    }

    private void exportQuestionAnswer(org.jdom2.Element questionAnswersElement, pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswer questionAnswer) {
        org.jdom2.Element questionAnswerElement = new org.jdom2.Element("questionAnswer");
        if (questionAnswer.getTimeTaken() != null) {
            questionAnswerElement.setAttribute("timeTaken", java.lang.String.valueOf(questionAnswer.getTimeTaken()));
        }
        questionAnswerElement.setAttribute(pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.AnswersXmlExport.SEQUENCE, java.lang.String.valueOf(questionAnswer.getSequence()));
        org.jdom2.Element quizQuestionElement = new org.jdom2.Element("quizQuestion");
        quizQuestionElement.setAttribute("key", java.lang.String.valueOf(questionAnswer.getQuizQuestion().getQuiz().getKey()));
        quizQuestionElement.setAttribute(pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.AnswersXmlExport.SEQUENCE, java.lang.String.valueOf(questionAnswer.getQuizQuestion().getSequence()));
        questionAnswerElement.addContent(quizQuestionElement);
        if (questionAnswer.getOption() != null) {
            org.jdom2.Element optionElement = new org.jdom2.Element("option");
            optionElement.setAttribute("questionKey", java.lang.String.valueOf(questionAnswer.getOption().getQuestion().getKey()));
            optionElement.setAttribute(pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.AnswersXmlExport.SEQUENCE, java.lang.String.valueOf(questionAnswer.getOption().getSequence()));
            questionAnswerElement.addContent(optionElement);
        }
        questionAnswersElement.addContent(questionAnswerElement);
    }
}