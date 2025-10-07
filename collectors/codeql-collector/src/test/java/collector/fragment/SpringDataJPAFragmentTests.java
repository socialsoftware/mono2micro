package collector.fragment;

import collector.AbstractStructuralCollector;
import collector.Configuration;
import collector.ProjectProperties;
import collector.frameworks.SpringDataJPACollector;
import collector.jpa.queryresults.EntityAttributes;
import collector.jpa.queryresults.FieldAnnotations;
import collector.jpa.queryresults.NamedQueries;
import collector.jpa.queryresults.RepoAccesses;
import collector.queryresults.Endpoints;
import collector.queryresults.EntityFields;
import collector.queryresults.EntitySuperclass;
import collector.queryresults.FunctionAccesses;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static collector.Constants.CALL_QUALIFIER;
import static collector.Constants.ENTITY_ATTRIBUTES;
import static collector.Constants.FIELD_ANNOTATIONS;
import static collector.Constants.JSON_PATH;
import static collector.Constants.NAMED_QUERIES;
import static collector.Constants.REPO_ACCESSES;
import static collector.TestConstants.SPRING_FRAGMENT_TEST_NAME;
import static collector.TestConstants.SPRING_FRAGMENT_TEST_PATH;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SpringDataJPAFragmentTests extends AbstractFragmentTest {

    @Override
    protected void setUp() {
        // Create config for spring data jpa collector
        Configuration config = new Configuration(
            ProjectProperties.SPRING_DATA_JPA,
            true,
            SPRING_FRAGMENT_TEST_NAME,
            SPRING_FRAGMENT_TEST_PATH);
        AbstractStructuralCollector collector = new SpringDataJPACollector(config);
        // Run just the queries
        collector.runAndDecodeQueries();
    }

    @Override
    protected List<EntitySuperclass> getExpectedEntitySuperclassList() {
        return List.of(
            new EntitySuperclass("Author", "", "BaseEntity"),
            new EntitySuperclass("BaseEntity", "", "Object"),
            new EntitySuperclass("Book", "", "BaseEntity")
        );
    }

    @Override
    protected List<EntityFields> getExpectedEntityFieldsList() {
        return List.of(
            new EntityFields("Author", "", "name", "String"),
            new EntityFields("Author", "", "books", "List<Book>"),
            new EntityFields("BaseEntity", "", "id", "Long"),
            new EntityFields("Book", "", "title", "String"),
            new EntityFields("Book", "", "author", "Author")
        );
    }

    @Override
    protected List<Endpoints> getExpectedEndpointsList() {
        return List.of(
            new Endpoints("BookController.getBookTitle", "")
        );
    }

    @Override
    protected List<FunctionAccesses> getExpectedFunctionAccessesList() {
        return List.of(
            new FunctionAccesses("", "Book", "", "R", ""),
            new FunctionAccesses("", "Book", "", "W", ""),
            new FunctionAccesses("", "Book", "", "W", "")
        );
    }

    private List<EntityAttributes> getExpectedEntityAttributesList() {
        return List.of(
            new EntityAttributes("Author", false, false, "Author", ""),
            new EntityAttributes("BaseEntity", true, false, "BaseEntity", ""),
            new EntityAttributes("Book", false, false, "Book", "")
        );
    }

    private List<FieldAnnotations> getExpectedFieldAnnotationsList() {
        return List.of(
            new FieldAnnotations("", "Author", "List<Book>", "null")
        );
    }

    private List<NamedQueries> getExpectedNamedQueriesList() {
        return List.of(
            new NamedQueries("\"Book.findByTitle\"", "\"SELECT b FROM Book b WHERE b.title = :title\"", false)
        );
    }

    private List<RepoAccesses> getExpectedRepoAccessesList() {
        return List.of(
            new RepoAccesses("org.springframework.data.repository.findById(java.lang.Long)", "CrudRepository<Book,Long>", "findById", "", false, "null", false, "null", "")
        );
    }

    @Test
    void testCallQualifierResult() throws IOException {
        String smt = fileParser.getQualifierEntityLocationByCallLocation(
                mapper.readTree(new File(JSON_PATH + CALL_QUALIFIER)), "");
        assertEquals(smt, "");
    }

    @Test
    void testEntityAttributesResult() throws IOException {
        List<EntityAttributes> resultList = fileParser.readEntityAttributes(
                mapper.readTree(new File(JSON_PATH + ENTITY_ATTRIBUTES)));
        List<EntityAttributes> expectedList = getExpectedEntityAttributesList();

        assertEquals(resultList.size(), expectedList.size(), "expected and result lists differ in size");
        for (int i = 0; i < resultList.size(); i++) {
            EntityAttributes expected = resultList.get(i);
            EntityAttributes actual = expectedList.get(i);

            assertEquals(expected.getEntityName(), actual.getEntityName(), "entityName mismatch at index " + i);
            assertEquals(expected.getMappedSuperclass(), actual.getMappedSuperclass(), "mappedSuperclass mismatch at index " + i);
            assertEquals(expected.getTableName(), actual.getTableName(), "tableName mismatch at index " + i);
        }
    }

    @Test
    void testFieldAnnotationsResult() throws IOException {
        List<FieldAnnotations> resultList = fileParser.readFieldAnnotations(
                mapper.readTree(new File(JSON_PATH + FIELD_ANNOTATIONS)));
        List<FieldAnnotations> expectedList = getExpectedFieldAnnotationsList();

        assertEquals(resultList.size(), expectedList.size(), "expected and result lists differ in size");
        for (int i = 0; i < resultList.size(); i++) {
            FieldAnnotations expected = resultList.get(i);
            FieldAnnotations actual = expectedList.get(i);

            assertEquals(expected.getDeclaringType(), actual.getDeclaringType(), "declaringType mismatch at index " + i);
            assertEquals(expected.getType(), actual.getType(), "type mismatch at index " + i);
            assertEquals(expected.getJoinTable(), actual.getJoinTable(), "joinTable mismatch at index " + i);
        }
    }

    @Test
    void testNamedQueriesResult() throws IOException {
        List<NamedQueries> resultList = fileParser.readNamedQueries(
                mapper.readTree(new File(JSON_PATH + NAMED_QUERIES)));
        List<NamedQueries> expectedList = getExpectedNamedQueriesList();

        assertEquals(resultList.size(), expectedList.size(), "expected and result lists differ in size");
        for (int i = 0; i < resultList.size(); i++) {
            NamedQueries expected = resultList.get(i);
            NamedQueries actual = expectedList.get(i);

            assertEquals(expected.getQueryName(), actual.getQueryName(), "queryName mismatch at index " + i);
            assertEquals(expected.getQueryValue(), actual.getQueryValue(), "queryValue mismatch at index " + i);
            assertEquals(expected.isNative(), actual.isNative(), "isNative mismatch at index " + i);
        }
    }

    @Test
    void testRepoAccessesResult() throws IOException {
        List<RepoAccesses> resultList = fileParser.readRepoAccesses(
                mapper.readTree(new File(JSON_PATH + REPO_ACCESSES)));
        List<RepoAccesses> expectedList = getExpectedRepoAccessesList();

        assertEquals(resultList.size(), expectedList.size(), "expected and result lists differ in size");
        for (int i = 0; i < resultList.size(); i++) {
            RepoAccesses expected = resultList.get(i);
            RepoAccesses actual = expectedList.get(i);

            assertEquals(expected.getClassName(), actual.getClassName(), "className mismatch at index " + i);
            assertEquals(expected.getMethodName(), actual.getMethodName(), "methodName mismatch at index " + i);
            assertEquals(expected.isDeclared(), actual.isDeclared(), "isDeclared mismatch at index " + i);
            assertEquals(expected.getAnnotation(), actual.getAnnotation(), "annotation mismatch at index " + i);
            assertEquals(expected.isNative(), actual.isNative(), "isNative mismatch at index " + i);
            assertEquals(expected.getQueryName(), actual.getQueryName(), "queryName mismatch at index " + i);
        }

    }

}
