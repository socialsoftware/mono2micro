package collector.results;

public class MethodAccesses {

    private String targetClass;
    private String targetMethod;
    private String entity;
    private String operation;
    private String callLocation;

    public MethodAccesses(String targetClass, String targetMethod, String entity, String operation, String callLocation) {
        this.targetClass = targetClass;
        this.targetMethod = targetMethod;
        this.entity = entity;
        this.operation = operation;
        this.callLocation = callLocation;
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

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getCallLocation() {
        return callLocation;
    }

    public void setCallLocation(String callLocation) {
        this.callLocation = callLocation;
    }

    public String getFullName() {
        return this.targetClass + "." + this.targetMethod;
    }

}
