package collector.results;

import collector.utils.Method;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class FileParser {

    public List<EntitySuperclass> readEntitySuperclass(JsonNode rootNode) {
        return StreamSupport.stream(rootNode.path("#select").path("tuples").spliterator(), false)
            .map(tuple -> {
                JsonNode entityLabel = tuple.get(0).get("label");
                JsonNode superclassLabel = tuple.get(1).get("label");
                JsonNode mappedSuperclassLabel = tuple.get(2);
                JsonNode tableNameLabel = tuple.get(3);
                JsonNode entityNameLabel = tuple.get(4);
                JsonNode locationLabel = tuple.get(5).get("label");

                if (entityLabel == null || superclassLabel == null || mappedSuperclassLabel == null || tableNameLabel == null || entityNameLabel == null || locationLabel == null) {
                    return null;
                }

                String entityName = entityLabel.asText();
                if (!tableNameLabel.asText().equals("null") && !tableNameLabel.asText().equals("\"\"")) {
                    entityName = tableNameLabel.asText();
                } else if (!entityNameLabel.asText().equals("null") && !entityNameLabel.asText().equals("\"\"")) {
                    entityName = entityNameLabel.asText();
                }

                return new EntitySuperclass(
                        entityLabel.asText(),
                        superclassLabel.asText(),
                        mappedSuperclassLabel.asText().equals("yes"),
                        cleanString(entityName),
                        locationLabel.asText()
                );
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    public List<EntityFields> readEntityFields(JsonNode rootNode) {
        return StreamSupport.stream(rootNode.path("#select").path("tuples").spliterator(), false)
            .map(tuple -> {
                JsonNode entityLabel = tuple.get(0).get("label");
                JsonNode fieldLabel = tuple.get(1).get("label");
                JsonNode fieldTypeLabel = tuple.get(2).get("label");
                JsonNode locationTypeLabel = tuple.get(3).get("label");

                if (entityLabel == null || fieldLabel == null || fieldTypeLabel == null || locationTypeLabel == null) {
                    return null;
                }

                return new EntityFields(
                        entityLabel.asText(),
                        fieldLabel.asText(),
                        fieldTypeLabel.asText(),
                        locationTypeLabel.asText()
                );
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    public List<NamedQueries> readNamedQueries(JsonNode rootNode) {
        return StreamSupport.stream(rootNode.path("#select").path("tuples").spliterator(), false)
            .map(tuple -> {
                JsonNode queryNameLabelNode = tuple.get(1).get("label");
                JsonNode queryValueLabelNode = tuple.get(2).get("label");
                JsonNode nativeLabelNode = tuple.get(3);

                if (queryNameLabelNode == null || queryValueLabelNode == null || nativeLabelNode == null) {
                    return null;
                }

                return new NamedQueries(
                        queryNameLabelNode.asText(),
                        queryValueLabelNode.asText(),
                        nativeLabelNode.asText().equals("true")
                );
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    public List<Method> readEndpoints(JsonNode rootNode) {
        return StreamSupport.stream(rootNode.path("#select").path("tuples").spliterator(), false)
            .map(tuple -> {
                JsonNode classLabelNode = tuple.get(0).get("label");
                JsonNode methodLabelNode = tuple.get(1).get("label");
                JsonNode locationLabelNode = tuple.get(2).get("label");

                if (classLabelNode == null || methodLabelNode == null || locationLabelNode == null) {
                    return null;
                }

                return new Method(
                        classLabelNode.asText(),
                        methodLabelNode.asText(),
                        locationLabelNode.asText()
                );
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    public List<MethodAccesses> readMethodAccesses(JsonNode rootNode) {
        return StreamSupport.stream(rootNode.path("#select").path("tuples").spliterator(), false)
            .map(tuple -> {
                JsonNode classLabelNode = tuple.get(0).get("label");
                JsonNode methodLabelNode = tuple.get(1).get("label");
                JsonNode entityLabelNode = tuple.get(2).get("label");
                JsonNode operationLabelNode = tuple.get(3);
                JsonNode callLocationLabelNode = tuple.get(4).get("label");

                if (classLabelNode == null || methodLabelNode == null || entityLabelNode == null || operationLabelNode == null || callLocationLabelNode == null) {
                    return null;
                }

                return new MethodAccesses(
                        classLabelNode.asText(),
                        methodLabelNode.asText(),
                        entityLabelNode.asText(),
                        operationLabelNode.asText(),
                        callLocationLabelNode.asText()
                );
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    public String getPrevCalleeByLocation(JsonNode rootNode, String location) {
        return StreamSupport.stream(rootNode.path("#select").path("tuples").spliterator(), false)
            .map(tuple -> {
                JsonNode prevTypeLabelNode = tuple.get(4).get("label");
                JsonNode locationLabelNode = tuple.get(5).get("label");

                if (prevTypeLabelNode == null || locationLabelNode == null) {
                    return null;
                }

                if (locationLabelNode.asText().equals(location)) {
                    return prevTypeLabelNode.asText();
                }

                return null;
            })
            .filter(Objects::nonNull)
            .findFirst()
            .orElse("");
    }

    public List<FieldAnnotations> readFieldAnnotations(JsonNode rootNode) {
        return StreamSupport.stream(rootNode.path("#select").path("tuples").spliterator(), false)
            .map(tuple -> {
                JsonNode declaringClassLabelNode = tuple.get(0).get("label");
                JsonNode typeLabelNode = tuple.get(1).get("label");
                JsonNode joinTableLabelNode = tuple.get(2);

                if (declaringClassLabelNode == null || typeLabelNode == null || joinTableLabelNode == null) {
                    return null;
                }

                return new FieldAnnotations(
                        declaringClassLabelNode.asText(),
                        typeLabelNode.asText(),
                        cleanString(joinTableLabelNode.asText())
                );
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    public List<RepoAccesses> readRepoAccesses(JsonNode rootNode) {
        return StreamSupport.stream(rootNode.path("#select").path("tuples").spliterator(), false)
            .map(tuple -> {
                JsonNode repoClassName = tuple.get(0).get("label");
                JsonNode repoMethodName = tuple.get(1).get("label");
                JsonNode entityLabelNode = tuple.get(2).get("label");
                JsonNode declaredLabelNode = tuple.get(3);
                JsonNode annotationLabelNode = tuple.get(4); // sql query
                JsonNode nativeLabelNode = tuple.get(5); // whether it's native sql or hql
                JsonNode queryNameLabelNode = tuple.get(6); // name field of query
                JsonNode callLocationLabelNode = tuple.get(7).get("label");

                if (repoClassName == null || repoMethodName == null || entityLabelNode == null || declaredLabelNode == null || annotationLabelNode == null || nativeLabelNode == null || callLocationLabelNode == null || queryNameLabelNode == null) {
                    return null;
                }

                return new RepoAccesses(
                        repoClassName.asText(),
                        repoMethodName.asText(),
                        entityLabelNode.asText(),
                        declaredLabelNode.asText().equals("yes"),
                        annotationLabelNode.asText(),
                        !nativeLabelNode.asText().equals("no-query") && !nativeLabelNode.asText().equals("false"),
                        queryNameLabelNode.asText(),
                        callLocationLabelNode.asText()
                );
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    public List<PrevCallee> readPrevCallee(JsonNode rootNode) {
        return StreamSupport.stream(rootNode.path("#select").path("tuples").spliterator(), false)
            .map(tuple -> {
                JsonNode prevTypeLabelNode = tuple.get(4).get("label");
                JsonNode locationLabelNode = tuple.get(5).get("label");

                if (prevTypeLabelNode == null || locationLabelNode == null) {
                    return null;
                }

                return new PrevCallee(
                        prevTypeLabelNode.asText(),
                        locationLabelNode.asText()
                );
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    public List<Calls> readCalls(JsonNode rootNode) {
        return StreamSupport.stream(rootNode.path("#select").path("tuples").spliterator(), false)
            .map(tuple -> {
                JsonNode callerClass = tuple.get(0).get("label");
                JsonNode callerMethod = tuple.get(1).get("label");
                JsonNode callerLocation = tuple.get(2).get("label");
                JsonNode calleeClass = tuple.get(3).get("label");
                JsonNode calleeMethod = tuple.get(4).get("label");
                JsonNode calleeLocation = tuple.get(5).get("label");
                JsonNode calleeRetType = tuple.get(6).get("label");
                JsonNode calleeSignature = tuple.get(7);
                JsonNode callLocation = tuple.get(8).get("label");

                if (callerClass == null || callerMethod == null || callerLocation == null || calleeClass == null || calleeMethod == null || calleeLocation == null || calleeRetType == null || calleeSignature == null || callLocation == null) {
                    return null;
                }

                return new Calls(
                    callerClass.asText(),
                    callerMethod.asText(),
                    callerLocation.asText(),
                    calleeClass.asText(),
                    calleeMethod.asText(),
                    calleeLocation.asText(),
                    calleeRetType.asText(),
                    calleeSignature.asText(),
                    callLocation.asText()
                );
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    private String cleanString(String name) {
        if (name.startsWith("\"") && name.endsWith("\"")) {
            name = name.substring(1, name.length() - 1);
        }
        return name;
    }

}
