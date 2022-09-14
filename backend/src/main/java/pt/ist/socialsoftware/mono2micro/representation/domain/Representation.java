package pt.ist.socialsoftware.mono2micro.representation.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import pt.ist.socialsoftware.mono2micro.codebase.domain.Codebase;

/**
 * Represents an imported file during the creation of the Codebase
 * Inheritors might contain additional information relevant to the imported file
 */
public abstract class Representation {
    @Id
    protected String name;

    @DBRef(lazy = true)
    protected Codebase codebase;

    public abstract String init(Codebase codebase, byte[] representationFile) throws Exception;

    @JsonIgnore
    public abstract String getType();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Codebase getCodebase() {
        return codebase;
    }

    public void setCodebase(Codebase codebase) {
        this.codebase = codebase;
    }
}
