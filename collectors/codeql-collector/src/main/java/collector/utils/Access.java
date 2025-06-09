package collector.utils;

public class Access {

    private DomainEntity entity; // Entity accessed
    private Method targetMethod; // Where the access was made
    private String mode; // Write or Read

    public Access() {
    }

    public Access(DomainEntity entity, Method targetMethod, String mode) {
        this.entity = entity;
        this.targetMethod = targetMethod;
        this.mode = mode;
    }

    public DomainEntity getEntity() {
        return entity;
    }

    public void setEntity(DomainEntity entity) {
        this.entity = entity;
    }

    public Method getTargetMethod() {
        return targetMethod;
    }

    public void setTargetMethod(Method targetMethod) {
        this.targetMethod = targetMethod;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }
}
