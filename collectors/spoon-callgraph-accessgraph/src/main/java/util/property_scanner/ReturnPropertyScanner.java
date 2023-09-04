package collectors.util.property_scanner;

import collectors.SpoonCollector;
import spoon.reflect.code.CtReturn;
import spoon.reflect.declaration.CtElement;

public class ReturnPropertyScanner extends PropertyScanner {
    public void process(SpoonCollector collector, CtElement element) {
        if(element instanceof CtReturn) {
            collector.addLabel("return");
        }
    }
}
