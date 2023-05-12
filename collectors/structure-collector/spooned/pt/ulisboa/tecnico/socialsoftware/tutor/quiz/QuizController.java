package pt.ulisboa.tecnico.socialsoftware.tutor.quiz;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
@pt.ulisboa.tecnico.socialsoftware.tutor.quiz.RestController
public class QuizController {
    @org.springframework.beans.factory.annotation.Autowired
    private pt.ulisboa.tecnico.socialsoftware.tutor.quiz.QuizService quizService;

    @pt.ulisboa.tecnico.socialsoftware.tutor.quiz.GetMapping("/executions/{executionId}/quizzes/non-generated")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ROLE_TEACHER') and hasPermission(#executionId, 'EXECUTION.ACCESS')")
    public java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.quiz.dto.QuizDto> findNonGeneratedQuizzes(@pt.ulisboa.tecnico.socialsoftware.tutor.quiz.PathVariable
    int executionId) {
        return quizService.findNonGeneratedQuizzes(executionId);
    }

    @pt.ulisboa.tecnico.socialsoftware.tutor.quiz.PostMapping("/executions/{executionId}/quizzes")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ROLE_TEACHER') and hasPermission(#executionId, 'EXECUTION.ACCESS')")
    public pt.ulisboa.tecnico.socialsoftware.tutor.quiz.dto.QuizDto createQuiz(@pt.ulisboa.tecnico.socialsoftware.tutor.quiz.PathVariable
    int executionId, @javax.validation.Valid
    @pt.ulisboa.tecnico.socialsoftware.tutor.quiz.RequestBody
    pt.ulisboa.tecnico.socialsoftware.tutor.quiz.dto.QuizDto quiz) {
        formatDates(quiz);
        return this.quizService.createQuiz(executionId, quiz);
    }

    @pt.ulisboa.tecnico.socialsoftware.tutor.quiz.GetMapping("/quizzes/{quizId}")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ROLE_TEACHER') and hasPermission(#quizId, 'QUIZ.ACCESS')")
    public pt.ulisboa.tecnico.socialsoftware.tutor.quiz.dto.QuizDto getQuiz(@pt.ulisboa.tecnico.socialsoftware.tutor.quiz.PathVariable
    java.lang.Integer quizId) {
        return this.quizService.findById(quizId);
    }

    @pt.ulisboa.tecnico.socialsoftware.tutor.quiz.PutMapping("/quizzes/{quizId}")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ROLE_TEACHER') and hasPermission(#quizId, 'QUIZ.ACCESS')")
    public pt.ulisboa.tecnico.socialsoftware.tutor.quiz.dto.QuizDto updateQuiz(@pt.ulisboa.tecnico.socialsoftware.tutor.quiz.PathVariable
    java.lang.Integer quizId, @javax.validation.Valid
    @pt.ulisboa.tecnico.socialsoftware.tutor.quiz.RequestBody
    pt.ulisboa.tecnico.socialsoftware.tutor.quiz.dto.QuizDto quiz) {
        formatDates(quiz);
        return this.quizService.updateQuiz(quizId, quiz);
    }

    @pt.ulisboa.tecnico.socialsoftware.tutor.quiz.DeleteMapping("/quizzes/{quizId}")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ROLE_TEACHER') and hasPermission(#quizId, 'QUIZ.ACCESS')")
    public org.springframework.http.ResponseEntity deleteQuiz(@pt.ulisboa.tecnico.socialsoftware.tutor.quiz.PathVariable
    java.lang.Integer quizId) {
        quizService.removeQuiz(quizId);
        return org.springframework.http.ResponseEntity.ok().build();
    }

    @pt.ulisboa.tecnico.socialsoftware.tutor.quiz.GetMapping("/quizzes/{quizId}/export")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ROLE_TEACHER') and hasPermission(#quizId, 'QUIZ.ACCESS')")
    public void exportQuiz(javax.servlet.http.HttpServletResponse response, @pt.ulisboa.tecnico.socialsoftware.tutor.quiz.PathVariable
    java.lang.Integer quizId) throws java.io.IOException {
        response.setHeader("Content-Disposition", "attachment; filename=file.zip");
        response.setContentType("application/zip");
        response.getOutputStream().write(this.quizService.exportQuiz(quizId).toByteArray());
        response.flushBuffer();
    }

    @pt.ulisboa.tecnico.socialsoftware.tutor.quiz.GetMapping("/quizzes/{quizId}/answers")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ROLE_TEACHER') and hasPermission(#quizId, 'QUIZ.ACCESS')")
    public pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.QuizAnswersDto getQuizAnswers(@pt.ulisboa.tecnico.socialsoftware.tutor.quiz.PathVariable
    java.lang.Integer quizId) {
        return this.quizService.getQuizAnswers(quizId);
    }

    private void formatDates(pt.ulisboa.tecnico.socialsoftware.tutor.quiz.dto.QuizDto quiz) {
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        if ((quiz.getAvailableDate() != null) && (!quiz.getAvailableDate().matches("(\\d{4})-(\\d{2})-(\\d{2}) (\\d{2}):(\\d{2})"))) {
            quiz.setAvailableDate(java.time.LocalDateTime.parse(quiz.getAvailableDate().replaceAll(".$", ""), java.time.format.DateTimeFormatter.ISO_DATE_TIME).format(formatter));
        }
        if ((quiz.getConclusionDate() != null) && (!quiz.getConclusionDate().matches("(\\d{4})-(\\d{2})-(\\d{2}) (\\d{2}):(\\d{2})")))
            quiz.setConclusionDate(java.time.LocalDateTime.parse(quiz.getConclusionDate().replaceAll(".$", ""), java.time.format.DateTimeFormatter.ISO_DATE_TIME).format(formatter));

    }
}