package pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain;
public interface Visitor {
    default void visitQuestion(pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question question) {
    }

    default void visitImage(pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Image image) {
    }

    default void visitOption(pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Option option) {
    }

    default void visitQuiz(pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz quiz) {
    }

    default void visitQuizQuestion(pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion quizQuestion) {
    }

    default void visitUser(pt.ulisboa.tecnico.socialsoftware.tutor.user.User user) {
    }

    default void visitQuizAnswer(pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer quizAnswer) {
    }

    default void visitQuestionAnswer(pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswer questionAnswer) {
    }

    default java.lang.String convertSequenceToLetter(java.lang.Integer value) {
        switch (value) {
            case 0 :
                return "A";
            case 1 :
                return "B";
            case 2 :
                return "C";
            case 3 :
                return "D";
            default :
                return "X";
        }
    }
}