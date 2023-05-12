package pt.ulisboa.tecnico.socialsoftware.tutor.statement;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
@pt.ulisboa.tecnico.socialsoftware.tutor.statement.RestController
public class StatementController {
    @org.springframework.beans.factory.annotation.Autowired
    private pt.ulisboa.tecnico.socialsoftware.tutor.statement.StatementService statementService;

    @pt.ulisboa.tecnico.socialsoftware.tutor.statement.GetMapping("/executions/{executionId}/quizzes/available")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ROLE_STUDENT') and hasPermission(#executionId, 'EXECUTION.ACCESS')")
    public java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.statement.dto.StatementQuizDto> getAvailableQuizzes(java.security.Principal principal, @pt.ulisboa.tecnico.socialsoftware.tutor.statement.PathVariable
    int executionId) {
        pt.ulisboa.tecnico.socialsoftware.tutor.user.User user = ((pt.ulisboa.tecnico.socialsoftware.tutor.user.User) (((org.springframework.security.core.Authentication) (principal)).getPrincipal()));
        if (user == null) {
            throw new pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.AUTHENTICATION_ERROR);
        }
        return statementService.getAvailableQuizzes(user.getUsername(), executionId);
    }

    @pt.ulisboa.tecnico.socialsoftware.tutor.statement.PostMapping("/executions/{executionId}/quizzes/generate")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ROLE_STUDENT') and hasPermission(#executionId, 'EXECUTION.ACCESS')")
    public pt.ulisboa.tecnico.socialsoftware.tutor.statement.dto.StatementQuizDto getNewQuiz(java.security.Principal principal, @pt.ulisboa.tecnico.socialsoftware.tutor.statement.PathVariable
    int executionId, @pt.ulisboa.tecnico.socialsoftware.tutor.statement.RequestBody
    pt.ulisboa.tecnico.socialsoftware.tutor.statement.dto.StatementCreationDto quizDetails) {
        pt.ulisboa.tecnico.socialsoftware.tutor.user.User user = ((pt.ulisboa.tecnico.socialsoftware.tutor.user.User) (((org.springframework.security.core.Authentication) (principal)).getPrincipal()));
        if (user == null) {
            throw new pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.AUTHENTICATION_ERROR);
        }
        return statementService.generateStudentQuiz(user.getUsername(), executionId, quizDetails);
    }

    @pt.ulisboa.tecnico.socialsoftware.tutor.statement.GetMapping("/executions/{executionId}/quizzes/solved")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ROLE_STUDENT') and hasPermission(#executionId, 'EXECUTION.ACCESS')")
    public java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.statement.dto.SolvedQuizDto> getSolvedQuizzes(java.security.Principal principal, @pt.ulisboa.tecnico.socialsoftware.tutor.statement.PathVariable
    int executionId) {
        pt.ulisboa.tecnico.socialsoftware.tutor.user.User user = ((pt.ulisboa.tecnico.socialsoftware.tutor.user.User) (((org.springframework.security.core.Authentication) (principal)).getPrincipal()));
        if (user == null) {
            throw new pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.AUTHENTICATION_ERROR);
        }
        return statementService.getSolvedQuizzes(user.getUsername(), executionId);
    }

    @pt.ulisboa.tecnico.socialsoftware.tutor.statement.GetMapping("/quizzes/{quizId}/byqrcode")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ROLE_STUDENT') and hasPermission(#quizId, 'QUIZ.ACCESS')")
    public pt.ulisboa.tecnico.socialsoftware.tutor.statement.dto.StatementQuizDto getQuizByQRCode(java.security.Principal principal, @pt.ulisboa.tecnico.socialsoftware.tutor.statement.PathVariable
    int quizId) {
        pt.ulisboa.tecnico.socialsoftware.tutor.user.User user = ((pt.ulisboa.tecnico.socialsoftware.tutor.user.User) (((org.springframework.security.core.Authentication) (principal)).getPrincipal()));
        if (user == null) {
            throw new pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.AUTHENTICATION_ERROR);
        }
        return statementService.getQuizByQRCode(user.getUsername(), quizId);
    }

    @pt.ulisboa.tecnico.socialsoftware.tutor.statement.PostMapping("/quizzes/{quizId}/submit")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ROLE_STUDENT') and hasPermission(#quizId, 'QUIZ.ACCESS')")
    public void submitAnswer(java.security.Principal principal, @pt.ulisboa.tecnico.socialsoftware.tutor.statement.PathVariable
    int quizId, @javax.validation.Valid
    @pt.ulisboa.tecnico.socialsoftware.tutor.statement.RequestBody
    pt.ulisboa.tecnico.socialsoftware.tutor.statement.dto.StatementAnswerDto answer) {
        pt.ulisboa.tecnico.socialsoftware.tutor.user.User user = ((pt.ulisboa.tecnico.socialsoftware.tutor.user.User) (((org.springframework.security.core.Authentication) (principal)).getPrincipal()));
        if (user == null) {
            throw new pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.AUTHENTICATION_ERROR);
        }
        statementService.submitAnswer(user.getUsername(), quizId, answer);
    }

    @pt.ulisboa.tecnico.socialsoftware.tutor.statement.GetMapping("/quizzes/{quizId}/start")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ROLE_STUDENT') and hasPermission(#quizId, 'QUIZ.ACCESS')")
    public void startQuiz(java.security.Principal principal, @pt.ulisboa.tecnico.socialsoftware.tutor.statement.PathVariable
    int quizId) {
        pt.ulisboa.tecnico.socialsoftware.tutor.user.User user = ((pt.ulisboa.tecnico.socialsoftware.tutor.user.User) (((org.springframework.security.core.Authentication) (principal)).getPrincipal()));
        if (user == null) {
            throw new pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.AUTHENTICATION_ERROR);
        }
        statementService.startQuiz(user.getUsername(), quizId);
    }

    @pt.ulisboa.tecnico.socialsoftware.tutor.statement.GetMapping("/quizzes/{quizId}/conclude")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ROLE_STUDENT') and hasPermission(#quizId, 'QUIZ.ACCESS')")
    public java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.CorrectAnswerDto> concludeQuiz(java.security.Principal principal, @pt.ulisboa.tecnico.socialsoftware.tutor.statement.PathVariable
    int quizId) {
        pt.ulisboa.tecnico.socialsoftware.tutor.user.User user = ((pt.ulisboa.tecnico.socialsoftware.tutor.user.User) (((org.springframework.security.core.Authentication) (principal)).getPrincipal()));
        if (user == null) {
            throw new pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.AUTHENTICATION_ERROR);
        }
        return statementService.concludeQuiz(user.getUsername(), quizId);
    }
}