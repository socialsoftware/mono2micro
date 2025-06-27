package collector;

public class CollectorManager {

    private AbstractStructuralCollector collector;

    public CollectorManager(ConfigurationManager configManager) {
        // Choose collector class based on user configurations
        ProjectProperties choice = configManager.getConfig().getProperties();
        switch (choice) {
            case SPRING_DATA_JPA:
                collector = new SpringDataJPACollector(configManager.getConfig());
                break;
            case FENIX_FRAMEWORK:
                collector = new FenixFrameworkCollector(configManager.getConfig());
                break;
            case DJANGO:
                collector = new DjangoCollector(configManager.getConfig());
                break;
            default:
               throw new IllegalArgumentException("Unsupported choice: " + choice);
        }
    }

    public void run() {
        collector.collect();
    }

}
