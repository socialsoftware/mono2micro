package collector;

public class MainRunner {

    public static void main(String[] args) {
        ConfigurationManager configManager = new ConfigurationManager(args);
        CollectorManager collectorManager = new CollectorManager(configManager);
        collectorManager.run();
    }

}
