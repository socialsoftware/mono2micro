package pt.ist.socialsoftware.mono2micro.representation.domain;

import org.springframework.data.mongodb.core.mapping.Document;
import pt.ist.socialsoftware.mono2micro.codebase.domain.Codebase;


@Document("representation")
public class CodeEmbeddingsRepresentation extends Representation {

    public static final String CODE_EMBEDDINGS = "Code Embeddings";

    public CodeEmbeddingsRepresentation() {}

    @Override
    public String init(Codebase codebase, byte[] representationFile) {
        this.name = codebase.getName() + " & " + getType();
        this.codebase = codebase;
        return name;
    }

    @Override
    public String getType() {
        return CODE_EMBEDDINGS;
    }
}
