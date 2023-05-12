package pt.ulisboa.tecnico.socialsoftware.tutor.statistics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
@org.springframework.web.bind.annotation.RestController
public class StatsController {
    @org.springframework.beans.factory.annotation.Autowired
    private pt.ulisboa.tecnico.socialsoftware.tutor.statistics.StatsService statsService;

    @org.springframework.web.bind.annotation.GetMapping("/executions/{executionId}/stats")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ROLE_STUDENT') and hasPermission(#executionId, 'EXECUTION.ACCESS')")
    public pt.ulisboa.tecnico.socialsoftware.tutor.statistics.StatsDto getStats(java.security.Principal principal, @org.springframework.web.bind.annotation.PathVariable
    int executionId) {
        pt.ulisboa.tecnico.socialsoftware.tutor.user.User user = ((pt.ulisboa.tecnico.socialsoftware.tutor.user.User) (((org.springframework.security.core.Authentication) (principal)).getPrincipal()));
        if (user == null) {
            throw new pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.AUTHENTICATION_ERROR);
        }
        return statsService.getStats(user.getUsername(), executionId);
    }
}