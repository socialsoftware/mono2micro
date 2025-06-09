package collector.results;

public class RepoAccesses {

    private String targetClass;
    private String targetMethod;
    private String entity;
    private boolean isDeclared;
    private String annotation;
    private boolean isNative;
    private String queryName;
    private String callLocation;

    public RepoAccesses(String targetClass, String targetMethod, String entity, boolean isDeclared, String annotation, boolean isNative, String queryName, String callLocation) {
        this.targetClass = targetClass;
        this.targetMethod = targetMethod;
        this.entity = entity;
        this.isDeclared = isDeclared;
        this.annotation = annotation;
        this.isNative = isNative;
        this.queryName = queryName;
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

    public String getFullName() {
        return this.targetClass + "." + this.targetMethod;
    }

}
