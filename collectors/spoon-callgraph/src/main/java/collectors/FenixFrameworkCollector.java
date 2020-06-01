package collectors;

import spoon.reflect.code.*;
import spoon.reflect.cu.SourcePosition;
import spoon.reflect.declaration.*;
import spoon.reflect.reference.CtTypeReference;
import spoon.support.reflect.code.CtInvocationImpl;
import spoon.support.reflect.code.CtReturnImpl;
import util.Constants;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Stack;

public class FenixFrameworkCollector extends SpoonCollector {

    public FenixFrameworkCollector(int launcherChoice, String repoName, String projectPath, boolean collectionFlag)
            throws IOException {
        super(launcherChoice, repoName, projectPath, collectionFlag);

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
                controllers.add((CtClass) clazz);
            } else {  //Domain class
                CtTypeReference<?> superclassReference = clazz.getSuperclass();
                if (superclassReference != null && superclassReference.getSimpleName().endsWith("_Base")) {
                    allDomainEntities.add(clazz.getSimpleName());

                    if (collectionFlag)
                        collectBaseClassMethods((CtClass) clazz.getSuperclass().getTypeDeclaration());
                }

                if (collectionFlag) {
                    if (clazz.getSimpleName().equals("DomainRoot_Base")) {
                        collectBaseClassMethods((CtClass) clazz);
                    }
                }

            }
            allEntities.add(clazz.getSimpleName());
        }
    }

    @Override
    public void methodCallDFS(CtExecutable callerMethod, CtAbstractInvocation prevCalleeLocation, Stack<SourcePosition> methodStack) {
        methodStack.push(callerMethod.getPosition());

        if (!methodCallees.containsKey(callerMethod.getPosition())) {
            methodCallees.put(callerMethod.getPosition(), getCalleesOf(callerMethod));
        }

        for (CtAbstractInvocation calleeLocation : methodCallees.get(callerMethod.getPosition())) {

            try {
                if (calleeLocation == null)
                    continue;
                else if (calleeLocation.getExecutable().getDeclaringType().getSimpleName().endsWith("_Base")) {
                    registerBaseClass(calleeLocation.getExecutable().getExecutableDeclaration(), calleeLocation);
                }
                else if (calleeLocation.getExecutable().getDeclaringType().getSimpleName().equals("FenixFramework") &&
                        calleeLocation.getExecutable().getSimpleName().equals("getDomainObject")) {
                    registerDomainObject(calleeLocation);
                }
                else if (allEntities.contains(calleeLocation.getExecutable().getDeclaringType().getSimpleName())) {
                    if (!methodStack.contains(calleeLocation.getExecutable().getExecutableDeclaration().getPosition())) {
                        methodCallDFS(calleeLocation.getExecutable().getExecutableDeclaration(), null, methodStack);
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
        else
            throw new Exception("Couldn't Resolve Type.");
    }

    private void collectBaseClassMethods(CtClass clazz) {
        Set<CtMethod> baseMethods = clazz.getMethods();
        for (CtMethod ctM : baseMethods) {
            if (ctM.isPublic()) {
                String returnType = ctM.getType().toString();
                if (returnType.contains("pt.ist.fenixframework")) {
                    continue;
                }

                List<CtParameter> parameters = ctM.getParameters();
                boolean skip = false;
                for (CtParameter p : parameters) {
                    if (p.getType().toString().contains("pt.ist.fenixframework")) {
                        skip = true;
                        break;
                    }
                }

                if (skip)
                    continue;

                else {
                    List<String> par = new ArrayList<>();
                    CtTypeReference typeReference = ctM.getType();
                    String returnTypeParsed;
                    if (typeReference.isParameterized())
                        returnTypeParsed = typeReference.getActualTypeArguments().get(0).getSimpleName();
                    else
                        returnTypeParsed = typeReference.getSimpleName();

                    parameters.forEach(parameter -> par.add(parameter.getType().getSimpleName()));
                    methodsListCollection.put(
                            String.join(
                                    ".",
                                    clazz.getSimpleName(),
                                    ctM.getSimpleName()
                            ),
                            new MethodContainer(
                                    returnTypeParsed,
                                    ctM.getSimpleName(),
                                    par,
                                    clazz.getSimpleName()
                            )
                    );
                }
            }
        }
    }
}
