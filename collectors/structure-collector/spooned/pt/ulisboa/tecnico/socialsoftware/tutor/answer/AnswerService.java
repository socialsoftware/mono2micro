package pt.ulisboa.tecnico.socialsoftware.tutor.answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
@org.springframework.stereotype.Service
public class AnswerService {
    @org.springframework.beans.factory.annotation.Autowired
    private pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository userRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository questionRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private pt.ulisboa.tecnico.socialsoftware.tutor.answer.repository.QuestionAnswerRepository questionAnswerRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private pt.ulisboa.tecnico.socialsoftware.tutor.quiz.repository.QuizRepository quizRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private pt.ulisboa.tecnico.socialsoftware.tutor.answer.repository.QuizAnswerRepository quizAnswerRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.OptionRepository optionRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.AnswersXmlImport xmlImporter;

    @org.springframework.retry.annotation.Retryable(value = { java.sql.SQLException.class }, backoff = @org.springframework.retry.annotation.Backoff(delay = 5000))
    @org.springframework.transaction.annotation.Transactional(isolation = org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.QuizAnswerDto createQuizAnswer(java.lang.Integer userId, java.lang.Integer quizId) {
        pt.ulisboa.tecnico.socialsoftware.tutor.user.User user = userRepository.findById(userId).orElseThrow(() -> new <USER_NOT_FOUND>pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(userId));
        pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz quiz = quizRepository.findById(quizId).orElseThrow(() -> new <QUIZ_NOT_FOUND>pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(quizId));
        pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer quizAnswer = new pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer(user, quiz);
        quizAnswerRepository.save(quizAnswer);
        return new pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.QuizAnswerDto(quizAnswer);
    }

    @org.springframework.retry.annotation.Retryable(value = { java.sql.SQLException.class }, backoff = @org.springframework.retry.annotation.Backoff(delay = 5000))
    @org.springframework.transaction.annotation.Transactional(isolation = org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.CorrectAnswerDto> concludeQuiz(pt.ulisboa.tecnico.socialsoftware.tutor.user.User user, java.lang.Integer quizId) {
        pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer quizAnswer = user.getQuizAnswers().stream().filter(qa -> qa.getQuiz().getId().equals(quizId)).findFirst().orElseThrow(() -> new pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.QUIZ_NOT_FOUND, quizId));
        if ((quizAnswer.getQuiz().getAvailableDate() != null) && quizAnswer.getQuiz().getAvailableDate().isAfter(java.time.LocalDateTime.now())) {
            throw new pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.QUIZ_NOT_YET_AVAILABLE);
        }
        if (!quizAnswer.isCompleted()) {
            quizAnswer.setAnswerDate(java.time.LocalDateTime.now());
            quizAnswer.setCompleted(true);
        }
        // In class quiz When student submits before conclusionDate
        if (((quizAnswer.getQuiz().getConclusionDate() != null) && quizAnswer.getQuiz().getType().equals(pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz.QuizType.IN_CLASS)) && java.time.LocalDateTime.now().isBefore(quizAnswer.getQuiz().getConclusionDate())) {
            return new java.util.ArrayList<>();
        }
        return quizAnswer.getQuestionAnswers().stream().sorted(java.util.Comparator.comparing(pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswer::getSequence)).map(pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.CorrectAnswerDto::new).collect(java.util.stream.Collectors.toList());
    }

    @org.springframework.retry.annotation.Retryable(value = { java.sql.SQLException.class }, backoff = @org.springframework.retry.annotation.Backoff(delay = 5000))
    @org.springframework.transaction.annotation.Transactional(isolation = org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public void submitAnswer(pt.ulisboa.tecnico.socialsoftware.tutor.user.User user, java.lang.Integer quizId, pt.ulisboa.tecnico.socialsoftware.tutor.statement.dto.StatementAnswerDto answer) {
        pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer quizAnswer = user.getQuizAnswers().stream().filter(qa -> qa.getQuiz().getId().equals(quizId)).findFirst().orElseThrow(() -> new pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.QUIZ_NOT_FOUND, quizId));
        pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswer questionAnswer = quizAnswer.getQuestionAnswers().stream().filter(qa -> qa.getSequence().equals(answer.getSequence())).findFirst().orElseThrow(() -> new pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.QUESTION_ANSWER_NOT_FOUND, answer.getSequence()));
        if (isNotAssignedStudent(user, quizAnswer)) {
            throw new pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.QUIZ_USER_MISMATCH, java.lang.String.valueOf(quizAnswer.getQuiz().getId()), user.getUsername());
        }
        if ((quizAnswer.getQuiz().getConclusionDate() != null) && quizAnswer.getQuiz().getConclusionDate().isBefore(java.time.LocalDateTime.now())) {
            throw new pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.QUIZ_NO_LONGER_AVAILABLE);
        }
        if ((quizAnswer.getQuiz().getAvailableDate() != null) && quizAnswer.getQuiz().getAvailableDate().isAfter(java.time.LocalDateTime.now())) {
            throw new pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.QUIZ_NOT_YET_AVAILABLE);
        }
        if (!quizAnswer.isCompleted()) {
            pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Option option;
            if (answer.getOptionId() != null) {
                option = optionRepository.findById(answer.getOptionId()).orElseThrow(() -> new <OPTION_NOT_FOUND>pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(answer.getOptionId()));
                if (isNotQuestionOption(questionAnswer.getQuizQuestion(), option)) {
                    throw new pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.QUESTION_OPTION_MISMATCH, questionAnswer.getQuizQuestion().getQuestion().getId(), option.getId());
                }
                if (questionAnswer.getOption() != null) {
                    questionAnswer.getOption().getQuestionAnswers().remove(questionAnswer);
                }
                questionAnswer.setOption(option);
                option.addQuestionAnswer(questionAnswer);
                questionAnswer.setTimeTaken(answer.getTimeTaken());
                quizAnswer.setAnswerDate(java.time.LocalDateTime.now());
            }
        }
    }

    private boolean isNotQuestionOption(pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion quizQuestion, pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Option option) {
        return quizQuestion.getQuestion().getOptions().stream().map(pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Option::getId).noneMatch(value -> value.equals(option.getId()));
    }

    private boolean isNotAssignedStudent(pt.ulisboa.tecnico.socialsoftware.tutor.user.User user, pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer quizAnswer) {
        return !user.getId().equals(quizAnswer.getUser().getId());
    }

    @org.springframework.retry.annotation.Retryable(value = { java.sql.SQLException.class }, backoff = @org.springframework.retry.annotation.Backoff(delay = 5000))
    @org.springframework.transaction.annotation.Transactional(isolation = org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public java.lang.String exportAnswers() {
        pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.AnswersXmlExport xmlExport = new pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.AnswersXmlExport();
        return xmlExport.export(quizAnswerRepository.findAll());
    }

    @org.springframework.retry.annotation.Retryable(value = { java.sql.SQLException.class }, backoff = @org.springframework.retry.annotation.Backoff(delay = 5000))
    @org.springframework.transaction.annotation.Transactional(isolation = org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public void importAnswers(java.lang.String answersXml) {
        xmlImporter.importAnswers(answersXml, this, questionRepository, quizRepository, quizAnswerRepository, userRepository);
    }

    @org.springframework.retry.annotation.Retryable(value = { java.sql.SQLException.class }, backoff = @org.springframework.retry.annotation.Backoff(delay = 5000))
    @org.springframework.transaction.annotation.Transactional(isolation = org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public void deleteQuizAnswer(pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer quizAnswer) {
        for (pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswer questionAnswer : quizAnswer.getQuestionAnswers()) {
            questionAnswer.remove();
            questionAnswerRepository.delete(questionAnswer);
        }
        quizAnswer.remove();
        quizAnswerRepository.delete(quizAnswer);
    }
}