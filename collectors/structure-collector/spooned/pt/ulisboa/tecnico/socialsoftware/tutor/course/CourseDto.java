package pt.ulisboa.tecnico.socialsoftware.tutor.course;
public class CourseDto implements java.io.Serializable {
    private int courseId;

    private pt.ulisboa.tecnico.socialsoftware.tutor.course.Course.Type courseType;

    private java.lang.String name;

    private int courseExecutionId;

    private pt.ulisboa.tecnico.socialsoftware.tutor.course.Course.Type courseExecutionType;

    private java.lang.String acronym;

    private java.lang.String academicTerm;

    private pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution.Status status;

    public CourseDto() {
    }

    public CourseDto(pt.ulisboa.tecnico.socialsoftware.tutor.course.Course course) {
        this.courseId = course.getId();
        this.courseType = course.getType();
        this.name = course.getName();
    }

    public CourseDto(pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution courseExecution) {
        this.courseId = courseExecution.getCourse().getId();
        this.courseType = courseExecution.getCourse().getType();
        this.name = courseExecution.getCourse().getName();
        this.courseExecutionId = courseExecution.getId();
        this.courseExecutionType = courseExecution.getType();
        this.acronym = courseExecution.getAcronym();
        this.academicTerm = courseExecution.getAcademicTerm();
        this.status = courseExecution.getStatus();
    }

    public CourseDto(java.lang.String name, java.lang.String acronym, java.lang.String academicTerm) {
        this.name = name;
        this.acronym = acronym;
        this.academicTerm = academicTerm;
    }

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public int getCourseExecutionId() {
        return courseExecutionId;
    }

    public void setCourseExecutionId(int courseExecutionId) {
        this.courseExecutionId = courseExecutionId;
    }

    public java.lang.String getName() {
        return name;
    }

    public void setName(java.lang.String name) {
        this.name = name;
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

    public pt.ulisboa.tecnico.socialsoftware.tutor.course.Course.Type getCourseType() {
        return courseType;
    }

    public void setCourseType(pt.ulisboa.tecnico.socialsoftware.tutor.course.Course.Type courseType) {
        this.courseType = courseType;
    }

    public pt.ulisboa.tecnico.socialsoftware.tutor.course.Course.Type getCourseExecutionType() {
        return courseExecutionType;
    }

    public void setCourseExecutionType(pt.ulisboa.tecnico.socialsoftware.tutor.course.Course.Type courseExecutionType) {
        this.courseExecutionType = courseExecutionType;
    }
}