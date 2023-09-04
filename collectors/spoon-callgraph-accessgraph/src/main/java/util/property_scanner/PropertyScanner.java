package collectors.util.property_scanner;

import collectors.SpoonCollector;
import spoon.reflect.declaration.CtElement;

public abstract class PropertyScanner {
    public abstract void process(SpoonCollector collector, CtElement element);
}
