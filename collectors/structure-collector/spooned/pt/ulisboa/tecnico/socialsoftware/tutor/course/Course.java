package pt.ulisboa.tecnico.socialsoftware.tutor.course;
import javax.persistence.*;
@pt.ulisboa.tecnico.socialsoftware.tutor.course.Entity
@pt.ulisboa.tecnico.socialsoftware.tutor.course.Table(name = "courses")
public class Course {
    public static final java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public enum Type {

        TECNICO,
        EXTERNAL;}

    @pt.ulisboa.tecnico.socialsoftware.tutor.course.Id
    @pt.ulisboa.tecnico.socialsoftware.tutor.course.GeneratedValue(strategy = GenerationType.IDENTITY)
    private java.lang.Integer id;

    @pt.ulisboa.tecnico.socialsoftware.tutor.course.Enumerated(EnumType.STRING)
    private pt.ulisboa.tecnico.socialsoftware.tutor.course.Course.Type type;

    private java.lang.String name;

    @pt.ulisboa.tecnico.socialsoftware.tutor.course.OneToMany(cascade = CascadeType.ALL, mappedBy = "course", fetch = FetchType.LAZY, orphanRemoval = true)
    private java.util.Set<pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution> courseExecutions = new java.util.HashSet<>();

    @pt.ulisboa.tecnico.socialsoftware.tutor.course.OneToMany(cascade = CascadeType.ALL, mappedBy = "course", fetch = FetchType.LAZY, orphanRemoval = true)
    private java.util.Set<pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question> questions = new java.util.HashSet<>();

    @pt.ulisboa.tecnico.socialsoftware.tutor.course.OneToMany(cascade = CascadeType.ALL, mappedBy = "course", fetch = FetchType.LAZY, orphanRemoval = true)
    private java.util.Set<pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic> topics = new java.util.HashSet<>();

    public Course() {
    }

    public Course(java.lang.String name, pt.ulisboa.tecnico.socialsoftware.tutor.course.Course.Type type) {
        if ((name == null) || name.trim().isEmpty()) {
            throw new pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.COURSE_NAME_IS_EMPTY);
        }
        this.type = type;
        this.name = name;
    }

    public java.util.Optional<pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution> getCourseExecution(java.lang.String acronym, java.lang.String academicTerm, pt.ulisboa.tecnico.socialsoftware.tutor.course.Course.Type type) {
        return getCourseExecutions().stream().filter(courseExecution -> (courseExecution.getType().equals(type) && courseExecution.getAcronym().equals(acronym)) && courseExecution.getAcademicTerm().equals(academicTerm)).findAny();
    }

    public boolean existsCourseExecution(java.lang.String acronym, java.lang.String academicTerm, pt.ulisboa.tecnico.socialsoftware.tutor.course.Course.Type type) {
        return getCourseExecution(acronym, academicTerm, type).isPresent();
    }

    public java.lang.Integer getId() {
        return id;
    }

    public void setId(java.lang.Integer id) {
        this.id = id;
    }

    public java.lang.String getName() {
        return name;
    }

    public void setName(java.lang.String name) {
        this.name = name;
    }

    public java.util.Set<pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution> getCourseExecutions() {
        return courseExecutions;
    }

    public java.util.Set<pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question> getQuestions() {
        return questions;
    }

    public java.util.Set<pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic> getTopics() {
        return topics;
    }

    public void addCourseExecution(pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution courseExecution) {
        courseExecutions.add(courseExecution);
    }

    public void addQuestion(pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question question) {
        questions.add(question);
    }

    public void addTopic(pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic topic) {
        topics.add(topic);
    }

    public pt.ulisboa.tecnico.socialsoftware.tutor.course.Course.Type getType() {
        return type;
    }

    public void setType(pt.ulisboa.tecnico.socialsoftware.tutor.course.Course.Type type) {
        this.type = type;
    }
}