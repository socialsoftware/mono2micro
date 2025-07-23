package collector.fragment;

import collector.AbstractStructuralCollector;
import collector.Configuration;
import collector.FenixFrameworkCollector;
import collector.ProjectProperties;
import collector.queryresults.Endpoints;
import collector.queryresults.EntityFields;
import collector.queryresults.EntitySuperclass;
import collector.queryresults.FunctionAccesses;

import java.util.List;

import static collector.TestConstants.FENIX_FRAGMENT_TEST_NAME;
import static collector.TestConstants.FENIX_FRAGMENT_TEST_PATH;

public class FenixFrameworkFragmentTests extends AbstractFragmentTest {
    @Override
    protected void setUp() {
        // Create config for spring data jpa collector
        Configuration config = new Configuration(
                ProjectProperties.FENIX_FRAMEWORK,
                true,
                FENIX_FRAGMENT_TEST_NAME,
                FENIX_FRAGMENT_TEST_PATH);
        AbstractStructuralCollector collector = new FenixFrameworkCollector(config);
        // Run just the queries
        collector.runAndDecodeQueries();
    }

    @Override
    protected List<EntitySuperclass> getExpectedEntitySuperclassList() {
        return List.of(
            new EntitySuperclass("Author", "", "Author_Base"),
            new EntitySuperclass("Book", "", "Book_Base")
        );
    }

    @Override
    protected List<EntityFields> getExpectedEntityFieldsList() {
        return List.of(
            new EntityFields("Author", "","relationAuthorBooks","DirectRelation<Book,Author>"),
            new EntityFields("Author", "","root","DomainRoot"),
            new EntityFields("Author", "","booksSet","Set<Book>"),
            new EntityFields("Author", "","name","String"),
            new EntityFields("Author", "","relationRootAuthors","DirectRelation<DomainRoot,Author>"),
            new EntityFields("Book", "","author","Author"),
            new EntityFields("Book", "","title","String"),
            new EntityFields("Book", "","relationAuthorBooks","DirectRelation<Book,Author>")
        );
    }

    @Override
    protected List<Endpoints> getExpectedEndpointsList() {
        return List.of(
            new Endpoints("LibraryController.getAllAuthors", ""),
            new Endpoints("LibraryDispatchAction.getAuthors", ""),
            new Endpoints("LibraryDispatchAction.getAuthorsSet", "")
        );
    }

    @Override
    protected List<FunctionAccesses> getExpectedFunctionAccessesList() {
        return List.of();
    }
}
