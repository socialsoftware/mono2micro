package processors;

import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.declaration.CtElement;

public class ProcessorUtils {

    public static boolean isAnnotatedWith(CtElement element, String annotationRegex) {
        for (CtAnnotation<?> a : element.getAnnotations()) {
            if (a.getAnnotationType().getSimpleName().matches(annotationRegex)) {
                return true;
            }
        }
        return false;
    }
}
