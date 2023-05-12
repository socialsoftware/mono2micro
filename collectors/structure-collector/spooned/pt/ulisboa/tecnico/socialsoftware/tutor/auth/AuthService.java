package pt.ulisboa.tecnico.socialsoftware.tutor.auth;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
@org.springframework.stereotype.Service
public class AuthService {
    @org.springframework.beans.factory.annotation.Value("${spring.profiles.active}")
    private java.lang.String activeProfile;

    @org.springframework.beans.factory.annotation.Autowired
    private pt.ulisboa.tecnico.socialsoftware.tutor.user.UserService userService;

    @org.springframework.beans.factory.annotation.Autowired
    private pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository courseRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository courseExecutionRepository;

    @org.springframework.retry.annotation.Retryable(value = { java.sql.SQLException.class }, backoff = @org.springframework.retry.annotation.Backoff(delay = 5000))
    @org.springframework.transaction.annotation.Transactional(isolation = org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public pt.ulisboa.tecnico.socialsoftware.tutor.auth.AuthDto fenixAuth(pt.ulisboa.tecnico.socialsoftware.tutor.auth.FenixEduInterface fenix) {
        java.lang.String username = fenix.getPersonUsername();
        java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseDto> fenixAttendingCourses = fenix.getPersonAttendingCourses();
        java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseDto> fenixTeachingCourses = fenix.getPersonTeachingCourses();
        java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution> activeAttendingCourses = getActiveTecnicoCourses(fenixAttendingCourses);
        java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution> activeTeachingCourses = getActiveTecnicoCourses(fenixTeachingCourses);
        pt.ulisboa.tecnico.socialsoftware.tutor.user.User user = this.userService.findByUsername(username);
        // If user is student and is not in db
        if ((user == null) && (!activeAttendingCourses.isEmpty())) {
            user = this.userService.createUser(fenix.getPersonName(), username, pt.ulisboa.tecnico.socialsoftware.tutor.user.User.Role.STUDENT);
        }
        // If user is teacher and is not in db
        if ((user == null) && (!fenixTeachingCourses.isEmpty())) {
            user = this.userService.createUser(fenix.getPersonName(), username, pt.ulisboa.tecnico.socialsoftware.tutor.user.User.Role.TEACHER);
        }
        if (user == null) {
            throw new pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.USER_NOT_ENROLLED, username);
        }
        user.setLastAccess(java.time.LocalDateTime.now());
        if (user.getRole() == pt.ulisboa.tecnico.socialsoftware.tutor.user.User.Role.ADMIN) {
            java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseDto> allCoursesInDb = courseExecutionRepository.findAll().stream().map(pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseDto::new).collect(java.util.stream.Collectors.toList());
            if (!fenixTeachingCourses.isEmpty()) {
                pt.ulisboa.tecnico.socialsoftware.tutor.user.User finalUser = user;
                activeTeachingCourses.stream().filter(courseExecution -> !finalUser.getCourseExecutions().contains(courseExecution)).forEach(user::addCourse);
                allCoursesInDb.addAll(fenixTeachingCourses);
                java.lang.String ids = fenixTeachingCourses.stream().map(courseDto -> courseDto.getAcronym() + courseDto.getAcademicTerm()).collect(java.util.stream.Collectors.joining(","));
                user.setEnrolledCoursesAcronyms(ids);
            }
            return new pt.ulisboa.tecnico.socialsoftware.tutor.auth.AuthDto(pt.ulisboa.tecnico.socialsoftware.tutor.auth.JwtTokenProvider.generateToken(user), new pt.ulisboa.tecnico.socialsoftware.tutor.user.dto.AuthUserDto(user, allCoursesInDb));
        }
        // Update student courses
        if ((!activeAttendingCourses.isEmpty()) && (user.getRole() == pt.ulisboa.tecnico.socialsoftware.tutor.user.User.Role.STUDENT)) {
            pt.ulisboa.tecnico.socialsoftware.tutor.user.User student = user;
            activeAttendingCourses.stream().filter(courseExecution -> !student.getCourseExecutions().contains(courseExecution)).forEach(user::addCourse);
            return new pt.ulisboa.tecnico.socialsoftware.tutor.auth.AuthDto(pt.ulisboa.tecnico.socialsoftware.tutor.auth.JwtTokenProvider.generateToken(user), new pt.ulisboa.tecnico.socialsoftware.tutor.user.dto.AuthUserDto(user));
        }
        // Update teacher courses
        if ((!fenixTeachingCourses.isEmpty()) && (user.getRole() == pt.ulisboa.tecnico.socialsoftware.tutor.user.User.Role.TEACHER)) {
            pt.ulisboa.tecnico.socialsoftware.tutor.user.User teacher = user;
            activeTeachingCourses.stream().filter(courseExecution -> !teacher.getCourseExecutions().contains(courseExecution)).forEach(user::addCourse);
            java.lang.String ids = fenixTeachingCourses.stream().map(courseDto -> courseDto.getAcronym() + courseDto.getAcademicTerm()).collect(java.util.stream.Collectors.joining(","));
            user.setEnrolledCoursesAcronyms(ids);
            return new pt.ulisboa.tecnico.socialsoftware.tutor.auth.AuthDto(pt.ulisboa.tecnico.socialsoftware.tutor.auth.JwtTokenProvider.generateToken(user), new pt.ulisboa.tecnico.socialsoftware.tutor.user.dto.AuthUserDto(user, fenixTeachingCourses));
        }
        // Previous teacher without active courses
        if (user.getRole() == pt.ulisboa.tecnico.socialsoftware.tutor.user.User.Role.TEACHER) {
            return new pt.ulisboa.tecnico.socialsoftware.tutor.auth.AuthDto(pt.ulisboa.tecnico.socialsoftware.tutor.auth.JwtTokenProvider.generateToken(user), new pt.ulisboa.tecnico.socialsoftware.tutor.user.dto.AuthUserDto(user));
        }
        throw new pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.USER_NOT_ENROLLED, username);
    }

    private java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution> getActiveTecnicoCourses(java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseDto> courses) {
        return courses.stream().map(courseDto -> {
            pt.ulisboa.tecnico.socialsoftware.tutor.course.Course course = courseRepository.findByNameType(courseDto.getName(), pt.ulisboa.tecnico.socialsoftware.tutor.course.Course.Type.TECNICO.name()).orElse(null);
            if (course == null) {
                return null;
            }
            return course.getCourseExecution(courseDto.getAcronym(), courseDto.getAcademicTerm(), pt.ulisboa.tecnico.socialsoftware.tutor.course.Course.Type.TECNICO).orElse(null);
        }).filter(java.util.Objects::nonNull).collect(java.util.stream.Collectors.toList());
    }

    @org.springframework.retry.annotation.Retryable(value = { java.sql.SQLException.class }, maxAttempts = 2, backoff = @org.springframework.retry.annotation.Backoff(delay = 5000))
    @org.springframework.transaction.annotation.Transactional(isolation = org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public pt.ulisboa.tecnico.socialsoftware.tutor.auth.AuthDto demoStudentAuth() {
        pt.ulisboa.tecnico.socialsoftware.tutor.user.User user;
        // if (activeProfile.equals("dev")) {
        // user = this.userService.createDemoStudent();
        // } else {
        user = this.userService.getDemoStudent();
        // }
        return new pt.ulisboa.tecnico.socialsoftware.tutor.auth.AuthDto(pt.ulisboa.tecnico.socialsoftware.tutor.auth.JwtTokenProvider.generateToken(user), new pt.ulisboa.tecnico.socialsoftware.tutor.user.dto.AuthUserDto(user));
    }

    @org.springframework.retry.annotation.Retryable(value = { java.sql.SQLException.class }, maxAttempts = 2, backoff = @org.springframework.retry.annotation.Backoff(delay = 5000))
    @org.springframework.transaction.annotation.Transactional(isolation = org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public pt.ulisboa.tecnico.socialsoftware.tutor.auth.AuthDto demoTeacherAuth() {
        pt.ulisboa.tecnico.socialsoftware.tutor.user.User user = this.userService.getDemoTeacher();
        return new pt.ulisboa.tecnico.socialsoftware.tutor.auth.AuthDto(pt.ulisboa.tecnico.socialsoftware.tutor.auth.JwtTokenProvider.generateToken(user), new pt.ulisboa.tecnico.socialsoftware.tutor.user.dto.AuthUserDto(user));
    }

    @org.springframework.retry.annotation.Retryable(value = { java.sql.SQLException.class }, maxAttempts = 2, backoff = @org.springframework.retry.annotation.Backoff(delay = 5000))
    @org.springframework.transaction.annotation.Transactional(isolation = org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public pt.ulisboa.tecnico.socialsoftware.tutor.auth.AuthDto demoAdminAuth() {
        pt.ulisboa.tecnico.socialsoftware.tutor.user.User user = this.userService.getDemoAdmin();
        return new pt.ulisboa.tecnico.socialsoftware.tutor.auth.AuthDto(pt.ulisboa.tecnico.socialsoftware.tutor.auth.JwtTokenProvider.generateToken(user), new pt.ulisboa.tecnico.socialsoftware.tutor.user.dto.AuthUserDto(user));
    }
}