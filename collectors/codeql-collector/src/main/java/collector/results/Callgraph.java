package collector.results;

public class Callgraph {

    private String controllerClass;
    private String controllerMethod;
    private String targetClass;
    private String targetMethod;
    private String callLocation;

    public Callgraph(String controllerClass, String controllerMethod, String targetClass, String targetMethod, String callLocation) {
        this.controllerClass = controllerClass;
        this.controllerMethod = controllerMethod;
        this.targetClass = targetClass;
        this.targetMethod = targetMethod;
        this.callLocation = callLocation;
    }

    public String getControllerClass() {
        return controllerClass;
    }

    public void setControllerClass(String controllerClass) {
        this.controllerClass = controllerClass;
    }

    public String getControllerMethod() {
        return controllerMethod;
    }

    public void setControllerMethod(String controllerMethod) {
        this.controllerMethod = controllerMethod;
    }

    public String getTargetClass() {
        return targetClass;
    }

    public void setTargetClass(String targetClass) {
        this.targetClass = targetClass;
    }

    public String getTargetMethod() {
        return targetMethod;
    }

    public void setTargetMethod(String targetMethod) {
        this.targetMethod = targetMethod;
    }

    public String getCallLocation() {
        return callLocation;
    }

    public void setCallLocation(String callLocation) {
        this.callLocation = callLocation;
    }

    public String getFullControllerMethodName() {
        return this.controllerClass + "." + this.controllerMethod;
    }

    public String getFullTargetMethodName() {
        return this.targetClass + "." + this.targetMethod;
    }

}
