package collector.jpa.queryresults;

public class FieldAnnotations {

    private String entityLocation;
    private String declaringType;
    private String type;
    private String joinTable;

    public FieldAnnotations(String entityLocation, String declaringType, String type, String joinTable) {
        this.entityLocation = entityLocation;
        this.declaringType = declaringType;
        this.type = type;
        this.joinTable = joinTable;
    }

    public String getDeclaringType() {
        return declaringType;
    }

    public void setDeclaringType(String declaringType) {
        this.declaringType = declaringType;
    }

    public String getEntityLocation() {
        return entityLocation;
    }

    public void setEntityLocation(String entityLocation) {
        this.entityLocation = entityLocation;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getJoinTable() {
        return joinTable;
    }

    public void setJoinTable(String joinTable) {
        this.joinTable = joinTable;
    }
}
