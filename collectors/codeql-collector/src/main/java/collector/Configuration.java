package collector;

public class Configuration {

    private ProjectProperties properties;
    private boolean runQueries;
    private String projectName;
    private String codeQLDbPath;

    public Configuration() {
    }

    public Configuration(boolean runQueries, String projectName, String codeQLDbPath) {
        this.properties = null;
        this.runQueries = runQueries;
        this.projectName = projectName;
        this.codeQLDbPath = codeQLDbPath;
    }

    public Configuration(ProjectProperties properties, boolean runQueries, String projectName, String codeQLDbPath) {
        this.properties = properties;
        this.runQueries = runQueries;
        this.projectName = projectName;
        this.codeQLDbPath = codeQLDbPath;
    }

    public ProjectProperties getProperties() {
        return properties;
    }

    public void setProperties(ProjectProperties properties) {
        this.properties = properties;
    }

    public boolean isRunQueries() {
        return runQueries;
    }

    public void setRunQueries(boolean runQueries) {
        this.runQueries = runQueries;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getCodeQLDbPath() {
        return codeQLDbPath;
    }

    public void setCodeQLDbPath(String codeQLDbPath) {
        this.codeQLDbPath = codeQLDbPath;
    }
}
