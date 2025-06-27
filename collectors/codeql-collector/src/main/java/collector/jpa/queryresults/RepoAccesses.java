package collector.jpa.queryresults;

public class RepoAccesses {

    private String functionId;
    private String className;
    private String methodName;
    private String entityLocation;
    private boolean isDeclared;
    private String annotation;
    private boolean isNative;
    private String queryName;
    private String callLocation;

    public RepoAccesses(String functionId, String className, String methodName, String entityLocation, boolean isDeclared, String annotation, boolean isNative, String queryName, String callLocation) {
        this.functionId = functionId;
        this.className = className;
        this.methodName = methodName;
        this.entityLocation = entityLocation;
        this.isDeclared = isDeclared;
        this.annotation = annotation;
        this.isNative = isNative;
        this.queryName = queryName;
        this.callLocation = callLocation;
    }

    public String getFunctionId() {
        return functionId;
    }

    public void setFunctionId(String functionId) {
        this.functionId = functionId;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getEntityLocation() {
        return entityLocation;
    }

    public void setEntityLocation(String entityLocation) {
        this.entityLocation = entityLocation;
    }

    public boolean isDeclared() {
        return isDeclared;
    }

    public void setDeclared(boolean declared) {
        isDeclared = declared;
    }

    public String getAnnotation() {
        return annotation;
    }

    public void setAnnotation(String annotation) {
        this.annotation = annotation;
    }

    public boolean isNative() {
        return isNative;
    }

    public void setNative(boolean aNative) {
        isNative = aNative;
    }

    public String getQueryName() {
        return queryName;
    }

    public void setQueryName(String queryName) {
        this.queryName = queryName;
    }

    public String getCallLocation() {
        return callLocation;
    }

    public void setCallLocation(String callLocation) {
        this.callLocation = callLocation;
    }
}
