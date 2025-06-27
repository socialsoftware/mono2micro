package collector.queryresults;

public class EntityFields {

    String entityName;
    String entityLocation;
    String fieldName;
    String fieldType;

    public EntityFields() {
    }

    public EntityFields(String entityName, String entityLocation, String fieldName, String fieldType) {
        this.entityName = entityName;
        this.entityLocation = entityLocation;
        this.fieldName = fieldName;
        this.fieldType = fieldType;
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

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldType() {
        return fieldType;
    }

    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }
}
