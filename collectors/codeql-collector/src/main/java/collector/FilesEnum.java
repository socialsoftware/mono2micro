package collector;

/**
 * Contains JSON file names
 */
public enum FilesEnum {

    // Output file names
    ENTITYTOID("entityToID.json"),
    IDTOENTITY("IDToEntity.json"),
    STRUCTURE("structure.json"),
    ACCESSES("accesses.json"),
    // Common queries
    ENTITY_FIELDS("EntityFields.json"),
    ENTITY_SUPERCLASS("EntitySuperclass.json"),
    FUNCTION_ACCESSES("FunctionAccesses.json"),
    ENDPOINTS("Endpoints.json"),
    CALLS("Calls.json"),
    // Framework specific queries
    ENTITY_ATTRIBUTES("EntityAttributes.json"),
    CALL_QUALIFIER("CallQualifier.json"),
    FIELD_ANNOTATIONS("FieldAnnotations.json"),
    NAMED_QUERIES("NamedQueries.json"),
    REPO_ACCESSES("RepoAccesses.json"),
    FUNCTION_ATTRIBUTES("FunctionAttributes.json");

    public final String file;

    FilesEnum(String file) {
        this.file = file;
    }

}
