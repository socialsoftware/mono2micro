package collector.frameworks;

import collector.AbstractStructuralCollector;
import collector.Configuration;
import collector.fenix.queryresults.FenixFunction;
import collector.fenix.queryresults.FunctionAttributes;
import collector.utils.DomainEntity;
import collector.utils.Function;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static collector.Constants.FUNCTION_ATTRIBUTES;
import static collector.utils.TypeUtils.getTypes;

public class FenixFrameworkCollector extends AbstractStructuralCollector {
    private static final Logger logger = Logger.getLogger(FenixFrameworkCollector.class.getName());

    // Map functionId to its function attributes
    private Map<String, FenixFunction> fenixFunctionMap;

    public FenixFrameworkCollector(Configuration config) {
        super(config);
        this.fenixFunctionMap = new HashMap<>();
    }

    @Override
    public void generateAccessesFile() {
        // Populate function attributes map
        buildFunctionAttributes();

        // Call on super to generate accesses
        super.generateAccessesFile();
    }

    private void buildFunctionAttributes() {
        for (FunctionAttributes fa : fileParser.readFunctionAttributes(getFile(FUNCTION_ATTRIBUTES))) {
            fenixFunctionMap
                .computeIfAbsent(fa.getFunctionId(), k -> new FenixFunction(
                    new Function(fa.getFunctionId()),
                    fa.getMethodDeclaringType(),
                    fa.getMethodName(),
                    fa.getReturningType(),
                    new ArrayList<>()
                ))
                .getParams()
                .add(fa.getParamType());
        }
    }

    @Override
    protected void checkForAccesses(String controllerMethodName, Function f) {
        // Get fenixFunction from map
        FenixFunction method = fenixFunctionMap.get(f.getFunctionId());

        if (method == null) {
            return;
        }

        // Check accesses
        if (method.getMethodName().endsWith("_Base")) {
            registerBaseClass(controllerMethodName, method);
        } else if (method.getMethodClass().equals("FenixFramework") && method.getMethodClass().equals("getDomainObject")) {
            registerDomainObject(controllerMethodName, method);
        }
    }

    private void registerDomainObject(String controllerMethodName, FenixFunction method) {
        // Get Domain location from call qualifier
        String qualifierDomainLocation = getQualifierEntityLocationByCallLocation(method.getFunction().getCallLocation());
        // Check for access
        if (locationToEntityMap.containsKey(qualifierDomainLocation))
            addEntitySequenceAccess(controllerMethodName, locationToEntityMap.get(qualifierDomainLocation).getId(), "R");
    }

    private void registerBaseClass(String controllerMethodName, FenixFunction method) {
        String mode = "";
        String returnType = "";
        List<String> argTypes = new ArrayList<>();
        // Analyze each method
        if (method.getMethodClass().endsWith("_Base")) {
            if (method.getMethodName().startsWith("get")) {
                mode = "R";
                returnType = getTypes(method.getReturnType()).get(0);
            } else if (method.getMethodName().startsWith("set") ||
                    method.getMethodName().startsWith("add") ||
                    method.getMethodName().startsWith("remove")) {
                mode = "W";
                argTypes.addAll(method.getParams());
            } else {
                return;
            }
        }

        String methodClassName = method.getMethodClass();
        String baseClassName = methodClassName.substring(0, methodClassName.length() - 5); //remove _Base

        String qualifierDomainLocation = getQualifierEntityLocationByCallLocation(method.getFunction().getCallLocation());

        if (mode.equals("R")) {
            // Get entities by name
            DomainEntity baseClassEntity = getEntityByName(baseClassName);
            DomainEntity returnTypeEntity = getEntityByName(returnType);
            // Check and register accesses
            if (locationToEntityMap.containsKey(qualifierDomainLocation)) {
                addEntitySequenceAccess(controllerMethodName, locationToEntityMap.get(qualifierDomainLocation).getId(), mode);
            } else if (baseClassEntity != null) {
                addEntitySequenceAccess(controllerMethodName, baseClassEntity.getId(), mode);
            }

            // Return Type Read
            if (returnTypeEntity != null) {
                addEntitySequenceAccess(controllerMethodName, returnTypeEntity.getId(), mode);
            }
        }
        else if (mode.equals("W")) {
            // Get entities by name
            DomainEntity baseClassEntity = getEntityByName(baseClassName);
            // Check and register accesses
            if (locationToEntityMap.containsKey(qualifierDomainLocation)) {
                addEntitySequenceAccess(controllerMethodName, locationToEntityMap.get(qualifierDomainLocation).getId(), mode);
            } else if (baseClassEntity != null) {
                addEntitySequenceAccess(controllerMethodName, baseClassEntity.getId(), mode);
            }

            // Argument Types Read
            for (String type : argTypes) {
                DomainEntity typeEntity = getEntityByName(type);
                if (typeEntity != null) {
                    addEntitySequenceAccess(controllerMethodName, typeEntity.getId(), mode);
                }
            }
        }

    }

}
