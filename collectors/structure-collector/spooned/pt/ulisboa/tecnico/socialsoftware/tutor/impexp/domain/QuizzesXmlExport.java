package pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
public class QuizzesXmlExport {
    public java.lang.String export(java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz> quizzes) {
        org.jdom2.Element element = createHeader();
        exportQuizzes(element, quizzes);
        org.jdom2.output.XMLOutputter xml = new org.jdom2.output.XMLOutputter();
        xml.setFormat(org.jdom2.output.Format.getPrettyFormat());
        return xml.outputString(element);
    }

    public org.jdom2.Element createHeader() {
        org.jdom2.Document jdomDoc = new org.jdom2.Document();
        org.jdom2.Element rootElement = new org.jdom2.Element("quizzes");
        jdomDoc.setRootElement(rootElement);
        return rootElement;
    }

    private void exportQuizzes(org.jdom2.Element element, java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz> quizzes) {
        for (pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz quiz : quizzes) {
            exportQuiz(element, quiz);
        }
    }

    private void exportQuiz(org.jdom2.Element element, pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz quiz) {
        java.time.format.DateTimeFormatter formatter = pt.ulisboa.tecnico.socialsoftware.tutor.course.Course.formatter;
        org.jdom2.Element quizElement = new org.jdom2.Element("quiz");
        quizElement.setAttribute("courseExecutionType", quiz.getCourseExecution().getType().name());
        quizElement.setAttribute("acronym", quiz.getCourseExecution().getAcronym());
        quizElement.setAttribute("academicTerm", quiz.getCourseExecution().getAcademicTerm());
        quizElement.setAttribute("key", java.lang.String.valueOf(quiz.getKey()));
        quizElement.setAttribute("scramble", java.lang.String.valueOf(quiz.getScramble()));
        quizElement.setAttribute("qrCodeOnly", java.lang.String.valueOf(quiz.isQrCodeOnly()));
        quizElement.setAttribute("oneWay", java.lang.String.valueOf(quiz.isOneWay()));
        quizElement.setAttribute("type", quiz.getType().name());
        quizElement.setAttribute("title", quiz.getTitle());
        if (quiz.getCreationDate() != null)
            quizElement.setAttribute("creationDate", quiz.getCreationDate().format(formatter));

        if (quiz.getAvailableDate() != null)
            quizElement.setAttribute("availableDate", quiz.getAvailableDate().format(formatter));

        if (quiz.getConclusionDate() != null)
            quizElement.setAttribute("conclusionDate", quiz.getConclusionDate().format(formatter));

        if (quiz.getSeries() != null)
            quizElement.setAttribute("series", java.lang.String.valueOf(quiz.getSeries()));

        if (quiz.getVersion() != null)
            quizElement.setAttribute("version", quiz.getVersion());

        exportQuizQuestions(quizElement, quiz.getQuizQuestions());
        element.addContent(quizElement);
    }

    private void exportQuizQuestions(org.jdom2.Element questionElement, java.util.Set<pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion> quizQuestions) {
        org.jdom2.Element quizQuestionsElement = new org.jdom2.Element("quizQuestions");
        for (pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion quizQuestion : quizQuestions) {
            exportQuizQuestion(quizQuestionsElement, quizQuestion);
        }
        questionElement.addContent(quizQuestionsElement);
    }

    private void exportQuizQuestion(org.jdom2.Element optionsElement, pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion quizQuestion) {
        org.jdom2.Element optionElement = new org.jdom2.Element("quizQuestion");
        optionElement.setAttribute("sequence", java.lang.String.valueOf(quizQuestion.getSequence()));
        optionElement.setAttribute("questionKey", java.lang.String.valueOf(quizQuestion.getQuestion().getKey()));
        optionsElement.addContent(optionElement);
    }
}