package collector.queryresults;

public class FunctionAccesses {

    private String functionId;
    private String entityName;
    private String entityLocation;
    private String operation;
    private String accessLocation;

    public FunctionAccesses() {
    }

    public FunctionAccesses(String functionId, String entityName, String entityLocation, String operation, String accessLocation) {
        this.functionId = functionId;
        this.entityName = entityName;
        this.entityLocation = entityLocation;
        this.operation = operation;
        this.accessLocation = accessLocation;
    }

    public String getFunctionId() {
        return functionId;
    }

    public void setFunctionId(String functionId) {
        this.functionId = functionId;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public String getEntityLocation() {
        return entityLocation;
    }

    public void setEntityLocation(String entityLocation) {
        this.entityLocation = entityLocation;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getAccessLocation() {
        return accessLocation;
    }

    public void setAccessLocation(String accessLocation) {
        this.accessLocation = accessLocation;
    }
}
