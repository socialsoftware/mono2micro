package collector.results;

public class Calls {

    private String callerClass;
    private String callerMethod;
    private String callerLocation;
    private String calleeClass;
    private String calleeMethod;
    private String calleeLocation;
    private String calleeRetType;
    private String calleeSignature;
    private String callLocation;

    public Calls(String callerClass, String callerMethod, String callerLocation, String calleeClass, String calleeMethod, String calleeLocation, String callLocation) {
        this.callerClass = callerClass;
        this.callerMethod = callerMethod;
        this.callerLocation = callerLocation;
        this.calleeClass = calleeClass;
        this.calleeMethod = calleeMethod;
        this.calleeLocation = calleeLocation;
        this.callLocation = callLocation;
    }

    public Calls(String callerClass, String callerMethod, String callerLocation, String calleeClass, String calleeMethod, String calleeLocation, String calleeRetType, String calleeSignature, String callLocation) {
        this.callerClass = callerClass;
        this.callerMethod = callerMethod;
        this.callerLocation = callerLocation;
        this.calleeClass = calleeClass;
        this.calleeMethod = calleeMethod;
        this.calleeLocation = calleeLocation;
        this.calleeRetType = calleeRetType;
        this.calleeSignature = calleeSignature;
        this.callLocation = callLocation;
    }

    public String getCallerClass() {
        return callerClass;
    }

    public void setCallerClass(String callerClass) {
        this.callerClass = callerClass;
    }

    public String getCallerMethod() {
        return callerMethod;
    }

    public void setCallerMethod(String callerMethod) {
        this.callerMethod = callerMethod;
    }

    public String getCalleeClass() {
        return calleeClass;
    }

    public void setCalleeClass(String calleeClass) {
        this.calleeClass = calleeClass;
    }

    public String getCalleeMethod() {
        return calleeMethod;
    }

    public void setCalleeMethod(String calleeMethod) {
        this.calleeMethod = calleeMethod;
    }

    public String getCalleeRetType() {
        return calleeRetType;
    }

    public void setCalleeRetType(String calleeRetType) {
        this.calleeRetType = calleeRetType;
    }

    public String getCalleeSignature() {
        return calleeSignature;
    }

    public void setCalleeSignature(String calleeSignature) {
        this.calleeSignature = calleeSignature;
    }

    public String getCallLocation() {
        return callLocation;
    }

    public void setCallLocation(String callLocation) {
        this.callLocation = callLocation;
    }

    public String getFullCallerMethodName() {
        return this.callerClass + "." + this.callerMethod;
    }

    public String getCallerLocation() {
        return callerLocation;
    }

    public void setCallerLocation(String callerLocation) {
        this.callerLocation = callerLocation;
    }

    public String getCalleeLocation() {
        return calleeLocation;
    }

    public void setCalleeLocation(String calleeLocation) {
        this.calleeLocation = calleeLocation;
    }

    public String getFullCalleeMethodName() {
        return this.calleeClass + "." + this.calleeMethod;
    }

}
