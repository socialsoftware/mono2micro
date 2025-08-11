package collector;

import java.util.logging.Logger;

public class ConfigurationManager {
    private static final Logger logger = Logger.getLogger(ConfigurationManager.class.getName());

    private Configuration config;

    public ConfigurationManager(String[] args) {
        // Check args size
        if (args.length < 4) {
            logger.severe("Different number of args than expected");
            System.exit(1);
        } else if (!args[1].equals("0") && !args[1].equals("1")) {
            logger.severe("RUN_QUERIES flag option must be either 1 or 0");
            System.exit(1);
        }

        config = new Configuration(args[1].equals("1"), args[2], args[3]);

        // ORM option
        switch (args[0]) {
            case "0" -> config.setProperties(ProjectProperties.SPRING_DATA_JPA);
            case "1" -> config.setProperties(ProjectProperties.FENIX_FRAMEWORK);
            case "2" -> config.setProperties(ProjectProperties.DJANGO);
            case "3" -> config.setProperties(ProjectProperties.RUBY_ON_RAILS);
            default -> {
                logger.severe("ORM option " + args[0] + " is not valid");
                System.exit(1);
            }
        }
    }

    public Configuration getConfig() {
        return config;
    }

    public void setConfig(Configuration config) {
        this.config = config;
    }
}
