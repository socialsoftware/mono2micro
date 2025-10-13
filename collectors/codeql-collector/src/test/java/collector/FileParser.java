package collector;

import collector.fenix.queryresults.FunctionAttributes;
import collector.jpa.queryresults.EntityAttributes;
import collector.jpa.queryresults.FieldAnnotations;
import collector.jpa.queryresults.NamedQueries;
import collector.jpa.queryresults.RepoAccesses;
import collector.queryresults.Calls;
import collector.queryresults.Endpoints;
import collector.queryresults.EntityFields;
import collector.queryresults.EntitySuperclass;
import collector.queryresults.FunctionAccesses;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class FileParser {

    protected JsonNode getField(JsonNode tuple, int index) {
        JsonNode node = tuple.get(index);
        if (node != null && node.has("label")) {
            return node.get("label");
        }
        return node;
    }

    public List<EntitySuperclass> readEntitySuperclass(JsonNode rootNode) {
        return StreamSupport.stream(rootNode.path("#select").path("tuples").spliterator(), false)
                .map(tuple -> {
                    JsonNode entityNameNode = getField(tuple, 1);
                    JsonNode entityLocationNode = getField(tuple, 2);
                    JsonNode superclassNode = getField(tuple, 3);

                    if (entityNameNode == null || entityLocationNode == null ||  superclassNode == null) {
                        return null;
                    }

                    return new EntitySuperclass(
                            entityNameNode.asText(),
                            entityLocationNode.asText(),
                            superclassNode.asText()
                    );
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<EntityFields> readEntityFields(JsonNode rootNode) {
        return StreamSupport.stream(rootNode.path("#select").path("tuples").spliterator(), false)
                .map(tuple -> {
                    JsonNode entityNameNode = getField(tuple, 1);
                    JsonNode entityLocationNode = getField(tuple, 2);
                    JsonNode fieldNameNode = getField(tuple, 3);
                    JsonNode fieldTypeNode = getField(tuple, 4);

                    if (entityNameNode == null || entityLocationNode == null || fieldNameNode == null || fieldTypeNode == null) {
                        return null;
                    }

                    return new EntityFields(
                            entityNameNode.asText(),
                            entityLocationNode.asText(),
                            fieldNameNode.asText(),
                            fieldTypeNode.asText()
                    );
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<Endpoints> readEndpoints(JsonNode rootNode) {
        return StreamSupport.stream(rootNode.path("#select").path("tuples").spliterator(), false)
                .map(tuple -> {
                    JsonNode functionFullNameNode = getField(tuple, 0);
                    JsonNode functionIdNode = getField(tuple, 1);

                    if (functionFullNameNode == null || functionIdNode == null) {
                        return null;
                    }

                    return new Endpoints(
                            functionFullNameNode.asText(),
                            functionIdNode.asText()
                    );
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<FunctionAccesses> readFunctionAccesses(JsonNode rootNode) {
        return StreamSupport.stream(rootNode.path("#select").path("tuples").spliterator(), false)
                .map(tuple -> {
                    JsonNode functionIdNode = getField(tuple, 0);
                    JsonNode entityNameNode = getField(tuple, 1);
                    JsonNode entityLocationNode = getField(tuple, 2);
                    JsonNode operationNode = getField(tuple, 3);
                    JsonNode accessLocationNode = getField(tuple, 4);

                    if (functionIdNode == null || entityNameNode == null || entityLocationNode == null || operationNode == null || accessLocationNode == null) {
                        return null;
                    }

                    return new FunctionAccesses(
                            functionIdNode.asText(),
                            entityNameNode.asText(),
                            entityLocationNode.asText(),
                            operationNode.asText(),
                            accessLocationNode.asText()
                    );
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<Calls> readCalls(JsonNode rootNode) {
        return StreamSupport.stream(rootNode.path("#select").path("tuples").spliterator(), false)
                .map(tuple -> {
                    JsonNode callerIdNode = getField(tuple, 0);
                    JsonNode calleeIdNode = getField(tuple, 1);
                    JsonNode callLocationNode = getField(tuple, 2);

                    if (callerIdNode == null || calleeIdNode == null || callLocationNode == null) {
                        return null;
                    }

                    return new Calls(
                            callerIdNode.asText(),
                            calleeIdNode.asText(),
                            callLocationNode.asText()
                    );
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<EntityAttributes> readEntityAttributes(JsonNode rootNode) {
        return StreamSupport.stream(rootNode.path("#select").path("tuples").spliterator(), false)
                .map(tuple -> {
                    JsonNode entityNameNode = getField(tuple, 0);
                    JsonNode mappedSuperclassNode = getField(tuple, 1);
                    JsonNode tableNameNode = getField(tuple, 2);
                    JsonNode entityAnnotationNode = getField(tuple, 3);
                    JsonNode entityLocationNode = getField(tuple, 4);

                    if (entityNameNode == null || mappedSuperclassNode == null ||  tableNameNode == null || entityAnnotationNode == null || entityLocationNode == null) {
                        return null;
                    }

                    String entityName = entityNameNode.asText();
                    if (!tableNameNode.asText().equals("null") && !tableNameNode.asText().equals("\"\"")) {
                        entityName = tableNameNode.asText();
                    } else if (!entityNameNode.asText().equals("null") && !entityNameNode.asText().equals("\"\"")) {
                        entityName = entityNameNode.asText();
                    }

                    return new EntityAttributes(
                            entityNameNode.asText(),
                            mappedSuperclassNode.asText().equals("yes"),
                            cleanString(entityName),
                            entityLocationNode.asText()
                    );
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<NamedQueries> readNamedQueries(JsonNode rootNode) {
        return StreamSupport.stream(rootNode.path("#select").path("tuples").spliterator(), false)
                .map(tuple -> {
                    JsonNode queryNameNode = getField(tuple, 0);
                    JsonNode queryValueNode = getField(tuple, 1);
                    JsonNode nativeNode = getField(tuple, 2);

                    if (queryNameNode == null || queryValueNode == null || nativeNode == null) {
                        return null;
                    }

                    return new NamedQueries(
                            queryNameNode.asText(),
                            queryValueNode.asText(),
                            nativeNode.asText().equals("yes")
                    );
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<FieldAnnotations> readFieldAnnotations(JsonNode rootNode) {
        return StreamSupport.stream(rootNode.path("#select").path("tuples").spliterator(), false)
                .map(tuple -> {
                    JsonNode entityLocationNode = getField(tuple, 0);
                    JsonNode declaringTypeNode = getField(tuple, 1);
                    JsonNode typeNode = getField(tuple, 2);
                    JsonNode joinTableNode = getField(tuple, 3);

                    if (entityLocationNode == null || declaringTypeNode == null || typeNode == null || joinTableNode == null) {
                        return null;
                    }

                    return new FieldAnnotations(
                            entityLocationNode.asText(),
                            declaringTypeNode.asText(),
                            typeNode.asText(),
                            cleanString(joinTableNode.asText())
                    );
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public String getQualifierEntityLocationByCallLocation(JsonNode rootNode, String location) {
        return StreamSupport.stream(rootNode.path("#select").path("tuples").spliterator(), false)
                .map(tuple -> {
                    JsonNode callLocationNode = getField(tuple, 2);
                    JsonNode entityLocationNode = getField(tuple, 3);

                    if (callLocationNode == null || entityLocationNode == null) {
                        return null;
                    }

                    if (callLocationNode.asText().equals(location)) {
                        return entityLocationNode.asText();
                    }

                    return null;
                })
                .filter(Objects::nonNull)
                .findFirst()
                .orElse("");
    }

    public List<RepoAccesses> readRepoAccesses(JsonNode rootNode) {
        return StreamSupport.stream(rootNode.path("#select").path("tuples").spliterator(), false)
                .map(tuple -> {
                    JsonNode functionIdNode = getField(tuple, 0);
                    JsonNode classNameNode = getField(tuple, 1);
                    JsonNode methodNameNode = getField(tuple, 2);
                    JsonNode entityLocationNode = getField(tuple, 3);
                    JsonNode declaredNode = getField(tuple, 4);
                    JsonNode annotationNode = getField(tuple, 5); // sql query
                    JsonNode nativeNode = getField(tuple, 6); // whether it's native sql or hql
                    JsonNode queryNameNode = getField(tuple, 7); // name field of query
                    JsonNode callLocationNode = getField(tuple, 8);

                    if (functionIdNode == null || classNameNode == null || methodNameNode == null || entityLocationNode == null || declaredNode == null || annotationNode == null || nativeNode == null || callLocationNode == null || queryNameNode == null) {
                        return null;
                    }

                    return new RepoAccesses(
                            functionIdNode.asText(),
                            classNameNode.asText(),
                            methodNameNode.asText(),
                            entityLocationNode.asText(),
                            declaredNode.asText().equals("yes"),
                            annotationNode.asText(),
                            !nativeNode.asText().equals("no-query") && !nativeNode.asText().equals("false"),
                            queryNameNode.asText(),
                            callLocationNode.asText()
                    );
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<FunctionAttributes> readFunctionAttributes(JsonNode rootNode) {
        return StreamSupport.stream(rootNode.path("#select").path("tuples").spliterator(), false)
                .map(tuple -> {
                    JsonNode functionIdNode = getField(tuple, 0);
                    JsonNode methodDeclaringTypeNode = getField(tuple, 1);
                    JsonNode methodNameNode = getField(tuple, 2);
                    JsonNode returnTypeNode = getField(tuple, 3);
                    JsonNode paramTypeNode = getField(tuple, 4);

                    if (functionIdNode == null || methodDeclaringTypeNode == null || methodNameNode == null || returnTypeNode == null || paramTypeNode == null) {
                        return null;
                    }

                    return new FunctionAttributes(
                            functionIdNode.asText(),
                            methodDeclaringTypeNode.asText(),
                            methodNameNode.asText(),
                            returnTypeNode.asText(),
                            paramTypeNode.asText()
                    );
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    protected String cleanString(String name) {
        return name.replace("\"", "");
    }

}
