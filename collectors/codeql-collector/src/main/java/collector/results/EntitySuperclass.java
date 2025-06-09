package collector.results;

public class EntitySuperclass {

    private String entity;
    private String superclass;
    private boolean isMappedSuperclass;
    private String tableName;
    private String location;

    public EntitySuperclass(String entity, String superclass, boolean isMappedSuperclass, String tableName, String location) {
        this.entity = entity;
        this.superclass = superclass;
        this.isMappedSuperclass = isMappedSuperclass;
        this.tableName = tableName;
        this.location = location;
    }

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public String getSuperclass() {
        return superclass;
    }

    public void setSuperclass(String superclass) {
        this.superclass = superclass;
    }

    public boolean isMappedSuperclass() {
        return isMappedSuperclass;
    }

    public void setMappedSuperclass(boolean mappedSuperclass) {
        isMappedSuperclass = mappedSuperclass;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
