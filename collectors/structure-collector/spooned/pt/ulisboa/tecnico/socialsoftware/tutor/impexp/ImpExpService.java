package pt.ulisboa.tecnico.socialsoftware.tutor.impexp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
@org.springframework.stereotype.Service
public class ImpExpService {
    public static final java.lang.String PATH_DELIMITER = "/";

    @org.springframework.beans.factory.annotation.Autowired
    private pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository userRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository questionRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.TopicRepository topicRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private pt.ulisboa.tecnico.socialsoftware.tutor.quiz.repository.QuizRepository quizRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private pt.ulisboa.tecnico.socialsoftware.tutor.quiz.repository.QuizQuestionRepository quizQuestionRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private pt.ulisboa.tecnico.socialsoftware.tutor.answer.repository.QuizAnswerRepository quizAnswerRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository courseRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository courseExecutionRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private pt.ulisboa.tecnico.socialsoftware.tutor.user.UserService userService;

    @org.springframework.beans.factory.annotation.Autowired
    private pt.ulisboa.tecnico.socialsoftware.tutor.question.QuestionService questionService;

    @org.springframework.beans.factory.annotation.Autowired
    private pt.ulisboa.tecnico.socialsoftware.tutor.question.TopicService topicService;

    @org.springframework.beans.factory.annotation.Autowired
    private pt.ulisboa.tecnico.socialsoftware.tutor.quiz.QuizService quizService;

    @org.springframework.beans.factory.annotation.Autowired
    private pt.ulisboa.tecnico.socialsoftware.tutor.answer.AnswerService answerService;

    @org.springframework.beans.factory.annotation.Autowired
    private pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.AnswersXmlImport answersXmlImport;

    @org.springframework.beans.factory.annotation.Value("${load.dir}")
    private java.lang.String loadDir;

    @org.springframework.beans.factory.annotation.Value("${export.dir}")
    private java.lang.String exportDir;

    @org.springframework.retry.annotation.Retryable(value = { java.sql.SQLException.class }, backoff = @org.springframework.retry.annotation.Backoff(delay = 5000))
    @org.springframework.transaction.annotation.Transactional(isolation = org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public java.lang.String exportAll() {
        java.lang.String timeStamp = new java.text.SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date());
        java.io.File directory = new java.io.File(exportDir);
        java.lang.String filename = ("tutor-" + timeStamp) + ".zip";
        try (java.io.FileOutputStream fos = new java.io.FileOutputStream((directory.getPath() + pt.ulisboa.tecnico.socialsoftware.tutor.impexp.ImpExpService.PATH_DELIMITER) + filename);java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(fos)) {
            zos.putNextEntry(new java.util.zip.ZipEntry("users.xml"));
            java.io.InputStream in = generateUsersInputStream();
            copyToZipStream(zos, in);
            zos.closeEntry();
            zos.putNextEntry(new java.util.zip.ZipEntry("questions.xml"));
            in = generateQuestionsInputStream();
            copyToZipStream(zos, in);
            zos.closeEntry();
            zos.putNextEntry(new java.util.zip.ZipEntry("topics.xml"));
            in = generateTopicsInputStream();
            copyToZipStream(zos, in);
            zos.closeEntry();
            zos.putNextEntry(new java.util.zip.ZipEntry("quizzes.xml"));
            in = generateQuizzesInputStream();
            copyToZipStream(zos, in);
            zos.closeEntry();
            zos.putNextEntry(new java.util.zip.ZipEntry("answers.xml"));
            in = generateAnswersInputStream();
            copyToZipStream(zos, in);
            zos.closeEntry();
        } catch (java.io.IOException ex) {
            throw new pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.CANNOT_OPEN_FILE);
        }
        return filename;
    }

    private void copyToZipStream(java.util.zip.ZipOutputStream zos, java.io.InputStream in) throws java.io.IOException {
        byte[] buffer = new byte[1024];
        int len;
        while ((len = in.read(buffer)) > 0) {
            zos.write(buffer, 0, len);
        } 
        in.close();
    }

    private java.io.InputStream generateUsersInputStream() {
        pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.UsersXmlExport usersExporter = new pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.UsersXmlExport();
        return org.apache.commons.io.IOUtils.toInputStream(usersExporter.export(userRepository.findAll()), java.nio.charset.StandardCharsets.UTF_8);
    }

    private java.io.InputStream generateQuestionsInputStream() {
        pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.XMLQuestionExportVisitor generator = new pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.XMLQuestionExportVisitor();
        return org.apache.commons.io.IOUtils.toInputStream(generator.export(questionRepository.findAll()), java.nio.charset.StandardCharsets.UTF_8);
    }

    private java.io.InputStream generateTopicsInputStream() {
        pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.TopicsXmlExport generator = new pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.TopicsXmlExport();
        return org.apache.commons.io.IOUtils.toInputStream(generator.export(topicRepository.findAll()), java.nio.charset.StandardCharsets.UTF_8);
    }

    private java.io.InputStream generateQuizzesInputStream() {
        pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.QuizzesXmlExport generator = new pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.QuizzesXmlExport();
        return org.apache.commons.io.IOUtils.toInputStream(generator.export(quizRepository.findAll()), java.nio.charset.StandardCharsets.UTF_8);
    }

    private java.io.InputStream generateAnswersInputStream() {
        pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.AnswersXmlExport generator = new pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.AnswersXmlExport();
        return org.apache.commons.io.IOUtils.toInputStream(generator.export(quizAnswerRepository.findAll()), java.nio.charset.StandardCharsets.UTF_8);
    }

    @org.springframework.retry.annotation.Retryable(value = { java.sql.SQLException.class }, backoff = @org.springframework.retry.annotation.Backoff(delay = 5000))
    @org.springframework.transaction.annotation.Transactional(isolation = org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public void importAll() {
        if (userRepository.findAll().isEmpty()) {
            try {
                java.io.File directory = new java.io.File(loadDir);
                java.io.File usersFile = new java.io.File((directory.getPath() + pt.ulisboa.tecnico.socialsoftware.tutor.impexp.ImpExpService.PATH_DELIMITER) + "users.xml");
                pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.UsersXmlImport usersXmlImport = new pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.UsersXmlImport();
                usersXmlImport.importUsers(new java.io.FileInputStream(usersFile), userService);
                java.io.File questionsFile = new java.io.File((directory.getPath() + pt.ulisboa.tecnico.socialsoftware.tutor.impexp.ImpExpService.PATH_DELIMITER) + "questions.xml");
                pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.QuestionsXmlImport questionsXmlImport = new pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.QuestionsXmlImport();
                questionsXmlImport.importQuestions(new java.io.FileInputStream(questionsFile), questionService, courseRepository);
                java.io.File topicsFile = new java.io.File((directory.getPath() + pt.ulisboa.tecnico.socialsoftware.tutor.impexp.ImpExpService.PATH_DELIMITER) + "topics.xml");
                pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.TopicsXmlImport topicsXmlImport = new pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.TopicsXmlImport();
                topicsXmlImport.importTopics(new java.io.FileInputStream(topicsFile), topicService, questionService, courseRepository);
                java.io.File quizzesFile = new java.io.File((directory.getPath() + pt.ulisboa.tecnico.socialsoftware.tutor.impexp.ImpExpService.PATH_DELIMITER) + "quizzes.xml");
                pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.QuizzesXmlImport quizzesXmlImport = new pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.QuizzesXmlImport();
                quizzesXmlImport.importQuizzes(new java.io.FileInputStream(quizzesFile), quizService, questionRepository, quizQuestionRepository, courseExecutionRepository);
                java.io.File answersFile = new java.io.File((directory.getPath() + pt.ulisboa.tecnico.socialsoftware.tutor.impexp.ImpExpService.PATH_DELIMITER) + "answers.xml");
                answersXmlImport.importAnswers(new java.io.FileInputStream(answersFile), answerService, questionRepository, quizRepository, quizAnswerRepository, userRepository);
            } catch (java.io.FileNotFoundException e) {
                throw new pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.CANNOT_OPEN_FILE);
            }
        }
    }
}