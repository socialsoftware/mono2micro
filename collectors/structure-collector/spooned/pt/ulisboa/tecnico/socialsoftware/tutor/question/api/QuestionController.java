package pt.ulisboa.tecnico.socialsoftware.tutor.question.api;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
@pt.ulisboa.tecnico.socialsoftware.tutor.question.api.RestController
public class QuestionController {
    private static org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(pt.ulisboa.tecnico.socialsoftware.tutor.question.api.QuestionController.class);

    private pt.ulisboa.tecnico.socialsoftware.tutor.question.QuestionService questionService;

    @org.springframework.beans.factory.annotation.Value("${figures.dir}")
    private java.lang.String figuresDir;

    QuestionController(pt.ulisboa.tecnico.socialsoftware.tutor.question.QuestionService questionService) {
        this.questionService = questionService;
    }

    @pt.ulisboa.tecnico.socialsoftware.tutor.question.api.GetMapping("/courses/{courseId}/questions")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ROLE_TEACHER') and hasPermission(#courseId, 'COURSE.ACCESS')")
    public java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto> getCourseQuestions(@pt.ulisboa.tecnico.socialsoftware.tutor.question.api.PathVariable
    int courseId) {
        return this.questionService.findQuestions(courseId);
    }

    @pt.ulisboa.tecnico.socialsoftware.tutor.question.api.GetMapping("/courses/{courseId}/questions/export")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ROLE_TEACHER') and hasPermission(#courseId, 'COURSE.ACCESS')")
    public void exportQuestions(javax.servlet.http.HttpServletResponse response, @pt.ulisboa.tecnico.socialsoftware.tutor.question.api.PathVariable
    int courseId) throws java.io.IOException {
        response.setHeader("Content-Disposition", "attachment; filename=file.zip");
        response.setContentType("application/zip");
        response.getOutputStream().write(this.questionService.exportCourseQuestions(courseId).toByteArray());
        response.flushBuffer();
    }

    @pt.ulisboa.tecnico.socialsoftware.tutor.question.api.GetMapping("/courses/{courseId}/questions/available")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ROLE_TEACHER') and hasPermission(#courseId, 'COURSE.ACCESS')")
    public java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto> getAvailableQuestions(@pt.ulisboa.tecnico.socialsoftware.tutor.question.api.PathVariable
    int courseId) {
        return this.questionService.findAvailableQuestions(courseId);
    }

    @pt.ulisboa.tecnico.socialsoftware.tutor.question.api.PostMapping("/courses/{courseId}/questions")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ROLE_TEACHER') and hasPermission(#courseId, 'COURSE.ACCESS')")
    public pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto createQuestion(@pt.ulisboa.tecnico.socialsoftware.tutor.question.api.PathVariable
    int courseId, @javax.validation.Valid
    @pt.ulisboa.tecnico.socialsoftware.tutor.question.api.RequestBody
    pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto question) {
        question.setStatus(pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question.Status.AVAILABLE.name());
        return this.questionService.createQuestion(courseId, question);
    }

    @pt.ulisboa.tecnico.socialsoftware.tutor.question.api.GetMapping("/questions/{questionId}")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ROLE_TEACHER') and hasPermission(#questionId, 'QUESTION.ACCESS')")
    public pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto getQuestion(@pt.ulisboa.tecnico.socialsoftware.tutor.question.api.PathVariable
    java.lang.Integer questionId) {
        return this.questionService.findQuestionById(questionId);
    }

    @pt.ulisboa.tecnico.socialsoftware.tutor.question.api.PutMapping("/questions/{questionId}")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ROLE_TEACHER') and hasPermission(#questionId, 'QUESTION.ACCESS')")
    public pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto updateQuestion(@pt.ulisboa.tecnico.socialsoftware.tutor.question.api.PathVariable
    java.lang.Integer questionId, @javax.validation.Valid
    @pt.ulisboa.tecnico.socialsoftware.tutor.question.api.RequestBody
    pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto question) {
        return this.questionService.updateQuestion(questionId, question);
    }

    @pt.ulisboa.tecnico.socialsoftware.tutor.question.api.DeleteMapping("/questions/{questionId}")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ROLE_TEACHER') and hasPermission(#questionId, 'QUESTION.ACCESS')")
    public org.springframework.http.ResponseEntity removeQuestion(@pt.ulisboa.tecnico.socialsoftware.tutor.question.api.PathVariable
    java.lang.Integer questionId) throws java.io.IOException {
        pt.ulisboa.tecnico.socialsoftware.tutor.question.api.QuestionController.logger.debug("removeQuestion questionId: {}: ", questionId);
        pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto questionDto = questionService.findQuestionById(questionId);
        java.lang.String url = (questionDto.getImage() != null) ? questionDto.getImage().getUrl() : null;
        questionService.removeQuestion(questionId);
        if ((url != null) && java.nio.file.Files.exists(getTargetLocation(url))) {
            java.nio.file.Files.delete(getTargetLocation(url));
        }
        return org.springframework.http.ResponseEntity.ok().build();
    }

    @pt.ulisboa.tecnico.socialsoftware.tutor.question.api.PostMapping("/questions/{questionId}/set-status")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ROLE_TEACHER') and hasPermission(#questionId, 'QUESTION.ACCESS')")
    public org.springframework.http.ResponseEntity questionSetStatus(@pt.ulisboa.tecnico.socialsoftware.tutor.question.api.PathVariable
    java.lang.Integer questionId, @javax.validation.Valid
    @pt.ulisboa.tecnico.socialsoftware.tutor.question.api.RequestBody
    java.lang.String status) {
        pt.ulisboa.tecnico.socialsoftware.tutor.question.api.QuestionController.logger.debug("questionSetStatus questionId: {}: ", questionId);
        questionService.questionSetStatus(questionId, pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question.Status.valueOf(status));
        return org.springframework.http.ResponseEntity.ok().build();
    }

    @pt.ulisboa.tecnico.socialsoftware.tutor.question.api.PutMapping("/questions/{questionId}/image")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ROLE_TEACHER') and hasPermission(#questionId, 'QUESTION.ACCESS')")
    public java.lang.String uploadImage(@pt.ulisboa.tecnico.socialsoftware.tutor.question.api.PathVariable
    java.lang.Integer questionId, @pt.ulisboa.tecnico.socialsoftware.tutor.question.api.RequestParam("file")
    org.springframework.web.multipart.MultipartFile file) throws java.io.IOException {
        pt.ulisboa.tecnico.socialsoftware.tutor.question.api.QuestionController.logger.debug("uploadImage  questionId: {}: , filename: {}", questionId, file.getContentType());
        pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto questionDto = questionService.findQuestionById(questionId);
        java.lang.String url = (questionDto.getImage() != null) ? questionDto.getImage().getUrl() : null;
        if ((url != null) && java.nio.file.Files.exists(getTargetLocation(url))) {
            java.nio.file.Files.delete(getTargetLocation(url));
        }
        int lastIndex = java.util.Objects.requireNonNull(file.getContentType()).lastIndexOf('/');
        java.lang.String type = file.getContentType().substring(lastIndex + 1);
        questionService.uploadImage(questionId, type);
        url = questionService.findQuestionById(questionId).getImage().getUrl();
        java.nio.file.Files.copy(file.getInputStream(), getTargetLocation(url), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        return url;
    }

    @pt.ulisboa.tecnico.socialsoftware.tutor.question.api.PutMapping("/questions/{questionId}/topics")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ROLE_TEACHER') and hasPermission(#questionId, 'QUESTION.ACCESS')")
    public org.springframework.http.ResponseEntity updateQuestionTopics(@pt.ulisboa.tecnico.socialsoftware.tutor.question.api.PathVariable
    java.lang.Integer questionId, @pt.ulisboa.tecnico.socialsoftware.tutor.question.api.RequestBody
    pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto[] topics) {
        questionService.updateQuestionTopics(questionId, topics);
        return org.springframework.http.ResponseEntity.ok().build();
    }

    private java.nio.file.Path getTargetLocation(java.lang.String url) {
        java.lang.String fileLocation = figuresDir + url;
        return java.nio.file.Paths.get(fileLocation);
    }
}