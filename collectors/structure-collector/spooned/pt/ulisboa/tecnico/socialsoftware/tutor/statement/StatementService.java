package pt.ulisboa.tecnico.socialsoftware.tutor.statement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
@org.springframework.stereotype.Service
public class StatementService {
    @org.springframework.beans.factory.annotation.Autowired
    private pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository userRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository courseExecutionRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private pt.ulisboa.tecnico.socialsoftware.tutor.quiz.repository.QuizRepository quizRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private pt.ulisboa.tecnico.socialsoftware.tutor.answer.repository.QuizAnswerRepository quizAnswerRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository questionRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.AssessmentRepository assessmentRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private pt.ulisboa.tecnico.socialsoftware.tutor.quiz.QuizService quizService;

    @org.springframework.beans.factory.annotation.Autowired
    private pt.ulisboa.tecnico.socialsoftware.tutor.answer.AnswerService answerService;

    @org.springframework.retry.annotation.Retryable(value = { java.sql.SQLException.class }, backoff = @org.springframework.retry.annotation.Backoff(delay = 5000))
    @org.springframework.transaction.annotation.Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
    public pt.ulisboa.tecnico.socialsoftware.tutor.statement.dto.StatementQuizDto generateStudentQuiz(java.lang.String username, int executionId, pt.ulisboa.tecnico.socialsoftware.tutor.statement.dto.StatementCreationDto quizDetails) {
        pt.ulisboa.tecnico.socialsoftware.tutor.user.User user = userRepository.findByUsername(username);
        pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz quiz = new pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz();
        quiz.setKey(quizService.getMaxQuizKey() + 1);
        quiz.setType(pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz.QuizType.GENERATED);
        quiz.setCreationDate(java.time.LocalDateTime.now());
        pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution courseExecution = courseExecutionRepository.findById(executionId).orElseThrow(() -> new <COURSE_EXECUTION_NOT_FOUND>pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(executionId));
        java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question> availableQuestions = questionRepository.findAvailableQuestions(courseExecution.getCourse().getId());
        if (quizDetails.getAssessment() != null) {
            availableQuestions = filterByAssessment(availableQuestions, quizDetails);
        }
        // TODO else use default assessment
        if (availableQuestions.size() < quizDetails.getNumberOfQuestions()) {
            throw new pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.NOT_ENOUGH_QUESTIONS);
        }
        availableQuestions = user.filterQuestionsByStudentModel(quizDetails.getNumberOfQuestions(), availableQuestions);
        quiz.generate(availableQuestions);
        pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer quizAnswer = new pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer(user, quiz);
        quiz.setCourseExecution(courseExecution);
        courseExecution.addQuiz(quiz);
        quizRepository.save(quiz);
        quizAnswerRepository.save(quizAnswer);
        return new pt.ulisboa.tecnico.socialsoftware.tutor.statement.dto.StatementQuizDto(quizAnswer);
    }

    @org.springframework.retry.annotation.Retryable(value = { java.sql.SQLException.class }, backoff = @org.springframework.retry.annotation.Backoff(delay = 5000))
    @org.springframework.transaction.annotation.Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
    public pt.ulisboa.tecnico.socialsoftware.tutor.statement.dto.StatementQuizDto getQuizByQRCode(java.lang.String username, int quizId) {
        pt.ulisboa.tecnico.socialsoftware.tutor.user.User user = userRepository.findByUsername(username);
        pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz quiz = quizRepository.findById(quizId).orElseThrow(() -> new <QUIZ_NOT_FOUND>pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(quizId));
        if (!user.getCourseExecutions().contains(quiz.getCourseExecution())) {
            throw new pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.USER_NOT_ENROLLED, username);
        }
        if ((quiz.getConclusionDate() != null) && java.time.LocalDateTime.now().isAfter(quiz.getConclusionDate())) {
            throw new pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.QUIZ_NO_LONGER_AVAILABLE);
        }
        pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer quizAnswer = quizAnswerRepository.findQuizAnswer(quiz.getId(), user.getId()).orElseGet(() -> {
            pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer qa = new pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer(user, quiz);
            quizAnswerRepository.save(qa);
            return qa;
        });
        if (quizAnswer.isCompleted()) {
            throw new pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.QUIZ_ALREADY_COMPLETED);
        }
        if (quizAnswer.getQuiz().isOneWay() && (quizAnswer.getAnswerDate() != null)) {
            throw new pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.QUIZ_ALREADY_COMPLETED);
        }
        if ((quiz.getAvailableDate() == null) || java.time.LocalDateTime.now().isAfter(quiz.getAvailableDate())) {
            return new pt.ulisboa.tecnico.socialsoftware.tutor.statement.dto.StatementQuizDto(quizAnswer);
            // Send timer
        } else {
            pt.ulisboa.tecnico.socialsoftware.tutor.statement.dto.StatementQuizDto quizDto = new pt.ulisboa.tecnico.socialsoftware.tutor.statement.dto.StatementQuizDto();
            quizDto.setSecondsToAvailability(java.time.temporal.ChronoUnit.SECONDS.between(java.time.LocalDateTime.now(), quiz.getAvailableDate()));
            return quizDto;
        }
    }

    @org.springframework.retry.annotation.Retryable(value = { java.sql.SQLException.class }, backoff = @org.springframework.retry.annotation.Backoff(delay = 5000))
    @org.springframework.transaction.annotation.Transactional(isolation = org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.statement.dto.StatementQuizDto> getAvailableQuizzes(java.lang.String username, int executionId) {
        pt.ulisboa.tecnico.socialsoftware.tutor.user.User user = userRepository.findByUsername(username);
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.util.Set<java.lang.Integer> studentQuizIds = user.getQuizAnswers().stream().filter(quizAnswer -> quizAnswer.getQuiz().getCourseExecution().getId() == executionId).map(pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer::getQuiz).map(pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz::getId).collect(java.util.stream.Collectors.toSet());
        // create QuizAnswer for quizzes
        quizRepository.findQuizzes(executionId).stream().filter(quiz -> !quiz.isQrCodeOnly()).filter(quiz -> !quiz.getType().equals(pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz.QuizType.GENERATED)).filter(quiz -> (quiz.getAvailableDate() == null) || quiz.getAvailableDate().isBefore(now)).filter(quiz -> !studentQuizIds.contains(quiz.getId())).forEach(quiz -> {
            if ((quiz.getConclusionDate() == null) || quiz.getConclusionDate().isAfter(now)) {
                pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer quizAnswer = new pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer(user, quiz);
                quizAnswerRepository.save(quizAnswer);
            }
        });
        return user.getQuizAnswers().stream().filter(quizAnswer -> !quizAnswer.isCompleted()).filter(quizAnswer -> (!quizAnswer.getQuiz().isOneWay()) || (quizAnswer.getCreationDate() == null)).filter(quizAnswer -> quizAnswer.getQuiz().getCourseExecution().getId() == executionId).filter(quizAnswer -> (quizAnswer.getQuiz().getConclusionDate() == null) || java.time.LocalDateTime.now().isBefore(quizAnswer.getQuiz().getConclusionDate())).filter(quizAnswer -> quizAnswer.getQuiz().getAvailableDate().isBefore(now)).map(pt.ulisboa.tecnico.socialsoftware.tutor.statement.dto.StatementQuizDto::new).sorted(java.util.Comparator.comparing(pt.ulisboa.tecnico.socialsoftware.tutor.statement.dto.StatementQuizDto::getAvailableDate, java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder()))).collect(java.util.stream.Collectors.toList());
    }

    @org.springframework.retry.annotation.Retryable(value = { java.sql.SQLException.class }, backoff = @org.springframework.retry.annotation.Backoff(delay = 5000))
    @org.springframework.transaction.annotation.Transactional(isolation = org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.statement.dto.SolvedQuizDto> getSolvedQuizzes(java.lang.String username, int executionId) {
        pt.ulisboa.tecnico.socialsoftware.tutor.user.User user = userRepository.findByUsername(username);
        pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution courseExecution = courseExecutionRepository.findById(executionId).orElseThrow(() -> new <COURSE_EXECUTION_NOT_FOUND>pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(executionId));
        return user.getQuizAnswers().stream().filter(quizAnswer -> quizAnswer.canResultsBePublic(courseExecution)).map(pt.ulisboa.tecnico.socialsoftware.tutor.statement.dto.SolvedQuizDto::new).sorted(java.util.Comparator.comparing(pt.ulisboa.tecnico.socialsoftware.tutor.statement.dto.SolvedQuizDto::getAnswerDate)).collect(java.util.stream.Collectors.toList());
    }

    @org.springframework.retry.annotation.Retryable(value = { java.sql.SQLException.class }, backoff = @org.springframework.retry.annotation.Backoff(delay = 5000))
    @org.springframework.transaction.annotation.Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
    public java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.CorrectAnswerDto> concludeQuiz(java.lang.String username, java.lang.Integer quizId) {
        pt.ulisboa.tecnico.socialsoftware.tutor.user.User user = userRepository.findByUsername(username);
        return answerService.concludeQuiz(user, quizId);
    }

    @org.springframework.retry.annotation.Retryable(value = { java.sql.SQLException.class }, backoff = @org.springframework.retry.annotation.Backoff(delay = 5000))
    @org.springframework.transaction.annotation.Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
    public void submitAnswer(java.lang.String username, java.lang.Integer quizId, pt.ulisboa.tecnico.socialsoftware.tutor.statement.dto.StatementAnswerDto answer) {
        pt.ulisboa.tecnico.socialsoftware.tutor.user.User user = userRepository.findByUsername(username);
        answerService.submitAnswer(user, quizId, answer);
    }

    @org.springframework.retry.annotation.Retryable(value = { java.sql.SQLException.class }, backoff = @org.springframework.retry.annotation.Backoff(delay = 5000))
    @org.springframework.transaction.annotation.Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
    public void completeOpenQuizAnswers() {
        java.util.Set<pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer> quizAnswersToClose = quizAnswerRepository.findQuizAnswersToClose(java.time.LocalDateTime.now());
        quizAnswersToClose.forEach(quizAnswer -> {
            if (!quizAnswer.isCompleted()) {
                quizAnswer.setAnswerDate(quizAnswer.getQuiz().getConclusionDate());
                quizAnswer.setCompleted(true);
            }
            quizAnswer.calculateStatistics();
        });
    }

    @org.springframework.retry.annotation.Retryable(value = { java.sql.SQLException.class }, backoff = @org.springframework.retry.annotation.Backoff(delay = 5000))
    @org.springframework.transaction.annotation.Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
    public void startQuiz(java.lang.String username, int quizId) {
        pt.ulisboa.tecnico.socialsoftware.tutor.user.User user = userRepository.findByUsername(username);
        pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz quiz = quizRepository.findById(quizId).orElseThrow(() -> new <QUIZ_NOT_FOUND>pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(quizId));
        if (!user.getCourseExecutions().contains(quiz.getCourseExecution())) {
            throw new pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.USER_NOT_ENROLLED, username);
        }
        if ((quiz.getConclusionDate() != null) && java.time.LocalDateTime.now().isAfter(quiz.getConclusionDate())) {
            throw new pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.QUIZ_NO_LONGER_AVAILABLE);
        }
        pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer quizAnswer = quizAnswerRepository.findQuizAnswer(quizId, user.getId()).orElseThrow(() -> new pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.QUIZ_ANSWER_NOT_FOUND, quizId));
        if (quizAnswer.isCompleted()) {
            throw new pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.QUIZ_ALREADY_COMPLETED);
        } else if (quizAnswer.getCreationDate() == null) {
            quizAnswer.setCreationDate(java.time.LocalDateTime.now());
        } else if (quiz.isOneWay()) {
            throw new pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.QUIZ_ALREADY_STARTED);
        }
    }

    public java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question> filterByAssessment(java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question> availableQuestions, pt.ulisboa.tecnico.socialsoftware.tutor.statement.dto.StatementCreationDto quizDetails) {
        pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Assessment assessment = assessmentRepository.findById(java.lang.Integer.valueOf(quizDetails.getAssessment())).orElseThrow(() -> new <ASSESSMENT_NOT_FOUND>pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(quizDetails.getAssessment()));
        return availableQuestions.stream().filter(question -> question.belongsToAssessment(assessment)).collect(java.util.stream.Collectors.toList());
    }
}