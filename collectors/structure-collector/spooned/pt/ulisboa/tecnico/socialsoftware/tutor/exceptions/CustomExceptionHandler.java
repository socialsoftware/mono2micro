package pt.ulisboa.tecnico.socialsoftware.tutor.exceptions;
import org.apache.catalina.connector.ClientAbortException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
// https://www.toptal.com/java/spring-boot-rest-api-error-handling
@org.springframework.web.bind.annotation.RestControllerAdvice
public class CustomExceptionHandler extends org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler {
    private static org.slf4j.Logger myLogger = org.slf4j.LoggerFactory.getLogger(pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.CustomExceptionHandler.class);

    @org.springframework.web.bind.annotation.ExceptionHandler(pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException.class)
    @org.springframework.web.bind.annotation.ResponseStatus(org.springframework.http.HttpStatus.BAD_REQUEST)
    public pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorExceptionDto tutorException(pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException e) {
        return new pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorExceptionDto(e);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    @org.springframework.web.bind.annotation.ResponseStatus(org.springframework.http.HttpStatus.FORBIDDEN)
    public pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorExceptionDto accessDeniedException(org.springframework.security.access.AccessDeniedException e) {
        pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.CustomExceptionHandler.myLogger.error(e.getMessage());
        return new pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorExceptionDto(pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.ACCESS_DENIED);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(org.apache.catalina.connector.ClientAbortException.class)
    @org.springframework.web.bind.annotation.ResponseStatus(org.springframework.http.HttpStatus.OK)
    public void clientAbortException(org.apache.catalina.connector.ClientAbortException e) {
        // Ignore my broken pipe. It still works
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(java.lang.Exception.class)
    @org.springframework.web.bind.annotation.ResponseStatus(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
    public pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorExceptionDto randomException(java.lang.Exception e) {
        pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.CustomExceptionHandler.myLogger.error(e.getMessage(), e);
        return new pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorExceptionDto(e);
    }
}