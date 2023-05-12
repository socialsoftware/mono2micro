package pt.ulisboa.tecnico.socialsoftware.tutor.course;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
@pt.ulisboa.tecnico.socialsoftware.tutor.course.RestController
public class CourseController {
    @org.springframework.beans.factory.annotation.Autowired
    private pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseService courseService;

    @pt.ulisboa.tecnico.socialsoftware.tutor.course.GetMapping("/courses/{courseId}")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ROLE_TEACHER') and hasPermission(#courseId, 'COURSE.ACCESS')")
    public java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseDto> getCourseExecutions(@pt.ulisboa.tecnico.socialsoftware.tutor.course.PathVariable
    int courseId) {
        return courseService.getCourseExecutions(courseId);
    }

    @pt.ulisboa.tecnico.socialsoftware.tutor.course.PostMapping("/courses")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ROLE_TEACHER') and hasPermission(#courseDto, 'EXECUTION.CREATE')")
    public pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseDto createCourseExecution(@pt.ulisboa.tecnico.socialsoftware.tutor.course.RequestBody
    pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseDto courseDto) {
        return courseService.createTecnicoCourseExecution(courseDto);
    }

    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ROLE_TEACHER') and hasPermission(#executionId, 'EXECUTION.ACCESS')")
    @pt.ulisboa.tecnico.socialsoftware.tutor.course.GetMapping("/executions/{executionId}/students")
    public java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.user.dto.StudentDto> getCourseStudents(@pt.ulisboa.tecnico.socialsoftware.tutor.course.PathVariable
    int executionId) {
        return courseService.courseStudents(executionId);
    }
}