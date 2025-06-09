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
    // Input files
    ENTITY_FIELDS("entityFields.json"),
    ENTITY_SUPERCLASS("entitySuperclass.json"),
    PREV_CALLEE("prevCallee.json"),
    REPO_ACCESSES("repoAccesses.json"),
    NAMED_QUERIES("namedQueries.json"),
    FIELD_ANNOTATIONS("fieldAnnotations.json"),
    METHOD_ACCESSES("methodAccesses.json"),
    ENDPOINTS("endpoints.json"),
    CALLS("calls.json");

    public final String file;

    FilesEnum(String file) {
        this.file = file;
    }

}
