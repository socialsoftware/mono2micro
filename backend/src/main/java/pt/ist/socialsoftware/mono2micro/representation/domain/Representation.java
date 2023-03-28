package pt.ist.socialsoftware.mono2micro.representation.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import pt.ist.socialsoftware.mono2micro.codebase.domain.Codebase;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static pt.ist.socialsoftware.mono2micro.representation.domain.AccessesRepresentation.ACCESSES;
import static pt.ist.socialsoftware.mono2micro.representation.domain.AuthorRepresentation.AUTHOR;
import static pt.ist.socialsoftware.mono2micro.representation.domain.CodeEmbeddingsRepresentation.CODE_EMBEDDINGS;
import static pt.ist.socialsoftware.mono2micro.representation.domain.CommitRepresentation.COMMIT;
import static pt.ist.socialsoftware.mono2micro.representation.domain.EntityToIDRepresentation.ENTITY_TO_ID;
import static pt.ist.socialsoftware.mono2micro.representation.domain.IDToEntityRepresentation.ID_TO_ENTITY;

/**
 * Represents an imported file during the creation of the Codebase
 * Inheritors might contain additional information relevant to the imported file
 */
public abstract class Representation {
    public static final String ACCESSES_TYPE = "Accesses Based";
    public static final String REPOSITORY_TYPE = "Repository Based";
    public static final String CODE_EMBEDDINGS_TYPE = "Code Embeddings Based";

    public static final Map<String, List<String>> representationGroupToRepresentations = Stream.of(
            new AbstractMap.SimpleImmutableEntry<>(ACCESSES_TYPE, new ArrayList<>(Arrays.asList(ID_TO_ENTITY, ACCESSES))),
            new AbstractMap.SimpleImmutableEntry<>(REPOSITORY_TYPE, new ArrayList<>(Arrays.asList(ID_TO_ENTITY, ACCESSES, AUTHOR, COMMIT))),
            new AbstractMap.SimpleImmutableEntry<>(CODE_EMBEDDINGS_TYPE, new ArrayList<>(Arrays.asList(ID_TO_ENTITY, ENTITY_TO_ID, ACCESSES, CODE_EMBEDDINGS)))
    ).collect(Collectors.toMap(AbstractMap.SimpleImmutableEntry::getKey, AbstractMap.SimpleImmutableEntry::getValue));

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
