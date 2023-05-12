package pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain;
public class LatexQuestionExportVisitor extends pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.LatexVisitor {
    public java.lang.String export(java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question> questions) {
        exportQuestions(questions);
        return this.result;
    }

    private void exportQuestions(java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question> questions) {
        for (pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question question : questions) {
            question.accept(this);
        }
    }
}