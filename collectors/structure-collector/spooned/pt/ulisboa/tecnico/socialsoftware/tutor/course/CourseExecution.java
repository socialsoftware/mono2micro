package pt.ulisboa.tecnico.socialsoftware.tutor.course;
import javax.persistence.*;
@pt.ulisboa.tecnico.socialsoftware.tutor.course.Entity
@pt.ulisboa.tecnico.socialsoftware.tutor.course.Table(name = "course_executions")
public class CourseExecution {
    public enum Status {

        ACTIVE,
        INACTIVE,
        HISTORIC;}

    @pt.ulisboa.tecnico.socialsoftware.tutor.course.Id
    @pt.ulisboa.tecnico.socialsoftware.tutor.course.GeneratedValue(strategy = GenerationType.IDENTITY)
    private java.lang.Integer id;

    @pt.ulisboa.tecnico.socialsoftware.tutor.course.Enumerated(EnumType.STRING)
    private pt.ulisboa.tecnico.socialsoftware.tutor.course.Course.Type type;

    private java.lang.String acronym;

    private java.lang.String academicTerm;

    @pt.ulisboa.tecnico.socialsoftware.tutor.course.Enumerated(EnumType.STRING)
    private pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution.Status status;

    @pt.ulisboa.tecnico.socialsoftware.tutor.course.ManyToOne
    @pt.ulisboa.tecnico.socialsoftware.tutor.course.JoinColumn(name = "course_id")
    private pt.ulisboa.tecnico.socialsoftware.tutor.course.Course course;

    @pt.ulisboa.tecnico.socialsoftware.tutor.course.ManyToMany(mappedBy = "courseExecutions")
    private java.util.Set<pt.ulisboa.tecnico.socialsoftware.tutor.user.User> users = new java.util.HashSet<>();

    @pt.ulisboa.tecnico.socialsoftware.tutor.course.OneToMany(cascade = CascadeType.ALL, mappedBy = "courseExecution", fetch = FetchType.LAZY, orphanRemoval = true)
    private java.util.Set<pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz> quizzes = new java.util.HashSet<>();

    @pt.ulisboa.tecnico.socialsoftware.tutor.course.OneToMany(cascade = CascadeType.ALL, mappedBy = "courseExecution", fetch = FetchType.LAZY, orphanRemoval = true)
    private java.util.Set<pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Assessment> assessments = new java.util.HashSet<>();

    public CourseExecution() {
    }

    public CourseExecution(pt.ulisboa.tecnico.socialsoftware.tutor.course.Course course, java.lang.String acronym, java.lang.String academicTerm, pt.ulisboa.tecnico.socialsoftware.tutor.course.Course.Type type) {
        if ((acronym == null) || acronym.trim().isEmpty()) {
            throw new pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.COURSE_EXECUTION_ACRONYM_IS_EMPTY);
        }
        if ((academicTerm == null) || academicTerm.trim().isEmpty()) {
            throw new pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.COURSE_EXECUTION_ACADEMIC_TERM_IS_EMPTY);
        }
        if (course.existsCourseExecution(acronym, academicTerm, type)) {
            throw new pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.DUPLICATE_COURSE_EXECUTION, acronym + academicTerm);
        }
        this.type = type;
        this.course = course;
        this.acronym = acronym;
        this.academicTerm = academicTerm;
        this.status = pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution.Status.ACTIVE;
        course.addCourseExecution(this);
    }

    public void delete() {
        if ((!getQuizzes().isEmpty()) || (!getAssessments().isEmpty())) {
            throw new pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.CANNOT_DELETE_COURSE_EXECUTION, acronym + academicTerm);
        }
        course.getCourseExecutions().remove(this);
        users.forEach(user -> user.getCourseExecutions().remove(this));
    }

    public java.lang.Integer getId() {
        return id;
    }

    public void setId(java.lang.Integer id) {
        this.id = id;
    }

    public java.lang.String getAcronym() {
        return acronym;
    }

    public void setAcronym(java.lang.String acronym) {
        this.acronym = acronym;
    }

    public java.lang.String getAcademicTerm() {
        return academicTerm;
    }

    public void setAcademicTerm(java.lang.String academicTerm) {
        this.academicTerm = academicTerm;
    }

    public pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution.Status getStatus() {
        return status;
    }

    public void setStatus(pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution.Status status) {
        this.status = status;
    }

    public pt.ulisboa.tecnico.socialsoftware.tutor.course.Course getCourse() {
        return course;
    }

    public void setCourse(pt.ulisboa.tecnico.socialsoftware.tutor.course.Course course) {
        this.course = course;
    }

    public java.util.Set<pt.ulisboa.tecnico.socialsoftware.tutor.user.User> getUsers() {
        return users;
    }

    public java.util.Set<pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz> getQuizzes() {
        return quizzes;
    }

    public java.util.Set<pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Assessment> getAssessments() {
        return assessments;
    }

    public void addQuiz(pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz quiz) {
        quizzes.add(quiz);
    }

    public void addAssessment(pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Assessment assessment) {
        assessments.add(assessment);
    }

    public void addUser(pt.ulisboa.tecnico.socialsoftware.tutor.user.User user) {
        users.add(user);
    }

    public pt.ulisboa.tecnico.socialsoftware.tutor.course.Course.Type getType() {
        return type;
    }

    public void setType(pt.ulisboa.tecnico.socialsoftware.tutor.course.Course.Type type) {
        this.type = type;
    }
}