package collector.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

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

    public static void setFieldType(ObjectMapper mapper, ObjectNode fieldTypeNode, String fieldType) {
        // Remove empty type brackets
        if (fieldType.contains("<>")) {
            fieldType = fieldType.replace("<>", "");
        }
        // If it's a collection add types with parameters
        if (fieldType.contains("<") && fieldType.contains(">")) {
            // Get Parameterized type
            fieldTypeNode.put("name", fieldType.substring(0, fieldType.indexOf('<')).trim());

            ArrayNode paramArrayNode = mapper.createArrayNode();

            getTypes(fieldType)
                    .forEach(
                            param -> {
                                ObjectNode paramTypeNode = mapper.createObjectNode();
                                paramTypeNode.put("name", param);
                                paramArrayNode.add(paramTypeNode);
                            });

            fieldTypeNode.put("parameters", paramArrayNode);
        } else {
            fieldTypeNode.put("name", fieldType);
        }
    }

}
