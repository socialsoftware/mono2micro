package collector;

public class CollectorManager {

    private AbstractStructuralCollector collector;

    public CollectorManager(ConfigurationManager configManager) {
        // Choose collector class based on user configurations
        String choice = configManager.getChoice();
        switch (choice) {
            case Constants.SPRING_DATA_JPA:
                collector = new SpringDataJPACollector(configManager.getCodeQLDbPath(), configManager.getProjectName(), configManager.isRunQueries());
                break;
            case Constants.FENIX_FRAMEWORK:
                collector = new FenixFrameworkCollector(configManager.getCodeQLDbPath(), configManager.getProjectName(), configManager.isRunQueries());
                break;
            case Constants.DJANGO:
                collector = new DjangoCollector(configManager.getCodeQLDbPath(), configManager.getProjectName(), configManager.isRunQueries());
                break;
            default:
               throw new IllegalArgumentException("Unsupported choice: " + choice);
        }
    }

    public void run() {
        collector.collect();
    }

}
