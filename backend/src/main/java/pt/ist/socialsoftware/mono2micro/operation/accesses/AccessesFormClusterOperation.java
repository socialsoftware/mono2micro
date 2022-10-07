package pt.ist.socialsoftware.mono2micro.operation.accesses;

import pt.ist.socialsoftware.mono2micro.operation.FormClusterOperation;

import java.util.List;
import java.util.Map;

public class AccessesFormClusterOperation extends FormClusterOperation {
    public static final String ACCESSES_FORM = "AccessesForm";
    private Map<String, List<Short>> entities;

    public AccessesFormClusterOperation() {}

    @Override
    public String getOperationType() {
        return ACCESSES_FORM;
    }

    public Map<String, List<Short>> getEntities() {
        return entities;
    }

    public void setEntities(Map<String, List<Short>> entities) {
        this.entities = entities;
    }
}
