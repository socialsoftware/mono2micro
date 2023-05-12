package pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain;
public class CSVQuizExportVisitor implements pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.Visitor {
    private java.lang.String[] line;

    private int column;

    private java.util.List<java.lang.String[]> table = new java.util.ArrayList<>();

    public java.lang.String export(pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz quiz) {
        int numberOfQuestions = quiz.getQuizQuestions().size();
        line = new java.lang.String[numberOfQuestions + 4];
        java.util.Arrays.fill(line, "");
        line[0] = "Username";
        line[1] = "Name";
        line[2] = "Start";
        line[3] = "Finish";
        table.add(line);
        for (pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer quizAnswer : quiz.getQuizAnswers()) {
            line = new java.lang.String[numberOfQuestions + 4];
            java.util.Arrays.fill(line, "");
            column = 0;
            quizAnswer.getUser().accept(this);
            quizAnswer.accept(this);
            quizAnswer.getQuestionAnswers().stream().sorted(java.util.Comparator.comparing(questionAnswer -> questionAnswer.getQuizQuestion().getSequence())).collect(java.util.stream.Collectors.toList()).forEach(questionAnswer -> questionAnswer.accept(this));
            table.add(line);
        }
        line = new java.lang.String[numberOfQuestions + 4];
        java.util.Arrays.fill(line, "");
        line[3] = "KEYS";
        column = 4;
        quiz.getQuizQuestions().stream().sorted(java.util.Comparator.comparing(pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion::getSequence)).forEach(quizQuestion -> quizQuestion.accept(this));
        table.add(line);
        return table.stream().map(this::convertToCSV).collect(java.util.stream.Collectors.joining("\n"));
    }

    @java.lang.Override
    public void visitUser(pt.ulisboa.tecnico.socialsoftware.tutor.user.User user) {
        line[column++] = (user.getUsername() != null) ? user.getUsername() : "";
        line[column++] = (user.getName() != null) ? user.getName() : "";
    }

    @java.lang.Override
    public void visitQuizAnswer(pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer quizAnswer) {
        java.time.format.DateTimeFormatter formatter = pt.ulisboa.tecnico.socialsoftware.tutor.course.Course.formatter;
        line[column++] = (quizAnswer.getCreationDate() != null) ? quizAnswer.getCreationDate().format(formatter) : "";
        line[column++] = (quizAnswer.getAnswerDate() != null) ? quizAnswer.getAnswerDate().format(formatter) : "";
    }

    @java.lang.Override
    public void visitQuestionAnswer(pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswer questionAnswer) {
        line[column++] = (questionAnswer.getOption() != null) ? convertSequenceToLetter(questionAnswer.getOption().getSequence()) : "X";
    }

    @java.lang.Override
    public void visitQuizQuestion(pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion quizQuestion) {
        line[column++] = quizQuestion.getQuestion().getOptions().stream().filter(pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Option::getCorrect).findAny().map(option -> convertSequenceToLetter(option.getSequence())).orElse("");
    }

    private java.lang.String convertToCSV(java.lang.String[] data) {
        return java.util.stream.Stream.of(data).collect(java.util.stream.Collectors.joining(","));
    }
}