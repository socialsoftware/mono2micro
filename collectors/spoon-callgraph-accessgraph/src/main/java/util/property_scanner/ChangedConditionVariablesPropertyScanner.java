package util.property_scanner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import collectors.SpoonCollector;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.code.CtVariableWrite;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.path.CtRole;
import spoon.reflect.reference.CtVariableReference;
import spoon.reflect.visitor.Filter;
import spoon.support.reflect.code.CtIfImpl;
import spoon.support.reflect.code.CtLoopImpl;
import util.ContextStackListener;

public class ChangedConditionVariablesPropertyScanner extends PropertyScanner implements ContextStackListener {
    static HashMap<Integer, List<CtVariableReference<?>>> conditionVariablesByContext = null;
    static ChangedConditionVariablesPropertyScanner backgroundInstance = null;

    private boolean addedCollectorListener = false;

    public ChangedConditionVariablesPropertyScanner() {
        if (backgroundInstance == null)
            backgroundInstance = this;

        if (conditionVariablesByContext == null)
            conditionVariablesByContext = new HashMap<>();
    }

    public void process(SpoonCollector collector, CtElement element) {
        if (element == null) return;

        if (!backgroundInstance.addedCollectorListener) {
            collector.addControllerContextListener(backgroundInstance);
            backgroundInstance.addedCollectorListener = true;
        }

        // if condition or expression, store variable names && context id
        if (element instanceof CtBinaryOperator && (element.getParent() instanceof CtLoopImpl || element.getParent() instanceof CtIfImpl) && (element.getRoleInParent() == CtRole.EXPRESSION || element.getRoleInParent() == CtRole.CONDITION)) {
            conditionVariablesByContext.put(collector.getCurrentContextIndex(), getVariableAccesses(collector, element));
        } else if(  collector.getCurrentContextIndex() != -1
                    &&
                    (collector.getCurrentContextType().equals(CtRole.BODY.toString())
                    ||
                    collector.getCurrentContextType().equals(CtRole.THEN.toString())
                    ||
                    collector.getCurrentContextType().equals(CtRole.ELSE.toString()))
                    &&
                    collector.getContextStack().size() >= 2
                    &&
                    conditionVariablesByContext.get(collector.getContextStack().get(collector.getContextStack().size()-2)) != null // second to last element is the parent component (if or loop)
                    ) {

            // else if assigning value, add tag
            element.getElements(new Filter<CtElement>() {
                public boolean matches(CtElement element) {
                    return element instanceof CtVariableWrite;
                }
            }).stream().forEach(a -> {
                System.out.println(((CtVariableWrite<?>)a).getVariable());
                if (conditionVariablesByContext.get(collector.getContextStack().get(collector.getContextStack().size()-2)).contains(((CtVariableWrite<?>)a).getVariable())
                ) {
                    collector.addLabel("later_changed_c_variable");
                }
            });
        }

    }

    List<CtVariableReference<?>> getVariableAccesses(SpoonCollector collector, CtElement element) {
        List<CtVariableReference<?>> result = new ArrayList<>();
        result.addAll(element.getElements(new Filter<CtElement>() {
            public boolean matches(CtElement element) {
                return element instanceof CtVariableRead;
            }
        }).stream().map(a -> ((CtVariableRead<?>)a).getVariable()).collect(Collectors.toList()));
        
        return result;
    }

    public void contextAdded(int index) {}

    public void contextClosed(int index) {
        if (conditionVariablesByContext.containsKey(index))
            conditionVariablesByContext.remove(index);
    }
}