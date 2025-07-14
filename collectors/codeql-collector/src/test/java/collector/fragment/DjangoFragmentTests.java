package collector.fragment;

import collector.AbstractStructuralCollector;
import collector.Configuration;
import collector.DjangoCollector;
import collector.ProjectProperties;
import collector.queryresults.Calls;
import collector.queryresults.Endpoints;
import collector.queryresults.EntityFields;
import collector.queryresults.EntitySuperclass;
import collector.queryresults.FunctionAccesses;

import java.util.List;

import static collector.TestConstants.DJANGO_FRAGMENT_TEST_NAME;
import static collector.TestConstants.DJANGO_FRAGMENT_TEST_PATH;

public class DjangoFragmentTests extends AbstractFragmentTest {
    @Override
    protected void setUp() {
        // Create config for spring data jpa collector
        Configuration config = new Configuration(
            ProjectProperties.DJANGO,
            true,
            DJANGO_FRAGMENT_TEST_NAME,
            DJANGO_FRAGMENT_TEST_PATH);
        AbstractStructuralCollector collector = new DjangoCollector(config);
        // Run just the queries
        collector.runAndDecodeQueries();
    }

    @Override
    protected List<EntitySuperclass> getExpectedEntitySuperclassList() {
        return List.of(
            new EntitySuperclass("Animal", "", "models.Model"),
            new EntitySuperclass("Dog", "", "Animal")
        );
    }

    @Override
    protected List<EntityFields> getExpectedEntityFieldsList() {
        return List.of(
            new EntityFields("Animal", "", "age", "IntegerField"),
            new EntityFields("Animal", "", "name", "CharField"),
            new EntityFields("Dog", "", "breed", "CharField")
        );
    }

    @Override
    protected List<Endpoints> getExpectedEndpointsList() {
        return List.of(
            new Endpoints("myapp.views.DogListView", "")
        );
    }

    @Override
    protected List<FunctionAccesses> getExpectedFunctionAccessesList() {
        return List.of(
            new FunctionAccesses("", "Class Dog", "", "W", ""),
            new FunctionAccesses("", "Class Dog", "", "R", ""),
            new FunctionAccesses("", "Class Dog", "", "R", ""),
            new FunctionAccesses("", "Class Dog", "", "R", "")
        );
    }
}
