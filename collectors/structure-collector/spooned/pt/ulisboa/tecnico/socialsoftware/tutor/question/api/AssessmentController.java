package pt.ulisboa.tecnico.socialsoftware.tutor.question.api;
import javax.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
@pt.ulisboa.tecnico.socialsoftware.tutor.question.api.RestController
public class AssessmentController {
    private pt.ulisboa.tecnico.socialsoftware.tutor.question.AssessmentService assessmentService;

    AssessmentController(pt.ulisboa.tecnico.socialsoftware.tutor.question.AssessmentService assessmentService) {
        this.assessmentService = assessmentService;
    }

    @pt.ulisboa.tecnico.socialsoftware.tutor.question.api.GetMapping("/executions/{executionId}/assessments")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ROLE_TEACHER') and hasPermission(#executionId, 'EXECUTION.ACCESS')")
    public java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.AssessmentDto> getExecutionCourseAssessments(@pt.ulisboa.tecnico.socialsoftware.tutor.question.api.PathVariable
    int executionId) {
        return this.assessmentService.findAssessments(executionId);
    }

    @pt.ulisboa.tecnico.socialsoftware.tutor.question.api.GetMapping("/executions/{executionId}/assessments/available")
    @org.springframework.security.access.prepost.PreAuthorize("(hasRole('ROLE_TEACHER') and hasPermission(#executionId, 'EXECUTION.ACCESS')) " + "or (hasRole('ROLE_STUDENT') and hasPermission(#executionId, 'EXECUTION.ACCESS'))")
    public java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.AssessmentDto> getAvailableAssessments(@pt.ulisboa.tecnico.socialsoftware.tutor.question.api.PathVariable
    int executionId) {
        return this.assessmentService.findAvailableAssessments(executionId);
    }

    @pt.ulisboa.tecnico.socialsoftware.tutor.question.api.PostMapping("/executions/{executionId}/assessments")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ROLE_TEACHER') and hasPermission(#executionId, 'EXECUTION.ACCESS')")
    public pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.AssessmentDto createAssessment(@pt.ulisboa.tecnico.socialsoftware.tutor.question.api.PathVariable
    int executionId, @javax.validation.Valid
    @pt.ulisboa.tecnico.socialsoftware.tutor.question.api.RequestBody
    pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.AssessmentDto assessment) {
        return this.assessmentService.createAssessment(executionId, assessment);
    }

    @pt.ulisboa.tecnico.socialsoftware.tutor.question.api.PutMapping("/assessments/{assessmentId}")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ROLE_TEACHER') and hasPermission(#assessmentId, 'ASSESSMENT.ACCESS')")
    public pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.AssessmentDto updateAssessment(@pt.ulisboa.tecnico.socialsoftware.tutor.question.api.PathVariable
    java.lang.Integer assessmentId, @javax.validation.Valid
    @pt.ulisboa.tecnico.socialsoftware.tutor.question.api.RequestBody
    pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.AssessmentDto assessment) {
        return this.assessmentService.updateAssessment(assessmentId, assessment);
    }

    @pt.ulisboa.tecnico.socialsoftware.tutor.question.api.DeleteMapping("/assessments/{assessmentId}")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ROLE_TEACHER') and hasPermission(#assessmentId, 'ASSESSMENT.ACCESS')")
    public org.springframework.http.ResponseEntity removeAssessment(@pt.ulisboa.tecnico.socialsoftware.tutor.question.api.PathVariable
    java.lang.Integer assessmentId) {
        assessmentService.removeAssessment(assessmentId);
        return org.springframework.http.ResponseEntity.ok().build();
    }

    @pt.ulisboa.tecnico.socialsoftware.tutor.question.api.PostMapping("/assessments/{assessmentId}/set-status")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ROLE_TEACHER') and hasPermission(#assessmentId, 'ASSESSMENT.ACCESS')")
    public org.springframework.http.ResponseEntity assessmentSetStatus(@pt.ulisboa.tecnico.socialsoftware.tutor.question.api.PathVariable
    java.lang.Integer assessmentId, @javax.validation.Valid
    @pt.ulisboa.tecnico.socialsoftware.tutor.question.api.RequestBody
    java.lang.String status) {
        assessmentService.assessmentSetStatus(assessmentId, pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Assessment.Status.valueOf(status));
        return org.springframework.http.ResponseEntity.ok().build();
    }
}