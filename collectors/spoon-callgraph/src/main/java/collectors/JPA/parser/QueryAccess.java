package collectors.JPA.parser;

public class QueryAccess {
    private String name; // entityName (hql) or TableName (sql)
    private String mode;

    public QueryAccess(String name, String mode) {
        this.name = name;
        this.mode = mode;
    }

    public String getName() {
        return name;
    }

    public void setName(String entityName) {
        this.name = entityName;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }
}
