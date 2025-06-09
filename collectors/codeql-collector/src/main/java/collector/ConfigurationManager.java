package collector;

public class ConfigurationManager {

    private String choice;
    private boolean runQueries;
    private String projectName;
    private String codeQLDbPath;

    public ConfigurationManager(String[] args) {
        // Check args size
        if (args.length < 4) {
            System.err.println("Different number of args than expected");
            System.exit(1);
        }

        // ORM option
        switch (args[0]) {
            case "0" -> this.choice = Constants.SPRING_DATA_JPA;
            case "1" -> this.choice = Constants.FENIX_FRAMEWORK;
            case "2" -> this.choice = Constants.DJANGO;
            default -> {
                System.err.println("ORM option " + args[0] + " is not valid");
                System.exit(1);
            }
        }

        this.runQueries = args[1].equals("1");
        this.projectName = args[2];
        this.codeQLDbPath = args[3];
    }

    public String getChoice() {
        return this.choice;
    }

    public String getCodeQLDbPath() {
        return this.codeQLDbPath;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public boolean isRunQueries() {
        return runQueries;
    }

    public void setRunQueries(boolean runQueries) {
        this.runQueries = runQueries;
    }
}
