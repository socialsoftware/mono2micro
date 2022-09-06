package pt.ist.socialsoftware.mono2micro.source.dto;

import pt.ist.socialsoftware.mono2micro.source.domain.Source;

public class SourceDto {
    private String name;

    private String type;

    private String codebaseName;

    protected SourceDto(Source source) {
        this.name = source.getName();
        this.type = source.getType();
        this.codebaseName = source.getCodebase().getName();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCodebaseName() {
        return codebaseName;
    }

    public void setCodebaseName(String codebaseName) {
        this.codebaseName = codebaseName;
    }
}
