package pt.ulisboa.tecnico.socialsoftware.tutor.user;
import javax.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
@pt.ulisboa.tecnico.socialsoftware.tutor.user.Entity
@pt.ulisboa.tecnico.socialsoftware.tutor.user.Table(name = "users")
public class User implements org.springframework.security.core.userdetails.UserDetails , pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.DomainEntity {
    public enum Role {

        STUDENT,
        TEACHER,
        ADMIN,
        DEMO_ADMIN;}

    @pt.ulisboa.tecnico.socialsoftware.tutor.user.Id
    @pt.ulisboa.tecnico.socialsoftware.tutor.user.GeneratedValue(strategy = GenerationType.IDENTITY)
    private java.lang.Integer id;

    @pt.ulisboa.tecnico.socialsoftware.tutor.user.Column(unique = true, nullable = false)
    private java.lang.Integer key;

    @pt.ulisboa.tecnico.socialsoftware.tutor.user.Enumerated(EnumType.STRING)
    private pt.ulisboa.tecnico.socialsoftware.tutor.user.User.Role role;

    @pt.ulisboa.tecnico.socialsoftware.tutor.user.Column(unique = true)
    private java.lang.String username;

    private java.lang.String name;

    private java.lang.String enrolledCoursesAcronyms;

    private java.lang.Integer numberOfTeacherQuizzes;

    private java.lang.Integer numberOfStudentQuizzes;

    private java.lang.Integer numberOfInClassQuizzes;

    private java.lang.Integer numberOfTeacherAnswers;

    private java.lang.Integer numberOfInClassAnswers;

    private java.lang.Integer numberOfStudentAnswers;

    private java.lang.Integer numberOfCorrectTeacherAnswers;

    private java.lang.Integer numberOfCorrectInClassAnswers;

    private java.lang.Integer numberOfCorrectStudentAnswers;

    @pt.ulisboa.tecnico.socialsoftware.tutor.user.Column(name = "creation_date")
    private java.time.LocalDateTime creationDate;

    @pt.ulisboa.tecnico.socialsoftware.tutor.user.Column(name = "last_access")
    private java.time.LocalDateTime lastAccess;

    @pt.ulisboa.tecnico.socialsoftware.tutor.user.OneToMany(cascade = CascadeType.ALL, mappedBy = "user", fetch = FetchType.LAZY, orphanRemoval = true)
    private java.util.Set<pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer> quizAnswers = new java.util.HashSet<>();

    @pt.ulisboa.tecnico.socialsoftware.tutor.user.ManyToMany
    private java.util.Set<pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution> courseExecutions = new java.util.HashSet<>();

    public User() {
    }

    public User(java.lang.String name, java.lang.String username, java.lang.Integer key, pt.ulisboa.tecnico.socialsoftware.tutor.user.User.Role role) {
        this.name = name;
        setUsername(username);
        this.key = key;
        this.role = role;
        this.creationDate = java.time.LocalDateTime.now();
        this.numberOfTeacherQuizzes = 0;
        this.numberOfInClassQuizzes = 0;
        this.numberOfStudentQuizzes = 0;
        this.numberOfTeacherAnswers = 0;
        this.numberOfInClassAnswers = 0;
        this.numberOfStudentAnswers = 0;
        this.numberOfCorrectTeacherAnswers = 0;
        this.numberOfCorrectInClassAnswers = 0;
        this.numberOfCorrectStudentAnswers = 0;
    }

    @java.lang.Override
    public void accept(pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.Visitor visitor) {
        visitor.visitUser(this);
    }

    public java.lang.Integer getId() {
        return id;
    }

    public void setId(java.lang.Integer id) {
        this.id = id;
    }

    public java.lang.Integer getKey() {
        return key;
    }

    public void setKey(java.lang.Integer key) {
        this.key = key;
    }

    @java.lang.Override
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

    public java.lang.String getEnrolledCoursesAcronyms() {
        return enrolledCoursesAcronyms;
    }

    public void setEnrolledCoursesAcronyms(java.lang.String enrolledCoursesAcronyms) {
        this.enrolledCoursesAcronyms = enrolledCoursesAcronyms;
    }

    public pt.ulisboa.tecnico.socialsoftware.tutor.user.User.Role getRole() {
        return role;
    }

    public void setRole(pt.ulisboa.tecnico.socialsoftware.tutor.user.User.Role role) {
        this.role = role;
    }

    public java.time.LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(java.time.LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public java.time.LocalDateTime getLastAccess() {
        return lastAccess;
    }

    public void setLastAccess(java.time.LocalDateTime lastAccess) {
        this.lastAccess = lastAccess;
    }

    public java.util.Set<pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer> getQuizAnswers() {
        return quizAnswers;
    }

    public java.util.Set<pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution> getCourseExecutions() {
        return courseExecutions;
    }

    public void setCourseExecutions(java.util.Set<pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution> courseExecutions) {
        this.courseExecutions = courseExecutions;
    }

    public java.lang.Integer getNumberOfTeacherQuizzes() {
        if (this.numberOfTeacherQuizzes == null)
            this.numberOfTeacherQuizzes = ((int) (getQuizAnswers().stream().filter(pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer::isCompleted).filter(quizAnswer -> quizAnswer.getQuiz().getType().equals(pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz.QuizType.PROPOSED)).count()));

        return numberOfTeacherQuizzes;
    }

    public void setNumberOfTeacherQuizzes(java.lang.Integer numberOfTeacherQuizzes) {
        this.numberOfTeacherQuizzes = numberOfTeacherQuizzes;
    }

    public java.lang.Integer getNumberOfStudentQuizzes() {
        if (this.numberOfStudentQuizzes == null)
            this.numberOfStudentQuizzes = ((int) (getQuizAnswers().stream().filter(pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer::isCompleted).filter(quizAnswer -> quizAnswer.getQuiz().getType().equals(pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz.QuizType.GENERATED)).count()));

        return numberOfStudentQuizzes;
    }

    public void setNumberOfStudentQuizzes(java.lang.Integer numberOfStudentQuizzes) {
        this.numberOfStudentQuizzes = numberOfStudentQuizzes;
    }

    public java.lang.Integer getNumberOfInClassQuizzes() {
        if (this.numberOfInClassQuizzes == null)
            this.numberOfInClassQuizzes = ((int) (getQuizAnswers().stream().filter(pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer::isCompleted).filter(quizAnswer -> quizAnswer.getQuiz().getType().equals(pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz.QuizType.IN_CLASS)).count()));

        return numberOfInClassQuizzes;
    }

    public void setNumberOfInClassQuizzes(java.lang.Integer numberOfInClassQuizzes) {
        this.numberOfInClassQuizzes = numberOfInClassQuizzes;
    }

    public java.lang.Integer getNumberOfTeacherAnswers() {
        if (this.numberOfTeacherAnswers == null)
            this.numberOfTeacherAnswers = getQuizAnswers().stream().filter(pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer::isCompleted).filter(quizAnswer -> quizAnswer.getQuiz().getType().equals(pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz.QuizType.PROPOSED)).mapToInt(quizAnswer -> quizAnswer.getQuiz().getQuizQuestions().size()).sum();

        return numberOfTeacherAnswers;
    }

    public void setNumberOfTeacherAnswers(java.lang.Integer numberOfTeacherAnswers) {
        this.numberOfTeacherAnswers = numberOfTeacherAnswers;
    }

    public java.lang.Integer getNumberOfInClassAnswers() {
        if (this.numberOfInClassAnswers == null)
            this.numberOfInClassAnswers = getQuizAnswers().stream().filter(pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer::isCompleted).filter(quizAnswer -> quizAnswer.getQuiz().getType().equals(pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz.QuizType.IN_CLASS)).mapToInt(quizAnswer -> quizAnswer.getQuiz().getQuizQuestions().size()).sum();

        return numberOfInClassAnswers;
    }

    public void setNumberOfInClassAnswers(java.lang.Integer numberOfInClassAnswers) {
        this.numberOfInClassAnswers = numberOfInClassAnswers;
    }

    public java.lang.Integer getNumberOfStudentAnswers() {
        if (this.numberOfStudentAnswers == null) {
            this.numberOfStudentAnswers = getQuizAnswers().stream().filter(pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer::isCompleted).filter(quizAnswer -> quizAnswer.getQuiz().getType().equals(pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz.QuizType.GENERATED)).mapToInt(quizAnswer -> quizAnswer.getQuiz().getQuizQuestions().size()).sum();
        }
        return numberOfStudentAnswers;
    }

    public void setNumberOfStudentAnswers(java.lang.Integer numberOfStudentAnswers) {
        this.numberOfStudentAnswers = numberOfStudentAnswers;
    }

    public java.lang.Integer getNumberOfCorrectTeacherAnswers() {
        if (this.numberOfCorrectTeacherAnswers == null)
            this.numberOfCorrectTeacherAnswers = ((int) (this.getQuizAnswers().stream().filter(pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer::isCompleted).filter(quizAnswer -> quizAnswer.getQuiz().getType().equals(pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz.QuizType.PROPOSED)).flatMap(quizAnswer -> quizAnswer.getQuestionAnswers().stream()).filter(questionAnswer -> (questionAnswer.getOption() != null) && questionAnswer.getOption().getCorrect()).count()));

        return numberOfCorrectTeacherAnswers;
    }

    public void setNumberOfCorrectTeacherAnswers(java.lang.Integer numberOfCorrectTeacherAnswers) {
        this.numberOfCorrectTeacherAnswers = numberOfCorrectTeacherAnswers;
    }

    public java.lang.Integer getNumberOfCorrectInClassAnswers() {
        if (this.numberOfCorrectInClassAnswers == null)
            this.numberOfCorrectInClassAnswers = ((int) (this.getQuizAnswers().stream().filter(pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer::isCompleted).filter(quizAnswer -> quizAnswer.getQuiz().getType().equals(pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz.QuizType.IN_CLASS)).flatMap(quizAnswer -> quizAnswer.getQuestionAnswers().stream()).filter(questionAnswer -> (questionAnswer.getOption() != null) && questionAnswer.getOption().getCorrect()).count()));

        return numberOfCorrectInClassAnswers;
    }

    public void setNumberOfCorrectInClassAnswers(java.lang.Integer numberOfCorrectInClassAnswers) {
        this.numberOfCorrectInClassAnswers = numberOfCorrectInClassAnswers;
    }

    public java.lang.Integer getNumberOfCorrectStudentAnswers() {
        if (this.numberOfCorrectStudentAnswers == null)
            this.numberOfCorrectStudentAnswers = ((int) (this.getQuizAnswers().stream().filter(pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer::isCompleted).filter(quizAnswer -> quizAnswer.getQuiz().getType().equals(pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz.QuizType.GENERATED)).flatMap(quizAnswer -> quizAnswer.getQuestionAnswers().stream()).filter(questionAnswer -> (questionAnswer.getOption() != null) && questionAnswer.getOption().getCorrect()).count()));

        return numberOfCorrectStudentAnswers;
    }

    public void setNumberOfCorrectStudentAnswers(java.lang.Integer numberOfCorrectStudentAnswers) {
        this.numberOfCorrectStudentAnswers = numberOfCorrectStudentAnswers;
    }

    public void increaseNumberOfQuizzes(pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz.QuizType type) {
        switch (type) {
            case PROPOSED :
                this.numberOfTeacherQuizzes = getNumberOfTeacherQuizzes() + 1;
                break;
            case IN_CLASS :
                this.numberOfInClassQuizzes = getNumberOfInClassQuizzes() + 1;
                break;
            case GENERATED :
                this.numberOfStudentQuizzes = getNumberOfStudentQuizzes() + 1;
                break;
            default :
                break;
        }
    }

    public void increaseNumberOfAnswers(pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz.QuizType type) {
        switch (type) {
            case PROPOSED :
                this.numberOfTeacherAnswers = getNumberOfTeacherAnswers() + 1;
                break;
            case IN_CLASS :
                this.numberOfInClassAnswers = getNumberOfInClassAnswers() + 1;
                break;
            case GENERATED :
                this.numberOfStudentAnswers = getNumberOfStudentAnswers() + 1;
                break;
            default :
                break;
        }
    }

    public void increaseNumberOfCorrectAnswers(pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz.QuizType type) {
        switch (type) {
            case PROPOSED :
                this.numberOfCorrectTeacherAnswers = getNumberOfCorrectTeacherAnswers() + 1;
                break;
            case IN_CLASS :
                this.numberOfCorrectInClassAnswers = getNumberOfCorrectInClassAnswers() + 1;
                break;
            case GENERATED :
                this.numberOfCorrectStudentAnswers = getNumberOfCorrectStudentAnswers() + 1;
                break;
            default :
                break;
        }
    }

    public void addQuizAnswer(pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer quizAnswer) {
        this.quizAnswers.add(quizAnswer);
    }

    public void addCourse(pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution course) {
        this.courseExecutions.add(course);
    }

    @java.lang.Override
    public java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> getAuthorities() {
        java.util.List<org.springframework.security.core.GrantedAuthority> list = new java.util.ArrayList<>();
        list.add(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + role));
        return list;
    }

    @java.lang.Override
    public java.lang.String getPassword() {
        return null;
    }

    @java.lang.Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @java.lang.Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @java.lang.Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @java.lang.Override
    public boolean isEnabled() {
        return true;
    }

    public java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question> filterQuestionsByStudentModel(java.lang.Integer numberOfQuestions, java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question> availableQuestions) {
        java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question> studentAnsweredQuestions = getQuizAnswers().stream().flatMap(quizAnswer -> quizAnswer.getQuestionAnswers().stream()).filter(questionAnswer -> availableQuestions.contains(questionAnswer.getQuizQuestion().getQuestion())).filter(questionAnswer -> (questionAnswer.getTimeTaken() != null) && (questionAnswer.getTimeTaken() != 0)).map(questionAnswer -> questionAnswer.getQuizQuestion().getQuestion()).collect(java.util.stream.Collectors.toList());
        java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question> notAnsweredQuestions = availableQuestions.stream().filter(question -> !studentAnsweredQuestions.contains(question)).collect(java.util.stream.Collectors.toList());
        java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question> result = new java.util.ArrayList<>();
        // add 80% of notanswered questions
        // may add less if not enough notanswered
        int numberOfAddedQuestions = 0;
        while ((numberOfAddedQuestions < (numberOfQuestions * 0.8)) && (notAnsweredQuestions.size() >= (numberOfAddedQuestions + 1))) {
            result.add(notAnsweredQuestions.get(numberOfAddedQuestions++));
        } 
        // add notanswered questions if there is not enough answered questions
        // it is ok because the total id of available questions > numberOfQuestions
        while ((studentAnsweredQuestions.size() + numberOfAddedQuestions) < numberOfQuestions) {
            result.add(notAnsweredQuestions.get(numberOfAddedQuestions++));
        } 
        // add answered questions
        java.util.Random rand = new java.util.Random(java.lang.System.currentTimeMillis());
        while (numberOfAddedQuestions < numberOfQuestions) {
            int next = rand.nextInt(studentAnsweredQuestions.size());
            if (!result.contains(studentAnsweredQuestions.get(next))) {
                result.add(studentAnsweredQuestions.get(next));
                numberOfAddedQuestions++;
            }
        } 
        return result;
    }
}