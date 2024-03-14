package pt.ist.socialsoftware.cml.converter.domain;

public class EntityAccess {
    private String type;
    private String entity;

    public EntityAccess() {
    }

    public String getType() {
        return type;
    }

    public String getEntity() {
        return entity;
    }

    public String getName() {
        return getParsedType() + getEntity();
    }

    public String getParsedType() {
        switch (type) {
            case "R":
                return "r";
            case "W":
                return "w";
            case "RW":
                return "rw";
            default:
                return "a";
        }
    }

    public boolean hasEntity(String entity) {
        return this.entity != null && this.entity.equals(entity);
    }
}
