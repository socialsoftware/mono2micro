package collector.utils;

public class Access {

    private DomainEntity entity; // Entity accessed
    private Function targetFunction; // Where the access was made
    private String mode; // Write or Read

    public Access() {
    }

    public Access(DomainEntity entity, Function targetFunction, String mode) {
        this.entity = entity;
        this.targetFunction = targetFunction;
        this.mode = mode;
    }

    public DomainEntity getEntity() {
        return entity;
    }

    public void setEntity(DomainEntity entity) {
        this.entity = entity;
    }

    public Function getTargetFunction() {
        return targetFunction;
    }

    public void setTargetFunction(Function targetFunction) {
        this.targetFunction = targetFunction;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }
}
