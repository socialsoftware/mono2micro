package parser;

public class HqlAccess {
    private String entityName;
    private String mode;

    public HqlAccess(String entityName, String mode) {
        this.entityName = entityName;
        this.mode = mode;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }
}
