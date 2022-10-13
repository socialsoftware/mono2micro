package pt.ist.socialsoftware.mono2micro.fileManager;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;


//FIXME This class should be avoided, use @Autowire whenever possible!
public class ContextManager {
    private static ContextManager contextManager = null;

    private final AnnotationConfigApplicationContext context;

    public ContextManager() {
        context = new AnnotationConfigApplicationContext();
        context.scan("pt.ist.socialsoftware.mono2micro");
        context.refresh();
    }

    public static ContextManager get() {
        if (contextManager == null) {
            contextManager = new ContextManager();
            return contextManager;
        }
        return contextManager;
    }

    public <T> T getBean(Class<T> beanClass) {
        return context.getBean(beanClass);
    }
}
