package collectors;

public class Entity {
    private String className;
    private String entityName;
    private String tableName;

    public Entity(String className, String entityName, String tableName) {
        this.className = className;
        this.entityName = entityName;
        this.tableName = tableName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
}
