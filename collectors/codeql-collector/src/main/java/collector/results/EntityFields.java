package collector.results;

public class EntityFields {

    String entity;
    String field;
    String fieldType;
    String location;

    public EntityFields(String entity, String field, String fieldType, String location) {
        this.entity = entity;
        this.field = field;
        this.fieldType = fieldType;
        this.location = location;
    }

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getFieldType() {
        return fieldType;
    }

    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
