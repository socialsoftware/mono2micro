package pt.ulisboa.tecnico.socialsoftware.tutor.statistics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
@org.springframework.stereotype.Service
public class StatsService {
    @org.springframework.beans.factory.annotation.Autowired
    private pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository userRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository questionRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository courseExecutionRepository;

    @org.springframework.retry.annotation.Retryable(value = { java.sql.SQLException.class }, backoff = @org.springframework.retry.annotation.Backoff(delay = 5000))
    @org.springframework.transaction.annotation.Transactional(isolation = org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public pt.ulisboa.tecnico.socialsoftware.tutor.statistics.StatsDto getStats(java.lang.String username, int executionId) {
        pt.ulisboa.tecnico.socialsoftware.tutor.user.User user = userRepository.findByUsername(username);
        pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution courseExecution = courseExecutionRepository.findById(executionId).orElseThrow(() -> new pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(COURSE_EXECUTION_NOT_FOUND, executionId));
        pt.ulisboa.tecnico.socialsoftware.tutor.statistics.StatsDto statsDto = new pt.ulisboa.tecnico.socialsoftware.tutor.statistics.StatsDto();
        int totalQuizzes = ((int) (user.getQuizAnswers().stream().filter(quizAnswer -> quizAnswer.canResultsBePublic(courseExecution)).count()));
        int totalAnswers = ((int) (user.getQuizAnswers().stream().filter(quizAnswer -> quizAnswer.canResultsBePublic(courseExecution)).map(pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer::getQuestionAnswers).mapToLong(java.util.Collection::size).sum()));
        int uniqueQuestions = ((int) (user.getQuizAnswers().stream().filter(quizAnswer -> quizAnswer.canResultsBePublic(courseExecution)).map(pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer::getQuestionAnswers).flatMap(java.util.Collection::stream).map(pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswer::getQuizQuestion).map(pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion::getQuestion).map(pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question::getId).distinct().count()));
        int correctAnswers = ((int) (user.getQuizAnswers().stream().filter(quizAnswer -> quizAnswer.canResultsBePublic(courseExecution)).map(pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer::getQuestionAnswers).flatMap(java.util.Collection::stream).map(pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswer::getOption).filter(java.util.Objects::nonNull).filter(pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Option::getCorrect).count()));
        int uniqueCorrectAnswers = ((int) (user.getQuizAnswers().stream().filter(quizAnswer -> quizAnswer.canResultsBePublic(courseExecution)).sorted(java.util.Comparator.comparing(pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer::getAnswerDate).reversed()).map(pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer::getQuestionAnswers).flatMap(java.util.Collection::stream).collect(java.util.stream.Collectors.collectingAndThen(java.util.stream.Collectors.toCollection(() -> new java.util.TreeSet<>(java.util.Comparator.comparingInt(questionAnswer -> questionAnswer.getQuizQuestion().getQuestion().getId()))), java.util.ArrayList::new)).stream().map(pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswer::getOption).filter(java.util.Objects::nonNull).filter(pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Option::getCorrect).count()));
        pt.ulisboa.tecnico.socialsoftware.tutor.course.Course course = courseExecutionRepository.findById(executionId).orElseThrow(() -> new pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(COURSE_EXECUTION_NOT_FOUND, executionId)).getCourse();
        int totalAvailableQuestions = questionRepository.getAvailableQuestionsSize(course.getId());
        statsDto.setTotalQuizzes(totalQuizzes);
        statsDto.setTotalAnswers(totalAnswers);
        statsDto.setTotalUniqueQuestions(uniqueQuestions);
        statsDto.setTotalAvailableQuestions(totalAvailableQuestions);
        if (totalAnswers != 0) {
            statsDto.setCorrectAnswers((((float) (correctAnswers)) * 100) / totalAnswers);
            statsDto.setImprovedCorrectAnswers((((float) (uniqueCorrectAnswers)) * 100) / uniqueQuestions);
        }
        return statsDto;
    }
}