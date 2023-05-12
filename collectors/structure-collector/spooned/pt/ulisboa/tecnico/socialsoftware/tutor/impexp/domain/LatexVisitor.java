package pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain;
public abstract class LatexVisitor implements pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.Visitor {
    protected java.lang.String result = "";

    protected java.lang.String questionContent;

    @java.lang.Override
    public void visitQuiz(pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz quiz) {
        this.result = ((((((((((((((((((((this.result + "% Title: ") + quiz.getTitle()) + "\n") + "% Available date: ") + quiz.getAvailableDate()) + "\n") + "% Conclusion date: ") + quiz.getConclusionDate()) + "\n") + "% Type: ") + quiz.getType()) + "\n") + "% Scramble: ") + quiz.getScramble()) + "\n") + "% OneWay: ") + quiz.isOneWay()) + "\n") + "% QrCodeOnly: ") + quiz.isQrCodeOnly()) + "\n\n";
    }

    @java.lang.Override
    public void visitQuizQuestion(pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion quizQuestion) {
        this.result = ((this.result + "\\q") + quizQuestion.getQuestion().getTitle().replaceAll("\\s+", "")) + "\n\n";
    }

    @java.lang.Override
    public void visitQuestion(pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question question) {
        this.result = (((this.result + "\\newcommand{\\q") + question.getTitle().replaceAll("\\s+", "")) + "}{\n") + "\\begin{ClosedQuestion}\n";
        this.questionContent = question.getContent();
        if (question.getImage() != null)
            question.getImage().accept(this);

        this.result = ((this.result + "\t") + this.questionContent) + "\n\n";
        for (pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Option option : question.getOptions().stream().sorted(java.util.Comparator.comparing(pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Option::getSequence)).collect(java.util.stream.Collectors.toList())) {
            option.accept(this);
        }
        this.result = this.result + "\\putOptions\n";
        this.result = ((this.result + "% Answer: ") + convertSequenceToLetter(question.getOptions().stream().filter(pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Option::getCorrect).map(pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Option::getSequence).findAny().orElse(null))) + "\n";
        this.result = this.result + "\\end{ClosedQuestion}\n}\n\n";
    }

    @java.lang.Override
    public void visitImage(pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Image image) {
        java.lang.String imageString = "\n\t\\begin{center}\n";
        imageString = ((((imageString + "\t\t\\includegraphics[width=") + image.getWidth()) + "cm]{") + image.getUrl()) + "}\n";
        imageString = imageString + "\t\\end{center}\n\t";
        this.questionContent = this.questionContent.replaceAll("!\\[image\\]\\[image\\]", imageString);
    }

    @java.lang.Override
    public void visitOption(pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Option option) {
        this.result = ((((this.result + "\t\\option") + convertSequenceToLetter(option.getSequence())) + "{") + option.getContent()) + "}\n";
    }
}