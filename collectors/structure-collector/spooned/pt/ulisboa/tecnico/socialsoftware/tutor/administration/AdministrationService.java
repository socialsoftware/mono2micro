package pt.ulisboa.tecnico.socialsoftware.tutor.administration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
@org.springframework.stereotype.Service
public class AdministrationService {
    @org.springframework.beans.factory.annotation.Autowired
    private pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository courseRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository courseExecutionRepository;

    @org.springframework.transaction.annotation.Transactional(isolation = org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseDto createExternalCourseExecution(pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseDto courseDto) {
        checkCourseType(courseDto);
        pt.ulisboa.tecnico.socialsoftware.tutor.course.Course course = getCourse(courseDto);
        if (course == null) {
            course = createCourse(courseDto);
        }
        pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution courseExecution = createCourseExecution(courseDto, course);
        return new pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseDto(courseExecution);
    }

    @org.springframework.transaction.annotation.Transactional(isolation = org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseDto getCourseExecutionById(int courseExecutionId) {
        return courseExecutionRepository.findById(courseExecutionId).map(pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseDto::new).orElseThrow(() -> new <COURSE_EXECUTION_NOT_FOUND>pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(courseExecutionId));
    }

    @org.springframework.transaction.annotation.Transactional(isolation = org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseDto> getCourseExecutions(pt.ulisboa.tecnico.socialsoftware.tutor.user.User.Role role) {
        return courseExecutionRepository.findAll().stream().filter(courseExecution -> role.equals(User.Role.ADMIN) || (role.equals(User.Role.DEMO_ADMIN) && courseExecution.getCourse().getName().equals(Demo.COURSE_NAME))).map(pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseDto::new).sorted(java.util.Comparator.comparing(pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseDto::getName).thenComparing(pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseDto::getAcademicTerm)).collect(java.util.stream.Collectors.toList());
    }

    @org.springframework.transaction.annotation.Transactional(isolation = org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public void removeCourseExecution(int courseExecutionId) {
        pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution courseExecution = courseExecutionRepository.findById(courseExecutionId).orElseThrow(() -> new <COURSE_EXECUTION_NOT_FOUND>pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(courseExecutionId));
        courseExecution.delete();
        courseExecutionRepository.delete(courseExecution);
    }

    private pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution createCourseExecution(pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseDto courseDto, pt.ulisboa.tecnico.socialsoftware.tutor.course.Course course) {
        pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution courseExecution = new pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution(course, courseDto.getAcronym(), courseDto.getAcademicTerm(), pt.ulisboa.tecnico.socialsoftware.tutor.course.Course.Type.EXTERNAL);
        courseExecutionRepository.save(courseExecution);
        return courseExecution;
    }

    private pt.ulisboa.tecnico.socialsoftware.tutor.course.Course createCourse(pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseDto courseDto) {
        pt.ulisboa.tecnico.socialsoftware.tutor.course.Course course;
        if (courseDto.getCourseType().equals(pt.ulisboa.tecnico.socialsoftware.tutor.course.Course.Type.EXTERNAL)) {
            course = new pt.ulisboa.tecnico.socialsoftware.tutor.course.Course(courseDto.getName(), pt.ulisboa.tecnico.socialsoftware.tutor.course.Course.Type.EXTERNAL);
            courseRepository.save(course);
        } else {
            throw new pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.COURSE_NOT_FOUND, courseDto.getName());
        }
        return course;
    }

    private pt.ulisboa.tecnico.socialsoftware.tutor.course.Course getCourse(pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseDto courseDto) {
        pt.ulisboa.tecnico.socialsoftware.tutor.course.Course course;
        if (courseDto.getCourseType().equals(pt.ulisboa.tecnico.socialsoftware.tutor.course.Course.Type.EXTERNAL)) {
            course = courseRepository.findByNameType(courseDto.getName(), pt.ulisboa.tecnico.socialsoftware.tutor.course.Course.Type.EXTERNAL.name()).orElse(null);
        } else {
            course = courseRepository.findByNameType(courseDto.getName(), pt.ulisboa.tecnico.socialsoftware.tutor.course.Course.Type.TECNICO.name()).orElse(null);
        }
        return course;
    }

    private void checkCourseType(pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseDto courseDto) {
        if (courseDto.getCourseType() == null) {
            throw new pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.COURSE_TYPE_NOT_DEFINED);
        }
    }
}