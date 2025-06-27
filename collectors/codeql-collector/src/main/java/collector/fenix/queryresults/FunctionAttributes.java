package collector.fenix.queryresults;

public class FunctionAttributes {

    private String functionId;
    private String methodDeclaringType;
    private String methodName;
    private String returningType;
    private String paramType;

    public FunctionAttributes() {
    }

    public FunctionAttributes(String functionId, String methodDeclaringType, String methodName, String returningType, String paramType) {
        this.functionId = functionId;
        this.methodDeclaringType = methodDeclaringType;
        this.methodName = methodName;
        this.returningType = returningType;
        this.paramType = paramType;
    }

    public String getFunctionId() {
        return functionId;
    }

    public void setFunctionId(String functionId) {
        this.functionId = functionId;
    }

    public String getMethodDeclaringType() {
        return methodDeclaringType;
    }

    public void setMethodDeclaringType(String methodDeclaringType) {
        this.methodDeclaringType = methodDeclaringType;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getReturningType() {
        return returningType;
    }

    public void setReturningType(String returningType) {
        this.returningType = returningType;
    }

    public String getParamType() {
        return paramType;
    }

    public void setParamType(String paramType) {
        this.paramType = paramType;
    }
}
