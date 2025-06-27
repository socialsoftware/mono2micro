package collector.utils;

public class Function {

    private String functionId;
    private String functionFullName;
    private String callLocation;

    public Function() {
    }

    public Function(String functionId) {
        this.functionId = functionId;
        this.functionFullName = "";
        this.callLocation = "";
    }

    public Function(String functionId, String callLocation) {
        this.functionId = functionId;
        this.functionFullName = "";
        this.callLocation = callLocation;
    }

    public Function(String functionId, String functionFullName, String callLocation) {
        this.functionId = functionId;
        this.functionFullName = functionFullName;
        this.callLocation = callLocation;
    }

    public String getFunctionId() {
        return functionId;
    }

    public void setFunctionId(String functionId) {
        this.functionId = functionId;
    }

    public String getFunctionFullName() {
        return functionFullName;
    }

    public void setFunctionFullName(String functionFullName) {
        this.functionFullName = functionFullName;
    }

    public String getCallLocation() {
        return callLocation;
    }

    public void setCallLocation(String callLocation) {
        this.callLocation = callLocation;
    }
}
