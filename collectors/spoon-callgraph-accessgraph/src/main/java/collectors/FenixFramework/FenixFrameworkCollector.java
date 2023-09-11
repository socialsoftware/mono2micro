package collectors.FenixFramework;

import collectors.SpoonCollector;
import spoon.reflect.code.*;
import spoon.reflect.cu.SourcePosition;
import spoon.reflect.declaration.*;
import spoon.reflect.path.CtRole;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.CtScanner;
import spoon.support.reflect.code.CtInvocationImpl;
import spoon.support.reflect.code.CtReturnImpl;
import spoon.support.reflect.code.CtDoImpl;
import spoon.support.reflect.code.CtForEachImpl;
import spoon.support.reflect.code.CtForImpl;
import spoon.support.reflect.code.CtIfImpl;
import spoon.support.reflect.code.CtLoopImpl;
import spoon.support.reflect.code.CtWhileImpl;
import util.Constants;
import util.property_scanner.PropertyScanner;
import util.UnkownMethodException;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.Arrays;

public class FenixFrameworkCollector extends SpoonCollector {
    
    private List<PropertyScanner> propertyScanners;

    public FenixFrameworkCollector(int launcherChoice, String repoName, String projectPath)
            throws IOException {
        super(launcherChoice, repoName, projectPath);

        switch (launcherChoice) {
            case Constants.LAUNCHER:
            case Constants.MAVEN_LAUNCHER:
            case Constants.JAR_LAUNCHER:
                launcher.getEnvironment().setSourceClasspath(new String[]{
                        "./lib/fenix-framework-core-2.0.jar",
                        "./lib/spring-context-5.2.3.RELEASE.jar",
                        "./lib/bennu-core-6.6.0.jar"}
                );
                break;
            default:
                System.exit(1);
                break;
        }

        // instantiate property scanners
        String[] pScanners = new String[]{
            "util.property_scanner.ReturnPropertyScanner",
            "util.property_scanner.ContinuePropertyScanner",
            "util.property_scanner.BreakPropertyScanner",
            "util.property_scanner.ZeroComparisonPropertyScanner"
        };
        
        propertyScanners = new ArrayList<PropertyScanner>();
        
        PropertyScanner scannerBuffer;
        for (String scanner : pScanners) {
            try {
                scannerBuffer = (PropertyScanner)(Class.forName(scanner).getDeclaredConstructor().newInstance());
                propertyScanners.add(scannerBuffer);

            } catch (IllegalAccessException | InstantiationException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException e) {
                e.printStackTrace();
                System.out.println("Invalid Scanner: " + scanner);
                System.exit(-1);
            }

        }


        System.out.println("Generating AST...");
        launcher.buildModel();
    }

    @Override
    public void collectControllersAndEntities() {
        for(CtType<?> clazz : factory.Class().getAll()) {
            List<CtAnnotation<? extends Annotation>> annotations = clazz.getAnnotations();

            if (existsAnnotation(annotations, "Controller") ||
                existsAnnotation(annotations, "RestController") ||
                existsAnnotation(annotations, "RequestMapping") ||
                existsAnnotation(annotations, "GetMapping") ||
                existsAnnotation(annotations, "PostMapping") ||
                existsAnnotation(annotations, "PatchMapping") ||
                existsAnnotation(annotations, "PutMapping") ||
                existsAnnotation(annotations, "DeleteMapping") ||
                clazz.getSimpleName().endsWith("Controller") ||
                (clazz.getSuperclass() != null && clazz.getSuperclass().getSimpleName().equals("FenixDispatchAction"))
            ) {
                if (clazz instanceof CtClass) {
                    controllers.add((CtClass) clazz);
                }
            } else {
                CtTypeReference<?> superclassReference = clazz.getSuperclass();
                if (superclassReference != null && superclassReference.getSimpleName().endsWith("_Base")) {
                    allDomainEntities.add(clazz.getSimpleName());
                }
                if (!clazz.getSimpleName().endsWith("_Base") && (clazz.isAbstract() || clazz.isInterface()))
                    abstractClassesAndInterfacesImplementorsMap.put(clazz.getSimpleName(), new ArrayList<>());
            }
            allEntities.add(clazz.getSimpleName());
        }

        for (String e : allDomainEntities)
            entitiesMap.put(e, entitiesMap.size() + 1);

        // 2nd iteration, to retrieve interface and abstract classes implementors and subclasses
        for(CtType<?> clazz : factory.Class().getAll()) {
            Set<CtTypeReference<?>> superInterfaces = clazz.getSuperInterfaces();
            for (CtTypeReference ctTypeReference : superInterfaces) {
                List<CtType> ctTypes = abstractClassesAndInterfacesImplementorsMap.get(ctTypeReference.getSimpleName());
                if (ctTypes != null) { // key exists
                    ctTypes.add(clazz);
                }
            }

            if (clazz.getSuperclass() != null) {
                // two times because, the first time we get the Base class and then the Domain Class
                CtTypeReference<?> superclass2 = clazz.getSuperclass().getSuperclass();
                if (superclass2 != null) {
                    List<CtType> ctTypes = abstractClassesAndInterfacesImplementorsMap.get(superclass2.getSimpleName());
                    if (ctTypes != null) // key exists
                        ctTypes.add(clazz);
                }
            }
        }
    }

    @Override
    public void methodCallDFS(CtExecutable callerMethod, CtAbstractInvocation prevCalleeLocation, Stack<SourcePosition> methodStack) {
        methodStack.push(callerMethod.getPosition());

        callerMethod.accept(new CtScanner() {
            private <T> void visitCtAbstractInvocation(CtAbstractInvocation calleeLocation) {
                try {
                    if (calleeLocation == null || calleeLocation.getExecutable().getDeclaringType() == null)
                        return;

                    try {
                        CtExecutable executableDeclaration = calleeLocation.getExecutable().getExecutableDeclaration();
                        if (executableDeclaration instanceof CtMethod) {
                            CtMethod eDM = (CtMethod) executableDeclaration;
                            if (eDM.isAbstract()) {
                                // call to an abstract method
                                // lets replace this call with a call to every possible implementation of this
                                List<CtMethod> explicitImplementationsOfAbstractMethod = getExplicitImplementationsOfAbstractMethod(eDM);
                                for (CtMethod ctMethod : explicitImplementationsOfAbstractMethod) {
                                    if (!methodStack.contains(ctMethod.getPosition())) {
                                        methodCallDFS(ctMethod, calleeLocation, methodStack);
                                    }
                                }
                                return;
                            }
                        }
                    } catch (UnkownMethodException e) {
                        return;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (calleeLocation.getExecutable().getDeclaringType().getSimpleName().endsWith("_Base")) {
                        registerBaseClass(calleeLocation.getExecutable().getExecutableDeclaration(), calleeLocation);
                    } else if (calleeLocation.getExecutable().getDeclaringType().getSimpleName().equals("FenixFramework") &&
                            calleeLocation.getExecutable().getSimpleName().equals("getDomainObject")) {
                        registerDomainObject(calleeLocation);
                    } else if (allEntities.contains(calleeLocation.getExecutable().getDeclaringType().getSimpleName())) {
                        CtExecutable executableDeclaration = calleeLocation.getExecutable().getExecutableDeclaration();
                        if (executableDeclaration != null && !methodStack.contains(executableDeclaration.getPosition())) {
                            methodCallDFS(calleeLocation.getExecutable().getExecutableDeclaration(), calleeLocation, methodStack);
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public <T> void visitCtMethod(CtMethod<T> m) {
                super.visitCtMethod(m);
                /* When inpecting calls, if we see an abstract method we redirect to the implementor method
                 * However, it is possible that the implementor is abstract as well. This block of code allows to redirect
                 * until we find a non-abstract implementor */
                if (m.isAbstract()) {
                    // call to an abstract method
                    // lets replace this call with a call to every possible implementation of this
                    try {
                        List<CtMethod> explicitImplementationsOfAbstractMethod = getExplicitImplementationsOfAbstractMethod(m);
                        for (CtMethod ctMethod : explicitImplementationsOfAbstractMethod) {
                            if (!methodStack.contains(ctMethod.getPosition())) {
                                methodCallDFS(ctMethod, prevCalleeLocation, methodStack);
                            }
                        }
                    } catch (UnkownMethodException ignored) {}
                }
            }

            @Override
            public <T> void visitCtInvocation(CtInvocation<T> invocation) {
                super.visitCtInvocation(invocation);
                visitCtAbstractInvocation(invocation);
            }

            @Override
            public <T> void visitCtConstructorCall(CtConstructorCall<T> ctConstructorCall) {
                super.visitCtConstructorCall(ctConstructorCall);
                visitCtAbstractInvocation(ctConstructorCall);
            }

            @Override
            public void scan(CtRole role, CtElement element) {
                CtElement elParent = null;
                if(element != null) {
                    try {
                        elParent = element.getParent();
                    } catch (ParentNotInitializedException e) {
                        elParent = null;
                    }
                }

                List<CtRole> loopRoles = Arrays.asList(
                        CtRole.EXPRESSION,
                        CtRole.BODY,
                        CtRole.FOR_INIT,
                        CtRole.FOR_UPDATE
                    );
                List<CtRole> ifRoles = Arrays.asList(
                        CtRole.CONDITION,
                        CtRole.THEN,
                        CtRole.ELSE
                    );

                boolean roleOpened = false;
                if( (elParent instanceof CtIfImpl && ifRoles.contains(role)) ||
                    (elParent instanceof CtLoopImpl && loopRoles.contains(role))
                    ) {
                    openNewContext(role.toString());
                    roleOpened = true;

                }

                if(element instanceof CtIfImpl) { // IF
                    openNewContext("if");
                    super.scan(element);
                    closeCurrentContext();

                } else if(element instanceof CtLoopImpl) { // LOOP
                    String loopType = "loop";

                    if(element instanceof CtDoImpl)
                        loopType = "do_loop";
                    else if(element instanceof CtWhileImpl)
                        loopType = "while_loop";
                    else if(element instanceof CtForImpl)
                        loopType = "for_loop";
                    else if(element instanceof CtForEachImpl)
                        loopType = "foreach_loop";

                    openNewContext(loopType);
                    super.scan(element);
                    closeCurrentContext();

                } else if(element instanceof CtInvocationImpl) {
                    openNewContext("call");
                    super.scan(element);
                    closeCurrentContext();

                } else {
                    super.scan(element);
                }

                // read heuristic properties
                for (PropertyScanner scanner : propertyScanners) {
                    scanner.process(FenixFrameworkCollector.this, element);
                }

                if(roleOpened) closeCurrentContext();

            }
        });

        methodStack.pop();
    }

    private void registerBaseClass(CtExecutable callee, CtAbstractInvocation calleeLocation) {
        if (callee == null) return;

        String methodName = callee.getSimpleName();
        String mode = "";
        String returnType = "";
        List<String> argTypes = new ArrayList<>();
        if (methodName.startsWith("get")) {
            mode = "R";
            CtTypeReference returnTypeReference = callee.getType();
            if (returnTypeReference.isParameterized())
                returnType = returnTypeReference.getActualTypeArguments().get(0).getSimpleName();
            else
                returnType = returnTypeReference.getSimpleName();
        }
        else if (methodName.startsWith("set") || methodName.startsWith("add") || methodName.startsWith("remove")) {
            mode = "W";
            List<CtParameter> calleeParameters = callee.getParameters();
            for (CtParameter ctP : calleeParameters) {
                CtTypeReference parameterType = ctP.getType();
                if (parameterType.isParameterized()) {
                    for (CtTypeReference ctTR : parameterType.getActualTypeArguments())
                        argTypes.add(ctTR.getSimpleName());
                }
                else
                    argTypes.add(ctP.getType().getSimpleName());
            }
        }
        else // super()
            return;

        String baseClassName = callee.getParent(CtClass.class).getSimpleName();
        baseClassName = baseClassName.substring(0, baseClassName.length()-5); //remove _Base

        String resolvedType = " ";
        resolvedType = resolveTypeBase(calleeLocation);

        if (mode.equals("R")) {
            // Class Read
            if (allDomainEntities.contains(resolvedType))
                addEntitiesSequenceAccess(resolvedType, mode);
            else if (allDomainEntities.contains(baseClassName))
                addEntitiesSequenceAccess(baseClassName, mode);

            // Return Type Read
            if (allDomainEntities.contains(returnType))
                addEntitiesSequenceAccess(returnType, mode);
        }
        else if (mode.equals("W")) {
            // Class Read
            if (allDomainEntities.contains(resolvedType))
                addEntitiesSequenceAccess(resolvedType, mode);
            else if (allDomainEntities.contains(baseClassName))
                addEntitiesSequenceAccess(baseClassName, mode);

            // Argument Types Read
            for (String type : argTypes) {
                if (allDomainEntities.contains(type))
                    addEntitiesSequenceAccess(type, mode);
            }
        }
    }

    private String resolveTypeBase(CtAbstractInvocation calleeLocation) {
        CtExpression target = ((CtInvocationImpl) calleeLocation).getTarget();
        if (target.getTypeCasts().size() > 0)
            return ((CtTypeReference) target.getTypeCasts().get(0)).getSimpleName();
        else
            return target.getType().getSimpleName();
    }

    private void registerDomainObject(CtAbstractInvocation abstractCalleeLocation) throws Exception {
        // Example: final CurricularCourse course = FenixFramework.getDomainObject(values[0]);
        // Example: return (DepartmentUnit) FenixFramework.getDomainObject(this.getSelectedDepartmentUnitID());
        CtInvocation calleeLocation = (CtInvocation) abstractCalleeLocation;
        String resolvedType;
        resolvedType = resolveTypeDomainObject(calleeLocation);
        if (resolvedType == null) {
            System.err.println("getDomainObject - Couldn't resolve type!");
            System.err.println(abstractCalleeLocation);
            return;
        }

        if (allDomainEntities.contains(resolvedType)) {
            addEntitiesSequenceAccess(resolvedType, "R");
        }
    }

    private String resolveTypeDomainObject(CtInvocation calleeLocation) throws Exception {
        CtElement parent = calleeLocation.getParent();
        if (calleeLocation.getTypeCasts().size() > 0)
            return ((CtTypeReference) calleeLocation.getTypeCasts().get(0)).getSimpleName();
        else if (parent instanceof CtReturnImpl)
            return parent.getParent(CtMethod.class).getType().getSimpleName();
        else if (parent instanceof CtLocalVariable)
            return ((CtLocalVariable) parent).getType().getSimpleName();
        else if (parent instanceof CtAssignment)
            return ((CtAssignment) parent).getAssigned().getType().getSimpleName();
        else if (!calleeLocation.getType().getSimpleName().equals("DomainObject"))
            return calleeLocation.getType().getSimpleName();
        else
            return null;
    }
}
