package collector.fragment;

import collector.AbstractStructuralCollector;
import collector.Configuration;
import collector.ProjectProperties;
import collector.frameworks.RubyOnRailsCollector;
import collector.queryresults.Endpoints;
import collector.queryresults.EntityFields;
import collector.queryresults.EntitySuperclass;
import collector.queryresults.FunctionAccesses;

import java.util.List;

import static collector.TestConstants.RUBY_FRAGMENT_TEST_NAME;
import static collector.TestConstants.RUBY_FRAGMENT_TEST_PATH;

public class RubyOnRailsFragmentTests extends AbstractFragmentTest {
    @Override
    protected void setUp() {
        // Create config for spring data jpa collector
        Configuration config = new Configuration(
            ProjectProperties.RUBY_ON_RAILS,
            true,
            RUBY_FRAGMENT_TEST_NAME,
            RUBY_FRAGMENT_TEST_PATH);
        AbstractStructuralCollector collector = new RubyOnRailsCollector(config);
        // Run just the queries
        collector.runAndDecodeQueries();
    }

    @Override
    protected List<EntitySuperclass> getExpectedEntitySuperclassList() {
        return List.of(
            new EntitySuperclass("ApplicationRecord", "", "Base"),
            new EntitySuperclass("Book", "", "ApplicationRecord"),
            new EntitySuperclass("Ebook", "", "Book")
        );
    }

    @Override
    protected List<EntityFields> getExpectedEntityFieldsList() {
        return List.of(
            new EntityFields("Book", "","published_year","integer"),
            new EntityFields("Book", "","author","string"),
            new EntityFields("Book", "","title","string"),
            new EntityFields("Book", "","created_at","datetime"),
            new EntityFields("Book", "","updated_at","datetime"),
            new EntityFields("Book", "","type","string")
        );
    }

    @Override
    protected List<Endpoints> getExpectedEndpointsList() {
        return List.of(
            new Endpoints("LibraryController.manage", "")
        );
    }

    @Override
    protected List<FunctionAccesses> getExpectedFunctionAccessesList() {
        return List.of(
            new FunctionAccesses("", "Book", "", "W", ""),
            new FunctionAccesses("", "Book", "", "R", ""),
            new FunctionAccesses("", "Ebook", "", "W", "")
        );
    }
}
