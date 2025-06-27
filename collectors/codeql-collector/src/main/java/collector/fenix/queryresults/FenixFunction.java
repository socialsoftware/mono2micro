package collector.fenix.queryresults;

import collector.utils.Function;

import java.util.List;

public class FenixFunction {

    private Function function;
    private String methodClass;
    private String methodName;
    private String returnType;
    private List<String> params;

    public FenixFunction(Function function, String methodClass, String methodName, String returnType, List<String> params) {
        this.function = function;
        this.methodClass = methodClass;
        this.methodName = methodName;
        this.returnType = returnType;
        this.params = params;
    }

    public Function getFunction() {
        return function;
    }

    public void setFunction(Function function) {
        this.function = function;
    }

    public String getMethodClass() {
        return methodClass;
    }

    public void setMethodClass(String methodClass) {
        this.methodClass = methodClass;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public List<String> getParams() {
        return params;
    }

    public void setParams(List<String> params) {
        this.params = params;
    }
}
