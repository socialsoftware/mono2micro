package pt.ist.socialsoftware.mono2micro.representation.domain;

import org.springframework.data.mongodb.core.mapping.Document;
import pt.ist.socialsoftware.mono2micro.codebase.domain.Codebase;

@Document("representation")
public class CommitRepresentation extends Representation {
    public static final String COMMIT = "File Changes";

    public CommitRepresentation() {}


    @Override
    public String init(Codebase codebase, byte[] representationFile) {
        this.name = codebase.getName() + " & " + getType();
        this.codebase = codebase;
        return name;
    }

    @Override
    public String getType() {
        return COMMIT;
    }
}
