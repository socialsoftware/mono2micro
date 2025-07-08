package collector;

import collector.queryresults.Endpoints;
import collector.queryresults.EntityFields;
import collector.queryresults.EntitySuperclass;
import collector.queryresults.FileParser;
import collector.utils.Access;
import collector.utils.DomainEntity;
import collector.jpa.ReposityMethodUtils;
import collector.utils.Function;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Logger;

import static collector.Constants.*;
import static collector.FilesEnum.*;
import static collector.utils.TypeUtils.setFieldType;

public abstract class AbstractStructuralCollector {
    private static final Logger logger = Logger.getLogger(AbstractStructuralCollector.class.getName());

    // Common Object Mapper
    protected ObjectMapper mapper;
    // Class to generate JSON files
    protected JSONFileGenerator jsonFileGenerator;
    // Class to execute the CodeQL queries
    protected CodeQLQueryExecutor codeQLQueryExecutor;
    // Project's configuration
    protected Configuration config;
    // Map entity location to Domain Entity object
    protected Map<String, DomainEntity> locationToEntityMap;
    // Repository method utils
    protected ReposityMethodUtils reposityMethodUtils;
    // CodeQL file parser
    protected FileParser fileParser;
    // Map each function to all functions called by it
    protected Map<String, List<Function>> reachableMap;
    // List of endpoint functions
    protected List<Endpoints> endpointFunctionList;
    // Map each endpoint function to its accesses as ArrayNodes
    protected Map<String, ArrayNode> endpointFunctionAccessMap;
    // Map each function's id to its accesses
    protected Map<String, List<Access>> accessMap;

    public AbstractStructuralCollector(Configuration config, FileParser fileParser) {
        this.jsonFileGenerator = new JSONFileGenerator();
        this.config = config;
        this.fileParser = fileParser;
        this.codeQLQueryExecutor = new CodeQLQueryExecutor(config);
        this.mapper = new ObjectMapper();
        this.locationToEntityMap = new HashMap<>();
        this.reposityMethodUtils = new ReposityMethodUtils();
        this.reachableMap = new HashMap<>();
        this.endpointFunctionList = new ArrayList<>();
        this.endpointFunctionAccessMap = new HashMap<>();
        this.accessMap = new HashMap<>();
    }

    public AbstractStructuralCollector(Configuration config) {
        this(config, new FileParser());
    }

    public void collect() {
        // Step 1: Run the CodeQL common queries and save output as JSONs
        runAndDecodeQueries();
        // Step 2: Generate IdToEntity and EntityToID files and save mappings
        generateIdEntityFiles();
        // Step 3: Generate the Structure file
        generateStructureFile();
        // Step 4: Generate the Accesses file
        generateAccessesFile();

        logger.info("Code collection complete");
    }

    public void runAndDecodeQueries() {
        // Check if run queries flag is on
        if (!config.isRunQueries()) return;
        // Run all common queries
        codeQLQueryExecutor.runAndDecodeCommonQueries();
        // Run framework specific queries
        codeQLQueryExecutor.runQueriesInWithLibrary(
                config.getProperties().getSpecificFolderPath(),
                config.getProperties().getLanguageLibraryPath()
        );
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
                // Instantiate new Domain Entity
                DomainEntity de = new DomainEntity(id, es);
                // Store the domain entities in a map for later
                locationToEntityMap.put(es.getEntityLocation(), de);
                entityToIDNode.put(es.getEntityName(), id);
                idToEntityNode.put(String.valueOf(id), es.getEntityName());
                id++;
            }

            // Write the output JSON files
            jsonFileGenerator.outputToJson(mapper, config.getProjectName() + "-" + ENTITYTOID.file, entityToIDNode);
            jsonFileGenerator.outputToJson(mapper, config.getProjectName() + "-" + IDTOENTITY.file, idToEntityNode);
            logger.info("Entity to ID file created successfully.");
            logger.info("ID to Entity file created successfully.");
        } catch (IOException e) {
            logger.warning("Error processing JSON files for ID-Entity: " + e.getMessage());
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
                fieldNode.put("name", ef.getFieldName());
                // Object node for the field type
                ObjectNode fieldTypeNode = mapper.createObjectNode();
                setFieldType(mapper, fieldTypeNode, ef.getFieldType());

                // Add type to field node
                fieldNode.set("type", fieldTypeNode);

                // Update field in entity - might have to create one
                ObjectNode entityNode = entityMap.getOrDefault(ef.getEntityLocation(), mapper.createObjectNode());

                if (entityMap.containsKey(ef.getEntityLocation())) {
                    ArrayNode fieldArray = (ArrayNode) entityNode.get("fields");
                    // Add new field to fieldArray
                    fieldArray.add(fieldNode);
                } else {
                    entityNode.put("name", ef.getEntityName());
                    // New arrayNode for fields
                    ArrayNode fieldArray = mapper.createArrayNode();
                    // Add field to fieldArray
                    fieldArray.add(fieldNode);
                    entityNode.set("fields", fieldArray);

                    String superclass = locationToEntityMap.get(ef.getEntityLocation()).getSuperclass();
                    if (superclass.equals("Object")) {
                        entityNode.putNull("superclass");
                    } else {
                        // Add superclass to entityNode
                        ObjectNode superclassNode = mapper.createObjectNode();
                        superclassNode.put("name", superclass);
                        entityNode.set("superclass", superclassNode);
                    }
                }

                // Store entity in map
                entityMap.put(ef.getEntityLocation(), entityNode);
            }

            // Add entities without fields
            locationToEntityMap.entrySet()
                .stream()
                .filter(entry -> !entityMap.containsKey(entry.getKey()))
                .forEach(entry -> {
                    // Create a new node for entity
                    ObjectNode entityNode = mapper.createObjectNode();
                    entityNode.put("name", entry.getKey());
                    // Array for the fields - will be empty
                    ArrayNode fieldArray = mapper.createArrayNode();
                    entityNode.set("fields", fieldArray);

                    // Get superclass
                    String superclass = entry.getValue().getSuperclass();
                    if (superclass.equals("Object")) {
                        entityNode.putNull("superclass");
                    } else {
                        ObjectNode superclassNode = mapper.createObjectNode();
                        superclassNode.put("name", superclass);
                        entityNode.set("superclass", superclassNode);
                    }
                    entityMap.put(entry.getValue().getLocation(), entityNode);
                });

            // Add all entities to entities array
            entityMap.forEach((key, value) -> entitiesArray.add(value));
            // Add all entities to global object
            structureNode.set("entities", entitiesArray);
            // Write the output JSON files
            jsonFileGenerator.outputToJson(mapper, config.getProjectName() + "-" + STRUCTURE.file, structureNode);
            logger.info("Structure file created successfully.");
        } catch (IOException e) {
            logger.warning("Error processing JSON files for Structure: " + e.getMessage());
        }
    }

    public void generateAccessesFile() {
        try {
            // Object to store final JSON
            ObjectNode accessesNode = mapper.createObjectNode();

            // Build access map
            buildFunctionAccesses();

            // Get all controller methods
            endpointFunctionList = fileParser.readEndpoints(
                    mapper.readTree(new File(JSON_PATH + ENDPOINTS.file)));

            // Build reachable map
            fileParser.readCalls(mapper.readTree(new File(JSON_PATH + CALLS.file)))
                .forEach(c ->
                    reachableMap
                        .computeIfAbsent(c.getCallerId(), k -> new ArrayList<>())
                        .add(new Function(c.getCalleeId(), c.getCallLocation()))
                );

            // DFS through calls
            performDFSFromEndpoints();

            // Add all entities to entities array
            endpointFunctionAccessMap.forEach((key, value) -> {
                ObjectNode tObjectNode = mapper.createObjectNode();
                ArrayNode idArrayNode = mapper.createArrayNode();
                ObjectNode idInnerObjectNode = mapper.createObjectNode();
                idInnerObjectNode.put("id", 0);
                idInnerObjectNode.set("a", value);
                idArrayNode.add(idInnerObjectNode);
                tObjectNode.set("t", idArrayNode);
                accessesNode.set(key, tObjectNode);
            });

            // Write the output JSON files
            jsonFileGenerator.outputToJson(mapper, config.getProjectName() + "-" + ACCESSES.file, accessesNode);
            logger.info("Accesses file created successfully.");
        } catch (IOException e) {
            logger.warning("Error processing JSON files for Accesses: " + e.getMessage());
        }
    }

    public void performDFSFromEndpoints() {
        for (Endpoints endpoint : endpointFunctionList) {
            Set<String> visitedCallLocations = new HashSet<>();
            // Perform dfs
            dfs(
                // We must visit the endpoint function as part of the DFS
                // Endpoint functions have no call location, so we use functionID twice
                new Function(endpoint.getFunctionId(), endpoint.getFunctionId()),
                visitedCallLocations,
                endpoint.getFunctionFullName()
            );
        }
    }

    private void dfs(
            Function currentFunction,
            Set<String> visitedCallLocations,
            String endpointName) {

        // Don't visit the same function twice
        if (visitedCallLocations.stream().anyMatch(s -> currentFunction.getCallLocation().equals(s))) {
            return;
        }

        checkForAccesses(endpointName, currentFunction);

        visitedCallLocations.add(currentFunction.getCallLocation());

        // For each callee, check for accesses and dfs through its calls
        reachableMap.getOrDefault(currentFunction.getFunctionId(), new ArrayList<>())
            .forEach(m -> dfs(m, visitedCallLocations, endpointName));
    }

    protected abstract void checkForAccesses(String controllerMethodName, Function m);

    protected void buildFunctionAccesses() throws IOException {
        // Read FunctionAccesses file as a list
        fileParser.readFunctionAccesses(mapper.readTree(new File(JSON_PATH + FUNCTION_ACCESSES.file)))
            .forEach(a -> {
                // Get domain entity for the access
                DomainEntity domainEntity = locationToEntityMap.get(a.getEntityLocation());
                // Build method
                Function function = new Function(a.getFunctionId());
                // Add access
                accessMap.computeIfAbsent(a.getFunctionId(), k -> new ArrayList<>())
                    .add(new Access(domainEntity, function, a.getOperation()));
            });
    }

    protected void addEntitySequenceAccess(String controllerMethod, int id, String mode) {
        // Get array with accesses for controller method
        ArrayNode controllerMethodNode = endpointFunctionAccessMap.getOrDefault(controllerMethod, mapper.createArrayNode());
        // create access array with id and mode
        ArrayNode accessArrayNode = mapper.createArrayNode();
        accessArrayNode.add(mode);
        accessArrayNode.add(id);
        controllerMethodNode.add(accessArrayNode);
        // Store access array
        endpointFunctionAccessMap.put(controllerMethod, controllerMethodNode);
    }

    protected DomainEntity getEntityByName(String entityName) {
        for(Map.Entry<String, DomainEntity> entry : locationToEntityMap.entrySet()) {
            if (entry.getValue().getName().equals(entityName)) {
                return entry.getValue();
            }
        }
        return null;
    }

}
