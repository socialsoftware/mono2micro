package collector.endToEnd;

import java.util.List;

import static collector.TestConstants.RUBY_CHOICE;

public class RubyOnRailsEndToEndTests extends AbstractEndToEndTest {

    @Override
    protected String getChoice() {
        return RUBY_CHOICE;
    }

    @Override
    protected List<String> getTestIds() {
        return List.of(
            "ruby-on-rails-blog-app",
            "ruby-on-rails-realworld-example-app",
            "ruby-on-rails-todo-app"
        );
    }
}
