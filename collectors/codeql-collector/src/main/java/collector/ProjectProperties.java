package collector;

import static collector.Constants.JAVA;
import static collector.Constants.JAVA_LIBRARY;
import static collector.Constants.PYTHON;
import static collector.Constants.PYTHON_LIBRARY;
import static collector.Constants.RUBY;
import static collector.Constants.RUBY_LIBRARY;

public enum ProjectProperties {

    SPRING_DATA_JPA("codeql-queries/spring-data-jpa/", JAVA_LIBRARY, JAVA, "SpringDataJPA"),
    FENIX_FRAMEWORK("codeql-queries/fenix-framework/", JAVA_LIBRARY, JAVA, "FenixFramework"),
    DJANGO("", PYTHON_LIBRARY, PYTHON, "Django"),
    RUBY_ON_RAILS("", RUBY_LIBRARY, RUBY, "RubyOnRails");

    // Path to framework-specific queries, empty string if not necessary
    private final String specificFolderPath;
    // Constant pointing to language library folder
    private final String languageLibraryPath;
    // Constant with language name - will be used by codeql packs
    private final String language;
    // Name of the framework; should match .qll file name without extension
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
