package pt.ulisboa.tecnico.socialsoftware.tutor.config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
@org.springframework.stereotype.Component
public class TutorPermissionEvaluator implements org.springframework.security.access.PermissionEvaluator {
    @org.springframework.beans.factory.annotation.Autowired
    private pt.ulisboa.tecnico.socialsoftware.tutor.administration.AdministrationService administrationService;

    @org.springframework.beans.factory.annotation.Autowired
    private pt.ulisboa.tecnico.socialsoftware.tutor.user.UserService userService;

    @org.springframework.beans.factory.annotation.Autowired
    private pt.ulisboa.tecnico.socialsoftware.tutor.question.QuestionService questionService;

    @org.springframework.beans.factory.annotation.Autowired
    private pt.ulisboa.tecnico.socialsoftware.tutor.question.TopicService topicService;

    @org.springframework.beans.factory.annotation.Autowired
    private pt.ulisboa.tecnico.socialsoftware.tutor.question.AssessmentService assessmentService;

    @org.springframework.beans.factory.annotation.Autowired
    private pt.ulisboa.tecnico.socialsoftware.tutor.quiz.QuizService quizService;

    @java.lang.Override
    public boolean hasPermission(org.springframework.security.core.Authentication authentication, java.lang.Object targetDomainObject, java.lang.Object permission) {
        java.lang.String username = ((pt.ulisboa.tecnico.socialsoftware.tutor.user.User) (authentication.getPrincipal())).getUsername();
        if (targetDomainObject instanceof pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseDto) {
            pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseDto courseDto = ((pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseDto) (targetDomainObject));
            java.lang.String permissionValue = ((java.lang.String) (permission));
            switch (permissionValue) {
                case "EXECUTION.CREATE" :
                    return userService.getEnrolledCoursesAcronyms(username).contains(courseDto.getAcronym() + courseDto.getAcademicTerm());
                case "DEMO.ACCESS" :
                    return courseDto.getName().equals("Demo Course");
                default :
                    return false;
            }
        }
        if (targetDomainObject instanceof java.lang.Integer) {
            int id = ((int) (targetDomainObject));
            java.lang.String permissionValue = ((java.lang.String) (permission));
            switch (permissionValue) {
                case "DEMO.ACCESS" :
                    pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseDto courseDto = administrationService.getCourseExecutionById(id);
                    return courseDto.getName().equals("Demo Course");
                case "COURSE.ACCESS" :
                    return userHasAnExecutionOfTheCourse(username, id);
                case "EXECUTION.ACCESS" :
                    return userHasThisExecution(username, id);
                case "QUESTION.ACCESS" :
                    return userHasAnExecutionOfTheCourse(username, questionService.findQuestionCourse(id).getCourseId());
                case "TOPIC.ACCESS" :
                    return userHasAnExecutionOfTheCourse(username, topicService.findTopicCourse(id).getCourseId());
                case "ASSESSMENT.ACCESS" :
                    return userHasThisExecution(username, assessmentService.findAssessmentCourseExecution(id).getCourseExecutionId());
                case "QUIZ.ACCESS" :
                    return userHasThisExecution(username, quizService.findQuizCourseExecution(id).getCourseExecutionId());
                default :
                    return false;
            }
        }
        return false;
    }

    private boolean userHasAnExecutionOfTheCourse(java.lang.String username, int id) {
        return userService.getCourseExecutions(username).stream().anyMatch(course -> course.getCourseId() == id);
    }

    private boolean userHasThisExecution(java.lang.String username, int id) {
        return userService.getCourseExecutions(username).stream().anyMatch(course -> course.getCourseExecutionId() == id);
    }

    @java.lang.Override
    public boolean hasPermission(org.springframework.security.core.Authentication authentication, java.io.Serializable serializable, java.lang.String s, java.lang.Object o) {
        return false;
    }
}