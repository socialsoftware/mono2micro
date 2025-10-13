package collector.endToEnd;

import collector.MainRunner;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public abstract class AbstractEndToEndTest {

    protected abstract String getChoice();
    protected abstract List<String> getTestIds();

    @Test
    void validateFilesOutput() throws IOException {
        for (String testId : getTestIds()) {
            String dbPath = String.format("./test-resources/endToEnd/%s/%s-db", testId, testId);
            String expectedBase = String.format("./test-resources/endToEnd/%s/expected-output", testId);
            String outputBase = "./output/";


            // Run the MainRunner
            int exitCode = MainRunner.run(new String[]{"--framework=" + getChoice(), testId, dbPath});
            assertEquals(0, exitCode);

            // Compare results
            compareEntityToID(new File(expectedBase + "/entityToID.json"), new File(outputBase + testId + "_entityToID.json"));
            compareIDToEntity(new File(expectedBase + "/IDToEntity.json"), new File(outputBase + testId + "_IDToEntity.json"));
            compareStructure(new File(expectedBase + "/structure.json"), new File(outputBase + testId + "_structure.json"));
            compareAccess(new File(expectedBase + "/accesses.json"), new File(outputBase + testId + ".json"));
        }
    }

    void compareEntityToID(File expected, File result) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        LinkedHashMap<String, Integer> expectedMap = mapper.readValue(expected, new TypeReference<>(){});
        LinkedHashMap<String, Integer> resultMap = mapper.readValue(result, new TypeReference<>(){});
        assertEquals(expectedMap, resultMap, "EntityToID files differ at file: " + result.getName());
    }

    void compareIDToEntity(File expected, File result) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        LinkedHashMap<Integer, String> expectedMap = mapper.readValue(expected, new TypeReference<>(){});
        LinkedHashMap<Integer, String> resultMap = mapper.readValue(result, new TypeReference<>(){});
        assertEquals(expectedMap, resultMap, "EntityToID files differ at file: " + result.getName());
    }

    void compareStructure(File expected, File result) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode expectedJsonNode = mapper.readTree(expected);
        JsonNode resultJsonNode = mapper.readTree(result);

        ArrayNode expectedEntities = (ArrayNode) expectedJsonNode.get("entities");
        ArrayNode resultEntities = (ArrayNode) resultJsonNode.get("entities");

        List<JsonNode> expectedList = StreamSupport.stream(expectedEntities.spliterator(), false)
                .map(this::normalizeEntity).sorted(Comparator.comparing(JsonNode::toString)).toList();

        List<JsonNode> resultList = StreamSupport.stream(resultEntities.spliterator(), false)
                .map(this::normalizeEntity).sorted(Comparator.comparing(JsonNode::toString)).toList();

        assertEquals(expectedList, resultList, "Structure JSONs differ at file: " + result.getName());
    }

    void compareAccess(File expected, File result) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode expectedJsonNode = mapper.readTree(expected);
        JsonNode resultJsonNode = mapper.readTree(result);

        assertEquals(normalizeAccess(expectedJsonNode), normalizeAccess(resultJsonNode), "Access JSONs differ at file: " + result.getName());
    }

    private JsonNode normalizeAccess(JsonNode node) {
        ObjectNode result = new ObjectMapper().createObjectNode();

        node.fields().forEachRemaining(entry -> {
            ArrayNode tArray = (ArrayNode) entry.getValue().get("t");

            ArrayNode normalizedT = new ObjectMapper().createArrayNode();
            for (JsonNode tItem : tArray) {
                ArrayNode aArray = (ArrayNode) tItem.get("a");

                List<JsonNode> sortedA = StreamSupport.stream(aArray.spliterator(), false)
                        .sorted(Comparator.comparing(JsonNode::toString)).toList();

                ObjectNode newTItem = new ObjectMapper().createObjectNode();
                newTItem.set("id", tItem.get("id"));
                newTItem.set("a", new ObjectMapper().valueToTree(sortedA));
                normalizedT.add(newTItem);
            }

            ObjectNode entryNode = new ObjectMapper().createObjectNode();
            entryNode.set("t", normalizedT);
            result.set(entry.getKey(), entryNode);
        });

        return result;
    }

    private JsonNode normalizeEntity(JsonNode entity) {
        ArrayNode fields = (ArrayNode) entity.get("fields");
        List<JsonNode> sortedFields = StreamSupport.stream(fields.spliterator(), false)
                .sorted(Comparator.comparing(JsonNode::toString)).toList();

        ((ObjectNode) entity).set("fields", new ObjectMapper().valueToTree(sortedFields));
        return entity;
    }

}
