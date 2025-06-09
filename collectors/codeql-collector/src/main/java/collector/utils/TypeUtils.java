package collector.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TypeUtils {

    public static List<String> getTypes(String type) {
        if (type.contains("<") && type.contains(">")) {
            int start = type.indexOf('<') + 1;
            int end = type.lastIndexOf('>');
            String[] typeParts = type.substring(start, end).split("\\s*,\\s*");

            return Arrays.stream(typeParts)
                    .flatMap(part -> getTypes(part).stream())
                    .collect(Collectors.toCollection(ArrayList::new));
        }
        return new ArrayList<>(List.of(type));
    }

    public static List<String> getTypesFromSignature(String signature) {
        List<String> types = new ArrayList<>();

        // Find the parameter part inside the parentheses
        int start = signature.indexOf('(');
        int end = signature.indexOf(')');
        if (start == -1 || end == -1 || end < start) {
            return types; // invalid format
        }

        String params = signature.substring(start + 1, end).trim();
        if (params.isEmpty()) {
            return types; // no parameters
        }

        // Split parameters by comma
        String[] paramArray = params.split(",");

        for (String param : paramArray) {
            param = param.trim();

            // Handle array types (e.g., byte[])
            if (param.endsWith("[]")) {
                String baseType = param.substring(0, param.length() - 2);
                String simpleName = baseType.contains(".") ?
                        baseType.substring(baseType.lastIndexOf('.') + 1) : baseType;
                types.add(simpleName + "[]");
            } else {
                // Normal case: strip package name
                String simpleName = param.contains(".") ?
                        param.substring(param.lastIndexOf('.') + 1) : param;
                types.add(simpleName);
            }
        }

        return types;
    }

}
