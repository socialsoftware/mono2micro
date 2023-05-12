package pt.ulisboa.tecnico.socialsoftware.tutor.statistics;
public class StatsDto implements java.io.Serializable {
    private java.lang.Integer totalQuizzes = 0;

    private java.lang.Integer totalAnswers = 0;

    private java.lang.Integer totalUniqueQuestions = 0;

    private float correctAnswers = 0;

    private float improvedCorrectAnswers = 0;

    private java.lang.Integer uniqueCorrectAnswers = 0;

    private java.lang.Integer uniqueWrongAnswers = 0;

    private java.lang.Integer totalAvailableQuestions = 0;

    public java.lang.Integer getTotalQuizzes() {
        return totalQuizzes;
    }

    public void setTotalQuizzes(java.lang.Integer totalQuizzes) {
        this.totalQuizzes = totalQuizzes;
    }

    public java.lang.Integer getTotalAnswers() {
        return totalAnswers;
    }

    public void setTotalAnswers(java.lang.Integer totalAnswers) {
        this.totalAnswers = totalAnswers;
    }

    public java.lang.Integer getTotalUniqueQuestions() {
        return totalUniqueQuestions;
    }

    public void setTotalUniqueQuestions(java.lang.Integer totalUniqueQuestions) {
        this.totalUniqueQuestions = totalUniqueQuestions;
    }

    public float getCorrectAnswers() {
        return correctAnswers;
    }

    public void setCorrectAnswers(float correctAnswers) {
        this.correctAnswers = correctAnswers;
    }

    public float getImprovedCorrectAnswers() {
        return improvedCorrectAnswers;
    }

    public void setImprovedCorrectAnswers(float improvedCorrectAnswers) {
        this.improvedCorrectAnswers = improvedCorrectAnswers;
    }

    public java.lang.Integer getUniqueCorrectAnswers() {
        return uniqueCorrectAnswers;
    }

    public void setUniqueCorrectAnswers(java.lang.Integer uniqueCorrectAnswers) {
        this.uniqueCorrectAnswers = uniqueCorrectAnswers;
    }

    public java.lang.Integer getUniqueWrongAnswers() {
        return uniqueWrongAnswers;
    }

    public void setUniqueWrongAnswers(java.lang.Integer uniqueWrongAnswers) {
        this.uniqueWrongAnswers = uniqueWrongAnswers;
    }

    public java.lang.Integer getTotalAvailableQuestions() {
        return totalAvailableQuestions;
    }

    public void setTotalAvailableQuestions(java.lang.Integer totalAvailableQuestions) {
        this.totalAvailableQuestions = totalAvailableQuestions;
    }

    @java.lang.Override
    public java.lang.String toString() {
        return (((((((((((((("StatsDto{" + "totalQuizzes=") + totalQuizzes) + ", totalAnswers=") + totalAnswers) + ", totalUniqueQuestions=") + totalUniqueQuestions) + ", correctAnswers=") + correctAnswers) + ", improvedCorrectAnswers=") + improvedCorrectAnswers) + ", uniqueCorrectAnswers=") + uniqueCorrectAnswers) + ", uniqueWrongAnswers=") + uniqueWrongAnswers) + '}';
    }
}