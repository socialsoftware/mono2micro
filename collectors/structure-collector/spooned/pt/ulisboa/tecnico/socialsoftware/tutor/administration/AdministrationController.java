package pt.ulisboa.tecnico.socialsoftware.tutor.administration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
@pt.ulisboa.tecnico.socialsoftware.tutor.administration.RestController
public class AdministrationController {
    @org.springframework.beans.factory.annotation.Autowired
    private pt.ulisboa.tecnico.socialsoftware.tutor.administration.AdministrationService administrationService;

    @pt.ulisboa.tecnico.socialsoftware.tutor.administration.GetMapping("/admin/courses/executions")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_DEMO_ADMIN')")
    public java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseDto> getCourseExecutions(java.security.Principal principal) {
        pt.ulisboa.tecnico.socialsoftware.tutor.user.User user = ((pt.ulisboa.tecnico.socialsoftware.tutor.user.User) (((org.springframework.security.core.Authentication) (principal)).getPrincipal()));
        if (user == null) {
            throw new pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.AUTHENTICATION_ERROR);
        }
        return administrationService.getCourseExecutions(user.getRole());
    }

    @pt.ulisboa.tecnico.socialsoftware.tutor.administration.PostMapping("/admin/courses/executions")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ROLE_ADMIN') or (hasRole('ROLE_DEMO_ADMIN') and hasPermission(#courseDto, 'DEMO.ACCESS'))")
    public pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseDto createCourseExecution(@pt.ulisboa.tecnico.socialsoftware.tutor.administration.RequestBody
    pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseDto courseDto) {
        return administrationService.createExternalCourseExecution(courseDto);
    }

    @pt.ulisboa.tecnico.socialsoftware.tutor.administration.DeleteMapping("/admin/courses/executions/{courseExecutionId}")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ROLE_ADMIN') or (hasRole('ROLE_DEMO_ADMIN') and hasPermission(#courseExecutionId, 'DEMO.ACCESS'))")
    public org.springframework.http.ResponseEntity removeCourseExecution(@pt.ulisboa.tecnico.socialsoftware.tutor.administration.PathVariable
    java.lang.Integer courseExecutionId) {
        administrationService.removeCourseExecution(courseExecutionId);
        return org.springframework.http.ResponseEntity.ok().build();
    }
}