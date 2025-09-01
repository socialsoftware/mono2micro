package collector;

import java.util.logging.Logger;

public class ConfigurationManager {
    private static final Logger logger = Logger.getLogger(ConfigurationManager.class.getName());

    private Configuration config;

    public ConfigurationManager(CliArgs cliArgs) {
        // init with queries flag, project name, db path
        config = new Configuration(!cliArgs.noQueries, cliArgs.projectName, cliArgs.dbPath);

        // framework mapping
        switch (cliArgs.frameworkOption.toLowerCase()) {
            case "spring" -> config.setProperties(ProjectProperties.SPRING_DATA_JPA);
            case "fenix"  -> config.setProperties(ProjectProperties.FENIX_FRAMEWORK);
            case "django" -> config.setProperties(ProjectProperties.DJANGO);
            case "rails"  -> config.setProperties(ProjectProperties.RUBY_ON_RAILS);
            default -> {
                logger.severe("Framework option " + cliArgs.frameworkOption + " is not valid");
                System.exit(1);
            }
        }
    }

    public Configuration getConfig() {
        return config;
    }
}
