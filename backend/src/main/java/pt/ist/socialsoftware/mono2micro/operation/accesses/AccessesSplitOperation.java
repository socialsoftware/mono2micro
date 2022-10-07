package pt.ist.socialsoftware.mono2micro.operation.accesses;

import pt.ist.socialsoftware.mono2micro.operation.SplitOperation;

public class AccessesSplitOperation extends SplitOperation {
    public static final String ACCESSES_SPLIT = "AccessesSplit";
    protected String entities;

    public AccessesSplitOperation() {}

    @Override
    public String getOperationType() {
        return ACCESSES_SPLIT;
    }

    public String getEntities() {
        return entities;
    }

    public void setEntities(String entities) {
        this.entities = entities;
    }
}
