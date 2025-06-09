package collector;

import collector.results.EntityFields;
import collector.results.EntitySuperclass;
import collector.results.FileParser;
import collector.utils.Access;
import collector.utils.DomainEntity;
import collector.jpa.ReposityMethodUtils;
import collector.utils.Method;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static collector.Constants.JSON_PATH;
import static collector.Constants.QUERY_COLLECTION_PATH;
import static collector.FilesEnum.ACCESSES;
import static collector.FilesEnum.CALLS;
import static collector.FilesEnum.ENDPOINTS;
import static collector.FilesEnum.ENTITYTOID;
import static collector.FilesEnum.ENTITY_FIELDS;
import static collector.FilesEnum.ENTITY_SUPERCLASS;
import static collector.FilesEnum.IDTOENTITY;
import static collector.FilesEnum.METHOD_ACCESSES;
import static collector.FilesEnum.PREV_CALLEE;
import static collector.FilesEnum.STRUCTURE;
import static collector.utils.TypeUtils.getTypes;

public abstract class AbstractStructuralCollector {
    // Path to queries for selected framework
    protected String SPECIFIC_FRAMEWORK_PATH = null;
    // Common Object Mapper
    protected ObjectMapper mapper;
    // Class to generate JSON files
    protected JSONFileGenerator jsonFileGenerator;
    // Class to execute the CodeQL queries
    protected CodeQLQueryExecutor codeQLQueryExecutor;
    // Path to CodeQL database
    protected String codeQLDbPath;
    // Map entity name to Domain Entity representation
    protected Map<String, DomainEntity> nameToEntityMap;
    // Repository method utils
    protected ReposityMethodUtils reposityMethodUtils;
    // CodeQL file parser
    protected FileParser fileParser;
    // Project name
    protected String projectName;
    // Query option
    protected boolean runQueries;
    // Reachable methods for all methods
    protected Map<String, List<Method>> reachableMap;
    // List of controller methods
    protected List<Method> controllerMethodList;
    // List of accesses for each controller method
    protected Map<String, ArrayNode> controllerMethodAccessMap;
    // Map each call to the qualifier type
    protected Map<String, String> calleeQualifierTypeMap;
    // Store accesses by method
    protected Map<String, List<Access>> accessMap;

    public AbstractStructuralCollector(String codeQLDbPath, String projectName, boolean runQueries, FileParser fileParser) {
        this.jsonFileGenerator = new JSONFileGenerator();
        this.codeQLQueryExecutor = new CodeQLQueryExecutor(codeQLDbPath);
        this.codeQLDbPath = codeQLDbPath;
        this.mapper = new ObjectMapper();
        this.nameToEntityMap = new HashMap<>();
        this.reposityMethodUtils = new ReposityMethodUtils();
        this.fileParser = fileParser;
        this.projectName = projectName;
        this.runQueries = runQueries;
        this.reachableMap = new HashMap<>();
        this.controllerMethodList = new ArrayList<>();
        this.controllerMethodAccessMap = new HashMap<>();
        this.calleeQualifierTypeMap = new HashMap<>();
        this.accessMap = new HashMap<>();
    }

    public void collect() {
        try {
            // Step 1: Run the CodeQL Queries and save output as JSONs
            runAndDecodeCodeQLQueries(SPECIFIC_FRAMEWORK_PATH);

            // Step 2: Generate IdToEntity and EntityToID files and save mappings
            generateIdEntityFiles();

            // Step 3: Generate the Structure file
            generateStructureFile();

            // Step 4: Generate the Accesses file
            generateAccessesFile();

            System.out.println("Code collection complete");
        } catch (IOException | InterruptedException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void runAndDecodeCodeQLQueries(String queriesFolderPath) throws IOException, InterruptedException {
        if (!this.runQueries) return;

        // Create the output directory if it doesn't exist
        Files.createDirectories(Paths.get(JSON_PATH));

        // List all .ql files in the specified queries folder
        Files.list(Paths.get(QUERY_COLLECTION_PATH + queriesFolderPath))
                .filter(file -> file.toString().endsWith(".ql"))
                .forEach(queryPath -> codeQLQueryExecutor.runAndDecodeCodeQLQuery(queryPath));
    }

    public void generateIdEntityFiles() {
        try {
            // Create a new JSON object for entityToID output
            ObjectNode entityToIDNode = mapper.createObjectNode();
            // Create a new JSON object for IDToEntity output
            ObjectNode idToEntityNode = mapper.createObjectNode();
            // Starting id value
            int id = 1;

            // Read entitySuperclasses file as a list
            List<EntitySuperclass> entitySuperclasses = fileParser.readEntitySuperclass(
                    mapper.readTree(new File(JSON_PATH + ENTITY_SUPERCLASS.file)));

            for (EntitySuperclass es : entitySuperclasses) {
                // Check for repeating entities
                if (nameToEntityMap.containsKey(es.getEntity())) continue;
                // Store the domain entities in a map for later
                nameToEntityMap.put(es.getEntity(),
                        new DomainEntity(id, es.getEntity(), es.isMappedSuperclass(), es.getSuperclass(), es.getTableName(), es.getLocation()));
                entityToIDNode.put(es.getEntity(), id);
                idToEntityNode.put(String.valueOf(id), es.getEntity());
                id++;
            }

            // Write the output JSON files
            jsonFileGenerator.outputToJson(mapper, projectName + "-" + ENTITYTOID.file, entityToIDNode);
            jsonFileGenerator.outputToJson(mapper, projectName + "-" + IDTOENTITY.file, idToEntityNode);
            System.out.println("Entity to ID file created successfully.");
            System.out.println("ID to Entity file created successfully.");
        } catch (IOException e) {
            System.err.println("Error processing JSON files: " + e.getMessage());
        }
    }

    public void generateStructureFile() {
        try {
            // Map to store all entities before putting them in output JSON
            Map<String, ObjectNode> entityMap = new TreeMap<>();
            // Create a new JSON object for structure file
            ObjectNode structureNode = mapper.createObjectNode();
            // Create entities array
            ArrayNode entitiesArray = mapper.createArrayNode();

            // Read entityFields file as a list
            List<EntityFields> entityFields = fileParser.readEntityFields(
                    mapper.readTree(new File(JSON_PATH + ENTITY_FIELDS.file)));

            for (EntityFields ef : entityFields) {
                // Object node for the field name
                ObjectNode fieldNode = mapper.createObjectNode();
                fieldNode.put("name", ef.getField());
                // Object node for the field type
                ObjectNode fieldTypeNode = mapper.createObjectNode();
                setFieldType(fieldTypeNode, ef.getFieldType());

                // Add type to field node
                fieldNode.put("type", fieldTypeNode);

                // Update field in entity - might have to create one
                ObjectNode entityNode = entityMap.getOrDefault(ef.getLocation(), mapper.createObjectNode());

                if (entityMap.containsKey(ef.getLocation())) {
                    ArrayNode fieldArray = (ArrayNode) entityNode.get("fields");
                    // Add new field to fieldArray
                    fieldArray.add(fieldNode);
                } else {
                    entityNode.put("name", ef.getEntity());
                    // New arrayNode for fields
                    ArrayNode fieldArray = mapper.createArrayNode();
                    // Add field to fieldArray
                    fieldArray.add(fieldNode);
                    entityNode.put("fields", fieldArray);

                    String superclass = nameToEntityMap.get(ef.getEntity()).getSuperclass();
                    if (superclass.equals("Object")) {
                        entityNode.putNull("superclass");
                    } else {
                        // Add superclass to entityNode
                        ObjectNode superclassNode = mapper.createObjectNode();
                        superclassNode.put("name", superclass);
                        entityNode.put("superclass", superclassNode);
                    }
                }

                // Store entity in map
                entityMap.put(ef.getLocation(), entityNode);
            }

            // Add entities without fields
            nameToEntityMap.entrySet()
                .stream()
                .filter(entry -> !entityMap.containsKey(entry.getValue().getLocation()))
                .forEach(entry -> {
                    // Create a new node for entity
                    ObjectNode entityNode = mapper.createObjectNode();
                    entityNode.put("name", entry.getKey());
                    // Array for the fields - will be empty
                    ArrayNode fieldArray = mapper.createArrayNode();
                    entityNode.put("fields", fieldArray);

                    // Get superclass
                    String superclass = entry.getValue().getSuperclass();
                    if (superclass.equals("Object")) {
                        entityNode.putNull("superclass");
                    } else {
                        ObjectNode superclassNode = mapper.createObjectNode();
                        superclassNode.put("name", superclass);
                        entityNode.put("superclass", superclassNode);
                    }
                    entityMap.put(entry.getValue().getLocation(), entityNode);
                });

            // Add all entities to entities array
            entityMap.forEach((key, value) -> entitiesArray.add(value));
            // Add all entities to global object
            structureNode.put("entities", entitiesArray);
            // Write the output JSON files
            jsonFileGenerator.outputToJson(mapper, projectName + "-" + STRUCTURE.file, structureNode);
            System.out.println("Structure file created successfully.");
        } catch (IOException e) {
            System.err.println("Error processing JSON files: " + e.getMessage());
        }
    }

    public void generateAccessesFile() {
        try {
            // Object to store final JSON
            ObjectNode accessesNode = mapper.createObjectNode();

            // Build access map
            buildMethodAccesses();

            // Get all controller methods
            controllerMethodList = fileParser.readEndpoints(mapper.readTree(new File(JSON_PATH + ENDPOINTS.file)));

            // Build reachable map
            fileParser.readCalls(mapper.readTree(new File(JSON_PATH + CALLS.file)))
                .forEach(c -> {
                    // Get list of reachable methods by caller
                    List<Method> reachableMethods = reachableMap.getOrDefault(c.getCallerLocation(), new ArrayList<>());
                    // Get callee
                    Method callee = new Method(c.getCalleeClass(), c.getCalleeMethod(), c.getCalleeLocation(), c.getCallLocation(), c.getCalleeRetType(), c.getCalleeSignature());
                    reachableMethods.add(callee);
                    reachableMap.put(c.getCallerLocation(), reachableMethods);
                });

            buildPrevCalleeQualiferMap();

            // DFS through calls
            performDFSFromControllers();

            // Add all entities to entities array
            controllerMethodAccessMap.forEach((key, value) -> {
                ObjectNode tObjectNode = mapper.createObjectNode();
                ArrayNode idArrayNode = mapper.createArrayNode();
                ObjectNode idInnerObjectNode = mapper.createObjectNode();
                idInnerObjectNode.put("id", 0);
                idInnerObjectNode.put("a", value);
                idArrayNode.add(idInnerObjectNode);
                tObjectNode.put("t", idArrayNode);
                accessesNode.put(key, tObjectNode);
            });

            // Write the output JSON files
            jsonFileGenerator.outputToJson(mapper, projectName + "-" + ACCESSES.file, accessesNode);
            System.out.println("Accesses file created successfully.");
        } catch (IOException e) {
            System.err.println("Error processing JSON files: " + e.getMessage());
        }
    }

    /**
     * Perform a DFS starting at each controller method's call
     */
    public void performDFSFromControllers() {
        for (Method controllerMethod : controllerMethodList) {
            Set<String> visitedCallLocations = new HashSet<>();
            // Perform dfs
            dfs(
                controllerMethod,
                visitedCallLocations,
                controllerMethod.getFullMethodName()
            );
        }
    }

    private void dfs(
            Method currentMethod,
            Set<String> visitedCallLocations,
            String controllerMethodName) {

        // Don't visit the same method twice
        if (visitedCallLocations.stream().anyMatch(s -> currentMethod.getCallLocation().equals(s))) {
            return;
        }

        checkForAccesses(controllerMethodName, currentMethod);

        visitedCallLocations.add(currentMethod.getCallLocation());

        // For each callee, check for accesses and dfs through its calls
        reachableMap.getOrDefault(currentMethod.getMethodLocation(), new ArrayList<>())
            .forEach(m -> dfs(m, visitedCallLocations, controllerMethodName));
    }

    protected abstract void checkForAccesses(String controllerMethodName, Method m);

    protected void buildMethodAccesses() throws IOException {
        // Read methodAccesses file as a list
        fileParser.readMethodAccesses(mapper.readTree(new File(JSON_PATH + METHOD_ACCESSES.file)))
            .forEach(a -> {
                // Get domain entity for the access
                DomainEntity domainEntity = nameToEntityMap.get(a.getEntity());
                // Build method
                Method method = new Method(a.getTargetClass(), a.getTargetMethod(), a.getCallLocation());
                // Add access
                accessMap.computeIfAbsent(a.getFullName(), k -> new ArrayList<>()).add(
                        new Access(domainEntity, method, a.getOperation()));
            });
    }

    protected void buildPrevCalleeQualiferMap() throws IOException {
        // Build qualifier type map
        fileParser.readPrevCallee(mapper.readTree(new File(JSON_PATH + PREV_CALLEE.file)))
            .forEach(p -> {
                calleeQualifierTypeMap.put(p.getLocation(), p.getPrevType());
            });
    }

    protected void addEntitySequenceAccess(String controllerMethod, int id, String mode) {
        // Get array with accesses for controller method
        ArrayNode controllerMethodNode = controllerMethodAccessMap.getOrDefault(controllerMethod, mapper.createArrayNode());
        // create access array with id and mode
        ArrayNode accessArrayNode = mapper.createArrayNode();
        accessArrayNode.add(mode);
        accessArrayNode.add(id);
        controllerMethodNode.add(accessArrayNode);
        // Store access array
        controllerMethodAccessMap.put(controllerMethod, controllerMethodNode);
    }

    private void setFieldType(ObjectNode fieldTypeNode, String fieldType) {
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
