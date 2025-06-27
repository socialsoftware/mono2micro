package collector.jpa.queryresults;

public class EntityAttributes {

    private String entityName;
    private boolean mappedSuperclass;
    private String tableName;
    private String entityLocation;

    public EntityAttributes() {
    }

    public EntityAttributes(String entityName, boolean mappedSuperclass, String tableName, String entityLocation) {
        this.entityName = entityName;
        this.mappedSuperclass = mappedSuperclass;
        this.tableName = tableName;
        this.entityLocation = entityLocation;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public boolean getMappedSuperclass() {
        return mappedSuperclass;
    }

    public void setMappedSuperclass(boolean mappedSuperclass) {
        this.mappedSuperclass = mappedSuperclass;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getEntityLocation() {
        return entityLocation;
    }

    public void setEntityLocation(String entityLocation) {
        this.entityLocation = entityLocation;
    }
}
