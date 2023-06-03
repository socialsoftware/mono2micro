package collectors.JPA.property_scanner;

import collectors.SpoonCollector;
import spoon.reflect.code.CtBreak;
import spoon.reflect.declaration.CtElement;
import spoon.support.reflect.code.CtLoopImpl;
import spoon.support.reflect.code.CtSwitchImpl;

public class BreakPropertyScanner extends PropertyScanner {
    public void process(SpoonCollector collector, CtElement element) {
        if(element instanceof CtBreak) {
            CtLoopImpl loopParent = element.getParent(CtLoopImpl.class);
            CtSwitchImpl switchParent = element.getParent(CtSwitchImpl.class);

            // breaks can also appear on switch statements

            // only register break if loopParent is the nearest parent
            if(loopParent != null && (switchParent == null || loopParent.getParent(CtSwitchImpl.class) == switchParent))
                collector.addLabel("break");
        }
    }
}
