package pt.ist.socialsoftware.cml.converter.strategies.naming;

import pt.ist.socialsoftware.cml.converter.domain.Functionality;

import java.util.HashMap;
import java.util.Map;

public class AnonymousStepsOperationNamingStrategy implements OperationNamingStrategy {

    private Functionality functionality;
    private static final Map<String, Integer> functionalitySteps = new HashMap<>();

    public AnonymousStepsOperationNamingStrategy(Functionality functionality) {
        this.functionality = functionality;
    }

    @Override
    public String createOperationName() {
        String functionalityName = functionality.getSimpleLowerCaseName();

        if (functionalitySteps.containsKey(functionalityName)) {
            functionalitySteps.replace(functionalityName, functionalitySteps.get(functionalityName) + 1);
        } else {
            functionalitySteps.put(functionalityName, 0);
        }
        return functionalityName + "_step" + functionalitySteps.get(functionalityName);
    }
}
