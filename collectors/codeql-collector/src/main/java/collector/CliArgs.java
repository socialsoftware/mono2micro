package collector;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "collector",
        mixinStandardHelpOptions = true,
        description = "Collects data for a given project and CodeQL database.")
public class CliArgs implements Runnable {

    @Option(
            names = "--framework",
            required = true,
            description = "Framework option (required)"
    )
    String frameworkOption;

    @Option(
            names = "--no-queries",
            description = "Disable running queries (default: enabled)"
    )
    boolean noQueries;

    @Parameters(index = "0", description = "Project name")
    String projectName;

    @Parameters(index = "1", description = "Path to the CodeQL database")
    String dbPath;

    @Option(names = {"-h", "--help"}, usageHelp = true, description = "Show this help message and exit")
    boolean helpRequested;

    @Option(names = {"-V", "--version"}, versionHelp = true, description = "Print version information and exit")
    boolean versionRequested;

    @Override
    public void run() {
        if (helpRequested || versionRequested) {
            return;
        }

        ConfigurationManager configManager = new ConfigurationManager(this);
        CollectorManager collectorManager = new CollectorManager(configManager);
        collectorManager.run();
    }
}
