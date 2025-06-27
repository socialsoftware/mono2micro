package collector.queryresults;

public class Endpoints {

    private String functionFullName;
    private String functionId;

    public Endpoints() {
    }

    public Endpoints(String functionFullName, String functionId) {
        this.functionFullName = functionFullName;
        this.functionId = functionId;
    }

    public String getFunctionFullName() {
        return functionFullName;
    }

    public void setFunctionFullName(String functionFullName) {
        this.functionFullName = functionFullName;
    }

    public String getFunctionId() {
        return functionId;
    }

    public void setFunctionId(String functionId) {
        this.functionId = functionId;
    }
}
