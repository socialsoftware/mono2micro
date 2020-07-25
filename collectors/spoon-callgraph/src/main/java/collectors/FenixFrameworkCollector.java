package collectors;

import spoon.reflect.code.*;
import spoon.reflect.cu.SourcePosition;
import spoon.reflect.declaration.*;
import spoon.reflect.path.CtRole;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.CtScanner;
import spoon.support.reflect.code.CtInvocationImpl;
import spoon.support.reflect.code.CtReturnImpl;
import util.Constants;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class FenixFrameworkCollector extends SpoonCollector {

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
                }
            }
            allEntities.add(clazz.getSimpleName());
        }
    }

    String lastMethodEndId;

    @Override
    public void methodCallDFS(
            CtExecutable callerMethod,
            CtAbstractInvocation prevCalleeLocation,
            Stack<SourcePosition> methodStack,
            Stack<String> nextNodeIdStack
    ) {
        methodStack.push(callerMethod.getPosition());

        callerMethod.accept(new CtScanner() {
            boolean lastStatementWasReturn = false;

            private <T> void visitCtAbstractInvocation(CtAbstractInvocation calleeLocation) {
                try {
                    if (calleeLocation == null)
                        return;
                    else if (calleeLocation.getExecutable().getDeclaringType().getSimpleName().endsWith("_Base")) {
                        registerBaseClass(calleeLocation.getExecutable().getExecutableDeclaration(), calleeLocation);
                    }
                    else if (calleeLocation.getExecutable().getDeclaringType().getSimpleName().equals("FenixFramework") &&
                            calleeLocation.getExecutable().getSimpleName().equals("getDomainObject")) {
                        registerDomainObject(calleeLocation);
                    }
                    else if (allEntities.contains(calleeLocation.getExecutable().getDeclaringType().getSimpleName())) {
                        if (!methodStack.contains(calleeLocation.getExecutable().getExecutableDeclaration().getPosition())) {
                            methodCallDFS(calleeLocation.getExecutable().getExecutableDeclaration(), calleeLocation, methodStack, nextNodeIdStack);
                        }
                    }
                } catch (Exception e) {
                    // cast error, proceed
                }
            }

            @Override
            public <S> void visitCtSwitch(CtSwitch<S> switchStatement) {
                super.visitCtSwitch(switchStatement);
            }

            @Override
            public <T, S> void visitCtSwitchExpression(CtSwitchExpression<T, S> switchExpression) {
                super.visitCtSwitchExpression(switchExpression);
            }

            @Override
            public <R> void visitCtReturn(CtReturn<R> returnStatement) {
                super.visitCtReturn(returnStatement);
                lastStatementWasReturn = true;
            }

            @Override
            public void visitCtIf(CtIf ifElement) {
                enter(ifElement);
                scan(CtRole.ANNOTATION, ifElement.getAnnotations());
                scan(CtRole.CONDITION, ifElement.getCondition());

                // Node after the 'then' and the 'else' branch
                String originNodeId = new String(currentParentNodeId);
                Node afterNode = createGraphNode(ifElement.toString() + "END");

                Node thenNode = createGraphNode(ifElement.getThenStatement().toString());
                currentParentNodeId = thenNode.getId();
                linkNodes(originNodeId, thenNode.getId());

                scan(CtRole.THEN, ((CtStatement) (ifElement.getThenStatement())));

                if (!lastStatementWasReturn)
                    linkNodes(thenNode.getId(), afterNode.getId());
                else {
                    // branch that ended in return
                    // must be linked to the end of the method execution
                    if (!nextNodeIdStack.isEmpty()) {
                        String peekId = nextNodeIdStack.peek();
                        linkNodes(thenNode.getId(), peekId);
                        lastStatementWasReturn = false;
                    }
                }

                Node elseNode = null;
                if (ifElement.getElseStatement() != null) {
                    elseNode = createGraphNode(ifElement.getElseStatement().toString());
                    currentParentNodeId = elseNode.getId();
                    linkNodes(originNodeId, elseNode.getId());
                }
                else {
                    // if there is no else, we can skip if and go to post-if
                    entitiesSequenceHashMap.get(originNodeId).addEdge(afterNode);
                }
                scan(CtRole.ELSE, ((CtStatement) (ifElement.getElseStatement())));
                if (elseNode != null) {
                    if (!lastStatementWasReturn)
                        linkNodes(elseNode.getId(), afterNode.getId());
                    else {
                        // branch that ended in return
                        // must be linked to the end of the method execution
                        String peekId = nextNodeIdStack.peek();
                        linkNodes(thenNode.getId(), peekId);
                        lastStatementWasReturn = false;
                    }
                }

                currentParentNodeId = afterNode.getId();

                scan(CtRole.COMMENT, ifElement.getComments());
                exit(ifElement);
            }

            @Override
            public <T> void visitCtConditional(CtConditional<T> conditional) {
                super.visitCtConditional(conditional);
            }

            @Override
            public <S> void visitCtCase(CtCase<S> caseStatement) {
                super.visitCtCase(caseStatement);
            }

            @Override
            public void visitCtTryWithResource(CtTryWithResource tryWithResource) {
                super.visitCtTryWithResource(tryWithResource);
            }

            @Override
            public void visitCtTry(CtTry tryBlock) {
                super.visitCtTry(tryBlock);
            }

            @Override
            public void visitCtCatch(CtCatch catchBlock) {
                super.visitCtCatch(catchBlock);
            }

            @Override
            public void visitCtThrow(CtThrow throwStatement) {
                super.visitCtThrow(throwStatement);
            }

            @Override
            public <T> void visitCtInvocation(CtInvocation<T> invocation) {
                String originNode = new String(currentParentNodeId);
                Node beginNode = createGraphNode(invocation.toString());
                Node endNode = createGraphNode(invocation.toString() + "END");

                nextNodeIdStack.push(endNode.getId());
                currentParentNodeId = beginNode.getId();

                super.visitCtInvocation(invocation);
                visitCtAbstractInvocation(invocation);

                // check
//                if (getGraphNode(beginNode.getId()).getEntitiesSequence().size() == 0 &&
//                        getGraphNode(endNode.getId()).getEntitiesSequence().size() == 0) {
//                    removeGraphNode(beginNode.getId());
//                    removeGraphNode(endNode.getId());
//                    currentParentNodeId = originNode;
//                    nextNodeIdStack.pop();
//                }
//                else {
                    linkNodes(originNode, beginNode.getId());
                    String popId = nextNodeIdStack.pop();
                    linkNodes(currentParentNodeId, popId);
                    currentParentNodeId = popId;
//                }

                lastStatementWasReturn = false;
            }

            @Override
            public <T> void visitCtConstructorCall(CtConstructorCall<T> ctConstructorCall) {
                super.visitCtConstructorCall(ctConstructorCall);
                visitCtAbstractInvocation(ctConstructorCall);

//                String popId = nextNodeIdStack.pop();
//                linkNodes(currentParentNodeId, popId);
//                currentParentNodeId = popId;
//
//                lastStatementWasReturn = false;
            }
        });

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
}
