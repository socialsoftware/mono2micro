package pt.ist.socialsoftware.mono2micro.domain.source;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = AccessesSource.class, name = Source.SourceType.ACCESSES),
        @JsonSubTypes.Type(value = TranslationSource.class, name = Source.SourceType.TRANSLATION)
})
public abstract class Source {
    protected String codebaseName;
    protected String inputFilePath;

    public abstract void init(String codebaseName, Object inputFile) throws Exception;

    public String getCodebaseName() {
        return codebaseName;
    }

    public void setCodebaseName(String codebaseName) {
        this.codebaseName = codebaseName;
    }

    @JsonIgnore
    public abstract String getType();

    public String getInputFilePath() {
        return inputFilePath;
    }

    public void setInputFilePath(String inputFilePath) {
        this.inputFilePath = inputFilePath;
    }

    public static class SourceType {
        public static final String ACCESSES = "Accesses";
        public static final String TRANSLATION = "IDToEntity";
    }
}
