package pt.ulisboa.tecnico.socialsoftware.tutor.question;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
@org.springframework.stereotype.Service
public class QuestionService {
    @org.springframework.beans.factory.annotation.Autowired
    private pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository courseRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository questionRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.TopicRepository topicRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.ImageRepository imageRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private pt.ulisboa.tecnico.socialsoftware.tutor.quiz.repository.QuizQuestionRepository quizQuestionRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.OptionRepository optionRepository;

    @org.springframework.retry.annotation.Retryable(value = { java.sql.SQLException.class }, backoff = @org.springframework.retry.annotation.Backoff(delay = 5000))
    @org.springframework.transaction.annotation.Transactional(isolation = org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto findQuestionById(java.lang.Integer questionId) {
        return questionRepository.findById(questionId).map(pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto::new).orElseThrow(() -> new pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(QUESTION_NOT_FOUND, questionId));
    }

    @org.springframework.transaction.annotation.Transactional(isolation = org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseDto findQuestionCourse(java.lang.Integer questionId) {
        return questionRepository.findById(questionId).map(pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question::getCourse).map(pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseDto::new).orElseThrow(() -> new pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(QUESTION_NOT_FOUND, questionId));
    }

    @org.springframework.retry.annotation.Retryable(value = { java.sql.SQLException.class }, backoff = @org.springframework.retry.annotation.Backoff(delay = 5000))
    @org.springframework.transaction.annotation.Transactional(isolation = org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto findQuestionByKey(java.lang.Integer key) {
        return questionRepository.findByKey(key).map(pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto::new).orElseThrow(() -> new pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.QUESTION_NOT_FOUND, key));
    }

    @org.springframework.retry.annotation.Retryable(value = { java.sql.SQLException.class }, backoff = @org.springframework.retry.annotation.Backoff(delay = 5000))
    @org.springframework.transaction.annotation.Transactional(isolation = org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto> findQuestions(int courseId) {
        return questionRepository.findQuestions(courseId).stream().map(pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto::new).collect(java.util.stream.Collectors.toList());
    }

    @org.springframework.retry.annotation.Retryable(value = { java.sql.SQLException.class }, backoff = @org.springframework.retry.annotation.Backoff(delay = 5000))
    @org.springframework.transaction.annotation.Transactional(isolation = org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto> findAvailableQuestions(int courseId) {
        return questionRepository.findAvailableQuestions(courseId).stream().map(pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto::new).collect(java.util.stream.Collectors.toList());
    }

    @org.springframework.retry.annotation.Retryable(value = { java.sql.SQLException.class }, backoff = @org.springframework.retry.annotation.Backoff(delay = 5000))
    @org.springframework.transaction.annotation.Transactional(isolation = org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto createQuestion(int courseId, pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto questionDto) {
        pt.ulisboa.tecnico.socialsoftware.tutor.course.Course course = courseRepository.findById(courseId).orElseThrow(() -> new pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(COURSE_NOT_FOUND, courseId));
        if (questionDto.getCreationDate() == null) {
            questionDto.setCreationDate(java.time.LocalDateTime.now().format(pt.ulisboa.tecnico.socialsoftware.tutor.course.Course.formatter));
        }
        pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question question = new pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question(course, questionDto);
        questionRepository.save(question);
        return new pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto(question);
    }

    @org.springframework.retry.annotation.Retryable(value = { java.sql.SQLException.class }, backoff = @org.springframework.retry.annotation.Backoff(delay = 5000))
    @org.springframework.transaction.annotation.Transactional(isolation = org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto updateQuestion(java.lang.Integer questionId, pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto questionDto) {
        pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question question = questionRepository.findById(questionId).orElseThrow(() -> new pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(QUESTION_NOT_FOUND, questionId));
        question.update(questionDto);
        return new pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto(question);
    }

    @org.springframework.retry.annotation.Retryable(value = { java.sql.SQLException.class }, backoff = @org.springframework.retry.annotation.Backoff(delay = 5000))
    @org.springframework.transaction.annotation.Transactional(isolation = org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public void removeQuestion(java.lang.Integer questionId) {
        pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question question = questionRepository.findById(questionId).orElseThrow(() -> new pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(QUESTION_NOT_FOUND, questionId));
        question.remove();
        questionRepository.delete(question);
    }

    @org.springframework.retry.annotation.Retryable(value = { java.sql.SQLException.class }, backoff = @org.springframework.retry.annotation.Backoff(delay = 5000))
    @org.springframework.transaction.annotation.Transactional(isolation = org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public void questionSetStatus(java.lang.Integer questionId, pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question.Status status) {
        pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question question = questionRepository.findById(questionId).orElseThrow(() -> new pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(QUESTION_NOT_FOUND, questionId));
        question.setStatus(status);
    }

    @org.springframework.retry.annotation.Retryable(value = { java.sql.SQLException.class }, backoff = @org.springframework.retry.annotation.Backoff(delay = 5000))
    @org.springframework.transaction.annotation.Transactional(isolation = org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public void uploadImage(java.lang.Integer questionId, java.lang.String type) {
        pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question question = questionRepository.findById(questionId).orElseThrow(() -> new pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(QUESTION_NOT_FOUND, questionId));
        pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Image image = question.getImage();
        if (image == null) {
            image = new pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Image();
            question.setImage(image);
            imageRepository.save(image);
        }
        question.getImage().setUrl((((question.getCourse().getName().replaceAll("\\s", "") + question.getCourse().getType()) + question.getKey()) + ".") + type);
    }

    @org.springframework.retry.annotation.Retryable(value = { java.sql.SQLException.class }, backoff = @org.springframework.retry.annotation.Backoff(delay = 5000))
    @org.springframework.transaction.annotation.Transactional(isolation = org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public void updateQuestionTopics(java.lang.Integer questionId, pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto[] topics) {
        pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question question = questionRepository.findById(questionId).orElseThrow(() -> new pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(QUESTION_NOT_FOUND, questionId));
        question.updateTopics(java.util.Arrays.stream(topics).map(topicDto -> topicRepository.findTopicByName(question.getCourse().getId(), topicDto.getName())).collect(java.util.stream.Collectors.toSet()));
    }

    public java.lang.String exportQuestionsToXml() {
        pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.XMLQuestionExportVisitor xmlExporter = new pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.XMLQuestionExportVisitor();
        return xmlExporter.export(questionRepository.findAll());
    }

    @org.springframework.retry.annotation.Retryable(value = { java.sql.SQLException.class }, backoff = @org.springframework.retry.annotation.Backoff(delay = 5000))
    @org.springframework.transaction.annotation.Transactional(isolation = org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public void importQuestionsFromXml(java.lang.String questionsXML) {
        pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.QuestionsXmlImport xmlImporter = new pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.QuestionsXmlImport();
        xmlImporter.importQuestions(questionsXML, this, courseRepository);
    }

    @org.springframework.retry.annotation.Retryable(value = { java.sql.SQLException.class }, backoff = @org.springframework.retry.annotation.Backoff(delay = 5000))
    @org.springframework.transaction.annotation.Transactional(isolation = org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public java.lang.String exportQuestionsToLatex() {
        pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.LatexQuestionExportVisitor latexExporter = new pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.LatexQuestionExportVisitor();
        return latexExporter.export(questionRepository.findAll());
    }

    @org.springframework.retry.annotation.Retryable(value = { java.sql.SQLException.class }, backoff = @org.springframework.retry.annotation.Backoff(delay = 5000))
    @org.springframework.transaction.annotation.Transactional(isolation = org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public java.io.ByteArrayOutputStream exportCourseQuestions(int courseId) {
        pt.ulisboa.tecnico.socialsoftware.tutor.course.Course course = courseRepository.findById(courseId).orElseThrow(() -> new pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(COURSE_NOT_FOUND, courseId));
        course.getQuestions();
        java.lang.String name = course.getName();
        try (java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(baos)) {
            java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question> questions = new java.util.ArrayList<>(course.getQuestions());
            pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.XMLQuestionExportVisitor xmlExport = new pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.XMLQuestionExportVisitor();
            java.io.InputStream in = org.apache.commons.io.IOUtils.toInputStream(xmlExport.export(questions), java.nio.charset.StandardCharsets.UTF_8);
            zos.putNextEntry(new java.util.zip.ZipEntry(name + ".xml"));
            copyToZipStream(zos, in);
            zos.closeEntry();
            pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.LatexQuestionExportVisitor latexExport = new pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.LatexQuestionExportVisitor();
            zos.putNextEntry(new java.util.zip.ZipEntry(name + ".tex"));
            in = org.apache.commons.io.IOUtils.toInputStream(latexExport.export(questions), java.nio.charset.StandardCharsets.UTF_8);
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
    public void deleteQuizQuestion(pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion quizQuestion) {
        pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question question = quizQuestion.getQuestion();
        quizQuestion.remove();
        quizQuestionRepository.delete(quizQuestion);
        if (question.getQuizQuestions().isEmpty()) {
            this.deleteQuestion(question);
        }
    }

    @org.springframework.retry.annotation.Retryable(value = { java.sql.SQLException.class }, backoff = @org.springframework.retry.annotation.Backoff(delay = 5000))
    @org.springframework.transaction.annotation.Transactional(isolation = org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public void deleteQuestion(pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question question) {
        for (pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Option option : question.getOptions()) {
            option.remove();
            optionRepository.delete(option);
        }
        if (question.getImage() != null) {
            imageRepository.delete(question.getImage());
        }
        question.getTopics().forEach(topic -> topic.getQuestions().remove(question));
        question.getTopics().clear();
        questionRepository.delete(question);
    }
}