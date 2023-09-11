package util.property_scanner;

import collectors.SpoonCollector;
import spoon.reflect.code.CtContinue;
import spoon.reflect.declaration.CtElement;

public class ContinuePropertyScanner extends PropertyScanner {
    public void process(SpoonCollector collector, CtElement element) {
        if(element instanceof CtContinue) {
            collector.addLabel("continue");
        }
    }
}
