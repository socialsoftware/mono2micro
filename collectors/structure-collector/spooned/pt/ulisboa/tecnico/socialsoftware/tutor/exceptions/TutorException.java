package pt.ulisboa.tecnico.socialsoftware.tutor.exceptions;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
@org.springframework.web.bind.annotation.ResponseStatus(org.springframework.http.HttpStatus.BAD_REQUEST)
public class TutorException extends java.lang.RuntimeException {
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException.class);

    private final pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage errorMessage;

    public TutorException(pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage errorMessage) {
        super(errorMessage.label);
        pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException.logger.error(errorMessage.label);
        this.errorMessage = errorMessage;
    }

    public TutorException(pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage errorMessage, java.lang.String value) {
        super(java.lang.String.format(errorMessage.label, value));
        pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException.logger.error(java.lang.String.format(errorMessage.label, value));
        this.errorMessage = errorMessage;
    }

    public TutorException(pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage errorMessage, java.lang.String value1, java.lang.String value2) {
        super(java.lang.String.format(errorMessage.label, value1, value2));
        pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException.logger.error(java.lang.String.format(errorMessage.label, value1, value2));
        this.errorMessage = errorMessage;
    }

    public TutorException(pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage errorMessage, int value) {
        super(java.lang.String.format(errorMessage.label, value));
        pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException.logger.error(java.lang.String.format(errorMessage.label, value));
        this.errorMessage = errorMessage;
    }

    public TutorException(pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage errorMessage, int value1, int value2) {
        super(java.lang.String.format(errorMessage.label, value1, value2));
        pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException.logger.error(java.lang.String.format(errorMessage.label, value1, value2));
        this.errorMessage = errorMessage;
    }

    public pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage getErrorMessage() {
        return errorMessage;
    }
}