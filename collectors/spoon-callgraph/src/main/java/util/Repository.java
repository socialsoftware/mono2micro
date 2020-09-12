package util;

public class Repository {
    private String repositoryClassName;
    private String typeClassName;

    public Repository(String repositoryClassName, String typeClassName) {
        this.repositoryClassName = repositoryClassName;
        this.typeClassName = typeClassName;
    }

    public String getRepositoryClassName() {
        return repositoryClassName;
    }

    public void setRepositoryClassName(String repositoryClassName) {
        this.repositoryClassName = repositoryClassName;
    }

    public String getTypeClassName() {
        return typeClassName;
    }

    public void setTypeClassName(String typeClassName) {
        this.typeClassName = typeClassName;
    }
}
