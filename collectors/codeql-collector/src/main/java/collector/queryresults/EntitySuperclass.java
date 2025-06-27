package collector.queryresults;

public class EntitySuperclass {

    private String entityName;
    private String entityLocation;
    private String superclass;

    public EntitySuperclass() {
    }

    public EntitySuperclass(String entityName, String entityLocation, String superclass) {
        this.entityName = entityName;
        this.entityLocation = entityLocation;
        this.superclass = superclass;
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

    public String getSuperclass() {
        return superclass;
    }

    public void setSuperclass(String superclass) {
        this.superclass = superclass;
    }
}
