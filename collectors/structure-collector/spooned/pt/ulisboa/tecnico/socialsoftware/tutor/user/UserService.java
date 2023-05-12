package pt.ulisboa.tecnico.socialsoftware.tutor.user;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
@org.springframework.stereotype.Service
public class UserService {
    @org.springframework.beans.factory.annotation.Autowired
    private pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository userRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository courseExecutionRepository;

    public pt.ulisboa.tecnico.socialsoftware.tutor.user.User findByUsername(java.lang.String username) {
        return this.userRepository.findByUsername(username);
    }

    public pt.ulisboa.tecnico.socialsoftware.tutor.user.User findByKey(java.lang.Integer key) {
        return this.userRepository.findByKey(key);
    }

    public java.lang.Integer getMaxUserNumber() {
        java.lang.Integer result = userRepository.getMaxUserNumber();
        return result != null ? result : 0;
    }

    public pt.ulisboa.tecnico.socialsoftware.tutor.user.User createUser(java.lang.String name, java.lang.String username, pt.ulisboa.tecnico.socialsoftware.tutor.user.User.Role role) {
        if (findByUsername(username) != null) {
            throw new pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.DUPLICATE_USER, username);
        }
        pt.ulisboa.tecnico.socialsoftware.tutor.user.User user = new pt.ulisboa.tecnico.socialsoftware.tutor.user.User(name, username, getMaxUserNumber() + 1, role);
        userRepository.save(user);
        return user;
    }

    @org.springframework.transaction.annotation.Transactional(isolation = org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public java.lang.String getEnrolledCoursesAcronyms(java.lang.String username) {
        pt.ulisboa.tecnico.socialsoftware.tutor.user.User user = this.userRepository.findByUsername(username);
        return user.getEnrolledCoursesAcronyms();
    }

    @org.springframework.transaction.annotation.Transactional(isolation = org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseDto> getCourseExecutions(java.lang.String username) {
        pt.ulisboa.tecnico.socialsoftware.tutor.user.User user = this.userRepository.findByUsername(username);
        return user.getCourseExecutions().stream().map(pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseDto::new).collect(java.util.stream.Collectors.toList());
    }

    @org.springframework.transaction.annotation.Transactional(isolation = org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public void addCourseExecution(java.lang.String username, int executionId) {
        pt.ulisboa.tecnico.socialsoftware.tutor.user.User user = this.userRepository.findByUsername(username);
        pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution courseExecution = courseExecutionRepository.findById(executionId).orElseThrow(() -> new pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(COURSE_EXECUTION_NOT_FOUND, executionId));
        user.addCourse(courseExecution);
        courseExecution.addUser(user);
    }

    public java.lang.String exportUsers() {
        pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.UsersXmlExport xmlExporter = new pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.UsersXmlExport();
        return xmlExporter.export(userRepository.findAll());
    }

    @org.springframework.retry.annotation.Retryable(value = { java.sql.SQLException.class }, backoff = @org.springframework.retry.annotation.Backoff(delay = 5000))
    @org.springframework.transaction.annotation.Transactional(isolation = org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public void importUsers(java.lang.String usersXML) {
        pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.UsersXmlImport xmlImporter = new pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.UsersXmlImport();
        xmlImporter.importUsers(usersXML, this);
    }

    @org.springframework.transaction.annotation.Transactional(isolation = org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public pt.ulisboa.tecnico.socialsoftware.tutor.user.User getDemoTeacher() {
        pt.ulisboa.tecnico.socialsoftware.tutor.user.User user = this.userRepository.findByUsername("Demo-Teacher");
        if (user == null)
            return createUser("Demo Teacher", "Demo-Teacher", pt.ulisboa.tecnico.socialsoftware.tutor.user.User.Role.TEACHER);

        return user;
    }

    @org.springframework.transaction.annotation.Transactional(isolation = org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public pt.ulisboa.tecnico.socialsoftware.tutor.user.User getDemoStudent() {
        pt.ulisboa.tecnico.socialsoftware.tutor.user.User user = this.userRepository.findByUsername("Demo-Student");
        if (user == null)
            return createUser("Demo Student", "Demo-Student", pt.ulisboa.tecnico.socialsoftware.tutor.user.User.Role.STUDENT);

        return user;
    }

    @org.springframework.transaction.annotation.Transactional(isolation = org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public pt.ulisboa.tecnico.socialsoftware.tutor.user.User getDemoAdmin() {
        pt.ulisboa.tecnico.socialsoftware.tutor.user.User user = this.userRepository.findByUsername("Demo-Admin");
        if (user == null)
            return createUser("Demo Admin", "Demo-Admin", pt.ulisboa.tecnico.socialsoftware.tutor.user.User.Role.DEMO_ADMIN);

        return user;
    }

    @org.springframework.transaction.annotation.Transactional(isolation = org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public pt.ulisboa.tecnico.socialsoftware.tutor.user.User createDemoStudent() {
        java.lang.String birthDate = java.time.LocalDateTime.now().toString();
        pt.ulisboa.tecnico.socialsoftware.tutor.user.User newDemoUser = createUser("Demo-Student-" + birthDate, "Demo-Student-" + birthDate, pt.ulisboa.tecnico.socialsoftware.tutor.user.User.Role.STUDENT);
        pt.ulisboa.tecnico.socialsoftware.tutor.user.User demoUser = this.userRepository.findByUsername("Demo-Student");
        pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution courseExecution = demoUser.getCourseExecutions().stream().findAny().orElse(null);
        if (courseExecution != null) {
            courseExecution.addUser(newDemoUser);
            newDemoUser.addCourse(courseExecution);
        }
        return newDemoUser;
    }
}