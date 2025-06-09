package collector.utils;

public class Method {

    private String className;
    private String methodName;
    private String methodLocation;
    private String callLocation;
    private String returnType;
    private String signature;

    public Method() {
    }

    public Method(String className, String methodName, String methodLocation) {
        this.className = className;
        this.methodName = methodName;
        this.methodLocation = methodLocation;
        this.callLocation = methodLocation;
        this.returnType = "";
        this.signature = "";
    }

    public Method(String className, String methodName, String methodLocation, String callLocation, String returnType, String signature) {
        this.className = className;
        this.methodName = methodName;
        this.methodLocation = methodLocation;
        this.callLocation = callLocation;
        this.returnType = returnType;
        this.signature = signature;
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

    public String getMethodLocation() {
        return methodLocation;
    }

    public void setMethodLocation(String methodLocation) {
        this.methodLocation = methodLocation;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getCallLocation() {
        return callLocation;
    }

    public void setCallLocation(String callLocation) {
        this.callLocation = callLocation;
    }

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getFullMethodName() {
        return this.className + "." + this.methodName;
    }

    public boolean methodEquals(Method method) {
        return this.getFullMethodName().equals(method.getFullMethodName());
    }

}
