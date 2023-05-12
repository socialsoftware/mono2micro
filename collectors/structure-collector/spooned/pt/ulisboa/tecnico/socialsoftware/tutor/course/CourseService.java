package pt.ulisboa.tecnico.socialsoftware.tutor.course;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
@org.springframework.stereotype.Service
public class CourseService {
    @org.springframework.beans.factory.annotation.Autowired
    private pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository courseRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository courseExecutionRepository;

    @org.springframework.retry.annotation.Retryable(value = { java.sql.SQLException.class }, backoff = @org.springframework.retry.annotation.Backoff(delay = 5000))
    @org.springframework.transaction.annotation.Transactional(isolation = org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseDto> getCourses() {
        return courseRepository.findAll().stream().map(pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseDto::new).sorted(java.util.Comparator.comparing(pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseDto::getName)).collect(java.util.stream.Collectors.toList());
    }

    @org.springframework.retry.annotation.Retryable(value = { java.sql.SQLException.class }, backoff = @org.springframework.retry.annotation.Backoff(delay = 5000))
    @org.springframework.transaction.annotation.Transactional(isolation = org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseDto> getCourseExecutions(int courseId) {
        pt.ulisboa.tecnico.socialsoftware.tutor.course.Course course = courseRepository.findById(courseId).orElseThrow(() -> new pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(COURSE_NOT_FOUND, courseId));
        return course.getCourseExecutions().stream().map(pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseDto::new).sorted(java.util.Comparator.comparing(pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseDto::getAcademicTerm).reversed()).collect(java.util.stream.Collectors.toList());
    }

    @org.springframework.retry.annotation.Retryable(value = { java.sql.SQLException.class }, backoff = @org.springframework.retry.annotation.Backoff(delay = 5000))
    @org.springframework.transaction.annotation.Transactional(isolation = org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseDto createTecnicoCourseExecution(pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseDto courseDto) {
        pt.ulisboa.tecnico.socialsoftware.tutor.course.Course course = courseRepository.findByNameType(courseDto.getName(), pt.ulisboa.tecnico.socialsoftware.tutor.course.Course.Type.TECNICO.name()).orElse(null);
        if (course == null) {
            course = new pt.ulisboa.tecnico.socialsoftware.tutor.course.Course(courseDto.getName(), pt.ulisboa.tecnico.socialsoftware.tutor.course.Course.Type.TECNICO);
            courseRepository.save(course);
        }
        pt.ulisboa.tecnico.socialsoftware.tutor.course.Course existingCourse = course;
        pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution courseExecution = existingCourse.getCourseExecution(courseDto.getAcronym(), courseDto.getAcademicTerm(), pt.ulisboa.tecnico.socialsoftware.tutor.course.Course.Type.TECNICO).orElseGet(() -> {
            pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution ce = new pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution(existingCourse, courseDto.getAcronym(), courseDto.getAcademicTerm(), pt.ulisboa.tecnico.socialsoftware.tutor.course.Course.Type.TECNICO);
            courseExecutionRepository.save(ce);
            return ce;
        });
        courseExecution.setStatus(pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution.Status.ACTIVE);
        return new pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseDto(courseExecution);
    }

    @org.springframework.retry.annotation.Retryable(value = { java.sql.SQLException.class }, backoff = @org.springframework.retry.annotation.Backoff(delay = 5000))
    @org.springframework.transaction.annotation.Transactional(isolation = org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.user.dto.StudentDto> courseStudents(int executionId) {
        pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution courseExecution = courseExecutionRepository.findById(executionId).orElse(null);
        if (courseExecution == null) {
            return new java.util.ArrayList<>();
        }
        return courseExecution.getUsers().stream().filter(user -> user.getRole().equals(pt.ulisboa.tecnico.socialsoftware.tutor.user.User.Role.STUDENT)).sorted(java.util.Comparator.comparing(pt.ulisboa.tecnico.socialsoftware.tutor.user.User::getKey)).map(pt.ulisboa.tecnico.socialsoftware.tutor.user.dto.StudentDto::new).collect(java.util.stream.Collectors.toList());
    }
}