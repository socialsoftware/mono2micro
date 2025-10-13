package collector;

import picocli.CommandLine;

public class MainRunner {
    public static void main(String[] args) {
        int exitCode = run(args);
        System.exit(exitCode);
    }

    public static int run(String[] args) {
        return new CommandLine(new CliArgs()).execute(args);
    }
}