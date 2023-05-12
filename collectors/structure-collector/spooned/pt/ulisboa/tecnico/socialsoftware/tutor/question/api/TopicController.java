package pt.ulisboa.tecnico.socialsoftware.tutor.question.api;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
@pt.ulisboa.tecnico.socialsoftware.tutor.question.api.RestController
public class TopicController {
    private static org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(pt.ulisboa.tecnico.socialsoftware.tutor.question.api.TopicController.class);

    @org.springframework.beans.factory.annotation.Autowired
    private pt.ulisboa.tecnico.socialsoftware.tutor.question.TopicService topicService;

    @pt.ulisboa.tecnico.socialsoftware.tutor.question.api.GetMapping("/courses/{courseId}/topics")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ROLE_TEACHER') and hasPermission(#courseId, 'COURSE.ACCESS')")
    public java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto> getCourseTopics(@pt.ulisboa.tecnico.socialsoftware.tutor.question.api.PathVariable
    int courseId) {
        pt.ulisboa.tecnico.socialsoftware.tutor.question.api.TopicController.logger.debug("courseId {}", courseId);
        return this.topicService.findTopics(courseId);
    }

    @pt.ulisboa.tecnico.socialsoftware.tutor.question.api.PostMapping("/courses/{courseId}/topics")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ROLE_TEACHER') and hasPermission(#courseId, 'COURSE.ACCESS')")
    public pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto createTopic(@pt.ulisboa.tecnico.socialsoftware.tutor.question.api.PathVariable
    int courseId, @javax.validation.Valid
    @pt.ulisboa.tecnico.socialsoftware.tutor.question.api.RequestBody
    pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto topicDto) {
        return this.topicService.createTopic(courseId, topicDto);
    }

    @pt.ulisboa.tecnico.socialsoftware.tutor.question.api.PutMapping("/topics/{topicId}")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ROLE_TEACHER') and hasPermission(#topicId, 'TOPIC.ACCESS')")
    public pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto updateTopic(@pt.ulisboa.tecnico.socialsoftware.tutor.question.api.PathVariable
    java.lang.Integer topicId, @javax.validation.Valid
    @pt.ulisboa.tecnico.socialsoftware.tutor.question.api.RequestBody
    pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto topic) {
        return this.topicService.updateTopic(topicId, topic);
    }

    @pt.ulisboa.tecnico.socialsoftware.tutor.question.api.DeleteMapping("/topics/{topicId}")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ROLE_TEACHER') and hasPermission(#topicId, 'TOPIC.ACCESS')")
    public org.springframework.http.ResponseEntity removeTopic(@pt.ulisboa.tecnico.socialsoftware.tutor.question.api.PathVariable
    java.lang.Integer topicId) {
        topicService.removeTopic(topicId);
        return org.springframework.http.ResponseEntity.ok().build();
    }
}