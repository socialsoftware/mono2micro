package pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain;
public class LatexQuizExportVisitor extends pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.LatexVisitor {
    public java.lang.String export(pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz quiz) {
        quiz.accept(this);
        quiz.getQuizQuestions().stream().sorted(java.util.Comparator.comparing(pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion::getSequence)).forEach(quizQuestion -> quizQuestion.accept(this));
        exportQuestions(quiz.getQuizQuestions().stream().map(pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion::getQuestion).collect(java.util.stream.Collectors.toList()));
        return this.result;
    }

    private void exportQuestions(java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question> questions) {
        for (pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question question : questions) {
            question.accept(this);
        }
    }
}