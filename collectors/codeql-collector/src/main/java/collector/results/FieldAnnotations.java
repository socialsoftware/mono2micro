package collector.results;

public class FieldAnnotations {

    private String declaringClass;
    private String type;
    private String joinTable;

    public FieldAnnotations(String declaringClass, String type, String joinTable) {
        this.declaringClass = declaringClass;
        this.type = type;
        this.joinTable = joinTable;
    }

    public String getDeclaringClass() {
        return declaringClass;
    }

    public void setDeclaringClass(String declaringClass) {
        this.declaringClass = declaringClass;
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
