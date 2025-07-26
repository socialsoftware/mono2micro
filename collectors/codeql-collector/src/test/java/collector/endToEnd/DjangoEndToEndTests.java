package collector.endToEnd;

import java.util.List;

import static collector.TestConstants.DJANGO_CHOICE;

public class DjangoEndToEndTests extends AbstractEndToEndTest {


    @Override
    protected String getChoice() {
        return DJANGO_CHOICE;
    }

    @Override
    protected List<String> getTestIds() {
        return List.of(
            "django-banking-app-test",
            "django-blog-app-test",
            "django-socialmedia-app-test"
        );
    }
}
