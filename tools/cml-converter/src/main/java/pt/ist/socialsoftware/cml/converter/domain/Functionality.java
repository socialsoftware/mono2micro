package pt.ist.socialsoftware.cml.converter.domain;

import java.util.ArrayList;
import java.util.List;

public class Functionality {
    private String name;
    private String orchestrator;
    private List<FunctionalityStep> steps;

    public Functionality() {
        this.steps = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public String getSimpleName() {
        String simpleName = getName().split("\\.")[1];
        return simpleName.substring(0, 1).toUpperCase() +
                simpleName.substring(1);
    }

    public String getSimpleLowerCaseName() {
        return getName().split("\\.")[1];
    }

    public String getOrchestrator() {
        return orchestrator;
    }

    public List<FunctionalityStep> getSteps() {
        return steps;
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Functionality && getName().equals(((Functionality) obj).getName());
    }
}
