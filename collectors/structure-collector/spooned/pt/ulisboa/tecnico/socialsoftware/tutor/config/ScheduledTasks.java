package pt.ulisboa.tecnico.socialsoftware.tutor.config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
@org.springframework.stereotype.Component
public class ScheduledTasks {
    @org.springframework.beans.factory.annotation.Autowired
    private pt.ulisboa.tecnico.socialsoftware.tutor.impexp.ImpExpService impExpService;

    @org.springframework.beans.factory.annotation.Autowired
    private pt.ulisboa.tecnico.socialsoftware.tutor.quiz.QuizService quizService;

    @org.springframework.beans.factory.annotation.Autowired
    private pt.ulisboa.tecnico.socialsoftware.tutor.statement.StatementService statementService;

    @org.springframework.beans.factory.annotation.Autowired
    private pt.ulisboa.tecnico.socialsoftware.tutor.question.TopicService topicService;

    @org.springframework.beans.factory.annotation.Autowired
    private pt.ulisboa.tecnico.socialsoftware.tutor.question.AssessmentService assessmentService;

    @org.springframework.scheduling.annotation.Scheduled(cron = "0 0 1,13 * * *")
    public void exportAll() {
        impExpService.exportAll();
    }

    @org.springframework.scheduling.annotation.Scheduled(cron = "0 0/15 * * * *")
    public void completeOpenQuizAnswers() {
        statementService.completeOpenQuizAnswers();
    }

    @org.springframework.scheduling.annotation.Scheduled(cron = "0 0 1 * * *")
    public void resetDemoInfo() {
        quizService.resetDemoQuizzes();
        topicService.resetDemoTopics();
        assessmentService.resetDemoAssessments();
    }
}