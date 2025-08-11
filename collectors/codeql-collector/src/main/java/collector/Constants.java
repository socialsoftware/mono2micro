package collector;

public class Constants {

    // PATHS
    public static final String JSON_PATH = "./json/";
    public static final String OUTPUT_PATH = "./output/";
    public static final String TEMPLATE_QUERIES_PATH = "template-queries/";
    public static final String GENERATED_QUERIES_PATH = "generated-queries/";
    public static final String QUERY_COLLECTION_PATH = "./codeql-queries/";

    // LANGUAGES
    public static final String JAVA = "java";
    public static final String PYTHON = "python";
    public static final String RUBY = "ruby";

    // LANGUAGE LIBRARY PATHS
    public static final String JAVA_LIBRARY = "codeql-queries/java-library/";
    public static final String PYTHON_LIBRARY = "codeql-queries/python-library/";
    public static final String RUBY_LIBRARY = "codeql-queries/ruby-library/";

    // FILE NAMES
    public static final String ENTITYTOID = "entityToID.json";
    public static final String IDTOENTITY = "IDToEntity.json";
    public static final String STRUCTURE = "structure.json";
    public static final String ACCESSES = "accesses.json";
    public static final String ENTITY_FIELDS = "EntityFields.json";
    public static final String ENTITY_SUPERCLASS = "EntitySuperclass.json";
    public static final String FUNCTION_ACCESSES = "FunctionAccesses.json";
    public static final String ENDPOINTS = "Endpoints.json";
    public static final String CALLS = "Calls.json";
    public static final String ENTITY_ATTRIBUTES = "EntityAttributes.json";
    public static final String CALL_QUALIFIER = "CallQualifier.json";
    public static final String FIELD_ANNOTATIONS = "FieldAnnotations.json";
    public static final String NAMED_QUERIES = "NamedQueries.json";
    public static final String REPO_ACCESSES = "RepoAccesses.json";
    public static final String FUNCTION_ATTRIBUTES = "FunctionAttributes.json";

}
