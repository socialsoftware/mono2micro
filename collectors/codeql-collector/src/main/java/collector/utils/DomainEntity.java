package collector.utils;

public class DomainEntity {

    private int id;
    private String name;
    private boolean isMappedSuperclass;
    private String superclass;
    private String tableName;
    private String location;

    public DomainEntity() {
    }

    public DomainEntity(int id, String name, boolean isMappedSuperclass, String superclass, String tableName, String location) {
        this.id = id;
        this.isMappedSuperclass = isMappedSuperclass;
        this.name = name;
        this.superclass = superclass;
        this.tableName = tableName;
        this.location = location;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isMappedSuperclass() {
        return isMappedSuperclass;
    }

    public void setMappedSuperclass(boolean mappedSuperclass) {
        isMappedSuperclass = mappedSuperclass;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSuperclass() {
        return superclass;
    }

    public void setSuperclass(String superclass) {
        this.superclass = superclass;
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
