package pt.ulisboa.tecnico.socialsoftware.tutor.statement.dto;
public class StatementCreationDto implements java.io.Serializable {
    private java.lang.Integer numberOfQuestions;

    private java.lang.Integer assessment;

    public java.lang.Integer getNumberOfQuestions() {
        return numberOfQuestions;
    }

    public void setNumberOfQuestions(java.lang.Integer numberOfQuestions) {
        this.numberOfQuestions = numberOfQuestions;
    }

    public java.lang.Integer getAssessment() {
        return assessment;
    }

    public void setAssessment(java.lang.Integer assessment) {
        this.assessment = assessment;
    }

    @java.lang.Override
    public java.lang.String toString() {
        return (((("StatementCreationDto{" + "numberOfQuestions=") + numberOfQuestions) + ", assessment=") + assessment) + '}';
    }
}