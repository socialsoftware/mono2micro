package collector.endToEnd;

import java.util.List;

import static collector.TestConstants.SPRING_CHOICE;

public class SpringDataJPAEndToEndTests extends AbstractEndToEndTest {
    @Override
    protected String getChoice() {
        return SPRING_CHOICE;
    }

    @Override
    protected List<String> getTestIds() {
        return List.of(
            "springboot-Angular-CRUD-Full-Stack-App",
            "springboot-REACT-CRUD-Full-Stack-App",
            "springboot-thymeleaf-crud-pagination-sorting-webapp"
        );
    }
}
