package collector.django;

import collector.results.Calls;
import collector.results.EntityFields;
import collector.results.EntitySuperclass;
import collector.results.FileParser;
import collector.results.MethodAccesses;
import collector.utils.Method;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class DjangoFileParser extends FileParser {

    @Override
    public List<EntitySuperclass> readEntitySuperclass(JsonNode rootNode) {
        return StreamSupport.stream(rootNode.path("#select").path("tuples").spliterator(), false)
            .map(tuple -> {
                JsonNode entityLabel = tuple.get(0).get("label");
                JsonNode locationLabel = tuple.get(1).get("label");

                if (entityLabel == null || locationLabel == null) {
                    return null;
                }

                return new EntitySuperclass(
                        getName(entityLabel.asText()),
                        "models.Model",
                        false,
                        "",
                        locationLabel.asText()
                );
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    @Override
    public List<EntityFields> readEntityFields(JsonNode rootNode) {
        List<EntityFields> entityFieldsList = StreamSupport
            .stream(rootNode.path("#select").path("tuples").spliterator(), false)
            .map(tuple -> {
                JsonNode entityLabel = tuple.get(0).get("label");
                JsonNode fieldLabel = tuple.get(1).get("label");
                JsonNode fieldTypeLabel = tuple.get(2);
                JsonNode locationTypeLabel = tuple.get(3).get("label");

                if (entityLabel == null || fieldLabel == null || fieldTypeLabel == null || locationTypeLabel == null) {
                    return null;
                }

                return new EntityFields(
                    getName(entityLabel.asText()),
                    fieldLabel.asText(),
                    fieldTypeLabel.asText(),
                    locationTypeLabel.asText()
                );
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        // Remove foreignKey entries that don't reference actual field types (ex: CASCADE)
        Set<String> foreignKeyKeys = entityFieldsList.stream()
            .filter(e -> e.getFieldType().equals("ForeignKey"))
            .map(e -> e.getEntity() + "::" + e.getField())
            .collect(Collectors.toSet());

        // Remove all entries with matching (entity, field)
        entityFieldsList.removeIf(e ->
            foreignKeyKeys.contains(e.getEntity() + "::" + e.getField()) && !e.getFieldType().equals("ForeignKey"));

        return entityFieldsList;
    }

    @Override
    public List<Calls> readCalls(JsonNode rootNode) {
        return StreamSupport.stream(rootNode.path("#select").path("tuples").spliterator(), false)
            .map(tuple -> {
                JsonNode callerClass = tuple.get(0).get("label");
                JsonNode callerMethod = tuple.get(1).get("label");
                JsonNode callerLocation = tuple.get(2).get("label");
                JsonNode calleeClass = tuple.get(3).get("label");
                JsonNode calleeMethod = tuple.get(4).get("label");
                JsonNode calleeLocation = tuple.get(5).get("label");
                JsonNode callLocation = tuple.get(6).get("label");

                if (callerClass == null || callerMethod == null || callerLocation == null || calleeClass == null || calleeMethod == null || calleeLocation == null || callLocation == null) {
                    return null;
                }

                return new Calls(
                        getName(callerClass.asText()),
                        getName(callerMethod.asText()),
                        callerLocation.asText(),
                        getName(calleeClass.asText()),
                        getName(calleeMethod.asText()),
                        calleeLocation.asText(),
                        callLocation.asText()
                );
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    @Override
    public List<Method> readEndpoints(JsonNode rootNode) {
        List<Method> endpoints = super.readEndpoints(rootNode);

        endpoints.forEach(ep -> {
            ep.setClassName(getName(ep.getClassName()));
            ep.setMethodName(getName(ep.getMethodName()));
        });

        return endpoints;
    }

    @Override
    public List<MethodAccesses> readMethodAccesses(JsonNode rootNode) {
        return StreamSupport.stream(rootNode.path("#select").path("tuples").spliterator(), false)
            .map(tuple -> {
                JsonNode classLabelNode = tuple.get(0).get("label");
                JsonNode methodLabelNode = tuple.get(1).get("label");
                JsonNode entityLabelNode = tuple.get(2);
                JsonNode operationLabelNode = tuple.get(3);
                JsonNode callLocationLabelNode = tuple.get(4).get("label");

                if (classLabelNode == null || methodLabelNode == null || entityLabelNode == null || operationLabelNode == null || callLocationLabelNode == null) {
                    return null;
                }

                return new MethodAccesses(
                    getName(classLabelNode.asText()),
                    getName(methodLabelNode.asText()),
                    entityLabelNode.asText(),
                    operationLabelNode.asText(),
                    callLocationLabelNode.asText()
                );
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    private String getName(String name) {
        if (name.contains(" ")) {
            return name.split(" ")[1];
        } else {
            return name;
        }
    }

}
