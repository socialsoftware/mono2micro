package pt.ulisboa.tecnico.socialsoftware.tutor.exceptions;
public class TutorExceptionDto implements pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorExceptionSubError {
    @com.fasterxml.jackson.annotation.JsonFormat(shape = com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm")
    private java.time.LocalDateTime timestamp;

    private java.lang.String message;

    private java.lang.String debugMessage;

    private java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorExceptionSubError> subErrors;

    TutorExceptionDto(java.lang.Throwable ex) {
        this.timestamp = java.time.LocalDateTime.now();
        this.message = "Unexpected error";
        this.debugMessage = ex.getLocalizedMessage();
    }

    public TutorExceptionDto(pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException e) {
        this.timestamp = java.time.LocalDateTime.now();
        this.message = e.getMessage();
    }

    public TutorExceptionDto(pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage errorMessage) {
        this.timestamp = java.time.LocalDateTime.now();
        this.message = errorMessage.label;
    }

    public java.time.LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(java.time.LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public java.lang.String getMessage() {
        return message;
    }

    public void setMessage(java.lang.String message) {
        this.message = message;
    }

    public java.lang.String getDebugMessage() {
        return debugMessage;
    }

    public void setDebugMessage(java.lang.String debugMessage) {
        this.debugMessage = debugMessage;
    }

    public java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorExceptionSubError> getSubErrors() {
        return subErrors;
    }

    public void setSubErrors(java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorExceptionSubError> subErrors) {
        this.subErrors = subErrors;
    }
}