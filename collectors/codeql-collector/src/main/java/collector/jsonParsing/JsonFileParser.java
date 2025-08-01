package collector.jsonParsing;

import collector.fenix.queryresults.FunctionAttributes;
import collector.jpa.queryresults.EntityAttributes;
import collector.jpa.queryresults.FieldAnnotations;
import collector.jpa.queryresults.NamedQueries;
import collector.jpa.queryresults.RepoAccesses;
import collector.queryresults.CallQualifier;
import collector.queryresults.Calls;
import collector.queryresults.Endpoints;
import collector.queryresults.EntityFields;
import collector.queryresults.EntitySuperclass;
import collector.queryresults.FunctionAccesses;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

public class JsonFileParser {

    public JsonFileParser() {
    }

    private String getLabel(JsonNode node) {
        if (node.isTextual()) {
            return node.asText();
        }
        JsonNode labelNode = node.get("label");
        return labelNode != null && labelNode.isTextual() ? labelNode.asText() : "";
    }


    public Iterable<EntitySuperclass> readEntitySuperclass(File file) {
        return () -> createIterator(file, tuple -> {
            JsonNode entityNameNode = tuple.get(1);
            JsonNode entityLocationNode = tuple.get(2);
            JsonNode superclassNode = tuple.get(3);

            if (entityNameNode == null || entityLocationNode == null ||  superclassNode == null) {
                return null;
            }

            return new EntitySuperclass(
                getLabel(entityNameNode),
                getLabel(entityLocationNode),
                getLabel(superclassNode)
            );
        });
    }

    public Iterable<EntityFields> readEntityFields(File file) {
        return () -> createIterator(file, tuple -> {
            JsonNode entityNameNode = tuple.get(1);
            JsonNode entityLocationNode = tuple.get(2);
            JsonNode fieldNameNode = tuple.get(3);
            JsonNode fieldTypeNode = tuple.get(4);

            if (entityNameNode == null || entityLocationNode == null || fieldNameNode == null || fieldTypeNode == null) {
                return null;
            }

            return new EntityFields(
                getLabel(entityNameNode),
                getLabel(entityLocationNode),
                getLabel(fieldNameNode),
                getLabel(fieldTypeNode)
            );
        });
    }

    public Iterable<Endpoints> readEndpoints(File file) {
        return () -> createIterator(file, tuple -> {
            JsonNode functionFullNameNode = tuple.get(0);
            JsonNode functionIdNode = tuple.get(1);

            if (functionFullNameNode == null || functionIdNode == null) {
                return null;
            }

            return new Endpoints(
                getLabel(functionFullNameNode),
                getLabel(functionIdNode)
            );
        });
    }

    public Iterable<FunctionAccesses> readFunctionAccesses(File file) {
        return () -> createIterator(file, tuple -> {
            JsonNode functionIdNode = tuple.get(0);
            JsonNode entityNameNode = tuple.get(1);
            JsonNode entityLocationNode = tuple.get(2);
            JsonNode operationNode = tuple.get(3);
            JsonNode accessLocationNode = tuple.get(4);

            if (functionIdNode == null || entityNameNode == null || entityLocationNode == null || operationNode == null || accessLocationNode == null) {
                return null;
            }

            return new FunctionAccesses(
                getLabel(functionIdNode),
                getLabel(entityNameNode),
                getLabel(entityLocationNode),
                getLabel(operationNode),
                getLabel(accessLocationNode)
            );
        });
    }

    public Iterable<Calls> readCalls(File file) {
        return () -> createIterator(file, tuple -> {
            JsonNode callerIdNode = tuple.get(0);
            JsonNode calleeIdNode = tuple.get(1);
            JsonNode callLocationNode = tuple.get(2);

            if (callerIdNode == null || calleeIdNode == null || callLocationNode == null) {
                return null;
            }

            return new Calls(
                getLabel(callerIdNode),
                getLabel(calleeIdNode),
                getLabel(callLocationNode)
            );
        });
    }

    public Iterable<EntityAttributes> readEntityAttributes(File file) {
        return () -> createIterator(file, tuple -> {
            JsonNode entityNameNode = tuple.get(0);
            JsonNode mappedSuperclassNode = tuple.get(1);
            JsonNode tableNameNode = tuple.get(2);
            JsonNode entityAnnotationNode = tuple.get(3);
            JsonNode entityLocationNode = tuple.get(4);

            if (entityNameNode == null || mappedSuperclassNode == null ||  tableNameNode == null || entityAnnotationNode == null || entityLocationNode == null) {
                return null;
            }

            String entityName = entityNameNode.asText();
            if (!tableNameNode.asText().equals("null") && !tableNameNode.asText().equals("\"\"")) {
                entityName = getLabel(tableNameNode);
            } else if (!entityNameNode.asText().equals("null") && !entityNameNode.asText().equals("\"\"")) {
                entityName = getLabel(entityNameNode);
            }

            return new EntityAttributes(
                    getLabel(entityNameNode),
                    getLabel(mappedSuperclassNode).equals("yes"),
                    cleanString(entityName),
                    getLabel(entityLocationNode)
            );
        });
    }

    public Iterable<NamedQueries> readNamedQueries(File file) {
        return () -> createIterator(file, tuple -> {
            JsonNode queryNameNode = tuple.get(0);
            JsonNode queryValueNode = tuple.get(1);
            JsonNode nativeNode = tuple.get(2);

            if (queryNameNode == null || queryValueNode == null || nativeNode == null) {
                return null;
            }

            return new NamedQueries(
                getLabel(queryNameNode),
                getLabel(queryValueNode),
                getLabel(nativeNode).equals("yes")
            );
        });
    }

    public Iterable<FieldAnnotations> readFieldAnnotations(File file) {
        return () -> createIterator(file, tuple -> {
            JsonNode entityLocationNode = tuple.get(0);
            JsonNode declaringTypeNode = tuple.get(1);
            JsonNode typeNode = tuple.get(2);
            JsonNode joinTableNode = tuple.get(3);

            if (entityLocationNode == null || declaringTypeNode == null || typeNode == null || joinTableNode == null) {
                return null;
            }

            return new FieldAnnotations(
                getLabel(entityLocationNode),
                getLabel(declaringTypeNode),
                getLabel(typeNode),
                cleanString(getLabel(joinTableNode))
            );
        });
    }

    public Iterable<CallQualifier> readCallQualifiers(File file) {
        return () -> createIterator(file, tuple -> {
            JsonNode callLocationNode = tuple.get(2);
            JsonNode entityLocationNode = tuple.get(3);

            if (callLocationNode == null || entityLocationNode == null) {
                return null;
            }

            return new CallQualifier(
                getLabel(callLocationNode),
                getLabel(entityLocationNode)
            );
        });
    }

    public Iterable<RepoAccesses> readRepoAccesses(File file) {
        return () -> createIterator(file, tuple -> {
            JsonNode functionIdNode = tuple.get(0);
            JsonNode classNameNode = tuple.get(1);
            JsonNode methodNameNode = tuple.get(2);
            JsonNode entityLocationNode = tuple.get(3);
            JsonNode declaredNode = tuple.get(4);
            JsonNode annotationNode = tuple.get(5); // sql query
            JsonNode nativeNode = tuple.get(6); // whether it's native sql or hql
            JsonNode queryNameNode = tuple.get(7); // name field of query
            JsonNode callLocationNode = tuple.get(8);

            if (functionIdNode == null || classNameNode == null || methodNameNode == null || entityLocationNode == null || declaredNode == null || annotationNode == null || nativeNode == null || callLocationNode == null || queryNameNode == null) {
                return null;
            }

            return new RepoAccesses(
                getLabel(functionIdNode),
                getLabel(classNameNode),
                getLabel(methodNameNode),
                getLabel(entityLocationNode),
                getLabel(declaredNode).equals("yes"),
                getLabel( annotationNode),
        !getLabel(nativeNode).equals("no-query") && !getLabel(nativeNode).equals("false"),
                getLabel(queryNameNode),
                getLabel(callLocationNode)
            );
        });
    }

    public Iterable<FunctionAttributes> readFunctionAttributes(File file) {
        return () -> createIterator(file, tuple -> {
            JsonNode functionIdNode = tuple.get(0);
            JsonNode methodDeclaringTypeNode = tuple.get(1);
            JsonNode methodNameNode = tuple.get(2);
            JsonNode returnTypeNode = tuple.get(3);
            JsonNode paramTypeNode = tuple.get(4);

            if (functionIdNode == null || methodDeclaringTypeNode == null || methodNameNode == null || returnTypeNode == null || paramTypeNode == null) {
                return null;
            }

            return new FunctionAttributes(
                getLabel(functionIdNode),
                getLabel(methodDeclaringTypeNode),
                getLabel(methodNameNode),
                getLabel(returnTypeNode),
                getLabel(paramTypeNode)
            );
        });
    }

    protected String cleanString(String name) {
        if (name == null) {
            return "";
        }
        return name.replace("\"", "");
    }

    private <T> ResultJsonIterator<T> createIterator(File file, ResultMapper<T> mapper) {
        try {
            return new ResultJsonIterator<>(file, mapper);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
