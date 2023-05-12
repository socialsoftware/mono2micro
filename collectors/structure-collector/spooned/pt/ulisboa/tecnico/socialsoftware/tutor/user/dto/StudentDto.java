package pt.ulisboa.tecnico.socialsoftware.tutor.user.dto;
public class StudentDto implements java.io.Serializable {
    private java.lang.String username;

    private java.lang.String name;

    private java.lang.Integer numberOfTeacherQuizzes;

    private java.lang.Integer numberOfInClassQuizzes;

    private java.lang.Integer numberOfStudentQuizzes;

    private java.lang.Integer numberOfAnswers;

    private java.lang.Integer numberOfTeacherAnswers;

    private java.lang.Integer numberOfInClassAnswers;

    private java.lang.Integer numberOfStudentAnswers;

    private int percentageOfCorrectAnswers = 0;

    private int percentageOfCorrectTeacherAnswers = 0;

    private int percentageOfCorrectInClassAnswers = 0;

    private int percentageOfCorrectStudentAnswers = 0;

    private java.lang.String creationDate;

    private java.lang.String lastAccess;

    public StudentDto(pt.ulisboa.tecnico.socialsoftware.tutor.user.User user) {
        this.username = user.getUsername();
        this.name = user.getName();
        this.numberOfTeacherQuizzes = user.getNumberOfTeacherQuizzes();
        this.numberOfInClassQuizzes = user.getNumberOfInClassQuizzes();
        this.numberOfStudentQuizzes = user.getNumberOfStudentQuizzes();
        this.numberOfAnswers = (user.getNumberOfTeacherAnswers() + user.getNumberOfInClassAnswers()) + user.getNumberOfStudentAnswers();
        this.numberOfTeacherAnswers = user.getNumberOfTeacherAnswers();
        this.numberOfInClassAnswers = user.getNumberOfInClassAnswers();
        this.numberOfStudentAnswers = user.getNumberOfStudentAnswers();
        if (this.numberOfTeacherAnswers != 0)
            this.percentageOfCorrectTeacherAnswers = (user.getNumberOfCorrectTeacherAnswers() * 100) / this.numberOfTeacherAnswers;

        if (this.numberOfInClassAnswers != 0)
            this.percentageOfCorrectInClassAnswers = (user.getNumberOfCorrectInClassAnswers() * 100) / this.numberOfInClassAnswers;

        if (this.numberOfStudentAnswers != 0)
            this.percentageOfCorrectStudentAnswers = (user.getNumberOfCorrectStudentAnswers() * 100) / this.numberOfStudentAnswers;

        if (this.numberOfAnswers != 0)
            this.percentageOfCorrectAnswers = (((user.getNumberOfCorrectTeacherAnswers() + user.getNumberOfCorrectInClassAnswers()) + user.getNumberOfCorrectStudentAnswers()) * 100) / this.numberOfAnswers;

        if (user.getLastAccess() != null)
            this.lastAccess = user.getLastAccess().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        if (user.getCreationDate() != null)
            this.creationDate = user.getCreationDate().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

    }

    public java.lang.String getUsername() {
        return username;
    }

    public void setUsername(java.lang.String username) {
        this.username = username;
    }

    public java.lang.String getName() {
        return name;
    }

    public void setName(java.lang.String name) {
        this.name = name;
    }

    public java.lang.Integer getNumberOfTeacherQuizzes() {
        return numberOfTeacherQuizzes;
    }

    public void setNumberOfTeacherQuizzes(java.lang.Integer numberOfTeacherQuizzes) {
        this.numberOfTeacherQuizzes = numberOfTeacherQuizzes;
    }

    public java.lang.Integer getNumberOfStudentQuizzes() {
        return numberOfStudentQuizzes;
    }

    public void setNumberOfStudentQuizzes(java.lang.Integer numberOfStudentQuizzes) {
        this.numberOfStudentQuizzes = numberOfStudentQuizzes;
    }

    public java.lang.Integer getNumberOfAnswers() {
        return numberOfAnswers;
    }

    public void setNumberOfAnswers(java.lang.Integer numberOfAnswers) {
        this.numberOfAnswers = numberOfAnswers;
    }

    public java.lang.Integer getNumberOfTeacherAnswers() {
        return numberOfTeacherAnswers;
    }

    public void setNumberOfTeacherAnswers(java.lang.Integer numberOfTeacherAnswers) {
        this.numberOfTeacherAnswers = numberOfTeacherAnswers;
    }

    public int getPercentageOfCorrectAnswers() {
        return percentageOfCorrectAnswers;
    }

    public void setPercentageOfCorrectAnswers(int percentageOfCorrectAnswers) {
        this.percentageOfCorrectAnswers = percentageOfCorrectAnswers;
    }

    public int getPercentageOfCorrectTeacherAnswers() {
        return percentageOfCorrectTeacherAnswers;
    }

    public void setPercentageOfCorrectTeacherAnswers(int percentageOfCorrectTeacherAnswers) {
        this.percentageOfCorrectTeacherAnswers = percentageOfCorrectTeacherAnswers;
    }

    public java.lang.String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(java.lang.String creationDate) {
        this.creationDate = creationDate;
    }

    public java.lang.String getLastAccess() {
        return lastAccess;
    }

    public void setLastAccess(java.lang.String lastAccess) {
        this.lastAccess = lastAccess;
    }

    public java.lang.Integer getNumberOfInClassQuizzes() {
        return numberOfInClassQuizzes;
    }

    public void setNumberOfInClassQuizzes(java.lang.Integer numberOfInClassQuizzes) {
        this.numberOfInClassQuizzes = numberOfInClassQuizzes;
    }

    public java.lang.Integer getNumberOfInClassAnswers() {
        return numberOfInClassAnswers;
    }

    public void setNumberOfInClassAnswers(java.lang.Integer numberOfInClassAnswers) {
        this.numberOfInClassAnswers = numberOfInClassAnswers;
    }

    public java.lang.Integer getNumberOfStudentAnswers() {
        return numberOfStudentAnswers;
    }

    public void setNumberOfStudentAnswers(java.lang.Integer numberOfStudentAnswers) {
        this.numberOfStudentAnswers = numberOfStudentAnswers;
    }

    public int getPercentageOfCorrectInClassAnswers() {
        return percentageOfCorrectInClassAnswers;
    }

    public void setPercentageOfCorrectInClassAnswers(int percentageOfCorrectInClassAnswers) {
        this.percentageOfCorrectInClassAnswers = percentageOfCorrectInClassAnswers;
    }

    public int getPercentageOfCorrectStudentAnswers() {
        return percentageOfCorrectStudentAnswers;
    }

    public void setPercentageOfCorrectStudentAnswers(int percentageOfCorrectStudentAnswers) {
        this.percentageOfCorrectStudentAnswers = percentageOfCorrectStudentAnswers;
    }

    @java.lang.Override
    public java.lang.String toString() {
        return (((((((((((((((((((((((("StudentDto{" + "username='") + username) + '\'') + ", name='") + name) + '\'') + ", numberOfTeacherQuizzes=") + numberOfTeacherQuizzes) + ", numberOfStudentQuizzes=") + numberOfStudentQuizzes) + ", numberOfAnswers=") + numberOfAnswers) + ", numberOfTeacherAnswers=") + numberOfTeacherAnswers) + ", percentageOfCorrectAnswers=") + percentageOfCorrectAnswers) + ", percentageOfCorrectTeacherAnswers=") + percentageOfCorrectTeacherAnswers) + ", creationDate='") + creationDate) + '\'') + ", lastAccess='") + lastAccess) + '\'') + '}';
    }
}