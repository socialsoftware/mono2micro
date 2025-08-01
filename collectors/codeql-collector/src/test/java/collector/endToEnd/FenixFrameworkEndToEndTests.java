package collector.endToEnd;

import java.util.List;

import static collector.TestConstants.FENIX_CHOICE;

public class FenixFrameworkEndToEndTests extends AbstractEndToEndTest {
    @Override
    protected String getChoice() {
        return FENIX_CHOICE;
    }

    @Override
    protected List<String> getTestIds() {
        return List.of("fenix-banking-app-test");
    }
}
