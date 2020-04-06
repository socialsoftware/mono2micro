package collectors;

import spoon.reflect.code.*;
import spoon.reflect.declaration.*;
import spoon.reflect.reference.CtTypeReference;
import spoon.support.reflect.code.CtInvocationImpl;
import spoon.support.reflect.code.CtReturnImpl;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Stack;

public class FenixFrameworkCollector extends SpoonCollector {
    public FenixFrameworkCollector(String projectPath, String repoName) {
        super(projectPath, repoName);
        launcher.getEnvironment().setSourceClasspath(new String[]{
                "./lib/fenix-framework-core-2.0.jar",
                "./lib/spring-context-5.2.3.RELEASE.jar"}
        );
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
                controllers.add((CtClass) clazz);
                allEntities.add(clazz.getSimpleName());
            } else if (!clazz.getSimpleName().endsWith("_Base")) {  //Domain class
                allEntities.add(clazz.getSimpleName());
            }
        }

        allEntities.sort((string1, string2) -> Integer.compare(string2.length(), string1.length()));  //Longer length first
    }


    @Override
    public void methodCallDFS(CtExecutable callerMethod, CtAbstractInvocation prevCalleeLocation, Stack<String> methodStack) {
        methodStack.push(callerMethod.toString());

        if (!methodCallees.containsKey(callerMethod.toString())) {
            methodCallees.put(callerMethod.toString(), getCalleesOf(callerMethod));
        }

        for (CtAbstractInvocation calleeLocation : methodCallees.get(callerMethod.toString())) {

            try {
                if (calleeLocation == null)
                    continue;
                else if (calleeLocation.getExecutable().getDeclaringType().getSimpleName().endsWith("_Base")) {
                    registerBaseClass(calleeLocation.getExecutable().getExecutableDeclaration(), calleeLocation);
                }
                else if (calleeLocation.getExecutable().getDeclaringType().getSimpleName().equals("FenixFramework") &&
                        calleeLocation.getExecutable().getSimpleName().equals("getDomainObject")) {
                    registerDomainObject(calleeLocation);
                } else if (allEntities.contains(calleeLocation.getExecutable().getDeclaringType().getSimpleName())) {
                    if (!methodStack.contains(calleeLocation.getExecutable().getExecutableDeclaration().toString())) {
                        methodCallDFS(calleeLocation.getExecutable().getExecutableDeclaration(), calleeLocation, methodStack);
                    }
                }
            } catch (Exception e) {
                // cast error, proceed
            }
        }
        methodStack.pop();
    }

    private void registerBaseClass(CtExecutable callee, CtAbstractInvocation calleeLocation) {
        String methodName = callee.getSimpleName();
        String mode = "";
        String returnType = "";
        String argType = "";
        if (methodName.startsWith("get")) {
            mode = "R";
            returnType = callee.getType().toString();
        } else if (methodName.startsWith("set") || methodName.startsWith("add") || methodName.startsWith("remove")) {
            mode = "W";
            CtParameter parameter = (CtParameter) callee.getParameters().get(0);
            argType = parameter.getType().toString();
        }

        String baseClassName = callee.getParent(CtClass.class).getSimpleName();
        baseClassName = baseClassName.substring(0,baseClassName.length()-5); //remove _Base

        String resolvedType = " ";
        //resolvedType[0] = String.valueOf(calleeLocation.getExecutable().getDeclaringType());

        resolvedType = resolveTypeBase(calleeLocation);
        // verificar! O que é o resolveTypeBinding() ?? Está a dar retorno diferente

        // String a = virtualEdition.getAcronym()
        // Neste caso getAcronym está definido na superclasse Edition, portanto eu iria obter uma leitura de Edition
        // em vez de VirtualEdition se não fizesse o resolveBinding

        if (mode.equals("R")) {
            if (allEntities.contains(resolvedType)) {
                addEntitiesSequenceAccess(resolvedType, mode);
            } else if (allEntities.contains(baseClassName)) {
                addEntitiesSequenceAccess(baseClassName, mode);
            }

            for (String entityName : allEntities) {
                if (returnType.contains(entityName)) {
                    addEntitiesSequenceAccess(entityName, mode);
                    return;
                }
            }

        } else if (mode.equals("W")) {
            if (allEntities.contains(resolvedType)) {
                addEntitiesSequenceAccess(resolvedType, mode);
            } else if (allEntities.contains(baseClassName)) {
                addEntitiesSequenceAccess(baseClassName, mode);
            }

            for (String entityName : allEntities) {
                if (argType.contains(entityName)) {
                    addEntitiesSequenceAccess(entityName, mode);
                    return;
                }
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

        if (allEntities.contains(resolvedType)) {
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
        else
            throw new Exception("Couldn't Resolve Type.");
    }
}
