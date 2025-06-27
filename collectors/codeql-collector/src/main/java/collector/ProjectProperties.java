package collector;

import static collector.Constants.JAVA;
import static collector.Constants.JAVA_LIBRARY;
import static collector.Constants.PYTHON;
import static collector.Constants.PYTHON_LIBRARY;

public enum ProjectProperties {

    SPRING_DATA_JPA("codeql-queries/spring-data-jpa/", JAVA_LIBRARY, JAVA, "SpringDataJPA"),
    FENIX_FRAMEWORK("codeql-queries/fenix-framework/", JAVA_LIBRARY, JAVA, "FenixFramework"),
    DJANGO("", PYTHON_LIBRARY, PYTHON, "Django");

    private final String specificFolderPath;
    private final String languageLibraryPath;
    private final String language;
    private final String framework;

    ProjectProperties(String specificFolderPath, String languageLibraryPath, String language, String framework) {
        this.specificFolderPath = specificFolderPath;
        this.languageLibraryPath = languageLibraryPath;
        this.language = language;
        this.framework = framework;
    }

    public String getSpecificFolderPath() {
        return specificFolderPath;
    }

    public String getLanguageLibraryPath() {
        return languageLibraryPath;
    }

    public String getLanguage() {
        return language;
    }

    public String getFramework() {
        return framework;
    }
}
