package collector;

import picocli.CommandLine;

public class MainRunner {
    public static void main(String[] args) {
        int exitCode = new CommandLine(new CliArgs()).execute(args);
        System.exit(exitCode);
    }
}
