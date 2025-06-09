package collector;

import collector.fenix.FenixFileParser;
import collector.utils.Method;

import java.util.ArrayList;
import java.util.List;
import static collector.utils.TypeUtils.getTypes;
import static collector.utils.TypeUtils.getTypesFromSignature;

public class FenixFrameworkCollector extends AbstractStructuralCollector {

    public FenixFrameworkCollector(String codeQLDbPath, String projectName, boolean runQueries) {
        super(codeQLDbPath, projectName, runQueries, new FenixFileParser());
        SPECIFIC_FRAMEWORK_PATH = Constants.FENIX_FRAMEWORK;
    }

    @Override
    protected void checkForAccesses(String controllerMethodName, Method m) {
        if (m.getClassName().endsWith("_Base")) {
            registerBaseClass(controllerMethodName, m);
        } else if (m.getClassName().equals("FenixFramework") && m.getMethodName().equals("getDomainObject")) {
            registerDomainObject(controllerMethodName, m);
        }
    }

    @Override
    protected void buildMethodAccesses() {
        // Does nothing for the Fenix framework
    }

    private void registerDomainObject(String controllerMethodName, Method method) {
        String type = calleeQualifierTypeMap.getOrDefault(method.getCallLocation(), "");
        if (nameToEntityMap.containsKey(type))
            addEntitySequenceAccess(controllerMethodName, nameToEntityMap.get(type).getId(), "R");
    }

    private void registerBaseClass(String controllerMethodName, Method method) {
        String mode = "";
        String returnType = "";
        List<String> argTypes = new ArrayList<>();
        // Analyze each method
        if (method.getClassName().endsWith("_Base")) {
            if (method.getMethodName().startsWith("get")) {
                mode = "R";
                returnType = getTypes(method.getReturnType()).get(0);
            } else if (method.getMethodName().startsWith("set") ||
                    method.getMethodName().startsWith("add") ||
                    method.getMethodName().startsWith("remove")) {
                mode = "W";
                argTypes.addAll(getTypesFromSignature(method.getSignature()));
            } else {
                return;
            }
        }

        String methodClassName = method.getClassName();
        String baseClassName = methodClassName.substring(0, methodClassName.length() - 5); //remove _Base

        String resolvedType = calleeQualifierTypeMap.getOrDefault(method.getCallLocation(), "");

        if (mode.equals("R")) {
            // Class Read
            if (nameToEntityMap.containsKey(resolvedType))
                addEntitySequenceAccess(controllerMethodName, nameToEntityMap.get(resolvedType).getId(), mode);
            else if (nameToEntityMap.containsKey(baseClassName))
                addEntitySequenceAccess(controllerMethodName, nameToEntityMap.get(baseClassName).getId(), mode);

            // Return Type Read
            if (nameToEntityMap.containsKey(returnType))
                addEntitySequenceAccess(controllerMethodName, nameToEntityMap.get(returnType).getId(), mode);
        }
        else if (mode.equals("W")) {
            // Class Read
            if (nameToEntityMap.containsKey(resolvedType))
                addEntitySequenceAccess(controllerMethodName, nameToEntityMap.get(resolvedType).getId(), mode);
            else if (nameToEntityMap.containsKey(baseClassName))
                addEntitySequenceAccess(controllerMethodName, nameToEntityMap.get(baseClassName).getId(), mode);

            // Argument Types Read
            for (String type : argTypes) {
                if (nameToEntityMap.containsKey(type))
                    addEntitySequenceAccess(controllerMethodName, nameToEntityMap.get(type).getId(), mode);
            }
        }

    }

}
