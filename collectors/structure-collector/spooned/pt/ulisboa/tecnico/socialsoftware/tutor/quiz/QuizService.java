package pt.ulisboa.tecnico.socialsoftware.tutor.quiz;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
@org.springframework.stereotype.Service
public class QuizService {
    @org.springframework.beans.factory.annotation.Autowired
    private pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository courseExecutionRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private pt.ulisboa.tecnico.socialsoftware.tutor.quiz.repository.QuizRepository quizRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository questionRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private pt.ulisboa.tecnico.socialsoftware.tutor.quiz.repository.QuizQuestionRepository quizQuestionRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private pt.ulisboa.tecnico.socialsoftware.tutor.answer.AnswerService answerService;

    @org.springframework.beans.factory.annotation.Autowired
    private pt.ulisboa.tecnico.socialsoftware.tutor.question.QuestionService questionService;

    @org.springframework.transaction.annotation.Transactional(isolation = org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseDto findQuizCourseExecution(int quizId) {
        return this.quizRepository.findById(quizId).map(pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz::getCourseExecution).map(pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseDto::new).orElseThrow(() -> new <QUIZ_NOT_FOUND>pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(quizId));
    }

    @org.springframework.retry.annotation.Retryable(value = { java.sql.SQLException.class }, backoff = @org.springframework.retry.annotation.Backoff(delay = 5000))
    @org.springframework.transaction.annotation.Transactional(isolation = org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public pt.ulisboa.tecnico.socialsoftware.tutor.quiz.dto.QuizDto findById(java.lang.Integer quizId) {
        return this.quizRepository.findById(quizId).map(quiz -> new pt.ulisboa.tecnico.socialsoftware.tutor.quiz.dto.QuizDto(quiz, true)).orElseThrow(() -> new <QUIZ_NOT_FOUND>pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(quizId));
    }

    @org.springframework.retry.annotation.Retryable(value = { java.sql.SQLException.class }, backoff = @org.springframework.retry.annotation.Backoff(delay = 5000))
    @org.springframework.transaction.annotation.Transactional(isolation = org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.quiz.dto.QuizDto> findNonGeneratedQuizzes(int executionId) {
        java.util.Comparator<pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz> comparator = java.util.Comparator.comparing(pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz::getAvailableDate, java.util.Comparator.nullsFirst(java.util.Comparator.reverseOrder())).thenComparing(pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz::getSeries, java.util.Comparator.nullsFirst(java.util.Comparator.reverseOrder())).thenComparing(pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz::getVersion, java.util.Comparator.nullsFirst(java.util.Comparator.reverseOrder()));
        return quizRepository.findQuizzes(executionId).stream().filter(quiz -> !quiz.getType().equals(pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz.QuizType.GENERATED)).sorted(comparator).map(quiz -> new pt.ulisboa.tecnico.socialsoftware.tutor.quiz.dto.QuizDto(quiz, false)).collect(java.util.stream.Collectors.toList());
    }

    public java.lang.Integer getMaxQuizKey() {
        java.lang.Integer maxQuizKey = quizRepository.getMaxQuizKey();
        return maxQuizKey != null ? maxQuizKey : 0;
    }

    @org.springframework.retry.annotation.Retryable(value = { java.sql.SQLException.class }, backoff = @org.springframework.retry.annotation.Backoff(delay = 5000))
    @org.springframework.transaction.annotation.Transactional(isolation = org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public pt.ulisboa.tecnico.socialsoftware.tutor.quiz.dto.QuizDto createQuiz(int executionId, pt.ulisboa.tecnico.socialsoftware.tutor.quiz.dto.QuizDto quizDto) {
        pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution courseExecution = courseExecutionRepository.findById(executionId).orElseThrow(() -> new <COURSE_EXECUTION_NOT_FOUND>pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(executionId));
        if (quizDto.getKey() == null) {
            quizDto.setKey(getMaxQuizKey() + 1);
        }
        pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz quiz = new pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz(quizDto);
        quiz.setCourseExecution(courseExecution);
        if (quizDto.getQuestions() != null) {
            for (pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto questionDto : quizDto.getQuestions()) {
                pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question question = questionRepository.findById(questionDto.getId()).orElseThrow(() -> new <QUESTION_NOT_FOUND>pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(questionDto.getId()));
                new pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion(quiz, question, quiz.getQuizQuestions().size());
            }
        }
        if (quizDto.getCreationDate() == null) {
            quiz.setCreationDate(java.time.LocalDateTime.now());
        } else {
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            quiz.setCreationDate(java.time.LocalDateTime.parse(quizDto.getCreationDate(), formatter));
        }
        quizRepository.save(quiz);
        return new pt.ulisboa.tecnico.socialsoftware.tutor.quiz.dto.QuizDto(quiz, true);
    }

    @org.springframework.retry.annotation.Retryable(value = { java.sql.SQLException.class }, backoff = @org.springframework.retry.annotation.Backoff(delay = 5000))
    @org.springframework.transaction.annotation.Transactional(isolation = org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public pt.ulisboa.tecnico.socialsoftware.tutor.quiz.dto.QuizDto updateQuiz(java.lang.Integer quizId, pt.ulisboa.tecnico.socialsoftware.tutor.quiz.dto.QuizDto quizDto) {
        pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz quiz = quizRepository.findById(quizId).orElseThrow(() -> new <QUIZ_NOT_FOUND>pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(quizId));
        quiz.checkCanChange();
        quiz.setTitle(quizDto.getTitle());
        quiz.setAvailableDate(quizDto.getAvailableDateDate());
        quiz.setConclusionDate(quizDto.getConclusionDateDate());
        quiz.setScramble(quizDto.isScramble());
        quiz.setQrCodeOnly(quizDto.isQrCodeOnly());
        quiz.setOneWay(quizDto.isOneWay());
        quiz.setType(quizDto.getType());
        java.util.Set<pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion> quizQuestions = new java.util.HashSet<>(quiz.getQuizQuestions());
        quizQuestions.forEach(pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion::remove);
        quizQuestions.forEach(quizQuestion -> quizQuestionRepository.delete(quizQuestion));
        if (quizDto.getQuestions() != null) {
            for (pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto questionDto : quizDto.getQuestions()) {
                pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question question = questionRepository.findById(questionDto.getId()).orElseThrow(() -> new <QUESTION_NOT_FOUND>pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(questionDto.getId()));
                pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion quizQuestion = new pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion(quiz, question, quiz.getQuizQuestions().size());
                quizQuestionRepository.save(quizQuestion);
            }
        }
        return new pt.ulisboa.tecnico.socialsoftware.tutor.quiz.dto.QuizDto(quiz, true);
    }

    @org.springframework.retry.annotation.Retryable(value = { java.sql.SQLException.class }, backoff = @org.springframework.retry.annotation.Backoff(delay = 5000))
    @org.springframework.transaction.annotation.Transactional(isolation = org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public pt.ulisboa.tecnico.socialsoftware.tutor.quiz.dto.QuizQuestionDto addQuestionToQuiz(int questionId, int quizId) {
        pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz quiz = quizRepository.findById(quizId).orElseThrow(() -> new <QUIZ_NOT_FOUND>pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(quizId));
        pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question question = questionRepository.findById(questionId).orElseThrow(() -> new <QUESTION_NOT_FOUND>pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(questionId));
        pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion quizQuestion = new pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion(quiz, question, quiz.getQuizQuestions().size());
        quizQuestionRepository.save(quizQuestion);
        return new pt.ulisboa.tecnico.socialsoftware.tutor.quiz.dto.QuizQuestionDto(quizQuestion);
    }

    @org.springframework.retry.annotation.Retryable(value = { java.sql.SQLException.class }, backoff = @org.springframework.retry.annotation.Backoff(delay = 5000))
    @org.springframework.transaction.annotation.Transactional(isolation = org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public void removeQuiz(java.lang.Integer quizId) {
        pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz quiz = quizRepository.findById(quizId).orElseThrow(() -> new <QUIZ_NOT_FOUND>pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(quizId));
        quiz.remove();
        java.util.Set<pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion> quizQuestions = new java.util.HashSet<>(quiz.getQuizQuestions());
        quizQuestions.forEach(pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion::remove);
        quizQuestions.forEach(quizQuestion -> quizQuestionRepository.delete(quizQuestion));
        quizRepository.delete(quiz);
    }

    @org.springframework.retry.annotation.Retryable(value = { java.sql.SQLException.class }, backoff = @org.springframework.retry.annotation.Backoff(delay = 5000))
    @org.springframework.transaction.annotation.Transactional(isolation = org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.QuizAnswersDto getQuizAnswers(java.lang.Integer quizId) {
        pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz quiz = quizRepository.findById(quizId).orElseThrow(() -> new <QUIZ_NOT_FOUND>pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(quizId));
        pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.QuizAnswersDto quizAnswersDto = new pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.QuizAnswersDto();
        quizAnswersDto.setCorrectSequence(quiz.getQuizQuestions().stream().sorted(java.util.Comparator.comparing(pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion::getSequence)).map(quizQuestion -> quizQuestion.getQuestion().getOptions().stream().filter(pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Option::getCorrect).findFirst().orElseThrow(() -> new pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.NO_CORRECT_OPTION)).getSequence()).collect(java.util.stream.Collectors.toList()));
        quizAnswersDto.setQuizAnswers(quiz.getQuizAnswers().stream().map(pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.QuizAnswerDto::new).collect(java.util.stream.Collectors.toList()));
        if ((quiz.getConclusionDate() != null) && quiz.getConclusionDate().isAfter(java.time.LocalDateTime.now())) {
            quizAnswersDto.setSecondsToSubmission(java.time.temporal.ChronoUnit.SECONDS.between(java.time.LocalDateTime.now(), quiz.getConclusionDate()));
        }
        return quizAnswersDto;
    }

    @org.springframework.retry.annotation.Retryable(value = { java.sql.SQLException.class }, backoff = @org.springframework.retry.annotation.Backoff(delay = 5000))
    @org.springframework.transaction.annotation.Transactional(isolation = org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public java.lang.String exportQuizzesToXml() {
        pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.QuizzesXmlExport xmlExport = new pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.QuizzesXmlExport();
        return xmlExport.export(quizRepository.findAll());
    }

    @org.springframework.retry.annotation.Retryable(value = { java.sql.SQLException.class }, backoff = @org.springframework.retry.annotation.Backoff(delay = 5000))
    @org.springframework.transaction.annotation.Transactional(isolation = org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public void importQuizzesFromXml(java.lang.String quizzesXml) {
        pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.QuizzesXmlImport xmlImport = new pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.QuizzesXmlImport();
        xmlImport.importQuizzes(quizzesXml, this, questionRepository, quizQuestionRepository, courseExecutionRepository);
    }

    @org.springframework.retry.annotation.Retryable(value = { java.sql.SQLException.class }, backoff = @org.springframework.retry.annotation.Backoff(delay = 5000))
    @org.springframework.transaction.annotation.Transactional(isolation = org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public java.lang.String exportQuizzesToLatex(int quizId) {
        pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz quiz = quizRepository.findById(quizId).orElseThrow(() -> new <QUIZ_NOT_FOUND>pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(quizId));
        pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.LatexQuizExportVisitor latexExport = new pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.LatexQuizExportVisitor();
        return latexExport.export(quiz);
    }

    @org.springframework.transaction.annotation.Transactional(isolation = org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public java.io.ByteArrayOutputStream exportQuiz(int quizId) {
        pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz quiz = quizRepository.findById(quizId).orElseThrow(() -> new <QUIZ_NOT_FOUND>pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(quizId));
        java.lang.String name = quiz.getTitle();
        try (java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(baos)) {
            java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz> quizzes = new java.util.ArrayList<>();
            quizzes.add(quiz);
            pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.QuizzesXmlExport xmlExport = new pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.QuizzesXmlExport();
            java.io.InputStream in = org.apache.commons.io.IOUtils.toInputStream(xmlExport.export(quizzes), java.nio.charset.StandardCharsets.UTF_8);
            zos.putNextEntry(new java.util.zip.ZipEntry(name + ".xml"));
            copyToZipStream(zos, in);
            zos.closeEntry();
            pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.LatexQuizExportVisitor latexExport = new pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.LatexQuizExportVisitor();
            zos.putNextEntry(new java.util.zip.ZipEntry(name + ".tex"));
            in = org.apache.commons.io.IOUtils.toInputStream(latexExport.export(quiz), java.nio.charset.StandardCharsets.UTF_8);
            copyToZipStream(zos, in);
            zos.closeEntry();
            pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.CSVQuizExportVisitor csvExport = new pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.CSVQuizExportVisitor();
            zos.putNextEntry(new java.util.zip.ZipEntry(name + ".csv"));
            in = org.apache.commons.io.IOUtils.toInputStream(csvExport.export(quiz), java.nio.charset.StandardCharsets.UTF_8);
            copyToZipStream(zos, in);
            zos.closeEntry();
            baos.flush();
            return baos;
        } catch (java.io.IOException ex) {
            throw new pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.CANNOT_OPEN_FILE);
        }
    }

    private void copyToZipStream(java.util.zip.ZipOutputStream zos, java.io.InputStream in) throws java.io.IOException {
        byte[] buffer = new byte[1024];
        int len;
        while ((len = in.read(buffer)) > 0) {
            zos.write(buffer, 0, len);
        } 
        in.close();
    }

    @org.springframework.retry.annotation.Retryable(value = { java.sql.SQLException.class }, backoff = @org.springframework.retry.annotation.Backoff(delay = 5000))
    @org.springframework.transaction.annotation.Transactional(isolation = org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public void resetDemoQuizzes() {
        quizRepository.findQuizzes(pt.ulisboa.tecnico.socialsoftware.tutor.config.Demo.COURSE_EXECUTION_ID).stream().filter(quiz -> quiz.getId() > 5360).forEach(quiz -> {
            for (pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer quizAnswer : new java.util.ArrayList<>(quiz.getQuizAnswers())) {
                answerService.deleteQuizAnswer(quizAnswer);
            }
            for (pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion quizQuestion : quiz.getQuizQuestions().stream().filter(quizQuestion -> quizQuestion.getQuestionAnswers().isEmpty()).collect(java.util.stream.Collectors.toList())) {
                questionService.deleteQuizQuestion(quizQuestion);
            }
            quiz.remove();
            this.quizRepository.delete(quiz);
        });
        // remove questions that werent in any quiz
        for (pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question question : questionRepository.findQuestions(pt.ulisboa.tecnico.socialsoftware.tutor.config.Demo.COURSE_ID).stream().filter(question -> question.getQuizQuestions().isEmpty()).collect(java.util.stream.Collectors.toList())) {
            questionService.deleteQuestion(question);
        }
    }
}