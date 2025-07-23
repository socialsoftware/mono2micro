package collector.fragment;

import collector.queryresults.Endpoints;
import collector.queryresults.EntityFields;
import collector.queryresults.EntitySuperclass;
import collector.queryresults.FileParser;
import collector.queryresults.FunctionAccesses;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static collector.Constants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractFragmentTest {

    FileParser fileParser = new FileParser();
    ObjectMapper mapper = new ObjectMapper();

    protected abstract void setUp();
    protected abstract List<EntitySuperclass> getExpectedEntitySuperclassList();
    protected abstract List<EntityFields> getExpectedEntityFieldsList();
    protected abstract List<Endpoints> getExpectedEndpointsList();
    protected abstract List<FunctionAccesses> getExpectedFunctionAccessesList();

    @BeforeAll
    void abstractSetUp() {
        setUp();
    }

    @Test
    void testEntitySuperclassResult() throws IOException {
        List<EntitySuperclass> resultList = fileParser.readEntitySuperclass(
                mapper.readTree(new File(JSON_PATH + ENTITY_SUPERCLASS)));
        List<EntitySuperclass> expectedList = getExpectedEntitySuperclassList();

        assertEquals(resultList.size(), expectedList.size(), "expected and result lists differ in size");
        for (int i = 0; i < resultList.size(); i++) {
            EntitySuperclass expected = resultList.get(i);
            EntitySuperclass actual = expectedList.get(i);

            assertEquals(expected.getEntityName(), actual.getEntityName(), "entityName mismatch at index " + i);
            assertEquals(expected.getSuperclass(), actual.getSuperclass(), "superclass mismatch at index " + i);
        }
    }

    @Test
    void testEntityFieldsResult() throws IOException {
        List<EntityFields> resultList = fileParser.readEntityFields(
                mapper.readTree(new File(JSON_PATH + ENTITY_FIELDS)));
        List<EntityFields> expectedList = getExpectedEntityFieldsList();

        assertEquals(resultList.size(), expectedList.size(), "expected and result lists differ in size");
        for (int i = 0; i < resultList.size(); i++) {
            EntityFields expected = resultList.get(i);
            EntityFields actual = expectedList.get(i);

            assertEquals(expected.getEntityName(), actual.getEntityName(), "entityName mismatch at index " + i);
            assertEquals(expected.getFieldName(), actual.getFieldName(), "fieldName mismatch at index " + i);
            assertEquals(expected.getFieldType(), actual.getFieldType(), "fieldType mismatch at index " + i);
        }
    }

    @Test
    void testEndpointsResult() throws IOException {
        List<Endpoints> resultList = fileParser.readEndpoints(
                mapper.readTree(new File(JSON_PATH + ENDPOINTS)));
        List<Endpoints> expectedList = getExpectedEndpointsList();

        assertEquals(resultList.size(), expectedList.size(), "expected and result lists differ in size");
        for (int i = 0; i < resultList.size(); i++) {
            Endpoints expected = resultList.get(i);
            Endpoints actual = expectedList.get(i);

            assertEquals(expected.getFunctionFullName(), actual.getFunctionFullName(), "functionFullName mismatch at index " + i);
        }
    }

    @Test
    void testFunctionAccessesResult() throws IOException {
        List<FunctionAccesses> resultList = fileParser.readFunctionAccesses(
                mapper.readTree(new File(JSON_PATH + FUNCTION_ACCESSES)));
        List<FunctionAccesses> expectedList = getExpectedFunctionAccessesList();

        assertEquals(resultList.size(), expectedList.size(), "expected and result lists differ in size");
        for (int i = 0; i < resultList.size(); i++) {
            FunctionAccesses expected = resultList.get(i);
            FunctionAccesses actual = expectedList.get(i);

            assertEquals(expected.getEntityName(), actual.getEntityName(), "entityName mismatch at index " + i);
            assertEquals(expected.getOperation(), actual.getOperation(), "operation mismatch at index " + i);
        }
    }

}
