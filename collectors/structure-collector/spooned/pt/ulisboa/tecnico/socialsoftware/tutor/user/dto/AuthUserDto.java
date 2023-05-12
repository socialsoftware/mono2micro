package pt.ulisboa.tecnico.socialsoftware.tutor.user.dto;
public class AuthUserDto implements java.io.Serializable {
    private java.lang.String name;

    private java.lang.String username;

    private pt.ulisboa.tecnico.socialsoftware.tutor.user.User.Role role;

    private java.util.Map<java.lang.String, java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseDto>> courses;

    public AuthUserDto(pt.ulisboa.tecnico.socialsoftware.tutor.user.User user) {
        this.name = user.getName();
        this.username = user.getUsername();
        this.role = user.getRole();
        this.courses = getActiveAndInactiveCourses(user, new java.util.ArrayList<>());
    }

    public AuthUserDto(pt.ulisboa.tecnico.socialsoftware.tutor.user.User user, java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseDto> currentCourses) {
        this.name = user.getName();
        this.username = user.getUsername();
        this.role = user.getRole();
        this.courses = getActiveAndInactiveCourses(user, currentCourses);
    }

    public java.lang.String getName() {
        return name;
    }

    public void setName(java.lang.String name) {
        this.name = name;
    }

    public java.lang.String getUsername() {
        return username;
    }

    public void setUsername(java.lang.String username) {
        this.username = username;
    }

    public pt.ulisboa.tecnico.socialsoftware.tutor.user.User.Role getRole() {
        return role;
    }

    public void setRole(pt.ulisboa.tecnico.socialsoftware.tutor.user.User.Role role) {
        this.role = role;
    }

    public java.util.Map<java.lang.String, java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseDto>> getCourses() {
        return courses;
    }

    public void setCourses(java.util.Map<java.lang.String, java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseDto>> courses) {
        this.courses = courses;
    }

    private java.util.Map<java.lang.String, java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseDto>> getActiveAndInactiveCourses(pt.ulisboa.tecnico.socialsoftware.tutor.user.User user, java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseDto> courses) {
        java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseDto> courseExecutions = user.getCourseExecutions().stream().map(pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseDto::new).collect(java.util.stream.Collectors.toList());
        courses.stream().forEach(courseDto -> {
            if (courseExecutions.stream().noneMatch(c -> c.getAcronym().equals(courseDto.getAcronym()) && c.getAcademicTerm().equals(courseDto.getAcademicTerm()))) {
                if (courseDto.getStatus() == null) {
                    courseDto.setStatus(pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution.Status.INACTIVE);
                }
                courseExecutions.add(courseDto);
            }
        });
        return courseExecutions.stream().sorted(java.util.Comparator.comparing(pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseDto::getName).thenComparing(pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseDto::getAcademicTerm).reversed()).collect(java.util.stream.Collectors.groupingBy(pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseDto::getName, java.util.stream.Collectors.mapping(courseDto -> courseDto, java.util.stream.Collectors.toList())));
    }
}